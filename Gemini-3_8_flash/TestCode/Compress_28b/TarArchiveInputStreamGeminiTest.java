package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.compress.archivers.ArchiveEntry;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect:
 *    - Defects4J known failure: TarArchiveInputStreamTest::shouldThrowAnExceptionOnTruncatedEntries
 *      In defective version, reading past EOF on a truncated entry silently returns -1 and sets hasHitEOF=true
 *      rather than throwing an IOException("Truncated TAR archive") when entryOffset < entrySize.
 *    - Targeted by: shouldThrowAnExceptionOnTruncatedEntries() and testTruncatedEntryPrematureEOFThrowsIOException()
 *
 * 2. Decision & Condition Coverage:
 *    - Constructors: 1-arg, 2-arg (with encoding), 2-arg (with blockSize), 3-arg, 4-arg overloads.
 *    - available(): entrySize - entryOffset <= Integer.MAX_VALUE vs > Integer.MAX_VALUE.
 *    - skip(): skip within available, skip exceeding available, skip with 0.
 *    - getNextTarEntry():
 *      - hasHitEOF == true -> return null.
 *      - currEntry != null -> skip remaining data and skipRecordPadding() (padding > 0 vs == 0).
 *      - getRecord() == null -> currEntry = null, return null.
 *      - new TarArchiveEntry() throwing IllegalArgumentException -> rethrown as IOException.
 *      - isGNULongLinkEntry() -> valid data vs malformed (null data).
 *      - isGNULongNameEntry() -> valid data vs malformed (null data).
 *      - isPaxHeader() -> parsePaxHeaders() and applyPaxHeadersToCurrentEntry() covering all keys:
 *        path, linkpath, gid, gname, uid, uname, size, mtime, SCHILY.devminor, SCHILY.devmajor.
 *      - parsePaxHeaders() error condition (got != len - read).
 *      - isGNUSparse() -> readGNUSparse() with isExtended() loop.
 *    - tryToConsumeSecondEOFRecord(): markSupported() == true vs false; second record EOF vs non-EOF (reset & pushedBackBytes).
 *    - isEOFRecord(): null vs all zeros vs non-zero.
 *    - read():
 *      - currEntry == null -> IllegalStateException.
 *      - hasHitEOF == true || entryOffset >= entrySize -> returns -1.
 *      - regular read progressing entryOffset.
 *    - canReadEntryData():
 *      - not a TarArchiveEntry -> false.
 *      - TarArchiveEntry isGNUSparse() == true -> false.
 *      - normal TarArchiveEntry -> true.
 *    - matches():
 *      - length < 265 -> false.
 *      - POSIX magic & version -> true.
 *      - GNU magic & version (space / zero) -> true.
 *      - ANT magic & version -> true.
 *      - non-matching signature -> false.
 */
