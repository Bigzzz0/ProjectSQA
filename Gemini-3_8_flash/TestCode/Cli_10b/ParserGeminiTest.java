package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli.Parser
 *
 * 1. Branch / Condition Coverage:
 * - parse(Options, String[], Properties, boolean):
 *   - options.helpOptions() iteration and Option.clearValues() invocation
 *   - arguments == null guard -> assigned new String[0]
 *   - Token loop:
 *     - "--" token: triggers eatTheRest = true, skips subsequent "--" tokens while draining
 *     - "-" token:
 *       - stopAtNonOption == true: triggers eatTheRest = true
 *       - stopAtNonOption == false: cmd.addArg("-")
 *     - startsWith("-"):
 *       - stopAtNonOption && !hasOption(t): eatTheRest = true, cmd.addArg(t)
 *       - else: processOption(t, iterator)
 *     - non-option argument:
 *       - cmd.addArg(t)
 *       - stopAtNonOption == true: triggers eatTheRest = true
 *     - eatTheRest handling: drains remaining tokens, filtering duplicate "--"
 * - processProperties(Properties):
 *   - properties == null early exit
 *   - cmd.hasOption(option) == true: skip property
 *   - opt.hasArg() == true:
 *     - opt.getValues() == null or empty: opt.addValueForProcessing(value)
 *     - RuntimeException swallowed
 *   - opt.hasArg() == false:
 *     - value in ("yes", "true", "1") [case-insensitive]: added to cmd
 *     - other value: triggers break statement terminating property iteration
 * - checkRequiredOptions():
 *   - requiredOptions.size() == 0: no exception
 *   - requiredOptions.size() == 1: "Missing required option: "
 *   - requiredOptions.size() > 1: "Missing required options: "
 * - processArgs(Option, ListIterator):
 *   - Token loop breaks if next token startsWith("-") and hasOption(token) == true (iter.previous())
 *   - opt.addValueForProcessing quotes stripping
 *   - RuntimeException caught -> iter.previous() and break
 *   - MissingArgumentException thrown if opt.getValues() == null && !opt.hasOptionalArg()
 *   - Optional argument (hasOptionalArg == true) bypasses MissingArgumentException when empty
 * - processOption(String, ListIterator):
 *   - !hasOption: throws UnrecognizedOptionException
 *   - opt.isRequired(): removes from requiredOptions list
 *   - OptionGroup presence:
 *     - group.isRequired(): removes group from requiredOptions list
 *     - group.setSelected(opt)
 *   - opt.hasArg(): delegates to processArgs
 *
 * 2. Defect Specification (Defects4J - CLI Ground Truth):
 * - Failure: testReuseOptionsTwice -> MissingOptionException not thrown.
 *   Root Cause: Parser modifies the internal List returned by options.getRequiredOptions()
 *   in-place (via getRequiredOptions().remove(...)), corrupting the Options instance
 *   for subsequent parse calls.
 */
public class ParserGeminiTest {

