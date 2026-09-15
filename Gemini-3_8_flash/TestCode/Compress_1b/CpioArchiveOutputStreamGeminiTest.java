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
package org.apache.commons.compress.archivers.cpio;

import java.io.*;
import java.util.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * TARGET CLASS:
 *   org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream
 *
 * COVERAGE & BRANCH MATRIX:
 * 1. Constructor & Format Validation:
 *    - CpioArchiveOutputStream(OutputStream) -> default FORMAT_NEW
 *    - CpioArchiveOutputStream(OutputStream, short):
 *      * Valid: FORMAT_NEW, FORMAT_NEW_CRC, FORMAT_OLD_ASCII, FORMAT_OLD_BINARY
 *      * Invalid: unknown format -> IllegalArgumentException("Unknown header type")
 * 2. ensureOpen() Guard:
 *    - Open stream: operations proceed normally
 *    - Closed stream: putNextEntry, closeArchiveEntry, write, finish -> IOException("Stream closed")
 * 3. Entry Initialization & Automatic Transitions (putNextEntry / putArchiveEntry):
 *    - Current entry open when putting next entry -> triggers closeArchiveEntry() automatically
 *    - Entry modification time == -1 -> assigns current time System.currentTimeMillis()
 *    - Entry format == -1 -> assigns output stream's default entryFormat
 *    - Duplicate entry name detection -> IOException("duplicate entry: ...")
 *    - ArchiveEntry polymorphism via putArchiveEntry((ArchiveEntry) e)
 * 4. Header Generation & Writing (writeHeader / writeNewEntry / writeOldAsciiEntry / writeOldBinaryEntry):
 *    - FORMAT_NEW: MAGIC_NEW, hex ASCII headers (radix 16), 4-byte header+name padding
 *    - FORMAT_NEW_CRC: MAGIC_NEW_CRC, hex ASCII headers (radix 16), 4-byte header+name padding
 *    - FORMAT_OLD_ASCII: MAGIC_OLD_ASCII, octal ASCII headers (radix 8), unpadded
 *    - FORMAT_OLD_BINARY: MAGIC_OLD_BINARY byte swap, binary big/little endian words, 2-byte header+name padding
 *    - writeAsciiLong branch: number representation <= length (leading zero padding) vs > length (truncation/substring)
 * 5. Data Writing & Validation (write):
 *    - write(int b): writes single byte
 *    - write(byte[], off, len):
 *      * off < 0, len < 0, off > b.length - len -> IndexOutOfBoundsException
 *      * len == 0 -> early return (no-op)
 *      * cpioEntry == null -> IOException("no current CPIO entry")
 *      * written + len > cpioEntry.getSize() -> IOException("attempt to write past end of STORED entry")
 *      * Checksum calculation when format == FORMAT_NEW_CRC (CRC byte accumulation)
 * 6. Closing Entry & Padding (closeArchiveEntry):
 *    - written != size -> IOException("invalid entry size ...")
 *    - Pad alignment: FORMAT_NEW_MASK (4 bytes), FORMAT_OLD_BINARY (2 bytes), FORMAT_OLD_ASCII (none)
 *    - Checksum check: FORMAT_NEW_CRC crc != entry.getChksum() -> IOException("CRC Error")
 * 7. Finish & Close Lifecycle:
 *    - finish():
 *      * finishes active entry if cpioEntry != null
 *      * writes "TRAILER!!!" entry (mode 0, links 1)
 *      * idempotent if already finished
 *    - close(): closes underlying stream, marks closed = true, idempotent on repeat calls
 *
 * DEFECT SPECIFICATION TARGET (COMPRESS-28 / Defects4J Compress-1):
 *    - Calling close() directly on CpioArchiveOutputStream without calling finish() first
 *      fails to write the mandatory "TRAILER!!!" entry.
 *    - When read back with CpioArchiveInputStream, reader encounters premature EOF
 *      and throws java.io.EOFException instead of clean end of archive (returning null).
 */
