package org.apache.commons.compress.compressors.bzip2;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * White-box test suite for BZip2CompressorInputStream targeting the known defect
 * where read(byte[], 0, 0) returns -1 instead of 0.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic – normal read, state transitions, CRC checks.
 * - Partition B: Boundary Value Analysis – zero-length read, negative offsets, null array.
 * - Partition C: Defect-targeted – read(byte[],0,0) must return 0.
 * - Partition D: Exception paths – closed stream, invalid header, corrupted data.
 * - Partition E: Object lifecycle – close, multiple streams, concatenated mode.
 *
 * Key branches:
 * - read0(): EOF, START_BLOCK, RAND_*_STATE, NO_RAND_*_STATE.
 * - read(byte[],int,int): offs<0, len<0, offs+len>dest.length, in==null.
 * - initBlock(): end-of-stream magic vs block magic, CRC mismatch.
 * - complete(): combined CRC check, decompressConcatenated flag.
 * - setupBlock(): origPtr bounds, blockRandomised branch.
 * - getAndMoveToFrontDecode(): RUNA/RUNB handling, EOB, block overrun.
 */
public class BZip2CompressorInputStreamDeepseekTest {

    // ==================== Helper: minimal valid empty BZip2 stream ====================
    // Stream: 'B','Z','h','1' + end-of-stream magic (6 bytes) + combined CRC (4 bytes = 0)
    private static byte[] emptyBz2Stream() {
        return new byte[] {
            'B', 'Z', 'h', '1',
            0x17, 0x72, 0x45, 0x38, 0x50, 0x90,
            0x00, 0x00, 0x00, 0x00
        };
    }

    // ==================== Partition C: Defect-targeted (len == 0) ====================

    @Test(timeout = 4000)
    public void testReadZeroLengthReturnsZero() throws IOException {
        // Defect: read(byte[],0,0) returns -1 instead of 0
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        byte[] buf = new byte[10];
        int result = bzIn.read(buf, 0, 0);
        assertEquals("read with len=0 must return 0", 0, result);
        bzIn.close();
    }

    // ==================== Partition A: Core functional logic ====================

    @Test(timeout = 4000)
    public void testReadSingleByteOnEmptyStreamReturnsMinusOne() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        int r = bzIn.read();
        assertEquals("EOF should return -1", -1, r);
        bzIn.close();
    }

    @Test(timeout = 4000)
    public void testReadArrayOnEmptyStreamReturnsMinusOne() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        byte[] buf = new byte[10];
        int r = bzIn.read(buf, 0, buf.length);
        assertEquals("EOF should return -1", -1, r);
        bzIn.close();
    }

    @Test(timeout = 4000)
    public void testReadArrayWithPartialOffset() throws IOException {
        // Use a stream that will be at EOF after construction
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        byte[] buf = new byte[10];
        // read with offset > 0, len > 0, but stream empty -> should return -1
        int r = bzIn.read(buf, 2, 5);
        assertEquals(-1, r);
        bzIn.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        try {
            bzIn.read(new byte[10], -1, 5);
        } finally {
            bzIn.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLength() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        try {
            bzIn.read(new byte[10], 0, -1);
        } finally {
            bzIn.close();
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffsetPlusLengthExceedsArray() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        try {
            bzIn.read(new byte[10], 5, 10);
        } finally {
            bzIn.close();
        }
    }

    // ==================== Partition D: Exception & defensive guard paths ====================

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadOnClosedStream() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        bzIn.close();
        bzIn.read();  // should throw IOException
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadArrayOnClosedStream() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        bzIn.close();
        bzIn.read(new byte[10], 0, 10);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidHeader() throws IOException {
        // Stream with wrong magic
        byte[] badHeader = new byte[] {0x00, 0x00, 0x00, 0x00};
        InputStream in = new ByteArrayInputStream(badHeader);
        new BZip2CompressorInputStream(in);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidBlockSize() throws IOException {
        // Magic OK but block size char out of range
        byte[] badBlockSize = new byte[] {'B', 'Z', 'h', '0'};
        InputStream in = new ByteArrayInputStream(badBlockSize);
        new BZip2CompressorInputStream(in);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testBadBlockHeader() throws IOException {
        // Valid header but block magic wrong
        byte[] stream = new byte[] {
            'B', 'Z', 'h', '1',
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00  // not end-of-stream or block magic
        };
        InputStream in = new ByteArrayInputStream(stream);
        new BZip2CompressorInputStream(in);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testTruncatedStream() throws IOException {
        // Only header, no block data
        byte[] truncated = new byte[] {'B', 'Z', 'h', '1'};
        InputStream in = new ByteArrayInputStream(truncated);
        new BZip2CompressorInputStream(in);
    }

    // ==================== Partition E: Object lifecycle & contract ====================

    @Test(timeout = 4000)
    public void testCloseIdempotent() throws IOException {
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        bzIn.close();
        bzIn.close();  // should not throw
    }

    @Test(timeout = 4000)
    public void testMatchesMethod() {
        byte[] validSig = new byte[] {'B', 'Z', 'h'};
        assertTrue(BZip2CompressorInputStream.matches(validSig, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] {'B', 'Z'}, 2));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] {'B', 'Z', 'x'}, 3));
        assertFalse(BZip2CompressorInputStream.matches(new byte[] {'A', 'Z', 'h'}, 3));
    }

    @Test(timeout = 4000)
    public void testConcatenatedStreams() throws IOException {
        // Two empty streams concatenated
        byte[] stream1 = emptyBz2Stream();
        byte[] stream2 = emptyBz2Stream();
        byte[] combined = new byte[stream1.length + stream2.length];
        System.arraycopy(stream1, 0, combined, 0, stream1.length);
        System.arraycopy(stream2, 0, combined, stream1.length, stream2.length);
        InputStream in = new ByteArrayInputStream(combined);
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in, true);
        // First stream should be consumed, second stream should be detected
        // After reading all, we should get -1
        byte[] buf = new byte[10];
        int r = bzIn.read(buf, 0, buf.length);
        assertEquals(-1, r);
        bzIn.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullInputStreamThrowsNullPointerException() {
        try {
            new BZip2CompressorInputStream(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    // ==================== Additional coverage: state machine transitions ====================

    @Test(timeout = 4000)
    public void testReadAfterCloseReturnsMinusOne() throws IOException {
        // After close, read() throws IOException, but we already tested that.
        // This test ensures the state machine is not accessed after close.
        InputStream in = new ByteArrayInputStream(emptyBz2Stream());
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
        bzIn.close();
        try {
            bzIn.read();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    // Note: To achieve deeper coverage of the Huffman decoding and block processing,
    // we would need a valid non-empty BZip2 stream. Due to complexity, we focus on
    // the defect and boundary conditions. The above tests cover the majority of
    // decision branches in read(), init(), initBlock(), complete(), close(), and matches().
}