package org.apache.commons.compress.archivers.zip;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.compress.archivers.zip.ZipFile
 *
 * Targeted Defects & Branches:
 * 1. COMPRESS-164 / Defects4J UTF8ZipFilesTest::testReadWinZipArchive:
 *    - Root Cause: During Central Directory parsing without UTF-8 flag, Unicode Extra Field
 *      (0x7075 / UnicodePathExtraField) alters ZipArchiveEntry name via
 *      ZipUtil.setNameAndCommentFromExtraFields(). Changing the entry's name mutates its hashCode,
 *      corrupting entry lookup in `entries` (LinkedHashMap<ZipArchiveEntry, OffsetEntry>).
 *      Consequently, getInputStream(ze) results in `entries.get(ze) == null`, returning null.
 *    - Fix verification: getInputStream() must return a non-null InputStream for entry with Unicode extra field.
 *
 * 2. EOCD / Signature Detection Branches:
 *    - Valid 32-bit ZIP with no comment.
 *    - Valid 32-bit ZIP with archive comment (triggering reverse scan in tryToLocateSignature).
 *    - Corrupt archives (empty file, invalid signatures, truncated headers).
 *    - ZIP64 locator and ZIP64 central directory reading.
 *    - Starts with LFH but missing Central Directory ("central directory is empty...").
 *
 * 3. Compression & Data Handling Branches:
 *    - STORED (uncompressed) stream reading via BoundedInputStream.
 *    - DEFLATED stream reading via InflaterInputStream with dummy byte.
 *    - Unsupported compression method (ZipException).
 *    - canReadEntryData() branches for supported / unsupported entries.
 *
 * 4. Entry Ordering & Traversal:
 *    - getEntries() maintaining Central Directory order.
 *    - getEntriesInPhysicalOrder() sorting via OFFSET_COMPARATOR.
 *    - Handling offset comparator ties, entries not in map.
 *
 * 5. Lifecycle & Defensive Guards:
 *    - Constructor variants (File, String, with/without encoding, unicode flags).
 *    - closeQuietly() on null and active ZipFile instances.
 *    - Multiple close() calls.
 *    - BoundedInputStream boundaries: single-byte read(), buffer read(), len <= 0, EOF conditions.
 */
public class ZipFileGeminiTest {

    private List<File> tempFiles;

    @Before
    public void setUp() {
        tempFiles = new ArrayList<File>();
    }

    @After
    public void tearDown() {
        for (File f : tempFiles) {
            if (f != null && f.exists()) {
                f.delete();
            }
        }
        tempFiles.clear();
    }

