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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor Overloads: (os), (os, blockSize), (os, blockSize, recordSize) - TarBuffer config verification.
 * 2. setLongFileMode: LONGFILE_ERROR (0), LONGFILE_TRUNCATE (1), LONGFILE_GNU (2).
 * 3. putArchiveEntry:
 *    - finished == true -> throws IOException
 *    - non-TarArchiveEntry argument -> ClassCastException
 *    - entry.getName().length() >= 100 vs < 100
 *    - mode == GNU -> write longLinkEntry recursively, write nameBytes, NUL terminator, closeArchiveEntry
 *    - mode == TRUNCATE -> truncates name silently
 *    - mode == ERROR -> throws RuntimeException("file name '...' is too long")
 *    - entry.isDirectory() == true (currSize = 0) vs false (currSize = entry.getSize())
 * 4. closeArchiveEntry:
 *    - finished == true -> throws IOException
 *    - !haveUnclosedEntry -> throws IOException
 *    - assemLen > 0 -> pad record with 0s, writeRecord, update currBytes, reset assemLen
 *    - currBytes < currSize -> throws premature close IOException
 * 5. write(byte[], int, int):
 *    - (currBytes + numToWrite) > currSize -> throws size exceeded IOException
 *    - assemLen > 0 && (assemLen + numToWrite) >= recordBuf.length (fill existing record and write)
 *    - assemLen > 0 && (assemLen + numToWrite) < recordBuf.length (partially append to assemble buffer)
 *    - while (numToWrite > 0):
 *        - numToWrite < recordBuf.length (buffer remainder into assemBuf and break)
 *        - numToWrite >= recordBuf.length (direct write to buffer)
 * 6. finish():
 *    - finished == true -> throws IOException
 *    - haveUnclosedEntry == true -> throws IOException
 *    - writeEOFRecord() called twice, buffer.flushBlock(), finished set to true
 * 7. close():
 *    - idempotent on multiple invocations
 *    - triggers finish() if not already finished
 * 8. Defect Target (Defects4J - TarArchiveOutputStreamTest::testCount):
 *    - getBytesWritten() / getCount() must accurately report the full bytes written (including
 *      tar buffer block padding of 10240 bytes) rather than merely inner data bytes.
 */
