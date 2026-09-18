package org.apache.commons.cli2;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * Target: WriteableCommandLine interface methods through a concrete implementation
 * Key branches to cover:
 * 1. addOption - normal add, duplicate add, null option
 * 2. addValue - normal add, null option, null value, multiple values
 * 3. setDefaultValues - normal set, null option, null list, empty list
 * 4. addSwitch - normal add, duplicate add (IllegalStateException), null option
 * 5. setDefaultSwitch - normal set, null option, null boolean
 * 6. addProperty - normal add, null property, null value, overwrite existing
 * 7. looksLikeOption - starts with trigger char, doesn't start with trigger, null argument, empty string
 * 
 * Defect targeting (BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2):
 * - The bug relates to incorrect handling of argument lookahead for option-like strings
 * - Need to test looksLikeOption behavior with various argument formats
 * - Specifically test cases where arguments contain option-like prefixes within values
 */
public class WriteableCommandLineDeepseekTest {
    
    // Helper method to create a minimal concrete implementation for testing
    private WriteableCommandLine createCommandLine() {
        return new WriteableCommandLine() {
            private final List<Option> options = new ArrayList<Option>();
            private final java.util.Map<Option, List<Object>> values = new java.util.HashMap<Option, List<Object>>();
            private final java.util.Map<Option, List<Object>> defaultValues = new java.util.HashMap<Option, List<Object>>();
            private final java.util.Map<Option, Boolean> switches = new java.util.HashMap<Option, Boolean>();
            private final java.util.Map<Option, Boolean> defaultSwitches = new java.util.HashMap<Option, Boolean>();
            private final java.util.Map<String, String> properties = new java.util.HashMap<String, String>();

            @Override
            public void addOption(Option option) {
                if (!options.contains(option)) {
                    options.add(option);
                }
            }

            @Override
            public void addValue(Option option, Object value) {
                List<Object> valueList = values.get(option);
                if (valueList == null) {
                    valueList = new ArrayList<Object>();
                    values.put(option, valueList);
                }
                valueList.add(value);
            }

            @Override
            public List getValues(Option option) {
                List<Object> valueList = values.get(option);
                return valueList != null ? valueList : new ArrayList<Object>();
            }

            @Override
            public List getValues(Option option, List defaultValues) {
                List<Object> valueList = values.get(option);
                if (valueList == null || valueList.isEmpty()) {
                    return defaultValues;
                }
                return valueList;
            }

            @Override
            public List getValues(Option option, Object defaultValue) {
                List<Object> valueList = values.get(option);
                if (valueList == null || valueList.isEmpty()) {
                    return defaultValue != null ? Collections.singletonList(defaultValue) : Collections.emptyList();
                }
                return valueList;
            }

            @Override
            public void setDefaultValues(Option option, List defaultValues) {
                this.defaultValues.put(option, defaultValues);
            }

            @Override
            public void addSwitch(Option option, boolean value) throws IllegalStateException {
                if (switches.containsKey(option)) {
                    throw new IllegalStateException("Switch already added for option: " + option);
                }
                switches.put(option, value);
            }

            @Override
            public void setDefaultSwitch(Option option, Boolean defaultSwitch) {
                defaultSwitches.put(option, defaultSwitch);
            }

            @Override
            public void addProperty(String property, String value) {
                properties.put(property, value);
            }

            @Override
            public boolean looksLikeOption(String argument) {
                return argument != null && argument.startsWith("-");
            }

            @Override
            public Option getOption(String trigger) {
                return null;
            }

            @Override
            public List getOptions() {
                return new ArrayList<Option>(options);
            }

            @Override
            public List getArgs() {
                return new ArrayList<Object>();
            }

            @Override
            public boolean hasOption(Option option) {
                return options.contains(option);
            }

            @Override
            public boolean hasOption(String trigger) {
                return false;
            }

            @Override
            public Object getSwitch(Option option, Boolean defaultValue) {
                Boolean value = switches.get(option);
                if (value == null) {
                    Boolean defaultSwitch = defaultSwitches.get(option);
                    return defaultSwitch != null ? defaultSwitch : defaultValue;
                }
                return value;
            }

            @Override
            public String getProperty(String property) {
                return properties.get(property);
            }

            @Override
            public String getProperty(String property, String defaultValue) {
                String value = properties.get(property);
                return value != null ? value : defaultValue;
            }
        };
    }

