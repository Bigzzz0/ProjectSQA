package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: PosixParser.flatten() and burstToken() logic.
 * 
 * Branches covered:
 * - flatten(): 
 *   - token.startsWith("--") true/false
 *   - token.indexOf('=') != -1 true/false (for "--" tokens)
 *   - "-".equals(token) true/false
 *   - token.startsWith("-") true/false
 *   - token.length() == 2 true/false (for short options)
 *   - options.hasOption(token) true/false (for long options)
 *   - stopAtNonOption true/false (for non-option tokens)
 *   - gobble() eatTheRest true/false
 * - processOptionToken():
 *   - options.hasOption(token) true/false
 *   - stopAtNonOption true/false
 * - burstToken():
 *   - options.hasOption(ch) true/false
 *   - currentOption.hasArg() true/false
 *   - token.length() != (i+1) true/false
 *   - stopAtNonOption true/false
 * - process():
 *   - currentOption != null && currentOption.hasArg() true/false
 *   - currentOption.hasArgs() true/false
 * 
 * Defect targeted: testUnrecognizedOption2 - when an unrecognized option
 * with a value is encountered and stopAtNonOption is false, the parser
 * should throw UnrecognizedOptionException. The bug is that the parser
 * incorrectly processes the token instead of throwing.
 * 
 * Boundary values:
 * - Empty arguments array
 * - Single character tokens
 * - Tokens with "=" (long options with values)
 * - Tokens with multiple short options
 * - Tokens with option arguments
 * - stopAtNonOption = true/false combinations
 * - Null/empty option values
 */
public class PosixParserDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testFlattenSimpleOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha option");
        options.addOption("b", "beta", true, "beta option");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", "-b", "value", "nonOption"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("value", result[2]);
        assertEquals("nonOption", result[3]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionsWithEquals() {
        Options options = new Options();
        options.addOption("l", "long", true, "long option");

        PosixParser parser = new PosixParser();
        String[] args = {"--long=value"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("--long", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenDoubleDash() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"--", "-a", "value"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("--", result[0]);
        assertEquals("-a", result[1]);
        assertEquals("value", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleDash() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {"-"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenBurstShortOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("b", "beta", false, "beta");
        options.addOption("c", "gamma", true, "gamma with arg");

        PosixParser parser = new PosixParser();
        String[] args = {"-abcvalue"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("-c", result[2]);
        assertEquals("value", result[3]);
    }

    @Test(timeout = 4000)
    public void testFlattenStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", "nonOption", "-b"};
        String[] result = parser.flatten(options, args, true);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("nonOption", result[1]);
        assertEquals("-b", result[2]);
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testFlattenEmptyArguments() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] args = {};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenNullArguments() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, null, false);

        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testFlattenSingleCharacterToken() {
        Options options = new Options();
        options.addOption("x", "xyz", false, "x option");

        PosixParser parser = new PosixParser();
        String[] args = {"-x"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-x", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenTokenWithOnlyDashAndChar() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-a"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-a", result[0]);
    }

    @Test(timeout = 4000)
    public void testFlattenLongOptionWithoutValue() {
        Options options = new Options();
        options.addOption("l", "long", false, "long option");

        PosixParser parser = new PosixParser();
        String[] args = {"--long"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("--long", result[0]);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect test: testUnrecognizedOption2
     * When an unrecognized option with a value is encountered and stopAtNonOption is false,
     * the parser should throw UnrecognizedOptionException.
     * The bug is that the parser incorrectly processes the token instead of throwing.
     */
    @Test(expected = UnrecognizedOptionException.class, timeout = 4000)
    public void testUnrecognizedOptionWithValue() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-z", "value"};
        parser.flatten(options, args, false);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedOptionWithStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-z", "value"};
        String[] result = parser.flatten(options, args, true);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-z", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedBurstOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-az"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("-az", result[0]);
    }

    @Test(timeout = 4000)
    public void testUnrecognizedBurstOptionStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-az", "rest"};
        String[] result = parser.flatten(options, args, true);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("z", result[2]);
        assertEquals("rest", result[3]);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testFlattenWithOptionHavingArg() {
        Options options = new Options();
        options.addOption("o", "output", true, "output file");

        PosixParser parser = new PosixParser();
        String[] args = {"-o", "file.txt"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-o", result[0]);
        assertEquals("file.txt", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionHavingArgs() {
        Options options = new Options();
        Option opt = new Option("m", "multi", true, "multi arg");
        opt.setArgs(2);
        options.addOption(opt);

        PosixParser parser = new PosixParser();
        String[] args = {"-m", "val1", "val2"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals("-m", result[0]);
        assertEquals("val1", result[1]);
        assertEquals("val2", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionArgInBurst() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("b", "beta", true, "beta with arg");

        PosixParser parser = new PosixParser();
        String[] args = {"-abvalue"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("value", result[2]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAndStopAtNonOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"nonOption", "-a"};
        String[] result = parser.flatten(options, args, true);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("nonOption", result[0]);
        assertEquals("-a", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNonOptionAndNoStop() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"nonOption", "-a"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("nonOption", result[0]);
        assertEquals("-a", result[1]);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testFlattenRepeatedCalls() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args1 = {"-a"};
        String[] result1 = parser.flatten(options, args1, false);
        assertEquals(1, result1.length);
        assertEquals("-a", result1[0]);

        String[] args2 = {"-a", "extra"};
        String[] result2 = parser.flatten(options, args2, false);
        assertEquals(2, result2.length);
        assertEquals("-a", result2[0]);
        assertEquals("extra", result2[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithNullOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", null};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertNull(result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithEmptyStringOption() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", ""};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithOptionThatHasNoArgButValueProvided() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", "value"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("value", result[1]);
    }

    @Test(timeout = 4000)
    public void testFlattenWithMultipleOptionsAndValues() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha");
        options.addOption("b", "beta", true, "beta with arg");
        options.addOption("c", "gamma", false, "gamma");

        PosixParser parser = new PosixParser();
        String[] args = {"-a", "-b", "val", "-c"};
        String[] result = parser.flatten(options, args, false);

        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("val", result[2]);
        assertEquals("-c", result[3]);
    }
}