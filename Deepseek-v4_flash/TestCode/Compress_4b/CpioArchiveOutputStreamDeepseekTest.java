package org.apache.commons.compress.archivers.cpio;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * ==============================================
 * Partition A: Core Functional Logic & State Transitions
 *   - Normal entry write with FORMAT_NEW, FORMAT_NEW_CRC, FORMAT_OLD_ASCII, FORMAT_OLD_BINARY
 *   - Multiple consecutive entries, finish, close
 *   - Verify CRC accumulation and check on closeArchiveEntry (FORMAT_NEW_CRC)
 *   - Duplicate entry detection via names map
 *   - Entry size mismatch detection (size != written)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Zero-length entry data
 *   - Very large entry size (Long.MAX_VALUE? Not testable due to memory, but size match)
 *   - Null/empty entry name (CpioArchiveEntry may allow? Should guard)
 *   - Off-by-one on write offsets (off, len combinations)
 *   - Negative arguments for off/len -> IndexOutOfBoundsException
 *   - len == 0 early return
 *   - write when written+len == entry.size (exact boundary)
 *   - write when written+len > entry.size (throw)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - CRC mismatch between computed crc and entry.getChksum() -> IOException
 *   - Closing archive entry when no entry is open (after finish) -> should not be possible, but ensure
 *   - finish() called twice -> second call no-op
 *   - close() without finishing -> finish called, then out closed
 *   - EnsureOpen checks before each operation on closed stream
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - putArchiveEntry with non-CpioArchiveEntry (ClassCastException)
 *   - putArchiveEntry with null name -> NPE from names.put
 *   - Invalid format in constructor -> IllegalArgumentException
 *   - Create stream with null OutputStream -> NPE
 *   - Write after close -> IOException
 *   - Write with no current entry -> IOException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Stream closed after close() -> double close safe
 *   - finish() after close() -> IOException (ensureOpen)
 *   - Entry properties correctly forwarded to underlying stream (byte-level verification not required, but structural)
 * 
 * Coverage Targets:
 *   - All branches in: putArchiveEntry, closeArchiveEntry, write(byte[],int,int), finish, close, writeHeader, pad, writeBinaryLong, writeAsciiLong, writeCString, ensureOpen, constructors
 *   - All format cases in writeHeader: 4 branches
 *   - CRC accumulation loop in write (pos < len)
 *   - CRC check in closeArchiveEntry
 *   - Time set branch (e.getTime() == -1)
 *   - Duplicate entry branch (names.put != null)
 *   - Entry size mismatch branch
 *   - Written length boundary check
 *   - Zero-length write early return
 *   - Off/len validation
 *   - EnsureOpen guard in each public method
 */
public class CpioArchiveOutputStreamDeepseekTest {

    // Helper to create a default entry for testing
    private CpioArchiveEntry createEntry(String name, long size, short format) {
        CpioArchiveEntry e = new CpioArchiveEntry(format);
        e.setName(name);
        e.setSize(size);
        e.setMode(CpioConstants.C_ISREG); // regular file
        e.setTime(System.currentTimeMillis());
        e.setNumberOfLinks(1);
        return e;
    }

