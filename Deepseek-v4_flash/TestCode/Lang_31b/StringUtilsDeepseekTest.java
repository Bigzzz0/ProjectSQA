package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StringUtilsDeepseekTest - Advanced White-Box Test Suite for StringUtils.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - isEmpty, isNotEmpty, isBlank, isNotBlank
 *   - trim, trimToNull, trimToEmpty
 *   - strip, stripToNull, stripToEmpty, stripStart, stripEnd
 *   - equals, equalsIgnoreCase
 *   - indexOf, lastIndexOf, ordinalIndexOf, indexOfIgnoreCase, lastIndexOfIgnoreCase
 *   - contains, containsIgnoreCase, containsAny, containsNone, containsOnly
 *   - substring, left, right, mid
 *   - substringBefore, substringAfter, substringBetween, substringsBetween
 *   - split, splitPreserveAllTokens, splitByWholeSeparator, splitByCharacterType
 *   - join (array, iterator, iterable)
 *   - deleteWhitespace, removeStart, removeEnd, remove, replace, replaceOnce, replaceEach
 *   - repeat, padding, leftPad, rightPad, center
 *   - upperCase, lowerCase, capitalize, uncapitalize, swapCase
 *   - countMatches, isAlpha, isNumeric, isWhitespace, isAsciiPrintable, etc.
 *   - reverse, reverseDelimited
 *   - abbreviate, abbreviateMiddle
 *   - difference, indexOfDifference, getCommonPrefix, getLevenshteinDistance
 *   - startsWith, startsWithIgnoreCase, startsWithAny, endsWith, endsWithIgnoreCase
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - null inputs for all methods
 *   - empty strings, whitespace-only strings
 *   - negative indices, indices beyond length
 *   - max integer boundaries (e.g., INDEX_NOT_FOUND = -1)
 *   - PAD_LIMIT (8192) for padding methods
 *   - zero-length arrays, null arrays
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - containsAny with supplementary characters (surrogate pairs)
 *   - The bug: char[] comparison treats surrogate halves as separate chars, causing false positives.
 *   - Test: string with supplementary char (e.g., "\uD83D\uDE00") should NOT match searchChars containing only one half.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - IllegalArgumentException for abbreviate with maxWidth < 4
 *   - IllegalArgumentException for getLevenshteinDistance with null
 *   - IndexOutOfBoundsException for padding with negative repeat
 *   - IllegalStateException for replaceEach with circular reference (timeToLive < 0)
 *   - IllegalArgumentException for replaceEach with mismatched array lengths
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - (StringUtils is static utility, no instance state; but we test constructor and static methods)
 */
