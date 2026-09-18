package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------
 * Target Method                 | Branch / Condition Targeted                       | Coverage Focus
 * -----------------------------------------------------------------------------------------------------
 * TarUtils()                    | Private constructor invocation via reflection      | Lifecycle & 100% Line
 * parseOctal                    | length < 2 defensive guard                        | Boundary / Exception
 * parseOctal                    | buffer[start] == 0 leading NUL work-around        | Compatibility Branch
 * parseOctal                    | leading spaces skip while-loop                    | Equivalence Partition
 * parseOctal                    | end-1 trailer check (space vs NUL vs invalid)     | Boundary / Exception
 * parseOctal                    | end-2 additional trailer check (space vs NUL)     | Branch Coverage
 * parseOctal                    | invalid octal byte ('/' < '0' or '8' > '7')       | Error Path / Exception
 * parseOctal                    | standard octal conversion accumulator             | Core Functional
 * parseOctalOrBinary            | (buffer[offset] & 0x80) == 0 (positive octal)     | Condition Branch
 * parseOctalOrBinary            | negative flag: buffer[offset] == (byte) 0xff      | Condition Branch
 * parseOctalOrBinary            | length < 9 -> parseBinaryLong                     | Boundary Branch
 * parseOctalOrBinary            | length >= 9 -> parseBinaryBigInteger              | Boundary Branch
 * parseBinaryBigInteger         | val.bitLength() > 63 check                        | Overflow Guard
 * parseBoolean                  | buffer[offset] == 1 vs other byte values          | Boolean Branch
 * parseName (3-arg / 4-arg)     | Trailing NUL trimming loop (len > 0 / len == 0)   | Boundary / Truncation
 * FALLBACK_ENCODING             | canEncode, encode, decode with trailing NUL/sign  | Inner Class Logic
 * formatNameBytes               | name length <= buffer length (padding NULs)       | Padding Loop
 * formatNameBytes               | name length > buffer length (truncation loop)     | Truncation While-Loop
 * formatUnsignedOctalString     | value == 0 special case                           | Branch Condition
 * formatUnsignedOctalString     | value != 0 and buffer overflow (val != 0)         | Buffer Overflow Guard
 * formatLongOctalOrBinaryBytes  | length == UIDLEN vs SIZELEN limits                | Ternary Branch
 * formatLongOctalOrBinaryBytes  | value <= maxAsOctalChar vs binary encoding        | Branch Condition
 * formatLongBinary              | Math.abs(value) >= max field size exception       | Buffer Overflow Guard
 * computeCheckSum               | BYTE_MASK & buf[i] summation                      | Accumulator Loop
 * verifyCheckSum                | storedSum == unsignedSum (standard tar header)    | Positive Verification
 * verifyCheckSum                | storedSum == signedSum (historic signed tar)      | Signed Sum Path
 * verifyCheckSum                | storedSum > unsignedSum (COMPRESS-177 heuristic)  | Special Heuristic
 * verifyCheckSum                | storedSum mismatch -> returns false               | Rejection Path
 * verifyCheckSum (DEFECT-197)   | 7-digit octal checksum (star tar implementation)  | Defects4J Failure Trigger
 * -----------------------------------------------------------------------------------------------------
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalStandard() {
        byte[] buffer = " 1750 \0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(01750L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithTrailingSpaceOnly() {
        byte[] buffer = "755 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithTrailingNullOnly() {
        byte[] buffer = "644\0".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0644L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingNullReturnsZero() {
        byte[] buffer = new byte[] { 0, '1', '2', ' ', 0 };
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpacesReturnsZero() {
        byte[] buffer = "   ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseBoolean() {
        byte[] buffer = new byte[] { 0, 1, 2, (byte) 0xFF };
        assertFalse(TarUtils.parseBoolean(buffer, 0));
        assertTrue(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
        assertFalse(TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testParseNameStandard() {
        byte[] buffer = "test-file.txt\0extra-bytes".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test-file.txt", name);
    }

    @Test(timeout = 4000)
    public void testParseNameEmptyBuffer() {
        byte[] buffer = new byte[16];
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesStandardAndPadding() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatNameBytes("hello", buffer, 0, 10);
        assertEquals(10, nextOffset);
        assertEquals('h', (char) buffer[0]);
        assertEquals('e', (char) buffer[1]);
        assertEquals('l', (char) buffer[2]);
        assertEquals('l', (char) buffer[3]);
        assertEquals('o', (char) buffer[4]);
        for (int i = 5; i < 10; i++) {
            assertEquals("Padding byte should be NUL", 0, buffer[i]);
        }
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncation() {
        byte[] buffer = new byte[4];
        int nextOffset = TarUtils.formatNameBytes("longfilename", buffer, 0, 4);
        assertEquals(4, nextOffset);
        assertEquals("long", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0123L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(' ', (char) buffer[6]);
        assertEquals(0, buffer[7]);
        long parsed = TarUtils.parseOctal(buffer, 0, 8);
        assertEquals(0123L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(' ', (char) buffer[7]);
        long parsed = TarUtils.parseOctal(buffer, 0, 8);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0644L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(0, buffer[6]);
        assertEquals(' ', (char) buffer[7]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buffer = new byte[] { 1, 2, 3, (byte) 255 };
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(1 + 2 + 3 + 255, sum);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertEquals("0000", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringPositive() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(7L, buffer, 0, 4);
        assertEquals("0007", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOctalPath() {
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(0777L, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, nextOffset);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(0777L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryLongPositive() {
        long value = TarConstants.MAXID + 1024L;
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, nextOffset);
        assertEquals((byte) 0x80, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryLongNegative() {
        long value = -12345L;
        byte[] buffer = new byte[TarConstants.UIDLEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, TarConstants.UIDLEN);
        assertEquals(TarConstants.UIDLEN, nextOffset);
        assertEquals((byte) 0xFF, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.UIDLEN);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBigIntegerPositive() {
        long value = TarConstants.MAXSIZE + 4096L;
        byte[] buffer = new byte[TarConstants.SIZELEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, TarConstants.SIZELEN);
        assertEquals(TarConstants.SIZELEN, nextOffset);
        assertEquals((byte) 0x80, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.SIZELEN);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBigIntegerNegative() {
        long value = -987654321L;
        byte[] buffer = new byte[TarConstants.SIZELEN];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(value, buffer, 0, TarConstants.SIZELEN);
        assertEquals(TarConstants.SIZELEN, nextOffset);
        assertEquals((byte) 0xFF, buffer[0]);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.SIZELEN);
        assertEquals(value, parsed);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryMaxSignedLong() {
        byte[] buffer = new byte[TarConstants.SIZELEN];
        TarUtils.formatLongOctalOrBinaryBytes(Long.MAX_VALUE, buffer, 0, TarConstants.SIZELEN);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, TarConstants.SIZELEN);
        assertEquals(Long.MAX_VALUE, parsed);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-197 & Checksum Rules)
    // =========================================================================

    /**
     * Targets COMPRESS-197: Star implementation tar headers store a 7-digit octal checksum
     * followed by a space or NUL. The defective parser limits parsing to 6 digits (digits++ < 6),
     * causing 7-digit checksums to be truncated/corrupted and verification to fail.
     */
    @Test(timeout = 4000)
    public void testVerifyCheckSumCompress197StarTarHeader() {
        byte[] header = new byte[512];
        header[0] = 'a';
        header[1] = 'r';
        header[2] = 'c';
        header[3] = 'h';
        header[4] = 'i';
        header[5] = 'v';
        header[6] = 'e';

        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                unsignedSum += ' ';
            } else {
                unsignedSum += (0xFF & header[i]);
            }
        }

        // Format as 7 octal digits + space (typical star tar format)
        String chk = String.format("%07o ", unsignedSum);
        byte[] chkBytes = chk.getBytes();
        System.arraycopy(chkBytes, 0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue("COMPRESS-197: 7-digit octal checksum from star must verify successfully",
                TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumCompress197StarTarHeaderTrailingNull() {
        byte[] header = new byte[512];
        for (int i = 0; i < 100; i++) {
            header[i] = (byte) ('A' + (i % 26));
        }

        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                unsignedSum += ' ';
            } else {
                unsignedSum += (0xFF & header[i]);
            }
        }

        // Format as 7 octal digits + NUL
        String chk = String.format("%07o\0", unsignedSum);
        byte[] chkBytes = chk.getBytes();
        System.arraycopy(chkBytes, 0, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue("COMPRESS-197: 7-digit octal checksum with NUL must verify successfully",
                TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumStandardUnsignedMatch() {
        byte[] header = new byte[512];
        header[0] = 'f';
        header[1] = 'i';
        header[2] = 'l';
        header[3] = 'e';

        long unsignedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                unsignedSum += ' ';
            } else {
                unsignedSum += (0xFF & header[i]);
            }
        }

        TarUtils.formatCheckSumOctalBytes(unsignedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue("Standard unsigned checksum match must return true", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumSignedMatch() {
        byte[] header = new byte[512];
        for (int i = 0; i < 50; i++) {
            header[i] = (byte) 0xFE; // negative signed byte (-2)
        }

        long signedSum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                signedSum += ' ';
            } else {
                signedSum += header[i];
            }
        }

        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue("Signed checksum match must return true", TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumCompress177Heuristic() {
        byte[] header = new byte[512];
        // Set stored checksum much higher than unsignedSum
        TarUtils.formatCheckSumOctalBytes(077777L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue("COMPRESS-177: storedSum > unsignedSum heuristic must return true",
                TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumCorruptedHeaderReturnsFalse() {
        byte[] header = new byte[512];
        for (int i = 0; i < 512; i++) {
            header[i] = (byte) 'Z';
        }
        // Write an explicitly low checksum that is <= unsignedSum and doesn't match signed or unsigned
        TarUtils.formatCheckSumOctalBytes(010L, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertFalse("Mismatched checksum must return false", TarUtils.verifyCheckSum(header));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThanTwoThrows() {
        byte[] buffer = new byte[] { '0' };
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalMissingTrailerThrows() {
        byte[] buffer = "1234".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteHighThrows() {
        byte[] buffer = "128 \0".getBytes(); // '8' is invalid octal
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteLowThrows() {
        byte[] buffer = "1/0 \0".getBytes(); // '/' is ASCII 47 (< '0')
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflowThrows() {
        byte[] buffer = new byte[2];
        // 64 requires 3 octal digits ("100"), cannot fit in length 2
        TarUtils.formatUnsignedOctalString(64L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryOverflowThrows() {
        byte[] buffer = new byte[2];
        // Negative value with abs(val) >= 256 for length 2 field (1 byte payload)
        TarUtils.formatLongOctalOrBinaryBytes(-256L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryBigIntegerExceeds63BitsThrows() {
        byte[] buffer = new byte[11];
        buffer[0] = (byte) 0x80;
        buffer[1] = 0x7F; // Positive BigInteger with 10 remaining bytes (> 63 bits)
        for (int i = 2; i < 11; i++) {
            buffer[i] = (byte) 0xFF;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, 11);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Internal Encodings
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        try {
            TarUtils instance = constructor.newInstance();
            assertNotNull(instance);
        } catch (InvocationTargetException e) {
            fail("Constructor threw an exception: " + e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingLogic() throws IOException {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        assertTrue(fallback.canEncode("test-name"));

        ByteBuffer buf = fallback.encode("ABC");
        assertEquals(3, buf.limit());
        assertEquals((byte) 'A', buf.get(0));
        assertEquals((byte) 'B', buf.get(1));
        assertEquals((byte) 'C', buf.get(2));

        byte[] decodeInput = new byte[] { (byte) 'H', (byte) 'i', 0, (byte) 'X' };
        String decoded = fallback.decode(decodeInput);
        assertEquals("Hi", decoded);

        byte[] signExtendInput = new byte[] { (byte) 0xFE, 0 };
        String decodedSign = fallback.decode(signExtendInput);
        assertEquals(1, decodedSign.length());
        assertEquals((char) 0xFE, decodedSign.charAt(0));
    }

    @Test(timeout = 4000)
    public void testParseAndFormatNameWithZipEncoding() throws IOException {
        ZipEncoding encoding = ZipEncodingHelper.getZipEncoding("UTF-8");
        byte[] buffer = new byte[20];
        int next = TarUtils.formatNameBytes("sample-name", buffer, 0, 20, encoding);
        assertEquals(20, next);

        String parsed = TarUtils.parseName(buffer, 0, 20, encoding);
        assertEquals("sample-name", parsed);
    }
}