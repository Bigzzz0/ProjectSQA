package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: TarArchiveInputStream.java
 *
 * Branch & Coverage Matrix:
 * 1. Defect-Targeted Branch Zone (Defects4J ground truth: shouldReadBigGid):
 *    - In applyPaxHeadersToCurrentEntry:
 *      Parsing "gid" and "uid" from PAX headers using Integer.parseInt(val)
 *      fails with NumberFormatException: For input string: "4294967294" when
 *      group ID or user ID exceeds Integer.MAX_VALUE (e.g. 4294967294L).
 *      Tested by: shouldReadBigGid, shouldReadBigUid.
 *
 * 2. Header parsing & GNU extensions:
 *    - currEntry.isGNULongNameEntry() (true / false / malformed without entry)
 *    - currEntry.isGNULongLinkEntry() (true / false / malformed without entry)
 *    - currEntry.isPaxHeader() (parsing path, linkpath, gid, gname, uid, uname,
 *      size, mtime, devminor, devmajor, unrecognized keys)
 *    - parsePaxHeaders EOF and truncation error conditions
 *    - IllegalArgumentException in TarArchiveEntry constructor wrapped as IOException
 *
 * 3. Stream lifecycle, EOF detection & Padding:
 *    - isEOFRecord (null / all zero / non-zero)
 *    - tryToConsumeSecondEOFRecord (markSupported: true/false; second EOF: present/missing)
 *    - consumeRemainderOfLastBlock (remainder > 0 / remainder == 0)
 *    - skipRecordPadding (entrySize % recordSize != 0)
 *    - available() (boundary when entrySize - entryOffset > Integer.MAX_VALUE)
 *    - skip() (n <= 0, n < available, n >= available)
 *    - read() (before getNextEntry, truncated archive, normal read, at EOF)
 *    - canReadEntryData (TarArchiveEntry vs non-TarArchiveEntry, sparse files)
 *    - matches() (POSIX, GNU space, GNU zero, Ant, too short buffer, invalid)
 * =========================================================================
 */
