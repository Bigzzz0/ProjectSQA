package org.apache.commons.compress.archivers.ar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ArArchiveInputStream
 * 
 * Branches covered:
 * 1. offset == 0 (first call) -> read global header, validate
 * 2. offset != 0 -> skip global header
 * 3. input.available() == 0 -> return null (EOF)
 * 4. offset % 2 != 0 -> read padding byte
 * 5. read(byte[]) returns less than expected -> IOException
 * 6. Header field parsing (name, length) -> trim, parseLong
 * 7. Trailer validation -> IOException if mismatch
 * 8. read() and read(byte[],int,int) -> offset update
 * 9. close() -> idempotent
 * 10. matches() static method -> signature check
 * 
 * Boundary values:
 * - Empty archive (only global header)
 * - Single entry with zero-length data
 * - Single entry with odd-length data (padding)
 * - Single entry with even-length data
 * - Multiple entries
 * - Invalid global header
 * - Invalid entry header (trailer mismatch)
 * - Negative/zero length in header (parseLong)
 * - Truncated header fields (read returns less)
 * 
 * Defect-targeted test (Defects4J: testArDelete):
 * The known defect causes getNextArEntry() to return null when an entry exists.
 * We simulate a valid archive with one entry and assert that the first call
 * returns a non-null entry. If the bug is present, this test will fail with
 * expected non-null but got null, matching the "expected:<1> but was:<0>" pattern.
 */
public class ArArchiveInputStreamDeepseekTest {

    // Helper to build a valid AR archive byte array with one entry
    private byte[] buildSingleEntryArchive(String name, long length, byte[] data) {
        // Global header
        byte[] globalHeader = "!<arch>\n".getBytes();
        // Entry header fields (each field is space-padded to fixed width)
        String nameField = String.format("%-16s", name);
        String lastModified = String.format("%-12s", "0");
        String userId = String.format("%-6s", "0");
        String groupId = String.format("%-6s", "0");
        String fileMode = String.format("%-8s", "100644");
        String lengthField = String.format("%-10s", Long.toString(length));
        String trailer = "`\n";
        String headerStr = nameField + lastModified + userId + groupId + fileMode + lengthField + trailer;
        byte[] header = headerStr.getBytes();
        // Data
        byte[] dataBytes = (data != null) ? data : new byte[0];
        // Padding if data length is odd
        int padding = (length % 2 != 0) ? 1 : 0;
        byte[] archive = new byte[globalHeader.length + header.length + dataBytes.length + padding];
        int pos = 0;
        System.arraycopy(globalHeader, 0, archive, pos, globalHeader.length);
        pos += globalHeader.length;
        System.arraycopy(header, 0, archive, pos, header.length);
        pos += header.length;
        System.arraycopy(dataBytes, 0, archive, pos, dataBytes.length);
        pos += dataBytes.length;
        if (padding == 1) {
            archive[pos] = 0; // padding byte (any value)
        }
        return archive;
    }

    // Helper to build an empty archive (only global header)
    private byte[] buildEmptyArchive() {
        return "!<arch>\n".getBytes();
    }

