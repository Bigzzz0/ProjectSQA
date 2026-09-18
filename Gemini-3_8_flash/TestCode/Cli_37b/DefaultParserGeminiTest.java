package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli.DefaultParser
 *
 * Branches & Logic Tested:
 * 1. CLI-265 Ground Truth: Multi-character short options (e.g. "-last") evaluated during argument
 *    processing where previous option has optional args (isShortOption checked substring(1,2) only).
 * 2. handleToken dispatch:
 *    - skipParsing flag set by "--" token or stopAtNonOption on unknown token
 *    - currentOption != null && acceptsArg() && isArgument()
 *    - token.startsWith("--") -> handleLongOption (with '=' and without '=')
 *    - token.startsWith("-") && !"-".equals(token) -> handleShortAndLongOption
 *    - Unknown tokens / bare "-" handling
 * 3. isArgument & isNegativeNumber:
 *    - Numbers parseable as Double (-42, -3.14) recognized as negative number arguments
 *    - Options recognized as non-arguments unless negative numbers
 * 4. handleLongOption:
 *    - Ambiguous long options detection (size > 1 -> AmbiguousOptionException)
 *    - Unrecognized long options
 *    - Long options with values (acceptsArg vs !acceptsArg)
 * 5. handleShortAndLongOption:
 *    - Single char short option (-s)
 *    - Multi-character short option or long match (-L)
 *    - Long prefix match (-Xmx512m style)
 *    - Java-style property (-Dkey=value or -Dkey with args >= 2)
 *    - Option concatenations/bursting (-abc, -abValue, -unknown)
 * 6. Properties handling:
 *    - Missing option in properties -> UnrecognizedOptionException
 *    - Option already present on command line -> skipped
 *    - Option in group already selected -> skipped
 *    - Option with arguments -> property value attached
 *    - Boolean options -> activated only on "yes", "true", "1" (case insensitive)
 * 7. Group & Required checks:
 *    - AlreadySelectedException on conflicting group options
 *    - MissingOptionException on unsatisfied required options or groups
 *    - MissingArgumentException on trailing unfulfilled option requiring argument
 * 8. Extremes & Nulls:
 *    - arguments == null, empty array, properties == null, bare dash ("-")
 */
