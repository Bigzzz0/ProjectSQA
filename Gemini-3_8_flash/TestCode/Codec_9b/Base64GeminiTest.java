/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.binary.Base64
 *
 * Targeted Defects & Branches:
 * 1. Defect CODEC-112 (Partition C):
 *    - In encodeBase64(byte[], boolean isChunked, boolean urlSafe, int maxResultSize),
 *      getEncodeLength uses MIME_CHUNK_SIZE even when isChunked is false.
 *    - When encoding unchunked single/few bytes with maxResultSize matching actual encoded
 *      size (e.g. 4 bytes for 1 input byte), buggy implementation computes len=6 (adding
 *      separator length) and throws IllegalArgumentException unexpectedly.
 * 2. Constructors & Configuration (Partition A & D):
 *    - Base64() default vs Base64(boolean urlSafe) vs Base64(int lineLength).
 *    - Base64(int lineLength, byte[] lineSeparator, boolean urlSafe) with null lineSeparator
 *      (disables chunking, uses CHUNK_SEPARATOR fallback).
 *    - lineSeparator containing Base64 alphabet bytes -> throws IllegalArgumentException.
 *    - lineLength > 0 (rounded down to multiple of 4) vs lineLength <= 0.
 * 3. Streaming & Buffering Logic:
 *    - Buffer expansion (resizeBuffer) under small and large payloads.
 *    - readResults, avail, hasData across partial reads and buffer drain.
 *    - EOF handling (inAvail < 0) when modulus is 0, 1, or 2.
 *    - Line breaking / chunking logic in encode (currentLinePos, lineLength checks).
 *    - Trailing separator avoidance if last character matches separator tail.
 * 4. Decoding Matrix:
 *    - Decoding byte[], String, and Object interface.
 *    - Whitespace skipping (' ', '\t', '\r', '\n') and discarded bytes.
 *    - Padding character ('=') handling with partial modulus (modulus 2 and 3).
 *    - Arbitrary non-base64 characters ignored (garbage-in, garbage-out).
 *    - Malformed or non-byte[]/non-String input in decode(Object) -> DecoderException.
 * 5. BigInteger Conversion & BVA:
 *    - encodeInteger / decodeInteger round-trip.
 *    - Null BigInteger parameter -> NullPointerException.
 *    - toIntegerBytes boundary: bitLength % 8 == 0 vs bitLength % 8 != 0.
 *    - Zero, positive, large 256-bit BigIntegers.
 * 6. Static Convenience Methods:
 *    - isBase64(byte), isBase64(String), isBase64(byte[]), isArrayByteBase64(byte[]).
 *    - discardWhitespace(byte[]).
 *    - encodeBase64String, encodeBase64URLSafe, encodeBase64URLSafeString, encodeBase64Chunked.
 * -----------------------------------------------------------------------------------------
 */

package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

public class Base64GeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndEncodeDecode() {
        Base64 b64 = new Base64();
        assertFalse("Default Base64 should not be URL-safe", b64.isUrlSafe());
        assertFalse("Buffer should initially have no data", b64.hasData());
        assertEquals("Available bytes should initially be 0", 0, b64.avail());

        byte[] original = "Hello World!".getBytes();
        byte[] encoded = b64.encode(original);
        assertNotNull(encoded);
        assertEquals("SGVsbG8gV29ybGQh", StringUtils.newStringUtf8(encoded));

        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUrlSafeConstructor() {
        Base64 b64UrlSafe = new Base64(true);
        assertTrue("Expected URL-safe mode enabled", b64UrlSafe.isUrlSafe());

        // Payload with bytes that map to '+' and '/' in standard base64 (indices 62 and 63)
        // e.g. 0xfb, 0xff, 0xbf -> +/+/ in standard, -_-_ in urlSafe
        byte[] binary = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xbe};
        byte[] stdEncoded = Base64.encodeBase64(binary, false, false);
        assertEquals("+/++", StringUtils.newStringUtf8(stdEncoded));

