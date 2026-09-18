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
 * Decision branches covered:
 * - Constructor variants (3 overloads)
 * - setLongFileMode (LONGFILE_ERROR, LONGFILE_TRUNCATE, LONGFILE_GNU)
 * - finish(): finished flag, haveUnclosedEntry flag, writeEOFRecord twice, flushBlock
 * - close(): calls finish if not finished, buffer.close, out.close, closed flag
 * - putArchiveEntry: finished check, long file name handling (3 modes), entry header write, currSize/currBytes/currName/haveUnclosedEntry
 * - closeArchiveEntry: finished check, haveUnclosedEntry check, assemLen > 0 padding, currBytes < currSize check
 * - write(): currBytes+numToWrite > currSize check, assembly logic (assemLen>0, full record vs partial), while loop for full records, count(numToWrite) at end
 * - createArchiveEntry: finished check
 * - flush: out.flush
 * 
 * Boundary values:
 * - recordSize = 512 (default), blockSize = 10240 (default)
 * - currSize = 0 for directories, positive for files
 * - assemLen = 0, 1..recordSize-1, recordSize
 * - numToWrite = 0, <recordSize, =recordSize, >recordSize
 * - long file name length = TarConstants.NAMELEN (100), exactly 100, 101, etc.
 * - write exactly currSize bytes, write less, write more (exception)
 * 
 * Defect-targeted zone (Defects4J bug):
 * - The count() method is called only with the leftover bytes after the while loop,
 *   not for the full records written. This causes getBytesWritten() to undercount.
 *   Test: write 10240 bytes (10 records of 512) -> expected 10240, but bug gives 76 (or similar).
 */
public class TarArchiveOutputStreamDeepseekTest {

    // Helper to create a simple TarArchiveOutputStream with default block/record sizes
    private TarArchiveOutputStream createStream(ByteArrayOutputStream baos) {
        return new TarArchiveOutputStream(baos);
    }

