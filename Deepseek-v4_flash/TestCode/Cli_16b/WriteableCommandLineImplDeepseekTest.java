package org.apache.commons.cli2.commandline;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: WriteableCommandLineImpl
 * 
 * Defect: The constructor does not add the rootOption to the internal options list,
 * and addOption() does not recursively add parent options. This causes getOptions()
 * to miss options, leading to ordering failures (e.g., testGetOptions_Order).
 * 
 * Branches targeted:
 * - addOption: normal add, parent propagation (missing)
 * - addValue: with Argument vs non-Argument, null/empty valueList
 * - addSwitch: normal, duplicate (IllegalStateException)
 * - hasOption: present/absent
 * - getOption: trigger mapping
 * - getValues: null/empty defaultValues, defaultValues larger than valueList
 * - getUndefaultedValues: null/empty valueList
 * - getSwitch: null defaultValue, defaultSwitches fallback
 * - getProperty: null properties, default fallback
 * - addProperty: new/existing Properties
 * - looksLikeOption: prefix matching
 * - toString: normalised arguments with spaces
 * - setDefaultValues: null vs non-null
 * - setDefaultSwitch: null vs non-null
 * - getNormalised: unmodifiable list
 * 
 * Boundary conditions: null arguments, empty collections, MAX_INT values (not applicable here)
 */
public class WriteableCommandLineImplDeepseekTest {

    // --- Stub implementations for testing ---

    private static class OptionStub implements Option {
        private final String preferredName;
        private final Set<String> triggers;
        private final Set<String> prefixes;
        private final Option parent;
        private final List<Option> children;

        OptionStub(String preferredName, Set<String> triggers, Set<String> prefixes, Option parent) {
            this.preferredName = preferredName;
            this.triggers = triggers;
            this.prefixes = prefixes;
            this.parent = parent;
            this.children = new ArrayList<>();
        }

        OptionStub(String preferredName, Set<String> triggers, Set<String> prefixes) {
            this(preferredName, triggers, prefixes, null);
        }

        @Override
        public String getPreferredName() { return preferredName; }

        @Override
        public Set<String> getTriggers() { return triggers; }

