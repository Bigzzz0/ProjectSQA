package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.cli.OptionBuilder
 * Known Defect (Defects4J): HelpFormatterTest::testDefaultArgName fails because OptionBuilder.reset()
 *                           erroneously initializes argName to "arg" instead of null. As a consequence,
 *                           OptionBuilder.create(...) assigns "arg" to the option's argument name,
 *                           causing option.hasArgName() to return true and preventing HelpFormatter
 *                           from substituting its default argument name (e.g., "argument").
 *
 * Decision / Condition Matrix:
 * ----------------------------------------------------------------------------------------------------
 * Method                     Branch / Condition                 Targeted in Test
 * ----------------------------------------------------------------------------------------------------
 * hasArg(boolean)            hasArg == true                     testHasArgBooleanTrue
 *                            hasArg == false                    testHasArgBooleanFalse
 * isRequired(boolean)        newRequired == true                testIsRequiredBooleanTrue
 *                            newRequired == false               testIsRequiredBooleanFalse
 * create()                   longopt == null (throws IAE)       testCreateWithoutLongOptThrowsException
 *                            longopt != null (delegates)        testCreateWithLongOptOnly
 * create(char)               delegates to create(String)        testCreateWithChar
 * create(String)             try-block / normal path            testCreateWithString, testCompleteOption
 *                            finally-block / reset() invoked    testResetAfterCreation, testResetAfterException
 * hasOptionalArgs(int)       explicit numArgs assigned          testHasOptionalArgsWithCount
 * hasOptionalArgs()          UNLIMITED_VALUES assigned          testHasOptionalArgsUnlimited
 * hasOptionalArg()           single arg (1) assigned            testHasOptionalArgSingle
 * hasArgs(int)               explicit num args assigned         testHasArgsWithCount
 * hasArgs()                  UNLIMITED_VALUES assigned          testHasArgsUnlimited
 * withValueSeparator()       sep = '='                          testWithValueSeparatorDefault
 * withValueSeparator(char)   sep = custom char                  testWithValueSeparatorCustom
 * reset() [CRITICAL DEFECT]  argName initialized to null?       testDefaultArgNameNull,
 *                                                               testDefaultArgNameHelpFormatterIntegration
 * ====================================================================================================
 */
