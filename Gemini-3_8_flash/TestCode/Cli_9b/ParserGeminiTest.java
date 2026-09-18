/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.Parser
 *
 * Decision / Condition Coverage Targets:
 * 1. parse(Options, String[], Properties, boolean):
 *    - arguments is null vs non-null (creates empty array)
 *    - helpOptions loop: clearValues() executed for options
 *    - token == "--": eatTheRest = true; skips subsequent "--" tokens, captures others into cmd.args
 *    - token == "-": stopAtNonOption = true (eatTheRest = true) vs stopAtNonOption = false (cmd.addArg("-"))
 *    - token starts with "-":
 *        * stopAtNonOption = true && !hasOption: eatTheRest = true; cmd.addArg(t)
 *        * hasOption or stopAtNonOption = false: processOption(t, iterator)
 *    - token is regular argument: cmd.addArg(t); stopAtNonOption = true (eatTheRest = true) vs false
 *    - eatTheRest handling of trailing "--" vs normal tokens
 * 2. processProperties(Properties):
 *    - properties == null: early return
 *    - cmd.hasOption(option) is true: skipped
 *    - opt.hasArg(): opt.getValues() null/empty -> addValueForProcessing(value) vs exception caught
 *    - !opt.hasArg():
 *        * value in ("yes", "true", "1") -> cmd.addOption(opt)
 *        * value NOT in ("yes", "true", "1") -> breaks loop
 * 3. checkRequiredOptions():
 *    - requiredOptions empty: no exception
 *    - requiredOptions.size() == 1: "Missing required option: "
 *    - requiredOptions.size() > 1: "Missing required options: "
 *    - DEFECT CLI-72 / Defects4J: Missing comma/separator between multiple missing options
 * 4. processArgs(Option, ListIterator):
 *    - next token is an option (hasOption(str) && str.startsWith("-")): iter.previous() and break
 *    - next token is an argument: quotes stripped, addValueForProcessing succeeds vs throws RuntimeException
 *    - opt.getValues() == null && !opt.hasOptionalArg(): throws MissingArgumentException
 *    - opt.getValues() == null && opt.hasOptionalArg(): succeeds without exception
 * 5. processOption(String, ListIterator):
 *    - unrecognized option: throws UnrecognizedOptionException
 *    - opt.isRequired(): removes from requiredOptions
 *    - opt in OptionGroup:
 *        * group.isRequired(): removes group from requiredOptions
 *        * group.setSelected(opt): throws AlreadySelectedException if conflicting
 *    - opt.hasArg(): calls processArgs
 *    - cmd.addOption(opt) executed
 * 6. Getters/Setters & Overloaded parse methods:
 *    - setOptions/getOptions/getRequiredOptions
 *    - 2-arg, 3-arg, 4-arg parse overloads
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

/**
 * Advanced White-Box Test Suite for {@link Parser}.
 */
public class ParserGeminiTest {