        @Override
        public Set<String> getPrefixes() { return prefixes; }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator<String> arguments) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments) {
        }

        @Override
        public Option getParent() { return parent; }

        @Override
        public List<Option> getChildren() { return children; }

        @Override
        public String getDescription() { return ""; }

        @Override
        public int getId() { return 0; }

        @Override
        public Argument getArgument() { return null; }

        @Override
        public List getDefaultValues() { return Collections.EMPTY_LIST; }

        @Override
        public Boolean getDefaultSwitch() { return null; }

        @Override
        public boolean isRequired() { return false; }

        @Override
        public boolean getTriggersInclude(String trigger) { return triggers.contains(trigger); }

        @Override
        public void defaults(WriteableCommandLine commandLine) {
        }

        @Override
        public String toString() { return preferredName; }
    }

    private static class ArgumentStub implements Argument {
        @Override
        public String getPreferredName() { return "arg"; }

        @Override
        public Set<String> getTriggers() { return Collections.singleton("arg"); }

        @Override
        public Set<String> getPrefixes() { return Collections.emptySet(); }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator<String> arguments) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments) {
        }

        @Override
        public Option getParent() { return null; }

        @Override
        public List<Option> getChildren() { return Collections.emptyList(); }

        @Override
        public String getDescription() { return ""; }

        @Override
        public int getId() { return 0; }

        @Override
        public Argument getArgument() { return this; }

        @Override
        public List getDefaultValues() { return Collections.EMPTY_LIST; }

        @Override
        public Boolean getDefaultSwitch() { return null; }

        @Override
        public boolean isRequired() { return false; }

        @Override
        public boolean getTriggersInclude(String trigger) { return false; }

        @Override
        public void defaults(WriteableCommandLine commandLine) {
        }

        @Override
        public String toString() { return "arg"; }
    }

    // --- Helper to create a root option with prefixes ---
    private Option createRootOption() {
        Set<String> prefixes = new HashSet<>(Arrays.asList("-", "--"));
        Set<String> triggers = new HashSet<>(Arrays.asList("--root", "-r"));
        return new OptionStub("--root", triggers, prefixes);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAddOption() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--test", Collections.singleton("--test"), Collections.singleton("--"));
        cmd.addOption(opt);
        assertTrue("Option should be present", cmd.hasOption(opt));
        assertSame("getOption should return the option", opt, cmd.getOption("--test"));
        assertTrue("getOptions should contain the option", cmd.getOptions().contains(opt));
    }

    @Test(timeout = 4000)
    public void testAddOptionParentPropagation() {
        // Defect: parent options are not added automatically
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option parent = new OptionStub("--parent", Collections.singleton("--parent"), Collections.singleton("--"));
        Option child = new OptionStub("--child", Collections.singleton("--child"), Collections.singleton("--"), parent);
        cmd.addOption(child);
        // The parent should also be added (defect: currently not)
        assertTrue("Parent option should be present after adding child", cmd.hasOption(parent));
        assertTrue("getOptions should contain parent", cmd.getOptions().contains(parent));
    }

    @Test(timeout = 4000)
    public void testGetOptionsOrder() {
        // Defect: rootOption not added, and order may be wrong
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt1 = new OptionStub("--first", Collections.singleton("--first"), Collections.singleton("--"));
        Option opt2 = new OptionStub("--second", Collections.singleton("--second"), Collections.singleton("--"));
        cmd.addOption(opt1);
        cmd.addOption(opt2);
        List<Option> options = cmd.getOptions();
        assertEquals("Options list should have 2 elements", 2, options.size());
        assertEquals("First option should be opt1", opt1, options.get(0));
        assertEquals("Second option should be opt2", opt2, options.get(1));
    }

    @Test(timeout = 4000)
    public void testAddValueWithArgument() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Argument arg = new ArgumentStub();
        cmd.addValue(arg, "value1");
        assertTrue("Argument should be added as option", cmd.hasOption(arg));
        List values = cmd.getValues(arg, null);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("Value should be 'value1'", "value1", values.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueMultiple() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addValue(opt, "a");
        cmd.addValue(opt, "b");
        List values = cmd.getValues(opt, null);
        assertEquals("Should have two values", 2, values.size());
        assertEquals("First value", "a", values.get(0));
        assertEquals("Second value", "b", values.get(1));
    }

    @Test(timeout = 4000)
    public void testAddSwitch() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--verbose", Collections.singleton("--verbose"), Collections.singleton("--"));
        cmd.addSwitch(opt, true);
        assertTrue("Switch should be present", cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testAddSwitchDuplicate() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--verbose", Collections.singleton("--verbose"), Collections.singleton("--"));
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false); // should throw
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGetValuesNullDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        List values = cmd.getValues(opt, null);
        assertEquals("Should return empty list", Collections.EMPTY_LIST, values);
    }

    @Test(timeout = 4000)
    public void testGetValuesEmptyDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        List values = cmd.getValues(opt, Collections.emptyList());
        assertEquals("Should return empty list", Collections.EMPTY_LIST, values);
    }

    @Test(timeout = 4000)
    public void testGetValuesDefaultFallback() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        List defaults = Arrays.asList("d1", "d2");
        cmd.setDefaultValues(opt, defaults);
        List values = cmd.getValues(opt, null);
        assertEquals("Should return default values", defaults, values);
    }

    @Test(timeout = 4000)
    public void testGetValuesDefaultAugment() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addValue(opt, "v1");
        List defaults = Arrays.asList("d1", "d2");
        cmd.setDefaultValues(opt, defaults);
        List values = cmd.getValues(opt, null);
        assertEquals("Should have 2 values (v1 + d2)", 2, values.size());
        assertEquals("First value from command line", "v1", values.get(0));
        assertEquals("Second value from defaults", "d2", values.get(1));
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValuesEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        List values = cmd.getUndefaultedValues(opt);
        assertEquals("Should return empty list", Collections.EMPTY_LIST, values);
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValuesWithValues() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addValue(opt, "x");
        List values = cmd.getUndefaultedValues(opt);
        assertEquals("Should have one value", 1, values.size());
        assertEquals("x", values.get(0));
    }

    @Test(timeout = 4000)
    public void testGetSwitchNullDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        Boolean result = cmd.getSwitch(opt, null);
        assertNull("Should return null", result);
    }

    @Test(timeout = 4000)
    public void testGetSwitchDefaultFallback() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        Boolean result = cmd.getSwitch(opt, null);
        assertTrue("Should return default switch true", result);
    }

    @Test(timeout = 4000)
    public void testGetSwitchMethodDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        Boolean result = cmd.getSwitch(opt, Boolean.FALSE);
        assertFalse("Should return method default false", result);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testGetOptionsRootNotAdded() {
        // Defect: rootOption is not added to options list
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        List<Option> options = cmd.getOptions();
        assertTrue("Options list should be empty because rootOption is not added", options.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetOptionsWithMultipleOptions() {
        // Defect: ordering may be wrong if parent not added
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option parent = new OptionStub("--parent", Collections.singleton("--parent"), Collections.singleton("--"));
        Option child = new OptionStub("--child", Collections.singleton("--child"), Collections.singleton("--"), parent);
        cmd.addOption(child);
        // Expected: both parent and child should be in options list (parent first if added)
        List<Option> options = cmd.getOptions();
        // Defective version: only child is present
        assertTrue("Options should contain parent", options.contains(parent));
        assertTrue("Options should contain child", options.contains(child));
        // Order: parent should appear before child (since parent added first via propagation)
        // But defect doesn't add parent, so this will fail
        assertEquals("Parent should be first", parent, options.get(0));
        assertEquals("Child should be second", child, options.get(1));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testAddSwitchDuplicateThrows() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addSwitch(opt, true);
        cmd.addSwitch(opt, false);
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithPrefix() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        assertTrue("--foo should look like option", cmd.looksLikeOption("--foo"));
        assertTrue("-f should look like option", cmd.looksLikeOption("-f"));
        assertFalse("foo should not look like option", cmd.looksLikeOption("foo"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionNoPrefixes() {
        Set<String> emptyPrefixes = Collections.emptySet();
        Option root = new OptionStub("--root", Collections.singleton("--root"), emptyPrefixes);
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList<>());
        assertFalse("With no prefixes, nothing looks like option", cmd.looksLikeOption("--anything"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToString() {
        List<String> args = Arrays.asList("arg1", "arg with space", "arg3");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), args);
        String expected = "arg1 \"arg with space\" arg3";
        assertEquals("toString should quote arguments with spaces", expected, cmd.toString());
    }

    @Test(timeout = 4000)
    public void testToStringEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        assertEquals("Empty normalised should produce empty string", "", cmd.toString());
    }

    @Test(timeout = 4000)
    public void testGetNormalised() {
        List<String> args = Arrays.asList("a", "b");
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), args);
        List normalised = cmd.getNormalised();
        assertEquals("Should return same list", args, normalised);
        // Verify unmodifiable
        try {
            normalised.add("c");
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesNull() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.setDefaultValues(opt, null);
        // Should not throw, and getValues should return empty
        List values = cmd.getValues(opt, null);
        assertEquals("Should be empty", Collections.EMPTY_LIST, values);
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchNull() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.setDefaultSwitch(opt, null);
        Boolean result = cmd.getSwitch(opt, null);
        assertNull("Should be null", result);
    }

    @Test(timeout = 4000)
    public void testAddProperty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addProperty(opt, "key", "value");
        assertEquals("Should retrieve property", "value", cmd.getProperty(opt, "key", null));
    }

    @Test(timeout = 4000)
    public void testGetPropertyDefault() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        assertEquals("Should return default", "default", cmd.getProperty(opt, "nonexistent", "default"));
    }

    @Test(timeout = 4000)
    public void testGetPropertiesEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        Set props = cmd.getProperties(opt);
        assertTrue("Should be empty set", props.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetPropertiesNonEmpty() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--opt", Collections.singleton("--opt"), Collections.singleton("--"));
        cmd.addProperty(opt, "k1", "v1");
        cmd.addProperty(opt, "k2", "v2");
        Set props = cmd.getProperties(opt);
        assertEquals("Should have 2 keys", 2, props.size());
        assertTrue(props.contains("k1"));
        assertTrue(props.contains("k2"));
    }

    @Test(timeout = 4000)
    public void testGetOptionTriggers() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--test", new HashSet<>(Arrays.asList("--test", "-t")), Collections.singleton("--"));
        cmd.addOption(opt);
        Set triggers = cmd.getOptionTriggers();
        assertTrue("Should contain --test", triggers.contains("--test"));
        assertTrue("Should contain -t", triggers.contains("-t"));
    }

    @Test(timeout = 4000)
    public void testHasOptionFalse() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        Option opt = new OptionStub("--nonexistent", Collections.singleton("--nonexistent"), Collections.singleton("--"));
        assertFalse("Should not have option", cmd.hasOption(opt));
    }

    @Test(timeout = 4000)
    public void testGetOptionNull() {
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(createRootOption(), new ArrayList<>());
        assertNull("Should return null for unknown trigger", cmd.getOption("--unknown"));
    }
}