package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Decision / Branch Condition                                   | Targeted In Test Method
 * ----------------------------------------------------------------------------------------------------
 * matches(signature, length)                                    | testMatchesValidPosixGnuAntAndInvalid
 *   - length < VERSION_OFFSET + VERSIONLEN                      | testMatchesTooShort
 *   - POSIX magic & version match                               | testMatchesValidPosixGnuAntAndInvalid
 *   - GNU magic & version (space / zero) match                  | testMatchesValidPosixGnuAntAndInvalid
 *   - Ant magic & version match                                 | testMatchesValidPosixGnuAntAndInvalid
 *   - Non-matching buffer                                       | testMatchesValidPosixGnuAntAndInvalid
 * ----------------------------------------------------------------------------------------------------
 * Constructors & Encoding handling                              | testConstructorsAndEncodingSupport
 *   - default / custom blksize & rcdsize                        | testConstructorsAndEncodingSupport
 *   - custom zipEncoding decoding GNU/Pax names (CP437, UTF-8)  | testEncodingGnuLongNameAndPax
 * ----------------------------------------------------------------------------------------------------
 * getNextTarEntry() & Lifecycle transitions                     |
 *   - hasHitEOF true -> returns null                            | testHitEOFStateTransitions
 *   - currEntry != null -> skip current entry & padding         | testSkipCurrentEntryDataAndPadding
 *   - headerBuf == null (stream truncated at header)            | testTruncatedArchiveAtHeaderReturnsNull
 *   - header parsing error -> IOException                       | testCorruptedHeaderThrowsIOException
 *   - GNU Long Link Entry -> decode & setLinkName               | testGnuLongLinkEntry
 *   - GNU Long Name Entry -> decode & setName                   | testEncodingGnuLongNameAndPax
 *   - Malformed GNU Long Name (not followed by entry)           | testMalformedGnuLongNameEntry
 *   - Pax Header parsing & attribute application                | testPaxHeadersParsingAndApplication
 *   - Malformed Pax Header (premature EOF in rest bytes)        | testMalformedPaxHeaderTruncatedRest
 *   - GNU Sparse entries traversal                              | testGnuSparseEntryHandling
 * ----------------------------------------------------------------------------------------------------
 * read() & available() & skip()                                 |
 *   - read when currEntry == null -> IllegalStateException      | testReadWithoutEntryThrowsIllegalState
 *   - read when hasHitEOF or offset >= size -> returns -1       | testReadBeyondEntryReturnsEOF
 *   - available() > Integer.MAX_VALUE cap                       | testAvailableCappedAtIntegerMaxValue
 *   - read truncated entry -> IOException("Truncated TAR...")   | testReadTruncatedEntryThrowsIOException
 *   - skip(n <= 0) returns 0                                    | testSkipZeroOrNegative
 *   - skip positive bound to entrySize                          | testSkipWithinEntryAndAcrossBoundaries
 * ----------------------------------------------------------------------------------------------------
 * EOF & Block Alignment                                         |
 *   - Single vs double EOF records & mark/reset support        | testConsumeSecondEOFRecordWithMarkReset
 *   - consumeRemainderOfLastBlock padding skipping             | testConsumeRemainderOfLastBlock
 *   - canReadEntryData logic (TarArchiveEntry vs non-tar)       | testCanReadEntryData
 *   - markSupported, mark, reset no-op compliance              | testMarkAndResetBehavior
 * ----------------------------------------------------------------------------------------------------
 */
public class TarArchiveInputStreamGeminiTest {

