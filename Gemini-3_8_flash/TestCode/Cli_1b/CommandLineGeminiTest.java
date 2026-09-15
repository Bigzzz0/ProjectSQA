package org.apache.commons.cli;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.cli.CommandLine
 *
 * Method Coverage & Decision Matrix:
 * 1. hasOption(String opt):
 *    - Branch: options.containsKey(opt) == true (option registered with matching short/primary key)
 *    - Branch: options.containsKey(opt) == false (unregistered option, null key, or longOpt alias)
 * 2. hasOption(char opt):
 *    - Delegates to hasOption(String.valueOf(opt)). Tests ASCII boundary characters ('a', '1', etc.).
 * 3. getOptionValues(String opt):
 *    - Branch: opt contains leading hyphens ("-d", "--debug") -> Util.stripLeadingHyphens resolves key.
 *    - Branch: names.containsKey(opt) == true -> maps alias/longOpt back to primary shortOpt key.
 *    - Branch: names.containsKey(opt) == false -> key remains unchanged.
 *    - Branch: options.containsKey(key) == true -> returns Option.getValues().
 *    - Branch: options.containsKey(key) == false -> returns null.
 * 4. getOptionValues(char opt):
 *    - Delegates to getOptionValues(String.valueOf(opt)).
 * 5. getOptionValue(String opt) & getOptionValue(char opt):
 *    - Branch: getOptionValues(opt) == null -> returns null.
 *    - Branch: getOptionValues(opt) != null -> returns values[0].
 * 6. getOptionValue(String/char opt, String defaultValue):
 *    - Branch: answer != null -> returns retrieved answer.
 *    - Branch: answer == null -> returns defaultValue fallback (including null or user default).
 * 7. getOptionObject(String opt) & getOptionObject(char opt):
 *    - Branch: !options.containsKey(opt) -> returns null immediately.
 *    - Branch: options.containsKey(opt) && res == null -> returns null (option has no argument).
 *    - Branch: options.containsKey(opt) && res != null -> TypeHandler.createValue(res, type).
 * 8. addOption(Option opt):
 *    - Branch: opt.getKey() == null -> falls back to opt.getLongOpt().
 *    - Branch: opt.getKey() != null -> names.put(opt.getLongOpt(), key).
 *    - Hashcode map insertion: verifies iterator() traverses hashcodeMap.values().
 * 9. Unrecognized Arguments (addArg, getArgs, getArgList):
 *    - Sequence ordering, empty state, array size conversion via toArray(new String[size]).
 * 10. CLI-1 / BugCLI13 Defect Focus:
 *    - Verifies cross-resolution between short opt, long opt, and hyphenated variations ("-d", "--debug")
 *      retrieving the expected argument values across multiple configured options.
 * ====================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

public class CommandLineGeminiTest {

    // =========================================================================
    // Partition A: Option Presence & Lookup (hasOption)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasOption_SingleCharAndString() {
        CommandLine cl = new CommandLine();
        Option opt = new Option("a", "alpha", false, "Alpha option");
        cl.addOption(opt);

