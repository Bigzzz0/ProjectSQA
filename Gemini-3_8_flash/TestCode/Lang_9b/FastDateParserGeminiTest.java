/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Branch / Zone                       Target Description & Coverage Intent
 * ---------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic  - Standard date/time patterns (yyyy-MM-dd, HH:mm:ss, etc.).
 *                                     - Adjacent numeric fields (yyyyMMddHHmmss) triggering isNextNumber().
 *                                     - Non-numeric text fields (MMM, MMMM, EEE, EEEE, a, G).
 *                                     - TimeZone parsing (z, Z) across offset formats (+00:00, GMT-5, PST).
 *                                     - Modulo strategies for hours (H: 0-23 % 24, h: 1-12 % 12, K, k).
 * Partition B: Boundary Value (BVA)   - 2-digit year handling (<100 vs >=100) via adjustYear().
 *                                     - Day of year (D), day of week in month (F), week of year/month (w, W).
 *                                     - Milliseconds (S).
 *                                     - Quoted literals ('text', escaped quotes '').
 * Partition C: Defect-Targeted Zone   - Defect LANG-832: Pattern "d'd'" leaves unparsed trailing quote/token
 *                                       and erroneously parses "d3" instead of failing.
 * Partition D: Defensive & Exception  - Japanese Imperial calendar (ja_JP_JP) pre-1868 error messaging.
 *                                     - Illegal pattern tokens throwing IllegalArgumentException in init().
 *                                     - ParseException on mismatched input.
 *                                     - Invalid field in getDisplayNames().
 *                                     - Null keys in toArray() era conversion.
 * Partition E: Contract & Lifecycle   - equals() & hashCode() equivalence across identical/distinct fields.
 *                                     - toString() output structure.
 *                                     - Java Serializable contract (write/readObject round-trip).
 * ---------------------------------------------------------------------------------------------------
 */

