package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.StringUtils
 * Defect Reference: Defects4J / StringUtilsEqualsIndexOfTest::testContainsIgnoreCase_LocaleIndependence
 * Root Cause:
 *   StringUtils.containsIgnoreCase(str, searchStr) delegates to contains(str.toUpperCase(), searchStr.toUpperCase()).
 *   Using toUpperCase() is sensitive to the default JVM Locale and Unicode case folding rules:
 *     1) In Java 7+, "\u00DF".toUpperCase() yields "SS" (German sharp S expands to two chars).
 *        Therefore, containsIgnoreCase("\u00DF", "SS") erroneously returns true instead of false.
 *     2) In Turkish locale ("tr"), lowercase 'i' uppercases to '\u0130' (dotted uppercase I),
 *        causing containsIgnoreCase("i", "I") to return false instead of true.
 *
 * Targeted Decision Branches & Boundary Conditions:
 *   - Empty/Blank Checks: null, empty (""), whitespace only (' ', '\t', '\r', '\n'), non-whitespace.
 *   - Trim/Strip: null-handling, strip with custom charset, empty stripChars, boundary trim.
 *   - Search & Match:
 *       * indexOf / lastIndexOf / ordinalIndexOf (positive, zero, negative ordinals, startPos < 0, startPos > len).
 *       * indexOfAny / lastIndexOfAny / containsAny / containsOnly / containsNone with null, empty, matching, non-matching.
 *       * containsIgnoreCase under German "\u00DF" vs "SS" and Turkish locale "i" vs "I".
 *   - Substrings & Slicing:
 *       * substring, left, right, mid (negative start/end, start > end, len < 0, exceeding lengths).
 *       * substringBefore, substringAfter, substringBeforeLast, substringAfterLast (found, not found, empty separators).
 *       * substringBetween, substringsBetween (matching nested, empty tags, single character tags, null inputs).
 *   - Split & Join:
 *       * split, splitWorker, splitPreserveAllTokens (preserveAllTokens true vs false, max limit, consecutive delimiters).
 *       * splitByWholeSeparator, splitByWholeSeparatorPreserveAllTokens (empty/null separator, multichar separator).
 *       * splitByCharacterType, splitByCharacterTypeCamelCase (UPPERCASE followed by LOWERCASE, digits, punctuation).
 *       * join (Object[], char, String, Iterator, Collection, startIndex, endIndex, empty, single-element, nulls).
 *   - Mutation & Replacing:
 *       * replace, replaceOnce, replace with max limits (-1, 0, 1, positive), replacement length differences.
 *       * replaceEach, replaceEachRepeatedly (circular detection, timeToLive < 0, mismatched array lengths).
 *       * replaceChars, overlay (start > end swap, negative indices, indices > length).
 *       * deleteWhitespace, remove, removeStart, removeEnd (case sensitive & insensitive).
 *       * chomp, chop (handling \r, \n, \r\n, single char, empty, null).
 *   - Padding & Centering:
 *       * repeat (repeat <= 0, repeat == 1, inputLen 1 vs 2 vs N, repeat with separator).
 *       * leftPad, rightPad, center (with char, with String, pad length > PAD_LIMIT 8192, size <= str.length).
 *   - Character Classification:
 *       * isAlpha, isAlphaSpace, isAlphanumeric, isAlphanumericSpace, isAsciiPrintable, isNumeric, isNumericSpace,
 *         isWhitespace, isAllLowerCase, isAllUpperCase (all covering null, empty, valid, invalid characters).
 *   - Distance & Difference:
 *       * getLevenshteinDistance (null throws IllegalArgumentException, empty strings, swaps where n > m).
 *       * difference, indexOfDifference (string-to-string and array variants with varying lengths, nulls, identicals).
 *       * getCommonPrefix (null array, empty array, mismatch, all identical).
 *   - Prefix/Suffix:
 *       * startsWith, startsWithIgnoreCase, startsWithAny, endsWith, endsWithIgnoreCase.
 * ---------------------------------------------------------------------------------------------------------
 */
