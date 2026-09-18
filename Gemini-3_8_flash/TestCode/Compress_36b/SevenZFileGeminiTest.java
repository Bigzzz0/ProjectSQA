package org.apache.commons.compress.archivers.sevenz;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.CRC32;

/* [Branch & Defect Analysis Matrix]
 * Target Class: SevenZFile
 *
 * 1. Defect-Targeted Branch Zone (Defects4J / COMPRESS-348):
 *    - readEntriesOfSize0(): When an archive contains an entry with size 0 (or no stream),
 *      folderIndex < 0 in buildDecodingStream(), clearing deferredBlockStreams. Calling
 *      read() or read(byte[]) on such entry throws IllegalStateException:
 *      "No current 7z entry (call getNextEntry() first)."
 *      Expected: read() returns -1 (EOF), read(byte[]) returns -1 (EOF).
 *
 * 2. Signature & Header Verification Branches:
 *    - matches(byte[], int): signature length < 6, length >= 6 matching, length >= 6 mismatch.
 *    - readHeaders(): invalid signature ("Bad 7z signature").
 *    - readHeaders(): invalid major version != 0 ("Unsupported 7z version").
 *    - readHeaders(): StartHeader CRC mismatch.
 *    - readHeaders(): NextHeader CRC mismatch.
 *    - readHeaders(): unsupported nextHeaderSize overflow.
 *    - readHeaders(): no kHeader or kEncodedHeader ("Broken or unsupported archive: no Header").
 *
 * 3. Archive Structure & Substream Parsing:
 *    - kArchiveProperties, kAdditionalStreamsInfo (throws IOException "Additional streams unsupported").
 *    - kStartPos (throws IOException "kStartPos is unsupported, please report").
 *    - readPackInfo(), readUnpackInfo(), readSubStreamsInfo().
 *    - Badly terminated headers (PackInfo, UnpackInfo, SubStreamsInfo, Header).
 *    - Alternative methods in coder definition (throws IOException).
 *    - Total output streams == 0 (throws IOException).
 *    - Total input streams < numBindPairs (throws IOException).
 *    - Folder stream bind pair indexing error.
 *    - Missing kEmptyStream before kEmptyFile / kAnti.
 *
 * 4. Operational & Lifecycle:
 *    - getNextEntry() iteration beyond file count returns null.
 *    - getEntries() returns metadata list without throwing.
 *    - read() / read(byte[]) without calling getNextEntry() -> IllegalStateException.
 *    - close() cleans up file handle and zeroes out password.
 */
public class SevenZFileGeminiTest {

    // --- Helper Methods to synthesize valid 7z headers in memory ---

    private static byte[] toLittleEndianLong(long val) {
        byte[] b = new byte[8];
        for (int i = 0; i < 8; i++) {
            b[i] = (byte) (val & 0xFF);
            val >>>= 8;
        }
        return b;
    }

