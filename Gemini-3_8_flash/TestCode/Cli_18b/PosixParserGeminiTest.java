/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.PosixParser
 * Methods Under Test:
 *   - flatten(Options, String[], boolean)
 *   - burstToken(String, boolean)
 *   - processOptionToken(String, boolean)
 *   - process(String)
 *   - processSingleHyphen(String)
 *   - gobble(Iterator)
 *   - init()
 *
 * Key Decision Branches & Edge Conditions:
 * 1. Special token "--":
 *    - token starts with "--" containing '=': splits into key and value.
 *    - token starts with "--" without '=': adds token verbatim.
 * 2. Single hyphen "-":
 *    - processed via processSingleHyphen and appended directly.
 * 3. Short option token (length == 2):
 *    - Valid option: set currentOption and add token.
 *    - Invalid option with stopAtNonOption=true: triggers known defect (CLI-106 / testStop2),
 *      where unrecognized 2-char token drops the option and erroneously parses subsequent tokens.
 *    - Invalid option with stopAtNonOption=false: ignores token.
 * 4. Multi-character option token:
 *    - Matches options.hasOption(token): added directly without bursting.
 *    - Does not match options: requires burstToken.
 * 5. burstToken algorithm:
 *    - Iterates chars:
 *      * Recognized option with argument and attached suffix: splits option and suffix arg.
 *      * Recognized option without argument: adds "-<char>" and continues loop.
 *      * Recognized option with argument as terminal char: adds option, next token serves as arg.
 *      * Unrecognized option with stopAtNonOption=true: processes remaining suffix as non-option.
 *      * Unrecognized option with stopAtNonOption=false: appends remaining token verbatim.
 * 6. Non-option tokens:
 *    - stopAtNonOption=true:
 *      * currentOption != null && currentOption.hasArg(): consumes token as argument.
 *      * currentOption == null (or no arg expected): adds "--" marker, consumes token, sets eatTheRest.
 *    - stopAtNonOption=false: adds token directly.
 * 7. Instance reusability:
 *    - Verifies init() resets tokens, currentOption, and eatTheRest flags across consecutive invocations.
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

public class PosixParserGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Flattening
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenSingleHyphen() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] args = new String[]{"-"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleHyphenAlone() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] args = new String[]{"--"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithoutEquals() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("foo", "foo-option", false, "sample option");
        String[] args = new String[]{"--foo-option"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo-option"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEquals() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", true, "option with value");
        String[] args = new String[]{"--foo=bar"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEmptyEquals() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] args = new String[]{"--foo="};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--foo", ""}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenValidShortOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        String[] args = new String[]{"-a"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenMultiCharacterOptionRecognized() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("opt", false, "multi-character option");
        String[] args = new String[]{"-opt"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-opt"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstMultipleFlags() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");
        String[] args = new String[]{"-abc"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstOptionWithAttachedArgument() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", true, "flag f with argument");
        String[] args = new String[]{"-fFileName"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-f", "FileName"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstMultipleFlagsWithAttachedArgumentOnLast() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("v", false, "verbose flag");
        options.addOption("f", true, "file flag with argument");
        String[] args = new String[]{"-vfOutputFile.txt"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-v", "-f", "OutputFile.txt"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstFlagWithSeparateArgument() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("v", false, "verbose");
        options.addOption("f", true, "file");
        String[] args = new String[]{"-vf", "myFile.txt"};

        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-v", "-f", "myFile.txt"}, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Non-Option Processing
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] result = parser.flatten(options, new String[0], false);

        assertNotNull("Result should never be null", result);
        assertEquals("Result should be empty", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenNonOptionStopAtNonOptionFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        String[] args = new String[]{"foo", "-a", "bar"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenNonOptionStopAtNonOptionTrueWithoutPriorOption() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        String[] args = new String[]{"file1.txt", "file2.txt", "-a"};

        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "file1.txt", "file2.txt", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenNonOptionStopAtNonOptionTrueAfterOptionWithArg() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", true, "file");
        String[] args = new String[]{"-f", "data.csv", "extra1", "extra2"};

        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-f", "data.csv", "--", "extra1", "extra2"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstUnrecognizedCharacterStopFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");
        String[] args = new String[]{"-az"};

        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-az"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstUnrecognizedCharacterStopTrue() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");
        String[] args = new String[]{"-az", "extra"};

        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "z", "extra"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenUnrecognizedShortOptionStopFalse() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        String[] args = new String[]{"-z"};

        String[] result = parser.flatten(options, args, false);
        assertEquals("Unrecognized option should be omitted when stopAtNonOption is false", 0, result.length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug PosixParserTest::testStop2.
     * Ground truth failure: junit.framework.AssertionFailedError: Confirm -a is not set
     *
     * In the defective implementation:
     * When an unrecognized 2-character option ("-z") is encountered and stopAtNonOption is true,
     * PosixParser.processOptionToken sets eatTheRest = true but fails to add "-z" to tokens.
     * Subsequent recognized options like "-a" are then consumed directly into tokens without
     * a "--" delimiter, causing the CommandLine parser to mistakenly activate option "a".
     */
    @Test(timeout = 4000)
    public void testStop2() throws Exception {
        Options options = new Options();
        options.addOption("c", "copt", false, "turn on c");
        options.addOption("a", "enable-a", false, "turn on a");

        PosixParser parser = new PosixParser();
        String[] args = new String[]{"-c", "-z", "-a"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue("Confirm -c is set", cl.hasOption("c"));
        assertFalse("Confirm -a is not set", cl.hasOption("a"));
        assertEquals("Confirm 2 extra args", 2, cl.getArgList().size());
        assertTrue("Confirm -z is in extra args", cl.getArgList().contains("-z"));
        assertTrue("Confirm -a is in extra args", cl.getArgList().contains("-a"));
    }

    @Test(timeout = 4000)
    public void testStop2DirectFlattenVerification() {
        Options options = new Options();
        options.addOption("c", false, "turn on c");
        options.addOption("a", false, "turn on a");

        PosixParser parser = new PosixParser();
        String[] args = new String[]{"-c", "-z", "-a"};
        String[] flattened = parser.flatten(options, args, true);

        assertTrue("Flattened tokens must preserve the unrecognized token '-z'",
                Arrays.asList(flattened).contains("-z"));
    }

    // =========================================================================
    // Partition D: Defensive & Exception Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFlattenNullOptionsThrowsException() {
        PosixParser parser = new PosixParser();
        parser.flatten(null, new String[]{"-a"}, false);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFlattenNullArgumentsThrowsException() {
        PosixParser parser = new PosixParser();
        parser.flatten(new Options(), null, false);
    }

    // =========================================================================
    // Partition E: Lifecycle & State Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserReusabilityAndStateReset() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", true, "option a");
        options.addOption("b", false, "option b");

        // First run leaves parser state with eatTheRest = true
        String[] firstArgs = new String[]{"-a", "arg1", "nonOption", "moreArgs"};
        String[] firstResult = parser.flatten(options, firstArgs, true);
        assertArrayEquals(new String[]{"-a", "arg1", "--", "nonOption", "moreArgs"}, firstResult);

        // Second run must be fully isolated; init() must clear all previous state
        String[] secondArgs = new String[]{"-b"};
        String[] secondResult = parser.flatten(options, secondArgs, false);
        assertArrayEquals(new String[]{"-b"}, secondResult);
    }
}