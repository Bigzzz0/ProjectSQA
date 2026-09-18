package org.apache.commons.lang3.time;

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

/**
 * Comprehensive JUnit4 test for FastDateFormat with defect targeting.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core formatting with various patterns (y, M, d, h, H, m, s, S, E, D, F, w, W, a, k, K, z, Z, literal)
 * - Partition B: Boundary values (pattern null, empty, length=1, single-digit numbers, two-digit numbers, padded numbers)
 * - Partition C: Defect-specific: timezone forced with UTC, pattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" -> assert correct UTC output
 * - Partition D: Exception paths (null constructor argument, illegal pattern token, unknown object type)
 * - Partition E: Object contracts (equals, hashCode, toString, getters, serialization)
 * - Branch coverage targeted: all switch cases in parsePattern, all Rule implementations, all if/else in parseToken,
 *   selectNumberRule padding switch, format(Object) instanceof checks, format(Calendar) timezoneForced branch,
 *   TimeZoneNameRule (forced vs non-forced), TimeZoneNumberRule (colon vs no-colon), PaddedNumberField (value <100, 100-999, >=1000)
 */
public class FastDateFormatDeepseekTest {

    // ----- Partition A: Core Functional Logic & State Transitions -----

    @Test(timeout = 4000)
    public void testGetInstanceDefault() {
        FastDateFormat fmt = FastDateFormat.getInstance();
        assertNotNull(fmt);
        assertNotNull(fmt.getPattern());
        assertNotNull(fmt.getTimeZone());
        assertNotNull(fmt.getLocale());
        assertTrue(fmt.getMaxLengthEstimate() > 0);
    }

