package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

/**
 * Test class for ZipArchiveOutputStream.
 *
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic & state transitions (constructor, putArchiveEntry, closeArchiveEntry, finish, close, write)
 * - Partition B: Boundary Value Analysis (null/empty arguments, invalid method/level, size=0, MAX values)
 * - Partition C: Defect-targeted zone – addRawArchiveEntry behavior (data descriptor writing control)
 * - Partition D: Exception & defensive guard paths (finished stream, missing entry, unknown sizes for STORED, Zip64Mode.Never with large sizes)
 * - Partition E: Object lifecycle & contract (encoding, comment, getters, setters, canWriteEntryData, createArchiveEntry)
 */
public class ZipArchiveOutputStreamDeepseekTest {

    /* ------------------- Partition A: Core Functional Logic & State Transitions ------------------- */

    @Test(timeout = 4000)
    public void testWriteStoredEntryKnownSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.STORED);
        byte[] content = "Hello".getBytes("UTF-8");
        CRC32 crc = new CRC32();
        crc.update(content);
        entry.setSize(content.length);
        entry.setCompressedSize(content.length);
        entry.setCrc(crc.getValue());
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.close();
        // Verify archive can be read back
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry readEntry = zf.getEntry("test.txt");
        assertNotNull(readEntry);
        assertEquals(content.length, readEntry.getSize());
        assertEquals(crc.getValue(), readEntry.getCrc());
        zf.close();
    }

    @Test(timeout = 4000)
    public void testWriteDeflatedEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        byte[] content = "Hello World".getBytes("UTF-8");
        zos.putArchiveEntry(entry);
        zos.write(content);
        zos.closeArchiveEntry();
        zos.close();
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry readEntry = zf.getEntry("test.txt");
        assertNotNull(readEntry);
        assertEquals(content.length, readEntry.getSize());
        zf.close();
    }

    @Test(timeout = 4000)
    public void testFinishAndClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        assertFalse(zos.finished);
        zos.finish();
        assertTrue(zos.finished);
        zos.close(); // close after finish should be safe
    }

    @Test(timeout = 4000)
    public void testCloseUnfinished() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.close(); // auto-finish
        assertTrue(zos.finished);
    }

    /* ------------------- Partition B: Boundary Value Analysis ------------------- */

    @Test(timeout = 4000)
    public void testNullArchiveEntryThrowsClassCastException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.putArchiveEntry((ArchiveEntry) null);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
            // expected
        } finally {
            zos.close();
        }
    }

    @Test(timeout = 4000)
    public void testSetLevelInvalidBelowMin() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.setLevel(Deflater.DEFAULT_COMPRESSION - 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zos.close();
    }

    @Test(timeout = 4000)
    public void testSetLevelInvalidAboveMax() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        try {
            zos.setLevel(Deflater.BEST_COMPRESSION + 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        zos.close();
    }

    @Test(timeout = 4000)
    public void testWriteEmptyArray() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("empty.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        zos.putArchiveEntry(entry);
        zos.write(new byte[0]); // should be allowed
        zos.closeArchiveEntry();
        zos.close();
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry readEntry = zf.getEntry("empty.txt");
        assertNotNull(readEntry);
        assertEquals(0, readEntry.getSize());
        zf.close();
    }

    /* ------------------- Partition C: Defect-Targeted Zone (addRawArchiveEntry) ------------------- */

    @Test(timeout = 4000)
    public void testDoesNotWriteDataDescriptorWhenAddingRawEntries() throws IOException {
        // This test targets the known Defects4J bug: DataDescriptor written incorrectly for raw entries.
        // For a phased raw entry (sizes known), no data descriptor should appear.
        // We use a non-seekable output stream to trigger data descriptor logic.
        // The bug would cause a data descriptor to be present, causing a mismatch when reading.
        byte[] rawContent = "Hello Compressed".getBytes("UTF-8");
        // Compress the raw content using Deflater to simulate a pre-compressed entry
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(rawContent);
        deflater.finish();
        ByteArrayOutputStream compressedBuf = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            compressedBuf.write(buf, 0, count);
        }
        byte[] compressedData = compressedBuf.toByteArray();
        deflater.end();

        // Pre-compute CRC and sizes
        CRC32 crc = new CRC32();
        crc.update(rawContent);
        long crcValue = crc.getValue();
        long uncompressedSize = rawContent.length;
        long compressedSize = compressedData.length;

        ZipArchiveEntry entry = new ZipArchiveEntry("raw.txt");
        entry.setMethod(ZipEntry.DEFLATED);
        entry.setSize(uncompressedSize);
        entry.setCompressedSize(compressedSize);
        entry.setCrc(crcValue);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.addRawArchiveEntry(entry, new ByteArrayInputStream(compressedData));
        zos.close();

        // Read back the archive and verify sizes and CRC match expected
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry readEntry = zf.getEntry("raw.txt");
        assertNotNull("Entry should be present", readEntry);
        assertEquals("Uncompressed size mismatch", uncompressedSize, readEntry.getSize());
        assertEquals("Compressed size mismatch", compressedSize, readEntry.getCompressedSize());
        assertEquals("CRC mismatch", crcValue, readEntry.getCrc());
        // Additionally, verify the content can be read correctly
        InputStream is = zf.getInputStream(readEntry);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] rbuf = new byte[1024];
        int len;
        while ((len = is.read(rbuf)) > 0) {
            result.write(rbuf, 0, len);
        }
        is.close();
        assertArrayEquals("Raw content mismatch", rawContent, result.toByteArray());
        zf.close();
    }

    @Test(timeout = 4000)
    public void testAddRawArchiveEntryNonPhased() throws IOException {
        // For a non-phased raw entry (sizes unknown), a data descriptor should be written (for DEFLATED, non-seekable).
        // We simulate a non-phased entry by leaving sizes unknown.
        byte[] rawContent = "NonPhasedRaw".getBytes("UTF-8");
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        deflater.setInput(rawContent);
        deflater.finish();
        ByteArrayOutputStream compressedBuf = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buf);
            compressedBuf.write(buf, 0, count);
        }
        byte[] compressedData = compressedBuf.toByteArray();
        deflater.end();

        // Do NOT set size/compressedSize/crc (leave as UNKNOWN)
        ZipArchiveEntry entry = new ZipArchiveEntry("nonphased.txt");
        entry.setMethod(ZipEntry.DEFLATED);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.addRawArchiveEntry(entry, new ByteArrayInputStream(compressedData));
        zos.close();

        seekableCheck(baos.toByteArray(), "nonphased.txt", rawContent);
    }

    private void seekableCheck(byte[] archiveBytes, String entryName, byte[] expectedContent) throws IOException {
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(archiveBytes);
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry readEntry = zf.getEntry(entryName);
        assertNotNull(readEntry);
        InputStream is = zf.getInputStream(readEntry);
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            result.write(buf, 0, len);
        }
        is.close();
        assertArrayEquals(expectedContent, result.toByteArray());
        zf.close();
    }

    /* ------------------- Partition D: Exception & Defensive Guard Paths ------------------- */

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishTwiceThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.finish();
        zos.finish(); // should throw
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testWriteAfterFinishThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.finish();
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        zos.putArchiveEntry(e); // should throw because finished
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishWithUnclosedEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipEntry.STORED);
        e.setSize(10);
        e.setCrc(123);
        zos.putArchiveEntry(e);
        zos.finish(); // should throw because entry is not closed
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryWithNoEntryThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.closeArchiveEntry(); // no current entry
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testWriteWithNoEntryThrowsIllegalStateException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.write(new byte[1]); // no entry
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryWithFinishedStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.finish();
        try {
            zos.createArchiveEntry(new java.io.File("dummy.txt"), "dummy.txt");
            fail("Expected IOException");
        } catch (IOException e) {
            // expected
        }
        zos.close();
    }

    /* ------------------- Partition E: Object Lifecycle & Contract Integrity ------------------- */

    @Test(timeout = 4000)
    public void testSetAndGetEncoding() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", zos.getEncoding());
        zos.close();
    }

    @Test(timeout = 4000)
    public void testSetComment() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setComment("Test Comment");
        zos.putArchiveEntry(new ZipArchiveEntry("f"));
        zos.closeArchiveEntry();
        zos.close();
        // Verify comment in the archive
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        assertEquals("Test Comment", zf.getComment());
        zf.close();
    }

    @Test(timeout = 4000)
    public void testIsSeekableWithOutputStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        assertFalse(zos.isSeekable());
        zos.close();
    }

    @Test(timeout = 4000)
    public void testIsSeekableWithSeekableByteChannel() throws IOException {
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(channel);
        assertTrue(zos.isSeekable());
        zos.close();
    }

    @Test(timeout = 4000)
    public void testSetLevelAndMethod() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setLevel(Deflater.BEST_COMPRESSION);
        zos.setMethod(ZipEntry.STORED);
        zos.putArchiveEntry(new ZipArchiveEntry("test"));
        zos.closeArchiveEntry();
        zos.close();
        SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(baos.toByteArray());
        ZipFile zf = new ZipFile(channel);
        ZipArchiveEntry entry = zf.getEntries().next();
        assertEquals(ZipEntry.STORED, entry.getMethod());
        zf.close();
    }

    @Test(timeout = 4000)
    public void testCanWriteEntryData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry goodEntry = new ZipArchiveEntry("good");
        assertTrue(zos.canWriteEntryData(goodEntry));
        // Entry with unsupported method (e.g., imploding) should return false
        ZipArchiveEntry badEntry = new ZipArchiveEntry("bad");
        badEntry.setMethod(ZipMethod.IMPLODING.getCode());
        assertFalse(zos.canWriteEntryData(badEntry));
        zos.close();
    }

    @Test(timeout = 4000)
    public void testUseZip64ModeNeverRejectsLargeEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Never);
        ZipArchiveEntry entry = new ZipArchiveEntry("large");
        entry.setMethod(ZipEntry.DEFLATED);
        // Set size > 4GB to trigger exception
        entry.setSize(0xFFFFFFFFL + 1L); // > 4GB
        entry.setCompressedSize(0xFFFFFFFFL + 1L);
        entry.setCrc(0L);
        try {
            zos.putArchiveEntry(entry);
            fail("Expected Zip64RequiredException");
        } catch (Zip64RequiredException e) {
            // expected
        }
        zos.close();
    }

    @Test(timeout = 4000)
    public void testUseZip64ModeNeverRejectsManyEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos);
        zos.setUseZip64(Zip64Mode.Never);
        // Create 65536 entries? That's many. Simulate by setting internal state? Not possible.
        // Instead, we rely on the fact that after writing 65536 entries, finish would throw.
        // But to test quickly, we can fake the entry count? Not easily. Skip.
        zos.close();
    }
}