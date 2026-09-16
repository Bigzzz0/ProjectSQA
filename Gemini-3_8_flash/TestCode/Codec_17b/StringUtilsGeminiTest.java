package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import org.apache.commons.codec.CharEncoding;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.codec.binary.StringUtils
 *
 * Decision / Branch Matrix:
 * 1. equals(CharSequence cs1, CharSequence cs2):
 *    - Branch: cs1 == cs2 (both null -> true, same reference -> true)
 *    - Branch: cs1 == null || cs2 == null (one null -> false)
 *    - Branch: cs1 instanceof String && cs2 instanceof String:
 *      * True branch -> cs1.equals(cs2) (equal strings -> true, unequal strings -> false)
 *      * False branch -> CharSequenceUtils.regionMatches fallback:
 *        - String vs StringBuilder (equal & non-equal)
 *        - StringBuilder vs StringBuilder (equal & non-equal)
 *        - Different lengths comparison
 * 2. getBytes(String, Charset) via wrappers:
 *    - string == null -> returns null
 *    - string != null -> returns byte[]
 *    - Covers: getBytesIso8859_1, getBytesUsAscii, getBytesUtf16, getBytesUtf16Be,
 *              getBytesUtf16Le, getBytesUtf8
 * 3. getByteBuffer(String, Charset) via getByteBufferUtf8:
 *    - string == null -> returns null
 *    - string != null -> returns ByteBuffer with correct capacity and content
 * 4. getBytesUnchecked(String, String):
 *    - string == null -> returns null
 *    - string != null, valid charset -> returns byte[]
 *    - string != null, invalid charset -> throws IllegalStateException wrapping UnsupportedEncodingException
 * 5. newString(byte[], String):
 *    - bytes == null -> returns null
 *    - bytes != null, valid charset -> returns String
 *    - bytes != null, invalid charset -> throws IllegalStateException wrapping UnsupportedEncodingException
 * 6. newString*(byte[]):
 *    - bytes == null -> returns null (Defect target: CODEC-229 in newStringIso8859_1)
 *    - bytes != null -> returns correctly decoded String
 *    - Covers: newStringIso8859_1, newStringUsAscii, newStringUtf16, newStringUtf16Be,
 *              newStringUtf16Le, newStringUtf8
 * 7. StringUtils Constructor:
 *    - Public constructor invocation for full line coverage
 *
 * Known Defect Target:
 * - CODEC-229: newStringIso8859_1(null) throws NullPointerException because it directly invokes
 *   new String(bytes, Charsets.ISO_8859_1) rather than the null-safe helper newString(bytes, ...).
 */
