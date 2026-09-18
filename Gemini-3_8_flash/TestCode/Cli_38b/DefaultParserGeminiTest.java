package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/*
 * [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.cli.DefaultParser
 *
 * 1. CLI-265 Defect Analysis:
 *    - In isShortOption(token), concatenated short options like "-ab" return false because
 *      options.hasShortOption("ab") is false.
 *    - Consequently, isArgument("-ab") evaluates to true. When preceded by an option that accepts an
 *      optional argument (e.g., -a), "-ab" is wrongly consumed as the value of "-a" rather than
 *      being parsed as option tokens.
 *    - Defect-revealing test: shouldParseConcatenatedShortOptions asserts null for cmd.getOptionValue("a")
 *      when inputs are ["-a", "-ab"], reproducing:
 *      junit.framework.AssertionFailedError: expected null, but was:<-ab>
 *
 * 2. Decision & Branch Coverage:
 *    - parse() overloads: (options, args), (options, args, props), (options, args, stop), (options, args, props, stop).
 *    - arguments: null, empty, lone hyphen "-", terminator "--", subsequent tokens when skipParsing=true.
 *    - handleToken:
 *      * skipParsing active -> cmd.addArg()
 *      * "--" delimiter -> skipParsing = true
 *      * currentOption.acceptsArg() && isArgument() -> add value (with quote stripping)
 *      * startsWith("--") -> handleLongOption (with/without '=')
 *      * startsWith("-") && !"-".equals() -> handleShortAndLongOption
 *      * else -> handleUnknownToken
 *    - isNegativeNumber: numeric values (-1, -3.14) vs non-numeric options (-abc).
 *    - handleLongOption / handleLongOptionWithEqual / handleLongOptionWithoutEqual:
 *      * matchingOpts.isEmpty() -> unknown token (error or arg)
 *      * matchingOpts.size() > 1 -> AmbiguousOptionException
 *      * single match -> handleOption; with '=' checks acceptsArg() vs unknown token.
 *    - handleShortAndLongOption:
 *      * length 1 (-S) -> short option match vs unknown token
 *      * pos == -1: short option vs long matching option vs long prefix (-Xmx) vs java property (-Dkey) vs burst concatenated
 *      * pos != -1: short with '=' vs java property with '=' (-Dkey=value) vs long with '='
 *    - handleConcatenatedOptions:
 *      * valid flags bursting
 *      * flag with argument consumes remaining characters
 *      * unknown character with stopAtNonOption (i > 1 bursts suffix) vs without stopAtNonOption
 *    - handleProperties:
 *      * null properties -> no-op
 *      * undefined option in properties -> UnrecognizedOptionException
 *      * option group conflict (already selected) -> skipped
 *      * command line already has option -> skipped
 *      * option hasArg() -> adds value
 *      * boolean option with "yes", "true", "1" -> handles option; other values ignored
 *    - checkRequiredOptions: missing required options/groups throws MissingOptionException.
 *    - checkRequiredArgs: currentOption.requiresArg() throws MissingArgumentException.
 *    - updateRequiredOptions: OptionGroup single-selection enforcement -> AlreadySelectedException.
 */