    // Helper to create a TarArchiveEntry with a given name and size
    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        return entry;
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testPutAndCloseEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1,2,3,4,5,6,7,8,9,10});
        tos.closeArchiveEntry();
        tos.close();
        // Verify that the archive contains at least the header and data
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testDirectoryEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry dir = new TarArchiveEntry("mydir/");
        dir.setSize(0); // directory
        tos.putArchiveEntry(dir);
        tos.closeArchiveEntry();
        tos.close();
        // Directory entries have size 0, no data written
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testFinishAndClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.finish();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testFinishTwiceThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.finish();
        try {
            tos.finish();
            fail("Expected IOException for double finish");
        } catch (IOException e) {
            // expected
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testCloseWithoutFinish() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        // close() should call finish() automatically
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testPutAfterFinishThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.finish();
        try {
            tos.putArchiveEntry(createEntry("test.txt", 10));
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryWithoutOpenThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        try {
            tos.closeArchiveEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testWriteExceedsSizeThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry entry = createEntry("small.txt", 5);
        tos.putArchiveEntry(entry);
        try {
            tos.write(new byte[10]);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tos.closeArchiveEntry();
        tos.close();
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryWithShortWriteThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry entry = createEntry("partial.txt", 100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]);
        try {
            tos.closeArchiveEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tos.close();
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testWriteZeroBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry entry = createEntry("zero.txt", 0);
        tos.putArchiveEntry(entry);
        tos.write(new byte[0]);
        tos.closeArchiveEntry();
        tos.close();
        // Should not throw, archive should be valid
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteExactRecordSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize(); // default 512
        TarArchiveEntry entry = createEntry("exact.txt", recordSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[recordSize];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteMultipleRecords() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize();
        int totalSize = recordSize * 3; // 3 records
        TarArchiveEntry entry = createEntry("multi.txt", totalSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[totalSize];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWritePartialRecordThenFull() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize();
        int totalSize = recordSize + 10; // one full record plus partial
        TarArchiveEntry entry = createEntry("partial_full.txt", totalSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[totalSize];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * This test directly targets the known Defects4J bug:
     * getBytesWritten() returns incorrect value because count() is not called
     * for full records written in the while loop.
     * We write 10240 bytes (10 records of 512) and expect getBytesWritten() to be 10240.
     * The buggy version returns a much smaller number (e.g., 76).
     */
    @Test(timeout = 4000)
    public void testCountAfterWritingMultipleRecords() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize(); // 512
        int totalSize = 10240; // 20 records
        TarArchiveEntry entry = createEntry("large.txt", totalSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[totalSize];
        // Fill with some pattern
        for (int i = 0; i < totalSize; i++) {
            data[i] = (byte) (i % 256);
        }
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        // The bug causes getBytesWritten() to be less than totalSize
        assertEquals("getBytesWritten() should equal total bytes written",
                totalSize, tos.getBytesWritten());
    }

    @Test(timeout = 4000)
    public void testCountAfterSingleRecord() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize();
        TarArchiveEntry entry = createEntry("single.txt", recordSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[recordSize];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        assertEquals(recordSize, tos.getBytesWritten());
    }

    @Test(timeout = 4000)
    public void testCountAfterPartialRecord() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize();
        int partialSize = 100;
        TarArchiveEntry entry = createEntry("partial.txt", partialSize);
        tos.putArchiveEntry(entry);
        byte[] data = new byte[partialSize];
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();
        // The bug: count() is called with leftover (0 if partial < recordSize? Actually after while loop, numToWrite becomes 0 because it breaks? Wait, careful: In write(), if assemLen==0 and numToWrite < recordBuf.length, it copies to assemBuf and breaks, then count(numToWrite) is called with numToWrite still the original? No, numToWrite is not modified in that branch. So count is called with the original numToWrite (100). That should be correct. But the bug is for full records. So this test should pass even on buggy version? Actually the bug is that count is not called for full records. So for partial only, count is called correctly. We'll still include it for coverage.
        assertEquals(partialSize, tos.getBytesWritten());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testLongFileNameErrorMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        // Create a name longer than 100 chars
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) sb.append('a');
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        try {
            tos.putArchiveEntry(entry);
            fail("Expected RuntimeException for long file name in error mode");
        } catch (RuntimeException e) {
            // expected
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) sb.append('a');
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        // Should not throw, name will be truncated
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) sb.append('a');
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryAfterFinishThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.finish();
        try {
            tos.createArchiveEntry(new File("test.txt"), "test.txt");
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.flush();
        tos.close();
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetRecordSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        assertEquals(512, tos.getRecordSize());
        tos.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos, 2048);
        assertEquals(512, tos.getRecordSize()); // default record size
        tos.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockAndRecordSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(baos, 2048, 1024);
        assertEquals(1024, tos.getRecordSize());
        tos.close();
    }

    @Test(timeout = 4000)
    public void testSetLongFileMode() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        // No getter, but we can verify behavior via putArchiveEntry
        tos.close();
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        TarArchiveEntry entry1 = createEntry("file1.txt", 10);
        tos.putArchiveEntry(entry1);
        tos.write(new byte[10]);
        tos.closeArchiveEntry();
        TarArchiveEntry entry2 = createEntry("file2.txt", 20);
        tos.putArchiveEntry(entry2);
        tos.write(new byte[20]);
        tos.closeArchiveEntry();
        tos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteWithAssemblyBufferNonEmpty() throws IOException {
        // This test triggers the assembly buffer path where assemLen > 0
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = createStream(baos);
        int recordSize = tos.getRecordSize();
        // Write a small chunk to fill assembly buffer partially
        TarArchiveEntry entry = createEntry("assembly.txt", recordSize + 10);
        tos.putArchiveEntry(entry);
        byte[] firstChunk = new byte[10];
        tos.write(firstChunk); // assemLen becomes 10
        // Now write a chunk that will fill the rest of the record and cause a flush
        byte[] secondChunk = new byte[recordSize]; // 512 bytes
        tos.write(secondChunk); // This should flush the assembly buffer and write full record
        // Then write remaining 10 bytes (since total size is 522, we have written 10+512=522, but entry size is 522? Actually entry size is recordSize+10 = 522, we wrote 10+512=522, so done)
        tos.closeArchiveEntry();
        tos.close();
        assertEquals(522, tos.getBytesWritten());
    }
}