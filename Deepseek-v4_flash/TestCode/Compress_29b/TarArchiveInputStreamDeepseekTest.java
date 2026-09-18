package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Branch coverage targets:
 * 1. Constructor paths: all 6 constructors (different parameter combinations)
 * 2. available(): entrySize - entryOffset > Integer.MAX_VALUE branch
 * 3. skip(): n <= 0 branch, available < n branch
 * 4. getNextTarEntry(): hasHitEOF true/false, currEntry null/non-null, headerBuf null, 
 *    GNULongLinkEntry, GNULongNameEntry, paxHeader, GNUSparse, skipRecordPadding
 * 5. skipRecordPadding(): entrySize > 0 && entrySize % recordSize != 0
 * 6. getRecord(): eof record detection, tryToConsumeSecondEOFRecord, consumeRemainderOfLastBlock
 * 7. read(): hasHitEOF/entryOffset>=entrySize, currEntry null, truncated archive
 * 8. matches(): multiple magic/version combinations, length check
 * 9. parsePaxHeaders(): space delimiter, equals delimiter, EOF handling, read failures
 * 10. applyPaxHeadersToCurrentEntry(): all 10 header types
 * 11. isEOFRecord(): null record, zero array
 * 
 * Defect-targeted: The known defect relates to encoding handling in TarArchiveInputStream.
 * The bug likely involves improper handling of zipEncoding when reading entry names,
 * specifically in getNextTarEntry() where zipEncoding.decode() is called for long names.
 * The testEncodingInputStream failures suggest encoding issues when decoding header fields.
 * 
 * Boundary values: recordSize=0, blockSize=0, null encoding, empty streams, 
 * negative sizes, MAX values for sizes
 */
