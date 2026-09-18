package org.apache.commons.compress.archivers.sevenz;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.CRC32;

/**
 * White-box test suite for SevenZOutputFile targeting known defects from Defects4J.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor, setContentCompression, createArchiveEntry, putArchiveEntry,
 *     closeArchiveEntry, write(int), write(byte[]), write(byte[],int,int),
 *     finish(), close()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Number of entries: 0, 1, 2, 6, 7, 8, 9 (defect-triggering counts)
 *   - Empty streams vs non-empty streams
 *   - Empty files (directories) vs empty files (non-directory)
 *   - Anti-items
 *   - Dates: null, valid, all present, some present
 *   - Windows attributes
 *   - Content compression methods: COPY, LZMA2, BZIP2, DEFLATE
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known failures: testSixEmptyFiles, testSevenEmptyFiles, testEightEmptyFiles,
 *     testNineEmptyFiles, testSixFilesSomeNotEmpty, testSevenFilesSomeNotEmpty,
 *     testEightFilesSomeNotEmpty, testNineFilesSomeNotEmpty
 *   - These trigger IOException("Unknown property 128/192") or
 *     IOException("Badly terminated header") or ArrayIndexOutOfBoundsException
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - finish() called twice -> IOException
 *   - close() without finish() -> should succeed
 *   - write() before putArchiveEntry -> NullPointerException? (deferred stream)
 *   - closeArchiveEntry() without putArchiveEntry -> IndexOutOfBoundsException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable (no equals/hashCode/clone/serialization)
 */
public class SevenZOutputFileDeepseekTest {

    // Helper: create a temporary file and return a SevenZOutputFile
    private SevenZOutputFile createTempOutputFile() throws IOException {
        File temp = File.createTempFile("7ztest", ".7z");
        temp.deleteOnExit();
        return new SevenZOutputFile(temp);
    }

    // Helper: add empty entries (directories) to the archive
    private void addEmptyEntries(SevenZOutputFile out, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("dir" + i);
            entry.setDirectory(true);
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
        }
    }

    // Helper: add non-empty entries (write a single byte)
    private void addNonEmptyEntries(SevenZOutputFile out, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file" + i);
            entry.setDirectory(false);
            out.putArchiveEntry(entry);
            out.write(42); // write one byte
            out.closeArchiveEntry();
        }
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testConstructorAndClose() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.close(); // should not throw
    }

    @Test(timeout = 4000)
    public void testSetContentCompression() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.setContentCompression(SevenZMethod.COPY);
        out.setContentCompression(SevenZMethod.LZMA2);
        out.setContentCompression(SevenZMethod.BZIP2);
        out.setContentCompression(SevenZMethod.DEFLATE);
        out.close();
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntry() throws IOException {
        File tempFile = File.createTempFile("testEntry", ".txt");
        tempFile.deleteOnExit();
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = out.createArchiveEntry(tempFile, "test.txt");
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
        assertTrue(entry.getHasLastModifiedDate());
        out.close();
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryAndCloseArchiveEntry() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteInt() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        out.putArchiveEntry(entry);
        out.write(0xFF);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteByteArray() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3});
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteByteArrayOffsetLen() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3,4,5}, 1, 3);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteZeroLength() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("test");
        out.putArchiveEntry(entry);
        out.write(new byte[]{}, 0, 0); // should be no-op
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testFinish() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testFinishTwice() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.finish();
        try {
            out.finish();
            fail("Expected IOException on second finish");
        } catch (IOException e) {
            // expected
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testCloseWithoutFinish() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.close(); // should call finish internally
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testZeroEntries() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testOneEmptyEntry() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 1);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testTwoEmptyEntries() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 2);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testOneNonEmptyEntry() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addNonEmptyEntries(out, 1);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testMixedEntries() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 2);
        addNonEmptyEntries(out, 3);
        out.finish();
        out.close();
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    // These tests directly target the known defect scenarios from Defects4J.
    // They should pass on a fixed version and fail on the defective version.

    @Test(timeout = 4000)
    public void testSixEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 6);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testSevenEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 7);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testEightEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 8);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testNineEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 9);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testSixFilesSomeNotEmpty() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 3);
        addNonEmptyEntries(out, 3);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testSevenFilesSomeNotEmpty() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 4);
        addNonEmptyEntries(out, 3);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testEightFilesSomeNotEmpty() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 5);
        addNonEmptyEntries(out, 3);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testNineFilesSomeNotEmpty() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addEmptyEntries(out, 6);
        addNonEmptyEntries(out, 3);
        out.finish();
        out.close();
    }

    // Additional boundary: all non-empty with various counts
    @Test(timeout = 4000)
    public void testSixNonEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addNonEmptyEntries(out, 6);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testSevenNonEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addNonEmptyEntries(out, 7);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testEightNonEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addNonEmptyEntries(out, 8);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testNineNonEmptyFiles() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        addNonEmptyEntries(out, 9);
        out.finish();
        out.close();
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishAfterClose() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.close();
        out.finish(); // should throw because file is closed
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testCloseArchiveEntryWithoutPut() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.closeArchiveEntry(); // no entries added -> IndexOutOfBoundsException
    }

    @Test(timeout = 4000)
    public void testWriteWithoutPutArchiveEntry() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        try {
            out.write(1);
            fail("Expected NullPointerException or IOException");
        } catch (NullPointerException e) {
            // expected because currentOutputStream is null
        } catch (IOException e) {
            // also acceptable
        }
        out.close();
    }

    // ========== Additional coverage: writeFileEmptyStreams, writeFileEmptyFiles, writeFileAntiItems ==========

    @Test(timeout = 4000)
    public void testEmptyFilesWithAntiItems() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        // Add some empty files, some anti-items
        for (int i = 0; i < 5; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("entry" + i);
            if (i % 2 == 0) {
                entry.setDirectory(true);
            } else {
                entry.setAntiItem(true);
            }
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testAllDatesPresent() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        for (int i = 0; i < 3; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file" + i);
            entry.setCreationDate(new java.util.Date());
            entry.setAccessDate(new java.util.Date());
            entry.setLastModifiedDate(new java.util.Date());
            out.putArchiveEntry(entry);
            out.write(i);
            out.closeArchiveEntry();
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testSomeDatesPresent() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        for (int i = 0; i < 4; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file" + i);
            if (i % 2 == 0) {
                entry.setCreationDate(new java.util.Date());
            }
            if (i % 3 == 0) {
                entry.setLastModifiedDate(new java.util.Date());
            }
            out.putArchiveEntry(entry);
            out.write(i);
            out.closeArchiveEntry();
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWindowsAttributes() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        for (int i = 0; i < 3; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file" + i);
            entry.setWindowsAttributes(0x20); // archive attribute
            out.putArchiveEntry(entry);
            out.write(i);
            out.closeArchiveEntry();
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testContentCompressionCopy() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.setContentCompression(SevenZMethod.COPY);
        addNonEmptyEntries(out, 2);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testContentCompressionBzip2() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.setContentCompression(SevenZMethod.BZIP2);
        addNonEmptyEntries(out, 2);
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testContentCompressionDeflate() throws IOException {
        SevenZOutputFile out = createTempOutputFile();
        out.setContentCompression(SevenZMethod.DEFLATE);
        addNonEmptyEntries(out, 2);
        out.finish();
        out.close();
    }
}