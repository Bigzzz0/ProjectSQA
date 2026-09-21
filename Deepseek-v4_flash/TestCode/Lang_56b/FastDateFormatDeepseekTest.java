package org.apache.commons.lang.time;

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

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Coverage targets:
 * - Factory methods: getInstance(), getInstance(String), getInstance(String,TimeZone), getInstance(String,Locale),
 *   getInstance(String,TimeZone,Locale), getDateInstance(int,...), getTimeInstance(int,...), getDateTimeInstance(int,int,...)
 * - Constructor: null pattern -> IllegalArgumentException
 * - parsePattern(): token types G,y,M,d,h,H,m,s,S,E,D,F,w,W,a,k,K,z,Z, literal, default -> IllegalArgumentException
 * - parseToken(): letter run, quoted text, escaped quote
 * - selectNumberField(): padding 1,2,3+ -> rule types
 * - format(Object): Date, Calendar, Long, else -> IllegalArgumentException
 * - format(long), format(Date), format(Calendar), format(long,StringBuffer), format(Date,StringBuffer), format(Calendar,StringBuffer)
 * - applyRules(): loop over rules
 * - parseObject(): returns null, sets error index
 * - getters: getPattern(), getTimeZone(), getTimeZoneOverridesCalendar(), getLocale(), getMaxLengthEstimate()
 * - equals(): null, wrong type, equal, not equal (pattern diff, tz diff, locale diff, forced flags diff)
 * - hashCode(): consistent with equals
 * - toString(): pattern in brackets
 * - Serialization: (DEFECT) NotSerializableException due to non-serializable inner rules (PaddedNumberField, etc.)
 * - Boundary values: pattern lengths, month/day/hour boundaries (0, 12, 24), year extremes, time zone offsets, DST
 * - Exception paths: invalid pattern, null, illegal style, non-Date/Calendar/Long object
 * 
 * Known defect: FastDateFormat is Serializable but its inner Rule classes (e.g. PaddedNumberField) are not,
 * causing NotSerializableException upon serialization.
 * 
 * Test design:
 * - Partition A: Core functional use (format with various patterns, object types).
 * - Partition B: Boundary and edge cases (null, empty, extremes of numbers as strings).
 * - Partition C: Defect-targeted serialization test (pattern with PaddedNumberField) and general serialization test.
 * - Partition D: Exception/guards (null pattern, illegal pattern, invalid object type).
 * - Partition E: Object contract (equals, hashCode, toString, getters).
 */
