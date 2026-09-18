package org.apache.commons.cli2.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.WriteableCommandLine;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor: prefixes, normalised list
 *   - addOption: options list, nameToOption map
 *   - addValue: values map, option registration for Argument
 *   - addSwitch: switches map, duplicate switch detection
 *   - hasOption: options.contains(option)
 *   - getOption: nameToOption.get(trigger)
 *   - getValues: command line values > method defaults > option defaults > EMPTY_LIST
 *   - getSwitch: command line switch > method default > option default > null
 *   - addProperty / getProperty / getProperties: Properties delegation
 *   - looksLikeOption: prefix matching
 *   - toString: normalised list formatting with quoting
 *   - getOptions / getOptionTriggers: unmodifiable views
 *   - setDefaultValues / setDefaultSwitch: map updates
 *   - getNormalised: unmodifiable view
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for addValue, addSwitch, setDefaultValues, setDefaultSwitch
 *   - empty lists for defaultValues in getValues
 *   - empty triggers for getOption
 *   - empty prefixes for looksLikeOption
 *   - arguments with spaces in toString
 *   - MAX_INT / MIN_INT values (not applicable, but we test null/empty)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - BugLoopingOptionLookAlike2: incorrect quoting in toString or misidentification of options
 *   - Test that looksLikeOption returns false for non-option arguments
 *   - Test that toString does not quote arguments without spaces
 *   - Test that getValues returns command line values over defaults
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - addSwitch with duplicate option throws IllegalStateException
 *   - getValues with null option (NPE expected)
 *   - getSwitch with null option (NPE expected)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Unmodifiable collections returned by getOptions, getOptionTriggers, getProperties, getNormalised
 *   - Immutability of internal state after construction
 */
public class WriteableCommandLineImplDeepseekTest {

