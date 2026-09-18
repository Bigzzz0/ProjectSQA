package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Class: PosixParser extends Parser
 * Methods Targeted:
 *   - flatten(Options options, String[] arguments, boolean stopAtNonOption)
 *   - init()
 *   - gobble(Iterator iter)
 *   - process(String value)
 *   - processSingleHyphen(String hyphen)
 *   - processOptionToken(String token, boolean stopAtNonOption)
 *   - burstToken(String token, boolean stopAtNonOption)
 *
 * Decision / Condition Coverage Matrix:
 * 1. Token starts with "--":
 *    - Has '=': splits into option and value (token.substring(0, index), token.substring(index + 1)).
 *    - Does not have '=': adds whole token as-is.
 * 2. Token is single hyphen "-":
 *    - Calls processSingleHyphen -> adds "-" directly.
 * 3. Token starts with single "-":
 *    - Length == 2:
 *      * options.hasOption(token) == true: sets currentOption, adds token.
 *      * options.hasOption(token) == false && stopAtNonOption == true: eatTheRest = true (gobbles rest).
 *      * options.hasOption(token) == false && stopAtNonOption == false: ignores token.
 *    - Length > 2 && options.hasOption(token) == true: adds token directly (multi-char option id).
 *    - Length > 2 && options.hasOption(token) == false: calls burstToken(token, stopAtNonOption).
 * 4. burstToken:
 *    - Loop through characters i = 1 .. length-1:
 *      * options.hasOption(ch) == true: adds "-" + ch, sets currentOption.
 *        - currentOption.hasArg() && not at end: adds remainder as argument token, breaks.
 *        - currentOption.hasArg() && at end: does not break (next arg will be eaten by process()).
 *      * options.hasOption(ch) == false && stopAtNonOption == true:
 *        [KNOWN DEFECT ZONE: Defects4J / CLI-something: burstToken calls process(token.substring(i))
 *         WITHOUT breaking out of the bursting loop. Characters after non-option continue to be processed!]
 *      * options.hasOption(ch) == false && stopAtNonOption == false: adds full token, breaks.
 * 5. Non-option token (does not start with "-"):
 *    - stopAtNonOption == true:
 *      * currentOption != null && currentOption.hasArg(): adds value, clears currentOption.
 *      * currentOption == null || !currentOption.hasArg(): eatTheRest = true, adds "--", adds value.
 *    - stopAtNonOption == false: adds token directly.
 * 6. gobble(iter):
 *    - eatTheRest == true: adds all remaining items from iterator to tokens.
 *    - eatTheRest == false: no-op.
 * 7. State Reset (init()):
 *    - eatTheRest reset to false, tokens cleared, currentOption set to null across runs.
 * =====================================================================================================
 */
