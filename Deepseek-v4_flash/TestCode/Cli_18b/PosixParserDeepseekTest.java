package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.cli.PosixParser
 * 
 * Known Defect (from Defects4J): testStop2 - "Confirm -a is not set"
 * This defect occurs when stopAtNonOption=true and a token like "-a" is encountered
 * where 'a' is NOT a valid option. The buggy behavior incorrectly sets eatTheRest=true
 * and adds "--" + token, causing the option to be treated as a non-option and
 * potentially setting it incorrectly. The correct behavior should be that when
 * stopAtNonOption=true and the token is not a valid option, the token should be
 * added as-is (without "--" prefix) and processing should stop.
 * 
 * Branch Coverage Targets:
 * 1. flatten() - token.startsWith("--") true/false
 * 2. flatten() - token.indexOf('=') != -1 true/false
 * 3. flatten() - "-".equals(token) true/false
 * 4. flatten() - token.startsWith("-") true/false
 * 5. flatten() - token.length() == 2 true/false
 * 6. flatten() - options.hasOption(token) true/false
 * 7. flatten() - stopAtNonOption true/false
 * 8. processOptionToken() - options.hasOption(token) true/false
 * 9. processOptionToken() - stopAtNonOption true/false
 * 10. burstToken() - options.hasOption(ch) true/false
 * 11. burstToken() - currentOption.hasArg() true/false
 * 12. burstToken() - token.length() != (i+1) true/false
 * 13. burstToken() - stopAtNonOption true/false
 * 14. process() - currentOption != null && currentOption.hasArg() true/false
 * 15. process() - currentOption.hasArgs() true/false
 * 16. gobble() - eatTheRest true/false
 * 
 * Boundary Values:
 * - null arguments array
 * - empty arguments array
 * - single character tokens
 * - multi-character tokens
 * - tokens with '=' sign
 * - tokens with option arguments
 * - stopAtNonOption = true/false combinations
 */
public class PosixParserDeepseekTest {

    // Helper method to create test options
    private Options createTestOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        options.addOption("b", "beta", true, "Beta option with arg");
        options.addOption("c", "gamma", false, "Gamma option");
        return options;
    }

    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testFlattenSimpleOptions() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "-b", "value", "-c"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 4 tokens", 4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("value", result[2]);
        assertEquals("-c", result[3]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithDoubleDash() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "--", "-b"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 tokens", 3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("-b", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithEqualsSign() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--alpha=value"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("--alpha", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleHyphen() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-", "value"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("-", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstTokenWithArg() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-bvalue"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstTokenMultipleOptions() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-ac"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-c", result[1]);
    }

    // ==================== PARTITION B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testFlattenNullArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        
        String[] result = parser.flatten(options, null, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 0 tokens", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 0 tokens", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleCharacterToken() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("-a", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEquals() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"--alpha=test=value"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("--alpha", result[0]);
        assertEquals("test=value", result[1]);
    }

    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================

    /**
     * Defect Test: testStop2 - "Confirm -a is not set"
     * 
     * This test targets the specific defect where stopAtNonOption=true and a token
     * like "-a" is encountered where 'a' is NOT a valid option. The buggy behavior
     * incorrectly sets eatTheRest=true and adds "--" + token, causing the option
     * to be treated as a non-option and potentially setting it incorrectly.
     * 
     * Expected correct behavior: When stopAtNonOption=true and the token is not a
     * valid option, the token should be added as-is (without "--" prefix) and
     * processing should stop.
     */
    @Test(timeout = 4000)
    public void testStop2_DefectTarget() {
        PosixParser parser = new PosixParser();
        Options options = new Options();
        // Only define option 'b' - 'a' is NOT a valid option
        options.addOption("b", "beta", false, "Beta option");
        
        String[] args = {"-a", "-b"};
        
        String[] result = parser.flatten(options, args, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("Token should be -a as-is", "-a", result[0]);
        // The buggy version would add "--" and "-a" resulting in 2 tokens
        // and would incorrectly set eatTheRest=true
        assertFalse("Should not contain -- token", containsToken(result, "--"));
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithValidOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "value"};
        
        String[] result = parser.flatten(options, args, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testStopAtNonOptionWithInvalidOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-x", "-a"};
        
        String[] result = parser.flatten(options, args, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("-x", result[0]);
    }

    @Test(timeout = 4000)
    public void testBurstTokenWithStopAtNonOption() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-ax"};
        
        String[] result = parser.flatten(options, args, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("-a", result[0]);
    }

    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testBurstTokenWithUnknownOptionAndStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-xz"};
        
        String[] result = parser.flatten(options, args, true);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("-xz", result[0]);
    }

    @Test(timeout = 4000)
    public void testBurstTokenWithUnknownOptionNoStop() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-xz"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 1 token", 1, result.length);
        assertEquals("-xz", result[0]);
    }

    @Test(timeout = 4000)
    public void testProcessWithCurrentOptionHasArg() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-b", "value"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 2 tokens", 2, result.length);
        assertEquals("-b", result[0]);
        assertEquals("value", result[1]);
    }

    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testFlattenReusability() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args1 = {"-a"};
        String[] args2 = {"-b", "value"};
        
        String[] result1 = parser.flatten(options, args1, false);
        String[] result2 = parser.flatten(options, args2, false);
        
        assertNotNull("First result should not be null", result1);
        assertNotNull("Second result should not be null", result2);
        assertEquals("First result should have 1 token", 1, result1.length);
        assertEquals("Second result should have 2 tokens", 2, result2.length);
        assertEquals("-a", result1[0]);
        assertEquals("-b", result2[0]);
        assertEquals("value", result2[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMixedTokens() {
        PosixParser parser = new PosixParser();
        Options options = createTestOptions();
        String[] args = {"-a", "plain", "-b", "value", "--", "--alpha"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 6 tokens", 6, result.length);
        assertEquals("-a", result[0]);
        assertEquals("plain", result[1]);
        assertEquals("-b", result[2]);
        assertEquals("value", result[3]);
        assertEquals("--", result[4]);
        assertEquals("--alpha", result[5]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionHavingArgs() {
        Options options = new Options();
        options.addOption("a", "alpha", true, "Alpha with arg");
        PosixParser parser = new PosixParser();
        String[] args = {"-a", "value1", "value2"};
        
        String[] result = parser.flatten(options, args, false);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Should have 3 tokens", 3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("value1", result[1]);
        assertEquals("value2", result[2]);
    }

    // Helper method to check if a token exists in the result
    private boolean containsToken(String[] tokens, String token) {
        for (String t : tokens) {
            if (token.equals(t)) {
                return true;
            }
        }
        return false;
    }
}