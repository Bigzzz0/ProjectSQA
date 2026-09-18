package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Component: org.apache.commons.cli.OptionBuilder
 *
 * Defect Under Analysis (Defects4J Ground Truth):
 * - org.apache.commons.cli.OptionBuilderTest::testBuilderIsResettedAlways
 *   Failure Mode: If an IllegalArgumentException is thrown during option construction (e.g., invalid opt char/string),
 *   OptionBuilder.reset() is bypassed because the constructor invocation is not wrapped in a try/finally block.
 *   Consequently, the builder leaks internal static state (description, longopt, etc.) into subsequent calls.
 *
 * Branch & Condition Coverage Matrix:
 * 1. create()
 *    - Branch: longopt == null -> resets state, throws IllegalArgumentException("must specify longopt")
 *    - Branch: longopt != null -> delegates to create(null)
 * 2. create(char opt)
 *    - Converts char to String and delegates to create(String.valueOf(opt))
 * 3. create(String opt)
 *    - Instantiates Option(opt, description) -> target for exception leak defect
 *    - Applies all builder properties: longopt, required, optionalArg, numberOfArgs, type, valuesep, argName
 *    - Invokes reset()
 * 4. hasArg(boolean hasArg)
 *    - Branch: true  -> numberOfArgs = 1
 *    - Branch: false -> numberOfArgs = Option.UNINITIALIZED (-1)
 * 5. isRequired(boolean newRequired)
 *    - Branch: true  -> required = true
 *    - Branch: false -> required = false
 * 6. Fluent Configuration Methods:
 *    - withLongOpt(String), withDescription(String), withArgName(String), withType(Object)
 *    - hasArg(), hasArgs(), hasArgs(int)
 *    - hasOptionalArg(), hasOptionalArgs(), hasOptionalArgs(int)
 *    - withValueSeparator(), withValueSeparator(char)
 * 7. Encapsulation / Lifecycle:
 *    - Private constructor coverage via reflection.
 * ---------------------------------------------------------------------------------------------------------
 */
public class OptionBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompleteFluentOptionConstruction() {
        Option option = OptionBuilder.withLongOpt("service-port")
                                     .withDescription("Port number for service")
                                     .hasArg()
                                     .isRequired()
                                     .withArgName("portNum")
                                     .withType(Integer.class)
                                     .withValueSeparator(':')
                                     .create('p');

