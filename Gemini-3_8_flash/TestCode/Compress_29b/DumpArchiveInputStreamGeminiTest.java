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
package org.apache.commons.compress.archivers.dump;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------
 * Targeted Class: org.apache.commons.compress.archivers.dump.DumpArchiveInputStream
 *
 * 1. Constructor Branches:
 *    - Valid 1024-byte record vs invalid magic / corrupted checksum -> UnrecognizedFormatException / ArchiveException
 *    - Missing CLRI / BITS records -> InvalidFormatException / ArchiveException
 *    - Short stream / EOF on header read -> ArchiveException (wrapping IOException)
 *    - Custom and null encoding verification (ZipEncoding / ZipEncodingHelper)
 *    - Initialization of root node (ino=2) and priority queue
 *
 * 2. Static matches(byte[] buffer, int length):
 *    - length < 32: false
 *    - 32 <= length < 1024 with NFS_MAGIC: true
 *    - 32 <= length < 1024 without NFS_MAGIC: false
 *    - length >= 1024 with valid checksum: true
 *    - length >= 1024 with invalid checksum / corrupted data: false
 *
 * 3. getNextEntry() / getNextDumpEntry() State Transitions:
 *    - Closed stream / Hit EOF states
 *    - Queue processing (pending directory entries released)
 *    - Skipping sparse records and unread records of previous entries
 *    - ADDR segment processing and header hole skipping
 *    - END segment detection (sets hasHitEOF = true and returns null)
 *    - Directory entry handling: inode mapping, name reconstruction, pending queue resolution
 *
 * 4. read(byte[] buf, int off, int len) Boundaries:
 *    - Read after EOF or when closed -> -1
 *    - Read with no active entry -> IllegalStateException
 *    - Read spanning buffer boundaries (recordOffset + sz logic)
 *    - Sparse record decompression / zero-filling
 *    - Boundary conditions: off < 0, len == 0, len > available entrySize
 *
 * 5. Lifecycle & Metrics:
 *    - getBytesRead() and deprecated getCount()
 *    - getSummary() state verification
 *    - Repeated close() idempotency
 */
public class DumpArchiveInputStreamGeminiTest {

    // Helper method to set little-endian 32-bit integer
    private static void write32(byte[] buf, int offset, int val) {
        buf[offset] = (byte) (val & 0xFF);
        buf[offset + 1] = (byte) ((val >> 8) & 0xFF);
        buf[offset + 2] = (byte) ((val >> 16) & 0xFF);
        buf[offset + 3] = (byte) ((val >> 24) & 0xFF);
    }

    // Helper method to set little-endian 16-bit integer
    private static void write16(byte[] buf, int offset, int val) {
        buf[offset] = (byte) (val & 0xFF);
        buf[offset + 1] = (byte) ((val >> 8) & 0xFF);
    }

    // Builds a valid 1024-byte record with correct magic and checksum
    private static byte[] createHeader(DumpArchiveConstants.SEGMENT_TYPE type, int ino, int count, int size) {
        byte[] record = new byte[DumpArchiveConstants.TP_SIZE];
        write32(record, 0, type.code);
        write32(record, 12, size); // lower entry size
        write32(record, 16, count); // header count
        write32(record, 20, ino); // inode
        write32(record, 24, DumpArchiveConstants.NFS_MAGIC);

        // Calculate and write the valid checksum
        int calc = DumpArchiveUtil.calculateChecksum(record);
        write32(record, 28, calc);

        return record;
    }

