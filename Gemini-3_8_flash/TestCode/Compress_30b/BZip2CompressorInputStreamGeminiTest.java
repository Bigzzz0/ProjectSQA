package org.apache.commons.compress.compressors.bzip2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
 *
 * 1. DEFECT UNDER TEST (Defects4J Ground Truth):
 *    - Method: read(final byte[] dest, final int offs, final int len)
 *    - Flaw: When len == 0, the loop condition (destOffs < hi) is false immediately.
 *            The method returns (destOffs == offs) ? -1 : (destOffs - offs), resulting in -1 instead of 0.
 *    - Contract: java.io.InputStream#read(byte[], int, int) specifies: "If len is zero, then no bytes
 *                are read and 0 is returned; otherwise, there is an attempt to read at least one byte."
 *    - Test: readOfLength0ShouldReturn0 directly exposes this defect by asserting assertEquals(0, read(...)).
 *
 * 2. BRANCH & STATE COVERAGE MATRIX:
 *    - matches(byte[], int):
 *        * length < 3 -> false
 *        * signature[0] != 'B' -> false
 *        * signature[1] != 'Z' -> false
 *        * signature[2] != 'h' -> false
 *        * valid signature ("BZh") -> true
 *    - constructor(InputStream, boolean):
 *        * null stream -> NullPointerException or IOException("No InputStream")
 *        * empty stream -> IOException("Stream is not in the BZip2 format")
 *        * invalid magic bytes -> IOException
 *        * invalid block size (< '1' or > '9') -> IOException("BZip2 block size is invalid")
 *    - initBlock() / complete():
 *        * Stream end marker (0x17, 0x72, 0x45, 0x38, 0x50, 0x90) with valid CRC -> EOF state
 *        * Stream end marker with bad CRC -> IOException("BZip2 CRC error")
 *        * Bad block header marker -> IOException("bad block header")
 *        * Concatenated stream support: decompressConcatenated = true / false
 *        * Garbage after valid stream in concatenated mode -> IOException
 *    - read(byte[], int, int) bounds checking:
 *        * offs < 0 -> IndexOutOfBoundsException
 *        * len < 0 -> IndexOutOfBoundsException
 *        * offs + len > dest.length -> IndexOutOfBoundsException
 *    - close():
 *        * closes underlying stream and nullifies internal fields
 *        * read() after close() -> IOException("stream closed")
 *        * read(b, off, len) after close() -> IOException("stream closed")
 *    - EOF handling:
 *        * read() at EOF -> returns -1
 *        * read(b, off, len) at EOF when len > 0 -> returns -1
 * ---------------------------------------------------------------------------------------------------------
 */
public class BZip2CompressorInputStreamGeminiTest {

    /**
     * Valid empty BZip2 payload containing:
     * - Header: 'B', 'Z', 'h', '1'
     * - End-of-Stream Marker: 0x17, 0x72, 0x45, 0x38, 0x50, 0x90
     * - Stored Combined CRC: 0x00, 0x00, 0x00, 0x00 (matches computed 0)
     */
    private static final byte[] EMPTY_BZIP2_STREAM = new byte[] {
        'B', 'Z', 'h', '1',
        0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
        0x00, 0x00, 0x00, 0x00
    };

    /**
     * Helper to instantiate a valid empty stream.
     */
    private BZip2CompressorInputStream createEmptyStream(boolean decompressConcatenated) throws IOException {
        return new BZip2CompressorInputStream(new ByteArrayInputStream(EMPTY_BZIP2_STREAM), decompressConcatenated);
    }

    /* ====================================================================
     * PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
     * ==================================================================== */

