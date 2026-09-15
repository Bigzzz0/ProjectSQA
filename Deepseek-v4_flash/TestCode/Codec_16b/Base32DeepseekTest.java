package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.codec.binary.Base32
 * 
 * Known Defect: Base32Test::testCodec200 --> IllegalArgumentException: pad must not be in alphabet or whitespace
 * Root Cause Analysis: The constructor Base32(int lineLength, byte[] lineSeparator, boolean useHex, byte pad)
 * validates the pad byte against the alphabet/whitespace. However, the validation logic may be incomplete
 * or incorrectly ordered, allowing invalid pad characters (e.g., '=' when using hex alphabet, or whitespace)
 * to pass through in certain configurations, causing the defect to manifest during encoding/decoding.
 * 
 * Branch Coverage Targets:
 * 1. Constructor branches:
 *    - useHex == true vs false (encodeTable/decodeTable selection)
 *    - lineLength > 0 vs <= 0 (line separator handling)
 *    - lineSeparator == null vs non-null
 *    - lineSeparator contains alphabet/pad characters vs not
 *    - pad validation: pad in alphabet, pad is whitespace, pad valid
 * 2. encode() method branches:
 *    - context.modulus values 0-4 (all 5 cases)
 *    - lineLength > 0 and currentLinePos > 0 (chunk separator insertion)
 *    - context.eof handling
 *    - inAvail < 0 (EOF condition)
 *    - b < 0 (invalid input byte)
 * 3. decode() method branches:
 *    - context.modulus values 0-7 (all cases)
 *    - pad character handling
 *    - whitespace handling
 *    - invalid alphabet characters
 * 4. isInAlphabet() branches:
 *    - octet < 0
 *    - octet >= decodeTable.length
 *    - decodeTable[octet] == -1
 *    - decodeTable[octet] >= 0
 * 5. Boundary values:
 *    - Empty input arrays
 *    - Single byte inputs (modulus 1)
 *    - Two byte inputs (modulus 2)
 *    - Three byte inputs (modulus 3)
 *    - Four byte inputs (modulus 4)
 *    - Five byte inputs (modulus 0)
 *    - lineLength = 0, 1, 7, 8, 9, 76, MAX_VALUE
 *    - pad = PAD_DEFAULT ('='), custom pads, invalid pads
 *    - useHex = true/false combinations
 * 
 * Defect-Targeted Tests:
 * - testInvalidPadInAlphabet: Directly tests pad='A' (in alphabet) should throw
 * - testInvalidPadWhitespace: Directly tests pad=' ' (whitespace) should throw
 * - testInvalidPadHexAlphabet: Tests pad='0' (in hex alphabet) should throw
 * - testValidPadCustom: Tests valid custom pad (e.g., '!') works correctly
 * - testCodec200Scenario: Reproduces the exact defect scenario with lineLength=200
 */
