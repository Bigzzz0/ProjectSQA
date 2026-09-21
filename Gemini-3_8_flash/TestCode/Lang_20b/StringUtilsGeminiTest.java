package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.apache.commons.lang3.StringUtils
 * TARGET DEFECTS:
 *   - Defects4J Lang Bug (testJoin_ArrayChar, testJoin_Objectarray):
 *     NPE occurs when joining an array where the first element (startIndex) is null. The original
 *     code attempted `array[startIndex].toString().length()` without guarding against null, whereas
 *     null elements must be tolerated and represented as empty strings or default capacity.
 *
 * BRANCH COVERAGE & BOUNDARY CONDITIONS TARGETED:
 *   1. Join Operations:
 *      - Array containing null at startIndex (0) with char separator: join(new Object[]{null, "b"}, ',')
 *      - Array containing null at startIndex (0) with null/empty separator: join((Object[]) new Object[]{null})
 *      - Slices with startIndex >= endIndex, negative ranges, empty arrays, null arrays
 *      - Iterable and Iterator join paths with empty, single-element, and multi-element sets containing nulls
 *   2. Empty & Blank Checks:
 *      - CharSequence variants: null, empty (""), whitespace only (" \t\r\n"), non-whitespace (" a ")
 *   3. Strip & Trim Operations:
 *      - trim, trimToNull, trimToEmpty
 *      - strip, stripToNull, stripToEmpty with null/empty stripChars and custom stripChars
 *      - stripStart and stripEnd with partial/complete strips and surrogate pairs
 *      - stripAccents with standard, accented (e.g., "éclair"), and null inputs
 *   4. Substring & Extraction:
 *      - substring, left, right, mid with negative bounds, out-of-bound start/end, start > end
 *      - substringBefore, substringAfter, substringBeforeLast, substringAfterLast with missing/empty tokens
 *      - substringBetween and substringsBetween with missing delimiters, unmatched tags, empty results
 *   5. IndexOf & Search:
 *      - indexOf, lastIndexOf, ordinalIndexOf, lastOrdinalIndexOf, indexOfIgnoreCase, lastIndexOfIgnoreCase
 *      - indexOfAny, indexOfAnyBut, containsAny, containsOnly, containsNone
 *      - Unicode Supplementary characters / surrogate pairs (e.g. \uD83D\uDE00)
 *      - indexOfDifference, difference, getCommonPrefix with all-null, partially-null, disjoint, identical
 *   6. Splitting & Joining:
 *      - split, splitPreserveAllTokens, splitByWholeSeparator, splitByWholeSeparatorPreserveAllTokens
 *      - splitByCharacterType, splitByCharacterTypeCamelCase across casing boundaries
 *   7. Replacement & Overlay:
 *      - replace, replaceOnce, replaceEach, replaceEachRepeatedly
 *      - replaceChars (string and char), overlay with inverted start/end indices
 *   8. Padding & Centering:
 *      - repeat (lengths 0, 1, 2, >2; repeat counts <=0, 1, large), repeat with separator
 *      - leftPad, rightPad, center with characters, strings, padLimit boundaries (>8192)
 *   9. Levenshtein Distance:
 *      - Levenshtein with & without threshold; edge boundaries (threshold exceeded, threshold == distance)
 *      - Stripe limit checks, string size swaps (n > m)
 *  10. Character Tests & Case Conversion:
 *      - isAlpha, isAlphaSpace, isAlphanumeric, isAsciiPrintable, isNumeric, isWhitespace
 *      - isAllLowerCase, isAllUpperCase, swapCase, capitalize, uncapitalize, upperCase, lowerCase
 * ----------------------------------------------------------------------------------------------------
 */
public class StringUtilsGeminiTest {

    // ==============================================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Lang-20 Regression Tests)
    // ==============================================================================================

    @Test(timeout = 4000)
    public void testDefectTarget_JoinArrayCharWithNullAtStartIndex() {
        // Targets defect where array[startIndex] is null when calculating initial StringBuilder capacity
        Object[] arrayWithNullFirst = new Object[]{null, "foo", "bar"};
        String result = StringUtils.join(arrayWithNullFirst, ';');
        assertEquals(";foo;bar", result);

        Object[] singleNull = new Object[]{null};
        assertEquals("", StringUtils.join(singleNull, ','));

        Object[] nullsOnly = new Object[]{null, null};
        assertEquals(",", StringUtils.join(nullsOnly, ','));

        Object[] mixedWithSubrange = new Object[]{"skip", null, "target"};
        assertEquals(";target", StringUtils.join(mixedWithSubrange, ';', 1, 3));
    }

