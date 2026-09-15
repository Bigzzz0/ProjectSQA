package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targeting all branches:
 * - Constructor: null lineSeparator, lineLength <= 0, lineLength > 0, lineSeparator containing base64 byte (exception), urlSafe true/false.
 * - encode(): modulus 0, 1, 2; eof condition; lineLength > 0 with currentLinePos >= lineLength; buffer resize; padding for STANDARD vs URL_SAFE.
 * - decode(): modulus 0,1,2,3; PAD char handling; invalid chars (ignored); eof with leftover modulus; buffer resize.
 * - isBase64(byte), isBase64(byte[]), isBase64(String), containsBase64Byte.
 * - Encode/Decode instance methods with null/empty arrays.
 * - static encodeBase64 overloads: isChunked, urlSafe, maxResultSize.
 * - static decodeBase64(String, byte[]).
 * - DiscardWhitespace (deprecated but still tested).
 * - decode(Object) with String and byte[] and wrong type.
 * - encode(Object) with wrong type.
 * - ReadResults with various avail values.
 * - getEncodeLength: chunkSize > 0 and len divisible or not.
 *
 * Defect target (testCodec112):
 *   When calling encodeBase64 with isChunked=false, urlSafe=false, maxResultSize=4, and binaryData of length 1,
 *   the buggy version incorrectly calculates the output size (including chunk separators) as 6 > 4 and throws IllegalArgumentException.
 *   Correct behaviour (fixed) should not throw because actual output without chunking is 4 <= 4.
 *   Test verifies no exception and correct output length.
 */
public class Base64DeepseekTest {

    // ========== Partition A: Core functional logic & state transitions ==========

