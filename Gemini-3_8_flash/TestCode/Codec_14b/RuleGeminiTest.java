package org.apache.commons.codec.language.bm;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT TARGETING (Defects4J Codec-16):
 *    - Issue: Rule.Phoneme(Phoneme, Phoneme) fails to restrict the language set of phonemeLeft
 *      with phonemeRight.languages. It retains phonemeLeft.languages unchanged, leading to phonemes
 *      with invalid/disjoint language combinations surviving pruning (e.g., 'vntsn' surviving
 *      instead of being restricted to NO_LANGUAGES and eliminated).
 *    - Targets:
 *        * Rule.Phoneme(Phoneme, Phoneme)
 *        * Consistency with deprecated Rule.Phoneme.join(Phoneme)
 *
 * 2. BRANCH & EQUIVALENCE PARTITIONING:
 *    - Rule.Phoneme.COMPARATOR:
 *        * Identical phoneme text (returns 0)
 *        * Left prefix of Right / shorter (returns -1)
 *        * Right prefix of Left / longer (returns +1)
 *        * Lexicographical char diff at index i (positive / negative)
 *        * Empty string comparisons
 *    - Rule.pattern(regex) compilation optimization:
 *        * !boxes && startsWith && endsWith:
 *            - content.length() == 0 ("^$") -> exact empty match
 *            - content.length() > 0 ("^abc$") -> exact string match
 *        * !boxes && (startsWith || endsWith) && content.length() == 0 ("^", "$") -> ALL_STRINGS_RMATCHER
 *        * !boxes && startsWith -> prefix match via startsWith(input, prefix)
 *        * !boxes && endsWith -> suffix match via endsWith(input, suffix)
 *        * boxes && startsWithBox && endsWithBox:
 *            - non-negated / negated character class ("^[abc]$", "^[^abc]$")
 *            - startsWith character class ("^[abc]", "^[^abc]")
 *            - endsWith character class ("[abc]$", "[^abc]$")
 *        * Complex boxes fallback (nested brackets, trailing characters) -> Pattern.compile fallback
 *    - Rule.patternAndContextMatches:
 *        * Negative index guard -> IndexOutOfBoundsException
 *        * Pattern length exceeds remaining length (ipl > input.length()) -> false
 *        * Pattern text mismatch -> false
 *        * Right context mismatch -> false
 *        * Left context mismatch -> false
 *        * Full match -> true
 *    - Rule Loading and Retrieval:
 *        * getInstance / getInstanceMap by single language and LanguageSet (singleton vs ANY vs explicit)
 *        * Missing rule resource handling -> IllegalArgumentException
 *        * Rule.toString() output formatting
 */
