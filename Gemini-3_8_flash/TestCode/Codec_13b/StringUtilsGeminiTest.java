package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.language.DoubleMetaphone;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Method Under Test           | Branch / Condition               | Input / Scenario         | Expected Output
 * -------------------------------------------------------------------------------------------------
 * getBytes(String, Charset)   | string == null                   | null String              | null
 * getBytes(String, Charset)   | string != null                   | "ASCII/UTF8/UTF16" text  | encoded byte[]
 * getBytesUnchecked           | string == null                   | null, "UTF-8"            | null
 * getBytesUnchecked           | string != null, valid charset    | "abc", "UTF-8"           | encoded byte[]
 * getBytesUnchecked           | unsupported charset exception    | "abc", "INVALID_NAME"    | IllegalStateException
 * newString(byte[], String)   | bytes == null                    | null, "UTF-8"            | null
 * newString(byte[], String)   | bytes != null, valid charset     | byte[], "UTF-8"          | decoded String
 * newString(byte[], String)   | unsupported charset exception    | byte[], "INVALID_NAME"   | IllegalStateException
 * newString(byte[], Charset)  | bytes == null (utf8)             | null byte[]              | null
 * newString(byte[], Charset)  | bytes != null (all charsets)     | valid byte[]             | decoded String
 * -------------------------------------------------------------------------------------------------
 * [Defect-Targeted Zone: CODEC-184 / DoubleMetaphone NPE on null/empty strings]
 * DoubleMetaphone.isDoubleMetaphoneEqual | cs1 or cs2 produces null | "", "aa"                 | false (no NPE)
 * DoubleMetaphone.isDoubleMetaphoneEqual | cs1 == cs2 == null       | null, null               | true (no NPE)
 * DoubleMetaphone.isDoubleMetaphoneEqual | one null, one non-null   | null, "foo"              | false (no NPE)
 * -------------------------------------------------------------------------------------------------
 */
public class StringUtilsGeminiTest {

    private static final String SAMPLE_TEXT = "Hello, Apache Commons Codec!";
    private static final String NON_EXISTENT_CHARSET = "INVALID_CHARSET_FOR_TESTING_12345";

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions (Roundtrip Encodings)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIso8859_1RoundTrip() {
        final byte[] encoded = StringUtils.getBytesIso8859_1(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringIso8859_1(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testUsAsciiRoundTrip() {
        final byte[] encoded = StringUtils.getBytesUsAscii(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringUsAscii(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf8RoundTrip() {
        final byte[] encoded = StringUtils.getBytesUtf8(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringUtf8(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16RoundTrip() {
        final byte[] encoded = StringUtils.getBytesUtf16(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringUtf16(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16BeRoundTrip() {
        final byte[] encoded = StringUtils.getBytesUtf16Be(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringUtf16Be(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testUtf16LeRoundTrip() {
        final byte[] encoded = StringUtils.getBytesUtf16Le(SAMPLE_TEXT);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newStringUtf16Le(encoded);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    @Test(timeout = 4000)
    public void testNamedCharsetRoundTrip() {
        final byte[] encoded = StringUtils.getBytesUnchecked(SAMPLE_TEXT, CharEncoding.UTF_8);
        assertNotNull("Encoded bytes should not be null", encoded);
        final String decoded = StringUtils.newString(encoded, CharEncoding.UTF_8);
        assertEquals("Decoded string must match original input", SAMPLE_TEXT, decoded);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullInputGetBytes() {
        assertNull("Null input string should yield null byte array", StringUtils.getBytesIso8859_1(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUsAscii(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUtf8(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUtf16(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUtf16Be(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUtf16Le(null));
        assertNull("Null input string should yield null byte array", StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNullInputNewString() {
        assertNull("Null byte array should yield null string", StringUtils.newStringUtf8(null));
        assertNull("Null byte array should yield null string", StringUtils.newString(null, "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testEmptyStringGetBytes() {
        final byte[] empty = new byte[0];
        assertArrayEquals(empty, StringUtils.getBytesIso8859_1(""));
        assertArrayEquals(empty, StringUtils.getBytesUsAscii(""));
        assertArrayEquals(empty, StringUtils.getBytesUtf8(""));
        assertArrayEquals(empty, StringUtils.getBytesUtf16Be(""));
        assertArrayEquals(empty, StringUtils.getBytesUtf16Le(""));
        assertArrayEquals(empty, StringUtils.getBytesUnchecked("", "UTF-8"));
    }

    @Test(timeout = 4000)
    public void testEmptyByteArrayNewString() {
        final byte[] empty = new byte[0];
        assertEquals("", StringUtils.newStringIso8859_1(empty));
        assertEquals("", StringUtils.newStringUsAscii(empty));
        assertEquals("", StringUtils.newStringUtf8(empty));
        assertEquals("", StringUtils.newStringUtf16(empty));
        assertEquals("", StringUtils.newStringUtf16Be(empty));
        assertEquals("", StringUtils.newStringUtf16Le(empty));
        assertEquals