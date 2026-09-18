package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.cli.Parser
 * Known Defect: CLI-137 / ValueTest::testPropertyOptionFlags
 * In processProperties(Properties):
 *   When an option without arguments has a property value other than "yes", "true", or "1" (e.g., "false",
 *   "no", "0"), the defective code executes `break;` instead of `continue;`. This prematurely terminates
 *   the entire properties processing loop, causing all subsequent valid property options to be ignored.
 *
 * Core Decision Branches & Coverage Targets:
 * 1. parse(...) entry points:
 *    - parse(Options, String[]) -> defaults properties=null, stopAtNonOption=false
 *    - parse(Options, String[], Properties) -> defaults stopAtNonOption=false
 *    - parse(Options, String[], boolean) -> defaults properties=null
 *    - parse(Options, String[], Properties, boolean) -> full configuration
 * 2. Pre-processing cleanup:
 *    - options.helpOptions() value clearing (CLI-71)
 *    - options.getOptionGroups() selection reset
 * 3. Token stream handling & eatTheRest:
 *    - arguments == null -> initialized to empty array
 *    - token "--" triggers eatTheRest
 *    - token "-" with stopAtNonOption=true vs stopAtNonOption=false
 *    - token starting with "-" (option-like):
 *        * stopAtNonOption=true and !hasOption -> triggers eatTheRest & added as arg
 *        * valid option -> processOption(t, iterator)
 *        * unrecognized option -> UnrecognizedOptionException
 *    - token not starting with "-" (argument):
 *        * added to cmd args
 *        * stopAtNonOption=true triggers eatTheRest
 *    - eatTheRest loop: adds all remaining tokens except subsequent "--"
 * 4. processProperties:
 *    - properties == null early exit
 *    - cmd already has option -> skip
 *    - option takes argument (hasArg):
 *        * values null or empty -> addValueForProcessing, catch RuntimeException
 *        * values already present -> keep existing
 *    - option is flag (!hasArg):
 *        * "yes", "true", "1" (case insensitive) -> cmd.addOption(opt)
 *        * invalid/false flag value -> skip current option (MUST NOT break subsequent property options)
 * 5. checkRequiredOptions:
 *    - requiredOptions empty -> OK
 *    - requiredOptions not empty -> throws MissingOptionException
 *    - required option satisfied via CLI option or OptionGroup
 * 6. processArgs:
 *    - next token is an existing option starting with "-" -> breaks and rewinds iterator
 *    - next token is value -> addValueForProcessing
 *    - RuntimeException on adding value (e.g. max args exceeded) -> breaks and rewinds iterator
 *    - Missing required argument (no values && !hasOptionalArg) -> throws MissingArgumentException
 *    - Optional argument omitted -> does not throw
 * 7. processOption:
 *    - Option cloning and group selection handling (required vs non-required group)
 * -----------------------------------------------------------------------------------------------------
 */
public class ParserGeminiTest {