    /**
     * Concrete test implementation of the abstract {@link Parser} class
     * that passes arguments through without mutation.
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
    public void testBasicParsingTwoArgs() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "toggle a");
        options.addOption("b", true, "value b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "valB", "extraArg"});
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("valB", cl.getOptionValue("b"));
        List args = cl.getArgList();
        assertEquals(1, args.size());
        assertEquals("extraArg", args.get(0));
    }

    @Test(timeout = 4000)
    public void testOverloadedParseSignatures() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("x", false, "opt x");

        // 2-arg parse(Options, String[])
        CommandLine cl1 = parser.parse(options, new String[]{"-x"});
        assertTrue(cl1.hasOption("x"));

        // 3-arg parse(Options, String[], boolean)
        CommandLine cl2 = parser.parse(options, new String[]{"-x", "foo"}, true);
        assertTrue(cl2.hasOption("x"));
        assertEquals("foo", cl2.getArgs()[0]);

        // 3-arg parse(Options, String[], Properties)
        Properties props = new Properties();
        props.setProperty("x", "true");
        CommandLine cl3 = parser.parse(options, new String[0], props);
        assertTrue(cl3.hasOption("x"));
    }

    @Test(timeout = 4000)
    public void testGettersAndSetters() {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option requiredOpt = OptionBuilder.isRequired().create("r");
        options.addOption(requiredOpt);

        parser.setOptions(options);
        assertSame(options, parser.getOptions());
        assertNotNull(parser.getRequiredOptions());
        assertEquals(1, parser.getRequiredOptions().size());
        assertEquals("r", parser.getRequiredOptions().get(0));
    }

    @Test(timeout = 4000)
    public void testDoubleDashDelimiterStopsOptionParsing() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");
        options.addOption("b", false, "opt b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "--", "file.txt"});
        assertTrue(cl.hasOption("a"));
        assertFalse("Option after -- should not be recognized as an option", cl.hasOption("b"));

        String[] args = cl.getArgs();
        // The first -- triggers eatTheRest; the second -- is skipped by "!\"--\".equals(str)"
        assertEquals(2, args.length);
        assertEquals("-b", args[0]);
        assertEquals("file.txt", args[1]);
    }

    @Test(timeout = 4000)
    public void testSingleDashWithAndWithoutStopAtNonOption() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        // stopAtNonOption = false: single dash is added to cmd as argument
        CommandLine cl1 = parser.parse(options, new String[]{"-a", "-", "next"}, false);
        assertTrue(cl1.hasOption("a"));
        assertEquals(2, cl1.getArgs().length);
        assertEquals("-", cl1.getArgs()[0]);
        assertEquals("next", cl1.getArgs()[1]);

        // stopAtNonOption = true: single dash triggers eatTheRest
        CommandLine cl2 = parser.parse(options, new String[]{"-a", "-", "next"}, true);
        assertTrue(cl2.hasOption("a"));
        assertEquals(1, cl2.getArgs().length);
        assertEquals("next", cl2.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedOption() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        // With stopAtNonOption = true, unrecognized option "-unknown" starts eating the rest
        CommandLine cl = parser.parse(options, new String[]{"-a", "-unknown", "extra1", "extra2"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(3, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("extra1", cl.getArgs()[1]);
        assertEquals("extra2", cl.getArgs()[2]);
    }

    @Test(timeout = 4000)
    public void testNonOptionArgumentStopsParsingWhenStopAtNonOption() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");
        options.addOption("b", false, "opt b");

        CommandLine cl = parser.parse(options, new String[]{"-a", "nonOptionToken", "-b"}, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOptionToken", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullArgumentsArray() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testEmptyArgumentsArray() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testOptionReinitializationClearsOldValues() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("o", true, "option with arg");
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[]{"-o", "val1"});
        assertEquals("val1", cl1.getOptionValue("o"));

        // Reparsing must clear previous values from option
        CommandLine cl2 = parser.parse(options, new String[]{"-o", "val2"});
        assertEquals("val2", cl2.getOptionValue("o"));
        assertEquals(1, cl2.getOptionValues("o").length);
    }

    @Test(timeout = 4000)
    public void testOptionWithQuotesAreStripped() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("s", true, "quoted string");

        CommandLine cl = parser.parse(options, new String[]{"-s", "\"quoted value\""});
        assertEquals("quoted value", cl.getOptionValue("s"));
    }

    @Test(timeout = 4000)
    public void testOptionalArgumentHandling() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("o", "optional", true, "optional arg");
        opt.setOptionalArg(true);
        options.addOption(opt);
        options.addOption("b", false, "another option");

        // Case 1: Optional arg is omitted, followed by another option
        CommandLine cl1 = parser.parse(options, new String[]{"-o", "-b"});
        assertTrue(cl1.hasOption("o"));
        assertTrue(cl1.hasOption("b"));
        assertNull(cl1.getOptionValue("o"));

        // Case 2: Optional arg is provided
        CommandLine cl2 = parser.parse(options, new String[]{"-o", "myValue"});
        assertTrue(cl2.hasOption("o"));
        assertEquals("myValue", cl2.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testOptionArgLimitExceededBreaksArgumentConsuming() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("s", true, "single argument option");
        opt.setArgs(1);
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-s", "val1", "val2"});
        assertTrue(cl.hasOption("s"));
        assertEquals("val1", cl.getOptionValue("s"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("val2", cl.getArgs()[0]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-72 / Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known defect:
     * When multiple required options are missing, checkRequiredOptions() joins their names
     * without separator: "Missing required options: bc" instead of "Missing required options: b, c".
     */
    @Test(timeout = 4000)
    public void testMultipleMissingRequiredOptionsExceptionMessage() {
        Parser parser = new TestParser();
        Options options = new Options();
        Option optB = OptionBuilder.isRequired().create("b");
        Option optC = OptionBuilder.isRequired().create("c");
        options.addOption(optB);
        options.addOption(optC);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException was not thrown");
        } catch (MissingOptionException e) {
            // Defect Assertion: Verifies presence of comma-separated list
            // Defective version produces: "Missing required options: bc"
            // Fixed version produces: "Missing required options: b, c"
            String msg = e.getMessage();
            assertTrue("Exception message must indicate plural missing options",
                    msg.startsWith("Missing required options: "));
            assertTrue("Multiple missing options must be separated by comma and space",
                    msg.equals("Missing required options: b, c") || msg.equals("Missing required options: c, b"));
        } catch (ParseException e) {
            fail("Unexpected ParseException type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testSingleMissingRequiredOptionExceptionMessage() {
        Parser parser = new TestParser();
        Options options = new Options();
        Option optA = OptionBuilder.isRequired().create("a");
        options.addOption(optA);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException was not thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required option: a", e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException type: " + e.getClass().getName());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedOptionThrowsException() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        parser.parse(options, new String[]{"-unknown"}, false);
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentThrowsException() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("b", true, "requires argument");

        parser.parse(options, new String[]{"-b"});
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentFollowedByOptionThrowsException() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("b", true, "requires argument");
        options.addOption("c", false, "flag option");

        parser.parse(options, new String[]{"-b", "-c"});
    }

