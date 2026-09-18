/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: org.apache.commons.cli.PosixParser
 * Target Defect: Defects4J / BugCLI51 (CLI-51)
 * Failure Symptom: Single-hyphen long option (e.g., "-something") is mistakenly burst into
 *                  constituent characters ('-s', '-o', ...), triggering UnrecognizedOptionException
 *                  for subsequent characters (e.g., "Unrecognized option: -o").
 *
 * Branch & Path Coverage Matrix:
 * 1. init(): Resets internal state (tokens, currentOption, eatTheRest) across executions.
 * 2. flatten() Main Loop:
 *    - Token startsWith("--"):
 *        * token.indexOf('=') != -1  -> Splits key & value (e.g., "--foo=bar", "--empty=")
 *        * token.indexOf('=') == -1  -> Adds token as-is (e.g., "--foo", "--")
 *    - Token equals("-"):
 *        * Calls processSingleHyphen -> Directly adds "-"
 *    - Token startsWith("-"):
 *        * tokenLength == 2:
 *            - options.hasOption(token) == true  -> processOptionToken sets currentOption & adds
 *            - options.hasOption(token) == false & stopAtNonOption == true  -> eatTheRest = true
 *            - options.hasOption(token) == false & stopAtNonOption == false -> ignores token
 *        * options.hasOption(token) == true (tokenLength > 2) -> (BugCLI51 Fix) adds token directly
 *        * else (bursting required):
 *            - Character is known Option:
 *                * Option hasArg() && not last char -> bursts remaining string as arg, breaks loop
 *                * Option hasArg() && last char     -> sets currentOption, does not break
 *                * Option has no arg                -> adds "-ch", continues bursting
 *            - Character is unknown Option:
 *                * stopAtNonOption == true  -> calls process(remaining), sets eatTheRest, stops
 *                * stopAtNonOption == false -> adds "-ch" to tokens, continues bursting
 *    - Non-option token:
 *        * stopAtNonOption == true:
 *            - currentOption != null && currentOption.hasArg() -> consumes arg, resets currentOption
 *            - currentOption == null or !hasArg()             -> sets eatTheRest, adds "--", value
 *        * stopAtNonOption == false:
 *            - Adds token directly to tokens list
 *    - gobble():
 *        * eatTheRest == true  -> consumes all remaining iterator entries into tokens
 *        * eatTheRest == false -> no-op
 * =========================================================================================
 */
