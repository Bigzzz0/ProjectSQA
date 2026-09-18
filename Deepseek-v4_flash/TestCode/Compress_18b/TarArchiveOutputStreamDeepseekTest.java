package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TarArchiveOutputStream
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructors (6 variants)
 *   - setLongFileMode, setBigNumberMode, setAddPaxHeadersForNonAsciiNames
 *   - putArchiveEntry with normal entry (short name, ASCII)
 *   - write and closeArchiveEntry with correct sizes
 *   - finish and close
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Null/empty entry names
 *   - Long file names (>= NAMELEN) with each longFileMode (ERROR, TRUNCATE, GNU, POSIX)
 *   - Big numbers (size, uid, gid, mtime, dev) with each bigNumberMode (ERROR, STAR, POSIX)
 *   - Non-ASCII names with addPaxHeadersForNonAsciiNames
 *   - Directory entries (size 0)
 *   - Writing exactly record size, less, more, assembly buffer scenarios
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: writing non-ASCII directory name in POSIX mode causes IOException
 *     because PaxHeaders entry becomes a directory (name ends with '/') and currSize=0.
 *     Test: putArchiveEntry with non-ASCII directory name, longFileMode=POSIX.
 *     Expected: no IOException.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - finish() when already finished
 *   - finish() with unclosed entry
 *   - closeArchiveEntry() when finished
 *   - closeArchiveEntry() when no unclosed entry
 *   - write() exceeding currSize
 *   - putArchiveEntry() when finished
 *   - createArchiveEntry() when finished
 *   - Long file name with LONGFILE_ERROR (RuntimeException)
 *   - Big number with BIGNUMBER_ERROR (RuntimeException)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - getRecordSize()
 *   - getBytesWritten() / getCount()
 *   - flush()
 */
public class TarArchiveOutputStreamDeepseekTest {

    // Helper to create a stream with default settings
    private TarArchiveOutputStream createStream() {
        return new TarArchiveOutputStream(new ByteArrayOutputStream());
    }

    // Helper to create a stream with specific encoding
    private TarArchiveOutputStream createStream(String encoding) {
        return new TarArchiveOutputStream(new ByteArrayOutputStream(), encoding);
    }

