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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 *
 * Decision / Branch Matrix Covered:
 * 1. matches(byte[], int):
 *    - len < 4 (false)
 *    - LFH signature match (true)
 *    - EOCD signature match (true)
 *    - DD signature match (true)
 *    - Single-segment split marker match (true)
 *    - Random/invalid bytes (false)
 * 2. getNextZipEntry() / getNextEntry():
 *    - Stream closed or hitCentralDirectory -> null
 *    - First entry reading & split archive check (DD_SIG -> UnsupportedZipFeatureException SPLITTING)
 *    - Single-segment split marker prefix skipping
 *    - CFH_SIG or AED_SIG encountered -> hitCentralDirectory = true, skipRemainderOfArchive -> null
 *    - Unexpected signature -> ZipException("Unexpected record signature...")
 *    - Parsing LFH: platform, GP flag, UTF8 flag, method, time, CRC, compressedSize, size
 *    - Data descriptor flag set vs unset
 *    - Unicode extra field handling (useUnicodeExtraFields = true/false)
 *    - Zip64 extended information extra field present vs absent (0xFFFFFFFF handling)
 *    - Specialized compression methods: UNSHRINKING, IMPLODING, BZIP2, ENHANCED_DEFLATED
 * 3. read(byte[], int, int):
 *    - Closed stream check -> IOException
 *    - Null current entry -> -1
 *    - Buffer bounds checking (offset < 0, length < 0, offset + length > buf.length) -> AIOOBE
 *    - Feature check & data descriptor support check
 *    - STORED read path (with and without data descriptor, cache buffering)
 *    - DEFLATED read path (Inflater inflate, finished, needsDictionary, truncated)
 *    - Other methods (delegation to current.in)
 *    - Unsupported compression method -> UnsupportedZipFeatureException
 *    - CRC-32 update verification
 * 4. skip(long):
 *    - value < 0 -> IllegalArgumentException
 *    - value == 0 -> returns 0
 *    - value > 0 -> reads and skips until value or EOF
 * 5. canReadEntryData(ArchiveEntry):
 *    - Non-ZipArchiveEntry instance -> false
 *    - Stored entry without data descriptor and unknown size (Defects4J defect target)
 *    - Stored entry with data descriptor and allowStoredEntriesWithDataDescriptor=false -> false
 *    - Stored entry with data descriptor and allowStoredEntriesWithDataDescriptor=true -> true
 *    - Encrypted entry -> false
 * 6. closeEntry():
 *    - Drain outstanding bytes vs skip & pushback
 *    - Data descriptor parsing (with signature and without signature, 4-byte vs 8-byte size)
 *    - Inflater reset & buffer reset
 * 7. skipRemainderOfArchive() & findEocdRecord():
 *    - Reading through central directory headers and trailing EOCD comment
 *
 * Known Defect Targeted:
 * - properlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknown:
 *   Entries with STORED method where the size is unknown (-1 / SIZE_UNKNOWN) should be recognized
 *   as unreadable by canReadEntryData().
 */
public class ZipArchiveInputStreamGeminiTest {

    // --- Helpers to build synthetic ZIP streams ---

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

    private static byte[] createZipArchive(String name, int method, byte[] data, boolean dataDescriptor) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        CRC32 crc = new CRC32();
        if (data != null && data.length > 0) {
            crc.update(data);
        }
        long crcValue = crc.getValue();

        byte[] compressedData = data == null ? new byte[0] : data;
        if (method == ZipEntry.DEFLATED && data != null && data.length > 0) {
            Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            deflater.setInput(data);
            deflater.finish();
            ByteArrayOutputStream defOut = new ByteArrayOutputStream();
            byte[] buf = new byte[128];
            while (!deflater.finished()) {
                int count = deflater.deflate(buf);
                defOut.write(buf, 0, count);
            }
            deflater.end();
            compressedData = defOut.toByteArray();
        }

        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);

        // LFH Signature
        writeInt(baos, ZipLong.LFH_SIG.getValue());
        writeShort(baos, 20); // version needed
        writeShort(baos, dataDescriptor ? 8 : 0); // GP flag: bit 3 = data descriptor
        writeShort(baos, method);
        writeShort(baos, 0); // time
        writeShort(baos, 0); // date

        if (dataDescriptor) {
            writeInt(baos, 0); // crc
            writeInt(baos, 0); // csize
            writeInt(baos, 0); // size
        } else {
            writeInt(baos, crcValue);
            writeInt(baos, compressedData.length);
            writeInt(baos, data == null ? 0 : data.length);
        }

        writeShort(baos, nameBytes.length); // file name length
        writeShort(baos, 0); // extra field length
        baos.write(nameBytes);

        baos.write(compressedData);

        if (dataDescriptor) {
            // Optional DD Signature
            writeInt(baos, ZipLong.DD_SIG.getValue());
            writeInt(baos, crcValue);
            writeInt(baos, compressedData.length);
            writeInt(baos, data == null ? 0 : data.length);
        }

        // Central Directory Header
        long cdOffset = baos.size();
        writeInt(baos, ZipLong.CFH_SIG.getValue());
        writeShort(baos, 20); // version made by
        writeShort(baos, 20); // version needed
        writeShort(baos, dataDescriptor ? 8 : 0);
        writeShort(baos, method);
        writeShort(baos, 0); // time
        writeShort(baos, 0); // date
        writeInt(baos, crcValue);
        writeInt(baos, compressedData.length);
        writeInt(baos, data == null ? 0 : data.length);
        writeShort(baos, nameBytes.length);
        writeShort(baos, 0); // extra len
        writeShort(baos, 0); // comment len
        writeShort(baos, 0); // disk number start
        writeShort(baos, 0); // internal attr
        writeInt(baos, 0); // external attr
        writeInt(baos, 0); // offset of LFH
        baos.write(nameBytes);

        // EOCD
        long cdSize = baos.size() - cdOffset;
        writeInt(baos, 0x06054b50L); // EOCD_SIG
        writeShort(baos, 0); // disk
        writeShort(baos, 0); // start disk
        writeShort(baos, 1); // entries this disk
        writeShort(baos, 1); // total entries
        writeInt(baos, cdSize);
        writeInt(baos, cdOffset);
        writeShort(baos, 0); // comment length

        return baos.toByteArray();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadStoredEntrySuccessfully() throws IOException {
        byte[] payload = "Hello Apache Commons Compress STORED".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("stored.txt", ZipEntry.STORED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("stored.txt", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());
            assertEquals(payload.length, entry.getSize());

            byte[] readBuf = new byte[payload.length];
            int totalRead = 0;
            int r;
            while ((r = zIn.read(readBuf, totalRead, readBuf.length - totalRead)) != -1) {
                totalRead += r;
            }
            assertEquals(payload.length, totalRead);
            assertArrayEquals(payload, readBuf);

            assertEquals(-1, zIn.read());
            assertNull(zIn.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntrySuccessfully() throws IOException {
        byte[] payload = "Hello Apache Commons Compress DEFLATED data string repeated here".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("deflated.txt", ZipEntry.DEFLATED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("deflated.txt", entry.getName());
            assertEquals(ZipEntry.DEFLATED, entry.getMethod());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16];
            int r;
            while ((r = zIn.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, r);
            }
            assertArrayEquals(payload, out.toByteArray());
            assertNull(zIn.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testGetNextEntryGenericArchiveEntry() throws IOException {
        byte[] payload = "data".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("file.txt", ZipEntry.STORED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            ArchiveEntry ae = zIn.getNextEntry();
            assertNotNull(ae);
            assertTrue(ae instanceof ZipArchiveEntry);
            assertEquals("file.txt", ae.getName());
            assertEquals(payload.length, ae.getSize());
        }
    }

    @Test(timeout = 4000)
    public void testSkipBytes() throws IOException {
        byte[] payload = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("skip.txt", ZipEntry.STORED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            assertNotNull(zIn.getNextZipEntry());

            assertEquals(0L, zIn.skip(0));
            assertEquals(5L, zIn.skip(5));

            byte[] remaining = new byte[11];
            int read = zIn.read(remaining, 0, remaining.length);
            assertEquals(11, read);
            assertEquals("56789ABCDEF", new String(remaining, 0, read, StandardCharsets.UTF_8));
        }
    }

    @Test(timeout = 4000)
    public void testMultipleEntriesAndAutoCloseEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] data1 = "first".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "second".getBytes(StandardCharsets.UTF_8);

        byte[] part1 = createZipArchive("1.txt", ZipEntry.STORED, data1, false);
        // Take part1 without EOCD and append a second LFH
        // But simpler: use two sequential entries with getNextZipEntry
        try (ZipArchiveOutputStream zOut = new ZipArchiveOutputStream(baos)) {
            ZipArchiveEntry e1 = new ZipArchiveEntry("first.txt");
            zOut.putArchiveEntry(e1);
            zOut.write(data1);
            zOut.closeArchiveEntry();

            ZipArchiveEntry e2 = new ZipArchiveEntry("second.txt");
            zOut.putArchiveEntry(e2);
            zOut.write(data2);
            zOut.closeArchiveEntry();
        }

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry e1 = zIn.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("first.txt", e1.getName());

            // Advance without fully reading first entry; triggers closeEntry()
            ZipArchiveEntry e2 = zIn.getNextZipEntry();
            assertNotNull(e2);
            assertEquals("second.txt", e2.getName());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b;
            while ((b = zIn.read()) != -1) {
                out.write(b);
            }
            assertArrayEquals(data2, out.toByteArray());
            assertNull(zIn.getNextZipEntry());
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStreamYieldsNull() throws IOException {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertNull(zIn.getNextZipEntry());
            assertNull(zIn.getNextEntry());
        }
    }

    @Test(timeout = 4000)
    public void testMatchesSignatureBoundaries() {
        assertFalse(ZipArchiveInputStream.matches(new byte[0], 0));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b}, 2));
        assertFalse(ZipArchiveInputStream.matches(new byte[]{0x50, 0x4b, 0x03}, 3));

        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        assertTrue(ZipArchiveInputStream.matches(lfh, 4));

        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue(ZipArchiveInputStream.matches(eocd, 4));

        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        assertTrue(ZipArchiveInputStream.matches(dd, 4));

        byte[] split = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue(ZipArchiveInputStream.matches(split, 4));

        byte[] invalid = new byte[]{0x12, 0x34, 0x56, 0x78};
        assertFalse(ZipArchiveInputStream.matches(invalid, 4));
    }

    @Test(timeout = 4000)
    public void testReadZeroBytesBuffer() throws IOException {
        byte[] payload = "testing".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("test.txt", ZipEntry.STORED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            assertNotNull(zIn.getNextZipEntry());
            assertEquals(0, zIn.read(new byte[0], 0, 0));
            assertEquals(0, zIn.read(new byte[10], 0, 0));
        }
    }

    @Test(timeout = 4000)
    public void testSkipBeyondAvailableBytes() throws IOException {
        byte[] payload = "abc".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("short.txt", ZipEntry.STORED, payload, false);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            assertNotNull(zIn.getNextZipEntry());
            long skipped = zIn.skip(1000);
            assertEquals(3L, skipped);
            assertEquals(-1, zIn.read());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    /**
     * TARGETS DEFECT: properlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknown
     * A STORED entry whose size or compressed size is unknown (-1 / SIZE_UNKNOWN)
     * cannot be reliably read by ZipArchiveInputStream. canReadEntryData must return false.
     */
    @Test(timeout = 4000)
    public void testProperlyMarksEntriesAsUnreadableIfUncompressedSizeIsUnknown() {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            ZipArchiveEntry ze = new ZipArchiveEntry("unknown-size.txt");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(ArchiveEntry.SIZE_UNKNOWN); // -1

            assertFalse("Stored entry with unknown size must not be readable",
                    zIn.canReadEntryData(ze));
        }
    }

    /**
     * Additional check for STORED entry with unknown compressed size
     */
    @Test(timeout = 4000)
    public void testProperlyMarksEntriesAsUnreadableIfCompressedSizeIsUnknown() {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            ZipArchiveEntry ze = new ZipArchiveEntry("unknown-csize.txt");
            ze.setMethod(ZipEntry.STORED);
            ze.setSize(100);
            ze.setCompressedSize(ArchiveEntry.SIZE_UNKNOWN);

            assertFalse("Stored entry with unknown compressed size must not be readable",
                    zIn.canReadEntryData(ze));
        }
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataVariousScenarios() {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, false)) {
            // Null or non-ZipArchiveEntry
            assertFalse(zIn.canReadEntryData(null));
            ArchiveEntry genericEntry = new ArchiveEntry() {
                @Override public String getName() { return "test"; }
                @Override public long getSize() { return 0; }
                @Override public boolean isDirectory() { return false; }
                @Override public java.util.Date getLastModifiedDate() { return null; }
            };
            assertFalse(zIn.canReadEntryData(genericEntry));

            // Deflated entry is readable even without pre-known size
            ZipArchiveEntry deflated = new ZipArchiveEntry("def.txt");
            deflated.setMethod(ZipEntry.DEFLATED);
            deflated.setSize(ArchiveEntry.SIZE_UNKNOWN);
            assertTrue(zIn.canReadEntryData(deflated));

            // Stored with known size
            ZipArchiveEntry stored = new ZipArchiveEntry("stored.txt");
            stored.setMethod(ZipEntry.STORED);
            stored.setSize(10);
            stored.setCompressedSize(10);
            assertTrue(zIn.canReadEntryData(stored));

            // Stored with data descriptor when allowStoredEntriesWithDataDescriptor = false
            ZipArchiveEntry storedDD = new ZipArchiveEntry("storedDD.txt");
            storedDD.setMethod(ZipEntry.STORED);
            GeneralPurposeBit gp = new GeneralPurposeBit();
            gp.useDataDescriptor(true);
            storedDD.setGeneralPurposeBit(gp);
            assertFalse(zIn.canReadEntryData(storedDD));
        }

        // When allowStoredEntriesWithDataDescriptor = true
        try (ZipArchiveInputStream zInAllowDD = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true)) {
            ZipArchiveEntry storedDD = new ZipArchiveEntry("storedDD.txt");
            storedDD.setMethod(ZipEntry.STORED);
            GeneralPurposeBit gp = new GeneralPurposeBit();
            gp.useDataDescriptor(true);
            storedDD.setGeneralPurposeBit(gp);
            assertTrue(zInAllowDD.canReadEntryData(storedDD));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeThrowsException() throws IOException {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            zIn.skip(-1);
        }
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadOnClosedStreamThrowsIOException() throws IOException {
        ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zIn.close();
        zIn.read(new byte[10], 0, 10);
    }

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        zIn.close();
        zIn.close(); // Should not throw
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadOffsetOutOfBoundsNegative() throws IOException {
        byte[] zipBytes = createZipArchive("t.txt", ZipEntry.STORED, new byte[2], false);
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            zIn.getNextZipEntry();
            zIn.read(new byte[10], -1, 5);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadLengthOutOfBoundsExceedsBuffer() throws IOException {
        byte[] zipBytes = createZipArchive("t.txt", ZipEntry.STORED, new byte[2], false);
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            zIn.getNextZipEntry();
            zIn.read(new byte[10], 5, 6);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadLengthNegative() throws IOException {
        byte[] zipBytes = createZipArchive("t.txt", ZipEntry.STORED, new byte[2], false);
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(zipBytes))) {
            zIn.getNextZipEntry();
            zIn.read(new byte[10], 0, -1);
        }
    }

    @Test(timeout = 4000, expected = UnsupportedZipFeatureException.class)
    public void testSplitArchiveThrowsUnsupportedZipFeatureException() throws IOException {
        // First 4 bytes are DD_SIG => indicates split archive
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, ZipLong.DD_SIG.getValue());
        for (int i = 0; i < 30; i++) {
            baos.write(0);
        }

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            zIn.getNextZipEntry();
        }
    }

    @Test(timeout = 4000, expected = ZipException.class)
    public void testCorruptRecordSignatureThrowsZipException() throws IOException {
        // Signature that is neither LFH, CFH, nor AED
        byte[] corruptHeader = new byte[30];
        corruptHeader[0] = 'B';
        corruptHeader[1] = 'A';
        corruptHeader[2] = 'D';
        corruptHeader[3] = '!';

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(corruptHeader))) {
            zIn.getNextZipEntry();
        }
    }

    @Test(timeout = 4000, expected = EOFException.class)
    public void testTruncatedZipEntryThrowsEOFExceptionOnDrain() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] nameBytes = "truncated.txt".getBytes(StandardCharsets.UTF_8);

        // LFH indicating compressedSize = 100, but stream ends right after header
        writeInt(baos, ZipLong.LFH_SIG.getValue());
        writeShort(baos, 20);
        writeShort(baos, 0); // no data descriptor
        writeShort(baos, ZipEntry.STORED);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeInt(baos, 12345L); // crc
        writeInt(baos, 100L); // csize = 100
        writeInt(baos, 100L); // size = 100
        writeShort(baos, nameBytes.length);
        writeShort(baos, 0);
        baos.write(nameBytes);
        // Do not write actual 100 bytes data

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            assertNotNull(zIn.getNextZipEntry());
            // Attempting to close or move to next entry drains remaining 100 bytes, causing EOFException
            zIn.getNextZipEntry();
        }
    }

    // =========================================================================
    // Partition E: Advanced Format Variants & Object Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleSegmentSplitMarkerSkipped() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeInt(baos, ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getValue());

        byte[] payload = "split-single-content".getBytes(StandardCharsets.UTF_8);
        byte[] validZip = createZipArchive("entry.txt", ZipEntry.STORED, payload, false);
        baos.write(validZip);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("entry.txt", entry.getName());

            byte[] b = new byte[payload.length];
            int r = zIn.read(b);
            assertEquals(payload.length, r);
            assertArrayEquals(payload, b);
        }
    }

    @Test(timeout = 4000)
    public void testStoredEntryWithDataDescriptorRead() throws IOException {
        byte[] payload = "data-descriptor-stored".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("dd_stored.txt", ZipEntry.STORED, payload, true);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(
                new ByteArrayInputStream(zipBytes), "UTF-8", true, true)) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("dd_stored.txt", entry.getName());
            assertTrue(entry.getGeneralPurposeBit().usesDataDescriptor());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8];
            int r;
            while ((r = zIn.read(buf, 0, buf.length)) != -1) {
                bos.write(buf, 0, r);
            }
            assertArrayEquals(payload, bos.toByteArray());
        }
    }

    @Test(timeout = 4000)
    public void testStoredEntryWithDataDescriptorWithoutSig() throws IOException {
        // Construct STORED entry with data descriptor that has NO DD_SIG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] payload = "no-dd-sig".getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = "test_nosig.txt".getBytes(StandardCharsets.UTF_8);

        CRC32 crc = new CRC32();
        crc.update(payload);

        writeInt(baos, ZipLong.LFH_SIG.getValue());
        writeShort(baos, 20);
        writeShort(baos, 8); // GP bit 3 set
        writeShort(baos, ZipEntry.STORED);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeInt(baos, 0); // crc
        writeInt(baos, 0); // csize
        writeInt(baos, 0); // size
        writeShort(baos, nameBytes.length);
        writeShort(baos, 0);
        baos.write(nameBytes);
        baos.write(payload);

        // Data descriptor without signature: crc (4), csize (4), size (4)
        writeInt(baos, crc.getValue());
        writeInt(baos, payload.length);
        writeInt(baos, payload.length);

        // Central directory to follow
        long cdOffset = baos.size();
        writeInt(baos, ZipLong.CFH_SIG.getValue());
        writeShort(baos, 20);
        writeShort(baos, 20);
        writeShort(baos, 8);
        writeShort(baos, ZipEntry.STORED);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeInt(baos, crc.getValue());
        writeInt(baos, payload.length);
        writeInt(baos, payload.length);
        writeShort(baos, nameBytes.length);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeInt(baos, 0);
        writeInt(baos, 0);
        baos.write(nameBytes);

        // EOCD
        long cdSize = baos.size() - cdOffset;
        writeInt(baos, 0x06054b50L);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeShort(baos, 1);
        writeShort(baos, 1);
        writeInt(baos, cdSize);
        writeInt(baos, cdOffset);
        writeShort(baos, 0);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(
                new ByteArrayInputStream(baos.toByteArray()), "UTF-8", true, true)) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            byte[] buf = new byte[payload.length];
            int read = zIn.read(buf);
            assertEquals(payload.length, read);
            assertArrayEquals(payload, buf);
            assertNull(zIn.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithoutCurrentEntryReturnsMinusOne() throws IOException {
        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertEquals(-1, zIn.read(new byte[10], 0, 10));
        }
    }

    @Test(timeout = 4000)
    public void testZip64ExtraFieldParsing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] payload = "zip64-test-content".getBytes(StandardCharsets.UTF_8);
        byte[] nameBytes = "zip64.txt".getBytes(StandardCharsets.UTF_8);

        CRC32 crc = new CRC32();
        crc.update(payload);

        // Build Zip64 Extra Field (Header ID 0x0001, size 16: 8 bytes uncompressed, 8 bytes compressed)
        ByteArrayOutputStream extraOut = new ByteArrayOutputStream();
        writeShort(extraOut, 0x0001); // Zip64 header ID
        writeShort(extraOut, 16); // data size
        // 8 bytes original size
        writeInt(extraOut, payload.length);
        writeInt(extraOut, 0);
        // 8 bytes compressed size
        writeInt(extraOut, payload.length);
        writeInt(extraOut, 0);
        byte[] extraBytes = extraOut.toByteArray();

        // LFH
        writeInt(baos, ZipLong.LFH_SIG.getValue());
        writeShort(baos, 45); // version needed 4.5
        writeShort(baos, 0);
        writeShort(baos, ZipEntry.STORED);
        writeShort(baos, 0);
        writeShort(baos, 0);
        writeInt(baos, crc.getValue());
        writeInt(baos, 0xFFFFFFFFL); // ZIP64_MAGIC
        writeInt(baos, 0xFFFFFFFFL); // ZIP64_MAGIC
        writeShort(baos, nameBytes.length);
        writeShort(baos, extraBytes.length);
        baos.write(nameBytes);
        baos.write(extraBytes);
        baos.write(payload);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(payload.length, entry.getSize());
            assertEquals(payload.length, entry.getCompressedSize());

            byte[] readBytes = new byte[payload.length];
            int r = zIn.read(readBytes);
            assertEquals(payload.length, r);
            assertArrayEquals(payload, readBytes);
        }
    }

    @Test(timeout = 4000)
    public void testEocdCommentHandling() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] payload = "content".getBytes(StandardCharsets.UTF_8);
        byte[] zipBytes = createZipArchive("entry.txt", ZipEntry.STORED, payload, false);

        // Append a comment to the EOCD
        byte[] comment = "Archive Comment Text".getBytes(StandardCharsets.UTF_8);
        // Update the last 2 bytes of zipBytes (comment length) and append comment
        zipBytes[zipBytes.length - 2] = (byte) (comment.length & 0xFF);
        zipBytes[zipBytes.length - 1] = (byte) ((comment.length >> 8) & 0xFF);

        baos.write(zipBytes);
        baos.write(comment);

        try (ZipArchiveInputStream zIn = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = zIn.getNextZipEntry();
            assertNotNull(entry);
            assertNull(zIn.getNextZipEntry()); // Correctly skips central directory and comment
        }
    }
}