    /**
     * Concrete TestParser implementation providing direct passthrough of arguments for flatten.
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
        options.addOption("a", "alpha", false, "flag option");
        options.addOption("b", "beta", true, "string option");

        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "valB", "extraArg"});
        assertNotNull(cl);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("valB", cl.getOptionValue("b"));
        List<?> args = cl.getArgList();
        assertEquals(1, args.size());
        assertEquals("extraArg", args.get(0));
    }

    @Test(timeout = 4000)
    public void testSetAndGetOptionsAndRequiredOptions() {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option req = new Option("r", "required", false, "req desc");
        req.setRequired(true);
        options.addOption(req);

        parser.setOptions(options);
        assertSame(options, parser.getOptions());
        List<?> reqList = parser.getRequiredOptions();
        assertNotNull(reqList);
        assertEquals(1, reqList.size());
        assertEquals("r", reqList.get(0));
    }

    @Test(timeout = 4000)
    public void testOptionGroupSelectionAndSatisfaction() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("f", "file", false, "file opt");
        Option opt2 = new Option("d", "directory", false, "dir opt");
        group.addOption(opt1);
        group.addOption(opt2);
        group.setRequired(true);
        options.addOptionGroup(group);

        CommandLine cl = parser.parse(options, new String[]{"-f"});
        assertTrue(cl.hasOption("f"));
        assertFalse(cl.hasOption("d"));
        assertEquals("f", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testDoubleDashDelimiterStopsOptionProcessing() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag");
        options.addOption("b", false, "flag");

        CommandLine cl = parser.parse(options, new String[]{"-a", "--", "-b", "--", "file.txt"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        List<?> args = cl.getArgList();
        assertEquals(2, args.size());
        assertEquals("-b", args.get(0));
        assertEquals("file.txt", args.get(1));
    }

    @Test(timeout = 4000)
    public void testSingleDashWithStopAtNonOptionTrueAndFalse() throws Exception {
        TestParser parserFalse = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag");

        CommandLine clFalse = parserFalse.parse(options, new String[]{"-", "-a"}, false);
        assertEquals(1, clFalse.getArgList().size());
        assertEquals("-", clFalse.getArgList().get(0));
        assertTrue(clFalse.hasOption("a"));

        TestParser parserTrue = new TestParser();
        CommandLine clTrue = parserTrue.parse(options, new String[]{"-", "-a"}, true);
        assertEquals(2, clTrue.getArgList().size());
        assertEquals("-", clTrue.getArgList().get(0));
        assertEquals("-a", clTrue.getArgList().get(1));
        assertFalse(clTrue.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedOptionToken() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag");

        CommandLine cl = parser.parse(options, new String[]{"-unknown", "-a"}, true);
        assertFalse(cl.hasOption("a"));
        List<?> args = cl.getArgList();
        assertEquals(2, args.size());
        assertEquals("-unknown", args.get(0));
        assertEquals("-a", args.get(1));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithRegularArgument() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag");

        CommandLine cl = parser.parse(options, new String[]{"arg1", "-a"}, true);
        assertFalse(cl.hasOption("a"));
        assertEquals(2, cl.getArgList().size());
        assertEquals("arg1", cl.getArgList().get(0));
        assertEquals("-a", cl.getArgList().get(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullArgumentsArrayHandledAsEmpty() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("a", false, "flag");

        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertFalse(cl.hasOption("a"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testNullPropertiesHandledGracefully() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        CommandLine cl = parser.parse(options, new String[0], null);
        assertNotNull(cl);
    }

    @Test(timeout = 4000)
    public void testQuotesStrippedFromOptionArgument() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        options.addOption("s", true, "string with quotes");

        CommandLine cl = parser.parse(options, new String[]{"-s", "\"quoted value\""});
        assertEquals("quoted value", cl.getOptionValue("s"));
    }

    @Test(timeout = 4000)
    public void testOptionalArgumentOmittedLeavesNullValue() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("o", true, "optional arg");
        opt.setOptionalArg(true);
        options.addOption(opt);
        options.addOption("b", false, "flag b");

        CommandLine cl = parser.parse(options, new String[]{"-o", "-b"});
        assertTrue(cl.hasOption("o"));
        assertTrue(cl.hasOption("b"));
        assertNull(cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testOptionReinitializationCleansPreviousRuns() throws Exception {
        TestParser parser = new TestParser();
        Options options = new Options();
        Option opt = new Option("v", true, "value");
        options.addOption(opt);

        CommandLine cl1 = parser.parse(options, new String[]{"-v", "first"});
        assertEquals("first", cl1.getOptionValue("v"));

        CommandLine cl2 = parser.parse(options, new String[0]);
        assertFalse(cl2.hasOption("v"));
        assertNull(cl2.getOptionValue("v"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-137 / testPropertyOptionFlags)
    // =========================================================================

    /**
     * Defects4J Target Defect:
     * When processing properties for flags, if a flag has a false/no/0 value,
     * the parser must NOT 'break' the loop, but 'continue' so subsequent properties
     * are properly parsed into the CommandLine.
     */
    @Test(timeout = 4000