public class TarArchiveInputStreamGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void shouldThrowAnExceptionOnTruncatedEntries() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("truncated.txt");
        entry.setSize(100);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(new byte[20]); // Entry specifies 100 bytes, but stream has only 20 bytes

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);

        byte[] buf = new byte[50];
        int read;
        // On the defective version, tis.read returns -1 without throwing an IOException,
        // causing this test expecting IOException to fail.
        while ((read = tis.read(buf, 0, buf.length)) != -1) {
            // keep reading until truncation triggers IOException
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testTruncatedEntryPrematureEOFThrowsIOException() throws Exception {
        TarArchiveEntry entry = new TarArchiveEntry("empty_truncated.txt");
        entry.setSize(500);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);

        // Header followed immediately by stream EOF (0 bytes of payload)
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(header));
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);

        byte[] buf = new byte[256];
        tis.read(buf, 0, buf.length);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleNormalEntryReadToCompletion() throws IOException {
        String content = "Hello, Tar World!";
        byte[] contentBytes = content.getBytes("UTF-8");

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        entry.setSize(contentBytes.length);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(contentBytes);
        int pad = 512 - (contentBytes.length % 512);
        if (pad < 512) {
            bos.write(new byte[pad]);
        }
        bos.write(new byte[1024]); // two 512-byte EOF records

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("test.txt", readEntry.getName());
        assertEquals(contentBytes.length, readEntry.getSize());
        assertEquals(contentBytes.length, tis.available());

        byte[] readBuf = new byte[64];
        int numRead = tis.read(readBuf, 0, readBuf.length);
        assertEquals(contentBytes.length, numRead);
        assertEquals(content, new String(readBuf, 0, numRead, "UTF-8"));
        assertEquals(0, tis.available());

        // Further reads return -1 (EOF for entry)
        assertEquals(-1, tis.read(readBuf, 0, readBuf.length));

        // Archive EOF reached
        assertNull(tis.getNextTarEntry());
        assertNull(tis.getNextEntry());
        assertTrue(tis.isAtEOF());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testMultipleEntriesWithPaddingSkipping() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // Entry 1: size 10 (not a multiple of 512, triggers skipRecordPadding)
        TarArchiveEntry entry1 = new TarArchiveEntry("file1.bin");
        entry1.setSize(10);
        byte[] h1 = new byte[512];
        entry1.writeEntryHeader(h1);
        bos.write(h1);
        bos.write(new byte[10]);
        bos.write(new byte[502]); // padding

        // Entry 2: size 512 (exact multiple of 512, padding calculation branch == 0)
        TarArchiveEntry entry2 = new TarArchiveEntry("file2.bin");
        entry2.setSize(512);
        byte[] h2 = new byte[512];
        entry2.writeEntryHeader(h2);
        bos.write(h2);
        bos.write(new byte[512]);

        // Entry 3: size 0 (zero size branch in skipRecordPadding)
        TarArchiveEntry entry3 = new TarArchiveEntry("file3.bin");
        entry3.setSize(0);
        byte[] h3 = new byte[512];
        entry3.writeEntryHeader(h3);
        bos.write(h3);

        bos.write(new byte[1024]); // EOF blocks

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        TarArchiveEntry r1 = tis.getNextTarEntry();
        assertNotNull(r1);
        assertEquals("file1.bin", r1.getName());

        // Skip to next entry without reading payload
        TarArchiveEntry r2 = tis.getNextTarEntry();
        assertNotNull(r2);
        assertEquals("file2.bin", r2.getName());

        TarArchiveEntry r3 = tis.getNextTarEntry();
        assertNotNull(r3);
        assertEquals("file3.bin", r3.getName());

        assertNull(tis.getNextTarEntry());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntryAndExceedingEntry() throws IOException {
        byte[] data = new byte[100];
        for (int i = 0; i < 100; i++) {
            data[i] = (byte) i;
        }

        TarArchiveEntry entry = new TarArchiveEntry("skip.dat");
        entry.setSize(100);
        byte[] header = new byte[512];
        entry.writeEntryHeader(header);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(header);
        bos.write(data);
        bos.write(new byte[412]);
        bos.write(new byte[1024]);

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(tis.getNextTarEntry());

        // Skip 30 bytes
        long skipped = tis.skip(30);
        assertEquals(30, skipped);
        assertEquals(70, tis.available());

        // Skip more than available (request 200, only 70 left)
        long skippedRemaining = tis.skip(200);
        assertEquals(70, skippedRemaining);
        assertEquals(0, tis.available());

        // Further skip returns 0
        assertEquals(0, tis.skip(10));
        assertEquals(-1, tis.read(new byte[10]));
        tis.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeadersAllFields() throws IOException {
        String longPath = "very/long/pax/path/to/a/deeply/nested/file.txt";
        String linkPath = "target/of/symlink";

        StringBuilder sb = new StringBuilder();
        sb.append(createPaxHeaderLine("path", longPath));
        sb.append(createPaxHeaderLine("linkpath", linkPath));
        sb.append(createPaxHeaderLine("gid", "501"));
        sb.append(createPaxHeaderLine("gname", "staff"));
        sb.append(createPaxHeaderLine("uid", "502"));
        sb.append(createPaxHeaderLine("uname", "tester"));
        sb.append(createPaxHeaderLine("size", "15"));
        sb.append(createPaxHeaderLine("mtime", "1600000000.5"));
        sb.append(createPaxHeaderLine("SCHILY.devminor", "7"));
        sb.append(createPaxHeaderLine("SCHILY.devmajor", "8"));
        byte[] paxBytes = sb.toString().getBytes("UTF-8");

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/test.txt", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        paxEntry.setSize(paxBytes.length);
        byte[] paxHeaderBuf = new byte[512];
        paxEntry.writeEntryHeader(paxHeaderBuf);

        TarArchiveEntry actualEntry = new TarArchiveEntry("fallback.txt");
        actualEntry.setSize(0); // will be overridden by Pax size (15)
        byte[] actualHeaderBuf = new byte[512];
        actualEntry.writeEntryHeader(actualHeaderBuf);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(paxHeaderBuf);
        bos.write(paxBytes);
        int paxPad = 512 - (paxBytes.length % 512);
        if (paxPad < 512) {
            bos.write(new byte[paxPad]);
        }
        bos.write(actualHeaderBuf);
        bos.write(new byte[15]); // file content
        bos.write(new byte[497]); // 512 - 15 padding
        bos.write(new byte[1024]); // EOF

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);

        assertEquals(longPath, entry.getName());
        assertEquals(linkPath, entry.getLinkName());
        assertEquals(501, entry.getGroupId());
        assertEquals("staff", entry.getGroupName());
        assertEquals(502, entry.getUserId());
        assertEquals("tester", entry.getUserName());
        assertEquals(15L, entry.getSize());
        assertEquals(1600000000500L, entry.getModTime().getTime());
        assertEquals(7, entry.getDevMinor());
        assertEquals(8, entry.getDevMajor());

        tis.close();
    }

    @Test(timeout = 4000)
    public void testGNULongNameAndLongLink() throws IOException {
        String longName = "super/extremely/long/path/name/that/exceeds/the/one/hundred/character/limit/imposed/by/traditional/tar/format/test.txt";
        byte[] longNameBytes = longName.getBytes("UTF-8");
        byte[] longNameData = new byte[longNameBytes.length + 1]; // null terminated
        System.arraycopy(longNameBytes, 0, longNameData, 0, longNameBytes.length);

        String longLink = "another/very/long/target/path/exceeding/traditional/tar/link/name/limits/for/symlink/target.txt";
        byte[] longLinkBytes = longLink.getBytes("UTF-8");
        byte[] longLinkData = new byte[longLinkBytes.length + 1];
        System.arraycopy(longLinkBytes, 0, longLinkData, 0, longLinkBytes.length);

        TarArchiveEntry longLinkEntry = new TarArchiveEntry("././@LongLink", TarConstants.LF_GNUTYPE_LONGLINK);
        longLinkEntry.setSize(longLinkData.length);
        byte[] longLinkHeader = new byte[512];
        longLinkEntry.writeEntryHeader(longLinkHeader);

        TarArchiveEntry longNameEntry = new TarArchiveEntry("././@LongLink", TarConstants.LF_GNUTYPE_LONGNAME);
        longNameEntry.setSize(longNameData.length);
        byte[] longNameHeader = new byte[512];
        longNameEntry.writeEntryHeader(longNameHeader);

        TarArchiveEntry realEntry = new TarArchiveEntry("short.txt");
        realEntry.setSize(5);
        byte[] realHeader = new byte[512];
        realEntry.writeEntryHeader(realHeader);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // 1. Long link entry & payload
        bos.write(longLinkHeader);
        bos.write(longLinkData);
        int linkPad = 512 - (longLinkData.length % 512);
        if (linkPad < 512) bos.write(new byte[linkPad]);

        // 2. Long name entry & payload
        bos.write(longNameHeader);
        bos.write(longNameData);
        int namePad = 512 - (longNameData.length % 512);
        if (namePad < 512) bos.write(new byte[namePad]);

        // 3. Real file entry & payload
        bos.write(realHeader);
        bos.write(new byte[5]);
        bos.write(new byte[507]);
        bos.write(new byte[1024]);

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
        assertEquals(longLink, entry.getLinkName());
        tis.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAvailableWithLargeEntrySize() throws IOException {
        // Size > Integer.MAX_VALUE via Pax header
        String largeSize = "3000000000"; // 3 GB
        String pax = createPaxHeaderLine("size", largeSize);
        byte[] paxBytes = pax.getBytes("UTF-8");

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/large.bin", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        paxEntry.setSize(paxBytes.length);
        byte[] paxHeaderBuf = new byte[512];
        paxEntry.writeEntryHeader(paxHeaderBuf);

        TarArchiveEntry actualEntry = new TarArchiveEntry("large.bin");
        actualEntry.setSize(0);
        byte[] actualHeaderBuf = new byte[512];
        actualEntry.writeEntryHeader(actualHeaderBuf);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(paxHeaderBuf);
        bos.write(paxBytes);
        int pad = 512 - (paxBytes.length % 512);
        if (pad < 512) bos.write(new byte[pad]);
        bos.write(actualHeaderBuf);

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tis.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(3000000000L, entry.getSize());
        // available() should cap at Integer.MAX_VALUE
        assertEquals(Integer.MAX_VALUE, tis.available());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testMatchesBoundaries() throws Exception {
        // Length shorter than header offset requirements
        assertFalse(TarArchiveInputStream.matches(new byte[200], 200));

        // POSIX magic matches
        byte[] sigPosix = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes("US-ASCII"), 0, sigPosix, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes("US-ASCII"), 0, sigPosix, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sigPosix, 512));

        // GNU space version matches
        byte[] sigGnuSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes("US-ASCII"), 0, sigGnuSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes("US-ASCII"), 0, sigGnuSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sigGnuSpace, 512));

        // GNU zero version matches
        byte[] sigGnuZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes("US-ASCII"), 0, sigGnuZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes("US-ASCII"), 0, sigGnuZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(sigGnuZero, 512));

        // Ant magic matches
        byte[] sigAnt = new byte[512];
        System.array