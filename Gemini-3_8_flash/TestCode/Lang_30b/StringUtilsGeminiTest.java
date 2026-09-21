package org.apache.commons.lang3;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects & Ground Truth Vulnerabilities:
 * 1. Supplementary Character Mishandling in Search/Contains Algorithms:
 *    - StringUtils.containsNone(CharSequence, char[]) & (CharSequence, String):
 *      Fails when comparing Unicode supplementary characters (surrogate pairs) sharing the same high surrogate.
 *      High surrogate equality prematurely returns false, ignoring low surrogate inequality.
 *    - StringUtils.containsAny(CharSequence, char[]) & (CharSequence, String):
 *      Unpaired/dangling high surrogates at boundary (e.g. index == csLast or searchLast) bypass surrogate
 *      checks and incorrectly match basic multilingual plane branches, returning true instead of false.
 *    - StringUtils.indexOfAny(CharSequence, char[]) & (CharSequence, String):
 *      Matches high surrogate alone and reports index 0 instead of searching full supplementary codepoint,
 *      resulting in expected index 2 but returning 0.
 *    - StringUtils.indexOfAnyBut(CharSequence, char[]) & (CharSequence, String):
 *      Fails to treat surrogate pairs as atomic code points; increments/skips incorrectly, reporting
 *      index 3 instead of 2.
 *
 * Core Decision Logic & Edge Branches Targeted:
 * - Empty & Blank checks: null, zero-length, whitespace-only, supplementary whitespace.
 * - Strip / Trim / StripAccents: null/empty bounds, whitespace vs custom strip chars, reflection fallback for stripAccents.
 * - Substring / Left / Right / Mid / SubstringBetween: negative indices, start > end, out-of-range bounds, nested tokens.
 * - Split / Join: regex-free split variants, null delimiters, preserveTokens true/false, empty elements, max limits, camelCase.
 * - Replace / ReplaceEach / ReplaceChars: cyclic dependency checks (timeToLive), buffer size estimations, mismatched arrays.
 * - Pad / Center / Repeat: PAD_LIMIT (>8192) boundary checks, negative repeat counts, multi-character pads.
 * - Levenshtein / CommonPrefix / Difference: identical arrays, null elements within arrays, single-dimensional matrix swap logic.
 */
public class StringUtilsGeminiTest {

