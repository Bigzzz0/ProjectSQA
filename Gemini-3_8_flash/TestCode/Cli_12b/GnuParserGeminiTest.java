/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.GnuParser (Defects4J CLI)
 *
 * Branch Coverage Targets:
 * 1. ("--".equals(arg)) == true
 *    - Followed by remaining arguments: triggers eatTheRest = true and inner drain loop.
 *    - Terminal position: eatTheRest inner loop does zero iterations.
 * 2. ("-".equals(arg)) == true
 *    - Lone hyphen handled without triggering option processing.
 * 3. (arg.startsWith("-")) == true
 *    3a. options.hasOption(opt) == true: recognized full option (--foo, -f).
 *    3b. options.hasOption(opt) == false:
 *        3b-1. options.hasOption(arg.substring(0, 2)) == true:
 *              Splits prefix (e.g. -D) and remainder (e.g. key=val).
 *        3b-2. options.hasOption(arg.substring(0, 2)) == false:
 *              - stopAtNonOption == true: eatTheRest activated, drains remaining args.
 *              - stopAtNonOption == false: token added, parsing continues without eatTheRest.
 * 4. (!arg.startsWith("-")) == true: regular non-option argument.
 *
 * Known Defects4J Regression Targets:
 * - testLongWithEqual: --foo=bar fails because Util.stripLeadingHyphens returns "foo=bar",
 *   which is not in options, causing an UnrecognizedOptionException in defective GnuParser.
 * - testShortWithEqual: -s=bar fails because arg.substring(2) retains '=' giving '=bar'
 *   instead of 'bar'.
 * - testLongWithEqualSingleDash: -foo=bar fails when -f is matched as substring(0, 2),
 *   producing 'oo=bar' instead of 'bar'.
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced White-Box Test Suite for {@link GnuParser}.
 */
