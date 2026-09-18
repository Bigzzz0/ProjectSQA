package org.apache.commons.compress.archivers.tar;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.compress.archivers.tar.TarArchiveInputStream
 *
 * Partition A: Core Functional Logic & State Transitions
 * - getNextTarEntry(): Multi-entry reading, sequential transitions, EOF record handling.
 * - read(byte[], int, int): Partial reads, multi-record reads, readBuf slicing and draining.
 * - skip(long): Normal entry skip, skip capped at entry boundary, large skip (> BUFFER_SIZE).
 * - available(): Normal computation, capped at Integer.MAX_VALUE when remaining > MAX_VALUE.
 * - canReadEntryData(): Null check, non-TarArchiveEntry check, normal vs. GNUSparse entries.
 * - close() and reset(): TarBuffer delegation, synchronization, lifecycle state.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Zero-byte entry sizes, empty archive streams, exact record size boundaries (512 bytes).
 * - Entry offset == entrySize boundary returning -1 on read().
 * - skip(0), skip(negative), skip beyond available bytes in entry.
 * - matches(): length < 265, POSIX, GNU (space/zero), ANT magic/versions, and corrupted headers.
 *
 * Partition C: Defect-Targeted Branch Zone (COMPRESS-178)
 * - testCOMPRESS178DefectTarget: Header containing embedded NUL bytes in octal fields (e.g.
 *   mode = '00\000765\000'). Tests that TarArchiveInputStream/TarArchiveEntry robustly
 *   handles embedded NULs without throwing java.lang.IllegalArgumentException.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Unexpected EOF during data record reading (TarBuffer returns null during read).
 * - Exception handling during PaxHeader parsing (truncated reader, malformed length).
 * - RuntimeException("failed to skip current tar entry") when skip returns <= 0 during entry skip.
 * - GNU Long Name EOF guard (Bugzilla 40334): GNU long name header followed by EOF.
 * - GNU Sparse EOF guard: hit EOF while reading extended sparse entries.
 *
 * Partition E: Object Lifecycle & Header Extensions
 * - GNU Long Name processing: null-terminator trimming vs. non-terminated long names.
 * - PAX Header processing: path, linkpath, gid, gname, uid, uname, size attributes.
 * - Protected accessors: getCurrentEntry/setCurrentEntry, isAtEOF/setAtEOF.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import org.apache.commons.compress.archivers.ArchiveEntry;

