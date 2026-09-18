/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.ar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: ArArchiveInputStream
 *
 * 1. matches(byte[] signature, int length):
 *    - Branch: length < 8 (returns false) [tested with length -1, 0, 7]
 *    - Branch: length >= 8
 *    - Condition branches: signature[0..7] mismatch on each byte 0x21, 0x3c, 0x61, 0x72, 0x63, 0x68, 0x3e, 0x0a
 *    - Branch: all 8 bytes match "!<arch>\n" (returns true) [tested with length == 8 and length > 8]
 *
 * 2. getNextArEntry() / getNextEntry():
 *    - Branch: offset == 0 (initial entry check)
 *      * Header read count != expected.length (EOF before header) -> throws IOException("failed to read header")
 *      * Header content mismatch -> throws IOException("invalid header ...")
 *      * Header valid -> proceeds
 *    - Branch: offset != 0 (subsequent entries)
 *    - Branch: input.available() == 0 -> returns null
 *    - Branch: offset % 2 != 0 (odd padding byte alignment)
 *      * offset odd -> read() consumed
 *      * offset even -> read() skipped
 *    - Branch: trailer read count != expected.length -> throws IOException("failed to read entry header")
 *    - Branch: trailer content mismatch -> throws IOException("invalid entry header. not read the content?")
 *    - Non-numeric length in entry header -> throws NumberFormatException
 *
 * 3. read(), read(byte[]), read(byte[], int, int):
 *    - Branch: ret > 0 vs ret <= 0 for offset accumulation
 *    - Single byte read with ret == 0 (0x00 byte)
 *    - Single byte read with ret == -1 (EOF)
 *
 * 4. close():
 *    - Branch: !closed -> closed = true, input.close() invoked
 *    - Branch: closed -> no-op, input.close() not invoked again (idempotent)
 *
 * 5. DEFECT-TARGETED ZONE (Defects4J: ArTestCase::testArDelete -> expected:<1> but was:<0>):
 *    - The defect occurs because ArArchiveInputStream does not bound reads to the current entry's size.
 *      When copying/reading an entry to EOF (e.g., via read(byte[]) until -1), it drains past the entry
 *      boundary and consumes subsequent entries, preventing getNextEntry() from retrieving subsequent entries.
 *    - Test asserts:
 *      * ais.read(buf) must signal EOF (-1) when the entry's declared length has been consumed.
 *      * Sequential entries must be readable after reading previous entry content.
 * ----------------------------------------------------------------------------------------------------
 */
public class ArArchiveInputStreamGeminiTest {

    // --- Helper Methods for Archive Construction ---

    private static class EntryPayload {
        final String name;
        final long mtime;
        final int uid;
        final int gid;
        final int mode;
        final byte[] content;

        EntryPayload(String name, byte[] content) {
            this(name, 0L, 0, 0, 0100644, content);
        }

        EntryPayload(String name, long mtime, int uid, int gid, int mode, byte[] content) {
            this.name = name;
            this.mtime = mtime;
            this.uid = uid;
            this.gid = gid;
            this.mode = mode;
            this.content = content;
        }
    }