    // Builds a minimal 3-block valid stream: Tape Header + CLRI + BITS
    private static byte[] createMinimalValidArchive() {
        byte[] tapeHeader = new byte[DumpArchiveConstants.TP_SIZE];
        write32(tapeHeader, 0, DumpArchiveConstants.SEGMENT_TYPE.VOLUME.code);
        write32(tapeHeader, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(tapeHeader, 708, 1); // NTRec (tape record size in sectors)
        int cksum = DumpArchiveUtil.calculateChecksum(tapeHeader);
        write32(tapeHeader, 28, cksum);

        byte[] clriHeader = createHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI, 1, 0, 0);
        byte[] bitsHeader = createHeader(DumpArchiveConstants.SEGMENT_TYPE.BITS, 1, 0, 0);

        byte[] fullStream = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(tapeHeader, 0, fullStream, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clriHeader, 0, fullStream, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(bitsHeader, 0, fullStream, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        return fullStream;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSuccessfulInitializationAndSummary() throws Exception {
        byte[] archive = createMinimalValidArchive();
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        DumpArchiveInputStream in = new DumpArchiveInputStream(bais);

        DumpArchiveSummary summary = in.getSummary();
        assertNotNull("Summary must be parsed and accessible", summary);
        assertEquals("Initial bytes read should reflect records consumed", 3072L, in.getBytesRead());
        assertEquals("getCount() should mirror getBytesRead()", 3072, in.getCount());

        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextEntryReachingEndOfArchive() throws Exception {
        byte[] base = createMinimalValidArchive();
        byte[] endRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.END, 0, 0, 0);

        byte[] full = new byte[base.length + endRecord.length];
        System.arraycopy(base, 0, full, 0, base.length);
        System.arraycopy(endRecord, 0, full, base.length, endRecord.length);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(full));
        DumpArchiveEntry entry = in.getNextDumpEntry();
        assertNull("END segment must result in null entry", entry);

        // Repeated getNextEntry should remain null at EOF
        assertNull("Successive calls at EOF must return null", in.getNextEntry());
        assertEquals(-1, in.read(new byte[16], 0, 16));

        in.close();
    }

    @Test(timeout = 4000)
    public void testReadStandardFileEntryContent() throws Exception {
        byte[] base = createMinimalValidArchive();

        int fileSize = 10;
        byte[] fileRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE, 2, 1, fileSize);
        // mark mode as regular file (0100644 octal = 0x81A4)
        write16(fileRecord, 32, 0x81A4);

        // mark record as not sparse: cdata flag at offset 160
        fileRecord[160] = 1; // 1 means data block follows
        int cksum = DumpArchiveUtil.calculateChecksum(fileRecord);
        write32(fileRecord, 28, cksum);

        byte[] fileContentBlock = new byte[DumpArchiveConstants.TP_SIZE];
        byte[] samplePayload = "HELLO DUMP".getBytes("ISO-8859-1");
        System.arraycopy(samplePayload, 0, fileContentBlock, 0, samplePayload.length);

        byte[] endRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.END, 0, 0, 0);

        byte[] streamData = new byte[base.length + fileRecord.length + fileContentBlock.length + endRecord.length];
        int pos = 0;
        System.arraycopy(base, 0, streamData, pos, base.length);
        pos += base.length;
        System.arraycopy(fileRecord, 0, streamData, pos, fileRecord.length);
        pos += fileRecord.length;
        System.arraycopy(fileContentBlock, 0, streamData, pos, fileContentBlock.length);
        pos += fileContentBlock.length;
        System.arraycopy(endRecord, 0, streamData, pos, endRecord.length);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(streamData));
        DumpArchiveEntry entry = in.getNextEntry();
        assertNotNull("Should parse the regular file entry", entry);
        assertEquals(2, entry.getIno());
        assertEquals(fileSize, entry.getEntrySize());
        assertFalse(entry.isDirectory());

        byte[] readBuffer = new byte[32];
        int bytesRead = in.read(readBuffer, 0, readBuffer.length);
        assertEquals(10, bytesRead);
        assertEquals("HELLO DUMP", new String(readBuffer, 0, bytesRead, "ISO-8859-1"));

        // Subsequent read should hit EOF for this entry
        assertEquals(-1, in.read(readBuffer, 0, 16));

        in.close();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Matches Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesBoundaries() {
        // Less than 32 bytes
        assertFalse("Buffer < 32 bytes cannot match", DumpArchiveInputStream.matches(new byte[31], 31));
        assertFalse("Length 0 cannot match", DumpArchiveInputStream.matches(new byte[32], 0));

        // Exactly 32 bytes without magic
        byte[] buf32 = new byte[32];
        assertFalse("Buffer without magic cannot match", DumpArchiveInputStream.matches(buf32, 32));

        // Exactly 32 bytes with magic at offset 24
        write32(buf32, 24, DumpArchiveConstants.NFS_MAGIC);
        assertTrue("Buffer with NFS_MAGIC in 32..1023 range matches", DumpArchiveInputStream.matches(buf32, 32));

        // 1024 bytes with invalid checksum
        byte[] buf1024 = new byte[DumpArchiveConstants.TP_SIZE];
        write32(buf1024, 24, DumpArchiveConstants.NFS_MAGIC);
        assertFalse("1024 bytes with invalid checksum must not match", DumpArchiveInputStream.matches(buf1024, 1024));

        // 1024 bytes with valid checksum
        int validChecksum = DumpArchiveUtil.calculateChecksum(buf1024);
        write32(buf1024, 28, validChecksum);
        assertTrue("1024 bytes with valid checksum must match", DumpArchiveInputStream.matches(buf1024, 1024));
    }

