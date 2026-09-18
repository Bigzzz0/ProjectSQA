package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for TarUtils.
 * Targets line/branch coverage and the known Defects4J defect
 * (testRoundTripNames failure due to incorrect byte-to-char conversion in parseName).
 *
 * [Branch & Defect Analysis Matrix]
 * - parseOctal: branches for leading spaces/zeros, trailing null/space, invalid octal digits, overflow.
 * - parseName: branches for null termination, buffer length, and critical byte-to-char conversion (bug: sign extension).
 * - formatNameBytes: branches for name length vs buffer length, padding with NUL.
 * - formatUnsignedOctalString: branches for value==0, value!=0, overflow detection, leading zeros.
 * - formatOctalBytes, formatLongOctalBytes, formatCheckSumOctalBytes: delegate to formatUnsignedOctalString and add trailer.
 * - computeCheckSum: simple loop over all bytes.
 *
 * Defect-targeted test: parseName with bytes > 127 must produce correct Unicode character (e.g., ±).
 * The bug is that (char) buffer[i] sign-extends negative bytes; correct is (char) (buffer[i] & 0xFF).
 */
public class TarUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testParseOctalSimple() {
        byte[] buf = "12345".getBytes();
        assertEquals(12345L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingSpaces() {
        byte[] buf = "   123".getBytes();
        assertEquals(123L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingZeros() {
        byte[] buf = "000123".getBytes();
        assertEquals(123L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalTrailingNull() {
        byte[] buf = new byte[] {'1', '2', '3', 0, '4'};
        assertEquals(123L, TarUtils.parseOctal(buf, 0, 5));
    }

    @Test(timeout = 4000)
    public void testParseOctalTrailingSpace() {
        byte[] buf = "123 ".getBytes();
        assertEquals(123L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalZero() {
        byte[] buf = "0".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpaces() {
        byte[] buf = "     ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalAllZeros() {
        byte[] buf = "00000".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidDigit() {
        byte[] buf = "12a45".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalLargeValue() {
        byte[] buf = "77777777777".getBytes(); // octal 77777777777 = 0x1FFFFFFFFF (max 40-bit)
        assertEquals(0x1FFFFFFFFFL, TarUtils.parseOctal(buf, 0, buf.length));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testParseOctalEmptyBuffer() {
        byte[] buf = new byte[0];
        assertEquals(0L, TarUtils.parseOctal(buf, 0, 0));
    }

    @Test(timeout = 4000)
    public void testParseOctalNegativeOffset() {
        byte[] buf = "123".getBytes();
        try {
            TarUtils.parseOctal(buf, -1, 3);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalNegativeLength() {
        byte[] buf = "123".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, -1);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testParseNameSimple() {
        byte[] buf = "hello".getBytes();
        assertEquals("hello", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test(timeout = 4000)
    public void testParseNameWithNull() {
        byte[] buf = new byte[] {'a', 'b', 0, 'c'};
        assertEquals("ab", TarUtils.parseName(buf, 0, 4));
    }

    @Test(timeout = 4000)
    public void testParseNameEmpty() {
        byte[] buf = new byte[0];
        assertEquals("", TarUtils.parseName(buf, 0, 0));
    }

    @Test(timeout = 4000)
    public void testParseNameAllNulls() {
        byte[] buf = new byte[10];
        assertEquals("", TarUtils.parseName(buf, 0, 10));
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesShorter() {
        byte[] buf = new byte[10];
        int newOffset = TarUtils.formatNameBytes("abc", buf, 0, 10);
        assertEquals(10, newOffset);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals(0, buf[3]);
        assertEquals(0, buf[9]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesLonger() {
        byte[] buf = new byte[5];
        int newOffset = TarUtils.formatNameBytes("abcdefgh", buf, 0, 5);
        assertEquals(5, newOffset);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
        assertEquals('d', buf[3]);
        assertEquals('e', buf[4]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesExact() {
        byte[] buf = new byte[3];
        int newOffset = TarUtils.formatNameBytes("abc", buf, 0, 3);
        assertEquals(3, newOffset);
        assertEquals('a', buf[0]);
        assertEquals('b', buf[1]);
        assertEquals('c', buf[2]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesEmptyName() {
        byte[] buf = new byte[5];
        int newOffset = TarUtils.formatNameBytes("", buf, 0, 5);
        assertEquals(5, newOffset);
        for (int i = 0; i < 5; i++) {
            assertEquals(0, buf[i]);
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known Defects4J defect: parseName must correctly handle bytes > 127.
     * The bug is that (char) buffer[i] sign-extends negative bytes.
     * Expected: byte 0xB1 (decimal 177) should become char '\u00B1' (plus-minus sign).
     */
    @Test(timeout = 4000)
    public void testParseNameWithHighBytes() {
        byte[] buf = new byte[] {(byte) 0xB1, (byte) 0xB2, 0}; // ±²
        String result = TarUtils.parseName(buf, 0, 3);
        // Expected characters: U+00B1, U+00B2
        assertEquals("\u00B1\u00B2", result);
    }

    @Test(timeout = 4000)
    public void testParseNameWithAllHighBytes() {
        byte[] buf = new byte[] {(byte) 0xFF, (byte) 0xFE, (byte) 0x80};
        String result = TarUtils.parseName(buf, 0, 3);
        // Expected: U+00FF, U+00FE, U+0080
        assertEquals("\u00FF\u00FE\u0080", result);
    }

    @Test(timeout = 4000)
    public void testRoundTripHighBytes() {
        // Simulate roundtrip: formatNameBytes then parseName
        String original = "\u00B1\u00B2\u00B3";
        byte[] buf = new byte[10];
        TarUtils.formatNameBytes(original, buf, 0, 10);
        String result = TarUtils.parseName(buf, 0, 10);
        assertEquals(original, result);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(0, buf, 0, 5);
        assertEquals('0', buf[4]);
        assertEquals('0', buf[3]);
        assertEquals('0', buf[2]);
        assertEquals('0', buf[1]);
        assertEquals('0', buf[0]);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringPositive() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(123, buf, 0, 5);
        // octal 123 = 173, padded to 5 digits: "00173"
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('7', buf[3]);
        assertEquals('3', buf[4]);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buf = new byte[2];
        try {
            TarUtils.formatUnsignedOctalString(100, buf, 0, 2); // octal 100 = 144, needs 3 digits
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringMaxFit() {
        byte[] buf = new byte[3];
        TarUtils.formatUnsignedOctalString(63, buf, 0, 3); // octal 63 = 77, fits in 2 digits, padded to 3: "077"
        assertEquals('0', buf[0]);
        assertEquals('7', buf[1]);
        assertEquals('7', buf[2]);
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatOctalBytes(123, buf, 0, 6);
        assertEquals(6, newOffset);
        // formatUnsignedOctalString(123, buf, 0, 4) -> "0173" (padded to 4)
        // then space at index 4, null at index 5
        assertEquals('0', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals('3', buf[3]);
        assertEquals(' ', buf[4]);
        assertEquals(0, buf[5]);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatLongOctalBytes(123, buf, 0, 6);
        assertEquals(6, newOffset);
        // formatUnsignedOctalString(123, buf, 0, 5) -> "00173" (padded to 5)
        // then space at index 5
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('7', buf[3]);
        assertEquals('3', buf[4]);
        assertEquals(' ', buf[5]);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[6];
        int newOffset = TarUtils.formatCheckSumOctalBytes(123, buf, 0, 6);
        assertEquals(6, newOffset);
        // formatUnsignedOctalString(123, buf, 0, 4) -> "0173"
        // then null at index 4, space at index 5
        assertEquals('0', buf[0]);
        assertEquals('1', buf[1]);
        assertEquals('7', buf[2]);
        assertEquals('3', buf[3]);
        assertEquals(0, buf[4]);
        assertEquals(' ', buf[5]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buf = new byte[] {1, 2, 3};
        // sum = (255 & 1) + (255 & 2) + (255 & 3) = 1+2+3 = 6
        assertEquals(6L, TarUtils.computeCheckSum(buf));
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumWithNegativeBytes() {
        byte[] buf = new byte[] {(byte) 0xFF, (byte) 0x80};
        // 0xFF & 255 = 255, 0x80 & 255 = 128, sum = 383
        assertEquals(383L, TarUtils.computeCheckSum(buf));
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumEmpty() {
        byte[] buf = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    // TarUtils is a utility class with private constructor; no instance methods.
    // We can test that the constructor is private via reflection (optional, but not required).
    // Instead, we ensure all static methods are covered.

    @Test(timeout = 4000)
    public void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<TarUtils> c = TarUtils.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", !c.isAccessible());
        c.setAccessible(true);
        TarUtils instance = c.newInstance();
        assertNotNull(instance);
    }
}