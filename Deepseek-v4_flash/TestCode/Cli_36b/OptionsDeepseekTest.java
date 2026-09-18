package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * White-box JUnit 4 test suite for the Options class.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - addOption(String, String, boolean, String) -> Option creation and storage
 *   - addOption(Option) -> registration in shortOpts, longOpts, requiredOpts
 *   - addOptionGroup(OptionGroup) -> group requirement propagation, option removal from required
 *   - getOption(String) -> lookup via shortOpts, then longOpts
 *   - hasOption/hasLongOption/hasShortOption -> correct identification
 *   - getMatchingOptions -> perfect match vs. prefix matching
 *   - getOptions/helpOptions -> iteration order (LinkedHashMap) and unmodifiability
 *   - getRequiredOptions -> read-only list
 *   - getOptionGroup -> group association
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null, empty, and leading-hyphen strings for option names
 *   - duplicate short/long keys – overwrite behavior
 *   - Option with only long name (short == null)
 *   - Option with only short name (long == null)
 *   - Required option added twice
 *   - OptionGroup with required true/false
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known bug BugCLI266: insertion order NOT preserved when using HashMap.
 *     Test that getOptions() / helpOptions() returns options in insertion order.
 *     (Fixed version uses LinkedHashMap, but buggy version uses HashMap.)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - No explicit exceptions thrown by Options methods under normal usage.
 *     However, Option constructor may throw IllegalArgumentException for null/empty opt.
 *     We test by verifying that getOption returns null for missing keys.
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - toString() non-null and contains relevant state
 *   - serialization UID present (not tested directly)
 *
 * Every test is guarded with @Test(timeout = 4000) for determinism.
 */
