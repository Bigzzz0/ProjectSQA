package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced White-Box JUnit 4 test suite for OptionBuilder.
 * Targets line/branch coverage and the known Defects4J defect:
 * "testBuilderIsResettedAlways" – after a failed create() (via create(null)),
 * the static fields are not reset, causing inheritance of previous state.
 *
 * [Branch & Defect Analysis Matrix]
 * 1. create() branch on longopt == null → thrown + reset (covered)
 * 2. create(String) – always resets at end, EXCEPT if exception thrown before reset.
 *    The defect path: create() with longopt set calls create(null) which throws
 *    (e.g., IllegalArgumentException in Option constructor) before reset is called.
 * 3. Test all setter methods to modify static state.
 * 4. Verify reset() restores default values: description=null, argName="arg",
 *    longopt=null, type=null, required=false, numberOfArgs=UNINITIALIZED,
 *    optionalArg=false, valuesep=(char)0.
 * 5. Boundary: create(null), create with invalid opt (empty string),
 *    create() without longopt.
 */

public class OptionBuilderDeepseekTest {

    // =============== Partition A: Core Functional Logic & State Transitions ===============

    @Test(timeout = 4000)
    public void testBasicOptionCreation() {
        Option opt = OptionBuilder
                .withLongOpt("long")
                .withDescription("desc")
                .hasArg()
                .isRequired()
                .withArgName("file")
                .withType(String.class)
                .withValueSeparator(':')
                .create('a');

        assertEquals("a", opt.getOpt());
        assertEquals("long", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
        assertTrue(opt.hasArg());
        assertTrue(opt.isRequired());
        assertEquals("file", opt.getArgName());
        assertEquals(String.class, opt.getType());
        assertEquals(':', opt.getValueSeparator());
        assertEquals(1, opt.getArgs());   // hasArg() sets numberOfArgs=1
    }

    @Test(timeout = 4000)
    public void testCreateWithLongOptAndNoOpt() {
        // This path calls create() -> create(null).
        // If longopt is set, create(null) is called; Option constructor likely
        // throws IllegalArgumentException for null opt. We expect the exception.
        // After catching, the builder must be reset.
        OptionBuilder.withLongOpt("myopt");
        OptionBuilder.withDescription("should not leak");
        try {
            OptionBuilder.create();   // triggers create(null)
            fail("Expected IllegalArgumentException due to null opt");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        // Verify that state is reset (no description leak)
        Option fresh = OptionBuilder.create('x');
        assertNull("Description should be null after failed create", fresh.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithoutLongoptThrows() {
        // OptionBuilder.create() requires longopt to be set
        try {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException: must specify longopt");
        } catch (IllegalArgumentException e) {
            assertEquals("must specify longopt", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testBuilderResetAfterSuccessfulCreate() {
        OptionBuilder.withDescription("temp");
        OptionBuilder.withLongOpt("temp");
        Option first = OptionBuilder.create('t');
        assertNotNull(first);

        // After reset, all fields are default
        Option second = OptionBuilder.create('u');
        assertNull("Description should be null after reset", second.getDescription());
        assertNull("LongOpt should be null after reset", second.getLongOpt());
        assertFalse("Required should be false after reset", second.isRequired());
        assertEquals("ArgName should be 'arg' after reset", "arg", second.getArgName());
        assertEquals("Args should be UNINITIALIZED after reset", Option.UNINITIALIZED, second.getArgs());
        assertFalse("OptionalArg should be false after reset", second.hasOptionalArg());
        assertEquals("ValueSeparator should be (char)0 after reset", (char) 0, second.getValueSeparator());
    }

    // =============== Partition B: Boundary Value Analysis & Reset ===============

    @Test(timeout = 4000)
    public void testBuilderResetAfterFailedCreateDueToNullOpt() {
        // The exact defect scenario: create() with longopt set calls create(null)
        // which throws. Reset() is NOT called because exception occurs before.
        // Ensure that state does NOT leak to next creation.
        OptionBuilder.withLongOpt("leak");
        OptionBuilder.withDescription("leaked");
        try {
            OptionBuilder.create();
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Now create a valid option – should not have description
        Option safe = OptionBuilder.create('s');
        assertNull("Description should NOT be inherited from previous failed creation", safe.getDescription());
    }

    @Test(timeout = 4000)
    public void testHasArgMethods() {
        // Test hasArg() sets numberOfArgs = 1
        OptionBuilder.reset(); // ensure clean
        OptionBuilder.hasArg();
        Option opt = OptionBuilder.create('a');
        assertEquals(1, opt.getArgs());

        // Test hasArg(true) same
        OptionBuilder.reset();
        OptionBuilder.hasArg(true);
        opt = OptionBuilder.create('b');
        assertEquals(1, opt.getArgs());

        // Test hasArg(false) sets numberOfArgs = UNINITIALIZED
        OptionBuilder.reset();
        OptionBuilder.hasArg(false);
        opt = OptionBuilder.create('c');
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgsMethods() {
        // hasArgs() sets UNLIMITED_VALUES
        OptionBuilder.reset();
        OptionBuilder.hasArgs();
        Option opt = OptionBuilder.create('d');
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());

        // hasArgs(int) sets exact number
        OptionBuilder.reset();
        OptionBuilder.hasArgs(5);
        opt = OptionBuilder.create('e');
        assertEquals(5, opt.getArgs());

        // hasOptionalArg() sets numberOfArgs=1 and optionalArg=true
        OptionBuilder.reset();
        OptionBuilder.hasOptionalArg();
        opt = OptionBuilder.create('f');
        assertTrue(opt.hasOptionalArg());
        assertEquals(1, opt.getArgs());

        // hasOptionalArgs() sets UNLIMITED_VALUES and optionalArg=true
        OptionBuilder.reset();
        OptionBuilder.hasOptionalArgs();
        opt = OptionBuilder.create('g');
        assertTrue(opt.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, opt.getArgs());

        // hasOptionalArgs(int) sets exact num and optionalArg=true
        OptionBuilder.reset();
        OptionBuilder.hasOptionalArgs(3);
        opt = OptionBuilder.create('h');
        assertTrue(opt.hasOptionalArg());
        assertEquals(3, opt.getArgs());
    }

    @Test(timeout = 4000)
    public void testWithValueSeparator() {
        // withValueSeparator(char)
        OptionBuilder.reset();
        OptionBuilder.withValueSeparator(':');
        Option opt = OptionBuilder.create('i');
        assertEquals(':', opt.getValueSeparator());

        // withValueSeparator() defaults to '='
        OptionBuilder.reset();
        OptionBuilder.withValueSeparator();
        opt = OptionBuilder.create('j');
        assertEquals('=', opt.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testIsRequired() {
        // isRequired() sets required = true
        OptionBuilder.reset();
        OptionBuilder.isRequired();
        Option opt = OptionBuilder.create('k');
        assertTrue(opt.isRequired());

        // isRequired(boolean) sets as given
        OptionBuilder.reset();
        OptionBuilder.isRequired(false);
        opt = OptionBuilder.create('l');
        assertFalse(opt.isRequired());

        OptionBuilder.reset();
        OptionBuilder.isRequired(true);
        opt = OptionBuilder.create('m');
        assertTrue(opt.isRequired());
    }

    @Test(timeout = 4000)
    public void testWithType() {
        OptionBuilder.reset();
        OptionBuilder.withType(Integer.class);
        Option opt = OptionBuilder.create('n');
        assertEquals(Integer.class, opt.getType());
    }

    @Test(timeout = 4000)
    public void testWithArgName() {
        OptionBuilder.reset();
        OptionBuilder.withArgName("custom");
        Option opt = OptionBuilder.create('o');
        assertEquals("custom", opt.getArgName());
    }

    @Test(timeout = 4000)
    public void testWithDescription() {
        OptionBuilder.reset();
        OptionBuilder.withDescription("my description");
        Option opt = OptionBuilder.create('p');
        assertEquals("my description", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testResetDefaultArgName() {
        // After reset, argName should be "arg"
        OptionBuilder.reset();
        Option opt = OptionBuilder.create('q');
        assertEquals("arg", opt.getArgName());
    }

    @Test(timeout = 4000)
    public void testMultipleCallsNoCrossContamination() {
        // Create two options with different settings; ensure no interference
        Option first = OptionBuilder
                .withLongOpt("first")
                .withDescription("first desc")
                .isRequired()
                .create('A');

        Option second = OptionBuilder
                .withLongOpt("second")
                .withDescription("second desc")
                .create('B');

        assertEquals("first", first.getLongOpt());
        assertEquals("first desc", first.getDescription());
        assertTrue(first.isRequired());

        assertEquals("second", second.getLongOpt());
        assertEquals("second desc", second.getDescription());
        assertFalse(second.isRequired());
    }

    // =============== Partition C: Defect-Targeted Branch Zone ===============

    @Test(timeout = 4000)
    public void testCreateWithNullOptThrowsButResetsState() {
        // Directly exercise create(String) with null opt via create() with longopt set.
        // This is the exact defect trigger.
        OptionBuilder.withLongOpt("mylong");
        OptionBuilder.withDescription("stale");
        OptionBuilder.withRequired(true);
        OptionBuilder.withArgs(Option.UNLIMITED_VALUES);
        OptionBuilder.withArgName("staleArg");
        try {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Now create a valid option without setting anything
        Option fresh = OptionBuilder.create('Z');
        assertNull("LongOpt leaked", fresh.getLongOpt());
        assertNull("Description leaked", fresh.getDescription());
        assertFalse("Required leaked", fresh.isRequired());
        assertEquals("Args not reset to UNINITIALIZED", Option.UNINITIALIZED, fresh.getArgs());
        assertEquals("ArgName not reset to default", "arg", fresh.getArgName());
    }

    // =============== Partition D: Exception & Defensive Guard Paths ===============

    @Test(timeout = 4000)
    public void testCreateWithInvalidOptThrows() {
        // Option constructor may throw for empty or invalid string.
        // We test with empty string (assuming it's invalid).
        try {
            OptionBuilder.create("");
            fail("Expected IllegalArgumentException for empty opt");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithLongoptAndNoArgumentToCreate() {
        // Ensure that create() with longopt set but null opt correctly throws
        // and resets (already covered in defect test)
        OptionBuilder.withLongOpt("longonly");
        OptionBuilder.withDescription("temp");
        try {
            OptionBuilder.create(); // calls create(null)
            fail("Expected exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // Verify reset
        Option next = OptionBuilder.create('n');
        assertNull(next.getDescription());
    }

    // =============== Partition E: Object Lifecycle & Contract Integrity ===============

    @Test(timeout = 4000)
    public void testOptionEqualityAfterBuilder() {
        // Not directly about builder, but verify Option equality works
        Option opt1 = OptionBuilder.create('x');
        Option opt2 = OptionBuilder.create('x');
        assertEquals("Options with same opt should be equal", opt1, opt2);
        assertEquals(opt1.hashCode(), opt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testOptionToStringNotNull() {
        Option opt = OptionBuilder.withDescription("test").create('y');
        assertNotNull(opt.toString());
    }

    @Test(timeout = 4000)
    public void testOptionCloning() throws CloneNotSupportedException {
        Option original = OptionBuilder.withLongOpt("clone").create('z');
        Option cloned = (Option) original.clone();
        assertEquals(original, cloned);
        assertEquals(original.getLongOpt(), cloned.getLongOpt());
    }

    // =============== Additional coverage for remaining methods ===============

    @Test(timeout = 4000)
    public void testWithLongOptAndDescription() {
        // Individual setters coverage
        OptionBuilder.reset();
        Option opt = OptionBuilder
                .withLongOpt("longopt")
                .withDescription("desc")
                .create('w');
        assertEquals("longopt", opt.getLongOpt());
        assertEquals("desc", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateCharMethod() {
        // create(char) = create(String.valueOf(char))
        Option opt = OptionBuilder.create('1');
        assertEquals("1", opt.getOpt());
    }

    @Test(timeout = 4000)
    public void testOptionDefaultValues() {
        Option opt = OptionBuilder.create('0');
        assertEquals(Option.UNINITIALIZED, opt.getArgs());
        assertFalse(opt.hasOptionalArg());
        assertEquals((char) 0, opt.getValueSeparator());
        assertEquals("arg", opt.getArgName());
        assertNull(opt.getLongOpt());
        assertNull(opt.getType());
        assertFalse(opt.isRequired());
    }
}