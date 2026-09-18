package org.apache.commons.cli2;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Interface/Class: org.apache.commons.cli2.WriteableCommandLine
 * Concrete Under Test:   org.apache.commons.cli2.commandline.WriteableCommandLineImpl
 * Defect Reference:      CLI-150 (BugCLI150Test::testNegativeNumber)
 * Failure Symptom:       OptionException: Unexpected -42 while processing --num
 *
 * Decision / Branch Matrix Covered:
 * 1. looksLikeOption(String argument):
 *    - argument matching registered prefix -> true (e.g. "-a", "--option")
 *    - argument without registered prefix -> false (e.g. "param")
 *    - argument == null -> throws NullPointerException
 *    - empty prefix list -> always false
 *    - exact prefix without suffix -> true ("-", "--")
 *    - negative numbers ("-42", "-3.14") -> improperly flagged as option trigger in CLI-150
 * 2. addOption(Option) / hasOption(Option / String):
 *    - unadded option -> false
 *    - added option -> true
 *    - trigger lookup for short and long option aliases -> true
 *    - null parameter handling -> defensive checks
 * 3. addValue(Option, Object) / getValues(Option) / getUndefaultedValues(Option):
 *    - single value added -> returned via getValue and getValues
 *    - multiple values added -> list preservation in insertion order
 *    - undefaulted values tracking -> returns ONLY explicitly added values
 * 4. setDefaultValues(Option, List):
 *    - defaults configured, no explicit value added -> defaults returned
 *    - defaults configured, undefaulted values requested -> empty list returned
 *    - defaults configured, explicit value added -> explicit value overrides defaults
 * 5. addSwitch(Option, boolean) / setDefaultSwitch(Option, Boolean):
 *    - initial addSwitch (true/false) -> successful state transition
 *    - subsequent addSwitch on same option -> throws IllegalStateException
 *    - default switch set, no explicit switch -> default returned
 *    - default switch set, explicit switch added -> explicit overrides default
 * 6. addProperty(Option, String, String) / addProperty(String, String):
 *    - property added to specific option -> retrievable via getProperty(opt, key)
 *    - property overwriting -> latest value replaces previous
 *    - default property set (no option) -> retrievable via getProperty(key)
 * 7. Defect CLI-150 (Negative Number Arguments):
 *    - Parser processing negative numbers (-42, -1, -3.14) assigned to argument options
 * -----------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.PropertyOptionBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;

