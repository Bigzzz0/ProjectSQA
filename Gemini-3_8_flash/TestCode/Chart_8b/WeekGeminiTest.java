package org.jfree.data.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.jfree.data.time.Week
 * Defect Target: Week(Date, TimeZone) ignores the 'zone' parameter and uses RegularTimePeriod.DEFAULT_TIME_ZONE
 *                causing off-by-one week discrepancy (AssertionFailedError: expected:<35> but was:<34>).
 * Branches & Boundaries Targeted:
 * 1. Constructor: Week() default system date.
 * 2. Constructor: Week(int, int) and Week(int, Year) with valid and extreme week/year boundaries.
 * 3. Constructor: Week(Date) with null and valid timestamps.
 * 4. Constructor: Week(Date, TimeZone) defect reproduction (zone parameter ignored).
 * 5. Constructor: Week(Date, TimeZone, Locale) checking December overflow (week 1 next year) and
 *    January underflow (week 52/53 previous year), null checks for time, zone, and locale.
 * 6. Method: previous() - week > 1, week == 1 with year > 1900, week == 1 with year == 1900 (null boundary).
 * 7. Method: next() - week < 52, week 52 with max 53, week == actualMaxWeek, year 9999 upper bound (null).
 * 8. Method: getFirstMillisecond / getLastMillisecond (with and without Calendar argument), null calendar guards.
 * 9. Method: peg(Calendar) state recalculation.
 * 10. Method: compareTo() - against Week (diff year, same year diff week, identical), other RegularTimePeriod, non-TimePeriod.
 * 11. Method: equals() & hashCode() contracts - reflexive, symmetric, across different week and year attributes.
 * 12. Method: parseWeek() - valid ISO formats ("YYYY-Wnn", "Wnn-YYYY"), various separators ('-', ',', ' ', '.'),
 *     missing separators, unparseable year/week throwing TimePeriodFormatException, null string.
 * 13. Serialization: byte-stream roundtrip ensuring state restoration.
 * ---------------------------------------------------------------------------------------------------------
 */
