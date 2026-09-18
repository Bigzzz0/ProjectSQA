package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: PosixParser.flatten(Options, String[], boolean) and burstToken(String, boolean)
 * 
 * Branches covered:
 * 1. token.startsWith("--") - long option handling
 *    - hasOption true/false, with/without '=' 
 * 2. "-".equals(token) - single dash
 * 3. token.startsWith("-") - short option handling
 *    - length==2, hasOption true, burst path
 * 4. stopAtNonOption true/false for non-option tokens
 * 5. burstToken loop: 
 *    - option exists with/without arg
 *    - option not exists with stopAtNonOption true/false
 *    - option has arg and remaining chars
 * 6. gobble() eatTheRest flag
 * 7. processNonOptionToken sets eatTheRest and adds "--"
 * 8. processOptionToken with stopAtNonOption and invalid option
 * 
 * Defect targeted (from ground truth):
 * - testGroovy: When a non-option token is encountered with stopAtNonOption=true,
 *   the parser incorrectly adds "--" before the token, causing expected "println 'hello'"
 *   to become "--" in output.
 * - testStopAtExpectedArg: Similar issue where "-b" is expected to be set to "foo"
 *   but the parser returns "--" instead.
 * 
 * The defect is in processNonOptionToken: it always adds "--" before the value,
 * but when called from burstToken (for invalid option char with stopAtNonOption),
 * it should NOT add the "--" prefix. The fix should only add "--" when called
 * from the main flatten loop, not from burstToken.
 */
public class PosixParserDeepseekTest {

    private PosixParser parser = new PosixParser();
    private Options options = new Options();

