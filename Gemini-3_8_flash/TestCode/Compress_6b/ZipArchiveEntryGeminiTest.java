package org.apache.commons.compress.archivers.zip;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * 1. DEFECT UNDER TEST (COMPRESS-93 / testNotEquals):
 *    - ZipArchiveEntry.equals(Object): The constructor `ZipArchiveEntry(String)` passes name to super,
 *      leaving `this.name == null`. As a result, two entries initialized with different names both have
 *      `this.name == null`, leading `equals()` to mistakenly return `true`.
 *      Targeted by: `testNotEqualsFailureCondition()`
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - Constructors:
 *      * ZipArchiveEntry(String): null / empty / normal paths.
 *      * ZipArchiveEntry(java.util.zip.ZipEntry): with extra != null, with extra == null, method copying.
 *      * ZipArchiveEntry(ZipArchiveEntry): deep copy of internal/external attributes and extra fields.
 *      * ZipArchiveEntry(): protected constructor with empty name default.
 *      * ZipArchiveEntry(File, String):
 *        - File is directory vs regular file.
 *        - String entryName endsWith("/") vs not ending with "/".
 *        - File lastModified and length checks.
 *    - Methods & Attributes:
 *      * isSupportedCompressionMethod(): STORED (0), DEFLATED (8), unsupported (e.g. 1, 99, -1).
 *      * getMethod() / setMethod(int): valid positive/zero vs negative (IllegalArgumentException).
 *      * getInternalAttributes() / setInternalAttributes(int).
 *      * getExternalAttributes() / setExternalAttributes(long).
 *      * setUnixMode(int) & getUnixMode():
 *        - mode directory flag (isDirectory() true vs false).
 *        - mode read-only flag ((mode & 0200) == 0 vs != 0).
 *        - platform == PLATFORM_UNIX vs platform != PLATFORM_UNIX.
 *      * getPlatform() / setPlatform(int).
 *    - Extra Fields Management:
 *      * setExtraFields(ZipExtraField[]): null, empty, multiple entries.
 *      * getExtraFields(): extraFields is null vs populated.
 *      * addExtraField(ZipExtraField): extraFields initially null vs already contains same or different type.
 *      * addAsFirstExtraField(ZipExtraField): extraFields null vs existing (reordering & replacing).
 *      * removeExtraField(ZipShort): extraFields is null, field not found, field removed successfully.
 *      * getExtraField(ZipShort): extraFields is null, field present vs absent.
 *      * setExtra(byte[]): valid extra bytes parsed, invalid bytes throwing RuntimeException.
 *      * setCentralDirectoryExtra(byte[]): valid bytes parsed, invalid bytes throwing RuntimeException.
 *      * getLocalFileDataExtra() & getCentralDirectoryExtra().
 *      * mergeExtraFields(ZipExtraField[], boolean local):
 *        - extraFields == null path.
 *        - extraFields != null: existing is null (calls addExtraField).
 *        - extraFields != null: existing != null (local == true vs local == false parsing).
 *    - Object Lifecycle & Contract:
 *      * getName() / setName(String): this.name == null vs != null.
 *      * isDirectory(): endsWith("/") vs does not.
 *      * hashCode(): deterministic based on getName().
 *      * getLastModifiedDate(): new Date(getTime()).
 *      * clone(): deep copy with null extraFields and non-null extraFields.
 *      * equals(Object): this == obj, obj == null, getClass() != obj.getClass(),
 *        name == null vs != null, other.name matching vs not matching.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

public class ZipArchiveEntryGeminiTest {

    // Helper dummy implementation of ZipExtraField for isolated extra-field tests
    private static class DummyExtraField implements ZipExtraField {
        private final ZipShort headerId;
        private byte[] localData = new byte[0];
        private byte[] centralData = new byte[0];

        DummyExtraField(int headerId) {
            this.headerId = new ZipShort(headerId);
        }

        @Override
        public ZipShort getHeaderId() {
            return headerId;
        }

        @Override
        public ZipShort getLocalFileDataLength() {
            return new ZipShort(localData.length);
        }

        @Override
        public ZipShort getCentralDirectoryLength() {
            return new ZipShort(centralData.length);
        }

        @Override
        public byte[] getLocalFileDataData() {
            return localData;
        }