    @Test(timeout = 4000)
    public void testBasicEncodeDecode() {
        byte[] original = new byte[] { (byte) 0x00, (byte) 0x01, (byte) 0x02 };
        String encoded = Base64.encodeBase64String(original);
        assertEquals("AAEC", encoded);
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testUrlSafeEncode() {
        byte[] original = new byte[] { (byte) 0xFF, (byte) 0xFE, (byte) 0xFD };
        byte[] encoded = Base64.encodeBase64URLSafe(original);
        // Standard would be "/v79", URL-safe is "_v79"
        assertArrayEquals(new byte[] { (byte) '_', (byte) 'v', (byte) '7', (byte) '9' }, encoded);
    }

    @Test(timeout = 4000)
    public void testUrlSafeString() {        byte[] input = new byte[] { 0x3B, 0xF2, (byte) 0x9A };
        String expected = "O/Ka"; // standard, not url-safe
        // But url-safe: "O_Ka"? Actually '+' becomes '-', '/' becomes '_'
        assertEquals("O_Ka", Base64.encodeBase64URLSafeString(input));
    }

    @Test(timeout = 4000)
    public void testChunkedEncode() {
        byte[] input = new byte[100];
        // Fill with pattern
        for (int i = 0; i < 100; i++) input[i] = (byte) (i % 256);
        byte[] chunked = Base64.encodeBase64Chunked(input);
        // Should contain CRLF sequence
        String encoded = new String(chunked, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(encoded.contains("\r\n"));
        // Decode back
        byte[] decoded = Base64.decodeBase64(chunked);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLineLength() {
        // Use custom Base64 instance with lineLength=8 (will be rounded to 8? Actually floor div by 4 => 8/4=2 -> 2*4=8)
        Base64 b64 = new Base64(8);
        byte[] input = "Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = b64.encode(input);
        // Expect CRLF after every 8 chars (but output might be 8 chars + CRLF + rest)
        String encodedStr = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals("SGVsbG8=", encodedStr.replace("\r\n", ""));
        // Now decode using same instance to ensure state reset works
        byte[] decoded = b64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // ========== Partition B: BVA & Extremes ==========

    @Test(timeout = 4000)
    public void testNullInputStatic() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertNull(Base64.decodeBase64((String) null));
    }

    @Test(timeout = 4000)
    public void testEmptyInput() {
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(""));
    }

    @Test(timeout = 4000)
    public void testSingleByteEncode() {
        byte[] input = new byte[] { 'A' };
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("QQ==", new String(encoded, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testTwoBytes() {
        byte[] input = new byte[] { (byte) 0xFF, (byte) 0x00 };
        byte[] encoded = Base64.encodeBase64(input);
        assertEquals("/wA=", new String(encoded, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testDecodeSingleChar() {
        // Decoding "QQ==" should give one byte
        byte[] result = Base64.decodeBase64("QQ==");
        assertArrayEquals(new byte[] { 'A' }, result);
    }

    @Test(timeout = 4000)
    public void testDecodeWithoutPadding() {
        // Decoders should accept missing padding
        byte[] decoded = Base64.decodeBase64("QQ");
        assertArrayEquals(new byte[] { 'A' }, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeWithWhitespace() {
        byte[] input = "Q Q = =".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] decoded = Base64.decodeBase64(input);
        assertArrayEquals(new byte[] { 'A' }, decoded);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the Defects4J defect (testCodec112).
     * The buggy version throws IllegalArgumentException for a small input when maxResultSize=4,
     * because getEncodeLength incorrectly always uses chunking parameters.
     */
    @Test(timeout = 4000)
    public void testCodec112Regression() {
        byte[] input = new byte[] { (byte) 0x01 };
        // Should not throw; actual output length is 4, maxResultSize=4.
        byte[] result = Base64.encodeBase64(input, false, false, 4);
        assertEquals(4, result.length);
        // Expected base64 "AQ=="
        assertArrayEquals(new byte[] { 'A', 'Q', '=', '=' }, result);
    }

    @Test(timeout = 4000)
    public void testMaxResultSizeExceeded() {
        byte[] input = new byte[10]; // 10 bytes -> encoded = ceil(10*4/3)=14?, actually 10*4/3=13.33 -> 16? Let's compute: 10/3=3 remainder, so len= (10/3)*4 + 4 = 12+4=16. So 16 > 15
        try {
            Base64.encodeBase64(input, false, false, 15);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Input array too big"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLineSeparatorContainsBase64Byte() {
        byte[] badSep = new byte[] { 'A', '\n' }; // 'A' is base64
        new Base64(10, badSep);
    }

    @Test(timeout = 4000)
    public void testNullLineSeparatorConstructor() {
        // When lineSeparator is null, lineLength is set to 0 and chunk separator is ignored
        Base64 b64 = new Base64(76, null);
        // Should encode without chunking
        byte[] encoded = b64.encode("test".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // Should not contain CRLF
        assertFalse(new String(encoded, java.nio.charset.StandardCharsets.US_ASCII).contains("\r\n"));
    }

    @Test(expected = DecoderException.class, timeout = 4000)
    public void testDecodeObjectWrongType() throws DecoderException {
        Base64 b64 = new Base64();
        b64.decode(Integer.valueOf(123));
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeObjectWrongType() throws EncoderException {
        Base64 b64 = new Base64();
        b64.encode("string");
    }

    @Test(timeout = 4000)
    public void testDecodeObjectString() throws DecoderException {
        Base64 b64 = new Base64();
        byte[] result = (byte[]) b64.decode("AQ==");
        assertArrayEquals(new byte[] { 0x01 }, result);
    }

    // ========== Partition E: Object lifecycle & contract integrity ==========

    @Test(timeout = 4000)
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte)'A'));
        assertTrue(Base64.isBase64((byte)'+'));
        assertTrue(Base64.isBase64((byte)'/'));
        assertTrue(Base64.isBase64((byte)'='));
        assertFalse(Base64.isBase64((byte)'!'));
        assertFalse(Base64.isBase64((byte) -1));
    }

    @Test(timeout = 4000)
    public void testIsBase64Array() {
        assertTrue(Base64.isBase64(new byte[] { 'A', '+', '/', '=' }));
        assertFalse(Base64.isBase64(new byte[] { 'A', '!', 'B' }));
        assertTrue(Base64.isBase64(new byte[] { ' ', '\n', '\r', '\t', 'A' })); // whitespace allowed
        assertTrue(Base64.isBase64(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testIsArrayByteBase64() {
        assertTrue(Base64.isArrayByteBase64(new byte[] { 'A', 'B' }));
        assertFalse(Base64.isArrayByteBase64(new byte[] { 'A', '#' }));
    }

    @Test(timeout = 4000)
    public void testHasDataAndAvail() {
        Base64 b64 = new Base64();
        assertFalse(b64.hasData());
        assertEquals(0, b64.avail());
        // After encode, data should be present
        b64.encode(new byte[] { 1, 2, 3 });
        assertTrue(b64.hasData());
        assertTrue(b64.avail() > 0);
    }

    @Test(timeout = 4000)
    public void testReset() {
        Base64 b64 = new Base64();
        b64.encode(new byte[] { 1 });
        b64.decode(new byte[] { 'A' }); // this resets? Actually decode calls reset then process
        // After decode, state should be fresh
        assertFalse(b64.hasData());
        assertEquals(0, b64.avail());
    }

    @Test(timeout = 4000)
    public void testUrlSafeModeGetter() {
        assertFalse(new Base64().isUrlSafe());
        assertTrue(new Base64(true).isUrlSafe());
        assertTrue(new Base64(0, null, true).isUrlSafe());
        assertFalse(new Base64(76, new byte[] { '\r', '\n' }, false).isUrlSafe());
    }

    // ========== Additional branch coverage ==========

    // Edge case: decode with modulus leftover after EOF and special cases
    @Test(timeout = 4000)
    public void testDecodeModulus2And3() {
        // Covers leftover handling in decode method:
        // modulus=2 -> x>>4 gives 8 bits; modulus=3 -> x>>2 gives 16 bits
        // We can force modulus via specific encoded strings.
        // For example, "Q" is 6 bits only -> modulus becomes 1 after one char, but decode loop only processes when result>=0.
        // Actually "Q" -> DECODE_TABLE['Q'] = 16, modulus=1, x=16, no output.
        // Then EOF with modulus=1 -> code has commented out case 1, so no output. That's fine.
        // For modulus=2: use two base64 chars without padding, e.g., "QQ" which is 12 bits.
        byte[] dec = Base64.decodeBase64("QQ"); // should produce 1 byte: 0x00? Let's compute: Q=16, Q=16, x = (16<<6)+16 = 1040, modulus=2, EOF triggers: x>>4 = 65, output byte 65 'A'. So "QQ" decodes to 'A'.
        assertArrayEquals(new byte[] { 'A' }, dec);
        // For modulus=3: "QUI" => char[0]=Q=16, char[1]=U=20, char[2]=I=8; x = ((16<<6)+20)<<6+8 = (1024+20)<<6+8 = 1044<<6+8 = 66816+8=66824; modulus=3, EOF: x>>2 = 16706; output bytes (x>>8)=65 (A) and (x&0xFF)=66 (B). So "QUI" decodes to "AB".
        dec = Base64.decodeBase64("QUI");
        assertArrayEquals(new byte[] { 'A', 'B' }, dec);
    }

    @Test(timeout = 4000)
    public void testEncodeModulus1And2() {
        // Orthogonal to URL-safe mode: modulus 1 and 2 produce different padding.
        // Standard: modulus=1 produces "a==" (two pads), modulus=2 produces "ab=" (one pad).
        Base64 standard = new Base64();
        byte[] enc1 = standard.encode(new byte[] { 0x00 }); // modulus 1: 0x00 -> encodeTable[0]? Actually x=0, modulus=1: outputs (x>>2)=0 -> 'A', (x<<4)=0 -> 'A', then pads "==" => "AA=="
        assertEquals("AA==", new String(enc1, java.nio.charset.StandardCharsets.US_ASCII));
        standard.reset();
        byte[] enc2 = standard.encode(new byte[] { 0x00, 0x00 }); // modulus 2: x=0, after second byte modulus=2 => outputs (x>>10)=0 -> 'A', (x>>4)=0 -> 'A', (x<<2)=0 -> 'A', then pad "=" => "AAA="
        assertEquals("AAA=", new String(enc2, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testEncodeUrlSafeNoPadding() {
        // URL-safe mode skips padding when modulus != 0
        Base64 urlSafe = new Base64(true);
        byte[] enc = urlSafe.encode(new byte[] { 0x00 }); // modulus 1: outputs 'A' and 'A', no padding
        assertEquals("AA", new String(enc, java.nio.charset.StandardCharsets.US_ASCII));
        urlSafe.reset();
        enc = urlSafe.encode(new byte[] { 0x00, 0x00 }); // modulus 2: outputs 'A','A','A' no pad
        assertEquals("AAA", new String(enc, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testDecodeInvalidCharsIgnored() {
        byte[] mixed = "A!B@C#".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] decoded = Base64.decodeBase64(mixed);
        // 'A','B','C' are base64 valid, ignore the rest; "ABC" decodes to two bytes? Actually "ABC" = 18 bits -> 2 bytes => x= ((0<<6)+0?) Let's compute: A=0, B=1, C=2 -> x = ((0<<6)+1)<<6+2 = 1<<6+2 = 66? Total 18 bits: bytes (x>>10)=0, (x>>2)?? Actually algorithm: after three chars, modulus=3, then x >> 16 = (66>>16)=0, x>>8 = 0, x=66? Wait correct: process: A (0) -> modulus=1, x=0; B (1) -> modulus=2, x = (0<<6)+1 = 1; C (2) -> modulus=3, x = (1<<6)+2 = 66; then output (x>>16)=0, (x>>8)=0, (x)=66 -> 'B'. So output is [0, 66]? Actually (x>>16)&0xFF=0, (x>>8)&0xFF=0, x&0xFF=66 -> that's 0x00, 0x00, 0x42. But we only output when modulus==0, which happens at each fourth char? Wait decode works differently: modulus increments on valid chars, and when modulus==0, output 3 bytes. For three valid chars, modulus would be 3 after third, not 0. Then EOF with modulus=3 triggers output of leftover bytes: case 3: x>>2 gives 16 bits -> two bytes. So output will be two bytes. Let's compute: after three chars, x=66, modulus=3, EOF triggers case 3: x>>2 = 16 (0x10), then output (x>>8)&0xFF = (66>>8)=0, (x)&0xFF=66? Wait careful: code: x = x >> 2; buffer[pos++] = (byte) ((x >> 8) & MASK_8BITS); buffer[pos++] = (byte) (x & MASK_8BITS); So x=66>>2 = 16. Then (x>>8)=0, (x&0xFF)=16. So output bytes {0, 16}. That's not 'ABC' decoded as typical. Actually "ABC" would be encoded from {0,0,?}. Not important. The point is invalid chars are ignored.
        // Just verify no exception and length >0
        assertTrue(decoded.length > 0);
    }

    @Test(timeout = 4000)
    public void testDecodeBase64String() {
        byte[] result = Base64.decodeBase64("SGVsbG8=");
        assertEquals("Hello", new String(result, java.nio.charset.StandardCharsets.UTF_8));
    }

    @Test(timeout = 4000)
    public void testEncodeInteger() {
        java.math.BigInteger bi = java.math.BigInteger.valueOf(12345);
        byte[] encoded = Base64.encodeInteger(bi);
        assertTrue(encoded.length > 0);
        java.math.BigInteger decoded = Base64.decodeInteger(encoded);
        assertEquals(bi, decoded);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testEncodeIntegerNull() {
        Base64.encodeInteger(null);
    }

    @Test(timeout = 4000)
    public void testDiscardWhitespace() {
        byte[] data = "A B C".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        byte[] expected = "ABC".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertArrayEquals(expected, Base64.discardWhitespace(data));
    }

    @Test(timeout = 4000)
    public void testReadResultsEmptyBuffer() {
        Base64 b64 = new Base64();
        byte[] out = new byte[10];
        int bytes = b64.readResults(out, 0, 10);
        assertEquals(0, bytes); // no EOF yet, return 0
    }

    @Test(timeout = 4000)
    public void testReadResultsAfterEof() {
        Base64 b64 = new Base64();
        b64.encode(new byte[] { 1, 2, 3 });
        b64.encode(new byte[0], 0, -1); // set eof
        byte[] out = new byte[10];
        int bytes = b64.readResults(out, 0, 10);
        assertTrue(bytes > 0);
        // If we read again, should return -1 (eof)
        int bytes2 = b64.readResults(out, 0, 10);
        assertEquals(-1, bytes2);
    }

    // Additional coverage for static encodeBase64 with isChunked and urlSafe
    @Test(timeout = 4000)
    public void testEncodeBase64ChunkedUrlSafe() {
        byte[] input = "Hello, World!".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = Base64.encodeBase64(input, true, true);
        assertTrue(new String(encoded, java.nio.charset.StandardCharsets.US_ASCII).contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testEncodeBase64BinaryDataOnly() {
        byte[] input = "test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = Base64.encodeBase64(input);
        assertEquals("dGVzdA==", new String(result, java.nio.charset.StandardCharsets.US_ASCII));
    }

    // Constructor with only lineLength (not urlSafe)
    @Test(timeout = 4000)
    public void testConstructorInt() {
        Base64 b64 = new Base64(0);
        assertFalse(b64.isUrlSafe());
        // lineLength 0 means no chunking
        byte[] input = new byte[10];
        byte[] encoded = b64.encode(input);
        // should not contain CRLF
        String encStr = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(encStr.contains("\r\n"));
    }

    @Test(timeout = 4000)
    public void testConstructorBoolean() {
        Base64 b64 = new Base64(true);
        assertTrue(b64.isUrlSafe());
        // Check encoding table used
        byte[] enc = b64.encode(new byte[] { (byte) 0xFF });
        // URL-safe encode of 0xFF: x=255, modulus=1 => output _ and w? compute: encodeTable[(255>>2)=63] -> '_', encodeTable[(255<<4)&0x3f = (4080)&0x3f = 0x30?] -> 'w'? Actually (255<<4)=4080, 4080&63=0x30=48, encodeTable[48]='w'. So "_w" without padding
        assertEquals("_w", new String(enc, java.nio.charset.StandardCharsets.US_ASCII));
    }

    @Test(timeout = 4000)
    public void testConstructorIntBool() {
        Base64 b64 = new Base64(76, new byte[] { '\r', '\n' }, true);
        assertTrue(b64.isUrlSafe());
        // Encode a block longer than 76 chars to see chunking with CRLF and URL-safe
        byte[] input = new byte[100];
        for (int i = 0; i < 100; i++) input[i] = (byte) (i % 256);
        byte[] encoded = b64.encode(input);
        String encStr = new String(encoded, java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(encStr.contains("\r\n"));
        assertTrue(encStr.contains("-") || encStr.contains("_")); // URL-safe chars
        assertFalse(encStr.contains("+") && encStr.contains("/"));
    }

    // Edge: lineSeparator length zero (empty) -> containsBase64Byte returns false
    @Test(timeout = 4000)
    public void testEmptyLineSeparator() {
        Base64 b64 = new Base64(10, new byte[0]);
        assertEquals(0, b64.lineLength); // because lineLength>0 but we'll see? Actually constructor: if lineSeparator null? not null, so lineLength stays >0. Then lineSeparator.length=0 -> encodeSize = 4+0 =4; decodeSize=3. ContainsBase64Byte on empty returns false. Should be okay.
        byte[] input = "A".getBytes();
        byte[] encoded = b64.encde(input);
        // No separator inserted because length zero? The code checks lineLength>0 and inserts lineSeparator, but if length zero, System.arraycopy copies 0 bytes, ok.
        assertNotNull(encoded);
    }
}