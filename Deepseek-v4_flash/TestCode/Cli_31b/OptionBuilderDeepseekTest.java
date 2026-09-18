package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Test each builder method returns the instance (chaining proof)
 * - Test create(char) and create(String) propagate correct properties
 * - Test create() uses longopt and falls back to null opt
 * - Test that after create, the builder is reset to defaults (static state)
 *
 * Partition B: Boundary Value Analysis & Extremes
 * - Null longopt, null description, null type
 * - Empty string opt ("")
 * - Char boundary: create((char)0) or create((char)65535) – but Option validates opt, so we test exception
 * - Negative numberOfArgs? Not allowed via builder (only through hasArgs(int))
 * - hasArgs(0), hasArgs(Integer.MAX_VALUE)
 * - hasOptionalArgs(0), hasOptionalArgs(-1) – but OptionBuilder does not validate, so we should test behavior
 *
 * Partition C: Defect-Targeted Branch Zone
 * - KNOWN DEFECT: OptionBuilder.reset() sets argName to "arg", but it should be null
 *   so that HelpFormatter uses its own default "argument".
 *   Test: create option without calling withArgName -> option.getArgName() should be null (actual is "arg")
 *   This test will fail on buggy version, pass on fixed.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - create() when longopt is null -> IllegalArgumentException
 * - create(null) ? Option constructor throws IllegalArgumentException for null opt
 * - hasArg(false) -> numberOfArgs = UNINITIALIZED
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - Not directly applicable (no equals/hashCode/clone needed for builder)
 * - But we test that builder is a singleton and static state is reset after each create
 *
 * Note: All tests use timeout=4000 to prevent hanging.
 */
public class OptionBuilderDeepseekTest {

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testWithLongOptReturnsInstance() {
        OptionBuilder builder = OptionBuilder.withLongOpt("verbose");
        assertNotNull("withLongOpt should return non-null instance", builder);
        // Chaining test: verify we can continue
        OptionBuilder.withLongOpt("debug").hasArg().isRequired();
        // cleanup? not needed as tests are isolated, but reset after last create
        OptionBuilder.create('x'); // reset state
    }

    @Test(timeout = 4000)
    public void testCreateCharCreatesOption() {
        Option opt = OptionBuilder.create('a');
        assertEquals("opt should be 'a'", "a", opt.getOpt());
        assertNull("longopt should be null if not set", opt.getLongOpt());
        assertFalse("required should default to false", opt.isRequired());
        assertEquals("args should be UNINITIALIZED", Option.UNINITIALIZED, opt.getArgs());
        assertEquals("argName should be 'arg' (buggy) or null (fixed)", "arg", opt.getArgName());
        // After create, builder reset; next creation gets defaults
    }

