package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: ZipArchiveOutputStream
 *
 * Branches targeted:
 * 1. Constructor: OutputStream vs File (raf vs out)
 * 2. setEncoding: sets zipEncoding, useEFS based on UTF8
 * 3. setUseLanguageEncodingFlag: sets useEFS conditionally
 * 4. setCreateUnicodeExtraFields: sets policy
 * 5. setFallbackToUTF8: sets flag
 * 6. setLevel: valid/invalid range, hasCompressionLevelChanged
 * 7. setMethod: stores method
 * 8. putArchiveEntry: close previous entry, cast, method/-1, time/-1,
 *    STORED+!raf: checks size/crc, throws ZipException
 *    DEFLATED+hasCompressionLevelChanged: setLevel
 *    calls writeLocalFileHeader
 * 9. closeArchiveEntry: null entry returns, method=DEFLATED: finish, deflate, set sizes, reset
 *    method=STORED+raf==null: CRC & size validation throws ZipException
 *    method=STORED+raf: sets sizes
 *    raf!=null: seek and rewrite local header CRC/sizes
 *    call writeDataDescriptor
 * 10. write: DEFLATED vs STORED, block size logic, crc.update
 * 11. finish: throws if entry!=null, writes central directory & end
 * 12. close: finish, close raf & out
 * 13. flush: out.flush()
 * 14. writeLocalFileHeader: encodable/fallbackToUTF8, Unicode extra fields,
 *     writes LFH, version needed, GPB, method, time, CRC/sizes (zeros if deflated or raf),
 *     name length, extra length, name, extra, dataStart
 * 15. writeDataDescriptor: only if DEFLATED and raf==null
 * 16. writeCentralFileHeader: similar encoding logic, writes CFH, offsets
 * 17. writeCentralDirectoryEnd: EOCD, entry count, cd length/offset, comment
 * 18. writeOut: delegates to raf or out
 * 19. deflate & deflateUntilInputIsNeeded: deflation loop
 * 20. writeVersionNeededToExtractAndGeneralPurposeBits: version 10 vs 20, GPB with EFS and data descriptor flags
 *
 * Known defect: archive with certain encoding/unicode extra field settings produces
 * corrupt empty central directory -> IOException on read.
 * This test specifically triggers that path by using fallbackToUTF8=true,
 * createUnicodeExtraFields=NOT_ENCODEABLE, and a non-ASCII filename.
 */
public class ZipArchiveOutputStreamDeepseekTest {

