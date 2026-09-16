package org.apache.commons.codec.net;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Methods                  | Branch / Condition Analyzed                 | Target Partition
 * ---------------------------------------------------------------------------------------------------------
 * encodeQuotedPrintable(BitSet,b) | bytes == null (true/false)                  | Partition B / A
 *                                 | printable == null (true -> default / false) | Partition B / A
 *                                 | b < 0 (negative byte promotion to unsigned) | Partition B
 *                                 | printable.get(b) (true -> write / false)    | Partition A
 * decodeQuotedPrintable(b)        | bytes == null (true/false)                  | Partition B / A
 *                                 | b == ESCAPE_CHAR ('=') (true/false)         | Partition A
 *                                 | Soft line break: "=\r\n" -> invalid digit 13| Partition C (Defect Target)
 *                                 | Truncated escape ("=", "=A") -> AIOOBE      | Partition D
 *                                 | Non-hex escape ("=ZZ", "=1G") -> DecException| Partition D
 * encode(byte[]) / decode(byte[]) | standard byte array roundtrip               | Partition A
 * encode(String) / decode(String) | str == null (true/false)                    | Partition B
 *                                 | default charset vs custom charset           | Partition A
 * encode(String, cs) / decode(...) | unsupported charset -> UEE / EncoderExc     | Partition D
 * encode(Object) / decode(Object) | null -> null                                | Partition B
 *                                 | instanceof byte[] / String / Invalid Type   | Partition A / D
 * getDefaultCharset()             | constructor initialization checks           | Partition E
 * ---------------------------------------------------------------------------------------------------------
 */
