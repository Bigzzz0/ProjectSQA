package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.zip.ZipException;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target Branches & Conditions:
 * 1. setName(): platform == PLATFORM_FAT && name.indexOf("/") == -1 -> replace backslashes
 *    - PLATFORM_UNIX branch: no replacement
 *    - name already contains "/": no replacement
 *    - name is null: skip
 * 2. setMethod(): negative value throws IllegalArgumentException
 * 3. setSize(): negative value throws IllegalArgumentException
 * 4. equals(): 
 *    - this == obj -> true
 *    - obj null -> false
 *    - different class -> false
 *    - name null vs non-null
 *    - comment null vs non-null
 *    - time, internalAttributes, platform, externalAttributes, method, size, crc, compressedSize
 *    - central/local extra arrays comparison
 *    - gpb.equals()
 * 5. hashCode(): delegation to getName().hashCode()
 * 6. isDirectory(): getName().endsWith("/")
 * 7. getExtraFields(): extraFields null, includeUnparseable true/false, unparseableExtra null/non-null
 * 8. addExtraField(): UnparseableExtraFieldData vs normal, extraFields lazy init
 * 9. addAsFirstExtraField(): ordering, copy null branch
 * 10. removeExtraField(): extraFields null -> NoSuchElementException, key not found -> NoSuchElementException
 * 11. removeUnparseableExtraFieldData(): unparseableExtra null -> NoSuchElementException
 * 12. clone(): deep copy of extra fields
 * 13. setUnixMode(): bit manipulation, platform change
 * 14. getUnixMode(): platform != PLATFORM_UNIX -> 0
 * 15. mergeExtraFields(): extraFields == null -> setExtraFields, else -> existing null or non-null, local flag
 * 
 * Defect Target: equals() handling of null comment vs empty comment
 *   - Known bug: entry with null comment considered equal to entry with empty comment ("")
 *   - This should be distinguishable: null comment != empty comment
 *   - Test must assert that entries differing only by null vs empty comment are NOT equal
 */
