package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Methods Under Test:
 *  - parseOctal(byte[], int, int)
 *  - parseOctalOrBinary(byte[], int, int)
 *  - parseBoolean(byte[], int)
 *  - parseName(byte[], int, int)
 *  - formatNameBytes(String, byte[], int, int)
 *  - formatUnsignedOctalString(long, byte[], int, int)
 *  - formatOctalBytes(long, byte[], int, int)
 *  - formatLongOctalBytes(long, byte[], int, int)
 *  - formatLongOctalOrBinaryBytes(long, byte[], int, int)
 *  - formatCheckSumOctalBytes(long, byte[], int, int)
 *  - computeCheckSum(byte[])
 *
 * Decision / Condition Coverage Targets:
 *  - parseOctal:
 *      * length < 2 (defensive exception)
 *      * allNUL check (all zero bytes returns 0L)
 *      * leading space skipping (all spaces, partial spaces, no spaces)
 *      * leading NUL detection (COMPRESS-191 / workaroundForBrokenTimeHeader bug target)
 *      * trailing space / NUL trailer validation (end-1 valid, invalid; end-2 additional valid/absent)
 *      * octal digit character validation (< '0', > '7', valid range '0'-'7')
 *  - parseOctalOrBinary:
 *      * MSB not set (buffer[offset] & 0x80 == 0) -> delegates to parseOctal
 *      * MSB set (buffer[offset] & 0x80 != 0) -> binary parsing mode
 *      * binary overflow detection: val >= (1L << 55) throws IllegalArgumentException
 *      * maximum signed long (Long.MAX_VALUE) boundary
 *  - parseBoolean:
 *      * buffer[offset] == 1 (true)
 *      * buffer[offset] != 1 (false: 0, -1, 2, 255)
 *  - parseName:
 *      * string with embedded NUL (terminates at NUL)
 *      * string without NUL (reads full length)
 *      * byte values with high bit set (sign-extension prevention: b & 0xFF)
 *  - formatNameBytes:
 *      * name length < buffer length (copies + pads trailing NULs)
 *      * name length == buffer length (exact fit)
 *      * name length > buffer length (truncation)
 *  - formatUnsignedOctalString:
 *      * value == 0 (writes '0' padded with leading '0's)
 *      * value > 0 fitting exactly or needing leading '0' padding
 *      * value too large to fit in length (throws IllegalArgumentException)
 *  - formatLongOctalOrBinaryBytes:
 *      * UIDLEN vs other field length (MAXID vs MAXSIZE threshold)
 *      * value <= maxAsOctalChar -> delegates to formatLongOctalBytes
 *      * value > maxAsOctalChar -> binary big-endian encoding
 *      * binary value too large: val != 0 || (buf[offset] & 0x80) != 0 -> IllegalArgumentException
 *  - computeCheckSum:
 *      * unsigned byte addition (BYTE_MASK & buf[i])
 *      * empty array, negative byte values
 *
 * Defect Zone (Defects4J - workaroundForBrokenTimeHeader):
 *  - TarArchiveInputStreamTest::workaroundForBrokenTimeHeader fails when an octal header field
 *    starts with a leading NUL byte (or spaces then NUL) alongside non-zero/space data.
 *    The javadoc explicitly specifies: "To work-around some tar implementations that insert a
 *    leading NUL this method returns 0 if it detects a leading NUL since Commons Compress 1.4."
 * ====================================================================================================
 */
