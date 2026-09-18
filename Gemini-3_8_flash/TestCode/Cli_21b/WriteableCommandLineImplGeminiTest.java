/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli2.commandline.WriteableCommandLineImpl
 *
 * Core Decision Branches & Methods Analyzed:
 * 1. Constructor:
 *    - rootOption.getPrefixes() extraction, arguments reference storage.
 * 2. addOption(Option):
 *    - options.add, nameToOption mapping for preferredName & triggers.
 *    - Parent traversal while loop: parent != null && !options.contains(parent).
 * 3. addValue(Option, Object):
 *    - Branch: option instanceof Argument -> triggers addOption(option).
 *    - Branch: valueList == null -> instantiate and register new ArrayList.
 * 4. addSwitch(Option, boolean):
 *    - Branch: switches.containsKey(option) -> throws IllegalStateException (SWITCH_ALREADY_SET).
 *    - Branch: value == true ? Boolean.TRUE : Boolean.FALSE.
 * 5. hasOption(Option):
 *    - options.contains(option) -> true/false.
 * 6. getOption(String trigger):
 *    - nameToOption.get(trigger) lookup -> returns matching Option or null.
 * 7. getValues(Option, List defaultValues):
 *    - Branch: defaultValues == null || defaultValues.isEmpty() -> fallback to this.defaultValues.get(option).
 *    - Branch: defaultValues != null && !defaultValues.isEmpty():
 *        - Sub-branch: valueList == null || valueList.isEmpty() -> valueList = defaultValues.
 *        - Sub-branch: defaultValues.size() > valueList.size() -> pad with remaining defaultValues.
 *    - Return check: valueList == null ? EMPTY_LIST : valueList.
 * 8. getUndefaultedValues(Option):
 *    - Branch: valueList == null -> EMPTY_LIST vs populated list.
 * 9. getSwitch(Option, Boolean defaultValue):
 *    - Cascading fallback: switches.get(option) -> defaultValue -> defaultSwitches.get(option) -> null.
 * 10. Properties Handling (addProperty, getProperty, getProperties):
 *    - Overloads with explicit Option vs implicit default PropertyOption.
 *    - Branch: properties == null -> return defaultValue or EMPTY_SET.
 * 11. looksLikeOption(String trigger):
 *    - Prefix iteration: trigger.startsWith(prefix) -> returns true.
 *    - Defect CLI-150 / BugCLI150Test: Negative numbers (e.g. "-42") matching '-' prefix
 *      are falsely recognized as options when parsing arguments.
 * 12. toString():
 *    - Formats normalized arguments with quotes if containing spaces, separated by single space.
 * 13. State mutation:
 *    - setDefaultValues (null vs populated), setDefaultSwitch (null vs Boolean).
 *    - getOptions(), getOptionTriggers(), getNormalised() immutability checks.
 */

package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.option.PropertyOption;

import org.junit.Test;
import static org.junit.Assert.*;

public class WriteableCommandLineImplGeminiTest {

    // Helper implementation of Option for white-box unit testing
    private static class DummyOption implements Option {
        private final String preferredName;
        private final Set triggers;
        private final Set prefixes;
        private Option parent;

        public DummyOption(String preferredName, Set triggers, Set prefixes) {
            this.preferredName = preferredName;
            this.triggers = triggers != null ? triggers : Collections.emptySet();
            this.prefixes = prefixes != null ? prefixes : Collections.emptySet();
        }

