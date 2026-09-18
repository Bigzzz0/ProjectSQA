package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Partitions targeted:
 *  A. Core navigation: getNextTarEntry/read/skip/available/close/record size
 *  B. Boundaries: empty archive, huge entry size, EOF, offset/readBuf transitions
 *  C. Defect zone (COMPRESS-178): embedded NUL in otherwise octal header field
 *     must not raise "Invalid byte 0 at offset 2 in '00{NUL}0765{NUL}' len=8"
 *  D. Defensive paths: unexpected EOF, invalid canReadEntryData, matches() rejection
 *  E. GNU long names, PAX headers, sparse entry detection, protected state mutators
 */
public class TarArchiveInputStreamDeepseekTest {

    private static final int HEADER_LEN = 512;
    private static final int NAME_OFFSET = 0;
    private static final int MODE_OFFSET = 100;
    private static final int UID_OFFSET = 108;
    private static final int GID_OFFSET = 116;
    private static final int SIZE_OFFSET = 124;
    private static final int MTIME_OFFSET = 136;
    private static final int CHKSUM_OFFSET = 148;
    private static final int CHKSUM_LEN = 8;
    private static final int TYPEFLAG_OFFSET = 156;
    private static final int LINKNAME_OFFSET = 157;
    private static final int MAGIC_OFFSET = 257;
    private static final int MAGIC_LEN = 6;
    private static final int VERSION_OFFSET = 263;
    private static final int VERSION_LEN = 2;
    private static final int UNAME_OFFSET = 265;
    private static final int GNAME_OFFSET = 297;
    private static final int DEVMAJOR_OFFSET = 329;
    private static final int DEVMINOR_OFFSET = 337;
    private static final int PREFIX_OFFSET = 345;

    @Test(timeout = 4000)
    public void testEmptyArchiveReturnsNull() throws Exception {
        byte[] archive = new byte[1024];
        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        assertFalse(in.isAtEOF());
        assertNull(in.getNextTarEntry());
        assertTrue(in.isAtEOF());
        assertNull(in.getNextTarEntry());
        assertEquals(0, in.available());

        in.close();
    }

    @Test(timeout = 4000)
    public void testReadsSingleEntryFully() throws Exception {
        byte[] data = "Hello, Tar!".getBytes(StandardCharsets.UTF_8);
        byte[] archive = createSingleEntryArchive("hello.txt", data, (byte) '0');

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry entry = in.getNextTarEntry();
        assertNotNull(entry);
        assertEquals("hello.txt", entry.getName());
        assertEquals(data.length, entry.getSize());
        assertEquals(data.length, in.available());

        byte[] buffer = new byte[128];
        assertEquals(data.length, in.read(buffer, 0, buffer.length));
        assertEquals(new String(data, StandardCharsets.UTF_8),
                     new String(buffer, 0, data.length, StandardCharsets.UTF_8));
        assertEquals(-1, in.read(buffer, 0, buffer.length));
        assertEquals(0, in.available());

        assertNull(in.getNextTarEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testAvailableReturnsMaxForHugeEntry() throws Exception {
        long hugeSize = Integer.MAX_VALUE + 1L;
        byte[] header = createTarHeader("huge", hugeSize, 0644, (byte) '0');
        byte[] archive = concat(header, new byte[1024]);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = in.getNextTarEntry();

        assertNotNull(entry);
        assertEquals(Integer.MAX_VALUE, in.available());
        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipWithinAndAcrossEntry() throws Exception {
        byte[] data = "0123456789ABCDEFGHIJ".getBytes(StandardCharsets.UTF_8);
        byte[] archive = createSingleEntryArchive("skip.txt", data, (byte) '0');

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextTarEntry());

        assertEquals(5, in.skip(5));
        assertEquals(15, in.available());
        assertEquals((int) '5', in.read());
        assertEquals(14, in.available());
        assertEquals(14, in.skip(100));
        assertEquals(0, in.available());
        assertEquals(-1, in.read());

        in.close();
    }

    @Test(timeout = 4000)
    public void testSkipBeyondEntryReturnsOnlyEntryBytes() throws Exception {
        byte[] data = "short".getBytes(StandardCharsets.UTF_8);
        byte[] archive = createSingleEntryArchive("short.txt", data, (byte) '0');

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextTarEntry());

        assertEquals(data.length, in.skip(1000));
        assertEquals(-1, in.read());

        in.close();
    }

    @Test(timeout = 4000)
    public void testReadWithLeftoverRecordBuffer() throws Exception {
        byte[] data = new byte[600];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        byte[] archive = createSingleEntryArchive("large.bin", data, (byte) '0');

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextTarEntry());

        byte[] first = new byte[100];
        assertEquals(100, in.read(first, 0, first.length));

        byte[] rest = new byte[500];
        assertEquals(500, in.read(rest, 0, rest.length));

        byte[] all = new byte[600];
        System.arraycopy(first, 0, all, 0, first.length);
        System.arraycopy(rest, 0, all, first.length, rest.length);

        assertArrayEquals(data, all);
        assertEquals(-1, in.read(new byte[1], 0, 1));

        in.close();
    }

