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
package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: ZipArchiveInputStream
 *
 * Branches & Execution Paths Targeted:
 * 1. matches(byte[] signature, int length):
 *    - length < LFH_SIG.length (returns false)
 *    - checksig matches LFH_SIG (returns true)
 *    - checksig matches EOCD_SIG (returns true)
 *    - checksig mismatch for both (returns false)
 *
 * 2. getNextZipEntry() / getNextEntry():
 *    - closed == true (returns null)
 *    - hitCentralDirectory == true (returns null)
 *    - current != null (triggers closeEntry() then proceeds)
 *    - readFully throws EOFException (short stream / 0 bytes, returns null)
 *    - signature == CFH_SIG (sets hitCentralDirectory = true, returns null)
 *    - signature != LFH_SIG (returns null)
 *    - generalPurposeFlag with EFS (UTF-8 encoding forced) vs without EFS (custom zipEncoding)
 *    - generalPurposeFlag with Data Descriptor (bit 3 set: crc/sizes skipped in LFH) vs without
 *    - useUnicodeExtraFields true + non-EFS (Unicode Path extra field 0x7075 processed)
 *
 * 3. read(byte[] buffer, int start, int length):
 *    - closed == true -> IOException("The stream is closed")
 *    - current == null || inf.finished() -> returns -1
 *    - bounds check failures (start < 0, length < 0, start > buffer.length, start + length > buffer.length)
 *      -> ArrayIndexOutOfBoundsException
 *    - STORED entry:
 *      * readBytesOfEntry >= csize -> returns -1
 *      * offsetInBuffer >= lengthOfLastRead (fills buffer from stream)
 *      * stream EOF reached mid-entry -> returns -1
 *      * chunked reads & buffer boundary alignment
 *    - DEFLATED entry:
 *      * inf.needsInput() triggers fill()
 *      * DataFormatException during inflate() -> rethrown as ZipException
 *      * read == 0 && inf.finished() -> returns -1
 *      * successful read updates CRC32 and returns positive count
 *
 * 4. skip(long value):
 *    - value < 0 -> IllegalArgumentException
 *    - value == 0 -> returns 0
 *    - value > 0 -> skips requested bytes across 1024-byte chunks or until EOF
 *
 * 5. close() / closeEntry():
 *    - Multiple close() calls (idempotency)
 *    - closeEntry() when DEFLATED (computes inf.getTotalIn()) vs STORED (readBytesOfEntry)
 *    - Pushback of over-read bytes: bytesReadFromStream - inB > 0
 *    - hasDataDescriptor == true -> reads 16-byte data descriptor
 *
 * 6. Defect Targeted:
 *    - Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForStream
 *      "shouldn't be able to read from truncated entry"
 *      When an entry's deflated stream is truncated unexpectedly, read() must not silently
 *      return 0 or exit cleanly; it must throw an IOException indicating truncated data.
 */
