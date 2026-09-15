package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.io.UnsupportedEncodingException;

/**
 * White-box JUnit 4 test suite for StringUtils.
 * Targets line/branch coverage and the known defect CODEC229 (NPE on null input to newStringIso8859_1).
 *
 * [Branch & Defect Analysis Matrix]
 * 1. equals(): 
 *    - cs1 == cs2 (same reference) -> true
 *    - cs1 == null or cs2 == null -> false
 *    - both String instances -> String.equals()
 *    - otherwise (e.g., StringBuilder) -> regionMatches with max length
 *    - Case-sensitive, handles null without exception.
 * 2. getBytes* (private): null string -> return null; non-null returns bytes.
 * 3. getByteBuffer*: null -> return null; non-null -> ByteBuffer.
 * 4. getBytesUnchecked: null string -> null; valid charset -> bytes; invalid charset -> IllegalStateException.
 * 5. newString(byte[], Charset): null bytes -> null; non-null -> String.
 * 6. newString(byte[], String): null -> null; valid charset -> String; invalid -> IllegalStateException.
 * 7. Public newString* methods:
 *    - Most delegate to private newString, safe for null.
 *    - newStringIso8859_1 does NOT check for null -> throws NPE (defect). 
 *      Correct behavior: should return null for null input.
 * 8. newStringIso8859_1: only one that bypasses null check -> test explicitly.
 */
public class StringUtilsDeepseekTest {

