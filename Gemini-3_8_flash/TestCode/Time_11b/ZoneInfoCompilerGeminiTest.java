package org.joda.time.tz;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: ZoneInfoCompiler
 *
 * Targeted Decisions & Branches:
 * 1. CLI Entry Point (main)
 *    - args.length == 0 -> prints usage and early returns.
 *    - Flags: "-src", "-dst", "-verbose", "-?", unknown option / end of options.
 *    - IndexOutOfBoundsException on trailing flag without value -> prints usage.
 *    - No source files after options (i >= args.length) -> prints usage.
 *    - Compilation workflow via main() with temporary source files.
 *
 * 2. Static Parsers & Utility Methods
 *    - parseYear: "min"/"minimum" -> Integer.MIN_VALUE, "max"/"maximum" -> Integer.MAX_VALUE,
 *                 "only" -> default value, numeric integers, case-insensitivity.
 *    - parseMonth: short and full month names in English (Jan, January, Dec, etc.).
 *    - parseDayOfWeek: short and full day of week names in English (Mon, Sunday, etc.).
 *    - parseOptional: "-" returns null, any other string returns itself.
 *    - parseTime: positive time, negative time (starting with "-"), invalid time format throws IllegalArgumentException.
 *    - parseZoneChar: 's'/'S' -> 's', 'u'/'U'/'g'/'G'/'z'/'Z' -> 'u', 'w'/'W'/default -> 'w'.
 *    - getStartOfYear: lazy instantiation singleton pattern.
 *    - getLenientISOChronology: cached lenient chronology instance.
 *    - verbose: reading ThreadLocal flag.
 *
 * 3. Binary Serialization & Mapping (writeZoneInfoMap)
 *    - Short index generation, string pool deduction for both key and DateTimeZone ID.
 *    - Empty map, single entry, multiple entries, duplicate aliases.
 *
 * 4. Validation Engine (test method)
 *    - id mismatch with tz.getID() -> returns true immediately.
 *    - Forward transition duplicate detection (offset == nextOffset && key.equals(nextKey)) -> returns false.
 *    - Name key validation (null or length < 3 unless "??") -> returns false.
 *    - Reverse transition matching (trans - 1 != millis) -> returns false.
 *    - Valid transitions round trip -> returns true.
 *
 * 5. Data File Parsing & Lexer (parseDataFile)
 *    - Comment lines (#), empty lines, inline comments.
 *    - Continuation lines: whitespace prefix chained to preceding Zone.
 *    - "Rule" lines: creating RuleSet, adding multiple rules to existing RuleSet.
 *    - "Zone" lines: standard and cutover zones.
 *    - "Link" lines: alias pairings.
 *    - Unknown command tokens.
 *
 * 6. Internal Data Structures (DateTimeOfYear, Rule, RuleSet, Zone)
 *    - DateTimeOfYear: default constructor, "lastSun", "Sun>=8", "Sun<=8", exact day,
 *                      "24:00" transition rollover, addRecurring, addCutover, toString.
 *    - Rule: formatName with '/', formatName with '%s' (with and without LetterS),
 *            toYear < fromYear exception, toString.
 *    - RuleSet: rule name mismatch check, addRecurring delegation.
 *    - Zone: chaining, rules as fixed savings vs rule set reference, non-existent rule set,
 *            untilYear max vs specified cutover, toString.
 *
 * 7. Known Defects4J Ground Truth (TestCompiler::testDateTimeZoneBuilder)
 *    - Parsing rules and zones containing "24:00" time specification and relative day of week
 *      transitions to verify builder cutover and recurring savings generation.
 */
public class ZoneInfoCompilerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseYearVariants() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 1970));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("MINIMUM", 1970));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 1970));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("MAXIMUM", 1970));
        assertEquals(1985, ZoneInfoCompiler.parseYear("only", 1985));
        assertEquals(2004, ZoneInfoCompiler.parseYear("2004", 1985));
        assertEquals(-500, ZoneInfoCompiler.parseYear("-500", 1985));
    }

    @Test(timeout = 4000)
    public void testParseMonthAndDayOfWeek() {
        assertEquals(DateTimeConstants.JANUARY, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(DateTimeConstants.JANUARY, ZoneInfoCompiler.parseMonth("January"));
        assertEquals(DateTimeConstants.DECEMBER, ZoneInfoCompiler.parseMonth("Dec"));
        assertEquals(DateTimeConstants.DECEMBER, ZoneInfoCompiler.parseMonth("December"));

        assertEquals(DateTimeConstants.MONDAY, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(DateTimeConstants.MONDAY, ZoneInfoCompiler.parseDayOfWeek("Monday"));
        assertEquals(DateTimeConstants.SUNDAY, ZoneInfoCompiler.parseDayOfWeek("Sun"));
        assertEquals(DateTimeConstants.SUNDAY, ZoneInfoCompiler.parseDayOfWeek("Sunday"));
    }

    @Test(timeout = 4000)
    public void testParseOptionalAndZoneChar() {
        assertNull(ZoneInfoCompiler.parseOptional("-"));
        assertEquals("standard", ZoneInfoCompiler.parseOptional("standard"));

        assertEquals('s', ZoneInfoCompiler.parseZoneChar('s'));
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('S'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('u'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('U'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('g'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('G'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('z'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('Z'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('w'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('W'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('x')); // fallback to wall
    }

    @Test(timeout = 4000)
    public void testParseTimeValid() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(0, ZoneInfoCompiler.parseTime("00:00"));
        assertEquals(0, ZoneInfoCompiler.parseTime("00:00:00"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("1"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00"));
        assertEquals(5400000, ZoneInfoCompiler.parseTime("1:30"));
        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-01:00"));
    }

    @Test(timeout = 4000)
    public void testCompileSimpleDataWithRuleAndZone() throws Exception {
        String data =
            "# Sample tz file\n" +
            "Rule US 1970 max - Oct lastSun 2:00 0 S\n" +
            "Rule US 1970 max - Apr Sun>=1 2:00 1:00 D\n" +
            "Zone America/TestZone -5:00 US %s\n" +
            "Link America/TestZone TestAlias\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);

        Map<String, DateTimeZone> zones = compiler.compile(null, null);
        assertNotNull(zones);
        assertTrue(zones.containsKey("America/TestZone"));
        assertTrue(zones.containsKey("TestAlias"));
        assertEquals(zones.get("America/TestZone"), zones.get("TestAlias"));
    }

    @Test(timeout = 4000)
    public void testCompileZoneWithFixedRuleAndContinuation() throws Exception {
        String data =
            "Zone Asia/ContZone 9:00 - JST 1980\n" +
            "                   10:00 1:00 JDT\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);

        Map<String, DateTimeZone> zones = compiler.compile(null, null);
        assertNotNull(zones);
        assertTrue(zones.containsKey("Asia/ContZone"));
        DateTimeZone tz = zones.get("Asia/ContZone");
        assertEquals("Asia/ContZone", tz.getID());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMainEmptyArgs() throws Exception {
        ZoneInfoCompiler.main(new String[0]);
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test(timeout = 4000)
    public void testMainHelpFlag() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-?"});
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test(timeout = 4000)
    public void testMainMissingFlagArguments() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-src"});
        ZoneInfoCompiler.main(new String[]{"-dst"});
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test(timeout = 4000)
    public void testMainNoFilesAfterFlags() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-verbose"});
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test(timeout = 4000)
    public void testWriteZoneInfoMapEmptyAndPopulated() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        Map<String, DateTimeZone> emptyMap = new HashMap<String, DateTimeZone>();
        ZoneInfoCompiler.writeZoneInfoMap(dos, emptyMap);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        short poolSize = dis.readShort();
        assertEquals(0, poolSize);
        short mapSize = dis.readShort();
        assertEquals(0, mapSize);

        baos.reset();
        dos = new DataOutputStream(baos);
        Map<String, DateTimeZone> populatedMap = new HashMap<String, DateTimeZone>();
        populatedMap.put("UTC", DateTimeZone.UTC);
        ZoneInfoCompiler.writeZoneInfoMap(dos, populatedMap);

        dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        poolSize = dis.readShort();
        assertEquals(1, poolSize);
        assertEquals("UTC", dis.readUTF());
        mapSize = dis.readShort();
        assertEquals(1, mapSize);
        assertEquals(0, dis.readShort());
        assertEquals(0, dis.readShort());
    }

    @Test(timeout = 4000)
    public void testTestReturnsTrueForMismatchedId() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertTrue(ZoneInfoCompiler.test("NotUTC", utc));
    }

    @Test(timeout = 4000)
    public void testGetStartOfYearAndLenientChronologySingletons() {
        assertNotNull(ZoneInfoCompiler.getStartOfYear());
        assertSame(ZoneInfoCompiler.getStartOfYear(), ZoneInfoCompiler.getStartOfYear());
        assertNotNull(ZoneInfoCompiler.getLenientISOChronology());
        assertSame(ZoneInfoCompiler.getLenientISOChronology(), ZoneInfoCompiler.getLenientISOChronology());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (TestCompiler::testDateTimeZoneBuilder)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDateTimeZoneBuilderWith24HourCutoverAndTransitions() throws Exception {
        // Ground truth defect check:
        // Tests handling of 24:00 time transitions in rules and cutovers.
        String data =
            "Rule TestRule 2000 2005 - Mar lastSun 24:00 1:00 D\n" +
            "Rule TestRule 2000 2005 - Oct lastSun 24:00 0 S\n" +
            "Zone Custom/Zone 1:00 TestRule C%sT 2006\n" +
            "                 1:00 - CST\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);

        Map<String, DateTimeZone> map = compiler.compile(null, null);
        assertNotNull(map);
        DateTimeZone tz = map.get("Custom/Zone");
        assertNotNull("Custom/Zone should be compiled and validated", tz);

        long march2000 = ISOChronology.getInstanceUTC().year().set(0, 2001);
        long next = tz.nextTransition(march2000);
        assertTrue("Transitions should progress forward", next > march2000);
        long prev = tz.previousTransition(next + 1000);
        assertEquals("Previous transition should mirror next transition", next, prev);
    }

    @Test(timeout = 4000)
    public void testDateTimeZoneBuilderDayOfWeekLeapAndSlashFormat() throws Exception {
        String data =
            "Rule RuleSlash 2000 max - Jan 15 2:00 0 S\n" +
            "Rule RuleSlash 2000 max - Jul 15 2:00 1:00 D\n" +
            "Zone Test/Slash 2:00 RuleSlash Standard/Daylight\n";

        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        compiler.parseDataFile(new BufferedReader(new StringReader(data)));
        Map<String, DateTimeZone> map = compiler.compile(null, null);
        assertNotNull(map.get("Test/Slash"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseTimeInvalidFormat() {
        ZoneInfoCompiler.parseTime("invalid_time_format");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRuleToYearLessThanFromYear() {
        StringTokenizer st = new StringTokenizer("InvalidRule 2000 1990 - Mar 1 0 0 S");
        new ZoneInfoCompiler.DateTimeOfYear(st);
        try {
            Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
            Constructor<?> ctor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
            ctor.setAccessible(true);
            ctor.newInstance(new StringTokenizer("InvalidRule 2000 1990 - Mar 1 0 0 S"));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
        } catch (Exception e) {
            fail("Reflection error: " + e.getMessage());
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testRuleSetMismatch() throws Exception {
        Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
        Constructor<?> ruleCtor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
        ruleCtor.setAccessible(true);

        Object rule1 = ruleCtor.newInstance(new StringTokenizer("R1 2000 2005 - Mar 1 0 0 S"));
        Object rule2 = ruleCtor.newInstance(new StringTokenizer("R2 2000 2005 - Mar 1 0 0 S"));

        Class<?> ruleSetClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$RuleSet");
        Constructor<?> ruleSetCtor = ruleSetClass.getDeclaredConstructor(ruleClass);
        ruleSetCtor.setAccessible(true);

        Object ruleSet = ruleSetCtor.newInstance(rule1);
        Method addRule = ruleSetClass.getDeclaredMethod("addRule", ruleClass);
        addRule.setAccessible(true);
        try {
            addRule.invoke(ruleSet, rule2);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCompileZoneWithMissingRuleSet() throws Exception {
        String data = "Zone MissingRuleZone 1:00 NonExistentRule %s\n";
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        compiler.parseDataFile(new BufferedReader(new StringReader(data)));
        compiler.compile(null, null);
    }

    @Test(timeout = 4000)
    public void testCompileOutputDirectoryCreationAndWriting() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "zic_test_" + System.nanoTime());
        assertFalse(tempDir.exists());
        try {
            String data =
                "Rule TestR 2000 2001 - Mar 1 0 0 S\n" +
                "Zone DirZone/Sub 1:00 TestR %s\n";

            ZoneInfoCompiler compiler = new ZoneInfoCompiler();
            compiler.parseDataFile(new BufferedReader(new StringReader(data)));
            Map<String, DateTimeZone> result = compiler.compile(tempDir, null);
            assertTrue(tempDir.exists());
            assertTrue(tempDir.isDirectory());
            assertTrue(result.containsKey("DirZone/Sub"));

            File compiledFile = new File(tempDir, "DirZone/Sub");
            assertTrue(compiledFile.exists());
            File mapFile = new File(tempDir, "ZoneInfoMap");
            assertTrue(mapFile.exists());
        } finally {
            deleteRecursive(tempDir);
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCompileDestinationNotDirectory() throws Exception {
        File tempFile = File.createTempFile("zic_file", ".tmp");
        try {
            ZoneInfoCompiler compiler = new ZoneInfoCompiler();
            compiler.compile(tempFile, null);
        } finally {
            tempFile.delete();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Structural Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDateTimeOfYearParsingPatterns() {
        StringTokenizer st1 = new StringTokenizer("Mar lastSun 2:00w");
        ZoneInfoCompiler.DateTimeOfYear dt1 = new ZoneInfoCompiler.DateTimeOfYear(st1);
        assertEquals(DateTimeConstants.MARCH, dt1.iMonthOfYear);
        assertEquals(-1, dt1.iDayOfMonth);
        assertEquals(DateTimeConstants.SUNDAY, dt1.iDayOfWeek);
        assertFalse(dt1.iAdvanceDayOfWeek);
        assertEquals(7200000, dt1.iMillisOfDay);
        assertEquals('w', dt1.iZoneChar);

        StringTokenizer st2 = new StringTokenizer("Apr Sun>=8 1:30s");
        ZoneInfoCompiler.DateTimeOfYear dt2 = new ZoneInfoCompiler.DateTimeOfYear(st2);
        assertEquals(DateTimeConstants.APRIL, dt2.iMonthOfYear);
        assertEquals(8, dt2.iDayOfMonth);
        assertEquals(DateTimeConstants.SUNDAY, dt2.iDayOfWeek);
        assertTrue(dt2.iAdvanceDayOfWeek);
        assertEquals(5400000, dt2.iMillisOfDay);
        assertEquals('s', dt2.iZoneChar);

        StringTokenizer st3 = new StringTokenizer("Oct Sun<=24 24:00u");
        ZoneInfoCompiler.DateTimeOfYear dt3 = new ZoneInfoCompiler.DateTimeOfYear(st3);
        assertEquals(DateTimeConstants.OCTOBER, dt3.iMonthOfYear);
        assertEquals(24, dt3.iDayOfMonth);
        assertEquals(DateTimeConstants.SUNDAY, dt3.iDayOfWeek);
        assertFalse(dt3.iAdvanceDayOfWeek);
        assertEquals('u', dt3.iZoneChar);

        StringTokenizer st4 = new StringTokenizer("Jun 15 12:00");
        ZoneInfoCompiler.DateTimeOfYear dt4 = new ZoneInfoCompiler.DateTimeOfYear(st4);
        assertEquals(DateTimeConstants.JUNE, dt4.iMonthOfYear);
        assertEquals(15, dt4.iDayOfMonth);
        assertEquals(0, dt4.iDayOfWeek);
        assertFalse(dt4.iAdvanceDayOfWeek);
        assertEquals(43200000, dt4.iMillisOfDay);

        assertNotNull(dt1.toString());
        assertTrue(dt1.toString().contains("MonthOfYear: 3"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDateTimeOfYearInvalidDayPattern() {
        StringTokenizer st = new StringTokenizer("Jan invalidPattern 0");
        new ZoneInfoCompiler.DateTimeOfYear(st);
    }

    @Test(timeout = 4000)
    public void testRuleAndZoneToString() throws Exception {
        Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
        Constructor<?> ruleCtor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
        ruleCtor.setAccessible(true);
        Object rule = ruleCtor.newInstance(new StringTokenizer("TestRule 1990 2000 - Jan 1 0 0 -"));
        String ruleStr = rule.toString();
        assertNotNull(ruleStr);
        assertTrue(ruleStr.contains("[Rule]"));
        assertTrue(ruleStr.contains("TestRule"));

        Class<?> zoneClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Zone");
        Constructor<?> zoneCtor = zoneClass.getDeclaredConstructor(StringTokenizer.class);
        zoneCtor.setAccessible(true);
        Object zone = zoneCtor.newInstance(new StringTokenizer("TestZone 1:00 - TZT 2000 Jan 1 0"));
        String zoneStr = zone.toString();
        assertNotNull(zoneStr);
        assertTrue(zoneStr.contains("[Zone]"));
        assertTrue(zoneStr.contains("TestZone"));

        Method chainMethod = zoneClass.getDeclaredMethod("chain", StringTokenizer.class);
        chainMethod.setAccessible(true);
        chainMethod.invoke(zone, new StringTokenizer("2:00 - TZT"));
        String chainedStr = zone.toString();
        assertTrue(chainedStr.contains("..."));
    }

    @Test(timeout = 4000)
    public void testMainExecutionFullFlow() throws Exception {
        File srcDir = new File(System.getProperty("java.io.tmpdir"), "zic_src_" + System.nanoTime());
        File dstDir = new File(System.getProperty("java.io.tmpdir"), "zic_dst_" + System.nanoTime());
        assertTrue(srcDir.mkdirs());
        try {
            File tzFile = new File(srcDir, "sample.tz");
            FileOutputStream fos = new FileOutputStream(tzFile);
            fos.write(
                ("Rule RuleMain 2000 max - Mar 1 2:00 0 S\n" +
                 "Zone Main/Zone 0:00 RuleMain MZT\n").getBytes("UTF-8")
            );
            fos.close();

            String[] args = new String[]{
                "-src", srcDir.getAbsolutePath(),
                "-dst", dstDir.getAbsolutePath(),
                "-verbose",
                "sample.tz"
            };

            ZoneInfoCompiler.main(args);
            assertTrue(new File(dstDir, "Main/Zone").exists());
            assertTrue(new File(dstDir, "ZoneInfoMap").exists());
        } finally {
            deleteRecursive(srcDir);
            deleteRecursive(dstDir);
        }
    }

    @Test(timeout = 4000)
    public void testParseDataFileUnknownLineAndComments() throws Exception {
        String data =
            "# Full comment line\n" +
            "   # Whitespace comment line\n" +
            "\n" +
            "UnknownCommand extra tokens\n";
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        compiler.parseDataFile(new BufferedReader(new StringReader(data)));
        Map<String, DateTimeZone> map = compiler.compile(null, null);
        assertTrue(map.isEmpty());
    }

    private static void deleteRecursive(File f) {
        if (f != null && f.exists()) {
            if (f.isDirectory()) {
                File[] children = f.listFiles();
                if (children != null) {
                    for (File child : children) {
                        deleteRecursive(child);
                    }
                }
            }
            f.delete();
        }
    }
}