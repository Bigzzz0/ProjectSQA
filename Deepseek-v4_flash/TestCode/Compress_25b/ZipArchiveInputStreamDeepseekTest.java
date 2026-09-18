package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * White-box test suite for ZipArchiveInputStream targeting the known defect
 * where reading the first stored entry returns incorrect data (first byte 0 instead of 100).
 * 
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (getNextZipEntry, read, closeEntry)
 * - Partition B: Boundary values (empty entries, max sizes, null/empty arrays)
 * - Partition C: Defect-targeted branch (stored entry with data descriptor, first entry)
 * - Partition D: Exception paths (closed stream, invalid arguments, unsupported features)
 * - Partition E: Object lifecycle (close, multiple entries, state transitions)
 * 
 * Targeted branches:
 * - readStored: hasDataDescriptor true/false, lastStoredEntry null/not null
 * - readFirstLocalFileHeader: DD_SIG, SINGLE_SEGMENT_SPLIT_MARKER
 * - processZip64Extra: z64 != null, sizes magic
 * - supportsDataDescriptorFor: various combinations
 * - closeEntry: bytesReadFromStream <= compressedSize, hasDataDescriptor
 * - bufferContainsSignature: LFH/CFH/DD detection
 * - readDataDescriptor: signature present/absent, zip64 sizes
 */
public class ZipArchiveInputStreamDeepseekTest {

    // Helper to create a minimal stored entry with data descriptor (bit 3 set)
    private byte[] createStoredEntryWithDataDescriptor(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Local file header
        writeWord(bos, ZipLong.LFH_SIG); // signature
        writeShort(bos, 20); // version needed (2.0)
        writeShort(bos, 0x0008); // general purpose bit flag: data descriptor
        writeShort(bos, ZipEntry.STORED); // compression method
        writeDword(bos, 0); // last mod time/date (dummy)
        writeDword(bos, 0); // crc-32 placeholder (0)
        writeDword(bos, 0); // compressed size placeholder (0)
        writeDword(bos, 0); // uncompressed size placeholder (0)
        writeShort(bos, 0); // file name length
        writeShort(bos, 0); // extra field length
        // No file name or extra
        // Entry data
        bos.write(data);
        // Data descriptor (without signature)
        CRC32 crc = new CRC32();
        crc.update(data);
        writeDword(bos, (int) crc.getValue()); // crc-32
        writeDword(bos, data.length); // compressed size
        writeDword(bos, data.length); // uncompressed size
        // End of central directory (minimal)
        writeWord(bos, ZipLong.CFH_SIG); // central directory header signature
        writeShort(bos, 20); // version made by
        writeShort(bos, 20); // version needed
        writeShort(bos, 0); // general purpose bit flag
        writeShort(bos, ZipEntry.STORED); // compression method
        writeDword(bos, 0); // last mod time/date
        writeDword(bos, (int) crc.getValue()); // crc
        writeDword(bos, data.length); // compressed size
        writeDword(bos, data.length); // uncompressed size
        writeShort(bos, 0); // file name length
        writeShort(bos, 0); // extra field length
        writeShort(bos, 0); // file comment length
        writeShort(bos, 0); // disk number start
        writeShort(bos, 0); // internal file attributes
        writeDword(bos, 0); // external file attributes
        writeDword(bos, 0); // relative offset of local header
        // End of central directory
        writeWord(bos, ZipLong.EOCD_SIG);
        writeShort(bos, 0); // disk number
        writeShort(bos, 0); // disk with central directory
        writeShort(bos, 1); // total entries on disk
        writeShort(bos, 1); // total entries
        writeDword(bos, bos.size() - 22 - 4); // size of central directory (excluding EOCD)
        writeDword(bos, 0); // offset of central directory (after LFH + data + DD)
        writeShort(bos, 0); // comment length
        return bos.toByteArray();
    }