        byte[] urlEncoded = b64UrlSafe.encode(binary);
        // Note: Base64(true) uses MIME_CHUNK_SIZE (76), so check unchunked URL-safe
        byte[] unchunkedUrl = Base64.encodeBase64URLSafe(binary);
        assertEquals("-_--", StringUtils.newStringUtf8(unchunkedUrl));
    }

    @Test(timeout = 4000)
    public void testModulus1And2EncodingStandardWithPadding() {
        Base64 b64 = new Base64(0); // non-chunked
        // 1 byte: 1 char remainder -> modulus 1 -> 2 b64 digits + 2 PAD '='
        byte[] oneByte = new byte[]{'M'};
        assertEquals("TQ==", b64.encodeToString(oneByte));

        // 2 bytes: 2 chars remainder -> modulus 2 -> 3 b64 digits + 1 PAD '='
        byte[] twoBytes = new byte[]{'M', 'a'};
        assertEquals("TWE=", b64.encodeToString(twoBytes));

        // 3 bytes: exact multiple of 3 -> modulus 0 -> 4 b64 digits, 0 PAD
        byte[] threeBytes = new byte[]{'M', 'a', 'n'};
        assertEquals("TWFu", b64.encodeToString(threeBytes));
    }

    @Test(timeout = 4000)
    public void testModulus1And2EncodingUrlSafeWithoutPadding() {
        Base64 b64 = new Base64(0, new byte[]{'\r', '\n'}, true);
        assertTrue(b64.isUrlSafe());

        byte[] oneByte = new byte[]{'M'};
        assertEquals("TQ", b64.encodeToString(oneByte));

        byte[] twoBytes = new byte[]{'M', 'a'};
        assertEquals("TWE", b64.encodeToString(twoBytes));
    }

    @Test(timeout = 4000)
    public void testDecodingModulusVariations() {
        Base64 b64 = new Base64();

        // 2 b64 digits decoded (modulus 2)
        byte[] decoded1 = b64.decode("TQ==");
        assertArrayEquals(new byte[]{'M'}, decoded1);

        // Optional padding support: no trailing '='
        byte[] decoded1NoPad = b64.decode("TQ");
        assertArrayEquals(new byte[]{'M'}, decoded1NoPad);

        // 3 b64 digits decoded (modulus 3)
        byte[] decoded2 = b64.decode("TWE=");
        assertArrayEquals(new byte[]{'M', 'a'}, decoded2);

        byte[] decoded2NoPad = b64.decode("TWE");
        assertArrayEquals(new byte[]{'M', 'a'}, decoded2NoPad);

        // 4 b64 digits decoded
        byte[] decoded3 = b64.decode("TWFu");
        assertArrayEquals(new byte[]{'M', 'a', 'n'}, decoded3);
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingAndLineBreakBehavior() {
        // Line length 4, separator "_"
        byte[] sep = new byte[]{'_'};
        Base64 b64 = new Base64(4, sep, false);

        // 3 bytes -> 4 chars "TWFu" -> triggers chunk boundary -> "TWFu_"
        byte[] in = new byte[]{'M', 'a', 'n'};
        byte[] encoded = b64.encode(in);
        assertEquals("TWFu_", StringUtils.newStringUtf8(encoded));

        // 6 bytes -> "TWFuTWFu" -> "TWFu_TWFu_"
        byte[] in6 = new byte[]{'M', 'a', 'n', 'M', 'a', 'n'};
        assertEquals("TWFu_TWFu_", b64.encodeToString(in6));
    }

    @Test(timeout = 4000)
    public void testEncodeAvoidsDuplicateSeparatorAtEOF() {
        // Custom separator where buffer[pos-1] matches separator tail
        byte[] sep = new byte[]{'X'};
        // Constructing Base64 where line separator doesn't contain base64 character:
        // Wait: 'X' is in Base64 alphabet! So we use non-base64 characters:
        byte[] nonBase64Sep = new byte[]{'*', '#'};
        Base64 b64 = new Base64(4, nonBase64Sep, false);
        byte[] in = new byte[]{'M', 'a', 'n'}; // 4 chars TWFu + separator "*#"
        byte[] encoded = b64.encode(in);
        assertEquals("TWFu*#", StringUtils.newStringUtf8(encoded));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyInputHandling() {
        Base64 b64 = new Base64();
        assertNull(b64.encode((byte[]) null));
        assertNull(b64.decode((byte[]) null));

        assertArrayEquals(new byte[0], b64.encode(new byte[0]));
        assertArrayEquals(new byte[0], b64.decode(new byte[0]));

        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertNull(Base64.decodeBase64((String) null));

        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testLargeBufferExpansion() {
        // Generates more than DEFAULT_BUFFER_SIZE (8192) to force buffer resizing
        int size = 16384;
        byte[] large = new byte[size];
        for (int i = 0; i < size; i++) {
            large[i] = (byte) (i % 127);
        }
        Base64 b64 = new Base64(0);
        byte[] encoded = b64.encode(large);
        assertNotNull(encoded);
        assertTrue(encoded.length > size);

        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(large, decoded);
    }

    @Test(timeout = 4000)
    public void testNegativeByteValues() {
        // Ensure signed bytes (negative ints) are treated as unsigned (b += 256)
        byte[] negBytes = new byte[]{(byte) -1, (byte) -128, (byte) -50};
        byte[] encoded = Base64.encodeBase64(negBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(negBytes, decoded);
    }

    @Test(timeout = 4000)
    public void testNullLineSeparatorDefaultsToNoChunking() {
        // If lineSeparator is null, lineLength becomes 0 and separator defaults to CHUNK_SEPARATOR
        Base64 b64 = new Base64(76, null, false);
        byte[] in = new byte[100];
        byte[] encoded = b64.encode(in);
        // If chunking was disabled, no \r\n in result
        String str = StringUtils.newStringUtf8(encoded);
        assertFalse(str.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testReadResultsPartialReadAndDrain() {
        Base64 b64 = new Base64();
        // Invoke internal encode to leave data in buffer
        b64.encode(new byte[]{'A', 'B', 'C'}, 0, 3);
        b64.encode(new byte[0], 0, -1); // flush

        assertTrue(b64.hasData());
        int avail = b64.avail();
        assertTrue(avail > 0);

        byte[] dest = new byte[2];
        int read1 = b64.readResults(dest, 0, 2);
        assertEquals(2, read1);
        assertEquals(avail - 2, b64.avail());

        byte[] remainder = new byte[avail];
        int read2 = b64.readResults(remainder, 0, avail);
        assertEquals(avail - 2, read2);
        assertFalse("Buffer should be null after full read", b64.hasData());
        assertEquals(0, b64.avail());

        // Calling readResults when buffer is null and eof=true returns -1
        int readEof = b64.readResults(dest, 0, 2);
        assertEquals(-1, readEof);
    }

    @Test(timeout = 4000)
    public void testEncodeWhenAlreadyEof() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{'A'}, 0, -1); // trigger EOF
        // Calling encode again should immediately return without doing anything
        b64.encode(new byte[]{'B'}, 0, 1);
        // Decoded data should only have 'A' representation
        byte[] result = new byte[10];
        int count = b64.readResults(result, 0, 10);
        assertEquals(4, count);
    }

    @Test(timeout = 4000)
    public void testDecodeWhenAlreadyEof() {
        Base64 b64 = new Base64();
        b64.decode(new byte[]{'='}, 0, 1); // PAD triggers EOF
        // Calling decode again when EOF is true should immediately return
        b64.decode(new byte[]{'A', 'A'}, 0, 2);
        assertEquals(0, b64.avail());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-112)
    // =========================================================================

    /**
     * CODEC-112 Ground Truth:
     * Base64.encodeBase64(binaryData, isChunked, urlSafe, maxResultSize)
     * When isChunked is false, getEncodeLength mistakenly computed the length with
     * MIME_CHUNK_SIZE, adding chunk separator length (6 instead of 4).
     * If maxResultSize is set to 4 for a 1-byte input, the buggy code throws:
     * IllegalArgumentException: Input array too big, the output array would be bigger (6) than the specified maxium size of 4
     */
    @Test(timeout = 4000)
    public void testCodec112() {
        byte[] singleByte = new byte[]{(byte) 0};
        // Unchunked 1 byte encodes to exactly 4 base64 characters without CRLF
        byte[] result = Base64.encodeBase64(singleByte, false, false, 4);
        assertNotNull(result);
        assertEquals(4, result.length);
        assertEquals("AA==", StringUtils.newStringUtf8(result));
    }

    @Test(timeout = 4000)
    public void testEncodeBase64ThrowsWhenExceedingMaxSize() {
        byte[] input = new byte[100];
        try {
            Base64.encodeBase64(input, false, false, 10);
            fail("Expected IllegalArgumentException when output length exceeds maxResultSize");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Input array too big"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithBase64CharInSeparatorThrows() {
        // 'A' is in the Base64 alphabet and invalid as a line separator
        new Base64(76, new byte[]{'A', '\n'});
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testObjectEncodeNonByteArrayThrows() throws Exception {
        Base64 b64 = new Base64();
        b64.encode("Not a byte array");
    }

    @Test(expected = DecoderException.class, timeout = 4000)
    public void testObjectDecodeInvalidTypeThrows() throws Exception {
        Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(12345));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEncodeIntegerNullThrows() {
        Base64.encodeInteger(null);
    }

    // =========================================================================
    // Partition E: Object Interface, Whitespace & Utility Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectEncodeAndDecodeValid() throws Exception {
        Base64 b64 = new Base64();

        // Object encode with byte[]
        byte[] src = "Codec".getBytes();
        Object encObj = b64.encode((Object) src);
        assertTrue(encObj instanceof byte[]);

        // Object decode with byte[]
        Object decObj = b64.decode(encObj);
        assertTrue(decObj instanceof byte[]);
        assertArrayEquals(src, (byte[]) decObj);

        // Object decode with String
        Object decStrObj = b64.decode("Q29kZWM=");
        assertTrue(decStrObj instanceof byte[]);
        assertArrayEquals(src, (byte[]) decStrObj);
    }

    @Test(timeout = 4000)
    public void testWhitespaceHandling() {
        // Test embedded whitespace in input during decode
        String withWs = " S G V s \r\n b G 8 g V 2 9 y b G Q h ";
        byte[] decoded = Base64.decodeBase64(withWs);
        assertEquals("Hello World!", StringUtils.newStringUtf8(decoded));

        // Test discardWhitespace deprecated method
        byte[] raw = new byte[]{' ', 'A', '\t', 'B', '\r', 'C', '\n', 'D'};
        byte[] clean = Base64.discardWhitespace(raw);
        assertArrayEquals(new byte[]{'A', 'B', 'C', 'D'}, clean);
    }

    @Test(timeout = 4000)
    public void testIsBase64Validation() {
        // Octet checks
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -5));
        assertFalse(Base64.isBase64((byte) 127));

        // Byte array checks
        assertTrue(Base64.isBase64(new byte[]{'A', 'B', 'C', ' '}));
        assertFalse(Base64.isBase64(new byte[]{'A', '$', 'C'}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A', 'B', '\r', '\n'}));

        // String checks
        assertTrue(Base64.isBase64("TWFu"));
        assertTrue(Base64.isBase64("TWFu\r\n"));
        assertFalse(Base64.isBase64("TWFu!"));
    }

    @Test(timeout = 4000)
    public void testStaticHelperVariants() {
        byte[] src = "Testing static helpers".getBytes();

        String strEnc = Base64.encodeBase64String(src);
        assertNotNull(strEnc);
        assertArrayEquals(src, Base64.decodeBase64(strEnc));

        String urlStrEnc = Base64.encodeBase64URLSafeString(src);
        assertNotNull(urlStrEnc);
        assertArrayEquals(src, Base64.decodeBase64(urlStrEnc));

        byte[] chunked = Base64.encodeBase64Chunked(src);
        assertNotNull(chunked);
        assertArrayEquals(src, Base64.decodeBase64(chunked));

        byte[] encChunkedDirect = Base64.encodeBase64(src, true);
        assertArrayEquals(chunked, encChunkedDirect);
    }

    // =========================================================================
    // Partition F: BigInteger Crypto Encoding & Decoding
    // =========================================================================

    @Test(timeout = 4000)
    public void testBigIntegerCodecRoundTrip() {
        BigInteger[] testValues = new BigInteger[]{
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.valueOf(127),
                BigInteger.valueOf(128),
                BigInteger.valueOf(255),
                BigInteger.valueOf(256),
                BigInteger.valueOf(65535),
                new BigInteger("123456789012345678901234567890"),
                new BigInteger("987654321987654321987654321987654321")
        };

        for (BigInteger val : testValues) {
            byte[] encoded = Base64.encodeInteger(val);
            assertNotNull("Encoded BigInteger should not be null", encoded);
            BigInteger decoded = Base64.decodeInteger(encoded);
            assertEquals("Roundtrip failed for BigInteger: " + val, val, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testToIntegerBytesBitLengthAlignment() {
        // bitLength % 8 == 0 case: 256 has bitLength 9; 128 has bitLength 8 (8 % 8 == 0)
        BigInteger biAligned = BigInteger.valueOf(128); // toByteArray() is [0x00, 0x80]
        byte[] bytesAligned = Base64.toIntegerBytes(biAligned);
        assertEquals(1, bytesAligned.length);
        assertEquals((byte) 0x80, bytesAligned[0]);

        // bitLength % 8 != 0 case
        BigInteger biUnaligned = BigInteger.valueOf(127); // bitLength 7
        byte[] bytesUnaligned = Base64.toIntegerBytes(biUnaligned);
        assertEquals(1, bytesUnaligned.length);
        assertEquals((byte) 127, bytesUnaligned[0]);

        // Zero BigInteger
        byte[] zeroBytes = Base64.toIntegerBytes(BigInteger.ZERO);
        assertEquals(0, zeroBytes.length);
    }
}