public class TarArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Methods for Tar Record & Stream Generation
    // -------------------------------------------------------------------------

    private byte[] createTarArchive(byte[]... blocks) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] b : blocks) {
            bos.write(b, 0, b.length);
        }
        return bos.toByteArray();
    }

    private byte[] padToRecord(byte[] data) {
        int remainder = data.length % 512;
        if (remainder == 0 && data.length > 0) {
            return data;
        }
        int total = remainder == 0 ? 0 : (data.length + (512 - remainder));
        if (total == 0) {
            return new byte[0];
        }
        byte[] padded = new byte[total];
        System.arraycopy(data, 0, padded, 0, data.length);
        return padded;
    }

    private byte[] createTarHeader(String name, long size, byte typeFlag, byte[] modeBytes) {
        byte[] header = new byte[512];
        try {
            byte[] nameBytes = name.getBytes("UTF-8");
            System.arraycopy(nameBytes, 0, header, 0, Math.min(nameBytes.length, 100));

            if (modeBytes != null) {
                System.arraycopy(modeBytes, 0, header, 100, Math.min(modeBytes.length, 8));
            } else {
                byte[] defMode = "0000644\0".getBytes("UTF-8");
                System.arraycopy(defMode, 0, header, 100, 8);
            }

            System.arraycopy("0000765\0".getBytes("UTF-8"), 0, header, 108, 8); // uid
            System.arraycopy("0000765\0".getBytes("UTF-8"), 0, header, 116, 8); // gid

            String sizeStr = String.format("%011o ", size);
            System.arraycopy(sizeStr.getBytes("UTF-8"), 0, header, 124, 12);

            String mtimeStr = String.format("%011o ", 1000000L);
            System.arraycopy(mtimeStr.getBytes("UTF-8"), 0, header, 136, 12);

            header[156] = typeFlag;

            System.arraycopy("ustar\0".getBytes("UTF-8"), 0, header, 257, 6);
            System.arraycopy("00".getBytes("UTF-8"), 0, header, 263, 2);

            recalculateChecksum(header);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return header;
    }

    private void recalculateChecksum(byte[] header) {
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        long sum = 0;
        for (int i = 0; i < 512; i++) {
            sum += (header[i] & 0xFF);
        }
        try {
            String chkStr = String.format("%06o\0 ", sum);
            System.arraycopy(chkStr.getBytes("UTF-8"), 0, header, 148, 8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String paxLine(String key, String value) {
        int len = key.length() + value.length() + 3; // ' ' + '=' + '\n'
        int totalLen = len + String.valueOf(len).length();
        while (String.valueOf(totalLen).length() + len != totalLen) {
            totalLen = String.valueOf(totalLen).length() + len;
        }
        return totalLen + " " + key + "=" + value + "\n";
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-178)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCOMPRESS178DefectTarget() throws Exception {
        // Octal field containing embedded NUL bytes: '00\000765\000'
        byte[] modeBytes = new byte[] { '0', '0', 0, '0', '7', '6', '5', 0 };
        byte[] header = createTarHeader("compress178.txt", 0, TarConstants.LF_NORMAL, modeBytes);
        byte[] eof = new byte[1024];
        byte[] tarData = createTarArchive(header, eof);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = in.getNextTarEntry();
        assertNotNull("Entry should be successfully parsed despite embedded NUL in mode field", entry);
        assertEquals("compress178.txt", entry.getName());
        in.close();
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMultiEntrySequentialReadAndSkip() throws Exception {
        byte[] data1 = "Hello Tar World!".getBytes("UTF-8");
        byte[] header1 = createTarHeader("file1.txt", data1.length, TarConstants.LF_NORMAL, null);

        byte[] data2 = "Second File Content Here".getBytes("UTF-8");
        byte[] header2 = createTarHeader("file2.txt", data2.length, TarConstants.LF_NORMAL, null);

        byte[] archive = createTarArchive(header1, padToRecord(data1), header2, padToRecord(data2), new byte[1024]);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        // Read first entry partially then advance
        TarArchiveEntry entry1 = tais.getNextTarEntry();
        assertNotNull(entry1);
        assertEquals("file1.txt", entry1.getName());
        assertEquals(data1.length, tais.available());

        byte[] partial = new byte[5];
        int readCount = tais.read(partial, 0, 5);
        assertEquals(5, readCount);
        assertEquals("Hello", new String(partial, "UTF-8"));
        assertEquals(data1.length - 5, tais.available());

        // Advance to second entry, automatically skipping unread data from entry1
        ArchiveEntry entry2 = tais.getNextEntry();
        assertNotNull(entry2);
        assertEquals("file2.txt", entry2.getName());
        assertEquals(data2.length, tais.available());

        byte[] fullData2 = new byte[data2.length];
        assertEquals(data2.length, tais.read(fullData2, 0, data2.length));
        assertEquals(new String(data2, "UTF-8"), new String(fullData2, "UTF-8"));
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(fullData2)); // At entry EOF

        // Archive EOF
        assertNull(tais.getNextTarEntry());
        assertNull(tais.getNextEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testReadBufSlicingAndDraining() throws Exception {
        // Data length spanning beyond 512 bytes to test readBuf logic
        int totalSize = 600;
        byte[] data = new byte[totalSize];
        for (int i = 0; i < totalSize; i++) {
            data[i] = (byte) (i % 127);
        }
        byte[] header = createTarHeader("buffer_test.bin", totalSize, TarConstants.LF_NORMAL, null);
        byte[] archive = createTarArchive(header, padToRecord(data), new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(tais.getNextTarEntry());

        // Step 1: Read 100 bytes (first 512-byte record fetched, 412 saved into readBuf)
        byte[] b1 = new byte[100];
        assertEquals(100, tais.read(b1, 0, 100));

        // Step 2: Read 100 bytes (readBuf sliced: sz < readBuf.length)
        byte[] b2 = new byte[100];
        assertEquals(100, tais.read(b2, 0, 100));

        // Step 3: Read 400 bytes (drains readBuf of 312 bytes and reads second record from buffer)
        byte[] b3 = new byte[400];
        assertEquals(400, tais.read(b3, 0, 400));

        // Step 4: Reading at EOF of entry returns -1
        assertEquals(-1, tais.read(new byte[10]));
        tais.close();
    }

    @Test(timeout = 4000)
    public void testSkipBeyondBufferLimit() throws Exception {
        int totalSize = 10000;
        byte[] data = new byte[totalSize];
        byte[] header = createTarHeader("large_skip.bin", totalSize, TarConstants.LF_NORMAL, null);
        byte[] archive = createTarArchive(header, padToRecord(data), new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(tais.getNextTarEntry());

        // Skip 9000 bytes (exercising BUFFER_SIZE loop in skip)
        long skipped = tais.skip(9000);
        assertEquals(9000, skipped);
        assertEquals(1000, tais.available());

        // Skip 2000 bytes (capped at remaining 1000)
        long skippedRemaining = tais.skip(2000);
        assertEquals(1000, skippedRemaining);
        assertEquals(0, tais.available());

        // Skip at entry EOF
        assertEquals(0, tais.skip(100));
        tais.close();
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Header Matching
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAvailableOverflowsIntegerMax() throws Exception {
        String paxData = paxLine("size", "3000000000"); // 3 Billion > Integer.MAX_VALUE
        byte[] paxBytes = paxData.getBytes("UTF-8");
        byte[] paxHeader = createTarHeader("pax", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC, null);
        byte[] fileHeader = createTarHeader("huge.bin", 0, TarConstants.LF_NORMAL, null);
        byte[] archive = createTarArchive(paxHeader, padToRecord(paxBytes), fileHeader, new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(Integer.MAX_VALUE, tais.available());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testMatchesAllFormats() throws Exception {
        // Less than required header prefix length
        assertFalse(TarArchiveInputStream.matches(new byte[264], 264));

        // POSIX match
        byte[] posix = new byte[512];
        System.arraycopy("ustar\0".getBytes("UTF-8"), 0, posix, 257, 6);
        System.arraycopy("00".getBytes("UTF-8"), 0, posix, 263, 2);
        assertTrue(TarArchiveInputStream.matches(posix, 512));

        // GNU Space match
        byte[] gnuSpace = new byte[512];
        System.arraycopy("ustar ".getBytes("UTF-8"), 0, gnuSpace, 257, 6);
        System.arraycopy(" \0".getBytes("UTF-8"), 0, gnuSpace, 263, 2);
        assertTrue(TarArchiveInputStream.matches(gnuSpace, 512));

        // GNU Zero match
        byte[] gnuZero = new byte[512];
        System.arraycopy("ustar ".getBytes("UTF-8"), 0, gnuZero, 257, 6);
        System.arraycopy("0\0".getBytes("UTF-8"), 0, gnuZero, 263, 2);
        assertTrue(TarArchiveInputStream.matches(gnuZero, 512));

        // ANT match
        byte[] ant = new byte[512];
        System.arraycopy("ustar\0".getBytes("UTF-8"), 0, ant, 257, 6);
        System.arraycopy("\0\0".getBytes("UTF-8"), 0, ant, 263, 2);
        assertTrue(TarArchiveInputStream.matches(ant, 512));

        // Bad magic / version combinations
        byte[] badMagic = new byte[512];
        System.arraycopy("custom".getBytes("UTF-8"), 0, badMagic, 257, 6);
        assertFalse(TarArchiveInputStream.matches(badMagic, 512));

        byte[] badPosixVer = new byte[512];
        System.arraycopy("ustar\0".getBytes("UTF-8"), 0, badPosixVer, 257, 6);
        System.arraycopy("99".getBytes("UTF-8"), 0, badPosixVer, 263, 2);
        assertFalse(TarArchiveInputStream.matches(badPosixVer, 512));

        byte[] badGnuVer = new byte[512];
        System.arraycopy("ustar ".getBytes("UTF-8"), 0, badGnuVer, 257, 6);
        System.arraycopy("99".getBytes("UTF-8"), 0, badGnuVer, 263, 2);
        assertFalse(TarArchiveInputStream.matches(badGnuVer, 512));

        byte[] badAntVer = new byte[512];
        System.arraycopy("ustar\0".getBytes("UTF-8"), 0, badAntVer, 257, 6);
        System.arraycopy("99".getBytes("UTF-8"), 0, badAntVer, 263, 2);
        assertFalse(TarArchiveInputStream.matches(badAntVer, 512));
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadUnexpectedEOFThrowsIOException() throws Exception {
        byte[] header = createTarHeader("truncated.bin", 100, TarConstants.LF_NORMAL, null);
        // Header provided but data stream ends abruptly
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(header));
        assertNotNull(tais.getNextTarEntry());

        try {
            tais.read(new byte[50]);
            fail("Expected IOException on unexpected EOF");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("unexpected EOF"));
        }
        tais.close();
    }

    @Test(timeout = 4000)
    public void testFailedSkipCurrentTarEntryThrowsRuntimeException() throws Exception {
        byte[] header = createTarHeader("skip_fail.bin", 50, TarConstants.LF_NORMAL, null);
        byte[] archive = createTarArchive(header, new byte[1024]);

        // Overriding read to return -1 prematurely during entry skip
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive)) {
            @Override
            public int read(byte[] buf, int offset, int numToRead) {
                return -1;
            }
        };

        assertNotNull(tais.getNextTarEntry());
        try {
            tais.getNextTarEntry();
            fail("Expected RuntimeException when skipping current tar entry fails");
        } catch (RuntimeException expected) {
            assertEquals("failed to skip current tar entry", expected.getMessage());
        }
        tais.close();
    }

    @Test(timeout = 4000)
    public void testGNULongNameMalformedEOFReturnsNull() throws Exception {
        String longName = "very/long/name/that/has/no/following/entry.txt\0";
        byte[] longNameBytes = longName.getBytes("UTF-8");
        byte[] longNameHeader = createTarHeader("././@LongLink", longNameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME, null);
        byte[] archive = createTarArchive(longNameHeader, padToRecord(longNameBytes), new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersIncompleteThrowsIOException() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            tais.parsePaxHeaders(new StringReader("25 path=short"));
            fail("Expected IOException for truncated Pax header");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Failed to read Paxheader"));
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Header Extensions & Object Lifecycle
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testPaxHeadersAllFieldsApplied() throws Exception {
        String paxData = paxLine("path", "pax/custom/path.txt")
                       + paxLine("linkpath", "pax/target/symlink")
                       + paxLine("gid", "501")
                       + paxLine("gname", "staff")
                       + paxLine("uid", "502")
                       + paxLine("uname", "johndoe")
                       + paxLine("size", "30");
        byte[] paxBytes = paxData.getBytes("UTF-8");
        byte[] paxHeader = createTarHeader("pax_header", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC, null);
        byte[] fileHeader = createTarHeader("dummy.txt", 10, TarConstants.LF_NORMAL, null);
        byte[] fileData = new byte[30];
        for (int i = 0; i < 30; i++) {
            fileData[i] = (byte) ('A' + (i % 26));
        }

        byte[] archive = createTarArchive(paxHeader, padToRecord(paxBytes), fileHeader, padToRecord(fileData), new byte[1024]);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("pax/custom/path.txt", entry.getName());
        assertEquals("pax/target/symlink", entry.getLinkName());
        assertEquals(501, entry.getGroupId());
        assertEquals("staff", entry.getGroupName());
        assertEquals(502, entry.getUserId());
        assertEquals("johndoe", entry.getUserName());
        assertEquals(30, entry.getSize());
        assertEquals(30, tais.available());

        byte[] readContent = new byte[30];
        assertEquals(30, tais.read(readContent));
        assertArrayEquals(fileData, readContent);
        tais.close();
    }

    @Test(timeout = 4000)
    public void testGNULongNameWithAndWithoutNullTerminator() throws Exception {
        // Case 1: Trailing NUL stripped
        String nameWithNull = "gnu/long/name/with/trailing/null/character/test/path.txt";
        byte[] bytesWithNull = (nameWithNull + "\0").getBytes("UTF-8");
        byte[] headerLong1 = createTarHeader("././@LongLink", bytesWithNull.length, TarConstants.LF_GNUTYPE_LONGNAME, null);
        byte[] headerFile1 = createTarHeader("placeholder.txt", 0, TarConstants.LF_NORMAL, null);

        // Case 2: No trailing NUL
        String nameWithoutNull = "gnu/long/name/without/null/character/test/path.txt";
        byte[] bytesWithoutNull = nameWithoutNull.getBytes("UTF-8");
        byte[] headerLong2 = createTarHeader("././@LongLink", bytesWithoutNull.length, TarConstants.LF_GNUTYPE_LONGNAME, null);
        byte[] headerFile2 = createTarHeader("placeholder.txt", 0, TarConstants.LF_NORMAL, null);

        byte[] archive = createTarArchive(headerLong1, padToRecord(bytesWithNull), headerFile1,
                                         headerLong2, padToRecord(bytesWithoutNull), headerFile2,
                                         new byte[1024]);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry e1 = tais.getNextTarEntry();
        assertNotNull(e1);
        assertEquals(nameWithNull, e1.getName());

        TarArchiveEntry e2 = tais.getNextTarEntry();
        assertNotNull(e2);
        assertEquals(nameWithoutNull, e2.getName());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testGNUSparseHandling() throws Exception {
        byte[] mainHeader = createTarHeader("sparse.bin", 0, TarConstants.LF_GNUTYPE_SPARSE, null);
        mainHeader[482] = 1; // isExtended = true
        recalculateChecksum(mainHeader);

        byte[] sparseRecord = new byte[512];
        sparseRecord[504] = 0; // isExtended = false in TarArchiveSparseEntry

        byte[] archive = createTarArchive(mainHeader, sparseRecord, new byte[1024]);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertTrue(entry.isGNUSparse());
        assertFalse(tais.canReadEntryData(entry));
        tais.close();
    }

    @Test(timeout = 4000)
    public void testGNUSparseHitEOFDuringSparseRead() throws Exception {
        byte[] mainHeader = createTarHeader("sparse_eof.bin", 0, TarConstants.LF_GNUTYPE_SPARSE, null);
        mainHeader[482] = 1; // isExtended = true
        recalculateChecksum(mainHeader);

        // Archive EOF reached immediately when reading extended sparse record
        byte[] archive = createTarArchive(mainHeader, new byte[1024]);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNull(entry);
        tais.close();
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() throws Exception {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));

        assertFalse(tais.canReadEntryData(null));

        ArchiveEntry anonymousEntry = new ArchiveEntry() {
            public String getName() { return "anon"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        };
        assertFalse(tais.canReadEntryData(anonymousEntry));

        TarArchiveEntry normalEntry = new TarArchiveEntry("test.txt");
        assertTrue(tais.canReadEntryData(normalEntry));
        tais.close();
    }

    @Test(timeout = 4000)
    public void testProtectedStateAccessorsAndConstructors() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais, 1024, 512);
        assertEquals(512, tais.getRecordSize());

        assertNull(tais.getCurrentEntry());
        TarArchiveEntry entry = new TarArchiveEntry("custom.txt");
        tais.setCurrentEntry(entry);
        assertSame(entry, tais.getCurrentEntry());

        assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());

        tais.reset(); // no-op contract

        final boolean[] streamClosed = new boolean[] { false };
        InputStream closeWatcher = new InputStream() {
            @Override
            public int read() { return -1; }
            @Override
            public void close() { streamClosed[0] = true; }
        };
        TarArchiveInputStream taisClose = new TarArchiveInputStream(closeWatcher);
        taisClose.close();
        assertTrue(streamClosed[0]);
    }
}