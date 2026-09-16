/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.codec.binary.Base64
 *
 * Targeted Defects & Regressions:
 * 1. Codec-4 / Base64Codec13Test failure:
 *    - The default constructor `Base64()` erroneously delegates to `this(false)`,
 *      which sets lineLength = CHUNK_SIZE (76), instead of unchunked (lineLength = 0)
 *      as specified by RFC 2045 and the method's own contract Javadoc ("line length is 0 (no chunking)").
 *    - BinaryEncoder/Encoder interface calls on `new Base64()` produce unexpected chunk separators
 *      ('\r\n') on inputs exceeding 57 bytes (>= 76 Base64 chars).
 *
 * Branch & Coverage Matrix:
 * - Constructors:
 *   - Base64(): default constructor (triggers defect when encoding > 57 bytes).
 *   - Base64(boolean): urlSafe toggle.
 *   - Base64(int): custom lineLength, default separator.
 *   - Base64(int, byte[]): custom lineLength and separator.
 *   - Base64(int, byte[], boolean): null lineSeparator branch, negative lineLength branch,
 *     containsBase64Byte validation branch (IllegalArgumentException).
 * - Modulus Branches (encode):
 *   - modulus 0 (multiples of 3, no padding).
 *   - modulus 1 (1 byte remainder: standard '==' vs URL-safe skip padding).
 *   - modulus 2 (2 bytes remainder: standard '=' vs URL-safe skip padding).
 *   - inAvail < 0 (EOF trigger), eof already set guard.
 *   - buffer resizing trigger (buffer == null, buffer.length - pos < encodeSize).
 * - Modulus Branches (decode):
 *   - PAD byte encounters (eof = true, break loop).
 *   - modulus 0, 2, 3 handling at EOF flush.
 *   - non-base64 characters / whitespace discarding.
 *   - negative bytes (< 0) and out-of-table characters.
 * - Static API:
 *   - encodeBase64(byte[]), encodeBase64(byte[], boolean), encodeBase64(byte[], boolean, boolean)
 *   - encodeBase64(byte[], boolean, boolean, int maxResultSize) - throws IllegalArgumentException if too large
 *   - encodeBase64String, encodeBase64Chunked, encodeBase64URLSafe, encodeBase64URLSafeString
 *   - decodeBase64(byte[]), decodeBase64(String)
 *   - isBase64(byte), isArrayByteBase64(byte[]), discardWhitespace(byte[])
 *   - BigInteger methods: encodeInteger(BigInteger), decodeInteger(byte[]), toIntegerBytes(BigInteger)
 * - Object / Interface Overrides:
 *   - encode(Object) with byte[] vs invalid non-byte[] (EncoderException).
 *   - decode(Object) with byte[], String, and invalid Object (DecoderException).
 *   - hasData(), avail(), readResults() with buffer == b and buffer != b.
 *   - setInitialBuffer optimization.
 */

package org.apache.commons.codec.binary;

import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.codec.BinaryDecoder;
import org.apache.commons.codec.BinaryEncoder;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Test;
import static org.junit.Assert.*;