    // Helper to create a TarArchiveEntry with given name and size
    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        return entry;
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testConstructors() {
        // Default constructor
        TarArchiveOutputStream s1 = new TarArchiveOutputStream(new ByteArrayOutputStream());
        assertNotNull(s1);
        // Constructor with encoding
        TarArchiveOutputStream s2 = new TarArchiveOutputStream(new ByteArrayOutputStream(), "UTF-8");
        assertNotNull(s2);
        // Constructor with blockSize
        TarArchiveOutputStream s3 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024);
        assertNotNull(s3);
        // Constructor with blockSize and encoding
        TarArchiveOutputStream s4 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, "UTF-8");
        assertNotNull(s4);
        // Constructor with blockSize and recordSize
        TarArchiveOutputStream s5 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, 512);
        assertNotNull(s5);
        // Full constructor
        TarArchiveOutputStream s6 = new TarArchiveOutputStream(new ByteArrayOutputStream(), 1024, 512, "UTF-8");
        assertNotNull(s6);
    }

    @Test(timeout = 4000)
    public void testSetLongFileMode() {
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
    }

    @Test(timeout = 4000)
    public void testSetBigNumberMode() {
        TarArchiveOutputStream s = createStream();
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
    }

    @Test(timeout = 4000)
    public void testSetAddPaxHeadersForNonAsciiNames() {
        TarArchiveOutputStream s = createStream();
        s.setAddPaxHeadersForNonAsciiNames(true);
        s.setAddPaxHeadersForNonAsciiNames(false);
    }

    @Test(timeout = 4000)
    public void testPutAndCloseNormalEntry() throws IOException {
        TarArchiveOutputStream s = createStream();
        TarArchiveEntry entry = createEntry("test.txt", 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testPutAndCloseDirectoryEntry() throws IOException {
        TarArchiveOutputStream s = createStream();
        TarArchiveEntry entry = new TarArchiveEntry("mydir/");
        s.putArchiveEntry(entry);
        // No data written for directory
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testFinishAndClose() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        s.close();
    }

    @Test(timeout = 4000)
    public void testGetRecordSize() {
        TarArchiveOutputStream s = createStream();
        assertTrue(s.getRecordSize() > 0);
    }

    @Test(timeout = 4000)
    public void testGetBytesWritten() throws IOException {
        TarArchiveOutputStream s = createStream();
        assertEquals(0, s.getBytesWritten());
        TarArchiveEntry entry = createEntry("test.txt", 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        assertTrue(s.getBytesWritten() > 0);
        s.close();
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.flush();
        s.close();
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testLongFileNameTruncate() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        String longName = "a".repeat(TarConstants.NAMELEN + 10);
        TarArchiveEntry entry = createEntry(longName, 0);
        s.putArchiveEntry(entry);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testLongFileNameError() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        String longName = "a".repeat(TarConstants.NAMELEN + 10);
        TarArchiveEntry entry = createEntry(longName, 0);
        s.putArchiveEntry(entry);
    }

    @Test(timeout = 4000)
    public void testLongFileNameGnu() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        String longName = "a".repeat(TarConstants.NAMELEN + 10);
        TarArchiveEntry entry = createEntry(longName, 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNamePosix() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        String longName = "a".repeat(TarConstants.NAMELEN + 10);
        TarArchiveEntry entry = createEntry(longName, 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testBigNumberError() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);
        TarArchiveEntry entry = createEntry("big", TarConstants.MAXSIZE + 1);
        s.putArchiveEntry(entry);
    }

    @Test(timeout = 4000)
    public void testBigNumberStar() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);
        TarArchiveEntry entry = createEntry("big", TarConstants.MAXSIZE + 1);
        s.putArchiveEntry(entry);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testBigNumberPosix() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
        TarArchiveEntry entry = createEntry("big", TarConstants.MAXSIZE + 1);
        s.putArchiveEntry(entry);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testNonAsciiNameWithPaxHeaders() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = createEntry("f\u00f6\u00f6.txt", 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testNonAsciiLinkNameWithPaxHeaders() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.setAddPaxHeadersForNonAsciiNames(true);
        TarArchiveEntry entry = createEntry("link", 0);
        entry.setLinkName("t\u00e4rget");
        entry.setLink(TarArchiveEntry.LF_SYMLINK);
        s.putArchiveEntry(entry);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testWriteExactRecordSize() throws IOException {
        TarArchiveOutputStream s = createStream();
        int recSize = s.getRecordSize();
        TarArchiveEntry entry = createEntry("exact", recSize);
        s.putArchiveEntry(entry);
        byte[] data = new byte[recSize];
        s.write(data);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testWriteMultipleRecords() throws IOException {
        TarArchiveOutputStream s = createStream();
        int recSize = s.getRecordSize();
        TarArchiveEntry entry = createEntry("multi", recSize * 3);
        s.putArchiveEntry(entry);
        byte[] data = new byte[recSize * 3];
        s.write(data);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testWritePartialRecord() throws IOException {
        TarArchiveOutputStream s = createStream();
        int recSize = s.getRecordSize();
        TarArchiveEntry entry = createEntry("partial", recSize + 10);
        s.putArchiveEntry(entry);
        byte[] data = new byte[recSize + 10];
        s.write(data);
        s.closeArchiveEntry();
        s.close();
    }

    @Test(timeout = 4000)
    public void testWriteSmallChunks() throws IOException {
        TarArchiveOutputStream s = createStream();
        int recSize = s.getRecordSize();
        TarArchiveEntry entry = createEntry("chunks", recSize);
        s.putArchiveEntry(entry);
        // Write in small pieces to trigger assembly buffer
        for (int i = 0; i < recSize; i += 10) {
            int len = Math.min(10, recSize - i);
            s.write(new byte[len]);
        }
        s.closeArchiveEntry();
        s.close();
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testWriteNonAsciiDirectoryNamePosixMode() throws IOException {
        // This test targets the known defect:
        // When writing a non-ASCII directory name with LONGFILE_POSIX,
        // the PaxHeaders entry becomes a directory (name ends with '/'),
        // causing currSize=0 and subsequent write to throw IOException.
        // Expected: no IOException.
        TarArchiveOutputStream s = createStream();
        s.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
        // Non-ASCII directory name (ends with '/')
        TarArchiveEntry entry = new TarArchiveEntry("f\u00f6\u00f6/");
        entry.setSize(0); // directory
        s.putArchiveEntry(entry);
        // No data written for directory, but PaxHeaders are written internally
        s.closeArchiveEntry();
        s.close();
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishWhenAlreadyFinished() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        s.finish(); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishWithUnclosedEntry() throws IOException {
        TarArchiveOutputStream s = createStream();
        TarArchiveEntry entry = createEntry("test.txt", 5);
        s.putArchiveEntry(entry);
        s.finish(); // should throw because entry not closed
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryWhenFinished() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        s.closeArchiveEntry(); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryWhenNoUnclosedEntry() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.closeArchiveEntry(); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testWriteExceedsSize() throws IOException {
        TarArchiveOutputStream s = createStream();
        TarArchiveEntry entry = createEntry("small", 5);
        s.putArchiveEntry(entry);
        s.write(new byte[10]); // exceeds currSize
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testPutArchiveEntryWhenFinished() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        TarArchiveEntry entry = createEntry("test.txt", 0);
        s.putArchiveEntry(entry); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCreateArchiveEntryWhenFinished() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        s.createArchiveEntry(new java.io.File("test.txt"), "test.txt"); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryWithIncompleteWrite() throws IOException {
        TarArchiveOutputStream s = createStream();
        TarArchiveEntry entry = createEntry("incomplete", 10);
        s.putArchiveEntry(entry);
        s.write(new byte[5]); // only 5 bytes written
        s.closeArchiveEntry(); // should throw because currBytes < currSize
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetCountDeprecated() throws IOException {
        TarArchiveOutputStream s = createStream();
        assertEquals(0, s.getCount());
        TarArchiveEntry entry = createEntry("test.txt", 5);
        s.putArchiveEntry(entry);
        s.write("hello".getBytes());
        s.closeArchiveEntry();
        assertTrue(s.getCount() > 0);
        s.close();
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        TarArchiveOutputStream s = createStream();
        for (int i = 0; i < 3; i++) {
            TarArchiveEntry entry = createEntry("file" + i + ".txt", 5);
            s.putArchiveEntry(entry);
            s.write("hello".getBytes());
            s.closeArchiveEntry();
        }
        s.close();
    }

    @Test(timeout = 4000)
    public void testEmptyArchive() throws IOException {
        TarArchiveOutputStream s = createStream();
        s.finish();
        s.close();
    }
}