package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.cli.OptionGroup
 * 
 * Decision Branches Covered:
 * 1. setSelected(Option):
 *    - option == null (true/false) -> reset selected to null
 *    - selected == null (true/false) -> first selection
 *    - selected.equals(option.getOpt()) (true/false) -> reselect same option
 *    - else -> throw AlreadySelectedException
 * 
 * 2. addOption(Option):
 *    - optionMap.put() with key from option.getKey()
 *    - Handles null option (NPE path - not explicitly guarded)
 * 
 * 3. getNames() / getOptions():
 *    - Empty map -> empty collection
 *    - Non-empty map -> correct keys/values
 * 
 * 4. setRequired(boolean) / isRequired():
 *    - true/false transitions
 * 
 * 5. toString():
 *    - Empty group -> "[]"
 *    - Single option with short opt -> "-x description"
 *    - Single option with long opt only -> "--long description"
 *    - Multiple options -> comma-separated
 *    - Null description handling
 * 
 * 6. getSelected():
 *    - Initial state (null)
 *    - After setSelected(null)
 *    - After setSelected(option)
 * 
 * DEFECT TARGET (from Defects4J):
 * - testOptionGroupLong: When an option with only a long name (no short name)
 *   is selected, the selected field is not properly updated because
 *   setSelected() compares against option.getOpt() which is null for
 *   long-only options. The fix should use option.getKey() which returns
 *   the long name when short name is null.
 * 
 * Expected behavior: setSelected(option) should store the option's key
 * (which is the long name if short name is null) so that getSelected()
 * returns the correct value.
 */
