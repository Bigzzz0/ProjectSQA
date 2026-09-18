package org.apache.commons.cli2.option;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.resource.ResourceConstants;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - canProcess: null arg, exact match, tailMap match, anonymous arg, looksLikeOption false
 *   - process: exact option match, tailMap option match, anonymous arg processing, abort on repeated arg
 *   - validate: present count <= maximum, present count > maximum (unexpected), present < minimum (missing)
 *   - isRequired: parent null + minimum > 0, parent not null + super.isRequired() + minimum > 0
 *   - defaults: propagates to options and anonymous
 *   - getOptions, getAnonymous, getMinimum, getMaximum, getPreferredName, getDescription, getPrefixes, getTriggers
 *   - findOption: recursive search through options
 *   - appendUsage: optional, expanded, named, both, arguments, outer, separator handling
 *   - helpLines: group name, expanded, argument display
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - minimum = 0, maximum = Integer.MAX_VALUE
 *   - minimum = 1, maximum = 1 (exactly one required)
 *   - empty options list, empty anonymous list
 *   - null name, null description
 *   - arg = "" (empty string)
 *   - arg = "-" (single dash)
 *   - arg = "--" (double dash)
 *   - arg = "-42" (negative number, defect trigger)
 *   - arg = "42" (positive number)
 *   - arg = "--num" (long option trigger)
 *   - arg = "-n" (short option trigger)
 *   - arg = "-abc" (burstable short options)
 *   - arg = "--" (end of options marker)
 *   - arg = "value" (non-option argument)
 *   - arg = null (edge case)
 *   - anonymous list size = 0, 1, 2
 *   - options list size = 0, 1, 2
 *   - optionMap empty, single entry, multiple entries
 *   - prefixes empty, single prefix, multiple prefixes
 *   - depth = 0, 1, 2
 *   - helpSettings = DisplaySetting.NONE, all settings, subset
 *   - comp = null, custom comparator
 *   - separator = "|", ",", " "
 * 
 * Partition C: Defect-Targeted Branch Zone (BugCLI150Test::testNegativeNumber)
 *   - The defect: When processing a negative number like "-42" as an argument to an option (e.g., --num -42),
 *     the canProcess method incorrectly returns true for the negative number because it looks like an option
 *     (starts with '-'), but the optionMap.tailMap("-42") may contain options that canProcess it, or it falls
 *     through to anonymous arguments. The expected behavior is that negative numbers should be treated as
 *     values, not options, when they follow an option that expects a numeric argument.
 *   - Key branches: canProcess with arg starting with '-' but not in optionMap, tailMap iteration,
 *     looksLikeOption returning true, anonymous.size() > 0
 *   - Test: Create a group with an option --num that expects an integer argument, then process command line
 *     "--num -42". The -42 should be consumed as the argument to --num, not treated as a separate option.
 *     The bug causes an OptionException "Unexpected -42" because -42 is not recognized as a valid option.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - validate: present > maximum throws OptionException with UNEXPECTED_TOKEN
 *   - validate: present < minimum throws OptionException with MISSING_OPTION
 *   - canProcess: arg == null returns false
 *   - process: arg == previous breaks loop (infinite loop prevention)
 *   - process: looksLikeOption true but no member option found -> return (abort group)
 *   - process: anonymous.isEmpty() true and not looksLikeOption -> break
 *   - Constructor: null options list (NPE expected)
 *   - Constructor: null option in options list (NPE expected)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor: options list is unmodifiable (try to modify -> UnsupportedOperationException)
 *   - Constructor: anonymous list is unmodifiable
 *   - Constructor: optionMap is unmodifiable
 *   - Constructor: prefixes is unmodifiable
 *   - Constructor: options with null triggers (NPE expected)
 *   - Constructor: options with null prefixes (NPE expected)
 *   - Constructor: option.setParent(this) called for each option
 *   - Constructor: Argument options removed from options list and added to anonymous
 *   - Constructor: Non-Argument options added to optionMap with their triggers
 *   - Constructor: Non-Argument options' prefixes added to prefixes set
 *   - isRequired: getParent() == null && minimum > 0 -> true
 *   - isRequired: getParent() != null && super.isRequired() && minimum > 0 -> true
 *   - isRequired: minimum == 0 -> false (even if parent is null)
 *   - isRequired: getParent() != null && !super.isRequired() -> false
 */