    // Helper to build options
    private Options buildOptions() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "alpha option");
        opts.addOption("b", "beta", true, "beta option with arg");
        opts.addOption("c", false, "c option");
        return opts;
    }

    // ==================== PARTITION A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testFlattenSimpleOption() {
        options = buildOptions();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenOptionWithArg() {
        options = buildOptions();
        String[] args = {"-b", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOption() {
        options = buildOptions();
        String[] args = {"--alpha"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithEquals() {
        options = buildOptions();
        String[] args = {"--beta=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleDash() {
        options = buildOptions();
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenNonOptionNoStop() {
        options = buildOptions();
        String[] args = {"foo", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-a"}, result);
    }

    // ==================== PARTITION B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testFlattenEmptyArgs() {
        options = buildOptions();
        String[] args = {};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenNullArgs() {
        options = buildOptions();
        String[] args = null;
        try {
            parser.flatten(options, args, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFlattenNullOptions() {
        String[] args = {"-a"};
        try {
            parser.flatten(null, args, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFlattenEmptyString() {
        options = buildOptions();
        String[] args = {""};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{""}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleCharOption() {
        options = buildOptions();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstWithArg() {
        options = buildOptions();
        String[] args = {"-bvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstMultipleOptions() {
        options = buildOptions();
        String[] args = {"-ac"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c"}, result);
    }

    // ==================== PARTITION C: Defect-Targeted Tests ====================

    /**
     * Defect test: testGroovy failure
     * When stopAtNonOption=true and a non-option token is encountered,
     * the parser should NOT add "--" before the token when called from burstToken.
     * Expected: "println 'hello'" should be preserved as-is.
     */
    @Test(timeout = 4000)
    public void testStopAtNonOptionWithBurst() {
        options = buildOptions();
        String[] args = {"-x", "println 'hello'"};
        String[] result = parser.flatten(options, args, true);
        // Expected: "-x" is not a valid option, so burstToken should add "-x" 
        // and then stopAtNonOption should add remaining tokens without "--"
        assertArrayEquals(new String[]{"-x", "println 'hello'"}, result);
    }

    /**
     * Defect test: testStopAtExpectedArg failure
     * When stopAtNonOption=true and a valid option with arg is followed by
     * a non-option, the parser should not add "--" incorrectly.
     */
    @Test(timeout = 4000)
    public void testStopAtExpectedArg() {
        options = buildOptions();
        String[] args = {"-b", "foo"};
        String[] result = parser.flatten(options, args, true);
        // Expected: "-b" is valid, "foo" is its argument, no "--" should be added
        assertArrayEquals(new String[]{"-b", "foo"}, result);
    }

    /**
     * Direct defect reproduction: testGroovy scenario
     * The bug causes "--" to be added before non-option tokens when 
     * stopAtNonOption=true and the token comes after an invalid option.
     */
    @Test(timeout = 4000)
    public void testGroovyScenario() {
        options = buildOptions();
        String[] args = {"-x", "println 'hello'"};
        String[] result = parser.flatten(options, args, true);
        // The bug would produce ["-x", "--", "println 'hello'"] 
        // but correct behavior is ["-x", "println 'hello'"]
        assertFalse("Should not contain '--' before non-option", 
                    java.util.Arrays.asList(result).contains("--"));
        assertEquals("println 'hello'", result[result.length - 1]);
    }

    /**
     * Test that specifically checks the "--" handling in burstToken
     * when stopAtNonOption is true and invalid option char is found.
     */
    @Test(timeout = 4000)
    public void testBurstTokenInvalidOptionStop() {
        options = buildOptions();
        String[] args = {"-zfoo"};
        String[] result = parser.flatten(options, args, true);
        // 'z' is not a valid option, stopAtNonOption=true
        // Should add "-zfoo" as-is without "--"
        assertArrayEquals(new String[]{"-zfoo"}, result);
    }

    @Test(timeout = 4000)
    public void testBurstTokenInvalidOptionNoStop() {
        options = buildOptions();
        String[] args = {"-zfoo"};
        String[] result = parser.flatten(options, args, false);
        // 'z' is not valid, stopAtNonOption=false
        // Should add "-zfoo" as-is
        assertArrayEquals(new String[]{"-zfoo"}, result);
    }

    // ==================== PARTITION D: Exception & Defensive Paths ====================

    @Test(timeout = 4000)
    public void testFlattenWithNullElement() {
        options = buildOptions();
        String[] args = {"-a", null, "-b"};
        String[] result = parser.flatten(options, args, false);
        // Null element should be treated as non-option
        assertArrayEquals(new String[]{"-a", null, "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithEmptyOption() {
        options = buildOptions();
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOnlyDashes() {
        options = buildOptions();
        String[] args = {"---"};
        String[] result = parser.flatten(options, args, false);
        // "---" starts with "-", length > 2, not valid option
        // burstToken will process chars after first dash
        assertArrayEquals(new String[]{"---"}, result);
    }

    // ==================== PARTITION E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testFlattenMultipleCalls() {
        options = buildOptions();
        String[] args1 = {"-a"};
        String[] args2 = {"-b", "value"};
        
        String[] result1 = parser.flatten(options, args1, false);
        assertArrayEquals(new String[]{"-a"}, result1);
        
        // Second call should not retain state from first
        String[] result2 = parser.flatten(options, args2, false);
        assertArrayEquals(new String[]{"-b", "value"}, result2);
    }

    @Test(timeout = 4000)
    public void testFlattenWithStopAtNonOptionAndValidOption() {
        options = buildOptions();
        String[] args = {"-a", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "-a" is valid, "foo" is non-option, stopAtNonOption=true
        // Should add "--" before "foo"
        assertArrayEquals(new String[]{"-a", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithStopAtNonOptionAndOptionAfterNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        // "foo" is non-option, stopAtNonOption=true
        // Should add "--" and then all remaining tokens as-is
        assertArrayEquals(new String[]{"--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAndStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "--alpha" is valid, "foo" is non-option
        assertArrayEquals(new String[]{"--alpha", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAndStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "--beta=value" is valid, "foo" is non-option
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "-ac" bursts to "-a", "-c", then "foo" is non-option
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgAndStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "-bvalue" bursts to "-b", "value", then "foo" is non-option
        assertArrayEquals(new String[]{"-b", "value", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAndStop() {
        options = buildOptions();
        String[] args = {"-z", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "-z" is invalid, stopAtNonOption=true
        // Should add "-z" then stop and add "foo" without "--"
        assertArrayEquals(new String[]{"-z", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAndNoStop() {
        options = buildOptions();
        String[] args = {"-z", "foo"};
        String[] result = parser.flatten(options, args, false);
        // "-z" is invalid, stopAtNonOption=false
        // Should add "-z" then "foo" as-is
        assertArrayEquals(new String[]{"-z", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleNonOptions() {
        options = buildOptions();
        String[] args = {"foo", "bar", "baz"};
        String[] result = parser.flatten(options, args, true);
        // All non-options, stopAtNonOption=true
        assertArrayEquals(new String[]{"--", "foo", "bar", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMixedOptionsAndNonOptions() {
        options = buildOptions();
        String[] args = {"-a", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        // "-a" valid, "foo" non-option, stopAtNonOption=true
        // After "foo", everything else is added as-is
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgAndNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "-b" valid with arg "value", then "foo" is non-option
        assertArrayEquals(new String[]{"-b", "value", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgAndMoreOptions() {
        options = buildOptions();
        String[] args = {"-b", "value", "-a"};
        String[] result = parser.flatten(options, args, false);
        // "-b" valid with arg "value", "-a" valid
        assertArrayEquals(new String[]{"-b", "value", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndArgThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "-a"};
        String[] result = parser.flatten(options, args, false);
        // "-bvalue" bursts to "-b", "value", then "-a" valid
        assertArrayEquals(new String[]{"-b", "value", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndInvalidChar() {
        options = buildOptions();
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, false);
        // "-az": 'a' valid, 'z' invalid, stopAtNonOption=false
        // Should add "-a" then "-z" (since 'z' is invalid and no stop)
        assertArrayEquals(new String[]{"-a", "-z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndInvalidCharStop() {
        options = buildOptions();
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, true);
        // "-az": 'a' valid, 'z' invalid, stopAtNonOption=true
        // Should add "-a" then stop and add "z" as-is
        assertArrayEquals(new String[]{"-a", "z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndArgThenInvalid() {
        options = buildOptions();
        String[] args = {"-bz"};
        String[] result = parser.flatten(options, args, false);
        // "-bz": 'b' valid with arg, so "z" becomes arg
        assertArrayEquals(new String[]{"-b", "z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndArgThenInvalidStop() {
        options = buildOptions();
        String[] args = {"-bz"};
        String[] result = parser.flatten(options, args, true);
        // "-bz": 'b' valid with arg, so "z" becomes arg
        assertArrayEquals(new String[]{"-b", "z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAndInvalid() {
        options = buildOptions();
        String[] args = {"--invalid", "foo"};
        String[] result = parser.flatten(options, args, false);
        // "--invalid" not a valid option, stopAtNonOption=false
        // Should add "--invalid" as-is
        assertArrayEquals(new String[]{"--invalid", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAndInvalidStop() {
        options = buildOptions();
        String[] args = {"--invalid", "foo"};
        String[] result = parser.flatten(options, args, true);
        // "--invalid" not valid, stopAtNonOption=true
        // Should add "--" then "--invalid" then "foo"
        assertArrayEquals(new String[]{"--", "--invalid", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAndInvalid() {
        options = buildOptions();
        String[] args = {"--invalid=value"};
        String[] result = parser.flatten(options, args, false);
        // "--invalid=value" not valid, stopAtNonOption=false
        // Should add whole token as-is
        assertArrayEquals(new String[]{"--invalid=value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAndInvalidStop() {
        options = buildOptions();
        String[] args = {"--invalid=value"};
        String[] result = parser.flatten(options, args, true);
        // "--invalid=value" not valid, stopAtNonOption=true
        // Should add "--" then whole token
        assertArrayEquals(new String[]{"--", "--invalid=value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionAndDoubleDash() {
        options = buildOptions();
        String[] args = {"-a", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        // "-a" valid, "--" special, then "-b" after "--" is non-option
        assertArrayEquals(new String[]{"-a", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        // "-a" valid, "--" special, then "-b" after "--" is non-option
        assertArrayEquals(new String[]{"-a", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOnlyDoubleDash() {
        options = buildOptions();
        String[] args = {"--"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOnlyDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        // "foo" non-option, then "--" special, then "-a" after "--"
        assertArrayEquals(new String[]{"foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        // "foo" non-option, stopAtNonOption=true, so add "--" then "foo"
        // then "--" is special, then "-a" after "--"
        assertArrayEquals(new String[]{"--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgAndDoubleDash() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndDoubleDash() {
        options = buildOptions();
        String[] args = {"-ac", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgAndDoubleDash() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAndDoubleDash() {
        options = buildOptions();
        String[] args = {"-z", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAndDoubleDash() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAndDoubleDash() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAndDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAndOptionAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAndOptionAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleDoubleDashes() {
        options = buildOptions();
        String[] args = {"--", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleDoubleDashesStop() {
        options = buildOptions();
        String[] args = {"--", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "-ac"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-ac"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "-ac"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "-ac"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "-bvalue"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-bvalue"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "-bvalue"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "-bvalue"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "--alpha"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "--alpha"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "--alpha"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--alpha"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "--beta=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "--beta=value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "--beta=value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "--beta=value"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "-z"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "-z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "-z"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "-z"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAfterDoubleDash() {
        options = buildOptions();
        String[] args = {"--", "foo"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAfterDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--", "foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDash() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-b", "value", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-b", "value", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-ac", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-ac", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-bvalue", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-bvalue", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-z", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-z", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--alpha", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--alpha", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--beta=value", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--beta=value", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-b", "value", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-b", "value", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-ac", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-ac", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-bvalue", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-bvalue", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-z", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-z", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--alpha", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--alpha", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--beta=value", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--beta=value", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a", "bar"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-b", "value", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-b", "value", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-ac", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-ac", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-bvalue", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-bvalue", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-z", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-z", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--alpha", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--alpha", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--beta=value", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--beta=value", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a", "bar", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-b", "value", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-b", "value", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-ac", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-ac", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-bvalue", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-bvalue", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-z", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-z", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--alpha", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--alpha", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--beta=value", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--beta=value", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c", "baz"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a", "bar", "-c", "baz"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-a", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-a", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-a", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-b", "value", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenOptionArgThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-b", "value", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-b", "value", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-ac", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-ac", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-ac", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-bvalue", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenBurstArgThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-bvalue", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-bvalue", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "-z", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenInvalidOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "-z", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "-z", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--alpha", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--alpha", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--alpha", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--", "--beta=value", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenDoubleDashThenLongOptionEqualsThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--", "--beta=value", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--", "--beta=value", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "foo", "--", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "--", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenNonOptionThenDoubleDashThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "foo", "--", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "--", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-ac", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-a", "-c", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-bvalue", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-b", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "-z", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--alpha", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOption() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionStop() {
        options = buildOptions();
        String[] args = {"foo", "--beta=value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--", "foo", "--beta", "value", "--", "bar", "-a", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-ac", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-a", "-c", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithBurstArgThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-bvalue", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-b", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithInvalidOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"-z", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--alpha", "--", "foo", "-b", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithLongOptionEqualsThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionStop() {
        options = buildOptions();
        String[] args = {"--beta=value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(new String[]{"--beta", "value", "--", "foo", "-a", "bar", "-c", "baz", "-d", "qux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption() {
        options = buildOptions();
        String[] args = {"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f", "quux"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(new String[]{"foo", "-a", "--", "bar", "-b", "baz", "-c", "qux", "-d", "quux", "-e", "quux", "-f", "quux"}, result);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionThenOptionThenDoubleDashThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOptionThenOptionThenNonOption