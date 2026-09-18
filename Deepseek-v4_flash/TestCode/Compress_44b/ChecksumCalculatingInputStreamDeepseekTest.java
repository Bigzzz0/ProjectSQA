package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * White-box test suite for ChecksumCalculatingInputStream.
 * Targets all branches and the known defect: constructor does not throw NullPointerException for null arguments.
 *
 * [Branch & Defect Analysis Matrix]
 * Branches in read()/read(byte[],int,int): if (ret >= 0) -> true/false
 * Branch in skip(): if (read() >= 0) -> true/false  (read() can return -1 or >=0)
 * Defect: constructor does not validate null parameters -> expected NullPointerException is not thrown.
 *
 * Partitions:
 * A: Core functional (single byte, byte array, offset, skip, getValue)
 * B: Boundary (empty stream, zero-length read, max array)
 * C: Defect-targeted (null arguments in constructor)
 * D: Exception paths (null byte array, negative offset, invalid len - but these are delegated to underlying stream)
 * E: Lifecycle (repeated read, consistent checksum)
 */
public class ChecksumCalculatingInputStreamDeepseekTest {

    // ============================================================
    // Partition C: Defect-Targeted – constructor null parameter handling
    // Known defect: constructor should throw NullPointerException but does not.
    // ============================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullChecksum() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        new ChecksumCalculatingInputStream(null, in);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullInputStream() throws IOException {
        Checksum cs = new CRC32();
        new ChecksumCalculatingInputStream(cs, null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorBothNull() throws IOException {
        new ChecksumCalculatingInputStream(null, null);
    }

    // ============================================================
    // Partition A: Core functional logic & state transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testReadSingleByteUpdatesChecksum() throws IOException {
        byte[] data = {0x01, 0x02, 0x03};
        Checksum cs = new CRC32();
        Checksum expectedCs = new CRC32();
        expectedCs.update(data, 0, data.length);

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            int b;
            while ((b = cis.read()) != -1) {
                // consume
            }
            assertEquals(expectedCs.getValue(), cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadByteArrayUpdatesChecksum() throws IOException {
        byte[] data = "Hello World".getBytes("UTF-8");
        Checksum cs = new CRC32();
        Checksum expectedCs = new CRC32();
        expectedCs.update(data, 0, data.length);

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            byte[] buf = new byte[1024];
            int len = cis.read(buf);
            assertEquals(data.length, len);
            assertEquals(expectedCs.getValue(), cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadByteArrayWithOffset() throws IOException {
        byte[] data = {0x0A, 0x0B, 0x0C, 0x0D};
        Checksum cs = new Adler32();
        Checksum expectedCs = new Adler32();
        // Read only bytes 1 and 2 (0x0B, 0x0C) into offset 1 of buf
        byte[] buf = new byte[10];
        expectedCs.update(data, 1, 2);

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            int len = cis.read(buf, 1, 2);
            assertEquals(2, len);
            assertEquals(expectedCs.getValue(), cis.getValue());
            // Verify buf content
            assertEquals(0x0B, buf[1]);
            assertEquals(0x0C, buf[2]);
        }
    }

    @Test(timeout = 4000)
    public void testSkipSingleByte() throws IOException {
        byte[] data = {0x42};
        Checksum cs = new CRC32();
        Checksum expectedCs = new CRC32();
        expectedCs.update((int)0x42);  // update with low byte

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            long skipped = cis.skip(1);
            assertEquals(1, skipped);
            assertEquals(expectedCs.getValue(), cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testSkipAtEndReturnsZero() throws IOException {
        try (InputStream raw = new ByteArrayInputStream(new byte[0]);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            cis.read(); // returns -1, so skip should return 0
            assertEquals(0, cis.skip(1));
        }
    }

    @Test(timeout = 4000)
    public void testGetValueAfterPartialRead() throws IOException {
        byte[] data = {0x10, 0x20, 0x30};
        Checksum cs = new CRC32();
        Checksum partialCs = new CRC32();
        partialCs.update(0x10);

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            cis.read(); // read one byte
            assertEquals(partialCs.getValue(), cis.getValue());
        }
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testEmptyStreamReadMinusOne() throws IOException {
        try (InputStream raw = new ByteArrayInputStream(new byte[0]);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            assertEquals(-1, cis.read());
            assertEquals(0L, cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadEmptyByteArray() throws IOException {
        byte[] data = {0x01};
        Checksum cs = new CRC32();
        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            byte[] empty = new byte[0];
            int ret = cis.read(empty, 0, 0);
            // The underlying read returns 0 when len is 0 (per InputStream spec)
            assertEquals(0, ret);
            // Checksum should not be updated because ret == 0 (0 >= 0 is true, but update with len 0 is harmless)
            assertEquals(0L, cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReadNullByteArrayThrowsNPE() throws IOException {
        try (InputStream raw = new ByteArrayInputStream(new byte[1]);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            try {
                cis.read((byte[]) null);
                fail("Expected NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadNullByteArrayWithOffsetThrowsNPE() throws IOException {
        try (InputStream raw = new ByteArrayInputStream(new byte[1]);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            try {
                cis.read(null, 0, 1);
                fail("Expected NullPointerException");
            } catch (NullPointerException e) {
                // expected
            }
        }
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // (constructor null already covered, also negative offset/length)
    // ============================================================ 

    @Test(timeout = 4000)
    public void testReadNegativeOffset() throws IOException {
        byte[] data = "test".getBytes();
        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            try {
                cis.read(new byte[10], -1, 5);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // expected from underlying stream
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadNegativeLength() throws IOException {
        byte[] data = "test".getBytes();
        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            try {
                cis.read(new byte[10], 0, -1);
                fail("Expected IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                // expected
            }
        }
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // (no equals/hashCode, but verify repeated reads on same stream)
    // ============================================================

    @Test(timeout = 4000)
    public void testMultipleReadsCumulativeChecksum() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        Checksum cs = new CRC32();
        Checksum expected = new CRC32();
        expected.update(data, 0, data.length);

        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(cs, raw)) {
            byte[] buf = new byte[33];
            int total = 0;
            int len;
            while ((len = cis.read(buf)) != -1) {
                total += len;
            }
            assertEquals(data.length, total);
            assertEquals(expected.getValue(), cis.getValue());
        }
    }

    @Test(timeout = 4000)
    public void testReuseGetValue() throws IOException {
        byte[] data = "ABC".getBytes();
        try (InputStream raw = new ByteArrayInputStream(data);
             ChecksumCalculatingInputStream cis = new ChecksumCalculatingInputStream(new CRC32(), raw)) {
            cis.read();            // value after first byte
            long v1 = cis.getValue();
            cis.read();            // after second
            long v2 = cis.getValue();
            cis.read();            // after third
            long v3 = cis.getValue();
            cis.read();            // returns -1, no change
            long v4 = cis.getValue();
            assertTrue(v1 != v2);
            assertTrue(v2 != v3);
            assertEquals(v3, v4);
        }
    }

    // Helper to close resources – not needed because try-with-resources is used.
}