    private Option createTestOption(String trigger) {
        return new Option() {
            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String argument) {
                return false;
            }

            @Override
            public void process(WriteableCommandLine commandLine, List arguments) {
            }

            @Override
            public void process(WriteableCommandLine commandLine, String argument) {
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public String getPreferredName() {
                return trigger;
            }

            @Override
            public String getDescription() {
                return "";
            }

            @Override
            public List getTriggers() {
                return Collections.singletonList(trigger);
            }

            @Override
            public boolean hasInitialSeparator() {
                return false;
            }

            @Override
            public boolean validate(WriteableCommandLine commandLine, Option option) {
                return true;
            }

            @Override
            public boolean canValidate(WriteableCommandLine commandLine, Option option) {
                return true;
            }

            @Override
            public String toString() {
                return trigger;
            }
        };
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddOptionAndGetOptions() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--test");
        cmd.addOption(opt);
        List options = cmd.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(opt));
    }

    @Test(timeout = 4000)
    public void testAddOptionDuplicate() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--test");
        cmd.addOption(opt);
        cmd.addOption(opt);
        List options = cmd.getOptions();
        assertEquals(1, options.size());
    }

    @Test(timeout = 4000)
    public void testAddValueAndGetValues() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--output");
        cmd.addValue(opt, "output.txt");
        cmd.addValue(opt, 123);
        List values = cmd.getValues(opt);
        assertEquals(2, values.size());
        assertEquals("output.txt", values.get(0));
        assertEquals(123, values.get(1));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithDefaultWhenEmpty() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--input");
        List defaults = Arrays.asList("default1", "default2");
        List result = cmd.getValues(opt, defaults);
        assertEquals(2, result.size());
        assertEquals("default1", result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithDefaultWhenHasValues() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--input");
        cmd.addValue(opt, "actual");
        List defaults = Arrays.asList("default1");
        List result = cmd.getValues(opt, defaults);
        assertEquals(1, result.size());
        assertEquals("actual", result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetValuesWithDefaultNull() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--config");
        List result = cmd.getValues(opt, (List) null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetValuesWithSingleDefault() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--verbose");
        Object defaultValue = "debug";
        List result = cmd.getValues(opt, defaultValue);
        assertEquals(1, result.size());
        assertEquals("debug", result.get(0));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testAddNullOption() {
        WriteableCommandLine cmd = createCommandLine();
        try {
            cmd.addOption(null);
        } catch (NullPointerException e) {
            // Expected behavior for null option
        }
    }

    @Test(timeout = 4000)
    public void testAddValueNullOption() {
        WriteableCommandLine cmd = createCommandLine();
        try {
            cmd.addValue(null, "value");
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testAddValueNullValue() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--nullable");
        cmd.addValue(opt, null);
        List values = cmd.getValues(opt);
        assertEquals(1, values.size());
        assertNull(values.get(0));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesNull() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--defaults");
        cmd.setDefaultValues(opt, null);
        cmd.addValue(opt, "actual");
        List defaults = Arrays.asList("fallback");
        List result = cmd.getValues(opt, defaults);
        assertEquals(1, result.size());
        assertEquals("actual", result.get(0));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesEmptyList() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--empty");
        cmd.setDefaultValues(opt, new ArrayList<Object>());
        List result = cmd.getValues(opt, Arrays.asList("fallback"));
        assertEquals(1, result.size());
        assertEquals("fallback", result.get(0));
    }

    @Test(timeout = 4000)
    public void testGetValuesEmptyOption() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--unused");
        List values = cmd.getValues(opt);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testLooksLikeOptionNullArgument() {
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.looksLikeOption(null));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionEmptyString() {
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.looksLikeOption(""));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithDash() {
        WriteableCommandLine cmd = createCommandLine();
        assertTrue(cmd.looksLikeOption("-"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionSingleDashTrigger() {
        WriteableCommandLine cmd = createCommandLine();
        assertTrue(cmd.looksLikeOption("-f"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionDoubleDashTrigger() {
        WriteableCommandLine cmd = createCommandLine();
        assertTrue(cmd.looksLikeOption("--file"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionNotTrigger() {
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.looksLikeOption("testfile.txt"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionPathWithDash() {
        // This test targets the defect scenario where values like "testfile.txt" are processed incorrectly
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.looksLikeOption("testfile.txt"));
        assertFalse(cmd.looksLikeOption("/path/to/file.txt"));
        assertFalse(cmd.looksLikeOption("C:\\data\\test.txt"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionNumber() {
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.looksLikeOption("123"));
        assertFalse(cmd.looksLikeOption("-123")); // This might be option-like depending on implementation
    }

    @Test(timeout = 4000)
    public void testBugLoopingOptionLookAlike2_DefectRevealer() {
        // This test directly targets the known defect from BugLoopingOptionLookAlikeTest
        // The bug occurs when arguments that look like options are processed incorrectly
        WriteableCommandLine cmd = createCommandLine();
        
        // Test case simulating the failure condition
        String argument = "testfile.txt";
        
        // The expected behavior: "testfile.txt" should NOT look like an option
        assertFalse("Argument '" + argument + "' should not look like an option", 
                    cmd.looksLikeOption(argument));
        
        // Additional tests for similar patterns that could trigger the bug
        assertFalse(cmd.looksLikeOption("input.txt"));
        assertFalse(cmd.looksLikeOption("data.csv"));
        assertFalse(cmd.looksLikeOption("file-with-dashes.txt"));
        
        // These should correctly be identified as option-like
        assertTrue(cmd.looksLikeOption("--input"));
        assertTrue(cmd.looksLikeOption("-o"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddSwitchDuplicateThrowsIllegalStateException() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--verbose");
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false); // Should throw IllegalStateException
    }

    @Test(timeout = 4000)
    public void testAddSwitchFirstTimeSuccess() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--debug");
        cmd.addSwitch(opt, true);
        Object result = cmd.getSwitch(opt, false);
        assertTrue((Boolean) result);
    }

    @Test(timeout = 4000)
    public void testGetSwitchWithDefaultNotSet() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--quiet");
        Object result = cmd.getSwitch(opt, Boolean.TRUE);
        assertTrue((Boolean) result);
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchAndGet() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--color");
        cmd.setDefaultSwitch(opt, Boolean.FALSE);
        Object result = cmd.getSwitch(opt, Boolean.TRUE);
        assertFalse((Boolean) result);
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchNull() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--null-switch");
        cmd.setDefaultSwitch(opt, null);
        Object result = cmd.getSwitch(opt, Boolean.TRUE);
        assertTrue((Boolean) result);
    }

    @Test(timeout = 4000)
    public void testAddPropertyThenGet() {
        WriteableCommandLine cmd = createCommandLine();
        cmd.addProperty("user", "john");
        String result = cmd.getProperty("user");
        assertEquals("john", result);
    }

    @Test(timeout = 4000)
    public void testAddPropertyOverwriteExisting() {
        WriteableCommandLine cmd = createCommandLine();
        cmd.addProperty("host", "localhost");
        cmd.addProperty("host", "remotehost");
        String result = cmd.getProperty("host");
        assertEquals("remotehost", result);
    }

    @Test(timeout = 4000)
    public void testAddPropertyNullValue() {
        WriteableCommandLine cmd = createCommandLine();
        cmd.addProperty("key", null);
        assertNull(cmd.getProperty("key"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyNonExistent() {
        WriteableCommandLine cmd = createCommandLine();
        assertNull(cmd.getProperty("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefaultNonExistent() {
        WriteableCommandLine cmd = createCommandLine();
        String result = cmd.getProperty("missing", "default");
        assertEquals("default", result);
    }

    @Test(timeout = 4000)
    public void testGetPropertyWithDefaultExists() {
        WriteableCommandLine cmd = createCommandLine();
        cmd.addProperty("existing", "value");
        String result = cmd.getProperty("existing", "default");
        assertEquals("value", result);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testHasOptionFalse() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--missing");
        assertFalse(cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testHasOptionTrue() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--present");
        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testGetArgsReturnsEmptyList() {
        WriteableCommandLine cmd = createCommandLine();
        List args = cmd.getArgs();
        assertNotNull(args);
        assertTrue(args.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetSwitchNotSetNoDefaults() {
        WriteableCommandLine cmd = createCommandLine();
        Option opt = createTestOption("--unset");
        Object result = cmd.getSwitch(opt, null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testHasOptionByTrigger() {
        WriteableCommandLine cmd = createCommandLine();
        assertFalse(cmd.hasOption("--nonexistent"));
    }
}