public class OptionsDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testAddOptionShortAndLong() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "Alpha option");
        assertTrue("hasOption('a')", opts.hasOption("a"));
        assertTrue("hasOption('alpha')", opts.hasShortOption("a"));
        assertTrue("hasLongOption('alpha')", opts.hasLongOption("alpha"));
        Option opt = opts.getOption("a");
        assertNotNull("getOption('a')", opt);
        assertEquals("short opt", 'a', opt.getOpt().charAt(0));
        assertEquals("long opt", "alpha", opt.getLongOpt());
        assertFalse("hasArg", opt.hasArg());
    }

    @Test(timeout = 4000)
    public void testAddOptionOnlyShort() {
        Options opts = new Options();
        opts.addOption("x", "description");
        assertTrue("hasOption('x')", opts.hasOption("x"));
        assertTrue("hasShortOption('x')", opts.hasShortOption("x"));
        assertFalse("hasLongOption misses", opts.hasLongOption("x"));
        Option opt = opts.getOption("x");
        assertEquals("short opt", "x", opt.getOpt());
        assertNull("long opt null", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testAddOptionOnlyLong() {
        Options opts = new Options();
        // Option with only long name: short opt is null
        Option longOnly = new Option(null, "longonly", false, "long only");
        opts.addOption(longOnly);
        assertTrue("hasOption('longonly')", opts.hasOption("longonly"));
        assertTrue("hasLongOption('longonly')", opts.hasLongOption("longonly"));
        assertFalse("hasShortOption false", opts.hasShortOption("longonly"));
        assertEquals("getOption returns same", longOnly, opts.getOption("longonly"));
    }

    @Test(timeout = 4000)
    public void testGetOptionWithHyphens() {
        Options opts = new Options();
        opts.addOption("b", "beta", false, "Beta");
        assertEquals("strip single hyphen", 'b', opts.getOption("-b").getOpt().charAt(0));
        assertEquals("strip double hyphen", 'b', opts.getOption("--b").getOpt().charAt(0));
        assertEquals("strip more hyphens", 'b', opts.getOption("---b").getOpt().charAt(0));
    }

    @Test(timeout = 4000)
    public void testHasOptionWithHyphens() {
        Options opts = new Options();
        opts.addOption("c", "charlie", false, "Charlie");
        assertTrue("hasOption('-c')", opts.hasOption("-c"));
        assertTrue("hasOption('--charlie')", opts.hasOption("--charlie"));
        assertFalse("hasOption('nonexistent')", opts.hasOption("zzz"));
    }

    @Test(timeout = 4000)
    public void testRequiredOption() {
        Options opts = new Options();
        opts.addOption("r", "required", false, "Required option");
        Option opt = new Option("r", "required", true, "Required with arg");
        opt.setRequired(true);
        opts.addOption(opt);
        List<?> requireds = opts.getRequiredOptions();
        assertEquals("one required", 1, requireds.size());
        assertTrue("contains key 'r'", requireds.contains("r"));
    }

    @Test(timeout = 4000)
    public void testAddOptionGroupRequired() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "Alpha"));
        group.addOption(new Option("b", "beta", false, "Beta"));
        opts.addOptionGroup(group);
        // Now requiredOpts should contain the group
        List<?> requireds = opts.getRequiredOptions();
        assertEquals("one required entry", 1, requireds.size());
        assertTrue("required entry is the group", requireds.get(0) instanceof OptionGroup);
        // Individual options should not be required
        assertFalse("alpha not required", opts.getOption("a").isRequired());
        assertFalse("beta not required", opts.getOption("b").isRequired());
    }

    @Test(timeout = 4000)
    public void testAddOptionGroupNotRequired() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "xray", false, "X-ray"));
        group.addOption(new Option("y", "yankee", false, "Yankee"));
        opts.addOptionGroup(group);
        List<?> requireds = opts.getRequiredOptions();
        assertTrue("no required entries", requireds.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsPerfectMatch() {
        Options opts = new Options();
        opts.addOption("l", "long", false, "Long option");
        List<String> matches = opts.getMatchingOptions("long");
        assertEquals("single perfect match", 1, matches.size());
        assertEquals("match is 'long'", "long", matches.get(0));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsPrefix() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "Alpha");
        opts.addOption("b", "beta", false, "Beta");
        opts.addOption("c", "gamma", false, "Gamma"); // not matching 'al'
        List<String> matches = opts.getMatchingOptions("al");
        assertEquals("one prefix match", 1, matches.size());
        assertEquals("match is 'alpha'", "alpha", matches.get(0));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsNoMatch() {
        Options opts = new Options();
        opts.addOption("z", "zulu", false, "Zulu");
        List<String> matches = opts.getMatchingOptions("nonexistent");
        assertTrue("empty list for no match", matches.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsStripHyphens() {
        Options opts = new Options();
        opts.addOption("n", "november", false, "November");
        List<String> matches = opts.getMatchingOptions("--november");
        assertEquals("perfect match after stripping", 1, matches.size());
        assertEquals("match 'november'", "november", matches.get(0));
    }

    @Test(timeout = 4000)
    public void testGetOptionGroup() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        Option opt1 = new Option("g1", "group1", false, "Group1 opt");
        group.addOption(opt1);
        opts.addOptionGroup(group);
        OptionGroup retrieved = opts.getOptionGroup(opt1);
        assertNotNull("group retrieved", retrieved);
        assertSame("same group instance", group, retrieved);
    }

    @Test(timeout = 4000)
    public void testGetOptionGroupForNonGroupedOption() {
        Options opts = new Options();
        Option opt = new Option("s", "solo", false, "Solo");
        opts.addOption(opt);
        assertNull("no group for solo option", opts.getOptionGroup(opt));
    }

    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiable() {
        Options opts = new Options();
        opts.addOption("u", "uniform", false, "Uniform");
        Collection<Option> options = opts.getOptions();
        try {
            options.clear();
            fail("should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testHelpOptionsReturnsSnapshot() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "Alpha");
        List<Option> helpList1 = opts.helpOptions();
        opts.addOption("b", "beta", false, "Beta");
        List<Option> helpList2 = opts.helpOptions();
        assertEquals("help list after add", 2, helpList2.size());
        assertEquals("first list size before add", 1, helpList1.size());
    }

    // ========== Partition B: Boundary & Extremes ==========

    @Test(timeout = 4000)
    public void testAddAndRetrieveWithNullOptInOption() {
        Options opts = new Options();
        // Option with null short opt and null long opt – probably not allowed by constructor
        // but we can still test if such an option is added (though constructor may throw)
        // For branch coverage, we test legal cases: null short opt with non-null long opt is already done.
        // Null opt in getOption: should return null
        assertNull("getOption(null) returns null", opts.getOption(null));
    }

    @Test(timeout = 4000)
    public void testEmptyStringOptions() {
        Options opts = new Options();
        String empty = "";
        opts.addOption(empty, "empty", false, "Empty short");
        assertTrue("hasOption('')", opts.hasOption(empty));
        Option opt = opts.getOption(empty);
        assertNotNull("getOption('')", opt);
        assertEquals("short opt is empty", empty, opt.getOpt());
        assertEquals("long opt is 'empty'", "empty", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testMultipleHyphensStrip() {
        Options opts = new Options();
        opts.addOption("d", "delta", false, "Delta");
        assertTrue("hasOption('----d')", opts.hasOption("----d"));
        assertTrue("hasOption('-------delta')", opts.hasOption("-------delta"));
    }

    @Test(timeout = 4000)
    public void testDuplicateShortOptOverwrites() {
        Options opts = new Options();
        opts.addOption("d", "delta1", false, "First delta");
        opts.addOption("d", "delta2", false, "Second delta");
        Option opt = opts.getOption("d");
        assertEquals("long opt is 'delta2'", "delta2", opt.getLongOpt());
    }

    @Test(timeout = 4000)
    public void testDuplicateLongOptOverwrites() {
        Options opts = new Options();
        opts.addOption("a", "common", false, "First");
        opts.addOption("b", "common", false, "Second");
        // Long opt "common" points to last added
        Option opt = opts.getOption("common");
        assertEquals("short opt is 'b'", "b", opt.getOpt());
    }

    @Test(timeout = 4000)
    public void testRequiredOptionAddedTwice() {
        Options opts = new Options();
        Option opt1 = new Option("req", "required", false, "Required");
        opt1.setRequired(true);
        opts.addOption(opt1);
        Option opt2 = new Option("req", "required2", false, "Another required");
        opt2.setRequired(true);
        opts.addOption(opt2);
        List<?> requireds = opts.getRequiredOptions();
        // Should contain only one entry (key "req") because duplicate key removal
        assertEquals("one required entry after duplicate", 1, requireds.size());
    }

    // ========== Partition C: Defect-Targeted (BugCLI266) ==========

    @Test(timeout = 4000)
    public void testInsertionOrderPreservedInGetOptions() {
        // This test targets the known bug BugCLI266:
        // The original implementation used HashMap for shortOpts, losing insertion order.
        // We expect that getOptions() returns options in the order they were added.
        Options opts = new Options();
        Option first = new Option("p", "first", false, "First option");
        Option second = new Option("x", "second", false, "Second option");
        opts.addOption(first);
        opts.addOption(second);

        // Retrieve the options via getOptions() and convert to list to check order
        List<Option> optionsList = new ArrayList<Option>(opts.getOptions());
        assertEquals("first option is 'p'", "p", optionsList.get(0).getOpt());
        assertEquals("second option is 'x'", "x", optionsList.get(1).getOpt());
    }

    @Test(timeout = 4000)
    public void testInsertionOrderPreservedInHelpOptions() {
        Options opts = new Options();
        opts.addOption("a", "alpha", false, "Alpha");
        opts.addOption("b", "bravo", false, "Bravo");
        opts.addOption("c", "charlie", false, "Charlie");
        List<Option> helpList = opts.helpOptions();
        assertEquals("first is 'a'", "a", helpList.get(0).getOpt());
        assertEquals("second is 'b'", "b", helpList.get(1).getOpt());
        assertEquals("third is 'c'", "c", helpList.get(2).getOpt());
    }

    @Test(timeout = 4000)
    public void testInsertionOrderWithMixedShortAndLong() {
        Options opts = new Options();
        opts.addOption("z", "zulu", false, "Zulu");
        opts.addOption("m", "mike", false, "Mike"); // added after zulu
        List<Option> helpList = opts.helpOptions();
        assertEquals("first is 'z'", "z", helpList.get(0).getOpt());
        assertEquals("second is 'm'", "m", helpList.get(1).getOpt());
    }

    // ========== Partition D: Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testGetOptionForNonexistentKey() {
        Options opts = new Options();
        assertNull("getOption for non-existent returns null", opts.getOption("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasOptionForNonexistent() {
        Options opts = new Options();
        assertFalse("hasOption for non-existent", opts.hasOption("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsWithNullOpt() {
        Options opts = new Options();
        // getMatchingOptions calls Util.stripLeadingHyphens which may handle null?
        // Actually Util.stripLeadingHyphens probably throws NPE on null.
        // We rely on that the caller will not pass null; we test only non-null.
        // But for coverage, we could test the path where opt is null after stripping? Not needed.
        // Instead, ensure method works with empty string.
        List<String> matches = opts.getMatchingOptions("");
        // No options with empty long opt, so empty list expected
        assertTrue("empty matching list", matches.isEmpty());
    }

    // ========== Partition E: Object Contracts ==========

    @Test(timeout = 4000)
    public void testToStringContainsState() {
        Options opts = new Options();
        opts.addOption("t", "test", false, "Test option");
        String str = opts.toString();
        assertTrue("contains short opts", str.contains("short"));
        assertTrue("contains long opts", str.contains("long"));
        assertTrue("contains 'test'", str.contains("test"));
    }

    @Test(timeout = 4000)
    public void testEmptyOptionsToString() {
        Options opts = new Options();
        String str = opts.toString();
        assertTrue("empty short map", str.contains("{}"));
    }
}