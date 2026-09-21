package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StrBuilderDeepseekTest - Advanced White-Box Test Suite for StrBuilder.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - append(String), append(Object), appendNull, appendNewLine
 *   - insert(int, String), insert(int, Object)
 *   - delete(int, int), deleteCharAt(int)
 *   - replace(int, int, String), replaceAll/replaceFirst (char/String)
 *   - substring, leftString, rightString, midString
 *   - startsWith, endsWith, contains, indexOf, lastIndexOf
 *   - reverse, trim, clear, setLength, ensureCapacity, minimizeCapacity
 *   - equals, hashCode, toString, toCharArray, getChars
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - null arguments for strings, arrays, collections, iterators
 *   - empty strings, zero-length arrays
 *   - negative indices, indices at size boundaries (0, size-1, size)
 *   - capacity boundaries (initial capacity 0, negative, large)
 *   - appendPadding with negative length, zero length
 *   - appendFixedWidthPadLeft/Right with width <= 0, width > str length
 *   - deleteAll/deleteFirst with empty/null strings
 *   - replaceAll/replaceFirst with null search/replace strings
 *   - indexOf/lastIndexOf with null strings, startIndex at boundaries
 *   - substring with endIndex > size, startIndex > endIndex
 *   - leftString/rightString with negative length, length >= size
 *   - midString with negative index, negative length, index >= size
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Defect D4J-299: ArrayIndexOutOfBoundsException in appendFixedWidthPadRight
 *     when obj is null and nullText is set to a non-null string.
 *     The bug occurs because getNullText() returns the nullText, but the code
 *     does not handle the case where the nullText length is greater than width.
 *     Specifically, when strLen >= width, it copies strLen characters into buffer
 *     starting at buffer[size], but only width characters should be copied.
 *     This causes an ArrayIndexOutOfBoundsException.
 *   - Additional defect: appendFixedWidthPadLeft has the same issue.
 *   - Also, appendFixedWidthPadRight when strLen < width, the padding loop
 *     uses buffer[size + strLen + i] which is correct, but the str.getChars
 *     copies strLen characters starting at buffer[size], which is correct.
 *     However, if strLen > width, it copies strLen characters into buffer[size],
 *     which overflows the buffer.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - charAt, setCharAt, deleteCharAt with invalid indices
 *   - insert with invalid index (negative, > size)
 *   - delete with invalid startIndex, startIndex > endIndex
 *   - substring with invalid startIndex
 *   - setLength with negative length
 *   - getChars(int, int, char[], int) with invalid indices
 *   - append(String, int, int) with invalid startIndex/length
 *   - append(char[], int, int) with invalid startIndex/length
 *   - insert(int, char[], int, int) with invalid offset/length
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals with same object, different size, same content
 *   - hashCode consistency with equals
 *   - toString returns correct string
 *   - toCharArray returns copy of internal buffer
 *   - clone (not implemented, but class implements Cloneable)
 */
