package org.apache.commons.lang3.time;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang3.time.FastDateFormat
 *
 * Decision / Condition Matrix Covered:
 * 1. Pattern Tokenization (parsePattern, parseToken):
 *    - Letters ('G', 'y', 'M', 'd', 'h', 'H', 'm', 's', 'S', 'E', 'D', 'F', 'w', 'W', 'a', 'k', 'K', 'z', 'Z')
 *    - Padded vs Unpadded vs TwoDigit number field rules (padding 1, 2, >=3)
 *    - Text field rules (short vs full names for ERA, Month, Day-of-week, AM/PM)
 *    - Hour fields boundary conditions:
 *      * TwelveHourField: value == 0 -> calendar.getLeastMaximum(HOUR) + 1 (12 AM/PM rollover)
 *      * TwentyFourHourField: value == 0 -> calendar.getMaximum(HOUR_OF_DAY) + 1 (24:00 rollover)
 *    - TimeZoneNameRule: forced vs unforced calendar timezone, daylight saving vs standard time, short vs long
 *    - TimeZoneNumberRule: positive vs negative offset, with colon (ZZ) vs without colon (Z)
 *    - Literals: single character ('x'), multi-character ('text'), escaped single quotes ('')
 *    - Invalid pattern component throws IllegalArgumentException
 *
 * 2. Caching & Factory Methods:
 *    - getInstance(), getDateInstance(), getTimeInstance(), getDateTimeInstance()
 *    - Cache reuse validation for identical parameters (InstanceCache, DateInstanceCache, TimeInstanceCache, DateTimeInstanceCache)
 *    - Null vs specified TimeZone and Locale defaults
 *    - Invalid style parameters throw IllegalArgumentException
 *
 * 3. Formatting Dispatch & Overrides:
 *    - format(Object) for Date, Calendar, Long, and invalid types/null (IllegalArgumentException)
 *    - format(long), format(Date), format(Calendar) to String and to StringBuffer
 *    - TimeZone override: mTimeZoneForced == true (clones calendar and sets TZ) vs false (uses calendar TZ)
 *
 * 4. Object Contract & Serialization:
 *    - equals(): identity, null, different types, different pattern/TZ/locale/forced flags
 *    - hashCode(): symmetry with equals
 *    - toString(): pattern containment
 *    - parseObject(): unsupported operation contract (returns null, resets ParsePosition indices)
 *    - Java Serialization: round-trip writeObject/readObject restoring transient rules and maxLengthEstimate
 *
 * 5. Target Defect (LANG-645):
 *    - Week of year calculation across locale boundaries (e.g., sv_SE where first day of week is Monday
 *      and minimal days in first week is 4). Demonstrates format(Date) failing to respect Locale-dependent
 *      Calendar properties if Calendar instance is created without locale.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class FastDateFormatGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardFormattingAllTokens() {
        // Construct calendar: 2023-07-04 (Tuesday) 09:05:07.089 AM GMT
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JULY, 4, 9, 5, 7);
        cal.set(Calendar.MILLISECOND, 89);
        Date date = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy MM dd HH mm ss SSS EEE", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals("AD 2023 07 04 09 05 07 089 Tue", fdf.format(date));
    }

    @Test(timeout = 4000)
    public void testMonthFormattingVariants() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 15);
        Date date = cal.getTime();

        assertEquals("3", FastDateFormat.getInstance("M", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("03", FastDateFormat.getInstance("MM", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("Mar", FastDateFormat.getInstance("MMM", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("March", FastDateFormat.getInstance("MMMM", TimeZone.getTimeZone("UTC"), Locale.US).format(date));

        // Test two-digit month with value >= 10
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        Date novDate = cal.getTime();
        assertEquals("11", FastDateFormat.getInstance("M", TimeZone.getTimeZone("UTC"), Locale.US).format(novDate));
        assertEquals("11", FastDateFormat.getInstance("MM", TimeZone.getTimeZone("UTC"), Locale.US).format(novDate));
    }

    @Test(timeout = 4000)
    public void testYearFormattingVariants() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2009, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        assertEquals("09", FastDateFormat.getInstance("y", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("09", FastDateFormat.getInstance("yy", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("09", FastDateFormat.getInstance("yyy", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("2009", FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("02009", FastDateFormat.getInstance("yyyyy", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
    }

    @Test(timeout = 4000)
    public void testDayAndWeekVariants() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 8); // 8th day of month, 2nd week, Sunday
        Date date = cal.getTime();

        assertEquals("8", FastDateFormat.getInstance("d", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("08", FastDateFormat.getInstance("dd", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("8", FastDateFormat.getInstance("D", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("008", FastDateFormat.getInstance("DDD", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("2", FastDateFormat.getInstance("F", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("2", FastDateFormat.getInstance("w", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("02", FastDateFormat.getInstance("ww", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
        assertEquals("2", FastDateFormat.getInstance("W", TimeZone.getTimeZone("UTC"), Locale.US).format(date));
    }

    @Test(timeout = 4000)
    public void testAmPmAndHours() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 15, 30); // Midnight: 00:15:30
        Date midnight = cal.getTime();

        // 12-hour field 'h' at 0 -> 12
        assertEquals("12 AM", FastDateFormat.getInstance("h a", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));
        assertEquals("12 AM", FastDateFormat.getInstance("hh a", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));

        // 24-hour field 'k' at 0 -> 24
        assertEquals("24", FastDateFormat.getInstance("k", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));
        assertEquals("24", FastDateFormat.getInstance("kk", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));

        // 0-based hour fields 'K' and 'H' at midnight -> 0
        assertEquals("0", FastDateFormat.getInstance("K", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));
        assertEquals("00", FastDateFormat.getInstance("KK", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));
        assertEquals("0", FastDateFormat.getInstance("H", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));
        assertEquals("00", FastDateFormat.getInstance("HH", TimeZone.getTimeZone("UTC"), Locale.US).format(midnight));

        // Test Afternoon: 13:00 (1 PM)
        cal.set(Calendar.HOUR_OF_DAY, 13);
        Date afternoon = cal.getTime();
        assertEquals("1 PM", FastDateFormat.getInstance("h a", TimeZone.getTimeZone("UTC"), Locale.US).format(afternoon));
        assertEquals("01 PM", FastDateFormat.getInstance("hh a", TimeZone.getTimeZone("UTC"), Locale.US).format(afternoon));
        assertEquals("13", FastDateFormat.getInstance("k", TimeZone.getTimeZone("UTC"), Locale.US).format(afternoon));
        assertEquals("13", FastDateFormat.getInstance("H", TimeZone.getTimeZone("UTC"), Locale.US).format(afternoon));
        assertEquals("1", FastDateFormat.getInstance("K", TimeZone.getTimeZone("UTC"), Locale.US).format(afternoon));
    }

    @Test(timeout = 4000)
    public void testDayOfWeekFormatting() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JULY, 3); // Monday
        Date date = cal.getTime();

        FastDateFormat shortDay = FastDateFormat.getInstance("E", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fullDay = FastDateFormat.getInstance("EEEE", TimeZone.getTimeZone("UTC"), Locale.US);

        assertEquals("Mon", shortDay.format(date));
        assertEquals("Monday", fullDay.format(date));
    }

    @Test(timeout = 4000)
    public void testLiteralQuoting() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);
        Date date = cal.getTime();

        FastDateFormat fdf1 = FastDateFormat.getInstance("'Date: 'yyyy' / 'd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("Date: 2023 / 1", fdf1.format(date));

        // Escaped single quote '' inside text literal
        FastDateFormat fdf2 = FastDateFormat.getInstance("'It''s 'yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("It's 2023", fdf2.format(date));

        // Single character literal
        FastDateFormat fdf3 = FastDateFormat.getInstance("'#'yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("#2023", fdf3.format(date));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumericRules() {
        TimeZone tzNegative = TimeZone.getTimeZone("GMT-05:00");
        Calendar calNeg = Calendar.getInstance(tzNegative, Locale.US);
        calNeg.clear();
        calNeg.set(2023, Calendar.JANUARY, 1);

        assertEquals("-0500", FastDateFormat.getInstance("Z", tzNegative, Locale.US).format(calNeg));
        assertEquals("-05:00", FastDateFormat.getInstance("ZZ", tzNegative, Locale.US).format(calNeg));

        TimeZone tzPositive = TimeZone.getTimeZone("GMT+08:30");
        Calendar calPos = Calendar.getInstance(tzPositive, Locale.US);
        calPos.clear();
        calPos.set(2023, Calendar.JANUARY, 1);

        assertEquals("+0830", FastDateFormat.getInstance("Z", tzPositive, Locale.US).format(calPos));
        assertEquals("+08:30", FastDateFormat.getInstance("ZZ", tzPositive, Locale.US).format(calPos));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayRulesStandardAndDaylight() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdfShort = FastDateFormat.getInstance("z", tz, Locale.US);
        FastDateFormat fdfLong = FastDateFormat.getInstance("zzzz", tz, Locale.US);

        // Winter (Standard Time)
        Calendar winter = Calendar.getInstance(tz, Locale.US);
        winter.clear();
        winter.set(2023, Calendar.JANUARY, 15, 12, 0, 0);

        assertEquals("EST", fdfShort.format(winter));
        assertEquals("Eastern Standard Time", fdfLong.format(winter));

        // Summer (Daylight Saving Time)
        Calendar summer = Calendar.getInstance(tz, Locale.US);
        summer.clear();
        summer.set(2023, Calendar.JULY, 15, 12, 0, 0);

        assertEquals("EDT", fdfShort.format(summer));
        assertEquals("Eastern Daylight Time", fdfLong.format(summer));

        // Unforced TimeZone in formatter, deriving from Calendar
        FastDateFormat unforcedShort = FastDateFormat.getInstance("z", Locale.US);
        FastDateFormat unforcedLong = FastDateFormat.getInstance("zzzz", Locale.US);
        assertEquals("EST", unforcedShort.format(winter));
        assertEquals("Eastern Standard Time", unforcedLong.format(winter));
        assertEquals("EDT", unforcedShort.format(summer));
        assertEquals("Eastern Daylight Time", unforcedLong.format(summer));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        FastDateFormat fdf = FastDateFormat.getInstance("");
        assertEquals("", fdf.format(new Date()));
        assertEquals(0, fdf.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testNumberRulePaddingBoundaries() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();

        // 1-digit, 2-digit, 3-digit, 4-digit, 5-digit padded number rule formatting
        FastDateFormat fdfPadded = FastDateFormat.getInstance("yyyyy SSSSS", TimeZone.getTimeZone("UTC"), Locale.US);

        // Value < 100
        cal.set(Calendar.YEAR, 25);
        cal.set(Calendar.MILLISECOND, 7);
        assertEquals("00025 00007", fdfPadded.format(cal));

        // 100 <= Value < 1000
        cal.set(Calendar.YEAR, 543);
        cal.set(Calendar.MILLISECOND, 543);
        assertEquals("00543 00543", fdfPadded.format(cal));

        // Value >= 1000
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MILLISECOND, 999);
        assertEquals("02023 00999", fdfPadded.format(cal));
    }

    @Test(timeout = 4000)
    public void testUnpaddedNumberFieldBoundaries() {
        FastDateFormat fdf = FastDateFormat.getInstance("d", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();

        cal.set(Calendar.DAY_OF_MONTH, 5); // < 10
        assertEquals("5", fdf.format(cal));

        cal.set(Calendar.DAY_OF_MONTH, 28); // < 100
        assertEquals("28", fdf.format(cal));

        FastDateFormat fdfDayOfYear = FastDateFormat.getInstance("D", TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(Calendar.DAY_OF_YEAR, 256); // >= 100
        assertEquals("256", fdfDayOfYear.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitNumberFieldBoundaries() {
        FastDateFormat fdf = FastDateFormat.getInstance("dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();

        cal.set(Calendar.DAY_OF_MONTH, 7); // < 100
        assertEquals("07", fdf.format(cal));

        cal.set(Calendar.DAY_OF_MONTH, 25); // < 100
        assertEquals("25", fdf.format(cal));

        // Padded field where field value is >= 100 (e.g. DAY_OF_YEAR with 2 digits)
        FastDateFormat fdfDayOfYear = FastDateFormat.getInstance("DD", TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(Calendar.DAY_OF_YEAR, 123);
        assertEquals("123", fdfDayOfYear.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatBufferAndEpochZero() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"), Locale.US);
        StringBuffer buf = new StringBuffer("Result: ");
        StringBuffer returned = fdf.format(0L, buf);
        assertSame(buf, returned);
        assertEquals("Result: 1970-01-01 00:00:00.000", buf.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-645 & TimeZone Override)
    // =========================================================================

    /**
     * Targets known defect LANG-645:
     * When formatting a Date using a FastDateFormat with a specific Locale (e.g. Swedish "sv", "SE"),
     * the formatter created its internal Calendar without the locale (new GregorianCalendar(mTimeZone)),
     * which caused Locale-dependent calendar rules (like minimalDaysInFirstWeek and firstDayOfWeek)
     * to fallback to the system default, producing week "01" instead of the expected week "53" for Jan 1, 2010.
     */
    @Test(timeout = 4000)
    public void testLang645() {
        Locale locale = new Locale("sv", "SE");
        Calendar cal = Calendar.getInstance(locale);
        cal.clear();
        cal.set(2010, Calendar.JANUARY, 1);
        Date d = cal.getTime();

        FastDateFormat fdf = FastDateFormat.getInstance("EEEE', week 'ww", locale);
        assertEquals("fredag, week 53", fdf.format(d));
    }

    @Test(timeout = 4000)
    public void testTimeZoneOverridesCalendarBranch() {
        TimeZone tzGmt = TimeZone.getTimeZone("GMT");
        TimeZone tzTokyo = TimeZone.getTimeZone("Asia/Tokyo");

        Calendar calTokyo = Calendar.getInstance(tzTokyo, Locale.US);
        calTokyo.clear();
        calTokyo.set(2023, Calendar.JANUARY, 1, 9, 0, 0); // 09:00 in Tokyo = 00:00 in GMT

        // Case 1: TimeZone is forced via constructor/factory
        FastDateFormat fdfForced = FastDateFormat.getInstance("HH:mm", tzGmt, Locale.US);
        assertTrue(fdfForced.getTimeZoneOverridesCalendar());
        assertEquals("00:00", fdfForced.format(calTokyo));

        // Case 2: TimeZone is not forced (null passed into factory)
        FastDateFormat fdfUnforced = FastDateFormat.getInstance("HH:mm", Locale.US);
        assertFalse(fdfUnforced.getTimeZoneOverridesCalendar());
        assertEquals("09:00", fdfUnforced.format(calTokyo));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPatternThrows() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIllegalPatternComponentThrows() {
        FastDateFormat.getInstance("yyyy-MM-dd X"); // 'X' is unsupported in Commons Lang 3.0 FastDateFormat
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatNullObjectThrows() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format(null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnknownObjectClassThrows() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("NotADateOrCalendar", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidDateStyleThrows() {
        FastDateFormat.getDateInstance(999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidTimeStyleThrows() {
        FastDateFormat.getTimeInstance(999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidDateTimeStyleThrows() {
        FastDateFormat.getDateTimeInstance(999, 999);
    }

    @Test(timeout = 4000)
    public void testParseObjectContract() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(4);
        pos.setErrorIndex(2);

        Object result = fdf.parseObject("2023-01-01", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryGetInstancesCaching() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertSame(fdf1, fdf2);

        FastDateFormat fdfPattern1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdfPattern2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertSame(fdfPattern1, fdfPattern2);

        FastDateFormat fdfTz1 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"));
        FastDateFormat fdfTz2 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"));
        assertSame(fdfTz1, fdfTz2);

        FastDateFormat fdfLoc1 = FastDateFormat.getInstance("yyyy", Locale.GERMANY);
        FastDateFormat fdfLoc2 = FastDateFormat.getInstance("yyyy", Locale.GERMANY);
        assertSame(fdfLoc1, fdfLoc2);

        FastDateFormat fdfFull1 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"), Locale.GERMANY);
        FastDateFormat fdfFull2 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"), Locale.GERMANY);
        assertSame(fdfFull1, fdfFull2);

        // Date instances
        assertSame(FastDateFormat.getDateInstance(FastDateFormat.SHORT), FastDateFormat.getDateInstance(FastDateFormat.SHORT));
        assertSame(FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.UK), FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.UK));
        assertSame(FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT")), FastDateFormat.getDateInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT")));
        assertSame(FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.FRENCH), FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.FRENCH));

        // Time instances
        assertSame(FastDateFormat.getTimeInstance(FastDateFormat.SHORT), FastDateFormat.getTimeInstance(FastDateFormat.SHORT));
        assertSame(FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.UK), FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.UK));
        assertSame(FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT")), FastDateFormat.getTimeInstance(FastDateFormat.LONG, TimeZone.getTimeZone("GMT")));
        assertSame(FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.FRENCH), FastDateFormat.getTimeInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.FRENCH));

        // DateTime instances
        assertSame(FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT), FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT));
        assertSame(FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG, Locale.ITALY), FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG, Locale.ITALY));
        assertSame(FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.SHORT, TimeZone.getTimeZone("GMT")), FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.SHORT, TimeZone.getTimeZone("GMT")));
        assertSame(FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US), FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDifferentPattern = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fdfDifferentTz = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfDifferentLocale = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.FRANCE);
        FastDateFormat fdfUnforcedTz = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);

        assertEquals(fdf1, fdf1);
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("NotAFastDateFormat"));
        assertFalse(fdf1.equals(fdfDifferentPattern));
        assertFalse(fdf1.equals(fdfDifferentTz));
        assertFalse(fdf1.equals(fdfDifferentLocale));
        assertFalse(fdf1.equals(fdfUnforcedTz));
    }

    @Test(timeout = 4000)
    public void testGettersAndToString() {
        TimeZone tz = TimeZone.getTimeZone("America/Chicago");
        Locale loc = Locale.CANADA;
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);

        assertEquals("yyyy-MM-dd", fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(loc, fdf.getLocale());
        assertTrue(fdf.getMaxLengthEstimate() >= 10);
        assertEquals("FastDateFormat[yyyy-MM-dd]", fdf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectDispatch() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 15);
        Date date = cal.getTime();
        long millis = date.getTime();

        assertEquals("2023-01-15", fdf.format((Object) date, new StringBuffer(), new FieldPosition(0)).toString());
        assertEquals("2023-01-15", fdf.format((Object) cal, new StringBuffer(), new FieldPosition(0)).toString());
        assertEquals("2023-01-15", fdf.format((Object) Long.valueOf(millis), new StringBuffer(), new FieldPosition(0)).toString());

        assertEquals("2023-01-15", fdf.format(date));
        assertEquals("2023-01-15", fdf.format(cal));
        assertEquals("2023-01-15", fdf.format(millis));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS Z", TimeZone.getTimeZone("GMT"), Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getMaxLengthEstimate(), deserialized.getMaxLengthEstimate());

        Date now = new Date(1672531199000L); // 2022-12-31 23:59:59.000 GMT
        assertEquals(original.format(now), deserialized.format(now));
    }
}