package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;

public class PosixParserDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: PosixParser (extends Parser)
     * 
     * Branches in flatten():
     * 1. token.startsWith("--") - true/false
     *    - token.indexOf('=') != -1 - true/false
     * 2. "-".equals(token) - true/false
     * 3. token.startsWith("-") - true/false
     *    - tokenLength == 2 - true/false
     *    - options.hasOption(token) - true/false
     *    - burstToken() called
     * 4. stopAtNonOption - true/false in non-option handling
     * 
     * Branches in burstToken():
     * - options.hasOption(ch) - true/false
     * - currentOption.hasArg() && (token.length() != (i+1)) - true/false
     * - stopAtNonOption - true/false
     * 
     * Branches in process():
     * - currentOption != null && currentOption.hasArg() - true/false
     * - currentOption.hasArg() - true/false (redundant but covered)
     * - currentOption.hasArgs() - true/false
     * 
     * Defect: testStopBursting - when stopAtNonOption is true and a
     * non-option is encountered during bursting, the remaining characters
     * should be added as a single token after "--", but the bug causes
     * extra arguments to be added.
     * 
     * Boundary values:
     * - Empty arguments array
     * - Single character token "-"
     * - Two-character option tokens
     * - Multi-character tokens requiring bursting
     * - Tokens with '=' for long options
     * - Null options (not expected but defensive)
     * - stopAtNonOption true/false combinations
     * - Options with arguments and multiple arguments
     */

    // Helper to create test options
    private Options createTestOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option with arg");
        options.addOption("c", false, "c option");
        options.addOption("d", "delta", true, "delta option with arg");
        options.addOption("e", "epsilon", false, "epsilon option");
        return options;
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testFlattenSimpleOptions() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "-b", "value", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("value", result[2]);
        assertEquals("-c", result[3]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionsWithEquals() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--alpha", "--beta=value", "--delta=test"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(5, result.length);
        assertEquals("--alpha", result[0]);
        assertEquals("--beta", result[1]);
        assertEquals("value", result[2]);
        assertEquals("--delta", result[3]);
        assertEquals("test", result[4]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphen() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-", "value"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleHyphen() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("-a", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstingWithArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -b expects an argument, so -bvalue should burst to -b value
        String[] args = {"-bvalue"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstingMultipleOptions() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -ac should burst to -a -c
        String[] args = {"-ac"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-c", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithStopAtNonOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "nonOption", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // When stopAtNonOption is true, non-option stops processing
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("nonOption", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithoutStopAtNonOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "nonOption", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("nonOption", result[1]);
        assertEquals("-c", result[2]);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenNullArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] result = parser.flatten(options, null, false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleCharacterToken() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenTwoCharacterOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-a", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenUnknownTwoCharacterOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-z"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        // Unknown option is ignored when not stopping
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenUnknownTwoCharacterOptionStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-z", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // When stopping, unknown option causes eatTheRest
        assertEquals(3, result.length);
        assertEquals("--", result[0]);
        assertEquals("-z", result[1]);
        assertEquals("rest", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEqualsAndEmptyValue() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--beta="};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--beta", result[0]);
        assertEquals("", result[1]);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Targets the known defect: testStopBursting
     * When stopAtNonOption is true and a non-option is encountered during
     * bursting, the remaining characters should be added as a single token
     * after "--", but the bug causes extra arguments to be added.
     */
    @Test(timeout = 4000)
    public void testStopBursting() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Token "-ab" where 'a' is a valid option but 'b' is not
        // With stopAtNonOption=true, should burst to "-a" then stop
        String[] args = {"-ab"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // Expected: "-a" is added, then "--" and "b" as remaining
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("b", result[2]);
    }

    @Test(timeout = 4000)
    public void testStopBurstingWithRemainingArgs() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Token "-ab" where 'a' is valid, 'b' is not, and more args follow
        String[] args = {"-ab", "extra1", "extra2"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // Expected: "-a", "--", "b", "extra1", "extra2"
        assertEquals(5, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("b", result[2]);
        assertEquals("extra1", result[3]);
        assertEquals("extra2", result[4]);
    }

    @Test(timeout = 4000)
    public void testStopBurstingWithOptionArgument() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Token "-bvalue" where 'b' expects an argument
        // With stopAtNonOption=true, should burst to "-b" "value"
        String[] args = {"-bvalue"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testStopBurstingWithInvalidCharAndStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Token "-az" where 'a' is valid, 'z' is not, stopAtNonOption=true
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // Expected: "-a", "--", "z"
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("z", result[2]);
    }

    @Test(timeout = 4000)
    public void testStopBurstingWithInvalidCharNoStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Token "-az" where 'a' is valid, 'z' is not, stopAtNonOption=false
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        // Expected: "-a" then the whole token is added
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-az", result[1]);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testFlattenWithNullOptions() {
        PosixParser parser = new PosixParser();
        String[] args = {"-a"};
        try {
            parser.flatten(null, args, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFlattenWithNullToken() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {null};
        try {
            parser.flatten(options, args, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testProcessWithCurrentOptionHasArg() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Use reflection to set currentOption
        try {
            java.lang.reflect.Field field = PosixParser.class.getDeclaredField("currentOption");
            field.setAccessible(true);
            field.set(parser, options.getOption("b"));
            
            String[] args = {"value"};
            String[] result = parser.flatten(options, args, false);
            assertNotNull(result);
            // Since currentOption has arg, value is added
            assertEquals(1, result.length);
            assertEquals("value", result[0]);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testProcessWithCurrentOptionHasArgs() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // Create an option with multiple args
        Option multiArgOption = new Option("m", "multi", true, "multi arg");
        multiArgOption.setArgs(3);
        options.addOption(multiArgOption);
        
        try {
            java.lang.reflect.Field field = PosixParser.class.getDeclaredField("currentOption");
            field.setAccessible(true);
            field.set(parser, multiArgOption);
            
            String[] args = {"value1", "value2"};
            String[] result = parser.flatten(options, args, false);
            assertNotNull(result);
            // Both values should be added
            assertEquals(2, result.length);
            assertEquals("value1", result[0]);
            assertEquals("value2", result[1]);
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testFlattenStateReset() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        
        // First call
        String[] args1 = {"-a", "-b", "value"};
        String[] result1 = parser.flatten(options, args1, false);
        assertEquals(3, result1.length);
        
        // Second call with different args should not retain state
        String[] args2 = {"-c"};
        String[] result2 = parser.flatten(options, args2, false);
        assertEquals(1, result2.length);
        assertEquals("-c", result2[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithEatTheRestState() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        
        // Trigger eatTheRest
        String[] args = {"-z", "a", "b", "c"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // When stopping at unknown option, all remaining are added
        assertEquals(4, result.length);
        assertEquals("--", result[0]);
        assertEquals("-z", result[1]);
        assertEquals("a", result[2]);
        assertEquals("b", result[3]);
        
        // Next call should not have eatTheRest set
        String[] args2 = {"-a"};
        String[] result2 = parser.flatten(options, args2, false);
        assertEquals(1, result2.length);
        assertEquals("-a", result2[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionHavingArgumentAndBurst() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -b expects an argument, so -bc should give -b and c
        String[] args = {"-bc"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("c", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleBursts() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -ac should burst to -a -c
        String[] args = {"-ac"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-c", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionNoEquals() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--alpha"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("--alpha", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionUnknown() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--unknown"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        // Unknown long option is added as-is
        assertEquals(1, result.length);
        assertEquals("--unknown", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMixedTokens() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "--beta=value", "-c", "plain", "-d", "arg"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(7, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--beta", result[1]);
        assertEquals("value", result[2]);
        assertEquals("-c", result[3]);
        assertEquals("plain", result[4]);
        assertEquals("-d", result[5]);
        assertEquals("arg", result[6]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithStopAtNonOptionAndBursting() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -ab where a is valid, b is not, stopAtNonOption=true
        String[] args = {"-ab", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // Expected: -a, --, b, rest
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("b", result[2]);
        assertEquals("rest", result[3]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithStopAtNonOptionAndNoBursting() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"plain", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        // First token is non-option, so stop immediately
        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("plain", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgumentAndStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -b expects arg, so -bvalue should be -b value
        String[] args = {"-bvalue", "rest"};
        String[] result = parser.flatten(options, args, true);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgumentAndNoStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-bvalue", "rest"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleOptionArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        // -b expects arg, so -b value1 value2
        String[] args = {"-b", "value1", "value2"};
        String[] result = parser.flatten(options, args, false);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value1", result[1]);
        assertEquals("value2", result[2]);
    }
}