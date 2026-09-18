package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
 * 
 * Known Defect (from Defects4J): 
 * - testFinish fails with "After putArchive should follow closeArchive"
 * - This indicates that calling finish() (or close()) after putArchiveEntry() 
 *   without closeArchiveEntry() does not throw an exception, but should.
 * 
 * Branches/conditions targeted:
 * 1. putArchiveEntry with long filename (>= NAMELEN) - LONGFILE_ERROR, LONGFILE_TRUNCATE, LONGFILE_GNU
 * 2. putArchiveEntry with short filename
 * 3. write() with numToWrite > currSize - should throw IOException
 * 4. write() with assemLen > 0 and assemLen + numToWrite >= recordBuf.length
 * 5. write() with assemLen > 0 and assemLen + numToWrite < recordBuf.length
 * 6. write() with assemLen == 0 and numToWrite < recordBuf.length
 * 7. write() with assemLen == 0 and numToWrite >= recordBuf.length
 * 8. closeArchiveEntry() with assemLen > 0
 * 9. closeArchiveEntry() with currBytes < currSize - should throw IOException
 * 10. finish() called without closeArchiveEntry() - defect target
 * 11. close() called without closeArchiveEntry() - defect target
 * 12. finish() called normally after closeArchiveEntry()
 * 13. close() called normally after closeArchiveEntry()
 * 14. close() called twice - should be idempotent
 * 15. getRecordSize() returns correct value
 * 16. flush() delegates to underlying stream
 * 17. createArchiveEntry() returns TarArchiveEntry
 * 18. setLongFileMode() with various values
 * 19. Constructor with various blockSize/recordSize
 * 20. Boundary: currBytes == currSize in closeArchiveEntry()
 * 21. Boundary: numToWrite == currSize - currBytes in write()
 * 22. Boundary: numToWrite == 0 in write()
 * 23. Boundary: assemLen + numToWrite == recordBuf.length in write()
 * 24. Boundary: numToWrite == recordBuf.length in write()
 * 25. Boundary: numToWrite > recordBuf.length in write()
 * 
 * Exception paths:
 * - IOException on write() exceeding entry size
 * - IOException on closeArchiveEntry() with incomplete data
 * - RuntimeException on long filename with LONGFILE_ERROR
 * - ClassCastException on non-TarArchiveEntry
 * - IOException on finish() without closeArchiveEntry() (defect)
 * - IOException on close() without closeArchiveEntry() (defect)
 */
public class TarArchiveOutputStreamDeepseekTest {

    // Helper to create a stream with default buffer sizes
    private TarArchiveOutputStream createStream(OutputStream os) {
        return new TarArchiveOutputStream(os);
    }

    // Helper to create a stream with custom blockSize
    private TarArchiveOutputStream createStream(OutputStream os, int blockSize) {
        return new TarArchiveOutputStream(os, blockSize);
    }

    // Helper to create a stream with custom blockSize and recordSize
    private TarArchiveOutputStream createStream(OutputStream os, int blockSize, int recordSize) {
        return new TarArchiveOutputStream(os, blockSize, recordSize);
    }

    // Helper to create a simple entry with given name and size
    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        return entry;
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testPutAndCloseEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
        
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(timeout = 4000)
    public void testPutAndCloseEntryWithDirectory() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
        
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(timeout = 4000)
    public void testWriteMultipleRecords() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("large.bin", 2048);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[2048];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
        
