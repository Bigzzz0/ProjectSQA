package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

/**
 * JUnit 4 test suite for {@link CommandLine}.
 *
 * This suite exercises both the population methods ({@code addOption},
 * {@code addArg}) and the query methods ({@code hasOption},
 * {@code getOptionValue}, {@code getOptionValues}, {@code getOptionObject},
 * {@code getArgs}, {@code getArgList}, {@code getOptions}, {@code iterator})
 * of {@link CommandLine}, and specifically targets the known CLI-13 defect
 * relating to inconsistent short/long option key resolution.
 */
public class CommandLineClaudeTest
{

    private CommandLine cmd;

    @Before
    public void setUp()
    {
        cmd = new CommandLine();
    }

    /**
     * @target CommandLine#hasOption(String), #getOptionValue(String), #getOptionValues(String)
     * @scenario An Option is registered with both a short key ("T") and a long key
     *           ("test") plus a single argument value. The option is then queried
     *           using the short key, the hyphenated short key, the long key and the
     *           hyphenated long key.
     * @defectRisk CLI-13: {@code hasOption(String)} performs a raw {@code Map#containsKey}
     *             lookup on the internal options map without stripping leading hyphens or
     *             resolving the long-option alias through the internal {@code names} map
     *             -- unlike {@code getOptionValue}/{@code getOptionValues} which DO perform
     *             this resolution. This inconsistency causes {@code hasOption("test")} and
     *             {@code hasOption("-T")} to incorrectly report {@code false} even though
     *             the option was properly registered, exposing a flawed key-resolution
     *             defect that this test is designed to detect.
     */
    @Test(timeout = 4000)
    public void testCLI13_OptionResolutionAndValueRetrieval() throws Exception
    {
        Option option = new Option("T", "test", true, "test option");
        option.addValue("alpha");

        cmd.addOption(option);

        // direct short-key lookup
        assertTrue("hasOption(short) should be true", cmd.hasOption("T"));
        assertTrue("hasOption(hyphenated short) should be true", cmd.hasOption("-T"));

        // long-name / hyphenated resolution -- exposes CLI-13 defect
        assertTrue("hasOption(long) should resolve via names map", cmd.hasOption("test"));
        assertTrue("hasOption(hyphenated long) should resolve", cmd.hasOption("--test"));

        // value retrieval should be consistent regardless of which alias is used
        assertEquals("alpha", cmd.getOptionValue("T"));
        assertEquals("alpha", cmd.getOptionValue("-T"));
        assertEquals("alpha", cmd.getOptionValue("test"));
        assertEquals("alpha", cmd.getOptionValue("--test"));

        String[] valuesShort = cmd.getOptionValues("T");
        String[] valuesLong = cmd.getOptionValues("test");
        assertNotNull(valuesShort);
        assertNotNull(valuesLong);
        assertArrayEquals(valuesShort, valuesLong);
    }

    /**
     * @target CommandLine#getOptionObject(String)
     * @scenario An Option is added with a short ("Z") and long ("zulu") identifier and a
     *           single value, then queried by its long name.
     * @defectRisk getOptionObject(String) resolves the value via getOptionValue(opt) (which
     *             internally consults the names map), but then re-verifies presence using
     *             options.containsKey(opt) against the RAW, unresolved key. When queried
     *             via the long name, this raw containsKey check fails (the options map is
     *             keyed by the short opt only), causing the method to incorrectly return
     *             null despite a validly resolved value -- another facet of the CLI-13
     *             key-resolution defect.
     */
    @Test(timeout = 4000)
    public void testGetOptionObjectLongNameResolutionDefect()
    {
        Option opt = new Option("Z", "zulu", true, "zulu option");
        opt.addValue("99");
        cmd.addOption(opt);

        // sanity: short-key lookup works
        assertNotNull(cmd.getOptionObject("Z"));

        // long-name lookup should also resolve to the same value per specification
        assertNotNull("getOptionObject should resolve via long option name", cmd.getOptionObject("zulu"));
    }

