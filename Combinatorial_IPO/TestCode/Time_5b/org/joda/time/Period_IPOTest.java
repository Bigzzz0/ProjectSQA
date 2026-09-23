package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Period.
 */
public class Period_IPOTest {
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
    public void test_fieldDifference_pairwise_001() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2000, 1, 1), end=new org.joda.time.LocalDate(2000, 1, 1)
        Object actual = Period.fieldDifference(new org.joda.time.LocalDate(2000, 1, 1), new org.joda.time.LocalDate(2000, 1, 1));
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P0D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_fieldDifference_pairwise_002() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2020, 6, 15), end=new org.joda.time.LocalDate(2000, 1, 1)
        Object actual = Period.fieldDifference(new org.joda.time.LocalDate(2020, 6, 15), new org.joda.time.LocalDate(2000, 1, 1));
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P-20Y-5M-14D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_fieldDifference_pairwise_003() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2000, 1, 1), end=new org.joda.time.LocalDate(2020, 6, 15)
        Object actual = Period.fieldDifference(new org.joda.time.LocalDate(2000, 1, 1), new org.joda.time.LocalDate(2020, 6, 15));
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P20Y5M14D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_fieldDifference_pairwise_004() throws Exception {
        // Combination: start=new org.joda.time.LocalDate(2020, 6, 15), end=new org.joda.time.LocalDate(2020, 6, 15)
        Object actual = Period.fieldDifference(new org.joda.time.LocalDate(2020, 6, 15), new org.joda.time.LocalDate(2020, 6, 15));
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P0D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_005() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=0
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.days(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT0S", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_006() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=1
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.days(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P1D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_007() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=-1
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.days(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P-1D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_008() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=Integer.MAX_VALUE
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.days(), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P2147483647D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_009() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=Integer.MIN_VALUE
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.days(), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P-2147483648D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_010() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=0
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.hours(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT0S", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_011() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=1
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.hours(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT1H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_012() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=-1
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.hours(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT-1H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_013() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=Integer.MAX_VALUE
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.hours(), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT2147483647H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_014() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=Integer.MIN_VALUE
        Object actual = (new Period()).withField(org.joda.time.DurationFieldType.hours(), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT-2147483648H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_015() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=0
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.days(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT0S", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_016() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=1
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.days(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P1D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_017() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=-1
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.days(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P-1D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_018() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=Integer.MAX_VALUE
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.days(), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P2147483647D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_019() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.days(), value=Integer.MIN_VALUE
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.days(), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("P-2147483648D", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_020() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=0
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.hours(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT0S", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_021() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=1
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.hours(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT1H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_022() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=-1
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.hours(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT-1H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_023() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=Integer.MAX_VALUE
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.hours(), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT2147483647H", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_024() throws Exception {
        // Combination: field=org.joda.time.DurationFieldType.hours(), value=Integer.MIN_VALUE
        Object actual = (new Period()).withFieldAdded(org.joda.time.DurationFieldType.hours(), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.Period", actual.getClass().getName());
        assertEquals("PT-2147483648H", formatValue(actual));
    }

}
