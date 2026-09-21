package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects:
 * - LANG-412: appendFixedWidthPadLeft and appendFixedWidthPadRight throw NullPointerException
 *   when null object is passed and nullText is null (default).
 *   Targeted tests: testLang412Left, testLang412Right.
 *
 * Branch & Condition Coverage Matrix:
 * - Constructors:
 *     - default (initial capacity 32)
 *     - int: capacity <= 0 (defaults to 32), capacity > 0
 *     - String: null string, non-null string
 * - State Management:
 *     - setNewLineText, getNewLineText
 *     - setNullText: null, empty string (resets to null), non-empty string
 *     - setLength: negative (< 0 -> StringIndexOutOfBoundsException), length < size, length == size, length > size (null padding)
 *     - capacity, ensureCapacity: capacity > buffer.length, capacity <= buffer.length
 *     - minimizeCapacity: buffer.length > length, buffer.length == length
 *     - size, length, isEmpty, clear
 * - Index-based access & mutation:
 *     - charAt, setCharAt, deleteCharAt: index < 0, index == size, index > size, valid indices
 *     - toCharArray: size == 0, size > 0; range: length == 0, valid slice
 *     - getChars: null dest array, small dest array, sufficient dest array
 *     - getChars(start, end, dest, destIndex): start < 0, end < 0, end > length, start > end
 * - Append family (Objects, Strings, StrBuilder, StringBuffer, char[], primitives):
 *     - null vs non-null handling, empty vs non-empty
 *     - slices with startIndex and length: valid, start < 0, start > len, length < 0, start + length > len
 *     - boolean (true vs false), char, int, long, float, double
 * - appendln family:
 *     - all data types followed by line separator (default vs custom newLineText)
 * - appendAll & appendWithSeparators:
 *     - Object[], Collection, Iterator: null, empty, 1-element, multi-elements
 *     - separator: null (converts to ""), non-null
 * - appendSeparator:
 *     - separator with empty builder vs non-empty builder
 *     - with loopIndex: index <= 0 vs index > 0
 * - Fixed Width & Padding:
 *     - appendPadding: negative length, zero, positive length
 *     - appendFixedWidthPadLeft / PadRight: width <= 0, width == len, width > len, width < len
 *     - LANG-412 regression tests: obj == null with nullText == null
 * - Insert family:
 *     - insert(index, Object/String/char[]/primitives): index < 0, index > size, index == 0, index == size
 *     - insert char[] with offset and length boundaries
 * - Deletion & Replacement:
 *     - delete(start, end): start < 0, end > size, start > end, empty slice
 *     - deleteAll(char), deleteFirst(char): matching contiguous, single, not found
 *     - deleteAll(String), deleteFirst(String): null, empty, found, not found
 *     - deleteAll(StrMatcher), deleteFirst(StrMatcher): null matcher, empty builder, non-matching, matching
 *     - replace(start, end, String): replaceStr null (deletes), equal length, larger length, smaller length
 *     - replaceAll/replaceFirst for char, String, and StrMatcher
 * - Manipulation & Search:
 *     - reverse: empty, odd length, even length
 *     - trim: empty, only whitespace, leading whitespace, trailing whitespace, both, none
 *     - startsWith, endsWith: null, empty, prefix/suffix shorter, equal, longer, matching, non-matching
 *     - substring, leftString, rightString, midString: negative, zero, beyond boundaries
 *     - contains, indexOf, lastIndexOf: char, String, StrMatcher; start index negative, middle, beyond size
 * - Views & Contracts:
 *     - asTokenizer: tokenization and reset verification
 *     - asReader: read(), read(char[], off, len), skip, ready, mark, reset, invalid read params
 *     - asWriter: write(int), write(char[]), write(char[], off, len), write(String), write(String, off, len)
 *     - equals, equalsIgnoreCase, hashCode, toString, toStringBuffer
 */
public class StrBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndInitialCapacity() {
        StrBuilder sbDefault = new StrBuilder();
        assertEquals(0, sbDefault.length());
        assertEquals(32, sbDefault.capacity());
        assertTrue(sbDefault.isEmpty());

        StrBuilder sbPositive = new StrBuilder(64);
        assertEquals(0, sbPositive.length());
        assertEquals(64, sbPositive.capacity());

        StrBuilder sbZero = new StrBuilder(0);
        assertEquals(32, sbZero.capacity());

        StrBuilder sbNegative = new StrBuilder(-10);
        assertEquals(32, sbNegative.capacity());

