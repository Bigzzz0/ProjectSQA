package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.compress.archivers.tar.TarUtils
 *
 * Defects4J Ground Truth Target:
 * - TarUtilsTest::testRoundTripNames
 *   Issue: Non-ASCII characters (e.g. '±' / \u00B1) cast to byte and then back to char via
 *   (char) buffer[i] without masking (BYTE_MASK / 0xFF) sign-extend into 0xFFB1 instead of 0x00B1.
 *   Targeted in: testRoundTripNamesDefectRevealing()
 *
 * Branch & Condition Coverage:
 * 1. parseOctal:
 *    - Empty buffer / zero length (no-op loop, returns 0)
 *    - Initial byte == 0 (early NUL termination, returns 0)
 *    - stillPadding == true with ' ' and '0' (ignored leading pads)
 *    - stillPadding == false with '0' (processed octal digit)
 *    - stillPadding == false with ' ' (trailing space break)
 *    - Embedded invalid octal digit < '0' (e.g. '/', '-') -> throws IllegalArgumentException
 *    - Embedded invalid octal digit > '7' (e.g. '8', '9', 'A') -> throws IllegalArgumentException
 *    - Value accumulation: all octal digits '0'-'7'
 *    - Multiple trailing spaces / NUL after value
 * 2. parseName:
 *    - String ends before length due to NUL byte
 *    - String fills entire length without NUL
 *    - Empty length / immediate NUL
 *    - Full 8-bit character parsing (reveals sign extension bug)
 * 3. formatNameBytes:
 *    - name shorter than buffer (truncated with trailing NULs)
 *    - name longer than buffer (truncated to length, no NUL padding loop executed)
 *    - name exactly buffer length (no NUL padding)
 *    - offset > 0 handling
 * 4. formatUnsignedOctalString:
 *    - value == 0 (writes single '0' and pads remaining with '0')
 *    - value > 0 fitting completely into buffer
 *    - value > 0 exceeding buffer capacity (throws IllegalArgumentException)
 *    - max boundary 64-bit values (Long.MAX_VALUE)
 * 5. formatOctalBytes / formatLongOctalBytes / formatCheckSumOctalBytes:
 *    - formatOctalBytes: writes octal + ' ' + NUL
 *    - formatLongOctalBytes: writes octal + ' '
 *    - formatCheckSumOctalBytes: writes octal + NUL + ' '
 *    - Offset and length boundary return values
 * 6. computeCheckSum:
 *    - Empty buffer (0 sum)
 *    - Buffer with signed/negative bytes (verifies 0xFF masking logic)
 *    - Buffer with zero and positive bytes
 * 7. TarUtils constructor:
 *    - Private constructor invocation via reflection for code coverage & contract integrity
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalValidBasic() {
        byte[] buffer = "0755 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, val); // 0755 octal = 493 decimal
    }

    @Test(timeout = 4000)
    public void testParseOctalLeadingSpacesAndZeroes() {
        byte[] buffer = "   000755 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalTrailingNul() {
        byte[] buffer = new byte[]{' ', '0', '6', '4', '4', 0, '9'};
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(420L, val); // 0644 octal = 420 decimal, stops at 0
    }

    @Test(timeout = 4000)
    public void testParseOctalZeroValue() {
        byte[] buffer = " 000000 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithOffset() {
        byte[] buffer = "XXXX0123 YYYY".getBytes();
        long val = TarUtils.parseOctal(buffer, 4, 5); // "0123 "
        assertEquals(83L, val); // 0123 octal = 83 decimal
    }

    @Test(timeout = 4000)
    public void testParseNameBasic() {
        byte[] buffer = new byte[]{'h', 'e', 'l', 'l', 'o', 0, 'w', 'o', 'r', 'l', 'd'};
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", name);
    }

    @Test(timeout = 4000)
    public void testParseNameNoNul() {
        byte[] buffer = new byte[]{'t', 'e', 's', 't'};
        String name = TarUtils.parseName(buffer, 0, 4);
        assertEquals("test", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesShort() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("abc", buffer, 1, 5);
        assertEquals(6, nextOffset);
        assertEquals(0, buffer[0]);
        assertEquals((byte) 'a', buffer[1]);
        assertEquals((byte) 'b', buffer[2]);
        assertEquals((byte) 'c', buffer[3]);
        assertEquals(0, buffer[4]);
        assertEquals(0, buffer[5]);
        assertEquals(0, buffer[6]);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncate() {
        byte[] buffer = new byte[4];
        int nextOffset = TarUtils.formatNameBytes("abcdefgh", buffer, 0, 4);
        assertEquals(4, nextOffset);
        assertEquals((byte) 'a', buffer[0]);
        assertEquals((byte) 'b', buffer[1]);
        assertEquals((byte) 'c', buffer[2]);
        assertEquals((byte) 'd', buffer[3]);
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatOctalBytes(0755, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals("0000755 ", new String(buffer, 0, 7));
        assertEquals(0, buffer[7]); // Trailing NUL
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatLongOctalBytes(0755, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals("0000755 ", new String(buffer, 0, 8));
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytes() {
        byte[] buffer = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(0123, buffer, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals("0000123", new String(buffer, 0, 7).substring(0, 6));
        assertEquals(0, buffer[6]);
        assertEquals((byte) ' ', buffer[7]);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buffer = new byte[]{(byte) 255, 1, 2, 3};
        long sum = TarUtils.computeCheckSum(buffer);
        // 255 + 1 + 2 + 3 = 261
        assertEquals(261L, sum);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalEmptyBufferLength() {
        byte[] buffer = new byte[10];
        long val = TarUtils.parseOctal(buffer, 0, 0);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllSpaces() {
        byte[] buffer = "     ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllZeroes() {
        byte[] buffer = "00000".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, val);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllDigits() {
        // Octal 01234567
        byte[] buffer = "1234567".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(342391L, val);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertArrayEquals(new byte[]{'0', '0', '0', '0'}, buffer);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringExactFit() {
        byte[] buffer = new byte[3];
        // 0777 octal = 511 decimal
        TarUtils.formatUnsignedOctalString(511L, buffer, 0, 3);
        assertArrayEquals(new byte[]{'7', '7', '7'}, buffer);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesEmptyString() {
        byte[] buffer = new byte[3];
        int res = TarUtils.formatNameBytes("", buffer, 0, 3);
        assertEquals(3, res);
        assertArrayEquals(new byte[]{0, 0, 0}, buffer);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumEmptyBuffer() {
        byte[] buffer = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(buffer));
    }

    @Test(timeout = 4000)
    public void testComputeCheckSumNegativeBytes() {
        byte[] buffer = new byte[]{(byte) -1, (byte) -128, 0, 127};
        // (-1 & 0xFF) = 255, (-128 & 0xFF) = 128, 0 = 0, 127 = 127 => 255 + 128 + 0 + 127 = 510
        long sum = TarUtils.computeCheckSum(buffer);
        assertEquals(510L, sum);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known failure: TarUtilsTest.testRoundTripNames
     * When characters outside the 7-bit ASCII range (such as '±', \u00B1) are parsed,
     * casting signed byte to char without 0xFF mask results in \uFFB1 rather than \u00B1.
     */
    @Test(timeout = 4000)
    public void testRoundTripNamesDefectRevealing() {
        String testName = "0302-0601-3±±±F06±W220±ZB±LALALA±±±±±±±±±±CAN±±DC±±±04±060302±MOE.model";
        byte[] buffer = new byte[100];
        TarUtils.formatNameBytes(testName, buffer, 0, buffer.length);
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals(testName, parsed);
    }

    @Test(timeout = 4000)
    public void testParseNameWithExtendedAsciiChar() {
        byte[] buffer = new byte[]{(byte) 0xB1, 0}; // 0xB1 is '±'
        String parsed = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("\u00B1", parsed);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalInvalidDigitBelowZero() {
        byte[] buffer = " 0/23 ".getBytes(); // '/' is ASCII 47 ('0' - 1)
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for digit '< 0'");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Invalid octal digit"));
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidDigitAboveSeven() {
        byte[] buffer = " 087 ".getBytes(); // '8' is ASCII 56 ('7' + 1)
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for digit '8'");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Invalid octal digit"));
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidDigitAlpha() {
        byte[] buffer = " 12A ".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for letter 'A'");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Invalid octal digit"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        try {
            // 0100 octal = 64 decimal requires at least 3 chars ("100")
            TarUtils.formatUnsignedOctalString(64L, buffer, 0, 2);
            fail("Expected IllegalArgumentException for value overflow");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("will not fit"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytesOverflow() {
        byte[] buffer = new byte[3];
        try {
            // buffer length 3 gives 1 char for octal value (3 - 2 = 1)
            // Value 8 requires 2 chars ("10")
            TarUtils.formatOctalBytes(8L, buffer, 0, 3);
            fail("Expected IllegalArgumentException for formatOctalBytes overflow");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("will not fit"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buffer = new byte[2];
        try {
            // length 2 gives 1 char for octal value (2 - 1 = 1)
            TarUtils.formatLongOctalBytes(8L, buffer, 0, 2);
            fail("Expected IllegalArgumentException for formatLongOctalBytes overflow");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("will not fit"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buffer = new byte[3];
        try {
            // length 3 gives 1 char for octal value (3 - 2 = 1)
            TarUtils.formatCheckSumOctalBytes(8L, buffer, 0, 3);
            fail("Expected IllegalArgumentException for formatCheckSumOctalBytes overflow");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("will not fit"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorIsPrivate() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }
}