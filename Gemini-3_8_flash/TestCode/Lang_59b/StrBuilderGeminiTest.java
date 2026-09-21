package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.lang.text.StrBuilder
 *
 * 1. Defect Targeting (Ground Truth: LANG-299 / Defects4J):
 *    - In appendFixedWidthPadRight(Object, int, char), when strLen >= width, the buggy implementation
 *      invokes str.getChars(0, strLen, buffer, size) instead of str.getChars(0, width, buffer, size).
 *      When width < strLen and buffer.length < size + strLen, this triggers ArrayIndexOutOfBoundsException,
 *      violating the contract that characters beyond width on the right should be truncated.
 *
 * 2. Decision & Branch Coverage:
 *    - Constructors: default (capacity 32), custom capacity (<= 0, > 0), String copy (null vs non-null).
 *    - Capacity management: ensureCapacity (grow vs no-op), minimizeCapacity (shrink vs no-op).
 *    - String/Char mutations: append, insert, delete, replace across null, empty, boundary indices.
 *    - Matcher-based mutations: StrMatcher integration across deleteAll, deleteFirst, replaceAll, replaceFirst.
 *    - Fixed-width padding: left and right padding with width <= 0, width < len, width == len, width > len.
 *    - Readers/Writers/Tokenizers: view facades, buffer sharing, mark/reset, read/write array boundaries.
 *    - Search algorithms: indexOf, lastIndexOf with char, String, and StrMatcher at negative, interior, and edge indices.
 *    - Boundary Validations: StringIndexOutOfBoundsException checks on negative indices and inverted ranges.
 *    - Equality & HashCode: structural equality, case-insensitive comparison, identity checks.
 */
public class StrBuilderGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndInitialState() {
        StrBuilder sbDefault = new StrBuilder();
        assertEquals(0, sbDefault.length());
        assertEquals(0, sbDefault.size());
        assertTrue(sbDefault.isEmpty());
        assertEquals(32, sbDefault.capacity());

        StrBuilder sbCap = new StrBuilder(64);
        assertEquals(64, sbCap.capacity());

        StrBuilder sbZeroCap = new StrBuilder(0);
        assertEquals(32, sbZeroCap.capacity());

        StrBuilder sbNegCap = new StrBuilder(-10);
        assertEquals(32, sbNegCap.capacity());

        StrBuilder sbFromStr = new StrBuilder("Hello");
        assertEquals(5, sbFromStr.length());
        assertEquals(37, sbFromStr.capacity());
        assertEquals("Hello", sbFromStr.toString());

