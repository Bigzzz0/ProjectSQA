/*
 *  Copyright 2001-2014 Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.time.chrono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.joda.time.Chronology;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MonthDay;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;
import org.joda.time.YearMonth;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: BasicMonthOfYearDateTimeField
 *
 * Methods and Targeted Branches:
 * 1. isLenient() -> Always false.
 * 2. get(long) -> Chronology month delegation.
 * 3. add(long, int):
 *    - months == 0 (early return).
 *    - monthToUse >= 0 (positive months offset, year rollover, remainder).
 *    - monthToUse < 0 (negative offset, year rollback, remMonthToUse == 0 boundary, monthToUse == 1 boundary).
 *    - dayOfMonth coercion when target month has fewer days (e.g. Jan 31 -> Feb 28/29, Mar 31 -> Apr 30).
 *    - preservation of millisOfDay.
 * 4. add(long, long):
 *    - (int)months == months -> delegating to add(long, int).
 *    - months > Integer.MAX_VALUE / months < Integer.MIN_VALUE (large offset logic).
 *    - yearToUse < minYear || yearToUse > maxYear -> throws IllegalArgumentException.
 *    - monthToUse >= 0 and monthToUse < 0 large branches.
 * 5. add(ReadablePartial, int, int[], int):
 *    - valueToAdd == 0 -> returns original values.
 *    - isContiguous(partial) -> MonthDay / YearMonth contiguous resolution.
 *    - Defect Ground Truth: MonthDay(2, 29) when base instant is 0L (1970, non-leap year)
 *      fails bounds check or coerces to 28 instead of preserving leap-day arithmetic.
 *    - Non-contiguous partial fallback to super.add.
 * 6. addWrapField(long, int) -> Wraps month strictly within [1, 12].
 * 7. getDifferenceAsLong(long, long):
 *    - minuendInstant < subtrahendInstant -> inverted negation recursion.
 *    - minuendDom == daysInMinuendMonth && subtrahendDom > minuendDom boundary condition.
 *    - minuendRem < subtrahendRem remainder adjustment (decrement difference).
 * 8. set(long, int):
 *    - Bounds verification [1, 12] via FieldUtils.
 *    - Day of month clipping when setting shorter month (e.g. July 31 -> June 30).
 *    - Millis of day preservation.
 * 9. Lifecycle, Leap, and Durations:
 *    - isLeap(long), getLeapAmount(long), getLeapDurationField().
 *    - getRangeDurationField(), getMinimumValue(), getMaximumValue().
 *    - roundFloor(long), remainder(long).
 *    - Serialization & readResolve() singleton resolution.
 */
public class BasicMonthOfYearDateTimeFieldGeminiTest {

    private GregorianChronology gChronology;
    private BasicMonthOfYearDateTimeField field;

