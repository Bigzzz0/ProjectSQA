package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: TarUtils.java (Defects4J defect: parseOctal leading NUL handling)
 *
 * Partition A: Core Functional Logic & State Transitions
 * - parseOctal: valid octal strings with and without trailing spaces/NULs, leading spaces, all NULs.
 * - parseOctalOrBinary: negative values (high bit set), valid octal fallback, overflow detection.
 * - parseBoolean: values 0, 1, other.
 * - parseName: normal strings, truncated, null-terminated, empty.
 * - formatNameBytes: full copy, truncation, padding.
 * - formatUnsignedOctalString: zero, small values, large values, boundary overflow.
 * - formatOctalBytes, formatLongOctalBytes, formatLongOctalOrBinaryBytes, formatCheckSumOctalBytes: various lengths.
 * - computeCheckSum: full buffer, empty.
 *
 * Partition B: Boundary Value Analysis & Extremes
 * - parseOctal: length < 2 (throws), length exactly 2, length large, buffer offset out of bounds (implicit).
 * - parseOctalOrBinary: first byte 0x80 (negative?), overflow with max 8-byte, min negative?
 * - formatUnsignedOctalString: value=0, value=1, value=7, value=8, value=MAX_LONG, value overflow.
 * - formatLongOctalOrBinaryBytes: value exactly maxAsOctalChar, value just above, very large.
 *
 * Partition C: Defect-Targeted Branch Zone
 * - parseOctal leading NUL (defect): buffer with leading 0x00 followed by valid octal digits and trailer.
 *   Expected fixed behavior: return 0L. Buggy behavior: throw IllegalArgumentException.
 *   Test: call parseOctal, catch exception -> fail; else assert result==0.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - parseOctal: length<2, invalid digit, missing trailer (no space/NUL), buffer with leading NUL but non-octal digits.
 * - parseOctalOrBinary: overflow beyond 63-bit signed.
 * - formatUnsignedOctalString: value that doesn't fit (val != 0 after loop).
 * - formatLongOctalOrBinaryBytes: value too large (binary overflow or sign bit).
 *
 * Partition E: Object Lifecycle & Contract Integrity (N/A for static utility)
 */
public class TarUtilsDeepseekTest {

