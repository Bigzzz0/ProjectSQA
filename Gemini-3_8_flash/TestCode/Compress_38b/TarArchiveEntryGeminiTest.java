package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.zip.ZipEncodingHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Branch / Condition Tested                             | Target Method & Partition
 * ----------------------------------------------------------------------------------------------------
 * survivesPaxHeaderWithNameEndingInSlash (COMPRESS-358)  | isDirectory() & isPaxHeader() / isGlobalPaxHeader()
 *   - PAX header ('x', 'X', 'g') whose name ends with '/' | Partition C: Defect-Targeted Branch Zone
 *   - Must NOT be classified as directory even if '/'   |
 * ----------------------------------------------------------------------------------------------------
 * File constructors & normalizations                    | TarArchiveEntry(File), TarArchiveEntry(File, String)
 *   - Non-directory vs Directory (trailing slash add)   | Partition A: Core Functional Logic
 *   - Preserve leading slashes (true vs false)          |
 *   - Windows/NetWare drive letter prefix stripping     |
 * ----------------------------------------------------------------------------------------------------
 * Header byte parsing and writing                       | writeEntryHeader(), parseTarHeader()
 *   - POSIX, GNU (FORMAT_OLDGNU), and XSTAR formats     | Partition A & B
 *   - Checksum calculation and verification (isCheckSumOK)|
 *   - Numeric octal/binary overflow in starMode         |
 *   - Long names and prefixes (>100 and >155 chars)     |
 * ----------------------------------------------------------------------------------------------------
 * Entry type indicators                                 | isFile(), isDirectory(), isSymbolicLink(), isLink(),
 *   - linkFlag combinations: LF_NORMAL, LF_DIR,         | isCharacterDevice(), isBlockDevice(), isFIFO(),
 *     LF_SYMLINK, LF_LINK, LF_CHR, LF_BLK, LF_FIFO,    | isGNUSparse(), isOldGNUSparse(), isPaxGNUSparse(),
 *     LF_GNUTYPE_SPARSE, LF_GNUTYPE_LONGLINK, etc.      | isStarSparse(), isGNULongLinkEntry(), etc.
 * ----------------------------------------------------------------------------------------------------
 * Bounds, Extremes & Exception Guards                   | setSize(-1), setDevMajor(-1), setDevMinor(-1)
 *   - Boundary values for ID fields (deprecated int vs  | Partition D: Exception & Defensive Guard Paths
 *     getLongUserId / getLongGroupId)                   |
 * ----------------------------------------------------------------------------------------------------
 * Object Contract Integrity                             | equals(), hashCode(), isDescendent()
 *   - Identity, Null, Type mismatch, Path descendancy   | Partition E: Object Lifecycle & Contract
 * ----------------------------------------------------------------------------------------------------
 */
public class TarArchiveEntryGeminiTest {

