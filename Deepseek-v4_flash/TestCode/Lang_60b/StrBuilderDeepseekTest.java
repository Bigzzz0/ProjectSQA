package org.apache.commons.lang.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StrBuilderDeepseekTest - Comprehensive White-Box JUnit 4 Test Suite
 * 
 * [Branch & Defect Analysis Matrix]
 * ==============================================
 * Target: org.apache.commons.lang.text.StrBuilder
 * 
 * Decision Branches Targeted:
 *   1. Constructor paths: 0 arg, int arg (<=0 vs >0), String arg (null vs non-null)
 *   2. setLength: length<0 throws, length<size truncate, length>size fill with '\0'
 *   3. ensureCapacity: capacity > buffer.length vs <=
 *   4. setNullText: if nullText != null && nullText.length()==0 then set null
 *   5. append(String): str null -> appendNull, strLen>0 vs ==0
 *   6. append(String,int,int): startIndex validation, length validation
 *   7. append(boolean): true vs false
 *   8. delete(int,int): validateRange, len>0 -> deleteImpl
 *   9. deleteAll(char): consecutive matches, loop offset adjustment
 *  10. replace(int,int,String): replaceImpl with insertLen vs removeLen
 *  11. replaceAll(String,String): indexOf loop, replaceLen adjustments
 *  12. replaceImpl(StrMatcher,String,int,int,int): matcher null, size==0, loop logic
 *  13. indexOf(String,int): null str -> -1, strLen==1 redirect, strLen==0 -> startIndex
 *  14. lastIndexOf(String,int): null str -> -1, strLen>0 and <=size search, strLen==0 -> startIndex
 *  15. contains(char): iterates full buffer (BUG: should only iterate size)
 *  16. substring,midd,left,right: boundary cases, negative/zero length
 *  17. equals,hashCode: size comparison, character-by-character
 *  18. trim: leading/trailing spaces removal
 *  19. reverse: odd/even length, empty builder
 *  20. appendFixedWidthPadLeft/Right: obj null, strLen>=width vs <width
 *  21. insert methods: validateIndex, null handling, array bounds
 *  22. validateRange/validateIndex: exception paths
 * 
 * Known Defect (testLang295): contains(char) iterates over whole buffer
 *   instead of up to size -> must test with builder that has buffer larger than size
 */
public class StrBuilderDeepseekTest {

    // ====================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        StrBuilder sb = new StrBuilder();
        assertEquals(32, sb.capacity());
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorWithInitialCapacity() {
        StrBuilder sb = new StrBuilder(64);
        assertTrue(sb.capacity() >= 64);
        assertEquals(0, sb.length());

        // zero or negative -> defaults to CAPACITY
        StrBuilder sb2 = new StrBuilder(0);
        assertEquals(32, sb2.capacity());

        StrBuilder sb3 = new StrBuilder(-5);
        assertEquals(32, sb3.capacity());
    }

    @Test(timeout = 4000)
    public void testConstructorWithString() {
        StrBuilder sb = new StrBuilder("abc");
        assertEquals("abc", sb.toString());
        assertTrue(sb.capacity() >= 3 + 32);

        StrBuilder sbNull = new StrBuilder((String) null);
        assertEquals(0, sbNull.length());
        assertEquals(32, sbNull.capacity());
    }

    @Test(timeout = 4000)
    public void testAppendAndLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("Hello");
        assertEquals(5, sb.length());
        assertFalse(sb.isEmpty());

