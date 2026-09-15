package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.codec.binary.StringUtils
 * 
 * Known Defect: StringIndexOutOfBoundsException in equals(CharSequence, CharSequence)
 * when comparing CharSequences of different lengths where the shorter is not a String
 * (e.g., StringBuilder vs String). The bug occurs because regionMatches is called with
 * Math.max(cs1.length(), cs2.length()) as the length parameter, which can exceed the
 * shorter sequence's length.
 * 
 * Branch Coverage Targets:
 * - equals: cs1 == cs2 (true/false)
 * - equals: cs1 == null || cs2 == null (true/false)
 * - equals: cs1 instanceof String && cs2 instanceof String (true/false)
 * - equals: regionMatches path with equal/different lengths
 * - getBytes: string == null (true/false)
 * - getByteBuffer: null/non-null string
 * - getBytesUnchecked: normal path, UnsupportedEncodingException path
 * - newString: bytes == null (true/false)
 * - newString: UnsupportedEncodingException path
 * - All charset-specific methods (null/non-null inputs)
 * 
 * Boundary Values:
 * - null inputs for all methods
 * - Empty strings/byte arrays
 * - Different length CharSequences (String vs StringBuilder)
 * - Unicode characters (surrogate pairs, multi-byte)
 * - Max/min byte values
 */
