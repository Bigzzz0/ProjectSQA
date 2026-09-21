package org.apache.commons.lang3.time;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: FastDateParser
 *
 * Decision / Branch Coverage Targets:
 * 1. formatPattern tokenization:
 *    - Letters: 'y', 'M', 'd', 'H', 'h', 'm', 's', 'S', 'E', 'D', 'F', 'w', 'W', 'a', 'k', 'K', 'z', 'Z', 'G'
 *    - Quotes: single quote pair "''", quoted literals "'text'", unclosed/empty edge cases
 *    - Non-letters / Escapes: regex metacharacters "?[](){}\\*+^$."
 *    - Consecutive number fields: parser.isNextNumber() (e.g., "yyyyMMdd")
 * 2. Strategy mappings:
 *    - Short vs long year: 'y'/'yy' (ABBREVIATED_YEAR_STRATEGY) vs 'yyyy' (LITERAL_YEAR_STRATEGY)
 *    - Short vs long month: 'M'/'MM' (NUMBER_MONTH_STRATEGY) vs 'MMM'/'MMMM' (TEXT_MONTH_STRATEGY)
 *    - Modulo calculations: 'H' (MODULO_HOUR_OF_DAY_STRATEGY % 24), 'h' (MODULO_HOUR_STRATEGY % 12)
 *    - CopyQuotedStrategy.isNumber() for digit literals vs quotes vs non-digits
 * 3. TimeZoneStrategy:
 *    - Prefixes: "+HHmm", "-HHmm", "GMT+HH:mm", named time zones (e.g., "UTC", "PST", "EST")
 * 4. Locale & Calendar handling:
 *    - Standard Gregorian vs JAPANESE_IMPERIAL locale
 *    - Japanese imperial era checks and exception message on parse failure
 * 5. Lifecycle & Contracts:
 *    - equals() / hashCode() with same, different pattern, different TZ, different locale, non-FastDateParser, null
 *    - toString() output structure
 *    - Java Serialization round-trip (readObject transient state restoration)
 *
 * Known Defect Target (LANG-831):
 * - SimpleDateFormat treats literal whitespace strictly, whereas FastDateParser incorrectly uses "\\s*+",
 *   allowing multiple spaces or mismatched whitespace to parse where SimpleDateFormat fails with null.
 * - Test: testLANG_831_WhitespaceMismatch reveals the defect when comparing against SimpleDateFormat parse behavior.
 */
