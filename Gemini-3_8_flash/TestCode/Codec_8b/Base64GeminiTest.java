package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect: CODEC-105 (Base64InputStreamTest::testCodec105 -> ArrayIndexOutOfBoundsException: 2)
 *    - Root cause: Buffer under-allocation/out-of-bounds when decoding/encoding with small caller-provided
 *      buffers via streaming buffer handoff (setInitialBuffer / readResults).
 *    - Targeting: testCodec105DefectStream, testCodec105DirectBufferHandoffDecode, testCodec105DirectBufferHandoffEncode.
 *
 * 2. Decision Branches Covered:
 *    - Constructors: urlSafe true/false, lineLength > 0 / <= 0, lineSeparator null / valid / containing Base64 byte.
 *    - LineLength rounding: lineLength > 0 -> (lineLength / 4) * 4.
 *    - Modulus paths in encode():
 *      - inAvail < 0: modulus 0 (no-op), modulus 1 (2 chars + 2 pad / no pad in urlSafe), modulus 2 (3 chars + 1 pad / no pad in urlSafe).
 *      - inAvail >= 0: modulus transitions 1, 2, 0 (output 4 chars); line separator injection on currentLinePos >= lineLength.
 *      - Trailing CRLF suppression: buffer[pos - 1] == lineSeparator[last].
 *    - Modulus paths in decode():
 *      - Encountering PAD ('='): early EOF break.
 *      - Table bounds and valid Base64 / non-Base64 byte filtering (DECODE_TABLE[b] != -1).
 *      - EOF with modulus != 0: switch modulus 2 (1 byte output), switch modulus 3 (2 bytes output), modulus 1 (discarded).
 *    - Buffer lifecycle & resizing:
 *      - buffer == null -> DEFAULT_BUFFER_SIZE allocation.
 *      - buffer resizing with DEFAULT_BUFFER_RESIZE_FACTOR.
 *      - readResults: len = Math.min(avail, bAvail), readPos >= pos -> buffer = null, eof ? -1 : 0.
 *    - Static helpers & wrappers:
 *      - isBase64: PAD, valid bytes, negative bytes, out-of-table bytes, whitespace bytes.
 *      - isArrayByteBase64: empty array, all-valid, whitespace-interspersed, invalid byte encounter.
 *      - discardWhitespace: whitespace chars (' ', '\t', '\r', '\n') stripped, regular bytes preserved.
 *      - encodeBase64 with maxResultSize: len > maxResultSize throws IllegalArgumentException.
 *      - BigInteger encode/decode: null check (NPE), byte alignment (bitLength % 8 == 0 vs != 0), sign bit stripping.
 *      - Type dispatch in Object encode(Object) and Object decode(Object): byte[], String, and unsupported type.
 */