public class StringUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        String s = "test";
        assertTrue(StringUtils.equals(s, s));
    }

    @Test(timeout = 4000)
    public void testEqualsBothNull() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test(timeout = 4000)
    public void testEqualsFirstNull() {
        assertFalse(StringUtils.equals(null, "test"));
    }

    @Test(timeout = 4000)
    public void testEqualsSecondNull() {
        assertFalse(StringUtils.equals("test", null));
    }

    @Test(timeout = 4000)
    public void testEqualsBothStringsEqual() {
        assertTrue(StringUtils.equals("hello", "hello"));
    }

    @Test(timeout = 4000)
    public void testEqualsBothStringsDifferent() {
        assertFalse(StringUtils.equals("hello", "world"));
    }

    @Test(timeout = 4000)
    public void testEqualsCaseSensitive() {
        assertFalse(StringUtils.equals("Hello", "hello"));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptyStrings() {
        assertTrue(StringUtils.equals("", ""));
    }

    @Test(timeout = 4000)
    public void testEqualsStringAndStringBuilderEqual() {
        // This should work correctly - both are CharSequences
        assertTrue(StringUtils.equals("test", new StringBuilder("test")));
    }

    @Test(timeout = 4000)
    public void testEqualsStringAndStringBuilderDifferent() {
        assertFalse(StringUtils.equals("test", new StringBuilder("other")));
    }

    @Test(timeout = 4000)
    public void testEqualsStringBuilderAndStringEqual() {
        assertTrue(StringUtils.equals(new StringBuilder("test"), "test"));
    }

    @Test(timeout = 4000)
    public void testEqualsStringBuilderAndStringDifferent() {
        assertFalse(StringUtils.equals(new StringBuilder("test"), "other"));
    }

    @Test(timeout = 4000)
    public void testEqualsStringBufferAndStringEqual() {
        assertTrue(StringUtils.equals(new StringBuffer("test"), "test"));
    }

    @Test(timeout = 4000)
    public void testEqualsStringBufferAndStringDifferent() {
        assertFalse(StringUtils.equals(new StringBuffer("test"), "other"));
    }

    @Test(timeout = 4000)
    public void testEqualsCustomCharSequenceEqual() {
        CharSequence cs1 = new CustomCharSequence("test");
        CharSequence cs2 = new CustomCharSequence("test");
        assertTrue(StringUtils.equals(cs1, cs2));
    }

    @Test(timeout = 4000)
    public void testEqualsCustomCharSequenceDifferent() {
        CharSequence cs1 = new CustomCharSequence("test");
        CharSequence cs2 = new CustomCharSequence("other");
        assertFalse(StringUtils.equals(cs1, cs2));
    }

    @Test(timeout = 4000)
    public void testEqualsStringAndCustomCharSequenceEqual() {
        assertTrue(StringUtils.equals("test", new CustomCharSequence("test")));
    }

    @Test(timeout = 4000)
    public void testEqualsCustomCharSequenceAndStringEqual() {
        assertTrue(StringUtils.equals(new CustomCharSequence("test"), "test"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEqualsNullAndEmpty() {
        assertFalse(StringUtils.equals(null, ""));
        assertFalse(StringUtils.equals("", null));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptyAndNonEmpty() {
        assertFalse(StringUtils.equals("", "a"));
        assertFalse(StringUtils.equals("a", ""));
    }

    @Test(timeout = 4000)
    public void testEqualsSingleCharacter() {
        assertTrue(StringUtils.equals("a", "a"));
        assertFalse(StringUtils.equals("a", "b"));
    }

    @Test(timeout = 4000)
    public void testEqualsUnicodeCharacters() {
        assertTrue(StringUtils.equals("héllo", "héllo"));
        assertFalse(StringUtils.equals("héllo", "hello"));
    }

    @Test(timeout = 4000)
    public void testEqualsSurrogatePairs() {
        String emoji = "\uD83D\uDE00"; // 😀
        assertTrue(StringUtils.equals(emoji, emoji));
        assertFalse(StringUtils.equals(emoji, "\uD83D\uDE01")); // 😁
    }

    @Test(timeout = 4000)
    public void testEqualsLongStrings() {
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb1.append('a');
            sb2.append('a');
        }
        assertTrue(StringUtils.equals(sb1.toString(), sb2.toString()));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengths() {
        assertFalse(StringUtils.equals("a", "ab"));
        assertFalse(StringUtils.equals("ab", "a"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsStringBuilder() {
        // This is the defect-triggering scenario
        assertFalse(StringUtils.equals(new StringBuilder("a"), "ab"));
        assertFalse(StringUtils.equals("ab", new StringBuilder("a")));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8Null() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8Empty() {
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8Ascii() {
        assertArrayEquals(new byte[]{97, 98, 99}, StringUtils.getBytesUtf8("abc"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8Unicode() {
        assertArrayEquals(new byte[]{(byte)0xC3, (byte)0xA9}, StringUtils.getBytesUtf8("é"));
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1Null() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1() {
        assertArrayEquals(new byte[]{97, 98, 99}, StringUtils.getBytesIso8859_1("abc"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAsciiNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAscii() {
        assertArrayEquals(new byte[]{97, 98, 99}, StringUtils.getBytesUsAscii("abc"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16Null() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16() {
        byte[] expected = new byte[]{(byte)0xFE, (byte)0xFF, 0, 97, 0, 98};
        assertArrayEquals(expected, StringUtils.getBytesUtf16("ab"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16BeNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16Be() {
        byte[] expected = new byte[]{0, 97, 0, 98};
        assertArrayEquals(expected, StringUtils.getBytesUtf16Be("ab"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16LeNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16Le() {
        byte[] expected = new byte[]{97, 0, 98, 0};
        assertArrayEquals(expected, StringUtils.getBytesUtf16Le("ab"));
    }

    @Test(timeout = 4000)
    public void testGetByteBufferUtf8() {
        ByteBuffer buffer = StringUtils.getByteBufferUtf8("abc");
        assertNotNull(buffer);
        assertEquals(3, buffer.capacity());
        assertEquals(97, buffer.get(0));
        assertEquals(98, buffer.get(1));
        assertEquals(99, buffer.get(2));
    }

    @Test(timeout = 4000)
    public void testGetByteBufferUtf8Empty() {
        ByteBuffer buffer = StringUtils.getByteBufferUtf8("");
        assertNotNull(buffer);
        assertEquals(0, buffer.capacity());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * CRITICAL DEFECT TEST: This test targets the StringIndexOutOfBoundsException
     * that occurs when comparing CharSequences of different lengths where the
     * shorter sequence is not a String instance.
     * 
     * The bug: regionMatches is called with Math.max(cs1.length(), cs2.length())
     * as the length parameter, which exceeds the shorter sequence's length.
     * 
     * Expected behavior: equals should return false for different-length sequences
     * without throwing an exception.
     */
    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsStringBuilderAndString() {
        // This triggers the defect: StringBuilder (length 1) vs String (length 2)
        assertFalse(StringUtils.equals(new StringBuilder("a"), "ab"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsStringAndStringBuilder() {
        // This triggers the defect: String (length 2) vs StringBuilder (length 1)
        assertFalse(StringUtils.equals("ab", new StringBuilder("a")));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsStringBufferAndString() {
        // StringBuffer is also a CharSequence that is not a String
        assertFalse(StringUtils.equals(new StringBuffer("a"), "ab"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsCustomCharSequenceAndString() {
        // Custom CharSequence with different length
        assertFalse(StringUtils.equals(new CustomCharSequence("a"), "ab"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsStringAndCustomCharSequence() {
        assertFalse(StringUtils.equals("ab", new CustomCharSequence("a")));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsBothNonString() {
        // Both are non-String CharSequences with different lengths
        assertFalse(StringUtils.equals(new StringBuilder("a"), new StringBuilder("ab")));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLengthsBothCustom() {
        assertFalse(StringUtils.equals(new CustomCharSequence("a"), new CustomCharSequence("ab")));
    }

    @Test(timeout = 4000)
    public void testEqualsSameLengthNonStringDifferentContent() {
        // Same length but different content - should not throw
        assertFalse(StringUtils.equals(new StringBuilder("ab"), new StringBuilder("cd")));
    }

    @Test(timeout = 4000)
    public void testEqualsSameLengthNonStringSameContent() {
        // Same length and same content - should return true
        assertTrue(StringUtils.equals(new StringBuilder("ab"), new StringBuilder("ab")));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testGetBytesUncheckedNullString() {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedValidCharset() {
        assertArrayEquals(new byte[]{97, 98, 99}, StringUtils.getBytesUnchecked("abc", "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedUnsupportedCharset() {
        try {
            StringUtils.getBytesUnchecked("abc", "UNSUPPORTED_CHARSET");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
            assertTrue(e.getMessage().contains("UNSUPPORTED_CHARSET"));
        }
    }

    @Test(timeout = 4000)
    public void testNewStringNullBytes() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNewStringValidBytes() {
        assertEquals("abc", StringUtils.newString(new byte[]{97, 98, 99}, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNewStringUnsupportedCharset() {
        try {
            StringUtils.newString(new byte[]{97}, "UNSUPPORTED_CHARSET");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // Expected
            assertTrue(e.getMessage().contains("UNSUPPORTED_CHARSET"));
        }
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1Null() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1() {
        assertEquals("abc", StringUtils.newStringIso8859_1(new byte[]{97, 98, 99}));
    }

    @Test(timeout = 4000)
    public void testNewStringUsAsciiNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUsAscii() {
        assertEquals("abc", StringUtils.newStringUsAscii(new byte[]{97, 98, 99}));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16Null() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16() {
        byte[] bytes = new byte[]{(byte)0xFE, (byte)0xFF, 0, 97, 0, 98};
        assertEquals("ab", StringUtils.newStringUtf16(bytes));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16BeNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16Be() {
        byte[] bytes = new byte[]{0, 97, 0, 98};
        assertEquals("ab", StringUtils.newStringUtf16Be(bytes));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16LeNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16Le() {
        byte[] bytes = new byte[]{97, 0, 98, 0};
        assertEquals("ab", StringUtils.newStringUtf16Le(bytes));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8Null() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8() {
        assertEquals("abc", StringUtils.newStringUtf8(new byte[]{97, 98, 99}));
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8Unicode() {
        byte[] bytes = new byte[]{(byte)0xC3, (byte)0xA9};
        assertEquals("é", StringUtils.newStringUtf8(bytes));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsConsistency() {
        // equals should be consistent across multiple calls
        String s1 = "test";
        String s2 = "test";
        assertTrue(StringUtils.equals(s1, s2));
        assertTrue(StringUtils.equals(s1, s2));
        assertTrue(StringUtils.equals(s1, s2));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        // equals should be symmetric
        String s1 = "test";
        StringBuilder sb = new StringBuilder("test");
        assertTrue(StringUtils.equals(s1, sb));
        assertTrue(StringUtils.equals(sb, s1));
    }

    @Test(timeout = 4000)
    public void testEqualsTransitive() {
        // equals should be transitive
        String s1 = "test";
        StringBuilder sb1 = new StringBuilder("test");
        StringBuilder sb2 = new StringBuilder("test");
        assertTrue(StringUtils.equals(s1, sb1));
        assertTrue(StringUtils.equals(sb1, sb2));
        assertTrue(StringUtils.equals(s1, sb2));
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTrip() {
        // Round-trip test: String -> bytes -> String
        String original = "Hello, World!";
        byte[] bytes = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripUnicode() {
        // Round-trip with Unicode characters
        String original = "héllo wörld";
        byte[] bytes = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripEmpty() {
        // Round-trip with empty string
        String original = "";
        byte[] bytes = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripIso8859_1() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesIso8859_1(original);
        String decoded = StringUtils.newStringIso8859_1(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripUsAscii() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesUsAscii(original);
        String decoded = StringUtils.newStringUsAscii(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripUtf16() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesUtf16(original);
        String decoded = StringUtils.newStringUtf16(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripUtf16Be() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesUtf16Be(original);
        String decoded = StringUtils.newStringUtf16Be(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesRoundTripUtf16Le() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesUtf16Le(original);
        String decoded = StringUtils.newStringUtf16Le(bytes);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedRoundTrip() {
        String original = "Hello";
        byte[] bytes = StringUtils.getBytesUnchecked(original, "UTF-8");
        String decoded = StringUtils.newString(bytes, "UTF-8");
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testNewStringWithNullCharset() {
        // newString with null charset should throw NullPointerException
        try {
            StringUtils.newString(new byte[]{97}, (Charset) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBytesWithNullCharset() {
        // getBytes with null charset should throw NullPointerException
        try {
            StringUtils.getBytes("test", (Charset) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // Helper class for testing custom CharSequence implementations
    private static class CustomCharSequence implements CharSequence {
        private final String value;

        CustomCharSequence(String value) {
            this.value = value;
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }
}