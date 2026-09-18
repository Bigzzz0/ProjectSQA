package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtilsDeepseekTest {

    /* 
     * [Branch & Defect Analysis Matrix]
     * 
     * Target: IOUtils (copy, skip, readFully, toByteArray, closeQuietly)
     * 
     * Defect: skip() does not fall back to read() when skip() returns 0,
     * causing incorrect skip count. Tests from Defects4J:
     *   - skipUsingRead: expects 10 but gets 0
     *   - skipUsingSkipAndRead: expects 10 but gets 5
     * 
     * Partitions:
     * A - Core functional: copy small/large data, readFully exact/partial, toByteArray
     * B - BVA boundaries: empty streams, max buffer sizes, null (closeQuietly only)
     * C - Defect-targeted: skip with zero-skip stream, read fallback
     * D - Exception paths: readFully with invalid offset/len (IndexOutOfBoundsException)
     * E - Lifecycle: closeQuietly with null, already closed, etc.
     */

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCopySmallData() throws IOException {
        byte[] data = "Hello".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out);
        assertEquals(data.length, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testCopyLargeData() throws IOException {
        // Use buffer size slightly larger than internal buffer
        int size = 10000;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) data[i] = (byte) (i % 256);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out);
        assertEquals(size, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testCopyCustomBufferSize() throws IOException {
        byte[] data = "TestData".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out, 16);
        assertEquals(data.length, copied);
        assertArrayEquals(data, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadFullyExact() throws IOException {
        byte[] data = "FullRead".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[data.length];
        int read = IOUtils.readFully(in, buf);
        assertEquals(data.length, read);
        assertArrayEquals(data, buf);
    }

    @Test(timeout = 4000)
    public void testReadFullyPartial() throws IOException {
        byte[] data = "Partial".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[20]; // larger buffer
        int read = IOUtils.readFully(in, buf);
        assertEquals(data.length, read);
        // Only first data.length bytes are filled
        for (int i = 0; i < data.length; i++) assertEquals(data[i], buf[i]);
    }

    @Test(timeout = 4000)
    public void testReadFullyWithOffsetLen() throws IOException {
        byte[] data = "OffsetTest".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[20];
        int read = IOUtils.readFully(in, buf, 5, 5);
        assertEquals(5, read);
        assertEquals('O', buf[5]);
        assertEquals('f', buf[6]);
        assertEquals('f', buf[7]);
        assertEquals('s', buf[8]);
        assertEquals('e', buf[9]);
    }

    @Test(timeout = 4000)
    public void testToByteArray() throws IOException {
        byte[] data = "ToByteArrayTest".getBytes();
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte[] result = IOUtils.toByteArray(in);
        assertArrayEquals(data, result);
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCopyEmptyStream() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IOUtils.copy(in, out);
        assertEquals(0, copied);
        assertArrayEquals(new byte[0], out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testReadFullyEmptyBuffer() throws IOException {
        byte[] data = "Any".getBytes();
        InputStream in = new ByteArrayInputStream(data);
        byte[] buf = new byte[0];
        int read = IOUtils.readFully(in, buf);
        assertEquals(0, read);
    }

    @Test(timeout = 4000)
    public void testReadFullyEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        byte[] buf = new byte[10];
        int read = IOUtils.readFully(in, buf);
        assertEquals(0, read);
    }

    @Test(timeout = 4000)
    public void testToByteArrayEmpty() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        byte[] result = IOUtils.toByteArray(in);
        assertArrayEquals(new byte[0], result);
    }

    @Test(timeout = 4000)
    public void testCloseQuietlyNull() {
        // Should not throw
        IOUtils.closeQuietly(null);
    }

    @Test(timeout = 4000)
    public void testCloseQuietlyCloseable() throws IOException {
        Closeable c = new Closeable() {
            boolean closed = false;
            @Override
            public void close() throws IOException {
                closed = true;
            }
        };
        IOUtils.closeQuietly(c);
        // Can't easily assert without exposing state, but no exception is fine
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (skip fallback)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSkipWithZeroSkipStream() throws IOException {
        // Stream that always returns 0 from skip, but can be read
        InputStream zeroSkipStream = new InputStream() {
            private int pos = 0;
            private final byte[] data = "0123456789".getBytes();

            @Override
            public int read() throws IOException {
                if (pos < data.length) return data[pos++] & 0xFF;
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (pos >= data.length) return -1;
                int toRead = Math.min(len, data.length - pos);
                System.arraycopy(data, pos, b, off, toRead);
                pos += toRead;
                return toRead;
            }

            @Override
            public long skip(long n) throws IOException {
                return 0; // Always return 0 to force fallback
            }
        };

        // The skip method should fall back to read, so it should skip all 10 bytes
        long skipped = IOUtils.skip(zeroSkipStream, 10);
        assertEquals("skip should have read fallback to skip 10 bytes", 10, skipped);
    }

    @Test(timeout = 4000)
    public void testSkipWithPartialZeroSkipStream() throws IOException {
        // Stream that returns 0 once, then reads normally
        InputStream partialStream = new InputStream() {
            private int pos = 0;
            private final byte[] data = "0123456789".getBytes();
            private boolean firstSkipCalled = false;

            @Override
            public int read() throws IOException {
                if (pos < data.length) return data[pos++] & 0xFF;
                return -1;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                if (pos >= data.length) return -1;
                int toRead = Math.min(len, data.length - pos);
                System.arraycopy(data, pos, b, off, toRead);
                pos += toRead;
                return toRead;
            }

            @Override
            public long skip(long n) throws IOException {
                if (!firstSkipCalled) {
                    firstSkipCalled = true;
                    return 0; // First skip returns 0
                }
                // Subsequent skips skip some bytes
                long toSkip = Math.min(n, data.length - pos);
                pos += toSkip;
                return toSkip;
            }
        };

        long skipped = IOUtils.skip(partialStream, 10);
        // Should still skip all 10 bytes (first failed skip triggers read fallback? Actually the current code breaks on 0)
        // The defect causes only 5 to be skipped (as in Defects4J test: expected 10, got 5)
        // Our test expects the correct behavior: 10
        // On defective code, this will fail with <10> but was <5>
        assertEquals("skip should eventually skip all 10 bytes via fallback", 10, skipped);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeLen() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        byte[] buf = new byte[10];
        IOUtils.readFully(in, buf, 0, -1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadFullyNegativeOffset() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        byte[] buf = new byte[10];
        IOUtils.readFully(in, buf, -1, 5);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadFullyOffsetPlusLenExceedsLength() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[10]);
        byte[] buf = new byte[10];
        IOUtils.readFully(in, buf, 8, 5); // 8+5=13 > 10
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    // IOUtils is a utility class with private constructor – no lifecycle test needed.
    // But closeQuietly can be tested with an IOException-throwing closeable.

    @Test(timeout = 4000)
    public void testCloseQuietlySwallowsIOException() {
        Closeable badClose = new Closeable() {
            @Override
            public void close() throws IOException {
                throw new IOException("Expected");
            }
        };
        // Should not throw
        IOUtils.closeQuietly(badClose);
    }

    // Additional boundary: copy with null input (NullPointerException expected)
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testCopyNullInput() throws IOException {
        IOUtils.copy(null, new ByteArrayOutputStream());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testCopyNullOutput() throws IOException {
        IOUtils.copy(new ByteArrayInputStream(new byte[1]), null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToByteArrayNullInput() throws IOException {
        IOUtils.toByteArray(null);
    }
}