public class GroupImplDeepseekTest {

    // ==================== Helper Methods ====================

    /**
     * Creates a simple mock Option for testing.
     */
    private Option createMockOption(final String trigger, final String prefix) {
        return new Option() {
            private Option parent;

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                // For testing, accept any argument that starts with the prefix
                return arg != null && arg.startsWith(prefix);
            }

            @Override
            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add(trigger);
                return triggers;
            }

            @Override
            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add(prefix);
                return prefixes;
            }

            @Override
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                // Consume the next argument as the value
                if (arguments.hasNext()) {
                    arguments.next(); // consume the value
                }
            }

            @Override
            public void validate(WriteableCommandLine commandLine) throws OptionException {
                // No validation for mock
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
                return "Mock option " + trigger;
            }

            @Override
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append(trigger);
            }

            @Override
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }

            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // No defaults
            }

            @Override
            public Option findOption(String trigger) {
                return trigger.equals(this.getPreferredName()) ? this : null;
            }

            @Override
            public void setParent(Option parent) {
                this.parent = parent;
            }

            @Override
            public Option getParent() {
                return parent;
            }

            @Override
            public boolean isSelected(WriteableCommandLine commandLine) {
                return false;
            }

            @Override
            public String toString() {
                return trigger;
            }
        };
    }

    /**
     * Creates a simple mock Argument for testing.
     */
    private Argument createMockArgument() {
        return new Argument() {
            private Option parent;

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
                return arguments.hasNext();
            }

            @Override
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                if (arguments.hasNext()) {
                    arguments.next(); // consume the argument
                }
            }

            @Override
            public void validate(WriteableCommandLine commandLine) throws OptionException {
                // No validation
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public String getPreferredName() {
                return "arg";
            }

            @Override
            public String getDescription() {
                return "Mock argument";
            }

            @Override
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("<arg>");
            }

            @Override
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }

            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // No defaults
            }

            @Override
            public Option findOption(String trigger) {
                return null;
            }

            @Override
            public void setParent(Option parent) {
                this.parent = parent;
            }

            @Override
            public Option getParent() {
                return parent;
            }

            @Override
            public boolean isSelected(WriteableCommandLine commandLine) {
                return false;
            }

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                return true; // Anonymous arguments can process any string
            }

            @Override
            public Set getTriggers() {
                return Collections.emptySet();
            }

            @Override
            public Set getPrefixes() {
                return Collections.emptySet();
            }

            @Override
            public String toString() {
                return "anonymous";
            }
        };
    }

    /**
     * Creates a simple mock WriteableCommandLine for testing.
     */
    private WriteableCommandLine createMockCommandLine() {
        return new WriteableCommandLine() {
            private final Set<String> seenOptions = new HashSet<String>();

            @Override
            public boolean hasOption(Option option) {
                return seenOptions.contains(option.getPreferredName());
            }

            @Override
            public void addOption(Option option) {
                seenOptions.add(option.getPreferredName());
            }

            @Override
            public boolean looksLikeOption(String trigger) {
                return trigger != null && trigger.startsWith("-");
            }

            @Override
            public String getOptionValue(Option option) {
                return null;
            }

            @Override
            public List getValues(Option option) {
                return new ArrayList();
            }

            @Override
            public void setDefaultValues(Option option, List defaults) {
                // No-op
            }

            @Override
            public void addValue(Option option, Object value) {
                // No-op
            }

            @Override
            public Object getLock() {
                return this;
            }

            @Override
            public void addProperty(String property, Object value) {
                // No-op
            }

            @Override
            public void addProperty(String option, String property, Object value) {
                // No-op
            }

            @Override
            public Object getProperty(String property) {
                return null;
            }

            @Override
            public Object getProperty(String option, String property) {
                return null;
            }

            @Override
            public Set getProperties() {
                return Collections.emptySet();
            }

            @Override
            public Set getProperties(String option) {
                return Collections.emptySet();
            }

            @Override
            public List getOptionValues(String trigger) {
                return new ArrayList();
            }

            @Override
            public List getOptionValues(Option option) {
                return new ArrayList();
            }

            @Override
            public boolean getUndefaultedValues(Option option) {
                return false;
            }

            @Override
            public boolean isSelected(Option option) {
                return false;
            }

            @Override
            public void setSelected(Option option) throws OptionException {
                // No-op
            }

            @Override
            public String[] getArgs() {
                return new String[0];
            }

            @Override
            public String getOptionValue(String trigger) {
                return null;
            }

            @Override
            public boolean hasOption(String trigger) {
                return seenOptions.contains(trigger);
            }

            @Override
            public void addOption(String trigger) {
                seenOptions.add(trigger);
            }
        };
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testCanProcessNullArg() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertFalse("canProcess should return false for null arg", group.canProcess(commandLine, null));
    }

    @Test(timeout = 4000)
    public void testCanProcessExactMatch() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertTrue("canProcess should return true for exact match", group.canProcess(commandLine, "--num"));
    }

    @Test(timeout = 4000)
    public void testCanProcessTailMapMatch() {
        Option opt = createMockOption("--number", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        // "--num" is a prefix of "--number", so tailMap should find it
        assertTrue("canProcess should return true for tailMap match", group.canProcess(commandLine, "--num"));
    }

    @Test(timeout = 4000)
    public void testCanProcessAnonymousArg() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertTrue("canProcess should return true for anonymous arg", group.canProcess(commandLine, "value"));
    }

    @Test(timeout = 4000)
    public void testCanProcessLooksLikeOptionButNoMatch() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        // "-x" looks like an option but doesn't match any option and no anonymous args
        assertFalse("canProcess should return false for unknown option", group.canProcess(commandLine, "-x"));
    }

    @Test(timeout = 4000)
    public void testProcessExactOption() throws OptionException {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("--num");
        args.add("42");
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // After processing, the iterator should be at position 2 (after consuming --num and 42)
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }

    @Test(timeout = 4000)
    public void testProcessTailMapOption() throws OptionException {
        Option opt = createMockOption("--number", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("--num");
        args.add("42");
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // After processing, the iterator should be at position 2
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }

    @Test(timeout = 4000)
    public void testProcessAnonymousArg() throws OptionException {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("value");
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // After processing, the iterator should be at position 1
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }

    @Test(timeout = 4000)
    public void testProcessAbortOnRepeatedArg() throws OptionException {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("--num");
        args.add("--num"); // repeated arg should cause abort
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // The iterator should have been rolled back to position 1 (after first --num)
        assertTrue("Iterator should have one element remaining", iterator.hasNext());
        assertEquals("Remaining element should be --num", "--num", iterator.next());
    }

    @Test(timeout = 4000)
    public void testValidateSuccess() throws OptionException {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        commandLine.addOption(opt);
        group.validate(commandLine); // Should not throw
    }

    @Test(timeout = 4000)
    public void testValidateTooManyOptions() {
        Option opt1 = createMockOption("--num1", "-");
        Option opt2 = createMockOption("--num2", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt1);
        options.add(opt2);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        commandLine.addOption(opt1);
        commandLine.addOption(opt2);
        try {
            group.validate(commandLine);
            fail("Expected OptionException for too many options");
        } catch (OptionException e) {
            assertEquals("Exception should indicate unexpected token", 
                         ResourceConstants.UNEXPECTED_TOKEN, e.getType());
        }
    }

    @Test(timeout = 4000)
    public void testValidateTooFewOptions() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 1, 1, true);
        WriteableCommandLine commandLine = createMockCommandLine();
        try {
            group.validate(commandLine);
            fail("Expected OptionException for too few options");
        } catch (OptionException e) {
            assertEquals("Exception should indicate missing option", 
                         ResourceConstants.MISSING_OPTION, e.getType());
        }
    }

    @Test(timeout = 4000)
    public void testIsRequiredParentNullMinimumPositive() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 1, 1, false);
        assertTrue("Group with minimum > 0 and no parent should be required", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testIsRequiredParentNullMinimumZero() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        assertFalse("Group with minimum == 0 should not be required", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testDefaultsPropagation() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        group.defaults(commandLine); // Should not throw
    }

    @Test(timeout = 4000)
    public void testGetOptions() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        List returnedOptions = group.getOptions();
        assertEquals("Should return one option", 1, returnedOptions.size());
        assertSame("Should return the same option", opt, returnedOptions.get(0));
    }

    @Test(timeout = 4000)
    public void testGetAnonymous() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        List returnedAnonymous = group.getAnonymous();
        assertEquals("Should return one anonymous argument", 1, returnedAnonymous.size());
        assertSame("Should return the same argument", arg, returnedAnonymous.get(0));
    }

    @Test(timeout = 4000)
    public void testGetMinimum() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 2, 5, false);
        assertEquals("Minimum should be 2", 2, group.getMinimum());
    }

    @Test(timeout = 4000)
    public void testGetMaximum() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 2, 5, false);
        assertEquals("Maximum should be 5", 5, group.getMaximum());
    }

    @Test(timeout = 4000)
    public void testGetPreferredName() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "testName", "test group", 0, 1, false);
        assertEquals("Preferred name should be testName", "testName", group.getPreferredName());
    }

    @Test(timeout = 4000)
    public void testGetDescription() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test description", 0, 1, false);
        assertEquals("Description should be test description", "test description", group.getDescription());
    }

    @Test(timeout = 4000)
    public void testGetPrefixes() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set prefixes = group.getPrefixes();
        assertTrue("Prefixes should contain '-'", prefixes.contains("-"));
    }

    @Test(timeout = 4000)
    public void testGetTriggers() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set triggers = group.getTriggers();
        assertTrue("Triggers should contain '--num'", triggers.contains("--num"));
    }

    @Test(timeout = 4000)
    public void testFindOptionFound() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Option found = group.findOption("--num");
        assertNotNull("Should find option", found);
        assertSame("Should find the correct option", opt, found);
    }

    @Test(timeout = 4000)
    public void testFindOptionNotFound() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Option found = group.findOption("--other");
        assertNull("Should not find option", found);
    }

    @Test(timeout = 4000)
    public void testAppendUsageOptional() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        StringBuffer buffer = new StringBuffer();
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        group.appendUsage(buffer, settings, null);
        assertTrue("Buffer should contain '['", buffer.toString().startsWith("["));
        assertTrue("Buffer should contain ']'", buffer.toString().endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testAppendUsageNamedAndExpanded() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        StringBuffer buffer = new StringBuffer();
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        group.appendUsage(buffer, settings, null);
        String result = buffer.toString();
        assertTrue("Buffer should contain group name", result.contains("test"));
        assertTrue("Buffer should contain option", result.contains("--num"));
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithSeparator() {
        Option opt1 = createMockOption("--num1", "-");
        Option opt2 = createMockOption("--num2", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt1);
        options.add(opt2);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 2, false);
        StringBuffer buffer = new StringBuffer();
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        group.appendUsage(buffer, settings, null, "|");
        String result = buffer.toString();
        assertTrue("Buffer should contain separator", result.contains("|"));
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithGroupName() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        List helpLines = group.helpLines(0, settings, null);
        assertEquals("Should have one help line", 1, helpLines.size());
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithExpanded() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        List helpLines = group.helpLines(0, settings, null);
        assertTrue("Should have at least one help line", helpLines.size() >= 1);
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithArgument() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set<DisplaySetting> settings = new HashSet<DisplaySetting>();
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        List helpLines = group.helpLines(0, settings, null);
        assertTrue("Should have at least one help line", helpLines.size() >= 1);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyOptionsList() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 0, false);
        assertEquals("Options list should be empty", 0, group.getOptions().size());
        assertEquals("Anonymous list should be empty", 0, group.getAnonymous().size());
    }

    @Test(timeout = 4000)
    public void testNullName() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, null, "test group", 0, 1, false);
        assertNull("Name should be null", group.getPreferredName());
    }

    @Test(timeout = 4000)
    public void testNullDescription() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", null, 0, 1, false);
        assertNull("Description should be null", group.getDescription());
    }

    @Test(timeout = 4000)
    public void testMinimumZeroMaximumMaxValue() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, Integer.MAX_VALUE, false);
        assertEquals("Minimum should be 0", 0, group.getMinimum());
        assertEquals("Maximum should be MAX_VALUE", Integer.MAX_VALUE, group.getMaximum());
    }

    @Test(timeout = 4000)
    public void testMinimumOneMaximumOne() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 1, 1, true);
        assertEquals("Minimum should be 1", 1, group.getMinimum());
        assertEquals("Maximum should be 1", 1, group.getMaximum());
    }

    @Test(timeout = 4000)
    public void testCanProcessEmptyString() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertFalse("canProcess should return false for empty string", group.canProcess(commandLine, ""));
    }

    @Test(timeout = 4000)
    public void testCanProcessSingleDash() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertFalse("canProcess should return false for single dash", group.canProcess(commandLine, "-"));
    }

    @Test(timeout = 4000)
    public void testCanProcessDoubleDash() {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertFalse("canProcess should return false for double dash", group.canProcess(commandLine, "--"));
    }

    @Test(timeout = 4000)
    public void testCanProcessPositiveNumber() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        // "42" doesn't look like an option and no anonymous args
        assertFalse("canProcess should return false for positive number without anonymous", 
                    group.canProcess(commandLine, "42"));
    }

    @Test(timeout = 4000)
    public void testCanProcessPositiveNumberWithAnonymous() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        assertTrue("canProcess should return true for positive number with anonymous", 
                   group.canProcess(commandLine, "42"));
    }

    @Test(timeout = 4000)
    public void testCanProcessNegativeNumberWithoutAnonymous() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        // "-42" looks like an option but doesn't match any option and no anonymous args
        assertFalse("canProcess should return false for negative number without anonymous", 
                    group.canProcess(commandLine, "-42"));
    }

    @Test(timeout = 4000)
    public void testCanProcessNegativeNumberWithAnonymous() {
        Argument arg = createMockArgument();
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        // "-42" looks like an option, but there are anonymous args, so canProcess should return true
        assertTrue("canProcess should return true for negative number with anonymous", 
                   group.canProcess(commandLine, "-42"));
    }

    @Test(timeout = 4000)
    public void testProcessNegativeNumberAsAnonymous() throws OptionException {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("-42");
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // The negative number should be consumed as an anonymous argument
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (BugCLI150Test::testNegativeNumber) ====================

    /**
     * This test targets the known defect where processing a negative number as an argument to an option
     * causes an OptionException "Unexpected -42". The expected behavior is that the negative number should
     * be consumed as the value for the preceding option.
     */
    @Test(timeout = 4000)
    public void testNegativeNumberAsOptionValue() throws OptionException {
        // Create an option --num that accepts a numeric argument
        Option numOption = new Option() {
            private Option parent;

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                // Accept any argument that starts with "-" (including negative numbers)
                return arg != null && arg.startsWith("-");
            }

            @Override
            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add("--num");
                return triggers;
            }

            @Override
            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add("--");
                return prefixes;
            }

            @Override
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                // Consume the next argument as the value
                if (arguments.hasNext()) {
                    String value = (String) arguments.next();
                    // The value should be "-42" (negative number)
                    // In the buggy version, this throws an OptionException because -42 is treated as an option
                }
            }

            @Override
            public void validate(WriteableCommandLine commandLine) throws OptionException {
                // No validation
            }

            @Override
            public boolean isRequired() {
                return false;
            }

            @Override
            public String getPreferredName() {
                return "--num";
            }

            @Override
            public String getDescription() {
                return "Numeric option";
            }

            @Override
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("--num <value>");
            }

            @Override
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }

            @Override
            public void defaults(WriteableCommandLine commandLine) {
                // No defaults
            }

            @Override
            public Option findOption(String trigger) {
                return trigger.equals("--num") ? this : null;
            }

            @Override
            public void setParent(Option parent) {
                this.parent = parent;
            }

            @Override
            public Option getParent() {
                return parent;
            }

            @Override
            public boolean isSelected(WriteableCommandLine commandLine) {
                return false;
            }

            @Override
            public String toString() {
                return "--num";
            }
        };

        List<Option> options = new ArrayList<Option>();
        options.add(numOption);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("--num");
        args.add("-42");
        ListIterator<String> iterator = args.listIterator();
        
        // This should not throw an OptionException. The bug causes:
        // org.apache.commons.cli2.OptionException: Unexpected -42 while processing --num
        group.process(commandLine, iterator);
        
        // After processing, both arguments should be consumed
        assertFalse("Iterator should have no more elements after processing --num -42", iterator.hasNext());
    }

    /**
     * Additional test to verify that negative numbers are correctly handled when they appear
     * as part of a command line with multiple options.
     */
    @Test(timeout = 4000)
    public void testNegativeNumberWithMultipleOptions() throws OptionException {
        // Create an option --num that accepts a numeric argument
        Option numOption = new Option() {
            private Option parent;

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                return arg != null && arg.startsWith("-");
            }

            @Override
            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add("--num");
                return triggers;
            }

            @Override
            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add("--");
                return prefixes;
            }

            @Override
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                if (arguments.hasNext()) {
                    arguments.next(); // consume the value
                }
            }

            @Override
            public void validate(WriteableCommandLine commandLine) throws OptionException {}

            @Override
            public boolean isRequired() { return false; }

            @Override
            public String getPreferredName() { return "--num"; }

            @Override
            public String getDescription() { return "Numeric option"; }

            @Override
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("--num <value>");
            }

            @Override
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }

            @Override
            public void defaults(WriteableCommandLine commandLine) {}

            @Override
            public Option findOption(String trigger) {
                return trigger.equals("--num") ? this : null;
            }

            @Override
            public void setParent(Option parent) { this.parent = parent; }

            @Override
            public Option getParent() { return parent; }

            @Override
            public boolean isSelected(WriteableCommandLine commandLine) { return false; }

            @Override
            public String toString() { return "--num"; }
            
            private Option parent;
        };

        // Create another option --verbose
        Option verboseOption = new Option() {
            private Option parent;

            @Override
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                return arg != null && arg.equals("--verbose");
            }

            @Override
            public Set getTriggers() {
                Set<String> triggers = new HashSet<String>();
                triggers.add("--verbose");
                return triggers;
            }

            @Override
            public Set getPrefixes() {
                Set<String> prefixes = new HashSet<String>();
                prefixes.add("--");
                return prefixes;
            }

            @Override
            public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
                // No value to consume
            }

            @Override
            public void validate(WriteableCommandLine commandLine) throws OptionException {}

            @Override
            public boolean isRequired() { return false; }

            @Override
            public String getPreferredName() { return "--verbose"; }

            @Override
            public String getDescription() { return "Verbose mode"; }

            @Override
            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("--verbose");
            }

            @Override
            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return new ArrayList();
            }

            @Override
            public void defaults(WriteableCommandLine commandLine) {}

            @Override
            public Option findOption(String trigger) {
                return trigger.equals("--verbose") ? this : null;
            }

            @Override
            public void setParent(Option parent) { this.parent = parent; }

            @Override
            public Option getParent() { return parent; }

            @Override
            public boolean isSelected(WriteableCommandLine commandLine) { return false; }

            @Override
            public String toString() { return "--verbose"; }
            
            private Option parent;
        };

        List<Option> options = new ArrayList<Option>();
        options.add(numOption);
        options.add(verboseOption);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 2, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("--verbose");
        args.add("--num");
        args.add("-42");
        ListIterator<String> iterator = args.listIterator();
        
        // This should process --verbose, then --num with value -42
        group.process(commandLine, iterator);
        
        // All arguments should be consumed
        assertFalse("Iterator should have no more elements", iterator.hasNext());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullOptionsList() {
        new GroupImpl(null, "test", "test group", 0, 1, false);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullOptionInList() {
        List<Option> options = new ArrayList<Option>();
        options.add(null);
        new GroupImpl(options, "test", "test group", 0, 1, false);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testOptionsListUnmodifiable() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        group.getOptions().add(createMockOption("--other", "-"));
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testAnonymousListUnmodifiable() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        group.getAnonymous().add(createMockArgument());
    }

    @Test(timeout = 4000)
    public void testProcessLooksLikeOptionButNoMemberFound() throws OptionException {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("-x"); // looks like an option but doesn't match any member
        args.add("value");
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // Should abort the group and leave the iterator at position 0 (before -x)
        assertTrue("Iterator should have elements remaining", iterator.hasNext());
        assertEquals("First element should still be -x", "-x", iterator.next());
    }

    @Test(timeout = 4000)
    public void testProcessNotLooksLikeOptionAndNoAnonymous() throws OptionException {
        List<Option> options = new ArrayList<Option>();
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        List<String> args = new ArrayList<String>();
        args.add("value"); // doesn't look like an option
        ListIterator<String> iterator = args.listIterator();
        group.process(commandLine, iterator);
        // Should break and leave the iterator at position 0
        assertTrue("Iterator should have elements remaining", iterator.hasNext());
        assertEquals("First element should still be value", "value", iterator.next());
    }

    @Test(timeout = 4000)
    public void testValidateWithAnonymousArgs() throws OptionException {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        WriteableCommandLine commandLine = createMockCommandLine();
        group.validate(commandLine); // Should not throw
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructorSetsParentOnOptions() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        assertSame("Option's parent should be the group", group, opt.getParent());
    }

    @Test(timeout = 4000)
    public void testConstructorMovesArgumentsToAnonymous() {
        Argument arg = createMockArgument();
        List<Option> options = new ArrayList<Option>();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        assertEquals("Options list should be empty", 0, group.getOptions().size());
        assertEquals("Anonymous list should have one element", 1, group.getAnonymous().size());
    }

    @Test(timeout = 4000)
    public void testConstructorPopulatesOptionMap() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set triggers = group.getTriggers();
        assertTrue("Triggers should contain '--num'", triggers.contains("--num"));
    }

    @Test(timeout = 4000)
    public void testConstructorPopulatesPrefixes() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        Set prefixes = group.getPrefixes();
        assertTrue("Prefixes should contain '-'", prefixes.contains("-"));
    }

    @Test(timeout = 4000)
    public void testIsRequiredWithParentAndSuperRequired() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 1, 1, true);
        // The group has no parent, so isRequired should check minimum > 0
        assertTrue("Group should be required", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testIsRequiredWithParentAndNotSuperRequired() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 1, 1, false);
        // The group has no parent and isRequired flag is false, but minimum > 0
        assertTrue("Group should be required because minimum > 0", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testIsRequiredWithParentAndMinimumZero() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, true);
        // The group has no parent and isRequired flag is true, but minimum == 0
        assertFalse("Group should not be required because minimum == 0", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testOptionMapUnmodifiable() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        try {
            group.getTriggers().add("--other");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testPrefixesUnmodifiable() {
        Option opt = createMockOption("--num", "-");
        List<Option> options = new ArrayList<Option>();
        options.add(opt);
        GroupImpl group = new GroupImpl(options, "test", "test group", 0, 1, false);
        try {
            group.getPrefixes().add("+");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }
}