public class Base64GeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Codec-4 / Codec-13)
    // =========================================================================

    /**
     * Targets Codec-4: Default constructor Base64() must not chunk output.
     * RFC 2045 and Base64() javadoc state: "When encoding the line length is 0 (no chunking)".
     * Defective implementation sets lineLength=76, introducing CRLF after 76 characters.
     */
    @Test(timeout = 4000)
    public void testDefaultConstructorMustNotChunk() {
        Base64 codec = new Base64();
        // 60 raw bytes encode into 80 base64 characters without chunking.
        // If chunked at 76, it will be 80 + 2 (CRLF) = 82 bytes.
        byte[] raw = new byte[60];
        Arrays.fill(raw, (byte) 'A');
        byte[] encoded = codec.encode(raw);
        assertEquals("Default constructor should produce unchunked output of exactly 80 bytes", 80, encoded.length);
        assertFalse("Unchunked output must not contain carriage return", containsByte(encoded, (byte) '\r'));
        assertFalse("Unchunked output must not contain line feed", containsByte(encoded, (byte) '\n'));
    }

    /**
     * Targets BinaryEncoder interface compatibility on default Base64 instance.
     * Reproduces Base64Codec13Test::testBinaryEncoder failure on defective version.
     */
    @Test(timeout = 4000)
    public void testBinaryEncoderInterfaceDefaultInstance() throws EncoderException {
        BinaryEncoder encoder = new Base64();
        byte[] input = new byte[75]; // 75 bytes -> 100 Base64 chars; would chunk at 76 if defective
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i & 0xff);
        }
        byte[] encoded = encoder.encode(input);
        assertEquals("Encoding via BinaryEncoder should not chunk with default constructor", 100, encoded.length);
    }

    /**
     * Targets Encoder (Object) interface compatibility on default Base64 instance.
     * Reproduces Base64Codec13Test::testEncoder failure on defective version.
     */
    @Test(timeout = 4000)
    public void testObjectEncoderInterfaceDefaultInstance() throws EncoderException {
        Base64 encoder = new Base64();
        byte[] input = new byte[63]; // 63 bytes -> 84 Base64 chars
        Object result = encoder.encode((Object) input);
        assertTrue("Encoder output must be byte[]", result instanceof byte[]);
        assertEquals("Encoding via Object encoder should not chunk with default constructor", 84, ((byte[]) result).length);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardEncodeDecodeRoundTrip() {
        String original = "The quick brown fox jumps over the lazy dog.";
        byte[] bytes = StringUtils.getBytesUtf8(original);
        byte[] encoded = Base64.encodeBase64(bytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertEquals(original, StringUtils.newStringUtf8(decoded));
    }

    @Test(timeout = 4000)
    public void testModulusPaddingsStandardMode() {
        // Modulus 1 (1 byte in -> 2 chars + "==")
        byte[] oneByte = new byte[]{'M'};
        byte[] encOne = Base64.encodeBase64(oneByte);
        assertEquals("TQ==", StringUtils.newStringUtf8(encOne));
        assertArrayEquals(oneByte, Base64.decodeBase64(encOne));

        // Modulus 2 (2 bytes in -> 3 chars + "=")
        byte[] twoBytes = new byte[]{'M', 'a'};
        byte[] encTwo = Base64.encodeBase64(twoBytes);
        assertEquals("TWE=", StringUtils.newStringUtf8(encTwo));
        assertArrayEquals(twoBytes, Base64.decodeBase64(encTwo));

        // Modulus 0 (3 bytes in -> 4 chars, no padding)
        byte[] threeBytes = new byte[]{'M', 'a', 'n'};
        byte[] encThree = Base64.encodeBase64(threeBytes);
        assertEquals("TWFu", StringUtils.newStringUtf8(encThree));
        assertArrayEquals(threeBytes, Base64.decodeBase64(encThree));
    }

    @Test(timeout = 4000)
    public void testUrlSafeEncodingAndDecoding() {
        // Standard Base64 uses + and / which become - and _ in URL-safe
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xfe};
        
        byte[] standard = Base64.encodeBase64(binaryData, false, false);
        assertEquals("Standard base64 encoding mismatch", "+//+", StringUtils.newStringUtf8(standard));

        byte[] urlSafe = Base64.encodeBase64URLSafe(binaryData);
        assertEquals("URL safe encoding mismatch", "-__-", StringUtils.newStringUtf8(urlSafe));

        // URL safe decode seamlessly handles standard and url-safe inputs
        assertArrayEquals(binaryData, Base64.decodeBase64(standard));
        assertArrayEquals(binaryData, Base64.decodeBase64(urlSafe));
        assertArrayEquals(binaryData, Base64.decodeBase64("-_/+".getBytes()));
    }

    @Test(timeout = 4000)
    public void testUrlSafeModulusWithoutPadding() {
        Base64 urlCodec = new Base64(0, new byte[]{}, true);
        assertTrue(urlCodec.isUrlSafe());

        // Modulus 1: 1 byte input -> 2 base64 chars without "=="
        byte[] oneByte = new byte[]{(byte) 'a'};
        byte[] enc1 = urlCodec.encode(oneByte);
        assertEquals("YQ", StringUtils.newStringUtf8(enc1));
        assertArrayEquals(oneByte, urlCodec.decode(enc1));

        // Modulus 2: 2 bytes input -> 3 base64 chars without "="
        byte[] twoBytes = new byte[]{(byte) 'a', (byte) 'b'};
        byte[] enc2 = urlCodec.encode(twoBytes);
        assertEquals("YWI", StringUtils.newStringUtf8(enc2));
        assertArrayEquals(twoBytes, urlCodec.decode(enc2));
    }

    @Test(timeout = 4000)
    public void testChunkedEncoding() {
        byte[] input = new byte[76 * 3]; // Generates 76 * 4 encoded chars
        Arrays.fill(input, (byte) 'Z');
        byte[] chunked = Base64.encodeBase64Chunked(input);

        // Verify chunk separators exist
        assertTrue("Chunked output must contain CRLF", containsByte(chunked, (byte) '\r'));
        assertTrue("Chunked output must contain CRLF", containsByte(chunked, (byte) '\n'));

        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testCustomChunkSeparatorAndLength() {
        byte[] separator = new byte[]{';', '$'};
        Base64 customCodec = new Base64(8, separator, false);
        assertFalse(customCodec.isUrlSafe());

        byte[] input = "123456789012".getBytes(); // 12 bytes -> 16 Base64 chars -> 2 chunks of 8
        byte[] encoded = customCodec.encode(input);

        String encodedStr = StringUtils.newStringUtf8(encoded);
        assertTrue(encodedStr.contains(";$"));
        assertArrayEquals(input, customCodec.decode(encoded));
    }

    @Test(timeout = 4000)
    public void testEncodeToStringAndDecodeString() {
        Base64 b64 = new Base64();
        String text = "Apache Commons Codec Unit Test";
        String encodedStr = b64.encodeToString(StringUtils.getBytesUtf8(text));
        assertEquals(encodedStr, Base64.encodeBase64URLSafeString(StringUtils.getBytesUtf8(text))
                .replace('-', '+').replace('_', '/')); // For this specific text no padding difference

        byte[] decodedBytes = b64.decode(encodedStr);
        assertEquals(text, StringUtils.newStringUtf8(decodedBytes));
    }

    @Test(timeout = 4000)
    public void testStreamingBufferSizeGrowth() {
        // Test encoding and decoding that exceeds DEFAULT_BUFFER_SIZE (8192) to force resizeBuffer()
        byte[] largeData = new byte[10000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 251);
        }
        byte[] encoded = Base64.encodeBase64(largeData, false);
        assertTrue(encoded.length > 8192);

        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(largeData, decoded);
    }

    @Test(timeout = 4000)
    public void testDecoderIgnoresWhitespaceAndInvalidBytes() {
        // In Base64, whitespace and out-of-alphabet characters are skipped
        String withNoise = " T\tW\r\nF\n  u ";
        byte[] decoded = Base64.decodeBase64(withNoise);
        assertEquals("Man", StringUtils.newStringUtf8(decoded));

        // Test decode with negative and out-of-table byte values
        byte[] noisyBytes = new byte[]{'T', -10, 'W', 127, 'F', -1, 'u'};
        byte[] decodedNoise = Base64.decodeBase64(noisyBytes);
        assertEquals("Man", StringUtils.newStringUtf8(decodedNoise));
    }

    @Test(timeout = 4000)
    public void testDecoderModulusEOFCombinations() {
        // Decoding strings without optional padding:
        // Case modulus 2: 2 base64 chars -> 1 decoded byte
        byte[] decMod2 = Base64.decodeBase64("TQ");
        assertEquals("M", StringUtils.newStringUtf8(decMod2));

        // Case modulus 3: 3 base64 chars -> 2 decoded bytes
        byte[] decMod3 = Base64.decodeBase64("TWE");
        assertEquals("Ma", StringUtils.newStringUtf8(decMod3));

        // Case modulus 0: complete 4 base64 chars
        byte[] decMod0 = Base64.decodeBase64("TWFu");
        assertEquals("Man", StringUtils.newStringUtf8(decMod0));
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyInputs() {
        assertNull(Base64.encodeBase64(null));
        assertEquals(0, Base64.encodeBase64(new byte[0]).length);

        assertNull(Base64.decodeBase64((byte[]) null));
        assertEquals(0, Base64.decodeBase64(new byte[0]).length);

        assertNull(Base64.decodeBase64((String) null));

        Base64 b64 = new Base64();
        assertNull(b64.encode(null));
        assertEquals(0, b64.encode(new byte[0]).length);
        assertNull(b64.decode((byte[]) null));
        assertEquals(0, b64.decode(new byte[0]).length);
    }

    @Test(timeout = 4000)
    public void testIsBase64Boundaries() {
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'Z'));
        assertTrue(Base64.isBase64((byte) 'a'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '9'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));

        assertFalse(Base64.isBase64((byte) -1));
        assertFalse(Base64.isBase64((byte) 0));
        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) '\n'));
        assertFalse(Base64.isBase64((byte) 127));
        assertFalse(Base64.isBase64((byte) -128));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A', 'B', 'C', '='}));
        assertTrue(Base64.isArrayByteBase64(new byte[]{' ', '\t', '\r', '\n'})); // whitespace allowed
        assertTrue(Base64.isArrayByteBase64(new byte[]{'A', ' ', 'B', '\r', '\n', 'C'}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'A', 'B', (byte) 0x80}));
        assertFalse(Base64.isArrayByteBase64(new byte[]{'!', 'A', 'B'}));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullOrNegativeParameters() {
        // lineSeparator == null disables chunk-separating and falls back to CHUNK_SEPARATOR
        Base64 nullSepCodec = new Base64(76, null);
        assertArrayEquals("Chunk separator fallback check", "YWJj".getBytes(), nullSepCodec.encode("abc".getBytes()));

        // Negative lineLength implies no chunking
        Base64 negLengthCodec = new Base64(-10, new byte[]{'\n'});
        byte[] longInput = new byte[100];
        byte[] enc = negLengthCodec.encode(longInput);
        assertFalse(containsByte(enc, (byte) '\n'));
    }

    @Test(timeout = 4000)
    public void testDiscardWhitespaceDeprecatedMethod() {
        byte[] input = new byte[]{' ', 'A', '\t', 'B', '\r', 'C', '\n', 'D', ' '};
        byte[] groomed = Base64.discardWhitespace(input);
        assertArrayEquals(new byte[]{'A', 'B', 'C', 'D'}, groomed);

        byte[] empty = Base64.discardWhitespace(new byte[0]);
        assertEquals(0, empty.length);
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsWhenSeparatorContainsBase64Character() {
        new Base64(76, new byte[]{'A'});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsWhenSeparatorContainsPadCharacter() {
        new Base64(76, new byte[]{'='});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsWhenSeparatorContainsUrlSafeChar() {
        new Base64(76, new byte[]{'-'});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEncodeBase64ExceedingMaxResultSizeThrows() {
        byte[] data = new byte[100];
        // Encoding 100 bytes requires at least 136 bytes; maxResultSize of 50 must throw
        Base64.encodeBase64(data, false, false, 50);
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeNonByteArrayThrowsEncoderException() throws EncoderException {
        new Base64().encode("Not a byte array");
    }

    @Test(expected = DecoderException.class, timeout = 4000)
    public void testDecodeInvalidObjectTypeThrowsDecoderException() throws DecoderException {
        new Base64().decode(Integer.valueOf(12345));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testEncodeIntegerNullThrowsNPE() {
        Base64.encodeInteger(null);
    }

    // =========================================================================
    // PARTITION E: CRYPTO & BIGINTEGER EXTENSIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testBigIntegerEncodeAndDecode() {
        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        byte[] encoded = Base64.encodeInteger(bigInt);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigInt, decoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerExactByteBoundary() {
        // Test BigInteger with exact byte-alignment to hit startSrc = 1 (skipping sign bit)
        BigInteger byteAligned = new BigInteger("255"); // 0x00FF (2 bytes in toByteArray())
        byte[] integerBytes = Base64.toIntegerBytes(byteAligned);
        assertEquals(1, integerBytes.length);
        assertEquals((byte) 0xff, integerBytes[0]);

        BigInteger decoded = Base64.decodeInteger(Base64.encodeInteger(byteAligned));
        assertEquals(byteAligned, decoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerZero() {
        BigInteger zero = BigInteger.ZERO;
        byte[] encoded = Base64.encodeInteger(zero);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(zero, decoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerNonByteAligned() {
        // Bit length not multiple of 8
        BigInteger nonAligned = new BigInteger("127"); // 7 bits
        byte[] bytes = Base64.toIntegerBytes(nonAligned);
        assertEquals(1, bytes.length);
        assertEquals(127, bytes[0]);
    }

    // =========================================================================
    // PARTITION F: STREAMING, STATE & BUFFER INTERNAL MANAGEMENT
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectDecodeWithStringAndByteArray() throws DecoderException {
        Base64 codec = new Base64();
        String plain = "CommonsCodecBase64";
        byte[] plainBytes = StringUtils.getBytesUtf8(plain);
        String b64String = Base64.encodeBase64String(plainBytes);

        // decode(Object) with String
        Object res1 = codec.decode((Object) b64String);
        assertTrue(res1 instanceof byte[]);
        assertEquals(plain, StringUtils.newStringUtf8((byte[]) res1));

        // decode(Object) with byte[]
        Object res2 = codec.decode((Object) StringUtils.getBytesUtf8(b64String));
        assertTrue(res2 instanceof byte[]);
        assertEquals(plain, StringUtils.newStringUtf8((byte[]) res2));
    }

    @Test(timeout = 4000)
    public void testHasDataAndAvailAndReadResults() {
        Base64 codec = new Base64();
        assertFalse(codec.hasData());
        assertEquals(0, codec.avail());

        byte[] dest = new byte[10];
        assertEquals(0, codec.readResults(dest, 0, dest.length));

        // Feed some bytes into encoder to populate buffer
        codec.encode(new byte[]{1, 2, 3}, 0, 3);
        assertTrue(codec.hasData());
        assertTrue(codec.avail() > 0);

        // Read out partially
        int read1 = codec.readResults(dest, 0, 2);
        assertEquals(2, read1);
        assertTrue(codec.hasData());

        // Read out remainder
        int read2 = codec.readResults(dest, 2, codec.avail());
        assertTrue(read2 > 0);
        assertFalse(codec.hasData());
    }

    @Test(timeout = 4000)
    public void testEncodeAfterEofIsNoOp() {
        Base64 codec = new Base64();
        codec.encode(new byte[]{1, 2, 3}, 0, 3);
        codec.encode(new byte[0], 0, -1); // Signal EOF
        int availAfterEof = codec.avail();

        // Further encode calls after EOF must be ignored
        codec.encode(new byte[]{4, 5, 6}, 0, 3);
        assertEquals("Encode calls after EOF should be no-ops", availAfterEof, codec.avail());
    }

    @Test(timeout = 4000)
    public void testDecodeAfterEofIsNoOp() {
        Base64 codec = new Base64();
        codec.decode(new byte[]{'A', 'A', 'A', 'A'}, 0, 4);
        codec.decode(new byte[0], 0, -1); // Signal EOF
        int availAfterEof = codec.avail();

        // Further decode calls after EOF must be ignored
        codec.decode(new byte[]{'B', 'B', 'B', 'B'}, 0, 4);
        assertEquals("Decode calls after EOF should be no-ops", availAfterEof, codec.avail());
    }

    @Test(timeout = 4000)
    public void testSetInitialBufferDirectOptimization() {
        Base64 codec = new Base64();
        byte[] preAllocated = new byte[4];
        codec.setInitialBuffer(preAllocated, 0, preAllocated.length);
        assertTrue(codec.hasData());

        codec.encode(new byte[]{'a', 'b', 'c'}, 0, 3);
        // readResults where buffer == b branch
        int read = codec.readResults(preAllocated, 0, preAllocated.length);
        assertEquals(4, read);
        assertFalse(codec.hasData());
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    private static boolean containsByte(byte[] array, byte target) {
        if (array == null) {
            return false;
        }
        for (byte b : array) {
            if (b == target) {
                return true;
            }
        }
        return false;
    }
}