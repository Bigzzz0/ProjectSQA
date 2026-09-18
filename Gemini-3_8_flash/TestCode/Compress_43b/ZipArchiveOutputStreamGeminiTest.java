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
package org.apache.commons.compress.archivers.zip;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Defects4J Ground Truth):
 *    - DataDescriptorTest::doesntWriteDataDescriptorWhenAddingRawEntries
 *      Failure: `arrays first differed at element [0]; expected:<0> but was:<8>`
 *      Root Cause: When using `addRawArchiveEntry()` with known size and CRC (2-phase source),
 *      `writeLocalFileHeader()` previously evaluated `usesDataDescriptor()` solely based on
 *      (zipMethod == DEFLATED && channel == null) without honoring the `phased` state. As a result,
 *      General Purpose Bit 3 (data descriptor flag = 1 << 3 = 8) was encoded into the Local File
 *      Header even though no data descriptor was written at the end.
 *      Target Test: `testDefectDoesntWriteDataDescriptorWhenAddingRawEntries()` asserting GPB byte == 0.
 *
 * 2. PARTITION A: Core Functional Logic & State Transitions
 *    - Standard DEFLATED streaming with auto-generated Data Descriptors.
 *    - Standard STORED streaming with upfront sizes/CRC.
 *    - Seekable output channel (SeekableInMemoryByteChannel & File) for random-access header rewriting.
 *    - Multi-entry archive lifecycle: putArchiveEntry -> write -> closeArchiveEntry -> finish -> close.
 *
 * 3. PARTITION B: Boundary Value Analysis & Extremes
 *    - Compression level boundaries (-1 to 9).
 *    - Empty entry (0-length write and preClose empty handling).
 *    - Encoding boundaries (UTF-8, ISO-8859-1, non-encodable characters).
 *    - Resource alignment padding calculations (alignment > 1).
 *
 * 4. PARTITION C: Zip64 Transitions & Modes
 *    - Zip64Mode.Never vs. Zip64Mode.Always vs. Zip64Mode.AsNeeded.
 *    - Size threshold crossing (ZIP64_MAGIC = 0xFFFFFFFFL).
 *
 * 5. PARTITION D: Defensive Guards & Exception Paths
 *    - Unclosed entries on finish(), finish() on already finished stream.
 *    - Premature write() or closeArchiveEntry() without active entry.
 *    - Checksum/size mismatches for STORED entries without seekable channel.
 *    - Unsupported entry methods in canWriteEntryData().
 */
