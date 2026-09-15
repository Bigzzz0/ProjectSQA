package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: StringUtils.java (commons-codec)
 * 
 * Partition A - Core Functional Logic & State Transitions:
 * - getBytes* methods with valid non-null strings
 * - newString* methods with valid non-null byte arrays
 * - getBytesUnchecked with valid charset name
 * - newString(bytes, charsetName) with valid charset name
 * 
 * Partition B - Boundary Value Analysis & Extremes:
 * - null string input to all getBytes* methods -> expected null
 * - null byte array input to all newString* methods -> expected null
 * - empty string "" to getBytes* methods -> zero-length byte array
 * - empty byte array to newString* methods -> empty string ""
 * - strings with special characters (non-ASCII) to test charset encoding
 * 
 * Partition C - Defect-Targeted Branches:
 * - newStringIso8859_1(null) - currently throws NullPointerException instead of returning null
 *   (documented defect in DoubleMetaphoneTest)
 * - newStringUsAscii(null) - same defect pattern
 * - newStringUtf16(null), newStringUtf16Be(null), newStringUtf16Le(null), newStringUtf8(null)
 *   - all have same null handling defect
 * 
 * Partition D - Exception & Defensive Guard Paths:
 * - getBytesUnchecked with unsupported charset -> IllegalStateException
 * - newString(bytes, charsetName) with unsupported charset -> IllegalStateException
 * 
 * Partition E - Object Lifecycle & Contract:
 * - Verify encoding/decoding roundtrip consistency
 * - Verify charset-specific byte patterns
 */
