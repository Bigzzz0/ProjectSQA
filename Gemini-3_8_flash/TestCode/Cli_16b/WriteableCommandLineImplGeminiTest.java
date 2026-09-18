/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.cli2.commandline;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.Group;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Target Class: WriteableCommandLineImpl
 *
 * 1. Branch Analysis:
 *    - addOption(Option):
 *        * iterates over getTriggers()
 *        * parent options propagation (CLI-123 Defect Zone)
 *    - addValue(Option, Object):
 *        * branch: option instanceof Argument (adds option if Argument, skips addOption otherwise)
 *        * branch: values.get(option) == null vs already present (creates new list vs appends)
 *    - addSwitch(Option, boolean):
 *        * branch: switches.containsKey(option) -> throws IllegalStateException
 *        * branch: value true vs false -> Boolean.TRUE / Boolean.FALSE
 *    - hasOption(Option):
 *        * present in options list vs absent
 *    - getOption(String trigger):
 *        * trigger maps to preferredName or alias in triggers
 *        * unknown trigger -> returns null
 *    - getValues(Option, List defaultValues):
 *        * branch: defaultValues null or empty -> fall back to this.defaultValues.get(option)
 *        * branch: augmented defaultValues null / empty
 *        * branch: valueList null / empty -> assigned to defaultValues
 *        * branch: defaultValues.size() > valueList.size() -> copies and appends tail defaults
 *        * branch: defaultValues.size() <= valueList.size() -> retains valueList as-is
 *        * branch: valueList == null -> Collections.EMPTY_LIST
 *    - getUndefaultedValues(Option):
 *        * branch: values.get(option) == null -> Collections.EMPTY_LIST
 *        * branch: values.get(option) != null -> returns existing List
 *    - getSwitch(Option, Boolean defaultValue):
 *        * branch: switches.get(option) != null
 *        * branch: fallback to supplied defaultValue != null
 *        * branch: fallback to defaultSwitches.get(option)
 *        * branch: all null -> returns null
 *    - Property Methods (addProperty, getProperty, getProperties):
 *        * with custom Option vs default PropertyOption()
 *        * properties map exists vs null
 *        * key exists vs missing with default fallback
 *    - looksLikeOption(String):
 *        * trigger starts with any rootOption prefix -> true
 *        * trigger starts with none -> false
 *    - toString():
 *        * arguments containing spaces (quoted) vs no spaces (unquoted)
 *        * delimiter insertion between elements
 *    - setDefaultValues & setDefaultSwitch:
 *        * value null -> map.remove(option)
 *        * value not null -> map.put(option, value)
 *    - Immutability guards on getOptions(), getOptionTriggers(), getProperties(), getNormalised()
 *
 * 2. Defect Analysis (CLI-123 & TestGetOptions_Order):
 *    - In addOption(Option), parent options are expected to be recursively added to the
 *      command line so that hasOption(parent) is true and parent appears in getOptions().
 *      The defective code has only a comment: "// ensure that all parent options are also added"
 *      without performing the addition, causing child option processing to fail hierarchy validation.
 */
public class WriteableCommandLineImplGeminiTest {

    private DefaultOption rootOption;
    private List<String> argumentsList;
    private WriteableCommandLineImpl commandLine;

    // Custom test stub to precisely control option hierarchy without heavy builders
    private static class DummyOption implements Option {
        private final String preferredName;
        private final Set<String> triggers;
        private final Set<String> prefixes;
        private Option parent;

        public DummyOption(String preferredName, Set<String> triggers, Set<String> prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers != null ? triggers : Collections.singleton(preferredName);
            this.prefixes = prefixes != null ? prefixes : Collections.emptySet();
        }

        public void setParent(Option parent) {
            this.parent = parent;
        }

        public Option getParent() {
            return this.parent;
        }

        public String getPreferredName() {
            return preferredName;
        }