    // Helper to get a ByteArrayOutputStream wrapped in CpioArchiveOutputStream with specified format
    private CpioArchiveOutputStream createStream(short format, ByteArrayOutputStream bos) {
        return new CpioArchiveOutputStream(bos, format);
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testWriteSingleEntryNewFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("test", 5, (short) CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3,4,5}, 0, 5);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue("Output should contain some bytes", bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteSingleEntryNewCrcFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = createStream(CpioConstants.FORMAT_NEW_CRC, bos);
        CpioArchiveEntry entry = createEntry("crcTest", 3, CpioConstants.FORMAT_NEW_CRC);
        // Set checksum to expected value (1+2+3=6)
        entry.setChksum(6);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3}, 0, 3);
        out.closeArchiveEntry(); // should pass CRC check
        out.finish();
        out.close();
        // If defect existed (CRC mismatch code path), would throw here
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testWriteSingleEntryOldAsciiFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = createStream(CpioConstants.FORMAT_OLD_ASCII, bos);
        CpioArchiveEntry entry = createEntry("old", 0, CpioConstants.FORMAT_OLD_ASCII);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testWriteSingleEntryOldBinaryFormat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = createStream(CpioConstants.FORMAT_OLD_BINARY, bos);
        CpioArchiveEntry entry = createEntry("bin", 0, CpioConstants.FORMAT_OLD_BINARY);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish();
        out.close();
        assertTrue(bos.size() > 0);
    }

    @Test(timeout = 4000)
    public void testMultipleEntries() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        for (int i = 0; i < 3; i++) {
            CpioArchiveEntry entry = createEntry("file"+i, i, CpioConstants.FORMAT_NEW);
            out.putArchiveEntry(entry);
            out.write(new byte[i], 0, i);
            out.closeArchiveEntry();
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testDuplicateEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry1 = createEntry("dup", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry1);
        out.closeArchiveEntry();
        CpioArchiveEntry entry2 = createEntry("dup", 0, CpioConstants.FORMAT_NEW);
        try {
            out.putArchiveEntry(entry2);
            fail("Expected IOException for duplicate entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("duplicate entry"));
        }
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testEntrySizeMismatchThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("mismatch", 5, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2,3}, 0, 3); // only 3 bytes, but size=5
        try {
            out.closeArchiveEntry();
            fail("Expected IOException for size mismatch");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("invalid entry size"));
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testCrcMismatchThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = createStream(CpioConstants.FORMAT_NEW_CRC, bos);
        CpioArchiveEntry entry = createEntry("crcBad", 2, CpioConstants.FORMAT_NEW_CRC);
        entry.setChksum(99); // wrong checksum
        out.putArchiveEntry(entry);
        out.write(new byte[]{10,20}, 0, 2);
        try {
            out.closeArchiveEntry();
            fail("Expected IOException for CRC mismatch");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("CRC Error"));
        }
        out.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testWriteZeroLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("zero", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        // write zero-length bytes
        out.write(new byte[0], 0, 0); // this goes through write method, should early return
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteExactSizeBoundary() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("exact", 10, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.write(new byte[10], 0, 10); // exactly fills entry
        out.closeArchiveEntry(); // should pass
        out.finish();
        out.close();
    }

    @Test(timeout = 4000)
    public void testWritePastEndThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("overflow", 3, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[5], 0, 5); // tries to write 5 > 3
            fail("Expected IOException for writing past end");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("attempt to write past end of STORED entry"));
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteNegativeOffThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("neg", 10, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[5], -1, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteNegativeLenThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("negLen", 10, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[5], 0, -3);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteOffBeyondLengthThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("offBeyond", 10, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        try {
            out.write(new byte[5], 6, 1); // off=6 > b.length=5
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
        out.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testFinishCalledTwice() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("first", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.finish(); // first call
        out.finish(); // second call should be no-op
        out.close();
    }

    @Test(timeout = 4000)
    public void testFinishWithUnclosedEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("unclosed", 5, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        // not closed before finish
        try {
            out.finish();
            fail("Expected IOException for unclosed entry");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("unclosed entries"));
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testWriteAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        try {
            out.write(new byte[1], 0, 1);
            fail("Expected IOException for closed stream");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Stream closed"));
        }
    }

    @Test(timeout = 4000)
    public void testPutArchiveEntryAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        out.close();
        try {
            CpioArchiveEntry entry = createEntry("afterClose", 0, CpioConstants.FORMAT_NEW);
            out.putArchiveEntry(entry);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Stream closed"));
        }
    }

    @Test(timeout = 4000)
    public void testCloseArchiveEntryAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("first", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close();
        // stream closed, but closeArchiveEntry on a fresh entry? Already closed. Not possible.
        // We can test ensureOpen inside closeArchiveEntry: try calling after close
        try {
            out.closeArchiveEntry(); // no current entry, but ensureOpen will fail
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Stream closed"));
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidFormatInConstructor() {
        new CpioArchiveOutputStream(new ByteArrayOutputStream(), (short) 0xFFFF);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testPutArchiveEntryNonCpioEntry() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        ArchiveEntry nonCpio = new ArchiveEntry() {
            @Override public String getName() { return "fake"; }
            @Override public long getSize() { return 0; }
            @Override public boolean isDirectory() { return false; }
        };
        out.putArchiveEntry(nonCpio); // should throw ClassCastException
    }

    @Test(timeout = 4000)
    public void testWriteWithoutEntryThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        try {
            out.write(new byte[1], 0, 1);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("no current CPIO entry"));
        }
        out.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullOutputStream() {
        try {
            new CpioArchiveOutputStream(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected because FilterOutputStream(null) throws NPE
        }
    }

    @Test(timeout = 4000)
    public void testDefaultFormat() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        // default is FORMAT_NEW
        assertNotNull(out);
        // can't access entryFormat directly, but we can test with putArchiveEntry
        CpioArchiveEntry entry = createEntry("default", 0, CpioConstants.FORMAT_NEW);
        // no exception expected
        try {
            out.putArchiveEntry(entry);
            out.closeArchiveEntry();
            out.finish();
            out.close();
        } catch (IOException e) {
            fail("Should not throw: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFormatMismatchThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos, CpioConstants.FORMAT_OLD_ASCII);
        CpioArchiveEntry entry = createEntry("mismatch", 0, CpioConstants.FORMAT_NEW); // different format
        try {
            out.putArchiveEntry(entry);
            fail("Expected IOException for format mismatch");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("does not match existing format"));
        }
        out.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testDoubleClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("doubleClose", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close(); // first close
        out.close(); // second close should be safe (closed already)
    }

    @Test(timeout = 4000)
    public void testFinishAfterCloseThrows() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = new CpioArchiveOutputStream(bos);
        CpioArchiveEntry entry = createEntry("test", 0, CpioConstants.FORMAT_NEW);
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
        out.close(); // close calls finish internally, sets closed=true
        try {
            out.finish(); // ensureOpen will fail
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Stream closed"));
        }
    }

    // Additional edge case: CRC accumulation across multiple writes
    @Test(timeout = 4000)
    public void testCrcMultipleWrites() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CpioArchiveOutputStream out = createStream(CpioConstants.FORMAT_NEW_CRC, bos);
        CpioArchiveEntry entry = createEntry("multiWrite", 5, CpioConstants.FORMAT_NEW_CRC);
        entry.setChksum(1+2+3+4+5); // sum of bytes
        out.putArchiveEntry(entry);
        out.write(new byte[]{1,2}, 0, 2);
        out.write(new byte[]{3,4,5}, 0, 3);
        out.closeArchiveEntry();
        out.finish();
        out.close();
    }
}