public class WriteableCommandLineGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionAndHasOptionQuery() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").withLongName("alpha").create();
        final Option optB = obuilder.withShortName("b").withLongName("beta").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        assertFalse("Unadded option should return false", cmd.hasOption(optA));
        assertFalse("Unadded trigger should return false", cmd.hasOption("-a"));
        assertFalse("Unadded trigger should return false", cmd.hasOption("--alpha"));

        cmd.addOption(optA);

        assertTrue("Added option must return true", cmd.hasOption(optA));
        assertTrue("Short trigger must return true", cmd.hasOption("-a"));
        assertTrue("Long trigger must return true", cmd.hasOption("--alpha"));
        assertFalse("Non-added option must still return false", cmd.hasOption(optB));
        assertFalse("Non-added trigger must still return false", cmd.hasOption("-b"));
    }

    @Test(timeout = 4000)
    public void testAddValueAndGetUndefaultedValues() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("f").withLongName("file").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        cmd.addOption(opt);
        cmd.addValue(opt, "file1.txt");
        cmd.addValue(opt, "file2.txt");

        assertEquals("First value should match", "file1.txt", cmd.getValue(opt));
        assertEquals("First value by trigger should match", "file1.txt", cmd.getValue("-f"));
        assertEquals("First value by long trigger should match", "file1.txt", cmd.getValue("--file"));

        final List values = cmd.getValues(opt);
        assertEquals("Should contain 2 values", 2, values.size());
        assertEquals("file1.txt", values.get(0));
        assertEquals("file2.txt", values.get(1));

        final List undefaulted = cmd.getUndefaultedValues(opt);
        assertEquals("Undefaulted values should match added values", 2, undefaulted.size());
        assertEquals("file1.txt", undefaulted.get(0));
        assertEquals("file2.txt", undefaulted.get(1));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesAndOverrides() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("o").withLongName("output").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        final List defaultVals = Arrays.asList("default1.out", "default2.out");
        cmd.setDefaultValues(opt, defaultVals);

        // Undefaulted values must remain empty
        final List undefaulted = cmd.getUndefaultedValues(opt);
        assertNotNull("Undefaulted values should not be null", undefaulted);
        assertTrue("Undefaulted values must be empty before explicit addition", undefaulted.isEmpty());

        // Regular getValues returns default values
        final List currentValues = cmd.getValues(opt);
        assertEquals("Defaults must be returned when no values are added", 2, currentValues.size());
        assertEquals("default1.out", cmd.getValue(opt));

        // Now explicitly add a value -> defaults must be overridden
        cmd.addValue(opt, "custom.out");
        final List afterExplicit = cmd.getValues(opt);
        assertEquals("Only explicit values should be returned after addValue", 1, afterExplicit.size());
        assertEquals("custom.out", cmd.getValue(opt));

        final List undefAfterExplicit = cmd.getUndefaultedValues(opt);
        assertEquals("Undefaulted list must now reflect explicit value", 1, undefAfterExplicit.size());
        assertEquals("custom.out", undefAfterExplicit.get(0));
    }

    @Test(timeout = 4000)
    public void testAddSwitchAndDefaultSwitch() {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option switchOpt = sbuilder.withShortName("s").withLongName("secure").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        cmd.setDefaultSwitch(switchOpt, Boolean.FALSE);
        assertEquals("Default switch should be FALSE", Boolean.FALSE, cmd.getSwitch(switchOpt));

        cmd.addSwitch(switchOpt, true);
        assertEquals("Explicit switch should override default", Boolean.TRUE, cmd.getSwitch(switchOpt));
    }

    @Test(timeout = 4000)
    public void testAddPropertyForOptionAndDefaultPropertySet() {
        final PropertyOptionBuilder pobuilder = new PropertyOptionBuilder();
        final Option propOpt = pobuilder.create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        // Option-scoped property
        cmd.addProperty(propOpt, "env", "production");
        assertEquals("production", cmd.getProperty(propOpt, "env"));
        assertNull("Non-existing property must return null", cmd.getProperty(propOpt, "nonexistent"));

        // Overwrite option-scoped property
        cmd.addProperty(propOpt, "env", "staging");
        assertEquals("staging", cmd.getProperty(propOpt, "env"));

        // Global / default property set
        cmd.addProperty("version", "1.0.0");
        assertEquals("1.0.0", cmd.getProperty("version"));
        assertNull("Non-existing global property must return null", cmd.getProperty("missing"));

        // Overwrite global property
        cmd.addProperty("version", "2.0.0");
        assertEquals("2.0.0", cmd.getProperty("version"));

        final Set globalProperties = cmd.getProperties();
        assertNotNull("Global properties set must not be null", globalProperties);
        assertTrue("Global properties must contain 'version'", globalProperties.contains("version"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionLogic() {
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--", "+"));

        assertTrue("Single dash prefix", cmd.looksLikeOption("-v"));
        assertTrue("Double dash prefix", cmd.looksLikeOption("--verbose"));
        assertTrue("Plus prefix", cmd.looksLikeOption("+opt"));
        assertFalse("Plain word does not look like option", cmd.looksLikeOption("verbose"));
        assertFalse("Empty string does not look like option", cmd.looksLikeOption(""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLooksLikeOptionExactPrefixes() {
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        assertTrue("Single dash itself starts with prefix", cmd.looksLikeOption("-"));
        assertTrue("Double dash itself starts with prefix", cmd.looksLikeOption("--"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithEmptyPrefixList() {
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Collections.emptyList());

        assertFalse("-v should not match empty prefix list", cmd.looksLikeOption("-v"));
        assertFalse("--option should not match empty prefix list", cmd.looksLikeOption("--option"));
        assertFalse("word should not match empty prefix list", cmd.looksLikeOption("word"));
    }

    @Test(timeout = 4000)
    public void testGetUndefaultedValuesForUnregisteredOptionReturnsEmptyList() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option unaddedOpt = obuilder.withShortName("u").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        final List undefaulted = cmd.getUndefaultedValues(unaddedOpt);

        assertNotNull("Undefaulted values should never be null", undefaulted);
        assertTrue("Undefaulted values for unadded option should be empty", undefaulted.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetValuesAndGetSwitchWithFallbacks() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("x").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        assertEquals("Fallback value must be returned", "fallback", cmd.getValue(opt, "fallback"));
        assertEquals("Fallback value by trigger", "fallbackTrigger", cmd.getValue("-x", "fallbackTrigger"));

        final List fallbackList = Arrays.asList("f1", "f2");
        assertEquals("Fallback list must be returned", fallbackList, cmd.getValues(opt, fallbackList));

        assertEquals("Fallback switch must be returned", Boolean.TRUE, cmd.getSwitch(opt, Boolean.TRUE));
        assertEquals("Fallback switch by trigger", Boolean.FALSE, cmd.getSwitch("-x", Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testAddValueWithNonStringObjects() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("i").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        final Integer intVal = 12345;
        cmd.addValue(opt, intVal);

        assertSame("Added object reference should be preserved", intVal, cmd.getValue(opt));
        assertEquals(intVal, cmd.getUndefaultedValues(opt).get(0));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug CLI-150)
    // =========================================================================

    /**
     * Target: org.apache.commons.cli2.bug.BugCLI150Test::testNegativeNumber
     * Triggering OptionException: Unexpected -42 while processing --num
     * This test ensures that negative integers are accepted as option values rather
     * than erroneously rejected as unexpected option triggers.
     */
    @Test(timeout = 4000)
    public void testNegativeNumberCli150() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Argument numArg = abuilder.withName("num").withMinimum(1).withMaximum(1).create();
        final Option numOpt = obuilder.withLongName("num").withArgument(numArg).create();
        final Group options = gbuilder.withOption(numOpt).create();

        final Parser parser = new Parser();
        parser.setGroup(options);
        final CommandLine cl = parser.parse(new String[]{"--num", "-42"});

        assertTrue("CommandLine should contain --num option", cl.hasOption(numOpt));
        assertEquals("-42", cl.getValue(numOpt));
    }

    /**
     * Target: CLI-150 variation with multiple negative arguments.
     */
    @Test(timeout = 4000)
    public void testNegativeNumberMultipleCli150() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Argument numArg = abuilder.withName("num").withMinimum(1).withMaximum(2).create();
        final Option numOpt = obuilder.withShortName("n").withLongName("numbers").withArgument(numArg).create();
        final Group options = gbuilder.withOption(numOpt).create();

        final Parser parser = new Parser();
        parser.setGroup(options);
        final CommandLine cl = parser.parse(new String[]{"-n", "-1", "-2"});

        assertTrue(cl.hasOption(numOpt));
        assertEquals("-1", cl.getValue(numOpt));
        final List values = cl.getValues(numOpt);
        assertEquals(2, values.size());
        assertEquals("-1", values.get(0));
        assertEquals("-2", values.get(1));
    }

    /**
     * Target: CLI-150 variation with negative decimal argument.
     */
    @Test(timeout = 4000)
    public void testNegativeDecimalArgumentCli150() throws OptionException {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Argument numArg = abuilder.withName("val").withMinimum(1).withMaximum(1).create();
        final Option numOpt = obuilder.withLongName("val").withArgument(numArg).create();
        final Group options = gbuilder.withOption(numOpt).create();

        final Parser parser = new Parser();
        parser.setGroup(options);
        final CommandLine cl = parser.parse(new String[]{"--val", "-3.14159"});

        assertTrue(cl.hasOption(numOpt));
        assertEquals("-3.14159", cl.getValue(numOpt));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddSwitchDuplicateThrowsIllegalStateException() {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option switchOpt = sbuilder.withShortName("s").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        cmd.addSwitch(switchOpt, true);
        // Second invocation MUST throw IllegalStateException
        cmd.addSwitch(switchOpt, false);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddSwitchDuplicateSameValueThrowsIllegalStateException() {
        final SwitchBuilder sbuilder = new SwitchBuilder();
        final Option switchOpt = sbuilder.withShortName("d").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        cmd.addSwitch(switchOpt, true);
        // Second invocation with same value MUST also throw IllegalStateException
        cmd.addSwitch(switchOpt, true);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testLooksLikeOptionNullThrowsNullPointerException() {
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        cmd.looksLikeOption(null);
    }

    @Test(timeout = 4000)
    public void testDefensiveNullQueries() {
        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));

        assertFalse("Null option check should return false", cmd.hasOption((Option) null));
        assertFalse("Null trigger check should return false", cmd.hasOption((String) null));
        assertTrue("getValues for null option should be empty", cmd.getValues((Option) null).isEmpty());
        assertTrue("getValues for null trigger should be empty", cmd.getValues((String) null).isEmpty());
        assertTrue("getUndefaultedValues for null option should be empty", cmd.getUndefaultedValues(null).isEmpty());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptionRegistryAndOptionNames() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optAlpha = obuilder.withShortName("a").withLongName("alpha").create();
        final Option optBeta = obuilder.withShortName("b").withLongName("beta").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        cmd.addOption(optAlpha);
        cmd.addOption(optBeta);

        final Set options = cmd.getOptions();
        assertEquals(2, options.size());
        assertTrue(options.contains(optAlpha));
        assertTrue(options.contains(optBeta));

        final Set names = cmd.getOptionNames();
        assertTrue("Should contain -a", names.contains("-a"));
        assertTrue("Should contain --alpha", names.contains("--alpha"));
        assertTrue("Should contain -b", names.contains("-b"));
        assertTrue("Should contain --beta", names.contains("--beta"));

        assertSame(optAlpha, cmd.getOption("-a"));
        assertSame(optAlpha, cmd.getOption("--alpha"));
        assertSame(optBeta, cmd.getOption("-b"));
        assertSame(optBeta, cmd.getOption("--beta"));
        assertNull("Unknown trigger must resolve to null option", cmd.getOption("--unknown"));
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("t").withLongName("target").create();

        final WriteableCommandLine cmd = new WriteableCommandLineImpl(null, Arrays.asList("-", "--"));
        cmd.addOption(opt);
        cmd.addValue(opt, "build-artifact");

        final String stringRepr = cmd.toString();
        assertNotNull("toString representation should not be null", stringRepr);
        assertTrue("toString should contain trigger or value",
                stringRepr.contains("target") || stringRepr.contains("build-artifact") || stringRepr.contains("-t"));
    }
}