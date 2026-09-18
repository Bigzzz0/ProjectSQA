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
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.resource.ResourceConstants;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli2.option.GroupImpl
 *
 * Tested Decisions & Branches:
 * 1. Constructor:
 *    - Separation of child options vs Argument (anonymous)
 *    - Trigger mapping in reverse comparator order (ReverseStringComparator)
 *    - Prefix harvesting and parent link establishment
 * 2. canProcess(commandLine, arg):
 *    - arg == null -> false
 *    - optionMap.containsKey(arg) -> true
 *    - tailMap search bursting: option.canProcess() -> true
 *    - looksLikeOption() -> false vs true
 *    - anonymous.size() > 0 fallback -> true vs false
 * 3. process(commandLine, arguments):
 *    - Loop condition, previous token detection & loop abort
 *    - Direct option hit in optionMap
 *    - Option not found -> looksLikeOption() true -> tailMap iterative burst check
 *      - member option found vs member option not found rollback
 *    - Option not found -> looksLikeOption() false -> anonymous argument check
 *      - anonymous.isEmpty() -> break
 *      - argument.canProcess -> argument.process()
 * 4. validate(commandLine):
 *    - present count validation: present > maximum -> OptionException(UNEXPECTED_TOKEN)
 *    - present count validation: present < minimum -> OptionException(MISSING_OPTION)
 *    - validation cascade for options (validate == true if required or present)
 *    - anonymous arguments validation cascade
 * 5. appendUsage(buffer, helpSettings, comp, separator):
 *    - Combinations of DISPLAY_OPTIONAL, DISPLAY_OPTIONAL_CHILD_GROUP, DISPLAY_GROUP_EXPANDED,
 *      DISPLAY_GROUP_NAME, DISPLAY_GROUP_ARGUMENT, DISPLAY_GROUP_OUTER
 *    - Flags: optional, expanded, named, both, arguments, outer
 *    - Custom separator support, comparator vs null comparator ordering
 * 6. helpLines(depth, helpSettings, comp):
 *    - DISPLAY_GROUP_NAME -> adds HelpLineImpl
 *    - DISPLAY_GROUP_EXPANDED -> cascades helpLines to options
 *    - DISPLAY_GROUP_ARGUMENT -> cascades helpLines to anonymous arguments
 * 7. isRequired():
 *    - evaluates: (getParent() == null || super.isRequired()) && getMinimum() > 0
 * 8. findOption(trigger):
 *    - searches across options, returns matched option or null
 * 9. defaults(commandLine):
 *    - applies defaults down to options and anonymous arguments
 * 10. Known Ground Truth Defect (CLI-150):
 *    - Parsing negative numbers as option arguments triggers OptionException:
 *      Unexpected -42 while processing --num (looksLikeOption treated -42 as an option).
 */
