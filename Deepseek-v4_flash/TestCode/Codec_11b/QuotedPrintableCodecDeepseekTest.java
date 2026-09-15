package org.apache.commons.codec.net;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Coverage targets for QuotedPrintableCodec:
 * 
 * Branch/Condition points:
 * 1. encodeQuotedPrintable(BitSet, byte[]):
 *    - bytes == null → null
 *    - printable == null → use PRINTABLE_CHARS
 *    - loop over bytes: if printable.get(b) write else encodeQuotedPrintable(b, buffer)
 *    - signed byte conversion: if b < 0 → 256 + b
 *    - buffer.toByteArray() return
 * 
 * 2. decodeQuotedPrintable(byte[]):
 *    - bytes == null → null
 *    - loop with i: if b == ESCAPE_CHAR then read two hex digits, else buffer.write(b)
 *    - exception handling: ArrayIndexOutOfBoundsException → DecoderException
 * 
 * 3. encode(byte[]) → delegates to encodeQuotedPrintable with default printable
 * 
 * 4. decode(byte[]) → delegates to decodeQuotedPrintable
 * 
 * 5. encode(String) → null check, then encode(pString, getDefaultCharset()) with UnsupportedEncodingException wrapping
 * 
 * 6. decode(String, String) → null check, then decode(StringUtils.getBytesUsAscii(pString), charset)
 * 
 * 7. decode(String) → null check, then decode(pString, getDefaultCharset()) with UnsupportedEncodingException wrapping
 * 
 * 8. encode(String, String) → null check, then StringUtils.newStringUsAscii(encode(pString.getBytes(charset)))
 * 
 * 9. encode(Object) → null | byte[] | String | else throw EncoderException
 * 
 * 10. decode(Object) → null | byte[] | String | else throw DecoderException
 * 
 * Known Defects (from Defects4J):
 * - Soft line breaks (CRLF after '=') cause decoder to throw because it tries to parse CR as hex digit.
 * - Encoder does not insert soft line breaks and does not encode trailing whitespace (RFC rule #3).
 * 
 * Tests target:
 * - Decoding with CRLF soft break → must not throw and produce correct output.
 * - Encoding strings with trailing spaces → space must be encoded as =20.
 * - Encoding long strings → must contain soft line breaks (=<CR><LF>).
 * - Edge cases: null inputs, empty arrays, unsigned byte conversions, boundary values of printable Bitset.
 * - Exception paths: invalid hex digits, short escapes, unsupported charsets.
 */

public class QuotedPrintableCodecDeepseekTest {

    // ========== Part A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testEncodeByteArrayNormal() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 'h', 'e', 'l', 'l', 'o', ' ' };
        byte[] expected = new byte[] { 'h', 'e', 'l', 'l', 'o', ' ' }; // space is printable
        assertArrayEquals(expected, codec.encode(input));
    }

    @Test(timeout = 4000)
    public void testEncodeByteArrayNonPrintable() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { '=', '\n', 0x0F };
        byte[] expected = new byte[] { '=', '3', 'D', '=', '0', 'A', '=', '0', 'F' };
        assertArrayEquals(expected, codec.encode(input));
    }

    @Test(timeout = 4000)
    public void testDecodeByteArrayNormal() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { 'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        assertArrayEquals(input, codec.decode(input));
    }

    @Test(timeout = 4000)
    public void testDecodeByteArrayEscaped() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { '=', '3', 'D' };
        byte[] expected = new byte[] { '=' };
        assertArrayEquals(expected, codec.decode(input));
    }

    @Test(timeout = 4000)
    public void testEncodeStringDefaultCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "hello world";
        assertEquals("hello world", codec.encode(input));
    }

    @Test(timeout = 4000)
    public void testDecodeStringDefaultCharset() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "hello=20world";
        assertEquals("hello world", codec.decode(input));
    }

    @Test(timeout = 4000)
    public void testGetDefaultCharset() {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("ISO-8859-1");
        assertEquals("ISO-8859-1", codec.getDefaultCharset());
    }

    // ========== Part B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEncodeNullByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((byte[]) null));
    }

    @Test(timeout = 4000)
    public void testDecodeNullByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((byte[]) null));
    }

    @Test(timeout = 4000)
    public void testEncodeNullString() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((String) null));
    }

    @Test(timeout = 4000)
    public void testDecodeNullString() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((String) null));
    }

    @Test(timeout = 4000)
    public void testEncodeEmptyByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertArrayEquals(new byte[0], codec.encode(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testDecodeEmptyByteArray() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertArrayEquals(new byte[0], codec.decode(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testEncodeByteArraySignedConversion() throws Exception {
        // bytes with high bit set should be treated as unsigned
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { (byte) 0x80 }; // 128 unsigned, not printable
        byte[] expected = new byte[] { '=', '8', '0' };
        assertArrayEquals(expected, codec.encode(input));
    }

    @Test(timeout = 4000)
    public void testDecodeByteArrayBoundaryHex() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        byte[] input = new byte[] { '=', 'F', 'F' };
        byte[] expected = new byte[] { (byte) 0xFF };
        assertArrayEquals(expected, codec.decode(input));
    }

    @Test(timeout = 4000)
    public void testEncodeNullObject() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.encode((Object) null));
    }

    @Test(timeout = 4000)
    public void testDecodeNullObject() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        assertNull(codec.decode((Object) null));
    }

    // ========== Part C: Defect-Targeted Branch Zone ==========

    // Defect: Decoder should ignore soft line breaks (CRLF) after '='
    @Test(timeout = 4000)
    public void testSoftLineBreakDecode() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        String input = "hello=20=\r\nworld";
        String expected = "hello world";
        assertEquals(expected, codec.decode(input));
    }

    // Defect: Encoder should insert soft line breaks for long lines (RFC line length limit)
    @Test(timeout = 4000)
    public void testSoftLineBreakEncode() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 80; i++) {
            sb.append('a');
        }
        String encoded = codec.encode(sb.toString());
        // Encoding should contain at least one soft line break (=CRLF)
        assertTrue("Expected soft line break in encoded output", encoded.contains("=\r\n"));
    }

    // Defect: Encoder should encode trailing whitespace on a line (rule #3)
    @Test(timeout = 4000)
    public void testTrailingSpecial() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // trailing space should be encoded as =20
        String input = "hello ";
        String expected = "hello=20";
        assertEquals(expected, codec.encode(input));
    }

    // Defect: Soft line break at end (ultimate soft break) – encode should handle
    @Test(timeout = 4000)
    public void testUltimateSoftBreak() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // A string that would cause a soft break exactly at the end
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 75; i++) {
            sb.append('a');
        }
        sb.append('='); // '=' triggers encoding to =3D, pushing line over 76
        String encoded = codec.encode(sb.toString());
        // The encoded string should contain a soft line break somewhere after 76 chars
        assertTrue("Expected soft line break in encoded output", encoded.contains("=\r\n"));
    }

    // Defect: Non-encoded CRLF in input should be skipped (decode should not throw)
    @Test(timeout = 4000)
    public void testSkipNotEncodedCRLF() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        // Input has raw CRLF that is not part of escape sequence
        byte[] input = "hello\r\nworld".getBytes("US-ASCII");
        // Decoder should treat CRLF as part of unencoded data? RFC says CRLF in encoded
        // data outside escapes should be ignored? Actually, strict RFC says CRLF should not appear in encoded data except as soft break.
        // But the defective decoder would throw if it tries to parse CR as hex.
        // We test that it does not throw and returns the same bytes (since CR and LF are not printable? they are not in printable set, so they will be encoded when encoding, but we are testing decode of raw bytes. The decoder currently writes all bytes except ESCAPE_CHAR. So it should pass through.
        try {
            byte[] decoded = codec.decode(input);
            // No exception: defect is that it does throw. So if it passes, test passes.
            assertArrayEquals(input, decoded);
        } catch (DecoderException e) {
            fail("DecoderException should not be thrown for raw CRLF: " + e.getMessage());
        }
    }

    // ========== Part D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = DecoderException.class)
    public void testDecodeInvalidHexDigit() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode("=XX".getBytes("US-ASCII"));
    }

    @Test(timeout = 4000, expected = DecoderException.class)
    public void testDecodeMissingHexDigit() throws Exception {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode("=".getBytes("US-ASCII"));
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeInvalidObject() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.encode(Integer.valueOf(42));
    }

    @Test(timeout = 4000, expected = DecoderException.class)
    public void testDecodeInvalidObject() throws DecoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec();
        codec.decode(Integer.valueOf(42));
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeStringUnsupportedEncoding() throws EncoderException {
        QuotedPrintableCodec codec = new QuotedPrintableCodec("unsupported-charset");
        // The encode(String) uses getDefaultCharset() which is unsupported, will cause UnsupportedEncodingException wrapped in EncoderException
        codec.encode("test");
    }

    // ========== Part E: Static utility methods ==========

    @Test(timeout = 4000)
    public void testStaticEncodeQuotedPrintableNullBytes() {
        assertNull(QuotedPrintableCodec.encodeQuotedPrintable(null, null));
    }

    @Test(timeout = 4000)
    public void testStaticEncodeQuotedPrintableNullPrintable() {
        byte[] input = new byte[] { 'a', (byte) 0x80 };
        byte[] result = QuotedPrintableCodec.encodeQuotedPrintable(null, input);
        // 'a' is printable, 0x80 should be encoded
        byte[] expected = new byte[] { 'a', '=', '8', '0' };
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testStaticEncodeQuotedPrintableCustomPrintable() {
        BitSet printable = new BitSet(256);
        printable.set('a');
        byte[] input = new byte[] { 'a', 'b' };
        byte[] result = QuotedPrintableCodec.encodeQuotedPrintable(printable, input);
        // 'a' is printable, 'b' is not
        byte[] expected = new byte[] { 'a', '=', '6', '2' };
        assertArrayEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testStaticDecodeQuotedPrintableNull() throws DecoderException {
        assertNull(QuotedPrintableCodec.decodeQuotedPrintable(null));
    }

    @Test(timeout = 4000)
    public void testStaticDecodeQuotedPrintableWithCRLFSoftBreak() throws DecoderException {
        // Soft line break: =<CR><LF> should be ignored
        byte[] input = "hello=\r\nworld".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        // In correct implementation, the "=\r\n" is removed, but the decoder in current buggy version
        // throws because it tries to parse CR as hex digit.
        // We test that it does not throw and returns "helloworld"
        try {
            byte[] result = QuotedPrintableCodec.decodeQuotedPrintable(input);
            byte[] expected = "helloworld".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
            assertArrayEquals(expected, result);
        } catch (DecoderException e) {
            // Expected failure in buggy version, but we want to reveal it
            fail("Decoder should handle soft line breaks without throwing: " + e.getMessage());
        }
    }
}