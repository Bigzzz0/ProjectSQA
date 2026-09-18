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

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.ArchiveEntry;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & States:
 * 1. Defect Zone: Multiple finish() calls or finish() followed by close() where the entries list
 *    has been cleared, causing a zero-length central directory and corrupted EOCD record.
 * 2. Seekable (RandomAccessFile) vs Stream (OutputStream) Output:
 *    - isSeekable() true vs false.
 *    - STORED entries: with RAF (no precomputed size/CRC needed; header backfilled via seek)
 *      vs with Stream (precomputed size & CRC required; verified at closeArchiveEntry).
 * 3. Compression Methods & Sizing:
 *    - DEFLATED vs STORED.
 *    - Data descriptor written for DEFLATED with Stream, omitted for RAF.
 *    - write() buffer chunking: length <= 8192 vs length > 8192 (full blocks + tail remainder).
 *    - write() zero bytes (length <= 0).
 * 4. Compression Levels & Dynamic Level Changes:
 *    - Default level (-1), valid bounds [0, 9], invalid levels (< -1 or > 9).
 *    - hasCompressionLevelChanged state transition applied to deflater on next DEFLATED entry.
 * 5. Character Encodings & Unicode Extra Fields:
 *    - UTF-8 vs non-UTF-8 encodings (e.g. US-ASCII, CP437).
 *    - UnicodeExtraFieldPolicy: NEVER, ALWAYS, NOT_ENCODEABLE.
 *    - fallbackToUTF8 enabled vs disabled for unencodable names/comments.
 *    - Entry comments: null, empty "", encodable, non-encodable.
 * 6. Guard Paths & Error Conditions:
 *    - finish() called with unclosed entry throws IOException.
 *    - STORED without size / CRC throws ZipException.
 *    - STORED with mismatched CRC or size throws ZipException.
 *    - closeArchiveEntry() called when entry is null (safe no-op).
 */
