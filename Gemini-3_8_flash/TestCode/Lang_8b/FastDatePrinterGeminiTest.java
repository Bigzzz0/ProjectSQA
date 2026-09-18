/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang3.time.FastDatePrinter
 *
 * 1. Defects4J Known Defect Targeted:
 *    - Bug: FastDatePrinter#TimeZoneNameRule formats the time zone using the printer's
 *      default time zone rather than respecting the time zone of the Calendar passed to format(Calendar).
 *    - Defect Root: TimeZoneNameRule holds a reference to the printer's TimeZone ('zone') and ignores
 *      calendar.getTimeZone() during Rule#appendTo(StringBuffer, Calendar).
 *    - Targeted Test: testCalendarTimezoneRespected() explicitly checks that a Calendar with
 *      TimeZone "Asia/Bangkok" (ICT) formatted via a printer instantiated with "America/Los_Angeles"
 *      outputs "ICT" (or respects the Calendar's zone display name).
 *
 * 2. Decision Branches Covered:
 *    - Pattern parsing ('G', 'y', 'M', 'd', 'h', 'H', 'm', 's', 'S', 'E', 'D', 'F', 'w', 'W', 'a', 'k', 'K', 'z', 'Z', '\'')
 *    - Month lengths: >= 4 (Full text), == 3 (Short text), == 2 (TwoDigitMonthField), 1 (UnpaddedMonthField)
 *    - Year lengths: == 2 (TwoDigitYearField), < 4 (Padded 4), >= 4 (Padded length)
 *    - Day of week ('E'): < 4 (Short text), >= 4 (Full text)
 *    - TimeZone text ('z'): < 4 (SHORT), >= 4 (LONG); standard vs daylight time
 *    - TimeZone number ('Z'): 1 (no colon RFC822), > 1 (ISO8601 colon formatted); positive vs negative offsets
 *    - TwelveHourField (hour 0 -> 12) & TwentyFourHourField (hour 0 -> 24)
 *    - PaddedNumberField, TwoDigitNumberField, UnpaddedNumberField branching on value magnitudes (<10, <100, <1000, >=1000)
 *    - parseToken quotes: single quote, consecutive quotes (''), text interspersed with quotes
 *    - Object formatting dispatch: Date, Calendar, Long, unsupported types, null
 *    - Serialization & deserialization (readObject reconstitutes transient rules & length estimate)
 *    - Object contract: equals, hashCode, toString
 */
package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class FastDatePrinterGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where calendar.getTimeZone() is ignored when formatting a Calendar,
     * incorrectly using the printer's TimeZone instead of the Calendar's TimeZone in TimeZoneNameRule.
     */
    @Test(timeout = 4000)
    public void testCalendarTimezoneRespected() {
        String pattern = "h:mma z";
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok"), Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JANUARY, 1, 14, 43, 0);

        FastDatePrinter printer = new FastDatePrinter(pattern, TimeZone.getTimeZone("America/Los_Angeles"), Locale.US);
        String formatted = printer.format(cal);
        assertTrue("Formatted calendar timezone should respect calendar's zone (ICT), but was: " + formatted,
                formatted.contains("ICT"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Pattern Rules
    // =========================================================================

    @Test(timeout = 4000)
    public void testEraAndYearPatterns() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        // G, y, yy, yyy, yyyyy
        FastDatePrinter pEra = new FastDatePrinter("G y yy yyy yyyyy", tz, locale);
        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(Calendar.ERA, GregorianCalendar.AD);
        cal.set(2023, Calendar.MARCH, 15, 10, 0, 0);

        String result = pEra.format(cal);
        assertEquals("AD 2023 23 2023 02023", result);

        // Negative/BC Era
        cal.set(Calendar.ERA, GregorianCalendar.BC);
        cal.set(Calendar.YEAR, 5);
        FastDatePrinter pBc = new FastDatePrinter("G y yy", tz, locale);
        assertEquals("BC 0005 05", pBc.format(cal));
    }

    @Test(timeout = 4000)
    public void testMonthPatternsAllLengths() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        // M (unpadded), MM (two-digit), MMM (short text), MMMM (full text)
        FastDatePrinter printer = new FastDatePrinter("M MM MMM MMMM", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        // Month 1-digit: February (index 1)
        cal.set(2023, Calendar.FEBRUARY, 10);
        assertEquals("2 02 Feb February", printer.format(cal));

        // Month 2-digit: November (index 10)
        cal.set(2023, Calendar.NOVEMBER, 10);
        assertEquals("11 11 Nov November", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayAndWeekInMonthYear() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        FastDatePrinter printer = new FastDatePrinter("d dd D DDD F w ww W", tz, locale);
        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 5); // Day 5 of month and year

        assertEquals("5 05 5 005 1 1 01 1", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testDayOfWeekPatterns() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        FastDatePrinter pShort = new FastDatePrinter("E", tz, locale);
        FastDatePrinter pLong = new FastDatePrinter("EEEE", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1); // Sunday
        assertEquals("Sun", pShort.format(cal));
        assertEquals("Sunday", pLong.format(cal));
    }

    @Test(timeout = 4000)
    public void testHourPatternsAndBoundaryZeroTransitions() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        // h (1..12), H (0..23), k (1..24), K (0..11)
        FastDatePrinter printer = new FastDatePrinter("h H k K a", tz, locale);

        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();

        // Midnight (00:00:00): h should be 12, H should be 0, k should be 24, K should be 0
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        assertEquals("12 0 24 0 AM", printer.format(cal));

        // Noon (12:00:00): h should be 12, H should be 12, k should be 12, K should be 0
        cal.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        assertEquals("12 12 12 0 PM", printer.format(cal));

        // 13:00:00: h should be 1, H should be 13, k should be 13, K should be 1
        cal.set(2023, Calendar.JANUARY, 1, 13, 0, 0);
        assertEquals("1 13 13 1 PM", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testMinutesSecondsMilliseconds() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        FastDatePrinter printer = new FastDatePrinter("m mm s ss S SS SSS SSSS", tz, locale);
        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1, 1, 5, 9);
        cal.set(Calendar.MILLISECOND, 7);

        assertEquals("5 05 9 09 7 07 007 0007", printer.format(cal));

        // Multi-digit milliseconds
        cal.set(Calendar.MILLISECOND, 42);
        FastDatePrinter pMs = new FastDatePrinter("S SS SSS SSSS", tz, locale);
        assertEquals("42 42 042 0042", pMs.format(cal));

        cal.set(Calendar.MILLISECOND, 850);
        assertEquals("850 850 850 0850", pMs.format(cal));
    }

    @Test(timeout = 4000)
    public void testPaddedNumberFieldLargeValues() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        FastDatePrinter printer = new FastDatePrinter("yyyyy", tz, locale);
        Calendar cal = new GregorianCalendar(tz, locale);
        cal.clear();
        cal.set(Calendar.YEAR, 12345);
        assertEquals("12345", printer.format(cal));

        cal.set(Calendar.YEAR, 123);
        assertEquals("00123", printer.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZonePatternsColonAndNoColon() {
        Locale locale = Locale.US;
        TimeZone tzPst = TimeZone.getTimeZone("GMT-08:00");
        TimeZone tzPositive = TimeZone.getTimeZone("GMT+05:30");

        // Z (RFC822, no colon), ZZ (ISO8601, with colon)
        FastDatePrinter pPst = new FastDatePrinter("Z ZZ", tzPst, locale);
        FastDatePrinter pPositive = new FastDatePrinter("Z ZZ", tzPositive, locale);

        Calendar cal = new GregorianCalendar(tzPst, locale);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);
        assertEquals("-0800 -08:00", pPst.format(cal));

        Calendar calPos = new GregorianCalendar(tzPositive, locale);
        calPos.clear();
        calPos.set(2023, Calendar.JANUARY, 1);
        assertEquals("+0530 +05:30", pPositive.format(calPos));
    }

    @Test(timeout = 4000)
    public void testTimeZoneNamePatternsShortAndLong() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("America/New_York");

        FastDatePrinter printer = new FastDatePrinter("z zzzz", tz, locale);

        Calendar calStandard = new GregorianCalendar(tz, locale);
        calStandard.clear();
        calStandard.set(2023, Calendar.JANUARY, 15); // Standard time
        String stdStr = printer.format(calStandard);
        assertTrue("Standard time should contain EST", stdStr.contains("EST"));
        assertTrue("Standard time should contain Eastern Standard Time", stdStr.contains("Eastern Standard Time"));

        Calendar calDst = new GregorianCalendar(tz, locale);
        calDst.clear();
        calDst.set(2023, Calendar.JULY, 15); // DST
        String dstStr = printer.format(calDst);
        assertTrue("Daylight time should contain EDT", dstStr.contains("EDT"));
        assertTrue("Daylight time should contain Eastern Daylight Time", dstStr.contains("Eastern Daylight Time"));
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyCoverage() {
        // Test custom timezone without DST rules
        TimeZone noDst = new SimpleTimeZone(3600000, "CustomNoDst");
        FastDatePrinter p = new FastDatePrinter("z zzzz", noDst, Locale.US);
        Calendar cal = new GregorianCalendar(noDst, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.JANUARY, 1);
        assertNotNull(p.format(cal));
    }

    @Test(timeout = 4000)
    public void testLiteralQuoting() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");

        // Literal single quotes, escaped single quotes, text in quotes, unquoted literal chars
        FastDatePrinter p1 = new FastDatePrinter("'Hello' 'o''clock' '-'", tz, locale);
        Calendar cal = new GregorianCalendar(tz, locale);
        assertEquals("Hello o'clock -", p1.format(cal));

        FastDatePrinter p2 = new FastDatePrinter("''yyyy''", tz, locale);
        cal.set(Calendar.YEAR, 2023);
        assertEquals("'2023'", p2.format(cal));

        FastDatePrinter p3 = new FastDatePrinter("yyyy-MM-dd'T'HH:mm:ss", tz, locale);
        cal.clear();
        cal.set(2023, Calendar.APRIL, 9, 8, 30, 45);
        assertEquals("2023-04-09T08:30:45", p3.format(cal));

        // Single character literal
        FastDatePrinter p4 = new FastDatePrinter("'X' yyyy", tz, locale);
        assertEquals("X 2023", p4.format(cal));
    }

    // =========================================================================
    // Partition B: Boundary Values, Overloaded Formats & Formatting Null/Objects
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatOverloads() {
        Locale locale = Locale.US;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        FastDatePrinter printer = new FastDatePrinter("yyyy-MM-dd HH:mm: