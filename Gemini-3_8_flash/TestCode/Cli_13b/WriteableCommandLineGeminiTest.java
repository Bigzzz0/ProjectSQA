package org.apache.commons.cli2;

/* [Branch & Defect Analysis Matrix]
 * Target Interface & Implementation: org.apache.commons.cli2.WriteableCommandLine / WriteableCommandLineImpl
 *
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - addOption(Option): Registering options into the command line; verifying hasOption(Option) and hasOption(String).
 *    - addValue(Option, Object): Adding single and multiple values to an option; verifying getValues(Option), getValue(Option).
 *    - addSwitch(Option, boolean): Adding switch state (true/false) to an option; verifying getSwitch(Option).
 *    - addProperty(String, String): Adding and replacing properties; verifying getProperty(String), getProperties().
 *    - setDefaultValues(Option, List): Setting default values when no explicit argument values are provided.
 *    - setDefaultSwitch(Option, Boolean): Setting default switch boolean states when no switch was explicitly parsed.
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - looksLikeOption(String): Checking empty strings, prefix matches ("-", "--", "+"), and non-prefix arguments.
 *    - Empty/Null Arguments: Adding null values, empty lists as defaults, single vs multiple values, empty property keys/values.
 *    - Undefined Options/Properties: Requesting non-existent options, properties, switches; asserting null / empty lists / default falls through.
 *
 * 3. Partition C: Defect-Targeted Branch Zone (CLI / Defects4J BugLoopingOptionLookAlikeTest)
 *    - Defect ground truth: BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2 where an unexpected argument
 *      causes a loop or mismatched OptionException message during parsing via WriteableCommandLine.
 *    - Validates expected OptionException message contract: "Unexpected [argument] while processing [trigger]".
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - addSwitch(Option, boolean): Throws IllegalStateException if a switch has already been explicitly added for that Option.
 *
 * 5. Partition E: Contract Integrity & Polymorphism
 *    - Interface polymorphic compliance through WriteableCommandLineImpl.
 */

