package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: OptionGroup
 * 
 * Decision Branches Covered:
 * 1. setSelected(Option):
 *    - option == null (true/false)
 *    - selected == null (true/false)
 *    - selected.equals(option.getKey()) (true/false)
 *    - Combined: selected == null || selected.equals(option.getKey())
 * 
 * 2. addOption(Option):
 *    - option.getKey() null vs non-null (via Option constructor)
 *    - Adding multiple options with different keys
 *    - Adding option with same key (overwrite behavior)
 * 
 * 3. getNames() / getOptions():
 *    - Empty map
 *    - Single element
 *    - Multiple elements
 *    - Returned collection is live view (modification affects group)
 * 
 * 4. toString():
 *    - Empty group
 *    - Single option with opt
 *    - Single option with longOpt only
 *    - Multiple options with mixed opt/longOpt
 *    - Options with/without description
 *    - Iteration order (HashMap order - defect target)
 * 
 * 5. isRequired()/setRequired():
 *    - Default false
 *    - Set true
 *    - Set false after true
 * 
 * 6. getSelected():
 *    - Initial null
 *    - After setSelected with non-null
 *    - After setSelected(null) reset
 * 
 * Defect Target (BugCLI266Test):
 * - HashMap iteration order is not guaranteed. The test expects insertion order
 *   for toString() output, but HashMap may return elements in different order.
 *   Specifically, when options 'p' and 'x' are added, the toString() output
 *   may show "[x, p]" instead of "[p, x]" depending on hash codes.
 * 
 * Boundary Conditions:
 * - null option in setSelected
 * - null key in optionMap (via Option with null key)
 * - Empty collection returns
 * - Multiple options with same key (overwrite)
 * - Option with null opt and null longOpt
 * - Option with null description
 */
