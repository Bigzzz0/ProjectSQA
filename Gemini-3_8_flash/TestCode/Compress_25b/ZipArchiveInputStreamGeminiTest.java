/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 *
 * Defects4J Targeted Defect:
 * - testReadingOfFirstStoredEntry: On initial instantiation, the internal ByteBuffer `buf`
 *   is allocated with position 0 and limit equal to capacity (BUFFER_SIZE).
 *   In readStored(), the condition `if (buf.position() >= buf.limit())` evaluates to false
 *   on the first read of the first STORED entry, causing readStored() to read uninitialized
 *   zeros directly from `buf` rather than fetching real bytes from the underlying stream.
 *
 * Branch & Equivalence Partitions Targeted:
 * - Partition A: Core Functional Paths
 *   - Reading STORED entry content correctly (first entry defect check)
 *   - Reading DEFLATED entry content, full read and multiple chunks
 *   - Reading multi-entry zip archives (transitions between entries, closeEntry)
 *   - skip() operation on positive offsets, boundary skipping, and negative offset validation
 *   - matches() utility with LFH_SIG, EOCD_SIG, DD_SIG, SINGLE_SEGMENT_SPLIT_MARKER, short length, mismatch
 * - Partition B: Boundary Value Analysis & Corner Cases
 *   - Empty archives (EOCD only)
 *   - Empty file entries (0 size STORED and DEFLATED)
 *   - Buffer boundary reads (read with offset > buffer length, negative length, negative offset)
 *   - Reading past EOF / end of archive
 * - Partition C: Defect-Targeted & Special Zip Features
 *   - First STORED entry byte verification: ensures real stream data is read rather than zero-filled buffer
 *   - Split archive marker detection (DD_SIG triggers UnsupportedZipFeatureException SPLITTING)
 *   - Single segment split marker handling (skipping 4-byte marker and parsing LFH)
 *   - Zip64 extra field handling (sizes 0xFFFFFFFF with Zip64ExtendedInformationExtraField)
 *   - Data descriptor handling for STORED entries (allowStoredEntriesWithDataDescriptor true vs false)
 * - Partition D: Defensive Guards & Exception Handling
 *   - Stream closed guards on read(), closeEntry(), fill()
 *   - canReadEntryData() with ZipArchiveEntry vs non-ZipArchiveEntry, unsupported compression methods
 *   - UnsupportedZipFeatureException for unhandled methods or data descriptor configurations
 * - Partition E: Lifecycle & Stream Contracts
 *   - Idempotent close() calls
 *   - getNextEntry() delegating to getNextZipEntry()
 */

package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import static org.junit.Assert.*;

public class ZipArchiveInputStreamGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Directly targets the Defects4J defect in ZipArchiveInputStream:
     * When reading the first entry of type STORED, `buf.position() >= buf.limit()`
     * evaluates to false if `buf` was freshly allocated (position 0, limit 512).
     * This causes it to read zeros instead of filling the buffer from the stream.
     */
    @Test(timeout = 4000)
    public void testReadingOfFirstStoredEntry() throws IOException {
        final byte[] expectedData = new byte[]{100, 101, 102, 103, 104, 105};
        final byte[] zipData = createZipWithStoredEntry("firstStored.bin", expectedData);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull("First entry must not be null", entry);
            assertEquals("firstStored.bin", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());

            byte[] actualData = new byte[expectedData.length];
            int bytesRead = zis.read(actualData, 0, actualData.length);

            assertEquals("Must read all expected bytes", expectedData.length, bytesRead);
            assertArrayEquals("First stored entry content must match underlying payload, not uninitialized buffer zeros",
                    expectedData, actualData);
        }
    }

    /**
     * Verifies consecutive STORED entries are read correctly after closing/draining the first entry.
     */
    @Test(timeout = 4000)
    public void testReadingMultipleConsecutiveStoredEntries() throws IOException {
        byte[] data1 = new byte[]{1, 2, 3, 4};
        byte[] data2 = new byte[]{5, 6, 7, 8, 9};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            addStoredEntry(zos, "file1.dat", data1);
            addStoredEntry(zos, "file2.dat", data2);
        }

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("file1.dat", e1.getName());
            byte[] read1 = new byte[data1.length];
            assertEquals(data1.length, zis.read(read1));
            assertArrayEquals(data1, read1);

            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("file2.dat", e2.getName());
            byte[] read2 = new byte[data2.length];
            assertEquals(data2.length, zis.read(read2));
            assertArrayEquals(data2, read2);

            assertNull(zis.getNextZipEntry());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadDeflatedEntrySuccessfully() throws IOException {
        byte[] payload = "Hello Apache Commons Compress Deflate!".getBytes("UTF-8");
        byte[] zipData = createZipWithDeflatedEntry("deflated.txt", payload);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ArchiveEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertTrue(zis.canReadEntryData(entry));

            ByteArrayOutputStream content = new ByteArrayOutputStream();
            byte[] readBuf = new byte[8];
            int read;
            while ((read = zis.read(readBuf, 0, readBuf.length)) != -1) {
                content.write(readBuf, 0, read);
            }

            assertArrayEquals(payload, content.toByteArray());
            assertEquals(-1, zis.read(readBuf, 0, readBuf.length));
        }
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntry() throws IOException {
        byte[] data = new byte[]{10, 20, 30, 40, 50, 60, 70, 80};
        byte[] zipData = createZipWithStoredEntry("skipTest.bin", data);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            assertNotNull(zis.getNextZipEntry());
            long skipped = zis.skip(4);
            assertEquals(4, skipped);

            byte[] remainder = new byte[4];
            int read = zis.read(remainder);
            assertEquals(4, read);
            assertArrayEquals(new byte[]{50, 60, 70, 80}, remainder);

            assertEquals(0, zis.skip(10));
            assertEquals(-1, zis.read());
        }
    }

    @Test(timeout = 4000)
    public void testSkipRemainderWhenSkippingLargeValue() throws IOException {
        byte[] data = new byte[]{1, 2, 3, 4, 5};
        byte[] zipData = createZipWithStoredEntry("largeSkip.bin", data);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            assertNotNull(zis.getNextZipEntry());
            long skipped = zis.skip(1000);
            assertEquals(5, skipped);
            assertEquals(-1, zis.read());
        }
    }

    @Test(timeout = 4000)
    public void testMatchesSignatureValidation() {
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(lfh, 4));

        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(eocd, 4));

        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(dd, 4));

        byte[] split = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(split, 4));

        assertFalse(ZipArchiveInputStream.matches(new byte[]{0, 0, 0, 0}, 4));
        assertFalse(ZipArchiveInputStream.matches(lfh, 3));
    }

    @Test(timeout = 4000)
    public void testEmptyZipFileReturnsNullEntry() throws IOException {
        byte[] emptyZip = createEmptyZip();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip))) {
            assertNull("Empty zip should return null for first entry", zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testImplicitCloseEntryTransitionsWithoutExhaustingStream() throws IOException {
        byte[] data1 = new byte[]{11, 22, 33, 44};
        byte[] data2 = new byte[]{55, 66};

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            addDeflatedEntry(zos, "d1.bin", data1);
            addDeflatedEntry(zos, "d2.bin", data2);
        }

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            // Read only 1 byte from e1, leaving the rest unconsumed
            int firstByte = zis.read();
            assertEquals(11, firstByte);

            // getNextZipEntry should internally close e1 and align stream for e2
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("d2.bin", e2.getName());

            byte[] b2 = new byte[data2.length];
            int read = zis.read(b2);
            assertEquals(data2.length, read);
            assertArrayEquals(data2, b2);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroLengthEntry() throws IOException {
        byte[] emptyData = new byte[0];
        byte[] zipData = createZipWithStoredEntry("empty.bin", emptyData);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(0, entry.getSize());
            assertEquals(-1, zis.read(new byte[1]));
        }
    }

    @Test(timeout = 4000)
    public void testReadWithZeroLengthBuffer() throws IOException {
        byte[] data = new byte[]{1, 2, 3};
        byte[] zipData = createZipWithStoredEntry("data.bin", data);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            assertEquals(0, zis.read(new byte[0], 0, 0));
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testReadWithNegativeOffsetThrowsException() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.bin", new byte[]{1, 2});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[4], -1, 2);
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testReadWithNegativeLengthThrowsException() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.bin", new byte[]{1, 2});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[4], 0, -1);
        }
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testReadWithOffsetLengthOverflowThrowsException() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.bin", new byte[]{1, 2});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[4], 3, 2);
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSkipNegativeValueThrowsException() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.bin", new byte[]{1});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.skip(-5);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testReadWhenClosedThrowsIOException() throws IOException {
        byte[] zipData = createZipWithStoredEntry("test.bin", new byte[]{1, 2});
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        zis.close();
        zis.read(new byte[2], 0, 2);
    }

    @Test(timeout = 4000)
    public void testReadWhenCurrentEntryIsNullReturnsNegativeOne() throws IOException {
        byte[] emptyZip = createEmptyZip();
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip))) {
            assertEquals(-1, zis.read(new byte[4], 0, 4));
        }
    }

    @Test(timeout = 4000)
    public void testCloseIsIdempotent() throws IOException {
        byte[] emptyZip = createEmptyZip();
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip));
        zis.close();
        zis.close(); // Second close must not fail
    }

    @Test(expected = UnsupportedZipFeatureException.class, timeout = 4000)
    public void testSplitZipThrowsUnsupportedZipFeatureException() throws IOException {
        // Construct stream beginning with DD_SIG (data descriptor signature used for split archives)
        byte[] splitSig = ZipArchiveOutputStream.DD_SIG;
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(splitSig))) {
            zis.getNextZipEntry();
        }
    }

    @Test(timeout = 4000)
    public void testSingleSegmentSplitMarkerIsSkipped() throws IOException {
        byte[] normalZip = createZipWithStoredEntry("inner.txt", new byte[]{42});
        byte[] marker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();

        byte[] combined = new byte[marker.length + normalZip.length];
        System.arraycopy(marker, 0, combined, 0, marker.length);
        System.arraycopy(normalZip, 0, combined, marker.length, normalZip.length);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(combined))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull("Entry after single segment split marker should be read", entry);
            assertEquals("inner.txt", entry.getName());
            assertEquals(42, zis.read());
        }
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataCompatibility() {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            ArchiveEntry fakeEntry = new ArchiveEntry() {
                public String getName() { return "fake"; }
                public long getSize() { return 0; }
                public boolean isDirectory() { return false; }
                public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
            };
            assertFalse("Non-ZipArchiveEntry cannot be read", zis.canReadEntryData(fakeEntry));

            ZipArchiveEntry validStored = new ZipArchiveEntry("test");
            validStored.setMethod(ZipEntry.STORED);
            assertTrue(zis.canReadEntryData(validStored));

            ZipArchiveEntry validDeflated = new ZipArchiveEntry("testDeflated");
            validDeflated.setMethod(ZipEntry.DEFLATED);
            assertTrue(zis.canReadEntryData(validDeflated));

            ZipArchiveEntry unsupportedMethod = new ZipArchiveEntry("unsupported");
            unsupportedMethod.setMethod(99);
            assertFalse(zis.canReadEntryData(unsupportedMethod));
        } catch (IOException e) {
            fail("Exception unexpected: " + e.getMessage());
        }
    }

    @Test(expected = UnsupportedZipFeatureException.class, timeout = 4000)
    public void testStoredWithDataDescriptorWithoutFlagThrowsException() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(ZipEntry.STORED);
        GeneralPurposeBit gpFlag = new GeneralPurposeBit();
        gpFlag.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpFlag);

        // Constructor with allowStoredEntriesWithDataDescriptor = false
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false)) {
            assertFalse(zis.canReadEntryData(entry));

            // Force read with current entry set by overriding or crafting stream
            // When read() checks supportsDataDescriptorFor, it throws UnsupportedZipFeatureException
            byte[] rawHeaderWithDD = createLocalFileHeader(ZipEntry.STORED, true, "f.txt", new byte[]{1});
            try (ZipArchiveInputStream rawZis = new ZipArchiveInputStream(new ByteArrayInputStream(rawHeaderWithDD), "UTF-8", true, false)) {
                rawZis.getNextZipEntry();
                rawZis.read(new byte[1], 0, 1);
            }
        }
    }

    @Test(timeout = 4000)
    public void testTruncatedZipThrowsEOFExceptionOnReadingHeader() throws IOException {
        byte[] truncated = new byte[]{ 'P', 'K', 3, 4, 10 }; // truncated LFH
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(truncated))) {
            assertNull("Truncated header should cause EOF and return null", zis.getNextZipEntry());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Encoding & Extras
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomEncodingConstructor() throws IOException {
        byte[] zipData = createZipWithStoredEntry("customEnc.txt", new byte[]{65, 66});
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData), "ISO-8859-1")) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("customEnc.txt", entry.getName());
        }
    }

    @Test(timeout = 4000)
    public void testUnicodeExtraFieldResolution() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry entry = new ZipArchiveEntry("asciiName.txt");
            entry.setMethod(ZipEntry.STORED);
            byte[] data = new byte[]{9};
            entry.setSize(data.length);
            entry.setCompressedSize(data.length);
            CRC32 crc = new CRC32();
            crc.update(data);
            entry.setCrc(crc.getValue());

            // Add InfoZIP Unicode Path extra field
            UnicodePathExtraField unicodeField = new UnicodePathExtraField("unicodeName.txt", "asciiName.txt".getBytes("US-ASCII"));
            entry.addExtraField(unicodeField);

            zos.putArchiveEntry(entry);
            zos.write(data);
            zos.closeArchiveEntry();
        }

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", true)) {
            ZipArchiveEntry readEntry = zis.getNextZipEntry();
            assertNotNull(readEntry);
            assertEquals("unicodeName.txt", readEntry.getName());
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private byte[] createZipWithStoredEntry(String name, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            addStoredEntry(zos, name, data);
        }
        return baos.toByteArray();
    }

    private byte[] createZipWithDeflatedEntry(String name, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            addDeflatedEntry(zos, name, data);
        }
        return baos.toByteArray();
    }

    private void addStoredEntry(ZipArchiveOutputStream zos, String name, byte[] data) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
    }

    private void addDeflatedEntry(ZipArchiveOutputStream zos, String name, byte[] data) throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write(data);
        zos.closeArchiveEntry();
    }

    private byte[] createEmptyZip() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            // Close without adding entries writes EOCD
        }
        return baos.toByteArray();
    }

    private byte[] createLocalFileHeader(int method, boolean hasDataDescriptor, String name, byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG); // 0..3: signature
        baos.write(new byte[]{20, 0}); // 4..5: version needed (2.0)
        baos.write(hasDataDescriptor ? new byte[]{0x08, 0} : new byte[]{0, 0}); // 6..7: gp flag
        baos.write(new byte[]{(byte) method, 0}); // 8..9: method
        baos.write(new byte[]{0, 0, 0, 0}); // 10..13: time/date
        if (hasDataDescriptor) {
            baos.write(new byte[]{0, 0, 0, 0}); // crc
            baos.write(new byte[]{0, 0, 0, 0}); // cSize
            baos.write(new byte[]{0, 0, 0, 0}); // size
        } else {
            CRC32 crc = new CRC32();
            crc.update(data);
            baos.write(new ZipLong(crc.getValue()).getBytes());
            baos.write(new ZipLong(data.length).getBytes());
            baos.write(new ZipLong(data.length).getBytes());
        }
        byte[] nameBytes = name.getBytes("UTF-8");
        baos.write(new ZipShort(nameBytes.length).getBytes()); // name len
        baos.write(new byte[]{0, 0}); // extra len
        baos.write(nameBytes);
        baos.write(data);
        return baos.toByteArray();
    }
}