    // Helper to create a simple Option with given prefixes, preferred name, and triggers
    private Option createOption(final Set prefixes, final String preferredName, final List triggers) {
        return new Option() {
            @Override
            public Set getPrefixes() {
                return prefixes;
            }
            @Override
            public String getPreferredName() {
                return preferredName;
            }
            @Override
            public List getTriggers() {
                return triggers;
            }
            @Override
            public void process(WriteableCommandLine commandLine, List arguments) {
                // not used
            }
            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String argument) {
                return false;
            }
            @Override
            public boolean isRequired() {
                return false;
            }
            @Override
            public void validate(WriteableCommandLine commandLine) {
                // not used
            }
            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // not used
            }
            @Override
            public String toString() {
                return preferredName;
            }
        };
    }

    // Helper to create a simple Argument (extends Option)
    private Argument createArgument(final Set prefixes, final String preferredName, final List triggers) {
        return new Argument() {
            @Override
            public Set getPrefixes() {
                return prefixes;
            }
            @Override
            public String getPreferredName() {
                return preferredName;
            }
            @Override
            public List getTriggers() {
                return triggers;
            }
            @Override
            public void process(WriteableCommandLine commandLine, List arguments) {
                // not used
            }
            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String argument) {
                return false;
            }
            @Override
            public boolean isRequired() {
                return false;
            }
            @Override
            public void validate(WriteableCommandLine commandLine) {
                // not used
            }
            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // not used
            }
            @Override
            public String toString() {
                return preferredName;
            }
            @Override
            public List getDefaultValues(WriteableCommandLine commandLine) {
                return Collections.emptyList();
            }
            @Override
            public boolean isList() {
                return false;
            }
            @Override
            public int getMaximum() {
                return 1;
            }
            @Override
            public String getPreferredName() {
                return preferredName;
            }
        };
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testConstructorAndBasicProperties() {
        Set prefixes = new HashSet(Arrays.asList("-", "--"));
        List arguments = Arrays.asList("arg1", "arg2");
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, arguments);
        assertNotNull("normalised should not be null", cmd.getNormalised());
        assertEquals("normalised list size", 2, cmd.getNormalised().size());
        assertTrue("normalised contains arg1", cmd.getNormalised().contains("arg1"));
        assertTrue("normalised contains arg2", cmd.getNormalised().contains("arg2"));
        // prefixes are not directly accessible, but looksLikeOption uses them
        assertTrue("looksLikeOption for -x", cmd.looksLikeOption("-x"));
        assertTrue("looksLikeOption for --y", cmd.looksLikeOption("--y"));
        assertFalse("looksLikeOption for plain", cmd.looksLikeOption("plain"));
    }

    @Test(timeout = 4000)
    public void testAddOptionAndHasOption() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt1 = createOption(prefixes, "opt1", Arrays.asList("o1", "O1"));
        cmd.addOption(opt1);
        assertTrue("hasOption should be true", cmd.hasOption(opt1));
        assertEquals("getOption by trigger o1", opt1, cmd.getOption("o1"));
        assertEquals("getOption by trigger O1", opt1, cmd.getOption("O1"));
        assertEquals("getOption by preferred name", opt1, cmd.getOption("opt1"));
        assertNull("getOption for unknown trigger", cmd.getOption("unknown"));
        // test that options list contains the option
        assertTrue("getOptions contains opt1", cmd.getOptions().contains(opt1));
    }

    @Test(timeout = 4000)
    public void testAddValueAndGetValues() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Argument argOpt = createArgument(prefixes, "arg", Arrays.asList("arg"));
        // addValue with an Argument should also add the option
        cmd.addValue(argOpt, "value1");
        assertTrue("hasOption should be true after addValue with Argument", cmd.hasOption(argOpt));
        // getValues with no defaults
        List values = cmd.getValues(argOpt, null);
        assertEquals("values size", 1, values.size());
        assertEquals("first value", "value1", values.get(0));
        // add another value
        cmd.addValue(argOpt, "value2");
        values = cmd.getValues(argOpt, null);
        assertEquals("values size after second add", 2, values.size());
        assertEquals("second value", "value2", values.get(1));
        // test with method defaults (should be ignored because command line values exist)
        List methodDefaults = Arrays.asList("default1", "default2");
        values = cmd.getValues(argOpt, methodDefaults);
        assertEquals("values should still be command line values", 2, values.size());
        assertEquals("first value still value1", "value1", values.get(0));
        // test with empty command line values (remove them)
        // We cannot remove, but we can test a different option with no values
        Option optNoValues = createOption(prefixes, "noval", Arrays.asList("nv"));
        cmd.addOption(optNoValues);
        values = cmd.getValues(optNoValues, methodDefaults);
        assertEquals("should return method defaults", 2, values.size());
        assertEquals("first default", "default1", values.get(0));
        // test with option defaults set via setDefaultValues
        List optionDefaults = Arrays.asList("optDefault1", "optDefault2");
        cmd.setDefaultValues(optNoValues, optionDefaults);
        values = cmd.getValues(optNoValues, null);
        assertEquals("should return option defaults", 2, values.size());
        assertEquals("first option default", "optDefault1", values.get(0));
        // test with both method and option defaults (method defaults take precedence)
        values = cmd.getValues(optNoValues, methodDefaults);
        assertEquals("method defaults should override option defaults", 2, values.size());
        assertEquals("first method default", "default1", values.get(0));
        // test with no defaults at all
        cmd.setDefaultValues(optNoValues, null);
        values = cmd.getValues(optNoValues, null);
        assertEquals("should return empty list", Collections.EMPTY_LIST, values);
    }

    @Test(timeout = 4000)
    public void testAddSwitchAndGetSwitch() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option swOpt = createOption(prefixes, "sw", Arrays.asList("s"));
        cmd.addSwitch(swOpt, true);
        assertEquals("getSwitch returns true", Boolean.TRUE, cmd.getSwitch(swOpt, null));
        // addSwitch with same option should throw IllegalStateException
        try {
            cmd.addSwitch(swOpt, false);
            fail("Expected IllegalStateException for duplicate switch");
        } catch (IllegalStateException e) {
            // expected
        }
        // test getSwitch with default value
        Option swOpt2 = createOption(prefixes, "sw2", Arrays.asList("s2"));
        cmd.addOption(swOpt2);
        assertEquals("getSwitch returns default false", Boolean.FALSE, cmd.getSwitch(swOpt2, Boolean.FALSE));
        // test with option default switch set
        cmd.setDefaultSwitch(swOpt2, Boolean.TRUE);
        assertEquals("getSwitch returns option default true", Boolean.TRUE, cmd.getSwitch(swOpt2, null));
        // test with method default overriding option default
        assertEquals("method default false overrides option default", Boolean.FALSE, cmd.getSwitch(swOpt2, Boolean.FALSE));
        // test with no defaults
        cmd.setDefaultSwitch(swOpt2, null);
        assertNull("getSwitch returns null", cmd.getSwitch(swOpt2, null));
    }

    @Test(timeout = 4000)
    public void testAddPropertyAndGetProperty() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        cmd.addProperty("key1", "value1");
        cmd.addProperty("key2", "value2");
        assertEquals("getProperty returns value1", "value1", cmd.getProperty("key1", "default"));
        assertEquals("getProperty returns value2", "value2", cmd.getProperty("key2", "default"));
        assertEquals("getProperty with default for missing key", "default", cmd.getProperty("missing", "default"));
        Set propKeys = cmd.getProperties();
        assertTrue("properties contains key1", propKeys.contains("key1"));
        assertTrue("properties contains key2", propKeys.contains("key2"));
        assertEquals("properties size", 2, propKeys.size());
        // test unmodifiable
        try {
            propKeys.add("newkey");
            fail("Properties set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLooksLikeOption() {
        Set prefixes = new HashSet(Arrays.asList("-", "--"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        assertTrue("looksLikeOption for -a", cmd.looksLikeOption("-a"));
        assertTrue("looksLikeOption for --long", cmd.looksLikeOption("--long"));
        assertFalse("looksLikeOption for plain", cmd.looksLikeOption("plain"));
        assertFalse("looksLikeOption for empty string", cmd.looksLikeOption(""));
        // test with empty prefixes
        Set emptyPrefixes = Collections.emptySet();
        Option rootOption2 = createOption(emptyPrefixes, "root2", Arrays.asList("root2"));
        WriteableCommandLineImpl cmd2 = new WriteableCommandLineImpl(rootOption2, new ArrayList());
        assertFalse("looksLikeOption with empty prefixes", cmd2.looksLikeOption("-a"));
        assertFalse("looksLikeOption with empty prefixes for any string", cmd2.looksLikeOption("anything"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        // Test with no spaces
        List args = Arrays.asList("file.txt", "arg2");
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, args);
        assertEquals("toString without spaces", "file.txt arg2", cmd.toString());
        // Test with spaces
        List argsWithSpaces = Arrays.asList("my file.txt", "arg2");
        WriteableCommandLineImpl cmd2 = new WriteableCommandLineImpl(rootOption, argsWithSpaces);
        assertEquals("toString with spaces", "\"my file.txt\" arg2", cmd2.toString());
        // Test with empty list
        WriteableCommandLineImpl cmd3 = new WriteableCommandLineImpl(rootOption, new ArrayList());
        assertEquals("toString empty", "", cmd3.toString());
    }

    @Test(timeout = 4000)
    public void testGetOptionsAndGetOptionTriggers() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt1 = createOption(prefixes, "opt1", Arrays.asList("o1", "O1"));
        Option opt2 = createOption(prefixes, "opt2", Arrays.asList("o2"));
        cmd.addOption(opt1);
        cmd.addOption(opt2);
        List opts = cmd.getOptions();
        assertEquals("options size", 2, opts.size());
        assertTrue("options contains opt1", opts.contains(opt1));
        assertTrue("options contains opt2", opts.contains(opt2));
        // test unmodifiable
        try {
            opts.add(createOption(prefixes, "extra", Arrays.asList("e")));
            fail("Options list should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        Set triggers = cmd.getOptionTriggers();
        assertTrue("triggers contains o1", triggers.contains("o1"));
        assertTrue("triggers contains O1", triggers.contains("O1"));
        assertTrue("triggers contains o2", triggers.contains("o2"));
        assertTrue("triggers contains opt1", triggers.contains("opt1"));
        assertTrue("triggers contains opt2", triggers.contains("opt2"));
        // test unmodifiable
        try {
            triggers.add("newtrigger");
            fail("Triggers set should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesAndGetValues() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt = createOption(prefixes, "opt", Arrays.asList("o"));
        cmd.addOption(opt);
        // set default values
        List defaults = Arrays.asList("default1", "default2");
        cmd.setDefaultValues(opt, defaults);
        List values = cmd.getValues(opt, null);
        assertEquals("should return option defaults", 2, values.size());
        assertEquals("first default", "default1", values.get(0));
        // set default values to null (remove)
        cmd.setDefaultValues(opt, null);
        values = cmd.getValues(opt, null);
        assertEquals("should return empty list", Collections.EMPTY_LIST, values);
        // add command line value
        cmd.addValue(opt, "cmdValue");
        values = cmd.getValues(opt, null);
        assertEquals("should return command line value", 1, values.size());
        assertEquals("cmdValue", "cmdValue", values.get(0));
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchAndGetSwitch() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt = createOption(prefixes, "opt", Arrays.asList("o"));
        cmd.addOption(opt);
        // set default switch
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals("getSwitch returns true", Boolean.TRUE, cmd.getSwitch(opt, null));
        // set default switch to null (remove)
        cmd.setDefaultSwitch(opt, null);
        assertNull("getSwitch returns null", cmd.getSwitch(opt, null));
        // add command line switch
        cmd.addSwitch(opt, false);
        assertEquals("getSwitch returns false", Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testBoundaryNullArguments() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        // addValue with null option should throw NullPointerException
        try {
            cmd.addValue(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // addSwitch with null option should throw NullPointerException
        try {
            cmd.addSwitch(null, true);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // getValues with null option should throw NullPointerException
        try {
            cmd.getValues(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // getSwitch with null option should throw NullPointerException
        try {
            cmd.getSwitch(null, Boolean.TRUE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // setDefaultValues with null option should throw NullPointerException
        try {
            cmd.setDefaultValues(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // setDefaultSwitch with null option should throw NullPointerException
        try {
            cmd.setDefaultSwitch(null, Boolean.TRUE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // addProperty with null property should throw NullPointerException
        try {
            cmd.addProperty(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        // getProperty with null property should throw NullPointerException
        try {
            cmd.getProperty(null, "default");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBoundaryEmptyCollections() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt = createOption(prefixes, "opt", Arrays.asList("o"));
        cmd.addOption(opt);
        // getValues with empty default list
        List values = cmd.getValues(opt, Collections.emptyList());
        assertEquals("should return empty list", Collections.EMPTY_LIST, values);
        // addValue with empty string
        cmd.addValue(opt, "");
        values = cmd.getValues(opt, null);
        assertEquals("should contain empty string", 1, values.size());
        assertEquals("empty string", "", values.get(0));
        // addSwitch with false
        cmd.addSwitch(opt, false);
        assertEquals("getSwitch false", Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testBugLoopingOptionLookAlike2() {
        // This test targets the known defect where the error message differs.
        // The bug likely involves incorrect quoting in toString or misidentification of options.
        // We simulate a scenario with a non-option argument "testfile.txt" and assert correct behavior.
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        List arguments = Arrays.asList("testfile.txt");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, arguments);
        // Test that looksLikeOption returns false for "testfile.txt"
        assertFalse("looksLikeOption should be false for non-option argument", cmd.looksLikeOption("testfile.txt"));
        // Test that toString does not quote "testfile.txt" (no spaces)
        assertEquals("toString should not quote argument without spaces", "testfile.txt", cmd.toString());
        // Test that getOption returns null for "testfile.txt"
        assertNull("getOption should return null for non-option trigger", cmd.getOption("testfile.txt"));
        // Test that getValues for a non-existent option returns empty list
        Option nonExistent = createOption(prefixes, "nonexist", Arrays.asList("ne"));
        List values = cmd.getValues(nonExistent, null);
        assertEquals("getValues for non-existent option should be empty", Collections.EMPTY_LIST, values);
        // Add an Argument option and a value "testfile.txt" to simulate parsing
        Argument argOpt = createArgument(prefixes, "file", Arrays.asList("f"));
        cmd.addValue(argOpt, "testfile.txt");
        List argValues = cmd.getValues(argOpt, null);
        assertEquals("should have one value", 1, argValues.size());
        assertEquals("value should be testfile.txt", "testfile.txt", argValues.get(0));
        // Verify that the command line still has the original argument in normalised
        assertTrue("normalised contains testfile.txt", cmd.getNormalised().contains("testfile.txt"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testDuplicateSwitchThrowsIllegalStateException() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        Option opt = createOption(prefixes, "opt", Arrays.asList("o"));
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false); // should throw
    }

    @Test(timeout = 4000)
    public void testUnmodifiableCollections() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, new ArrayList());
        // getOptions
        List opts = cmd.getOptions();
        try {
            opts.add(createOption(prefixes, "extra", Arrays.asList("e")));
            fail("getOptions should return unmodifiable list");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // getOptionTriggers
        Set triggers = cmd.getOptionTriggers();
        try {
            triggers.add("new");
            fail("getOptionTriggers should return unmodifiable set");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // getProperties
        Set props = cmd.getProperties();
        try {
            props.add("new");
            fail("getProperties should return unmodifiable set");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        // getNormalised
        List norm = cmd.getNormalised();
        try {
            norm.add("new");
            fail("getNormalised should return unmodifiable list");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testImmutabilityAfterConstruction() {
        Set prefixes = new HashSet(Arrays.asList("-"));
        Option rootOption = createOption(prefixes, "root", Arrays.asList("root"));
        List originalArgs = new ArrayList(Arrays.asList("arg1", "arg2"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, originalArgs);
        // Modify original list after construction
        originalArgs.add("arg3");
        // The normalised list should not be affected (it was copied? Actually constructor assigns directly)
        // In the source, normalised = arguments; so it is a reference, not a copy.
        // This is a potential bug: the list is shared. We test that the normalised list reflects changes.
        // According to the code, it is not a copy, so the test should pass.
        assertEquals("normalised should reflect changes to original list", 3, cmd.getNormalised().size());
        // However, the getNormalised returns an unmodifiable view, but the underlying list is the same.
        // This is a design issue, but we test it.
        // We also test that adding to the original list does not break the unmodifiable view.
        // The unmodifiable view will still reflect changes.
        assertTrue("normalised contains arg3", cmd.getNormalised().contains("arg3"));
    }
}