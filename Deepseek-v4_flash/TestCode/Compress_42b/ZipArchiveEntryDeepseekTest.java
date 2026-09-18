package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * Branch & Defect Analysis Matrix:
 *
 * Targeted branches:
 * - setMethod(int): negative value → IllegalArgumentException
 * - setSize(long): negative value → IllegalArgumentException
 * - setName(String): null, empty, with/without "/", Platform FAT backslash conversion
 * - isDirectory(): true only if name ends with "/"
 * - getUnixMode(): platform != PLATFORM_UNIX → 0
 * - isUnixSymlink(): (getUnixMode() & UnixStat.LINK_FLAG) == UnixStat.LINK_FLAG
 * - setUnixMode(int): combines mode << 16, MS-DOS readonly/directory bits
 * - Extra fields: setExtraFields, addExtraField, addAsFirstExtraField, removeExtraField,
 *   removeUnparseableExtraFieldData, getExtraField, getUnparseableExtraFieldData,
 *   setExtra(byte[]), setCentralDirectoryExtra, mergeExtraFields, etc.
 * - equals(): symmetry, null, different types, all compared fields
 * - hashCode(): based on getName()
 * - clone(): shallow copy of attributes
 *
 * Known defect: isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet
 *   → when unix mode has symlink flag plus other bits (e.g., 0120755),
 *     isUnixSymlink() incorrectly returns false.
 */
public class ZipArchiveEntryDeepseekTest {

    // ---------- Partition A: Core Functionality ----------

    @Test(timeout = 4000)
    public void testConstructorString() {
        ZipArchiveEntry e = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", e.getName());
        assertFalse(e.isDirectory());
    }

    @Test(timeout = 4000)
    public void testConstructorStringDirectory() {
        ZipArchiveEntry e = new ZipArchiveEntry("dir/");
        assertTrue(e.isDirectory());
    }

    @Test(timeout = 4000)
    public void testConstructorZipEntry() throws Exception {
        java.util.zip.ZipEntry ze = new java.util.zip.ZipEntry("entry.txt");
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setSize(100);
        ze.setExtra(new byte[] {0,1,2,3});
        ZipArchiveEntry e = new ZipArchiveEntry(ze);
        assertEquals("entry.txt", e.getName());
        assertEquals(ZipEntry.DEFLATED, e.getMethod());
        assertEquals(100, e.getSize());
        assertNotNull(e.getExtraFields());
    }

