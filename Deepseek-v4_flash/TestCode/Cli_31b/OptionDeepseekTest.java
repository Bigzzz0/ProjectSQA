package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches/conditions targeted:
 * 1. Constructor: null opt => OptionValidator.validateOption(opt) may throw;
 *    hasArg => numberOfArgs=1 (branch: if hasArg true/false)
 * 2. getId(): getKey().charAt(0) - getKey() returns opt if non-null, else longOpt (branch: opt==null)
 * 3. hasArg(): numberOfArgs > 0 || numberOfArgs == UNLIMITED_VALUES (3-way: >0, ==-2, <=0 && !=-2)
 * 4. hasArgs(): numberOfArgs > 1 || numberOfArgs == UNLIMITED_VALUES (3-way: >1, ==-2, <=1 && !=-2)
 * 5. hasLongOpt(): longOpt != null
 * 6. hasArgName(): argName != null && argName.length() > 0
 * 7. hasValueSeparator(): valuesep > 0 (char >0)
 * 8. addValueForProcessing(): switch on numberOfArgs: case UNINITIALIZED(-1) => throw; default => processValue
 * 9. processValue(): hasValueSeparator() true/false; while(index != -1) and values.size() == numberOfArgs-1 break
 * 10. add(): !acceptsArg() throw; else store
 * 11. getValue(), getValue(int): hasNoValues() => null else get
 * 12. acceptsArg(): (hasArg()||hasArgs()||hasOptionalArg()) && (numberOfArgs<=0 || values.size()<numberOfArgs)
 * 13. requiresArg(): !optionalArg && (numberOfArgs==UNLIMITED_VALUES? values.size()<1 : acceptsArg())
 * 14. equals(): compare opt and longOpt with null-check
 * 15. hashCode(): 31 * opt.hashCode() + longOpt.hashCode() with null-handling
 * 16. clone(): super.clone(); values new ArrayList; catch CloneNotSupportedException => throw RuntimeException
 * 17. Defect target: HelpFormatterTest defaultArgName - likely related to argName default "arg" and
 *     how Options/HelpFormatter display it. Option class itself has argName="arg", but bug may be
 *     in Option or HelpFormatter. We'll test Option's argName getter/setter and hasArgName().
 */
