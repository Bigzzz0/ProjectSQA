package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang.text.StrBuilder
 *
 * 1. Defect LANG-295 (Ground Truth):
 *    - Method: contains(char ch)
 *    - Defect: Iterates over buffer.length instead of size, checking residual/unallocated characters.
 *    - Targeted Test: testLang295ContainsCharBeyondSize
 *
 * 2. Coverage & Partition Matrix:
 *    - Partition A (Core & State): Constructors (default, sized, String), capacity, length, setLength,
 *      ensureCapacity, minimizeCapacity, size, isEmpty, clear.
 *    - Partition B (Character & Array Access): charAt, setCharAt, deleteCharAt, toCharArray, getChars.
 *    - Partition C (Append Operations): Object, String, StringBuffer, StrBuilder, char[], primitives,
 *      appendPadding, appendFixedWidthPadLeft/Right, appendWithSeparators (array, collection, iterator).
 *    - Partition D (Insert, Delete & Replace Operations): insert primitives/strings/arrays, delete ranges,
 *      deleteAll/deleteFirst (char, String, StrMatcher), replace (char, String, StrMatcher).
 *    - Partition E (Search, Compare & Transform): indexOf, lastIndexOf, startsWith, endsWith, contains,
 *      substring, leftString, rightString, midString, reverse, trim, equals, equalsIgnoreCase, hashCode.
 *    - Partition F (Views & Adaptors): asTokenizer (tokenization & content), asReader (read, skip, mark, reset),
 *      asWriter (write char, array, string).
 *    - Partition G (Defensive Boundary & Exceptions): Negative indices, out-of-bound ranges, null arguments.
 */