    @Test(timeout = 4000)
    public void testRequiredOptionSatisfiedDoesNotThrow() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        Option opt = OptionBuilder.isRequired().create("r");
        options.addOption(opt);

        CommandLine cl = parser.parse(options, new String[]{"-r"});
        assertTrue(cl.hasOption("r"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupRequiredAndSelection() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("e", "edit"));
        group.addOption(new Option("v", "view"));
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-e"});
        assertTrue(cl.hasOption("e"));
        assertFalse(cl.hasOption("v"));
        assertEquals("e", group.getSelected());
    }

    @Test(expected = AlreadySelectedException.class, timeout = 4000)
    public void testOptionGroupAlreadySelectedConflictThrowsException() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("e", "edit"));
        group.addOption(new Option("v", "view"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-e", "-v"});
    }

    @Test(expected = MissingOptionException.class, timeout = 4000)
    public void testMissingRequiredOptionGroupThrowsException() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("e", "edit"));
        group.addOption(new Option("v", "view"));
        options.addOptionGroup(group);

        parser.parse(options, new String[0]);
    }

    // =========================================================================
    // Partition E: Properties Processing & Complex Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testProcessPropertiesWithNullProperties() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");

        CommandLine cl = parser.parse(options, new String[]{"-a"}, null, false);
        assertTrue(cl.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesWithValueArgs() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("p", true, "param from properties");

        Properties props = new Properties();
        props.setProperty("p", "propertyValue");

        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertTrue(cl.hasOption("p"));
        assertEquals("propertyValue", cl.getOptionValue("p"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesCommandLineTakesPrecedence() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("p", true, "param");

        Properties props = new Properties();
        props.setProperty("p", "fromProperty");

        CommandLine cl = parser.parse(options, new String[]{"-p", "fromCommandLine"}, props, false);
        assertTrue(cl.hasOption("p"));
        assertEquals("fromCommandLine", cl.getOptionValue("p"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesBooleanFlagTrue() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("t", false, "flag toggle");

        Properties props = new Properties();
        props.setProperty("t", "true");

        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertTrue(cl.hasOption("t"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesBooleanFlagYesAndOne() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("y", false, "flag y");
        options.addOption("o", false, "flag o");

        Properties props = new Properties();
        props.setProperty("y", "yes");
        props.setProperty("o", "1");

        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertTrue(cl.hasOption("y"));
        assertTrue(cl.hasOption("o"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesBooleanFlagInvalidValueBreaksLoop() throws Exception {
        Parser parser = new TestParser();
        Options options = new Options();
        options.addOption("n", false, "flag no");

        Properties props = new Properties();
        props.setProperty("n", "false");

        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertFalse(cl.hasOption("n"));
    }

    @Test(timeout = 4000)
    public void testDirectProcessArgsCall() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("d", true, "direct process");
        options.addOption(opt);
        parser.setOptions(options);

        List tokens = Arrays.asList("value1", "-nextOpt");
        ListIterator iter = tokens.listIterator();

        options.addOption("nextOpt", false, "next");
        parser.processArgs(opt, iter);

        assertEquals("value1", opt.getValue());
        // Verify iter was backed up when -nextOpt was encountered
        assertEquals("-nextOpt", iter.next());
    }

    @Test(timeout = 4000)
    public void testDirectProcessOptionCall() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = OptionBuilder.hasArg().isRequired().create("m");
        options.addOption(opt);
        parser.setOptions(options);
        parser.cmd = new CommandLine();

        List tokens = Arrays.asList("argForM");
        ListIterator iter = tokens.listIterator();

        parser.processOption("-m", iter);

        assertTrue(parser.cmd.hasOption("m"));
        assertEquals("argForM", parser.cmd.getOptionValue("m"));
        assertTrue(parser.getRequiredOptions().isEmpty());
    }
}