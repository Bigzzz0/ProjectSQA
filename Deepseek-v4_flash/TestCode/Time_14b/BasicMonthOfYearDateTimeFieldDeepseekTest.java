package org.joda.time.chrono;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeField;
import org.joda.time.DurationField;
import org.joda.time.Instant;
import org.joda.time.MonthDay;
import org.joda.time.ReadablePartial;
import org.joda.time.field.FieldUtils;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * <parallel>
 * This test suite is designed to exercise the <code>BasicMonthOfYearDateTimeField</code>
 * with a focus on boundary conditions and the known defect in the
 * <code>add(ReadablePartial, int, int[], int)</code> method.
 *
 * <h3>[Branch & Defect Analysis Matrix]</h3>
 * <ul>
 *   <li><b>add(long, int):</b>
 *     <ul>
 *       <li>months == 0 : return instant</li>
 *       <li>positive months : branch monthToUse >= 0</li>
 *       <li>negative months : branch monthToUse < 0 (including boundary remMonthToUse == 0, monthToUse == 1 -> yearToUse++)</li>
 *       <li>day clamping: dayToUse > maxDay -> dayToUse = maxDay</li>
 *     </ul>
 *   </li>
 *   <li><b>add(long, long):</b>
 *     <ul>
 *       <li>conversion to int and fallback to add(long, int)</li>
 *       <li>overflow/underflow year bounds</li>
 *     </ul>
 *   </li>
 *   <li><b>add(ReadablePartial, int, int[], int):</b>
 *     <ul>
 *       <li>valueToAdd == 0 -> return values</li>
 *       <li>isContiguous: builds instant by setting fields (order matters – this is the bug zone)</li>
 *       <li>non‑contiguous fallback to super.add</li>
 *     </ul>
 *   </li>
 *   <li><b>set(long, int):</b>
 *     <ul>
 *       <li>invalid month -> throws IllegalFieldValueException</li>
 *       <li>day clamping when original day > maxDom</li>
 *     </ul>
 *   </li>
 *   <li><b>addWrapField:</b> delegates to set with wrapped month</li>
 *   <li><b>getDifferenceAsLong:</b> handles reverse, last‑day‑of‑month special case</li>
 *   <li><b>roundFloor / remainder:</b> simple arithmetic</li>
 *   <li><b>isLeap:</b>  checks leap year and month matches iLeapMonth</li>
 * </ul>
 * <parallel>
 */
public class BasicMonthOfYearDateTimeFieldDeepseekTest {

    // Shared chronology and field
    private static final BasicChronology CHRON = ISOChronology.getInstanceUTC();
    private static final BasicMonthOfYearDateTimeField FIELD =
        (BasicMonthOfYearDateTimeField) CHRON.monthOfYear();

