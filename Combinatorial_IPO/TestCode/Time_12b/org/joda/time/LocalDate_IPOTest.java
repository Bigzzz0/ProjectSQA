package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for LocalDate.
 */
public class LocalDate_IPOTest {
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
    public void test_withField_pairwise_001() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.year(), value=0
        Object actual = (new LocalDate()).withField(org.joda.time.DateTimeFieldType.year(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("0000-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_002() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.year(), value=1
        Object actual = (new LocalDate()).withField(org.joda.time.DateTimeFieldType.year(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("0001-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_003() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.year(), value=-1
        Object actual = (new LocalDate()).withField(org.joda.time.DateTimeFieldType.year(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("-0001-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_004() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.year(), value=Integer.MAX_VALUE
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.year(), Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_005() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.year(), value=Integer.MIN_VALUE
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.year(), Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_006() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.monthOfYear(), value=0
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.monthOfYear(), 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_007() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.monthOfYear(), value=1
        Object actual = (new LocalDate()).withField(org.joda.time.DateTimeFieldType.monthOfYear(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-01-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_008() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.monthOfYear(), value=-1
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.monthOfYear(), -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_009() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.monthOfYear(), value=Integer.MAX_VALUE
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.monthOfYear(), Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withField_pairwise_010() throws Exception {
        // Combination: fieldType=org.joda.time.DateTimeFieldType.monthOfYear(), value=Integer.MIN_VALUE
        try {
            (new LocalDate()).withField(org.joda.time.DateTimeFieldType.monthOfYear(), Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_011() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.days(), amount=0
        Object actual = (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.days(), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_012() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.hours(), amount=0
        try {
            (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.hours(), 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_013() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.days(), amount=1
        Object actual = (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.days(), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-24", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_014() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.hours(), amount=1
        try {
            (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.hours(), 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_015() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.days(), amount=-1
        Object actual = (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.days(), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-22", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_016() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.hours(), amount=-1
        try {
            (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.hours(), -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_017() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.days(), amount=Integer.MAX_VALUE
        Object actual = (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.days(), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("5881637-04-02", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_018() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.hours(), amount=Integer.MAX_VALUE
        try {
            (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.hours(), Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_019() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.days(), amount=Integer.MIN_VALUE
        Object actual = (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.days(), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("-5877584-03-14", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withFieldAdded_pairwise_020() throws Exception {
        // Combination: fieldType=org.joda.time.DurationFieldType.hours(), amount=Integer.MIN_VALUE
        try {
            (new LocalDate()).withFieldAdded(org.joda.time.DurationFieldType.hours(), Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_021() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, scalar=0
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.ZERO, 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_022() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, scalar=1
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.ZERO, 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_023() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, scalar=-1
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.ZERO, -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_024() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, scalar=Integer.MAX_VALUE
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.ZERO, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_025() throws Exception {
        // Combination: period=org.joda.time.Period.ZERO, scalar=Integer.MIN_VALUE
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.ZERO, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_026() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), scalar=0
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.days(1), 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-23", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_027() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), scalar=1
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.days(1), 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-24", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_028() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), scalar=-1
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.days(1), -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("2026-09-22", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_029() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), scalar=Integer.MAX_VALUE
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.days(1), Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("5881637-04-02", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withPeriodAdded_pairwise_030() throws Exception {
        // Combination: period=org.joda.time.Period.days(1), scalar=Integer.MIN_VALUE
        Object actual = (new LocalDate()).withPeriodAdded(org.joda.time.Period.days(1), Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDate", actual.getClass().getName());
        assertEquals("-5877584-03-14", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_031() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.ROOT
        try {
            (new LocalDate()).toString("", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_032() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_033() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("a", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_034() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.ROOT
        try {
            (new LocalDate()).toString("test123", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_035() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_036() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("0", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_037() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_038() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_039() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_040() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_041() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_042() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.US
        try {
            (new LocalDate()).toString("", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_043() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_044() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("a", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_045() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.US
        try {
            (new LocalDate()).toString("test123", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_046() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_047() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("0", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_048() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_049() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_050() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_051() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_052() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_053() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.JAPAN
        try {
            (new LocalDate()).toString("", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_054() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_055() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("a", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_056() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.JAPAN
        try {
            (new LocalDate()).toString("test123", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_057() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_058() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("0", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_059() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_060() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_061() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_062() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_063() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", formatValue(actual));
    }

}
