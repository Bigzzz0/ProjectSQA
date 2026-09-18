package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.Options
 *
 * 1. CLI-266 Insertion Order Defect:
 *    - Historical Defect: shortOpts and longOpts must retain insertion order (LinkedHashMap instead of HashMap).
 *      Defect reproduced when comparator preserves insertion order or helpOptions()/getOptions() is iterated.
 *      Trigger: Adding options in order (e.g., "p", "x", etc.) and verifying getOptions() maintains exact insertion order.
 *
 * 2. Decision & Branch Coverage Target:
 *    - addOptionGroup(OptionGroup):
 *        * Branch: group.isRequired() == true vs false.
 *        * Loop: group.getOptions() empty vs non-empty.
 *        * Option mutation: option.setRequired(false) verified.
 *        * Mapping: optionGroups.put(option.getKey(), group) populated.
 *    - addOption(Option):
 *        * Branch: opt.hasLongOpt() == true vs false (longOpts mapping updated).
 *        * Branch: opt.isRequired() == true vs false.
 *        * Branch: opt.isRequired() && requiredOpts.contains(key) -> remove old index, append.
 *    - getOption(String):
 *        * stripLeadingHyphens applied (single '-', double '--', none).
 *        * Branch: shortOpts.containsKey(opt) == true -> returns short option.
 *        * Branch: shortOpts.containsKey(opt) == false -> fallback to longOpts.get(opt).
 *        * Not found in either -> returns null.
 *    - getMatchingOptions(String):
 *        * stripLeadingHyphens applied.
 *        * Branch: longOpts.keySet().contains(opt) -> returns singleton exact match.
 *        * Branch: partial match loop (longOpt.startsWith(opt)) -> multiple / single / empty matches.
 *    - hasOption(String), hasLongOption(String), hasShortOption(String):
 *        * Both true/false branches for short, long, and nonexistent options.
 *        * Stripping of hyphens ('-', '--').
 *    - getOptionGroup(Option):
 *        * Option in group -> returns OptionGroup.
 *        * Option not in group -> returns null.
 *    - getRequiredOptions():
 *        * Unmodifiable list verification (mutation triggers UnsupportedOperationException).
 *        * Mixed content (String keys and OptionGroup instances).
 *    - Serialization & toString Integrity:
 *        * Verification of serialVersionUID round-trip and non-null debug output.
 */
