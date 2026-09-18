package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 *
 * Target: org.apache.commons.cli.Option
 *
 * Decision / Branch Matrix Covered:
 * 1. Constructor Overloads:
 *    - Option(opt, description) -> delegates to 4-arg constructor with longOpt=null, hasArg=false.
 *    - Option(opt, hasArg, description) -> delegates to 4-arg constructor with longOpt=null.
 *    - Option(opt, longOpt, hasArg, description):
 *        * OptionValidator.validateOption(opt) called (validated).
 *        * hasArg == true -> numberOfArgs = 1.
 *        * hasArg == false -> numberOfArgs = UNINITIALIZED (-1).
 * 2. getId() / getKey():
 *    - opt != null -> returns opt.charAt(0) / opt.
 *    - opt == null -> returns longOpt.charAt(0) / longOpt.
 * 3. hasArg() / hasArgs():
 *    - numberOfArgs > 0, numberOfArgs == UNLIMITED_VALUES (-2), numberOfArgs == UNINITIALIZED (-1), numberOfArgs == 0.
 * 4. hasLongOpt():
 *    - longOpt != null vs. longOpt == null.
 * 5. hasOptionalArg() / setOptionalArg():
 *    - optionalArg = true vs. false.
 * 6. hasArgName():
 *    - argName != null && argName.length() > 0.
 *    - Known Defect: argName initialized to "arg" instead of null/empty, causing hasArgName() to incorrectly
 *      return true for options that never had an argName set, overriding HelpFormatter's default arg name.
 * 7. hasValueSeparator() / setValueSeparator() / getValueSeparator():
 *    - valuesep > 0 vs. valuesep == 0.
 * 8. addValueForProcessing(value) & processValue(value) & add(value):
 *    - numberOfArgs == UNINITIALIZED -> throws RuntimeException("NO_ARGS_ALLOWED").
 *    - hasValueSeparator() == true:
 *        * Token parsing loop: index != -1 vs. index == -1.
 *        * Loop break condition: values.size() == (numberOfArgs - 1).
 *        * Substring token addition and final token addition.
 *    - hasValueSeparator() == false: direct add(value).
 *    - add(value) guard: acceptsArg() == false -> throws RuntimeException("Cannot add value, list full.").
 * 9. getValue() / getValue(index) / getValue(defaultValue) / getValues() / getValuesList():
 *    - hasNoValues() == true -> returns null (or defaultValue).
 *    - hasNoValues() == false -> returns first element or indexed element or array of values.
 *    - IndexOutOfBoundsException on getValue(index) out of bounds.
 * 10. toString():
 *    - opt only, opt + longOpt, hasArgs ([ARG...]), hasArg ([ARG]), type != null, description.
 * 11. equals() & hashCode():
 *    - this == o, o == null, o.getClass() != getClass().
 *    - opt == null vs. opt != null (matching and non-matching).
 *    - longOpt == null vs. longOpt != null (matching and non-matching).
 *    - Symmetric & Transitive equivalence contracts.
 * 12. clone():
 *    - Deep copy verification: cloned instance has cloned values list independent of original.
 * 13. clearValues():
 *    - Clears internal values list; subsequent getValue() returns null.
 * 14. acceptsArg() & requiresArg():
 *    - acceptsArg(): (hasArg() || hasArgs() || hasOptionalArg()) && (numberOfArgs <= 0 || values.size() < numberOfArgs).
 *    - requiresArg():
 *        * optionalArg == true -> returns false immediately.
 *        * numberOfArgs == UNLIMITED_VALUES -> returns values.size() < 1.
 *        * otherwise -> returns acceptsArg().
 * 15. addValue(value):
 *    - Deprecated method -> always throws UnsupportedOperationException.
 * 16. Serialization:
 *    - Option is Serializable -> round-trip serialization preserves state.
 */
public class OptionGeminiTest
{
    /* ==================================================================== */
    /* Partition A: Core Functional Logic & State Transitions               */
    /* ==================================================================== */

