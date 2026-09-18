package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and conditions in TarUtils:
 * 
 * parseOctal():
 * - length < 2: IllegalArgumentException branch
 * - buffer[start] == 0: return 0L
 * - Leading space skipping loop
 * - trailer check: (trailer == 0 || trailer == ' ') -> end--, else throw
 * - Second trailer check loop: while (start < end - 1 && (trailer == 0 || trailer == ' '))
 * - Invalid octal digit: currentByte < '0' || currentByte > '7' -> throw
 * - Normal octal parsing with shift-and-add
 * 
 * parseOctalOrBinary():
 * - (buffer[offset] & 0x80) == 0 -> parseOctal path
 * - negative = buffer[offset] == (byte) 0xff
 * - length < 9 -> parseBinaryLong
 * - else -> parseBinaryBigInteger
 * 
 * parseBoolean():
 * - Returns buffer[offset] == 1
 * 
 * parseName() (single arg):
 * - DEFAULT_ENCODING.decode -> if IOException, try FALLBACK_ENCODING
 * 
 * parseName() (with encoding):
 * - Trims trailing NULs
 * - Returns "" if empty
 * 
 * formatUnsignedOctalString():
 * - value == 0 -> '0'
 * - val != 0 remainder loop
 * - if val != 0 after loop -> throw
 * - leading zero fill
 * 
 * formatOctalBytes():
 * - Uses idx=length-2 for space+null
 * 
 * formatLongOctalBytes():
 * - Uses idx=length-1 for space
 * 
 * formatLongOctalOrBinaryBytes():
 * - !negative && value <= maxAsOctalChar -> octal path
 * - length < 9 -> formatLongBinary
 * - else -> formatBigIntegerBinary
 * - Sets buf[offset] to 0xff or 0x80
 * 
 * computeCheckSum():
 * - Sum of (BYTE_MASK & element)
 * 
 * verifyCheckSum():
 * - Checksum extraction with digit counting
 * - storedSum == unsignedSum || storedSum == signedSum || storedSum > unsignedSum
 * 
 * DEFECT TARGET: parseOctal with "777777777777" (12 chars, no valid trailer)
 * - Known issue: buffer "777777777777" len=12 throws IllegalArgumentException
 *   because the trailer check fails (last byte is '7', not space or NUL)
 * - Expected behavior depends on spec; the bug is that it throws when it
 *   shouldn't (or the test expects it to succeed). Based on the defect,
 *   the test expects parseOctal to handle this input without throwing.
 */
