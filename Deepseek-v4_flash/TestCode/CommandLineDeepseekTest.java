package org.apache.commons.cli;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for CommandLine class.
 * Targets high line/branch coverage and the known Defects4J defect (BugCLI13).
 */
public class CommandLineDeepseekTest {

    private CommandLine cmd;

    @Before
    public void setUp() {
        cmd = new CommandLine();
    }

    // -----------------------------------------------------------------------
    // Tests for hasOption(String)
    // -----------------------------------------------------------------------

    /**
     * @target hasOption(String)
     * @scenario Option with both short and long names added, queried with short name.
     * @defectRisk Missing mapping from long name to short name in hasOption.
     */
    @Test(timeout = 4000)
    public void testHasOption_shortOptReturnsTrue() {
        Option opt = new Option("f", "file", false, "a file");
        cmd.addOption(opt);
        assertTrue("hasOption should return true for short option", cmd.hasOption("f"));
    }

    /**
     * @target hasOption(String)
     * @scenario Option with both short and long names added, queried with long name.
     * @defectRisk hasOption does not consult names map, so long option returns false (defective behaviour).
     */
    @Test(timeout = 4000)
    public void testHasOption_longOptShouldReturnTrue_DEFECT() {
        Option opt = new Option("f", "file", false, "a file");
        cmd.addOption(opt);
        // According to expected specification, hasOption should recognise long name.
        // Current implementation returns false -> this test will fail on defective version.
        assertTrue("hasOption should return true for long option", cmd.hasOption("file"));
    }

    /**
     * @target hasOption(String)
     * @scenario Option only with long name added, queried with long name.
     * @defectRisk No short name defined, long name is stored directly in options map -> should work.
     */
    @Test(timeout = 4000)
    public void testHasOption_longOnlyWorks() {
        Option opt = new Option(null, "verbose", false, "be verbose");
        cmd.addOption(opt);
        assertTrue("hasOption should work for long-only option", cmd.hasOption("verbose"));
    }

    /**
     * @target hasOption(String)
     * @scenario Option not present.
     * @defectRisk Incorrect return for absent option.
     */
    @Test(timeout = 4000)
    public void testHasOption_absentOptionReturnsFalse() {
        assertFalse("hasOption should return false for absent option", cmd.hasOption("x"));
    }

    // -----------------------------------------------------------------------
    // Tests for hasOption(char)
    // -----------------------------------------------------------------------

