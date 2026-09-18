package org.apache.commons.cli2.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.option.ArgumentImpl;
import org.apache.commons.cli2.option.OptionImpl;
import org.apache.commons.cli2.option.GroupImpl;
import org.apache.commons.cli2.option.PropertyOption;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: WriteableCommandLineImpl
 * 
 * Key branches to cover:
 * - addOption: null/valid option, trigger mapping
 * - addValue: Argument vs non-Argument, null/empty value list
 * - addSwitch: duplicate switch detection, true/false values
 * - hasOption: present/absent option
 * - getOption: existing/non-existing trigger
 * - getValues: null/empty/valid value list, default values fallback chain
 * - getUndefaultedValues: null/valid value list
 * - getSwitch: null/valid switch, default value fallback chain
 * - getProperty: null/valid properties, default value fallback
 * - addProperty: null/valid properties map
 * - getProperties: null/valid properties, unmodifiable set
 * - looksLikeOption: prefix match/no match
 * - toString: normalised list with/without spaces
 * - getOptions: unmodifiable list
 * - getOptionTriggers: unmodifiable set
 * - setDefaultValues: null/non-null defaults
 * - setDefaultSwitch: null/non-null default switch
 * - getNormalised: unmodifiable list
 * 
 * Defect targeting (BugCLI158Test):
 * - getValues must return default values when command line values are empty
 * - Specifically: when values list is empty but defaultValues exist,
 *   the method should return the defaultValues, not an empty list
 * - Test: testSingleOptionSingleArgument and testSingleOptionMaximumNumberOfArgument
 *   expect defaults to be returned when no command line values present
 */
public class WriteableCommandLineImplDeepseekTest {

