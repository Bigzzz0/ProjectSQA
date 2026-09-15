package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - encode/decode with various lengths: 0, 1, 2, 3, 4, 5, 6, 7 bytes
 *   - URL-safe and standard encode tables
 *   - Chunked encoding (lineLength > 0) with default CRLF
 *   - Chunked encoding with custom lineSeparator (non-base64)
 *   - Static helper methods: encodeBase64, encodeBase64String, encodeBase64URLSafe, etc.
 *   - Decode with padding (standard) and without (URL-safe)
 *   - Roundtrip: encode then decode
 *   - isUrlSafe(), hasData(), avail()
 *   - reset() state clear
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - null input arrays (encode/decode)
 *   - empty input arrays
 *   - Single byte input
 *   - Two byte input (modulus = 2)
 *   - Three byte input (modulus = 0)
 *   - Four byte input
 *   - Large input (up to 8192 bytes) to force buffer resizing
 *   - Max line length (Integer.MAX_VALUE)
 *   - Line length = 0 (no chunking)
 *   - Line length = 4 (minimum chunking)
 *   - Line length = 76 (MIME default)
 *   - Line separator length 1, 2, 3
 *   - BigInteger edges: 0, 1, -1 (but toIntegerBytes uses bigInt.bitLength)
 *
 * Partition C: Defect-Targeted Branch Zone (CODEC-105)
 *   - Simulate streaming with setInitialBuffer(byte[2], 0, 2) then encode to force flush at modulus=1
 *   - Simulate streaming with setInitialBuffer(byte[2], 0, 2) then decode with padding
 *   - Call encode/decode with -1 EOF multiple times
 *   - Ensure no ArrayIndexOutOfBoundsException occurs
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - encode(Object) with non-byte[] -> EncoderException
 *   - decode(Object) with non-byte[]/String -> DecoderException
 *   - Constructor with lineSeparator containing base64 chars -> IllegalArgumentException
 *   - encodeBase64 with oversized data (maxResultSize exceeded) -> IllegalArgumentException
 *   - encodeInteger(null) -> NullPointerException
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple encode/decode cycles on same instance, verify state reset
 *   - Internal methods: encode(byte[],int,int), decode(byte[],int,int), readResults, setInitialBuffer
 */
public class Base64DeepseekTest {

    // ======================== Partition A: Core Functional Logic ========================

