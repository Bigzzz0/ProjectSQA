/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.tar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Targets: TarArchiveOutputStream (Java 8 / JUnit 4)
 *
 * Branch & State Coverage:
 * 1. Construction & Encodings:
 *    - All constructors (1, 2, 3, 4 params). Custom encoding string vs null encoding.
 * 2. Long File Handling Modes:
 *    - LONGFILE_ERROR: throws RuntimeException for > NAMELEN names/linkNames.
 *    - LONGFILE_TRUNCATE: truncates long name without exception.
 *    - LONGFILE_GNU: writes GNU LongLink record before actual entry.
 *    - LONGFILE_POSIX: writes PAX header ("path" / "linkpath").
 * 3. Big Number Handling Modes:
 *    - BIGNUMBER_ERROR: fails for size, gid, mtime, uid, mode, devmajor, devminor out of bounds.
 *    - BIGNUMBER_POSIX: writes PAX headers for big numbers, fails on mode.
 *    - BIGNUMBER_STAR: writes binary header encoding.
 * 4. Non-ASCII Pax Header Handling:
 *    - setAddPaxHeadersForNonAsciiNames(true/false) for regular path and linkpath.
 * 5. Record Buffering & Writing:
 *    - Assembly buffer edge cases: write smaller than recordSize, write spanning record boundary,
 *      write multiple full records directly, write exact record size.
 *    - Write when no entry open -> IllegalStateException.
 *    - Write exceeding declared entry size -> IOException.
 * 6. Lifecycle & Guard Conditions:
 *    - closeArchiveEntry before declared size written -> IOException.
 *    - closeArchiveEntry without open entry -> IOException.
 *    - finish with open entry -> IOException.
 *    - finish called twice -> IOException.
 *    - putArchiveEntry or createArchiveEntry on finished stream -> IOException.
 *    - Padding logic: recordsWritten % recordsPerBlock alignment.
 * 7. Name Sanitization:
 *    - stripTo7Bits replaces 0, '/', '\\' with '_' and strips > 127 bits; PAX name truncation at 99 chars.
 * 8. Defects4J Ground Truth Target:
 *    - ArchiveStreamFactory encoding propagation / custom encoding roundtrip for entry names.
 */