    /**
     * @target hasOption(char)
     * @scenario Short option added, queried with char.
     * @defectRisk Delegates to hasOption(String) - same potential defect.
     */
    @Test(timeout = 4000)
    public void testHasOption_charShortOpt() {
        Option opt = new Option("a", null, false, "alpha");
        cmd.addOption(opt);
        assertTrue("hasOption(char) should return true for 'a'", cmd.hasOption('a'));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValue(String)
    // -----------------------------------------------------------------------

    /**
     * @target getOptionValue(String)
     * @scenario Option with a single value, retrieved with short name.
     * @defectRisk Value not returned correctly due to key resolution.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_shortOpt() {
        Option opt = new Option("o", "opt", true, "option with arg");
        opt.addValue("myvalue");
        cmd.addOption(opt);
        assertEquals("Should retrieve value via short name", "myvalue", cmd.getOptionValue("o"));
    }

    /**
     * @target getOptionValue(String)
     * @scenario Option with a single value, retrieved with long name.
     * @defectRisk Names map incorrectly resolved, may return null.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_longOpt() {
        Option opt = new Option("o", "opt", true, "option with arg");
        opt.addValue("myvalue");
        cmd.addOption(opt);
        assertEquals("Should retrieve value via long name", "myvalue", cmd.getOptionValue("opt"));
    }

    /**
     * @target getOptionValue(String)
     * @scenario Option with hyphens in query (e.g., "--opt"), should still work.
     * @defectRisk Util.stripLeadingHyphens not applied consistently.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_withHyphens() {
        Option opt = new Option("o", "opt", true, "option with arg");
        opt.addValue("value");
        cmd.addOption(opt);
        assertEquals("Hyphenated long opt should work", "value", cmd.getOptionValue("--opt"));
        assertEquals("Single hyphen short opt should work", "value", cmd.getOptionValue("-o"));
    }

    /**
     * @target getOptionValue(String)
     * @scenario Option absent -> null.
     * @defectRisk Returns null for missing option.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_absentReturnsNull() {
        assertNull("Should return null for absent option", cmd.getOptionValue("nonexistent"));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValue(char)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetOptionValue_char() {
        cmd.addOption(createOption("v", "verbose", true, "verbose value"));
        assertEquals("char overload should work", "verbose value", cmd.getOptionValue('v'));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValue(String, String) overload with default
    // -----------------------------------------------------------------------

    /**
     * @target getOptionValue(String, String)
     * @scenario Option exists -> returns value, not default.
     * @defectRisk Default returned incorrectly.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_withDefault_optionPresent() {
        cmd.addOption(createOption("a", null, true, "avalue"));
        assertEquals("Should return option value, not default", "avalue", cmd.getOptionValue("a", "default"));
    }

    /**
     * @target getOptionValue(String, String)
     * @scenario Option absent -> returns default.
     * @defectRisk Default not returned.
     */
    @Test(timeout = 4000)
    public void testGetOptionValue_withDefault_optionAbsent() {
        assertEquals("Should return default for absent option", "default", cmd.getOptionValue("missing", "default"));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValue(char, String)
    // -----------------------------------------------------------------------
    @Test(timeout = 4000)
    public void testGetOptionValue_charWithDefault() {
        assertEquals("char overload with default", "fallback", cmd.getOptionValue('z', "fallback"));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValues(String)
    // -----------------------------------------------------------------------

    /**
     * @target getOptionValues(String)
     * @scenario Option with multiple values.
     * @defectRisk Array not properly returned or null.
     */
    @Test(timeout = 4000)
    public void testGetOptionValues_multipleValues() {
        Option opt = new Option("m", "multi", true, "multi-valued");
        opt.addValue("v1");
        opt.addValue("v2");
        cmd.addOption(opt);
        String[] values = cmd.getOptionValues("m");
        assertNotNull("Should not be null", values);
        assertEquals("Should have 2 values", 2, values.length);
        assertEquals("v1", values[0]);
        assertEquals("v2", values[1]);
    }

    /**
     * @target getOptionValues(String)
     * @scenario Option with no values.
     * @defectRisk Returns null or empty array? Current implementation: if option has no values, getValues() returns null.
     */
    @Test(timeout = 4000)
    public void testGetOptionValues_noValues() {
        Option opt = new Option("n", "none", false, "no args");
        cmd.addOption(opt);
        assertNull("Should return null for option without values", cmd.getOptionValues("n"));
    }

    /**
     * @target getOptionValues(String)
     * @scenario Option absent.
     * @defectRisk Null returned.
     */
    @Test(timeout = 4000)
    public void testGetOptionValues_absent() {
        assertNull("Should return null for absent option", cmd.getOptionValues("absent"));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionValues(char)
    // -----------------------------------------------------------------------
    @Test(timeout = 4000)
    public void testGetOptionValues_char() {
        cmd.addOption(createOption("d", null, true, "data"));
        String[] vals = cmd.getOptionValues('d');
        assertNotNull(vals);
        assertEquals(1, vals.length);
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionObject(String)
    // -----------------------------------------------------------------------

    /**
     * @target getOptionObject(String)
     * @scenario Option exists, type not set -> returns string.
     * @defectRisk Null returned due to inconsistent key check.
     */
    @Test(timeout = 4000)
    public void testGetOptionObject_existingOption() {
        Option opt = new Option("i", "intopt", true, "integer option");
        opt.addValue("42");
        cmd.addOption(opt);
        Object obj = cmd.getOptionObject("i");
        assertEquals("Should return the value as string", "42", obj);
    }

    /**
     * @target getOptionObject(String)
     * @scenario Option absent.
     * @defectRisk Should return null.
     */
    @Test(timeout = 4000)
    public void testGetOptionObject_absent() {
        assertNull("Should return null for absent option", cmd.getOptionObject("absent"));
    }

    /**
     * @target getOptionObject(String)
     * @scenario When option exists but value is null, returns null.
     * @defectRisk Inconsistent with getOptionValue.
     */
    @Test(timeout = 4000)
    public void testGetOptionObject_nullValue() {
        Option opt = new Option("e", "empty", false, "no arg");
        cmd.addOption(opt);
        assertNull("Should return null because no value", cmd.getOptionObject("e"));
    }

    // -----------------------------------------------------------------------
    // Tests for getOptionObject(char)
    // -----------------------------------------------------------------------
    @Test(timeout = 4000)
    public void testGetOptionObject_char() {
        cmd.addOption(createOption("c", null, true, "charobj"));
        assertNotNull(cmd.getOptionObject('c'));
    }

    // -----------------------------------------------------------------------
    // Tests for getArgs() and getArgList()
    // -----------------------------------------------------------------------

    /**
     * @target getArgs()
     * @scenario No args added -> empty array.
     * @defectRisk Incorrect array creation.
     */
    @Test(timeout = 4000)
    public void testGetArgs_empty() {
        assertEquals("Should have 0 args", 0, cmd.getArgs().length);
    }

    /**
     * @target getArgs()
     * @scenario Multiple args added.
     * @defectRisk Array not containing all args.
     */
    @Test(timeout = 4000)
    public void testGetArgs_withArgs() {
        cmd.addArg("arg1");
        cmd.addArg("arg2");
        cmd.addArg("arg3");
        String[] args = cmd.getArgs();
        assertEquals(3, args.length);
        assertEquals("arg1", args[0]);
        assertEquals("arg2", args[1]);
        assertEquals("arg3", args[2]);
    }

    /**
     * @target getArgList()
     * @scenario Same as getArgs but returns List.
     */
    @Test(timeout = 4000)
    public void testGetArgList() {
        cmd.addArg("first");
        assertNotNull(cmd.getArgList());
        assertEquals(1, cmd.getArgList().size());
        assertEquals("first", cmd.getArgList().get(0));
    }

    // -----------------------------------------------------------------------
    // Tests for iterator()
    // -----------------------------------------------------------------------

    /**
     * @target iterator()
     * @scenario No options added -> empty iterator.
     * @defectRisk NPE or infinite loop.
     */
    @Test(timeout = 4000)
    public void testIterator_empty() {
        assertFalse("Iterator should have no elements", cmd.iterator().hasNext());
    }

    /**
     * @target iterator()
     * @scenario Options added -> iterator returns them.
     */
    @Test(timeout = 4000)
    public void testIterator_withOptions() {
        Option o1 = new Option("a", null, false, "");
        Option o2 = new Option("b", null, false, "");
        cmd.addOption(o1);
        cmd.addOption(o2);
        int count = 0;
        for (java.util.Iterator it = cmd.iterator(); it.hasNext(); ) {
            assertNotNull(it.next());
            count++;
        }
        assertEquals("Should have 2 options", 2, count);
    }

    // -----------------------------------------------------------------------
    // Tests for getOptions()
    // -----------------------------------------------------------------------

    /**
     * @target getOptions()
     * @scenario No options -> empty array.
     * @defectRisk Null returned or incorrect length.
     */
    @Test(timeout = 4000)
    public void testGetOptions_empty() {
        assertEquals(0, cmd.getOptions().length);
    }

    /**
     * @target getOptions()
     * @scenario Multiple options added.
     */
    @Test(timeout = 4000)
    public void testGetOptions_withOptions() {
        cmd.addOption(new Option("x", null, false, ""));
        cmd.addOption(new Option("y", null, false, ""));
        assertEquals("Should have 2 options", 2, cmd.getOptions().length);
    }

    // -----------------------------------------------------------------------
    // Dedicated test for Defects4J issue BugCLI13
    // This test exercises option resolution, hyphens, and consistency across
    // hasOption, getOptionValue, getOptionValues.
    // -----------------------------------------------------------------------
    /**
     * @target Combined: hasOption, getOptionValue, getOptionValues
     * @scenario Option with short 'f' and long 'file' added with a value.
     *          Query via short, long, with/without hyphens.
     *          Also verify consistency across hasOption and getOptionValue.
     * @defectRisk BugCLI13: possible inconsistency in key mapping causing
     *             hasOption to return false for long name or getOptionValue
     *             to return null despite option present.
     */
    @Test(timeout = 4000)
    public void testCLI13_OptionResolutionAndValueRetrieval() {
        Option opt = new Option("f", "file", true, "input file");
        opt.addValue("data.txt");
        cmd.addOption(opt);

        // hasOption
        assertTrue("hasOption('f')", cmd.hasOption("f"));
        // This assertion is expected to pass if the bug is fixed, but fail on defective version.
        // The bug is that hasOption does not resolve long names.
        assertTrue("hasOption('file') - should be true", cmd.hasOption("file"));

        // getOptionValue
        assertEquals("getOptionValue('f')", "data.txt", cmd.getOptionValue("f"));
        assertEquals("getOptionValue('file')", "data.txt", cmd.getOptionValue("file"));
        assertEquals("getOptionValue('--file')", "data.txt", cmd.getOptionValue("--file"));
        assertEquals("getOptionValue('-f')", "data.txt", cmd.getOptionValue("-f"));

        // getOptionValues
        String[] values = cmd.getOptionValues("f");
        assertNotNull("getOptionValues('f') should not be null", values);
        assertEquals("getOptionValues('f') length", 1, values.length);
        assertEquals("getOptionValues('f')[0]", "data.txt", values[0]);

        values = cmd.getOptionValues("file");
        assertNotNull("getOptionValues('file') should not be null", values);
        assertEquals("getOptionValues('file') length", 1, values.length);
        assertEquals("getOptionValues('file')[0]", "data.txt", values[0]);

        // Also verify that option with only long name works
        Option longOnly = new Option(null, "verbose", false, "be verbose");
        cmd.addOption(longOnly);
        assertTrue("hasOption('verbose') for long-only", cmd.hasOption("verbose"));
        assertNull("getOptionValue('verbose')", cmd.getOptionValue("verbose"));
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------
    private Option createOption(String shortOpt, String longOpt, boolean hasArg, String value) {
        Option opt = new Option(shortOpt, longOpt, hasArg, "");
        if (hasArg && value != null) {
            opt.addValue(value);
        }
        return opt;
    }
}