public class ZipArchiveEntryDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorString() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testConstructorStringDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
        assertEquals("dir/", entry.getName());
    }

    @Test(timeout = 4000)
    public void testConstructorZipEntry() throws ZipException {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("ze.txt");
        ze.setMethod(java.util.zip.ZipEntry.DEFLATED);
        ze.setSize(100);
        ze.setExtra(new byte[] {0, 0, 0, 0}); // empty extra field
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("ze.txt", entry.getName());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, entry.getMethod());
        assertEquals(100, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testConstructorZipArchiveEntry() throws ZipException {
        ZipArchiveEntry original = new ZipArchiveEntry("orig.txt");
        original.setInternalAttributes(1);
        original.setExternalAttributes(2);
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original.getName(), copy.getName());
        assertEquals(1, copy.getInternalAttributes());
        assertEquals(2, copy.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testConstructorFileDirectory() {
        // Using a file that doesn't exist but name with trailing slash
        ZipArchiveEntry entry = new ZipArchiveEntry(new File("/tmp"), "mydir");
        assertTrue(entry.isDirectory());
        assertTrue(entry.getName().endsWith("/"));
    }

    @Test(timeout = 4000)
    public void testConstructorFileRegular() {
        ZipArchiveEntry entry = new ZipArchiveEntry(new File("test.txt"), "test.txt");
        assertFalse(entry.isDirectory());
        assertEquals("test.txt", entry.getName());
    }

    @Test(timeout = 4000)
    public void testSetGetMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertEquals(-1, entry.getMethod());
        entry.setMethod(8); // deflated
        assertEquals(8, entry.getMethod());
    }

    @Test(timeout = 4000)
    public void testSetGetSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, entry.getSize());
        entry.setSize(12345L);
        assertEquals(12345L, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testInternalExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertEquals(0, entry.getInternalAttributes());
        assertEquals(0L, entry.getExternalAttributes());
        entry.setInternalAttributes(0xFF);
        entry.setExternalAttributes(0xFFFFL);
        assertEquals(0xFF, entry.getInternalAttributes());
        assertEquals(0xFFFFL, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test(timeout = 4000)
    public void testSetUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setUnixMode(0644);
        int mode = entry.getUnixMode();
        assertEquals(0644, mode);
        // Check that platform is UNIX
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test(timeout = 4000)
    public void testGetUnixModeNonUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // Default platform is FAT
        assertEquals(0, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertNotNull(entry.getGeneralPurposeBit());
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
    }

    @Test(timeout = 4000)
    public void testLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setTime(1000000L);
        Date expected = new Date(1000000L);
        assertEquals(expected, entry.getLastModifiedDate());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testSetNameBackslashReplacement() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setName("a\\b\\c.txt");
        assertEquals("a/b/c.txt", entry.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameNoBackslash() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setName("a/b/c.txt");
        assertEquals("a/b/c.txt", entry.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameUnixPlatformNoReplace() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        entry.setName("a\\b\\c.txt");
        assertEquals("a\\b\\c.txt", entry.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameWithRawName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        byte[] raw = {0x61, 0x62, 0x63}; // "abc"
        entry.setName("abc", raw);
        assertEquals("abc", entry.getName());
        assertArrayEquals(raw, entry.getRawName());
    }

    @Test(timeout = 4000)
    public void testGetRawNameReturnsCopy() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        byte[] raw = {0x61, 0x62};
        entry.setName("ab", raw);
        byte[] result = entry.getRawName();
        assertArrayEquals(raw, result);
        // Verify it's a copy
        result[0] = 0x00;
        assertNotEquals(result[0], entry.getRawName()[0]);
    }

    @Test(timeout = 4000)
    public void testGetRawNameNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertNull(entry.getRawName());
    }

    @Test(timeout = 4000)
    public void testSetLargeSize() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        long largeSize = 0x100000000L; // > 4GB
        entry.setSize(largeSize);
        assertEquals(largeSize, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataExtraEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertArrayEquals(new byte[0], entry.getLocalFileDataExtra());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryExtraEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Known defect: equals() treats null comment same as empty comment

    @Test(timeout = 4000)
    public void testEqualsNullCommentVsEmptyComment() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry2.setComment("");
        // These should NOT be equal because comments differ (null != "")
        assertFalse("Entries with null comment and empty comment should not be equal",
                    entry1.equals(entry2));
        // Also test symmetric behavior
        assertFalse(entry2.equals(entry1));
    }

    @Test(timeout = 4000)
    public void testEqualsBothNullComment() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry2.setComment(null);
        assertEquals(entry1, entry2);
        assertEquals(entry2, entry1);
    }

    @Test(timeout = 4000)
    public void testEqualsNullName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        // Force name to null via setName(null) - protected, but accessible
        // Use reflection or test via clone? Actually setName is protected, we can call it.
        // setName is protected, but test class is in same package, so accessible.
        entry1.setName(null);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertFalse(entry1.equals(entry2));
        assertFalse(entry2.equals(entry1));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMethodNegative() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setMethod(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSizeNegative() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.setSize(-1);
    }

    @Test(expected = java.util.NoSuchElementException.class, timeout = 4000)
    public void testRemoveExtraFieldNullMap() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.removeExtraField(new ZipShort(1));
    }

    @Test(expected = java.util.NoSuchElementException.class, timeout = 4000)
    public void testRemoveExtraFieldNonExistent() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // First add a field so map is non-null
        entry.addExtraField(new AsiExtraField());
        entry.removeExtraField(new ZipShort(0xFFFF)); // non-existent
    }

    @Test(expected = java.util.NoSuchElementException.class, timeout = 4000)
    public void testRemoveUnparseableExtraFieldDataNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        entry.removeUnparseableExtraFieldData();
    }

    @Test(timeout = 4000)
    public void testAddExtraFieldUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testAddExtraFieldNormal() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        AsiExtraField asiField = new AsiExtraField();
        entry.addExtraField(asiField);
        assertSame(asiField, entry.getExtraField(asiField.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        AsiExtraField asiField = new AsiExtraField();
        entry.addAsFirstExtraField(asiField);
        // Should be present and first
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(asiField, fields[0]);
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraFieldWhenExtraFieldsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        AsiExtraField asiField = new AsiExtraField();
        entry.addAsFirstExtraField(asiField);
        assertSame(asiField, entry.getExtraField(asiField.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldsIncludeUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        ZipExtraField[] fields = entry.getExtraFields(true);
        assertTrue(fields.length >= 1);
        boolean found = false;
        for (ZipExtraField f : fields) {
            if (f == unparseable) found = true;
        }
        assertTrue("Unparseable field should be included", found);
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldsExcludeUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.addExtraField(unparseable);
        ZipExtraField[] fields = entry.getExtraFields(false);
        boolean found = false;
        for (ZipExtraField f : fields) {
            if (f == unparseable) found = true;
        }
        assertFalse("Unparseable field should NOT be included", found);
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldsWhenExtraFieldsNullExcludeUnparseableNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        ZipExtraField[] fields = entry.getExtraFields(false);
        assertEquals(0, fields.length);
    }

    @Test(timeout = 4000)
    public void testSetExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        AsiExtraField asiField = new AsiExtraField();
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        entry.setExtraFields(new ZipExtraField[] { asiField, unparseable });
        assertSame(asiField, entry.getExtraField(asiField.getHeaderId()));
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testSetExtraBytes() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // Create valid extra field bytes (AsiExtraField header 0x756E)
        AsiExtraField asiField = new AsiExtraField();
        asiField.setMode(0644);
        asiField.setDirectory(false);
        byte[] localData = asiField.getLocalFileDataData();
        // Build header + length + data
        byte[] extra = new byte[4 + localData.length];
        extra[0] = (byte) (asiField.getHeaderId().getValue() & 0xFF);
        extra[1] = (byte) ((asiField.getHeaderId().getValue() >> 8) & 0xFF);
        extra[2] = (byte) (localData.length & 0xFF);
        extra[3] = (byte) ((localData.length >> 8) & 0xFF);
        System.arraycopy(localData, 0, extra, 4, localData.length);
        entry.setExtra(extra);
        assertNotNull(entry.getExtraField(asiField.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        // Create minimal central directory extra (empty)
        entry.setCentralDirectoryExtra(new byte[] {0, 0, 0, 0});
        // Should not throw
        assertNotNull(entry.getCentralDirectoryExtra());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testHashCode() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("hashTest");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("hashTest");
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferentName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsIdentity() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertTrue(entry.equals(entry));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertFalse(entry.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ZipArchiveEntry entry = new ZipArchiveEntry("entry");
        assertFalse(entry.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsByName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("same");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("same");
        assertTrue(entry1.equals(entry2));
        assertTrue(entry2.equals(entry1));
    }

    @Test(timeout = 4000)
    public void testEqualsByNameDifferent() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("one");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("two");
        assertFalse(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByComment() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setComment("comment");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setComment("comment");
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByCommentDifferent() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setComment("comment1");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setComment("comment2");
        assertFalse(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByTime() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setTime(1000L);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setTime(1000L);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByTimeDifferent() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setTime(1000L);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setTime(2000L);
        assertFalse(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByAttributes() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setInternalAttributes(5);
        entry1.setExternalAttributes(10L);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setInternalAttributes(5);
        entry2.setExternalAttributes(10L);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByPlatform() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByMethod() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setMethod(8);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setMethod(8);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsBySize() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        entry1.setSize(100);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        entry2.setSize(100);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testEqualsByGpb() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entry");
        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useDataDescriptor(true);
        entry1.setGeneralPurposeBit(gpb1);
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entry");
        GeneralPurposeBit gpb2 = new GeneralPurposeBit();
        gpb2.useDataDescriptor(true);
        entry2.setGeneralPurposeBit(gpb2);
        assertTrue(entry1.equals(entry2));
    }

    @Test(timeout = 4000)
    public void testClone() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("cloneTest");
        original.setInternalAttributes(7);
        original.setExternalAttributes(14L);
        original.setUnixMode(0755);
        original.addExtraField(new AsiExtraField());
        ZipArchiveEntry cloned = (ZipArchiveEntry) original.clone();
        assertEquals(original.getName(), cloned.getName());
        assertEquals(original.getInternalAttributes(), cloned.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), cloned.getExternalAttributes());
        assertEquals(original.getUnixMode(), cloned.getUnixMode());
        assertEquals(original.getPlatform(), cloned.getPlatform());
        // Extra fields should be present but not same reference
        assertNotSame(original.getExtraFields(), cloned.getExtraFields());
    }

    @Test(timeout = 4000)
    public void testIsDirectorySlash() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testIsDirectoryNoSlash() {
        ZipArchiveEntry entry = new ZipArchiveEntry("file.txt");
        assertFalse(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testConstructorWithBytesAndUnparseable() {
        // Test setExtraFields with unparseable data
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setExtraFields(new ZipExtraField[] { new UnparseableExtraFieldData() });
        assertNotNull(entry.getUnparseableExtraFieldData());
    }
}