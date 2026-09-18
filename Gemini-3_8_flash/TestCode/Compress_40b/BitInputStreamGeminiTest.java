package org.apache.commons.compress.utils;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.utils.BitInputStream
 *
 * 1. Constructor & Close Contract:
 *    - Valid InputStream & ByteOrder (LITTLE_ENDIAN / BIG_ENDIAN).
 *    - close() propagates invocation to underlying InputStream.
 *    - close() propagates underlying IOException.
 *
 * 2. Parameter Validation (readBits):
 *    - count < 0: throws IllegalArgumentException.
 *    - count > 63 (MAXIMUM_CACHE_SIZE): throws IllegalArgumentException.
 *    - count == 0: boundary edge case, returns 0 without consuming underlying stream.
 *    - count == 63: max boundary condition.
 *
 * 3. Bit Reading & Alignment Logic:
 *    - LITTLE_ENDIAN: LSB-first bit assembly, MASKS[count] application, unsigned right shift.
 *    - BIG_ENDIAN: MSB-first bit assembly, left shift, right shift by (bitsCachedSize - count).
 *    - Single-bit, multi-bit, byte-aligned, and multi-byte crossing requests.
 *    - clearBitCache(): resets cached bits and cache size to zero.
 *
 * 4. Stream End (EOF) Handling:
 *    - in.read() == -1 when cache empty: returns -1.
 *    - in.read() == -1 when bitsCachedSize < count (partial stream end): returns -1.
 *    - Successive calls after EOF continue to return -1.
 *
 * 5. Defect Analysis & Ground Truth Trigger:
 *    - Defects4J Known Defect: Cache overflow when bitsCachedSize >= 57 and an additional byte
 *      is read into a 64-bit signed long.
 *    - LITTLE_ENDIAN overflow: (nextByte << bitsCachedSize) where bitsCachedSize >= 57 causes
 *      shift distances >= 64 (modulo 64 in Java), overwriting low-order bits and losing high-order bits.
 *    - BIG_ENDIAN overflow: (bitsCached <<= 8) when bitsCachedSize >= 57 shifts high-order cached
 *      bits past bit 63, causing bit loss and corrupting the resulting value.
 */
