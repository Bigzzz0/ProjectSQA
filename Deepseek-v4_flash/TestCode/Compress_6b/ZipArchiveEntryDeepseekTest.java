package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/* [Branch & Defect Analysis Matrix]
 * Branches covered:
 * - Constructors: String, java.util.zip.ZipEntry, ZipArchiveEntry, File, default.
 * - Getters/Setters for method (including negative), internal/external attributes, platform, unix mode.
 * - Extra fields: add, addFirst, remove, set, get, getExtraField, setExtraFields.
 * - getName/setName, isDirectory, hashCode, equals, clone.
 * - Boundary: method -1,0,1, negative; external attributes 0, max; platform 0,3; extra fields empty, one, multiple.
 * - Exception paths: setMethod negative, removeExtraField on empty, setExtra invalid bytes, setCentralDirectoryExtra invalid.
 * - Defect-targeted: Equals comparison fails when entries created via String constructor have different names but are considered equal due to only comparing the custom name field (which is always null for String constructor).
 * - Also tests for equals symmetry, null, different class.
 * - Clone deep copy.
 * - HashCode consistency with equals.
 * - mergeExtraFields local/central distinction.
 */

public class ZipArchiveEntryDeepseekTest {

    // ---------- Part A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testStringConstructorName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testStringConstructorDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        assertTrue(entry.isDirectory());
        assertEquals("dir/", entry.getName());
    }

    @Test(timeout = 4000)
    public void testJavaUtilZipEntryConstructor() throws Exception {
        ZipEntry ze = new ZipEntry("foo");
        ze.setMethod(ZipEntry.STORED);
        ze.setSize(100);
        ZipArchiveEntry entry = new ZipArchiveEntry(ze);
        assertEquals("foo", entry.getName());
        assertEquals(ZipEntry.STORED, entry.getMethod());
        assertEquals(100, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() throws Exception {
        ZipArchiveEntry orig = new ZipArchiveEntry("orig");
        orig.setInternalAttributes(1);
        orig.setExternalAttributes(2);
        ZipArchiveEntry copy = new ZipArchiveEntry(orig);
        assertEquals("orig", copy.getName());
        assertEquals(1, copy.getInternalAttributes());
        assertEquals(2, copy.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testFileConstructorFile() {
        File f = new File("/nonexistent/test.txt");
        ZipArchiveEntry entry = new ZipArchiveEntry(f, "entryName");
        assertEquals("entryName", entry.getName());
        assertFalse(entry.isDirectory());
        assertEquals(0L, entry.getSize());
        assertEquals(f.lastModified(), entry.getTime());
    }

    @Test(timeout = 4000)
    public void testMethodSetAndGet() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(-1, entry.getMethod());
        entry.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, entry.getMethod());
        entry.setMethod(ZipEntry.DEFLATED);
        assertEquals(ZipEntry.DEFLATED, entry.getMethod());
    }

    @Test(timeout = 4000)
    public void testIsSupportedCompressionMethod() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.isSupportedCompressionMethod());
        entry.setMethod(ZipEntry.STORED);
        assertTrue(entry.isSupportedCompressionMethod());
        entry.setMethod(ZipEntry.DEFLATED);
        assertTrue(entry.isSupportedCompressionMethod());
        entry.setMethod(7);
        assertFalse(entry.isSupportedCompressionMethod());
    }

    @Test(timeout = 4000)
    public void testInternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getInternalAttributes());
        entry.setInternalAttributes(123);
        assertEquals(123, entry.getInternalAttributes());
        entry.setInternalAttributes(-1);
        assertEquals(-1, entry.getInternalAttributes());
    }

    @Test(timeout = 4000)
    public void testExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0L, entry.getExternalAttributes());
        entry.setExternalAttributes(456L);
        assertEquals(456L, entry.getExternalAttributes());
        entry.setExternalAttributes(-1L);
        assertEquals(-1L, entry.getExternalAttributes());
        entry.setExternalAttributes(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
    }

    @Test(timeout = 4000)
    public void testSetUnixMode() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        long expected = (0755L << 16) | ((0755 & 0200) == 0 ? 1L : 0L) | 0L;
        assertEquals(expected, entry.getExternalAttributes());
        assertEquals(0755, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testSetUnixModeDirectory() {
        ZipArchiveEntry entry = new ZipArchiveEntry("dir/");
        entry.setUnixMode(0755);
        long expected = (0755L << 16) | ((0755 & 0200) == 0 ? 1L : 0L) | 0x10L;
        assertEquals(expected, entry.getExternalAttributes());
        assertEquals(0755, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testGetUnixModeOnNonUnix() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(0, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testExtraFieldsInitiallyEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testAddExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        ZipShort header = new ZipShort(0x1234);
        uef.setHeaderId(header);
        uef.setLocalFileDataData(new byte[] {1, 2, 3});
        entry.addExtraField(uef);
        assertEquals(1, entry.getExtraFields().length);
        assertSame(uef, entry.getExtraField(header));
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        ZipShort h1 = new ZipShort(1);
        uef1.setHeaderId(h1);
        uef1.setLocalFileDataData(new byte[] {1});
        entry.addExtraField(uef1);
        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        ZipShort h2 = new ZipShort(2);
        uef2.setHeaderId(h2);
        uef2.setLocalFileDataData(new byte[] {2});
        entry.addAsFirstExtraField(uef2);
        assertEquals(2, entry.getExtraFields().length);
        assertEquals(h2, entry.getExtraFields()[0].getHeaderId());
        assertEquals(h1, entry.getExtraFields()[1].getHeaderId());
    }

    @Test(timeout = 4000)
    public void testRemoveExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        ZipShort header = new ZipShort(0x1234);
        uef.setHeaderId(header);
        entry.addExtraField(uef);
        entry.removeExtraField(header);
        assertEquals(0, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testSetExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(1));
        entry.setExtraFields(new ZipExtraField[] {uef});
        assertEquals(1, entry.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldNullWhenNone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNull(entry.getExtraField(new ZipShort(1)));
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNotNull(entry.getLocalFileDataExtra());
        assertEquals(0, entry.getLocalFileDataExtra().length);
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryExtra() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertNotNull(entry.getCentralDirectoryExtra());
        assertEquals(0, entry.getCentralDirectoryExtra().length);
    }

    @Test(timeout = 4000)
    public void testGetLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        Date date = entry.getLastModifiedDate();
        assertNotNull(date);
    }

    // ---------- Part B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000)
    public void testMethodNegativeThrows() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        try {
            entry.setMethod(-1);
            fail("Expected IllegalArgumentException for negative method");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMethodZero() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setMethod(0);
        assertEquals(0, entry.getMethod());
    }

    @Test(timeout = 4000)
    public void testGetNameFromSuperWhenNameNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("superName");
        assertEquals("superName", entry.getName());
    }

    @Test(timeout = 4000)
    public void testSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.setName("newname");
        assertEquals("newname", entry.getName());
    }

    @Test(timeout = 4000)
    public void testIsDirectoryTrue() {
        ZipArchiveEntry entry = new ZipArchiveEntry("mydir/");
        assertTrue(entry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testIsDirectoryFalse() {
        ZipArchiveEntry entry = new ZipArchiveEntry("myfile.txt");
        assertFalse(entry.isDirectory());
    }

    // ---------- Part C: Defect-Targeted Branch Zone (COMPRESS issue) ----------

    @Test(timeout = 4000)
    public void testEqualsForStringConstructorDifferentNames() {
        // This triggers the known defect: two entries with different names are considered equal
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("bar");
        assertFalse("Entries with different names should not be equal", entry1.equals(entry2));
        assertNotEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsForStringConstructorSameName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        assertTrue(entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullName() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test"); // this.name = null
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("test");
        ZipArchiveEntry entry2 = null;
        try {
            entry2 = new ZipArchiveEntry(ze); // this.name = "test"
        } catch (ZipException e) {
            fail(e.getMessage());
        }
        // They have the same logical name, but the buggy equals incorrectly returns false
        assertTrue("Entries with same name should be equal", entry1.equals(entry2));
        assertEquals(entry1.hashCode(), entry2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertTrue(entry.equals(entry));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        assertFalse(entry.equals("string"));
    }

    // ---------- Part D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testRemoveExtraFieldThrowsWhenEmpty() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        try {
            entry.removeExtraField(new ZipShort(1));
            fail("Expected NoSuchElementException");
        } catch (java.util.NoSuchElementException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetExtraInvalidBytes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] invalid = new byte[] {0, 1, 2, 3};
        try {
            entry.setExtra(invalid);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetCentralDirectoryExtraInvalid() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] invalid = new byte[] {0, 1, 2, 3};
        try {
            entry.setCentralDirectoryExtra(invalid);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetExtraWithValidBytes() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        ZipShort header = new ZipShort(0x1234);
        uef.setHeaderId(header);
        byte[] data = new byte[] {1, 2, 3, 4};
        uef.setLocalFileDataData(data);
        byte[] extraBytes = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] {uef});
        entry.setExtra(extraBytes);
        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(1, fields.length);
        assertArrayEquals(data, fields[0].getLocalFileDataData());
    }

    @Test(timeout = 4000)
    public void testMergeExtraFieldsWhenExisting() throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // First add an extra field
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        ZipShort header = new ZipShort(0x1234);
        uef1.setHeaderId(header);
        byte[] originalData = new byte[] {1, 2, 3};
        uef1.setLocalFileDataData(originalData);
        entry.addExtraField(uef1);

        // Now set extra with bytes that contain the same header but different data
        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(header);
        byte[] newData = new byte[] {4, 5, 6};
        uef2.setLocalFileDataData(newData);
        byte[] mergedBytes = ExtraFieldUtils.mergeLocalFileDataData(new ZipExtraField[] {uef2});
        entry.setExtra(mergedBytes);
        // The existing field should have been updated with the new local data
        ZipExtraField after = entry.getExtraField(header);
        assertNotNull(after);
        assertArrayEquals(newData, after.getLocalFileDataData());
    }

    // ---------- Part E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testClone() {
        ZipArchiveEntry entry = new ZipArchiveEntry("original");
        entry.setInternalAttributes(5);
        entry.setExternalAttributes(10);
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1111));
        uef.setLocalFileDataData(new byte[] {1});
        entry.addExtraField(uef);
        ZipArchiveEntry clone = (ZipArchiveEntry) entry.clone();
        assertNotSame(entry, clone);
        assertEquals(entry.getName(), clone.getName());
        assertEquals(entry.getInternalAttributes(), clone.getInternalAttributes());
        assertEquals(entry.getExternalAttributes(), clone.getExternalAttributes());
        assertEquals(entry.getExtraFields().length, clone.getExtraFields().length);
        assertNotSame(entry.getExtraFields(), clone.getExtraFields());
        // Modify original and ensure clone unchanged
        entry.setInternalAttributes(99);
        assertEquals(5, clone.getInternalAttributes());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        ZipArchiveEntry entry = new ZipArchiveEntry("foo");
        int hash1 = entry.hashCode();
        assertEquals(hash1, entry.hashCode());
    }
}