    @Test(timeout = 4000)
    public void testUnexpectedEofThrowsIoException() throws Exception {
        byte[] header = createTarHeader("truncated", 100, 0644, (byte) '0');
        byte[] archive = new byte[HEADER_LEN];
        System.arraycopy(header, 0, archive, 0, HEADER_LEN);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNotNull(in.getNextTarEntry());

        try {
            in.read(new byte[256], 0, 256);
            fail("Expected IOException for truncated entry data");
        } catch (IOException expected) {
            assertTrue(expected.getMessage().contains("unexpected EOF"));
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testGetRecordSize() throws Exception {
        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));
        assertEquals(512, in.getRecordSize());
        in.close();
    }

    @Test(timeout = 4000)
    public void testResetDoesNothing() throws Exception {
        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));
        in.reset();
        in.close();
    }

    @Test(timeout = 4000)
    public void testMatchesSignatureBranches() throws Exception {
        byte[] sig = new byte[265];

        assertFalse(TarArchiveInputStream.matches(sig, 264));
        assertFalse(TarArchiveInputStream.matches(sig, 265));

        assertTrue(TarArchiveInputStream.matches(tarSignature("ustar", "00"), 265));
        assertTrue(TarArchiveInputStream.matches(tarSignature("ustar ", " \0"), 265));
        assertTrue(TarArchiveInputStream.matches(tarSignature("ustar ", "0\0"), 265));
        assertTrue(TarArchiveInputStream.matches(tarSignature(TarConstants.MAGIC_ANT, TarConstants.VERSION_ANT), 265));

        assertFalse(TarArchiveInputStream.matches(new byte[100], 100));
    }

    @Test(timeout = 4000)
    public void testCanReadEntryData() throws Exception {
        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));

        assertFalse(in.canReadEntryData(null));

        TarArchiveEntry normal = new TarArchiveEntry("normal.txt");
        assertTrue(in.canReadEntryData(normal));

        byte[] sparseHeader = createTarHeader("sparse", 0, 0644, (byte) 'S');
        TarArchiveEntry sparse = new TarArchiveEntry(sparseHeader);
        assertFalse(in.canReadEntryData(sparse));

        in.close();
    }

    @Test(timeout = 4000)
    public void testProtectedStateAccessors() throws Exception {
        byte[] data = new byte[] { 1 };
        byte[] archive = createSingleEntryArchive("state.bin", data, (byte) '0');

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        assertFalse(in.isAtEOF());
        assertNull(in.getCurrentEntry());

        TarArchiveEntry entry = in.getNextTarEntry();
        assertNotNull(entry);
        assertSame(entry, in.getCurrentEntry());

        in.setCurrentEntry(null);
        assertNull(in.getCurrentEntry());

        in.setAtEOF(true);
        assertTrue(in.isAtEOF());
        assertNull(in.getNextTarEntry());

        in.close();
    }

    @Test(timeout = 4000)
    public void testGnuLongNameEntry() throws Exception {
        String longName = "this/is/a/very/long/file/name.txt";
        byte[] longNameBytes = longName.getBytes(StandardCharsets.UTF_8);

        byte[] longNameData = new byte[longNameBytes.length + 1];
        System.arraycopy(longNameBytes, 0, longNameData, 0, longNameBytes.length);

        byte[] longHeader = createTarHeader("././@LongLink", longNameData.length, 0, (byte) 'L');
        byte[] fileData = "content".getBytes(StandardCharsets.UTF_8);
        byte[] fileHeader = createTarHeader("", fileData.length, 0644, (byte) '0');

        byte[] archive = concat(longHeader, longNameData, pad(longNameData),
                                fileHeader, fileData, pad(fileData), new byte[1024]);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = in.getNextTarEntry();

        assertNotNull(entry);
        assertEquals(longName, entry.getName());
        assertEquals(fileData.length, in.available());

        byte[] buf = new byte[128];
        assertEquals(fileData.length, in.read(buf, 0, buf.length));
        assertEquals("content", new String(buf, 0, fileData.length, StandardCharsets.UTF_8));

        assertNull(in.getNextTarEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testGnuLongNameMissingActualEntry() throws Exception {
        byte[] longName = "lonely".getBytes(StandardCharsets.UTF_8);
        byte[] longHeader = createTarHeader("././@LongLink", longName.length, 0, (byte) 'L');

        byte[] archive = concat(longHeader, longName, pad(longName), new byte[1024]);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        assertNull(in.getNextTarEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testPaxHeaderPathAndSize() throws Exception {
        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("path", "renamed.txt");
        headers.put("size", "5");

        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] archive = createPaxArchive(headers, "original.txt", data);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));
        TarArchiveEntry entry = in.getNextTarEntry();

        assertNotNull(entry);
        assertEquals("renamed.txt", entry.getName());
        assertEquals(5, entry.getSize());
        assertEquals(5, in.available());

        byte[] buf = new byte[8];
        assertEquals(5, in.read(buf, 0, buf.length));
        assertArrayEquals(data, Arrays.copyOf(buf, 5));

        assertNull(in.getNextTarEntry());
        in.close();
    }

    @Test(timeout = 4000)
    public void testParsePaxHeadersDirect() throws Exception {
        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]));

        Map<String, String> headers = in.parsePaxHeaders(new StringReader("20 path=renamed.txt\n"));
        assertEquals(1, headers.size());
        assertEquals("renamed.txt", headers.get("path"));

        assertTrue(in.parsePaxHeaders(new StringReader("")).isEmpty());

        in.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithBlockAndRecordSize() throws Exception {
        TarArchiveInputStream in =
                new TarArchiveInputStream(new ByteArrayInputStream(new byte[1024]), 1024, 512);
        assertEquals(512, in.getRecordSize());
        in.close();
    }

    /**
     * Defect regression for COMPRESS-178.
     *
     * A tar header containing an embedded NUL in an 8-byte octal field such as
     * {@code "00{NUL}0765{NUL}"} must not cause
     * {@code IllegalArgumentException("Invalid byte 0 at offset 2 ...")} while
     * reading the next entry.
     */
    @Test(timeout = 4000)
    public void testCOMPRESS178EmbeddedNulInModeDoesNotThrow() throws Exception {
        byte[] rawMode = new byte[] { '0', '0', 0, '0', '7', '6', '5', 0 };
        byte[] header = createHeaderWithRawMode("test.txt", 0, (byte) '0', rawMode);
        byte[] archive = concat(header, new byte[1024]);

        TarArchiveInputStream in = new TarArchiveInputStream(new ByteArrayInputStream(archive));

        TarArchiveEntry entry = in.getNextTarEntry();
        assertNotNull("COMPRESS-178: entry should be readable", entry);
        assertEquals("test.txt", entry.getName());

        in.close();
    }

    private static byte[] createSingleEntryArchive(String name, byte[] data, byte typeflag) {
        byte[] header = createTarHeader(name, data.length, 0644, typeflag);
        return concat(header, data, pad(data), new byte[1024]);
    }

    private static byte[] createPaxArchive(Map<String, String> headers, String originalName, byte[] data) {
        ByteArrayOutputStream paxData = new ByteArrayOutputStream();
        for (Map.Entry<String, String> header : headers.entrySet()) {
            byte[] rec = paxRecord(header.getKey(), header.getValue());
            paxData.write(rec, 0, rec.length);
        }

        byte[] paxBytes = paxData.toByteArray();
        byte[] paxHeader = createTarHeader("pax", paxBytes.length, 0644, (byte) 'x');
        byte[] fileHeader = createTarHeader(originalName, 0, 0644, (byte) '0');

        return concat(paxHeader, paxBytes, pad(paxBytes),
                      fileHeader, data, pad(data), new byte[1024]);
    }

    private static byte[] paxRecord(String key, String value) {
        String body = key + "=" + value + "\n";
        int total = body.length() + 1;
        while (true) {
            String len = Integer.toString(total);
            String record = len + " " + body;
            int actual = record.getBytes(StandardCharsets.UTF_8).length;
            if (actual == total) {
                return record.getBytes(StandardCharsets.UTF_8);
            }
            total = actual;
        }
    }

    private static byte[] createTarHeader(String name, long size, int mode, byte typeflag) {
        byte[] h = new byte[HEADER_LEN];

        writeAscii(h, NAME_OFFSET, 100, name);
        writeOctal(h, MODE_OFFSET, 8, mode);
        writeOctal(h, UID_OFFSET, 8, 0);
        writeOctal(h, GID_OFFSET, 8, 0);
        writeOctal(h, SIZE_OFFSET, 12, size);
        writeOctal(h, MTIME_OFFSET, 12, 0);

        Arrays.fill(h, CHKSUM_OFFSET, CHKSUM_OFFSET + CHKSUM_LEN, (byte) ' ');

        h[TYPEFLAG_OFFSET] = typeflag;
        writeAscii(h, LINKNAME_OFFSET, 100, "");
        writeAscii(h, MAGIC_OFFSET, MAGIC_LEN, "ustar");
        writeAscii(h, VERSION_OFFSET, VERSION_LEN, "00");
        writeAscii(h, UNAME_OFFSET, 32, "");
        writeAscii(h, GNAME_OFFSET, 32, "");
        writeOctal(h, DEVMAJOR_OFFSET, 8, 0);
        writeOctal(h, DEVMINOR_OFFSET, 8, 0);
        writeAscii(h, PREFIX_OFFSET, 155, "");

        long sum = 0;
        for (byte b : h) {
            sum += b & 0xff;
        }
        writeOctal(h, CHKSUM_OFFSET, CHKSUM_LEN, sum);

        return h;
    }

    private static byte[] createHeaderWithRawMode(String name, long size, byte typeflag, byte[] rawMode) {
        byte[] h = createTarHeader(name, size, 0, typeflag);
        System.arraycopy(rawMode, 0, h, MODE_OFFSET, rawMode.length);

        Arrays.fill(h, CHKSUM_OFFSET, CHKSUM_OFFSET + CHKSUM_LEN, (byte) ' ');
        long sum = 0;
        for (byte b : h) {
            sum += b & 0xff;
        }
        writeOctal(h, CHKSUM_OFFSET, CHKSUM_LEN, sum);

        return h;
    }

    private static void writeAscii(byte[] buf, int offset, int length, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        Arrays.fill(buf, offset, offset + length, (byte) 0);
        System.arraycopy(bytes, 0, buf, offset, Math.min(bytes.length, length));
    }

    private static void writeOctal(byte[] buf, int offset, int length, long value) {
        String octal = Long.toOctalString(value);
        if (octal.length() > length - 1) {
            throw new IllegalArgumentException("Octal value too long for tar field");
        }

        Arrays.fill(buf, offset, offset + length, (byte) '0');
        byte[] digits = octal.getBytes(StandardCharsets.UTF_8);
        int start = offset + length - 1 - digits.length;
        System.arraycopy(digits, 0, buf, start, digits.length);
        buf[offset + length - 1] = 0;
    }

    private static byte[] pad(byte[] data) {
        int rem = data.length % HEADER_LEN;
        if (rem == 0) {
            return new byte[0];
        }
        return new byte[HEADER_LEN - rem];
    }

    private static byte[] concat(byte[]... arrays) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            bos.write(array, 0, array.length);
        }
        return bos.toByteArray();
    }

    private static byte[] tarSignature(String magic, String version) {
        byte[] sig = new byte[265];
        byte[] m = magic.getBytes(StandardCharsets.UTF_8);
        byte[] v = version.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(m, 0, sig, MAGIC_OFFSET, m.length);
        System.arraycopy(v, 0, sig, VERSION_OFFSET, v.length);
        return sig;
    }
}