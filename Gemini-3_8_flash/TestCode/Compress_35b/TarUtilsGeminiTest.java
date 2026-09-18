package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.apache.commons.compress.archivers.zip.ZipEncoding;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: TarUtils
 * Defect Reference: Defects4J COMPRESS-335 / DetectArchiverTestCase::testCOMPRESS335
 *
 * Core Decision Branches & Boundary Conditions Targeted:
 * 1. parseOctal:
 *    - length < 2: IllegalArgumentException boundary guard.
 *    - buffer[start] == 0: early return 0L (leading NUL / missing fields).
 *    - Leading space trimming: while buffer[start] == ' '.
 *    - Trailing NUL/space trimming: while trailer == 0 || trailer == ' '.
 *    - Character range check: currentByte < '0' || currentByte > '7' -> IllegalArgumentException.
 *    - exceptionMessage: proper replacement of '\0' with '{NUL}' in exception formatting.
 *
 * 2. parseOctalOrBinary:
 *    - buffer[offset] & 0x80 == 0: octal mode delegation.
 *    - buffer[offset] & 0x80 != 0: binary mode.
 *    - negative flag: buffer[offset] == (byte) 0xff vs (byte) 0x80.
 *    - length < 9: parseBinaryLong vs parseBinaryBigInteger.
 *    - parseBinaryLong with length >= 9: IllegalArgumentException.
 *    - parseBinaryBigInteger with bitLength() > 63: IllegalArgumentException ("exceeds maximum signed long").
 *    - Negative 2's complement calculation for both long and BigInteger.
 *
 * 3. parseBoolean:
 *    - buffer[offset] == 1 -> true.
 *    - buffer[offset] != 1 (0, 2, negative) -> false.
 *
 * 4. parseName & formatNameBytes:
 *    - Trailing NUL trimming logic.
 *    - Full length string with no NUL terminator.
 *    - Empty name (all NULs).
 *    - Default and custom ZipEncoding paths.
 *    - Name truncation when name length exceeds buffer length.
 *    - NUL padding when name length is shorter than buffer length.
 *    - Fallback encoding execution (canEncode, encode, decode with trailing NUL & sign extension).
 *
 * 5. formatUnsignedOctalString:
 *    - value == 0: single '0' with leading zeroes.
 *    - value > 0: bit-shifting and ASCII conversion.
 *    - value overflow (val != 0 when remaining < 0): IllegalArgumentException.
 *
 * 6. formatOctalBytes & formatLongOctalBytes & formatCheckSumOctalBytes:
 *    - Space and NUL placement according to Tar specifications.
 *    - Length boundaries.
 *
 * 7. formatLongOctalOrBinaryBytes:
 *    - value <= maxAsOctalChar (UIDLEN/MAXID vs SIZE/MAXSIZE) -> octal format.
 *    - value > maxAsOctalChar -> binary format.
 *    - negative values (< 0) -> binary format with 0xff prefix.
 *    - formatLongBinary (length < 9) overflow guard: val >= (1L << bits) -> IllegalArgumentException.
 *    - formatBigIntegerBinary (length >= 9) filling with 0x00 or 0xff.
 *
 * 8. computeCheckSum:
 *    - Unsigned byte addition (BYTE_MASK & element).
 *
 * 9. verifyCheckSum (COMPRESS-335 Defect Target):
 *    - Checksum digits < 6, digits == 6, and digits == 7 (BusyBox/Solaris 7-digit octal checksum).
 *    - Unsigned sum vs signed sum match (historic signed byte implementations).
 *    - Invalid checksum heuristics returning false.
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalStandard() {
        byte[] buffer = " 0123456 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123456L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalOffset() {
        byte[] buffer = "XX017777 \0YY".getBytes();
        long value = TarUtils.parseOctal(buffer, 2, 9);
        assertEquals(017777L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalZeroValue() {
        byte[] buffer = "0000000 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingNulReturnsZero() {
        byte[] buffer = new byte[10];
        buffer[0] = 0;
        buffer[1] = '7';
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
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
    public void testParseNameStandard() {
        byte[] buffer = "tar-entry-name\0ignored-bytes".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("tar-entry-name", name);
    }

    @Test(timeout = 4000)
    public void testParseNameEmpty() {
        byte[] buffer = new byte[16];
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", name);
    }

    @Test(timeout = 4000)
    public void testParseNameNoNul() {
        byte[] buffer = "no-null-terminator".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("no-null-terminator", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesStandard() {
        byte[] buffer = new byte[16];
        int nextOffset = TarUtils.formatNameBytes("hello", buffer, 0, 10);
        assertEquals(10, nextOffset);
        assertEquals("hello\0\0\0\0\0", new String(buffer, 0, 10));
        assertEquals(0, buffer[10]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncation() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("verylongentryname", buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals("verylong", new String(buffer, 0, 8));
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[12];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(' ', buffer[6]);
        assertEquals(0, buffer[7]);
        long parsed = TarUtils.parseOctal(buffer, 0, 8);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatLongOctalBytes(0644L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(' ', buffer[7]);
        long parsed = TarUtils.parseOctal(buffer, 0, 8);
        assertEquals(0644L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[10];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(01234L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(0, buffer[6]);
        assertEquals((byte) ' ', buffer[7]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] data = new byte[]{1, 2, 3, (byte) 255};
        long sum = TarUtils.computeCheckSum(data);
        assertEquals(1 + 2 + 3 + 255L, sum);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertArrayEquals(new byte[]{'0', '0', '0', '0'}, buffer);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringMaxFit() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(0777L, buffer, 0, 3);
        assertArrayEquals(new byte[]{'7', '7', '7'}, buffer);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpacesReturnsZero() {
        byte[] buffer = "    ".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalMinLength2() {
        byte[] buffer = "0 ".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, 2);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBoundaries() {
        byte[] buf8 = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID, buf8, 0, 8);
        assertEquals(TarConstants.MAXID, TarUtils.parseOctalOrBinary(buf8, 0, 8));

        TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXID + 1L, buf8, 0, 8);
        assertEquals(TarConstants.MAXID + 1L, TarUtils.parseOctalOrBinary(buf8, 0, 8));

        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf8, 0, 8);
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buf8, 0, 8));

        TarUtils.formatLongOctalOrBinaryBytes(-12345L, buf8, 0, 8);
        assertEquals(-12345L, TarUtils.parseOctalOrBinary(buf8, 0, 8));

        byte[] buf12 = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXSIZE, buf12, 0, 12);
        assertEquals(TarConstants.MAXSIZE, TarUtils.parseOctalOrBinary(buf12, 0, 12));

        TarUtils.formatLongOctalOrBinaryBytes(TarConstants.MAXSIZE + 100L, buf12, 0, 12);
        assertEquals(TarConstants.MAXSIZE + 100L, TarUtils.parseOctalOrBinary(buf12, 0, 12));

        TarUtils.formatLongOctalOrBinaryBytes(-999999L, buf12, 0, 12);
        assertEquals(-999999L, TarUtils.parseOctalOrBinary(buf12, 0, 12));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumSignedDifference() {
        byte[] header = new byte[512];
        header[0] = (byte) 0xFF; // Signed -1, unsigned 255
        long signedSum = 0;
        long unsignedSum = 0;

        for (int i = 0; i < 512; i++) {
            byte b = header[i];
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                b = ' ';
            }
            unsignedSum += (0xFF & b);
            signedSum += b;
        }

        TarUtils.formatCheckSumOctalBytes(unsignedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue("Checksum matching unsigned sum should verify", TarUtils.verifyCheckSum(header));

        Arrays.fill(header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN, (byte) 0);
        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue("Checksum matching signed sum should verify", TarUtils.verifyCheckSum(header));

        header[TarConstants.CHKSUM_OFFSET] = '7';
        header[TarConstants.CHKSUM_OFFSET + 1] = '7';
        assertFalse("Corrupted checksum must not verify", TarUtils.verifyCheckSum(header));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-335)
    // =========================================================================

    /**
     * Targets COMPRESS-335 / DetectArchiverTestCase::testCOMPRESS335:
     * Valid tar headers generated by tools such as BusyBox or Solaris tar format
     * the checksum as a 7-digit octal number followed by NUL or space.
     * The method verifyCheckSum must correctly extract and verify 7-digit checksums.
     */
    @Test(timeout = 4000)
    public void testCOMPRESS335_VerifyCheckSum7DigitOctalWithNul() {
        byte[] header = new byte[512];
        for (int i = 0; i < 100; i++) {
            header[i] = (byte) ('A' + (i % 26));
        }

        long sum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                sum += ' ';
            } else {
                sum += (header[i] & 0xFF);
            }
        }

        // Format exactly as 7-digit octal with trailing NUL
        String checksumStr = String.format("%07o", sum);
        for (int i = 0; i < 7; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) checksumStr.charAt(i);
        }
        header[TarConstants.CHKSUM_OFFSET + 7] = 0; // Trailing NUL

        assertTrue("Tar header with 7-digit octal checksum must be recognized (COMPRESS-335)",
                   TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testCOMPRESS335_VerifyCheckSum7DigitOctalWithSpace() {
        byte[] header = new byte[512];
        for (int i = 0; i < 150; i++) {
            header[i] = (byte) (i & 0x7F);
        }

        long sum = 0;
        for (int i = 0; i < 512; i++) {
            if (i >= TarConstants.CHKSUM_OFFSET && i < TarConstants.CHKSUM_OFFSET + TarConstants.CHKSUMLEN) {
                sum += ' ';
            } else {
                sum += (header[i] & 0xFF);
            }
        }

        // Format exactly as 7-digit octal with trailing space
        String checksumStr = String.format("%07o", sum);
        for (int i = 0; i < 7; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = (byte) checksumStr.charAt(i);
        }
        header[TarConstants.CHKSUM_OFFSET + 7] = (byte) ' '; // Trailing Space

        assertTrue("Tar header with 7-digit octal checksum and trailing space must be recognized",
                   TarUtils.verifyCheckSum(header));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthTooShortThrows() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteThrows() {
        byte[] buffer = " 01824 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(timeout = 4000)
    public void testParseOctalExceptionMessageFormatting() {
        byte[] buffer = new byte[]{'1', 0, '9', ' '};
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException on invalid octal byte");
        } catch (IllegalArgumentException ex) {
            assertTrue("Exception message should contain {NUL} replacement",
                       ex.getMessage().contains("{NUL}"));
            assertTrue("Exception message should identify invalid byte",
                       ex.getMessage().contains("Invalid byte 57"));
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflowThrows() {
        byte[] buffer = new byte[2];
        TarUtils.formatUnsignedOctalString(0100L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryOverflowThrows() {
        byte[] buffer = new byte[2];
        // For length=2, bits = 8, max = 256. Value 300 must throw.
        TarUtils.formatLongOctalOrBinaryBytes(300L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryBigIntegerExceedsMaxSignedLongThrows() {
        byte[] buffer = new byte[12];
        buffer[0] = (byte) 0x80; // Binary flag
        Arrays.fill(buffer, 1, 12, (byte) 0xFF); // 11 bytes of 0xFF = 88 bits > 63 bits
        TarUtils.parseOctalOrBinary(buffer, 0, 12);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Encodings & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFallbackEncodingContract() {
        ZipEncoding fallback = TarUtils.FALLBACK_ENCODING;
        assertTrue(fallback.canEncode("test-name"));

        ByteBuffer encoded = fallback.encode("ABC\u0080");
        assertEquals(4, encoded.remaining());
        assertEquals((byte) 'A', encoded.get());
        assertEquals((byte) 'B', encoded.get());
        assertEquals((byte) 'C', encoded.get());
        assertEquals((byte) 0x80, encoded.get());

        byte[] raw = new byte[]{'X', 'Y', 0, 'Z'};
        String decoded = fallback.decode(raw);
        assertEquals("XY", decoded);

        byte[] signExtend = new byte[]{(byte) 0xFE};
        String decodedSign = fallback.decode(signExtend);
        assertEquals(1, decodedSign.length());
        assertEquals(0xFE, decodedSign.charAt(0));
    }

    @Test(timeout = 4000)
    public void testParseNameWithZipEncoding() throws IOException {
        ZipEncoding customEncoding = new ZipEncoding() {
            public boolean canEncode(String name) { return true; }
            public ByteBuffer encode(String name) { return ByteBuffer.wrap(name.getBytes()); }
            public String decode(byte[] buffer) { return "custom:" + new String(buffer); }
        };

        byte[] buffer = "sample-entry\0\0".getBytes();
        String result = TarUtils.parseName(buffer, 0, buffer.length, customEncoding);
        assertEquals("custom:sample-entry", result);

        byte[] emptyBuffer = new byte[5];
        String emptyResult = TarUtils.parseName(emptyBuffer, 0, emptyBuffer.length, customEncoding);
        assertEquals("", emptyResult);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesWithZipEncoding() throws IOException {
        ZipEncoding customEncoding = TarUtils.DEFAULT_ENCODING;
        byte[] buffer = new byte[10];
        int offset = TarUtils.formatNameBytes("abcdef", buffer, 0, 8, customEncoding);
        assertEquals(8, offset);
        assertEquals("abcdef\0\0", new String(buffer, 0, 8));
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertFalse("Constructor should be private", constructor.isAccessible());
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull("Utility class should be instantiable via reflection", instance);
    }
}