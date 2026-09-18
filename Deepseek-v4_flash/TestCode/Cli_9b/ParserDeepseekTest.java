package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Properties;

public class ParserDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: Parser (abstract)
     * Tested via concrete subclass PosixParser (same package).
     *
     * Partitions:
     *  A) Core parse logic: null arguments, empty arguments, normal options, double dash, single dash,
     *     stopAtNonOption=true, eatTheRest.
     *  B) Boundary: extreme argument lengths, empty required list, single required option.
     *  C) Defect-targeted: Missing required options with two or more required options – the exception
     *     message should separate option keys with ", " (comma space). The bug concatenates them.
     *  D) Exception branches: MissingArgumentException (missing arg for required option),
     *     UnrecognizedOptionException, processArgs fallback when non-option found.
     *  E) processProperties: null, empty, valid properties matching options with/without args,
     *     value handling (yes/true/1 vs others), break condition.
     *  F) OptionGroup handling: required group selection, unselecting previous option.
     *  G) Lifecycle: clearValues called at parse start (CLI-71).
     *
     * Branch coverage targets for parse() method (high-level):
     *   - arguments == null → set to empty array
     *   - "--" → eatTheRest = true
     *   - "-" → if stopAtNonOption eatTheRest else cmd.addArg
     *   - t.startsWith("-") → if stopAtNonOption && !hasOption then eatTheRest+addArg else processOption
     *   - else → cmd.addArg; if stopAtNonOption eatTheRest
     *   - eatTheRest loop: skip "--" tokens
     *
     * Branch coverage for processOption():
     *   - hasOption? → throw UnrecognizedOptionException
     *   - opt.isRequired() → remove from requiredOptions
     *   - optionGroup != null → setSelected, if group.required remove from requiredOptions
     *   - opt.hasArg() → processArgs
     *   - always cmd.addOption(opt)
     *
     * Branch coverage for processArgs():
     *   - iter.hasNext() loop
     *   - if argument is an option → iter.previous(); break
     *   - else try addValueForProcessing; on RuntimeException → iter.previous(); break
     *   - after loop: if no values && !hasOptionalArg() → MissingArgumentException
     *
     * Branch coverage for processProperties():
     *   - properties == null → return
     *   - loop over propertyNames
     *      - if cmd.hasOption(option) → skip (addOption already done)
     *      - else:
     *         - get Option
     *         - if opt.hasArg() and values empty/null → try addValueForProcessing
     *         - else if not (yes/true/1) → break (skip adding option)
     *         - always cmd.addOption(opt)
     *
     * Branch coverage for checkRequiredOptions():
     *   - requiredOptions.size() > 0:
     *        - size == 1 → "s" omitted
     *        - loop: buffer.append(iter.next()) WITHOUT separator → BUG
     */

    // ------------------------------------------------
    // Section A: Core Functional & State Transitions
    // ------------------------------------------------

    @Test(timeout = 4000)
    public void testParseNullArguments() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc with arg");

        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, null);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
        assertFalse(cl.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseEmptyArguments() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[0]);
        assertNotNull(cl);
        assertEquals(0, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testParseSingleOption() throws Exception {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[]{"-v"});
        assertTrue(cl.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testParseOptionWithArgument() throws Exception {
        Options options = new Options();
        options.addOption("o", true, "output file");
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[]{"-o", "out.txt"});
        assertTrue(cl.hasOption("o"));
        assertEquals("out.txt", cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testParseDoubleDash() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[]{"--", "-a", "arg1"});
        assertFalse(cl.hasOption("a")); // "--" stops option processing
        assertEquals(2, cl.getArgs().length);
        assertEquals("-a", cl.getArgs()[0]);
        assertEquals("arg1", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseSingleDashStop() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        // With stopAtNonOption=false, single dash is treated as an argument
        CommandLine cl = parser.parse(options, new String[]{"-", "something"}, false);
        assertEquals(1, cl.getArgs().length);
        assertEquals("-", cl.getArgs()[0]); // NOTE: bug? Actually single dash becomes an arg; no option "a"
        // Because "-" does not start with "-" after dash? Actually "-" is handled separately.
        // The first token is "-", so it adds "-" as arg, then "something" as arg.
        assertEquals(2, cl.getArgs().length);
        assertEquals("something", cl.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        // With stopAtNonOption=true, first non-option "file" stops parsing
        CommandLine cl = parser.parse(options, new String[]{"-a", "file", "-b"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(1, cl.getArgs().length);
        assertEquals("file", cl.getArgs()[0]);
        // "-b" should not be processed because stopAtNonOption=true after "file"
        assertFalse(cl.hasOption("b"));
    }

    // ------------------------------------------------
    // Section B: Boundary Value Analysis
    // ------------------------------------------------

    @Test(timeout = 4000)
    public void testParseWithManyArguments() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        String[] args = new String[100];
        for (int i = 0; i < 100; i++) {
            args[i] = "arg" + i;
        }
        CommandLine cl = parser.parse(options, args, false);
        assertEquals(100, cl.getArgs().length);
    }

    @Test(timeout = 4000)
    public void testRequiredOptionsEmpty() throws Exception {
        Options options = new Options();
        options.addRequiredOption("a", "a-option", false, "desc");
        options.addRequiredOption("b", "b-option", true, "desc with arg");
        PosixParser parser = new PosixParser();
        // Provide both required options -> no exception
        CommandLine cl = parser.parse(options, new String[]{"-a", "-b", "val"});
        assertTrue(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testOneRequiredOptionMissing() throws Exception {
        Options options = new Options();
        options.addRequiredOption("a", "a-option", false, "desc");
        options.addRequiredOption("b", "b-option", false, "desc");
        PosixParser parser = new PosixParser();
        // Only supply -a; "b" is missing
        try {
            parser.parse(options, new String[]{"-a"});
            fail("MissingOptionException expected");
        } catch (MissingOptionException e) {
            // For single missing option, message should be "Missing required option: b"
            // But our options has two required, but we supplied -a, so only "b" missing?
            // Wait: both are required; after adding -a, "a" is removed from required list, "b" remains.
            // Actually both are required, so the required list initially has two items.
            // After processing -a, it removes "a" from requiredOptions (see processOption: opt.isRequired() removes opt.getKey()).
            // So requiredOptions contains "b". Size == 1 -> "Missing required option: b" (no 's').
            String msg = e.getMessage();
            assertTrue("Message should contain 'Missing required option: b'", msg.contains("Missing required option: b"));
        }
    }

    // ------------------------------------------------
    // Section C: Defect-Targeted Branch - MissingRequiredOptions with multiple required
    // ------------------------------------------------

    @Test(timeout = 4000)
    public void testMissingRequiredOptionsWithTwoOptions() throws Exception {
        Options options = new Options();
        options.addRequiredOption("a", "a-option", false, "desc");
        options.addRequiredOption("b", "b-option", false, "desc");
        PosixParser parser = new PosixParser();
        try {
            parser.parse(options, new String[0]);
            fail("MissingOptionException expected");
        } catch (MissingOptionException e) {
            String msg = e.getMessage();
            // Expected: "Missing required options: a, b"
            assertTrue("Message should contain 'a, b' (with comma and space)",
                       msg.contains("a, b"));
            assertFalse("Message should NOT be just concatenated without separator",
                        msg.contains("ab"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingRequiredOptionsWithThreeOptions() throws Exception {
        Options options = new Options();
        options.addRequiredOption("x", "x-option", false, "desc");
        options.addRequiredOption("y", "y-option", false, "desc");
        options.addRequiredOption("z", "z-option", false, "desc");
        PosixParser parser = new PosixParser();
        try {
            parser.parse(options, new String[]{"-x"}); // only x given, y and z missing
            fail("MissingOptionException expected");
        } catch (MissingOptionException e) {
            String msg = e.getMessage();
            // Expected: "Missing required options: y, z"
            assertTrue("Message should contain 'y, z'", msg.contains("y, z"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingRequiredOptionsSingle() throws Exception {
        Options options = new Options();
        options.addRequiredOption("s", "single", false, "desc");
        PosixParser parser = new PosixParser();
        try {
            parser.parse(options, new String[0]);
            fail("MissingOptionException expected");
        } catch (MissingOptionException e) {
            String msg = e.getMessage();
            // Expected: "Missing required option: s"
            assertTrue("Message should contain 'Missing required option: s'",
                       msg.contains("Missing required option: s"));
        }
    }

    // ------------------------------------------------
    // Section D: Exception & Defensive Guard Paths
    // ------------------------------------------------

    @Test(timeout = 4000)
    public void testMissingArgumentException() throws Exception {
        Options options = new Options();
        options.addOption("o", true, "requires arg");
        PosixParser parser = new PosixParser();
        try {
            parser.parse(options, new String[]{"-o"});
            fail("MissingArgumentException expected");
        } catch (MissingArgumentException e) {
            assertTrue(e.getMessage().contains("Missing argument for option: o"));
        }
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionException() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        try {
            parser.parse(options, new String[]{"-x"});
            fail("UnrecognizedOptionException expected");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("Unrecognized option: -x"));
        }
    }

    @Test(timeout = 4000)
    public void testProcessArgsNonOptionBreak() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", true, "output");
        opt.setOptionalArg(true);  // makes argument optional
        options.addOption(opt);
        PosixParser parser = new PosixParser();
        // After -o, the next token is -a (another option) -> should stop processing args and not throw MissingArgumentException
        options.addOption("a", false, "desc");
        CommandLine cl = parser.parse(options, new String[]{"-o", "-a"});
        assertTrue(cl.hasOption("o"));
        assertNull(cl.getOptionValue("o"));  // no value because -a interrupted
        assertTrue(cl.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionInStopAtNonOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        // unrecognized option -x with stopAtNonOption=true: should be added as arg, not throw
        CommandLine cl = parser.parse(options, new String[]{"-a", "-x", "foo"}, true);
        assertTrue(cl.hasOption("a"));
        assertEquals(2, cl.getArgs().length);
        assertEquals("-x", cl.getArgs()[0]);
        assertEquals("foo", cl.getArgs()[1]);
    }

    // ------------------------------------------------
    // Section E: Object Lifecycle & Contract / ProcessProperties
    // ------------------------------------------------

    @Test(timeout = 4000)
    public void testProcessPropertiesNull() {
        Parser parser = new PosixParser();
        // processProperties is called only via parse, but we can test indirectly
        // We'll call it directly: need to set Options and cmd?
        // Since processProperties is protected, we can use a subclass or reflection?
        // To keep simple, we test through parse with null properties => should not fail
        Options options = new Options();
        options.addOption("a", false, "desc");
        try {
            CommandLine cl = parser.parse(options, new String[]{"-a"}, (Properties) null, false);
            assertTrue(cl.hasOption("a"));
        } catch (ParseException e) {
            fail("No exception expected");
        }
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesValidProperties() throws Exception {
        Options options = new Options();
        options.addOption("d", true, "dir");
        options.addOption("v", false, "verbose");
        PosixParser parser = new PosixParser();
        Properties props = new Properties();
        props.setProperty("d", "/tmp");
        props.setProperty("v", "true");
        // Parse with no arguments; properties should set options
        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertTrue(cl.hasOption("d"));
        assertEquals("/tmp", cl.getOptionValue("d"));
        assertTrue(cl.hasOption("v"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesOptionAlreadyGiven() throws Exception {
        Options options = new Options();
        options.addOption("o", true, "output");
        PosixParser parser = new PosixParser();
        Properties props = new Properties();
        props.setProperty("o", "prop_val");
        // Parse giving -o command line argument
        CommandLine cl = parser.parse(options, new String[]{"-o", "cmd_val"}, props, false);
        // Command line should take precedence; property value should be ignored
        assertEquals("cmd_val", cl.getOptionValue("o"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesNonArgOptionFalseValue() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        PosixParser parser = new PosixParser();
        Properties props = new Properties();
        props.setProperty("f", "false");
        CommandLine cl = parser.parse(options, new String[0], props, false);
        // value "false" is not yes/true/1, so flag should NOT be added (break)
        assertFalse(cl.hasOption("f"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesNonArgOptionTrueValue() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        PosixParser parser = new PosixParser();
        Properties props = new Properties();
        props.setProperty("f", "yes"); // yes, true, 1 are accepted
        CommandLine cl = parser.parse(options, new String[0], props, false);
        assertTrue(cl.hasOption("f"));
    }

    @Test(timeout = 4000)
    public void testProcessPropertiesArgOptionWithValues() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", true, "output");
        options.addOption(opt);
        PosixParser parser = new PosixParser();
        Properties props = new Properties();
        props.setProperty("o", "value1");
        // First parse to set the option via property, then parse again to ensure values are cleared (CLI-71)
        CommandLine cl1 = parser.parse(options, new String[0], props, false);
        assertTrue(cl1.hasOption("o"));
        assertEquals("value1", cl1.getOptionValue("o"));
        // Second parse with different property
        props.setProperty("o", "value2");
        CommandLine cl2 = parser.parse(options, new String[0], props, false);
        assertEquals("value2", cl2.getOptionValue("o")); // values should have been cleared
    }

    @Test(timeout = 4000)
    public void testOptionGroupRequired() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        PosixParser parser = new PosixParser();
        // Provide one option from group; group is required and should be removed from required list
        CommandLine cl = parser.parse(options, new String[]{"-a"});
        assertTrue(cl.hasOption("a"));
        assertFalse(cl.hasOption("b"));
        // No MissingOptionException expected because group requirement satisfied
    }

    @Test(timeout = 4000)
    public void testOptionGroupSelection() throws Exception {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.create("a"));
        group.addOption(OptionBuilder.create("b"));
        options.addOptionGroup(group);
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[]{"-a", "-b"});
        // Group selects only the last option
        assertFalse(cl.hasOption("a"));
        assertTrue(cl.hasOption("b"));
    }

    // Additional branch: processArgs with RuntimeException (value processing fails)
    @Test(timeout = 4000)
    public void testProcessArgsRuntimeException() throws Exception {
        Options options = new Options();
        Option opt = new Option("o", true, "output") {
            @Override
            public void addValueForProcessing(String value) {
                throw new RuntimeException("Simulated add failure");
            }
        };
        options.addOption(opt);
        PosixParser parser = new PosixParser();
        // The next token after -o is "value", but addValueForProcessing throws RuntimeException.
        // processArgs should catch it, do iter.previous(), break, then check for optional arg? opt.hasOptionalArg() is false.
        // So it should throw MissingArgumentException because no values set.
        try {
            parser.parse(options, new String[]{"-o", "value"});
            fail("MissingArgumentException expected");
        } catch (MissingArgumentException e) {
            assertTrue(e.getMessage().contains("Missing argument for option: o"));
        }
    }

    // Eat-the-rest branch: after "--", the loop adds remaining tokens as args, skipping any double dash.
    @Test(timeout = 4000)
    public void testEatTheRestWithDoubleDashTokens() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        PosixParser parser = new PosixParser();
        CommandLine cl = parser.parse(options, new String[]{"--", "--", "file1", "--", "file2"}, false);
        assertEquals(3, cl.getArgs().length);
        // The first "--" triggers eatTheRest; subsequent double dashes should be skipped (only one added)
        // Actually the loop: after setting eatTheRest=true, it enters while(iterator.hasNext()) inside eatTheRest block.
        // It adds tokens one by one, but if the token is "--", it does NOT add it (see code: if (!"--".equals(str)) { cmd.addArg(str); }).
        // So the remaining tokens: "--" (skipped), "file1" (added), "--" (skipped), "file2" (added). 
        assertEquals("file1", cl.getArgs()[0]);
        assertEquals("file2", cl.getArgs()[1]);
        // Additionally, the first "--" is already processed (the token that caused eatTheRest). It is not added.
        assertEquals(2, cl.getArgs().length);
    }
}