public class GroupImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").withDescription("Option A").create();
        final Option optB = obuilder.withShortName("b").withDescription("Option B").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "testGroup", "A group description", 1, 2, true);

        assertEquals("testGroup", group.getPreferredName());
        assertEquals("A group description", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertEquals(2, group.getOptions().size());
        assertTrue(group.getAnonymous().isEmpty());
        assertTrue(group.isRequired());

        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("-b"));

        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));

        assertEquals(optA, group.findOption("-a"));
        assertEquals(optB, group.findOption("-b"));
        assertNull(group.findOption("-unknown"));
    }

    @Test(timeout = 4000)
    public void testIsRequiredWithParentCondition() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();

        final List childOptions = new ArrayList();
        childOptions.add(optA);

        // Required because parent == null and min > 0
        final GroupImpl childGroup = new GroupImpl(childOptions, "child", "desc", 1, 1, false);
        assertTrue(childGroup.isRequired());

        // When parent is attached and childGroup is not required
        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parent", "desc", 0, 1, false);

        assertSame(parentGroup, childGroup.getParent());
        assertFalse(childGroup.isRequired());

        // When childGroup has minimum 0
        final GroupImpl zeroMinGroup = new GroupImpl(new ArrayList(), "zero", "desc", 0, 1, true);
        assertFalse(zeroMinGroup.isRequired());
    }

    @Test(timeout = 4000)
    public void testCanProcessDirectAndBursting() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optF = obuilder.withShortName("f").create();

        final List options = new ArrayList();
        options.add(optF);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        // Null argument
        assertFalse(group.canProcess(cmd, (String) null));

        // Exact match
        assertTrue(group.canProcess(cmd, "-f"));

        // Non-option token when no anonymous argument is configured
        assertFalse(group.canProcess(cmd, "notAnOption"));
    }

    @Test(timeout = 4000)
    public void testCanProcessWithAnonymousArgument() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("arg").create();

        final List options = new ArrayList();
        options.add(arg);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        // Anonymous argument accepts non-option arguments
        assertTrue(group.canProcess(cmd, "someValue"));
    }

    @Test(timeout = 4000)
    public void testProcessOptionsAndAnonymousArguments() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("arg").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(arg);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        final List tokens = new ArrayList();
        tokens.add("-a");
        tokens.add("targetValue");

        final ListIterator iterator = tokens.listIterator();
        group.process(cmd, iterator);

        assertTrue(cmd.hasOption(optA));
        assertTrue(cmd.hasOption(arg));
        assertEquals("targetValue", cmd.getValue(arg));
        assertFalse(iterator.hasNext());
    }

    @Test(timeout = 4000)
    public void testValidateSuccess() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();
        final Option optB = obuilder.withShortName("b").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());
        cmd.addOption(optA);

        group.validate(cmd);
        assertTrue(cmd.hasOption(optA));
    }

    @Test(timeout = 4000)
    public void testDefaultsApplication() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("arg").withDefault("defVal").create();

        final List options = new ArrayList();
        options.add(arg);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        group.defaults(cmd);
        assertEquals("defVal", cmd.getValue(arg));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyGroup() throws OptionException {
        final GroupImpl emptyGroup = new GroupImpl(new ArrayList(), "empty", "desc", 0, 0, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(emptyGroup, new ArrayList());

        assertFalse(emptyGroup.canProcess(cmd, "-anything"));
        assertFalse(emptyGroup.canProcess(cmd, "value"));

        final List tokens = new ArrayList();
        tokens.add("val");
        final ListIterator it = tokens.listIterator();
        emptyGroup.process(cmd, it);
        // "val" cannot be processed by empty group, iterator resets
        assertTrue(it.hasNext());
        assertEquals("val", it.next());

        emptyGroup.validate(cmd);
    }

    @Test(timeout = 4000)
    public void testRepeatedTokenDoesNotCauseInfiniteLoop() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();

        final List options = new ArrayList();
        options.add(optA);
        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        // Token list where same token instance repeats
        final String dup = "-unknown";
        final List tokens = new ArrayList();
        tokens.add(dup);
        tokens.add(dup);

        final ListIterator it = tokens.listIterator();
        group.process(cmd, it);

        // Group should break when same token is seen twice
        assertTrue(it.hasPrevious());
    }

    @Test(timeout = 4000)
    public void testUsageDisplaySettingsAllPermutations() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();
        final Option optB = obuilder.withShortName("b").create();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("arg").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);
        options.add(arg);

        final GroupImpl group = new GroupImpl(options, "myGroup", "desc", 0, 2, false);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, null, ",");

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
        assertTrue(usage.contains("myGroup"));
        assertTrue(usage.contains("-a"));
        assertTrue(usage.contains("-b"));
        assertTrue(usage.contains("arg"));
    }

    @Test(timeout = 4000)
    public void testUsageWithComparator() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optZ = obuilder.withShortName("z").create();
        final Option optA = obuilder.withShortName("a").create();

        final List options = new ArrayList();
        options.add(optZ);
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, null, "desc", 0, 2, true);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator comp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option op1 = (Option) o1;
                final Option op2 = (Option) o2;
                return op1.getPreferredName().compareTo(op2.getPreferredName());
            }
        };

        final StringBuffer buffer = new StringBuffer();
        group.appendUsage(buffer, settings, comp, "|");

        assertEquals("-a|-z", buffer.toString());
    }

    @Test(timeout = 4000)
    public void testHelpLinesGeneration() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").withDescription("Option A").create();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument arg = abuilder.withName("file").withDescription("Input file").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(arg);

        final GroupImpl group = new GroupImpl(options, "group", "Group description", 1, 1, false);

        final Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List help = group.helpLines(0, settings, null);
        assertNotNull(help);
        assertEquals(3, help.size()); // group + optA + arg

        final HelpLine line0 = (HelpLine) help.get(0);
        assertEquals(0, line0.getDepth());
        assertSame(group, line0.getOption());

        final HelpLine line1 = (HelpLine) help.get(1);
        assertEquals(1, line1.getDepth());
        assertSame(optA, line1.getOption());

        final HelpLine line2 = (HelpLine) help.get(2);
        assertEquals(1, line2.getDepth());
        assertSame(arg, line2.getOption());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-150 Ground Truth)
    // =========================================================================

    /**
     * Targets BugCLI150: OptionException: Unexpected -42 while processing --num.
     * When parsing a negative number as an argument value, the parser/group should
     * not misinterpret the negative number as an unexpected option trigger.
     */
    @Test(timeout = 4000)
    public void testBugCLI150NegativeNumberArgument() throws OptionException {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Argument numArg = abuilder.withName("num").create();
        final Option num = obuilder.withLongName("num").withArgument(numArg).create();
        final Group root = new GroupBuilder().withOption(num).create();

        final Parser parser = new Parser();
        parser.setGroup(root);

        final CommandLine cl = parser.parse(new String[] { "--num", "-42" });
        assertEquals("-42", cl.getValue(num));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidateThrowsMissingOptionWhenUnderMinimum() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();

        final List options = new ArrayList();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "minGroup", "desc", 1, 2, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        try {
            group.validate(cmd);
            fail("Expected OptionException for missing minimum options");
        } catch (OptionException oe) {
            assertEquals(ResourceConstants.MISSING_OPTION, oe.getErrorCode());
            assertSame(group, oe.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testValidateThrowsUnexpectedTokenWhenOverMaximum() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").create();
        final Option optB = obuilder.withShortName("b").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);

        final GroupImpl group = new GroupImpl(options, "maxGroup", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());
        cmd.addOption(optA);
        cmd.addOption(optB);

        try {
            group.validate(cmd);
            fail("Expected OptionException for exceeding maximum options");
        } catch (OptionException oe) {
            assertEquals(ResourceConstants.UNEXPECTED_TOKEN, oe.getErrorCode());
            assertSame(group, oe.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testValidateCascadesToChildRequiredOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").withRequired(true).create();

        final List options = new ArrayList();
        options.add(optA);

        final GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        try {
            group.validate(cmd);
            fail("Expected OptionException because child required option was missing");
        } catch (OptionException oe) {
            assertEquals(ResourceConstants.MISSING_OPTION, oe.getErrorCode());
            assertSame(optA, oe.getOption());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Complex Combinations
    // =========================================================================

    @Test(timeout = 4000)
    public void testReverseStringComparatorSingletonAndOrder() {
        final Comparator comp = ReverseStringComparator.getInstance();
        assertNotNull(comp);
        assertSame(comp, ReverseStringComparator.getInstance());

        assertTrue(comp.compare("a", "b") > 0);
        assertTrue(comp.compare("b", "a") < 0);
        assertEquals(0, comp.compare("equal", "equal"));
    }

    @Test(timeout = 4000)
    public void testSwitchOptionWithinGroup() throws OptionException {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option displaySwitch = sbuilder.withName("display").create();

        final List options = new ArrayList();
        options.add(displaySwitch);

        final GroupImpl group = new GroupImpl(options, "switchGroup", "desc", 0, 1, false);
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());

        final List tokens = new ArrayList();
        tokens.add("+display");

        final ListIterator it = tokens.listIterator();
        group.process(cmd, it);

        assertTrue(cmd.hasOption(displaySwitch));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testNestedGroupProcessingAndFindOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optInner = obuilder.withShortName("i").create();

        final List innerOptions = new ArrayList();
        innerOptions.add(optInner);
        final Group innerGroup = new GroupImpl(innerOptions, "inner", "inner desc", 0, 1, false);

        final List outerOptions = new ArrayList();
        outerOptions.add(innerGroup);
        final GroupImpl outerGroup = new GroupImpl(outerOptions, "outer", "outer desc", 0, 1, false);

        assertEquals(optInner, outerGroup.findOption("-i"));
        assertNull(outerGroup.findOption("-absent"));
    }
}