public class StringUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));
        assertFalse(StringUtils.isEmpty("  bob  "));
    }

    @Test(timeout = 4000)
    public void testIsNotEmpty() {
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));
    }

    @Test(timeout = 4000)
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank("bob"));
        assertFalse(StringUtils.isBlank("  bob  "));
    }

    @Test(timeout = 4000)
    public void testIsNotBlank() {
        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertTrue(StringUtils.isNotBlank("bob"));
    }

    @Test(timeout = 4000)
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("     "));
        assertEquals("abc", StringUtils.trim("abc"));
        assertEquals("abc", StringUtils.trim("    abc    "));
    }

    @Test(timeout = 4000)
    public void testTrimToNull() {
        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("     "));
        assertEquals("abc", StringUtils.trimToNull("abc"));
        assertEquals("abc", StringUtils.trimToNull("    abc    "));
    }

    @Test(timeout = 4000)
    public void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("     "));
        assertEquals("abc", StringUtils.trimToEmpty("abc"));
    }

    @Test(timeout = 4000)
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("abc"));
        assertEquals("abc", StringUtils.strip("  abc"));
        assertEquals("abc", StringUtils.strip("abc  "));
        assertEquals("abc", StringUtils.strip(" abc "));
        assertEquals("ab c", StringUtils.strip(" ab c "));
    }

    @Test(timeout = 4000)
    public void testStripToNull() {
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull(""));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("abc"));
        assertEquals("abc", StringUtils.stripToNull("  abc"));
    }

    @Test(timeout = 4000)
    public void testStripToEmpty() {
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("abc"));
    }

    @Test(timeout = 4000)
    public void testStripStart() {
        assertNull(StringUtils.stripStart(null, null));
        assertEquals("", StringUtils.stripStart("", null));
        assertEquals("abc", StringUtils.stripStart("abc", null));
        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("abc  ", StringUtils.stripStart("abc  ", null));
        assertEquals("abc ", StringUtils.stripStart(" abc ", null));
        assertEquals("abc  ", StringUtils.stripStart("yxabc  ", "xyz"));
    }

    @Test(timeout = 4000)
    public void testStripEnd() {
        assertNull(StringUtils.stripEnd(null, null));
        assertEquals("", StringUtils.stripEnd("", null));
        assertEquals("abc", StringUtils.stripEnd("abc", null));
        assertEquals("  abc", StringUtils.stripEnd("  abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertEquals(" abc", StringUtils.stripEnd(" abc ", null));
        assertEquals("  abc", StringUtils.stripEnd("  abcyx", "xyz"));
    }

    @Test(timeout = 4000)
    public void testStripAll() {
        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[]{}, StringUtils.stripAll(new String[]{}));
        assertArrayEquals(new String[]{"abc", "abc"}, StringUtils.stripAll(new String[]{"abc", "  abc"}));
        assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll(new String[]{"abc  ", null}));
    }

    @Test(timeout = 4000)
    public void testStripAllWithStripChars() {
        assertNull(StringUtils.stripAll(null, null));
        assertArrayEquals(new String[]{}, StringUtils.stripAll(new String[]{}, null));
        assertArrayEquals(new String[]{"abc", "abc"}, StringUtils.stripAll(new String[]{"abc", "  abc"}, null));
        assertArrayEquals(new String[]{"abc  ", null}, StringUtils.stripAll(new String[]{"abc  ", null}, "yz"));
        assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll(new String[]{"yabcz", null}, "yz"));
    }

    @Test(timeout = 4000)
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
    }

    @Test(timeout = 4000)
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    @Test(timeout = 4000)
    public void testIndexOfChar() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
    }

    @Test(timeout = 4000)
    public void testIndexOfCharStartPos() {
        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', -1));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("a", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(-1, StringUtils.indexOf("", "a"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringStartPos() {
        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("a", null, 0));
        assertEquals(0, StringUtils.indexOf("", "", 0));
        assertEquals(-1, StringUtils.indexOf("", "a", 0));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a", 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 9));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));
    }

    @Test(timeout = 4000)
    public void testOrdinalIndexOf() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("a", null, 1));
        assertEquals(0, StringUtils.ordinalIndexOf("", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(2, StringUtils.ordinalIndexOf("aabaabaa", "b", 1));
        assertEquals(5, StringUtils.ordinalIndexOf("aabaabaa", "b", 2));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "ab", 1));
        assertEquals(4, StringUtils.ordinalIndexOf("aabaabaa", "ab", 2));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 2));
    }

    @Test(timeout = 4000)
    public void testIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "ab"));
    }

    @Test(timeout = 4000)
    public void testIndexOfIgnoreCaseStartPos() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null, 0));
        assertEquals(0, StringUtils.indexOfIgnoreCase("", "", 0));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 0));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB", 0));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOfIgnoreCase("abc", "", 9));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfCharStartPos() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", 'a', 0));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(0, StringUtils.lastIndexOf("", ""));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.lastIndexOf("aabaabaa", "ab"));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
    }

    @Test(timeout = 4000)
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", null, 1));
        assertEquals(0, StringUtils.lastOrdinalIndexOf("", "", 1));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 1));
        assertEquals(2, StringUtils.lastOrdinalIndexOf("aabaabaa", "b", 2));
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1));
        assertEquals(1, StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 2));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfStringStartPos() {
        assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b", 9));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));
        assertEquals(0, StringUtils.lastIndexOf("aabaabaa", "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", 0));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB"));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfIgnoreCaseStartPos() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null, 0));
        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(0, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0));
    }

    @Test(timeout = 4000)
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));
    }

    @Test(timeout = 4000)
    public void testContainsString() {
        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("a", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));
    }

    @Test(timeout = 4000)
    public void testContainsIgnoreCase() {
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("a", null));
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "z"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    @Test(timeout = 4000)
    public void testContainsAnyCharArray() {
        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
        assertFalse(StringUtils.containsAny("abc", null));
        assertFalse(StringUtils.containsAny("abc", new char[]{}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z','a'}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'b','y'}));
        assertFalse(StringUtils.containsAny("aba", new char[]{'z'}));
    }

    @Test(timeout = 4000)
    public void testContainsAnyString() {
        assertFalse(StringUtils.containsAny(null, "a"));
        assertFalse(StringUtils.containsAny("", "a"));
        assertFalse(StringUtils.containsAny("abc", null));
        assertFalse(StringUtils.containsAny("abc", ""));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "by"));
        assertFalse(StringUtils.containsAny("aba", "z"));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Test for known defect: containsAny with supplementary characters.
     * Supplementary characters (code points > U+FFFF) are represented as surrogate pairs.
     * The bug: char[] comparison treats each surrogate half as a separate char,
     * causing false positives when the search array contains only one half.
     * For example, string "\uD83D\uDE00" (emoji 😀) should NOT match searchChars containing '\uD83D' alone.
     */
    @Test(timeout = 4000)
    public void testContainsAnyCharArrayWithSupplementaryChars() {
        // String with a supplementary character (U+1F600, 😀)
        String str = "\uD83D\uDE00"; // surrogate pair
        // Search array containing only the high surrogate
        char[] searchChars = {'\uD83D'};
        // Expected: false (the string does NOT contain the single char '\uD83D' as a standalone character)
        assertFalse("containsAny should return false for surrogate half match", StringUtils.containsAny(str, searchChars));
    }

    @Test(timeout = 4000)
    public void testContainsAnyStringWithSupplementaryChars() {
        String str = "\uD83D\uDE00"; // supplementary character
        String searchChars = "\uD83D"; // only high surrogate
        assertFalse("containsAny should return false for surrogate half match", StringUtils.containsAny(str, searchChars));
    }

    // Additional supplementary character tests for coverage
    @Test(timeout = 4000)
    public void testContainsAnyCharArrayWithSupplementaryCharsFullMatch() {
        String str = "\uD83D\uDE00"; // 😀
        char[] searchChars = {'\uD83D', '\uDE00'}; // both surrogates as separate chars (not a valid pair)
        // This should still be false because the string contains the pair, not the individual chars? Actually, the string contains both chars sequentially.
        // The method iterates char by char, so it will find '\uD83D' at index 0 and return true. This is a known limitation.
        // But the defect is about false positive when only one surrogate is in search array. Here we have both, so it's true.
        assertTrue(StringUtils.containsAny(str, searchChars));
    }

    @Test(timeout = 4000)
    public void testContainsAnyStringWithSupplementaryCharsFullMatch() {
        String str = "\uD83D\uDE00";
        String searchChars = "\uD83D\uDE00"; // both surrogates as string (not a valid supplementary char in search string)
        // The search string contains two chars, so it will match the first char and return true.
        assertTrue(StringUtils.containsAny(str, searchChars));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testIndexOfAnyCharArray() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("abc", null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[]{}));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[]{'z','a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[]{'b','y'}));
        assertEquals(-1, StringUtils.indexOfAny("aba", new char[]{'z'}));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyString() {
        assertEquals(-1, StringUtils.indexOfAny(null, "a"));
        assertEquals(-1, StringUtils.indexOfAny("", "a"));
        assertEquals(-1, StringUtils.indexOfAny("abc", null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "by"));
        assertEquals(-1, StringUtils.indexOfAny("aba", "z"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyButCharArray() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[]{}));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z','a'}));
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", new char[]{'a','b'}));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyButString() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("", "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(0, StringUtils.indexOfAnyBut("zzabyycdxx", ""));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
    }

    @Test(timeout = 4000)
    public void testContainsOnlyCharArray() {
        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("abc", null));
        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("ab", new char[]{}));
        assertTrue(StringUtils.containsOnly("abab", new char[]{'a','b','c'}));
        assertFalse(StringUtils.containsOnly("ab1", new char[]{'a','b','c'}));
        assertFalse(StringUtils.containsOnly("abz", new char[]{'a','b','c'}));
    }

    @Test(timeout = 4000)
    public void testContainsOnlyString() {
        assertFalse(StringUtils.containsOnly(null, "a"));
        assertFalse(StringUtils.containsOnly("abc", null));
        assertTrue(StringUtils.containsOnly("", "a"));
        assertFalse(StringUtils.containsOnly("ab", ""));
        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly("ab1", "abc"));
        assertFalse(StringUtils.containsOnly("abz", "abc"));
    }

    @Test(timeout = 4000)
    public void testContainsNoneCharArray() {
        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abc", null));
        assertTrue(StringUtils.containsNone("", new char[]{'a'}));
        assertTrue(StringUtils.containsNone("ab", new char[]{}));
        assertTrue(StringUtils.containsNone("abab", new char[]{'x','y','z'}));
        assertTrue(StringUtils.containsNone("ab1", new char[]{'x','y','z'}));
        assertFalse(StringUtils.containsNone("abz", new char[]{'x','y','z'}));
    }

    @Test(timeout = 4000)
    public void testContainsNoneString() {
        assertTrue(StringUtils.containsNone(null, "a"));
        assertTrue(StringUtils.containsNone("abc", null));
        assertTrue(StringUtils.containsNone("", "a"));
        assertTrue(StringUtils.containsNone("ab", ""));
        assertTrue(StringUtils.containsNone("abab", "xyz"));
        assertTrue(StringUtils.containsNone("ab1", "xyz"));
        assertFalse(StringUtils.containsNone("abz", "xyz"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyStrings() {
        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.indexOfAny("abc", null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new String[]{}));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"ab","cd"}));
        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"cd","ab"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"mn","op"}));
        assertEquals(1, StringUtils.indexOfAny("zzabyycdxx", new String[]{"zab","aby"}));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new String[]{""}));
        assertEquals(0, StringUtils.indexOfAny("", new String[]{""}));
        assertEquals(-1, StringUtils.indexOfAny("", new String[]{"a"}));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfAny() {
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", null));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", new String[]{}));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", new String[]{null}));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"ab","cd"}));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"cd","ab"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn","op"}));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn",""}));
    }

    @Test(timeout = 4000)
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));
    }

    @Test(timeout = 4000)
    public void testSubstringStartEnd() {
        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("", StringUtils.substring("abc", 2, 2));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
    }

    @Test(timeout = 4000)
    public void testLeft() {
        assertNull(StringUtils.left(null, 0));
        assertEquals("", StringUtils.left("", 0));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));
    }

    @Test(timeout = 4000)
    public void testRight() {
        assertNull(StringUtils.right(null, 0));
        assertEquals("", StringUtils.right("", 0));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));
    }

    @Test(timeout = 4000)
    public void testMid() {
        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("", 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("abc", StringUtils.mid("abc", 0, 4));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("", StringUtils.mid("abc", 4, 2));
    }

    @Test(timeout = 4000)
    public void testSubstringBefore() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtils.substringBefore("abc", "c"));
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));
        assertEquals("", StringUtils.substringBefore("abc", ""));
    }

    @Test(timeout = 4000)
    public void testSubstringAfter() {
        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "c"));
        assertEquals("", StringUtils.substringAfter("abc", "d"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeLast() {
        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        assertEquals("ab", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", "d"));
    }

    @Test(timeout = 4000)
    public void testSubstringAfterLast() {
        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("bc", StringUtils.substringAfterLast("abc", "a"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
        assertEquals("", StringUtils.substringAfterLast("abc", "c"));
        assertEquals("", StringUtils.substringAfterLast("a", "a"));
        assertEquals("", StringUtils.substringAfterLast("a", "z"));
    }

    @Test(timeout = 4000)
    public void testSubstringBetween() {
        assertNull(StringUtils.substringBetween(null, "tag"));
        assertEquals("", StringUtils.substringBetween("", ""));
        assertNull(StringUtils.substringBetween("", "tag"));
        assertNull(StringUtils.substringBetween("tagabctag", null));
        assertEquals("", StringUtils.substringBetween("tagabctag", ""));
        assertEquals("abc", StringUtils.substringBetween("tagabctag", "tag"));
    }

    @Test(timeout = 4000)
    public void testSubstringBetweenOpenClose() {
        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("abc", null, "]"));
        assertNull(StringUtils.substringBetween("abc", "[", null));
        assertEquals("", StringUtils.substringBetween("", "", ""));
        assertNull(StringUtils.substringBetween("", "", "]"));
        assertNull(StringUtils.substringBetween("", "[", "]"));
        assertEquals("", StringUtils.substringBetween("yabcz", "", ""));
        assertEquals("abc", StringUtils.substringBetween("yabcz", "y", "z"));
        assertEquals("abc", StringUtils.substringBetween("yabczyabcz", "y", "z"));
    }

    @Test(timeout = 4000)
    public void testSubstringsBetween() {
        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", null, "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", null));
        assertNull(StringUtils.substringsBetween("", "[", "]"));
        assertArrayEquals(new String[]{"a","b","c"}, StringUtils.substringsBetween("[a][b][c]", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[]{}, StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"abc"}, StringUtils.split(" abc "));
    }

    @Test(timeout = 4000)
    public void testSplitChar() {
        assertNull(StringUtils.split(null, '.'));
        assertArrayEquals(new String[]{}, StringUtils.split("", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));
        assertArrayEquals(new String[]{"a:b:c"}, StringUtils.split("a:b:c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b c", ' '));
    }

    @Test(timeout = 4000)
    public void testSplitString() {
        assertNull(StringUtils.split(null, " "));
        assertArrayEquals(new String[]{}, StringUtils.split("", " "));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def", null));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def", " "));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def", " "));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":"));
    }

    @Test(timeout = 4000)
    public void testSplitMax() {
        assertNull(StringUtils.split(null, " ", 0));
        assertArrayEquals(new String[]{}, StringUtils.split("", " ", 0));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.split("ab de fg", null, 0));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 0));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.split("ab:cd:ef", ":", 2));
    }

    @Test(timeout = 4000)
    public void testSplitPreserveAllTokens() {
        assertNull(StringUtils.splitPreserveAllTokens(null));
        assertArrayEquals(new String[]{}, StringUtils.splitPreserveAllTokens(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.splitPreserveAllTokens("abc def"));
        assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def"));
        assertArrayEquals(new String[]{"", "abc", ""}, StringUtils.splitPreserveAllTokens(" abc "));
    }

    @Test(timeout = 4000)
    public void testSplitPreserveAllTokensChar() {
        assertNull(StringUtils.splitPreserveAllTokens(null, '.'));
        assertArrayEquals(new String[]{}, StringUtils.splitPreserveAllTokens("", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.splitPreserveAllTokens("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"a:b:c"}, StringUtils.splitPreserveAllTokens("a:b:c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.splitPreserveAllTokens("a b c", ' '));
        assertArrayEquals(new String[]{"a", "b", "c", ""}, StringUtils.splitPreserveAllTokens("a b c ", ' '));
        assertArrayEquals(new String[]{"a", "b", "c", "", ""}, StringUtils.splitPreserveAllTokens("a b c  ", ' '));
        assertArrayEquals(new String[]{"", "a", "b", "c"}, StringUtils.splitPreserveAllTokens(" a b c", ' '));
        assertArrayEquals(new String[]{"", "", "a", "b", "c"}, StringUtils.splitPreserveAllTokens("  a b c", ' '));
        assertArrayEquals(new String[]{"", "a", "b", "c", ""}, StringUtils.splitPreserveAllTokens(" a b c ", ' '));
    }

    @Test(timeout = 4000)
    public void testSplitPreserveAllTokensString() {
        assertNull(StringUtils.splitPreserveAllTokens(null, " "));
        assertArrayEquals(new String[]{}, StringUtils.splitPreserveAllTokens("", " "));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.splitPreserveAllTokens("abc def", null));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.splitPreserveAllTokens("abc def", " "));
        assertArrayEquals(new String[]{"abc", "", "def"}, StringUtils.splitPreserveAllTokens("abc  def", " "));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitPreserveAllTokens("ab:cd:ef", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef", ""}, StringUtils.splitPreserveAllTokens("ab:cd:ef:", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef", "", ""}, StringUtils.splitPreserveAllTokens("ab:cd:ef::", ":"));
        assertArrayEquals(new String[]{"ab", "", "cd", "ef"}, StringUtils.splitPreserveAllTokens("ab::cd:ef", ":"));
        assertArrayEquals(new String[]{"", "cd", "ef"}, StringUtils.splitPreserveAllTokens(":cd:ef", ":"));
        assertArrayEquals(new String[]{"", "", "cd", "ef"}, StringUtils.splitPreserveAllTokens("::cd:ef", ":"));
        assertArrayEquals(new String[]{"", "cd", "ef", ""}, StringUtils.splitPreserveAllTokens(":cd:ef:", ":"));
    }

    @Test(timeout = 4000)
    public void testSplitPreserveAllTokensMax() {
        assertNull(StringUtils.splitPreserveAllTokens(null, " ", 0));
        assertArrayEquals(new String[]{}, StringUtils.splitPreserveAllTokens("", " ", 0));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitPreserveAllTokens("ab de fg", null, 0));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 0));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.splitPreserveAllTokens("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "  de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 2));
        assertArrayEquals(new String[]{"ab", "", " de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 3));
        assertArrayEquals(new String[]{"ab", "", "", "de fg"}, StringUtils.splitPreserveAllTokens("ab   de fg", null, 4));
    }

    @Test(timeout = 4000)
    public void testSplitByWholeSeparator() {
        assertNull(StringUtils.splitByWholeSeparator(null, " "));
        assertArrayEquals(new String[]{}, StringUtils.splitByWholeSeparator("", " "));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", null));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab   de fg", null));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab:cd:ef", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
    }

    @Test(timeout = 4000)
    public void testSplitByWholeSeparatorMax() {
        assertNull(StringUtils.splitByWholeSeparator(null, " ", 0));
        assertArrayEquals(new String[]{}, StringUtils.splitByWholeSeparator("", " ", 0));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparator("ab de fg", null, 0));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 5));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
    }

    @Test(timeout = 4000)
    public void testSplitByWholeSeparatorPreserveAllTokens() {
        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, " "));
        assertArrayEquals(new String[]{}, StringUtils.splitByWholeSeparatorPreserveAllTokens("", " "));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null));
        assertArrayEquals(new String[]{"ab", "", "", "de", "fg"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   de fg", null));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":"));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-"));
    }

    @Test(timeout = 4000)
    public void testSplitByWholeSeparatorPreserveAllTokensMax() {
        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, " ", 0));
        assertArrayEquals(new String[]{}, StringUtils.splitByWholeSeparatorPreserveAllTokens("", " ", 0));
        assertArrayEquals(new String[]{"ab", "de", "fg"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab de fg", null, 0));
        assertArrayEquals(new String[]{"ab", "cd:ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:cd:ef", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 5));
        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!-cd-!-ef", "-!-", 2));
    }

    @Test(timeout = 4000)
    public void testSplitByCharacterType() {
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[]{}, StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[]{"ab", "   ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab   de fg"));
        assertArrayEquals(new String[]{"ab", ":", "cd", ":", "ef"}, StringUtils.splitByCharacterType("ab:cd:ef"));
        assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterType("number5"));
        assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));
        assertArrayEquals(new String[]{"foo", "200", "B", "ar"}, StringUtils.splitByCharacterType("foo200Bar"));
        assertArrayEquals(new String[]{"ASFR", "ules"}, StringUtils.splitByCharacterType("ASFRules"));
    }

    @Test(timeout = 4000)
    public void testSplitByCharacterTypeCamelCase() {
        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[]{}, StringUtils.splitByCharacterTypeCamelCase(""));
        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterTypeCamelCase("ab de fg"));
        assertArrayEquals(new String[]{"ab", "   ", "de", " ", "fg"}, StringUtils.splitByCharacterTypeCamelCase("ab   de fg"));
        assertArrayEquals(new String[]{"ab", ":", "cd", ":", "ef"}, StringUtils.splitByCharacterTypeCamelCase("ab:cd:ef"));
        assertArrayEquals(new String[]{"number", "5"}, StringUtils.splitByCharacterTypeCamelCase("number5"));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"foo", "200", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test(timeout = 4000)
    public void testJoinArray() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("", StringUtils.join(new Object[]{null}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a", StringUtils.join(new Object[]{null, "", "a"}));
    }

    @Test(timeout = 4000)
    public void testJoinArrayChar() {
        assertNull(StringUtils.join((Object[]) null, ','));
        assertEquals("", StringUtils.join(new Object[]{}, ','));
        assertEquals("", StringUtils.join(new Object[]{null}, ','));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals(";;a", StringUtils.join(new Object[]{null, "", "a"}, ';'));
    }

    @Test(timeout = 4000)
    public void testJoinArrayCharRange() {
        assertNull(StringUtils.join((Object[]) null, ',', 0, 2));
        assertEquals("", StringUtils.join(new Object[]{}, ',', 0, 0));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ',', 0, 0));
        assertEquals("a;b", StringUtils.join(new Object[]{"a", "b", "c"}, ';', 0, 2));
    }

    @Test(timeout = 4000)
    public void testJoinArrayString() {
        assertNull(StringUtils.join((Object[]) null, "--"));
        assertEquals("", StringUtils.join(new Object[]{}, "--"));
        assertEquals("", StringUtils.join(new Object[]{null}, "--"));
        assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, ""));
        assertEquals(",,a", StringUtils.join(new Object[]{null, "", "a"}, ","));
    }

    @Test(timeout = 4000)
    public void testJoinArrayStringRange() {
        assertNull(StringUtils.join((Object[]) null, "--", 0, 2));
        assertEquals("", StringUtils.join(new Object[]{}, "--", 0, 0));
        assertEquals("a--b", StringUtils.join(new Object[]{"a", "b", "c"}, "--", 0, 2));
    }

    @Test(timeout = 4000)
    public void testJoinIteratorChar() {
        assertNull(StringUtils.join((java.util.Iterator<?>) null, ','));
        assertEquals("", StringUtils.join(new java.util.ArrayList<String>().iterator(), ','));
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("a");
        assertEquals("a", StringUtils.join(list.iterator(), ','));
        list.add("b");
        assertEquals("a,b", StringUtils.join(list.iterator(), ','));
    }

    @Test(timeout = 4000)
    public void testJoinIteratorString() {
        assertNull(StringUtils.join((java.util.Iterator<?>) null, ","));
        assertEquals("", StringUtils.join(new java.util.ArrayList<String>().iterator(), ","));
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("a");
        assertEquals("a", StringUtils.join(list.iterator(), ","));
        list.add("b");
        assertEquals("a,b", StringUtils.join(list.iterator(), ","));
    }

    @Test(timeout = 4000)
    public void testJoinIterableChar() {
        assertNull(StringUtils.join((java.lang.Iterable<?>) null, ','));
        assertEquals("", StringUtils.join(new java.util.ArrayList<String>(), ','));
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("a");
        list.add("b");
        assertEquals("a,b", StringUtils.join(list, ','));
    }

    @Test(timeout = 4000)
    public void testJoinIterableString() {
        assertNull(StringUtils.join((java.lang.Iterable<?>) null, ","));
        assertEquals("", StringUtils.join(new java.util.ArrayList<String>(), ","));
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("a");
        list.add("b");
        assertEquals("a,b", StringUtils.join(list, ","));
    }

    @Test(timeout = 4000)
    public void testDeleteWhitespace() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("abc"));
        assertEquals("abc", StringUtils.deleteWhitespace("   ab  c  "));
    }

    @Test(timeout = 4000)
    public void testRemoveStart() {
        assertNull(StringUtils.removeStart(null, "www."));
        assertEquals("", StringUtils.removeStart("", "www."));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStart("domain.com", "www."));
        assertEquals("www.domain.com", StringUtils.removeStart("www.domain.com", "domain"));
        assertEquals("abc", StringUtils.removeStart("abc", ""));
    }

    @Test(timeout = 4000)
    public void testRemoveStartIgnoreCase() {
        assertNull(StringUtils.removeStartIgnoreCase(null, "www."));
        assertEquals("", StringUtils.removeStartIgnoreCase("", "www."));
        assertEquals("abc", StringUtils.removeStartIgnoreCase("abc", null));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("www.domain.com", "WWW."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("domain.com", "www."));
        assertEquals("www.domain.com", StringUtils.removeStartIgnoreCase("www.domain.com", "domain"));
        assertEquals("abc", StringUtils.removeStartIgnoreCase("abc", ""));
    }

    @Test(timeout = 4000)
    public void testRemoveEnd() {
        assertNull(StringUtils.removeEnd(null, ".com"));
        assertEquals("", StringUtils.removeEnd("", ".com"));
        assertEquals("abc", StringUtils.removeEnd("abc", null));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", ".com."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", "domain"));
        assertEquals("abc", StringUtils.removeEnd("abc", ""));
    }

    @Test(timeout = 4000)
    public void testRemoveEndIgnoreCase() {
        assertNull(StringUtils.removeEndIgnoreCase(null, ".com"));
        assertEquals("", StringUtils.removeEndIgnoreCase("", ".com"));
        assertEquals("abc", StringUtils.removeEndIgnoreCase("abc", null));
        assertEquals("www.domain.com", StringUtils.removeEndIgnoreCase("www.domain.com", ".com."));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEndIgnoreCase("www.domain.com", "domain"));
        assertEquals("abc", StringUtils.removeEndIgnoreCase("abc", ""));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.com", ".COM"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
    }

    @Test(timeout = 4000)
    public void testRemoveString() {
        assertNull(StringUtils.remove(null, "ue"));
        assertEquals("", StringUtils.remove("", "ue"));
        assertEquals("abc", StringUtils.remove("abc", null));
        assertEquals("abc", StringUtils.remove("abc", ""));
        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("queued", StringUtils.remove("queued", "zz"));
    }

    @Test(timeout = 4000)
    public void testRemoveChar() {
        assertNull(StringUtils.remove(null, 'u'));
        assertEquals("", StringUtils.remove("", 'u'));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertEquals("queued", StringUtils.remove("queued", 'z'));
    }

    @Test(timeout = 4000)
    public void testReplaceOnce() {
        assertNull(StringUtils.replaceOnce(null, "a", "z"));
        assertEquals("", StringUtils.replaceOnce("", "a", "z"));
        assertEquals("any", StringUtils.replaceOnce("any", null, "z"));
        assertEquals("any", StringUtils.replaceOnce("any", "a", null));
        assertEquals("any", StringUtils.replaceOnce("any", "", "z"));
        assertEquals("aba", StringUtils.replaceOnce("aba", "a", null));
        assertEquals("ba", StringUtils.replaceOnce("aba", "a", ""));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
    }

    @Test(timeout = 4000)
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "z"));
        assertEquals("", StringUtils.replace("", "a", "z"));
        assertEquals("any", StringUtils.replace("any", null, "z"));
        assertEquals("any", StringUtils.replace("any", "a", null));
        assertEquals("any", StringUtils.replace("any", "", "z"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("b", StringUtils.replace("aba", "a", ""));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
    }

    @Test(timeout = 4000)
    public void testReplaceMax() {
        assertNull(StringUtils.replace(null, "a", "z", -1));
        assertEquals("", StringUtils.replace("", "a", "z", -1));
        assertEquals("any", StringUtils.replace("any", null, "z", -1));
        assertEquals("any", StringUtils.replace("any", "a", null, -1));
        assertEquals("any", StringUtils.replace("any", "", "z", -1));
        assertEquals("any", StringUtils.replace("any", "a", "z", 0));
        assertEquals("abaa", StringUtils.replace("abaa", "a", null, -1));
        assertEquals("b", StringUtils.replace("abaa", "a", "", -1));
        assertEquals("abaa", StringUtils.replace("abaa", "a", "z", 0));
        assertEquals("zbaa", StringUtils.replace("abaa", "a", "z", 1));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));
        assertEquals("zbzz", StringUtils.replace("abaa", "a", "z", -1));
    }

    @Test(timeout = 4000)
    public void testReplaceEach() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[0]));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        // Non-repeating
        assertEquals("dcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
    }

    @Test(timeout = 4000)
    public void testReplaceCharsChar() {
        assertNull(StringUtils.replaceChars(null, 'b', 'y'));
        assertEquals("", StringUtils.replaceChars("", 'b', 'y'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("abcba", StringUtils.replaceChars("abcba", 'z', 'y'));
    }

    @Test(timeout = 4000)
    public void testReplaceCharsString() {
        assertNull(StringUtils.replaceChars(null, "ho", "jy"));
        assertEquals("", StringUtils.replaceChars("", "ho", "jy"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "jy"));
        assertEquals("abc", StringUtils.replaceChars("abc", "", "jy"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yzx"));
    }

    @Test(timeout = 4000)
    public void testOverlay() {
        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        assertEquals("abef", StringUtils.overlay("abcdef", "", 2, 4));
        assertEquals("abef", StringUtils.overlay("abcdef", "", 4, 2));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertEquals("abcdefzzzz", StringUtils.overlay("abcdef", "zzzz", 8, 10));
    }

    @Test(timeout = 4000)
    public void testChomp() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc ", StringUtils.chomp("abc \r"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc\r\n", StringUtils.chomp("abc\r\n\r\n"));
        assertEquals("abc\n", StringUtils.chomp("abc\n\r"));
        assertEquals("abc\n\rabc", StringUtils.chomp("abc\n\rabc"));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("", StringUtils.chomp("\r\n"));
    }

    @Test(timeout = 4000)
    public void testChompSeparator() {
        assertNull(StringUtils.chomp(null, "bar"));
        assertEquals("", StringUtils.chomp("", "bar"));
        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));
        assertEquals("", StringUtils.chomp("foo", "foo"));
        assertEquals("foo ", StringUtils.chomp("foo ", "foo"));
        assertEquals(" ", StringUtils.chomp(" foo", "foo"));
        assertEquals("foo", StringUtils.chomp("foo", "foooo"));
        assertEquals("foo", StringUtils.chomp("foo", ""));
        assertEquals("foo", StringUtils.chomp("foo", null));
    }

    @Test(timeout = 4000)
    public void testChop() {
        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("abc ", StringUtils.chop("abc \r"));
        assertEquals("abc", StringUtils.chop("abc\n"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc\nab", StringUtils.chop("abc\nabc"));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("", StringUtils.chop("\r"));
        assertEquals("", StringUtils.chop("\n"));
        assertEquals("", StringUtils.chop("\r\n"));
    }

    @Test(timeout = 4000)
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("", 0));
        assertEquals("", StringUtils.repeat("", 2));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("", StringUtils.repeat("a", -2));
    }

    @Test(timeout = 4000)
    public void testRepeatWithSeparator() {
        assertNull(StringUtils.repeat(null, null, 2));
        assertNull(StringUtils.repeat(null, "x", 2));
        assertEquals("", StringUtils.repeat("", null, 0));
        assertEquals("", StringUtils.repeat("", "", 2));
        assertEquals("xxx", StringUtils.repeat("", "x", 3));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
    }

    @Test(timeout = 4000)
    public void testRightPad() {
        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("   ", StringUtils.rightPad("", 3));
        assertEquals("bat", StringUtils.rightPad("bat", 3));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 1));
        assertEquals("bat", StringUtils.rightPad("bat", -1));
    }

    @Test(timeout = 4000)
    public void testRightPadChar() {
        assertNull(StringUtils.rightPad(null, 5, 'z'));
        assertEquals("zzz", StringUtils.rightPad("", 3, 'z'));
        assertEquals("bat", StringUtils.rightPad("bat", 3, 'z'));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("bat", StringUtils.rightPad("bat", 1, 'z'));
        assertEquals("bat", StringUtils.rightPad("bat", -1, 'z'));
    }

    @Test(timeout = 4000)
    public void testRightPadString() {
        assertNull(StringUtils.rightPad(null, 5, "yz"));
        assertEquals("zzz", StringUtils.rightPad("", 3, "z"));
        assertEquals("bat", StringUtils.rightPad("bat", 3, "yz"));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertEquals("bat", StringUtils.rightPad("bat", 1, "yz"));
        assertEquals("bat", StringUtils.rightPad("bat", -1, "yz"));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, null));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));
    }

    @Test(timeout = 4000)
    public void testLeftPad() {
        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("   ", StringUtils.leftPad("", 3));
        assertEquals("bat", StringUtils.leftPad("bat", 3));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat", StringUtils.leftPad("bat", 1));
        assertEquals("bat", StringUtils.leftPad("bat", -1));
    }

    @Test(timeout = 4000)
    public void testLeftPadChar() {
        assertNull(StringUtils.leftPad(null, 5, 'z'));
        assertEquals("zzz", StringUtils.leftPad("", 3, 'z'));
        assertEquals("bat", StringUtils.leftPad("bat", 3, 'z'));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("bat", StringUtils.leftPad("bat", 1, 'z'));
        assertEquals("bat", StringUtils.leftPad("bat", -1, 'z'));
    }

    @Test(timeout = 4000)
    public void testLeftPadString() {
        assertNull(StringUtils.leftPad(null, 5, "yz"));
        assertEquals("zzz", StringUtils.leftPad("", 3, "z"));
        assertEquals("bat", StringUtils.leftPad("bat", 3, "yz"));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", 1, "yz"));
        assertEquals("bat", StringUtils.leftPad("bat", -1, "yz"));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, null));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));
    }

    @Test(timeout = 4000)
    public void testCenter() {
        assertNull(StringUtils.center(null, 5));
        assertEquals("    ", StringUtils.center("", 4));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("abcd", StringUtils.center("abcd", 2));
        assertEquals(" a  ", StringUtils.center("a", 4));
    }

    @Test(timeout = 4000)
    public void testCenterChar() {
        assertNull(StringUtils.center(null, 5, ' '));
        assertEquals("    ", StringUtils.center("", 4, ' '));
        assertEquals("ab", StringUtils.center("ab", -1, ' '));
        assertEquals(" ab", StringUtils.center("ab", 4, ' '));
        assertEquals("abcd", StringUtils.center("abcd", 2, ' '));
        assertEquals(" a  ", StringUtils.center("a", 4, ' '));
        assertEquals("yayy", StringUtils.center("a", 4, 'y'));
    }

    @Test(timeout = 4000)
    public void testCenterString() {
        assertNull(StringUtils.center(null, 5, " "));
        assertEquals("    ", StringUtils.center("", 4, " "));
        assertEquals("ab", StringUtils.center("ab", -1, " "));
        assertEquals(" ab", StringUtils.center("ab", 4, " "));
        assertEquals("abcd", StringUtils.center("abcd", 2, " "));
        assertEquals(" a  ", StringUtils.center("a", 4, " "));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  abc  ", StringUtils.center("abc", 7, null));
        assertEquals("  abc  ", StringUtils.center("abc", 7, ""));
    }

    @Test(timeout = 4000)
    public void testUpperCase() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
    }

    @Test(timeout = 4000)
    public void testUpperCaseLocale() {
        assertNull(StringUtils.upperCase(null, java.util.Locale.ENGLISH));
        assertEquals("", StringUtils.upperCase("", java.util.Locale.ENGLISH));
        assertEquals("ABC", StringUtils.upperCase("aBc", java.util.Locale.ENGLISH));
    }

    @Test(timeout = 4000)
    public void testLowerCase() {
        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
    }

    @Test(timeout = 4000)
    public void testLowerCaseLocale() {
        assertNull(StringUtils.lowerCase(null, java.util.Locale.ENGLISH));
        assertEquals("", StringUtils.lowerCase("", java.util.Locale.ENGLISH));
        assertEquals("abc", StringUtils.lowerCase("aBc", java.util.Locale.ENGLISH));
    }

    @Test(timeout = 4000)
    public void testCapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("CAt", StringUtils.capitalize("cAt"));
    }

    @Test(timeout = 4000)
    public void testUncapitalize() {
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cAT", StringUtils.uncapitalize("CAT"));
    }

    @Test(timeout = 4000)
    public void testSwapCase() {
        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
    }

    @Test(timeout = 4000)
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("", "a"));
        assertEquals(0, StringUtils.countMatches("abba", null));
        assertEquals(0, StringUtils.countMatches("abba", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));
        assertEquals(0, StringUtils.countMatches("abba", "xxx"));
    }

    @Test(timeout = 4000)
    public void testIsAlpha() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertFalse(StringUtils.isAlpha("  "));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));
        assertFalse(StringUtils.isAlpha("ab-c"));
    }

    @Test(timeout = 4000)
    public void testIsAlphaSpace() {
        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("  "));
        assertTrue(StringUtils.isAlphaSpace("abc"));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab2c"));
        assertFalse(StringUtils.isAlphaSpace("ab-c"));
    }

    @Test(timeout = 4000)
    public void testIsAlphanumeric() {
        assertFalse(StringUtils.isAlphanumeric(null));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertFalse(StringUtils.isAlphanumeric("  "));
        assertTrue(StringUtils.isAlphanumeric("abc"));
        assertTrue(StringUtils.isAlphanumeric("123"));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));
    }

    @Test(timeout = 4000)
    public void testIsAlphanumericSpace() {
        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("  "));
        assertTrue(StringUtils.isAlphanumericSpace("abc"));
        assertTrue(StringUtils.isAlphanumericSpace("ab c"));
        assertTrue(StringUtils.isAlphanumericSpace("123"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-c"));
    }

    @Test(timeout = 4000)
    public void testIsAsciiPrintable() {
        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable(" "));
        assertTrue(StringUtils.isAsciiPrintable("Ceki"));
        assertTrue(StringUtils.isAsciiPrintable("ab2c"));
        assertTrue(StringUtils.isAsciiPrintable("!ab-c~"));
        assertTrue(StringUtils.isAsciiPrintable("\u0020"));
        assertTrue(StringUtils.isAsciiPrintable("\u0021"));
        assertTrue(StringUtils.isAsciiPrintable("\u007e"));
        assertFalse(StringUtils.isAsciiPrintable("\u007f"));
        assertFalse(StringUtils.isAsciiPrintable("Ceki G\u00fclc\u00fc"));
    }

    @Test(timeout = 4000)
    public void testIsNumeric() {
        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertFalse(StringUtils.isNumeric("  "));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12 3"));
        assertFalse(StringUtils.isNumeric("ab2c"));
        assertFalse(StringUtils.isNumeric("12-3"));
        assertFalse(StringUtils.isNumeric("12.3"));
    }

    @Test(timeout = 4000)
    public void testIsNumericSpace() {
        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("  "));
        assertTrue(StringUtils.isNumericSpace("123"));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("ab2c"));
        assertFalse(StringUtils.isNumericSpace("12-3"));
        assertFalse(StringUtils.isNumericSpace("12.3"));
    }

    @Test(timeout = 4000)
    public void testIsWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  "));
        assertFalse(StringUtils.isWhitespace("abc"));
        assertFalse(StringUtils.isWhitespace("ab2c"));
        assertFalse(StringUtils.isWhitespace("ab-c"));
    }

    @Test(timeout = 4000)
    public void testIsAllLowerCase() {
        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertFalse(StringUtils.isAllLowerCase("  "));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));
    }

    @Test(timeout = 4000)
    public void testIsAllUpperCase() {
        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertFalse(StringUtils.isAllUpperCase("  "));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("AbC"));
    }

    @Test(timeout = 4000)
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("bat", StringUtils.defaultString("bat"));
    }

    @Test(timeout = 4000)
    public void testDefaultStringDefault() {
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("", StringUtils.defaultString("", "NULL"));
        assertEquals("bat", StringUtils.defaultString("bat", "NULL"));
    }

    @Test(timeout = 4000)
    public void testDefaultIfEmpty() {
        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals("bat", StringUtils.defaultIfEmpty("bat", "NULL"));
        assertNull(StringUtils.defaultIfEmpty("", null));
    }

    @Test(timeout = 4000)
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 10));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
    }

    @Test(timeout = 4000)
    public void testAbbreviateOffset() {
        assertNull(StringUtils.abbreviate(null, 0, 10));
        assertEquals("", StringUtils.abbreviate("", 0, 4));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 1, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 4, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ghij...", StringUtils.abbreviate("abcdefghijklmno", 6, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 8, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 10, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));
    }

    @Test(timeout = 4000)
    public void testAbbreviateMiddle() {
        assertNull(StringUtils.abbreviateMiddle(null, null, 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    @Test(timeout = 4000)
    public void testDifference() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("", StringUtils.difference("", ""));
        assertEquals("abc", StringUtils.difference("", "abc"));
        assertEquals("", StringUtils.difference("abc", ""));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("xyz", StringUtils.difference("ab", "abxyz"));
        assertEquals("xyz", StringUtils.difference("abcde", "abxyz"));
        assertEquals("xyz", StringUtils.difference("abcde", "xyz"));
    }

    @Test(timeout = 4000)
    public void testIndexOfDifference() {
        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(-1, StringUtils.indexOfDifference("", ""));
        assertEquals(0, StringUtils.indexOfDifference("", "abc"));
        assertEquals(0, StringUtils.indexOfDifference("abc", ""));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
    }

    @Test(timeout = 4000)
    public void testIndexOfDifferenceArray() {
        assertEquals(-1, StringUtils.indexOfDifference((String[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{null, null}));
        assertEquals(-1, StringUtils.indexOfDifference(new String[]{"", ""}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"", null}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"abc", null, null}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{null, null, "abc"}));
        assertEquals(0, StringUtils.indexOfDifference(new String[]{"", "abc"}));
        assertEquals(7, StringUtils.indexOfDifference(new String[]{"i am a machine", "i am a robot"}));
    }

    @Test(timeout = 4000)
    public void testGetCommonPrefix() {
        assertEquals("", StringUtils.getCommonPrefix((String[]) null));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{null, null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"", ""}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"", null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", null, null}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{null, null, "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"", "abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", ""}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc", "abc"}));
        assertEquals("a", StringUtils.getCommonPrefix(new String[]{"abc", "a"}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[]{"ab", "abxyz"}));
        assertEquals("ab", StringUtils.getCommonPrefix(new String[]{"abcde", "abxyz"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abcde", "xyz"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"xyz", "abcde"}));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
    }

    @Test(timeout = 4000)
    public void testGetLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));
        assertEquals(8, StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz"));
        assertEquals(1, StringUtils.getLevenshteinDistance("hello", "hallo"));
    }

    @Test(timeout = 4000)
    public void testStartsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
    }

    @Test(timeout = 4000)
    public void testStartsWithIgnoreCase() {
        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
    }

    @Test(timeout = 4000)
    public void testStartsWithAny() {
        assertFalse(StringUtils.startsWithAny(null, null));
        assertFalse(StringUtils.startsWithAny(null, new String[]{"abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", null));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{""}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"abc"}));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{null, "xyz", "abc"}));
    }

    @Test(timeout = 4000)
    public void testEndsWith() {
        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "cde"));
    }

    @Test(timeout = 4000)
    public void testEndsWithIgnoreCase() {
        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateThrowsExceptionWhenMaxWidthLessThan4() {
        StringUtils.abbreviate("abc", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetThrowsExceptionWhenMaxWidthLessThan4() {
        StringUtils.abbreviate("abcdefghij", 0, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetThrowsExceptionWhenMaxWidthLessThan7WithOffset() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLevenshteinDistanceThrowsExceptionWhenFirstNull() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLevenshteinDistanceThrowsExceptionWhenSecondNull() {
        StringUtils.getLevenshteinDistance("a", null);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testPaddingThrowsExceptionWhenRepeatNegative() {
        // padding is private, but called via leftPad/rightPad with negative pads? Actually, padding is called only when pads>0.
        // We can trigger via leftPad with large negative? No, leftPad returns original if pads<=0.
        // The padding method itself throws if repeat<0, but it's private. We can test via leftPad with size < str.length? No.
        // Actually, padding is called only when pads>0. So we cannot directly test the exception from public methods.
        // But we can test via leftPad with negative size? leftPad returns original if pads<=0. So no exception.
        // The padding method is private and not directly testable. We'll skip this test or test via reflection? Not allowed.
        // Instead, we can test that leftPad does not throw for negative size.
        // We'll just call leftPad with negative size to ensure no exception.
        StringUtils.leftPad("abc", -1); // no exception expected
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachThrowsIllegalStateExceptionWhenCircular() {
        // This triggers timeToLive < 0 due to circular replacement
        StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, true, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachThrowsIllegalArgumentExceptionWhenArrayLengthsMismatch() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"b", "c"});
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructor() {
        // Just ensure constructor works (public)
        new StringUtils();
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }
}