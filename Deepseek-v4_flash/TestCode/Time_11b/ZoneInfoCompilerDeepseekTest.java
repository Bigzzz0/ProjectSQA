package org.joda.time.tz;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ZoneInfoCompilerDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: ZoneInfoCompiler and its inner classes (Zone, Rule, DateTimeOfYear, RuleSet)
     * 
     * Defect: org.joda.time.tz.TestCompiler::testDateTimeZoneBuilder fails with AssertionFailedError
     * This indicates that the DateTimeZoneBuilder produces incorrect results when compiling
     * certain zone definitions, likely related to the handling of recurring rules, cutover
     * times, or the interaction between zones and rules.
     * 
     * Key branches to test:
     * - Zone constructor: parsing of zone lines with various token counts
     * - Zone.chain(): linking multiple zone entries
     * - Zone.addToBuilder(): handling of null rules, fixed savings, recurring rules
     * - Zone.buildDateTimeZone(): building zones with and without rules
     * - Rule constructor: validation of year ranges, parsing of date/time
     * - Rule.addRecurring(): handling of zero save millis, letter substitution
     * - DateTimeOfYear constructor: parsing of "last", "24:00", day-of-week, etc.
     * - DateTimeOfYear.addRecurring(): handling of cutover times, leap years
     * - DateTimeOfYear.addCutover(): boundary at Integer.MAX_VALUE
     * - RuleSet.addRecurring(): adding rules to builder
     * - parseYear(): handling of "minimum", "maximum", "only", and numeric values
     * - parseMonth(): month name parsing
     * - parseDayOfWeek(): day-of-week parsing
     * - parseTime(): time parsing with negative values, "24:00"
     * - parseZoneChar(): handling of 's', 'u', 'w', 'z'
     * - parseOptional(): handling of "-" 
     * - writeZoneInfoMap(): serialization of zone map
     * - compile(): file I/O, directory creation, error handling
     * 
     * Partitions:
     * A: Core functional logic - Zone/Rule parsing, builder operations
     * B: Boundary values - Integer.MAX_VALUE, 24:00, "last", "only", negative times
     * C: Defect-targeted - Zone with recurring rules and cutover times
     * D: Exception paths - invalid rules, missing rule sets, bad input
     * E: Object lifecycle - Rule/Zone state, toString, intern() behavior
     */

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testParseYearBasic() {
        assertEquals(2023, ZoneInfoCompiler.parseYear("2023", 0));
        assertEquals(1999, ZoneInfoCompiler.parseYear("1999", 0));
        assertEquals(0, ZoneInfoCompiler.parseYear("0", 0));
    }

    @Test(timeout = 4000)
    public void testParseYearSpecialValues() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 0));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 0));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 0));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 0));
        assertEquals(2000, ZoneInfoCompiler.parseYear("only", 2000));
    }

    @Test(timeout = 4000)
    public void testParseMonth() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(2, ZoneInfoCompiler.parseMonth("Feb"));
        assertEquals(3, ZoneInfoCompiler.parseMonth("Mar"));
        assertEquals(4, ZoneInfoCompiler.parseMonth("Apr"));
        assertEquals(5, ZoneInfoCompiler.parseMonth("May"));
        assertEquals(6, ZoneInfoCompiler.parseMonth("Jun"));
        assertEquals(7, ZoneInfoCompiler.parseMonth("Jul"));
        assertEquals(8, ZoneInfoCompiler.parseMonth("Aug"));
        assertEquals(9, ZoneInfoCompiler.parseMonth("Sep"));
        assertEquals(10, ZoneInfoCompiler.parseMonth("Oct"));
        assertEquals(11, ZoneInfoCompiler.parseMonth("Nov"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
    }

    @Test(timeout = 4000)
    public void testParseDayOfWeek() {
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(2, ZoneInfoCompiler.parseDayOfWeek("Tue"));
        assertEquals(3, ZoneInfoCompiler.parseDayOfWeek("Wed"));
        assertEquals(4, ZoneInfoCompiler.parseDayOfWeek("Thu"));
        assertEquals(5, ZoneInfoCompiler.parseDayOfWeek("Fri"));
        assertEquals(6, ZoneInfoCompiler.parseDayOfWeek("Sat"));
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sun"));
    }

    @Test(timeout = 4000)
    public void testParseOptional() {
        assertEquals(null, ZoneInfoCompiler.parseOptional("-"));
        assertEquals("abc", ZoneInfoCompiler.parseOptional("abc"));
        assertEquals("", ZoneInfoCompiler.parseOptional(""));
    }

    @Test(timeout = 4000)
    public void testParseTime() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("1:00"));
        assertEquals(7200000, ZoneInfoCompiler.parseTime("2"));
        assertEquals(90000000, ZoneInfoCompiler.parseTime("25:00"));
        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-1:00"));
        assertEquals(86400000, ZoneInfoCompiler.parseTime("24:00"));
    }

    @Test(timeout = 4000)
    public void testParseZoneChar() {
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('s'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('u'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('w'));
        assertEquals('z', ZoneInfoCompiler.parseZoneChar('z'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('x')); // default
    }

    @Test(timeout = 4000)
    public void testGetStartOfYear() {
        ZoneInfoCompiler.DateTimeOfYear start = ZoneInfoCompiler.getStartOfYear();
        assertNotNull(start);
        assertEquals(1, start.iMonthOfYear);
        assertEquals(1, start.iDayOfMonth);
        assertEquals(0, start.iMillisOfDay);
        assertEquals(0, start.iDayOfWeek);
        assertFalse(start.iAdvanceDayOfWeek);
        assertEquals('w', start.iZoneChar);
    }

    @Test(timeout = 4000)
    public void testGetLenientISOChronology() {
        assertNotNull(ZoneInfoCompiler.getLenientISOChronology());
        // Should be cached
        assertSame(ZoneInfoCompiler.getLenientISOChronology(), ZoneInfoCompiler.getLenientISOChronology());
    }

    @Test(timeout = 4000)
    public void testVerboseDefault() {
        ZoneInfoCompiler.cVerbose.set(Boolean.FALSE);
        assertFalse(ZoneInfoCompiler.verbose());
        ZoneInfoCompiler.cVerbose.set(Boolean.TRUE);
        assertTrue(ZoneInfoCompiler.verbose());
        ZoneInfoCompiler.cVerbose.remove();
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testParseYearBoundaryValues() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 0));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 0));
        assertEquals(1, ZoneInfoCompiler.parseYear("1", 0));
        assertEquals(-1, ZoneInfoCompiler.parseYear("-1", 0));
    }

    @Test(timeout = 4000)
    public void testParseTimeBoundaryValues() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(86399999, ZoneInfoCompiler.parseTime("23:59:59.999"));
        assertEquals(86400000, ZoneInfoCompiler.parseTime("24:00"));
        assertEquals(-1, ZoneInfoCompiler.parseTime("-0:00.001"));
    }

    @Test(timeout = 4000)
    public void testDateTimeOfYearBoundary() {
        // Test "last" day of month
        StringTokenizer st = new StringTokenizer("lastSun Mar 2:00", " \t");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(0, dt.iDayOfMonth);
        assertEquals(7, dt.iDayOfWeek);
        assertTrue(dt.iAdvanceDayOfWeek);
        assertEquals(7200000, dt.iMillisOfDay);
        assertEquals('w', dt.iZoneChar);
    }

    @Test(timeout = 4000)
    public void testDateTimeOfYear24Hour() {
        StringTokenizer st = new StringTokenizer("Mar lastSun 24:00", " \t");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(0, dt.iDayOfMonth);
        assertEquals(7, dt.iDayOfWeek);
        assertTrue(dt.iAdvanceDayOfWeek);
        assertEquals(86400000, dt.iMillisOfDay);
    }

    @Test(timeout = 4000)
    public void testZoneChain() {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Zone zone = new Zone("Test", new StringTokenizer("Test +1:00 - +02", " \t"));
        zone.chain(new StringTokenizer("+2:00 - +03", " \t"));
        assertNotNull(zone.iNext);
        assertEquals("Test", zone.iNext.iName);
        assertEquals(7200000, zone.iNext.iOffsetMillis);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testDateTimeZoneBuilderDefect() throws IOException {
        // This test targets the known defect in TestCompiler::testDateTimeZoneBuilder
        // The defect causes AssertionFailedError when building zones with recurring rules
        // and cutover times.
        
        String data = 
            "Rule Test 2000 max - Mar lastSun 2:00 1:00 D\n" +
            "Rule Test 2000 max - Oct lastSun 2:00 0 S\n" +
            "Zone TestZone 0:00 Test - +00\n" +
            "0:00 Test +00\n" +
            "Link TestZone TestLink\n";
        
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> zones = compiler.compile(null, new java.io.File[] {});
        
        // Use reflection to access private fields for testing
        try {
            java.lang.reflect.Field ruleSetsField = ZoneInfoCompiler.class.getDeclaredField("iRuleSets");
            ruleSetsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ZoneInfoCompiler.RuleSet> ruleSets = 
                (Map<String, ZoneInfoCompiler.RuleSet>) ruleSetsField.get(compiler);
            
            java.lang.reflect.Field zonesField = ZoneInfoCompiler.class.getDeclaredField("iZones");
            zonesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Zone> zonesList = 
                (java.util.List<ZoneInfoCompiler.Zone>) zonesField.get(compiler);
            
            // Add a rule set
            ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet();
            java.lang.reflect.Field rulesField = ZoneInfoCompiler.RuleSet.class.getDeclaredField("iRules");
            rulesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Rule> rules = 
                (java.util.List<ZoneInfoCompiler.Rule>) rulesField.get(rs);
            
            // Create a rule with recurring savings
            ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
            rules.add(rule);
            ruleSets.put("Test", rs);
            
            // Create a zone with rules
            ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("TestZone 0:00 Test - +00", " \t"));
            zonesList.add(zone);
            
            // Build the DateTimeZone
            DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
            zone.addToBuilder(builder, ruleSets);
            DateTimeZone tz = builder.toDateTimeZone("TestZone", true);
            
            assertNotNull(tz);
            assertEquals("TestZone", tz.getID());
            
            // Verify transitions exist
            long now = System.currentTimeMillis();
            long next = tz.nextTransition(now - 365L * 24 * 3600 * 1000);
            assertTrue(next > 0);
            
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testZoneWithFixedSavings() {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, ZoneInfoCompiler.RuleSet> ruleSets = new HashMap<String, ZoneInfoCompiler.RuleSet>();
        
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
            new StringTokenizer("Fixed +1:00 - +02", " \t"));
        
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, ruleSets);
        DateTimeZone tz = builder.toDateTimeZone("Fixed", true);
        
        assertNotNull(tz);
        assertEquals(3600000, tz.getOffset(0));
    }

    @Test(timeout = 4000)
    public void testZoneWithRecurringRules() {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, ZoneInfoCompiler.RuleSet> ruleSets = new HashMap<String, ZoneInfoCompiler.RuleSet>();
        
        // Create a rule set
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet();
        try {
            java.lang.reflect.Field rulesField = ZoneInfoCompiler.RuleSet.class.getDeclaredField("iRules");
            rulesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Rule> rules = 
                (java.util.List<ZoneInfoCompiler.Rule>) rulesField.get(rs);
            
            ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
            rules.add(rule);
            ruleSets.put("Test", rs);
            
            ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Recurring 0:00 Test - +00", " \t"));
            
            DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
            zone.addToBuilder(builder, ruleSets);
            DateTimeZone tz = builder.toDateTimeZone("Recurring", true);
            
            assertNotNull(tz);
            // Verify DST transition exists
            long march = new org.joda.time.DateTime(2023, 3, 26, 2, 0).getMillis();
            long next = tz.nextTransition(march - 86400000L);
            assertTrue(next > 0);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRuleInvalidYearRange() {
        new ZoneInfoCompiler.Rule(new StringTokenizer("Test 2020 2019 - Jan 1 0:00 0 S", " \t"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRuleMissingFields() {
        new ZoneInfoCompiler.Rule(new StringTokenizer("Test 2020", " \t"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testZoneMissingFields() {
        new ZoneInfoCompiler.Zone(new StringTokenizer("Test", " \t"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDateTimeOfYearInvalid() {
        new ZoneInfoCompiler.DateTimeOfYear(new StringTokenizer("", " \t"));
    }

    @Test(timeout = 4000)
    public void testCompileWithInvalidDirectory() {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        try {
            compiler.compile(new File("/nonexistent/path"), new File[] {});
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCompileWithFileAsOutputDir() throws IOException {
        File tempFile = File.createTempFile("test", ".tmp");
        tempFile.deleteOnExit();
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        try {
            compiler.compile(tempFile, new File[] {});
            fail("Expected IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testParseDataFileWithComments() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "# comment\n\nRule Test 2000 max - Mar lastSun 2:00 1:00 D\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        
        // Verify rule was added
        try {
            java.lang.reflect.Field field = ZoneInfoCompiler.class.getDeclaredField("iRuleSets");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, ZoneInfoCompiler.RuleSet> ruleSets = 
                (Map<String, ZoneInfoCompiler.RuleSet>) field.get(compiler);
            assertTrue(ruleSets.containsKey("Test"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testParseDataFileWithZoneContinuation() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Zone Test 0:00 - +00\n0:00 - +01\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        
        try {
            java.lang.reflect.Field field = ZoneInfoCompiler.class.getDeclaredField("iZones");
            field.setAccessible(true);
            @SupppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Zone> zones = 
                (java.util.List<ZoneInfoCompiler.Zone>) field.get(compiler);
            assertEquals(1, zones.size());
            assertNotNull(zones.get(0).iNext);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testRuleToString() {
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
            new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
        String str = rule.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test"));
        assertTrue(str.contains("2000"));
    }

    @Test(timeout = 4000)
    public void testZoneToString() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
            new StringTokenizer("Test 0:00 - +00", " \t"));
        String str = zone.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test"));
    }

    @Test(timeout = 4000)
    public void testDateTimeOfYearToString() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(
            new StringTokenizer("Mar lastSun 2:00", " \t"));
        String str = dt.toString();
        assertNotNull(str);
        assertTrue(str.contains("Mar"));
    }

    @Test(timeout = 4000)
    public void testRuleNameIntern() {
        ZoneInfoCompiler.Rule rule1 = new ZoneInfoCompiler.Rule(
            new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
        ZoneInfoCompiler.Rule rule2 = new ZoneInfoCompiler.Rule(
            new StringTokenizer("Test 2001 max - Mar lastSun 2:00 1:00 D", " \t"));
        assertSame(rule1.iName, rule2.iName);
    }

    @Test(timeout = 4000)
    public void testZoneNameIntern() {
        ZoneInfoCompiler.Zone zone1 = new ZoneInfoCompiler.Zone(
            new StringTokenizer("Test 0:00 - +00", " \t"));
        ZoneInfoCompiler.Zone zone2 = new ZoneInfoCompiler.Zone(
            new StringTokenizer("Test 0:00 - +00", " \t"));
        assertSame(zone1.iName, zone2.iName);
    }

    @Test(timeout = 4000)
    public void testAddRecurringWithLetter() {
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
            new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        rule.addRecurring(builder, "Test%s");
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testAddRecurringWithoutLetter() {
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
            new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 -", " \t"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        rule.addRecurring(builder, "Test");
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testAddCutover() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(
            new StringTokenizer("Mar lastSun 2:00", " \t"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        dt.addCutover(builder, 2023);
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testAddCutoverMaxYear() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(
            new StringTokenizer("Mar lastSun 2:00", " \t"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        dt.addCutover(builder, Integer.MAX_VALUE);
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testWriteZoneInfoMap() throws IOException {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        builder.setStandardOffset(0);
        builder.setFixedSavings("UTC", 0);
        DateTimeZone tz = builder.toDateTimeZone("UTC", true);
        map.put("UTC", tz);
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dout, map);
        dout.flush();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteZoneInfoMapWithDuplicateIds() throws IOException {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        builder.setStandardOffset(0);
        builder.setFixedSavings("UTC", 0);
        DateTimeZone tz = builder.toDateTimeZone("UTC", true);
        map.put("utc", tz); // case-insensitive duplicate
        map.put("UTC", tz);
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dout, map);
        dout.flush();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testRuleSetAddRecurring() {
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet();
        try {
            java.lang.reflect.Field field = ZoneInfoCompiler.RuleSet.class.getDeclaredField("iRules");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Rule> rules = 
                (java.util.List<ZoneInfoCompiler.Rule>) field.get(rs);
            
            ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
            rules.add(rule);
            
            DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
            rs.addRecurring(builder, "Test%s");
            // Should not throw
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testRuleSetAddRecurringWithMismatchedNames() {
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet();
        try {
            java.lang.reflect.Field field = ZoneInfoCompiler.RuleSet.class.getDeclaredField("iRules");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.List<ZoneInfoCompiler.Rule> rules = 
                (java.util.List<ZoneInfoCompiler.Rule>) field.get(rs);
            
            ZoneInfoCompiler.Rule rule1 = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test1 2000 max - Mar lastSun 2:00 1:00 D", " \t"));
            ZoneInfoCompiler.Rule rule2 = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test2 2001 max - Mar lastSun 2:00 1:00 D", " \t"));
            rules.add(rule1);
            rules.add(rule2);
            
            DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
            rs.addRecurring(builder, "Test%s");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Wrong exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCompileWithNullSources() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> result = compiler.compile(null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCompileWithEmptySources() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> result = compiler.compile(null, new File[] {});
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMainWithNoArgs() {
        try {
            ZoneInfoCompiler.main(new String[] {});
            fail("Expected exception");
        } catch (Exception e) {
            // Expected - prints usage
        }
    }

    @Test(timeout = 4000)
    public void testMainWithHelp() throws Exception {
        ZoneInfoCompiler.main(new String[] {"-?"});
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testMainWithVerbose() throws Exception {
        ZoneInfoCompiler.main(new String[] {"-verbose", "-src", "/tmp", "-dst", "/tmp"});
        // Should not throw, though directories may not exist
    }

    @Test(timeout = 4000)
    public void testMainWithInvalidArgs() {
        try {
            ZoneInfoCompiler.main(new String[] {"-src"});
            fail("Expected exception");
        } catch (Exception e) {
            // Expected - IndexOutOfBounds
        }
    }

    @Test(timeout = 4000)
    public void testGetStartOfYearCached() {
        ZoneInfoCompiler.DateTimeOfYear first = ZoneInfoCompiler.getStartOfYear();
        ZoneInfoCompiler.DateTimeOfYear second = ZoneInfoCompiler.getStartOfYear();
        assertSame(first, second);
    }

    @Test(timeout = 4000)
    public void testGetLenientISOChronologyCached() {
        Chronology first = ZoneInfoCompiler.getLenientISOChronology();
        Chronology second = ZoneInfoCompiler.getLenientISOChronology();
        assertSame(first, second);
    }
}