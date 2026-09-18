package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 * White-box test suite for ZipArchiveInputStream targeting the known defect
 * where reading from a truncated entry does not throw an IOException.
 *
 * [Branch & Defect Analysis Matrix]
 * - LFH parsing: signature check, general purpose flag (EFS, data descriptor),
 *   compression method, timestamps, sizes, filename/extra lengths.
 * - read(): STORED vs DEFLATED, buffer boundaries, finished stream, closed stream,
 *   invalid arguments (ArrayIndexOutOfBounds).
 * - closeEntry(): pushback logic, data descriptor reading, reset state.
 * - skip(): negative value throws IllegalArgumentException.
 * - matches(): signature length check, LFH and EOCD signatures.
 * - Defect: truncated entry (less data than declared size) should throw IOException
 *   but the buggy version may return -1 or silently succeed.
 */
public class ZipArchiveInputStreamDeepseekTest {

    // ==================== Helper: build minimal zip entry bytes ====================
    private static byte[] buildStoredEntry(String name, byte[] content) {
        try {
            byte[] nameBytes = name.getBytes("UTF-8");
            int nameLen = nameBytes.length;
            int extraLen = 0;
            int totalLen = 30 + nameLen + extraLen + content.length;
            byte[] buf = new byte[totalLen];
            int off = 0;
            // local file header signature
            System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, buf, off, 4);
            off += 4;
            // version needed to extract (2.0)
            System.arraycopy(ZipShort.getBytes(20), 0, buf, off, 2);
            off += 2;
            // general purpose bit flag (no EFS, no data descriptor)
            System.arraycopy(ZipShort.getBytes(0), 0, buf, off, 2);
            off += 2;
            // compression method: STORED (0)
            System.arraycopy(ZipShort.getBytes(0), 0, buf, off, 2);
            off += 2;
            // last mod file time (dummy)
            System.arraycopy(ZipLong.getBytes(0), 0, buf, off, 4);
            off += 4;
            // crc-32
            CRC32 crc = new CRC32();
            crc.update(content);
            System.arraycopy(ZipLong.getBytes(crc.getValue()), 0, buf, off, 4);
            off += 4;
            // compressed size
            System.arraycopy(ZipLong.getBytes(content.length), 0, buf, off, 4);
            off += 4;
            // uncompressed size
            System.arraycopy(ZipLong.getBytes(content.length), 0, buf, off, 4);
            off += 4;
            // file name length
            System.arraycopy(ZipShort.getBytes(nameLen), 0, buf, off, 2);
            off += 2;
            // extra field length
            System.arraycopy(ZipShort.getBytes(extraLen), 0, buf, off, 2);
            off += 2;
            // file name
            System.arraycopy(nameBytes, 0, buf, off, nameLen);
            off += nameLen;
            // extra field (none)
            // file data
            System.arraycopy(content, 0, buf, off, content.length);
            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] buildDeflatedEntry(String name, byte[] uncompressed) {
        try {
            byte[] nameBytes = name.getBytes("UTF-8");
            int nameLen = nameBytes.length;
            int extraLen = 0;
            // compress
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            deflater.setInput(uncompressed);
            deflater.finish();
            byte[] compressed = new byte[uncompressed.length + 64];
            int compLen = deflater.deflate(compressed);
            deflater.end();
            byte[] compData = new byte[compLen];
            System.arraycopy(compressed, 0, compData, 0, compLen);

            int totalLen = 30 + nameLen + extraLen + compData.length;
            byte[] buf = new byte[totalLen];
            int off = 0;
            System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, buf, off, 4);
            off += 4;
            System.arraycopy(ZipShort.getBytes(20), 0, buf, off, 2);
            off += 2;
            // general purpose bit flag: bit 3 (data descriptor) set? No, we provide sizes.
            // For simplicity, set bit 3 to 0 and provide sizes.
            System.arraycopy(ZipShort.getBytes(0), 0, buf, off, 2);
            off += 2;
            // compression method: DEFLATED (8)
            System.arraycopy(ZipShort.getBytes(8), 0, buf, off, 2);
            off += 2;
            System.arraycopy(ZipLong.getBytes(0), 0, buf, off, 4);
            off += 4;
            // crc-32
            CRC32 crc = new CRC32();
            crc.update(uncompressed);
            System.arraycopy(ZipLong.getBytes(crc.getValue()), 0, buf, off, 4);
            off += 4;
            // compressed size
            System.arraycopy(ZipLong.getBytes(compData.length), 0, buf, off, 4);
            off += 4;
            // uncompressed size
            System.arraycopy(ZipLong.getBytes(uncompressed.length), 0, buf, off, 4);
            off += 4;
            // file name length
            System.arraycopy(ZipShort.getBytes(nameLen), 0, buf, off, 2);
            off += 2;
            // extra field length
            System.arraycopy(ZipShort.getBytes(extraLen), 0, buf, off, 2);
            off += 2;
            System.arraycopy(nameBytes, 0, buf, off, nameLen);
            off += nameLen;
            System.arraycopy(compData, 0, buf, off, compData.length);
            return buf;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello, World!".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull("Entry should not be null", entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        byte[] readBuf = new byte[1024];
        int totalRead = 0;
        int n;
        while ((n = zis.read(readBuf, 0, readBuf.length)) != -1) {
            totalRead += n;
        }
        assertEquals("Should read full content", content.length, totalRead);
        assertArrayEquals(content, java.util.Arrays.copyOf(readBuf, totalRead));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntry() throws IOException {
        byte[] content = "Compressed data test.".getBytes("UTF-8");
        byte[] zipData = buildDeflatedEntry("compressed.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("compressed.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());
        byte[] readBuf = new byte[1024];
        int totalRead = 0;
        int n;
        while ((n = zis.read(readBuf, 0, readBuf.length)) != -1) {
            totalRead += n;
        }
        assertEquals("Should decompress to original length", content.length, totalRead);
        assertArrayEquals(content, java.util.Arrays.copyOf(readBuf, totalRead));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkip() throws IOException {
        byte[] content = "Skip me!".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("skip.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        long skipped = zis.skip(5);
        assertEquals(5, skipped);
        byte[] remaining = new byte[content.length - 5];
        int n = zis.read(remaining, 0, remaining.length);
        assertEquals(remaining.length, n);
        assertArrayEquals(" me!".getBytes("UTF-8"), remaining);
        zis.close();
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        byte[] content1 = "First".getBytes("UTF-8");
        byte[] content2 = "Second".getBytes("UTF-8");
        byte[] zip1 = buildStoredEntry("a.txt", content1);
        byte[] zip2 = buildStoredEntry("b.txt", content2);
        byte[] combined = new byte[zip1.length + zip2.length];
        System.arraycopy(zip1, 0, combined, 0, zip1.length);
        System.arraycopy(zip2, 0, combined, zip1.length, zip2.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(combined));
        ZipArchiveEntry e1 = zis.getNextZipEntry();
        assertNotNull(e1);
        assertEquals("a.txt", e1.getName());
        byte[] buf = new byte[1024];
        int n = zis.read(buf, 0, buf.length);
        assertEquals(content1.length, n);
        ZipArchiveEntry e2 = zis.getNextZipEntry();
        assertNotNull(e2);
        assertEquals("b.txt", e2.getName());
        n = zis.read(buf, 0, buf.length);
        assertEquals(content2.length, n);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    // ==================== Partition B: Boundary & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyZip() throws IOException {
        // Only EOCD signature (empty zip)
        byte[] eocd = ZipLong.EOCD_SIG.getBytes();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(eocd));
        assertNull("Empty zip should return null entry", zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testNegativeSkip() throws IOException {
        byte[] content = "test".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("x.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        try {
            zis.skip(-1);
            fail("Expected IllegalArgumentException for negative skip");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidArguments() throws IOException {
        byte[] content = "data".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("x.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        byte[] buf = new byte[10];
        try {
            zis.read(buf, -1, 5);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            zis.read(buf, 0, -1);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        try {
            zis.read(buf, 5, 10);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadOnClosedStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        try {
            zis.read(new byte[1], 0, 1);
            fail("Expected IOException for closed stream");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMatches() {
        byte[] lfh = ZipLong.LFH_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(lfh, lfh.length));
        byte[] eocd = ZipLong.EOCD_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(eocd, eocd.length));
        byte[] shortSig = new byte[3];
        assertFalse(ZipArchiveInputStream.matches(shortSig, 3));
        byte[] wrongSig = new byte[4];
        assertFalse(ZipArchiveInputStream.matches(wrongSig, 4));
    }

    // ==================== Partition C: Defect-Targeted (Truncated Entry) ====================

    @Test(timeout = 4000)
    public void testReadFromTruncatedStoredEntry() throws IOException {
        // Build a stored entry with declared size larger than actual data
        byte[] content = "short".getBytes("UTF-8");
        byte[] fullZip = buildStoredEntry("trunc.txt", content);
        // Truncate the data part: keep only header + first 2 bytes of content
        int headerLen = 30 + "trunc.txt".getBytes("UTF-8").length; // no extra
        byte[] truncated = new byte[headerLen + 2];
        System.arraycopy(fullZip, 0, truncated, 0, truncated.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull("Entry should be parsed", entry);
        assertEquals("trunc.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        // The declared size is content.length (5) but only 2 bytes available.
        // Reading should throw an IOException (EOFException) because the stream
        // cannot provide the full declared size.
        byte[] buf = new byte[1024];
        try {
            int n = zis.read(buf, 0, buf.length);
            // If buggy, it might return -1 or a smaller number without throwing.
            // We expect an IOException.
            fail("Expected IOException when reading truncated stored entry, but got " + n);
        } catch (IOException e) {
            // Expected: EOFException or IOException wrapping it.
            assertTrue("Exception should be IOException", e instanceof IOException);
        }
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadFromTruncatedDeflatedEntry() throws IOException {
        // Build a deflated entry with declared compressed size larger than actual data
        byte[] content = "longer data to compress".getBytes("UTF-8");
        byte[] fullZip = buildDeflatedEntry("def_trunc.txt", content);
        // Truncate the compressed data part: keep header + first few bytes of compressed data
        int headerLen = 30 + "def_trunc.txt".getBytes("UTF-8").length;
        byte[] truncated = new byte[headerLen + 5]; // only 5 bytes of compressed data
        System.arraycopy(fullZip, 0, truncated, 0, truncated.length);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("def_trunc.txt", entry.getName());
        assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());
        byte[] buf = new byte[1024];
        try {
            int n = zis.read(buf, 0, buf.length);
            // The inflater may return 0 or -1 without throwing, but the entry is truncated.
            // The bug is that it doesn't throw an IOException.
            fail("Expected IOException when reading truncated deflated entry, but got " + n);
        } catch (IOException e) {
            // Expected: ZipException or IOException from inflater.
            assertTrue("Exception should be IOException", e instanceof IOException);
        }
        zis.close();
    }

    // ==================== Partition D: Exception & Defensive Paths ====================

    @Test(timeout = 4000)
    public void testCloseEntryOnClosedStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        try {
            // Access private closeEntry via getNextZipEntry? Actually closeEntry is called internally.
            // We can trigger it by calling getNextZipEntry after close? That will throw because closed.
            // Better: we can't directly call closeEntry. But we can test that getNextZipEntry throws.
            zis.getNextZipEntry();
            fail("Expected IOException for closed stream");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryAfterCentralDirectory() throws IOException {
        // Simulate hitting central directory by providing CFH signature
        byte[] cfh = ZipLong.CFH_SIG.getBytes();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(cfh));
        assertNull("Should return null when CFH encountered", zis.getNextZipEntry());
        // Subsequent calls should also return null
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryWithInvalidSignature() throws IOException {
        byte[] invalidSig = new byte[]{0x00, 0x00, 0x00, 0x00};
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(invalidSig));
        assertNull("Invalid signature should return null", zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadAfterEntryFinished() throws IOException {
        byte[] content = "done".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("f.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        byte[] buf = new byte[1024];
        // Read all data
        while (zis.read(buf, 0, buf.length) != -1) {}
        // Next read should return -1
        assertEquals(-1, zis.read(buf, 0, buf.length));
        zis.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testDoubleClose() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.close(); // should not throw
    }

    @Test(timeout = 4000)
    public void testSkipAfterEntryFinished() throws IOException {
        byte[] content = "skip".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("s.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        // Read all
        byte[] buf = new byte[1024];
        while (zis.read(buf, 0, buf.length) != -1) {}
        // Skip should return 0
        assertEquals(0, zis.skip(10));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithEncoding() throws IOException {
        byte[] content = "test".getBytes("UTF-8");
        byte[] zipData = buildStoredEntry("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(
            new ByteArrayInputStream(zipData), "UTF-8", true);
        assertNotNull(zis.getNextZipEntry());
        zis.close();
    }
}