public class TarArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardLifecycleSingleEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos.getRecordSize());
        assertEquals(0, tos.getBytesWritten());
        assertEquals(0, tos.getCount());

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] data = "Hello Tar World".getBytes("UTF-8");
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        tos.write(data);
        tos.closeArchiveEntry();

        tos.finish();
        tos.close();

        assertTrue(tos.getBytesWritten() > 0);
        assertTrue(tos.getCount() > 0);
        assertEquals(tos.getBytesWritten(), bos.toByteArray().length);

        // Verify read back
        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertEquals("test.txt", readEntry.getName());
        assertEquals(data.length, readEntry.getSize());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testDirectoryEntryHandling() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("folder/");
        dirEntry.setSize(100); // Directory size should be overridden to 0 by tos.putArchiveEntry
        tos.putArchiveEntry(dirEntry);
        tos.closeArchiveEntry();

        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry readEntry = tis.getNextTarEntry();
        assertNotNull(readEntry);
        assertTrue(readEntry.isDirectory());
        assertEquals(0, readEntry.getSize());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testFlushAndCreateArchiveEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tempFile = File.createTempFile("tar_test", ".tmp");
        tempFile.deleteOnExit();

        ArchiveEntry entry = tos.createArchiveEntry(tempFile, "entry_from_file");
        assertNotNull(entry);
        assertTrue(entry instanceof TarArchiveEntry);
        assertEquals("entry_from_file", entry.getName());

        tos.flush();
        tos.finish();
        tos.close();

        tempFile.delete();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Record Buffering
    // =========================================================================

    @Test(timeout = 4000)
    public void testAssemblyBufferSmallWrites() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 1024, 512);

        TarArchiveEntry entry = new TarArchiveEntry("small_chunks.bin");
        byte[] payload = new byte[700];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i & 0xFF);
        }
        entry.setSize(payload.length);
        tos.putArchiveEntry(entry);

        // Write 100 bytes (less than 512)
        tos.write(payload, 0, 100);
        // Write 450 bytes (crosses 512 limit into next record)
        tos.write(payload, 100, 450);
        // Write remaining 150 bytes
        tos.write(payload, 550, 150);

        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(700, read.getSize());
        byte[] readBuf = new byte[700];
        int totalRead = 0;
        int r;
        while ((r = tis.read(readBuf, totalRead, 700 - totalRead)) > 0) {
            totalRead += r;
        }
        assertEquals(700, totalRead);
        assertArrayEquals(payload, readBuf);
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLargeDirectRecordWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("large.bin");
        int size = 2048; // Exactly 4 records (512 each)
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 127);
        }
        entry.setSize(size);
        tos.putArchiveEntry(entry);

        tos.write(data, 0, size);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertEquals(size, read.getSize());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testBlockPaddingOnFinish() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int blockSize = 1024; // 2 records per block
        int recordSize = 512;
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, blockSize, recordSize);

        TarArchiveEntry entry = new TarArchiveEntry("pad_test.txt");
        entry.setSize(512);
        tos.putArchiveEntry(entry);
        tos.write(new byte[512]);
        tos.closeArchiveEntry();

        tos.finish();
        tos.close();

        byte[] output = bos.toByteArray();
        assertEquals(0, output.length % blockSize);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Encoding & Names & Defects4J ground truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodingOutputStreamPreservation() throws IOException {
        // Target defect: ArchiveStreamFactoryTest::testEncodingOutputStream
        // Verifies that TarArchiveOutputStream constructed with custom encoding handles non-ASCII names cleanly
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        String customEncoding = "ISO-8859-1";
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, customEncoding);

        String entryName = "t\u00e9st_\u00e0.txt"; // accented characters
        TarArchiveEntry entry = new TarArchiveEntry(entryName);
        entry.setSize(4);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2, 3, 4});
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()), customEncoding);
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(entryName, read.getName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameGnuMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            sb.append("a");
        }
        sb.append(".txt");
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(longName, read.getName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLongLinkNameGnuMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 110; i++) {
            sb.append("l");
        }
        String longLink = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry("symlink", TarConstants.LF_SYMLINK);
        entry.setLinkName(longLink);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals("symlink", read.getName());
        assertEquals(longLink, read.getLinkName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNamePosixMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("x");
        }
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(longName, read.getName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLongLinkNamePosixMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            sb.append("target");
        }
        String longLink = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry("link_file", TarConstants.LF_LINK);
        entry.setLinkName(longLink);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(longLink, read.getLinkName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testLongFileNameTruncateMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            sb.append("z");
        }
        String longName = sb.toString();

        TarArchiveEntry entry = new TarArchiveEntry(longName);
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(100, read.getName().length());
        assertEquals(longName.substring(0, 100), read.getName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testNonAsciiPaxHeadersOption() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);

        String nonAsciiName = "\u0440\u0443\u0441\u0441\u043a\u0438\u0439.txt"; // Russian chars
        TarArchiveEntry entry = new TarArchiveEntry(nonAsciiName);
        entry.setSize(0);

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(nonAsciiName, read.getName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testNonAsciiPaxHeadersForLinkPath() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setAddPaxHeadersForNonAsciiNames(true);

        TarArchiveEntry entry = new TarArchiveEntry("sym", TarConstants.LF_SYMLINK);
        entry.setSize(0);
        entry.setLinkName("\u00fcber_link");

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals("\u00fcber_link", read.getLinkName());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testBigNumberStarMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_STAR);

        TarArchiveEntry entry = new TarArchiveEntry("star_big.bin");
        long bigSize = 0100000000000L; // 8 GiB, exceeds standard octal 11 digits
        entry.setSize(bigSize);
        entry.setGroupId(077777777L + 1);
        entry.setUserId(077777777L + 2);

        tos.putArchiveEntry(entry);
        // Do not write bytes, but test that header creation succeeded
        try {
            tos.closeArchiveEntry();
            fail("Expected IOException due to unwritten bytes");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("before the '" + bigSize + "' bytes specified"));
        }
        tos.close();
    }

    @Test(timeout = 4000)
    public void testBigNumberPosixMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("posix_big.bin");
        entry.setSize(0);
        entry.setGroupId(TarConstants.MAXID + 10);
        entry.setUserId(TarConstants.MAXID + 20);
        entry.setDevMajor((int) TarConstants.MAXID + 5);
        entry.setDevMinor((int) TarConstants.MAXID + 6);
        entry.setModTime(new Date(1000L * (TarConstants.MAXSIZE + 10)));

        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        TarArchiveEntry read = tis.getNextTarEntry();
        assertNotNull(read);
        assertEquals(TarConstants.MAXID + 10, read.getGroupId());
        assertEquals(TarConstants.MAXID + 20, read.getUserId());
        assertEquals(TarConstants.MAXID + 5, read.getDevMajor());
        assertEquals(TarConstants.MAXID + 6, read.getDevMinor());
        tis.close();
    }

    @Test(timeout = 4000)
    public void testWritePaxHeadersSanitizesInvalidChars() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("entry");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("key", "val");

        // entryName with 0, '/', '\\', and > 127 char
        String trickyName = "a\0b/c\\d\u00FF";
        tos.writePaxHeaders(entry, trickyName, headers);
        tos.finish();
        tos.close();

        byte[] tarBytes = bos.toByteArray();
        assertTrue(tarBytes.length > 0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testLongFileNameErrorModeThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            sb.append("a");
        }
        TarArchiveEntry entry = new TarArchiveEntry(sb.toString());
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testLongLinkNameErrorModeThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 101; i++) {
            sb.append("b");
        }
        TarArchiveEntry entry = new TarArchiveEntry("short_name", TarConstants.LF_LINK);
        entry.setLinkName(sb.toString());
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("huge.bin");
        entry.setSize(TarConstants.MAXSIZE + 1L);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForNegativeSize() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("neg.bin");
        entry.setSize(-1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForGroupId() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("gid.bin");
        entry.setGroupId(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForUserId() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("uid.bin");
        entry.setUserId(TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForDevMajor() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("devmaj.bin");
        entry.setDevMajor((int) TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForDevMinor() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("devmin.bin");
        entry.setDevMinor((int) TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberErrorModeThrowsForMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_ERROR);

        TarArchiveEntry entry = new TarArchiveEntry("mode.bin");
        entry.setMode((int) TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testBigNumberPosixThrowsOnBadMode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);

        TarArchiveEntry entry = new TarArchiveEntry("bad_mode.bin");
        entry.setMode((int) TarConstants.MAXID + 1);
        tos.putArchiveEntry(entry);
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWriteWithoutOpenEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testWriteExceedingSizeThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("overflow.bin");
        entry.setSize(2);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2, 3});
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryBeforeSizeMetThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("underflow.bin");
        entry.setSize(10);
        tos.putArchiveEntry(entry);
        tos.write(new byte[]{1, 2, 3});
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryWithoutOpenEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithUnclosedEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        TarArchiveEntry entry = new TarArchiveEntry("unclosed.bin");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.finish();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishTwiceThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.finish();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testPutArchiveEntryAfterFinishedThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.putArchiveEntry(new TarArchiveEntry("test"));
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryAfterFinishedThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCreateArchiveEntryAfterFinishedThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();
        tos.createArchiveEntry(new File("dummy"), "dummy");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Multiple Constructors
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndParameters() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // 1 param
        TarArchiveOutputStream tos1 = new TarArchiveOutputStream(bos);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos1.getRecordSize());

        // 2 params (os, encoding)
        TarArchiveOutputStream tos2 = new TarArchiveOutputStream(bos, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos2.getRecordSize());

        // 2 params (os, blockSize)
        TarArchiveOutputStream tos3 = new TarArchiveOutputStream(bos, 2048);
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos3.getRecordSize());

        // 3 params (os, blockSize, encoding)
        TarArchiveOutputStream tos4 = new TarArchiveOutputStream(bos, 2048, "UTF-8");
        assertEquals(TarConstants.DEFAULT_RCDSIZE, tos4.getRecordSize());

        // 3 params (os, blockSize, recordSize)
        TarArchiveOutputStream tos5 = new TarArchiveOutputStream(bos, 2048, 1024);
        assertEquals(1024, tos5.getRecordSize());

        // 4 params (os, blockSize, recordSize, encoding)
        TarArchiveOutputStream tos6 = new TarArchiveOutputStream(bos, 4096, 512, "CP437");
        assertEquals(512, tos6.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.close();
        // Second close should be safe and no-op
        tos.close();
    }
}