public class TarArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardFileWriteAndLifecycle() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        byte[] content = new byte[] { 1, 2, 3, 4, 5 };
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.finish();
        tos.close();

        byte[] tarBytes = bos.toByteArray();
        assertTrue("Archive should have at least one block size", tarBytes.length >= TarBuffer.DEFAULT_BLKSIZE);
        assertEquals("Tar buffer default record size should be 512", 512, tos.getRecordSize());
    }

    @Test(timeout = 4000)
    public void testDirectoryEntryHandling() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry dirEntry = new TarArchiveEntry("mydir/");
        dirEntry.setSize(1024); // Intentional non-zero size to verify directory override
        assertTrue("Entry must report directory", dirEntry.isDirectory());

        tos.putArchiveEntry(dirEntry);
        // Closing immediately must succeed because directory forces currSize = 0
        tos.closeArchiveEntry();
        tos.close();

        assertTrue("Output should be flushed to block size", bos.toByteArray().length >= TarBuffer.DEFAULT_BLKSIZE);
    }

    @Test(timeout = 4000)
    public void testSingleByteWriteDelegation() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("single.bin");
        entry.setSize(1);
        tos.putArchiveEntry(entry);
        tos.write(0x7F);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testAssemblyBufferSmallChunksFillAndPad() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int totalSize = 600;
        TarArchiveEntry entry = new TarArchiveEntry("chunked.dat");
        entry.setSize(totalSize);
        tos.putArchiveEntry(entry);

        // First write: 300 bytes (< 512, buffered into assemBuf)
        byte[] chunk1 = new byte[300];
        tos.write(chunk1);

        // Second write: 300 bytes (assemLen 300 + 300 = 600 >= 512, completes record and leaves 88)
        byte[] chunk2 = new byte[300];
        tos.write(chunk2);

        tos.closeArchiveEntry(); // assemLen = 88 flushed and padded
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testAssemblyBufferMultipleSubRecordWrites() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("subchunks.dat");
        entry.setSize(150);
        tos.putArchiveEntry(entry);

        // Three writes of 50 bytes each (50 < 512, 100 < 512, 150 < 512)
        tos.write(new byte[50]);
        tos.write(new byte[50]);
        tos.write(new byte[50]);

        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testDirectMultiRecordWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        int totalSize = 2048; // Exactly 4 records (4 * 512)
        TarArchiveEntry entry = new TarArchiveEntry("multirecord.dat");
        entry.setSize(totalSize);

        tos.putArchiveEntry(entry);
        tos.write(new byte[totalSize]);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testFlushDelegation() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.flush(); // Verify no exception when flushing
        tos.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroByteWrite() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("empty.txt");
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.write(new byte[0], 0, 0);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testFileNameBoundaryAt99Characters() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        // 99 characters (< TarConstants.NAMELEN = 100), default LONGFILE_ERROR must NOT trigger
        StringBuilder name99 = new StringBuilder();
        for (int i = 0; i < 99; i++) {
            name99.append('a');
        }

        TarArchiveEntry entry = new TarArchiveEntry(name99.toString());
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testFileNameBoundaryAt100CharactersWithTruncate() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_TRUNCATE);

        // 100 characters (>= TarConstants.NAMELEN = 100)
        StringBuilder name100 = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            name100.append('b');
        }

        TarArchiveEntry entry = new TarArchiveEntry(name100.toString());
        entry.setSize(0);
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testFileNameBoundaryAt100CharactersWithGNU() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            longName.append('c');
        }

        TarArchiveEntry entry = new TarArchiveEntry(longName.toString());
        byte[] content = new byte[] { 10, 20, 30 };
        entry.setSize(content.length);

        tos.putArchiveEntry(entry);
        tos.write(content);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }

    @Test(timeout = 4000)
    public void testCustomBlockAndRecordSizes() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int customBlockSize = 2048;
        int customRecordSize = 1024;
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, customBlockSize, customRecordSize);

        assertEquals(customRecordSize, tos.getRecordSize());

        TarArchiveEntry entry = new TarArchiveEntry("custom.dat");
        entry.setSize(customRecordSize);
        tos.putArchiveEntry(entry);
        tos.write(new byte[customRecordSize]);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals(customBlockSize, bos.size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockSizeOnly() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos, 2048);
        assertEquals(TarBuffer.DEFAULT_RCDSIZE, tos.getRecordSize());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * org.apache.commons.compress.archivers.tar.TarArchiveOutputStreamTest::testCount
     * --> junit.framework.AssertionFailedError: expected:<10240> but was:<76>
     *
     * The TarArchiveOutputStream must return total bytes written to the underlying stream
     * (accounting for tar EOF records and block padding up to 10240 bytes) via getBytesWritten().
     */
    @Test(timeout = 4000)
    public void testCountReflectsTotalBytesWrittenIncludingTarPadding() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("test1.xml");
        byte[] data = new byte[76];
        entry.setSize(data.length);

        tos.putArchiveEntry(entry);
        tos.write(data, 0, data.length);
        tos.closeArchiveEntry();
        tos.close();

        assertEquals("getBytesWritten() must reflect the padded tar block size", 10240L, tos.getBytesWritten());
        assertEquals("getCount() must reflect the padded tar block size as integer", 10240, tos.getCount());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutArchiveEntryLongFileNameThrowsExceptionInErrorMode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_ERROR);

        StringBuilder name100 = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            name100.append('x');
        }

        TarArchiveEntry entry = new TarArchiveEntry(name100.toString());
        entry.setSize(0);

        try {
            tos.putArchiveEntry(entry);
            fail("Expected RuntimeException for file name >= 100 characters in LONGFILE_ERROR mode");
        } catch (RuntimeException expected) {
            assertTrue("Exception message should identify long file name",
                    expected.getMessage().contains("is too long"));
        }
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testPutArchiveEntryIncompatibleEntryTypeThrowsClassCast() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        ArchiveEntry foreignEntry = new ArchiveEntry() {
            @Override
            public String getName() {
                return "foreign";
            }

            @Override
            public long getSize() {
                return 0;
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public java.util.Date getLastModifiedDate() {
                return new java.util.Date();
            }
        };

        tos.putArchiveEntry(foreignEntry);
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryAfterFinishThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();

        try {
            tos.putArchiveEntry(new TarArchiveEntry("entry.txt"));
            fail("Expected IOException when putArchiveEntry called after finish");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Stream has already been finished"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryWithoutActiveEntryThrowsIOException() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        try {
            tos.closeArchiveEntry();
            fail("Expected IOException when closeArchiveEntry called without active entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("No current entry to close"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryAfterFinishThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();

        try {
            tos.closeArchiveEntry();
            fail("Expected IOException when closeArchiveEntry called after finish");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Stream has already been finished"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryPrematurelyThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("premature.dat");
        entry.setSize(100);
        tos.putArchiveEntry(entry);
        tos.write(new byte[50]); // Only wrote 50 out of 100 bytes

        try {
            tos.closeArchiveEntry();
            fail("Expected IOException when closing entry before writing all declared bytes");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("closed at '50' before the '100' bytes specified"));
        }
    }

    @Test(timeout = 4000)
    public void testWriteExceedingDeclaredSizeThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("overflow.dat");
        entry.setSize(10);
        tos.putArchiveEntry(entry);

        try {
            tos.write(new byte[11]);
            fail("Expected IOException when writing more bytes than declared size");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("exceeds size in header"));
        }
    }

    @Test(timeout = 4000)
    public void testFinishWithUnclosedEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        TarArchiveEntry entry = new TarArchiveEntry("unclosed.dat");
        entry.setSize(0);
        tos.putArchiveEntry(entry);

        try {
            tos.finish();
            fail("Expected IOException when finish called with unclosed entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("This archives contains unclosed entries."));
        }
    }

    @Test(timeout = 4000)
    public void testFinishTwiceThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();

        try {
            tos.finish();
            fail("Expected IOException on subsequent finish() call");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("This archive has already been finished"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryAfterFinishThrowsIOException() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);
        tos.finish();

        File tempFile = File.createTempFile("test_tar_entry", ".tmp");
        try {
            tos.createArchiveEntry(tempFile, "tempEntry");
            fail("Expected IOException when createArchiveEntry called after finish");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Stream has already been finished"));
        } finally {
            tempFile.delete();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateArchiveEntrySuccess() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        File tempFile = File.createTempFile("test_create", ".tmp");
        try {
            ArchiveEntry entry = tos.createArchiveEntry(tempFile, "createdEntry");
            assertNotNull("Created archive entry must not be null", entry);
            assertTrue("Should create a TarArchiveEntry instance", entry instanceof TarArchiveEntry);
            assertEquals("createdEntry", entry.getName());
        } finally {
            tempFile.delete();
            tos.close();
        }
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        TarArchiveOutputStream tos = new TarArchiveOutputStream(bos);

        tos.close();
        // Subsequent calls to close must be safe and idempotent
        tos.close();
        tos.close();

        assertEquals(TarBuffer.DEFAULT_BLKSIZE, bos.size());
    }
}