public class Base64GeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-105)
    // =========================================================================

    /**
     * Exact reproduction of CODEC-105 via Base64InputStream reading into a 2-byte buffer.
     * Triggers ArrayIndexOutOfBoundsException on the buggy implementation when buffer hand-off occurs.
     */
    @Test(timeout = 4000)
    public void testCodec105DefectStream() throws IOException {
        final Base64InputStream in = new Base64InputStream(
                new ByteArrayInputStream(new byte[] { '0', '0', '0', '0' }));
        final byte[] buf = new byte[2];

        int c = in.read(buf, 0, 2);
        assertEquals("First read must extract 2 bytes", 2, c);

        c = in.read(buf, 0, 2);
        assertEquals("Second read must extract 1 byte remaining from 3-byte decoded block", 1, c);

        c = in.read(buf, 0, 2);
        assertEquals("Third read must encounter EOF (-1)", -1, c);
        in.close();
    }

    /**
     * Direct test against Base64 internal buffer handoff mechanism (setInitialBuffer)
     * during decode operations where requested buffer length is smaller than decodeSize (3).
     */
    @Test(timeout = 4000)
    public void testCodec105DirectBufferHandoffDecode() {
        final Base64 b64 = new Base64();
        final byte[] out = new byte[2];
        b64.setInitialBuffer(out, 0, 2);

        final byte[] input = new byte[] { 'A', 'A', 'A', 'A' }; // Decodes to 3 zero bytes
        b64.decode(input, 0, input.length);
        b64.decode(input, 0, -1);

        final byte[] dest = new byte[3];
        final int read = b64.readResults(dest, 0, 3);
        assertEquals("Must successfully decode and buffer all 3 bytes without index exceptions", 3, read);
        assertArrayEquals(new byte[] { 0, 0, 0 }, dest);
    }

    /**
     * Direct test against Base64 internal buffer handoff mechanism during encode operations
     * where requested buffer length is smaller than encodeSize (4).
     */
    @Test(timeout = 4000)
    public void testCodec105DirectBufferHandoffEncode() {
        final Base64 b64 = new Base64();
        final byte[] out = new byte[2];
        b64.setInitialBuffer(out, 0, 2);

        final byte[] input = new byte[] { 1, 2, 3 }; // Encodes to 4 bytes: "AQID"
        b64.encode(input, 0, input.length);
        b64.encode(input, 0, -1);

        final byte[] dest = new byte[4];
        final int read = b64.readResults(dest, 0, 4);
        assertEquals("Must successfully encode all 4 bytes after buffer expansion", 4, read);
        assertEquals("AQID", StringUtils.newStringUtf8(dest));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeStandardModulusTransitions() {
        final Base64 b64 = new Base64(0);

        // Modulus 1 (1 input byte -> 2 chars + 2 pad '=')
        final byte[] enc1 = b64.encode(new byte[] { 'a' });
        assertEquals("YQ==", StringUtils.newStringUtf8(enc1));

        // Modulus 2 (2 input bytes -> 3 chars + 1 pad '=')
        final byte[] enc2 = b64.encode(new byte[] { 'a', 'b' });
        assertEquals("YWI=", StringUtils.newStringUtf8(enc2));

        // Modulus 0 (3 input bytes -> 4 chars, 0 pad)
        final byte[] enc3 = b64.encode(new byte[] { 'a', 'b', 'c' });
        assertEquals("YWJj", StringUtils.newStringUtf8(enc3));
    }

    @Test(timeout = 4000)
    public void testEncodeUrlSafeTransitions() {
        final Base64 b64UrlSafe = new Base64(0, Base64.CHUNK_SEPARATOR, true);
        assertTrue(b64UrlSafe.isUrlSafe());

        // Characters that translate to '+' and '/' in standard must be '-' and '_' in URL-safe
        final byte[] binaryData = new byte[] { (byte) 0xfb, (byte) 0xff, (byte) 0xbf };
        final byte[] std = Base64.encodeBase64(binaryData, false, false);
        assertEquals("+/+/", StringUtils.newStringUtf8(std));

        final byte[] urlSafe = Base64.encodeBase64(binaryData, false, true);
        assertEquals("-_-_", StringUtils.newStringUtf8(urlSafe));

        // URL-Safe must omit padding in modulus 1 and 2
        final byte[] enc1 = b64UrlSafe.encode(new byte[] { 'a' });
        assertEquals("YQ", StringUtils.newStringUtf8(enc1));

        final byte[] enc2 = b64UrlSafe.encode(new byte[] { 'a', 'b' });
        assertEquals("YWI", StringUtils.newStringUtf8(enc2));
    }

    @Test(timeout = 4000)
    public void testDecodeStandardAndUrlSafe() {
        final Base64 b64 = new Base64();

        // Decode standard padded
        assertArrayEquals(new byte[] { 'a' }, b64.decode("YQ=="));
        assertArrayEquals(new byte[] { 'a', 'b' }, b64.decode("YWI="));
        assertArrayEquals(new byte[] { 'a', 'b', 'c' }, b64.decode("YWJj"));

        // Decode unpadded (URL-safe style) seamlessly
        assertArrayEquals(new byte[] { 'a' }, b64.decode("YQ"));
        assertArrayEquals(new byte[] { 'a', 'b' }, b64.decode("YWI"));

        // Decode URL-safe characters '-' and '_'
        final byte[] expected = new byte[] { (byte) 0xfb, (byte) 0xff, (byte) 0xbf };
        assertArrayEquals(expected, b64.decode("-_-_"));
        assertArrayEquals(expected, b64.decode("+/+/"));
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingAndLineSeparator() {
        // Chunk size 4 with standard CRLF separator
        final Base64 chunked = new Base64(4, new byte[] { '$', '%' });
        final byte[] input = new byte[] { '1', '2', '3', '4', '5', '6' }; // 6 bytes -> 8 Base64 chars
        final byte[] encoded = chunked.encode(input);

        // Expected format: 4 chars + separator + 4 chars + separator
        final String expected = "MTIz$%NDU2$%";
        assertEquals(expected, StringUtils.newStringUtf8(encoded));
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingDoesNotDoubleAppendSeparator() {
        // If lineSeparator happens to end with the character already written, branch pos > 0 check
        final Base64 b64 = new Base64(4, new byte[] { '=' });
        final byte[] input = new byte[] { 'a' }; // encodes to "YQ=="
        final byte[] enc = b64.encode(input);
        // The last character before EOF is PAD ('='). The separator is also '='.
        // It should avoid appending the duplicate separator.
        assertEquals("YQ==", StringUtils.newStringUtf8(enc));
    }

    @Test(timeout = 4000)
    public void testLineLengthRoundingDownToMultipleOf4() {
        // Line length 7 rounded down to (7 / 4) * 4 = 4
        final Base64 b64 = new Base64(7, new byte[] { '\n' });
        final byte[] input = new byte[] { '1', '2', '3', '4', '5', '6' };
        final byte[] enc = b64.encode(input);
        assertEquals("MTIz\nNDU2\n", StringUtils.newStringUtf8(enc));
    }

    @Test(timeout = 4000)
    public void testStreamingMultiStepEncodeAndDecode() {
        final Base64 b64 = new Base64();

        // Feed data in two chunks, then signal EOF
        b64.encode(new byte[] { 'H', 'e' }, 0, 2);
        assertTrue(b64.hasData());
        b64.encode(new byte[] { 'l', 'l', 'o' }, 0, 3);
        b64.encode(new byte[0], 0, -1); // Signal EOF

        final int avail = b64.avail();
        final byte[] out = new byte[avail];
        final int read = b64.readResults(out, 0, avail);
        assertEquals(avail, read);
        assertEquals("SGVsbG8=", StringUtils.newStringUtf8(out));

        // Submitting data after EOF should be silently ignored
        b64.encode(new byte[] { '!' }, 0, 1);
        assertEquals(0, b64.avail());

        // Decode streaming
        final Base64 decB64 = new Base64();
        final byte[] encBytes = StringUtils.getBytesUtf8("SGVsbG8=");
        decB64.decode(encBytes, 0, 4); // "SGVs"
        decB64.decode(encBytes, 4, 4); // "bG8="
        decB64.decode(new byte[0], 0, -1);

        final byte[] decOut = new byte[decB64.avail()];
        decB64.readResults(decOut, 0, decOut.length);
        assertEquals("Hello", StringUtils.newStringUtf8(decOut));

        // Submitting decode after EOF should be ignored
        decB64.decode(encBytes, 0, 4);
        assertFalse(decB64.hasData());
    }

    @Test(timeout = 4000)
    public void testReadResultsPartialRead() {
        final Base64 b64 = new Base64();
        b64.encode(new byte[] { 1, 2, 3, 4, 5, 6 }, 0, 6);
        b64.encode(new byte[0], 0, -1);

        assertEquals(8, b64.avail());
        final byte[] part1 = new byte[3];
        final int read1 = b64.readResults(part1, 0, 3);
        assertEquals(3, read1);
        assertEquals(5, b64.avail());
        assertTrue(b64.hasData());

        final byte[] part2 = new byte[5];
        final int read2 = b64.readResults(part2, 0, 5);
        assertEquals(5, read2);
        assertEquals(0, b64.avail());
        assertFalse(b64.hasData()); // readPos >= pos -> buffer resets to null

        // Subsequent read on empty buffer at EOF returns -1
        final int readAfterEOF = b64.readResults(new byte[1], 0, 1);
        assertEquals(-1, readAfterEOF);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeAndDecodeNullOrEmpty() {
        final Base64 b64 = new Base64();

        assertNull(b64.encode((byte[]) null));
        assertNull(b64.decode((byte[]) null));
        assertNull(b64.decode((String) null));

        final byte[] empty = new byte[0];
        assertSame(empty, b64.encode(empty));
        assertSame(empty, b64.decode(empty));

        assertNull(Base64.encodeBase64(null));
        assertArrayEquals(empty, Base64.encodeBase64(empty));

        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(empty, Base64.decodeBase64(empty));
        assertArrayEquals(empty, Base64.decodeBase64(""));
    }

    @Test(timeout = 4000)
    public void testStaticConvenienceWrappers() {
        final byte[] input = StringUtils.getBytesUtf8("Commons Codec Base64");
        final String encodedStr = Base64.encodeBase64String(input);
        assertNotNull(encodedStr);
        assertArrayEquals(input, Base64.decodeBase64(encodedStr));

        final byte[] chunked = Base64.encodeBase64Chunked(input);
        assertTrue(StringUtils.newStringUtf8(chunked).endsWith("\r\n"));

        final String urlSafeStr = Base64.encodeBase64URLSafeString(input);
        assertFalse(urlSafeStr.contains("+"));
        assertFalse(urlSafeStr.contains("/"));
        assertFalse(urlSafeStr.contains("="));
        assertArrayEquals(input, Base64.decodeBase64(urlSafeStr));

        final byte[] urlSafeBytes = Base64.encodeBase64URLSafe(input);
        assertArrayEquals(StringUtils.getBytesUtf8(urlSafeStr), urlSafeBytes);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullLineSeparator() {
        // null lineSeparator should reset lineLength to 0 and use CHUNK_SEPARATOR safely
        final Base64 b64 = new Base64(76, null);
        final byte[] encoded = b64.encode(new byte[] { 1, 2, 3 });
        assertEquals("AQID", StringUtils.newStringUtf8(encoded)); // no chunking applied
    }

    @Test(timeout = 4000)
    public void testBufferAutoResize() {
        final Base64 b64 = new Base64();
        // DEFAULT_BUFFER_SIZE is 8192. Produce input that exceeds 8192 decoded & encoded bytes
        final byte[] largeData = new byte[10000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 127);
        }
        final byte[] encoded = b64.encode(largeData);
        assertNotNull(encoded);
        assertTrue(encoded.length > largeData.length);

        final byte[] decoded = b64.decode(encoded);
        assertArrayEquals(largeData, decoded);
    }

    @Test(timeout = 4000)
    public void testIsBase64OctetEvaluations() {
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));

        assertFalse(Base64.isBase64((byte) -1));
        assertFalse(Base64.isBase64((byte) -128));
        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) '\n'));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) 127));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64Evaluations() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("YWJj\r\n \t")));
        assertFalse(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("YWJj!")));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDiscardWhitespace() {
        final byte[] dirty = StringUtils.getBytesUtf8(" Y \t W \n B \r j ");
        final byte[] cleaned = Base64.discardWhitespace(dirty);
        assertEquals("YWJj", StringUtils.newStringUtf8(cleaned));

        final byte[] clean = StringUtils.getBytesUtf8("YWJj");
        final byte[] identical = Base64.discardWhitespace(clean);
        assertArrayEquals(clean, identical);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorRejectsBase64InSeparator() {
        // 'A' is in the Base64 alphabet, which is illegal in lineSeparator
        new Base64(76, new byte[] { 'A', '\n' });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEncodeExceedsMaxResultSize() {
        final byte[] data = new byte[100];
        // 100 bytes will encode to ~136 bytes; maxResultSize of 50 must throw IllegalArgumentException
        Base64.encodeBase64(data, false, false, 50);
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testObjectEncodeThrowsOnNonByteArray() throws Exception {
        final Base64 b64 = new Base64();
        b64.encode("StringNotAllowed");
    }

    @Test(expected = DecoderException.class, timeout = 4000)
    public void testObjectDecodeThrowsOnInvalidType() throws Exception {
        final Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(12345));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEncodeIntegerNullThrowsNPE() {
        Base64.encodeInteger(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Polymorphism & BigInteger Crypto Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectEncodeAndDecodeValidTypes() throws Exception {
        final Base64 b64 = new Base64();

        final byte[] raw = new byte[] { 'x', 'y', 'z' };
        final Object encodedObj = b64.encode((Object) raw);
        assertTrue(encodedObj instanceof byte[]);
        assertEquals("eHl6", StringUtils.newStringUtf8((byte[]) encodedObj));

        final Object decodedBytes = b64.decode(encodedObj);
        assertTrue(decodedBytes instanceof byte[]);
        assertArrayEquals(raw, (byte[]) decodedBytes);

        final Object decodedStr = b64.decode((Object) "eHl6");
        assertTrue(decodedStr instanceof byte[]);
        assertArrayEquals(raw, (byte[]) decodedStr);
    }

    @Test(timeout = 4000)
    public void testEncodeToString() {
        final Base64 b64 = new Base64();
        final String encoded = b64.encodeToString(new byte[] { 'f', 'o', 'o' });
        assertEquals("Zm9v", encoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerEncodingAndDecoding() {
        // Zero
        final BigInteger zero = BigInteger.ZERO;
        final byte[] encZero = Base64.encodeInteger(zero);
        assertEquals(zero, Base64.decodeInteger(encZero));

        // Standard positive number with byte alignment exactly bitLength % 8 == 0
        final BigInteger exactAligned = new BigInteger("255"); // 8 bits -> 0x00FF (sign bit stripped)
        final byte[] encAligned = Base64.encodeInteger(exactAligned);
        assertEquals(exactAligned, Base64.decodeInteger(encAligned));

        // BigInteger non-byte-aligned bitLength % 8 != 0
        final BigInteger notAligned = new BigInteger("12345678901234567890");
        final byte[] encNotAligned = Base64.encodeInteger(notAligned);
        assertEquals(notAligned, Base64.decodeInteger(encNotAligned));

        // Very large BigInteger
        final BigInteger largeInt = new BigInteger("9876543210987654321098765432109876543210");
        final byte[] encLarge = Base64.encodeInteger(largeInt);
        assertEquals(largeInt, Base64.decodeInteger(encLarge));
    }

    @Test(timeout = 4000)
    public void testToIntegerBytesDirectCoverage() {
        // Test byte array representation edge-cases: bitlen not divisible by 8 vs divisible by 8
        final BigInteger biAligned = BigInteger.valueOf(128); // 8 bits value + sign bit = 9 bits
        final byte[] res1 = Base64.toIntegerBytes(biAligned);
        assertEquals(1, res1.length);
        assertEquals((byte) 128, res1[0]);

        final BigInteger biUnpadded = BigInteger.valueOf(1);
        final byte[] res2 = Base64.toIntegerBytes(biUnpadded);
        assertEquals(1, res2.length);
        assertEquals((byte) 1, res2[0]);
    }

    @Test(timeout = 4000)
    public void testDecodeGarbageAndNoiseIgnored() {
        final Base64 b64 = new Base64();
        // Embedded non-Base64 noise ('!', '@', '#', whitespace) should be ignored
        final byte[] decoded = b64.decode("Y ! @ W # J $ % c");
        assertEquals("abc", StringUtils.newStringUtf8(decoded));

        // High-bit characters not in Base64 table
        final byte[] highBitData = new byte[] { 'Y', (byte) 0x80, (byte) 0xFF, 'W', 'J', 'j' };
        final byte[] decodedHigh = b64.decode(highBitData);
        assertEquals("abc", StringUtils.newStringUtf8(decodedHigh));
    }

    @Test(timeout = 4000)
    public void testDecodeModulusEdgeCase1Discarded() {
        // A single Base64 character yields only 6 bits, which is less than 1 byte.
        // It must be cleanly ignored when EOF arrives (modulus 1 path).
        final Base64 b64 = new Base64();
        final byte[] decoded = b64.decode(new byte[] { 'A' });
        assertEquals(0, decoded.length);
    }

    @Test(timeout = 4000)
    public void testSetInitialBufferMismatchedLengthIgnored() {
        final Base64 b64 = new Base64();
        final byte[] buf = new byte[10];
        // When out.length != outAvail, setInitialBuffer must do nothing
        b64.setInitialBuffer(buf, 0, 5);
        assertFalse(b64.hasData());
    }

    @Test(timeout = 4000)
    public void testConstructorsCoveringAllSignatures() {
        final Base64 b1 = new Base64();
        assertFalse(b1.isUrlSafe());

        final Base64 b2 = new Base64(true);
        assertTrue(b2.isUrlSafe());

        final Base64 b3 = new Base64(64);
        assertFalse(b3.isUrlSafe());

        final Base64 b4 = new Base64(64, new byte[] { '\n' });
        assertFalse(b4.isUrlSafe());

        final Base64 b5 = new Base64(64, new byte[] { '\n' }, true);
        assertTrue(b5.isUrlSafe());
    }
}