        @Override
        public byte[] getCentralDirectoryData() {
            return centralData;
        }

        @Override
        public void parseFromLocalFileData(byte[] buffer, int offset, int length) {
            localData = new byte[length];
            System.arraycopy(buffer, offset, localData, 0, length);
        }

        @Override
        public void parseFromCentralDirectoryData(byte[] buffer, int offset, int length) {
            centralData = new byte[length];
            System.arraycopy(buffer, offset, centralData, 0, length);
        }
    }

    // Subclass exposing protected constructors/methods
    private static class SubZipArchiveEntry extends ZipArchiveEntry {
        public SubZipArchiveEntry() {
            super();
        }

        public void publicSetPlatform(int platform) {
            super.setPlatform(platform);
        }

        public void publicSetName(String name) {
            super.setName(name);
        }

        public void publicSetExtra() {
            super.setExtra();
        }
    }

    /* =========================================================================
     * Partition C: Defect-Targeted Branch Zone (COMPRESS-93 / testNotEquals)
     * ========================================================================= */

    /**
     * Targets the defect where entries with different names created via
     * ZipArchiveEntry(String) incorrectly evaluate as equal because internal
     * field 'name' remains null on both instances.
     */
    @Test(timeout = 4000)
    public void testNotEqualsFailureCondition() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry1.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry2.txt");

