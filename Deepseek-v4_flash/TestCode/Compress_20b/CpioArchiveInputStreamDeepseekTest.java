package org.apache.commons.compress.archivers.cpio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Test;

/**
 * Advanced White-Box test suite for CpioArchiveInputStream.
 * <p>
 * [Branch & Defect Analysis Matrix]
 * <ul>
 *   <li>Coverage targets: line, branch, exception paths, state transitions</li>
 *   <li>Defect-specific: mode 0x1a4 (420) triggers IllegalArgumentException in CpioArchiveEntry.setMode
 *       when file type bits are missing. Test constructs a new-format entry with mode=420 and expects
 *       successful read; the defective version will throw.</li>
 *   <li>Boundaries: magic checks, header padding, data padding, CRC, EOF, closed stream, illegal arguments</li>
 * </ul>
 * </p>
 */
public class CpioArchiveInputStreamDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadNewEntryNoCrc() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Should return entry", e);
            assertEquals("regulardir/myfile.txt", e.getName());
            assertEquals(0100644L, e.getMode());
            assertEquals(2, e.getUID());
            assertEquals(3, e.getGID());
            long size = e.getSize();
            assertTrue("Size should be > 0", size > 0);
            byte[] buf = new byte[(int) size];
            int read = cis.read(buf, 0, buf.length);
            assertEquals("Read full content", size, read);
            byte[] expectedContent = new byte[] { 0x41, 0x42, 0x43 };
            assertArrayEquals("Content", expectedContent, Arrays.copyOf(buf, read));
            // next entry should be trailer -> null
            assertNull("Trailer should return null", cis.getNextCPIOEntry());
            // after trailer, read returns -1
            assertEquals("Read after EOF", -1, cis.read(buf, 0, 1));
        }
    }

    @Test(timeout = 4000)
    public void testReadNewEntryWithCrc() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(true, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Should return entry", e);
            assertEquals("regulardir/myfile.txt", e.getName());
            assertEquals(0100644L, e.getMode());
            // read all data
            byte[] buf = new byte[(int) e.getSize()];
            cis.read(buf, 0, buf.length);
            // CRC check happens at end-of-entry, no exception expected
            // now read the trailer
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadOldAsciiEntry() throws IOException {
        byte[] archive = createOldAsciiEntry();
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Old ASCII entry", e);
            assertEquals("file.txt", e.getName());
            assertEquals(0100644L, e.getMode());
            byte[] buf = new byte[(int) e.getSize()];
            cis.read(buf, 0, buf.length);
            assertArrayEquals("Content", new byte[] { 0x44 }, buf);
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadOldBinaryEntrySwapped() throws IOException {
        byte[] archive = createOldBinaryEntry(true);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Old binary swapped entry", e);
            assertEquals("data.bin", e.getName());
            assertEquals(0100644L, e.getMode());
            cis.skip(e.getSize());
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    @Test(timeout = 4000)
    public void testReadOldBinaryEntryNotSwapped() throws IOException {
        byte[] archive = createOldBinaryEntry(false);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Old binary not swapped entry", e);
            assertEquals("data.bin", e.getName());
            assertEquals(0100644L, e.getMode());
            cis.skip(e.getSize());
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeOff() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            cis.read(new byte[10], -1, 5);
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadOffTooLarge() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            cis.read(new byte[10], 8, 5);
        }
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadNegativeLen() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            cis.read(new byte[10], 0, -1);
        }
    }

    @Test(timeout = 4000)
    public void testReadZeroLen() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            assertEquals("Zero length read returns 0", 0, cis.read(new byte[10], 0, 0));
        }
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadOnClosedStream() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.close();
        cis.read(new byte[1], 0, 1);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testAvailableOnClosedStream() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.close();
        cis.available();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testGetNextEntryOnClosedStream() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.close();
        cis.getNextEntry();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegative() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.skip(-1);
    }

    @Test(timeout = 4000)
    public void testSkipOnClosedStream() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.close();
        try {
            cis.skip(10);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("Stream closed", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAvailableBeforeAndAfterEntryEOF() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(createNewEntry(false, 0100644L));
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            assertEquals("Before getNextEntry", 1, cis.available());
            cis.getNextCPIOEntry();
            assertEquals("Entry data available", 1, cis.available());
            byte[] buf = new byte[1024];
            while (cis.read(buf, 0, buf.length) != -1) ;
            assertEquals("After entry EOF", 0, cis.available());
        }
    }

    @Test(timeout = 4000)
    public void testMultipleEntriesAndTrailer() throws IOException {
        // build a stream with two entries plus trailer
        byte[] entry1 = createNewEntryData(false, "file1.dat", 4L, 0100644L, new byte[]{0x01,0x02,0x03,0x04});
        byte[] entry2 = createNewEntryData(false, "file2.dat", 2L, 0100644L, new byte[]{0x05,0x06});
        byte[] trailer = createNewTrailer();
        byte[] archive = concat(entry1, entry2, trailer);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e1 = cis.getNextCPIOEntry();
            assertNotNull("Entry 1", e1);
            assertEquals("file1.dat", e1.getName());
            byte[] buf = new byte[4];
            assertEquals(4, cis.read(buf, 0, 4));
            assertArrayEquals(new byte[]{0x01,0x02,0x03,0x04}, buf);
            CpioArchiveEntry e2 = cis.getNextCPIOEntry();
            assertNotNull("Entry 2", e2);
            assertEquals("file2.dat", e2.getName());
            assertEquals(2, e2.getSize());
            assertEquals(0x05, cis.read());
            assertEquals(0x06, cis.read());
            assertEquals(-1, cis.read());
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (mode=0x1a4)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReadEntryWithMode0x1a4() throws IOException {
        // This test targets the known defect: mode = 420 (0x1a4) causes
        // IllegalArgumentException in defective version. Successful read is expected.
        byte[] archive = createNewEntry(false, 0x1a4L);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            CpioArchiveEntry e = cis.getNextCPIOEntry();
            assertNotNull("Entry should be read", e);
            assertEquals("Mode should be 0x1a4", 0x1a4L, e.getMode());
            assertEquals("Name", "badmode.txt", e.getName());
            // consume data
            byte[] buf = new byte[(int) e.getSize()];
            cis.read(buf, 0, buf.length);
            // next should be trailer
            assertNull("Trailer", cis.getNextCPIOEntry());
        }
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IOException.class)
    public void testUnknownMagic() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
        }
    }

    @Test(timeout = 4000, expected = EOFException.class)
    public void testTruncatedStream() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] {0x30, 0x37, 0x30, 0x37, 0x30, 0x31}); // magic but no header
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
        }
    }

    @Test(timeout = 4000)
    public void testMode0NonTrailerThrowsIOException() throws IOException {
        // Use OLD ASCII format where mode=0 but name is not trailer
        byte[] header = createOldAsciiHeader("file_with_mode0.bin", 10L, 0L);
        byte[] trailer = createOldAsciiTrailer();
        byte[] archive = concat(header, trailer);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            try {
                cis.getNextCPIOEntry();
                fail("Expected IOException for mode 0 non-trailer entry");
            } catch (IOException e) {
                assertTrue(e.getMessage().contains("Mode 0 only allowed in the trailer"));
            }
        }
    }

    @Test(timeout = 4000)
    public void testCrcMismatchThrowsIOException() throws IOException {
        // Construct a NEW_CRC entry with wrong checksum
        byte[] entry = createNewEntryData(true, "crcbad.dat", 3L, 0100644L, new byte[]{0x41,0x42,0x43});
        // corrupt the checksum field: bytes 104-111 (0-indexed) are checksum. We'll set to 0
        // length of entry header: magic 6 + 13*8 = 110, plus name=13 ("crcbad.dat\0" padded to 16)
        // The checksum is at offset 104, length 8. We'll overwrite with zeros.
        for (int i = 104; i < 112 && i < entry.length; i++) {
            entry[i] = 0;
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(entry);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            byte[] buf = new byte[3];
            cis.read(buf, 0, 3);
            // Now close entry will check CRC
            try {
                cis.getNextCPIOEntry(); // triggers closeEntry
                fail("Expected IOException for CRC mismatch");
            } catch (IOException e) {
                assertTrue(e.getMessage().contains("CRC Error"));
            }
        }
    }

    @Test(timeout = 4000)
    public void testSkipInMiddleOfEntry() throws IOException {
        byte[] archive = createNewEntry(false, 0100644L);
        ByteArrayInputStream bin = new ByteArrayInputStream(archive);
        try (CpioArchiveInputStream cis = new CpioArchiveInputStream(bin)) {
            cis.getNextCPIOEntry();
            long skipped = cis.skip(2);
            assertEquals("Skipped 2 bytes", 2, skipped);
            int data = cis.read();
            assertEquals("Third byte after skip", 0x43, data);
        }
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMatchesWithValidSignatures() {
        byte[] sigNew = {0x30,0x37,0x30,0x37,0x30,0x31};
        assertTrue("MAGIC_NEW should match", CpioArchiveInputStream.matches(sigNew, 6));
        byte[] sigNewCrc = {0x30,0x37,0x30,0x37,0x30,0x32};
        assertTrue("MAGIC_NEW_CRC should match", CpioArchiveInputStream.matches(sigNewCrc, 6));
        byte[] sigOldAscii = {0x30,0x37,0x30,0x37,0x30,0x37};
        assertTrue("MAGIC_OLD_ASCII should match", CpioArchiveInputStream.matches(sigOldAscii, 6));
        byte[] sigOldBinaryLE = {(byte)0x71, (byte)0xC7};
        assertTrue("MAGIC_OLD_BINARY LE should match", CpioArchiveInputStream.matches(sigOldBinaryLE, 2));
        byte[] sigOldBinaryBE = {(byte)0xC7, (byte)0x71};
        assertTrue("MAGIC_OLD_BINARY BE should match", CpioArchiveInputStream.matches(sigOldBinaryBE, 2));
        byte[] invalid = {0x00,0x00,0x00,0x00,0x00,0x00};
        assertFalse("Invalid signature should not match", CpioArchiveInputStream.matches(invalid, 6));
        assertFalse("Length < 6 should not match", CpioArchiveInputStream.matches(invalid, 5));
    }

    @Test(timeout = 4000)
    public void testCloseIdempotent() throws IOException {
        CpioArchiveInputStream cis = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        cis.close();
        cis.close(); // should not throw
    }

    // =======================================================================
    // Helper methods to construct CPIO byte streams
    // =======================================================================

    private static byte[] createNewEntry(boolean hasCrc, long mode) {
        byte[] content = {0x41, 0x42, 0x43};
        return createNewEntryData(hasCrc, "regulardir/myfile.txt", content.length, mode, content);
    }

    private static byte[] createNewEntryData(boolean hasCrc, String name, long filesize, long mode, byte[] data) {
        byte[] nameBytes = (name + "\0").getBytes();
        int nameLen = nameBytes.length;
        int headerSize = 110; // 6 magic + 13*8
        int padName = (4 - (headerSize + nameLen) % 4) % 4;
        int padData = (4 - (filesize % 4)) % 4;
        int totalLen = headerSize + nameLen + padName + (int) filesize + padData;
        byte[] entry = new byte[totalLen];
        int pos = 0;

        // magic
        byte[] magic = hasCrc ? "070702".getBytes() : "070701".getBytes();
        System.arraycopy(magic, 0, entry, pos, 6);
        pos += 6;

        // fields: 8 bytes each, hex string
        writeHex8(entry, pos, 0); // inode
        pos += 8;
        writeHex8(entry, pos, mode);
        pos += 8;
        writeHex8(entry, pos, 2); // uid
        pos += 8;
        writeHex8(entry, pos, 3); // gid
        pos += 8;
        writeHex8(entry, pos, 1); // nlink
        pos += 8;
        writeHex8(entry, pos, 1000000L); // mtime
        pos += 8;
        writeHex8(entry, pos, filesize);
        pos += 8;
        writeHex8(entry, pos, 0); // devmajor
        pos += 8;
        writeHex8(entry, pos, 0); // devminor
        pos += 8;
        writeHex8(entry, pos, 0); // rdevmajor
        pos += 8;
        writeHex8(entry, pos, 0); // rdevminor
        pos += 8;
        writeHex8(entry, pos, nameLen);
        pos += 8;
        // checksum (for CRC format)
        long crc = 0;
        if (hasCrc) {
            for (byte b : data) {
                crc += (b & 0xFF);
            }
        }
        writeHex8(entry, pos, crc);
        pos += 8;

        // name
        System.arraycopy(nameBytes, 0, entry, pos, nameLen);
        pos += nameLen + padName;

        // data
        System.arraycopy(data, 0, entry, pos, (int) filesize);
        // padding automatically zero

        // skip trailer creation: we will append in caller
        return entry;
    }

    private static byte[] createNewTrailer() {
        String name = "TRAILER!!!\0";
        return createNewEntryData(false, name, 0, 0L, new byte[0]);
    }

    private static void writeHex8(byte[] buf, int offset, long value) {
        String hex = String.format("%08x", value);
        byte[] hexBytes = hex.getBytes();
        System.arraycopy(hexBytes, 0, buf, offset, 8);
    }

    // Old ASCII format: fields 6/11 bytes octal
    private static byte[] createOldAsciiEntry() {
        String name = "file.txt\0";
        byte[] nameBytes = name.getBytes();
        int nameLen = nameBytes.length;
        int headerSize = 6 + 6 + 6 + 6 + 6 + 6 + 6 + 6 + 11 + 6 + 11; // total 76 bytes
        // plus no headers? Actually old ascii header size is fixed: 6*6 + 11*2 = 36+22=58? Let's compute:
        // fields: device(6), inode(6), mode(6), uid(6), gid(6), nlink(6), rdev(6), mtime(11), namesize(6), filesize(11) => 6*8+11*2 = 48+22=70? Wait:
        // Actually old ascii format: each field is fixed width octal (6 for most, 11 for mtime and filesize)
        // device(6), inode(6), mode(6), uid(6), gid(6), nlink(6), rdev(6), mtime(11), namesize(6), filesize(11) = 6*8 + 11*2 = 48+22=70 bytes.
        // But original source readAsciiLong length: 6,6,6,6,6,6,6,11,6,11 = total 70.
        // Then name padded to 2-byte boundary? Not sure; but we'll compute.
        int headerSize = 70;
        int padName = (2 - (headerSize + nameLen) % 2) % 2;
        long filesize = 1;
        int padData = (2 - (filesize % 2)) % 2;
        int totalLen = headerSize + nameLen + padName + (int) filesize + padData;
        byte[] entry = new byte[totalLen];
        int pos = 0;

        writeOctal6(entry, pos, 0); // device
        pos += 6;
        writeOctal6(entry, pos, 0); // inode
        pos += 6;
        writeOctal6(entry, pos, 0100644L);
        pos += 6;
        writeOctal6(entry, pos, 0); // uid
        pos += 6;
        writeOctal6(entry, pos, 0); // gid
        pos += 6;
        writeOctal6(entry, pos, 1); // nlink
        pos += 6;
        writeOctal6(entry, pos, 0); // rdev
        pos += 6;
        writeOctal11(entry, pos, 0); // mtime
        pos += 11;
        writeOctal6(entry, pos, nameLen);
        pos += 6;
        writeOctal11(entry, pos, filesize);
        pos += 11;

        // name
        System.arraycopy(nameBytes, 0, entry, pos, nameLen);
        pos += nameLen + padName;

        // data
        entry[pos] = 0x44;
        pos += (int) filesize;

        // trailer will be appended
        return entry;
    }

    private static byte[] createOldAsciiTrailer() {
        String name = "TRAILER!!!\0";
        byte[] nameBytes = name.getBytes();
        int headerSize = 70;
        int padName = (2 - (headerSize + nameBytes.length) % 2) % 2;
        int totalLen = headerSize + nameBytes.length + padName;
        byte[] trailer = new byte[totalLen];
        int pos = 0;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal6(trailer, pos, 1); pos+=6;
        writeOctal6(trailer, pos, 0); pos+=6;
        writeOctal11(trailer, pos, 0); pos+=11;
        writeOctal6(trailer, pos, nameBytes.length); pos+=6;
        writeOctal11(trailer, pos, 0); pos+=11;
        System.arraycopy(nameBytes, 0, trailer, pos, nameBytes.length);
        return trailer;
    }

    private static void writeOctal6(byte[] buf, int off, long val) {
        String oct = String.format("%06o", val);
        byte[] b = oct.getBytes();
        System.arraycopy(b, 0, buf, off, 6);
    }

    private static void writeOctal11(byte[] buf, int off, long val) {
        String oct = String.format("%011o", val);
        byte[] b = oct.getBytes();
        System.arraycopy(b, 0, buf, off, 11);
    }

    // Old binary format (2/4 byte fields, little-endian as per CpioUtil)
    private static byte[] createOldBinaryEntry(boolean swapped) {
        // minimal entry
        String name = "data.bin\0";
        byte[] nameBytes = name.getBytes();
        int nameLen = nameBytes.length;
        int headerSize = 2+2+2+2+2+2+2+4+2+4; // 24 bytes
        long filesize = 5;
        int padData = (2 - (filesize % 2)) % 2;
        int totalLen = headerSize + nameLen + (int) filesize + padData;
        byte[] entry = new byte[totalLen];
        int pos = 0;

        // Write fields as binary in little-endian (unless swapped)
        writeShortLE(entry, pos, 0, swapped); // device
        pos += 2;
        writeShortLE(entry, pos, 0, swapped); // inode
        pos += 2;
        writeShortLE(entry, pos, (int) 0100644L, swapped);
        pos += 2;
        writeShortLE(entry, pos, 0, swapped); // uid
        pos += 2;
        writeShortLE(entry, pos, 0, swapped); // gid
        pos += 2;
        writeShortLE(entry, pos, 1, swapped); // nlink
        pos += 2;
        writeShortLE(entry, pos, 0, swapped); // rdev
        pos += 2;
        writeIntLE(entry, pos, 0, swapped); // mtime
        pos += 4;
        writeShortLE(entry, pos, nameLen, swapped); // namesize
        pos += 2;
        writeIntLE(entry, pos, filesize, swapped); // filesize
        pos += 4;

        // name
        System.arraycopy(nameBytes, 0, entry, pos, nameLen);
        pos += nameLen;

        // data: 5 bytes
        for (int i = 0; i < filesize; i++) {
            entry[pos++] = (byte) (0x30 + i);
        }

        // pad data is automatically zero
        return entry;
    }

    private static void writeShortLE(byte[] buf, int off, int val, boolean swap) {
        if (swap) {
            buf[off] = (byte) (val >> 8);
            buf[off+1] = (byte) val;
        } else {
            buf[off] = (byte) val;
            buf[off+1] = (byte) (val >> 8);
        }
    }

    private static void writeIntLE(byte[] buf, int off, long val, boolean swap) {
        int v = (int) val;
        if (swap) {
            buf[off] = (byte) (v >> 24);
            buf[off+1] = (byte) (v >> 16);
            buf[off+2] = (byte) (v >> 8);
            buf[off+3] = (byte) v;
        } else {
            buf[off] = (byte) v;
            buf[off+1] = (byte) (v >> 8);
            buf[off+2] = (byte) (v >> 16);
            buf[off+3] = (byte) (v >> 24);
        }
    }

    // helper to create OldAscii header for mode 0 test
    private static byte[] createOldAsciiHeader(String name, long filesize, long mode) {
        String nameStr = name + "\0";
        byte[] nameBytes = nameStr.getBytes();
        int headerSize = 70;
        int padName = (2 - (headerSize + nameBytes.length) % 2) % 2;
        int totalLen = headerSize + nameBytes.length + padName;
        byte[] header = new byte[totalLen];
        int pos = 0;
        writeOctal6(header, pos, 0); pos+=6;
        writeOctal6(header, pos, 0); pos+=6;
        writeOctal6(header, pos, mode); pos+=6;
        writeOctal6(header, pos, 0); pos+=6;
        writeOctal6(header, pos, 0); pos+=6;
        writeOctal6(header, pos, 1); pos+=6;
        writeOctal6(header, pos, 0); pos+=6;
        writeOctal11(header, pos, 0); pos+=11;
        writeOctal6(header, pos, nameBytes.length); pos+=6;
        writeOctal11(header, pos, filesize); pos+=11;
        System.arraycopy(nameBytes, 0, header, pos, nameBytes.length);
        return header;
    }

    private static byte[] concat(byte[]... arrays) {
        int total = 0;
        for (byte[] arr : arrays) total += arr.length;
        byte[] result = new byte[total];
        int pos = 0;
        for (byte[] arr : arrays) {
            System.arraycopy(arr, 0, result, pos, arr.length);
            pos += arr.length;
        }
        return result;
    }
}