/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 *
 * 1. Construction & State:
 *    - Constructors: (in), (in, encoding), (in, encoding, useUnicode), (in, encoding, useUnicode, allowStoredDD).
 *    - Initial state: closed=false, current=null, hitCentralDirectory=false.
 *
 * 2. Static Methods:
 *    - matches(byte[], length): length < 4 (false), LFH_SIG (true), EOCD_SIG (true), DD_SIG (true),
 *      SINGLE_SEGMENT_SPLIT_MARKER (true), arbitrary mismatch (false).
 *
 * 3. getNextZipEntry() / getNextEntry():
 *    - Closed or hitCentralDirectory -> returns null.
 *    - Close previous entry when current != null before advancing.
 *    - First entry reading:
 *      * DD_SIG -> UnsupportedZipFeatureException(SPLITTING).
 *      * SINGLE_SEGMENT_SPLIT_MARKER -> skipped properly.
 *      * EOFException -> returns null.
 *      * CFH_SIG / AED_SIG -> hitCentralDirectory = true, skipRemainderOfArchive(), returns null.
 *      * Unknown signature -> returns null.
 *    - Parsing LFH:
 *      * Platform extraction, general purpose flags (UTF-8, Data Descriptor).
 *      * Method (STORED, DEFLATED, UNSHRINKING, IMPLODING, etc.).
 *      * Timestamps (DOS to Java conversion).
 *      * Sizes & CRC (normal vs data descriptor path).
 *      * Extra field and file name parsing.
 *      * Encoding resolution (UTF-8 GP flag override vs stream-level encoding).
 *      * InfoZIP Unicode Extra Field name setting (useUnicodeExtraFields = true/false).
 *      * Zip64 extra field parsing (cSize/size = 0xFFFFFFFF).
 *
 * 4. read(byte[], int, int):
 *    - closed -> IOException("The stream is closed").
 *    - current == null -> -1.
 *    - Boundary checks on buffer / offset / length -> ArrayIndexOutOfBoundsException.
 *    - canReadEntryData / supportsDataDescriptorFor:
 *      * Non-ZipArchiveEntry -> false.
 *      * Unsupported data descriptor (STORED without allowStoredDD) -> UnsupportedZipFeatureException.
 *      * Encryption or unsupported compression method -> UnsupportedZipFeatureException.
 *    - STORED entry reading:
 *      * with and without data descriptor (cache entry, bufferContainsSignature, cacheBytesRead).
 *      * partial read, boundary read, bytesRead >= csize.
 *    - DEFLATED entry reading:
 *      * Normal inflate, truncated stream (IOException("Truncated ZIP file")), needsDictionary.
 *    - CRC32 verification during reads.
 *
 * 5. skip(long):
 *    - value < 0 -> IllegalArgumentException.
 *    - value == 0, value > 0, value past EOF.
 *
 * 6. closeEntry() & close():
 *    - drainCurrentEntryData vs skipping and pushing back unconsumed inflated bytes.
 *    - Data descriptor parsing (with signature vs without signature, 4-byte vs 8-byte Zip64 fields).
 *    - Idempotent close(), end Inflater, close underlying stream.
 */

