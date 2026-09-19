package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StringUtils.
 */
public class StringUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_001() throws Exception {
        // Combination: text="", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_002() throws Exception {
        // Combination: text=" ", searchList=new String[] {"value"}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach(" ", new String[] {"value"}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_003() throws Exception {
        // Combination: text=" ", searchList=new String[] {}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach(" ", new String[] {}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_004() throws Exception {
        // Combination: text="", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_005() throws Exception {
        // Combination: text="a", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("a", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_006() throws Exception {
        // Combination: text="a", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("a", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_007() throws Exception {
        // Combination: text="test123", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("test123", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_008() throws Exception {
        // Combination: text="test123", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("test123", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_009() throws Exception {
        // Combination: text="!@#", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("!@#", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_010() throws Exception {
        // Combination: text="!@#", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("!@#", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_011() throws Exception {
        // Combination: text="0", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("0", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_012() throws Exception {
        // Combination: text="0", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("0", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_013() throws Exception {
        // Combination: text="-1", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("-1", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_014() throws Exception {
        // Combination: text="-1", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("-1", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_015() throws Exception {
        // Combination: text="1.5", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("1.5", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_016() throws Exception {
        // Combination: text="1.5", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("1.5", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_017() throws Exception {
        // Combination: text="9223372036854775807", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("9223372036854775807", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_018() throws Exception {
        // Combination: text="9223372036854775807", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("9223372036854775807", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_019() throws Exception {
        // Combination: text="9223372036854775808", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("9223372036854775808", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_020() throws Exception {
        // Combination: text="9223372036854775808", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("9223372036854775808", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_021() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchList=new String[] {}, replacementList=new String[] {}
        Object actual = StringUtils.replaceEach("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {}, new String[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_replaceEach_pairwise_022() throws Exception {
        // Combination: text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchList=new String[] {"value"}, replacementList=new String[] {"value"}
        Object actual = StringUtils.replaceEach("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new String[] {"value"}, new String[] {"value"});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

}
