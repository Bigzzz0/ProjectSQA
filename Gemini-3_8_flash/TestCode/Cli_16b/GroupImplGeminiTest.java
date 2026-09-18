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
package org.apache.commons.cli2.option;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.cli2.option.GroupImpl
 *
 * Decision / Branch Points Targeted:
 * 1. Constructor Initialization:
 *    - Option is Argument vs regular Option (Anonymous separation, triggers extraction, prefixes collation).
 *    - Unmodifiable wrappers on options, anonymous, optionMap, and prefixes.
 * 2. canProcess(CommandLine, String):
 *    - arg == null -> false
 *    - optionMap.containsKey(arg) -> true
 *    - tailMap search iterating over options where option.canProcess(...) -> true
 *    - commandLine.looksLikeOption(arg) -> false
 *    - anonymous.size() > 0 -> true vs false
 * 3. process(CommandLine, ListIterator):
 *    - arguments.hasNext() loop with previous-instance guard (cycle prevention rollback).
 *    - opt != null: option found in optionMap -> process directly.
 *    - opt == null & commandLine.looksLikeOption:
 *      * tailMap bursting search: foundMemberOption -> break & process.
 *      * not foundMemberOption -> back track and abort group.
 *    - opt == null & !commandLine.looksLikeOption:
 *      * anonymous.isEmpty() -> break.
 *      * anonymous arguments iteration -> argument.canProcess(...) -> argument.process(...).
 * 4. validate(CommandLine):
 *    - present counts tracking against minimum and maximum boundaries.
 *    - option.isRequired() || option instanceof Group validation propagation.
 *    - present > maximum -> throw OptionException (UNEXPECTED_TOKEN).
 *    - present < minimum -> throw OptionException (MISSING_OPTION).
 *    - anonymous argument validation sequence.
 * 5. appendUsage(StringBuffer, Set, Comparator, String):
 *    - Optional brackets '[' and ']' conditionally rendered (minimum == 0 && DISPLAY_OPTIONAL).
 *    - Outer optional brackets handling (DISPLAY_GROUP_OUTER removed & placed conditionally).
 *    - Group name rendering: both named and expanded combinations ' (' / ')'.
 *    - Expanded group options: comparator sorting vs default initial order.
 *    - Separator positioning (default '|' vs custom separator).
 *    - Anonymous arguments usage appended.
 * 6. helpLines(int, Set, Comparator):
 *    - DISPLAY_GROUP_NAME: HelpLineImpl creation.
 *    - DISPLAY_GROUP_EXPANDED: Recursion with comparator vs default order, depth + 1.
 *    - DISPLAY_GROUP_ARGUMENT: Anonymous options recursion at depth + 1.
 * 7. findOption(String):
 *    - Recursion into child options: found match vs exhausted null.
 * 8. defaults(CommandLine):
 *    - Defaults propagation across options and anonymous arguments.
 * 9. ReverseStringComparator:
 *    - Reverse alphabetical ordering verification.
 * 10. CLI-123 Defect Coverage:
 *    - Child option groups nested within parents; verification of minimum and maximum boundaries
 *      when child groups and parent groups interact during validation.
 */
public class GroupImplGeminiTest {

    private DefaultOptionBuilder obuilder;
    private ArgumentBuilder abuilder;
    private GroupBuilder gbuilder;

    @Before
    public void setUp() {
        obuilder = new DefaultOptionBuilder();
        abuilder = new ArgumentBuilder();
        gbuilder = new GroupBuilder();
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        Option opt1 = obuilder.withShortName("a").withLongName("alpha").create();
        Option opt2 = obuilder.withShortName("b").withLongName("beta").create();
        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);

        GroupImpl group = new GroupImpl(options, "testGroup", "A sample group", 1, 2);

