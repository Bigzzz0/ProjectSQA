package org.apache.commons.cli2.commandline;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.cli2.commandline.WriteableCommandLineImpl
 * Benchmark: Defects4J (Bug CLI-158)
 *
 * Branch & State Coverage Strategy:
 * 1. Constructor:
 *    - rootOption.getPrefixes(), normalised arguments initialization.
 * 2. addOption(Option):
 *    - Populates options list, preferredName, and all trigger aliases.
 * 3. addValue(Option, Object):
 *    - Branch: option instanceof Argument (invokes addOption) vs non-Argument (does not).
 *    - Branch: valueList == null (new list creation) vs valueList != null (append to existing).
 * 4. addSwitch(Option, boolean):
 *    - Branch: switches.containsKey(option) -> throws IllegalStateException.
 *    - Branch: !switches.containsKey(option) -> stores Boolean.TRUE or Boolean.FALSE.
 * 5. hasOption(Option):
 *    - options.contains(option) -> true / false.
 * 6. getOption(String):
 *    - trigger lookup in nameToOption map -> hit / miss.
 * 7. getValues(Option, List):
 *    - Branch: valueList == null || valueList.isEmpty() -> fallback to defaultValues parameter.
 *    - Branch: valueList == null || valueList.isEmpty() -> fallback to this.defaultValues map.
 *    - Branch: valueList == null -> Collections.EMPTY_LIST.
 *    - DEFECT CLI-158 BRANCH: Augmenting existing values when this.defaultValues has more
 *      entries than user-supplied values.
 * 8. getUndefaultedValues(Option):
 *    - Branch: valueList == null -> Collections.EMPTY_LIST vs non-null list.
 * 9. getSwitch(Option, Boolean):
 *    - Branch: switch in switches map vs defaultValue parameter vs defaultSwitches map vs null.
 * 10. Properties Management:
 *    - Option-mapped properties and global/default PropertyOption properties.
 *    - Missing property fallback to defaultValue vs existing property value.
 *    - Null properties map branch -> Collections.EMPTY_SET.
 * 11. looksLikeOption(String):
 *    - Loop prefixes -> trigger.startsWith(prefix) == true vs false.
 * 12. toString():
 *    - Arguments with spaces (quote wrapped) vs arguments without spaces.
 * 13. Immutability Guards:
 *    - getOptions(), getOptionTriggers(), getProperties(), getNormalised() unmodifiable checks.
 * -----------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.option.PropertyOption;

