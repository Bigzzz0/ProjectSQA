package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StringUtil.
 */
public class StringUtil_IPOTest {
    @Test(timeout = 4000)
    public void test_join_pairwise_001() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep=""
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_002() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep=""
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("ab", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_003() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep=" "
        Object actual = StringUtil.join(java.util.Collections.emptyList(), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_004() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep=" "
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_005() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="a"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_006() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="a"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aab", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_007() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="test123"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_008() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="test123"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("atest123b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_009() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="!@#"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_010() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="!@#"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a!@#b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_011() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="0"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_012() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="0"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a0b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_013() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="-1"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_014() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="-1"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a-1b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_015() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="1.5"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_016() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="1.5"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a1.5b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_017() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="9223372036854775807"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_018() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="9223372036854775807"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a9223372036854775807b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_019() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="9223372036854775808"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_020() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="9223372036854775808"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a9223372036854775808b", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_021() throws Exception {
        // Combination: strings=java.util.Collections.emptyList(), sep="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.join(java.util.Collections.emptyList(), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_022() throws Exception {
        // Combination: strings=java.util.Arrays.asList("a", "b"), sep="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.join(java.util.Arrays.asList("a", "b"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_023() throws Exception {
        // Combination: strings=new String[] {}, sep=""
        Object actual = StringUtil.join(new String[] {}, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_024() throws Exception {
        // Combination: strings=new String[] {"value"}, sep=""
        Object actual = StringUtil.join(new String[] {"value"}, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_025() throws Exception {
        // Combination: strings=new String[] {}, sep=" "
        Object actual = StringUtil.join(new String[] {}, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_026() throws Exception {
        // Combination: strings=new String[] {"value"}, sep=" "
        Object actual = StringUtil.join(new String[] {"value"}, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_027() throws Exception {
        // Combination: strings=new String[] {}, sep="a"
        Object actual = StringUtil.join(new String[] {}, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_028() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="a"
        Object actual = StringUtil.join(new String[] {"value"}, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_029() throws Exception {
        // Combination: strings=new String[] {}, sep="test123"
        Object actual = StringUtil.join(new String[] {}, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_030() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="test123"
        Object actual = StringUtil.join(new String[] {"value"}, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_031() throws Exception {
        // Combination: strings=new String[] {}, sep="!@#"
        Object actual = StringUtil.join(new String[] {}, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_032() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="!@#"
        Object actual = StringUtil.join(new String[] {"value"}, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_033() throws Exception {
        // Combination: strings=new String[] {}, sep="0"
        Object actual = StringUtil.join(new String[] {}, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_034() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="0"
        Object actual = StringUtil.join(new String[] {"value"}, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_035() throws Exception {
        // Combination: strings=new String[] {}, sep="-1"
        Object actual = StringUtil.join(new String[] {}, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_036() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="-1"
        Object actual = StringUtil.join(new String[] {"value"}, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_037() throws Exception {
        // Combination: strings=new String[] {}, sep="1.5"
        Object actual = StringUtil.join(new String[] {}, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_038() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="1.5"
        Object actual = StringUtil.join(new String[] {"value"}, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_039() throws Exception {
        // Combination: strings=new String[] {}, sep="9223372036854775807"
        Object actual = StringUtil.join(new String[] {}, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_040() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="9223372036854775807"
        Object actual = StringUtil.join(new String[] {"value"}, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_041() throws Exception {
        // Combination: strings=new String[] {}, sep="9223372036854775808"
        Object actual = StringUtil.join(new String[] {}, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_042() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="9223372036854775808"
        Object actual = StringUtil.join(new String[] {"value"}, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_043() throws Exception {
        // Combination: strings=new String[] {}, sep="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.join(new String[] {}, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_join_pairwise_044() throws Exception {
        // Combination: strings=new String[] {"value"}, sep="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.join(new String[] {"value"}, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("value", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_045() throws Exception {
        // Combination: needle="", haystack=new String[] {}
        Object actual = StringUtil.in("", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_046() throws Exception {
        // Combination: needle=" ", haystack=new String[] {}
        Object actual = StringUtil.in(" ", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_047() throws Exception {
        // Combination: needle="a", haystack=new String[] {}
        Object actual = StringUtil.in("a", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_048() throws Exception {
        // Combination: needle="test123", haystack=new String[] {}
        Object actual = StringUtil.in("test123", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_049() throws Exception {
        // Combination: needle="!@#", haystack=new String[] {}
        Object actual = StringUtil.in("!@#", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_050() throws Exception {
        // Combination: needle="0", haystack=new String[] {}
        Object actual = StringUtil.in("0", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_051() throws Exception {
        // Combination: needle="-1", haystack=new String[] {}
        Object actual = StringUtil.in("-1", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_052() throws Exception {
        // Combination: needle="1.5", haystack=new String[] {}
        Object actual = StringUtil.in("1.5", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_053() throws Exception {
        // Combination: needle="9223372036854775807", haystack=new String[] {}
        Object actual = StringUtil.in("9223372036854775807", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_054() throws Exception {
        // Combination: needle="9223372036854775808", haystack=new String[] {}
        Object actual = StringUtil.in("9223372036854775808", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_055() throws Exception {
        // Combination: needle="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", haystack=new String[] {}
        Object actual = StringUtil.in("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_056() throws Exception {
        // Combination: needle="", haystack=new String[] {"value"}
        Object actual = StringUtil.in("", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_057() throws Exception {
        // Combination: needle=" ", haystack=new String[] {"value"}
        Object actual = StringUtil.in(" ", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_058() throws Exception {
        // Combination: needle="a", haystack=new String[] {"value"}
        Object actual = StringUtil.in("a", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_059() throws Exception {
        // Combination: needle="test123", haystack=new String[] {"value"}
        Object actual = StringUtil.in("test123", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_060() throws Exception {
        // Combination: needle="!@#", haystack=new String[] {"value"}
        Object actual = StringUtil.in("!@#", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_061() throws Exception {
        // Combination: needle="0", haystack=new String[] {"value"}
        Object actual = StringUtil.in("0", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_062() throws Exception {
        // Combination: needle="-1", haystack=new String[] {"value"}
        Object actual = StringUtil.in("-1", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_063() throws Exception {
        // Combination: needle="1.5", haystack=new String[] {"value"}
        Object actual = StringUtil.in("1.5", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_064() throws Exception {
        // Combination: needle="9223372036854775807", haystack=new String[] {"value"}
        Object actual = StringUtil.in("9223372036854775807", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_065() throws Exception {
        // Combination: needle="9223372036854775808", haystack=new String[] {"value"}
        Object actual = StringUtil.in("9223372036854775808", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_in_pairwise_066() throws Exception {
        // Combination: needle="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", haystack=new String[] {"value"}
        Object actual = StringUtil.in("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_067() throws Exception {
        // Combination: needle="", haystack=new String[] {}
        Object actual = StringUtil.inSorted("", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_068() throws Exception {
        // Combination: needle=" ", haystack=new String[] {}
        Object actual = StringUtil.inSorted(" ", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_069() throws Exception {
        // Combination: needle="a", haystack=new String[] {}
        Object actual = StringUtil.inSorted("a", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_070() throws Exception {
        // Combination: needle="test123", haystack=new String[] {}
        Object actual = StringUtil.inSorted("test123", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_071() throws Exception {
        // Combination: needle="!@#", haystack=new String[] {}
        Object actual = StringUtil.inSorted("!@#", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_072() throws Exception {
        // Combination: needle="0", haystack=new String[] {}
        Object actual = StringUtil.inSorted("0", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_073() throws Exception {
        // Combination: needle="-1", haystack=new String[] {}
        Object actual = StringUtil.inSorted("-1", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_074() throws Exception {
        // Combination: needle="1.5", haystack=new String[] {}
        Object actual = StringUtil.inSorted("1.5", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_075() throws Exception {
        // Combination: needle="9223372036854775807", haystack=new String[] {}
        Object actual = StringUtil.inSorted("9223372036854775807", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_076() throws Exception {
        // Combination: needle="9223372036854775808", haystack=new String[] {}
        Object actual = StringUtil.inSorted("9223372036854775808", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_077() throws Exception {
        // Combination: needle="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", haystack=new String[] {}
        Object actual = StringUtil.inSorted("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_078() throws Exception {
        // Combination: needle="", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_079() throws Exception {
        // Combination: needle=" ", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted(" ", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_080() throws Exception {
        // Combination: needle="a", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("a", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_081() throws Exception {
        // Combination: needle="test123", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("test123", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_082() throws Exception {
        // Combination: needle="!@#", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("!@#", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_083() throws Exception {
        // Combination: needle="0", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("0", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_084() throws Exception {
        // Combination: needle="-1", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("-1", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_085() throws Exception {
        // Combination: needle="1.5", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("1.5", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_086() throws Exception {
        // Combination: needle="9223372036854775807", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("9223372036854775807", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_087() throws Exception {
        // Combination: needle="9223372036854775808", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("9223372036854775808", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inSorted_pairwise_088() throws Exception {
        // Combination: needle="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", haystack=new String[] {"value"}
        Object actual = StringUtil.inSorted("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_089() throws Exception {
        // Combination: baseUrl="", relUrl=""
        Object actual = StringUtil.resolve("", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_090() throws Exception {
        // Combination: baseUrl="", relUrl=" "
        Object actual = StringUtil.resolve("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_091() throws Exception {
        // Combination: baseUrl="", relUrl="a"
        Object actual = StringUtil.resolve("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_092() throws Exception {
        // Combination: baseUrl="", relUrl="test123"
        Object actual = StringUtil.resolve("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_093() throws Exception {
        // Combination: baseUrl="", relUrl="!@#"
        Object actual = StringUtil.resolve("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_094() throws Exception {
        // Combination: baseUrl="", relUrl="0"
        Object actual = StringUtil.resolve("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_095() throws Exception {
        // Combination: baseUrl="", relUrl="-1"
        Object actual = StringUtil.resolve("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_096() throws Exception {
        // Combination: baseUrl="", relUrl="1.5"
        Object actual = StringUtil.resolve("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_097() throws Exception {
        // Combination: baseUrl="", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_098() throws Exception {
        // Combination: baseUrl="", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_099() throws Exception {
        // Combination: baseUrl="", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_100() throws Exception {
        // Combination: baseUrl=" ", relUrl=""
        Object actual = StringUtil.resolve(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_101() throws Exception {
        // Combination: baseUrl=" ", relUrl=" "
        Object actual = StringUtil.resolve(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_102() throws Exception {
        // Combination: baseUrl=" ", relUrl="a"
        Object actual = StringUtil.resolve(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_103() throws Exception {
        // Combination: baseUrl=" ", relUrl="test123"
        Object actual = StringUtil.resolve(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_104() throws Exception {
        // Combination: baseUrl=" ", relUrl="!@#"
        Object actual = StringUtil.resolve(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_105() throws Exception {
        // Combination: baseUrl=" ", relUrl="0"
        Object actual = StringUtil.resolve(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_106() throws Exception {
        // Combination: baseUrl=" ", relUrl="-1"
        Object actual = StringUtil.resolve(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_107() throws Exception {
        // Combination: baseUrl=" ", relUrl="1.5"
        Object actual = StringUtil.resolve(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_108() throws Exception {
        // Combination: baseUrl=" ", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_109() throws Exception {
        // Combination: baseUrl=" ", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_110() throws Exception {
        // Combination: baseUrl=" ", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_111() throws Exception {
        // Combination: baseUrl="a", relUrl=""
        Object actual = StringUtil.resolve("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_112() throws Exception {
        // Combination: baseUrl="a", relUrl=" "
        Object actual = StringUtil.resolve("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_113() throws Exception {
        // Combination: baseUrl="a", relUrl="a"
        Object actual = StringUtil.resolve("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_114() throws Exception {
        // Combination: baseUrl="a", relUrl="test123"
        Object actual = StringUtil.resolve("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_115() throws Exception {
        // Combination: baseUrl="a", relUrl="!@#"
        Object actual = StringUtil.resolve("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_116() throws Exception {
        // Combination: baseUrl="a", relUrl="0"
        Object actual = StringUtil.resolve("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_117() throws Exception {
        // Combination: baseUrl="a", relUrl="-1"
        Object actual = StringUtil.resolve("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_118() throws Exception {
        // Combination: baseUrl="a", relUrl="1.5"
        Object actual = StringUtil.resolve("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_119() throws Exception {
        // Combination: baseUrl="a", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_120() throws Exception {
        // Combination: baseUrl="a", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_121() throws Exception {
        // Combination: baseUrl="a", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_122() throws Exception {
        // Combination: baseUrl="test123", relUrl=""
        Object actual = StringUtil.resolve("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_123() throws Exception {
        // Combination: baseUrl="test123", relUrl=" "
        Object actual = StringUtil.resolve("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_124() throws Exception {
        // Combination: baseUrl="test123", relUrl="a"
        Object actual = StringUtil.resolve("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_125() throws Exception {
        // Combination: baseUrl="test123", relUrl="test123"
        Object actual = StringUtil.resolve("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_126() throws Exception {
        // Combination: baseUrl="test123", relUrl="!@#"
        Object actual = StringUtil.resolve("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_127() throws Exception {
        // Combination: baseUrl="test123", relUrl="0"
        Object actual = StringUtil.resolve("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_128() throws Exception {
        // Combination: baseUrl="test123", relUrl="-1"
        Object actual = StringUtil.resolve("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_129() throws Exception {
        // Combination: baseUrl="test123", relUrl="1.5"
        Object actual = StringUtil.resolve("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_130() throws Exception {
        // Combination: baseUrl="test123", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_131() throws Exception {
        // Combination: baseUrl="test123", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_132() throws Exception {
        // Combination: baseUrl="test123", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_133() throws Exception {
        // Combination: baseUrl="!@#", relUrl=""
        Object actual = StringUtil.resolve("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_134() throws Exception {
        // Combination: baseUrl="!@#", relUrl=" "
        Object actual = StringUtil.resolve("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_135() throws Exception {
        // Combination: baseUrl="!@#", relUrl="a"
        Object actual = StringUtil.resolve("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_136() throws Exception {
        // Combination: baseUrl="!@#", relUrl="test123"
        Object actual = StringUtil.resolve("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_137() throws Exception {
        // Combination: baseUrl="!@#", relUrl="!@#"
        Object actual = StringUtil.resolve("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_138() throws Exception {
        // Combination: baseUrl="!@#", relUrl="0"
        Object actual = StringUtil.resolve("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_139() throws Exception {
        // Combination: baseUrl="!@#", relUrl="-1"
        Object actual = StringUtil.resolve("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_140() throws Exception {
        // Combination: baseUrl="!@#", relUrl="1.5"
        Object actual = StringUtil.resolve("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_141() throws Exception {
        // Combination: baseUrl="!@#", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_142() throws Exception {
        // Combination: baseUrl="!@#", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_143() throws Exception {
        // Combination: baseUrl="!@#", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_144() throws Exception {
        // Combination: baseUrl="0", relUrl=""
        Object actual = StringUtil.resolve("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_145() throws Exception {
        // Combination: baseUrl="0", relUrl=" "
        Object actual = StringUtil.resolve("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_146() throws Exception {
        // Combination: baseUrl="0", relUrl="a"
        Object actual = StringUtil.resolve("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_147() throws Exception {
        // Combination: baseUrl="0", relUrl="test123"
        Object actual = StringUtil.resolve("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_148() throws Exception {
        // Combination: baseUrl="0", relUrl="!@#"
        Object actual = StringUtil.resolve("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_149() throws Exception {
        // Combination: baseUrl="0", relUrl="0"
        Object actual = StringUtil.resolve("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_150() throws Exception {
        // Combination: baseUrl="0", relUrl="-1"
        Object actual = StringUtil.resolve("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_151() throws Exception {
        // Combination: baseUrl="0", relUrl="1.5"
        Object actual = StringUtil.resolve("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_152() throws Exception {
        // Combination: baseUrl="0", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_153() throws Exception {
        // Combination: baseUrl="0", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_154() throws Exception {
        // Combination: baseUrl="0", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_155() throws Exception {
        // Combination: baseUrl="-1", relUrl=""
        Object actual = StringUtil.resolve("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_156() throws Exception {
        // Combination: baseUrl="-1", relUrl=" "
        Object actual = StringUtil.resolve("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_157() throws Exception {
        // Combination: baseUrl="-1", relUrl="a"
        Object actual = StringUtil.resolve("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_158() throws Exception {
        // Combination: baseUrl="-1", relUrl="test123"
        Object actual = StringUtil.resolve("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_159() throws Exception {
        // Combination: baseUrl="-1", relUrl="!@#"
        Object actual = StringUtil.resolve("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_160() throws Exception {
        // Combination: baseUrl="-1", relUrl="0"
        Object actual = StringUtil.resolve("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_161() throws Exception {
        // Combination: baseUrl="-1", relUrl="-1"
        Object actual = StringUtil.resolve("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_162() throws Exception {
        // Combination: baseUrl="-1", relUrl="1.5"
        Object actual = StringUtil.resolve("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_163() throws Exception {
        // Combination: baseUrl="-1", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_164() throws Exception {
        // Combination: baseUrl="-1", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_165() throws Exception {
        // Combination: baseUrl="-1", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_166() throws Exception {
        // Combination: baseUrl="1.5", relUrl=""
        Object actual = StringUtil.resolve("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_167() throws Exception {
        // Combination: baseUrl="1.5", relUrl=" "
        Object actual = StringUtil.resolve("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_168() throws Exception {
        // Combination: baseUrl="1.5", relUrl="a"
        Object actual = StringUtil.resolve("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_169() throws Exception {
        // Combination: baseUrl="1.5", relUrl="test123"
        Object actual = StringUtil.resolve("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_170() throws Exception {
        // Combination: baseUrl="1.5", relUrl="!@#"
        Object actual = StringUtil.resolve("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_171() throws Exception {
        // Combination: baseUrl="1.5", relUrl="0"
        Object actual = StringUtil.resolve("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_172() throws Exception {
        // Combination: baseUrl="1.5", relUrl="-1"
        Object actual = StringUtil.resolve("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_173() throws Exception {
        // Combination: baseUrl="1.5", relUrl="1.5"
        Object actual = StringUtil.resolve("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_174() throws Exception {
        // Combination: baseUrl="1.5", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_175() throws Exception {
        // Combination: baseUrl="1.5", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_176() throws Exception {
        // Combination: baseUrl="1.5", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_177() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl=""
        Object actual = StringUtil.resolve("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_178() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl=" "
        Object actual = StringUtil.resolve("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_179() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="a"
        Object actual = StringUtil.resolve("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_180() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="test123"
        Object actual = StringUtil.resolve("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_181() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="!@#"
        Object actual = StringUtil.resolve("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_182() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="0"
        Object actual = StringUtil.resolve("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_183() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="-1"
        Object actual = StringUtil.resolve("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_184() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="1.5"
        Object actual = StringUtil.resolve("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_185() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_186() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_187() throws Exception {
        // Combination: baseUrl="9223372036854775807", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_188() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl=""
        Object actual = StringUtil.resolve("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_189() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl=" "
        Object actual = StringUtil.resolve("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_190() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="a"
        Object actual = StringUtil.resolve("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_191() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="test123"
        Object actual = StringUtil.resolve("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_192() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="!@#"
        Object actual = StringUtil.resolve("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_193() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="0"
        Object actual = StringUtil.resolve("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_194() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="-1"
        Object actual = StringUtil.resolve("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_195() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="1.5"
        Object actual = StringUtil.resolve("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_196() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_197() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_198() throws Exception {
        // Combination: baseUrl="9223372036854775808", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_199() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl=""
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_200() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl=" "
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_201() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="a"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_202() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="test123"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_203() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="!@#"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_204() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="0"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_205() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="-1"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_206() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="1.5"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_207() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="9223372036854775807"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_208() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="9223372036854775808"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_resolve_pairwise_209() throws Exception {
        // Combination: baseUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", relUrl="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtil.resolve("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

}
