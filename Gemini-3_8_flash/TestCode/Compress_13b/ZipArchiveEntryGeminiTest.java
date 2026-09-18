package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.compress.archivers.zip.ZipArchiveEntry
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructors:
 *    - ZipArchiveEntry(String): basic initialization, name normalization.
 *    - ZipArchiveEntry(java.util.zip.ZipEntry): entry with extra data != null vs extra == null.
 *    - ZipArchiveEntry(ZipArchiveEntry): deep copy of internal/external attributes and extra fields.
 *    - ZipArchiveEntry(File, String):
 *        * File.isDirectory() == true, name ends with '/' vs not ending with '/' (appends '/').
 *        * File.isFile() == true (sets size) vs non-existent file.
 * 2. Compression Method & Size Boundaries:
 *    - getMethod() / setMethod(int): valid methods (0, 8, etc.), negative method -> IllegalArgumentException.
 *    - getSize() / setSize(long): valid size (0, positive, > 2GB boundary for Zip64), negative -> IllegalArgumentException.
 * 3. Unix Mode & Platform Logic:
 *    - setUnixMode(int):
 *        * File vs Directory (0x10 DOS flag).
 *        * Read-only ((mode & 0200) == 0 -> bit 1 set) vs write permission.
 *    - getUnixMode(): platform == PLATFORM_UNIX vs platform != PLATFORM_UNIX (returns 0).
 * 4. Extra Fields Management:
 *    - setExtraFields: standard fields vs UnparseableExtraFieldData.
 *    - getExtraFields(boolean): extraFields == null (includeUnparseable true/false) vs extraFields != null.
 *    - addExtraField / addAsFirstExtraField: unparseable vs standard; existing vs new header; first position insertion.
 *    - removeExtraField: field present vs missing / extraFields null -> NoSuchElementException.
 *    - removeUnparseableExtraFieldData: unparseable present vs null -> NoSuchElementException.
 *    - mergeExtraFields: local vs central directory data merging with existing vs new fields.
 * 5. Raw Name & General Purpose Bit:
 *    - getRawName(): null vs non-null defensive clone.
 *    - GeneralPurposeBit: getter, setter, and effect on equals().
 * 6. Equality Contract & HashCode:
 *    - Reflexive, symmetric, null comparison, class mismatch.
 *    - Pairwise divergence: name, comment, time, internal/external attributes, platform, method, size,
 *      crc, compressed size, central extra, local extra, general purpose bit.
 * 7. Defect-Targeted Regression (Defects4J WinZip Backslash Workaround):
 *    - Tests processing zip archives containing directory entries with WinZip backslash path separators ('\')
 *      ensuring normalization to forward slash ('/') in archive stream reading and entry lookup.
 */
public class ZipArchiveEntryGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicNameAndDirectoryResolution() {
        ZipArchiveEntry fileEntry = new ZipArchiveEntry("folder/file.txt");
        assertEquals("folder/file.txt", fileEntry.getName());
        assertFalse(fileEntry.isDirectory());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("folder/subfolder/");
        assertEquals("folder/subfolder/", dirEntry.getName());
        assertTrue(dirEntry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testFileConstructorWithDirectoryAndFile() throws IOException {
        File tempDir = File.createTempFile("gemini_dir", "");
        assertTrue(tempDir.delete());
        assertTrue(tempDir.mkdir());

        try {
            // Case 1: Directory without trailing slash -> should append '/'
            ZipArchiveEntry dirEntryNoSlash = new ZipArchiveEntry(tempDir, "myDir");
            assertEquals("myDir/", dirEntryNoSlash.getName());
            assertTrue(dirEntryNoSlash.isDirectory());

            // Case 2: Directory with trailing slash -> should keep single '/'
            ZipArchiveEntry dirEntrySlash = new ZipArchiveEntry(tempDir, "myDir/");
            assertEquals("myDir/", dirEntrySlash.getName());
            assertTrue(dirEntrySlash.isDirectory());

            // Case 3: Regular File
            File tempFile = File.createTempFile("gemini_file", ".tmp");
            try {
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(new byte[]{1, 2, 3, 4, 5});
                fos.close();

                ZipArchiveEntry fileEntry = new ZipArchiveEntry(tempFile, "myFile.tmp");
                assertEquals("myFile.tmp", fileEntry.getName());
                assertFalse(fileEntry.isDirectory());
                assertEquals(5L, fileEntry.getSize());
                assertEquals(tempFile.lastModified(), fileEntry.getTime());
            } finally {
                tempFile.delete();
            }

            // Case 4: Non-existent file (neither directory nor file)
            File nonExistent = new File(tempDir, "doesNotExist.bin");
            ZipArchiveEntry nonExistentEntry = new ZipArchiveEntry(nonExistent, "nonExistent.bin");
            assertEquals("nonExistent.bin", nonExistentEntry.getName());
            assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, nonExistentEntry.getSize());
        } finally {
            tempDir.delete();
        }
    }

    @Test(timeout = 4000)
    public void testMethodAndInternalExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals(-1, entry.getMethod());

        entry.setMethod(ZipArchiveEntry.DEFLATED);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());

        entry.setMethod(ZipArchiveEntry.STORED);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());

        entry.setInternalAttributes(0x0001);
        assertEquals(0x0001, entry.getInternalAttributes());

        entry.setExternalAttributes(0x12345678L);
        assertEquals(0x12345678L, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testUnixModePermissionsAndDosFlags() {
        ZipArchiveEntry fileEntry = new ZipArchiveEntry("readWrite.txt");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, fileEntry.getPlatform());
        assertEquals(0, fileEntry.getUnixMode());

        // Standard 0644 writable file: (0644 & 0200) != 0 -> DOS read-only bit is NOT set
        fileEntry.setUnixMode(0644);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, fileEntry.getPlatform());
        assertEquals(0644, fileEntry.getUnixMode());
        assertEquals(0, fileEntry.getExternalAttributes() & 0x01); // Not read-only
        assertEquals(0, fileEntry.getExternalAttributes() & 0x10); // Not directory

        // Read-only file 0444: (0444 & 0200) == 0 -> DOS read-only bit IS set (0x01)
        ZipArchiveEntry roEntry = new ZipArchiveEntry("readOnly.txt");
        roEntry.setUnixMode(0444);
        assertEquals(0444, roEntry.getUnixMode());
        assertEquals(1, roEntry.getExternalAttributes() & 0x01);

        // Directory 0755: isDirectory() == true -> DOS directory flag IS set (0x10)
        ZipArchiveEntry dirEntry = new ZipArchiveEntry("directory/");
        dirEntry.setUnixMode(0755);
        assertEquals(0755, dirEntry.getUnixMode());
        assertEquals(0x10, dirEntry.getExternalAttributes() & 0x10);

        // FAT platform override returns 0 unix mode
        dirEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, dirEntry.getPlatform());
        assertEquals(0, dirEntry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testGeneralPurposeBitAndDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.bin");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        gpb.useEncryption(true);
        entry.setGeneralPurposeBit(gpb);

        assertSame(gpb, entry.getGeneralPurposeBit());
        assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
        assertTrue(entry.getGeneralPurposeBit().usesEncryption());

        long time = 12345678000L;
        entry.setTime(time);
        assertEquals(time, entry.getTime());
        assertEquals(new Date(time), entry.getLastModifiedDate());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSizeBoundaries() {
        ZipArchiveEntry entry = new ZipArchiveEntry("size.bin");
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());

        entry.setSize(0L);
        assertEquals(0L, entry.getSize());

        entry.setSize(100L);
        assertEquals(100L, entry.getSize());

        // Test 64-bit size beyond 32-bit (Zip64 capability)
        long largeSize = 0x100000000L + 50L; // > 4GB
        entry.setSize(largeSize);
        assertEquals(largeSize, entry.getSize());

        entry.setSize(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testRawNameDefensiveCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("name");
        assertNull(entry.getRawName());

        byte[] raw = new byte[]{65, 66, 67};
        entry.setName("ABC", raw);
        byte[] retrieved = entry.getRawName();
        assertNotNull(retrieved);
        assertArrayEquals(raw, retrieved);
        assertNotSame(raw, retrieved);

        // Mutating retrieved array must not mutate internal state
        retrieved[0] = 99;
        assertArrayEquals(new byte[]{65, 66, 67}, entry.getRawName());
    }

    @Test(timeout = 4000)
    public void testExtraFieldsNullAndEmptyRetrieval() {
        ZipArchiveEntry entry = new ZipArchiveEntry("emptyExtra");
        assertNull(entry.getExtraField(new ZipShort(1)));
        assertNull(entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields().length);
        assertEquals(0, entry.getExtraFields(false).length);
        assertEquals(0, entry.getExtraFields(true).length);
        assertEquals(0, entry.getLocalFileDataExtra().length);
        assertEquals(0, entry.getCentralDirectoryExtra().length);

        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[]{1, 2}, 0, 2);
        entry.addExtraField(unparseable);

        assertEquals(0, entry.getExtraFields(false).length);
        assertEquals(1, entry.getExtraFields(true).length);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (WinZip Backslash Workaround)
    // =========================================================================

    /**
     * Targets known failure condition from Defects4J:
     * WinZip backslash path separator workaround. When zip entries are created with
     * backslashes (e.g. "ä\" or "ä\ü.txt"), reading via ZipArchiveInputStream or looking
     * up via ZipFile must normalize separators to standard forward slashes '/'.
     */
    @Test(timeout = 4000)
    public void testWinzipBackSlashWorkaroundInZipArchiveInputStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

        // Entry 1: Directory ending with backslash
        java.util.zip.ZipEntry dirEntry = new java.util.zip.ZipEntry("ä\\");
        zos.putNextEntry(dirEntry);
        zos.closeEntry();

        // Entry 2: File with backslash separator
        java.util.zip.ZipEntry fileEntry = new java.util.zip.ZipEntry("ä\\ü.txt");
        zos.putNextEntry(fileEntry);
        zos.write(new byte[]{42});
        zos.closeEntry();
        zos.close();

        ZipArchiveInputStream in = new ZipArchiveInputStream(new ByteArrayInputStream(baos.toByteArray()));
        try {
            ZipArchiveEntry zae1 = in.getNextZipEntry();
            assertNotNull(zae1);
            assertEquals("ä/", zae1.getName());
            assertTrue("WinZip directory with backslash must be recognized as directory", zae1.isDirectory());

            ZipArchiveEntry zae2 = in.getNextZipEntry();
            assertNotNull(zae2);
            assertEquals("ä/ü.txt", zae2.getName());
            assertFalse(zae2.isDirectory());
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testWinzipBackSlashWorkaroundInZipFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);

        java.util.zip.ZipEntry fileEntry = new java.util.zip.ZipEntry("ä\\ü.txt");
        zos.putNextEntry(fileEntry);
        zos.write(new byte[]{42});
        zos.closeEntry();
        zos.close();

        File tempFile = File.createTempFile("winzip_test", ".zip");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(baos.toByteArray());
            fos.close();

            ZipFile zf = new ZipFile(tempFile);
            try {
                // Must be normalized to forward slash: backslash lookup returns null, slash lookup returns entry
                assertNull(zf.getEntry("ä\\ü.txt"));
                assertNotNull(zf.getEntry("ä/ü.txt"));
            } finally {
                zf.close();
            }
        } finally {
            tempFile.delete();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeMethodThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(-2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeSizeThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setSize(-1L);
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveExtraFieldWhenEmptyThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(0x1234));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveNonExistingExtraFieldThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x0001));
        entry.addExtraField(uef);

        entry.removeExtraField(new ZipShort(0x9999));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveUnparseableExtraFieldWhenNullThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    // =========================================================================
    // Partition E: Extra Field Manipulation & Merging
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndRemoveExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("extraTest");
        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(1));
        field1.setLocalFileDataData(new byte[]{1, 2});
        field1.setCentralDirectoryData(new byte[]{1, 2});

        UnrecognizedExtraField field2 = new UnrecognizedExtraField();
        field2.setHeaderId(new ZipShort(2));
        field2.setLocalFileDataData(new byte[]{3, 4});
        field2.setCentralDirectoryData(new byte[]{3, 4});

        entry.addExtraField(field1);
        entry.addExtraField(field2);

        assertEquals(2, entry.getExtraFields().length);
        assertSame(field1, entry.getExtraField(new ZipShort(1)));
        assertSame(field2, entry.getExtraField(new ZipShort(2)));

        entry.removeExtraField(new ZipShort(1));
        assertEquals(1, entry.getExtraFields().length);
        assertNull(entry.getExtraField(new ZipShort(1)));
        assertSame(field2, entry.getExtraField(new ZipShort(2)));
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraFieldAndReordering() {
        ZipArchiveEntry entry = new ZipArchiveEntry("firstExtra");
        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(1));

        UnrecognizedExtraField field2 = new UnrecognizedExtraField();
        field2.setHeaderId(new ZipShort(2));

        entry.addExtraField(field1);
        entry.addAsFirstExtraField(field2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(new ZipShort(2), fields[0].getHeaderId());
        assertEquals(new ZipShort(1), fields[1].getHeaderId());

        // Test adding as first when field already existed
        entry.addAsFirstExtraField(field1);
        fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(new ZipShort(1), fields[0].getHeaderId());
        assertEquals(new ZipShort(2), fields[1].getHeaderId());

        // Test adding unparseable as first
        UnparseableExtraFieldData uefd = new UnparseableExtraFieldData();
        entry.addAsFirstExtraField(uefd);
        assertSame(uefd, entry.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testSetExtraFieldsReplacesAll() {
        ZipArchiveEntry entry = new ZipArchiveEntry("replaceExtra");
        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(1));
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();

        entry.setExtraFields(new ZipExtraField[]{field1, unparseable});

        assertEquals(1, entry.getExtraFields(false).length);
        assertEquals(2, entry.getExtraFields(true).length);
        assertSame(field1, entry.getExtraField(new ZipShort(1)));
        assertSame(unparseable, entry.getUnparseableExtraFieldData());

        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
        assertEquals(1, entry.getExtraFields(true).length);
    }

    @Test(timeout = 4000)
    public void testSetExtraAndCentralDirectoryMerging() {
        ZipArchiveEntry entry = new ZipArchiveEntry("mergeTest");
        // Extra field with Header ID 0x0001 and 2 bytes of data [10, 20]
        byte[] extraLocal = new byte[]{1, 0, 2, 0, 10, 20};
        entry.setExtra(extraLocal);

        ZipExtraField field = entry.getExtraField(new ZipShort(1));
        assertNotNull(field);
        assertArrayEquals(new byte[]{10, 20}, field.getLocalFileDataData());

        // Central directory extra with Header ID 0x0001 and 2 bytes [30, 40]
        byte[] extraCentral = new byte[]{1, 0, 2, 0, 30, 40};
        entry.setCentralDirectoryExtra(extraCentral);

        field = entry.getExtraField(new ZipShort(1));
        assertNotNull(field);
        assertArrayEquals(new byte[]{30, 40}, field.getCentralDirectoryData());
    }

    // =========================================================================
    // Partition F: Object Lifecycle & Contract Integrity (Equals, HashCode, Clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneIndependence() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("original.txt");
        original.setSize(500);
        original.setMethod(ZipArchiveEntry.DEFLATED);
        original.setInternalAttributes(2);
        original.setExternalAttributes(0644L << 16);
        original.setUnixMode(0644);

        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(5));
        field.setLocalFileDataData(new byte[]{1, 2});
        original.addExtraField(field);

        ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertEquals(original, cloned);
        assertEquals(original.hashCode(), cloned.hashCode());

        // Modifying cloned extra fields should not alter original
        cloned.removeExtraField(new ZipShort(5));
        assertNotNull(original.getExtraField(new ZipShort(5)));
        assertNull(cloned.getExtraField(new ZipShort(5)));
    }

    @Test(timeout = 4000)
    public void testCopyConstructors() throws Exception {
        java.util.zip.ZipEntry standardEntry = new java.util.zip.ZipEntry("standard.txt");
        standardEntry.setComment("Standard comment");
        standardEntry.setSize(1234L);
        standardEntry.setMethod(java.util.zip.ZipEntry.DEFLATED);
        standardEntry.setTime(1000000L);
        standardEntry.setExtra(new byte[]{1, 0, 2, 0, 9, 8});

        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(standardEntry);
        assertEquals("standard.txt", zipArchiveEntry.getName());
        assertEquals("Standard comment", zipArchiveEntry.getComment());
        assertEquals(1234L, zipArchiveEntry.getSize());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, zipArchiveEntry.getMethod());
        assertEquals(1000000L, zipArchiveEntry.getTime());
        assertNotNull(zipArchiveEntry.getExtraField(new ZipShort(1)));

        // Copy constructor from another ZipArchiveEntry
        zipArchiveEntry.setInternalAttributes(7);
        zipArchiveEntry.setExternalAttributes(42L);
        ZipArchiveEntry copyOfZipArchiveEntry = new ZipArchiveEntry(zipArchiveEntry);
        assertEquals(zipArchiveEntry, copyOfZipArchiveEntry);
        assertEquals(7, copyOfZipArchiveEntry.getInternalAttributes());
        assertEquals(42L, copyOfZipArchiveEntry.getExternalAttributes());

        // Copy constructor with null extra in standard entry
        java.util.zip.ZipEntry nullExtraEntry = new java.util.zip.ZipEntry("nullExtra");
        nullExtraEntry.setExtra(null);
        ZipArchiveEntry fromNullExtra = new ZipArchiveEntry(nullExtraEntry);
        assertEquals(0, fromNullExtra.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeExhaustiveBranches() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("entry.txt");
        ZipArchiveEntry e2 = new ZipArchiveEntry("entry.txt");

        // Reflexive & Identity
        assertTrue(e1.equals(e1));
        assertEquals(e1.hashCode(), e1.hashCode());

        // Null and different type
        assertFalse(e1.equals(null));
        assertFalse(e1.equals("StringObject"));

        // Symmetric
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
        assertEquals(e1.hashCode(), e2.hashCode());

        // Branch: Different names
        ZipArchiveEntry diffName = new ZipArchiveEntry("other.txt");
        assertFalse(e1.equals(diffName));

        // Branch: Null name handling
        e1.setName(null);
        assertFalse(e1.equals(e2));
        e2.setName(null);
        assertTrue(e1.equals(e2));
        e1.setName("entry.txt");
        assertFalse(e1.equals(e2));
        e2.setName("entry.txt");

        // Branch: Comment
        e1.setComment("c1");
        assertFalse(e1.equals(e2));
        e2.setComment("c2");
        assertFalse(e1.equals(e2));
        e2.setComment("c1");
        assertTrue(e1.equals(e2));

        // Branch: Time
        e1.setTime(100L);
        e2.setTime(200L);
        assertFalse(e1.equals(e2));
        e2.setTime(100L);

        // Branch: Internal Attributes
        e1.setInternalAttributes(1);
        assertFalse(e1.equals(e2));
        e2.setInternalAttributes(1);

        // Branch: Platform
        e1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertFalse(e1.equals(e2));
        e2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);

        // Branch: External Attributes
        e1.setExternalAttributes(99L);
        assertFalse(e1.equals(e2));
        e2.setExternalAttributes(99L);

        // Branch: Method
        e1.setMethod(ZipArchiveEntry.STORED);
        assertFalse(e1.equals(e2));
        e2.setMethod(ZipArchiveEntry.STORED);

        // Branch: Size
        e1.setSize(1000L);
        assertFalse(e1.equals(e2));
        e2.setSize(1000L);

        // Branch: CRC
        e1.setCrc(555L);
        assertFalse(e1.equals(e2));
        e2.setCrc(555L);

        // Branch: Compressed Size
        e1.setCompressedSize(500L);
        assertFalse(e1.equals(e2));
        e2.setCompressedSize(500L);

        // Branch: Extra Data
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        uef1.setHeaderId(new ZipShort(9));
        uef1.setLocalFileDataData(new byte[]{1});
        uef1.setCentralDirectoryData(new byte[]{1});
        e1.addExtraField(uef1);
        assertFalse(e1.equals(e2));

        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(9));
        uef2.setLocalFileDataData(new byte[]{1});
        uef2.setCentralDirectoryData(new byte[]{1});
        e2.addExtraField(uef2);
        assertTrue(e1.equals(e2));

        // Branch: General Purpose Bit
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        e1.setGeneralPurposeBit(gpb);
        assertFalse(e1.equals(e2));
        e2.setGeneralPurposeBit(gpb);
        assertTrue(e1.equals(e2));
    }
}