public class WriteableCommandLineImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionAndTriggersLookup() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("f")
                                   .withLongName("file")
                                   .withDescription("A file option")
                                   .create();

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(opt, Collections.emptyList());
        assertFalse("Option should not be registered initially", cmdLine.hasOption(opt));

        cmdLine.addOption(opt);
        assertTrue("Option should be registered after addOption", cmdLine.hasOption(opt));
        assertSame("Lookup by preferred name should return opt", opt, cmdLine.getOption("file"));
        assertSame("Lookup by short trigger should return opt", opt, cmdLine.getOption("-f"));
        assertSame("Lookup by long trigger should return opt", opt, cmdLine.getOption("--file"));
        assertNull("Lookup by unregistered trigger should return null", cmdLine.getOption("--unknown"));
    }

    @Test(timeout = 4000)
    public void testAddValueForStandardOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("v").withLongName("val").create();
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(opt, Collections.emptyList());

        // For non-Argument options, addValue should NOT auto-register the option in options list
        cmdLine.addValue(opt, "value1");
        assertFalse("Non-Argument option should not be registered via addValue", cmdLine.hasOption(opt));

        cmdLine.addValue(opt, "value2");
        final List values = cmdLine.getValues(opt, null);
        assertNotNull("Values list should not be null", values);
        assertEquals("Should contain 2 values", 2, values.size());
        assertEquals("value1", values.get(0));
        assertEquals("value2", values.get(1));

        final List undefaulted = cmdLine.getUndefaultedValues(opt);
        assertEquals("Undefaulted values should match specified values", 2, undefaulted.size());
        assertEquals("value1", undefaulted.get(0));
    }

    @Test(timeout = 4000)
    public void testAddValueForArgumentOptionTriggersAddOption() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument argOption = abuilder.withName("argTarget").create();
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(argOption, Collections.emptyList());

        assertFalse("Argument should not be registered initially", cmdLine.hasOption(argOption));
        // Argument instance triggers addOption inside addValue
        cmdLine.addValue(argOption, "capturedValue");

        assertTrue("Argument option MUST be registered when addValue is invoked", cmdLine.hasOption(argOption));
        final List values = cmdLine.getValues(argOption, null);
        assertEquals(1, values.size());
        assertEquals("capturedValue", values.get(0));
    }

    @Test(timeout = 4000)
    public void testSwitchHandling() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optTrue = obuilder.withShortName("t").withLongName("trueSwitch").create();
        final Option optFalse = obuilder.withShortName("f").withLongName("falseSwitch").create();

        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(optTrue, Collections.emptyList());

        cmdLine.addSwitch(optTrue, true);
        cmdLine.addSwitch(optFalse, false);

        assertTrue("Switch option should be registered", cmdLine.hasOption(optTrue));
        assertTrue("Switch option should be registered", cmdLine.hasOption(optFalse));
        assertEquals("Switch value should be Boolean.TRUE", Boolean.TRUE, cmdLine.getSwitch(optTrue, null));
        assertEquals("Switch value should be Boolean.FALSE", Boolean.FALSE, cmdLine.getSwitch(optFalse, null));
    }

    @Test(timeout = 4000)
    public void testSwitchFallbackHierarchy() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("s").create();
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(opt, Collections.emptyList());

        // Level 4: nothing set anywhere -> null
        assertNull("Switch should be null when unset", cmdLine.getSwitch(opt, null));

        // Level 3: option default switch set
        cmdLine.setDefaultSwitch(opt, Boolean.FALSE);
        assertEquals("Should fallback to option default switch", Boolean.FALSE, cmdLine.getSwitch(opt, null));

        // Level 2: method argument default provided -> overrides option default switch
        assertEquals("Method default should override option default switch",
                     Boolean.TRUE, cmdLine.getSwitch(opt, Boolean.TRUE));

        // Level 1: explicit switch set on command line -> overrides everything
        cmdLine.addSwitch(opt, true);
        assertEquals("Explicit switch should override all defaults",
                     Boolean.TRUE, cmdLine.getSwitch(opt, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testPropertyHandlingWithOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("D").create();
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(opt, Collections.emptyList());

        assertEquals("Should return default value when property does not exist",
                     "defaultVal", cmdLine.getProperty(opt, "env", "defaultVal"));
        assertTrue("Properties set should be empty initially", cmdLine.getProperties(opt).isEmpty());

        cmdLine.addProperty(opt, "env", "production");
        cmdLine.addProperty(opt, "threads", "8");

        assertEquals("production", cmdLine.getProperty(opt, "env", "defaultVal"));
        assertEquals("8", cmdLine.getProperty(opt, "threads", "1"));
        assertEquals("defaultVal", cmdLine.getProperty(opt, "nonExisting", "defaultVal"));

        final Set keys = cmdLine.getProperties(opt);
        assertEquals(2, keys.size());
        assertTrue(keys.contains("env"));
        assertTrue(keys.contains("threads"));
    }

    @Test(timeout = 4000)
    public void testDefaultPropertyOptionHandling() {
        final PropertyOption propOption = new PropertyOption();
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(propOption, Collections.emptyList());

        assertNull("Non-existing property should return null", cmdLine.getProperty("database.url"));
        assertTrue("Default properties should be empty", cmdLine.getProperties().isEmpty());

        cmdLine.addProperty("database.url", "jdbc:postgresql://localhost/test");
        assertEquals("jdbc:postgresql://localhost/test", cmdLine.getProperty("database.url"));

        final Set propKeys = cmdLine.getProperties();
        assertEquals(1, propKeys.size());
        assertTrue(propKeys.contains("database.url"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionLogic() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option root = obuilder.withShortName("h").withLongName("help").create();

        // root Option has prefixes "-" and "--"
        final WriteableCommandLineImpl cmdLine = new WriteableCommandLineImpl(root, Collections.emptyList());

        assertTrue("Should recognize '-' prefix", cmdLine.looksLikeOption("-a"));
        assertTrue("Should recognize '--' prefix", cmdLine.looksLikeOption("--option"));
        assertFalse("Should not recognize argument without prefix", cmdLine.looksLikeOption("regularArg"));
        assertFalse("Empty string without prefix should return false", cmdLine.looksLikeOption(""));
    }

    @Test(timeout = 4000)
    public void testToStringNormalisedFormatting() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option root = obuilder.withShortName("h").create();

        final List args = new ArrayList();
        args.add("--file");
        args.add("my document.txt");
        args.add("simple");

        final WriteableCommandLine