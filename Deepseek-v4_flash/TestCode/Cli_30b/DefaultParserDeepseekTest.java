package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Properties;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: DefaultParser class
 * Defect Type: OptionGroup property handling bug leading to AlreadySelectedException and NullPointerException
 * 
 * Key Decision Branches Targeted:
 * 1. handleProperties(): The loop iterates over Properties entries but calls opt.addValueForProcessing()
 *    without first checking if opt is null. When properties contain unexpected option names, opt can be null.
 * 2. handleProperties(): After calling handleOption(opt), currentOption is set to null. But when an option
 *    from a group is processed via properties, the group's selected state is updated in updateRequiredOptions(),
 *    but the group selection validation may not be correctly ordering or checking selections.
 * 3. checkRequiredArgs(): Checks if currentOption != null && currentOption.requiresArg() and throws
 *    MissingArgumentException if so.
 * 4. handleOption(): Calls option.clone() and then updateRequiredOptions() which can throw AlreadySelectedException
 *    if two options from same group are processed in conflicting order.
 * 5. isOption() -> isShortOption() and isLongOption() branches for token parsing
 * 6. handleShortAndLongOption() complex branching for -S, -SV, -S=V, -L, -LV, -L=V, -l forms
 * 7. handleConcatenatedOptions() iterative breakdown of short options
 * 8. handleUnknownToken() with stopAtNonOption and token.startsWith("-") branches
 * 9. checkRequiredOptions() checks expectedOpts.isEmpty() and throws MissingOptionException
 * 10. handleLongOptionWithoutEqual() matchingOpts empty/size>1/size==1 branches
 * 11. handleLongOptionWithEqual() same branches plus option.acceptsArg() check
 * 12. isJavaProperty() checks args >= 2 or UNLIMITED_VALUES
 * 13. getLongPrefix() iterative prefix search from t.length()-2 down to 1
 * 14. isArgument() calls isOption() and isNegativeNumber() which tries Double.parseDouble()
 */

public class DefaultParserDeepseekTest {

    /**
     * Partition A: Core Functional Logic & State Transitions
     */
    
    @Test(timeout = 4000)
    public void testSimpleShortOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a"});
        
