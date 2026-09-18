package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Methods Under Test & Branch Coverage Objectives:
 * 1. parseOctal(byte[], int, int):
 *    - Branch: length < 2 -> IllegalArgumentException
 *    - Branch: buffer[start] == 0 -> returns 0L (leading NUL)
 *    - Branch: leading spaces skipped (buffer[start] == ' ')
 *    - Branch: trailing spaces and NULs trimmed (trailer == 0 || trailer == ' ')
 *    - Branch: start == end -> KNOWN DEFECT (Defects4J Compress):
 *      When buffer contains only space(s) and trailing NUL (e.g. {' ', 0}), start advances to 1
 *      and end retreats to 1. start == end triggers IllegalArgumentException instead of returning 0L.
 *    - Branch: currentByte < '0' || currentByte > '7' -> IllegalArgumentException with formatted message
 *    - Branch: valid octal digits accumulate correctly
 *
 * 2. parseOctalOrBinary(byte[], int, int):
 *    - Branch: (buffer[offset] & 0x80) == 0 -> delegates to parseOctal
 *    - Branch: (buffer[offset] & 0x80) != 0 -> binary mode
 *      - Branch: negative (buffer[offset] == (byte) 0xff) vs positive
 *      - Branch: length < 9 -> parseBinaryLong
 *      - Branch: length >= 9 -> parseBinaryBigInteger
 *      - Branch: parseBinaryLong length >= 9 guard check
 *      - Branch: parseBinaryBigInteger val.bitLength() > 63 -> IllegalArgumentException
 *
 * 3. parseBoolean(byte[], int):
 *    - Branch: buffer[offset] == 1 -> true; buffer[offset] != 1 -> false
 *
 * 4. parseName(byte[], int, int) & parseName(byte[], int, int, ZipEncoding):
 *    - Branch: all NUL -> returns ""
 *    - Branch: trailing NUL trimmed
 *    - Branch: decoding via DEFAULT_ENCODING / FALLBACK_ENCODING
 *
 * 5. formatNameBytes(String, byte[], int, int) & with ZipEncoding:
 *    - Branch: name.length() < length (pads trailing NULs)
 *    - Branch: name.length() > length (truncation loop while b.limit() > length)
 *    - Branch: exact fit
 *
 * 6. formatUnsignedOctalString(long, byte[], int, int):
 *    - Branch: value == 0 -> writes '0' and pads leading '0's
 *    - Branch: value > 0 -> encodes digits, pads leading '0's
 *    - Branch: val != 0 when remaining < 0 -> IllegalArgumentException (overflow)
 *
 * 7. formatOctalBytes & formatLongOctalBytes & formatCheckSumOctalBytes:
 *    - Correct padding with trailing spaces / NULs and offsets returned
 *
 * 8. formatLongOctalOrBinaryBytes(long, byte[], int, int):
 *    - Branch: UIDLEN vs standard MAXSIZE threshold check
 *    - Branch: !negative && value <= maxAsOctalChar -> formatLongOctalBytes
 *    - Branch: value > maxAsOctalChar || negative:
 *      - Branch: length < 9 -> formatLongBinary
 *      - Branch: formatBigIntegerBinary
 *      - Branch: formatLongBinary val >= max overflow guard -> IllegalArgumentException
 *
 * 9. computeCheckSum(byte[]) & verifyCheckSum(byte[]):
 *    - Branch: storedSum == unsignedSum -> true
 *    - Branch: storedSum == signedSum -> true
 *    - Branch: storedSum > unsignedSum (COMPRESS-177 heuristic) -> true
 *    - Branch: checksum mismatch -> false
 *    - Branch: digits sequence in checksum field parsing
 *
 * 10. TarUtils.FALLBACK_ENCODING:
 *     - canEncode(String) -> true
 *     - encode(String) -> ByteBuffer
 *     - decode(byte[]) -> String (handles sign-extension & trailing NUL)
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalValidValues() {
        byte[] buffer = new byte[] { ' ', ' ', '0', '7', '5', '5', ' ', 0 };
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, result); // 0755 octal = 493 decimal
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingZeroes() {
        byte[] buffer = new byte[] { '0', '0', '0', '1', '2', ' ' };
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(10L, result); // 012 octal = 10 decimal
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingNullReturnsZero() {
        byte[] buffer = new byte[] { 0, '7', '5', '5', ' ' };
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseBoolean() {
        byte[] buffer = new byte[] { 0, 1, 2, (byte) 255 };
        assertFalse(TarUtils.parseBoolean(buffer, 0));
        assertTrue(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
        assertFalse(TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testParseNameNormalAndEmpty() {
        byte[] buffer = new byte[] { 'h', 'e', 'l', 'l', 'o', 0, 'x', 'y' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", name);

        byte[] emptyBuffer = new byte[] { 0, 0, 0 };
        assertEquals("", TarUtils.parseName(emptyBuffer, 0, emptyBuffer.length));
    }

    @Test(timeout = 4000)
    public void testParseNameNoNullTerminator() {
        byte[] buffer = new byte[] { 'f', 'i', 'l', 'e' };
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("file", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesFitAndTruncate() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("abc", buf, 0, 6);
        assertEquals(6, nextOffset);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals(0, buf[3]);
        assertEquals(0, buf[4]);
        assertEquals(0, buf[5]);

        // Truncation when name exceeds length
        byte[] truncBuf = new byte[4];
        int truncOffset = TarUtils.formatNameBytes("toolongname", truncBuf, 0, 4);
        assertEquals(4, truncOffset);
        assertEquals("tool", new String(truncBuf, 0, 4));
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatOctalBytes(075, buf, 0, 6);
        assertEquals(6, offset);
        // formatOctalBytes: idx = length - 2 = 4 for unsigned octal, then ' ', then 0
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals('5', buf[3]);
        assertEquals(' ', buf[4]);
        assertEquals(0, buf[5]);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatLongOctalBytes(012, buf, 0, 5);
        assertEquals(5, offset);
        // formatLongOctalBytes: idx = length - 1 = 4 for unsigned octal, then ' '
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('2', buf[3]);
        assertEquals(' ', buf[4]);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatCheckSumOctalBytes(077, buf, 0, 6);
        assertEquals(6, offset);
        // idx = length - 2 = 4 for octal string, then 0, then ' '
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals('7', buf[3]);
        assertEquals(0, buf[4]);
        assertEquals(' ', buf[5]);
    }

    @Test(timeout = 4000)
    public void testComputeAndVerifyCheckSum() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) ('A' + (i % 26));
        }

        // Initialize checksum area to spaces
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }

        long checkSum = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(checkSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumSignedSumMatch() {
        byte[] header = new byte[512];
        // Introduce negative bytes so signedSum != unsignedSum
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) 0xFE; // -2 signed, 254 unsigned
        }
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }

        // Calculate signed sum
        long signedSum = 0;
        for (byte b : header) {
            signedSum += b;
        }

        // Format the positive signed sum into checksum field
        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue("Should verify when matching signed checksum", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumHeuristicGreaterStoredSum() {
        byte[] header = new byte[512];
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        // Set a huge valid octal checksum
        TarUtils.formatCheckSumOctalBytes(0777777L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        // Header bytes are mostly 0, so storedSum > unsignedSum triggers heuristic
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumMismatchReturnsFalse() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = 1;
        }
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        // Format a checksum that is much lower than the actual unsigned sum
        TarUtils.formatCheckSumOctalBytes(1L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumWithNonOctalTrailingDigits() {
        byte[] header = new byte[512];
        for (int i = TarConstants.CHKSUM_OFFSET; i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN; i++) {
            header[i] = ' ';
        }
        // Insert a non-octal digit after first sequence to cover `digits > 0 -> digits = 6` branch
        header[TarConstants.CHKSUM_OFFSET] = '1';
        header[TarConstants.CHKSUM_OFFSET + 1] = '2';
        header[TarConstants.CHKSUM_OFFSET + 2] = ' '; // triggers digits = 6
        header[TarConstants.CHKSUM_OFFSET + 3] = '3'; // ignored for storedSum

        // Should not throw, evaluates safely
        TarUtils.verifyCheckSum(header);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        assertArrayEquals(new byte[] { '0', '0', '0', '0' }, buf);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringMaxValue() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(0777L, buf, 0, 3);
        assertArrayEquals(new byte[] { '7', '7', '7' }, buf);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinarySmallOctal() {
        byte[] buffer = new byte[] { '0', '1', '2', ' ', 0 };
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(10L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryPositiveBinaryShortLength() {
        byte[] buffer = new byte[] { (byte) 0x80, 0x01, 0x02 };
        // Length 3 (< 9), positive binary: (1 << 8) + 2 = 258
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 3);
        assertEquals(258L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryNegativeBinaryShortLength() {
        byte[] buffer = new byte[] { (byte) 0xFF, (byte) 0xFF };
        // 2-byte negative: 0xFF, 0xFF -> -1
        long val = TarUtils.parseOctalOrBinary(buffer, 0, 2);
        assertEquals(-1L, val);

        byte[] buffer2 = new byte[] { (byte) 0xFF, (byte) 0xFE };
        long val2 = TarUtils.parseOctalOrBinary(buffer2, 0, 2);
        assertEquals(-2L, val2);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryRoundTripSmallNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-42L, buf, 0, 8);
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 8);
        assertEquals(-42L, parsed);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryPositiveBigInteger() {
        byte[] buf = new byte[12];
        long expected = TarConstants.MAXSIZE + 1024L;
        TarUtils.formatLongOctalOrBinaryBytes(expected, buf, 0, 12);
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(expected, parsed);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryNegativeBigInteger() {
        byte[] buf = new byte[12];
        long expected = -9876543210L;
        TarUtils.formatLongOctalOrBinaryBytes(expected, buf, 0, 12);
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(expected, parsed);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryLengthExactlyNine() {
        byte[] buf = new byte[9];
        long expected = 123456789012L;
        TarUtils.formatLongOctalOrBinaryBytes(expected, buf, 0, 9);
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 9);
        assertEquals(expected, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesUnderMaxId() {
        byte[] buf = new byte[TarConstants.UIDLEN];
        // UIDLEN = 8, MAXID = 07777777L
        int offset = TarUtils.formatLongOctalOrBinaryBytes(100L, buf, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, offset);
        // Stored as octal chars followed by space
        assertEquals(' ', buf[TarConstants.UIDLEN - 1]);
        assertEquals(100L, TarUtils.parseOctal(buf, 0, TarConstants.UIDLEN));
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOverMaxIdBinary() {
        byte[] buf = new byte[TarConstants.UIDLEN];
        long bigId = TarConstants.MAXID + 100L;
        int offset = TarUtils.formatLongOctalOrBinaryBytes(bigId, buf, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, offset);
        assertEquals((byte) 0x80, buf[0]); // Binary flag set
        long parsed = TarUtils.parseOctalOrBinary(buf, 0, TarConstants.UIDLEN);
        assertEquals(bigId, parsed);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * TarUtilsTest.testParseOctal -> java.lang.IllegalArgumentException: Invalid byte 32 at offset 1 in ' {NUL}' len=2
     *
     * When buffer contains leading space and trailing NUL without octal digits (e.g. {' ', 0}),
     * start is incremented to 1 (skipping leading space) and end is decremented to 1 (skipping trailing NUL).
     * In the defective code, start == end throws IllegalArgumentException instead of returning 0L.
     */
    @Test(timeout = 4000)
    public void testParseOctalSpaceFollowedByNullTriggersDefect() {
        byte[] buffer = new byte[] { ' ', 0 };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals("Buffer with ' ' and NUL should parse to 0L", 0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalMultipleSpacesAndNullTriggersDefect() {
        byte[] buffer = new byte[] { ' ', ' ', 0 };
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals("Buffer with spaces and trailing NUL should parse to 0L", 0L, value);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThanTwo() {
        TarUtils.parseOctal(new byte[] { '0' }, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteThrows() {
        byte[] buffer = new byte[] { '0', '9', ' ', 0 }; // '9' is not octal
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidByteExceptionMessage() {
        byte[] buffer = new byte[] { '1', 0, '8', ' ' };
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for invalid octal digit");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should contain '{NUL}' replacement", e.getMessage().contains("{NUL}"));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        // 0100 octal = 64 decimal, cannot fit in 2 characters
        TarUtils.formatUnsignedOctalString(64L, buf, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryValueTooLargeForField() {
        byte[] buf = new byte[3];
        // 3 byte field: bits = 16, max = 65536. Supplying negative with magnitude >= 65536
        TarUtils.formatLongOctalOrBinaryBytes(-70000L, buf, 0, 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryBigIntegerExceeds63Bits() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80; // Binary positive flag
        buffer[1] = 0x01;        // High byte set in 9-byte remainder -> > 63 bits
        TarUtils.parseOctalOrBinary(buffer, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryLongLengthGuardViaReflection() throws Throwable {
        Method method = TarUtils.class.getDeclaredMethod("parseBinaryLong", byte[].class, int.class, int.class, boolean.class);
        method.setAccessible(true);
        try {
            method.invoke(null, new byte[10], 0, 9, false);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Fallback Encoding Contracts
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingCanEncodeAndEncode() {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        assertTrue(fallback.canEncode("testName"));

        ByteBuffer buffer = fallback.encode("ABC");
        assertEquals(3, buffer.limit());
        assertEquals((byte) 'A', buffer.get(0));
        assertEquals((byte) 'B', buffer.get(1));
        assertEquals((byte) 'C', buffer.get(2));
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingDecode() throws IOException {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        byte[] bytes = new byte[] { 't', 'a', 'r', 0, 'e', 'x', 't', 'r', 'a' };
        String decoded = fallback.decode(bytes);
        assertEquals("tar", decoded);

        byte[] signExtended = new byte[] { (byte) 0xE9, 0 }; // é in ISO-8859-1
        String decodedSign = fallback.decode(signExtended);
        assertEquals(1, decodedSign.length());
        assertEquals(0xE9, (int) decodedSign.charAt(0));
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesWithExplicitEncoding() throws IOException {
        byte[] buf = new byte[10];
        int result = TarUtils.formatNameBytes("hello", buf, 0, 10, TarUtils.DEFAULT_ENCODING);
        assertEquals(10, result);
        assertEquals("hello", TarUtils.parseName(buf, 0, 10, TarUtils.DEFAULT_ENCODING));
    }
}