public class PosixParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testLongOptionWithEqualsSeparator() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("foo", true, "Foo option");

        String[] args = new String[] { "--foo=bar" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals("Should split into 2 tokens", 2, flattened.length);
        assertEquals("--foo", flattened[0]);
        assertEquals("bar", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testLongOptionWithoutEqualsSeparator() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("foo", true, "Foo option");

        String[] args = new String[] { "--foo" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals("Should preserve single token", 1, flattened.length);
        assertEquals("--foo", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testSingleHyphenArgument() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "-" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals("Single hyphen preserved", 1, flattened.length);
        assertEquals("-", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testRecognizedSingleHyphenSingleCharOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "-a" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(1, flattened.length);
        assertEquals("-a", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testRecognizedMultiCharOptionToken() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("help", false, "Help option");

        // tokenLength > 2 and options.hasOption("-help") returns true
        String[] args = new String[] { "-help" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(1, flattened.length);
        assertEquals("-help", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testBurstingSingleOptionWithAttachedArgument() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("b", true, "Option b with argument");

        String[] args = new String[] { "-bFILE" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals("Should burst option and attached value", 2, flattened.length);
        assertEquals("-b", flattened[0]);
        assertEquals("FILE", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testBurstingFlagsFollowedByOptionWithAttachedArgument() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");
        options.addOption("b", true, "Option b with arg");

        String[] args = new String[] { "-abFILE" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(3, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-b", flattened[1]);
        assertEquals("FILE", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testBurstingMultipleFlagsOnly() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");
        options.addOption("c", false, "Flag c");

        String[] args = new String[] { "-ac" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-c", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testBurstingFlagAndOptionWithArgAtEndFollowedBySeparateArg() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");
        options.addOption("b", true, "Option b with arg");

        // When -ab is processed, 'b' is at token.length()-1 so it does not break inside burstToken
        String[] args = new String[] { "-ab", "separateValue" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(3, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-b", flattened[1]);
        assertEquals("separateValue", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWhenStopAtNonOptionIsFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "file1.txt", "file2.txt" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("file1.txt", flattened[0]);
        assertEquals("file2.txt", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWhenStopAtNonOptionIsTrueWithoutCurrentOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "file1.txt", "file2.txt" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull("Flattened array must not be null", flattened);
        // Process rule 6 + process(): inserts "--" before non-option and gobbles rest
        assertEquals(3, flattened.length);
        assertEquals("--", flattened[0]);
        assertEquals("file1.txt", flattened[1]);
        assertEquals("file2.txt", flattened[2]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArgumentsArray() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[0];
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testSpecialDoubleHyphenTokenOnly() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "--" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(1, flattened.length);
        assertEquals("--", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenFollowedByOptions() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");

        String[] args = new String[] { "--", "-a", "val" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(3, flattened.length);
        assertEquals("--", flattened[0]);
        assertEquals("-a", flattened[1]);
        assertEquals("val", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testLongOptionWithEmptyValue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("foo", true, "Foo option");

        String[] args = new String[] { "--foo=" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("--foo", flattened[0]);
        assertEquals("", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testEmptyStringArgumentStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(1, flattened.length);
        assertEquals("", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testEmptyStringArgumentStopAtNonOptionTrue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("--", flattened[0]);
        assertEquals("", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedTwoCharOptionStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        // Option "-z" does not exist in options; when stopAtNonOption is false, it is ignored
        String[] args = new String[] { "-z" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedTwoCharOptionStopAtNonOptionTrue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        // When stopAtNonOption is true, unrecognized option triggers eatTheRest = true
        // The token itself is not added, but remaining tokens are gobbled
        String[] args = new String[] { "-z", "foo", "bar" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("foo", flattened[0]);
        assertEquals("bar", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testBurstTokenUnrecognizedOptionStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        // "-xyz" with no options configured and stopAtNonOption == false -> adds entire token
        String[] args = new String[] { "-xyz" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(1, flattened.length);
        assertEquals("-xyz", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testBurstTokenRecognizedThenUnrecognizedStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");

        // 'a' is recognized, 'x' is not -> adds "-a", then adds "-axyz" and breaks
        String[] args = new String[] { "-axyz" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals(2, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-axyz", flattened[1]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug:
     * org.apache.commons.cli.PosixParserTest::testStopBursting
     * junit.framework.AssertionFailedError: Confirm  1 extra arg: 2
     *
     * In defective PosixParser, burstToken() does not break after encountering
     * an unrecognized character when stopAtNonOption is true. It continues to burst
     * subsequent characters (e.g., 'c'), wrongly adding them instead of stopping.
     */
    @Test(timeout = 4000)
    public void testStopBursting() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "toggle -a");
        options.addOption("c", false, "toggle -c");

        String[] args = new String[] { "-azc" };

        CommandLine cl = parser.parse(options, args, true);
        assertTrue("Confirm -a is set", cl.hasOption("a"));
        assertFalse("Confirm -c is not set", cl.hasOption("c"));

        // Ground truth assertion: exactly 1 non-option argument ("zc") must exist
        assertEquals("Confirm  1 extra arg: " + cl.getArgs().length, 1, cl.getArgs().length);
        assertEquals("zc", cl.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testStopBurstingFlattenArrayIntegrity() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "toggle -a");
        options.addOption("c", false, "toggle -c");

        String[] args = new String[] { "-azc" };
        String[] flattened = parser.flatten(options, args, true);

        // Correct POSIX bursting stops at non-option 'z', producing "-a", "--", "zc"
        // On the defective version, "-c" is erroneously appended as a 4th token
        assertArrayEquals("Flattened tokens must not process '-c' after stopAtNonOption triggered",
                new String[] { "-a", "--", "zc" }, flattened);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFlattenNullArgumentsThrowsNullPointerException() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        parser.flatten(options, null, false);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFlattenNullOptionsThrowsNullPointerException() {
        PosixParser parser = new PosixParser();
        String[] args = new String[] { "-a" };
        parser.flatten(null, args, false);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserStateResetAcrossMultipleInvocations() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");

        // Invocation 1: triggers eatTheRest = true
        String[] args1 = new String[] { "-z", "extra1", "extra2" };
        String[] flattened1 = parser.flatten(options, args1, true);
        assertEquals(2, flattened1.length);
        assertEquals("extra1", flattened1[0]);
        assertEquals("extra2", flattened1[1]);

        // Invocation 2: should be completely clean; eatTheRest must be reset to false
        String[] args2 = new String[] { "-a" };
        String[] flattened2 = parser.flatten(options, args2, false);
        assertEquals("State must be reset after init()", 1, flattened2.length);
        assertEquals("-a", flattened2[0]);
    }

    @Test(timeout = 4000)
    public void testFullParseIntegration() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Flag a");
        options.addOption("b", true, "Option b with value");
        options.addOption("d", "debug", false, "Debug mode");

        String[] args = new String[] { "-a", "-b", "val", "--debug", "extraArg" };
        CommandLine cl = parser.parse(options, args);

        assertTrue("Flag -a should be set", cl.hasOption("a"));
        assertTrue("Option -b should be set", cl.hasOption("b"));
        assertEquals("val", cl.getOptionValue("b"));
        assertTrue("Long option --debug should be set", cl.hasOption("debug"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("extraArg", cl.getArgs()[0]);
    }
}