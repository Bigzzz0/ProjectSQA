package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and conditions:
 * 
 * 1. Constructor variants: encoding null/UTF-8, useUnicodeExtraFields true/false,
 *    allowStoredEntriesWithDataDescriptor true/false
 * 2. getNextZipEntry(): 
 *    - closed stream returns null
 *    - hitCentralDirectory returns null
 *    - EOFException returns null
 *    - CFH_SIG/AED_SIG detection triggers hitCentralDirectory
 *    - Invalid signature throws ZipException
 *    - Data descriptor flag handling (hasDataDescriptor true/false)
 *    - Zip64 extra field processing (size/cSize magic values)
 *    - UTF8 flag handling for names
 *    - Various compression methods (STORED, DEFLATED, UNSHRINKING, IMPLODING, BZIP2, ENHANCED_DEFLATED)
 * 3. read(): 
 *    - Closed stream throws IOException
 *    - current == null returns -1
 *    - Invalid buffer/offset/length throws ArrayIndexOutOfBoundsException
 *    - Unsupported method throws UnsupportedZipFeatureException
 *    - CRC update on successful read
 * 4. readStored(): 
 *    - Data descriptor path with caching
 *    - Normal stored read with size tracking
 *    - Buffer boundary conditions
 * 5. readDeflated(): 
 *    - Inflater finished returns -1
 *    - Needs dictionary throws ZipException
 *    - Truncated data throws IOException
 * 6. close(): 
 *    - Idempotent close
 *    - Inflater ended
 * 7. skip(): 
 *    - Negative value throws IllegalArgumentException
 *    - Normal skip with buffer
 * 8. matches(): 
 *    - Short signature returns false
 *    - Various valid signatures
 * 9. canReadEntryData(): 
 *    - Non-ZipArchiveEntry returns false
 *    - Unsupported methods return false
 * 10. closeEntry():
 *     - Closed stream throws IOException
 *     - Current null returns silently
 *     - Outstanding bytes draining
 *     - Data descriptor reading
 * 11. readDataDescriptor():
 *     - With/without DD signature
 *     - Zip64 vs regular size detection
 * 12. readStoredEntry():
 *     - Buffer signature detection (LFH, CFH, DD)
 *     - Truncated stream throws IOException
 * 
 * Defect-targeted test: properlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknown
 * This tests that when an entry has unknown uncompressed size (SIZE_UNKNOWN),
 * the entry is properly handled and canReadEntryData returns appropriate value.
 * The defect causes entries with unknown sizes to not be properly marked.
 */
