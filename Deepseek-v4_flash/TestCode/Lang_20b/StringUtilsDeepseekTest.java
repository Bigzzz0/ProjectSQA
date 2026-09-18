package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - isEmpty/isBlank: null, empty, whitespace, non-empty
 *   - trim/strip: null, empty, whitespace, mixed, with/without stripChars
 *   - equals/equalsIgnoreCase: null combinations, case, same object
 *   - indexOf/lastIndexOf: null, empty, char, string, startPos, ordinal
 *   - contains/containsAny/containsNone/containsOnly: null, empty, char arrays/strings
 *   - substring/left/right/mid: null, negative, zero, valid, overflow
 *   - substringBefore/After/BeforeLast/AfterLast: null, empty, separator
 *   - substringBetween: null tags, missing, multiple
 *   - split/join: null, empty, various separators, preserve tokens
 *   - replace/remove/overlay: null, empty, max limits
 *   - padding: null, negative, zero, large
 *   - case conversion: null, empty, mixed case
 *   - countMatches, isAlpha, isNumeric, etc.
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null inputs for all methods returning nullable values
 *   - empty strings, negative indices, zero lengths, max int bounds
 *   - String with only whitespace, supplementary characters (surrogates)
 *   - Arrays with null elements, empty arrays, large arrays
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: NPE in join(Object[], char) when array contains null elements
 *     (the method should skip nulls, but defective version may call toString() on null)
 *   - Also test join(Object[], String) with null elements and null separator
 *   - Test join(Iterator) with null elements
 *   - Test join with varargs null
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - abbreviate with maxWidth < 4 => IllegalArgumentException
 *   - getLevenshteinDistance with null => IllegalArgumentException
 *   - replaceEach with mismatched lengths => IllegalArgumentException
 *   - replaceEachRepeatedly with circular ref => IllegalStateException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - N/A for static utility class; covered by consistency checks
 */