    @Test(timeout = 4000)
    public void testDefectTarget_JoinObjectArrayWithNullAtStartIndex() {
        // Targets defect in join(Object[]) and join(Object[], String) with null at startIndex
        Object[] singleNull = new Object[]{null};
        assertEquals("", StringUtils.join(singleNull));
        assertEquals("", StringUtils.join(singleNull, (String) null));

        Object[] nullFirst = new Object[]{null, "a", "b"};
        assertEquals("ab", StringUtils.join(nullFirst));
        assertEquals("-a-b", StringUtils.join(nullFirst, "-"));

        Object[] subArrayNullFirst = new Object[]{"first", null, "second"};
        assertEquals("second", StringUtils.join(subArrayNullFirst, null, 1, 3));
        assertEquals(":second", StringUtils.join(subArrayNullFirst, ":", 1, 3));
    }

    // ==============================================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // ==============================================================================================

    @Test(timeout = 4000)
    public void testEmptyAndBlankChecks() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t\r\n "));
        assertFalse(StringUtils.isBlank(" bob "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("bob"));
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
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("xxabcxx", "x"));
        assertEquals("abc", StringUtils.strip("xyzabcxyz", "xyz"));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc  "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals("abcxx", StringUtils.stripStart("xxabcxx", "x"));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals("xxabc", StringUtils.stripEnd("xxabcxx", "x"));

