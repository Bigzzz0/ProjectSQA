package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream
 *
 * 1. Defect Analysis (Defects4J Ground Truth):
 *    - Defect ID / Symptom: testCpioUnarchiveCreatedByRedlineRpm fails with
 *      "java.lang.IllegalArgumentException: Unknown mode. Full: 1a4 Masked: 0".
 *    - Root Cause: RPMs created by Redline RPM (and certain legacy tools) write CPIO entries
 *      with POSIX file permissions only in the mode field (e.g., 01a4 hex / 0644 octal) without
 *      the file-type bitmask (S_IFMT, e.g., 0100000 for regular file).
 *    - Test Strategy: Construct a CPIO archive entry with mode "000001a4" (hex), verify reading
 *      via getNextEntry() or getNextCPIOEntry().
 *
 * 2. Decision Branches Covered:
 *    - matches():
 *      * length < 6 -> false
 *      * binary magic LE (0x71, 0xc7) -> true
 *      * binary magic BE (0xc7, 0x71) -> true
 *      * ASCII magic prefixes ("30 37 30 37 30"):
 *        - '1' (MAGIC_NEW 070701) -> true
 *        - '2' (MAGIC_NEW_CRC 070702) -> true
 *        - '7' (MAGIC_OLD_ASCII 070707) -> true
 *        - other 6th char ('3', '0', etc.) -> false
 *      * signature prefix mismatches at indices 0, 1, 2, 3, 4 -> false
 *    - getNextCPIOEntry():
 *      * closed stream check -> IOException
 *      * entry != null (auto close previous entry)
 *      * Magic detection:
 *        - MAGIC_OLD_BINARY (swapHalfWord = false)
 *        - MAGIC_OLD_BINARY (swapHalfWord = true)
 *        - MAGIC_NEW ("070701")
 *        - MAGIC_NEW_CRC ("070702")
 *        - MAGIC_OLD_ASCII ("070707")
 *        - Unknown magic -> IOException
 *      * Mode == 0 and name != TRAILER!!! -> IOException
 *      * Mode == 0 and name == TRAILER!!! -> skipRemainderOfLastBlock(), return null
 *    - read(byte[], int, off):
 *      * closed stream -> IOException
 *      * invalid bounds: off < 0, len < 0, off > b.length - len -> IndexOutOfBoundsException
 *      * len == 0 -> return 0
 *      * entry == null or entryEOF -> return -1
 *      * entryBytesRead == entry.getSize() -> skip data pad, CRC check, entryEOF = true, return -1
 *      * CRC error verification on FORMAT_NEW_CRC: mismatch -> IOException, match -> success
 *      * readFully() EOF before len bytes -> EOFException
 *    - skip(long):
 *      * n < 0 -> IllegalArgumentException
 *      * n == 0 -> 0
 *      * n > 0 within entry data, boundary caps at Math.min(n, Integer.MAX_VALUE)
 *    - available():
 *      * closed -> IOException
 *      * entryEOF == true -> 0
 *      * entryEOF == false -> 1
 *    - close():
 *      * multiple invocations idempotent
 */