public class FastDateParserGeminiTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone EST = TimeZone.getTimeZone("America/New_York");
    private static final Locale US = Locale.US;

    // =========================================================================
    // PARTITION A: Core Functional Logic & Strategy Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCorePatternsAllStrategies() throws ParseException {
        // Test combining various standard format symbols
        String pattern = "G yyyy M d H m s S E D F w W a k K z";
        FastDateParser parser = new FastDateParser(pattern, GMT, US);

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.clear();
        cal.set(Calendar.ERA, Calendar.AD);
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.OCTOBER);
        cal.set(Calendar.DAY_OF_MONTH, 15);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 45);
        cal.set(Calendar.MILLISECOND, 500);

        // Parse matching string
        String source = "AD 2023 10 15 14 30 45 500 Sun 288 3 42 3 PM 14 2 GMT";
        Date parsed = parser.parse(source);
        assertNotNull("Date should be parsed", parsed);

        Calendar resultCal = Calendar.getInstance(GMT, US);
        resultCal.setTime(parsed);
        assertEquals(2023, resultCal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, resultCal.get(Calendar.MONTH));
        assertEquals(15, resultCal.get(Calendar.DAY_OF_MONTH));
        assertEquals(30, resultCal.get(Calendar.MINUTE));
        assertEquals(45, resultCal.get(Calendar.SECOND));
        assertEquals(500, resultCal.get(Calendar.MILLISECOND));
    }

    @Test(timeout = 4000)
    public void testContiguousNumericFields() throws ParseException {
        // Targets isNextNumber() == true branch: regex with fixed field width
        FastDateParser parser = new FastDateParser("yyyyMMddHHmmss", GMT, US);
        Date date = parser.parse("20231124153045");
        assertNotNull("Contiguous numeric date must parse", date);

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH));
        assertEquals(24, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(45, cal.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testTextMonthAndDayOfWeek() throws ParseException {
        // Targets TEXT_MONTH_STRATEGY and DAY_OF_WEEK_STRATEGY
        FastDateParser parser = new FastDateParser("MMMM EEEE d, yyyy", GMT, US);
        Date date = parser.parse("September Wednesday 20, 2023");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testAbbreviatedMonthAndDayOfWeek() throws ParseException {
        FastDateParser parser = new FastDateParser("MMM E d, yyyy", GMT, US);
        Date date = parser.parse("Sep Wed 20, 2023");
        assertNotNull(date);

        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.SEPTEMBER, cal.get(Calendar.MONTH));
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testAbbreviatedTwoDigitYearAdjustment() throws ParseException {
        FastDateParser parser = new FastDateParser("yy-MM-dd", GMT, US);
        Calendar currentCal = Calendar.getInstance(GMT, US);
        int currentCentury = currentCal.get(Calendar.YEAR) - (currentCal.get(Calendar.YEAR) % 100);

        Date d1 = parser.parse("20-01-01");
        Calendar c1 = Calendar.getInstance(GMT, US);
        c1.setTime(d1);
        int year1 = c1.get(Calendar.YEAR);
        assertTrue(year1 >= currentCentury - 80 && year1 < currentCentury + 120);

        Date d2 = parser.parse("85-01-01");
        Calendar c2 = Calendar.getInstance(GMT, US);
        c2.setTime(d2);
        int year2 = c2.get(Calendar.YEAR);
        assertTrue(year2 >= currentCentury - 80 && year2 < currentCentury + 120);
    }

    @Test(timeout = 4000)
    public void testModuloHourStrategies() throws ParseException {
        // 'H' modulo 24 and 'h' modulo 12
        FastDateParser parserH = new FastDateParser("yyyy-MM-dd H", GMT, US);
        Date date24 = parserH.parse("2023-01-01 24");
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date24);
        assertEquals("24 hours should be 0 mod 24", 0, cal.get(Calendar.HOUR_OF_DAY));

        FastDateParser parserh = new FastDateParser("yyyy-MM-dd h a", GMT, US);
        Date date12 = parserh.parse("2023-01-01 12 AM");
        cal.setTime(date12);
        assertEquals("12 AM should be 0 mod 12", 0, cal.get(Calendar.HOUR));
    }

    @Test(timeout = 4000)
    public void testTimeZoneStrategies() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm z", GMT, US);

        Date dGmt = parser.parse("2023-01-01 12:00 GMT");
        assertNotNull(dGmt);

        Date dOffset1 = parser.parse("2023-01-01 12:00 +0530");
        assertNotNull(dOffset1);

        Date dOffset2 = parser.parse("2023-01-01 12:00 -0800");
        assertNotNull(dOffset2);

        Date dGmtOffset = parser.parse("2023-01-01 12:00 GMT+02:00");
        assertNotNull(dGmtOffset);

        Date dNamed = parser.parse("2023-01-01 12:00 UTC");
        assertNotNull(dNamed);
    }

    @Test(timeout = 4000)
    public void testZoneStrategyRFC822() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd HH:mm Z", GMT, US);
        Date date = parser.parse("2023-01-01 12:00 -0500");
        assertNotNull(date);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRegexSpecialCharactersInPattern() throws ParseException {
        // Checks escapeRegex handling all special metacharacters: ? [ ] ( ) { } \ | * + ^ $ .
        String pattern = "yyyy.MM.dd '?' '[' ']' '(' ')' '{' '}' '\\' '|' '*' '+' '^' '$'";
        FastDateParser parser = new FastDateParser(pattern, GMT, US);
        String input = "2023.10.15 ? [ ] ( ) { } \\ | * + ^ $";
        Date date = parser.parse(input);
        assertNotNull("Should parse escaped regex metacharacters cleanly", date);
    }

    @Test(timeout = 4000)
    public void testQuotedLiteralsAndTwoSingleQuotes() throws ParseException {
        // Tests '' (two single quotes -> literal single quote) and quoted literals
        FastDateParser parser = new FastDateParser("yyyy 'o''clock' ''MM''", GMT, US);
        Date date = parser.parse("2023 o'clock '10'");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
    }

    @Test(timeout = 4000)
    public void testQuotedDigitInPattern() throws ParseException {
        // Quoted literal starting with digit exercises CopyQuotedStrategy.isNumber()
        FastDateParser parser = new FastDateParser("yyyy'3'MMdd", GMT, US);
        Date date = parser.parse("202331015");
        assertNotNull(date);
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(date);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test(timeout = 4000)
    public void testParseWithPositionOffset() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        String input = "Prefix 2023-10-15 Suffix";
        ParsePosition pos = new ParsePosition(7);
        Date date = parser.parse(input, pos);
        assertNotNull("Should parse from offset", date);
        assertEquals("Offset should advance to end of match", 17, pos.getIndex());

        // Parse with invalid offset returning null
        ParsePosition invalidPos = new ParsePosition(0);
        Date invalidDate = parser.parse(input, invalidPos);
        assertNull("Should return null when match fails at offset", invalidDate);
        assertEquals(0, invalidPos.getIndex());
    }

    @Test(timeout = 4000)
    public void testParseObjectVariants() throws ParseException {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        Object obj1 = parser.parseObject("2023-10-15");
        assertTrue("parseObject(String) must return a Date", obj1 instanceof Date);

        ParsePosition pos = new ParsePosition(0);
        Object obj2 = parser.parseObject("2023-10-15", pos);
        assertTrue("parseObject(String, ParsePosition) must return a Date", obj2 instanceof Date);
        assertEquals(10, pos.getIndex());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Zone (Defects4J LANG-831)
    // =========================================================================

    /**
     * Target Defect: LANG-831.
     * Pattern: "M E"
     * Input: "3  Tue" (two spaces between the tokens)
     * SimpleDateFormat fails to parse this input because " " in the pattern matches
     * strictly one space, returning null.
     * Defective FastDateParser treats whitespace as \s*+, incorrectly swallowing multiple
     * spaces and producing a non-null Date when it should fail.
     */
    @Test(timeout = 4000)
    public void testLANG_831_WhitespaceMismatch() {
        String pattern = "M E";
        String input = "3  Tue";

        SimpleDateFormat sdf = new SimpleDateFormat(pattern, US);
        FastDateParser fdp = new FastDateParser(pattern, GMT, US);

        ParsePosition sdfPos = new ParsePosition(0);
        Date sdfDate = sdf.parse(input, sdfPos);

        ParsePosition fdpPos = new ParsePosition(0);
        Date fdpDate = fdp.parse(input, fdpPos);

        // FastDateParser must conform to SimpleDateFormat behavior
        assertEquals("Parsed date must match SimpleDateFormat result (LANG-831)", sdfDate, fdpDate);
        assertEquals("Parse index must match SimpleDateFormat result (LANG-831)", sdfPos.getIndex(), fdpPos.getIndex());
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyPatternThrowsIllegalArgumentException() {
        // Matcher.lookingAt() fails on empty pattern
        new FastDateParser("", GMT, US);
    }

    @Test(timeout = 4000)
    public void testUnparseableDateThrowsParseException() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        try {
            parser.parse("Not-A-Date");
            fail("Expected ParseException on unparseable input");
        } catch (ParseException expected) {
            assertTrue("ParseException message should contain unparseable date info",
                    expected.getMessage().contains("Unparseable date"));
            assertEquals(0, expected.getErrorOffset());
        }
    }

    @Test(timeout = 4000)
    public void testJapaneseImperialLocaleExceptionMessage() {
        // FastDateParser special-cases JAPANESE_IMPERIAL in parse error messages
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, FastDateParser.JAPANESE_IMPERIAL);
        try {
            parser.parse("InvalidJapaneseDate");
            fail("Expected ParseException for Japanese imperial invalid date");
        } catch (ParseException expected) {
            assertTrue("Message should document Japanese Imperial limitation before 1868 AD",
                    expected.getMessage().contains("does not support dates before 1868 AD"));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFieldInGetDisplayNames() {
        FastDateParser parser = new FastDateParser("yyyy-MM-dd", GMT, US);
        // Calendar.YEAR (1) is not supported by getDisplayNames switch
        parser.getDisplayNames(Calendar.YEAR);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle, Contract Integrity & Serialization
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        FastDateParser p1 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser p2 = new FastDateParser("yyyy-MM-dd", GMT, US);
        FastDateParser pDiffPattern = new FastDateParser("yyyy/MM/dd", GMT, US);
        FastDateParser pDiffTz = new FastDateParser("yyyy-MM-dd", EST, US);
        FastDateParser pDiffLocale = new FastDateParser("yyyy-MM-dd", GMT, Locale.GERMANY);

        // Reflexive
        assertEquals(p1, p1);
        // Symmetric
        assertEquals(p1, p2);
        assertEquals(p2, p1);
        assertEquals(p1.hashCode(), p2.hashCode());

        // Asymmetric / Incompatible
        assertNotEquals(p1, null);
        assertNotEquals(p1, "not-a-fast-date-parser");
        assertNotEquals(p1, pDiffPattern);
        assertNotEquals(p1, pDiffTz);
        assertNotEquals(p1, pDiffLocale);
    }

    @Test(timeout = 4000)
    public void testGettersAndToString() {
        String pattern = "yyyy-MM-dd HH:mm";
        FastDateParser parser = new FastDateParser(pattern, EST, US);

        assertEquals(pattern, parser.getPattern());
        assertEquals(EST, parser.getTimeZone());
        assertEquals(US, parser.getLocale());
        assertNotNull(parser.getParsePattern());

        String toString = parser.toString();
        assertTrue("toString must start with FastDateParser[", toString.startsWith("FastDateParser["));
        assertTrue("toString must contain pattern", toString.contains(pattern));
        assertTrue("toString must contain locale", toString.contains(US.toString()));
        assertTrue("toString must contain timezone ID", toString.contains(EST.getID()));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        FastDateParser original = new FastDateParser("yyyy-MM-dd HH:mm:ss", GMT, US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        FastDateParser deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            deserialized = (FastDateParser) ois.readObject();
        }

        assertNotNull("Deserialized object must not be null", deserialized);
        assertEquals("Deserialized instance must be equal to original", original, deserialized);
        assertEquals("HashCode must match", original.hashCode(), deserialized.hashCode());

        // Validate transient fields were reinitialized properly via init() in readObject
        Date parsed = deserialized.parse("2023-12-25 10:20:30");
        assertNotNull("Deserialized parser must successfully parse dates", parsed);
        Calendar cal = Calendar.getInstance(GMT, US);
        cal.setTime(parsed);
        assertEquals(2023, cal.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH));
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(20, cal.get(Calendar.MINUTE));
        assertEquals(30, cal.get(Calendar.SECOND));
    }
}