package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class ZipArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Methods for Synthesizing ZIP Archives in Memory
    // -------------------------------------------------------------------------

    private static byte[] createZipArchive(String name, byte[] data, int method, boolean useDD) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(method);
        if (useDD && method == ZipArchiveOutputStream.STORED) {
            // ZipArchiveOutputStream automatically handles or entries can be configured
            entry.setSize(data.length);
            entry.setCompressedSize(data.length);
            CRC32 crc = new CRC32();
            crc.update(data);
            entry.setCrc(crc.getValue());
        }
        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }

    private static byte[] createZipWithComment(String name, byte[] data, String comment) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setComment(comment);
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(data.length);
        entry.setCompressedSize(data.length);
        CRC32 crc = new CRC32();
        crc.update(data);
        entry.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry);
        zaos.write(data);
        zaos.closeArchiveEntry();
        zaos.close();
        return baos.toByteArray();
    }

    private static byte[] createEmptyZipArchive() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.close();
        return baos.toByteArray();
    }

    private static byte[] writeLittleEndianLong(long value, int length) {
        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = (byte) ((value >> (i * 8)) & 0xFF);
        }
        return b;
    }

    // -------------------------------------------------------------------------
    // Partition A: Static Matching & Signature Checking
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMatchesSignatures() {
        // Length < 4 should fail
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x03}, 3));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{}, 0));

        // Normal LFH
        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(lfh, 4));

        // Empty zip / EOCD
        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(eocd, 4));

        // DD signature
        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(dd, 4));

        // Single segment split marker
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(splitMarker, 4));

        // Random non-zip bytes
        byte[] random = new byte[]{0x12, 0x34, 0x56, 0x78};
        assertFalse(ZipArchiveInputStream.matches(random, 4));

        // Matching first 3 bytes only
        byte[] almostLfh = new byte[]{0x50, 0x4b, 0x03, 0x00};
        assertFalse(ZipArchiveInputStream.matches(almostLfh, 4));
    }

    // -------------------------------------------------------------------------
    // Partition B: Constructor Equivalence & Encoding Handling
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndEncodingSupport() throws IOException {
        byte[] dummy = createEmptyZipArchive();

        // 1-arg constructor
        try (ZipArchiveInputStream z = new ZipArchiveInputStream(new ByteArrayInputStream(dummy))) {
            assertNull(z.getNextEntry());
        }

        // 2-arg constructor
        try (ZipArchiveInputStream z = new ZipArchiveInputStream(new ByteArrayInputStream(dummy), "UTF-8")) {
            assertNull(z.getNextZipEntry());
        }

        // 3-arg constructor
        try (ZipArchiveInputStream z = new ZipArchiveInputStream(new ByteArrayInputStream(dummy), "CP437", false)) {
            assertNull(z.getNextEntry());
        }

        // 4-arg constructor
        try (ZipArchiveInputStream z = new ZipArchiveInputStream(new ByteArrayInputStream(dummy), "US-ASCII", true, true)) {
            assertNull(z.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testDecodingCustomEncoding() throws IOException {
        // Prepare archive with CP437 umlaut or non-ASCII characters
        String entryName = "t\u00e4st.txt"; // täst.txt
        byte[] content = "Hello World".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("ISO-8859-1");
        ZipArchiveEntry inEntry = new ZipArchiveEntry(entryName);
        inEntry.setMethod(ZipArchiveEntry.STORED);
        inEntry.setSize(content.length);
        inEntry.setCompressedSize(content.length);
        CRC32 crc = new CRC32();
        crc.update(content);
        inEntry.setCrc(crc.getValue());
        zaos.putArchiveEntry(inEntry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.close();

        byte[] zipBytes = baos.toByteArray();

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes), "ISO-8859-1", false)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(entryName, entry.getName());
            assertEquals(content.length, in.skip(content.length + 10));
            assertNull(in.getNextZipEntry());
        }
    }

    // -------------------------------------------------------------------------
    // Partition C: STORED and DEFLATED Reading Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadStoredEntryFully() throws IOException {
        byte[] data = "Sample stored payload 1234567890".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("stored.txt", data, ZipArchiveOutputStream.STORED, false);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals("stored.txt", ze.getName());
            assertEquals(ZipArchiveOutputStream.STORED, ze.getMethod());
            assertTrue(in.canReadEntryData(ze));

            byte[] readBuf = new byte[data.length];
            int read = in.read(readBuf, 0, readBuf.length);
            assertEquals(data.length, read);
            assertArrayEquals(data, readBuf);

            // Subsequent read should hit EOF (-1)
            assertEquals(-1, in.read(readBuf, 0, 1));
            assertNull(in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntryFully() throws IOException {
        byte[] data = "Deflated payload repeated repeated repeated repeated repeated".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("deflated.txt", data, ZipArchiveOutputStream.DEFLATED, false);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals("deflated.txt", ze.getName());
            assertEquals(ZipArchiveOutputStream.DEFLATED, ze.getMethod());
            assertTrue(in.canReadEntryData(ze));

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8];
            int r;
            while ((r = in.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, r);
            }
            assertArrayEquals(data, bos.toByteArray());
            assertNull(in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadMultiEntrySequential() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

        byte[] data1 = "First entry payload".getBytes(StandardCharsets.UTF_8);
        ZipArchiveEntry entry1 = new ZipArchiveEntry("first.txt");
        entry1.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry1);
        zaos.write(data1);
        zaos.closeArchiveEntry();

        byte[] data2 = "Second entry payload".getBytes(StandardCharsets.UTF_8);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("second.txt");
        entry2.setMethod(ZipArchiveOutputStream.STORED);
        entry2.setSize(data2.length);
        entry2.setCompressedSize(data2.length);
        CRC32 crc = new CRC32();
        crc.update(data2);
        entry2.setCrc(crc.getValue());
        zaos.putArchiveEntry(entry2);
        zaos.write(data2);
        zaos.closeArchiveEntry();

        zaos.close();

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            // First entry: read partially, then let getNextZipEntry drain/close it
            ZipArchiveEntry e1 = in.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("first.txt", e1.getName());
            byte[] partial = new byte[5];
            int r = in.read(partial, 0, 5);
            assertEquals(5, r);

            // Advance to second entry without reading all of first
            ZipArchiveEntry e2 = in.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("second.txt", e2.getName());

            byte[] b2 = new byte[data2.length];
            int r2 = in.read(b2, 0, b2.length);
            assertEquals(data2.length, r2);
            assertArrayEquals(data2, b2);

            assertNull(in.getNextZipEntry());
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Skip and Boundary Validations
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSkipBehavior() throws IOException {
        byte[] data = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("skipTest.txt", data, ZipArchiveOutputStream.DEFLATED, false);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            assertNotNull(in.getNextZipEntry());

            long skipped = in.skip(10);
            assertEquals(10, skipped);

            byte[] remainder = new byte[data.length - 10];
            int read = in.read(remainder, 0, remainder.length);
            assertEquals(data.length - 10, read);

            byte[] expected = new byte[data.length - 10];
            System.arraycopy(data, 10, expected, 0, expected.length);
            assertArrayEquals(expected, remainder);

            // Skip at EOF
            assertEquals(0, in.skip(5));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeThrowsException() throws IOException {
        byte[] zip = createEmptyZipArchive();
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            in.skip(-1);
        }
    }

    @Test(timeout = 4000)
    public void testReadBoundsCheck() throws IOException {
        byte[] data = "content".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("test.txt", data, ZipArchiveOutputStream.STORED, false);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            assertNotNull(in.getNextZipEntry());

            byte[] b = new byte[10];
            try {
                in.read(b, -1, 5);
                fail("Should throw ArrayIndexOutOfBoundsException for negative offset");
            } catch (ArrayIndexOutOfBoundsException expected) {}

            try {
                in.read(b, 0, -1);
                fail("Should throw ArrayIndexOutOfBoundsException for negative length");
            } catch (ArrayIndexOutOfBoundsException expected) {}

            try {
                in.read(b, 6, 5);
                fail("Should throw ArrayIndexOutOfBoundsException for out of range slice");
            } catch (ArrayIndexOutOfBoundsException expected) {}

            try {
                in.read(b, 11, 0);
                fail("Should throw ArrayIndexOutOfBoundsException for offset > b.length");
            } catch (ArrayIndexOutOfBoundsException expected) {}
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Splitting & Special Marker Handling
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = UnsupportedZipFeatureException.class)
    public void testSplitZipThrowsUnsupportedFeature() throws IOException {
        // Zip split archive starts with DD_SIG signature
        byte[] splitSig = ZipLong.DD_SIG.getBytes();
        byte[] buffer = new byte[40];
        System.arraycopy(splitSig, 0, buffer, 0, splitSig.length);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(buffer))) {
            in.getNextZipEntry();
        }
    }

    @Test(timeout = 4000)
    public void testSingleSegmentSplitMarkerSkipped() throws IOException {
        byte[] data = "Hello segment".getBytes(StandardCharsets.UTF_8);
        byte[] normalZip = createZipArchive("entry.txt", data, ZipArchiveOutputStream.STORED, false);

        // Prepend SINGLE_SEGMENT_SPLIT_MARKER (0x30304b50)
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        byte[] combined = new byte[splitMarker.length + normalZip.length];
        System.arraycopy(splitMarker, 0, combined, 0, splitMarker.length);
        System.arraycopy(normalZip, 0, combined, splitMarker.length, normalZip.length);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(combined))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull("Should safely read entry after single-segment split marker", entry);
            assertEquals("entry.txt", entry.getName());
            byte[] out = new byte[data.length];
            int read = in.read(out, 0, out.length);
            assertEquals(data.length, read);
            assertArrayEquals(data, out);
        }
    }

    // -------------------------------------------------------------------------
    // Partition F: Zip64 Extra Field Processing
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testZip64ExtraFieldProcessing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Construct LFH manually
        baos.write(ZipArchiveOutputStream.LFH_SIG); // 4 bytes
        baos.write(new byte[]{20, 0}); // version made by = 20
        baos.write(new byte[]{0, 0});  // general purpose = 0
        baos.write(new byte[]{0, 0});  // method = 0 (STORED)
        baos.write(new byte[]{0, 0, 0, 0}); // time/date
        baos.write(new byte[]{1, 2, 3, 4}); // crc

        // compressed size = 0xFFFFFFFF, uncompressed size = 0xFFFFFFFF
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});

        byte[] nameBytes = "zip64.txt".getBytes(StandardCharsets.UTF_8);
        baos.write(writeLittleEndianLong(nameBytes.length, 2));

        // Zip64 Extra Field: Header ID (2 bytes) + Size (2 bytes) + Size (8 bytes) + CSize (8 bytes)
        byte[] z64Extra = new byte[20];
        System.arraycopy(Zip64ExtendedInformationExtraField.HEADER_ID.getBytes(), 0, z64Extra, 0, 2);
        System.arraycopy(writeLittleEndianLong(16, 2), 0, z64Extra, 2, 2);
        System.arraycopy(writeLittleEndianLong(100L, 8), 0, z64Extra, 4, 8); // original size
        System.arraycopy(writeLittleEndianLong(50L, 8), 0, z64Extra, 12, 8); // compressed size

        baos.write(writeLittleEndianLong(z64Extra.length, 2));
        baos.write(nameBytes);
        baos.write(z64Extra);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("zip64.txt", entry.getName());
            assertEquals(100L, entry.getSize());
            assertEquals(50L, entry.getCompressedSize());
        }
    }

    // -------------------------------------------------------------------------
    // Partition G: InfoZIP Unicode Extra Field & Platform
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testUnicodePathExtraFieldOverridesName() throws IOException {
        String originalName = "ascii.txt";
        String unicodeName = "\u00e9l\u00e8ve.txt";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry(originalName);
        entry.setMethod(ZipArchiveEntry.STORED);
        entry.setSize(0);
        entry.setCrc(0);
        // Add UnicodePathExtraField
        UnicodePathExtraField uField = new UnicodePathExtraField(unicodeName, originalName.getBytes(StandardCharsets.US_ASCII));
        entry.addExtraField(uField);

        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.close();

        // Reading with useUnicodeExtraFields = true
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", true)) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals(unicodeName, ze.getName());
        }

        // Reading with useUnicodeExtraFields = false
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "US-ASCII", false)) {
            ZipArchiveEntry ze = in.getNextZipEntry();
            assertNotNull(ze);
            assertEquals(originalName, ze.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Partition H: STORED Entries with Data Descriptor
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStoredEntryWithDataDescriptorAllowed() throws IOException {
        // Construct STORED entry with bit 3 (usesDataDescriptor) set
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0}); // version
        baos.write(new byte[]{0x08, 0x00}); // GP flag: bit 3 set!
        baos.write(new byte[]{0, 0}); // method = STORED
        baos.write(new byte[]{0, 0, 0, 0}); // time
        baos.write(new byte[]{0, 0, 0, 0}); // crc zero in LFH
        baos.write(new byte[]{0, 0, 0, 0}); // compressed size zero
        baos.write(new byte[]{0, 0, 0, 0}); // size zero

        byte[] name = "storedDD.txt".getBytes(StandardCharsets.UTF_8);
        baos.write(writeLittleEndianLong(name.length, 2));
        baos.write(new byte[]{0, 0}); // extra len = 0
        baos.write(name);

        byte[] payload = "DataDescriptorPayload".getBytes(StandardCharsets.UTF_8);
        baos.write(payload);

        // Data descriptor with signature
        CRC32 crc = new CRC32();
        crc.update(payload);
        baos.write(ZipArchiveOutputStream.DD_SIG);
        baos.write(writeLittleEndianLong(crc.getValue(), 4));
        baos.write(writeLittleEndianLong(payload.length, 4));
        baos.write(writeLittleEndianLong(payload.length, 4));

        // Followed by CFH signature so bufferContainsSignature recognizes end
        baos.write(ZipArchiveOutputStream.CFH_SIG);
        baos.write(new byte[42]); // remainder of minimal CFH

        // Case 1: allowStoredEntriesWithDataDescriptor = false -> should throw exception on read
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "UTF-8", true, false)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertFalse(in.canReadEntryData(entry));
            try {
                in.read(new byte[10], 0, 10);
                fail("Should reject STORED entry with Data Descriptor when not allowed");
            } catch (UnsupportedZipFeatureException ex) {
                assertEquals(UnsupportedZipFeatureException.Feature.DATA_DESCRIPTOR, ex.getFeature());
            }
        }

        // Case 2: allowStoredEntriesWithDataDescriptor = true -> read successfully
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "UTF-8", true, true)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertTrue(in.canReadEntryData(entry));

            byte[] readBack = new byte[payload.length];
            int read = in.read(readBack, 0, readBack.length);
            assertEquals(payload.length, read);
            assertArrayEquals(payload, readBack);
            assertEquals(-1, in.read(readBack, 0, 1));
        }
    }

    // -------------------------------------------------------------------------
    // Partition I: Data Descriptor Signatures and 8-byte / 4-byte Sizes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDataDescriptorWithoutSignatureDeflated() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ZipArchiveOutputStream.LFH_SIG);
        baos.write(new byte[]{20, 0}); // version
        baos.write(new byte[]{0x08, 0x00}); // GP flag: bit 3 set!
        baos.write(new byte[]{8, 0}); // method = DEFLATED
        baos.write(new byte[]{0, 0, 0, 0}); // time
        baos.write(new byte[]{0, 0, 0, 0}); // crc zero in LFH
        baos.write(new byte[]{0, 0, 0, 0}); // compressed size zero
        baos.write(new byte[]{0, 0, 0, 0}); // size zero

        byte[] name = "deflatedDD.txt".getBytes(StandardCharsets.UTF_8);
        baos.write(writeLittleEndianLong(name.length, 2));
        baos.write(new byte[]{0, 0}); // extra len = 0
        baos.write(name);

        byte[] payload = "TestDeflatedWithDataDescriptorContent".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream defBaos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(payload);
        deflater.finish();
        byte[] defBuf = new byte[1024];
        while (!deflater.finished()) {
            int c = deflater.deflate(defBuf);
            defBaos.write(defBuf, 0, c);
        }
        byte[] compressed = defBaos.toByteArray();
        baos.write(compressed);

        // Data descriptor without DD_SIG (just CRC, CSize, Size - 12 bytes)
        CRC32 crc = new CRC32();
        crc.update(payload);
        baos.write(writeLittleEndianLong(crc.getValue(), 4));
        baos.write(writeLittleEndianLong(compressed.length, 4));
        baos.write(writeLittleEndianLong(payload.length, 4));

        // Followed by CFH signature
        baos.write(ZipArchiveOutputStream.CFH_SIG);
        baos.write(new byte[42]);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            byte[] out = new byte[payload.length];
            int r = in.read(out, 0, out.length);
            assertEquals(payload.length, r);
            assertArrayEquals(payload, out);

            // Closing / next entry drains and checks data descriptor
            assertNull(in.getNextZipEntry());
        }
    }

    // -------------------------------------------------------------------------
    // Partition J: Corrupt, Truncated and Unsupported Compression Methods
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTruncatedZipThrowsException() throws IOException {
        byte[] data = "Short string".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("entry.txt", data, ZipArchiveOutputStream.DEFLATED, false);

        // Truncate zip abruptly inside compressed stream
        byte[] truncated = new byte[zip.length - 30];
        System.arraycopy(zip, 0, truncated, 0, truncated.length);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(truncated))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            byte[] buf = new byte[100];
            try {
                while (in.read(buf, 0, buf.length) != -1) {
                    // reading until EOF or error
                }
            } catch (IOException expected) {
                // Should encounter IOException or EOFException on truncation
                assertTrue(expected.getMessage().contains("Truncated") || expected instanceof EOFException);
            }
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedCompressionMethodThrows() throws IOException {
        byte[] data = "Data".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipArchive("unsupported.txt", data, ZipArchiveOutputStream.STORED, false);

        // Method is at offset 8 in LFH (2 bytes). Set method to 99 (unsupported)
        zip[8] = 99;
        zip[9] = 0;

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertFalse(in.canReadEntryData(entry));
            try {
                in.read(new byte[10], 0, 10);
                fail("Should fail for unsupported method");
            } catch (UnsupportedZipFeatureException ex) {
                assertEquals(UnsupportedZipFeatureException.Feature.METHOD, ex.getFeature());
            }
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedDataDescriptorFeature() throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setMethod(ZipEntry.STORED);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useDataDescriptor(true);
        entry.setGeneralPurposeBit(gpb);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false)) {
            assertFalse(in.canReadEntryData(entry));
        }

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true)) {
            assertTrue(in.canReadEntryData(entry));
        }
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataNonZipEntry() throws IOException {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            ArchiveEntry dummyEntry = new ArchiveEntry() {
                public String getName() { return "dummy"; }
                public long getSize() { return 0; }
                public boolean isDirectory() { return false; }
                public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
            };
            assertFalse(in.canReadEntryData(dummyEntry));
        }
    }

    // -------------------------------------------------------------------------
    // Partition K: Lifecycle, Closed Stream & Idempotency
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testClosedStreamOperationsThrow() throws IOException {
        byte[] zip = createEmptyZipArchive();
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip));
        in.close();

        // Idempotent close
        in.close();

        try {
            in.read(new byte[10], 0, 10);
            fail("read() after close() must throw IOException");
        } catch (IOException expected) {
            assertEquals("The stream is closed", expected.getMessage());
        }

        assertNull("getNextZipEntry after close must return null", in.getNextZipEntry());
        assertNull("getNextEntry after close must return null", in.getNextEntry());
    }

    @Test(timeout = 4000)
    public void testReadWithoutCurrentEntryReturnsNegative() throws IOException {
        byte[] zip = createEmptyZipArchive();
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            // Before reading any entry
            assertEquals(-1, in.read(new byte[10], 0, 10));
        }
    }

    @Test(timeout = 4000)
    public void testArchiveWithZipCommentAndEocd() throws IOException {
        byte[] data = "payload with comment".getBytes(StandardCharsets.UTF_8);
        byte[] zip = createZipWithComment("commented.txt", data, "This is an archive comment");

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zip))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("commented.txt", entry.getName());
            byte[] out = new byte[data.length];
            int read = in.read(out, 0, out.length);
            assertEquals(data.length, read);
            assertArrayEquals(data, out);

            // Should cleanly hit Central Directory and consume EOCD
            assertNull(in.getNextZipEntry());
        }
    }
}