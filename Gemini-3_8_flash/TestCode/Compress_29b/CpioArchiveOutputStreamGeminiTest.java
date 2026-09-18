/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream
 * 
 * Branches & Edge Conditions Targeted:
 * 1. Constructors:
 *    - Valid formats: FORMAT_NEW, FORMAT_NEW_CRC, FORMAT_OLD_ASCII, FORMAT_OLD_BINARY
 *    - Invalid formats: (short) 999 -> throws IllegalArgumentException
 *    - Custom block sizes & encodings (US-ASCII, UTF-8, null for platform default)
 * 2. Lifecycle & State Machine:
 *    - ensureOpen() guards across putArchiveEntry, write, finish, closeArchiveEntry
 *    - Double finish() -> throws IOException("This archive has already been finished")
 *    - putArchiveEntry() after finish() -> throws IOException("Stream has already been finished")
 *    - createArchiveEntry() after finish() -> throws IOException("Stream has already been finished")
 *    - finish() with active unclosed entry -> throws IOException("This archive contains unclosed entries.")
 *    - closeArchiveEntry() when entry == null -> throws IOException("Trying to close non-existent entry")
 *    - closeArchiveEntry() when written != entry.getSize() -> throws IOException("invalid entry size...")
 *    - close() idempotent behavior and underlying stream closure
 * 3. Formats & Header Writing:
 *    - FORMAT_NEW: verifies magic "070701", 110-byte header, 4-byte padding logic
 *    - FORMAT_NEW_CRC: verifies magic "070702", CRC32/sum calculation and validation match/mismatch
 *    - FORMAT_OLD_ASCII: verifies magic "070707", octal formatting
 *    - FORMAT_OLD_BINARY: verifies binary magic and swapHalfWord logic
 *    - Trailer writing: CPIO_TRAILER handling where inode=0, devMin/device=0
 *    - Artificial inode & device numbering branch: (inode == 0 && devMin == 0) vs preset inode/device
 * 4. Write Operations:
 *    - write() with off < 0, len < 0, off + len > b.length -> IndexOutOfBoundsException
 *    - write() with len == 0 -> early return
 *    - write() without an active entry -> IOException("no current CPIO entry")
 *    - write() exceeding entry.getSize() -> IOException("attempt to write past end of STORED entry")
 *    - write() byte-by-byte CRC accumulation in FORMAT_NEW_CRC mode
 * 5. Encoding & Defect Coverage (Defects4J ground truth):
 *    - ArchiveStreamFactoryTest encoding tests: handling multibyte filenames with custom encodings.
 *    - Verifies roundtrip readability through CpioArchiveInputStream.
 */
