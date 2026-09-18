package org.apache.commons.compress.archivers.cpio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: CpioArchiveInputStream
 *
 * Decision / Branch Coverage Targets:
 * 1. Constructor variants:
 *    - CpioArchiveInputStream(in) -> BLOCK_SIZE, US_ASCII
 *    - CpioArchiveInputStream(in, encoding) -> BLOCK_SIZE, custom encoding
 *    - CpioArchiveInputStream(in, blockSize) -> custom blockSize, US_ASCII
 *    - CpioArchiveInputStream(in, blockSize, encoding) -> fully parameterized
 * 2. Signature Matching (matches(byte[], int)):
 *    - length < 6 (false)
 *    - Old binary magic 0x71C7 and 0xC771 (true)
 *    - Non-matching binary/ascii signatures (false)
 *    - Ascii magic headers: "070701" (NEW), "070702" (NEW_CRC), "070707" (OLD_ASCII) (true)
 *    - Ascii magic mismatch at byte 0..4 and byte 5 (false)
 * 3. Archive Parsing & Formats:
 *    - MAGIC_OLD_BINARY (swapped and unswapped halfwords)
 *    - MAGIC_NEW (070701)
 *    - MAGIC_NEW_CRC (070702) with CRC validation & CRC mismatch detection
 *    - MAGIC_OLD_ASCII (070707)
 *    - Unknown magic header detection (IOException)
 * 4. Entry Lifecycle & Stream Controls:
 *    - CPIO_TRAILER handling and skipRemainderOfLastBlock()
 *    - closeEntry() triggered automatically when getNextEntry() called before exhausting entry data
 *    - available() before and after entry EOF, and when stream closed
 *    - read(b, off, len) boundary conditions (off < 0, len < 0, off > len, len == 0, entry == null, entryEOF)
 *    - skip(long n) with negative values, large values, and EOF condition
 *    - Premature EOF (EOFException) during header, name, or payload read
 *    - Zero mode validation (only trailer allows mode == 0, others throw IOException)
 * 5. Defect Zone: Encoding support & trailing NUL read
 *    - Filename decoding across different encodings (UTF-8, ISO-8859-1, etc.)
 *    - Correct stream alignment across entries including padding
 */
public class CpioArchiveInputStreamGeminiTest {

    // Helper: builds standard valid CPIO entry bytes
    private byte[] createArchive(short format, String filename, byte[] content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, format);
        CpioArchiveEntry entry = new CpioArchiveEntry(format, filename, content.length);
        entry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(entry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();
        return baos.toByteArray();
    }