public class RuleGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Codec-16 Ground Truth)
    // =========================================================================

    /**
     * Targets the root defect causing:
     * PhoneticEngineRegressionTest::testCompatibilityWithOriginalVersion
     * expected:<...dzn|bntsn|bnzn|vndzn[]> but was:<...dzn|bntsn|bnzn|vndzn[|vntsn]>
     *
     * In the defective version, Rule.Phoneme(Phoneme, Phoneme) ignores phonemeRight's languages
     * and sets languages = phonemeLeft.languages instead of restricting to the intersection.
     */
    @Test(timeout = 4000)
    public void testPhonemeConstructorLanguageRestrictionDefect() {
        final Set<String> langs1 = new HashSet<String>(Arrays.asList("hebrew", "russian"));
        final Set<String> langs2 = new HashSet<String>(Arrays.asList("russian", "polish"));
        final Languages.LanguageSet set1 = Languages.LanguageSet.from(langs1);
        final Languages.LanguageSet set2 = Languages.LanguageSet.from(langs2);

        final Rule.Phoneme left = new Rule.Phoneme("v", set1);
        final Rule.Phoneme right = new Rule.Phoneme("ntsn", set2);

        final Rule.Phoneme combined = new Rule.Phoneme(left, right);

        final Languages.LanguageSet expected = set1.restrictTo(set2);
        assertEquals("Combined Phoneme must restrict languages to the intersection of both sets",
                expected, combined.getLanguages());
    }

    @Test(timeout = 4000)
    public void testPhonemeConstructorDisjointLanguagesDefect() {
        final Languages.LanguageSet hebrew = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("hebrew")));
        final Languages.LanguageSet polish = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("polish")));

        final Rule.Phoneme left = new Rule.Phoneme("v", hebrew);
        final Rule.Phoneme right = new Rule.Phoneme("ntsn", polish);

        final Rule.Phoneme combined = new Rule.Phoneme(left, right);

        // When languages are disjoint, restriction must result in NO_LANGUAGES so it can be pruned
        assertEquals("Disjoint languages must yield NO_LANGUAGES",
                Languages.NO_LANGUAGES, combined.getLanguages());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testPhonemeConstructorConsistencyWithJoinDefect() {
        final Languages.LanguageSet hebrew = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("hebrew")));
        final Languages.LanguageSet polish = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("polish")));

        final Rule.Phoneme left = new Rule.Phoneme("v", hebrew);
        final Rule.Phoneme right = new Rule.Phoneme("ntsn", polish);

        final Rule.Phoneme constructed = new Rule.Phoneme(left, right);
        final Rule.Phoneme joined = left.join(right);

        assertEquals("Phoneme(Phoneme, Phoneme) must produce identical language scope as join()",
                joined.getLanguages(), constructed.getLanguages());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testPhonemeBasicOperations() {
        final Rule.Phoneme phoneme = new Rule.Phoneme("test", Languages.ANY_LANGUAGE);
        assertEquals("test", phoneme.getPhonemeText().toString());
        assertEquals(Languages.ANY_LANGUAGE, phoneme.getLanguages());
        assertEquals("test[Any]", phoneme.toString());

        final Rule.Phoneme appended = phoneme.append("ing");
        assertSame(phoneme, appended);
        assertEquals("testing", phoneme.getPhonemeText().toString());
        assertEquals("testing[Any]", phoneme.toString());

        final Iterable<Rule.Phoneme> iterable = phoneme.getPhonemes();
        assertNotNull(iterable);
        assertTrue(iterable.iterator().hasNext());
        assertSame(phoneme, iterable.iterator().next());
    }

    @Test(timeout = 4000)
    public void testPhonemeThreeArgConstructor() {
        final Languages.LanguageSet hebrew = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("hebrew")));
        final Languages.LanguageSet french = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("french")));

        final Rule.Phoneme left = new Rule.Phoneme("bon", hebrew);
        final Rule.Phoneme right = new Rule.Phoneme("jour", french);

        final Rule.Phoneme explicit = new Rule.Phoneme(left, right, Languages.NO_LANGUAGES);
        assertEquals("bonjour", explicit.getPhonemeText().toString());
        assertEquals(Languages.NO_LANGUAGES, explicit.getLanguages());
    }

    @Test(timeout = 4000)
    public void testPhonemeList() {
        final Rule.Phoneme p1 = new Rule.Phoneme("a", Languages.ANY_LANGUAGE);
        final Rule.Phoneme p2 = new Rule.Phoneme("b", Languages.ANY_LANGUAGE);
        final List<Rule.Phoneme> list = Arrays.asList(p1, p2);

        final Rule.PhonemeList phonemeList = new Rule.PhonemeList(list);
        assertEquals(2, phonemeList.getPhonemes().size());
        assertSame(p1, phonemeList.getPhonemes().get(0));
        assertSame(p2, phonemeList.getPhonemes().get(1));
    }

    @Test(timeout = 4000)
    public void testPhonemeComparatorExhaustive() {
        final Rule.Phoneme a = new Rule.Phoneme("a", Languages.ANY_LANGUAGE);
        final Rule.Phoneme b = new Rule.Phoneme("b", Languages.ANY_LANGUAGE);
        final Rule.Phoneme aa = new Rule.Phoneme("aa", Languages.ANY_LANGUAGE);
        final Rule.Phoneme empty = new Rule.Phoneme("", Languages.ANY_LANGUAGE);

        // Identical
        assertEquals(0, Rule.Phoneme.COMPARATOR.compare(a, new Rule.Phoneme("a", Languages.NO_LANGUAGES)));
        assertEquals(0, Rule.Phoneme.COMPARATOR.compare(empty, new Rule.Phoneme("", Languages.ANY_LANGUAGE)));

        // Char diff
        assertTrue(Rule.Phoneme.COMPARATOR.compare(a, b) < 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(b, a) > 0);

        // Length diff (prefix)
        assertTrue(Rule.Phoneme.COMPARATOR.compare(a, aa) < 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(aa, a) > 0);

        // Empty vs non-empty
        assertTrue(Rule.Phoneme.COMPARATOR.compare(empty, a) < 0);
        assertTrue(Rule.Phoneme.COMPARATOR.compare(a, empty) > 0);
    }

    @Test(timeout = 4000)
    public void testAllStringsRM উদ্দেশে() {
        assertEquals("ALL", Rule.ALL);
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch(""));
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch("anything"));
        assertTrue(Rule.ALL_STRINGS_RMATCHER.isMatch(null));
    }

    @Test(timeout = 4000)
    public void testRuleAccessors() {
        final Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("pat", "left", "right", ph);

        assertEquals("pat", rule.getPattern());
        assertSame(ph, rule.getPhoneme());
        assertNotNull(rule.getLContext());
        assertNotNull(rule.getRContext());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Regex Branch Matrix
    // =========================================================================

    @Test(timeout = 4000)
    public void testPatternExactEmptyMatch() {
        // lContext "^" -> "^$" regex
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "^", "", ph);

        // left context at 0 is "" -> matches
        assertTrue(rule.patternAndContextMatches("x", 0));
        // left context at 1 is "a" -> does not match empty
        assertFalse(rule.patternAndContextMatches("ax", 1));
    }

    @Test(timeout = 4000)
    public void testPatternExactNonEmptyMatch() {
        // lContext "^abc" -> "^abc$" regex
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "^abc", "", ph);

        assertTrue(rule.patternAndContextMatches("abcx", 3));
        assertFalse(rule.patternAndContextMatches("zabcx", 4));
        assertFalse(rule.patternAndContextMatches("abx", 2));
    }

    @Test(timeout = 4000)
    public void testPatternAllStringsMatcherBranch() {
        // empty contexts: lContext "" -> "$", rContext "" -> "^"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "", "", ph);

        assertTrue(rule.patternAndContextMatches("x", 0));
        assertTrue(rule.patternAndContextMatches("prefix_x_suffix", 7));
    }

    @Test(timeout = 4000)
    public void testPatternStartsWithBranch() {
        // rContext "abc" -> "^abc"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "", "abc", ph);

        // Matching suffix
        assertTrue(rule.patternAndContextMatches("xabcdef", 0));
        // Suffix too short (prefix.length() > input.length())
        assertFalse(rule.patternAndContextMatches("xab", 0));
        // Char mismatch
        assertFalse(rule.patternAndContextMatches("xazcdef", 0));
    }

    @Test(timeout = 4000)
    public void testPatternEndsWithBranch() {
        // lContext "abc" -> "abc$"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "abc", "", ph);

        // Matching prefix
        assertTrue(rule.patternAndContextMatches("abcx", 3));
        // Prefix too short (suffix.length() > input.length())
        assertFalse(rule.patternAndContextMatches("abx", 2));
        // Char mismatch
        assertFalse(rule.patternAndContextMatches("abzx", 3));
    }

    @Test(timeout = 4000)
    public void testPatternBoxExactMatch() {
        // lContext "^[abc]" -> "^[abc]$"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "^[abc]", "", ph);

        assertTrue(rule.patternAndContextMatches("ax", 1));
        assertTrue(rule.patternAndContextMatches("bx", 1));
        assertTrue(rule.patternAndContextMatches("cx", 1));
        assertFalse(rule.patternAndContextMatches("dx", 1));
        assertFalse(rule.patternAndContextMatches("x", 0));
        assertFalse(rule.patternAndContextMatches("aax", 2));
    }

    @Test(timeout = 4000)
    public void testPatternBoxExactNegatedMatch() {
        // lContext "^[^abc]" -> "^[^abc]$"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "^[^abc]", "", ph);

        assertTrue(rule.patternAndContextMatches("dx", 1));
        assertFalse(rule.patternAndContextMatches("ax", 1));
        assertFalse(rule.patternAndContextMatches("bx", 1));
        assertFalse(rule.patternAndContextMatches("x", 0));
        assertFalse(rule.patternAndContextMatches("ddx", 2));
    }

    @Test(timeout = 4000)
    public void testPatternBoxStartsWith() {
        // rContext "[abc]" -> "^[abc]"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "", "[abc]", ph);

        assertTrue(rule.patternAndContextMatches("xa", 0));
        assertTrue(rule.patternAndContextMatches("xapple", 0));
        assertFalse(rule.patternAndContextMatches("xdog", 0));
        assertFalse(rule.patternAndContextMatches("x", 0));
    }

    @Test(timeout = 4000)
    public void testPatternBoxStartsWithNegated() {
        // rContext "[^abc]" -> "^[^abc]"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "", "[^abc]", ph);

        assertTrue(rule.patternAndContextMatches("xdog", 0));
        assertFalse(rule.patternAndContextMatches("xa", 0));
        assertFalse(rule.patternAndContextMatches("xapple", 0));
        assertFalse(rule.patternAndContextMatches("x", 0));
    }

    @Test(timeout = 4000)
    public void testPatternBoxEndsWith() {
        // lContext "[abc]" -> "[abc]$"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "[abc]", "", ph);

        assertTrue(rule.patternAndContextMatches("ax", 1));
        assertTrue(rule.patternAndContextMatches("banana_ax", 9));
        assertFalse(rule.patternAndContextMatches("dx", 1));
        assertFalse(rule.patternAndContextMatches("x", 0));
    }

    @Test(timeout = 4000)
    public void testPatternBoxEndsWithNegated() {
        // lContext "[^abc]" -> "[^abc]$"
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule = new Rule("x", "[^abc]", "", ph);

        assertTrue(rule.patternAndContextMatches("dx", 1));
        assertTrue(rule.patternAndContextMatches("banana_dx", 9));
        assertFalse(rule.patternAndContextMatches("ax", 1));
        assertFalse(rule.patternAndContextMatches("x", 0));
    }

    @Test(timeout = 4000)
    public void testPatternBoxFallbackComplexRegex() {
        // Nested bracket: [a[b]]
        final Rule.Phoneme ph = new Rule.Phoneme("", Languages.ANY_LANGUAGE);
        final Rule rule1 = new Rule("x", "", "[a[b]]", ph);
        assertNotNull(rule1.getRContext());

        // Box followed by characters: [a-z]foo
        final Rule rule2 = new Rule("x", "", "[a-z]foo", ph);
        assertTrue(rule2.patternAndContextMatches("xafoo", 0));
        assertFalse(rule2.patternAndContextMatches("x1foo", 0));

        // Grouping regex fallback
        final Rule rule3 = new Rule("x", "(ab|cd)", "", ph);
        assertTrue(rule3.patternAndContextMatches("abx", 2));
        assertTrue(rule3.patternAndContextMatches("cdx", 2));
        assertFalse(rule3.patternAndContextMatches("efx", 2));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testPatternAndContextMatchesNegativeIndex() {
        final Rule rule = new Rule("pat", "", "", new Rule.Phoneme("", Languages.ANY_LANGUAGE));
        rule.patternAndContextMatches("input", -1);
    }

    @Test(timeout = 4000)
    public void testPatternAndContextMatchesInsufficientRoom() {
        final Rule rule = new Rule("pattern", "", "", new Rule.Phoneme("", Languages.ANY_LANGUAGE));
        // "pat".length() == 3, pattern length is 7 -> ipl > input.length()
        assertFalse(rule.patternAndContextMatches("pat", 0));
        assertFalse(rule.patternAndContextMatches("pattern", 1));
    }

    @Test(timeout = 4000)
    public void testPatternAndContextMatchesPatternMismatch() {
        final Rule rule = new Rule("pat", "", "", new Rule.Phoneme("", Languages.ANY_LANGUAGE));
        assertFalse(rule.patternAndContextMatches("abcdef", 0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetInstanceMapMissingLanguage() {
        Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, "non_existent_language_xyz");
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Factory & Integration Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstanceAndInstanceMapIntegration() {
        // Test singleton language resolution
        final Languages.LanguageSet frenchSet = Languages.LanguageSet.from(new HashSet<String>(Collections.singletonList("french")));
        final Map<String, List<Rule>> frenchMap = Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, frenchSet);
        assertNotNull(frenchMap);
        assertFalse(frenchMap.isEmpty());

        // Test list factory overload
        final List<Rule> frenchRules = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, frenchSet);
        assertNotNull(frenchRules);
        assertFalse(frenchRules.isEmpty());

        // Test single string overload
        final List<Rule> frenchRulesByString = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, "french");
        assertEquals(frenchRules.size(), frenchRulesByString.size());

        // Test multi-language set resolution (should fall back to Languages.ANY)
        final Languages.LanguageSet multiSet = Languages.LanguageSet.from(new HashSet<String>(Arrays.asList("french", "german")));
        final Map<String, List<Rule>> anyMap = Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, multiSet);
        assertNotNull(anyMap);
        assertFalse(anyMap.isEmpty());

        // Verify common rules mapping exists for APPROX
        final Map<String, List<Rule>> commonMap = Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, "common");
        assertNotNull(commonMap);
        assertFalse(commonMap.isEmpty());

        // Verify loaded rule attributes and anonymous toString()
        final Rule sampleRule = frenchRules.get(0);
        assertNotNull(sampleRule.getPattern());
        assertNotNull(sampleRule.getLContext());
        assertNotNull(sampleRule.getRContext());
        assertNotNull(sampleRule.getPhoneme());
        final String ruleString = sampleRule.toString();
        assertTrue(ruleString.startsWith("Rule{"));
        assertTrue(ruleString.contains("pat='"));
        assertTrue(ruleString.contains("loc='"));
    }
}