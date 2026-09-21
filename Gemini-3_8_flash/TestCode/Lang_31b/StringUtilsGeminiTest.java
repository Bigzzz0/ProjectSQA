package org.apache.commons.lang3;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects (Defects4J ground truth):
 * - StringUtilsEqualsIndexOfTest::testContainsAnyCharArrayWithSupplementaryChars
 *   Issue: containsAny(CharSequence, char[]) iterates 16-bit char values rather than Unicode code points.
 *   When testing two different supplementary characters that share a high or low surrogate (e.g. \uD840\uDC00 vs \uD840\uDC01),
 *   the method wrongly returns true on matching the surrogate alone.
 * - StringUtilsEqualsIndexOfTest::testContainsAnyStringWithSupplementaryChars
 *   Issue: containsAny(CharSequence, String) delegates to containsAny(CharSequence, char[]) suffering the exact same defect.
 *
 * Targeted Decision Branches & Boundary Partitions:
 * - Partition A: Core Functional Logic & State Transitions (null safety, normal execution paths for all operations).
 * - Partition B: Boundary Value Analysis (empty strings, single characters, PAD_LIMIT boundaries, length mismatches).
 * - Partition C: Defect-Targeted Branch Zone (Unicode surrogate pairs & supplementary character handling in containsAny).
 * - Partition D: Exception & Defensive Guard Paths (IllegalArgumentException, IndexOutOfBoundsException, IllegalStateException).
 * - Partition E: Object Lifecycle & Structural Integrity (public constructor instantiation for JavaBean compliance).
 */

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testContainsAnyCharArrayWithSupplementaryChars() {
        // Unicode Supplementary characters sharing high surrogate \uD840:
        // U+20000 = \uD840\uDC00
        // U+20001 = \uD840\uDC01
        String cs = "\uD840\uDC00";
        char[] searchChars = "\uD840\uDC01".toCharArray();
        assertFalse("containsAny should not match different supplementary characters sharing a surrogate",
                StringUtils.containsAny(cs, searchChars));
    }

    @Test(timeout = 4000)
    public void testContainsAnyStringWithSupplementaryChars() {
        String cs = "\uD840\uDC00";
        String searchChars = "\uD840\uDC01";
        assertFalse("containsAny should not match different supplementary characters sharing a surrogate",
                StringUtils.containsAny(cs, searchChars));
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & JAVABEAN CONTRACT
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        StringUtils instance = new StringUtils();
        assertNotNull(instance);
        assertEquals("", StringUtils.EMPTY);
        assertEquals(-1, StringUtils.INDEX_NOT_FOUND);
    }

    // =========================================================================
    // PARTITION A & B: EMPTY, BLANK, AND NULL CHECKS
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("bob"));
    }

    @Test(timeout = 4000)
    public void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   \t\r\n"));
        assertFalse(StringUtils.isBlank("  bob  "));
        assertFalse(StringUtils.isBlank("bob"));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   \t\r\n"));
        assertTrue(StringUtils.isNotBlank("  bob  "));
        assertTrue(StringUtils.isNotBlank("bob"));
    }

    // =========================================================================
    // TRIM AND STRIP OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testTrimOperations() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   \t  "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("   \t  "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   \t  "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test(timeout = 4000)
    public void testStripOperations() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));

        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull(""));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("  abc  "));

        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("  abc  "));

        assertNull(StringUtils.strip(null, "xyz"));
        assertEquals("", StringUtils.strip("", "xyz"));
        assertEquals("abc", StringUtils.strip("yxabcyx", "xyz"));
        assertEquals("abc", StringUtils.strip("  abc  ", null));
        assertEquals("  abc  ", StringUtils.strip("  abc  ", ""));
    }

    @Test(timeout = 4000)
    public void testStripStartAndEnd() {
        assertNull(StringUtils.stripStart(null, "a"));
        assertEquals("", StringUtils.stripStart("", "a"));
        assertEquals("abc  ", StringUtils.stripStart("  abc  ", null));
        assertEquals("abc", StringUtils.stripStart("yyabc", "y"));
        assertEquals("yyabc", StringUtils.stripStart("yyabc", ""));

        assertNull(StringUtils.stripEnd(null, "a"));
        assertEquals("", StringUtils.stripEnd("", "a"));
        assertEquals("  abc", StringUtils.stripEnd("  abc  ", null));
        assertEquals("abc", StringUtils.stripEnd("abcyy", "y"));
        assertEquals("abcyy", StringUtils.stripEnd("abcyy", ""));
    }

    @Test(timeout = 4000)
    public void testStripAll() {
        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{"  a  ", " b ", null}));
        assertArrayEquals(new String[]{"a", "b", null}, StringUtils.stripAll(new String[]{"xax", "xbx", null}, "x"));
        assertNull(StringUtils.stripAll(null, "x"));
    }

    @Test(timeout = 4000)
    public void testStripAccents() {
        assertNull(StringUtils.stripAccents(null));
        assertEquals("", StringUtils.stripAccents(""));
        assertEquals("eclair", StringUtils.stripAccents("\u00e9clair"));
        assertEquals("control", StringUtils.stripAccents("control"));
    }

    // =========================================================================
    // EQUALITY & COMPARISONS
    // =========================================================================

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
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    // =========================================================================
    // INDEX OF & CONTAINS OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testIndexOfChar() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(1, StringUtils.indexOf("aabaa", 'b'));
        assertEquals(-1, StringUtils.indexOf("aabaa", 'z'));

        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("", 'a', 0));
        assertEquals(1, StringUtils.indexOf("aabaa", 'b', 0));
        assertEquals(1, StringUtils.indexOf("aabaa", 'b', -10));
        assertEquals(-1, StringUtils.indexOf("aabaa", 'b', 2));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("a", null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(-1, StringUtils.indexOf("", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));

        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("a", null, 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 10));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
        assertEquals(3, StringUtils.indexOf("abc", "", 10));
    }

    @Test(timeout = 4000)
    public void testOrdinalIndexOf() {
        assertEquals(-1, StringUtils.ordinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaa", null, 1));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaa", "a", 0));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaa", "a", -1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaa", "", 1));
        assertEquals(0, StringUtils.ordinalIndexOf("aabaa", "a", 1));
        assertEquals(1, StringUtils.ordinalIndexOf("aabaa", "a", 2));
        assertEquals(3, StringUtils.ordinalIndexOf("aabaa", "a", 3));
        assertEquals(-1, StringUtils.ordinalIndexOf("aabaa", "a", 5));
    }

    @Test(timeout = 4000)
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaa", null, 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaa", "a", 0));
        assertEquals(5, StringUtils.lastOrdinalIndexOf("aabaa", "", 1));
        assertEquals(4, StringUtils.lastOrdinalIndexOf("aabaa", "a", 1));
        assertEquals(3, StringUtils.lastOrdinalIndexOf("aabaa", "a", 2));
        assertEquals(1, StringUtils.lastOrdinalIndexOf("aabaa", "a", 3));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("aabaa", "a", 5));
    }

    @Test(timeout = 4000)
    public void testIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("a", null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aBa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aBa", "b", -1));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aBa", "b", 3));
        assertEquals(2, StringUtils.indexOfIgnoreCase("abc", "", 2));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("abc", "d", 0));
    }

    @Test(timeout = 4000)
    public void testLastIndexOf() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(4, StringUtils.lastIndexOf("aabaa", 'a'));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a', 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaa", 'b', 0));
        assertEquals(2, StringUtils.lastIndexOf("aabaa", 'b', 4));
        assertEquals(2, StringUtils.lastIndexOf("aabaa", 'b', 10));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(4, StringUtils.lastIndexOf("aabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaa", ""));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOf("a", null, 0));
        assertEquals(-1, StringUtils.lastIndexOf("aabaa", "b", -1));
        assertEquals(2, StringUtils.lastIndexOf("aabaa", "b", 2));
        assertEquals(2, StringUtils.lastIndexOf("aabaa", "b", 10));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aAbAa", "A"));
        assertEquals(4, StringUtils.lastIndexOfIgnoreCase("aAbAa", "a"));

        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("a", null, 0));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aAbAa", "B", 10));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aAbAa", "B", -1));
        assertEquals(2, StringUtils.lastIndexOfIgnoreCase("aAbAa", "", 2));
        assertEquals(-1, StringUtils.lastIndexOfIgnoreCase("aAbAa", "Z", 4));
    }

    @Test(timeout = 4000)
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));

        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "BC"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "D"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyAndContainsAny() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", new char[0]));
        assertEquals(1, StringUtils.indexOfAny("zzabyy", new char[]{'b', 'a'}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyy", new char[]{'c', 'd'}));

        assertEquals(-1, StringUtils.indexOfAny(null, "ab"));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("abc", ""));
        assertEquals(0, StringUtils.indexOfAny("abc", "a"));

        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
        assertFalse(StringUtils.containsAny("abc", new char[0]));
        assertTrue(StringUtils.containsAny("abc", new char[]{'c', 'd'}));
        assertFalse(StringUtils.containsAny("abc", new char[]{'x', 'y'}));

        assertFalse(StringUtils.containsAny("abc", (String) null));
        assertFalse(StringUtils.containsAny(null, "a"));
        assertTrue(StringUtils.containsAny("abc", "c"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyBut() {
        assertEquals(-1, StringUtils.indexOfAnyBut(null, new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("", new char[]{'a'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", new char[0]));
        assertEquals(2, StringUtils.indexOfAnyBut("zzabyy", new char[]{'z'}));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzz", new char[]{'z'}));

        assertEquals(-1, StringUtils.indexOfAnyBut(null, "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("", "a"));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", (String) null));
        assertEquals(-1, StringUtils.indexOfAnyBut("abc", ""));
        assertEquals(2, StringUtils.indexOfAnyBut("zzabyy", "z"));
        assertEquals(-1, StringUtils.indexOfAnyBut("zzz", "z"));
    }

    @Test(timeout = 4000)
    public void testContainsOnlyAndContainsNone() {
        assertFalse(StringUtils.containsOnly(null, new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("abc", (char[]) null));
        assertTrue(StringUtils.containsOnly("", new char[]{'a'}));
        assertFalse(StringUtils.containsOnly("ab", new char[0]));
        assertTrue(StringUtils.containsOnly("abab", new char[]{'a', 'b'}));
        assertFalse(StringUtils.containsOnly("abac", new char[]{'a', 'b'}));

        assertFalse(StringUtils.containsOnly(null, "ab"));
        assertFalse(StringUtils.containsOnly("abc", (String) null));
        assertTrue(StringUtils.containsOnly("abab", "ab"));

        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abc", (char[]) null));
        assertTrue(StringUtils.containsNone("", new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abc", new char[]{'x', 'y'}));
        assertFalse(StringUtils.containsNone("abc", new char[]{'a', 'x'}));

        assertTrue(StringUtils.containsNone(null, "a"));
        assertTrue(StringUtils.containsNone("abc", (String) null));
        assertTrue(StringUtils.containsNone("abc", "xyz"));
        assertFalse(StringUtils.containsNone("abc", "axyz"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAnyAndLastIndexOfAnyStrings() {
        assertEquals(-1, StringUtils.indexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.indexOfAny("abc", (String[]) null));
        assertEquals(0, StringUtils.indexOfAny("abc", new String[]{""}));
        assertEquals(1, StringUtils.indexOfAny("zzabyycd", new String[]{"cd", "ab", null}));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycd", new String[]{"mn", "op"}));

        assertEquals(-1, StringUtils.lastIndexOfAny(null, new String[]{"ab"}));
        assertEquals(-1, StringUtils.lastIndexOfAny("abc", (String[]) null));
        assertEquals(3, StringUtils.lastIndexOfAny("abc", new String[]{""}));
        assertEquals(6, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"cd", "ab", null}));
        assertEquals(-1, StringUtils.lastIndexOfAny("zzabyycdxx", new String[]{"mn", "op"}));
    }

    // =========================================================================
    // SUBSTRING OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 1));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("c", StringUtils.substring("abc", 2));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
        assertEquals("", StringUtils.substring("abc", 4, 6));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
        assertEquals("ab", StringUtils.substring("abc", -4, 2));
        assertEquals("", StringUtils.substring("abc", -1, -2));
        assertEquals("", StringUtils.substring("abc", -2, -4));
    }

    @Test(timeout = 4000)
    public void testLeftRightMid() {
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("", 2));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("", 2));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 5, 2));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
        assertEquals("abc", StringUtils.mid("abc", 0, 5));
        assertEquals("bc", StringUtils.mid("abc", 1, 2));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeAndAfter() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", ""));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
        assertEquals("abc", StringUtils.substringBefore("abc", "z"));

        assertNull(StringUtils.substringAfter(null, "a"));
        assertEquals("", StringUtils.substringAfter("", "a"));
        assertEquals("", StringUtils.substringAfter("abc", null));
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
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
    }

    @Test(timeout = 4000)
    public void testSubstringBetween() {
        assertNull(StringUtils.substringBetween(null, "tag"));
        assertNull(StringUtils.substringBetween("abc", null));
        assertEquals("", StringUtils.substringBetween("", ""));
        assertNull(StringUtils.substringBetween("", "tag"));
        assertEquals("abc", StringUtils.substringBetween("[abc]", "[", "]"));
        assertNull(StringUtils.substringBetween("[abc", "[", "]"));
        assertNull(StringUtils.substringBetween(null, "[", "]"));
        assertNull(StringUtils.substringBetween("[abc]", null, "]"));
        assertNull(StringUtils.substringBetween("[abc]", "[", null));
        assertEquals("", StringUtils.substringBetween("yabcz", "", ""));

        assertNull(StringUtils.substringsBetween(null, "[", "]"));
        assertNull(StringUtils.substringsBetween("abc", null, "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", null));
        assertNull(StringUtils.substringsBetween("abc", "", "]"));
        assertNull(StringUtils.substringsBetween("abc", "[", ""));
        assertArrayEquals(new String[0], StringUtils.substringsBetween("", "[", "]"));
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.substringsBetween("[a][b]", "[", "]"));
        assertNull(StringUtils.substringsBetween("no tags here", "[", "]"));
        assertNull(StringUtils.substringsBetween("[open without close", "[", "]"));
    }

    // =========================================================================
    // SPLIT OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"ab", "cd"}, StringUtils.split("ab cd"));
        assertArrayEquals(new String[]{"ab", "cd"}, StringUtils.split("ab   cd"));

        assertNull(StringUtils.split(null, '.'));
        assertArrayEquals(new String[0], StringUtils.split("", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));

        assertNull(StringUtils.split(null, ":"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ":"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b c", (String) null));

        assertNull(StringUtils.split(null, ":", 2));
        assertArrayEquals(new String[0], StringUtils.split("", ":", 2));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a:b:c", ":", 0));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"ab", "de fg"}, StringUtils.split("ab de fg", null, 2));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.split("a::b:c", ":;", 2));
    }

    @Test(timeout = 4000)
    public void testSplitPreserveAllTokens() {
        assertNull(StringUtils.splitPreserveAllTokens(null));
        assertArrayEquals(new String[0], StringUtils.splitPreserveAllTokens(""));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a  b c"));

        assertNull(StringUtils.splitPreserveAllTokens(null, '.'));
        assertArrayEquals(new String[]{"a", "", "b", "c"}, StringUtils.splitPreserveAllTokens("a..b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", ""}, StringUtils.splitPreserveAllTokens("a.b.", '.'));

        assertNull(StringUtils.splitPreserveAllTokens(null, ":"));
        assertArrayEquals(new String[]{"", "a", "b", ""}, StringUtils.splitPreserveAllTokens(":a:b:", ":"));
        assertArrayEquals(new String[]{"a", "b:c"}, StringUtils.splitPreserveAllTokens("a:b:c", ":", 2));
        assertArrayEquals(new String[]{"a", "", "b:c"}, StringUtils.splitPreserveAllTokens("a::b:c", ":", 3));
        assertArrayEquals(new String[]{"a", " b c"}, StringUtils.splitPreserveAllTokens("a  b c", null, 2));
        assertArrayEquals(new String[]{"a", ";b:c"}, StringUtils.splitPreserveAllTokens("a:;b:c", ":;", 2));
    }

    @Test(timeout = 4000)
    public void testSplitByWholeSeparator() {
        assertNull(StringUtils.splitByWholeSeparator(null, "."));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparator("", "."));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-"));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab cd ef", null));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab cd ef", ""));

        assertArrayEquals(new String[]{"ab", "cd-!-ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 0));

        assertNull(StringUtils.splitByWholeSeparatorPreserveAllTokens(null, "."));
        assertArrayEquals(new String[0], StringUtils.splitByWholeSeparatorPreserveAllTokens("", "."));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!----!-cd", "-!-"));
        assertArrayEquals(new String[]{"ab", "-!-cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab-!----!-cd", "-!-", 2));
        assertArrayEquals(new String[]{"ab", "", "cd"}, StringUtils.splitByWholeSeparatorPreserveAllTokens("ab   cd", null));
    }

    @Test(timeout = 4000)
    public void testSplitByCharacterType() {
        assertNull(StringUtils.splitByCharacterType(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterType(""));
        assertArrayEquals(new String[]{"ab", " ", "de", " ", "fg"}, StringUtils.splitByCharacterType("ab de fg"));
        assertArrayEquals(new String[]{"foo", "B", "ar"}, StringUtils.splitByCharacterType("fooBar"));

        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
        assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
        assertArrayEquals(new String[]{"ASF", "Rules"}, StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    // =========================================================================
    // JOIN OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testJoinObjectArray() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("", StringUtils.join(new Object[]{null}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a;b;c", StringUtils.join(new Object[]{"a", "b", "c"}, ';'));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, ';', 1, 1));
        assertNull(StringUtils.join((Object[]) null, ';', 0, 1));

        assertEquals("a--b--c", StringUtils.join(new Object[]{"a", "b", "c"}, "--"));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals("", StringUtils.join(new Object[]{"a", "b"}, "--", 1, 1));
        assertNull(StringUtils.join((Object[]) null, "--", 0, 1));
    }

    @Test(timeout = 4000)
    public void testJoinIteratorsAndCollections() {
        assertNull(StringUtils.join((Iterator<?>) null, ','));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), ','));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ','));
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c").iterator(), ','));

        assertNull(StringUtils.join((Iterator<?>) null, ","));
        assertEquals("", StringUtils.join(Collections.emptyList().iterator(), ","));
        assertEquals("a", StringUtils.join(Collections.singletonList("a").iterator(), ","));
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c").iterator(), ","));
        assertEquals("abc", StringUtils.join(Arrays.asList("a", "b", "c").iterator(), null));

        assertNull(StringUtils.join((Iterable<?>) null, ','));
        assertEquals("a,b", StringUtils.join((Iterable<?>) Arrays.asList("a", "b"), ','));
        assertNull(StringUtils.join((Iterable<?>) null, ","));
        assertEquals("a,b", StringUtils.join((Iterable<?>) Arrays.asList("a", "b"), ","));
    }

    // =========================================================================
    // REMOVAL & REPLACEMENT
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeleteWhitespace() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("  a \t b \r\n c "));
        assertEquals("abc", StringUtils.deleteWhitespace("abc"));
    }

    @Test(timeout = 4000)
    public void testRemoveStartAndEnd() {
        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", null));
        assertEquals("abc", StringUtils.removeStart("abc", ""));
        assertEquals("bc", StringUtils.removeStart("abc", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", "z"));

        assertNull(StringUtils.removeStartIgnoreCase(null, "a"));
        assertEquals("bc", StringUtils.removeStartIgnoreCase("abc", "A"));
        assertEquals("abc", StringUtils.removeStartIgnoreCase("abc", "Z"));

        assertNull(StringUtils.removeEnd(null, "a"));
        assertEquals("", StringUtils.removeEnd("", "a"));
        assertEquals("abc", StringUtils.removeEnd("abc", null));
        assertEquals("abc", StringUtils.removeEnd("abc", ""));
        assertEquals("ab", StringUtils.removeEnd("abc", "c"));
        assertEquals("abc", StringUtils.removeEnd("abc", "z"));

        assertNull(StringUtils.removeEndIgnoreCase(null, "a"));
        assertEquals("ab", StringUtils.removeEndIgnoreCase("abc", "C"));
        assertEquals("abc", StringUtils.removeEndIgnoreCase("abc", "Z"));

        assertNull(StringUtils.remove(null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("abc", StringUtils.remove("abc", null));
        assertEquals("abc", StringUtils.remove("abc", ""));
        assertEquals("ac", StringUtils.remove("abc", "b"));

        assertNull(StringUtils.remove(null, 'a'));
        assertEquals("", StringUtils.remove("", 'a'));
        assertEquals("ac", StringUtils.remove("abc", 'b'));
        assertEquals("abc", StringUtils.remove("abc", 'z'));
    }

    @Test(timeout = 4000)
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("abc", StringUtils.replace("abc", null, "b"));
        assertEquals("abc", StringUtils.replace("abc", "a", null));
        assertEquals("abc", StringUtils.replace("abc", "", "b"));
        assertEquals("abc", StringUtils.replace("abc", "a", "b", 0));
        assertEquals("zbc", StringUtils.replaceOnce("abc", "a", "z"));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z"));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z", -1));
        assertEquals("aba", StringUtils.replace("aba", "x", "z"));
    }

    @Test(timeout = 4000)
    public void testReplaceEach() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("abc", StringUtils.replaceEach("abc", null, new String[]{"b"}));
        assertEquals("abc", StringUtils.replaceEach("abc", new String[0], new String[]{"b"}));
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"a"}, null));
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"a"}, new String[0]));
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
        assertEquals("tcte", StringUtils.replaceEachRepeatedly("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}));
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"z"}, new String[]{"x"}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachLengthMismatchThrows() {
        StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"z"});
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReplaceEachRepeatedlyCircularThrows() {
        StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"b", "a"});
    }

    @Test(timeout = 4000)
    public void testReplaceChars() {
        assertNull(StringUtils.replaceChars(null, 'a', 'b'));
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));

        assertNull(StringUtils.replaceChars(null, "a", "b"));
        assertEquals("", StringUtils.replaceChars("", "a", "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", null, "b"));
        assertEquals("abc", StringUtils.replaceChars("abc", "", "b"));
        assertEquals("ayzya", StringUtils.replaceChars("abcba", "bc", "yz"));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", ""));
        assertEquals("ayya", StringUtils.replaceChars("abcba", "bc", "y"));
        assertEquals("abc", StringUtils.replaceChars("abc", "z", "x"));
    }

    @Test(timeout = 4000)
    public void testOverlay() {
        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abc", StringUtils.overlay("", "abc", 0, 0));
        assertEquals("abef", StringUtils.overlay("abcdef", null, 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 4, 2));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
        assertEquals("abzzzz", StringUtils.overlay("abcdef", "zzzz", 2, 10));
        assertEquals("zzzzabcdef", StringUtils.overlay("abcdef", "zzzz", -5, -2));
    }

    // =========================================================================
    // CHOMP & CHOP
    // =========================================================================

    @Test(timeout = 4000)
    public void testChomp() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("", StringUtils.chomp("\r"));
        assertEquals("", StringUtils.chomp("\n"));
        assertEquals("a", StringUtils.chomp("a"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r"));
        assertEquals("abc\r\n", StringUtils.chomp("abc\r\n\r\n"));
        assertEquals("abc", StringUtils.chomp("abc", "xyz"));
        assertNull(StringUtils.chomp(null, "xyz"));
        assertEquals("abc", StringUtils.chomp("abc", null));
        assertEquals("abc", StringUtils.chomp("abcxyz", "xyz"));
    }

    @Test(timeout = 4000)
    public void testChop() {
        assertNull(StringUtils.chop(null));
        assertEquals("", StringUtils.chop(""));
        assertEquals("", StringUtils.chop("a"));
        assertEquals("ab", StringUtils.chop("abc"));
        assertEquals("abc", StringUtils.chop("abc\r\n"));
        assertEquals("abc", StringUtils.chop("abc\n"));
    }

    // =========================================================================
    // PADDING & CENTERING
    // =========================================================================

    @Test(timeout = 4000)
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -1));
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("aaa", StringUtils.repeat("a", 3));
        assertEquals("abab", StringUtils.repeat("ab", 2));
        assertEquals("abcabc", StringUtils.repeat("abc", 2));
        assertEquals("?, ?, ?", StringUtils.repeat("?", ", ", 3));
        assertNull(StringUtils.repeat(null, ", ", 3));
        assertEquals("???", StringUtils.repeat("?", null, 3));
    }

    @Test(timeout = 4000)
    public void testPad() {
        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("bat", StringUtils.rightPad("bat", 1));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("batzz", StringUtils.rightPad("bat", 5, 'z'));
        assertEquals("batyz", StringUtils.rightPad("bat", 5, "yz"));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5, ""));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("bat", StringUtils.leftPad("bat", 1));
        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("zzbat", StringUtils.leftPad("bat", 5, 'z'));
        assertEquals("yzbat", StringUtils.leftPad("bat", 5, "yz"));
        assertEquals("  bat", StringUtils.leftPad("bat", 5, ""));

        // Limit testing for PAD_LIMIT (> 8192)
        assertEquals(8200, StringUtils.leftPad("a", 8200, ' ').length());
        assertEquals(8200, StringUtils.rightPad("a", 8200, ' ').length());
    }

    @Test(timeout = 4000)
    public void testCenter() {
        assertNull(StringUtils.center(null, 5));
        assertEquals("ab", StringUtils.center("ab", -1));
        assertEquals("ab", StringUtils.center("ab", 2));
        assertEquals(" ab ", StringUtils.center("ab", 4));
        assertEquals("yaby", StringUtils.center("ab", 4, 'y'));
        assertEquals("yayz", StringUtils.center("a", 4, "yz"));
        assertEquals("  a   ", StringUtils.center("a", 6, ""));
    }

    @Test(timeout = 4000)
    public void testLength() {
        assertEquals(0, StringUtils.length(null));
        assertEquals(3, StringUtils.length("abc"));
    }

    // =========================================================================
    // CASE CONVERSION & WORD CHECKS
    // =========================================================================

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
        assertEquals("tHE dOG", StringUtils.swapCase("The Dog"));
    }

    // =========================================================================
    // CHARACTER CLASSIFICATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testCharacterClassifications() {
        assertFalse(StringUtils.isAlpha(null));
        assertTrue(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertFalse(StringUtils.isAlphaSpace(null));
        assertTrue(StringUtils.isAlphaSpace(""));
        assertTrue(StringUtils.isAlphaSpace("ab c"));
        assertFalse(StringUtils.isAlphaSpace("ab1 c"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertTrue(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("ab1c"));
        assertFalse(StringUtils.isAlphanumeric("ab-1c"));

        assertFalse(StringUtils.isAlphanumericSpace(null));
        assertTrue(StringUtils.isAlphanumericSpace(""));
        assertTrue(StringUtils.isAlphanumericSpace("ab 1c"));
        assertFalse(StringUtils.isAlphanumericSpace("ab-1c"));

        assertFalse(StringUtils.isAsciiPrintable(null));
        assertTrue(StringUtils.isAsciiPrintable(""));
        assertTrue(StringUtils.isAsciiPrintable("abc ~!"));
        assertFalse(StringUtils.isAsciiPrintable("abc\u0000"));

        assertFalse(StringUtils.isNumeric(null));
        assertTrue(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertFalse(StringUtils.isNumericSpace(null));
        assertTrue(StringUtils.isNumericSpace(""));
        assertTrue(StringUtils.isNumericSpace("12 3"));
        assertFalse(StringUtils.isNumericSpace("12.3"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace(" \t\r\n "));
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

    // =========================================================================
    // DEFAULTS, REVERSING, ABBREVIATING & DIFFERENCES
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaults() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("abc", StringUtils.defaultString("abc", "default"));

        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "default"));
    }

    @Test(timeout = 4000)
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("cba", StringUtils.reverse("abc"));
        assertNull(StringUtils.reverseDelimited(null, '.'));
        assertEquals("c.b.a", StringUtils.reverseDelimited("a.b.c", '.'));
    }

    @Test(timeout = 4000)
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("...fghi...", StringUtils.abbreviate("abcdefghijklmno", 5, 10));
        assertEquals("...ijklmno", StringUtils.abbreviate("abcdefghijklmno", 12, 10));

        assertNull(StringUtils.abbreviateMiddle(null, ".", 4));
        assertEquals("abc", StringUtils.abbreviateMiddle("abc", null, 4));
        assertEquals("ab.f", StringUtils.abbreviateMiddle("abcdef", ".", 4));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateInvalidWidthThrows() {
        StringUtils.abbreviate("abcdef", 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateOffsetInvalidWidthThrows() {
        StringUtils.abbreviate("abcdefghij", 5, 6);
    }

    @Test(timeout = 4000)
    public void testDifferenceAndPrefix() {
        assertNull(StringUtils.difference(null, null));
        assertEquals("abc", StringUtils.difference("", "abc"));
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));

        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference(null, "abc"));
        assertEquals(2, StringUtils.indexOfDifference("ab", "abxyz"));
        assertEquals(-1, StringUtils.indexOfDifference("abc", "abc"));

        assertEquals(-1, StringUtils.indexOfDifference((CharSequence[]) null));
        assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[]{"abc"}));
        assertEquals(0, StringUtils.indexOfDifference(new CharSequence[]{"abc", null}));
        assertEquals(2, StringUtils.indexOfDifference(new CharSequence[]{"ab", "abxyz"}));
        assertEquals(-1, StringUtils.indexOfDifference(new CharSequence[]{"abc", "abc"}));

        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("i am a ", StringUtils.getCommonPrefix(new String[]{"i am a machine", "i am a robot"}));
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

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLevenshteinDistanceNullThrows() {
        StringUtils.getLevenshteinDistance(null, "a");
    }

    // =========================================================================
    // STARTS WITH & ENDS WITH
    // =========================================================================

    @Test(timeout = 4000)
    public void testStartsWithAndEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abc", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("abcdef", "ABC"));
        assertFalse(StringUtils.startsWith("abc", "abcdef"));

        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));

        assertFalse(StringUtils.startsWithAny(null, new String[]{"a"}));
        assertFalse(StringUtils.startsWithAny("abc", null));
        assertFalse(StringUtils.startsWithAny("abc", new String[0]));
        assertTrue(StringUtils.startsWithAny("abcxyz", new String[]{"xyz", "abc"}));
        assertFalse(StringUtils.startsWithAny("abcxyz", new String[]{"def"}));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("def", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("abcdef", "DEF"));
        assertFalse(StringUtils.endsWith("def", "abcdef"));

        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
    }

    @Test(timeout = 4000)
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abc", null));
        assertEquals(0, StringUtils.countMatches("", "a"));
        assertEquals(0, StringUtils.countMatches("abc", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));
        assertEquals(0, StringUtils.countMatches("abba", "z"));
    }
}