package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigInteger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects (Defects4J):
 * - Codec-9 / testByteToStringVariations / testRfc4648Section10Encode:
 *   encodeBase64String(byte[]) in defective versions delegates to encodeBase64(data, true) [chunked],
 *   erroneously appending chunk separators (CRLF "\r\n") to standard string encodings.
 *   Correct RFC 4648/2045 behavior expects unchunked output from encodeBase64String unless chunking is requested.
 *
 * Targeted Branches & Edge Conditions:
 * - Constructors:
 *   - Base64(): defaults (lineLength = 0, standard table)
 *   - Base64(boolean): urlSafe true/false
 *   - Base64(int): chunk length rounded down to multiple of 4
 *   - Base64(int, byte[]): custom separator validation
 *   - Base64(int, byte[], boolean): null lineSeparator handling, illegal separator with base64 chars
 * - Encoding:
 *   - null and empty byte[] inputs
 *   - modulus 0 (multiples of 3 bytes), modulus 1 (1 trailing byte -> 2 base64 chars + 2 pad or 0 pad for URL-safe),
 *     modulus 2 (2 trailing bytes -> 3 base64 chars + 1 pad or 0 pad for URL-safe)
 *   - chunking boundary: currentLinePos reaching lineLength, insertion of lineSeparator
 *   - negative bytes (signed byte promotion to unsigned 0..255)
 *   - maxResultSize boundary: exceeding maxResultSize throwing IllegalArgumentException
 *   - buffer expansion: resizeBuffer() when buffer runs out of room
 *   - setInitialBuffer optimization and direct output array re-use
 * - Decoding:
 *   - null and empty byte[] inputs
 *   - PAD character ('=') handling causing early EOF termination
 *   - non-base64 characters / whitespace skipping in decode loop
 *   - EOF with modulus 0, modulus 2 (1 byte output), modulus 3 (2 bytes output)
 *   - decode string vs byte[] vs invalid Object type (DecoderException)
 * - Static Helper Methods:
 *   - isBase64: PAD, valid ASCII indices in DECODE_TABLE, negative bytes, out-of-range (>127) bytes
 *   - isArrayByteBase64: all valid, mixed whitespace, invalid bytes, empty array
 *   - discardWhitespace: empty array, all whitespace, mixed whitespace
 *   - toIntegerBytes / encodeInteger / decodeInteger: BigInteger null check (NPE), byte alignment (mod 8 == 0 vs != 0)
 * - Object encode/decode interface contracts:
 *   - encode(Object) with byte[] vs other types (EncoderException)
 *   - decode(Object) with byte[], String, vs other types (DecoderException)
 */