import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class WriteableCommandLineGeminiTest {

    private DefaultOption testOption;
    private DefaultOption switchOption;
    private PropertyOption propertyOption;
    private List prefixes;
    private WriteableCommandLine commandLine;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        testOption = obuilder
                .withLongName("test")
                .withShortName("t")
                .withArgument(abuilder.withName("arg").create())
                .create();

        switchOption = obuilder
                .withLongName("switch")
                .withShortName("s")
                .create();

        propertyOption = new PropertyOption();

        prefixes = Arrays.asList("-", "--", "+");
        commandLine = new WriteableCommandLineImpl(testOption, prefixes);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionAndHasOption() {
        assertFalse(commandLine.hasOption(testOption));
        assertFalse(commandLine.hasOption("-t"));
        assertFalse(commandLine.hasOption("--test"));

        commandLine.addOption(testOption);

        assertTrue(commandLine.hasOption(testOption));
        assertTrue(commandLine.hasOption("-t"));
        assertTrue(commandLine.hasOption("--test"));
    }

    @Test(timeout = 4000)
    public void testAddValueAndRetrieval() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, "value1");
        commandLine.addValue(testOption, "value2");

        final List values = commandLine.getValues(testOption);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));
        assertEquals("value1", commandLine.getValue(testOption));
    }

    @Test(timeout = 4000)
    public void testAddSwitchAndGetSwitch() {
        assertNull(commandLine.getSwitch(switchOption));

        commandLine.addOption(switchOption);
        commandLine.addSwitch(switchOption, true);

        assertEquals(Boolean.TRUE, commandLine.getSwitch(switchOption));
        assertTrue(commandLine.getSwitch(switchOption).booleanValue());
    }

    @Test(timeout = 4000)
    public void testAddPropertyAndGetProperty() {
        assertNull(commandLine.getProperty("db.host"));

        commandLine.addProperty("db.host", "localhost");
        assertEquals("localhost", commandLine.getProperty("db.host"));

        // Replacing existing property
        commandLine.addProperty("db.host", "127.0.0.1");
        assertEquals("127.0.0.1", commandLine.getProperty("db.host"));

        final Set properties = commandLine.getProperties();
        assertNotNull(properties);
        assertTrue(properties.contains("db.host"));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValues() {
        final List defaults = Arrays.asList("defaultVal1", "defaultVal2");
        commandLine.setDefaultValues(testOption, defaults);

        // When no explicit values added, defaults are returned by getValues/getValue
        final List returnedValues = commandLine.getValues(testOption);
        assertEquals(defaults, returnedValues);
        assertEquals("defaultVal1", commandLine.getValue(testOption));

        // Adding an explicit value overrides defaults
        commandLine.addValue(testOption, "explicitVal");
        final List explicitList = commandLine.getValues(testOption);
        assertEquals(1, explicitList.size());
        assertEquals("explicitVal", explicitList.get(0));
    }

    @Test(timeout = 4000)
    public void testSetDefaultSwitch() {
        assertNull(commandLine.getSwitch(switchOption));

        commandLine.setDefaultSwitch(switchOption, Boolean.TRUE);
        assertEquals(Boolean.TRUE, commandLine.getSwitch(switchOption));

        // When explicitly added, explicit switch is retained
        commandLine.addSwitch(switchOption, false);
        assertEquals(Boolean.FALSE, commandLine.getSwitch(switchOption));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLooksLikeOptionBoundaries() {
        assertTrue(commandLine.looksLikeOption("-"));
        assertTrue(commandLine.looksLikeOption("--"));
        assertTrue(commandLine.looksLikeOption("-opt"));
        assertTrue(commandLine.looksLikeOption("--long-opt"));
        assertTrue(commandLine.looksLikeOption("+opt"));

        assertFalse(commandLine.looksLikeOption("value"));
        assertFalse(commandLine.looksLikeOption(""));
        assertFalse(commandLine.looksLikeOption("/help"));
    }

    @Test(timeout = 4000)
    public void testGetValuesForNonExistentOption() {
        final List values = commandLine.getValues(switchOption);
        assertNotNull(values);
        assertTrue(values.isEmpty());
        assertNull(commandLine.getValue(switchOption));
    }

    @Test(timeout = 4000)
    public void testSetDefaultValuesNullOrEmpty() {
        commandLine.setDefaultValues(testOption, null);
        assertTrue(commandLine.getValues(testOption).isEmpty());

        commandLine.setDefaultValues(testOption, Collections.emptyList());
        assertTrue(commandLine.getValues(testOption).isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddNullValue() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, null);

        final List values = commandLine.getValues(testOption);
        assertEquals(1, values.size());
        assertNull(values.get(0));
        assertNull(commandLine.getValue(testOption));
    }

    @Test(timeout = 4000)
    public void testUndelimitedValuesEmpty() {
        final List undelimited = commandLine.getUndelimitedValues(testOption);
        assertNotNull(undelimited);
        assertTrue(undelimited.isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth: Defects4J CLI bug)
    // =========================================================================

    /**
     * Targets BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2 defect.
     * When an unexpected argument is encountered without triggering options,
     * the error message must strictly follow: "Unexpected [argument] while processing "
     */
    @Test(timeout = 4000)
    public void testDefectLoopingOptionLookAlike2() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option o1 = obuilder.withShortName("o1").create();
        final Option input = gbuilder
                .withName("input")
                .withOption(o1)
                .create();

        final Parser parser = new Parser();
        parser.setGroup(input);

        try {
            parser.parse(new String[]{"testfile.txt"});
            fail("OptionException expected for unexpected argument");
        } catch (final OptionException e) {
            assertEquals("Unexpected testfile.txt while processing ", e.getMessage());
        }
    }

    /**
     * Additional verification for unexpected token with looksLikeOption.
     */
    @Test(timeout = 4000)
    public void testDefectLoopingOptionLookAlikeWithPrefixOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option o1 = obuilder.withShortName("o1").create();
        final Option input = gbuilder
                .withName("input")
                .withOption(o1)
                .create();

        final Parser parser = new Parser();
        parser.setGroup(input);

        try {
            parser.parse(new String[]{"-unknown"});
            fail("OptionException expected for unknown option");
        } catch (final OptionException e) {
            assertTrue(e.getMessage().startsWith("Unexpected -unknown while processing "));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddSwitchDuplicateThrowsException() {
        commandLine.addSwitch(switchOption, true);
        // Attempting to add switch twice must throw IllegalStateException
        commandLine.addSwitch(switchOption, false);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteableCommandLineInterfacePolymorphism() {
        assertTrue(commandLine instanceof WriteableCommandLine);
        assertTrue(commandLine instanceof CommandLine);

        final WriteableCommandLine wcl = commandLine;
        wcl.addProperty("key1", "val1");
        assertEquals("val1", wcl.getProperty("key1"));

        final CommandLine cl = commandLine;
        assertEquals("val1", cl.getProperty("key1"));
    }

    @Test(timeout = 4000)
    public void testCommandLineToStringRepresentation() {
        commandLine.addOption(testOption);
        commandLine.addValue(testOption, "sampleValue");

        final String stringRepr = commandLine.toString();
        assertNotNull(stringRepr);
        assertTrue(stringRepr.contains("sampleValue") || stringRepr.contains("--test") || stringRepr.contains("-t"));
    }
}