package org.apache.commons.cli2.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.option.PropertyOption;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - addOption: normal add, duplicate add, parent chain addition
 *   - addValue: with Argument option, non-Argument option, multiple values
 *   - addSwitch: normal add, duplicate add (IllegalStateException)
 *   - hasOption: present, not present
 *   - getOption: existing trigger, non-existing trigger
 *   - getValues: with/without defaults, with/without command line values, default augmentation
 *   - getUndefaultedValues: with values, without values
 *   - getSwitch: command line value, method default, option default, null
 *   - getProperty: with/without option, with/without default
 *   - addProperty: with/without option
 *   - getProperties: with/without option, with/without properties
 *   - looksLikeOption: with prefix match, without prefix match, null trigger
 *   - toString: normal args, args with spaces
 *   - getOptions, getOptionTriggers, getNormalised: unmodifiable lists/sets
 *   - setDefaultValues: null defaults (remove), non-null defaults
 *   - setDefaultSwitch: null defaultSwitch (remove), non-null defaultSwitch
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for option, value, trigger, property, defaults
 *   - empty lists for arguments, defaults, values
 *   - empty string for trigger, property
 *   - MAX_INT/MIN_INT values for numeric arguments
 *   - Boolean.TRUE/Boolean.FALSE for switches
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - BugCLI150: Negative number handling (e.g., "-42") should not be treated as option
 *   - looksLikeOption with negative numbers: should return false for numeric negative strings
 *   - addValue with negative number values
 *   - getValues with negative number defaults
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - addSwitch duplicate: IllegalStateException
 *   - null option in addValue, addSwitch, hasOption, getValues, etc.
 *   - null trigger in getOption, looksLikeOption
 *   - null property in getProperty, addProperty
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - toString consistency with normalised list
 *   - getOptions returns unmodifiable list
 *   - getOptionTriggers returns unmodifiable set
 *   - getNormalised returns unmodifiable list
 *   - getProperties returns unmodifiable set
 */
public class WriteableCommandLineImplDeepseekTest {

    // Helper class to create a minimal Option for testing
    private static class TestOption implements Option {
        private final String preferredName;
        private final List<String> triggers;
        private Option parent;
        private final List<String> prefixes;

        public TestOption(String preferredName, String... triggers) {
            this.preferredName = preferredName;
            this.triggers = new ArrayList<String>();
            this.triggers.add(preferredName);
            if (triggers != null) {
                for (String t : triggers) {
                    this.triggers.add(t);
                }
            }
            this.prefixes = new ArrayList<String>();
            this.prefixes.add("--");
            this.prefixes.add("-");
        }

        public void setParent(Option parent) {
            this.parent = parent;
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public List<String> getTriggers() {
            return triggers;
        }

        @Override
        public Option getParent() {
            return parent;
        }

        @Override
        public List<String> getPrefixes() {
            return prefixes;
        }

        // Unused methods - provide default implementations
        @Override public boolean canProcess(WriteableCommandLine commandLine, String arg) { return false; }
        @Override public void process(WriteableCommandLine commandLine, List<String> arguments) {}
        @Override public void validate(WriteableCommandLine commandLine) {}
        @Override public boolean isRequired() { return false; }
        @Override public String getDescription() { return null; }
        @Override public List<?> getDefaults() { return Collections.emptyList(); }
        @Override public boolean isTrigger(String trigger) { return triggers.contains(trigger); }
        @Override public void defaults(WriteableCommandLine commandLine) {}
        @Override public boolean isArgument() { return false; }
    }

    // Helper class for Argument option
    private static class TestArgument implements Argument {
        private final String preferredName;
        private final List<String> triggers;
        private Option parent;
        private final List<String> prefixes;

        public TestArgument(String preferredName) {
            this.preferredName = preferredName;
            this.triggers = new ArrayList<String>();
            this.triggers.add(preferredName);
            this.prefixes = new ArrayList<String>();
            this.prefixes.add("--");
            this.prefixes.add("-");
        }