        assertEquals("p", option.getOpt());
        assertEquals("service-port", option.getLongOpt());
        assertEquals("Port number for service", option.getDescription());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
        assertTrue(option.isRequired());
        assertEquals("portNum", option.getArgName());
        assertEquals(Integer.class, option.getType());
        assertEquals(':', option.getValueSeparator());
        assertFalse(option.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testCreateWithStringOpt() {
        Option option = OptionBuilder.withLongOpt("alpha")
                                     .withDescription("alpha option")
                                     .create("a");

        assertEquals("a", option.getOpt());
        assertEquals("alpha", option.getLongOpt());
        assertEquals("alpha option", option.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithNullOptLongOptOnly() {
        Option option = OptionBuilder.withLongOpt("standalone")
                                     .withDescription("Long opt only")
                                     .create();

        assertNull(option.getOpt());
        assertEquals("standalone", option.getLongOpt());
        assertEquals("Long opt only", option.getDescription());
    }

    @Test(timeout = 4000)
    public void testResetStateAfterSuccessfulCreation() {
        Option opt1 = OptionBuilder.withLongOpt("temp")
                                   .withDescription("temporary")
                                   .hasArg()
                                   .isRequired()
                                   .withArgName("customName")
                                   .withType(Double.class)
                                   .withValueSeparator(';')
                                   .create('t');

        assertNotNull(opt1);

        // Next option created bare must have clean default state
        Option opt2 = OptionBuilder.create('b');
        assertEquals("b", opt2.getOpt());
        assertNull(opt2.getLongOpt());
        assertNull(opt2.getDescription());
        assertFalse(opt2.hasArg());
        assertEquals(Option.UNINITIALIZED, opt2.getArgs());
        assertFalse(opt2.isRequired());
        assertEquals("arg", opt2.getArgName());
        assertNull(opt2.getType());
        assertEquals((char) 0, opt2.getValueSeparator());
        assertFalse(opt2.hasOptionalArg());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Method Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasArgBooleanVariations() {
        Option optTrue = OptionBuilder.hasArg(true).create("t");
        assertTrue(optTrue.hasArg());
        assertEquals(1, optTrue.getArgs());

        Option optFalse = OptionBuilder.hasArg(false).create("f");
        assertFalse(optFalse.hasArg());
        assertEquals(Option.UNINITIALIZED, optFalse.getArgs());
    }

    @Test(timeout = 4000)
    public void testIsRequiredBooleanVariations() {
        Option optReqTrue = OptionBuilder.isRequired(true).create("r1");
        assertTrue(optReqTrue.isRequired());

        Option optReqFalse = OptionBuilder.isRequired(false).create("r2");
        assertFalse(optReqFalse.isRequired());

        Option optReqDefault = OptionBuilder.isRequired().create("r3");
        assertTrue(optReqDefault.isRequired());
    }

    @Test(timeout = 4000)
    public void testArgsVariants() {
        // Unlimited args
        Option optUnlimited = OptionBuilder.hasArgs().create("u");
        assertTrue(optUnlimited.hasArgs());
        assertEquals(Option.UNLIMITED_VALUES, optUnlimited.getArgs());

        // Exact args count
        Option optFixed = OptionBuilder.hasArgs(5).create("f");
        assertEquals(5, optFixed.getArgs());

        // Zero args
        Option optZero = OptionBuilder.hasArgs(0).create("z");
        assertEquals(0, optZero.getArgs());

        // Negative args boundary
        Option optNeg = OptionBuilder.hasArgs(-2).create("n");
        assertEquals(-2, optNeg.getArgs());
    }

    @Test(timeout = 4000)
    public void testOptionalArgsVariants() {
        // hasOptionalArg()
        Option optSingle = OptionBuilder.hasOptionalArg().create("s");
        assertTrue(optSingle.hasOptionalArg());
        assertEquals(1, optSingle.getArgs());

        // hasOptionalArgs() - unlimited
        Option optUnlim = OptionBuilder.hasOptionalArgs().create("u");
        assertTrue(optUnlim.hasOptionalArg());
        assertEquals(Option.UNLIMITED_VALUES, optUnlim.getArgs());

        // hasOptionalArgs(int)
        Option optMulti = OptionBuilder.hasOptionalArgs(3).create("m");
        assertTrue(optMulti.hasOptionalArg());
        assertEquals(3, optMulti.getArgs());
    }

    @Test(timeout = 4000)
    public void testValueSeparatorVariants() {
        // Default separator '='
        Option optDefaultSep = OptionBuilder.withValueSeparator().create("eq");
        assertEquals('=', optDefaultSep.getValueSeparator());

        // Custom separator
        Option optCustomSep = OptionBuilder.withValueSeparator(',').create("comma");
        assertEquals(',', optCustomSep.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testChainingInstanceIdentity() {
        OptionBuilder b1 = OptionBuilder.withLongOpt("chain");
        OptionBuilder b2 = OptionBuilder.hasArg();
        assertSame("Builder methods must return the singleton builder instance", b1, b2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where an IllegalArgumentException during option creation
     * (e.g. invalid option character) leaves builder state un-reset.
     */
    @Test(timeout = 4000)
    public void testBuilderIsResettedAlwaysOnInvalidCharOpt() {
        try {
            OptionBuilder.withDescription("leaked description")
                         .withLongOpt("leaked-long-opt")
                         .isRequired(true)
                         .create('%'); // Invalid option character
            fail("IllegalArgumentException should have been thrown for invalid option character '%'");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        Option nextOption = OptionBuilder.create("clean");
        assertNull("Builder should not leak description after failed create(char)", nextOption.getDescription());
        assertNull("Builder should not leak longopt after failed create(char)", nextOption.getLongOpt());
        assertFalse("Builder should not leak required state after failed create(char)", nextOption.isRequired());
    }

    @Test(timeout = 4000)
    public void testBuilderIsResettedAlwaysOnInvalidStringOpt() {
        try {
            OptionBuilder.withDescription("leaked description 2")
                         .withArgName("leakedArg")
                         .hasArg()
                         .create("invalid opt!"); // Invalid option identifier with whitespace/punctuation
            fail("IllegalArgumentException should have been thrown for invalid option string");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }

        Option nextOption = OptionBuilder.create("clean2");
        assertNull("Builder should not leak description after failed create(String)", nextOption.getDescription());
        assertEquals("Builder argName should be reset to default 'arg'", "arg", nextOption.getArgName());
        assertFalse("Builder should not leak hasArg after failed create(String)", nextOption.hasArg());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateWithoutLongOptThrowsException() {
        try {
            OptionBuilder.withDescription("orphan description").create();
            fail("Expected IllegalArgumentException when longopt is not specified for create()");
        } catch (IllegalArgumentException ex) {
            assertEquals("must specify longopt", ex.getMessage());
        }

        // Verify that create() without longOpt resets builder state as specified in implementation
        Option nextOption = OptionBuilder.create("valid");
        assertNull("Builder must be reset even after missing longopt exception", nextOption.getDescription());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorReflectiveAccess() throws Exception {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull("Instance instantiated via reflection should not be null", instance);
    }
}