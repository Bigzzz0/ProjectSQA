package org.apache.commons.compress.archivers.tar;

import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUMLEN;
import static org.apache.commons.compress.archivers.tar.TarConstants.CHKSUM_OFFSET;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

/**
 * White-box test suite for TarUtils.
 * Targets all public methods, boundary conditions, and the known defect
 * (COMPRESS-335) where verifyCheckSum may incorrectly reject valid headers.
 *
 * Branch & Defect Analysis Matrix:
 * - parseOctal: length<2, leading NUL, leading spaces, trailing NUL/space, invalid octal digit
 * - parseOctalOrBinary: octal path, binary long (length<9), binary big integer (length>=9), negative
 * - parseBoolean: byte==1 -> true, else false
 * - parseName: NUL termination, empty, encoding fallback
 * - formatUnsignedOctalString: value=0, positive, overflow
 * - formatOctalBytes, formatLongOctalBytes, formatCheckSumOctalBytes: correct trailing chars
 * - formatLongOctalOrBinaryBytes: octal fits, binary long, binary big integer, negative
 * - computeCheckSum: simple sum
 * - verifyCheckSum: valid header, header with leading NUL in checksum field, all zeros, mismatched sum
 */
public class TarUtilsDeepseekTest {

    // ========== Partition A: parseOctal ==========