        assertFalse("Entries with different names must not be equal", entry1.equals(entry2));
        assertFalse("Equality check must be symmetric", entry2.equals(entry1));
    }

    /* =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testStringConstructorBasic() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(-1, entry.getMethod());
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getInternalAttributes());
        assertEquals(0L, entry.getExternalAttributes());
        assertEquals(0, entry.getExtraFields().length);
        assertEquals(0, entry.getLocalFileDataExtra().length);
        assertEquals(0, entry.getCentralDirectoryExtra().length);
    }

    @Test(timeout = 4000)
    public void testDefaultProtectedConstructor() {
        SubZipArchiveEntry entry = new SubZipArchiveEntry();
        assertEquals("", entry.getName());
        assertFalse(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testFileConstructorRegularFile() throws IOException {
        File tempFile = File.createTempFile("test_zip_file", ".tmp");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(new byte[]{1, 2, 3, 4, 5});
            fos.close();

            ZipArchiveEntry entry = new ZipArchiveEntry(tempFile, "my_file.dat");
            assertEquals("my_file.dat", entry.getName());
            assertFalse(entry.isDirectory());
            assertEquals(5L, entry.getSize());
            assertEquals(tempFile.lastModified(), entry.getTime());
            assertEquals(new Date(tempFile.lastModified()), entry.getLastModifiedDate());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testFileConstructorDirectoryWithoutTrailingSlash() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_dir_" + System.currentTimeMillis());
        assertTrue(tempDir.mkdirs());
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry(tempDir, "my_dir");
            assertEquals("my_dir/", entry.getName());
            assertTrue(entry.isDirectory());
            assertEquals(tempDir.lastModified(), entry.getTime());
        } finally {
            tempDir.delete();
        }
    }

    @Test(timeout = 4000)
    public void testFileConstructorDirectoryWithTrailingSlash() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "test_dir_slash_" + System.currentTimeMillis());
        assertTrue(tempDir.mkdirs());
        try {
            ZipArchiveEntry entry = new ZipArchiveEntry(tempDir, "already_slash/");
            assertEquals("already_slash/", entry.getName());
            assertTrue(entry.isDirectory());
        } finally {
            tempDir.delete();
        }
    }

    @Test(timeout = 4000)
    public void testZipEntryConstructorWithExtraAndMethod() throws Exception {
        java.util.zip.ZipEntry stdEntry = new java.util.zip.ZipEntry("stdEntry.txt");
        stdEntry.setMethod(java.util.zip.ZipEntry.DEFLATED);
        // Build valid extra bytes: headerId 0x0001 (ZipShort), length 0x0002 (ZipShort), data 2 bytes
        byte[] extraBytes = new byte[]{0x01, 0x00, 0x02, 0x00, (byte) 0xAA, (byte) 0xBB};
        stdEntry.setExtra(extraBytes);

        ZipArchiveEntry entry = new ZipArchiveEntry(stdEntry);
        assertEquals("stdEntry.txt", entry.getName());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testZipEntryConstructorWithoutExtra() throws Exception {
        java.util.zip.ZipEntry stdEntry = new java.util.zip.ZipEntry("noExtra.txt");
        stdEntry.setMethod(java.util.zip.ZipEntry.STORED);
        stdEntry.setExtra(null);

        ZipArchiveEntry entry = new ZipArchiveEntry(stdEntry);
        assertEquals("noExtra.txt", entry.getName());
        assertEquals(java.util.zip.ZipEntry.STORED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testCopyConstructorZipArchiveEntry() throws Exception {
        ZipArchiveEntry orig = new ZipArchiveEntry("original.txt");
        orig.setInternalAttributes(42);
        orig.setExternalAttributes(0x81A40000L);
        orig.setMethod(java.util.zip.ZipEntry.DEFLATED);
        DummyExtraField field = new DummyExtraField(0x1234);
        orig.addExtraField(field);

        ZipArchiveEntry copy = new ZipArchiveEntry(orig);
        assertEquals("original.txt", copy.getName());
        assertEquals(42, copy.getInternalAttributes());
        assertEquals(0x81A40000L, copy.getExternalAttributes());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, copy.getMethod());
        assertEquals(1, copy.getExtraFields().length);
        assertNotNull(copy.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testSupportedCompressionMethods() {
        ZipArchiveEntry entry = new ZipArchiveEntry("methods.bin");
        assertEquals(-1, entry.getMethod());
        assertFalse(entry.isSupportedCompressionMethod());

        entry.setMethod(ZipArchiveEntry.STORED);
        assertEquals(ZipArchiveEntry.STORED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());

        entry.setMethod(ZipArchiveEntry.DEFLATED);
        assertEquals(ZipArchiveEntry.DEFLATED, entry.getMethod());
        assertTrue(entry.isSupportedCompressionMethod());

        entry.setMethod(1); // Unsupported method (e.g., Shrunk)
        assertEquals(1, entry.getMethod());
        assertFalse(entry.isSupportedCompressionMethod());
    }

    @Test(timeout = 4000)
    public void testAttributesGetAndSet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("attrs.txt");
        entry.setInternalAttributes(1234);
        assertEquals(1234, entry.getInternalAttributes());

        entry.setExternalAttributes(56789L);
        assertEquals(56789L, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testUnixModeCalculationFileWritable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        // Mode 0644 (octal): owner read+write, group read, others read. Mode & 0200 != 0 -> read-only bit = 0
        int mode = 0644;
        entry.setUnixMode(mode);

        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0644, entry.getUnixMode());
        long expectedExternal = ((long) mode << 16) | 0; // Not read-only, not directory
        assertEquals(expectedExternal, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testUnixModeCalculationFileReadOnly() {
        ZipArchiveEntry entry = new ZipArchiveEntry("readonly.txt");
        // Mode 0444 (octal): mode & 0200 == 0 -> read-only bit set to 1
        int mode = 0444;
        entry.setUnixMode(mode);

        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0444, entry.getUnixMode());
        long expectedExternal = ((long) mode << 16) | 1; // Read-only bit is 1
        assertEquals(expectedExternal, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testUnixModeCalculationDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        // Mode 0755 (octal): directory flag 0x10 should be set
        int mode = 0755;
        entry.setUnixMode(mode);

        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());
        long expectedExternal = ((long) mode << 16) | 0x10;
        assertEquals(expectedExternal, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testGetUnixModeWhenPlatformNotUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        entry.setExternalAttributes(0644L << 16);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testSetPlatformProtected() {
        SubZipArchiveEntry entry = new SubZipArchiveEntry();
        entry.publicSetPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        entry.publicSetPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
    }

    /* =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extra Fields Operations
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testExtraFieldsSetAndGet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        ZipExtraField[] emptyFields = new ZipExtraField[0];
        entry.setExtraFields(emptyFields);
        assertEquals(0, entry.getExtraFields().length);

        DummyExtraField f1 = new DummyExtraField(0x0001);
        DummyExtraField f2 = new DummyExtraField(0x0002);
        entry.setExtraFields(new ZipExtraField[]{f1, f2});

        assertEquals(2, entry.getExtraFields().length);
        assertSame(f1, entry.getExtraField(new ZipShort(0x0001)));
        assertSame(f2, entry.getExtraField(new ZipShort(0x0002)));
        assertNull(entry.getExtraField(new ZipShort(0x9999)));
    }

    @Test(timeout = 4000)
    public void testAddExtraFieldInitialAndReplacement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        DummyExtraField f1 = new DummyExtraField(0x0005);
        entry.addExtraField(f1);
        assertEquals(1, entry.getExtraFields().length);
        assertSame(f1, entry.getExtraField(new ZipShort(0x0005)));

        // Replacing field with the same header id
        DummyExtraField f1Replacement = new DummyExtraField(0x0005);
        entry.addExtraField(f1Replacement);
        assertEquals(1, entry.getExtraFields().length);
        assertSame(f1Replacement, entry.getExtraField(new ZipShort(0x0005)));

        // Adding second distinct field
        DummyExtraField f2 = new DummyExtraField(0x0006);
        entry.addExtraField(f2);
        assertEquals(2, entry.getExtraFields().length);
        ZipExtraField[] fields = entry.getExtraFields();
        assertSame(f1Replacement, fields[0]);
        assertSame(f2, fields[1]);
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraFieldWhenEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        DummyExtraField f1 = new DummyExtraField(0x0001);
        entry.addAsFirstExtraField(f1);

        assertEquals(1, entry.getExtraFields().length);
        assertSame(f1, entry.getExtraFields()[0]);
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraFieldWithExistingDistinctAndDuplicate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        DummyExtraField f1 = new DummyExtraField(0x0001);
        DummyExtraField f2 = new DummyExtraField(0x0002);
        entry.addExtraField(f1);
        entry.addExtraField(f2);

        DummyExtraField f3 = new DummyExtraField(0x0003);
        entry.addAsFirstExtraField(f3);
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(3, fields.length);
        assertSame(f3, fields[0]);
        assertSame(f1, fields[1]);
        assertSame(f2, fields[2]);

        // Replace an existing field and move it to first
        DummyExtraField f2Replacement = new DummyExtraField(0x0002);
        entry.addAsFirstExtraField(f2Replacement);
        fields = entry.getExtraFields();
        assertEquals(3, fields.length);
        assertSame(f2Replacement, fields[0]);
        assertSame(f3, fields[1]);
        assertSame(f1, fields[2]);
    }

    @Test(timeout = 4000)
    public void testRemoveExtraFieldSuccess() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        DummyExtraField f1 = new DummyExtraField(0x0001);
        DummyExtraField f2 = new DummyExtraField(0x0002);
        entry.addExtraField(f1);
        entry.addExtraField(f2);

        entry.removeExtraField(new ZipShort(0x0001));
        assertEquals(1, entry.getExtraFields().length);
        assertNull(entry.getExtraField(new ZipShort(0x0001)));
        assertSame(f2, entry.getExtraField(new ZipShort(0x0002)));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testRemoveExtraFieldThrowsWhenNullExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(0x0001));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testRemoveExtraFieldThrowsWhenFieldNotFound() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.addExtraField(new DummyExtraField(0x0001));
        entry.removeExtraField(new ZipShort(0x0002));
    }

    @Test(timeout = 4000)
    public void testSetExtraBytesAndMerge() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Header 0x0001, len 2, data [0xAA, 0xBB]
        byte[] validExtra = new byte[]{0x01, 0x00, 0x02, 0x00, (byte) 0xAA, (byte) 0xBB};
        entry.setExtra(validExtra);

        assertEquals(1, entry.getExtraFields().length);
        byte[] localData = entry.getLocalFileDataExtra();
        assertNotNull(localData);
        assertTrue(localData.length > 0);

        // Merge second extra byte payload containing header 0x0002, len 1, data [0xCC]
        byte[] secondExtra = new byte[]{0x02, 0x00, 0x01, 0x00, (byte) 0xCC};
        entry.setExtra(secondExtra);
        assertEquals(2, entry.getExtraFields().length);
        assertNotNull(entry.getExtraField(new ZipShort(0x0001)));
        assertNotNull(entry.getExtraField(new ZipShort(0x0002)));

        // Merge update for existing header 0x0001 with new data [0x11, 0x22]
        byte[] updateExtra = new byte[]{0x01, 0x00, 0x02, 0x00, (byte) 0x11, (byte) 0x22};
        entry.setExtra(updateExtra);
        assertEquals(2, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testSetCentralDirectoryExtraAndMerge() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Header 0x0001, len 2, data [0xAA, 0xBB]
        byte[] centralExtra = new byte[]{0x01, 0x00, 0x02, 0x00, (byte) 0xAA, (byte) 0xBB};
        entry.setCentralDirectoryExtra(centralExtra);

        assertEquals(1, entry.getExtraFields().length);
        byte[] cdExtra = entry.getCentralDirectoryExtra();
        assertNotNull(cdExtra);
        assertTrue(cdExtra.length > 0);

        // Merge update for existing header via central directory
        byte[] updateExtra = new byte[]{0x01, 0x00, 0x02, 0x00, (byte) 0x33, (byte) 0x44};
        entry.setCentralDirectoryExtra(updateExtra);
        assertEquals(1, entry.getExtraFields().length);

        // Merge a newly introduced header via central directory
        byte[] newExtra = new byte[]{0x02, 0x00, 0x01, 0x00, (byte) 0x55};
        entry.setCentralDirectoryExtra(newExtra);
        assertEquals(2, entry.getExtraFields().length);
    }

    /* =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ========================================================================= */

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetNegativeMethodThrows() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(-2);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testSetExtraCorruptBytesThrowsRuntimeException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Malformed extra bytes: declared length 10 but only 1 byte follows
        byte[] corrupt = new byte[]{0x01, 0x00, 0x0A, 0x00, 0x01};
        entry.setExtra(corrupt);
    }

    @Test(timeout = 4000, expected = RuntimeException.class)
    public void testSetCentralDirectoryCorruptBytesThrowsRuntimeException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Malformed extra bytes
        byte[] corrupt = new byte[]{0x01, 0x00, 0x05, 0x00, 0x01};
        entry.setCentralDirectoryExtra(corrupt);
    }

    /* =========================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testCloneIntegrity() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("toClone.txt");
        entry.setInternalAttributes(11);
        entry.setExternalAttributes(22L);
        DummyExtraField field = new DummyExtraField(0x00AA);
        entry.addExtraField(field);

        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, cloned);
        assertEquals(entry.getName(), cloned.getName());
        assertEquals(entry.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(entry.getExternalAttributes(), cloned.getExternalAttributes());
        assertEquals(entry.getExtraFields().length, cloned.getExtraFields().length);
        assertNotNull(cloned.getExtraField(new ZipShort(0x00AA)));

        // Verify modifying clone extra fields does not affect original
        cloned.removeExtraField(new ZipShort(0x00AA));
        assertEquals(0, cloned.getExtraFields().length);
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testCloneWithNullExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("noExtraClone.txt");
        ZipArchiveEntry cloned = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, cloned);
        assertEquals(0, cloned.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry.txt");
        assertEquals("entry.txt".hashCode(), entry1.hashCode());
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsContractComprehensive() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("sample.txt");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("sample.txt");

        // Reflexivity
        assertTrue(entry1.equals(entry1));

        // Null comparison
        assertFalse(entry1.equals(null));

        // Different class comparison
        assertFalse(entry1.equals("sample.txt"));

        // Explicit setName paths
        SubZipArchiveEntry sub1 = new SubZipArchiveEntry();
        sub1.publicSetName("custom.txt");
        SubZipArchiveEntry sub2 = new SubZipArchiveEntry();
        sub2.publicSetName("custom.txt");
        assertTrue(sub1.equals(sub2));

        SubZipArchiveEntry sub3 = new SubZipArchiveEntry();
        sub3.publicSetName(null);
        SubZipArchiveEntry sub4 = new SubZipArchiveEntry();
        sub4.publicSetName(null);
        assertTrue(sub3.equals(sub4));

        assertFalse(sub3.equals(sub1));
        assertFalse(sub1.equals(sub3));

        SubZipArchiveEntry sub5 = new SubZipArchiveEntry();
        sub5.publicSetName("different.txt");
        assertFalse(sub1.equals(sub5));
    }

    @Test(timeout = 4000)
    public void testSetExtraWithoutParametersProtected() {
        SubZipArchiveEntry entry = new SubZipArchiveEntry();
        entry.publicSetExtra();
        assertEquals(0, entry.getLocalFileDataExtra().length);
    }
}