public class TarUtilsDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testParseOctalNormal() {
        byte[] buffer = "     12345 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithLeadingSpaces() {
        byte[] buffer = "     0    ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNul() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithTrailingNul() {
        byte[] buffer = "  123\0\0\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(123L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalMaxValue() {
        byte[] buffer = "7777777 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        // 7777777 octal = 2097151 decimal
        assertEquals(2097151L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalZero() {
        byte[] buffer = "0000000 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buffer = " ".getBytes();
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalInvalidTrailer() {
        byte[] buffer = "12345X".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buffer = " 123A5 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalDigit8() {
        byte[] buffer = " 1238 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000)
    public void testParseOctalMultipleTrailing() {
        byte[] buffer = "123   \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(123L, result);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // This specifically targets the known defect:
    // org.apache.commons.compress.archivers.tar.TarUtilsTest::testParseOctal
    // --> java.lang.IllegalArgumentException: Invalid byte 55 at offset 11 in '777777777777' len=12
    // The buffer "777777777777" has 12 octal digits with no trailing space or NUL.
    // The bug is that parseOctal throws IllegalArgumentException instead of parsing it.
    // According to the spec, trailing NUL/space is required, but some implementations
    // omit it. The test expects the method to handle this gracefully.

    @Test(timeout = 4000)
    public void testParseOctalDefectTarget() {
        byte[] buffer = "777777777777".getBytes();
        // This should not throw; the expected value is the octal value of 777777777777
        // which would be a very large number, but the method may need to handle it.
        // Based on the known defect, the method throws when it shouldn't.
        // We'll verify it doesn't throw and returns a reasonable value.
        try {
            long result = TarUtils.parseOctal(buffer, 0, buffer.length);
            // If it returns without exception, the bug is fixed
            assertTrue("Should return a non-negative value", result >= 0);
        } catch (IllegalArgumentException e) {
            fail("parseOctal should handle buffer without trailer: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalDefectTargetWithSpaces() {
        byte[] buffer = "777777777777\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result); // Leading NUL causes early return of 0
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryWithBinaryNegative() {
        // Use binary negative value: first byte 0xff, length 8
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xff;
        buffer[1] = (byte) 0xff;
        buffer[2] = (byte) 0xff;
        buffer[3] = (byte) 0xff;
        buffer[4] = (byte) 0xff;
        buffer[5] = (byte) 0xff;
        buffer[6] = (byte) 0xff;
        buffer[7] = (byte) 0xff; // -1 in 8-byte two's complement
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(-1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryWithBinaryPositive() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0x80;
        buffer[7] = 1;
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryOctalPath() {
        byte[] buffer = "     123 \0".getBytes();
        long result = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(123L, result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseBinaryLongLengthTooLarge() {
        byte[] buffer = new byte[9];
        buffer[0] = (byte) 0xff;
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseBinaryBigIntegerTooLarge() {
        // Create a BigInteger that exceeds 63 bits
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0xff;
        buffer[1] = (byte) 0x80;
        // Large positive value
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000)
    public void testParseBooleanTrue() {
        byte[] buffer = {1};
        assertTrue(TarUtils.parseBoolean(buffer, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanFalse() {
        byte[] buffer = {0};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanNonZero() {
        byte[] buffer = {2};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testParseNameEmpty() {
        byte[] buffer = new byte[10];
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testParseNameWithContent() {
        byte[] buffer = "hello\0\0\0\0\0".getBytes();
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", result);
    }

    @Test(timeout = 4000)
    public void testParseNameFullBuffer() {
        byte[] buffer = "test".getBytes();
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", result);
    }

    @Test(timeout = 4000)
    public void testParseNameWithEncoding() throws Exception {
        byte[] buffer = "foo\0\0\0".getBytes();
        String result = TarUtils.parseName(buffer, 0, buffer.length, TarUtils.DEFAULT_ENCODING);
        assertEquals("foo", result);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesNormal() {
        byte[] buf = new byte[10];
        TarUtils.formatNameBytes("test", buf, 0, buf.length);
        assertEquals("test", new String(buf, 0, 4));
        // Check trailing NULs
        for (int i = 4; i < buf.length; i++) {
            assertEquals(0, buf[i]);
        }
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncated() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("hello", buf, 0, buf.length);
        assertEquals("hel", new String(buf, 0, 3));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(0, buf, 0, buf.length);
        assertEquals("00000", new String(buf));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringValue() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(65, buf, 0, buf.length); // 65 octal = 101
        assertEquals("00101", new String(buf));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(2097151L, buf, 0, buf.length); // 7777777 octal
        assertEquals("007777777", new String(buf, 4, 8));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(100, buf, 0, buf.length); // too big
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatOctalBytes(65L, buf, 0, buf.length);
        assertEquals(10, result);
        // Should have octal value followed by space and NUL
        assertTrue("Space at index 8", buf[8] == ' ');
        assertTrue("NUL at index 9", buf[9] == 0);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatLongOctalBytes(65L, buf, 0, buf.length);
        assertEquals(10, result);
        assertTrue("Space at index 9", buf[9] == ' ');
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(65L, buf, 0, buf.length);
        // Should use octal format
        assertTrue("Should end with space", buf[buf.length - 1] == ' ');
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryPositive() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0x100000L, buf, 0, buf.length);
        // Should use binary format with 0x80
        assertEquals((byte) 0x80, buf[0]);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, buf.length);
        assertEquals((byte) 0xff, buf[0]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFormatLongBinaryTooLarge() {
        byte[] buf = new byte[2];
        // Need to call private method via public method
        TarUtils.formatLongOctalOrBinaryBytes(256L, buf, 0, buf.length);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[10];
        int result = TarUtils.formatCheckSumOctalBytes(65L, buf, 0, buf.length);
        assertEquals(10, result);
        // Should have NUL then space at end
        assertTrue("NUL at index 8", buf[8] == 0);
        assertTrue("Space at index 9", buf[9] == ' ');
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buf = {1, 2, 3, 4, 5};
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(1 + 2 + 3 + 4 + 5, sum);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumLarge() {
        byte[] buf = new byte[100];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) i;
        }
        long sum = TarUtils.computeCheckSum(buf);
        // Sum of 0..99 each masked with 0xFF
        long expected = 0;
        for (int i = 0; i < 100; i++) {
            expected += (0xFF & (byte) i);
        }
        assertEquals(expected, sum);
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumValid() {
        // Create a minimal valid header (empty name, etc.)
        byte[] header = new byte[512];
        // Set checksum field to a computed value
        // First compute unsigned sum with spaces in checksum field
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            unsignedSum += 0xff & header[i];
        }
        // Make sure checksum area has spaces
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        // Write the stored checksum as octal
        String octal = Long.toOctalString(unsignedSum);
        int idx = TarConstants.CHKSUM_OFFSET;
        for (int i = 0; i < 6 - octal.length(); i++) {
            header[idx++] = '0';
        }
        for (char c : octal.toCharArray()) {
            header[idx++] = (byte) c;
        }
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumMismatch() {
        byte[] header = new byte[512];
        // Fill with garbage, no valid checksum
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) 0xff;
        }
        // The checksum field should contain spaces for calculation, but stored sum is random
        boolean result = TarUtils.verifyCheckSum(header);
        // Could be true or false depending on random data, just ensure no exception
        // In practice, this likely returns false
    }
}