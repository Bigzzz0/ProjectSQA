/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.Parser
 * Subclass under test: DummyParser (direct concrete pass-through for flatten)
 *
 * Decision / Branch Matrix:
 * 1. parse(...) overloads:
 *    - parse(Options, String[]) -> forwards to 4-arg parse
 *    - parse(Options, String[], Properties) -> forwards to 4-arg parse
 *    - parse(Options, String[], boolean) -> forwards to 4-arg parse
 *    - parse(Options, String[], Properties, boolean) -> core logic
 * 2. Token Loop & Control Flow:
 *    - arguments == null -> default to empty array
 *    - token "--" -> eatTheRest = true
 *    - token "-" with stopAtNonOption = true -> eatTheRest = true
 *    - token "-" with stopAtNonOption = false -> cmd.addArg("-")
 *    - token starting with "-":
 *        * stopAtNonOption && !options.hasOption(t) -> eatTheRest = true, cmd.addArg(t)
 *        * hasOption(t) -> processOption(t, iter)
 *        * !hasOption(t) && !stopAtNonOption -> throws UnrecognizedOptionException
 *    - non-option token:
 *        * cmd.addArg(t)
 *        * stopAtNonOption -> eatTheRest = true
 *    - eatTheRest loop:
 *        * str != "--" -> cmd.addArg(str)
 *        * str == "--" -> ignored / skipped
 * 3. processOption & updateRequiredOptions:
 *    - !hasOption -> throws UnrecognizedOptionException
 *    - opt.isRequired() -> removed from requiredOptions list
 *    - opt in OptionGroup:
 *        * group.isRequired() -> removed from requiredOptions list
 *        * group.setSelected(opt)
 *    - opt.hasArg() -> delegates to processArgs
 * 4. processArgs:
 *    - loop tokens until next token is an Option (hasOption && startsWith("-")) -> iter.previous(), break
 *    - token stripped of quotes via Util.stripLeadingAndTrailingQuotes
 *    - addValueForProcessing throws RuntimeException -> iter.previous(), break
 *    - if values is null and !opt.hasOptionalArg() -> throws MissingArgumentException
 * 5. checkRequiredOptions:
 *    - !requiredOptions.isEmpty() -> throws MissingOptionException
 * 6. processProperties:
 *    - properties == null -> returns immediately
 *    - properties with option already in cmd -> ignored
 *    - properties with option with args:
 *        * values == null or empty -> addValueForProcessing(value)
 *        * RuntimeException on addValueForProcessing -> caught and ignored
 *        * values already present -> skipped
 *    - properties with boolean option (no args):
 *        * "true", "yes", "1" (case insensitive) -> cmd.addOption(opt), updateRequiredOptions(opt)
 *        * other values ("no", "0", "false", etc.) -> ignored
 *
 * Defects4J Ground Truth Defects:
 * - testPropertyOptionUnexpected:
 *    Property contains a key not configured in Options -> getOptions().getOption(option)
 *    returns null -> opt.hasArg() causes NullPointerException.
 * - testPropertyOptionGroup:
 *    Property contains an option belonging to an OptionGroup when another option from that
 *    group was already parsed / selected -> throws AlreadySelectedException instead of handling safely.
 */

package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

public class ParserGeminiTest
{
    /**
     * Concrete test implementation of Parser using a transparent pass-through for flatten.
     */
    private static class DummyParser extends Parser
    {
        @Override
        protected String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption)
        {
            return arguments != null ? arguments : new String[0];
        }

        // Expose protected helpers for direct white-box assertions
        public Options exposeOptions()
        {
            return getOptions();
        }

        public List exposeRequiredOptions()
        {
            return getRequiredOptions();
        }

        public void exposeSetOptions(Options options)
        {
            setOptions(options);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseTwoArgOverload() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        options.addOption("a", false, "Option A");

        CommandLine cmd = parser.parse(options, new String[] { "-a" });
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseThreeArgWithPropertiesOverload() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        options.addOption("b", true, "Option B");

        Properties props = new Properties();
        props.setProperty("b", "valB");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("b"));
        assertEquals("valB", cmd.getOptionValue("b"));
    }

    @Test(timeout = 4000)
    public void testParseThreeArgWithStopAtNonOptionOverload() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        options.addOption("a", false, "Option A");

        CommandLine cmd = parser.parse(options, new String[] { "-a", "extra1", "extra2" }, true);
        assertNotNull(cmd);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("extra1", cmd.getArgs()[0]);
        assertEquals("extra2", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testOptionWithValueProcessingAndQuotesStripping() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        options.addOption("f", true, "File path");

        CommandLine cmd = parser.parse(options, new String[] { "-f", "\"quoted/path/to/file\"" });
        assertTrue(cmd.hasOption("f"));
        assertEquals("quoted/path/to/file", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testMultipleOptionArgsBreakOnNextOption() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        Option m = OptionBuilder.hasArgs(3).create('m');
        options.addOption(m);
        options.addOption("n", false, "Next Option");

        CommandLine cmd = parser.parse(options, new String[] { "-m", "val1", "val2", "-n" });
        assertTrue(cmd.hasOption("m"));
        assertTrue(cmd.hasOption("n"));
        String[] vals = cmd.getOptionValues("m");
        assertEquals(2, vals.length);
        assertEquals("val1", vals[0]);
        assertEquals("val2", vals[1]);
    }

    @Test(timeout = 4000)
    public void testOptionalArgumentProvided() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        Option opt = OptionBuilder.hasOptionalArg().create('o');
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[] { "-o", "optValue" });
        assertTrue(cmd.hasOption("o"));
        assertEquals("optValue", cmd.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testOptionalArgumentOmitted() throws Exception
    {
        DummyParser parser = new DummyParser();
        Options options = new Options();
        Option opt =