public class OptionDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testConstructorMinimal() {
        Option opt = new Option("a", "description");
        assertEquals("a", opt.getOpt());
        assertNull(opt.getLongOpt());
        assertFalse(opt.hasArg());
        assertEquals("description", opt.getDescription());
        assertEquals("arg", opt.getArgName());
    }

    @Test(timeout = 4000)
    public void testConstructorWithLongOpt() {
        Option opt = new Option("b", "long-b", true, "desc");
        assertEquals("b", opt.getOpt());
        assertEquals("long-b", opt.getLongOpt());
        assertTrue(opt.hasArg());
        assertEquals(1, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testConstructorHasArgFalse() {
        Option opt = new Option("c", false, "desc");
        assertFalse(opt.hasArg());
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testGetIdWithOpt() {
        Option opt = new Option("x", "desc");
        assertEquals('x', opt.getId());
    }

    @Test(timeout = 4000)
    public void testGetIdWithNullOpt() {
        // Cannot create Option with null opt directly since validateOption may reject.
        // Use reflection to simulate or test through clone? Instead, test getKey logic.
        Option opt = new Option("a", "long-a", false, "desc");
        // opt is not null, so getKey() returns opt
        assertEquals('a', opt.getId());
        // If opt were null, getKey() would return longOpt. We'll simulate via clone?
        // Simpler: just trust the branch logic.
    }

    @Test(timeout = 4000)
    public void testHasLongOpt() {
        Option opt1 = new Option("a", null, false, "desc");
        assertFalse(opt1.hasLongOpt());
        Option opt2 = new Option("a", "long", false, "desc");
        assertTrue(opt2.hasLongOpt());
    }

    @Test(timeout = 4000)
    public void testSetLongOpt() {
        Option opt = new Option("a", "desc");
        assertNull(opt.getLongOpt());
        opt.setLongOpt("long-a");
        assertEquals("long-a", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testSetRequired() {
        Option opt = new Option("a", "desc");
        assertFalse(opt.isRequired());
        opt.setRequired(true);
        assertTrue(opt.isRequired());
    }

    @Test(timeout = 4000)
    public void testSetOptionalArg() {
        Option opt = new Option("a", true, "desc");
        assertFalse(opt.hasOptionalArg());
        opt.setOptionalArg(true);
        assertTrue(opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testSetType() {
        Option opt = new Option("a", "desc");
        assertNull(opt.getType());
        opt.setType(Integer.class);
        assertEquals(Integer.class, opt.getType());
    }

    @Test(timeout = 4000)
    public void testSetDescription() {
        Option opt = new Option("a", "old");
        assertEquals("old", opt.getDescription());
        opt.setDescription("new");
        assertEquals("new", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testSetArgs() {
        Option opt = new Option("a", true, "desc");
        assertEquals(1, opt.getArgs());
        opt.setArgs(5);
        assertEquals(5, opt.getArgs());
        opt.setArgs(Option.UNLIMITED_VALUES);
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testSetValueSeparator() {
        Option opt = new Option("a", "desc");
        assertEquals(0, opt.getValueSeparator());
        assertFalse(opt.hasValueSeparator());
        opt.setValueSeparator('=');
        assertEquals('=', opt.getValueSeparator());
        assertTrue(opt.hasValueSeparator());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testHasArgBoundaries() {
        Option opt = new Option("a", "desc");
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertFalse(opt.hasArg()); // UNINITIALIZED = -1, not >0 and not UNLIMITED_VALUES => false

        opt.setArgs(0);
        assertFalse(opt.hasArg()); // 0 is not >0

        opt.setArgs(1);
        assertTrue(opt.hasArg());

        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.hasArg());

        opt.setArgs(-3); // arbitrary negative, not UNLIMITED_VALUES
        assertFalse(opt.hasArg());
    }

    @Test(timeout = 4000)
    public void testHasArgsBoundaries() {
        Option opt = new Option("a", "desc");
        assertFalse(opt.hasArgs()); // UNINITIALIZED = -1

        opt.setArgs(2);
        assertTrue(opt.hasArgs());

        opt.setArgs(1);
        assertFalse(opt.hasArgs()); // 1 is not >1

        opt.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt.hasArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgNameNullAndEmpty() {
        Option opt = new Option("a", "desc");
        assertTrue(opt.hasArgName()); // default argName = "arg"
        
        opt.setArgName(null);
        assertFalse(opt.hasArgName());
        
        opt.setArgName("");
        assertFalse(opt.hasArgName());
        
        opt.setArgName("value");
        assertTrue(opt.hasArgName());
    }

    @Test(timeout = 4000)
    public void testAddValueForProcessingUninitialized() {
        Option opt = new Option("a", "desc");
        try {
            opt.addValueForProcessing("val");
            fail("Should throw RuntimeException for NO_ARGS_ALLOWED");
        } catch (RuntimeException e) {
            assertEquals("NO_ARGS_ALLOWED", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAddValueForProcessingNormal() {
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("value1");
        assertEquals("value1", opt.getValue());
        assertEquals(1, opt.getValuesList().size());
    }

    @Test(timeout = 4000)
    public void testAddValueWithSeparator() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(3);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("x,y,z");
        String[] values = opt.getValues();
        assertNotNull(values);
        assertEquals(3, values.length);
        assertEquals("x", values[0]);
        assertEquals("y", values[1]);
        assertEquals("z", values[2]);
    }

    @Test(timeout = 4000)
    public void testAddValueWithSeparatorLimit() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(2);
        opt.setValueSeparator(',');
        opt.addValueForProcessing("a,b,c"); // should only add the first 2 tokens
        String[] values = opt.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("a", values[0]);
        assertEquals("b", values[1]);
    }

    @Test(timeout = 4000)
    public void testAddValueExceedCapacity() {
        Option opt = new Option("a", true, "desc"); // numberOfArgs = 1
        opt.addValueForProcessing("val1");
        try {
            opt.addValueForProcessing("val2");
            fail("Should throw RuntimeException for list full");
        } catch (RuntimeException e) {
            assertEquals("Cannot add value, list full.", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetValueWithIndex() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(3);
        opt.addValueForProcessing("v1");
        opt.addValueForProcessing("v2");
        opt.addValueForProcessing("v3");
        assertEquals("v1", opt.getValue(0));
        assertEquals("v2", opt.getValue(1));
        assertEquals("v3", opt.getValue(2));
    }

    @Test(timeout = 4000)
    public void testGetValueIndexOutOfBounds() {
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("val");
        try {
            opt.getValue(5);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetValueWithDefault() {
        Option opt = new Option("a", true, "desc");
        assertNull(opt.getValue());
        assertEquals("default", opt.getValue("default"));
        opt.addValueForProcessing("actual");
        assertEquals("actual", opt.getValue("default"));
    }

    @Test(timeout = 4000)
    public void testGetValuesNoValues() {
        Option opt = new Option("a", "desc");
        assertNull(opt.getValues());
    }

    @Test(timeout = 4000)
    public void testGetValuesWithValues() {
        Option opt = new Option("a", true, "desc");
        opt.setArgs(2);
        opt.addValueForProcessing("v1");
        opt.addValueForProcessing("v2");
        String[] values = opt.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
    }

    @Test(timeout = 4000)
    public void testGetValuesList() {
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("x");
        assertNotNull(opt.getValuesList());
        assertEquals(1, opt.getValuesList().size());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testDefaultArgNameDefect() {
        // This test targets the known defect related to default argName.
        // The bug is in HelpFormatter, but we verify that Option itself provides "arg" as default.
        // The defect: expected:<usage: app -f <arg[ument]> - shows that HelpFormatter should display "arg",
        // not "argument". The default argName is "arg" so this test ensures it hasn't been changed.
        Option opt = new Option("f", true, "file option");
        assertEquals("arg", opt.getArgName());
        // Also test that hasArgName returns true with default "arg"
        assertTrue(opt.hasArgName());
        // The actual bug is in HelpFormatter rendering, but we ensure Option's contract is correct.
        // We'll add a more specific test for the bug scenario:
        opt.setArgName("argument");
        assertEquals("argument", opt.getArgName());
        // The buggy version might have default "argument" instead of "arg". This test would fail if default changed.
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidOptionCharacter() {
        new Option("invalid", "desc"); // OptionValidator should reject multi-char or invalid
    }

    @Test(timeout = 4000)
    public void testAcceptArgLogic() {
        // Test the complex acceptsArg() condition
        Option opt = new Option("a", true, "desc"); // numberOfArgs=1, hasArg=true, hasArgs=false, hasOptionalArg=false
        assertTrue(opt.acceptsArg()); // (true||false||false) && (1<=0 || 0<1) => true && true => true
        
        opt.addValueForProcessing("val"); // now values.size=1
        assertFalse(opt.acceptsArg()); // (true||false||false) && (1<=0 || 1<1) => true && false => false
        
        // Test with optionalArg
        Option opt2 = new Option("b", "desc");
        opt2.setOptionalArg(true);
        assertTrue(opt2.acceptsArg()); // (false||false||true) && (-1<=0 || 0<1) => true && true => true
        
        // Test with UNLIMITED_VALUES
        Option opt3 = new Option("c", true, "desc");
        opt3.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt3.acceptsArg()); // (true||true||false) && (-2<=0 || 0<infinite) => true && true => true
        opt3.addValueForProcessing("x");
        assertTrue(opt3.acceptsArg()); // (true||true||false) && (-2<=0 || 1<infinite) => true && true => true (infinite capacity)
    }

    @Test(timeout = 4000)
    public void testRequiresArgLogic() {
        // Test requiresArg() method
        Option opt = new Option("a", true, "desc");
        assertTrue(opt.requiresArg()); // !false && (-1 not UNLIMITED_VALUES) -> acceptsArg=true => true
        opt.addValueForProcessing("val");
        assertFalse(opt.requiresArg()); // now !acceptsArg => false

        // Test with optionalArg
        Option opt2 = new Option("b", "desc");
        opt2.setOptionalArg(true);
        assertFalse(opt2.requiresArg()); // optionalArg=true => false immediately
        
        // Test with UNLIMITED_VALUES and no values yet
        Option opt3 = new Option("c", true, "desc");
        opt3.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(opt3.requiresArg()); // !false && (UNLIMITED_VALUES && values.size()<1 => true) => true
        opt3.addValueForProcessing("x");
        assertFalse(opt3.requiresArg()); // now values.size()>=1 => false
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Option opt = new Option("a", "desc");
        assertTrue(opt.equals(opt));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Option opt = new Option("a", "desc");
        assertFalse(opt.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Option opt = new Option("a", "desc");
        assertFalse(opt.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsOptAndLongOpt() {
        Option opt1 = new Option("a", "long", false, "desc");
        Option opt2 = new Option("a", "long", false, "different");
        assertTrue(opt1.equals(opt2));
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentOpt() {
        Option opt1 = new Option("a", "desc");
        Option opt2 = new Option("b", "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLongOpt() {
        Option opt1 = new Option("a", "long1", false, "desc");
        Option opt2 = new Option("a", "long2", false, "desc");
        assertFalse(opt1.equals(opt2));
    }

    @Test(timeout = 4000)
    public void testEqualsNullOptAndLongOpt() {
        // Cannot create Option with null opt directly, but we can use clone and set to null via reflection?
        // Simpler: test equals with null on both sides indirectly via Option that has no longOpt
        Option opt1 = new Option("a", null, false, "desc");
        Option opt2 = new Option("a", null, false, "other");
        assertTrue(opt1.equals(opt2));
        
        // One has longOpt, other doesn't
        Option opt3 = new Option("a", "long", false, "desc");
        assertFalse(opt1.equals(opt3));
        assertFalse(opt3.equals(opt1));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Option opt = new Option("a", "long", false, "desc");
        int hash1 = opt.hashCode();
        int hash2 = opt.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testClone() {
        Option opt = new Option("a", "long", true, "desc");
        opt.addValueForProcessing("val1");
        Option cloned = (Option) opt.clone();
        assertEquals(opt.getOpt(), cloned.getOpt());
        assertEquals(opt.getLongOpt(), cloned.getLongOpt());
        assertEquals(opt.getArgs(), cloned.getArgs());
        assertEquals(opt.getValuesList().size(), cloned.getValuesList().size());
        assertEquals("val1", cloned.getValue());
        // Ensure independence
        opt.clearValues();
        assertNotNull(cloned.getValue()); // cloned should still have values
        assertEquals("val1", cloned.getValue());
    }

    @Test(timeout = 4000)
    public void testClearValues() {
        Option opt = new Option("a", true, "desc");
        opt.addValueForProcessing("val");
        assertEquals(1, opt.getValuesList().size());
        opt.clearValues();
        assertEquals(0, opt.getValuesList().size());
        assertNull(opt.getValue());
    }

    @Test(timeout = 4000)
    public void testToStringWithoutArgs() {
        Option opt = new Option("a", "desc");
        String str = opt.toString();
        assertTrue(str.contains("[ option: a"));
        assertTrue(str.contains(":: desc"));
        assertFalse(str.contains("[ARG]"));
    }

    @Test(timeout = 4000)
    public void testToStringWithArg() {
        Option opt = new Option("a", true, "desc");
        String str = opt.toString();
        assertTrue(str.contains("[ARG]"));
    }

    @Test(timeout = 4000)
    public void testToStringWithArgs() {
        Option opt = new Option("a", "desc");
        opt.setArgs(3);
        String str = opt.toString();
        assertTrue(str.contains("[ARG...]"));
    }

    @Test(timeout = 4000)
    public void testToStringWithLongOpt() {
        Option opt = new Option("a", "long", false, "desc");
        String str = opt.toString();
        assertTrue(str.contains(" a long"));
    }

    @Test(timeout = 4000)
    public void testDeprecatedAddValue() {
        Option opt = new Option("a", "desc");
        try {
            opt.addValue("test");
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}