public class StrBuilderDeepseekTest {

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testAppendString() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        assertEquals("Hello", sb.toString());
        assertEquals(5, sb.length());
    }

    @Test(timeout = 4000)
    public void testAppendObject() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) "World");
        assertEquals("World", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullWithNullText() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.append((String) null);
        assertEquals("NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullWithoutNullText() {
        StrBuilder sb = new StrBuilder();
        sb.append((String) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNewLine() {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine();
        assertTrue(sb.length() > 0);
        // SystemUtils.LINE_SEPARATOR is platform-dependent
        assertEquals(SystemUtils.LINE_SEPARATOR, sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNewLineCustom() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");
        sb.appendNewLine();
        assertEquals("\n", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, "xx");
        assertEquals("Hexxllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertObject() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, (Object) "World");
        assertEquals("HelloWorld", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertNullWithNullText() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setNullText("NULL");
        sb.insert(1, (String) null);
        assertEquals("aNULLbc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteRange() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        sb.delete(3, 8);
        assertEquals("Helrld", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteCharAt(1);
        assertEquals("Hllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceRange() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        sb.replace(3, 8, "ABC");
        assertEquals("HelABCld", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.replaceAll('a', 'x');
        assertEquals("xbxcxd", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstChar() {
        StrBuilder sb = new StrBuilder("abacad");
        sb.replaceFirst('a', 'x');
        assertEquals("xbacad", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllString() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceAll("ab", "xy");
        assertEquals("xyxyxy", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstString() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceFirst("ab", "xy");
        assertEquals("xyabab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSubstring() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals("ello", sb.substring(1, 5));
    }

    @Test(timeout = 4000)
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals("Hello", sb.leftString(5));
        assertEquals("HelloWorld", sb.leftString(20));
        assertEquals("", sb.leftString(-1));
    }

    @Test(timeout = 4000)
    public void testRightString() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals("World", sb.rightString(5));
        assertEquals("HelloWorld", sb.rightString(20));
        assertEquals("", sb.rightString(-1));
    }

    @Test(timeout = 4000)
    public void testMidString() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals("lloW", sb.midString(2, 4));
        assertEquals("HelloWorld", sb.midString(-5, 20));
        assertEquals("", sb.midString(0, -1));
        assertEquals("", sb.midString(20, 5));
    }

    @Test(timeout = 4000)
    public void testStartsWith() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertTrue(sb.startsWith("Hello"));
        assertFalse(sb.startsWith("World"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
    }

    @Test(timeout = 4000)
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertTrue(sb.endsWith("World"));
        assertFalse(sb.endsWith("Hello"));
        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
    }

    @Test(timeout = 4000)
    public void testContainsChar() {
        StrBuilder sb = new StrBuilder("Hello");
        assertTrue(sb.contains('l'));
        assertFalse(sb.contains('z'));
    }

    @Test(timeout = 4000)
    public void testContainsString() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertTrue(sb.contains("World"));
        assertFalse(sb.contains("xyz"));
    }

    @Test(timeout = 4000)
    public void testIndexOfChar() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(2, sb.indexOf('l'));
        assertEquals(3, sb.indexOf('l', 3));
        assertEquals(-1, sb.indexOf('z'));
        assertEquals(-1, sb.indexOf('l', 10));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals(5, sb.indexOf("World"));
        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf(null));
        assertEquals(0, sb.indexOf("", 0));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(3, sb.lastIndexOf('l'));
        assertEquals(2, sb.lastIndexOf('l', 2));
        assertEquals(-1, sb.lastIndexOf('z'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("HelloWorldHello");
        assertEquals(10, sb.lastIndexOf("Hello"));
        assertEquals(-1, sb.lastIndexOf("xyz"));
        assertEquals(-1, sb.lastIndexOf(null));
    }

    @Test(timeout = 4000)
    public void testReverse() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.reverse();
        assertEquals("olleH", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReverseEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrim() {
        StrBuilder sb = new StrBuilder("  Hello World  ");
        sb.trim();
        assertEquals("Hello World", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.trim();
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testClear() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSetLength() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        sb.setLength(5);
        assertEquals("Hello", sb.toString());
        sb.setLength(10);
        assertEquals("Hello\0\0\0\0\0", sb.toString());
    }

    @Test(timeout = 4000)
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder(10);
        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);
    }

    @Test(timeout = 4000)
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder(100);
        sb.append("Hello");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testEquals() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("World");
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(null));
        assertFalse(sb1.equals("Hello"));
    }

    @Test(timeout = 4000)
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("HELLO");
        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertFalse(sb1.equalsIgnoreCase(new StrBuilder("World")));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] chars = sb.toCharArray();
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, chars);
    }

    @Test(timeout = 4000)
    public void testToCharArrayEmpty() {
        StrBuilder sb = new StrBuilder();
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, sb.toCharArray());
    }

    @Test(timeout = 4000)
    public void testGetChars() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] dest = new char[5];
        sb.getChars(dest);
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, dest);
    }

    @Test(timeout = 4000)
    public void testGetCharsWithIndices() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        char[] dest = new char[5];
        sb.getChars(1, 6, dest, 0);
        assertArrayEquals(new char[]{'e', 'l', 'l', 'o', 'W'}, dest);
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullString() {
        StrBuilder sb = new StrBuilder((String) null);
        assertEquals("", sb.toString());
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNegativeCapacity() {
        StrBuilder sb = new StrBuilder(-10);
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testConstructorWithZeroCapacity() {
        StrBuilder sb = new StrBuilder(0);
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testAppendEmptyString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.append("");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullTextEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("");
        sb.append((String) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingNegative() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.appendPadding(-5, '*');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.appendPadding(0, '*');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingPositive() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.appendPadding(3, '*');
        assertEquals("Hello***", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftWidthZero() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("Hello", 0, '*');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftWidthNegative() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("Hello", -1, '*');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftStringShorter() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("Hello", 8, '*');
        assertEquals("***Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftStringLonger() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("HelloWorld", 5, '*');
        assertEquals("World", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightWidthZero() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("Hello", 0, '*');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightWidthNegative() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("Hello", -1, '*');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightStringShorter() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("Hello", 8, '*');
        assertEquals("Hello***", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightStringLonger() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("HelloWorld", 5, '*');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsNullArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsEmptyArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsNullSeparator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b"}, null);
        assertEquals("ab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsNullCollection() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Collection) null, ",");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsNullIterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Iterator) null, ",");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllCharNotFound() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteAll('z');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllStringNotFound() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteAll("xyz");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllNullString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteAll((String) null);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstCharNotFound() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteFirst('z');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstStringNotFound() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteFirst("xyz");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllNullSearchString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceAll((String) null, "World");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllNullReplaceString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceAll("l", null);
        assertEquals("Heo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstNullSearchString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceFirst((String) null, "World");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstNullReplaceString() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceFirst("l", null);
        assertEquals("Helo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testIndexOfStringWithStartIndexBoundary() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(-1, sb.indexOf("l", 10));
        assertEquals(2, sb.indexOf("l", -5));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfStringWithStartIndexBoundary() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(-1, sb.lastIndexOf("l", -1));
        assertEquals(3, sb.lastIndexOf("l", 10));
    }

    @Test(timeout = 4000)
    public void testSubstringEndIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.substring(0, 100));
    }

    @Test(timeout = 4000)
    public void testLeftStringLengthGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.leftString(100));
    }

    @Test(timeout = 4000)
    public void testRightStringLengthGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.rightString(100));
    }

    @Test(timeout = 4000)
    public void testMidStringIndexNegative() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("Hello", sb.midString(-5, 10));
    }

    @Test(timeout = 4000)
    public void testMidStringIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("", sb.midString(10, 5));
    }

    @Test(timeout = 4000)
    public void testMidStringLengthNegative() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals("", sb.midString(0, -1));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ========================================================================

    /**
     * Defect D4J-299: ArrayIndexOutOfBoundsException in appendFixedWidthPadRight
     * when obj is null and nullText is set to a non-null string with length > width.
     * 
     * The bug: When strLen >= width, the code does str.getChars(strLen - width, strLen, buffer, size)
     * but if str is null, it uses getNullText() which returns the nullText string.
     * If nullText length is greater than width, strLen >= width is true, and it copies
     * strLen characters into buffer starting at buffer[size], but only width characters
     * should be copied. This causes an ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightNullObjectWithNullTextLongerThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULLTEXT");
        // nullText = "NULLTEXT" (length 8), width = 5
        // strLen = 8 >= 5, so it tries to copy 8 characters into buffer starting at size
        // but only 5 characters should be copied -> ArrayIndexOutOfBoundsException
        sb.appendFixedWidthPadRight(null, 5, '*');
        // Expected: "NULLT" (first 5 characters of nullText)
        assertEquals("NULLT", sb.toString());
    }

    /**
     * Defect D4J-299: Same issue in appendFixedWidthPadLeft.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftNullObjectWithNullTextLongerThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULLTEXT");
        // nullText = "NULLTEXT" (length 8), width = 5
        // strLen = 8 >= 5, so it tries to copy 8 characters into buffer starting at size
        // but only 5 characters should be copied -> ArrayIndexOutOfBoundsException
        sb.appendFixedWidthPadLeft(null, 5, '*');
        // Expected: "XTEXT" (last 5 characters of nullText)
        assertEquals("XTEXT", sb.toString());
    }

    /**
     * Additional test: appendFixedWidthPadRight with null object and nullText shorter than width.
     * This should work correctly.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightNullObjectWithNullTextShorterThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NUL");
        sb.appendFixedWidthPadRight(null, 6, '*');
        assertEquals("NUL***", sb.toString());
    }

    /**
     * Additional test: appendFixedWidthPadLeft with null object and nullText shorter than width.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftNullObjectWithNullTextShorterThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NUL");
        sb.appendFixedWidthPadLeft(null, 6, '*');
        assertEquals("***NUL", sb.toString());
    }

    /**
     * Additional test: appendFixedWidthPadRight with null object and nullText exactly equal to width.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightNullObjectWithNullTextEqualToWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("HELLO");
        sb.appendFixedWidthPadRight(null, 5, '*');
        assertEquals("HELLO", sb.toString());
    }

    /**
     * Additional test: appendFixedWidthPadLeft with null object and nullText exactly equal to width.
     */
    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftNullObjectWithNullTextEqualToWidth() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("HELLO");
        sb.appendFixedWidthPadLeft(null, 5, '*');
        assertEquals("HELLO", sb.toString());
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtNegativeIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtIndexEqualToSize() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.charAt(5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtNegativeIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.setCharAt(-1, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtNegativeIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtIndexEqualToSize() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteCharAt(5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertNegativeIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(6, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteNegativeStartIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.delete(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteStartIndexGreaterThanEndIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.delete(3, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSubstringNegativeStartIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.substring(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetLengthNegative() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.setLength(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsNegativeStartIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.getChars(-1, 3, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.getChars(0, 10, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsStartIndexGreaterThanEndIndex() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.getChars(3, 1, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidStartIndex() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello", -1, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello", 0, 10);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidStartIndex() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c'}, -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c'}, 0, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidOffset() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(0, new char[]{'a', 'b', 'c'}, -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidLength() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(0, new char[]{'a', 'b', 'c'}, 0, 5);
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        StrBuilder sb = new StrBuilder("Hello");
        assertTrue(sb.equals(sb));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentSize() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("World");
        assertFalse(sb1.equals(sb2));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        StrBuilder sb = new StrBuilder("Hello");
        assertFalse(sb.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsNonStrBuilder() {
        StrBuilder sb = new StrBuilder("Hello");
        assertFalse(sb.equals("Hello"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        StrBuilder sb = new StrBuilder("Hello");
        int hash1 = sb.hashCode();
        sb.append("World");
        int hash2 = sb.hashCode();
        assertNotEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder("Hello");
        StringBuffer buf = sb.toStringBuffer();
        assertEquals("Hello", buf.toString());
    }

    @Test(timeout = 4000)
    public void testCloneable() {
        assertTrue(new StrBuilder() instanceof Cloneable);
    }

    @Test(timeout = 4000)
    public void testSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(5, sb.size());
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        StrBuilder sb = new StrBuilder();
        assertTrue(sb.isEmpty());
        sb.append("Hello");
        assertFalse(sb.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCapacity() {
        StrBuilder sb = new StrBuilder(100);
        assertTrue(sb.capacity() >= 100);
    }

    @Test(timeout = 4000)
    public void testGetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.setNewLineText("\n");
        assertEquals("\n", sb.getNewLineText());
    }

    @Test(timeout = 4000)
    public void testGetNullText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNullText());
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testSetNullTextEmptyString() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testAppendBooleanTrue() {
        StrBuilder sb = new StrBuilder();
        sb.append(true);
        assertEquals("true", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendBooleanFalse() {
        StrBuilder sb = new StrBuilder();
        sb.append(false);
        assertEquals("false", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendChar() {
        StrBuilder sb = new StrBuilder();
        sb.append('A');
        assertEquals("A", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendInt() {
        StrBuilder sb = new StrBuilder();
        sb.append(123);
        assertEquals("123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendLong() {
        StrBuilder sb = new StrBuilder();
        sb.append(123L);
        assertEquals("123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(1.5f);
        assertEquals("1.5", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.5);
        assertEquals("2.5", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("Hello"));
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringBufferNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((StringBuffer) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilder() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StrBuilder("Hello"));
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilderNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((StrBuilder) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'H', 'e', 'l', 'l', 'o'});
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((char[]) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayWithOffset() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'H', 'e', 'l', 'l', 'o'}, 1, 3);
        assertEquals("ell", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertBooleanTrue() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, true);
        assertEquals("Hellotrue", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertBooleanFalse() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, false);
        assertEquals("Hellofalse", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertChar() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, 'X');
        assertEquals("HeXllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertInt() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, 123);
        assertEquals("Hello123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertLong() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, 123L);
        assertEquals("Hello123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertFloat() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, 1.5f);
        assertEquals("Hello1.5", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertDouble() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(5, 2.5);
        assertEquals("Hello2.5", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArray() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, new char[]{'X', 'Y', 'Z'});
        assertEquals("HeXYZllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArrayNull() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.setNullText("NULL");
        sb.insert(2, (char[]) null);
        assertEquals("HeNULLllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArrayWithOffset() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, new char[]{'X', 'Y', 'Z'}, 1, 2);
        assertEquals("HeYZllo", sb.toString());
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
        StrBuilder sb = new StrBuilder("ababab");
        sb.deleteAll("ab");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstString() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.deleteFirst("ab");
        assertEquals("abab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllCharSameChar() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceAll('l', 'l');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstCharSameChar() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceFirst('l', 'l');
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(123, 5, '0');
        assertEquals("00123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(123, 5, '0');
        assertEquals("12300", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("Hello");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[5];
        int read = reader.read(buf);
        assertEquals(5, read);
        assertEquals("Hello", new String(buf));
    }

    @Test(timeout = 4000)
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        java.io.Writer writer = sb.asWriter();
        writer.write("Hello");
        writer.flush();
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tokenizer = sb.asTokenizer();
        String[] tokens = tokenizer.getTokenArray();
        assertArrayEquals(new String[]{"a", "b", "c"}, tokens);
    }

    @Test(timeout = 4000)
    public void testValidateRangeEndIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        // validateRange is protected, but we can test via substring
        assertEquals("Hello", sb.substring(0, 100));
    }

    @Test(timeout = 4000)
    public void testValidateIndexAtSize() {
        StrBuilder sb = new StrBuilder("Hello");
        // validateIndex allows index == size for insert operations
        sb.insert(5, "World");
        assertEquals("HelloWorld", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.append("", 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringBufferWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.append(new StringBuffer(""), 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilderWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.append(new StrBuilder(""), 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.append(new char[0], 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, "");
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArrayWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, new char[0], 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.delete(2, 2);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceWithLengthZero() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replace(2, 2, "XYZ");
        assertEquals("HeXYZllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceNullReplaceStr() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replace(1, 4, null);
        assertEquals("Ho", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllStringWithEmptyReplace() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceAll("ab", "");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstStringWithEmptyReplace() {
        StrBuilder sb = new StrBuilder("ababab");
        sb.replaceFirst("ab", "");
        assertEquals("abab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testIndexOfStringWithEmptyString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(0, sb.indexOf("", 0));
        assertEquals(3, sb.indexOf("", 3));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfStringWithEmptyString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(4, sb.lastIndexOf("", 4));
        assertEquals(0, sb.lastIndexOf("", 0));
    }

    @Test(timeout = 4000)
    public void testIndexOfCharWithStartIndexNegative() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(2, sb.indexOf('l', -5));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfCharWithStartIndexGreaterThanSize() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(3, sb.lastIndexOf('l', 100));
    }

    @Test(timeout = 4000)
    public void testIndexOfMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('H')));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('z')));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('l')));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('z')));
    }

    @Test(timeout = 4000)
    public void testContainsMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        assertTrue(sb.contains(StrMatcher.charMatcher('l')));
        assertFalse(sb.contains(StrMatcher.charMatcher('z')));
    }

    @Test(timeout = 4000)
    public void testDeleteAllMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteAll(StrMatcher.charMatcher('l'));
        assertEquals("Heo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.deleteFirst(StrMatcher.charMatcher('l'));
        assertEquals("Helo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceAll(StrMatcher.charMatcher('l'), "LL");
        assertEquals("HeLLLo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replaceFirst(StrMatcher.charMatcher('l'), "LL");
        assertEquals("HeLLlo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceMatcherWithNullMatcher() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replace(null, "XYZ", 0, sb.length(), -1);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceMatcherWithReplaceCount() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.replace(StrMatcher.charMatcher('l'), "LL", 0, sb.length(), 1);
        assertEquals("HeLLlo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testGetCharsWithNullDestination() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] result = sb.getChars(null);
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, result);
    }

    @Test(timeout = 4000)
    public void testGetCharsWithSmallDestination() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] dest = new char[3];
        char[] result = sb.getChars(dest);
        assertArrayEquals(new char[]{'H', 'e', 'l', 'l', 'o'}, result);
        assertNotSame(dest, result);
    }

    @Test(timeout = 4000)
    public void testToCharArrayWithRange() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        char[] chars = sb.toCharArray(1, 5);
        assertArrayEquals(new char[]{'e', 'l', 'l', 'o'}, chars);
    }

    @Test(timeout = 4000)
    public void testToCharArrayWithRangeEmpty() {
        StrBuilder sb = new StrBuilder("Hello");
        char[] chars = sb.toCharArray(2, 2);
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, chars);
    }

    @Test(timeout = 4000)
    public void testAppendNullWithNullTextSetToNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText(null);
        sb.append((String) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendObjectNull() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertObjectNull() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.setNullText("NULL");
        sb.insert(2, (Object) null);
        assertEquals("HeNULLllo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertObjectNullWithoutNullText() {
        StrBuilder sb = new StrBuilder("Hello");
        sb.insert(2, (Object) null);
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftWithNullObjectAndNoNullText() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 5, '*');
        assertEquals("*****", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightWithNullObjectAndNoNullText() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 5, '*');
        assertEquals("*****", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsCollection() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        sb.appendWithSeparators(list, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsIterator() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        list.add("b");
        sb.appendWithSeparators(list.iterator(), ",");
        assertEquals("a,b", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsIteratorSingleElement() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        sb.appendWithSeparators(list.iterator(), ",");
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArraySingleElement() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a"}, ",");
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsCollectionSingleElement() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("a");
        sb.appendWithSeparators(list, ",");
        assertEquals("a", sb.toString());
    }
}