package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang.text.StrBuilder
 *
 * Key Defects Targeted:
 * - LANG-294 / Defects4J:
 *   In `indexOf(String str, int startIndex)`:
 *   The loop bound calculation erroneously used `thisBuf.length - strLen` (capacity-based)
 *   instead of `size - strLen + 1` (size-based). When `size < buffer.length`, searching for
 *   a string that remains in residual buffer capacity past `size` causes `indexOf` to return
 *   an index >= `size` (expected -1).
 *   Subsequent operations like `deleteAll(String)` invoke `deleteImpl` with the invalid index,
 *   causing `System.arraycopy` with negative length and throwing `ArrayIndexOutOfBoundsException`.
 *
 * Targeted Decision Branches & Boundary Partitions:
 * - Partition A: Constructors (default, capacity <= 0 vs > 0, String null vs populated).
 * - Partition B: Capacity & Buffer Sizing (ensureCapacity, minimizeCapacity, setLength with
 *                truncation and '\0' padding, negative length exception).
 * - Partition C: Appenders (Object, String, StringBuffer, StrBuilder, char[], primitives,
 *                null handling, nullText fallback, newLineText, appendWithSeparators for
 *                array/collection/iterator, fixed-width padding left/right with truncation/padding).
 * - Partition D: Insert & Delete (insert at bounds, null handling, delete range, deleteCharAt,
 *                deleteAll/deleteFirst for char, String, and StrMatcher).
 * - Partition E: Search & Replace (indexOf, lastIndexOf for char/String/StrMatcher with
 *                boundary start positions, replace variations with count limits).
 * - Partition F: Substrings & Extractors (substring, leftString, rightString, midString with
 *                negative/overflow boundaries, toCharArray, getChars).
 * - Partition G: Readers, Writers & Tokenizers (StrBuilderReader read/skip/mark/reset,
 *                StrBuilderWriter write methods, StrBuilderTokenizer tokenization).
 * - Partition H: Object Contract & Equality (equals, equalsIgnoreCase, hashCode, toStringBuffer).
 */
