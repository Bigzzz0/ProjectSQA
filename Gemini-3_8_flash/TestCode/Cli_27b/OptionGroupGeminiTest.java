package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Method   | Branch / Condition                          | Test Target & Defect Focus
 * ---------------------------------------------------------------------------------------------------
 * addOption()     | Normal insertion                            | Return 'this' (fluent), store in map
 * getNames()      | Key set retrieval                           | Verify matches keys of added options
 * getOptions()    | Collection retrieval                        | Verify matches added Option instances
 * setSelected()   | option == null                              | Branch 1: Reset selected to null
 * setSelected()   | selected == null                            | Branch 2: First selection sets selected key
 * setSelected()   | selected.equals(option.getKey()/getOpt())   | Branch 3: Reselecting same option succeeds
 * setSelected()   | Long-only Option (opt == null)              | DEFECT TARGET: getOpt() is null causing
 *                 |                                             | selected to become null instead of key!
 * setSelected()   | selected != null && diff option             | Branch 4: Throws AlreadySelectedException
 * isRequired()    | default false, setter modifies              | Boolean flag verification
 * toString()      | empty group                                 | Yields "[]"
 * toString()      | single short option (opt != null)           | Branch: "-" prefix + opt + " " + desc
 * toString()      | single long-only option (opt == null)       | Branch: "--" prefix + longOpt + " " + desc
 * toString()      | multiple options                            | Checks delimiter ", " and both elements
 * Serialization   | serialVersionUID = 1L                       | Complete round-trip deep state recovery
 * ---------------------------------------------------------------------------------------------------
 */