    private static final int RECORD_SIZE = TarConstants.DEFAULT_RCDSIZE;
    private static final int BLOCK_SIZE = TarConstants.DEFAULT_BLKSIZE;

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadStandardArchiveEntriesAndContent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos, BLOCK_SIZE, RECORD_SIZE);

        byte[] content1 = "Hello World".getBytes(CharsetNames.UTF_8);
        TarArchiveEntry entry1 = new TarArchiveEntry("test1.txt");
        entry1.setSize(content1.length);
        taos.putArchiveEntry(entry1);
        taos.write(content1);
        taos.closeArchiveEntry();

        byte[] content2 = "Second File Content with padding check".getBytes(CharsetNames.UTF_8);
        TarArchiveEntry entry2 = new TarArchiveEntry("dir/test2.txt");
        entry2.setSize(content2.length);
        taos.putArchiveEntry(entry2);
        taos.write(content2);
        taos.closeArchiveEntry();

        taos.finish();
        taos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        TarArchiveInputStream tais = new TarArchiveInputStream(bais, BLOCK_SIZE, RECORD_SIZE);

        assertEquals(RECORD_SIZE, tais.getRecordSize());

        // First entry
        ArchiveEntry e1 = tais.getNextEntry();
        assertNotNull(e1);
        assertEquals("test1.txt", e1.getName());
        assertEquals(entry1.getSize(), tais.available());

        byte[] readBuf1 = new byte[content1.length];
        int readCount1 = tais.read(readBuf1, 0, readBuf1.length);
        assertEquals(content1.length, readCount1);
        assertArrayEquals(content1, readBuf1);
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(readBuf1, 0, 1));

        // Second entry
        TarArchiveEntry e2 = tais.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("dir/test2.txt", e2.getName());
        assertSame(e2, tais.getCurrentEntry());

        byte[] readBuf2 = new byte[content2.length];
        int readCount2 = tais.read(readBuf2, 0, readBuf2.length);
        assertEquals(content2.length, readCount2);
        assertArrayEquals(content2, readBuf2);

        // EOF
        assertNull(tais.getNextTarEntry());
        assertNull(tais.getCurrentEntry());
        assertTrue(tais.isAtEOF());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testSkipCurrentEntryDataAndPadding() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos, BLOCK_SIZE, RECORD_SIZE);

        byte[] content1 = new byte[700]; // Multi-record entry requiring record padding
        Arrays.fill(content1, (byte) 'A');
        TarArchiveEntry entry1 = new TarArchiveEntry("large_padded.bin");
        entry1.setSize(content1.length);
        taos.putArchiveEntry(entry1);
        taos.write(content1);
        taos.closeArchiveEntry();

        byte[] content2 = "Next Entry".getBytes(CharsetNames.UTF_8);
        TarArchiveEntry entry2 = new TarArchiveEntry("next.txt");
        entry2.setSize(content2.length);
        taos.putArchiveEntry(entry2);
        taos.write(content2);
        taos.closeArchiveEntry();

        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        TarArchiveEntry e1 = tais.getNextTarEntry();
        assertNotNull(e1);
        // Only read 10 bytes, leave rest to be skipped by getNextTarEntry
        byte[] partial = new byte[10];
        assertEquals(10, tais.read(partial, 0, 10));

        // Advance to next entry, triggering skip on entry data and padding records
        TarArchiveEntry e2 = tais.getNextTarEntry();
        assertNotNull(e2);
        assertEquals("next.txt", e2.getName());
        assertEquals(content2.length, e2.getSize());

        tais.close();
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSkipZeroOrNegative() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("entry.txt");
        entry.setSize(20);
        taos.putArchiveEntry(entry);
        taos.write(new byte[20]);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());

        assertEquals(0L, tais.skip(0));
        assertEquals(0L, tais.skip(-5));
        assertEquals(0L, tais.skip(-100));

        tais.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntryAndAcrossBoundaries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        byte[] data = "0123456789ABCDEFGHIJ".getBytes(CharsetNames.UTF_8);
        TarArchiveEntry entry = new TarArchiveEntry("test_skip.txt");
        entry.setSize(data.length);
        taos.putArchiveEntry(entry);
        taos.write(data);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());

        long skipped = tais.skip(5);
        assertEquals(5L, skipped);

        byte[] buf = new byte[5];
        assertEquals(5, tais.read(buf, 0, 5));
        assertEquals("56789", new String(buf, CharsetNames.UTF_8));

        // Skip more than available
        long remainingSkipped = tais.skip(500);
        assertEquals(10L, remainingSkipped);
        assertEquals(-1, tais.read(buf, 0, 1));

        tais.close();
    }

    @Test(timeout = 4000)
    public void testAvailableCappedAtIntegerMaxValue() throws IOException {
        byte[] dummyHeader = new byte[RECORD_SIZE];
        TarArchiveEntry entry = new TarArchiveEntry("large");
        entry.setSize((long) Integer.MAX_VALUE + 5000L);
        entry.writeEntryHeader(dummyHeader);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(dummyHeader));
        tais.getNextTarEntry();

        assertEquals(Integer.MAX_VALUE, tais.available());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testReadBeyondEntryReturnsEOF() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());
        assertEquals(-1, tais.read(new byte[10], 0, 10));

        tais.close();
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Encodings, GNU Names, Pax)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndEncodingSupport() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);

        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais);
        assertEquals(RECORD_SIZE, tais1.getRecordSize());

        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais, "UTF-8");
        assertEquals(RECORD_SIZE, tais2.getRecordSize());

        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais, 2048);
        assertEquals(RECORD_SIZE, tais3.getRecordSize());

        TarArchiveInputStream tais4 = new TarArchiveInputStream(bais, 2048, "ISO-8859-1");
        assertEquals(RECORD_SIZE, tais4.getRecordSize());

        TarArchiveInputStream tais5 = new TarArchiveInputStream(bais, 2048, 1024);
        assertEquals(1024, tais5.getRecordSize());

        TarArchiveInputStream tais6 = new TarArchiveInputStream(bais, 2048, 1024, "CP437");
        assertEquals(1024, tais6.getRecordSize());

        tais1.close();
        tais2.close();
        tais3.close();
        tais4.close();
        tais5.close();
        tais6.close();
    }

    @Test(timeout = 4000)
    public void testEncodingGnuLongNameAndPax() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos, "UTF-8");
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        // Long name > 100 characters with non-ASCII UTF-8 characters
        String longNonAsciiName = "long_path_prefix_directory_structure_exceeding_standard_one_hundred_chars_limit_"
                + "\u00e4\u00f6\u00fc\u00df_special_characters_filename.txt";
        TarArchiveEntry entry = new TarArchiveEntry(longNonAsciiName);
        byte[] content = "Payload".getBytes("UTF-8");
        entry.setSize(content.length);

        taos.putArchiveEntry(entry);
        taos.write(content);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "UTF-8");

        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals(longNonAsciiName, readEntry.getName());
        assertEquals(content.length, readEntry.getSize());

        byte[] readContent = new byte[content.length];
        assertEquals(content.length, tais.read(readContent, 0, content.length));
        assertArrayEquals(content, readContent);

        tais.close();
    }

    @Test(timeout = 4000)
    public void testGnuLongLinkEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        String longTarget = "a/very/long/target/symlink/path/that/definitely/exceeds/the/normal/tar/header/limit/"
                + "of/one/hundred/characters/and/must/be/split/or/stored/as/gnu/long/link/target.txt";
        TarArchiveEntry entry = new TarArchiveEntry("symlink_entry", TarConstants.LF_SYMLINK);
        entry.setLinkName(longTarget);

        taos.putArchiveEntry(entry);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("symlink_entry", readEntry.getName());
        assertEquals(longTarget, readEntry.getLinkName());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeadersParsingAndApplication() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos, "UTF-8");
        taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("pax_entry.txt");
        entry.setSize(12);
        entry.setGroupId(1234);
        entry.setGroupName("groupie");
        entry.setUserId(5678);
        entry.setUserName("tester");
        entry.setModTime(1400000000000L);

        taos.putArchiveEntry(entry);
        taos.write("012345678901".getBytes(CharsetNames.UTF_8));
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "UTF-8");

        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("pax_entry.txt", readEntry.getName());
        assertEquals(12, readEntry.getSize());
        assertEquals(1234, readEntry.getGroupId());
        assertEquals("groupie", readEntry.getGroupName());
        assertEquals(5678, readEntry.getUserId());
        assertEquals("tester", readEntry.getUserName());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersDirectly() throws IOException {
        // Construct raw PAX header string: "length keyword=value\n"
        String rawPax = "25 path=some/custom/path\n"
                + "29 linkpath=link/target/here\n"
                + "16 gid=10001\n"
                + "18 gname=supergroup\n"
                + "15 uid=20002\n"
                + "16 uname=john_doe\n"
                + "17 size=9999999\n"
                + "28 mtime=1357924680.123456\n"
                + "20 SCHILY.devminor=5\n"
                + "21 SCHILY.devmajor=10\n";

        byte[] paxBytes = rawPax.getBytes(CharsetNames.UTF_8);
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));

        Map<String, String> headers = tais.parsePaxHeaders(new ByteArrayInputStream(paxBytes));
        assertEquals("some/custom/path", headers.get("path"));
        assertEquals("link/target/here", headers.get("linkpath"));
        assertEquals("10001", headers.get("gid"));
        assertEquals("supergroup", headers.get("gname"));
        assertEquals("20002", headers.get("uid"));
        assertEquals("john_doe", headers.get("uname"));
        assertEquals("9999999", headers.get("size"));
        assertEquals("1357924680.123456", headers.get("mtime"));
        assertEquals("5", headers.get("SCHILY.devminor"));
        assertEquals("10", headers.get("SCHILY.devmajor"));

        tais.close();
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testReadWithoutEntryThrowsIllegalState() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[RECORD_SIZE]));
        tais.read(new byte[10], 0, 10);
    }

    @Test(timeout = 4000)
    public void testReadTruncatedEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry entry = new TarArchiveEntry("truncated.txt");
        entry.setSize(100);
        taos.putArchiveEntry(entry);
        taos.write(new byte[50]); // write only 50 bytes instead of 100
        // Do not finish archive, write truncated raw stream
        byte[] raw = baos.toByteArray();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(raw));
        assertNotNull(tais.getNextTarEntry());
        byte[] buf = new byte[100];
        int read1 = tais.read(buf, 0, 50);
        assertEquals(50, read1);

        try {
            tais.read(buf, 50, 50);
            fail("Expected IOException due to truncated entry data");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Truncated TAR archive"));
        }
        tais.close();
    }

    @Test(timeout = 4000)
    public void testCorruptedHeaderThrowsIOException() {
        byte[] corrupted = new byte[RECORD_SIZE];
        Arrays.fill(corrupted, (byte) 0xFF); // Invalid octal values in header

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(corrupted));
        try {
            tais.getNextTarEntry();
            fail("Expected IOException when parsing invalid header");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Error detected parsing the header"));
            assertNotNull(expected.getCause());
            assertTrue(expected.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testMalformedPaxHeaderTruncatedRest() {
        // Declared length 50, but stream ends before that
        String truncatedPax = "50 path=incomplete";
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            tais.parsePaxHeaders(new ByteArrayInputStream(truncatedPax.getBytes(CharsetNames.UTF_8)));
            fail("Expected IOException on truncated Paxheader");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Failed to read Paxheader"));
        }
    }

    @Test(timeout = 4000)
    public void testMalformedGnuLongNameEntry() throws IOException {
        // GNU Long name entry that is NOT followed by an actual entry
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);
        TarArchiveEntry gnuEntry = new TarArchiveEntry(TarConstants.GNU_LONGLINK, TarConstants.LF_GNUTYPE_LONGNAME);
        byte[] longNameData = "actual_file_name.txt\0".getBytes(CharsetNames.UTF_8);
        gnuEntry.setSize(longNameData.length);
        taos.putArchiveEntry(gnuEntry);
        taos.write(longNameData);
        taos.closeArchiveEntry();
        taos.finish();
        taos.close();

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        // Should handle gracefully and return null when entry following long name is missing
        TarArchiveEntry entry = tais.getNextTarEntry();
        assertNull(entry);
        tais.close();
    }

    @Test(timeout = 4000)
    public void testTruncatedArchiveAtHeaderReturnsNull() throws IOException {
        byte[] partialHeader = new byte[RECORD_SIZE - 100];
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(partialHeader));
        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    // -------------------------------------------------------------------------
    // Partition E: State Inspection, Mark/Reset, Sparse, Matches & Cleanup
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMatchesValidPosixGnuAntAndInvalid() {
        byte[] validPosix = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, validPosix, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, validPosix, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(validPosix, 512));

        byte[] validGnuZero = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, validGnuZero, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, validGnuZero, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(validGnuZero, 512));

        byte[] validGnuSpace = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, validGnuSpace, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, validGnuSpace, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(validGnuSpace, 512));

        byte[] validAnt = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, validAnt, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, validAnt, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(validAnt, 512));

        byte[] invalid = new byte[512];
        assertFalse(TarArchiveInputStream.matches(invalid, 512));
    }

    @Test(timeout = 4000)
    public void testMatchesTooShort() {
        byte[] shortBuffer = new byte[TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN - 1];
        assertFalse(TarArchiveInputStream.matches(shortBuffer, shortBuffer.length));
    }

    @Test(timeout = 4000)
    public void testMarkAndResetBehavior() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.markSupported());
        tais.mark(1024); // no-op check
        tais.reset();    // no-op check
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));

        TarArchiveEntry standardEntry = new TarArchiveEntry("test.txt");
        assertTrue(tais.canReadEntryData(standardEntry));

        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.txt", TarConstants.LF_GNUTYPE_SPARSE);
        assertFalse(tais.canReadEntryData(sparseEntry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "dummy"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        };
        assertFalse(tais.canReadEntryData(nonTarEntry));
    }

    @Test(timeout = 4000)
    public void testHitEOFStateTransitions() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[RECORD_SIZE * 2]));
        assertFalse(tais.isAtEOF());

        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());

        // Subsequent call returns null immediately
        assertNull(tais.getNextTarEntry());

        tais.setAtEOF(false);
        assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());

        TarArchiveEntry entry = new TarArchiveEntry("manual.txt");
        tais.setCurrentEntry(entry);
        assertSame(entry, tais.getCurrentEntry());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testConsumeSecondEOFRecordWithMarkReset() throws IOException {
        // Stream containing one EOF record, then real data, wrapped in Markable stream
        byte[] eofRecord = new byte[RECORD_SIZE];
        byte[] nextRecord = new byte[RECORD_SIZE];
        Arrays.fill(nextRecord, (byte) 'Z');

        byte[] combined = new byte[RECORD_SIZE * 2];
        System.arraycopy(eofRecord, 0, combined, 0, RECORD_SIZE);
        System.arraycopy(nextRecord, 0, combined, RECORD_SIZE, RECORD_SIZE);

        ByteArrayInputStream markableStream = new ByteArrayInputStream(combined);
        TarArchiveInputStream tais = new TarArchiveInputStream(markableStream, BLOCK_SIZE, RECORD_SIZE);

        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testConsumeRemainderOfLastBlock() throws IOException {
        // Total bytes written is less than 1 full block (10 records = 5120 bytes)
        // 1 record of data (512 bytes) + 2 EOF records (1024 bytes) = 1536 bytes
        // Remainder to skip = 5120 - 1536 = 3584 bytes
        byte[] dummyBlock = new byte[BLOCK_SIZE];
        TarArchiveEntry entry = new TarArchiveEntry("test.bin");
        entry.setSize(0);
        entry.writeEntryHeader(dummyBlock);

        // Record 1: header, Records 2-3: zeros (EOF marker), Records 4-10: padding
        ByteArrayInputStream bais = new ByteArrayInputStream(dummyBlock);
        TarArchiveInputStream tais = new TarArchiveInputStream(bais, BLOCK_SIZE, RECORD_SIZE);

        TarArchiveEntry read = tais.getNextTarEntry();
        assertNotNull(read);
        assertEquals("test.bin", read.getName());

        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        assertEquals(BLOCK_SIZE, tais.getBytesRead());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testGnuSparseEntryHandling() throws IOException {
        // Create an entry marked as extended sparse GNU
        byte[] header = new byte[RECORD_SIZE];
        byte[] sparseHeader = new byte[RECORD_SIZE];

        TarArchiveEntry entry = new TarArchiveEntry("sparse.bin", TarConstants.LF_GNUTYPE_SPARSE);
        entry.setSize(1024);
        entry.writeEntryHeader(header);
        // Mark as extended in header (offset 482)
        header[482] = 1;

        // In the sparse header record, set isextended to 0 (offset 504 in sparse record)
        sparseHeader[504] = 0;

        byte[] archive = new byte[RECORD_SIZE * 4];
        System.arraycopy(header, 0, archive, 0, RECORD_SIZE);
        System.arraycopy(sparseHeader, 0, archive, RECORD_SIZE, RECORD_SIZE);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry readEntry = tais.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("sparse.bin", readEntry.getName());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testCloseDelegation() throws IOException {
        final boolean[] closed = new boolean[] { false };
        InputStream is = new FilterInputStream(new ByteArrayInputStream(new byte[0])) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };

        TarArchiveInputStream tais = new TarArchiveInputStream(is);
        assertFalse(closed[0]);
        tais.close();
        assertTrue(closed[0]);
    }
}