        public Set getTriggers() {
            return triggers;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String arg) { return false; }
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) { return false; }
        public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {}
        public void validate(WriteableCommandLine commandLine) throws OptionException {}
        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {}
        public String getDescription() { return preferredName; }
        public int getId() { return preferredName.hashCode(); }
        public boolean checkPrefixes(Set prefixes) { return true; }
        public Option findOption(String trigger) { return preferredName.equals(trigger) ? this : null; }
        public boolean isRequired() { return false; }
        public void defaults(WriteableCommandLine commandLine) {}
    }

    @Before
    public void setUp() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("--");
        prefixes.add("-");
        rootOption = new DefaultOptionBuilder()
                .withLongName("root")
                .withShortName("r")
                .create();
        argumentsList = new ArrayList<String>();
        commandLine = new WriteableCommandLineImpl(rootOption, argumentsList);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndHasOption() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("opt").withShortName("o").create();
        assertFalse(commandLine.hasOption(opt));
        assertNull(commandLine.getOption("--opt"));
        assertNull(commandLine.getOption("-o"));

        commandLine.addOption(opt);

        assertTrue(commandLine.hasOption(opt));
        assertSame(opt, commandLine.getOption("--opt"));
        assertSame(opt, commandLine.getOption("-o"));
        assertEquals(1, commandLine.getOptions().size());
        assertTrue(commandLine.getOptionTriggers().contains("--opt"));
        assertTrue(commandLine.getOptionTriggers().contains("-o"));
    }

    @Test(timeout = 4000)
    public void testAddSwitchSuccess() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("debug").create();
        assertNull(commandLine.getSwitch(opt, null));

        commandLine.addSwitch(opt, true);
        assertTrue(commandLine.hasOption(opt));
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testAddSwitchFalse() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("silent").create();
        commandLine.addSwitch(opt, false);

        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testAddValueForRegularOptionDoesNotImplicitlyAddOption() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("file").create();
        commandLine.addValue(opt, "file.txt");

        // Regular options (not instanceof Argument) are NOT automatically added to options list
        assertFalse(commandLine.hasOption(opt));
        List values = commandLine.getValues(opt, null);
        assertEquals(1, values.size());
        assertEquals("file.txt", values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueForArgumentImplicitlyAddsOption() {
        Argument arg = new ArgumentBuilder().withName("target").create();
        commandLine.addValue(arg, "targetValue");

        assertTrue(commandLine.hasOption(arg));
        List values = commandLine.getValues(arg, null);
        assertEquals(1, values.size());
        assertEquals("targetValue", values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddMultipleValues() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("item").create();
        commandLine.addValue(opt, "first");
        commandLine.addValue(opt, "second");

        List values = commandLine.getValues(opt, null);
        assertEquals(2, values.size());
        assertEquals("first", values.get(0));
        assertEquals("second", values.get(1));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOption() {
        assertTrue(commandLine.looksLikeOption("--option"));
        assertTrue(commandLine.looksLikeOption("-o"));
        assertFalse(commandLine.looksLikeOption("value"));
        assertFalse(commandLine.looksLikeOption("+option"));
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        argumentsList.add("--file");
        argumentsList.add("my document.txt");
        argumentsList.add("--verbose");

        String str = commandLine.toString();
        assertEquals("--file \"my document.txt\" --verbose", str);
    }

    @Test(timeout = 4000)
    public void testToStringEmptyArguments() {
        assertEquals("", commandLine.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValuesWithDefaultsAugmentation() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("port").create();

        // 1. Both empty -> returns EMPTY_LIST
        List res = commandLine.getValues(opt, null);
        assertNotNull(res);
        assertTrue(res.isEmpty());

        // 2. No command-line value, method defaults provided
        List<String> defaults = Arrays.asList("8080", "8443");
        res = commandLine.getValues(opt, defaults);
        assertEquals(defaults, res);

        // 3. Command line has 1 value, defaults has 2 values -> augmentation branch
        commandLine.addValue(opt, "9090");
        res = commandLine.getValues(opt, defaults);
        assertEquals(2, res.size());
        assertEquals("9090", res.get(0));
        assertEquals("8443", res.get(1)); // augmented from defaults index 1

        // 4. Command line has more values than defaults -> no augmentation
        commandLine.addValue(opt, "9091");
        commandLine.addValue(opt, "9092");
        res = commandLine.getValues(opt, defaults);
        assertEquals(3, res.size());
        assertEquals("9090", res.get(0));
        assertEquals("9091", res.get(1));
        assertEquals("9092", res.get(2));
    }

    @Test(timeout = 4000)
    public void testGetValuesOptionDefaultFallback() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("host").create();
        List<String> optDefaults = Collections.singletonList("localhost");

        commandLine.setDefaultValues(opt, optDefaults);

        // defaultValues parameter is null, fallback to this.defaultValues
        List res = commandLine.getValues(opt, null);
        assertEquals(optDefaults, res);

        // defaultValues parameter is empty list, fallback to this.defaultValues
        res = commandLine.getValues(opt, Collections.emptyList());
        assertEquals(optDefaults, res);

        // Removing default values
        commandLine.setDefaultValues(opt, null);
        res = commandLine.getValues(opt, null);
        assertTrue(res.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValues() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("data").create();

        assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(opt));

        commandLine.setDefaultValues(opt, Collections.singletonList("defaultData"));
        // Still empty because getUndefaultedValues ignores defaults
        assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(opt));

        commandLine.addValue(opt, "actualData");
        List undefaulted = commandLine.getUndefaultedValues(opt);
        assertEquals(1, undefaulted.size());
        assertEquals("actualData", undefaulted.get(0));
    }

    @Test(timeout = 4000)
    public void testGetSwitchHierarchy() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("flag").create();

        // 1. All null -> returns null
        assertNull(commandLine.getSwitch(opt, null));

        // 2. Default switch set on commandLine
        commandLine.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));

        // 3. Explicit method defaultValue overrides setDefaultSwitch
        assertEquals(Boolean.FALSE, commandLine.getSwitch(opt, Boolean.FALSE));

        // 4. Command line parsed switch overrides both
        commandLine.addSwitch(opt, true);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, Boolean.FALSE));

        // 5. setDefaultSwitch with null removes it
        commandLine.setDefaultSwitch(opt, null);
    }

    @Test(timeout = 4000)
    public void testPropertyOperationsDefaultOption() {
        assertNull(commandLine.getProperty("some.prop"));
        assertTrue(commandLine.getProperties().isEmpty());

        commandLine.addProperty("prop1", "value1");
        assertEquals("value1", commandLine.getProperty("prop1"));
        assertEquals(1, commandLine.getProperties().size());
        assertTrue(commandLine.getProperties().contains("prop1"));
    }

    @Test(timeout = 4000)
    public void testPropertyOperationsCustomOption() {
        PropertyOption propOption = new PropertyOption();
        assertNull(commandLine.getProperty(propOption, "custom.key", null));
        assertEquals("defaultVal", commandLine.getProperty(propOption, "custom.key", "defaultVal"));
        assertTrue(commandLine.getProperties(propOption).isEmpty());

        commandLine.addProperty(propOption, "custom.key", "customValue");
        assertEquals("customValue", commandLine.getProperty(propOption, "custom.key", "defaultVal"));
        assertEquals(1, commandLine.getProperties(propOption).size());
        assertTrue(commandLine.getProperties(propOption).contains("custom.key"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-123 & TestGetOptions_Order)
    // =========================================================================

    /**
     * Targets the defect where WriteableCommandLineImpl.addOption(Option) does not
     * propagate addition to option's parents as documented in the source code comment:
     * "// ensure that all parent options are also added".
     *
     * In the defective implementation, adding a child option leaves hasOption(parent) as false.
     * The correct expected behavior requires all parent options to be added as well.
     */
    @Test(timeout = 4000)
    public void testDefectAddOptionShouldAddParentHierarchy() {
        DummyOption parentOption = new DummyOption("parent", Collections.singleton("parent"), Collections.singleton("--"));
        DummyOption childOption = new DummyOption("child", Collections.singleton("child"), Collections.singleton("--"));
        childOption.setParent(parentOption);

        commandLine.addOption(childOption);

        assertTrue("Defect CLI-123: Parent option must be recorded when child is added",
                commandLine.hasOption(parentOption));
        assertTrue("getOptions() must contain parent option",
                commandLine.getOptions().contains(parentOption));
    }

    /**
     * Targets the order and presence of parent and child options in getOptions().
     */
    @Test(timeout = 4000)
    public void testDefectParentChildOptionsOrder() {
        DummyOption grandParent = new DummyOption("rootGroup", Collections.singleton("rootGroup"), Collections.emptySet());
        DummyOption parent = new DummyOption("parentCmd", Collections.singleton("parentCmd"), Collections.emptySet());
        parent.setParent(grandParent);
        DummyOption child = new DummyOption("childArg", Collections.singleton("childArg"), Collections.emptySet());
        child.setParent(parent);

        commandLine.addOption(child);

        List options = commandLine.getOptions();
        assertTrue("All ancestor options should be registered", options.contains(grandParent));
        assertTrue("Parent option should be registered", options.contains(parent));
        assertTrue("Child option should be registered", options.contains(child));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testAddSwitchAlreadySetThrowsIllegalStateException() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("verbose").create();
        commandLine.addSwitch(opt, true);
        // Second call on the same option MUST throw IllegalStateException
        commandLine.addSwitch(opt, true);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testAddSwitchAlreadySetWithDifferentValueThrowsIllegalStateException() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("quiet").create();
        commandLine.addSwitch(opt, true);
        commandLine.addSwitch(opt, false);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetOptionsIsUnmodifiable() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("test").create();
        commandLine.getOptions().add(opt);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetOptionTriggersIsUnmodifiable() {
        commandLine.getOptionTriggers().add("--dummy");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetPropertiesIsUnmodifiable() {
        commandLine.addProperty("k", "v");
        commandLine.getProperties().add("newKey");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetNormalisedIsUnmodifiable() {
        commandLine.getNormalised().add("extra");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Complex Combinations
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetNormalisedReturnsPassedList() {
        argumentsList.add("-a");
        argumentsList.add("-b");
        List normalised = commandLine.getNormalised();
        assertEquals(2, normalised.size());
        assertEquals("-a", normalised.get(0));
        assertEquals("-b", normalised.get(1));
    }

    @Test(timeout = 4000)
    public void testMultipleOptionTriggersRegistered() {
        Set<String> prefixes = new HashSet<String>();
        prefixes.add("-");
        prefixes.add("--");
        DefaultOption opt = new DefaultOptionBuilder()
                .withLongName("help")
                .withShortName("h")
                .create();

        commandLine.addOption(opt);

        assertSame(opt, commandLine.getOption("--help"));
        assertSame(opt, commandLine.getOption("-h"));
    }

    @Test(timeout = 4000)
    public void testGetValuesReturnsEmptyListWhenOptionNotSet() {
        DefaultOption opt = new DefaultOptionBuilder().withLongName("unknown").create();
        List values = commandLine.getValues(opt);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithEmptyPrefixes() {
        DummyOption emptyPrefixRoot = new DummyOption("root", null, Collections.<String>emptySet());
        WriteableCommandLineImpl emptyCmdLine = new WriteableCommandLineImpl(emptyPrefixRoot, Collections.emptyList());
        assertFalse(emptyCmdLine.looksLikeOption("--any"));
        assertFalse(emptyCmdLine.looksLikeOption("-a"));
    }
}