    /**
     * Targets the known defect where reading 0 bytes erroneously returns -1 instead of 0.
     * java.io.InputStream.read(byte[], int, int) strictly requires returning 0 when len == 0.
     */
    @Test(timeout = 4000)
    public void readOfLength0ShouldReturn0() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            byte[] buf = new byte[64];
            int bytesRead = bzip2.read(buf, 0, 0);
            assertEquals("Reading 0 bytes must return 0 per InputStream contract", 0, bytesRead);
        }
    }

    /**
     * Additional check for read(buf, offs, 0) with non-zero offset.
     */
    @Test(timeout = 4000)
    public void readOfLength0WithNonZeroOffsetShouldReturn0() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            byte[] buf = new byte[64];
            int bytesRead = bzip2.read(buf, 10, 0);
            assertEquals("Reading 0 bytes with offset must return 0", 0, bytesRead);
        }
    }

    /* ====================================================================
     * PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
     * ==================================================================== */

    @Test(timeout = 4000)
    public void testMatchesSignatureVerification() {
        // Less than 3 bytes
        assertFalse(BZip2CompressorInputStream.matches(new byte[0], 0));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B' }, 1));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z' }, 2));

        // Invalid signature characters
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'A', 'Z', 'h' }, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'X', 'h' }, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z', 'x' }, 3));

        // Valid signature
        assertTrue(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z', 'h' }, 3));
        assertTrue(BZip2CompressorInputStream.matches(new byte[] { 'B', 'Z', 'h', '9', 'a' }, 5));
    }

    @Test(timeout = 4000)
    public void testSingleStreamReadAtEof() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            assertEquals("read() on empty stream at EOF must return -1", -1, bzip2.read());
            byte[] buf = new byte[10];
            assertEquals("read(buf, 0, 10) on empty stream at EOF must return -1", -1, bzip2.read(buf, 0, 10));
        }
    }

    @Test(timeout = 4000)
    public void testConcatenatedEmptyStreams() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(EMPTY_BZIP2_STREAM);
        baos.write(EMPTY_BZIP2_STREAM);

        try (BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(
                new ByteArrayInputStream(baos.toByteArray()), true)) {
            assertEquals(-1, bzip2.read());
        }
    }

    @Test(timeout = 4000)
    public void testNonConcatenatedStopsAfterFirstStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(EMPTY_BZIP2_STREAM);
        baos.write(EMPTY_BZIP2_STREAM);

        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        try (BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(in, false)) {
            assertEquals(-1, bzip2.read());
            // Since decompressConcatenated is false, the second stream's bytes remain unread in the InputStream
            assertEquals("Second stream should remain unconsumed", EMPTY_BZIP2_STREAM.length, in.available());
        }
    }

    /* ====================================================================
     * PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
     * ==================================================================== */

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadNegativeOffsetThrowsException() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            bzip2.read(new byte[10], -1, 5);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadNegativeLengthThrowsException() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            bzip2.read(new byte[10], 0, -1);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadOffsetPlusLengthExceedsBufferThrowsException() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            bzip2.read(new byte[10], 5, 6);
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadOffsetAtBufferLengthWithPositiveLengthThrowsException() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            bzip2.read(new byte[10], 10, 1);
        }
    }

    @Test(timeout = 4000)
    public void testReadOffsetAtBufferLengthWithZeroLength() throws IOException {
        try (BZip2CompressorInputStream bzip2 = createEmptyStream(false)) {
            // offs + len == 10 + 0 <= 10 -> valid boundary!
            int result = bzip2.read(new byte[10], 10, 0);
            assertEquals(0, result);
        }
    }

    /* ====================================================================
     * PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
     * ==================================================================== */

    @Test(expected = IOException.class, timeout = 4000)
    public void testConstructorWithNullInputStreamThrowsException() throws IOException {
        new BZip2CompressorInputStream(null);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testConstructorWithEmptyStreamThrowsException() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testConstructorWithInvalidMagicThrowsException() throws IOException {
        new BZip2CompressorInputStream(new ByteArrayInputStream(new byte[] { 'X', 'Y', 'Z', '1' }));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testConstructorWithBlockSizeTooLowThrowsException() throws IOException {
        byte[] invalidBlockSize = new byte[] { 'B', 'Z', 'h', '0' };
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidBlockSize));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testConstructorWithBlockSizeTooHighThrowsException() throws IOException {
        byte[] invalidBlockSize = new byte[] { 'B', 'Z', 'h', ':' }; // ':' is '9' + 1
        new BZip2CompressorInputStream(new ByteArrayInputStream(invalidBlockSize));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testUnexpectedEndOfStreamDuringMagic() throws IOException {
        byte[] truncated = new byte[] { 'B', 'Z', 'h', '1', 0x17, 0x72 };
        new BZip2CompressorInputStream(new ByteArrayInputStream(truncated));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testBadBlockHeaderThrowsException() throws IOException {
        byte[] badHeader = new byte[] {
            'B', 'Z', 'h', '1',
            0x11, 0x22, 0x33, 0x44, 0x55, 0x66, // Invalid block header
            0x00, 0x00, 0x00, 0x00
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badHeader));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCrcMismatchThrowsException() throws IOException {
        byte[] badCrcStream = new byte[] {
            'B', 'Z', 'h', '1',
            0x17, 0x72, 0x45, 0x38, 0x50, (byte) 0x90,
            0x00, 0x00, 0x12, 0x34 // Corrupted CRC (expected 0)
        };
        new BZip2CompressorInputStream(new ByteArrayInputStream(badCrcStream));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testConcatenatedGarbageThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(EMPTY_BZIP2_STREAM);
        baos.write(new byte[] { 'J', 'U', 'N', 'K' });

        try (BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(
                new ByteArrayInputStream(baos.toByteArray()), true)) {
            bzip2.read();
        }
    }

    /* ====================================================================
     * PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
     * ==================================================================== */

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadAfterCloseThrowsException() throws IOException {
        BZip2CompressorInputStream bzip2 = createEmptyStream(false);
        bzip2.close();
        bzip2.read();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadBufferAfterCloseThrowsException() throws IOException {
        BZip2CompressorInputStream bzip2 = createEmptyStream(false);
        bzip2.close();
        bzip2.read(new byte[10], 0, 5);
    }

    @Test(timeout = 4000)
    public void testDoubleCloseIsIdempotent() throws IOException {
        BZip2CompressorInputStream bzip2 = createEmptyStream(false);
        bzip2.close();
        bzip2.close(); // Should not throw exception
    }

    @Test(timeout = 4000)
    public void testCloseClosesUnderlyingStream() throws IOException {
        final boolean[] underlyingClosed = new boolean[] { false };
        InputStream in = new ByteArrayInputStream(EMPTY_BZIP2_STREAM) {
            @Override
            public void close() throws IOException {
                underlyingClosed[0] = true;
                super.close();
            }
        };

        BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream(in);
        bzip2.close();
        assertTrue("Underlying stream must be closed when bzip2 stream closes", underlyingClosed[0]);
    }
}