    /**
     * Concrete test implementation of abstract Parser using basic pass-through flattening.
     */
    private static class TestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseSimpleOptionsAndArgs() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "toggle a");
        options.addOption("b", true, "value b");

        String[] args = new String[] { "-a", "-b", "foo", "extra1", "extra2" };
        CommandLine cl = parser.parse(options, args);

        assertNotNull("CommandLine should not be null", cl);
        assertTrue("Option -a should be present", cl.hasOption("a"));
        assertTrue("Option -b should be present", cl.hasOption("b"));
        assertEquals("foo", cl.getOptionValue("b"));
        List argList = cl.getArgList();
        assertEquals(2, argList.size());
        assertEquals("extra1", argList.get(0));
        assertEquals("extra2", argList.get(1));
    }

    @Test(timeout = 4000)
    public void testParseWithPropertiesFlagOptions() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");

        Properties props = new Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "yes");
        props.setProperty("c", "1");

        CommandLine cl = parser.parse(options, new String[0], props);

        assertTrue("Flag 'a' enabled via 'true'", cl.hasOption("a"));
        assertTrue("Flag 'b' enabled via 'yes'", cl.hasOption("b"));
        assertTrue("Flag 'c' enabled via '1'", cl.hasOption("c"));
    }

    @Test(timeout = 4000)
    public void testParseWithPropertiesValuedOption() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("f", true, "file path");

        Properties props = new Properties();
        props.setProperty("f", "/tmp/config.xml");

        CommandLine cl = parser.parse(options, new String[0], props);

        assertTrue("Option -f should be present from properties", cl.hasOption("f"));
        assertEquals("/tmp/config.xml", cl.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testParseCommandLineOverridesProperties() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("f", true, "file path");

        Properties props = new Properties();
        props.setProperty("f", "propValue");

        CommandLine cl = parser.parse(options, new String[] { "-f", "cliValue" }, props);

        assertTrue(cl.hasOption("f"));
        assertEquals("CommandLine value must take precedence over property", "cliValue", cl.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupHandling() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();

        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        Option optA = new Option("a", "alpha", false, "alpha option");
        Option optB = new Option("b", "beta", false, "beta option");
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[] { "-b" });

        assertTrue("Option -b should be selected in group", cl.hasOption("b"));
        assertEquals("b", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testProtectedGettersAndSetters() {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("o", false, "desc");

        parser.setOptions(options);

        assertSame("getOptions() should return provided options", options, parser.getOptions());
        assertNotNull("getRequiredOptions() should return list", parser.getRequiredOptions());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseNullArgumentsArray() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("x", false, "toggle");

        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("x"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseEmptyArgumentsArray() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();

        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseDoubleDashStopsOptionProcessing() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        String[] args = new String[] { "-a", "--", "-b", "--", "arg1" };
        CommandLine cl = parser.parse(options, args);

        assertTrue("Option -a before '--' should be processed", cl.hasOption("a"));
        assertFalse("Option -b after '--' should be treated as non-option argument", cl.hasOption("b"));
        String[] remaining = cl.getArgs();
        assertEquals(2, remaining.length);
        assertEquals("-b", remaining.0 == 0 ? remaining[0] : "");
        assertEquals("arg1", remaining[1]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDashWithoutStopAtNonOption() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();

        String[] args = new String[] { "-" };
        CommandLine cl = parser.parse(options, args, false);

        assertEquals("Single dash should be added to args when stopAtNonOption is false", 1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDashWithStopAtNonOption() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();

        String[] args = new String[] { "-", "remaining" };
        CommandLine cl = parser.parse(options, args, true);

        String[] extraArgs = cl.getArgs();
        assertEquals(1, extraArgs.length);
        assertEquals("remaining", extraArgs[0]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedOptionToken() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "known option");

        String[] args = new String[] { "-a", "-unknown", "extra" };
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("a"));
        String[] extraArgs = cl.getArgs();
        assertEquals(2, extraArgs.length);
        assertEquals("-unknown", extraArgs[0]);
        assertEquals("extra", extraArgs[1]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithNormalArgument() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "known option");

        String[] args = new String[] { "nonOptionArg", "-a" };
        CommandLine cl = parser.parse(options, args, true);

        assertFalse("Option -a after first non-option should not be processed as option", cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOptionArg", cl.getArgs()[0]);
        assertEquals("-a", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testStrippingQuotesFromOptionValues() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("v", true, "value with quotes");

        CommandLine cl = parser.parse(options, new String[] { "-v", "\"quoted value\"" });

        assertEquals("Quotes should be stripped from argument value", "quoted value", cl.getOptionValue("v"));
    }

    @Test(timeout = 4000)
    public void testOptionalArgumentHandling() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("o", "optional", true, "optional argument");
        opt.setOptionalArg(true);
        options.addOption(opt);

        // Option provided without value, followed by next option
        options.addOption("n", false, "next option");
        CommandLine cl = parser.parse(options, new String[] { "-o", "-n" });

        assertTrue(cl.hasOption("o"));
        assertTrue(cl.hasOption("n"));
        assertNull("Optional arg value should be null if not supplied", cl.getOptionValue("o"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Target: org.apache.commons.cli.ParseRequiredTest::testReuseOptionsTwice
     * Parser modifies the Options internal required list in-place. When reusing Options,
     * the second parse fails to detect missing required options because the list was emptied.
     */
    @Test(timeout = 4000)
    public void testReuseOptionsTwice() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option requiredOpt = new Option("r", "req", false, "required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);

        // Run 1: supply required option -> must succeed
        CommandLine cl = parser.parse(options, new String[] { "-r" });
        assertTrue(cl.hasOption("r"));

        // Run 2: reuse options instance without supplying required option
        // Fault in target class: MissingOptionException is NOT thrown on the second run!
        try {
            parser.parse(options, new String[0]);
            fail("MissingOptionException should have been thrown on second parse using same Options");
        } catch (MissingOptionException expected) {
            assertTrue("Exception message should reference required option",
                    expected.getMessage().contains("r"));
        }
    }

    @Test(timeout = 4000)
    public void testOptionValuesClearedBetweenParses() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("v", true, "valued option");
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[] { "-v", "val1" });
        assertEquals("val1", cl1.getOptionValue("v"));

        // Second parse without option -> option's internal values must have been cleared
        CommandLine cl2 = parser.parse(options, new String[0]);
        assertFalse(cl2.hasOption("v"));
        assertNull("Option internal values must be cleared across parses", opt.getValues());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedOptionThrowsException() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "valid");

        parser.parse(options, new String[] { "-invalid" }, false);
    }

    @Test(timeout = 4000)
    public void testSingleMissingRequiredOptionMessage() {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option req = new Option("r", false, "required");
        req.setRequired(true);
        options.addOption(req);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException ex) {
            assertEquals("Missing required option: r", ex.getMessage());
        } catch (ParseException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testMultipleMissingRequiredOptionsMessage() {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option r1 = new Option("a", false, "required A");
        r1.setRequired(true);
        Option r2 = new Option("b", false, "required B");
        r2.setRequired(true);
        options.addOption(r1);
        options.addOption(r2);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException");
        } catch (MissingOptionException ex) {
            assertTrue("Message should indicate multiple options",
                    ex.getMessage().startsWith("Missing required options: "));
            assertTrue(ex.getMessage().contains("a"));
            assertTrue(ex.getMessage().contains("b"));
        } catch (ParseException e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testMissingArgumentThrowsException() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("v", true, "requires argument");

        parser.parse(options, new String[] { "-v" });
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testMissingArgumentBeforeNextOptionThrowsException() throws