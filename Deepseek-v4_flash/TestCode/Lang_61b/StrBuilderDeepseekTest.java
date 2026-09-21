package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for StrBuilder targeting maximum line/branch coverage
 * and the known defect (Lang294) where indexOf incorrectly searches beyond the
 * logical size of the builder, causing false matches and potential
 * ArrayIndexOutOfBoundsException in subsequent operations.
 *
 * [Branch & Defect Analysis Matrix]
 * - indexOf(String, int): uses buffer.length instead of size for loop bound
 *   -> can match characters in the unused buffer area (e.g., '\0')
 * - deleteImpl: uses size - endIndex as length for arraycopy; if endIndex > size,
 *   length becomes negative -> ArrayIndexOutOfBoundsException
 * - validateRange: clamps endIndex to size, but if startIndex > size, throws
 * - setNullText: sets nullText to null if empty string
 * - appendNull: returns this if nullText == null
 * - appendPadding: only appends if length >= 0
 * - appendFixedWidthPadLeft/Right: handles null object via getNullText()
 * - insert: validates index via validateIndex (0..size inclusive)
 * - deleteAll/deleteFirst: loops over buffer, uses deleteImpl
 * - replaceImpl: handles insertLen != removeLen by shifting
 * - reverse: swaps characters in place
 * - trim: removes leading/trailing chars <= ' '
 * - startsWith/endsWith: null safe, empty string returns true
 * - substring: uses validateRange
 * - leftString/rightString/midString: boundary conditions
 * - contains: uses indexOf
 * - indexOf(char, int): clamps startIndex to 0, returns -1 if >= size
 * - lastIndexOf: clamps startIndex to size-1, returns -1 if < 0
 * - equals/hashCode: compares size and buffer content
 * - asTokenizer/asReader/asWriter: inner class delegation
 */