public class FastDateFormatDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testGetInstanceDefault() {
        FastDateFormat fmt = FastDateFormat.getInstance();
        assertNotNull(fmt);
        assertEquals(new java.text.SimpleDateFormat().toPattern(), fmt.getPattern());
        assertNotNull(fmt.getTimeZone());
        assertEquals(Locale.getDefault(), fmt.getLocale());
        assertFalse(fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPattern() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        assertEquals("yyyy-MM-dd", fmt.getPattern());
        assertNotNull(fmt.format(new Date()));
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPatternAndTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm:ss", tz);
        assertTrue(fmt.getTimeZoneOverridesCalendar());
        assertEquals(tz, fmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetInstanceWithPatternAndLocale() {
        Locale locale = Locale.FRANCE;
        FastDateFormat fmt = FastDateFormat.getInstance("dd MMM yyyy", locale);
        assertEquals(locale, fmt.getLocale());
        assertTrue(fmt.getLocale().toString().startsWith("fr")); // locale forced
    }

    @Test(timeout = 4000)
    public void testGetDateInstance() {
        FastDateFormat fmt = FastDateFormat.getDateInstance(FastDateFormat.LONG);
        assertNotNull(fmt);
        assertFalse(fmt.getPattern().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetTimeInstance() {
        FastDateFormat fmt = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM);
        assertNotNull(fmt);
        assertFalse(fmt.getPattern().isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetDateTimeInstance() {
        FastDateFormat fmt = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT);
        assertNotNull(fmt);
        assertFalse(fmt.getPattern().isEmpty());
    }

    @Test(timeout = 4000)
    public void testFormatDate() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd");
        Date now = new Date();
        String result = fmt.format(now);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test(timeout = 4000)
    public void testFormatCalendar() {
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String result = fmt.format(cal);
        assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test(timeout = 4000)
    public void testFormatLong() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        String result = fmt.format(0L);
        assertEquals("1970", result);
    }

    @Test(timeout = 4000)
    public void testFormatWithStringBuffer() {
        FastDateFormat fmt = FastDateFormat.getInstance("'Date:' yyyy");
        StringBuffer buf = new StringBuffer();
        StringBuffer result = fmt.format(new Date(), buf);
        assertSame(buf, result);
        assertTrue(result.toString().startsWith("Date:"));
    }

    @Test(timeout = 4000)
    public void testFormatObjectDate() {
        FastDateFormat fmt = FastDateFormat.getInstance("MM/dd");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = fmt.format(new Date(), buf, pos);
        assertSame(buf, result);
        assertTrue(result.toString().matches("\\d{2}/\\d{2}"));
    }

    @Test(timeout = 4000)
    public void testFormatObjectCalendar() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        Calendar cal = Calendar.getInstance();
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = fmt.format((Object) cal, buf, pos);
        assertSame(buf, result);
        assertEquals(String.valueOf(cal.get(Calendar.YEAR)), result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectLong() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = fmt.format((Object) 0L, buf, pos);
        assertEquals("1970", result.toString());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testFormatNullObject() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        try {
            fmt.format((Object) null, buf, pos);
            fail("Expected IllegalArgumentException for null");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("null"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatInvalidObjectClass() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        try {
            fmt.format((Object) "string", buf, pos);
            fail("Expected IllegalArgumentException for non-date object");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Unknown class"));
        }
    }

    @Test(timeout = 4000)
    public void testParseObjectReturnsNull() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(0);
        Object result = fmt.parseObject("1970", pos);
        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testFormatYearUnpadded() {
        // Pattern "y" -> UnpaddedNumberField with Calendar.YEAR
        FastDateFormat fmt = FastDateFormat.getInstance("y");
        // Year 10 -> "10"
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 10);
        assertEquals("10", fmt.format(cal));
        // Year 100 -> "100"
        cal.set(Calendar.YEAR, 100);
        assertEquals("100", fmt.format(cal));
        // Year 9999 -> "9999"
        cal.set(Calendar.YEAR, 9999);
        assertEquals("9999", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatTwoDigitYear() {
        FastDateFormat fmt = FastDateFormat.getInstance("yy");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2000);
        assertEquals("00", fmt.format(cal));
        cal.set(Calendar.YEAR, 1999);
        assertEquals("99", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatMonthBoundaries() {
        // MMM -> short month, MMMM -> full month
        FastDateFormat fmtShort = FastDateFormat.getInstance("MMM", Locale.US);
        FastDateFormat fmtLong = FastDateFormat.getInstance("MMMM", Locale.US);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals("Jan", fmtShort.format(cal));
        assertEquals("January", fmtLong.format(cal));
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        assertEquals("Dec", fmtShort.format(cal));
        assertEquals("December", fmtLong.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatUnpaddedMonth() {
        FastDateFormat fmt = FastDateFormat.getInstance("M");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals("1", fmt.format(cal));
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        assertEquals("10", fmt.format(cal));
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        assertEquals("12", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatTwoDigitMonth() {
        FastDateFormat fmt = FastDateFormat.getInstance("MM");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        assertEquals("01", fmt.format(cal));
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        assertEquals("12", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatTwelveHour() {
        // Pattern "h" -> TwelveHourField
        FastDateFormat fmt = FastDateFormat.getInstance("h");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // midnight -> 12
        assertEquals("12", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 12); // noon -> 12
        assertEquals("12", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("1", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("11", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatTwentyFourHour() {
        // Pattern "k" -> TwentyFourHourField
        FastDateFormat fmt = FastDateFormat.getInstance("k");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // midnight -> 24
        assertEquals("24", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("1", fmt.format(cal));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("23", fmt.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatTimeZoneNumberNoColon() {
        FastDateFormat fmt = FastDateFormat.getInstance("Z");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        String result = fmt.format(cal);
        assertTrue(result.matches("[+-]\\d{4}"));
    }

    @Test(timeout = 4000)
    public void testFormatTimeZoneNumberColon() {
        FastDateFormat fmt = FastDateFormat.getInstance("ZZ");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT-08:00"));
        String result = fmt.format(cal);
        assertTrue(result.matches("[+-]\\d{2}:\\d{2}"));
    }

    @Test(timeout = 4000)
    public void testFormatLiteralCharacters() {
        FastDateFormat fmt = FastDateFormat.getInstance("'hello' yyyy");
        assertEquals("hello 1970", fmt.format(0L));
    }

    @Test(timeout = 4000)
    public void testFormatEscapedQuote() {
        FastDateFormat fmt = FastDateFormat.getInstance("''''");
        assertEquals("'", fmt.format(new Date()));
    }

    // ========== Partition C: Defect-Targeted Branch Zone (Serialization Bug) ==========

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        // This test will expose the NotSerializableException on buggy versions.
        // Use a pattern that involves PaddedNumberField (padding >= 3).
        // Pattern "yyyy" -> PaddedNumberField with size 4.
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fmt);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        // Assert equality of original and deserialized
        assertEquals(fmt, deserialized);
        assertEquals(fmt.getPattern(), deserialized.getPattern());
        assertEquals(fmt.getTimeZone(), deserialized.getTimeZone());
        assertEquals(fmt.getMaxLengthEstimate(), deserialized.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testSerializationWithAllRuleTypes() throws Exception {
        // Use a pattern that exercises multiple rule types including PaddedNumberField and others.
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS Z");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(fmt);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(fmt, deserialized);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetInstanceNullPattern() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullPattern() {
        new FastDateFormat(null, null, null);  // protected, but accessible within same package
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetDateInstanceInvalidStyle() {
        FastDateFormat.getDateInstance(9999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetTimeInstanceInvalidStyle() {
        FastDateFormat.getTimeInstance(9999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIllegalPatternComponent() {
        FastDateFormat.getInstance("x");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatInvalidObjectClassNoOverload() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        fmt.format((Object) new Object(), buf, pos);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy");
        assertTrue(fmt1.equals(fmt1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        assertFalse(fmt.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualObjects() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.UK);
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.UK);
        assertEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPattern() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy");
        FastDateFormat fmt2 = FastDateFormat.getInstance("yy");
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTimeZone() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("UTC"));
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy", TimeZone.getTimeZone("GMT"));
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLocale() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy", Locale.US);
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy", Locale.FRANCE);
        assertNotEquals(fmt1, fmt2);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistentWithEquals() {
        FastDateFormat fmt1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.UK);
        FastDateFormat fmt2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.UK);
        assertEquals(fmt1.hashCode(), fmt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        FastDateFormat fmt = FastDateFormat.getInstance("MM/dd/yyyy");
        assertEquals("FastDateFormat[MM/dd/yyyy]", fmt.toString());
    }

    @Test(timeout = 4000)
    public void testGetPattern() {
        FastDateFormat fmt = FastDateFormat.getInstance("HH:mm");
        assertEquals("HH:mm", fmt.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy", tz);
        assertEquals(tz, fmt.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetTimeZoneOverridesCalendarFalse() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        assertFalse(fmt.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testGetLocale() {
        Locale locale = Locale.GERMANY;
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy", locale);
        assertEquals(locale, fmt.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetMaxLengthEstimate() {
        FastDateFormat fmt = FastDateFormat.getInstance("yyyy");
        assertTrue(fmt.getMaxLengthEstimate() > 0);
    }
}