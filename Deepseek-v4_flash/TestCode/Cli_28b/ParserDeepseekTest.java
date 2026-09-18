package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

/**
 * Test suite targeting Parser class with focus on:
 * - Core parsing logic and state transitions
 * - Boundary conditions (null, empty, special tokens)
 * - Defect-targeted branch zones (property option flags, required options, option groups)
 * - Exception paths and defensive guards
 * - Object lifecycle and contract integrity
 */
public class ParserDeepseekTest {

    /**
     * Helper method to create a concrete Parser implementation for testing.
     * Uses a simple flatten implementation that returns the arguments as-is.
     */
    private Parser createTestParser() {
        return new Parser() {
            @Override
            protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException {
                if (arguments == null) {
                    return new String[0];
                }
                return arguments;
            }
        };
    }

    /* ===================================================================
     * PARTITION A: Core Functional Logic & State Transitions
     * =================================================================== */

    @Test(timeout = 4000)
    public void testParseSimpleOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v"});
        
        assertTrue("Should have verbose option", cmd.hasOption("v"));
        assertTrue("Should have verbose option (long)", cmd.hasOption("verbose"));
        assertEquals("Args should be empty", 0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseOptionWithValue() throws ParseException {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-o", "test.txt"});
        
        assertTrue("Should have output option", cmd.hasOption("o"));
        assertEquals("Option value should match", "test.txt", cmd.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testParseMultipleOptions() throws ParseException {
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b"});
        
        assertTrue("Should have option a", cmd.hasOption("a"));
        assertTrue("Should have option b", cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testParseWithArguments() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v", "file1.txt", "file2.txt"});
        
        assertTrue("Should have verbose option", cmd.hasOption("v"));
        assertEquals("Should have 2 args", 2, cmd.getArgs().length);
        assertEquals("First arg should match", "file1.txt", cmd.getArgs()[0]);
        assertEquals("Second arg should match", "file2.txt", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseDoubleDash() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"--", "-v", "file.txt"});
        
        assertFalse("Should not have verbose option (after --)", cmd.hasOption("v"));
        assertEquals("Should have 2 args", 2, cmd.getArgs().length);
        assertEquals("First arg should be -v", "-v", cmd.getArgs()[0]);
        assertEquals("Second arg should be file.txt", "file.txt", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDash() throws ParseException {
        Options options = new Options();
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-"});
        
        assertEquals("Should have 1 arg", 1, cmd.getArgs().length);
        assertEquals("Arg should be -", "-", cmd.getArgs()[0]);
    }

    /* ===================================================================
     * PARTITION B: Boundary Value Analysis & Extremes
     * =================================================================== */

    @Test(timeout = 4000)
    public void testParseNullArguments() throws ParseException {
        Options options = new Options();
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, (String[]) null);
        
        assertEquals("Args should be empty with null input", 0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseEmptyArguments() throws ParseException {
        Options options = new Options();
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[0]);
        
        assertEquals("Args should be empty", 0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseNullProperties() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v"}, (Properties) null);
        
        assertTrue("Should have verbose option", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testParseEmptyProperties() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v"}, new Properties());
        
        assertTrue("Should have verbose option", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testParseWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-v", "unknown", "-o"}, true);
        
        assertTrue("Should have verbose option", cmd.hasOption("v"));
        assertEquals("Should have 2 args", 2, cmd.getArgs().length);
        assertEquals("First arg should be unknown", "unknown", cmd.getArgs()[0]);
        assertEquals("Second arg should be -o", "-o", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseWithStopAtNonOptionAndUnknownOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-x", "-v"}, true);
        
        assertFalse("Should not have unknown option", cmd.hasOption("x"));
        assertTrue("Should have verbose option", cmd.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testParseWithStopAtNonOptionDoubleDash() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"--", "-v"}, true);
        
        assertFalse("Should not have verbose option", cmd.hasOption("v"));
        assertEquals("Should have 1 arg", 1, cmd.getArgs().length);
        assertEquals("Arg should be -v", "-v", cmd.getArgs()[0]);
    }

    /* ===================================================================
     * PARTITION C: Defect-Targeted Branch Zone
     * =================================================================== */

    /**
     * Directly targets the known defect from Defects4J:
     * org.apache.commons.cli.ValueTest::testPropertyOptionFlags
     * 
     * This test verifies that property options with flag values "true"/"yes"/"1" 
     * are correctly added to the command line, and other values are not.
     */
    @Test(timeout = 4000)
    public void testPropertyOptionFlags() throws ParseException {
        Options options = new Options();
        Option flagOpt = new Option("f", "flag", false, "a flag option");
        Option argOpt = OptionBuilder.withLongOpt("arg").hasArg().create("a");
        
        options.addOption(flagOpt);
        options.addOption(argOpt);
        
        Parser parser = createTestParser();
        CommandLine cmd;
        Properties props;
        
        // Test 1: flag option with "true" value should add the option
        props = new Properties();
        props.setProperty("f", "true");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with 'true' should be added", cmd.hasOption("f"));
        
        // Test 2: flag option with "false" value should NOT add the option
        props = new Properties();
        props.setProperty("f", "false");
        cmd = parser.parse(options, new String[0], props);
        assertFalse("Flag option with 'false' should NOT be added", cmd.hasOption("f"));
        
        // Test 3: flag option with "yes" value should add the option
        props = new Properties();
        props.setProperty("f", "yes");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with 'yes' should be added", cmd.hasOption("f"));
        
        // Test 4: flag option with "no" value should NOT add the option
        props = new Properties();
        props.setProperty("f", "no");
        cmd = parser.parse(options, new String[0], props);
        assertFalse("Flag option with 'no' should NOT be added", cmd.hasOption("f"));
        
        // Test 5: flag option with "1" value should add the option
        props = new Properties();
        props.setProperty("f", "1");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with '1' should be added", cmd.hasOption("f"));
        
        // Test 6: flag option with "0" value should NOT add the option
        props = new Properties();
        props.setProperty("f", "0");
        cmd = parser.parse(options, new String[0], props);
        assertFalse("Flag option with '0' should NOT be added", cmd.hasOption("f"));
        
        // Test 7: flag option with uppercase "TRUE" should add the option
        props = new Properties();
        props.setProperty("f", "TRUE");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with uppercase 'TRUE' should be added", cmd.hasOption("f"));
        
        // Test 8: arg option with value should add the option
        props = new Properties();
        props.setProperty("a", "value1");
        props.setProperty("f", "true");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Arg option should be added", cmd.hasOption("a"));
        assertEquals("Arg option value should match", "value1", cmd.getOptionValue("a"));
        
        // Test 9: Check that option already on command line from arguments takes precedence
        cmd = parser.parse(options, new String[]{"-f"}, props);
        assertTrue("Flag option from args should still be present", cmd.hasOption("f"));
    }

    @Test(timeout = 4000)
    public void testPropertyOptionFlagsWithMixedCase() throws ParseException {
        Options options = new Options();
        Option flagOpt = new Option("f", "flag", false, "a flag option");
        options.addOption(flagOpt);
        
        Parser parser = createTestParser();
        Properties props = new Properties();
        props.setProperty("f", "True");
        
        CommandLine cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with 'True' (mixed case) should be added", cmd.hasOption("f"));
        
        props = new Properties();
        props.setProperty("f", "Yes");
        cmd = parser.parse(options, new String[0], props);
        assertTrue("Flag option with 'Yes' (mixed case) should be added", cmd.hasOption("f"));
    }

    /**
     * Additional defect-targeted test: Required options from properties.
     */
    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testRequiredOptionNotProvided() throws ParseException {
        Options options = new Options();
        Option requiredOpt = new Option("r", "required", true, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);
        
        Parser parser = createTestParser();
        parser.parse(options, new String[0]);
    }

    @Test(timeout = 4000)
    public void testRequiredOptionProvidedViaProperties() throws ParseException {
        Options options = new Options();
        Option requiredOpt = new Option("r", "required", true, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);
        
        Properties props = new Properties();
        props.setProperty("r", "value");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[0], props);
        
        assertTrue("Required option should be present", cmd.hasOption("r"));
        assertEquals("Required option value should match", "value", cmd.getOptionValue("r"));
    }

    /* ===================================================================
     * PARTITION D: Exception & Defensive Guard Paths
     * =================================================================== */

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnknownOptionWithoutStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        parser.parse(options, new String[]{"-x"});
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testMissingArgForOption() throws ParseException {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        
        Parser parser = createTestParser();
        parser.parse(options, new String[]{"-o"});
    }

    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testMissingRequiredOptions() throws ParseException {
        Options options = new Options();
        Option opt = new Option("r", false, "required");
        opt.setRequired(true);
        options.addOption(opt);
        
        Parser parser = createTestParser();
        parser.parse(options, new String[]{});
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionInStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-x", "arg1"}, true);
        
        assertEquals("Unrecognized option should be added to args", 2, cmd.getArgs().length);
        assertEquals("First arg should be -x", "-x", cmd.getArgs()[0]);
    }

    /* ===================================================================
     * PARTITION E: Object Lifecycle & Contract Integrity
     * =================================================================== */

    @Test(timeout = 4000)
    public void testParserStateClearedBetweenParses() throws ParseException {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        options.addOption("o", true, "output");
        
        Parser parser = createTestParser();
        
        // First parse
        parser.parse(options, new String[]{"-v"});
        
        // Second parse should work correctly
        CommandLine cmd2 = parser.parse(options, new String[]{"-o", "file.txt"});
        
        assertTrue("Second parse should have output option", cmd2.hasOption("o"));
        assertEquals("Second parse output value should match", "file.txt", cmd2.getOptionValue("o"));
        assertFalse("Second parse should not have verbose option", cmd2.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupSelection() throws ParseException {
        Options options = new Options();
        
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "option a");
        Option opt2 = new Option("b", "option b");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        
        assertTrue("Should have option a", cmd.hasOption("a"));
        assertFalse("Should not have option b", cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupWithRequired() throws ParseException {
        Options options = new Options();
        
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option opt1 = new Option("a", "option a");
        Option opt2 = new Option("b", "option b");
        group.addOption(opt1);
        group.addOption(opt2);
        options.addOptionGroup(group);
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-b"});
        
        assertTrue("Should have option b", cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testOptionValuesClearedBeforeParse() throws ParseException {
        Options options = new Options();
        Option opt = new Option("o", "output", true, "output file");
        options.addOption(opt);
        
        Parser parser = createTestParser();
        
        // First parse with value
        parser.parse(options, new String[]{"-o", "first.txt"});
        
        // Second parse should clear values
        CommandLine cmd2 = parser.parse(options, new String[]{"-o", "second.txt"});
        
        assertEquals("Option value should be from second parse", "second.txt", cmd2.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testPropertiesWithExistingOption() throws ParseException {
        Options options = new Options();
        Option flagOpt = new Option("f", "flag", false, "flag");
        options.addOption(flagOpt);
        
        Parser parser = createTestParser();
        
        // First set the option via command line
        Properties props = new Properties();
        props.setProperty("f", "true");
        
        // When option already set from command line, properties should not override
        CommandLine cmd = parser.parse(options, new String[]{"-f"}, props);
        assertTrue("Option should be present from command line", cmd.hasOption("f"));
    }

    @Test(timeout = 4000)
    public void testPropertiesValueForArgOption() throws ParseException {
        Options options = new Options();
        Option argOpt = OptionBuilder.withLongOpt("arg").hasArg().create("a");
        options.addOption(argOpt);
        
        Parser parser = createTestParser();
        
        Properties props = new Properties();
        props.setProperty("a", "value1");
        
        // First parse
        CommandLine cmd1 = parser.parse(options, new String[0], props);
        assertEquals("First parse value should be value1", "value1", cmd1.getOptionValue("a"));
        
        // Second parse with different property value
        props.setProperty("a", "value2");
        CommandLine cmd2 = parser.parse(options, new String[0], props);
        assertEquals("Second parse value should be value2", "value2", cmd2.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testProcessArgsWithQuotedValue() throws ParseException {
        Options options = new Options();
        Option opt = new Option("o", "output", true, "output file");
        options.addOption(opt);
        
        Parser parser = createTestParser();
        CommandLine cmd = parser.parse(options, new String[]{"-o", "\"test file.txt\""});
        
        assertEquals("Quoted value should be unquoted", "test file.txt", cmd.getOptionValue("o"));
    }
}