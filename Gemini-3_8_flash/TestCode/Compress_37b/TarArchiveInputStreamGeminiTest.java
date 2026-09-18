/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.CharsetNames;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: TarArchiveInputStream
 *
 * Defect Targeted (Defects4J):
 * - Issue: TarArchiveInputStream fails when PAX headers contain leading or intervening blank lines,
 *          causing NegativeArraySizeException in parsePaxHeaders due to negative length calculations.
 * - Test Target: testSurvivesBlankLinesInPaxHeader()
 *
 * Branch Coverage Targets:
 * 1. matches(byte[] signature, int length):
 *    - length < 265 boundary (below VERSION_OFFSET + VERSIONLEN) -> false
 *    - POSIX magic & version match -> true
 *    - GNU magic & VERSION_GNU_SPACE match -> true
 *    - GNU magic & VERSION_GNU_ZERO match -> true
 *    - ANT magic & VERSION_ANT match -> true
 *    - Invalid magic or invalid version -> false
 * 2. available():
 *    - isDirectory() true -> returns 0
 *    - (entrySize - entryOffset) > Integer.MAX_VALUE -> returns Integer.MAX_VALUE
 *    - normal range -> returns (int)(entrySize - entryOffset)
 * 3. skip(long n):
 *    - n <= 0 -> returns 0
 *    - isDirectory() true -> returns 0
 *    - Math.min(n, available) calculations and offset update
 * 4. read(byte[] buf, int offset, int numToRead):
 *    - hasHitEOF == true -> -1
 *    - isDirectory() == true -> -1
 *    - entryOffset >= entrySize -> -1
 *    - currEntry == null -> IllegalStateException
 *    - numToRead > 0 and EOF reached -> IOException("Truncated TAR archive")
 * 5. getNextTarEntry():
 *    - hasHitEOF true -> returns null
 *    - currEntry != null -> skips remaining data and executes skipRecordPadding()
 *    - headerBuf == null -> returns null (currEntry = null)
 *    - IllegalArgumentException in TarArchiveEntry instantiation -> IOException
 *    - isGNULongLinkEntry() -> reads long link data; handles null data (truncated)
 *    - isGNULongNameEntry() -> reads long name data; handles null data (truncated)
 *    - isGlobalPaxHeader() -> parses and stores global headers, advances to entry
 *    - isPaxHeader() -> parses PAX headers and applies to entry
 *    - !globalPaxHeaders.isEmpty() -> applies global headers to subsequent entries
 *    - isOldGNUSparse() / isExtended() -> reads chained TarArchiveSparseEntry records
 * 6. parsePaxHeaders(InputStream i):
 *    - restLen == 1 -> removal of existing keyword
 *    - restLen > 1 -> sets keyword value
 *    - truncated read (got != restLen) -> IOException
 *    - blank lines / newline characters -> tests defect fix
 * 7. canReadEntryData(ArchiveEntry ae):
 *    - ae not instance of TarArchiveEntry -> false
 *    - ae.isSparse() == true -> false
 *    - regular TarArchiveEntry -> true
 * 8. EOF record handling & tryToConsumeSecondEOFRecord():
 *    - marked stream with second EOF record -> consumed
 *    - marked stream without second EOF record -> reset
 *    - unmarked stream (markSupported == false) -> handled safely
 */