public class WeekGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Week week = new Week();
        assertTrue("Week number must be >= 1", week.getWeek() >= 1);
        assertTrue("Week number must be <= 53", week.getWeek() <= 53);
        assertTrue("Year must be valid", week.getYearValue() >= 1900);
        assertTrue("First millisecond must be <= last millisecond",
                week.getFirstMillisecond() <= week.getLastMillisecond());
    }

    @Test(timeout = 4000)
    public void testWeekIntIntConstructor() {
        Week week = new Week(26, 2021);
        assertEquals(26, week.getWeek());
        assertEquals(2021, week.getYearValue());
        assertEquals(new Year(2021), week.getYear());
        assertEquals("Week 26, 2021", week.toString());
        assertEquals(2021 * 53L + 26, week.getSerialIndex());
    }

    @Test(timeout = 4000)
    public void testWeekIntYearConstructor() {
        Year year = new Year(2022);
        Week week = new Week(15, year);
        assertEquals(15, week.getWeek());
        assertEquals(2022, week.getYearValue());
        assertEquals(year, week.getYear());
    }

    @Test(timeout = 4000)
    public void testPreviousNormal() {
        Week week = new Week(20, 2020);
        RegularTimePeriod prev = week.previous();
        assertNotNull(prev);
        assertEquals(19, ((Week) prev).getWeek());
        assertEquals(2020, ((Week) prev).getYearValue());
    }

    @Test(timeout = 4000)
    public void testPreviousWeek1YearTransition() {
        // Week 1 of 2005 transitioned back to week 53 of 2004 in Gregorian Calendar
        Week week = new Week(1, 2005);
        RegularTimePeriod prev = week.previous();
        assertNotNull(prev);
        Week prevWeek = (Week) prev;
        assertEquals(2004, prevWeek.getYearValue());
        assertTrue("Previous year's last week should be 52 or 53",
                prevWeek.getWeek() == 52 || prevWeek.getWeek() == 53);
    }

    @Test(timeout = 4000)
    public void testNextNormal() {
        Week week = new Week(20, 2020);
        RegularTimePeriod next = week.next();
        assertNotNull(next);
        assertEquals(21, ((Week) next).getWeek());
        assertEquals(2020, ((Week) next).getYearValue());
    }

    @Test(timeout = 4000)
    public void testNextWeek52YearTransition() {
        // Year 2001 has 52 weeks in typical Gregorian settings
        Calendar cal = Calendar.getInstance();
        cal.set(2001, Calendar.DECEMBER, 31);
        int maxWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR);

        Week week = new Week(maxWeeks, 2001);
        RegularTimePeriod next = week.next();
        assertNotNull(next);
        assertEquals(1, ((Week) next).getWeek());
        assertEquals(2002, ((Week) next).getYearValue());
    }

    @Test(timeout = 4000)
    public void testPegUpdatesMilliseconds() {
        Week week = new Week(10, 2020);
        Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.UK);
        week.peg(gmtCal);
        long gmtStart = week.getFirstMillisecond();
        long gmtEnd = week.getLastMillisecond();

        Calendar jstCal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"), Locale.UK);
        week.peg(jstCal);
        long jstStart = week.getFirstMillisecond();
        long jstEnd = week.getLastMillisecond();

        assertNotEquals("Start millis must shift when pegged to a different time zone", gmtStart, jstStart);
        assertNotEquals("End millis must shift when pegged to a different time zone", gmtEnd, jstEnd);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLowerBoundPreviousAt1900() {
        Week week = new Week(1, 1900);
        assertNull("previous() on Week 1, 1900 must return null", week.previous());
    }

    @Test(timeout = 4000)
    public void testUpperBoundNextAt9999() {
        Week week = new Week(53, 9999);
        assertNull("next() on Week 53, 9999 must return null", week.next());
    }

    @Test(timeout = 4000)
    public void testYearEndDecemberRolloverToWeek1NextYear() {
        // In ISO 8601, 2012-12-31 is Monday, which belongs to Week 1 of 2013
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        cal.clear();
        cal.set(2012, Calendar.DECEMBER, 31, 12, 0, 0);

        Week week = new Week(cal.getTime(), TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        assertEquals("December 31, 2012 should roll into Week 1", 1, week.getWeek());
        assertEquals("December 31, 2012 should roll into Year 2013", 2013, week.getYearValue());
    }

    @Test(timeout = 4000)
    public void testYearStartJanuaryRollbackToLastWeekPreviousYear() {
        // In ISO 8601, 2005-01-01 is Saturday, which belongs to Week 53 of 2004
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        cal.clear();
        cal.set(2005, Calendar.JANUARY, 1, 12, 0, 0);

        Week week = new Week(cal.getTime(), TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        assertEquals("January 1, 2005 should roll back to Week 53", 53, week.getWeek());
        assertEquals("January 1, 2005 should roll back to Year 2004", 2004, week.getYearValue());
    }

    @Test(timeout = 4000)
    public void testMillisecondConsistencyWithCalendar() {
        Week week = new Week(25, 2023);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        long start = week.getFirstMillisecond(cal);
        long end = week.getLastMillisecond(cal);

        assertTrue("End millisecond must exceed start millisecond", end > start);
        assertEquals("Week duration must be exactly 7 days minus 1 millisecond",
                (7L * 24 * 60 * 60 * 1000) - 1, end - start);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where Week(Date, TimeZone) erroneously passes
     * RegularTimePeriod.DEFAULT_TIME_ZONE instead of the supplied 'zone'.
     *
     * In this test, we deliberately place a date at the boundary of a week
     * in the default time zone (Sunday night -> Week 34 in UK locale), while
     * in a time zone ahead (+3 hours) it is already Monday morning -> Week 35.
     * When the defective constructor ignores 'zone', it asserts week 34 instead of 35.
     */
    @Test(timeout = 4000)
    public void testConstructor() {
        Locale savedLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.UK);
            TimeZone defaultZone = RegularTimePeriod.DEFAULT_TIME_ZONE;

            // Aug 28, 2005 is Sunday (end of week 34 in UK locale)
            Calendar cal = Calendar.getInstance(defaultZone, Locale.UK);
            cal.clear();
            cal.set(2005, Calendar.AUGUST, 28, 23, 0, 0);
            Date date = cal.getTime();

            // Create a target timezone 3 hours ahead of defaultZone
            int aheadOffset = defaultZone.getRawOffset() + 3 * 3600 * 1000;
            String[] ids = TimeZone.getAvailableIDs(aheadOffset);
            TimeZone targetZone = ids.length > 0
                    ? TimeZone.getTimeZone(ids[0])
                    : new SimpleTimeZone(aheadOffset, "AheadZone");

            // In targetZone, it is already Monday morning Aug 29, 2005 -> Week 35
            Week week = new Week(date, targetZone);
            assertEquals("Week(Date, TimeZone) must honor the specified time zone", 35, week.getWeek());
        } finally {
            Locale.setDefault(savedLocale);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDateConstructorNull() {
        new Week(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThreeArgConstructorNullDate() {
        new Week(null, TimeZone.getDefault(), Locale.getDefault());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThreeArgConstructorNullZone() {
        new Week(new Date(), null, Locale.getDefault());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testThreeArgConstructorNullLocale() {
        new Week(new Date(), TimeZone.getDefault(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetFirstMillisecondNullCalendar() {
        Week week = new Week(10, 2020);
        week.getFirstMillisecond(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetLastMillisecondNullCalendar() {
        Week week = new Week(10, 2020);
        week.getLastMillisecond(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPegNullCalendar() {
        Week week = new Week(10, 2020);
        week.peg(null);
    }

    @Test(timeout = 4000)
    public void testParseWeekNullReturnsNull() {
        assertNull(Week.parseWeek(null));
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekMissingSeparator() {
        Week.parseWeek("202005");
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekUnparseableYearBothSides() {
        Week.parseWeek("invalid-data");
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekYearFirstInvalidWeekNumber() {
        Week.parseWeek("2020-99");
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekYearFirstNonNumericWeek() {
        Week.parseWeek("2020-WXYZ");
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekWeekFirstInvalidWeekNumber() {
        Week.parseWeek("99-2020");
    }

    @Test(expected = TimePeriodFormatException.class, timeout = 4000)
    public void testParseWeekWeekFirstNonNumericWeek() {
        Week.parseWeek("XYZ-2020");
    }

    @Test(timeout = 4000)
    public void testParseWeekVariousSeparators() {
        Week expected = new Week(12, 2020);
        assertEquals(expected, Week.parseWeek("2020-W12"));
        assertEquals(expected, Week.parseWeek("2020-12"));
        assertEquals(expected, Week.parseWeek("W12-2020"));
        assertEquals(expected, Week.parseWeek("12-2020"));
        assertEquals(expected, Week.parseWeek("2020,12"));
        assertEquals(expected, Week.parseWeek("2020 12"));
        assertEquals(expected, Week.parseWeek("2020.12"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Week w1 = new Week(30, 2020);
        Week w2 = new Week(30, 2020);
        Week wDiffWeek = new Week(31, 2020);
        Week wDiffYear = new Week(30, 2021);

        // Reflexive
        assertEquals(w1, w1);
        // Symmetric
        assertEquals(w1, w2);
        assertEquals(w2, w1);
        assertEquals(w1.hashCode(), w2.hashCode());

        // Dissimilar values
        assertNotEquals(w1, wDiffWeek);
        assertNotEquals(w1, wDiffYear);
        assertNotEquals(w1, null);
        assertNotEquals(w1, "Non-Week Object");
    }

    @Test(timeout = 4000)
    public void testCompareTo() {
        Week w1 = new Week(20, 2020);
        Week wIdentical = new Week(20, 2020);
        Week wEarlierWeek = new Week(19, 2020);
        Week wLaterWeek = new Week(21, 2020);
        Week wEarlierYear = new Week(20, 2019);
        Week wLaterYear = new Week(20, 2021);

        assertEquals(0, w1.compareTo(wIdentical));
        assertTrue(w1.compareTo(wEarlierWeek) > 0);
        assertTrue(w1.compareTo(wLaterWeek) < 0);
        assertTrue(w1.compareTo(wEarlierYear) > 0);
        assertTrue(w1.compareTo(wLaterYear) < 0);

        // Comparison against non-Week RegularTimePeriod returns 0
        Year year = new Year(2020);
        assertEquals(0, w1.compareTo(year));

        // Comparison against generic non-TimePeriod returns 1
        assertEquals(1, w1.compareTo(new Object()));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        Week original = new Week(42, 2018);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Week deserialized = (Week) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.getSerialIndex(), deserialized.getSerialIndex());
        assertEquals(original.getFirstMillisecond(), deserialized.getFirstMillisecond());
        assertEquals(original.getLastMillisecond(), deserialized.getLastMillisecond());
    }
}