    // ====================================================================================================
    // Partition C: Defect-Targeted Branch Zone (COMPRESS-358 / survivesPaxHeaderWithNameEndingInSlash)
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testPaxHeaderEndingInSlashNotADirectory() {
        // A PAX extended header or global pax header whose name happens to end in '/'
        // must NOT be treated as a directory. Otherwise stream consumers skip reading
        // its contents, corrupting header state alignment.
        TarArchiveEntry paxEntryLC = new TarArchiveEntry("PaxHeaders.X/dir/", TarConstants.LF_PAX_EXTENDED_HEADER_LC);
        assertTrue("Entry with LF_PAX_EXTENDED_HEADER_LC should identify as PaxHeader", paxEntryLC.isPaxHeader());
        assertFalse("PAX header ending in '/' must not be considered a directory", paxEntryLC.isDirectory());

        TarArchiveEntry paxEntryUC = new TarArchiveEntry("PaxHeaders.X/dir/", TarConstants.LF_PAX_EXTENDED_HEADER_UC);
        assertTrue("Entry with LF_PAX_EXTENDED_HEADER_UC should identify as PaxHeader", paxEntryUC.isPaxHeader());
        assertFalse("PAX header UC ending in '/' must not be considered a directory", paxEntryUC.isDirectory());

        TarArchiveEntry globalPaxEntry = new TarArchiveEntry("GlobalHead.X/dir/", TarConstants.LF_PAX_GLOBAL_EXTENDED_HEADER);
        assertTrue("Entry should identify as global PaxHeader", globalPaxEntry.isGlobalPaxHeader());
        assertFalse("Global PAX header ending in '/' must not be considered a directory", globalPaxEntry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testGnuLongLinkAndNameEndingInSlashNotADirectory() {
        TarArchiveEntry longLink = new TarArchiveEntry("@LongLink/", TarConstants.LF_GNUTYPE_LONGLINK);
        assertTrue("Should be GNU long link entry", longLink.isGNULongLinkEntry());
        assertFalse("GNU LongLink entry must not be a directory", longLink.isDirectory());

        TarArchiveEntry longName = new TarArchiveEntry("@LongName/", TarConstants.LF_GNUTYPE_LONGNAME);
        assertTrue("Should be GNU long name entry", longName.isGNULongNameEntry());
        assertFalse("GNU LongName entry must not be a directory", longName.isDirectory());
    }

    // ====================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testConstructFromNameAndLinkFlag() {
        TarArchiveEntry entry = new TarArchiveEntry("archive/file.txt", TarConstants.LF_NORMAL);
        assertEquals("archive/file.txt", entry.getName());
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());

        TarArchiveEntry dirEntry = new TarArchiveEntry("archive/dir/", TarConstants.LF_DIR);
        assertEquals("archive/dir/", dirEntry.getName());
        assertTrue(dirEntry.isDirectory());
        assertFalse(dirEntry.isFile());
        assertEquals(TarArchiveEntry.DEFAULT_DIR_MODE, dirEntry.getMode());

        TarArchiveEntry gnuLongName = new TarArchiveEntry("longname", TarConstants.LF_GNUTYPE_LONGNAME);
        assertTrue(gnuLongName.isGNULongNameEntry());
    }

    @Test(timeout = 4000)
    public void testConstructFromFileSystemFile() throws IOException {
        File tempFile = File.createTempFile("compress-tar-test", ".tmp");
        tempFile.deleteOnExit();

        TarArchiveEntry entry = new TarArchiveEntry(tempFile);
        assertNotNull(entry.getFile());
        assertEquals(tempFile, entry.getFile());
        assertTrue(entry.isFile());
        assertFalse(entry.isDirectory());
        assertEquals(tempFile.length(), entry.getSize());
        assertEquals(TarArchiveEntry.DEFAULT_FILE_MODE, entry.getMode());
        assertEquals(tempFile.lastModified() / 1000L, entry.getModTime().getTime() / 1000L);
        assertEquals(entry.getModTime(), entry.getLastModifiedDate());

        TarArchiveEntry[] childEntries = entry.getDirectoryEntries();
        assertEquals(0, childEntries.length);

        tempFile.delete();
    }

    @Test(timeout = 4000)
    public void testConstructFromFileSystemDirectory() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        TarArchiveEntry entry = new TarArchiveEntry(tempDir);
        assertNotNull(entry.getFile());
        assertTrue(entry.isDirectory());
        assertFalse(entry.isFile());
        assertTrue(entry.getName().endsWith("/"));
        assertEquals(TarArchiveEntry.DEFAULT_DIR_MODE, entry.getMode());

