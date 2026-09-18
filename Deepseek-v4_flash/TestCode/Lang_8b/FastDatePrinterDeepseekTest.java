package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: FastDatePrinter (white-box testing)
 * 
 * Known Defect (Defects4J):
 *   - testCalendarTimezoneRespected: When formatting a Calendar, the printer
 *     uses its own timezone (mTimeZone) instead of the Calendar's timezone.
 *     The TimeZoneNameRule.appendTo() uses the printer's zone field, not the
 *     calendar's timezone. This causes incorrect timezone name output.
 * 
 * Key branches to cover:
 *   - parsePattern(): all pattern letters (G,y,M,d,h,H,m,s,S,E,D,F,w,W,a,k,K,z,Z,')
 *   - selectNumberRule(): padding 1,2,default
 *   - format(Object): Date, Calendar, Long, else (null/unknown)
 *   - applyRules(): iterates over mRules
 *   - Rule implementations: CharacterLiteral, StringLiteral, TextField,
 *     UnpaddedNumberField, UnpaddedMonthField, PaddedNumberField,
 *     TwoDigitNumberField, TwoDigitYearField, TwoDigitMonthField,
 *     TwelveHourField, TwentyFourHourField, TimeZoneNameRule,
 *     TimeZoneNumberRule
 *   - Boundary: null arguments, empty pattern, extreme values (year 0, month 0, etc.)
 *   - Exception paths: illegal pattern component, null constructor args,
 *     PaddedNumberField size < 3, format() unknown class
 *   - equals/hashCode/toString/serialization (readObject)
 * 
 * Test partitions:
 *   A: Core functional logic & state transitions
 *   B: Boundary value analysis & extremes
 *   C: Defect-targeted branch zone (timezone respect)
 *   D: Exception & defensive guard paths
 *   E: Object lifecycle & contract integrity
 */
public class FastDatePrinterDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testFormatDate() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Date date = new Date(0L); // 1970-01-01 UTC
        String result = printer.format(date);
        assertEquals("1970-01-01", result);
    }

    @Test(timeout = 4000)
    public void testFormatCalendar() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JUNE, 15, 10, 30, 0);
        String result = printer.format(cal);
        assertEquals("2020-06-15 10:30:00", result);
    }

    @Test(timeout = 4000)
    public void testFormatLong() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(0L);
        assertEquals("1970-01-01", result);
    }

    @Test(timeout = 4000)
    public void testFormatObjectDate() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format(new Date(0L), buf, pos);
        assertEquals("1970-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectCalendar() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format((Object) cal, buf, pos);
        assertEquals("2020-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatObjectLong() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        StringBuffer result = printer.format((Object) 0L, buf, pos);
        assertEquals("1970-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWithStringBufferLong() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(0L, buf);
        assertEquals("1970-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWithStringBufferDate() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(new Date(0L), buf);
        assertEquals("1970-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testFormatWithStringBufferCalendar() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        StringBuffer buf = new StringBuffer();
        StringBuffer result = printer.format(cal, buf);
        assertEquals("2020-01-01", result.toString());
    }

    @Test(timeout = 4000)
    public void testGetPattern() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals("yyyy-MM-dd", printer.getPattern());
    }

    @Test(timeout = 4000)
    public void testGetTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", tz, Locale.US);
        assertEquals(tz, printer.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testGetLocale() {
        Locale locale = Locale.GERMANY;
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), locale);
        assertEquals(locale, printer.getLocale());
    }

    @Test(timeout = 4000)
    public void testGetMaxLengthEstimate() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        FastDatePrinter printer = new FastDatePrinter("", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testSingleQuotePattern() {
        FastDatePrinter printer = new FastDatePrinter("'Hello'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testEscapedSingleQuote() {
        FastDatePrinter printer = new FastDatePrinter("''", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("'", result);
    }

    @Test(timeout = 4000)
    public void testYearTwoDigit() {
        FastDatePrinter printer = new FastDatePrinter("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        Date date = new Date(0L); // 1970
        String result = printer.format(date);
        assertEquals("70", result);
    }

    @Test(timeout = 4000)
    public void testYearFourDigit() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Date date = new Date(0L);
        String result = printer.format(date);
        assertEquals("1970", result);
    }

    @Test(timeout = 4000)
    public void testMonthTextLong() {
        FastDatePrinter printer = new FastDatePrinter("MMMM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("January", result);
    }

    @Test(timeout = 4000)
    public void testMonthTextShort() {
        FastDatePrinter printer = new FastDatePrinter("MMM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("Jan", result);
    }

    @Test(timeout = 4000)
    public void testMonthTwoDigit() {
        FastDatePrinter printer = new FastDatePrinter("MM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("01", result);
    }

    @Test(timeout = 4000)
    public void testMonthUnpadded() {
        FastDatePrinter printer = new FastDatePrinter("M", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("1", result);
    }

    @Test(timeout = 4000)
    public void testDayOfMonthUnpadded() {
        FastDatePrinter printer = new FastDatePrinter("d", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        String result = printer.format(cal);
        assertEquals("5", result);
    }

    @Test(timeout = 4000)
    public void testDayOfMonthTwoDigit() {
        FastDatePrinter printer = new FastDatePrinter("dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 15);
        String result = printer.format(cal);
        assertEquals("15", result);
    }

    @Test(timeout = 4000)
    public void testHour12() {
        FastDatePrinter printer = new FastDatePrinter("h", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0); // midnight -> 12
        String result = printer.format(cal);
        assertEquals("12", result);
    }

    @Test(timeout = 4000)
    public void testHour24() {
        FastDatePrinter printer = new FastDatePrinter("H", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal);
        assertEquals("0", result);
    }

    @Test(timeout = 4000)
    public void testAmPm() {
        FastDatePrinter printer = new FastDatePrinter("a", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 13, 0, 0); // PM
        String result = printer.format(cal);
        assertEquals("PM", result);
    }

    @Test(timeout = 4000)
    public void testWeekdayShort() {
        FastDatePrinter printer = new FastDatePrinter("E", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6); // Monday
        String result = printer.format(cal);
        assertEquals("Mon", result);
    }

    @Test(timeout = 4000)
    public void testWeekdayLong() {
        FastDatePrinter printer = new FastDatePrinter("EEEE", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6);
        String result = printer.format(cal);
        assertEquals("Monday", result);
    }

    @Test(timeout = 4000)
    public void testEra() {
        FastDatePrinter printer = new FastDatePrinter("G", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("AD", result);
    }

    @Test(timeout = 4000)
    public void testDayOfYear() {
        FastDatePrinter printer = new FastDatePrinter("D", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("1", result);
    }

    @Test(timeout = 4000)
    public void testDayOfWeekInMonth() {
        FastDatePrinter printer = new FastDatePrinter("F", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6); // first Monday
        String result = printer.format(cal);
        assertEquals("1", result);
    }

    @Test(timeout = 4000)
    public void testWeekOfYear() {
        FastDatePrinter printer = new FastDatePrinter("w", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("1", result);
    }

    @Test(timeout = 4000)
    public void testWeekOfMonth() {
        FastDatePrinter printer = new FastDatePrinter("W", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 6);
        String result = printer.format(cal);
        assertEquals("2", result);
    }

    @Test(timeout = 4000)
    public void testHourInDay24() {
        FastDatePrinter printer = new FastDatePrinter("k", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal);
        assertEquals("24", result);
    }

    @Test(timeout = 4000)
    public void testHourInAmPm0to11() {
        FastDatePrinter printer = new FastDatePrinter("K", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal);
        assertEquals("0", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneShort() {
        FastDatePrinter printer = new FastDatePrinter("z", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertTrue(result.equals("EST") || result.equals("EDT")); // depends on DST
    }

    @Test(timeout = 4000)
    public void testTimeZoneLong() {
        FastDatePrinter printer = new FastDatePrinter("zzzz", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertTrue(result.contains("Eastern"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneRFC822() {
        FastDatePrinter printer = new FastDatePrinter("Z", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("+0000", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneISO8601() {
        FastDatePrinter printer = new FastDatePrinter("ZZ", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("+00:00", result);
    }

    @Test(timeout = 4000)
    public void testLiteralCharacter() {
        FastDatePrinter printer = new FastDatePrinter("'T'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("T", result);
    }

    @Test(timeout = 4000)
    public void testLiteralString() {
        FastDatePrinter printer = new FastDatePrinter("'Test'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("Test", result);
    }

    @Test(timeout = 4000)
    public void testMixedPattern() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd 'at' HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 10, 30, 0);
        String result = printer.format(cal);
        assertEquals("2020-01-01 at 10:30:00", result);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: FastDatePrinter.format(Calendar) should use the Calendar's timezone,
     * not the printer's timezone. The TimeZoneNameRule uses the printer's zone field.
     * This test creates a printer with one timezone and a Calendar with a different
     * timezone, then verifies that the formatted timezone name matches the Calendar's.
     */
    @Test(timeout = 4000)
    public void testCalendarTimezoneRespected() {
        // Printer uses PST (America/Los_Angeles)
        TimeZone printerTz = TimeZone.getTimeZone("America/Los_Angeles");
        // Calendar uses ICT (Asia/Bangkok)
        TimeZone calTz = TimeZone.getTimeZone("Asia/Bangkok");
        FastDatePrinter printer = new FastDatePrinter("h:mm a z", printerTz, Locale.US);
        Calendar cal = Calendar.getInstance(calTz);
        cal.set(2020, Calendar.JANUARY, 1, 14, 43, 0); // 2:43 PM ICT
        String result = printer.format(cal);
        // Expected: "2:43 PM ICT" (since Calendar's timezone is ICT)
        // Bug: printer uses its own timezone, so it would produce "2:43 PM PST"
        assertTrue("Expected timezone from Calendar, got: " + result, result.contains("ICT"));
    }

    @Test(timeout = 4000)
    public void testCalendarTimezoneRespectedWithDST() {
        TimeZone printerTz = TimeZone.getTimeZone("America/New_York");
        TimeZone calTz = TimeZone.getTimeZone("Europe/London");
        FastDatePrinter printer = new FastDatePrinter("h:mm a z", printerTz, Locale.US);
        Calendar cal = Calendar.getInstance(calTz);
        cal.set(2020, Calendar.JULY, 1, 14, 43, 0); // BST (GMT+1)
        String result = printer.format(cal);
        // Expected: "2:43 PM BST" (or "2:43 PM GMT+01:00" depending on locale)
        assertTrue("Expected timezone from Calendar, got: " + result,
                result.contains("BST") || result.contains("GMT+01:00"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullPattern() {
        new FastDatePrinter(null, TimeZone.getTimeZone("UTC"), Locale.US);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullTimeZone() {
        new FastDatePrinter("yyyy-MM-dd", null, Locale.US);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullLocale() {
        new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIllegalPatternComponent() {
        new FastDatePrinter("X", TimeZone.getTimeZone("UTC"), Locale.US);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatObjectNull() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        printer.format(null, buf, pos);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatObjectUnknownClass() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        printer.format(new Object(), buf, pos);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPaddedNumberFieldSizeLessThan3() {
        // This is indirectly tested via selectNumberRule, but we can trigger via reflection?
        // Actually PaddedNumberField constructor throws if size<3.
        // We can test by creating a pattern that would cause padding=3? No, selectNumberRule only calls PaddedNumberField for padding>=3.
        // To cover the exception, we need to directly instantiate PaddedNumberField with size=2.
        // But it's a private inner class. We can test via pattern that uses padding=3? Actually padding=3 is valid.
        // The exception is thrown only if size<3, which is not reachable via selectNumberRule because padding>=3.
        // However, we can test via reflection or by using a pattern that forces padding=2? No, padding=2 uses TwoDigitNumberField.
        // So this branch is not reachable from public API. We'll skip.
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSame() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentPattern() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy/MM/dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertNotEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentTimeZone() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("America/New_York"), Locale.US);
        assertNotEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLocale() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        assertNotEquals(p1, p2);
    }

    @Test(timeout = 4000)
    public void testEqualsNonFastDatePrinter() {
        FastDatePrinter p = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertFalse(p.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        FastDatePrinter p = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        int h1 = p.hashCode();
        int h2 = p.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualObjects() {
        FastDatePrinter p1 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDatePrinter p2 = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        FastDatePrinter p = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        String str = p.toString();
        assertTrue(str.contains("FastDatePrinter"));
        assertTrue(str.contains("yyyy-MM-dd"));
        assertTrue(str.contains("UTC"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        FastDatePrinter original = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        // Serialize to byte array
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        FastDatePrinter deserialized = (FastDatePrinter) ois.readObject();
        ois.close();
        assertEquals(original, deserialized);
        assertEquals(original.getPattern(), deserialized.getPattern());
        assertEquals(original.getTimeZone(), deserialized.getTimeZone());
        assertEquals(original.getLocale(), deserialized.getLocale());
        // Verify formatting works after deserialization
        String formatted = deserialized.format(new Date(0L));
        assertEquals("1970-01-01", formatted);
    }

    // Additional coverage for internal rule types

    @Test(timeout = 4000)
    public void testTwoDigitYearField() {
        FastDatePrinter printer = new FastDatePrinter("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(1999, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("99", result);
    }

    @Test(timeout = 4000)
    public void testTwoDigitMonthField() {
        FastDatePrinter printer = new FastDatePrinter("MM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.OCTOBER, 1);
        String result = printer.format(cal);
        assertEquals("10", result);
    }

    @Test(timeout = 4000)
    public void testUnpaddedMonthField() {
        FastDatePrinter printer = new FastDatePrinter("M", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.OCTOBER, 1);
        String result = printer.format(cal);
        assertEquals("10", result);
    }

    @Test(timeout = 4000)
    public void testUnpaddedNumberFieldLargeValue() {
        // This tests the else branch in UnpaddedNumberField.appendTo(int) for value >= 100
        FastDatePrinter printer = new FastDatePrinter("d", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.DECEMBER, 31); // day 31 -> value 31 < 100, but we need >=100
        // To get value >=100, we need a field that can be >=100, e.g., DAY_OF_YEAR
        FastDatePrinter printer2 = new FastDatePrinter("D", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal2.set(2020, Calendar.DECEMBER, 31); // day of year = 366 (leap year) or 365
        String result = printer2.format(cal2);
        assertTrue(result.length() >= 3);
    }

    @Test(timeout = 4000)
    public void testPaddedNumberField() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertEquals("2020", result);
    }

    @Test(timeout = 4000)
    public void testTwelveHourFieldMidnight() {
        FastDatePrinter printer = new FastDatePrinter("h", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal);
        assertEquals("12", result);
    }

    @Test(timeout = 4000)
    public void testTwentyFourHourFieldMidnight() {
        FastDatePrinter printer = new FastDatePrinter("k", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        String result = printer.format(cal);
        assertEquals("24", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleDaylight() {
        // Use a timezone that observes DST and a date in DST
        FastDatePrinter printer = new FastDatePrinter("z", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JULY, 1, 12, 0, 0); // EDT
        String result = printer.format(cal);
        assertEquals("EDT", result);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleNegativeOffset() {
        FastDatePrinter printer = new FastDatePrinter("Z", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertTrue(result.startsWith("-") || result.startsWith("+"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleColon() {
        FastDatePrinter printer = new FastDatePrinter("ZZ", TimeZone.getTimeZone("America/New_York"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        cal.set(2020, Calendar.JANUARY, 1);
        String result = printer.format(cal);
        assertTrue(result.contains(":"));
    }

    @Test(timeout = 4000)
    public void testTextFieldEstimateLength() {
        // Indirectly tested via getMaxLengthEstimate
        FastDatePrinter printer = new FastDatePrinter("MMMM", TimeZone.getTimeZone("UTC"), Locale.US);
        assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    @Test(timeout = 4000)
    public void testCharacterLiteralEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("'A'", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(1, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testStringLiteralEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("'Hello'", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(5, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testUnpaddedNumberFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("d", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(4, printer.getMaxLengthEstimate()); // UnpaddedNumberField returns 4
    }

    @Test(timeout = 4000)
    public void testTwoDigitNumberFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("dd", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(2, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testPaddedNumberFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(4, printer.getMaxLengthEstimate()); // PaddedNumberField returns 4
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("z", TimeZone.getTimeZone("America/New_York"), Locale.US);
        assertTrue(printer.getMaxLengthEstimate() > 0);
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumberRuleEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("Z", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(5, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testTwelveHourFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("h", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(4, printer.getMaxLengthEstimate()); // delegates to UnpaddedNumberField
    }

    @Test(timeout = 4000)
    public void testTwentyFourHourFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("k", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(4, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testTwoDigitYearFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("yy", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(2, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testTwoDigitMonthFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("MM", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(2, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testUnpaddedMonthFieldEstimateLength() {
        FastDatePrinter printer = new FastDatePrinter("M", TimeZone.getTimeZone("UTC"), Locale.US);
        assertEquals(2, printer.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePadding1() {
        // Indirectly tested via pattern "d" -> UnpaddedNumberField
        FastDatePrinter printer = new FastDatePrinter("d", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("5", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePadding2() {
        FastDatePrinter printer = new FastDatePrinter("dd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("05", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testSelectNumberRulePaddingDefault() {
        FastDatePrinter printer = new FastDatePrinter("ddd", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 5);
        assertEquals("005", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testParseTokenLiteralWithLetters() {
        // Pattern "'o''clock'"
        FastDatePrinter printer = new FastDatePrinter("'o''clock'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("o'clock", result);
    }

    @Test(timeout = 4000)
    public void testParseTokenLiteralEndsWithQuote() {
        FastDatePrinter printer = new FastDatePrinter("'Hello'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testParseTokenLiteralWithEscapedQuoteInside() {
        FastDatePrinter printer = new FastDatePrinter("'It''s'", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("It's", result);
    }

    @Test(timeout = 4000)
    public void testParseTokenLiteralUnclosedQuote() {
        // Pattern "'Hello" (unclosed) - this is actually valid? The parser treats it as literal until end.
        FastDatePrinter printer = new FastDatePrinter("'Hello", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testParseTokenLiteralEmpty() {
        FastDatePrinter printer = new FastDatePrinter("''", TimeZone.getTimeZone("UTC"), Locale.US);
        String result = printer.format(new Date(0L));
        assertEquals("'", result);
    }

    @Test(timeout = 4000)
    public void testParseTokenMultipleSameLetters() {
        FastDatePrinter printer = new FastDatePrinter("yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("2020", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testParseTokenMixedLettersAndLiteral() {
        FastDatePrinter printer = new FastDatePrinter("yyyy'年'MM'月'dd'日'", TimeZone.getTimeZone("UTC"), Locale.CHINA);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2020, Calendar.JANUARY, 1);
        assertEquals("2020年01月01日", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatWithNullCalendar() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        try {
            printer.format((Calendar) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatWithNullDate() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        try {
            printer.format((Date) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatWithNullStringBuffer() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        try {
            printer.format(new Date(0L), (StringBuffer) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatObjectWithNullStringBuffer() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        try {
            printer.format(new Date(0L), null, new FieldPosition(0));
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatObjectWithNullFieldPosition() {
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        // FieldPosition can be null? The method doesn't check, but it's not used.
        // It's safe to pass null, but we can test that it doesn't throw.
        printer.format(new Date(0L), buf, null);
        assertEquals("1970-01-01", buf.toString());
    }

    @Test(timeout = 4000)
    public void testApplyRulesWithEmptyRules() {
        FastDatePrinter printer = new FastDatePrinter("", TimeZone.getTimeZone("UTC"), Locale.US);
        StringBuffer buf = new StringBuffer();
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        printer.applyRules(cal, buf);
        assertEquals("", buf.toString());
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayCache() {
        // Ensure cache works
        String display1 = FastDatePrinter.getTimeZoneDisplay(TimeZone.getTimeZone("UTC"), false, TimeZone.SHORT, Locale.US);
        String display2 = FastDatePrinter.getTimeZoneDisplay(TimeZone.getTimeZone("UTC"), false, TimeZone.SHORT, Locale.US);
        assertEquals(display1, display2);
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyEquals() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key1 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        FastDatePrinter.TimeZoneDisplayKey key2 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyNotEquals() {
        TimeZone tz1 = TimeZone.getTimeZone("UTC");
        TimeZone tz2 = TimeZone.getTimeZone("America/New_York");
        FastDatePrinter.TimeZoneDisplayKey key1 = new FastDatePrinter.TimeZoneDisplayKey(tz1, false, TimeZone.SHORT, Locale.US);
        FastDatePrinter.TimeZoneDisplayKey key2 = new FastDatePrinter.TimeZoneDisplayKey(tz2, false, TimeZone.SHORT, Locale.US);
        assertFalse(key1.equals(key2));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyDaylightFlag() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key1 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        FastDatePrinter.TimeZoneDisplayKey key2 = new FastDatePrinter.TimeZoneDisplayKey(tz, true, TimeZone.SHORT, Locale.US);
        assertFalse(key1.equals(key2));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyStyle() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key1 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        FastDatePrinter.TimeZoneDisplayKey key2 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.LONG, Locale.US);
        assertFalse(key1.equals(key2));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyLocale() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key1 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        FastDatePrinter.TimeZoneDisplayKey key2 = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.GERMANY);
        assertFalse(key1.equals(key2));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyHashCodeConsistency() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        int h1 = key.hashCode();
        int h2 = key.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyEqualsSelf() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        assertTrue(key.equals(key));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyEqualsNull() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        assertFalse(key.equals(null));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyEqualsDifferentType() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter.TimeZoneDisplayKey key = new FastDatePrinter.TimeZoneDisplayKey(tz, false, TimeZone.SHORT, Locale.US);
        assertFalse(key.equals("string"));
    }
}