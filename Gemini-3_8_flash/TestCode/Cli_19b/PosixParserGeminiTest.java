package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Class under Test: org.apache.commons.cli.PosixParser (extends Parser)
 *
 * Decision / Condition Coverage Targets:
 * 1. flatten():
 *    - token.startsWith("--"):
 *        - token.indexOf('=') != -1: splits into key and value (e.g. "--foo=bar").
 *        - token.indexOf('=') == -1: keeps token as-is (e.g. "--foo", "--").
 *    - "-".equals(token): single hyphen preserved as standalone token.
 *    - token.startsWith("-"):
 *        - token.length() == 2:
 *            - options.hasOption(token) == true: currentOption set, token added.
 *            - options.hasOption(token) == false && stopAtNonOption == true: eatTheRest=true, token added.
 *            - options.hasOption(token) == false && stopAtNonOption == false: token ignored (omitted!).
 *        - token.length() > 2:
 *            - options.hasOption(token) == true: full token preserved without bursting.
 *            - options.hasOption(token) == false: delegates to burstToken().
 *    - stopAtNonOption == true:
 *        - calls process(token):
 *            - currentOption != null && currentOption.hasArg(): adds token, currentOption reset to null.
 *            - currentOption != null && currentOption.hasArgs(): adds token (dead branch in standard logic but reached if hasArg() is false).
 *            - currentOption == null: eatTheRest=true, adds "--", adds token.
 *    - stopAtNonOption == false: adds token directly to tokens list.
 *    - gobble(iter):
 *        - eatTheRest == true: copies all remaining iterator tokens to tokens list.
 *        - eatTheRest == false: no-op.
 * 2. burstToken():
 *    - loops character by character starting at index 1:
 *        - options.hasOption(ch) == true:
 *            - adds "-" + ch, sets currentOption = options.getOption(ch).
 *            - currentOption.hasArg() && token.length() != i + 1: consumes remaining substring as arg, breaks.
 *        - options.hasOption(ch) == false && stopAtNonOption == true:
 *            - calls process(token.substring(i)), breaks.
 *        - options.hasOption(ch) == false && stopAtNonOption == false:
 *            - adds token directly, breaks.
 *
 * Ground Truth Defect Analysis (Defects4J - PosixParserTest::testUnrecognizedOption2):
 * - When stopAtNonOption is false and an unrecognized single-hyphen 2-char option like "-z"
 *   is passed to PosixParser:
 *     processOptionToken("-z", false):
 *       if (options.hasOption(token)) -> false
 *       else if (stopAtNonOption) -> false
 *   The token is SILENTLY DROPPED!
 *   Therefore, when parser.parse(options, new String[]{"-z"}, false) runs,
 *   PosixParser flattens it to an empty list, and Parser never checks "-z" against options,
 *   failing to throw UnrecognizedOptionException.
 *   Expected behavior: Parser MUST throw UnrecognizedOptionException on unknown options.
 */
public class PosixParserGeminiTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenStandardOptionsAndLongOptions() throws Exception {
        options.addOption("a", false, "option a");
        options.addOption("b", true, "option b");
        options.addOption("c", "c-long", true, "option c");

