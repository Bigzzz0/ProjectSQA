package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Method Covered                 | Branches / Conditions Targeted                       | Defect / Edge
 * ====================================================================================================
 * TarUtils()                     | Reflection invocation of private constructor         | Lifecycle check
 * parseOctal                     | length < 2 (buffer.length = 0, length = 0)           | DEFECT TARGET (compress-1)
 * parseOctal                     | length < 2 (buffer.length = 1, length = 1)           | DEFECT TARGET (compress-1)
 * parseOctal                     | buffer with all NULs -> returns 0L                   | Normal empty entry
 * parseOctal                     | Leading spaces and '0's when stillPadding=true       | Space/Zero trim
 * parseOctal                     | Internal '0' after stillPadding=false                | Octal digit '0'
 * parseOctal                     | Trailing space when stillPadding=false -> break      | Valid trailer
 * parseOctal                     | Trailing NUL (byte 0) -> break                       | Valid trailer
 * parseOctal                     | Non-octal char < '0' (e.g., '/', '-', '+', '!')      | IllegalArgumentException
 * parseOctal                     | Non-octal char > '7' (e.g., '8', '9', 'a')           | IllegalArgumentException
 * parseOctal                     | exceptionMessage formatting with {NUL} replacement   | Error message path
 * parseOctal                     | Non-zero offset parsing                              | Offset calculation
 * parseName                      | Stops on NUL delimiter                               | Standard tar string
 * parseName                      | Consumes up to length without NUL                    | Truncated string
 * parseName                      | High-bit bytes (> 127) & 0xFF sign-extension         | UTF-8 / extended ASCII
 * parseName                      | Offset > 0 parsing                                   | Offset boundary
 * formatNameBytes                | name.length() < length (pads remainder with NUL)     | NUL padding
 * formatNameBytes                | name.length() > length (truncates to length)         | Truncation
 * formatNameBytes                | name.length() == length (exact fit)                  | Exact boundary
 * formatNameBytes                | Offset > 0 destination buffer                        | Offset boundary
 * formatUnsignedOctalString      | value == 0 (single '0' + leading zeroes)             | Zero formatting
 * formatUnsignedOctalString      | value > 0 with leading zeroes fill                   | Standard octal
 * formatUnsignedOctalString      | val != 0 when remaining < 0 (overflow)               | IllegalArgumentException
 * formatOctalBytes               | idx = length - 2, writes trailing ' ' and NUL        | Octal with space+NUL
 * formatOctalBytes               | Overflow check via formatUnsignedOctalString         | Overflow exception
 * formatLongOctalBytes           | idx = length - 1, writes trailing ' '                | Long octal with space
 * formatLongOctalBytes           | Overflow check via formatUnsignedOctalString         | Overflow exception
 * formatCheckSumOctalBytes       | idx = length - 2, writes trailing NUL and ' '        | Checksum formatting
 * formatCheckSumOctalBytes       | Overflow check via formatUnsignedOctalString         | Overflow exception
 * computeCheckSum                | Positive and negative bytes (BYTE_MASK unsigned)     | Checksum calculation
 * computeCheckSum                | Empty buffer (length = 0)                            | Zero sum boundary
 * ====================================================================================================
 */
public class TarUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalValidStandardValues() {
        byte[] buffer = " 755 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493L, value); // 7*64 + 5*8 + 5 = 493