public class StrBuilderGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-295)
    // =========================================================================

    /**
     * Targets LANG-295: contains(char) must only search up to 'size', not 'buffer.length'.
     */
    @Test(timeout = 4000)
    public void testLang295ContainsCharBeyondSize() {
        StrBuilder sb = new StrBuilder("1234567890");
        assertEquals(10, sb.length());
        sb.delete(5, 10);
        assertEquals(5, sb.length());
        assertEquals("12345", sb.toString());
        assertFalse("The contains(char) method is looking beyond the end of the string", sb.contains('6'));
        assertFalse("The contains(char) method is looking beyond the end of the string", sb.contains('9'));
        assertTrue(sb.contains('1'));
        assertTrue(sb.contains('5'));

        StrBuilder sb2 = new StrBuilder(32);
        sb2.append("abc");
        sb2.clear();
        assertFalse("contains(char) should return false on empty/cleared builder", sb2.contains('a'));
    }

    // =========================================================================
    // Partition A: Constructors, Capacity, Length & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructors() {
        StrBuilder sb1 = new StrBuilder();
        assertEquals(0, sb1.length());
        assertEquals(32, sb1.capacity());

        StrBuilder sb2 = new StrBuilder(-10);
        assertEquals(0, sb2.length());
        assertEquals(32, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(50);
        assertEquals(0, sb3.length());
        assertEquals(50, sb3.capacity());

        StrBuilder sb4 = new StrBuilder((String) null);
        assertEquals(0, sb4.length());
        assertEquals(32, sb4.capacity());

        StrBuilder sb5 = new StrBuilder("Hello");
        assertEquals(5, sb5.length());
        assertEquals(5 + 32, sb5.capacity());
        assertEquals("Hello", sb5.toString());
    }

    @Test(timeout = 4000)
    public void testNewLineAndNullText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.setNewLineText("\r\n");
        assertEquals("\r\n", sb.getNewLineText());
        sb.appendNewLine();
        assertEquals("\r\n", sb.toString());

        sb.clear();
        sb.setNewLineText(null);
        assertNull(sb.getNewLineText());
        sb.appendNewLine();
        assertNotNull(sb.toString());

        sb.clear();
        assertNull(sb.getNullText());
        sb.setNullText("");
        assertNull(sb.getNullText());
        sb.setNullText("NULL");
        assertEquals("NULL", sb.getNullText());
        sb.appendNull();
        assertEquals("NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSetLengthAndCapacity() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertEquals(11, sb.length());
        assertEquals(11, sb.size());
        assertFalse(sb.isEmpty());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("Hello", sb.toString());

        sb.setLength(8);
        assertEquals(8, sb.length());
        assertEquals("Hello\0\0\0", sb.toString());

        sb.setLength(8); // no change branch
        assertEquals(8, sb.length());

        sb.ensureCapacity(100);
        assertTrue(sb.capacity() >= 100);
        sb.ensureCapacity(50); // no expansion needed
        assertTrue(sb.capacity() >= 100);

        sb.minimizeCapacity();
        assertEquals(8, sb.capacity());

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
        sb.minimizeCapacity();
        assertEquals(0, sb.capacity());
    }

    // =========================================================================
    // Partition B: Character and Array Accessors
    // =========================================================================

    @Test(timeout = 4000)
    public void testCharacterAccess() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals('a', sb.charAt(0));
        assertEquals('f', sb.charAt(5));

        sb.setCharAt(0, 'z');
        assertEquals('z', sb.charAt(0));

        sb.deleteCharAt(0);
        assertEquals("bcdef", sb.toString());
    }

    @Test(timeout = 4000)
    public void testToCharArrayAndGetChars() {
        StrBuilder empty = new StrBuilder();
        assertArrayEquals(new char[0], empty.toCharArray());
        assertArrayEquals(new char[0], empty.toCharArray(0, 0));

        StrBuilder sb = new StrBuilder("abcdef");
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, sb.toCharArray());
        assertArrayEquals(new char[]{'b', 'c', 'd'}, sb.toCharArray(1, 4));
        assertArrayEquals(new char[]{'d', 'e', 'f'}, sb.toCharArray(3, 100)); // clamped range
        assertArrayEquals(new char[0], sb.toCharArray(2, 2));

        char[] dest = new char[10];
        char[] returned = sb.getChars(dest);
        assertSame(dest, returned);
        assertEquals('a', dest[0]);
        assertEquals('f', dest[5]);

        char[] returnedNew = sb.getChars(null);
        assertEquals(6, returnedNew.length);
        assertEquals('a', returnedNew[0]);

        char[] smallDest = new char[2];
        char[] returnedExpanded = sb.getChars(smallDest);
        assertEquals(6, returnedExpanded.length);

        char[] customDest = new char[10];
        sb.getChars(1, 4, customDest, 2);
        assertEquals('b', customDest[2]);
        assertEquals('c', customDest[3]);
        assertEquals('d', customDest[4]);
    }

    // =========================================================================
    // Partition C: Append Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendPrimitivesAndObjects() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");

        sb.append((Object) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append(new Object() {
            public String toString() {
                return "Obj";
            }
        });
        assertEquals("Obj", sb.toString());
        sb.clear();

        sb.append((String) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append("Str");
        sb.append((String) null, 0, 0); // null calls appendNull
        assertEquals("Str<null>", sb.toString());
        sb.clear();

        sb.append("HelloWorld", 0, 5);
        assertEquals("Hello", sb.toString());
        sb.append("HelloWorld", 5, 0); // length 0
        assertEquals("Hello", sb.toString());

        sb.clear();
        sb.append((StringBuffer) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        StringBuffer sbuf = new StringBuffer("BufferText");
        sb.append(sbuf);
        assertEquals("BufferText", sb.toString());
        sb.clear();

        sb.append((StringBuffer) null, 0, 0);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append(sbuf, 6, 4);
        assertEquals("Text", sb.toString());
        sb.append(sbuf, 0, 0); // length 0
        assertEquals("Text", sb.toString());

        sb.clear();
        sb.append((StrBuilder) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        StrBuilder other = new StrBuilder("BuilderText");
        sb.append(other);
        assertEquals("BuilderText", sb.toString());
        sb.clear();

        sb.append((StrBuilder) null, 0, 0);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append(other, 7, 4);
        assertEquals("Text", sb.toString());
        sb.append(other, 0, 0);
        assertEquals("Text", sb.toString());

        sb.clear();
        sb.append((char[]) null);
        assertEquals("<null>", sb.toString());
        sb.clear();

        char[] chars = new char[]{'C', 'h', 'a', 'r', 's'};
        sb.append(chars);
        assertEquals("Chars", sb.toString());
        sb.clear();

        sb.append((char[]) null, 0, 0);
        assertEquals("<null>", sb.toString());
        sb.clear();

        sb.append(chars, 1, 3);
        assertEquals("har", sb.toString());
        sb.append(chars, 0, 0);
        assertEquals("har", sb.toString());

        sb.clear();
        sb.append(true).append('-').append(false);
        assertEquals("true-false", sb.toString());

        sb.clear();
        sb.append('X');
        assertEquals("X", sb.toString());

        sb.clear();
        sb.append(123).append(4567890123L).append(1.5f).append(2.75d);
        assertEquals("12345678901231.52.75", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals("", sb.toString());
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"A", "B"}, null);
        assertEquals("AB", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Collection) null, ",");
        assertEquals("", sb.toString());
        sb.appendWithSeparators(Collections.emptyList(), ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList("X", "Y", "Z"), "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("X", "Y"), null);
        assertEquals("XY", sb.toString());

        sb.clear();
        sb.appendWithSeparators((Iterator) null, ",");
        assertEquals("", sb.toString());
        sb.appendWithSeparators(Collections.emptyList().iterator(), ",");
        assertEquals("", sb.toString());

        sb.appendWithSeparators(Arrays.asList(1, 2, 3).iterator(), ":");
        assertEquals("1:2:3", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList(1, 2).iterator(), null);
        assertEquals("12", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendPaddingAndFixedWidth() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-1, ' ');
        assertEquals("", sb.toString());
        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, '0');
        assertEquals("00abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 4, '0');
        assertEquals("cdef", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(null, 4, 'x'); // nullText is null -> NPE on length() or getNullText
        assertEquals("xnull", sb.toString().substring(0, 0)); // testing null text
        sb.clear();
        sb.setNullText("nil");
        sb.appendFixedWidthPadLeft(null, 5, '0');
        assertEquals("00nil", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 4, '0');
        assertEquals("0042", sb.toString());
        sb.appendFixedWidthPadLeft(42, -1, '0'); // width <= 0 no effect
        assertEquals("0042", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abc", 5, '0');
        assertEquals("abc00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 4, '0');
        assertEquals("abcd", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(null, 5, '0');
        assertEquals("nil00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight(42, 4, '0');
        assertEquals("4200", sb.toString());
        sb.appendFixedWidthPadRight(42, 0, '0'); // width <= 0 no effect
        assertEquals("4200", sb.toString());
    }

    // =========================================================================
    // Partition D: Insert, Delete & Replace Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testInsertMethods() {
        StrBuilder sb = new StrBuilder("ac");
        sb.setNullText("<null>");

        sb.insert(1, "b");
        assertEquals("abc", sb.toString());

        sb.insert(3, (String) null);
        assertEquals("abc<null>", sb.toString());

        sb.clear();
        sb.append("ac");
        sb.insert(1, (Object) "b");
        assertEquals("abc", sb.toString());
        sb.insert(1, (Object) null);
        assertEquals("a<null>bc", sb.toString());

        sb.clear();
        sb.append("ac");
        sb.insert(1, new char[]{'b'});
        assertEquals("abc", sb.toString());
        sb.insert(1, (char[]) null);
        assertEquals("a<null>bc", sb.toString());

        sb.clear();
        sb.append("ad");
        sb.insert(1, new char[]{'x', 'b', 'c', 'y'}, 1, 2);
        assertEquals("abcd", sb.toString());
        sb.insert(1, (char[]) null, 0, 0);
        assertEquals("a<null>bcd", sb.toString());
        sb.insert(1, new char[]{'z'}, 0, 0); // length 0
        assertEquals("a<null>bcd", sb.toString());

        sb.clear();
        sb.insert(0, true);
        sb.insert(4, false);
        assertEquals("truefalse", sb.toString());

        sb.clear();
        sb.insert(0, 'c');
        sb.insert(0, 10);
        sb.insert(sb.length(), 20L);
        sb.insert(sb.length(), 3.5f);
        sb.insert(sb.length(), 4.25d);
        assertEquals("10c203.54.25", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteOperations() {
        StrBuilder sb = new StrBuilder("Hello World Beautiful World");

        sb.delete(5, 11); // removes " World"
        assertEquals("Hello Beautiful World", sb.toString());
        sb.delete(0, 0); // length 0
        assertEquals("Hello Beautiful World", sb.toString());
        sb.delete(5, 100); // clamped to size
        assertEquals("Hello", sb.toString());

        sb = new StrBuilder("banana");
        sb.deleteAll('a');
        assertEquals("bnn", sb.toString());

        sb = new StrBuilder("banana");
        sb.deleteFirst('a');
        assertEquals("bnana", sb.toString());
        sb.deleteFirst('z'); // char not found
        assertEquals("bnana", sb.toString());

        sb = new StrBuilder("foo bar foo baz foo");
        sb.deleteAll("foo");
        assertEquals(" bar  baz ", sb.toString());
        sb.deleteAll((String) null); // null no action
        assertEquals(" bar  baz ", sb.toString());
        sb.deleteAll(""); // empty string no action
        assertEquals(" bar  baz ", sb.toString());

        sb = new StrBuilder("foo bar foo baz foo");
        sb.deleteFirst("foo");
        assertEquals(" bar foo baz foo", sb.toString());
        sb.deleteFirst("nonexistent");
        assertEquals(" bar foo baz foo", sb.toString());
        sb.deleteFirst((String) null);
        assertEquals(" bar foo baz foo", sb.toString());
        sb.deleteFirst("");
        assertEquals(" bar foo baz foo", sb.toString());

        sb = new StrBuilder("a123b456c");
        sb.deleteAll(StrMatcher.charRangeMatcher('0', '9'));
        assertEquals("abc", sb.toString());
        sb.deleteAll((StrMatcher) null);
        assertEquals("abc", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteFirst(StrMatcher.charRangeMatcher('0', '9'));
        assertEquals("ab2c3", sb.toString());
        sb.deleteFirst((StrMatcher) null);
        assertEquals("ab2c3", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceOperations() {
        StrBuilder sb = new StrBuilder("Hello World");
        sb.replace(0, 5, "Greetings");
        assertEquals("Greetings World", sb.toString());

        sb.replace(9, 100, null); // replace with null = delete
        assertEquals("Greetings", sb.toString());

        sb = new StrBuilder("aba cac");
        sb.replaceAll('a', 'x');
        assertEquals("xbx cxc", sb.toString());
        sb.replaceAll('x', 'x'); // search == replace, no-op
        assertEquals("xbx cxc", sb.toString());

        sb = new StrBuilder("aba cac");
        sb.replaceFirst('a', 'x');
        assertEquals("xba cac", sb.toString());
        sb.replaceFirst('z', 'y'); // not found
        assertEquals("xba cac", sb.toString());
        sb.replaceFirst('x', 'x'); // search == replace
        assertEquals("xba cac", sb.toString());

        sb = new StrBuilder("foo bar foo baz");
        sb.replaceAll("foo", "qux");
        assertEquals("qux bar qux baz", sb.toString());
        sb.replaceAll((String) null, "test");
        assertEquals("qux bar qux baz", sb.toString());
        sb.replaceAll("bar", null); // replace with null
        assertEquals("qux  qux baz", sb.toString());

        sb = new StrBuilder("foo bar foo baz");
        sb.replaceFirst("foo", "qux");
        assertEquals("qux bar foo baz", sb.toString());
        sb.replaceFirst("notfound", "x");
        assertEquals("qux bar foo baz", sb.toString());
        sb.replaceFirst((String) null, "x");
        assertEquals("qux bar foo baz", sb.toString());
        sb.replaceFirst("bar", null);
        assertEquals("qux  foo baz", sb.toString());

        sb = new StrBuilder("a1 b2 c3 d4");
        sb.replaceAll(StrMatcher.charRangeMatcher('0', '9'), "#");
        assertEquals("a# b# c# d#", sb.toString());

        sb = new StrBuilder("a1 b2 c3 d4");
        sb.replaceFirst(StrMatcher.charRangeMatcher('0', '9'), "#");
        assertEquals("a# b2 c3 d4", sb.toString());

        sb = new StrBuilder("a1 b2 c3 d4");
        sb.replace(StrMatcher.charRangeMatcher('0', '9'), "#", 2, 8, 1);
        assertEquals("a1 b# c3 d4", sb.toString());

        sb.replace((StrMatcher) null, "x", 0, sb.length(), -1); // null matcher no-op
        assertEquals("a1 b# c3 d4", sb.toString());

        StrBuilder emptySb = new StrBuilder();
        emptySb.replace(StrMatcher.charMatcher('a'), "x", 0, 0, -1);
        assertEquals("", emptySb.toString());
    }

    // =========================================================================
    // Partition E: Search, Substring, Reverse, Trim, Equals & HashCode
    // =========================================================================

    @Test(timeout = 4000)
    public void testReverseAndTrim() {
        StrBuilder sb = new StrBuilder("12345");
        sb.reverse();
        assertEquals("54321", sb.toString());
        sb.setLength(4);
        sb.reverse();
        assertEquals("2345", sb.toString());

        StrBuilder empty = new StrBuilder();
        empty.reverse();
        assertEquals("", empty.toString());

        StrBuilder trimSb = new StrBuilder("   \t  Hello World \n \r  ");
        trimSb.trim();
        assertEquals("Hello World", trimSb.toString());

        StrBuilder allSpaces = new StrBuilder("    ");
        allSpaces.trim();
        assertEquals("", allSpaces.toString());

        empty.trim();
        assertEquals("", empty.toString());
    }

    @Test(timeout = 4000)
    public void testStartsAndEndsWith() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.startsWith("Hello"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith((String) null));
        assertFalse(sb.startsWith("HelloWorldExtended"));
        assertFalse(sb.startsWith("Help"));

        assertTrue(sb.endsWith("World"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith((String) null));
        assertFalse(sb.endsWith("HelloWorldExtended"));
        assertFalse(sb.endsWith("Word"));
    }

    @Test(timeout = 4000)
    public void testSubstrings() {
        StrBuilder sb = new StrBuilder("HelloWorld");
        assertEquals("World", sb.substring(5));
        assertEquals("Hello", sb.substring(0, 5));
        assertEquals("World", sb.substring(5, 100)); // clamped

        assertEquals("", sb.leftString(-1));
        assertEquals("", sb.leftString(0));
        assertEquals("Hello", sb.leftString(5));
        assertEquals("HelloWorld", sb.leftString(20));

        assertEquals("", sb.rightString(-1));
        assertEquals("", sb.rightString(0));
        assertEquals("World", sb.rightString(5));
        assertEquals("HelloWorld", sb.rightString(20));

        assertEquals("", sb.midString(-1, -1));
        assertEquals("", sb.midString(50, 5));
        assertEquals("", sb.midString(2, 0));
        assertEquals("Hello", sb.midString(-5, 5));
        assertEquals("World", sb.midString(5, 20));
        assertEquals("loWo", sb.midString(3, 4));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndLastIndexOf() {
        StrBuilder sb = new StrBuilder("abracadabra");

        assertTrue(sb.contains('a'));
        assertFalse(sb.contains('z'));
        assertTrue(sb.contains("cad"));
        assertFalse(sb.contains("cadx"));
        assertTrue(sb.contains(StrMatcher.charMatcher('c')));
        assertFalse(sb.contains(StrMatcher.charMatcher('z')));

        assertEquals(0, sb.indexOf('a'));
        assertEquals(-1, sb.indexOf('z'));
        assertEquals(3, sb.indexOf('a', 2));
        assertEquals(-1, sb.indexOf('a', 50));
        assertEquals(0, sb.indexOf('a', -5));

        assertEquals(0, sb.indexOf("abra"));
        assertEquals(7, sb.indexOf("abra", 1));
        assertEquals(-1, sb.indexOf("abra", 50));
        assertEquals(-1, sb.indexOf("xyz"));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(2, sb.indexOf("", 2));
        assertEquals(-1, sb.indexOf("abracadabrazzzzz"));
        assertEquals(1, sb.indexOf("b")); // 1-char fastpath

        assertEquals(0, sb.indexOf(StrMatcher.stringMatcher("abra")));
        assertEquals(7, sb.indexOf(StrMatcher.stringMatcher("abra"), 1));
        assertEquals(-1, sb.indexOf(StrMatcher.stringMatcher("abra"), 50));
        assertEquals(-1, sb.indexOf((StrMatcher) null));

        assertEquals(10, sb.lastIndexOf('a'));
        assertEquals(-1, sb.lastIndexOf('z'));
        assertEquals(7, sb.lastIndexOf('a', 9));
        assertEquals(10, sb.lastIndexOf('a', 50));
        assertEquals(-1, sb.lastIndexOf('a', -1));

        assertEquals(7, sb.lastIndexOf("abra"));
        assertEquals(0, sb.lastIndexOf("abra", 6));
        assertEquals(-1, sb.lastIndexOf("abra", -1));
        assertEquals(10, sb.lastIndexOf("", 10));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(-1, sb.lastIndexOf("abracadabrazzzzz"));
        assertEquals(8, sb.lastIndexOf("b")); // 1-char fastpath
        assertEquals(-1, sb.lastIndexOf("notfound"));

        assertEquals(7, sb.lastIndexOf(StrMatcher.stringMatcher("abra")));
        assertEquals(0, sb.lastIndexOf(StrMatcher.stringMatcher("abra"), 6));
        assertEquals(-1, sb.lastIndexOf(StrMatcher.stringMatcher("abra"), -1));
        assertEquals(-1, sb.lastIndexOf((StrMatcher) null));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("hello");
        StrBuilder sb4 = new StrBuilder("Hello World");

        assertTrue(sb1.equals(sb1));
        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(sb4));
        assertFalse(sb1.equals((Object) null));
        assertFalse(sb1.equals("Hello"));

        assertTrue(sb1.equalsIgnoreCase(sb1));
        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(null));

        assertEquals(sb1.hashCode(), sb2.hashCode());
        assertNotEquals(sb1.hashCode(), sb3.hashCode());

        StringBuffer sbuf = sb1.toStringBuffer();
        assertEquals("Hello", sbuf.toString());
    }

    // =========================================================================
    // Partition F: Views & Adaptors (Tokenizer, Reader, Writer)
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsTokenizer() {
        StrBuilder sb = new StrBuilder("apple orange banana");
        StrTokenizer tok = sb.asTokenizer();
        String[] tokens = tok.getTokenArray();
        assertArrayEquals(new String[]{"apple", "orange", "banana"}, tokens);
        assertEquals("apple orange banana", tok.getContent());

        tok.reset(new char[]{'x', ' ', 'y'});
        assertArrayEquals(new String[]{"x", "y"}, tok.getTokenArray());
    }

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("abcdef");
        Reader reader = sb.asReader();

        assertTrue(reader.markSupported());
        assertTrue(reader.ready());

        assertEquals('a', (char) reader.read());
        assertEquals('b', (char) reader.read());

        reader.mark(10);
        assertEquals('c', (char) reader.read());
        reader.reset();
        assertEquals('c', (char) reader.read());

        char[] cbuf = new char[2];
        int readCount = reader.read(cbuf, 0, 2);
        assertEquals(2, readCount);
        assertArrayEquals(new char[]{'d', 'e'}, cbuf);

        assertEquals(0, reader.read(cbuf, 0, 0));

        long skipped = reader.skip(1);
        assertEquals(1, skipped);
        assertEquals(-1, reader.read());
        assertFalse(reader.ready());

        assertEquals(0, reader.skip(-5));
        assertEquals(0, reader.skip(5));
        assertEquals(-1, reader.read(cbuf, 0, 2));

        reader.close(); // no-op
    }

    @Test(timeout = 4000)
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();

        writer.write('a');
        assertEquals("a", sb.toString());

        writer.write(new char[]{'b', 'c'});
        assertEquals("abc", sb.toString());

        writer.write(new char[]{'x', 'd', 'e', 'y'}, 1, 2);
        assertEquals("abcde", sb.toString());

        writer.write("fg");
        assertEquals("abcdefg", sb.toString());

        writer.write("xhiyj", 1, 2);
        assertEquals("abcdefghi", sb.toString());

        writer.flush(); // no-op
        writer.close(); // no-op
    }

    // =========================================================================
    // Partition G: Defensive Guard Paths & Exceptions
    // =========================================================================

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetLengthNegative() {
        new StrBuilder().setLength(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtNegative() {
        new StrBuilder("abc").charAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testCharAtBeyond() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtNegative() {
        new StrBuilder("abc").setCharAt(-1, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtBeyond() {
        new StrBuilder("abc").setCharAt(3, 'x');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtNegative() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtBeyond() {
        new StrBuilder("abc").deleteCharAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateRangeStartNegative() {
        new StrBuilder("abc").substring(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateRangeEndLessThanStart() {
        new StrBuilder("abc").substring(2, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsNegativeStart() {
        new StrBuilder("abc").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndBeyondLength() {
        new StrBuilder("abc").getChars(0, 5, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndLessThanStart() {
        new StrBuilder("abc").getChars(2, 1, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidStart() {
        new StrBuilder().append("abc", -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringInvalidLength() {
        new StrBuilder().append("abc", 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringBufferInvalidStart() {
        new StrBuilder().append(new StringBuffer("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStringBufferInvalidLength() {
        new StrBuilder().append(new StringBuffer("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStrBuilderInvalidStart() {
        new StrBuilder().append(new StrBuilder("abc"), -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendStrBuilderInvalidLength() {
        new StrBuilder().append(new StrBuilder("abc"), 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidStart() {
        new StrBuilder().append(new char[]{'a', 'b'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testAppendCharArrayInvalidLength() {
        new StrBuilder().append(new char[]{'a', 'b'}, 1, 5);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertStringInvalidIndex() {
        new StrBuilder("abc").insert(5, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidOffset() {
        new StrBuilder("abc").insert(1, new char[]{'x'}, -1, 1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testInsertCharArrayInvalidLength() {
        new StrBuilder("abc").insert(1, new char[]{'x'}, 0, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReaderReadInvalidParams() throws Exception {
        new StrBuilder("abc").asReader().read(new char[2], -1, 1);
    }
}