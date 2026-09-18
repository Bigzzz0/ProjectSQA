package org.apache.commons.cli2;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects (Defects4J / Commons-CLI2 CLI-123 & Order Regressions):
 * 1. BugCLI123Test::testSingleChildOption
 *    - Cause: Parent option hierarchy association failure. Parsing child options caused GroupImpl
 *             to fail parent validation ("Missing option parentOptions").
 * 2. BugCLI123Test::testMultipleChildOptions
 *    - Cause: Similar to single child, when multiple child options under a parent were parsed,
 *             parent group tracking failed validation.
 * 3. BugCLI123Test::testParentOptionAndChildOption
 *    - Cause: When parent maximum restrictions were configured (minimum 1, maximum 1),
 *             processing parent1 + child + parent2 failed to enforce maximum group constraints.
 * 4. WriteableCommandLineImplTest / DefaultingCommandLineTest::testGetOptions_Order
 *    - Cause: When an option belonging to a parent group was processed, the parent group
 *             must be added to the command line before the child option. In defective versions,
 *             root/parent was omitted, returning the child as the first option.
 *
 * Core Option Contract Partitions:
 * - Partition A: Core Functional Logic & State Transitions (canProcess, process, defaults, validate)
 * - Partition B: Boundary Value Analysis (iterator restoration, non-matching triggers, empty args)
 * - Partition C: Defect-Targeted Branch Zone (CLI-123 child/parent hierarchy & option order verification)
 * - Partition D: Exception & Defensive Guard Paths (required option validation failure, max limit breach)
 * - Partition E: Object Lifecycle & Contract Integrity (triggers, prefixes, id, preferred name, findOption)
 */

import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.CommandBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static org.junit.Assert.*;