        public void setParent(Option parent) {
            this.parent = parent;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String arg) { return true; }
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator args) { return true; }
        public void process(WriteableCommandLine commandLine, ListIterator args) throws OptionException {}
        public void validate(WriteableCommandLine commandLine) throws OptionException {}
        public void appendUsage(StringBuffer buffer, Set helpSettings, java.util.Comparator comp) {}
        public String getDescription() { return "DummyOption"; }
        public List helpLines(int depth, Set helpSettings, java.util.Comparator comp) { return Collections.emptyList(); }
        public Option getParent() { return this.parent; }
        public void setParent(Option parent, boolean dummy) { this.parent = parent; }
        public String getPreferredName() { return this.preferredName; }
        public String getId() { return this.preferredName; }
        public Set getPrefixes() { return this.prefixes; }
        public Set getTriggers() { return this.triggers; }
        public boolean isRequired() { return false; }
        public boolean checkPrefixes(Set prefixes) { return true; }
    }

    // Helper implementation of Argument for white-box unit testing
    private static class DummyArgument extends DummyOption implements Argument {
        public DummyArgument(String preferredName) {
            super(preferredName, Collections.singleton(preferredName), Collections.emptySet());
        }
        public Object getInitialValue() { return null; }
        public void processValues(WriteableCommandLine commandLine, ListIterator args, Option option) throws OptionException {}
        public void defaultValues(WriteableCommandLine commandLine, Option option) {}
        public int getMinimum() { return 0; }
        public int getMaximum() { return 1; }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndHasOptionSimple() {
        DummyOption root = new DummyOption("root", Collections.singleton("root"), Collections.singleton("--"));
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption opt = new DummyOption("opt", Collections.singleton("-o"), Collections.emptySet());
        assertFalse(cmd.hasOption(opt));
        assertNull(cmd.getOption("-o"));

        cmd.addOption(opt);
        assertTrue(cmd.hasOption(opt));
        assertSame(opt, cmd.getOption("-o"));
        assertSame(opt, cmd.getOption("opt"));
        assertTrue(cmd.getOptions().contains(opt));
        assertTrue(cmd.getOptionTriggers().contains("-o"));
        assertTrue(cmd.getOptionTriggers().contains("opt"));
    }

    @Test(timeout = 4000)
    public void testAddOptionWithParentHierarchy() {
        DummyOption grandParent = new DummyOption("grandParent", Collections.emptySet(), Collections.emptySet());
        DummyOption parent = new DummyOption("parent", Collections.emptySet(), Collections.emptySet());
        DummyOption child = new DummyOption("child", Collections.emptySet(), Collections.emptySet());

        parent.setParent(grandParent);
        child.setParent(parent);

        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addOption(child);

        assertTrue(cmd.hasOption(child));
        assertTrue(cmd.hasOption(parent));
        assertTrue(cmd.hasOption(grandParent));
        List options = cmd.getOptions();
        assertEquals(3, options.size());
        assertEquals(child, options.get(0));
        assertEquals(parent, options.get(1));
        assertEquals(grandParent, options.get(2));
    }

    @Test(timeout = 4000)
    public void testAddValueArgumentInstanceTriggersAddOption() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyArgument arg = new DummyArgument("file");
        assertFalse(cmd.hasOption(arg));

        cmd.addValue(arg, "data.txt");
        assertTrue(cmd.hasOption(arg));
        List vals = cmd.getValues(arg, null);
        assertEquals(1, vals.size());
        assertEquals("data.txt", vals.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueRegularOptionDoesNotTriggerAddOption() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption regularOpt = new DummyOption("custom", Collections.emptySet(), Collections.emptySet());
        cmd.addValue(regularOpt, "value1");
        cmd.addValue(regularOpt, "value2");

        assertFalse(cmd.hasOption(regularOpt));
        List vals = cmd.getValues(regularOpt, null);
        assertEquals(Arrays.asList("value1", "value2"), vals);
    }

    @Test(timeout = 4000)
    public void testAddAndGetSwitch() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption switchOpt = new DummyOption("verbose", Collections.emptySet(), Collections.emptySet());
        cmd.addSwitch(switchOpt, true);

        assertTrue(cmd.hasOption(switchOpt));
        assertEquals(Boolean.TRUE, cmd.getSwitch(switchOpt, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testAddSwitchAlreadySetThrowsException() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption switchOpt = new DummyOption("flag", Collections.emptySet(), Collections.emptySet());
        cmd.addSwitch(switchOpt, false);

        try {
            cmd.addSwitch(switchOpt, true);
            fail("Expected IllegalStateException when switch is already set");
        } catch (IllegalStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testPropertyOperations() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addProperty("key1", "val1");
        assertEquals("val1", cmd.getProperty("key1"));
        assertNull(cmd.getProperty("nonexistent"));
        assertEquals("fallback", cmd.getProperty(new PropertyOption(), "nonexistent", "fallback"));

        Set props = cmd.getProperties();
        assertEquals(1, props.size());
        assertTrue(props.contains("key1"));

        // Custom Option property mapping
        DummyOption custom = new DummyOption("custom", Collections.emptySet(), Collections.emptySet());
        assertEquals("defVal", cmd.getProperty(custom, "propA", "defVal"));
        assertTrue(cmd.getProperties(custom).isEmpty());

        cmd.addProperty(custom, "propA", "propValA");
        assertEquals("propValA", cmd.getProperty(custom, "propA", "defVal"));
        assertEquals(1, cmd.getProperties(custom).size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValuesWithDefaultValuesPadding() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption opt = new DummyOption("target", Collections.emptySet(), Collections.emptySet());

        // Case 1: valueList is empty/null, defaultValues supplied
        List defaults1 = Arrays.asList("def1", "def2");
        List result1 = cmd.getValues(opt, defaults1);
        assertEquals(defaults1, result1);

        // Case 2: defaultValues specified in instance via setDefaultValues
        cmd.setDefaultValues(opt, Arrays.asList("inst1", "inst2", "inst3"));
        List result2 = cmd.getValues(opt, null);
        assertEquals(Arrays.asList("inst1", "inst2", "inst3"), result2);

        // Case 3: valueList exists, defaultValues size > valueList size (padding test)
        cmd.addValue(opt, "actual1");
        List result3 = cmd.getValues(opt, Arrays.asList("p1", "p2", "p3"));
        assertEquals(Arrays.asList("actual1", "p2", "p3"), result3);

        // Case 4: valueList size >= defaultValues size (no padding occurs)
        cmd.addValue(opt, "actual2");
        cmd.addValue(opt, "actual3");
        List result4 = cmd.getValues(opt, Arrays.asList("d1", "d2"));
        assertEquals(Arrays.asList("actual1", "actual2", "actual3"), result4);

        // Case 5: clear defaults with null
        cmd.setDefaultValues(opt, null);
        DummyOption optNoVal = new DummyOption("noVal", Collections.emptySet(), Collections.emptySet());
        assertEquals(Collections.EMPTY_LIST, cmd.getValues(optNoVal, Collections.EMPTY_LIST));
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValues() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption opt = new DummyOption("opt", Collections.emptySet(), Collections.emptySet());
        cmd.setDefaultValues(opt, Collections.singletonList("defaultOnly"));

        assertEquals(Collections.EMPTY_LIST, cmd.getUndefaultedValues(opt));

        cmd.addValue(opt, "realValue");
        assertEquals(Collections.singletonList("realValue"), cmd.getUndefaultedValues(opt));
    }

    @Test(timeout = 4000)
    public void testGetSwitchCascadingDefaults() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption sw = new DummyOption("sw", Collections.emptySet(), Collections.emptySet());

        // Level 1: no switch, no default supplied, no instance default -> null
        assertNull(cmd.getSwitch(sw, (Boolean) null));

        // Level 2: fallback to instance defaultSwitch
        cmd.setDefaultSwitch(sw, Boolean.FALSE);
        assertEquals(Boolean.FALSE, cmd.getSwitch(sw, (Boolean) null));

        // Level 3: method supplied default overrides instance default
        assertEquals(Boolean.TRUE, cmd.getSwitch(sw, Boolean.TRUE));

        // Clear instance default
        cmd.setDefaultSwitch(sw, null);
        assertNull(cmd.getSwitch(sw, (Boolean) null));

        // Level 4: added switch takes precedence over all defaults
        cmd.addSwitch(sw, true);
        assertEquals(Boolean.TRUE, cmd.getSwitch(sw, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testToStringNormalisedFormatting() {
        List args = new ArrayList();
        args.add("--file");
        args.add("my document.txt");
        args.add("-v");

        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, args);

        String result = cmd.toString();
        assertEquals("--file \"my document.txt\" -v", result);
        assertEquals(args, cmd.getNormalised());
    }

    @Test(timeout = 4000)
    public void testToStringEmpty() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, Collections.emptyList());

        assertEquals("", cmd.toString());
        assertTrue(cmd.getNormalised().isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Bug CLI-150: Negative Numbers)
    // =========================================================================

    /**
     * Target Defect CLI-150:
     * When processing an option expecting a numeric argument like "--num -42",
     * looksLikeOption("-42") returning true causes the parser to treat "-42"
     * as an unexpected option trigger rather than a value argument.
     * WriteableCommandLineImpl.looksLikeOption() should not identify "-42"
     * as an option trigger when only registered triggers/prefixes exist.
     */
    @Test(timeout = 4000)
    public void testDefectCLI150NegativeNumberDoesNotLookLikeOption() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        DummyOption root = new DummyOption("root", Collections.emptySet(), prefixes);
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // Regular trigger should look like an option
        assertTrue(cmd.looksLikeOption("--num"));
        assertTrue(cmd.looksLikeOption("-n"));

        // A negative number starts with '-' which is in prefixes.
        // However, it is an argument value (-42), NOT an actual option.
        // In the presence of Bug CLI-150, looksLikeOption("-42") returns true.
        // Here we assert that "-42" does NOT look like an option.
        assertFalse("CLI-150: Negative number should not be recognized as an option trigger",
                    cmd.looksLikeOption("-42"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionBoundaryConditions() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");
        prefixes.add("+");

        DummyOption root = new DummyOption("root", Collections.emptySet(), prefixes);
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertTrue(cmd.looksLikeOption("-a"));
        assertTrue(cmd.looksLikeOption("--long-arg"));
        assertTrue(cmd.looksLikeOption("+toggle"));
        assertFalse(cmd.looksLikeOption("plainValue"));
        assertFalse(cmd.looksLikeOption("value-with-hyphen-inside"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetOptionsAndTriggersAreUnmodifiable() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        DummyOption opt = new DummyOption("test", Collections.singleton("testTrigger"), Collections.emptySet());
        cmd.addOption(opt);

        List options = cmd.getOptions();
        try {
            options.add(new DummyOption("illegal", Collections.emptySet(), Collections.emptySet()));
            fail("Expected UnsupportedOperationException when mutating getOptions()");
        } catch (UnsupportedOperationException expected) {}

        Set triggers = cmd.getOptionTriggers();
        try {
            triggers.add("illegalTrigger");
            fail("Expected UnsupportedOperationException when mutating getOptionTriggers()");
        } catch (UnsupportedOperationException expected) {}

        List normalised = cmd.getNormalised();
        try {
            normalised.add("illegalArg");
            fail("Expected UnsupportedOperationException when mutating getNormalised()");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test(timeout = 4000)
    public void testGetPropertiesIsUnmodifiable() {
        DummyOption root = new DummyOption("root", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addProperty("foo", "bar");
        Set propKeys = cmd.getProperties();
        try {
            propKeys.add("baz");
            fail("Expected UnsupportedOperationException when mutating getProperties()");
        } catch (UnsupportedOperationException expected) {}
    }

    // =========================================================================
    // Partition E: Parser & Option Builder Contract Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testParserIntegrationWithNegativeNumberCLI150() throws OptionException {
        // Construct options using CLI2 Builders
        Argument numArg = new ArgumentBuilder()
                .withName("num")
                .withMinimum(1)
                .withMaximum(1)
                .create();

        Option numOption = new DefaultOptionBuilder()
                .withLongName("num")
                .withShortName("n")
                .withArgument(numArg)
                .create();

        GroupBuilder gBuilder = new GroupBuilder();
        gBuilder.withOption(numOption);
        Option rootGroup = gBuilder.create();

        Parser parser = new Parser();
        parser.setGroup(rootGroup);

        // Parsing "--num -42": "-42" must be accepted as the argument value of --num
        org.apache.commons.cli2.CommandLine cl = parser.parse(new String[]{"--num", "-42"});
        assertTrue(cl.hasOption(numOption));
        assertEquals("-42", cl.getValue(numOption));
    }
}