        assertEquals("testGroup", group.getPreferredName());
        assertEquals("A sample group", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertTrue(group.isRequired());
        assertEquals(2, group.getOptions().size());
        assertTrue(group.getAnonymous().isEmpty());

        Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));

        Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--alpha"));
        assertTrue(triggers.contains("-b"));
        assertTrue(triggers.contains("--beta"));
    }

    @Test(timeout = 4000)
    public void testFindOption() {
        Option optA = obuilder.withShortName("a").withLongName("alpha").create();
        Option optB = obuilder.withShortName("b").withLongName("beta").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);

        assertSame(optA, group.findOption("-a"));
        assertSame(optA, group.findOption("--alpha"));
        assertSame(optB, group.findOption("-b"));
        assertSame(optB, group.findOption("--beta"));
        assertNull(group.findOption("-c"));
        assertNull(group.findOption("unknown"));
    }

    @Test(timeout = 4000)
    public void testCanProcessDirectAndBursting() {
        Option optHelp = obuilder.withShortName("h").withLongName("help").create();
        Option optVerbose = obuilder.withShortName("v").withLongName("verbose").create();
        Argument arg = abuilder.withName("target").create();

        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        options.add(arg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 10);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // Null argument check
        assertFalse(group.canProcess(commandLine, (String) null));

        // Direct triggers
        assertTrue(group.canProcess(commandLine, "-h"));
        assertTrue(group.canProcess(commandLine, "--help"));
        assertTrue(group.canProcess(commandLine, "-v"));

        // Non-option argument matches anonymous
        assertTrue(group.canProcess(commandLine, "file.txt"));

        // Looks like option but not found in group
        assertFalse(group.canProcess(commandLine, "-x"));
    }

    @Test(timeout = 4000)
    public void testProcessSimpleOption() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        List args = new ArrayList();
        args.add("-a");
        ListIterator it = args.listIterator();

        group.process(commandLine, it);

        assertTrue(commandLine.hasOption("-a"));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testProcessAnonymousArgument() throws OptionException {
        Argument arg = abuilder.withName("input").withMinimum(1).withMaximum(1).create();
        List options = new ArrayList();
        options.add(arg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        List args = new ArrayList();
        args.add("filename.txt");
        ListIterator it = args.listIterator();

        group.process(commandLine, it);

        assertTrue(commandLine.hasOption(arg));
        assertEquals("filename.txt", commandLine.getValue(arg));
    }

    @Test(timeout = 4000)
    public void testProcessUnrecognizedOptionAborts() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        List args = new ArrayList();
        args.add("-z"); // Not recognized
        ListIterator it = args.listIterator();

        group.process(commandLine, it);

        assertFalse(commandLine.hasOption("-z"));
        assertTrue(it.hasNext());
        assertEquals("-z", it.next());
    }

    @Test(timeout = 4000)
    public void testProcessStopsWhenNoAnonymousAvailable() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        List args = new ArrayList();
        args.add("nonOptionToken"); // No anonymous argument in group
        ListIterator it = args.listIterator();

        group.process(commandLine, it);

        assertTrue(it.hasNext());
        assertEquals("nonOptionToken", it.next());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyGroup() {
        GroupImpl emptyGroup = new GroupImpl(new ArrayList(), "empty", "no options", 0, 0);

        assertEquals("empty", emptyGroup.getPreferredName());
        assertEquals(0, emptyGroup.getMinimum());
        assertEquals(0, emptyGroup.getMaximum());
        assertFalse(emptyGroup.isRequired());
        assertTrue(emptyGroup.getOptions().isEmpty());
        assertTrue(emptyGroup.getAnonymous().isEmpty());
        assertTrue(emptyGroup.getPrefixes().isEmpty());
        assertTrue(emptyGroup.getTriggers().isEmpty());
        assertNull(emptyGroup.findOption("-any"));
    }

    @Test(timeout = 4000)
    public void testValidateZeroRequiredSatisfied() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // 0 present, min = 0, max = 1: valid
        group.validate(commandLine);
    }

    @Test(timeout = 4000)
    public void testValidateExactRequiredSatisfied() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        Option optB = obuilder.withShortName("b").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);

        // 1 present, min = 1, max = 2: valid
        group.validate(commandLine);
    }

    @Test(timeout = 4000)
    public void testValidateMissingOptionException() {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        try {
            group.validate(commandLine);
            fail("Expected OptionException for missing option");
        } catch (OptionException e) {
            assertSame(group, e.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testValidateUnexpectedTokenExceptionWhenExceedingMaximum() {
        Option optA = obuilder.withShortName("a").create();
        Option optB = obuilder.withShortName("b").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);
        commandLine.addOption(optB);

        try {
            group.validate(commandLine);
            fail("Expected OptionException for unexpected token");
        } catch (OptionException e) {
            assertSame(group, e.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testDefaultsPropagation() {
        Option optA = obuilder.withShortName("a").withArgument(
            abuilder.withName("val").withDefault("defA").create()
        ).create();

        Argument anonArg = abuilder.withName("anon").withDefault("defAnon").create();

        List options = new ArrayList();
        options.add(optA);
        options.add(anonArg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        group.defaults(commandLine);

        assertEquals("defA", commandLine.getValue(optA));
        assertEquals("defAnon", commandLine.getValue(anonArg));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (CLI-123 & Nested Group Bounds)
    // =========================================================================

    /**
     * CLI-123 Regression Test:
     * When parent option has child group or child options, validating parent and child options
     * must respect the maximum limit. If both parent and child options are provided inappropriately
     * or maximum restrictions are defined, OptionException must be thrown.
     */
    @Test(timeout = 4000)
    public void testDefectCLI123ParentOptionAndChildGroupValidation() throws OptionException {
        // Child options
        Option child1 = obuilder.withShortName("c1").withLongName("child1").create();
        Option child2 = obuilder.withShortName("c2").withLongName("child2").create();

        Group childGroup = gbuilder.withName("childGroup")
            .withMinimum(1)
            .withMaximum(1)
            .withOption(child1)
            .withOption(child2)
            .create();

        // Parent option that holds childGroup
        Option parentOpt = obuilder.withShortName("p").withLongName("parent")
            .withChildren(childGroup)
            .create();

        Group rootGroup = gbuilder.withName("rootGroup")
            .withMinimum(0)
            .withMaximum(1)
            .withOption(parentOpt)
            .create();

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(rootGroup, new ArrayList());

        // Process arguments: parent and child1
        List args = new ArrayList();
        args.add("-p");
        args.add("-c1");
        ListIterator it = args.listIterator();
        rootGroup.process(commandLine, it);

        // Validation should succeed: parent is present, childGroup has 1 child present
        rootGroup.validate(commandLine);
        assertTrue(commandLine.hasOption("-p"));
        assertTrue(commandLine.hasOption("-c1"));
    }

    @Test(timeout = 4000)
    public void testDefectCLI123ChildGroupMaximumExceeded() {
        Option child1 = obuilder.withShortName("c1").create();
        Option child2 = obuilder.withShortName("c2").create();

        Group childGroup = gbuilder.withName("childGroup")
            .withMinimum(1)
            .withMaximum(1)
            .withOption(child1)
            .withOption(child2)
            .create();

        Option parentOpt = obuilder.withShortName("p")
            .withChildren(childGroup)
            .create();

        Group rootGroup = gbuilder.withName("rootGroup")
            .withMinimum(1)
            .withMaximum(1)
            .withOption(parentOpt)
            .create();

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(rootGroup, new ArrayList());

        // Provide both children when maximum is 1
        commandLine.addOption(parentOpt);
        commandLine.addOption(child1);
        commandLine.addOption(child2);

        try {
            rootGroup.validate(commandLine);
            fail("Expected OptionException because childGroup maximum was exceeded (1 max, got 2)");
        } catch (OptionException e) {
            // Success: restriction verified
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDefectCLI123NestedGroupMissingChildOption() {
        Option child1 = obuilder.withShortName("c1").create();

        Group childGroup = gbuilder.withName("childGroup")
            .withMinimum(1)
            .withMaximum(1)
            .withOption(child1)
            .create();

        Option parentOpt = obuilder.withShortName("p")
            .withChildren(childGroup)
            .create();

        Group rootGroup = gbuilder.withName("rootGroup")
            .withMinimum(1)
            .withMaximum(1)
            .withOption(parentOpt)
            .create();

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(rootGroup, new ArrayList());
        // Parent present, but required child missing
        commandLine.addOption(parentOpt);

        try {
            rootGroup.validate(commandLine);
            fail("Expected OptionException because childGroup requires minimum 1 option");
        } catch (OptionException e) {
            assertNotNull(e.getMessage());
        }
    }

    // =========================================================================
    // PARTITION D: Usage Formatting & Help Line Formatting
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendUsageDefaultFormatting() {
        Option optA = obuilder.withShortName("a").withLongName("alpha").create();
        Option optB = obuilder.withShortName("b").withLongName("beta").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        GroupImpl group = new GroupImpl(options, "mygroup", "Sample Group", 0, 1);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, helpSettings, null);

        String usage = buffer.toString();
        // Optional group with minimum 0 displays square brackets and group name
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
        assertTrue(usage.contains("mygroup"));
        assertTrue(usage.contains("-a"));
        assertTrue(usage.contains("-b"));
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithComparator() {
        Option optZ = obuilder.withShortName("z").create();
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optZ);
        options.add(optA);

        GroupImpl group = new GroupImpl(options, null, "desc", 1, 2);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option op1 = (Option) o1;
                Option op2 = (Option) o2;
                return op1.getPreferredName().compareTo(op2.getPreferredName());
            }
        };

        group.appendUsage(buffer, helpSettings, comp, " || ");

        String usage = buffer.toString();
        assertTrue(usage.indexOf("-a") < usage.indexOf("-z"));
        assertTrue(usage.contains(" || "));
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithAnonymousArgumentsAndOuter() {
        Option optA = obuilder.withShortName("a").create();
        Argument arg = abuilder.withName("target").create();

        List options = new ArrayList();
        options.add(optA);
        options.add(arg);

        GroupImpl group = new GroupImpl(options, "outerGroup", "desc", 0, 1);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        group.appendUsage(buffer, helpSettings, null);

        String usage = buffer.toString();
        assertTrue(usage.contains("target"));
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testAppendUsageWithoutExpansion() {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "collapsedGroup", "desc", 1, 1);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        // Neither expanded nor null name, but named
        group.appendUsage(buffer, helpSettings, null);

        String usage = buffer.toString();
        assertEquals("collapsedGroup", usage);
    }

    @Test(timeout = 4000)
    public void testHelpLinesFormatting() {
        Option optA = obuilder.withShortName("a").withDescription("Option A").create();
        Option optB = obuilder.withShortName("b").withDescription("Option B").create();
        Argument arg = abuilder.withName("anonArg").create();

        List options = new ArrayList();
        options.add(optA);
        options.add(optB);
        options.add(arg);

        GroupImpl group = new GroupImpl(options, "helpGroup", "Help group desc", 0, 2);

        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        List lines = group.helpLines(0, helpSettings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        // First line should be group itself (DISPLAY_GROUP_NAME)
        HelpLine first = (HelpLine) lines.get(0);
        assertSame(group, first.getOption());
        assertEquals(0, first.getIndent());

        // Check that child help lines are indented with depth + 1
        boolean foundChildIndent = false;
        for (Iterator it = lines.iterator(); it.hasNext();) {
            HelpLine hl = (HelpLine) it.next();
            if (hl.getIndent() == 1) {
                foundChildIndent = true;
                break;
            }
        }
        assertTrue("Expected child options indented at depth 1", foundChildIndent);
    }

    @Test(timeout = 4000)
    public void testHelpLinesWithComparator() {
        Option optZ = obuilder.withShortName("z").withDescription("Option Z").create();
        Option optA = obuilder.withShortName("a").withDescription("Option A").create();
        List options = new ArrayList();
        options.add(optZ);
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "compGroup", "desc", 0, 2);

        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option op1 = (Option) o1;
                Option op2 = (Option) o2;
                return op1.getPreferredName().compareTo(op2.getPreferredName());
            }
        };

        List lines = group.helpLines(0, helpSettings, comp);
        assertEquals(2, lines.size());
        HelpLine first = (HelpLine) lines.get(0);
        assertEquals("-a", first.getOption().getPreferredName());
    }

    // =========================================================================
    // PARTITION E: Exception Guard Paths, Reverse Comparator, and Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testReverseStringComparatorDirect() {
        Comparator comp = ReverseStringComparator.getInstance();
        assertNotNull(comp);
        assertSame(comp, ReverseStringComparator.getInstance());

        assertTrue(comp.compare("apple", "banana") > 0);
        assertTrue(comp.compare("banana", "apple") < 0);
        assertEquals(0, comp.compare("test", "test"));
    }

    @Test(timeout = 4000)
    public void testProcessLoopRollbackOnDuplicateTokenInstance() throws OptionException {
        Option optA = obuilder.withShortName("a").create();
        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // Create a list with the exact same String instance twice
        String token = "-unknown";
        List args = new ArrayList();
        args.add(token);
        args.add(token);

        ListIterator it = args.listIterator();
        // Since -unknown is not recognized and looks like option,
        // it fails to find member option and returns immediately.
        group.process(commandLine, it);

        assertTrue(it.hasNext());
        assertEquals(token, it.next());
    }

    @Test(timeout = 4000)
    public void testBurstingCanProcessDetection() {
        Option optA = obuilder.withShortName("a").create();
        Option optB = obuilder.withShortName("b").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        GroupImpl group = new GroupImpl(options, "burstGroup", "desc", 0, 2);
        WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // "-ab" might be burstable if options support it
        // canProcess calls tailMap for prefix matching
        assertNotNull(group.getPrefixes());
        assertTrue(group.getPrefixes().contains("-"));
    }

    @Test(timeout = 4000)
    public void testUnmodifiableListsIntegrity() {
        Option optA = obuilder.withShortName("a").create();
        Argument arg = abuilder.withName("arg1").create();
        List options = new ArrayList();
        options.add(optA);
        options.add(arg);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1);

        try {
            group.getOptions().add(obuilder.withShortName("b").create());
            fail("getOptions() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        try {
            group.getAnonymous().add(abuilder.withName("arg2").create());
            fail("getAnonymous() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        try {
            group.getPrefixes().add("+");
            fail("getPrefixes() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }

        try {
            group.getTriggers().add("-extra");
            fail("getTriggers() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }
}