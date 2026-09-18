package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.Properties;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.Parser
 *
 * Decision / Branch Matrix:
 * 1. parse(Options, String[], Properties, boolean)
 *    - arguments == null -> default to empty array String[0]
 *    - iterator token handling:
 *      * "--" -> eatTheRest = true; ignore further "--" tokens
 *      * "-" -> stopAtNonOption == true (eatTheRest = true) vs false (addArg("-"))
 *      * startsWith("-") ->
 *          - stopAtNonOption && !options.hasOption(t) -> eatTheRest = true, addArg(t)
 *          - else -> processOption(t, iterator)
 *      * other token (argument) ->
 *          - addArg(t)
 *          - stopAtNonOption == true (eatTheRest = true) vs false
 *      * eatTheRest loop -> iterates remaining tokens and adds to cmd args if not "--"
 * 2. processProperties(Properties)
 *    - properties == null -> early return
 *    - property key already in cmd -> skipped
 *    - opt.hasArg():
 *      * opt.getValues() == null or length == 0 -> addValue, catch RuntimeException
 *    - !opt.hasArg():
 *      * value is "yes", "true", "1" (case-insensitive) -> addOption(opt)
 *      * other value -> break properties loop
 * 3. checkRequiredOptions()
 *    - requiredOptions.size() > 0 -> construct MissingOptionException message
 *      * DEFECT CLI-13 / Defects4J: In defective code, message is just "f" or "fx"
 *        without "Missing required option: " or "Missing required options: " prefix.
 * 4. processArgs(Option, ListIterator)
 *    - token startsWith("-") and options.hasOption(str) -> iter.previous(), break
 *    - valid value -> opt.addValue(Util.stripLeadingAndTrailingQuotes(str))
 *    - opt.addValue throws RuntimeException -> iter.previous(), break
 *    - opt.getValues() == null && !opt.hasOptionalArg() -> throws MissingArgumentException
 * 5. processOption(String, ListIterator)
 *    - !options.hasOption(arg) -> throws UnrecognizedOptionException
 *    - opt.isRequired() -> removes opt.getKey() from requiredOptions
 *    - options.getOptionGroup(opt) != null:
 *      * group.isRequired() -> removes group from requiredOptions
 *      * group.setSelected(opt)
 *    - opt.hasArg() -> delegates to processArgs
 */
public class ParserGeminiTest {

    /**
     * Concrete Test Parser implementation providing a basic identity flatten
     * to test Parser logic directly.
     */
    private static class BasicTestParser extends Parser {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) {
            return arguments != null ? arguments : new String[0];
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMissingOptionExceptionDefectSingle() {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        Option requiredOpt = OptionBuilder.isRequired().withLongOpt("file").create('f');
        options.addOption(requiredOpt);

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException was not thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required option: f", e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMissingOptionExceptionDefectMultiple() {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create('f'));
        options.addOption(OptionBuilder.isRequired().create('x'));

        try {
            parser.parse(options, new String[0]);
            fail("Expected MissingOptionException was not thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required options: fx", e.getMessage());
        } catch (ParseException e) {
            fail("Unexpected ParseException: " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleParseNoArgs() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");

        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue("Parsed CommandLine must have option 'a'", cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testConvenienceParseSignatures() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");

        // parse(Options, String[])
        CommandLine cmd1 = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd1.hasOption("a"));

        // parse(Options, String[], boolean)
        CommandLine cmd2 = parser.parse(options, new String[]{"-a", "arg1"}, true);
        assertTrue(cmd2.hasOption("a"));
        assertArrayEquals(new String[]{"arg1"}, cmd2.getArgs());

        // parse(Options, String[], Properties)
        Properties props = new Properties();
        CommandLine cmd3 = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd3.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseOptionWithArgument() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("f", true, "file option");

        CommandLine cmd = parser.parse(options, new String[]{"-f", "test.txt"});
        assertTrue(cmd.hasOption("f"));
        assertEquals("test.txt", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testProcessArgsStopsAtNextOption() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", true, "option a with arg");
        options.addOption("b", false, "option b");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "valueA", "-b"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("valueA", cmd.getOptionValue("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testDoubleDashStopsOptionParsing() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "--", "arg1"});
        assertTrue(cmd.hasOption("a"));
        assertFalse("Option 'b' should be treated as non-option arg", cmd.hasOption("b"));
        assertArrayEquals(new String[]{"-b", "arg1"}, cmd.getArgs());
    }

    @Test(timeout = 4000)
    public void testSingleDashHandling() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");

        // Single dash with stopAtNonOption = false
        CommandLine cmdFalse = parser.parse(options, new String[]{"-", "-a"}, false);
        assertTrue(cmdFalse.hasOption("a"));
        assertArrayEquals(new String[]{"-"}, cmdFalse.getArgs());

        // Single dash with stopAtNonOption = true -> triggers eatTheRest
        CommandLine cmdTrue = parser.parse(options, new String[]{"-", "-a"}, true);
        assertFalse(cmdTrue.hasOption("a"));
        assertArrayEquals(new String[]{"-a"}, cmdTrue.getArgs());
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionNormalArgument() throws ParseException {
        Parser parser = new BasicTestParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "file.txt", "-b"}, true);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
        assertArrayEquals(new String[]{"file.txt", "-b"}, cmd.getArgs());
    }

    @Test(timeout = 4000)