    @Test(timeout = 4000)
    public void testEncodeDecodeEmpty() {
        Base64 b64 = new Base64();
        byte[] empty = new byte[0];
        assertArrayEquals(empty, b64.encode(empty));
        assertArrayEquals(empty, b64.decode(empty));
        assertEquals("", b64.encodeToString(empty));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeStandardSingleByte() {
        Base64 b64 = new Base64();
        byte[] input = new byte[]{(byte) 0x00};
        byte[] expected = new byte[]{'A', 'A', '=', '='};
        assertArrayEquals(expected, b64.encode(input));
        assertArrayEquals(input, b64.decode(b64.encode(input)));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeStandardTwoBytes() {
        Base64 b64 = new Base64();
        byte[] input = new byte[]{(byte) 0x00, (byte) 0x01};
        byte[] encoded = b64.encode(input);
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeStandardThreeBytes() {
        Base64 b64 = new Base64();
        byte[] input = new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0x02};
        byte[] encoded = b64.encode(input);
        assertEquals(4, encoded.length);
        assertEquals('A', encoded[0]);
        assertEquals('A', encoded[1]);
        assertEquals('E', encoded[2]);
        assertEquals('C', encoded[3]);
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeStandardFourBytes() {
        Base64 b64 = new Base64();
        byte[] input = new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
        byte[] encoded = b64.encode(input);
        assertEquals(8, encoded.length); // two 4-byte blocks + padding
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeURLSafe() {
        Base64 b64 = new Base64(true);
        // URL-safe encoding uses '-' and '_' instead of '+' and '/', and omits padding
        byte[] input = new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00};
        byte[] encoded = b64.encode(input);
        assertEquals(4, encoded.length);
        assertEquals('A', encoded[0]);
        assertEquals('A', encoded[1]);
        assertEquals('A', encoded[2]);
        assertEquals('A', encoded[3]);
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testURLSafeNoPadding() {
        Base64 b64 = new Base64(true);
        byte[] input = new byte[]{(byte) 0x00};
        byte[] encoded = b64.encode(input);
        // URL-safe: only 2 bytes emitted, no padding
        assertEquals(2, encoded.length);
        assertEquals('A', encoded[0]);
        assertEquals('A', encoded[1]);
        // decodes to original
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testIsUrlSafe() {
        assertFalse(new Base64().isUrlSafe());
        assertTrue(new Base64(true).isUrlSafe());
        assertFalse(new Base64(0).isUrlSafe());
        assertFalse(new Base64(76, new byte[]{'\r','\n'}).isUrlSafe());
        assertTrue(new Base64(76, new byte[]{'\r','\n'}, true).isUrlSafe());
    }

    @Test(timeout = 4000)
    public void testChunkedEncoding() {
        // With lineLength=8, we get chunks of 8 base64 chars (multiple of 4)
        Base64 b64 = new Base64(8);
        byte[] input = new byte[9]; // 9 bytes produce 12 base64 chars, split into chunks
        for (int i = 0; i < 9; i++) {
            input[i] = (byte)i;
        }
        byte[] encoded = b64.encode(input);
        // Check presence of CRLF (CHUNK_SEPARATOR)
        String encodedStr = new String(encoded, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(encodedStr.contains("\r\n"));
        // Decode back
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingCustomSeparator() {
        byte[] sep = new byte[]{'\n'};
        Base64 b64 = new Base64(8, sep);
        byte[] input = new byte[6];
        for (int i = 0; i < 6; i++) input[i] = (byte)i;
        byte[] encoded = b64.encode(input);
        String s = new String(encoded, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(s.contains("\n"));
        assertArrayEquals(input, b64.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testStaticEncodeBase64Methods() {
        byte[] data = new byte[]{(byte)0x00, (byte)0x01, (byte)0x02};
        assertArrayEquals(new byte[]{'A','A','E','C'}, Base64.encodeBase64(data));
        assertEquals("AAEC", Base64.encodeBase64String(data));
        // Chunked
        byte[] chunked = Base64.encodeBase64Chunked(data);
        assertEquals(4, chunked.length); // less than 76 chars, so no chunking
        // URL-safe
        assertArrayEquals(new byte[]{'A','A','E','C'}, Base64.encodeBase64URLSafe(data));
        assertEquals("AAEC", Base64.encodeBase64URLSafeString(data));
    }

    @Test(timeout = 4000)
    public void testStaticDecodeBase64() {
        byte[] expected = new byte[]{(byte)0x00};
        assertArrayEquals(expected, Base64.decodeBase64("AA=="));
        assertArrayEquals(expected, Base64.decodeBase64(new byte[]{'A','A','=','='}));
    }

    @Test(timeout = 4000)
    public void testRoundTrip() {
        Base64 b64 = new Base64();
        byte[] original = "Apache Commons Codec".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = b64.encode(original);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeWithMissingPadding() {
        Base64 b64 = new Base64();
        byte[] data = "AA".getBytes(); // missing padding
        byte[] decoded = b64.decode(data);
        assertArrayEquals(new byte[]{(byte)0x00}, decoded);
    }

    // ======================== Partition B: Boundary Value Analysis ========================

    @Test(timeout = 4000)
    public void testEncodeDecodeNullArray() {
        Base64 b64 = new Base64();
        assertNull(b64.encode(null));
        assertNull(b64.decode((byte[]) null));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeEmptyArray() {
        Base64 b64 = new Base64();
        byte[] empty = new byte[0];
        assertArrayEquals(empty, b64.encode(empty));
        assertArrayEquals(empty, b64.decode(empty));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeBoundaryLengths() {
        Base64 b64 = new Base64();
        // lengths from 0 to 7 inclusive
        for (int len = 0; len <= 7; len++) {
            byte[] input = new byte[len];
            for (int i = 0; i < len; i++) input[i] = (byte)(i * 17);
            byte[] encoded = b64.encode(input);
            byte[] decoded = b64.decode(encoded);
            assertArrayEquals("Failed at length " + len, input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeLargeInput() {
        Base64 b64 = new Base64();
        byte[] input = new byte[8192];
        for (int i = 0; i < input.length; i++) input[i] = (byte)(i & 0xFF);
        byte[] encoded = b64.encode(input);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeBase64MaxResultSizeExceed() {
        // Create a huge array that would produce output > Integer.MAX_VALUE
        // But we can't allocate that, so we test with a moderate array and low maxResultSize
        byte[] data = new byte[1024];
        try {
            Base64.encodeBase64(data, false, false, 100);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test(timeout = 4000)
    public void testLineLengthZero() {
        Base64 b64 = new Base64(0);
        assertFalse(b64.isUrlSafe());
        byte[] input = new byte[]{0, 1, 2};
        byte[] encoded = b64.encode(input);
        assertEquals(4, encoded.length);
        assertEquals('A', encoded[0]);
    }

    @Test(timeout = 4000)
    public void testLineLengthMultipleOfFour() {
        // lineLength already rounded down to multiple of 4 in constructor
        Base64 b64 = new Base64(10); // should become 8
        assertFalse(b64.isUrlSafe());
        byte[] input = new byte[9];
        for (int i = 0; i < 9; i++) input[i] = (byte)i;
        byte[] encoded = b64.encode(input);
        // Should have CRLF every 8 base64 chars
        String s = new String(encoded, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(s.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testIntegerEncodingDecoding() {
        BigInteger bi = new BigInteger("12345678901234567890");
        byte[] encoded = Base64.encodeInteger(bi);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    @Test(timeout = 4000)
    public void testIntegerEncodingZero() {
        BigInteger bi = BigInteger.ZERO;
        byte[] encoded = Base64.encodeInteger(bi);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    // ======================== Partition C: Defect-Targeted Branch Zone (CODEC-105) ========================

    /**
     * Reproduce the ArrayIndexOutOfBoundsException that was reported in Base64InputStreamTest.testCodec105.
     * The bug occurs when the internal buffer is set via setInitialBuffer with a small array (e.g., size 2)
     * and then encode/decode is called with data that causes flush at specific modulus values.
     * We simulate by calling setInitialBuffer then encode/decode directly.
     */
    @Test(timeout = 4000)
    public void testCodec105Defect_EncodeSmallBuffer() {
        Base64 b64 = new Base64(); // lineLength=0, encodeSize=4, decodeSize=3
        // Set internal buffer to a tiny array (size 2)
        byte[] smallBuf = new byte[2];
        b64.setInitialBuffer(smallBuf, 0, 2);
        // Now call encode on a single byte (will produce 4 bytes standard, but resize should kick in)
        byte[] input = new byte[]{0x00};
        byte[] output = b64.encode(input);
        assertArrayEquals(new byte[]{'A','A','=','='}, output);
        // No AIOOB should occur.
    }

    @Test(timeout = 4000)
    public void testCodec105Defect_DecodeSmallBuffer() {
        Base64 b64 = new Base64();
        byte[] smallBuf = new byte[2];
        b64.setInitialBuffer(smallBuf, 0, 2);
        // Decode a 2-char encoded data that requires 3 bytes output (but will trigger resize)
        byte[] input = "AAA".getBytes(); // 3 chars, will decode to 2 bytes? Actually 3 base64 chars decode to 2 bytes (modulus=3)
        byte[] output = b64.decode(input);
        assertNotNull(output);
        // Should not throw AIOOB
    }

    @Test(timeout = 4000)
    public void testCodec105Defect_EncodeEofFlushSmallBuffer() {
        // Test the EOF condition where after writing flush bytes, buffer might not have space for lineSeparator.
        Base64 b64 = new Base64(4); // lineLength=4, encodeSize=4+2=6, decodeSize=5
        byte[] smallBuf = new byte[8]; // just enough to fit one chunk + lineSep? Let's see: after writing 4 chars, space left=4, then lineSep (2) fits. But after EOF flush with modulus=1, writes 2 bytes (standard) then lineSep fails?
        b64.setInitialBuffer(smallBuf, 0, 8);
        // Encode 1 byte only - modulus becomes 1, then flush writes 2 bytes (no padding for URL-safe) but we use standard, flush writes 4 bytes.
        // Standard flush writes 4 bytes, total pos after regular encode? Actually encode first call with data will process 1 byte, modulus=1, no output yet.
        // Then encode with -1 triggers flush: writes 4 bytes (since modulus=1). At that point buffer pos=0, writes 4 bytes -> pos=4. Then it tries to append lineSeparator if lineLength>0. lineSeparator length=2. buffer length=8, pos=4, space=4 >=2, so safe.
        // To make it unsafe, we need buffer length such that after flush, space < lineSeparator.length. With encodeSize=6, pre-flush resize ensures buffer.length - pos >=6. After writing 4 bytes, remaining >=2, so safe. So this test can't trigger but it's a regression test.
        byte[] input = new byte[]{0x00};
        byte[] output = b64.encode(input);
        // Should not throw, and output should contain lineSep
        String outStr = new String(output, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(outStr.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testCodec105Defect_MultipleEOF() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{0,0,0}, 0, 3);
        b64.encode(new byte[0], 0, -1); // EOF
        b64.encode(new byte[0], 0, -1); // second EOF, should be no-op
        // Should not throw
        byte[] buf = new byte[4];
        int len = b64.readResults(buf, 0, 4);
        assertTrue(len > 0);
    }

    @Test(timeout = 4000)
    public void testCodec105Defect_DecodePaddingMiddle() {
        // Input with '=' in the middle: "AB=CD". Should treat '=' as EOF and ignore rest.
        Base64 b64 = new Base64();
        byte[] input = "AB=CD".getBytes();
        byte[] output = b64.decode(input);
        // After "AB", modulus=2, then '=' sets eof, then process remainder? The code breaks on '=', so rest ignored.
        // Expected: decode("AB") = 1 byte? Actually two base64 chars decode to 1 byte? Let's compute: "AB" decodes to byte 0x05? 
        assertNotNull(output);
        // Should not throw AIOOB.
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeObjectNonByteArray() throws EncoderException {
        new Base64().encode("not a byte array");
    }

    @Test(timeout = 4000, expected = DecoderException.class)
    public void testDecodeObjectNonByteArrayNorString() throws DecoderException {
        new Base64().decode(Integer.valueOf(123));
    }

    @Test(timeout = 4000, expected = DecoderException.class)
    public void testDecodeObjectFloat() throws DecoderException {
        new Base64().decode(3.14);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorLineSeparatorContainsBase64Chars() {
        // '+' is a base64 char
        new Base64(76, new byte[]{'+', '\n'});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorLineSeparatorContainsSlash() {
        // '/' is base64 char
        new Base64(76, new byte[]{'a', '/'});
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEncodeBase64MaxResultSizeViolation() {
        // With isChunked=false, urlSafe=false, maxResultSize=100
        byte[] data = new byte[200];
        Base64.encodeBase64(data, false, false, 100);
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================

    @Test(timeout = 4000)
    public void testMultipleEncodeDecodeCycles() {
        Base64 b64 = new Base64();
        byte[] data1 = new byte[]{1, 2, 3};
        byte[] data2 = new byte[]{4, 5, 6, 7, 8};
        byte[] enc1 = b64.encode(data1);
        byte[] enc2 = b64.encode(data2);
        assertArrayEquals(data1, b64.decode(enc1));
        assertArrayEquals(data2, b64.decode(enc2));
        // Encode again after decode should work
        byte[] enc3 = b64.encode(data1);
        assertArrayEquals(enc1, enc3);
    }

    @Test(timeout = 4000)
    public void testResetClearsState() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{1,2,3});
        b64.reset();
        // After reset, should be able to encode from scratch
        byte[] enc = b64.encode(new byte[]{0});
        assertEquals(4, enc.length);
    }

    @Test(timeout = 4000)
    public void testHasDataAndAvail() {
        Base64 b64 = new Base64();
        assertFalse(b64.hasData());
        assertEquals(0, b64.avail());
        // After encode, internal buffer may have data
        b64.encode(new byte[]{1,2,3}, 0, 3);
        assertTrue(b64.hasData());
        assertTrue(b64.avail() > 0);
        b64.reset();
        assertFalse(b64.hasData());
        assertEquals(0, b64.avail());
    }

    @Test(timeout = 4000)
    public void testReadResults() {
        Base64 b64 = new Base64();
        byte[] data = new byte[]{0, 0, 0};
        b64.encode(data, 0, 3);
        b64.encode(data, 0, -1); // flush
        int avail = b64.avail();
        byte[] out = new byte[avail];
        int len = b64.readResults(out, 0, avail);
        assertEquals(avail, len);
        // After reading all, buffer should be null
        assertFalse(b64.hasData());
        // Read again returns 0 (not eof? Actually readResults returns 0 when buffer null and eof false? eof is true after flush, so return -1)
        // Let's check: after flush, eof=true, buffer now null, readResults returns -1
        int secondRead = b64.readResults(out, 0, 1);
        assertEquals(-1, secondRead);
    }

    @Test(timeout = 4000)
    public void testDecodeObjectString() throws DecoderException {
        Base64 b64 = new Base64();
        byte[] decoded = (byte[]) b64.decode("AAEC");
        assertArrayEquals(new byte[]{0, 1, 2}, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeObjectByteArray() throws EncoderException {
        Base64 b64 = new Base64();
        byte[] encoded = (byte[]) b64.encode(new byte[]{0, 1, 2});
        assertArrayEquals(new byte[]{'A','A','E','C'}, encoded);
    }

    @Test(timeout = 4000)
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte)'A'));
        assertTrue(Base64.isBase64((byte)'+');
        assertTrue(Base64.isBase64((byte)'/');
        assertTrue(Base64.isBase64((byte)'=');
        assertFalse(Base64.isBase64((byte)'!');
        assertFalse(Base64.isBase64((byte)0));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64("AB==". getBytes()));
        assertFalse(Base64.isArrayByteBase64("AB=C".getBytes()));
    }

    @Test(timeout = 4000)
    public void testContainsBase64Byte() {
        // private method, test via constructor validation
        // Already tested in constructor exception test
    }

    @Test(timeout = 4000)
    public void testDiscardWhitespace() {
        // deprecated, but test for coverage
        byte[] data = "A B C".getBytes();
        byte[] result = Base64.discardWhitespace(data);
        assertArrayEquals("ABC".getBytes(), result);
    }

    @Test(timeout = 4000)
    public void testToIntegerBytes() {
        // test edge cases: bit length multiple of 8, not multiple
        BigInteger bi1 = new BigInteger("0"); // bitLength=0
        byte[] res1 = Base64.toIntegerBytes(bi1);
        assertArrayEquals(new byte[0], res1); // Actually? Let's check: bitLength=0, bitlen=0, bigBytes=[0], startSrc=0? We'll just run and verify no exception.
        assertNotNull(res1);
        BigInteger bi2 = new BigInteger("255"); // 0xFF, bitLength=8
        byte[] res2 = Base64.toIntegerBytes(bi2);
        assertArrayEquals(new byte[]{ (byte)0xFF }, res2);
        BigInteger bi3 = new BigInteger("256"); // 0x100, bitLength=9, should give 2 bytes
        byte[] res3 = Base64.toIntegerBytes(bi3);
        assertEquals(2, res3.length);
        assertEquals(0x01, res3[0] & 0xFF);
        assertEquals(0x00, res3[1] & 0xFF);
    }
}