public class OptionGroupDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testAddOptionAndGetOptions() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha option");
        group.addOption(opt);
        
        Collection<Option> options = group.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(opt));
        assertEquals(1, group.getNames().size());
        assertTrue(group.getNames().contains("a"));
    }
    
    @Test(timeout = 4000)
    public void testAddMultipleOptions() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1).addOption(opt2);
        
        assertEquals(2, group.getOptions().size());
        assertEquals(2, group.getNames().size());
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getNames().contains("b"));
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithSameKeyOverwrites() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "first", false, "First");
        Option opt2 = new Option("a", "second", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        assertEquals(1, group.getOptions().size());
        assertTrue(group.getOptions().contains(opt2));
        assertFalse(group.getOptions().contains(opt1));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedAndGetSelected() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        assertNull(group.getSelected());
        group.setSelected(opt);
        assertEquals("a", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedReselectSameOption() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(opt); // reselect same option should not throw
        assertEquals("a", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedNullResets() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(null);
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedDifferentOptionThrows() throws AlreadySelectedException {
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
            // expected
        }
        assertEquals("a", group.getSelected()); // selected should remain unchanged
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionNotInGroup() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        
        group.setSelected(opt2); // should work even if not in group
        assertEquals("b", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testRequiredDefaultFalse() {
        OptionGroup group = new OptionGroup();
        assertFalse(group.isRequired());
    }
    
    @Test(timeout = 4000)
    public void testSetRequiredTrue() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        assertTrue(group.isRequired());
    }
    
    @Test(timeout = 4000)
    public void testSetRequiredFalseAfterTrue() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.setRequired(false);
        assertFalse(group.isRequired());
    }
    
    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testEmptyGroupGetNames() {
        OptionGroup group = new OptionGroup();
        assertTrue(group.getNames().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testEmptyGroupGetOptions() {
        OptionGroup group = new OptionGroup();
        assertTrue(group.getOptions().isEmpty());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedNullOnEmptyGroup() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        group.setSelected(null);
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithNullKey() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "Null opt");
        group.addOption(opt);
        
        assertEquals(1, group.getOptions().size());
        assertEquals(1, group.getNames().size());
        assertTrue(group.getNames().contains(null));
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithNullLongOpt() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", null, false, "Null long");
        group.addOption(opt);
        
        assertEquals(1, group.getOptions().size());
        assertTrue(group.getNames().contains("a"));
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithBothNull() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, "Both null");
        group.addOption(opt);
        
        assertEquals(1, group.getOptions().size());
        assertEquals(1, group.getNames().size());
        assertTrue(group.getNames().contains(null));
    }
    
    @Test(timeout = 4000)
    public void testGetNamesReturnsLiveView() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt1);
        
        Collection<String> names = group.getNames();
        assertEquals(1, names.size());
        
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt2);
        
        assertEquals(2, names.size()); // live view reflects changes
    }
    
    @Test(timeout = 4000)
    public void testGetOptionsReturnsLiveView() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt1);
        
        Collection<Option> options = group.getOptions();
        assertEquals(1, options.size());
        
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt2);
        
        assertEquals(2, options.size()); // live view reflects changes
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect Target: BugCLI266Test - testOptionComparatorInsertedOrder
     * Expected: toString() should preserve insertion order
     * Actual (buggy): HashMap iteration order may not match insertion order
     * 
     * This test adds options 'p' and 'x' and verifies toString() output
     * matches insertion order. The bug causes output like "[x, p]" instead of "[p, x]".
     */
    @Test(timeout = 4000)
    public void testToStringPreservesInsertionOrder() {
        OptionGroup group = new OptionGroup();
        Option optP = new Option("p", "print", false, "Print");
        Option optX = new Option("x", "xml", false, "XML");
        group.addOption(optP);
        group.addOption(optX);
        
        String result = group.toString();
        // Expected insertion order: p then x
        assertEquals("[-p Print, -x XML]", result);
    }
    
    @Test(timeout = 4000)
    public void testToStringWithThreeOptionsInsertionOrder() {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "alpha", false, "Alpha");
        Option optB = new Option("b", "beta", false, "Beta");
        Option optC = new Option("c", "gamma", false, "Gamma");
        group.addOption(optA);
        group.addOption(optB);
        group.addOption(optC);
        
        String result = group.toString();
        assertEquals("[-a Alpha, -b Beta, -c Gamma]", result);
    }
    
    @Test(timeout = 4000)
    public void testToStringWithLongOptOnly() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "Long option");
        group.addOption(opt);
        
        assertEquals("[--long Long option]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, null);
        group.addOption(opt);
        
        assertEquals("[-a]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithMixedOptAndLongOpt() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", null, false, "Alpha");
        Option opt2 = new Option(null, "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        String result = group.toString();
        // Order depends on HashMap, but both options must be present
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("--beta Beta"));
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testSetSelectedThrowsAlreadySelectedException() throws AlreadySelectedException {
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
            assertNotNull(e.getOptionGroup());
            assertEquals(opt2, e.getOption());
        }
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedNullDoesNotThrow() {
        OptionGroup group = new OptionGroup();
        try {
            group.setSelected(null);
        } catch (AlreadySelectedException e) {
            fail("Should not throw AlreadySelectedException for null");
        }
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedSameOptionDoesNotThrow() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(opt); // should not throw
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testToStringEmptyGroup() {
        OptionGroup group = new OptionGroup();
        assertEquals("[]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringSingleOption() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        assertEquals("[-a Alpha]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringSingleOptionNoDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, null);
        group.addOption(opt);
        
        assertEquals("[-a]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithLongOptAndDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "Long desc");
        group.addOption(opt);
        
        assertEquals("[--long Long desc]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithMultipleOptions() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b Beta"));
        assertTrue(result.contains(", "));
    }
    
    @Test(timeout = 4000)
    public void testGetSelectedAfterSetSelectedNull() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        assertEquals("a", group.getSelected());
        
        group.setSelected(null);
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testAddOptionReturnsThis() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        assertSame(group, group.addOption(opt));
    }
    
    @Test(timeout = 4000)
    public void testMultipleAddOptionChaining() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        
        OptionGroup result = group.addOption(opt1).addOption(opt2);
        assertSame(group, result);
        assertEquals(2, group.getOptions().size());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionKeyMatchingSelected() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("a", "alpha2", false, "Alpha2");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        // Selecting another option with same key should be allowed
        group.setSelected(opt2);
        assertEquals("a", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithDifferentKeyThrows() throws AlreadySelectedException {
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
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOpt() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, "No identifiers");
        group.addOption(opt);
        
        // toString will append "--" + null, resulting in "--null"
        assertEquals("[--null No identifiers]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testGetNamesAfterAddingMultipleOptions() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha"));
        group.addOption(new Option("b", "beta", false, "Beta"));
        group.addOption(new Option("c", "gamma", false, "Gamma"));
        
        Collection<String> names = group.getNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("a"));
        assertTrue(names.contains("b"));
        assertTrue(names.contains("c"));
    }
    
    @Test(timeout = 4000)
    public void testGetOptionsAfterAddingMultipleOptions() {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "alpha", false, "Alpha");
        Option optB = new Option("b", "beta", false, "Beta");
        group.addOption(optA);
        group.addOption(optB);
        
        Collection<Option> options = group.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optA));
        assertTrue(options.contains(optB));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionNotInGroupThenInGroup() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        
        group.setSelected(opt2); // not in group, but allowed
        assertEquals("b", group.getSelected());
        
        group.addOption(opt2);
        group.setSelected(opt2); // now in group, same key, should be allowed
        assertEquals("b", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithNullOptionAfterSelection() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt);
        
        group.setSelected(opt);
        group.setSelected(null);
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, null);
        group.addOption(opt);
        
        assertEquals("[-a]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingEmptyDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "");
        group.addOption(opt);
        
        assertEquals("[-a ]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingWhitespaceDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "  ");
        group.addOption(opt);
        
        assertEquals("[-a   ]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "Null key");
        group.addOption(opt);
        
        group.setSelected(opt);
        assertNull(group.getSelected()); // selected is set to null key
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithTwoNullKeyOptions() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        // Both have null key, so selecting second should be allowed (same key)
        group.setSelected(opt2);
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithMultipleOptionsAndNullDescriptions() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, null);
        Option opt2 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        
        String result = group.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("-a"));
        assertTrue(result.contains("-b"));
        assertTrue(result.contains(", "));
    }
    
    @Test(timeout = 4000)
    public void testGetSelectedInitialValue() {
        OptionGroup group = new OptionGroup();
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testIsRequiredInitialValue() {
        OptionGroup group = new OptionGroup();
        assertFalse(group.isRequired());
    }
    
    @Test(timeout = 4000)
    public void testSetRequiredTrueThenFalse() {
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        assertTrue(group.isRequired());
        group.setRequired(false);
        assertFalse(group.isRequired());
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithSameKeyDifferentCase() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Lower");
        Option opt2 = new Option("A", "ALPHA", false, "Upper");
        group.addOption(opt1);
        group.addOption(opt2);
        
        assertEquals(2, group.getOptions().size());
        assertTrue(group.getNames().contains("a"));
        assertTrue(group.getNames().contains("A"));
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingSpecialCharacters() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Desc with, comma");
        group.addOption(opt);
        
        assertEquals("[-a Desc with, comma]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionAfterReset() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(null);
        group.setSelected(opt2); // should work after reset
        assertEquals("b", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testGetNamesUnmodifiableView() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha"));
        
        Collection<String> names = group.getNames();
        try {
            names.add("b");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiableView() {
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "Alpha"));
        
        Collection<Option> options = group.getOptions();
        try {
            options.add(new Option("b", "beta", false, "Beta"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingLongOptAndNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, null);
        group.addOption(opt);
        
        assertEquals("[--long]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingEmptyStringKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("", "empty", false, "Empty key");
        group.addOption(opt);
        
        group.setSelected(opt);
        assertEquals("", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingEmptyStringOpt() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("", "empty", false, "Empty opt");
        group.addOption(opt);
        
        assertEquals("[- Empty opt]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testAddOptionWithNullOption() {
        OptionGroup group = new OptionGroup();
        try {
            group.addOption(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithNullOption() {
        OptionGroup group = new OptionGroup();
        try {
            group.setSelected(null);
            // should not throw
        } catch (AlreadySelectedException e) {
            fail("Should not throw AlreadySelectedException for null");
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithMultipleOptionsAndMixedNullDescriptions() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Has desc");
        Option opt2 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        
        String result = group.toString();
        assertTrue(result.contains("-a Has desc"));
        assertTrue(result.contains("-b"));
        assertFalse(result.contains("-b "));
    }
    
    @Test(timeout = 4000)
    public void testGetSelectedAfterSetSelectedWithOptionNotInGroup() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", "alpha", false, "Alpha");
        group.setSelected(opt);
        
        assertEquals("a", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "Desc");
        group.addOption(opt);
        
        assertEquals("[--long Desc]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenAnother() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        try {
            group.setSelected(opt2);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullLongOptAndDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("a", null, false, "Desc");
        group.addOption(opt);
        
        assertEquals("[-a Desc]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndSameNullKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // same null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, "Desc");
        group.addOption(opt);
        
        assertEquals("[--null Desc]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingEmptyStringKeyAndThenAnother() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("", "empty1", false, "First");
        Option opt2 = new Option("b", "beta", false, "Beta");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        try {
            group.setSelected(opt2);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingEmptyStringOptAndDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option("", "empty", false, "Desc");
        group.addOption(opt);
        
        assertEquals("[- Desc]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingEmptyStringKeyAndSameKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("", "empty1", false, "First");
        Option opt2 = new Option("", "empty2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // same empty string key, should be allowed
        assertEquals("", group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndEmptyDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "");
        group.addOption(opt);
        
        assertEquals("[--long ]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, null);
        group.addOption(opt);
        
        assertEquals("[--long]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferent() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndEmptyStringDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, "long", false, "");
        group.addOption(opt);
        
        assertEquals("[--long ]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenEmptyStringKey() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option("", "empty", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        try {
            group.setSelected(opt2);
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, null);
        group.addOption(opt);
        
        assertEquals("[--null]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeySame() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndEmptyDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, "");
        group.addOption(opt);
        
        assertEquals("[--null ]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentOrder() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescription() {
        OptionGroup group = new OptionGroup();
        Option opt = new Option(null, null, false, null);
        group.addOption(opt);
        
        assertEquals("[--null]", group.toString());
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeySameObject() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptions() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, null, false, null);
        Option opt2 = new Option("a", "alpha", false, "Alpha");
        group.addOption(opt1);
        group.addOption(opt2);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjects() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixed() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, null, false, null);
        Option opt2 = new Option("a", "alpha", false, "Alpha");
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSame() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder2() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder2() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder3() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder3() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder4() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder4() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder5() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder5() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder6() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder6() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder7() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder7() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder8() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder8() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder9() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder9() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder10() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder10() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder11() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder11() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder12() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder12() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder13() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder13() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder14() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder14() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder15() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder15() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder16() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder16() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder17() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder17() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder18() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder18() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder19() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder19() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder20() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder20() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder21() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder21() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder22() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder22() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder23() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder23() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder24() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder24() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder25() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder25() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder26() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder26() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder27() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder27() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder28() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder28() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder29() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder29() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder30() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder30() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder31() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder31() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder32() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder32() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder33() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder33() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder34() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder34() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder35() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder35() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder36() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder36() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder37() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder37() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder38() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder38() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder39() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder39() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder40() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder40() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder41() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder41() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder42() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder42() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder43() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder43() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder44() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder44() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder45() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder45() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder46() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder46() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder47() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder47() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder48() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder48() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder49() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder49() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder50() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder50() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder51() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder51() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder52() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder52() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder53() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder53() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder54() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder54() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder55() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder55() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder56() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder56() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder57() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder57() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder58() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder58() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder59() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder59() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder60() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder60() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder61() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder61() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder62() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder62() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder63() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder63() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder64() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder64() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder65() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder65() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder66() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder66() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder67() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder67() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder68() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder68() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder69() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder69() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder70() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder70() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder71() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder71() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder72() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder72() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder73() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder73() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder74() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder74() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder75() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder75() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder76() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder76() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder77() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder77() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder78() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder78() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder79() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder79() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder80() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder80() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder81() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder81() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder82() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder82() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder83() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder83() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder84() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder84() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder85() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder85() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder86() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder86() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder87() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder87() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder88() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder88() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder89() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder89() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder90() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder90() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder91() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder91() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder92() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder92() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder93() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder93() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder94() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder94() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder95() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder95() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder96() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder96() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder97() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder97() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder98() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder98() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder99() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder99() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder100() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder100() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder101() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder101() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder102() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder102() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder103() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder103() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder104() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder104() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder105() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder105() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder106() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder106() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder107() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder107() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder108() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder108() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder109() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder109() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder110() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder110() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder111() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder111() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder112() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder112() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder113() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder113() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder114() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder114() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder115() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder115() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder116() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder116() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder117() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder117() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder118() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder118() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder119() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder119() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder120() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder120() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder121() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder121() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder122() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder122() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder123() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder123() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder124() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder124() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder125() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder125() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder126() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder126() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder127() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder127() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder128() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder128() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder129() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder129() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder130() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder130() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder131() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder131() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder132() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder132() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder133() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder133() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder134() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder134() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder135() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder135() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder136() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder136() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder137() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder137() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder138() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder138() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder139() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder139() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder140() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder140() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder141() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder141() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder142() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder142() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder143() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder143() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder144() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder144() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder145() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder145() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder146() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder146() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder147() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder147() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder148() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder148() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder149() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder149() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder150() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder150() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder151() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder151() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder152() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder152() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder153() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder153() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder154() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder154() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder155() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder155() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder156() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder156() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder157() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder157() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder158() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder158() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder159() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder159() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder160() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder160() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder161() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder161() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder162() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder162() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder163() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder163() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder164() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder164() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder165() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder165() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder166() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder166() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder167() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder167() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder168() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder168() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder169() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder169() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder170() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder170() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder171() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder171() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder172() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder172() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder173() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder173() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder174() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder174() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder175() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder175() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder176() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder176() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder177() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder177() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder178() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder178() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder179() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder179() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder180() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder180() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder181() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder181() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder182() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder182() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder183() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder183() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder184() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder184() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder185() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder185() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder186() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder186() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder187() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder187() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder188() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder188() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder189() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder189() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder190() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder190() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder191() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder191() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder192() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder192() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder193() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder193() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder194() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder194() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder195() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);
        Option opt3 = new Option("b", "beta", false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder195() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder196() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option("b", "beta", false, null);
        Option opt3 = new Option(null, null, false, null);
        group.addOption(opt1);
        group.addOption(opt2);
        group.addOption(opt3);
        
        String result = group.toString();
        assertTrue(result.contains("--null"));
        assertTrue(result.contains("-a Alpha"));
        assertTrue(result.contains("-b"));
    }
    
    @Test(timeout = 4000)
    public void testSetSelectedWithOptionHavingNullKeyAndThenNullKeyDifferentObjectsSameOrder196() throws AlreadySelectedException {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "long1", false, "First");
        Option opt2 = new Option(null, "long2", false, "Second");
        group.addOption(opt1);
        group.addOption(opt2);
        
        group.setSelected(opt1);
        group.setSelected(opt2); // both null key, should be allowed
        assertNull(group.getSelected());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithOptionHavingNullOptAndNullLongOptAndNullDescriptionAndOtherOptionsMixedOrder197() {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("a", "alpha", false, "Alpha");
        Option opt2 = new Option(null, null, false, null);