public class StrBuilderGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J LANG-294)
    // =========================================================================

    /**
     * Direct replication of LANG-294:
     * When characters exist in the buffer past 'size' (e.g. after deletion/truncation),
     * indexOf(String, int) must NOT scan past 'size' into unused buffer capacity.
     */
    @Test(timeout = 4000)
    public void testIndexOfLang294() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.delete(0, 6); // Content is now "three", size is 5, but buffer[6..10] still contains "three"
        assertEquals("three", sb.toString());
        assertEquals(5, sb.length());
        // In the defective code, indexOf searched up to buffer.length - strLen and returned 6!
        assertEquals(-1, sb.indexOf("three", 1));
    }

    /**
     * LANG-294 secondary failure:
     * Calling deleteAll when residual characters exist in buffer past 'size' triggers
     * deleteImpl with an out-of-bounds index, causing ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testLang294() {
        StrBuilder sb = new StrBuilder("onetwothree");
        sb.delete(0, 6); // Content is now "three", size = 5
        sb.deleteAll("three"); // Deletes the "three" at index 0. Should stop because size is now 0.
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testIndexOfStringBeyondSizeResiduals() {
        StrBuilder sb = new StrBuilder("hello world");
        sb.setLength(5); // Truncates to "hello", but " world" remains in the buffer
        assertEquals(5, sb.length());
        assertEquals(-1, sb.indexOf("world"));
        assertEquals(-1, sb.indexOf("world", 0));
        assertEquals(-1, sb.indexOf("world", 4));
    }

    // =========================================================================
    // PARTITION A: CONSTRUCTORS & BASIC STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructors() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(StrBuilder.CAPACITY, sb1.capacity());
        assertEquals(0, sb1.length());
        assertTrue(sb1.isEmpty());

        StrBuilder sb2 = new StrBuilder(-5);
        assertEquals(StrBuilder.CAPACITY, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(0);
        assertEquals(StrBuilder.CAPACITY, sb3.capacity());

        StrBuilder sb4 = new StrBuilder(64);
        assertEquals(64, sb4.capacity());
        assertEquals(0, sb4.size());

        StrBuilder sb5 = new StrBuilder((String) null);
        assertEquals(StrBuilder.CAPACITY, sb5.capacity());
        assertEquals(0, sb5.length());

        StrBuilder sb6 = new StrBuilder("Hello");
        assertEquals("Hello", sb6.toString());
        assertEquals(5, sb6.length());
        assertEquals(5 + StrBuilder.CAPACITY, sb6.capacity());
        assertFalse(sb6.isEmpty());
    }

    @Test(timeout = 4000)
    public void testNewLineAndNullTextProperties() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        assertNull(sb.getNullText());

        assertSame(sb, sb.setNewLineText("\r\n"));
        assertEquals("\r\n", sb.getNewLineText());
        sb.appendNewLine();
        assertEquals("\r\n", sb.toString());

        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());

        sb.clear();
        assertSame(sb, sb.setNullText("NULL"));
        assertEquals("NULL", sb.getNullText());
        sb.appendNull();
        assertEquals("NULL", sb.toString());

        sb.setNullText("");
        assertNull(sb.getNullText());

        sb.setNullText(null);
        assertNull(sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testCapacityAndLengthAdjustments() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals(6, sb.length());
        int initialCap = sb.capacity();

        assertSame(sb, sb.ensureCapacity(initialCap - 5));
        assertEquals(initialCap, sb.capacity());

        assertSame(sb, sb.ensureCapacity(100));
        assertEquals(100, sb.capacity());
        assertEquals("abcdef", sb.toString());

        assertSame(sb, sb.minimizeCapacity());
        assertEquals(6, sb.capacity());
        assertEquals("abcdef", sb.toString());

        // Minimizing when capacity equals length does nothing
        assertSame(sb, sb.minimizeCapacity());
        assertEquals(6, sb.capacity());

        // setLength smaller
        assertSame(sb, sb.setLength(3));
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());

        // setLength larger (pads with null char)
        assertSame(sb, sb.setLength(5));
        assertEquals(5, sb.length());
        assertEquals('a', sb.charAt(0));
        assertEquals('b', sb.charAt(1));
        assertEquals('c', sb.charAt(2));
        assertEquals('\0', sb.charAt(3));
        assertEquals('\0', sb.charAt(4));

        // clear
        assertSame(sb, sb.clear());
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetLengthNegativeThrows() {
        new StrBuilder().setLength(-1);
    }

    @Test(timeout = 4000)
    public void testCharAccessAndMutation() {
        StrBuilder sb = new StrBuilder("Java");
        assertEquals('J', sb.charAt(0));
        assertEquals('a', sb.charAt(1));

        assertSame(sb, sb.setCharAt(0, 'K'));
        assertEquals("Kava", sb.toString());

        assertSame(sb, sb.deleteCharAt(1));
        assertEquals("Kva", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtNegativeThrows() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtOverflowThrows() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtInvalidThrows() {
        new StrBuilder("abc").setCharAt(3, 'z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtInvalidThrows() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & APPEND VARIATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendPrimitives() {
        StrBuilder sb = new StrBuilder();
        sb.append(true)
          .append(false)
          .append('!')
          .append(123)
          .append(456789L)
          .append(1.5f)
          .append(2.25d);

        String expected = "truefalse!1234567891.52.25";
        assertEquals(expected, sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendObjectsAndStrings() {
        StrBuilder sb = new StrBuilder();
        sb.append((Object) null);
        assertEquals("", sb.toString());

        sb.setNullText("<null>");
        sb.append((Object) null);
        assertEquals("<null>", sb.toString());

        sb.clear();
        sb.setNullText(null);
        sb.append((String) null);
        sb.append("");
        sb.append("Hello");
        assertEquals("Hello", sb.toString());

        sb.clear();
        sb.append("HelloWorld", 0, 5);
        assertEquals("Hello", sb.toString());

        sb.append((String) null, 0, 0);
        assertEquals("Hello", sb.toString());

        sb.append("World", 0, 0);
        assertEquals("Hello", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringOutOfBounds1() {
        new StrBuilder().append("Test", -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringOutOfBounds2() {
        new StrBuilder().append("Test", 2, 5);
    }

    @Test(timeout = 4000)
    public void testAppendStringBufferAndStrBuilder() {
        StrBuilder sb = new StrBuilder();
        sb.append((StringBuffer) null);
        sb.append(new StringBuffer("Buf"));
        sb.append(new StringBuffer("LongBuffer"), 4, 3);
        assertEquals("BufBuf", sb.toString());

        sb.append((StringBuffer) null, 0, 0);
        assertEquals("BufBuf", sb.toString());

        StrBuilder other = new StrBuilder("Builder");
        sb.append((StrBuilder) null);
        sb.append(other);
        sb.append(other, 0, 4);
        sb.append((StrBuilder) null, 0, 0);
        assertEquals("BufBufBuilderBuil", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrays() {
        StrBuilder sb = new StrBuilder();
        sb.append((char[]) null);
        sb.append(new char[0]);
        sb.append(new char[]{'a', 'b', 'c'});
        assertEquals("abc", sb.toString());

        sb.append((char[]) null, 0, 0);
        sb.append(new char[]{'x', 'y', 'z'}, 1, 2);
        assertEquals("abcyz", sb.toString());

        sb.append(new char[]{'x', 'y', 'z'}, 1, 0);
        assertEquals("abcyz", sb.toString());
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidOffset() {
        new StrBuilder().append(new char[]{'a'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidLength() {
        new StrBuilder().append(new char[]{'a'}, 0, 2);
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"A", "B"}, null);
        assertEquals("AB", sb.toString());

        sb.clear();
        sb.appendWithSeparators((List<?>) null, ",");
        sb.appendWithSeparators(Collections.emptyList(), ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z"), "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator<?>) null, ",");
        sb.appendWithSeparators(Collections.emptyList().iterator(), ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList("1", "2").iterator(), ":");
        assertEquals("1:2", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, ' ');
        sb.appendPadding(0, ' ');
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("foo", -1, ' ');
        sb.appendFixedWidthPadLeft("foo", 0, ' ');
        assertEquals("", sb.toString());

        sb.appendFixedWidthPadLeft("toolong", 4, ' ');
        assertEquals("long", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("bar", 6, '0');
        assertEquals("000bar", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 6, '*');
        assertEquals("**null", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 5, '0');
        assertEquals("00042", sb.toString());

        // Fixed width Pad Right
        sb.clear();
        sb.appendFixedWidthPadRight("foo", 0, ' ');
        assertEquals("", sb.toString());

        sb.appendFixedWidthPadRight("toolong", 4, ' ');
        assertEquals("tool", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("bar", 6, '-');
        assertEquals("bar---", sb.toString());

        sb.clear();
        sb.setNullText("nil");
        sb.appendFixedWidthPadRight(null, 5, '.');
        assertEquals("nil..", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(99, 4, 'x');
        assertEquals("99xx", sb.toString());
    }

    // =========================================================================
    // PARTITION D: INSERT & DELETE MUTATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertOperations() {
        StrBuilder sb = new StrBuilder("world");
        sb.insert(0, "hello ");
        assertEquals("hello world", sb.toString());

        sb.setNullText("NULL");
        sb.insert(5, (Object) null);
        assertEquals("helloNULL world", sb.toString());

        sb.clear().append("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());

        sb.insert(4, false);
        assertEquals("truefalseabc", sb.toString());

        sb.clear().append("ac");
        sb.insert(1, new char[]{'b'});
        assertEquals("abc", sb.toString());

        sb.insert(1, new char[]{'1', '2', '3'}, 1, 1);
        assertEquals("a2bc", sb.toString());

        sb.insert(0, (char[]) null);
        assertEquals("a2bc", sb.toString());

        sb.insert(0, (char[]) null, 0, 0);
        assertEquals("a2bc", sb.toString());

        sb.insert(0, (String) null);
        assertEquals("a2bc", sb.toString());

        sb.clear().append("val: ");
        sb.insert(5, 10);
        sb.insert(7, 20L);
        sb.insert(9, 3.0f);
        sb.insert(12, 4.0d);
        assertTrue(sb.toString().startsWith("val: 10203.04.0"));
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertInvalidIndexThrows() {
        new StrBuilder("abc").insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertPastEndThrows() {
        new StrBuilder("abc").insert(4, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidOffsetThrows() {
        new StrBuilder("abc").insert(0, new char[]{'x'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidLengthThrows() {
        new StrBuilder("abc").insert(0, new char[]{'x'}, 0, 2);
    }

    @Test(timeout = 4000)
    public void testDeleteRangesAndChars() {
        StrBuilder sb = new StrBuilder("abcdefgh");
        assertSame(sb, sb.delete(2, 4)); // removes 'c', 'd'
        assertEquals("abefgh", sb.toString());

        sb.delete(4, 100); // capped at size
        assertEquals("abef", sb.toString());

        sb.delete(2, 2); // len == 0 no-op
        assertEquals("abef", sb.toString());

        sb.clear().append("banana");
        assertSame(sb, sb.deleteAll('a'));
        assertEquals("bnn", sb.toString());

        sb.clear().append("banana");
        assertSame(sb, sb.deleteFirst('a'));
        assertEquals("bnana", sb.toString());

        sb.deleteFirst('z'); // not found
        assertEquals("bnana", sb.toString());

        sb.clear().append("abbaabba");
        assertSame(sb, sb.deleteAll("ba"));
        assertEquals("abab", sb.toString());

        assertSame(sb, sb.deleteAll((String) null));
        assertSame(sb, sb.deleteAll(""));

        sb.clear().append("abbaabba");
        assertSame(sb, sb.deleteFirst("ba"));
        assertEquals("ababba", sb.toString());

        assertSame(sb, sb.deleteFirst((String) null));
        assertSame(sb, sb.deleteFirst(""));
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteNegativeStartThrows() {
        new StrBuilder("abc").delete(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteStartGreaterThanEndThrows() {
        new StrBuilder("abc").delete(2, 1);
    }

    @Test(timeout = 4000)
    public void testDeleteWithStrMatcher() {
        StrBuilder sb = new StrBuilder("a12b34c");
        assertSame(sb, sb.deleteAll((StrMatcher) null));
        assertEquals("a12b34c", sb.toString());

        sb.deleteAll(StrMatcher.charRangeMatcher('0', '9'));
        assertEquals("abc", sb.toString());

        sb = new StrBuilder("a12b34c");
        assertSame(sb, sb.deleteFirst((StrMatcher) null));
        assertEquals("a12b34c", sb.toString());

        sb.deleteFirst(StrMatcher.charRangeMatcher('0', '9'));
        assertEquals("a2b34c", sb.toString());
    }

    // =========================================================================
    // PARTITION E: SEARCH & REPLACE PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testReplaceRangeAndChars() {
        StrBuilder sb = new StrBuilder("hello world");
        assertSame(sb, sb.replace(6, 11, "earth"));
        assertEquals("hello earth", sb.toString());

        sb.replace(0, 5, null);
        assertEquals(" earth", sb.toString());

        sb.clear().append("banana");
        assertSame(sb, sb.replaceAll('a', 'o'));
        assertEquals("bonono", sb.toString());

        sb.replaceAll('x', 'y'); // not found
        assertEquals("bonono", sb.toString());

        sb.replaceAll('o', 'o'); // search == replace
        assertEquals("bonono", sb.toString());

        sb.clear().append("banana");
        assertSame(sb, sb.replaceFirst('a', 'o'));
        assertEquals("bonana", sb.toString());

        sb.replaceFirst('x', 'y'); // not found
        assertEquals("bonana", sb.toString());

        sb.replaceFirst('b', 'b'); // search == replace
        assertEquals("bonana", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceStringsAndMatchers() {
        StrBuilder sb = new StrBuilder("foo bar foo baz");
        assertSame(sb, sb.replaceAll("foo", "qux"));
        assertEquals("qux bar qux baz", sb.toString());

        sb.replaceAll((String) null, "test");
        sb.replaceAll("", "test");
        assertEquals("qux bar qux baz", sb.toString());

        sb.replaceAll("qux", null); // null replacement is treated as empty string
        assertEquals(" bar  baz", sb.toString());

        sb.clear().append("foo bar foo baz");
        assertSame(sb, sb.replaceFirst("foo", "qux"));
        assertEquals("qux bar foo baz", sb.toString());

        sb.replaceFirst((String) null, "test");
        sb.replaceFirst("", "test");
        assertEquals("qux bar foo baz", sb.toString());

        sb.replaceFirst("qux", null);
        assertEquals(" bar foo baz", sb.toString());

        // StrMatcher replace
        sb = new StrBuilder("cat dog cat pig");
        assertSame(sb, sb.replaceAll((StrMatcher) null, "x"));
        assertSame(sb, sb.replaceAll(StrMatcher.stringMatcher("cat"), "bird"));
        assertEquals("bird dog bird pig", sb.toString());

        sb = new StrBuilder("cat dog cat pig");
        assertSame(sb, sb.replaceFirst((StrMatcher) null, "x"));
        assertSame(sb, sb.replaceFirst(StrMatcher.stringMatcher("cat"), "bird"));
        assertEquals("bird dog cat pig", sb.toString());

        // replace with count limits
        sb = new StrBuilder("1-2-3-4-5");
        assertSame(sb, sb.replace(StrMatcher.charMatcher('-'), ":", 0, sb.length(), 2));
        assertEquals("1:2:3-4-5", sb.toString());

        // empty StrBuilder replace no-op
        StrBuilder emptySb = new StrBuilder();
        assertSame(emptySb, emptySb.replace(StrMatcher.charMatcher('a'), "b", 0, 0, -1));
    }

    @Test(timeout = 4000)
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder();
        assertSame(sb, sb.reverse());
        assertEquals("", sb.toString());

        sb.append("a");
        sb.reverse();
        assertEquals("a", sb.toString());

        sb.clear().append("ab");
        sb.reverse();
        assertEquals("ba", sb.toString());

        sb.clear().append("12345");
        sb.reverse();
        assertEquals("54321", sb.toString());

        // trim
        StrBuilder tb = new StrBuilder();
        assertSame(tb, tb.trim());
        assertEquals("", tb.toString());

        tb.append("   ");
        tb.trim();
        assertEquals("", tb.toString());

        tb.clear().append("  hello world  ");
        tb.trim();
        assertEquals("hello world", tb.toString());

        tb.clear().append("no-trim");
        tb.trim();
        assertEquals("no-trim", tb.toString());
    }

    @Test(timeout = 4000)
    public void testStartsWithAndEndsWith() {
        StrBuilder sb = new StrBuilder("hello world");
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        assertTrue(sb.startsWith("hello"));
        assertFalse(sb.startsWith("world"));
        assertFalse(sb.startsWith("hello world longer"));

        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
        assertTrue(sb.endsWith("world"));
        assertFalse(sb.endsWith("hello"));
        assertFalse(sb.endsWith("hello world longer"));
    }

    @Test(timeout = 4000)
    public void testSubstringsAndExtractors() {
        StrBuilder sb = new StrBuilder("0123456789");
        assertEquals("0123456789", sb.substring(0));
        assertEquals("56789", sb.substring(5));
        assertEquals("234", sb.substring(2, 5));
        assertEquals("789", sb.substring(7, 20)); // clamped

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.leftString(0));
        assertEquals("012", sb.leftString(3));
        assertEquals("0123456789", sb.leftString(50));

        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.rightString(0));
        assertEquals("789", sb.rightString(3));
        assertEquals("0123456789", sb.rightString(50));

        assertEquals("", sb.midString(-1, -1));
        assertEquals("", sb.midString(0, 0));
        assertEquals("", sb.midString(20, 5));
        assertEquals("012", sb.midString(-5, 3));
        assertEquals("345", sb.midString(3, 3));
        assertEquals("89", sb.midString(8, 10)); // clamped to end
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSubstringNegativeStartThrows() {
        new StrBuilder("abc").substring(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSubstringStartGreaterThanEndThrows() {
        new StrBuilder("abc").substring(3, 2);
    }

    @Test(timeout = 4000)
    public void testToCharArrayAndGetChars() {
        StrBuilder empty = new StrBuilder();
        assertEquals(0, empty.toCharArray().length);
        assertEquals(0, empty.toCharArray(0, 0).length);

        StrBuilder sb = new StrBuilder("abcdef");
        char[] array = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, array);

        char[] subArray = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'b', 'c', 'd'}, subArray);

        char[] emptySubArray = sb.toCharArray(2, 2);
        assertEquals(0, emptySubArray.length);

        // getChars(destination)
        char[] destNull = sb.getChars(null);
        assertEquals(6, destNull.length);

        char[] destSmall = sb.getChars(new char[2]);
        assertEquals(6, destSmall.length);

        char[] destExact = new char[6];
        assertSame(destExact, sb.getChars(destExact));
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, destExact);

        // getChars(srcBegin, srcEnd, dst, dstBegin)
        char[] destTarget = new char[5];
        sb.getChars(1, 4, destTarget, 1);
        assertEquals('b', destTarget[1]);
        assertEquals('c', destTarget[2]);
        assertEquals('d', destTarget[3]);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsNegativeSrcBeginThrows() {
        new StrBuilder("abc").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndGreaterThanLengthThrows() {
        new StrBuilder("abc").getChars(0, 5, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndLessThanStartThrows() {
        new StrBuilder("abc").getChars(2, 1, new char[5], 0);
    }

    @Test(timeout = 4000)
    public void testIndexOfAndContains() {
        StrBuilder sb = new StrBuilder("banana");
        assertTrue(sb.contains('a'));
        assertFalse(sb.contains('z'));
        assertTrue(sb.contains("nan"));
        assertFalse(sb.contains("xyz"));
        assertTrue(sb.contains(StrMatcher.charMatcher('b')));
        assertFalse(sb.contains(StrMatcher.charMatcher('z')));

        // indexOf char
        assertEquals(1, sb.indexOf('a'));
        assertEquals(1, sb.indexOf('a', 0));
        assertEquals(3, sb.indexOf('a', 2));
        assertEquals(-1, sb.indexOf('a', 10));
        assertEquals(1, sb.indexOf('a', -5));
        assertEquals(-1, sb.indexOf('z'));

        // indexOf String
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(0, sb.indexOf("", 0));
        assertEquals(2, sb.indexOf("", 2));
        assertEquals(1, sb.indexOf("a"));
        assertEquals(1, sb.indexOf("an"));
        assertEquals(3, sb.indexOf("an", 2));
        assertEquals(-1, sb.indexOf("an", 10));
        assertEquals(-1, sb.indexOf("longerthantargetstring"));

        // indexOf StrMatcher
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('a'), 10));
        assertEquals(0, sb.indexOf(StrMatcher.charMatcher('b'), -1));
        assertEquals(1, sb.indexOf(StrMatcher.charMatcher('a'), 0));
        assertEquals(-1, sb.indexOf(StrMatcher.charMatcher('z')));

        // lastIndexOf char
        assertEquals(5, sb.lastIndexOf('a'));
        assertEquals(5, sb.lastIndexOf('a', 100));
        assertEquals(3, sb.lastIndexOf('a', 4));
        assertEquals(-1, sb.lastIndexOf('a', -1));
        assertEquals(-1, sb.lastIndexOf('z'));

        // lastIndexOf String
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(-1, sb.lastIndexOf("an", -1));
        assertEquals(3, sb.lastIndexOf("an"));
        assertEquals(1, sb.lastIndexOf("an", 2));
        assertEquals(5, sb.lastIndexOf("a"));
        assertEquals(2, sb.lastIndexOf("", 2));
        assertEquals(-1, sb.lastIndexOf("toolongstringhere"));

        // lastIndexOf StrMatcher
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('a'), -1));
        assertEquals(5, sb.lastIndexOf(StrMatcher.charMatcher('a')));
        assertEquals(3, sb.lastIndexOf(StrMatcher.charMatcher('a'), 4));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.charMatcher('z')));
    }

    // =========================================================================
    // PARTITION F: VIEWS (READER, WRITER, TOKENIZER)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("Hello World");
        Reader reader = sb.asReader();
        assertTrue(reader.ready());
        assertTrue(reader.markSupported());

        assertEquals('H', reader.read());
        assertEquals('e', reader.read());

        reader.mark(10);
        assertEquals('l', reader.read());
        assertEquals('l', reader.read());
        reader.reset();
        assertEquals('l', reader.read());

        char[] buf = new char[5];
        int count = reader.read(buf, 0, 5);
        assertEquals(5, count);
        assertEquals("lo Wo", new String(buf, 0, count));

        assertEquals(0, reader.read(buf, 0, 0));
        assertEquals(0, reader.skip(-2));
        assertEquals(2, reader.skip(2)); // skips "rl"
        assertEquals('d', reader.read());
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read(buf, 0, 1));

        assertFalse(reader.ready());
        reader.close(); // no-op
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReaderReadInvalidOffsetThrows() throws Exception {
        new StrBuilder("abc").asReader().read(new char[2], -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReaderReadInvalidLengthThrows() throws Exception {
        new StrBuilder("abc").asReader().read(new char[2], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReaderReadOverflowThrows() throws Exception {
        new StrBuilder("abc").asReader().read(new char[2], 1, 2);
    }

    @Test(timeout = 4000)
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();

        writer.write('A');
        writer.write(new char[]{'B', 'C'});
        writer.write(new char[]{'D', 'E', 'F'}, 1, 2); // "EF"
        writer.write("GHI");
        writer.write("JKLM", 1, 2); // "KL"

        writer.flush(); // no-op
        writer.close(); // no-op

        assertEquals("ABCEFGHIJKL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("apple banana cherry");
        StrTokenizer tok = sb.asTokenizer();
        String[] tokens = tok.getTokenArray();
        assertEquals(3, tokens.length);
        assertEquals("apple", tokens[0]);
        assertEquals("banana", tokens[1]);
        assertEquals("cherry", tokens[2]);
        assertEquals("apple banana cherry", tok.getContent());

        sb.append(" date");
        tok.reset();
        assertEquals(4, tok.getTokenArray().length);
    }

    // =========================================================================
    // PARTITION G: EQUALITY, HASHCODE & STRING CONVERSIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("abc");
        StrBuilder sb2 = new StrBuilder("abc");
        StrBuilder sb3 = new StrBuilder("ABC");
        StrBuilder sb4 = new StrBuilder("abcd");

        assertEquals(sb1, sb1);
        assertEquals(sb1, sb2);
        assertNotEquals(sb1, sb3);
        assertNotEquals(sb1, sb4);
        assertNotEquals(sb1, null);
        assertNotEquals(sb1, "abc");

        assertEquals(sb1.hashCode(), sb2.hashCode());

        assertTrue(sb1.equalsIgnoreCase(sb1));
        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(null));

        StrBuilder sbDifferentChar = new StrBuilder("abd");
        assertFalse(sb1.equalsIgnoreCase(sbDifferentChar));
    }

    @Test(timeout = 4000)
    public void testToStringAndToStringBuffer() {
        StrBuilder sb = new StrBuilder("TestStringBuffer");
        StringBuffer sbuf = sb.toStringBuffer();
        assertEquals("TestStringBuffer", sbuf.toString());
        assertEquals(sb.length(), sbuf.length());

        // Modifying the returned StringBuffer should not mutate StrBuilder
        sbuf.append("Extra");
        assertEquals("TestStringBuffer", sb.toString());
    }
}