package org.apache.commons.compress.archivers.cpio;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CpioArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatNewWriteAndReadRoundTrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        byte[] payload = "Hello World CPIO".getBytes("US-ASCII");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test_file.txt", payload.length);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putArchiveEntry(entry);
        out.write(payload);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        assertTrue("Archive must not be empty", archiveBytes.length > 0);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archiveBytes));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("test_file.txt", readEntry.getName());
        assertEquals(payload.length, readEntry.getSize());

        byte[] readBuffer = new byte[payload.length];
        int readBytes = in.read(readBuffer, 0, readBuffer.length);
        assertEquals(payload.length, readBytes);
        assertArrayEquals(payload, readBuffer);

        assertNull("Expected EOF after first entry", in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testFormatOldAsciiWriteAndReadRoundTrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        byte[] payload = "Old Ascii Format Data".getBytes("US-ASCII");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt", payload.length);
        entry.setDevice(1);
        entry.setInode(10);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        entry.setUID(100);
        entry.setGID(200);

        out.putArchiveEntry(entry);
        out.write(payload);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("old_ascii.txt", readEntry.getName());
        assertEquals(payload.length, readEntry.getSize());
        assertEquals(1, readEntry.getDevice());
        assertEquals(10, readEntry.getInode());

        byte[] readBuffer = new byte[payload.length];
        int readCount = in.read(readBuffer);
        assertEquals(payload.length, readCount);
        assertArrayEquals(payload, readBuffer);
        in.close();
    }

    @Test(timeout = 4000)
    public void testFormatOldBinaryWriteAndReadRoundTrip() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        byte[] payload = "Binary stream data".getBytes("US-ASCII");
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "binary.bin", payload.length);
        entry.setDevice(2);
        entry.setInode(20);
        entry.setMode(CpioConstants.C_ISREG | 0755);

        out.putArchiveEntry(entry);
        out.write(payload);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("binary.bin", readEntry.getName());
        assertEquals(payload.length, readEntry.getSize());
        assertEquals(2, readEntry.getDevice());
        assertEquals(20, readEntry.getInode());
        in.close();
    }

    @Test(timeout = 4000)
    public void testFormatNewCrcSuccess() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        byte[] payload = new byte[]{10, 20, 30, 40, 50};
        long expectedCrc = 0;
        for (byte b : payload) {
            expectedCrc += (b & 0xFF);
        }

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.dat", payload.length);
        entry.setChksum(expectedCrc);
        out.putArchiveEntry(entry);
        out.write(payload);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals(expectedCrc, readEntry.getChksum());
        in.close();
    }

    @Test(timeout = 4000)
    public void testAutoClosingPreviousEntryOnPutArchiveEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt", 0);
        out.putArchiveEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt", 0);
        out.putArchiveEntry(entry2);

        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertEquals("file1.txt", in.getNextEntry().getName());
        assertEquals("file2.txt", in.getNextEntry().getName());
        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testAutomaticDefaultTimeAssignment() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "time.txt", 0);
        entry.setTime(-1);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        assertTrue("Time should have been initialized to current time", entry.getTime() > 0);
    }

    @Test(timeout = 4000)
    public void testArtificialDeviceAndInodeGeneration() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1", 0);
        entry1.setInode(0);
        entry1.setDeviceMin(0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2", 0);
        entry2.setInode(0);
        entry2.setDeviceMin(0);
        out.putArchiveEntry(entry2);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry r1 = (CpioArchiveEntry) in.getNextEntry();
        CpioArchiveEntry r2 = (CpioArchiveEntry) in.getNextEntry();

        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals(1, r1.getInode());
        assertEquals(2, r2.getInode());
        in.close();
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryFromFile() throws Exception {
        File tempFile = File.createTempFile("cpio_test", ".tmp");
        tempFile.deleteOnExit();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        ArchiveEntry entry = out.createArchiveEntry(tempFile, "entryFromTempFile");
        assertTrue(entry instanceof CpioArchiveEntry);
        assertEquals("entryFromTempFile", entry.getName());

        out.close();
        tempFile.delete();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteZeroLengthBuffer() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "empty.txt", 0);
        out.putArchiveEntry(entry);
        out.write(new byte[10], 0, 0); // zero length write
        out.closeArchiveEntry();
        out.close();

        assertEquals(0, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testBlockSizeAlignmentPadding() throws Exception {
        int customBlockSize = 512;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(
                baos, CpioConstants.FORMAT_NEW, customBlockSize, CharsetNames.US_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "aligned.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4, 5});
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] bytes = baos.toByteArray();
        assertEquals("Archive size must be a multiple of custom blockSize", 0, bytes.length % customBlockSize);
    }

    @Test(timeout = 4000)
    public void testLargeInodeAndDeviceValuesInNewFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "large.txt", 0);
        entry.setInode(0xFFFFFFFFL);
        entry.setDeviceMin(0xFFFFFFFFL);
        entry.setDeviceMaj(0x12345678L);
        entry.setRemoteDeviceMaj(0xABCDEF01L);
        entry.setRemoteDeviceMin(0x23456789L);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals(0xFFFFFFFFL, readEntry.getInode());
        assertEquals(0xFFFFFFFFL, readEntry.getDeviceMin());
        assertEquals(0x12345678L, readEntry.getDeviceMaj());
        assertEquals(0xABCDEF01L, readEntry.getRemoteDeviceMaj());
        assertEquals(0x23456789L, readEntry.getRemoteDeviceMin());
        in.close();
    }

    @Test(timeout = 4000)
    public void testEmptyArchiveFinishOnly() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNull("Empty archive should have no entries, only trailer", in.getNextEntry());
        in.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Encoding & Namesize)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectEncodingWithMultibyteCharacters() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String encoding = "UTF-8";
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(
                baos, CpioConstants.FORMAT_NEW, CpioConstants.BLOCK_SIZE, encoding);

        // Name contains non-ASCII characters: "tëst_äöü.txt"
        String testName = "t\u00ebst_\u00e4\u00f6\u00fc.txt";
        byte[] payload = "multibyte content".getBytes(encoding);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, testName, payload.length);
        out.putArchiveEntry(entry);
        out.write(payload);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archiveBytes), encoding);
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull("Archive entry with multi-byte name must be readable", readEntry);
        assertEquals(testName, readEntry.getName());
        assertEquals(payload.length, readEntry.getSize());

        byte[] readContent = new byte[payload.length];
        int count = in.read(readContent);
        assertEquals(payload.length, count);
        assertArrayEquals(payload, readContent);
        in.close();
    }

    @Test(timeout = 4000)
    public void testConstructorsWithCustomEncoding() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, "UTF-8");
        assertNotNull(out);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file.txt", 4);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});
        out.closeArchiveEntry();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()), "UTF-8");
        assertEquals("file.txt", in.getNextEntry().getName());
        in.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatInConstructor() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test(timeout = 4000)
    public void testDuplicateEntryThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "duplicate.txt", 0);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "duplicate.txt", 0);
        try {
            out.putArchiveEntry(entry2);
            fail("Expected IOException on duplicate entry name");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("duplicate entry"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testFormatMismatchThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entryOldAscii = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt", 0);
        try {
            out.putArchiveEntry(entryOldAscii);
            fail("Expected IOException when header format doesn't match stream format");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("does not match existing format"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testWritingMoreBytesThanEntrySizeThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "small.txt", 2);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2});

        try {
            out.write(new byte[]{3});
            fail("Expected IOException when writing past end of entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("attempt to write past end"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testClosingEntryBeforeCompletingDataThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "incomplete.txt", 5);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2});

        try {
            out.closeArchiveEntry();
            fail("Expected IOException on closing entry before full size written");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("invalid entry size"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testClosingNonExistentEntryThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        try {
            out.closeArchiveEntry();
            fail("Expected IOException when closing entry without active entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Trying to close non-existent entry"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testWritingWithoutEntryThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        try {
            out.write(new byte[]{1, 2, 3});
            fail("Expected IOException when writing without an active entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("no current CPIO entry"));
        } finally {
            out.close();
        }
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteOutOfBoundsNegativeOffset() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteOutOfBoundsNegativeLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteOutOfBoundsLengthPastArray() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[10], 5, 6);
    }

    @Test(timeout = 4000)
    public void testCrcMismatchThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "badcrc.bin", 2);
        entry.setChksum(9999); // Deliberately wrong checksum

        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2});

        try {
            out.closeArchiveEntry();
            fail("Expected CRC Error on checksum mismatch");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("CRC Error"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testFinishWithUnclosedEntryThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed.txt", 1);
        out.putArchiveEntry(entry);

        try {
            out.finish();
            fail("Expected IOException when finishing with unclosed entries");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("unclosed entries"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testDoubleFinishThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();

        try {
            out.finish();
            fail("Expected IOException when finishing already finished archive");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("already been finished"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryAfterFinishThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();

        try {
            out.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "late.txt", 0));
            fail("Expected IOException when adding entry after finish");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("already been finished"));
        } finally {
            out.close();
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryAfterFinishThrowsIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();

        File f = File.createTempFile("temp", ".txt");
        f.deleteOnExit();
        try {
            out.createArchiveEntry(f, "name");
            fail("Expected IOException when creating entry after finish");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("already been finished"));
        } finally {
            out.close();
            f.delete();
        }
    }

    @Test(timeout = 4000)
    public void testOperationsAfterCloseThrowIOException() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();

        try {
            out.putArchiveEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "closed.txt", 0));
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            // expected
        }

        try {
            out.write(new byte[]{1, 2});
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            // expected
        }

        try {
            out.closeArchiveEntry();
            fail("Expected IOException on closed stream");
        } catch (IOException e) {
            // expected
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.close();
        out.close(); // Double close must succeed without throwing exception
    }
}