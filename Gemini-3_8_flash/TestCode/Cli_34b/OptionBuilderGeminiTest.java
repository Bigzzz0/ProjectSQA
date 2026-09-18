package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.apache.commons.cli.OptionBuilder
 *
 * DECISION / CONDITION BRANCHES TARGETED:
 * 1. create() : Branch (longopt == null) -> throws IllegalArgumentException & resets.
 * 2. create() : Branch (longopt != null) -> delegates to create(null).
 * 3. hasArg(boolean) : Branch (hasArg == true) -> numberOfArgs = 1.
 * 4. hasArg(boolean) : Branch (hasArg == false) -> numberOfArgs = Option.UNINITIALIZED.
 * 5. isRequired(boolean) : Branch (newRequired == true) -> required = true.
 * 6. isRequired(boolean) : Branch (newRequired == false) -> required = false.
 * 7. create(String) : finally block ensures OptionBuilder.reset() is ALWAYS executed on success/error.
 *
 * DEFECT-SPECIFIC GROUND TRUTH:
 * - CLI Defect (CommandLineTest::testGetParsedOptionValue / OptionBuilderTest::testTwoCompleteOptions):
 *   When creating consecutive options, if the first option defines a custom type and the second option
 *   does not specify a type, the second option must retain the default Option type (String.class),
 *   rather than inheriting null or corrupt state from OptionBuilder.reset() invoking option.setType(null).
 *
 * EQUIVALENCE PARTITIONS:
 * - Partition A: Core Functional Logic & State Transitions (Builder chaining, full parameter propagation)
 * - Partition B: Boundary Value Analysis & Extremes (Arity counts, separators, boolean flags)
 * - Partition C: Defect-Targeted Branch Zone (Consecutive builder calls, default type integrity)
 * - Partition D: Exception & Defensive Guard Paths (Missing longopt, illegal chars, finally-block reset)
 * - Partition E: Object Lifecycle & Contract Integrity (Private constructor reflection coverage)
 * ----------------------------------------------------------------------------------------------------
 */
public class OptionBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompleteOptionCreationWithChar() {
        Option opt = OptionBuilder.withLongOpt("output")
                                  .withDescription("target output file")
                                  .hasArg()
                                  .isRequired()
                                  .withType(String.class)
                                  .withArgName("file")
                                  .withValueSeparator(':')
                                  .create('o');

