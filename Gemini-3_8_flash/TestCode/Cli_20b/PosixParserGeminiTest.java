package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.cli.PosixParser (extends Parser)
 * Target Environment: Java 8 / JUnit 4
 *
 * Decision / Condition Branch Coverage Map:
 * 1. flatten(Options, String[], boolean):
 *    - token.startsWith("--")
 *      * token.indexOf('=') != -1 (e.g. "--foo=bar" -> burst to "--foo", "bar")
 *      * token.indexOf('=') == -1 (e.g. "--foo" -> "--foo", "--" -> "--")
 *    - "-".equals(token) (single hyphen preserved directly as token)
 *    - token.startsWith("-")
 *      * token.length() == 2 -> processOptionToken(token, stopAtNonOption)
 *        - options.hasOption(token) == true  -> currentOption set, token added
 *        - options.hasOption(token) == false && stopAtNonOption == true  -> eatTheRest = true
 *        - options.hasOption(token) == false && stopAtNonOption == false -> ignore stop, token added
 *      * token.length() > 2 && options.hasOption(token) -> token added without bursting
 *      * token.length() > 2 && !options.hasOption(token) -> burstToken(token, stopAtNonOption)
 *    - Non-option token (!token.startsWith("-")):
 *      * stopAtNonOption == true  -> process(token)
 *      * stopAtNonOption == false -> token added directly
 *    - gobble(Iterator):
 *      * eatTheRest == true  -> consumes all remaining iterator entries
 *      * eatTheRest == false -> no-op
 *
 * 2. process(String):
 *    - currentOption != null && currentOption.hasArg() -> consumes arg, currentOption = null
 *    - currentOption == null || !currentOption.hasArg() -> eatTheRest = true, adds "--", adds token
 *
 * 3. burstToken(String, boolean):
 *    - Loop through characters 1..length-1:
 *      * options.hasOption(ch) == true:
 *        - currentOption.hasArg() && token.length() != i + 1 -> adds arg remainder, breaks
 *        - currentOption.hasArg() && token.length() == i + 1 -> leaves currentOption set, completes loop
 *        - !currentOption.hasArg() -> continues bursting next char
 *      * options.hasOption(ch) == false:
 *        - stopAtNonOption == true  -> process(remainder), breaks
 *        - stopAtNonOption == false -> adds original full token, breaks
 *
 * 4. Ground Truth Defect Analysis (Defects4J PosixParserTest::testStop3):
 *    - Defect: When stopAtNonOption is true and an unrecognized option is supplied, PosixParser
 *      fails to properly halt and delimiterize remaining arguments with "--", causing subsequent
 *      options/arguments to be improperly parsed rather than retained as raw extra arguments.
 * ====================================================================================================
 */