package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class FastDateParserGeminiTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone PST = TimeZone.getTimeZone("PST");
    private static final Locale US = Locale.US;

    // ===============================================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-832)
    // ===============================================================================================

    /**
     * LANG-832: Pattern with trailing unclosed quote or partially consumed pattern like "d'd'".
     * Expected behavior: SimpleDateFormat / FastDateParser should fail on invalid syntax,
     * or at minimum not match and parse "d3" using "d(\\p{IsNd}++)".
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLANG_832_InvalidPatternRejection() {
        new FastDateParser("d'd'", PST, US);
    }

    @Test(timeout = 4000)
    public void testLANG_832_ParseFailure() {
        try {
            DateParser fdp = new FastDateParser("d'd'", PST, US);
            Date parsed = fdp.parse("d3");
            fail("Expected failure for pattern [d'd'] with input [d3], but got: " + parsed);
        } catch (IllegalArgumentException expected) {
            // Expected when constructor or parser identifies invalid format
        } catch (ParseException expected) {
            // Expected if constructor passed but parsing correctly failed
        }
    }

    // ===============================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testStandardDateParsing() throws ParseException {
        DateParser parser = new FastDateParser("yyyy-MM-dd HH:mm:ss.SSS", GMT, US);
        Date date = parser.parse("2023-11-15 14:30:45.123");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(14, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
        assertEquals(123, cal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testAdjacentNumericFields_isNextNumberBranch() throws ParseException {
        // Exercises isNextNumber() == true branch where width specifier is added to regex
        DateParser parser = new FastDateParser("yyyyMMddHHmmss", GMT, US);
        Date date = parser.parse("20231225184530");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(45, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testTextStrategies_Month_DayOfWeek_AMPM_Era() throws ParseException {
        DateParser parser = new FastDateParser("GGGG yyyy MMMM EEEE a", GMT, US);
        Date date = parser.parse("AD 2022 September Sunday PM");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        assertEquals(1, cal.get(Calendar.ERA)); // GregorianCalendar.AD
        assertEquals(2022, cal.get(Calendar.YEAR));
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));
        assertEquals(Calendar.SUNDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(Calendar.PM, cal.get(Calendar.AM_PM));
    }

    @Test(timeout = 4000)
    public void testShortTextStrategies() throws ParseException {
        DateParser parser = new FastDateParser("G yy MMM EEE a", GMT, US);
        Date date = parser.parse("AD 21 Mar Wed AM");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH));
        assertEquals(Calendar.WEDNESDAY, cal.get(Calendar.DAY_OF_WEEK));
        assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));
    }

    @Test(timeout = 4000)
    public void testHourStrategies_H_K_h_k() throws ParseException {
        // H: 0-23 (modulo 24)
        DateParser parserH = new FastDateParser("H", GMT, US);
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(parserH.parse("24"));
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));

        // h: 1-12 (modulo 12)
        DateParser parserh = new FastDateParser("h a", GMT, US);
        cal.setTime(parserh.parse("12 PM"));
        assertEquals(0, cal.get(Calendar.HOUR));
        assertEquals(12, cal.get(Calendar.HOUR_OF_DAY));

        // K: 0-11
        DateParser parserK = new FastDateParser("K a", GMT, US);
        cal.setTime(parserK.parse("11 AM"));
        assertEquals(11, cal.get(Calendar.HOUR));
        assertEquals(Calendar.AM, cal.get(Calendar.AM_PM));

        // k: 1-24
        DateParser parserk = new FastDateParser("k", GMT, US);
        cal.setTime(parserk.parse("23"));
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test(timeout = 4000)
    public void testCalendarFields_D_F_w_W() throws ParseException {
        DateParser parser = new FastDateParser("yyyy D F w W", GMT, US);
        Date date = parser.parse("2023 150 2 22 3");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);

        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(150, cal.get(Calendar.DAY_OF_YEAR));
        assertEquals(2, cal.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        assertEquals(22, cal.get(Calendar.WEEK_OF_YEAR));
        assertEquals(3, cal.get(Calendar.WEEK_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testTimeZoneStrategyVariants() throws ParseException {
        // Offset format with sign
        DateParser parserOffset = new FastDateParser("yyyy-MM-dd z", GMT, US);
        Date d1 = parserOffset.parse("2023-01-01 +05:00");
        assertNotNull(d1);

        Date d2 = parserOffset.parse("2023-01-01 -0800");
        assertNotNull(d2);

        // Explicit GMT format
        Date d3 = parserOffset.parse("2023-01-01 GMT-07:00");
        assertNotNull(d3);

        // Named timezone format
        Date d4 = parserOffset.parse("2023-01-01 PST");
        assertNotNull(d4);

        // Pattern with 'Z'
        DateParser parserZ = new FastDateParser("yyyy-MM-dd Z", GMT, US);
        Date d5 = parserZ.parse("2023-01-01 -0500");
        assertNotNull(d5);
    }

    // ===============================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testAdjustYearBoundary() {
        FastDateParser parser = new FastDateParser("yy", GMT, US);
        int currentYear = Calendar.getInstance(GMT, US).get(Calendar.YEAR);
        int thisCentury = currentYear - (currentYear % 100);

        // Within thisYear + 20 boundary
        int yearWithin = (currentYear + 10) % 100;
        int expectedWithin = thisCentury + yearWithin;
        if (expectedWithin >= currentYear + 20) {
            expectedWithin -= 100;
        }
        assertEquals(expectedWithin, parser.adjustYear(yearWithin));

        // Outside thisYear + 20 boundary
        int yearBeyond = (currentYear + 25) % 100;
        int trial = thisCentury + yearBeyond;
        int expectedBeyond = (trial < currentYear + 20) ? trial : trial - 100;
        assertEquals(expectedBeyond, parser.adjustYear(yearBeyond));
    }

    @Test(timeout = 4000)
    public void testAbbreviatedYearStrategy_AboveAndBelow100() throws ParseException {
        DateParser parser = new FastDateParser("yy-MM-dd", GMT, US);

        // 2-digit year (triggers adjustYear)
        Date d1 = parser.parse("21-01-01");
        Calendar cal1 = Calendar.getInstance(GMT, US);
        cal1.setTime(d1);
        assertTrue(cal1.get(Calendar.YEAR) >= 1900);

        // 4-digit value matching pattern with yy (value >= 100 does not call adjustYear)
        Date d2 = parser.parse("2045-01-01");
        Calendar cal2 = Calendar.getInstance(GMT, US);
        cal2.setTime(d2);
        assertEquals(2045, cal2.get(Calendar.YEAR));
    }

    @Test(timeout = 4000)
    public void testLiteralYear_ThreeOrMoreDigits() throws ParseException {
        DateParser parser = new FastDateParser("yyyyy-MM-dd", GMT, US);
        Date date = parser.parse("02023-05-10");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
    }

    @Test(timeout = 4000)
    public void testEscapeRegexAndQuotes() throws ParseException {
        // Special regex characters escaped: [ ] ( ) { } \ | * + ^ $ . ?
        String specialPattern = "'['yyyy']' '.' '('MM')' '?' '{'dd'}'";
        DateParser parser = new FastDateParser(specialPattern, GMT, US);
        Date date = parser.parse("[2023] . (10) ? {25}");

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));

        // Escaped single quotes within quoted section: ''
        DateParser quoteParser = new FastDateParser("''yyyy''", GMT, US);
        Date quoteDate = quoteParser.parse("'2023'");
        cal.setTime(quoteDate);
        assertEquals(2023, cal.get(Calendar.YEAR));
    }

    @Test(timeout = 4000)
    public void testParsePositionAdvance() {
        DateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String text = "Prefix 2023-11-20 Suffix";
        ParsePosition pos = new ParsePosition(7);

        Date date = parser.parse(text, pos);
        assertNotNull(date);
        assertEquals(17, pos.getIndex());

        // ParseObject variant
        pos.setIndex(7);
        Object obj = parser.parseObject(text, pos);
        assertEquals(date, obj);
    }

    // ===============================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===============================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPatternStart() {
        new FastDateParser("[invalid]", GMT, US);
    }

    @Test(expected = ParseException.class, timeout = 4000)
    public void testUnparseableDateThrowsParseException() throws ParseException {
        DateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.parse("NotADate");
    }

    @Test(timeout = 4000)
    public void testJapaneseImperialLocaleExceptionMessage() {
        DateParser parser = new FastDateParser("yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("invalid");
            fail("Expected ParseException");
        } catch (ParseException ex) {
            assertTrue(ex.getMessage().contains("The ja_JP_JP locale does not support dates before 1868 AD"));
        }
    }

    @Test(timeout = 4000)
    public void testJapaneseImperialEraParsing() throws ParseException {
        DateParser parser = new FastDateParser("GGGG yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        assertNotNull(parser);
        assertNotNull(parser.getParsePattern());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFieldInGetDisplayNames() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        parser.getDisplayNames(Calendar.ZONE_OFFSET);
    }

    @Test(timeout = 4000)
    public void testToArrayNullKeyThrowsIllegalArgumentException() throws Exception {
        FastDateParser parser = new FastDateParser("yyyy", GMT, US);
        Method toArrayMethod = FastDateParser.class.getDeclaredMethod("toArray", Map.class);
        toArrayMethod.setAccessible(true);

        Map<String, Integer> mapWithNull = new HashMap<String, Integer>();
        mapWithNull.put(null, 0);

        try {
            toArrayMethod.invoke(parser, mapWithNull);
            fail("Expected InvocationTargetException containing IllegalArgumentException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testCopyAndCountHelperMethods() throws Exception {
        Method countMethod = FastDateParser.class.getDeclaredMethod("count", String[].class);
        countMethod.setAccessible(true);

        assertEquals(0, countMethod.invoke(null, (Object) null));
        assertEquals(2, countMethod.invoke(null, (Object) new String[]{"abc", "", "def"}));

        Method copyMethod = FastDateParser.class.getDeclaredMethod("copy",
                Class.forName("org.apache.commons.lang3.time.FastDateParser$KeyValue[]"),
                int.class,
                String[].class);
        copyMethod.setAccessible(true);

        assertEquals(5, copyMethod.invoke(null, null, 5, null));
    }

    @Test(timeout = 4000)
    public void testTextStrategySetCalendarNotFoundThrows() throws Exception {
        FastDateParser parser = new FastDateParser("MMMM", GMT, US);
        Field strategiesField = FastDateParser.class.getDeclaredField("strategies");
        strategiesField.setAccessible(true);
        Object[] strategies = (Object[]) strategiesField.get(parser);

        Method setCalendarMethod = strategies[0].getClass().getDeclaredMethod("setCalendar",
                FastDateParser.class, Calendar.class, String.class);
        setCalendarMethod.setAccessible(true);

        Calendar cal = Calendar.getInstance(GMT, US);
        try {
            setCalendarMethod.invoke(strategies[0], parser, cal, "NonExistentMonth");
            fail("Expected IllegalArgumentException wrapped in InvocationTargetException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testTimeZoneStrategyUnsupportedZoneThrows() throws Exception {
        FastDateParser parser = new FastDateParser("z", GMT, US);
        Field strategiesField = FastDateParser.class.getDeclaredField("strategies");
        strategiesField.setAccessible(true);
        Object[] strategies = (Object[]) strategiesField.get(parser);

        Method setCalendarMethod = strategies[0].getClass().getDeclaredMethod("setCalendar",
                FastDateParser.class, Calendar.class, String.class);
        setCalendarMethod.setAccessible(true);

        Calendar cal = Calendar.getInstance(GMT, US);
        try {
            setCalendarMethod.invoke(strategies[0], parser, cal, "UnsupportedTimeZoneXYZ");
            fail("Expected IllegalArgumentException wrapped in InvocationTargetException");
        } catch (Exception ex) {
            assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }

    // ===============================================================================================
    // Partition E: Object Lifecycle, Contract Integrity & Accessors
    // ===============================================================================================

    @Test(timeout = 4000)
    public void testAccessors() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        assertEquals("yyyy-MM-dd", parser.getPattern());
        assertEquals(GMT, parser.getTimeZone());
        assertEquals(US, parser.getLocale());
        assertNotNull(parser.getParsePattern());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        FastDateParser p1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser p2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser diffPattern = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser diffZone = new FastDateParser("yyyy-MM-dd", PST, US);
        FastDateParser diffLocale = new FastDateParser("yyyy-MM-dd", GMT, Locale.GERMANY);

        // Reflexive
        assertTrue(p1.equals(p1));
        // Symmetric
        assertTrue(p1.equals(p2));
        assertTrue(p2.equals(p1));
        assertEquals(p1.hashCode(), p2.hashCode());

        // Incompatible types and null
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("A String"));

        // Differences
        assertFalse(p1.equals(diffPattern));
        assertFalse(p1.equals(diffZone));
        assertFalse(p1.equals(diffLocale));
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String str = parser.toString();
        assertEquals("FastDateParser[yyyy-MM-dd,en_US,GMT]", str);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        FastDateParser original = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FastDateParser deserialized = (FastDateParser) ois.readObject();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());

        // Verify deserialized parser is fully operational
        Date d1 = original.parse("2023-08-15 12:00:00");
        Date d2 = deserialized.parse("2023-08-15 12:00:00");
        assertEquals(d1, d2);
    }

    @Test(timeout = 4000)
    public void testParseObjectVariant() throws ParseException {
        DateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj = parser.parseObject("2023-01-01");
        assertTrue(obj instanceof Date);
    }
}