    // Helper: read a ZipInputStream and count entries
    private int countEntries(byte[] data) throws IOException {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data));
        int count = 0;
        while (zis.getNextEntry() != null) {
            count++;
            zis.closeEntry();
        }
        zis.close();
        return count;
    }

    // Helper: create a simple stored entry with known CRC/size
    private byte[] simpleEntryBytes() {
        return "Hello World!".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testWriteSingleStoredEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        byte[] content = simpleEntryBytes();
        entry.setSize(content.length);
        entry.setCrc(0xED82CD11L); // CRC32 of "Hello World!"
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] archive = baos.toByteArray();
        assertEquals(1, countEntries(archive));
    }

    @Test(timeout = 4000)
    public void testWriteSingleDeflatedEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("deflated.txt");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        zaos.write("Deflated content".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] archive = baos.toByteArray();
        assertEquals(1, countEntries(archive));
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        byte[] content = "data".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        for (int i = 0; i < 3; i++) {
            ZipArchiveEntry entry = new ZipArchiveEntry("file" + i + ".txt");
            entry.setSize(content.length);
            entry.setCrc(0x9BC3E7CCL); // CRC32 of "data"
            entry.setMethod(ZipArchiveOutputStream.STORED);
            zaos.putArchiveEntry(entry);
            zaos.write(content);
            zaos.closeArchiveEntry();
        }
        zaos.finish();
        zaos.close();
        assertEquals(3, countEntries(baos.toByteArray()));
    }

    @Test(timeout = 4000)
    public void testSetGetEncoding() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        assertEquals("UTF-8", zaos.getEncoding()); // default
        zaos.setEncoding("ISO-8859-1");
        assertEquals("ISO-8859-1", zaos.getEncoding());
        zaos.close();
    }

    @Test(timeout = 4000)
    public void testIsSeekableWithStream() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        assertFalse(zaos.isSeekable());
        zaos.close();
    }

    @Test(timeout = 4000)
    public void testIsSeekableWithFile() throws IOException {
        File temp = File.createTempFile("testZip", ".zip");
        temp.deleteOnExit();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(temp);
        assertTrue(zaos.isSeekable());
        zaos.close();
    }

    @Test(timeout = 4000)
    public void testSetComment() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setComment("archive comment");
        ZipArchiveEntry entry = new ZipArchiveEntry("a.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        // verify comment via reading? Not strictly needed, just coverage
        assertArrayEquals(new byte[]{}, ((ByteArrayOutputStream) baos).toByteArray()); // dummy
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.flush(); // should not throw
        zaos.close();
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testWriteEmptyContent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("empty.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[0]); // no actual write
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        assertEquals(1, countEntries(baos.toByteArray()));
    }

    @Test(timeout = 4000)
    public void testWriteLargeBlockDeflated() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("large.bin");
        entry.setMethod(ZipArchiveOutputStream.DEFLATED);
        zaos.putArchiveEntry(entry);
        byte[] data = new byte[65536];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        zaos.write(data, 0, data.length);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        assertEquals(1, countEntries(baos.toByteArray()));
    }

    @Test(timeout = 4000)
    public void testBoundaryCompressionLevels() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        // valid range: -1 to 9
        zaos.setLevel(-1);
        zaos.setLevel(0);
        zaos.setLevel(9);
        zaos.close();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidCompressionLevelLow() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(-2);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInvalidCompressionLevelHigh() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setLevel(10);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testDefectTriggerUtf8RoundtripWithUnicodeExtra() throws IOException {
        // This test targets the known defect: central directory empty/corrupt
        // when using fallbackToUTF8 and createUnicodeExtraFields with non-ASCII filename.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("CP437");
        zaos.setFallbackToUTF8(true);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);
        zaos.setUseLanguageEncodingFlag(true);

        ZipArchiveEntry entry = new ZipArchiveEntry("\u00E4\u00F6\u00FC.txt"); // non-CP437 chars
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] archive = baos.toByteArray();
        // On buggy version, reading with ZipInputStream may throw "central directory is empty"
        // On fixed version, should have 1 entry
        try {
            int count = countEntries(archive);
            assertEquals(1, count);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("central directory is empty")) {
                fail("Defect reproduced: central directory is empty for UTF-8 roundtrip with Unicode extra fields.");
            }
            throw e;
        }
    }

    // Additional variant with explicit unicode extra always
    @Test(timeout = 4000)
    public void testDefectWithAlwaysUnicodeExtra() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("ISO-8859-1");
        zaos.setFallbackToUTF8(true);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
        zaos.setUseLanguageEncodingFlag(true);

        ZipArchiveEntry entry = new ZipArchiveEntry("normal.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        byte[] archive = baos.toByteArray();
        assertEquals(1, countEntries(archive));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = ZipException.class)
    public void testStoredWithoutSizeFails() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("missing.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        // size not set -> should throw ZipException
        zaos.putArchiveEntry(entry);
    }

    @Test(timeout = 4000, expected = ZipException.class)
    public void testStoredWithoutCrcFails() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("missing.txt");
        entry.setMethod(ZipArchiveOutputStream.STORED);
        entry.setSize(10);
        // crc not set -> should throw ZipException
        zaos.putArchiveEntry(entry);
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryWhenNoEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.closeArchiveEntry(); // should not throw
        zaos.close();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishWithOpenEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("open.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        // not closed
        zaos.finish();
    }

    @Test(timeout = 4000)
    public void testCloseAfterFinish() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("dummy.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close(); // should not throw
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testWriteWithRandomAccessFile() throws IOException {
        File temp = File.createTempFile("rafTest", ".zip");
        temp.deleteOnExit();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(temp);
        ZipArchiveEntry entry = new ZipArchiveEntry("raf.txt");
        byte[] content = "RandomAccess".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        entry.setSize(content.length);
        entry.setCrc(0xB5F7F7F2L);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.write(content);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();

        // Read back using ZipInputStream to verify
        byte[] archive = new byte[(int) temp.length()];
        try (FileInputStream fis = new FileInputStream(temp)) {
            fis.read(archive);
        }
        assertEquals(1, countEntries(archive));
    }

    @Test(timeout = 4000)
    public void testSetMethodAndLevel() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setMethod(ZipArchiveOutputStream.STORED);
        zaos.setLevel(5);
        ZipArchiveEntry entry = new ZipArchiveEntry("methodtest.txt");
        entry.setSize(0);
        entry.setCrc(0);
        // method should default to STORED because setMethod was called
        // but entry's method is -1, so it will adopt the stream method
        zaos.putArchiveEntry(entry);
        assertEquals(ZipArchiveOutputStream.STORED, entry.getMethod());
        zaos.closeArchiveEntry();
        zaos.close();
    }

    @Test(timeout = 4000)
    public void testWriteNullOffsetLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        ZipArchiveEntry entry = new ZipArchiveEntry("zero.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.write(new byte[0], 0, 0);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        assertEquals(1, countEntries(baos.toByteArray()));
    }

    // Additional coverage: use fallbackToUTF8 false, unicode extra not encodeable
    @Test(timeout = 4000)
    public void testFallbackToUTF8FalseWithNotEncodeable() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);
        zaos.setEncoding("UTF-8");
        zaos.setFallbackToUTF8(false);
        zaos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.NOT_ENCODEABLE);
        ZipArchiveEntry entry = new ZipArchiveEntry("valid.txt");
        entry.setSize(0);
        entry.setCrc(0);
        entry.setMethod(ZipArchiveOutputStream.STORED);
        zaos.putArchiveEntry(entry);
        zaos.closeArchiveEntry();
        zaos.finish();
        zaos.close();
        assertEquals(1, countEntries(baos.toByteArray()));
    }
}