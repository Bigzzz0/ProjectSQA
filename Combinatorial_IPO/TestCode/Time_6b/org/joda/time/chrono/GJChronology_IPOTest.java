package org.joda.time.chrono;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for GJChronology.
 */
public class GJChronology_IPOTest {
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
    public void test_getDateTimeMillis_pairwise_001() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=0, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_002() throws Exception {
        // Combination: year=1, monthOfYear=1, dayOfMonth=0, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 1, 0, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_003() throws Exception {
        // Combination: year=-1, monthOfYear=-1, dayOfMonth=0, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, -1, 0, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_004() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=Integer.MAX_VALUE, dayOfMonth=0, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_005() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=Integer.MIN_VALUE, dayOfMonth=0, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_006() throws Exception {
        // Combination: year=-1, monthOfYear=1, dayOfMonth=1, millisOfDay=0
        Object actual = (GJChronology.getInstance()).getDateTimeMillis(-1, 1, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-62167392000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_007() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=0, dayOfMonth=1, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 0, 1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_008() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MAX_VALUE, dayOfMonth=1, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MAX_VALUE, 1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_009() throws Exception {
        // Combination: year=1, monthOfYear=-1, dayOfMonth=1, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, -1, 1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_010() throws Exception {
        // Combination: year=1, monthOfYear=0, dayOfMonth=1, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 0, 1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_011() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=-1, dayOfMonth=-1, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, -1, -1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_012() throws Exception {
        // Combination: year=-1, monthOfYear=Integer.MAX_VALUE, dayOfMonth=-1, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, Integer.MAX_VALUE, -1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_013() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=0, dayOfMonth=-1, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 0, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_014() throws Exception {
        // Combination: year=0, monthOfYear=1, dayOfMonth=-1, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 1, -1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_015() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=1, dayOfMonth=-1, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 1, -1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_016() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MAX_VALUE, dayOfMonth=Integer.MAX_VALUE, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MAX_VALUE, Integer.MAX_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_017() throws Exception {
        // Combination: year=0, monthOfYear=-1, dayOfMonth=Integer.MAX_VALUE, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, -1, Integer.MAX_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_018() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=1, dayOfMonth=Integer.MAX_VALUE, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_019() throws Exception {
        // Combination: year=-1, monthOfYear=0, dayOfMonth=Integer.MAX_VALUE, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_020() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=-1, dayOfMonth=Integer.MAX_VALUE, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, -1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_021() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MIN_VALUE, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_022() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=0, dayOfMonth=Integer.MIN_VALUE, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_023() throws Exception {
        // Combination: year=1, monthOfYear=1, dayOfMonth=Integer.MIN_VALUE, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 1, Integer.MIN_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_024() throws Exception {
        // Combination: year=-1, monthOfYear=-1, dayOfMonth=Integer.MIN_VALUE, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_025() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MAX_VALUE, dayOfMonth=Integer.MIN_VALUE, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_026() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MIN_VALUE, dayOfMonth=1, millisOfDay=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MIN_VALUE, 1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_027() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MIN_VALUE, dayOfMonth=-1, millisOfDay=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MIN_VALUE, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_028() throws Exception {
        // Combination: year=-1, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MAX_VALUE, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_029() throws Exception {
        // Combination: year=-1, monthOfYear=0, dayOfMonth=0, millisOfDay=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 0, 0, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_030() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MIN_VALUE, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_031() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=1, dayOfMonth=1, millisOfDay=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 1, 1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_032() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=Integer.MAX_VALUE, dayOfMonth=0, millisOfDay=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_033() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, 0, 0, 0, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_034() throws Exception {
        // Combination: year=1, monthOfYear=1, dayOfMonth=0, hourOfDay=1, minuteOfHour=1, secondOfMinute=1, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 1, 0, 1, 1, 1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_035() throws Exception {
        // Combination: year=-1, monthOfYear=-1, dayOfMonth=0, hourOfDay=-1, minuteOfHour=-1, secondOfMinute=-1, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, -1, 0, -1, -1, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_036() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=Integer.MAX_VALUE, dayOfMonth=0, hourOfDay=Integer.MAX_VALUE, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_037() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=Integer.MIN_VALUE, dayOfMonth=0, hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_038() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MAX_VALUE, dayOfMonth=1, hourOfDay=0, minuteOfHour=-1, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MAX_VALUE, 1, 0, -1, Integer.MIN_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_039() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=-1, dayOfMonth=1, hourOfDay=1, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, -1, 1, 1, Integer.MAX_VALUE, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_040() throws Exception {
        // Combination: year=-1, monthOfYear=1, dayOfMonth=1, hourOfDay=-1, minuteOfHour=0, secondOfMinute=1, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 1, 1, -1, 0, 1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_041() throws Exception {
        // Combination: year=1, monthOfYear=0, dayOfMonth=1, hourOfDay=Integer.MAX_VALUE, minuteOfHour=1, secondOfMinute=-1, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 0, 1, Integer.MAX_VALUE, 1, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_042() throws Exception {
        // Combination: year=-1, monthOfYear=Integer.MAX_VALUE, dayOfMonth=1, hourOfDay=Integer.MIN_VALUE, minuteOfHour=1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, Integer.MAX_VALUE, 1, Integer.MIN_VALUE, 1, Integer.MAX_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_043() throws Exception {
        // Combination: year=0, monthOfYear=1, dayOfMonth=-1, hourOfDay=0, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=1, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 1, -1, 0, Integer.MAX_VALUE, 1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_044() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=0, dayOfMonth=-1, hourOfDay=1, minuteOfHour=-1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 0, -1, 1, -1, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_045() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MAX_VALUE, dayOfMonth=-1, hourOfDay=-1, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MAX_VALUE, -1, -1, Integer.MIN_VALUE, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_046() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=-1, dayOfMonth=-1, hourOfDay=Integer.MAX_VALUE, minuteOfHour=0, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, -1, -1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_047() throws Exception {
        // Combination: year=-1, monthOfYear=0, dayOfMonth=-1, hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=-1, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 0, -1, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_048() throws Exception {
        // Combination: year=0, monthOfYear=-1, dayOfMonth=Integer.MAX_VALUE, hourOfDay=0, minuteOfHour=1, secondOfMinute=0, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, -1, Integer.MAX_VALUE, 0, 1, 0, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_049() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=Integer.MAX_VALUE, dayOfMonth=Integer.MAX_VALUE, hourOfDay=1, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 0, -1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_050() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MAX_VALUE, hourOfDay=-1, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_051() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=1, dayOfMonth=Integer.MAX_VALUE, hourOfDay=Integer.MAX_VALUE, minuteOfHour=-1, secondOfMinute=1, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, -1, 1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_052() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=0, dayOfMonth=Integer.MAX_VALUE, hourOfDay=Integer.MIN_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=1, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 1, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_053() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MIN_VALUE, hourOfDay=0, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, -1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_054() throws Exception {
        // Combination: year=-1, monthOfYear=1, dayOfMonth=Integer.MIN_VALUE, hourOfDay=1, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 1, Integer.MIN_VALUE, 1, Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_055() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=0, dayOfMonth=Integer.MIN_VALUE, hourOfDay=-1, minuteOfHour=1, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 0, Integer.MIN_VALUE, -1, 1, Integer.MIN_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_056() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MIN_VALUE, dayOfMonth=Integer.MIN_VALUE, hourOfDay=Integer.MAX_VALUE, minuteOfHour=-1, secondOfMinute=0, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 0, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_057() throws Exception {
        // Combination: year=1, monthOfYear=-1, dayOfMonth=Integer.MIN_VALUE, hourOfDay=Integer.MIN_VALUE, minuteOfHour=0, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, -1, Integer.MIN_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_058() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=Integer.MIN_VALUE, dayOfMonth=1, hourOfDay=1, minuteOfHour=1, secondOfMinute=1, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, Integer.MIN_VALUE, 1, 1, 1, 1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_059() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=-1, hourOfDay=-1, minuteOfHour=1, secondOfMinute=0, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, -1, -1, 1, 0, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_060() throws Exception {
        // Combination: year=-1, monthOfYear=1, dayOfMonth=Integer.MAX_VALUE, hourOfDay=Integer.MAX_VALUE, minuteOfHour=-1, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, -1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_061() throws Exception {
        // Combination: year=0, monthOfYear=1, dayOfMonth=0, hourOfDay=Integer.MIN_VALUE, minuteOfHour=-1, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 1, 0, Integer.MIN_VALUE, -1, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_062() throws Exception {
        // Combination: year=1, monthOfYear=Integer.MAX_VALUE, dayOfMonth=Integer.MIN_VALUE, hourOfDay=0, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=1, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, Integer.MAX_VALUE, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, 1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_063() throws Exception {
        // Combination: year=0, monthOfYear=-1, dayOfMonth=1, hourOfDay=0, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, -1, 1, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_064() throws Exception {
        // Combination: year=0, monthOfYear=Integer.MIN_VALUE, dayOfMonth=0, hourOfDay=Integer.MAX_VALUE, minuteOfHour=Integer.MIN_VALUE, secondOfMinute=-1, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, Integer.MIN_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_065() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=-1, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=1, millisOfSecond=Integer.MIN_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, -1, 0, 0, 0, 1, Integer.MIN_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_066() throws Exception {
        // Combination: year=-1, monthOfYear=Integer.MIN_VALUE, dayOfMonth=-1, hourOfDay=0, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, Integer.MIN_VALUE, -1, 0, 0, -1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_067() throws Exception {
        // Combination: year=-1, monthOfYear=0, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=0, millisOfSecond=1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(-1, 0, 0, 0, 0, 0, 1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_068() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=1, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=-1, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 1, 0, 0, 0, -1, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_069() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=Integer.MAX_VALUE, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, 0, 0, 0, Integer.MAX_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_070() throws Exception {
        // Combination: year=1, monthOfYear=0, dayOfMonth=Integer.MAX_VALUE, hourOfDay=0, minuteOfHour=Integer.MAX_VALUE, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=-1
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, -1);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_071() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=0, hourOfDay=0, minuteOfHour=0, secondOfMinute=Integer.MIN_VALUE, millisOfSecond=Integer.MAX_VALUE
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, 0, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_072() throws Exception {
        // Combination: year=0, monthOfYear=0, dayOfMonth=0, hourOfDay=1, minuteOfHour=0, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(0, 0, 0, 1, 0, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_073() throws Exception {
        // Combination: year=1, monthOfYear=0, dayOfMonth=0, hourOfDay=0, minuteOfHour=-1, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(1, 0, 0, 0, -1, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_074() throws Exception {
        // Combination: year=Integer.MAX_VALUE, monthOfYear=0, dayOfMonth=0, hourOfDay=-1, minuteOfHour=0, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MAX_VALUE, 0, 0, -1, 0, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDateTimeMillis_pairwise_075() throws Exception {
        // Combination: year=Integer.MIN_VALUE, monthOfYear=0, dayOfMonth=0, hourOfDay=Integer.MAX_VALUE, minuteOfHour=0, secondOfMinute=0, millisOfSecond=0
        try {
            (GJChronology.getInstance()).getDateTimeMillis(Integer.MIN_VALUE, 0, 0, Integer.MAX_VALUE, 0, 0, 0);
            fail("Expected org.joda.time.IllegalFieldValueException");
        } catch (org.joda.time.IllegalFieldValueException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
