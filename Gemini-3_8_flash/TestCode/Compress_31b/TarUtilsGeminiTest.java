package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.archivers.tar.TarUtils
 *
 * Partition A: Core Functional Logic & State Transitions
 * - parseOctal: Valid standard octal inputs, spaces, trailing NULs, leading spaces.
 * - parseOctalOrBinary: Positive long binary (< 9 bytes), positive BigInteger (>= 9 bytes),
 *   negative long binary (< 9 bytes), negative BigInteger (>= 9 bytes).
 * - parseBoolean: True (1) and False (0, other bytes).
 * - parseName & formatNameBytes: Exact fit, truncations, null-padded buffers, ZipEncoding integration.
 * - formatUnsignedOctalString, formatOctalBytes, formatLongOctalBytes, formatCheckSumOctalBytes.
 * - computeCheckSum & verifyCheckSum: Standard checksum calculation, signed-vs-unsigned matches,
 *   heuristic COMPRESS-177 storedSum > unsignedSum path, checksum mismatch.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - parseOctal: Length boundaries (length < 2), buffer starting with NUL (missing field).
 * - parseOctalOrBinary: Boundary lengths (8, 9, 12 bytes), Long.MAX_VALUE, Long.MIN_VALUE, 0.
 * - formatLongBinary: Max value boundaries for field length.
 * - BigInteger binary: Bit-length overflow boundary (> 63 bits).
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - COMPRESS-178 / testParseOctalInvalid: Embedded NUL byte within octal stream followed by
 *   digits must throw IllegalArgumentException rather than terminating early and silently returning.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - parseOctal: Non-octal characters ('8', '9', 'A', space in middle), length < 2.
 * - formatUnsignedOctalString: Value too large for target buffer.
 * - formatLongBinary: Absolute value too large for target field.
 * - parseBinaryBigInteger / parseBinaryLong: Negative and positive overflow > 63 bits.
 * - TarUtils constructor: Private constructor defensive reflection verification.
 *
 * Partition E: ZipEncoding & Fallback Encoding Contract Integrity
 * - TarUtils.FALLBACK_ENCODING: canEncode, encode, decode with trailing null, decode sign extension.
 * - TarUtils.DEFAULT_ENCODING: Standard string encoding/decoding.
 */