package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class PosixParserGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (BugCLI51 / CLI-51)
    // =========================================================================

    /**
     * Targets BugCLI51: When a long option is passed with a single hyphen (e.g., "-something"),
     * PosixParser must recognize it as a whole option if present in Options instead of
     * bursting it into constituent characters ('-s', '-o', ...), which causes an
     * UnrecognizedOptionException: Unrecognized option: -o.
     */
    @Test(timeout = 4000)
    public void testCLI51SingleHyphenLongOptionParse() throws Exception {
        Options options = new Options();
        options.addOption("s", "something", false, "Defect CLI-51 Option");

        PosixParser parser = new PosixParser();
        CommandLine line = parser.parse(options, new String[] { "-something" });

        assertNotNull("CommandLine should not be null", line);
        assertTrue("Option 'something' should be recognized", line.hasOption("something"));
        assertTrue("Option 's' should be recognized", line.hasOption("s"));
    }

    /**
     * Verifies that flatten directly preserves a single-hyphen long option when registered in Options.
     */
    @Test(timeout = 4000)
    public void testCLI51SingleHyphenLongOptionFlatten() throws Exception {
        Options options = new Options();
        options.addOption("s", "something", false, "Defect CLI-51 Option");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-something" }, false);

        assertNotNull(flattened);
        assertEquals("Flattened array must contain exactly 1 token", 1, flattened.length);
        assertEquals("-something", flattened[0]);
    }

    /**
     * Verifies that single-hyphen long option is preserved even when stopAtNonOption is true.
     */
    @Test(timeout = 4000)
    public void testCLI51SingleHyphenLongOptionFlattenStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("s", "something", false, "Defect CLI-51 Option");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-something", "extraArg" }, true);

        assertNotNull(flattened);
        assertEquals(3, flattened.length);
        assertEquals("-something", flattened[0]);
        assertEquals("--", flattened[1]);
        assertEquals("extraArg", flattened[2]);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleHyphenTokenWithoutEquals() throws Exception {
        Options options = new Options();
        options.addOption("f", "foo", false, "foo option");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "--foo", "bar" }, false);

        assertArrayEquals(new String[] { "--foo", "bar" }, flattened);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenTokenWithEquals() throws Exception {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option with arg");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "--foo=bar" }, false);

        assertArrayEquals(new String[] { "--foo", "bar" }, flattened);
    }

    @Test(timeout = 4000)
    public void testSingleHyphenOnlyToken() throws Exception {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-", "value" }, false);

        assertArrayEquals(new String[] { "-", "value" }, flattened);
    }

    @Test(timeout = 4000)
    public void testTwoCharOptionTokenRecognized() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-a" }, false);

        assertArrayEquals(new String[] { "-a" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstingMultipleBooleanOptions() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");
        options.addOption("c", false, "option c");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-abc" }, false);

        assertArrayEquals(new String[] { "-a", "-b", "-c" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstingOptionWithAppendedArgumentValue() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("f", true, "file option with value");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "-afmyFile.txt" }, false);

        assertArrayEquals(new String[] { "-a", "-f", "myFile.txt" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstingOptionArgAtLastCharFollowedBySeparateToken() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b with arg");

        PosixParser parser = new PosixParser();
        // Here -ab has 'b' at length == i + 1, so it shouldn't burst value from token,
        // and next non-option token "argValue" should be consumed as b's argument when stopAtNonOption=true.
        String[] flattened = parser.flatten(options, new String[] { "-ab", "argValue" }, true);

        assertArrayEquals(new String[] { "-a", "-b", "argValue" }, flattened);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWithoutStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "nonOption1", "-a", "nonOption2" }, false);

        assertArrayEquals(new String[] { "nonOption1", "-a", "nonOption2" }, flattened);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArgumentsArray() throws Exception {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[0], false);

        assertNotNull(flattened);
        assertEquals(0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenWithEmptyValueAfterEquals() throws Exception {
        Options options = new Options();
        options.addOption("f", "foo", true, "foo option");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "--foo=" }, false);

        assertArrayEquals(new String[] { "--foo", "" }, flattened);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenWithMultipleEquals() throws Exception {
        Options options = new Options();
        options.addOption("p", "property", true, "property option");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "--property=key=value" }, false);

        assertArrayEquals(new String[] { "--property", "key=value" }, flattened);
    }

    @Test(timeout = 4000)
    public void testSpecialDoubleHyphenTokenOnly() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        String[] flattened = parser.flatten(options, new String[] { "--", "-a" }, false);

        assertArrayEquals(new String[] { "--", "-a" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstTokenDirectInvocation() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // Initialize parser options through flatten
        parser.flatten(options, new String[] { "-a" }, false);

        // Directly call protected burstToken
        parser.burstToken("-a", false);
        // Ensure no exception is thrown and method completes gracefully
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths (stopAtNonOption & Bursting)
    // =========================================================================

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedTwoCharOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // -z is unrecognized; stopAtNonOption=true triggers eatTheRest = true,
        // dropping -z and gobbling remaining tokens directly.
        String[] flattened = parser.flatten(options, new String[] { "-z", "remain1", "remain2" }, true);

        assertArrayEquals(new String[] { "remain1", "remain2" }, flattened);
    }

    @Test(timeout = 4000)
    public void testIgnoredUnrecognizedTwoCharOptionWhenNoStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // -z is unrecognized and stopAtNonOption=false; -z is ignored, remaining processed
        String[] flattened = parser.flatten(options, new String[] { "-z", "-a" }, false);

        assertArrayEquals(new String[] { "-a" }, flattened);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWhenCurrentOptionHasNoArg() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a (no arg)");

        PosixParser parser = new PosixParser();
        // -a has no arg, next token is "nonOpt". In process("nonOpt"), currentOption.hasArg() is false,
        // leading to tokens.add("--") and tokens.add("nonOpt") with eatTheRest = true.
        String[] flattened = parser.flatten(options, new String[] { "-a", "nonOpt", "remain" }, true);

        assertArrayEquals(new String[] { "-a", "--", "nonOpt", "remain" }, flattened);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWhenNoCurrentOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // Starts with non-option token when stopAtNonOption=true
        String[] flattened = parser.flatten(options, new String[] { "nonOpt", "-a" }, true);

        assertArrayEquals(new String[] { "--", "nonOpt", "-a" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstingUnrecognizedCharsNoStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // 'u' and 'v' are not options; stopAtNonOption=false adds "-u" and "-v"
        String[] flattened = parser.flatten(options, new String[] { "-auv" }, false);

        assertArrayEquals(new String[] { "-a", "-u", "-v" }, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstingUnrecognizedCharsWithStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // In -axy: 'a' is recognized, 'x' is unrecognized with stopAtNonOption=true,
        // triggering process("xy") which appends "--" and "xy" and gobbles remainder.
        String[] flattened = parser.flatten(options, new String[] { "-axy", "trailing" }, true);

        assertArrayEquals(new String[] { "-a", "--", "xy", "trailing" }, flattened);
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testParseThrowsUnrecognizedOptionException() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "option a");

        PosixParser parser = new PosixParser();
        // With stopAtNonOption=false, parse() will attempt to process unrecognized tokens
        parser.parse(options, new String[] { "-unknown" });
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testParseThrowsMissingArgumentException() throws Exception {
        Options options = new Options();
        options.addOption("req", true, "option requiring argument");

        PosixParser parser = new PosixParser();
        parser.parse(options, new String[] { "-req" });
    }

    // =========================================================================
    // Partition E: Object Lifecycle & State Reusability
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserReusabilityAndStateReset() throws Exception {
        Options options = new Options();
        options.addOption("a", true, "option a with arg");
        options.addOption("b", false, "option b");

        PosixParser parser = new PosixParser();

        // Run 1: with stopAtNonOption=true and argument consumption
        String[] run1 = parser.flatten(options, new String[] { "-a", "valA", "extra" }, true);
        assertArrayEquals(new String[] { "-a", "valA", "--", "extra" }, run1);

        // Run 2: same parser instance, different arguments
        String[] run2 = parser.flatten(options, new String[] { "-b" }, false);
        assertArrayEquals(new String[] { "-b" }, run2);

        // Run 3: verify empty run
        String[] run3 = parser.flatten(options, new String[0], false);
        assertEquals(0, run3.length);
    }

    @Test(timeout = 4000)
    public void testFullCommandLineParseIntegration() throws Exception {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption("v", "version", false, "display version");
        options.addOption("f", "file", true, "target file");

        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[] { "-hv", "--file=data.txt", "arg1", "arg2" });

        assertNotNull(cl);
        assertTrue(cl.hasOption("h"));
        assertTrue(cl.hasOption("help"));
        assertTrue(cl.hasOption("v"));
        assertTrue(cl.hasOption("version"));
        assertTrue(cl.hasOption("f"));
        assertEquals("data.txt", cl.getOptionValue("f"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("arg1", cl.getArgs()[0]);
        assertEquals("arg2", cl.getArgs()[1]);
    }
}