    // Helper to build an archive with multiple entries
    private byte[] buildMultiEntryArchive() {
        byte[] entry1 = buildSingleEntryArchive("file1.txt", 4, "data".getBytes());
        byte[] entry2 = buildSingleEntryArchive("file2.txt", 5, "hello".getBytes());
        byte[] combined = new byte[entry1.length + entry2.length];
        System.arraycopy(entry1, 0, combined, 0, entry1.length);
        System.arraycopy(entry2, 0, combined, entry1.length, entry2.length);
        return combined;
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testGetNextArEntry_SingleEntry() throws IOException {
        byte[] archive = buildSingleEntryArchive("test.txt", 4, "data".getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull("First entry should not be null", entry);
        assertEquals("Entry name", "test.txt", entry.getName());
        assertEquals("Entry length", 4, entry.getLength());
        // Read data
        byte[] buf = new byte[4];
        int read = in.read(buf);
        assertEquals("Read data length", 4, read);
        assertArrayEquals("Data content", "data".getBytes(), buf);
        // Next entry should be null
        assertNull("No more entries", in.getNextArEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_MultipleEntries() throws IOException {
        byte[] archive = buildMultiEntryArchive();
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry1 = in.getNextArEntry();
        assertNotNull("First entry", entry1);
        assertEquals("file1.txt", entry1.getName());
        assertEquals(4, entry1.getLength());
        byte[] buf1 = new byte[4];
        in.read(buf1);
        assertArrayEquals("data".getBytes(), buf1);

        ArArchiveEntry entry2 = in.getNextArEntry();
        assertNotNull("Second entry", entry2);
        assertEquals("file2.txt", entry2.getName());
        assertEquals(5, entry2.getLength());
        byte[] buf2 = new byte[5];
        in.read(buf2);
        assertArrayEquals("hello".getBytes(), buf2);

        assertNull("No more entries", in.getNextArEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_EmptyArchive() throws IOException {
        byte[] archive = buildEmptyArchive();
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        assertNull("Empty archive should return null", in.getNextArEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_ZeroLengthData() throws IOException {
        byte[] archive = buildSingleEntryArchive("empty.txt", 0, new byte[0]);
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull("Entry with zero length", entry);
        assertEquals("empty.txt", entry.getName());
        assertEquals(0, entry.getLength());
        // Reading data should return -1 or 0? Actually read() returns 0 for zero-length? We'll just check next entry null
        assertNull("No more entries", in.getNextArEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_OddLengthData() throws IOException {
        // Data length odd -> padding byte present
        byte[] archive = buildSingleEntryArchive("odd.txt", 3, "abc".getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull("Entry with odd length", entry);
        assertEquals("odd.txt", entry.getName());
        assertEquals(3, entry.getLength());
        byte[] buf = new byte[3];
        int read = in.read(buf);
        assertEquals(3, read);
        assertArrayEquals("abc".getBytes(), buf);
        // After reading data, offset is odd (8+60+3=71). Next call should skip padding byte.
        assertNull("No more entries after padding", in.getNextArEntry());
        in.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testGetNextArEntry_InvalidGlobalHeader() throws IOException {
        // Wrong global header
        byte[] archive = "!<arch>X\n".getBytes(); // 8 bytes but wrong
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        try {
            in.getNextArEntry();
            fail("Expected IOException for invalid global header");
        } catch (IOException e) {
            // expected
        }
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_TruncatedGlobalHeader() throws IOException {
        // Only 7 bytes
        byte[] archive = "!<arch>".getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        try {
            in.getNextArEntry();
            fail("Expected IOException for truncated header");
        } catch (IOException e) {
            // expected
        }
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_InvalidEntryTrailer() throws IOException {
        // Build archive with correct global header but wrong entry trailer
        byte[] globalHeader = "!<arch>\n".getBytes();
        String nameField = String.format("%-16s", "test");
        String lastModified = String.format("%-12s", "0");
        String userId = String.format("%-6s", "0");
        String groupId = String.format("%-6s", "0");
        String fileMode = String.format("%-8s", "100644");
        String lengthField = String.format("%-10s", "0");
        String trailer = "XX"; // invalid trailer
        String headerStr = nameField + lastModified + userId + groupId + fileMode + lengthField + trailer;
        byte[] header = headerStr.getBytes();
        byte[] archive = new byte[globalHeader.length + header.length];
        System.arraycopy(globalHeader, 0, archive, 0, globalHeader.length);
        System.arraycopy(header, 0, archive, globalHeader.length, header.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        try {
            in.getNextArEntry();
            fail("Expected IOException for invalid trailer");
        } catch (IOException e) {
            // expected
        }
        in.close();
    }

    @Test(timeout = 4000)
    public void testGetNextArEntry_NegativeLengthInHeader() throws IOException {
        // Length field with negative number (should cause NumberFormatException)
        byte[] globalHeader = "!<arch>\n".getBytes();
        String nameField = String.format("%-16s", "test");
        String lastModified = String.format("%-12s", "0");
        String userId = String.format("%-6s", "0");
        String groupId = String.format("%-6s", "0");
        String fileMode = String.format("%-8s", "100644");
        String lengthField = String.format("%-10s", "-1"); // negative
        String trailer = "`\n";
        String headerStr = nameField + lastModified + userId + groupId + fileMode + lengthField + trailer;
        byte[] header = headerStr.getBytes();
        byte[] archive = new byte[globalHeader.length + header.length];
        System.arraycopy(globalHeader, 0, archive, 0, globalHeader.length);
        System.arraycopy(header, 0, archive, globalHeader.length, header.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        try {
            in.getNextArEntry();
            fail("Expected NumberFormatException for negative length");
        } catch (NumberFormatException e) {
            // expected
        }
        in.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testDefect_ArDelete_SingleEntryReturnsNonNull() throws IOException {
        // This test directly targets the known defect: getNextArEntry() returns null
        // when there is a valid entry. We construct a simple archive with one entry
        // and assert that the first call returns a non-null entry.
        byte[] archive = buildSingleEntryArchive("delete_test.txt", 10, "1234567890".getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull("Defect: getNextArEntry() should return an entry for a non-empty archive", entry);
        assertEquals("delete_test.txt", entry.getName());
        assertEquals(10, entry.getLength());
        // Read data to ensure stream is consumed
        byte[] buf = new byte[10];
        int read = in.read(buf);
        assertEquals(10, read);
        assertArrayEquals("1234567890".getBytes(), buf);
        // Next entry should be null
        assertNull(in.getNextArEntry());
        in.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testRead_AfterClose() throws IOException {
        byte[] archive = buildSingleEntryArchive("test.txt", 0, new byte[0]);
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        in.close();
        // read() should throw IOException? Actually, underlying stream is closed, but read() may return -1 or throw.
        // We'll just ensure no exception from close itself.
        // The read() method will call input.read() which may throw IOException if stream is closed.
        try {
            in.read();
            // May or may not throw depending on implementation; we don't assert.
        } catch (IOException e) {
            // acceptable
        }
    }

    @Test(timeout = 4000)
    public void testClose_Idempotent() throws IOException {
        byte[] archive = buildEmptyArchive();
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        in.close();
        in.close(); // should not throw
    }

    @Test(timeout = 4000)
    public void testRead_ByteArray() throws IOException {
        byte[] archive = buildSingleEntryArchive("test.txt", 5, "hello".getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull(entry);
        byte[] buf = new byte[5];
        int read = in.read(buf);
        assertEquals(5, read);
        assertArrayEquals("hello".getBytes(), buf);
        in.close();
    }

    @Test(timeout = 4000)
    public void testRead_ByteArrayOffsetLen() throws IOException {
        byte[] archive = buildSingleEntryArchive("test.txt", 5, "hello".getBytes());
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArArchiveEntry entry = in.getNextArEntry();
        assertNotNull(entry);
        byte[] buf = new byte[10];
        int read = in.read(buf, 2, 5);
        assertEquals(5, read);
        assertArrayEquals(new byte[]{0,0,'h','e','l','l','o',0,0,0}, buf);
        in.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMatches_ValidSignature() {
        byte[] sig = new byte[]{0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a};
        assertTrue("Valid signature should match", ArArchiveInputStream.matches(sig, 8));
    }

    @Test(timeout = 4000)
    public void testMatches_ShortSignature() {
        byte[] sig = new byte[]{0x21, 0x3c, 0x61};
        assertFalse("Short signature should not match", ArArchiveInputStream.matches(sig, 3));
    }

    @Test(timeout = 4000)
    public void testMatches_WrongByte() {
        byte[] sig = new byte[]{0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0b}; // last byte wrong
        assertFalse("Wrong byte should not match", ArArchiveInputStream.matches(sig, 8));
    }

    @Test(timeout = 4000)
    public void testMatches_AllBytesWrong() {
        byte[] sig = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        assertFalse("All zeros should not match", ArArchiveInputStream.matches(sig, 8));
    }

    @Test(timeout = 4000)
    public void testGetNextEntry_DelegatesToGetNextArEntry() throws IOException {
        byte[] archive = buildSingleEntryArchive("test.txt", 0, new byte[0]);
        ByteArrayInputStream bais = new ByteArrayInputStream(archive);
        ArArchiveInputStream in = new ArArchiveInputStream(bais);
        ArchiveEntry entry = in.getNextEntry();
        assertNotNull("getNextEntry should delegate", entry);
        assertTrue("Should be ArArchiveEntry", entry instanceof ArArchiveEntry);
        assertEquals("test.txt", entry.getName());
        in.close();
    }
}