    private byte[] createMultiArchive(short format, String[] names, byte[][] contents) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, format);
        for (int i = 0; i < names.length; i++) {
            CpioArchiveEntry entry = new CpioArchiveEntry(format, names[i], contents[i].length);
            entry.setMode(CpioConstants.C_ISREG | 0644);
            out.putNextEntry(entry);
            out.write(contents[i]);
            out.closeArchiveEntry();
        }
        out.close();
        return baos.toByteArray();
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadNewFormatEntry() throws IOException {
        byte[] content = "Hello World New Format".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "test_new.txt", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("test_new.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        assertEquals(CpioConstants.FORMAT_NEW, entry.getFormat());

        byte[] readBuf = new byte[content.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuf);

        assertEquals(-1, in.read(readBuf, 0, 1));
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadOldAsciiFormatEntry() throws IOException {
        byte[] content = "Old Ascii Data".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_OLD_ASCII, "old_ascii.txt", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        ArchiveEntry entry = in.getNextEntry();
        assertNotNull(entry);
        assertEquals("old_ascii.txt", entry.getName());
        assertEquals(content.length, entry.getSize());

        byte[] readBuf = new byte[content.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuf);

        assertNull(in.getNextEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadOldBinaryFormatEntry() throws IOException {
        byte[] content = "Binary Format Payload".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_OLD_BINARY, "binary.bin", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals("binary.bin", entry.getName());
        assertEquals(content.length, entry.getSize());
        assertEquals(CpioConstants.FORMAT_OLD_BINARY, entry.getFormat());

        byte[] readBuf = new byte[content.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuf);

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadNewCrcFormatSuccess() throws IOException {
        byte[] content = "CRC Verified Content".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW_CRC, "crc_test.txt", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);
        assertEquals(CpioConstants.FORMAT_NEW_CRC, entry.getFormat());

        byte[] readBuf = new byte[content.length];
        int bytesRead = in.read(readBuf, 0, readBuf.length);
        assertEquals(content.length, bytesRead);
        assertArrayEquals(content, readBuf);

        assertEquals(-1, in.read(readBuf, 0, 1)); // Triggers CRC verification
        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testMultiEntrySequentialReadAndAutoCloseEntry() throws IOException {
        String[] names = new String[]{"file1.txt", "file2.dat", "file3.log"};
        byte[][] contents = new byte[][]{
            "first entry data".getBytes(StandardCharsets.US_ASCII),
            "second entry data - longer payload".getBytes(StandardCharsets.US_ASCII),
            "third".getBytes(StandardCharsets.US_ASCII)
        };
        byte[] archive = createMultiArchive(CpioConstants.FORMAT_NEW, names, contents);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));

        // Read file1 completely
        CpioArchiveEntry entry1 = in.getNextCPIOEntry();
        assertEquals(names[0], entry1.getName());
        byte[] buf1 = new byte[contents[0].length];
        assertEquals(contents[0].length, in.read(buf1, 0, buf1.length));
        assertEquals(-1, in.read(buf1, 0, 1));

        // Read file2 partially, then invoke getNextCPIOEntry() to trigger closeEntry() skip logic
        CpioArchiveEntry entry2 = in.getNextCPIOEntry();
        assertEquals(names[1], entry2.getName());
        byte[] buf2 = new byte[4];
        assertEquals(4, in.read(buf2, 0, 4));

        // Auto skips remainder of file2 and starts file3
        CpioArchiveEntry entry3 = in.getNextCPIOEntry();
        assertEquals(names[2], entry3.getName());
        byte[] buf3 = new byte[contents[2].length];
        assertEquals(contents[2].length, in.read(buf3, 0, buf3.length));

        assertNull(in.getNextCPIOEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testAvailableBehavior() throws IOException {
        byte[] content = "abc".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "test.txt", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        assertEquals(1, in.available());

        in.getNextCPIOEntry();
        assertEquals(1, in.available());

        byte[] buf = new byte[3];
        in.read(buf, 0, 3);
        in.read(buf, 0, 1); // Hit EOF on current entry
        assertEquals(0, in.available());

        in.close();
        try {
            in.available();
            fail("Expected IOException on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSkipWithinEntry() throws IOException {
        byte[] content = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "skip.txt", content);

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        in.getNextCPIOEntry();

        long skipped = in.skip(10);
        assertEquals(10, skipped);

        byte[] buf = new byte[5];
        int read = in.read(buf, 0, 5);
        assertEquals(5, read);
        assertEquals("ABCDE", new String(buf, 0, 5, StandardCharsets.US_ASCII));

        // Skip beyond entry remaining bytes
        long remaining = in.skip(100);
        assertEquals(content.length - 15, remaining);

        // Next skip should be 0 because entryEOF is reached
        assertEquals(0, in.skip(10));
        in.close();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesSignatureBoundaries() {
        assertFalse(CpioArchiveInputStream.matches(new byte[5], 5));
        assertFalse(CpioArchiveInputStream.matches(null, 0));

        // Binary signatures
        byte[] sigBinary1 = new byte[]{(byte) 0x71, (byte) 0xc7, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(sigBinary1, 6));

        byte[] sigBinary2 = new byte[]{(byte) 0xc7, (byte) 0x71, 0, 0, 0, 0};
        assertTrue(CpioArchiveInputStream.matches(sigBinary2, 6));

        // Ascii signatures: 070701, 070702, 070707
        byte[] sigNew = "070701".getBytes(StandardCharsets.US_ASCII);
        assertTrue(CpioArchiveInputStream.matches(sigNew, 6));

        byte[] sigNewCrc = "070702".getBytes(StandardCharsets.US_ASCII);
        assertTrue(CpioArchiveInputStream.matches(sigNewCrc, 6));

        byte[] sigOldAscii = "070707".getBytes(StandardCharsets.US_ASCII);
        assertTrue(CpioArchiveInputStream.matches(sigOldAscii, 6));

        // Ascii prefix failure
        byte[] sigInvalidPrefix = "170701".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidPrefix, 6));

        byte[] sigInvalidPrefix2 = "080701".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidPrefix2, 6));

        byte[] sigInvalidPrefix3 = "071701".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidPrefix3, 6));

        byte[] sigInvalidPrefix4 = "070801".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidPrefix4, 6));

        byte[] sigInvalidPrefix5 = "070711".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidPrefix5, 6));

        // Ascii suffix failure
        byte[] sigInvalidSuffix = "070708".getBytes(StandardCharsets.US_ASCII);
        assertFalse(CpioArchiveInputStream.matches(sigInvalidSuffix, 6));
    }

    @Test(timeout = 4000)
    public void testReadZeroBytes() throws IOException {
        byte[] content = "Content".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "file.txt", content);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        in.getNextCPIOEntry();

        byte[] b = new byte[10];
        int readCount = in.read(b, 0, 0);
        assertEquals(0, readCount);
        in.close();
    }

    @Test(timeout = 4000)
    public void testReadBeforeNextEntryReturnsMinusOne() throws IOException {
        byte[] content = "Content".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "file.txt", content);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));

        byte[] b = new byte[10];
        assertEquals(-1, in.read(b, 0, b.length));
        in.close();
    }

    @Test(timeout = 4000)
    public void testZeroSkipLength() throws IOException {
        byte[] content = "Content".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "file.txt", content);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        in.getNextCPIOEntry();

        long skipped = in.skip(0);
        assertEquals(0, skipped);
        in.close();
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Encoding & Stream Handling)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectEncodingInputStreamWithNonAsciiName() throws IOException {
        // Targets ArchiveStreamFactory encoding defect in CPIO filenames
        String nonAsciiName = "test_äöü_name.txt";
        byte[] content = "Encoding payload".getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(baos, CpioConstants.FORMAT_NEW,
                CpioConstants.BLOCK_SIZE, "UTF-8");
        CpioArchiveEntry outEntry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, nonAsciiName, content.length);
        outEntry.setMode(CpioConstants.C_ISREG | 0644);
        out.putNextEntry(outEntry);
        out.write(content);
        out.closeArchiveEntry();
        out.close();

        byte[] archiveData = baos.toByteArray();

        // 1. Read with UTF-8 encoding
        CpioArchiveInputStream inUtf8 = new CpioArchiveInputStream(
                new ByteArrayInputStream(archiveData), CpioConstants.BLOCK_SIZE, "UTF-8");
        CpioArchiveEntry entryUtf8 = inUtf8.getNextCPIOEntry();
        assertNotNull("Entry should be successfully read with UTF-8", entryUtf8);
        assertEquals("Filename should match original Unicode name when UTF-8 encoding is used",
                nonAsciiName, entryUtf8.getName());
        inUtf8.close();

        // 2. Test 2-arg constructor with encoding
        CpioArchiveInputStream in2Arg = new CpioArchiveInputStream(
                new ByteArrayInputStream(archiveData), "UTF-8");
        CpioArchiveEntry entry2Arg = in2Arg.getNextCPIOEntry();
        assertNotNull(entry2Arg);
        assertEquals(nonAsciiName, entry2Arg.getName());
        in2Arg.close();
    }

    @Test(timeout = 4000)
    public void testCrcChecksumFailureThrowsException() throws IOException {
        byte[] content = "Data to be corrupted".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW_CRC, "crc_fail.txt", content);

        // Corrupt content payload in archive (payload begins after 110-byte header + name)
        archive[110 + "crc_fail.txt".length() + 2] ^= 0xFF;

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        CpioArchiveEntry entry = in.getNextCPIOEntry();
        assertNotNull(entry);

        byte[] buf = new byte[content.length];
        try {
            in.read(buf, 0, buf.length);
            in.read(buf, 0, 1); // Trigger CRC check at EOF
            fail("Expected IOException due to CRC Error");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("CRC Error"));
        } finally {
            in.close();
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSkipNegativeThrowsException() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.skip(-1);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testReadInvalidBounds() throws IOException {
        byte[] archive = createArchive(CpioConstants.FORMAT_NEW, "test.txt", new byte[10]);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(archive));
        in.getNextCPIOEntry();

        byte[] buf = new byte[10];

        try {
            in.read(buf, -1, 5);
            fail("Expected IndexOutOfBoundsException for negative offset");
        } catch (IndexOutOfBoundsException expected) {}

        try {
            in.read(buf, 0, -1);
            fail("Expected IndexOutOfBoundsException for negative length");
        } catch (IndexOutOfBoundsException expected) {}

        try {
            in.read(buf, 6, 5);
            fail("Expected IndexOutOfBoundsException for off + len > length");
        } catch (IndexOutOfBoundsException expected) {}

        in.close();
    }

    @Test(timeout = 4000)
    public void testEnsureOpenChecks() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        in.close();

        try {
            in.getNextCPIOEntry();
            fail("Expected IOException on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        try {
            in.read(new byte[5], 0, 5);
            fail("Expected IOException on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        try {
            in.skip(5);
            fail("Expected IOException on closed stream");
        } catch (IOException expected) {
            assertEquals("Stream closed", expected.getMessage());
        }

        // Multiple close calls should be safe
        in.close();
    }

    @Test(timeout = 4000)
    public void testUnknownMagicThrowsException() throws IOException {
        byte[] corruptedMagic = "999999corrupted_header_data".getBytes(StandardCharsets.US_ASCII);
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(corruptedMagic));

        try {
            in.getNextCPIOEntry();
            fail("Expected IOException for unknown magic");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Unknown magic"));
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testPrematureHeaderEOFThrowsEOFException() throws IOException {
        byte[] truncated = new byte[]{ '0', '7', '0', '7' }; // Less than 6 bytes
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(truncated));
        try {
            in.getNextCPIOEntry();
            fail("Expected EOFException on truncated header");
        } catch (EOFException expected) {
            // Success
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testModeZeroNotAllowedNonTrailer() throws IOException {
        // Construct a manual CPIO new format header where mode is 0 and name is not TRAILER!!!
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write("070701".getBytes(StandardCharsets.US_ASCII));
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // inode
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // mode = 0
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // uid
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // gid
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // nlink
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // mtime
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // filesize = 0
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // devmaj
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // devmin
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // rdevmaj
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // rdevmin
        baos.write("00000005".getBytes(StandardCharsets.US_ASCII)); // namesize = 5 ("abc\0")
        baos.write("00000000".getBytes(StandardCharsets.US_ASCII)); // chksum
        baos.write("abc\0".getBytes(StandardCharsets.US_ASCII));     // name + NUL

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            in.getNextCPIOEntry();
            fail("Expected IOException: Mode 0 only allowed in the trailer");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("Mode 0 only allowed in the trailer"));
        } finally {
            in.close();
        }
    }

    // =========================================================================
    // PARTITION E: Stream Lifecycle & Edge Cases
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnderlyingStreamClosesOnClose() throws IOException {
        final boolean[] streamClosed = new boolean[]{false};
        InputStream dummyIn = new FilterInputStream(new ByteArrayInputStream(new byte[0])) {
            @Override
            public void close() throws IOException {
                streamClosed[0] = true;
                super.close();
            }
        };

        CpioArchiveInputStream cpioIn = new CpioArchiveInputStream(dummyIn, 512);
        cpioIn.close();
        assertTrue("Underlying stream should have been closed", streamClosed[0]);
    }

    @Test(timeout = 4000)
    public void testEmptyStreamAtStartThrowsEOF() throws IOException {
        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        try {
            in.getNextCPIOEntry();
            fail("Expected EOFException on empty stream");
        } catch (EOFException expected) {
            // expected
        } finally {
            in.close();
        }
    }
}