public class OptionGroupDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testAddOptionAndGetOptions() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha option");
        
        assertSame(group, group.addOption(opt));
        Collection options = group.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(opt));
    }

    @Test(timeout = 4000)
    public void testAddMultipleOptionsAndGetNames() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        
        group.addOption(opt1);
        group.addOption(opt2);
        
        Collection names = group.getNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
    }

    @Test(timeout = 4000)
    public void testSetSelectedFirstTime() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        assertEquals("a", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testSetSelectedReselectSameOption() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(opt); // Should not throw
        assertEquals("a", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testSetSelectedNullResets() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(null);
        assertNull(group.getSelected());
    }

    @Test(timeout = 4000)
    public void testSetRequiredAndIsRequired() {
        OptionGroup group = new OptionGroup();
        assertFalse(group.isRequired());
        
        group.setRequired(true);
        assertTrue(group.isRequired());
        
        group.setRequired(false);
        assertFalse(group.isRequired());
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testEmptyGroupGetNames() {
        OptionGroup group = new OptionGroup();
        Collection names = group.getNames();
        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyGroupGetOptions() {
        OptionGroup group = new OptionGroup();
        Collection options = group.getOptions();
        assertNotNull(options);
        assertTrue(options.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetSelectedInitialState() {
        OptionGroup group = new OptionGroup();
        assertNull(group.getSelected());
    }

    @Test(timeout = 4000)
    public void testToStringEmptyGroup() {
        OptionGroup group = new OptionGroup();
        assertEquals("[]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleOptionWithShortOpt() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha desc"));
        assertEquals("[-a Alpha desc]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleOptionWithLongOptOnly() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option(null, "alpha", false, "Alpha desc"));
        assertEquals("[--alpha Alpha desc]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringMultipleOptions() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha desc"));
        group.addOption(new Option("b", "beta", false, "Beta desc"));
        
        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a Alpha desc"));
        assertTrue(result.contains("-b Beta desc"));
        assertTrue(result.contains(", "));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullDescription() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, null));
        assertEquals("[-a null]", group.toString());
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * DEFECT REPRODUCTION TEST:
     * This test targets the known defect where selecting an option with
     * only a long name (no short name) fails to update the selected field.
     * 
     * The bug: setSelected() compares against option.getOpt() which is null
     * for long-only options, so the condition `selected.equals(option.getOpt())`
     * fails and the selected field remains null.
     * 
     * Expected: getSelected() should return the long name "bar"
     * Actual (buggy): getSelected() returns null
     */
    @Test(timeout = 4000)
    public void testSetSelectedLongOptionOnly() throws Exception {
        OptionGroup group = new OptionGroup();
        Option longOpt = new Option(null, "bar", false, "Bar option");
        group.addOption(longOpt);
        
        group.setSelected(longOpt);
        
        // The defect: this assertion fails because selected is null
        // The fix should make this return "bar" (the long name)
        assertEquals("bar", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testSetSelectedLongOptionAfterShortOption() throws Exception {
        OptionGroup group = new OptionGroup();
        Option shortOpt = new Option("a", "alpha", false, "Alpha");
        Option longOpt = new Option(null, "bar", false, "Bar");
        group.addOption(shortOpt);
        group.addOption(longOpt);
        
        group.setSelected(shortOpt);
        assertEquals("a", group.getSelected());
        
        // This should throw because "a" is already selected
        try {
            group.setSelected(longOpt);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSelectedLongOptionReselect() throws Exception {
        OptionGroup group = new OptionGroup();
        Option longOpt = new Option(null, "bar", false, "Bar");
        group.addOption(longOpt);
        
        group.setSelected(longOpt);
        group.setSelected(longOpt); // Reselect same option
        
        // Defect: this fails because selected is null after first call
        assertEquals("bar", group.getSelected());
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(timeout = 4000)
    public void testSetSelectedDifferentOptionThrows() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        
        try {
            group.setSelected(opt2);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            assertEquals(group, e.getOptionGroup());
            assertEquals(opt2, e.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testSetSelectedNullAfterSelection() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(null);
        assertNull(group.getSelected());
        
        // Can select again after reset
        group.setSelected(opt);
        assertEquals("a", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testAddNullOption() {
        OptionGroup group = new OptionGroup();
        try {
            group.addOption(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected - addOption doesn't guard against null
        }
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testMultipleGroupsIndependent() throws Exception {
        OptionGroup group1 = new OptionGroup();
        OptionGroup group2 = new OptionGroup();
        
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        
        group1.addOption(opt1);
        group2.addOption(opt2);
        
        group1.setSelected(opt1);
        group2.setSelected(opt2);
        
        assertEquals("a", group1.getSelected());
        assertEquals("b", group2.getSelected());
    }

    @Test(timeout = 4000)
    public void testOptionGroupWithSameOptionInMultipleGroups() throws Exception {
        OptionGroup group1 = new OptionGroup();
        OptionGroup group2 = new OptionGroup();
        
        Option shared = new Option("x", "xray", false, "Shared");
        
        group1.addOption(shared);
        group2.addOption(shared);
        
        group1.setSelected(shared);
        group2.setSelected(shared);
        
        assertEquals("x", group1.getSelected());
        assertEquals("x", group2.getSelected());
    }

    @Test(timeout = 4000)
    public void testGetNamesReturnsModifiableView() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha"));
        
        Collection names = group.getNames();
        // The returned collection is backed by the map's keySet
        assertNotNull(names);
        assertEquals(1, names.size());
    }

    @Test(timeout = 4000)
    public void testToStringWithMixedOptionTypes() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha desc"));
        group.addOption(new Option(null, "beta", false, "Beta desc"));
        
        String result = group.toString();
        assertTrue(result.contains("-a Alpha desc"));
        assertTrue(result.contains("--beta Beta desc"));
    }

    @Test(timeout = 4000)
    public void testSetSelectedWithOptionNotInGroup() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        Option outside = new Option("z", "zulu", false, "Zulu");
        group.addOption(opt);
        
        // Even though option is not in group, setSelected doesn't validate
        group.setSelected(outside);
        assertEquals("z", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testRepeatedAddOptionOverwrites() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("a", "alpha2", false, "Alpha2");
        
        group.addOption(opt1);
        group.addOption(opt2);
        
        Collection options = group.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(opt2));
        assertFalse(options.contains(opt1));
    }

    @Test(timeout = 4000)
    public void testSelectedPersistsAfterAddOption() throws Exception {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt1);
        group.setSelected(opt1);
        
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt2);
        
        assertEquals("a", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testIsRequiredDefaultFalse() {
        OptionGroup group = new OptionGroup();
        assertFalse(group.isRequired());
    }

    @Test(timeout = 4000)
    public void testToStringWithMultipleLongOptions() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option(null, "alpha", false, "Alpha desc"));
        group.addOption(new Option(null, "beta", false, "Beta desc"));
        
        String result = group.toString();
        assertTrue(result.contains("--alpha Alpha desc"));
        assertTrue(result.contains("--beta Beta desc"));
    }
}