    @Before
    public void setUp() {
        gChronology = GregorianChronology.getInstanceUTC();
        field = new BasicMonthOfYearDateTimeField(gChronology, DateTimeConstants.FEBRUARY);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsLenient() {
        assertFalse("BasicMonthOfYearDateTimeField must not be lenient", field.isLenient());
    }

    @Test(timeout = 4000)
    public void testGetAndRangeValues() {
        assertEquals("Minimum value must be 1 (January)", 1, field.getMinimumValue());
        assertEquals("Maximum value must be 12 (December)", 12, field.getMaximumValue());

        // 2021-06-15 12:00:00 UTC
        long instant = gChronology.getDateTimeMillis(2021, 6, 15, 12, 0, 0, 0);
        assertEquals(6, field.get(instant));

        DurationField rangeField = field.getRangeDurationField();
        assertEquals("Range duration field must be years", DurationFieldType.years(), rangeField.getType());
        DurationField leapField = field.getLeapDurationField();
        assertEquals("Leap duration field must be days", DurationFieldType.days(), leapField.getType());
    }

    @Test(timeout = 4000)
    public void testAddIntZero() {
        long instant = gChronology.getDateTimeMillis(2021, 5, 20, 10, 30, 0, 0);
        long result = field.add(instant, 0);
        assertEquals("Adding zero months must return exact instant", instant, result);
    }

    @Test(timeout = 4000)
    public void testAddIntPositiveNoRollover() {
        long instant = gChronology.getDateTimeMillis(2021, 1, 15, 8, 15, 30, 250);
        long result = field.add(instant, 5); // 2021-06-15
        assertEquals(2021, gChronology.getYear(result));
        assertEquals(6, gChronology.getMonthOfYear(result));
        assertEquals(15, gChronology.getDayOfMonth(result));
        assertEquals(gChronology.getMillisOfDay(instant), gChronology.getMillisOfDay(result));
    }

    @Test(timeout = 4000)
    public void testAddIntPositiveWithYearRollover() {
        long instant = gChronology.getDateTimeMillis(2021, 10, 10, 14, 0, 0, 0);
        long result = field.add(instant, 15); // + 1 year and 3 months -> 2023-01-10
        assertEquals(2023, gChronology.getYear(result));
        assertEquals(1, gChronology.getMonthOfYear(result));
        assertEquals(10, gChronology.getDayOfMonth(result));
    }

    @Test(timeout = 4000)
    public void testAddIntNegativeWithinYear() {
        long instant = gChronology.getDateTimeMillis(2021, 11, 20, 5, 0, 0, 0);
        long result = field.add(instant, -4); // 2021-07-20
        assertEquals(2021, gChronology.getYear(result));
        assertEquals(7, gChronology.getMonthOfYear(result));
        assertEquals(20, gChronology.getDayOfMonth(result));
    }

    @Test(timeout = 4000)
    public void testAddIntNegativeCrossYearBoundaryExactZeroRemainder() {
        // monthToUse % 12 == 0 boundary
        long instant = gChronology.getDateTimeMillis(2021, 6, 15, 0, 0, 0, 0);
        long result = field.add(instant, -6); // 2020-12-15
        assertEquals(2020, gChronology.getYear(result));
        assertEquals(12, gChronology.getMonthOfYear(result));
        assertEquals(15, gChronology.getDayOfMonth(result));
    }

    @Test(timeout = 4000)
    public void testAddIntNegativeMonthToUseEqualsOneBoundary() {
        // Triggers branch: if (monthToUse == 1) yearToUse += 1;
        long instant = gChronology.getDateTimeMillis(2021, 5, 15, 0, 0, 0, 0);
        long result = field.add(instant, -4); // 2021-01-15
        assertEquals(2021, gChronology.getYear(result));
        assertEquals(1, gChronology.getMonthOfYear(result));
    }

    @Test(timeout = 4000)
    public void testAddIntDayCoercionToLeapYear() {
        // Leap year: 2020 is leap, 2019 is not
        long leapInstant = gChronology.getDateTimeMillis(2020, 1, 31, 12, 0, 0, 0);
        long febLeap = field.add(leapInstant, 1); // 2020-02-29
        assertEquals(2020, gChronology.getYear(febLeap));
        assertEquals(2, gChronology.getMonthOfYear(febLeap));
        assertEquals(29, gChronology.getDayOfMonth(febLeap));

        long nonLeapInstant = gChronology.getDateTimeMillis(2019, 1, 31, 12, 0, 0, 0);
        long febNonLeap = field.add(nonLeapInstant, 1); // 2019-02-28
        assertEquals(2019, gChronology.getYear(febNonLeap));
        assertEquals(2, gChronology.getMonthOfYear(febNonLeap));
        assertEquals(28, gChronology.getDayOfMonth(febNonLeap));
    }

    @Test(timeout = 4000)
    public void testAddWrapField() {
        long instant = gChronology.getDateTimeMillis(2021, 11, 15, 0, 0, 0, 0);
        long wrapped = field.addWrapField(instant, 3); // 11 + 3 -> 2 (February)
        assertEquals(2021, gChronology.getYear(wrapped));
        assertEquals(2, gChronology.getMonthOfYear(wrapped));

        long wrappedNegative = field.addWrapField(instant, -11); // 11 - 11 -> 12 (December)
        assertEquals(2021, gChronology.getYear(wrappedNegative));
        assertEquals(12, gChronology.getMonthOfYear(wrappedNegative));
    }

    @Test(timeout = 4000)
    public void testRoundFloorAndRemainder() {
        long instant = gChronology.getDateTimeMillis(2021, 8, 25, 15, 45, 30, 500);
        long rounded = field.roundFloor(instant);
        long expectedFloor = gChronology.getDateTimeMillis(2021, 8, 1, 0, 0, 0, 0);
        assertEquals(expectedFloor, rounded);
        assertEquals(instant - expectedFloor, field.remainder(instant));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddLongDelegatingToInt() {
        long instant = gChronology.getDateTimeMillis(2021, 3, 10, 0, 0, 0, 0);
        long result = field.add(instant, 5L);
        assertEquals(8, gChronology.getMonthOfYear(result));
    }

    @Test(timeout = 4000)
    public void testAddLongLargeMagnitude() {
        long instant = gChronology.getDateTimeMillis(2000, 1, 15, 6, 30, 0, 0);
        long largeMonths = 120000L; // 10,000 years
        long result = field.add(instant, largeMonths);
        assertEquals(12000, gChronology.getYear(result));
        assertEquals(1, gChronology.getMonthOfYear(result));
        assertEquals(15, gChronology.getDayOfMonth(result));

        long negativeLargeMonths = -120000L;
        long resultNeg = field.add(instant, negativeLargeMonths);
        assertEquals(-8000, gChronology.getYear(resultNeg));
        assertEquals(1, gChronology.getMonthOfYear(resultNeg));
        assertEquals(15, gChronology.getDayOfMonth(resultNeg));
    }

    @Test(timeout = 4000)
    public void testAddLongNegativeCrossYearBoundaryExactZeroRemainder() {
        long instant = gChronology.getDateTimeMillis(2000, 6, 15, 0, 0, 0, 0);
        long largeMonths = -(12L * 1000L + 6L); // -1000 years and 6 months
        long result = field.add(instant, largeMonths);
        assertEquals(999, gChronology.getYear(result));
        assertEquals(12, gChronology.getMonthOfYear(result));
    }

    @Test(timeout = 4000)
    public void testAddLongDayCoercion() {
        long instant = gChronology.getDateTimeMillis(2000, 1, 31, 0, 0, 0, 0);
        long largeMonths = 12L * 400L + 1L; // 2400-02 (2400 is leap)
        long result = field.add(instant, largeMonths);
        assertEquals(2400, gChronology.getYear(result));
        assertEquals(2, gChronology.getMonthOfYear(result));
        assertEquals(29, gChronology.getDayOfMonth(result));
    }

    @Test(timeout = 4000)
    public void testSetBoundaryValues() {
        long instant = gChronology.getDateTimeMillis(2021, 5, 10, 12, 0, 0, 0);
        long jan = field.set(instant, 1);
        assertEquals(1, gChronology.getMonthOfYear(jan));
        long dec = field.set(instant, 12);
        assertEquals(12, gChronology.getMonthOfYear(dec));
    }

    @Test(timeout = 4000)
    public void testSetCoerceDayOfMonth() {
        long instant = gChronology.getDateTimeMillis(2021, 1, 31, 18, 0, 0, 0);
        long setApril = field.set(instant, 4); // April has 30 days
        assertEquals(4, gChronology.getMonthOfYear(setApril));
        assertEquals(30, gChronology.getDayOfMonth(setApril));
        assertEquals(18 * 3600 * 1000, gChronology.getMillisOfDay(setApril));
    }

    @Test(timeout = 4000)
    public void testDifference() {
        long t1 = gChronology.getDateTimeMillis(2021, 10, 15, 12, 0, 0, 0);
        long t2 = gChronology.getDateTimeMillis(2021, 4, 15, 12, 0, 0, 0);

        assertEquals(6L, field.getDifferenceAsLong(t1, t2));
        assertEquals(-6L, field.getDifferenceAsLong(t2, t1));
    }

    @Test(timeout = 4000)
    public void testDifferenceWithDayAdjustmentForEndOfMonth() {
        // Last day of minuend month, subtrahend day is larger
        // Minuend: Feb 28, 2021. Subtrahend: Jan 31, 2021
        long minuend = gChronology.getDateTimeMillis(2021, 2, 28, 12, 0, 0, 0);
        long subtrahend = gChronology.getDateTimeMillis(2021, 1, 31, 12, 0, 0, 0);
        assertEquals(1L, field.getDifferenceAsLong(minuend, subtrahend));
    }

    @Test(timeout = 4000)
    public void testDifferenceWithRemainderAdjustment() {
        // Same year and month, but minuend earlier in day/time than subtrahend
        long minuend = gChronology.getDateTimeMillis(2021, 5, 15, 10, 0, 0, 0);
        long subtrahend = gChronology.getDateTimeMillis(2021, 3, 15, 11, 0, 0, 0);
        // Remainder of minuend is less than subtrahend, difference drops by 1
        assertEquals(1L, field.getDifferenceAsLong(minuend, subtrahend));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J issue where adding months to a contiguous ReadablePartial
     * initialized with Feb 29 (leap day) failed bounds validation due to 0L anchor.
     */
    @Test(timeout = 4000)
    public void testDefectAddPartialFromLeapDay() {
        MonthDay leapMonthDay = new MonthDay(2, 29, gChronology);
        int[] initialValues = new int[]{2, 29};
        
        // Adding 1 month to Feb 29 should evaluate to March 29
        int[] resultPlusOne = field.add(leapMonthDay, 0, initialValues, 1);
        assertEquals("Month should advance to March (3)", 3, resultPlusOne[0]);
        assertEquals("Day should remain 29", 29, resultPlusOne[1]);

        // Adding 12 months should wrap back to Feb 29
        int[] resultPlusTwelve = field.add(leapMonthDay, 0, initialValues, 12);
        assertEquals("Month should remain February (2)", 2, resultPlusTwelve[0]);
        assertEquals("Day should remain 29", 29, resultPlusTwelve[1]);
    }

    /**
     * Targets end-of-month adjustment when subtracting months to land on leap February.
     */
    @Test(timeout = 4000)
    public void testDefectSubtractMonthsEndOfMonthLeapAdjust() {
        MonthDay march31 = new MonthDay(3, 31, gChronology);
        int[] initialValues = new int[]{3, 31};

        // In a leap-year-safe partial calculation, subtracting 1 month from March 31 should yield Feb 29
        int[] result = field.add(march31, 0, initialValues, -1);
        assertEquals("Month should be February (2)", 2, result[0]);
        assertEquals("Day should coerce to 29 in leap year context", 29, result[1]);
    }

    @Test(timeout = 4000)
    public void testAddPartialNonContiguous() {
        // Non-contiguous partial: MonthOfYear and MinuteOfHour
        DateTimeFieldType[] types = new DateTimeFieldType[]{
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.minuteOfHour()
        };
        int[] values = new int[]{4, 15};
        Partial partial = new Partial(types, values, gChronology);

        int[] result = field.add(partial, 0, values, 3);
        assertEquals(7, result[0]);
        assertEquals(15, result[1]);
    }

    @Test(timeout = 4000)
    public void testAddPartialValueToAddZero() {
        MonthDay md = new MonthDay(6, 15, gChronology);
        int[] values = new int[]{6, 15};
        int[] result = field.add(md, 0, values, 0);
        assertSame("Should return exact same array reference if adding 0", values, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalFieldValueException.class, timeout = 4000)
    public void testSetBelowMinimumThrows() {
        long instant = gChronology.getDateTimeMillis(2021, 5, 10, 0, 0, 0, 0);
        field.set(instant, 0);
    }

    @Test(expected = IllegalFieldValueException.class, timeout = 4000)
    public void testSetAboveMaximumThrows() {
        long instant = gChronology.getDateTimeMillis(2021, 5, 10, 0, 0, 0, 0);
        field.set(instant, 13);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddLongExceedsMaxYearThrows() {
        long instant = gChronology.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long overflowMonths = ((long) gChronology.getMaxYear() + 10L) * 12L;
        field.add(instant, overflowMonths);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddLongExceedsMinYearThrows() {
        long instant = gChronology.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long underflowMonths = ((long) gChronology.getMinYear() - 2000L - 10L) * 12L;
        field.add(instant, underflowMonths);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testLeapCalculations() {
        long leapYearFeb = gChronology.getDateTimeMillis(2020, 2, 10, 0, 0, 0, 0);
        assertTrue("2020 February must be leap month", field.isLeap(leapYearFeb));
        assertEquals("Leap amount in leap month must be 1", 1, field.getLeapAmount(leapYearFeb));

        long leapYearMar = gChronology.getDateTimeMillis(2020, 3, 10, 0, 0, 0, 0);
        assertFalse("2020 March is not leap month", field.isLeap(leapYearMar));
        assertEquals("Leap amount in non-leap month must be 0", 0, field.getLeapAmount(leapYearMar));

        long nonLeapFeb = gChronology.getDateTimeMillis(2019, 2, 10, 0, 0, 0, 0);
        assertFalse("2019 February is not leap in non-leap year", field.isLeap(nonLeapFeb));
        assertEquals(0, field.getLeapAmount(nonLeapFeb));
    }

    @Test(timeout = 4000)
    public void testSerializationReadResolve() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertEquals("Deserialized object must resolve to Chronology's singleton monthOfYear field",
                gChronology.monthOfYear().getClass(), deserialized.getClass());
    }
}