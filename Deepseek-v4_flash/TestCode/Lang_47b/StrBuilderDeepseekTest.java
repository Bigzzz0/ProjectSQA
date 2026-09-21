package org.apache.commons.lang.text;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for StrBuilder.
 * Targeted at maximum line/branch coverage and detection of the known Defects4J
 * defect: NullPointerException in appendFixedWidthPadLeft/Right when
 * nullText is null and object is null.
 * 
 * [Branch & Defect Analysis Matrix]
 * - appendFixedWidthPadLeft/Right: branches on (obj == null) -> getNullText()
 *   which may return null -> str.length() NPE.  Also width>0 vs <=0, strLen >= width vs < width.
 * - Many other methods: null checks on arguments, boundary indices, empty/full buffer states.
 * - We focus on exhaustive coverage of public methods, including overloads.
 */
public class StrBuilderDeepseekTest {

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testConstructorDefault() {
        StrBuilder sb = new StrBuilder();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacityPositive() {
        StrBuilder sb = new StrBuilder(64);
        assertEquals(0, sb.length());
        assertTrue(sb.capacity() >= 64);
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacityZero() {
        StrBuilder sb = new StrBuilder(0);
        assertEquals(0, sb.length());
        // capacity is reset to CAPACITY (32)
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacityNegative() {
        StrBuilder sb = new StrBuilder(-10);
        assertEquals(0, sb.length());
        assertTrue(sb.capacity() >= 32);
    }

    @Test(timeout = 4000)
    public void testConstructorFromString() {
        StrBuilder sb = new StrBuilder("Hello");
        assertEquals(5, sb.length());
        assertEquals("Hello", sb.toString());
    }

    @Test(timeout = 4000)
    public void testConstructorFromNullString() {
        StrBuilder sb = new StrBuilder(null);
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testLength() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(3, sb.length());
        sb.append("de");
        assertEquals(5, sb.length());
    }

    @Test(timeout = 4000)
    public void testSetLengthToSmaller() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSetLengthToLarger() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setLength(6);
        assertEquals(6, sb.length());
        // padded with '\0'
        assertEquals("abc\0\0\0", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testSetLengthNegative() {
        new StrBuilder().setLength(-1);
    }

    @Test(timeout = 4000)
    public void testCapacity() {
        StrBuilder sb = new StrBuilder(10);
        assertTrue(sb.capacity() >= 10);
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
        sb.append("short");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testSize() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals(3, sb.size());
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        assertTrue(new StrBuilder().isEmpty());
        assertFalse(new StrBuilder("a").isEmpty());
    }

    @Test(timeout = 4000)
    public void testClear() {
        StrBuilder sb = new StrBuilder("hello");
        sb.clear();
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testCharAt() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals('t', sb.charAt(0));
        assertEquals('e', sb.charAt(1));
        assertEquals('s', sb.charAt(2));
        assertEquals('t', sb.charAt(3));
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testCharAtNegativeIndex() {
        new StrBuilder("a").charAt(-1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testCharAtIndexEqualsLength() {
        new StrBuilder("a").charAt(1);
    }

    @Test(timeout = 4000)
    public void testSetCharAt() {
        StrBuilder sb = new StrBuilder("abc");
        sb.setCharAt(1, 'X');
        assertEquals("aXc", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testSetCharAtInvalidIndex() {
        new StrBuilder("a").setCharAt(1, 'b');
    }

    @Test(timeout = 4000)
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("hello");
        sb.deleteCharAt(1);
        assertEquals("hllo", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteCharAtInvalidIndex() {
        new StrBuilder().deleteCharAt(0);
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testAppendNullObject() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        // default nullText = null, so appendNull() does nothing
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullString() {
        StrBuilder sb = new StrBuilder();
        sb.append((String) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append((StringBuffer) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullStrBuilder() {
        StrBuilder sb = new StrBuilder();
        sb.append((StrBuilder) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append((char[]) null);
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendEmptyString() {
        StrBuilder sb = new StrBuilder("a");
        sb.append("");
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        sb.append(new StringBuffer("test"));
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilder() {
        StrBuilder sb1 = new StrBuilder("first");
        StrBuilder sb2 = new StrBuilder("second");
        sb1.append(sb2);
        assertEquals("firstsecond", sb1.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c'});
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayPartial() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        assertEquals("bc", sb.toString());
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
        sb.append(1234567890123L);
        assertEquals("1234567890123", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFloat() {
        StrBuilder sb = new StrBuilder();
        sb.append(3.14f);
        assertTrue(sb.toString().startsWith("3.14"));
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        StrBuilder sb = new StrBuilder();
        sb.append(2.71828);
        assertTrue(sb.toString().startsWith("2.71828"));
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone (NPE in fixedWidthPad methods)
    // ========================================================================

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftNullObject() {
        StrBuilder sb = new StrBuilder();
        // This call should NOT throw NullPointerException, but the buggy version does.
        sb.appendFixedWidthPadLeft(null, 5, '*');
        // After fix, with nullText==null, the string is empty -> pad left with 5 spaces.
        assertEquals("     ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightNullObject() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 5, '*');
        assertEquals("     ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftWithNonNull() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 5, '*');
        assertEquals("**abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftLongerThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abcdef", 3, '*');
        assertEquals("def", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightWithNonNull() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 5, '*');
        assertEquals("abc**", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightLongerThanWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abcdef", 3, '*');
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeftInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(42, 4, '0');
        assertEquals("0042", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRightInt() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(42, 4, '0');
        assertEquals("4200", sb.toString());
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBadStartIndex() {
        new StrBuilder().append("test", -1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBadLength() {
        new StrBuilder().append("test", 0, 10);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferBadStartIndex() {
        new StrBuilder().append(new StringBuffer("test"), -1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringBufferBadLength() {
        new StrBuilder().append(new StringBuffer("test"), 0, 10);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderBadStartIndex() {
        new StrBuilder().append(new StrBuilder("test"), -1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStrBuilderBadLength() {
        new StrBuilder().append(new StrBuilder("test"), 0, 10);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayBadStartIndex() {
        new StrBuilder().append(new char[]{'a'}, -1, 1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArrayBadLength() {
        new StrBuilder().append(new char[]{'a'}, 0, 10);
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
        assertTrue(sb1.equals((Object) sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals("abc"));
        assertFalse(sb1.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder("ABC");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("abd");
        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertFalse(sb1.equalsIgnoreCase(sb3));
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
        assertEquals(new StringBuffer("test").toString(), sb.toStringBuffer().toString());
    }

    // ========================================================================
    // Additional Coverage for Methods with Complex Logic
    // ========================================================================

    // --- appendNewLine and appendNull ---
    @Test(timeout = 4000)
    public void testAppendNewLineDefault() {
        StrBuilder sb = new StrBuilder();
        sb.appendNewLine();
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
    public void testAppendNullWhenNullTextSet() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("NULL");
        sb.appendNull();
        assertEquals("NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNullWhenNullTextNull() {
        StrBuilder sb = new StrBuilder();
        sb.appendNull();
        assertEquals("", sb.toString());
    }

    // --- appendAll methods ---
    @Test(timeout = 4000)
    public void testAppendAllObjectArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(new Object[]{"a", "b", "c"});
        assertEquals("abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendAllCollection() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(Arrays.asList("x", "y", "z"));
        assertEquals("xyz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendAllIterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendAll(Arrays.asList("1", "2").iterator());
        assertEquals("12", sb.toString());
    }

    // --- appendWithSeparators ---
    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsCollection() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(Arrays.asList("a", "b"), ".");
        assertEquals("a.b", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsIterator() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(Arrays.asList("a").iterator(), ",");
        assertEquals("a", sb.toString());
    }

    // --- appendSeparator ---
    @Test(timeout = 4000)
    public void testAppendSeparatorStringWhenEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorStringWhenNonEmpty() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(",");
        assertEquals("a,", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorCharWhenEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(',');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorCharWhenNonEmpty() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(',');
        assertEquals("a,", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorStringWithLoopIndexZero() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(",", 0);
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorStringWithLoopIndexPositive() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(",", 1);
        assertEquals("a,", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorCharWithLoopIndexZero() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(',', 0);
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorCharWithLoopIndexPositive() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendSeparator(',', 1);
        assertEquals("a,", sb.toString());
    }

    // --- appendPadding ---
    @Test(timeout = 4000)
    public void testAppendPaddingPositive() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(5, 'x');
        assertEquals("xxxxx", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingZero() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendPadding(0, 'x');
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingNegative() {
        StrBuilder sb = new StrBuilder("a");
        sb.appendPadding(-1, 'x');
        assertEquals("a", sb.toString());
    }

    // --- insert methods ---
    @Test(timeout = 4000)
    public void testInsertString() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, "XX");
        assertEquals("aXXb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertNullString() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, (String) null);
        assertEquals("ab", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArray() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, new char[]{'X', 'Y'});
        assertEquals("aXYb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertCharArrayPartial() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, new char[]{'X', 'Y', 'Z'}, 1, 1);
        assertEquals("aYb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertBoolean() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, true);
        assertEquals("atrueb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertChar() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, 'X');
        assertEquals("aXb", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertInt() {
        StrBuilder sb = new StrBuilder("ab");
        sb.insert(1, 42);
        assertEquals("a42b", sb.toString());
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndex() {
        new StrBuilder().insert(1, "x");
    }

    // --- delete methods ---
    @Test(timeout = 4000)
    public void testDeleteRange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.delete(1, 4);
        assertEquals("ho", sb.toString());
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
        StrBuilder sb = new StrBuilder("hellohello");
        sb.deleteAll("hello");
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstString() {
        StrBuilder sb = new StrBuilder("hellohello");
        sb.deleteFirst("hello");
        assertEquals("hello", sb.toString());
    }

    // --- replace methods ---
    @Test(timeout = 4000)
    public void testReplaceRange() {
        StrBuilder sb = new StrBuilder("hello");
        sb.replace(1, 4, "XX");
        assertEquals("hXXo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllChar() {
        StrBuilder sb = new StrBuilder("aba");
        sb.replaceAll('a', 'X');
        assertEquals("XbX", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstChar() {
        StrBuilder sb = new StrBuilder("aba");
        sb.replaceFirst('a', 'X');
        assertEquals("Xba", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllString() {
        StrBuilder sb = new StrBuilder("catcat");
        sb.replaceAll("cat", "dog");
        assertEquals("dogdog", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstString() {
        StrBuilder sb = new StrBuilder("catcat");
        sb.replaceFirst("cat", "dog");
        assertEquals("dogcat", sb.toString());
    }

    // --- reverse and trim ---
    @Test(timeout = 4000)
    public void testReverse() {
        StrBuilder sb = new StrBuilder("hello");
        sb.reverse();
        assertEquals("olleh", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReverseEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.reverse();
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrim() {
        StrBuilder sb = new StrBuilder("  hello world  ");
        sb.trim();
        assertEquals("hello world", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimAlreadyTrimmed() {
        StrBuilder sb = new StrBuilder("hello");
        sb.trim();
        assertEquals("hello", sb.toString());
    }

    // --- substring / left / right / mid ---
    @Test(timeout = 4000)
    public void testSubstringStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ello", sb.substring(1));
    }

    @Test(timeout = 4000)
    public void testSubstringRange() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ell", sb.substring(1, 4));
    }

    @Test(timeout = 4000)
    public void testLeftStringNormal() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hel", sb.leftString(3));
    }

    @Test(timeout = 4000)
    public void testLeftStringNegative() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("", sb.leftString(-1));
    }

    @Test(timeout = 4000)
    public void testLeftStringExceeding() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.leftString(10));
    }

    @Test(timeout = 4000)
    public void testRightStringNormal() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("llo", sb.rightString(3));
    }

    @Test(timeout = 4000)
    public void testRightStringNegative() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("", sb.rightString(-1));
    }

    @Test(timeout = 4000)
    public void testRightStringExceeding() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.rightString(10));
    }

    @Test(timeout = 4000)
    public void testMidStringNormal() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("ell", sb.midString(1, 3));
    }

    @Test(timeout = 4000)
    public void testMidStringNegativeIndex() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("hello", sb.midString(-1, 5));
    }

    @Test(timeout = 4000)
    public void testMidStringNegativeLength() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("", sb.midString(0, -1));
    }

    @Test(timeout = 4000)
    public void testMidStringIndexExceedsSize() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals("", sb.midString(10, 2));
    }

    // --- indexOf / lastIndexOf / contains ---
    @Test(timeout = 4000)
    public void testIndexOfChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(1, sb.indexOf('e'));
    }

    @Test(timeout = 4000)
    public void testIndexOfCharNotFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf('x'));
    }

    @Test(timeout = 4000)
    public void testIndexOfCharWithStart() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(4, sb.indexOf('o', 3));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(0, sb.indexOf("hell"));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringNotFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf("world"));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringNull() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.indexOf(null));
    }

    @Test(timeout = 4000)
    public void testIndexOfStringEmpty() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(0, sb.indexOf(""));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(3, sb.lastIndexOf('l'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfCharNotFound() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.lastIndexOf('x'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("hellohello");
        assertEquals(5, sb.lastIndexOf("hello"));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfStringNull() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals(-1, sb.lastIndexOf(null));
    }

    @Test(timeout = 4000)
    public void testContainsChar() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('z'));
    }

    @Test(timeout = 4000)
    public void testContainsString() {
        StrBuilder sb = new StrBuilder("hello");
        assertTrue(sb.contains("ell"));
        assertFalse(sb.contains("xyz"));
    }

    // --- toCharArray ---
    @Test(timeout = 4000)
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder("abc");
        assertArrayEquals(new char[]{'a','b','c'}, sb.toCharArray());
    }

    @Test(timeout = 4000)
    public void testToCharArrayEmpty() {
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, new StrBuilder().toCharArray());
    }

    @Test(timeout = 4000)
    public void testToCharArrayRange() {
        StrBuilder sb = new StrBuilder("abcde");
        assertArrayEquals(new char[]{'b','c'}, sb.toCharArray(1, 3));
    }

    @Test(timeout = 4000)
    public void testToCharArrayRangeEndTooLarge() {
        StrBuilder sb = new StrBuilder("abc");
        assertArrayEquals(new char[]{'a','b','c'}, sb.toCharArray(0, 10));
    }

    // --- getChars ---
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
        assertArrayEquals(new char[]{'a','b','c', '\0', '\0'}, dest);
    }

    @Test(timeout = 4000)
    public void testGetCharsWithIndices() {
        StrBuilder sb = new StrBuilder("hello");
        char[] dest = new char[3];
        sb.getChars(1, 4, dest, 0);
        assertArrayEquals(new char[]{'e','l','l'}, dest);
    }

    // --- asTokenizer, asReader, asWriter (basic smoke tests) ---
    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("a b c");
        StrTokenizer tok = sb.asTokenizer();
        String[] tokens = tok.getTokenArray();
        assertArrayEquals(new String[]{"a", "b", "c"}, tokens);
    }

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("hello");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[5];
        int read = reader.read(buf);
        assertEquals(5, read);
        assertArrayEquals(new char[]{'h','e','l','l','o'}, buf);
        assertEquals(-1, reader.read());
    }

    @Test(timeout = 4000)
    public void testAsWriter() {
        StrBuilder sb = new StrBuilder();
        java.io.Writer writer = sb.asWriter();
        try {
            writer.write("test");
            writer.flush();
            assertEquals("test", sb.toString());
            writer.write(" more");
            assertEquals("test more", sb.toString());
        } catch (java.io.IOException e) {
            fail("Unexpected IOException");
        }
    }
}