public class PosixParserGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEquals() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", true, "Foo option");

        String[] args = new String[] { "--foo=bar" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull("Flattened array must not be null", flattened);
        assertEquals("Should burst key and value into 2 tokens", 2, flattened.length);
        assertEquals("--foo", flattened[0]);
        assertEquals("bar", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithoutEquals() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "foo", false, "Foo option");

        String[] args = new String[] { "--foo" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(1, flattened.length);
        assertEquals("--foo", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphen() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "-" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(1, flattened.length);
        assertEquals("-", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphenWithStopAtNonOption() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] args = new String[] { "-" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        assertEquals(1, flattened.length);
        assertEquals("-", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleCharacterOptionRecognized() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "-a" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(1, flattened.length);
        assertEquals("-a", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenOptionLongerThanTwoRecognizedDirectly() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        // Register an option whose name has multiple characters
        options.addOption("all", false, "All option");

        String[] args = new String[] { "-all" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals("Option matching length > 2 should not burst", 1, flattened.length);
        assertEquals("-all", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testBurstMultipleFlagsWithoutArgs() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");
        options.addOption("b", false, "Option b");
        options.addOption("c", false, "Option c");

        String[] args = new String[] { "-abc" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals("Should burst into 3 separate flags", 3, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-b", flattened[1]);
        assertEquals("-c", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testBurstFlagsTerminatingWithArgumentValue() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");
        options.addOption("b", true, "Option b with argument");

        String[] args = new String[] { "-abFILE" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals("Should burst -a, -b, and trailing value", 3, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-b", flattened[1]);
        assertEquals("FILE", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testBurstOptionWhereArgIsNextSeparateToken() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");
        options.addOption("b", true, "Option b with argument");

        // -ab where b takes an argument provided as the subsequent token
        String[] args = new String[] { "-ab", "myArgument" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        assertEquals(3, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-b", flattened[1]);
        assertEquals("myArgument", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testNonOptionTokensWithoutStop() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "file1", "-a", "file2" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(3, flattened.length);
        assertEquals("file1", flattened[0]);
        assertEquals("-a", flattened[1]);
        assertEquals("file2", flattened[2]);
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();

        String[] flattened = parser.flatten(options, new String[0], false);
        assertNotNull(flattened);
        assertEquals("Empty arguments should produce empty flattened output", 0, flattened.length);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenDelimiter() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "--", "-a", "file" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(3, flattened.length);
        assertEquals("--", flattened[0]);
        assertEquals("-a", flattened[1]);
        assertEquals("file", flattened[2]);
    }

    @Test(timeout = 4000)
    public void testBurstUnknownOptionCharWithStopAtNonOptionFalse() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        // 'x' is not recognized. When stopAtNonOption is false, burstToken stops and keeps original token
        String[] args = new String[] { "-ax" };
        String[] flattened = parser.flatten(options, args, false);

        assertNotNull(flattened);
        assertEquals(2, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("-ax", flattened[1]);
    }

    @Test(timeout = 4000)
    public void testBurstUnknownOptionCharWithStopAtNonOptionTrue() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        // 'x' is not recognized. When stopAtNonOption is true, process is invoked on remaining substring
        String[] args = new String[] { "-ax", "remaining" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        assertEquals(4, flattened.length);
        assertEquals("-a", flattened[0]);
        assertEquals("--", flattened[1]);
        assertEquals("x", flattened[2]);
        assertEquals("remaining", flattened[3]);
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialFlattenInvocationsResetState() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args1 = new String[] { "nonOption1", "nonOption2" };
        String[] flattened1 = parser.flatten(options, args1, true);
        assertEquals(3, flattened1.length);
        assertEquals("--", flattened1[0]);
        assertEquals("nonOption1", flattened1[1]);
        assertEquals("nonOption2", flattened1[2]);

        // Second invocation should start from completely clean internal state
        String[] args2 = new String[] { "-a" };
        String[] flattened2 = parser.flatten(options, args2, false);
        assertEquals(1, flattened2.length);
        assertEquals("-a", flattened2[0]);
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth: testStop3)
    // ================================================================================================

    /**
     * Targets Defects4J ground truth defect in PosixParser:
     * - org.apache.commons.cli.PosixParserTest::testStop3
     * --> junit.framework.AssertionFailedError: Confirm  3 extra args: 7
     *
     * In the defective version, when stopAtNonOption is enabled and an unrecognized option is
     * encountered, PosixParser does not properly stop and insert the "--" delimiter, corrupting
     * the remaining argument processing and failing the extra argument count verification.
     */
    @Test(timeout = 4000)
    public void testStop3() throws Exception {
        Options options = new Options();
        options.addOption("a", "enable-a", false, "turn [a] on or off");
        options.addOption("b", "bfile", true, "set the value of [b]");
        options.addOption("c", "copt", false, "turn [c] on or off");

        String[] args = new String[] {
            "-z",
            "-a",
            "-b", "toast",
            "foo",
            "bar",
            "baz"
        };

        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, args, true);

        assertNotNull("Parsed CommandLine must not be null", cl);
        assertFalse("Confirm -a is not set because parser should stop at -z", cl.hasOption("a"));
        assertFalse("Confirm -b is not set because parser should stop at -z", cl.hasOption("b"));

        // Exact assertion triggering the ground-truth failure on buggy PosixParser:
        // Expected: 3 extra arguments after parsing stops appropriately; Buggy actual: 7.
        assertTrue("Confirm  3 extra args: " + cl.getArgList().size(), cl.getArgList().size() == 3);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedShortOptionToken() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        // Length == 2 option, not registered, with stopAtNonOption = true
        String[] args = new String[] { "-u", "arg1", "arg2" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        // Under standard gobble with eatTheRest = true in processOptionToken
        assertTrue(flattened.length >= 3);
        assertEquals("-u", flattened[0]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedLongOptionToken() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "--unrecognized", "-a" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        assertEquals(2, flattened.length);
        assertEquals("--unrecognized", flattened[0]);
        assertEquals("-a", flattened[1]);
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFlattenNullOptionsThrowsException() {
        PosixParser parser = new PosixParser();
        parser.flatten(null, new String[] { "-a" }, false);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFlattenNullArgumentsThrowsException() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        parser.flatten(options, null, false);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionWithoutStopThrowsParseException() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("a", false, "Option a");

        String[] args = new String[] { "-z" };
        try {
            parser.parse(options, args, false);
            fail("Expected UnrecognizedOptionException when parsing unknown option with stopAtNonOption=false");
        } catch (ParseException expected) {
            assertTrue("Exception should indicate unrecognized option",
                    expected.getMessage().contains("-z") || expected instanceof UnrecognizedOptionException);
        }
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testPosixParserFullParseIntegration() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("f", "file", true, "The input file");
        options.addOption("v", "verbose", false, "Verbose mode");
        options.addOption("d", "debug", false, "Debug mode");

        String[] args = new String[] { "-vd", "--file=data.txt", "extra1", "extra2" };
        CommandLine cl = parser.parse(options, args);

        assertNotNull(cl);
        assertTrue("Option v should be set", cl.hasOption("v"));
        assertTrue("Option d should be set", cl.hasOption("d"));
        assertTrue("Option file should be set", cl.hasOption("file"));
        assertEquals("File value must match", "data.txt", cl.getOptionValue("file"));

        List argsList = cl.getArgList();
        assertEquals(2, argsList.size());
        assertEquals("extra1", argsList.get(0));
        assertEquals("extra2", argsList.get(1));
    }

    @Test(timeout = 4000)
    public void testProcessOptionTokenDirectBranchCoverage() throws Exception {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        options.addOption("k", false, "Key option");

        // Two-character valid token with stopAtNonOption = true
        String[] args = new String[] { "-k", "value" };
        String[] flattened = parser.flatten(options, args, true);

        assertNotNull(flattened);
        assertEquals(3, flattened.length);
        assertEquals("-k", flattened[0]);
        assertEquals("--", flattened[1]);
        assertEquals("value", flattened[2]);
    }
}