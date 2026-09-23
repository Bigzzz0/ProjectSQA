package org.joda.time.format;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DateTimeParserBucket.
 */
public class DateTimeParserBucket_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_001() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_002() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_003() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_004() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_005() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_006() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_007() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_008() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_009() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_010() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_011() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_012() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_013() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_014() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getZone_pairwise_015() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getZone());
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_016() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_017() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_018() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_019() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_020() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_021() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_022() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_023() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_024() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_025() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_026() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_027() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_028() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_029() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOffset_pairwise_030() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getOffset();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_031() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_032() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_033() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_034() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_035() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_036() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_037() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_038() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_039() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_040() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_041() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_042() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        assertNull((new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_043() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_044() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_getPivotYear_pairwise_045() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        assertNull((new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).getPivotYear());
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_046() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, savedState=new Object()
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).restoreState(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_047() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, savedState="sample_str"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).restoreState("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_048() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, savedState=Integer.valueOf(1)
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).restoreState(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_049() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, savedState="sample_str"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).restoreState("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_050() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, savedState=Integer.valueOf(1)
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).restoreState(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_051() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, savedState=Integer.valueOf(1)
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).restoreState(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_052() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, savedState=new Object()
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).restoreState(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_053() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, savedState="sample_str"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).restoreState("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_054() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, savedState=new Object()
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).restoreState(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_055() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, savedState=new Object()
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).restoreState(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_056() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, savedState=new Object()
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).restoreState(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_057() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, savedState=Integer.valueOf(1)
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).restoreState(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_058() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, savedState="sample_str"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).restoreState("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_059() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, savedState=Integer.valueOf(1)
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).restoreState(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_restoreState_pairwise_060() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, savedState="sample_str"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).restoreState("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_061() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_062() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_063() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_064() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_065() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_066() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_067() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_068() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_069() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_070() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_071() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_072() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_073() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_074() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_075() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis();
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_076() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_077() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_078() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_079() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_080() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_081() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_082() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_083() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_084() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_085() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_086() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_087() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_088() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=false
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(false);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_089() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_090() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_091() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=""
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_092() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false, text=" "
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false, " ");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_093() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="a"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "a");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_094() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="test123"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_095() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="!@#"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_096() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="test123"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_097() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="0"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_098() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="-1"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_099() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text=" "
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, " ");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_100() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false, text=""
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false, "");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_101() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="1.5"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_102() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="-1"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_103() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=false, text="0"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(false, "0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_104() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="9223372036854775807"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_105() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="9223372036854775808"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_106() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text=""
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_107() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=""
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_108() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=""
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_109() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=" "
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, " ");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_110() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=" "
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, " ");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_111() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text=" "
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, " ");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_112() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="a"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "a");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_113() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="a"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "a");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_114() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="a"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "a");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_115() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="a"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "a");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_116() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="test123"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_117() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="test123"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_118() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="test123"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_119() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false, text="!@#"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_120() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="!@#"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_121() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="!@#"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_122() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="!@#"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_123() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="0"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_124() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="0"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_125() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="0"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_126() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="-1"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_127() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="-1"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_128() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="-1"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_129() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="1.5"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_130() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="1.5"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_131() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="1.5"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_132() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="1.5"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_133() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="9223372036854775807"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_134() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="9223372036854775807"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_135() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="9223372036854775807"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_136() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="9223372036854775807"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_137() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=false, text="9223372036854775808"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(false, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_138() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=true, text="9223372036854775808"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(true, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_139() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="9223372036854775808"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_140() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="9223372036854775808"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_141() throws Exception {
        // Combination: receiver__instantLocal=0L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DateTimeParserBucket(0L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_142() throws Exception {
        // Combination: receiver__instantLocal=1L, receiver__chrono=org.joda.time.chrono.GJChronology.getInstanceUTC(), receiver__locale=java.util.Locale.US, resetFields=false, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DateTimeParserBucket(1L, org.joda.time.chrono.GJChronology.getInstanceUTC(), java.util.Locale.US)).computeMillis(false, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_143() throws Exception {
        // Combination: receiver__instantLocal=-1L, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.JAPAN, resetFields=true, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DateTimeParserBucket(-1L, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.JAPAN)).computeMillis(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_144() throws Exception {
        // Combination: receiver__instantLocal=Long.MAX_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DateTimeParserBucket(Long.MAX_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_computeMillis_pairwise_145() throws Exception {
        // Combination: receiver__instantLocal=Long.MIN_VALUE, receiver__chrono=org.joda.time.chrono.ISOChronology.getInstanceUTC(), receiver__locale=java.util.Locale.ROOT, resetFields=true, text="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new DateTimeParserBucket(Long.MIN_VALUE, org.joda.time.chrono.ISOChronology.getInstanceUTC(), java.util.Locale.ROOT)).computeMillis(true, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

}