public class BitInputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadBitsSingleBitsLittleEndian() throws IOException {
        // 0b10101100 = 0xAC
        final byte[] data = new byte[] { (byte) 0xAC };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // In LE, bit order read from 0xAC: 0, 0, 1, 1, 0, 1, 0, 1
        assertEquals(0L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(-1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsSingleBitsBigEndian() throws IOException {
        // 0b10101100 = 0xAC
        final byte[] data = new byte[] { (byte) 0xAC };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        // In BE, bit order read from 0xAC: 1, 0, 1, 0, 1, 1, 0, 0
        assertEquals(1L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(1L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(0L, bin.readBits(1));
        assertEquals(-1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsMultipleBitsLittleEndian() throws IOException {
        // Bytes: 0x35 (0b00110101), 0xF0 (0b11110000)
        final byte[] data = new byte[] { 0x35, (byte) 0xF0 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // 3 bits from 0x35: lower 3 bits 0b101 = 5
        assertEquals(5L, bin.readBits(3));
        // Next 5 bits from 0x35: remaining bits 0b00110 = 6
        assertEquals(6L, bin.readBits(5));
        // Next 4 bits from 0xF0: lower 4 bits 0b0000 = 0
        assertEquals(0L, bin.readBits(4));
        // Next 4 bits from 0xF0: upper 4 bits 0b1111 = 15
        assertEquals(15L, bin.readBits(4));
        assertEquals(-1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsMultipleBitsBigEndian() throws IOException {
        // Bytes: 0x35 (0b00110101), 0xF0 (0b11110000)
        final byte[] data = new byte[] { 0x35, (byte) 0xF0 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        // 3 bits from 0x35: upper 3 bits 0b001 = 1
        assertEquals(1L, bin.readBits(3));
        // Next 5 bits from 0x35: remaining 5 bits 0b10101 = 21
        assertEquals(21L, bin.readBits(5));
        // Next 4 bits from 0xF0: upper 4 bits 0b1111 = 15
        assertEquals(15L, bin.readBits(4));
        // Next 4 bits from 0xF0: lower 4 bits 0b0000 = 0
        assertEquals(0L, bin.readBits(4));
        assertEquals(-1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsCrossingByteBoundaryLittleEndian() throws IOException {
        // 0b01010111 (0x57), 0b00000010 (0x02)
        final byte[] data = new byte[] { 0x57, 0x02 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // Read 12 bits: 8 bits of 0x57 + lower 4 bits of 0x02 (which is 2) -> (2 << 8) | 0x57 = 512 + 87 = 599
        assertEquals(599L, bin.readBits(12));
        // Remaining 4 bits in 0x02: upper 4 bits are 0
        assertEquals(0L, bin.readBits(4));
    }

    @Test(timeout = 4000)
    public void testReadBitsCrossingByteBoundaryBigEndian() throws IOException {
        // 0b01010111 (0x57), 0b00000010 (0x02)
        final byte[] data = new byte[] { 0x57, 0x02 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        // Read 12 bits: 8 bits of 0x57 + upper 4 bits of 0x02 (0) -> (0x57 << 4) | 0 = 0x570 = 1392
        assertEquals(1392L, bin.readBits(12));
        // Remaining 4 bits in 0x02: lower 4 bits 0b0010 = 2
        assertEquals(2L, bin.readBits(4));
    }

    @Test(timeout = 4000)
    public void testReadBitsByteAligned() throws IOException {
        final byte[] data = new byte[] { 10, 20, 30 };
        final BitInputStream binLE = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);
        assertEquals(10L, binLE.readBits(8));
        assertEquals(20L, binLE.readBits(8));
        assertEquals(30L, binLE.readBits(8));

        final BitInputStream binBE = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);
        assertEquals(10L, binBE.readBits(8));
        assertEquals(20L, binBE.readBits(8));
        assertEquals(30L, binBE.readBits(8));
    }

    @Test(timeout = 4000)
    public void testClearBitCacheLittleEndian() throws IOException {
        final byte[] data = new byte[] { 0x35, 0x42 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // Consume 3 bits from 0x35; 5 bits remain cached
        assertEquals(5L, bin.readBits(3));
        bin.clearBitCache();

        // Next read must discard the remaining 5 cached bits of 0x35 and read fresh from 0x42
        assertEquals(0x42L, bin.readBits(8));
    }

    @Test(timeout = 4000)
    public void testClearBitCacheBigEndian() throws IOException {
        final byte[] data = new byte[] { 0x35, 0x42 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        // Consume 3 bits from 0x35; 5 bits remain cached
        assertEquals(1L, bin.readBits(3));
        bin.clearBitCache();

        // Next read must discard the remaining 5 cached bits of 0x35 and read fresh from 0x42
        assertEquals(0x42L, bin.readBits(8));
    }

    @Test(timeout = 4000)
    public void testCloseClosesUnderlyingStream() throws IOException {
        final boolean[] closed = new boolean[1];
        final InputStream in = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() {
                closed[0] = true;
            }
        };

        final BitInputStream bin = new BitInputStream(in, ByteOrder.LITTLE_ENDIAN);
        bin.close();
        assertTrue("Underlying InputStream close() must be invoked", closed[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadBitsZeroCountWithEmptyCache() throws IOException {
        final byte[] data = new byte[] { 0x12 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // Reading 0 bits should return 0 without consuming any byte
        assertEquals(0L, bin.readBits(0));
        assertEquals(0x12L, bin.readBits(8));
    }

    @Test(timeout = 4000)
    public void testReadBitsZeroCountWithCachedBitsLittleEndian() throws IOException {
        final byte[] data = new byte[] { 0x12, 0x34 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        assertEquals(2L, bin.readBits(4)); // 0x12 lower nibble is 2
        assertEquals(0L, bin.readBits(0)); // 0 bits requested
        assertEquals(1L, bin.readBits(4)); // 0x12 upper nibble is 1
    }

    @Test(timeout = 4000)
    public void testReadBitsZeroCountWithCachedBitsBigEndian() throws IOException {
        final byte[] data = new byte[] { 0x12, 0x34 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        assertEquals(1L, bin.readBits(4)); // 0x12 upper nibble is 1
        assertEquals(0L, bin.readBits(0)); // 0 bits requested
        assertEquals(2L, bin.readBits(4)); // 0x12 lower nibble is 2
    }

    @Test(timeout = 4000)
    public void testReadBitsCountBoundary63LittleEndian() throws IOException {
        // 8 bytes of 0xFF -> 64 bits of 1s
        final byte[] data = new byte[] {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        final long expected = (1L << 63) - 1; // 63 bits of 1s
        assertEquals(expected, bin.readBits(63));
        // 1 bit remains cached from byte 8
        assertEquals(1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsCountBoundary63BigEndian() throws IOException {
        final byte[] data = new byte[] {
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF
        };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        final long expected = (1L << 63) - 1;
        assertEquals(expected, bin.readBits(63));
        assertEquals(1L, bin.readBits(1));
    }

    @Test(timeout = 4000)
    public void testReadBitsEmptyStreamReturnsNegativeOne() throws IOException {
        final BitInputStream binLE = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.LITTLE_ENDIAN);
        assertEquals(-1L, binLE.readBits(1));

        final BitInputStream binBE = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.BIG_ENDIAN);
        assertEquals(-1L, binBE.readBits(8));
    }

    @Test(timeout = 4000)
    public void testReadBitsStreamEndsPrematurelyReturnsNegativeOne() throws IOException {
        final byte[] data = new byte[] { 0x01 };
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // Requested 16 bits, only 8 bits available in stream
        assertEquals(-1L, bin.readBits(16));
        // Subsequent calls after EOF must continue returning -1
        assertEquals(-1L, bin.readBits(1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Trigger)
    // =========================================================================

    /**
     * Targets Defects4J BitInputStreamTest::littleEndianWithOverflow.
     * When bitsCachedSize is >= 57 (e.g. 57) and count == 63, the while loop executes
     * another in.read(), shifting nextByte by 57 bits. In Java, (nextByte << 57) on an 8-bit
     * value with high bits set causes bits above position 63 to wrap around (shift % 64),
     * corrupting bits in the cache.
     */
    @Test(timeout = 4000)
    public void testLittleEndianBitCacheOverflowRevealsDefect() throws IOException {
        final byte[] data = new byte[16];
        // Byte 15 has bits 6 and 7 set (0b11000000 = 0xC0)
        data[15] = (byte) 0xC0;

        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);

        // 1st read: 63 bits read from bytes 0..7. bitsCachedSize becomes 64 - 63 = 1.
        assertEquals(0L, bin.readBits(63));

        // 2nd read: count=63. bitsCachedSize starts at 1. Loop reads 7 bytes (size 57),
        // then 57 < 63 triggers reading byte 15.
        // Size becomes 57 + 8 = 65. 63 bits are consumed, leaving 2 bits in cache.
        // In the correct implementation, bits 6 and 7 of byte 15 are retained in the cache.
        assertEquals(0L, bin.readBits(63));

        // The remaining 2 bits in cache must be 0b11 = 3L.
        // On defective versions, bit 7 wrapped and was discarded, yielding 1L instead of 3L.
        assertEquals("Defect trigger: Little-endian bit cache overflow corrupted remaining cached bits",
                3L, bin.readBits(2));
    }

    /**
     * Targets Defects4J BitInputStreamTest::bigEndianWithOverflow.
     * In Big Endian mode, when bitsCachedSize >= 57, executing (bitsCached <<= 8)
     * shifts the existing cached bits past bit 63 in a 64-bit long, dropping high bits.
     */
    @Test(timeout = 4000)
    public void testBigEndianBitCacheOverflowRevealsDefect() throws IOException {
        final byte[] data = new byte[16];
        // Byte 7 has bit 0 set, which corresponds to the 64th bit read in Big Endian
        data[7] = 0x01;

        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);

        // 1st read: 63 bits consumed from bytes 0..7. The 64th bit (data[7] bit 0 = 1) remains in cache.
        // bitsCachedSize = 1, bitsCached = 1L.
        assertEquals(0L, bin.readBits(63));

        // 2nd read: count=63. bitsCachedSize reaches 57 < 63, and left-shifting by 8
        // in the defective code shifts the cached bit 1 past bit 63, losing it completely (0L).
        // In the correct implementation, this bit is preserved and output at bit position 62 (1L << 62).
        final long expectedBit62 = 1L << 62;
        assertEquals("Defect trigger: Big-endian bit cache overflow dropped high-order cached bits",
                expectedBit62, bin.readBits(63));
    }

    @Test(timeout = 4000)
    public void testLittleEndianMaxOverflowAt62Bits() throws IOException {
        // Tests maximum possible pre-shift cache size (62 bits)
        // Read 2 bits, leaving 6 bits in cache. Then read 63 bits:
        // Cache reaches 6 + 7*8 = 62 bits. 62 < 63 triggers read of 8th byte at shift 62.
        final byte[] data = new byte[10];
        data[0] = 0x00;
        data[8] = (byte) 0x0C; // bits 2 and 3 set

        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.LITTLE_ENDIAN);
        assertEquals(0L, bin.readBits(2)); // bitsCachedSize = 6
        assertEquals(0L, bin.readBits(63)); // consumes 63 bits, leaving (62 + 8) - 63 = 7 bits in cache

        // The remaining bits must not be corrupted by overflow modulo 64
        final long remaining = bin.readBits(7);
        assertEquals(3L, remaining); // bits 2 and 3 shifted right by 2 -> 3
    }

    @Test(timeout = 4000)
    public void testBigEndianMaxOverflowAt62Bits() throws IOException {
        final byte[] data = new byte[10];
        data[0] = (byte) 0x03; // lower 2 bits set

        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(data), ByteOrder.BIG_ENDIAN);
        assertEquals(0L, bin.readBits(2)); // reads top 2 bits, lower 6 bits (including 0x03) remain cached
        // When 63 bits are read, cache size was 62 < 63, left-shifting by 8
        final long result = bin.readBits(63);
        // Cached bits must be preserved without truncation
        final long expectedMask = 0x03L << (63 - 6);
        assertEquals(expectedMask, result & expectedMask);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadBitsNegativeCountThrowsIllegalArgumentException() throws IOException {
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(new byte[1]), ByteOrder.LITTLE_ENDIAN);
        bin.readBits(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadBitsCount64ThrowsIllegalArgumentException() throws IOException {
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(new byte[10]), ByteOrder.LITTLE_ENDIAN);
        bin.readBits(64);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReadBitsCountLargeThrowsIllegalArgumentException() throws IOException {
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(new byte[10]), ByteOrder.BIG_ENDIAN);
        bin.readBits(Integer.MAX_VALUE);
    }

    @Test(timeout = 4000)
    public void testReadBitsPropagatesIOExceptionFromStream() {
        final InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read error");
            }
        };

        final BitInputStream bin = new BitInputStream(failingStream, ByteOrder.LITTLE_ENDIAN);
        try {
            bin.readBits(8);
            fail("Expected IOException was not thrown");
        } catch (final IOException expected) {
            assertEquals("Simulated read error", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testClosePropagatesIOExceptionFromStream() {
        final InputStream failingStream = new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("Simulated close error");
            }
        };

        final BitInputStream bin = new BitInputStream(failingStream, ByteOrder.BIG_ENDIAN);
        try {
            bin.close();
            fail("Expected IOException on close() was not thrown");
        } catch (final IOException expected) {
            assertEquals("Simulated close error", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testImplementsCloseable() {
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(new byte[0]), ByteOrder.LITTLE_ENDIAN);
        assertTrue("BitInputStream must implement java.io.Closeable", bin instanceof Closeable);
    }

    @Test(timeout = 4000)
    public void testMultipleClosesAreAllowed() throws IOException {
        final BitInputStream bin = new BitInputStream(new ByteArrayInputStream(new byte[1]), ByteOrder.LITTLE_ENDIAN);
        bin.close();
        bin.close(); // Subsequent close must not throw unexpected exception
    }
}