    /**
     * @target CommandLine#hasOption(char)
     * @scenario Option registered under short key 'v'; query existing and non-existing
     *           characters/strings.
     * @defectRisk Baseline sanity check confirming char overload correctly delegates to the
     *             String overload for both hit and miss cases.
     */
    @Test(timeout = 4000)
    public void testHasOptionChar()
    {
        Option verbose = new Option("v", false, "verbose");
        cmd.addOption(verbose);

        assertTrue(cmd.hasOption('v'));
        assertFalse(cmd.hasOption('x'));
        assertFalse(cmd.hasOption("nonexistent"));
    }

    /**
     * @target CommandLine#getOptionValue(String), #getOptionValue(char), #getOptionValue(String)-longAlias
     * @scenario Single-argument option added with a value; retrieved via short-string,
     *           char, and long-name accessors.
     * @defectRisk Ensures the primary value-retrieval path functions for a simple,
     *             single-valued option across all supported query forms.
     */
    @Test(timeout = 4000)
    public void testGetOptionValueSingleArgument()
    {
        Option opt = new Option("f", "file", true, "file name");
        opt.addValue("input.txt");
        cmd.addOption(opt);

        assertEquals("input.txt", cmd.getOptionValue("f"));
        assertEquals("input.txt", cmd.getOptionValue('f'));
        assertEquals("input.txt", cmd.getOptionValue("file"));
    }

    /**
     * @target CommandLine#getOptionValue(String, String), #getOptionValue(char, String)
     * @scenario Option is not present in the command line at all.
     * @defectRisk Verifies the default-value branch is returned correctly when the option
     *             is entirely missing (answer == null path).
     */
    @Test(timeout = 4000)
    public void testGetOptionValueDefaultWhenMissing()
    {
        assertEquals("default", cmd.getOptionValue("missing", "default"));
        assertEquals("default", cmd.getOptionValue('m', "default"));
    }

    /**
     * @target CommandLine#getOptionValue(String, String), #getOptionValue(char, String)
     * @scenario Option is present and has an explicit value set.
     * @defectRisk Verifies the actual value (not the default) is returned when the option
     *             is present (answer != null branch).
     */
    @Test(timeout = 4000)
    public void testGetOptionValueDefaultWhenPresent()
    {
        Option opt = new Option("p", false, "present flag with arg");
        opt.setArgs(1);
        opt.addValue("actual");
        cmd.addOption(opt);

        assertEquals("actual", cmd.getOptionValue("p", "default"));
        assertEquals("actual", cmd.getOptionValue('p', "default"));
    }

    /**
     * @target CommandLine#getOptionValues(String), #getOptionValues(char)
     * @scenario Option configured to accept multiple arguments (3) and populated with
     *           three distinct values; queried via short key, char, and long key.
     * @defectRisk Ensures multi-value retrieval preserves ordering and length, and that
     *             short/char/long-key resolution paths remain consistent.
     */
    @Test(timeout = 4000)
    public void testGetOptionValuesMultiple()
    {
        Option opt = new Option("l", "list", true, "multi value option");
        opt.setArgs(3);
        opt.addValue("a");
        opt.addValue("b");
        opt.addValue("c");
        cmd.addOption(opt);

        String[] values = cmd.getOptionValues("l");
        assertNotNull(values);
        assertEquals(3, values.length);
        assertEquals("a", values[0]);
        assertEquals("b", values[1]);
        assertEquals("c", values[2]);

        String[] valuesChar = cmd.getOptionValues('l');
        assertArrayEquals(values, valuesChar);

        String[] valuesLong = cmd.getOptionValues("list");
        assertArrayEquals(values, valuesLong);
    }