public class ZipArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadStoredEntryFully() throws IOException {
        byte[] payload = "Hello Apache Commons Compress Stored".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("stored.txt", payload, ZipArchiveOutputStream.STORED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("stored.txt", entry.getName());
            assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
            assertEquals(payload.length, entry.getSize());

            byte[] out = new byte[payload.length];
            int totalRead = 0;
            int r;
            while ((r = zis.read(out, totalRead, out.length - totalRead)) != -1) {
                totalRead += r;
            }
            assertEquals(payload.length, totalRead);
            assertArrayEquals(payload, out);

            // Subsequent read returns -1
            assertEquals(-1, zis.read(out, 0, 1));
            // No more entries
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntryFully() throws IOException {
        byte[] payload = "Deflated content to compress repeated repeated repeated".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("deflated.txt", payload, ZipArchiveOutputStream.DEFLATED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("deflated.txt", entry.getName());
            assertEquals(ZipArchiveOutputStream.DEFLATED, entry.getMethod());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[16];
            int read;
            while ((read = zis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, read);
            }
            assertArrayEquals(payload, baos.toByteArray());
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadMultipleEntriesSequentially() throws IOException {
        byte[] data1 = "First Entry Content".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Second Entry Content".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(createEntryBytes("file1.txt", data1, ZipArchiveOutputStream.STORED, false));
        baos.write(createEntryBytes("file2.txt", data2, ZipArchiveOutputStream.DEFLATED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("file1.txt", e1.getName());
            byte[] b1 = new byte[data1.length];
            int r1 = zis.read(b1, 0, b1.length);
            assertEquals(data1.length, r1);
            assertArrayEquals(data1, b1);

            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("file2.txt", e2.getName());
            byte[] b2 = new byte[data2.length];
            int r2 = zis.read(b2, 0, b2.length);
            assertEquals(data2.length, r2);
            assertArrayEquals(data2, b2);

            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testGetNextEntryInterfaceMethod() throws IOException {
        byte[] payload = "Testing ArchiveEntry alias".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("alias.txt", payload, ZipArchiveOutputStream.STORED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ArchiveEntry entry = zis.getNextEntry();
            assertNotNull(entry);
            assertEquals("alias.txt", entry.getName());
            assertFalse(entry.isDirectory());
        }
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntryAndAcrossBoundary() throws IOException {
        byte[] payload = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("skip.txt", payload, ZipArchiveOutputStream.STORED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            assertNotNull(zis.getNextZipEntry());
            assertEquals(0, zis.skip(0));

            long skipped = zis.skip(5);
            assertEquals(5, skipped);

            byte[] rem = new byte[11];
            int read = zis.read(rem, 0, rem.length);
            assertEquals(11, read);
            assertEquals("56789ABCDEF", new String(rem, StandardCharsets.UTF_8));

            // Skip past EOF of current entry
            long skippedAfterEof = zis.skip(10);
            assertEquals(0, skippedAfterEof);
        }
    }

    @Test(timeout = 4000)
    public void testCloseEntryWithoutReadingPayload() throws IOException {
        byte[] data1 = "Unread Content 1".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "Read Content 2".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(createEntryBytes("skipMe.txt", data1, ZipArchiveOutputStream.DEFLATED, false));
        baos.write(createEntryBytes("readMe.txt", data2, ZipArchiveOutputStream.STORED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry e1 = zis.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("skipMe.txt", e1.getName());

            // Move directly to next entry without reading e1
            ZipArchiveEntry e2 = zis.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("readMe.txt", e2.getName());

            byte[] b2 = new byte[data2.length];
            int r = zis.read(b2, 0, b2.length);
            assertEquals(data2.length, r);
            assertArrayEquals(data2, b2);
        }
    }

    @Test(timeout = 4000)
    public void testDataDescriptorFlagProcessing() throws IOException {
        byte[] payload = "Payload with data descriptor".getBytes(StandardCharsets.UTF_8);
        byte[] entryBytes = createEntryBytes("desc.txt", payload, ZipArchiveOutputStream.STORED, true);

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(entryBytes))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("desc.txt", entry.getName());

            byte[] readBuf = new byte[payload.length];
            int r = zis.read(readBuf, 0, readBuf.length);
            assertEquals(payload.length, r);
            assertArrayEquals(payload, readBuf);

            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testUnicodeExtraFieldSetsName() throws IOException {
        String asciiName = "ascii.txt";
        String unicodeName = "ünïcöde.txt";
        byte[] payload = "Unicode test".getBytes(StandardCharsets.UTF_8);

        byte[] originalNameBytes = asciiName.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(originalNameBytes);
        long nameCrc = crc.getValue();

        byte[] uBytes = unicodeName.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream extraBaos = new ByteArrayOutputStream();
        writeShort(extraBaos, 0x7075); // InfoZIP Unicode Path Header ID
        writeShort(extraBaos, 1 + 4 + uBytes.length);
        extraBaos.write(1); // version
        writeInt(extraBaos, nameCrc);
        extraBaos.write(uBytes);

        byte[] entryBytes = createEntryBytesWithExtra(asciiName, payload, ZipArchiveOutputStream.STORED,
                0, extraBaos.toByteArray());

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(entryBytes), "US-ASCII", true)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(unicodeName, entry.getName());
        }
    }

    @Test(timeout = 4000)
    public void testEfsFlagForcesUtf8Encoding() throws IOException {
        String utf8Name = "тест.txt";
        byte[] payload = "EFS test".getBytes(StandardCharsets.UTF_8);
        int efsFlag = ZipArchiveOutputStream.EFS_FLAG;

        byte[] entryBytes = createEntryBytesWithFlags(utf8Name, payload, ZipArchiveOutputStream.STORED, efsFlag);

        // Supply a non-UTF8 encoding in constructor; EFS flag should force UTF-8
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(entryBytes), "ISO-8859-1", false)) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(utf8Name, entry.getName());
        }
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMatchesSignatureBoundaries() {
        assertFalse(ZipArchiveInputStream.matches(new byte[0], 0));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b}, 2));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x03}, 3));

        byte[] lfhSig = new byte[]{0x50, 0x4b, 0x03, 0x04};
        assertTrue(ZipArchiveInputStream.matches(lfhSig, 4));

        byte[] eocdSig = new byte[]{0x50, 0x4b, 0x05, 0x06};
        assertTrue(ZipArchiveInputStream.matches(eocdSig, 4));

        byte[] largerWithLfh = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x00, 0x00};
        assertTrue(ZipArchiveInputStream.matches(largerWithLfh, 6));

        byte[] nonZipSig = new byte[]{0x50, 0x4b, 0x01, 0x02}; // CFH is not matched by matches()
        assertFalse(ZipArchiveInputStream.matches(nonZipSig, 4));

        byte[] arbitraryData = new byte[]{0x12, 0x34, 0x56, 0x78};
        assertFalse(ZipArchiveInputStream.matches(arbitraryData, 4));
    }

    @Test(timeout = 4000)
    public void testEmptyInputStream() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertNull(zis.getNextZipEntry());
            assertNull(zis.getNextEntry());
        }
    }

    @Test(timeout = 4000)
    public void testTruncatedLocalFileHeader() throws IOException {
        byte[] incompleteLfh = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x14, 0x00};
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(incompleteLfh))) {
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testCentralDirectorySignatureHitsCentralDirectory() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, 0x02014b50L); // CFH_SIG
        for (int i = 0; i < 42; i++) {
            baos.write(0);
        }

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            assertNull(zis.getNextZipEntry());
            // Subsequent calls after hitCentralDirectory must immediately return null
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testNonLfhSignatureReturnsNull() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, 0x08074b50L); // Data Descriptor sig or other non-LFH
        for (int i = 0; i < 26; i++) {
            baos.write(0);
        }

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadBeforeNextZipEntryReturnsMinusOne() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            byte[] buf = new byte[10];
            assertEquals(-1, zis.read(buf, 0, buf.length));
        }
    }

    @Test(timeout = 4000)
    public void testReadZeroBytesReturnsZero() throws IOException {
        byte[] payload = "data".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("zero.txt", payload, ZipArchiveOutputStream.STORED, false));

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            assertNotNull(zis.getNextZipEntry());
            byte[] buf = new byte[10];
            assertEquals(0, zis.read(buf, 0, 0));
        }
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Defects4J Target: Maven221MultiVolumeTest::testRead7ZipMultiVolumeArchiveForStream
     * Failure: "junit.framework.AssertionFailedError: shouldn't be able to read from truncated entry"
     *
     * In the defective version, when a DEFLATED entry's compressed data is truncated mid-stream,
     * read() returns 0 instead of throwing an IOException (or reaching EOF without proper inflation).
     * The test asserts that attempting to read a truncated entry throws an IOException.
     */
    @Test(timeout = 4000)
    public void testRead7ZipMultiVolumeArchiveForStreamDefect() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // LFH signature
        writeInt(baos, 0x04034b50L);
        writeShort(baos, 20); // version needed
        writeShort(baos, 0);  // flags
        writeShort(baos, ZipArchiveOutputStream.DEFLATED); // method: deflated
        writeInt(baos, 0);    // time/date
        writeInt(baos, 0x12345678L); // CRC32
        writeInt(baos, 100);  // compressed size: 100 bytes declared
        writeInt(baos, 500);  // uncompressed size: 500 bytes declared
        byte[] nameBytes = "truncated.bin".getBytes(StandardCharsets.US_ASCII);
        writeShort(baos, nameBytes.length); // file name length
        writeShort(baos, 0);  // extra length
        baos.write(nameBytes);
        // Truncate payload completely: write 0 bytes of compressed data instead of 100

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = zis.getNextZipEntry();
            assertNotNull(entry);

            byte[] buf = new byte[64];
            try {
                int bytesRead;
                while ((bytesRead = zis.read(buf, 0, buf.length)) > 0) {
                    // Loop while reading valid bytes
                }
                fail("shouldn't be able to read from truncated entry");
            } catch (IOException expected) {
                // Expected behavior: Reading a truncated deflated entry must fail with an IOException
                assertTrue(expected.getMessage() != null || expected instanceof ZipException);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadAfterCloseThrowsException() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            zis.close();
            zis.read(new byte[10], 0, 10);
        }
    }

    @Test(timeout = 4000)
    public void testGetNextZipEntryAfterCloseReturnsNull() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            zis.close();
            assertNull(zis.getNextZipEntry());
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeThrowsException() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            zis.skip(-1);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadNegativeStartThrowsException() throws IOException {
        byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("t.txt", payload, ZipArchiveOutputStream.STORED, false));
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[10], -1, 5);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadNegativeLengthThrowsException() throws IOException {
        byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("t.txt", payload, ZipArchiveOutputStream.STORED, false));
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[10], 0, -1);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadBufferOverflowThrowsException() throws IOException {
        byte[] payload = "test".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createZipArchive(createEntryBytes("t.txt", payload, ZipArchiveOutputStream.STORED, false));
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            zis.getNextZipEntry();
            zis.read(new byte[10], 5, 6);
        }
    }

    @Test(timeout = 4000, expected = ZipException.class)
    public void testCorruptedDeflatePayloadThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, 0x04034b50L);
        writeShort(baos, 20);
        writeShort(baos, 0);
        writeShort(baos, ZipArchiveOutputStream.DEFLATED);
        writeInt(baos, 0);
        writeInt(baos, 0);
        writeInt(baos, 10);
        writeInt(baos, 20);
        byte[] nameBytes = "corrupt.txt".getBytes(StandardCharsets.US_ASCII);
        writeShort(baos, nameBytes.length);
        writeShort(baos, 0);
        baos.write(nameBytes);
        // Random corrupt compressed bytes that are invalid for zlib/inflate
        baos.write(new byte[]{0x1F, (byte) 0x8B, 0x00, 0x00, 0x12, 0x34, 0x56, 0x78, (byte) 0xAA, (byte) 0xBB});

        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            assertNotNull(zis.getNextZipEntry());
            byte[] buf = new byte[32];
            zis.read(buf, 0, buf.length);
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCloseIdempotence() throws IOException {
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zis.close();
        zis.close(); // Second close must not throw
    }

    @Test(timeout = 4000)
    public void testConstructorWithDefaultEncoding() throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertNull(zis.getNextZipEntry());
        }
    }

    // -------------------------------------------------------------------------
    // Binary Generation Helper Utilities
    // -------------------------------------------------------------------------

    private static byte[] createZipArchive(byte[] entryData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(entryData);
        // Central Directory Header (CFH) to cleanly terminate entries
        writeInt(baos, 0x02014b50L);
        for (int i = 0; i < 42; i++) {
            baos.write(0);
        }
        return baos.toByteArray();
    }

    private static byte[] createEntryBytes(String name, byte[] payload, int method, boolean hasDataDescriptor)
            throws IOException {
        int flags = hasDataDescriptor ? 8 : 0;
        return createEntryBytesWithExtra(name, payload, method, flags, new byte[0]);
    }

    private static byte[] createEntryBytesWithFlags(String name, byte[] payload, int method, int flags)
            throws IOException {
        return createEntryBytesWithExtra(name, payload, method, flags, new byte[0]);
    }

    private static byte[] createEntryBytesWithExtra(String name, byte[] payload, int method, int flags, byte[] extra)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] compressedData;
        if (method == ZipArchiveOutputStream.DEFLATED) {
            compressedData = deflate(payload);
        } else {
            compressedData = payload;
        }

        CRC32 crc = new CRC32();
        crc.update(payload);
        long crcVal = crc.getValue();

        boolean hasDataDesc = (flags & 8) != 0;

        // Local File Header Signature
        writeInt(baos, 0x04034b50L);
        writeShort(baos, 20); // Version
        writeShort(baos, flags);
        writeShort(baos, method);
        writeInt(baos, 0); // Mod time & date

        if (!hasDataDesc) {
            writeInt(baos, crcVal);
            writeInt(baos, compressedData.length);
            writeInt(baos, payload.length);
        } else {
            writeInt(baos, 0);
            writeInt(baos, 0);
            writeInt(baos, 0);
        }

        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        writeShort(baos, nameBytes.length);
        writeShort(baos, extra.length);
        baos.write(nameBytes);
        baos.write(extra);

        baos.write(compressedData);

        if (hasDataDesc) {
            writeInt(baos, 0x08074b50L); // Optional DD signature (4 bytes)
            writeInt(baos, crcVal);       // CRC (4 bytes)
            writeInt(baos, compressedData.length); // Compressed size (4 bytes)
            writeInt(baos, payload.length);        // Uncompressed size (4 bytes)
        }

        return baos.toByteArray();
    }

    private static byte[] deflate(byte[] data) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            baos.write(buf, 0, count);
        }
        deflater.end();
        return baos.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream baos, int val) {
        baos.write(val & 0xFF);
        baos.write((val >> 8) & 0xFF);
    }

    private static void writeInt(ByteArrayOutputStream baos, long val) {
        baos.write((int) (val & 0xFF));
        baos.write((int) ((val >> 8) & 0xFF));
        baos.write((int) ((val >> 16) & 0xFF));
        baos.write((int) ((val >> 24) & 0xFF));
    }
}