public class StringUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================
    
    @Test(timeout = 4000)
    public void testGetBytesUtf8NonNull() {
        byte[] result = StringUtils.getBytesUtf8("Hello");
        assertNotNull("getBytesUtf8 should not return null for non-null input", result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals("UTF-8 encoding mismatch", expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1NonNull() {
        byte[] result = StringUtils.getBytesIso8859_1("Hello");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAsciiNonNull() {
        byte[] result = StringUtils.getBytesUsAscii("Hello");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16NonNull() {
        byte[] result = StringUtils.getBytesUtf16("Hello");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16BeNonNull() {
        byte[] result = StringUtils.getBytesUtf16Be("Hello");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16LeNonNull() {
        byte[] result = StringUtils.getBytesUtf16Le("Hello");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedNonNull() {
        byte[] result = StringUtils.getBytesUnchecked("Hello", "UTF-8");
        assertNotNull(result);
        byte[] expected = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8NonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String result = StringUtils.newStringUtf8(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1NonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
        String result = StringUtils.newStringIso8859_1(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUsAsciiNonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        String result = StringUtils.newStringUsAscii(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16NonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16);
        String result = StringUtils.newStringUtf16(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16BeNonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16BE);
        String result = StringUtils.newStringUtf16Be(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf16LeNonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        String result = StringUtils.newStringUtf16Le(bytes);
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8CharsetNameNonNull() {
        byte[] bytes = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String result = StringUtils.newString(bytes, "UTF-8");
        assertNotNull(result);
        assertEquals("Hello", result);
    }

    // ==================== Partition B: Boundary Value Analysis ====================
    
    @Test(timeout = 4000)
    public void testGetBytesUtf8Null() {
        byte[] result = StringUtils.getBytesUtf8(null);
        assertNull("getBytesUtf8 should return null for null input", result);
    }

    @Test(timeout = 4000)
    public void testGetBytesIso8859_1Null() {
        byte[] result = StringUtils.getBytesIso8859_1(null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUsAsciiNull() {
        byte[] result = StringUtils.getBytesUsAscii(null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16Null() {
        byte[] result = StringUtils.getBytesUtf16(null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16BeNull() {
        byte[] result = StringUtils.getBytesUtf16Be(null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf16LeNull() {
        byte[] result = StringUtils.getBytesUtf16Le(null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedNull() {
        byte[] result = StringUtils.getBytesUnchecked(null, "UTF-8");
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8Null() {
        String result = StringUtils.newString(null, java.nio.charset.StandardCharsets.UTF_8);
        assertNull("newString with Charset should return null for null byte array", result);
    }

    @Test(timeout = 4000)
    public void testNewStringCharsetNameNull() {
        String result = StringUtils.newString(null, "UTF-8");
        assertNull("newString with charset name should return null for null byte array", result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUtf8EmptyString() {
        byte[] result = StringUtils.getBytesUtf8("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8EmptyBytes() {
        String result = StringUtils.newStringUtf8(new byte[0]);
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedEmptyString() {
        byte[] result = StringUtils.getBytesUnchecked("", "UTF-8");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1EmptyBytes() {
        String result = StringUtils.newStringIso8859_1(new byte[0]);
        assertNotNull(result);
        assertEquals("", result);
    }

    // ==================== Partition C: Defect-Targeted Branches (NullPointerException defect) ====================
    
    /**
     * CRITICAL DEFECT TEST: newStringIso8859_1(null) currently throws NullPointerException
     * per the known defect where DoubleMetaphoneTest fails.
     * The correct behavior should return null (as documented in the class javadoc).
     */
    @Test(timeout = 4000)
    public void testNewStringIso8859_1Null() {
        try {
            String result = StringUtils.newStringIso8859_1(null);
            // If we get here, the bug is fixed - result should be null
            assertNull("newStringIso8859_1(null) should return null", result);
        } catch (NullPointerException e) {
            // This is the known defect - original code throws NPE instead of returning null
            fail("newStringIso8859_1(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    /**
     * CRITICAL DEFECT TEST: newStringUsAscii(null) - same defect pattern
     */
    @Test(timeout = 4000)
    public void testNewStringUsAsciiNull() {
        try {
            String result = StringUtils.newStringUsAscii(null);
            assertNull("newStringUsAscii(null) should return null", result);
        } catch (NullPointerException e) {
            fail("newStringUsAscii(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    /**
     * CRITICAL DEFECT TEST: newStringUtf16(null) - same defect pattern
     */
    @Test(timeout = 4000)
    public void testNewStringUtf16Null() {
        try {
            String result = StringUtils.newStringUtf16(null);
            assertNull("newStringUtf16(null) should return null", result);
        } catch (NullPointerException e) {
            fail("newStringUtf16(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    /**
     * CRITICAL DEFECT TEST: newStringUtf16Be(null) - same defect pattern
     */
    @Test(timeout = 4000)
    public void testNewStringUtf16BeNull() {
        try {
            String result = StringUtils.newStringUtf16Be(null);
            assertNull("newStringUtf16Be(null) should return null", result);
        } catch (NullPointerException e) {
            fail("newStringUtf16Be(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    /**
     * CRITICAL DEFECT TEST: newStringUtf16Le(null) - same defect pattern
     */
    @Test(timeout = 4000)
    public void testNewStringUtf16LeNull() {
        try {
            String result = StringUtils.newStringUtf16Le(null);
            assertNull("newStringUtf16Le(null) should return null", result);
        } catch (NullPointerException e) {
            fail("newStringUtf16Le(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    /**
     * CRITICAL DEFECT TEST: newStringUtf8(null) - same defect pattern
     */
    @Test(timeout = 4000)
    public void testNewStringUtf8NullDefect() {
        try {
            String result = StringUtils.newStringUtf8(null);
            assertNull("newStringUtf8(null) should return null", result);
        } catch (NullPointerException e) {
            fail("newStringUtf8(null) should return null, but threw NullPointerException: " + e.getMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetBytesUncheckedUnsupportedCharset() {
        StringUtils.getBytesUnchecked("Hello", "UNSUPPORTED_CHARSET");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNewStringUnsupportedCharset() {
        StringUtils.newString("Hello".getBytes(), "UNSUPPORTED_CHARSET");
    }

    @Test(timeout = 4000)
    public void testGetBytesUncheckedNullStringNonNullCharset() {
        byte[] result = StringUtils.getBytesUnchecked(null, "UTF-8");
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testNewStringNonNullBytesNullCharsetName() {
        // newString(bytes, null charname) should throw NullPointerException from String constructor
        try {
            StringUtils.newString(new byte[]{72, 101}, null);
            fail("Should have thrown NullPointerException or IllegalStateException");
        } catch (NullPointerException e) {
            // Expected - String constructor throws NPE for null charset name
            assertTrue(true);
        } catch (Exception e) {
            // Any other exception is acceptable
            assertTrue(e instanceof NullPointerException || e instanceof IllegalStateException);
        }
    }

    // ==================== Partition E: Encoding/Decoding Roundtrip ====================
    
    @Test(timeout = 4000)
    public void testUtf8Roundtrip() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf8(original);
        String decoded = StringUtils.newStringUtf8(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testIso8859_1Roundtrip() {
        String original = "Hello, world!";
        byte[] encoded = StringUtils.getBytesIso8859_1(original);
        String decoded = StringUtils.newStringIso8859_1(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUsAsciiRoundtrip() {
        String original = "Hello";
        byte[] encoded = StringUtils.getBytesUsAscii(original);
        String decoded = StringUtils.newStringUsAscii(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16Roundtrip() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf16(original);
        String decoded = StringUtils.newStringUtf16(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16BeRoundtrip() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf16Be(original);
        String decoded = StringUtils.newStringUtf16Be(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16LeRoundtrip() {
        String original = "Hello, 世界!";
        byte[] encoded = StringUtils.getBytesUtf16Le(original);
        String decoded = StringUtils.newStringUtf16Le(encoded);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUncheckedRoundtrip() {
        String original = "Hello, world!";
        byte[] encoded = StringUtils.getBytesUnchecked(original, "UTF-8");
        String decoded = StringUtils.newString(encoded, "UTF-8");
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testNewStringIso8859_1NonNullSpecialChars() {
        // Test with characters within ISO-8859-1 range
        byte[] bytes = new byte[]{(byte)0xC0, (byte)0xE9, (byte)0xF1}; // Àéñ
        String result = StringUtils.newStringIso8859_1(bytes);
        assertNotNull(result);
        assertEquals("Àéñ", result);
    }

    @Test(timeout = 4000)
    public void testNewStringUtf8SpecialChars() {
        byte[] bytes = new byte[]{(byte)0xE2, (byte)0x82, (byte)0xAC}; // Euro sign €
        String result = StringUtils.newStringUtf8(bytes);
        assertNotNull(result);
        assertEquals("€", result);
    }
}