public class TarArchiveInputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadStandardArchiveEntries() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final byte[] content1 = "FirstFileContent".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry1 = new TarArchiveEntry("file1.txt");
        entry1.setSize(content1.length);
        tos.putArchiveEntry(entry1);
        tos.write(content1);
        tos.closeArchiveEntry();

        final byte[] content2 = "SecondFile".getBytes(CharsetNames.UTF_8);
        final TarArchiveEntry entry2 = new TarArchiveEntry("file2.txt");
        entry2.setSize(content2.length);
        tos.putArchiveEntry(entry2);
        tos.write(content2);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));

        final TarArchiveEntry r1 = tais.getNextTarEntry();
        assertNotNull(r1);
        assertEquals("file1.txt", r1.getName());
        assertSame(r1, tais.getCurrentEntry());
        assertEquals(content1.length, tais.available());

        final byte[] readBuf1 = new byte[content1.length];
        final int readBytes1 = tais.read(readBuf1, 0, readBuf1.length);
        assertEquals(content1.length, readBytes1);
        assertArrayEquals(content1, readBuf1);
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(readBuf1, 0, 1));

        // Advance to next entry without reading all of it (tests skipRecordPadding & auto-skip)
        final ArchiveEntry r2 = tais.getNextEntry();
        assertNotNull(r2);
        assertEquals("file2.txt", r2.getName());

        final TarArchiveEntry r3 = tais.getNextTarEntry();
        assertNull(r3);
        assertTrue(tais.isAtEOF());

        tais.close();
    }

    @Test(timeout = 4000)
    public void testGnuLongNameAndLongLink() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        final String longName = "very/long/directory/path/structure/that/exceeds/one/hundred/characters/in/total/length/file_with_long_name.txt";
        final String longLink = "very/long/directory/path/structure/that/exceeds/one/hundred/characters/in/total/length/target_symlink_file.txt";

        final TarArchiveEntry entry = new TarArchiveEntry(longName, TarConstants.LF_SYMLINK);
        entry.setLinkName(longLink);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry readEntry = tais.getNextTarEntry();

        assertNotNull(readEntry);
        assertEquals(longName, readEntry.getName());
        assertEquals(longLink, readEntry.getLinkName());
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeadersApplication() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/target.txt", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        final String paxData = "27 path=actual_pax.txt\n"
                + "23 linkpath=target_link\n"
                + "15 gid=4567\n"
                + "18 gname=paxgroup\n"
                + "15 uid=1234\n"
                + "17 uname=paxuser\n"
                + "14 size=5\n"
                + "23 mtime=123456789.5\n"
                + "23 SCHILY.devminor=10\n"
                + "23 SCHILY.devmajor=20\n";
        final byte[] paxBytes = paxData.getBytes(CharsetNames.UTF_8);
        paxEntry.setSize(paxBytes.length);
        tos.putArchiveEntry(paxEntry);
        tos.write(paxBytes);
        tos.closeArchiveEntry();

        final TarArchiveEntry realEntry = new TarArchiveEntry("original.txt");
        realEntry.setSize(5);
        tos.putArchiveEntry(realEntry);
        tos.write("abcde".getBytes(CharsetNames.UTF_8));
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry result = tais.getNextTarEntry();

        assertNotNull(result);
        assertEquals("actual_pax.txt", result.getName());
        assertEquals("target_link", result.getLinkName());
        assertEquals(4567L, result.getGroupId());
        assertEquals("paxgroup", result.getGroupName());
        assertEquals(1234L, result.getUserId());
        assertEquals("paxuser", result.getUserName());
        assertEquals(5L, result.getSize());
        assertEquals(123456789500L, result.getModTime().getTime());
        assertEquals(10, result.getDevMinor());
        assertEquals(20, result.getDevMajor());

        final byte[] buf = new byte[5];
        assertEquals(5, tais.read(buf, 0, 5));
        assertEquals("abcde", new String(buf, CharsetNames.UTF_8));

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testGlobalPaxHeadersApplication() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final TarArchiveEntry globalPax = new TarArchiveEntry("GlobalHead/pax", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        final byte[] gData = "17 gname=globalgrp\n16 uname=globalusr\n".getBytes(CharsetNames.UTF_8);
        globalPax.setSize(gData.length);
        tos.putArchiveEntry(globalPax);
        tos.write(gData);
        tos.closeArchiveEntry();

        final TarArchiveEntry e1 = new TarArchiveEntry("fileA.txt");
        e1.setSize(0);
        tos.putArchiveEntry(e1);
        tos.closeArchiveEntry();

        final TarArchiveEntry e2 = new TarArchiveEntry("fileB.txt");
        e2.setSize(0);
        tos.putArchiveEntry(e2);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry r1 = tais.getNextTarEntry();
        assertNotNull(r1);
        assertEquals("fileA.txt", r1.getName());
        assertEquals("globalgrp", r1.getGroupName());
        assertEquals("globalusr", r1.getUserName());

        final TarArchiveEntry r2 = tais.getNextTarEntry();
        assertNotNull(r2);
        assertEquals("fileB.txt", r2.getName());
        assertEquals("globalgrp", r2.getGroupName());
        assertEquals("globalusr", r2.getUserName());

        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesSignatureBoundaries() {
        final byte[] signature = new byte[TarConstants.DEFAULT_RCDSIZE];

        assertFalse(TarArchiveInputStream.matches(signature, 0));
        assertFalse(TarArchiveInputStream.matches(signature, TarConstants.VERSION_OFFSET + TarConstants.VERSIONLEN - 1));
        assertFalse(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // POSIX magic & version
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // POSIX magic with wrong version
        signature[TarConstants.VERSION_OFFSET] = '9';
        assertFalse(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // GNU magic & space version
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // GNU magic & zero version
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // GNU magic with invalid version
        signature[TarConstants.VERSION_OFFSET] = 'X';
        assertFalse(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));

        // ANT magic & version
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, signature, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, signature, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        assertTrue(TarArchiveInputStream.matches(signature, TarConstants.DEFAULT_RCDSIZE));
    }

    @Test(timeout = 4000)
    public void testAvailableAndSkipOnDirectoryEntry() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final TarArchiveEntry dirEntry = new TarArchiveEntry("testdir/", TarConstants.LF_DIR);
        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertTrue(entry.isDirectory());

        assertEquals(0, tais.available());
        assertEquals(0L, tais.skip(100L));
        assertEquals(0L, tais.skip(-5L));
        assertEquals(-1, tais.read(new byte[10], 0, 10));

        tais.close();
    }

    @Test(timeout = 4000)
    public void testSkipBoundariesAndExhaustion() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final byte[] content = new byte[100];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) i;
        }
        final TarArchiveEntry entry = new TarArchiveEntry("file.bin");
        entry.setSize(content.length);
        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNotNull(tais.getNextTarEntry());

        assertEquals(0L, tais.skip(-10L));
        assertEquals(0L, tais.skip(0L));
        assertEquals(30L, tais.skip(30L));
        assertEquals(70, tais.available());

        final long skippedRemainder = tais.skip(200L);
        assertEquals(70L, skippedRemainder);
        assertEquals(0, tais.available());
        assertEquals(-1, tais.read(new byte[1], 0, 1));

        tais.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeaderKeywordRemoval() throws Exception {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        // "11 foo=bar\n" -> 11 bytes, sets foo=bar
        // "7 foo=\n"    -> 7 bytes: '7',' ','f','o','o','=','\n', restLen = 1, removes foo
        final String paxStream = "11 foo=bar\n7 foo=\n";
        final Map<String, String> headers = tais.parsePaxHeaders(new ByteArrayInputStream(paxStream.getBytes(CharsetNames.UTF_8)));
        assertFalse(headers.containsKey("foo"));
        tais.close();
    }

    @Test(timeout = 4000)
    public void testIsEOFRecordRecognizesNullAndZeroedRecord() {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertTrue(tais.isEOFRecord(null));
        assertTrue(tais.isEOFRecord(new byte[TarConstants.DEFAULT_RCDSIZE]));

        final byte[] nonZero = new byte[TarConstants.DEFAULT_RCDSIZE];
        nonZero[50] = 1;
        assertFalse(tais.isEOFRecord(nonZero));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where blank lines in a PAX header produce a NegativeArraySizeException
     * during parsePaxHeaders length calculation.
     */
    @Test(timeout = 4000)
    public void testSurvivesBlankLinesInPaxHeader() throws Exception {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        final String paxHeaderWithBlankLine = "\n\n28 devminor=0\n";
        final Map<String, String> headers = tais.parsePaxHeaders(
                new ByteArrayInputStream(paxHeaderWithBlankLine.getBytes(CharsetNames.UTF_8))
        );
        assertEquals(1, headers.size());
        assertEquals("0", headers.get("devminor"));
        tais.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testReadWithoutCurrentEntryThrowsException() throws IOException {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.read(new byte[10], 0, 10);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCorruptedHeaderParsingThrowsIOException() throws Exception {
        final byte[] invalidHeader = new byte[TarConstants.DEFAULT_RCDSIZE];
        for (int i = 0; i < invalidHeader.length; i++) {
            invalidHeader[i] = (byte) 0xFF;
        }
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(invalidHeader));
        tais.getNextTarEntry();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testTruncatedEntryDataThrowsIOException() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        final TarArchiveEntry entry = new TarArchiveEntry("truncated.txt");
        entry.setSize(50);
        tos.putArchiveEntry(entry);
        tos.write(new byte[20]); // Write only 20 bytes out of 50
        // Intentionally do not closeArchiveEntry properly, just flush and cut stream
        tos.flush();

        // Cut the stream right after the header and 20 bytes
        final byte[] raw = bos.toByteArray();
        final byte[] truncated = new byte[TarConstants.DEFAULT_RCDSIZE + 20];
        System.arraycopy(raw, 0, truncated, 0, truncated.length);

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(truncated));
        assertNotNull(tais.getNextTarEntry());
        final byte[] buf = new byte[50];
        tais.read(buf, 0, 50); // Must throw IOException("Truncated TAR archive")
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testTruncatedPaxHeaderThrowsIOException() throws Exception {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        // Length declares 30 bytes, but stream ends prematurely after 15 bytes
        final String truncatedPax = "30 path=foo\n";
        tais.parsePaxHeaders(new ByteArrayInputStream(truncatedPax.getBytes(CharsetNames.UTF_8)));
    }

    @Test(timeout = 4000)
    public void testMalformedLongNameNotFollowedByEntryReturnsNull() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        final TarArchiveEntry entry = new TarArchiveEntry("././@LongLink", TarConstants.LF_GNUTYPE_LONGNAME);
        final byte[] nameBytes = "testname.txt\0".getBytes(CharsetNames.UTF_8);
        entry.setSize(nameBytes.length);
        tos.putArchiveEntry(entry);
        tos.write(nameBytes);
        tos.closeArchiveEntry();
        // Close archive immediately so EOF records follow instead of an actual entry
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testMalformedLongLinkNotFollowedByEntryReturnsNull() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        final TarArchiveEntry entry = new TarArchiveEntry("././@LongLink", TarConstants.LF_GNUTYPE_LONGLINK);
        final byte[] linkBytes = "targetlink.txt\0".getBytes(CharsetNames.UTF_8);
        entry.setSize(linkBytes.length);
        tos.putArchiveEntry(entry);
        tos.write(linkBytes);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Sparse Chunks & Stream Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndRecordSize() {
        final InputStream empty = new ByteArrayInputStream(new byte[0]);
        final TarArchiveInputStream s1 = new TarArchiveInputStream(empty);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, s1.getRecordSize());

        final TarArchiveInputStream s2 = new TarArchiveInputStream(empty, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, s2.getRecordSize());

        final TarArchiveInputStream s3 = new TarArchiveInputStream(empty, 2048);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, s3.getRecordSize());

        final TarArchiveInputStream s4 = new TarArchiveInputStream(empty, 2048, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, s4.getRecordSize());

        final TarArchiveInputStream s5 = new TarArchiveInputStream(empty, 2048, 1024);
        assertEquals(1024, s5.getRecordSize());

        final TarArchiveInputStream s6 = new TarArchiveInputStream(empty, 2048, 1024, "UTF-8");
        assertEquals(1024, s6.getRecordSize());
        assertEquals("UTF-8", s6.encoding);
    }

    @Test(timeout = 4000)
    public void testMarkResetOperations() {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.markSupported());
        tais.mark(1024); // Verify no-op does not fail
        tais.reset();    // Verify no-op does not fail
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertFalse(tais.canReadEntryData(null));

        final ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "dummy"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        assertFalse(tais.canReadEntryData(nonTarEntry));

        final TarArchiveEntry regularTarEntry = new TarArchiveEntry("file.txt");
        assertTrue(tais.canReadEntryData(regularTarEntry));
    }

    @Test(timeout = 4000)
    public void testStateMutatorsAndGetters() {
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertNull(tais.getCurrentEntry());
        assertFalse(tais.isAtEOF());

        final TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tais.setCurrentEntry(entry);
        assertSame(entry, tais.getCurrentEntry());

        tais.setAtEOF(true);
        assertTrue(tais.isAtEOF());
    }

    @Test(timeout = 4000)
    public void testUnmarkedStreamTryToConsumeSecondEOFRecord() throws Exception {
        // Stream where markSupported is false
        final byte[] archiveData = new byte[TarConstants.DEFAULT_RCDSIZE * 2]; // Two EOF records
        final InputStream unmarkable = new FilterInputStream(new ByteArrayInputStream(archiveData)) {
            @Override
            public boolean markSupported() {
                return false;
            }
        };

        final TarArchiveInputStream tais = new TarArchiveInputStream(unmarkable);
        assertNull(tais.getNextTarEntry());
        assertTrue(tais.isAtEOF());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testSparseHeadersInPax() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        final TarArchiveEntry paxEntry = new TarArchiveEntry("PaxHeader/sparse.bin", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        final String paxData = "22 GNU.sparse.size=100\n"
                + "26 GNU.sparse.realsize=100\n"
                + "25 SCHILY.filetype=sparse\n";
        final byte[] paxBytes = paxData.getBytes(CharsetNames.UTF_8);
        paxEntry.setSize(paxBytes.length);
        tos.putArchiveEntry(paxEntry);
        tos.write(paxBytes);
        tos.closeArchiveEntry();

        final TarArchiveEntry realEntry = new TarArchiveEntry("sparse.bin");
        realEntry.setSize(0);
        tos.putArchiveEntry(realEntry);
        tos.closeArchiveEntry();
        tos.close();

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        final TarArchiveEntry result = tais.getNextTarEntry();
        assertNotNull(result);
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testOldGnuSparseChainedEntries() throws Exception {
        final byte[] header1 = new byte[TarConstants.DEFAULT_RCDSIZE];
        System.arraycopy("sparsefile".getBytes(CharsetNames.UTF_8), 0, header1, 0, 10);
        header1[156] = TarConstants.LF_GNUTYPE_SPARSE; // typeflag
        header1[482] = 1; // isExtended = true in TarArchiveEntry

        // Checksum calculation for header1
        long sum = 0;
        for (int i = 0; i < 148; i++) sum += (header1[i] & 0xFF);
        for (int i = 148; i < 156; i++) sum += ' ';
        for (int i = 156; i < TarConstants.DEFAULT_RCDSIZE; i++) sum += (header1[i] & 0xFF);
        final String chkStr = String.format("%06o\0 ", sum);
        System.arraycopy(chkStr.getBytes(CharsetNames.UTF_8), 0, header1, 148, 8);

        // Chained TarArchiveSparseEntry record with isExtended = false
        final byte[] sparseRecord = new byte[TarConstants.DEFAULT_RCDSIZE];
        sparseRecord[504] = 0; // isExtended = false

        final byte[] eof = new byte[TarConstants.DEFAULT_RCDSIZE * 2];

        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        data.write(header1);
        data.write(sparseRecord);
        data.write(eof);

        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(data.toByteArray()));
        final TarArchiveEntry entry = tais.getNextTarEntry();
        assertNotNull(entry);
        assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(timeout = 4000)
    public void testOldGnuSparsePrematureEOF() throws Exception {
        final byte[] header1 = new byte[TarConstants.DEFAULT_RCDSIZE];
        System.arraycopy("sparsefile".getBytes(CharsetNames.UTF_8), 0, header1, 0, 10);
        header1[156] = TarConstants.LF_GNUTYPE_SPARSE;
        header1[482] = 1; // isExtended = true

        long sum = 0;
        for (int i = 0; i < 148; i++) sum += (header1[i] & 0xFF);
        for (int i = 148; i < 156; i++) sum += ' ';
        for (int i = 156; i < TarConstants.DEFAULT_RCDSIZE; i++) sum += (header1[i] & 0xFF);
        final String chkStr = String.format("%06o\0 ", sum);
        System.arraycopy(chkStr.getBytes(CharsetNames.UTF_8), 0, header1, 148, 8);

        // Immediate EOF following header, sparse record is missing
        final TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(header1));
        assertNull(tais.getNextTarEntry());
        tais.close();
    }
}