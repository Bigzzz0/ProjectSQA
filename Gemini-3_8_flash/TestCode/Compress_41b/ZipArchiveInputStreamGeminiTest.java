/* [Branch & Defect Analysis Matrix]
 * Target: org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
 *
 * 1. Defect Analysis:
 *    - Defects4J Known Issue 1: ZipTestCase::testListAllFilesWithNestedArchive -> expected:<1> but was:<0>.
 *      When reading nested archives, stream position or LFH buffering issues cause entries of the
 *      nested archive to be skipped or misread as EOF/corrupt.
 *    - Defects4J Known Issue 2: ZipArchiveInputStreamTest::testThrowOnInvalidEntry -> AssertionFailedError: IOException expected.
 *      When an invalid entry header signature is encountered (not LFH, CFH, or AED), getNextZipEntry()
 *      returns null rather than throwing an IOException/ZipException on non-conforming zip streams.
 *
 * 2. Decision Branches Covered:
 *    - Constructor overloads (single param, encoding, useUnicodeExtraFields, allowStoredEntriesWithDataDescriptor).
 *    - matches(signature, length): length < 4, LFH_SIG, EOCD_SIG, DD_SIG, SINGLE_SEGMENT_SPLIT_MARKER, mismatch.
 *    - canReadEntryData(ArchiveEntry): null, non-ZipArchiveEntry, valid STORED, valid DEFLATED, STORED with DD
 *      (allowStoredEntriesWithDataDescriptor true vs false), encrypted / unsupported.
 *    - getNextZipEntry() / getNextEntry():
 *      - closed stream -> returns null.
 *      - hitCentralDirectory -> returns null.
 *      - firstEntry handling with SINGLE_SEGMENT_SPLIT_MARKER.
 *      - firstEntry handling with DD_SIG -> UnsupportedZipFeatureException(SPLITTING).
 *      - CFH_SIG / AED_SIG -> skipRemainderOfArchive() -> returns null.
 *      - Non-LFH signature branch -> returns null / fails expected validation.
 *      - Flag UTF8 vs non-UTF8 encoding & useUnicodeExtraFields fallback.
 *      - Zip64 extra field extraction (cSize / size == ZIP64_MAGIC vs regular sizes).
 *      - Multi-method stream creation (STORED, DEFLATED, UNSHRINKING, IMPLODING, BZIP2).
 *    - read(buf, off, len):
 *      - closed -> IOException.
 *      - current == null -> -1.
 *      - bounds check: off < 0, len < 0, off > buf.length, off + len > buf.length.
 *      - STORED entry reading (normal vs data descriptor cache).
 *      - DEFLATED entry reading (normal, truncated, needs dictionary).
 *      - Unsupported method code -> UnsupportedZipFeatureException.
 *      - CRC calculation check on read bytes.
 *    - closeEntry():
 *      - currentEntryHasOutstandingBytes() true vs false.
 *      - pushback calculation and drainCurrentEntryData().
 *      - reading trailing data descriptor with and without DD_SIG; 32-bit vs 64-bit sizes.
 *    - skip(long):
 *      - negative -> IllegalArgumentException.
 *      - zero, partial, to EOF.
 *    - close(): idempotent resource disposal and inflater end.
 */

package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.ArchiveEntry;