    // Helper method to create a test option
    private Option createTestOption(String name) {
        return new OptionImpl(Collections.singleton(name), false, false, null) {
            @Override
            public void process(WriteableCommandLine commandLine, List arguments) {
                // no-op for testing
            }
            
            @Override
            public String getPreferredName() {
                return name;
            }
            
            @Override
            public Set getTriggers() {
                return Collections.singleton(name);
            }
            
            @Override
            public Set getPrefixes() {
                return Collections.singleton("-");
            }
            
            @Override
            public void validate(WriteableCommandLine commandLine) {
                // no-op
            }
            
            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // no-op
            }
        };
    }

    // Helper to create an Argument option
    private Argument createTestArgument(String name) {
        return new ArgumentImpl(name, null, null, 0, 1, null) {
            @Override
            public String getPreferredName() {
                return name;
            }
            
            @Override
            public Set getTriggers() {
                return Collections.singleton(name);
            }
            
            @Override
            public Set getPrefixes() {
                return Collections.singleton("-");
            }
        };
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddOptionAndHasOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("test");
        
        assertFalse(cmd.hasOption(opt));
        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testAddOptionWithTriggers() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("test");
        cmd.addOption(opt);
        
        assertEquals(opt, cmd.getOption("test"));
        assertNull(cmd.getOption("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testAddValueAndGetValues() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.addValue(opt, "value1");
        cmd.addValue(opt, "value2");
        
        List values = cmd.getValues(opt, null);
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testAddSwitchAndGetSwitch() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.addSwitch(opt, true);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, Boolean.FALSE));
        
        Option opt2 = createTestOption("switch2");
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt2, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testAddSwitchDuplicateThrows() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.addSwitch(opt, true);
        try {
            cmd.addSwitch(opt, false);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValuesWithDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        List defaults = Arrays.asList("default1", "default2");
        cmd.setDefaultValues(opt, defaults);
        
        List values = cmd.getValues(opt, null);
        assertEquals(2, values.size());
        assertEquals("default1", values.get(0));
        assertEquals("default2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithMethodDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        List methodDefaults = Arrays.asList("method1");
        List values = cmd.getValues(opt, methodDefaults);
        assertEquals(1, values.size());
        assertEquals("method1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetValuesEmptyAll() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        List values = cmd.getValues(opt, null);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValues() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.addValue(opt, "value");
        List values = cmd.getUndefaultedValues(opt);
        assertEquals(1, values.size());
        assertEquals("value", values.get(0));
        
        Option opt2 = createTestArgument("arg2");
        List emptyValues = cmd.getUndefaultedValues(opt2);
        assertTrue(emptyValues.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, null));
        
        Option opt2 = createTestOption("switch2");
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt2, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testGetSwitchNullAll() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        assertNull(cmd.getSwitch(opt, null));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testAddValueWithNullValue() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.addValue(opt, null);
        List values = cmd.getValues(opt, null);
        assertEquals(1, values.size());
        assertNull(values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueWithEmptyList() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.addValue(opt, "value");
        List values = cmd.getValues(opt, null);
        assertEquals(1, values.size());
    }

    @Test(timeout = 4000)
    public void testGetValuesWithEmptyDefaultList() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.setDefaultValues(opt, new ArrayList<String>());
        List values = cmd.getValues(opt, null);
        assertTrue(values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetValuesWithNullDefaultList() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.setDefaultValues(opt, null);
        List values = cmd.getValues(opt, null);
        assertTrue(values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchNull() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        cmd.setDefaultSwitch(opt, null);
        assertNull(cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testGetPropertiesEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("prop");
        
        Set props = cmd.getProperties(opt);
        assertNotNull(props);
        assertTrue(props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("prop");
        
        assertEquals("default", cmd.getProperty(opt, "key", "default"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        assertTrue(cmd.looksLikeOption("-test"));
        assertFalse(cmd.looksLikeOption("test"));
        assertFalse(cmd.looksLikeOption(""));
        assertFalse(cmd.looksLikeOption(null));
    }

    @Test(timeout = 4000)
    public void testToStringWithSpaces() {
        List<String> args = Arrays.asList("arg1", "arg with space", "arg3");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), args);
        
        String result = cmd.toString();
        assertEquals("arg1 \"arg with space\" arg3", result);
    }

    @Test(timeout = 4000)
    public void testToStringEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        assertEquals("", cmd.toString());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test for BugCLI158Test::testSingleOptionSingleArgument
     * The bug: when command line values are empty but default values exist,
     * getValues returns empty list instead of default values
     */
    @Test(timeout = 4000)
    public void testGetValuesWithDefaultsWhenCommandLineEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        // Set default values
        List defaults = Arrays.asList("1", "1000");
        cmd.setDefaultValues(opt, defaults);
        
        // No command line values added
        List values = cmd.getValues(opt, null);
        
        // Expected: should return defaults [1, 1000]
        assertEquals(2, values.size());
        assertEquals("1", values.get(0));
        assertEquals("1000", values.get(1));
    }

    /**
     * Defect-targeted test for BugCLI158Test::testSingleOptionMaximumNumberOfArgument
     * The bug: when command line values are empty but default values exist,
     * getValues returns empty list instead of default values
     */
    @Test(timeout = 4000)
    public void testGetValuesWithDefaultsWhenCommandLineEmptyMultiple() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        // Set default values
        List defaults = Arrays.asList("1", "2", "10000");
        cmd.setDefaultValues(opt, defaults);
        
        // No command line values added
        List values = cmd.getValues(opt, null);
        
        // Expected: should return defaults [1, 2, 10000]
        assertEquals(3, values.size());
        assertEquals("1", values.get(0));
        assertEquals("2", values.get(1));
        assertEquals("10000", values.get(2));
    }

    /**
     * Additional defect-targeted test: when command line values exist,
     * they should take precedence over defaults
     */
    @Test(timeout = 4000)
    public void testGetValuesWithCommandLineValuesAndDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        // Set default values
        List defaults = Arrays.asList("default1", "default2");
        cmd.setDefaultValues(opt, defaults);
        
        // Add command line values
        cmd.addValue(opt, "actual1");
        
        List values = cmd.getValues(opt, null);
        
        // Expected: command line values take precedence
        assertEquals(1, values.size());
        assertEquals("actual1", values.get(0));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testAddSwitchDuplicateWithDifferentValue() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.addSwitch(opt, true);
        try {
            cmd.addSwitch(opt, true);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetOptionWithNullTrigger() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        assertNull(cmd.getOption(null));
    }

    @Test(timeout = 4000)
    public void testAddValueWithNonArgumentOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("nonarg");
        
        cmd.addValue(opt, "value");
        List values = cmd.getValues(opt, null);
        assertEquals(1, values.size());
        assertEquals("value", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetPropertiesWithPropertyOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        cmd.addProperty("key", "value");
        assertEquals("value", cmd.getProperty("key"));
        
        Set props = cmd.getProperties();
        assertTrue(props.contains("key"));
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("prop");
        
        cmd.addProperty(opt, "key", "value");
        assertEquals("value", cmd.getProperty(opt, "key", "default"));
        
        Set props = cmd.getProperties(opt);
        assertTrue(props.contains("key"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithNullOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        assertEquals("default", cmd.getProperty(null, "key", "default"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiable() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("test");
        cmd.addOption(opt);
        
        List options = cmd.getOptions();
        assertEquals(1, options.size());
        
        try {
            options.add(createTestOption("new"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetOptionTriggersUnmodifiable() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("test");
        cmd.addOption(opt);
        
        Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.contains("test"));
        
        try {
            triggers.add("new");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetNormalisedUnmodifiable() {
        List<String> args = Arrays.asList("arg1", "arg2");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), args);
        
        List normalised = cmd.getNormalised();
        assertEquals(2, normalised.size());
        
        try {
            normalised.add("arg3");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPropertiesUnmodifiable() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("prop");
        
        cmd.addProperty(opt, "key", "value");
        Set props = cmd.getProperties(opt);
        
        try {
            props.add("newkey");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMultipleOptionsAndValues() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt1 = createTestArgument("arg1");
        Option opt2 = createTestArgument("arg2");
        
        cmd.addValue(opt1, "value1");
        cmd.addValue(opt2, "value2");
        
        assertEquals(1, cmd.getValues(opt1, null).size());
        assertEquals(1, cmd.getValues(opt2, null).size());
        assertEquals("value1", cmd.getValues(opt1, null).get(0));
        assertEquals("value2", cmd.getValues(opt2, null).get(0));
    }

    @Test(timeout = 4000)
    public void testDefaultValuesAndSwitchesCombined() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        Option switchOpt = createTestOption("switch");
        
        cmd.setDefaultValues(opt, Arrays.asList("default"));
        cmd.setDefaultSwitch(switchOpt, Boolean.TRUE);
        
        assertEquals("default", cmd.getValues(opt, null).get(0));
        assertEquals(Boolean.TRUE, cmd.getSwitch(switchOpt, null));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithMethodAndStoredDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestArgument("arg");
        
        cmd.setDefaultValues(opt, Arrays.asList("stored"));
        List methodDefaults = Arrays.asList("method");
        
        // Method defaults should be used first
        List values = cmd.getValues(opt, methodDefaults);
        assertEquals("method", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithMethodAndStoredDefaults() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("switch");
        
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        
        // Method default should be used first
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testAddOptionMultipleTimes() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        Option opt = createTestOption("test");
        
        cmd.addOption(opt);
        cmd.addOption(opt);
        
        // Should still only have one option
        assertEquals(1, cmd.getOptions().size());
    }

    @Test(timeout = 4000)
    public void testPropertyOptionDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(
            createTestOption("root"), new ArrayList<String>());
        
        // Test with default PropertyOption
        cmd.addProperty("key", "value");
        assertEquals("value", cmd.getProperty("key"));
        assertEquals("default", cmd.getProperty("nonexistent", "default"));
    }
}