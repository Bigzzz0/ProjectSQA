/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli2.option.GroupImpl
 * Defect Reference: BugCLI144 (ClassCastException: java.io.File cannot be cast to java.lang.String)
 *
 * Key Decision Branches & Scenarios Covered:
 * 1. Constructor:
 *    - Options partitioning: Option vs Argument (anonymous vs named options)
 *    - Trigger mapping in optionMap (ReverseStringComparator ordering)
 *    - Prefixes aggregation across child options
 *    - Removal of Argument instances from options list and placement into anonymous
 * 2. canProcess(WriteableCommandLine, String):
 *    - null argument check -> returns false
 *    - optionMap exact match -> returns true
 *    - tailMap traversal: child option.canProcess() -> returns true if child can process
 *    - commandLine.looksLikeOption() check -> returns false if true
 *    - anonymous argument check: anonymous.size() > 0 -> returns true, else false
 * 3. process(WriteableCommandLine, ListIterator):
 *    - Repeated token loop guard: previous == arg -> rollback (arguments.previous()) and break
 *    - Option found in optionMap: rollback and opt.process(...)
 *    - Option NOT found:
 *      * looksLikeOption == true:
 *        - tailMap candidate evaluation: foundMemberOption rollback and process
 *        - no member found: rollback and return
 *      * looksLikeOption == false:
 *        - rollback
 *        - anonymous.isEmpty() -> break
 *        - anonymous argument traversal: argument.canProcess(...) -> argument.process(...)
 * 4. validate(WriteableCommandLine):
 *    - option.isRequired() || option instanceof Group -> triggers first option.validate()
 *    - commandLine.hasOption(option) -> increments present counter
 *      * present > maximum -> sets unexpected option and breaks -> throws OptionException(UNEXPECTED_TOKEN)
 *      * second option.validate() -> triggers DEFECT CLI-144 when option is both required and present
 *    - present < minimum -> throws OptionException(MISSING_OPTION)
 *    - Anonymous arguments validation loop
 * 5. appendUsage(StringBuffer, Set, Comparator, String):
 *    - DisplaySetting.DISPLAY_OPTIONAL (minimum == 0) -> '[' ... ']' wrapping
 *    - DisplaySetting.DISPLAY_GROUP_EXPANDED vs DISPLAY_GROUP_NAME
 *    - Child settings adjustments (removing DISPLAY_OPTIONAL)
 *    - Comparator sorting: comp == null vs custom comp
 *    - Child separators (| or custom)
 *    - DISPLAY_GROUP_OUTER handling
 *    - DISPLAY_GROUP_ARGUMENT handling for anonymous arguments
 * 6. helpLines(int, Set, Comparator):
 *    - DISPLAY_GROUP_NAME -> adds HelpLineImpl
 *    - DISPLAY_GROUP_EXPANDED -> recursively adds child help lines (comp == null vs custom)
 *    - DISPLAY_GROUP_ARGUMENT -> recursively adds anonymous help lines
 * 7. findOption(String), getPreferredName(), getDescription(), getMinimum(), getMaximum(), isRequired(), defaults()
 * 8. ReverseStringComparator: compare(o1, o2) reverse string ordering
 */

