package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor variants (is, is+encoding, blockSize, recordSize, etc.)
 *   - getNextTarEntry: normal file entry, long link name, long file name,
 *     global pax header, pax header, old GNU sparse, directory, EOF
 *   - read: normal read, end of entry, truncated stream, directory, currEntry==null
 *   - skip: negative/zero/positive, directory, boundary exact/overshoot
 *   - available: directory, small size, large size > Integer.MAX_VALUE
 *   - getCurrentEntry, getRecordSize, isAtEOF, setAtEOF, setCurrentEntry
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty archive (immediate EOF)
 *   - Entry with size 0, size == recordSize, size % recordSize != 0
 *   - Padding at end of entry (skipRecordPadding)
 *   - Null input stream (via constructor)
 *   - Large entry size > Long.MAX? Not realistic but we test available cap
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - PAX header containing blank lines (NegativeArraySizeException)
 *   - PAX header with incomplete data
 *   - Long name entry not followed by entry (Bugzilla 40334)
 *   - isEOFRecord on null, zero-filled, partial records
 *   - tryToConsumeSecondEOFRecord with mark reset
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - read() when currEntry is null -> IllegalStateException
 *   - Truncated TAR archive -> IOException
 *   - Invalid header -> IOException (IllegalArgumentException wrapped)
 *   - canReadEntryData on non-TarArchiveEntry returns false
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - matches(byte[],int) with valid/invalid signatures (POSIX, GNU, ANT)
 *   - mark/reset (no-op)
 */
public class TarArchiveInputStreamDeepseekTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Tests
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndBasicAccessors() {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        assertNotNull(tis);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tis.getRecordSize());
        assertNull(tis.getCurrentEntry());
        assertFalse(((TarArchiveInputStream) tis).isAtEOF());

        // Constructor with encoding
        tis = new TarArchiveInputStream(bis, "UTF-8");
        assertNotNull(tis);
        assertEquals("UTF-8", tis.encoding);
        // Constructor with blockSize
        tis = new TarArchiveInputStream(bis, 1024);
        assertNotNull(tis);
        // Constructor with blockSize and encoding
        tis = new TarArchiveInputStream(bis, 1024, "UTF-8");
        assertNotNull(tis);
        // Full constructor
        tis = new TarArchiveInputStream(bis, 1024, 512, "UTF-8");
        assertNotNull(tis);
        assertEquals(512, tis.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testEmptyArchiveReturnsNull() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        assertNull(tis.getNextTarEntry());
        assertTrue(((TarArchiveInputStream) tis).isAtEOF());
    }

    @Test(timeout = 4000)
    public void testNextEntryWithSimpleFile() throws IOException {
        byte[] archive = createSimpleTarEntry("test.txt", "Hello".getBytes("UTF-8"));
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(5, entry.getSize());

        byte[] content = new byte[5];
        assertEquals(5, tis.read(content, 0, 5));
        assertArrayEquals("Hello".getBytes("UTF-8"), content);
        // End of entry
        assertEquals(-1, tis.read(new byte[1], 0, 1));
    }

    @Test(timeout = 4000)
    public void testReadAfterEntryExhausted() throws IOException {
        byte[] archive = createSimpleTarEntry("empty.txt", new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(0, entry.getSize());
        assertEquals(-1, tis.read(new byte[1], 0, 1));
    }

    @Test(timeout = 4000)
    public void testAvailableForDirectory() throws IOException {
        byte[] archive = createDirectoryEntry("mydir");
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertTrue(entry.isDirectory());
        assertEquals(0, tis.available());
    }

    @Test(timeout = 4000)
    public void testAvailableLargeSize() throws IOException {
        // Create entry with size > Integer.MAX_VALUE (only possible via PAX header)
        // We'll simulate by calling available after setting internal fields via reflection.
        // Since we cannot mock, we test the logic directly using a subclass.
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis) {
            {
                // Set state as if current entry has huge size
                entrySize = ((long) Integer.MAX_VALUE) + 1;
                entryOffset = 0;
                // Avoid NPE on currEntry.isDirectory() by setting a non-directory entry
                currEntry = new TarArchiveEntry("dummy");
            }
        };
        assertEquals(Integer.MAX_VALUE, tis.available());
    }

    @Test(timeout = 4000)
    public void testSkipNegativeZero() throws IOException {
        byte[] archive = createSimpleTarEntry("test.txt", "abc".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        tis.getNextTarEntry();
        assertEquals(0, tis.skip(-1));
        assertEquals(0, tis.skip(0));
    }

    @Test(timeout = 4000)
    public void testSkipPartial() throws IOException {
        byte[] archive = createSimpleTarEntry("test.txt", "abcdef".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        tis.getNextTarEntry();
        // skip 2 bytes
        assertEquals(2, tis.skip(2));
        byte[] buf = new byte[4];
        assertEquals(4, tis.read(buf, 0, 4));
        assertArrayEquals("cdef".getBytes(), buf);
    }

    @Test(timeout = 4000)
    public void testSkipEntryBoundary() throws IOException {
        byte[] archive = createSimpleTarEntry("test.txt", "abc".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        tis.getNextTarEntry();
        // skip more than available
        assertEquals(3, tis.skip(100));
        assertEquals(-1, tis.read(new byte[1], 0, 1));
    }

    @Test(timeout = 4000)
    public void testReadWithNullCurrEntry() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        // No entry has been read
        try {
            tis.read(new byte[1], 0, 1);
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadTruncatedArchive() throws IOException {
        // Create an entry with size 10 but only provide 5 bytes of data
        byte[] archive = createPartialEntry("test.txt", 10, "12345".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        tis.getNextTarEntry();
        byte[] buf = new byte[10];
        try {
            tis.read(buf, 0, 10);
            fail("Should have thrown IOException for truncated archive");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Truncated"));
        }
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() {
        TarArchiveEntry te = new TarArchiveEntry("test.txt");
        Te.setSize(100);
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        assertTrue(tis.canReadEntryData(te));

        // Sparse entries are not readable
        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse") {
            @Override
            public boolean isSparse() {
                return true;
            }
        };
        // Sparse check relies on getSparseHeaders? Actually isSparse method checks if there are sparse headers.
        // We can't easily create a true sparse entry without a real tar file, but we can test the instanceof guard.
        assertFalse(tis.canReadEntryData(new ArchiveEntry() {
            @Override
            public String getName() { return "unknown"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
        }));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEntrySizeZeroAndPadding() throws IOException {
        // Entry with size 0 -> no padding
        byte[] archive = createSimpleTarEntry("zero", new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(0, entry.getSize());
        // After entry, next should be null
        assertNull(tis.getNextTarEntry());
    }

    @Test(timeout = 4000)
    public void testSkipRecordPadding() throws IOException {
        // Entry with size not multiple of recordSize (e.g., size=1, recordSize=512)
        byte[] archive = createSimpleTarEntry("pad", new byte[] { 1 });
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        // Read the single byte
        assertEquals(1, tis.read(new byte[1], 0, 1));
        // Next entry should advance correctly (padding consumed)
        assertNull(tis.getNextTarEntry());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Tests (PAX Blank Line)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSurvivesBlankLinesInPaxHeader() throws Exception {
        // Malformed PAX data: two valid lines separated by a blank line
        String data = "11 path=./\n\n11 linkpath=./\n";
        byte[] paxData = data.getBytes("UTF-8");

        // Create a TarArchiveInputStream instance (dummy input)
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        // Directly invoke parsePaxHeaders – the bug triggers NegativeArraySizeException
        Map<String, String> headers = tis.parsePaxHeaders(new ByteArrayInputStream(paxData));
        // On a fixed version, the blank line is skipped and the two valid lines are parsed
        assertNotNull(headers);
        assertEquals("./", headers.get("path"));
        assertEquals("./", headers.get("linkpath"));
    }

    // Test that a normal PAX header works correctly
    @Test(timeout = 4000)
    public void testPaxHeaderProcessing() throws IOException {
        // Create a tar with a PAX header entry that sets path and size
        String paxData = "16 path=/newpath\n12 size=20\n";
        byte[] archive = createTarWithPaxHeader("x", paxData.getBytes("UTF-8"), "file.txt", "Hello World!".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        // The PAX header should have been applied: the entry should have the new path and size
        assertEquals("/newpath", entry.getName());
        assertEquals(20, entry.getSize()); // Note: actual data is shorter, but we don't read it
    }

    // Test global PAX header
    @Test(timeout = 4000)
    public void testGlobalPaxHeader() throws IOException {
        // Create tar with global PAX header (type 'g') followed by a file entry
        String globalData = "5 uid=0\n";
        byte[] archive = createTarWithPaxHeader("g", globalData.getBytes("UTF-8"), "file.txt", "data".getBytes());
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(0, entry.getUserId());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIllegalStateExceptionOnReadWithoutEntry() throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tis = new TarArchiveInputStream(bis);
        try {
            tis.read(new byte[1], 0, 1);
            fail();
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIOExceptionOnInvalidHeader() throws IOException {
        // Create a header that will cause TarArchiveEntry constructor to throw
        byte[] header = new byte[TarConstants.DEFAULT_RCDSIZE];
        // Fill with arbitrary data that fails parsing
        header[0] = 'X';
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(header));
        try {
            tis.getNextTarEntry();
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMatchesValidity() {
        // POSIX magic + version
        byte[] posixSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX, 0, posixSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX, 0, posixSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(posixSig, 512));

        // GNU magic + space version
        byte[] gnuSigSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU, 0, gnuSigSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE, 0, gnuSigSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuSigSpace, 512));

        // GNU magic + zero version
        byte[] gnuSigZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU, 0, gnuSigZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO, 0, gnuSigZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuSigZero, 512));

        // ANT magic
        byte[] antSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT, 0, antSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT, 0, antSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(antSig, 512));

        // Too short length
        assertFalse(TarArchiveInputStream.matches(posixSig, TarConstants.VERSION_OFFSET - 1));

        // Invalid magic
        byte[] invalid = new byte[512];
        assertFalse(TarArchiveInputStream.matches(invalid, 512));
    }

    // -------------------------------------------------------------------------
    // Partition E: Mark/Reset (no-op)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMarkResetSupported() {
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tis.markSupported());
        tis.mark(100);
        tis.reset(); // should not throw
    }

    // -------------------------------------------------------------------------
    // Helper methods to construct minimal tar archives
    // -------------------------------------------------------------------------

    private byte[] createSimpleTarEntry(String name, byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        if (data.length > 0) {
            tos.write(data);
        }
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    private byte[] createDirectoryEntry(String name) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(0);
        entry.setMode(TarArchiveEntry.DEFAULT_DIR_MODE);
        entry.setLinkFlag(TarArchiveEntry.LF_DIR);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();
        return bos.toByteArray();
    }

    private byte[] createPartialEntry(String name, long declaredSize, byte[] partialData) throws IOException {
        // Build a raw tar header manually to set size larger than data
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setSize(declaredSize);
        // Write header block (512 bytes)
        ByteArrayOutputStream headerOs = new ByteArrayOutputStream();
        byte[] header = new byte[TarConstants.DEFAULT_RCDSIZE];
        entry.writeEntryHeader(header); // This method exists? Actually TarArchiveEntry.writeEntryHeader(byte[]) is used internally.
        // We'll use TarArchiveOutputStream to get header, but it expects full data. Instead, we'll write a header directly.
        // Simpler: use TarArchiveOutputStream to write a header with the declared size, but we'll then truncate data.
        // We'll create a TarArchiveOutputStream that only writes header and partial data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos) {
            @Override
            public void putArchiveEntry(ArchiveEntry archiveEntry) throws IOException {
                // Override to prevent default write of entry data
                super.putArchiveEntry(archiveEntry);
                // Then we manually write only the partial data
            }
        };
        tos.putArchiveEntry(entry);
        tos.write(partialData);
        // Do not close archive entry properly - that would add padding.
        // Instead, we close the stream to get the buffer.
        tos.close();
        byte[] raw = bos.toByteArray();
        // The header plus data, but the data is only partial. That's fine.
        return raw;
    }

    private byte[] createTarWithPaxHeader(String type, byte[] paxData, String fileName, byte[] fileContent) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Write PAX header entry
        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader." + fileName);
        paxEntry.setSize(paxData.length);
        paxEntry.setLinkFlag(type.charAt(0)); // 'x' or 'g'
        // Write header block manually because TarArchiveOutputStream may not allow arbitrary link flag
        byte[] paxHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        paxEntry.writeEntryHeader(paxHeader);
        // Set the type flag in the header
        // linkFlag is stored in filetype field (offset 156)
        paxHeader[156] = type.charAt(0);
        bos.write(paxHeader);
        // Write PAX data
        bos.write(paxData);
        // Pad to record size if needed
        int padLen = TarConstants.DEFAULT_RCDSIZE - (paxData.length % TarConstants.DEFAULT_RCDSIZE);
        if (padLen > 0 && padLen != TarConstants.DEFAULT_RCDSIZE) {
            bos.write(new byte[padLen]);
        }
        // Write actual file entry
        TarArchiveEntry fileEntry = new TarArchiveEntry(fileName);
        fileEntry.setSize(fileContent.length);
        byte[] fileHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        fileEntry.writeEntryHeader(fileHeader);
        bos.write(fileHeader);
        bos.write(fileContent);
        // Pad file data to record size
        padLen = TarConstants.DEFAULT_RCDSIZE - (fileContent.length % TarConstants.DEFAULT_RCDSIZE);
        if (padLen > 0 && padLen != TarConstants.DEFAULT_RCDSIZE) {
            bos.write(new byte[padLen]);
        }
        // Write end-of-archive records (two zero blocks)
        byte[] zeroBlock = new byte[TarConstants.DEFAULT_RCDSIZE];
        bos.write(zeroBlock);
        bos.write(zeroBlock);
        return bos.toByteArray();
    }
}