    @Test(timeout = 4000)
    public void testTwoArgConstructorDefaults()
    {
        Option option = new Option("a", "Alpha option");
        assertEquals("a", option.getOpt());
        assertEquals("Alpha option", option.getDescription());
        assertNull(option.getLongOpt());
        assertFalse(option.hasLongOpt());
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(Option.UNINITIALIZED, option.getArgs());
        assertFalse(option.isRequired());
        assertFalse(option.hasOptionalArg());
        assertFalse(option.hasValueSeparator());
        assertEquals((char) 0, option.getValueSeparator());
        assertNull(option.getType());
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertTrue(option.getValuesList().isEmpty());
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructorWithArg()
    {
        Option option = new Option("b", true, "Beta option with arg");
        assertEquals("b", option.getOpt());
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());
        assertEquals(1, option.getArgs());
        assertEquals("Beta option with arg", option.getDescription());
        assertNull(option.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorComplete()
    {
        Option option = new Option("c", "charlie", true, "Charlie option");
        assertEquals("c", option.getOpt());
        assertEquals("charlie", option.getLongOpt());
        assertTrue(option.hasLongOpt());
        assertTrue(option.hasArg());
        assertEquals(1, option.getArgs());
        assertEquals("Charlie option", option.getDescription());
    }

    @Test(timeout = 4000)
    public void testGetIdAndGetKey()
    {
        Option shortOpt = new Option("k", "key-option");
        assertEquals('k', shortOpt.getId());
        assertEquals("k", shortOpt.getKey());

        Option longOnly = new Option(null, "long-key", false, "long only key");
        assertEquals('l', longOnly.getId());
        assertEquals("long-key", longOnly.getKey());
    }

    @Test(timeout = 4000)
    public void testSettersAndGettersStateTransitions()
    {
        Option option = new Option("o", "Original description");
        option.setDescription("Updated description");
        assertEquals("Updated description", option.getDescription());

        option.setLongOpt("output");
        assertEquals("output", option.getLongOpt());
        assertTrue(option.hasLongOpt());

        option.setRequired(true);
        assertTrue(option.isRequired());
        option.setRequired(false);
        assertFalse(option.isRequired());

        option.setOptionalArg(true);
        assertTrue(option.hasOptionalArg());
        option.setOptionalArg(false);
        assertFalse(option.hasOptionalArg());

        option.setType(Integer.class);
        assertEquals(Integer.class, option.getType());

        option.setArgs(3);
        assertEquals(3, option.getArgs());
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setValueSeparator('=');
        assertEquals('=', option.getValueSeparator());
        assertTrue(option.hasValueSeparator());
    }

    @Test(timeout = 4000)
    public void testAddValueForProcessingSingleArg()
    {
        Option option = new Option("p", true, "Port");
        assertTrue(option.acceptsArg());
        assertTrue(option.requiresArg());

        option.addValueForProcessing("8080");

        assertEquals("8080", option.getValue());
        assertEquals("8080", option.getValue(0));
        assertEquals("8080", option.getValue("defaultPort"));
        assertNotNull(option.getValues());
        assertEquals(1, option.getValues().length);
        assertEquals("8080", option.getValues()[0]);
        assertEquals(1, option.getValuesList().size());
        assertFalse(option.acceptsArg());
        assertFalse(option.requiresArg());
    }

    @Test(timeout = 4000)
    public void testAddValueForProcessingMultipleArgsWithoutSeparator()
    {
        Option option = new Option("m", "multiple", true, "Multi arg");
        option.setArgs(2);

        assertTrue(option.acceptsArg());
        assertTrue(option.requiresArg());

        option.addValueForProcessing("val1");
        assertTrue(option.acceptsArg());
        assertTrue(option.requiresArg());

        option.addValueForProcessing("val2");
        assertFalse(option.acceptsArg());
        assertFalse(option.requiresArg());

        String[] values = option.getValues();
        assertNotNull(values);
        assertEquals(2, values.length);
        assertEquals("val1", values[0]);
        assertEquals("val2", values[1]);
    }

    @Test(timeout = 4000)
    public void testValueSeparatorParsing()
    {
        Option option = new Option("D", true, "Define property");
        option.setArgs(2);
        option.setValueSeparator('=');

        option.addValueForProcessing("key=value");
        assertEquals("key", option.getValue(0));
        assertEquals("value", option.getValue(1));
        assertEquals(2, option.getValuesList().size());
        assertFalse(option.acceptsArg());
    }

    @Test(timeout = 4000)
    public void testValueSeparatorWithMoreSeparatorsThanArgs()
    {
        Option option = new Option("D", true, "Property with multiple equals");
        option.setArgs(2);
        option.setValueSeparator('=');

        // Remaining tokens after numberOfArgs - 1 should stay intact in the last value
        option.addValueForProcessing("key=val1=val2");
        assertEquals(2, option.getValuesList().size());
        assertEquals("key", option.getValue(0));
        assertEquals("val1=val2", option.getValue(1));
    }

    @Test(timeout = 4000)
    public void testClearValuesResetsOption()
    {
        Option option = new Option("c", true, "Clear test");
        option.addValueForProcessing("testVal");
        assertEquals("testVal", option.getValue());

        option.clearValues();
        assertNull(option.getValue());
        assertNull(option.getValues());
        assertTrue(option.getValuesList().isEmpty());
        assertTrue(option.acceptsArg());
    }

    @Test(timeout = 4000)
    public void testToStringFormatting()
    {
        Option simple = new Option("s", "Simple");
        assertEquals("[ option: s  :: Simple ]", simple.toString());

        Option withLong = new Option("s", "simple", false, "Simple with long");
        assertEquals("[ option: s simple  :: Simple with long ]", withLong.toString());

        Option withArg = new Option("a", "arg-opt", true, "Option with arg");
        assertEquals("[ option: a arg-opt  [ARG] :: Option with arg ]", withArg.toString());

        Option withArgs = new Option("m", "multi-opt", false, "Option with args");
        withArgs.setArgs(3);
        assertEquals("[ option: m multi-opt [ARG...] :: Option with args ]", withArgs.toString());

        Option withType = new Option("t", "Typed option");
        withType.setType(String.class);
        assertTrue(withType.toString().contains(":: class java.lang.String ]"));
    }

    /* ==================================================================== */
    /* Partition B: Boundary Value Analysis (BVA) & Extremes                */
    /* ==================================================================== */

    @Test(timeout = 4000)
    public void testUnlimitedValuesBehavior()
    {
        Option option = new Option("u", "unlimited");
        option.setArgs(Option.UNLIMITED_VALUES);
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());
        assertTrue(option.requiresArg());
        assertTrue(option.acceptsArg());

        option.addValueForProcessing("v1");
        assertFalse(option.requiresArg()); // requires at least 1 value
        assertTrue(option.acceptsArg());   // can accept more

        option.addValueForProcessing("v2");
        option.addValueForProcessing("v3");
        assertEquals(3, option.getValuesList().size());
        assertTrue(option.acceptsArg());
    }

    @Test(timeout = 4000)
    public void testGetValueDefaultFallback()
    {
        Option option = new Option("d", true, "Default test");
        assertEquals("fallback", option.getValue("fallback"));

        option.addValueForProcessing("actual");
        assertEquals("actual", option.getValue("fallback"));
    }

    @Test(timeout = 4000)
    public void testHasArgBoundaryZeroAndNegative()
    {
        Option option = new Option("z", "zero args");
        option.setArgs(0);
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(1);
        assertTrue(option.hasArg());
        assertFalse(option.hasArgs());

        option.setArgs(2);
        assertTrue(option.hasArg());
        assertTrue(option.hasArgs());

        option.setArgs(Option.UNINITIALIZED);
        assertFalse(option.hasArg());
        assertFalse(option.hasArgs());
    }

    @Test(timeout = 4000)
    public void testRequiresArgWithOptionalArg()
    {
        Option option = new Option("opt", true, "Optional argument");
        option.setOptionalArg(true);
        // An option with optionalArg == true should never require an argument
        assertFalse(option.requiresArg());

        option.setArgs(Option.UNLIMITED_VALUES);
        assertFalse(option.requiresArg());
    }

    @Test(timeout = 4000)
    public void testAcceptsArgWithOptionalArgWhenValuesEmpty()
    {
        Option option = new Option("o", "optional flag");
        option.setArgs(1);
        option.setOptionalArg(true);
        assertTrue(option.acceptsArg());

        option.addValueForProcessing("val");
        assertFalse(option.acceptsArg());
    }

    /* ==================================================================== */
    /* Partition C: Defect-Targeted Branch Zone                             */
    /* Defect: HelpFormatterTest::testDefaultArgName                        */
    /* Root cause: argName is default initialized to "arg" in Option,       */
    /* causing hasArgName() to return true, which prevents HelpFormatter    */
    /* from using its configured defaultArgName (e.g. "argument").          */
    /* ==================================================================== */

    @Test(timeout = 4000)
    public void testArgNameDefaultStateTargetsDefect()
    {
        Option option = new Option("f", true, "Targeting default argName defect");

        // When newly instantiated without explicitly calling setArgName,
        // an Option should NOT report having a custom argName set.
        // On the defective version, argName is initialized to "arg" and hasArgName() returns true!
        assertFalse("Option should not report hasArgName() as true unless explicitly configured",
                option.hasArgName());
        assertNull("Option argName should default to null so external formatters can supply defaults",
                option.getArgName());
    }

    @Test(timeout = 4000)
    public void testExplicitArgNameConfiguration()
    {
        Option option = new Option("x", true, "Explicit arg name test");
        option.setArgName("custom");
        assertTrue(option.hasArgName());
        assertEquals("custom", option.getArgName());

        option.setArgName("");
        assertFalse(option.hasArgName());

        option.setArgName(null);
        assertFalse(option.hasArgName());
        assertNull(option.getArgName());
    }

    /* ==================================================================== */
    /* Partition D: Exception & Defensive Guard Paths                       */
    /* ==================================================================== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidOptCharacterThrowsException()
    {
        new Option("invalid opt with space", "description");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidSpecialCharOptThrowsException()
    {
        new Option("@", "special char not allowed");
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddValueForProcessingWhenUninitializedThrowsException()
    {
        Option option = new Option("n", "No args allowed option");
        // numberOfArgs is UNINITIALIZED (-1)
        option.addValueForProcessing("forbiddenValue");
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testAddValueWhenCapacityExceededThrowsException()
    {
        Option option = new Option("s", true, "Single arg option");
        option.addValueForProcessing("v1");
        option.addValueForProcessing("v2"); // List is full, must throw RuntimeException
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueIndexNegativeThrowsException()
    {
        Option option = new Option("i", true, "Index test");
        option.addValueForProcessing("first");
        option.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetValueIndexTooLargeThrowsException()
    {
        Option option = new Option("i", true, "Index test");
        option.addValueForProcessing("first");
        option.getValue(1);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testDeprecatedAddValueThrowsUnsupportedOperationException()
    {
        Option option = new Option("d", true, "Deprecated test");
        option.addValue("value");
    }

    /* ==================================================================== */
    /* Partition E: Object Lifecycle, Contract Integrity & Serialization     */
    /* ==================================================================== */

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract()
    {
        Option opt1 = new Option("a", "alpha", false, "Description");
        Option opt1Same = new Option("a", "alpha", false, "Different description ignored");
        Option opt2 = new Option("a", "beta", false, "Description");
        Option opt3 = new Option("b", "alpha", false, "Description");
        Option optNullLong1 = new Option("a", null, false, "Desc");
        Option optNullLong2 = new Option("a", null, false, "Desc");
        Option optLongOnly1 = new Option(null, "alpha", false, "Desc");
        Option optLongOnly2 = new Option(null, "alpha", false, "Desc");

        // Reflexivity
        assertEquals(opt1, opt1);
        assertEquals(opt1.hashCode(), opt1.hashCode());

        // Symmetry
        assertEquals(opt1, opt1Same);
        assertEquals(opt1Same, opt1);
        assertEquals(opt1.hashCode(), opt1Same.hashCode());

        // Difference in longOpt
        assertNotEquals(opt1, opt2);
        assertNotEquals(opt2, opt1);

        // Difference in opt
        assertNotEquals(opt1, opt3);
        assertNotEquals(opt3, opt1);

        // Null longOpts equality
        assertEquals(optNullLong1, optNullLong2);
        assertEquals(optNullLong1.hashCode(), optNullLong2.hashCode());
        assertNotEquals(opt1, optNullLong1);
        assertNotEquals(optNullLong1, opt1);

        // Null opt equality (long-only options)
        assertEquals(optLongOnly1, optLongOnly2);
        assertEquals(optLongOnly1.hashCode(), optLongOnly2.hashCode());
        assertNotEquals(opt1, optLongOnly1);
        assertNotEquals(optLongOnly1, opt1);

        // Non-Option or null comparisons
        assertNotNull(opt1);
        assertNotEquals("not an option", opt1);
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity()
    {
        Option original = new Option("c", "cloneable", true, "Clone test");
        original.addValueForProcessing("initialVal");

        Option cloned = (Option) original.clone();
        assertNotSame(original, cloned);
        assertEquals(original, cloned);
        assertEquals(original.getValue(), cloned.getValue());

        // Deep copy verification: modifying values of cloned should not affect original
        cloned.clearValues();
        assertEquals(1, original.getValuesList().size());
        assertEquals("initialVal", original.getValue());
        assertNull(cloned.getValue());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception
    {
        Option original = new Option("s", "serial", true, "Serializable test");
        original.setRequired(true);
        original.setArgs(2);
        original.setValueSeparator(',');
        original.addValueForProcessing("v1,v2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Option deserialized = (Option) ois.readObject();
        ois.close();

        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getOpt(), deserialized.getOpt());
        assertEquals(original.getLongOpt(), deserialized.getLongOpt());
        assertEquals(original.getArgs(), deserialized.getArgs());
        assertEquals(original.isRequired(), deserialized.isRequired());
        assertEquals(original.getValueSeparator(), deserialized.getValueSeparator());
        assertEquals(2, deserialized.getValuesList().size());
        assertEquals("v1", deserialized.getValue(0));
        assertEquals("v2", deserialized.getValue(1));
    }
}