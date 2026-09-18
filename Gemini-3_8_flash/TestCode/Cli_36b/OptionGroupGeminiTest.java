package org.apache.commons.cli;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.OptionGroup
 *
 * Decision / Condition Coverage Targets:
 * 1. addOption(Option):
 *    - Store mapping of option.getKey() -> option
 * 2. getNames():
 *    - Return optionMap.keySet()
 * 3. getOptions():
 *    - Return optionMap.values()
 * 4. setSelected(Option):
 *    - Branch A1: option == null -> resets selected = null, returns immediately.
 *    - Branch B1: selected == null -> sets selected = option.getKey().
 *    - Branch B2: selected != null && selected.equals(option.getKey()) -> reselection allowed, no-op/keeps selected.
 *    - Branch B3: selected != null && !selected.equals(option.getKey()) -> throws AlreadySelectedException(this, option).
 * 5. getSelected():
 *    - Return current selected option key (or null).
 * 6. setRequired(boolean) / isRequired():
 *    - State flag toggle (true / false).
 * 7. toString():
 *    - Branch C1: empty options -> "[]"
 *    - Branch C2: option.getOpt() != null -> prefix "-" + opt
 *    - Branch C3: option.getOpt() == null (longOpt only) -> prefix "--" + longOpt
 *    - Branch C4: option.getDescription() != null -> append " " + description
 *    - Branch C5: option.getDescription() == null -> no description appended
 *    - Branch C6: iter.hasNext() (intermediate item) -> append ", "
 *    - Branch C7: !iter.hasNext() (last item) -> no trailing delimiter
 *
 * Defect Analysis (Bug CLI-266 / Defect Tracking):
 * - Defect: OptionGroup historically used HashMap instead of LinkedHashMap.
 *   This causes loss of insertion order when retrieving options/names or rendering toString().
 *   When options are inserted in a specific order (e.g. key hashing to higher bucket first),
 *   HashMap iterates in bucket order, violating insertion order expectations.
 */
public class OptionGroupGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndGetOptionsAndNames()
    {
        OptionGroup group = new OptionGroup();
        assertTrue(group.getNames().isEmpty());
        assertTrue(group.getOptions().isEmpty());

        Option optA = new Option("a", "alpha", false, "Option Alpha");
        Option optB = new Option("b", "beta", true, "Option Beta");

        OptionGroup result = group.addOption(optA);
        assertSame("addOption should support method chaining", group, result);
        group.addOption(optB);

        Collection<String> names = group.getNames();
        Collection<Option> options = group.getOptions();

        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));

        assertEquals(2, options.size());
        assertTrue(options.contains(optA));
        assertTrue(options.contains(optB));
    }

    @Test(timeout = 4000)
    public void testSetSelectedAndGetSelected() throws Exception
    {
        OptionGroup group = new OptionGroup();
        assertNull(group.getSelected());

        Option opt1 = new Option("o1", "first option");
        group.addOption(opt1);

        group.setSelected(opt1);
        assertEquals("o1", group.getSelected());

        // Reselecting the same option should be permissible without throwing exception
        group.setSelected(opt1);
        assertEquals("o1", group.getSelected());

        // Resetting selected to null
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test(timeout = 4000)
    public void testRequiredStateTransitions()
    {
        OptionGroup group = new OptionGroup();
        assertFalse("Default required state should be false", group.isRequired());

        group.setRequired(true);
        assertTrue("Required state should be true", group.isRequired());

        group.setRequired(false);
        assertFalse("Required state should be toggled back to false", group.isRequired());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & ToString Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringEmptyGroup()
    {
        OptionGroup group = new OptionGroup();
        assertEquals("[]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleShortOptionWithDescription()
    {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("f", "file", false, "input file path");
        group.addOption(opt);

        assertEquals("[-f input file path]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleShortOptionWithoutDescription()
    {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("f", false, null);
        group.addOption(opt);

        assertEquals("[-f]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleLongOptionOnlyWithDescription()
    {
        OptionGroup group = new OptionGroup();
        // opt is null, only longOpt is provided
        Option opt = new Option(null, "config", false, "configuration file");
        group.addOption(opt);

        assertEquals("[--config configuration file]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleLongOptionOnlyWithoutDescription()
    {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "verbose", false, null);
        group.addOption(opt);

        assertEquals("[--verbose]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringMultipleOptionsContainsDelimiters()
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "first");
        Option opt2 = new Option("b", "second");
        group.addOption(opt1);
        group.addOption(opt2);

        String str = group.toString();
        assertTrue("Should start with [", str.startsWith("["));
        assertTrue("Should end with ]", str.endsWith("]"));
        assertTrue("Should contain comma delimiter between options", str.contains(", "));
        assertTrue("Should contain -a first", str.contains("-a first"));
        assertTrue("Should contain -b second", str.contains("-b second"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Bug CLI-266 / Insertion Order)
    // =========================================================================

    /**
     * CLI-266 / Insertion Order preservation check:
     * In defective versions of Commons CLI, OptionGroup internally uses HashMap
     * instead of LinkedHashMap.
     * When options are inserted where the first inserted key has a larger hash
     * bucket than the second key (e.g. 'z' -> bucket 10 vs 'a' -> bucket 1 in 16-bucket map),
     * a HashMap yields 'a' first instead of preserving insertion order 'z' then 'a'.
     * OptionGroup is expected to preserve the insertion order of options.
     */
    @Test(timeout = 4000)
    public void testOptionInsertionOrderPreservedInNamesAndOptions()
    {
        OptionGroup group = new OptionGroup();
        Option optZ = new Option("z", "Option Z description");
        Option optA = new Option("a", "Option A description");

        group.addOption(optZ);
        group.addOption(optA);

        Iterator<String> nameIter = group.getNames().iterator();
        assertTrue(nameIter.hasNext());
        assertEquals("First inserted option name must be iterated first (preserves insertion order)",
                "z", nameIter.next());
        assertTrue(nameIter.hasNext());
        assertEquals("Second inserted option name must be iterated second",
                "a", nameIter.next());

        Iterator<Option> optIter = group.getOptions().iterator();
        assertTrue(optIter.hasNext());
        assertSame("First inserted option must be iterated first", optZ, optIter.next());
        assertTrue(optIter.hasNext());
        assertSame("Second inserted option must be iterated second", optA, optIter.next());
    }

    /**
     * Dedicated defect check targeting BugCLI266Test scenario where toString / order starts
     * with the first inserted option.
     */
    @Test(timeout = 4000)
    public void testOptionComparatorInsertedOrderDefectCLI266()
    {
        OptionGroup group = new OptionGroup();
        // Insert 'p' first, then 'x'
        Option optP = new Option("p", "print", false, "print option");
        Option optX = new Option("x", "xml", false, "xml option");

        group.addOption(optP);
        group.addOption(optX);

        String groupString = group.toString();
        // The first element rendered in toString() must correspond to the first added option ("p")
        assertTrue("OptionGroup toString should present options in insertion order: expected [-p... to be first",
                groupString.startsWith("[-p"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAlreadySelectedExceptionThrownWhenSelectingDifferentOption()
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("x", "exclusive 1");
        Option opt2 = new Option("y", "exclusive 2");

        group.addOption(opt1);
        group.addOption(opt2);

        try