public class TarUtilsGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseOctalValidStandard() {
        byte[] buffer = "0755 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithLeadingSpacesAndTrailingSpace() {
        byte[] buffer = "   123 ".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithTrailingNulAndSpace() {
        byte[] buffer = new byte[] { '7', '7', 0, ' ' };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(077L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithSingleTrailingSpace() {
        byte[] buffer = new byte[] { '7', ' ' };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(7L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithSingleTrailingNul() {
        byte[] buffer = new byte[] { '5', 0 };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(5L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryStandardOctal() {
        byte[] buffer = "0000755 \0".getBytes();
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(0755L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryPositiveBinary() {
        byte[] buffer = new byte[] {
            (byte) 0x80, 0, 0, 0, 0, 0, 0, 1
        };
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, value);
    }

    @Test(timeout = 4000)
    public void testParseBooleanTrueAndFalse() {
        byte[] buffer = new byte[] { 1, 0, 2, (byte) 255 };
        assertTrue("Byte value 1 must parse to true", TarUtils.parseBoolean(buffer, 0));
        assertFalse("Byte value 0 must parse to false", TarUtils.parseBoolean(buffer, 1));
        assertFalse("Byte value 2 must parse to false", TarUtils.parseBoolean(buffer, 2));
        assertFalse("Byte value 255 must parse to false", TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testParseNameTerminatedByNul() {
        byte[] buffer = new byte[] { 'a', 'b', 'c', 0, 'd', 'e' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("abc", name);
    }

    @Test(timeout = 4000)
    public void testParseNameNotTerminatedByNul() {
        byte[] buffer = new byte[] { 'h', 'e', 'l', 'l', 'o' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesFitWithPadding() {
        byte[] buffer = new byte[8];
        int endOffset = TarUtils.formatNameBytes("tar", buffer, 1, 6);
        assertEquals(7, endOffset);
        assertEquals(0, buffer[0]);
        assertEquals((byte) 't', buffer[1]);
        assertEquals((byte) 'a', buffer[2]);
        assertEquals((byte) 'r', buffer[3]);
        assertEquals(0, buffer[4]);
        assertEquals(0, buffer[5]);
        assertEquals(0, buffer[6]);
        assertEquals(0, buffer[7]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncated() {
        byte[] buffer = new byte[3];
        int endOffset = TarUtils.formatNameBytes("abcdef", buffer, 0, 3);
        assertEquals(3, endOffset);
        assertEquals((byte) 'a', buffer[0]);
        assertEquals((byte) 'b', buffer[1]);
        assertEquals((byte) 'c', buffer[2]);
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytesRoundTrip() {
        byte[] buffer = new byte[8];
        int endOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, buffer.length);
        assertEquals(buffer.length, endOffset);
        assertEquals((byte) ' ', buffer[buffer.length - 2]);
        assertEquals(0, buffer[buffer.length - 1]);
        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytesRoundTrip() {
        byte[] buffer = new byte[8];
        int endOffset = TarUtils.formatLongOctalBytes(0777L, buffer, 0, buffer.length);
        assertEquals(buffer.length, endOffset);
        assertEquals((byte) ' ', buffer[buffer.length - 1]);
        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0777L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytesRoundTrip() {
        byte[] buffer = new byte[8];
        int endOffset = TarUtils.formatCheckSumOctalBytes(0123L, buffer, 0, buffer.length);
        assertEquals(buffer.length, endOffset);
        assertEquals(0, buffer[buffer.length - 2]);
        assertEquals((byte) ' ', buffer[buffer.length - 1]);
        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, parsed);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buffer = new byte[] { 1, 2, 3, (byte) 255, (byte) 0x80 };
        // 1 + 2 + 3 + 255 + 128 = 389
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(389L, sum);
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testParseOctalAllNulReturnsZero() {
        byte[] buffer = new byte[6];
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpacesReturnsZero() {
        byte[] buffer = new byte[] { ' ', ' ', ' ' };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalMinimumAllowedLengthTwo() {
        byte[] buffer = new byte[] { '0', ' ' };
        long value = TarUtils.parseOctal(buffer, 0, 2);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertArrayEquals(new byte[] { '0', '0', '0', '0' }, buffer);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringNonZeroLeadingZeros() {
        byte[] buffer = new byte[5];
        TarUtils.formatUnsignedOctalString(7L, buffer, 1, 4);
        assertEquals(0, buffer[0]);
        assertEquals((byte) '0', buffer[1]);
        assertEquals((byte) '0', buffer[2]);
        assertEquals((byte) '0', buffer[3]);
        assertEquals((byte) '7', buffer[4]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumEmptyBuffer() {
        long sum = TarUtils.computeCheckSum(new byte[0]);
        assertEquals(0L, sum);
    }

    @Test(timeout = 4000)
    public void testParseNameHighByteSignExtension() {
        byte[] buffer = new byte[] { (byte) 0xE9, (byte) 0xFF, 0 };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(2, name.length());
        assertEquals(0x00E9, name.charAt(0));
        assertEquals(0x00FF, name.charAt(1));
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryMaxSignedLong() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0x80;
        buffer[1] = 0x7f;
        for (int i = 2; i < 9; i++) {
            buffer[i] = (byte) 0xff;
        }
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(Long.MAX_VALUE, value);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesUnderUidThreshold() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int endOffset = TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, endOffset);
        assertEquals((byte) ' ', buffer[TarConstants.UIDLEN - 1]);
        long parsed = TarUtils.parseOctal(buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.MAXID, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesUnderSizeThreshold() {
        byte[] buffer = new byte[12];
        int endOffset = TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXSIZE, buffer, 0, 12);
        assertEquals(12, endOffset);
        assertEquals((byte) ' ', buffer[11]);
        long parsed = TarUtils.parseOctal(buffer, 0, 12);
        assertEquals(TarConstants.MAXSIZE, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOverUidThresholdUsesBinary() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        long valueToEncode = TarConstants.MAXID + 1024L;
        int endOffset = TarUtils.formatLongOctalOrBinaryBytes(valueToEncode, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, endOffset);
        assertTrue("MSB of first byte must be set in binary mode", (buffer[0] & 0x80) != 0);

        long roundTripped = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(valueToEncode, roundTripped);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOverSizeThresholdUsesBinary() {
        byte[] buffer = new byte[12];
        long valueToEncode = TarConstants.MAXSIZE + 4096L;
        int endOffset = TarUtils.formatLongOctalOrBinaryBytes(valueToEncode, buffer, 0, 12);
        assertEquals(12, endOffset);
        assertTrue("MSB of first byte must be set in binary mode", (buffer[0] & 0x80) != 0);

        long roundTripped = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        assertEquals(valueToEncode, roundTripped);
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: workaroundForBrokenTimeHeader)
    // ================================================================================================

    /**
     * Target Defect:
     * TarArchiveInputStreamTest::workaroundForBrokenTimeHeader
     * Failure symptom: java.io.IOException: Error detected parsing the header
     *
     * Root Cause:
     * Certain broken TAR creators emit time headers with a leading NUL byte instead of standard
     * ASCII octal digits or spaces. The specification explicitly dictates that when a leading NUL
     * is detected, parseOctal MUST return 0L rather than failing on non-octal character parsing.
     */
    @Test(timeout = 4000)
    public void testWorkaroundForBrokenTimeHeaderLeadingNul() {
        byte[] buffer = new byte[] { 0, '1', '2', '3', ' ' };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals("Leading NUL in octal header field must return 0L as per workaround specification", 0L, value);
    }

    @Test(timeout = 4000)
    public void testWorkaroundForBrokenTimeHeaderLeadingSpacesThenNul() {
        byte[] buffer = new byte[] { ' ', ' ', 0, '7', '5', '5', ' ' };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals("Leading spaces followed by NUL must return 0L as per workaround specification", 0L, value);
    }

    @Test(timeout = 4000)
    public void testWorkaroundForBrokenTimeHeaderParseOctalOrBinary() {
        byte[] buffer = new byte[] { 0, '7', '7', '7', ' ' };
        long value = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals("parseOctalOrBinary must return 0L when encountering leading NUL workaround header", 0L, value);
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThanTwoThrows() {
        TarUtils.parseOctal(new byte[] { '0' }, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalMissingTrailingSpaceOrNulThrows() {
        byte[] buffer = new byte[] { '1', '2', '3' };
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidTrailingByteExceptionMessageContainsNulReplacement() {
        byte[] buffer = new byte[] { 0, 'a', 'x' };
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for invalid trailing character");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should replace NUL with {NUL}", e.getMessage().contains("{NUL}"));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidDigitBelowZeroThrows() {
        byte[] buffer = new byte[] { '1', '/', ' ' };
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidDigitAboveSevenThrows() {
        byte[] buffer = new byte[] { '1', '8', ' ' };
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalOrBinaryBinaryExceedsMaxSignedLongThrows() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0x80;
        buffer[1] = (byte) 0x80; // val >= (1L << 55) triggers signed 64-bit overflow guard
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringWillNotFitThrows() {
        byte[] buffer = new byte[2];
        // 0100 (64) requires 3 octal digits, cannot fit in length 2
        TarUtils.formatUnsignedOctalString(64L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatOctalBytesTooSmallForValueThrows() {
        byte[] buffer = new byte[3];
        // Length 3 gives idx = 1 for octal digits; 077 needs 2 digits
        TarUtils.formatOctalBytes(077L, buffer, 0, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesValueNegativeThrows() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buffer, 0, 8);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesValueExceedsCapacityThrows() {
        byte[] buffer = new byte[2];
        // Attempting to write a 64-bit value into 2 bytes binary field where val cannot fit
        TarUtils.formatLongOctalOrBinaryBytes(0x10000L, buffer, 0, 2);
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorContract() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertTrue("TarUtils constructor must be private", Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull("Instantiated utility class instance should not be null", instance);
    }
}