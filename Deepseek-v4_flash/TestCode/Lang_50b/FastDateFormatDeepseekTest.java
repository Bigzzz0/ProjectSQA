package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: FastDateFormat (Defects4J bug: caching of date/time instances ignores default locale changes)
 * 
 * Decision branches covered:
 * - Constructor: pattern null check, timeZone null -> default, locale null -> default
 * - getInstance: cache hit/miss, init() called on new instances
 * - getDateInstance/getTimeInstance/getDateTimeInstance: cache key construction with/without timeZone/locale
 * - parsePattern: all pattern letters (G,y,M,d,h,H,m,s,S,E,D,F,w,W,a,k,K,z,Z,')
 * - format(Object): type dispatch (Date, Calendar, Long, else)
 * - format(Calendar): timeZoneForced branch
 * - applyRules: loop over rules
 * - parseObject: always returns null
 * - equals/hashCode: field comparisons
 * - TimeZoneNameRule: forced vs non-forced, daylight vs standard
 * - TimeZoneNumberRule: colon vs no-colon, negative offset
 * - PaddedNumberField: value < 100, value >= 100, negative guard
 * - TwelveHourField: hour==0 -> use getLeastMaximum+1
 * - TwentyFourHourField: hour==0 -> use getMaximum+1
 * 
 * Boundary conditions:
 * - Null pattern -> IllegalArgumentException
 * - Null timeZone/locale -> default
 * - Pattern with single quotes, escaped quotes
 * - Token length 1,2,3,4+ for various fields
 * - Calendar fields at extremes (0, max, negative? not possible)
 * - TimeZone offset negative, zero, positive
 * - DST offset non-zero
 * 
 * Defect-targeted branch:
 * - getDateInstance/getDateTimeInstance when locale is null (default) and default locale changes
 *   -> cache key does not include locale, so old instance with wrong locale is returned.
 *   Test: set default locale to de_DE, get instance, assert locale is de_DE.
 */
public class FastDateFormatDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetInstanceDefault() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        assertNotNull("Instance should not be null", fdf);
        assertEquals("Pattern should be default", new SimpleDateFormat().toPattern(), fdf.getPattern());
        assertEquals("TimeZone should be default", TimeZone.getDefault(), fdf.getTimeZone());
        assertEquals("Locale should be default", Locale.getDefault(), fdf.getLocale());
        assertFalse("TimeZoneForced should be false", fdf.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPattern() {
        String pattern = "yyyy-MM-dd";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern);
        assertEquals("Pattern should match", pattern, fdf.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPatternAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:00");
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm:ss", tz);
        assertEquals("TimeZone should be set", tz, fdf.getTimeZone());
        assertTrue("TimeZoneForced should be true", fdf.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPatternAndLocale() {
        Locale locale = Locale.GERMANY;
        FastDateFormat fdf = FastDateFormat.getInstance("dd.MM.yyyy", locale);
        assertEquals("Locale should be set", locale, fdf.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithAllParams() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale locale = Locale.FRANCE;
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE, MMMM d, yyyy", tz, locale);
        assertEquals(tz, fdf.getTimeZone());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertEquals(locale, fdf.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetDateInstanceDefault() {
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertNotNull(fdf);
        // Pattern should be a short date pattern for default locale
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT);
        assertEquals("Pattern should match SimpleDateFormat short date", sdf.toPattern(), fdf.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetDateInstanceWithStyleAndLocale() {
        Locale locale = Locale.UK;
        FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.LONG, locale);
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.LONG, locale);
        assertEquals(sdf.toPattern(), fdf.getPattern());
        assertEquals(locale, fdf.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetTimeInstanceDefault() {
        FastDateFormat fdf = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM);
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.MEDIUM);
        assertEquals(sdf.toPattern(), fdf.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeInstanceDefault() {
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        assertEquals(sdf.toPattern(), fdf.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeInstanceWithTimeZoneAndLocale() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale locale = Locale.JAPAN;
        FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, locale);
        SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale);
        assertEquals(sdf.toPattern(), fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertEquals(locale, fdf.getLocale());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNullPattern() {
        new FastDateFormat(null, null, null);
    }

    @Test(timeout = 4000)
    public void testFormatDateObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        Date date = new Date(0); // 1970-01-01 00:00:00 GMT
        String result = fdf.format(date);
        // Depending on timezone, but default is system timezone. Use a fixed timezone for deterministic test.
        FastDateFormat fdfUtc = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        assertEquals("1970-01-01", fdfUtc.format(date));
    }

    @Test(timeout = 4000)
    public void testFormatCalendarObject() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"));
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        String result = fdf.format(cal);
        assertEquals("2020-06-15 10:30:45", result);
    }

    @Test(timeout = 4000)
    public void testFormatLongMillis() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        long millis = 86400000L; // 1970-01-02 UTC
        assertEquals("1970-01-02", fdf.format(millis));
    }

    @Test(timeout = 4000)
    public void testFormatWithStringBuffer() {
        FastDateFormat fdf = FastDateFormat.getInstance("HH:mm", TimeZone.getTimeZone("UTC"));
        StringBuffer sb = new StringBuffer();
        fdf.format(new Date(0), sb, new FieldPosition(0));
        assertEquals("00:00", sb.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWithCalendarAndForcedTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm", tz);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        // Forced timezone should override calendar's timezone
        String result = fdf.format(cal);
        // New York is UTC-5 in standard time (January)
        assertEquals("2019-12-31 19:00", result);
    }

    @Test(timeout = 4000)
    public void testFormatObjectInvalidType() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        try {
            fdf.format(new Object(), new StringBuffer(), new FieldPosition(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseObjectReturnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        Object result = fdf.parseObject("any", pos);
        assertNull("parseObject should return null", result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testGetMaxLengthEstimate() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        assertTrue("Max length estimate should be positive", fdf.getMaxLengthEstimate() > 0);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT"), Locale.US);
        assertEquals("Equal instances should be equal", fdf1, fdf2);
        assertEquals("Equal instances should have same hash", fdf1.hashCode(), fdf2.hashCode());
        assertNotEquals("Different timezone should not be equal", fdf1, fdf3);
    }

    @Test(timeout = 4000)
    public void testToString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        assertEquals("FastDateFormat[yyyy]", fdf.toString());
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Locale caching bug)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testChangeDefaultLocaleDateInstance() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
            // The bug: cached instance may have been created with original locale (en_US)
            // Expected: locale should be de_DE
            assertEquals("Locale should be the new default (de_DE)", Locale.GERMANY, fdf.getLocale());
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test(timeout = 4000)
    public void testChangeDefaultLocaleDateTimeInstance() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat fdf = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
            assertEquals("Locale should be the new default (de_DE)", Locale.GERMANY, fdf.getLocale());
        } finally {
            Locale.setDefault(original);
        }
    }

    // Additional test to ensure cache is not polluted by previous test
    @Test(timeout = 4000)
    public void testChangeDefaultLocaleDateInstanceAfterCache() {
        // First call with default locale (en_US) to populate cache
        FastDateFormat.getInstance("dummy"); // just to ensure cache is warm
        Locale original = Locale.getDefault();
        try {
            // Change default to German
            Locale.setDefault(Locale.GERMANY);
            FastDateFormat fdf = FastDateFormat.getDateInstance(FastDateFormat.LONG);
            assertEquals("Locale should be de_DE even after cache", Locale.GERMANY, fdf.getLocale());
        } finally {
            Locale.setDefault(original);
        }
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidPattern() {
        FastDateFormat.getInstance("Invalid pattern with @");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetDateInstanceInvalidStyle() {
        // Style must be FULL, LONG, MEDIUM, SHORT; using 0 may cause ClassCastException in some locales
        // but the method catches ClassCastException and throws IllegalArgumentException
        FastDateFormat.getDateInstance(0);
    }

    @Test(timeout = 4000)
    public void testGetTimeZoneDisplayCache() {
        // Indirectly test the static cache method
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        String display = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.SHORT, Locale.US);
        assertNotNull("Display name should not be null", display);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializationEquivalent() throws Exception {
        // FastDateFormat is Serializable; we can test that deserialization re-initializes rules
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        // Serialize to byte array
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();
        assertEquals("Deserialized instance should be equal", original, deserialized);
        // Ensure formatting works after deserialization
        Date date = new Date(0);
        assertEquals(original.format(date), deserialized.format(date));
    }

    // -----------------------------------------------------------------------
    // Additional coverage for pattern parsing branches
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPatternWithAllLetters() {
        // This pattern includes many different letters to exercise parsePattern switch
        String pattern = "GG yyyy MM dd HH mm ss SSS E D F w W a k K z Z 'text' ''";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        String result = fdf.format(cal);
        assertNotNull("Formatted string should not be null", result);
        assertTrue("Result should contain 'text'", result.contains("text"));
    }

    @Test(timeout = 4000)
    public void testTwelveHourFieldMidnight() {
        // h pattern: hour 0 should become 12
        FastDateFormat fdf = FastDateFormat.getInstance("hh:mm a", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0); // midnight
        String result = fdf.format(cal);
        assertEquals("12:00 AM", result);
    }

    @Test(timeout = 4000)
    public void testTwentyFourHourFieldMidnight() {
        // k pattern: hour 0 should become 24
        FastDateFormat fdf = FastDateFormat.getInstance("kk:mm", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = fdf.format(cal);
        assertEquals("24:00", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleNegativeOffset() {
        // Use a timezone with negative offset (e.g., US/Eastern)
        FastDateFormat fdf = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        String result = fdf.format(cal);
        // Standard time offset is -0500
        assertTrue("Should start with '-'", result.startsWith("-"));
        assertEquals("-0500", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleWithColon() {
        FastDateFormat fdf = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("Europe/London"), Locale.UK);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("Europe/London"));
        cal.set(2020, Calendar.JUNE, 1, 12, 0, 0); // BST (UTC+1)
        String result = fdf.format(cal);
        // Should be +01:00
        assertTrue("Should start with '+'", result.startsWith("+"));
        assertEquals("+01:00", result);
    }

    @Test(timeout = 4000)
    public void testPaddedNumberFieldLargeValue() {
        // Use pattern with 4-digit year (already covered) but also test padding >2
        FastDateFormat fdf = FastDateFormat.getInstance("SSSS", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.MILLISECOND, 999);
        String result = fdf.format(cal);
        assertEquals("0999", result);
    }

    @Test(timeout = 4000)
    public void testUnpaddedMonthField() {
        FastDateFormat fdf = FastDateFormat.getInstance("M", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.OCTOBER, 1);
        assertEquals("10", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitMonthField() {
        FastDateFormat fdf = FastDateFormat.getInstance("MM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.MARCH, 1);
        assertEquals("03", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitYearField() {
        FastDateFormat fdf = FastDateFormat.getInstance("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("20", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldEra() {
        FastDateFormat fdf = FastDateFormat.getInstance("G", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("AD", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldDayOfWeekShort() {
        FastDateFormat fdf = FastDateFormat.getInstance("E", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6); // Monday
        assertEquals("Mon", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTextFieldDayOfWeekLong() {
        FastDateFormat fdf = FastDateFormat.getInstance("EEEE", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6);
        assertEquals("Monday", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleForcedDaylight() {
        // Use a timezone that observes DST and force it
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fdf = FastDateFormat.getInstance("z", tz, Locale.US);
        Calendar cal = new GregorianCalendar(tz);
        cal.set(2020, Calendar.JUNE, 1, 12, 0, 0); // DST
        String result = fdf.format(cal);
        // Forced timezone, so it should use the forced timezone's display name
        // Since mTimeZoneForced is true, it will use mStandard or mDaylight based on DST_OFFSET
        assertEquals("EDT", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleNotForced() {
        // Not forced: use calendar's timezone
        FastDateFormat fdf = FastDateFormat.getInstance("zzzz", null, Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1, 12, 0, 0);
        String result = fdf.format(cal);
        // Should use calendar's timezone display name (Eastern Standard Time)
        assertEquals("Eastern Standard Time", result);
    }

    @Test(timeout = 4000)
    public void testCharacterLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("'T'", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar();
        assertEquals("T", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testStringLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("'Hello'", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar();
        assertEquals("Hello", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testEscapedQuoteInLiteral() {
        FastDateFormat fdf = FastDateFormat.getInstance("''''", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar();
        assertEquals("'", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePadding1() {
        // This is indirectly tested via pattern "d" (day of month unpadded)
        FastDateFormat fdf = FastDateFormat.getInstance("d", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("5", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePadding2() {
        FastDateFormat fdf = FastDateFormat.getInstance("dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("05", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePadding3() {
        FastDateFormat fdf = FastDateFormat.getInstance("ddd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("005", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatWithNullObject() {
        FastDateFormat fdf = FastDateFormat.getInstance();
        try {
            fdf.format((Object) null, new StringBuffer(), new FieldPosition(0));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("<null>"));
        }
    }
}