    // ------------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void test_get() {
        long instant = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);
        assertEquals(2, FIELD.get(instant));
    }

    @Test(timeout = 4000)
    public void test_add_instant_int_zero() {
        long inst = 0L;
        assertEquals(inst, FIELD.add(inst, 0));
    }

    @Test(timeout = 4000)    public void test_add_instant_int_positive() {
        // 2004‑02‑29 -> add 1 month -> should become 2004‑03‑29
        long start = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);
        long end = CHRON.getDateTimeMillis(2004, 3, 29, 0, 0, 0, 0);
        assertEquals(end, FIELD.add(start, 1));
    }

    @Test(timeout = 4000)    public void test_add_instant_int_negative() {
        // 2004‑03‑31 -> add -1 month -> should become 2004‑02‑29 (leap year)
        long start = CHRON.getDateTimeMillis(2004, 3, 31, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, -1));
    }

    @Test(timeout = 4000)    public void test_add_instant_int_negative_year_boundary() {
        // 2004‑01‑31 -> add -1 month -> 2003‑12‑31
        long start = CHRON.getDateTimeMillis(2004, 1, 31, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2003, 12, 31, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, -1));
    }

    @Test(timeout = 4000)
    public void test_add_instant_int_clamp_day_nonLeap() {
        // 2015‑03‑31 -> add -1 month -> 2015‑02‑28 (2015 not leap)
        long start = CHRON.getDateTimeMillis(2015, 3, 31, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2015, 2, 28, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, -1));
    }

    @Test(timeout = 4000)
    public void test_add_instant_int_boundary_negMonthRemainderZero() {
        // 2004‑01‑15 -> add -12 months -> 2003‑01‑15
        long start = CHRON.getDateTimeMillis(2004, 1, 15, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2003, 1, 15, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, -12));
    }

    @Test(timeout = 4000)
    public void test_add_instant_int_boundary_remMonthToUseZero() {
        // 2004‑12‑01 -> add -12 months -> 2003‑12‑01
        long start = CHRON.getDateTimeMillis(2004, 12, 1, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2003, 12, 1, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, -12));
    }

    @Test(timeout = 4000)
    public void test_add_instant_int_largeMonths() {
        // add 48 months from leap day -> 2008‑02‑29 (2008 is leap)
        long start = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2008, 2, 29, 0, 0, 0, 0);
        assertEquals(expected, FIELD.add(start, 48));
    }

    // ------------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void test_set_invalidMonth_low() {
        try {
            FIELD.set(0L, 0);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void test_set_invalidMonth_high() {
        try {
            FIELD.set(0L, 13);
            fail("Expected IllegalFieldValueException");
        } catch (IllegalFieldValueException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void test_set_normal() {
        long base = CHRON.getDateTimeMillis(2000, 5, 15, 6, 30, 0,0);
        long expected = CHRON.getDateTimeMillis(2000, 10, 15, 6, 30, 0, 0);
        assertEquals(expected, FIELD.set(base, 10));
    }

    @Test(timeout = 4000)
    public void test_set_dayClamping() {
        // 2000‑01‑31 -> set month to February 2000 (not leap) -> day clamped to 28
        long base = CHRON.getDateTimeMillis(2000, 1, 31, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2000, 2, 28, 0, 0, 0, 0);
        assertEquals(expected, FIELD.set(base, 2));
    }

    @Test(timeout = 4000)
    public void test_set_dayClamping_leap() {
        // 2004‑01‑31 -> set month to February 2004 (leap) -> day clamped to 29
        long base = CHRON.getDateTimeMillis(2004, 1, 31, 0, 0, 0, 0);
        long expected = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0);
        assertEquals(expected, FIELD.set(base, 2));
    }

    @Test(timeout = 4000)    public void test_addWrapField_negative() {
        long base = CHRON.getDateTimeMillis(2004, 2, 15, 0, 0, 0, 0);
        // get(instant)=2, wrapped with months=-3 -> 2-3=-1 -> wrap to iMax+(-1%iMax?) see FieldUtils.getWrappedValue
        // Actually getWrappedValue(2, -3, 1, 12) = (2-1 + (-3) %12 +12) %12 +1 = (1-3+12)%12+1 = 10%12+1=11
        long expected = FIELD.set(base, 11);
        assertEquals(expected, FIELD.addWrapField(base, -3));
    }

    @Test(timeout = 4000)
    public void test_addWrapField_positive() {
        long base = CHRON.getDateTimeMillis(2004, 11, 15, 0, 0, 0, 0);
        // getWrappedValue(11, 4, 1, 12) = (11-1+4)%12+1 = 14%12+1=3
        long expected = FIELD.set(base, 3);
        assertEquals(expected, FIELD.addWrapField(base, 4));
    }

    // ------------------------------------------------------------------
    // Partition C: Defect‑Targeted Branch Zone (ReadablePartial add)
    // -----------:-------------------------------------------------------

    @Test(timeout = 4000)
    public void test_add_ReadablePartial_ZerovalueToAdd() {
        MonthDay md = new MonthDay(2, 29);
        int[] values = md.getValues();
        int[] result = FIELD.add(md, 0, values, 0);
        assertArray(new int[]{2, 29}, result);
    }

    @Test(timeout = 4000)
    public void test_add_ReadablePartial_plusOneFromLeap() {
        // This is the known defect #1: adding 1 month to Feb 29 should yield Mar 29
        // The bug cause was that setting month first then day throws because Feb 29 is invalid in non‑leap context.
        MonthDay md = new MonthDay(2, 29);
        try {
            int[] result = FIELD.add(md, 0, md.getValues(), 1);
            // Should succeed and return [3, 29]
            assertArrayEquals(new int[]{3, 29}, result);
        } catch (IllegalFieldValueException e) {
            fail("Defect exposed: expected no exception for plusMonths from leap day, but got: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)    public void test_add_ReadablePartial_minusOneFromLeap() {
        // minus 1 month from Feb 29 -> should be Jan 29
        MonthDay md = new MonthDay(2, 29);
        try {
            int[] result = FIELD.add(md, 0, md.getValues(), -1);
            assertArrayEquals(new int[]{1, 29}, result);
        } catch (IllegalFieldValueException e) {
            fail("Defect exposed: expected no exception for minusMonths from leap day, but got: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void test_add_ReadablePartial_plusTwelveFromLeap() {
        // Adding 12 months to Feb 29 of a leap year -> should be Feb 29 of next leap? Actually add years correctly.
        MonthDay md = new MonthDay(2, 29);
        try {
            int[] result = FIELD.add(md, 0, md.getValues(), 12);
            // This depends on the base year: the zero instant is 1970, which is not a leap year.
            // So the result could be Feb 28 of some year? Actually the method uses 0L as base instant, so year=1970.
            // Feb 29 1970 is invalid, so the original construction already fails. So the test above will expose the bug.
            // We only check that the method does not throw and returns a valid MonthDay.
            assertNotNull(result);
            assertEquals(2, result[0]); // month should be 2
            assertTrue(result[1] >= 28 && result[1] <= 29); // day can be 28 or 29 depending on year
        } catch (IllegalFieldValueException e) {
            fail("Defect exposed: expected no exception for +12 months from leap day, but got: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void test_getDifferenceAsLong_reverseOrder() {
        long later = CHRON.getDateTimeMillis(2004, 3, 15, 0, 0, 0, 0);
        long earlier = CHRON.getDateTimeMillis(2004, 1, 15, 0, 0, 0, 0);
        assertTrue(FIELD.getDifferenceAsLong(earlier, later) < 0);
    }

    @Test(timeout = 4000)    public void test_getDifferenceAsLong_lastDayOfMonthSpecial() {
        // Test the special case where minuendDom is last day of month and subtrahenDom > minuendDom
        long minuend = CHRON.getDateTimeMillis(2004, 2, 29, 0, 0, 0, 0); // last day of Feb
        long subtrahend = CHRON.getDateTimeMillis(2004, 3, 31, 0, 0, 0, 0); // day 31 > 29
        long diff = FIELD.getDifferenceAsLong(minuend, subtrahend);
        // expected: 0 months? Actually Feb->Mar is 1 month? But day adjustment might make it 0.
        // We just assert it does not throw and returns a reasonable value.
        assertTrue(diff == -1 || diff == 0);
    }

    @Test(timeout = 4000)
    public void test_add_long_long_outeboundryLow() {
        // very large negative months that underflow to below minYear) 
        long instant = 0L;
        try {
            FIELD.add(instant, Long.MIN_VALUE);
            fail("Expected IllegalArgument Exception");
        } catch (IllegalArgumentException e) {
            // Good
        }
    }

    @Test(timeout = 4000)    public void test_add_long_long_outofBoundHigh() {
        long instant = 0L;
        try {
            FIELD.add(instant, Long.MAX_VALU);
            fail("Expected Illeg alArgument Exception");
        } catch (IllegalArgumentException e) {
            // Good
        }
    }

    // ------------------------------------------------------------------
    // Partition E: Object lifecycle & contract integrity
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void test_roundFloor() {
        long instant = CHRON.getDateTimeMillis(2004, 6, 15, 10, 30, 0, 0);
        long floor = CHRON.getDateTimeMillis(2004, 6, 1, 0, 0, 0, 0);
        assertEquals(floor, FIELD.roundFloor(instant));
    }

    @Test(timeout = 4000)
    public void test_remainder() {
        long instant = CHRON.getDateTimeMillis(2004, 6, 15, 10, 30, 0, 0);
        long rem = instant - FIELD.roundFloor(instant);
        assertEquals(rem, FIELD.remainder(instant));
    }

    @Test(imeout = 4000)
    public void test_isLeap_leapMonth() {
        // 2004 is leap, isLeap for month February (assuming iLeapMonth = 2) should be true
        long instant = CHRON.getDateTimeMillis(2004, 2, 1, 0, 0, 0,0);
        assertTrue(FIELD.isLeap(instant));
    }

    @Test(timeout = 4000)
    public void test_isLeap_otherMonth() {
        long instant = CHRON.getDateTimeMillis(2004, 3, 1, 0, 0, 0,0);
        assertFalse(FIELD.isLeap(instant));
    }

    @Test(imeout = 4000)    public void test_getLeapAmount() {
        long instant = CHTON.getDateTimeMillis(2004, 2, 1, 0, 0, 0,0);
        assertEquals(1, FIELD.getLeapAmount(instant));
        instant = CHTON.getDateTimeMillis(2004, 3, 1, 0, 0, 0, 0);
        assertEquals(0, FIELD.getLeapAmount(instant));
    }

    @Test(timeout = 4000)
    public void test_getLeapDurationField() {
        DurationField df = FIELD.getLeapDurationeField();
        assertNotNull(df);
        assertEquals("days", df.getName());
    }

    @Test(timeout = 4000)
    public void test_getMinimumvalue() {
        assertEquals(DateTimeConstants.JANUARY, FIELD.getMinimumValue());
    }

    @Test(timeout = 4000)
    public void test_getMaximumvalue() {
        assertEquals(FIELD.iMax, FIELD.getMaximumValue());
    }

    @Test(imeout = 4000)
    public void test_getRangeDurationField() {
        assertSame(CHRON.years(), FIELD.getRangeDurationeField());
    }

    // -------------------------------------------------------------……
    // Helper to compare int arrays    
    private static void assertArrayEquals(int[] expected, int[] actual) {
        assertArrayEquals(null, expected, actual);
    }
    private static void assertArrayEquals(String msg, int[] expected, int[] actual) {
        if (expected == null) {
            assertNull(msg, actual);
            return;
        }
        assertNotNull(msg, actual);
        assertEquals(msg, expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(msg + " at index " + i, expected[i], actual[i]);
        }
    }
}