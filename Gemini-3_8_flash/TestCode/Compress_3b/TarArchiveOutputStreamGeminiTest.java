package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Class Under Test: TarArchiveOutputStream
 * Target Logic & Branches Covered:
 *
 * 1. Constructor Overloads & Record Sizes:
 *    - TarArchiveOutputStream(OutputStream) -> default blksize (10240), default rcdsize (512)
 *    - TarArchiveOutputStream(OutputStream, int) -> custom blksize, default rcdsize (512)
 *    - TarArchiveOutputStream(OutputStream, int, int) -> custom blksize and rcdsize
 *
 * 2. putArchiveEntry(ArchiveEntry):
 *    - Non-TarArchiveEntry instance -> ClassCastException branch
 *    - Long filename (name.length >= TarConstants.NAMELEN = 100):
 *        * Mode LONGFILE_ERROR (0) -> throws RuntimeException
 *        * Mode LONGFILE_TRUNCATE (1) -> bypasses exception, continues with truncated header
 *        * Mode LONGFILE_GNU (2) -> creates longLinkEntry, recurses putArchiveEntry, writes name, closes link
 *    - Short filename (name.length < 100) -> normal header generation
 *    - Directory entry vs File entry -> currSize forced to 0 for directories, otherwise entry.getSize()
 *
 * 3. write(byte[], int, int) & write(int):
 *    - currBytes + numToWrite > currSize -> throws IOException ("exceeds size in header")
 *    - assemLen > 0 (Assembly buffer active):
 *        * assemLen + numToWrite >= recordBuf.length -> fills recordBuf, flushes record, advances pointers
 *        * assemLen + numToWrite < recordBuf.length -> appends to assemBuf, terminates
 *    - numToWrite > 0 (Main write loop):
 *        * numToWrite < recordBuf.length -> copies into assemBuf, break
 *        * numToWrite >= recordBuf.length -> direct buffer.writeRecord, loops
 *
 * 4. closeArchiveEntry():
 *    - assemLen > 0 -> zeroes remainder of assemBuf, writes record, resets assemLen
 *    - currBytes < currSize -> throws IOException ("closed before bytes specified were written")
 *    - currBytes == currSize -> normal completion
 *
 * 5. finish() & close():
 *    - finish() writes two 512-byte zero EOF records
 *    - close() idempotent guard: closed boolean prevents double flush/close
 *
 * 6. Defect-Targeted Zone (Defects4J Ground Truth: ArchiveOutputStreamTest::testFinish):
 *    - Trigger condition: putArchiveEntry called without a matching closeArchiveEntry before finish()/close().
 *    - Required contract: finish() MUST fail if an entry is left unclosed ("After putArchive should follow closeArchive").
 * -------------------------------------------------------------------------------------------------------
 */