        StrBuilder sbFromNull = new StrBuilder((String) null);
        assertEquals(0, sbFromNull.length());
        assertEquals(32, sbFromNull.capacity());
    }

    @Test(timeout = 4000)
    public void testCapacityTransitions() {
        StrBuilder sb = new StrBuilder(10);
        assertEquals(10, sb.capacity());

        sb.ensureCapacity(5);
        assertEquals(10, sb.capacity());

        sb.ensureCapacity(25);
        assertEquals(25, sb.capacity());

        sb.append("12345");
        assertEquals(5, sb.length());
        assertEquals(25, sb.capacity());

        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());

        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testSetLengthAndClear() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertEquals(11, sb.length());

        sb.setLength(5);
        assertEquals(5, sb.length());
        assertEquals("Hello", sb.toString());

        sb.setLength(8);
        assertEquals(8, sb.length());
        assertEquals("Hello\0\0\0", sb.toString());

        sb.setLength(8);
        assertEquals(8, sb.length());

        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCharAccessAndMutations() {
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
    public void testAppendPrimitivesAndObjects() {
        StrBuilder sb = new StrBuilder();
        sb.append(true).append(' ');
        sb.append(false).append(' ');
        sb.append('!').append(' ');
        sb.append(100).append(' ');
        sb.append(12345678901L).append(' ');
        sb.append(2.5f).append(' ');
        sb.append(5.25d);

        assertEquals("true false ! 100 12345678901 2.5 5.25", sb.toString());

        sb.clear();
        Object obj = new Object() {
            public String toString() {
                return "CustomObj";
            }
        };
        sb.append(obj);
        assertEquals("CustomObj", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringAndSubstrings() {
        StrBuilder sb = new StrBuilder();
        sb.append("First");
        sb.append("Second", 1, 3);
        assertEquals("Firsteco", sb.toString());

        sb.append((String) null, 0, 0);
        assertEquals("Firsteco", sb.toString());

        StringBuffer sbuf = new StringBuffer("BufValue");
        sb.append(sbuf);
        assertEquals("FirstecoBufValue", sb.toString());

        sb.append(sbuf, 3, 4);
        assertEquals("FirstecoBufValueValu", sb.toString());

        StrBuilder otherSb = new StrBuilder("Other");
        sb.append(otherSb);
        assertEquals("FirstecoBufValueValuOther", sb.toString());

        sb.append(otherSb, 1, 3);
        assertEquals("FirstecoBufValueValuOtherth", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendCharArrays() {
        StrBuilder sb = new StrBuilder();
        char[] chars = new char[]{'a', 'b', 'c', 'd', 'e'};
        sb.append(chars);
        assertEquals("abcde", sb.toString());

        sb.append(chars, 1, 3);
        assertEquals("abcdebcd", sb.toString());

        sb.append(chars, 0, 0);
        assertEquals("abcdebcd", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparators() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[]{"A", "B", "C"}, ",");
        assertEquals("A,B,C", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[]{"One"}, ",");
        assertEquals("One", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals(0, sb.length());

        sb.clear();
        List<String> list = Arrays.asList("X", "Y", "Z");
        sb.appendWithSeparators(list, "-");
        assertEquals("X-Y-Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(list.iterator(), ":");
        assertEquals("X:Y:Z", sb.toString());

        sb.clear();
        sb.appendWithSeparators(Collections.emptyList(), "-");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators(Collections.emptyList().iterator(), "-");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators(Arrays.asList("M", "N"), (String) null);
        assertEquals("MN", sb.toString());
    }

    @Test(timeout = 4000)
    public void testNewLineAndNullTextHandling() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.append("A").appendNewLine().append("B");
        assertTrue(sb.toString().contains("A") && sb.toString().contains("B"));

        sb.setNewLineText("\n");
        assertEquals("\n", sb.getNewLineText());
        sb.clear();
        sb.append("Line1").appendNewLine().append("Line2");
        assertEquals("Line1\nLine2", sb.toString());

        assertNull(sb.getNullText());
        sb.appendNull();
        assertEquals("Line1\nLine2", sb.toString());

        sb.setNullText("<null>");
        assertEquals("<null>", sb.getNullText());
        sb.appendNull();
        assertEquals("Line1\nLine2<null>", sb.toString());

        sb.append((String) null);
        assertEquals("Line1\nLine2<null><null>", sb.toString());

        sb.setNullText("");
        assertNull(sb.getNullText());
    }

    @Test(timeout = 4000)
    public void testInsertOperations() {
        StrBuilder sb = new StrBuilder("ac");
        sb.insert(1, 'b');
        assertEquals("abc", sb.toString());

        sb.insert(0, true);
        assertEquals("trueabc", sb.toString());

        sb.insert(7, false);
        assertEquals("trueabcfalse", sb.toString());

        sb.clear().append("World");
        sb.insert(0, "Hello ");
        assertEquals("Hello World", sb.toString());

        sb.insert(5, (String) null);
        assertEquals("Hello World", sb.toString());

        sb.setNullText("!");
        sb.insert(5, (String) null);
        assertEquals("Hello! World", sb.toString());

        sb.clear().append("15");
        sb.insert(1, new char[]{'2', '3', '4'});
        assertEquals("12345", sb.toString());

        sb.insert(3, new char[]{'A', 'B', 'C', 'D'}, 1, 2);
        assertEquals("123BC45", sb.toString());

        sb.clear().append("End");
        sb.insert(0, 10);
        sb.insert(2, 9999999999L);
        sb.insert(sb.length(), 1.5f);
        sb.insert(sb.length(), 2.5d);
        sb.insert(0, (Object) "Start:");
        assertTrue(sb.toString().startsWith("Start:109999999999End"));
    }

    @Test(timeout = 4000)
    public void testDeleteAndTrimOperations() {
        StrBuilder sb = new StrBuilder("abracadabra");
        sb.deleteAll('a');
        assertEquals("brcdbr", sb.toString());

        sb.deleteFirst('b');
        assertEquals("rcdbr", sb.toString());

        sb = new StrBuilder("foo-bar-foo-baz");
        sb.deleteAll("foo-");
        assertEquals("bar-baz", sb.toString());

        sb.deleteFirst("ba");
        assertEquals("r-baz", sb.toString());

        sb.delete(1, 3);
        assertEquals("rbaz", sb.toString());

        StrBuilder toTrim = new StrBuilder("   \t  trimmed content \n  ");
        toTrim.trim();
        assertEquals("trimmed content", toTrim.toString());

        StrBuilder allSpaces = new StrBuilder("    ");
        allSpaces.trim();
        assertEquals("", allSpaces.toString());

        StrBuilder empty = new StrBuilder();
        empty.trim();
        assertEquals("", empty.toString());
    }

    @Test(timeout = 4000)
    public void testReverse() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.reverse();
        assertEquals("fedcba", sb.toString());

        sb = new StrBuilder("abcde");
        sb.reverse();
        assertEquals("edcba", sb.toString());

        sb = new StrBuilder("");
        sb.reverse();
        assertEquals("", sb.toString());

        sb = new StrBuilder("x");
        sb.reverse();
        assertEquals("x", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSubstringAndSlicing() {
        StrBuilder sb = new StrBuilder("hello world");
        assertEquals("world", sb.substring(6));
        assertEquals("hello", sb.substring(0, 5));
        assertEquals("world", sb.substring(6, 50));

        assertEquals("hel", sb.leftString(3));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-5));
        assertEquals("hello world", sb.leftString(50));

        assertEquals("rld", sb.rightString(3));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-5));
        assertEquals("hello world", sb.rightString(50));

        assertEquals("lo wo", sb.midString(3, 5));
        assertEquals("", sb.midString(3, 0));
        assertEquals("", sb.midString(3, -2));
        assertEquals("world", sb.midString(6, 100));
        assertEquals("", sb.midString(20, 5));
        assertEquals("hello", sb.midString(-5, 5));
    }

    @Test(timeout = 4000)
    public void testSearchMethods() {
        StrBuilder sb = new StrBuilder("abababab");
        assertTrue(sb.contains('a'));
        assertFalse(sb.contains('z'));
        assertTrue(sb.contains("aba"));
        assertFalse(sb.contains("xyz"));

        assertEquals(0, sb.indexOf('a'));
        assertEquals(1, sb.indexOf('b'));
        assertEquals(-1, sb.indexOf('z'));
        assertEquals(2, sb.indexOf('a', 1));
        assertEquals(-1, sb.indexOf('a', 10));

        assertEquals(0, sb.indexOf("aba"));
        assertEquals(2, sb.indexOf("aba", 1));
        assertEquals(-1, sb.indexOf("nonexistent"));
        assertEquals(-1, sb.indexOf((String) null));
        assertEquals(0, sb.indexOf("", 0));
        assertEquals(3, sb.indexOf("", 3));
        assertEquals(-1, sb.indexOf("long string that exceeds size"));

        assertEquals(7, sb.lastIndexOf('b'));
        assertEquals(6, sb.lastIndexOf('a'));
        assertEquals(-1, sb.lastIndexOf('z'));
        assertEquals(4, sb.lastIndexOf('a', 5));
        assertEquals(-1, sb.lastIndexOf('a', -1));

        assertEquals(4, sb.lastIndexOf("aba"));
        assertEquals(2, sb.lastIndexOf("aba", 3));
        assertEquals(-1, sb.lastIndexOf("aba", 0));
        assertEquals(-1, sb.lastIndexOf("xyz"));
        assertEquals(-1, sb.lastIndexOf((String) null));
        assertEquals(4, sb.lastIndexOf("", 4));
        assertEquals(-1, sb.lastIndexOf("long string that exceeds size"));

        assertTrue(sb.startsWith("aba"));
        assertFalse(sb.startsWith("bab"));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith((String) null));
        assertFalse(sb.startsWith("longer than sb content here"));

        assertTrue(sb.endsWith("bab"));
        assertFalse(sb.endsWith("aba"));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith((String) null));
        assertFalse(sb.endsWith("longer than sb content here"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAppendPaddingAndFixedLengthLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(-5, ' ');
        assertEquals(0, sb.length());

        sb.appendPadding(3, '-');
        assertEquals("---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abc", 5, '0');
        assertEquals("00abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 3, '0');
        assertEquals("def", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("exact", 5, '0');
        assertEquals("exact", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("ignore", 0, 'x');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("ignore", -2, 'x');
        assertEquals("", sb.toString());

        sb.clear();
        sb.setNullText("NULL");
        sb.appendFixedWidthPadLeft((Object) null, 6, '*');
        assertEquals("**NULL", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft(42, 5, ' ');
        assertEquals("   42", sb.toString());
    }

    @Test(timeout = 4000)
    public void testCharConversionsAndArrayCopies() {
        StrBuilder sb = new StrBuilder();
        char[] emptyChars = sb.toCharArray();
        assertEquals(0, emptyChars.length);

        sb.append("abcdef");
        char[] fullChars = sb.toCharArray();
        assertArrayEquals(new char[]{'a', 'b', 'c', 'd', 'e', 'f'}, fullChars);

        char[] subChars = sb.toCharArray(2, 5);
        assertArrayEquals(new char[]{'c', 'd', 'e'}, subChars);

        char[] zeroLenChars = sb.toCharArray(2, 2);
        assertEquals(0, zeroLenChars.length);

        char[] dest = new char[4];
        char[] retDest = sb.getChars(dest);
        assertNotSame(dest, retDest);
        assertEquals(6, retDest.length);

        char[] largeDest = new char[10];
        char[] retLarge = sb.getChars(largeDest);
        assertSame(largeDest, retLarge);
        assertEquals('a', largeDest[0]);
        assertEquals('f', largeDest[5]);

        char[] explicitDest = new char[5];
        sb.getChars(1, 4, explicitDest, 1);
        assertEquals('b', explicitDest[1]);
        assertEquals('c', explicitDest[2]);
        assertEquals('d', explicitDest[3]);
    }

    @Test(timeout = 4000)
    public void testReplaceWithMatcher() {
        StrBuilder sb = new StrBuilder("a123b456c");
        StrMatcher numericMatcher = StrMatcher.charSetMatcher("0123456789");
        assertTrue(sb.contains(numericMatcher));
        assertEquals(1, sb.indexOf(numericMatcher));
        assertEquals(7, sb.lastIndexOf(numericMatcher));

        sb.deleteAll(numericMatcher);
        assertEquals("abc", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.deleteFirst(numericMatcher);
        assertEquals("ab2c3", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replaceAll(numericMatcher, "#");
        assertEquals("a#b#c#", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replaceFirst(numericMatcher, "#");
        assertEquals("a#b2c3", sb.toString());

        sb = new StrBuilder("a1b2c3");
        sb.replace(numericMatcher, "X", 0, sb.length(), 2);
        assertEquals("aXbXc3", sb.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-299 / Defects4J)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang299_appendFixedWidthPadRight_truncateWhenWidthLessThanStringLength() {
        // Targets Defect LANG-299:
        // When width < strLen, appendFixedWidthPadRight must truncate the right-hand characters.
        // In defective versions, str.getChars(0, strLen, buffer, size) is executed instead of
        // str.getChars(0, width, buffer, size), triggering an ArrayIndexOutOfBoundsException
        // when the buffer is tightly-sized.
        StrBuilder sb = new StrBuilder(4);
        sb.appendFixedWidthPadRight("abcdef", 3, '-');
        assertEquals("Width is 3, expected right side to be truncated to 3 characters", "abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testLang299_appendFixedWidthPadRight_intOverloadTruncate() {
        StrBuilder sb = new StrBuilder(3);
        sb.appendFixedWidthPadRight(12345, 2, ' ');
        assertEquals("12", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRight_paddedNormally() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 5, '0');
        assertEquals("abc00", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("exact", 5, '0');
        assertEquals("exact", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("ignore", 0, 'x');
        assertEquals("", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("ignore", -5, 'x');
        assertEquals("", sb.toString());

        sb.clear();
        sb.setNullText("NULL");
        sb.appendFixedWidthPadRight((Object) null, 6, '*');
        assertEquals("NULL**", sb.toString());
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
    public void testCharAtExceedsThrows() {
        new StrBuilder("abc").charAt(3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testSetCharAtInvalidIndexThrows() {
        new StrBuilder("abc").setCharAt(5, 'Z');
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtNegativeThrows() {
        new StrBuilder("abc").deleteCharAt(-1);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testDeleteCharAtExceedsThrows() {
        new StrBuilder("abc").deleteCharAt(3);
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
        new StrBuilder().append(new char[]{'a', 'b'}, 0, 3);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateRangeInvertedThrows() {
        new StrBuilder("abcdef").substring(4, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateRangeNegativeThrows() {
        new StrBuilder("abcdef").substring(-1, 2);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateIndexNegativeThrows() {
        new StrBuilder("abcdef").insert(-1, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testValidateIndexExceedsThrows() {
        new StrBuilder("abcdef").insert(10, "x");
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsNegativeStartThrows() {
        new StrBuilder("abcdef").getChars(-1, 2, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsEndExceedsThrows() {
        new StrBuilder("abcdef").getChars(0, 10, new char[5], 0);
    }

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetCharsInvertedThrows() {
        new StrBuilder("abcdef").getChars(3, 1, new char[5], 0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("alpha");
        StrBuilder sb2 = new StrBuilder("alpha");
        StrBuilder sb3 = new StrBuilder("ALPHA");
        StrBuilder sb4 = new StrBuilder("beta");
        StrBuilder sb5 = new StrBuilder("alphabeta");

        assertEquals(sb1, sb1);
        assertEquals(sb1, sb2);
        assertNotEquals(sb1, sb3);
        assertNotEquals(sb1, sb4);
        assertNotEquals(sb1, sb5);
        assertNotEquals(sb1, null);
        assertNotEquals(sb1, "alpha");

        assertEquals(sb1.hashCode(), sb2.hashCode());

        assertTrue(sb1.equalsIgnoreCase(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb1));
        assertFalse(sb1.equalsIgnoreCase(sb4));
        assertFalse(sb1.equalsIgnoreCase(sb5));
        assertFalse(sb1.equalsIgnoreCase(null));
    }

    @Test(timeout = 4000)
    public void testToStringAndBufferConversion() {
        StrBuilder sb = new StrBuilder("test");
        assertEquals("test", sb.toString());
        StringBuffer sbuf = sb.toStringBuffer();
        assertEquals("test", sbuf.toString());
    }

    @Test(timeout = 4000)
    public void testReaderIntegration() throws IOException {
        StrBuilder sb = new StrBuilder("ReadMe");
        Reader reader = sb.asReader();

        assertTrue(reader.ready());
        assertTrue(reader.markSupported());
        assertEquals('R', reader.read());

        reader.mark(10);
        assertEquals('e', reader.read());
        assertEquals('a', reader.read());

        reader.reset();
        assertEquals('e', reader.read());

        char[] cbuf = new char[4];
        int readCount = reader.read(cbuf, 0, 4);
        assertEquals(4, readCount);
        assertEquals("adMe", new String(cbuf, 0, readCount));

        assertEquals(-1, reader.read());
        assertEquals(0, reader.read(cbuf, 0, 0));

        reader.reset();
        assertEquals(2, reader.skip(2));
        assertEquals('d', reader.read());
        assertEquals(0, reader.skip(-1));

        reader.close(); // No effect
    }

    @Test(timeout = 4000)
    public void testWriterIntegration() throws IOException {
        StrBuilder sb = new StrBuilder();
        Writer writer = sb.asWriter();

        writer.write('A');
        writer.write(new char[]{'B', 'C'});
        writer.write(new char[]{'X', 'D', 'E', 'Y'}, 1, 2);
        writer.write("FG");
        writer.write("1HI2", 1, 2);

        assertEquals("ABCDEFGHI", sb.toString());

        writer.flush(); // No effect
        writer.close(); // No effect
        assertEquals("ABCDEFGHI", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTokenizerIntegration() {
        StrBuilder sb = new StrBuilder("token1 token2 token3");
        StrTokenizer tok = sb.asTokenizer();

        String[] tokens = tok.getTokenArray();
        assertEquals(3, tokens.length);
        assertEquals("token1", tokens[0]);
        assertEquals("token2", tokens[1]);
        assertEquals("token3", tokens[2]);
        assertEquals("token1 token2 token3", tok.getContent());
    }
}