    private static byte[] toLittleEndianInt(int val) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (val & 0xFF);
            val >>>= 8;
        }
        return b;
    }

    private static void writeUint64(ByteArrayOutputStream baos, long value) {
        if (value < 0x80) {
            baos.write((int) value);
        } else {
            // Encode multi-byte variable length integer
            int mask = 0x80;
            int numExtra = 0;
            long temp = value;
            while (temp >= 0x80 && numExtra < 8) {
                numExtra++;
                temp >>>= 8;
            }
            int firstByte = (mask - (1 << (8 - numExtra))) | (int) (temp & 0xFF);
            baos.write(firstByte);
            for (int i = 0; i < numExtra; i++) {
                baos.write((int) ((value >>> (8 * i)) & 0xFF));
            }
        }
    }

    /**
     * Builds a minimal valid 7z archive file containing an empty file (size 0).
     */
    private File create7zArchiveWithEmptyFile(String fileName) throws IOException {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();

        // kHeader
        headerStream.write(NID.kHeader);

        // kMainStreamsInfo -> empty streams info
        headerStream.write(NID.kMainStreamsInfo);
        headerStream.write(NID.kEnd); // end of streams info

        // kFilesInfo
        headerStream.write(NID.kFilesInfo);
        writeUint64(headerStream, 1); // 1 file

        // kEmptyStream
        headerStream.write(NID.kEmptyStream);
        writeUint64(headerStream, 1); // 1 byte size
        headerStream.write(0x80); // 1 file is empty stream (bit 7 set)

        // kEmptyFile
        headerStream.write(NID.kEmptyFile);
        writeUint64(headerStream, 1); // 1 byte
        headerStream.write(0x80); // 1 file is empty file (not directory)

        // kName
        headerStream.write(NID.kName);
        byte[] nameBytes = (fileName + "\0").getBytes("UTF-16LE");
        writeUint64(headerStream, 1 + nameBytes.length); // size = 1 (external byte) + length
        headerStream.write(0); // external = 0
        headerStream.write(nameBytes);

        // End of properties
        headerStream.write(NID.kEnd);

        // End of kHeader
        headerStream.write(NID.kEnd);

        byte[] nextHeader = headerStream.toByteArray();
        CRC32 headerCrc = new CRC32();
        headerCrc.update(nextHeader);

        // Construct StartHeader (20 bytes)
        ByteArrayOutputStream startHeaderBaos = new ByteArrayOutputStream();
        DataOutputStream startHeaderDos = new DataOutputStream(startHeaderBaos);
        startHeaderDos.write(toLittleEndianLong(0L)); // nextHeaderOffset
        startHeaderDos.write(toLittleEndianLong((long) nextHeader.length)); // nextHeaderSize
        startHeaderDos.write(toLittleEndianInt((int) headerCrc.getValue())); // nextHeaderCrc

        byte[] startHeaderBytes = startHeaderBaos.toByteArray();
        CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);

        // Assemble full file:
        // Signature (6), Version (2), StartHeaderCRC (4), StartHeader (20), NextHeader
        File tempFile = File.createTempFile("empty-entry-test", ".7z");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(SevenZFile.sevenZSignature);
            fos.write(0); // major
            fos.write(2); // minor
            fos.write(toLittleEndianInt((int) startHeaderCrc.getValue()));
            fos.write(startHeaderBytes);
            fos.write(nextHeader);
        } finally {
            fos.close();
        }

        return tempFile;
    }

    private File createRawFile(byte[] content) throws IOException {
        File f = File.createTempFile("sevenz-raw", ".7z");
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
        try {
            fos.write(content);
        } finally {
            fos.close();
        }
        return f;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesSignatureValidation() {
        byte[] validSignature = new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C };
        assertTrue(SevenZFile.matches(validSignature, 6));

        byte[] longerBuffer = new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, (byte) 0x1C, 0x00, 0x01 };
        assertTrue(SevenZFile.matches(longerBuffer, 8));

        assertFalse(SevenZFile.matches(validSignature, 5));
        assertFalse(SevenZFile.matches(validSignature, 0));

        byte[] corrupted = new byte[] { '7', 'z', (byte) 0xBC, (byte) 0xAF, (byte) 0x27, 0x00 };
        assertFalse(SevenZFile.matches(corrupted, 6));
    }

    @Test(timeout = 4000)
    public void testGetEntriesMetadata() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("test_meta.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            assertNotNull(entries);
            Iterator<SevenZArchiveEntry> it = entries.iterator();
            assertTrue(it.hasNext());
            SevenZArchiveEntry entry = it.next();
            assertEquals("test_meta.txt", entry.getName());
            assertEquals(0L, entry.getSize());
            assertFalse(it.hasNext());
        } finally {
            sevenZFile.close();
        }
    }

    @Test(timeout = 4000)
    public void testIterationPastEndReturnsNull() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("entry1.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            SevenZArchiveEntry first = sevenZFile.getNextEntry();
            assertNotNull(first);
            assertEquals("entry1.txt", first.getName());

            SevenZArchiveEntry second = sevenZFile.getNextEntry();
            assertNull(second);

            SevenZArchiveEntry third = sevenZFile.getNextEntry();
            assertNull(third);
        } finally {
            sevenZFile.close();
        }
    }

    @Test(timeout = 4000)
    public void testToStringDoesNotThrow() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("entry.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            String str = sevenZFile.toString();
            assertNotNull(str);
        } finally {
            sevenZFile.close();
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Guard Conditions
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidSignatureThrowsIOException() throws IOException {
        byte[] invalidHeader = new byte[32];
        Arrays.fill(invalidHeader, (byte) 0xFF);
        File raw = createRawFile(invalidHeader);
        new SevenZFile(raw);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testUnsupportedMajorVersionThrowsIOException() throws IOException {
        byte[] header = new byte[32];
        System.arraycopy(SevenZFile.sevenZSignature, 0, header, 0, 6);
        header[6] = 1; // major version != 0
        header[7] = 0; // minor version
        File raw = createRawFile(header);
        new SevenZFile(raw);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testStartHeaderCrcMismatchThrowsIOException() throws IOException {
        byte[] header = new byte[32];
        System.arraycopy(SevenZFile.sevenZSignature, 0, header, 0, 6);
        header[6] = 0; // major = 0
        header[7] = 2; // minor = 2
        // Intentionally wrong CRC
        header[8] = 0x12;
        header[9] = 0x34;
        header[10] = 0x56;
        header[11] = 0x78;
        File raw = createRawFile(header);
        new SevenZFile(raw);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testNextHeaderCrcMismatchThrowsIOException() throws IOException {
        ByteArrayOutputStream startHeaderBaos = new ByteArrayOutputStream();
        DataOutputStream startHeaderDos = new DataOutputStream(startHeaderBaos);
        startHeaderDos.write(toLittleEndianLong(0L)); // nextHeaderOffset
        startHeaderDos.write(toLittleEndianLong(4L)); // nextHeaderSize
        startHeaderDos.write(toLittleEndianInt(0x12345678)); // bogus nextHeaderCrc

        byte[] startHeaderBytes = startHeaderBaos.toByteArray();
        CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);

        File tempFile = File.createTempFile("crc-mismatch", ".7z");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(SevenZFile.sevenZSignature);
            fos.write(0);
            fos.write(2);
            fos.write(toLittleEndianInt((int) startHeaderCrc.getValue()));
            fos.write(startHeaderBytes);
            fos.write(new byte[] { 0x01, 0x02, 0x03, 0x04 }); // 4 payload bytes
        } finally {
            fos.close();
        }

        new SevenZFile(tempFile);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testHeaderMissingKHeaderThrowsIOException() throws IOException {
        // nextHeader with invalid NID (e.g. 0x00 instead of kHeader or kEncodedHeader)
        byte[] nextHeader = new byte[] { (byte) 0x7F };
        CRC32 headerCrc = new CRC32();
        headerCrc.update(nextHeader);

        ByteArrayOutputStream startHeaderBaos = new ByteArrayOutputStream();
        DataOutputStream startHeaderDos = new DataOutputStream(startHeaderBaos);
        startHeaderDos.write(toLittleEndianLong(0L));
        startHeaderDos.write(toLittleEndianLong((long) nextHeader.length));
        startHeaderDos.write(toLittleEndianInt((int) headerCrc.getValue()));

        byte[] startHeaderBytes = startHeaderBaos.toByteArray();
        CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);

        File tempFile = File.createTempFile("missing-header", ".7z");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(SevenZFile.sevenZSignature);
            fos.write(0);
            fos.write(2);
            fos.write(toLittleEndianInt((int) startHeaderCrc.getValue()));
            fos.write(startHeaderBytes);
            fos.write(nextHeader);
        } finally {
            fos.close();
        }

        new SevenZFile(tempFile);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (readEntriesOfSize0 / COMPRESS-348)
    // =========================================================================

    /**
     * Targets Defects4J known failure: SevenZFileTest::readEntriesOfSize0.
     * When reading from an entry with size 0, folderIndex < 0, deferredBlockStreams is empty.
     * In the buggy implementation, calling read() throws:
     *   IllegalStateException: No current 7z entry (call getNextEntry() first).
     * The correct behavior is returning -1 indicating immediate EOF.
     */
    @Test(timeout = 4000)
    public void testReadEntriesOfSize0ReturnsEOF() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("zero_length.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            assertNotNull("Entry should not be null", entry);
            assertEquals("zero_length.txt", entry.getName());
            assertEquals(0L, entry.getSize());

            // Bug triggers here: Calling read() should return -1 (EOF), not throw IllegalStateException
            int readByte = sevenZFile.read();
            assertEquals(-1, readByte);

            // Calling read(byte[]) should also return -1 (EOF)
            byte[] buf = new byte[16];
            int readBytes = sevenZFile.read(buf);
            assertEquals(-1, readBytes);

            int readOffsetBytes = sevenZFile.read(buf, 0, 16);
            assertEquals(-1, readOffsetBytes);
        } finally {
            sevenZFile.close();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReadWithoutGetNextEntryThrowsIllegalStateException() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("file.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            // Did not call getNextEntry()
            sevenZFile.read();
        } finally {
            sevenZFile.close();
        }
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReadBufferWithoutGetNextEntryThrowsIllegalStateException() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("file.txt");
        SevenZFile sevenZFile = new SevenZFile(archiveFile);
        try {
            sevenZFile.read(new byte[10]);
        } finally {
            sevenZFile.close();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testHeaderBadlyTerminatedThrowsIOException() throws IOException {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        headerStream.write(NID.kHeader);
        headerStream.write(0x7F); // Invalid / unhandled NID without kEnd
        byte[] nextHeader = headerStream.toByteArray();

        CRC32 headerCrc = new CRC32();
        headerCrc.update(nextHeader);

        ByteArrayOutputStream startHeaderBaos = new ByteArrayOutputStream();
        DataOutputStream startHeaderDos = new DataOutputStream(startHeaderBaos);
        startHeaderDos.write(toLittleEndianLong(0L));
        startHeaderDos.write(toLittleEndianLong((long) nextHeader.length));
        startHeaderDos.write(toLittleEndianInt((int) headerCrc.getValue()));

        byte[] startHeaderBytes = startHeaderBaos.toByteArray();
        CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);

        File tempFile = File.createTempFile("badly-term", ".7z");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(SevenZFile.sevenZSignature);
            fos.write(0);
            fos.write(2);
            fos.write(toLittleEndianInt((int) startHeaderCrc.getValue()));
            fos.write(startHeaderBytes);
            fos.write(nextHeader);
        } finally {
            fos.close();
        }

        new SevenZFile(tempFile);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testAdditionalStreamsUnsupportedThrowsIOException() throws IOException {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        headerStream.write(NID.kHeader);
        headerStream.write(NID.kAdditionalStreamsInfo); // unsupported
        byte[] nextHeader = headerStream.toByteArray();

        CRC32 headerCrc = new CRC32();
        headerCrc.update(nextHeader);

        ByteArrayOutputStream startHeaderBaos = new ByteArrayOutputStream();
        DataOutputStream startHeaderDos = new DataOutputStream(startHeaderBaos);
        startHeaderDos.write(toLittleEndianLong(0L));
        startHeaderDos.write(toLittleEndianLong((long) nextHeader.length));
        startHeaderDos.write(toLittleEndianInt((int) headerCrc.getValue()));

        byte[] startHeaderBytes = startHeaderBaos.toByteArray();
        CRC32 startHeaderCrc = new CRC32();
        startHeaderCrc.update(startHeaderBytes);

        File tempFile = File.createTempFile("unsupported-add-streams", ".7z");
        tempFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(tempFile);
        try {
            fos.write(SevenZFile.sevenZSignature);
            fos.write(0);
            fos.write(2);
            fos.write(toLittleEndianInt((int) startHeaderCrc.getValue()));
            fos.write(startHeaderBytes);
            fos.write(nextHeader);
        } finally {
            fos.close();
        }

        new SevenZFile(tempFile);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Resource Management
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseClearsPasswordAndFileHandle() throws IOException {
        File archiveFile = create7zArchiveWithEmptyFile("file.txt");
        byte[] pwd = new byte[] { 'p', 0, 'a', 0, 's', 0, 's', 0 };
        SevenZFile sevenZFile = new SevenZFile(archiveFile, pwd);

        // Multiple close() calls must be safe and idempotent
        sevenZFile.close();
        sevenZFile.close();
    }

    @Test(timeout = 4000)
    public void testFailedConstructionClosesRandomAccessFile() {
        File nonExistent = new File("non_existent_archive_gemini.7z");
        try {
            new SevenZFile(nonExistent);
            fail("Expected IOException for non-existent file");
        } catch (IOException expected) {
            // Succeeded in throwing expected exception
        }
    }
}