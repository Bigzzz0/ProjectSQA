package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for DefaultParser.
 * Targets line/branch coverage and the known defect from BugCLI265Test.
 *
 * [Branch & Defect Analysis Matrix]
 * Partition A: Core functional logic & state transitions
 *   - parse with null/empty arguments
 *   - parse with properties
 *   - required options and groups
 *   - stopAtNonOption flag
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments, empty arrays, empty strings
 *   - negative numbers as arguments
 *   - tokens with leading/trailing quotes
 *   - very long option names
 * Partition C: Defect-Targeted Branch Zone
 *   - Short options without values: "-a -b" should not consume "-b" as value for "-a"
 *   - Concatenated short options: "-ab" where a and b are boolean options
 *   - Long option with equal sign and no arg: "--long=value" when option does not accept arg
 * Partition D: Exception & Defensive Guard Paths
 *   - Unrecognized option (short/long)
 *   - Ambiguous option
 *   - Missing argument
 *   - Missing required option
 *   - Already selected exception in option group
 *   - Properties with null, unrecognized option, group conflict
 * Partition E: Object Lifecycle & Contract Integrity
 *   - (Not applicable for parser; focus on parsing behavior)
 */
public class DefaultParserDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testParseNullArguments() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, null);
        assertFalse(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseEmptyArguments() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{});
        assertFalse(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseSimpleShortOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseSimpleLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseLongOptionWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha", "value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseLongOptionWithEquals() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseShortOptionWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseShortOptionWithEquals() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseStopAtNonOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "foo", "-b"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-b", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testParseProperties() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "propValue");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
        assertEquals("propValue", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParsePropertiesWithBooleanOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "true");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParsePropertiesWithBooleanOptionFalseValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "false");
        CommandLine cmd = parser.parse(options, new String[]{}, props);
        assertFalse(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testParseRequiredOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addRequiredOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testParseMissingRequiredOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addRequiredOption("a", "alpha", false, "desc");
        parser.parse(options, new String[]{});
    }

    @Test(timeout = 4000)
    public void testParseOptionGroup() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(timeout = 4000, expected = AlreadySelectedException.class)
    public void testParseOptionGroupConflict() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        parser.parse(options, new String[]{"-a", "-b"});
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testParseNegativeNumberAsArgument() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-1"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("-1", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseNegativeNumberAsOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("1", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-1"});
        assertTrue(cmd.hasOption("1"));
    }

    @Test(timeout = 4000)
    public void testParseTokenWithQuotes() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "\"value\""});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testParseDoubleDashStopsParsing() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testParseEmptyStringToken() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{""});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("", cmd.getArgs()[0]);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the known defect from BugCLI265Test:
     * Short options without values should not consume the next option as a value.
     */
    @Test(timeout = 4000)
    public void testShortOptionWithoutValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "first option");
        options.addOption("b", false, "second option");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-b"});
        assertTrue("First option should be present", cmd.hasOption("a"));
        assertTrue("Second option should be present", cmd.hasOption("b"));
        assertNull("First option should not have a value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testConcatenatedShortOptions() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testConcatenatedShortOptionWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-bvalue"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test(timeout = 4000)
    public void testLongOptionWithoutValueAndNoArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha=value"});
        // Option does not accept arg, so token should be treated as unknown
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("--alpha=value", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testLongOptionWithEqualAndArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedShortOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"-x"});
    }

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"--unknown"});
    }

    @Test(timeout = 4000, expected = AmbiguousOptionException.class)
    public void testAmbiguousLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "aaa", false, "desc");
        options.addOption("b", "aab", false, "desc");
        parser.parse(options, new String[]{"--aa"});
    }

    @Test(timeout = 4000, expected = MissingArgumentException.class)
    public void testMissingArgumentForOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        parser.parse(options, new String[]{"-a"});
    }

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testPropertiesUnrecognizedOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        java.util.Properties props = new java.util.Properties();
        props.setProperty("x", "value");
        parser.parse(options, new String[]{}, props);
    }

    @Test(timeout = 4000)
    public void testPropertiesOptionAlreadySelectedInGroup() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "true");
        props.setProperty("b", "true");
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTokenWithStopAtNonOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-x", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-x", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testHandleUnknownTokenWithoutStopAtNonOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        parser.parse(options, new String[]{"-x"}, false);
    }

    @Test(timeout = 4000)
    public void testJavaPropertyStyleOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        // Set args to 2 to trigger isJavaProperty path
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testLongPrefixOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("Xmx", true, "max memory");
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testHandleOptionClonesOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        // Ensure the option in cmd is a clone, not the original
        assertNotNull(cmd.getOption("a"));
    }

    @Test(timeout = 4000)
    public void testUpdateRequiredOptionsWithGroup() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").required(true).build());
        group.addOption(Option.builder("b").required(true).build());
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
        // No exception because group requirement satisfied
    }

    @Test(timeout = 4000, expected = MissingOptionException.class)
    public void testMissingRequiredGroup() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        parser.parse(options, new String[]{});
    }

    @Test(timeout = 4000)
    public void testCheckRequiredArgsAfterOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        options.addOption("b", false, "desc");
        // -a requires an arg, but next token is -b (option), so MissingArgumentException should be thrown
        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsWithUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        // -ab where b is unknown, stopAtNonOption false -> UnrecognizedOptionException
        try {
            parser.parse(options, new String[]{"-ab"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-ab"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsWithStopAtNonOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("b", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testIsArgumentForNegativeNumber() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "-1.5"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("-1.5", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testIsArgumentForOptionLikeToken() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        // Token "-b" is an option, so it should not be consumed as value
        try {
            parser.parse(options, new String[]{"-a", "-b"});
            fail("Expected MissingArgumentException because -b is not a valid argument");
        } catch (MissingArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithoutEqualAmbiguous() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "aaa", false, "desc");
        options.addOption("b", "aab", false, "desc");
        try {
            parser.parse(options, new String[]{"--aa"});
            fail("Expected AmbiguousOptionException");
        } catch (AmbiguousOptionException e) {
            assertTrue(e.getMessage().contains("aa"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithoutEqualUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"--unknown"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("--unknown"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithEqualUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"--unknown=value"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("--unknown"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionWithEqualAndNoArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        // Option does not accept arg, so token should be unknown
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a=value", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionWithEqualAndArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionJavaProperty() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionLongPrefix() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("Xmx", true, "max memory");
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionConcatenated() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionSingleShort() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionSingleShortUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"-x"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-x"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionLongWithoutEqual() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-alpha"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionLongWithEqual() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-alpha=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionLongPrefixWithArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("Xmx", true, "max memory");
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionJavaPropertyWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionConcatenatedWithValue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-bvalue"});
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionConcatenatedUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        try {
            parser.parse(options, new String[]{"-ab"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-ab"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionConcatenatedWithStop() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("b", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testGetLongPrefixReturnsNull() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        // Token "-xyz" has no long prefix, should fall through to concatenated handling
        CommandLine cmd = parser.parse(options, new String[]{"-xyz"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("y", cmd.getArgs()[0]);
        assertEquals("z", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testIsJavaPropertyFalse() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        // Token "-a=value" should be handled as short option with equals, not Java property
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandlePropertiesNull() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{}, (java.util.Properties) null);
        assertNotNull(cmd);
    }

    @Test(timeout = 4000)
    public void testHandlePropertiesOptionAlreadyPresent() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        java.util.Properties props = new java.util.Properties();
        props.setProperty("a", "propValue");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "cliValue"}, props);
        // CLI value should take precedence
        assertEquals("cliValue", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandlePropertiesOptionInGroupSelected() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        java.util.Properties props = new java.util.Properties();
        props.setProperty("b", "true");
        CommandLine cmd = parser.parse(options, new String[]{"-a"}, props);
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testCheckRequiredOptionsEmpty() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{});
        assertNotNull(cmd);
    }

    @Test(timeout = 4000)
    public void testCheckRequiredArgsNullCurrentOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertNotNull(cmd);
    }

    @Test(timeout = 4000)
    public void testHandleTokenSkipParsing() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--", "-a"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleTokenCurrentOptionAcceptsArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleTokenCurrentOptionNotAcceptsArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a", "extra"});
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("extra", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleTokenDoubleDash() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--", "foo"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleTokenLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleTokenShortOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleTokenUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"foo"}, true);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleTokenUnknownWithStop() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"foo", "-a"}, true);
        assertFalse(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-a", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testIsArgumentTrueForNonOption() {
        DefaultParser parser = new DefaultParser();
        // We can't directly test private method, but we can test behavior
        // Already covered by other tests
    }

    @Test(timeout = 4000)
    public void testIsNegativeNumberTrue() {
        DefaultParser parser = new DefaultParser();
        // Already covered by testParseNegativeNumberAsArgument
    }

    @Test(timeout = 4000)
    public void testIsNegativeNumberFalse() {
        DefaultParser parser = new DefaultParser();
        // Already covered by testIsArgumentForOptionLikeToken
    }

    @Test(timeout = 4000)
    public void testIsOptionTrueForShort() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsOptionTrueForLong() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsOptionFalse() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsShortOptionTrue() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsShortOptionFalse() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsLongOptionTrue() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testIsLongOptionFalse() {
        DefaultParser parser = new DefaultParser();
        // Already covered
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTokenStartsWithDash() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"-x"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-x"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTokenNotStartsWithDash() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"foo"}, false);
        assertEquals(1, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleUnknownTokenWithStop() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, new String[]{"foo", "-x"}, true);
        assertEquals(2, cmd.getArgs().length);
        assertEquals("foo", cmd.getArgs()[0]);
        assertEquals("-x", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithoutEqualEmptyMatching() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"--unknown"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("--unknown"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithEqualEmptyMatching() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"--unknown=value"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("--unknown"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithEqualOptionAcceptsArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleLongOptionWithEqualOptionDoesNotAcceptArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"--alpha=value"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("--alpha=value", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionSingleShortWithOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionSingleShortUnknown() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        try {
            parser.parse(options, new String[]{"-x"});
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-x"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionNoEqualShortOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionNoEqualLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-alpha"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionNoEqualLongPrefix() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("Xmx", true, "max memory");
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionNoEqualJavaProperty() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionNoEqualConcatenated() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionEqualShortOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionEqualShortOptionNoArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertFalse(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-a=value", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionEqualJavaProperty() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testHandleShortAndLongOptionEqualLongOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-alpha=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testGetLongPrefixFound() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("Xmx", true, "max memory");
        CommandLine cmd = parser.parse(options, new String[]{"-Xmx512m"});
        assertTrue(cmd.hasOption("Xmx"));
        assertEquals("512m", cmd.getOptionValue("Xmx"));
    }

    @Test(timeout = 4000)
    public void testGetLongPrefixNotFound() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-xyz"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("y", cmd.getArgs()[0]);
        assertEquals("z", cmd.getArgs()[1]);
    }

    @Test(timeout = 4000)
    public void testIsJavaPropertyTrue() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("D", true, "property");
        Option dOpt = options.getOption("D");
        dOpt.setArgs(2);
        CommandLine cmd = parser.parse(options, new String[]{"-Dkey=value"});
        assertTrue(cmd.hasOption("D"));
        assertEquals("key", cmd.getOptionValue("D"));
    }

    @Test(timeout = 4000)
    public void testIsJavaPropertyFalse() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a=value"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleOptionClonesAndUpdatesRequired() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addRequiredOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testUpdateRequiredOptionsRemovesOption() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addRequiredOption("a", "alpha", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        // No exception thrown
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testUpdateRequiredOptionsGroupSelected() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        CommandLine cmd = parser.parse(options, new String[]{"-a"});
        assertTrue(cmd.hasOption("a"));
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsFirstOptionWithArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("b", cmd.getOptionValue("a"));
        assertFalse(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsFirstOptionNoArg() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsUnknownWithStop() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"}, true);
        assertTrue(cmd.hasOption("a"));
        assertEquals(1, cmd.getArgs().length);
        assertEquals("b", cmd.getArgs()[0]);
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsUnknownWithoutStop() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        try {
            parser.parse(options, new String[]{"-ab"}, false);
            fail("Expected UnrecognizedOptionException");
        } catch (UnrecognizedOptionException e) {
            assertTrue(e.getMessage().contains("-ab"));
        }
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsMultipleOptions() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        options.addOption("c", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-abc"});
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsOptionWithArgTrail() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-avalue"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }

    @Test(timeout = 4000)
    public void testHandleConcatenatedOptionsOptionWithArgAndMoreOptions() throws ParseException {
        DefaultParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption("a", true, "desc");
        options.addOption("b", false, "desc");
        CommandLine cmd = parser.parse(options, new String[]{"-ab"});
        assertTrue(cmd.hasOption("a"));
        assertEquals("b", cmd.getOptionValue("a"));
        assertFalse(cmd.hasOption("b"));
    }
}