public class DefaultParserGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-265)
    // =========================================================================

    /**
     * Targets Defects4J CLI-265:
     * When an option accepts an optional argument, following concatenated short options
     * (e.g., "-ab") must NOT be treated as a value argument to that option.
     */
    @Test(timeout = 4000)
    public void shouldParseConcatenatedShortOptions() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option optA = new Option("a", "option A");
        optA.setArgs(1);
        optA.setOptionalArg(true);

        Option optB = new Option("b", "option B");

        options.addOption(optA);
        options.addOption(optB);

        CommandLine cmd = parser.parse(options, new String[]{"-a", "-ab"});
        assertNull(cmd.getOptionValue("a"));
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseSimpleShortAndLongOptions() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "all", false, "toggle all");
        options.addOption("f", "file", true, "file path");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "--file", "output.txt"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("file"));
        assertEquals("output.txt", cmd.getOptionValue("f"));
        assertEquals("output.txt", cmd.getOptionValue("file"));
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseAllOverloads() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("v", "verbose", false, "verbose output");

        // 1. parse(options, arguments)
        CommandLine cmd1 = parser.parse(options, new String[]{"-v"});
        assertTrue(cmd1.hasOption("v"));

        // 2. parse(options, arguments, stopAtNonOption)
        CommandLine cmd2 = parser.parse(options, new String[]{"-v", "extra"}, true);
        assertTrue(cmd2.hasOption("v"));
        assertEquals(1, cmd2.getArgs().length);

        // 3. parse(options, arguments, properties)
        Properties props = new Properties();
        props.setProperty("v", "true");
        CommandLine cmd3 = parser.parse(options, new String[0], props);
        assertTrue(cmd3.hasOption("v"));

        // 4. parse(options, arguments, properties, stopAtNonOption)
        CommandLine cmd4 = parser.parse(options, new String[]{"extra"}, props, true);
        assertTrue(cmd4.hasOption("v"));
        assertEquals("extra", cmd4.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testDoubleHyphenStopsOptionParsing() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "option a");
        options.addOption("b", false, "option b");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "--", "-b", "--other"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));

        List<String> remaining = Arrays.asList(cmd.getArgs());
        assertEquals(2, remaining.size());
        assertEquals("-b", remaining.get(0));
        assertEquals("--other", remaining.get(1));
    }

    @Test(timeout = 4000)
    public void testQuotedArgumentStripping() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", true, "file");

        CommandLine cmd1 = parser.parse(options, new String[]{"-f", "\"my file.txt\""});
        assertEquals("my file.txt", cmd1.getOptionValue("f"));

        CommandLine cmd2 = parser.parse(options, new String[]{"-f", "'my file.txt'"});
        assertEquals("my file.txt", cmd2.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testLongOptionWithEqualSign() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", "file", true, "target file");

        CommandLine cmd = parser.parse(options, new String[]{"--file=test.log"});
        assertTrue(cmd.hasOption("file"));
        assertEquals("test.log", cmd.getOptionValue("file"));
    }

    @Test(timeout = 4000)
    public void testShortOptionWithEqualSign() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("s", true, "short opt");

        CommandLine cmd = parser.parse(options, new String[]{"-s=data"});
        assertTrue(cmd.hasOption("s"));
        assertEquals("data", cmd.getOptionValue("s"));
    }

    @Test(timeout = 4000)
    public void testJavaPropertyOptionHandling() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option propOpt = new Option("D", "define property");
        propOpt.setArgs(2);
        propOpt.setValueSeparator('=');
        options.addOption(propOpt);

        // Case 1: -Dkey=val (with equal sign)
        CommandLine cmd1 = parser.parse(options, new String[]{"-Dparam=val"});
        assertTrue(cmd1.hasOption("D"));
        String[] vals1 = cmd1.getOptionValues("D");
        assertEquals("param", vals1[0]);
        assertEquals("val", vals1[1]);

        // Case 2: -Dkey (without equal sign)
        CommandLine cmd2 = parser.parse(options, new String[]{"-DparamOnly"});
        assertTrue(cmd2.hasOption("D"));
        assertEquals("paramOnly", cmd2.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testLongPrefixOptionMatching() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option optXmx = new Option("Xmx", "Xmx", true, "jvm memory");
        options.addOption(optXmx);

        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testConcatenatedShortOptionsBursting() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");
        options.addOption("b", false, "opt b");
        options.addOption("c", true, "opt c with arg");

        // -abcValue: a and b are flags, c consumes remaining "Value"
        CommandLine cmd = parser.parse(options, new String[]{"-abcValue"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
        assertEquals("Value", cmd.getOptionValue("c"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullArgumentsArray() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, null);
        assertNotNull(cmd);
        assertEquals(0, cmd.getOptions().length);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testEmptyArgumentsArray() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[0]);
        assertNotNull(cmd);
        assertEquals(0, cmd.getOptions().length);
        assertEquals(0, cmd.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testSingleHyphenArgument() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "-"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testNegativeNumberAsOptionArgument() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("n", "number", true, "numeric value");

        CommandLine cmd = parser.parse(options, new String[]{"-n", "-42.5"});
        assertTrue(cmd.hasOption("n"));
        assertEquals("-42.5", cmd.getOptionValue("n"));
    }

    @Test(timeout = 4000)
    public void testLongOptionPartialMatching() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        CommandLine cmd = parser.parse(options, new String[]{"--vers"});
        assertTrue(cmd.hasOption("version"));
    }

    @Test(timeout = 4000)
    public void testPartialLongOptionWithEqual() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("c", "config", true, "config file");

        CommandLine cmd = parser.parse(options, new String[]{"--conf=app.conf"});
        assertTrue(cmd.hasOption("config"));
        assertEquals("app.conf", cmd.getOptionValue("config"));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionBehavior() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "opt a");
        options.addOption("b", false, "opt b");

        CommandLine cmd = parser.parse(options, new String[]{"-a", "non-option-arg", "-b"}, true);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));

        String[] remaining = cmd.getArgs();
        assertEquals(2, remaining.length);
        assertEquals("non-option-arg", remaining[0]);
        assertEquals("-b", remaining[1]);
    }

    @Test(timeout = 4000)
    public void testConcatenatedOptionWithStopAtNonOption() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "flag a");

        // -az where 'a' is valid and 'z' is unknown, with stopAtNonOption=true
        CommandLine cmd = parser.parse(options, new String[]{"-az", "-extra"}, true);
        assertTrue(cmd.hasOption("a"));
        String[] args = cmd.getArgs();
        assertEquals(2, args.length);
        assertEquals("z", args[0]);
        assertEquals("-extra", args[1]);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedLongOptionThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedShortOptionThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"-u"});
    }

    @Test(expected = AmbiguousOptionException.class, timeout = 4000)
    public void testAmbiguousLongOptionThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(new Option("f", "foo", false, "foo option"));
        options.addOption(new Option("b", "foobar", false, "foobar option"));

        parser.parse(options, new String[]{"--fo"});
    }

    @Test(expected = AmbiguousOptionException.class, timeout = 4000)
    public void testAmbiguousLongOptionWithEqualThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(new Option("f", "foo", true, "foo option"));
        options.addOption(new Option("b", "foobar", true, "foobar option"));

        parser.parse(options, new String[]{"--fo=value"});
    }

    @Test(expected = MissingOptionException.class, timeout = 4000)
    public void testMissingRequiredOptionThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        Option req = new Option("r", "required", false, "mandatory");
        req.setRequired(true);
        options.addOption(req);

        parser.parse(options, new String[0]);
    }

    @Test(expected = MissingOptionException.class, timeout = 4000)
    public void testMissingRequiredOptionGroupThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "opt a"));
        group.addOption(new Option("b", "opt b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[0]);
    }

    @Test(expected = AlreadySelectedException.class, timeout = 4000)
    public void testMultipleOptionsFromSameGroupThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "opt a"));
        group.addOption(new Option("b", "opt b"));
        options.addOptionGroup(group);

        parser.parse(options, new String[]{"-a", "-b"});
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentAtEndOfArgsThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", true, "requires file argument");

        parser.parse(options, new String[]{"-f"});
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentBeforeNextOptionThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", true, "requires file argument");
        options.addOption("v", false, "verbose flag");

        parser.parse(options, new String[]{"-f", "-v"});
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testLongOptionWithEqualWhenArgNotAcceptedThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("v", "version", false, "no arg accepted");

        parser.parse(options, new String[]{"--version=unexpected"});
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testShortOptionWithEqualWhenArgNotAcceptedThrowsException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("v", false, "no arg accepted");

        parser.parse(options, new String[]{"-v=unexpected"});
    }

    // =========================================================================
    // Partition E: Properties Handling & Option Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertiesHandlingForArgsAndFlags() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", "file", true, "target file");
        options.addOption("v", "verbose", false, "verbose flag");
        options.addOption("d", "debug", false, "debug flag");
        options.addOption("x", "extra", false, "extra flag");

        Properties props = new Properties();
        props.setProperty("f", "props_file.txt");
        props.setProperty("v", "true");
        props.setProperty("d", "yes");
        props.setProperty("x", "1");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertEquals("props_file.txt", cmd.getOptionValue("f"));
        assertTrue(cmd.hasOption("v"));
        assertTrue(cmd.hasOption("d"));
        assertTrue(cmd.hasOption("x"));
    }

    @Test(timeout = 4000)
    public void testPropertiesIgnoredWhenAlreadyProvidedOnCommandLine() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("f", true, "target file");

        Properties props = new Properties();
        props.setProperty("f", "from_properties.txt");

        CommandLine cmd = parser.parse(options, new String[]{"-f", "from_cli.txt"}, props);
        assertEquals("from_cli.txt", cmd.getOptionValue("f"));
    }

    @Test(timeout = 4000)
    public void testPropertiesFalsyValuesDoNotEnableFlag() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("v", false, "verbose flag");

        Properties props = new Properties();
        props.setProperty("v", "no");

        CommandLine cmd = parser.parse(options, new String[0], props);
        assertFalse(cmd.hasOption("v"));
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUndefinedPropertyThrowsUnrecognizedOptionException() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Properties props = new Properties();
        props.setProperty("unknownProp", "value");

        parser.parse(options, new String[0], props);
    }

    @Test(timeout = 4000)
    public void testPropertiesRespectOptionGroupExclusion() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testMultipleOptionValuesProcessing() throws Exception {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();

        Option opt = new Option("m", "multi", true, "multiple values");
        opt.setArgs(2);
        options.addOption(opt);

        CommandLine cmd = parser.parse(options, new String[]{"-m", "val1", "val2"});
        assertTrue(cmd.hasOption("m"));
        String[] values = cmd.getOptionValues("m");
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }
}