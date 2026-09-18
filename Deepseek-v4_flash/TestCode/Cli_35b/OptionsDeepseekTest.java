package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target class: Options
 * 
 * Partitions and branches covered:
 * A. Core Functional Logic & State Transitions:
 *    - addOption(Option): longOpt mapping, required handling, key duplicates
 *    - addOptionGroup: required group, option setRequired(false), option group mapping
 *    - getOption: short/long lookup, leading hyphens stripping, null/empty
 *    - hasOption, hasLongOption, hasShortOption: valid, invalid, hyphens
 *    - getMatchingOptions: exact match priority, partial prefix, no match
 *    - getRequiredOptions: modifiable list, group inclusion
 *    - getOptionGroup: known key, unknown key
 *    - helpOptions, getOptions, toString
 * B. Boundary Value Analysis & Extremes:
 *    - null arguments to getOption, hasOption, etc. (expect NPE from Util.stripLeadingHyphens)
 *    - empty string, string with only hyphens, multiple hyphens
 *    - duplicate option insertion (update behaviour)
 *    - requiredOpts mixing String and OptionGroup
 *    - longOpts key case sensitivity
 * C. Defect-Targeted Branch Zone:
 *    - Ambiguous option bug: getMatchingOptions must return only exact match if one exists
 *      (Defects4J BugCLI252Test). Test 'prefix' vs 'prefixplusplus'.
 * D. Exception & Defensive Guard Paths:
 *    - IllegalArgumentException? Not thrown explicitly, but we test NPE on null.
 * E. Object Lifecycle & Contract Integrity:
 *    - toString basic string verification
 *    - Unmodifiable collections returned by getOptions and getRequiredOptions
 *
 * Note: The class uses Util.stripLeadingHyphens which may throw NullPointerException on null input.
 */