    // ------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseOctalStandard() {
        // "12345 " (with trailing space)
        byte[] buf = "12345 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithTrailingNul() {
        // "12345\0"
        byte[] buf = new byte[]{'1','2','3','4','5',0};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNulReturnsZero() {
        byte[] buf = new byte[]{0,0,0};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingSpaces() {
        // "   12345 " (leading spaces)
        String s = "   12345 ";
        byte[] buf = s.getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalExtraTrailingSpace() {
        // "12345  " (two trailing spaces)
        String s = "12345  ";
        byte[] buf = s.getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryHighBitSet() {
        // First byte with 0x80 set -> binary interpretation
        // Example: two-byte number 0x80 0x01 -> -128? Actually val = 0x00? No, it's interpreted as binary.
        // We'll use a known value: 0x80 0x01 represents 1? Let's compute:
        // val = buffer[0] & 0x7f = 0; then shift left 8 + 0x01 = 1 => val = 1
        byte[] buf = {(byte)0x80, 0x01};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryNegativeValue() {
        // Two's complement negative: 0xFF 0xFF = -1 in 16-bit signed, but here it's treated as unsigned binary?
        // The method does not treat it as signed; it just builds a positive value.
        // For 0xFF 0xFF: val = 0x7f, then shift + 0xff => 0x7fff = 32767
        byte[] buf = {(byte)0xFF, (byte)0xFF};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(0x7FFF, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryOctalFallback() {
        // First byte high bit not set -> delegates to parseOctal
        byte[] buf = "777 ".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(511L, result); // octal 777 = decimal 511
    }

    @Test(timeout = 4000)
    public void testParseBooleanTrue() {
        byte[] buf = {1};
        assertTrue(TarUtils.parseBoolean(buf, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanFalse() {
        byte[] buf = {0};
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }

    @Test(timeout = 4000)
    public void testParseBooleanOtherValue() {
        // Any non-1 returns false
        byte[] buf = {2};
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }

    @Test(timeout = 4000)
    public void testParseNameSimple() {
        byte[] buf = "hello\0".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello", name);
    }

    @Test(timeout = 4000)
    public void testParseNameTruncated() {
        // buffer longer than name, no NUL -> full string
        byte[] buf = "hello".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello", name);
    }

    @Test(timeout = 4000)
    public void testParseNameEmpty() {
        byte[] buf = new byte[5];
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesFullCopy() {
        byte[] buf = new byte[10];
        int offset = TarUtils.formatNameBytes("test", buf, 0, buf.length);
        assertEquals(10, offset);
        byte[] expected = "test\0\0\0\0\0\0".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncation() {
        byte[] buf = new byte[4];
        int offset = TarUtils.formatNameBytes("longname", buf, 0, buf.length);
        assertEquals(4, offset);
        byte[] expected = "long".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesEmptyName() {
        byte[] buf = new byte[5];
        int offset = TarUtils.formatNameBytes("", buf, 0, buf.length);
        assertEquals(5, offset);
        for (byte b : buf) assertEquals(0, b);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(0, buf, 0, buf.length);
        byte[] expected = "0000".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringSmall() {
        byte[] buf = new byte[4];
        TarUtils.formatUnsignedOctalString(8, buf, 0, buf.length);
        byte[] expected = "0010".getBytes(); // octal 10 = decimal 8
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringLarge() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(077777L, buf, 0, buf.length);
        // octal 77777 = 32767 decimal
        byte[] expected = "077777".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringOverflowThrows() {
        byte[] buf = new byte[3];
        // Value 01000 (octal) = 512 decimal, needs 4 digits, but buffer length=3 -> overflow
        try {
            TarUtils.formatUnsignedOctalString(512, buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int offset = TarUtils.formatOctalBytes(511, buf, 0, buf.length);
        assertEquals(8, offset);
        // Expected: "777" (octal) padded + trailing space + NUL: positions 0-4: '0' '0' '7' '7' '7', then space, then NUL
        // Actually formatUnsignedOctalString writes with leading zeros length=6 (8-2). So "000777" + space + NUL
        byte[] expected = "000777 \0".getBytes(); // length 8
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatLongOctalBytes(511, buf, 0, buf.length);
        assertEquals(6, offset);
        // idx=5 (length-1), formatUnsignedOctalString into 5 bytes -> "00777" then space
        byte[] expected = "00777 ".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesOctal() {
        // value <= MAXSIZE (TarConstants.MAXSIZE is 077777777777L?) Not available, but we can assume small value
        // Test with value 0
        byte[] buf = new byte[8];
        int offset = TarUtils.formatLongOctalOrBinaryBytes(0, buf, 0, buf.length);
        assertEquals(8, offset);
        // should be octal: formatLongOctalBytes -> "00000000 "? Actually length=8, idx=7, formatUnsignedOctalString(0,buf,0,7) -> "0000000", then space
        byte[] expected = "0000000 ".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinary() {
        // value large enough to require binary
        byte[] buf = new byte[8];
        // Use value 1L << 40 (too large for octal typical? Actually MAXSIZE is large, but we need > MAXSIZE)
        // We'll use a value that fits in 8 bytes but high bit not set initially
        long value = 0x7FFFFFFFFFFFFFFFL; // max positive signed long
        // This will not fit in octal (requires 21 octal digits), so binary path is taken.
        int offset = TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, buf.length);
        assertEquals(8, offset);
        // The method writes the value in big-endian, then sets high bit of first byte.
        byte[] expected = new byte[8];
        long v = value;
        for (int i = 7; i >= 0; i--) {
            expected[i] = (byte) v;
            v >>= 8;
        }
        expected[0] |= 0x80; // set high bit
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesTooLargeThrows() {
        // value that overflows 8-byte signed representation (i.e., would set the sign bit after shifting)
        // Use value = 0xFF... (all ones) will cause final val != 0 after loop
        long value = 0xFFFFFFFFFFFFFFFFL; // -1 as signed, but method treats as unsigned?
        byte[] buf = new byte[8];
        try {
            TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[6];
        int offset = TarUtils.formatCheckSumOctalBytes(511, buf, 0, buf.length);
        assertEquals(6, offset);
        // idx=4 (length-2), formatUnsignedOctalString writes into 4 bytes -> "0777" then NUL then space
        byte[] expected = "0777\0 ".getBytes();
        assertArrayEquals(expected, buf);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumEmpty() {
        byte[] buf = new byte[0];
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(0L, sum);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumNormal() {
        byte[] buf = new byte[]{1,2,3};
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(6L, sum);
    }

    // ------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testParseOctalLengthLessThanTwo() {
        byte[] buf = new byte[]{'1'};
        TarUtils.parseOctal(buf, 0, 1);
    }

    @Test(timeout = 4000)
    public void testParseOctalLengthExactlyTwo() {
        byte[] buf = new byte[]{'0', ' '};
        long result = TarUtils.parseOctal(buf, 0, 2);
        assertEquals(0L, result);
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidDigit() {
        byte[] buf = "12g5 ".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalMissingTrailer() {
        // no trailing space or NUL
        byte[] buf = "12345".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalOrBinaryOverflow() {
        // 9-byte binary with high bit set -> overflow detection
        byte[] buf = new byte[9];
        buf[0] = (byte) 0xFF; // high bit set, then shift overflow
        for (int i = 1; i < 9; i++) buf[i] = (byte) 0xFF;
        try {
            TarUtils.parseOctalOrBinary(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Leading NUL in parseOctal)
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseOctalLeadingNulShouldReturnZero() {
        // This test targets the known defect: a leading NUL followed by valid octal digits.
        // Buffer: NUL, '1','2','3','4','5', space
        byte[] buf = new byte[]{0, '1', '2', '3', '4', '5', ' '};
        try {
            long result = TarUtils.parseOctal(buf, 0, buf.length);
            // On fixed version, result should be 0 because leading NUL triggers early return.
            assertEquals("Leading NUL should make parseOctal return 0", 0L, result);
        } catch (IllegalArgumentException e) {
            // On buggy version, an exception is thrown -> test fails as intended.
            fail("Bug: parseOctal should not throw on leading NUL, but returned exception: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingNulAndTrailingNul() {
        // Another variant: NUL + octal digits + NUL
        byte[] buf = new byte[]{0, '1', '2', '3', 0};
        try {
            long result = TarUtils.parseOctal(buf, 0, buf.length);
            assertEquals(0L, result);
        } catch (IllegalArgumentException e) {
            fail("Bug: parseOctal should not throw on leading NUL");
        }
    }

    // ------------------------------------------------------------------
    // Partition D: Additional Exception & Defensive Guard Paths
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParseOctalTrailerAfterTrim() {
        // After trimming one trailer, the new trailer is not valid
        byte[] buf = "12345\0 ".getBytes(); // first trailer is space, reduces end, new trailer is NUL, reduces again, then digits valid
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(12345L, result);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringNegativeThrows() {
        // value negative -> treated as unsigned? Actually method expects unsigned, but negative long becomes large positive.
        // It will be written as octal of the two's complement representation.
        byte[] buf = new byte[20];
        // -1 as unsigned = 0xFFFFFFFFFFFFFFFF -> octal 1777777777777777777777 (22 digits) won't fit in 20
        try {
            TarUtils.formatUnsignedOctalString(-1L, buf, 0, 20);
            fail("Expected IllegalArgumentException for value that does not fit");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalOrBinaryBytesBinaryWithHighBitAlreadySet() {
        // value where after shifting, buf[0] has high bit set -> overflow detection
        // Use value that when written in binary, the most significant byte already has high bit set.
        // For 8-byte, value 0x8000000000000000L = Long.MIN_VALUE
        // But such value will cause val != 0 after loop? The method shifts until all bits are accounted.
        // It will write bytes, then val will be 0 after 8 shifts because long has 64 bits?
        // Actually if value is exactly 0x80..., after shifting 8 times, val becomes 0. But buf[0] will have high bit set, so condition (buf[offset] & 0x80) != 0 true, throws.
        long value = Long.MIN_VALUE; // 0x8000000000000000
        byte[] buf = new byte[8];
        try {
            TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, buf.length);
            fail("Expected IllegalArgumentException because high bit set in first byte");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Note: The format methods also may throw if value does not fit octal space.
    // Already covered in testFormatUnsignedOctalStringOverflowThrows.
}