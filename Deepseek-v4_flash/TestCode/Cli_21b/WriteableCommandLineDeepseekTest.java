package org.apache.commons.cli2;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * White-box test for WriteableCommandLine interface targeting branch coverage,
 * boundary conditions, and the known defect where negative numbers (e.g., "-42")
 * are incorrectly identified as option triggers (see BugCLI150Test).
 *
 * <p>[Branch & Defect Analysis Matrix]</p>
 * <ul>
 *   <li><b>Core functional:</b> addOption, addValue, getUndefaultedValues,
 *       setDefaultValues, addSwitch, setDefaultSwitch, addProperty (both overloads)</li>
 *   <li><b>Boundary & extremes:</b> null arguments, empty strings/lists,
 *       negative numbers, duplicate switches, invalid prefixes</li>
 *   <li><b>Defect-targeted:</b> looksLikeOption("-42") must return false;
 *       looksLikeOption("-a") must return true when option exists</li>
 *   <li><b>Exception paths:</b> IllegalStateException on duplicate switch,
 *       NullPointerException on null list/option</li>
 * </ul>
 */
public class WriteableCommandLineDeepseekTest {

    // ==================== Helper: create a minimal CommandLine instance ====================
    private WriteableCommandLine createCommandLine() {
        List<Option> emptyOptions = new ArrayList<Option>();
        Option root = new GroupImpl(emptyOptions, "root", "root", 0, 0);
        List arguments = new ArrayList();
        return new WriteableCommandLineImpl(root, arguments);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddAndGetUndefaultedValues() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("a", true); // boolean switch option
        wcl.addOption(opt);

        // Initially no undefaulted values
        List undefaulted = wcl.getUndefaultedValues(opt);
        assertNotNull(undefaulted);
        assertTrue(undefaulted.isEmpty());

        // Add a value
        wcl.addValue(opt, "value1");
        undefaulted = wcl.getUndefaultedValues(opt);
        assertEquals(1, undefaulted.size());
        assertEquals("value1", undefaulted.get(0));

        // Add another value
        wcl.addValue(opt, "value2");
        undefaulted = wcl.getUndefaultedValues(opt);
        assertEquals(2, undefaulted.size());
        assertTrue(undefaulted.contains("value1"));
        assertTrue(undefaulted.contains("value2"));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValues() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("b");
        wcl.addOption(opt);

        List defaultVals = new ArrayList();
        defaultVals.add("default1");
        defaultVals.add("default2");
        wcl.setDefaultValues(opt, defaultVals);

        // Undefaulted values should still be empty because we haven't added actual values
        List undefaulted = wcl.getUndefaultedValues(opt);
        assertTrue(undefaulted.isEmpty());

        // Add a real value and confirm separation
        wcl.addValue(opt, "actual");
        undefaulted = wcl.getUndefaultedValues(opt);
        assertEquals(1, undefaulted.size());
        assertEquals("actual", undefaulted.get(0));
    }

    @Test(timeout = 4000)
    public void testAddSwitchAndSetDefaultSwitch() {
        WriteableCommandLine wcl = createCommandLine();
        Option sw = DefaultOption.create("verbose");
        wcl.addOption(sw);

        // Initially no switch value; default switch is null
        wcl.addSwitch(sw, true);
        // No getter for switch value, but we can verify no exception

        // Set default switch
        wcl.setDefaultSwitch(sw, Boolean.FALSE);
        // Setting default switch should not throw
    }

    @Test(timeout = 4000)
    public void testAddProperty() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("propOpt");
        wcl.addOption(opt);

        // Add property with option
        wcl.addProperty(opt, "key1", "value1");
        // Add property without option (default property set)
        wcl.addProperty("key2", "value2");
        // Properties are stored, but there is no getter in the interface.
        // The test simply verifies no exception is thrown.
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddValueWithNullOption() {
        WriteableCommandLine wcl = createCommandLine();
        wcl.addValue(null, "value");
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSetDefaultValuesWithNullList() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("d");
        wcl.addOption(opt);
        wcl.setDefaultValues(opt, null);
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValuesForUnknownOption() {
        WriteableCommandLine wcl = createCommandLine();
        Option unknown = DefaultOption.create("unknown");
        // Option not added to command line
        List undefaulted = wcl.getUndefaultedValues(unknown);
        assertNotNull(undefaulted);
        assertTrue(undefaulted.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddSwitchWithDuplicate() {
        WriteableCommandLine wcl = createCommandLine();
        Option sw = DefaultOption.create("duplicateSwitch");
        wcl.addOption(sw);
        wcl.addSwitch(sw, true);
        try {
            wcl.addSwitch(sw, false);
            fail("Expected IllegalStateException for duplicate switch");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddSwitchWithNullOption() {
        WriteableCommandLine wcl = createCommandLine();
        try {
            wcl.addSwitch(null, true);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (BugCLI150) ====================

    @Test(timeout = 4000)
    public void testLooksLikeOptionNegativeNumber() {
        WriteableCommandLine wcl = createCommandLine();
        // The defect: "-42" should NOT look like an option trigger
        assertFalse("Negative number should not be considered an option",
                wcl.looksLikeOption("-42"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionValidOption() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("a");
        wcl.addOption(opt);
        // "-a" should look like an option trigger (matches known option)
        assertTrue("Known option trigger should be recognized",
                wcl.looksLikeOption("-a"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionDoubleDash() {
        WriteableCommandLine wcl = createCommandLine();
        // "--" alone is not an option
        assertFalse("Double dash alone is not an option",
                wcl.looksLikeOption("--"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionEmptyString() {
        WriteableCommandLine wcl = createCommandLine();
        assertFalse("Empty string should not look like an option",
                wcl.looksLikeOption(""));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionNull() {
        WriteableCommandLine wcl = createCommandLine();
        assertFalse("Null should not look like an option",
                wcl.looksLikeOption(null));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddPropertyWithNullOption() {
        WriteableCommandLine wcl = createCommandLine();
        wcl.addProperty(null, "key", "value");
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddPropertyWithNullPropertyName() {
        WriteableCommandLine wcl = createCommandLine();
        wcl.addProperty((String) null, "value");
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitchWithNullOption() {
        WriteableCommandLine wcl = createCommandLine();
        try {
            wcl.setDefaultSwitch(null, Boolean.TRUE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testAddOptionMultipleTimes() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("multi");
        wcl.addOption(opt);
        // Adding the same option again should be idempotent (or no exception)
        wcl.addOption(opt); // no exception expected
    }

    @Test(timeout = 4000)
    public void testAddValueAfterDefaultValues() {
        WriteableCommandLine wcl = createCommandLine();
        Option opt = DefaultOption.create("valAfterDef");
        wcl.addOption(opt);
        List defaults = new ArrayList();
        defaults.add("def");
        wcl.setDefaultValues(opt, defaults);
        wcl.addValue(opt, "real");
        List undefaulted = wcl.getUndefaultedValues(opt);
        assertEquals(1, undefaulted.size());
        assertEquals("real", undefaulted.get(0));
    }

    @Test(timeout = 4000)
    public void testLookLikeOptionWithNumericArgument() {
        WriteableCommandLine wcl = createCommandLine();
        // Additional numeric-like strings that should not be options
        assertFalse(wcl.looksLikeOption("+42"));
        assertFalse(wcl.looksLikeOption("-0"));
        assertFalse(wcl.looksLikeOption("--10"));
        // A valid option with number inside? e.g., "-2d" - depends on prefix detection
        // but we assume it's not a trigger if not a known option
        // Just ensure no crash
        wcl.looksLikeOption("-2d");
    }
}