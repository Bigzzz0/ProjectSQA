package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang.time.FastDateFormat
 * Target Defect (LANG-303): Serialization failure with java.io.NotSerializableException on inner Rule
 * classes (specifically PaddedNumberField and other Rule implementations used in mRules).
 *
 * Key Decision Branches & Partitions:
 * - Partition A: Core Functional Logic & Pattern Rules
 *   - Pattern characters: 'G' (era), 'y' (year >=4 padded vs <4 two-digit), 'M' (1, 2, 3, >=4),
 *     'd' (day of month 1, 2), 'h' (12-hour: 0 -> 12), 'H' (24-hour: 0..23), 'm' (minute), 's' (second),
 *     'S' (millisecond: padding <3, 3, >3), 'E' (day of week: short vs full), 'D' (day of year),
 *     'F' (day of week in month), 'w' (week of year), 'W' (week of month), 'a' (AM/PM marker),
 *     'k' (hour in day 1..24: 0 -> 24), 'K' (hour in am/pm 0..11), 'z' (time zone text: short vs long),
 *     'Z' (RFC822 vs ISO8601 with colon), quotes & escaped quotes ('', 'literal', mixed).
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Number padding boundary: value < 10, 10 <= value < 100, 100 <= value < 1000, value >= 1000.
 *   - TimeZone offsets: positive vs negative UTC offsets, daylight saving transitions.
 *   - Hours: 0 AM -> 12, 0 HR 24-hour clock -> 24.
 *   - Milliseconds: 0..9, 10..99, 100..999.
 * - Partition C: Defect-Targeted Zone (LANG-303)
 *   - Serialization round-trip of FastDateFormat with padded fields (e.g. "yyyy-MM-dd HH:mm:ss.SSS Z").
 *   - Verifying serialization integrity of rules and proper state restoration.
 * - Partition D: Exception & Defensive Guard Paths
 *   - Null pattern in constructor / factory.
 *   - Illegal pattern character (e.g. 'X', 'Q', '?').
 *   - Invalid object passed to format(Object, StringBuffer, FieldPosition).
 *   - parseObject contract verification (always returns null).
 * - Partition E: Object Lifecycle & Contract Integrity
 *   - equals, hashCode, toString, factory caching semantics.
 */
public class FastDateFormatGeminiTest {

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final TimeZone PST = TimeZone.getTimeZone("PST");
    private static final TimeZone NEW_YORK = TimeZone.getTimeZone("America/New_York");

