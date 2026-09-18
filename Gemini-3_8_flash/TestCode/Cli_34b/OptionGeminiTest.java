package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.cli.Option
 * Focus Areas:
 *   - Partition A: Core Functional Logic & State Transitions (getters, setters, flags, state changes)
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes (UNINITIALIZED, UNLIMITED_VALUES, 0, MAX_VALUE, char 0)
 *   - Partition C: Defect-Targeted Branch Zone (Type preservation for getParsedOptionValue / OptionBuilder, null vs opt)
 *   - Partition D: Exception & Defensive Guard Paths (validateOption, list full, uninitialized processing, addValue)
 *   - Partition E: Object Lifecycle & Contract Integrity (equals, hashCode, clone deep-copy, serialization)
 *
 * Specific Decision Coverage:
 *   - acceptsArg(): (hasArg() || hasArgs() || hasOptionalArg()) && (numberOfArgs <= 0 || values.size() < numberOfArgs)
 *   - requiresArg(): optionalArg branch, UNLIMITED_VALUES branch, default acceptsArg() branch
 *   - processValue(): with value separator, token limit (numberOfArgs - 1), no separator, remainder handling
 *   - toString(): longOpt present/absent, hasArgs vs hasArg, type present/absent
 *   - equals()/hashCode(): opt null/not-null, longOpt null/not-null, class mismatch, self identity
 *   - getKey(): opt null fallback to longOpt
 * ---------------------------------------------------------------------------------------------------------
 */