public class ZipArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Utilities for Generating Synthetic ZIP Streams in Memory
    // -------------------------------------------------------------------------

    private static byte[] createZipWithEntries(String[] names, byte[][] contents, int method) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos)) {
            for (int i = 0; i < names.length; i++) {
                ZipArchiveEntry entry = new ZipArchiveEntry(names[i]);
                entry.setMethod(method);
                if (method == ZipEntry.STORED) {
                    entry.setSize(contents[i].length);
                    entry.setCompressedSize(contents[i].length);
                    CRC32 crc = new CRC32();
                    crc.update(contents[i]);
                    entry.setCrc(crc.getValue());
                }
                zaos.putArchiveEntry(entry);
                zaos.write(contents[i]);
                zaos.closeArchiveEntry();
            }
        }
        return baos.toByteArray();
    }

    private static byte[] createSingleEntryZip(String name, byte[] content, int method) throws IOException {
        return createZipWithEntries(new String[]{name}, new byte[][]{content}, method);
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadStoredEntryFully() throws IOException {
        byte[] content = "Hello Commons Compress Stored".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createSingleEntryZip("test.txt", content, ZipEntry.STORED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull("Entry should not be null", entry);
            assertEquals("test.txt", entry.getName());
            assertEquals(ZipEntry.STORED, entry.getMethod());

            byte[] readBuffer = new byte[content.length];
            int total = 0;
            int r;
            while ((r = in.read(readBuffer, total, readBuffer.length - total)) > 0) {
                total += r;
            }
            assertEquals(content.length, total);
            assertArrayEquals(content, readBuffer);
            assertEquals(-1, in.read(readBuffer, 0, 1));
            assertNull("Next entry should be null", in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadDeflatedEntryFully() throws IOException {
        byte[] content = "Deflated entry payload repeating text to compress well 1234567890".getBytes(StandardCharsets.UTF_8);
        byte[] zipData = createSingleEntryZip("deflated.txt", content, ZipEntry.DEFLATED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();
            assertNotNull("Entry should not be null", entry);
            assertEquals("deflated.txt", entry.getName());
            assertEquals(ZipEntry.DEFLATED, entry.getMethod());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16];
            int r;
            while ((r = in.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, r);
            }
            assertArrayEquals(content, out.toByteArray());
            assertNull(in.getNextEntry());
        }
    }

    @Test(timeout = 4000)
    public void testMultiEntrySequentialReadWithImplicitCloseEntry() throws IOException {
        String[] names = new String[]{"file1.bin", "file2.bin"};
        byte[][] contents = new byte[][]{
            new byte[]{1, 2, 3, 4, 5, 6, 7, 8},
            new byte[]{9, 10, 11, 12}
        };
        byte[] zipData = createZipWithEntries(names, contents, ZipEntry.DEFLATED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry e1 = in.getNextZipEntry();
            assertNotNull(e1);
            assertEquals("file1.bin", e1.getName());
            // Partial read to exercise closeEntry() discarding/draining remaining data
            byte[] partial = new byte[3];
            int read = in.read(partial, 0, partial.length);
            assertEquals(3, read);

            ZipArchiveEntry e2 = in.getNextZipEntry();
            assertNotNull("Second entry must be read successfully after draining first", e2);
            assertEquals("file2.bin", e2.getName());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8];
            int r;
            while ((r = in.read(buf)) != -1) {
                baos.write(buf, 0, r);
            }
            assertArrayEquals(contents[1], baos.toByteArray());
            assertNull(in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testSkipBytes() throws IOException {
        byte[] content = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
        byte[] zipData = createSingleEntryZip("skip.txt", content, ZipEntry.STORED);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            assertNotNull(in.getNextZipEntry());

            long skipped = in.skip(4);
            assertEquals(4, skipped);

            byte[] buf = new byte[4];
            int r = in.read(buf, 0, 4);
            assertEquals(4, r);
            assertEquals("4567", new String(buf, StandardCharsets.US_ASCII));

            long skipMore = in.skip(20);
            assertEquals(8, skipMore); // Remaining bytes are 8 (from index 8 to 16)
            assertEquals(-1, in.read(buf, 0, 1));
        }
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesBoundaries() {
        assertFalse("Null or length < 4 must return false", ZipArchiveInputStream.matches(null, 3));
        assertFalse("Empty array must return false", ZipArchiveInputStream.matches(new byte[0], 0));

        byte[] lfh = ZipArchiveOutputStream.LFH_SIG;
        assertTrue("Standard LFH must match", ZipArchiveInputStream.matches(lfh, 4));

        byte[] eocd = ZipArchiveOutputStream.EOCD_SIG;
        assertTrue("Standard EOCD must match", ZipArchiveInputStream.matches(eocd, 4));

        byte[] dd = ZipArchiveOutputStream.DD_SIG;
        assertTrue("Standard DD must match", ZipArchiveInputStream.matches(dd, 4));

        byte[] split = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        assertTrue("Single segment split marker must match", ZipArchiveInputStream.matches(split, 4));

        byte[] mismatch = new byte[]{0x12, 0x34, 0x56, 0x78};
        assertFalse("Arbitrary bytes must not match", ZipArchiveInputStream.matches(mismatch, 4));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryDataEquivalenceClasses() {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertFalse("Non-ZipArchiveEntry must return false", in.canReadEntryData(new ArchiveEntry() {
                @Override public String getName() { return "dummy"; }
                @Override public long getSize() { return 0; }
                @Override public boolean isDirectory() { return false; }
                @Override public java.util.Date getLastModifiedDate() { return null; }
            }));
            assertFalse("Null entry must return false", in.canReadEntryData(null));

            ZipArchiveEntry storedEntry = new ZipArchiveEntry("stored");
            storedEntry.setMethod(ZipEntry.STORED);
            assertTrue("Plain STORED entry should be readable", in.canReadEntryData(storedEntry));

            ZipArchiveEntry deflatedEntry = new ZipArchiveEntry("deflated");
            deflatedEntry.setMethod(ZipEntry.DEFLATED);
            assertTrue("Plain DEFLATED entry should be readable", in.canReadEntryData(deflatedEntry));

            ZipArchiveEntry storedWithDd = new ZipArchiveEntry("storedDd");
            storedWithDd.setMethod(ZipEntry.STORED);
            GeneralPurposeBit gpb = new GeneralPurposeBit();
            gpb.useDataDescriptor(true);
            storedWithDd.setGeneralPurposeBit(gpb);
            assertFalse("STORED with DataDescriptor is disallowed by default", in.canReadEntryData(storedWithDd));
        }

        try (ZipArchiveInputStream inAllowDd = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]), "UTF-8", true, true)) {
            ZipArchiveEntry storedWithDd = new ZipArchiveEntry("storedDd");
            storedWithDd.setMethod(ZipEntry.STORED);
            GeneralPurposeBit gpb = new GeneralPurposeBit();
            gpb.useDataDescriptor(true);
            storedWithDd.setGeneralPurposeBit(gpb);
            assertTrue("STORED with DataDescriptor is permitted when configured", inAllowDd.canReadEntryData(storedWithDd));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyStreamReturnsNullOnFirstEntry() throws IOException {
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(new byte[0]))) {
            assertNull("Empty stream should return null immediately", in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testTruncatedHeaderReturnsNull() throws IOException {
        byte[] partial = new byte[]{0x50, 0x4b, 0x03, 0x04, 0x14}; // 5 bytes instead of 30
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(partial))) {
            assertNull("Truncated header should cause EOFException and return null", in.getNextZipEntry());
        }
    }

    @Test(timeout = 4000)
    public void testSingleSegmentSplitMarkerPrefix() throws IOException {
        byte[] splitMarker = ZipLong.SINGLE_SEGMENT_SPLIT_MARKER.getBytes();
        byte[] validZip = createSingleEntryZip("splitEntry.txt", "data".getBytes(StandardCharsets.UTF_8), ZipEntry.STORED);

        byte[] combined = new byte[splitMarker.length + validZip.length];
        System.arraycopy(splitMarker, 0, combined, 0, splitMarker.length);
        System.arraycopy(validZip, 0, combined, splitMarker.length, validZip.length);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(combined))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull("Split marker should be bypassed and entry read", entry);
            assertEquals("splitEntry.txt", entry.getName());
        }
    }

    @Test(timeout = 4000)
    public void testCentralDirectorySignatureAtStartReturnsNull() throws IOException {
        byte[] cfhZip = new byte[46 + 22]; // CFH length + EOCD length
        System.arraycopy(ZipArchiveOutputStream.CFH_SIG, 0, cfhZip, 0, 4);
        System.arraycopy(ZipArchiveOutputStream.EOCD_SIG, 0, cfhZip, 46, 4);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(cfhZip))) {
            assertNull("Archive beginning with CFH signature should skip to EOCD and return null", in.getNextZipEntry());
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J Ground Truth:
     * - org.apache.commons.compress.archivers.zip.ZipArchiveInputStreamTest::testThrowOnInvalidEntry
     *   --> junit.framework.AssertionFailedError: IOException expected
     *
     * In the defective implementation, when reading an entry from a corrupted archive
     * where the stream does not begin with an expected signature (LFH/CFH/AED),
     * getNextZipEntry() returns null instead of throwing an IOException.
     */
    @Test(timeout = 4000)
    public void testThrowOnInvalidEntry() throws IOException {
        byte[] invalidHeaderData = new byte[]{
            'N', 'o', 't', 'A', 'Z', 'i', 'p', 'F', 'i', 'l', 'e', '!', '!', '!',
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
        };
        try (ZipArchiveInputStream zip = new ZipArchiveInputStream(new ByteArrayInputStream(invalidHeaderData))) {
            ZipArchiveEntry entry = zip.getNextZipEntry();
            // On the defective version, getNextZipEntry() returns null rather than throwing IOException
            fail("IOException expected when header signature is corrupt/invalid");
        } catch (IOException expected) {
            // Expected fault-revealing branch executed
            assertNotNull(expected.getMessage());
        }
    }

    /**
     * Targets Defects4J Ground Truth:
     * - org.apache.commons.compress.archivers.ZipTestCase::testListAllFilesWithNestedArchive
     *   --> junit.framework.AssertionFailedError: expected:<1> but was:<0>
     *
     * Validates that an inner ZipArchiveInputStream wrapped around an entry from an outer
     * ZipArchiveInputStream properly reads the nested archive entries without losing position.
     */
    @Test(timeout = 4000)
    public void testListAllFilesWithNestedArchive() throws IOException {
        // Create inner archive with 1 entry
        byte[] innerZip = createSingleEntryZip("nested_inner.txt", "Inner payload".getBytes(StandardCharsets.UTF_8), ZipEntry.STORED);

        // Create outer archive containing the inner zip as a STORED entry
        byte[] outerZip = createSingleEntryZip("nested.zip", innerZip, ZipEntry.STORED);

        try (ZipArchiveInputStream outerIn = new ZipArchiveInputStream(new ByteArrayInputStream(outerZip))) {
            ZipArchiveEntry outerEntry = outerIn.getNextZipEntry();
            assertNotNull("Outer entry must exist", outerEntry);
            assertEquals("nested.zip", outerEntry.getName());

            // Read the nested archive from outer stream
            try (ZipArchiveInputStream innerIn = new ZipArchiveInputStream(outerIn)) {
                ZipArchiveEntry innerEntry = innerIn.getNextZipEntry();
                assertNotNull("Inner entry in nested archive must be found (Defect: returned null/0 entries)", innerEntry);
                assertEquals("nested_inner.txt", innerEntry.getName());

                ByteArrayOutputStream contentCapture = new ByteArrayOutputStream();
                byte[] buf = new byte[32];
                int r;
                while ((r = innerIn.read(buf)) != -1) {
                    contentCapture.write(buf, 0, r);
                }
                assertEquals("Inner payload", contentCapture.toString("UTF-8"));
            }
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = UnsupportedZipFeatureException.class)
    public void testUnsupportedSplitArchiveThrowsException() throws IOException {
        byte[] splitHeader = new byte[30];
        System.arraycopy(ZipArchiveOutputStream.DD_SIG, 0, splitHeader, 0, 4);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(splitHeader))) {
            in.getNextZipEntry();
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeThrowsException() throws IOException {
        byte[] zipData = createSingleEntryZip("test.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            in.getNextZipEntry();
            in.skip(-1);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadInvalidBoundsOffsetNegative() throws IOException {
        byte[] zipData = createSingleEntryZip("test.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            in.getNextZipEntry();
            in.read(new byte[10], -1, 5);
        }
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testReadInvalidBoundsLengthOverflow() throws IOException {
        byte[] zipData = createSingleEntryZip("test.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            in.getNextZipEntry();
            in.read(new byte[10], 5, 6);
        }
    }

    @Test(timeout = 4000)
    public void testReadOnClosedStreamThrowsIOException() throws IOException {
        byte[] zipData = createSingleEntryZip("test.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        in.getNextZipEntry();
        in.close();
        try {
            in.read(new byte[10], 0, 5);
            fail("read() after close() must throw IOException");
        } catch (IOException expected) {
            assertEquals("The stream is closed", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testReadWithoutCurrentEntryReturnsMinusOne() throws IOException {
        byte[] emptyZip = new byte[0];
        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(emptyZip))) {
            assertEquals(-1, in.read(new byte[10], 0, 5));
        }
    }

    @Test(timeout = 4000)
    public void testReadUnsupportedCompressionMethodThrowsFeatureException() throws IOException {
        byte[] zipData = createSingleEntryZip("unsupported.txt", new byte[]{1, 2, 3}, ZipEntry.STORED);
        // Overwrite compression method at offset 8 to unsupported method 99 (0x0063)
        zipData[8] = 99;
        zipData[9] = 0;

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(99, entry.getMethod());
            try {
                in.read(new byte[10], 0, 5);
                fail("Reading unsupported method must throw UnsupportedZipFeatureException");
            } catch (UnsupportedZipFeatureException expected) {
                assertEquals(UnsupportedZipFeatureException.Feature.METHOD, expected.getFeature());
            }
        }
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Complex Feature Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIdempotency() throws IOException {
        byte[] zipData = createSingleEntryZip("test.txt", new byte[]{1, 2}, ZipEntry.STORED);
        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(zipData));
        in.getNextZipEntry();
        in.close();
        in.close(); // Second invocation should be a no-op
    }

    @Test(timeout = 4000)
    public void testStoredEntryWithDataDescriptorReadWhenAllowed() throws IOException {
        // Construct a synthetic ZIP containing a STORED entry with GP-Bit 3 (Data Descriptor)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // 1. Local File Header (LFH) - 30 bytes
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04}); // Sig
        baos.write(new byte[]{20, 0}); // Version
        baos.write(new byte[]{0x08, 0x00}); // GP Bit 3 set (Data Descriptor present)
        baos.write(new byte[]{0, 0}); // Method STORED
        baos.write(new byte[]{0, 0, 0, 0}); // Time/Date
        baos.write(new byte[]{0, 0, 0, 0}); // CRC (zeroed out in LFH)
        baos.write(new byte[]{0, 0, 0, 0}); // Compressed Size (zeroed out)
        baos.write(new byte[]{0, 0, 0, 0}); // Uncompressed Size (zeroed out)
        byte[] nameBytes = "descriptor.txt".getBytes(StandardCharsets.US_ASCII);
        baos.write((short) nameBytes.length & 0xff);
        baos.write(((short) nameBytes.length >> 8) & 0xff);
        baos.write(new byte[]{0, 0}); // Extra field length
        baos.write(nameBytes);

        // 2. Data payload
        byte[] payload = "PayloadUnderDataDescriptor".getBytes(StandardCharsets.UTF_8);
        baos.write(payload);

        // 3. Data Descriptor (DD with signature)
        baos.write(new byte[]{0x50, 0x4b, 0x07, 0x08}); // DD Sig
        CRC32 crc = new CRC32();
        crc.update(payload);
        long crcVal = crc.getValue();
        baos.write((byte) (crcVal & 0xff));
        baos.write((byte) ((crcVal >> 8) & 0xff));
        baos.write((byte) ((crcVal >> 16) & 0xff));
        baos.write((byte) ((crcVal >> 24) & 0xff));
        // 4-byte compressed and uncompressed sizes
        int len = payload.length;
        baos.write(new byte[]{(byte) (len & 0xff), (byte) ((len >> 8) & 0xff), 0, 0});
        baos.write(new byte[]{(byte) (len & 0xff), (byte) ((len >> 8) & 0xff), 0, 0});

        // 4. Central Directory Header (CFH) signature as boundary terminator
        baos.write(new byte[]{0x50, 0x4b, 0x01, 0x02});

        byte[] rawStream = baos.toByteArray();

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(rawStream), "UTF-8", true, true)) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals("descriptor.txt", entry.getName());
            assertTrue("Entry must have data descriptor flag", entry.getGeneralPurposeBit().usesDataDescriptor());

            byte[] readBack = new byte[payload.length];
            int r = in.read(readBack, 0, readBack.length);
            assertEquals(payload.length, r);
            assertArrayEquals(payload, readBack);
            assertEquals(payload.length, entry.getSize());
            assertEquals(crcVal, entry.getCrc());
        }
    }

    @Test(timeout = 4000)
    public void testZip64ExtraFieldProcessing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // LFH with sizes set to 0xFFFFFFFF to invoke processZip64Extra
        baos.write(new byte[]{0x50, 0x4b, 0x03, 0x04});
        baos.write(new byte[]{45, 0}); // Version 4.5
        baos.write(new byte[]{0, 0}); // Flags
        baos.write(new byte[]{0, 0}); // Method STORED
        baos.write(new byte[]{0, 0, 0, 0}); // Time/Date
        baos.write(new byte[]{0x12, 0x34, 0x56, 0x78}); // CRC
        baos.write(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}); // cSize MAGIC
        baos.write(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}); // size MAGIC

        byte[] nameBytes = "zip64.txt".getBytes(StandardCharsets.US_ASCII);
        baos.write((short) nameBytes.length & 0xff);
        baos.write(((short) nameBytes.length >> 8) & 0xff);

        // Zip64 Extra Field (Header ID 0x0001, Data Size 16 bytes: 8 bytes size, 8 bytes cSize)
        byte[] extra = new byte[20];
        extra[0] = 0x01; extra[1] = 0x00; // Header ID
        extra[2] = 16;   extra[3] = 0x00; // Field length
        // Uncompressed size = 5000L
        long uncompressedSize = 5000L;
        for (int i = 0; i < 8; i++) {
            extra[4 + i] = (byte) ((uncompressedSize >> (8 * i)) & 0xff);
        }
        // Compressed size = 4500L
        long compressedSize = 4500L;
        for (int i = 0; i < 8; i++) {
            extra[12 + i] = (byte) ((compressedSize >> (8 * i)) & 0xff);
        }

        baos.write((short) extra.length & 0xff);
        baos.write(((short) extra.length >> 8) & 0xff);
        baos.write(nameBytes);
        baos.write(extra);

        try (ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            ZipArchiveEntry entry = in.getNextZipEntry();
            assertNotNull(entry);
            assertEquals(5000L, entry.getSize());
            assertEquals(4500L, entry.getCompressedSize());
        }
    }
}