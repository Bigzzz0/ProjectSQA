package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.binary.Base64
 * Primary Defect Targeted:
 *   - Defects4J Streaming Base64 Empty Input Flaw:
 *     In encode(byte[], int, int), when called with EOF (inAvail < 0) on an empty stream,
 *     if lineLength > 0, the chunk separator was unconditionally appended to the buffer
 *     even when pos == 0 and modulus == 0. This caused streaming operations on empty input
 *     to return the line separator (e.g. CR/LF, byte 13) instead of EOF (-1).
 *
 * Decision / Branch Matrix Covered:
 *   1. Constructors & Mode Configuration:
 *      - Base64(), Base64(boolean), Base64(int), Base64(int, byte[]), Base64(int, byte[], boolean)
 *      - Illegal lineSeparator containing Base64 characters (throws IllegalArgumentException)
 *      - isUrlSafe() true/false branches
 *   2. State & Buffer Transitions:
 *      - hasData() and avail() on uninitialized, active, and depleted buffers
 *      - resizeBuf() when buf == null (allocates 8192) vs buf != null (doubles capacity)
 *      - setInitialBuffer() matching length vs non-matching length
 *      - readResults() when buf != b vs buf == b (buffer reuse single-round contract)
 *      - readResults() returning -1 on EOF vs 0 on active empty buffer
 *   3. Encoding Logic:
 *      - inAvail >= 0: positive byte, negative byte handling (b + 256), modulus 0, 1, 2
 *      - lineLength > 0 chunking: lineLength <= currentLinePos trigger
 *      - inAvail < 0 (EOF):
 *          * modulus == 0: no trailing pad/data
 *          * modulus == 1: 2 chars + '==' (standard) vs 2 chars without pad (URL-safe)
 *          * modulus == 2: 3 chars + '=' (standard) vs 3 chars without pad (URL-safe)
 *      - Subsequent encode calls after EOF (eof == true early exit)
 *   4. Decoding Logic:
 *      - inAvail >= 0: whitespace skipping, non-base64 character ignoring, negative byte filtering
 *      - Encountering PAD ('=') -> early EOF transition
 *      - inAvail < 0 (EOF) with remaining modulus:
 *          * modulus == 2: extracts 1 byte
 *          * modulus == 3: extracts 2 bytes
 *      - Subsequent decode calls after EOF (eof == true early exit)
 *   5. Static & Utility Methods:
 *      - isBase64: PAD, valid range, invalid range, negative values, boundary (>= DECODE_TABLE.length)
 *      - isArrayByteBase64: null/empty, whitespace-only, valid base64 with mixed whitespace, invalid byte
 *      - encodeBase64 / decodeBase64: null, empty, chunked, URL-safe
 *      - encodeInteger / decodeInteger / toIntegerBytes: null guard, byte-aligned, non-byte-aligned, zero
 *      - discardWhitespace & discardNonBase64:grooming filters
 *   6. Interface Implementations:
 *      - encode(Object) & decode(Object): byte[] success vs non-byte[] (EncoderException/DecoderException)
 * -----------------------------------------------------------------------------------------
 */
public class Base64GeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardEncodeDecodeRoundTrip() throws UnsupportedEncodingException {
        String original = "Hello World! Standard Base64 Encoding & Decoding Verification.";
        byte[] originalBytes = original.getBytes("UTF-8");

