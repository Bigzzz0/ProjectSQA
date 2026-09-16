/* [Branch & Defect Analysis Matrix]
 *
 * Target: org.apache.commons.codec.binary.Base32
 * Defect Reference: CODEC-200 (Defects4J failure: testCodec200 -> IllegalArgumentException)
 *
 * Branches & Decision Points Targeted:
 * 1. Constructor Validation:
 *    - useHex flag (true -> HEX_ENCODE/DECODE_TABLE, false -> ENCODE/DECODE_TABLE)
 *    - lineLength > 0 vs <= 0 (chunking vs no chunking)
 *    - lineSeparator validation (null when lineLength > 0 -> IAE)
 *    - lineSeparator containing Base32 alphabet or pad -> IAE
 *    - pad byte validation (isInAlphabet(pad) or isWhiteSpace(pad) -> IAE)
 *    - Specific Bug CODEC-200: HEX_DECODE_TABLE erroneously maps 'W' to 32 instead of -1,
 *      causing isInAlphabet('W') to return true and throwing IAE when 'W' is used as pad.
 * 2. decode(byte[], int, int, Context):
 *    - context.eof == true guard
 *    - inAvail < 0 (EOF signal)
 *    - Loop over inAvail: byte == pad -> context.eof = true, break
 *    - decodeTable bounds check: b >= 0 && b < decodeTable.length
 *    - decodeTable result >= 0 vs -1 (ignoring whitespace / invalid chars)
 *    - context.modulus tracking (modulo 8)
 *    - context.modulus == 0 -> write 5 decoded bytes
 *    - EOF handling: context.modulus < 2 (drop remaining) vs context.modulus 2..7
 *      - case 2: 10 bits -> 1 byte
 *      - case 3: 15 bits -> 1 byte
 *      - case 4: 20 bits -> 2 bytes
 *      - case 5: 25 bits -> 3 bytes
 *      - case 6: 30 bits -> 3 bytes
 *      - case 7: 35 bits -> 4 bytes
 * 3. encode(byte[], int, int, Context):
 *    - context.eof == true guard
 *    - inAvail < 0 (EOF flush):
 *      - modulus == 0 and lineLength == 0 -> immediate return
 *      - modulus == 1..4 -> emit pad characters up to 8 bytes per block
 *      - lineLength > 0 && context.currentLinePos > 0 -> emit lineSeparator
 *    - inAvail >= 0 (Byte processing loop):
 *      - Handling signed bytes (b < 0 -> b += 256)
 *      - 5-byte accumulation into 40-bit lbitWorkArea
 *      - modulus == 0 -> emit 8 Base32 characters
 *      - lineLength chunking: currentLinePos >= lineLength -> append lineSeparator
 * 4. isInAlphabet(byte):
 *    - octet < 0, octet >= decodeTable.length, decodeTable[octet] == -1 vs != -1
 */