public class DefaultParserGeminiTest
{
    private final DefaultParser parser = new DefaultParser();

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-265 Ground Truth)
    // =========================================================================

    /**
     * Targets Bug CLI-265:
     * When an option specifies an optional argument (e.g. -t1), followed by a multi-character
     * option without long format (e.g. -last), the parser must not consume "-last" as the
     * value of "-t1".
     */
    @Test(timeout = 4000)
    public void testCli265ShortOptionWithoutValue() throws Exception
    {
        Options options = new Options();
        Option t1 = new Option("t1", "has optional arg");
        t1.setArgs(1);
        t1.setOptionalArg(true);

        Option last = new Option("last", "second option flag");

        options.addOption(t1);
        options.addOption(last);

        String[] args = new String[] { "-t1", "-last" };
        CommandLine cl = parser.parse(options, args);

        assertTrue("Option t1 should be present", cl.hasOption("t1"));
        assertFalse("Second option has been used as value for first option. Actual: " + cl.getOptionValue("t1"),
                "-last".equals(cl.getOptionValue("t1")));
        assertTrue("Option last should be present", cl.hasOption("last"));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSimpleShortOptions() throws Exception
    {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option");

        CommandLine cl = parser.parse(options, new String[] { "-a", "-b", "valB" });
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertEquals("valB", cl.getOptionValue("b"));
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testLongOptionWithAndWithoutEquals() throws Exception
    {
        Options options = new Options();
        options.addOption("f", "file", true, "input file");
        options.addOption("o", "output", true, "output file");

        CommandLine cl = parser.parse(options, new String[] { "--file=in.txt", "--output", "out.txt" });
        assertTrue(cl.hasOption("file"));
        assertEquals("in.txt", cl.getOptionValue("file"));
        assertTrue(cl.hasOption("output"));
        assertEquals("out.txt", cl.getOptionValue("output"));
    }

    @Test(timeout = 4000)
    public void testConcatenatedShortOptions() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", true, "option c with arg");

        CommandLine cl = parser.parse(options, new String[] { "-abcValue" });
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
        assertEquals("Value", cl.getOptionValue("c"));
    }

    @Test(timeout = 4000)
    public void testConcatenatedShortFlagsOnly() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");
        options.addOption("c", false, "flag c");

        CommandLine cl = parser.parse(options, new String[] { "-abc" });
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
        assertTrue(cl.hasOption("c"));
    }

    @Test(timeout = 4000)
    public void testJavaStylePropertyKeyValue() throws Exception
    {
        Options options = new Options();
        Option prop = new Option("D", "define property");
        prop.setArgs(2);
        prop.setValueSeparator('=');
        options.addOption(prop);

        CommandLine cl = parser.parse(options, new String[] { "-Dkey=value" });
        assertTrue(cl.hasOption("D"));
        String[] values = cl.getOptionValues("D");
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("key", values[0]);
        assertEquals("value", values[1]);
    }

    @Test(timeout = 4000)
    public void testJavaStylePropertyFlagValue() throws Exception
    {
        Options options = new Options();
        Option prop = new Option("D", "define flag");
        prop.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(prop);

        CommandLine cl = parser.parse(options, new String[] { "-DflagOnly" });
        assertTrue(cl.hasOption("D"));
        assertEquals("flagOnly", cl.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testLongOptionPrefixLikeXmx() throws Exception
    {
        Options options = new Options();
        Option xmx = new Option("Xmx", true, "max memory");
        xmx.setLongOpt("Xmx");
        options.addOption(xmx);

        CommandLine cl = parser.parse(options, new String[] { "-Xmx1024m" });
        assertTrue(cl.hasOption("Xmx"));
        assertEquals("1024m", cl.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testNegativeNumberAsArgument() throws Exception
    {
        Options options = new Options();
        options.addOption("n", "num", true, "number");

        CommandLine cl = parser.parse(options, new String[] { "-n", "-42.5" });
        assertTrue(cl.hasOption("n"));
        assertEquals("-42.5", cl.getOptionValue("n"));
    }

    @Test(timeout = 4000)
    public void testStopParsingDelimiterDoubleDash() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");

        CommandLine cl = parser.parse(options, new String[] { "-a", "--", "-b", "--foo", "bar" });
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));

        List<String> argList = cl.getArgList();
        assertEquals(3, argList.size());
        assertEquals("-b", argList.get(0));
        assertEquals("--foo", argList.get(1));
        assertEquals("bar", argList.get(2));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionFlag() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", false, "flag b");

        CommandLine cl = parser.parse(options, new String[] { "-a", "nonOption", "-b" }, true);
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));

        assertEquals(2, cl.getArgs().length);
        assertEquals("nonOption", cl.getArgs()[0]);
        assertEquals("-b", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnknownDashToken() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");

        CommandLine cl = parser.parse(options, new String[] { "-a", "-unknown", "extra" }, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-unknown", cl.getArgs()[0]);
        assertEquals("extra", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testQuotedArgumentQuotesStripped() throws Exception
    {
        Options options = new Options();
        options.addOption("m", "message", true, "commit message");

        CommandLine cl = parser.parse(options, new String[] { "-m", "\"hello world\"" });
        assertEquals("hello world", cl.getOptionValue("m"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullArgumentsArray() throws Exception
    {
        Options options = new Options();
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testEmptyArgumentsArray() throws Exception
    {
        Options options = new Options();
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertEquals(0, cl.getOptions().length);
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testSingleHyphenArgument() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");

        CommandLine cl = parser.parse(options, new String[] { "-", "-a", "-" });
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]);
        assertEquals("-", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testShortOptionWithEqualSign() throws Exception
    {
        Options options = new Options();
        options.addOption("s", true, "setting");

        CommandLine cl = parser.parse(options, new String[] { "-s=value123" });
        assertTrue(cl.hasOption("s"));
        assertEquals("value123", cl.getOptionValue("s"));
    }

    @Test(timeout = 4000)
    public void testPartialLongOptionMatching() throws Exception
    {
        Options options = new Options();
        options.addOption("verbose", false, "enable verbosity");

        CommandLine cl = parser.parse(options, new String[] { "--verb" });
        assertTrue(cl.hasOption("verbose"));
    }

    @Test(timeout = 4000)
    public void testPartialLongOptionWithEqualSign() throws Exception
    {
        Options options = new Options();
        options.addOption("output", true, "output file");

        CommandLine cl = parser.parse(options, new String[] { "--out=dest.txt" });
        assertTrue(cl.hasOption("output"));
        assertEquals("dest.txt", cl.getOptionValue("output"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedOptionThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");

        parser.parse(options, new String[] { "-z" });
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedLongOptionThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("a", "alpha", false, "flag a");

        parser.parse(options, new String[] { "--unknown" });
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedLongOptionWithEqualThrows() throws Exception
    {
        Options options = new Options();
        parser.parse(options, new String[] { "--unknown=value" });
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testLongOptionWithEqualWhenOptionDoesNotAcceptArgThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("n", "noarg", false, "flag without arg");

        parser.parse(options, new String[] { "--noarg=illegalValue" });
    }

    @Test(expected = AmbiguousOptionException.class, timeout = 4000)
    public void testAmbiguousOptionThrows() throws Exception
    {
        Options options = new Options();
        options.addOption(new Option("v", "version", false, "show version"));
        options.addOption(new Option("b", "verbose", false, "verbose mode"));

        parser.parse(options, new String[] { "--ver" });
    }

    @Test(expected = AmbiguousOptionException.class, timeout = 4000)
    public void testAmbiguousOptionWithEqualThrows() throws Exception
    {
        Options options = new Options();
        options.addOption(new Option("f", "file1", true, "file 1"));
        options.addOption(new Option("o", "file2", true, "file 2"));

        parser.parse(options, new String[] { "--file=something" });
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentAtEndOfArgsThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("f", "file", true, "target file");

        parser.parse(options, new String[] { "-f" });
    }

    @Test(expected = MissingArgumentException.class, timeout = 4000)
    public void testMissingArgumentBeforeAnotherOptionThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("f", "file", true, "target file");
        options.addOption("v", "verbose", false, "verbose flag");

        parser.parse(options, new String[] { "-f", "-v" });
    }

    @Test(expected = MissingOptionException.class, timeout = 4000)
    public void testMissingRequiredOptionThrows() throws Exception
    {
        Options options = new Options();
        Option req = new Option("r", "required", true, "mandatory option");
        req.setRequired(true);
        options.addOption(req);

        parser.parse(options, new String[] { "arg1" });
    }

    @Test(expected = MissingOptionException.class, timeout = 4000)
    public void testMissingRequiredOptionGroupThrows() throws Exception
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[] { "standaloneArg" });
    }

    @Test(expected = AlreadySelectedException.class, timeout = 4000)
    public void testMutuallyExclusiveOptionGroupThrows() throws Exception
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "option A"));
        group.addOption(new Option("b", "option B"));
        options.addOptionGroup(group);

        parser.parse(options, new String[] { "-a", "-b" });
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testConcatenatedOptionWithUnknownCharThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");

        parser.parse(options, new String[] { "-az" });
    }

    @Test(timeout = 4000)
    public void testConcatenatedOptionWithUnknownCharAndStopAtNonOption() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");

        CommandLine cl = parser.parse(options, new String[] { "-az" }, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("z", cl.getArgs()[0]);
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testShortOptionWithEqualWhenOptionDoesNotAcceptArgThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag without arg");

        parser.parse(options, new String[] { "-a=badValue" });
    }

    // =========================================================================
    // Partition E: Properties Integration & State Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testPropertiesSupplyMissingOptions() throws Exception
    {
        Options options = new Options();
        options.addOption("f", "file", true, "filename");
        options.addOption("v", "verbose", false, "verbose flag");
        options.addOption("d", "debug", false, "debug flag");
        options.addOption("s", "silent", false, "silent flag");
        options.addOption("q", "quiet", false, "quiet flag");

        Properties props = new Properties();
        props.setProperty("file", "default.txt");
        props.setProperty("verbose", "true");
        props.setProperty("debug", "yes");
        props.setProperty("silent", "1");
        props.setProperty("quiet", "no");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("file"));
        assertEquals("default.txt", cl.getOptionValue("file"));
        assertTrue(cl.hasOption("verbose"));
        assertTrue(cl.hasOption("debug"));
        assertTrue(cl.hasOption("silent"));
        assertFalse("Quiet should NOT be enabled since value is 'no'", cl.hasOption("quiet"));
    }

    @Test(timeout = 4000)
    public void testCommandLineOverridesProperties() throws Exception
    {
        Options options = new Options();
        options.addOption("f", "file", true, "filename");

        Properties props = new Properties();
        props.setProperty("file", "fromProps.txt");

        CommandLine cl = parser.parse(options, new String[] { "-f", "fromCmd.txt" }, props);
        assertEquals("fromCmd.txt", cl.getOptionValue("file"));
    }

    @Test(timeout = 4000)
    public void testPropertiesDoNotOverrideOptionGroupSelection() throws Exception
    {
        Options options = new Options();
        Option optA = new Option("a", "option A");
        Option optB = new Option("b", "option B");
        OptionGroup group = new OptionGroup();
        group.addOption(optA);
        group.addOption(optB);
        options.addOptionGroup(group);

        Properties props = new Properties();
        props.setProperty("b", "true");

        CommandLine cl = parser.parse(options, new String[] { "-a" }, props);
        assertTrue(cl.hasOption("a"));
        assertFalse("Option B from properties must not override selection", cl.hasOption("b"));
    }

    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testPropertiesWithUndefinedOptionThrows() throws Exception
    {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        Properties props = new Properties();
        props.setProperty("unknownProp", "someVal");

        parser.parse(options, new String[0], props);
    }

    @Test(timeout = 4000)
    public void testRequiredOptionSatisfiedByProperties() throws Exception
    {
        Options options = new Options();
        Option req = new Option("r", "req", true, "required option");
        req.setRequired(true);
        options.addOption(req);

        Properties props = new Properties();
        props.setProperty("r", "propValue");

        CommandLine cl = parser.parse(options, new String[0], props);
        assertTrue(cl.hasOption("r"));
        assertEquals("propValue", cl.getOptionValue("r"));
    }

    @Test(timeout = 4000)
    public void testReusableParserAcrossMultipleRuns() throws Exception
    {
        Options options = new Options();
        options.addOption("a", false, "flag a");
        options.addOption("b", true, "option b");

        CommandLine cl1 = parser.parse(options, new String[] { "-a", "-b", "first" });
        assertTrue(cl1.hasOption("a"));
        assertEquals("first", cl1.getOptionValue("b"));

        CommandLine cl2 = parser.parse(options, new String[] { "-b", "second" });
        assertFalse(cl2.hasOption("a"));
        assertEquals("second", cl2.getOptionValue("b"));
    }
}