public class QuotedPrintableCodecGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicEncodeDecodeByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = "Hello, World! 12345".getBytes(CharEncoding.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded array should not be null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded array must match original input", input, decoded);
    }

    @Test(timeout = 4000)
    public void testBasicEncodeDecodeString() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String plainText = "The quick brown fox jumps over the lazy dog.";
        String encoded = codec.encode(plainText);
        assertEquals("Printable ASCII sentence should remain unchanged", plainText, encoded);
        String decoded = codec.decode(encoded);
        assertEquals("Decoded text should match input", plainText, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithCustomCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(CharEncoding.ISO_8859_1);
        assertEquals(CharEncoding.ISO_8859_1, codec.getDefaultCharset());

        String input = "Caf\u00e9"; // 'é' is 0xE9 in ISO-8859-1
        String encoded = codec.encode(input, CharEncoding.ISO_8859_1);
        assertEquals("Caf=E9", encoded);

        String decoded = codec.decode(encoded, CharEncoding.ISO_8859_1);
        assertEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeLowerCaseHexDigits() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // "=3d" lowercase hex digit decoding
        byte[] decoded = codec.decode("test=3dvalue".getBytes(CharEncoding.US_ASCII));
        assertEquals("test=value", new String(decoded, CharEncoding.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testPrintableAsciiSetInvariance() throws Exception {
        // Tab (9), Space (32), 33-60, 62-126 are in PRINTABLE_CHARS; 61 ('=') is NOT.
        byte[] printableChars = new byte[] {
            9, 32, 33, 48, 57, 60, 62, 65, 90, 97, 122, 126
        };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, printableChars);
        assertArrayEquals("Printable chars should not be escaped", printableChars, encoded);
    }

    @Test(timeout = 4000)
    public void testEqualSignIsEscaped() throws Exception {
        byte[] input = new byte[] { '=' };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        assertEquals("=3D", new String(encoded, CharEncoding.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testCustomBitSetEncoding() throws Exception {
        BitSet customBitSet = new BitSet(256);
        customBitSet.set('A');
        customBitSet.set('B');

        byte[] input = "ABC".getBytes(CharEncoding.US_ASCII);
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(customBitSet, input);
        // 'C' is ASCII 67 (0x43), so it must be escaped
        assertEquals("AB=43", new String(encoded, CharEncoding.US_ASCII));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullInputsReturnNull() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();

        assertNull("Static encode null bytes", QuotedPrintableCodec.encodeQuotedPrintable(null, null));
        assertNull("Static decode null bytes", QuotedPrintableCodec.decodeQuotedPrintable(null));

        assertNull("Instance encode null bytes", codec.encode((byte[]) null));
        assertNull("Instance decode null bytes", codec.decode((byte[]) null));

        assertNull("Instance encode null string", codec.encode((String) null));
        assertNull("Instance decode null string", codec.decode((String) null));

        assertNull("Instance encode null string with charset", codec.encode(null, CharEncoding.UTF_8));
        assertNull("Instance decode null string with charset", codec.decode(null, CharEncoding.UTF_8));

        assertNull("Instance encode null object", codec.encode((Object) null));
        assertNull("Instance decode null object", codec.decode((Object) null));
    }

    @Test(timeout = 4000)
    public void testEmptyInputsReturnEmpty() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();

        assertArrayEquals(new byte[0], codec.encode(new byte[0]));
        assertArrayEquals(new byte[0], codec.decode(new byte[0]));

        assertEquals("", codec.encode(""));
        assertEquals("", codec.decode(""));

        assertEquals("", codec.encode("", CharEncoding.UTF_8));
        assertEquals("", codec.decode("", CharEncoding.UTF_8));
    }

    @Test(timeout = 4000)
    public void testNegativeByteValuesEncoding() throws Exception {
        // Bytes with MSB set (negative byte values) must be promoted to 256 + b
        byte[] input = new byte[] { (byte) 0x80, (byte) 0xFF, (byte) 0xAA };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        assertEquals("=80=FF=AA", new String(encoded, CharEncoding.US_ASCII));

        byte[] decoded = QuotedPrintableCodec.decodeQuotedPrintable(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testControlCharactersEncoding() throws Exception {
        byte[] input = new byte[] { 0, 1, 2, 7, 8, 10, 13, 27, 31, 127 };
        byte[] encoded = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        assertEquals("=00=01=02=07=08=0A=0D=1B=1F=7F", new String(encoded, CharEncoding.US_ASCII));

        byte[] decoded = QuotedPrintableCodec.decodeQuotedPrintable(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeObjectTypes() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();

        // Object wrapping byte[]
        byte[] rawBytes = "Testing Objects".getBytes(CharEncoding.UTF_8);
        Object encodedBytesObj = codec.encode((Object) rawBytes);
        assertTrue("Result must be byte[]", encodedBytesObj instanceof byte[]);
        Object decodedBytesObj = codec.decode(encodedBytesObj);
        assertArrayEquals(rawBytes, (byte[]) decodedBytesObj);

        // Object wrapping String
        String rawStr = "Testing String Object =?";
        Object encodedStrObj = codec.encode((Object) rawStr);
        assertTrue("Result must be String", encodedStrObj instanceof String);
        Object decodedStrObj = codec.decode(encodedStrObj);
        assertEquals(rawStr, decodedStrObj);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * - testSoftLineBreakDecode
     * - testSkipNotEncodedCRLF
     * According to RFC 1521 Rule #5, a soft line break represented by "=\r\n"
     * must be removed/skipped during quoted-printable decoding.
     * The defective code attempts to parse '\r' (ASCII 13) as a hex digit,
     * triggering: DecoderException: Invalid URL encoding: not a valid digit (radix 16): 13
     */
    @Test(timeout = 4000)
    public void testSoftLineBreakDecodeDefect() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String softLineBreakInput = "Hello,=\r\nWorld!";
        String expected = "Hello,World!";
        String actual = codec.decode(softLineBreakInput);
        assertEquals("Soft line break '=\\r\\n' should be skipped by decoder", expected, actual);
    }

    @Test(timeout = 4000)
    public void testSkipNotEncodedCRLFDefect() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // Pattern from Commons Codec defect test case: embedded soft line break and CRLF
        String qpData = "=A1=B1=\r\n=C1=D1=E1";
        byte[] decoded = codec.decode(qpData.getBytes(CharEncoding.US_ASCII));
        assertNotNull("Decoded bytes must not be null", decoded);
        byte[] expected = new byte[] { (byte) 0xA1, (byte) 0xB1, (byte) 0xC1, (byte) 0xD1, (byte) 0xE1 };
        assertArrayEquals(expected, decoded);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecodeInvalidQuotedPrintablePrematureEndSingleChar() {
        try {
            QuotedPrintableCodec.decodeQuotedPrintable(new byte[] { '=' });
            fail("Expected DecoderException for truncated escape '='");
        } catch (DecoderException expected) {
            assertTrue("Exception message should indicate invalid encoding",
                    expected.getMessage().contains("Invalid quoted-printable encoding"));
        }
    }

    @Test(timeout = 4000)
    public void testDecodeInvalidQuotedPrintablePrematureEndTwoChars() {
        try {
            QuotedPrintableCodec.decodeQuotedPrintable(new byte[] { '=', 'A' });
            fail("Expected DecoderException for truncated escape '=A'");
        } catch (DecoderException expected) {
            assertTrue("Exception message should indicate invalid encoding",
                    expected.getMessage().contains("Invalid quoted-printable encoding"));
        }
    }

    @Test(timeout = 4000)
    public void testDecodeInvalidQuotedPrintableInvalidHexDigits() {
        try {
            QuotedPrintableCodec.decodeQuotedPrintable(new byte[] { '=', 'Z', 'Z' });
            fail("Expected DecoderException for non-hex characters after escape");
        } catch (DecoderException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testEncodeUnsupportedObjectThrowsEncoderException() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        try {
            codec.encode(Double.valueOf(3.14159));
            fail("Expected EncoderException when encoding an unsupported object type");
        } catch (EncoderException expected) {
            assertTrue(expected.getMessage().contains("cannot be quoted-printable encoded"));
        }
    }

    @Test(timeout = 4000)
    public void testDecodeUnsupportedObjectThrowsDecoderException() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        try {
            codec.decode(Integer.valueOf(42));
            fail("Expected DecoderException when decoding an unsupported object type");
        } catch (DecoderException expected) {
            assertTrue(expected.getMessage().contains("cannot be quoted-printable decoded"));
        }
    }

    @Test(timeout = 4000)
    public void testEncodeStringInvalidCharsetThrowsEncoderException() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("INVALID_CHARSET_NAME_XYZ");
        try {
            codec.encode("test string");
            fail("Expected EncoderException due to invalid default charset");
        } catch (EncoderException expected) {
            assertTrue(expected.getCause() instanceof UnsupportedEncodingException);
        }
    }

    @Test(timeout = 4000)
    public void testDecodeStringInvalidCharsetThrowsDecoderException() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("INVALID_CHARSET_NAME_XYZ");
        try {
            codec.decode("test string");
            fail("Expected DecoderException due to invalid default charset");
        } catch (DecoderException expected) {
            assertTrue(expected.getCause() instanceof UnsupportedEncodingException);
        }
    }

    @Test(expected = UnsupportedEncodingException.class, timeout = 4000)
    public void testEncodeWithExplicitInvalidCharsetThrowsUEE() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode("sample", "INVALID_CHARSET_123");
    }

    @Test(expected = UnsupportedEncodingException.class, timeout = 4000)
    public void testDecodeWithExplicitInvalidCharsetThrowsUEE() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode("sample", "INVALID_CHARSET_123");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorInitializesUtf8() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertEquals("Default charset should be UTF-8", CharEncoding.UTF_8, codec.getDefaultCharset());
    }

    @Test(timeout = 4000)
    public void testCustomConstructorInitializesSpecifiedCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(CharEncoding.US_ASCII);
        assertEquals("Configured charset should match input", CharEncoding.US_ASCII, codec.getDefaultCharset());
    }

    @Test(timeout = 4000)
    public void testNullCharsetConstructor() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec(null);
        assertNull("Default charset should be null when initialized with null", codec.getDefaultCharset());
    }
}