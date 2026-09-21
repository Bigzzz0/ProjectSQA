package org.apache.commons.lang3.time;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect (LANG-538):
 *    - FastDateFormat.format(Calendar) must honor the formatter's forced time zone (mTimeZoneForced).
 *    - In the defective state, passing a Calendar with a differing time zone (e.g., GMT-8) to a formatter
 *      configured with GMT outputted raw calendar fields (e.g., 08:42:16) instead of adjusting to GMT (16:42:16).
 *    - Targeted by: testLang538(), testLang538BufferVariant()
 *
 * 2. Equivalence Partitions & Decision Logic:
 *    - Pattern Token Parsing (parsePattern / parseToken):
 *      - Tokens: 'G', 'y', 'yy', 'yyyy', 'M', 'MM', 'MMM', 'MMMM', 'd', 'dd', 'ddd', 'h', 'hh', 'H', 'HH',
 *                'm', 'mm', 's', 'ss', 'S', 'SS', 'SSS', 'SSSS', 'E', 'EEEE', 'D', 'DDD', 'F', 'w', 'ww',
 *                'W', 'a', 'k', 'kk', 'K', 'KK', 'z', 'zzzz', 'Z', 'ZZ'
 *      - Literals: Single quote escaping (''), single char ('c'), multi-char ('literal'), unquoted delimiters.
 *      - Invalid token: Character not in SimpleDateFormat spec throws IllegalArgumentException.
 *      - Null pattern: Throws IllegalArgumentException.
 *    - Numeric Rules (PaddedNumberField, UnpaddedNumberField, TwoDigitNumberField, UnpaddedMonthField):
 *      - Values < 10, 10..99, 100..999, >= 1000.
 *      - Custom padding size boundaries.
 *    - Hour Fields (TwelveHourField, TwentyFourHourField):
 *      - Boundary hours: Midnight (0 -> 12 for 12-hour, 0 -> 24 for 24-hour), Noon (12), 1..11, 13..23.
 *    - TimeZone Rules (TimeZoneNameRule, TimeZoneNumberRule):
 *      - Forced vs unforced time zone.
 *      - Standard time vs Daylight Saving Time (DST).
 *      - Positive, negative, and zero UTC offsets.
 *      - Colon vs non-colon notation ('Z' vs 'ZZ').
 *    - Factory & Caching Logic:
 *      - getInstance (default, with pattern, timeZone, locale variations).
 *      - getDateInstance, getTimeInstance, getDateTimeInstance across styles (FULL, LONG, MEDIUM, SHORT)
 *        and cache hit/miss verifications.
 *      - Invalid date/time styles throwing IllegalArgumentException.
 *    - Formatter Polymorphism:
 *      - format(Object, StringBuffer, FieldPosition) on Date, Calendar, Long, invalid type (throws IAE), null (throws IAE).
 *      - format(long), format(Date), format(Calendar) with and without target StringBuffer.
 *    - Parsing Contract:
 *      - parseObject(String, ParsePosition) returns null and resets index/errorIndex to 0.
 *    - Contract & Lifecycle:
 *      - equals, hashCode, toString, and Java Object Serialization/Deserialization.
 */

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
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