        TarArchiveEntry[] dirEntries = entry.getDirectoryEntries();
        assertNotNull(dirEntries);
    }

    @Test(timeout = 4000)
    public void testFileConstructorCustomName() throws IOException {
        File tempFile = File.createTempFile("compress-tar-custom", ".tmp");
        tempFile.deleteOnExit();

        TarArchiveEntry entry = new TarArchiveEntry(tempFile, "custom/dir/name.bin");
        assertEquals("custom/dir/name.bin", entry.getName());
        assertEquals(tempFile.length(), entry.getSize());

        tempFile.delete();
    }

    @Test(timeout = 4000)
    public void testFileConstructorCustomDirectoryNameWithoutSlash() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        TarArchiveEntry entry = new TarArchiveEntry(tempDir, "customDir");
        assertEquals("customDir/", entry.getName());
        assertTrue(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testSetAndGetAttributes() {
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");

        entry.setName("updated.txt");
        assertEquals("updated.txt", entry.getName());

        entry.setMode(0640);
        assertEquals(0640, entry.getMode());

        entry.setSize(2048L);
        assertEquals(2048L, entry.getSize());

        entry.setLinkName("target.txt");
        assertEquals("target.txt", entry.getLinkName());

        entry.setUserId(1001);
        assertEquals(1001, entry.getUserId());
        assertEquals(1001L, entry.getLongUserId());

        entry.setUserId(4294967295L); // 32-bit boundary unsigned
        assertEquals(4294967295L, entry.getLongUserId());
        assertEquals(-1, entry.getUserId());

        entry.setGroupId(2002);
        assertEquals(2002, entry.getGroupId());
        assertEquals(2002L, entry.getLongGroupId());

        entry.setGroupId(5000000000L);
        assertEquals(5000000000L, entry.getLongGroupId());

        entry.setIds(10, 20);
        assertEquals(10L, entry.getLongUserId());
        assertEquals(20L, entry.getLongGroupId());

        entry.setUserName("alice");
        assertEquals("alice", entry.getUserName());

        entry.setGroupName("developers");
        assertEquals("developers", entry.getGroupName());

        entry.setNames("bob", "testers");
        assertEquals("bob", entry.getUserName());
        assertEquals("testers", entry.getGroupName());

        Date now = new Date(1600000000000L);
        entry.setModTime(now);
        assertEquals(now.getTime() / 1000L, entry.getModTime().getTime() / 1000L);

        entry.setModTime(1700000000000L);
        assertEquals(1700000000L, entry.getModTime().getTime() / 1000L);

        entry.setDevMajor(8);
        assertEquals(8, entry.getDevMajor());

        entry.setDevMinor(1);
        assertEquals(1, entry.getDevMinor());
    }

    @Test(timeout = 4000)
    public void testLinkTypeClassifications() {
        TarArchiveEntry symlink = new TarArchiveEntry("link", TarConstants.LF_SYMLINK);
        assertTrue(symlink.isSymbolicLink());
        assertFalse(symlink.isFile());

        TarArchiveEntry link = new TarArchiveEntry("hardlink", TarConstants.LF_LINK);
        assertTrue(link.isLink());
        assertFalse(link.isFile());

        TarArchiveEntry chr = new TarArchiveEntry("dev/null", TarConstants.LF_CHR);
        assertTrue(chr.isCharacterDevice());

        TarArchiveEntry blk = new TarArchiveEntry("dev/sda", TarConstants.LF_BLK);
        assertTrue(blk.isBlockDevice());

        TarArchiveEntry fifo = new TarArchiveEntry("pipe", TarConstants.LF_FIFO);
        assertTrue(fifo.isFIFO());

        TarArchiveEntry oldNorm = new TarArchiveEntry("file", TarConstants.LF_OLDNORM);
        assertTrue(oldNorm.isFile());

        TarArchiveEntry oldGnuSparse = new TarArchiveEntry("sparse", TarConstants.LF_GNUTYPE_SPARSE);
        assertTrue(oldGnuSparse.isOldGNUSparse());
        assertTrue(oldGnuSparse.isGNUSparse());
        assertTrue(oldGnuSparse.isSparse());
    }

    @Test(timeout = 4000)
    public void testSparseMetadataPopulators() {
        TarArchiveEntry entry = new TarArchiveEntry("sparse.bin");
        assertFalse(entry.isPaxGNUSparse());
        assertFalse(entry.isStarSparse());
        assertFalse(entry.isExtended());
        assertEquals(0L, entry.getRealSize());

        Map<String, String> gnu01Headers = new HashMap<String, String>();
        gnu01Headers.put("GNU.sparse.size", "5000");
        gnu01Headers.put("GNU.sparse.name", "sparse01.bin");
        entry.fillGNUSparse0xData(gnu01Headers);
        assertTrue(entry.isPaxGNUSparse());
        assertTrue(entry.isGNUSparse());
        assertTrue(entry.isSparse());
        assertEquals(5000L, entry.getRealSize());
        assertEquals("sparse01.bin", entry.getName());

        Map<String, String> gnu10Headers = new HashMap<String, String>();
        gnu10Headers.put("GNU.sparse.realsize", "8000");
        gnu10Headers.put("GNU.sparse.name", "sparse10.bin");
        entry.fillGNUSparse1xData(gnu10Headers);
        assertTrue(entry.isPaxGNUSparse());
        assertEquals(8000L, entry.getRealSize());
        assertEquals("sparse10.bin", entry.getName());

        Map<String, String> starHeaders = new HashMap<String, String>();
        starHeaders.put("SCHILY.realsize", "12000");
        entry.fillStarSparseData(starHeaders);
        assertTrue(entry.isStarSparse());
        assertTrue(entry.isSparse());
        assertEquals(12000L, entry.getRealSize());
    }

    // ====================================================================================================
    // Partition B: Boundary Value Analysis (BVA), Serialization & Parsing Paths
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testWriteAndParseEntryHeaderRoundTrip() {
        TarArchiveEntry original = new TarArchiveEntry("test/path/sample.txt");
        original.setMode(0755);
        original.setSize(1024L);
        original.setModTime(1500000000000L);
        original.setUserId(501);
        original.setGroupId(502);
        original.setUserName("alice");
        original.setGroupName("staff");

        byte[] headerBuf = new byte[512];
        original.writeEntryHeader(headerBuf);

        TarArchiveEntry parsed = new TarArchiveEntry(headerBuf);
        assertTrue(parsed.isCheckSumOK());
        assertEquals("test/path/sample.txt", parsed.getName());
        assertEquals(0755, parsed.getMode());
        assertEquals(1024L, parsed.getSize());
        assertEquals(1500000000L, parsed.getModTime().getTime() / 1000L);
        assertEquals(501, parsed.getUserId());
        assertEquals(502, parsed.getGroupId());
        assertEquals("alice", parsed.getUserName());
        assertEquals("staff", parsed.getGroupName());
    }

    @Test(timeout = 4000)
    public void testWriteEntryHeaderWithEncodingAndStarMode() throws IOException {
        TarArchiveEntry entry = new TarArchiveEntry("big-entry.bin");
        // Value larger than standard 11 octal digits (077777777777L = 8589934591L)
        entry.setSize(9999999999L);
        byte[] buf = new byte[512];

        // Star mode true should write binary/octal correctly without zeroing
        entry.writeEntryHeader(buf, ZipEncodingHelper.getZipEncoding("UTF-8"), true);
        TarArchiveEntry parsed = new TarArchiveEntry(buf);
        assertEquals(9999999999L, parsed.getSize());

        // Star mode false will exceed bounds and write 0 in octal field
        byte[] bufNonStar = new byte[512];
        entry.writeEntryHeader(bufNonStar, ZipEncodingHelper.getZipEncoding("UTF-8"), false);
        TarArchiveEntry parsedNonStar = new TarArchiveEntry(bufNonStar);
        assertEquals(0L, parsedNonStar.getSize());
    }

    @Test(timeout = 4000)
    public void testPrefixPathSplittingAndJoining() throws IOException {
        // Construct entry with Posix prefix
        String prefix = "deeply/nested/prefix/structure";
        String name = "filename.txt";
        TarArchiveEntry entry = new TarArchiveEntry(prefix + "/" + name);
        byte[] headerBuf = new byte[512];
        entry.writeEntryHeader(headerBuf);

        TarArchiveEntry parsed = new TarArchiveEntry(headerBuf, ZipEncodingHelper.getZipEncoding("UTF-8"));
        assertEquals(prefix + "/" + name, parsed.getName());
    }

    @Test(timeout = 4000)
    public void testNormalizeFileNameLeadingSlashes() {
        TarArchiveEntry entryStripped = new TarArchiveEntry("///var/log/messages", false);
        assertEquals("var/log/messages", entryStripped.getName());

        TarArchiveEntry entryPreserved = new TarArchiveEntry("///var/log/messages", true);
        assertEquals("///var/log/messages", entryPreserved.getName());

        entryPreserved.setName("/etc/passwd");
        assertEquals("/etc/passwd", entryPreserved.getName());
    }

    @Test(timeout = 4000)
    public void testNormalizeFileNameWindowsDriveLetters() {
        TarArchiveEntry entry = new TarArchiveEntry("C:\\Users\\admin\\file.txt", false);
        // On Windows or replaced separators:
        assertFalse(entry.getName().contains("\\"));
    }

    @Test(timeout = 4000)
    public void testHeaderEvaluationFormats() {
        byte[] headerBuf = new byte[512];
        TarArchiveEntry entry = new TarArchiveEntry("test");
        entry.writeEntryHeader(headerBuf);

        // Check Posix evaluation
        TarArchiveEntry parsedPosix = new TarArchiveEntry(headerBuf);
        assertTrue(parsedPosix.isCheckSumOK());

        // Corrupt checksum
        headerBuf[148] = (byte) '9';
        TarArchiveEntry parsedCorrupt = new TarArchiveEntry(headerBuf);
        assertFalse(parsedCorrupt.isCheckSumOK());
    }

    @Test(timeout = 4000)
    public void testOldGnuFormatHeaderParsing() {
        byte[] header = new byte[512];
        // Populate magic with GNU magic
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, header, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        header[TarConstants.ISEXTENDEDLEN_GNU + 482] = 1;

        TarArchiveEntry parsed = new TarArchiveEntry(header);
        assertNotNull(parsed);
    }

    // ====================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ====================================================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSizeNegative() {
        TarArchiveEntry entry = new TarArchiveEntry("negativeSize");
        entry.setSize(-1L);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDevMajorNegative() {
        TarArchiveEntry entry = new TarArchiveEntry("negativeDevMajor");
        entry.setDevMajor(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDevMinorNegative() {
        TarArchiveEntry entry = new TarArchiveEntry("negativeDevMinor");
        entry.setDevMinor(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testParseTarHeaderInvalidNumericField() {
        byte[] invalidHeader = new byte[512];
        invalidHeader[100] = (byte) 'z'; // invalid mode character
        new TarArchiveEntry(invalidHeader);
    }

    @Test(timeout = 4000)
    public void testGetDirectoryEntriesOnNullOrNonDir() {
        TarArchiveEntry entry = new TarArchiveEntry("plainName");
        assertEquals(0, entry.getDirectoryEntries().length);
        assertNull(entry.getFile());
    }

    // ====================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ====================================================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TarArchiveEntry entry1 = new TarArchiveEntry("common/path.txt");
        TarArchiveEntry entry2 = new TarArchiveEntry("common/path.txt");
        TarArchiveEntry entry3 = new TarArchiveEntry("other/path.txt");

        assertEquals(entry1, entry2);
        assertTrue(entry1.equals(entry2));
        assertTrue(entry1.equals((Object) entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());

        assertNotEquals(entry1, entry3);
        assertFalse(entry1.equals(entry3));
        assertFalse(entry1.equals((Object) entry3));

        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("String Object"));
    }

    @Test(timeout = 4000)
    public void testIsDescendent() {
        TarArchiveEntry parent = new TarArchiveEntry("parent/dir/");
        TarArchiveEntry child = new TarArchiveEntry("parent/dir/child.txt");
        TarArchiveEntry nonChild = new TarArchiveEntry("other/dir/child.txt");

        assertTrue(parent.isDescendent(child));
        assertFalse(parent.isDescendent(nonChild));
    }
}