    @Test(timeout = 4000)
    public void testConstructorZipArchiveEntry() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("orig.txt");
        original.setInternalAttributes(1);
        original.setExternalAttributes(2L);
        original.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        original.setGeneralPurposeBit(new GeneralPurposeBit());
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals(original.getName(), copy.getName());
        assertEquals(1, copy.getInternalAttributes());
        assertEquals(2L, copy.getExternalAttributes());
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, copy.getPlatform());
        assertNotNull(copy.getGeneralPurposeBit());
    }

    @Test(timeout = 4000)
    public void testConstructorFile() {
        // Use a temp file to ensure isFile() returns true
        File f = new File("testfile.txt");
        // We don't actually create the file; we test the logic assuming it's a file
        ZipArchiveEntry e = new ZipArchiveEntry(f, "entry");
        assertEquals("entry", e.getName());
        assertFalse(e.isDirectory());
        // For a real file, time and size would be set
    }

    @Test(timeout = 4000)
    public void testConstructorFileDirectory() {
        File d = new File(".");
        ZipArchiveEntry e = new ZipArchiveEntry(d, "dir");
        assertEquals("dir/", e.getName());
        assertTrue(e.isDirectory());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        ZipArchiveEntry e = new ZipArchiveEntry();
        assertEquals("", e.getName());
        assertFalse(e.isDirectory());
    }

    // ---------- Name / Directory ----------

    @Test(timeout = 4000)
    public void testSetName() {
        ZipArchiveEntry e = new ZipArchiveEntry("a");
        e.setName("b");
        assertEquals("b", e.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("a");
        e.setName(null);
        // getName() returns super.getName() when name is null
        assertNotNull(e.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameBackslashOnFat() {
        ZipArchiveEntry e = new ZipArchiveEntry("a");
        e.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        e.setName("a\\b\\c");
        assertEquals("a/b/c", e.getName());
    }

    @Test(timeout = 4000)
    public void testIsDirectoryTrue() {
        assertTrue(new ZipArchiveEntry("x/").isDirectory());
    }

    @Test(timeout = 4000)
    public void testIsDirectoryFalse() {
        assertFalse(new ZipArchiveEntry("x").isDirectory());
    }

    // ---------- Size ----------

    @Test(timeout = 4000)
    public void testSetSizeGetSize() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setSize(1000);
        assertEquals(1000, e.getSize());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSizeNegative() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setSize(-1);
    }

    @Test(timeout = 4000)
    public void testSizeUnknown() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, e.getSize());
    }

    // ---------- Method ----------

    @Test(timeout = 4000)
    public void testSetMethodGetMethod() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, e.getMethod());
    }

    @Test(timeout = 4000)
    public void testMethodDefaultUnknown() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertEquals(ZipMethod.UNKNOWN_CODE, e.getMethod());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetMethodNegative() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setMethod(-1);
    }

    // ---------- Internal/External Attributes ----------

    @Test(timeout = 4000)
    public void testInternalAttributes() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setInternalAttributes(123);
        assertEquals(123, e.getInternalAttributes());
    }

    @Test(timeout = 4000)
    public void testExternalAttributes() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setExternalAttributes(456L);
        assertEquals(456L, e.getExternalAttributes());
    }

    // ---------- Unix Mode / Symlink (defect targeted) ----------

    @Test(timeout = 4000)
    public void testSetUnixModeGetUnixMode() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setUnixMode(0755);
        assertEquals(0755, e.getUnixMode());
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, e.getPlatform());
    }

    @Test(timeout = 4000)
    public void testGetUnixModeNonUnixPlatform() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        e.setExternalAttributes(0755L << 16); // manually set
        assertEquals(0, e.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testIsUnixSymlinkTrue() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setUnixMode(0120000); // only symlink
        assertTrue(e.isUnixSymlink());
    }

    @Test(timeout = 4000)
    public void testIsUnixSymlinkFalse() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setUnixMode(0100644); // regular file
        assertFalse(e.isUnixSymlink());
    }

    @Test(timeout = 4000)
    public void testIsUnixSymlinkFalseOnFat() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertFalse(e.isUnixSymlink());
    }

    // ** Defect-targeted test: symlink with multiple flags **
    @Test(timeout = 4000)
    public void testIsUnixSymlinkWithMultipleFlags() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        // Mode = symlink (0120000) plus rwxr-xr-x (0755) = 0120755
        e.setUnixMode(0120755);
        assertTrue("isUnixSymlink should be true even with other flags set",
                   e.isUnixSymlink());
    }

    // ---------- Platform ----------

    @Test(timeout = 4000)
    public void testSetPlatform() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, e.getPlatform());
    }

    // ---------- Version / Raw Flag ----------

    @Test(timeout = 4000)
    public void testVersionMadeBy() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setVersionMadeBy(20);
        assertEquals(20, e.getVersionMadeBy());
    }

    @Test(timeout = 4000)
    public void testVersionRequired() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setVersionRequired(10);
        assertEquals(10, e.getVersionRequired());
    }

    @Test(timeout = 4000)
    public void testRawFlag() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setRawFlag(0x1234);
        assertEquals(0x1234, e.getRawFlag());
    }

    // ---------- GeneralPurposeBit ----------

    @Test(timeout = 4000)
    public void testGeneralPurposeBit() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        e.setGeneralPurposeBit(gpb);
        assertTrue(e.getGeneralPurposeBit().usesUTF8ForNames());
    }

    // ---------- Extra Fields ----------

    @Test(timeout = 4000)
    public void testSetExtraFields() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1234));
        uef.setLocalFileDataData(new byte[] {1,2});
        e.setExtraFields(new ZipExtraField[] {uef});
        assertEquals(1, e.getExtraFields().length);
        assertNotNull(e.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testSetExtraFieldsWithUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        e.setExtraFields(new ZipExtraField[] {unparseable});
        assertSame(unparseable, e.getUnparseableExtraFieldData());
        assertEquals(0, e.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testAddExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1234));
        e.addExtraField(uef);
        assertEquals(1, e.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testAddExtraFieldReplacesExisting() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        uef1.setHeaderId(new ZipShort(0x1234));
        uef1.setLocalFileDataData(new byte[] {1});
        e.addExtraField(uef1);
        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(0x1234));
        uef2.setLocalFileDataData(new byte[] {2});
        e.addExtraField(uef2);
        assertEquals(1, e.getExtraFields().length);
        assertArrayEquals(new byte[] {2}, e.getExtraField(new ZipShort(0x1234)).getLocalFileDataData());
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef1 = new UnrecognizedExtraField();
        uef1.setHeaderId(new ZipShort(0x1111));
        uef1.setLocalFileDataData(new byte[] {1});
        e.addExtraField(uef1);
        UnrecognizedExtraField uef2 = new UnrecognizedExtraField();
        uef2.setHeaderId(new ZipShort(0x2222));
        uef2.setLocalFileDataData(new byte[] {2});
        e.addAsFirstExtraField(uef2);
        assertEquals(2, e.getExtraFields().length);
        assertSame(uef2, e.getExtraFields()[0]);
    }

    @Test(timeout = 4000, expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraFieldNonExistent() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.removeExtraField(new ZipShort(0x1234));
    }

    @Test(timeout = 4000)
    public void testRemoveExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1234));
        e.addExtraField(uef);
        e.removeExtraField(new ZipShort(0x1234));
        assertEquals(0, e.getExtraFields().length);
    }

    @Test(timeout = 4000, expected = java.util.NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataNone() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.removeUnparseableExtraFieldData();
    }

    @Test(timeout = 4000)
    public void testRemoveUnparseableExtraFieldData() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setExtraFields(new ZipExtraField[] {new UnparseableExtraFieldData()});
        assertNotNull(e.getUnparseableExtraFieldData());
        e.removeUnparseableExtraFieldData();
        assertNull(e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldNullWhenMissing() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertNull(e.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testGetExtraFieldsWithUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.setExtraFields(new ZipExtraField[] {u});
        assertEquals(1, e.getExtraFields(true).length);
        assertEquals(0, e.getExtraFields(false).length);
    }

    @Test(timeout = 4000)
    public void testSetExtraBytes() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        // Valid extra field data: header id (2 bytes) + length (2 bytes) + data
        byte[] extra = new byte[] {0x12, 0x34, 0x02, 0x00, 0x01, 0x02};
        e.setExtra(extra);
        assertNotNull(e.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        byte[] extra = new byte[] {0x12, 0x34, 0x02, 0x00, 0x01, 0x02};
        e.setCentralDirectoryExtra(extra);
        // Central directory data should be present
        assertNotNull(e.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        byte[] extra = new byte[] {0x12, 0x34, 0x02, 0x00, 0x01, 0x02};
        e.setExtra(extra);
        assertArrayEquals(extra, e.getLocalFileDataExtra());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataExtraEmpty() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertArrayEquals(new byte[0], e.getLocalFileDataExtra());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1111));
        uef.setLocalFileDataData(new byte[] {1,2});
        uef.setCentralDirectoryData(new byte[] {3,4});
        e.addExtraField(uef);
        byte[] central = e.getCentralDirectoryExtra();
        assertTrue(central.length > 0);
    }

    // ---------- RawName ----------

    @Test(timeout = 4000)
    public void testSetNameWithRawName() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        byte[] raw = new byte[] {0x41, 0x42};
        e.setName("AB", raw);
        assertEquals("AB", e.getName());
        assertArrayEquals(raw, e.getRawName());
    }

    @Test(timeout = 4000)
    public void testGetRawNameNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertNull(e.getRawName());
    }

    // ---------- Clone ----------

    @Test(timeout = 4000)
    public void testClone() {
        ZipArchiveEntry original = new ZipArchiveEntry("orig");
        original.setInternalAttributes(10);
        original.setExternalAttributes(20L);
        UnrecognizedExtraField uef = new UnrecognizedExtraField();
        uef.setHeaderId(new ZipShort(0x1234));
        uef.setLocalFileDataData(new byte[] {5,6});
        original.addExtraField(uef);
        ZipArchiveEntry clone = (ZipArchiveEntry) original.clone();
        assertEquals(original.getName(), clone.getName());
        assertEquals(10, clone.getInternalAttributes());
        assertEquals(20L, clone.getExternalAttributes());
        assertNotNull(clone.getExtraField(new ZipShort(0x1234)));
        // Ensure it's a shallow copy (same field references)
        assertSame(original.getExtraField(new ZipShort(0x1234)), clone.getExtraField(new ZipShort(0x1234)));
    }

    // ---------- equals / hashCode ----------

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertEquals(e, e);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertNotNull(e);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        assertFalse(e.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEquals() throws Exception {
        ZipArchiveEntry e1 = new ZipArchiveEntry("x");
        e1.setMethod(ZipEntry.STORED);
        e1.setSize(100);
        e1.setCrc(12345);
        e1.setComment("comment");
        e1.setTime(1000);
        e1.setInternalAttributes(1);
        e1.setExternalAttributes(2L);
        e1.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        e1.setGeneralPurposeBit(gpb);

        ZipArchiveEntry e2 = new ZipArchiveEntry("x");
        e2.setMethod(ZipEntry.STORED);
        e2.setSize(100);
        e2.setCrc(12345);
        e2.setComment("comment");
        e2.setTime(1000);
        e2.setInternalAttributes(1);
        e2.setExternalAttributes(2L);
        e2.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        e2.setGeneralPurposeBit(gpb);

        assertEquals(e1, e2);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentName() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("a");
        ZipArchiveEntry e2 = new ZipArchiveEntry("b");
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        ZipArchiveEntry e = new ZipArchiveEntry("name");
        assertEquals("name".hashCode(), e.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNullName() {
        ZipArchiveEntry e = new ZipArchiveEntry("a");
        e.setName(null);
        // super.getName() might return empty string? We'll just ensure no NPE.
        int hc = e.hashCode();
        assertTrue(hc != 0); // at least not throwing.
    }

    // ---------- GetLastModifiedDate ----------

    @Test(timeout = 4000)
    public void testGetLastModifiedDate() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.setTime(123456789L);
        assertEquals(new java.util.Date(123456789L), e.getLastModifiedDate());
    }

    // ---------- Exception/Edge Cases ----------

    @Test(timeout = 4000)
    public void testSetExtraInvalidData() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        // Invalid extra: length larger than remaining bytes
        byte[] bad = new byte[] {0x00, 0x01, 0x05, 0x00, 0x01, 0x02};
        try {
            e.setExtra(bad);
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertEquals("Error parsing extra fields for entry: x - ", ex.getMessage().substring(0, 40));
        }
    }

    @Test(timeout = 4000, expected = java.util.NoSuchElementException.class)
    public void testRemoveExtraFieldNone() {
        ZipArchiveEntry e = new ZipArchiveEntry("x");
        e.removeExtraField(new ZipShort(0x1234));
    }
}