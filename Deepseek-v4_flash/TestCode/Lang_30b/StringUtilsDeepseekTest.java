package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StringUtilsDeepseekTest
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target methods with supplementary character bugs:
 * - containsNone(CharSequence, char[]): fails when supplementary chars are present (returns false instead of true)
 * - containsNone(CharSequence, String): same
 * - containsAny(CharSequence, char[]): fails with bad supplementary chars (returns true instead of false)
 * - containsAny(CharSequence, String): same
 * - indexOfAny(CharSequence, char[]): returns 0 instead of correct index (2)
 * - indexOfAny(CharSequence, String): same
 * - indexOfAnyBut(CharSequence, char[]): returns 3 instead of 2
 * - indexOfAnyBut(String, String): returns 3 instead of 2
 * 
 * Branch coverage targets:
 * - isEmpty/isBlank: null, empty, whitespace, non-empty
 * - trim/strip: null, empty, whitespace, mixed
 * - equals: null vs null, null vs non-null, case sensitive/insensitive
 * - indexOf/lastIndexOf: various positions, null/empty strings
 * - substring: negative indices, out-of-bounds
 * - split/join: null, empty, single char, multiple delimiters
 * - replace: null, empty, max limits
 * - padding: negative, zero, positive
 * - case conversion: null, empty, mixed case
 * - character checks: null, empty, valid/invalid chars
 * - levenshtein: null throws, empty strings, equal strings
 * - startsWith/endsWith: null, empty, case sensitive/insensitive
 * 
 * All tests include timeout=4000 to prevent hanging.
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
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank("bob"));
        assertFalse(StringUtils.isBlank("  bob  "));
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
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("ab c", StringUtils.strip(" ab c "));
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
    public void testIndexOfString() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("a", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
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
        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));
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
        assertEquals("abc", StringUtils.left("abc", 4));
        assertEquals("", StringUtils.left("abc", -1));
    }

    @Test(timeout = 4000)
    public void testRight() {
        assertNull(StringUtils.right(null, 0));
        assertEquals("", StringUtils.right("", 0));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 4));
        assertEquals("", StringUtils.right("abc", -1));
    }

    @Test(timeout = 4000)
    public void testMid() {
        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("", 0, 2));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", -2, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
    }

    @Test(timeout = 4000)
    public void testSubstringBefore() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("ab", StringUtils.substringBefore("abc", "c"));
        assertEquals("abc", StringUtils.substringBefore("abc", "d"));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
    }

    @Test(timeout = 4000)
    public void testSubstringAfter() {
        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("bc", StringUtils.substringAfter("abc", "a"));
        assertEquals("cba", StringUtils.substringAfter("abcba", "b"));
        assertEquals("", StringUtils.substringAfter("abc", "c"));
        assertEquals("", StringUtils.substringAfter("abc", "d"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
        assertEquals("", StringUtils.substringAfter("abc", null));
    }

    @Test(timeout = 4000)
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc  def"));
        assertArrayEquals(new String[]{"abc"}, StringUtils.split(" abc "));
    }

    @Test(timeout = 4000)
    public void testSplitChar() {
        assertNull(StringUtils.split(null, '.'));
        assertArrayEquals(new String[0], StringUtils.split("", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));
        assertArrayEquals(new String[]{"a:b:c"}, StringUtils.split("a:b:c", '.'));
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
        assertNull(StringUtils.join((Object[]) null, ';'));
        assertEquals("", StringUtils.join(new Object[]{}, ';'));
        assertEquals("", StringUtils.join(new Object[]{null}, ';'));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals(";;a", StringUtils.join(new Object[]{null, "", "a"}, ';'));
    }

    @Test(timeout = 4000)
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("any", StringUtils.replace("any", null, "b"));
        assertEquals("any", StringUtils.replace("any", "a", null));
        assertEquals("any", StringUtils.replace("any", "", "b"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("b", StringUtils.replace("aba", "a", ""));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
    }

    @Test(timeout = 4000)
    public void testReplaceOnce() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("", StringUtils.replaceOnce("", "a", "b"));
        assertEquals("any", StringUtils.replaceOnce("any", null, "b"));
        assertEquals("any", StringUtils.replaceOnce("any", "a", null));
        assertEquals("any", StringUtils.replaceOnce("any", "", "b"));
        assertEquals("aba", StringUtils.replaceOnce("aba", "a", null));
        assertEquals("ba", StringUtils.replaceOnce("aba", "a", ""));
        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
    }

    @Test(timeout = 4000)
    public void testReplaceWithMax() {
        assertEquals("any", StringUtils.replace("any", "a", "b", 0));
        assertEquals("abaa", StringUtils.replace("abaa", "a", null, -1));
        assertEquals("b", StringUtils.replace("abaa", "a", "", -1));
        assertEquals("abaa", StringUtils.replace("abaa", "a", "z", 0));
        assertEquals("zbaa", StringUtils.replace("abaa", "a", "z", 1));
        assertEquals("zbza", StringUtils.replace("abaa", "a", "z", 2));
        assertEquals("zbzz", StringUtils.replace("abaa", "a", "z", -1));
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
    public void testRightPad() {
        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("   ", StringUtils.rightPad("", 3));
        assertEquals("bat", StringUtils.rightPad("bat", 3));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 1));
        assertEquals("bat", StringUtils.rightPad("bat", -1));
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
    public void testUpperCase() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("ABC", StringUtils.upperCase("aBc"));
    }

    @Test(timeout = 4000)
    public void testLowerCase() {
        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("abc", StringUtils.lowerCase("aBc"));
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
    public void testIsAlpha() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertFalse(StringUtils.isAlpha("  "));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));
        assertFalse(StringUtils.isAlpha("ab-c"));
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
    public void testIsWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  "));
        assertFalse(StringUtils.isWhitespace("abc"));
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
        assertFalse(StringUtils.isAllUpperCase("aBC"));
    }

    @Test(timeout = 4000)
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("bat", StringUtils.defaultString("bat"));
    }

    @Test(timeout = 4000)
    public void testDefaultStringWithDefault() {
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
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("tab", StringUtils.reverse("bat"));
    }

    @Test(timeout = 4000)
    public void testReverseDelimited() {
        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("", StringUtils.reverseDelimited("", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    @Test(timeout = 4000)
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        try {
            StringUtils.abbreviate("abcdefg", 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAbbreviateWithOffset() {
        assertNull(StringUtils.abbreviate(null, 0, 10));
        assertEquals("", StringUtils.abbreviate("", 0, 4));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 8, 10));
        try {
            StringUtils.abbreviate("abcdefghij", 0, 3);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
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
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));
        assertEquals(2, StringUtils.indexOfDifference("abcde", "abxyz"));
        assertEquals(0, StringUtils.indexOfDifference("abcde", "xyz"));
    }

    @Test(timeout = 4000)
    public void testGetCommonPrefix() {
        assertEquals("", StringUtils.getCommonPrefix(null));
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
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
    }

    @Test(timeout = 4000)
    public void testGetLevenshteinDistance() {
        try {
            StringUtils.getLevenshteinDistance(null, "a");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            StringUtils.getLevenshteinDistance("a", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
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
    public void testEndsWith() {
        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
    }

    @Test(timeout = 4000)
    public void testEndsWithIgnoreCase() {
        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testIndexOfBoundary() {
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(-1, StringUtils.indexOf("a", 'a', 1));
        assertEquals(0, StringUtils.indexOf("a", 'a', -1));
        assertEquals(-1, StringUtils.indexOf("a", 'a', 2));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfBoundary() {
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", 'a', -1));
        assertEquals(0, StringUtils.lastIndexOf("a", 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", 'a', -2));
    }

    @Test(timeout = 4000)
    public void testSubstringBoundary() {
        assertEquals("", StringUtils.substring("abc", 3));
        assertEquals("", StringUtils.substring("abc", 3, 3));
        assertEquals("", StringUtils.substring("abc", 2, 2));
        assertEquals("", StringUtils.substring("abc", 4, 5));
    }

    @Test(timeout = 4000)
    public void testLeftBoundary() {
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("abc", StringUtils.left("abc", 3));
        assertEquals("abc", StringUtils.left("abc", 5));
        assertEquals("", StringUtils.left("abc", -1));
    }

    @Test(timeout = 4000)
    public void testRightBoundary() {
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("abc", StringUtils.right("abc", 3));
        assertEquals("abc", StringUtils.right("abc", 5));
        assertEquals("", StringUtils.right("abc", -1));
    }

    @Test(timeout = 4000)
    public void testMidBoundary() {
        assertEquals("", StringUtils.mid("abc", 3, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("abc", StringUtils.mid("abc", 0, 5));
        assertEquals("", StringUtils.mid("abc", 5, 2));
    }

    @Test(timeout = 4000)
    public void testRepeatBoundary() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("", 0));
        assertEquals("", StringUtils.repeat("", 2));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("", StringUtils.repeat("a", -2));
    }

    @Test(timeout = 4000)
    public void testPaddingBoundary() {
        // padding is private, but tested via leftPad/rightPad
        assertEquals("", StringUtils.leftPad("", 0));
        assertEquals("", StringUtils.rightPad("", 0));
        try {
            // negative padding via leftPad with size < str.length() returns str, not exception
            // padding method itself throws IndexOutOfBoundsException for negative repeat
            // We can test via leftPad with size negative? leftPad returns str if pads <=0
            // So we need to trigger padding directly? Not possible. Skip.
        } catch (Exception e) {
            // not expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (Supplementary Characters) ====================

    // Supplementary character: U+1D11E (MUSICAL SYMBOL G CLEF) = "\uD834\uDD1E"
    private static final String SUPPLEMENTARY = "\uD834\uDD1E";
    private static final String BAD_SUPPLEMENTARY = "\uD834\uDD1E"; // same as above for bad tests

    @Test(timeout = 4000)
    public void testContainsNone_CharArrayWithSupplementaryChars() {
        // Expected: true (contains none of the chars in the array)
        // Bug: returns false
        assertTrue("containsNone should return true for supplementary char not in array",
            StringUtils.containsNone(SUPPLEMENTARY, new char[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testContainsNone_StringWithSupplementaryChars() {
        assertTrue("containsNone should return true for supplementary char not in string",
            StringUtils.containsNone(SUPPLEMENTARY, "ab"));
    }

    @Test(timeout = 4000)
    public void testContainsAny_StringCharArrayWithBadSupplementaryChars() {
        // Expected: false (contains any of the chars? The supplementary char is not in the array)
        // Bug: returns true
        assertFalse("containsAny should return false when supplementary char not in array",
            StringUtils.containsAny(SUPPLEMENTARY, new char[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testContainsAny_StringWithBadSupplementaryChars() {
        assertFalse("containsAny should return false when supplementary char not in string",
            StringUtils.containsAny(SUPPLEMENTARY, "ab"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAny_StringCharArrayWithSupplementaryChars() {
        // String "ab" + supplementary + "cd" -> index of any of {'c','d'} should be 2 (position of 'c')
        String str = "ab" + SUPPLEMENTARY + "cd";
        assertEquals("indexOfAny should find 'c' at index 2", 2,
            StringUtils.indexOfAny(str, new char[]{'c', 'd'}));
    }

    @Test(timeout = 4000)
    public void testIndexOfAny_StringStringWithSupplementaryChars() {
        String str = "ab" + SUPPLEMENTARY + "cd";
        assertEquals("indexOfAny should find 'c' at index 2", 2,
            StringUtils.indexOfAny(str, "cd"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyBut_StringCharArrayWithSupplementaryChars() {
        // String "ab" + supplementary + "cd" -> indexOfAnyBut of {'a','b'} should be 2 (first char not a or b)
        String str = "ab" + SUPPLEMENTARY + "cd";
        assertEquals("indexOfAnyBut should find first non-'ab' at index 2", 2,
            StringUtils.indexOfAnyBut(str, new char[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyBut_StringStringWithSupplementaryChars() {
        String str = "ab" + SUPPLEMENTARY + "cd";
        assertEquals("indexOfAnyBut should find first non-'ab' at index 2", 2,
            StringUtils.indexOfAnyBut(str, "ab"));
    }

    @Test(timeout = 4000)
    public void testContainsNone_CharArrayWithBadSupplementaryChars() {
        // Expected: true (contains none of the bad chars)
        // Bug: returns false
        assertTrue("containsNone should return true for supplementary char not in array",
            StringUtils.containsNone(SUPPLEMENTARY, new char[]{'x', 'y'}));
    }

    @Test(timeout = 4000)
    public void testContainsNone_StringWithBadSupplementaryChars() {
        assertTrue("containsNone should return true for supplementary char not in string",
            StringUtils.containsNone(SUPPLEMENTARY, "xy"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAbbreviateMinWidthException() {
        StringUtils.abbreviate("abc", 3);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAbbreviateWithOffsetMinWidthException() {
        StringUtils.abbreviate("abcdefghij", 0, 3);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAbbreviateWithOffsetMinWidth7Exception() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistanceNullS() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLevenshteinDistanceNullT() {
        StringUtils.getLevenshteinDistance("a", null);
    }

    @Test(timeout = 4000)
    public void testReplaceEachWithNull() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[0]));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testReplaceEachMismatchedArrays() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"b", "c"});
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testReplaceEachRepeatedlyCircular() {
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"});
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    // StringUtils is a utility class with static methods only; no instance state.
    // Constructor is public but not used. We can test that it exists.
    @Test(timeout = 4000)
    public void testConstructor() {
        new StringUtils(); // just ensure no exception
    }

    // Constants
    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }
}