public class Base64GeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets Codec-9 / testByteToStringVariations:
     * Base64.encodeBase64String should produce an unchunked Base64 string without trailing CRLF.
     */
    @Test(timeout = 4000)
    public void testDefectEncodeBase64StringNoTrailingChunkSeparator() {
        byte[] input = StringUtils.getBytesUtf8("Hello World");
        String result = Base64.encodeBase64String(input);
        assertEquals("SGVsbG8gV29ybGQ=", result);
    }

    /**
     * Targets Codec-9 / testRfc4648Section10Encode:
     * Standard RFC 4648 single-character encode via encodeBase64String should not append CRLF.
     */
    @Test(timeout = 4000)
    public void testDefectRfc4648Section10SingleCharEncode() {
        byte[] input = StringUtils.getBytesUtf8("f");
        String result = Base64.encodeBase64String(input);
        assertEquals("Zg==", result);
    }

    /**
     * Targets RFC 4648 test vector series for encodeBase64String.
     */
    @Test(timeout = 4000)
    public void testDefectRfc4648VectorsEncodeString() {
        assertEquals("", Base64.encodeBase64String(StringUtils.getBytesUtf8("")));
        assertEquals("Zg==", Base64.encodeBase64String(StringUtils.getBytesUtf8("f")));
        assertEquals("Zm8=", Base64.encodeBase64String(StringUtils.getBytesUtf8("fo")));
        assertEquals("Zm9v", Base64.encodeBase64String(StringUtils.getBytesUtf8("foo")));
        assertEquals("Zm9vYg==", Base64.encodeBase64String(StringUtils.getBytesUtf8("foob")));
        assertEquals("Zm9vYmE=", Base64.encodeBase64String(StringUtils.getBytesUtf8("fooba")));
        assertEquals("Zm9vYmFy", Base64.encodeBase64String(StringUtils.getBytesUtf8("foobar")));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Base64 b64 = new Base64();
        assertFalse("Default constructor must not be URL-safe", b64.isUrlSafe());
        assertFalse("Initial state hasData must be false", b64.hasData());
        assertEquals("Initial avail must be 0", 0, b64.avail());
    }

    @Test(timeout = 4000)
    public void testConstructorUrlSafeTrue() {
        Base64 b64 = new Base64(true);
        assertTrue("Expected URL-safe mode to be active", b64.isUrlSafe());
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTripStandard() {
        Base64 b64 = new Base64();
        byte[] raw = StringUtils.getBytesUtf8("Light & Truth");
        byte[] encoded = b64.encode(raw);
        assertNotNull(encoded);
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(raw, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTripUrlSafe() {
        Base64 encoder = new Base64(0, new byte[]{}, true);
        Base64 decoder = new Base64();
        // Byte values that yield '+' and '/' in standard base64 (e.g. 0xfb, 0xef, 0xbf)
        byte[] binaryData = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xfe};
        byte[] encoded = encoder.encode(binaryData);
        String encodedStr = StringUtils.newStringUtf8(encoded);
        assertFalse("URL safe encode should not contain '+'", encodedStr.contains("+"));
        assertFalse("URL safe encode should not contain '/'", encodedStr.contains("/"));
        assertFalse("URL safe encode should not contain '='", encodedStr.contains("="));

        byte[] decoded = decoder.decode(encoded);
        assertArrayEquals(binaryData, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeChunkedLineLengthRounding() {
        // Line length 7 rounds down to 4
        byte[] sep = new byte[]{'\n'};
        Base64 b64 = new Base64(7, sep);
        byte[] data = StringUtils.getBytesUtf8("123456"); // 6 bytes -> 8 base64 chars
        byte[] encoded = b64.encode(data);
        // Should have chunk after 4 chars: "1234" (first chunk) + '\n' + "5678" + '\n'
        String encodedStr = StringUtils.newStringUtf8(encoded);
        assertTrue(encodedStr.contains("\n"));
        assertEquals(10, encoded.length); // 8 base64 chars + 2 separators
    }

    @Test(timeout = 4000)
    public void testEncodeModulusCases() {
        Base64 b64 = new Base64();

        // 1 byte -> modulus 1 -> 2 chars + '=='
        byte[] oneByte = new byte[]{'A'};
        byte[] encOne = b64.encode(oneByte);
        assertEquals("QQ==", StringUtils.newStringUtf8(encOne));

        // 2 bytes -> modulus 2 -> 3 chars + '='
        byte[] twoBytes = new byte[]{'A', 'B'};
        byte[] encTwo = b64.encode(twoBytes);
        assertEquals("QUI=", StringUtils.newStringUtf8(encTwo));

        // 3 bytes -> modulus 0 -> 4 chars, no padding
        byte[] threeBytes = new byte[]{'A', 'B', 'C'};
        byte[] encThree = b64.encode(threeBytes);
        assertEquals("QUJD", StringUtils.newStringUtf8(encThree));
    }

    @Test(timeout = 4000)
    public void testEncodeUrlSafeSkipsPadding() {
        Base64 b64UrlSafe = new Base64(0, Base64.CHUNK_SEPARATOR, true);

        // Modulus 1: 1 byte input
        byte[] enc1 = b64UrlSafe.encode(new byte[]{'A'});
        assertEquals("QQ", StringUtils.newStringUtf8(enc1));

        // Modulus 2: 2 bytes input
        byte[] enc2 = b64UrlSafe.encode(new byte[]{'A', 'B'});
        assertEquals("QUI", StringUtils.newStringUtf8(enc2));
    }

    @Test(timeout = 4000)
    public void testDecodeModulusCasesWithoutPadding() {
        Base64 b64 = new Base64();

        // 2 base64 chars -> modulus 2 in decode -> 1 decoded byte
        byte[] dec1 = b64.decode(StringUtils.getBytesUtf8("QQ"));
        assertArrayEquals(new byte[]{'A'}, dec1);

        // 3 base64 chars -> modulus 3 in decode -> 2 decoded bytes
        byte[] dec2 = b64.decode(StringUtils.getBytesUtf8("QUI"));
        assertArrayEquals(new byte[]{'A', 'B'}, dec2);
    }

    @Test(timeout = 4000)
    public void testDecodeWithIgnoredCharactersAndWhitespace() {
        Base64 b64 = new Base64();
        // Embedded space, carriage return, newline, and non-base64 characters like '!' and '$'
        String dirtyEncoded = "  Q\r\nU ! $  J   D \n";
        byte[] decoded = b64.decode(StringUtils.getBytesUtf8(dirtyEncoded));
        assertArrayEquals(new byte[]{'A', 'B', 'C'}, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeEarlyPadCharacter() {
        Base64 b64 = new Base64();
        // PAD '=' encountered early triggers EOF and stops further decoding
        byte[] decoded = b64.decode(StringUtils.getBytesUtf8("QUJD=QUJD"));
        assertArrayEquals(new byte[]{'A', 'B', 'C'}, decoded);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeNullAndEmpty() {
        Base64 b64 = new Base64();
        assertNull(b64.encode((byte[]) null));
        assertArrayEquals(new byte[0], b64.encode(new byte[0]));

        assertNull(Base64.encodeBase64(null));
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testDecodeNullAndEmpty() {
        Base64 b64 = new Base64();
        assertNull(b64.decode((byte[]) null));
        assertArrayEquals(new byte[0], b64.decode(new byte[0]));

        assertNull(b64.decode((String) null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        assertNull(Base64.decodeBase64((String) null));
    }

    @Test(timeout = 4000)
    public void testEncodeLargeBufferResizing() {
        // DEFAULT_BUFFER_SIZE is 8192, encode data larger to trigger resizeBuffer multiple times
        byte[] largeData = new byte[12000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        Base64 b64 = new Base64(76);
        byte[] encoded = b64.encode(largeData);
        assertNotNull(encoded);
        assertTrue(encoded.length > largeData.length);

        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(largeData, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeLargeBufferResizing() {
        byte[] largeData = new byte[16000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) ((i * 31) & 0xFF);
        }
        byte[] encoded = Base64.encodeBase64(largeData);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(largeData, decoded);
    }

    @Test(timeout = 4000)
    public void testReadResultsPartialAndEof() {
        Base64 b64 = new Base64();
        // When buffer is null and not EOF, readResults returns 0
        assertEquals(0, b64.readResults(new byte[10], 0, 10));

        // Encode a small chunk to create internal buffer
        b64.encode(new byte[]{'A', 'B', 'C'}, 0, 3);
        assertTrue(b64.hasData());
        int avail = b64.avail();
        assertTrue(avail > 0);

        // Read fewer bytes than avail
        byte[] smallOut = new byte[2];
        int read = b64.readResults(smallOut, 0, 2);
        assertEquals(2, read);
        assertTrue(b64.hasData());

        // Read remaining
        byte[] remainingOut = new byte[avail];
        int read2 = b64.readResults(remainingOut, 0, avail);
        assertEquals(avail - 2, read2);
        assertFalse(b64.hasData());
    }

    @Test(timeout = 4000)
    public void testReadResultsDirectBufferReUse() {
        Base64 b64 = new Base64();
        byte[] direct = new byte[4];
        b64.setInitialBuffer(direct, 0, direct.length);
        b64.encode(new byte[]{'X', 'Y', 'Z'}, 0, 3);
        // Reading into the same array used as initial buffer branches into: buffer == b
        int extracted = b64.readResults(direct, 0, direct.length);
        assertEquals(4, extracted);
        assertFalse(b64.hasData());
    }

    @Test(timeout = 4000)
    public void testEncodeWhenAlreadyEof() {
        Base64 b64 = new Base64();
        b64.encode(new byte[]{'A'}, 0, 1);
        b64.encode(new byte[0], 0, -1); // Triggers EOF

        // Subsequent call when eof is true must return immediately without change
        int posBefore = b64.avail();
        b64.encode(new byte[]{'B'}, 0, 1);
        assertEquals(posBefore, b64.avail());
    }

    @Test(timeout = 4000)
    public void testDecodeWhenAlreadyEof() {
        Base64 b64 = new Base64();
        b64.decode(new byte[]{'Q', '='}, 0, 2); // Triggers EOF via '='
        int availBefore = b64.avail();
        b64.decode(new byte[]{'Q'}, 0, 1);
        assertEquals(availBefore, b64.avail());
    }

    @Test(timeout = 4000)
    public void testEncodeBase64MaxResultSizeExceeded() {
        byte[] input = new byte[100];
        try {
            Base64.encodeBase64(input, false, false, 50);
            fail("Expected IllegalArgumentException when output exceeds maxResultSize");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Input array too big"));
        }
    }

    @Test(timeout = 4000)
    public void testEncodeBase64WithinMaxResultSize() {
        byte[] input = new byte[]{'1', '2', '3'};
        byte[] encoded = Base64.encodeBase64(input, false, false, 100);
        assertNotNull(encoded);
        assertEquals(4, encoded.length);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorLineSeparatorContainsBase64Character() {
        byte[] badSeparator = new byte[]{'A', '\n'};
        try {
            new Base64(76, badSeparator);
            fail("Expected IllegalArgumentException when lineSeparator contains Base64 character");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("lineSeperator must not contain base64 characters"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorNullLineSeparatorDisablesChunking() {
        Base64 b64 = new Base64(76, null);
        // Null separator should disable chunking (lineLength becomes 0)
        byte[] data = new byte[200];
        byte[] encoded = b64.encode(data);
        String s = StringUtils.newStringUtf8(encoded);
        assertFalse(s.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testEncodeObjectValidAndInvalidTypes() throws Exception {
        Base64 b64 = new Base64();
        Object validResult = b64.encode((Object) StringUtils.getBytesUtf8("Test"));
        assertTrue(validResult instanceof byte[]);

        try {
            b64.encode("A String is not a byte[]");
            fail("Expected EncoderException for non-byte[] object");
        } catch (EncoderException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDecodeObjectValidAndInvalidTypes() throws Exception {
        Base64 b64 = new Base64();
        Object fromBytes = b64.decode((Object) StringUtils.getBytesUtf8("VGVzdA=="));
        assertTrue(fromBytes instanceof byte[]);
        assertEquals("Test", StringUtils.newStringUtf8((byte[]) fromBytes));

        Object fromString = b64.decode((Object) "VGVzdA==");
        assertTrue(fromString instanceof byte[]);
        assertEquals("Test", StringUtils.newStringUtf8((byte[]) fromString));

        try {
            b64.decode(12345);
            fail("Expected DecoderException for unsupported type");
        } catch (DecoderException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEncodeIntegerNullThrowsNullPointerException() {
        try {
            Base64.encodeInteger(null);
            fail("Expected NullPointerException for null BigInteger");
        } catch (NullPointerException expected) {
            assertEquals("encodeInteger called with null parameter", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Static Methods & Crypto BigInteger Support
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsBase64SingleByte() {
        assertTrue(Base64.isBase64((byte) '='));
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '-'));
        assertTrue(Base64.isBase64((byte) '_'));

        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) '\n'));
        assertFalse(Base64.isBase64((byte) '$'));
        assertFalse(Base64.isBase64((byte) -1));
        assertFalse(Base64.isBase64((byte) -50));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[0]));
        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("VGVzdA==")));
        assertTrue(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("VG Vz\r\ndA=="))); // with whitespace

        assertFalse(Base64.isArrayByteBase64(StringUtils.getBytesUtf8("VG!VzdA==")));
        assertFalse(Base64.isArrayByteBase64(new byte[]{(byte) 0x80})); // negative byte
    }

    @Test(timeout = 4000)
    public void testDiscardWhitespace() {
        byte[] input = StringUtils.getBytesUtf8(" A \t B \r \n C ");
        byte[] expected = StringUtils.getBytesUtf8("ABC");
        byte[] groomed = Base64.discardWhitespace(input);
        assertArrayEquals(expected, groomed);

        byte[] empty = Base64.discardWhitespace(new byte[0]);
        assertEquals(0, empty.length);
    }

    @Test(timeout = 4000)
    public void testEncodeToString() {
        Base64 b64 = new Base64();
        String encoded = b64.encodeToString(StringUtils.getBytesUtf8("Hello World"));
        assertEquals("SGVsbG8gV29ybGQ=", encoded);
    }

    @Test(timeout = 4000)
    public void testEncodeBase64URLSafeAndString() {
        byte[] input = new byte[]{(byte) 0xfb, (byte) 0xff, (byte) 0xfe};
        byte[] urlSafeBytes = Base64.encodeBase64URLSafe(input);
        String urlSafeString = Base64.encodeBase64URLSafeString(input);

        assertNotNull(urlSafeBytes);
        assertNotNull(urlSafeString);
        assertEquals(StringUtils.newStringUtf8(urlSafeBytes), urlSafeString);
        assertFalse(urlSafeString.contains("+"));
        assertFalse(urlSafeString.contains("/"));
        assertFalse(urlSafeString.contains("="));
    }

    @Test(timeout = 4000)
    public void testEncodeBase64Chunked() {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ('a' + (i % 26));
        }
        byte[] chunked = Base64.encodeBase64Chunked(data);
        String chunkedStr = StringUtils.newStringUtf8(chunked);
        assertTrue(chunkedStr.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testBigIntegerEncodingByteAligned() {
        // Bit length 16: exactly byte aligned (16 % 8 == 0)
        BigInteger bigIntAligned = new BigInteger("65535"); // 0x00FFFF, 2 bytes + sign byte in toByteArray
        byte[] encoded = Base64.encodeInteger(bigIntAligned);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigIntAligned, decoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerEncodingNonByteAligned() {
        // Bit length 15: non byte-aligned (15 % 8 != 0)
        BigInteger bigIntNonAligned = new BigInteger("32767"); // 0x7FFF
        byte[] encoded = Base64.encodeInteger(bigIntNonAligned);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bigIntNonAligned, decoded);
    }

    @Test(timeout = 4000)
    public void testBigIntegerZero() {
        BigInteger zero = BigInteger.ZERO;
        byte[] encoded = Base64.encodeInteger(zero);
        assertNotNull(encoded);
        BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(zero, decoded);
    }

    @Test(timeout = 4000)
    public void testNegativeByteEncoding() {
        // Test byte array with negative signed values (-128 to -1)
        byte[] negativeBytes = new byte[]{(byte) 0x80, (byte) 0xFF, (byte) 0xAA, (byte) 0x55};
        byte[] encoded = Base64.encodeBase64(negativeBytes);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(negativeBytes, decoded);
    }
}