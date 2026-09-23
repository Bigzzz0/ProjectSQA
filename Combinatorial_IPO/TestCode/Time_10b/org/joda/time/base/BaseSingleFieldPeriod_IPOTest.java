package org.joda.time.base;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for BaseSingleFieldPeriod.
 */
public class BaseSingleFieldPeriod_IPOTest {
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
    public void test_between_pairwise_001() throws Exception {
        // Combination: start=new org.joda.time.Instant(0L), end=new org.joda.time.Instant(0L), field=org.joda.time.DurationFieldType.days()
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.Instant(0L), new org.joda.time.Instant(0L), org.joda.time.DurationFieldType.days());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_002() throws Exception {
        // Combination: start=new org.joda.time.Instant(1000000000000L), end=new org.joda.time.Instant(0L), field=org.joda.time.DurationFieldType.hours()
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.Instant(1000000000000L), new org.joda.time.Instant(0L), org.joda.time.DurationFieldType.hours());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-277777", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_003() throws Exception {
        // Combination: start=new org.joda.time.Instant(1000000000000L), end=new org.joda.time.Instant(1000000000000L), field=org.joda.time.DurationFieldType.days()
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.Instant(1000000000000L), new org.joda.time.Instant(1000000000000L), org.joda.time.DurationFieldType.days());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_004() throws Exception {
        // Combination: start=new org.joda.time.Instant(0L), end=new org.joda.time.Instant(1000000000000L), field=org.joda.time.DurationFieldType.hours()
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.Instant(0L), new org.joda.time.Instant(1000000000000L), org.joda.time.DurationFieldType.hours());
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("277777", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_005() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2000, 1, 1), end=new org.joda.time.LocalDate(2000, 1, 1), zeroInstance=org.joda.time.Period.ZERO
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.LocalDate(2000, 1, 1), new org.joda.time.LocalDate(2000, 1, 1), org.joda.time.Period.ZERO);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_006() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2020, 6, 15), end=new org.joda.time.LocalDate(2000, 1, 1), zeroInstance=org.joda.time.Period.days(1)
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.LocalDate(2020, 6, 15), new org.joda.time.LocalDate(2000, 1, 1), org.joda.time.Period.days(1));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-20", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_007() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2000, 1, 1), end=new org.joda.time.LocalDate(2020, 6, 15), zeroInstance=org.joda.time.Period.days(1)
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.LocalDate(2000, 1, 1), new org.joda.time.LocalDate(2020, 6, 15), org.joda.time.Period.days(1));
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("20", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_between_pairwise_008() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2020, 6, 15), end=new org.joda.time.LocalDate(2020, 6, 15), zeroInstance=org.joda.time.Period.ZERO
        Object actual = BaseSingleFieldPeriod.between(new org.joda.time.LocalDate(2020, 6, 15), new org.joda.time.LocalDate(2020, 6, 15), org.joda.time.Period.ZERO);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_009() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, millisPerUnit=0L
        try {
            BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.ZERO, 0L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_010() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), millisPerUnit=0L
        try {
            BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.days(1), 0L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_011() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, millisPerUnit=1L
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.ZERO, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_012() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), millisPerUnit=1L
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.days(1), 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("86400000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_013() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, millisPerUnit=-1L
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.ZERO, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_014() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), millisPerUnit=-1L
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.days(1), -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-86400000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_015() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, millisPerUnit=Long.MAX_VALUE
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.ZERO, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_016() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), millisPerUnit=Long.MAX_VALUE
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.days(1), Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_017() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, millisPerUnit=Long.MIN_VALUE
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.ZERO, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_standardPeriodIn_pairwise_018() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), millisPerUnit=Long.MIN_VALUE
        Object actual = BaseSingleFieldPeriod.standardPeriodIn(org.joda.time.Period.days(1), Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

}