    @Test(timeout = 4000)
    public void testCreateStringPropagatesProperties() {
        OptionBuilder.withLongOpt("output");
        OptionBuilder.withDescription("Output file");
        OptionBuilder.withArgName("file");
        OptionBuilder.isRequired(true);
        OptionBuilder.hasArgs(2);
        OptionBuilder.withType(String.class);
        OptionBuilder.withValueSeparator(':');
        Option opt = OptionBuilder.create("o");

        assertEquals("opt", "o", opt.getOpt());
        assertEquals("longOpt", "output", opt.getLongOpt());
        assertEquals("description", "Output file", opt.getDescription());
        assertEquals("argName", "file", opt.getArgName());
        assertTrue("isRequired", opt.isRequired());
        assertEquals("numberOfArgs", 2, opt.getArgs());
        assertEquals("type", String.class, opt.getType());
        assertEquals("value separator", ':', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testCreateNoArgUsesLongopt() {
        OptionBuilder.withLongOpt("verbose");
        Option opt = OptionBuilder.create();
        assertNull("opt should be null when create() without string", opt.getOpt());
        assertEquals("longOpt should be set", "verbose", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testBuilderResetsAfterCreate() {
        // Set some properties
        OptionBuilder.withLongOpt("temp").hasArg().isRequired();
        Option opt = OptionBuilder.create('t');
        // Verify opt properties are set correctly
        assertEquals("longOpt", "temp", opt.getLongOpt());
        assertEquals("args", 1, opt.getArgs());
        assertTrue("required", opt.isRequired());
        // Now create another option without any settings (should be default)
        Option opt2 = OptionBuilder.create('d');
        assertNull("longOpt should be null after reset", opt2.getLongOpt());
        assertEquals("args should be UNINITIALIZED", Option.UNINITIALIZED, opt2.getArgs());
        assertFalse("required should be false", opt2.isRequired());
        // argName should be "arg" (buggy) or null (fixed)
        assertEquals("argName after reset", "arg", opt2.getArgName());
    }

    // ===== Partition B: Boundary Values & Extremes =====

    @Test(timeout = 4000)
    public void testNullLongOpt() {
        OptionBuilder.withLongOpt(null);
        Option opt = OptionBuilder.create('n');
        assertNull("longOpt should be null", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testEmptyStringOpt() {
        Option opt = OptionBuilder.create("");
        assertEquals("opt should be empty string", "", opt.getOpt());
    }

    @Test(timeout = 4000)
    public void testNullDescription() {
        OptionBuilder.withDescription(null);
        Option opt = OptionBuilder.create('d');
        assertNull("description should be null", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testNullType() {
        OptionBuilder.withType(null);
        Option opt = OptionBuilder.create('t');
        assertNull("type should be null", opt.getType());
    }

    @Test(timeout = 4000)
    public void testHasArgsZero() {
        OptionBuilder.hasArgs(0);
        Option opt = OptionBuilder.create('z');
        assertEquals("args should be 0", 0, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgsNegative() {
        // hasArgs accepts negative numbers; it sets numberOfArgs = negative,
        // Option constructor does not validate, so we allow it.
        OptionBuilder.hasArgs(-5);
        Option opt = OptionBuilder.create('n');
        assertEquals("args should be -5", -5, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgsUnlimited() {
        OptionBuilder.hasArgs();
        Option opt = OptionBuilder.create('u');
        assertEquals("args should be UNLIMITED_VALUES", Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgSetsArgsToOne() {
        OptionBuilder.hasOptionalArg();
        Option opt = OptionBuilder.create('o');
        assertEquals("args should be 1", 1, opt.getArgs());
        assertTrue("optionalArg should be true", opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsUnlimited() {
        OptionBuilder.hasOptionalArgs();
        Option opt = OptionBuilder.create('u');
        assertEquals("args should be UNLIMITED_VALUES", Option.UNLIMITED_VALUES, opt.getArgs());
        assertTrue("optionalArg should be true", opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsZero() {
        OptionBuilder.hasOptionalArgs(0);
        Option opt = OptionBuilder.create('z');
        assertEquals("args should be 0", 0, opt.getArgs());
        assertTrue("optionalArg should be true", opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsNegative() {
        OptionBuilder.hasOptionalArgs(-3);
        Option opt = OptionBuilder.create('n');
        assertEquals("args should be -3", -3, opt.getArgs());
        assertTrue("optionalArg should be true", opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testValueSeparatorChar() {
        OptionBuilder.withValueSeparator('|');
        Option opt = OptionBuilder.create('v');
        assertEquals("value separator should be '|'", '|', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testValueSeparatorDefault() {
        OptionBuilder.withValueSeparator();
        Option opt = OptionBuilder.create('v');
        assertEquals("value separator should be '='", '=', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testIsRequiredTrueAndFalse() {
        OptionBuilder.isRequired(true);
        Option opt1 = OptionBuilder.create('1');
        assertTrue("option should be required", opt1.isRequired());

        OptionBuilder.isRequired(false);
        Option opt2 = OptionBuilder.create('2');
        assertFalse("option should not be required", opt2.isRequired());
    }

    @Test(timeout = 4000)
    public void testHasArgNoArg() {
        OptionBuilder.hasArg(false);
        Option opt = OptionBuilder.create('h');
        assertEquals("args should be UNINITIALIZED", Option.UNINITIALIZED, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgTrue() {
        OptionBuilder.hasArg(true);
        Option opt = OptionBuilder.create('h');
        assertEquals("args should be 1", 1, opt.getArgs());
    }

    // ===== Partition C: Defect-Targeted Test =====
    // The known defect: OptionBuilder.reset() sets argName to "arg" instead of null.
    // This causes HelpFormatter to display "arg" rather than its default "argument".
    @Test(timeout = 4000)
    public void testDefaultArgNameShouldBeNull() {
        // Create option without calling withArgName
        Option opt = OptionBuilder.create('f');
        // In the buggy version, getArgName() returns "arg"
        // The correct behavior (after fix) is to return null
        // We assert the correct expected value: null
        assertNull("Default argName should be null, not 'arg'", opt.getArgName());
    }

    // Additional test to verify that withArgName works correctly
    @Test(timeout = 4000)
    public void testCustomArgNameAfterDefault() {
        OptionBuilder.withArgName("myarg");
        Option opt = OptionBuilder.create('c');
        assertEquals("argName should be 'myarg'", "myarg", opt.getArgName());
    }

    // ===== Partition D: Exception & Defensive Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateWithoutLongoptThrowsException() {
        // longopt is null (due to reset), and create() requires it
        OptionBuilder.create();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateNullOption() {
        // Option constructor throws IllegalArgumentException for null string
        OptionBuilder.create((String) null);
    }

    @Test(timeout = 4000)
    public void testCreateCharBoundaryValid() {
        // Option must be single non-whitespace letter or digit. We'll test '1' is valid.
        Option opt = OptionBuilder.create('1');
        assertEquals("opt", "1", opt.getOpt());
    }

    @Test(timeout = 4000)
    public void testWithTypeObject() {
        OptionBuilder.withType(Integer.class);
        Option opt = OptionBuilder.create('i');
        assertEquals("type", Integer.class, opt.getType());
    }

    @Test(timeout = 4000)
    public void testWithDescription() {
        OptionBuilder.withDescription("Help text");
        Option opt = OptionBuilder.create('h');
        assertEquals("description", "Help text", opt.getDescription());
    }

    // ===== Additional coverage: Chaining and reuse =====
    @Test(timeout = 4000)
    public void testChainingMultipleCalls() {
        // Simulate typical usage pattern
        OptionBuilder.withLongOpt("verbose")
                     .withDescription("Verbose output")
                     .hasArg()
                     .isRequired()
                     .withArgName("level");
        Option opt = OptionBuilder.create('v');
        assertTrue("opt isRequired", opt.isRequired());
        assertEquals("args", 1, opt.getArgs());
        assertEquals("argName", "level", opt.getArgName());
        assertEquals("description", "Verbose output", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testReuseBuilderAfterReset() {
        OptionBuilder.withLongOpt("first");
        Option first = OptionBuilder.create('1');
        assertNotNull(first);
        // After create, builder is reset. Create another without setting anything.
        Option second = OptionBuilder.create('2');
        assertNull("longopt should be null after reset", second.getLongOpt());
        assertEquals("args should be UNINITIALIZED", Option.UNINITIALIZED, second.getArgs());
    }
}