/* [Branch & Defect Analysis Matrix]
 * ================================================================================================
 * Target Class: org.apache.commons.cli2.commandline.WriteableCommandLineImpl
 *
 * Decision / Condition Branch Analysis:
 * 1. Constructor:
 *    - Captures rootOption.getPrefixes() and arguments (normalised).
 * 2. addOption(Option):
 *    - Appends to options list, maps preferredName to option, iterates over triggers and maps each.
 * 3. addValue(Option, Object):
 *    - Branch: option instanceof Argument -> triggers addOption(option).
 *    - Branch: option not an Argument -> does not addOption.
 *    - Branch: values.get(option) == null (first value) vs != null (append subsequent values).
 * 4. addSwitch(Option, boolean):
 *    - Always calls addOption(option).
 *    - Branch: switches.containsKey(option) -> throws IllegalStateException (SWITCH_ALREADY_SET).
 *    - Branch: !switches.containsKey(option) -> puts Boolean.TRUE or Boolean.FALSE.
 * 5. hasOption(Option):
 *    - Branch: options.contains(option) -> true/false.
 * 6. getOption(String):
 *    - Branch: trigger in nameToOption -> returns Option.
 *    - Branch: trigger not in nameToOption / null -> returns null.
 * 7. getValues(Option, List):
 *    - Value resolution fallback hierarchy:
 *      (1) CommandLine values present & non-empty -> return values.
 *      (2) CommandLine values null/empty & method defaultValues != null & non-empty -> return method defaultValues.
 *      (3) Method defaultValues null/empty & option defaultValues != null -> return option defaultValues.
 *      (4) All fallbacks exhausted/null -> return Collections.EMPTY_LIST.
 * 8. getSwitch(Option, Boolean):
 *    - Switch resolution fallback hierarchy:
 *      (1) CommandLine switch != null -> return switch.
 *      (2) Method defaultValue != null -> return method defaultValue.
 *      (3) Option defaultSwitch != null -> return option defaultSwitch.
 *      (4) All fallbacks exhausted -> return null.
 * 9. addProperty / getProperty / getProperties:
 *    - Properties mapping, default fallback, and unmodifiable view.
 * 10. looksLikeOption(String):
 *     - Loops prefixes: startsWith(prefix) -> returns true immediately; loop terminates -> false.
 * 11. toString():
 *     - Empty normalised list -> empty string.
 *     - Elements with space (' ') wrapped in quotes, elements without spaces left intact, joined by ' '.
 * 12. Mutators for defaults:
 *     - setDefaultValues(option, null) removes option; non-null stores defaults.
 *     - setDefaultSwitch(option, null) removes option; non-null stores switch.
 * 13. Immutability protections:
 *     - getOptions(), getOptionTriggers(), getProperties(), getNormalised() return unmodifiable collections.
 *
 * Defect-Targeted Branch Zone:
 * - Defects4J BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2:
 *   Validates command line token processing when arguments look alike vs unexpected trailing arguments.
 * ================================================================================================
 */
package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;

import org.junit.Test;
import static org.junit.Assert.*;