public class StringUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect)
    // =========================================================================

    /**
     * Targets the known defect in containsIgnoreCase where using toUpperCase()
     * produces locale and character-expansion failures (e.g., German 'ß' to 'SS').
     * Case insensitivity should not match German sharp s ("\u00DF") with "SS".
     */
    @Test(timeout = 4000)
    public void testContainsIgnoreCase_DefectGermanSharpS() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertFalse("en: 0 \u00DF SS", StringUtils.containsIgnoreCase("\u00DF", "SS"));
            assertFalse("en: 0 SS \u00DF", StringUtils.containsIgnoreCase("SS", "\u00DF"));
        } finally {
            Locale.setDefault(original);
        }
    }

    /**
     * Targets Turkish locale sensitivity where 'i' and 'I' case conversion
     * breaks if toUpperCase() is applied with the default Locale.
     */
    @Test(timeout = 4000)
    public void testContainsIgnoreCase_DefectTurkishLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            assertTrue("Turkish locale: 'i' should match 'I'", StringUtils.containsIgnoreCase("i", "I"));
            assertTrue("Turkish locale: 'I' should match 'i'", StringUtils.containsIgnoreCase("I", "i"));
        } finally {
            Locale.setDefault(original);
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }

    @Test(timeout = 4000)
    public void testEmptyAndBlankChecks() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("abc"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank(" \t\r\n "));
        assertFalse(StringUtils.isBlank("  a  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" \t "));
        assertTrue(StringUtils.isNotBlank("  a  "));
    }

    @Test(timeout = 4000)
    public void testTrimAndStripOperations() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("yyyabcyyy", "y"));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc  "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals("abc  ", StringUtils.stripStart("xxabc  ", "x"));
        assertEquals("xxabc  ", StringUtils.stripStart("xxabc  ", ""));

        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals("  abc", StringUtils.stripEnd("  abcyy", "y"));
        assertEquals("  abcyy", StringUtils.stripEnd("  abcyy", ""));

        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        assertArrayEquals(new String[]{"a", null, "b"}, StringUtils.stripAll(new String[]{"  a ", null, " b"}));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"xxaxx", "ybx"}, "xy"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndEqualsIgnoreCase() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "ABCD"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndLastIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(1, StringUtils.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("a", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));

        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("a", null, 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));

        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", "a", 0));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 10));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));

        assertEquals(-1, StringUtils.lastIndexOf(null, "b"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));

        assertEquals(-1, StringUtils.lastIndexOf(null, "b", 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
    }

    @Test(timeout = 4000)
    public void testContainsMethods() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("a", null));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("a", null));
        assertTrue(StringUtils.containsIgnoreCase("abc", "BC"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "BD"));
    }

    @Test(timeout = 4000)
    public void testAnyAllNoneSearches() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        assertEquals(1, StringUtils.indexOfAny("zzabyycdxx", new char[]{'a', 'b'}));

        assertEquals(-1, StringUtils.indexOfAny(null, "a"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(1, StringUtils.indexOfAny("zzabyycdxx", "ab"));

        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', '1'}));
        assertFalse(StringUtils.containsAny("zzabyycdxx", new char[]{'1', '2'}));

        assertFalse(StringUtils.containsAny(null, "a"));
        assertFalse(StringUtils.containsAny("abc", (String) null));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));

        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        assertEquals(2, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzz", new char[]{'z'}));

        assertEquals(-1, StringUtils.indexOfAnyBut(null, "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        assertEquals(2, StringUtils.indexOfAnyBut("zzabyycdxx", "z"));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzz", "z"));

        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("abc", (char[]) null));
        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("abc", new char[0]));
        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b'}));
        assertFalse(StringUtils.containsOnly("abac", new char[]{'a', 'b'}));

        assertFalse(StringUtils.containsOnly(null, "a"));
        assertFalse(StringUtils.containsOnly("abc", (String) null));
        assertTrue(StringUtils.containsOnly("abab", "ab"));

        assertTrue(StringUtils.containsNone(null, (char[]) null));
        assertTrue(StringUtils.containsNone("abc", (char[]) null));
        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abc", new char[]{'x', 'y'}));
        assertFalse(StringUtils.containsNone("abc", new char[]{'a', 'y'}));

        assertTrue(StringUtils.containsNone(null, (String) null));
        assertTrue(StringUtils.containsNone("abc", (String) null));
        assertTrue(StringUtils.containsNone("abc", "xy"));
        assertFalse(StringUtils.containsNone("abc", "ax"));

        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String[]) null));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new String[]{null, ""}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"mn", null}));

        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"a"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", (String[]) null));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{null, "123"}));
    }

    @Test(timeout = 4000)
    public void testSubstrings() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 2));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));
        assertEquals("", StringUtils.substring("abc", 4));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
        assertEquals("", StringUtils.substring("abc", -1, -2));
        assertEquals("", StringUtils.substring("abc", -1, -5));

        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 5, 2));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 5));
        assertEquals("b", StringUtils.mid("abc", 1, 1));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAfterAndBetween() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "z"));

        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "z"));

        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", "z"));

        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
        assertEquals("", StringUtils.substringAfterLast("abc", "z"));

        assertNull(StringUtils.substringBetween(null, "a"));
        assertNull(StringUtils.substringBetween("abc", null));
        assertEquals("b", StringUtils.substringBetween("aba", "a"));

        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("abc", null, "]"));
        assertNull(StringUtils.substringBetween("abc", "[", null));
        assertEquals("", StringUtils.substringBetween("", "", ""));
        assertNull(StringUtils.substringBetween("abc", "[", "]"));
        assertEquals("tag", StringUtils.substringBetween("[tag]", "[", "]"));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", ""));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", "]"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a][b]", "[", "]"));
        assertNull(StringUtils.substringsBetween("[a", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplitOperations() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.split("a.b", '.'));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.split("a:b", ":"));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));

        assertNull(StringUtils.splitPreserveAllTokens(null));
        assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitPreserveAllTokens("a..b", '.'));
        assertArrayEquals(new String[]{"", "a", ""}, StringUtils.splitPreserveAllTokens(" a "));
        assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitPreserveAllTokens("a::b", ":"));
        assertArrayEquals(new String[]{"a", "b::c"}, StringUtils.splitPreserveAllTokens("a::b::c", ":", 2));
        assertArrayEquals(new String[]{"a", "", "b::c"}, StringUtils.splitPreserveAllTokens("a::b::c", "::", 3));

        assertNull(StringUtils.splitByWholeSeparator(null, ":"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab cd  ef", null));

        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, ":"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", ":"));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!--!-cd", "-!-"));
        assertArrayEquals(new String[]{"ab", "-!-cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!--!-cd", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab  cd", null));

        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"foo", "200", "B", "ar"}, StringUtils.splitByCharacterType("foo200Bar"));

        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
        assertArrayEquals(new String[]{"foo", "200", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test(timeout = 4000)
    public void testJoinOperations() {
        assertNull(StringUtils.join((Object[]) null));
        assertNull(StringUtils.join((Object[]) null, ','));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("", StringUtils.join(new Object[]{null}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("a, b", StringUtils.join(new Object[]{"a", "b"}, ", "));
        assertEquals("b, c", StringUtils.join(new Object[]{"a", "b", "c"}, ", ", 1, 3));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ", ", 2, 1));

        assertNull(StringUtils.join((Iterator<?>) null, ','));
        assertNull(StringUtils.join((Iterator<?>) null, ","));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), ','));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), ","));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ','));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ","));
        assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b").iterator(), ','));
        assertEquals("a--b", StringUtils.join(Arrays.asList("a", "b").iterator(), "--"));

        assertNull(StringUtils.join((List<?>) null, ','));
        assertNull(StringUtils.join((List<?>) null, ","));
        assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b"), ','));
        assertEquals("a--b", StringUtils.join(Arrays.asList("a", "b"), "--"));
    }

    @Test(timeout = 4000)
    public void testDeleteAndRemove() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("  a  b c  "));

        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));

        assertNull(StringUtils.removeStartIgnoreCase(null, "a"));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("domain.com", "www."));

        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", ".org"));

        assertNull(StringUtils.removeEndIgnoreCase(null, "a"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("www.domain.COM", StringUtils.removeEndIgnoreCase("www.domain.COM", ".org"));

        assertNull(StringUtils.remove((String) null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("abc", StringUtils.remove("abc", null));
        assertEquals("qd", StringUtils.remove("queued", "ue"));

        assertNull(StringUtils.remove((String) null, 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("abc", StringUtils.remove("abc", 'z'));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
    }

    @Test(timeout = 4000)
    public void testReplaceOperations() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("", StringUtils.replaceOnce("", "a", "b"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));

        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("any", StringUtils.replace("any", null, "b"));
        assertEquals("any", StringUtils.replace("any", "a", null));
        assertEquals("any", StringUtils.replace("any", "", "b"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));

        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));

        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        assertNull(StringUtils.replaceChars(null, "a", "b"));
        assertEquals("", StringUtils.replaceChars("", "a", "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));

        assertNull(StringUtils.overlay(null, "a", 0, 1));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 10, 12));
    }

    @Test(timeout = 4000)
    public void testChompAndChop() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("", StringUtils.chomp("\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc", StringUtils.chomp("abc"));

        assertNull(StringUtils.chomp(null, "a"));
        assertEquals("", StringUtils.chomp("", "a"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foo", StringUtils.chomp("foo", null));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("", StringUtils.chop("\r\n"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
    }

    @Test(timeout = 4000)
    public void testRepeatAndPadding() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("abcabc", StringUtils.repeat("abc", 2));

        assertNull(StringUtils.repeat(null, ",", 2));
        assertEquals("a, a, a", StringUtils.repeat("a", ", ", 3));
        assertEquals("aaa", StringUtils.repeat("a", null, 3));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat", StringUtils.rightPad("bat", -1));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("bat", StringUtils.leftPad("bat", -1));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));

        assertEquals(0, StringUtils.length(null));
        assertEquals(3, StringUtils.length("abc"));

        assertNull(StringUtils.center(null, 4));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
    }

    @Test(timeout = 4000)
    public void testCaseConversions() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertNull(StringUtils.upperCase(null, Locale.ENGLISH));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertNull(StringUtils.lowerCase(null, Locale.ENGLISH));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    @Test(timeout = 4000)
    public void testCharacterTypeTests() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abc", null));
        assertEquals(2, StringUtils.countMatches("abba", "a"));

        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab1c"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("ab1c"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("ab 1 c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-c"));

        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable("!ab-c~ "));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));

        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12-3"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  \t\n "));
        assertFalse(StringUtils.isWhitespace("  a "));

        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test(timeout = 4000)
    public void testDefaultsAndReversing() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("def", StringUtils.defaultString(null, "def"));
        assertEquals("abc", StringUtils.defaultString("abc", "def"));

        assertEquals("def", StringUtils.defaultIfEmpty(null, "def"));
        assertEquals("def", StringUtils.defaultIfEmpty("", "def"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "def"));

        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("cba", StringUtils.reverse("abc"));

        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    @Test(timeout = 4000)
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        assertNull(StringUtils.abbreviate(null, 0, 4));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 8, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));
    }

    @Test(timeout = 4000)
    public void testDifferenceAndCommonPrefix() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("abc", StringUtils.difference(null, "abc"));
        assertEquals("abc", StringUtils.difference("abc", null));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("xyz", StringUtils.difference("abcde", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", null));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference((String[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[0]));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{null, null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"", ""}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"", null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc", "abc"}));
        assertEquals(2, StringUtils.indexOfDifference(new String[]{"ab", "abxyz"}));
        assertEquals(1, StringUtils.indexOfDifference(new String[]{"abc", "a"}));

        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{null, null}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[]{"abc", "abxyz"}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc", "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"xyz", "abc"}));
    }

    @Test(timeout = 4000)
    public void testLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));
    }

    @Test(timeout = 4000)
    public void testStartsWithAndEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertFalse(StringUtils.startsWith("abc", "abcdef"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));

        assertFalse(StringUtils.startsWithAny(null, new String[]{"abc"}));
        assertFalse(StringUtils.startsWithAny("abc", (String[]) null));
        assertFalse(StringUtils.startsWithAny("abc", new String[0]));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{null, "xyz", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"xyz"}));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertFalse(StringUtils.endsWith("def", "abcdef"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPaddingExceedingPadLimit() {
        String paddedRight = StringUtils.rightPad("a", 8200, 'b');
        assertEquals(8200, paddedRight.length());
        assertEquals('a', paddedRight.charAt(0));
        assertEquals('b', paddedRight.charAt(8199));

        String paddedLeft = StringUtils.leftPad("a", 8200, 'b');
        assertEquals(8200, paddedLeft.length());
        assertEquals('b', paddedLeft.charAt(0));
        assertEquals('a', paddedLeft.charAt(8199));
    }

    @Test(timeout = 4000)
    public void testRepeatOptimizationBranches() {
        String repLen1 = StringUtils.repeat("x", 8200);
        assertEquals(8200, repLen1.length());

        String repLen2 = StringUtils.repeat("xy", 4);
        assertEquals("xyxyxyxy", repLen2);

        String repLen3 = StringUtils.repeat("xyz", 3);
        assertEquals("xyzxyzxyz", repLen3);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateWidthTooSmall() {
        StringUtils.abbreviate("abcdef", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateWithOffsetWidthTooSmall() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullArg1() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullArg2() {
        StringUtils.getLevenshteinDistance("a", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachMismatchedArrays() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"c"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachCircularInfiniteLoop() {
        StringUtils.replaceEachRepeatedly("a", new String[]{"a", "b"}, new String[]{"b", "a"});
    }
}