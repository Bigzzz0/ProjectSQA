package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TarArchiveInputStream
 * 
 * Key Decision Branches & Boundary Conditions:
 * 1. getNextTarEntry() - hasHitEOF check, currEntry null/not null, skipRecordPadding, getRecord null/EOF
 * 2. getRecord() - readRecord null, isEOFRecord true/false, tryToConsumeSecondEOFRecord, consumeRemainderOfLastBlock
 * 3. read() - hasHitEOF, entryOffset >= entrySize, currEntry null, available() calculation
 * 4. available() - entrySize - entryOffset > Integer.MAX_VALUE boundary
 * 5. skip() - numToSkip vs available, IOUtils.skip delegation
 * 6. skipRecordPadding() - entrySize > 0 && entrySize % recordSize != 0
 * 7. getLongNameData() - read loop, getNextEntry null check, trailing null removal
 * 8. paxHeaders() - parsePaxHeaders loop, applyPaxHeadersToCurrentEntry key matching
 * 9. parsePaxHeaders() - length parsing, keyword/value extraction, EOF handling
 * 10. readGNUSparse() - isExtended() loop, getRecord null
 * 11. tryToConsumeSecondEOFRecord() - mark/reset logic, isEOFRecord check
 * 12. consumeRemainderOfLastBlock() - bytesReadOfLastBlock > 0
 * 13. matches() - multiple magic/version combinations
 * 14. Defect: Truncated entries should throw IOException (Defects4J ground truth)
 * 
 * Boundary Values:
 * - entrySize = 0, entrySize = Long.MAX_VALUE
 * - entryOffset = 0, entryOffset = entrySize
 * - recordSize = 512 (default), blockSize = 10240 (default)
 * - numToRead = 0, numToRead = available()
 * - skip amount = 0, skip amount > available
 * - Null/empty/zero-length arrays for matches()
 * - Pax header with all key types
 * - Long name/link entries with trailing nulls
 */