        public void setParent(Option parent) {
            this.parent = parent;
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public List<String> getTriggers() {
            return triggers;
        }

        @Override
        public Option getParent() {
            return parent;
        }

        @Override
        public List<String> getPrefixes() {
            return prefixes;
        }

        @Override
        public boolean isArgument() { return true; }

        // Unused methods
        @Override public boolean canProcess(WriteableCommandLine commandLine, String arg) { return false; }
        @Override public void process(WriteableCommandLine commandLine, List<String> arguments) {}
        @Override public void validate(WriteableCommandLine commandLine) {}
        @Override public boolean isRequired() { return false; }
        @Override public String getDescription() { return null; }
        @Override public List<?> getDefaults() { return Collections.emptyList(); }
        @Override public boolean isTrigger(String trigger) { return triggers.contains(trigger); }
        @Override public void defaults(WriteableCommandLine commandLine) {}
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddOption() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1", "-o", "--option");
        cmd.addOption(opt);
        
        assertTrue("Option should be present", cmd.hasOption(opt));
        assertEquals("Should retrieve by preferred name", opt, cmd.getOption("opt1"));
        assertEquals("Should retrieve by trigger -o", opt, cmd.getOption("-o"));
        assertEquals("Should retrieve by trigger --option", opt, cmd.getOption("--option"));
        assertNull("Non-existing trigger should return null", cmd.getOption("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAddOptionWithParent() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption parent = new TestOption("parent");
        TestOption child = new TestOption("child");
        child.setParent(parent);
        
        cmd.addOption(child);
        
        assertTrue("Child should be present", cmd.hasOption(child));
        assertTrue("Parent should be present", cmd.hasOption(parent));
    }

    @Test(timeout = 4000)
    public void testAddValueWithArgument() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestArgument arg = new TestArgument("arg1");
        cmd.addValue(arg, "value1");
        
        assertTrue("Argument should be added as option", cmd.hasOption(arg));
        List<?> values = cmd.getValues(arg, null);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("Value should be 'value1'", "value1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueMultiple() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addValue(opt, "value1");
        cmd.addValue(opt, "value2");
        cmd.addValue(opt, "value3");
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have three values", 3, values.size());
        assertEquals("First value", "value1", values.get(0));
        assertEquals("Second value", "value2", values.get(1));
        assertEquals("Third value", "value3", values.get(2));
    }

    @Test(timeout = 4000)
    public void testAddSwitch() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose", "-v");
        cmd.addSwitch(opt, true);
        
        assertTrue("Option should be present", cmd.hasOption(opt));
        assertEquals("Switch should be true", Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testAddSwitchDuplicate() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        cmd.addSwitch(opt, true);
        
        try {
            cmd.addSwitch(opt, false);
            fail("Should throw IllegalStateException for duplicate switch");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testHasOption() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        assertFalse("Option should not be present initially", cmd.hasOption(opt));
        
        cmd.addOption(opt);
        assertTrue("Option should be present after add", cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithDefaults() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<String> defaults = Arrays.asList("default1", "default2");
        cmd.setDefaultValues(opt, defaults);
        
        // No command line values, should return defaults
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should return defaults", defaults, values);
    }

    @Test(timeout = 4000)
    public void testGetValuesWithMethodDefaults() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<String> methodDefaults = Arrays.asList("method1", "method2");
        
        // No command line values and no option defaults, should return method defaults
        List<?> values = cmd.getValues(opt, methodDefaults);
        assertEquals("Should return method defaults", methodDefaults, values);
    }

    @Test(timeout = 4000)
    public void testGetValuesWithAugmentation() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addValue(opt, "cmd1");
        
        List<String> defaults = Arrays.asList("default1", "default2", "default3");
        cmd.setDefaultValues(opt, defaults);
        
        // Command line has 1 value, defaults have 3, should augment with 2 more
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have 3 values", 3, values.size());
        assertEquals("First should be cmd1", "cmd1", values.get(0));
        assertEquals("Second should be default2", "default2", values.get(1));
        assertEquals("Third should be default3", "default3", values.get(2));
    }

    @Test(timeout = 4000)
    public void testGetValuesEmpty() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<?> values = cmd.getValues(opt, null);
        assertTrue("Should return empty list", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValues() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addValue(opt, "value1");
        
        List<?> values = cmd.getUndefaultedValues(opt);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("Value should be 'value1'", "value1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValuesEmpty() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<?> values = cmd.getUndefaultedValues(opt);
        assertTrue("Should return empty list", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithCommandLine() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        cmd.addSwitch(opt, true);
        
        assertEquals("Should return true from command line", Boolean.TRUE, cmd.getSwitch(opt, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithMethodDefault() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        assertEquals("Should return method default", Boolean.TRUE, cmd.getSwitch(opt, Boolean.TRUE));
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithOptionDefault() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        
        assertEquals("Should return option default", Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testGetSwitchNull() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        assertNull("Should return null when no switch set", cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testGetProperty() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        cmd.addProperty("key1", "value1");
        assertEquals("Should return property value", "value1", cmd.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithOption() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addProperty(opt, "key1", "value1");
        
        assertEquals("Should return property value", "value1", cmd.getProperty(opt, "key1", null));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefault() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertEquals("Should return default value", "default", cmd.getProperty("nonexistent", "default"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithOptionDefault() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        assertEquals("Should return default value", "default", cmd.getProperty(opt, "nonexistent", "default"));
    }

    @Test(timeout = 4000)
    public void testGetProperties() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addProperty(opt, "key1", "value1");
        cmd.addProperty(opt, "key2", "value2");
        
        Set<?> props = cmd.getProperties(opt);
        assertEquals("Should have 2 properties", 2, props.size());
        assertTrue("Should contain key1", props.contains("key1"));
        assertTrue("Should contain key2", props.contains("key2"));
    }

    @Test(timeout = 4000)
    public void testGetPropertiesEmpty() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        Set<?> props = cmd.getProperties(opt);
        assertTrue("Should return empty set", props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetPropertiesGlobal() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        cmd.addProperty("key1", "value1");
        Set<?> props = cmd.getProperties();
        assertTrue("Should contain key1", props.contains("key1"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithPrefix() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertTrue("--option should look like option", cmd.looksLikeOption("--option"));
        assertTrue("-o should look like option", cmd.looksLikeOption("-o"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithoutPrefix() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertFalse("plaintext should not look like option", cmd.looksLikeOption("plaintext"));
        assertFalse("empty string should not look like option", cmd.looksLikeOption(""));
    }

    @Test(timeout = 4000)
    public void testToString() {
        List<String> args = Arrays.asList("arg1", "arg2", "arg with space");
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        
        String result = cmd.toString();
        assertEquals("Should format correctly", "arg1 arg2 \"arg with space\"", result);
    }

    @Test(timeout = 4000)
    public void testGetOptions() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt1 = new TestOption("opt1");
        TestOption opt2 = new TestOption("opt2");
        cmd.addOption(opt1);
        cmd.addOption(opt2);
        
        List<?> options = cmd.getOptions();
        assertEquals("Should have 2 options", 2, options.size());
        assertTrue("Should contain opt1", options.contains(opt1));
        assertTrue("Should contain opt2", options.contains(opt2));
    }

    @Test(timeout = 4000)
    public void testGetOptionTriggers() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1", "-o", "--option");
        cmd.addOption(opt);
        
        Set<?> triggers = cmd.getOptionTriggers();
        assertTrue("Should contain preferred name", triggers.contains("opt1"));
        assertTrue("Should contain -o", triggers.contains("-o"));
        assertTrue("Should contain --option", triggers.contains("--option"));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValues() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<String> defaults = Arrays.asList("default1", "default2");
        cmd.setDefaultValues(opt, defaults);
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should return defaults", defaults, values);
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesNull() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.setDefaultValues(opt, Arrays.asList("default1"));
        cmd.setDefaultValues(opt, null);
        
        List<?> values = cmd.getValues(opt, null);
        assertTrue("Should return empty list after removing defaults", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitch() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        
        assertEquals("Should return default switch", Boolean.TRUE, cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchNull() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("verbose");
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        cmd.setDefaultSwitch(opt, null);
        
        assertNull("Should return null after removing default switch", cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testGetNormalised() {
        List<String> args = Arrays.asList("arg1", "arg2");
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        
        List<?> normalised = cmd.getNormalised();
        assertEquals("Should return original arguments", args, normalised);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testNullOptionInAddValue() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.addValue(null, "value");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullOptionInAddSwitch() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.addSwitch(null, true);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullTriggerInGetOption() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertNull("Null trigger should return null", cmd.getOption(null));
    }

    @Test(timeout = 4000)
    public void testEmptyListInConstructor() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertTrue("Options should be empty", cmd.getOptions().isEmpty());
        assertEquals("Normalised should be empty", 0, cmd.getNormalised().size());
    }

    @Test(timeout = 4000)
    public void testEmptyStringTrigger() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertNull("Empty string trigger should return null", cmd.getOption(""));
    }

    @Test(timeout = 4000)
    public void testNullValueInAddValue() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addValue(opt, null);
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have one null value", 1, values.size());
        assertNull("Value should be null", values.get(0));
    }

    @Test(timeout = 4000)
    public void testEmptyDefaultsList() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        List<?> values = cmd.getValues(opt, Collections.emptyList());
        assertTrue("Should return empty list", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullPropertyKey() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.addProperty(null, "value");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullPropertyValue() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.addProperty("key", null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testNegativeNumberNotOption() {
        // BugCLI150: Negative numbers like "-42" should not be treated as options
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        // A negative number should not look like an option
        assertFalse("Negative number -42 should not look like option", cmd.looksLikeOption("-42"));
        assertFalse("Negative number -1 should not look like option", cmd.looksLikeOption("-1"));
        assertFalse("Negative number -0 should not look like option", cmd.looksLikeOption("-0"));
    }

    @Test(timeout = 4000)
    public void testNegativeNumberAsValue() {
        // BugCLI150: Negative numbers should be accepted as values, not treated as options
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("num");
        cmd.addValue(opt, "-42");
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("Value should be '-42'", "-42", values.get(0));
        
        // Verify the option is still present
        assertTrue("Option should be present", cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testNegativeNumberWithPrefixOption() {
        // BugCLI150: Ensure that --num -42 works correctly (negative number as argument)
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption numOption = new TestOption("num", "--num");
        cmd.addOption(numOption);
        cmd.addValue(numOption, "-42");
        
        // The option --num should be recognized
        assertTrue("Option --num should be present", cmd.hasOption(numOption));
        assertEquals("Option should be retrievable", numOption, cmd.getOption("--num"));
        
        // The value -42 should be stored
        List<?> values = cmd.getValues(numOption, null);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("Value should be '-42'", "-42", values.get(0));
    }

    @Test(timeout = 4000)
    public void testNegativeNumberInDefaults() {
        // BugCLI150: Default values with negative numbers
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("num");
        List<String> defaults = Arrays.asList("-1", "-2", "-3");
        cmd.setDefaultValues(opt, defaults);
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should return defaults with negative numbers", defaults, values);
    }

    @Test(timeout = 4000)
    public void testNegativeNumberAugmentation() {
        // BugCLI150: Augmenting values with negative number defaults
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("num");
        cmd.addValue(opt, "-10");
        
        List<String> defaults = Arrays.asList("-20", "-30", "-40");
        cmd.setDefaultValues(opt, defaults);
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have 3 values", 3, values.size());
        assertEquals("First should be -10", "-10", values.get(0));
        assertEquals("Second should be -30", "-30", values.get(1));
        assertEquals("Third should be -40", "-40", values.get(2));
    }

    @Test(timeout = 4000)
    public void testMultipleNegativeNumbers() {
        // BugCLI150: Multiple negative numbers as values
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("nums");
        cmd.addValue(opt, "-1");
        cmd.addValue(opt, "-2");
        cmd.addValue(opt, "-3");
        
        List<?> values = cmd.getValues(opt, null);
        assertEquals("Should have 3 values", 3, values.size());
        assertEquals("-1", values.get(0));
        assertEquals("-2", values.get(1));
        assertEquals("-3", values.get(2));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testNullOptionInHasOption() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertFalse("Null option should return false", cmd.hasOption(null));
    }

    @Test(timeout = 4000)
    public void testNullOptionInGetValues() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        List<?> values = cmd.getValues(null, null);
        assertTrue("Should return empty list for null option", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullOptionInGetUndefaultedValues() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        List<?> values = cmd.getUndefaultedValues(null);
        assertTrue("Should return empty list for null option", values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullOptionInGetSwitch() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertNull("Null option should return null", cmd.getSwitch(null, null));
    }

    @Test(timeout = 4000)
    public void testNullOptionInGetProperty() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        assertEquals("Should return default for null option", "default", cmd.getProperty(null, "key", "default"));
    }

    @Test(timeout = 4000)
    public void testNullOptionInGetProperties() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        Set<?> props = cmd.getProperties(null);
        assertTrue("Should return empty set for null option", props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullOptionInSetDefaultValues() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.setDefaultValues(null, Arrays.asList("value"));
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testNullOptionInSetDefaultSwitch() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        try {
            cmd.setDefaultSwitch(null, Boolean.TRUE);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiable() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        List<?> options = cmd.getOptions();
        try {
            options.add(new TestOption("newopt"));
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetOptionTriggersUnmodifiable() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        Set<?> triggers = cmd.getOptionTriggers();
        try {
            triggers.add("newtrigger");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetNormalisedUnmodifiable() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        List<?> normalised = cmd.getNormalised();
        try {
            normalised.add("newarg");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPropertiesUnmodifiable() {
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<String>());
        
        TestOption opt = new TestOption("opt1");
        cmd.addProperty(opt, "key1", "value1");
        
        Set<?> props = cmd.getProperties(opt);
        try {
            props.add("newkey");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testToStringConsistency() {
        List<String> args = Arrays.asList("first", "second", "third");
        TestOption root = new TestOption("root");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);
        
        String result = cmd.toString();
        assertEquals("first second third", result);
        
        // Verify that toString matches the normalised list
        StringBuilder expected = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) expected.append(' ');
            String arg = args.get(i);
            if (arg.indexOf(' ') >= 0) {
                expected.append('"').append(arg).append('"');
            } else {
                expected.append(arg);
            }
        }
        assertEquals(expected.toString(), result);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullArguments() {
        TestOption root = new TestOption("root");
        try {
            new WriteableCommandLineImpl(root, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullRoot() {
        try {
            new WriteableCommandLineImpl(null, new ArrayList<String>());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }
}