    private static String padRight(String s, int n) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static byte[] createArchive(EntryPayload... entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ArArchiveEntry.HEADER.getBytes(StandardCharsets.US_ASCII));
        for (EntryPayload entry : entries) {
            baos.write(padRight(entry.name, 16).getBytes(StandardCharsets.US_ASCII));
            baos.write(padRight(String.valueOf(entry.mtime), 12).getBytes(StandardCharsets.US_ASCII));
            baos.write(padRight(String.valueOf(entry.uid), 6).getBytes(StandardCharsets.US_ASCII));
            baos.write(padRight(String.valueOf(entry.gid), 6).getBytes(StandardCharsets.US_ASCII));
            baos.write(padRight(Integer.toOctalString(entry.mode), 8).getBytes(StandardCharsets.US_ASCII));
            baos.write(padRight(String.valueOf(entry.content.length), 10).getBytes(StandardCharsets.US_ASCII));
            baos.write(ArArchiveEntry.TRAILER.getBytes(StandardCharsets.US_ASCII));
            baos.write(entry.content);
            if (entry.content.length % 2 != 0) {
                baos.write('\n'); // 2-byte alignment padding
            }
        }
        return baos.toByteArray();
    }

    private static class CloseTrackingInputStream extends InputStream {
        private final InputStream delegate;
        int closeCount = 0;

        CloseTrackingInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public int read() throws IOException {
            return delegate.read();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return delegate.read(b, off, len);
        }

        @Override
        public int available() throws IOException {
            return delegate.available();
        }

        @Override
        public void close() throws IOException {
            closeCount++;
            delegate.close();
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadSingleEntrySuccessfully() throws IOException {
        byte[] content = "Hello Ar".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(new EntryPayload("test.txt", content));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();

        assertNotNull(entry);
        assertEquals("test.txt", entry.getName());
        assertEquals(content.length, entry.getLength());

        byte[] readContent = new byte[content.length];
        int readBytes = ais.read(readContent, 0, readContent.length);
        assertEquals(content.length, readBytes);
        assertArrayEquals(content, readContent);

        ais.close();
    }

    @Test(timeout = 4000)
    public void testGetNextEntryDelegation() throws IOException {
        byte[] content = "DelegationTest".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(new EntryPayload("delegated.txt", content));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArchiveEntry entry = ais.getNextEntry();

        assertNotNull(entry);
        assertEquals("delegated.txt", entry.getName());
        assertEquals(content.length, entry.getSize());
        ais.close();
    }

    @Test(timeout = 4000)
    public void testEvenLengthEntryNoPaddingConsumed() throws IOException {
        // Content with even length: 4 bytes
        byte[] content = new byte[]{1, 2, 3, 4};
        byte[] archive = createArchive(new EntryPayload("even.bin", content));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry = ais.getNextArEntry();
        assertNotNull(entry);
        assertEquals(4, entry.getLength());

        byte[] buf = new byte[4];
        int read = ais.read(buf);
        assertEquals(4, read);

        ArArchiveEntry next = ais.getNextArEntry();
        assertNull(next);
        ais.close();
    }

    @Test(timeout = 4000)
    public void testOddLengthEntryWithPaddingConsumed() throws IOException {
        // Content with odd length: 5 bytes
        byte[] content1 = new byte[]{'A', 'B', 'C', 'D', 'E'};
        byte[] content2 = new byte[]{'1', '2'};
        byte[] archive = createArchive(
                new EntryPayload("odd1.txt", content1),
                new EntryPayload("even2.txt", content2)
        );

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry1 = ais.getNextArEntry();
        assertNotNull(entry1);
        assertEquals("odd1.txt", entry1.getName());

        byte[] buf1 = new byte[5];
        assertEquals(5, ais.read(buf1));

        // When reading the next entry, odd offset padding must be consumed
        ArArchiveEntry entry2 = ais.getNextArEntry();
        assertNotNull(entry2);
        assertEquals("even2.txt", entry2.getName());

        byte[] buf2 = new byte[2];
        assertEquals(2, ais.read(buf2));

        assertNull(ais.getNextArEntry());
        ais.close();
    }

    @Test(timeout = 4000)
    public void testReadByteByByteWithZeroValue() throws IOException {
        // Ensure single-byte read() correctly handles byte 0x00 and positive bytes
        byte[] content = new byte[]{0x00, 0x7F, (byte) 0xFF};
        byte[] archive = createArchive(new EntryPayload("zeros.bin", content));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(ais.getNextArEntry());

        assertEquals(0x00, ais.read());
        assertEquals(0x7F, ais.read());
        assertEquals(0xFF, ais.read());

        ais.close();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Matches Matrix
    // =========================================================================

    @Test(timeout = 4000)
    public void testMatchesBoundaries() {
        byte[] valid = "!<arch>\n".getBytes(StandardCharsets.US_ASCII);

        // Boundary length < 8
        assertFalse(ArArchiveInputStream.matches(valid, -1));
        assertFalse(ArArchiveInputStream.matches(valid, 0));
        assertFalse(ArArchiveInputStream.matches(valid, 7));

        // Boundary length == 8
        assertTrue(ArArchiveInputStream.matches(valid, 8));

        // Boundary length > 8
        byte[] extended = "!<arch>\nextra_bytes".getBytes(StandardCharsets.US_ASCII);
        assertTrue(ArArchiveInputStream.matches(extended, extended.length));
    }

    @Test(timeout = 4000)
    public void testMatchesEachByteConditionBranch() {
        byte[] valid = "!<arch>\n".getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < 8; i++) {
            byte[] corrupted = valid.clone();
            corrupted[i] = (byte) (corrupted[i] ^ 0xFF); // flip bits
            assertFalse("Byte at index " + i + " must cause matches() to return false",
                    ArArchiveInputStream.matches(corrupted, 8));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyArchiveReturnsNullEntry() throws IOException {
        // Valid header but 0 entries
        byte[] archive = ArArchiveEntry.HEADER.getBytes(StandardCharsets.US_ASCII);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));

        ArArchiveEntry entry = ais.getNextArEntry();
        assertNull(entry);
        ais.close();
    }

    @Test(timeout = 4000)
    public void testAvailableZeroReturnsNullAtEof() throws IOException {
        byte[] archive = createArchive(new EntryPayload("one.txt", new byte[]{1, 2}));
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));

        assertNotNull(ais.getNextArEntry());
        byte[] buf = new byte[2];
        ais.read(buf);

        assertNull(ais.getNextArEntry());
        // Subsequent calls should consistently return null
        assertNull(ais.getNextArEntry());
        ais.close();
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J testArDelete)
    // =========================================================================

    /**
     * Targets the defect where ArArchiveInputStream fails to bound read() to the current entry.
     * When reading an entry to EOF (e.g., in IOUtils.copy or while ((r = ais.read(buf)) != -1)),
     * the stream should return -1 at the entry boundary. In the defective implementation, it drains
     * the entire underlying stream past the entry into subsequent entries, causing getNextEntry()
     * to return null and missing remaining entries (reproducing expected:<1> but was:<0>).
     */
    @Test(timeout = 4000)
    public void testArDeleteDefectTargetReadMustStopAtEntryBoundary() throws IOException {
        byte[] content1 = "1234567890".getBytes(StandardCharsets.US_ASCII); // 10 bytes
        byte[] content2 = "abcdefgh".getBytes(StandardCharsets.US_ASCII);   // 8 bytes

        byte[] archive = createArchive(
                new EntryPayload("test1.xml", content1),
                new EntryPayload("test2.xml", content2)
        );

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));

        int entriesFound = 0;
        int deleted = 0;
        ArchiveEntry entry;
        byte[] buffer = new byte[1024];

        while ((entry = ais.getNextEntry()) != null) {
            entriesFound++;
            if ("test2.xml".equals(entry.getName())) {
                deleted++;
                continue;
            }
            // Simulating IOUtils.copy(ais, aos)
            int totalEntryRead = 0;
            int r;
            while ((r = ais.read(buffer)) != -1) {
                totalEntryRead += r;
            }
            assertEquals("Must read exactly the declared entry length for test1.xml",
                    content1.length, totalEntryRead);
        }

        ais.close();

        assertEquals("Must discover both entries in the archive", 2, entriesFound);
        assertEquals("Defects4J testArDelete assertion: expected:<1> but was:<0>", 1, deleted);
    }

    /**
     * Targets entry boundary enforcement for single-byte read().
     */
    @Test(timeout = 4000)
    public void testSingleByteReadReturnsEofAtEntryBoundary() throws IOException {
        byte[] content1 = new byte[]{'x', 'y'};
        byte[] content2 = new byte[]{'z'};

        byte[] archive = createArchive(
                new EntryPayload("first.txt", content1),
                new EntryPayload("second.txt", content2)
        );

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry1 = ais.getNextArEntry();
        assertNotNull(entry1);

        assertEquals('x', ais.read());
        assertEquals('y', ais.read());
        // Reading past the declared 2 bytes of entry1 MUST return -1 (EOF for entry)
        assertEquals("read() must return -1 after current entry bytes are exhausted", -1, ais.read());

        ArArchiveEntry entry2 = ais.getNextArEntry();
        assertNotNull("Next entry should be reachable after exhausting current entry", entry2);
        assertEquals("second.txt", entry2.getName());

        ais.close();
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IOException.class, timeout = 4000)
    public void testCorruptGlobalHeaderThrowsIOException() throws IOException {
        byte[] corruptHeader = "INVALID!\n".getBytes(StandardCharsets.US_ASCII);
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(corruptHeader));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testTruncatedGlobalHeaderThrowsIOException() throws IOException {
        byte[] shortHeader = "!<ar".getBytes(StandardCharsets.US_ASCII); // only 4 bytes instead of 8
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(shortHeader));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testTruncatedEntryHeaderThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ArArchiveEntry.HEADER.getBytes(StandardCharsets.US_ASCII));
        baos.write("incomplete_entry_header".getBytes(StandardCharsets.US_ASCII)); // < 60 bytes

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCorruptedEntryTrailerThrowsIOException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ArArchiveEntry.HEADER.getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("file.txt", 16).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 12).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 6).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 6).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("100644", 8).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 10).getBytes(StandardCharsets.US_ASCII));
        baos.write("XX".getBytes(StandardCharsets.US_ASCII)); // Corrupt trailer (expected "`\n")

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ais.getNextArEntry();
    }

    @Test(expected = NumberFormatException.class, timeout = 4000)
    public void testNonNumericEntryLengthThrowsNumberFormatException() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(ArArchiveEntry.HEADER.getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("file.txt", 16).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 12).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 6).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("0", 6).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("100644", 8).getBytes(StandardCharsets.US_ASCII));
        baos.write(padRight("NaN_LENGTH", 10).getBytes(StandardCharsets.US_ASCII)); // Invalid number
        baos.write(ArArchiveEntry.TRAILER.getBytes(StandardCharsets.US_ASCII));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        ais.getNextArEntry();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testCallingGetNextEntryWithoutReadingContentThrowsIOException() throws IOException {
        // In the unpatched version, calling getNextArEntry() without reading content
        // causes offset mismatch leading to trailer check failure: "invalid entry header. not read the content?"
        byte[] content1 = "some content".getBytes(StandardCharsets.US_ASCII);
        byte[] content2 = "other".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(
                new EntryPayload("file1.txt", content1),
                new EntryPayload("file2.txt", content2)
        );

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        ArArchiveEntry entry1 = ais.getNextArEntry();
        assertNotNull(entry1);

        // Intentionally DO NOT read content of entry1
        ais.getNextArEntry();
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Stream Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloseIsIdempotent() throws IOException {
        byte[] archive = createArchive(new EntryPayload("test.txt", new byte[]{1}));
        CloseTrackingInputStream tracker = new CloseTrackingInputStream(new ByteArrayInputStream(archive));

        ArArchiveInputStream ais = new ArArchiveInputStream(tracker);
        assertEquals(0, tracker.closeCount);

        ais.close();
        assertEquals(1, tracker.closeCount);

        // Multiple close() calls should not close the underlying stream again
        ais.close();
        assertEquals(1, tracker.closeCount);
    }

    @Test(timeout = 4000)
    public void testReadWithOffsetAndLength() throws IOException {
        byte[] content = "ABCDEFGH".getBytes(StandardCharsets.US_ASCII);
        byte[] archive = createArchive(new EntryPayload("offset.txt", content));

        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(ais.getNextArEntry());

        byte[] dest = new byte[16];
        int read = ais.read(dest, 4, 4);
        assertEquals(4, read);
        assertEquals('A', dest[4]);
        assertEquals('B', dest[5]);
        assertEquals('C', dest[6]);
        assertEquals('D', dest[7]);
        assertEquals(0, dest[0]); // Untouched prefix

        ais.close();
    }

    @Test(timeout = 4000)
    public void testReadAtEofReturnsNegativeOne() throws IOException {
        byte[] archive = createArchive(new EntryPayload("eof.txt", new byte[]{99}));
        ArArchiveInputStream ais = new ArArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(ais.getNextArEntry());

        assertEquals(99, ais.read());
        // Read until EOF
        byte[] buf = new byte[10];
        int read = ais.read(buf);
        assertEquals(-1, read);
        assertEquals(-1, ais.read());

        ais.close();
    }
}