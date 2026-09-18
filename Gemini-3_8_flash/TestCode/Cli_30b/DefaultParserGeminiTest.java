package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Method / Branch Area              | Targeted Condition                           | Expected Outcome
 * ----------------------------------+----------------------------------------------+-------------------
 * handleProperties / Missing Opt    | Property key not in Options (opt == null)    | NPE in buggy version,
 *                                   |                                              | ignored in fixed
 * handleProperties / OptionGroup    | Option in group selected via args, another   | AlreadySelectedEx
 *                                   | option from group specified in properties   | in buggy version
 * parse() overloads                 | 4 variants (Options, args, props, stopAtNon) | CommandLine state
 * parse() arguments boundary        | arguments == null, arguments == empty        | Empty CommandLine
 * parse() properties boundary       | properties == null, properties == empty      | Empty CommandLine
 * checkRequiredOptions              | Single required option missing               | MissingOptionEx
 * checkRequiredOptions (Group)      | Required OptionGroup missing                 | MissingOptionEx
 * checkRequiredOptions (Satisfied)  | Required option present                      | Normal execution
 * checkRequiredArgs                 | Option requires argument but none supplied   | MissingArgumentEx
 * checkRequiredArgs (Next is Opt)   | -a requires arg, next token is -b            | MissingArgumentEx
 * handleToken / skipParsing         | Token after '--' separator                   | Treated as argument
 * handleToken / negative numbers    | Arg is negative number (-42, -3.14)          | Value for option
 * handleToken / quotes stripping    | Token wrapped in single or double quotes     | Quotes stripped
 * handleToken / single dash '-'     | "-" token                                    | Treated as argument
 * handleLongOption / with equal     | --opt=value vs --flag=value (no arg)         | Assigned vs UnrecognizedEx
 * handleLongOption / ambiguous      | Partial matching prefix matches multiple     | AmbiguousOptionEx
 * handleShortAndLongOption / -S=V   | -s=value vs -s=value where s takes no arg    | Assigned vs UnrecognizedEx
 * handleShortAndLongOption / JVM -D | -Dkey=value or -Dkey (getArgs() >= 2)        | Split & assigned
 * getLongPrefix / -Xmx512m          | Long option embedded in short token          | Prefix opt + arg
 * handleConcatenatedOptions         | -abc, -abVal, -abUnknown (stopAtNonOption)   | Split / burst args
 * -------------------------------------------------------------------------------------------------
 */
public class DefaultParserGeminiTest {

    private final DefaultParser parser = new DefaultParser();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleShortOptions() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option A");
        options.addOption("b", true, "Option B");

        String[] args = new String[]{"-a", "-b", "valueB"};
        CommandLine cmd = parser.parse(options, args);

        assertTrue("Option 'a' should be present", cmd.hasOption("a"));
        assertTrue("Option 'b' should be present", cmd.hasOption("b"));
        assertEquals("valueB", cmd.getOptionValue("b"));
    }

    @Test(timeout = 4000)
    public void testSimpleLongOptions() throws Exception {
        Options options = new Options();
        options.addOption(new Option("a", "alpha", false, "Option Alpha"));
        options.addOption(new Option("b", "beta", true, "Option Beta"));

        String[] args = new String[]{"--alpha", "--beta", "valueBeta"};
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("alpha"));
        assertTrue(cmd.hasOption("beta"));
        assertEquals("valueBeta", cmd.getOptionValue("beta"));
    }

    @Test(timeout = 4000)
    public void testLongOptionWithEqual() throws Exception {
        Options options = new Options();
        options.addOption(new Option("o", "output", true, "Output file"));

        CommandLine cmd = parser.parse(options, new String[]{"--output=test.txt"});
        assertTrue(cmd.hasOption("output"));
        assertEquals("test.txt", cmd.getOptionValue("output"));
    }

    @Test(timeout = 4000)
    public void testShortOptionWithEqual() throws Exception {
        Options options = new Options();
        options.addOption(new Option("s", true, "Short with equal"));

        CommandLine cmd = parser.parse(options, new String[]{"-s=hello"});
        assertTrue(cmd.hasOption("s"));
        assertEquals("hello", cmd.getOptionValue("s"));
    }

    @Test(timeout = 4000)
    public void testPartialLongOptionMatching() throws Exception {
        Options options = new Options();
        options.addOption(new Option("f", "foobar", false, "Foobar option"));

        CommandLine cmd = parser.parse(options, new String[]{"--foo"});
        assertTrue("Partial prefix should match --foobar", cmd.hasOption("foobar"));
    }

    @Test(timeout = 4000)
    public void testDoubleDashStopsOptionParsing() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "Option A");
        options.addOption("b", false, "Option B");

        String[] args = new String[]{"-a", "--", "-b", "extraArg"};
        CommandLine cmd = parser.parse(options, args);

        assertTrue(cmd.hasOption("a"));
        assertFalse("Option 'b' should not be parsed after '--'", cmd.hasOption("b"));
        List<String> remaining = cmd.getArgList();
        assertEquals(2, remaining.size());
        assertEquals("-b", remaining.get(0));
        assertEquals("extraArg", remaining.get(1));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOption() throws Exception {
        Options options = new Options();