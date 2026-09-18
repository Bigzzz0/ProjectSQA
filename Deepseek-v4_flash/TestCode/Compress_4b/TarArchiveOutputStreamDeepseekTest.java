package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TarArchiveOutputStreamDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: The write() method has a critical flaw in the record assembly logic.
     * When assemLen > 0 and the assembled data plus new data fits exactly into a record,
     * the code incorrectly copies from wBuf/wOffset instead of using the assembled buffer.
     * This causes data corruption and can lead to "central directory is empty" errors
     * when reading back archives (as seen in the failing tests).
     * 
     * Branches targeted:
     * 1. write() with assemLen == 0 (direct record write path)
     * 2. write() with assemLen > 0 and (assemLen + numToWrite) >= recordBuf.length
     *    - This is the defect zone: the code copies from wBuf instead of assemBuf
     * 3. write() with assemLen > 0 and (assemLen + numToWrite) < recordBuf.length
     *    - Normal assembly path
     * 4. Boundary: numToWrite == 0 (no-op)
     * 5. Boundary: numToWrite exactly fills remaining record space
     * 6. Exception: writing more than currSize
     * 
     * Additional branches:
     * - putArchiveEntry with long filename (LONGFILE_ERROR, LONGFILE_TRUNCATE, LONGFILE_GNU)
     * - closeArchiveEntry with partial records
     * - finish() with unclosed entries
     * - close() idempotency
     * - getRecordSize() default and custom
     * - createArchiveEntry
     * - flush()
     */
    
    private static class TestOutputStream extends OutputStream {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        boolean closed = false;
        
        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }
        
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            baos.write(b, off, len);
        }
        
        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
        
        @Override
        public void flush() throws IOException {
            super.flush();
        }
    }

    private TarArchiveEntry createEntry(String name, long size) {
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(size);
        return entry;
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testBasicWriteAndCloseEntry() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("test.txt", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[]{1,2,3,4,5,6,7,8,9,10}, 0, 10);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
        assertFalse(tos.closed);
    }

    @Test(timeout = 4000)
    public void testWriteWithAssemblyBuffer() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos, 1024, 512);
        
        TarArchiveEntry entry = createEntry("assembly-test", 1000);
        tarOut.putArchiveEntry(entry);
        
        // Write 300 bytes first (fills part of assembly buffer)
        byte[] data1 = new byte[300];
        for (int i = 0; i < data1.length; i++) data1[i] = (byte)i;
        tarOut.write(data1, 0, data1.length);
        
        // Write 300 more bytes - this should trigger the assembly logic
        byte[] data2 = new byte[300];
        for (int i = 0; i < data2.length; i++) data2[i] = (byte)(i+100);
        tarOut.write(data2, 0, data2.length);
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteExactRecordBoundary() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos, 1024, 512);
        
        TarArchiveEntry entry = createEntry("boundary-test", 512);
        tarOut.putArchiveEntry(entry);
        
        byte[] data = new byte[512];
        for (int i = 0; i < data.length; i++) data[i] = (byte)(i % 256);
        tarOut.write(data, 0, data.length);
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====
    
    @Test(timeout = 4000)
    public void testWriteZeroBytes() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("empty-write", 0);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[0], 0, 0);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteExceedingSize() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("too-big", 5);
        tarOut.putArchiveEntry(entry);
        
        try {
            tarOut.write(new byte[10], 0, 10);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("exceeds size"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseEntryBeforeWritingAllData() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("incomplete", 100);
        tarOut.putArchiveEntry(entry);
        tarOut.write(new byte[50], 0, 50);
        
        try {
            tarOut.closeArchiveEntry();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("closed at"));
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testDefectAssemblyBufferCorruption() throws IOException {
        // This test targets the specific defect where assembly buffer data
        // is corrupted when assemLen > 0 and data fits exactly into record
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos, 1024, 512);
        
        TarArchiveEntry entry = createEntry("defect-target", 1024);
        tarOut.putArchiveEntry(entry);
        
        // Write 300 bytes - this goes into assembly buffer
        byte[] firstChunk = new byte[300];
        for (int i = 0; i < firstChunk.length; i++) firstChunk[i] = 0x55;
        tarOut.write(firstChunk, 0, firstChunk.length);
        
        // Write 212 more bytes - this makes assemLen + numToWrite = 512 (record size)
        // This triggers the defect path where wBuf is copied instead of assemBuf
        byte[] secondChunk = new byte[212];
        for (int i = 0; i < secondChunk.length; i++) secondChunk[i] = 0x33;
        tarOut.write(secondChunk, 0, secondChunk.length);
        
        // Write remaining 512 bytes to complete the entry
        byte[] thirdChunk = new byte[512];
        for (int i = 0; i < thirdChunk.length; i++) thirdChunk[i] = 0x11;
        tarOut.write(thirdChunk, 0, thirdChunk.length);
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        // Verify the output contains our data patterns
        byte[] output = tos.baos.toByteArray();
        boolean foundFirstPattern = false;
        boolean foundSecondPattern = false;
        
        for (int i = 0; i < output.length - 1; i++) {
            if ((output[i] & 0xFF) == 0x55 && (output[i+1] & 0xFF) == 0x55) {
                foundFirstPattern = true;
            }
            if ((output[i] & 0xFF) == 0x33 && (output[i+1] & 0xFF) == 0x33) {
                foundSecondPattern = true;
            }
        }
        
        assertTrue("Assembly buffer data should be present in output", foundFirstPattern);
        assertTrue("Second chunk data should be present in output", foundSecondPattern);
    }

    @Test(timeout = 4000)
    public void testDefectWithMultipleAssemblyOperations() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos, 1024, 512);
        
        TarArchiveEntry entry = createEntry("multi-assembly", 1500);
        tarOut.putArchiveEntry(entry);
        
        // Write in small chunks to exercise assembly buffer repeatedly
        for (int i = 0; i < 1500; i += 100) {
            int len = Math.min(100, 1500 - i);
            byte[] chunk = new byte[len];
            for (int j = 0; j < len; j++) {
                chunk[j] = (byte)(i + j);
            }
            tarOut.write(chunk, 0, len);
        }
        
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000)
    public void testFinishWithUnclosedEntry() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("unclosed", 10);
        tarOut.putArchiveEntry(entry);
        
        try {
            tarOut.finish();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("unclosed entries"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseTwice() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        tarOut.close();
        tarOut.close(); // Should not throw
        
        assertTrue(tos.closed);
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryWithLongNameError() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('a');
        }
        
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        
        try {
            tarOut.putArchiveEntry(entry);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("too long"));
        }
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryWithLongNameTruncate() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('b');
        }
        
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryWithLongNameGNU() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append('c');
        }
        
        TarArchiveEntry entry = createEntry(sb.toString(), 0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testGetRecordSizeDefault() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        assertEquals(512, tarOut.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testGetRecordSizeCustom() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos, 1024, 512);
        assertEquals(512, tarOut.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntry() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        java.io.File tempFile = java.io.File.createTempFile("test", ".txt");
        try {
            ArchiveEntry entry = tarOut.createArchiveEntry(tempFile, "test-entry.txt");
            assertNotNull(entry);
            assertTrue(entry instanceof TarArchiveEntry);
            assertEquals("test-entry.txt", entry.getName());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        tarOut.flush(); // Should not throw
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testDirectoryEntry() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = new TarArchiveEntry("testdir/");
        entry.setSize(0);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.finish();
        
        assertTrue(tos.baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteAfterClose() throws IOException {
        TestOutputStream tos = new TestOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(tos);
        
        TarArchiveEntry entry = createEntry("test", 10);
        tarOut.putArchiveEntry(entry);
        tarOut.closeArchiveEntry();
        tarOut.close();
        
        try {
            tarOut.write(new byte[10], 0, 10);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // Expected
        }
    }
}