        String[] args = new String[]{"-a", "-b", "valueB", "--c-long=valueC"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a", "-b", "valueB", "--c-long", "valueC"}, flattened);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertEquals("valueB", cl.getOptionValue("b"));
        assertEquals("valueC", cl.getOptionValue("c"));
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithoutEquals() throws Exception {
        options.addOption("f", "file", true, "file path");

        String[] args = new String[]{"--file", "out.txt"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"--file", "out.txt"}, flattened);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphenSpecialToken() throws Exception {
        options.addOption("i", false, "read stdin");

        String[] args = new String[]{"-i", "-"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-i", "-"}, flattened);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("i"));
        assertEquals("-", cl.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleHyphenDelimiter() throws Exception {
        options.addOption("v", false, "verbose");

        String[] args = new String[]{"-v", "--", "-notAnOption", "file.txt"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-v", "--", "-notAnOption", "file.txt"}, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstTokenMultipleFlags() throws Exception {
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");

        String[] args = new String[]{"-abc"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a", "-b", "-c"}, flattened);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test(timeout = 4000)
    public void testBurstTokenWithTrailingArgument() throws Exception {
        options.addOption("a", false, "flag a");
        options.addOption("f", true, "file flag with argument");

        String[] args = new String[]{"-afoutput.log"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-a", "-f", "output.log"}, flattened);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("f"));
        assertEquals("output.log", cl.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testOptionLongerThanTwoCharsRecognizedDirectly() throws Exception {
        // Options can have short-name style but multiple characters like -foo
        options.addOption("foo", false, "foo option");

        String[] args = new String[]{"-foo"};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"-foo"}, flattened);

        CommandLine cl = parser.parse(options, args);
        assertTrue(cl.hasOption("foo"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFlattenEmptyArgs() {
        String[] flattened = parser.flatten(options, new String[0], false);
        assertNotNull(flattened);
        assertEquals(0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testFlattenEmptyStringsInArgs() {
        String[] args = new String[]{"", ""};
        String[] flattened = parser.flatten(options, args, false);

        assertArrayEquals(new String[]{"", ""}, flattened);
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialCallsReinitializesState() throws Exception {
        options.addOption("x", true, "option x");

        String[] args1 = new String[]{"-x", "val1"};
        String[] flat1 = parser.flatten(options, args1, false);
        assertArrayEquals(new String[]{"-x", "val1"}, flat1);

        String[] args2 = new String[]{"-x", "val2"};
        String[] flat2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[]{"-x", "val2"}, flat2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: PosixParserTest::testUnrecognizedOption2
     * When stopAtNonOption is false and an unrecognized option like "-z" is passed,
     * PosixParser.processOptionToken() simply drops it if options.hasOption("-z") is false.
     * The parser subsequently completes without throwing UnrecognizedOptionException.
     * The correct behavior according to POSIX and Commons CLI Parser contracts is to throw
     * UnrecognizedOptionException when encountering an unrecognized option.
     */
    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedOption2Defect() throws Exception {
        options.addOption("a", false, "valid option a");

        String[] args = new String[]{"-z"};
        // Must throw UnrecognizedOptionException
        parser.parse(options, args, false);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionFlatteningPreserved() {
        options.addOption("a", false, "valid option");

        // When stopAtNonOption is false, -z must not be silently discarded during flatten
        String[] args = new String[]{"-z"};
        String[] flattened = parser.flatten(options, args, false);

        boolean containsZ = false;
        for (String s : flattened) {
            if ("-z".equals(s)) {
                containsZ = true;
                break;
            }
        }
        assertTrue("Unrecognized option '-z' must be preserved in flattened tokens so Parser can report it", containsZ);
    }

    // =========================================================================
    // Partition D: StopAtNonOption Branches & Gobble Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedTwoCharOption() {
        options.addOption("a", false, "option a");

        // -u is unrecognized; with stopAtNonOption=true, it and rest should be treated as non-options
        String[] args = new String[]{"-a", "-u", "extra1", "extra2"};
        String[] flattened = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-a", "-u", "extra1", "extra2"}, flattened);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithBurstTokenUnrecognizedPrefix() {
        options.addOption("a", false, "option a");
        // token is "-xyz", 'x' is unrecognized; with stopAtNonOption=true:
        // burstToken calls process("xyz") -> triggers eatTheRest=true, adds "--", "xyz"
        // then gobble adds "tail"
        String[] args = new String[]{"-a", "-xyz", "tail"};
        String[] flattened = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-a", "--", "xyz", "tail"}, flattened);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithBurstTokenPartialOptionThenUnrecognized() {
        options.addOption("a", false, "option a");
        // 'a' recognized, 'z' not recognized
        // burstToken loops: adds "-a", then sees 'z', calls process("z") -> adds "--", "z", then gobbles rest
        String[] args = new String[]{"-az", "rest"};
        String[] flattened = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-a", "--", "z", "rest"}, flattened);
    }

    @Test(timeout = 4000)
    public void testBurstTokenUnrecognizedWithoutStopAtNonOption() {
        options.addOption("a", false, "option a");
        // 'z' is unrecognized, stopAtNonOption=false: burstToken adds original token "-z"
        String[] args = new String[]{"-z"};
        String[] flattened = parser.flatten(options, args, false);

        // When token length > 2 and unrecognized with stopAtNonOption=false:
        String[] args3 = new String[]{"-xyz"};
        String[] flattened3 = parser.flatten(options, args3, false);
        assertArrayEquals(new String[]{"-xyz"}, flattened3);
    }

    @Test(timeout = 4000)
    public void testProcessNonOptionArgumentWhenExpectingArg() {
        options.addOption("b", true, "option expecting arg");

        // stopAtNonOption=true, but currentOption is 'b' which needs an arg
        // process("value") should fulfill 'b's arg and reset currentOption
        String[] args = new String[]{"-b", "value", "subcommand", "param"};
        String[] flattened = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"-b", "value", "--", "subcommand", "param"}, flattened);
    }

    @Test(timeout = 4000)
    public void testProcessNonOptionDirectlyWithNoPriorOption() {
        options.addOption("a", false, "option a");

        String[] args = new String[]{"cmd", "-a", "arg1"};
        String[] flattened = parser.flatten(options, args, true);

        assertArrayEquals(new String[]{"--", "cmd", "-a", "arg1"}, flattened);
    }

    // =========================================================================
    // Partition E: Parser Lifecycle and Full Parse Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullParseLifecycleWithStopAtNonOption() throws Exception {
        options.addOption("h", "help", false, "display help");
        options.addOption("o", "output", true, "output file");

        String[] args = new String[]{"-h", "run", "-o", "log.txt"};
        CommandLine cl = parser.parse(options, args, true);

        assertTrue(cl.hasOption("h"));
        assertFalse(cl.hasOption("o"));

        List remaining = cl.getArgList();
        assertTrue(remaining.contains("run"));
        assertTrue(remaining.contains("-o"));
        assertTrue(remaining.contains("log.txt"));
    }
}