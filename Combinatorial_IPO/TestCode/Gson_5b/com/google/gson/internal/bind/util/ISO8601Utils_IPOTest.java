package com.google.gson.internal.bind.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ISO8601Utils.
 */
public class ISO8601Utils_IPOTest {
    @Test(timeout = 4000)
    public void test_format_pairwise_001() throws Exception {
        // Combination: date=new java.util.Date(0L), millis=true
        Object actual = ISO8601Utils.format(new java.util.Date(0L), true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1970-01-01T00:00:00.000Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_002() throws Exception {
        // Combination: date=new java.util.Date(0L), millis=false
        Object actual = ISO8601Utils.format(new java.util.Date(0L), false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1970-01-01T00:00:00Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_003() throws Exception {
        // Combination: date=new java.util.Date(1000000000000L), millis=true
        Object actual = ISO8601Utils.format(new java.util.Date(1000000000000L), true);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("2001-09-09T01:46:40.000Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_004() throws Exception {
        // Combination: date=new java.util.Date(1000000000000L), millis=false
        Object actual = ISO8601Utils.format(new java.util.Date(1000000000000L), false);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("2001-09-09T01:46:40Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_005() throws Exception {
        // Combination: date=new java.util.Date(0L), millis=true, tz=java.util.TimeZone.getTimeZone("UTC")
        Object actual = ISO8601Utils.format(new java.util.Date(0L), true, java.util.TimeZone.getTimeZone("UTC"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1970-01-01T00:00:00.000Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_006() throws Exception {
        // Combination: date=new java.util.Date(0L), millis=false, tz=java.util.TimeZone.getTimeZone("GMT")
        Object actual = ISO8601Utils.format(new java.util.Date(0L), false, java.util.TimeZone.getTimeZone("GMT"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1970-01-01T00:00:00Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_007() throws Exception {
        // Combination: date=new java.util.Date(1000000000000L), millis=true, tz=java.util.TimeZone.getTimeZone("GMT")
        Object actual = ISO8601Utils.format(new java.util.Date(1000000000000L), true, java.util.TimeZone.getTimeZone("GMT"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("2001-09-09T01:46:40.000Z", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_008() throws Exception {
        // Combination: date=new java.util.Date(1000000000000L), millis=false, tz=java.util.TimeZone.getTimeZone("UTC")
        Object actual = ISO8601Utils.format(new java.util.Date(1000000000000L), false, java.util.TimeZone.getTimeZone("UTC"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("2001-09-09T01:46:40Z", String.valueOf(actual));
    }

}
