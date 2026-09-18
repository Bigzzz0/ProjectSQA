package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for Parser.java targeting line/branch coverage and the known
 * defect in checkRequiredOptions() (missing exception message prefix).
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional paths (parse with options, arguments, properties)
 * - Partition B: Boundary values (null arguments, empty arrays, stopAtNonOption)
 * - Partition C: Defect-targeted branch (required options missing – message format)
 * - Partition D: Exception paths (unrecognized option, missing argument, illegal state)
 * - Partition E: Object lifecycle (clearValues, option groups, required removal)
 *
 * Known defect: MissingOptionException message is empty instead of "Missing required option(s): ..."
 */
public class ParserDeepseekTest {

    // Minimal concrete Parser for testing abstract methods
    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments;
        }
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testParseSimpleOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose mode");
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-v"});
        assertTrue("Option 'v' should be present", cl.hasOption("v"));
        assertEquals("No extra args expected", 0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseOptionWithArgument() throws ParseException {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-o", "out.txt"});
        assertTrue("Option 'o' should be present", cl.hasOption("o"));
        assertEquals("Argument value", "out.txt", cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testParseProperties() throws ParseException {
        Options options = new Options();
        options.addOption("d", "debug", false, "debug mode");
        Parser parser = new TestParser();
        Properties props = new Properties();
        props.setProperty("d", "true");
        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue("Option 'd' should be set from properties", cl.hasOption("d"));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testParseNullArguments() throws ParseException {
        Options options = new Options();
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, (String[]) null);
        assertEquals("No args expected", 0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseEmptyArguments() throws ParseException {
        Options options = new Options();
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[0]);
        assertEquals("No args expected", 0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseDoubleDash() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"--", "-v", "extra"});
        assertFalse("Option after -- should not be parsed", cl.hasOption("v"));
        assertEquals("Extra args after --", 2, cl.getArgs().length);
        assertEquals("First extra arg", "-v", cl.getArgs()[0]);
        assertEquals("Second extra arg", "extra", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDash() throws ParseException {
        Options options = new Options();
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-"});
        assertEquals("Single dash should be added as arg", 1, cl.getArgs().length);
        assertEquals("Arg is '-'", "-", cl.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testParseStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-v", "nonopt", "-x"}, true);
        assertTrue("Option -v should be parsed", cl.hasOption("v"));
        assertEquals("Remaining args after non-option", 2, cl.getArgs().length);
        assertEquals("First arg", "nonopt", cl.getArgs()[0]);
        assertEquals("Second arg", "-x", cl.getArgs()[1]);
    }

    // ========== Partition C: Defect-Targeted Branch (MissingOptionException message) ==========

    @Test(timeout = 4000)
    public void testMissingRequiredOptionMessage() {
        Options options = new Options();
        options.addOption("f", "file", true, "file option");
        options.addOption("x", "xopt", false, "x option");
        // Make both required
        Option f = options.getOption("f");
        f.setRequired(true);
        Option x = options.getOption("x");
        x.setRequired(true);
        Parser parser = new TestParser();
        try {
            parser.parse(options, new String[]{"-f", "test.txt"});
            fail("Expected MissingOptionException for missing -x");
        } catch (MissingOptionException e) {
            // Defect: message is empty; correct should contain "Missing required option(s): x"
            String msg = e.getMessage();
            assertNotNull("Message should not be null", msg);
            assertTrue("Message should contain 'x'", msg.contains("x"));
            // The known defect is that the message is empty; we assert it's not empty
            assertFalse("Message should not be empty (defect check)", msg.isEmpty());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMissingRequiredOptionsMultipleMessage() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("b", "beta", false, "beta");
        options.addOption("c", "gamma", false, "gamma");
        options.getOption("a").setRequired(true);
        options.getOption("b").setRequired(true);
        options.getOption("c").setRequired(true);
        Parser parser = new TestParser();
        try {
            parser.parse(options, new String[]{});
            fail("Expected MissingOptionException for missing a,b,c");
        } catch (MissingOptionException e) {
            String msg = e.getMessage();
            assertNotNull("Message should not be null", msg);
            // Should contain all required option keys
            assertTrue("Message should contain 'a'", msg.contains("a"));
            assertTrue("Message should contain 'b'", msg.contains("b"));
            assertTrue("Message should contain 'c'", msg.contains("c"));
            assertFalse("Message should not be empty (defect check)", msg.isEmpty());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOption() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = new TestParser();
        parser.parse(options, new String[]{"-x"});
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testMissingArgumentForOption() throws ParseException {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        Parser parser = new TestParser();
        parser.parse(options, new String[]{"-o"});
    }

    @Test(timeout = 4000)
    public void testProcessArgsWithOptionStop() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        options.addOption("o", "output", true, "output file");
        Parser parser = new TestParser();
        // -o should consume "value", then -v stops argument processing
        CommandLine cl = parser.parse(options, new String[]{"-o", "value", "-v"});
        assertTrue("Option -o should be present", cl.hasOption("o"));
        assertEquals("Option -o value", "value", cl.getOptionValue("o"));
        assertTrue("Option -v should be present", cl.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testProcessArgsWithOptionalArg() throws ParseException {
        Options options = new Options();
        Option opt = OptionBuilder.withLongOpt("opt").hasOptionalArg().create('o');
        options.addOption(opt);
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-o"});
        assertTrue("Option -o should be present", cl.hasOption("o"));
        assertNull("Optional arg should be null", cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupRequired() throws ParseException {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option a = new Option("a", "alpha", false, "alpha");
        Option b = new Option("b", "beta", false, "beta");
        group.addOption(a);
        group.addOption(b);
        options.addOptionGroup(group);
        Parser parser = new TestParser();
        // Provide one option from the group
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue("Option -a should be present", cl.hasOption("a"));
        // No MissingOptionException because group is satisfied
    }

    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testOptionGroupRequiredMissing() throws ParseException {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option a = new Option("a", "alpha", false, "alpha");
        Option b = new Option("b", "beta", false, "beta");
        group.addOption(a);
        group.addOption(b);
        options.addOptionGroup(group);
        Parser parser = new TestParser();
        parser.parse(options, new String[]{});
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testClearValuesOnReuse() throws ParseException {
        Options options = new Options();
        Option opt = new Option("o", "output", true, "output file");
        options.addOption(opt);
        Parser parser = new TestParser();
        // First parse
        parser.parse(options, new String[]{"-o", "first.txt"});
        assertEquals("First value", "first.txt", opt.getValue());
        // Second parse should clear values (CLI-71)
        parser.parse(options, new String[]{"-o", "second.txt"});
        assertEquals("Value should be overwritten", "second.txt", opt.getValue());
        assertEquals("Only one value stored", 1, opt.getValues().length);
    }

    @Test(timeout = 4000)
    public void testRequiredOptionRemovedAfterProcessing() throws ParseException {
        Options options = new Options();
        Option req = new Option("r", "required", false, "required");
        req.setRequired(true);
        options.addOption(req);
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue("Required option should be present", cl.hasOption("r"));
        // No exception thrown
    }

    @Test(timeout = 4000)
    public void testPropertiesWithNonBooleanValue() throws ParseException {
        Options options = new Options();
        options.addOption("d", "debug", false, "debug mode");
        Parser parser = new TestParser();
        Properties props = new Properties();
        props.setProperty("d", "false"); // not yes/true/1
        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertFalse("Option should not be added for non-true value", cl.hasOption("d"));
    }

    @Test(timeout = 4000)
    public void testPropertiesWithArgOption() throws ParseException {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        Parser parser = new TestParser();
        Properties props = new Properties();
        props.setProperty("o", "out.txt");
        CommandLine cl = parser.parse(options, new String[]{}, props);
        assertTrue("Option should be added from properties", cl.hasOption("o"));
        assertEquals("Value from properties", "out.txt", cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testPropertiesNull() throws ParseException {
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose");
        Parser parser = new TestParser();
        CommandLine cl = parser.parse(options, new String[]{}, (Properties) null);
        assertFalse("No option should be set", cl.hasOption("v"));
    }
}