public class ZipArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeflatedEntryViaStreamWorkflow() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        assertFalse("Stream output must not be seekable", zos.isSeekable());

        ZipArchiveEntry ze = new ZipArchiveEntry("deflated.txt");
        ze.setMethod(ZipArchiveOutputStream.DEFLATED);
        ze.setTime(1234567890L);
        zos.putArchiveEntry(ze);

        byte[] payload = "Compression test payload string".getBytes("UTF-8");
        zos.write(payload, 0, payload.length);
        zos.closeArchiveEntry();

        // Calling closeArchiveEntry a second time when entry is null must be a safe no-op
        zos.closeArchiveEntry();

        zos.finish();
        zos.close();

        assertTrue("Archive bytes must be generated", baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testStoredEntryViaStreamWithCorrectCrcAndSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        byte[] content = "Raw uncompressed data".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);

        ZipArchiveEntry ze = new ZipArchiveEntry("stored.bin");
        ze.setMethod(ZipArchiveOutputStream.STORED);
        ze.setSize(content.length);
        ze.setCrc(crc.getValue());

        zos.putArchiveEntry(ze);
        zos.write(content, 0, content.length);
        zos.closeArchiveEntry();
        zos.close();

        assertTrue("Output should contain written stored data", baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testSeekableFileStoredWorkflowWithoutPresetSizeOrCrc() throws IOException {
        File tempFile = File.createTempFile("seekable_test", ".zip");
        tempFile.deleteOnExit();

        try {
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(tempFile);
            assertTrue("File-based constructor should yield seekable stream", zos.isSeekable());

            ZipArchiveEntry ze = new ZipArchiveEntry("seekable_stored.txt");
            ze.setMethod(ZipArchiveOutputStream.STORED);
            // Neither CRC nor Size is pre-set; seekable stream must resolve them at entry close
            zos.putArchiveEntry(ze);

            byte[] data = "Hello RandomAccessFile Zip!".getBytes("UTF-8");
            zos.write(data, 0, data.length);
            zos.closeArchiveEntry();
            zos.close();

            ZipFile zf = new ZipFile(tempFile);
            try {
                ZipArchiveEntry readEntry = zf.getEntry("seekable_stored.txt");
                assertNotNull("Entry must exist in archive", readEntry);
                assertEquals(data.length, readEntry.getSize());
                assertEquals(data.length, readEntry.getCompressedSize());

                CRC32 expectedCrc = new CRC32();
                expectedCrc.update(data);
                assertEquals(expectedCrc.getValue(), readEntry.getCrc());
            } finally {
                zf.close();
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test(timeout = 4000)
    public void testLargeDeflatedWriteSplittingBlocks() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("large_chunk.bin");
        ze.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(ze);

        // Buffer size larger than DEFLATER_BLOCK_SIZE (8192) to test chunking loop and remainder
        byte[] largeData = new byte[8192 * 2 + 1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 127);
        }

        zos.write(largeData, 0, largeData.length);
        // Also test zero-length write
        zos.write(largeData, 0, 0);
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testCompressionLevelChangeBetweenEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setLevel(Deflater.BEST_SPEED);
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry1.txt");
        zos.putArchiveEntry(entry1);
        zos.write("data 1".getBytes("UTF-8"), 0, 6);
        zos.closeArchiveEntry();

        zos.setLevel(Deflater.BEST_COMPRESSION);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry2.txt");
        zos.putArchiveEntry(entry2);
        zos.write("data 2".getBytes("UTF-8"), 0, 6);
        zos.closeArchiveEntry();

        zos.close();
        assertTrue(baos.size() > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompressionLevelBoundaryValues() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setLevel(Deflater.DEFAULT_COMPRESSION); // -1
        zos.setLevel(Deflater.NO_COMPRESSION);      // 0
        zos.setLevel(Deflater.BEST_SPEED);          // 1
        zos.setLevel(Deflater.BEST_COMPRESSION);    // 9
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCompressionLevelUnderflowThrows() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setLevel(-2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCompressionLevelOverflowThrows() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setLevel(10);
    }

    @Test(timeout = 4000)
    public void testEncodingAndLanguageFlagGettersSetters() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        assertEquals("UTF-8", zos.getEncoding());

        zos.setEncoding("US-ASCII");
        assertEquals("US-ASCII", zos.getEncoding());

        zos.setUseLanguageEncodingFlag(true);
        zos.setFallbackToUTF8(true);
        zos.setComment("Global archive comment");

        zos.setEncoding(null);
        assertNull(zos.getEncoding());
    }

    @Test(timeout = 4000)
    public void testUnicodeExtraFieldPolicyValues() {
        ZipArchiveOutputStream.UnicodeExtraFieldPolicy always =
            ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS;
        ZipArchiveOutputStream.UnicodeExtraFieldPolicy never =
            ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NEVER;
        ZipArchiveOutputStream.UnicodeExtraFieldPolicy notEncodeable =
            ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE;

        assertEquals("always", always.toString());
        assertEquals("never", never.toString());
        assertEquals("not encodeable", notEncodeable.toString());
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryFromFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        File temp = File.createTempFile("sample_entry", ".tmp");
        temp.deleteOnExit();
        try {
            ArchiveEntry entry = zos.createArchiveEntry(temp, "custom/name.txt");
            assertNotNull(entry);
            assertTrue(entry instanceof ZipArchiveEntry);
            assertEquals("custom/name.txt", entry.getName());
        } finally {
            temp.delete();
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Corrupt CD Ground Truth)
    // =========================================================================

    /**
     * Targets the Defect where finish() followed by close(), or repeated finish() calls,
     * causes finish() to re-execute on an already cleared entries list, generating a
     * zero-length central directory and corrupted End-of-Central-Directory (EOCD).
     *
     * Expected behavior: The written archive must remain structurally valid and readable
     * by ZipFile without throwing "central directory is empty, can't expand corrupt archive".
     */
    @Test(timeout = 4000)
    public void testFinishAndCloseDoesNotCorruptCentralDirectory() throws IOException {
        File tempFile = File.createTempFile("defect_target_test", ".zip");
        tempFile.deleteOnExit();

        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos);

            ZipArchiveEntry ze = new ZipArchiveEntry("data.txt");
            ze.setMethod(ZipArchiveOutputStream.DEFLATED);
            zos.putArchiveEntry(ze);
            byte[] payload = "Verification payload for defect".getBytes("UTF-8");
            zos.write(payload, 0, payload.length);
            zos.closeArchiveEntry();

            // Explicitly call finish(), followed by close() (e.g. in try-with-resources or finally)
            zos.finish();
            zos.close();

            // Verify with ZipFile that central directory is not empty/corrupted
            ZipFile zf = new ZipFile(tempFile);
            try {
                ZipArchiveEntry readEntry = zf.getEntry("data.txt");
                assertNotNull("Central directory must contain 'data.txt'", readEntry);
                assertEquals(payload.length, readEntry.getSize());
            } finally {
                zf.close();
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @Test(timeout = 4000)
    public void testMultipleFinishInvocationsAreIdempotent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("file.bin");
        ze.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(ze);
        zos.write(new byte[]{1, 2, 3, 4}, 0, 4);
        zos.closeArchiveEntry();

        zos.finish();
        int sizeAfterFirstFinish = baos.size();

        // Second finish should be a no-op and not append a corrupt empty EOCD
        zos.finish();
        assertEquals("Subsequent finish() should not append duplicate/empty EOCD records",
                     sizeAfterFirstFinish, baos.size());
        zos.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithUnclosedEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("unclosed.txt");
        zos.putArchiveEntry(ze);
        // Calling finish before closeArchiveEntry must fail per contract
        zos.finish();
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredStreamMissingSizeThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("no_size.bin");
        ze.setMethod(ZipArchiveOutputStream.STORED);
        ze.setCrc(12345L);
        // size remains -1
        zos.putArchiveEntry(ze);
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredStreamMissingCrcThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("no_crc.bin");
        ze.setMethod(ZipArchiveOutputStream.STORED);
        ze.setSize(100L);
        // crc remains -1
        zos.putArchiveEntry(ze);
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredStreamBadCrcAtCloseThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry ze = new ZipArchiveEntry("bad_crc.bin");
        ze.setMethod(ZipArchiveOutputStream.STORED);
        ze.setSize(4);
        ze.setCrc(999999L); // Deliberately mismatching CRC

        zos.putArchiveEntry(ze);
        zos.write(new byte[]{1, 2, 3, 4}, 0, 4);
        zos.closeArchiveEntry();
    }

    @Test(expected = ZipException.class, timeout = 4000)
    public void testStoredStreamBadSizeAtCloseThrowsZipException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        byte[] data = new byte[]{1, 2, 3};
        CRC32 crc = new CRC32();
        crc.update(data);

        ZipArchiveEntry ze = new ZipArchiveEntry("bad_size.bin");
        ze.setMethod(ZipArchiveOutputStream.STORED);
        ze.setSize(10); // Declared 10, but only writing 3 bytes
        ze.setCrc(crc.getValue());

        zos.putArchiveEntry(ze);
        zos.write(data, 0, data.length);
        zos.closeArchiveEntry();
    }

    // =========================================================================
    // Partition E: Encodings, Fallback, Extra Fields, & Flusher Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testNonEncodableFilenameWithFallbackToUTF8AndUnicodeExtraFields() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        // US-ASCII cannot encode umlaut 'ä' or euro symbol
        zos.setEncoding("US-ASCII");
        zos.setFallbackToUTF8(true);
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);

        ZipArchiveEntry ze = new ZipArchiveEntry("t\u00E4st_\u20AC.txt");
        ze.setComment("Comment with non-ascii \u00FC");
        ze.setMethod(ZipArchiveOutputStream.DEFLATED);

        zos.putArchiveEntry(ze);
        byte[] payload = "Unicode test".getBytes("UTF-8");
        zos.write(payload, 0, payload.length);
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testUnicodeExtraFieldsPolicyAlwaysWithEncodableAscii() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setEncoding("US-ASCII");
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry ze = new ZipArchiveEntry("ascii_only.txt");
        ze.setComment("pure ascii comment");
        ze.setMethod(ZipArchiveOutputStream.DEFLATED);

        zos.putArchiveEntry(ze);
        zos.write(new byte[]{'A', 'B'}, 0, 2);
        zos.closeArchiveEntry();
        zos.close();

        assertTrue(baos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testEntryDefaultsAppliedWhenUnspecified() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        zos.setMethod(ZipArchiveOutputStream.DEFLATED);

        // Entry with method = -1 and time = -1
        ZipArchiveEntry ze = new ZipArchiveEntry("defaults.txt");
        assertEquals(-1, ze.getMethod());
        assertEquals(-1, ze.getTime());

        zos.putArchiveEntry(ze);
        assertEquals("Method should default to current output stream method",
                     ZipArchiveOutputStream.DEFLATED, ze.getMethod());
        assertTrue("Time should have been populated", ze.getTime() != -1);

        zos.closeArchiveEntry();
        zos.close();
    }

    @Test(timeout = 4000)
    public void testFlushPassesThroughUnderlyingStream() throws IOException {
        final boolean[] flushed = new boolean[]{false};
        OutputStream trackingOut = new OutputStream() {
            @Override
            public void write(int b) {
                // discard
            }
            @Override
            public void flush() {
                flushed[0] = true;
            }
        };

        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(trackingOut);
        zos.flush();
        assertTrue("Underlying stream flush() must be called", flushed[0]);
    }

    @Test(timeout = 4000)
    public void testAutoClosingPreviousEntryOnPutArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);

        ZipArchiveEntry entry1 = new ZipArchiveEntry("first.txt");
        entry1.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(entry1);
        zos.write(new byte[]{1, 2}, 0, 2);

        // putArchiveEntry should implicitly close entry1
        ZipArchiveEntry entry2 = new ZipArchiveEntry("second.txt");
        entry2.setMethod(ZipArchiveOutputStream.DEFLATED);
        zos.putArchiveEntry(entry2);
        zos.write(new byte[]{3, 4}, 0, 2);
        zos.closeArchiveEntry();

        zos.close();
        assertTrue(baos.size() > 0);
    }
}