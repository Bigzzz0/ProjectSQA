package org.apache.commons.compress.archivers.cpio;

import java.io.*;
import java.util.*;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class CpioArchiveOutputStreamDeepseekTest {

    /**
     * @target CpioArchiveOutputStream.close()
     * @scenario Close stream without calling finish() after writing a valid entry
     * @defectRisk Missing TRAILER record causes EOFException when reading back
     */
    @Test(timeout = 4000)
    public void testCloseWithoutFinish_WritesTrailer_COMPRESS28() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close(); // No finish() call

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertNotNull("Entry should be readable", readEntry);
        assertEquals("test.txt", readEntry.getName());
        byte[] content = new byte[4];
        assertEquals(4, in.read(content));
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, content);
        assertNull("Reading past entry should return null, not throw EOFException", in.getNextCPIOEntry());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream constructor
     * @scenario Create stream with FORMAT_NEW
     * @defectRisk Incorrect format handling
     */
    @Test(timeout = 4000)
    public void testConstructorFormatNew() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream constructor
     * @scenario Create stream with FORMAT_NEW_CRC
     * @defectRisk Incorrect format handling
     */
    @Test(timeout = 4000)
    public void testConstructorFormatNewCRC() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream constructor
     * @scenario Create stream with FORMAT_OLD_ASCII
     * @defectRisk Incorrect format handling
     */
    @Test(timeout = 4000)
    public void testConstructorFormatOldAscii() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream constructor
     * @scenario Create stream with FORMAT_OLD_BINARY
     * @defectRisk Incorrect format handling
     */
    @Test(timeout = 4000)
    public void testConstructorFormatOldBinary() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream constructor
     * @scenario Create stream with default format
     * @defectRisk Incorrect default format
     */
    @Test(timeout = 4000)
    public void testConstructorDefaultFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream.setFormat
     * @scenario Set invalid format
     * @defectRisk IllegalArgumentException not thrown
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetFormatInvalid() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, (short) 999);
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Write entry with FORMAT_NEW and verify header
     * @defectRisk Incorrect header writing
     */
    @Test(timeout = 4000)
    public void testPutNextEntryNewFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        // Verify magic bytes for FORMAT_NEW
        assertEquals("070701", new String(data, 0, 6, "US-ASCII"));
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Write entry with FORMAT_NEW_CRC and verify header
     * @defectRisk Incorrect header writing
     */
    @Test(timeout = 4000)
    public void testPutNextEntryNewCRCFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        // Verify magic bytes for FORMAT_NEW_CRC
        assertEquals("070702", new String(data, 0, 6, "US-ASCII"));
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Write entry with FORMAT_OLD_ASCII and verify header
     * @defectRisk Incorrect header writing
     */
    @Test(timeout = 4000)
    public void testPutNextEntryOldAsciiFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        // Verify magic bytes for FORMAT_OLD_ASCII
        assertEquals("070707", new String(data, 0, 6, "US-ASCII"));
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Write entry with FORMAT_OLD_BINARY and verify header
     * @defectRisk Incorrect header writing
     */
    @Test(timeout = 4000)
    public void testPutNextEntryOldBinaryFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
        // Verify magic bytes for FORMAT_OLD_BINARY (first 2 bytes)
        assertEquals(0x71, data[0] & 0xFF);
        assertEquals(0xC7, data[1] & 0xFF);
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Duplicate entry name
     * @defectRisk IOException not thrown for duplicate
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testPutNextEntryDuplicate() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry1);
        out.closeArchiveEntry();
        out.putNextEntry(entry2);
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Entry with no format specified
     * @defectRisk Default format not applied
     */
    @Test(timeout = 4000)
    public void testPutNextEntryNoFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry("test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        assertEquals("070701", new String(data, 0, 6, "US-ASCII"));
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Entry with no time specified
     * @defectRisk Current time not set
     */
    @Test(timeout = 4000)
    public void testPutNextEntryNoTime() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        entry.setTime(-1);
        out.putNextEntry(entry);
        assertTrue(entry.getTime() > 0);
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write with negative offset
     * @defectRisk IndexOutOfBoundsException not thrown
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeOffset() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, -1, 2);
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write with negative length
     * @defectRisk IndexOutOfBoundsException not thrown
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testWriteNegativeLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 0, -1);
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write with offset beyond array bounds
     * @defectRisk IndexOutOfBoundsException not thrown
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testWriteOffsetBeyondBounds() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 5, 1);
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write with zero length
     * @defectRisk Unexpected behavior
     */
    @Test(timeout = 4000)
    public void testWriteZeroLength() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 0, 0);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write without current entry
     * @defectRisk IOException not thrown
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testWriteNoCurrentEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.write(new byte[] { 1, 2, 3, 4 });
    }

    /**
     * @target CpioArchiveOutputStream.write
     * @scenario Write past declared entry size
     * @defectRisk IOException not thrown
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testWritePastEnd() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4, 5 });
    }

    /**
     * @target CpioArchiveOutputStream.closeArchiveEntry
     * @scenario Close entry with incorrect size
     * @defectRisk IOException not thrown
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryWrongSize() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
        out.closeArchiveEntry();
    }

    /**
     * @target CpioArchiveOutputStream.closeArchiveEntry
     * @scenario Close entry with CRC mismatch in FORMAT_NEW_CRC
     * @defectRisk IOException not thrown for CRC error
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryCRCMismatch() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "test.txt", 4);
        entry.setChksum(999); // Wrong CRC
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
    }

    /**
     * @target CpioArchiveOutputStream.closeArchiveEntry
     * @scenario Close entry with correct CRC in FORMAT_NEW_CRC
     * @defectRisk CRC calculation incorrect
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryCRCCorrect() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "test.txt", 4);
        // CRC of {1,2,3,4} = 1+2+3+4 = 10
        entry.setChksum(10);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream.finish
     * @scenario Call finish twice
     * @defectRisk IOException not thrown on second finish
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testFinishTwice() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.finish();
    }

    /**
     * @target CpioArchiveOutputStream.finish
     * @scenario Finish after writing entry
     * @defectRisk TRAILER not written correctly
     */
    @Test(timeout = 4000)
    public void testFinishAfterEntry() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.finish();
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertNotNull(readEntry);
        assertEquals("test.txt", readEntry.getName());
        byte[] content = new byte[4];
        assertEquals(4, in.read(content));
        assertArrayEquals(new byte[] { 1, 2, 3, 4 }, content);
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.close
     * @scenario Close stream twice
     * @defectRisk Double close should not throw
     */
    @Test(timeout = 4000)
    public void testCloseTwice() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream.putArchiveEntry
     * @scenario Put ArchiveEntry (not CpioArchiveEntry)
     * @defectRisk ClassCastException not thrown
     */
    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testPutArchiveEntryWrongType() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        ArchiveEntry entry = new ArchiveEntry() {
            @Override
            public String getName() { return "test"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public boolean isFile() { return true; }
            @Override
            public long getLastModifiedDate() { return 0; }
        };
        out.putArchiveEntry(entry);
    }

    /**
     * @target CpioArchiveOutputStream.write(int)
     * @scenario Write single byte
     * @defectRisk Single byte write not working
     */
    @Test(timeout = 4000)
    public void testWriteSingleByte() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 1);
        out.putNextEntry(entry);
        out.write(42);
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertEquals(42, in.read());
        assertEquals(-1, in.read());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.pad
     * @scenario Pad for 4-byte alignment in FORMAT_NEW
     * @defectRisk Padding not applied correctly
     */
    @Test(timeout = 4000)
    public void testPadNewFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 3);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        // Find entry data and check padding
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        byte[] content = new byte[3];
        assertEquals(3, in.read(content));
        // After reading 3 bytes, next should be padding (0) then trailer
        assertEquals(0, in.read());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.pad
     * @scenario Pad for 2-byte alignment in FORMAT_OLD_BINARY
     * @defectRisk Padding not applied correctly
     */
    @Test(timeout = 4000)
    public void testPadOldBinaryFormat() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "test.txt", 3);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        byte[] content = new byte[3];
        assertEquals(3, in.read(content));
        // After reading 3 bytes, next should be padding (0) then trailer
        assertEquals(0, in.read());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.writeHeader
     * @scenario Write header for entry with long name
     * @defectRisk Header size calculation incorrect
     */
    @Test(timeout = 4000)
    public void testWriteHeaderLongName() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        String longName = "a".repeat(100);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, longName, 1);
        out.putNextEntry(entry);
        out.write(new byte[] { 1 });
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        CpioArchiveEntry readEntry = in.getNextCPIOEntry();
        assertEquals(longName, readEntry.getName());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.writeAsciiLong
     * @scenario Write ASCII long with radix 16
     * @defectRisk Incorrect hex conversion
     */
    @Test(timeout = 4000)
    public void testWriteAsciiLongHex() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        entry.setInode(255);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        // Check inode field in header (offset 6, length 8)
        String inodeHex = new String(data, 6, 8, "US-ASCII");
        assertEquals("000000ff", inodeHex);
    }

    /**
     * @target CpioArchiveOutputStream.writeAsciiLong
     * @scenario Write ASCII long with radix 8
     * @defectRisk Incorrect octal conversion
     */
    @Test(timeout = 4000)
    public void testWriteAsciiLongOctal() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "test.txt", 0);
        entry.setInode(8);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        // Check inode field in header (offset 6, length 6)
        String inodeOctal = new String(data, 6, 6, "US-ASCII");
        assertEquals("000010", inodeOctal);
    }

    /**
     * @target CpioArchiveOutputStream.writeCString
     * @scenario Write C string with null terminator
     * @defectRisk Missing null terminator
     */
    @Test(timeout = 4000)
    public void testWriteCString() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 0);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        // Find name in header and verify null terminator
        String header = new String(data, 0, 110, "US-ASCII");
        assertTrue(header.contains("test.txt\0"));
    }

    /**
     * @target CpioArchiveOutputStream.writeBinaryLong
     * @scenario Write binary long with swap
     * @defectRisk Incorrect byte order
     */
    @Test(timeout = 4000)
    public void testWriteBinaryLongSwap() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "test.txt", 0);
        entry.setDevice(0x1234);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
        out.close();

        byte[] data = baos.toByteArray();
        // Device field is at offset 2, length 2, swapped
        assertEquals(0x34, data[2] & 0xFF);
        assertEquals(0x12, data[3] & 0xFF);
    }

    /**
     * @target CpioArchiveOutputStream.ensureOpen
     * @scenario Write after close
     * @defectRisk IOException not thrown
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testWriteAfterClose() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.close();
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 1);
        out.putNextEntry(entry);
    }

    /**
     * @target CpioArchiveOutputStream.closeArchiveEntry
     * @scenario Close entry without writing data
     * @defectRisk Size mismatch not detected
     */
    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseArchiveEntryNoData() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.closeArchiveEntry();
    }

    /**
     * @target CpioArchiveOutputStream.finish
     * @scenario Finish without any entries
     * @defectRisk TRAILER not written
     */
    @Test(timeout = 4000)
    public void testFinishNoEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        out.finish();
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    /**
     * @target CpioArchiveOutputStream.putNextEntry
     * @scenario Multiple entries
     * @defectRisk Multiple entries not handled correctly
     */
    @Test(timeout = 4000)
    public void testMultipleEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW);
        
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file1.txt", 2);
        out.putNextEntry(entry1);
        out.write(new byte[] { 1, 2 });
        out.closeArchiveEntry();
        
        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "file2.txt", 3);
        out.putNextEntry(entry2);
        out.write(new byte[] { 3, 4, 5 });
        out.closeArchiveEntry();
        
        out.close();

        byte[] data = baos.toByteArray();
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(data));
        
        CpioArchiveEntry readEntry1 = in.getNextCPIOEntry();
        assertEquals("file1.txt", readEntry1.getName());
        byte[] content1 = new byte[2];
        assertEquals(2, in.read(content1));
        assertArrayEquals(new byte[] { 1, 2 }, content1);
        
        CpioArchiveEntry readEntry2 = in.getNextCPIOEntry();
        assertEquals("file2.txt", readEntry2.getName());
        byte[] content2 = new byte[3];
        assertEquals(3, in.read(content2));
        assertArrayEquals(new byte[] { 3, 4, 5 }, content2);
        
        assertNull(in.getNextCPIOEntry());
        in.close();
    }
}