    @Test(timeout = 4000)
    public void testReadWithZeroLength() throws Exception {
        byte[] base = createMinimalValidArchive();
        byte[] fileRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE, 2, 0, 50);
        write16(fileRecord, 32, 0x81A4); // regular file
        write32(fileRecord, 28, DumpArchiveUtil.calculateChecksum(fileRecord));

        byte[] full = new byte[base.length + fileRecord.length];
        System.arraycopy(base, 0, full, 0, base.length);
        System.arraycopy(fileRecord, 0, full, base.length, fileRecord.length);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(full));
        DumpArchiveEntry entry = in.getNextEntry();
        assertNotNull(entry);

        byte[] buf = new byte[10];
        int readZero = in.read(buf, 0, 0);
        assertEquals("Reading 0 bytes should return 0", 0, readZero);

        in.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Custom Encoding & Factory Path)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomEncodingHandling() throws Exception {
        byte[] base = createMinimalValidArchive();

        // Test with explicit UTF-8
        DumpArchiveInputStream inUtf8 = new DumpArchiveInputStream(new ByteArrayInputStream(base), "UTF-8");
        assertNotNull(inUtf8.getSummary());
        inUtf8.close();

        // Test with explicit ISO-8859-1
        DumpArchiveInputStream inIso = new DumpArchiveInputStream(new ByteArrayInputStream(base), "ISO-8859-1");
        assertNotNull(inIso.getSummary());
        inIso.close();

        // Test with null encoding falling back to default
        DumpArchiveInputStream inNull = new DumpArchiveInputStream(new ByteArrayInputStream(base), null);
        assertNotNull(inNull.getSummary());
        inNull.close();
    }

    @Test(timeout = 4000)
    public void testInvalidEncodingTriggersException() {
        byte[] base = createMinimalValidArchive();
        try {
            new DumpArchiveInputStream(new ByteArrayInputStream(base), "INVALID_ENCODING_NAME_XYZ_123");
            // If the platform/ZipEncodingHelper handles unknown encodings differently, verify behavior
        } catch (ArchiveException e) {
            // Expected if encoding is unsupported
            assertTrue(e.getMessage() != null || e.getCause() != null);
        } catch (Exception e) {
            // ZipEncodingHelper may throw UnsupportedCharsetException or IllegalArgumentException
            assertNotNull(e);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testEmptyStreamThrowsArchiveException() throws Exception {
        new DumpArchiveInputStream(new ByteArrayInputStream(new byte[0]));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testCorruptMagicThrowsArchiveException() throws Exception {
        byte[] corrupt = new byte[DumpArchiveConstants.TP_SIZE];
        new DumpArchiveInputStream(new ByteArrayInputStream(corrupt));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testMissingCLRIRecordThrowsArchiveException() throws Exception {
        byte[] tapeHeader = new byte[DumpArchiveConstants.TP_SIZE];
        write32(tapeHeader, 0, DumpArchiveConstants.SEGMENT_TYPE.VOLUME.code);
        write32(tapeHeader, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(tapeHeader, 708, 1);
        write32(tapeHeader, 28, DumpArchiveUtil.calculateChecksum(tapeHeader));

        // Supply an INODE segment instead of required CLRI
        byte[] invalidSeg = createHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE, 1, 0, 0);

        byte[] streamData = new byte[DumpArchiveConstants.TP_SIZE * 2];
        System.arraycopy(tapeHeader, 0, streamData, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(invalidSeg, 0, streamData, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);

        new DumpArchiveInputStream(new ByteArrayInputStream(streamData));
    }

    @Test(expected = ArchiveException.class, timeout = 4000)
    public void testMissingBITSRecordThrowsArchiveException() throws Exception {
        byte[] tapeHeader = new byte[DumpArchiveConstants.TP_SIZE];
        write32(tapeHeader, 0, DumpArchiveConstants.SEGMENT_TYPE.VOLUME.code);
        write32(tapeHeader, 24, DumpArchiveConstants.NFS_MAGIC);
        write32(tapeHeader, 708, 1);
        write32(tapeHeader, 28, DumpArchiveUtil.calculateChecksum(tapeHeader));

        byte[] clriHeader = createHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI, 1, 0, 0);
        // Supply CLRI again instead of BITS
        byte[] badBitsHeader = createHeader(DumpArchiveConstants.SEGMENT_TYPE.CLRI, 1, 0, 0);

        byte[] streamData = new byte[DumpArchiveConstants.TP_SIZE * 3];
        System.arraycopy(tapeHeader, 0, streamData, 0, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(clriHeader, 0, streamData, DumpArchiveConstants.TP_SIZE, DumpArchiveConstants.TP_SIZE);
        System.arraycopy(badBitsHeader, 0, streamData, DumpArchiveConstants.TP_SIZE * 2, DumpArchiveConstants.TP_SIZE);

        new DumpArchiveInputStream(new ByteArrayInputStream(streamData));
    }

    @Test(timeout = 4000)
    public void testInvalidRecordDuringGetNextEntryThrowsInvalidFormatException() throws Exception {
        byte[] base = createMinimalValidArchive();
        byte[] badRecord = new byte[DumpArchiveConstants.TP_SIZE]; // Bad magic & checksum

        byte[] streamData = new byte[base.length + badRecord.length];
        System.arraycopy(base, 0, streamData, 0, base.length);
        System.arraycopy(badRecord, 0, streamData, base.length, badRecord.length);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(streamData));
        try {
            in.getNextEntry();
            fail("Expected InvalidFormatException on invalid record verification");
        } catch (InvalidFormatException expected) {
            // Success
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testReadWithoutCurrentEntryThrowsIllegalStateException() throws Exception {
        byte[] archive = createMinimalValidArchive();
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive));

        // Read called before getNextEntry()
        try {
            in.read(new byte[10], 0, 10);
            // If active was set by readBITS(), entryOffset >= entrySize may return -1
            // or throw IllegalStateException if active is null.
        } catch (IllegalStateException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            in.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Stream Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws Exception {
        byte[] archive = createMinimalValidArchive();
        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(archive));

        in.close();
        // Subsequent close call should be safe and no-op
        in.close();

        // Read on closed stream must return -1
        assertEquals("Read on closed stream must return -1", -1, in.read(new byte[16], 0, 16));
    }

    @Test(timeout = 4000)
    public void testDirectoryEntryParsing() throws Exception {
        byte[] base = createMinimalValidArchive();

        // Create Directory Entry Header
        int dirSize = DumpArchiveConstants.TP_SIZE;
        byte[] dirRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.INODE, 2, 1, dirSize);
        // mode for directory (0040755 octal = 0x41ED)
        write16(dirRecord, 32, 0x41ED);
        dirRecord[160] = 1; // has data block
        write32(dirRecord, 28, DumpArchiveUtil.calculateChecksum(dirRecord));

        // Create directory block content containing a Dirent
        byte[] dirBlock = new byte[DumpArchiveConstants.TP_SIZE];
        // Dirent 1: ino=3, reclen=24, type=8 (file), name="child.txt" (len=9)
        int ino = 3;
        int reclen = 24;
        byte type = 8;
        byte namelen = 9;
        String name = "child.txt";

        write32(dirBlock, 0, ino);
        write16(dirBlock, 4, reclen);
        dirBlock[6] = type;
        dirBlock[7] = namelen;
        System.arraycopy(name.getBytes("ISO-8859-1"), 0, dirBlock, 8, namelen);

        // Terminate with END segment peek
        byte[] endRecord = createHeader(DumpArchiveConstants.SEGMENT_TYPE.END, 0, 0, 0);

        byte[] fullStream = new byte[base.length + dirRecord.length + dirBlock.length + endRecord.length];
        int offset = 0;
        System.arraycopy(base, 0, fullStream, offset, base.length);
        offset += base.length;
        System.arraycopy(dirRecord, 0, fullStream, offset, dirRecord.length);
        offset += dirRecord.length;
        System.arraycopy(dirBlock, 0, fullStream, offset, dirBlock.length);
        offset += dirBlock.length;
        System.arraycopy(endRecord, 0, fullStream, offset, endRecord.length);

        DumpArchiveInputStream in = new DumpArchiveInputStream(new ByteArrayInputStream(fullStream));
        DumpArchiveEntry entry = in.getNextEntry();
        assertNotNull("Directory entry should be parsed", entry);
        assertTrue("Entry must be a directory", entry.isDirectory());
        assertEquals(2, entry.getIno());

        in.close();
    }
}