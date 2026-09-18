package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Components & Decision Branches:
 * 1. TarUtils.parseOctal(byte[], int, int):
 *    - Branch: length < 2 -> throw IllegalArgumentException.
 *    - Branch: buffer[start] == 0 -> return 0L.
 *    - Branch: leading spaces skipped -> while (buffer[start] == ' ') start++.
 *    - Branch: trailing spaces and NULs trimmed -> while (trailer == 0 || trailer == ' ').
 *    - Branch: invalid ASCII octal characters (< '0' || > '7') -> throw IllegalArgumentException.
 *
 * 2. TarUtils.parseOctalOrBinary(byte[], int, int):
 *    - Branch: (buffer[offset] & 0x80) == 0 -> calls parseOctal.
 *    - Branch: binary positive (buffer[offset] & 0x80 != 0, offset != 0xff).
 *    - Branch: binary negative (buffer[offset] == 0xff).
 *    - Branch: length < 9 -> parseBinaryLong (2's complement logic).
 *    - Branch: length >= 9 -> parseBinaryBigInteger (val.bitLength() > 63 check).
 *
 * 3. TarUtils.formatLongOctalOrBinaryBytes(long, byte[], int, int):
 *    - Branch: non-negative && value <= maxAsOctalChar (UIDLEN vs other lengths).
 *    - Branch: binary positive / negative paths.
 *    - Branch: length < 9 vs length >= 9.
 *    - DEFECT-TARGET: testRoundTripOctalOrBinary8 / formatLongBinary vs formatBigIntegerBinary fall-through.
 *      When length == 8 and value is negative, formatBigIntegerBinary throws
 *      "IllegalArgumentException: Value ... is too large for 8 byte field" if mistakenly executed.
 *
 * 4. TarUtils.parseName & formatNameBytes:
 *    - Branch: length == 0 / buffer all NULs -> returns "".
 *    - Branch: string truncation when name exceeds target length.
 *    - Branch: trailing NUL padding.
 *    - Custom & Fallback ZipEncoding execution paths.
 *
 * 5. TarUtils.formatUnsignedOctalString:
 *    - Branch: value == 0 -> write '0' and leading '0's.
 *    - Branch: value > 0 -> successive bitshifts; overflow check (val != 0 -> throw IllegalArgumentException).
 *
 * 6. TarUtils.computeCheckSum & verifyCheckSum:
 *    - Branch: unsignedSum == storedSum.
 *    - Branch: signedSum == storedSum (historic tar format heuristic).
 *    - Branch: mismatch -> false.
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseBoolean() {
        byte[] buffer = new byte[] { 1, 0, 2, -1 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
        assertFalse(TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testFormatAndParseOctalBytes() {
        byte[] buffer = new byte[12];
        int nextOffset = TarUtils.formatOctalBytes(12345L, buffer, 0, buffer.length);
        assertEquals(12, nextOffset);
        assertEquals((byte) ' ', buffer[buffer.length - 2]);
        assertEquals((byte) 0, buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(12345L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseLongOctalBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatLongOctalBytes(54321L, buffer, 0, buffer.length);
        assertEquals(10, nextOffset);
        assertEquals((byte) ' ', buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(54321L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(777L, buffer, 0, buffer.length);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0, buffer[buffer.length - 2]);
        assertEquals((byte) ' ', buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(777L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, buffer.length);
        for (byte b : buffer) {
            assertEquals((byte) '0', b);
        }
    }

    @Test(timeout = 4000)
    public void testComputeAndVerifyCheckSumMatchingUnsigned() {
        byte[] header = new byte[512];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i & 0x7F);
        }
        TarUtils.formatCheckSumOctalBytes(0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        long checkSum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(checkSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumMatchingSigned() {
        byte[] header = new byte[512];
        // Populate with bytes > 127 to create signed vs unsigned difference
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) 0xFE;
        }

        long signedSum = 0;
        for (int i = 0; i < header.length; i++) {
            byte b = header[i];
            if (TarConstants.CHKSUM_OFFSET <= i && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                b = ' ';
            }
            signedSum += b;
        }

        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumFailure() {
        byte[] header = new byte[512];
        TarUtils.formatCheckSumOctalBytes(1234L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testParseAndFormatName() throws IOException {
        byte[] buffer = new byte[20];
        int nextOffset = TarUtils.formatNameBytes("test-entry", buffer, 0, buffer.length);
        assertEquals(20, nextOffset);

        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test-entry", parsed);
    }

    @Test(timeout = 4000)
    public void testFormatNameTruncation() throws IOException {
        byte[] buffer = new byte[5];
        TarUtils.formatNameBytes("longfilename", buffer, 0, buffer.length);
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("longf", parsed);
    }

    @Test(timeout = 4000)
    public void testParseNameEmptyBuffer() throws IOException {
        byte[] buffer = new byte[10];
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", parsed);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalLeadingNulReturnsZero() {
        byte[] buffer = new byte[] { 0, '1', '2', ' ' };
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNulsReturnsZero() {
        byte[] buffer = new byte[4];
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingAndTrailingSpaces() {
        byte[] buffer = "  755   ".getBytes();
        assertEquals(493L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalTrailingNulAndSpace() {
        byte[] buffer = new byte[] { '7', '5', '5', ' ', 0 };
        assertEquals(493L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(timeout = 4000)
    public void testFormatOctalOrBinaryWithinOctalLimit() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int offset = TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, offset);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.MAXID, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatOctalOrBinaryExceedingOctalLimitPositive() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        long value = TarConstants.MAXID + 1L;
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, TarConstants.UIDLEN);
        assertEquals((byte) 0x80, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseBinaryBigIntegerPositive() {
        byte[] buffer = new byte[12];
        long value = TarConstants.MAXSIZE + 1024L;
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, buffer.length);
        assertEquals((byte) 0x80, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseBinaryBigIntegerNegative() {
        byte[] buffer = new byte[12];
        long value = -123456789L;
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, buffer.length);
        assertEquals((byte) 0xff, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testParseBinaryLongSmallLength() {
        byte[] buffer = new byte[] { (byte) 0x80, 0x01, 0x02 };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 3);
        assertEquals(258L, result);
    }

    @Test(timeout = 4000)
    public void testParseBinaryLongNegativeSmallLength() {
        // 3 bytes negative: 0xFF, followed by 2's complement of 1
        // Val = 1 -> inverted: -1
        byte[] buffer = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFE };
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 3);
        assertEquals(-2L, result);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets Defects4J known failure condition:
     * TarUtils.formatLongOctalOrBinaryBytes with a negative value fitting in an 8-byte field.
     * Bug: formatLongBinary is called, and execution mistakenly falls through to formatBigIntegerBinary,
     * which throws "IllegalArgumentException: Value ... is too large for 8 byte field."
     */
    @Test(timeout = 4000)
    public void testRoundTripOctalOrBinary8NegativeDefect() {
        long value = -72057594037927935L; // -(2^56 - 1)
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(value, result);
    }

    @Test(timeout = 4000)
    public void testRoundTripOctalOrBinary8NegativeOne() {
        long value = -1L;
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(value, result);
    }

    @Test(timeout = 4000)
    public void testRoundTripOctalOrBinary8Positive() {
        long value = 0x0001020304050607L;
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, 8);
        long result = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(value, result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThan2() {
        byte[] buffer = new byte[] { '0' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteHigh() {
        byte[] buffer = " 128 ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteAlpha() {
        byte[] buffer = " 12A ".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        // 64 requires at least 3 octal digits ('1', '0', '0')
        TarUtils.formatUnsignedOctalString(64L, buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryOverflow() {
        byte[] buffer = new byte[2];
        // 2-byte binary format allows max value < 2^(8*1) = 256
        TarUtils.formatLongOctalOrBinaryBytes(300L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryBigIntegerOverflow() {
        // Buffer of 10 bytes with all bits set (positive), exceeding 63 bits
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80;
        for (int i = 1; i < buffer.length; i++) {
            buffer[i] = (byte) 0xFF;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    // =========================================================================
    // Partition E: Encodings, Fallback, & Object Lifecycle Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFallbackEncodingCanEncodeAndEncodeDecode() {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        assertTrue(fallback.canEncode("anyName.txt"));

        String original = "test\u0080name";
        ByteBuffer encoded = fallback.encode(original);
        assertNotNull(encoded);

        byte[] raw = new byte[encoded.remaining()];
        encoded.get(raw);
        assertEquals(original.length(), raw.length);

        String decoded = fallback.decode(raw);
        assertEquals(original, decoded);
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingDecodeTrailingNull() {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        byte[] raw = new byte[] { 'a', 'b', 0, 'c' };
        String decoded = fallback.decode(raw);
        assertEquals("ab", decoded);
    }

    @Test(timeout = 4000)
    public void testParseNameWithExplicitEncoding() throws IOException {
        byte[] buffer = "custom-encoding-name\0extra".getBytes();
        String parsed = TarUtils.parseName(buffer, 0, buffer.length, TarUtils.FALLBACK_ENCODING);
        assertEquals("custom-encoding-name", parsed);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesWithExplicitEncoding() throws IOException {
        byte[] buffer = new byte[15];
        int offset = TarUtils.formatNameBytes("hello", buffer, 0, buffer.length, TarUtils.FALLBACK_ENCODING);
        assertEquals(15, offset);
        assertEquals('h', buffer[0]);
        assertEquals(0, buffer[5]);
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }
}