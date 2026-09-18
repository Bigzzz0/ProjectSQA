package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * Test class for ZipArchiveInputStream targeting known defects and high coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic – getNextZipEntry, read, closeEntry, skip
 * - Partition B: Boundary values – empty archive, single entry, large sizes, zero-length names
 * - Partition C: Defect-targeted – nested archive detection, invalid entry handling
 * - Partition D: Exception paths – closed stream, unsupported features, truncated data
 * - Partition E: Object lifecycle – close, multiple getNextEntry calls, state transitions
 *
 * Known defects:
 * 1. testListAllFilesWithNestedArchive: expected 1 entry but got 0 (nested archive not detected)
 * 2. testThrowOnInvalidEntry: expected IOException but not thrown
 */
public class ZipArchiveInputStreamDeepseekTest {

    // Helper: create a minimal local file header for a stored entry
    private byte[] createLocalFileHeader(String name, long compressedSize, long uncompressedSize, long crc, boolean useDataDescriptor) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // signature
            bos.write(ZipLong.LFH_SIG.getBytes());
            // version needed to extract (2.0)
            bos.write(ZipShort.getBytes(20));
            // general purpose bit flag
            int gpFlag = useDataDescriptor ? 8 : 0; // bit 3 = data descriptor
            bos.write(ZipShort.getBytes(gpFlag));
            // compression method (0 = stored)
            bos.write(ZipShort.getBytes(ZipEntry.STORED));
            // last mod file time and date (dummy)
            bos.write(ZipLong.getBytes(0));
            // crc-32
            bos.write(ZipLong.getBytes(crc));
            // compressed size
            bos.write(ZipLong.getBytes(compressedSize));
            // uncompressed size
            bos.write(ZipLong.getBytes(uncompressedSize));
            // file name length
            byte[] nameBytes = name.getBytes("UTF-8");
            bos.write(ZipShort.getBytes(nameBytes.length));
            // extra field length
            bos.write(ZipShort.getBytes(0));
            // file name
            bos.write(nameBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    // Helper: create a data descriptor (without signature)
    private byte[] createDataDescriptor(long crc, long compressedSize, long uncompressedSize) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(ZipLong.getBytes(crc));
            bos.write(ZipLong.getBytes(compressedSize));
            bos.write(ZipLong.getBytes(uncompressedSize));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    // Helper: create a central directory header for a stored entry
    private byte[] createCentralDirectoryHeader(String name, long compressedSize, long uncompressedSize, long crc, long offset) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // signature
            bos.write(ZipLong.CFH_SIG.getBytes());
            // version made by (2.0)
            bos.write(ZipShort.getBytes(20));
            // version needed to extract (2.0)
            bos.write(ZipShort.getBytes(20));
            // general purpose bit flag (no data descriptor)
            bos.write(ZipShort.getBytes(0));
            // compression method (stored)
            bos.write(ZipShort.getBytes(ZipEntry.STORED));
            // last mod file time and date
            bos.write(ZipLong.getBytes(0));
            // crc-32
            bos.write(ZipLong.getBytes(crc));
            // compressed size
            bos.write(ZipLong.getBytes(compressedSize));
            // uncompressed size
            bos.write(ZipLong.getBytes(uncompressedSize));
            // file name length
            byte[] nameBytes = name.getBytes("UTF-8");
            bos.write(ZipShort.getBytes(nameBytes.length));
            // extra field length
            bos.write(ZipShort.getBytes(0));
            // file comment length
            bos.write(ZipShort.getBytes(0));
            // disk number start
            bos.write(ZipShort.getBytes(0));
            // internal file attributes
            bos.write(ZipShort.getBytes(0));
            // external file attributes
            bos.write(ZipLong.getBytes(0));
            // relative offset of local header
            bos.write(ZipLong.getBytes(offset));
            // file name
            bos.write(nameBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    // Helper: create end of central directory record
    private byte[] createEndOfCentralDirectory(int totalEntries, long centralDirSize, long centralDirOffset) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // signature
            bos.write(ZipArchiveOutputStream.EOCD_SIG);
            // number of this disk
            bos.write(ZipShort.getBytes(0));
            // disk where central directory starts
            bos.write(ZipShort.getBytes(0));
            // total number of entries on this disk
            bos.write(ZipShort.getBytes(totalEntries));
            // total number of entries
            bos.write(ZipShort.getBytes(totalEntries));
            // size of central directory
            bos.write(ZipLong.getBytes(centralDirSize));
            // offset of start of central directory
            bos.write(ZipLong.getBytes(centralDirOffset));
            // comment length
            bos.write(ZipShort.getBytes(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    // Helper: create a complete simple ZIP with one stored entry (no data descriptor)
    private byte[] createSimpleZip(String name, byte[] content) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            CRC32 crc = new CRC32();
            crc.update(content);
            long crcValue = crc.getValue();
            long size = content.length;

            // local file header
            bos.write(createLocalFileHeader(name, size, size, crcValue, false));
            // file data
            bos.write(content);
            // central directory header
            long offset = 0; // local header starts at 0
            bos.write(createCentralDirectoryHeader(name, size, size, crcValue, offset));
            // end of central directory
            long centralDirSize = 46 + name.getBytes("UTF-8").length; // CFH size
            long centralDirOffset = 30 + name.getBytes("UTF-8").length + size; // after LFH + data
            bos.write(createEndOfCentralDirectory(1, centralDirSize, centralDirOffset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toByteArray();
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testGetNextZipEntryReturnsNullWhenClosed() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        assertNull("getNextZipEntry should return null after close", zis.getNextZipEntry());
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryReturnsNullWhenHitCentralDirectory() throws IOException {
        // Provide a valid ZIP with one entry, then read it and expect null after
        byte[] zipData = createSimpleZip("test.txt", "Hello".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull("First entry should not be null", entry);
        assertEquals("test.txt", entry.getName());
        // Read the entry data
        byte[] buffer = new byte[1024];
        int read = zis.read(buffer, 0, buffer.length);
        assertTrue("Should have read some bytes", read > 0);
        // Close entry
        zis.close();
        // After closing, getNextZipEntry should return null
        assertNull("getNextZipEntry should return null after close", zis.getNextZipEntry());
    }

    @Test(timeout = 4000)
    public void testReadStoredEntry() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipData = createSimpleZip("file.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("file.txt", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());
        byte[] buffer = new byte[1024];
        int totalRead = 0;
        int read;
        while ((read = zis.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
            totalRead += read;
        }
        assertEquals(content.length, totalRead);
        assertArrayEquals(content, java.util.Arrays.copyOf(buffer, totalRead));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipOnStoredEntry() throws IOException {
        byte[] content = "Skip this content".getBytes();
        byte[] zipData = createSimpleZip("skip.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        long skipped = zis.skip(5);
        assertEquals(5, skipped);
        // Read remaining
        byte[] buffer = new byte[1024];
        int read = zis.read(buffer, 0, buffer.length);
        assertEquals(content.length - 5, read);
        zis.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyArchive() throws IOException {
        // Create an empty ZIP (only EOCD)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(createEndOfCentralDirectory(0, 0, 0));
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull("Empty archive should return null", zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testEntryWithZeroLengthName() throws IOException {
        // Create a ZIP with empty name (should be allowed)
        byte[] content = "data".getBytes();
        CRC32 crc = new CRC32();
        crc.update(content);
        long crcValue = crc.getValue();
        long size = content.length;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // LFH with empty name
        bos.write(createLocalFileHeader("", size, size, crcValue, false));
        bos.write(content);
        // CFH with empty name
        bos.write(createCentralDirectoryHeader("", size, size, crcValue, 0));
        // EOCD
        long centralDirSize = 46; // no name
        long centralDirOffset = 30 + size;
        bos.write(createEndOfCentralDirectory(1, centralDirSize, centralDirOffset));

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("", entry.getName());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testEntryWithLargeSize() throws IOException {
        // Use a stored entry with size > 0xFFFFFFFF (Zip64) – but we won't implement full Zip64 here,
        // just test that the stream handles it gracefully (likely throws exception or reads incorrectly)
        // For simplicity, test a normal size boundary: 0xFFFFFFFF
        long largeSize = 0xFFFFFFFFL;
        // We cannot create actual data of that size, so we'll create a fake header with that size
        // and expect the stream to either read or throw.
        // This test targets the processZip64Extra branch.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // LFH with size = 0xFFFFFFFF, no data descriptor
        bos.write(createLocalFileHeader("large.txt", largeSize, largeSize, 0, false));
        // No data, just header
        // CFH
        bos.write(createCentralDirectoryHeader("large.txt", largeSize, largeSize, 0, 0));
        // EOCD
        long centralDirSize = 46 + "large.txt".getBytes().length;
        long centralDirOffset = 30 + "large.txt".getBytes().length;
        bos.write(createEndOfCentralDirectory(1, centralDirSize, centralDirOffset));

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        // The entry size should be set to 0xFFFFFFFF because no Zip64 extra field
        assertEquals(largeSize, entry.getSize());
        // Reading should fail because we don't have that much data
        try {
            byte[] buffer = new byte[1024];
            zis.read(buffer, 0, buffer.length);
            fail("Expected IOException due to truncated data");
        } catch (IOException e) {
            // expected
        }
        zis.close();
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    /**
     * Defect: testListAllFilesWithNestedArchive – expected 1 entry but got 0.
     * This test creates a ZIP that contains another ZIP as entry (nested archive).
     * The stream should be able to read the outer entry (the nested ZIP file).
     */
    @Test(timeout = 4000)
    public void testNestedArchiveDetection() throws IOException {
        // Create inner ZIP data
        byte[] innerContent = "inner file content".getBytes();
        byte[] innerZip = createSimpleZip("inner.txt", innerContent);
        // Create outer ZIP containing the inner ZIP as a stored entry
        byte[] outerZip = createSimpleZip("nested.zip", innerZip);

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(outerZip));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull("Should read the outer entry (nested.zip)", entry);
        assertEquals("nested.zip", entry.getName());
        // Read the outer entry data (the inner ZIP bytes)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = zis.read(buffer, 0, buffer.length)) != -1) {
            bos.write(buffer, 0, read);
        }
        byte[] readInnerZip = bos.toByteArray();
        assertArrayEquals("Outer entry data should be the inner ZIP", innerZip, readInnerZip);
        // After reading the outer entry, getNextZipEntry should return null (only one entry)
        assertNull("No more entries expected", zis.getNextZipEntry());
        zis.close();
    }

    /**
     * Defect: testThrowOnInvalidEntry – expected IOException but not thrown.
     * This test creates an invalid entry (e.g., corrupted local file header signature)
     * and expects getNextZipEntry to throw IOException.
     */
    @Test(timeout = 4000)
    public void testThrowOnInvalidEntry() throws IOException {
        // Create a byte array with a valid EOCD but invalid LFH (wrong signature)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Write a fake signature (not LFH)
        bos.write(new byte[] {0x01, 0x02, 0x03, 0x04});
        // Write some garbage
        bos.write(new byte[26]); // rest of LFH
        // Write EOCD to make stream think it's a valid archive? Actually the stream will try to read LFH first.
        // To trigger the defect, we need a situation where the stream reads a signature that is not LFH, CFH, or AED.
        // According to the code, if signature is not LFH, it returns null (not IOException).
        // The defect might be about an entry that uses a data descriptor but the stream fails to throw IOException when it should.
        // Let's create a stored entry with data descriptor but without allowing it.
        // The method supportsDataDescriptorFor returns false for stored entries with data descriptor if allowStoredEntriesWithDataDescriptor is false.
        // In read() method, if !supportsDataDescriptorFor, it throws UnsupportedZipFeatureException (which is an IOException subclass).
        // So we need to create a stored entry with data descriptor and then try to read it.
        // The defect might be that the exception is not thrown when expected.
        // We'll create a ZIP with a stored entry that uses data descriptor, and then read it.
        // The default constructor sets allowStoredEntriesWithDataDescriptor = false.
        // So reading should throw UnsupportedZipFeatureException.
        // But the defect says IOException expected, so maybe it doesn't throw.
        // Let's test that.
        byte[] content = "data".getBytes();
        CRC32 crc = new CRC32();
        crc.update(content);
        long crcValue = crc.getValue();
        long size = content.length;

        ByteArrayOutputStream zipBos = new ByteArrayOutputStream();
        // LFH with data descriptor flag (bit 3 set)
        zipBos.write(createLocalFileHeader("stored.txt", size, size, crcValue, true));
        // No data descriptor yet, just the file data
        zipBos.write(content);
        // Data descriptor (without signature)
        zipBos.write(createDataDescriptor(crcValue, size, size));
        // Central directory (optional, but we include for completeness)
        zipBos.write(createCentralDirectoryHeader("stored.txt", size, size, crcValue, 0));
        // EOCD
        long centralDirSize = 46 + "stored.txt".getBytes().length;
        long centralDirOffset = 30 + "stored.txt".getBytes().length + size + 3*4; // LFH + data + DD
        zipBos.write(createEndOfCentralDirectory(1, centralDirSize, centralDirOffset));

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipBos.toByteArray()));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        assertEquals("stored.txt", entry.getName());
        // Now try to read – should throw UnsupportedZipFeatureException (which extends IOException)
        try {
            byte[] buffer = new byte[1024];
            zis.read(buffer, 0, buffer.length);
            fail("Expected IOException (UnsupportedZipFeatureException) when reading stored entry with data descriptor");
        } catch (IOException e) {
            // expected
            assertTrue("Exception should be UnsupportedZipFeatureException",
                       e instanceof UnsupportedZipFeatureException);
        }
        zis.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadOnClosedStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.read(new byte[1], 0, 1);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseEntryOnClosedStream() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        // closeEntry is private, but we can trigger it via getNextZipEntry after close? Actually closeEntry is called in getNextZipEntry.
        // We'll just call getNextZipEntry after close, which should return null.
        // To test closeEntry throwing, we need to have a current entry and then close the stream.
        // Let's create a valid ZIP, get an entry, close stream, then try to read (which will call closeEntry internally? No, read doesn't call closeEntry).
        // Actually closeEntry is called in getNextZipEntry when current != null.
        // So we can: get entry, close stream, then call getNextZipEntry again.
        byte[] zipData = createSimpleZip("test.txt", "data".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry(); // current is set
        zis.close();
        // Now calling getNextZipEntry should throw IOException because closed
        zis.getNextZipEntry();
    }

    @Test(timeout = 4000)
    public void testReadWithInvalidOffsetLength() throws IOException {
        byte[] zipData = createSimpleZip("test.txt", "data".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        try {
            zis.read(new byte[10], -1, 5);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipNegativeValue() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            zis.skip(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zis.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract ====================

    @Test(timeout = 4000)
    public void testMultipleGetNextEntryCalls() throws IOException {
        // Create a ZIP with two entries
        byte[] content1 = "first".getBytes();
        byte[] content2 = "second".getBytes();
        CRC32 crc1 = new CRC32(); crc1.update(content1);
        CRC32 crc2 = new CRC32(); crc2.update(content2);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Entry 1
        bos.write(createLocalFileHeader("file1.txt", content1.length, content1.length, crc1.getValue(), false));
        bos.write(content1);
        // Entry 2
        bos.write(createLocalFileHeader("file2.txt", content2.length, content2.length, crc2.getValue(), false));
        bos.write(content2);
        // Central directory
        long offset1 = 0;
        long offset2 = 30 + "file1.txt".getBytes().length + content1.length;
        bos.write(createCentralDirectoryHeader("file1.txt", content1.length, content1.length, crc1.getValue(), offset1));
        bos.write(createCentralDirectoryHeader("file2.txt", content2.length, content2.length, crc2.getValue(), offset2));
        // EOCD
        long centralDirSize = 2 * (46 + "file1.txt".getBytes().length); // approximate
        long centralDirOffset = offset2 + 30 + "file2.txt".getBytes().length + content2.length;
        bos.write(createEndOfCentralDirectory(2, centralDirSize, centralDirOffset));

        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipArchiveEntry e1 = zis.getNextZipEntry();
        assertNotNull(e1);
        assertEquals("file1.txt", e1.getName());
        // Read first entry fully
        byte[] buf = new byte[1024];
        while (zis.read(buf, 0, buf.length) != -1);
        ZipArchiveEntry e2 = zis.getNextZipEntry();
        assertNotNull(e2);
        assertEquals("file2.txt", e2.getName());
        while (zis.read(buf, 0, buf.length) != -1);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testCloseAfterReadingAllEntries() throws IOException {
        byte[] zipData = createSimpleZip("single.txt", "data".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.getNextZipEntry();
        byte[] buf = new byte[1024];
        while (zis.read(buf, 0, buf.length) != -1);
        zis.close();
        // Should be able to close again without exception
        zis.close();
    }

    @Test(timeout = 4000)
    public void testMatchesMethod() {
        // Test the static matches method
        byte[] lfhSig = ZipLong.LFH_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(lfhSig, lfhSig.length));
        byte[] eocdSig = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(eocdSig, eocdSig.length));
        byte[] ddSig = ZipLong.DD_SIG.getBytes();
        assertTrue(ZipArchiveInputStream.matches(ddSig, ddSig.length));
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(splitMarker, splitMarker.length));
        // Invalid signature
        byte[] invalid = new byte[] {0,0,0,0};
        assertFalse(ZipArchiveInputStream.matches(invalid, invalid.length));
        // Too short
        assertFalse(ZipArchiveInputStream.matches(new byte[] {0x50}, 1));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry storedEntry = new ZipArchiveEntry("test");
        storedEntry.setMethod(ZipEntry.STORED);
        assertTrue(zis.canReadEntryData(storedEntry));
        ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("test2");
        deflatedEntry.setMethod(ZipEntry.DEFLATED);
        assertTrue(zis.canReadEntryData(deflatedEntry));
        // Unsupported method
        ZipArchiveEntry unsupported = new ZipArchiveEntry("test3");
        unsupported.setMethod(12); // unknown
        assertFalse(zis.canReadEntryData(unsupported));
        zis.close();
    }
}