        assertNull(StringUtils.stripAll((String[]) null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{" a ", " b ", null}));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"xa", "bx"}, "x"));
    }

    @Test(timeout = 4000)
    public void testStripAccents() {
        assertNull(StringUtils.stripAccents(null));
        assertEquals("", StringUtils.stripAccents(""));
        assertEquals("control", StringUtils.stripAccents("control"));
        assertEquals("eclair", StringUtils.stripAccents("éclair"));
        assertEquals("a e i o u", StringUtils.stripAccents("à é î ô ù"));
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
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abcd", "abc"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndLastIndexOfMethods() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 9));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.lastIndexOf(null, "b"));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", (String) null));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b", 4));
    }

    @Test(timeout = 4000)
    public void testOrdinalIndexOfAndLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", -1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(3, StringUtils.ordinalIndexOf("aabaabaa", "a", 3));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));

        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaabaa", null, 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 0));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 3));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));
    }

    @Test(timeout = 4000)
    public void testIndexOfIgnoreCaseAndLastIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 10));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));

        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", null));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 4));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 10));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "", 4));
    }

    @Test(timeout = 4000)
    public void testContainsAndContainsIgnoreCase() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", (String) null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));

        assertFalse(StringUtils.containsIgnoreCase(null, "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));

        assertFalse(StringUtils.containsWhitespace(null));
        assertFalse(StringUtils.containsWhitespace(""));
        assertFalse(StringUtils.containsWhitespace("abc"));
        assertTrue(StringUtils.containsWhitespace("a b c"));
        assertTrue(StringUtils.containsWhitespace("abc\t"));
    }

    @Test(timeout = 4000)
    public void testAnyAllNoneChecks() {
        assertEquals(-1, StringUtils.indexOfAny(null, 'a', 'b'));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", 'a', 'b'));
        assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));

        assertEquals(-1, StringUtils.indexOfAny(null, "ab"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        assertFalse(StringUtils.containsAny(null, 'a'));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertFalse(StringUtils.containsAny("aba", 'z'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertFalse(StringUtils.containsAny("zzabyycdxx", (String) null));

        assertEquals(-1, StringUtils.indexOfAnyBut(null, 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[0]));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", 'z', 'a'));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", 'a', 'b'));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));

        assertFalse(StringUtils.containsOnly(null, 'a'));
        assertFalse(StringUtils.containsOnly("abc", (char[]) null));
        assertTrue(StringUtils.containsOnly("", 'a'));
        assertFalse(StringUtils.containsOnly("abc", new char[0]));
        assertTrue(StringUtils.containsOnly("abab", 'a', 'b'));
        assertFalse(StringUtils.containsOnly("ab1", 'a', 'b'));
        assertTrue(StringUtils.containsOnly("abab", "ab"));
        assertFalse(StringUtils.containsOnly("ab", (String) null));

        assertTrue(StringUtils.containsNone(null, 'a'));
        assertTrue(StringUtils.containsNone("abc", (char[]) null));
        assertTrue(StringUtils.containsNone("", 'a'));
        assertTrue(StringUtils.containsNone("abab", 'x', 'y'));
        assertFalse(StringUtils.containsNone("ab1", '1', 'y'));
        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertTrue(StringUtils.containsNone("ab", (String) null));
    }

    @Test(timeout = 4000)
    public void testSupplementarySurrogatePairsInSearch() {
        // Unicode Supplementary Character (e.g., U+1F600 GRINNING FACE: \uD83D\uDE00)
        String surrogateString = "hello \uD83D\uDE00 world";
        char high = '\uD83D';
        char low = '\uDE00';

        assertEquals(6, StringUtils.indexOfAny(surrogateString, high, low));
        assertTrue(StringUtils.containsAny(surrogateString, high, low));
        assertFalse(StringUtils.containsNone(surrogateString, high, low));

        // High surrogate present without matching low surrogate
        assertEquals(6, StringUtils.indexOfAny(surrogateString, high));
        assertTrue(StringUtils.containsAny(surrogateString, high));

        // Testing indexOfAnyBut across surrogate pairs
        assertEquals(0, StringUtils.indexOfAnyBut(surrogateString, high, low));
        assertEquals(6, StringUtils.indexOfAnyBut("\uD83D\uDE00\uD83D\uDE00X", high, low));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyStringArrays() {
        assertEquals(-1, StringUtils.indexOfAny(null, "a", "b"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String[]) null));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", "cd", "ab"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "mn", "op"));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", (String) null, ""));

        assertEquals(-1, StringUtils.lastIndexOfAny(null, "a", "b"));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", (String[]) null));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", "ab", "cd"));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", ""));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", "mn", "op"));
    }

    @Test(timeout = 4000)
    public void testSubstringExtractions() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -5));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
        assertEquals("", StringUtils.substring("abc", -1, -2));

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
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("ab", StringUtils.mid("abc", -2, 2));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAfterAndBetween() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "z"));
        assertEquals("", StringUtils.substringBefore("abc", ""));

        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "z"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));

        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", "z"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));

        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "z"));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));

        assertNull(StringUtils.substringBetween(null, "tag"));
        assertNull(StringUtils.substringBetween("tagabctag", null));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));
        assertEquals("b", StringUtils.substringBetween("wx[b]yz", "[", "]"));
        assertNull(StringUtils.substringBetween("wx[byz", "[", "]"));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("[a]", null, "]"));
        assertNull(StringUtils.substringsBetween("[a]", "[", null));
        assertNull(StringUtils.substringsBetween("[a]", "", "]"));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplittingMethods() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split(" abc def "));

        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));

        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 0));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", -1));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab cd ef", null, -1));

        assertNull(StringUtils.splitPreserveAllTokens(null));
        assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def"));
        assertArrayEquals(new String[]{"", "abc", ""}, StringUtils.splitPreserveAllTokens(" abc "));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.splitPreserveAllTokens("a:b:c", ":", 2));

        assertNull(StringUtils.splitByWholeSeparator(null, "::"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", "::"));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab  de fg", null));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));

        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, "::"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", "::"));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!----!-cd", "-!-"));
        assertArrayEquals(new String[]{"ab", "-!-cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!----!-cd", "-!-", 2));

        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));

        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"foo", "200", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test(timeout = 4000)
    public void testJoinIterableAndIterator() {
        assertNull(StringUtils.join((Iterable<?>) null, ','));
        assertNull(StringUtils.join((Iterable<?>) null, ","));
        assertNull(StringUtils.join((Iterator<?>) null, ','));
        assertNull(StringUtils.join((Iterator<?>) null, ","));

        List<String> emptyList = Collections.emptyList();
        assertEquals("", StringUtils.join(emptyList, ','));
        assertEquals("", StringUtils.join(emptyList, ","));
        assertEquals("", StringUtils.join(emptyList.iterator(), ','));
        assertEquals("", StringUtils.join(emptyList.iterator(), ","));

        List<String> singleList = Collections.singletonList("a");
        assertEquals("a", StringUtils.join(singleList, ','));
        assertEquals("a", StringUtils.join(singleList, ","));
        assertEquals("a", StringUtils.join(singleList.iterator(), ','));
        assertEquals("a", StringUtils.join(singleList.iterator(), ","));

        List<String> multiList = Arrays.asList("a", "b", null, "c");
        assertEquals("a,b,,c", StringUtils.join(multiList, ','));
        assertEquals("a--b----c", StringUtils.join(multiList, "--"));
        assertEquals("a,b,,c", StringUtils.join(multiList.iterator(), ','));
        assertEquals("a--b----c", StringUtils.join(multiList.iterator(), "--"));
    }

    @Test(timeout = 4000)
    public void testDeleteWhitespaceAndRemoveMethods() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace(" a b  c \t\r\n"));

        assertNull(StringUtils.removeStart(null, "www."));
        assertEquals("", StringUtils.removeStart("", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", null));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));

        assertNull(StringUtils.removeStartIgnoreCase(null, "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));

        assertNull(StringUtils.removeEnd(null, ".com"));
        assertEquals("", StringUtils.removeEnd("", ".com"));
        assertEquals("domain", StringUtils.removeEnd("domain.com", ".com"));
        assertEquals("domain.com", StringUtils.removeEnd("domain.com", ".org"));

        assertNull(StringUtils.removeEndIgnoreCase(null, ".com"));
        assertEquals("domain", StringUtils.removeEndIgnoreCase("domain.COM", ".com"));

        assertNull(StringUtils.remove(null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("abc", StringUtils.remove("abc", null));
        assertEquals("abc", StringUtils.remove("abc", ""));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("queued", StringUtils.remove("queued", "zz"));

        assertNull(StringUtils.remove(null, 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("queued", StringUtils.remove("queued", 'z'));
    }

    @Test(timeout = 4000)
    public void testReplaceOperations() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));
        assertEquals("abaa", StringUtils.replace("abaa", "a", "z", 0));
        assertEquals("abaa", StringUtils.replace("abaa", "a", null, -1));

        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"b"}));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));

        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertNull(StringUtils.replaceChars(null, "a", "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
    }

    @Test(timeout = 4000)
    public void testOverlayChompAndChop() {
        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));

        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc\r\n", StringUtils.chomp("abc\r\n\r\n"));
        assertEquals("a", StringUtils.chomp("a"));

        assertNull(StringUtils.chomp(null, "bar"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foo", StringUtils.chomp("foo", ""));
        assertEquals("foo", StringUtils.chomp("foo", null));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));

        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("abc", StringUtils.chop("abc\n"));
        assertEquals("abc", StringUtils.chop("abc\r"));
    }

    @Test(timeout = 4000)
    public void testRepeatAndPaddingMethods() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("ababab", StringUtils.repeat("ab", 3));
        assertEquals("abcabc", StringUtils.repeat("abc", 2));

        assertNull(StringUtils.repeat(null, ",", 2));
        assertEquals("?,?,?", StringUtils.repeat("?", ",", 3));
        assertEquals("???", StringUtils.repeat("?", null, 3));

        assertEquals("eee", StringUtils.repeat('e', 3));
        assertEquals("", StringUtils.repeat('e', 0));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 2));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat", StringUtils.leftPad("bat", 2));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));

        assertNull(StringUtils.center(null, 4));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" a  ", StringUtils.center("a", 4));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, (String) null));
    }

    @Test(timeout = 4000)
    public void testCaseConversionsAndCharacterTests() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAT", StringUtils.capitalize("cAT"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));

        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abba", null));
        assertEquals(0, StringUtils.countMatches("abba", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));

        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("ab c"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertFalse(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("ab12"));

        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("ab 12"));

        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable("!ab-c~"));
        assertFalse(StringUtils.isAsciiPrintable("ab\u007fc"));

        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("12345"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("12 34"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  \t\n "));
        assertFalse(StringUtils.isWhitespace("  a  "));

        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));

        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("ABc"));
    }

    @Test(timeout = 4000)
    public void testDefaultsAndReversing() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("bat", StringUtils.defaultString("bat"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("bat", StringUtils.defaultString("bat", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("   ", "NULL"));
        assertEquals("bat", StringUtils.defaultIfBlank("bat", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals(" ", StringUtils.defaultIfEmpty(" ", "NULL"));
        assertEquals("bat", StringUtils.defaultIfEmpty("bat", "NULL"));

        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("tab", StringUtils.reverse("bat"));

        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("", StringUtils.reverseDelimited("", '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
    }

    @Test(timeout = 4000)
    public void testAbbreviationMethods() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 4, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));

        assertNull(StringUtils.abbreviateMiddle(null, ".", 4));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 4));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    @Test(timeout = 4000)
    public void testDifferenceAndCommonPrefix() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("abc", StringUtils.difference("", "abc"));
        assertEquals("", StringUtils.difference("abc", ""));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("xyz", StringUtils.difference("ab", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", null));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));

        assertEquals(-1, StringUtils.indexOfDifference((CharSequence[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[0]));
        assertEquals(-1, StringUtils.indexOfDifference("abc"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(0, StringUtils.indexOfDifference("", "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", null, "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz", "ab123"));
        assertEquals(2, StringUtils.indexOfDifference("abcde", "abxyz"));

        assertEquals("", StringUtils.getCommonPrefix((String[]) null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("abc", StringUtils.getCommonPrefix("abc"));
        assertEquals("ab", StringUtils.getCommonPrefix("ab", "abxyz"));
        assertEquals("i am a ", StringUtils.getCommonPrefix("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.getCommonPrefix("abc", null));
    }

    @Test(timeout = 4000)
    public void testLevenshteinDistanceOperations() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));

        assertEquals(0, StringUtils.getLevenshteinDistance("", "", 0));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 8));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", "", 7));
        assertEquals(-1, StringUtils.getLevenshteinDistance("aaapppp", "", 6));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog", 1));
        assertEquals(-1, StringUtils.getLevenshteinDistance("fly", "ant", 2));
        assertEquals(-1, StringUtils.getLevenshteinDistance("12345", "123456789", 1));
    }

    @Test(timeout = 4000)
    public void testPrefixSuffixAndNormalizeSpace() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith("abc", null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));

        assertFalse(StringUtils.startsWithAny(null, "abc"));
        assertFalse(StringUtils.startsWithAny("abc", (String[]) null));
        assertFalse(StringUtils.startsWithAny("abc", new String[0]));
        assertTrue(StringUtils.startsWithAny("abcxyz", "xyz", "abc"));
        assertFalse(StringUtils.startsWithAny("abcxyz", "def", "ghi"));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith("def", null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));

        assertFalse(StringUtils.endsWithAny(null, "abc"));
        assertFalse(StringUtils.endsWithAny("abc", (String[]) null));
        assertFalse(StringUtils.endsWithAny("abc", new String[0]));
        assertTrue(StringUtils.endsWithAny("abcxyz", "def", "xyz"));
        assertFalse(StringUtils.endsWithAny("abcxyz", "def", "ghi"));

        assertNull(StringUtils.normalizeSpace(null));
        assertEquals("", StringUtils.normalizeSpace(""));
        assertEquals("a b c", StringUtils.normalizeSpace("  a \t\r\n b   c  "));
    }

    // ==============================================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // ==============================================================================================

    @Test(timeout = 4000)
    public void testLengthBoundary() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(0, StringUtils.length(""));
        assertEquals(5, StringUtils.length("hello"));
    }

    @Test(timeout = 4000)
    public void testRightPadAndLeftPadExtremeLimits() {
        // Pad size larger than PAD_LIMIT (8192) invokes the repeat(String, int) branch
        String base = "test";
        int extremeSize = 8200;
        String paddedRight = StringUtils.rightPad(base, extremeSize, 'x');
        assertEquals(extremeSize, paddedRight.length());
        assertTrue(paddedRight.startsWith("test"));
        assertTrue(paddedRight.endsWith("x"));

        String paddedLeft = StringUtils.leftPad(base, extremeSize, 'y');
        assertEquals(extremeSize, paddedLeft.length());
        assertTrue(paddedLeft.startsWith("y"));
        assertTrue(paddedLeft.endsWith("test"));
    }

    @Test(timeout = 4000)
    public void testJoinSubRanges() {
        Object[] array = new Object[]{"a", "b", "c", "d"};
        assertEquals("", StringUtils.join(array, ',', 2, 2));
        assertEquals("", StringUtils.join(array, ',', 3, 2));
        assertEquals("b,c", StringUtils.join(array, ',', 1, 3));
        assertEquals("b-c", StringUtils.join(array, "-", 1, 3));
        assertEquals("bc", StringUtils.join(array, null, 1, 3));
    }

    // ==============================================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // ==============================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateWidthTooSmall() {
        StringUtils.abbreviate("abcdef", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetWidthTooSmall() {
        StringUtils.abbreviate("abcdefghijkl", 5, 6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullFirstParam() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNullSecondParam() {
        StringUtils.getLevenshteinDistance("abc", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinNegativeThreshold() {
        StringUtils.getLevenshteinDistance("abc", "def", -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachMismatchedArrays() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"z"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachRepeatedlyCircularLoop() {
        StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    // ==============================================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // ==============================================================================================

    @Test(timeout = 4000)
    public void testPublicConstructorForJavaBeanSupport() {
        // Contract requirement: constructor is public to permit tools that require JavaBean instances
        StringUtils instance = new StringUtils();
        assertNotNull(instance);
    }
}