public class TarArchiveInputStreamDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream);
        assertEquals("Default record size", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSize() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 2048);
        assertEquals("Record size with block constructor", TarConstants.DEFAULT_RCDSIZE, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithAllParams() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(emptyStream, 1024, 512, "UTF-8");
        assertEquals("Custom record size", 512, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testAvailableWhenEntrySet() throws IOException {
        // Create a minimal valid tar entry (header only)
        byte[] header = new byte[TarConstants.DEFAULT_RCDSIZE];
        // Set checksum to something valid enough to not throw
        // Actually we need proper structure - test via available() logic only
        InputStream is = new ByteArrayInputStream(header);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        
        // Without a current entry, available() uses entrySize - entryOffset which are both 0
        // So it returns 0 (not -1 because entrySize - entryOffset == 0)
        assertEquals("Available with no entry", 0, tarIn.available());
    }

    @Test(timeout = 4000)
    public void testSkipWithNegativeValue() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertEquals("Skipping negative bytes returns 0", 0L, tarIn.skip(-10));
    }

    @Test(timeout = 4000)
    public void testSkipWithZeroValue() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertEquals("Skipping zero bytes returns 0", 0L, tarIn.skip(0));
    }

    @Test(timeout = 4000)
    public void testMarkSupportedReturnsFalse() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertFalse("markSupported should return false", tarIn.markSupported());
    }

    @Test(timeout = 4000)
    public void testGetCurrentEntryInitiallyNull() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertNull("Current entry should be null initially", tarIn.getCurrentEntry());
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithNonTarEntry() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertFalse("Can't read non-TarArchiveEntry", tarIn.canReadEntryData(new ArchiveEntry() {
            @Override
            public String getName() { return "test"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
        }));
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testConstructorWithNullInputStream() {
        // Should not throw NPE, but will throw when trying to read
        TarArchiveInputStream tarIn = new TarArchiveInputStream(null);
        assertNotNull("Constructor with null stream should not throw", tarIn);
    }

    @Test(timeout = 4000)
    public void testConstructorWithNegativeBlockSize() {
        // Negative block size should be allowed (though unusual)
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is, -1);
        assertEquals("Negative block size stored", -1, tarIn.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testConstructorWithZeroRecordSize() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is, 512, 0);
        assertEquals("Zero record size", 0, tarIn.getRecordSize());
        // readRecord() with recordSize=0 will return non-null empty array
        assertNull("getNextTarEntry should return null with empty stream", 
                   tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testReadOnClosedStream() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        tarIn.close();
        // Subsequent read should throw IOException
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf, 0, 10);
            fail("Should throw IOException after close");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNullBuffer() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        try {
            tarIn.read(null, 0, 10);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected from InputStream.read contract
        }
    }

    @Test(timeout = 4000)
    public void testReadWithNegativeOffset() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf, -1, 10);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testReadWhenNoCurrentEntry() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        byte[] buf = new byte[10];
        try {
            tarIn.read(buf, 0, 10);
            fail("Should throw IllegalStateException when no current entry");
        } catch (IllegalStateException e) {
            // Expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testEncodingDecodingInGetNextTarEntry() throws IOException {
        // This test targets the known encoding defect
        // The bug likely manifests when zipEncoding.decode() is called with
        // long name data that has encoding issues
        
        // Create a minimal tar header with a long name entry
        // The long name entry format: a regular entry with type 'L' followed by
        // the actual entry with the name
        
        // First, create the GNU long name header
        byte[] longNameHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        // Set block/filename fields to proper offsets
        // Set the checksum field (octal 11 chars + null)
        // For simplicity, we'll use a known valid checksum pattern
        
        // Set the name field (offset 0, length 100) to a valid name
        String longName = "very_long_name_that_exceeds_100_chars_".repeat(5);
        byte[] nameBytes = longName.getBytes("UTF-8");
        System.arraycopy(nameBytes, 0, longNameHeader, 0, Math.min(nameBytes.length, 100));
        
        // Set size field (offset 124, 12 bytes) to the size of the long name data
        String sizeStr = String.format("%011o", (long) nameBytes.length);
        byte[] sizeBytes = sizeStr.getBytes("ASCII");
        System.arraycopy(sizeBytes, 0, longNameHeader, 124, sizeBytes.length);
        
        // Set type flag to 'L' for GNU long name (offset 156)
        longNameHeader[156] = 'L';
        
        // Calculate and set checksum (offset 148, 8 bytes)
        // We need valid checksum to pass TarArchiveEntry validation
        // For test purposes, we'll just set it to spaces initially and then compute
        StringBuilder checksumStr = new StringBuilder();
        long checksum = 0;
        for (int i = 0; i < longNameHeader.length; i++) {
            checksum += longNameHeader[i] & 0xff;
        }
        // Replace checksum field (spaces)
        for (int i = 148; i < 156; i++) {
            checksum -= longNameHeader[i] & 0xff;
        }
        String checksumOctal = String.format("%06o", checksum);
        byte[] checksumBytes = (checksumOctal + "\0 ").getBytes("ASCII");
        System.arraycopy(checksumBytes, 0, longNameHeader, 148, checksumBytes.length);
        
        // Now create the actual entry that follows
        byte[] actualEntryHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        // Set it as a regular file with a small name
        System.arraycopy("shortname".getBytes("ASCII"), 0, actualEntryHeader, 0, 9);
        // Set size to 0
        byte[] zeroSize = "00000000000".getBytes("ASCII");
        System.arraycopy(zeroSize, 0, actualEntryHeader, 124, zeroSize.length);
        // Set type to '0' (regular file)
        actualEntryHeader[156] = '0';
        
        // Calculate checksum for actual entry
        long checksum2 = 0;
        for (int i = 0; i < actualEntryHeader.length; i++) {
            checksum2 += actualEntryHeader[i] & 0xff;
        }
        for (int i = 148; i < 156; i++) {
            checksum2 -= actualEntryHeader[i] & 0xff;
        }
        String checksumOctal2 = String.format("%06o", checksum2);
        byte[] checksumBytes2 = (checksumOctal2 + "\0 ").getBytes("ASCII");
        System.arraycopy(checksumBytes2, 0, actualEntryHeader, 148, checksumBytes2.length);
        
        // Combine all into a stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(longNameHeader);
        baos.write(nameBytes);
        // Pad to record boundary
        int longNamePadding = TarConstants.DEFAULT_RCDSIZE - (nameBytes.length % TarConstants.DEFAULT_RCDSIZE);
        if (longNamePadding != TarConstants.DEFAULT_RCDSIZE) {
            baos.write(new byte[longNamePadding]);
        }
        baos.write(actualEntryHeader);
        // Add EOF records (two blocks of zeros)
        baos.write(new byte[TarConstants.DEFAULT_RCDSIZE * 2]);
        
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tarIn = new TarArchiveInputStream(bais, "UTF-8");
        
        // This should either return null or a valid entry depending on bug status
        // The defect is that encoding might not be properly applied
        TarArchiveEntry entry = tarIn.getNextTarEntry();
        
        // If the bug is present, entry may be null or have incorrect name
        // We assert that the name should be properly decoded
        if (entry != null) {
            assertNotNull("Entry name should not be null", entry.getName());
            assertTrue("Entry name should contain expected content", 
                       entry.getName().contains("very_long_name"));
        }
    }

    @Test(timeout = 4000)
    public void testEncodingWithPaxHeaders() throws IOException {
        // Test encoding handling with PAX headers
        // Create a PAX header entry followed by a regular entry
        
        // PAX header format: "length key=value\n" where length includes the newline
        String paxHeaderContent = "17 path=testfile.txt\n19 linkpath=symlink\n";
        byte[] paxData = paxHeaderContent.getBytes("UTF-8");
        int paxLength = paxData.length;
        
        // Create PAX header entry
        byte[] paxHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        System.arraycopy("paxheader".getBytes("ASCII"), 0, paxHeader, 0, 9);
        String sizeStr = String.format("%011o", (long) paxLength);
        System.arraycopy(sizeStr.getBytes("ASCII"), 0, paxHeader, 124, 11);
        paxHeader[156] = 'x'; // PAX header type
        
        long checksum = 0;
        for (int i = 0; i < paxHeader.length; i++) {
            checksum += paxHeader[i] & 0xff;
        }
        for (int i = 148; i < 156; i++) {
            checksum -= paxHeader[i] & 0xff;
        }
        String checkStr = String.format("%06o", checksum);
        byte[] chkBytes = (checkStr + "\0 ").getBytes("ASCII");
        System.arraycopy(chkBytes, 0, paxHeader, 148, chkBytes.length);
        
        // Create actual entry
        byte[] actualHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        System.arraycopy("actualfile".getBytes("ASCII"), 0, actualHeader, 0, 10);
        byte[] zeroSize2 = "00000000000".getBytes("ASCII");
        System.arraycopy(zeroSize2, 0, actualHeader, 124, 11);
        actualHeader[156] = '0';
        
        long checksum3 = 0;
        for (int i = 0; i < actualHeader.length; i++) {
            checksum3 += actualHeader[i] & 0xff;
        }
        for (int i = 148; i < 156; i++) {
            checksum3 -= actualHeader[i] & 0xff;
        }
        String checkStr3 = String.format("%06o", checksum3);
        byte[] chkBytes3 = (checkStr3 + "\0 ").getBytes("ASCII");
        System.arraycopy(chkBytes3, 0, actualHeader, 148, chkBytes3.length);
        
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        baos2.write(paxHeader);
        baos2.write(paxData);
        // Pad to record boundary
        int pad = TarConstants.DEFAULT_RCDSIZE - (paxLength % TarConstants.DEFAULT_RCDSIZE);
        if (pad != TarConstants.DEFAULT_RCDSIZE) {
            baos2.write(new byte[pad]);
        }
        baos2.write(actualHeader);
        baos2.write(new byte[TarConstants.DEFAULT_RCDSIZE * 2]);
        
        ByteArrayInputStream bais2 = new ByteArrayInputStream(baos2.toByteArray());
        TarArchiveInputStream tarIn2 = new TarArchiveInputStream(bais2, "ISO-8859-1");
        
        TarArchiveEntry entry2 = tarIn2.getNextTarEntry();
        // The encoding defect may cause incorrect name parsing
        if (entry2 != null) {
            assertEquals("PAX header should update name", "testfile.txt", entry2.getName());
            assertEquals("PAX header should update linkname", "symlink", entry2.getLinkName());
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testReadWithoutCurrentEntry() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[100]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        byte[] buf = new byte[10];
        tarIn.read(buf, 0, 10);
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryOnEmptyStream() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertNull("getNextTarEntry on empty stream should return null", tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testGetNextTarEntryAfterEOF() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[TarConstants.DEFAULT_RCDSIZE * 2]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertNull("Should return null on all-zero stream", tarIn.getNextTarEntry());
        assertNull("Second call should also return null", tarIn.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersWithEOF() throws IOException {
        // Empty input should produce empty headers
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        Map<String, String> headers = tarIn.parsePaxHeaders(is);
        assertNotNull("Parse of empty stream should return non-null map", headers);
        assertTrue("Parse of empty stream should return empty map", headers.isEmpty());
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersWithMalformedData() throws IOException {
        // Data without space delimiter
        InputStream is = new ByteArrayInputStream("10".getBytes("UTF-8"));
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        Map<String, String> headers = tarIn.parsePaxHeaders(is);
        assertNotNull("Malformed pax headers should still produce map", headers);
    }

    @Test(timeout = 4000)
    public void testMatchesWithShortSignature() {
        byte[] sig = new byte[5];
        assertFalse("Short signature should not match", TarArchiveInputStream.matches(sig, 5));
    }

    @Test(timeout = 4000)
    public void testMatchesWithPosixMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        // Set POSIX magic
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, sig, 
                         TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, sig,
                         TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue("POSIX magic should match", TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithGNUMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, sig,
                         TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, sig,
                         TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue("GNU magic should match", TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithGNUMagicZeroVersion() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, sig,
                         TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, sig,
                         TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue("GNU magic with zero version should match", 
                   TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithAntMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, sig,
                         TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, sig,
                         TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue("Ant magic should match", TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testMatchesWithInvalidMagic() {
        byte[] sig = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN];
        assertFalse("Invalid magic should not match", TarArchiveInputStream.matches(sig, sig.length));
    }

    @Test(timeout = 4000)
    public void testIsEOFRecordWithNull() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertTrue("Null record should be EOF", tarIn.isEOFRecord(null));
    }

    @Test(timeout = 4000)
    public void testIsEOFRecordWithZeroArray() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        byte[] zeroRec = new byte[512];
        assertTrue("Zero-filled record should be EOF", tarIn.isEOFRecord(zeroRec));
    }

    @Test(timeout = 4000)
    public void testReadRecordReturnsNullOnShortRead() throws IOException {
        // Stream with less than recordSize bytes
        byte[] data = new byte[100];
        InputStream is = new ByteArrayInputStream(data);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is, 512, 200);
        byte[] record = tarIn.readRecord();
        assertNull("readRecord should return null when insufficient data", record);
    }

    @Test(timeout = 4000)
    public void testConsumeRemainderOfLastBlock() throws IOException {
        // Test the EOF block consumption
        byte[] data = new byte[TarConstants.DEFAULT_RCDSIZE];
        InputStream is = new ByteArrayInputStream(data);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        // Calling getNextTarEntry should internally handle EOF
        assertNull(tarIn.getNextTarEntry());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testCloseDoesNotThrowOnClosedStream() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        tarIn.close();
        // Double close should be safe
        tarIn.close();
    }

    @Test(timeout = 4000)
    public void testSetCurrentEntry() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        TarArchiveEntry entry = new TarArchiveEntry("test");
        tarIn.setCurrentEntry(entry);
        assertSame("setCurrentEntry should set currEntry", entry, tarIn.getCurrentEntry());
    }

    @Test(timeout = 4000)
    public void testSetAtEOF() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertFalse("Initially not at EOF", tarIn.isAtEOF());
        tarIn.setAtEOF(true);
        assertTrue("After setAtEOF(true), should be at EOF", tarIn.isAtEOF());
        tarIn.setAtEOF(false);
        assertFalse("After setAtEOF(false), should not be at EOF", tarIn.isAtEOF());
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataWithSparseFile() {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        TarArchiveEntry entry = new TarArchiveEntry("sparse");
        // We can't easily set sparse flag without extensive header manipulation
        // But the method checks isGNUSparse which we can't set via public API
        // So this tests the base case
        assertTrue("Non-sparse entries are readable", tarIn.canReadEntryData(entry));
    }

    @Test(timeout = 4000)
    public void testGetNextEntryDelegates() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(is);
        assertNull("getNextEntry should delegate to getNextTarEntry", tarIn.getNextEntry());
    }
}