package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base32GeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-200)
    // =========================================================================

    /**
     * CODEC-200: In Base32 Hex mode, 'W' (0x57) is not in the Base32 Hex alphabet
     * (valid hex symbols are 0-9 and A-V). The decode table erroneously contained 32
     * for 'W', causing isInAlphabet((byte) 'W') to return true and preventing 'W'
     * from being used as a valid custom padding byte.
     */
    @Test(timeout = 4000)
    public void testCodec200HexCustomPadW() {
        final Base32 codec = new Base32(true, (byte) 'W');
        assertNotNull("Base32 instance with pad 'W' in hex mode should be constructed", codec);
        final byte[] input = new byte[] { (byte) 0xAB, (byte) 0xCD };
        final byte[] encoded = codec.encode(input);
        assertNotNull(encoded);
        final byte[] decoded = codec.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test(timeout = 4000)
    public void testCodec200IsInAlphabetWOnHex() {
        final Base32 hexCodec = new Base32(true);
        // 'W' is outside the Base32 Hex alphabet (0-9, A-V)
        assertFalse("'W' must not be in the Base32 Hex alphabet", hexCodec.isInAlphabet((byte) 'W'));
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (Encode / Decode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardBase32EncodingModulusVariations() {
        final Base32 codec = new Base32();

        // 1 byte -> modulus 1 -> 2 chars + 6 pads
        assertEquals("MY======", codec.encodeAsString(new byte[] { 'f' }));

        // 2 bytes -> modulus 2 -> 4 chars + 4 pads
        assertEquals("MZXQ====", codec.encodeAsString(new byte[] { 'f', 'o' }));

        // 3 bytes -> modulus 3 -> 5 chars + 3 pads
        assertEquals("MZXW6===", codec.encodeAsString(new byte[] { 'f', 'o', 'o' }));

        // 4 bytes -> modulus 4 -> 7 chars + 1 pad
        assertEquals("MZXW6YQ=", codec.encodeAsString(new byte[] { 'f', 'o', 'o', 'b' }));

        // 5 bytes -> modulus 0 -> 8 chars, 0 pads
        assertEquals("MZXW6YTB", codec.encodeAsString(new byte[] { 'f', 'o', 'o', 'b', 'a' }));

        // 6 bytes -> modulus 1 on second block
        assertEquals("MZXW6YTBOI======", codec.encodeAsString(new byte[] { 'f', 'o', 'o', 'b', 'a', 'r' }));
    }

    @Test(timeout = 4000)
    public void testStandardBase32DecodingModulusVariations() {
        final Base32 codec = new Base32();

        // Decoding with padding
        assertArrayEquals(new byte[] { 'f' }, codec.decode("MY======"));
        assertArrayEquals(new byte[] { 'f', 'o' }, codec.decode("MZXQ===="));
        assertArrayEquals(new byte[] { 'f', 'o', 'o' }, codec.decode("MZXW6==="));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b' }, codec.decode("MZXW6YQ="));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b', 'a' }, codec.decode("MZXW6YTB"));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b', 'a', 'r' }, codec.decode("MZXW6YTBOI======"));

        // Decoding without padding (unpadded Base32)
        assertArrayEquals(new byte[] { 'f' }, codec.decode("MY"));
        assertArrayEquals(new byte[] { 'f', 'o' }, codec.decode("MZXQ"));
        assertArrayEquals(new byte[] { 'f', 'o', 'o' }, codec.decode("MZXW6"));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b' }, codec.decode("MZXW6YQ"));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b', 'a' }, codec.decode("MZXW6YTB"));
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b', 'a', 'r' }, codec.decode("MZXW6YTBOI"));
    }

    @Test(timeout = 4000)
    public void testBase32HexEncodingAndDecoding() {
        final Base32 hexCodec = new Base32(true);

        final byte[] data = new byte[] { 'H', 'e', 'l', 'l', 'o', '!' };
        final String encoded = hexCodec.encodeAsString(data);
        assertNotNull(encoded);

        final byte[] decoded = hexCodec.decode(encoded);
        assertArrayEquals(data, decoded);

        // Verify hex characters are in 0-9, A-V
        for (final byte b : encoded.getBytes()) {
            if (b != '=') {
                assertTrue("Hex encoded chars must be 0-9 or A-V", (b >= '0' && b <= '9') || (b >= 'A' && b <= 'V'));
            }
        }
    }

    @Test(timeout = 4000)
    public void testDecodeWithWhitespaceAndIgnoredChars() {
        final Base32 codec = new Base32();
        // RFC 4648 test vector for "foobar" with whitespace & non-alphabet characters interspersed
        final String messyInput = " M Z\tX\r\n W 6 Y T B\n O I = = = = = = ";
        final byte[] decoded = codec.decode(messyInput);
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b', 'a', 'r' }, decoded);
    }

    @Test(timeout = 4000)
    public void testDecodeAllModulusLengthsExplicitly() {
        final Base32 codec = new Base32();

        // Modulus 2 (10 bits -> 1 byte)
        assertArrayEquals(new byte[] { 'f' }, codec.decode("MY"));
        // Modulus 3 (15 bits -> 1 byte decoded)
        assertArrayEquals(new byte[] { 'f' }, codec.decode("MYA"));
        // Modulus 4 (20 bits -> 2 bytes decoded)
        assertArrayEquals(new byte[] { 'f', 'o' }, codec.decode("MZXQ"));
        // Modulus 5 (25 bits -> 3 bytes decoded)
        assertArrayEquals(new byte[] { 'f', 'o', 'o' }, codec.decode("MZXW6"));
        // Modulus 6 (30 bits -> 3 bytes decoded)
        assertArrayEquals(new byte[] { 'f', 'o', 'o' }, codec.decode("MZXW6A"));
        // Modulus 7 (35 bits -> 4 bytes decoded)
        assertArrayEquals(new byte[] { 'f', 'o', 'o', 'b' }, codec.decode("MZXW6YQ"));
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingDefaultSeparator() {
        // Line length 8 with default CRLF separator
        final Base32 codec = new Base32(8);
        final byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        final byte[] encoded = codec.encode(data);

        final String encodedStr = new String(encoded);
        assertTrue("Output should contain CRLF", encodedStr.contains("\r\n"));
        assertTrue("Output should end with CRLF", encodedStr.endsWith("\r\n"));

        final byte[] decoded = codec.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    @Test(timeout = 4000)
    public void testChunkedEncodingCustomSeparator() {
        final byte[] separator = new byte[] { '$', '%' };
        final Base32 codec = new Base32(8, separator);
        final byte[] data = new byte[] { 10, 20, 30, 40, 50, 60, 70, 80 };
        final byte[] encoded = codec.encode(data);

        final String encodedStr = new String(encoded);
        assertTrue("Output should contain custom separator", encodedStr.contains("$%"));

        final byte[] decoded = codec.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeDecodeEmptyByteArray() {
        final Base32 codec = new Base32();
        assertArrayEquals(new byte[0], codec.encode(new byte[0]));
        assertArrayEquals(new byte[0], codec.decode(new byte[0]));
        assertEquals("", codec.encodeAsString(new byte[0]));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeNull() {
        final Base32 codec = new Base32();
        assertNull(codec.encode((byte[]) null));
        assertNull(codec.decode((byte[]) null));
        assertNull(codec.encodeAsString(null));
    }

    @Test(timeout = 4000)
    public void testDecodeSingleCharacterBelowModulusTwo() {
        final Base32 codec = new Base32();
        // A single Base32 character only yields 5 bits, which is < 1 byte (context.modulus = 1 < 2)
        final byte[] decoded = codec.decode("M");
        assertArrayEquals("Single character cannot yield a full byte and should produce empty array",
                new byte[0], decoded);
    }

    @Test(timeout = 4000)
    public void testNegativeByteHandlingDuringEncode() {
        final Base32 codec = new Base32();
        final byte[] negativeBytes = new byte[] { -1, -128, 127, -50, -2 };
        final byte[] encoded = codec.encode(negativeBytes);
        final byte[] decoded = codec.decode(encoded);
        assertArrayEquals(negativeBytes, decoded);
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetBoundaries() {
        final Base32 standard = new Base32();
        // Negative byte
        assertFalse(standard.isInAlphabet((byte) -1));
        assertFalse(standard.isInAlphabet((byte) -128));

        // Valid Base32 standard alphabet: 'A'-'Z' and '2'-'7'
        assertTrue(standard.isInAlphabet((byte) 'A'));
        assertTrue(standard.isInAlphabet((byte) 'Z'));
        assertTrue(standard.isInAlphabet((byte) '2'));
        assertTrue(standard.isInAlphabet((byte) '7'));

        // Invalid characters
        assertFalse(standard.isInAlphabet((byte) '0'));
        assertFalse(standard.isInAlphabet((byte) '1'));
        assertFalse(standard.isInAlphabet((byte) '8'));
        assertFalse(standard.isInAlphabet((byte) '9'));
        assertFalse(standard.isInAlphabet((byte) 'a')); // case-sensitive check in table
        assertFalse(standard.isInAlphabet((byte) '=')); // pad is not alphabet
        assertFalse(standard.isInAlphabet((byte) 127));
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetHexBoundaries() {
        final Base32 hexCodec = new Base32(true);
        // Valid Base32 Hex alphabet: '0'-'9' and 'A'-'V'
        assertTrue(hexCodec.isInAlphabet((byte) '0'));
        assertTrue(hexCodec.isInAlphabet((byte) '9'));
        assertTrue(hexCodec.isInAlphabet((byte) 'A'));
        assertTrue(hexCodec.isInAlphabet((byte) 'V'));

        // Invalid
        assertFalse(hexCodec.isInAlphabet((byte) 'X'));
        assertFalse(hexCodec.isInAlphabet((byte) 'Y'));
        assertFalse(hexCodec.isInAlphabet((byte) 'Z'));
        assertFalse(hexCodec.isInAlphabet((byte) -5));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLineLengthPositiveSeparatorNull() {
        new Base32(76, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLineLengthPositiveSeparatorNullHex() {
        new Base32(76, null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLineSeparatorContainsAlphabet() {
        new Base32(64, new byte[] { 'A', '\n' });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLineSeparatorContainsPad() {
        new Base32(64, new byte[] { '=', '\n' });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadInAlphabetStandard() {
        new Base32((byte) 'A');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadInAlphabetHex() {
        new Base32(true, (byte) '0');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadIsWhitespaceSpace() {
        new Base32((byte) ' ');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadIsWhitespaceTab() {
        new Base32((byte) '\t');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadIsWhitespaceNewline() {
        new Base32((byte) '\n');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorPadIsWhitespaceCR() {
        new Base32((byte) '\r');
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Multiple Constructor Signatures
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllConstructorsCoverage() {
        final Base32 c1 = new Base32();
        assertNotNull(c1);

        final Base32 c2 = new Base32((byte) '_');
        assertNotNull(c2);

        final Base32 c3 = new Base32(true);
        assertNotNull(c3);

        final Base32 c4 = new Base32(true, (byte) ':');
        assertNotNull(c4);

        final Base32 c5 = new Base32(76);
        assertNotNull(c5);

        final Base32 c6 = new Base32(76, new byte[] { '\n' });
        assertNotNull(c6);

        final Base32 c7 = new Base32(76, new byte[] { '\n' }, true);
        assertNotNull(c7);

        final Base32 c8 = new Base32(76, new byte[] { '\n' }, true, (byte) '.');
        assertNotNull(c8);

        // Negative lineLength should disable chunking
        final Base32 c9 = new Base32(-1, new byte[] { '\n' }, false, (byte) '=');
        final byte[] enc = c9.encode(new byte[20]);
        final String str = new String(enc);
        assertFalse(str.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testCustomPaddingRoundTrip() {
        final Base32 codec = new Base32((byte) '.');
        final byte[] data = "Hello World".getBytes();
        final String encoded = codec.encodeAsString(data);
        assertTrue("Encoded string should contain custom pad '.'", encoded.contains("."));
        assertFalse("Encoded string must not contain default pad '='", encoded.contains("="));
        final byte[] decoded = codec.decode(encoded);
        assertArrayEquals(data, decoded);
    }
}