public class WriteableCommandLineImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionAndRetrieveOptionByTriggers() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option helpOption = obuilder
            .withShortName("h")
            .withLongName("help")
            .withDescription("Displays help")
            .create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(helpOption).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertFalse(cmd.hasOption(helpOption));
        assertNull(cmd.getOption("h"));
        assertNull(cmd.getOption("--help"));

        cmd.addOption(helpOption);

        assertTrue(cmd.hasOption(helpOption));
        assertSame(helpOption, cmd.getOption("h"));
        assertSame(helpOption, cmd.getOption("-h"));
        assertSame(helpOption, cmd.getOption("--help"));

        final List options = cmd.getOptions();
        assertEquals(1, options.size());
        assertTrue(options.contains(helpOption));

        final Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.contains("-h"));
        assertTrue(triggers.contains("--help"));
    }

    @Test(timeout = 4000)
    public void testAddValueForArgumentTriggersAddOption() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument fileArg = abuilder.withName("file").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(fileArg).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertFalse(cmd.hasOption(fileArg));

        // Adding value to Argument instance must automatically add option
        cmd.addValue(fileArg, "data.txt");
        assertTrue(cmd.hasOption(fileArg));

        final List values = cmd.getValues(fileArg, null);
        assertEquals(1, values.size());
        assertEquals("data.txt", values.get(0));

        // Add second value to same option
        cmd.addValue(fileArg, "extra.txt");
        final List updatedValues = cmd.getValues(fileArg, null);
        assertEquals(2, updatedValues.size());
        assertEquals("data.txt", updatedValues.get(0));
        assertEquals("extra.txt", updatedValues.get(1));
    }

    @Test(timeout = 4000)
    public void testAddValueForNonArgumentDoesNotAddOption() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("v").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // DefaultOption is NOT an instance of Argument
        cmd.addValue(opt, "val1");
        assertFalse(cmd.hasOption(opt));

        final List vals = cmd.getValues(opt, null);
        assertEquals(1, vals.size());
        assertEquals("val1", vals.get(0));
    }

    @Test(timeout = 4000)
    public void testAddSwitchStateTransitions() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option debugOpt = obuilder.withShortName("d").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(debugOpt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertNull(cmd.getSwitch(debugOpt, null));

        cmd.addSwitch(debugOpt, true);
        assertTrue(cmd.hasOption(debugOpt));
        assertEquals(Boolean.TRUE, cmd.getSwitch(debugOpt, null));
    }

    @Test(timeout = 4000)
    public void testAddSwitchFalse() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("f").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addSwitch(opt, false);

        assertTrue(cmd.hasOption(opt));
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, null));
    }

    @Test(timeout = 4000)
    public void testPropertiesHandling() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertTrue(cmd.getProperties().isEmpty());

        cmd.addProperty("timeout", "5000");
        cmd.addProperty("retries", "3");

        assertEquals("5000", cmd.getProperty("timeout", "1000"));
        assertEquals("3", cmd.getProperty("retries", "1"));
        assertEquals("defaultVal", cmd.getProperty("nonexistent", "defaultVal"));
        assertNull(cmd.getProperty("unknown", null));

        final Set props = cmd.getProperties();
        assertEquals(2, props.size());
        assertTrue(props.contains("timeout"));
        assertTrue(props.contains("retries"));
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionLogic() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("o").withLongName("opt").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // Prefixes registered on root: "-", "--"
        assertTrue(cmd.looksLikeOption("-o"));
        assertTrue(cmd.looksLikeOption("--opt"));
        assertTrue(cmd.looksLikeOption("-unknown"));
        assertFalse(cmd.looksLikeOption("plainArgument"));
        assertFalse(cmd.looksLikeOption("testfile.txt"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testValuesFallbackHierarchy() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("k").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // 1. All empty/null -> returns Collections.EMPTY_LIST
        List res = cmd.getValues(opt, null);
        assertNotNull(res);
        assertTrue(res.isEmpty());

        // 2. Option defaultValues set, method defaultValues is null -> returns option defaultValues
        final List optDefaults = Arrays.asList(new Object[]{"optDef1", "optDef2"});
        cmd.setDefaultValues(opt, optDefaults);
        res = cmd.getValues(opt, null);
        assertEquals(optDefaults, res);

        // 3. Option defaultValues set, but method defaultValues provided -> returns method defaultValues
        final List methodDefaults = Arrays.asList(new Object[]{"methDef"});
        res = cmd.getValues(opt, methodDefaults);
        assertEquals(methodDefaults, res);

        // 4. Method defaultValues is empty list -> falls back to option defaultValues
        res = cmd.getValues(opt, Collections.EMPTY_LIST);
        assertEquals(optDefaults, res);

        // 5. CommandLine value added -> takes precedence over all defaults
        cmd.addValue(opt, "actualVal");
        res = cmd.getValues(opt, methodDefaults);
        assertEquals(1, res.size());
        assertEquals("actualVal", res.get(0));

        // 6. Reset option defaultValues to null
        cmd.setDefaultValues(opt, null);
        // Still returns actual value
        res = cmd.getValues(opt, null);
        assertEquals(1, res.size());
        assertEquals("actualVal", res.get(0));
    }

    @Test(timeout = 4000)
    public void testValuesFallbackWhenOptionDefaultsEmpty() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("k").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // Option default values explicitly set to empty list
        cmd.setDefaultValues(opt, Collections.EMPTY_LIST);

        // Should fall back to empty list constant
        final List res = cmd.getValues(opt, Collections.EMPTY_LIST);
        assertTrue(res.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSwitchFallbackHierarchy() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("s").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // 1. All null -> returns null
        assertNull(cmd.getSwitch(opt, null));

        // 2. Option default switch set, method default is null -> returns option default
        cmd.setDefaultSwitch(opt, Boolean.TRUE);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, null));

        // 3. Option default switch set, method default supplied -> returns method default
        assertEquals(Boolean.FALSE, cmd.getSwitch(opt, Boolean.FALSE));

        // 4. Reset option default switch to null -> falls back to null
        cmd.setDefaultSwitch(opt, null);
        assertNull(cmd.getSwitch(opt, null));

        // 5. CommandLine switch added -> takes precedence
        cmd.addSwitch(opt, true);
        assertEquals(Boolean.TRUE, cmd.getSwitch(opt, Boolean.FALSE));
    }

    @Test(timeout = 4000)
    public void testToStringNormalisedFormatting() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        // Test 1: Empty normalised args
        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, Collections.EMPTY_LIST);
        assertEquals("", cmd.toString());
        assertTrue(cmd.getNormalised().isEmpty());

        // Test 2: Single arg without space
        cmd = new WriteableCommandLineImpl(root, Collections.singletonList("singleArg"));
        assertEquals("singleArg", cmd.toString());

        // Test 3: Multiple args with and without spaces
        final List args = Arrays.asList(new Object[]{"--file", "path with spaces/file.txt", "-v", "noSpace"});
        cmd = new WriteableCommandLineImpl(root, args);
        assertEquals("--file \"path with spaces/file.txt\" -v noSpace", cmd.toString());
        assertEquals(args, cmd.getNormalised());
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionEmptyPrefixes() {
        // Group without options has empty prefixes
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        assertFalse(cmd.looksLikeOption("-o"));
        assertFalse(cmd.looksLikeOption("--help"));
        assertFalse(cmd.looksLikeOption(""));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets BugLoopingOptionLookAlikeTest::testLoopingOptionLookAlike2 defect from Defects4J.
     * When multiple look-alike non-option arguments ("testfile.txt", "testfile.txt") are provided,
     * the parser must identify trailing unexpected tokens without looping or attributing them to input.
     */
    @Test(timeout = 4000)
    public void testLoopingOptionLookAlike2Defect() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();

        final Option o1 = obuilder.withShortName("o1").create();
        final Option input = abuilder.withName("input").create();
        final Group root = gbuilder.withOption(o1).withOption(input).create();

        final Parser parser = new Parser();
        parser.setGroup(root);

        try {
            parser.parse(new String[]{"testfile.txt", "testfile.txt"});
            fail("OptionException expected when unexpected lookalike argument is parsed");
        } catch (OptionException e) {
            assertEquals("Unexpected testfile.txt while processing ", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLooksLikeOptionWithSimilarPrefixes() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option o1 = obuilder.withShortName("o1").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(o1).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        // A file name beginning with text must not look like an option
        assertFalse(cmd.looksLikeOption("testfile.txt"));
        assertFalse(cmd.looksLikeOption("o1"));

        // Must accurately match prefixes
        assertTrue(cmd.looksLikeOption("-o1"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAddSwitchAlreadySetThrowsException() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option opt = obuilder.withShortName("x").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(opt).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addSwitch(opt, true);
        // Second call must trigger SWITCH_ALREADY_SET IllegalStateException
        cmd.addSwitch(opt, false);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetOptionsIsUnmodifiable() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.getOptions().add(root);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetOptionTriggersIsUnmodifiable() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.getOptionTriggers().add("-test");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetPropertiesIsUnmodifiable() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.getProperties().add("illegalKey");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetNormalisedIsUnmodifiable() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.getNormalised().add("illegalArg");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndNullTriggerLookupIntegrity() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        assertFalse(cmd.hasOption((Option) null));
        assertNull(cmd.getOption(null));
        assertNull(cmd.getOption(""));
        assertNull(cmd.getOption("--nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetOptionTriggersMatchesAddedOptions() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optA = obuilder.withShortName("a").withLongName("alpha").create();
        final Option optB = obuilder.withShortName("b").withLongName("beta").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(optA).withOption(optB).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());

        cmd.addOption(optA);
        cmd.addOption(optB);

        final Set triggers = cmd.getOptionTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--alpha"));
        assertTrue(triggers.contains("-b"));
        assertTrue(triggers.contains("--beta"));
        assertEquals(4, triggers.size());
    }

    @Test(timeout = 4000)
    public void testCommandLineImplInheritedGetValuesConvenienceMethod() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument fileArg = abuilder.withName("target").create();

        final GroupBuilder gbuilder = new GroupBuilder();
        final Group root = gbuilder.withOption(fileArg).create();

        final WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(root, new ArrayList());
        cmd.addValue(fileArg, "file1.txt");
        cmd.addValue(fileArg, "file2.txt");

        // getValues(Option) without defaultValues delegates to getValues(Option, null)
        final List values = cmd.getValues(fileArg);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals("file1.txt", values.get(0));
        assertEquals("file2.txt", values.get(1));
    }
}