        byte[] bufferWithZeros = "0000755 \0".getBytes();
        long value2 = TarUtils.parseOctal(bufferWithZeros, 0, bufferWithZeros.length);
        assertEquals(493L, value2);
    }

    @Test(timeout = 4000)
    public void testParseOctalInternalZerosAfterNonZero() {
        // Octal 107 = 1*64 + 0*8 + 7 = 71 decimal
        byte[] buffer = " 107 \0".getBytes();
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(71L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalAllNulsReturnsZero() {
        byte[] buffer = new byte[8];
        long value = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(0L, value);
    }

    @Test(timeout = 4000)
    public void testParseOctalWithOffset() {
        byte[] buffer = "XXXX 0775 \0YYYY".getBytes();
        long value = TarUtils.parseOctal(buffer, 4, 8);
        assertEquals(509L, value); // 7*64 + 7*8 + 5 = 509
    }

    @Test(timeout = 4000)
    public void testParseNameNormalTerminatedByNul() {
        byte[] buffer = "archive/file.txt\0extraGarbage".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("archive/file.txt", name);
    }

    @Test(timeout = 4000)
    public void testParseNameNoNulConsumesFullLength() {
        byte[] buffer = "test123456".getBytes();
        String name = TarUtils.parseName(buffer, 0, 4);
        assertEquals("test", name);
    }

    @Test(timeout = 4000)
    public void testParseNameWithOffset() {
        byte[] buffer = "HEADER:myFileName\0TAIL".getBytes();
        String name = TarUtils.parseName(buffer, 7, 11);
        assertEquals("myFileName", name);
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesShortNamePadsNul() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatNameBytes("foo", buf, 0, 6);
        assertEquals(6, nextOffset);
        assertEquals('f', (char) buf[0]);
        assertEquals('o', (char) buf[1]);
        assertEquals('o', (char) buf[2]);
        assertEquals(0, buf[3]);
        assertEquals(0, buf[4]);
        assertEquals(0, buf[5]);
        assertEquals(0, buf[6]); // Unchanged remainder of buffer
    }

    @Test(timeout = 4000)
    public void testFormatNameBytesTruncatesLongName() {
        byte[] buf = new byte[6];
        int nextOffset = TarUtils.formatNameBytes("123456789", buf, 1, 4);
        assertEquals(5, nextOffset);
        assertEquals(0, buf[0]);
        assertEquals('1', (char) buf[1]);
        assertEquals('2', (char) buf[2]);
        assertEquals('3', (char) buf[3]);
        assertEquals('4', (char) buf[4]);
        assertEquals(0, buf[5]);
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringZero() {
        byte[] buffer = new byte[4];
        TarUtils.formatUnsignedOctalString(0L, buffer, 0, 4);
        assertEquals("0000", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringPositiveValue() {
        byte[] buffer = new byte[6];
        TarUtils.formatUnsignedOctalString(493L, buffer, 0, 6);
        assertEquals("000755", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytesRoundTrip() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatOctalBytes(0755L, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals(' ', buf[10]);
        assertEquals(0, buf[11]);

        long parsed = TarUtils.parseOctal(buf, 0, 12);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytesRoundTrip() {
        byte[] buf = new byte[12];
        int nextOffset = TarUtils.formatLongOctalBytes(0755L, buf, 0, 12);
        assertEquals(12, nextOffset);
        assertEquals(' ', buf[11]);

        long parsed = TarUtils.parseOctal(buf, 0, 12);
        assertEquals(0755L, parsed);
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytesRoundTrip() {
        byte[] buf = new byte[8];
        int nextOffset = TarUtils.formatCheckSumOctalBytes(01234L, buf, 0, 8);
        assertEquals(8, nextOffset);
        assertEquals(0, buf[6]);
        assertEquals(' ', buf[7]);

        long parsed = TarUtils.parseOctal(buf, 0, 8);
        assertEquals(01234L, parsed);
    }

    @Test(timeout = 4000)
    public void testComputeCheckSum() {
        byte[] buf = new byte[] { 1, 2, 3, 4, 5 };
        assertEquals(15L, TarUtils.computeCheckSum(buf));

        byte[] empty = new byte[0];
        assertEquals(0L, TarUtils.computeCheckSum(empty));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testComputeCheckSumWithHighBitNegativeBytes() {
        // Ensure byte value -1 ((byte)0xFF) is treated as unsigned 255
        byte[] buf = new byte[] { (byte) 0xFF, (byte) 0x80 };
        long sum = TarUtils.computeCheckSum(buf);
        assertEquals(255L + 128L, sum);
    }

    @Test(timeout = 4000)
    public void testParseNameWithExtendedAsciiHighBitBytes() {
        byte[] buffer = new byte[] { (byte) 0xE4, (byte) 0xF6, (byte) 0xFC, 0 }; // ä, ö, ü in ISO-8859-1
        String name = TarUtils.parseName(buffer, 0, 4);
        assertEquals(3, name.length());
        assertEquals(0xE4, (int) name.charAt(0));
        assertEquals(0xF6, (int) name.charAt(1));
        assertEquals(0xFC, (int) name.charAt(2));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringExactFit() {
        byte[] buffer = new byte[3];
        TarUtils.formatUnsignedOctalString(0777L, buffer, 0, 3);
        assertEquals("777", new String(buffer));
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringLargeLong() {
        byte[] buffer = new byte[11];
        TarUtils.formatUnsignedOctalString(017777777777L, buffer, 0, 11);
        assertEquals("17777777777", new String(buffer));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth: compress-1)
    // Contract: length must be at least 2 bytes; throws IllegalArgumentException.
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalInvalidLengthZeroShouldThrow() {
        byte[] buffer = new byte[0];
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
            // Expected defect behavior
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidLengthOneShouldThrow() {
        byte[] buffer = new byte[] { 0 };
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
            // Expected defect behavior
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidLengthOneSpaceShouldThrow() {
        byte[] buffer = new byte[] { (byte) ' ' };
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException - should be at least 2 bytes long");
        } catch (IllegalArgumentException expected) {
            // Expected defect behavior
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseOctalInvalidByteBelowZero() {
        byte[] buffer = " 12-3 \0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for invalid '-' byte");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should indicate invalid byte", e.getMessage().contains("Invalid byte"));
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidByteAboveSeven() {
        byte[] buffer = " 078 \0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for invalid '8' byte");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should indicate invalid byte", e.getMessage().contains("Invalid byte"));
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalInvalidByteAlpha() {
        byte[] buffer = " 12A \0".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException for invalid 'A' byte");
        } catch (IllegalArgumentException e) {
            assertTrue("Message should indicate invalid byte", e.getMessage().contains("Invalid byte"));
        }
    }

    @Test(timeout = 4000)
    public void testParseOctalExceptionMessageContainsNulPlaceholder() {
        byte[] buffer = new byte[] { '1', (byte) 255, 0, ' ' };
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should replace \\0 with {NUL}", e.getMessage().contains("{NUL}"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatUnsignedOctalStringOverflow() {
        byte[] buffer = new byte[2];
        try {
            // 0100 octal requires 3 digits, buffer only has length 2
            TarUtils.formatUnsignedOctalString(0100L, buffer, 0, 2);
            fail("Expected IllegalArgumentException when value exceeds buffer capacity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("will not fit in octal number buffer"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatOctalBytesOverflow() {
        byte[] buffer = new byte[3];
        try {
            // Length 3 gives idx = 1 for octal conversion; 077L (63) requires 2 digits
            TarUtils.formatOctalBytes(077L, buffer, 0, 3);
            fail("Expected IllegalArgumentException when value exceeds octal bytes capacity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("will not fit in octal number buffer"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatLongOctalBytesOverflow() {
        byte[] buffer = new byte[2];
        try {
            // Length 2 gives idx = 1 for octal conversion; 077L requires 2 digits
            TarUtils.formatLongOctalBytes(077L, buffer, 0, 2);
            fail("Expected IllegalArgumentException when value exceeds long octal capacity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("will not fit in octal number buffer"));
        }
    }

    @Test(timeout = 4000)
    public void testFormatCheckSumOctalBytesOverflow() {
        byte[] buffer = new byte[3];
        try {
            // Length 3 gives idx = 1 for octal conversion; 077L requires 2 digits
            TarUtils.formatCheckSumOctalBytes(077L, buffer, 0, 3);
            fail("Expected IllegalArgumentException when value exceeds checksum octal capacity");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("will not fit in octal number buffer"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorCoverageViaReflection() throws Exception {
        Constructor<TarUtils> constructor = TarUtils.class.getDeclaredConstructor();
        assertTrue("Constructor should be private", (constructor.getModifiers() & java.lang.reflect.Modifier.PRIVATE) != 0);
        constructor.setAccessible(true);
        TarUtils instance = constructor.newInstance();
        assertNotNull("Instance should not be null", instance);
    }
}