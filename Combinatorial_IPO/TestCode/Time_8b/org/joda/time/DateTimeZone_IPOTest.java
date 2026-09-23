package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DateTimeZone.
 */
public class DateTimeZone_IPOTest {
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
    public void test_forOffsetHoursMinutes_pairwise_001() throws Exception {
        // Combination: hoursOffset=0, minutesOffset=0
        Object actual = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("UTC", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_002() throws Exception {
        // Combination: hoursOffset=0, minutesOffset=1
        Object actual = DateTimeZone.forOffsetHoursMinutes(0, 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("+00:01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_003() throws Exception {
        // Combination: hoursOffset=0, minutesOffset=-1
        Object actual = DateTimeZone.forOffsetHoursMinutes(0, -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("-00:01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_004() throws Exception {
        // Combination: hoursOffset=0, minutesOffset=Integer.MAX_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_005() throws Exception {
        // Combination: hoursOffset=0, minutesOffset=Integer.MIN_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_006() throws Exception {
        // Combination: hoursOffset=1, minutesOffset=0
        Object actual = DateTimeZone.forOffsetHoursMinutes(1, 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("+01:00", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_007() throws Exception {
        // Combination: hoursOffset=1, minutesOffset=1
        Object actual = DateTimeZone.forOffsetHoursMinutes(1, 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("+01:01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_008() throws Exception {
        // Combination: hoursOffset=1, minutesOffset=-1
        try {
            DateTimeZone.forOffsetHoursMinutes(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_009() throws Exception {
        // Combination: hoursOffset=1, minutesOffset=Integer.MAX_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_010() throws Exception {
        // Combination: hoursOffset=1, minutesOffset=Integer.MIN_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_011() throws Exception {
        // Combination: hoursOffset=-1, minutesOffset=0
        Object actual = DateTimeZone.forOffsetHoursMinutes(-1, 0);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("-01:00", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_012() throws Exception {
        // Combination: hoursOffset=-1, minutesOffset=1
        Object actual = DateTimeZone.forOffsetHoursMinutes(-1, 1);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("-01:01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_013() throws Exception {
        // Combination: hoursOffset=-1, minutesOffset=-1
        Object actual = DateTimeZone.forOffsetHoursMinutes(-1, -1);
        assertNotNull(actual);
        assertEquals("org.joda.time.tz.FixedDateTimeZone", actual.getClass().getName());
        assertEquals("-01:01", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_014() throws Exception {
        // Combination: hoursOffset=-1, minutesOffset=Integer.MAX_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_015() throws Exception {
        // Combination: hoursOffset=-1, minutesOffset=Integer.MIN_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_016() throws Exception {
        // Combination: hoursOffset=Integer.MAX_VALUE, minutesOffset=0
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_017() throws Exception {
        // Combination: hoursOffset=Integer.MAX_VALUE, minutesOffset=1
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_018() throws Exception {
        // Combination: hoursOffset=Integer.MAX_VALUE, minutesOffset=-1
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_019() throws Exception {
        // Combination: hoursOffset=Integer.MAX_VALUE, minutesOffset=Integer.MAX_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_020() throws Exception {
        // Combination: hoursOffset=Integer.MAX_VALUE, minutesOffset=Integer.MIN_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_021() throws Exception {
        // Combination: hoursOffset=Integer.MIN_VALUE, minutesOffset=0
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_022() throws Exception {
        // Combination: hoursOffset=Integer.MIN_VALUE, minutesOffset=1
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_023() throws Exception {
        // Combination: hoursOffset=Integer.MIN_VALUE, minutesOffset=-1
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_024() throws Exception {
        // Combination: hoursOffset=Integer.MIN_VALUE, minutesOffset=Integer.MAX_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_forOffsetHoursMinutes_pairwise_025() throws Exception {
        // Combination: hoursOffset=Integer.MIN_VALUE, minutesOffset=Integer.MIN_VALUE
        try {
            DateTimeZone.forOffsetHoursMinutes(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