        StrBuilder sbStr = new StrBuilder("Hello");
        assertEquals(5, sbStr.length());
        assertEquals(5 + 32, sbStr.capacity());
        assertEquals("Hello", sbStr.toString());

        StrBuilder sbNullStr = new StrBuilder((String) null);
        assertEquals(0, sbNullStr.length());
        assertEquals(32, sbNullStr.capacity());
    }

    @Test(timeout = 4000)
    public void testNewLineAndNullTextHandling() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        assertNull(sb.getNullText());

        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
        sb.appendNewLine();
        assertEquals("\r\n", sb.toString());

        sb.clear();
        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());

        sb.setNullText("NULL_VAL");
        assertEquals("NULL_VAL", sb.getNullText());
        sb.appendNull();
        assertEquals("NULL_VAL", sb.toString());

        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testCapacityAndMinimize() {
        StrBuilder sb = new StrBuilder("Apache Commons");
        int originalCap = sb.capacity();
        sb.ensureCapacity(originalCap + 50);
        assertTrue(sb.capacity() >= originalCap + 50);

        sb.minimizeCapacity();
        assertEquals(sb.length(), sb.capacity());

        // Minimizing when buffer.length == length()
        sb.minimizeCapacity();
        assertEquals(sb.length(), sb.capacity());
    }

    @Test(timeout = 4000)
    public void testSetLengthTransitions() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("Hello", sb.toString());

        sb.setLength(8);
        assertEquals(8, sb.length());
        assertEquals("Hello\0\0\0", sb.toString());

        sb.setLength(8);
        assertEquals(8, sb.length());
    }

    @Test(timeout = 4000)
    public void testCharAtSetCharAtDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abcde");
        assertEquals('a', sb.charAt(0));
        assertEquals('e', sb.charAt(4));

        sb.setCharAt(2, 'Z');
        assertEquals("abZde", sb.toString());

        sb.deleteCharAt(2);
        assertEquals("abde", sb.toString());
        assertEquals(4, sb.length());
    }

    @Test(timeout = 4000)
    public void testToCharArrayAndGetChars() {
        StrBuilder empty = new StrBuilder();
        assertArrayEquals(new char[0], empty.toCharArray());
        assertArrayEquals(new char[0], empty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("ABCDEF");
        char[] full = sb.toCharArray();
        assertArrayEquals(new char[]{'A', 'B', 'C', 'D', 'E', 'F'}, full);

        char[] slice = sb.toCharArray(1, 4);
        assertArrayEquals(new char[]{'B', 'C', 'D'}, slice);

        char[] emptySlice = sb.toCharArray(2, 2);
        assertEquals(0, emptySlice.length);

        char[] dest = sb.getChars((char[]) null);
        assertArrayEquals(full, dest);

        char[] smallDest = new char[2];
        char[] resized = sb.getChars(smallDest);
        assertEquals(6, resized.length);

        char[] exactDest = new char[10];
        sb.getChars(1, 4, exactDest, 2);
        assertEquals('B', exactDest[2]);
        assertEquals('C', exactDest[3]);
        assertEquals('D', exactDest[4]);
    }

    @Test(timeout = 4000)
    public void testReverseAndTrim() {
        StrBuilder empty = new StrBuilder();
        assertSame(empty, empty.reverse());
        assertSame(empty, empty.trim());

        StrBuilder even = new StrBuilder("1234");
        even.reverse();
        assertEquals("4321", even.toString());

        StrBuilder odd = new StrBuilder("12345");
        odd.reverse();
        assertEquals("54321", odd.toString());

        StrBuilder whitespace = new StrBuilder("   \t  \n");
        whitespace.trim();
        assertEquals("", whitespace.toString());

        StrBuilder padded = new StrBuilder("  \t hello world  \n ");
        padded.trim();
        assertEquals("hello world", padded.toString());

        StrBuilder noWhitespace = new StrBuilder("hello");
        noWhitespace.trim();
        assertEquals("hello", noWhitespace.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendAllAndSeparators() {
        StrBuilder sb = new StrBuilder();

        // Arrays
        sb.appendAll((Object[]) null);
        sb.appendAll(new Object[0]);
        sb.appendAll(new Object[]{"a", "b", "c"});
        assertEquals("abc", sb.toString());

        // Collections
        sb.clear();
        sb.appendAll((List<?>) null);
        sb.appendAll(Collections.emptyList());
        sb.appendAll(Arrays.asList("d", "e"));
        assertEquals("de", sb.toString());

        // Iterators
        sb.clear();
        sb.appendAll((java.util.Iterator<?>) null);
        sb.appendAll(Arrays.asList("f", "g").iterator());
        assertEquals("fg", sb.toString());

        // With Separators
        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        sb.appendWithSeparators(new Object[0], ",");
        sb.appendWithSeparators(new Object[]{"1"}, ",");
        assertEquals("1", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"1", "2", "3"}, null);
        assertEquals("123", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"1", "2", "3"}, ",");
        assertEquals("1,2,3", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.emptyList(), ",");
        sb.appendWithSeparators(Arrays.asList("A", "B"), ",");
        assertEquals("A,B", sb.toString());

        sb.clear();
        sb.appendWithSeparators((java.util.Iterator<?>) null, ",");
        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z").iterator(), "-");
        assertEquals("X-Y-Z", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSeparatorVariants() {
        StrBuilder sb = new StrBuilder();
        sb.appendSeparator(",");
        sb.appendSeparator(',');
        assertEquals("", sb.toString());

        sb.append("A");
        sb.appendSeparator((String) null);
        sb.appendSeparator(",");
        sb.append("B");
        sb.appendSeparator(';');
        sb.append("C");
        assertEquals("A,B;C", sb.toString());

        sb.clear();
        sb.appendSeparator(",", 0);
        sb.appendSeparator(',', 0);
        assertEquals("", sb.toString());

        sb.appendSeparator(",", 1);
        sb.appendSeparator(';', 1);
        assertEquals(",;", sb.toString());
        sb.appendSeparator((String) null, 2);
        assertEquals(",;", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-5, '*');
        assertEquals("", sb.toString());
        sb.appendPadding(0, '*');
        assertEquals("", sb.toString());
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, ' ');
        assertEquals("  abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, ' ');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 4, '0');
        assertEquals("0042", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(12345, 3, '0');
        assertEquals("345", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, ' ');
        assertEquals("abc  ", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 3, ' ');
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 4, '0');
        assertEquals("4200", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(12345, 3, '0');
        assertEquals("123", sb.toString());

        // Zero or negative width
        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 0, ' ');
        sb.appendFixedWidthPadLeft("abc", -1, ' ');
        sb.appendFixedWidthPadRight("abc", 0, ' ');
        sb.appendFixedWidthPadRight("abc", -1, ' ');
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSubstringsAndBoundaries() {
        StrBuilder sb = new StrBuilder("abcdefgh");
        assertEquals("cdefgh", sb.substring(2));
        assertEquals("cde", sb.substring(2, 5));
        assertEquals("cdefgh", sb.substring(2, 20)); // treats large end as size

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.leftString(0));
        assertEquals("abc", sb.leftString(3));
        assertEquals("abcdefgh", sb.leftString(50));

        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.rightString(0));
        assertEquals("fgh", sb.rightString(3));
        assertEquals("abcdefgh", sb.rightString(50));

        assertEquals("", sb.midString(-5, 0));
        assertEquals("", sb.midString(0, -5));
        assertEquals("", sb.midString(100, 5));
        assertEquals("ab", sb.midString(-5, 2)); // negative index treated as zero
        assertEquals("cde", sb.midString(2, 3));
        assertEquals("fgh", sb.midString(5, 10)); // insufficient chars returns remainder
    }

    @Test(timeout = 4000)
    public void testStartsWithAndEndsWith() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        assertTrue(sb.startsWith("Hello"));
        assertFalse(sb.startsWith("HelloWorldExtended"));
        assertFalse(sb.startsWith("World"));

        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
        assertTrue(sb.endsWith("World"));
        assertFalse(sb.endsWith("HelloWorldExtended"));
        assertFalse(sb.endsWith("Hello"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-412)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang412Left() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals("**********", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadLeft(null, 10, '*');
        assertEquals("******null", sb.toString());
    }

    @Test(timeout = 4000)
    public void testLang412Right() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals("**********", sb.toString());

        sb.clear();
        sb.setNullText("null");
        sb.appendFixedWidthPadRight(null, 10, '*');
        assertEquals("null******", sb.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetLengthNegativeThrows() {
        new StrBuilder().setLength(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtNegativeThrows() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtBeyondSizeThrows() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtInvalidIndexThrows() {
        new StrBuilder("abc").setCharAt(5, 'X');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtNegativeThrows() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtBeyondSizeThrows() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsNegativeStartThrows() {
        new StrBuilder("abc").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndGreaterThanLengthThrows() {
        new StrBuilder("abc").getChars(0, 5, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsStartGreaterThanEndThrows() {
        new StrBuilder("abc").getChars(3, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidStartThrows() {
        new StrBuilder().append("abc", -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidLengthThrows() {
        new StrBuilder().append("abc", 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringBufferInvalidStartThrows() {
        new StrBuilder().append(new StringBuffer("abc"), -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringBufferInvalidLengthThrows() {
        new StrBuilder().append(new StringBuffer("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStrBuilderInvalidStartThrows() {
        new StrBuilder().append(new StrBuilder("abc"), -1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStrBuilderInvalidLengthThrows() {
        new StrBuilder().append(new StrBuilder("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidStartThrows() {
        new StrBuilder().append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidLengthThrows() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertInvalidIndexThrows() {
        new StrBuilder("abc").insert(5, "X");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidOffsetThrows() {
        new StrBuilder("abc").insert(1, new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidLengthThrows() {
        new StrBuilder("abc").insert(1, new char[]{'a', 'b'}, 1, 5);
    }

    // =========================================================================
    // Partition E: Comprehensive Append, Insert, Delete, Replace Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllAppendVariants() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(false);
        assertEquals("truefalse", sb.toString());

        sb.clear();
        sb.append('c').append(123).append(456789L).append(1.5f).append(2.5d);
        assertEquals("c1234567891.52.5", sb.toString());

        sb.clear();
        sb.append((Object) "Obj").append((String) null).append((StringBuffer) null).append((StrBuilder) null).append((char[]) null);
        assertEquals("Obj", sb.toString());

        sb.clear();
        sb.append("Hello", 0, 0); // zero length
        sb.append("Hello", 1, 3);
        assertEquals("ell", sb.toString());

        sb.clear();
        sb.append(new StringBuffer("StringBuffer"), 6, 6);
        assertEquals("Buffer", sb.toString());

        sb.clear();
        sb.append(new StrBuilder("StrBuilder"), 3, 7);
        assertEquals("Builder", sb.toString());

        sb.clear();
        sb.append(new char[]{'j', 'a', 'v', 'a'}, 1, 3);
        assertEquals("ava", sb.toString());
        sb.append(new char[]{'1', '2'}, 0, 0);
        assertEquals("ava", sb.toString());

        sb.clear();
        sb.setNullText("<NULL>");
        sb.append((Object) null);
        sb.append((String) null, 0, 0);
        sb.append((StringBuffer) null, 0, 0);
        sb.append((StrBuilder) null, 0, 0);
        sb.append((char[]) null, 0, 0);
        assertEquals("<NULL><NULL><NULL><NULL><NULL>", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAllAppendLnVariants() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\n");

        sb.appendln((Object) "Obj");
        sb.appendln("Str");
        sb.appendln("PartStr", 1, 3);
        sb.appendln(new StringBuffer("Buf"));
        sb.appendln(new StringBuffer("PartBuf"), 1, 3);
        sb.appendln(new StrBuilder("Bld"));
        sb.appendln(new StrBuilder("PartBld"), 1, 3);
        sb.appendln(new char[]{'a', 'b'});
        sb.appendln(new char[]{'a', 'b', 'c', 'd'}, 1, 2);
        sb.appendln(true);
        sb.appendln('z');
        sb.appendln(10);
        sb.appendln(20L);
        sb.appendln(1.0f);
        sb.appendln(2.0d);

        String expected = "Obj\nStr\nart\nBuf\nart\nBld\nart\nab\nbc\ntrue\nz\n10\n20\n1.0\n2.0\n";
        assertEquals(expected, sb.toString());
    }

    @Test(timeout = 4000)
    public void testAllInsertVariants() {
        StrBuilder sb = new StrBuilder("world");
        sb.insert(0, "hello ");
        assertEquals("hello world", sb.toString());

        sb.insert(5, (String) null); // null text is null -> no op
        assertEquals("hello world", sb.toString());

        sb.insert(5, (char[]) null);
        assertEquals("hello world", sb.toString());

        sb.insert(5, (char[]) null, 0, 0);
        assertEquals("hello world", sb.toString());

        sb.insert(0, new char[]{'!', ' '});
        assertEquals("! hello world", sb.toString());

        sb.insert(1, new char[]{'a', 'b', 'c'}, 1, 2);
        assertEquals("!bchello world", sb.toString());

        sb.clear().append("base");
        sb.insert(0, true).insert(sb.length(), false);
        assertEquals("truebasefalse", sb.toString());

        sb.clear().append("test");
        sb.insert(1, 'X');
        sb.insert(0, 100);
        sb.insert(sb.length(), 200L);
        sb.insert(0, 1.5f);
        sb.insert(sb.length(), 2.5d);
        assertEquals("1.5100tXest2002.5", sb.toString());

        sb.clear().append("test");
        sb.insert(0, (Object) null);
        assertEquals("test", sb.toString());
        sb.setNullText("NULL_");
        sb.insert(0, (Object) null);
        assertEquals("NULL_test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteVariants() {
        StrBuilder sb = new StrBuilder("foobarbazfoo");
        sb.delete(3, 6); // remove bar
        assertEquals("foobazfoo", sb.toString());

        sb.delete(0, 0); // no-op
        assertEquals("foobazfoo", sb.toString());

        sb.deleteAll('o');
        assertEquals("fbazf", sb.toString());

        sb.clear().append("aabbccaa");
        sb.deleteFirst('a');
        assertEquals("abbccaa", sb.toString());

        sb.clear().append("food and good food");
        sb.deleteAll("food");
        assertEquals(" and good ", sb.toString());

        sb.clear().append("food and good food");
        sb.deleteFirst("food");
        assertEquals(" and good food", sb.toString());

        sb.deleteAll((String) null);
        sb.deleteFirst((String) null);
        sb.deleteAll("");
        sb.deleteFirst("");
        assertEquals(" and good food", sb.toString());

        // StrMatcher deletion
        StrMatcher spaceMatcher = StrMatcher.spaceMatcher();
        sb.deleteFirst(spaceMatcher);
        assertEquals("and good food", sb.toString());

        sb.deleteAll(spaceMatcher);
        assertEquals("andgoodfood", sb.toString());

        sb.deleteAll((StrMatcher) null);
        sb.deleteFirst((StrMatcher) null);
        assertEquals("andgoodfood", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceVariants() {
        StrBuilder sb = new StrBuilder("banana");
        sb.replaceAll('a', 'o');
        assertEquals("bonono", sb.toString());

        sb.replaceFirst('o', 'a');
        assertEquals("banono", sb.toString());

        sb.replaceAll('z', 'x'); // not found
        assertEquals("banono", sb.toString());

        sb.clear().append("the quick brown fox jumps over the lazy dog");
        sb.replaceAll("the", "a");
        assertEquals("a quick brown fox jumps over a lazy dog", sb.toString());

        sb.replaceFirst("a", "one");
        assertEquals("one quick brown fox jumps over a lazy dog", sb.toString());

        sb.replaceAll((String) null, "test");
        sb.replaceAll("quick", (String) null); // null replacement removes it
        assertEquals("one  brown fox jumps over a lazy dog", sb.toString());

        sb.clear().append("apple pie apple tart");
        sb.replace(0, 5, "cherry");
        assertEquals("cherry pie apple tart", sb.toString());

        sb.replace(0, 6, null); // delete range
        assertEquals(" pie apple tart", sb.toString());

        // Replace with matcher
        StrMatcher charPMatcher = StrMatcher.charMatcher('p');
        sb.replaceAll(charPMatcher, "X");
        assertEquals(" Xie aXXle tart", sb.toString());

        sb.replaceFirst(StrMatcher.charMatcher('X'), "p");
        assertEquals(" pie aXXle tart", sb.toString());

        sb.replace(StrMatcher.charMatcher('X'), null, 0, sb.length(), -1); // delete matches
        assertEquals(" pie ale tart", sb.toString());

        sb.replace((StrMatcher) null, "test", 0, sb.length(), -1);
        assertEquals(" pie ale tart", sb.toString());

        StrBuilder empty = new StrBuilder();
        empty.replace(StrMatcher.charMatcher('a'), "b", 0, 0, -1);
        assertEquals("", empty.toString());
    }

    // =========================================================================
    // Partition F: Search, Match, Tokenizer, Reader, Writer & Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testSearchAndContains() {
        StrBuilder sb = new StrBuilder("blue berries and blue birds");

        assertTrue(sb.contains('e'));
        assertFalse(sb.contains('z'));
        assertTrue(sb.contains("berries"));
        assertFalse(sb.contains("strawberries"));
        assertTrue(sb.contains(StrMatcher.stringMatcher("birds")));
        assertFalse(sb.contains((StrMatcher) null));

        assertEquals(3, sb.indexOf('e'));
        assertEquals(3, sb.indexOf('e', 0));
        assertEquals(9, sb.indexOf('e', 4));
        assertEquals(-1, sb.indexOf('z'));
        assertEquals(-1, sb.indexOf('e', 100));
        assertEquals(3, sb.indexOf('e', -5));

        assertEquals(0, sb.indexOf("blue"));
        assertEquals(17, sb.indexOf("blue", 5));
        assertEquals(-1, sb.indexOf("blue", 50));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(2, sb.indexOf("", 2));
        assertEquals(-1, sb.indexOf("blue berries and blue birds with extra chars"));

        assertEquals(0, sb.indexOf(StrMatcher.stringMatcher("blue")));
        assertEquals(17, sb.indexOf(StrMatcher.stringMatcher("blue"), 5));
        assertEquals(-1, sb.indexOf((StrMatcher) null));
        assertEquals(-1, sb.indexOf(StrMatcher.stringMatcher("blue"), 100));

        assertEquals(23, sb.lastIndexOf('i'));
        assertEquals(23, sb.lastIndexOf('i', 25));
        assertEquals(8, sb.lastIndexOf('i', 20));
        assertEquals(-1, sb.lastIndexOf('i', -1));
        assertEquals(-1, sb.lastIndexOf('z'));

        assertEquals(17, sb.lastIndexOf("blue"));
        assertEquals(0, sb.lastIndexOf("blue", 10));
        assertEquals(-1, sb.lastIndexOf("blue", -1));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(5, sb.lastIndexOf("", 5));

        assertEquals(17, sb.lastIndexOf(StrMatcher.stringMatcher("blue")));
        assertEquals(0, sb.lastIndexOf(StrMatcher.stringMatcher("blue"), 10));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.stringMatcher("blue"), -1));
    }

    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("one two three");
        StrTokenizer tokenizer = sb.asTokenizer();
        assertArrayEquals(new String[]{"one", "two", "three"}, tokenizer.getTokenArray());
        assertEquals("one two three", tokenizer.getContent());

        tokenizer.reset(new char[]{'a', ' ', 'b'});
        assertArrayEquals(new String[]{"a", "b"}, tokenizer.getTokenArray());
    }

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
        reader.reset();
        assertEquals('l', reader.read());

        char[] buf = new char[5];
        int readCount = reader.read(buf, 0, 5);
        assertEquals(5, readCount);
        assertEquals("lo Wo", new String(buf));

        assertEquals(0, reader.read(buf, 0, 0));
        assertEquals(3, reader.skip(3)); // skips "rld"
        assertEquals(0, reader.skip(-1));
        assertEquals(-1, reader.read());
        assertEquals(-1, reader.read(buf, 0, 1));
        assertFalse(reader.ready());

        reader.close(); // no-op
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReaderInvalidParamsThrows() throws Exception {
        Reader reader = new StrBuilder("Hello").asReader();
        reader.read(new char[5], -1, 2);
    }

    @Test(timeout = 4000)
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();

        writer.write('A');
        writer.write(new char[]{'B', 'C'});
        writer.write(new char[]{'D', 'E', 'F'}, 1, 2);
        writer.write("GHI");
        writer.write("JKLMNOP", 2, 3);
        writer.flush(); // no-op
        writer.close(); // no-op

        assertEquals("ABCEFGCLM", sb.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("Test");
        StrBuilder sb2 = new StrBuilder("Test");
        StrBuilder sb3 = new StrBuilder("test");
        StrBuilder sb4 = new StrBuilder("Testing");

        assertEquals(sb1, sb1);
        assertEquals(sb1, sb2);
        assertEquals(sb1.hashCode(), sb2.hashCode());

        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(sb4));
        assertFalse(sb1.equals(null));
        assertFalse(sb1.equals("Test"));

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb1));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(null));

        StrBuilder sbDiffChar = new StrBuilder("Tess");
        assertFalse(sb1.equalsIgnoreCase(sbDiffChar));
    }

    @Test(timeout = 4000)
    public void testToStringAndToStringBuffer() {
        StrBuilder sb = new StrBuilder("BufferCheck");
        assertEquals("BufferCheck", sb.toString());

        StringBuffer sbuf = sb.toStringBuffer();
        assertNotNull(sbuf);
        assertEquals("BufferCheck", sbuf.toString());
    }
}