    @Test(timeout = 4000)
    public void testParseOctalNormal() {
        byte[] buf = "0000755 ".getBytes(); // typical octal
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(493, result); // 0755 octal = 493 decimal
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingSpaces() {
        byte[] buf = "   123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83, result); // 123 octal = 83 decimal
    }

    @Test(timeout = 4000)
    public void testParseOctalTrailingNul() {
        byte[] buf = "000755\0 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(493, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingNulReturnsZero() {
        byte[] buf = new byte[]{0, '1', '2', '3', ' ', 0};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buf = new byte[]{'1'};
        TarUtils.parseOctal(buf, 0, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buf = "0008 ".getBytes(); // '8' is invalid octal
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNul() {
        byte[] buf = new byte[10];
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    // ========== Partition B: parseOctalOrBinary ==========

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryOctalPath() {
        byte[] buf = "0000755 ".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(493, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryLongPositive() {
        // length < 9, high bit set -> binary
        byte[] buf = new byte[8];
        buf[0] = (byte) 0x80; // indicates binary
        buf[1] = 0x01; // value = 1
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryLongNegative() {
        // length < 9, high bit set, first byte 0xff -> negative
        byte[] buf = new byte[8];
        buf[0] = (byte) 0xff; // negative indicator
        buf[1] = (byte) 0xff; // 2's complement of -1 in 7 bytes? Actually for 8-byte field, -1 is all 0xff
        // For length=8, negative, value = -1
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryBigIntegerPositive() {
        // length >= 9, high bit set
        byte[] buf = new byte[9];
        buf[0] = (byte) 0x80;
        buf[1] = 0x01; // value = 1
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryBigIntegerNegative() {
        // length >= 9, negative
        byte[] buf = new byte[9];
        buf[0] = (byte) 0xff;
        buf[1] = (byte) 0xff; // -1 in 2's complement for 8 bytes
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1L, result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryBinaryBigIntegerOverflow() {
        // value > Long.MAX_VALUE
        byte[] buf = new byte[10];
        buf[0] = (byte) 0x80;
        // set all remaining bytes to 0xff -> BigInteger value = 2^72 - 1 > Long.MAX_VALUE
        Arrays.fill(buf, 1, buf.length, (byte) 0xff);
        TarUtils.parseOctalOrBinary(buf, 0, buf.length);
    }

    // ========== Partition C: parseBoolean ==========

    @Test(timeout = 4000)
    public void testParseBooleanTrue() {
        byte[] buf = new byte[]{1};
        assertTrue(TarUtils.parseBoolean(buf, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanFalse() {
        byte[] buf = new byte[]{0};
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanOther() {
        byte[] buf = new byte[]{2};
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }

    // ========== Partition D: parseName ==========

    @Test(timeout = 4000)
    public void testParseNameNormal() {
        byte[] buf = "hello\0world".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello", name);
    }

    @Test(timeout = 4000)
    public void testParseNameEmpty() {
        byte[] buf = new byte[10];
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("", name);
    }

    @Test(timeout = 4000)
    public void testParseNameWithEncoding() throws IOException {
        byte[] buf = "test\0".getBytes("UTF-8");
        String name = TarUtils.parseName(buf, 0, buf.length, TarUtils.DEFAULT_ENCODING);
        assertEquals("test", name);
    }

    // ========== Partition E: formatUnsignedOctalString ==========

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0, buf, 0, buf.length);
        assertArrayEquals(new byte[]{'0', '0', '0', '0'}, buf);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringPositive() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(493, buf, 0, buf.length);
        assertArrayEquals(new byte[]{'0', '7', '5', '5'}, buf);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64, buf, 0, buf.length); // 64 octal = 100, needs 3 chars
    }

    // ========== Partition F: formatOctalBytes, formatLongOctalBytes, formatCheckSumOctalBytes ==========

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatOctalBytes(493, buf, 0, buf.length);
        assertEquals(6, newOffset);
        // expected: "000755" + space + NUL? Actually formatOctalBytes: idx=length-2, then space, then NUL
        // So for length=6, idx=4, formatUnsignedOctalString writes to indices 0-3, then buf[4]=' ', buf[5]=0
        assertArrayEquals(new byte[]{'0', '0', '0', '7', ' ', 0}, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[5];
        int newOffset = TarUtils.formatLongOctalBytes(493, buf, 0, buf.length);
        assertEquals(5, newOffset);
        // idx=length-1=4, formatUnsignedOctalString writes to 0-3, then buf[4]=' '
        assertArrayEquals(new byte[]{'0', '0', '0', '7', ' '}, buf);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatCheckSumOctalBytes(493, buf, 0, buf.length);
        assertEquals(6, newOffset);
        // idx=length-2=4, formatUnsignedOctalString writes to 0-3, then buf[4]=0, buf[5]=' '
        assertArrayEquals(new byte[]{'0', '0', '0', '7', 0, ' '}, buf);
    }

    // ========== Partition G: formatLongOctalOrBinaryBytes ==========

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOctalFits() {
        byte[] buf = new byte[8];
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(493, buf, 0, buf.length);
        assertEquals(8, newOffset);
        // Should use octal format: "0000755 " (7 chars + space)
        assertArrayEquals(new byte[]{'0', '0', '0', '0', '7', '5', '5', ' '}, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryLong() {
        byte[] buf = new byte[8];
        // value too large for octal in 8-byte field? Actually max octal for 8 bytes is 77777777? But we use length=8, so octal fits up to 7777777? Not important.
        // Use negative value to force binary
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, buf.length);
        assertEquals(8, newOffset);
        // Should be binary: first byte 0xff, rest 0xff for -1 in 7 bytes? Actually for length=8, binary long path (length<9) is used.
        // formatLongBinary: for negative, val = Math.abs(-1)=1, then val ^= max-1, val |= 0xff<<bits, val++. bits=56, max=2^56.
        // Result should be all 0xff.
        for (int i = 0; i < 8; i++) {
            assertEquals((byte) 0xff, buf[i]);
        }
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryBigInteger() {
        byte[] buf = new byte[9];
        // length >=9, negative value
        int newOffset = TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, buf.length);
        assertEquals(9, newOffset);
        // First byte 0xff, rest 0xff for -1 in 8 bytes
        for (int i = 0; i < 9; i++) {
            assertEquals((byte) 0xff, buf[i]);
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesTooLargeForBinaryLong() {
        byte[] buf = new byte[8];
        // value too large for 7-byte binary (max 2^56-1)
        TarUtils.formatLongOctalOrBinaryBytes(1L << 56, buf, 0, buf.length);
    }

    // ========== Partition H: computeCheckSum ==========

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3};
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(1 + 2 + 3, sum);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumEmpty() {
        byte[] buf = new byte[0];
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(0, sum);
    }

    // ========== Partition I: verifyCheckSum (targeting COMPRESS-335) ==========

    /**
     * Build a minimal tar header with a given checksum.
     * The header is 512 bytes. We set the checksum field at CHKSUM_OFFSET (148) of length CHKSUMLEN (8).
     * All other bytes are set to 0 (or spaces for the checksum field).
     * The checksum is computed as the sum of all bytes with the checksum field replaced by spaces.
     */
    private byte[] buildHeaderWithChecksum(long checksum) {
        byte[] header = new byte[512];
        // Fill with zeros
        // Set checksum field: octal representation with leading zeros, then NUL, then space
        // Format: 6 octal digits, NUL, space
        String octal = String.format("%06o", checksum);
        byte[] checksumBytes = (octal + "\0 ").getBytes();
        System.arraycopy(checksumBytes, 0, header, CHKSUM_OFFSET, CHKSUMLEN);
        return header;
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumValid() {
        // Compute the correct checksum for a header with all zeros except checksum field
        byte[] header = new byte[512];
        // Set checksum field to spaces initially for sum calculation
        for (int i = CHKSUM_OFFSET; i < CHKSUM_OFFSET + CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long correctSum = TarUtils.computeCheckSum(header);
        // Now set the actual checksum field
        header = buildHeaderWithChecksum(correctSum);
        assertTrue("verifyCheckSum should return true for a valid header", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumLeadingNulInChecksumField() {
        // Some tar implementations put a leading NUL before the octal digits.
        // The checksum field should still be valid.
        byte[] header = new byte[512];
        // Set checksum field to spaces for sum calculation
        for (int i = CHKSUM_OFFSET; i < CHKSUM_OFFSET + CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long correctSum = TarUtils.computeCheckSum(header);
        // Now set the actual checksum field with a leading NUL
        String octal = String.format("%06o", correctSum);
        byte[] checksumBytes = ("\0" + octal.substring(0, 5) + "\0 ").getBytes(); // leading NUL, then 5 digits, NUL, space
        System.arraycopy(checksumBytes, 0, header, CHKSUM_OFFSET, CHKSUMLEN);
        // This header should be considered valid by verifyCheckSum
        assertTrue("verifyCheckSum should accept header with leading NUL in checksum field", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumMismatch() {
        byte[] header = new byte[512];
        // Set checksum field to spaces for sum calculation
        for (int i = CHKSUM_OFFSET; i < CHKSUM_OFFSET + CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        long correctSum = TarUtils.computeCheckSum(header);
        // Set a different checksum
        header = buildHeaderWithChecksum(correctSum + 1);
        assertFalse("verifyCheckSum should return false for mismatched checksum", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumAllZeros() {
        byte[] header = new byte[512];
        // All zeros: checksum field is all zeros, but the sum of the header (with zeros replaced by spaces) is not zero.
        // The stored checksum is 0, but the computed sum is not 0, so should be false.
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    // ========== Additional edge cases for parseOctalOrBinary ==========

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryLongEdge() {
        // length = 2, high bit set, negative
        byte[] buf = new byte[2];
        buf[0] = (byte) 0xff;
        buf[1] = (byte) 0xff; // -1 in 1 byte? Actually for length=2, bits=8, max=256, negative: val=1, val ^= 255, val |= 0xff<<8, val++ -> 0xffff? Let's just test that it doesn't throw.
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryBinaryLongPositiveEdge() {
        byte[] buf = new byte[2];
        buf[0] = (byte) 0x80;
        buf[1] = 0x01; // value = 1
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, result);
    }

    // ========== Test for formatNameBytes ==========

    @Test(timeout = 4000)
    public void testFormatNameBytes() {
        byte[] buf = new byte[10];
        int newOffset = TarUtils.formatNameBytes("hello", buf, 0, buf.length);
        assertEquals(10, newOffset);
        // Should copy "hello" and pad with NULs
        byte[] expected = new byte[10];
        System.arraycopy("hello".getBytes(), 0, expected, 0, 5);
        // rest are zeros
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncated() {
        byte[] buf = new byte[3];
        int newOffset = TarUtils.formatNameBytes("hello", buf, 0, buf.length);
        assertEquals(3, newOffset);
        // Should truncate to "hel"
        assertArrayEquals("hel".getBytes(), buf);
    }

    // ========== Test for exceptionMessage (indirectly) ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalExceptionMessage() {
        byte[] buf = "abc ".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            assertTrue(msg.contains("Invalid byte"));
            assertTrue(msg.contains("len=" + buf.length));
            throw e;
        }
    }
}