    @Test(timeout = 4000)
    public void testFormatDateWithPattern() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        Date date = new Date(0L); // 1970-01-01 00:00:00 GMT
        assertEquals("1970-01-01", fmt.format(date));
    }

    @Test(timeout = 4000)
    public void testFormatCalendar() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm:ss", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1, 12, 30, 45);
        assertEquals("12:30:45", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatLong() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        long millis = 0L;
        assertEquals("1970-01-01", fmt.format(millis));
    }

    @Test(timeout = 4000)
    public void testFormatObjectDate() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(new Date(0L), buf, pos);
        assertEquals("1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectCalendar() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.setTime(new Date(0L));
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format((Object) cal, buf, pos);
        assertEquals("1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectLong() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(0L, buf, pos);
        assertEquals("1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testCalendarTimeZoneForced() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", gmt);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(1970, Calendar.JANUARY, 1, 12, 0, 0); // 12:00 EST = 17:00 GMT
        // format will force GMT inside format(Calendar)
        String result = fmt.format(cal);
        assertEquals("1970-01-01 17:00:00", result);
    }

    @Test(timeout = 4000)
    public void testFormatWithMultipleFields() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("'Date:' yyyy/MM/dd 'Time:' HH:mm:ss.SSS", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(2009, Calendar.OCTOBER, 16, 16, 42, 16);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals("Date: 2009/10/16 Time: 16:42:16.000", fmt.format(cal));
    }

    // ----- Partition B: Boundary Value Analysis & Extremes -----

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNullPatternConstructor() {
        new FastDateFormat(null, TimeZone.getDefault(), Locale.getDefault());
    }

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        // parsePattern will return empty list, format will produce empty string
        FastDateFormat fmt = FastDateFormat.getInstance("", TimeZone.getDefault(), Locale.getDefault());
        assertEquals("", fmt.format(new Date()));
    }

    @Test(timeout = 4000)
    public void testSingleDigitNumbers() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        // UnpaddedNumberField for day of month (d) with padding=1
        FastDateFormat fmt = FastDateFormat.getInstance("d", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 5);
        assertEquals("5", fmt.format(cal));
        // day=15 -> "15"
        cal.set(Calendar.DAY_OF_MONTH, 15);
        assertEquals("15", fmt.format(cal));
        // day=123 -> "123" (but max 31? Use day of year with D up to 365)
        FastDateFormat fmtD = FastDateFormat.getInstance("D", tz);
        Calendar calD = Calendar.getInstance(tz);
        calD.set(1970, Calendar.DECEMBER, 31); // day of year 365
        assertEquals("365", fmtD.format(calD));
    }

    @Test(timeout = 4000)
    public void testTwoDigitNumbers() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        // TwoDigitNumberField for two-digit month (MM)
        FastDateFormat fmt = FastDateFormat.getInstance("MM", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.MARCH, 1); // month 2 (0-based)
        assertEquals("03", fmt.format(cal));
        // month December -> 12
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        assertEquals("12", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testPaddedNumberField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        // PaddedNumberField with padding=4 for year yyyy
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("1970", fmt.format(cal));
        // year 1 -> "0001"
        cal.set(Calendar.YEAR, 1);
        assertEquals("0001", fmt.format(cal));
        // year 999 -> "0999"
        cal.set(Calendar.YEAR, 999);
        assertEquals("0999", fmt.format(cal));
        // year 10000 -> "10000" (padding=4 but value larger, should just output digits)
        cal.set(Calendar.YEAR, 10000);
        assertEquals("10000", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testUnpaddedMonthField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("M", tz); // UnpaddedMonthField
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("1", fmt.format(cal));
        cal.set(Calendar.MONTH, Calendar.OCTOBER); // month=9 -> 10
        assertEquals("10", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitMonthField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("MM", tz); // TwoDigitMonthField
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("01", fmt.format(cal));
        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        assertEquals("11", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitYearField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("yy", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("70", fmt.format(cal));
        cal.set(Calendar.YEAR, 2009);
        assertEquals("09", fmt.format(cal));
        cal.set(Calendar.YEAR, 2000);
        assertEquals("00", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwelveHourField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("hh", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0); // midnight -> 12
        assertEquals("12", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 11);
        assertEquals("11", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("11", fmt.format(cal)); // 23 hour -> 11 (since HOUR is 11)
    }

    @Test(timeout = 4000)
    public void testTwentyFourHourField() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("kk", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0); // midnight -> 24
        assertEquals("24", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("01", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("23", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleShort() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("z", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        assertTrue(result.equals("GMT") || result.equals("UTC"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleLong() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fmt = FastDateFormat.getInstance("zzzz", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        assertNotNull(result);
        assertTrue(result.length() > 4);
        // should contain "Eastern Standard" or similar
        assertTrue(result.contains("Eastern") || result.contains("Time"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleNoColon() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("Z", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("+0000", fmt.format(cal));
        // negative offset
        tz = TimeZone.getTimeZone("America/New_York");
        fmt = FastDateFormat.getInstance("Z", tz);
        cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        String result = fmt.format(cal);
        assertTrue(result.startsWith("-05") || result.startsWith("-04")); // EST or EDT
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleWithColon() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("ZZ", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("+00:00", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testLiteralCharacters() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("'Today' yyyy", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("Today 1970", fmt.format(cal));
        // escaped single quote
        fmt = FastDateFormat.getInstance("''y''", tz);
        assertEquals("'1970'", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldEra() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("G", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("AD", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldDayOfWeekShort() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("E", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1); // Thursday
        assertEquals("Thu", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldDayOfWeekLong() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("EEEE", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1); // Thursday
        assertEquals("Thursday", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldAmPm() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("a", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("AM", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 13);
        assertEquals("PM", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfYear() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("D", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("1", fmt.format(cal));
        cal.set(Calendar.DAY_OF_YEAR, 365);
        assertEquals("365", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testWeekOfYear() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("w", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        // week of year 1
        assertTrue(Integer.parseInt(fmt.format(cal)) >= 1);
    }

    @Test(timeout = 4000)
    public void testWeekOfMonth() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("W", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1);
        assertEquals("1", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfWeekInMonth() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("F", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1); // first Thursday
        assertEquals("1", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testHourInAmPmZeroBased() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fmt = FastDateFormat.getInstance("KK", tz);
        Calendar cal = Calendar.getInstance(tz);
        cal.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("00", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 11);
        assertEquals("11", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("11", fmt.format(cal));
    }

    // ----- Partition C: Defect-Targeted Branch Zone (Lang538) -----

    @Test(timeout = 4000)
    public void testLang538() {
        // Pattern with 'Z' at end is not exactly "Z", but we need ISO8601 like "yyyy-MM-dd'T'HH:mm:ss.SSSZ"? 
        // Actually known bug: formatting a date with pattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" and UTC timezone
        // should produce string ending with Z (literal Z), but bug causes timezone offset to be inserted incorrectly.
        // The real test from defects4j uses: format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("UTC"));
        // Then format a Date that represents 2009-10-16T16:42:16.000Z.
        // Expected: "2009-10-16T16:42:16.000Z"
        // Buggy: "2009-10-16T08:42:16.000Z" (if local TZ is -8)
        TimeZone utc = TimeZone.getTimeZone("UTC");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", utc);
        // 1255704136000 is 2009-10-16T16:42:16.000Z? Let's compute: 2009-10-16 16:42:16 UTC = 1255704136000?
        // Actually 2009-10-16 16:42:16 UTC epoch seconds? Use Calendar to be safe.
        Calendar cal = Calendar.getInstance(utc);
        cal.set(2009, Calendar.OCTOBER, 16, 16, 42, 16);
        cal.set(Calendar.MILLISECOND, 0);
        long millis = cal.getTimeInMillis();
        String result = fmt.format(millis);
        assertEquals("2009-10-16T16:42:16.000Z", result);
        // Also format via Date object
        Date date = new Date(millis);
        assertEquals("2009-10-16T16:42:16.000Z", fmt.format(date));
        // Also via Calendar (should be same)
        assertEquals("2009-10-16T16:42:16.000Z", fmt.format(cal));
    }

    // ----- Partition D: Exception & Defensive Guard Paths -----

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIllegalPatternToken() {
        FastDateFormat.getInstance("#", TimeZone.getDefault(), Locale.getDefault());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatUnknownObject() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(new Object(), buf, pos);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatNullObject() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format(null, buf, pos);
    }

    @Test(timeout = 4000)
    public void testParseObjectUnsupported() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Object result = fmt.parseObject("1970", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testGetDateInstance() {
        FastDateFormat fmt = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        assertNotNull(fmt);
        String result = fmt.format(new Date(0L));
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testGetTimeInstance() {
        FastDateFormat fmt = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM);
        assertNotNull(fmt);
        String result = fmt.format(new Date(0L));
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testGetDateTimeInstance() {
        FastDateFormat fmt = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertNotNull(fmt);
        String result = fmt.format(new Date(0L));
        assertNotNull(result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateInstanceInvalidStyle() {
        // style that doesn't exist will cause ClassCastException in DateFormat -> rethrown
        FastDateFormat.getDateInstance(12345);
    }

    // ----- Partition E: Object Lifecycle & Contract Integrity -----

    @Test(timeout = 4000)
    public void testEquals() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertTrue(fmt1.equals(fmt2));
        assertTrue(fmt2.equals(fmt1));
        FastDateFormat fmt3 = FastDateFormat.getInstance("yyyy/MM/dd");
        assertFalse(fmt1.equals(fmt3));
        assertFalse(fmt1.equals(null));
        assertFalse(fmt1.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals(fmt1.hashCode(), fmt2.hashCode());
        FastDateFormat fmt3 = FastDateFormat.getInstance("yyyy/MM/dd");
        assertNotEquals(fmt1.hashCode(), fmt3.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("FastDateFormat[yyyy-MM-dd]", fmt.toString());
    }

    @Test(timeout = 4000)
    public void testGetters() {
        TimeZone tz = TimeZone.getTimeZone("PST");
        Locale locale = Locale.UK;
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd", tz, locale);
        assertEquals("yyyy-MM-dd", fmt.getPattern());
        assertEquals(tz, fmt.getTimeZone());
        assertTrue(fmt.getTimeZoneOverridesCalendar());
        assertEquals(locale, fmt.getLocale());
        assertTrue(fmt.getMaxLengthEstimate() > 0);
    }

    @Test(timeout = 4000)
    public void testGettersWithDefaults() {
        FastDateFormat fmt = FastDateFormat.getInstance();
        assertFalse(fmt.getTimeZoneOverridesCalendar()); // no timezone forced
        // locale forced? default constructor sets mLocaleForced=false because locale == null
        // but the package-private constructor sets mLocaleForced based on passed locale? In getInstance, it passes null for locale, so mLocaleForced=false.
        // However, we can't directly test mLocaleForced, but we can check that getLocale() returns default.
        assertEquals(Locale.getDefault(), fmt.getLocale());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT"), Locale.US);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(original.getPattern(), deserialized.getPattern());
        assertEquals(original.getTimeZone(), deserialized.getTimeZone());
        assertEquals(original.getLocale(), deserialized.getLocale());
        assertEquals(original.getMaxLengthEstimate(), deserialized.getMaxLengthEstimate());

        // Verify formatting works after deserialization
        Date date = new Date(0L);
        assertEquals(original.format(date), deserialized.format(date));
    }

    @Test(timeout = 4000)
    public void testCachingInstances() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertSame(fmt1, fmt2); // should be same cached instance
    }

    @Test(timeout = 4000)
    public void testDifferentTimeZones() {
        FastDateFormat gmt = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"));
        FastDateFormat pst = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("PST"));
        assertNotSame(gmt, pst);
        assertFalse(gmt.equals(pst));
        // Format same instant
        Date date = new Date(0L);
        assertEquals("1970-01-01", gmt.format(date));
        // PST is -8h, so date is 1969-12-31 in PST
        assertEquals("1969-12-31", pst.format(date));
    }

    @Test(timeout = 4000)
    public void testPairEquality() {
        // Indirectly test Pair via getDateInstance cache key construction
        FastDateFormat fmt1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);
        FastDateFormat fmt2 = FastDateFormat.getDateInstance(FastDateFormat.FULL, TimeZone.getTimeZone("GMT"), Locale.US);
        assertSame(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKey() {
        // Test caching of time zone display names
        TimeZone tz = TimeZone.getTimeZone("GMT");
        String display1 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.LONG, Locale.US);
        String display2 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.LONG, Locale.US);
        assertSame(display1, display2); // cached
    }

    @Test(timeout = 4000)
    public void testReadObjectReinitializes() throws Exception {
        // Create a FastDateFormat, serialize, modify transient fields (if possible), deserialize, check reinit.
        // Since transient fields are package-private, we can't easily corrupt them.
        // But we can ensure that deserialized instance works.
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fmt);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        // Verify mRules and mMaxLengthEstimate are initialized (formatting works)
        assertEquals(fmt.format(new Date(0L)), deserialized.format(new Date(0L)));
    }
}