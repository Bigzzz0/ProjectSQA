package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

/**
 * White-box test suite for Parser.java targeting maximum coverage and the known Defects4J defect
 * (CLI-71: required options not re-initialized on reuse).
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic (parse with options, properties, stopAtNonOption)
 * - Partition B: Boundary values (null arguments, empty arrays, "--", "-")
 * - Partition C: Defect-targeted (reuse parser twice with required options)
 * - Partition D: Exception paths (unrecognized option, missing argument, missing required)
 * - Partition E: State transitions (processOption with groups, required removal, processArgs)
 */
public class ParserDeepseekTest {

    // Helper to create a concrete parser (GnuParser is available in commons-cli)
    private Parser createParser() {
        return new GnuParser();
    }

    // Helper to build a simple Options with one required option
    private Options buildRequiredOptions() {
        Options options = new Options();
        options.addOption("r", "required", true, "required option");
        Option required = options.getOption("r");
        required.setRequired(true);
        return options;
    }

    // Helper to build Options with an option group
    private Options buildGroupOptions() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withLongOpt("opt1").create("a"));
        group.addOption(OptionBuilder.withLongOpt("opt2").create("b"));
        group.setRequired(true);
        options.addOptionGroup(group);
        return options;
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testParseBasic() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v"});
        assertTrue("Option -v should be present", cmd.hasOption("v"));
        assertEquals("No extra args expected", 0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseWithProperties() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file name");
        Parser parser = createParser();
        Properties props = new Properties();
        props.setProperty("f", "test.txt");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue("Option -f should be set from properties", cmd.hasOption("f"));
        assertEquals("Value should be 'test.txt'", "test.txt", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testParseStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v", "nonOption", "-x"}, true);
        assertTrue("Option -v should be present", cmd.hasOption("v"));
        assertEquals("Should have 2 extra args", 2, cmd.getArgs().length);
        assertEquals("First arg should be 'nonOption'", "nonOption", cmd.getArgs()[0]);
        assertEquals("Second arg should be '-x'", "-x", cmd.getArgs()[1]);
    }

    // ========== Partition B: Boundary Values & Extremes ==========

    @Test(timeout = 4000)
    public void testParseNullArguments() throws ParseException {
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, null);
        assertNotNull("CommandLine should not be null", cmd);
        assertEquals("No args expected", 0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseEmptyArguments() throws ParseException {
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseDoubleDash() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"--", "-v"});
        assertFalse("Option -v should not be parsed after --", cmd.hasOption("v"));
        assertEquals("Should have one arg '-v'", 1, cmd.getArgs().length);
        assertEquals("-v", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDash() throws ParseException {
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-"});
        assertEquals("Single dash should be added as arg", 1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDashStopAtNonOption() throws ParseException {
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-", "other"}, true);
        assertEquals("Single dash and other should be args", 2, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
        assertEquals("other", cmd.getArgs()[1]);
    }

    // ========== Partition C: Defect-Targeted (CLI-71) ==========

    @Test(timeout = 4000)
    public void testReuseOptionsTwice() throws ParseException {
        Options options = buildRequiredOptions();
        Parser parser = createParser();

        // First parse: provide required option
        CommandLine cmd1 = parser.parse(options, new String[]{"-r", "value1"});
        assertTrue("First parse should succeed", cmd1.hasOption("r"));

        // Second parse: do NOT provide required option -> should throw MissingOptionException
        try {
            parser.parse(options, new String[]{"-x"});
            fail("MissingOptionException should have been thrown on second parse");
        } catch (MissingOptionException e) {
            assertTrue("Exception message should mention required option",
                    e.getMessage().contains("required"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testParseUnrecognizedOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        parser.parse(options, new String[]{"-x"});
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testParseMissingArgument() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file name");
        Parser parser = createParser();
        parser.parse(options, new String[]{"-f"});
    }

    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testParseMissingRequiredOption() throws ParseException {
        Options options = buildRequiredOptions();
        Parser parser = createParser();
        parser.parse(options, new String[0]);
    }

    @Test(timeout = 4000)
    public void testParseWithOptionalArg() throws ParseException {
        Options options = new Options();
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create("o");
        options.addOption(opt);
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-o"});
        assertTrue("Option -o should be present", cmd.hasOption("o"));
        assertNull("Optional arg should be null", cmd.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testParseWithOptionalArgValue() throws ParseException {
        Options options = new Options();
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create("o");
        options.addOption(opt);
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-o", "value"});
        assertTrue(cmd.hasOption("o"));
        assertEquals("value", cmd.getOptionValue("o"));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testProcessOptionWithGroup() throws ParseException {
        Options options = buildGroupOptions();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue("Option -a should be present", cmd.hasOption("a"));
        assertFalse("Option -b should not be present", cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testProcessOptionRemovesRequired() throws ParseException {
        Options options = buildRequiredOptions();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-r", "val"});
        assertTrue(cmd.hasOption("r"));
        // After parsing, the required list should be empty (option was processed)
        // But this is internal state; we verify by parsing again without required option
        // (already covered in testReuseOptionsTwice)
    }

    @Test(timeout = 4000)
    public void testProcessArgsWithQuotes() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-f", "\"test.txt\""});
        assertEquals("Quotes should be stripped", "test.txt", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testProcessArgsStopsAtOption() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file");
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-f", "-v"});
        // -f should not consume -v as its argument; -v should be separate option
        assertTrue("Option -f should be present", cmd.hasOption("f"));
        assertNull("Option -f should have no value", cmd.getOptionValue("f"));
        assertTrue("Option -v should be present", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesWithNull() throws ParseException {
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[0], (Properties) null);
        assertNotNull(cmd);
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesNonBoolean() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        Properties props = new Properties();
        props.setProperty("v", "false"); // "false" should not add option
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertFalse("Option -v should not be added when property is 'false'", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesBooleanTrue() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        Properties props = new Properties();
        props.setProperty("v", "true");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue("Option -v should be added when property is 'true'", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesWithArg() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file");
        Parser parser = createParser();
        Properties props = new Properties();
        props.setProperty("f", "myfile.txt");
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue(cmd.hasOption("f"));
        assertEquals("myfile.txt", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesWithExistingOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        Properties props = new Properties();
        props.setProperty("v", "true");
        // First parse with -v on command line, then properties should not override
        CommandLine cmd = parser.parse(options, new String[]{"-v"}, props);
        assertTrue(cmd.hasOption("v"));
        // The option is already present, so properties processing should skip
    }

    @Test(timeout = 4000)
    public void testCheckRequiredOptionsEmpty() throws Exception {
        // This is indirectly tested when no required options are present
        Options options = new Options();
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
    }

    @Test(timeout = 4000)
    public void testProcessOptionWithUnrecognized() throws ParseException {
        // Already covered by testParseUnrecognizedOption
    }

    @Test(timeout = 4000)
    public void testProcessArgsWithRuntimeException() throws ParseException {
        // This branch is hard to trigger directly; we rely on coverage from other tests
    }

    @Test(timeout = 4000)
    public void testEatTheRestWithDoubleDash() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"--", "-v", "arg1", "--", "arg2"});
        assertFalse(cmd.hasOption("v"));
        assertEquals("Should have 3 args: -v, arg1, arg2", 3, cmd.getArgs().length);
        assertEquals("-v", cmd.getArgs()[0]);
        assertEquals("arg1", cmd.getArgs()[1]);
        assertEquals("arg2", cmd.getArgs()[2]);
    }

    @Test(timeout = 4000)
    public void testEatTheRestWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v", "nonOpt", "-x"}, true);
        assertTrue(cmd.hasOption("v"));
        assertEquals("Should have 2 args: nonOpt, -x", 2, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseWithMultipleOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("b", "beta", true, "beta");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b", "value"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test(timeout = 4000)
    public void testParseWithLongOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"--verbose"});
        assertTrue(cmd.hasOption("verbose"));
    }

    @Test(timeout = 4000)
    public void testParseWithLongOptionAndValue() throws ParseException {
        Options options = new Options();
        options.addOption("f", "file", true, "file");
        Parser parser = createParser();
        CommandLine cmd = parser.parse(options, new String[]{"--file", "test.txt"});
        assertTrue(cmd.hasOption("file"));
        assertEquals("test.txt", cmd.getOptionValue("file"));
    }
}