    private File createTempZipFile(byte[] zipData) throws IOException {
        File file = File.createTempFile("compress_zip_", ".zip");
        file.deleteOnExit();
        tempFiles.add(file);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(zipData);
        }
        return file;
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-164 / Unicode Entry Lookup)
    // =========================================================================

    /**
     * Targets COMPRESS-164: When an entry has a Unicode Path Extra Field (0x7075)
     * and general purpose bit UTF-8 flag is clear, resolveLocalFileHeaderData updates
     * the name of the ZipArchiveEntry. Since ZipArchiveEntry.hashCode() is derived
     * from its name, modifying the name while it is a key in the `entries` LinkedHashMap
     * invalidates the hash table bucket, causing entries.get(ze) to return null in getInputStream(ze).
     */
    @Test(timeout = 4000)
    public void testReadEntryWithUnicodeExtraFieldMutatedHashCode() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] content = "Hello Unicode Bug!".getBytes(StandardCharsets.US_ASCII);

        CRC32 crc = new CRC32();
        crc.update(content);
        long crcValue = crc.getValue();

        String asciiName = "ascii_test.txt";
        byte[] asciiNameBytes = asciiName.getBytes(StandardCharsets.US_ASCII);

        String unicodeName = "unicode_\u00e9\u00e0.txt";
        byte[] unicodeNameBytes = unicodeName.getBytes(StandardCharsets.UTF_8);

        // Build Info-ZIP Unicode Path Extra Field (Header ID 0x7075 = 'up')
        // Format: version (1 byte) + CRC32 of original name (4 bytes) + UTF-8 name
        CRC32 nameCrc = new CRC32();
        nameCrc.update(asciiNameBytes);
        long nameCrcVal = nameCrc.getValue();

        ByteArrayOutputStream extraOut = new ByteArrayOutputStream();
        extraOut.write(0x75); // Header ID 0x7075
        extraOut.write(0x70);
        int extraDataLen = 1 + 4 + unicodeNameBytes.length;
        extraOut.write(extraDataLen & 0xFF);
        extraOut.write((extraDataLen >> 8) & 0xFF);
        extraOut.write(1); // Version 1
        extraOut.write((int) (nameCrcVal & 0xFF));
        extraOut.write((int) ((nameCrcVal >> 8) & 0xFF));
        extraOut.write((int) ((nameCrcVal >> 16) & 0xFF));
        extraOut.write((int) ((nameCrcVal >> 24) & 0xFF));
        extraOut.write(unicodeNameBytes);
        byte[] extraFieldBytes = extraOut.toByteArray();

        long lfhOffset = baos.size();

        // Local File Header (LFH)
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04}); // LFH Signature
        baos.write(new byte[]{20, 0}); // Version needed: 2.0
        baos.write(new byte[]{0, 0}); // GP Bit Flag: NO UTF-8 flag set
        baos.write(new byte[]{0, 0}); // Compression: STORED (0)
        baos.write(new byte[]{0, 0, 0, 0}); // Mod time & date
        baos.write(toBytes4(crcValue));
        baos.write(toBytes4(content.length));
        baos.write(toBytes4(content.length));
        baos.write(toBytes2(asciiNameBytes.length));
        baos.write(toBytes2(extraFieldBytes.length));
        baos.write(asciiNameBytes);
        baos.write(extraFieldBytes);
        baos.write(content);

        long cdhOffset = baos.size();

        // Central Directory Header (CDH)
        baos.write(new byte[]{0x50, 0x4b, 0x01, 0x02}); // CDH Signature
        baos.write(new byte[]{20, 0}); // Version made by
        baos.write(new byte[]{20, 0}); // Version needed
        baos.write(new byte[]{0, 0}); // GP Bit Flag: NO UTF-8 flag set
        baos.write(new byte[]{0, 0}); // Method: STORED
        baos.write(new byte[]{0, 0, 0, 0}); // Time & date
        baos.write(toBytes4(crcValue));
        baos.write(toBytes4(content.length));
        baos.write(toBytes4(content.length));
        baos.write(toBytes2(asciiNameBytes.length));
        baos.write(toBytes2(extraFieldBytes.length));
        baos.write(new byte[]{0, 0}); // Comment length
        baos.write(new byte[]{0, 0}); // Disk start
        baos.write(new byte[]{0, 0}); // Internal attrs
        baos.write(new byte[]{0, 0, 0, 0}); // External attrs
        baos.write(toBytes4(lfhOffset)); // Relative offset of LFH
        baos.write(asciiNameBytes);
        baos.write(extraFieldBytes);

        long cdhSize = baos.size() - cdhOffset;

        // End of Central Directory Record (EOCD)
        baos.write(new byte[]{0x50, 0x4b, 0x05, 0x06}); // EOCD Signature
        baos.write(new byte[]{0, 0}); // Disk number
        baos.write(new byte[]{0, 0}); // CD start disk
        baos.write(new byte[]{1, 0}); // Entries on this disk
        baos.write(new byte[]{1, 0}); // Total entries
        baos.write(toBytes4(cdhSize));
        baos.write(toBytes4(cdhOffset));
        baos.write(new byte[]{0, 0}); // Comment len

        File zipFile = createTempZipFile(baos.toByteArray());
        ZipFile zf = new ZipFile(zipFile, "US-ASCII", true);
        try {
            ZipArchiveEntry entry = zf.getEntry(unicodeName);
            assertNotNull("Entry should be indexed under unicode name", entry);
            assertEquals(unicodeName, entry.getName());

            // The defect triggers here: if entries map is corrupted, getInputStream returns null
            InputStream is = zf.getInputStream(entry);
            assertNotNull("InputStream must not be null for Unicode extra field entry", is);

            byte[] buf = new byte[content.length];
            int read = is.read(buf);
            assertEquals(content.length, read);
            assertArrayEquals(content, buf);
            is.close();
        } finally {
            zf.close();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStoredAndDeflatedEntries() throws Exception {
        byte[] data1 = "Uncompressed STORED content".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Deflated DEFLATED content that repeats repeats repeats repeats repeats".getBytes(StandardCharsets.UTF_8);

        File f = File.createTempFile("test_stored_deflated_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze1 = new ZipArchiveEntry("entry1.txt");
            ze1.setMethod(ZipArchiveEntry.STORED);
            ze1.setSize(data1.length);
            CRC32 crc = new CRC32();
            crc.update(data1);
            ze1.setCrc(crc.getValue());
            zaos.putArchiveEntry(ze1);
            zaos.write(data1);
            zaos.closeArchiveEntry();

            ZipArchiveEntry ze2 = new ZipArchiveEntry("entry2.txt");
            ze2.setMethod(ZipArchiveEntry.DEFLATED);
            zaos.putArchiveEntry(ze2);
            zaos.write(data2);
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f);
        try {
            assertEquals("UTF8", zf.getEncoding());

            ZipArchiveEntry e1 = zf.getEntry("entry1.txt");
            assertNotNull(e1);
            assertTrue(zf.canReadEntryData(e1));
            InputStream is1 = zf.getInputStream(e1);
            assertNotNull(is1);
            byte[] read1 = readAll(is1);
            assertArrayEquals(data1, read1);
            is1.close();

            ZipArchiveEntry e2 = zf.getEntry("entry2.txt");
            assertNotNull(e2);
            assertTrue(zf.canReadEntryData(e2));
            InputStream is2 = zf.getInputStream(e2);
            assertNotNull(is2);
            byte[] read2 = readAll(is2);
            assertArrayEquals(data2, read2);
            is2.close();
        } finally {
            zf.close();
        }
    }

    @Test(timeout = 4000)
    public void testGetEntriesAndGetEntriesInPhysicalOrder() throws Exception {
        File f = File.createTempFile("order_test_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry zeA = new ZipArchiveEntry("fileA.txt");
            zaos.putArchiveEntry(zeA);
            zaos.write("A".getBytes(StandardCharsets.UTF_8));
            zaos.closeArchiveEntry();

            ZipArchiveEntry zeB = new ZipArchiveEntry("fileB.txt");
            zaos.putArchiveEntry(zeB);
            zaos.write("B".getBytes(StandardCharsets.UTF_8));
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f.getAbsolutePath());
        try {
            Enumeration<ZipArchiveEntry> en = zf.getEntries();
            assertTrue(en.hasMoreElements());
            assertEquals("fileA.txt", en.nextElement().getName());
            assertTrue(en.hasMoreElements());
            assertEquals("fileB.txt", en.nextElement().getName());
            assertFalse(en.hasMoreElements());

            Enumeration<ZipArchiveEntry> phys = zf.getEntriesInPhysicalOrder();
            assertTrue(phys.hasMoreElements());
            assertEquals("fileA.txt", phys.nextElement().getName());
            assertTrue(phys.hasMoreElements());
            assertEquals("fileB.txt", phys.nextElement().getName());
            assertFalse(phys.hasMoreElements());
        } finally {
            zf.close();
        }
    }

    @Test(timeout = 4000)
    public void testBoundedInputStreamEdgeCases() throws Exception {
        byte[] payload = new byte[]{1, 2, 3, 4, 5};
        File f = File.createTempFile("bounded_stream_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("bytes.bin");
            ze.setMethod(ZipArchiveEntry.STORED);
            ze.setSize(payload.length);
            CRC32 crc = new CRC32();
            crc.update(payload);
            ze.setCrc(crc.getValue());
            zaos.putArchiveEntry(ze);
            zaos.write(payload);
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f);
        try {
            ZipArchiveEntry entry = zf.getEntry("bytes.bin");
            InputStream is = zf.getInputStream(entry);
            assertNotNull(is);

            // Read with len <= 0
            byte[] buf = new byte[10];
            assertEquals(0, is.read(buf, 0, 0));

            // Single byte read
            int b0 = is.read();
            assertEquals(1, b0);

            // Read remaining with buffer larger than remaining
            int read = is.read(buf, 0, buf.length);
            assertEquals(4, read);
            assertEquals(2, buf[0]);
            assertEquals(3, buf[1]);
            assertEquals(4, buf[2]);
            assertEquals(5, buf[3]);

            // Stream exhausted
            assertEquals(-1, is.read());
            assertEquals(-1, is.read(buf, 0, buf.length));

            is.close();
        } finally {
            zf.close();
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Signatures
    // =========================================================================

    @Test(timeout = 4000)
    public void testArchiveWithEOCDComment() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] comment = "Test zip archive comment".getBytes(StandardCharsets.UTF_8);

        // Empty zip with archive comment
        baos.write(new byte[]{0x50, 0x4b, 0x05, 0x06}); // EOCD Sig
        baos.write(new byte[]{0, 0}); // Disk
        baos.write(new byte[]{0, 0}); // CD Disk
        baos.write(new byte[]{0, 0}); // Disk entries
        baos.write(new byte[]{0, 0}); // Total entries
        baos.write(new byte[]{0, 0, 0, 0}); // CD Size
        baos.write(new byte[]{0, 0, 0, 0}); // CD Offset
        baos.write(toBytes2(comment.length));
        baos.write(comment);

        File f = createTempZipFile(baos.toByteArray());
        ZipFile zf = new ZipFile(f);
        try {
            assertFalse(zf.getEntries().hasMoreElements());
            assertNull(zf.getEntry("any"));
        } finally {
            zf.close();
        }
    }

    @Test(timeout = 4000)
    public void testZip64CentralDirectoryParsing() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        long cfdOffset = 0;
        long cfdSize = 0;

        // Zip64 End of Central Directory Record
        long zip64EOCDRecordOffset = baos.size();
        baos.write(new byte[]{0x50, 0x4b, 0x06, 0x06}); // Zip64 EOCD Sig
        baos.write(toBytes8(44L)); // Size of remaining zip64 EOCD record
        baos.write(new byte[]{45, 0}); // Version made by
        baos.write(new byte[]{45, 0}); // Version needed
        baos.write(new byte[]{0, 0, 0, 0}); // Disk number
        baos.write(new byte[]{0, 0, 0, 0}); // CD start disk
        baos.write(toBytes8(0)); // Disk entries
        baos.write(toBytes8(0)); // Total entries
        baos.write(toBytes8(cfdSize)); // Size of CD
        baos.write(toBytes8(cfdOffset)); // Offset of CD

        // Zip64 End of Central Directory Locator
        baos.write(new byte[]{0x50, 0x4b, 0x06, 0x07}); // Zip64 EOCD Locator Sig
        baos.write(new byte[]{0, 0, 0, 0}); // Disk with start of Zip64 EOCD
        baos.write(toBytes8(zip64EOCDRecordOffset)); // Offset of Zip64 EOCD record
        baos.write(new byte[]{1, 0, 0, 0}); // Total disks

        // Standard EOCD Record (values set to 0xFFFF / 0xFFFFFFFF for Zip64)
        baos.write(new byte[]{0x50, 0x4b, 0x05, 0x06});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{0, 0});
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF});
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF});
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        baos.write(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF});
        baos.write(new byte[]{0, 0}); // Comment length 0

        File f = createTempZipFile(baos.toByteArray());
        ZipFile zf = new ZipFile(f);
        try {
            assertFalse(zf.getEntries().hasMoreElements());
        } finally {
            zf.close();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ZipException.class, timeout = 4000)
    public void testCorruptZipNotAZipArchive() throws Exception {
        byte[] corruptData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        File f = createTempZipFile(corruptData);
        new ZipFile(f);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCentralDirectoryEmptyStartsWithLFH() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04}); // LFH Sig
        baos.write(new byte[26]); // LFH remainder
        // EOCD pointing to non-existent central directory
        baos.write(new byte[]{0x50, 0x4b, 0x05, 0x06});
        baos.write(new byte[6]);
        baos.write(new byte[]{1, 0}); // 1 entry reported
        baos.write(new byte[]{0, 0, 0, 0}); // Size of CD 0
        baos.write(new byte[]{0, 0, 0, 0}); // Offset of CD 0 (points to LFH, not CDH)
        baos.write(new byte[]{0, 0});

        File f = createTempZipFile(baos.toByteArray());
        new ZipFile(f);
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testCorruptZip64LocatorSignatureMismatch() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long corruptOffset = baos.size();
        baos.write(new byte[]{0x50, 0x4b, 0x00, 0x00}); // Corrupt Sig (not 0x0606)
        baos.write(new byte[50]);

        // Zip64 Locator
        baos.write(new byte[]{0x50, 0x4b, 0x06, 0x07});
        baos.write(new byte[]{0, 0, 0, 0});
        baos.write(toBytes8(corruptOffset));
        baos.write(new byte[]{1, 0, 0, 0});

        // EOCD
        baos.write(new byte[]{0x50, 0x4b, 0x05, 0x06});
        baos.write(new byte[18]);

        File f = createTempZipFile(baos.toByteArray());
        new ZipFile(f);
    }

    @Test(timeout = 4000)
    public void testGetInputStreamForUnknownEntryReturnsNull() throws Exception {
        File f = File.createTempFile("empty_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("contained.txt");
            zaos.putArchiveEntry(ze);
            zaos.write("content".getBytes(StandardCharsets.UTF_8));
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f);
        try {
            ZipArchiveEntry alien = new ZipArchiveEntry("alien.txt");
            assertNull(zf.getInputStream(alien));
        } finally {
            zf.close();
        }
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testUnsupportedCompressionMethodThrowsZipException() throws Exception {
        File f = File.createTempFile("unsupported_method_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("unsupported.bin");
            ze.setSize(10);
            zaos.putArchiveEntry(ze);
            zaos.write(new byte[10]);
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f);
        try {
            ZipArchiveEntry ze = zf.getEntry("unsupported.bin");
            assertNotNull(ze);
            ze.setMethod(99); // Unsupported compression method
            zf.getInputStream(ze);
        } finally {
            zf.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Clean-up
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseQuietlyHandlesNullAndValid() throws Exception {
        ZipFile.closeQuietly(null);

        File f = File.createTempFile("quietly_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("q.txt");
            zaos.putArchiveEntry(ze);
            zaos.write(new byte[]{42});
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f);
        ZipFile.closeQuietly(zf);
        // Repeated closeQuietly should not throw
        ZipFile.closeQuietly(zf);
    }

    @Test(timeout = 4000)
    public void testMultipleCloseCalls() throws Exception {
        File f = File.createTempFile("multiclose_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("m.txt");
            zaos.putArchiveEntry(ze);
            zaos.write(new byte[]{1});
            zaos.closeArchiveEntry();
        }

        ZipFile zf = new ZipFile(f.getPath(), "UTF-8");
        assertEquals("UTF-8", zf.getEncoding());
        zf.close();
        // Second close
        zf.close();
    }

    @Test(timeout = 4000)
    public void testFinalizeExecution() throws Throwable {
        File f = File.createTempFile("finalize_test_", ".zip");
        f.deleteOnExit();
        tempFiles.add(f);

        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(f)) {
            ZipArchiveEntry ze = new ZipArchiveEntry("test.txt");
            zaos.putArchiveEntry(ze);
            zaos.write(new byte[]{1, 2, 3});
            zaos.closeArchiveEntry();
        }

        class TestZipFileSubclass extends ZipFile {
            public TestZipFileSubclass(File file) throws IOException {
                super(file);
            }
            @Override
            public void finalize() throws Throwable {
                super.finalize();
            }
        }

        TestZipFileSubclass zf = new TestZipFileSubclass(f);
        // Call finalize without prior close() to trigger cleanup branch
        zf.finalize();

        // Calling finalize after already closed
        zf.finalize();
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private static byte[] readAll(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private static byte[] toBytes2(int val) {
        return new byte[]{(byte) (val & 0xFF), (byte) ((val >> 8) & 0xFF)};
    }

    private static byte[] toBytes4(long val) {
        return new byte[]{
                (byte) (val & 0xFF),
                (byte) ((val >> 8) & 0xFF),
                (byte) ((val >> 16) & 0xFF),
                (byte) ((val >> 24) & 0xFF)
        };
    }

    private static byte[] toBytes8(long val) {
        return new byte[]{
                (byte) (val & 0xFF),
                (byte) ((val >> 8) & 0xFF),
                (byte) ((val >> 16) & 0xFF),
                (byte) ((val >> 24) & 0xFF),
                (byte) ((val >> 32) & 0xFF),
                (byte) ((val >> 40) & 0xFF),
                (byte) ((val >> 48) & 0xFF),
                (byte) ((val >> 56) & 0xFF)
        };
    }
}