public class GnuParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenStandardOptionsAndNonOptions() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("a", "all", false, "all files"));
        options.addOption(new Option("f", "file", true, "output file"));

        String[] args = new String[] { "-a", "--file", "out.txt", "extra1", "extra2" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-a", "--file", "out.txt", "extra1", "extra2" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphenToken() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("v", false, "verbose"));

        String[] args = new String[] { "-v", "-", "stdin_data" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-v", "-", "stdin_data" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleHyphenDelimiterConsumesRemaining() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("a", false, "all"));
        options.addOption(new Option("b", false, "binary"));

        String[] args = new String[] { "-a", "--", "-b", "literal_arg" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-a", "--", "-b", "literal_arg" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleHyphenAtEnd() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("x", false, "flag"));

        String[] args = new String[] { "-x", "--" };
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-x", "--" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenPropertyStyleOptionSplitsPrefixAndValue() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("D", true, "define property"));

        String[] args = new String[] { "-Dkey=value" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-D", "key=value" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenUnrecognizedOptionWithStopAtNonOptionTrue() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("a", false, "recognized"));

        String[] args = new String[] { "-a", "-unknown", "trailing1", "-b" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-a", "-unknown", "trailing1", "-b" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenUnrecognizedOptionWithStopAtNonOptionFalse() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("a", false, "recognized"));
        options.addOption(new Option("b", false, "also recognized"));

        String[] args = new String[] { "-a", "-unknown", "-b" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertArrayEquals(new String[] { "-a", "-unknown", "-b" }, flattened);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() {
        GnuParser parser = new GnuParser();
        Options options = new Options();

        String[] flattened = parser.flatten(options, new String[0], false);
        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testFlattenOnlyDoubleHyphen() {
        GnuParser parser = new GnuParser();
        Options options = new Options();

        String[] flattened = parser.flatten(options, new String[] { "--" }, false);
        assertArrayEquals(new String[] { "--" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenOnlySingleHyphen() {
        GnuParser parser = new GnuParser();
        Options options = new Options();

        String[] flattened = parser.flatten(options, new String[] { "-" }, false);
        assertArrayEquals(new String[] { "-" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenConsecutiveNonOptionTokens() {
        GnuParser parser = new GnuParser();
        Options options = new Options();

        String[] args = new String[] { "cmd", "arg1", "arg2", "arg3" };
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "cmd", "arg1", "arg2", "arg3" }, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenMultiplePropertyStyleOptions() {
        GnuParser parser = new GnuParser();
        Options options = new Options();
        options.addOption(new Option("D", true, "JVM property"));

        String[] args = new String[] { "-Da=1", "-Db=2", "-Dc=three" };
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[] { "-D", "a=1", "-D", "b=2", "-D", "c=three" }, flattened);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: org.apache.commons.cli.GnuParserTest::testLongWithEqual
     * Defective GnuParser fails to split "--foo=bar" and throws UnrecognizedOptionException.
     */
    @Test(timeout = 4000)
    public void testLongWithEqual() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("f", "foo", true, "Option foo with argument"));

        GnuParser parser = new GnuParser();
        CommandLine cl = parser.parse(options, new String[] { "--foo=bar" });

        assertTrue("Option 'foo' must be present", cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    /**
     * Targets known defect: org.apache.commons.cli.GnuParserTest::testShortWithEqual
     * Defective GnuParser splits "-s=bar" into "-s" and "=bar", retaining '=' in value.
     */
    @Test(timeout = 4000)
    public void testShortWithEqual() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("s", true, "Option s with argument"));

        GnuParser parser = new GnuParser();
        CommandLine cl = parser.parse(options, new String[] { "-s=bar" });

        assertTrue("Option 's' must be present", cl.hasOption("s"));
        assertEquals("bar", cl.getOptionValue("s"));
    }

    /**
     * Targets known defect: org.apache.commons.cli.GnuParserTest::testLongWithEqualSingleDash
     * Defective GnuParser splits "-foo=bar" by matching first two chars as short opt "-f",
     * yielding "oo=bar" as the value instead of recognizing long option "foo" with value "bar".
     */
    @Test(timeout = 4000)
    public void testLongWithEqualSingleDash() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("f", "foo", true, "Option foo with argument"));

        GnuParser parser = new GnuParser();
        CommandLine cl = parser.parse(options, new String[] { "-foo=bar" });

        assertTrue("Option 'foo' must be present", cl.hasOption("foo"));
        assertEquals("bar", cl.getOptionValue("foo"));
    }

    // =========================================================================
    // Partition D: Defensive & Parser Lifecycle Integration Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullParseLifecycleWithStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("a", false, "flag a"));
        options.addOption(new Option("b", false, "flag b"));

        GnuParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, new String[] { "-a", "non-option-1", "-b", "non-option-2" }, true);

        assertTrue("Should have option 'a'", cmd.hasOption("a"));
        assertFalse("Should not have processed option 'b' when stopAtNonOption is true", cmd.hasOption("b"));
        assertEquals(3, cmd.getArgs().length);
        assertEquals("non-option-1", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
        assertEquals("non-option-2", cmd.getArgs()[2]);
    }

    @Test(timeout = 4000)
    public void testFullParseLifecycleWithoutStopAtNonOption() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("a", false, "flag a"));
        options.addOption(new Option("b", false, "flag b"));

        GnuParser parser = new GnuParser();
        CommandLine cmd = parser.parse(options, new String[] { "-a", "non-option-1", "-b", "non-option-2" }, false);

        assertTrue("Should have option 'a'", cmd.hasOption("a"));
        assertTrue("Should have option 'b'", cmd.hasOption("b"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("non-option-1", cmd.getArgs()[0]);
        assertEquals("non-option-2", cmd.getArgs()[1]);
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testParseMissingArgumentThrowsException() throws ParseException {
        Options options = new Options();
        options.addOption(new Option("f", true, "requires argument"));

        GnuParser parser = new GnuParser();
        parser.parse(options, new String[] { "-f" });
    }
}