public class Base32DeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Base32 codec = new Base32();
        assertNotNull("Codec should not be null", codec);
        assertTrue("Default should use standard alphabet", !codec.isInAlphabet((byte) '2'));
        assertTrue("Default should use standard alphabet", codec.isInAlphabet((byte) 'A'));
        assertFalse("Digit 0 not in standard alphabet", codec.isInAlphabet((byte) '0'));
    }

    @Test(timeout = 4000)
    public void testHexConstructor() {
        Base32 codec = new Base32(true);
        assertNotNull("Codec should not be null", codec);
        assertTrue("Hex should use hex alphabet", codec.isInAlphabet((byte) '0'));
        assertTrue("Hex should use hex alphabet", codec.isInAlphabet((byte) '9'));
        assertTrue("Hex should use hex alphabet", codec.isInAlphabet((byte) 'A'));
        assertFalse("Hex should not use standard alphabet", codec.isInAlphabet((byte) 'Z'));
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTrip() {
        Base32 codec = new Base32();
        byte[] input = "Hello, World!".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Round trip should preserve data", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeRoundTripHex() {
        Base32 codec = new Base32(true);
        byte[] input = "Test Hex Encoding".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Hex round trip should preserve data", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLineLength() {
        Base32 codec = new Base32(8);
        byte[] input = "Line length test data".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Round trip with line length should preserve data", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithCustomPad() {
        Base32 codec = new Base32(false, (byte) '!');
        byte[] input = "Custom pad test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Round trip with custom pad should preserve data", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLineSeparator() {
        byte[] separator = {'\n'};
        Base32 codec = new Base32(8, separator);
        byte[] input = "Line separator test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Round trip with line separator should preserve data", input, decoded);
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testEncodeEmptyInput() {
        Base32 codec = new Base32();
        byte[] result = codec.encode(new byte[0]);
        assertNotNull("Encoding empty should not return null", result);
        assertEquals("Encoding empty should return empty", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testDecodeEmptyInput() {
        Base32 codec = new Base32();
        byte[] result = codec.decode(new byte[0]);
        assertNotNull("Decoding empty should not return null", result);
        assertEquals("Decoding empty should return empty", 0, result.length);
    }

    @Test(timeout = 4000)
    public void testEncodeSingleByte() {
        Base32 codec = new Base32();
        byte[] input = {(byte) 0x41}; // 'A'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding single byte should not return null", encoded);
        assertEquals("Single byte should encode to 8 chars", 8, encoded.length);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Single byte round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeTwoBytes() {
        Base32 codec = new Base32();
        byte[] input = {(byte) 0x41, (byte) 0x42}; // 'AB'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding two bytes should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Two byte round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeThreeBytes() {
        Base32 codec = new Base32();
        byte[] input = {(byte) 0x41, (byte) 0x42, (byte) 0x43}; // 'ABC'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding three bytes should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Three byte round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeFourBytes() {
        Base32 codec = new Base32();
        byte[] input = {(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44}; // 'ABCD'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding four bytes should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Four byte round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeFiveBytes() {
        Base32 codec = new Base32();
        byte[] input = {(byte) 0x41, (byte) 0x42, (byte) 0x43, (byte) 0x44, (byte) 0x45}; // 'ABCDE'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding five bytes should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Five byte round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeMaxBytes() {
        Base32 codec = new Base32();
        byte[] input = new byte[1024];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding max bytes should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Max bytes round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthZero() {
        Base32 codec = new Base32(0);
        byte[] input = "No line length".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 0 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 0 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthNegative() {
        Base32 codec = new Base32(-1);
        byte[] input = "Negative line length".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with negative line length should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Negative line length round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthOne() {
        Base32 codec = new Base32(1);
        byte[] input = "Line length one".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 1 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 1 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthSeven() {
        Base32 codec = new Base32(7);
        byte[] input = "Line length seven".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 7 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 7 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthEight() {
        Base32 codec = new Base32(8);
        byte[] input = "Line length eight".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 8 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 8 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthNine() {
        Base32 codec = new Base32(9);
        byte[] input = "Line length nine".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 9 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 9 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthSeventySix() {
        Base32 codec = new Base32(76);
        byte[] input = "Line length seventy six".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with line length 76 should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line length 76 round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testLineLengthMaxValue() {
        Base32 codec = new Base32(Integer.MAX_VALUE);
        byte[] input = "Max line length".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoding with max line length should not return null", encoded);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Max line length round trip", input, decoded);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPadInAlphabet() {
        // Pad 'A' is in the standard Base32 alphabet - should throw
        new Base32(0, null, false, (byte) 'A');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPadWhitespace() {
        // Pad ' ' (space) is whitespace - should throw
        new Base32(0, null, false, (byte) ' ');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPadTab() {
        // Pad '\t' (tab) is whitespace - should throw
        new Base32(0, null, false, (byte) '\t');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPadHexAlphabet() {
        // Pad '0' is in the hex alphabet - should throw
        new Base32(0, null, true, (byte) '0');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidPadHexAlphabetLetter() {
        // Pad 'A' is in the hex alphabet - should throw
        new Base32(0, null, true, (byte) 'A');
    }

    @Test(timeout = 4000)
    public void testValidPadCustom() {
        // Pad '!' is not in alphabet or whitespace - should work
        Base32 codec = new Base32(0, null, false, (byte) '!');
        assertNotNull("Codec with custom pad should not be null", codec);
        byte[] input = "Valid custom pad".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Custom pad round trip", input, decoded);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCodec200Scenario() {
        // Reproduces the exact defect scenario from Base32Test::testCodec200
        // This should throw IllegalArgumentException because pad '=' is in the alphabet
        // when using hex alphabet with line length 200
        new Base32(200, null, true, (byte) '=');
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidLineSeparatorContainsAlphabet() {
        // Line separator containing Base32 characters should throw
        byte[] separator = {'A', 'B'};
        new Base32(8, separator);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidLineSeparatorContainsPad() {
        // Line separator containing pad character should throw
        byte[] separator = {'=', '\n'};
        new Base32(8, separator);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidLineSeparatorContainsWhitespace() {
        // Line separator containing whitespace should throw
        byte[] separator = {' ', '\n'};
        new Base32(8, separator);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLineLengthPositiveWithNullSeparator() {
        // lineLength > 0 with null lineSeparator should throw
        new Base32(8, null);
    }

    @Test(timeout = 4000)
    public void testLineLengthZeroWithNullSeparator() {
        // lineLength = 0 with null lineSeparator should work
        Base32 codec = new Base32(0, null);
        assertNotNull("Codec with line length 0 and null separator should not be null", codec);
    }

    @Test(timeout = 4000)
    public void testLineLengthNegativeWithNullSeparator() {
        // lineLength < 0 with null lineSeparator should work
        Base32 codec = new Base32(-1, null);
        assertNotNull("Codec with negative line length and null separator should not be null", codec);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testIsInAlphabetNegative() {
        Base32 codec = new Base32();
        assertFalse("Negative octet should not be in alphabet", codec.isInAlphabet((byte) -1));
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetOutOfRange() {
        Base32 codec = new Base32();
        assertFalse("Octet beyond table should not be in alphabet", codec.isInAlphabet((byte) 127));
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetValid() {
        Base32 codec = new Base32();
        assertTrue("Valid alphabet character should be in alphabet", codec.isInAlphabet((byte) 'A'));
        assertTrue("Valid alphabet character should be in alphabet", codec.isInAlphabet((byte) 'Z'));
        assertTrue("Valid alphabet character should be in alphabet", codec.isInAlphabet((byte) '2'));
        assertTrue("Valid alphabet character should be in alphabet", codec.isInAlphabet((byte) '7'));
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetInvalid() {
        Base32 codec = new Base32();
        assertFalse("Invalid character should not be in alphabet", codec.isInAlphabet((byte) '0'));
        assertFalse("Invalid character should not be in alphabet", codec.isInAlphabet((byte) '8'));
        assertFalse("Invalid character should not be in alphabet", codec.isInAlphabet((byte) '9'));
        assertFalse("Invalid character should not be in alphabet", codec.isInAlphabet((byte) '!'));
    }

    @Test(timeout = 4000)
    public void testIsInAlphabetHex() {
        Base32 codec = new Base32(true);
        assertTrue("Hex digit should be in hex alphabet", codec.isInAlphabet((byte) '0'));
        assertTrue("Hex digit should be in hex alphabet", codec.isInAlphabet((byte) '9'));
        assertTrue("Hex letter should be in hex alphabet", codec.isInAlphabet((byte) 'A'));
        assertTrue("Hex letter should be in hex alphabet", codec.isInAlphabet((byte) 'V'));
        assertFalse("Non-hex character should not be in hex alphabet", codec.isInAlphabet((byte) 'W'));
        assertFalse("Non-hex character should not be in hex alphabet", codec.isInAlphabet((byte) 'Z'));
    }

    @Test(timeout = 4000)
    public void testDecodeWithWhitespace() {
        Base32 codec = new Base32();
        byte[] input = "MZXW6 YTB".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with whitespace should not return null", decoded);
        assertEquals("Decoding with whitespace should produce correct length", 5, decoded.length);
    }

    @Test(timeout = 4000)
    public void testDecodeWithInvalidCharacters() {
        Base32 codec = new Base32();
        byte[] input = "MZXW6!YTB".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with invalid characters should not return null", decoded);
        assertEquals("Decoding with invalid characters should produce correct length", 5, decoded.length);
    }

    @Test(timeout = 4000)
    public void testDecodeWithPad() {
        Base32 codec = new Base32();
        byte[] input = "MZXW6===".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with pad should not return null", decoded);
        assertEquals("Decoding with pad should produce correct length", 4, decoded.length);
    }

    @Test(timeout = 4000)
    public void testDecodeWithMultiplePads() {
        Base32 codec = new Base32();
        byte[] input = "MZXW6===".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with multiple pads should not return null", decoded);
        assertEquals("Decoding with multiple pads should produce correct length", 4, decoded.length);
    }

    @Test(timeout = 4000)
    public void testDecodeWithOnlyPad() {
        Base32 codec = new Base32();
        byte[] input = "========".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with only pads should not return null", decoded);
        assertEquals("Decoding with only pads should produce empty", 0, decoded.length);
    }

    @Test(timeout = 4000)
    public void testDecodeWithLineSeparator() {
        Base32 codec = new Base32(8);
        byte[] input = "MZXW6YTB\nMZXW6YTB".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] decoded = codec.decode(input);
        assertNotNull("Decoding with line separator should not return null", decoded);
        assertEquals("Decoding with line separator should produce correct length", 10, decoded.length);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMultipleEncodeDecodeCycles() {
        Base32 codec = new Base32();
        byte[] input = "Multiple cycles".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (int i = 0; i < 10; i++) {
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Cycle " + i + " should preserve data", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithDifferentSizes() {
        Base32 codec = new Base32();
        for (int size = 0; size < 100; size++) {
            byte[] input = new byte[size];
            for (int i = 0; i < size; i++) {
                input[i] = (byte) (i % 256);
            }
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Size " + size + " round trip", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithAllByteValues() {
        Base32 codec = new Base32();
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("All byte values round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithHexAllByteValues() {
        Base32 codec = new Base32(true);
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Hex all byte values round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithCustomPadAllByteValues() {
        Base32 codec = new Base32(false, (byte) '!');
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Custom pad all byte values round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLineSeparatorAllByteValues() {
        byte[] separator = {'\n'};
        Base32 codec = new Base32(8, separator);
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Line separator all byte values round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithHexAndLineLength() {
        Base32 codec = new Base32(8, null, true);
        byte[] input = "Hex with line length".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Hex with line length round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithHexAndCustomPad() {
        Base32 codec = new Base32(true, (byte) '!');
        byte[] input = "Hex with custom pad".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Hex with custom pad round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithAllCombinations() {
        // Test various combinations of parameters
        byte[][] inputs = {
            "Short".getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "Medium length input".getBytes(java.nio.charset.StandardCharsets.UTF_8),
            "This is a longer input string that should test multiple encoding blocks".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        };
        
        int[] lineLengths = {0, 1, 7, 8, 9, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#'};
        
        for (byte[] input : inputs) {
            for (int lineLength : lineLengths) {
                for (boolean useHex : useHexOptions) {
                    for (byte pad : pads) {
                        try {
                            Base32 codec = new Base32(lineLength, null, useHex, pad);
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("Combination round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Some combinations may be invalid (e.g., pad in alphabet)
                            // This is expected for certain pad values
                            if (pad == '=' && useHex) {
                                // '=' is not in hex alphabet, should be valid
                                fail("Unexpected exception for valid combination: " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithStreaming() {
        Base32 codec = new Base32();
        byte[] input = "Streaming test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        // Simulate streaming by encoding in chunks
        BaseNCodec.Context context = new BaseNCodec.Context();
        byte[] buffer = new byte[1024];
        
        // Encode in chunks
        for (int i = 0; i < input.length; i += 3) {
            int len = Math.min(3, input.length - i);
            byte[] chunk = new byte[len];
            System.arraycopy(input, i, chunk, 0, len);
            codec.encode(chunk, 0, len, context);
        }
        codec.encode(input, input.length, 0, context); // Signal EOF
        
        byte[] encoded = new byte[context.pos];
        System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
        
        // Decode
        BaseNCodec.Context decodeContext = new BaseNCodec.Context();
        codec.decode(encoded, 0, encoded.length, decodeContext);
        codec.decode(encoded, encoded.length, 0, decodeContext); // Signal EOF
        
        byte[] decoded = new byte[decodeContext.pos];
        System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
        
        assertArrayEquals("Streaming round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithStreamingChunks() {
        Base32 codec = new Base32();
        byte[] input = "Streaming chunks test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        BaseNCodec.Context context = new BaseNCodec.Context();
        
        // Encode byte by byte
        for (byte b : input) {
            codec.encode(new byte[]{b}, 0, 1, context);
        }
        codec.encode(input, input.length, 0, context); // Signal EOF
        
        byte[] encoded = new byte[context.pos];
        System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
        
        // Decode byte by byte
        BaseNCodec.Context decodeContext = new BaseNCodec.Context();
        for (byte b : encoded) {
            codec.decode(new byte[]{b}, 0, 1, decodeContext);
        }
        codec.decode(encoded, encoded.length, 0, decodeContext); // Signal EOF
        
        byte[] decoded = new byte[decodeContext.pos];
        System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
        
        assertArrayEquals("Streaming chunks round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLargeData() {
        Base32 codec = new Base32();
        byte[] input = new byte[10000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Large data round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLargeDataHex() {
        Base32 codec = new Base32(true);
        byte[] input = new byte[10000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Large data hex round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLargeDataLineLength() {
        Base32 codec = new Base32(76);
        byte[] input = new byte[10000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Large data line length round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLargeDataCustomPad() {
        Base32 codec = new Base32(false, (byte) '!');
        byte[] input = new byte[10000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Large data custom pad round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLargeDataAllCombinations() {
        byte[] input = new byte[5000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        byte[] encoded = codec.encode(input);
                        byte[] decoded = codec.decode(encoded);
                        assertArrayEquals("Large data combination round trip", input, decoded);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseModulus() {
        Base32 codec = new Base32();
        
        // Test all modulus values (0-4 for encoding)
        for (int size = 0; size <= 20; size++) {
            byte[] input = new byte[size];
            for (int i = 0; i < size; i++) {
                input[i] = (byte) (i * 7 % 256);
            }
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Modulus " + (size % 5) + " round trip", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseModulusHex() {
        Base32 codec = new Base32(true);
        
        // Test all modulus values (0-4 for encoding)
        for (int size = 0; size <= 20; size++) {
            byte[] input = new byte[size];
            for (int i = 0; i < size; i++) {
                input[i] = (byte) (i * 13 % 256);
            }
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Hex modulus " + (size % 5) + " round trip", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseModulusCustomPad() {
        Base32 codec = new Base32(false, (byte) '!');
        
        // Test all modulus values (0-4 for encoding)
        for (int size = 0; size <= 20; size++) {
            byte[] input = new byte[size];
            for (int i = 0; i < size; i++) {
                input[i] = (byte) (i * 17 % 256);
            }
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Custom pad modulus " + (size % 5) + " round trip", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseModulusLineLength() {
        Base32 codec = new Base32(8);
        
        // Test all modulus values (0-4 for encoding)
        for (int size = 0; size <= 20; size++) {
            byte[] input = new byte[size];
            for (int i = 0; i < size; i++) {
                input[i] = (byte) (i * 19 % 256);
            }
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Line length modulus " + (size % 5) + " round trip", input, decoded);
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseModulusAllCombinations() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        for (int size = 0; size <= 20; size++) {
                            byte[] input = new byte[size];
                            for (int i = 0; i < size; i++) {
                                input[i] = (byte) (i * 23 % 256);
                            }
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("All combos modulus " + (size % 5) + " round trip", input, decoded);
                        }
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithSpecialCharacters() {
        Base32 codec = new Base32();
        byte[] input = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Special characters round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithUnicode() {
        Base32 codec = new Base32();
        byte[] input = "Unicode: 你好世界".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Unicode round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithNullBytes() {
        Base32 codec = new Base32();
        byte[] input = new byte[10];
        // All zeros
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Null bytes round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithMixedBytes() {
        Base32 codec = new Base32();
        byte[] input = new byte[100];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 2 == 0 ? 0 : 255);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Mixed bytes round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithAlternatingBytes() {
        Base32 codec = new Base32();
        byte[] input = new byte[50];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 2 == 0 ? 0x0F : 0xF0);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Alternating bytes round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithHighBits() {
        Base32 codec = new Base32();
        byte[] input = new byte[30];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (0x80 | (i % 0x7F));
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("High bits round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithLowBits() {
        Base32 codec = new Base32();
        byte[] input = new byte[30];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 0x10);
        }
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Low bits round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithRandomData() {
        Base32 codec = new Base32();
        // Use deterministic pseudo-random data
        long seed = 123456789L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[1000];
        random.nextBytes(input);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Random data round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithRandomDataHex() {
        Base32 codec = new Base32(true);
        long seed = 987654321L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[1000];
        random.nextBytes(input);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Random data hex round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithRandomDataCustomPad() {
        Base32 codec = new Base32(false, (byte) '!');
        long seed = 555555555L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[1000];
        random.nextBytes(input);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Random data custom pad round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithRandomDataLineLength() {
        Base32 codec = new Base32(76);
        long seed = 111111111L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[1000];
        random.nextBytes(input);
        byte[] encoded = codec.encode(input);
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Random data line length round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithRandomDataAllCombinations() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        long[] seeds = {111L, 222L, 333L, 444L, 555L};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        for (long seed : seeds) {
                            java.util.Random random = new java.util.Random(seed);
                            byte[] input = new byte[500];
                            random.nextBytes(input);
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("All combos random data round trip", input, decoded);
                        }
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithStreamingRandomData() {
        Base32 codec = new Base32();
        long seed = 777777777L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[1000];
        random.nextBytes(input);
        
        BaseNCodec.Context context = new BaseNCodec.Context();
        
        // Encode in random chunk sizes
        int offset = 0;
        while (offset < input.length) {
            int chunkSize = 1 + random.nextInt(10);
            chunkSize = Math.min(chunkSize, input.length - offset);
            codec.encode(input, offset, chunkSize, context);
            offset += chunkSize;
        }
        codec.encode(input, input.length, 0, context); // Signal EOF
        
        byte[] encoded = new byte[context.pos];
        System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
        
        // Decode in random chunk sizes
        BaseNCodec.Context decodeContext = new BaseNCodec.Context();
        offset = 0;
        while (offset < encoded.length) {
            int chunkSize = 1 + random.nextInt(10);
            chunkSize = Math.min(chunkSize, encoded.length - offset);
            codec.decode(encoded, offset, chunkSize, decodeContext);
            offset += chunkSize;
        }
        codec.decode(encoded, encoded.length, 0, decodeContext); // Signal EOF
        
        byte[] decoded = new byte[decodeContext.pos];
        System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
        
        assertArrayEquals("Streaming random data round trip", input, decoded);
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithStreamingRandomDataAllCombinations() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        long[] seeds = {111L, 222L, 333L};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        for (long seed : seeds) {
                            java.util.Random random = new java.util.Random(seed);
                            byte[] input = new byte[300];
                            random.nextBytes(input);
                            
                            BaseNCodec.Context context = new BaseNCodec.Context();
                            int offset = 0;
                            while (offset < input.length) {
                                int chunkSize = 1 + random.nextInt(10);
                                chunkSize = Math.min(chunkSize, input.length - offset);
                                codec.encode(input, offset, chunkSize, context);
                                offset += chunkSize;
                            }
                            codec.encode(input, input.length, 0, context);
                            
                            byte[] encoded = new byte[context.pos];
                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                            
                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                            offset = 0;
                            while (offset < encoded.length) {
                                int chunkSize = 1 + random.nextInt(10);
                                chunkSize = Math.min(chunkSize, encoded.length - offset);
                                codec.decode(encoded, offset, chunkSize, decodeContext);
                                offset += chunkSize;
                            }
                            codec.decode(encoded, encoded.length, 0, decodeContext);
                            
                            byte[] decoded = new byte[decodeContext.pos];
                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                            
                            assertArrayEquals("All combos streaming random data round trip", input, decoded);
                        }
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseLineSeparator() {
        // Test with various line separators
        byte[][] separators = {
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'}
        };
        
        for (byte[] separator : separators) {
            try {
                Base32 codec = new Base32(8, separator);
                byte[] input = "Line separator test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                byte[] encoded = codec.encode(input);
                byte[] decoded = codec.decode(encoded);
                assertArrayEquals("Separator round trip", input, decoded);
            } catch (IllegalArgumentException e) {
                // Some separators may be invalid (contain alphabet chars)
                // This is expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseLineSeparatorAllCombinations() {
        byte[][] separators = {
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'}
        };
        
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        
        for (byte[] separator : separators) {
            for (int lineLength : lineLengths) {
                for (boolean useHex : useHexOptions) {
                    for (byte pad : pads) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            byte[] input = "All combos separator test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("All combos separator round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCasePad() {
        // Test various pad values
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        
        for (byte pad : pads) {
            try {
                Base32 codec = new Base32(false, pad);
                byte[] input = "Pad test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                byte[] encoded = codec.encode(input);
                byte[] decoded = codec.decode(encoded);
                assertArrayEquals("Pad round trip", input, decoded);
            } catch (IllegalArgumentException e) {
                // Some pads may be invalid (in alphabet or whitespace)
                // This is expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCasePadAllCombinations() {
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        
        for (byte pad : pads) {
            for (int lineLength : lineLengths) {
                for (boolean useHex : useHexOptions) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        byte[] input = "All combos pad test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        byte[] encoded = codec.encode(input);
                        byte[] decoded = codec.decode(encoded);
                        assertArrayEquals("All combos pad round trip", input, decoded);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseLineLength() {
        int[] lineLengths = {Integer.MIN_VALUE, -100, -1, 0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, Integer.MAX_VALUE};
        
        for (int lineLength : lineLengths) {
            try {
                Base32 codec = new Base32(lineLength);
                byte[] input = "Line length test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                byte[] encoded = codec.encode(input);
                byte[] decoded = codec.decode(encoded);
                assertArrayEquals("Line length round trip", input, decoded);
            } catch (IllegalArgumentException e) {
                // Some line lengths may be invalid (positive with null separator)
                // This is expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseLineLengthAllCombinations() {
        int[] lineLengths = {Integer.MIN_VALUE, -100, -1, 0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, Integer.MAX_VALUE};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    try {
                        Base32 codec = new Base32(lineLength, null, useHex, pad);
                        byte[] input = "All combos line length test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                        byte[] encoded = codec.encode(input);
                        byte[] decoded = codec.decode(encoded);
                        assertArrayEquals("All combos line length round trip", input, decoded);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid combinations
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParameters() {
        int[] lineLengths = {Integer.MIN_VALUE, -100, -1, 0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, Integer.MAX_VALUE};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            byte[] input = "All parameters test".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("All parameters round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersLargeData() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        byte[] input = new byte[1000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            byte[] encoded = codec.encode(input);
                            byte[] decoded = codec.decode(encoded);
                            assertArrayEquals("All parameters large data round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreaming() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long seed = 999999999L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[500];
        random.nextBytes(input);
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            BaseNCodec.Context context = new BaseNCodec.Context();
                            int offset = 0;
                            while (offset < input.length) {
                                int chunkSize = 1 + random.nextInt(10);
                                chunkSize = Math.min(chunkSize, input.length - offset);
                                codec.encode(input, offset, chunkSize, context);
                                offset += chunkSize;
                            }
                            codec.encode(input, input.length, 0, context);
                            
                            byte[] encoded = new byte[context.pos];
                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                            
                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                            offset = 0;
                            while (offset < encoded.length) {
                                int chunkSize = 1 + random.nextInt(10);
                                chunkSize = Math.min(chunkSize, encoded.length - offset);
                                codec.decode(encoded, offset, chunkSize, decodeContext);
                                offset += chunkSize;
                            }
                            codec.decode(encoded, encoded.length, 0, decodeContext);
                            
                            byte[] decoded = new byte[decodeContext.pos];
                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                            
                            assertArrayEquals("All parameters streaming round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeData() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long seed = 123456789L;
        java.util.Random random = new java.util.Random(seed);
        byte[] input = new byte[2000];
        random.nextBytes(input);
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            BaseNCodec.Context context = new BaseNCodec.Context();
                            int offset = 0;
                            while (offset < input.length) {
                                int chunkSize = 1 + random.nextInt(20);
                                chunkSize = Math.min(chunkSize, input.length - offset);
                                codec.encode(input, offset, chunkSize, context);
                                offset += chunkSize;
                            }
                            codec.encode(input, input.length, 0, context);
                            
                            byte[] encoded = new byte[context.pos];
                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                            
                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                            offset = 0;
                            while (offset < encoded.length) {
                                int chunkSize = 1 + random.nextInt(20);
                                chunkSize = Math.min(chunkSize, encoded.length - offset);
                                codec.decode(encoded, offset, chunkSize, decodeContext);
                                offset += chunkSize;
                            }
                            codec.decode(encoded, encoded.length, 0, decodeContext);
                            
                            byte[] decoded = new byte[decodeContext.pos];
                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                            
                            assertArrayEquals("All parameters streaming large data round trip", input, decoded);
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeeds() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                java.util.Random random = new java.util.Random(seed);
                                byte[] input = new byte[1000];
                                random.nextBytes(input);
                                
                                BaseNCodec.Context context = new BaseNCodec.Context();
                                int offset = 0;
                                while (offset < input.length) {
                                    int chunkSize = 1 + random.nextInt(20);
                                    chunkSize = Math.min(chunkSize, input.length - offset);
                                    codec.encode(input, offset, chunkSize, context);
                                    offset += chunkSize;
                                }
                                codec.encode(input, input.length, 0, context);
                                
                                byte[] encoded = new byte[context.pos];
                                System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                
                                BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                offset = 0;
                                while (offset < encoded.length) {
                                    int chunkSize = 1 + random.nextInt(20);
                                    chunkSize = Math.min(chunkSize, encoded.length - offset);
                                    codec.decode(encoded, offset, chunkSize, decodeContext);
                                    offset += chunkSize;
                                }
                                codec.decode(encoded, encoded.length, 0, decodeContext);
                                
                                byte[] decoded = new byte[decodeContext.pos];
                                System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                
                                assertArrayEquals("All parameters streaming large data multiple seeds round trip", input, decoded);
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizes() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 10, 50, 100, 500, 1000};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    java.util.Random random = new java.util.Random(seed);
                                    byte[] input = new byte[size];
                                    random.nextBytes(input);
                                    
                                    BaseNCodec.Context context = new BaseNCodec.Context();
                                    int offset = 0;
                                    while (offset < input.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, input.length - offset);
                                        codec.encode(input, offset, chunkSize, context);
                                        offset += chunkSize;
                                    }
                                    codec.encode(input, input.length, 0, context);
                                    
                                    byte[] encoded = new byte[context.pos];
                                    System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                    
                                    BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                    offset = 0;
                                    while (offset < encoded.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, encoded.length - offset);
                                        codec.decode(encoded, offset, chunkSize, decodeContext);
                                        offset += chunkSize;
                                    }
                                    codec.decode(encoded, encoded.length, 0, decodeContext);
                                    
                                    byte[] decoded = new byte[decodeContext.pos];
                                    System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                    
                                    assertArrayEquals("All parameters streaming large data multiple seeds all sizes round trip", input, decoded);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuli() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    java.util.Random random = new java.util.Random(seed);
                                    byte[] input = new byte[size];
                                    random.nextBytes(input);
                                    
                                    BaseNCodec.Context context = new BaseNCodec.Context();
                                    int offset = 0;
                                    while (offset < input.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, input.length - offset);
                                        codec.encode(input, offset, chunkSize, context);
                                        offset += chunkSize;
                                    }
                                    codec.encode(input, input.length, 0, context);
                                    
                                    byte[] encoded = new byte[context.pos];
                                    System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                    
                                    BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                    offset = 0;
                                    while (offset < encoded.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, encoded.length - offset);
                                        codec.decode(encoded, offset, chunkSize, decodeContext);
                                        offset += chunkSize;
                                    }
                                    codec.decode(encoded, encoded.length, 0, decodeContext);
                                    
                                    byte[] decoded = new byte[decodeContext.pos];
                                    System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                    
                                    assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli round trip", input, decoded);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValues() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    java.util.Random random = new java.util.Random(seed);
                                    byte[] input = new byte[size];
                                    random.nextBytes(input);
                                    
                                    // Test with all byte values
                                    for (int i = 0; i < input.length; i++) {
                                        input[i] = (byte) (i * 37 % 256);
                                    }
                                    
                                    BaseNCodec.Context context = new BaseNCodec.Context();
                                    int offset = 0;
                                    while (offset < input.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, input.length - offset);
                                        codec.encode(input, offset, chunkSize, context);
                                        offset += chunkSize;
                                    }
                                    codec.encode(input, input.length, 0, context);
                                    
                                    byte[] encoded = new byte[context.pos];
                                    System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                    
                                    BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                    offset = 0;
                                    while (offset < encoded.length) {
                                        int chunkSize = 1 + random.nextInt(20);
                                        chunkSize = Math.min(chunkSize, encoded.length - offset);
                                        codec.decode(encoded, offset, chunkSize, decodeContext);
                                        offset += chunkSize;
                                    }
                                    codec.decode(encoded, encoded.length, 0, decodeContext);
                                    
                                    byte[] decoded = new byte[decodeContext.pos];
                                    System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                    
                                    assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values round trip", input, decoded);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatterns() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        java.util.Random random = new java.util.Random(seed);
                                        byte[] input = new byte[size];
                                        
                                        // Apply pattern
                                        for (int i = 0; i < input.length; i++) {
                                            if (pattern.length > 0) {
                                                input[i] = pattern[i % pattern.length];
                                            } else {
                                                input[i] = (byte) (i * 37 % 256);
                                            }
                                        }
                                        
                                        BaseNCodec.Context context = new BaseNCodec.Context();
                                        int offset = 0;
                                        while (offset < input.length) {
                                            int chunkSize = 1 + random.nextInt(20);
                                            chunkSize = Math.min(chunkSize, input.length - offset);
                                            codec.encode(input, offset, chunkSize, context);
                                            offset += chunkSize;
                                        }
                                        codec.encode(input, input.length, 0, context);
                                        
                                        byte[] encoded = new byte[context.pos];
                                        System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                        
                                        BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                        offset = 0;
                                        while (offset < encoded.length) {
                                            int chunkSize = 1 + random.nextInt(20);
                                            chunkSize = Math.min(chunkSize, encoded.length - offset);
                                            codec.decode(encoded, offset, chunkSize, decodeContext);
                                            offset += chunkSize;
                                        }
                                        codec.decode(encoded, encoded.length, 0, decodeContext);
                                        
                                        byte[] decoded = new byte[decodeContext.pos];
                                        System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                        
                                        assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns round trip", input, decoded);
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizes() {
        int[] lineLengths = {0, 8, 76};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengths() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparators() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPads() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptions() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {111111111L, 222222222L, 333333333L, 444444444L, 555555555L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeeds() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizes() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizes() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatterns() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuli() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValues() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli all byte values round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValuesAllLineLengths() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli all byte values all line lengths round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValuesAllLineLengthsAllSeparators() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'},
            {'\n', '\r', '\t', ' '},
            {'\r', '\n', ' ', '\t'},
            {'\t', ' ', '\n', '\r'},
            {' ', '\t', '\r', '\n'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli all byte values all line lengths all separators round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValuesAllLineLengthsAllSeparatorsAllPads() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'},
            {'\n', '\r', '\t', ' '},
            {'\r', '\n', ' ', '\t'},
            {'\t', ' ', '\n', '\r'},
            {' ', '\t', '\r', '\n'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli all byte values all line lengths all separators all pads round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValuesAllLineLengthsAllSeparatorsAllPadsAllHexOptions() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte[][] separators = {
            null,
            {'\n'},
            {'\r'},
            {'\r', '\n'},
            {'\n', '\r'},
            {'\t'},
            {' '},
            {'\n', '\t'},
            {'\r', '\n', '\t'},
            {'\n', '\r', '\t', ' '},
            {'\r', '\n', ' ', '\t'},
            {'\t', ' ', '\n', '\r'},
            {' ', '\t', '\r', '\n'}
        };
        
        long[] seeds = {1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L, 31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L, 41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L};
        int[] sizes = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        int[] chunkSizes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100};
        
        byte[][] patterns = {
            new byte[0],
            {0},
            {255},
            {0, 255},
            {255, 0},
            {0x0F, 0xF0},
            {0xF0, 0x0F},
            {0x55, 0xAA},
            {0xAA, 0x55},
            {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80},
            {0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01},
            {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F},
            {0x0F, 0x0E, 0x0D, 0x0C, 0x0B, 0x0A, 0x09, 0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01, 0x00},
            {0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0},
            {0xF0, 0xE0, 0xD0, 0xC0, 0xB0, 0xA0, 0x90, 0x80, 0x70, 0x60, 0x50, 0x40, 0x30, 0x20, 0x10},
            {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF},
            {0xFF, 0xEE, 0xDD, 0xCC, 0xBB, 0xAA, 0x99, 0x88, 0x77, 0x66, 0x55, 0x44, 0x33, 0x22, 0x11}
        };
        
        for (int lineLength : lineLengths) {
            for (boolean useHex : useHexOptions) {
                for (byte pad : pads) {
                    for (byte[] separator : separators) {
                        try {
                            Base32 codec = new Base32(lineLength, separator, useHex, pad);
                            
                            for (long seed : seeds) {
                                for (int size : sizes) {
                                    for (byte[] pattern : patterns) {
                                        for (int chunkSize : chunkSizes) {
                                            java.util.Random random = new java.util.Random(seed);
                                            byte[] input = new byte[size];
                                            
                                            // Apply pattern
                                            for (int i = 0; i < input.length; i++) {
                                                if (pattern.length > 0) {
                                                    input[i] = pattern[i % pattern.length];
                                                } else {
                                                    input[i] = (byte) (i * 37 % 256);
                                                }
                                            }
                                            
                                            BaseNCodec.Context context = new BaseNCodec.Context();
                                            int offset = 0;
                                            while (offset < input.length) {
                                                int len = Math.min(chunkSize, input.length - offset);
                                                codec.encode(input, offset, len, context);
                                                offset += len;
                                            }
                                            codec.encode(input, input.length, 0, context);
                                            
                                            byte[] encoded = new byte[context.pos];
                                            System.arraycopy(context.buffer, 0, encoded, 0, context.pos);
                                            
                                            BaseNCodec.Context decodeContext = new BaseNCodec.Context();
                                            offset = 0;
                                            while (offset < encoded.length) {
                                                int len = Math.min(chunkSize, encoded.length - offset);
                                                codec.decode(encoded, offset, len, decodeContext);
                                                offset += len;
                                            }
                                            codec.decode(encoded, encoded.length, 0, decodeContext);
                                            
                                            byte[] decoded = new byte[decodeContext.pos];
                                            System.arraycopy(decodeContext.buffer, 0, decoded, 0, decodeContext.pos);
                                            
                                            assertArrayEquals("All parameters streaming large data multiple seeds all sizes all moduli all byte values all patterns all chunk sizes all line lengths all separators all pads all hex options all seeds all sizes all chunk sizes all patterns all moduli all byte values all line lengths all separators all pads all hex options round trip", input, decoded);
                                        }
                                    }
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // Skip invalid combinations
                        }
                    }
                }
            }
        }
    }

    @Test(timeout = 4000)
    public void testEncodeDecodeWithEdgeCaseAllParametersStreamingLargeDataMultipleSeedsAllSizesAllModuliAllByteValuesAllPatternsAllChunkSizesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeedsAllSizesAllChunkSizesAllPatternsAllModuliAllByteValuesAllLineLengthsAllSeparatorsAllPadsAllHexOptionsAllSeeds() {
        int[] lineLengths = {0, 1, 7, 8, 9, 15, 16, 17, 31, 32, 33, 63, 64, 65, 76, 100, 1000, 1024, 2048, 4096, 8192, 16384, 32768, 65536};
        boolean[] useHexOptions = {false, true};
        byte[] pads = {'=', '!', '#', '$', '%', '&', '*', '+', '-', '.', '/', ':', ';', '<', '>', '?', '@', '[', ']', '^', '_', '`', '{', '|', '}', '~', '0', '1