package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StdDeserializer.
 */
public class StdDeserializer_IPOTest {
    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_001() throws Exception {
        // Combination: a=new Object(), b=new Object()
        Object actual = StdDeserializer._neitherNull(new Object(), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_002() throws Exception {
        // Combination: a=new Object(), b="sample_str"
        Object actual = StdDeserializer._neitherNull(new Object(), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_003() throws Exception {
        // Combination: a=new Object(), b=Integer.valueOf(1)
        Object actual = StdDeserializer._neitherNull(new Object(), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_004() throws Exception {
        // Combination: a="sample_str", b=new Object()
        Object actual = StdDeserializer._neitherNull("sample_str", new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_005() throws Exception {
        // Combination: a="sample_str", b="sample_str"
        Object actual = StdDeserializer._neitherNull("sample_str", "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_006() throws Exception {
        // Combination: a="sample_str", b=Integer.valueOf(1)
        Object actual = StdDeserializer._neitherNull("sample_str", Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_007() throws Exception {
        // Combination: a=Integer.valueOf(1), b=new Object()
        Object actual = StdDeserializer._neitherNull(Integer.valueOf(1), new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_008() throws Exception {
        // Combination: a=Integer.valueOf(1), b="sample_str"
        Object actual = StdDeserializer._neitherNull(Integer.valueOf(1), "sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__neitherNull_pairwise_009() throws Exception {
        // Combination: a=Integer.valueOf(1), b=Integer.valueOf(1)
        Object actual = StdDeserializer._neitherNull(Integer.valueOf(1), Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