    /**
     * @target CommandLine#getOptionValues(String), #getOptionValues(char), #getOptionValue(String)
     * @scenario No option has been added at all; all queries reference a non-existent key.
     * @defectRisk Confirms the "return null" branch (key not found in options map) is
     *             exercised correctly for both string and char accessors.
     */
    @Test(timeout = 4000)
    public void testGetOptionValuesMissingReturnsNull()
    {
        assertNull(cmd.getOptionValues("missing"));
        assertNull(cmd.getOptionValues('m'));
        assertNull(cmd.getOptionValue("missing"));
    }

    /**
     * @target CommandLine#getOptionObject(String), #getOptionObject(char)
     * @scenario Queries for options that were never added to the command line.
     * @defectRisk Confirms the early "!options.containsKey(opt) -> return null" branch is
     *             correctly triggered when no matching option exists at all.
     */
    @Test(timeout = 4000)
    public void testGetOptionObjectMissingReturnsNull()
    {
        assertNull(cmd.getOptionObject("missing"));
        assertNull(cmd.getOptionObject('m'));
    }

    /**
     * @target CommandLine#getOptionObject(String), #getOptionObject(char)
     * @scenario Option added and queried via its exact short key (the literal key stored
     *           in the internal options map), which should always succeed regardless of
     *           the CLI-13 defect.
     * @defectRisk Baseline positive-path check ensuring type conversion machinery returns a
     *             non-null object when the raw containsKey check succeeds.
     */
    @Test(timeout = 4000)
    public void testGetOptionObjectPresentViaShortKey()
    {
        Option opt = new Option("n", "number", true, "a value");
        opt.addValue("42");
        cmd.addOption(opt);

        Object result = cmd.getOptionObject("n");
        assertNotNull(result);
        assertEquals("42", result.toString());

        Object resultChar = cmd.getOptionObject('n');
        assertNotNull(resultChar);
    }

    /**
     * @target CommandLine#addArg(String), #getArgs(), #getArgList()
     * @scenario Several unrecognized arguments are added sequentially; verify both array
     *           and list accessors reflect the same ordered content, and that an empty
     *           CommandLine yields empty collections.
     * @defectRisk Ensures list-to-array conversion in getArgs() preserves insertion order
     *             and size, and that getArgList() exposes the live backing list.
     */
    @Test(timeout = 4000)
    public void testAddArgAndGetArgs()
    {
        assertEquals(0, cmd.getArgs().length);
        assertTrue(cmd.getArgList().isEmpty());

        cmd.addArg("arg1");
        cmd.addArg("arg2");
        cmd.addArg("arg3");

        String[] args = cmd.getArgs();
        assertEquals(3, args.length);
        assertEquals("arg1", args[0]);
        assertEquals("arg2", args[1]);
        assertEquals("arg3", args[2]);

        List argList = cmd.getArgList();
        assertEquals(3, argList.size());
        assertEquals("arg1", argList.get(0));
        assertEquals("arg3", argList.get(2));
    }

    /**
     * @target CommandLine#addOption(Option), #getOptions(), #iterator()
     * @scenario Three distinct options (each with unique short/long keys) are added; both
     *           getOptions() and iterator() should report exactly three entries.
     * @defectRisk Confirms the options map and hashcodeMap remain in sync (same count) for
     *             normal, non-colliding option registrations.
     */
    @Test(timeout = 4000)
    public void testGetOptionsAndIterator()
    {
        Option a = new Option("a", "alpha", false, "alpha flag");
        Option b = new Option("b", "beta", false, "beta flag");
        Option c = new Option("c", "gamma", false, "gamma flag");

        cmd.addOption(a);
        cmd.addOption(b);
        cmd.addOption(c);

        Option[] options = cmd.getOptions();
        assertEquals(3, options.length);

        Iterator it = cmd.iterator();
        int count = 0;
        while (it.hasNext())
        {
            Object o = it.next();
            assertTrue(o instanceof Option);
            count++;
        }
        assertEquals(3, count);
    }