public class OptionBuilderGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompleteOptionCreationWithChar()
    {
        Option option = OptionBuilder.withLongOpt("output")
                                     .withDescription("output file destination")
                                     .isRequired()
                                     .hasArg()
                                     .withArgName("file")
                                     .withType(String.class)
                                     .withValueSeparator(':')
                                     .create('o');

        assertNotNull("Option instance must not be null", option);
        assertEquals("o", option.getOpt());
        assertEquals("output", option.getLongOpt());
        assertEquals("output file destination", option.getDescription());
        assertTrue("Option should be required", option.isRequired());
        assertTrue("Option should have an argument", option.hasArg());
        assertEquals(1, option.getArgs());
        assertEquals("file", option.getArgName());
        assertEquals(String.class, option.getType());
        assertEquals(':', option.getValueSeparator());
        assertTrue("Option should have value separator", option.hasValueSeparator());
        assertFalse("Option argument should not be optional", option.hasOptionalArg());
    }

    @Test(timeout = 4000)
    public void testCompleteOptionCreationWithString()
    {
        Option option = OptionBuilder.withLongOpt("config")
                                     .withDescription("configuration settings")
                                     .isRequired(false)
                                     .hasArgs(2)
                                     .withArgName("key-val")
                                     .withType(Integer.class)
                                     .create("cfg");

        assertNotNull("Option instance must not be null", option);
        assertEquals("cfg", option.getOpt());
        assertEquals("config", option.getLongOpt());
        assertEquals("configuration settings", option.getDescription());
        assertFalse("Option should not be required", option.isRequired());
        assertEquals(2, option.getArgs());
        assertTrue("Option should have args", option.hasArgs());
        assertEquals("key-val", option.getArgName());
        assertEquals(Integer.class, option.getType());
    }

    @Test(timeout = 4000)
    public void testCreateWithLongOptOnly()
    {
        Option option = OptionBuilder.withLongOpt("long-only")
                                     .withDescription("option with only a long opt")
                                     .create();

        assertNotNull("Option instance must not be null", option);
        assertNull("Short opt must be null for long-only option", option.getOpt());
        assertEquals("long-only", option.getLongOpt());
        assertEquals("option with only a long opt", option.getDescription());
    }

    @Test(timeout = 4000)
    public void testFluentMethodChainingReturnsNonNull()
    {
        OptionBuilder b1 = OptionBuilder.withLongOpt("chain");
        OptionBuilder b2 = OptionBuilder.hasArg();
        OptionBuilder b3 = OptionBuilder.isRequired();
        OptionBuilder b4 = OptionBuilder.withValueSeparator();
        OptionBuilder b5 = OptionBuilder.withDescription("desc");

        assertNotNull(b1);
        assertSame(b1, b2);
        assertSame(b2, b3);
        assertSame(b3, b4);
        assertSame(b4, b5);

        Option option = OptionBuilder.create('c');
        assertNotNull(option);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasArgBooleanTrue()
    {
        Option option = OptionBuilder.hasArg(true).create('a');
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgBooleanFalse()
    {
        Option option = OptionBuilder.hasArg(false).create('a');
        assertFalse(option.hasArg());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testIsRequiredBooleanTrue()
    {
        Option option = OptionBuilder.isRequired(true).create('r');
        assertTrue(option.isRequired());
    }

    @Test(timeout = 4000)
    public void testIsRequiredBooleanFalse()
    {
        Option option = OptionBuilder.isRequired(false).create('r');
        assertFalse(option.isRequired());
    }

    @Test(timeout = 4000)
    public void testHasArgsUnlimited()
    {
        Option option = OptionBuilder.hasArgs().create('m');
        assertTrue(option.hasArgs());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgsWithCountZero()
    {
        Option option = OptionBuilder.hasArgs(0).create('z');
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(0, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasArgsWithMaxInteger()
    {
        Option option = OptionBuilder.hasArgs(Integer.MAX_VALUE).create('x');
        assertTrue(option.hasArgs());
        assertEquals(Integer.MAX_VALUE, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgSingle()
    {
        Option option = OptionBuilder.hasOptionalArg().create('o');
        assertTrue(option.hasOptionalArg());
        assertEquals(1, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsUnlimited()
    {
        Option option = OptionBuilder.hasOptionalArgs().create('u');
        assertTrue(option.hasOptionalArg());
        assertTrue(option.hasArgs());
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testHasOptionalArgsWithCount()
    {
        Option option = OptionBuilder.hasOptionalArgs(5).create('f');
        assertTrue(option.hasOptionalArg());
        assertTrue(option.hasArgs());
        assertEquals(5, option.getArgs());
    }

    @Test(timeout = 4000)
    public void testWithValueSeparatorDefault()
    {
        Option option = OptionBuilder.withValueSeparator().create('d');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testWithValueSeparatorCustom()
    {
        Option option = OptionBuilder.withValueSeparator(';').create('s');
        assertTrue(option.hasValueSeparator());
        assertEquals(';', option.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testWithValueSeparatorNotSpecified()
    {
        Option option = OptionBuilder.create('n');
        assertFalse(option.hasValueSeparator());
        assertEquals((char) 0, option.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testWithTypeObject()
    {
        Object customType = new Object();
        Option option = OptionBuilder.withType(customType).create('t');
        assertSame(customType, option.getType());
    }

    @Test(timeout = 4000)
    public void testWithNullAndEmptyStrings()
    {
        Option option = OptionBuilder.withLongOpt("")
                                     .withDescription(null)
                                     .withArgName("")
                                     .create("k");

        assertEquals("k", option.getOpt());
        assertEquals("", option.getLongOpt());
        assertNull(option.getDescription());
        assertEquals("", option.getArgName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-16 / HelpFormatterTest)
    // =========================================================================

    /**
     * Directly targets Defects4J defect: HelpFormatterTest::testDefaultArgName.
     * OptionBuilder.reset() must reset argName to null. If it resets to "arg",
     * option.hasArgName() will evaluate to true and return "arg", which fails this test.
     */
    @Test(timeout = 4000)
    public void testDefaultArgNameNull()
    {
        Option option = OptionBuilder.hasArg().create('f');

        assertNull("Default argName must be null if not explicitly configured", option.getArgName());
        assertFalse("Option must not report hasArgName() == true when argName was not set", option.hasArgName());
    }

    /**
     * End-to-end integration test validating that a HelpFormatter with a custom
     * default argument name correctly prints that default when an Option is built
     * via OptionBuilder without explicit argName.
     */
    @Test(timeout = 4000)
    public void testDefaultArgNameHelpFormatterIntegration()
    {
        Option option = OptionBuilder.hasArg().create("f");

        Options options = new Options();
        options.addOption(option);

        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("argument");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();

        String usageOutput = baos.toString();
        assertTrue("HelpFormatter output must use custom default argName '<argument>' instead of '<arg>'",
                   usageOutput.contains("<argument>"));
        assertFalse("HelpFormatter output should not contain '<arg>' when default is overridden",
                    usageOutput.contains("<arg>"));
    }

    @Test(timeout = 4000)
    public void testArgNameResetsToNullAcrossCreations()
    {
        // First option sets an explicit argName
        Option opt1 = OptionBuilder.hasArg().withArgName("customArg").create('1');
        assertEquals("customArg", opt1.getArgName());
        assertTrue(opt1.hasArgName());

        // Subsequent option should NOT inherit the previous argName and should reset to null
        Option opt2 = OptionBuilder.hasArg().create('2');
        assertNull("Subsequent option must reset argName to null", opt2.getArgName());
        assertFalse("Subsequent option must not have an argName", opt2.hasArgName());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateWithoutLongOptThrowsException()
    {
        try
        {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException because longopt was not specified");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("must specify longopt", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithoutLongOptResetsStateOnException()
    {
        // Stage state that should be cleared when create() throws an exception
        OptionBuilder.isRequired(true);
        OptionBuilder.hasArg(true);
        OptionBuilder.withDescription("abandoned description");

        try
        {
            OptionBuilder.create();
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected)
        {
            assertEquals("must specify longopt", expected.getMessage());
        }

        // Verify that the builder state was reset
        Option cleanOption = OptionBuilder.withLongOpt("recovered").create();
        assertFalse("isRequired must have reset after exception in create()", cleanOption.isRequired());
        assertFalse("hasArg must have reset after exception in create()", cleanOption.hasArg());
        assertNull("description must have reset after exception in create()", cleanOption.getDescription());
    }

    @Test(timeout = 4000)
    public void testCreateWithInvalidCharResetsState()
    {
        // Stage state
        OptionBuilder.isRequired(true);
        OptionBuilder.hasArg(true);

        try
        {
            // '!' is an illegal option character
            OptionBuilder.create('!');
            fail("Expected IllegalArgumentException for illegal option character '!'");
        }
        catch (IllegalArgumentException expected)
        {
            // Expected exception from OptionValidator
        }

        // Verify builder state was reset by finally block in create(String)
        Option cleanOption = OptionBuilder.withLongOpt("valid").create();
        assertFalse("isRequired must have reset after illegal char exception", cleanOption.isRequired());
        assertFalse("hasArg must have reset after illegal char exception", cleanOption.hasArg());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception
    {
        Constructor<OptionBuilder> constructor = OptionBuilder.class.getDeclaredConstructor();
        assertTrue("Constructor must be private", Modifier.isPrivate(constructor.getModifiers()));

        constructor.setAccessible(true);
        OptionBuilder instance = constructor.newInstance();
        assertNotNull("Instantiated OptionBuilder via reflection should not be null", instance);
    }

    @Test(timeout = 4000)
    public void testStateIsolationBetweenSequentialCreations()
    {
        // First build fully configured option
        Option opt1 = OptionBuilder.withLongOpt("first")
                                   .withDescription("first opt")
                                   .isRequired(true)
                                   .hasArgs(3)
                                   .withValueSeparator(',')
                                   .withType(Double.class)
                                   .create('1');

        assertNotNull(opt1);
        assertEquals("first", opt1.getLongOpt());
        assertTrue(opt1.isRequired());

        // Next build bare minimum option
        Option opt2 = OptionBuilder.create('2');
        assertNotNull(opt2);
        assertEquals("2", opt2.getOpt());
        assertNull("longOpt should reset to null", opt2.getLongOpt());
        assertNull("description should reset to null", opt2.getDescription());
        assertFalse("isRequired should reset to false", opt2.isRequired());
        assertEquals("args should reset to UNINITIALIZED", Option.UNINITIALIZED, opt2.getArgs());
        assertFalse("hasOptionalArg should reset to false", opt2.hasOptionalArg());
        assertFalse("hasValueSeparator should reset to false", opt2.hasValueSeparator());
        assertEquals("value separator should reset to 0", (char) 0, opt2.getValueSeparator());
        assertNull("type should reset to null", opt2.getType());
    }
}