public class TarUtilsGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseOctalBasic() {
        byte[] buffer = "0755 \0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithLeadingSpacesAndTrailingNul() {
        byte[] buffer = "   123\0".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0123L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNulsReturnsZero() {
        byte[] buffer = new byte[10];
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpacesReturnsZero() {
        byte[] buffer = "          ".getBytes();
        long result = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseBoolean() {
        byte[] buffer = new byte[] { 1, 0, 2, -1 };
        assertTrue(TarUtils.parseBoolean(buffer, 0));
        assertFalse(TarUtils.parseBoolean(buffer, 1));
        assertFalse(TarUtils.parseBoolean(buffer, 2));
        assertFalse(TarUtils.parseBoolean(buffer, 3));
    }

    @Test(timeout = 4000)
    public void testParseNameAndFormatNameDefault() {
        byte[] buffer = new byte[10];
        int offset = TarUtils.formatNameBytes("foo", buffer, 0, buffer.length);
        assertEquals(10, offset);

        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("foo", parsed);
    }

    @Test(timeout = 4000)
    public void testParseNameEmptyBuffer() {
        byte[] buffer = new byte[8];
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("", parsed);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncation() {
        byte[] buffer = new byte[4];
        int offset = TarUtils.formatNameBytes("toolongname", buffer, 0, buffer.length);
        assertEquals(4, offset);

        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("tool", parsed);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesWithOffset() {
        byte[] buffer = new byte[20];
        int nextOffset = TarUtils.formatNameBytes("hello", buffer, 5, 10);
        assertEquals(15, nextOffset);

        String parsed = TarUtils.parseName(buffer, 5, 10);
        assertEquals("hello", parsed);
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0755, buffer, 0, buffer.length);
        assertEquals(8, nextOffset);
        assertEquals(' ', buffer[buffer.length - 2]);
        assertEquals(0, buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755, buffer, 0, buffer.length);
        assertEquals(8, nextOffset);
        assertEquals(' ', buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(01234, buffer, 0, buffer.length);
        assertEquals(8, nextOffset);
        assertEquals(0, buffer[buffer.length - 2]);
        assertEquals(' ', buffer[buffer.length - 1]);

        long parsed = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(01234L, parsed);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buffer = new byte[] { 10, 20, (byte) 200 };
        // 10 + 20 + 200 = 230
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(230L, sum);
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumNormal() {
        byte[] header = new byte[512];
        header[0] = 'a';
        header[1] = 'b';
        // Set checksum field to spaces to simulate standard tar header preparation
        for (int i = 0; i < TarConstants.CHKSUMLEN; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = ' ';
        }
        long cs = TarUtils.computeCheckSum(header);
        TarUtils.formatCheckSumOctalBytes(cs, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);

        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumFailure() {
        byte[] header = new byte[512];
        header[0] = 'a';
        TarUtils.formatCheckSumOctalBytes(1234, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumHistoricSignedSumMatch() {
        byte[] header = new byte[512];
        header[0] = (byte) -10; // Negative signed byte
        for (int i = 0; i < TarConstants.CHKSUMLEN; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = ' ';
        }
        // Calculate signed sum
        long signedSum = 0;
        for (byte b : header) {
            signedSum += b;
        }
        TarUtils.formatCheckSumOctalBytes(signedSum, header, TarConstants.CHKSUM_OFFSET, TarConstants.CHKSUMLEN);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumStoredSumGreaterThanUnsignedSum() {
        // COMPRESS-177 heuristic check: storedSum > unsignedSum
        byte[] header = new byte[512];
        for (int i = 0; i < 6; i++) {
            header[TarConstants.CHKSUM_OFFSET + i] = '7';
        }
        header[TarConstants.CHKSUM_OFFSET + 6] = 0;
        header[TarConstants.CHKSUM_OFFSET + 7] = ' ';
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test(timeout = 4000)
    public void testVerifyCheckSumDigitsBreakSequence() {
        byte[] header = new byte[512];
        header[TarConstants.CHKSUM_OFFSET] = '1';
        header[TarConstants.CHKSUM_OFFSET + 1] = '2';
        header[TarConstants.CHKSUM_OFFSET + 2] = ' ';
        header[TarConstants.CHKSUM_OFFSET + 3] = '3';
        header[TarConstants.CHKSUM_OFFSET + 4] = '4';
        header[TarConstants.CHKSUM_OFFSET + 5] = '5';
        header[TarConstants.CHKSUM_OFFSET + 6] = 0;
        header[TarConstants.CHKSUM_OFFSET + 7] = ' ';
        // Tests the branch: digits > 0 && !(octal digit && digits++ < 6) -> digits = 6
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesWithinOctalLimit() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(07777777L, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(' ', buffer[7]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(07777777L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesSmallBinaryPositive() {
        byte[] buffer = new byte[8];
        long largeVal = 07777777L + 1L; // Exceeds TarConstants.MAXID
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(largeVal, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0x80, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(largeVal, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesSmallBinaryNegative() {
        byte[] buffer = new byte[8];
        long negativeVal = -12345L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(negativeVal, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals((byte) 0xff, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(negativeVal, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBigIntegerPositive() {
        byte[] buffer = new byte[12];
        long hugeVal = TarConstants.MAXSIZE + 1024L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(hugeVal, buffer, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) 0x80, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        assertEquals(hugeVal, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBigIntegerNegative() {
        byte[] buffer = new byte[12];
        long negativeVal = -9876543210L;
        int nextOffset = TarUtils.formatLongOctalOrBinaryBytes(negativeVal, buffer, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals((byte) 0xff, buffer[0]);

        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 12);
        assertEquals(negativeVal, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesZero() {
        byte[] buffer = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0, buffer, 0, 8);
        long parsed = TarUtils.parseOctalOrBinary(buffer, 0, 8);
        assertEquals(0L, parsed);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-178 / testParseOctalInvalid)
    // -------------------------------------------------------------------------

    /**
     * Targets COMPRESS-178: In defective code, parseOctal encounters an embedded NUL
     * byte before the trimmed end and breaks the parsing loop early without throwing
     * IllegalArgumentException. Correct behavior demands IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidEmbeddedNul() {
        byte[] buffer = new byte[10];
        TarUtils.formatLongOctalOrBinaryBytes(0, buffer, 0, 10);
        buffer[2] = 0; // Insert embedded NUL in place of an octal digit
        TarUtils.parseOctal(buffer, 0, 10);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buffer = new byte[1];
        TarUtils.parseOctal(buffer, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteHigherThanSeven() {
        byte[] buffer = "0785 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalInvalidByteLowerThanZero() {
        byte[] buffer = "07-5 \0".getBytes();
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalEmbeddedSpace() {
        byte[] buffer = new byte[10];
        TarUtils.formatLongOctalOrBinaryBytes(0, buffer, 0, 10);
        buffer[2] = ' ';
        TarUtils.parseOctal(buffer, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        // 01000 in octal requires 4 chars, which will not fit in 2 bytes
        TarUtils.formatUnsignedOctalString(01000L, buffer, 0, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testFormatLongBinaryValueTooLarge() {
        byte[] buffer = new byte[4];
        // 4 bytes binary allows (4-1)*8 = 24 bits (max 16777216)
        TarUtils.formatLongOctalOrBinaryBytes(100000000L, buffer, 0, 4);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalOrBinaryPositiveBigIntegerExceeds63Bits() {
        byte[] buffer = new byte[10];
        buffer[0] = (byte) 0x80; // Positive binary marker
        buffer[1] = 0x01; // Leads to 9 remaining bytes with high magnitude > 63 bits
        for (int i = 2; i < 10; i++) {
            buffer[i] = (byte) 0xff;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseOctalOrBinaryNegativeBigIntegerExceeds63Bits() {
        byte[] buffer = new byte[11];
        buffer[0] = (byte) 0xff; // Negative binary marker
        buffer[1] = 0x7f;        // Large magnitude remainder
        for (int i = 2; i < 11; i++) {
            buffer[i] = (byte) 0xff;
        }
        TarUtils.parseOctalOrBinary(buffer, 0, 11);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseBinaryLongLengthExceedsNineViaReflection() throws Throwable {
        Method method = TarUtils.class.getDeclaredMethod("parseBinaryLong", byte[].class, int.class, int.class, boolean.class);
        method.setAccessible(true);
        try {
            method.invoke(null, new byte[10], 0, 9, false);
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorViaReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    // -------------------------------------------------------------------------
    // Partition E: ZipEncoding & Fallback Encoding Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCustomZipEncodingParseAndFormat() throws IOException {
        ZipEncoding enc = ZipEncodingHelper.getZipEncoding("UTF-8");
        byte[] buffer = new byte[20];
        int off = TarUtils.formatNameBytes("hello_tar", buffer, 0, 20, enc);
        assertEquals(20, off);

        String name = TarUtils.parseName(buffer, 0, 20, enc);
        assertEquals("hello_tar", name);
    }

    @Test(timeout = 4000)
    public void testFallbackEncodingContract() {
        assertTrue(TarUtils.FALLBACK_ENCODING.canEncode("test"));

        ByteBuffer bb = TarUtils.FALLBACK_ENCODING.encode("abc");
        assertEquals(3, bb.limit());
        assertEquals((byte) 'a', bb.get(0));
        assertEquals((byte) 'b', bb.get(1));
        assertEquals((byte) 'c', bb.get(2));

        byte[] toDecode = new byte[] { 'x', 'y', 0, 'z' };
        String decoded = TarUtils.FALLBACK_ENCODING.decode(toDecode);
        assertEquals("xy", decoded);

        byte[] highBytes = new byte[] { (byte) 0xfe, (byte) 0xff };
        String decodedHigh = TarUtils.FALLBACK_ENCODING.decode(highBytes);
        assertEquals(2, decodedHigh.length());
        assertEquals(0xfe, (int) decodedHigh.charAt(0));
        assertEquals(0xff, (int) decodedHigh.charAt(1));
    }
}