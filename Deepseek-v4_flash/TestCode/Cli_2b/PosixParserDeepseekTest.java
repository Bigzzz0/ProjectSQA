package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Test basic option parsing with short options (-a, -b)
 *   - Test long option parsing (--longopt)
 *   - Test option with argument (-o value)
 *   - Test combined short options (-abc)
 *   - Test stopAtNonOption behavior
 *   - Test "--" token handling
 *   - Test "-" token handling
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty arguments array
 *   - Null arguments (though not directly testable, empty array)
 *   - Single character options
 *   - Options with '=' in long format
 *   - Token length boundaries (2 chars, >2 chars)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - BugCLI51: UnrecognizedOptionException for -o when option exists
 *   - The bug is in burstToken: when an option has an argument and there are remaining characters,
 *     the code adds the remaining characters as a token but doesn't properly handle the case
 *     where the option is recognized but the bursting continues incorrectly
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Unrecognized options with stopAtNonOption=true
 *   - Unrecognized options with stopAtNonOption=false
 *   - Options that require arguments but none provided
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple calls to flatten with different options
 *   - State reset between calls (init method)
 */
public class PosixParserDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testSimpleShortOption() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-a", "-b"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testLongOption() {
        Options options = new Options();
        options.addOption("long", true, "long option");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--long", "value"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--long", result[0]);
        assertEquals("value", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testLongOptionWithEquals() {
        Options options = new Options();
        options.addOption("long", true, "long option");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--long=value"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--long", result[0]);
        assertEquals("value", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testOptionWithArgument() {
        Options options = new Options();
        options.addOption("o", true, "output file");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-o", "file.txt"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-o", result[0]);
        assertEquals("file.txt", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testCombinedShortOptions() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        options.addOption("c", false, "charlie");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("-c", result[2]);
    }
    
    @Test(timeout = 4000)
    public void testStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-a", "nonOption", "-b"}, true);
        
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("nonOption", result[2]);
        assertEquals("-b", result[3]);
    }
    
    @Test(timeout = 4000)
    public void testDoubleHyphenToken() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--", "-a"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("-a", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testSingleHyphenToken() {
        Options options = new Options();
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-"}, false);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-", result[0]);
    }
    
    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testEmptyArguments() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{}, false);
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    @Test(timeout = 4000)
    public void testSingleCharacterOption() {
        Options options = new Options();
        options.addOption("x", false, "x-ray");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-x", result[0]);
    }
    
    @Test(timeout = 4000)
    public void testOptionWithMultipleArguments() {
        Options options = new Options();
        options.addOption("o", true, "output");
        options.addOption("v", false, "verbose");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-o", "file.txt", "-v"}, false);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-o", result[0]);
        assertEquals("file.txt", result[1]);
        assertEquals("-v", result[2]);
    }
    
    @Test(timeout = 4000)
    public void testNonOptionTokens() {
        Options options = new Options();
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"file.txt", "another.txt"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("file.txt", result[0]);
        assertEquals("another.txt", result[1]);
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * This test targets the known defect from BugCLI51.
     * The bug occurs when bursting a token like "-o" where 'o' is a valid option
     * that can take an argument. The burstToken method incorrectly handles the case
     * where the option has an argument and there are remaining characters in the token.
     * 
     * In the defective version, when processing "-o" and 'o' is a valid option with argument,
     * the code checks if (currentOption.hasArg() && (token.length() != (i + 1))).
     * For "-o", token.length() is 2, i starts at 1, so token.length() != (i + 1) is 2 != 2 = false,
     * so it doesn't add the remaining characters. But the bug is that when the option is recognized
     * and has an argument, the next token should be consumed as the argument value.
     * 
     * The actual bug is more subtle - it's about how the parser handles the case where
     * an option with argument is followed by another option or non-option token.
     */
    @Test(timeout = 4000)
    public void testBugCLI51() {
        Options options = new Options();
        options.addOption("o", true, "output file");
        options.addOption("v", false, "verbose");
        
        PosixParser parser = new PosixParser();
        // This should parse -o as an option with argument "value" and -v as verbose
        String[] result = parser.flatten(options, new String[]{"-o", "value", "-v"}, false);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-o", result[0]);
        assertEquals("value", result[1]);
        assertEquals("-v", result[2]);
    }
    
    @Test(timeout = 4000)
    public void testBurstTokenWithArgument() {
        Options options = new Options();
        options.addOption("o", true, "output file");
        options.addOption("v", false, "verbose");
        
        PosixParser parser = new PosixParser();
        // Test bursting with option that has argument - the remaining characters should be the argument
        String[] result = parser.flatten(options, new String[]{"-ovalue"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-o", result[0]);
        assertEquals("value", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testBurstTokenWithMultipleOptionsAndArgument() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("o", true, "output file");
        
        PosixParser parser = new PosixParser();
        // -ao should burst to -a and -o, but -o needs an argument
        // The remaining characters after 'o' should be the argument
        String[] result = parser.flatten(options, new String[]{"-aofile.txt"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-o", result[1]);
        // Note: The argument "file.txt" should be added but the current implementation
        // adds it as a separate token only if there are remaining characters after the option
        // In this case, after processing 'a' (i=1), then 'o' (i=2), token.length()=9, i+1=3
        // So it adds token.substring(3) = "file.txt"
    }
    
    @Test(timeout = 4000)
    public void testBurstTokenWithStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        // -bx where 'b' is not an option and stopAtNonOption is true
        String[] result = parser.flatten(options, new String[]{"-abx"}, true);
        
        assertNotNull(result);
        // After processing -a, 'b' is not an option, so stopAtNonOption triggers
        // process("bx") which adds "--" and "bx" and sets eatTheRest
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("bx", result[2]);
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testUnrecognizedOptionWithoutStop() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-x", result[0]);
    }
    
    @Test(timeout = 4000)
    public void testUnrecognizedOptionWithStop() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x"}, true);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("-x", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testUnrecognizedBurstTokenWithoutStop() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ax"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-x", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testUnrecognizedBurstTokenWithStop() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ax"}, true);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("x", result[2]);
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testMultipleFlattenCalls() {
        Options options1 = new Options();
        options1.addOption("a", false, "alpha");
        
        Options options2 = new Options();
        options2.addOption("b", false, "beta");
        
        PosixParser parser = new PosixParser();
        
        String[] result1 = parser.flatten(options1, new String[]{"-a"}, false);
        assertNotNull(result1);
        assertEquals(1, result1.length);
        assertEquals("-a", result1[0]);
        
        String[] result2 = parser.flatten(options2, new String[]{"-b"}, false);
        assertNotNull(result2);
        assertEquals(1, result2.length);
        assertEquals("-b", result2[0]);
    }
    
    @Test(timeout = 4000)
    public void testStateResetBetweenCalls() {
        Options options = new Options();
        options.addOption("o", true, "output");
        
        PosixParser parser = new PosixParser();
        
        // First call with option that has argument
        String[] result1 = parser.flatten(options, new String[]{"-o", "file1.txt"}, false);
        assertNotNull(result1);
        assertEquals(2, result1.length);
        
        // Second call should not be affected by first call's state
        String[] result2 = parser.flatten(options, new String[]{"-o", "file2.txt"}, false);
        assertNotNull(result2);
        assertEquals(2, result2.length);
        assertEquals("-o", result2[0]);
        assertEquals("file2.txt", result2[1]);
    }
    
    @Test(timeout = 4000)
    public void testGobbleAfterEatTheRest() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        // When stopAtNonOption is true and we encounter a non-option after an option
        String[] result = parser.flatten(options, new String[]{"-a", "nonOpt1", "nonOpt2"}, true);
        
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("nonOpt1", result[2]);
        assertEquals("nonOpt2", result[3]);
    }
    
    @Test(timeout = 4000)
    public void testProcessWithCurrentOption() {
        Options options = new Options();
        options.addOption("o", true, "output");
        
        PosixParser parser = new PosixParser();
        // This tests the process() method when currentOption is set and hasArg()
        // The flatten method calls process() for non-option tokens when stopAtNonOption is true
        // But we can also test it indirectly through the burstToken path
        String[] result = parser.flatten(options, new String[]{"-o", "value"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-o", result[0]);
        assertEquals("value", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testLongOptionWithoutValue() {
        Options options = new Options();
        options.addOption("long", false, "long option");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--long"}, false);
        
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("--long", result[0]);
    }
    
    @Test(timeout = 4000)
    public void testMultipleLongOptions() {
        Options options = new Options();
        options.addOption("alpha", false, "alpha");
        options.addOption("beta", false, "beta");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"--alpha", "--beta"}, false);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--alpha", result[0]);
        assertEquals("--beta", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testMixedOptionsAndNonOptions() {
        Options options = new Options();
        options.addOption("v", false, "verbose");
        options.addOption("o", true, "output");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-v", "-o", "file.txt", "arg1", "arg2"}, false);
        
        assertNotNull(result);
        assertEquals(5, result.length);
        assertEquals("-v", result[0]);
        assertEquals("-o", result[1]);
        assertEquals("file.txt", result[2]);
        assertEquals("arg1", result[3]);
        assertEquals("arg2", result[4]);
    }
    
    @Test(timeout = 4000)
    public void testBurstTokenWithLongToken() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        options.addOption("c", false, "charlie");
        
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abcdef"}, false);
        
        assertNotNull(result);
        // -a, -b, -c are recognized, 'd', 'e', 'f' are not
        assertEquals(6, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("-c", result[2]);
        assertEquals("-d", result[3]);
        assertEquals("-e", result[4]);
        assertEquals("-f", result[5]);
    }
    
    @Test(timeout = 4000)
    public void testBurstTokenWithStopAtNonOptionAndUnrecognized() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        
        PosixParser parser = new PosixParser();
        // -ab where 'b' is not an option and stopAtNonOption is true
        String[] result = parser.flatten(options, new String[]{"-ab"}, true);
        
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("b", result[2]);
    }
    
    @Test(timeout = 4000)
    public void testProcessWithNullCurrentOption() {
        Options options = new Options();
        
        PosixParser parser = new PosixParser();
        // When there's no current option and we encounter a non-option with stopAtNonOption=true
        String[] result = parser.flatten(options, new String[]{"arg"}, true);
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--", result[0]);
        assertEquals("arg", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testProcessWithCurrentOptionHasArgs() {
        Options options = new Options();
        options.addOption("o", true, "output");
        
        PosixParser parser = new PosixParser();
        // First set currentOption by processing -o
        String[] result = parser.flatten(options, new String[]{"-o", "val1", "val2"}, false);
        
        assertNotNull(result);
        // -o takes one argument, so val1 is consumed, val2 remains as non-option
        assertEquals(3, result.length);
        assertEquals("-o", result[0]);
        assertEquals("val1", result[1]);
        assertEquals("val2", result[2]);
    }
}