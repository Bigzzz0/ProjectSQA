/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang.time;

import org.junit.Test;

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

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Defect Target (Defects4J):
 * - getDateInstance(style) & getDateTimeInstance(dateStyle, timeStyle) cache keys do not account for
 *   the system default locale when null is passed. Calling Locale.setDefault(...) between calls
 *   returns stale cached instances configured with the previous default locale.
 *   Targeted tests: test_changeDefault_Locale_DateInstance(), test_changeDefault_Locale_DateTimeInstance().
 *
 * Rules and Pattern Branch Coverage:
 * - 'G' (Era), 'y' (tokenLen >= 4 vs < 4 TwoDigitYearField),
 * - 'M' (tokenLen >= 4, 3, 2, 1), 'd' (day of month)
 * - 'h' (1..12 hour, zero-handling), 'H' (0..23 hour)
 * - 'k' (1..24 hour, zero-handling), 'K' (0..11 hour)
 * - 'm' (minute), 's' (second), 'S' (millisecond: <10, <100, >=100, >=1000)
 * - 'E' (day of week: short < 4 vs full >= 4)
 * - 'D' (day of year), 'F' (day of week in month), 'w' (week in year), 'W' (week in month)
 * - 'a' (AM/PM marker)
 * - 'z' (time zone text: tokenLen >= 4 LONG vs SHORT, forced vs non-forced, DST vs standard)
 * - 'Z' (time zone number: tokenLen 1 no colon vs colon, positive vs negative offsets)
 * - Literal patterns: single character, multi-character, escaped single quotes ('' -> ')
 * - selectNumberRule / PaddedNumberField padding checks (1, 2, >=3, size < 3 check)
 * - Format dispatch: Object (Date, Calendar, Long, invalid class, null)
 * - Calendar timezone forcing logic (clone & override vs unchanged)
 * - Equals, HashCode, ToString, Serialization (readObject initialization)
 * - parseObject unsupported behavior
 * ---------------------------------------------------------------------------------------------------
 */
public class FastDateFormatGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Caching Flaw)
    // =========================================================================

    /**
     * Targets defect: getDateInstance(style) ignores subsequent changes to Locale.getDefault()
     * because null locale isn't bound to the default locale in cache keys.
     */
    @Test(timeout = 4000)
    public void test_changeDefault_Locale_DateInstance() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
            assertEquals("Locale should be US", Locale.US, format1.getLocale());

            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateInstance(FastDateFormat.SHORT);
            assertSame("Cache must respect changed default locale", Locale.GERMANY, format2.getLocale());
        } finally {
            Locale.setDefault(original);
        }
    }

    /**
     * Targets defect: getDateTimeInstance(dateStyle, timeStyle) caching with default locale.
     */
    @Test(timeout = 4000)
    public void test_changeDefault_Locale_DateTimeInstance() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.US);
            FastDateFormat format1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
            assertEquals("Locale should be US", Locale.US, format1.getLocale());

            Locale.setDefault(Locale.GERMANY);
            FastDateFormat format2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT);
            assertSame("Cache must respect changed default locale", Locale.GERMANY, format2.getLocale());
        } finally {
            Locale.setDefault(original);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPatternFactoryVariantsAndGetters() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.ENGLISH;
        String pattern = "yyyy-MM-dd HH:mm:ss";

        FastDateFormat f1 = FastDateFormat.getInstance();
        assertNotNull(f1.getPattern());
        assertTrue(f1.getMaxLengthEstimate() > 0);

        FastDateFormat f2 = FastDateFormat.getInstance(pattern);
        assertEquals(pattern, f2.getPattern());
        assertFalse(f2.getTimeZoneOverridesCalendar());

        FastDateFormat f3 = FastDateFormat.getInstance(pattern, tz);
        assertEquals(tz, f3.getTimeZone());
        assertTrue(f3.getTimeZoneOverridesCalendar());

        FastDateFormat f4 = FastDateFormat.getInstance(pattern, loc);
        assertEquals(loc, f4.getLocale());

        FastDateFormat f5 = FastDateFormat.getInstance(pattern, tz, loc);
        assertEquals(tz, f5.getTimeZone());
        assertEquals(loc, f5.getLocale());
        assertTrue(f5.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testDateFormatFactoryVariants() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        Locale loc = Locale.US;

        FastDateFormat f1 = FastDateFormat.getDateInstance(FastDateFormat.FULL);
        assertNotNull(f1);

        FastDateFormat f2 = FastDateFormat.getDateInstance(FastDateFormat.LONG, loc);
        assertEquals(loc, f2.getLocale());

        FastDateFormat f3 = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, tz);
        assertEquals(tz, f3.getTimeZone());

        FastDateFormat f4 = FastDateFormat.getDateInstance(FastDateFormat.SHORT, tz, loc);
        assertEquals(tz, f4.getTimeZone());
        assertEquals(loc, f4.getLocale());
    }

    @Test(timeout = 4000)
    public void testTimeFormatFactoryVariants() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Locale loc = Locale.UK;

        FastDateFormat f1 = FastDateFormat.getTimeInstance(FastDateFormat.FULL);
        assertNotNull(f1);

        FastDateFormat f2 = FastDateFormat.getTimeInstance(FastDateFormat.LONG, loc);
        assertEquals(loc, f2.getLocale());

        FastDateFormat f3 = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, tz);
        assertEquals(tz, f3.getTimeZone());

        FastDateFormat f4 = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, tz, loc);
        assertEquals(tz, f4.getTimeZone());
        assertEquals(loc, f4.getLocale());
    }

    @Test(timeout = 4000)
    public void testDateTimeFormatFactoryVariants() {
        TimeZone tz = TimeZone.getTimeZone("GMT+1");
        Locale loc = Locale.FRANCE;

        FastDateFormat f1 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG);
        assertNotNull(f1);

        FastDateFormat f2 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG, loc);
        assertEquals(loc, f2.getLocale());

        FastDateFormat f3 = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.LONG, tz);
        assertEquals(tz, f3.getTimeZone());

        FastDateFormat f4 = FastDateFormat.getDateTimeInstance(FastDateFormat.FULL, FastDateFormat.FULL, tz, loc);
        assertEquals(tz, f4.getTimeZone());
        assertEquals(loc, f4.getLocale());
    }

    @Test(timeout = 4000)
    public void testAllPatternTokensFormatting() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Locale loc = Locale.US;
        // G: Era, y: 4-digit, yy: 2-digit, M: 1-digit, MM: 2-digit, MMM: short, MMMM: long
        // d: day, h: 12-hr, H: 24-hr, m: min, s: sec, S: ms, E: short day, EEEE: full day
        // D: day-in-year, F: day-of-week-in-month, w: week-in-year, W: week-in-month, a: AM/PM
        // k: 1-24 hr, K: 0-11 hr, z: short tz, zzzz: long tz, Z: RFC822, ZZ: ISO8601
        String pattern = "G yyyy yy M MM MMM MMMM d h H m s S E EEEE D F w W a k K z zzzz Z ZZ";
        FastDateFormat f = FastDateFormat.getInstance(pattern, tz, loc);

        Calendar cal = new GregorianCalendar(tz, loc);
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(2023, Calendar.JANUARY, 5, 14, 8, 9);
        cal.set(Calendar.MILLISECOND, 7);

        String result = f.format(cal);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("2023 23"));
        assertTrue(result.contains("1 01 Jan January"));
        assertTrue(result.contains("5"));     // day
        assertTrue(result.contains("2 14"));  // 12-hr, 24-hr
        assertTrue(result.contains("8 9 7")); // min, sec, ms
        assertTrue(result.contains("PM"));
        assertTrue(result.contains("+0000")); // Z
        assertTrue(result.contains("+00:00")); // ZZ
    }

    @Test(timeout = 4000)
    public void testSpecialHourBoundaries() {
        // TwelveHourField (h) should map 0 -> 12
        // TwentyFourHourField (k) should map 0 -> 24
        FastDateFormat f = FastDateFormat.getInstance("h k H K", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);

        cal.set(Calendar.HOUR_OF_DAY, 0); // Midnight
        assertEquals("12 24 0 0", f.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 12); // Noon
        assertEquals("12 12 12 0", f.format(cal));
    }

    @Test(timeout = 4000)
    public void testNumberFieldPaddings() {
        // SSS (padding 3), SSSS (padding 4), SSSSS (padding 5)
        FastDateFormat f3 = FastDateFormat.getInstance("SSS SSSS SSSSS", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);

        cal.set(Calendar.MILLISECOND, 5);
        assertEquals("005 0005 00005", f3.format(cal));

        cal.set(Calendar.MILLISECOND, 45);
        assertEquals("045 0045 00045", f3.format(cal));

        cal.set(Calendar.MILLISECOND, 678);
        assertEquals("678 0678 00678", f3.format(cal));

        FastDateFormat fYear = FastDateFormat.getInstance("yyyyy", TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(Calendar.YEAR, 12345);
        assertEquals("12345", fYear.format(cal));

        cal.set(Calendar.YEAR, 2023);
        assertEquals("02023", fYear.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatObjectDispatch() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2020, Calendar.OCTOBER, 31, 12, 0, 0);
        Date date = cal.getTime();
        long millis = date.getTime();

        StringBuffer sb = new StringBuffer();
        assertEquals("2020-10-31 12:00:00", f.format((Object) date, sb, new FieldPosition(0)).toString());

        sb = new StringBuffer();
        assertEquals("2020-10-31 12:00:00", f.format((Object) cal, sb, new FieldPosition(0)).toString());

        sb = new StringBuffer();
        assertEquals("2020-10-31 12:00:00", f.format((Object) new Long(millis), sb, new FieldPosition(0)).toString());

        assertEquals("2020-10-31 12:00:00", f.format(millis));
        assertEquals("2020-10-31 12:00:00", f.format(date));
        assertEquals("2020-10-31 12:00:00", f.format(cal));

        sb = new StringBuffer();
        assertEquals("2020-10-31 12:00:00", f.format(millis, sb).toString());
    }

    @Test(timeout = 4000)
    public void testCalendarTimeZoneForcing() {
        TimeZone tzUtc = TimeZone.getTimeZone("UTC");
        TimeZone tzJst = TimeZone.getTimeZone("Asia/Tokyo");

        // Forced timezone (fForced)
        FastDateFormat fForced = FastDateFormat.getInstance("HH", tzUtc);
        Calendar calJst = new GregorianCalendar(tzJst, Locale.US);
        calJst.set(Calendar.HOUR_OF_DAY, 9); // 09:00 JST -> 00:00 UTC
        calJst.set(Calendar.MINUTE, 0);
        calJst.set(Calendar.SECOND, 0);

        // Calendar timezone should be converted to UTC because mTimeZoneForced is true
        assertEquals("00", fForced.format(calJst));

        // Non-forced timezone: calendar's timezone remains untouched
        FastDateFormat fDefault = FastDateFormat.getInstance("HH");
        assertFalse(fDefault.getTimeZoneOverridesCalendar());
    }

    @Test(timeout = 4000)
    public void testTimeZoneNumericFormatting() {
        // Test negative offset
        TimeZone tzNeg = TimeZone.getTimeZone("GMT-08:00");
        FastDateFormat fNeg = FastDateFormat.getInstance("Z ZZ", tzNeg, Locale.US);
        Calendar calNeg = new GregorianCalendar(tzNeg, Locale.US);
        assertEquals("-0800 -08:00", fNeg.format(calNeg));

        // Test positive offset with partial hour
        TimeZone tzPos = TimeZone.getTimeZone("GMT+05:30");
        FastDateFormat fPos = FastDateFormat.getInstance("Z ZZ", tzPos, Locale.US);
        Calendar calPos = new GregorianCalendar(tzPos, Locale.US);
        assertEquals("+0530 +05:30", fPos.format(calPos));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNameRuleStandardAndDaylight() {
        // Use a timezone with daylight savings, e.g., America/New_York
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        FastDateFormat fForced = FastDateFormat.getInstance("z zzzz", tz, Locale.US);

        Calendar calWinter = new GregorianCalendar(tz, Locale.US);
        calWinter.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        String winter = fForced.format(calWinter);
        assertTrue(winter.contains("EST") || winter.contains("Eastern Standard Time"));

        Calendar calSummer = new GregorianCalendar(tz, Locale.US);
        calSummer.set(2023, Calendar.JULY, 1, 12, 0, 0);
        String summer = fForced.format(calSummer);
        assertTrue(summer.contains("EDT") || summer.contains("Eastern Daylight Time"));

        // Non-forced timezone rule branch
        FastDateFormat fUnforced = FastDateFormat.getInstance("z zzzz", Locale.US);
        String unforcedWinter = fUnforced.format(calWinter);
        assertTrue(unforcedWinter.contains("EST") || unforcedWinter.contains("Eastern Standard Time"));
        String unforcedSummer = fUnforced.format(calSummer);
        assertTrue(unforcedSummer.contains("EDT") || unforcedSummer.contains("Eastern Daylight Time"));
    }

    @Test(timeout = 4000)
    public void testLiteralQuoting() {
        // Single character literal 'X', multi-char literal 'Hello', and escaped quote ''
        FastDateFormat f = FastDateFormat.getInstance("'[' 'Hello' '' yyyy ''']'", TimeZone.getTimeZone("UTC"), Locale.US);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(Calendar.YEAR, 2023);
        String formatted = f.format(cal);
        assertEquals("[ Hello ' 2023 ']", formatted);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPattern() {
        FastDateFormat f = FastDateFormat.getInstance("");
        assertEquals("", f.getPattern());
        assertEquals("", f.format(new Date()));
        assertEquals(0, f.getMaxLengthEstimate());
    }

    @Test(timeout = 4000)
    public void testParseObjectContract() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy");
        ParsePosition pos = new ParsePosition(10);
        Object parsed = f.parseObject("2023", pos);
        assertNull("Parsing is explicitly unsupported and must return null", parsed);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    @Test(timeout = 4000)
    public void testCustomTimeZoneWithoutDaylight() {
        TimeZone customTz = new SimpleTimeZone(2 * 60 * 60 * 1000, "CustomGMT+2");
        FastDateFormat f = FastDateFormat.getInstance("z ZZ", customTz, Locale.US);
        Calendar cal = new GregorianCalendar(customTz, Locale.US);
        String output = f.format(cal);
        assertTrue(output.contains("+02:00"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPatternThrowsException() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPatternTokenThrowsException() {
        FastDateFormat.getInstance("yyyy-MM-dd B"); // 'B' is illegal in SimpleDateFormat/FastDateFormat
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnknownObjectThrowsException() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy");
        f.format("not a date/calendar/long", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatNullObjectThrowsException() {
        FastDateFormat f = FastDateFormat.getInstance("yyyy");
        f.format(null, new StringBuffer(), new FieldPosition(0));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT");
        TimeZone tz2 = TimeZone.getTimeZone("UTC");
        Locale loc1 = Locale.US;
        Locale loc2 = Locale.UK;

        FastDateFormat f1 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc1);
        FastDateFormat f2 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc1);
        FastDateFormat f3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm", tz1, loc1);
        FastDateFormat f4 = FastDateFormat.getInstance("yyyy-MM-dd", tz2, loc1);
        FastDateFormat f5 = FastDateFormat.getInstance("yyyy-MM-dd", tz1, loc2);
        FastDateFormat f6 = FastDateFormat.getInstance("yyyy-MM-dd", loc1); // tz forced false

        // Reflexive & Symmetric
        assertEquals(f1, f1);
        assertEquals(f1, f2);
        assertEquals(f2, f1);
        assertEquals(f1.hashCode(), f2.hashCode());

        // Inequality
        assertNotEquals(f1, f3);
        assertNotEquals(f1, f4);
        assertNotEquals(f1, f5);
        assertNotEquals(f1, f6);
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("different-type-string"));

        assertEquals("FastDateFormat[yyyy-MM-dd]", f1.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        FastDateFormat original = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS z Z", TimeZone.getTimeZone("UTC"), Locale.US);

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

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"), Locale.US);
        cal.set(2023, Calendar.MARCH, 15, 10, 20, 30);
        cal.set(Calendar.MILLISECOND, 100);

        assertEquals(original.format(cal), deserialized.format(cal));
    }
}