public class TarArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleFileEntryLifecycle() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("file1.txt");
        byte[] content = "Hello Tar World".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        byte[] tarBytes = bos.toByteArray();
        assertTrue("Tar archive should not be empty", tarBytes.length > 0);
        assertEquals("Tar archive size must be aligned to block size", 0, tarBytes.length % TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test(timeout = 4000)
    public void testDirectoryEntryLifecycle() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("testdir/");
        dirEntry.setSize(1024); // Size set on directory entry should be ignored and set to 0

        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.close();

        byte[] tarBytes = bos.toByteArray();
        assertTrue("Archive containing directory must be created", tarBytes.length >= TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        byte[] data1 = new byte[300];
        Arrays.fill(data1, (byte) 'A');
        TarArchiveEntry entry1 = new TarArchiveEntry("entry1.bin");
        entry1.setSize(data1.length);
        tos.putArchiveEntry(entry1);
        tos.write(data1);
        tos.closeArchiveEntry();

        byte[] data2 = new byte[700];
        Arrays.fill(data2, (byte) 'B');
        TarArchiveEntry entry2 = new TarArchiveEntry("entry2.bin");
        entry2.setSize(data2.length);
        tos.putArchiveEntry(entry2);
        tos.write(data2);
        tos.closeArchiveEntry();

        tos.close();
        assertTrue("Archive must be successfully finalized", bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testAssembledSmallWritesSpanningRecord() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int totalSize = 700; // > 512, will trigger both assemble buffer paths
        byte[] payload = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            payload[i] = (byte) (i % 128);
        }

        TarArchiveEntry entry = new TarArchiveEntry("assembled.dat");
        entry.setSize(totalSize);
        tos.putArchiveEntry(entry);

        // Write 1: 100 bytes (stored into assemBuf)
        tos.write(payload, 0, 100);

        // Write 2: 100 bytes (accumulated in assemBuf: total 200 < 512)
        tos.write(payload, 100, 100);

        // Write 3: 400 bytes (assemLen 200 + 400 >= 512, fills recordBuf and remaining 88 goes to assemBuf)
        tos.write(payload, 200, 400);

        // Write 4: 100 bytes (assemLen 88 + 100 = 188 < 512)
        tos.write(payload, 600, 100);

        tos.closeArchiveEntry(); // Pads remaining 512 - 188 bytes with zero
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testDirectFullRecordAndMultiRecordWrites() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int totalSize = 1024; // Exactly two 512-byte records
        byte[] payload = new byte[totalSize];
        Arrays.fill(payload, (byte) 0x5A);

        TarArchiveEntry entry = new TarArchiveEntry("records.dat");
        entry.setSize(totalSize);
        tos.putArchiveEntry(entry);

        tos.write(payload, 0, totalSize);
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() >= TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test(timeout = 4000)
    public void testWriteSingleByteMethod() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("singlebyte.txt");
        entry.setSize(2);
        tos.putArchiveEntry(entry);

        tos.write('X');
        tos.write('Y');
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testGnuLongFileNameMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder longName = new StringBuilder("directory_with_a_very_long_name_path_hierarchy/");
        while (longName.length() < 120) {
            longName.append("subfolder_segment/");
        }
        longName.append("testfile.txt");

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        byte[] content = "Data inside long named file".getBytes("UTF-8");
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testTruncateLongFileNameMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            longName.append('a');
        }

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryFromFile() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tempFile = File.createTempFile("gemini_tar_test", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tempFile, "custom/entry/name.tmp");
            assertNotNull("Created ArchiveEntry should not be null", entry);
            assertTrue("Created entry should be an instance of TarArchiveEntry", entry instanceof TarArchiveEntry);
            assertEquals("custom/entry/name.tmp", entry.getName());
        } finally {
            tempFile.delete();
        }
        tos.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroLengthEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() >= TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test(timeout = 4000)
    public void testWriteZeroBytesDoesNothing() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("zero_write.txt");
        entry.setSize(5);
        tos.putArchiveEntry(entry);

        tos.write(new byte[0], 0, 0);
        tos.write(new byte[]{1, 2, 3, 4, 5});
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testBoundaryNameLength99AllowedWithoutLongfileMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        char[] chars = new char[TarConstants.NAMELEN - 1]; // 99 characters
        Arrays.fill(chars, 'k');
        String name99 = new String(chars);

        TarArchiveEntry entry = new TarArchiveEntry(name99);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testBoundaryNameLength100ThrowsInErrorMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        char[] chars = new char[TarConstants.NAMELEN]; // Exactly 100 characters
        Arrays.fill(chars, 'k');
        String name100 = new String(chars);

        TarArchiveEntry entry = new TarArchiveEntry(name100);
        entry.setSize(0);

        try {
            tos.putArchiveEntry(entry);
            fail("Expected RuntimeException when filename length equals TarConstants.NAMELEN (100)");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("is too long"));
        } finally {
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testCustomBlockAndRecordSizes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int customBlockSize = 1024;
        int customRecordSize = 256;
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, customBlockSize, customRecordSize);

        assertEquals(customRecordSize, tos.getRecordSize());

        TarArchiveEntry entry = new TarArchiveEntry("custom_rcd.txt");
        entry.setSize(customRecordSize);
        tos.putArchiveEntry(entry);
        tos.write(new byte[customRecordSize]);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(0, bos.size() % customBlockSize);
    }

    @Test(timeout = 4000)
    public void testCustomBlockSizeWithDefaultRecordSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int customBlockSize = 2048;
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, customBlockSize);

        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
        tos.close();
        assertEquals(customBlockSize, bos.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Defects4J Defect: ArchiveOutputStreamTest::testFinish
    // "junit.framework.AssertionFailedError: After putArchive should follow closeArchive"
    // =========================================================================

    @Test(timeout = 4000)
    public void testFinishWithoutCloseArchiveEntryThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("unclosed_entry.txt");
        entry.setSize(50);
        tos.putArchiveEntry(entry);

        try {
            tos.finish();
            fail("After putArchive should follow closeArchive");
        } catch (IOException expected) {
            // Expected: finish() must detect unclosed entry and throw IOException
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCloseWithoutCloseArchiveEntryThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("unclosed_before_close.txt");
        entry.setSize(20);
        tos.putArchiveEntry(entry);

        try {
            tos.close();
            fail("After putArchive should follow closeArchive");
        } catch (IOException expected) {
            // Expected: close() calls finish(), which must reject unclosed entries
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testPutNonTarArchiveEntryThrowsClassCastException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            public String getName() { return "foreign_entry"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public Date getLastModifiedDate() { return new Date(); }
        };

        try {
            tos.putArchiveEntry(nonTarEntry);
        } finally {
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testWriteExceedingDeclaredSizeThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("overflow.txt");
        entry.setSize(10);
        tos.putArchiveEntry(entry);

        try {
            tos.write(new byte[11]);
            fail("Expected IOException when writing more bytes than specified in entry size");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("exceeds size in header"));
        } finally {
            try {
                tos.closeArchiveEntry();
            } catch (IOException ignored) {}
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testWriteExceedingDeclaredSizeAcrossMultipleWritesThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("multi_overflow.txt");
        entry.setSize(15);
        tos.putArchiveEntry(entry);

        tos.write(new byte[10]);
        try {
            tos.write(new byte[6]); // 10 + 6 = 16 > 15
            fail("Expected IOException on second write exceeding declared entry size");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("exceeds size in header"));
        } finally {
            try {
                tos.closeArchiveEntry();
            } catch (IOException ignored) {}
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testWriteToDirectoryEntryThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("folder/");
        tos.putArchiveEntry(dirEntry);

        try {
            tos.write(new byte[]{1});
            fail("Expected IOException when attempting to write bytes into a directory entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("exceeds size in header"));
        } finally {
            tos.closeArchiveEntry();
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryBeforeSpecifiedBytesWrittenThrowsException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("underflow.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]); // Only 50 bytes written

        try {
            tos.closeArchiveEntry();
            fail("Expected IOException when closing entry before declared size is fulfilled");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("before the '100' bytes specified in the header were written"));
        } finally {
            tos.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        final int[] closeCounter = new int[]{0};
        OutputStream trackingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
            @Override
            public void write(byte[] b, int off, int len) throws IOException {}
            @Override
            public void close() throws IOException {
                closeCounter[0]++;
            }
        };

        TarArchiveOutputStream tos = new TarArchiveOutputStream(trackingStream);
        tos.close();
        assertEquals("Underlying stream should be closed once", 1, closeCounter[0]);

        // Second close should be a no-op
        tos.close();
        assertEquals("Second call to close() should not close underlying stream again", 1, closeCounter[0]);
    }

    @Test(timeout = 4000)
    public void testFlushDelegatesToUnderlyingStream() throws IOException {
        final boolean[] flushCalled = new boolean[]{false};
        OutputStream trackingStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}
            @Override
            public void flush() throws IOException {
                flushCalled[0] = true;
            }
        };

        TarArchiveOutputStream tos = new TarArchiveOutputStream(trackingStream);
        assertFalse(flushCalled[0]);
        tos.flush();
        assertTrue("flush() should delegate to underlying stream", flushCalled[0]);
        tos.close();
    }

    @Test(timeout = 4000)
    public void testGetRecordSizeDefault() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
        tos.close();
    }

    @Test(timeout = 4000)
    public void testFinishWritesTwoZeroRecords() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();

        byte[] output = bos.toByteArray();
        // finish() writes 2 records of 512 bytes = 1024 bytes
        assertEquals(1024, output.length);
        for (byte b : output) {
            assertEquals("EOF records must consist entirely of zero bytes", 0, b);
        }
        tos.close();
    }
}