package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for TarArchiveEntry.
 */
public class TarArchiveEntry_IPOTest {
    @Test(timeout = 4000)
    public void test_equals_pairwise_001() throws Exception {
        // Combination: receiver__name="", it=new Object()
        Object actual = (new TarArchiveEntry("")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_002() throws Exception {
        // Combination: receiver__name=" ", it=new Object()
        Object actual = (new TarArchiveEntry(" ")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_003() throws Exception {
        // Combination: receiver__name="a", it=new Object()
        Object actual = (new TarArchiveEntry("a")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_004() throws Exception {
        // Combination: receiver__name="test123", it=new Object()
        Object actual = (new TarArchiveEntry("test123")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_005() throws Exception {
        // Combination: receiver__name="!@#", it=new Object()
        Object actual = (new TarArchiveEntry("!@#")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_006() throws Exception {
        // Combination: receiver__name="0", it=new Object()
        Object actual = (new TarArchiveEntry("0")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_007() throws Exception {
        // Combination: receiver__name="-1", it=new Object()
        Object actual = (new TarArchiveEntry("-1")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_008() throws Exception {
        // Combination: receiver__name="1.5", it=new Object()
        Object actual = (new TarArchiveEntry("1.5")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_009() throws Exception {
        // Combination: receiver__name="9223372036854775807", it=new Object()
        Object actual = (new TarArchiveEntry("9223372036854775807")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_010() throws Exception {
        // Combination: receiver__name="9223372036854775808", it=new Object()
        Object actual = (new TarArchiveEntry("9223372036854775808")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_011() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", it=new Object()
        Object actual = (new TarArchiveEntry("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_012() throws Exception {
        // Combination: receiver__name="", it="sample_str"
        Object actual = (new TarArchiveEntry("")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_013() throws Exception {
        // Combination: receiver__name=" ", it="sample_str"
        Object actual = (new TarArchiveEntry(" ")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_014() throws Exception {
        // Combination: receiver__name="a", it="sample_str"
        Object actual = (new TarArchiveEntry("a")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_015() throws Exception {
        // Combination: receiver__name="test123", it="sample_str"
        Object actual = (new TarArchiveEntry("test123")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_016() throws Exception {
        // Combination: receiver__name="!@#", it="sample_str"
        Object actual = (new TarArchiveEntry("!@#")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_017() throws Exception {
        // Combination: receiver__name="0", it="sample_str"
        Object actual = (new TarArchiveEntry("0")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_018() throws Exception {
        // Combination: receiver__name="-1", it="sample_str"
        Object actual = (new TarArchiveEntry("-1")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_019() throws Exception {
        // Combination: receiver__name="1.5", it="sample_str"
        Object actual = (new TarArchiveEntry("1.5")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_020() throws Exception {
        // Combination: receiver__name="9223372036854775807", it="sample_str"
        Object actual = (new TarArchiveEntry("9223372036854775807")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_021() throws Exception {
        // Combination: receiver__name="9223372036854775808", it="sample_str"
        Object actual = (new TarArchiveEntry("9223372036854775808")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_022() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", it="sample_str"
        Object actual = (new TarArchiveEntry("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_023() throws Exception {
        // Combination: receiver__name="", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_024() throws Exception {
        // Combination: receiver__name=" ", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry(" ")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_025() throws Exception {
        // Combination: receiver__name="a", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("a")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_026() throws Exception {
        // Combination: receiver__name="test123", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("test123")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_027() throws Exception {
        // Combination: receiver__name="!@#", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("!@#")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_028() throws Exception {
        // Combination: receiver__name="0", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("0")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_029() throws Exception {
        // Combination: receiver__name="-1", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("-1")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_030() throws Exception {
        // Combination: receiver__name="1.5", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("1.5")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_031() throws Exception {
        // Combination: receiver__name="9223372036854775807", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("9223372036854775807")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_032() throws Exception {
        // Combination: receiver__name="9223372036854775808", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("9223372036854775808")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_033() throws Exception {
        // Combination: receiver__name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", it=Integer.valueOf(1)
        Object actual = (new TarArchiveEntry("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