    // =========================================================================
    // Partition A: Core Functional Logic & Pattern Rules
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatAllCorePatterns() {
        // Construct calendar: 2004-01-05 00:04:05.006 GMT
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.clear();
        cal.set(2004, Calendar.JANUARY, 5, 0, 4, 5);
        cal.set(Calendar.MILLISECOND, 6);

        String pattern = "G yyyy yy MMMM MMM MM M d h H m s S EEEE E D F w W a k K";
        FastDateFormat fdf = FastDateFormat.getInstance(pattern, GMT, Locale.US);

        String result = fdf.format(cal);
        assertNotNull(result);
        assertTrue(result.contains("AD"));
        assertTrue(result.contains("2004"));
        assertTrue(result.contains("04"));
        assertTrue(result.contains("January"));
        assertTrue(result.contains("Jan"));
        assertTrue(result.contains("Monday"));
        assertTrue(result.contains("Mon"));
        assertTrue(result.contains("AM"));

        // Verify specific field values
        // h at 00:04 should be 12
        FastDateFormat fHour12 = FastDateFormat.getInstance("h", GMT, Locale.US);
        assertEquals("12", fHour12.format(cal));

        // k at 00:04 should be 24
        FastDateFormat fHour24 = FastDateFormat.getInstance("k", GMT, Locale.US);
        assertEquals("24", fHour24.format(cal));

        // K at 00:04 should be 0
        FastDateFormat fHourK = FastDateFormat.getInstance("K", GMT, Locale.US);
        assertEquals("0", fHourK.format(cal));

        // H at 00:04 should be 0
        FastDateFormat fHourH = FastDateFormat.getInstance("H", GMT, Locale.US);
        assertEquals("0", fHourH.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatHourBoundariesAtNoonAndMidnight() {
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.clear();
        cal.set(2020, Calendar.JULY, 15, 12, 30, 0);

        FastDateFormat f12 = FastDateFormat.getInstance("h", GMT, Locale.US);
        FastDateFormat f24 = FastDateFormat.getInstance("k", GMT, Locale.US);
        FastDateFormat fK = FastDateFormat.getInstance("K", GMT, Locale.US);
        FastDateFormat fH = FastDateFormat.getInstance("H", GMT, Locale.US);

        assertEquals("12", f12.format(cal));
        assertEquals("12", f24.format(cal));
        assertEquals("0", fK.format(cal));
        assertEquals("12", fH.format(cal));

        cal.set(Calendar.HOUR_OF_DAY, 23);
        assertEquals("11", f12.format(cal));
        assertEquals("23", f24.format(cal));
        assertEquals("11", fK.format(cal));
        assertEquals("23", fH.format(cal));
    }

    @Test(timeout = 4000)
    public void testLiteralQuotesInPattern() {
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.clear();
        cal.set(2023, Calendar.MARCH, 10);

        // Pattern with escaped single quote and string literal
        FastDateFormat fdf1 = FastDateFormat.getInstance("''yyyy'' 'year' ''", GMT, Locale.US);
        assertEquals("'2023' year '", fdf1.format(cal));

        // Pattern with single character literal
        FastDateFormat fdf2 = FastDateFormat.getInstance("'T'HH:mm:ss'Z'", GMT, Locale.US);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 15);
        cal.set(Calendar.SECOND, 30);
        assertEquals("T08:15:30Z", fdf2.format(cal));

        // Pattern with complex text token
        FastDateFormat fdf3 = FastDateFormat.getInstance("yyyy 'o''clock' a", GMT, Locale.US);
        assertEquals("2023 o'clock AM", fdf3.format(cal));
    }

    @Test(timeout = 4000)
    public void testTimeZoneRuleFormatting() {
        // Test RFC822 (Z) and ISO8601 (ZZ)
        SimpleTimeZone tzPlus530 = new SimpleTimeZone(19800000, "Asia/Kolkata");
        SimpleTimeZone tzMinus8 = new SimpleTimeZone(-28800000, "GMT-8");

        Calendar calPlus = new GregorianCalendar(tzPlus530, Locale.US);
        calPlus.clear();
        calPlus.set(2021, Calendar.JUNE, 1, 10, 0, 0);

        Calendar calMinus = new GregorianCalendar(tzMinus8, Locale.US);
        calMinus.clear();
        calMinus.set(2021, Calendar.JUNE, 1, 10, 0, 0);

        FastDateFormat fdfZ = FastDateFormat.getInstance("Z", tzPlus530, Locale.US);
        FastDateFormat fdfZZ = FastDateFormat.getInstance("ZZ", tzPlus530, Locale.US);
        assertEquals("+0530", fdfZ.format(calPlus));
        assertEquals("+05:30", fdfZZ.format(calPlus));

        FastDateFormat fdfNegZ = FastDateFormat.getInstance("Z", tzMinus8, Locale.US);
        FastDateFormat fdfNegZZ = FastDateFormat.getInstance("ZZ", tzMinus8, Locale.US);
        assertEquals("-0800", fdfNegZ.format(calMinus));
        assertEquals("-08:00", fdfNegZZ.format(calMinus));

        // Test time zone text short and long
        FastDateFormat fdfzShort = FastDateFormat.getInstance("z", PST, Locale.US);
        FastDateFormat fdfzLong = FastDateFormat.getInstance("zzzz", PST, Locale.US);
        String shortName = fdfzShort.format(calMinus);
        String longName = fdfzLong.format(calMinus);
        assertNotNull(shortName);
        assertNotNull(longName);
        assertFalse(shortName.isEmpty());
        assertFalse(longName.isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPaddedNumberFieldBoundaries() {
        // Test 1-digit, 2-digit, 3-digit, 4-digit numbers with SSS (millisecond padding = 3)
        FastDateFormat fdf3 = FastDateFormat.getInstance("SSS", GMT, Locale.US);
        FastDateFormat fdf5 = FastDateFormat.getInstance("SSSSS", GMT, Locale.US);

        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.clear();

        cal.set(Calendar.MILLISECOND, 7);
        assertEquals("007", fdf3.format(cal));
        assertEquals("00007", fdf5.format(cal));

        cal.set(Calendar.MILLISECOND, 85);
        assertEquals("085", fdf3.format(cal));
        assertEquals("00085", fdf5.format(cal));

        cal.set(Calendar.MILLISECOND, 789);
        assertEquals("789", fdf3.format(cal));
        assertEquals("00789", fdf5.format(cal));

        // Test year >= 1000 with yyyyy (size 5)
        FastDateFormat fdfY5 = FastDateFormat.getInstance("yyyyy", GMT, Locale.US);
        cal.set(Calendar.YEAR, 2024);
        assertEquals("02024", fdfY5.format(cal));
        cal.set(Calendar.YEAR, 99);
        assertEquals("00099", fdfY5.format(cal));
        cal.set(Calendar.YEAR, 5);
        assertEquals("00005", fdfY5.format(cal));
        cal.set(Calendar.YEAR, 12345);
        assertEquals("12345", fdfY5.format(cal));
    }

    @Test(timeout = 4000)
    public void testUnpaddedAndTwoDigitBoundaries() {
        // Month boundary: unpadded M (1..12) vs MM (01..12)
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.clear();

        FastDateFormat fdfM = FastDateFormat.getInstance("M", GMT, Locale.US);
        FastDateFormat fdfMM = FastDateFormat.getInstance("MM", GMT, Locale.US);

        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        assertEquals("2", fdfM.format(cal));
        assertEquals("02", fdfMM.format(cal));

        cal.set(Calendar.MONTH, Calendar.NOVEMBER);
        assertEquals("11", fdfM.format(cal));
        assertEquals("11", fdfMM.format(cal));

        // Day of month boundary: d vs dd
        FastDateFormat fdfD = FastDateFormat.getInstance("d", GMT, Locale.US);
        FastDateFormat fdfDD = FastDateFormat.getInstance("dd", GMT, Locale.US);

        cal.set(Calendar.DAY_OF_MONTH, 9);
        assertEquals("9", fdfD.format(cal));
        assertEquals("09", fdfDD.format(cal));

        cal.set(Calendar.DAY_OF_MONTH, 28);
        assertEquals("28", fdfD.format(cal));
        assertEquals("28", fdfDD.format(cal));
    }

    @Test(timeout = 4000)
    public void testFormatMethodsOverloads() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", GMT, Locale.US);
        long millis = 1577836800000L; // 2020-01-01 00:00:00 GMT
        Date date = new Date(millis);
        Calendar cal = new GregorianCalendar(GMT, Locale.US);
        cal.setTimeInMillis(millis);

        // format(long)
        assertEquals("2020-01-01 00:00:00", fdf.format(millis));

        // format(Date)
        assertEquals("2020-01-01 00:00:00", fdf.format(date));

        // format(Calendar)
        assertEquals("2020-01-01 00:00:00", fdf.format(cal));

        // format(long, StringBuffer)
        StringBuffer buf1 = new StringBuffer("Time: ");
        assertSame(buf1, fdf.format(millis, buf1));
        assertEquals("Time: 2020-01-01 00:00:00", buf1.toString());

        // format(Date, StringBuffer)
        StringBuffer buf2 = new StringBuffer("Date: ");
        assertSame(buf2, fdf.format(date, buf2));
        assertEquals("Date: 2020-01-01 00:00:00", buf2.toString());

        // format(Calendar, StringBuffer)
        StringBuffer buf3 = new StringBuffer("Cal: ");
        assertSame(buf3, fdf.format(cal, buf3));
        assertEquals("Cal: 2020-01-01 00:00:00", buf3.toString());

        // format(Object, StringBuffer, FieldPosition)
        StringBuffer buf4 = new StringBuffer();
        assertSame(buf4, fdf.format(new Long(millis), buf4, new FieldPosition(0)));
        assertEquals("2020-01-01 00:00:00", buf4.toString());

        StringBuffer buf5 = new StringBuffer();
        assertSame(buf5, fdf.format(date, buf5, new FieldPosition(0)));
        assertEquals("2020-01-01 00:00:00", buf5.toString());

        StringBuffer buf6 = new StringBuffer();
        assertSame(buf6, fdf.format(cal, buf6, new FieldPosition(0)));
        assertEquals("2020-01-01 00:00:00", buf6.toString());
    }

    @Test(timeout = 4000)
    public void testCalendarWithOverriddenTimeZone() {
        FastDateFormat forced = FastDateFormat.getInstance("HH:mm", PST, Locale.US);
        assertTrue(forced.getTimeZoneOverridesCalendar());

        Calendar calUtc = new GregorianCalendar(UTC, Locale.US);
        calUtc.clear();
        calUtc.set(2022, Calendar.JANUARY, 1, 18, 0, 0); // 18:00 UTC = 10:00 PST

        // Calendar should be converted to PST because timeZone was explicitly forced
        assertEquals("10:00", forced.format(calUtc));

        // Format where TimeZone is null initially -> not forced
        FastDateFormat unforced = FastDateFormat.getInstance("HH:mm");
        assertFalse(unforced.getTimeZoneOverridesCalendar());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (LANG-303)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang303SerializationRoundTrip() throws Exception {
        // Defect LANG-303: FastDateFormat inner Rule classes (e.g., PaddedNumberField)
        // are not Serializable, causing NotSerializableException when serialized.
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy/MM/dd HH:mm:ss.SSS Z", Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.flush();
        oos.close();

        byte[] bytes = baos.toByteArray();
        assertTrue("Serialized stream should contain data", bytes.length > 0);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized object must not be null", deserialized);
        assertTrue("Deserialized object must be FastDateFormat", deserialized instanceof FastDateFormat);
        FastDateFormat deserializedFdf = (FastDateFormat) deserialized;

        assertEquals(fdf, deserializedFdf);
        assertEquals(fdf.getPattern(), deserializedFdf.getPattern());
        assertEquals(fdf.getTimeZone(), deserializedFdf.getTimeZone());
        assertEquals(fdf.getLocale(), deserializedFdf.getLocale());

        // Verify deserialized instance can format accurately
        Date testDate = new Date(1577836800000L);
        assertEquals(fdf.format(testDate), deserializedFdf.format(testDate));
    }

    @Test(timeout = 4000)
    public void testSerializationWithTimeZoneNameRules() throws Exception {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd zzzz", NEW_YORK, Locale.US);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(fdf);
        oos.flush();
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FastDateFormat deserialized = (FastDateFormat) ois.readObject();
        ois.close();

        assertEquals(fdf, deserialized);
        Date testDate = new Date(1577836800000L);
        assertEquals(fdf.format(testDate), deserialized.format(testDate));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPatternThrowsException() {
        FastDateFormat.getInstance(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPatternInConstructorThrowsException() {
        new FastDateFormat(null, GMT, Locale.US) {};
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPatternCharacterThrowsException() {
        FastDateFormat.getInstance("yyyy-MM-dd ? HH:mm");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnsupportedPatternLetterThrowsException() {
        FastDateFormat.getInstance("yyyy-MM-dd X");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnknownObjectThrowsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format("2020-01-01", new StringBuffer(), new FieldPosition(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatNullObjectThrowsException() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        fdf.format((Object) null, new StringBuffer(), new FieldPosition(0));
    }

    @Test(timeout = 4000)
    public void testParseObjectContract() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
        ParsePosition pos = new ParsePosition(0);
        Object result = fdf.parseObject("2020-01-01", pos);
        assertNull("FastDateFormat.parseObject should always return null (unsupported)", result);
        assertEquals(0, pos.getIndex());
        assertEquals(0, pos.getErrorIndex());
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Caching & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryCachingAndEquality() {
        FastDateFormat instance1 = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.US);
        FastDateFormat instance2 = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.US);
        assertSame("Cached instances should return identical references", instance1, instance2);
        assertEquals(instance1, instance2);
        assertEquals(instance1.hashCode(), instance2.hashCode());

        FastDateFormat instanceDiffTz = FastDateFormat.getInstance("yyyy-MM-dd", PST, Locale.US);
        assertNotEquals(instance1, instanceDiffTz);

        FastDateFormat instanceDiffLocale = FastDateFormat.getInstance("yyyy-MM-dd", GMT, Locale.FRANCE);
        assertNotEquals(instance1, instanceDiffLocale);

        FastDateFormat instanceDiffPattern = FastDateFormat.getInstance("yyyy/MM/dd", GMT, Locale.US);
        assertNotEquals(instance1, instanceDiffPattern);

        assertFalse(instance1.equals(null));
        assertFalse(instance1.equals("Not a FastDateFormat"));
    }

    @Test(timeout = 4000)
    public void testDateAndTimeStyleFactories() {
        // Test getDateInstance styles
        FastDateFormat dateFull = FastDateFormat.getDateInstance(FastDateFormat.FULL, GMT, Locale.US);
        FastDateFormat dateLong = FastDateFormat.getDateInstance(FastDateFormat.LONG, GMT, Locale.US);
        FastDateFormat dateMed = FastDateFormat.getDateInstance(FastDateFormat.MEDIUM, GMT, Locale.US);
        FastDateFormat dateShort = FastDateFormat.getDateInstance(FastDateFormat.SHORT, GMT, Locale.US);

        assertNotNull(dateFull);
        assertNotNull(dateLong);
        assertNotNull(dateMed);
        assertNotNull(dateShort);

        // Factory overloads for getDateInstance
        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.SHORT));
        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.SHORT, Locale.US));
        assertNotNull(FastDateFormat.getDateInstance(FastDateFormat.SHORT, GMT));

        // Test getTimeInstance styles
        FastDateFormat timeFull = FastDateFormat.getTimeInstance(FastDateFormat.FULL, GMT, Locale.US);
        FastDateFormat timeLong = FastDateFormat.getTimeInstance(FastDateFormat.LONG, GMT, Locale.US);
        FastDateFormat timeMed = FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM, GMT, Locale.US);
        FastDateFormat timeShort = FastDateFormat.getTimeInstance(FastDateFormat.SHORT, GMT, Locale.US);

        assertNotNull(timeFull);
        assertNotNull(timeLong);
        assertNotNull(timeMed);
        assertNotNull(timeShort);

        // Factory overloads for getTimeInstance
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.SHORT));
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.SHORT, Locale.US));
        assertNotNull(FastDateFormat.getTimeInstance(FastDateFormat.SHORT, GMT));