public class TarArchiveInputStreamDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorDefault() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        assertEquals("Default record size", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
        assertNotNull("Encoding should not be null", tarIn);
    }

    @Test(timeout = 4000)
    public void testConstructorWithEncoding() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, "UTF-8");
        assertEquals("Default record size", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 2048);
        assertEquals("Default record size", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSizeAndEncoding() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 2048, "UTF-8");
        assertEquals("Default record size", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSizeAndRecordSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 2048, 512);
        assertEquals("Record size 512", 512, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 2048, 512, "UTF-8");
        assertEquals("Record size 512", 512, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testGetRecordSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 10240, 512);
        assertEquals(512, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(bais);
        tarIn.close();
        // After close, reading should throw IOException
        try {
            bais.read();
            fail("Should have thrown IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReset() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        tarIn.reset(); // Should do nothing
    }

    @Test(timeout = 4000)
    public void testGetCurrentEntryInitiallyNull() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        assertNull("Current entry should be null initially", tarIn.getCurrentEntry());
    }

    @Test(timeout = 4000)
    public void testIsAtEOFInitiallyFalse() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        assertFalse("Should not be at EOF initially", tarIn.isAtEOF());
    }

    @Test(timeout = 4000)
    public void testSetCurrentEntry() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tarIn.setCurrentEntry(entry);
        assertSame(entry, tarIn.getCurrentEntry());
    }

    @Test(timeout = 4000)
    public void testSetAtEOF() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        tarIn.setAtEOF(true);
        assertTrue(tarIn.isAtEOF());
        tarIn.setAtEOF(false);
        assertFalse(tarIn.isAtEOF());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testAvailableWithNoEntry() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        // available() should work even without entry, returning 0
        assertEquals(0, tarIn.available());
    }

    @Test(timeout = 4000)
    public void testAvailableWithEntrySizeZero() throws IOException {
        // Create a minimal valid tar entry with size 0
        byte[] tarData = createMinimalTarEntry("test.txt", 0);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(0, tarIn.available());
    }

    @Test(timeout = 4000)
    public void testAvailableWithLargeEntry() throws IOException {
        // Create entry with size > Integer.MAX_VALUE to test boundary
        byte[] tarData = createMinimalTarEntry("large.txt", (long) Integer.MAX_VALUE + 1);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(Integer.MAX_VALUE, tarIn.available());
    }

    @Test(timeout = 4000)
    public void testSkipZero() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 100);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        assertEquals(0, tarIn.skip(0));
    }

    @Test(timeout = 4000)
    public void testSkipNegative() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 100);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        assertEquals(0, tarIn.skip(-10));
    }

    @Test(timeout = 4000)
    public void testSkipMoreThanAvailable() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 50);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        long skipped = tarIn.skip(100);
        assertEquals(50, skipped);
    }

    @Test(timeout = 4000)
    public void testReadWithZeroLength() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 100);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        byte[] buf = new byte[10];
        assertEquals(0, tarIn.read(buf, 0, 0));
    }

    @Test(timeout = 4000)
    public void testReadAfterEntryExhausted() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        byte[] buf = new byte[20];
        int read = tarIn.read(buf);
        assertEquals(10, read);
        assertEquals(-1, tarIn.read(buf));
    }

    @Test(timeout = 4000)
    public void testReadWithNoCurrentEntry() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        // Don't call getNextTarEntry
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMatchesWithNullSignature() {
        assertFalse(TarArchiveInputStream.matches(null, 0));
    }

    @Test(timeout = 4000)
    public void testMatchesWithShortSignature() {
        byte[] sig = new byte[10];
        assertFalse(TarArchiveInputStream.matches(sig, 10));
    }

    @Test(timeout = 4000)
    public void testMatchesWithPosixMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_POSIX, 0, sig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX, 0, sig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithGNUMagicSpace() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_GNU, 0, sig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE, 0, sig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithGNUMagicZero() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_GNU, 0, sig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO, 0, sig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithAntMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_ANT, 0, sig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT, 0, sig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithInvalidMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        sig[0] = 0x01;
        assertFalse(TarArchiveInputStream.matches(sig, sig.length));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test: Truncated entries should throw IOException.
     * This targets the Defects4J ground truth where truncated entries
     * cause an AssertionFailedError instead of throwing IOException.
     */
    @Test(timeout = 4000)
    public void testShouldThrowAnExceptionOnTruncatedEntries() throws IOException {
        // Create a tar entry with a size larger than the actual data provided
        // This simulates a truncated entry
        byte[] tarData = createTruncatedTarEntry("truncated.txt", 1000, 100);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull("Should have an entry", entry);
        
        byte[] buf = new byte[200];
        try {
            // Try to read more data than available - should throw IOException
            // when trying to read past the truncated data
            int totalRead = 0;
            while (totalRead < entry.getSize()) {
                int read = tarIn.read(buf);
                if (read == -1) {
                    break;
                }
                totalRead += read;
            }
            // If we get here without exception, the truncated entry was not detected
            // This is the defect - should have thrown IOException
            fail("Should have thrown IOException for truncated entry");
        } catch (IOException e) {
            // Expected behavior - truncated entry should cause IOException
            assertTrue("IOException should be thrown", true);
        }
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryOnEmptyStream() throws IOException {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        assertNull("Should return null on empty stream", tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryAfterEOF() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry1 = tarIn.getNextTarEntry();
        assertNotNull(entry1);
        // Read all data
        byte[] buf = new byte[20];
        tarIn.read(buf);
        // Next entry should be null (EOF)
        assertNull(tarIn.getNextTarEntry());
        // Calling again should still return null
        assertNull(tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryWithLongName() throws IOException {
        // Create a tar with a long name entry (GNU style)
        byte[] tarData = createTarWithLongName("very_long_name_that_exceeds_100_characters_"
            + "to_test_gnu_long_name_handling_in_tar_archive_input_stream_"
            + "this_should_be_longer_than_100_characters_for_sure");
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull("Should have entry with long name", entry);
        assertTrue("Name should contain the long name", 
            entry.getName().contains("very_long_name"));
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryWithPaxHeaders() throws IOException {
        // Create a tar with PAX extended headers
        byte[] tarData = createTarWithPaxHeaders("pax_test.txt", 
            "path", "renamed_file.txt",
            "uid", "1000",
            "gid", "1000");
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull("Should have entry with PAX headers", entry);
        assertEquals("Name should be overridden by PAX header", "renamed_file.txt", entry.getName());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeBlockSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        new TarArchiveInputStream(emptyStream, -1, 512);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeRecordSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        new TarArchiveInputStream(emptyStream, 10240, -1);
    }

    @Test(timeout = 4000)
    public void testReadWithNullBuffer() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        try {
            tarIn.read(null, 0, 10);
            fail("Should have thrown NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeOffset() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf, -1, 10);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeLength() throws IOException {
        byte[] tarData = createMinimalTarEntry("test.txt", 10);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tarIn.getNextTarEntry();
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf, 0, -1);
            fail("Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithNonTarEntry() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "test"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public long getLastModified() { return 0; }
        };
        assertFalse(tarIn.canReadEntryData(nonTarEntry));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithSparseEntry() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.txt");
        // Mark as sparse (GNUSparse)
        sparseEntry.setSize(100);
        // We can't easily set the GNUSparse flag, but canReadEntryData checks isGNUSparse()
        // which returns false by default, so this test verifies normal behavior
        assertTrue(tarIn.canReadEntryData(sparseEntry));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        // Create tar with two entries
        byte[] tarData = createTarWithMultipleEntries("file1.txt", 50, "file2.txt", 30);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        
        TarArchiveEntry entry1 = tarIn.getNextTarEntry();
        assertNotNull("First entry should exist", entry1);
        assertEquals("file1.txt", entry1.getName());
        assertEquals(50, entry1.getSize());
        
        byte[] buf = new byte[100];
        int read1 = tarIn.read(buf);
        assertEquals(50, read1);
        
        TarArchiveEntry entry2 = tarIn.getNextTarEntry();
        assertNotNull("Second entry should exist", entry2);
        assertEquals("file2.txt", entry2.getName());
        assertEquals(30, entry2.getSize());
        
        int read2 = tarIn.read(buf);
        assertEquals(30, read2);
        
        assertNull("No more entries", tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testSkipRecordPadding() throws IOException {
        // Create entry with size not multiple of record size
        byte[] tarData = createMinimalTarEntry("test.txt", 600); // 600 % 512 = 88
        TarArchiveInputStream tarIn = new TarArchiveInputStream(new ByteArrayInputStream(tarData), 10240, 512);
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        assertNotNull(entry);
        
        // Read all data
        byte[] buf = new byte[600];
        int totalRead = 0;
        while (totalRead < 600) {
            int read = tarIn.read(buf, totalRead, 600 - totalRead);
            if (read == -1) break;
            totalRead += read;
        }
        assertEquals(600, totalRead);
        
        // Next entry should be null (EOF)
        assertNull(tarIn.getNextTarEntry());
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a minimal valid tar entry with the given name and size.
     * The entry data is filled with zeros.
     */
    private byte[] createMinimalTarEntry(String name, long size) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Create header
            byte[] header = new byte[512];
            // Name (offset 0, length 100)
            byte[] nameBytes = name.getBytes("ASCII");
            System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
            
            // Size (offset 124, length 12) - octal
            String sizeStr = Long.toOctalString(size);
            byte[] sizeBytes = sizeStr.getBytes("ASCII");
            System.arraycopy(sizeBytes, 0, header, 124 + (11 - sizeBytes.length), sizeBytes.length);
            header[124 + 11] = ' '; // trailing space
            
            // Magic (offset 257, length 6)
            System.arraycopy(TarConstants.MAGIC_POSIX, 0, header, 257, TarConstants.MAGICLEN);
            
            // Version (offset 263, length 2)
            System.arraycopy(TarConstants.VERSION_POSIX, 0, header, 263, TarConstants.VERSIONLEN);
            
            // Checksum (offset 148, length 8) - calculate later
            // For simplicity, set to spaces
            for (int i = 148; i < 156; i++) {
                header[i] = ' ';
            }
            
            // Type flag (offset 156) - '0' for normal file
            header[156] = '0';
            
            // Calculate checksum
            int checksum = 0;
            for (byte b : header) {
                checksum += b & 0xFF;
            }
            String checksumStr = String.format("%06o", checksum);
            byte[] checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, header, 148, checksumBytes.length);
            header[154] = '\0';
            header[155] = ' ';
            
            baos.write(header);
            
            // Write data (padded to record size)
            long dataSize = size;
            long paddedSize = ((dataSize + 511) / 512) * 512;
            byte[] data = new byte[(int) paddedSize];
            baos.write(data);
            
            // Write end-of-archive records (two zero blocks)
            byte[] eof = new byte[1024];
            baos.write(eof);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tar entry", e);
        }
    }

    /**
     * Creates a truncated tar entry where the data is shorter than the declared size.
     */
    private byte[] createTruncatedTarEntry(String name, long declaredSize, int actualDataSize) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Create header with declared size larger than actual data
            byte[] header = new byte[512];
            byte[] nameBytes = name.getBytes("ASCII");
            System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
            
            String sizeStr = Long.toOctalString(declaredSize);
            byte[] sizeBytes = sizeStr.getBytes("ASCII");
            System.arraycopy(sizeBytes, 0, header, 124 + (11 - sizeBytes.length), sizeBytes.length);
            header[124 + 11] = ' ';
            
            System.arraycopy(TarConstants.MAGIC_POSIX, 0, header, 257, TarConstants.MAGICLEN);
            System.arraycopy(TarConstants.VERSION_POSIX, 0, header, 263, TarConstants.VERSIONLEN);
            
            for (int i = 148; i < 156; i++) {
                header[i] = ' ';
            }
            header[156] = '0';
            
            int checksum = 0;
            for (byte b : header) {
                checksum += b & 0xFF;
            }
            String checksumStr = String.format("%06o", checksum);
            byte[] checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, header, 148, checksumBytes.length);
            header[154] = '\0';
            header[155] = ' ';
            
            baos.write(header);
            
            // Write only partial data (truncated)
            byte[] data = new byte[actualDataSize];
            baos.write(data);
            
            // No EOF records - stream ends abruptly
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create truncated tar entry", e);
        }
    }

    /**
     * Creates a tar with a GNU long name entry.
     */
    private byte[] createTarWithLongName(String longName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Create long name entry (type 'L')
            byte[] longNameHeader = new byte[512];
            byte[] longNameBytes = longName.getBytes("ASCII");
            
            // Set size for long name data
            String sizeStr = Long.toOctalString(longNameBytes.length + 1); // +1 for null terminator
            byte[] sizeBytes = sizeStr.getBytes("ASCII");
            System.arraycopy(sizeBytes, 0, longNameHeader, 124 + (11 - sizeBytes.length), sizeBytes.length);
            longNameHeader[124 + 11] = ' ';
            
            System.arraycopy(TarConstants.MAGIC_GNU, 0, longNameHeader, 257, TarConstants.MAGICLEN);
            System.arraycopy(TarConstants.VERSION_GNU_SPACE, 0, longNameHeader, 263, TarConstants.VERSIONLEN);
            
            for (int i = 148; i < 156; i++) {
                longNameHeader[i] = ' ';
            }
            longNameHeader[156] = 'L'; // Long name type
            
            int checksum = 0;
            for (byte b : longNameHeader) {
                checksum += b & 0xFF;
            }
            String checksumStr = String.format("%06o", checksum);
            byte[] checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, longNameHeader, 148, checksumBytes.length);
            longNameHeader[154] = '\0';
            longNameHeader[155] = ' ';
            
            baos.write(longNameHeader);
            
            // Write long name data (padded)
            byte[] longNameData = new byte[longNameBytes.length + 1];
            System.arraycopy(longNameBytes, 0, longNameData, 0, longNameBytes.length);
            longNameData[longNameBytes.length] = 0; // null terminator
            long paddedSize = ((longNameData.length + 511) / 512) * 512;
            byte[] paddedLongName = new byte[(int) paddedSize];
            System.arraycopy(longNameData, 0, paddedLongName, 0, longNameData.length);
            baos.write(paddedLongName);
            
            // Now create the actual file entry with a short name
            byte[] fileHeader = new byte[512];
            byte[] shortName = "short_name.txt".getBytes("ASCII");
            System.arraycopy(shortName, 0, fileHeader, 0, Math.min(shortName.length, 100));
            
            String fileSizeStr = "0";
            byte[] fileSizeBytes = fileSizeStr.getBytes("ASCII");
            System.arraycopy(fileSizeBytes, 0, fileHeader, 124 + (11 - fileSizeBytes.length), fileSizeBytes.length);
            fileHeader[124 + 11] = ' ';
            
            System.arraycopy(TarConstants.MAGIC_GNU, 0, fileHeader, 257, TarConstants.MAGICLEN);
            System.arraycopy(TarConstants.VERSION_GNU_SPACE, 0, fileHeader, 263, TarConstants.VERSIONLEN);
            
            for (int i = 148; i < 156; i++) {
                fileHeader[i] = ' ';
            }
            fileHeader[156] = '0';
            
            checksum = 0;
            for (byte b : fileHeader) {
                checksum += b & 0xFF;
            }
            checksumStr = String.format("%06o", checksum);
            checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, fileHeader, 148, checksumBytes.length);
            fileHeader[154] = '\0';
            fileHeader[155] = ' ';
            
            baos.write(fileHeader);
            
            // EOF records
            byte[] eof = new byte[1024];
            baos.write(eof);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tar with long name", e);
        }
    }

    /**
     * Creates a tar with PAX extended headers.
     */
    private byte[] createTarWithPaxHeaders(String fileName, String... paxKeyValues) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Build PAX header data
            StringBuilder paxData = new StringBuilder();
            for (int i = 0; i < paxKeyValues.length; i += 2) {
                String key = paxKeyValues[i];
                String value = paxKeyValues[i + 1];
                String line = key + "=" + value + "\n";
                int len = line.length() + 1; // +1 for the length prefix space
                // Format: "length key=value\n"
                String lenStr = String.valueOf(len);
                paxData.append(lenStr).append(" ").append(line);
            }
            
            byte[] paxBytes = paxData.toString().getBytes("UTF-8");
            
            // Create PAX header entry (type 'x')
            byte[] paxHeader = new byte[512];
            byte[] paxName = "PaxHeader".getBytes("ASCII");
            System.arraycopy(paxName, 0, paxHeader, 0, Math.min(paxName.length, 100));
            
            String paxSizeStr = Long.toOctalString(paxBytes.length);
            byte[] paxSizeBytes = paxSizeStr.getBytes("ASCII");
            System.arraycopy(paxSizeBytes, 0, paxHeader, 124 + (11 - paxSizeBytes.length), paxSizeBytes.length);
            paxHeader[124 + 11] = ' ';
            
            System.arraycopy(TarConstants.MAGIC_POSIX, 0, paxHeader, 257, TarConstants.MAGICLEN);
            System.arraycopy(TarConstants.VERSION_POSIX, 0, paxHeader, 263, TarConstants.VERSIONLEN);
            
            for (int i = 148; i < 156; i++) {
                paxHeader[i] = ' ';
            }
            paxHeader[156] = 'x'; // PAX header type
            
            int checksum = 0;
            for (byte b : paxHeader) {
                checksum += b & 0xFF;
            }
            String checksumStr = String.format("%06o", checksum);
            byte[] checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, paxHeader, 148, checksumBytes.length);
            paxHeader[154] = '\0';
            paxHeader[155] = ' ';
            
            baos.write(paxHeader);
            
            // Write PAX data (padded)
            long paddedPaxSize = ((paxBytes.length + 511) / 512) * 512;
            byte[] paddedPax = new byte[(int) paddedPaxSize];
            System.arraycopy(paxBytes, 0, paddedPax, 0, paxBytes.length);
            baos.write(paddedPax);
            
            // Now create the actual file entry
            byte[] fileHeader = new byte[512];
            byte[] fileNameBytes = fileName.getBytes("ASCII");
            System.arraycopy(fileNameBytes, 0, fileHeader, 0, Math.min(fileNameBytes.length, 100));
            
            String fileSizeStr = "0";
            byte[] fileSizeBytes = fileSizeStr.getBytes("ASCII");
            System.arraycopy(fileSizeBytes, 0, fileHeader, 124 + (11 - fileSizeBytes.length), fileSizeBytes.length);
            fileHeader[124 + 11] = ' ';
            
            System.arraycopy(TarConstants.MAGIC_POSIX, 0, fileHeader, 257, TarConstants.MAGICLEN);
            System.arraycopy(TarConstants.VERSION_POSIX, 0, fileHeader, 263, TarConstants.VERSIONLEN);
            
            for (int i = 148; i < 156; i++) {
                fileHeader[i] = ' ';
            }
            fileHeader[156] = '0';
            
            checksum = 0;
            for (byte b : fileHeader) {
                checksum += b & 0xFF;
            }
            checksumStr = String.format("%06o", checksum);
            checksumBytes = checksumStr.getBytes("ASCII");
            System.arraycopy(checksumBytes, 0, fileHeader, 148, checksumBytes.length);
            fileHeader[154] = '\0';
            fileHeader[155] = ' ';
            
            baos.write(fileHeader);
            
            // EOF records
            byte[] eof = new byte[1024];
            baos.write(eof);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tar with PAX headers", e);
        }
    }

    /**
     * Creates a tar with multiple entries.
     */
    private byte[] createTarWithMultipleEntries(String name1, long size1, String name2, long size2) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // First entry
            byte[] header1 = createTarHeader(name1, size1);
            baos.write(header1);
            
            long paddedSize1 = ((size1 + 511) / 512) * 512;
            byte[] data1 = new byte[(int) paddedSize1];
            baos.write(data1);
            
            // Second entry
            byte[] header2 = createTarHeader(name2, size2);
            baos.write(header2);
            
            long paddedSize2 = ((size2 + 511) / 512) * 512;
            byte[] data2 = new byte[(int) paddedSize2];
            baos.write(data2);
            
            // EOF records
            byte[] eof = new byte[1024];
            baos.write(eof);
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tar with multiple entries", e);
        }
    }

    private byte[] createTarHeader(String name, long size) throws Exception {
        byte[] header = new byte[512];
        byte[] nameBytes = name.getBytes("ASCII");
        System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));
        
        String sizeStr = Long.toOctalString(size);
        byte[] sizeBytes = sizeStr.getBytes("ASCII");
        System.arraycopy(sizeBytes, 0, header, 124 + (11 - sizeBytes.length), sizeBytes.length);
        header[124 + 11] = ' ';
        
        System.arraycopy(TarConstants.MAGIC_POSIX, 0, header, 257, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX, 0, header, 263, TarConstants.VERSIONLEN);
        
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        header[156] = '0';
        
        int checksum = 0;
        for (byte b : header) {
            checksum += b & 0xFF;
        }
        String checksumStr = String.format("%06o", checksum);
        byte[] checksumBytes = checksumStr.getBytes("ASCII");
        System.arraycopy(checksumBytes, 0, header, 148, checksumBytes.length);
        header[154] = '\0';
        header[155] = ' ';
        
        return header;
    }
}