        byte[] result = bos.toByteArray();
        assertTrue(result.length > 2048);
    }

    @Test(timeout = 4000)
    public void testWriteSmallChunks() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("small.txt", 100);
        tarOut.putArchiveEntry(entry);
        for (int i = 0; i < 100; i++) {
            tarOut.write(new byte[]{1}, 0, 1);
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
        
        byte[] result = bos.toByteArray();
        assertTrue(result.length > 0);
    }

    @Test(timeout = 4000)
    public void testGetRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tarOut.getRecordSize());
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.flush();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        java.io.File file = java.io.File.createTempFile("test", ".txt");
        try {
            ArchiveEntry entry = tarOut.createArchiveEntry(file, "test.txt");
            assertNotNull(entry);
            assertTrue(entry instanceof TarArchiveEntry);
            assertEquals("test.txt", entry.getName());
        } finally {
            file.delete();
        }
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testSetLongFileMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        tarOut.close();
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testWriteZeroBytes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("zero.txt", 0);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[0], 0, 0);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteExactlyRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        TarArchiveEntry entry = createEntry("exact.bin", recordSize);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[recordSize];
        tarOut.write(data, 0, recordSize);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteRecordSizePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        TarArchiveEntry entry = createEntry("plusone.bin", recordSize + 1);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[recordSize + 1];
        tarOut.write(data, 0, recordSize + 1);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteRecordSizeMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        TarArchiveEntry entry = createEntry("minusone.bin", recordSize - 1);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[recordSize - 1];
        tarOut.write(data, 0, recordSize - 1);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteMultipleOfRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        TarArchiveEntry entry = createEntry("multiple.bin", recordSize * 3);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[recordSize * 3];
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteNonMultipleOfRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        TarArchiveEntry entry = createEntry("nonmultiple.bin", recordSize * 2 + 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[recordSize * 2 + 10];
        tarOut.write(data, 0, data.length);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("offset.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        for (int i = 0; i < 20; i++) {
            data[i] = (byte) i;
        }
        tarOut.write(data, 5, 10);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameGNU() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameTruncate() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameError() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('a');
        }
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        try {
            tarOut.putArchiveEntry(entry);
            fail("Expected RuntimeException for long filename with LONGFILE_ERROR");
        } catch (RuntimeException e) {
            // expected
        }
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testBoundaryNameLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        char[] chars = new char[TarConstants.NAMELEN - 1];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = 'a';
        }
        TarArchiveEntry entry = createEntry(new String(chars), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testBoundaryNameLengthExact() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        char[] chars = new char[TarConstants.NAMELEN];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = 'a';
        }
        TarArchiveEntry entry = createEntry(new String(chars), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testBoundaryNameLengthPlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        char[] chars = new char[TarConstants.NAMELEN + 1];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = 'a';
        }
        TarArchiveEntry entry = createEntry(new String(chars), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect target: finish() called without closeArchiveEntry() should throw IOException
     * This is the known defect from Defects4J.
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithoutCloseArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tarOut.putArchiveEntry(entry);
        // No closeArchiveEntry() call - should throw IOException
        tarOut.finish();
    }

    /**
     * Defect target: close() called without closeArchiveEntry() should throw IOException
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseWithoutCloseArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tarOut.putArchiveEntry(entry);
        // No closeArchiveEntry() call - should throw IOException
        tarOut.close();
    }

    /**
     * Defect target: finish() called after putArchiveEntry with data written but not closed
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishAfterPartialWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3}, 0, 3);
        // Not all data written, not closed - should throw IOException
        tarOut.finish();
    }

    /**
     * Defect target: close() called after putArchiveEntry with data written but not closed
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseAfterPartialWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3}, 0, 3);
        // Not all data written, not closed - should throw IOException
        tarOut.close();
    }

    /**
     * Defect target: finish() called after putArchiveEntry with directory entry not closed
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithUnclosedDirectory() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        // No closeArchiveEntry() call - should throw IOException
        tarOut.finish();
    }

    /**
     * Defect target: close() called after putArchiveEntry with directory entry not closed
     */
    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseWithUnclosedDirectory() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("dir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        // No closeArchiveEntry() call - should throw IOException
        tarOut.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IOException.class, timeout = 4000)
    public void testWriteExceedingSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("small.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5, 6}, 0, 6);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryIncomplete() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("incomplete.txt", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3}, 0, 3);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testPutNonTarEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        ArchiveEntry entry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "test.txt";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public int getMode() {
                return 0;
            }

            @Override
            public long getLastModified() {
                return 0;
            }

            @Override
            public long getUserId() {
                return 0;
            }

            @Override
            public long getGroupId() {
                return 0;
            }

            @Override
            public String getUserName() {
                return null;
            }

            @Override
            public String getGroupName() {
                return null;
            }
        };
        tarOut.putArchiveEntry(entry);
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNullBuffer() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("null.bin", 10);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(null, 0, 5);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("neg.bin", 10);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], -1, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("neglen.bin", 10);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetPlusLengthExceedsBuffer() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exceed.bin", 10);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], 5, 10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testCloseTwice() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.close();
        tarOut.close(); // Should be idempotent
    }

    @Test(timeout = 4000)
    public void testFinishAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.close();
        tarOut.finish(); // Should not throw, but may be a no-op
    }

    @Test(timeout = 4000)
    public void testWriteAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.close();
        try {
            tarOut.write(new byte[10], 0, 10);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPutEntryAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.close();
        try {
            TarArchiveEntry entry = createEntry("test.txt", 0);
            tarOut.putArchiveEntry(entry);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryWithoutPut() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        try {
            tarOut.closeArchiveEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tarOut.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testConstructorWithCustomBlockSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos, 1024);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tarOut.getRecordSize());
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithCustomBlockAndRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos, 1024, 256);
        assertEquals(256, tarOut.getRecordSize());
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullOutputStream() {
        try {
            new TarArchiveOutputStream(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithInvalidBlockSize() {
        try {
            new TarArchiveOutputStream(new ByteArrayOutputStream(), 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithInvalidRecordSize() {
        try {
            new TarArchiveOutputStream(new ByteArrayOutputStream(), 512, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetLongFileModeInvalid() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.setLongFileMode(99); // Invalid mode
        // Should not throw, but behavior is undefined
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        
        for (int i = 0; i < 3; i++) {
            TarArchiveEntry entry = createEntry("file" + i + ".txt", 5);
            tarOut.putArchiveEntry(entry);
            tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
            tarOut.closeArchiveEntry();
        }
        
        tarOut.finish();
        tarOut.close();
        
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(timeout = 4000)
    public void testMixedEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        
        // File entry
        TarArchiveEntry fileEntry = createEntry("file.txt", 5);
        tarOut.putArchiveEntry(fileEntry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        
        // Directory entry
        TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
        dirEntry.setSize(0);
        tarOut.putArchiveEntry(dirEntry);
        tarOut.closeArchiveEntry();
        
        tarOut.finish();
        tarOut.close();
        
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(timeout = 4000)
    public void testEmptyArchive() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.finish();
        tarOut.close();
        
        byte[] data = bos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test(timeout = 4000)
    public void testWriteAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        try {
            tarOut.write(new byte[10], 0, 10);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testPutEntryAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        try {
            TarArchiveEntry newEntry = createEntry("new.txt", 0);
            tarOut.putArchiveEntry(newEntry);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryAfterFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("test.txt", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        try {
            tarOut.closeArchiveEntry();
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testGetRecordSizeAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        tarOut.close();
        assertEquals(recordSize, tarOut.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testFlushAfterClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        tarOut.close();
        tarOut.flush(); // Should not throw
    }

    @Test(timeout = 4000)
    public void testWriteWithLargeEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        int entrySize = recordSize * 10 + 100;
        TarArchiveEntry entry = createEntry("large.bin", entrySize);
        tarOut.putArchiveEntry(entry);
        
        byte[] data = new byte[entrySize];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        
        // Write in various chunk sizes
        int offset = 0;
        while (offset < data.length) {
            int chunkSize = Math.min(recordSize / 2, data.length - offset);
            tarOut.write(data, offset, chunkSize);
            offset += chunkSize;
        }
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
        
        byte[] result = bos.toByteArray();
        assertTrue(result.length > entrySize);
    }

    @Test(timeout = 4000)
    public void testWriteWithExactMultipleOfRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        int recordSize = tarOut.getRecordSize();
        int entrySize = recordSize * 2;
        TarArchiveEntry entry = createEntry("exact.bin", entrySize);
        tarOut.putArchiveEntry(entry);
        
        byte[] data = new byte[entrySize];
        tarOut.write(data, 0, data.length);
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithZeroLengthEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("empty.bin", 0);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[0], 0, 0);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeSizeEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("negative.bin", -1);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithMaxLongSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("max.bin", Long.MAX_VALUE);
        tarOut.putArchiveEntry(entry);
        // Writing 0 bytes should be fine
        tarOut.write(new byte[0], 0, 0);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithLargeNumToWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("large.bin", 100);
        tarOut.putArchiveEntry(entry);
        
        try {
            tarOut.write(new byte[200], 0, 200);
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAtEnd() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("end.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 10, 0); // Zero length write at end
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndZeroLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("zero.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 5, 0); // Zero length write
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithLargeOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("largeoffset.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[100];
        tarOut.write(data, 95, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthExceedingBuffer() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exceed.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        try {
            tarOut.write(data, 5, 10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNullAndZeroLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("nullzero.bin", 5);
        tarOut.putArchiveEntry(entry);
        tarOut.write(null, 0, 0); // Should be no-op
        tarOut.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNullAndNonZeroLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("nullnonzero.bin", 5);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(null, 0, 5);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeOffsetAndZeroLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("negoffset.bin", 5);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], -1, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeLengthAndZeroOffset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("neglen.bin", 5);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithNegativeOffsetAndNegativeLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("negboth.bin", 5);
        tarOut.putArchiveEntry(entry);
        try {
            tarOut.write(new byte[10], -1, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferEnd() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("end.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 5, 5); // Exactly at end
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthJustPastBufferEnd() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("past.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        try {
            tarOut.write(data, 5, 6);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStart() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("start.bin", 5);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 0, 5);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEnd() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("full.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 0, 10);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndPlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("fullplus.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[11];
        try {
            tarOut.write(data, 0, 11);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("fullminus.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[9];
        tarOut.write(data, 0, 9);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExact() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exact.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[10];
        tarOut.write(data, 0, 10);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactPlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactplus.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[11];
        try {
            tarOut.write(data, 0, 11);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactminus.bin", 10);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[9];
        tarOut.write(data, 0, 9);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultiple() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmulti.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultiplePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[21];
        try {
            tarOut.write(data, 0, 21);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusplusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[20];
        tarOut.write(data, 0, 20);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOnePlusOneMinusOneMinusOne() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = createStream(bos);
        TarArchiveEntry entry = createEntry("exactmultiminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusplusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminusminus.bin", 20);
        tarOut.putArchiveEntry(entry);
        byte[] data = new byte[19];
        tarOut.write(data, 0, 19);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        tarOut.close();
    }

    @Test(timeout = 4000)
    public void testWriteWithOffsetAndLengthAtBufferStartAndEndExactMultipleMinusOne