public class OptionGroupGeminiTest
{
    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionFluentAndRetrieval()
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "Option A description");

        OptionGroup returnedGroup = group.addOption(optA);
        assertSame("addOption should support fluent chaining returning this", group, returnedGroup);

        Collection names = group.getNames();
        assertEquals(1, names.size());
        assertTrue("getNames must contain 'a'", names.contains("a"));

        Collection options = group.getOptions();
        assertEquals(1, options.size());
        assertTrue("getOptions must contain optA", options.contains(optA));
    }

    @Test(timeout = 4000)
    public void testRequiredStateTransitions()
    {
        OptionGroup group = new OptionGroup();
        assertFalse("OptionGroup should default to required=false", group.isRequired());

        group.setRequired(true);
        assertTrue("OptionGroup should reflect required=true", group.isRequired());

        group.setRequired(false);
        assertFalse("OptionGroup should reflect required=false", group.isRequired());
    }

    @Test(timeout = 4000)
    public void testSetSelectedFirstTimeWithShortOption() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "Short option A");
        group.addOption(optA);

        assertNull("Selected should initially be null", group.getSelected());

        group.setSelected(optA);
        assertEquals("Selected must match option name", "a", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testReselectSameShortOption() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "Short option A");
        group.addOption(optA);

        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Reselecting the same option instance should not throw
        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Reselecting an equivalent option with the same opt should not throw
        Option optA2 = new Option("a", "Another instance of option A");
        group.setSelected(optA2);
        assertEquals("a", group.getSelected());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetSelectedNullResetsSelection() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "Option A");
        group.addOption(optA);

        group.setSelected(optA);
        assertEquals("a", group.getSelected());

        // Reset selected by passing null
        group.setSelected(null);
        assertNull("Selected must be null after passing null to setSelected", group.getSelected());

        // After reset, selecting a different option should now succeed
        Option optB = new Option("b", "Option B");
        group.addOption(optB);
        group.setSelected(optB);
        assertEquals("b", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testToStringEmptyGroup()
    {
        OptionGroup group = new OptionGroup();
        assertEquals("Empty group must stringify to '[]'", "[]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleShortOption()
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "Option A desc");
        group.addOption(optA);

        assertEquals("[-a Option A desc]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringSingleLongOnlyOption()
    {
        OptionGroup group = new OptionGroup();
        Option optLong = new Option(null, "verbose", false, "Verbose output");
        group.addOption(optLong);

        assertEquals("[--verbose Verbose output]", group.toString());
    }

    @Test(timeout = 4000)
    public void testToStringMultipleOptions()
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "desc a");
        Option optB = new Option(null, "bar", false, "desc bar");
        group.addOption(optA);
        group.addOption(optB);

        String result = group.toString();
        assertTrue("Must start with '['", result.startsWith("["));
        assertTrue("Must end with ']'", result.endsWith("]"));
        assertTrue("Must contain optA rendering", result.contains("-a desc a"));
        assertTrue("Must contain optB rendering", result.contains("--bar desc bar"));
        assertTrue("Must contain item separator", result.contains(", "));
    }

    @Test(timeout = 4000)
    public void testToStringNullDescription()
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", null);
        group.addOption(optA);

        assertEquals("[-a null]", group.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // Targets: BasicParserTest/GnuParserTest/PosixParserTest testOptionGroupLong
    // Defect: setSelected(longOnlyOption) sets selected to null instead of "bar".
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptionGroupLongSelectedBehavior() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option longOnly = new Option(null, "bar", false, "bar description");
        group.addOption(longOnly);

        // When selecting an option that only has a longOpt (opt == null),
        // getSelected() must return the option's key ("bar"), NOT null.
        group.setSelected(longOnly);
        assertEquals("selected option expected:<bar> but was:<null>", "bar", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testOptionGroupLongReselection() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option longOnly = new Option(null, "bar", false, "bar description");
        group.addOption(longOnly);

        group.setSelected(longOnly);
        // Reselecting the same long-only option must succeed and retain the key
        group.setSelected(longOnly);
        assertEquals("bar", group.getSelected());
    }

    @Test(timeout = 4000)
    public void testOptionGroupLongConflictWithAnotherOption() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option longOnly = new Option(null, "bar", false, "bar description");
        Option optFoo = new Option("f", "foo", false, "foo description");
        group.addOption(longOnly);
        group.addOption(optFoo);

        group.setSelected(longOnly);
        assertEquals("bar", group.getSelected());

        try
        {
            group.setSelected(optFoo);
            fail("Expected AlreadySelectedException when selecting another option after a long option");
        }
        catch (AlreadySelectedException expected)
        {
            assertSame(group, expected.getOptionGroup());
            assertSame(optFoo, expected.getOption());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testConflictThrowsAlreadySelectedException() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "desc A");
        Option optB = new Option("b", "desc B");
        group.addOption(optA);
        group.addOption(optB);

        group.setSelected(optA);

        try
        {
            group.setSelected(optB);
            fail("Selecting optB when optA is selected must throw AlreadySelectedException");
        }
        catch (AlreadySelectedException ex)
        {
            assertSame("Exception must reference the OptionGroup", group, ex.getOptionGroup());
            assertSame("Exception must reference the conflicting Option", optB, ex.getOption());
        }
    }

    @Test(timeout = 4000)
    public void testConflictBetweenTwoLongOnlyOptions() throws AlreadySelectedException
    {
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option(null, "first", false, "first option");
        Option opt2 = new Option(null, "second", false, "second option");
        group.addOption(opt1);
        group.addOption(opt2);

        group.setSelected(opt1);

        try
        {
            group.setSelected(opt2);
            fail("Selecting opt2 after opt1 must throw AlreadySelectedException");
        }
        catch (AlreadySelectedException ex)
        {
            assertSame(group, ex.getOptionGroup());
            assertSame(opt2, ex.getOption());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception
    {
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "alpha", false, "Option Alpha");
        Option optB = new Option(null, "beta", false, "Option Beta");
        group.addOption(optA);
        group.addOption(optB);
        group.setRequired(true);
        group.setSelected(optA);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(group);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        OptionGroup deserialized = (OptionGroup) ois.readObject();
        ois.close();

        assertNotNull("Deserialized object must not be null", deserialized);
        assertTrue("Required flag must be preserved", deserialized.isRequired());
        assertEquals("Selected value must be preserved", "a", deserialized.getSelected());
        assertEquals("Names size must match", 2, deserialized.getNames().size());
        assertTrue("Names must contain 'a'", deserialized.getNames().contains("a"));
        assertTrue("Names must contain 'beta'", deserialized.getNames().contains("beta"));
        assertEquals("Options count must match", 2, deserialized.getOptions().size());
    }
}