        assertNotNull("Created option should not be null", opt);
        assertEquals("o", opt.getOpt());
        assertEquals("output", opt.getLongOpt());
        assertEquals("target output file", opt.getDescription());
        assertEquals("file", opt.getArgName());
        assertEquals(':', opt.getValueSeparator());
        assertEquals(1, opt.getArgs());
        assertTrue(opt.hasArg());
        assertTrue(opt.isRequired());
        assertEquals(String.class, opt.getType());
    }

    @Test(timeout = 4000)
    public void testCompleteOptionCreationWithString() {
        Option opt = OptionBuilder.withLongOpt("config")
                                  .withDescription("config file path")
                                  .hasArgs(2)
                                  .isRequired(false)
                                  .withArgName("path")
                                  .withValueSeparator('=')
                                  .create("cfg");

        assertNotNull("Created option should not be null", opt);
        assertEquals("cfg", opt.getOpt());
        assertEquals("config", opt.getLongOpt());
        assertEquals("config file path", opt.getDescription());
        assertEquals("path", opt.getArgName());
        assertEquals('=', opt.getValueSeparator());
        assertEquals(2, opt.getArgs());
        assertTrue(opt.hasArgs());
        assertFalse(opt.isRequired());
    }

    @Test(timeout = 4000)
    public void testOptionCreationWithLongOptOnly() {
        Option opt = OptionBuilder.withLongOpt("dry-run")
                                  .withDescription("simulate run")
                                  .create();

        assertNotNull("Option created via longOpt only should not be null", opt);
        assertNull("Short opt should be null when created without one", opt.getOpt());
        assertEquals("dry-run", opt.getLongOpt());
        assertEquals("simulate run", opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testOptionBuilderChainingReturnsInstance() {
        OptionBuilder builder1 = OptionBuilder.withLongOpt("test");
        OptionBuilder builder2 = OptionBuilder.hasArg();
        OptionBuilder builder3 = OptionBuilder.isRequired();

        assertSame("Builder methods must return the singleton instance", builder1, builder2);
        assertSame("Builder methods must return the singleton instance", builder2, builder3);

        // Clean up builder state
        OptionBuilder.create('t');
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasArgBooleanEquivalence() {
        // hasArg(true) -> numberOfArgs = 1
        Option optTrue = OptionBuilder.withLongOpt("flagTrue")
                                      .hasArg(true)
                                      .create('t');
        assertTrue(optTrue.hasArg());
        assertEquals(1, optTrue.getArgs());

        // hasArg(false) -> numberOfArgs = Option.UNINITIALIZED
        Option optFalse = OptionBuilder.withLongOpt("flagFalse")
                                       .hasArg(false)
                                       .create('f');
        assertFalse(optFalse.hasArg());
        assertEquals(Option.UNINITIALIZED, optFalse.getArgs());
    }

    @Test(timeout = 4000)
    public void testIsRequiredBooleanEquivalence() {
        // isRequired(true)
        Option optReq = OptionBuilder.withLongOpt("req")
                                     .isRequired(true)
                                     .create('r');
        assertTrue(optReq.isRequired());

        // isRequired(false)
        Option optNotReq = OptionBuilder.withLongOpt("notReq")
                                        .isRequired(false)
                                        .create('n');
        assertFalse(optNotReq.isRequired());
    }

    @Test(timeout = 4000)
    public void testHasArgsVariations() {
        // hasArgs() -> UNLIMITED_VALUES
        Option optUnlimited = OptionBuilder.withLongOpt("files")
                                           .hasArgs()
                                           .create();
        assertEquals(Option.UNLIMITED_VALUES, optUnlimited.getArgs());
        assertTrue(optUnlimited.hasArgs());

        // hasArgs(0)
        Option optZero = OptionBuilder.withLongOpt("zero")
                                      .hasArgs(0)
                                      .create();
        assertEquals(0, optZero.getArgs());

        // hasArgs(5)
        Option optFive = OptionBuilder.withLongOpt("five")
                                      .hasArgs(5)
                                      .create();
        assertEquals(5, optFive.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsVariations() {
        // hasOptionalArg() -> 1 arg, optional = true
        Option opt1 = OptionBuilder.withLongOpt("opt1")
                                   .hasOptionalArg()
                                   .create();
        assertTrue(opt1.hasOptionalArg());
        assertEquals(1, opt1.getArgs());

        // hasOptionalArgs() -> UNLIMITED_VALUES, optional = true
        Option optInf = OptionBuilder.withLongOpt("optInf")
                                     .hasOptionalArgs()
                                     .create();
        assertTrue(optInf.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, optInf.getArgs());

        // hasOptionalArgs(3) -> 3 args, optional = true
        Option opt3 = OptionBuilder.withLongOpt("opt3")
                                   .hasOptionalArgs(3)
                                   .create();
        assertTrue(opt3.hasOptionalArg());
        assertEquals(3, opt3.getArgs());
    }

    @Test(timeout = 4000)
    public void testWithValueSeparatorVariations() {
        // Default withValueSeparator() -> '='
        Option optDefault = OptionBuilder.withLongOpt("prop1")
                                         .withValueSeparator()
                                         .create();
        assertEquals('=', optDefault.getValueSeparator());

        // Custom withValueSeparator(':')
        Option optCustom = OptionBuilder.withLongOpt("prop2")
                                        .withValueSeparator(':')
                                        .create();
        assertEquals(':', optCustom.getValueSeparator());

        // Unset value separator should default to (char) 0
        Option optNone = OptionBuilder.withLongOpt("prop3")
                                      .create();
        assertEquals((char) 0, optNone.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testNullAndEmptyPropertiesHandling() {
        Option opt = OptionBuilder.withLongOpt(null)
                                  .withDescription(null)
                                  .withArgName(null)
                                  .create("opt");

        assertNull(opt.getLongOpt());
        assertNull(opt.getDescription());
        assertNull(opt.getArgName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J Target)
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * - OptionBuilderTest::testTwoCompleteOptions
     * - CommandLineTest::testGetParsedOptionValue
     *
     * When creating two consecutive options where the first defines a custom type
     * (e.g. Float.class) and the second does not specify a type, the second option
     * must default to String.class (standard Option contract) and not null.
     */
    @Test(timeout = 4000)
    public void testTwoCompleteOptions() {
        Option simple = OptionBuilder.withLongOpt("simple option")
                                     .hasArg()
                                     .isRequired()
                                     .hasArgs()
                                     .withType(Float.class)
                                     .withDescription("this is a simple option")
                                     .create('s');

        assertEquals("s", simple.getOpt());
        assertEquals("simple option", simple.getLongOpt());
        assertEquals("this is a simple option", simple.getDescription());
        assertEquals(Float.class, simple.getType());
        assertTrue(simple.hasArg());
        assertTrue(simple.isRequired());
        assertTrue(simple.hasArgs());

        simple = OptionBuilder.withLongOpt("dimple option")
                              .hasArg()
                              .withDescription("this is a dimple option")
                              .create('d');

        assertEquals("d", simple.getOpt());
        assertEquals("dimple option", simple.getLongOpt());
        assertEquals("this is a dimple option", simple.getDescription());
        assertEquals("Type of option without explicit withType() must be String.class",
                     String.class, simple.getType());
        assertTrue(simple.hasArg());
        assertFalse(simple.isRequired());
        assertFalse(simple.hasArgs());
    }

    @Test(timeout = 4000)
    public void testStateResetBetweenBuilds() {
        // Build first option with all settings enabled
        OptionBuilder.withLongOpt("opt1")
                     .withDescription("first opt")
                     .hasArg()
                     .isRequired()
                     .withArgName("arg1")
                     .withValueSeparator(',')
                     .create('1');

        // Build second minimal option
        Option opt2 = OptionBuilder.create('2');

        assertNull("longopt must be reset", opt2.getLongOpt());
        assertNull("description must be reset", opt2.getDescription());
        assertNull("argName must be reset", opt2.getArgName());
        assertFalse("required must be reset", opt2.isRequired());
        assertFalse("optionalArg must be reset", opt2.hasOptionalArg());
        assertEquals("valuesep must be reset", (char) 0, opt2.getValueSeparator());
        assertEquals("numberOfArgs must be reset", Option.UNINITIALIZED, opt2.getArgs());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateWithoutLongOptThrowsException() {
        try {
            // longopt is not set
            OptionBuilder.withDescription("missing longopt").create();
            fail("Expected IllegalArgumentException when create() called without longopt");
        } catch (IllegalArgumentException expected) {
            assertEquals("must specify longopt", expected.getMessage());
        }

        // Verify that reset() was triggered inside create() upon failure
        Option opt = OptionBuilder.withLongOpt("recovered").create();
        assertNull("Description should have been reset from the previous failed attempt",
                   opt.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithInvalidCharOptThrowsExceptionAndResets() {
        OptionBuilder.withLongOpt("testInvalid")
                     .withDescription("invalid char");

        try {
            // ' ' (space) is an illegal option character in OptionValidator
            OptionBuilder.create(' ');
            fail("Expected IllegalArgumentException for illegal option character");
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }

        // Verify that finally block executed reset()
        try {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException because longopt was reset in finally");
        } catch (IllegalArgumentException expected) {
            assertEquals("must specify longopt", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithInvalidStringOptThrowsExceptionAndResets() {
        OptionBuilder.withLongOpt("testInvalidString")
                     .withDescription("invalid string opt");

        try {
            OptionBuilder.create("invalid opt with spaces");
            fail("Expected IllegalArgumentException for illegal option string");
        } catch (IllegalArgumentException expected) {
            // Expected exception
        }

        // Verify builder is cleanly reset
        try {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException because longopt was reset");
        } catch (IllegalArgumentException expected) {
            assertEquals("must specify longopt", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorContract() throws Exception {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        assertTrue("Constructor must be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull("Instantiated instance via reflection must not be null", instance);
    }
}