public class OptionsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testAddOptionShortOnly() {
        Options opts = new Options();
        opts.addOption("a", "description");
        assertTrue("short option should exist", opts.hasOption("a"));
        assertFalse("no long option", opts.hasLongOption("a"));
        assertEquals("description", opts.getOption("a").getDescription());
    }

    @Test(timeout = 4000)
    public void testAddOptionWithLong() {
        Options opts = new Options();
        opts.addOption("b", "long-b", false, "desc");
        assertTrue("short option exists", opts.hasOption("b"));
        assertTrue("long option exists", opts.hasLongOption("long-b"));
        assertEquals("desc", opts.getOption("b").getDescription());
        assertEquals("desc", opts.getOption("--long-b").getDescription());
    }

    @Test(timeout = 4000)
    public void testAddOptionRequired() {
        Options opts = new Options();
        Option opt = new Option("r", "required-opt", false, "required");
        opt.setRequired(true);
        opts.addOption(opt);
        assertTrue("option is required", opts.getRequiredOptions().contains("r"));
        // Adding again with same key should not duplicate
        opts.addOption(opt);
        assertEquals("required list size should be 1", 1, opts.getRequiredOptions().size());
    }

    @Test(timeout = 4000)
    public void testAddOptionGroupRequired() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("x", "opt-x", false, "x"));
        group.addOption(new Option("y", "opt-y", false, "y"));
        opts.addOptionGroup(group);
        // Verify options are not required individually
        assertFalse("option x should not be required", opts.getOption("x").isRequired());
        assertFalse("option y should not be required", opts.getOption("y").isRequired());
        // Group is required, so requiredOpts should contain the group
        assertEquals("required list should contain group", 1, opts.getRequiredOptions().size());
        assertTrue("required list should contain OptionGroup",
                   opts.getRequiredOptions().get(0) instanceof OptionGroup);
    }

    @Test(timeout = 4000)
    public void testGetOptionWithHyphens() {
        Options opts = new Options();
        opts.addOption("c", "long-c", false, "desc");
        assertEquals("getOption with --", opts.getOption("c"), opts.getOption("--c"));
        assertEquals("getOption with -", opts.getOption("c"), opts.getOption("-c"));
        assertEquals("getOption with long --", opts.getOption("long-c"), opts.getOption("--long-c"));
    }

    @Test(timeout = 4000)
    public void testGetOptionNonExistent() {
        Options opts = new Options();
        assertNull("non-existent short", opts.getOption("z"));
        assertNull("non-existent long", opts.getOption("nonexistent"));
    }

    @Test(timeout = 4000)
    public void testHasOption() {
        Options opts = new Options();
        opts.addOption("d", "long-d", false, "desc");
        assertTrue("has short", opts.hasOption("d"));
        assertTrue("has long", opts.hasOption("long-d"));
        assertTrue("has with --", opts.hasOption("--d"));
        assertFalse("non-existent", opts.hasOption("e"));
    }

    @Test(timeout = 4000)
    public void testHasLongOption() {
        Options opts = new Options();
        opts.addOption("f", "long-f", false, "desc");
        assertTrue("has long", opts.hasLongOption("long-f"));
        assertTrue("has with --", opts.hasLongOption("--long-f"));
        assertFalse("short is not long", opts.hasLongOption("f"));
    }

    @Test(timeout = 4000)
    public void testHasShortOption() {
        Options opts = new Options();
        opts.addOption("g", "long-g", false, "desc");
        assertTrue("has short", opts.hasShortOption("g"));
        assertTrue("has with -", opts.hasShortOption("-g"));
        assertFalse("long is not short", opts.hasShortOption("long-g"));
    }

    @Test(timeout = 4000)
    public void testGetOptionGroup() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("h", "long-h", false, "h"));
        opts.addOptionGroup(group);
        assertNotNull("option group exists", opts.getOptionGroup(opts.getOption("h")));
        assertNull("option with no group", opts.getOptionGroup(new Option("i", "alone", false, "i")));
    }

    @Test(timeout = 4000)
    public void testHelpOptionsReturnsList() {
        Options opts = new Options();
        opts.addOption("j", "descj");
        opts.addOption("k", "desck");
        assertEquals("help options size", 2, opts.helpOptions().size());
    }

    @Test(timeout = 4000)
    public void testGetOptionsUnmodifiable() {
        Options opts = new Options();
        opts.addOption("l", "descl");
        try {
            opts.getOptions().add(new Option("m", "descm"));
            fail("should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetRequiredOptionsUnmodifiable() {
        Options opts = new Options();
        opts.addOption(new Option("n", true, "requiredn"));
        try {
            opts.getRequiredOptions().add("something");
            fail("should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString() {
        Options opts = new Options();
        opts.addOption("o", "long-o", false, "desco");
        String str = opts.toString();
        assertTrue("contains short map", str.contains("short"));
        assertTrue("contains long map", str.contains("long"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetOptionNull() {
        Options opts = new Options();
        opts.getOption(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testHasOptionNull() {
        Options opts = new Options();
        opts.hasOption(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testHasLongOptionNull() {
        Options opts = new Options();
        opts.hasLongOption(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testHasShortOptionNull() {
        Options opts = new Options();
        opts.hasShortOption(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetMatchingOptionsNull() {
        Options opts = new Options();
        opts.getMatchingOptions(null);
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsEmptyString() {
        Options opts = new Options();
        opts.addOption("p", "prefix", false, "p");
        opts.addOption("q", "prefixplusplus", false, "q");
        List<String> matches = opts.getMatchingOptions("");
        // empty prefix matches all long options
        assertTrue("empty prefix returns all long options", matches.contains("prefix"));
        assertTrue("empty prefix returns all long options", matches.contains("prefixplusplus"));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsOnlyHyphens() {
        Options opts = new Options();
        opts.addOption("r", "real", false, "r");
        List<String> matches = opts.getMatchingOptions("--");
        // after stripping hyphens, empty string => returns all long opts
        assertTrue("only hyphens becomes empty", matches.contains("real"));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsNoMatch() {
        Options opts = new Options();
        opts.addOption("s", "some", false, "s");
        List<String> matches = opts.getMatchingOptions("nonexist");
        assertTrue("no match returns empty list", matches.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsMultiplePartial() {
        Options opts = new Options();
        opts.addOption("t", "test", false, "t");
        opts.addOption("u", "testing", false, "u");
        opts.addOption("v", "tester", false, "v");
        List<String> matches = opts.getMatchingOptions("test");
        // "test" prefix matches all three: test, testing, tester
        assertEquals("should match three", 3, matches.size());
        assertTrue(matches.contains("test"));
        assertTrue(matches.contains("testing"));
        assertTrue(matches.contains("tester"));
    }

    // ==================== Partition C: Defect-Targeted (AmbiguousOption Fix) ====================

    @Test(timeout = 4000)
    public void testGetMatchingOptionsExactMatch() {
        // Defect: When an exact match exists, getMatchingOptions should return only that option,
        // not all options with that prefix.
        Options opts = new Options();
        opts.addOption("x", "prefix", false, "p");
        opts.addOption("y", "prefixplusplus", false, "pp");
        List<String> matches = opts.getMatchingOptions("prefix");
        // Buggy version returns both ["prefix", "prefixplusplus"]; correct version returns ["prefix"]
        assertEquals("exact match should return single option", 1, matches.size());
        assertEquals("the single match should be the exact option", "prefix", matches.get(0));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsExactMatchWithHyphens() {
        Options opts = new Options();
        opts.addOption("z", "exact", false, "e");
        opts.addOption("a", "exactlonger", false, "el");
        List<String> matches = opts.getMatchingOptions("--exact");
        assertEquals("exact match with hyphens", 1, matches.size());
        assertEquals("exact", matches.get(0));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOptionNullOption() {
        Options opts = new Options();
        // Option constructor with null opt? Actually Option can be constructed with null? Not defined.
        // This test is to ensure no NPE from addOption(Option) if null passed.
        // Since Option is not our class, we just call with null to see behavior.
        opts.addOption((Option) null);
    }

    // Actually Option constructors don't accept null opt? They might throw IllegalArgumentException.
    // Let's defer and test valid exception paths from Option validation.
    // But we can test addOption with null strings via addOption(String,..).
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddOptionNullShort() {
        Options opts = new Options();
        opts.addOption(null, "long", false, "desc");
    }

    @Test(timeout = 4000)
    public void testAddOptionDuplicateShort() {
        Options opts = new Options();
        opts.addOption("a", "first", false, "first");
        opts.addOption("a", "second", false, "second");
        // Short key 'a' is overwritten
        assertEquals("short option overwritten", "second", opts.getOption("a").getDescription());
        // Long opt mapping remains for first? Actually long "first" is added first, then when adding second option with same short but different long,
        // the second Option's long "second" is put into longOpts, but does not remove the first long.
        // So both long opts exist. Let's verify: hasLongOption("first") should be true.
        assertTrue("first long still present", opts.hasLongOption("first"));
        assertTrue("second long present", opts.hasLongOption("second"));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetRequiredOptionsContainsGroups() {
        Options opts = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("1", "one", false, "1"));
        opts.addOptionGroup(group);
        assertTrue("required list contains group",
                   opts.getRequiredOptions().get(0) instanceof OptionGroup);
    }

    @Test(timeout = 4000)
    public void testGetOptionGroupAfterMultipleGroups() {
        Options opts = new Options();
        OptionGroup g1 = new OptionGroup();
        g1.addOption(new Option("2", "two", false, "2"));
        OptionGroup g2 = new OptionGroup();
        g2.addOption(new Option("3", "three", false, "3"));
        opts.addOptionGroup(g1);
        opts.addOptionGroup(g2);
        assertNotNull(opts.getOptionGroup(opts.getOption("2")));
        assertNotNull(opts.getOptionGroup(opts.getOption("3")));
        // Option not in any group
        assertNull(opts.getOptionGroup(new Option("4", "four", false, "4")));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsEmptyListWhenNoLongOptions() {
        Options opts = new Options();
        opts.addOption("a", "short only");
        assertTrue("no long options => empty", opts.getMatchingOptions("a").isEmpty());
    }
}