public class StrBuilderDeepseekTest {

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testConstructorDefault() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacity() {
        StrBuilder sb = new StrBuilder(10);
        assertEquals(10, sb.capacity());
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNegativeCapacity() {
        StrBuilder sb = new StrBuilder(-5);
        assertEquals(32, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testConstructorWithString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(35, sb.capacity()); // 3 + 32
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullString() {
        StrBuilder sb = new StrBuilder(null);
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testGetSetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.setNewLineText("\n");
        assertEquals("\n", sb.getNewLineText());
        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());
    }

    @Test(timeout = 4000)
    public void testGetSetNullText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
        sb.setNullText(null);
        assertNull(sb.getNullText());
        // Setting empty string should set to null
        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testLengthAndSize() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(5, sb.length());
        assertEquals(5, sb.size());
        sb.append(" world");
        assertEquals(11, sb.length());
    }

    @Test(timeout = 4000)
    public void testSetLength() {
        StrBuilder sb = new StrBuilder("hello");
        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("hel", sb.toString());
        sb.setLength(10);
        assertEquals(10, sb.length());
        // The extra characters should be '\0'
        assertEquals("hel\0\0\0\0\0\0\0", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testSetLengthNegative() {
        StrBuilder sb = new StrBuilder();
        sb.setLength(-1);
    }

    @Test(timeout = 4000)
    public void testCapacityAndEnsureCapacity() {
        StrBuilder sb = new StrBuilder(5);
        assertEquals(5, sb.capacity());
        sb.ensureCapacity(10);
        assertEquals(10, sb.capacity());
        sb.ensureCapacity(5); // no change
        assertEquals(10, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder(100);
        sb.append("short");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testClear() {
        StrBuilder sb = new StrBuilder("hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
        // capacity unchanged
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals('a', sb.charAt(0));
        assertEquals('c', sb.charAt(2));
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegativeIndex() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(-1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testCharAtBeyondLength() {
        StrBuilder sb = new StrBuilder("abc");
        sb.charAt(3);
    }

    @Test(timeout = 4000)
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setCharAt(1, 'X');
        assertEquals("aXc", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtInvalid() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setCharAt(3, 'X');
    }

    @Test(timeout = 4000)
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abcde");
        sb.deleteCharAt(2);
        assertEquals("abde", sb.toString());
        assertEquals(4, sb.length());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtInvalid() {
        StrBuilder sb = new StrBuilder("abc");
        sb.deleteCharAt(3);
    }

    @Test(timeout = 4000)
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder("hello");
        char[] arr = sb.toCharArray();
        assertArrayEquals(new char[]{'h','e','l','l','o'}, arr);
    }

    @Test(timeout = 4000)
    public void testToCharArrayEmpty() {
        StrBuilder sb = new StrBuilder();
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray());
    }

    @Test(timeout = 4000)
    public void testToCharArrayRange() {
        StrBuilder sb = new StrBuilder("hello");
        char[] arr = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'e','l','l'}, arr);
    }

    @Test(timeout = 4000)
    public void testToCharArrayRangeEmpty() {
        StrBuilder sb = new StrBuilder("hello");
        char[] arr = sb.toCharArray(2, 2);
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, arr);
    }

    @Test(timeout = 4000)
    public void testGetCharsToNewArray() {
        StrBuilder sb = new StrBuilder("abc");
        char[] dest = sb.getChars(null);
        assertArrayEquals(new char[]{'a','b','c'}, dest);
    }

    @Test(timeout = 4000)
    public void testGetCharsToExistingArray() {
        StrBuilder sb = new StrBuilder("abc");
        char[] dest = new char[5];
        char[] result = sb.getChars(dest);
        assertSame(dest, result);
        assertArrayEquals(new char[]{'a','b','c', 0, 0}, dest);
    }

    @Test(timeout = 4000)
    public void testGetCharsWithIndexes() {
        StrBuilder sb = new StrBuilder("hello");
        char[] dest = new char[3];
        sb.getChars(1, 4, dest, 0);
        assertArrayEquals(new char[]{'e','l','l'}, dest);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidStart() {
        StrBuilder sb = new StrBuilder("hello");
        sb.getChars(-1, 2, new char[2], 0);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsInvalidEnd() {
        StrBuilder sb = new StrBuilder("hello");
        sb.getChars(0, 6, new char[6], 0);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testGetCharsStartAfterEnd() {
        StrBuilder sb = new StrBuilder("hello");
        sb.getChars(3, 2, new char[2], 0);
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testAppendNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.append((String) null);
        assertEquals("NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullWithNullTextNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((String) null);
        assertEquals("", sb.toString()); // nullText is null, so nothing appended
    }

    @Test(timeout = 4000)
    public void testAppendObject() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) "test");
        assertEquals("test", sb.toString());
        sb.append((Object) null);
        assertEquals("test", sb.toString()); // nullText null, no append
    }

    @Test(timeout = 4000)
    public void testAppendString() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello");
        assertEquals("hello", sb.toString());
        sb.append(" world");
        assertEquals("hello world", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringEmpty() {
        StrBuilder sb = new StrBuilder("a");
        sb.append("");
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringSubstring() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello", 1, 3);
        assertEquals("ell", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubstringInvalidStart() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello", -1, 3);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubstringInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("hello", 1, 10);
    }

    @Test(timeout = 4000)
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("test"));
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilder() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("def");
        sb1.append(sb2);
        assertEquals("abcdef", sb1.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a','b','c'});
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArraySubset() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a','b','c','d'}, 1, 2);
        assertEquals("bc", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubsetInvalidStart() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a','b'}, -1, 1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubsetInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a','b'}, 0, 5);
    }

    @Test(timeout = 4000)
    public void testAppendBoolean() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());
        sb.clear();
        sb.append(false);
        assertEquals("false", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendChar() {
        StrBuilder sb = new StrBuilder();
        sb.append('X');
        assertEquals("X", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendInt() {
        StrBuilder sb = new StrBuilder();
        sb.append(42);
        assertEquals("42", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendLong() {
        StrBuilder sb = new StrBuilder();
        sb.append(123456789L);
        assertEquals("123456789", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(3.14f);
        assertEquals("3.14", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.71828);
        assertEquals("2.71828", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNewLineDefault() {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine();
        assertEquals(SystemUtils.LINE_SEPARATOR, sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNewLineCustom() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\r\n");
        sb.appendNewLine();
        assertEquals("\r\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(5, '*');
        assertEquals("*****", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingNegative() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendPadding(-1, 'x');
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("ab", 5, '0');
        assertEquals("000ab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftTruncate() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abcdef", 3, '0');
        assertEquals("def", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.appendFixedWidthPadLeft((Object) null, 6, '*');
        assertEquals("**NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(123, 5, '0');
        assertEquals("00123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("ab", 5, '0');
        assertEquals("ab000", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightTruncate() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abcdef", 3, '0');
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.appendFixedWidthPadRight((Object) null, 6, '*');
        assertEquals("NULL**", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(123, 5, '0');
        assertEquals("12300", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArrayNullSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b"}, null);
        assertEquals("ab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArrayEmpty() {
        StrBuilder sb = new StrBuilder("x");
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals("x", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsCollection() {
        StrBuilder sb = new StrBuilder();
        java.util.Collection<String> coll = java.util.Arrays.asList("x", "y");
        sb.appendWithSeparators(coll, "-");
        assertEquals("x-y", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsIterator() {
        StrBuilder sb = new StrBuilder();
        java.util.Iterator<String> it = java.util.Arrays.asList("1", "2").iterator();
        sb.appendWithSeparators(it, ":");
        assertEquals("1:2", sb.toString());
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone (Lang294)
    // ========================================================================

    /**
     * Directly targets the known defect: indexOf(String, int) uses buffer.length
     * instead of size for the loop bound, causing it to find matches in the
     * unused buffer area (filled with '\0').
     */
    @Test(timeout = 4000)
    public void testIndexOfLang294_ShouldReturnMinusOne() {
        StrBuilder sb = new StrBuilder("hello");
        // The string "\0" is not logically present; should return -1.
        // Buggy version returns 5 (the index of the first '\0' after "hello").
        assertEquals(-1, sb.indexOf("\0", 0));
    }

    /**
     * Reproduces the ArrayIndexOutOfBoundsException that occurs when using the
     * wrong index from indexOf in a subsequent delete operation.
     */
    @Test(timeout = 4000)
    public void testLang294_DeleteAfterFalseMatch() {
        StrBuilder sb = new StrBuilder("hello");
        // On buggy version, indexOf returns 5 (pointing to '\0' in buffer).
        int idx = sb.indexOf("\0", 0);
        // If idx is -1 (correct), delete is a no-op.
        // If idx is 5 (buggy), deleteImpl will compute a negative length -> exception.
        sb.delete(idx, idx + 1);
        // If we reach here, no exception occurred (correct behavior).
        assertEquals("hello", sb.toString());
    }

    /**
     * Additional test: indexOf with a longer string of null chars.
     */
    @Test(timeout = 4000)
    public void testIndexOfLang294_MultipleNulls() {
        StrBuilder sb = new StrBuilder("abc");
        // Search for two null characters; should return -1.
        assertEquals(-1, sb.indexOf("\0\0", 0));
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndex() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(4, "x");
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(-1, "x");
    }

    @Test(timeout = 4000)
    public void testInsertString() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, "b");
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertNullString() {
        StrBuilder sb = new StrBuilder("a");
        sb.setNullText("NULL");
        sb.insert(1, (String) null);
        assertEquals("aNULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArray() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, new char[]{'b'});
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArraySubset() {
        StrBuilder sb = new StrBuilder("ad");
        sb.insert(1, new char[]{'b','c','x'}, 0, 2);
        assertEquals("abcd", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubsetInvalidOffset() {
        StrBuilder sb = new StrBuilder("a");
        sb.insert(0, new char[]{'b'}, -1, 1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubsetInvalidLength() {
        StrBuilder sb = new StrBuilder("a");
        sb.insert(0, new char[]{'b'}, 0, 5);
    }

    @Test(timeout = 4000)
    public void testInsertBoolean() {
        StrBuilder sb = new StrBuilder("a");
        sb.insert(1, true);
        assertEquals("atrue", sb.toString());
        sb = new StrBuilder("a");
        sb.insert(1, false);
        assertEquals("afalse", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertChar() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertInt() {
        StrBuilder sb = new StrBuilder("a");
        sb.insert(1, 42);
        assertEquals("a42", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteRange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(1, 4);
        assertEquals("ho", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteRangeEndBeyondSize() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(2, 10);
        assertEquals("he", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteRangeInvalidStart() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(-1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteRangeStartAfterEnd() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(4, 2);
    }

    @Test(timeout = 4000)
    public void testDeleteAllChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteAll('a');
        assertEquals("bcd", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.deleteFirst('a');
        assertEquals("bacad", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllString() {
        StrBuilder sb = new StrBuilder("xabcyabcz");
        sb.deleteAll("abc");
        assertEquals("xyz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstString() {
        StrBuilder sb = new StrBuilder("xabcyabcz");
        sb.deleteFirst("abc");
        assertEquals("xyabcz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllStringNull() {
        StrBuilder sb = new StrBuilder("test");
        sb.deleteAll((String) null);
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceRange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.replace(1, 4, "i");
        assertEquals("hio", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceRangeWithNull() {
        StrBuilder sb = new StrBuilder("hello");
        sb.replace(1, 4, null);
        assertEquals("ho", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.replaceAll('a', 'X');
        assertEquals("XbXcXd", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.replaceFirst('a', 'X');
        assertEquals("Xbacad", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllString() {
        StrBuilder sb = new StrBuilder("xabcyabcz");
        sb.replaceAll("abc", "123");
        assertEquals("x123y123z", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstString() {
        StrBuilder sb = new StrBuilder("xabcyabcz");
        sb.replaceFirst("abc", "123");
        assertEquals("x123yabcz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllStringNullSearch() {
        StrBuilder sb = new StrBuilder("test");
        sb.replaceAll((String) null, "x");
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReverse() {
        StrBuilder sb = new StrBuilder("abc");
        sb.reverse();
        assertEquals("cba", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReverseEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrim() {
        StrBuilder sb = new StrBuilder("  hello  ");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimAllSpaces() {
        StrBuilder sb = new StrBuilder("   ");
        sb.trim();
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimNoChange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testStartsWith() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.startsWith("he"));
        assertFalse(sb.startsWith("lo"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("hello!"));
    }

    @Test(timeout = 4000)
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.endsWith("lo"));
        assertFalse(sb.endsWith("he"));
        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("!hello"));
    }

    @Test(timeout = 4000)
    public void testSubstring() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ell", sb.substring(1, 4));
        assertEquals("hello", sb.substring(0));
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testSubstringInvalidStart() {
        StrBuilder sb = new StrBuilder("hello");
        sb.substring(-1);
    }

    @Test(timeout = 4000)
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hel", sb.leftString(3));
        assertEquals("hello", sb.leftString(10));
        assertEquals("", sb.leftString(-1));
    }

    @Test(timeout = 4000)
    public void testRightString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("llo", sb.rightString(3));
        assertEquals("hello", sb.rightString(10));
        assertEquals("", sb.rightString(-1));
    }

    @Test(timeout = 4000)
    public void testMidString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ell", sb.midString(1, 3));
        assertEquals("hello", sb.midString(0, 10));
        assertEquals("", sb.midString(-1, 3));
        assertEquals("", sb.midString(5, 3));
        assertEquals("", sb.midString(0, -1));
    }

    @Test(timeout = 4000)
    public void testContainsChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.contains('l'));
        assertFalse(sb.contains('z'));
    }

    @Test(timeout = 4000)
    public void testContainsString() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.contains("ell"));
        assertFalse(sb.contains("xyz"));
    }

    @Test(timeout = 4000)
    public void testIndexOfChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(2, sb.indexOf('l'));
        assertEquals(-1, sb.indexOf('z'));
    }

    @Test(timeout = 4000)
    public void testIndexOfCharWithStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(3, sb.indexOf('l', 3));
        assertEquals(-1, sb.indexOf('l', 4));
        assertEquals(0, sb.indexOf('h', -1));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(1, sb.indexOf("ell"));
        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf((String) null));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringWithStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(1, sb.indexOf("ell", 0));
        assertEquals(-1, sb.indexOf("ell", 2));
        // Single character optimization
        assertEquals(2, sb.indexOf("l", 0));
        // Empty string returns startIndex
        assertEquals(3, sb.indexOf("", 3));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(3, sb.lastIndexOf('l'));
        assertEquals(-1, sb.lastIndexOf('z'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfCharWithStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(2, sb.lastIndexOf('l', 2));
        assertEquals(-1, sb.lastIndexOf('l', 1));
        assertEquals(-1, sb.lastIndexOf('h', -1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(1, sb.lastIndexOf("ell"));
        assertEquals(-1, sb.lastIndexOf("xyz"));
        assertEquals(-1, sb.lastIndexOf((String) null));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfStringWithStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(1, sb.lastIndexOf("ell", 4));
        assertEquals(-1, sb.lastIndexOf("ell", 1));
        // Single character optimization
        assertEquals(3, sb.lastIndexOf("l", 4));
        // Empty string returns startIndex
        assertEquals(2, sb.lastIndexOf("", 2));
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testEquals() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("abd");
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(null));
        assertFalse(sb1.equals("abc"));
    }

    @Test(timeout = 4000)
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("ABC");
        StrBuilder sb3 = new StrBuilder("abd");
        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertFalse(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(null));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder("test");
        StringBuffer buf = sb.toStringBuffer();
        assertEquals("test", buf.toString());
    }

    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tokenizer = sb.asTokenizer();
        String[] tokens = tokenizer.getTokenArray();
        assertArrayEquals(new String[]{"a", "b", "c"}, tokens);
    }

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("hello");
        Reader reader = sb.asReader();
        char[] buf = new char[5];
        int len = reader.read(buf);
        assertEquals(5, len);
        assertEquals("hello", new String(buf));
        assertEquals(-1, reader.read());
    }

    @Test(timeout = 4000)
    public void testAsWriter() {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();
        try {
            writer.write("hello");
            writer.flush();
            assertEquals("hello", sb.toString());
            writer.write(' ');
            writer.write("world".toCharArray());
            assertEquals("hello world", sb.toString());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    @Test(timeout = 4000)
    public void testClone() {
        StrBuilder sb = new StrBuilder("clone");
        StrBuilder cloned = (StrBuilder) sb.clone();
        assertNotSame(sb, cloned);
        assertEquals(sb.toString(), cloned.toString());
        cloned.append(" test");
        assertFalse(sb.toString().equals(cloned.toString()));
    }
}