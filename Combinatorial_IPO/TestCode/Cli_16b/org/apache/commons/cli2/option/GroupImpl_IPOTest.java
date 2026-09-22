package org.apache.commons.cli2.option;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for GroupImpl.
 */
public class GroupImpl_IPOTest {
    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_001() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_002() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_003() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_004() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_005() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_006() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_007() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_008() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_009() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_010() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_011() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_012() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_013() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_014() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_015() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_016() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_017() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_018() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_019() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_020() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_021() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_022() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_023() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_024() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_025() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_026() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_027() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_028() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_029() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_030() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_031() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_032() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_033() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_034() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_035() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_036() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_037() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_038() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_039() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_040() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_041() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_042() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_043() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_044() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_045() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_046() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_047() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_048() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_049() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_050() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_051() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_052() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getPrefixes();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_053() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_054() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_055() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_056() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_057() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_058() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_059() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_060() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_061() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_062() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_063() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_064() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_065() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_066() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_067() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_068() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_069() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_070() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_071() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_072() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_073() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_074() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_075() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_076() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_077() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_078() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_079() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_080() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_081() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_082() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_083() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_084() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_085() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_086() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_087() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_088() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_089() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_090() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_091() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_092() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_093() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_094() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_095() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_096() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_097() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_098() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_099() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_100() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_101() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_102() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_103() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_104() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_105() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_106() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_107() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_108() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_109() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_110() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_111() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_112() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_113() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_114() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_115() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_116() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_117() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_118() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_119() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_120() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPrefixes_pairwise_121() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getPrefixes();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_122() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_123() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_124() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_125() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_126() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_127() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_128() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_129() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_130() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_131() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_132() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_133() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_134() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_135() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_136() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_137() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_138() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_139() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_140() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_141() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_142() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_143() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_144() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_145() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_146() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_147() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_148() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_149() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_150() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_151() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_152() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_153() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_154() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_155() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_156() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_157() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_158() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_159() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_160() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_161() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_162() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_163() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_164() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_165() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_166() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_167() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_168() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_169() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_170() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_171() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_172() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_173() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getTriggers();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_174() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_175() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_176() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_177() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_178() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_179() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_180() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_181() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_182() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_183() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_184() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_185() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_186() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_187() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_188() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_189() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_190() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_191() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_192() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_193() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_194() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_195() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_196() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_197() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_198() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_199() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_200() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_201() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_202() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_203() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_204() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_205() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_206() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_207() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_208() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_209() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_210() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_211() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_212() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_213() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_214() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_215() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_216() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_217() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_218() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_219() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_220() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_221() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_222() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_223() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_224() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_225() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_226() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_227() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_228() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_229() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_230() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_231() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_232() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_233() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_234() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_235() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_236() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_237() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_238() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_239() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_240() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_241() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTriggers_pairwise_242() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getTriggers();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableSet", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_243() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_244() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_245() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_246() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_247() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_248() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_249() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_250() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_251() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_252() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_253() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_254() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_255() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_256() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_257() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_258() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_259() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_260() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_261() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_262() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_263() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_264() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_265() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_266() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_267() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_268() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_269() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_270() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_271() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_272() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_273() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_274() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_275() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_276() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_277() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_278() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_279() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_280() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_281() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_282() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_283() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_284() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_285() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_286() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_287() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_288() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_289() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_290() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_291() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_292() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_293() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_294() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getPreferredName();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_295() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_296() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_297() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_298() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_299() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_300() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_301() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_302() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_303() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_304() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_305() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_306() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_307() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_308() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_309() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_310() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_311() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_312() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_313() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_314() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_315() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_316() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_317() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_318() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_319() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_320() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_321() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_322() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_323() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_324() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_325() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_326() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_327() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_328() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_329() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_330() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_331() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_332() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_333() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_334() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_335() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_336() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_337() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_338() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_339() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_340() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_341() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_342() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_343() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_344() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_345() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_346() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_347() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_348() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_349() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_350() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_351() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_352() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_353() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_354() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_355() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_356() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_357() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_358() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_359() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_360() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_361() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_362() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPreferredName_pairwise_363() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getPreferredName();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_364() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_365() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_366() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_367() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_368() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_369() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_370() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_371() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_372() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_373() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_374() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_375() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_376() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_377() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_378() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_379() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_380() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_381() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_382() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_383() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_384() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_385() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_386() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_387() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_388() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_389() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_390() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_391() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_392() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_393() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_394() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_395() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_396() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_397() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_398() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_399() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_400() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_401() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_402() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_403() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_404() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_405() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_406() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_407() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_408() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_409() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_410() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_411() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_412() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_413() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_414() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_415() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getDescription();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_416() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_417() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_418() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_419() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_420() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_421() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_422() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_423() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_424() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_425() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_426() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_427() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_428() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_429() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_430() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_431() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_432() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_433() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_434() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_435() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_436() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_437() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_438() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_439() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_440() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_441() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_442() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_443() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_444() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_445() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_446() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_447() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_448() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_449() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_450() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_451() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_452() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_453() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_454() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_455() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_456() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_457() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_458() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_459() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_460() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_461() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_462() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_463() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_464() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_465() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_466() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_467() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_468() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_469() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_470() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_471() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_472() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_473() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_474() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_475() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_476() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_477() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_478() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_479() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_480() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_481() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_482() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_483() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDescription_pairwise_484() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getDescription();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_485() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_486() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_487() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_488() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_489() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_490() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_491() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_492() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_493() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_494() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_495() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_496() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_497() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_498() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_499() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_500() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_501() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_502() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_503() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_504() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_505() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_506() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_507() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_508() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_509() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_510() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_511() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_512() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_513() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_514() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_515() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_516() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_517() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_518() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_519() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_520() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_521() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_522() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_523() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_524() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_525() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_526() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_527() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_528() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_529() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_530() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_531() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_532() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_533() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_534() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_535() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_536() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getOptions();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_537() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_538() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_539() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_540() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_541() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_542() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_543() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_544() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_545() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_546() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_547() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_548() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_549() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_550() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_551() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_552() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_553() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_554() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_555() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_556() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_557() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_558() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_559() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_560() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_561() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_562() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_563() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_564() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_565() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_566() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_567() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_568() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_569() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_570() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_571() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_572() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_573() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_574() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_575() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_576() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_577() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_578() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_579() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_580() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_581() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_582() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_583() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_584() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_585() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_586() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_587() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_588() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_589() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_590() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_591() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_592() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_593() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_594() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_595() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_596() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_597() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_598() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_599() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_600() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_601() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_602() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_603() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_604() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptions_pairwise_605() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getOptions();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_606() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_607() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_608() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_609() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_610() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_611() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_612() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_613() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_614() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_615() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_616() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_617() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_618() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_619() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_620() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_621() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_622() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_623() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_624() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_625() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_626() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_627() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_628() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_629() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_630() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_631() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_632() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_633() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_634() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_635() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_636() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_637() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_638() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_639() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_640() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_641() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_642() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_643() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_644() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_645() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_646() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_647() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_648() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_649() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_650() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_651() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_652() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_653() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_654() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_655() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_656() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_657() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getAnonymous();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_658() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_659() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_660() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_661() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_662() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_663() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_664() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_665() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_666() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_667() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_668() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_669() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_670() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_671() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_672() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_673() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_674() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_675() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_676() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_677() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_678() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_679() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_680() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_681() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_682() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_683() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_684() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_685() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_686() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_687() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_688() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_689() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_690() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_691() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_692() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_693() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_694() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_695() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_696() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_697() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_698() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_699() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_700() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_701() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_702() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_703() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_704() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_705() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_706() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_707() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_708() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_709() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_710() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_711() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_712() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_713() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_714() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_715() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_716() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_717() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_718() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_719() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_720() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_721() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_722() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_723() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_724() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_725() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getAnonymous_pairwise_726() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getAnonymous();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_727() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_728() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_729() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_730() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_731() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_732() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_733() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_734() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_735() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_736() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_737() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_738() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_739() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_740() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_741() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_742() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_743() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_744() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_745() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_746() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_747() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_748() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_749() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_750() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_751() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_752() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_753() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_754() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_755() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_756() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_757() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_758() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_759() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_760() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_761() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_762() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_763() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_764() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_765() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_766() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_767() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_768() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_769() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_770() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_771() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_772() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_773() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_774() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_775() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_776() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_777() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_778() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getMinimum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_779() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_780() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_781() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_782() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_783() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_784() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_785() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_786() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_787() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_788() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_789() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_790() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_791() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_792() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_793() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_794() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_795() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_796() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_797() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_798() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_799() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_800() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_801() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_802() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_803() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_804() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_805() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_806() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_807() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_808() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_809() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_810() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_811() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_812() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_813() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_814() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_815() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_816() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_817() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_818() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_819() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_820() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_821() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_822() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_823() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_824() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_825() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_826() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_827() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_828() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_829() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_830() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_831() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_832() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_833() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_834() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_835() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_836() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_837() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_838() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_839() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_840() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_841() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_842() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_843() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_844() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_845() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_846() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMinimum_pairwise_847() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getMinimum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_848() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_849() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_850() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_851() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_852() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_853() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_854() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_855() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_856() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_857() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_858() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_859() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_860() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_861() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_862() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_863() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_864() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_865() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_866() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_867() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_868() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_869() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_870() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_871() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_872() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_873() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_874() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_875() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_876() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_877() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_878() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_879() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_880() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_881() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_882() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_883() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_884() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_885() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_886() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_887() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_888() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_889() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_890() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_891() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_892() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_893() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_894() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_895() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_896() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_897() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_898() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_899() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).getMaximum();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_900() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_901() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_902() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_903() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_904() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_905() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_906() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_907() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_908() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_909() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_910() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_911() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_912() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_913() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_914() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_915() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_916() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_917() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_918() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_919() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_920() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_921() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_922() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_923() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_924() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_925() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_926() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_927() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_928() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_929() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_930() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_931() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_932() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_933() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_934() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_935() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_936() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_937() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_938() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_939() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_940() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_941() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_942() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_943() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_944() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_945() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_946() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_947() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_948() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_949() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_950() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_951() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_952() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_953() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_954() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_955() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_956() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_957() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_958() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_959() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_960() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_961() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_962() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_963() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_964() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_965() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_966() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_967() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaximum_pairwise_968() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).getMaximum();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_969() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_970() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description="", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", "", 1, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_971() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "", -1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_972() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "", Integer.MAX_VALUE, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_973() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "", Integer.MIN_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_974() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="a", receiver__description=" ", receiver__minimum=1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "a", " ", 1, 0)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_975() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description=" ", receiver__minimum=0, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", " ", 0, 1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_976() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description=" ", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", " ", Integer.MAX_VALUE, -1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_977() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name=" ", receiver__description=" ", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), " ", " ", -1, Integer.MAX_VALUE)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_978() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description=" ", receiver__minimum=0, receiver__maximum=Integer.MIN_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", " ", 0, Integer.MIN_VALUE)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_979() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="test123", receiver__description="a", receiver__minimum=-1, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "test123", "a", -1, 0)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_980() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="a", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "a", Integer.MAX_VALUE, 1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_981() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="a", receiver__minimum=0, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "a", 0, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_982() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "a", 1, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_983() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="a", receiver__minimum=1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "a", 1, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_984() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="test123", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "test123", Integer.MAX_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_985() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="", receiver__description="test123", receiver__minimum=-1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "", "test123", -1, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_986() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="test123", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "test123", 1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_987() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="test123", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "test123", 0, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_988() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="test123", receiver__minimum=-1, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "test123", -1, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_989() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "!@#", Integer.MIN_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_990() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="!@#", receiver__description="!@#", receiver__minimum=0, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "!@#", "!@#", 0, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_991() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="!@#", receiver__minimum=1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "!@#", 1, -1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_992() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="!@#", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "!@#", -1, Integer.MAX_VALUE)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_993() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="!@#", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "!@#", Integer.MAX_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_994() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_995() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="1.5", receiver__description="0", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "1.5", "0", Integer.MIN_VALUE, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_996() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="0", receiver__minimum=1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "0", 1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_997() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="0", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "0", -1, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_998() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="0", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "0", Integer.MAX_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_999() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1000() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="0", receiver__description="-1", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "0", "-1", 1, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1001() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="-1", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "-1", Integer.MIN_VALUE, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1002() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="-1", receiver__minimum=-1, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "-1", -1, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1003() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", Integer.MAX_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1004() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775807", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775807", "1.5", 0, 0)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1005() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="1.5", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "1.5", 1, 1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1006() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", receiver__minimum=-1, receiver__maximum=-1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", -1, -1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1007() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="1.5", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "1.5", Integer.MIN_VALUE, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1008() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="1.5", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "1.5", Integer.MAX_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1009() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="9223372036854775808", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "9223372036854775808", "9223372036854775807", 0, 0)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1010() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775807", receiver__minimum=1, receiver__maximum=1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775807", 1, 1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1011() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775807", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775807", -1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1012() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775807", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1013() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775807", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1014() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1015() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="-1", receiver__description="9223372036854775808", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "-1", "9223372036854775808", 1, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1016() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="9223372036854775808", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "9223372036854775808", -1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1017() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775808", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1018() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775808", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1019() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1020() throws Exception {
        // Combination: receiver__options=java.util.Arrays.asList("a", "b"), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=1, receiver__maximum=1
        try {
            (new GroupImpl(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1)).isRequired();
            fail("Expected java.lang.ClassCastException");
        } catch (java.lang.ClassCastException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1021() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=-1, receiver__maximum=-1
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1022() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1023() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=Integer.MIN_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1024() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description=" ", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", " ", Integer.MIN_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1025() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="a", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "a", Integer.MIN_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1026() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", Integer.MIN_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1027() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "!@#", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1028() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1029() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1030() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1031() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1032() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="!@#", receiver__minimum=Integer.MIN_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "!@#", Integer.MIN_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1033() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1034() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1035() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1036() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1037() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1038() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "!@#", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1039() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1040() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1041() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1042() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1043() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1044() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "!@#", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1045() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1046() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1047() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1048() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1049() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1050() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", " ", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1051() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "a", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1052() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "test123", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1053() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1054() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="!@#", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "!@#", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1055() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1056() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "a", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1057() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "test123", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1058() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1059() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="0", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "0", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1060() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="", receiver__minimum=Integer.MAX_VALUE, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "", Integer.MAX_VALUE, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1061() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", " ", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1062() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "test123", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1063() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1064() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1065() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1066() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1067() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", " ", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1068() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "a", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1069() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="1.5", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "1.5", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1070() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1071() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1072() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "", 0, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1073() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "a", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1074() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "test123", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1075() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1076() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1077() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1078() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "", 0, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1079() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", " ", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1080() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="test123", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "test123", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1081() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "!@#", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1082() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="-1", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "-1", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1083() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="9223372036854775808", receiver__description="9223372036854775808", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "9223372036854775808", "9223372036854775808", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1084() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", receiver__minimum=0, receiver__maximum=Integer.MAX_VALUE
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", 0, Integer.MAX_VALUE)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1085() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1086() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1087() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1088() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isRequired_pairwise_1089() throws Exception {
        // Combination: receiver__options=java.util.Collections.emptyList(), receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", receiver__minimum=0, receiver__maximum=0
        Object actual = (new GroupImpl(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", 0, 0)).isRequired();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