        assertTrue("Short option char should exist", cl.hasOption('a'));
        assertTrue("Short option string should exist", cl.hasOption("a"));
        assertFalse("Unregistered short option char should not exist", cl.hasOption('z'));
        assertFalse("Unregistered string option should not exist", cl.hasOption("z"));
    }

    @Test(timeout = 4000)
    public void testHasOption_LongOptOnly() {
        CommandLine cl = new CommandLine();
        Option longOnly = new Option(null, "config", true, "Config file option");
        cl.addOption(longOnly);

        assertTrue("Long-only option should be found by its key", cl.hasOption("config"));
        assertFalse("Hyphenated prefix should not match direct map key", cl.hasOption("--config"));
        assertFalse("Null query should return false", cl.hasOption(null));
        assertFalse("Empty string should return false", cl.hasOption(""));
    }

    // =========================================================================
    // Partition B: Value Retrieval (getOptionValue, getOptionValues, default values)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetOptionValue_SingleAndDefault() {
        CommandLine cl = new CommandLine();
        Option opt = new Option("f", "file", true, "Input file");
        opt.addValue("data.csv");
        cl.addOption(opt);

        assertEquals("data.csv", cl.getOptionValue('f'));
        assertEquals("data.csv", cl.getOptionValue("f"));
        assertEquals("data.csv", cl.getOptionValue("file"));
        assertEquals("data.csv", cl.getOptionValue('f', "fallback.csv"));
        assertEquals("data.csv", cl.getOptionValue("f", "fallback.csv"));

        // Non-existent option fallback
        assertEquals("fallback.csv", cl.getOptionValue("nonexistent", "fallback.csv"));
        assertEquals("fallback.csv", cl.getOptionValue('z', "fallback.csv"));
        assertNull(cl.getOptionValue("nonexistent"));
        assertNull(cl.getOptionValue('z'));
    }

    @Test(timeout = 4000)
    public void testGetOptionValues_MultipleValues() {
        CommandLine cl = new CommandLine();
        Option opt = new Option("m", "multi", true, "Multiple values");
        opt.addValue("val1");
        opt.addValue("val2");
        opt.addValue("val3");
        cl.addOption(opt);

        String[] charValues = cl.getOptionValues('m');
        assertNotNull(charValues);
        assertEquals(3, charValues.length);
        assertEquals("val1", charValues[0]);
        assertEquals("val2", charValues[1]);
        assertEquals("val3", charValues[2]);

        String[] stringValues = cl.getOptionValues("m");
        assertNotNull(stringValues);
        assertArrayEquals(charValues, stringValues);

        String[] longValues = cl.getOptionValues("multi");
        assertNotNull(longValues);
        assertArrayEquals(charValues, longValues);

        // getOptionValue should return the first element
        assertEquals("val1", cl.getOptionValue('m'));
        assertEquals("val1", cl.getOptionValue("m"));
        assertEquals("val1", cl.getOptionValue("multi"));
    }

    @Test(timeout = 4000)
    public void testGetOptionValues_OptionWithNoValues() {
        CommandLine cl = new CommandLine();
        Option opt = new Option("n", "none", false, "Flag without arguments");
        cl.addOption(opt);

        assertNull("Flag option with no arguments should return null values", cl.getOptionValues("n"));
        assertNull("Flag option with no arguments should return null value", cl.getOptionValue("n"));
        assertEquals("default", cl.getOptionValue("n", "default"));
        assertEquals("default", cl.getOptionValue('n', "default"));
    }

    @Test(timeout = 4000)
    public void testGetOptionValues_UnregisteredOption() {
        CommandLine cl = new CommandLine();

        assertNull("Unregistered option should return null values array", cl.getOptionValues("unknown"));
        assertNull("Unregistered char option should return null values array", cl.getOptionValues('u'));
        assertNull("Unregistered option should return null value", cl.getOptionValue("unknown"));
        assertNull("Unregistered char option should return null value", cl.getOptionValue('u'));
    }

    // =========================================================================
    // Partition C: Option Object & Type Conversion (getOptionObject)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetOptionObject_UnregisteredAndNoArgument() {
        CommandLine cl = new CommandLine();

        // Branch: !options.containsKey(opt)
        assertNull("Unregistered option object should be null", cl.getOptionObject("missing"));
        assertNull("Unregistered char option object should be null", cl.getOptionObject('m'));

        // Branch: options.containsKey(opt) && res == null
        Option flagOpt = new Option("f", "flag", false, "Boolean flag");
        cl.addOption(flagOpt);
        assertNull("Option with no value should return null object", cl.getOptionObject("f"));
        assertNull("Option with no value should return null object via char", cl.getOptionObject('f'));
    }

    @Test(timeout = 4000)
    public void testGetOptionObject_WithTypeHandler() {
        CommandLine cl = new CommandLine();
        Option intOpt = new Option("c", "count", true, "Item count");
        intOpt.setType(Number.class);
        intOpt.addValue("42");
        cl.addOption(intOpt);

        Object resultString = cl.getOptionObject("c");
        assertNotNull(resultString);
        assertTrue(resultString instanceof Number);
        assertEquals(42L, ((Number) resultString).longValue());

        Object resultChar = cl.getOptionObject('c');
        assertNotNull(resultChar);
        assertEquals(42L, ((Number) resultChar).longValue());
    }

    // =========================================================================
    // Partition D: Unrecognized Arguments Handling (addArg, getArgs, getArgList)
    // =========================================================================

    @Test(timeout = 4000)
    public void testArgs_EmptyAndSequential() {
        CommandLine cl = new CommandLine();

        assertNotNull(cl.getArgs());
        assertEquals(0, cl.getArgs().length);
        assertNotNull(cl.getArgList());
        assertTrue(cl.getArgList().isEmpty());

        cl.addArg("extra1");
        cl.addArg("extra2");
        cl.addArg("extra3");

        String[] args = cl.getArgs();
        assertEquals(3, args.length);
        assertEquals("extra1", args[0]);
        assertEquals("extra2", args[1]);
        assertEquals("extra3", args[2]);

        List argList = cl.getArgList();
        assertEquals(3, argList.size());
        assertEquals("extra1", argList.get(0));
        assertEquals("extra2", argList.get(1));
        assertEquals("extra3", argList.get(2));
    }

    // =========================================================================
    // Partition E: Option Collections & Iteration (getOptions, iterator)
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptions_ArrayAndIterator() {
        CommandLine cl = new CommandLine();
        Option optA = new Option("a", "all", false, "All flag");
        Option optB = new Option("b", "buffer", true, "Buffer size");
        optB.addValue("1024");

        cl.addOption(optA);
        cl.addOption(optB);

        Option[] optionsArray = cl.getOptions();
        assertNotNull(optionsArray);
        assertEquals(2, optionsArray.length);

        Iterator it = cl.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            Object item = it.next();
            assertTrue("Iterator items should be Option instances", item instanceof Option);
            count++;
        }
        assertEquals(2, count);
    }

    @Test(timeout = 4000)
    public void testOptions_OptionWithoutLongOpt() {
        CommandLine cl = new CommandLine();
        Option optShortOnly = new Option("s", false, "Short only");
        cl.addOption(optShortOnly);

        assertTrue(cl.hasOption("s"));
        assertTrue(cl.hasOption('s'));
        Option[] optionsArray = cl.getOptions();
        assertEquals(1, optionsArray.length);
        assertSame(optShortOnly, optionsArray[0]);
    }

    // =========================================================================
    // Partition F: CLI-1 / BugCLI13 Defect Target Suite
    // =========================================================================

    @Test(timeout = 4000)
    public void testCLI13_MultipleOptionsAndHyphenResolution() {
        CommandLine cl = new CommandLine();

        Option debugOpt = new Option("d", "debug", true, "Debug mode setting");
        debugOpt.addValue("true");

        Option verboseOpt = new Option("v", "verbose", true, "Verbosity level");
        verboseOpt.addValue("3");

        cl.addOption(debugOpt);
        cl.addOption(verboseOpt);

        // 1. Verify querying via short opt string and char
        assertEquals("true", cl.getOptionValue("d"));
        assertEquals("true", cl.getOptionValue('d'));
        assertEquals("3", cl.getOptionValue("v"));
        assertEquals("3", cl.getOptionValue('v'));

        // 2. Verify querying via long opt
        assertEquals("true", cl.getOptionValue("debug"));
        assertEquals("3", cl.getOptionValue("verbose"));

        // 3. Verify querying via single hyphen prefix
        assertEquals("true", cl.getOptionValue("-d"));
        assertEquals("3", cl.getOptionValue("-v"));

        // 4. Verify querying via double hyphen prefix
        assertEquals("true", cl.getOptionValue("--debug"));
        assertEquals("3", cl.getOptionValue("--verbose"));

        // 5. Verify array queries resolve accurately with hyphens
        String[] debugVals = cl.getOptionValues("--debug");
        assertNotNull(debugVals);
        assertEquals(1, debugVals.length);
        assertEquals("true", debugVals[0]);

        String[] verboseVals = cl.getOptionValues("-v");
        assertNotNull(verboseVals);
        assertEquals(1, verboseVals.length);
        assertEquals("3", verboseVals[0]);
    }
}