public class StringUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    // --- isEmpty / isNotEmpty ---
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

    // --- isBlank / isNotBlank ---
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
        assertTrue(StringUtils.isNotBlank("  bob  "));
    }

    // --- trim / trimToNull / trimToEmpty ---
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

    // --- strip / stripToNull / stripToEmpty ---
    @Test(timeout = 4000)
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("abc"));
        assertEquals("abc", StringUtils.strip("  abc"));
        assertEquals("abc", StringUtils.strip("abc  "));
        assertEquals("ab c", StringUtils.strip(" ab c "));
    }

    @Test(timeout = 4000)
    public void testStripToNull() {
        assertNull(StringUtils.stripToNull(null));
        assertNull(StringUtils.stripToNull(""));
        assertNull(StringUtils.stripToNull("   "));
        assertEquals("abc", StringUtils.stripToNull("abc"));
    }

    @Test(timeout = 4000)
    public void testStripToEmpty() {
        assertEquals("", StringUtils.stripToEmpty(null));
        assertEquals("", StringUtils.stripToEmpty(""));
        assertEquals("", StringUtils.stripToEmpty("   "));
        assertEquals("abc", StringUtils.stripToEmpty("abc"));
    }

    // --- strip(String, String) ---
    @Test(timeout = 4000)
    public void testStripWithStripChars() {
        assertNull(StringUtils.strip(null, "*"));
        assertEquals("", StringUtils.strip("", "*"));
        assertEquals("abc", StringUtils.strip("abc", null));
        assertEquals("  abc", StringUtils.strip("  abcyx", "xyz"));
        assertEquals("  abc", StringUtils.strip("  abcyx", "xyz"));
    }

    // --- stripStart / stripEnd ---
    @Test(timeout = 4000)
    public void testStripStart() {
        assertNull(StringUtils.stripStart(null, null));
        assertEquals("", StringUtils.stripStart("", null));
        assertEquals("abc", StringUtils.stripStart("abc", ""));
        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("abc  ", StringUtils.stripStart("abc  ", null));
        assertEquals("abc ", StringUtils.stripStart(" abc ", null));
    }

    @Test(timeout = 4000)
    public void testStripEnd() {
        assertNull(StringUtils.stripEnd(null, null));
        assertEquals("", StringUtils.stripEnd("", null));
        assertEquals("abc", StringUtils.stripEnd("abc", ""));
        assertEquals("  abc", StringUtils.stripEnd("  abc", null));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertEquals(" abc", StringUtils.stripEnd(" abc ", null));
    }

    // --- stripAll ---
    @Test(timeout = 4000)
    public void testStripAll() {
        assertNull(StringUtils.stripAll(null));
        assertArrayEquals(new String[0], StringUtils.stripAll());
        assertArrayEquals(new String[]{"abc", "abc"}, StringUtils.stripAll("abc", "  abc"));
        assertArrayEquals(new String[]{"abc", null}, StringUtils.stripAll("abc  ", null));
    }

    // --- equals / equalsIgnoreCase ---
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

    // --- indexOf / lastIndexOf ---
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
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringStartPos() {
        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("a", null, 0));
        assertEquals(0, StringUtils.indexOf("", "", 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", -1));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "", 2));
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
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("a", null));
        assertEquals(0, StringUtils.lastIndexOf("", ""));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
    }

    @Test(timeout = 4000)
    public void testLastOrdinalIndexOf() {
        assertEquals(-1, StringUtils.lastOrdinalIndexOf(null, "a", 1));
        assertEquals(-1, StringUtils.lastOrdinalIndexOf("a", null, 1));
        assertEquals(0, StringUtils.lastOrdinalIndexOf("", "", 1));
        assertEquals(7, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 1));
        assertEquals(6, StringUtils.lastOrdinalIndexOf("aabaabaa", "a", 2));
    }

    // --- contains ---
    @Test(timeout = 4000)
    public void testContainsChar() {
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
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    // --- indexOfAny / containsAny / containsNone / containsOnly ---
    @Test(timeout = 4000)
    public void testIndexOfAnyChars() {
        assertEquals(-1, StringUtils.indexOfAny(null, 'z'));
        assertEquals(-1, StringUtils.indexOfAny("", 'z'));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", 'z', 'a'));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", 'b', 'y'));
        assertEquals(-1, StringUtils.indexOfAny("aba", 'z'));
    }

    @Test(timeout = 4000)
    public void testContainsAnyChars() {
        assertFalse(StringUtils.containsAny(null, 'z'));
        assertFalse(StringUtils.containsAny("", 'z'));
        assertTrue(StringUtils.containsAny("zzabyycdxx", 'z', 'a'));
        assertFalse(StringUtils.containsAny("aba", 'z'));
    }

    @Test(timeout = 4000)
    public void testContainsNoneChars() {
        assertTrue(StringUtils.containsNone(null, 'z'));
        assertTrue(StringUtils.containsNone("", 'z'));
        assertTrue(StringUtils.containsNone("ab", 'z'));
        assertFalse(StringUtils.containsNone("abz", 'z'));
    }

    @Test(timeout = 4000)
    public void testContainsOnlyChars() {
        assertFalse(StringUtils.containsOnly(null, 'a'));
        assertTrue(StringUtils.containsOnly("", 'a'));
        assertFalse(StringUtils.containsOnly("ab", 'a'));
        assertTrue(StringUtils.containsOnly("abab", 'a', 'b'));
    }

    // --- substring/left/right/mid ---
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
        assertEquals("b", StringUtils.substring("abc", -2, -1));
    }

    @Test(timeout = 4000)
    public void testLeft() {
        assertNull(StringUtils.left(null, 0));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));
    }

    @Test(timeout = 4000)
    public void testRight() {
        assertNull(StringUtils.right(null, 0));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));
    }

    @Test(timeout = 4000)
    public void testMid() {
        assertNull(StringUtils.mid(null, 0, 0));
        assertEquals("", StringUtils.mid("abc", -1, -1));
        assertEquals("", StringUtils.mid("", 0, 0));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("c", StringUtils.mid("abc", 2, 4));
        assertEquals("", StringUtils.mid("abc", 4, 2));
    }

    // --- substringBefore/After ---
    @Test(timeout = 4000)
    public void testSubstringBefore() {
        assertNull(StringUtils.substringBefore(null, "a"));
        assertEquals("", StringUtils.substringBefore("", "a"));
        assertEquals("abc", StringUtils.substringBefore("abc", null));
        assertEquals("", StringUtils.substringBefore("abc", "a"));
        assertEquals("a", StringUtils.substringBefore("abcba", "b"));
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
        assertEquals("abc", StringUtils.substringAfter("abc", ""));
    }

    @Test(timeout = 4000)
    public void testSubstringBeforeLast() {
        assertNull(StringUtils.substringBeforeLast(null, "a"));
        assertEquals("", StringUtils.substringBeforeLast("", "a"));
        assertEquals("abc", StringUtils.substringBeforeLast("abc", null));
        assertEquals("abc", StringUtils.substringBeforeLast("abcba", "b"));
        assertEquals("", StringUtils.substringBeforeLast("a", "a"));
    }

    @Test(timeout = 4000)
    public void testSubstringAfterLast() {
        assertNull(StringUtils.substringAfterLast(null, "a"));
        assertEquals("", StringUtils.substringAfterLast("", "a"));
        assertEquals("", StringUtils.substringAfterLast("abc", null));
        assertEquals("bc", StringUtils.substringAfterLast("abc", "a"));
        assertEquals("a", StringUtils.substringAfterLast("abcba", "b"));
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
        assertNull(StringUtils.substringBetween("", "[", "]"));
        assertNull(StringUtils.substringBetween("abc", null, "]"));
        assertNull(StringUtils.substringBetween("abc", "[", null));
        assertEquals("", StringUtils.substringBetween("yabcz", "", ""));
        assertEquals("abc", StringUtils.substringBetween("yabcz", "y", "z"));
    }

    // --- split ---
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

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testJoinNullArray() {
        assertNull(StringUtils.join((Object[]) null));
        assertNull(StringUtils.join((Object[]) null, ','));
        assertNull(StringUtils.join((Object[]) null, ",", 0, 0));
    }

    @Test(timeout = 4000)
    public void testJoinEmptyArray() {
        assertEquals("", StringUtils.join(new Object[0]));
        assertEquals("", StringUtils.join(new Object[0], ','));
        assertEquals("", StringUtils.join(new Object[0], ",", 0, 0));
    }

    @Test(timeout = 4000)
    public void testJoinArrayWithNulls() {
        // Defect-targeted: NPE when array contains null elements
        Object[] arrayWithNull = {null, "a", null, "b"};
        // Expected: ",a,,b" for char separator? Actually skip nulls, but separator after null? 
        // i=0: no separator, null skipped; i=1: append separator, then "a"; i=2: append separator, null skipped; i=3: append separator, "b"
        // Result: ",a,,b" - wait, for i=2, we add separator before checking element, so separator is added even if element is null.
        // So result should be ",a,,b" (comma before each non-first element, nulls skipped but commas remain)
        // Let's compute: 
        //   startIndex=0, endIndex=4
        //   i=0: i>0? false, no separator; array[0] null => skip
        //   i=1: i>0 true, append separator (','), array[1]="a" => append "a" -> ",a"
        //   i=2: i>0 true, append separator -> ",a,", array[2] null => skip
        //   i=3: i>0 true, append separator -> ",a,,", array[3]="b" => append "b" -> ",a,,b"
        // So expected ",a,,b"
        assertEquals(",a,,b", StringUtils.join(arrayWithNull, ','));
        // With String separator:
        assertEquals(",,a,,,b", StringUtils.join(arrayWithNull, ",")); // separator is ",", so result: ",,a,,,b"
    }

    @Test(timeout = 4000)
    public void testJoinArraySingleElement() {
        assertEquals("abc", StringUtils.join(new Object[]{"abc"}, ','));
        assertEquals("abc", StringUtils.join(new Object[]{"abc"}, "!"));
    }

    @Test(timeout = 4000)
    public void testJoinIteratorNullElement() {
        java.util.ArrayList<Object> list = new java.util.ArrayList<>();
        list.add(null);
        list.add("a");
        list.add(null);
        // join with char: expected ",a,"? Let's compute:
        // first = null, then while: append separator ',', append obj "a", then separator ',', then null skip -> ",a,"
        assertEquals(",a,", StringUtils.join(list.iterator(), ','));
    }

    // --- padding extremes ---
    @Test(timeout = 4000)
    public void testRepeatNull() {
        assertNull(StringUtils.repeat(null, 5));
        assertEquals("", StringUtils.repeat("", 0));
        assertEquals("", StringUtils.repeat("a", -2));
        assertEquals("aaa", StringUtils.repeat("a", 3));
    }

    @Test(timeout = 4000)
    public void testLeftPadNegative() {
        assertEquals("bat", StringUtils.leftPad("bat", -1));
        assertEquals("bat", StringUtils.leftPad("bat", 1));
    }

    @Test(timeout = 4000)
    public void testRightPadNegative() {
        assertEquals("bat", StringUtils.rightPad("bat", -1));
        assertEquals("bat", StringUtils.rightPad("bat", 1));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testJoinWithNullElementsAndCharSeparator() {
        // This test targets the known NullPointerException defect in join(Object[], char)
        // The defective version may throw NPE when array elements are null.
        Object[] arr = new Object[]{null, "test", null};
        // Should not throw any exception
        String result = StringUtils.join(arr, ',');
        // Expected: start with no leading separator, then for each non-first element add separator.
        // i=0: no separator, null -> skip
        // i=1: separator ',' then "test" -> ",test"
        // i=2: separator ',' then null -> skip -> ",test,"
        assertEquals(",test,", result);
    }

    @Test(timeout = 4000)
    public void testJoinVarargsNull() {
        // Calling join with a single null argument (varargs)
        String result = StringUtils.join((Object) null);
        // This should return "" because the array contains one null element, which is skipped.
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testJoinObjectArrayWithNullSeparator() {
        Object[] arr = {"a", "b", "c"};
        String result = StringUtils.join(arr, (String) null);
        // With null separator, it's treated as empty string, so concatenation: "abc"
        assertEquals("abc", result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAbbreviateMaxWidthTooSmall() {
        StringUtils.abbreviate("abc", 3);
    }

    @Test(timeout = 4000)
    public void testAbbreviateNormal() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abc...", StringUtils.abbreviate("abcdefg", 6));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLevenshteinDistanceNull() {
        StringUtils.getLevenshteinDistance(null, "abc");
    }

    @Test(timeout = 4000)
    public void testGetLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance("", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("", "a"));
        assertEquals(1, StringUtils.getLevenshteinDistance("frog", "fog"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReplaceEachMismatchedLengths() {
        StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"x", "y"});
    }

    @Test(timeout = 4000)
    public void testReplaceEach() {
        assertEquals("wcte", StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}));
    }

    // ==================== Additional high-coverage methods ====================

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
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "ab"));
    }

    @Test(timeout = 4000)
    public void testIsAlpha() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab2c"));
    }

    @Test(timeout = 4000)
    public void testIsNumeric() {
        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12 3"));
    }

    @Test(timeout = 4000)
    public void testIsWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("  "));
        assertFalse(StringUtils.isWhitespace("abc"));
    }

    @Test(timeout = 4000)
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("bat", StringUtils.defaultString("bat"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
    }

    @Test(timeout = 4000)
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("tab", StringUtils.reverse("bat"));
    }

    @Test(timeout = 4000)
    public void testDeleteWhitespace() {
        assertNull(StringUtils.deleteWhitespace(null));
        assertEquals("", StringUtils.deleteWhitespace(""));
        assertEquals("abc", StringUtils.deleteWhitespace("   ab  c  "));
    }

    @Test(timeout = 4000)
    public void testRemoveStartRemoveEnd() {
        assertEquals("domain.com", StringUtils.removeStart("www.domain.com", "www."));
        assertEquals("www.domain", StringUtils.removeEnd("www.domain.com", ".com"));
    }

    @Test(timeout = 4000)
    public void testReplaceChars() {
        assertEquals("aycya", StringUtils.replaceChars("abcba", 'b', 'y'));
        assertEquals("ac", StringUtils.replaceChars("abc", "b", null));
    }

    @Test(timeout = 4000)
    public void testOverlay() {
        assertNull(StringUtils.overlay(null, "abc", 0, 0));
        assertEquals("abzzzzef", StringUtils.overlay("abcdef", "zzzz", 2, 4));
        assertEquals("zzzzef", StringUtils.overlay("abcdef", "zzzz", -1, 4));
    }

    @Test(timeout = 4000)
    public void testChomp() {
        assertNull(StringUtils.chomp(null));
        assertEquals("", StringUtils.chomp(""));
        assertEquals("abc", StringUtils.chomp("abc\n"));
        assertEquals("abc", StringUtils.chomp("abc\r\n"));
    }

    @Test(timeout = 4000)
    public void testDifference() {
        assertNull(StringUtils.difference(null, "abc"));
        assertNull(StringUtils.difference("abc", null));
        assertEquals("robot", StringUtils.difference("i am a machine", "i am a robot"));
        assertEquals("", StringUtils.difference("abc", "abc"));
    }

    @Test(timeout = 4000)
    public void testIndexOfDifference() {
        assertEquals(-1, StringUtils.indexOfDifference(null, null));
        assertEquals(0, StringUtils.indexOfDifference("", "abc"));
        assertEquals(2, StringUtils.indexOfDifference("abcde", "abxyz"));
    }

    @Test(timeout = 4000)
    public void testGetCommonPrefix() {
        assertEquals("", StringUtils.getCommonPrefix(null));
        assertEquals("", StringUtils.getCommonPrefix(new String[0]));
        assertEquals("abc", StringUtils.getCommonPrefix("abc", "abc"));
        assertEquals("ab", StringUtils.getCommonPrefix("abcde", "abxyz"));
    }

    @Test(timeout = 4000)
    public void testStartsEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
    }

    @Test(timeout = 4000)
    public void testNormalizeSpace() {
        assertNull(StringUtils.normalizeSpace(null));
        assertEquals("abc def", StringUtils.normalizeSpace(" abc   def "));
    }

    @Test(timeout = 4000)
    public void testStripAccents() {
        // This method may throw RuntimeException if no Normalizer available, but we can test null
        assertNull(StringUtils.stripAccents(null));
        // We cannot easily test non-null without proper environment, so just call it.
    }
}