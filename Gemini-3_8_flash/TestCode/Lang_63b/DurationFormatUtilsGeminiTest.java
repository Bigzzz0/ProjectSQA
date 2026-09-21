package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.TimeZone;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.time.DurationFormatUtils
 * Defects4J Defect: LANG-281 (testJiraLang281)
 * Failure Mechanism: In formatPeriod(), reduceAndCorrect() improperly adjusts Calendar.MONTH when
 * calculating differences across year boundaries with end days smaller than start days.
 * For example: 2005-12-31 to 2006-10-06 requested as "MM" computes negative months (-2) instead of "09"
 * because end.add(Calendar.MONTH, -9) shifts the month into January (0), causing startValue (11) - endValue (0)
 * = 11 to be subtracted from months (9 - 11 = -2).
 *
 * Decision / Branch Coverage Targets:
 * - formatDurationHMS: delegation to formatDuration with "H:mm:ss.SSS"
 * - formatDurationISO: delegation to formatDuration with ISO_EXTENDED_FORMAT_PATTERN and padWithZeros=false
 * - formatDuration (millis, format, padWithZeros):
 *   * Token contains d, H, m, s, S (each present / absent)
 * - formatDurationWords:
 *   * suppressLeadingZeroElements: true/false with leading days, hours, minutes, seconds zero combinations
 *   * suppressTrailingZeroElements: true/false with trailing seconds, minutes, hours, days zero combinations
 *   * singular vs plural word formatting ("1 day" vs "2 days", "1 hour" vs "2 hours", etc.)
 *   * zero duration edge cases (suppress all -> empty string)
 * - formatPeriod (startMillis, endMillis, format, padWithZeros, timezone):
 *   * millis < 28 days branch vs millis >= 28 days branch
 *   * while loops for negative adjustments: milliseconds, seconds, minutes, hours, days, months
 *   * reduceAndCorrect: endValue < startValue (newdiff returned) vs endValue >= startValue (0 returned)
 *   * Token containment cascades:
 *     - !containsTokenWithValue(y) -> containsTokenWithValue(M) [months += 12*years] vs else [days += 365*years]
 *     - !containsTokenWithValue(M) -> days += end.DAY_OF_YEAR - start.DAY_OF_YEAR; months = 0
 *     - !containsTokenWithValue(d) -> hours += 24 * days; days = 0
 *     - !containsTokenWithValue(H) -> minutes += 60 * hours; hours = 0
 *     - !containsTokenWithValue(m) -> seconds += 60 * minutes; minutes = 0
 *     - !containsTokenWithValue(s) -> milliseconds += 1000 * seconds; seconds = 0
 * - format (tokens, years, months, days, hours, minutes, seconds, milliseconds, padWithZeros):
 *   * value instanceof StringBuffer (literal)
 *   * value == y, M, d, H, m, s, S
 *   * S preceded immediately by s (lastOutputSeconds=true -> ms += 1000, substring(1)) vs not preceded
 *   * padWithZeros true/false for each token type
 * - lexx:
 *   * inLiteral quotes ('...') escaping and literal text accumulation
 *   * consecutive identical tokens (token.increment()) vs new token transitions
 * - Token helper class:
 *   * equals: not Token, different class, different count, StringBuffer value, Number value, other Object
 *   * hashCode, toString, getCount, getValue
 *   * containsTokenWithValue: found vs not found
 * -------------------------------------------------------------------------------------------------------
 */