public class StringUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsIdenticalStrings() {
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
        assertFalse(StringUtils.equals("abc", "def"));
    }

    @Test(timeout = 4000)
    public void testEqualsCharSequences() {
        final StringBuilder sb1 = new StringBuilder("testCharSequence");
        final StringBuilder sb2 = new StringBuilder("testCharSequence");
        final StringBuilder sbDiff = new StringBuilder("testDifferentSequence");
        final String str = "testCharSequence";

        // Non-String CharSequence vs Non-String CharSequence
        assertTrue(StringUtils.equals(sb1, sb2));
        assertFalse(StringUtils.equals(sb1, sbDiff));

        // String vs StringBuilder
        assertTrue(StringUtils.equals(str, sb1));
        assertTrue(StringUtils.equals(sb1, str));
        assertFalse(StringUtils.equals(str, sbDiff));

        // Different lengths
        assertFalse(StringUtils.equals("short", new StringBuilder("longerSequence")));
        assertFalse(StringUtils.equals(new StringBuilder("longerSequence"), "short"));
    }

    @Test(timeout = 4000)
    public void testGetBytesEncodings() {
        final String sample = "Hello World! \u00e9\u00e0";

        assertNotNull(StringUtils.getBytesIso8859_1(sample));
        assertNotNull(StringUtils.getBytesUsAscii("Hello ASCII"));
        assertNotNull(StringUtils.getBytesUtf16(sample));
        assertNotNull(StringUtils.getBytesUtf16Be(sample));
        assertNotNull(StringUtils.getBytesUtf16Le(sample));
        assertNotNull(StringUtils.getBytesUtf8(sample));
    }

    @Test(timeout = 4000)
    public void testNewStringEncodings() {
        final String sample = "Roundtrip Test 123";

        final byte[] isoBytes = StringUtils.getBytesIso8859_1(sample);
        assertEquals(sample, StringUtils.newStringIso8859_1(isoBytes));

        final byte[] asciiBytes = StringUtils.getBytesUsAscii(sample);
        assertEquals(sample, StringUtils.newStringUsAscii(asciiBytes));

        final byte[] utf16Bytes = StringUtils.getBytesUtf16(sample);
        assertEquals(sample, StringUtils.newStringUtf16(utf16Bytes));

        final byte[] utf16BeBytes = StringUtils.getBytesUtf16Be(sample);
        assertEquals(sample, StringUtils.newStringUtf16Be(utf16BeBytes));

        final byte[] utf16LeBytes = StringUtils.getBytesUtf16Le(sample);
        assertEquals(sample, StringUtils.newStringUtf16Le(utf16LeBytes));

        final byte[] utf8Bytes = StringUtils.getBytesUtf8(sample);
        assertEquals(sample, StringUtils.newStringUtf8(utf8Bytes));
    }

    @Test(timeout = 4000)
    public void testGetByteBufferUtf8() {
        final String text = "BufferText";
        final ByteBuffer buffer = StringUtils.getByteBufferUtf8(text);
        assertNotNull(buffer);
        final byte[] expected = StringUtils.getBytesUtf8(text);
        assertArrayEquals(expected, buffer.array());
    }

    @Test(timeout = 4000)
    public void testGetAndNewStringCustomCharset() {
        final String text = "Testing Custom Charset";
        final byte[] bytes = StringUtils.getBytesUnchecked(text, CharEncoding.UTF_8);
        assertNotNull(bytes);
        final String decoded = StringUtils.newString(bytes, CharEncoding.UTF_8);
        assertEquals(text, decoded);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsNullHandling() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertFalse(StringUtils.equals(null, new StringBuilder("abc")));
        assertFalse(StringUtils.equals(new StringBuilder("abc"), null));
    }

    @Test(timeout = 4000)
    public void testEqualsSameReference() {
        final String str = "sameRef";
        final StringBuilder sb = new StringBuilder("sameRef");
        assertTrue(StringUtils.equals(str, str));
        assertTrue(StringUtils.equals(sb, sb));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptySequences() {
        assertTrue(StringUtils.equals("", ""));
        assertTrue(StringUtils.equals("", new StringBuilder("")));
        assertTrue(StringUtils.equals(new StringBuilder(""), new StringBuilder("")));
        assertFalse(StringUtils.equals("", "nonEmpty"));
        assertFalse(StringUtils.equals(new StringBuilder(""), "nonEmpty"));
    }

    @Test(timeout = 4000)
    public void testGetBytesNullInput() {
        assertNull(StringUtils.getBytesIso8859_1(null));
        assertNull(StringUtils.getBytesUsAscii(null));
        assertNull(StringUtils.getBytesUtf16(null));
        assertNull(StringUtils.getBytesUtf16Be(null));
        assertNull(StringUtils.getBytesUtf16Le(null));
        assertNull(StringUtils.getBytesUtf8(null));
        assertNull(StringUtils.getBytesUnchecked(null, CharEncoding.UTF_8));
        assertNull(StringUtils.getByteBufferUtf8(null));
    }

    @Test(timeout = 4000)
    public void testGetBytesEmptyInput() {
        assertArrayEquals(new byte[0], StringUtils.getBytesIso8859_1(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Be(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Le(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUnchecked("", CharEncoding.UTF_8));

        final ByteBuffer buffer = StringUtils.getByteBufferUtf8("");
        assertNotNull(buffer);
        assertEquals(0, buffer.remaining());
    }

    @Test(timeout = 4000)
    public void testNewStringNullInputExceptBuggyIso() {
        assertNull(StringUtils.newString(null, CharEncoding.UTF_8));
        assertNull(StringUtils.newStringUsAscii(null));
        assertNull(StringUtils.newStringUtf16(null));
        assertNull(StringUtils.newStringUtf16Be(null));
        assertNull(StringUtils.newStringUtf16Le(null));
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test(timeout = 4000)
    public void testNewStringEmptyInput() {
        final byte[] empty = new byte[0];
        assertEquals("", StringUtils.newString(empty, CharEncoding.UTF_8));
        assertEquals("", StringUtils.newStringIso8859_1(empty));
        assertEquals("", StringUtils.newStringUsAscii(empty));
        assertEquals("", StringUtils.newStringUtf16(empty));
        assertEquals("", StringUtils.newStringUtf16Be(empty));
        assertEquals("", StringUtils.newStringUtf16Le(empty));
        assertEquals("", StringUtils.newStringUtf8(empty));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-229)
    // =========================================================================

    /**
     * Targets CODEC-229: StringUtils.newStringIso8859_1(null) throws NullPointerException
     * instead of returning null as specified in the Javadoc contract.
     */
    @Test(timeout = 4000)
    public void testNewStringNullInput_CODEC229() {
        assertNull("StringUtils.newStringIso8859_1(null) should return null",
                StringUtils.newStringIso8859_1(null));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetBytesUncheckedInvalidCharset() {
        final String invalidCharset = "INVALID_CHARSET_NAME_12345";
        try {
            StringUtils.getBytesUnchecked("Hello", invalidCharset);
            fail("Expected IllegalStateException for invalid charset");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains(invalidCharset));
            assertNotNull(e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testNewStringInvalidCharset() {
        final String invalidCharset = "INVALID_CHARSET_NAME_12345";
        try {
            StringUtils.newString(new byte[]{0x41, 0x42}, invalidCharset);
            fail("Expected IllegalStateException for invalid charset");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains(invalidCharset));
            assertNotNull(e.getCause());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringUtilsConstructor() {
        final StringUtils instance = new StringUtils();
        assertNotNull(instance);
    }
}