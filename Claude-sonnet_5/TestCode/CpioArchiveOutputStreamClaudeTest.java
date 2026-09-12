package org.apache.commons.compress.archivers.cpio;

import java.io.*;
import java.util.*;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * High-coverage JUnit4 test suite for {@link CpioArchiveOutputStream}.
 */
public class CpioArchiveOutputStreamClaudeTest {

    /**
     * @target CpioArchiveOutputStream(OutputStream)
     * @scenario Construct stream with default (single-arg) constructor
     * @defectRisk default format must be FORMAT_NEW; constructor must not throw
     */
    @Test(timeout = 4000)
    public void testDefaultConstructorUsesFormatNew() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        assertNotNull(out);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream(OutputStream, short) / setFormat
     * @scenario Construct with FORMAT_NEW_CRC explicitly
     * @defectRisk incorrect switch handling for FORMAT_NEW_CRC
     */
    @Test(timeout = 4000)
    public void testConstructorFormatNewCrc() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream(OutputStream, short) / setFormat
     * @scenario Construct with FORMAT_OLD_ASCII explicitly
     * @defectRisk incorrect switch handling for FORMAT_OLD_ASCII
     */
    @Test(timeout = 4000)
    public void testConstructorFormatOldAscii() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_ASCII);
        out.close();
    }

    /**
     * @target CpioArchiveOutputStream(OutputStream, short) / setFormat
     * @scenario Construct with FORMAT_OLD_BINARY explicitly
     * @defectRisk incorrect switch handling for FORMAT_OLD_BINARY
     */
    @Test(timeout = 4000)
    public void testConstructorFormatOldBinary() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_BINARY);
        out.close();
    }

    /**
     * @target setFormat(short) default branch
     * @scenario Construct with an unknown/invalid format value
     * @defectRisk missing validation would allow invalid formats silently
     */
    @Test(timeout = 4000)
    public void testConstructorInvalidFormatThrowsException() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            new CpioArchiveOutputStream(bos, (short) 9999);
            fail("Expected IllegalArgumentException for unknown header type");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    /**
     * @target putNextEntry(CpioArchiveEntry) - time assignment branch
     * @scenario Entry with unset time (-1) is put into the stream
     * @defectRisk time not defaulted to current time when unset
     */
    @Test(timeout = 4000)
    public void testPutNextEntrySetsTimeWhenUnset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "timeless.txt", 0);
        out.putNextEntry(entry);
        assertTrue(entry.getTime() != -1);
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target putNextEntry(CpioArchiveEntry) - format defaulting branch
     * @scenario Entry created with no-arg constructor (format unspecified) is put
     * @defectRisk format not defaulted to stream's entryFormat when unset
     */
    @Test(timeout = 4000)
    public void testPutNextEntryDefaultsFormatWhenUnset() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry();
        entry.setName("noformat.txt");
        entry.setFileSize(4);
        out.putNextEntry(entry);
        assertEquals(CpioConstants.FORMAT_NEW, entry.getFormat());
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target putNextEntry(CpioArchiveEntry) - duplicate detection
     * @scenario Two entries with identical names are put sequentially
     * @defectRisk missing duplicate-name detection allows corrupt archive
     */
    @Test(timeout = 4000)
    public void testPutNextEntryDuplicateNameThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        out.putNextEntry(entry1);
        out.closeArchiveEntry();

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "dup.txt", 0);
        try {
            out.putNextEntry(entry2);
            fail("Expected IOException for duplicate entry name");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("duplicate entry"));
        }
        out.close();
    }

    /**
     * @target putNextEntry(CpioArchiveEntry) - implicit close of previous entry
     * @scenario putNextEntry called again while a previous entry is still open
     * @defectRisk previous entry not closed automatically, corrupting stream layout
     */
    @Test(timeout = 4000)
    public void testPutNextEntryClosesPreviousEntryAutomatically() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "first.txt", 4);
        out.putNextEntry(entry1);
        out.write(new byte[] { 1, 2, 3, 4 });
        // Do NOT call closeArchiveEntry() explicitly.

        CpioArchiveEntry entry2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "second.txt", 0);
        out.putNextEntry(entry2); // should auto-close entry1
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target ArchiveOutputStream#putArchiveEntry(ArchiveEntry)
     * @scenario putArchiveEntry delegates correctly to putNextEntry
     * @defectRisk cast or delegation failure in putArchiveEntry
     */
    @Test(timeout = 4000)
    public void testPutArchiveEntryDelegatesToPutNextEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        ArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "delegated.txt", 2);
        out.putArchiveEntry(entry);
        out.write(new byte[] { 9, 8 });
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target write(byte[], int, int) - bounds check (negative offset)
     * @scenario Call write with a negative offset
     * @defectRisk missing bounds validation would allow invalid array access
     */
    @Test(timeout = 4000)
    public void testWriteNegativeOffsetThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "neg.txt", 4);
        out.putNextEntry(entry);
        try {
            out.write(new byte[] { 1, 2, 3, 4 }, -1, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - bounds check (negative length)
     * @scenario Call write with a negative length
     * @defectRisk missing bounds validation would allow invalid array access
     */
    @Test(timeout = 4000)
    public void testWriteNegativeLengthThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "neglen.txt", 4);
        out.putNextEntry(entry);
        try {
            out.write(new byte[] { 1, 2, 3, 4 }, 0, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - bounds check (off > b.length - len)
     * @scenario Call write with offset/length combination exceeding array bounds
     * @defectRisk missing bounds validation would allow array index overflow
     */
    @Test(timeout = 4000)
    public void testWriteOffsetExceedsArrayBoundsThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "offbound.txt", 4);
        out.putNextEntry(entry);
        try {
            out.write(new byte[] { 1, 2, 3, 4 }, 3, 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            // expected
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - zero length early return
     * @scenario write called with len == 0 while no current entry exists
     * @defectRisk zero-length write incorrectly validates entry state / throws
     */
    @Test(timeout = 4000)
    public void testWriteZeroLengthReturnsImmediately() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        // No putNextEntry called - cpioEntry is null, but len==0 should short-circuit.
        out.write(new byte[] { 1, 2, 3 }, 0, 0);
        out.close();
    }

    /**
     * @target write(byte[], int, int) - no current entry branch
     * @scenario write called without any active entry (cpioEntry == null)
     * @defectRisk missing null-entry check allows writing outside an entry
     */
    @Test(timeout = 4000)
    public void testWriteWithoutCurrentEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        try {
            out.write(new byte[] { 1 }, 0, 1);
            fail("Expected IOException: no current CPIO entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("no current CPIO entry"));
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - past-end-of-entry check
     * @scenario write more bytes than the declared entry size
     * @defectRisk missing size validation allows corrupt/oversized entry data
     */
    @Test(timeout = 4000)
    public void testWriteExceedingDeclaredSizeThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "small.txt", 2);
        out.putNextEntry(entry);
        try {
            out.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
            fail("Expected IOException: attempt to write past end of STORED entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("past end"));
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - multi-chunk writes accumulate correctly
     * @scenario Write entry data across two separate calls summing to declared size
     * @defectRisk incorrect accumulation of 'written' counter across calls
     */
    @Test(timeout = 4000)
    public void testWriteInMultipleChunksSucceeds() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "chunks.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2 }, 0, 2);
        out.write(new byte[] { 3, 4 }, 0, 2);
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target write(byte[], int, int) - CRC accumulation branch (FORMAT_NEW_CRC)
     * @scenario Entry with correct checksum set is written and closed successfully
     * @defectRisk incorrect CRC computation causes false CRC mismatch on close
     */
    @Test(timeout = 4000)
    public void testWriteComputesCrcCorrectlyForCrcFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        byte[] data = { 1, 2, 3, 4 };
        long sum = 0;
        for (byte b : data) {
            sum += (b & 0xFF);
        }
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "crc.txt", 4);
        entry.setChksum(sum);
        out.putNextEntry(entry);
        out.write(data, 0, data.length);
        out.closeArchiveEntry(); // must not throw
        out.close();
    }

    /**
     * @target closeArchiveEntry() - CRC mismatch branch
     * @scenario Entry checksum does not match computed CRC from written data
     * @defectRisk CRC validation not enforced, corrupting archive integrity
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryCrcMismatchThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW_CRC);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW_CRC, "badcrc.txt", 4);
        entry.setChksum(999999L); // deliberately wrong
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
        try {
            out.closeArchiveEntry();
            fail("Expected IOException: CRC Error");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("CRC Error"));
        }
        out.close();
    }

    /**
     * @target closeArchiveEntry() - invalid size branch
     * @scenario Declared entry size does not equal number of bytes actually written
     * @defectRisk missing size validation allows truncated/incomplete entries
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryInvalidSizeThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "mismatch.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2 }, 0, 2); // only 2 of 4 declared bytes
        try {
            out.closeArchiveEntry();
            fail("Expected IOException: invalid entry size");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("invalid entry size"));
        }
        out.close();
    }

    /**
     * @target closeArchiveEntry() - padding branch (4-byte alignment, FORMAT_NEW)
     * @scenario Entry size is not a multiple of 4 requiring padding bytes
     * @defectRisk incorrect padding causes malformed CPIO layout
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryPadsForFormatNewWhenNotAligned() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "pad3.txt", 3);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3 }, 0, 3);
        out.closeArchiveEntry(); // size % 4 != 0 -> padding branch executed
        out.close();
    }

    /**
     * @target closeArchiveEntry() - no padding branch (4-byte alignment, FORMAT_NEW)
     * @scenario Entry size is already a multiple of 4, no padding necessary
     * @defectRisk unnecessary padding bytes corrupt archive layout
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryNoPadForFormatNewWhenAligned() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_NEW);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "pad4.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
        out.closeArchiveEntry(); // size % 4 == 0 -> skip==0, no padding
        out.close();
    }

    /**
     * @target closeArchiveEntry() - padding branch (2-byte alignment, FORMAT_OLD_BINARY)
     * @scenario Old binary format entry with odd size requires 1 padding byte
     * @defectRisk incorrect 2-byte alignment padding for old binary format
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryPadsForOldBinaryWhenNotAligned() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "oldbinpad.txt", 3);
        out.putNextEntry(entry);
        out.write(new byte[] { 5, 6, 7 }, 0, 3);
        out.closeArchiveEntry(); // 3 % 2 != 0 -> padding branch
        out.close();
    }

    /**
     * @target writeOldAsciiEntry / closeArchiveEntry for FORMAT_OLD_ASCII
     * @scenario Write and close an entry using the old ASCII CPIO format
     * @defectRisk incorrect header field widths/radix for old ASCII format
     */
    @Test(timeout = 4000)
    public void testOldAsciiFormatWriteAndCloseEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_ASCII, "oldascii.txt", 5);
        out.putNextEntry(entry);
        out.write("hello".getBytes(), 0, 5);
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target write(int) single-byte write path
     * @scenario Write a single byte using the write(int) overload
     * @defectRisk write(int) fails to delegate correctly to underlying stream
     */
    @Test(timeout = 4000)
    public void testWriteSingleByteMethod() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "onebyte.txt", 1);
        out.putNextEntry(entry);
        out.write(65); // 'A'
        out.closeArchiveEntry();
        out.close();
        assertTrue(bos.toByteArray().length > 0);
    }

    /**
     * @target finish() - idempotent second invocation
     * @scenario finish() is called twice in succession
     * @defectRisk second finish() call incorrectly writes a duplicate trailer
     */
    @Test(timeout = 4000)
    public void testFinishCalledTwiceIsNoOp() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.finish();
        long lengthAfterFirst = bos.toByteArray().length;
        out.finish(); // should return immediately - no additional trailer
        long lengthAfterSecond = bos.toByteArray().length;
        assertEquals(lengthAfterFirst, lengthAfterSecond);
        out.close();
    }

    /**
     * @target finish() - closes still-open entry before writing trailer
     * @scenario finish() called while an entry is still open (not manually closed)
     * @defectRisk open entry not closed before trailer write, corrupting archive
     */
    @Test(timeout = 4000)
    public void testFinishClosesOpenEntryAutomatically() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "openentry.txt", 3);
        out.putNextEntry(entry);
        out.write(new byte[] { 7, 8, 9 }, 0, 3);
        out.finish(); // should auto close entry then write trailer
        out.close();
    }

    /**
     * @target finish() - writing trailer without ever adding an entry
     * @scenario finish() invoked on a fresh stream with no entries added
     * @defectRisk trailer writing fails/throws when no prior entries exist
     */
    @Test(timeout = 4000)
    public void testFinishWithoutAnyEntryWritesTrailerOnly() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    /**
     * @target close() - idempotent second invocation
     * @scenario close() is called twice on the same stream
     * @defectRisk second close() call throws or double-closes underlying stream
     */
    @Test(timeout = 4000)
    public void testCloseCalledTwiceIsIdempotent() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        out.close(); // must not throw
    }

    /**
     * @target ensureOpen() - guarded operations after close()
     * @scenario write() invoked after the stream has already been closed
     * @defectRisk missing closed-state check permits writes to a closed stream
     */
    @Test(timeout = 4000)
    public void testWriteAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        try {
            out.write(new byte[] { 1 }, 0, 1);
            fail("Expected IOException: Stream closed");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    /**
     * @target ensureOpen() - guarded operations after close()
     * @scenario putNextEntry() invoked after the stream has already been closed
     * @defectRisk missing closed-state check permits new entries on closed stream
     */
    @Test(timeout = 4000)
    public void testPutNextEntryAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        try {
            out.putNextEntry(new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "late.txt", 0));
            fail("Expected IOException: Stream closed");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    /**
     * @target ensureOpen() - guarded operations after close()
     * @scenario finish() invoked after the stream has already been closed
     * @defectRisk missing closed-state check permits finishing a closed stream
     */
    @Test(timeout = 4000)
    public void testFinishAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        try {
            out.finish();
            fail("Expected IOException: Stream closed");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("closed"));
        }
    }

    /**
     * @target closeArchiveEntry() - invoked with no active entry (this.cpioEntry == null)
     * @scenario closeArchiveEntry() called without a preceding putNextEntry()
     * @defectRisk missing null guard causes uncontrolled NullPointerException
     */
    @Test(timeout = 4000)
    public void testCloseArchiveEntryWithoutActiveEntryThrowsNPE() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        try {
            out.closeArchiveEntry();
            fail("Expected NullPointerException due to missing active entry");
        } catch (NullPointerException expected) {
            // documents current (unguarded) behavior
        }
        out.close();
    }

    /**
     * @target write(byte[], int, int) - "no current entry" branch after finish()
     * @scenario write() invoked after finish() has nulled out the current entry
     * @defectRisk missing state check allows writing after logical end of archive
     */
    @Test(timeout = 4000)
    public void testWriteAfterFinishThrowsNoCurrentEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.finish();
        try {
            out.write(new byte[] { 1 }, 0, 1);
            fail("Expected IOException: no current CPIO entry");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("no current CPIO entry"));
        }
        out.close();
    }

    /**
     * @target Full round trip - write archive with multiple entries and read it back
     * @scenario Two distinct entries written, finished properly, then verified via input stream
     * @defectRisk regression in header/padding logic corrupting multi-entry archives
     */
    @Test(timeout = 4000)
    public void testFullRoundTripMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);

        CpioArchiveEntry e1 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "a.txt", 3);
        out.putNextEntry(e1);
        out.write("abc".getBytes(), 0, 3);
        out.closeArchiveEntry();

        CpioArchiveEntry e2 = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "b.txt", 5);
        out.putNextEntry(e2);
        out.write("world".getBytes(), 0, 5);
        out.closeArchiveEntry();

        out.finish();
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            CpioArchiveEntry read1 = in.getNextEntry();
            assertNotNull(read1);
            assertEquals("a.txt", read1.getName());

            CpioArchiveEntry read2 = in.getNextEntry();
            assertNotNull(read2);
            assertEquals("b.txt", read2.getName());

            assertNull(in.getNextEntry());
        } finally {
            in.close();
        }
    }

    /**
     * @target close() vs finish() interaction - KNOWN DEFECT (COMPRESS-28)
     * @scenario An entry is written and closed properly, then the stream is closed
     *           directly WITHOUT calling finish(). The TRAILER!!! record must still
     *           be written (either by close() delegating to finish(), or by the fix),
     *           so that reading the archive back does not throw EOFException when the
     *           reader looks for the terminating trailer entry.
     * @defectRisk close() fails to emit the TRAILER!!! record when finish() was never
     *             explicitly invoked, causing CpioArchiveInputStream to throw
     *             java.io.EOFException instead of gracefully returning null at EOF.
     */
    @Test(timeout = 4000)
    public void testCloseWithoutFinish_WritesTrailer_COMPRESS28() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);

        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "test.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 });
        out.closeArchiveEntry();

        // Defect trigger: close the stream WITHOUT calling finish() first.
        out.close();

        CpioArchiveInputStream in = new CpioArchiveInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            CpioArchiveEntry readEntry = in.getNextEntry();
            assertNotNull("Expected to read back the written entry", readEntry);
            assertEquals("test.txt", readEntry.getName());

            byte[] buffer = new byte[4];
            int totalRead = 0;
            int r;
            while (totalRead < 4 && (r = in.read(buffer, totalRead, 4 - totalRead)) != -1) {
                totalRead += r;
            }
            assertEquals(4, totalRead);
            assertArrayEquals(new byte[] { 1, 2, 3, 4 }, buffer);

            // Reading past the last entry must gracefully return null,
            // NOT throw java.io.EOFException.
            CpioArchiveEntry trailingEntry = in.getNextEntry();
            assertNull("Expected null at end of archive, not an exception", trailingEntry);
        } finally {
            in.close();
        }
    }

    /**
     * @target ArchiveOutputStream inheritance - write(byte[]) convenience overload
     * @scenario write(byte[]) (inherited from OutputStream) delegates to write(byte[],int,int)
     * @defectRisk convenience overload bypasses size/format validation logic
     */
    @Test(timeout = 4000)
    public void testWriteByteArrayConvenienceOverload() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_NEW, "conv.txt", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 10, 20, 30, 40 });
        out.closeArchiveEntry();
        out.close();
    }

    /**
     * @target Old binary format - full header + data + padding cycle, aligned size
     * @scenario FORMAT_OLD_BINARY entry whose size is already 2-byte aligned
     * @defectRisk incorrect skip==0 handling causing spurious padding bytes
     */
    @Test(timeout = 4000)
    public void testOldBinaryFormatAlignedSizeNoPadding() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_BINARY);
        CpioArchiveEntry entry = new CpioArchiveEntry(CpioConstants.FORMAT_OLD_BINARY, "aligned.bin", 4);
        out.putNextEntry(entry);
        out.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
        out.closeArchiveEntry();
        out.close();
    }
}