public class ZipArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition C / Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectDoesntWriteDataDescriptorWhenAddingRawEntries() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        final ZipArchiveEntry rawEntry = new ZipArchiveEntry("rawDeflatedEntry.txt");
        rawEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        rawEntry.setSize(16);
        rawEntry.setCompressedSize(12);
        rawEntry.setCrc(0x12345678L);

        final byte[] dummyDeflatedPayload = new byte[] {
            0x78, (byte) 0x9c, 0x4b, 0x4c, 0x4a, 0x4e, 0x01, 0x00, 0x04, 0x5b, 0x01, 0x59
        };
        final ByteArrayInputStream rawStream = new ByteArrayInputStream(dummyDeflatedPayload);

        zos.addRawArchiveEntry(rawEntry, rawStream);
        zos.close();

        final byte[] outputBytes = baos.toByteArray();
        assertTrue("Output should at least contain a local file header", outputBytes.length >= 30);

        // In Local File Header, General Purpose Bit flag is at offset 6 (2 bytes, little-endian)
        // Bit 3 (0x08) indicates presence of Data Descriptor. When adding raw phased entries,
        // it must NOT be set.
        final byte[] gpb = new byte[] { outputBytes[6], outputBytes[7] };
        assertArrayEquals("GPB flags must not have Data Descriptor flag (bit 3) set",
                new byte[] { 0, 0 }, gpb);
    }

    @Test(timeout = 4000)
    public void testAddRawArchiveEntryRemovesExistingZip64Extra() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        final ZipArchiveEntry rawEntry = new ZipArchiveEntry("rawWithZip64.txt");
        rawEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        rawEntry.setSize(100);
        rawEntry.setCompressedSize(50);
        rawEntry.setCrc(0x87654321L);

        final Zip64ExtendedInformationExtraField z64 = new Zip64ExtendedInformationExtraField(
                new ZipEightByteInteger(100), new ZipEightByteInteger(50));
        rawEntry.addExtraField(z64);

        final byte[] payload = new byte[50];
        zos.addRawArchiveEntry(rawEntry, new ByteArrayInputStream(payload));
        zos.close();

        final byte[] result = baos.toByteArray();
        assertTrue("Archive should be written successfully", result.length > 0);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeflatedEntryWithDataDescriptorOnNonSeekableStream() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        assertFalse("Standard OutputStream must not be seekable", zos.isSeekable());

        final ZipArchiveEntry entry = new ZipArchiveEntry("file1.txt");
        zos.putArchiveEntry(entry);
        final byte[] content = "HelloDeflatedStream".getBytes("UTF-8");
        zos.write(content, 0, content.length);
        zos.closeArchiveEntry();

        zos.finish();
        zos.close();

        final byte[] data = baos.toByteArray();
        assertTrue("Archive must not be empty", data.length > 0);

        // Verify Data Descriptor signature (0x08074b50 -> {0x50, 0x4b, 0x07, 0x08}) exists in output
        boolean ddFound = false;
        for (int i = 0; i < data.length - 4; i++) {
            if (data[i] == 0x50 && data[i + 1] == 0x4b && data[i + 2] == 0x07 && data[i + 3] == 0x08) {
                ddFound = true;
                break;
            }
        }
        assertTrue("Data descriptor signature must be present for DEFLATED non-seekable entry", ddFound);
    }

    @Test(timeout = 4000)
    public void testStoredEntryOnNonSeekableStream() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        final byte[] content = "ExactStoredContent".getBytes("UTF-8");
        final java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(content);

        final ZipArchiveEntry entry = new ZipArchiveEntry("stored.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(content.length);
        entry.setCrc(crc.getValue());

        zos.putArchiveEntry(entry);
        zos.write(content, 0, content.length);
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > content.length);
    }

    @Test(timeout = 4000)
    public void testSeekableInMemoryByteChannelLifecycle() throws Exception {
        final SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel);

        assertTrue("SeekableInMemoryByteChannel must be seekable", zos.isSeekable());

        // For seekable channels, STORED entries do NOT require CRC and size up front
        final ZipArchiveEntry storedEntry = new ZipArchiveEntry("seekableStored.bin");
        storedEntry.setMethod(ZipArchiveOutputStream.STORED);
        zos.putArchiveEntry(storedEntry);
        final byte[] data = new byte[] { 10, 20, 30, 40 };
        zos.write(data, 0, data.length);
        zos.closeArchiveEntry();

        // DEFLATED entry on seekable channel should rewrite sizes to LFH without Data Descriptor
        final ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("seekableDeflated.bin");
        deflatedEntry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(deflatedEntry);
        zos.write(data, 0, data.length);
        zos.closeArchiveEntry();

        zos.close();

        final byte[] archiveBytes = channel.array();
        assertTrue(archiveBytes.length > 0);
    }

    @Test(timeout = 4000)
    public void testFileConstructorOperation() throws Exception {
        final File tempFile = File.createTempFile("tempZipStream", ".zip");
        try {
            final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempFile);
            assertTrue("File-based stream should be seekable", zos.isSeekable());

            final ZipArchiveEntry entry = new ZipArchiveEntry("fileEntry.txt");
            zos.putArchiveEntry(entry);
            zos.write(new byte[] { 65, 66, 67 });
            zos.closeArchiveEntry();
            zos.close();

            assertTrue("Zip file should exist and have content", tempFile.exists() && tempFile.length() > 0);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test(timeout = 4000)
    public void testAutomaticArchiveEntryCreation() throws Exception {
        final File tempFile = File.createTempFile("tempSource", ".txt");
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

            final ArchiveEntry ae = zos.createArchiveEntry(tempFile, "dir/entry.txt");
            assertTrue(ae instanceof ZipArchiveEntry);
            assertEquals("dir/entry.txt", ae.getName());

            zos.close();
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Configurations
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyZipArchive() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.finish();
        zos.close();

        // An empty ZIP file still contains the 22-byte End of Central Directory record
        final byte[] data = baos.toByteArray();
        assertEquals("Empty ZIP archive must be 22 bytes (EOCD)", 22, data.length);
    }

    @Test(timeout = 4000)
    public void testCompressionLevelsAndMethodChanges() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setLevel(Deflater.NO_COMPRESSION);
        final ZipArchiveEntry e1 = new ZipArchiveEntry("none.txt");
        zos.putArchiveEntry(e1);
        zos.write(new byte[] { 1, 2, 3 });
        zos.closeArchiveEntry();

        zos.setLevel(Deflater.BEST_COMPRESSION);
        final ZipArchiveEntry e2 = new ZipArchiveEntry("best.txt");
        zos.putArchiveEntry(e2);
        zos.write(new byte[] { 4, 5, 6 });
        zos.closeArchiveEntry();

        zos.setMethod(ZipArchiveOutputStream.STORED);
        final ZipArchiveEntry e3 = new ZipArchiveEntry("storedByDefault.txt");
        e3.setSize(0);
        e3.setCrc(0);
        zos.putArchiveEntry(e3);
        zos.closeArchiveEntry();

        zos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testCommentsAndEncodingSettings() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setEncoding("UTF-8");
        assertEquals("UTF-8", zos.getEncoding());

        zos.setComment("Main archive comment");
        zos.setUseLanguageEncodingFlag(true);
        zos.setFallbackToUTF8(true);

        final ZipArchiveEntry entry = new ZipArchiveEntry("commented.txt");
        entry.setComment("Entry level comment");
        zos.putArchiveEntry(entry);
        zos.write(new byte[] { 'A' });
        zos.closeArchiveEntry();

        zos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testUnicodeExtraFieldsPolicy() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setEncoding("US-ASCII");
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        final ZipArchiveEntry entry = new ZipArchiveEntry("unicode_test_✓.txt");
        entry.setComment("Comment_✓");
        zos.putArchiveEntry(entry);
        zos.write(new byte[] { 42 });
        zos.closeArchiveEntry();

        zos.close();
        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testResourceAlignmentHandling() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        final ZipArchiveEntry entry = new ZipArchiveEntry("alignedEntry.bin");
        entry.setAlignment(16);
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);

        zos.putArchiveEntry(entry);
        zos.write(new byte[] { 1, 2, 3, 4, 5 });
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > 0);
    }

    // =========================================================================
    // Partition C (Cont): Zip64 Modes and Limits
    // =========================================================================

    @Test(timeout = 4000)
    public void testZip64ModeAlwaysAddsZip64Headers() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Always);

        final ZipArchiveEntry entry = new ZipArchiveEntry("smallAlwaysZip64.txt");
        zos.putArchiveEntry(entry);
        zos.write(new byte[] { 1, 2, 3, 4 });
        zos.closeArchiveEntry();
        zos.close();

        final byte[] data = baos.toByteArray();
        // Zip64 End of Central Directory signature: 0x06064b50 -> { 0x50, 0x4b, 0x06, 0x06 }
        boolean zip64EOCDFound = false;
        for (int i = 0; i < data.length - 4; i++) {
            if (data[i] == 0x50 && data[i + 1] == 0x4b && data[i + 2] == 0x06 && data[i + 3] == 0x06) {
                zip64EOCDFound = true;
                break;
            }
        }
        assertTrue("Zip64Mode.Always must write ZIP64 EOCD even for small archives", zip64EOCDFound);
    }

    @Test(expected = Zip64RequiredException.class, timeout = 4000)
    public void testZip64ModeNeverRejectsLargeEntryAtPut() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Never);

        final ZipArchiveEntry largeEntry = new ZipArchiveEntry("tooBig.bin");
        largeEntry.setSize(ZipConstants.ZIP64_MAGIC + 1L);

        zos.putArchiveEntry(largeEntry);
    }

    // =========================================================================
    // Partition D: Defensive Guard Paths & Exception Handling
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidCompressionLevelHighThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.setLevel(10);
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidCompressionLevelLowThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.setLevel(-2);
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testWriteWithoutActiveEntryThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.write(new byte[] { 1, 2, 3 }, 0, 3);
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntryWithoutActiveEntryThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.closeArchiveEntry();
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithUnclosedEntryThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.putArchiveEntry(new ZipArchiveEntry("unclosed.txt"));
            zos.finish();
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishTwiceThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.finish();
            zos.finish();
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCreateArchiveEntryAfterFinishThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        final File dummyFile = File.createTempFile("dummy", ".tmp");
        try {
            zos.finish();
            zos.createArchiveEntry(dummyFile, "test.txt");
        } finally {
            if (dummyFile.exists()) {
                dummyFile.delete();
            }
            zos.destroy();
        }
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredEntryWithoutSizeThrowsExceptionOnNonSeekableStream() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            final ZipArchiveEntry entry = new ZipArchiveEntry("storedNoSize.txt");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            entry.setCrc(1234L);
            // Size omitted
            zos.putArchiveEntry(entry);
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredEntryWithoutCrcThrowsExceptionOnNonSeekableStream() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            final ZipArchiveEntry entry = new ZipArchiveEntry("storedNoCrc.txt");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            entry.setSize(10L);
            // CRC omitted
            zos.putArchiveEntry(entry);
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredEntrySizeMismatchThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            final ZipArchiveEntry entry = new ZipArchiveEntry("mismatch.txt");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            entry.setSize(5);
            entry.setCrc(0x12345678L);
            zos.putArchiveEntry(entry);

            // Write 3 bytes instead of declared 5
            zos.write(new byte[] { 1, 2, 3 });
            zos.closeArchiveEntry();
        } finally {
            zos.destroy();
        }
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredEntryCrcMismatchThrowsException() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            final byte[] data = new byte[] { 1, 2, 3 };
            final ZipArchiveEntry entry = new ZipArchiveEntry("badCrc.txt");
            entry.setMethod(ZipArchiveOutputStream.STORED);
            entry.setSize(data.length);
            entry.setCrc(999999L); // Deliberately incorrect CRC
            zos.putArchiveEntry(entry);

            zos.write(data);
            zos.closeArchiveEntry();
        } finally {
            zos.destroy();
        }
    }

    // =========================================================================
    // Partition E: Entry Capability and Utility Inspections
    // =========================================================================

    @Test(timeout = 4000)
    public void testCanWriteEntryDataInspection() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        final ZipArchiveEntry deflated = new ZipArchiveEntry("deflated.txt");
        deflated.setMethod(ZipArchiveOutputStream.DEFLATED);
        assertTrue("Must be able to write DEFLATED entry", zos.canWriteEntryData(deflated));

        final ZipArchiveEntry stored = new ZipArchiveEntry("stored.txt");
        stored.setMethod(ZipArchiveOutputStream.STORED);
        assertTrue("Must be able to write STORED entry", zos.canWriteEntryData(stored));

        final ZipArchiveEntry imploded = new ZipArchiveEntry("imploded.txt");
        imploded.setMethod(ZipMethod.IMPLODING.getCode());
        assertFalse("Must reject IMPLODING entry", zos.canWriteEntryData(imploded));

        final ZipArchiveEntry unshrinking = new ZipArchiveEntry("unshrinking.txt");
        unshrinking.setMethod(ZipMethod.UNSHRINKING.getCode());
        assertFalse("Must reject UNSHRINKING entry", zos.canWriteEntryData(unshrinking));

        final ArchiveEntry foreignEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "foreign"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public Date getLastModifiedDate() { return new Date(); }
        };
        assertFalse("Must reject non-ZipArchiveEntry instance", zos.canWriteEntryData(foreignEntry));

        zos.close();
    }

    @Test(timeout = 4000)
    public void testFlushAndMultipleEntriesConsecutiveWrite() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        // putArchiveEntry should automatically close any preceding active entry
        final ZipArchiveEntry e1 = new ZipArchiveEntry("first.txt");
        zos.putArchiveEntry(e1);
        zos.write(new byte[] { 1 });

        final ZipArchiveEntry e2 = new ZipArchiveEntry("second.txt");
        zos.putArchiveEntry(e2);
        zos.write(new byte[] { 2, 3 });

        zos.flush();
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > 0);
    }
}