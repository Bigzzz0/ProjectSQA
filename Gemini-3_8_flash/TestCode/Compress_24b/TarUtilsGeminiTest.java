package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects & Branches:
 * 1. Defect COMPRESS-33 / Defects4J Target:
 *    - parseOctal fails when all bytes are octal digits (e.g. 12-byte '777777777777' without
 *      trailing space or NUL).
 *    - Method: testParseOctalDefectMaxOctalWithoutTrailingSpaceOrNul()
 * 2. parseOctal:
 *    - length < 2 -> IllegalArgumentException
 *    - buffer[start] == 0 -> returns 0L immediately
 *    - leading spaces skipped
 *    - trailing space / trailing NUL trimming
 *    - invalid bytes (< '0' or > '7') -> IllegalArgumentException with formatted exceptionMessage
 * 3. parseOctalOrBinary & parseBinaryLong & parseBinaryBigInteger:
 *    - Most significant bit not set ((b[offset] & 0x80) == 0) -> parseOctal delegation
 *    - MSB set, positive binary (offset byte == 0x80), length < 9 -> parseBinaryLong
 *    - MSB set, negative binary (offset byte == 0xff), length < 9 -> parseBinaryLong 2's complement
 *    - length >= 9 -> parseBinaryBigInteger (both positive 0x80 and negative 0xff)
 *    - BigInteger bitLength > 63 -> IllegalArgumentException
 *    - parseBinaryLong with length >= 9 direct call guard -> IllegalArgumentException
 * 4. parseBoolean:
 *    - buffer[offset] == 1 -> true; buffer[offset] != 1 -> false
 * 5. parseName & formatNameBytes:
 *    - DEFAULT_ENCODING, custom ZipEncoding, and package-private FALLBACK_ENCODING paths
 *    - Truncation when name length exceeds target length
 *    - Padding with NULs when name is shorter than buffer
 *    - Name with embedded/trailing NULs
 * 6. formatUnsignedOctalString:
 *    - value == 0 -> fills with '0'
 *    - value > 0 -> octal conversion and zero-padding
 *    - value overflow -> IllegalArgumentException
 * 7. formatOctalBytes, formatLongOctalBytes, formatCheckSumOctalBytes:
 *    - Space & NUL trailers, offset + length returned value
 * 8. formatLongOctalOrBinaryBytes:
 *    - Within octal range (<= maxAsOctalChar) -> octal format
 *    - Exceeds octal range, positive & negative -> binary formatting (length < 9 vs >= 9)
 *    - Value overflow for binary representation -> IllegalArgumentException
 * 9. computeCheckSum & verifyCheckSum:
 *    - Standard unsigned sum matching
 *    - Signed sum matching for historical tar formats
 *    - COMPRESS-177 heuristic (storedSum > unsignedSum)
 *    - Checksum digit parsing termination when non-digit encountered
 *    - Mismatch returning false
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the Defects4J defect in TarUtils.parseOctal where a buffer filled
     * with 12 octal digits without a trailing space or NUL triggers
     * IllegalArgumentException: "Invalid byte 55 at offset 11 in '777777777777' len=12".
     *
     * In tar specifications, large sizes/UIDs/GIDs can use the full 12 bytes without
     * a trailing space or NUL.
     */
    @Test(timeout = 4000)
    public void testParseOctalDefectMaxOctalWithoutTrailingSpaceOrNul() {
        byte[] buffer = "777777777777".getBytes(StandardCharsets.US_ASCII);
        long expected = 0777777777777L; // 68719476735L
        long actual = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalStandardValues() {
        byte[] buffer = " 0755 \0".getBytes(StandardCharsets.US_ASCII);
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, value);

        byte[] buf2 = "00000000123 \0".getBytes(StandardCharsets.US_ASCII);
        assertEquals(0123L, TarUtils.parseOctal(buf2, 0, buf2.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNulOrLeadingNul() {
        byte[] buffer = new byte[10];
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));

        byte[] bufferLeadingNul = new byte[]{0, '7', '5', '5', ' '};
        assertEquals(0L, TarUtils.parseOctal(bufferLeadingNul, 0, bufferLeadingNul.length));
    }

    @Test(timeout = 4000)
    public void testParseBoolean() {
        byte[] buffer = new byte[]{0, 1, 2, -1};
        assertFalse(TarUtils.parseBoolean(buffer, 0));
        assertTrue(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
        assertFalse(TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testParseNameAndFormatNameRoundTrip() throws IOException {
        String testName = "dir/subdir/file.txt";
        byte[] buf = new byte[50];
        int nextOffset = TarUtils.formatNameBytes(testName, buf, 0, 50);
        assertEquals(50, nextOffset);

        String parsedName = TarUtils.parseName(buf, 0, 50);
        assertEquals(testName, parsedName);

        // Parsing empty name when first byte is NUL
        byte[] emptyBuf = new byte[10];
        assertEquals("", TarUtils.parseName(emptyBuf, 0, 10));
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncation() {
        String longName = "this_is_a_very_long_file_name_that_exceeds_buffer";
        byte[] buf = new byte[10];
        int nextOffset = TarUtils.formatNameBytes(longName, buf, 0, 10);
        assertEquals(10, nextOffset);
        assertEquals("this_is_a_", TarUtils.parseName(buf, 0, 10));
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) ' ', buf[10]);
        assertEquals((byte) 0, buf[11]);
        assertEquals(0755L, TarUtils.parseOctal(buf, 0, 12));
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) ' ', buf[11]);
        assertEquals(0755L, TarUtils.parseOctal(buf, 0, 12));
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(01234L, buf, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0, buf[6]);
        assertEquals((byte) ' ', buf[7]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buf = new byte[]{1, 2, 3, (byte) 255};
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(1 + 2 + 3 + 255, sum);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Binary / Large Numbers
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryOctalFallback() {
        byte[] buf = " 0755 \0".getBytes(StandardCharsets.US_ASCII);
        long val = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(0755L, val);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseLongOctalOrBinarySmallPositiveBinary() {
        byte[] buf = new byte[8];
        long val = 0x12345678L;
        // UID length is 8; TarConstants.MAXID is 07777777L = 2097151L.
        // val exceeds MAXID, so it will be formatted as binary.
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0x80, buf[0]);

        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 8);
        assertEquals(val, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseLongOctalOrBinaryNegativeBinarySmall() {
        byte[] buf = new byte[8];
        long val = -12345L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0xff, buf[0]);

        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 8);
        assertEquals(val, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseLongOctalOrBinaryPositiveBigInteger() {
        byte[] buf = new byte[12];
        long val = 0x1000000000L; // exceeds max size 077777777777L (8589934591L)
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) 0x80, buf[0]);

        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(val, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatAndParseLongOctalOrBinaryNegativeBigInteger() {
        byte[] buf = new byte[12];
        long val = -9876543210L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) 0xff, buf[0]);

        long parsed = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(val, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesFitsInOctal() {
        byte[] buf = new byte[12];
        long val = 0755L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) ' ', buf[11]);
        assertEquals(val, TarUtils.parseOctal(buf, 0, 12));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, 4);
        assertArrayEquals(new byte[]{'0', '0', '0', '0'}, buf);
    }

    // =========================================================================
    // Partition D: Defensive Guard Paths & Exception Handling
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buf = new byte[]{ '0' };
        TarUtils.parseOctal(buf, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalMissingTrailingSpaceOrNul() {
        byte[] buf = "012345".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteInMiddle() {
        byte[] buf = "012A45 \0".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        // 0100 (octal) requires at least 3 digits
        TarUtils.formatUnsignedOctalString(64L, buf, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryTooLarge() {
        byte[] buf = new byte[2];
        // 2 byte field has 1 byte for value = max 256. 1000 will overflow.
        TarUtils.formatLongOctalOrBinaryBytes(1000L, buf, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryBigIntegerExceedsLongMax() {
        byte[] buf = new byte[12];
        buf[0] = (byte) 0x80;
        // Set most significant bits in remainder to overflow signed 63-bit integer
        Arrays.fill(buf, 1, 12, (byte) 0xFF);
        TarUtils.parseOctalOrBinary(buf, 0, 12);
    }

    // =========================================================================
    // Partition E: Checksum Verification & Encodings
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerifyCheckSumValid() {
        byte[] header = new byte[512];
        // Initialize some dummy data in header
        for (int i = 0; i < 148; i++) {
            header[i] = (byte) (i & 0x7F);
        }
        for (int i = 156; i < 512; i++) {
            header[i] = (byte) (i & 0x7F);
        }

        // Compute checksum considering 8 space characters at [148, 156)
        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                unsignedSum += ' ';
            } else {
                unsignedSum += (header[i] & 0xFF);
            }
        }

        TarUtils.formatCheckSumOctalBytes(unsignedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumSignedSumMatch() {
        byte[] header = new byte[512];
        // Inject signed negative bytes
        header[0] = (byte) 0xFE;
        header[1] = (byte) 0x80;

        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                signedSum += ' ';
            } else {
                signedSum += header[i];
            }
        }

        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumCompress177Heuristic() {
        byte[] header = new byte[512];
        // Set checksum to a higher value than unsigned sum to trigger storedSum > unsignedSum
        long highVal = 077777L;
        TarUtils.formatCheckSumOctalBytes(highVal, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumInvalid() {
        byte[] header = new byte[512];
        Arrays.fill(header, (byte) 'A');
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumWithNonDigitAfterDigits() {
        byte[] header = new byte[512];
        // Checksum field starts with octal digits and has a non-digit like 'A'
        header[TarConstants.CHKSUM_OFFSET] = '1';
        header[TarConstants.CHKSUM_OFFSET + 1] = '2';
        header[TarConstants.CHKSUM_OFFSET + 2] = 'A';
        header[TarConstants.CHKSUM_OFFSET + 3] = '3';
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingAndCustomEncoding() throws IOException {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        assertTrue(fallback.canEncode("anyName"));

        ByteBuffer encoded = fallback.encode("testFile");
        assertEquals(8, encoded.limit());

        byte[] rawBytes = new byte[]{'t', 'e', 's', 't', 0, 'e', 'x', 't'};
        String decoded = fallback.decode(rawBytes);
        assertEquals("test", decoded);

        ZipEncoding customEncoding = ZipEncodingHelper.getZipEncoding("ISO-8859-1");
        byte[] buf = new byte[20];
        TarUtils.formatNameBytes("hello", buf, 0, 20, customEncoding);
        String name = TarUtils.parseName(buf, 0, 20, customEncoding);
        assertEquals("hello", name);
    }
}