public class CpioArchiveInputStreamGeminiTest {

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadSingleEntryNewFormat() throws Exception {
        byte[] content = "Hello CPIO World".getBytes("UTF-8");
        byte[] archive = createNewArchive("file1.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertEquals(1, in.available());

        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull("Entry should not be null", entry);
        assertEquals("file1.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        assertEquals(CpioConstants.FORMAT_NEW, entry.getFormat());

        byte[] readBuffer = new byte[content.length];
        int bytesRead = in.read(readBuffer, 0, readBuffer.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuffer);

        // Next read triggers entry EOF
        assertEquals(-1, in.read(readBuffer, 0, 1));
        assertEquals(0, in.available());

        // Next entry should be null (due to TRAILER!!!)
        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadMultiEntryAndAutoClosePrevious() throws Exception {
        byte[] content1 = "FirstFilePayload".getBytes("UTF-8");
        byte[] content2 = "SecondFilePayloadData".getBytes("UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryRaw(baos, "first.txt", 0100644, content1, false);
        writeNewEntryRaw(baos, "second.txt", 0100644, content2, false);
        writeTrailerNewRaw(baos, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));

        CpioArchiveEntry e1 = in.getNextCPIOEntry();
        assertNotNull(e1);
        assertEquals("first.txt", e1.getName());

        // Intentionally do NOT read e1 payload to verify getNextCPIOEntry closes it automatically
        CpioArchiveEntry e2 = in.getNextCPIOEntry();
        assertNotNull(e2);
        assertEquals("second.txt", e2.getName());

        byte[] buf2 = new byte[content2.length];
        int read2 = in.read(buf2, 0, buf2.length);
        assertEquals(content2.length, read2);
        assertArrayEquals(content2, buf2);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadOldAsciiFormat() throws Exception {
        byte[] content = "Old Ascii Content".getBytes("UTF-8");
        byte[] archive = createOldAsciiArchive("ascii.txt", 0100644, content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("ascii.txt", entry.getName());
        assertEquals(CpioConstants.FORMAT_OLD_ASCII, entry.getFormat());
        assertEquals(content.length, entry.getSize());

        byte[] buf = new byte[content.length];
        int read = in.read(buf, 0, buf.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buf);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadOldBinaryFormatBigEndian() throws Exception {
        byte[] content = "Binary Big Endian".getBytes("UTF-8");
        byte[] archive = createOldBinaryArchive("binBE.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("binBE.txt", entry.getName());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, entry.getFormat());
        assertEquals(content.length, entry.getSize());

        byte[] buf = new byte[content.length];
        int read = in.read(buf, 0, buf.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buf);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadOldBinaryFormatLittleEndian() throws Exception {
        byte[] content = "Binary Little Endian".getBytes("UTF-8");
        byte[] archive = createOldBinaryArchive("binLE.txt", 0100644, content, true);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("binLE.txt", entry.getName());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, entry.getFormat());
        assertEquals(content.length, entry.getSize());

        byte[] buf = new byte[content.length];
        int read = in.read(buf, 0, buf.length);
        assertEquals(content.length, read);
        assertArrayEquals(content, buf);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntry() throws Exception {
        byte[] content = "0123456789ABCDEF".getBytes("UTF-8");
        byte[] archive = createNewArchive("skip.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextCPIOEntry());

        long skipped = in.skip(5);
        assertEquals(5L, skipped);

        byte[] remaining = new byte[11];
        int read = in.read(remaining, 0, remaining.length);
        assertEquals(11, read);
        assertEquals("56789ABCDEF", new String(remaining, "UTF-8"));

        in.close();
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadZeroLengthBuffer() throws Exception {
        byte[] content = "Data".getBytes("UTF-8");
        byte[] archive = createNewArchive("zero.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextCPIOEntry());

        byte[] buf = new byte[10];
        int read = in.read(buf, 0, 0);
        assertEquals(0, read);

        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipZeroBytes() throws Exception {
        byte[] content = "Data".getBytes("UTF-8");
        byte[] archive = createNewArchive("zero_skip.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextCPIOEntry());

        assertEquals(0L, in.skip(0));
        in.close();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSkipNegativeThrowsException() throws Exception {
        byte[] content = "Data".getBytes("UTF-8");
        byte[] archive = createNewArchive("neg_skip.txt", 0100644, content, false);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextCPIOEntry());
        in.skip(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadInvalidBoundsNegativeOffset() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.read(new byte[10], -1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadInvalidBoundsNegativeLength() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.read(new byte[10], 0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testReadInvalidBoundsOverflow() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.read(new byte[10], 8, 5);
    }

    @Test(timeout = 4000)
    public void testReadWhenNoEntryActiveReturnsMinusOne() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        assertEquals(-1, in.read(new byte[10], 0, 5));
    }

    @Test(timeout = 4000)
    public void testMatchesBoundaries() {
        assertFalse(CpioArchiveInputStream.matches(new byte[]{'0', '7', '0', '7', '0'}, 5));

        byte[] oldBinLE = new byte[]{(byte) 0x71, (byte) 0xc7, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(oldBinLE, 6));

        byte[] oldBinBE = new byte[]{(byte) 0xc7, (byte) 0x71, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(oldBinBE, 6));

        byte[] newAscii = new byte[]{'0', '7', '0', '7', '0', '1'};
        assertTrue(CpioArchiveInputStream.matches(newAscii, 6));

        byte[] newCrc = new byte[]{'0', '7', '0', '7', '0', '2'};
        assertTrue(CpioArchiveInputStream.matches(newCrc, 6));

        byte[] oldAscii = new byte[]{'0', '7', '0', '7', '0', '7'};
        assertTrue(CpioArchiveInputStream.matches(oldAscii, 6));

        byte[] badPrefix0 = new byte[]{'1', '7', '0', '7', '0', '1'};
        assertFalse(CpioArchiveInputStream.matches(badPrefix0, 6));

        byte[] badPrefix1 = new byte[]{'0', '6', '0', '7', '0', '1'};
        assertFalse(CpioArchiveInputStream.matches(badPrefix1, 6));

        byte[] badPrefix2 = new byte[]{'0', '7', '1', '7', '0', '1'};
        assertFalse(CpioArchiveInputStream.matches(badPrefix2, 6));

        byte[] badPrefix3 = new byte[]{'0', '7', '0', '8', '0', '1'};
        assertFalse(CpioArchiveInputStream.matches(badPrefix3, 6));

        byte[] badPrefix4 = new byte[]{'0', '7', '0', '7', '1', '1'};
        assertFalse(CpioArchiveInputStream.matches(badPrefix4, 6));

        byte[] badSuffix = new byte[]{'0', '7', '0', '7', '0', '9'};
        assertFalse(CpioArchiveInputStream.matches(badSuffix, 6));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J Ground Truth:
     * CpioArchiveInputStreamTest::testCpioUnarchiveCreatedByRedlineRpm
     * Trigger: entry mode containing only permissions without file type mask (e.g. 01a4 hex).
     * The implementation must accept this entry without throwing:
     * java.lang.IllegalArgumentException: Unknown mode. Full: 1a4 Masked: 0
     */
    @Test(timeout = 4000)
    public void testCpioUnarchiveCreatedByRedlineRpm() throws Exception {
        byte[] payload = "Redline RPM Test Payload".getBytes("UTF-8");
        // Mode 01a4 hex = 0644 permissions without S_IFREG bitmask
        byte[] archive = createNewArchiveRawMode("rpm_entry.txt", "000001a4", payload);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull("Archive entry created by Redline RPM with mode 01a4 should be read successfully", entry);
        assertEquals("rpm_entry.txt", entry.getName());
        assertEquals(0x1a4L, entry.getMode());

        byte[] readData = new byte[payload.length];
        int count = in.read(readData, 0, readData.length);
        assertEquals(payload.length, count);
        assertArrayEquals(payload, readData);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testNewEntryCrcValidationSuccess() throws Exception {
        byte[] payload = "CheckCRCValid".getBytes("UTF-8");
        long crc = 0;
        for (byte b : payload) {
            crc += (b & 0xFF);
        }
        byte[] archive = createNewCrcArchive("crc_ok.txt", 0100644, payload, crc);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals(CpioConstants.FORMAT_NEW_CRC, entry.getFormat());

        byte[] buf = new byte[payload.length];
        int read = in.read(buf, 0, buf.length);
        assertEquals(payload.length, read);

        // Reading past EOF will verify CRC
        assertEquals(-1, in.read(buf, 0, 1));
        in.close();
    }

    @Test(timeout = 4000)
    public void testNewEntryCrcValidationFailure() throws Exception {
        byte[] payload = "CorruptedCRCData".getBytes("UTF-8");
        long wrongCrc = 0x12345678L; // incorrect CRC
        byte[] archive = createNewCrcArchive("crc_bad.txt", 0100644, payload, wrongCrc);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextCPIOEntry());

        byte[] buf = new byte[payload.length];
        in.read(buf, 0, buf.length);

        try {
            in.read(buf, 0, 1);
            fail("Expected IOException due to CRC Error");
        } catch (IOException e) {
            assertTrue("Exception message should mention CRC Error", e.getMessage().contains("CRC Error"));
        } finally {
            in.close();
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IOException.class, timeout = 4000)
    public void testUnknownMagicThrowsIOException() throws Exception {
        byte[] badMagic = "999999000000000000".getBytes("UTF-8");
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(badMagic));
        in.getNextCPIOEntry();
    }

    @Test(expected = EOFException.class, timeout = 4000)
    public void testTruncatedStreamThrowsEOFException() throws Exception {
        byte[] truncated = new byte[]{'0', '7', '0', '7'};
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        in.getNextCPIOEntry();
    }

    @Test(timeout = 4000)
    public void testModeZeroNotAllowedExceptTrailerInNewEntry() throws Exception {
        // Mode 0 with non-trailer name should throw IOException
        byte[] archive = createNewArchiveRawMode("not_trailer.txt", "00000000", new byte[0]);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        try {
            in.getNextCPIOEntry();
            fail("Expected IOException for mode 0 on non-trailer entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testModeZeroNotAllowedExceptTrailerInOldAsciiEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeAsciiHeader(baos, "070707", 0, 0, 0, 0, 0, 1, 0, 0, "non_trailer.txt".length() + 1, 0);
        baos.write("non_trailer.txt\0".getBytes("UTF-8"));
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
            fail("Expected IOException for mode 0 on non-trailer entry in old ASCII format");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testModeZeroNotAllowedExceptTrailerInOldBinaryEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeOldBinaryHeaderRaw(baos, 0, 0, 0, 0, 0, 1, 0, 0, "non_trailer_bin.txt".length() + 1, 0, false);
        baos.write("non_trailer_bin.txt\0".getBytes("UTF-8"));
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
            fail("Expected IOException for mode 0 on non-trailer entry in old binary format");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
        } finally {
            in.close();
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Stream Contract Integrity
    // -------------------------------------------------------------------------

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenOnGetNextEntry() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.getNextCPIOEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenOnRead() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.read(new byte[10], 0, 5);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenOnAvailable() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.available();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testEnsureOpenOnSkip() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.skip(10);
    }

    @Test(timeout = 4000)
    public void testCloseIsIdempotent() throws Exception {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();
        in.close(); // Subsequent invocation must not throw exception
    }

    // -------------------------------------------------------------------------
    // Test Helpers for constructing CPIO wire frames
    // -------------------------------------------------------------------------

    private static String padHex(long val, int length) {
        String s = Long.toHexString(val);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }

    private static String padOctal(long val, int length) {
        String s = Long.toOctalString(val);
        while (s.length() < length) {
            s = "0" + s;
        }
        return s;
    }

    private static byte[] createNewArchive(String name, long mode, byte[] content, boolean hasCrc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeNewEntryRaw(baos, name, mode, content, hasCrc);
        writeTrailerNewRaw(baos, hasCrc);
        return baos.toByteArray();
    }

    private static byte[] createNewArchiveRawMode(String name, String hexMode, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String magic = "070701";
        baos.write(magic.getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII")); // inode
        baos.write(hexMode.getBytes("US-ASCII"));       // raw mode
        baos.write(padHex(1000, 8).getBytes("US-ASCII")); // uid
        baos.write(padHex(1000, 8).getBytes("US-ASCII")); // gid
        baos.write(padHex(1, 8).getBytes("US-ASCII"));    // nlink
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // mtime
        baos.write(padHex(content.length, 8).getBytes("US-ASCII")); // size
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // devmajor
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // devminor
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // rdevmajor
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // rdevminor
        baos.write(padHex(name.length() + 1, 8).getBytes("US-ASCII")); // namesize
        baos.write(padHex(0, 8).getBytes("US-ASCII"));    // chksum

        baos.write(name.getBytes("UTF-8"));
        baos.write(0);
        int headerPad = (4 - ((110 + name.length() + 1) % 4)) % 4;
        baos.write(new byte[headerPad]);

        baos.write(content);
        int dataPad = (4 - (content.length % 4)) % 4;
        baos.write(new byte[dataPad]);

        writeTrailerNewRaw(baos, false);
        return baos.toByteArray();
    }

    private static byte[] createNewCrcArchive(String name, long mode, byte[] content, long crc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String magic = "070702";
        baos.write(magic.getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII"));
        baos.write(padHex(mode, 8).getBytes("US-ASCII"));
        baos.write(padHex(1000, 8).getBytes("US-ASCII"));
        baos.write(padHex(1000, 8).getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(content.length, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(name.length() + 1, 8).getBytes("US-ASCII"));
        baos.write(padHex(crc, 8).getBytes("US-ASCII"));

        baos.write(name.getBytes("UTF-8"));
        baos.write(0);
        int headerPad = (4 - ((110 + name.length() + 1) % 4)) % 4;
        baos.write(new byte[headerPad]);

        baos.write(content);
        int dataPad = (4 - (content.length % 4)) % 4;
        baos.write(new byte[dataPad]);

        writeTrailerNewRaw(baos, true);
        return baos.toByteArray();
    }

    private static void writeNewEntryRaw(ByteArrayOutputStream baos, String name, long mode, byte[] content, boolean hasCrc) throws IOException {
        String magic = hasCrc ? "070702" : "070701";
        baos.write(magic.getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII"));
        baos.write(padHex(mode, 8).getBytes("US-ASCII"));
        baos.write(padHex(1000, 8).getBytes("US-ASCII"));
        baos.write(padHex(1000, 8).getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(content.length, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(name.length() + 1, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));

        baos.write(name.getBytes("UTF-8"));
        baos.write(0);
        int headerPad = (4 - ((110 + name.length() + 1) % 4)) % 4;
        baos.write(new byte[headerPad]);

        baos.write(content);
        int dataPad = (4 - (content.length % 4)) % 4;
        baos.write(new byte[dataPad]);
    }

    private static void writeTrailerNewRaw(ByteArrayOutputStream baos, boolean hasCrc) throws IOException {
        String magic = hasCrc ? "070702" : "070701";
        baos.write(magic.getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII")); // mode 0 for trailer
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(1, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));
        baos.write(padHex(CpioConstants.CPIO_TRAILER.length() + 1, 8).getBytes("US-ASCII"));
        baos.write(padHex(0, 8).getBytes("US-ASCII"));

        baos.write(CpioConstants.CPIO_TRAILER.getBytes("UTF-8"));
        baos.write(0);
        int headerPad = (4 - ((110 + CpioConstants.CPIO_TRAILER.length() + 1) % 4)) % 4;
        baos.write(new byte[headerPad]);

        int totalWritten = baos.size();
        int blockRem = totalWritten % CpioConstants.BLOCK_SIZE;
        if (blockRem != 0) {
            baos.write(new byte[CpioConstants.BLOCK_SIZE - blockRem]);
        }
    }

    private static byte[] createOldAsciiArchive(String name, long mode, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeAsciiHeader(baos, "070707", 0, 1, mode, 1000, 1000, 1, 0, 0, name.length() + 1, content.length);
        baos.write(name.getBytes("UTF-8"));
        baos.write(0);
        baos.write(content);

        // Trailer
        writeAsciiHeader(baos, "070707", 0, 0, 0, 0, 0, 1, 0, 0, CpioConstants.CPIO_TRAILER.length() + 1, 0);
        baos.write(CpioConstants.CPIO_TRAILER.getBytes("UTF-8"));
        baos.write(0);

        int totalWritten = baos.size();
        int blockRem = totalWritten % CpioConstants.BLOCK_SIZE;
        if (blockRem != 0) {
            baos.write(new byte[CpioConstants.BLOCK_SIZE - blockRem]);
        }
        return baos.toByteArray();
    }

    private static void writeAsciiHeader(ByteArrayOutputStream baos, String magic, long dev, long ino, long mode,
                                        long uid, long gid, long nlink, long rdev, long mtime,
                                        long namesize, long filesize) throws IOException {
        baos.write(magic.getBytes("US-ASCII"));
        baos.write(padOctal(dev, 6).getBytes("US-ASCII"));
        baos.write(padOctal(ino, 6).getBytes("US-ASCII"));
        baos.write(padOctal(mode, 6).getBytes("US-ASCII"));
        baos.write(padOctal(uid, 6).getBytes("US-ASCII"));
        baos.write(padOctal(gid, 6).getBytes("US-ASCII"));
        baos.write(padOctal(nlink, 6).getBytes("US-ASCII"));
        baos.write(padOctal(rdev, 6).getBytes("US-ASCII"));
        baos.write(padOctal(mtime, 11).getBytes("US-ASCII"));
        baos.write(padOctal(namesize, 6).getBytes("US-ASCII"));
        baos.write(padOctal(filesize, 11).getBytes("US-ASCII"));
    }

    private static byte[] createOldBinaryArchive(String name, long mode, byte[] content, boolean swapBytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeOldBinaryHeaderRaw(baos, 0, 1, mode, 1000, 1000, 1, 0, 0, name.length() + 1, content.length, swapBytes);
        baos.write(name.getBytes("UTF-8"));
        baos.write(0);
        int pad = (name.length() + 1) % 2;
        if (pad != 0) {
            baos.write(0);
        }
        baos.write(content);
        int dataPad = content.length % 2;
        if (dataPad != 0) {
            baos.write(0);
        }

        // Trailer
        writeOldBinaryHeaderRaw(baos, 0, 0, 0, 0, 0, 1, 0, 0, CpioConstants.CPIO_TRAILER.length() + 1, 0, swapBytes);
        baos.write(CpioConstants.CPIO_TRAILER.getBytes("UTF-8"));
        baos.write(0);
        int trailerPad = (CpioConstants.CPIO_TRAILER.length() + 1) % 2;
        if (trailerPad != 0) {
            baos.write(0);
        }

        int totalWritten = baos.size();
        int blockRem = totalWritten % CpioConstants.BLOCK_SIZE;
        if (blockRem != 0) {
            baos.write(new byte[CpioConstants.BLOCK_SIZE - blockRem]);
        }
        return baos.toByteArray();
    }

    private static void writeShort(ByteArrayOutputStream baos, int val, boolean swap) {
        byte b0 = (byte) ((val >> 8) & 0xFF);
        byte b1 = (byte) (val & 0xFF);
        if (swap) {
            baos.write(b1);
            baos.write(b0);
        } else {
            baos.write(b0);
            baos.write(b1);
        }
    }

    private static void writeInt(ByteArrayOutputStream baos, long val, boolean swap) {
        int w0 = (int) ((val >> 16) & 0xFFFF);
        int w1 = (int) (val & 0xFFFF);
        if (swap) {
            writeShort(baos, w1, true);
            writeShort(baos, w0, true);
        } else {
            writeShort(baos, w0, false);
            writeShort(baos, w1, false);
        }
    }

    private static void writeOldBinaryHeaderRaw(ByteArrayOutputStream baos, int dev, int ino, long mode,
                                               int uid, int gid, int nlink, int rdev, long mtime,
                                               int namesize, long filesize, boolean swap) {
        writeShort(baos, (int) CpioConstants.MAGIC_OLD_BINARY, swap);
        writeShort(baos, dev, swap);
        writeShort(baos, ino, swap);
        writeShort(baos, (int) mode, swap);
        writeShort(baos, uid, swap);
        writeShort(baos, gid, swap);
        writeShort(baos, nlink, swap);
        writeShort(baos, rdev, swap);
        writeInt(baos, mtime, swap);
        writeShort(baos, namesize, swap);
        writeInt(baos, filesize, swap);
    }
}