    /**
     * @target CommandLine#getOptions(), #iterator(), #getArgs(), #hasOption(String)
     * @scenario A freshly constructed CommandLine with no options and no arguments added.
     * @defectRisk Verifies all query methods behave gracefully (empty collections, false
     *             results) on an entirely empty state, guarding against NPEs on empty maps.
     */
    @Test(timeout = 4000)
    public void testEmptyCommandLine()
    {
        assertEquals(0, cmd.getOptions().length);
        assertFalse(cmd.iterator().hasNext());
        assertEquals(0, cmd.getArgs().length);
        assertFalse(cmd.hasOption("anything"));
    }

    /**
     * @target CommandLine#addOption(Option) - key==null branch (opt.getKey() null)
     * @scenario An Option is created with a null short opt and only a long opt ("verbose").
     *           addOption() must fall back to using the long opt as the storage key.
     * @defectRisk Exercises the "if (key == null) { key = opt.getLongOpt(); }" branch in
     *             addOption, ensuring long-only options are still stored and retrievable.
     */
    @Test(timeout = 4000)
    public void testAddOptionWithLongOptOnlyKeyResolution()
    {
        Option longOnly = new Option(null, "verbose", false, "verbose flag");
        cmd.addOption(longOnly);

        // key resolves to the long option name since short opt is null
        assertTrue(cmd.hasOption("verbose"));
        assertNull(cmd.getOptionValue("verbose"));

        Option[] options = cmd.getOptions();
        assertEquals(1, options.length);
    }

    /**
     * @target CommandLine#addOption(Option) - names.put(longOpt, key) branch with null longOpt
     * @scenario Two options are added, each having a short key only (no long opt), which
     *           causes the internal names map to receive a null-keyed entry on each call.
     * @defectRisk Confirms that repeatedly overwriting a null-keyed entry in the names map
     *             does not corrupt state for unrelated, independently-keyed options.
     */
    @Test(timeout = 4000)
    public void testAddOptionWithShortOnlyNoLongOpt()
    {
        Option shortOnly = new Option("s", false, "short only flag");
        cmd.addOption(shortOnly);

        assertTrue(cmd.hasOption("s"));
        assertNull(cmd.getOptionValue("s"));

        Option shortOnly2 = new Option("t", false, "second short only flag");
        cmd.addOption(shortOnly2);

        assertTrue(cmd.hasOption("s"));
        assertTrue(cmd.hasOption("t"));
        assertEquals(2, cmd.getOptions().length);
    }

    /**
     * @target CommandLine#getOptionValue(String), #getOptionValues(String), Util.stripLeadingHyphens
     * @scenario Option has only a short key (no long opt) and a single value; queried using
     *           a hyphen-prefixed form of the short key.
     * @defectRisk Confirms hyphen-stripping logic inside getOptionValue/getOptionValues
     *             functions correctly even when no long-opt alias exists in the names map.
     */
    @Test(timeout = 4000)
    public void testGetOptionValuesHyphenStrippingWithoutLongOpt()
    {
        Option opt = new Option("s", true, "value option");
        opt.addValue("hello");
        cmd.addOption(opt);

        assertEquals("hello", cmd.getOptionValue("-s"));
        assertArrayEquals(new String[] {"hello"}, cmd.getOptionValues("-s"));
    }

    /**
     * @target CommandLine#getOptionValue(char), #getOptionValues(char)
     * @scenario Char-based accessors delegate to their String-based counterparts; verify
     *           correctness for a populated, single-value option.
     * @defectRisk Baseline check that char overloads correctly stringify the character key
     *             before delegating.
     */
    @Test(timeout = 4000)
    public void testCharOverloadsDelegateCorrectly()
    {
        Option opt = new Option("q", "query", true, "query option");
        opt.addValue("value1");
        cmd.addOption(opt);

        assertEquals("value1", cmd.getOptionValue('q'));
        assertArrayEquals(new String[] {"value1"}, cmd.getOptionValues('q'));
        assertTrue(cmd.hasOption('q'));
    }
}