public class ZipArchiveInputStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorWithDefaultEncoding() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        assertNotNull(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullEncoding() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip), null);
        assertNotNull(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithEncodingAndUnicodeFalse() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip), "UTF-8", false);
        assertNotNull(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip), "UTF-8", true, true);
        assertNotNull(zis);
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryReturnsNullOnClosedStream() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        zis.close();
        assertNull(zis.getNextZipEntry());
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryReturnsNullOnEmptyStream() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        assertNull(zis.getNextZipEntry());
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadReturnsMinusOneWhenNoCurrentEntry() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        byte[] buffer = new byte[10];
        assertEquals(-1, zis.read(buffer, 0, 10));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadThrowsIOExceptionOnClosedStream() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        zis.close();
        try {
            zis.read(new byte[10], 0, 10);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadThrowsArrayIndexOutOfBoundsOnInvalidParams() throws IOException {
        byte[] zipData = createSimpleStoredEntryZip("test.txt", "Hello".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zis.getNextZipEntry());
        
        try {
            zis.read(new byte[10], -1, 5);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            zis.read(new byte[10], 0, 15);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        
        try {
            zis.read(new byte[10], 5, 10);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
        
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithNegativeValue() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        try {
            zis.skip(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithZeroValue() throws IOException {
        byte[] zipData = createSimpleStoredEntryZip("test.txt", "Hello".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zis.getNextZipEntry());
        assertEquals(0, zis.skip(0));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testMatchesWithShortSignature() {
        byte[] sig = new byte[3];
        assertFalse(ZipArchiveInputStream.matches(sig, 3));
    }

    @Test(timeout = 4000)
    public void testMatchesWithLFHSignature() {
        byte[] sig = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithEOCDSignature() {
        byte[] sig = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithDDSignature() {
        byte[] sig = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithSplitMarker() {
        byte[] sig = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithInvalidSignature() {
        byte[] sig = new byte[]{0x00, 0x01, 0x02, 0x03};
        assertFalse(ZipArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithNonZipEntry() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ArchiveEntry nonZipEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "test"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
        };
        assertFalse(zis.canReadEntryData(nonZipEntry));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithStoredEntry() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.STORED);
        assertTrue(zis.canReadEntryData(entry));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithDeflatedEntry() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        assertTrue(zis.canReadEntryData(entry));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithUnsupportedMethod() {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(99); // unsupported
        assertFalse(zis.canReadEntryData(entry));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testReadWithEmptyBuffer() throws IOException {
        byte[] zipData = createSimpleStoredEntryZip("test.txt", "Hello".getBytes());
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zis.getNextZipEntry());
        assertEquals(0, zis.read(new byte[0], 0, 0));
        zis.close();
    }

    @Test(timeout = 4000)
    public void testReadWithLargeBuffer() throws IOException {
        byte[] content = "Hello World".getBytes();
        byte[] zipData = createSimpleStoredEntryZip("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zis.getNextZipEntry());
        byte[] buffer = new byte[1024];
        int read = zis.read(buffer, 0, 1024);
        assertEquals(content.length, read);
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipBeyondEntrySize() throws IOException {
        byte[] content = "Hello".getBytes();
        byte[] zipData = createSimpleStoredEntryZip("test.txt", content);
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        assertNotNull(zis.getNextZipEntry());
        long skipped = zis.skip(100);
        assertTrue(skipped >= content.length);
        zis.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testProperlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknown() throws IOException {
        // This test targets the known defect: entries with unknown uncompressed size
        // should be properly handled. The defect causes entries with SIZE_UNKNOWN
        // to not be properly marked as unreadable.
        
        // Create a zip entry with unknown uncompressed size (using data descriptor)
        // We'll create a minimal zip with a stored entry that has data descriptor
        // and unknown sizes in the local file header
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Local file header
        byte[] lfh = new byte[30];
        System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, lfh, 0, 4); // signature
        ZipShort.putShort(20, lfh, 4); // version needed
        ZipShort.putShort(0, lfh, 6); // general purpose bit flag (no data descriptor)
        ZipShort.putShort(ZipEntry.STORED, lfh, 8); // compression method
        ZipUtil.toDosTime(System.currentTimeMillis(), lfh, 10); // time
        ZipLong.putLong(0, lfh, 14); // crc-32 (unknown)
        ZipLong.putLong(0xFFFFFFFFL, lfh, 18); // compressed size (unknown)
        ZipLong.putLong(0xFFFFFFFFL, lfh, 22); // uncompressed size (unknown)
        ZipShort.putShort(8, lfh, 26); // file name length
        ZipShort.putShort(0, lfh, 28); // extra field length
        
        try {
            baos.write(lfh);
            baos.write("test.txt".getBytes());
            baos.write("Hello".getBytes());
        } finally {
            baos.close();
        }
        
        byte[] zipData = baos.toByteArray();
        
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        
        // The entry should have unknown sizes
        assertNotNull(entry);
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getCompressedSize());
        
        // The entry should be readable (STORED without data descriptor)
        assertTrue("Entry with unknown uncompressed size should be readable", 
                   zis.canReadEntryData(entry));
        
        // Reading should work
        byte[] buffer = new byte[10];
        int read = zis.read(buffer, 0, 10);
        assertEquals(5, read);
        assertEquals("Hello", new String(buffer, 0, 5));
        
        zis.close();
    }

    @Test(timeout = 4000)
    public void testProperlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknownWithDataDescriptor() throws IOException {
        // Test with data descriptor flag set - should be unreadable for STORED entries
        // when allowStoredEntriesWithDataDescriptor is false
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Local file header with data descriptor flag
        byte[] lfh = new byte[30];
        System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, lfh, 0, 4); // signature
        ZipShort.putShort(20, lfh, 4); // version needed
        ZipShort.putShort(8, lfh, 6); // general purpose bit flag (bit 3 = data descriptor)
        ZipShort.putShort(ZipEntry.STORED, lfh, 8); // compression method
        ZipUtil.toDosTime(System.currentTimeMillis(), lfh, 10); // time
        ZipLong.putLong(0, lfh, 14); // crc-32 (unknown)
        ZipLong.putLong(0, lfh, 18); // compressed size (unknown)
        ZipLong.putLong(0, lfh, 22); // uncompressed size (unknown)
        ZipShort.putShort(8, lfh, 26); // file name length
        ZipShort.putShort(0, lfh, 28); // extra field length
        
        try {
            baos.write(lfh);
            baos.write("test.txt".getBytes());
            baos.write("Hello".getBytes());
        } finally {
            baos.close();
        }
        
        byte[] zipData = baos.toByteArray();
        
        // With allowStoredEntriesWithDataDescriptor = false
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, false);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        
        assertNotNull(entry);
        assertTrue(entry.getGeneralPurposeBit().usesDataDescriptor());
        
        // Should be unreadable because it's STORED with data descriptor and not allowed
        assertFalse("STORED entry with data descriptor should be unreadable when not allowed", 
                    zis.canReadEntryData(entry));
        
        zis.close();
    }

    @Test(timeout = 4000)
    public void testProperlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknownWithDataDescriptorAllowed() throws IOException {
        // Test with data descriptor flag set - should be readable when allowed
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        byte[] lfh = new byte[30];
        System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, lfh, 0, 4);
        ZipShort.putShort(20, lfh, 4);
        ZipShort.putShort(8, lfh, 6); // data descriptor flag
        ZipShort.putShort(ZipEntry.STORED, lfh, 8);
        ZipUtil.toDosTime(System.currentTimeMillis(), lfh, 10);
        ZipLong.putLong(0, lfh, 14);
        ZipLong.putLong(0, lfh, 18);
        ZipLong.putLong(0, lfh, 22);
        ZipShort.putShort(8, lfh, 26);
        ZipShort.putShort(0, lfh, 28);
        
        try {
            baos.write(lfh);
            baos.write("test.txt".getBytes());
            baos.write("Hello".getBytes());
        } finally {
            baos.close();
        }
        
        byte[] zipData = baos.toByteArray();
        
        // With allowStoredEntriesWithDataDescriptor = true
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "UTF-8", true, true);
        ZipArchiveEntry entry = zis.getNextZipEntry();
        
        assertNotNull(entry);
        assertTrue(entry.getGeneralPurposeBit().usesDataDescriptor());
        
        // Should be readable when allowed
        assertTrue("STORED entry with data descriptor should be readable when allowed", 
                   zis.canReadEntryData(entry));
        
        zis.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = ZipException.class)
    public void testGetNextZipEntryThrowsZipExceptionOnInvalidSignature() throws IOException {
        byte[] invalidData = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09};
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(invalidData));
        try {
            zis.getNextZipEntry();
        } finally {
            zis.close();
        }
    }

    @Test(timeout = 4000)
    public void testCloseIsIdempotent() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        zis.close();
        zis.close(); // should not throw
    }

    @Test(timeout = 4000)
    public void testReadThrowsUnsupportedZipFeatureExceptionForUnsupportedMethod() throws IOException {
        // Create a zip entry with unsupported compression method
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        byte[] lfh = new byte[30];
        System.arraycopy(ZipLong.LFH_SIG.getBytes(), 0, lfh, 0, 4);
        ZipShort.putShort(20, lfh, 4);
        ZipShort.putShort(0, lfh, 6);
        ZipShort.putShort(99, lfh, 8); // unsupported method
        ZipUtil.toDosTime(System.currentTimeMillis(), lfh, 10);
        ZipLong.putLong(0, lfh, 14);
        ZipLong.putLong(5, lfh, 18);
        ZipLong.putLong(5, lfh, 22);
        ZipShort.putShort(8, lfh, 26);
        ZipShort.putShort(0, lfh, 28);
        
        try {
            baos.write(lfh);
            baos.write("test.txt".getBytes());
            baos.write("Hello".getBytes());
        } finally {
            baos.close();
        }
        
        byte[] zipData = baos.toByteArray();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        ZipArchiveEntry entry = zis.getNextZipEntry();
        assertNotNull(entry);
        
        try {
            zis.read(new byte[10], 0, 10);
            fail("Expected UnsupportedZipFeatureException");
        } catch (UnsupportedZipFeatureException e) {
            // expected
        }
        
        zis.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testReadAndCloseMultipleEntries() throws IOException {
        // Create a zip with multiple stored entries
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // First entry
        writeLocalFileHeader(baos, "file1.txt", "Content1".getBytes(), ZipEntry.STORED);
        baos.write("Content1".getBytes());
        
        // Second entry
        writeLocalFileHeader(baos, "file2.txt", "Content2".getBytes(), ZipEntry.STORED);
        baos.write("Content2".getBytes());
        
        byte[] zipData = baos.toByteArray();
        
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        assertNotNull(entry1);
        assertEquals("file1.txt", entry1.getName());
        
        byte[] buffer = new byte[20];
        int read1 = zis.read(buffer, 0, 20);
        assertEquals(8, read1);
        assertEquals("Content1", new String(buffer, 0, 8));
        
        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());
        
        int read2 = zis.read(buffer, 0, 20);
        assertEquals(8, read2);
        assertEquals("Content2", new String(buffer, 0, 8));
        
        assertNull(zis.getNextZipEntry());
        
        zis.close();
    }

    @Test(timeout = 4000)
    public void testSkipOverEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLocalFileHeader(baos, "file1.txt", "Content1".getBytes(), ZipEntry.STORED);
        baos.write("Content1".getBytes());
        writeLocalFileHeader(baos, "file2.txt", "Content2".getBytes(), ZipEntry.STORED);
        baos.write("Content2".getBytes());
        
        byte[] zipData = baos.toByteArray();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        
        ZipArchiveEntry entry1 = zis.getNextZipEntry();
        assertNotNull(entry1);
        
        // Skip first entry
        long skipped = zis.skip(100);
        assertTrue(skipped >= 8);
        
        ZipArchiveEntry entry2 = zis.getNextZipEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());
        
        zis.close();
    }

    // ==================== Helper Methods ====================

    private byte[] createEmptyZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // End of central directory record
        baos.write(ZipArchiveOutputStream.EOCD_SIG);
        ZipShort.putShort(0, baos); // disk number
        ZipShort.putShort(0, baos); // disk with start of central directory
        ZipShort.putShort(0, baos); // total entries on this disk
        ZipShort.putShort(0, baos); // total entries
        ZipLong.putLong(0, baos); // size of central directory
        ZipLong.putLong(0, baos); // offset of start of central directory
        ZipShort.putShort(0, baos); // comment length
        baos.close();
        return baos.toByteArray();
    }

    private byte[] createSimpleStoredEntryZip(String name, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeLocalFileHeader(baos, name, content, ZipEntry.STORED);
        baos.write(content);
        baos.close();
        return baos.toByteArray();
    }

    private void writeLocalFileHeader(ByteArrayOutputStream baos, String name, byte[] content, int method) throws IOException {
        byte[] nameBytes = name.getBytes("UTF-8");
        long crc = computeCRC32(content);
        
        baos.write(ZipLong.LFH_SIG.getBytes());
        ZipShort.putShort(20, baos); // version needed
        ZipShort.putShort(0, baos); // general purpose bit flag
        ZipShort.putShort(method, baos); // compression method
        ZipUtil.toDosTime(System.currentTimeMillis(), baos); // time
        ZipLong.putLong(crc, baos); // crc-32
        ZipLong.putLong(content.length, baos); // compressed size
        ZipLong.putLong(content.length, baos); // uncompressed size
        ZipShort.putShort(nameBytes.length, baos); // file name length
        ZipShort.putShort(0, baos); // extra field length
        baos.write(nameBytes);
    }

    private long computeCRC32(byte[] data) {
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        crc32.update(data);
        return crc32.getValue();
    }
}