    // Helper to create a simple stored entry without data descriptor
    private byte[] createSimpleStoredEntry(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CRC32 crc = new CRC32();
        crc.update(data);
        // Local file header
        writeWord(bos, ZipLong.LFH_SIG);
        writeShort(bos, 20);
        writeShort(bos, 0); // no data descriptor
        writeShort(bos, ZipEntry.STORED);
        writeDword(bos, 0); // time/date
        writeDword(bos, (int) crc.getValue());
        writeDword(bos, data.length);
        writeDword(bos, data.length);
        writeShort(bos, 0); // file name length
        writeShort(bos, 0); // extra field length
        // data
        bos.write(data);
        // Central directory
        writeWord(bos, ZipLong.CFH_SIG);
        writeShort(bos, 20);
        writeShort(bos, 20);
        writeShort(bos, 0);
        writeShort(bos, ZipEntry.STORED);
        writeDword(bos, 0);
        writeDword(bos, (int) crc.getValue());
        writeDword(bos, data.length);
        writeDword(bos, data.length);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        // EOCD
        writeWord(bos, ZipLong.EOCD_SIG);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 1);
        writeShort(bos, 1);
        writeDword(bos, 46); // size of central directory
        writeDword(bos, 30 + data.length); // offset of central directory
        writeShort(bos, 0);
        return bos.toByteArray();
    }

    private void writeWord(ByteArrayOutputStream bos, byte[] bytes) throws IOException {
        bos.write(bytes);
    }

    private void writeShort(ByteArrayOutputStream bos, int value) throws IOException {
        bos.write(ZipShort.getBytes(value));
    }

    private void writeDword(ByteArrayOutputStream bos, long value) throws IOException {
        bos.write(ZipLong.getBytes(value));
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testReadSimpleStoredEntry() throws IOException {
        byte[] data = {100, 101, 102};
        byte[] zip = createSimpleStoredEntry(data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(ZipEntry.STORED, entry.getMethod());
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(data.length, read);
            byte[] actual = new byte[read];
            System.arraycopy(buf, 0, actual, 0, read);
            assertArrayEquals(data, actual);
            assertEquals(-1, zis.read(buf, 0, 1));
        }
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntry() throws IOException {
        // Create a deflated entry using ZipArchiveOutputStream for simplicity
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("test");
            entry.setMethod(ZipEntry.DEFLATED);
            byte[] data = "Hello World".getBytes("UTF-8");
            entry.setSize(data.length);
            zos.putArchiveEntry(entry);
            zos.write(data);
            zos.closeArchiveEntry();
        }
        byte[] zip = bos.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(ZipEntry.DEFLATED, entry.getMethod());
            byte[] buf = new byte[1024];
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            int read;
            while ((read = zis.read(buf, 0, buf.length)) != -1) {
                result.write(buf, 0, read);
            }
            assertArrayEquals("Hello World".getBytes("UTF-8"), result.toByteArray());
        }
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        byte[] data1 = {1, 2, 3};
        byte[] data2 = {4, 5, 6};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // First entry
        byte[] entry1 = createSimpleStoredEntry(data1);
        bos.write(entry1);
        // Second entry (need to adjust offsets, but for simplicity we just concatenate two archives? Not correct.
        // Instead, we'll build a proper multi-entry archive using ZipArchiveOutputStream
        // But to keep it simple, we'll use the helper that creates a single entry.
        // For multi-entry, we'll rely on ZipArchiveOutputStream.
        // Actually, we can manually construct a multi-entry archive.
        // Let's use ZipArchiveOutputStream for correctness.
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos2)) {
            ZipArchiveEntry e1 = new ZipArchiveEntry("first");
            e1.setMethod(ZipEntry.STORED);
            e1.setSize(data1.length);
            CRC32 crc = new CRC32();
            crc.update(data1);
            e1.setCrc(crc.getValue());
            zos.putArchiveEntry(e1);
            zos.write(data1);
            zos.closeArchiveEntry();

            ZipArchiveEntry e2 = new ZipArchiveEntry("second");
            e2.setMethod(ZipEntry.STORED);
            e2.setSize(data2.length);
            crc.reset();
            crc.update(data2);
            e2.setCrc(crc.getValue());
            zos.putArchiveEntry(e2);
            zos.write(data2);
            zos.closeArchiveEntry();
        }
        byte[] zip = bos2.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("first", e1.getName());
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(data1.length, read);
            byte[] actual1 = new byte[read];
            System.arraycopy(buf, 0, actual1, 0, read);
            assertArrayEquals(data1, actual1);

            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("second", e2.getName());
            read = zis.read(buf, 0, buf.length);
            assertEquals(data2.length, read);
            byte[] actual2 = new byte[read];
            System.arraycopy(buf, 0, actual2, 0, read);
            assertArrayEquals(data2, actual2);

            assertNull(zis.getNextZipEntry());
        }
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testEmptyStoredEntry() throws IOException {
        byte[] data = new byte[0];
        byte[] zip = createSimpleStoredEntry(data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(-1, read);
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeOffset() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            try {
                zis.read(new byte[10], -1, 5);
                fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeLength() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            try {
                zis.read(new byte[10], 0, -1);
                fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testReadWithOffsetExceedingBufferLength() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            try {
                zis.read(new byte[10], 11, 1);
                fail("Expected ArrayIndexOutOfBoundsException");
            } catch (ArrayIndexOutOfBoundsException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testSkipNegative() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            try {
                zis.skip(-1);
                fail("Expected IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // expected
            }
        }
    }

    // ========== Partition C: Defect-Targeted Branch ==========

    @Test(timeout = 4000)
    public void testReadFirstStoredEntryWithDataDescriptor() throws IOException {
        // This test targets the known defect: reading first stored entry with data descriptor
        // yields first byte 0 instead of the actual value.
        byte[] data = {100, 101, 102};
        byte[] zip = createStoredEntryWithDataDescriptor(data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip), null, true, true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(ZipEntry.STORED, entry.getMethod());
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(data.length, read);
            byte[] actual = new byte[read];
            System.arraycopy(buf, 0, actual, 0, read);
            assertArrayEquals("First byte should be 100, not 0", data, actual);
        }
    }

    @Test(timeout = 4000)
    public void testReadFirstStoredEntryWithDataDescriptorWithoutAllow() throws IOException {
        // When allowStoredEntriesWithDataDescriptor is false, reading should throw
        byte[] data = {100};
        byte[] zip = createStoredEntryWithDataDescriptor(data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip), null, true, false)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            try {
                zis.read(new byte[1024], 0, 1024);
                fail("Expected UnsupportedZipFeatureException");
            } catch (UnsupportedZipFeatureException e) {
                // expected
            }
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadOnClosedStream() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.close();
        zis.read(new byte[10], 0, 10);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testGetNextEntryOnClosedStream() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.close();
        zis.getNextZipEntry();
    }

    @Test(timeout = 4000)
    public void testUnsupportedCompressionMethod() throws IOException {
        // Create a local file header with method 99 (unsupported)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        writeWord(bos, ZipLong.LFH_SIG);
        writeShort(bos, 20);
        writeShort(bos, 0);
        writeShort(bos, 99); // unsupported method
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        // Central directory and EOCD (minimal)
        writeWord(bos, ZipLong.CFH_SIG);
        writeShort(bos, 20);
        writeShort(bos, 20);
        writeShort(bos, 0);
        writeShort(bos, 99);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        writeWord(bos, ZipLong.EOCD_SIG);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 1);
        writeShort(bos, 1);
        writeDword(bos, 46);
        writeDword(bos, 30);
        writeShort(bos, 0);
        byte[] zip = bos.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            try {
                zis.read(new byte[1024], 0, 1024);
                fail("Expected UnsupportedZipFeatureException");
            } catch (UnsupportedZipFeatureException e) {
                // expected
            }
        }
    }

    @Test(timeout = 4000)
    public void testTruncatedZip() throws IOException {
        byte[] zip = new byte[]{0x50, 0x4b, 0x03, 0x04}; // just LFH signature
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            try {
                zis.getNextZipEntry();
                fail("Expected EOFException");
            } catch (IOException e) {
                // expected (EOFException or ZipException)
            }
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testCloseMultipleTimes() throws IOException {
        byte[] zip = createSimpleStoredEntry(new byte[]{1});
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        zis.close();
        zis.close(); // should not throw
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() throws IOException {
        ZipArchiveEntry stored = new ZipArchiveEntry("test");
        stored.setMethod(ZipEntry.STORED);
        ZipArchiveEntry deflated = new ZipArchiveEntry("test2");
        deflated.setMethod(ZipEntry.DEFLATED);
        ZipArchiveEntry unsupported = new ZipArchiveEntry("test3");
        unsupported.setMethod(99);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertTrue(zis.canReadEntryData(stored));
            assertTrue(zis.canReadEntryData(deflated));
            assertFalse(zis.canReadEntryData(unsupported));
            assertFalse(zis.canReadEntryData(new ArchiveEntry() {
                @Override
                public String getName() { return "dummy"; }
                @Override
                public long getSize() { return 0; }
                @Override
                public boolean isDirectory() { return false; }
            }));
        }
    }

    @Test(timeout = 4000)
    public void testMatches() {
        assertTrue(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x03, 0x04}, 4));
        assertTrue(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x05, 0x06}, 4));
        assertTrue(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x07, 0x08}, 4));
        assertTrue(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x07, 0x08}, 4));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x00, 0x00, 0x00, 0x00}, 4));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b}, 2));
    }

    @Test(timeout = 4000)
    public void testSkipAfterReadingEntry() throws IOException {
        byte[] data = {1, 2, 3, 4, 5};
        byte[] zip = createSimpleStoredEntry(data);
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            zis.getNextZipEntry();
            long skipped = zis.skip(2);
            assertEquals(2, skipped);
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(3, read);
            assertEquals(3, buf[0]);
            assertEquals(4, buf[1]);
            assertEquals(5, buf[2]);
        }
    }

    @Test(timeout = 4000)
    public void testReadStoredEntryWithDataDescriptorAndZip64() throws IOException {
        // Create a stored entry with data descriptor and Zip64 extra field
        // For simplicity, we'll use a small size but set the magic values.
        // This tests processZip64Extra branch.
        byte[] data = {100};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Local file header
        writeWord(bos, ZipLong.LFH_SIG);
        writeShort(bos, 45); // version needed for Zip64
        writeShort(bos, 0x0008); // data descriptor
        writeShort(bos, ZipEntry.STORED);
        writeDword(bos, 0);
        writeDword(bos, 0xFFFFFFFFL); // crc magic
        writeDword(bos, 0xFFFFFFFFL); // compressed size magic
        writeDword(bos, 0xFFFFFFFFL); // uncompressed size magic
        writeShort(bos, 0); // file name length
        writeShort(bos, 20); // extra field length (Zip64 extra)
        // Zip64 extra field
        writeShort(bos, 0x0001); // header ID
        writeShort(bos, 16); // data size (size + compressed size)
        writeLong(bos, data.length); // uncompressed size
        writeLong(bos, data.length); // compressed size
        // No file name
        bos.write(data);
        // Data descriptor (without signature)
        CRC32 crc = new CRC32();
        crc.update(data);
        writeDword(bos, (int) crc.getValue());
        writeLong(bos, data.length); // compressed size (8 bytes)
        writeLong(bos, data.length); // uncompressed size (8 bytes)
        // Central directory (minimal)
        writeWord(bos, ZipLong.CFH_SIG);
        writeShort(bos, 45);
        writeShort(bos, 45);
        writeShort(bos, 0);
        writeShort(bos, ZipEntry.STORED);
        writeDword(bos, 0);
        writeDword(bos, (int) crc.getValue());
        writeDword(bos, 0xFFFFFFFFL);
        writeDword(bos, 0xFFFFFFFFL);
        writeShort(bos, 0);
        writeShort(bos, 20);
        // Zip64 extra in central directory
        writeShort(bos, 0x0001);
        writeShort(bos, 16);
        writeLong(bos, data.length);
        writeLong(bos, data.length);
        writeShort(bos, 0); // comment length
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeDword(bos, 0);
        writeDword(bos, 0);
        // EOCD
        writeWord(bos, ZipLong.EOCD_SIG);
        writeShort(bos, 0);
        writeShort(bos, 0);
        writeShort(bos, 1);
        writeShort(bos, 1);
        writeDword(bos, 46 + 20); // size of central directory
        writeDword(bos, 30 + 20 + data.length + 20); // offset (LFH + extra + data + DD)
        writeShort(bos, 0);
        byte[] zip = bos.toByteArray();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(
                new ByteArrayInputStream(zip), null, true, true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(data.length, entry.getSize());
            assertEquals(data.length, entry.getCompressedSize());
            byte[] buf = new byte[1024];
            int read = zis.read(buf, 0, buf.length);
            assertEquals(data.length, read);
            assertEquals(100, buf[0]);
        }
    }

    private void writeLong(ByteArrayOutputStream bos, long value) throws IOException {
        bos.write(ZipEightByteInteger.getBytes(value));
    }
}