        // Test getDateTimeInstance styles & overloads
        FastDateFormat dtInstance = FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, GMT, Locale.US);
        assertNotNull(dtInstance);
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT));
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, Locale.US));
        assertNotNull(FastDateFormat.getDateTimeInstance(FastDateFormat.SHORT, FastDateFormat.SHORT, GMT));
    }

    @Test(timeout = 4000)
    public void testGetDefaultInstance() {
        FastDateFormat defaultFdf = FastDateFormat.getInstance();
        assertNotNull(defaultFdf);
        assertNotNull(defaultFdf.getPattern());
        assertEquals(Locale.getDefault(), defaultFdf.getLocale());
        assertFalse(defaultFdf.getTimeZoneOverridesCalendar());

        FastDateFormat patternOnly = FastDateFormat.getInstance("yyyy-MM-dd");
        assertNotNull(patternOnly);
        assertEquals("yyyy-MM-dd", patternOnly.getPattern());

        FastDateFormat patternAndTz = FastDateFormat.getInstance("yyyy-MM-dd", GMT);
        assertNotNull(patternAndTz);
        assertEquals(GMT, patternAndTz.getTimeZone());

        FastDateFormat patternAndLocale = FastDateFormat.getInstance("yyyy-MM-dd", Locale.GERMAN);
        assertNotNull(patternAndLocale);
        assertEquals(Locale.GERMAN, patternAndLocale.getLocale());
    }

    @Test(timeout = 4000)
    public void testGettersAndToString() {
        FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", GMT, Locale.UK);
        assertEquals("yyyy-MM-dd HH:mm:ss", fdf.getPattern());
        assertEquals(GMT, fdf.getTimeZone());
        assertEquals(Locale.UK, fdf.getLocale());
        assertTrue(fdf.getTimeZoneOverridesCalendar());
        assertTrue(fdf.getMaxLengthEstimate() >= 19);
        assertEquals("FastDateFormat[yyyy-MM-dd HH:mm:ss]", fdf.toString());
    }

    @Test(timeout = 4000)
    public void testTimeZoneDisplayKeyAndCache() {
        // Direct test for static getTimeZoneDisplay helper via short and long formats
        FastDateFormat fdfShort = FastDateFormat.getInstance("z", NEW_YORK, Locale.US);
        FastDateFormat fdfLong = FastDateFormat.getInstance("zzzz", NEW_YORK, Locale.US);

        Calendar standardCal = new GregorianCalendar(NEW_YORK, Locale.US);
        standardCal.clear();
        standardCal.set(2021, Calendar.JANUARY, 15, 12, 0, 0); // Standard time (EST)

        Calendar daylightCal = new GregorianCalendar(NEW_YORK, Locale.US);
        daylightCal.clear();
        daylightCal.set(2021, Calendar.JULY, 15, 12, 0, 0); // Daylight time (EDT)

        String stdShort = fdfShort.format(standardCal);
        String dstShort = fdfShort.format(daylightCal);
        assertNotEquals(stdShort, dstShort);

        String stdLong = fdfLong.format(standardCal);
        String dstLong = fdfLong.format(daylightCal);
        assertNotEquals(stdLong, dstLong);
    }
}