public class DurationFormatUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J LANG-281 Ground Truth)
    // =========================================================================

    /**
     * Exact recreation of Defects4J testJiraLang281.
     * Demonstrates the defect where calculating month durations across year boundaries
     * incorrectly yields a negative value ("-2") instead of "09".
     */
    @Test(timeout = 4000)
    public void testJiraLang281() {
        Calendar cal = Calendar.getInstance();
        cal.set(2005, Calendar.DECEMBER, 31, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Calendar cal2 = Calendar.getInstance();
        cal2.set(2006, Calendar.OCTOBER, 6, 0, 0, 0);
        cal2.set(Calendar.MILLISECOND, 0);

        String result = DurationFormatUtils.formatPeriod(cal.getTimeInMillis(), cal2.getTimeInMillis(), "MM");
        assertEquals("09", result);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        DurationFormatUtils instance = new DurationFormatUtils();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testFormatDurationHMS() {
        long duration = 3661001L; // 1 hr, 1 min, 1 sec, 1 ms
        String result = DurationFormatUtils.formatDurationHMS(duration);
        assertEquals("1:01:01.001", result);
    }

    @Test(timeout = 4000)
    public void testFormatDurationISO() {
        long duration = DateUtils.MILLIS_PER_DAY * 7 
                      + DateUtils.MILLIS_PER_HOUR * 6 
                      + DateUtils.MILLIS_PER_MINUTE * 5 
                      + DateUtils.MILLIS_PER_SECOND * 4 
                      + 321;
        String result = DurationFormatUtils.formatDurationISO(duration);
        assertEquals("P0Y0M7DT6H5M4.321S", result);
    }

    @Test(timeout = 4000)
    public void testFormatDurationSimplePaddedAndUnpadded() {
        long millis = DateUtils.MILLIS_PER_DAY * 2 
                    + DateUtils.MILLIS_PER_HOUR * 3 
                    + DateUtils.MILLIS_PER_MINUTE * 4 
                    + DateUtils.MILLIS_PER_SECOND * 5 
                    + 6;
        
        // Padded
        String padded = DurationFormatUtils.formatDuration(millis, "dd:HH:mm:ss:SSS", true);
        assertEquals("02:03:04:05:006", padded);

        // Unpadded
        String unpadded = DurationFormatUtils.formatDuration(millis, "d:H:m:s:S", false);
        assertEquals("2:3:4:5:6", unpadded);

        // Default 2-arg delegates to padded = true
        String defaultPad = DurationFormatUtils.formatDuration(millis, "dd:HH:mm:ss:SSS");
        assertEquals("02:03:04:05:006", defaultPad);
    }

    @Test(timeout = 4000)
    public void testFormatDurationWordsSingularAndPlural() {
        long oneUnit = DateUtils.MILLIS_PER_DAY 
                     + DateUtils.MILLIS_PER_HOUR 
                     + DateUtils.MILLIS_PER_MINUTE 
                     + DateUtils.MILLIS_PER_SECOND;
        String singular = DurationFormatUtils.formatDurationWords(oneUnit, false, false);
        assertEquals("1 day 1 hour 1 minute 1 second", singular);

        long multiUnit = DateUtils.MILLIS_PER_DAY * 2 
                       + DateUtils.MILLIS_PER_HOUR * 3 
                       + DateUtils.MILLIS_PER_MINUTE * 4 
                       + DateUtils.MILLIS_PER_SECOND * 5;
        String plural = DurationFormatUtils.formatDurationWords(multiUnit, false, false);
        assertEquals("2 days 3 hours 4 minutes 5 seconds", plural);
    }

    @Test(timeout = 4000)
    public void testFormatDurationWordsSuppressLeadingAndTrailing() {
        long millisOnlyHours = DateUtils.MILLIS_PER_HOUR * 2;

        // Suppress leading only
        String leadSuppressed = DurationFormatUtils.formatDurationWords(millisOnlyHours, true, false);
        assertEquals("2 hours 0 minutes 0 seconds", leadSuppressed);

        // Suppress trailing only
        String trailSuppressed = DurationFormatUtils.formatDurationWords(millisOnlyHours, false, true);
        assertEquals("0 days 2 hours", trailSuppressed);

        // Suppress both
        String bothSuppressed = DurationFormatUtils.formatDurationWords(millisOnlyHours, true, true);
        assertEquals("2 hours", bothSuppressed);

        // Suppress all for 0 millis
        String zeroSuppressed = DurationFormatUtils.formatDurationWords(0L, true, true);
        assertEquals("", zeroSuppressed);

        // Neither suppressed for 0 millis
        String zeroRetained = DurationFormatUtils.formatDurationWords(0L, false, false);
        assertEquals("0 days 0 hours 0 minutes 0 seconds", zeroRetained);
    }

    @Test(timeout = 4000)
    public void testFormatDurationWordsEdgeSuppressionPatterns() {
        // Only seconds present (leading zero days, hours, minutes suppressed)
        long millisSecondsOnly = DateUtils.MILLIS_PER_SECOND * 30;
        String resSec = DurationFormatUtils.formatDurationWords(millisSecondsOnly, true, false);
        assertEquals("30 seconds", resSec);

        // Only days present (trailing zero hours, minutes, seconds suppressed)
        long millisDaysOnly = DateUtils.MILLIS_PER_DAY * 5;
        String resDay = DurationFormatUtils.formatDurationWords(millisDaysOnly, false, true);
        assertEquals("5 days", resDay);
    }

    @Test(timeout = 4000)
    public void testFormatPeriodISO() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar start = Calendar.getInstance(tz);
        start.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance(tz);
        end.set(2021, Calendar.FEBRUARY, 2, 3, 4, 5);
        end.set(Calendar.MILLISECOND, 6);

        String result = DurationFormatUtils.formatPeriodISO(start.getTimeInMillis(), end.getTimeInMillis());
        assertNotNull(result);
        assertTrue(result.startsWith("P"));
    }

    @Test(timeout = 4000)
    public void testFormatPeriodShortDurationDelegates() {
        // Less than 28 days branches directly to formatDuration()
        long start = 1000000000L;
        long end = start + (5 * DateUtils.MILLIS_PER_DAY);
        String period = DurationFormatUtils.formatPeriod(start, end, "d' days'");
        assertEquals("5 days", period);

        String periodDefault = DurationFormatUtils.formatPeriod(start, end, "dd");
        assertEquals("05", periodDefault);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatDurationZero() {
        String result = DurationFormatUtils.formatDuration(0L, "d H m s S", false);
        assertEquals("0 0 0 0 0", result);
    }

    @Test(timeout = 4000)
    public void testFormatDurationNegativeOrSelectiveTokens() {
        long millis = 5000L; // 5 seconds
        // Only s token requested; others should be omitted
        String resultSecOnly = DurationFormatUtils.formatDuration(millis, "s", false);
        assertEquals("5", resultSecOnly);

        // Only S token requested
        String resultMillisOnly = DurationFormatUtils.formatDuration(millis, "S", false);
        assertEquals("5000", resultMillisOnly);

        // Only m token requested
        String resultMinOnly = DurationFormatUtils.formatDuration(60000L, "m", false);
        assertEquals("1", resultMinOnly);

        // Only H token requested
        String resultHourOnly = DurationFormatUtils.formatDuration(3600000L, "H", false);
        assertEquals("1", resultHourOnly);
    }

    @Test(timeout = 4000)
    public void testFormatPeriodWithNegativeFieldDifferences() {
        // Start date has higher time elements than end date (exercising negative while loops)
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar start = Calendar.getInstance(tz);
        start.set(2020, Calendar.JANUARY, 10, 23, 59, 59);
        start.set(Calendar.MILLISECOND, 999);

        Calendar end = Calendar.getInstance(tz);
        end.set(2020, Calendar.MARCH, 5, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);

        String result = DurationFormatUtils.formatPeriod(
                start.getTimeInMillis(), end.getTimeInMillis(), "y M d H m s S", false, tz);
        assertNotNull(result);
        assertFalse(result.contains("-"));
    }

    @Test(timeout = 4000)
    public void testFormatPeriodTokenOmissionBranches() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar start = Calendar.getInstance(tz);
        start.set(2018, Calendar.JANUARY, 1, 0, 0, 0);
        start.set(Calendar.MILLISECOND, 0);

        Calendar end = Calendar.getInstance(tz);
        end.set(2020, Calendar.JANUARY, 1, 0, 0, 0);
        end.set(Calendar.MILLISECOND, 0);

        // No y, has M -> months += 12 * years
        String resM = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "M", false, tz);
        assertEquals("24", resM);

        // No y, no M -> days += 365 * years (approx)
        String resD = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "d", false, tz);
        assertNotNull(resD);
        assertTrue(Integer.parseInt(resD) >= 730);

        // No d -> hours += 24 * days
        String resH = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "H", false, tz);
        assertTrue(Long.parseLong(resH) >= 730 * 24);

        // No H -> minutes += 60 * hours
        String resMin = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "m", false, tz);
        assertTrue(Long.parseLong(resMin) >= 730 * 24 * 60);

        // No m -> seconds += 60 * minutes
        String resSec = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "s", false, tz);
        assertTrue(Long.parseLong(resSec) >= 730 * 24 * 3600);

        // No s -> milliseconds += 1000 * seconds
        String resMs = DurationFormatUtils.formatPeriod(start.getTimeInMillis(), end.getTimeInMillis(), "S", false, tz);
        assertTrue(Long.parseLong(resMs) >= 730L * 24 * 3600 * 1000);
    }

    // =========================================================================
    // Partition D: Lexx, Format, and Internal Helper Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testLexxLiteralHandling() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("'Duration:' d 'days and' H 'hours'");
        assertNotNull(tokens);
        assertTrue(tokens.length >= 4);

        // Verify literal content
        assertEquals("Duration: ", tokens[0].getValue().toString());
        assertEquals(DurationFormatUtils.d, tokens[1].getValue());
        assertEquals(" days and ", tokens[2].getValue().toString());
        assertEquals(DurationFormatUtils.H, tokens[3].getValue());
    }

    @Test(timeout = 4000)
    public void testLexxAdjacentAndRepeatedTokens() {
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("yyyy-MM-dd HH:mm:ss.SSS");
        assertNotNull(tokens);

        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.y));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.M));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.d));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.H));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.m));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.s));
        assertTrue(DurationFormatUtils.Token.containsTokenWithValue(tokens, DurationFormatUtils.S));
        assertFalse(DurationFormatUtils.Token.containsTokenWithValue(tokens, "UNMATCHED"));

        for (DurationFormatUtils.Token token : tokens) {
            if (token.getValue() == DurationFormatUtils.y) {
                assertEquals(4, token.getCount());
            } else if (token.getValue() == DurationFormatUtils.S) {
                assertEquals(3, token.getCount());
            }
        }
    }

    @Test(timeout = 4000)
    public void testFormatMillisecondsDirectlyFollowingSeconds() {
        // When 'S' directly follows 's', milliseconds should be adjusted by +1000 and substring(1)
        DurationFormatUtils.Token[] tokens = DurationFormatUtils.lexx("s.SSS");
        String formatted = DurationFormatUtils.format(tokens, 0, 0, 0, 0, 0, 5, 42, true);
        assertEquals("5.042", formatted);

        // When 'S' does NOT directly follow 's', standard padding / unpadded is applied
        DurationFormatUtils.Token[] tokensSoloS = DurationFormatUtils.lexx("SSS");
        String formattedSolo = DurationFormatUtils.format(tokensSoloS, 0, 0, 0, 0, 0, 5, 42, true);
        assertEquals("042", formattedSolo);

        String formattedUnpaddedSolo = DurationFormatUtils.format(tokensSoloS, 0, 0, 0, 0, 0, 5, 42, false);
        assertEquals("42", formattedUnpaddedSolo);
    }

    @Test(timeout = 4000)
    public void testReduceAndCorrectBranches() {
        Calendar start = Calendar.getInstance();
        start.set(2020, Calendar.FEBRUARY, 15);
        Calendar end = Calendar.getInstance();
        end.set(2020, Calendar.FEBRUARY, 20);

        // endValue >= startValue -> returns 0
        int diff1 = DurationFormatUtils.reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, 3);
        assertEquals(0, diff1);

        // endValue < startValue -> returns difference
        start.set(2020, Calendar.FEBRUARY, 25);
        end.set(2020, Calendar.FEBRUARY, 20);
        int diff2 = DurationFormatUtils.reduceAndCorrect(start, end, Calendar.DAY_OF_MONTH, 5);
        assertTrue(diff2 > 0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (Token static inner class)
    // =========================================================================

    @Test(timeout = 4000)
    public void testTokenEqualsHashCodeAndToString() {
        DurationFormatUtils.Token t1 = new DurationFormatUtils.Token("test", 2);
        DurationFormatUtils.Token t2 = new DurationFormatUtils.Token("test", 2);
        DurationFormatUtils.Token tDiffVal = new DurationFormatUtils.Token("other", 2);
        DurationFormatUtils.Token tDiffCount = new DurationFormatUtils.Token("test", 3);

        // Equality
        assertEquals(t1, t1);
        assertEquals(t1, t2);
        assertNotEquals(t1, tDiffVal);
        assertNotEquals(t1, tDiffCount);
        assertNotEquals(t1, null);
        assertNotEquals(t1, "not-a-token");

        // Hash code consistency
        assertEquals(t1.hashCode(), t2.hashCode());

        // toString repetition
        assertEquals("testtest", t1.toString());

        // Token with StringBuffer
        StringBuffer sb1 = new StringBuffer("buf");
        StringBuffer sb2 = new StringBuffer("buf");
        DurationFormatUtils.Token tSb1 = new DurationFormatUtils.Token(sb1, 1);
        DurationFormatUtils.Token tSb2 = new DurationFormatUtils.Token(sb2, 1);
        assertEquals(tSb1, tSb2);
        assertNotEquals(t1, tSb1); // Different value class

        // Token with Number
        DurationFormatUtils.Token tNum1 = new DurationFormatUtils.Token(Integer.valueOf(100), 1);
        DurationFormatUtils.Token tNum2 = new DurationFormatUtils.Token(Integer.valueOf(100), 1);
        DurationFormatUtils.Token tNum3 = new DurationFormatUtils.Token(Integer.valueOf(200), 1);
        assertEquals(tNum1, tNum2);
        assertNotEquals(tNum1, tNum3);
    }

    @Test(timeout = 4000)
    public void testTokenIncrementAndGetters() {
        DurationFormatUtils.Token token = new DurationFormatUtils.Token("item");
        assertEquals(1, token.getCount());
        assertEquals("item", token.getValue());

        token.increment();
        assertEquals(2, token.getCount());
    }
}