public class CpioArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Archive Formats
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorUsesFormatNew() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("test.txt");
        assertEquals(-1, entry.getFormat());
        entry.setSize(0);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] data = baos.toByteArray();
        // FORMAT_NEW magic is "070701"
        assertTrue(data.length >= 6);
        assertEquals('0', (char) data[0]);
        assertEquals('7', (char) data[1]);
        assertEquals('0', (char) data[2]);
        assertEquals('7', (char) data[3]);
        assertEquals('0', (char) data[4]);
        assertEquals('1', (char) data[5]);
    }

    @Test(timeout = 4000)
    public void testFormatOldAsciiCreationAndReadBack() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "ascii_entry.txt", 5);
        entry.setTime(12345678L);
        entry.setDevice(1);
        entry.setInode(2);
        entry.setMode(0100644);
        entry.setUID(1000);
        entry.setGID(1000);
        entry.setNumberOfLinks(1);
        entry.setRemoteDevice(0);

        out.putNextEntry(entry);
        byte[] payload = "Hello".getBytes("US-ASCII");
        out.write(payload, 0, payload.length);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] raw = baos.toByteArray();
        // FORMAT_OLD_ASCII magic is "070707"
        assertEquals('0', (char) raw[0]);
        assertEquals('7', (char) raw[1]);
        assertEquals('0', (char) raw[2]);
        assertEquals('7', (char) raw[3]);
        assertEquals('0', (char) raw[4]);
        assertEquals('7', (char) raw[5]);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(raw));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("ascii_entry.txt", readEntry.getName());
        assertEquals(5, readEntry.getSize());
        byte[] buf = new byte[5];
        assertEquals(5, in.read(buf));
        assertArrayEquals(payload, buf);
        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testFormatOldBinaryCreationAndReadBack() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "bin.dat", 3);
        entry.setTime(987654321L);
        out.putNextEntry(entry);
        out.write(new byte[]{10, 20, 30}, 0, 3);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] raw = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(raw));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("bin.dat", readEntry.getName());
        assertEquals(3, readEntry.getSize());
        byte[] buf = new byte[3];
        assertEquals(3, in.read(buf));
        assertArrayEquals(new byte[]{10, 20, 30}, buf);
        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatThrowsException() {
        new CpioArchiveOutputStream(new ByteArrayOutputStream(), (short) 999);
    }

    // =========================================================================
    // Partition B: Entry Writing, Data Alignment & CRC
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatNewCrcValidChecksum() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        byte[] data = new byte[]{1, 2, 3, 4, 5};
        long expectedCrc = 0;
        for (byte b : data) {
            expectedCrc += (b & 0xFF);
        }

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_valid.bin", data.length);
        entry.setChksum(expectedCrc);
        out.putNextEntry(entry);
        out.write(data, 0, data.length);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(readEntry);
        assertEquals("crc_valid.bin", readEntry.getName());
        assertEquals(expectedCrc, readEntry.getChksum());
        in.close();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFormatNewCrcMismatchThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        byte[] data = new byte[]{1, 2, 3, 4, 5};
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_invalid.bin", data.length);
        entry.setChksum(999999L); // Wrong CRC
        out.putNextEntry(entry);
        out.write(data, 0, data.length);
        out.closeArchiveEntry(); // Expected to throw "CRC Error"
    }

    @Test(timeout = 4000)
    public void testAutoClosingPreviousEntryOnPutNextEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry e1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt", 2);
        out.putNextEntry(e1);
        out.write(new byte[]{1, 2});

        // Put next entry without explicitly closing e1
        CpioArchiveEntry e2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt", 3);
        out.putNextEntry(e2);
        out.write(new byte[]{3, 4, 5});
        out.closeArchiveEntry();
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry r1 = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(r1);
        assertEquals("file1.txt", r1.getName());
        assertEquals(2, r1.getSize());

        CpioArchiveEntry r2 = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(r2);
        assertEquals("file2.txt", r2.getName());
        assertEquals(3, r2.getSize());

        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryPolymorphism() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        ArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "poly.txt", 1);
        out.putArchiveEntry(entry);
        out.write(new byte[]{65});
        out.closeArchiveEntry();
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ArchiveEntry read = in.getNextEntry();
        assertNotNull(read);
        assertEquals("poly.txt", read.getName());
        in.close();
    }

    @Test(timeout = 4000)
    public void testWriteSingleByteMethod() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        out.write(0xFE); // Direct stream byte write
        assertEquals(0xFE, baos.toByteArray()[0] & 0xFF);
    }

    @Test(timeout = 4000)
    public void testWriteZeroLengthNoOp() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        // write with len == 0 should not throw even if no current entry
        out.write(new byte[10], 0, 0);
    }

    @Test(timeout = 4000)
    public void testAsciiLongTruncationBranch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        // Max 8 hex characters: 0xFFFFFFFFL (8 chars).
        // A value >= 0x100000000L produces 9+ hex chars, hitting the length truncation branch in writeAsciiLong
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "large_val.txt", 0);
        entry.setInode(0x123456789ABCDEFL);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        assertTrue(baos.size() > 0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-28 / Compress-1)
    // =========================================================================

    /**
     * TARGET DEFECT TEST:
     * When closing CpioArchiveOutputStream directly via close() WITHOUT finish(),
     * the archive must write the TRAILER!!! entry so readers do not encounter EOFException.
     */
    @Test(timeout = 4000)
    public void testCloseWithoutFinish_WritesTrailer_COMPRESS28() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});
        out.closeArchiveEntry();

        // Close directly without finish()
        out.close();

        // Read back with CpioArchiveInputStream
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry readEntry = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull("Entry should be readable", readEntry);
        assertEquals("test.txt", readEntry.getName());

        byte[] content = new byte[4];
        int readBytes = in.read(content);
        assertEquals(4, readBytes);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, content);

        // This call MUST return null gracefully instead of throwing java.io.EOFException
        ArchiveEntry endOfArchive = in.getNextEntry();
        assertNull("Reader should cleanly encounter trailer and return null", endOfArchive);
        in.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testDuplicateEntryNameThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry e1 = new CpioArchiveEntry("duplicate.txt");
        e1.setSize(0);
        out.putNextEntry(e1);
        out.closeArchiveEntry();

        CpioArchiveEntry e2 = new CpioArchiveEntry("duplicate.txt");
        e2.setSize(0);
        out.putNextEntry(e2); // Must throw IOException("duplicate entry: duplicate.txt")
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testWriteWithoutEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        out.write(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testWritePastDeclaredSizeThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("bounds.txt");
        entry.setSize(2);
        out.putNextEntry(entry);
        out.write(new byte[]{1, 2, 3}, 0, 3); // 3 > 2
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryWithUnderflowThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("underflow.txt");
        entry.setSize(10);
        out.putNextEntry(entry);
        out.write(new byte[]{1, 2, 3}, 0, 3); // wrote 3 out of 10
        out.closeArchiveEntry(); // Expected: invalid entry size
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteNegativeOffsetThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteNegativeLengthThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteOffsetPlusLengthOverflowThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[10], 8, 5);
    }

    @Test(timeout = 4000)
    public void testOperationsAfterCloseThrowIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.close();

        try {
            out.putNextEntry(new CpioArchiveEntry("test"));
            fail("putNextEntry should fail on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        try {
            out.write(new byte[]{1, 2, 3}, 0, 3);
            fail("write should fail on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        try {
            out.closeArchiveEntry();
            fail("closeArchiveEntry should fail on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        try {
            out.finish();
            fail("finish should fail on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Stream Lifecycle & Idempotency
    // =========================================================================

    @Test(timeout = 4000)
    public void testFinishWithOpenEntryClosesItFirst() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry("file.txt");
        entry.setSize(4);
        out.putNextEntry(entry);
        out.write(new byte[]{1, 2, 3, 4});

        // finish() should close current active entry automatically
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        CpioArchiveEntry read = (CpioArchiveEntry) in.getNextEntry();
        assertNotNull(read);
        assertEquals("file.txt", read.getName());
        assertEquals(4, read.getSize());
        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testMultipleCloseCallsAreIdempotent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();
        out.close(); // Second close should be a no-op
    }

    @Test(timeout = 4000)
    public void testEntryPaddingAlignment() throws IOException {
        // Test padding for byte alignments (1, 2, 3, 4 byte content)
        for (int size = 1; size <= 4; size++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "pad" + size, size);
            out.putNextEntry(entry);
            byte[] payload = new byte[size];
            Arrays.fill(payload, (byte) 0xAA);
            out.write(payload);
            out.closeArchiveEntry();
            out.finish();
            out.close();

            CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
            CpioArchiveEntry read = (CpioArchiveEntry) in.getNextEntry();
            assertNotNull(read);
            assertEquals("pad" + size, read.getName());
            assertEquals(size, read.getSize());
            byte[] readBuf = new byte[size];
            assertEquals(size, in.read(readBuf));
            assertArrayEquals(payload, readBuf);
            assertNull(in.getNextEntry());
            in.close();
        }
    }
}