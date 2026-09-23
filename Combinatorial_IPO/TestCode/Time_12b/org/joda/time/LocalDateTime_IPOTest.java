package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for LocalDateTime.
 */
public class LocalDateTime_IPOTest {
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
    public void test_withTime_pairwise_001() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=0, secondOfMinute=0, millisOfSecond=0
        Object actual = (new LocalDateTime()).withTime(0, 0, 0, 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDateTime", actual.getClass().getName());
        assertEquals("2026-09-23T00:00:00.000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_002() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=1, secondOfMinute=1, millisOfSecond=1
        Object actual = (new LocalDateTime()).withTime(0, 1, 1, 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.LocalDateTime", actual.getClass().getName());
        assertEquals("2026-09-23T00:01:01.001", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_003() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=-1, secondOfMinute=-1, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(0, -1, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_004() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_005() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_006() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=1, secondOfMinute=-1, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(1, 1, -1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_007() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=0, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=1
        try {
            (new LocalDateTime()).withTime(1, 0, Integer.MAX_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_008() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=0, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(1, Integer.MAX_VALUE, 0, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_009() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=-1, secondOfMinute=1, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(1, -1, 1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_010() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=0, secondOfMinute=1, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(1, 0, 1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_011() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=-1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(-1, -1, Integer.MAX_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_012() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=-1, millisOfSecond=1
        try {
            (new LocalDateTime()).withTime(-1, Integer.MAX_VALUE, -1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_013() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=0, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(-1, 0, Integer.MIN_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_014() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=1, secondOfMinute=0, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(-1, 1, 0, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_015() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(-1, 1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_016() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=1, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_017() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=-1, secondOfMinute=0, millisOfSecond=1
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, -1, 0, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_018() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_019() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, 0, -1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_020() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=-1, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, -1, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_021() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=0, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_022() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=0, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=1
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_023() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=1, secondOfMinute=1, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, 1, 1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_024() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=-1, secondOfMinute=-1, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, -1, -1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_025() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=0, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_026() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=1, millisOfSecond=1
        try {
            (new LocalDateTime()).withTime(1, Integer.MIN_VALUE, 1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_027() throws Exception {
        // Combination: hourOfDay=-1, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=1, millisOfSecond=-1
        try {
            (new LocalDateTime()).withTime(-1, Integer.MIN_VALUE, 1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_028() throws Exception {
        // Combination: hourOfDay=Integer.MAX_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=-1, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(Integer.MAX_VALUE, Integer.MIN_VALUE, -1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_029() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=Integer.MIN_VALUE
        try {
            (new LocalDateTime()).withTime(0, 0, -1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_030() throws Exception {
        // Combination: hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_031() throws Exception {
        // Combination: hourOfDay=1, minuteOfHour=1, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=0
        try {
            (new LocalDateTime()).withTime(1, 1, Integer.MIN_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_withTime_pairwise_032() throws Exception {
        // Combination: hourOfDay=0, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (new LocalDateTime()).withTime(0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_033() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.ROOT
        try {
            (new LocalDateTime()).toString("", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_034() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString(" ", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_035() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("a", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("PM", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_036() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.ROOT
        try {
            (new LocalDateTime()).toString("test123", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_037() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("!@#", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_038() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("0", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_039() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("-1", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_040() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("1.5", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_041() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("9223372036854775807", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_042() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("9223372036854775808", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_043() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.ROOT
        Object actual = (new LocalDateTime()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("PM", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_044() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.US
        try {
            (new LocalDateTime()).toString("", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_045() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString(" ", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_046() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("a", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("PM", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_047() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.US
        try {
            (new LocalDateTime()).toString("test123", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_048() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("!@#", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_049() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("0", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_050() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("-1", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_051() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("1.5", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_052() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("9223372036854775807", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_053() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("9223372036854775808", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_054() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.US
        Object actual = (new LocalDateTime()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("PM", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_055() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.JAPAN
        try {
            (new LocalDateTime()).toString("", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_056() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString(" ", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_057() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("a", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\u5348\u5f8c", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_058() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.JAPAN
        try {
            (new LocalDateTime()).toString("test123", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_059() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("!@#", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_060() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("0", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_061() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("-1", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_062() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("1.5", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_063() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("9223372036854775807", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_064() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("9223372036854775808", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_065() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDateTime()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\u5348\u5f8c", formatValue(actual));
    }

}
