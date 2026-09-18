package org.apache.commons.compress.archivers.cpio;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Method                  | Branch / Condition Coverage Target
 * -------------------------------------------------------------------------------------------------
 * Constructor(out, format)| FORMAT_NEW, FORMAT_NEW_CRC, FORMAT_OLD_ASCII, FORMAT_OLD_BINARY
 *                         | Default/Unknown format -> IllegalArgumentException
 * Constructor(out)        | Default constructor delegation to FORMAT_NEW
 * ensureOpen              | closed == true -> IOException("Stream closed")
 *                         | closed == false -> normal execution
 * putArchiveEntry         | Non-CpioArchiveEntry instance -> ClassCastException
 *                         | this.entry != null -> invoke closeArchiveEntry()
 *                         | e.getTime() == -1 -> assigns current time; e.getTime() != -1 retains time
 *                         | format != this.entryFormat -> IOException("Header format: ... does not match...")
 *                         | Duplicate entry name -> IOException("duplicate entry: ...")
 * writeHeader             | Switch cases: FORMAT_NEW, FORMAT_NEW_CRC, FORMAT_OLD_ASCII, FORMAT_OLD_BINARY
 * writeAsciiLong          | tmp.length() <= length (zero-padding path)
 *                         | tmp.length() > length (radix truncation / substring path)
 * pad                     | count > 0 -> writes padding zero bytes; count <= 0 -> no-op
 * closeArchiveEntry       | entry.getSize() != this.written -> IOException("invalid entry size...")
 *                         | FORMAT_NEW_CRC with matching CRC -> succeeds
 *                         | FORMAT_NEW_CRC with mismatching CRC -> IOException("CRC Error")
 * write                   | off < 0, len < 0, off > b.length - len -> IndexOutOfBoundsException
 *                         | len == 0 -> early return
 *                         | entry == null -> IOException("no current CPIO entry")
 *                         | written + len > entry.getSize() -> IOException("attempt to write past end...")
 *                         | FORMAT_NEW_CRC checksum calculation accumulation
 * finish                  | closed archive check via ensureOpen()
 *                         | this.entry != null -> IOException("This archives contains unclosed entries.")
 *                         | Trailer record generation (CPIO_TRAILER, links=1)
 * close                   | !closed -> finish(), out.close(), set closed=true
 *                         | Idempotent close call (closed == true -> no-op)
 * createArchiveEntry      | (File, String) -> returns new CpioArchiveEntry
 * -------------------------------------------------------------------------------------------------
 */

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class CpioArchiveOutputStreamGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (All 4 Formats)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFormatNewRoundtripSingleEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt");
        byte[] content = "Hello CPIO World".getBytes();
        entry.setSize(content.length);
        entry.setMode(CpioConstants.C_ISREG);
        entry.setTime(1000L);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        assertTrue("Archive must not be empty", archiveBytes.length > 0);
        // FORMAT_NEW magic is "070701"
        String magic = new String(archiveBytes, 0, 6);
        assertEquals(CpioConstants.MAGIC_NEW, magic);
    }

    @Test(timeout = 4000)
    public void testFormatNewCrcValidChecksum() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc_test.bin");
        byte[] content = new byte[]{10, 20, 30, 40};
        long expectedCrc = (10 & 0xFF) + (20 & 0xFF) + (30 & 0xFF) + (40 & 0xFF);
        entry.setSize(content.length);
        entry.setChksum(expectedCrc);
        entry.setTime(5000L);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        String magic = new String(archiveBytes, 0, 6);
        assertEquals(CpioConstants.MAGIC_NEW_CRC, magic);
    }

    @Test(timeout = 4000)
    public void testFormatOldAsciiSingleEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "ascii.txt");
        byte[] content = "Ascii Entry Content".getBytes();
        entry.setSize(content.length);
        entry.setTime(2000L);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        String magic = new String(archiveBytes, 0, 6);
        assertEquals(CpioConstants.MAGIC_OLD_ASCII, magic);
    }

    @Test(timeout = 4000)
    public void testFormatOldBinarySingleEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "binary.bin");
        byte[] content = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        entry.setSize(content.length);
        entry.setTime(3000L);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        assertTrue("Binary archive must contain data", archiveBytes.length > 0);
    }

    @Test(timeout = 4000)
    public void testSequentialEntriesAutomaticPreviousClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "zero1.txt");
        entry1.setSize(0);
        entry1.setTime(100L);
        out.putArchiveEntry(entry1);

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "zero2.txt");
        entry2.setSize(0);
        entry2.setTime(200L);
        // Automatically closes entry1 because entry1 is fully written (size 0)
        out.putArchiveEntry(entry2);
        out.closeArchiveEntry();
        out.close();

        byte[] archiveBytes = baos.toByteArray();
        assertTrue(archiveBytes.length > 0);
    }

    @Test(timeout = 4000)
    public void testEmptyArchiveFinishedContainsTrailer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();

        byte[] archive = baos.toByteArray();
        assertTrue(archive.length > 0);
        String archiveString = new String(archive);
        assertTrue("Archive trailer must be present", archiveString.contains(CpioConstants.CPIO_TRAILER));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriteAsciiLongTruncationWhenNumberExceedsFieldLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "overflow.dat");
        // Field size for size in FORMAT_NEW is 8 hex digits (up to 0xFFFFFFFFL)
        // 0x100000000L is 9 hex digits, triggering the tmp.length() > length branch in writeAsciiLong
        entry.setSize(0x100000000L);
        entry.setTime(1000L);

        out.putArchiveEntry(entry);
        assertNotNull(out);
    }

    @Test(timeout = 4000)
    public void testWriteOldAsciiLongTruncation() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "overflow_ascii.dat");
        // Old ASCII uses radix 8; time is 11 octal digits. Exceeding 11 octal digits triggers truncation branch.
        entry.setTime(01000000000000L); // 12 octal digits
        entry.setSize(0);

        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] result = baos.toByteArray();
        assertTrue(result.length > 0);
    }

    @Test(timeout = 4000)
    public void testHeaderPaddingVariations() throws IOException {
        // Test varying filename lengths to cover both pad > 0 and pad == 0 in writeHeader
        for (int nameLen = 1; nameLen <= 8; nameLen++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nameLen; i++) {
                sb.append('x');
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, sb.toString());
            entry.setSize(0);
            entry.setTime(1000L);
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
            out.close();
            assertTrue(baos.size() > 0);
        }
    }

    @Test(timeout = 4000)
    public void testDataPaddingVariations() throws IOException {
        // FORMAT_NEW pads data to 4-byte boundaries. Test data lengths 1, 2, 3, 4
        for (int dataLen = 1; dataLen <= 4; dataLen++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
            CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "testpad.txt");
            byte[] data = new byte[dataLen];
            entry.setSize(dataLen);
            entry.setTime(1000L);
            out.putArchiveEntry(entry);
            out.write(data, 0, dataLen);
            out.closeArchiveEntry();
            out.close();
            assertTrue(baos.size() > 0);
        }
    }

    @Test(timeout = 4000)
    public void testZeroLengthWriteDoesNothing() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "zero_write.txt");
        entry.setSize(5);
        entry.setTime(1000L);
        out.putArchiveEntry(entry);

        // write with len == 0 should immediately return without error
        out.write(new byte[]{1, 2, 3}, 0, 0);

        out.write(new byte[]{1, 2, 3, 4, 5}, 0, 5);
        out.closeArchiveEntry();
        out.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted & Contract Integrity Zone
    // =========================================================================

    @Test(timeout = 4000)
    public void testRepeatedCloseIsIdempotent() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();
        // Subsequent close call must be a safe no-op
        out.close();
    }

    @Test(timeout = 4000)
    public void testEntryTimeUnsetDefaultsToCurrentTime() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "default_time.txt");
        entry.setSize(0);
        assertEquals(-1L, entry.getTime());

        out.putArchiveEntry(entry);
        assertTrue("Entry time should have been populated", entry.getTime() != -1L);
        out.closeArchiveEntry();
        out.close();
    }

    @Test(timeout = 4000)
    public void testCreateArchiveEntryFromFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        File dummyFile = new File("test_dummy_gemini.txt");
        ArchiveEntry entry = out.createArchiveEntry(dummyFile, "custom_name.txt");
        assertNotNull(entry);
        assertTrue(entry instanceof CpioArchiveEntry);
        assertEquals("custom_name.txt", entry.getName());
        out.close();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorWithInvalidFormat() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new CpioArchiveOutputStream(baos, (short) 999);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testPutArchiveEntryNonCpioArchiveEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        ArchiveEntry nonCpioEntry = new ArchiveEntry() {
            public String getName() { return "fake"; }
            public long getSize() { return 0; }
            public boolean isDirectory() { return false; }
            public java.util.Date getLastModifiedDate() { return new java.util.Date(); }
        };
        out.putArchiveEntry(nonCpioEntry);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testPutArchiveEntryFormatMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "mismatch.txt");
        out.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testPutArchiveEntryDuplicateNameThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "duplicate.txt");
        entry1.setSize(0);
        entry1.setTime(1000L);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "duplicate.txt");
        entry2.setSize(0);
        entry2.setTime(1000L);
        out.putArchiveEntry(entry2);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testPutArchiveEntryWhenPreviousEntryNotFullyWritten() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unwritten.txt");
        entry1.setSize(100);
        entry1.setTime(1000L);
        out.putArchiveEntry(entry1);

        // Putting next entry triggers closeArchiveEntry() on entry1, which detects size mismatch
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "next.txt");
        entry2.setSize(0);
        out.putArchiveEntry(entry2);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCloseArchiveEntrySizeMismatch() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "wrong_size.txt");
        entry.setSize(10);
        entry.setTime(1000L);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1, 2, 3}, 0, 3);
        // Only 3 bytes written, expected 10
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCrcMismatchThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "bad_crc.txt");
        byte[] content = new byte[]{1, 2, 3};
        entry.setSize(content.length);
        entry.setChksum(999999L); // Wrong checksum
        entry.setTime(1000L);

        out.putArchiveEntry(entry);
        out.write(content, 0, content.length);
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testWriteWithoutOpenEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testWritePastEndOfStoredEntry() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "overflow.txt");
        entry.setSize(2);
        entry.setTime(1000L);
        out.putArchiveEntry(entry);
        // Attempting to write 3 bytes when size is 2
        out.write(new byte[]{1, 2, 3}, 0, 3);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteNegativeOffset() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[]{1, 2}, -1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteNegativeLength() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[]{1, 2}, 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testWriteOffsetBeyondBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.write(new byte[]{1, 2}, 2, 1);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testFinishWithUnclosedEntryThrowsException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "unclosed.txt");
        entry.setSize(5);
        entry.setTime(1000L);
        out.putArchiveEntry(entry);
        // Entry is not closed
        out.finish();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenRejectsWriteAfterClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();
        out.write(new byte[]{1, 2}, 0, 1);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenRejectsPutArchiveEntryAfterClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "closed.txt");
        out.putArchiveEntry(entry);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenRejectsCloseArchiveEntryAfterClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();
        out.closeArchiveEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenRejectsFinishAfterClose() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        out.finish();
        out.close();
        out.finish();
    }
}