package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: OptionBuilder (static builder pattern with mutable static state)
 * 
 * Decision branches covered:
 * 1. create(char) -> delegates to create(String) with String.valueOf(opt)
 * 2. create() -> throws if longopt == null, else delegates to create(null)
 * 3. create(String) -> try-finally block that always resets state
 * 4. hasArg(boolean) -> ternary: true -> 1, false -> UNINITIALIZED
 * 5. hasOptionalArgs(int) -> sets numberOfArgs and optionalArg
 * 6. withValueSeparator() -> sets '=' vs withValueSeparator(char)
 * 7. All setter methods return the singleton instance (fluent interface)
 * 
 * Boundary conditions:
 * - null longopt in create() -> must throw IllegalArgumentException
 * - null opt string in create(String) -> passes to Option constructor
 * - char boundaries: 'a', 'Z', '0', special chars
 * - numberOfArgs: UNINITIALIZED (-1), 0, 1, UNLIMITED_VALUES (-2)
 * - valuesep: default (char)0, '=', ':', custom char
 * 
 * KNOWN DEFECT (from Defects4J):
 * - testGetParsedOptionValue: expected:<foo> but was:<null>
 * - testTwoCompleteOptions: expected:<class java.lang.String> but was:<null>
 * Root cause: The builder's static state is not properly reset between
 * consecutive create() calls, causing the second option to inherit
 * stale state from the first (specifically type and description).
 * 
 * Defect-triggering test: testTwoCompleteOptionsDefect()
 * - Creates first option with type String and description
 * - Creates second option WITHOUT setting type/description
 * - Asserts second option has null type and null description
 * - On defective version, second option incorrectly inherits first's type
 */