    // ======================== Partition A: Core Functional Logic & State Transitions ========================

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        String a = "test";
        assertTrue("Same reference should be equal", StringUtils.equals(a, a));
    }

    @Test(timeout = 4000)
    public void testEqualsBothNull() {
        assertTrue("Both null should be equal", StringUtils.equals(null, null));
    }

    @Test(timeout = 4000)
    public void testEqualsFirstNull() {
        assertFalse("First null should return false", StringUtils.equals(null, "abc"));
    }

    @Test(timeout = 4000)
    public void testEqualsSecondNull() {
        assertFalse("Second null should return false", StringUtils.equals("abc", null));
    }

    @Test(timeout = 4000)
    public void testEqualsCaseSensitive() {
        assertTrue("Equal strings (case sensitive)", StringUtils.equals("abc", "abc"));
        assertFalse("Different case should be unequal", StringUtils.equals("abc", "ABC"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithStringBuilder() {
        // regionMatches path when not both Strings
        CharSequence cs1 = new StringBuilder("hello");
        CharSequence cs2 = new StringBuilder("hello");
        assertTrue("StringBuilders with same content should be equal", StringUtils.equals(cs1, cs2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithMixedTypes() {
        CharSequence cs1 = "hello";
        CharSequence cs2 = new StringBuilder("hello");
        assertTrue("String and StringBuilder with same content should be equal", StringUtils.equals(cs1, cs2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLength() {
        assertFalse("Different lengths should be unequal", StringUtils.equals("abc", "abcd"));
    }

    // ======================== Partition B: Boundary Value Analysis & Extremes ========================

    @Test(timeout = 4000)
    public void testGetBytesUncheckedNullString() {
        assertNull("getBytesUnchecked with null string should return null",
                StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedValidCharset() {
        byte[] expected = "test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals("getBytesUnchecked with valid charset",
                expected, StringUtils.getBytesUnchecked("test", "UTF-8"));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetBytesUncheckedInvalidCharset() {
        StringUtils.getBytesUnchecked("test", "INVALID_CHARSET");
    }

    @Test(timeout = 4000)
    public void testGetByteBufferUtf8Null() {
        assertNull("getByteBufferUtf8 with null string should return null",
                StringUtils.getByteBufferUtf8(null));
    }

    @Test(timeout = 4000)
    public void testGetByteBufferUtf8NonNull() {
        ByteBuffer buf = StringUtils.getByteBufferUtf8("abc");
        assertNotNull("getByteBufferUtf8 should return non-null for non-null input", buf);
        byte[] expected = "abc".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] actual = new byte[buf.remaining()];
        buf.get(actual);
        assertArrayEquals("ByteBuffer content should match UTF-8 bytes", expected, actual);
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1Null() {
        assertNull("getBytesIso8859_1 with null string should return null",
                StringUtils.getBytesIso8859_1(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1NonNull() {
        byte[] expected = "abc".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        assertArrayEquals("getBytesIso8859_1", expected, StringUtils.getBytesIso8859_1("abc"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAsciiNull() {
        assertNull("getBytesUsAscii with null string should return null",
                StringUtils.getBytesUsAscii(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16Null() {
        assertNull("getBytesUtf16 with null string", StringUtils.getBytesUtf16(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16BeNull() {
        assertNull("getBytesUtf16Be with null string", StringUtils.getBytesUtf16Be(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16LeNull() {
        assertNull("getBytesUtf16Le with null string", StringUtils.getBytesUtf16Le(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8Null() {
        assertNull("getBytesUtf8 with null string", StringUtils.getBytesUtf8(null));
    }

    // ======================== Partition C: Defect-Targeted Branch Zone (CODEC229) ========================

    @Test(timeout = 4000)
    public void testNewStringNullInput_CODEC229() {
        // This test targets the known defect: newStringIso8859_1(null) should return null
        // but currently throws NullPointerException.
        // Expect null; if bug exists, test fails with NPE.
        assertNull("newStringIso8859_1(null) should return null (defect CODEC229)",
                StringUtils.newStringIso8859_1(null));
    }

    // Additional safety tests for other newString methods with null input
    @Test(timeout = 4000)
    public void testNewStringUsAsciiNull() {
        assertNull("newStringUsAscii(null)", StringUtils.newStringUsAscii(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16Null() {
        assertNull("newStringUtf16(null)", StringUtils.newStringUtf16(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16BeNull() {
        assertNull("newStringUtf16Be(null)", StringUtils.newStringUtf16Be(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16LeNull() {
        assertNull("newStringUtf16Le(null)", StringUtils.newStringUtf16Le(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8Null() {
        assertNull("newStringUtf8(null)", StringUtils.newStringUtf8(null));
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(timeout = 4000)
    public void testNewStringBytesCharsetNull() {
        // private newString(byte[], Charset) – tested via public method that delegates
        assertNull("newString(byte[], Charset) with null bytes", 
                StringUtils.newStringUsAscii(null));
    }

    @Test(timeout = 4000)
    public void testNewStringBytesCharsetNonNull() {
        byte[] bytes = "abc".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals("newStringUsAscii", "abc", StringUtils.newStringUsAscii(bytes));
    }

    @Test(timeout = 4000)
    public void testNewStringBytesStringNull() {
        assertNull("newString(byte[], String) with null bytes",
                StringUtils.newString(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNewStringBytesStringValid() {
        byte[] bytes = "abc".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("newString(byte[], String) valid", "abc", 
                StringUtils.newString(bytes, "UTF-8"));
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNewStringBytesStringInvalidCharset() {
        byte[] bytes = "abc".getBytes();
        StringUtils.newString(bytes, "INVALID_CHARSET");
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1NonNull() {
        byte[] bytes = "abc".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        assertEquals("newStringIso8859_1 non-null", "abc", 
                StringUtils.newStringIso8859_1(bytes));
    }

    // Test that newStringIso8859_1 with empty byte array returns empty string
    @Test(timeout = 4000)
    public void testNewStringIso8859_1Empty() {
        byte[] bytes = new byte[0];
        assertEquals("newStringIso8859_1 empty", "", 
                StringUtils.newStringIso8859_1(bytes));
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================
    // Not applicable for static utility class; no state.

    // Additional coverage for edge cases in equals

    @Test(timeout = 4000)
    public void testEqualsEmptyStrings() {
        assertTrue("Empty strings equal", StringUtils.equals("", ""));
    }

    @Test(timeout = 4000)
    public void testEqualsWithEmptyAndNull() {
        assertFalse("Empty vs null", StringUtils.equals("", null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithWhitespace() {
        assertTrue("Whitespace same", StringUtils.equals("  ", "  "));
        assertFalse("Whitespace different", StringUtils.equals(" ", "  "));
    }

    // RegionMatches path with different lengths
    @Test(timeout = 4000)
    public void testEqualsRegionMatchesDifferentLength() {
        CharSequence cs1 = new StringBuilder("abc");
        CharSequence cs2 = new StringBuilder("abcd");
        assertFalse("StringBuilder different lengths", StringUtils.equals(cs1, cs2));
    }

    @Test(timeout = 4000)
    public void testEqualsRegionMatchesSameLengthDiffContent() {
        CharSequence cs1 = new StringBuilder("abc");
        CharSequence cs2 = new StringBuilder("abd");
        assertFalse("StringBuilder different content", StringUtils.equals(cs1, cs2));
    }

    // Test public getBytes methods with non-null values to ensure coverage
    @Test(timeout = 4000)
    public void testGetBytesUtf16NonNull() {
        byte[] bytes = StringUtils.getBytesUtf16("a");
        assertNotNull(bytes);
        assertTrue("UTF-16 bytes length > 0", bytes.length > 0);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16BeNonNull() {
        byte[] bytes = StringUtils.getBytesUtf16Be("a");
        assertNotNull(bytes);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16LeNonNull() {
        byte[] bytes = StringUtils.getBytesUtf16Le("a");
        assertNotNull(bytes);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8NonNull() {
        byte[] bytes = StringUtils.getBytesUtf8("a");
        assertArrayEquals("UTF-8 'a'", new byte[]{97}, bytes);
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAsciiNonNull() {
        byte[] bytes = StringUtils.getBytesUsAscii("a");
        assertArrayEquals("US-ASCII 'a'", new byte[]{97}, bytes);
    }

    // String with special characters to trigger non-Latin1 encoding paths
    @Test(timeout = 4000)
    public void testGetBytesUtf8SpecialChars() {
        byte[] bytes = StringUtils.getBytesUtf8("\u00E9"); // é
        assertArrayEquals("UTF-8 é", new byte[]{(byte)0xC3, (byte)0xA9}, bytes);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8SpecialChars() {
        byte[] bytes = new byte[]{(byte)0xC3, (byte)0xA9};
        assertEquals("newStringUtf8 é", "\u00E9", StringUtils.newStringUtf8(bytes));
    }
}