public class FastDateFormatGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-538)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang538() {
        final String dateTime = "2009-10-16T16:42:16.000Z";
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        assertEquals("dateTime", dateTime, format.format(cal));
    }

    @Test(timeout = 4000)
    public void testLang538BufferVariant() {
        final String dateTime = "2009-10-16T16:42:16.000Z";
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT-8"));
        cal.clear();
        cal.set(2009, 9, 16, 8, 42, 16);

        FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", TimeZone.getTimeZone("GMT"));
        StringBuffer buffer = new StringBuffer();
        format.format(cal, buffer);
        assertEquals("dateTime", dateTime, buffer.toString());

        FieldPosition pos = new FieldPosition(0);
        StringBuffer objBuffer = new StringBuffer();
        format.format((Object) cal, objBuffer, pos);
        assertEquals("dateTime", dateTime, objBuffer.toString());
    }

    // =========================================================================
    // Partition A: Core Pattern Parsing, Rules & Formatting Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllStandardPatternTokens() {
        String pattern = "G y yy yyyy M MM MMM MMMM d dd ddd h hh H HH m mm s ss S SS SSS SSSS "
                       + "E EEEE D DDD F w ww W a k kk K KK z zzzz Z ZZ";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, TimeZone.getTimeZone("UTC"), Locale.US);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 5, 4, 6, 7);
        cal.set(Calendar.MILLISECOND, 89);

        String formatted = fdf.format(cal);
        assertNotNull(formatted);
        assertTrue(formatted.contains("AD"));
        assertTrue(formatted.contains("2023"));
        assertTrue(formatted.contains("23"));
        assertTrue(formatted.contains("Mar"));
        assertTrue(formatted.contains("March"));
        assertTrue(formatted.contains("Sun"));
        assertTrue(formatted.contains("Sunday"));
        assertTrue(formatted.contains("AM"));
        assertTrue(formatted.contains("+0000"));
        assertTrue(formatted.contains("+00:00"));
    }

    @Test(timeout = 4000)
    public void testLiteralTextAndQuotes() {
        FastDateFormat fdf = FastDateFormat.getInstance("'' 'Year:' yyyy 'O''clock' '!'", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);

        String result = fdf.format(cal);
        assertEquals("' Year: 2023 O'clock !", result);
    }

    @Test(timeout = 4000)
    public void testHourBoundariesTwelveAndTwentyFour() {
        FastDateFormat fdf12 = FastDateFormat.getInstance("h hh", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf24 = FastDateFormat.getInstance("k kk", TimeZone.getTimeZone("UTC"), Locale.US);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // Midnight

        assertEquals("12 12", fdf12.format(cal));
        assertEquals("24 24", fdf24.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12); // Noon
        assertEquals("12 12", fdf12.format(cal));
        assertEquals("12 12", fdf24.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 1);
        assertEquals("1 01", fdf12.format(cal));
        assertEquals("1 01", fdf24.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 13);
        assertEquals("1 01", fdf12.format(cal));
        assertEquals("13 13", fdf24.format(cal));
    }

    @Test(timeout = 4000)
    public void testHourBoundariesZeroToTwentyThreeAndZeroToEleven() {
        FastDateFormat fdfH = FastDateFormat.getInstance("H HH", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfK = FastDateFormat.getInstance("K KK", TimeZone.getTimeZone("UTC"), Locale.US);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0); // Midnight

        assertEquals("0 00", fdfH.format(cal));
        assertEquals("0 00", fdfK.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12); // Noon
        assertEquals("12 12", fdfH.format(cal));
        assertEquals("0 00", fdfK.format(cal));
    }

    @Test(timeout = 4000)
    public void testMonthFormattingBranches() {
        FastDateFormat fdfM = FastDateFormat.getInstance("M MM", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();

        cal.set(2023, Calendar.MAY, 1); // 5
        assertEquals("5 05", fdfM.format(cal));

        cal.set(2023, Calendar.NOVEMBER, 1); // 11
        assertEquals("11 11", fdfM.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayAndPaddedNumberBranches() {
        FastDateFormat fdf = FastDateFormat.getInstance("d dd ddd DDDD yyyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();

        cal.set(2023, Calendar.JANUARY, 5); // day 5, day of year 5
        assertEquals("5 05 005 0005 02023", fdf.format(cal));

        cal.set(2023, Calendar.MAY, 25); // day 25, day of year 145
        assertEquals("25 25 025 0145 02023", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTwoDigitNumberFieldOverflow() {
        // TwoDigitNumberField with value >= 100 via day of year 'DD'
        FastDateFormat fdf = FastDateFormat.getInstance("DD", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(2023, Calendar.MAY, 25); // day of year 145
        assertEquals("145", fdf.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumericOffsets() {
        FastDateFormat fdfRFC = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("GMT+02:30"), Locale.US);
        FastDateFormat fdfISO = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT+02:30"), Locale.US);
        assertEquals("+0230", fdfRFC.format(0L));
        assertEquals("+02:30", fdfISO.format(0L));

        FastDateFormat fdfRFCMinus = FastDateFormat.getInstance("Z", TimeZone.getTimeZone("GMT-05:00"), Locale.US);
        FastDateFormat fdfISOMinus = FastDateFormat.getInstance("ZZ", TimeZone.getTimeZone("GMT-05:00"), Locale.US);
        assertEquals("-0500", fdfRFCMinus.format(0L));
        assertEquals("-05:00", fdfISOMinus.format(0L));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameForcedAndUnforcedWithDaylight() {
        SimpleTimeZone tz = new SimpleTimeZone(
            -5 * 3600 * 1000, "America/New_York",
            Calendar.APRIL, 1, 0, 2 * 3600 * 1000,
            Calendar.OCTOBER, -1, Calendar.SUNDAY, 2 * 3600 * 1000
        );

        // Forced timezone (passed directly to getInstance)
        FastDateFormat fdfShort = FastDateFormat.getInstance("z", tz, Locale.US);
        FastDateFormat fdfLong = FastDateFormat.getInstance("zzzz", tz, Locale.US);

        Calendar calSummer = Calendar.getInstance(tz, Locale.US);
        calSummer.clear();
        calSummer.set(2023, Calendar.JULY, 1);

        Calendar calWinter = Calendar.getInstance(tz, Locale.US);
        calWinter.clear();
        calWinter.set(2023, Calendar.DECEMBER, 1);

        String summerShort = fdfShort.format(calSummer);
        String winterShort = fdfShort.format(calWinter);
        assertNotEquals(summerShort, winterShort);

        String summerLong = fdfLong.format(calSummer);
        String winterLong = fdfLong.format(calWinter);
        assertNotEquals(summerLong, winterLong);

        // Unforced timezone (null passed to getInstance, timezone comes from calendar)
        FastDateFormat fdfUnforcedShort = FastDateFormat.getInstance("z", Locale.US);
        FastDateFormat fdfUnforcedLong = FastDateFormat.getInstance("zzzz", Locale.US);
        assertFalse(fdfUnforcedShort.getTimeZoneOverridesCalendar());

        assertEquals(summerShort, fdfUnforcedShort.format(calSummer));
        assertEquals(summerLong, fdfUnforcedLong.format(calSummer));
    }

    @Test(timeout = 4000)
    public void testBCDateEra() {
        FastDateFormat fdf = FastDateFormat.getInstance("G yyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 44);
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 15);

        String formatted = fdf.format(cal);
        assertTrue(formatted.startsWith("BC"));
    }

    // =========================================================================
    // Partition B: Boundary Values, Cache Integrity & Factory Overloads
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstanceCachingIntegrity() {
        FastDateFormat fdf1 = FastDateFormat.getInstance();
        FastDateFormat fdf2 = FastDateFormat.getInstance();
        assertSame(fdf1, fdf2);

        FastDateFormat fdfPattern1 = FastDateFormat.getInstance("yyyy-MM-dd");
        FastDateFormat fdfPattern2 = FastDateFormat.getInstance("yyyy-MM-dd");
        assertSame(fdfPattern1, fdfPattern2);

        TimeZone tz = TimeZone.getTimeZone("America/Chicago");
        FastDateFormat fdfTz1 = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        FastDateFormat fdfTz2 = FastDateFormat.getInstance("yyyy-MM-dd", tz);
        assertSame(fdfTz1, fdfTz2);

        Locale loc = Locale.FRENCH;
        FastDateFormat fdfLoc1 = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        FastDateFormat fdfLoc2 = FastDateFormat.getInstance("yyyy-MM-dd", loc);
        assertSame(fdfLoc1, fdfLoc2);

        FastDateFormat fdfFull1 = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        FastDateFormat fdfFull2 = FastDateFormat.getInstance("yyyy-MM-dd", tz, loc);
        assertSame(fdfFull1, fdfFull2);
    }

    @Test(timeout = 4000)
    public void testDateInstanceCaching() {
        FastDateFormat fdf1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfLoc1 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.GERMANY);
        FastDateFormat fdfLoc2 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, Locale.GERMANY);
        assertSame(fdfLoc1, fdfLoc2);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdfTz1 = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        FastDateFormat fdfTz2 = FastDateFormat.getDateInstance(FastDateFormat.LONG, tz);
        assertSame(fdfTz1, fdfTz2);

        FastDateFormat fdfFull1 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.UK);
        FastDateFormat fdfFull2 = FastDateFormat.getDateInstance(FastDateFormat.FULL, tz, Locale.UK);
        assertSame(fdfFull1, fdfFull2);
    }

    @Test(timeout = 4000)
    public void testTimeInstanceCaching() {
        FastDateFormat fdf1 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfLoc1 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.ITALY);
        FastDateFormat fdfLoc2 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, Locale.ITALY);
        assertSame(fdfLoc1, fdfLoc2);

        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDateFormat fdfTz1 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, tz);
        FastDateFormat fdfTz2 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, tz);
        assertSame(fdfTz1, fdfTz2);

        FastDateFormat fdfFull1 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, Locale.JAPAN);
        FastDateFormat fdfFull2 = FastDateFormat.getTimeInstance(FastDateFormat.FULL, tz, Locale.JAPAN);
        assertSame(fdfFull1, fdfFull2);
    }

    @Test(timeout = 4000)
    public void testDateTimeInstanceCaching() {
        FastDateFormat fdf1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        FastDateFormat fdf2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
        assertSame(fdf1, fdf2);

        FastDateFormat fdfLoc1 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG, Locale.CANADA);
        FastDateFormat fdfLoc2 = FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.LONG, Locale.CANADA);
        assertSame(fdfLoc1, fdfLoc2);

        TimeZone tz = TimeZone.getTimeZone("GMT");
        FastDateFormat fdfTz1 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT, tz);
        FastDateFormat fdfTz2 = FastDateFormat.getDateTimeInstance(FastDateFormat.LONG, FastDateFormat.SHORT, tz);
        assertSame(fdfTz1, fdfTz2);

        FastDateFormat fdfFull1 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, Locale.FRANCE);
        FastDateFormat fdfFull2 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, Locale.FRANCE);
        assertSame(fdfFull1, fdfFull2);
    }

    @Test(timeout = 4000)
    public void testFormatPolymorphicOverloads() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);
        long epochMillis = 1672531199000L; // 2022-12-31 23:59:59 UTC
        Date date = new Date(epochMillis);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.setTimeInMillis(epochMillis);

        String expected = "2022-12-31 23:59:59";

        assertEquals(expected, fdf.format(epochMillis));
        assertEquals(expected, fdf.format(date));
        assertEquals(expected, fdf.format(cal));

        StringBuffer buf1 = new StringBuffer("Prefix: ");
        assertSame(buf1, fdf.format(epochMillis, buf1));
        assertEquals("Prefix: " + expected, buf1.toString());

        StringBuffer buf2 = new StringBuffer();
        assertSame(buf2, fdf.format(date, buf2));
        assertEquals(expected, buf2.toString());

        StringBuffer buf3 = new StringBuffer();
        FieldPosition pos = new FieldPosition(0);
        assertSame(buf3, fdf.format(Long.valueOf(epochMillis), buf3, pos));
        assertEquals(expected, buf3.toString());

        StringBuffer buf4 = new StringBuffer();
        assertSame(buf4, fdf.format((Object) date, buf4, pos));
        assertEquals(expected, buf4.toString());
    }

    @Test(timeout = 4000)
    public void testGetTimeZoneDisplayStaticDirect() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        String name1 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.SHORT, Locale.US);
        String name2 = FastDateFormat.getTimeZoneDisplay(tz, false, TimeZone.SHORT, Locale.US);
        assertSame(name1, name2);

        String daylightName = FastDateFormat.getTimeZoneDisplay(tz, true, TimeZone.LONG, Locale.US);
        assertNotNull(daylightName);
        assertNotEquals(name1, daylightName);
    }

    // =========================================================================
    // Partition D: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPatternThrowsException() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIllegalPatternTokenThrowsException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnknownObjectTypeThrowsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format("NotADateOrCalendar", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatNullObjectThrowsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy");
        fdf.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidDateStyleThrowsException() {
        FastDateFormat.getDateInstance(999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidTimeStyleThrowsException() {
        FastDateFormat.getTimeInstance(999);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidDateTimeStyleThrowsException() {
        FastDateFormat.getDateTimeInstance(999, FastDateFormat.SHORT);
    }

    // =========================================================================
    // Partition E: Object Contract, ParsePosition & Serialization Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseObjectContractReturnsNull() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(4);
        Object result = fdf.parseObject("2023-01-01", pos);

        assertNull(result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testGettersAndEstimateLength() {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS Z";
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        Locale loc = Locale.GERMAN;

        FastDateFormat fdf = FastDateFormat.getInstance(pattern, tz, loc);
        assertEquals(pattern, fdf.getPattern());
        assertEquals(tz, fdf.getTimeZone());
        assertEquals(loc, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertTrue(fdf.getMaxLengthEstimate() >= pattern.length());
        assertEquals("FastDateFormat[" + pattern + "]", fdf.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        FastDateFormat fdf1 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdf2 = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", TimeZone.getTimeZone("UTC"), Locale.US);
        FastDateFormat fdfDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("GMT+1"), Locale.US);
        FastDateFormat fdfDiffLoc = FastDateFormat.getInstance("yyyy-MM-dd", TimeZone.getTimeZone("UTC"), Locale.GERMANY);
        FastDateFormat fdfUnforced = FastDateFormat.getInstance("yyyy-MM-dd", Locale.US);

        assertEquals(fdf1, fdf1);
        assertEquals(fdf1, fdf2);
        assertEquals(fdf1.hashCode(), fdf2.hashCode());

        assertFalse(fdf1.equals(null));
        assertFalse(fdf1.equals("NotAFastDateFormat"));
        assertNotEquals(fdf1, fdfDiffPattern);
        assertNotEquals(fdf1, fdfDiffTz);
        assertNotEquals(fdf1, fdfDiffLoc);
        assertNotEquals(fdf1, fdfUnforced);
    }

    @Test(timeout = 4000)
    public void testSerializationAndDeserializationReinitialization() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS zzzz", TimeZone.getTimeZone("UTC"), Locale.ENGLISH);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        FastDateFormat deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (FastDateFormat) ois.readObject();
        }

        assertNotNull(deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());

        Date testDate = new Date(1672531199000L);
        assertEquals(original.format(testDate), deserialized.format(testDate));
    }
}