    private static final String CHAR_U20000 = "\uD840\uDC00"; // Supplementary char 1
    private static final String CHAR_U20001 = "\uD840\uDC01"; // Supplementary char 2 (same high surrogate \uD840)

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Supplementary Chars & Defects4J)
    // =========================================================================

    @Test(timeout = 4000)
    public void testContainsNone_CharArrayWithSupplementaryChars_RevealsDefect() {
        // CHAR_U20000 and CHAR_U20001 share the high surrogate \uD840 but differ in low surrogate.
        // containsNone must return true because CHAR_U20000 does NOT contain CHAR_U20001.
        assertTrue(StringUtils.containsNone(CHAR_U20000, CHAR_U20001.toCharArray()));
        assertFalse(StringUtils.containsNone(CHAR_U20000, CHAR_U20000.toCharArray()));
    }

    @Test(timeout = 4000)
    public void testContainsNone_StringWithSupplementaryChars_RevealsDefect() {
        assertTrue(StringUtils.containsNone(CHAR_U20000, CHAR_U20001));
        assertFalse(StringUtils.containsNone(CHAR_U20000, CHAR_U20000));
    }

    @Test(timeout = 4000)
    public void testContainsNone_CharArrayWithBadSupplementaryChars_RevealsDefect() {
        // High surrogate alone should not match full supplementary character
        char[] badSurrogate = new char[] { CHAR_U20000.charAt(0) };
        assertTrue(StringUtils.containsNone(CHAR_U20000, badSurrogate));
    }

    @Test(timeout = 4000)
    public void testContainsNone_StringWithBadSupplementaryChars_RevealsDefect() {
        assertTrue(StringUtils.containsNone(CHAR_U20000, CHAR_U20000.substring(0, 1)));
    }

    @Test(timeout = 4000)
    public void testContainsAny_StringCharArrayWithBadSupplementaryChars_RevealsDefect() {
        // Lone high surrogate at the end of search array must not match complete supplementary character
        char[] badSurrogate = new char[] { CHAR_U20000.charAt(0) };
        assertFalse(StringUtils.containsAny(CHAR_U20000, badSurrogate));
    }

    @Test(timeout = 4000)
    public void testContainsAny_StringWithBadSupplementaryChars_RevealsDefect() {
        assertFalse(StringUtils.containsAny(CHAR_U20000, CHAR_U20000.substring(0, 1)));
    }

    @Test(timeout = 4000)
    public void testIndexOfAny_StringCharArrayWithSupplementaryChars_RevealsDefect() {
        // String has CHAR_U20000 at [0,1] and CHAR_U20001 at [2,3].
        // Searching for CHAR_U20001 should return 2, not 0.
        String source = CHAR_U20000 + CHAR_U20001;
        assertEquals(2, StringUtils.indexOfAny(source, CHAR_U20001.toCharArray()));
    }

    @Test(timeout = 4000)
    public void testIndexOfAny_StringStringWithSupplementaryChars_RevealsDefect() {
        String source = CHAR_U20000 + CHAR_U20001;
        assertEquals(2, StringUtils.indexOfAny(source, CHAR_U20001));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyBut_StringCharArrayWithSupplementaryChars_RevealsDefect() {
        // Searching for character not in CHAR_U20000: index 2 should be returned (start of CHAR_U20001)
        String source = CHAR_U20000 + CHAR_U20001;
        assertEquals(2, StringUtils.indexOfAnyBut(source, CHAR_U20000.toCharArray()));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyBut_StringStringWithSupplementaryChars_RevealsDefect() {
        String source = CHAR_U20000 + CHAR_U20001;
        assertEquals(2, StringUtils.indexOfAnyBut(source, CHAR_U20000));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsEmptyAndIsBlank() {
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
        assertTrue(StringUtils.isBlank(" \t \r \n "));
        assertFalse(StringUtils.isBlank("bob"));
        assertFalse(StringUtils.isBlank(" bob "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("bob"));
    }

    @Test(timeout = 4000)
    public void testTrimAndStrip() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));
        assertEquals("", StringUtils.trim("     "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("     "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("     "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc "));
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty("   "));

        assertEquals("abc", StringUtils.strip("yyyabcyyy", "y"));
        assertEquals("abc", StringUtils.stripStart("yyyabc", "y"));
        assertEquals("abc", StringUtils.stripEnd("abcyyy", "y"));
        assertEquals("abc", StringUtils.stripStart("   abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc   ", null));
        assertEquals("abc", StringUtils.stripStart("abc", ""));
        assertEquals("abc", StringUtils.stripEnd("abc", ""));

        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{" a ", "  b"}));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.stripAll(new String[]{"xax", "yb"}, "xy"));
        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));

        assertEquals("eclair", StringUtils.stripAccents("éclair"));
        assertNull(StringUtils.stripAccents(null));
        assertEquals("", StringUtils.stripAccents(""));
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
        assertFalse(StringUtils.equalsIgnoreCase("abc", "abcd"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndLastIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(1, StringUtils.indexOf("aabaa", 'b'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 2));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 9));
        assertEquals(1, StringUtils.indexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.indexOf(null, "b"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(3, StringUtils.indexOf("abc", "", 9));

        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaabaa", "a", 2));
        assertEquals(3, StringUtils.ordinalIndexOf("aabaabaa", "a", 3));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 10));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaabaa", "", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaabaa", "a", 0));

        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "", 2));
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "A"));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b', 8));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", 'b', -1));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(0, StringUtils.lastIndexOf("", ""));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", "b", -1));

        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 10));
        assertEquals(8, StringUtils.lastOrdinalIndexOf("aabaabaa", "", 1));

        assertEquals(7, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aabaabaa", "", 2));
    }

    @Test(timeout = 4000)
    public void testContainsAndContainsIgnoreCase() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "BC"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "D"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyAndContainsAnyBasic() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[]{'b', 'y'}));

        assertEquals(-1, StringUtils.indexOfAny(null, "za"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));

        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertFalse(StringUtils.containsAny("aba", new char[]{'z'}));
        assertFalse(StringUtils.containsAny("aba", (String) null));
        assertFalse(StringUtils.containsAny("aba", ""));
        assertTrue(StringUtils.containsAny("zzabyycdxx", "za"));

        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", new char[]{'z', 'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", new char[]{'a', 'b'}));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        assertEquals(3, StringUtils.indexOfAnyBut("zzabyycdxx", "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", "ab"));
        assertEquals(-1, StringUtils.indexOfAnyBut(null, "za"));
        assertEquals(-1, StringUtils.indexOfAnyBut("aba", ""));

        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("ab", new char[0]));
        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("ab", (char[]) null));
        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b', 'c'}));
        assertFalse(StringUtils.containsOnly("ab1", new char[]{'a', 'b', 'c'}));
        assertTrue(StringUtils.containsOnly("abab", "abc"));
        assertFalse(StringUtils.containsOnly(null, "abc"));
        assertFalse(StringUtils.containsOnly("abab", (String) null));

        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("ab", (char[]) null));
        assertTrue(StringUtils.containsNone("", new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abab", new char[]{'x', 'y'}));
        assertFalse(StringUtils.containsNone("abab", new char[]{'a', 'y'}));
        assertTrue(StringUtils.containsNone(null, "abc"));
        assertTrue(StringUtils.containsNone("ab", (String) null));
        assertFalse(StringUtils.containsNone("abab", "a"));

        assertEquals(2, StringUtils.indexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String[]) null));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new String[]{""}));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"ab", "cd"}));
        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", (String[]) null));
        assertEquals(10, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{""}));
    }

    @Test(timeout = 4000)
    public void testSubstringMethods() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 1));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 1));
        assertEquals("", StringUtils.substring("", 0, 1));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));

        assertNull(StringUtils.left(null, 1));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));

        assertNull(StringUtils.right(null, 1));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));

        assertNull(StringUtils.mid(null, 0, 1));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 4, 1));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 3));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));

        assertNull(StringUtils.substringBefore(null, ":"));
        assertEquals("abc", StringUtils.substringBefore("abc:def", ":"));
        assertEquals("abc", StringUtils.substringBefore("abc", ":"));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("abc", StringUtils.substringBefore("abc", null));

        assertNull(StringUtils.substringAfter(null, ":"));
        assertEquals("def", StringUtils.substringAfter("abc:def", ":"));
        assertEquals("", StringUtils.substringAfter("abc", ":"));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
        assertEquals("", StringUtils.substringAfter("abc", null));

        assertNull(StringUtils.substringBeforeLast(null, ":"));
        assertEquals("abc:def", StringUtils.substringBeforeLast("abc:def:ghi", ":"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ":"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", ""));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));

        assertNull(StringUtils.substringAfterLast(null, ":"));
        assertEquals("ghi", StringUtils.substringAfterLast("abc:def:ghi", ":"));
        assertEquals("", StringUtils.substringAfterLast("abc", ":"));
        assertEquals("", StringUtils.substringAfterLast("abc", ""));
        assertEquals("", StringUtils.substringAfterLast("abc", null));

        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("abc", null, "]"));
        assertNull(StringUtils.substringBetween("abc", "[", null));
        assertEquals("", StringUtils.substringBetween("", "", ""));
        assertNull(StringUtils.substringBetween("", "[", "]"));
        assertEquals("b", StringUtils.substringBetween("a[b]c", "[", "]"));
        assertEquals("abc", StringUtils.substringBetween("yabcy", "y"));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", null, "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", null));
        assertNull(StringUtils.substringsBetween("abc", "", "]"));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a]foo[b]", "[", "]"));
        assertNull(StringUtils.substringsBetween("foobar", "[", "]"));
    }

    @Test(timeout = 4000)
    public void testSplitAndJoin() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def"}, StringUtils.split("abc def"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a::b::c", ":"));
        assertArrayEquals(new String[]{"a", "b::c"}, StringUtils.split("a::b::c", ":", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.split("ab:cd:ef", ":", 0));

        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab:!cd:!ef", ":!"));
        assertArrayEquals(new String[]{"ab", "cd:!ef"}, StringUtils.splitByWholeSeparator("ab:!cd:!ef", ":!", 2));
        assertNull(StringUtils.splitByWholeSeparator(null, ":!"));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", ":!"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.splitByWholeSeparator("a b", null));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.splitByWholeSeparator("a   b", ""));

        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:!:!cd", ":!"));
        assertArrayEquals(new String[]{"ab", ":!cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab:!:!cd", ":!", 2));
        assertArrayEquals(new String[]{"a", "", "", "b"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("a   b", null));

        assertArrayEquals(new String[]{"", "abc", ""}, StringUtils.splitPreserveAllTokens(" abc "));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"a", "", "b"}, StringUtils.splitPreserveAllTokens("a::b", ":"));
        assertArrayEquals(new String[]{"a", ":b"}, StringUtils.splitPreserveAllTokens("a::b", ":", 2));

        assertArrayEquals(new String[]{"foo", "200", "B", "ar"}, StringUtils.splitByCharacterType("foo200Bar"));
        assertArrayEquals(new String[]{"foo", "200", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));

        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("a,b", StringUtils.join(new Object[]{"a", "b"}, ",", 0, 2));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ",", 2, 2));
        assertEquals("a--b", StringUtils.join(new Object[]{"a", "b"}, "--"));

        List<String> list = Arrays.asList("foo", "bar");
        assertEquals("foo;bar", StringUtils.join(list.iterator(), ';'));
        assertEquals("foo,bar", StringUtils.join(list.iterator(), ","));
        assertEquals("foo;bar", StringUtils.join(list, ';'));
        assertEquals("foo,bar", StringUtils.join(list, ","));
        assertNull(StringUtils.join((Iterator<?>) null, ';'));
        assertNull(StringUtils.join((Iterator<?>) null, ","));
        assertNull(StringUtils.join((Iterable<?>) null, ';'));
        assertNull(StringUtils.join((Iterable<?>) null, ","));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), ';'));
        assertEquals("", StringUtils.join(Collections.emptyIterator(), ","));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo").iterator(), ';'));
        assertEquals("foo", StringUtils.join(Collections.singletonList("foo").iterator(), ","));
    }

    @Test(timeout = 4000)
    public void testDeleteRemoveReplaceAndOverlay() {
        assertEquals("abc", StringUtils.deleteWhitespace(" a b  c \n "));
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));

        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringUtils.removeStartIgnoreCase("WWW.domain.com", "www."));
        assertEquals("www.domain.com", StringUtils.removeStart("www.domain.com", "xyz"));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain", StringUtils.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("www.domain.com", StringUtils.removeEnd("www.domain.com", "xyz"));

        assertEquals("qd", StringUtils.remove("queued", "ue"));
        assertEquals("qeed", StringUtils.remove("queued", 'u'));
        assertNull(StringUtils.remove(null, "ue"));
        assertNull(StringUtils.remove(null, 'u'));

        assertEquals("zba", StringUtils.replaceOnce("aba", "a", "z"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zbaa", StringUtils.replace("abaa", "a", "z", 1));
        assertEquals("abaa", StringUtils.replace("abaa", "a", "z", 0));
        assertNull(StringUtils.replace(null, "a", "z"));

        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], null));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));

        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertNull(StringUtils.replaceChars(null, 'b', 'y'));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));

        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 8));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -2, -3));
        assertNull(StringUtils.overlay(null, "zzzz", 0, 1));
    }

    @Test(timeout = 4000)
    public void testChompAndChop() {
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc", StringUtils.chomp("abc"));
        assertEquals("", StringUtils.chomp("\r\n"));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("a", StringUtils.chomp("a"));
        assertNull(StringUtils.chomp(null));

        assertEquals("foo", StringUtils.chomp("foobar", "bar"));
        assertEquals("foobar", StringUtils.chomp("foobar", "baz"));
        assertNull(StringUtils.chomp(null, "bar"));
        assertEquals("foo", StringUtils.chomp("foo", null));

        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("abc", StringUtils.chop("abc\n"));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("", StringUtils.chop("\r\n"));
        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
    }

    @Test(timeout = 4000)
    public void testRepeatPadAndCenter() {
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("a", StringUtils.repeat("a", 1));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("", StringUtils.repeat("a", -1));
        assertEquals("", StringUtils.repeat("", 5));
        assertNull(StringUtils.repeat(null, 3));
        assertEquals("abcabc", StringUtils.repeat("abc", 2));

        assertEquals("a, a, a", StringUtils.repeat("a", ", ", 3));
        assertNull(StringUtils.repeat(null, ", ", 3));
        assertEquals("aaa", StringUtils.repeat("a", null, 3));

        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("bat", StringUtils.rightPad("bat", 2));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("batyzyzy", StringUtils.rightPad("bat", 8, "yz"));
        assertNull(StringUtils.rightPad(null, 5));

        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat", StringUtils.leftPad("bat", 2));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("yzyzybat", StringUtils.leftPad("bat", 8, "yz"));
        assertNull(StringUtils.leftPad(null, 5));

        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("ab", StringUtils.center("ab", 2));
        assertEquals("yaby", StringUtils.center("ab", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertNull(StringUtils.center(null, 4));
    }

    @Test(timeout = 4000)
    public void testLargePaddingBoundary() {
        // Exceeds PAD_LIMIT (8192)
        String paddedRight = StringUtils.rightPad("x", 8200, 'a');
        assertEquals(8200, paddedRight.length());
        assertTrue(paddedRight.startsWith("xaaaa"));

        String paddedLeft = StringUtils.leftPad("x", 8200, 'b');
        assertEquals(8200, paddedLeft.length());
        assertTrue(paddedLeft.endsWith("bbbbx"));

        String paddedRepeat = StringUtils.repeat("x", 8200);
        assertEquals(8200, paddedRepeat.length());
    }

    @Test(timeout = 4000)
    public void testCaseConversionsAndCounts() {
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));
        assertNull(StringUtils.upperCase(null));

        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));
        assertNull(StringUtils.lowerCase(null));

        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("", StringUtils.capitalize(""));
        assertNull(StringUtils.capitalize(null));

        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("", StringUtils.uncapitalize(""));
        assertNull(StringUtils.uncapitalize(null));

        assertEquals("tHE DOG HAS A bone", StringUtils.swapCase("The dog has a BONE"));
        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));

        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(0, StringUtils.countMatches("abba", "z"));
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abba", null));
        assertEquals(0, StringUtils.countMatches("abba", ""));
    }

    @Test(timeout = 4000)
    public void testCharacterTests() {
        assertTrue(StringUtils.isAlpha("abc"));
        assertTrue(StringUtils.isAlpha(""));
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertFalse(StringUtils.isAlphaSpace(null));
        assertFalse(StringUtils.isAlphaSpace("ab1 c"));

        assertTrue(StringUtils.isAlphanumeric("ab1c"));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertFalse(StringUtils.isAlphanumeric(null));
        assertFalse(StringUtils.isAlphanumeric("ab-c"));

        assertTrue(StringUtils.isAlphanumericSpace("ab 1c"));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertFalse(StringUtils.isAlphanumericSpace("ab-1c"));

        assertTrue(StringUtils.isAsciiPrintable("abc !~"));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertFalse(StringUtils.isAsciiPrintable(null));
        assertFalse(StringUtils.isAsciiPrintable("\u0000"));

        assertTrue(StringUtils.isNumeric("123"));
        assertTrue(StringUtils.isNumeric(""));
        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertTrue(StringUtils.isNumericSpace(""));
        assertFalse(StringUtils.isNumericSpace(null));
        assertFalse(StringUtils.isNumericSpace("12.3"));

        assertTrue(StringUtils.isWhitespace("  \t\n "));
        assertTrue(StringUtils.isWhitespace(""));
        assertFalse(StringUtils.isWhitespace(null));
        assertFalse(StringUtils.isWhitespace("  a "));

        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("abC"));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertFalse(StringUtils.isAllLowerCase(null));

        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("ABc"));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertFalse(StringUtils.isAllUpperCase(null));
    }

    @Test(timeout = 4000)
    public void testDefaultsReversingDifferenceAndLevenshtein() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "NULL"));

        assertEquals("cba", StringUtils.reverse("abc"));
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));

        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
        assertEquals("a.b.c", StringUtils.reverseDelimited("a.b.c", 'x'));
        assertNull(StringUtils.reverseDelimited(null, '.'));

        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));
        assertEquals("xyz", StringUtils.difference(null, "xyz"));
        assertEquals("abc", StringUtils.difference("abc", null));

        assertEquals(7, StringUtils.indexOfDifference("i am a machine", "i am a robot"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(-1, StringUtils.indexOfDifference((CharSequence[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[]{}));
        assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[]{"abc"}));
        assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[]{"abc", "abc"}));
        assertEquals(7, StringUtils.indexOfDifference(new CharSequence[]{"i am a machine", "i am a robot"}));
        assertEquals(0, StringUtils.indexOfDifference(new CharSequence[]{"", "abc"}));
        assertEquals(0, StringUtils.indexOfDifference(new CharSequence[]{null, "abc"}));

        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{}));
        assertEquals("abc", StringUtils.getCommonPrefix(new String[]{"abc"}));
        assertEquals("", StringUtils.getCommonPrefix(new String[]{"abc", "xyz"}));

        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(7, StringUtils.getLevenshteinDistance("aaapppp", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
        assertEquals(3, StringUtils.getLevenshteinDistance("fly", "ant"));
        assertEquals(7, StringUtils.getLevenshteinDistance("elephant", "hippo"));
        assertEquals(7, StringUtils.getLevenshteinDistance("hippo", "elephant"));
    }

    @Test(timeout = 4000)
    public void testPrefixAndSuffixChecks() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abcdef", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));

        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"def", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"def", "xyz"}));
        assertFalse(StringUtils.startsWithAny(null, new String[]{"abc"}));
        assertFalse(StringUtils.startsWithAny("abc", null));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("abcdef", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
    }

    @Test(timeout = 4000)
    public void testAbbreviateVariants() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));

        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", -1, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 0, 10));
        assertEquals("abcdefg...", StringUtils.abbreviate("abcdefghijklmno", 4, 10));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ghij...", StringUtils.abbreviate("abcdefghijklmno", 6, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 8, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));

        assertNull(StringUtils.abbreviateMiddle(null, ".", 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 0));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", ".", 3));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testLengthMethod() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(0, StringUtils.length(""));
        assertEquals(3, StringUtils.length("abc"));
    }

    @Test(timeout = 4000)
    public void testReplaceEachEdgeCases() {
        // Empty strings in replacement or search list
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{""}, new String[]{"z"}));

        // Replacement expanding string buffer
        String large = StringUtils.replaceEach("abc", new String[]{"a", "b", "c"}, new String[]{"longA", "longB", "longC"});
        assertEquals("longAlongBlongC", large);
    }

    @Test(timeout = 4000)
    public void testJoinRangeExtremes() {
        Object[] array = new Object[]{"1", "2", "3", "4"};
        assertEquals("", StringUtils.join(array, ",", 2, 1)); // start > end returns EMPTY
        assertEquals("2,3", StringUtils.join(array, ",", 1, 3));
        assertEquals("", StringUtils.join(array, ",", 0, 0));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateWidthTooSmall() {
        StringUtils.abbreviate("abcdef", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetWidthTooSmall() {
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
        StringUtils.replaceEach("text", new String[]{"a"}, new String[]{"b", "c"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachRepeatedlyInfiniteLoop() {
        // Cyclic replacement causes timeToLive exhaustion
        StringUtils.replaceEachRepeatedly("abcde", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        // Public constructor permitted for JavaBean tools
        StringUtils utils = new StringUtils();
        assertNotNull(utils);

        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }
}