        assertTrue(cmd.hasOption("a"));
    }
    
    @Test(timeout = 4000)
    public void testSimpleLongOption() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"--alpha"});
        
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("alpha"));
    }
    
    @Test(timeout = 4000)
    public void testOptionWithArgument() throws Exception {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-o", "file.txt"});
        
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }
    
    @Test(timeout = 4000)
    public void testLongOptionWithEquals() throws Exception {
        Options options = new Options();
        options.addOption("o", "output", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"--output=file.txt"});
        
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }
    
    @Test(timeout = 4000)
    public void testShortOptionWithEquals() throws Exception {
        Options options = new Options();
        options.addOption("o", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-o=file.txt"});
        
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }
    
    @Test(timeout = 4000)
    public void testDoubleDashStopsParsing() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a", "--", "-b", "-c"});
        
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("-c", cmd.getArgs()[1]);
    }
    
    @Test(timeout = 4000)
    public void testRequiredOptions() throws Exception {
        Options options = new Options();
        options.addRequiredOption("r", "required", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-r", "value"});
        
        assertTrue(cmd.hasOption("r"));
        assertEquals("value", cmd.getOptionValue("r"));
    }
    
    @Test(timeout = 4000)
    public void testRequiredOptionsMissing() throws Exception {
        Options options = new Options();
        options.addRequiredOption("r", "required", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        
        try {
            parser.parse(options, new String[] {});
            fail("Expected MissingOptionException");
        } catch (MissingOptionException e) {
            assertNotNull(e.getMessage());
        }
    }
    
    /**
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     */
    
    @Test(timeout = 4000)
    public void testNullArguments() throws Exception {
        Options options = new Options();
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null);
        
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }
    
    @Test(timeout = 4000)
    public void testEmptyArguments() throws Exception {
        Options options = new Options();
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {});
        
        assertNotNull(cmd);
        assertEquals(0, cmd.getArgs().length);
    }
    
    @Test(timeout = 4000)
    public void testNullProperties() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a"}, (Properties) null);
        
        assertTrue(cmd.hasOption("a"));
    }
    
    @Test(timeout = 4000)
    public void testSingleDashToken() throws Exception {
        Options options = new Options();
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-"});
        
        assertEquals(1, cmd.getArgs().length);
        assertEquals("-", cmd.getArgs()[0]);
    }
    
    @Test(timeout = 4000)
    public void testNegativeNumberArgument() throws Exception {
        Options options = new Options();
        options.addOption("a", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a", "-123"});
        
        assertTrue(cmd.hasOption("a"));
        assertEquals("-123", cmd.getOptionValue("a"));
    }
    
    @Test(timeout = 4000)
    public void testStopAtNonOptionWithUnrecognizedOption() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a", "-b", "-c"}, true);
        
        assertTrue(cmd.hasOption("a"));
        assertEquals(2, cmd.getArgs().length);
        assertEquals("-b", cmd.getArgs()[0]);
        assertEquals("-c", cmd.getArgs()[1]);
    }
    
    @Test(timeout = 4000)
    public void testRequiredArgsMissing() throws Exception {
        Options options = new Options();
        options.addOption("a", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        
        try {
            parser.parse(options, new String[] {"-a"});
            fail("Expected MissingArgumentException");
        } catch (MissingArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }
    
    /**
     * Partition C: Defect-Targeted Branch Zone
     * Targeting the bug where properties processing with option groups causes
     * AlreadySelectedException and NullPointerException
     */
    
    @Test(timeout = 4000)
    public void testPropertyOptionGroup_CorrectBehavior() throws Exception {
        // This test targets the bug: When properties are used with option groups,
        // the parser should handle group selection correctly and not throw
        // AlreadySelectedException when options from different groups are processed
        Options options = new Options();
        
        OptionGroup group1 = new OptionGroup();
        group1.addOption(Option.builder("a").hasArg(true).build());
        group1.addOption(Option.builder("b").hasArg(true).build());
        options.addOptionGroup(group1);
        
        OptionGroup group2 = new OptionGroup();
        group2.addOption(Option.builder("c").hasArg(true).build());
        group2.addOption(Option.builder("d").hasArg(true).build());
        options.addOptionGroup(group2);
        
        Properties props = new Properties();
        props.setProperty("a", "value_a");
        props.setProperty("c", "value_c");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        // Both options from different groups should be present
        assertTrue("Option a should be present", cmd.hasOption("a"));
        assertTrue("Option c should be present", cmd.hasOption("c"));
        assertEquals("value_a", cmd.getOptionValue("a"));
        assertEquals("value_c", cmd.getOptionValue("c"));
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionUnexpected_NullSafety() throws Exception {
        // This test targets the NullPointerException bug when properties contain
        // option names that don't exist in the options
        Options options = new Options();
        options.addOption("a", "alpha", true, "desc");
        
        Properties props = new Properties();
        props.setProperty("a", "value_a");
        props.setProperty("unexpected_option", "some_value");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        // Only recognized option should be processed
        assertTrue("Option a should be present", cmd.hasOption("a"));
        assertEquals("value_a", cmd.getOptionValue("a"));
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionGroup_ConflictingSelections() throws Exception {
        // This test targets the scenario where two options from the SAME group
        // are specified via properties, which should throw AlreadySelectedException
        Options options = new Options();
        
        OptionGroup group = new OptionGroup();
        group.addOption(Option.builder("a").hasArg(true).build());
        group.addOption(Option.builder("b").hasArg(true).build());
        options.addOptionGroup(group);
        
        Properties props = new Properties();
        props.setProperty("a", "value_a");
        props.setProperty("b", "value_b");
        
        DefaultParser parser = new DefaultParser();
        
        try {
            parser.parse(options, null, props, false);
            fail("Expected AlreadySelectedException for conflicting options in same group");
        } catch (AlreadySelectedException e) {
            assertTrue(e.getMessage().contains("already been selected"));
        }
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionWithSingleValueFlag_no() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        
        Properties props = new Properties();
        props.setProperty("f", "no");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        // "no" should NOT add the option
        assertFalse("Option f should not be present with value 'no'", cmd.hasOption("f"));
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionWithSingleValueFlag_yes() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        
        Properties props = new Properties();
        props.setProperty("f", "yes");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        // "yes" should add the option
        assertTrue("Option f should be present with value 'yes'", cmd.hasOption("f"));
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionWithSingleValueFlag_true() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        
        Properties props = new Properties();
        props.setProperty("f", "true");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        assertTrue("Option f should be present with value 'true'", cmd.hasOption("f"));
    }
    
    @Test(timeout = 4000)
    public void testPropertyOptionWithSingleValueFlag_1() throws Exception {
        Options options = new Options();
        options.addOption("f", false, "flag");
        
        Properties props = new Properties();
        props.setProperty("f", "1");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, null, props, false);
        
        assertTrue("Option f should be present with value '1'", cmd.hasOption("f"));
    }
    
    /**
     * Partition D: Exception & Defensive Guard Paths
     */
    
    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedShortOptionThrowsException() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[] {"-b"});
    }
    
    @Test(timeout = 4000, expected = UnrecognizedOptionException.class)
    public void testUnrecognizedLongOptionThrowsException() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[] {"--beta"});
    }
    
    @Test(timeout = 4000, expected = AmbiguousOptionException.class)
    public void testAmbiguousOptionThrowsException() throws Exception {
        Options options = new Options();
        options.addOption("a", "alpha", false, "desc");
        options.addOption("b", "alphabeta", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        parser.parse(options, new String[] {"--alp"});
    }
    
    @Test(timeout = 4000)
    public void testOptionWithQuotedArgument() throws Exception {
        Options options = new Options();
        options.addOption("o", true, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-o", "\"file.txt\""});
        
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }
    
    /**
     * Partition E: Object Lifecycle & Contract Integrity
     */
    
    @Test(timeout = 4000)
    public void testConsecutiveParsingWithDifferentOptions() throws Exception {
        Options options1 = new Options();
        options1.addOption("a", false, "desc");
        
        Options options2 = new Options();
        options2.addOption("b", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        
        CommandLine cmd1 = parser.parse(options1, new String[] {"-a"});
        assertTrue(cmd1.hasOption("a"));
        
        CommandLine cmd2 = parser.parse(options2, new String[] {"-b"});
        assertTrue("Second parse should work independently", cmd2.hasOption("b"));
        assertFalse(cmd2.hasOption("a"));
    }
    
    @Test(timeout = 4000)
    public void testConcatenatedShortOptions() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", false, "desc");
        options.addOption("c", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-abc"});
        
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertTrue(cmd.hasOption("c"));
    }
    
    @Test(timeout = 4000)
    public void testConcatenatedShortOptionsWithArgument() throws Exception {
        Options options = new Options();
        options.addOption("a", false, "desc");
        Option optionB = Option.builder("b").hasArg(true).build();
        options.addOption(optionB);
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-abvalue"});
        
        assertTrue(cmd.hasOption("a"));
        assertTrue(cmd.hasOption("b"));
        assertEquals("value", cmd.getOptionValue("b"));
    }
    
    @Test(timeout = 4000)
    public void testLongOptionPrefixMatch() throws Exception {
        Options options = new Options();
        options.addOption("o", "output", true, "desc");
        options.addOption("o2", "output2", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"--output", "file.txt"});
        
        assertTrue(cmd.hasOption("o"));
        assertEquals("file.txt", cmd.getOptionValue("o"));
    }
    
    @Test(timeout = 4000)
    public void testJavaStylePropertyOption() throws Exception {
        Options options = new Options();
        Option dOption = Option.builder("D").numberOfArgs(2).valueSeparator('=').build();
        options.addOption(dOption);
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-Dkey=value"});
        
        assertTrue(cmd.hasOption("D"));
        assertEquals("value", cmd.getOptionValue("D"));
    }
    
    @Test(timeout = 4000)
    public void testShortLongOptionAmbiguityWithEquals() throws Exception {
        Options options = new Options();
        options.addOption("a", true, "desc");
        options.addOption("aa", false, "desc");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a=value"});
        
        assertTrue("Short option with = should work", cmd.hasOption("a"));
        assertEquals("value", cmd.getOptionValue("a"));
    }
    
    @Test(timeout = 4000)
    public void testPropertiesSkipAlreadyPresentOption() throws Exception {
        Options options = new Options();
        options.addOption("a", true, "desc");
        
        Properties props = new Properties();
        props.setProperty("a", "prop_value");
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a", "cmd_value"}, props, false);
        
        // Command-line value should take precedence over properties value
        assertTrue(cmd.hasOption("a"));
        assertEquals("cmd_value", cmd.getOptionValue("a"));
    }
    
    @Test(timeout = 4000)
    public void testRequiredOptionGroupWithCommandLineArg() throws Exception {
        Options options = new Options();
        
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(Option.builder("a").build());
        group.addOption(Option.builder("b").build());
        options.addOptionGroup(group);
        
        DefaultParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, new String[] {"-a"});
        
        assertTrue(cmd.hasOption("a"));
        assertFalse(cmd.hasOption("b"));
    }
}