package org.apache.commons.cli2.option;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.validation.FileValidator;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroupImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicGroupPropertiesAndGetters() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder()
                .withShortName("a")
                .withLongName("alpha")
                .withDescription("Alpha option")
                .create();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "testGroup", "Test Group Description", 1, 2);

        assertEquals("testGroup", group.getPreferredName());
        assertEquals("Test Group Description", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertTrue(group.isRequired());

        assertEquals(1, group.getOptions().size());
        assertEquals(optA, group.getOptions().get(0));
        assertTrue(group.getAnonymous().isEmpty());

        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));

        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--alpha"));
    }

    @Test(timeout = 4000)
    public void testFindOption() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        final DefaultOption optB = new DefaultOptionBuilder().withShortName("b").create();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "myGroup", "desc", 0, 2);

        assertEquals(optA, group.findOption("-a"));
        assertEquals(optB, group.findOption("-b"));
        assertNull(group.findOption("-c"));
    }

    @Test(timeout = 4000)
    public void testCanProcessExactTrigger() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        assertFalse(group.canProcess(commandLine, (String) null));
        assertTrue(group.canProcess(commandLine, "-a"));
        assertFalse(group.canProcess(commandLine, "-unknown"));
    }

    @Test(timeout = 4000)
    public void testCanProcessBurstingAndAnonymous() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        final Argument arg1 = new ArgumentBuilder().withName("param").create();
        options.add(optA);
        options.add(arg1); // Will be filtered into anonymous

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // Anonymous argument presence allows processing non-option arguments
        assertTrue(group.canProcess(commandLine, "someValue"));
        // An unrecognized option flag when commandLine recognizes it as option
        assertFalse(group.canProcess(commandLine, "--unknown"));
    }

    @Test(timeout = 4000)
    public void testCanProcessWithoutAnonymousArguments() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // No anonymous arguments: bare tokens cannot be processed
        assertFalse(group.canProcess(commandLine, "someBareValue"));
    }

    @Test(timeout = 4000)
    public void testProcessDirectOption() throws OptionException {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final List args = new ArrayList();
        args.add("-a");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator it = args.listIterator();

        group.process(commandLine, it);
        assertTrue(commandLine.hasOption(optA));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testProcessLoopPrevention() throws OptionException {
        final List options = new ArrayList();
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 0);

        final List args = new ArrayList();
        args.add("stuckToken");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator it = args.listIterator();

        group.process(commandLine, it);
        // Stuck token should cause break and backtrack
        assertTrue(it.hasNext());
        assertEquals("stuckToken", it.next());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Validation Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidationSuccessWithinBounds() throws OptionException {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        final DefaultOption optB = new DefaultOptionBuilder().withShortName("b").create();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);

        group.validate(commandLine); // 1 present, minimum is 1, maximum is 2 -> Valid
    }

    @Test(timeout = 4000)
    public void testValidationMissingOptionFailure() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        try {
            group.validate(commandLine);
            fail("Expected OptionException for missing option");
        } catch (OptionException e) {
            assertEquals(group, e.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testValidationUnexpectedOptionFailure() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        final DefaultOption optB = new DefaultOptionBuilder().withShortName("b").create();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);
        commandLine.addOption(optB);

        try {
            group.validate(commandLine);
            fail("Expected OptionException for unexpected option (exceeding maximum)");
        } catch (OptionException e) {
            assertEquals(group, e.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testDefaultsPropagation() {
        final List options = new ArrayList();
        final Argument arg1 = new ArgumentBuilder().withName("arg1").withDefault("defVal").create();
        options.add(arg1);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        group.defaults(commandLine);
        assertTrue(commandLine.hasOption(arg1));
        assertEquals("defVal", commandLine.getValue(arg1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (BugCLI144)
    // =========================================================================

    /**
     * CLI-144 Defect Target:
     * In GroupImpl.validate(), child options are checked:
     *   boolean validate = option.isRequired() || option instanceof Group;
     *   if (validate) { option.validate(commandLine); }
     * and subsequently:
     *   if (commandLine.hasOption(option)) {
     *       ...
     *       option.validate(commandLine); // CALLED A SECOND TIME!
     *   }
     * When an option is both required AND present on the command line, and has an argument
     * validator that transforms string tokens into objects (like FileValidator converting String
     * to java.io.File in WriteableCommandLine), the second validate() invocation receives
     * the already-converted File object and throws:
     *   java.lang.ClassCastException: java.io.File cannot be cast to java.lang.String
     */
    @Test(timeout = 4000)
    public void testBugCLI144DoubleValidationWithFileValidator() throws Exception {
        final File tempFile = File.createTempFile("bugcli144", ".tmp");
        tempFile.deleteOnExit();

        final Argument fileArg = new ArgumentBuilder()
                .withName("target")
                .withMinimum(1)
                .withMaximum(1)
                .withValidator(new FileValidator())
                .create();

        final DefaultOption fileOpt = new DefaultOptionBuilder()
                .withLongName("file")
                .withRequired(true)
                .withArgument(fileArg)
                .create();

        final List options = new ArrayList();
        options.add(fileOpt);

        final GroupImpl group = new GroupImpl(options, "optionsGroup", "Target group for CLI-144", 1, 1);

        final List args = new ArrayList();
        args.add("--file");
        args.add(tempFile.getAbsolutePath());

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator it = args.listIterator();

        // Process token into commandLine
        group.process(commandLine, it);

        assertTrue(commandLine.hasOption(fileOpt));

        // Group validation must succeed without throwing ClassCastException
        group.validate(commandLine);

        final Object val = commandLine.getValue(fileOpt);
        assertNotNull(val);
        assertTrue("Value should be a File instance", val instanceof File);
        assertEquals(tempFile.getAbsoluteFile(), ((File) val).getAbsoluteFile());
    }

    // =========================================================================
    // Partition D: Help, Usage Formatting & ReverseStringComparator
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendUsageWithDisplaySettings() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").withDescription("Option A").create();
        final DefaultOption optB = new DefaultOptionBuilder().withShortName("b").withDescription("Option B").create();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "myGroup", "group desc", 0, 2);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null);

        final String usage = buffer.toString();
        assertTrue("Usage must contain group name", usage.contains("myGroup"));
        assertTrue("Usage must contain option -a", usage.contains("-a"));
        assertTrue("Usage must contain option -b", usage.contains("-b"));
        assertTrue("Usage must contain brackets for optional", usage.startsWith("[") && usage.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithComparatorAndOuterSetting() {
        final List options = new ArrayList();
        final DefaultOption optZ = new DefaultOptionBuilder().withShortName("z").create();
        final DefaultOption optA = new DefaultOptionBuilder().withShortName("a").create();
        options.add(optZ);
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, null, "desc", 0, 2);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option op1 = (Option) o1;
                Option op2 = (Option) o2;
                return op1.getPreferredName().compareTo(op2.getPreferredName());
            }
        };

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, comp, " || ");

        final String usage = buffer.toString();
        // Since comp orders 'a' before 'z', -a should precede -z
        final int posA = usage.indexOf("-a");
        final int posZ = usage.indexOf("-z");
        assertTrue("Sorted usage should place -a before -z", posA < posZ);
        assertTrue("Separator || should be used", usage.contains(" || "));
    }

    @Test(timeout = 4000)
    public void testHelpLinesFormatting() {
        final List options = new ArrayList();
        final DefaultOption optA = new DefaultOptionBuilder()
                .withShortName("a")
                .withDescription("Help for A")
                .create();
        final Argument arg1 = new ArgumentBuilder().withName("param").create();
        options.add(optA);
        options.add(arg1);

        final GroupImpl group = new GroupImpl(options, "groupName", "Group desc", 1, 1);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, settings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        boolean hasGroupName = false;
        for (Iterator it = lines.iterator(); it.hasNext();) {
            HelpLine line = (HelpLine) it.next();
            if (line.getOption().equals(group)) {
                hasGroupName = true;
            }
        }
        assertTrue("HelpLines should include group header", hasGroupName);
    }

    @Test(timeout = 4000)
    public void testReverseStringComparatorDirect() {
        final Comparator comp = ReverseStringComparator.getInstance();
        assertNotNull(comp);
        assertSame(comp, ReverseStringComparator.getInstance());

        assertTrue(comp.compare("a", "b") > 0);
        assertTrue(comp.compare("b", "a") < 0);
        assertEquals(0, comp.compare("same", "same"));
    }

    // =========================================================================
    // Partition E: Nested Groups & Complex Processing Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testNestedGroupValidationAndProcess() throws OptionException {
        final DefaultOption childOpt = new DefaultOptionBuilder().withShortName("c").create();
        final List childOpts = new ArrayList();
        childOpts.add(childOpt);
        final GroupImpl childGroup = new GroupImpl(childOpts, "childGroup", "child desc", 1, 1);

        final List parentOpts = new ArrayList();
        parentOpts.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOpts, "parentGroup", "parent desc", 1, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());

        // Process token for nested child option
        final List args = new ArrayList();
        args.add("-c");
        final ListIterator it = args.listIterator();

        parentGroup.process(commandLine, it);
        assertTrue(commandLine.hasOption(childOpt));

        // Validation should succeed on nested child group
        parentGroup.validate(commandLine);
    }

    @Test(timeout = 4000)
    public void testAnonymousArgumentProcessingWithoutOptionMatch() throws OptionException {
        final Argument anonArg = new ArgumentBuilder().withName("file").withMinimum(1).withMaximum(2).create();
        final List options = new ArrayList();
        options.add(anonArg);

        final GroupImpl group = new GroupImpl(options, "anonGroup", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        final List args = new ArrayList();
        args.add("input.txt");
        final ListIterator it = args.listIterator();

        group.process(commandLine, it);
        assertTrue(commandLine.hasOption(anonArg));
        assertEquals("input.txt", commandLine.getValue(anonArg));
    }
}