public class OptionGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTwoArgConstructorDefaults()
    {
        Option option = new Option("f", "flag option");
        assertEquals("f", option.getOpt());
        assertNull(option.getLongOpt());
        assertFalse(option.hasLongOpt());
        assertEquals("flag option", option.getDescription());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertFalse(option.hasOptionalArg());
        assertFalse(option.isRequired());
        assertNull(option.getType());
        assertNull(option.getArgName());
        assertFalse(option.hasArgName());
        assertFalse(option.hasValueSeparator());
        assertEquals((char) 0, option.getValueSeparator());
        assertEquals('f', option.getId());
        assertEquals("f", option.getKey());
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructorWithArg()
    {
        Option option = new Option("o", true, "single arg option");
        assertEquals("o", option.getOpt());
        assertNull(option.getLongOpt());
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(1, option.getArgs());
        assertEquals("single arg option", option.getDescription());
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorComplete()
    {
        Option option = new Option("c", "config", true, "configuration file path");
        assertEquals("c", option.getOpt());
        assertEquals("config", option.getLongOpt());
        assertTrue(option.hasLongOpt());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
        assertEquals("configuration file path", option.getDescription());
        assertEquals('c', option.getId());
        assertEquals("c", option.getKey());
    }

    @Test(timeout = 4000)
    public void testStateMutatorsAndAccessors()
    {
        Option option = new Option("t", "test option");

        option.setLongOpt("target");
        assertEquals("target", option.getLongOpt());
        assertTrue(option.hasLongOpt());

        option.setDescription("updated description");
        assertEquals("updated description", option.getDescription());

        option.setRequired(true);
        assertTrue(option.isRequired());
        option.setRequired(false);
        assertFalse(option.isRequired());

        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
        option.setOptionalArg(false);
        assertFalse(option.hasOptionalArg());

        option.setArgName("FILE");
        assertTrue(option.hasArgName());
        assertEquals("FILE", option.getArgName());

        option.setArgName("");
        assertFalse(option.hasArgName());

        option.setArgName(null);
        assertFalse(option.hasArgName());
        assertNull(option.getArgName());
    }

    @Test(timeout = 4000)
    public void testValueSeparatorBehavior()
    {
        Option option = new Option("D", true, "define property");
        option.setValueSeparator('=');
        assertTrue(option.hasValueSeparator());
        assertEquals('=', option.getValueSeparator());

        option.setValueSeparator((char) 0);
        assertFalse(option.hasValueSeparator());
        assertEquals((char) 0, option.getValueSeparator());
    }

    @Test(timeout = 4000)
    public void testAddAndRetrieveValues()
    {
        Option option = new Option("p", true, "param");
        option.setArgs(2);

        assertNull(option.getValue());
        assertNull(option.getValue(0));
        assertNull(option.getValues());
        assertEquals("defaultVal", option.getValue("defaultVal"));

        option.addValueForProcessing("val1");
        assertEquals("val1", option.getValue());
        assertEquals("val1", option.getValue(0));
        assertEquals("val1", option.getValue("fallback"));

        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals("val1", values[0]);

        option.addValueForProcessing("val2");
        assertEquals("val1", option.getValue());
        assertEquals("val2", option.getValue(1));

        String[] multiValues = option.getValues();
        assertEquals(2, multiValues.length);
        assertEquals("val1", multiValues[0]);
        assertEquals("val2", multiValues[1]);

        List valList = option.getValuesList();
        assertEquals(2, valList.size());
        assertEquals("val1", valList.get(0));
        assertEquals("val2", valList.get(1));

        option.clearValues();
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertTrue(option.getValuesList().isEmpty());
    }

    @Test(timeout = 4000)
    public void testToStringFormattingVariants()
    {
        Option optOnly = new Option("a", "short only");
        assertEquals("[ option: a  :: short only ]", optOnly.toString());

        Option optLong = new Option("a", "all", false, "short and long");
        assertEquals("[ option: a all  :: short and long ]", optLong.toString());

        Option optWithSingleArg = new Option("f", "file", true, "load file");
        assertEquals("[ option: f file  [ARG] :: load file ]", optWithSingleArg.toString());

        Option optWithArgs = new Option("m", "multi", false, "multi args");
        optWithArgs.setArgs(3);
        assertEquals("[ option: m multi [ARG...] :: multi args ]", optWithArgs.toString());

        Option optWithType = new Option("t", "typed option");
        optWithType.setType(Integer.class);
        assertEquals("[ option: t  :: typed option :: class java.lang.Integer ]", optWithType.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testArgsBoundaries()
    {
        Option option = new Option("b", "boundary testing");

        option.setArgs(Option.UNINITIALIZED);
        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(0);
        assertEquals(0, option.getArgs());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(1);
        assertEquals(1, option.getArgs());
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(2);
        assertEquals(2, option.getArgs());
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setArgs(Option.UNLIMITED_VALUES);
        assertEquals(Option.UNLIMITED_VALUES, option.getArgs());
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setArgs(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, option.getArgs());
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());
    }

    @Test(timeout = 4000)
    public void testAcceptsArgAndRequiresArgStates()
    {
        // 1. UNINITIALIZED without optionalArg
        Option uninit = new Option("u", "uninitialized");
        assertFalse(uninit.acceptsArg());
        assertFalse(uninit.requiresArg());

        // 2. UNINITIALIZED with optionalArg -> acceptsArg is true because numberOfArgs <= 0 (-1 <= 0)
        uninit.setOptionalArg(true);
        assertTrue(uninit.acceptsArg());
        assertFalse(uninit.requiresArg()); // optionalArg is true, requiresArg must be false

        // 3. Single required arg
        Option single = new Option("s", true, "single arg");
        assertTrue(single.acceptsArg());
        assertTrue(single.requiresArg());
        single.addValueForProcessing("item");
        assertFalse(single.acceptsArg());
        assertFalse(single.requiresArg());

        // 4. UNLIMITED_VALUES
        Option unlimited = new Option("m", "multiple");
        unlimited.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(unlimited.acceptsArg());
        assertTrue(unlimited.requiresArg());
        unlimited.addValueForProcessing("first");
        assertTrue(unlimited.acceptsArg()); // still accepts more
        assertFalse(unlimited.requiresArg()); // 1 is sufficient for unlimited
        unlimited.addValueForProcessing("second");
        assertTrue(unlimited.acceptsArg());
        assertFalse(unlimited.requiresArg());

        // 5. Zero args option
        Option zero = new Option("z", "zero args");
        zero.setArgs(0);
        assertFalse(zero.acceptsArg());
        assertFalse(zero.requiresArg());
    }

    @Test(timeout = 4000)
    public void testProcessValueWithSeparatorTokenCeiling()
    {
        // Case: numberOfArgs = 2, input has 2 separators (3 tokens) -> parsing stops at n-1 tokens
        Option option = new Option("D", "property");
        option.setArgs(2);
        option.setValueSeparator('=');

        option.addValueForProcessing("k=v=extra");
        assertEquals(2, option.getValuesList().size());
        assertEquals("k", option.getValue(0));
        assertEquals("v=extra", option.getValue(1));
    }

    @Test(timeout = 4000)
    public void testProcessValueWithUnlimitedSeparatorTokens()
    {
        Option option = new Option("D", "property");
        option.setArgs(Option.UNLIMITED_VALUES);
        option.setValueSeparator(',');

        option.addValueForProcessing("one,two,three,four");
        assertEquals(4, option.getValuesList().size());
        assertEquals("one", option.getValue(0));
        assertEquals("two", option.getValue(1));
        assertEquals("three", option.getValue(2));
        assertEquals("four", option.getValue(3));
    }

    @Test(timeout = 4000)
    public void testProcessValueSeparatorNotPresent()
    {
        Option option = new Option("p", true, "param");
        option.setValueSeparator(':');
        option.addValueForProcessing("singleTokenWithoutColon");
        assertEquals(1, option.getValuesList().size());
        assertEquals("singleTokenWithoutColon", option.getValue(0));
    }

    @Test(timeout = 4000)
    public void testLongOptionOnlyIdAndKey()
    {
        // opt is null -> longOpt must serve as key and ID
        Option option = new Option(null, "verbose", false, "verbosity flag");
        assertNull(option.getOpt());
        assertEquals("verbose", option.getLongOpt());
        assertEquals("verbose", option.getKey());
        assertEquals('v', option.getId());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets defects where Option's type metadata is lost, uninitialized,
     * or not correctly returned (e.g. OptionBuilderTest::testTwoCompleteOptions
     * and CommandLineTest::testGetParsedOptionValue).
     */
    @Test(timeout = 4000)
    public void testOptionTypeIntegrityAndPreservation()
    {
        Option option = new Option("f", "file", true, "target file");
        assertNull("Type should default to null", option.getType());

        option.setType(String.class);
        assertEquals("Type must be preserved after setType", String.class, option.getType());

        option.addValueForProcessing("sample.txt");
        assertEquals("sample.txt", option.getValue());
        assertEquals("Type must remain intact after value processing", String.class, option.getType());

        option.setType(java.io.File.class);
        assertEquals("Type can be changed to File.class", java.io.File.class, option.getType());

        option.setType(null);
        assertNull("Type can be reset to null", option.getType());
    }

    @Test(timeout = 4000)
    public void testNullOptVsNonNullOptEqualityBranches()
    {
        Option optNull1 = new Option(null, "config", false, "desc");
        Option optNull2 = new Option(null, "config", false, "desc");
        Option optNullDifferentLong = new Option(null, "other", false, "desc");
        Option optNonNull = new Option("c", "config", false, "desc");

        assertTrue("Null opts with identical longOpts must be equal", optNull1.equals(optNull2));
        assertTrue("Symmetric equality for null opts", optNull2.equals(optNull1));
        assertEquals(optNull1.hashCode(), optNull2.hashCode());

        assertFalse("Null opt vs non-null opt must not be equal", optNull1.equals(optNonNull));
        assertFalse("Non-null opt vs null opt must not be equal", optNonNull.equals(optNull1));
        assertFalse("Null opts with different longOpts must not be equal", optNull1.equals(optNullDifferentLong));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorValidatesIllegalSingleCharacter()
    {
        new Option("?", "illegal question mark option");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorValidatesIllegalSpecialCharacters()
    {
        new Option("foo!bar", "illegal exclamation mark");
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddValueForProcessingWhenUninitializedThrowsException()
    {
        Option option = new Option("n", "no args allowed");
        option.addValueForProcessing("illegalValue");
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddValueExceedingCapacityThrowsException()
    {
        Option option = new Option("s", true, "single arg");
        option.addValueForProcessing("first");
        // Capacity is 1; second insertion must trigger RuntimeException("Cannot add value, list full.")
        option.addValueForProcessing("second");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testDeprecatedAddValueThrowsUnsupportedOperationException()
    {
        Option option = new Option("o", true, "option");
        option.addValue("value");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueIndexNegativeThrowsException()
    {
        Option option = new Option("a", true, "option");
        option.addValueForProcessing("val");
        option.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueIndexOutOfUpperBoundThrowsException()
    {
        Option option = new Option("a", true, "option");
        option.addValueForProcessing("val");
        option.getValue(1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContractComprehensive()
    {
        Option optA1 = new Option("a", "alpha", false, "desc");
        Option optA2 = new Option("a", "alpha", false, "desc");
        Option optADiffLong = new Option("a", "alternate", false, "desc");
        Option optANoLong = new Option("a", false, "desc");
        Option optB = new Option("b", "alpha", false, "desc");

        // Identity
        assertTrue(optA1.equals(optA1));

        // Null and different type
        assertFalse(optA1.equals(null));
        assertFalse(optA1.equals("different-type"));

        // Value equality
        assertTrue(optA1.equals(optA2));
        assertTrue(optA2.equals(optA1));
        assertEquals(optA1.hashCode(), optA2.hashCode());

        // Same opt, different longOpt
        assertFalse(optA1.equals(optADiffLong));
        assertFalse(optADiffLong.equals(optA1));

        // Same opt, null longOpt vs non-null longOpt
        assertFalse(optA1.equals(optANoLong));
        assertFalse(optANoLong.equals(optA1));

        // Different opt, same longOpt
        assertFalse(optA1.equals(optB));
        assertFalse(optB.equals(optA1));

        // Both opt and longOpt null
        Option nullAll1 = new Option(null, null, false, "desc");
        Option nullAll2 = new Option(null, null, false, "desc");
        assertTrue(nullAll1.equals(nullAll2));
        assertEquals(nullAll1.hashCode(), nullAll2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneDeepCopiesValues()
    {
        Option original = new Option("c", "cloneable", true, "description");
        original.setArgs(2);
        original.addValueForProcessing("val1");

        Option cloned = (Option) original.clone();
        assertNotSame("Clone must produce a distinct object reference", original, cloned);
        assertEquals("Clone must be equal to original", original, cloned);
        assertEquals("Clone hashCode must match original", original.hashCode(), cloned.hashCode());

        // Modifying cloned values must not affect original
        cloned.addValueForProcessing("val2");
        assertEquals(1, original.getValuesList().size());
        assertEquals(2, cloned.getValuesList().size());

        original.clearValues();
        assertEquals(0, original.getValuesList().size());
        assertEquals(2, cloned.getValuesList().size());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception
    {
        Option original = new Option("s", "serial", true, "serializable option");
        original.setType(String.class);
        original.setRequired(true);
        original.setOptionalArg(true);
        original.setValueSeparator(':');
        original.addValueForProcessing("data");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Option deserialized = (Option) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.getOpt(), deserialized.getOpt());
        assertEquals(original.getLongOpt(), deserialized.getLongOpt());
        assertEquals(original.getDescription(), deserialized.getDescription());
        assertEquals(original.getType(), deserialized.getType());
        assertEquals(original.isRequired(), deserialized.isRequired());
        assertEquals(original.hasOptionalArg(), deserialized.hasOptionalArg());
        assertEquals(original.getValueSeparator(), deserialized.getValueSeparator());
        assertEquals(original.getValue(), deserialized.getValue());
    }
}