        sb.append(' ');
        sb.append("World");
        assertEquals(11, sb.length());
        assertEquals("Hello World", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNull() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("<null>");
        sb.append((String) null);
        assertEquals("<null>", sb.toString());

        sb.clear();
        sb.setNullText(null);
        sb.append((String) null);
        assertEquals(0, sb.length()); // nullText null -> no append
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
    public void testAppendNumericTypes() {
        StrBuilder sb = new StrBuilder();
        sb.append(42);
        assertEquals("42", sb.toString());

        sb.clear();
        sb.append(12345L);
        assertEquals("12345", sb.toString());

        sb.clear();
        sb.append(3.14f);
        assertEquals("3.14", sb.toString());

        sb.clear();
        sb.append(2.71828);
        assertEquals("2.71828", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSubstring() {
        StrBuilder sb = new StrBuilder();
        sb.append("abcdef", 1, 3);
        assertEquals("bcd", sb.toString());

        // null str
        sb.clear();
        sb.setNullText("NULL");
        sb.append((String) null, 0, 0);
        assertEquals("NULL", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStringBuffer() {
        StrBuilder sb = new StrBuilder();
        StringBuffer buf = new StringBuffer("test");
        sb.append(buf);
        assertEquals("test", sb.toString());

        sb.clear();
        sb.append((StringBuffer) null);
        assertEquals(0, sb.length());

        sb.append(buf, 1, 2);
        assertEquals("es", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStrBuilder() {
        StrBuilder sb1 = new StrBuilder("Hello ");
        StrBuilder sb2 = new StrBuilder("World");
        sb1.append(sb2);
        assertEquals("Hello World", sb1.toString());

        sb1.clear();
        sb1.append((StrBuilder) null);
        assertEquals(0, sb1.length());
    }

    @Test(timeout = 4000)
    public void testAppendCharArray() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[] {'a', 'b', 'c'});
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.append((char[]) null);
        assertEquals(0, sb.length());

        sb.append(new char[] {'x', 'y', 'z'}, 1, 1);
        assertEquals("y", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSetLength() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.setLength(3);
        assertEquals(3, sb.length());
        assertEquals("abc", sb.toString());

        sb.setLength(6);
        assertEquals(6, sb.length());
        assertEquals("abc\0\0\0", sb.toString());

        // negative throws
        try {
            sb.setLength(-1);
            fail("Expected StringIndexOutOfBoundsException");
        } catch (StringIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testClear() {
        StrBuilder sb = new StrBuilder("content");
        sb.clear();
        assertEquals(0, sb.length());
        assertTrue(sb.isEmpty());
        // capacity unchanged
        assertTrue(sb.capacity() >= "content".length() + 32);
    }

    @Test(timeout = 4000)
    public void testEnsureCapacity() {
        StrBuilder sb = new StrBuilder(10);
        int origCap = sb.capacity();
        sb.ensureCapacity(origCap + 100);
        assertTrue(sb.capacity() >= origCap + 100);
    }

    @Test(timeout = 4000)
    public void testMinimizeCapacity() {
        StrBuilder sb = new StrBuilder(200);
        sb.append("small");
        sb.minimizeCapacity();
        assertEquals(5, sb.capacity());
    }

    @Test(timeout = 4000)
    public void testCharAtAndSetCharAt() {
        StrBuilder sb = new StrBuilder("hello");
        assertEquals('h', sb.charAt(0));
        assertEquals('o', sb.charAt(4));

        sb.setCharAt(0, 'H');
        assertEquals("Hello", sb.toString());

        try {
            sb.charAt(-1);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }

        try {
            sb.charAt(10);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }

        try {
            sb.setCharAt(-1, 'X');
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testDeleteCharAt() {
        StrBuilder sb = new StrBuilder("abcd");
        sb.deleteCharAt(1);
        assertEquals("acd", sb.toString());

        try {
            sb.deleteCharAt(-1);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }

        try {
            sb.deleteCharAt(10);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }
    }

    // ====================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ====================================================================

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsArray() {
        StrBuilder sb = new StrBuilder();
        sb.appendWithSeparators(new Object[] {"a", "b", "c"}, ",");
        assertEquals("a,b,c", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new Object[0], ",");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators((Object[]) null, ",");
        assertEquals(0, sb.length());

        // null separator
        sb.clear();
        sb.appendWithSeparators(new Object[] {"x", "y"}, null);
        assertEquals("xy", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsCollection() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("one");
        list.add("two");
        sb.appendWithSeparators(list, "|");
        assertEquals("one|two", sb.toString());

        sb.clear();
        sb.appendWithSeparators(new java.util.ArrayList<String>(), ",");
        assertEquals(0, sb.length());

        sb.clear();
        sb.appendWithSeparators((java.util.Collection) null, ",");
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testAppendWithSeparatorsIterator() {
        StrBuilder sb = new StrBuilder();
        java.util.List<String> list = new java.util.ArrayList<String>();
        list.add("x");
        list.add("y");
        list.add("z");
        sb.appendWithSeparators(list.iterator(), "::");
        assertEquals("x::y::z", sb.toString());

        sb.clear();
        sb.appendWithSeparators((java.util.Iterator) null, ",");
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testAppendPadding() {
        StrBuilder sb = new StrBuilder();
        sb.appendPadding(5, '*');
        assertEquals("*****", sb.toString());

        // negative length -> no effect
        sb.clear();
        sb.appendPadding(-3, 'X');
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadLeft() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadLeft("abc", 6, '0');
        assertEquals("000abc", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadLeft("abcdef", 4, '0');
        assertEquals("cdef", sb.toString());

        // null object uses null text
        sb.clear();
        sb.setNullText("NUL");
        sb.appendFixedWidthPadLeft((Object) null, 5, ' ');
        assertEquals("  NUL", sb.toString());

        // zero width -> no effect
        sb.clear();
        sb.append("X");
        sb.appendFixedWidthPadLeft("abc", 0, '*');
        assertEquals("X", sb.toString());

        // int version
        sb.clear();
        sb.appendFixedWidthPadLeft(42, 4, '0');
        assertEquals("0042", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFixedWidthPadRight() {
        StrBuilder sb = new StrBuilder();
        sb.appendFixedWidthPadRight("abc", 6, '-');
        assertEquals("abc---", sb.toString());

        sb.clear();
        sb.appendFixedWidthPadRight("abcdef", 4, '-');
        assertEquals("abcd", sb.toString());

        sb.clear();
        sb.setNullText("NUL");
        sb.appendFixedWidthPadRight((Object) null, 5, '.');
        assertEquals("NUL..", sb.toString());

        // int version
        sb.clear();
        sb.appendFixedWidthPadRight(42, 5, ' ');
        assertEquals("42   ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSubstring() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertEquals("Hello", sb.substring(0, 5));
        assertEquals("World", sb.substring(6, 11));
        assertEquals("Hello World", sb.substring(0));

        // endIndex > size -> clamped
        assertEquals("Hello World", sb.substring(0, 100));

        try {
            sb.substring(-1, 5);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }

        try {
            sb.substring(5, 3);
            fail();
        } catch (StringIndexOutOfBoundsException e) { /* ok */ }
    }

    @Test(timeout = 4000)
    public void testLeftString() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("abc", sb.leftString(3));
        assertEquals("abcdef", sb.leftString(10));
        assertEquals("", sb.leftString(0));
        assertEquals("", sb.leftString(-1));
    }

    @Test(timeout = 4000)
    public void testRightString() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("def", sb.rightString(3));
        assertEquals("abcdef", sb.rightString(10));
        assertEquals("", sb.rightString(0));
        assertEquals("", sb.rightString(-1));
    }

    @Test(timeout = 4000)
    public void testMidString() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertEquals("bcd", sb.midString(1, 3));
        assertEquals("abcdef", sb.midString(0, 10));
        assertEquals("", sb.midString(-1, 3));
        assertEquals("", sb.midString(10, 3));
        assertEquals("", sb.midString(0, -1));
    }

    // ====================================================================
    // Partition C: Defect-Targeted Branch Zone - Bug testLang295
    // ====================================================================

    @Test(timeout = 4000)
    public void testContainsChar_OnlyScansSize_NotFullBuffer() {
        // This targets the known defect: contains(char) iterates over buffer.length
        // instead of size (which may be smaller). We need to construct a StrBuilder
        // where the buffer has extra capacity with leftover characters from previous
        // operations that are beyond the current size.
        StrBuilder sb = new StrBuilder(5);
        sb.append("abc"); // size=3, but has capacity 5
        // At this point buffer[3] and buffer[4] may still have zero chars,
        // but that doesn't prove the bug. We need to ensure there's a character
        // in the unused portion that should NOT be found.
        // Force the buffer to have a 'Z' at index 3 and 4 by doing tricks:
        sb.ensureCapacity(8);
        sb.append("def"); // now size=6, buffer index 0-5 are 'a','b','c','d','e','f'
        // Now delete the end to reduce size but keep buffer content beyond
        sb.setLength(3); // size=3, but buffer still contains 'd','e','f' at indices 3-5
        // contains('e') must return FALSE because 'e' is beyond size
        assertFalse("Bug: contains('e') should be false since 'e' is beyond size=3",
                     sb.contains('e'));
        assertFalse("Bug: contains('d') should be false since 'd' is beyond size=3",
                     sb.contains('d'));
        assertFalse("Bug: contains('f') should be false since 'f' is beyond size=3",
                     sb.contains('f'));
        // But contains('a') should still be true
        assertTrue("contains('a') should be true", sb.contains('a'));
        assertTrue("contains('b') should be true", sb.contains('b'));
        assertTrue("contains('c') should be true", sb.contains('c'));
    }

    // Additional tests to further expose the contains(char) bug
    @Test(timeout = 4000)
    public void testContainsChar_AfterDelete() {
        StrBuilder sb = new StrBuilder("Hello World");
        sb.delete(5, sb.length()); // now size=5, content="Hello"
        // buffer beyond size still has " World" -> contains('W') must be false
        assertFalse("contains('W') should be false after delete", sb.contains('W'));
        assertFalse("contains(' ') should be false after delete", sb.contains(' '));
        assertTrue("contains('H') should be true", sb.contains('H'));
        assertTrue("contains('o') should be true", sb.contains('o'));
    }

    @Test(timeout = 4000)
    public void testContainsChar_AfterReplaceShrink() {
        StrBuilder sb = new StrBuilder("abcdefgh");
        // Replace "cdef" with "X" -> size reduces from 8 to 5, content="abXgh"
        sb.replace(2, 6, "X");
        assertEquals("abXgh", sb.toString());
        // contains('d') and contains('e') must be false
        assertFalse("contains('d') must be false", sb.contains('d'));
        assertFalse("contains('e') must be false", sb.contains('e'));
        assertFalse("contains('f') must be false", sb.contains('f'));
        assertTrue("contains('X') must be true", sb.contains('X'));
    }

    // ====================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ====================================================================

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertInvalidIndex() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(-1, "X");
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertPastEnd() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(4, "X"); // index > size
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteInvalidRange() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(-1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testDeleteEndBeforeStart() {
        StrBuilder sb = new StrBuilder("abc");
        sb.delete(2, 1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubInvalidStart() {
        StrBuilder sb = new StrBuilder();
        sb.append("test", -1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendStringSubInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append("test", 2, 5); // startIndex+length > str.length()
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubInvalidStart() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[] {'a','b','c'}, -1, 1);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testAppendCharArraySubInvalidLength() {
        StrBuilder sb = new StrBuilder();
        sb.append(new char[] {'a','b','c'}, 1, 5);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubInvalidOffset() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, new char[] {'x','y','z'}, -1, 2);
    }

    @Test(timeout = 4000, expected = StringIndexOutOfBoundsException.class)
    public void testInsertCharArraySubInvalidLength() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, new char[] {'x','y','z'}, 0, 10);
    }

    // ====================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ====================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("Hello");
        StrBuilder sb3 = new StrBuilder("World");

        assertTrue(sb1.equals(sb2));
        assertFalse(sb1.equals(sb3));
        assertFalse(sb1.equals(null));
        assertFalse(sb1.equals("Hello"));

        assertEquals(sb1.hashCode(), sb2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsIgnoreCase() {
        StrBuilder sb1 = new StrBuilder("Hello");
        StrBuilder sb2 = new StrBuilder("HELLO");
        StrBuilder sb3 = new StrBuilder("World");

        assertTrue(sb1.equalsIgnoreCase(sb2));
        assertFalse(sb1.equalsIgnoreCase(sb3));
        assertTrue(sb1.equalsIgnoreCase(sb1));
        assertFalse(sb1.equalsIgnoreCase(null));
    }

    @Test(timeout = 4000)
    public void testClone() {
        StrBuilder sb = new StrBuilder("clone me");
        StrBuilder cloned = (StrBuilder) sb.clone();
        assertEquals(sb.toString(), cloned.toString());
        assertEquals(sb.length(), cloned.length());
        // Ensure independent
        cloned.append(" modified");
        assertFalse(sb.toString().equals(cloned.toString()));
    }

    // ====================================================================
    // Additional Branch Coverage Tests
    // ====================================================================

    @Test(timeout = 4000)
    public void testReverse() {
        StrBuilder sb = new StrBuilder("abcdef");
        sb.reverse();
        assertEquals("fedcba", sb.toString());

        sb.clear();
        sb.reverse(); // empty -> no change
        assertEquals(0, sb.length());

        sb.append("a");
        sb.reverse();
        assertEquals("a", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrim() {
        StrBuilder sb = new StrBuilder("  Hello World  ");
        sb.trim();
        assertEquals("Hello World", sb.toString());

        sb.clear();
        sb.trim(); // empty -> no change
        assertEquals(0, sb.length());

        sb.append("   ");
        sb.trim();
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testStartsWith() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.startsWith("Hello"));
        assertFalse(sb.startsWith("World"));
        assertFalse(sb.startsWith(null));
        assertTrue(sb.startsWith(""));
        assertFalse(sb.startsWith("Hello World!!!"));
    }

    @Test(timeout = 4000)
    public void testEndsWith() {
        StrBuilder sb = new StrBuilder("Hello World");
        assertTrue(sb.endsWith("World"));
        assertFalse(sb.endsWith("Hello"));
        assertFalse(sb.endsWith(null));
        assertTrue(sb.endsWith(""));
        assertFalse(sb.endsWith("Hello World!!!"));
    }

    @Test(timeout = 4000)
    public void testIndexOfString() {
        StrBuilder sb = new StrBuilder("banana");
        assertEquals(0, sb.indexOf("banana"));
        assertEquals(2, sb.indexOf("nana"));
        assertEquals(-1, sb.indexOf("apple"));
        assertEquals(-1, sb.indexOf((String) null));

        // startIndex clamped
        assertEquals(2, sb.indexOf("na", 1));
        assertEquals(-1, sb.indexOf("na", 100));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfString() {
        StrBuilder sb = new StrBuilder("banana");
        assertEquals(4, sb.lastIndexOf("na"));
        assertEquals(2, sb.lastIndexOf("na", 3));
        assertEquals(-1, sb.lastIndexOf("apple"));
        assertEquals(-1, sb.lastIndexOf((String) null));

        // empty string
        assertEquals(5, sb.lastIndexOf("", 5));
        assertEquals(0, sb.lastIndexOf("", 0));
    }

    @Test(timeout = 4000)
    public void testDeleteAllChar() {
        StrBuilder sb = new StrBuilder("aabbcc");
        sb.deleteAll('b');
        assertEquals("aacc", sb.toString());

        sb.clear();
        sb.append("aaaa");
        sb.deleteAll('a');
        assertEquals(0, sb.length());

        sb.clear();
        sb.append("xyz");
        sb.deleteAll('w');
        assertEquals("xyz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteFirstChar() {
        StrBuilder sb = new StrBuilder("aabbcc");
        sb.deleteFirst('b');
        assertEquals("aabcc", sb.toString());

        sb.clear();
        sb.append("xyz");
        sb.deleteFirst('w');
        assertEquals("xyz", sb.toString());
    }

    @Test(timeout = 4000)
    public void testDeleteAllString() {
        StrBuilder sb = new StrBuilder("abcabcabc");
        sb.deleteAll("abc");
        assertEquals(0, sb.length());

        sb.clear();
        sb.append("test");
        sb.deleteAll((String) null);
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllString() {
        StrBuilder sb = new StrBuilder("catcatcat");
        sb.replaceAll("cat", "dog");
        assertEquals("dogdogdog", sb.toString());

        sb.clear();
        sb.append("hello");
        sb.replaceAll("l", "LL");
        assertEquals("heLLLLo", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstString() {
        StrBuilder sb = new StrBuilder("catcatcat");
        sb.replaceFirst("cat", "dog");
        assertEquals("dogcatcat", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceAllChar() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.replaceAll('a', 'X');
        assertEquals("XbcXbc", sb.toString());

        // same char -> no change
        sb.clear();
        sb.append("test");
        sb.replaceAll('t', 't');
        assertEquals("test", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceFirstChar() {
        StrBuilder sb = new StrBuilder("abcabc");
        sb.replaceFirst('a', 'X');
        assertEquals("Xbcabc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReplaceImpl_Matcher() {
        StrBuilder sb = new StrBuilder("hello world");
        // Using a matcher that matches "lo"
        sb.replaceAll(new StrMatcher() {
            public int isMatch(char[] buffer, int pos, int start, int end) {
                if (pos + 2 <= end && buffer[pos] == 'l' && buffer[pos+1] == 'o') {
                    return 2;
                }
                return 0;
            }
        }, "XX");
        assertEquals("helXX world", sb.toString());
    }

    @Test(timeout = 4000)
    public void testInsertVariousTypes() {
        StrBuilder sb = new StrBuilder("abc");
        sb.insert(1, (Object) "XYZ");
        assertEquals("aXYZbc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, (char[]) null);
        assertEquals("abc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, true);
        assertEquals("atruebc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, false);
        assertEquals("afalsebc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, 'X');
        assertEquals("aXbc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, 42);
        assertEquals("a42bc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, 42L);
        assertEquals("a42bc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, 3.14f);
        assertEquals("a3.14bc", sb.toString());

        sb.clear();
        sb.append("abc");
        sb.insert(1, 2.718);
        assertEquals("a2.718bc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAsReader() throws Exception {
        StrBuilder sb = new StrBuilder("abcdef");
        java.io.Reader reader = sb.asReader();
        char[] buf = new char[10];
        int len = reader.read(buf);
        assertEquals(6, len);
        assertEquals("abcdef", new String(buf, 0, len));

        reader.close(); // no-op
    }

    @Test(timeout = 4000)
    public void testAsWriter() throws Exception {
        StrBuilder sb = new StrBuilder();
        java.io.Writer writer = sb.asWriter();
        writer.write("Hello");
        writer.write(' ');
        writer.write("World");
        writer.flush(); // no-op
        writer.close(); // no-op
        assertEquals("Hello World", sb.toString());
    }

    @Test(timeout = 4000)
    public void testSetNullTextEmpty() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("");
        // Should convert empty string to null
        assertNull(sb.getNullText());
        sb.append((String) null);
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testGetNewLineText() {
        StrBuilder sb = new StrBuilder();
        assertNull(sb.getNewLineText());
        sb.setNewLineText("\n");
        assertEquals("\n", sb.getNewLineText());
    }

    @Test(timeout = 4000)
    public void testAppendNewLine() {
        StrBuilder sb = new StrBuilder();
        sb.setNewLineText("\\n");
        sb.append("a");
        sb.appendNewLine();
        sb.append("b");
        assertEquals("a\\nb", sb.toString());

        // system default
        sb.clear();
        sb.setNewLineText(null);
        sb.appendNewLine();
        assertTrue(sb.length() > 0);
    }

    @Test(timeout = 4000)
    public void testAppendNullExplicit() {
        StrBuilder sb = new StrBuilder();
        sb.setNullText("nil");
        sb.appendNull();
        assertEquals("nil", sb.toString());

        sb.clear();
        sb.setNullText(null);
        sb.appendNull();
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testToStringBuffer() {
        StrBuilder sb = new StrBuilder("test");
        StringBuffer buf = sb.toStringBuffer();
        assertEquals("test", buf.toString());
    }

    @Test(timeout = 4000)
    public void testGetCharsArray() {
        StrBuilder sb = new StrBuilder("abc");
        char[] dest = sb.getChars(null);
        assertEquals(3, dest.length);
        assertArrayEquals(new char[] {'a','b','c'}, dest);

        // destination too small
        dest = new char[2];
        char[] result = sb.getChars(dest);
        assertNotSame(dest, result); // new array created because too small
        assertEquals(3, result.length);
    }

    @Test(timeout = 4000)
    public void testToCharArray() {
        StrBuilder sb = new StrBuilder();
        assertArrayEquals(new char[0], sb.toCharArray());

        sb.append("abc");
        assertArrayEquals(new char[] {'a','b','c'}, sb.toCharArray());
    }

    @Test(timeout = 4000)
    public void testToCharArrayRange() {
        StrBuilder sb = new StrBuilder("abcdef");
        assertArrayEquals(new char[] {'b','c','d'}, sb.toCharArray(1, 4));
        assertArrayEquals(new char[0], sb.toCharArray(0, 0));
    }
}