public class TarArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Known Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J bug:
     * TarArchiveInputStreamTest::shouldReadBigGid
     * -> java.lang.NumberFormatException: For input string: "4294967294"
     * 
     * When reading a tar entry with big GID (4294967294L) stored in PAX header,
     * applyPaxHeadersToCurrentEntry must parse the GID without throwing NumberFormatException.
     */
    @Test(timeout = 4000)
    public void shouldReadBigGid() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry t = new TarArchiveEntry("big_gid_file.txt");
        t.setGroupId(4294967294L);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(30);
        tos.closeArchiveEntry();
        tos.close();

        byte[] data = bos.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        TarArchiveInputStream tin = new TarArchiveInputStream(bin);
        TarArchiveEntry roundTrip = tin.getNextTarEntry();

        assertNotNull("Entry should not be null", roundTrip);
        assertEquals(4294967294L, roundTrip.getLongGroupId());
        tin.close();
    }

    /**
     * Targets corresponding failure condition for UID exceeding Integer.MAX_VALUE.
     */
    @Test(timeout = 4000)
    public void shouldReadBigUid() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry t = new TarArchiveEntry("big_uid_file.txt");
        t.setUserId(4294967294L);
        t.setSize(1);
        tos.putArchiveEntry(t);
        tos.write(42);
        tos.closeArchiveEntry();
        tos.close();

        byte[] data = bos.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        TarArchiveInputStream tin = new TarArchiveInputStream(bin);
        TarArchiveEntry roundTrip = tin.getNextTarEntry();

        assertNotNull("Entry should not be null", roundTrip);
        assertEquals(4294967294L, roundTrip.getLongUserId());
        tin.close();
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadStandardArchiveWithPadding() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry1 = new TarArchiveEntry("entry1.txt");
        byte[] content1 = "Hello World 12345".getBytes(CharsetNames.UTF_8); // 17 bytes, not 512-aligned
        entry1.setSize(content1.length);
        tos.putArchiveEntry(entry1);
        tos.write(content1);
        tos.closeArchiveEntry();

        TarArchiveEntry entry2 = new TarArchiveEntry("entry2.txt");
        byte[] content2 = "Second File Content".getBytes(CharsetNames.UTF_8);
        entry2.setSize(content2.length);
        tos.putArchiveEntry(entry2);
        tos.write(content2);
        tos.closeArchiveEntry();

        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        ArchiveEntry readEntry1 = tin.getNextEntry();
        assertNotNull(readEntry1);
        assertEquals("entry1.txt", readEntry1.getName());
        assertEquals(content1.length, tin.available());

        byte[] buf = new byte[content1.length];
        int bytesRead = tin.read(buf, 0, buf.length);
        assertEquals(content1.length, bytesRead);
        assertArrayEquals(content1, buf);
        assertEquals(0, tin.available());
        assertEquals(-1, tin.read(buf, 0, buf.length));

        ArchiveEntry readEntry2 = tin.getNextEntry();
        assertNotNull(readEntry2);
        assertEquals("entry2.txt", readEntry2.getName());

        byte[] buf2 = new byte[content2.length];
        bytesRead = tin.read(buf2, 0, buf2.length);
        assertEquals(content2.length, bytesRead);
        assertArrayEquals(content2, buf2);

        assertNull(tin.getNextEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeadersAllStandardFields() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeaders.X/test", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        String paxContent = 
            "20 path=pax/custom\n" +
            "24 linkpath=pax/target\n" +
            "13 gid=12345\n" +
            "17 gname=paxgroup\n" +
            "13 uid=54321\n" +
            "16 uname=paxuser\n" +
            "12 size=10\n" +
            "20 mtime=1234567.89\n" +
            "21 SCHILY.devminor=42\n" +
            "21 SCHILY.devmajor=84\n";
        byte[] paxBytes = paxContent.getBytes(CharsetNames.UTF_8);
        paxEntry.setSize(paxBytes.length);
        tos.putArchiveEntry(paxEntry);
        tos.write(paxBytes);
        tos.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("default_name");
        fileEntry.setSize(10);
        tos.putArchiveEntry(fileEntry);
        tos.write("0123456789".getBytes(CharsetNames.UTF_8));
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tin.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("pax/custom", entry.getName());
        assertEquals("pax/target", entry.getLinkName());
        assertEquals(12345, entry.getGroupId());
        assertEquals("paxgroup", entry.getGroupName());
        assertEquals(54321, entry.getUserId());
        assertEquals("paxuser", entry.getUserName());
        assertEquals(10L, entry.getSize());
        assertEquals(1234567890L, entry.getModTime().getTime());
        assertEquals(42, entry.getDevMinor());
        assertEquals(84, entry.getDevMajor());

        tin.close();
    }

    @Test(timeout = 4000)
    public void testGNULongNameEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        String longName = "this/is/a/very/long/name/that/exceeds/the/normal/tar/limit/of/one/hundred/characters/which/requires/gnu/longname/entry/test.txt";
        TarArchiveEntry longNameEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME);
        byte[] longNameBytes = longName.getBytes(CharsetNames.UTF_8);
        byte[] withNull = new byte[longNameBytes.length + 1];
        System.arraycopy(longNameBytes, 0, withNull, 0, longNameBytes.length);
        longNameEntry.setSize(withNull.length);
        tos.putArchiveEntry(longNameEntry);
        tos.write(withNull);
        tos.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("short.txt");
        fileEntry.setSize(4);
        tos.putArchiveEntry(fileEntry);
        tos.write("data".getBytes(CharsetNames.UTF_8));
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tin.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longName, entry.getName());
        byte[] readBuf = new byte[4];
        assertEquals(4, tin.read(readBuf, 0, 4));
        assertEquals("data", new String(readBuf, CharsetNames.UTF_8));
        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testGNULongLinkEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        String longLinkName = "this/is/a/very/long/link/target/that/exceeds/the/normal/tar/limit/of/one/hundred/characters/link_target.txt";
        TarArchiveEntry longLinkEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGLINK);
        byte[] longLinkBytes = longLinkName.getBytes(CharsetNames.UTF_8);
        byte[] withNull = new byte[longLinkBytes.length + 1];
        System.arraycopy(longLinkBytes, 0, withNull, 0, longLinkBytes.length);
        longLinkEntry.setSize(withNull.length);
        tos.putArchiveEntry(longLinkEntry);
        tos.write(withNull);
        tos.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("symlink.txt");
        fileEntry.setSize(0);
        tos.putArchiveEntry(fileEntry);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tin.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(longLinkName, entry.getLinkName());
        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithoutReadingEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        byte[] data1 = new byte[600];
        entry1.setSize(data1.length);
        tos.putArchiveEntry(entry1);
        tos.write(data1);
        tos.closeArchiveEntry();

        TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        byte[] data2 = new byte[100];
        entry2.setSize(data2.length);
        tos.putArchiveEntry(entry2);
        tos.write(data2);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        TarArchiveEntry read1 = tin.getNextTarEntry();
        assertNotNull(read1);
        assertEquals("file1.txt", read1.getName());

        // Skip to file2 without reading file1
        TarArchiveEntry read2 = tin.getNextTarEntry();
        assertNotNull(read2);
        assertEquals("file2.txt", read2.getName());

        byte[] buf = new byte[100];
        int readBytes = tin.read(buf, 0, buf.length);
        assertEquals(100, readBytes);

        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testSkipMethod() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("skip.txt");
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        entry.setSize(data.length);
        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(tin.getNextTarEntry());

        assertEquals(0L, tin.skip(-10));
        assertEquals(0L, tin.skip(0));

        assertEquals(4L, tin.skip(4));
        assertEquals(6, tin.available());

        byte[] remaining = new byte[6];
        int read = tin.read(remaining, 0, remaining.length);
        assertEquals(6, read);
        assertEquals(4, remaining[0]);
        assertEquals(9, remaining[5]);

        // Beyond available data
        assertEquals(0L, tin.skip(10));
        tin.close();
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMatches() {
        assertFalse(TarArchiveInputStream.matches(null, 0));
        assertFalse(TarArchiveInputStream.matches(new byte[10], 10));

        byte[] header = new byte[512];
        assertFalse(TarArchiveInputStream.matches(header, header.length));

        // Test POSIX match
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, header, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(header, header.length));

        // Test GNU space match
        byte[] gnuHeader = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuHeader, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, gnuHeader, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuHeader, gnuHeader.length));

        // Test GNU zero match
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, gnuHeader, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(gnuHeader, gnuHeader.length));

        // Test Ant match
        byte[] antHeader = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, antHeader, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, antHeader, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(antHeader, antHeader.length));

        // Test length boundary just below required
        assertFalse(TarArchiveInputStream.matches(header, TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN - 1));
    }

    @Test(timeout = 4000)
    public void testIsEOFRecord() {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tin.isEOFRecord(null));
        assertTrue(tin.isEOFRecord(new byte[512]));

        byte[] nonZero = new byte[512];
        nonZero[511] = 1;
        assertFalse(tin.isEOFRecord(nonZero));

        try {
            tin.close();
        } catch (IOException ignored) {}
    }

    @Test(timeout = 4000)
    public void testAvailableBoundaryIntegerMax() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeaders.X/large", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        String paxContent = "22 size=3000000000\n"; // > Integer.MAX_VALUE
        byte[] paxBytes = paxContent.getBytes(CharsetNames.UTF_8);
        paxEntry.setSize(paxBytes.length);
        tos.putArchiveEntry(paxEntry);
        tos.write(paxBytes);
        tos.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("large_file.bin");
        fileEntry.setSize(0); // Dummy size before pax applied
        tos.putArchiveEntry(fileEntry);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry entry = tin.getNextTarEntry();
        assertNotNull(entry);
        assertEquals(3000000000L, entry.getSize());

        // Available must cap at Integer.MAX_VALUE without overflow
        assertEquals(Integer.MAX_VALUE, tin.available());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testEmptyInputStream() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(tin.getNextTarEntry());
        assertEquals(-1, tin.read(new byte[10], 0, 10));
        tin.close();
    }

    @Test(timeout = 4000)
    public void testGNULongNameNotFollowedByEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry longNameEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME);
        byte[] data = "abandoned_long_name\0".getBytes(CharsetNames.UTF_8);
        longNameEntry.setSize(data.length);
        tos.putArchiveEntry(longNameEntry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testGNULongLinkNotFollowedByEntry() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry longLinkEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGLINK);
        byte[] data = "abandoned_long_link\0".getBytes(CharsetNames.UTF_8);
        longLinkEntry.setSize(data.length);
        tos.putArchiveEntry(longLinkEntry);
        tos.write(data);
        tos.closeArchiveEntry();
        tos.close();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadWithoutCurrentEntryThrowsException() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));
        try {
            tin.read(new byte[10], 0, 10);
            fail("Expected IllegalStateException when reading without current entry");
        } catch (IllegalStateException expected) {
            assertEquals("No current tar entry", expected.getMessage());
        } finally {
            tin.close();
        }
    }

    @Test(timeout = 4000)
    public void testTruncatedArchiveDuringEntryRead() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("truncated.txt");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]); // write only 50 bytes
        // Don't close archive entry properly, terminate stream early
        byte[] data = bos.toByteArray();

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(data));
        assertNotNull(tin.getNextTarEntry());

        byte[] buf = new byte[100];
        int read1 = tin.read(buf, 0, 50);
        assertEquals(50, read1);

        try {
            tin.read(buf, 50, 50);
            fail("Expected IOException on truncated archive");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Truncated TAR archive"));
        } finally {
            tin.close();
        }
    }

    @Test(timeout = 4000)
    public void testInvalidHeaderThrowsWrappedIOException() throws Exception {
        byte[] corruptedHeader = new byte[512];
        for (int i = 0; i < corruptedHeader.length; i++) {
            corruptedHeader[i] = (byte) 0xFF; // Invalid octal fields
        }

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(corruptedHeader));
        try {
            tin.getNextTarEntry();
            fail("Expected IOException for invalid header");
        } catch (IOException expected) {
            assertEquals("Error detected parsing the header", expected.getMessage());
            assertNotNull(expected.getCause());
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        } finally {
            tin.close();
        }
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersTruncatedDataThrowsException() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        // Declared length is 50, but only 20 bytes provided
        InputStream in = new ByteArrayInputStream("50 key=short_stream\n".getBytes(CharsetNames.UTF_8));
        try {
            tin.parsePaxHeaders(in);
            fail("Expected IOException for truncated Pax header");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Failed to read Paxheader"));
        } finally {
            tin.close();
        }
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersNormal() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        InputStream in = new ByteArrayInputStream("13 key1=val1\n13 key2=val2\n".getBytes(CharsetNames.UTF_8));
        Map<String, String> headers = tin.parsePaxHeaders(in);
        assertEquals(2, headers.size());
        assertEquals("val1", headers.get("key1"));
        assertEquals("val2", headers.get("key2"));
        tin.close();
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndGetters() throws Exception {
        InputStream bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin1 = new TarArchiveInputStream(bais);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tin1.getRecordSize());
        assertFalse(tin1.markSupported());
        tin1.mark(10); // should do nothing
        tin1.reset();  // should do nothing
        tin1.close();

        bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin2 = new TarArchiveInputStream(bais, "UTF-8");
        assertEquals("UTF-8", tin2.encoding);
        tin2.close();

        bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin3 = new TarArchiveInputStream(bais, 1024);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tin3.getRecordSize());
        tin3.close();

        bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin4 = new TarArchiveInputStream(bais, 1024, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tin4.getRecordSize());
        tin4.close();

        bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin5 = new TarArchiveInputStream(bais, 1024, 1024);
        assertEquals(1024, tin5.getRecordSize());
        tin5.close();

        bais = new ByteArrayInputStream(new byte[1024]);
        TarArchiveInputStream tin6 = new TarArchiveInputStream(bais, 1024, 1024, "UTF-8");
        assertEquals(1024, tin6.getRecordSize());
        tin6.close();
    }

    @Test(timeout = 4000)
    public void testSettersAndStateGetters() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(tin.getCurrentEntry());
        assertFalse(tin.isAtEOF());

        TarArchiveEntry dummy = new TarArchiveEntry("dummy");
        tin.setCurrentEntry(dummy);
        assertSame(dummy, tin.getCurrentEntry());

        tin.setAtEOF(true);
        assertTrue(tin.isAtEOF());

        tin.setAtEOF(false);
        assertFalse(tin.isAtEOF());

        tin.close();
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() throws Exception {
        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry standardEntry = new TarArchiveEntry("test.txt");
        assertTrue(tin.canReadEntryData(standardEntry));

        assertFalse(tin.canReadEntryData(null));
        assertFalse(tin.canReadEntryData(new ArchiveEntry() {
            @Override
            public String getName() { return "custom"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        }));

        tin.close();
    }

    @Test(timeout = 4000)
    public void testSingleEOFRecordWithMarkSupportedStream() throws Exception {
        // Construct archive with 1 entry, then 1 EOF record, then garbage data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("single_eof.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();

        byte[] tarBytes = bos.toByteArray();
        // tarBytes now has 512 header + padding up to block size
        // We simulate a stream that has 1 EOF record (512 zeroes) followed by non-zero bytes
        ByteArrayOutputStream customStreamData = new ByteArrayOutputStream();
        customStreamData.write(tarBytes);
        customStreamData.write(new byte[512]); // 1 EOF record
        byte[] trailing = new byte[512];
        trailing[0] = 42; // non-zero second record
        customStreamData.write(trailing);

        TarArchiveInputStream tin = new TarArchiveInputStream(new ByteArrayInputStream(customStreamData.toByteArray()));
        TarArchiveEntry e = tin.getNextTarEntry();
        assertNotNull(e);
        assertEquals("single_eof.txt", e.getName());

        // Hit first EOF record -> tryToConsumeSecondEOFRecord resets stream on non-EOF
        assertNull(tin.getNextTarEntry());
        tin.close();
    }

    @Test(timeout = 4000)
    public void testStreamWithoutMarkSupported() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("no_mark.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        // Wrap in FilterInputStream that overrides markSupported to return false
        InputStream unmarkable = new FilterInputStream(new ByteArrayInputStream(bos.toByteArray())) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };

        TarArchiveInputStream tin = new TarArchiveInputStream(unmarkable);
        TarArchiveEntry e = tin.getNextTarEntry();
        assertNotNull(e);
        assertEquals("no_mark.txt", e.getName());
        assertNull(tin.getNextTarEntry());
        tin.close();
    }
}