public class OptionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptionCanProcessString() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withLongName("execute").withShortName("e").create();
        final Group root = new GroupBuilder().withOption(opt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertTrue(opt.canProcess(cmd, "--execute"));
        assertTrue(opt.canProcess(cmd, "-e"));
        assertFalse(opt.canProcess(cmd, "--unknown"));
        assertFalse(opt.canProcess(cmd, (String) null));
    }

    @Test(timeout = 4000)
    public void testOptionProcessConsumesArgumentAndUpdatesCommandLine() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withLongName("target").withShortName("t").create();
        final Group root = new GroupBuilder().withOption(opt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = new ArrayList(Arrays.asList("--target", "subsequent"));
        final ListIterator it = args.listIterator();

        opt.process(cmd, it);

        assertTrue("CommandLine must register processed option", cmd.hasOption(opt));
        assertTrue("CommandLine must register processed option by trigger", cmd.hasOption("--target"));
        assertEquals("Iterator must advance past processed argument", 1, it.nextIndex());
        assertEquals("Next argument must be subsequent", "subsequent", it.next());
    }

    @Test(timeout = 4000)
    public void testOptionDefaultsAppliedToCommandLine() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Option defaultArg = abuilder.withName("port").withDefault("8080").create();
        final Group root = new GroupBuilder().withOption(defaultArg).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        defaultArg.defaults(cmd);
        assertEquals("8080", cmd.getValue(defaultArg));
    }

    @Test(timeout = 4000)
    public void testSwitchOptionProcess() throws OptionException {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option sw = sbuilder.withName("debug").create();
        final Group root = new GroupBuilder().withOption(sw).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = new ArrayList(Arrays.asList("+debug"));
        final ListIterator it = args.listIterator();

        assertTrue(sw.canProcess(cmd, it));
        sw.process(cmd, it);
        assertTrue(cmd.hasOption(sw));
        assertTrue(cmd.hasOption("+debug"));
        assertEquals(Boolean.TRUE, cmd.getSwitch(sw));
    }

    @Test(timeout = 4000)
    public void testCommandOptionProcessWithChildren() throws OptionException {
        final CommandBuilder cbuilder = new CommandBuilder();
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option verbose = obuilder.withLongName("verbose").withShortName("v").create();
        final Group commitOptions = gbuilder.withOption(verbose).create();
        final Option commit = cbuilder.withName("commit").withChildren(commitOptions).create();

        final Group root = gbuilder.reset().withOption(commit).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = new ArrayList(Arrays.asList("commit", "-v"));
        final ListIterator it = args.listIterator();

        assertTrue(commit.canProcess(cmd, it));
        commit.process(cmd, it);
        assertTrue(cmd.hasOption(commit));
        assertTrue(cmd.hasOption("commit"));
        assertTrue(cmd.hasOption(verbose));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Iterator State Restoration
    // =========================================================================

    @Test(timeout = 4000)
    public void testCanProcessRestoresIteratorStateOnMatch() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withLongName("flag").create();
        final Group root = new GroupBuilder().withOption(opt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = new ArrayList(Arrays.asList("--flag", "other"));
        final ListIterator it = args.listIterator();

        final boolean can = opt.canProcess(cmd, it);
        assertTrue(can);
        assertEquals("Iterator cursor must be reset to initial position on match", 0, it.nextIndex());
        assertEquals("--flag", it.next());
    }

    @Test(timeout = 4000)
    public void testCanProcessRestoresIteratorStateOnMismatch() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withLongName("flag").create();
        final Group root = new GroupBuilder().withOption(opt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = new ArrayList(Arrays.asList("--unknown", "other"));
        final ListIterator it = args.listIterator();

        final boolean can = opt.canProcess(cmd, it);
        assertFalse(can);
        assertEquals("Iterator cursor must be reset to initial position on mismatch", 0, it.nextIndex());
        assertEquals("--unknown", it.next());
    }

    @Test(timeout = 4000)
    public void testCanProcessEmptyIteratorReturnsFalse() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withLongName("flag").create();
        final Group root = new GroupBuilder().withOption(opt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        final List args = Collections.emptyList();
        final ListIterator it = args.listIterator();

        assertFalse(opt.canProcess(cmd, it));
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testAppendUsageAndHelpLinesBasicContract() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder
            .withLongName("help")
            .withShortName("h")
            .withDescription("Display help info")
            .withId(42)
            .create();

        final StringBuffer buffer = new StringBuffer();
        opt.appendUsage(buffer, Collections.emptySet(), null);
        assertTrue("Usage buffer must contain option trigger", buffer.toString().contains("--help"));

        final List lines = opt.helpLines(0, Collections.emptySet(), null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-123 & Order Regressions)
    // =========================================================================

    /**
     * Defects4J Ground Truth Target:
     * org.apache.commons.cli2.bug.BugCLI123Test::testSingleChildOption
     * Fails on defective code with: OptionException: Missing option parentOptions
     */
    @Test(timeout = 4000)
    public void testBugCLI123SingleChildOption() throws Exception {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Option child1 = oBuilder.withLongName("child1").create();
        final Option child2 = oBuilder.withLongName("child2").create();
        final Group children = gBuilder.withName("children").withOption(child1).withOption(child2).create();

        final Option parent1 = oBuilder.withLongName("parent1").withChildren(children).create();
        final Option parent2 = oBuilder.withLongName("parent2").create();
        final Group parentOptions = gBuilder.withName("parentOptions").withMinimum(1).withMaximum(1)
            .withOption(parent1).withOption(parent2).create();

        final Parser parser = new Parser();
        parser.setGroup(parentOptions);
        final CommandLine cl = parser.parse(new String[] { "--parent1", "--child1" });

        assertTrue("Parsed CommandLine must contain parent1", cl.hasOption(parent1));
        assertTrue("Parsed CommandLine must contain child1", cl.hasOption(child1));
        assertFalse("Parsed CommandLine must not contain parent2", cl.hasOption(parent2));
        assertFalse("Parsed CommandLine must not contain child2", cl.hasOption(child2));
    }

    /**
     * Defects4J Ground Truth Target:
     * org.apache.commons.cli2.bug.BugCLI123Test::testMultipleChildOptions
     * Fails on defective code with: OptionException: Missing option parentOptions
     */
    @Test(timeout = 4000)
    public void testBugCLI123MultipleChildOptions() throws Exception {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Option child1 = oBuilder.withLongName("child1").create();
        final Option child2 = oBuilder.withLongName("child2").create();
        final Group children = gBuilder.withName("children").withOption(child1).withOption(child2).create();

        final Option parent1 = oBuilder.withLongName("parent1").withChildren(children).create();
        final Option parent2 = oBuilder.withLongName("parent2").create();
        final Group parentOptions = gBuilder.withName("parentOptions").withMinimum(1).withMaximum(1)
            .withOption(parent1).withOption(parent2).create();

        final Parser parser = new Parser();
        parser.setGroup(parentOptions);
        final CommandLine cl = parser.parse(new String[] { "--parent1", "--child1", "--child2" });

        assertTrue("Parsed CommandLine must contain parent1", cl.hasOption(parent1));
        assertTrue("Parsed CommandLine must contain child1", cl.hasOption(child1));
        assertTrue("Parsed CommandLine must contain child2", cl.hasOption(child2));
        assertFalse("Parsed CommandLine must not contain parent2", cl.hasOption(parent2));
    }

    /**
     * Defects4J Ground Truth Target:
     * org.apache.commons.cli2.bug.BugCLI123Test::testParentOptionAndChildOption
     * Fails on defective code with: junit.framework.AssertionFailedError: Maximum restriction for parent not verified!
     */
    @Test(timeout = 4000)
    public void testBugCLI123ParentOptionAndChildOptionMaximumRestriction() throws Exception {
        final DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        final ArgumentBuilder aBuilder = new ArgumentBuilder();
        final GroupBuilder gBuilder = new GroupBuilder();

        final Option child1 = oBuilder.withLongName("child1").create();
        final Option child2 = oBuilder.withLongName("child2").create();
        final Group children = gBuilder.withName("children").withOption(child1).withOption(child2).create();

        final Option parent1 = oBuilder.withLongName("parent1").withChildren(children).create();
        final Option parent2 = oBuilder.withLongName("parent2").create();
        final Group parentOptions = gBuilder.withName("parentOptions").withMinimum(1).withMaximum(1)
            .withOption(parent1).withOption(parent2).create();

        final Parser parser = new Parser();
        parser.setGroup(parentOptions);

        try {
            parser.parse(new String[] { "--parent1", "--child1", "--parent2" });
            fail("Maximum restriction for parent not verified!");
        } catch (final OptionException expected) {
            // Expected: parentOptions allows at most 1 parent option, both parent1 and parent2 were provided
            assertNotNull(expected.getOption());
        }
    }

    /**
     * Defects4J Ground Truth Target:
     * WriteableCommandLineImplTest / DefaultingCommandLineTest::testGetOptions_Order
     * Fails on defective code with: expected same: root group was not: help option
     */
    @Test(timeout = 4000)
    public void testCommandLineOptionsOrderHierarchy() throws Exception {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option help = obuilder
            .withShortName("h")
            .withShortName("-?")
            .withLongName("help")
            .withDescription("print this message")
            .create();

        final Option login = obuilder
            .withLongName("login")
            .withDescription("login option")
            .create();

        final Group root = gbuilder
            .withOption(help)
            .withOption(login)
            .create();

        final Parser parser = new Parser();
        parser.setGroup(root);
        final CommandLine cl = parser.parse(new String[] { "--help" });

        final List options = cl.getOptions();
        assertNotNull(options);
        assertTrue("Options list should contain elements", options.size() >= 2);
        assertSame("First option in CommandLine must be the parent root group", root, options.get(0));
        assertSame("Second option in CommandLine must be the help option", help, options.get(1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OptionException.class, timeout = 4000)
    public void testValidationFailsWhenRequiredOptionIsMissing() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option requiredOpt = obuilder
            .withLongName("mandatory")
            .withRequired(true)
            .create();

        final Group root = new GroupBuilder().withOption(requiredOpt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // cmd does not contain requiredOpt; validate must throw OptionException
        requiredOpt.validate(cmd);
    }

    @Test(timeout = 4000)
    public void testValidationPassesWhenRequiredOptionIsPresent() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option requiredOpt = obuilder
            .withLongName("mandatory")
            .withRequired(true)
            .create();

        final Group root = new GroupBuilder().withOption(requiredOpt).create();
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addOption(requiredOpt);

        requiredOpt.validate(cmd);
        assertTrue(cmd.hasOption(requiredOpt));
    }

    @Test(expected = OptionException.class, timeout = 4000)
    public void testGroupMinimumConstraintViolationThrowsException() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withLongName("optA").create();
        final Option optB = obuilder.withLongName("optB").create();

        final Group group = new GroupBuilder()
            .withMinimum(1)
            .withOption(optA)
            .withOption(optB)
            .create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(group, new ArrayList());
        // No option added, minimum 1 required
        group.validate(cmd);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindOptionRecursionInNestedHierarchy() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option deepChild = obuilder.withLongName("deep-child").withShortName("dc").create();
        final Group childGroup = gbuilder.withName("childGroup").withOption(deepChild).create();
        final Option parent = obuilder.reset().withLongName("parent").withChildren(childGroup).create();
        final Group root = gbuilder.reset().withName("root").withOption(parent).create();

        assertSame("Must find top-level parent by trigger", parent, root.findOption("--parent"));
        assertSame("Must recursively find nested child by long trigger", deepChild, root.findOption("--deep-child"));
        assertSame("Must recursively find nested child by short trigger", deepChild, root.findOption("-dc"));
        assertNull("Non-existent trigger returns null", root.findOption("--not-there"));
    }

    @Test(timeout = 4000)
    public void testOptionMetadataAndGetters() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder
            .withLongName("config")
            .withShortName("c")
            .withDescription("Configuration file path")
            .withId(101)
            .withRequired(true)
            .create();

        assertEquals("Configuration file path", opt.getDescription());
        assertEquals(101, opt.getId());
        assertTrue(opt.isRequired());
        assertEquals("--config", opt.getPreferredName());

        final Set triggers = opt.getTriggers();
        assertNotNull(triggers);
        assertTrue(triggers.contains("--config"));
        assertTrue(triggers.contains("-c"));

        final Set prefixes = opt.getPrefixes();
        assertNotNull(prefixes);
        assertTrue(prefixes.contains("--"));
        assertTrue(prefixes.contains("-"));
    }

    @Test(timeout = 4000)
    public void testSwitchMetadataAndGetters() {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option sw = sbuilder
            .withName("trace")
            .withDescription("Toggle trace mode")
            .withId(202)
            .create();

        assertEquals("Toggle trace mode", sw.getDescription());
        assertEquals(202, sw.getId());
        assertFalse(sw.isRequired());
        assertEquals("+trace", sw.getPreferredName());

        final Set triggers = sw.getTriggers();
        assertNotNull(triggers);
        assertTrue(triggers.contains("+trace"));
        assertTrue(triggers.contains("-trace"));

        final Set prefixes = sw.getPrefixes();
        assertNotNull(prefixes);
        assertTrue(prefixes.contains("+"));
        assertTrue(prefixes.contains("-"));
    }
}