public class OptionsGeminiTest
{
    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-266 Insertion Order)
    // =========================================================================

    /**
     * CLI-266: Verifies that Options preserves insertion order of added options.
     * When options like "p" and "x" are added, getOptions() and helpOptions() must
     * iterate in the exact order of insertion rather than arbitrary hash order.
     */
    @Test(timeout = 4000)
    public void testOptionComparatorInsertedOrder()
    {
        Options options = new Options();
        Option optP = new Option("p", "print", false, "print option");
        Option optX = new Option("x", "extract", false, "extract option");
        Option optA = new Option("a", "all", false, "all option");
        Option optM = new Option("m", "modify", false, "modify option");

        options.addOption(optP);
        options.addOption(optX);
        options.addOption(optA);
        options.addOption(optM);

        Collection<Option> retrieved = options.getOptions();
        assertEquals("Total registered options mismatch", 4, retrieved.size());

        List<String> keys = new ArrayList<String>();
        for (Option opt : retrieved)
        {
            keys.add(opt.getOpt());
        }

        // Expected insertion order: [p, x, a, m]
        assertEquals("Expected first inserted option 'p'", "p", keys.get(0));
        assertEquals("Expected second inserted option 'x'", "x", keys.get(1));
        assertEquals("Expected third inserted option 'a'", "a", keys.get(2));
        assertEquals("Expected fourth inserted option 'm'", "m", keys.get(3));

        List<Option> helpOpts = options.helpOptions();
        assertEquals("helpOptions() must match insertion size", 4, helpOpts.size());
        assertEquals("helpOptions() first element must be 'p'", "p", helpOpts.get(0).getOpt());
        assertEquals("helpOptions() second element must be 'x'", "x", helpOpts.get(1).getOpt());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddOptionShortOnlyVariants()
    {
        Options options = new Options();

        // 1. addOption(opt, description)
        Options ret1 = options.addOption("v", "Verbose output");
        assertSame("addOption should return this", options, ret1);
        assertTrue(options.hasOption("v"));
        assertTrue(options.hasShortOption("v"));
        assertFalse(options.hasLongOption("v"));
        Option optV = options.getOption("v");
        assertNotNull(optV);
        assertEquals("v", optV.getOpt());
        assertNull(optV.getLongOpt());
        assertFalse(optV.hasArg());
        assertEquals("Verbose output", optV.getDescription());

        // 2. addOption(opt, hasArg, description)
        Options ret2 = options.addOption("f", true, "File path");
        assertSame("addOption should return this", options, ret2);
        assertTrue(options.hasOption("f"));
        Option optF = options.getOption("f");
        assertNotNull(optF);
        assertEquals("f", optF.getOpt());
        assertTrue(optF.hasArg());
        assertEquals("File path", optF.getDescription());

        // 3. addOption(opt, longOpt, hasArg, description)
        Options ret3 = options.addOption("o", "output", true, "Output target");
        assertSame("addOption should return this", options, ret3);
        assertTrue(options.hasOption("o"));
        assertTrue(options.hasOption("output"));
        assertTrue(options.hasShortOption("o"));
        assertTrue(options.hasLongOption("output"));
        Option optO = options.getOption("output");
        assertNotNull(optO);
        assertEquals("o", optO.getOpt());
        assertEquals("output", optO.getLongOpt());
        assertTrue(optO.hasArg());
        assertEquals("Output target", optO.getDescription());
    }

    @Test(timeout = 4000)
    public void testAddOptionLongOnly()
    {
        Options options = new Options();
        Option longOnly = new Option(null, "config", true, "Configuration file");

        options.addOption(longOnly);

        assertTrue(options.hasOption("config"));
        assertTrue(options.hasLongOption("config"));
        assertFalse(options.hasShortOption("config"));
        assertSame(longOnly, options.getOption("config"));
        assertSame(longOnly, options.getOption("--config"));
    }

    @Test(timeout = 4000)
    public void testGetOptionWithHyphenStripping()
    {
        Options options = new Options();
        options.addOption("s", "short-opt", false, "sample short/long");

        assertSame(options.getOption("s"), options.getOption("-s"));
        assertSame(options.getOption("short-opt"), options.getOption("--short-opt"));
        assertSame(options.getOption("short-opt"), options.getOption("-short-opt"));
        assertNull(options.getOption("non-existent"));
        assertNull(options.getOption("--non-existent"));
    }

    @Test(timeout = 4000)
    public void testHasOptionVariations()
    {
        Options options = new Options();
        options.addOption("a", false, "Option A");
        options.addOption(null, "long-only", false, "Long Only");

        // hasOption tests
        assertTrue(options.hasOption("a"));
        assertTrue(options.hasOption("-a"));
        assertTrue(options.hasOption("long-only"));
        assertTrue(options.hasOption("--long-only"));
        assertFalse(options.hasOption("b"));
        assertFalse(options.hasOption("--missing"));

        // hasShortOption tests
        assertTrue(options.hasShortOption("a"));
        assertTrue(options.hasShortOption("-a"));
        assertFalse(options.hasShortOption("long-only"));
        assertFalse(options.hasShortOption("unknown"));

        // hasLongOption tests
        assertTrue(options.hasLongOption("long-only"));
        assertTrue(options.hasLongOption("--long-only"));
        assertFalse(options.hasLongOption("a"));
        assertFalse(options.hasLongOption("unknown"));
    }

    @Test(timeout = 4000)
    public void testOptionGroupLifecycleAndRequiredStatus()
    {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        Option optA = new Option("a", "alpha", false, "Option Alpha");
        optA.setRequired(true); // Should be reset to false inside addOptionGroup
        Option optB = new Option("b", "beta", false, "Option Beta");
        group.addOption(optA);
        group.addOption(optB);

        assertFalse(group.isRequired());
        options.addOptionGroup(group);

        assertFalse("Option added through group must have required reset to false", optA.isRequired());
        assertFalse(optB.isRequired());
        assertEquals("Non-required group should not be in requiredOpts", 0, options.getRequiredOptions().size());

        assertSame(group, options.getOptionGroup(optA));
        assertSame(group, options.getOptionGroup(optB));
        Collection<OptionGroup> groups = options.getOptionGroups();
        assertEquals(1, groups.size());
        assertTrue(groups.contains(group));

        // Required OptionGroup branch
        Options requiredGroupOptions = new Options();
        OptionGroup reqGroup = new OptionGroup();
        reqGroup.setRequired(true);
        reqGroup.addOption(new Option("c", "gamma", false, "Gamma"));
        requiredGroupOptions.addOptionGroup(reqGroup);

        assertEquals(1, requiredGroupOptions.getRequiredOptions().size());
        assertSame(reqGroup, requiredGroupOptions.getRequiredOptions().get(0));
    }

    @Test(timeout = 4000)
    public void testGetOptionGroupForStandaloneOption()
    {
        Options options = new Options();
        Option standalone = new Option("z", "Standalone");
        options.addOption(standalone);

        assertNull("Standalone option must not have an OptionGroup", options.getOptionGroup(standalone));
    }

    @Test(timeout = 4000)
    public void testRequiredOptionDuplicateUpdate()
    {
        Options options = new Options();
        Option req1 = new Option("r", "req", false, "Required option");
        req1.setRequired(true);

        options.addOption(req1);
        List requiredList = options.getRequiredOptions();
        assertEquals(1, requiredList.size());
        assertEquals("r", requiredList.get(0));

        // Re-adding the same required option key should update without duplicating
        Option req2 = new Option("r", "req-new", false, "Re-added required option");
        req2.setRequired(true);
        options.addOption(req2);

        assertEquals("Required options list must not contain duplicates", 1, options.getRequiredOptions().size());
        assertEquals("r", options.getRequiredOptions().get(0));
        assertSame(req2, options.getOption("r"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Matching Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetMatchingOptionsExactMatch()
    {
        Options options = new Options();
        options.addOption(null, "filter", false, "Filter elements");
        options.addOption(null, "filter-advanced", false, "Advanced filter");

        // Exact match should return a singleton list even if other options start with it
        List<String> matches = options.getMatchingOptions("filter");
        assertEquals(1, matches.size());
        assertEquals("filter", matches.get(0));

        List<String> hyphenMatches = options.getMatchingOptions("--filter");
        assertEquals(1, hyphenMatches.size());
        assertEquals("filter", hyphenMatches.get(0));
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsPartialMatches()
    {
        Options options = new Options();
        options.addOption(null, "version", false, "Display version");
        options.addOption(null, "verbose", false, "Verbose log");
        options.addOption(null, "verbosity", false, "Verbosity level");
        options.addOption(null, "quiet", false, "Quiet mode");

        List<String> matches = options.getMatchingOptions("verbo");
        assertEquals(2, matches.size());
        assertTrue(matches.contains("verbose"));
        assertTrue(matches.contains("verbosity"));

        List<String> singlePrefixMatch = options.getMatchingOptions("q");
        assertEquals(1, singlePrefixMatch.size());
        assertEquals("quiet", singlePrefixMatch.get(0));

        List<String> noMatches = options.getMatchingOptions("xyz");
        assertTrue(noMatches.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetMatchingOptionsEmptyString()
    {
        Options options = new Options();
        options.addOption(null, "alpha", false, "Alpha");
        options.addOption(null, "beta", false, "Beta");

        List<String> allLongOpts = options.getMatchingOptions("");
        assertEquals(2, allLongOpts.size());
        assertTrue(allLongOpts.contains("alpha"));
        assertTrue(allLongOpts.contains("beta"));
    }

    // =========================================================================
    // Partition D: Defensive Guard Paths & Immutability
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetOptionsIsUnmodifiable()
    {
        Options options = new Options();
        options.addOption("a", "Option A");
        Collection<Option> col = options.getOptions();
        col.clear();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetRequiredOptionsIsUnmodifiable()
    {
        Options options = new Options();
        Option req = new Option("r", "req", false, "desc");
        req.setRequired(true);
        options.addOption(req);

        List reqList = options.getRequiredOptions();
        reqList.clear();
    }

    @Test(timeout = 4000)
    public void testAddOptionGroupEmpty()
    {
        Options options = new Options();
        OptionGroup emptyGroup = new OptionGroup();
        options.addOptionGroup(emptyGroup);

        assertTrue(options.getOptions().isEmpty());
        assertTrue(options.getOptionGroups().isEmpty());
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Serialization & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringContract()
    {
        Options options = new Options();
        options.addOption("s", "sample", false, "sample option");

        String str = options.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("[ Options: [ short "));
        assertTrue(str.contains("sample"));
        assertTrue(str.endsWith(" ]"));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception
    {
        Options original = new Options();
        original.addOption("h", "help", false, "Print help");
        Option requiredOpt = new Option("e", "env", true, "Environment");
        requiredOpt.setRequired(true);
        original.addOption(requiredOpt);

        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "Option X"));
        original.addOptionGroup(group);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Options deserialized = (Options) ois.readObject();

        assertNotNull(deserialized);
        assertEquals(3, deserialized.getOptions().size());
        assertTrue(deserialized.hasOption("h"));
        assertTrue(deserialized.hasOption("help"));
        assertTrue(deserialized.hasOption("e"));
        assertTrue(deserialized.hasOption("x"));
        assertEquals(1, deserialized.getRequiredOptions().size());
        assertEquals("e", deserialized.getRequiredOptions().get(0));

        Option desX = deserialized.getOption("x");
        assertNotNull(deserialized.getOptionGroup(desX));
    }
}