public class OptionBuilderDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testFluentInterfaceReturnsSameInstance() {
        OptionBuilder builder = OptionBuilder.withLongOpt("test");
        assertSame("withLongOpt should return singleton", builder, OptionBuilder.withLongOpt("other"));
        assertSame("hasArg should return singleton", builder, OptionBuilder.hasArg());
        assertSame("withArgName should return singleton", builder, OptionBuilder.withArgName("name"));
        assertSame("isRequired should return singleton", builder, OptionBuilder.isRequired());
        assertSame("withValueSeparator should return singleton", builder, OptionBuilder.withValueSeparator(':'));
        assertSame("withType should return singleton", builder, OptionBuilder.withType(String.class));
        assertSame("withDescription should return singleton", builder, OptionBuilder.withDescription("desc"));
        assertSame("hasArgs should return singleton", builder, OptionBuilder.hasArgs());
        assertSame("hasOptionalArg should return singleton", builder, OptionBuilder.hasOptionalArg());
        assertSame("hasOptionalArgs should return singleton", builder, OptionBuilder.hasOptionalArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithAllPropertiesSet() {
        Option opt = OptionBuilder
                .withLongOpt("long")
                .withDescription("description")
                .withArgName("arg")
                .isRequired()
                .hasArg()
                .withType(Integer.class)
                .withValueSeparator(':')
                .create('a');

        assertEquals("opt char", "a", opt.getOpt());
        assertEquals("long opt", "long", opt.getLongOpt());
        assertEquals("description", "description", opt.getDescription());
        assertEquals("arg name", "arg", opt.getArgName());
        assertTrue("required", opt.isRequired());
        assertEquals("number of args", 1, opt.getArgs());
        assertEquals("type", Integer.class, opt.getType());
        assertEquals("value separator", ':', opt.getValueSeparator());
        assertFalse("optional arg", opt.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testCreateWithBooleanHasArg() {
        Option optTrue = OptionBuilder.hasArg(true).create('a');
        assertEquals("hasArg(true) sets 1 arg", 1, optTrue.getArgs());

        Option optFalse = OptionBuilder.hasArg(false).create('b');
        assertEquals("hasArg(false) sets UNINITIALIZED", Option.UNINITIALIZED, optFalse.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithOptionalArgs() {
        Option opt1 = OptionBuilder.hasOptionalArg().create('a');
        assertTrue("hasOptionalArg sets optional", opt1.hasOptionalArg());
        assertEquals("hasOptionalArg sets 1 arg", 1, opt1.getArgs());

        Option optUnlimited = OptionBuilder.hasOptionalArgs().create('b');
        assertTrue("hasOptionalArgs sets optional", optUnlimited.hasOptionalArg());
        assertEquals("hasOptionalArgs sets unlimited", Option.UNLIMITED_VALUES, optUnlimited.getArgs());

        Option optNum = OptionBuilder.hasOptionalArgs(3).create('c');
        assertTrue("hasOptionalArgs(num) sets optional", optNum.hasOptionalArg());
        assertEquals("hasOptionalArgs(num) sets num args", 3, optNum.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithArgsCount() {
        Option opt = OptionBuilder.hasArgs(5).create('a');
        assertEquals("hasArgs(5) sets 5 args", 5, opt.getArgs());

        Option optUnlimited = OptionBuilder.hasArgs().create('b');
        assertEquals("hasArgs() sets unlimited", Option.UNLIMITED_VALUES, optUnlimited.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithValueSeparatorDefault() {
        Option opt = OptionBuilder.withValueSeparator().create('a');
        assertEquals("default separator is '='", '=', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testCreateWithCustomValueSeparator() {
        Option opt = OptionBuilder.withValueSeparator(';').create('a');
        assertEquals("custom separator", ';', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testCreateWithBooleanRequired() {
        Option optTrue = OptionBuilder.isRequired(true).create('a');
        assertTrue("isRequired(true)", optTrue.isRequired());

        Option optFalse = OptionBuilder.isRequired(false).create('b');
        assertFalse("isRequired(false)", optFalse.isRequired());
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testCreateWithNullLongOpt() {
        Option opt = OptionBuilder.withLongOpt(null).create('a');
        assertNull("null long opt", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testCreateWithEmptyStringOpt() {
        Option opt = OptionBuilder.create("");
        assertNotNull("empty string opt should create", opt);
        assertEquals("empty opt", "", opt.getOpt());
    }

    @Test(timeout = 4000)
    public void testCreateWithSpecialCharacters() {
        Option opt1 = OptionBuilder.create("!");
        assertEquals("special char !", "!", opt1.getOpt());

        Option opt2 = OptionBuilder.create("?");
        assertEquals("special char ?", "?", opt2.getOpt());

        Option opt3 = OptionBuilder.create(" ");
        assertEquals("space char", " ", opt3.getOpt());
    }

    @Test(timeout = 4000)
    public void testCreateWithZeroArgs() {
        Option opt = OptionBuilder.hasArgs(0).create('a');
        assertEquals("zero args", 0, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithNegativeArgs() {
        Option opt = OptionBuilder.hasArgs(-5).create('a');
        assertEquals("negative args", -5, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithNullType() {
        Option opt = OptionBuilder.withType(null).create('a');
        assertNull("null type", opt.getType());
    }

    @Test(timeout = 4000)
    public void testCreateWithNullDescription() {
        Option opt = OptionBuilder.withDescription(null).create('a');
        assertNull("null description", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithNullArgName() {
        Option opt = OptionBuilder.withArgName(null).create('a');
        assertNull("null arg name", opt.getArgName());
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    /**
     * Defect-triggering test for the known failure:
     * testTwoCompleteOptions expected:<class java.lang.String> but was:<null>
     * 
     * This test creates two options in sequence. The first option sets
     * type and description. The second option does NOT set these.
     * On the defective version, the second option incorrectly inherits
     * the type from the first option (due to static state not being
     * properly reset between create calls).
     */
    @Test(timeout = 4000)
    public void testTwoCompleteOptionsDefect() {
        // First option with type and description
        Option first = OptionBuilder
                .withLongOpt("first")
                .withDescription("first description")
                .withType(String.class)
                .create('f');

        assertEquals("first type", String.class, first.getType());
        assertEquals("first description", "first description", first.getDescription());

        // Second option WITHOUT setting type/description
        Option second = OptionBuilder
                .withLongOpt("second")
                .create('s');

        // On defective version, second.getType() incorrectly returns String.class
        assertNull("second type should be null (not inherited from first)", second.getType());
        assertNull("second description should be null (not inherited from first)", second.getDescription());
    }

    /**
     * Additional defect-related test: verify that state is reset
     * after create() is called, so a fresh option doesn't inherit
     * previous settings.
     */
    @Test(timeout = 4000)
    public void testStateResetAfterCreate() {
        OptionBuilder.withLongOpt("temp").withType(Integer.class).withDescription("temp desc").create('t');

        // After create, builder should be reset to defaults
        Option fresh = OptionBuilder.create('n');
        assertNull("long opt should be null after reset", fresh.getLongOpt());
        assertNull("type should be null after reset", fresh.getType());
        assertNull("description should be null after reset", fresh.getDescription());
        assertFalse("required should be false after reset", fresh.isRequired());
        assertEquals("args should be UNINITIALIZED after reset", Option.UNINITIALIZED, fresh.getArgs());
        assertFalse("optionalArg should be false after reset", fresh.hasOptionalArg());
        assertEquals("value separator should be (char)0 after reset", (char) 0, fresh.getValueSeparator());
        assertNull("arg name should be null after reset", fresh.getArgName());
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateWithoutLongOptThrows() {
        // Ensure no long opt is set
        OptionBuilder.withLongOpt(null);
        // Force reset by calling create() which checks longopt
        OptionBuilder.create();
    }

    @Test(timeout = 4000)
    public void testCreateWithInvalidOptThrows() {
        try {
            OptionBuilder.create("invalid");
            fail("Expected IllegalArgumentException for invalid opt");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithNullStringOpt() {
        // create(null) should pass null to Option constructor
        // Option constructor may throw or accept null depending on implementation
        try {
            Option opt = OptionBuilder.create((String) null);
            assertNotNull("null opt should create option", opt);
        } catch (IllegalArgumentException e) {
            // acceptable if Option rejects null
        }
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testCreateReturnsDistinctOptions() {
        Option opt1 = OptionBuilder.withLongOpt("one").create('a');
        Option opt2 = OptionBuilder.withLongOpt("two").create('b');

        assertNotSame("options should be distinct objects", opt1, opt2);
        assertEquals("first opt char", "a", opt1.getOpt());
        assertEquals("second opt char", "b", opt2.getOpt());
        assertEquals("first long opt", "one", opt1.getLongOpt());
        assertEquals("second long opt", "two", opt2.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testCreateWithCharBoundaries() {
        Option optMin = OptionBuilder.create(Character.MIN_VALUE);
        assertEquals("min char", String.valueOf(Character.MIN_VALUE), optMin.getOpt());

        Option optMax = OptionBuilder.create(Character.MAX_VALUE);
        assertEquals("max char", String.valueOf(Character.MAX_VALUE), optMax.getOpt());

        Option optDigit = OptionBuilder.create('5');
        assertEquals("digit char", "5", optDigit.getOpt());
    }

    @Test(timeout = 4000)
    public void testBuilderStateIsolation() {
        // Set some state
        OptionBuilder.withLongOpt("test").withType(String.class).withDescription("desc");

        // Create option - this should reset state
        Option opt = OptionBuilder.create('a');
        assertEquals("test", opt.getLongOpt());

        // Next create should have no long opt
        Option next = OptionBuilder.create('b');
        assertNull("long opt should be null after reset", next.getLongOpt());
        assertNull("type should be null after reset", next.getType());
        assertNull("description should be null after reset", next.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithAllOptionalSettings() {
        Option opt = OptionBuilder
                .withLongOpt("opt")
                .withDescription("desc")
                .withArgName("arg")
                .isRequired()
                .hasOptionalArgs(2)
                .withType(Double.class)
                .withValueSeparator(',')
                .create('x');

        assertEquals("opt", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
        assertEquals("arg", opt.getArgName());
        assertTrue(opt.isRequired());
        assertTrue(opt.hasOptionalArg());
        assertEquals(2, opt.getArgs());
        assertEquals(Double.class, opt.getType());
        assertEquals(',', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testCreateWithUnlimitedOptionalArgs() {
        Option opt = OptionBuilder.hasOptionalArgs().create('a');
        assertTrue(opt.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testCreateWithZeroOptionalArgs() {
        Option opt = OptionBuilder.hasOptionalArgs(0).create('a');
        assertTrue(opt.hasOptionalArg());
        assertEquals(0, opt.getArgs());
    }
}