        byte[] encoded = Base64.encodeBase64(originalBytes);
        assertNotNull("Encoded array should not be null", encoded);
        assertTrue("Array should contain only valid Base64 chars", Base64.isArrayByteBase64(encoded));

        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals("Decoded result must match original input", originalBytes, decoded);
    }

    @Test(timeout = 4000)
    public void testUrlSafeEncodeDecodeRoundTrip() throws UnsupportedEncodingException {
        // Bytes chosen to produce '+' and '/' in standard base64 (0xfb, 0xff -> "+/8=")
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xfe};

        byte[] standardEncoded = Base64.encodeBase64(binaryData, false, false);
        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(binaryData);

        String standardStr = new String(standardEncoded, "UTF-8");
        String urlSafeStr = new String(urlSafeEncoded, "UTF-8");

        assertTrue("Standard encoding should contain '+' or '/'", standardStr.contains("+") || standardStr.contains("/"));
        assertFalse("URL safe encoding must not contain '+'", urlSafeStr.contains("+"));
        assertFalse("URL safe encoding must not contain '/'", urlSafeStr.contains("/"));
        assertFalse("URL safe encoding must not contain padding '='", urlSafeStr.contains("="));

        byte[] decoded = Base64.decodeBase64(urlSafeEncoded);
        assertArrayEquals("URL safe decoded bytes must match input", binaryData, decoded);
    }

    @Test(timeout = 4000)
    public void testModulusPaddingsStandardAndUrlSafe() {
        byte[] oneByte = new byte[]{ 'A' };
        byte[] twoBytes = new byte[]{ 'A', 'B' };
        byte[] threeBytes = new byte[]{ 'A', 'B', 'C' };

        // Standard with padding
        byte[] enc1 = Base64.encodeBase64(oneByte, false, false);
        byte[] enc2 = Base64.encodeBase64(twoBytes, false, false);
        byte[] enc3 = Base64.encodeBase64(threeBytes, false, false);

        assertEquals("Modulus 1 standard encoding must have length 4", 4, enc1.length);
        assertEquals('=', enc1[2]);
        assertEquals('=', enc1[3]);

        assertEquals("Modulus 2 standard encoding must have length 4", 4, enc2.length);
        assertEquals('=', enc2[3]);

        assertEquals("Modulus 0 standard encoding must have length 4 without pad", 4, enc3.length);
        assertNotEquals('=', enc3[3]);

        // URL safe without padding
        byte[] urlEnc1 = Base64.encodeBase64(oneByte, false, true);
        byte[] urlEnc2 = Base64.encodeBase64(twoBytes, false, true);
        byte[] urlEnc3 = Base64.encodeBase64(threeBytes, false, true);

        assertEquals("Modulus 1 URL-safe encoding omits pad", 2, urlEnc1.length);
        assertEquals("Modulus 2 URL-safe encoding omits pad", 3, urlEnc2.length);
        assertEquals("Modulus 0 URL-safe encoding length 4", 4, urlEnc3.length);

        // Verify decoding handles both forms
        assertArrayEquals(oneByte, Base64.decodeBase64(enc1));
        assertArrayEquals(oneByte, Base64.decodeBase64(urlEnc1));
        assertArrayEquals(twoBytes, Base64.decodeBase64(enc2));
        assertArrayEquals(twoBytes, Base64.decodeBase64(urlEnc2));
        assertArrayEquals(threeBytes, Base64.decodeBase64(enc3));
        assertArrayEquals(threeBytes, Base64.decodeBase64(urlEnc3));
    }

    @Test(timeout = 4000)
    public void testChunkedEncoding() throws UnsupportedEncodingException {
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 128);
        }

        byte[] chunked = Base64.encodeBase64Chunked(input);
        String chunkedStr = new String(chunked, "UTF-8");

        assertTrue("Chunked output must contain CRLF separator", chunkedStr.contains("\r\n"));
        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals("Chunked output must decode to original input", input, decoded);
    }

    @Test(timeout = 4000)
    public void testInitialBufferDirectReuseBranch() {
        Base64 b64 = new Base64(0);
        byte[] out = new byte[4];
        b64.setInitialBuffer(out, 0, 4);

        b64.encode(new byte[]{ 'A', 'B', 'C' }, 0, 3);

        // readResults with the SAME buffer triggers the `buf == b` branch
        int read = b64.readResults(out, 0, 4);
        assertEquals("Should have read 4 bytes into the shared buffer", 4, read);
        assertFalse("Buffer must be set to null after direct reuse", b64.hasData());
    }

    @Test(timeout = 4000)
    public void testBufferResizingOnLargeStreamingEncodeAndDecode() {
        Base64 encoder = new Base64(0);
        byte[] largeData = new byte[16384]; // larger than default 8192 buffer
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i & 0xFF);
        }

        encoder.encode(largeData, 0, largeData.length);
        encoder.encode(largeData, 0, -1); // EOF

        int avail = encoder.avail();
        assertTrue("Encoder buffer must have grown beyond 8192 bytes", avail > 8192);

        byte[] encodedOutput = new byte[avail];
        int bytesRead = encoder.readResults(encodedOutput, 0, avail);
        assertEquals(avail, bytesRead);

        // Decode in streaming mode to test decode resizeBuf()
        Base64 decoder = new Base64(0);
        decoder.decode(encodedOutput, 0, encodedOutput.length);
        decoder.decode(encodedOutput, 0, -1);

        int decAvail = decoder.avail();
        assertEquals(largeData.length, decAvail);
        byte[] decodedOutput = new byte[decAvail];
        decoder.readResults(decodedOutput, 0, decAvail);

        assertArrayEquals("Resized streaming buffers must retain data integrity", largeData, decodedOutput);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyArrayBoundaries() {
        assertNull("Null input to encodeBase64 returns null", Base64.encodeBase64(null));
        assertNull("Null input to decodeBase64 returns null", Base64.decodeBase64(null));

        byte[] empty = new byte[0];
        assertSame("Empty array to encodeBase64 returns original empty array", empty, Base64.encodeBase64(empty));
        assertSame("Empty array to decodeBase64 returns original empty array", empty, Base64.decodeBase64(empty));

        assertTrue("Empty array is valid Base64", Base64.isArrayByteBase64(empty));
    }

    @Test(timeout = 4000)
    public void testIsBase64Boundaries() {
        assertTrue("Pad char '=' must be Base64", Base64.isBase64((byte) '='));
        assertTrue("Character 'A' (0) must be Base64", Base64.isBase64((byte) 'A'));
        assertTrue("Character 'z' (51) must be Base64", Base64.isBase64((byte) 'z'));
        assertTrue("Character '0' (52) must be Base64", Base64.isBase64((byte) '0'));
        assertTrue("Character '9' (61) must be Base64", Base64.isBase64((byte) '9'));
        assertTrue("Character '+' (62) must be Base64", Base64.isBase64((byte) '+'));
        assertTrue("Character '/' (63) must be Base64", Base64.isBase64((byte) '/'));
        assertTrue("Character '-' (URL safe 62) must be Base64", Base64.isBase64((byte) '-'));
        assertTrue("Character '_' (URL safe 63) must be Base64", Base64.isBase64((byte) '_'));

        assertFalse("Negative byte must not be Base64", Base64.isBase64((byte) -1));
        assertFalse("Negative byte -128 must not be Base64", Base64.isBase64((byte) -128));
        assertFalse("Byte out of decode table bounds (>123) must not be Base64", Base64.isBase64((byte) 124));
        assertFalse("Byte 127 must not be Base64", Base64.isBase64((byte) 127));
        assertFalse("Non-Base64 character '$' must not be Base64", Base64.isBase64((byte) '$'));
        assertFalse("Whitespace ' ' must not be Base64", Base64.isBase64((byte) ' '));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64Permutations() {
        byte[] validWithWhitespace = new byte[]{'S