package org.apache.commons.compress.archivers.zip;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ZipArchiveEntry
 * 
 * Partitions:
 * A. Core Functional Logic & State Transitions
 *    - Constructors (String, ZipEntry, ZipArchiveEntry, protected, File+String)
 *    - Getters/setters: method, size, internal/external attributes, platform, name, rawName, generalPurposeBit
 *    - Extra fields: setExtraFields, getExtraFields, addExtraField, addAsFirstExtraField, removeExtraField,
 *      removeUnparseableExtraFieldData, getExtraField, getUnparseableExtraFieldData, setExtra(byte[]),
 *      setExtra(), setCentralDirectoryExtra, getLocalFileDataExtra, getCentralDirectoryExtra
 *    - Unix mode: setUnixMode, getUnixMode
 *    - Directory detection: isDirectory
 *    - Clone, equals, hashCode, getLastModifiedDate
 * 
 * B. Boundary Value Analysis & Extremes
 *    - Null name, empty name, name with trailing slash, name with backslash (defect target)
 *    - Zero/negative/MAX values for size, method, attributes
 *    - Null extra fields, empty extra fields, null rawName
 *    - Null ZipEntry argument
 * 
 * C. Defect-Targeted Branch Zone (COMPRESS-?? backslash workaround)
 *    - Names containing backslash characters should be normalized to forward slash
 *    - isDirectory() should recognize directory indicator even with backslash
 *    - The known defect: backslash not converted, causing assertion failures in
 *      ZipArchiveInputStreamTest and ZipFileTest
 * 
 * D. Exception & Defensive Guard Paths
 *    - setMethod with negative value -> IllegalArgumentException
 *    - setSize with negative value -> IllegalArgumentException
 *    - removeExtraField when extraFields is null -> NoSuchElementException
 *    - removeExtraField with non-existent type -> NoSuchElementException
 *    - removeUnparseableExtraFieldData when unparseableExtra is null -> NoSuchElementException
 *    - setExtra(byte[]) with invalid data -> RuntimeException (wrapping ZipException)
 *    - Constructor with null ZipEntry? (ZipEntry constructor may throw NullPointerException)
 * 
 * E. Object Lifecycle & Contract Integrity
 *    - equals: reflexive, symmetric, transitive, null, different class, field differences
 *    - hashCode: consistent with equals
 *    - clone: deep copy of extra fields, attributes
 * 
 * Coverage goals: 100% line and branch coverage of ZipArchiveEntry class.
 */
public class ZipArchiveEntryDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorString() {
        ZipArchiveEntry e = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", e.getName());
        assertFalse(e.isDirectory());
        assertEquals(-1, e.getMethod());
        assertEquals(ZipArchiveEntry.SIZE_UNKNOWN, e.getSize());
        assertEquals(0, e.getInternalAttributes());
        assertEquals(0, e.getExternalAttributes());
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, e.getPlatform());
    }

    @Test(timeout = 4000)
    public void testConstructorStringDirectory() {
        ZipArchiveEntry e = new ZipArchiveEntry("dir/");
        assertTrue(e.isDirectory());
        assertEquals("dir/", e.getName());
    }

    @Test(timeout = 4000)
    public void testConstructorZipEntry() throws ZipException {
        ZipEntry ze = new ZipEntry("entry.txt");
        ze.setMethod(ZipEntry.DEFLATED);
        ze.setSize(100);
        ze.setExtra(new byte[] {0,0,0,0}); // dummy extra
        ZipArchiveEntry e = new ZipArchiveEntry(ze);
        assertEquals("entry.txt", e.getName());
        assertEquals(ZipEntry.DEFLATED, e.getMethod());
        assertEquals(100, e.getSize());
        // extra fields should be parsed (empty in this case)
        assertNotNull(e.getExtraFields());
    }

    @Test(timeout = 4000)
    public void testConstructorZipEntryNullExtra() throws ZipException {
        ZipEntry ze = new ZipEntry("test");
        ze.setExtra(null);
        ZipArchiveEntry e = new ZipArchiveEntry(ze);
        assertEquals("test", e.getName());
        assertEquals(0, e.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testConstructorZipArchiveEntry() throws ZipException {
        ZipArchiveEntry original = new ZipArchiveEntry("orig");
        original.setInternalAttributes(1);
        original.setExternalAttributes(2);
        original.setUnixMode(0755);
        ZipArchiveEntry copy = new ZipArchiveEntry(original);
        assertEquals("orig", copy.getName());
        assertEquals(1, copy.getInternalAttributes());
        assertEquals(2, copy.getExternalAttributes());
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, copy.getPlatform());
    }

    @Test(timeout = 4000)
    public void testProtectedConstructor() {
        // Use anonymous subclass to access protected constructor
        ZipArchiveEntry e = new ZipArchiveEntry() {};
        assertEquals("", e.getName());
    }

    @Test(timeout = 4000)
    public void testConstructorFileAndName() {
        File f = new File("/tmp/dir");
        ZipArchiveEntry e = new ZipArchiveEntry(f, "entry");
        // f is not a real file, but we can still test logic
        // Since f.isDirectory() returns false (file doesn't exist), name stays "entry"
        assertEquals("entry", e.getName());
        assertFalse(e.isDirectory());
        // For a real directory file, name would get "/" appended
    }

    @Test(timeout = 4000)
    public void testConstructorFileAndNameDirectory() {
        // Simulate a directory by using a path that ends with separator? Not reliable.
        // We'll just test the logic: if inputFile.isDirectory() && !entryName.endsWith("/")
        // We can't easily create a File that isDirectory without a real file.
        // Instead, we test the branch by using a known directory (e.g., "/") on most systems.
        File dir = new File("/");
        if (dir.isDirectory()) {
            ZipArchiveEntry e = new ZipArchiveEntry(dir, "subdir");
            assertTrue(e.isDirectory());
            assertEquals("subdir/", e.getName());
        }
    }

    @Test(timeout = 4000)
    public void testSetGetMethod() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(ZipEntry.STORED);
        assertEquals(ZipEntry.STORED, e.getMethod());
        e.setMethod(8); // custom method
        assertEquals(8, e.getMethod());
    }

    @Test(timeout = 4000)
    public void testSetGetSize() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setSize(0);
        assertEquals(0, e.getSize());
        e.setSize(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, e.getSize());
    }

    @Test(timeout = 4000)
    public void testSetGetInternalAttributes() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setInternalAttributes(0xFFFF);
        assertEquals(0xFFFF, e.getInternalAttributes());
    }

    @Test(timeout = 4000)
    public void testSetGetExternalAttributes() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setExternalAttributes(0x12345678L);
        assertEquals(0x12345678L, e.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testSetGetPlatform() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, e.getPlatform());
        e.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, e.getPlatform());
    }

    @Test(timeout = 4000)
    public void testUnixMode() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, e.getPlatform());
        assertEquals(0755, e.getUnixMode());
        // Check external attributes: mode shifted left 16, plus MS-DOS flags
        long expected = (0755L << 16) | ((0755 & 0200) == 0 ? 1 : 0) | (e.isDirectory() ? 0x10 : 0);
        assertEquals(expected, e.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testUnixModeNonUnixPlatform() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(0, e.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testGetName() {
        ZipArchiveEntry e = new ZipArchiveEntry("name");
        assertEquals("name", e.getName());
        // When name is null, should fall back to super.getName()
        // But super.getName() returns empty string for ZipEntry with no name set?
        // Actually super.getName() returns the name passed to constructor.
        // We can test by creating via ZipEntry constructor and then setting name to null via reflection? Not possible.
        // Instead, we test that getName returns the overridden name.
    }

    @Test(timeout = 4000)
    public void testIsDirectory() {
        assertFalse(new ZipArchiveEntry("file").isDirectory());
        assertTrue(new ZipArchiveEntry("dir/").isDirectory());
        assertFalse(new ZipArchiveEntry("").isDirectory());
    }

    @Test(timeout = 4000)
    public void testSetName() {
        ZipArchiveEntry e = new ZipArchiveEntry("old");
        e.setName("new");
        assertEquals("new", e.getName());
    }

    @Test(timeout = 4000)
    public void testSetNameWithRawName() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        byte[] raw = new byte[] {0x61, 0x62};
        e.setName("new", raw);
        assertEquals("new", e.getName());
        assertArrayEquals(raw, e.getRawName());
    }

    @Test(timeout = 4000)
    public void testGetRawNameNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertNull(e.getRawName());
    }

    @Test(timeout = 4000)
    public void testGetRawNameCopy() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        byte[] raw = new byte[] {1,2,3};
        e.setName("test", raw);
        byte[] retrieved = e.getRawName();
        assertNotSame(raw, retrieved);
        assertArrayEquals(raw, retrieved);
    }

    @Test(timeout = 4000)
    public void testGeneralPurposeBit() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        GeneralPurposeBit gpb = e.getGeneralPurposeBit();
        assertNotNull(gpb);
        GeneralPurposeBit newGpb = new GeneralPurposeBit();
        newGpb.useUTF8ForNames(true);
        e.setGeneralPurposeBit(newGpb);
        assertTrue(e.getGeneralPurposeBit().usesUTF8ForNames());
    }

    @Test(timeout = 4000)
    public void testGetLastModifiedDate() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        long time = System.currentTimeMillis();
        e.setTime(time);
        Date d = e.getLastModifiedDate();
        assertEquals(new Date(time), d);
    }

    // ==================== Extra Fields ====================

    @Test(timeout = 4000)
    public void testExtraFieldsInitiallyEmpty() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertEquals(0, e.getExtraFields().length);
        assertEquals(0, e.getExtraFields(true).length);
        assertNull(e.getExtraField(new ZipShort(1)));
        assertNull(e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testSetExtraFields() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        JarMarker marker = JarMarker.getInstance();
        e.setExtraFields(new ZipExtraField[] {marker});
        assertEquals(1, e.getExtraFields().length);
        assertSame(marker, e.getExtraField(marker.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testSetExtraFieldsWithUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        e.setExtraFields(new ZipExtraField[] {unparseable});
        assertEquals(0, e.getExtraFields().length); // unparseable not in regular list
        assertEquals(1, e.getExtraFields(true).length);
        assertSame(unparseable, e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testAddExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        JarMarker marker = JarMarker.getInstance();
        e.addExtraField(marker);
        assertSame(marker, e.getExtraField(marker.getHeaderId()));
        // Adding same type replaces
        JarMarker marker2 = JarMarker.getInstance();
        e.addExtraField(marker2);
        assertSame(marker2, e.getExtraField(marker.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testAddExtraFieldUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.addExtraField(u);
        assertSame(u, e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        JarMarker marker1 = JarMarker.getInstance();
        JarMarker marker2 = JarMarker.getInstance(); // same header ID, will replace
        e.addExtraField(marker1);
        e.addAsFirstExtraField(marker2);
        // marker2 should be first in order
        ZipExtraField[] fields = e.getExtraFields();
        assertEquals(1, fields.length);
        assertSame(marker2, fields[0]);
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraFieldUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.addAsFirstExtraField(u);
        assertSame(u, e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testRemoveExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        JarMarker marker = JarMarker.getInstance();
        e.addExtraField(marker);
        e.removeExtraField(marker.getHeaderId());
        assertNull(e.getExtraField(marker.getHeaderId()));
        assertEquals(0, e.getExtraFields().length);
    }

    @Test(timeout = 4000)
    public void testRemoveExtraFieldThrowsWhenNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        try {
            e.removeExtraField(new ZipShort(1));
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveExtraFieldThrowsWhenNotPresent() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        JarMarker marker = JarMarker.getInstance();
        e.addExtraField(marker);
        try {
            e.removeExtraField(new ZipShort(0xCAFE));
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveUnparseableExtraFieldData() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.addExtraField(u);
        e.removeUnparseableExtraFieldData();
        assertNull(e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testRemoveUnparseableExtraFieldDataThrowsWhenNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        try {
            e.removeUnparseableExtraFieldData();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetExtraField() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertNull(e.getExtraField(new ZipShort(1)));
        JarMarker marker = JarMarker.getInstance();
        e.addExtraField(marker);
        assertSame(marker, e.getExtraField(marker.getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testGetUnparseableExtraFieldData() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertNull(e.getUnparseableExtraFieldData());
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.addExtraField(u);
        assertSame(u, e.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testSetExtraByteArray() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        // Create valid extra field bytes for JarMarker
        byte[] extra = JarMarker.getInstance().getHeaderId().getBytes();
        // Actually need full extra field: header id (2 bytes) + length (2 bytes) + data
        // JarMarker has no data, so length = 0
        byte[] validExtra = new byte[4];
        System.arraycopy(extra, 0, validExtra, 0, 2);
        validExtra[2] = 0;
        validExtra[3] = 0;
        e.setExtra(validExtra);
        assertNotNull(e.getExtraField(JarMarker.getInstance().getHeaderId()));
    }

    @Test(timeout = 4000)
    public void testSetExtraByteArrayInvalid() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        // Invalid extra data (e.g., truncated) should throw RuntimeException
        byte[] invalid = new byte[] {0x01}; // too short
        try {
            e.setExtra(invalid);
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof ZipException);
        }
    }

    @Test(timeout = 4000)
    public void testSetCentralDirectoryExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        byte[] centralExtra = new byte[4]; // dummy valid extra
        centralExtra[0] = 0; centralExtra[1] = 0; // header id 0
        centralExtra[2] = 0; centralExtra[3] = 0; // length 0
        e.setCentralDirectoryExtra(centralExtra);
        // Should have parsed and added an extra field with header id 0
        assertNotNull(e.getExtraField(new ZipShort(0)));
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertArrayEquals(new byte[0], e.getLocalFileDataExtra());
        // After setting extra, should return non-empty
        e.setExtra(new byte[] {0,0,0,0});
        assertTrue(e.getLocalFileDataExtra().length > 0);
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryExtra() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        byte[] central = e.getCentralDirectoryExtra();
        assertNotNull(central);
        // Initially empty
        assertEquals(0, central.length);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNameNull() {
        // setName allows null? The method is protected and sets this.name = name.
        // getName returns name if not null, else super.getName().
        // We can test by creating a ZipArchiveEntry via ZipEntry constructor and then
        // setting name to null via reflection? Not easily. Instead, we test that
        // when name is set to null, getName falls back to super.getName().
        // We'll use a subclass to access protected setName.
        ZipArchiveEntry e = new ZipArchiveEntry("original") {};
        e.setName(null);
        // super.getName() returns "original" because ZipEntry stores the name.
        assertEquals("original", e.getName());
    }

    @Test(timeout = 4000)
    public void testNameEmpty() {
        ZipArchiveEntry e = new ZipArchiveEntry("");
        assertEquals("", e.getName());
        assertFalse(e.isDirectory());
    }

    @Test(timeout = 4000)
    public void testNameTrailingSlash() {
        ZipArchiveEntry e = new ZipArchiveEntry("dir/");
        assertTrue(e.isDirectory());
    }

    @Test(timeout = 4000)
    public void testSizeNegative() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        try {
            e.setSize(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSizeZero() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setSize(0);
        assertEquals(0, e.getSize());
    }

    @Test(timeout = 4000)
    public void testMethodNegative() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        try {
            e.setMethod(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMethodZero() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(0);
        assertEquals(0, e.getMethod());
    }

    @Test(timeout = 4000)
    public void testExtraFieldsNullInitial() {
        // getExtraFields when extraFields is null and includeUnparseable false
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        // extraFields is null initially
        assertEquals(0, e.getExtraFields(false).length);
        // includeUnparseable true but unparseableExtra null
        assertEquals(0, e.getExtraFields(true).length);
    }

    @Test(timeout = 4000)
    public void testExtraFieldsNullWithUnparseable() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        UnparseableExtraFieldData u = new UnparseableExtraFieldData();
        e.addExtraField(u);
        // Now extraFields is still null? Actually addExtraField for UnparseableExtraFieldData sets unparseableExtra,
        // but does not create extraFields map. So extraFields remains null.
        assertEquals(0, e.getExtraFields(false).length);
        assertEquals(1, e.getExtraFields(true).length);
    }

    // ==================== Partition C: Defect-Targeted (Backslash Workaround) ====================

    @Test(timeout = 4000)
    public void testBackslashInNameShouldBeNormalized() {
        // Known defect: backslash in entry name is not converted to forward slash.
        // According to ZIP specification, forward slash is the path separator.
        // The correct behavior is to replace backslashes with forward slashes.
        ZipArchiveEntry e = new ZipArchiveEntry("a\\b\\c.txt");
        // Expected: "a/b/c.txt"
        assertEquals("a/b/c.txt", e.getName());
        // Also test directory detection: "dir\\" should be considered directory if normalized to "dir/"
        ZipArchiveEntry dir = new ZipArchiveEntry("dir\\");
        assertTrue(dir.isDirectory());
    }

    @Test(timeout = 4000)
    public void testBackslashInNameWithUnicode() {
        // Specific failure from defect: "ä\ü.txt" expected "ä/ü.txt"
        ZipArchiveEntry e = new ZipArchiveEntry("ä\\ü.txt");
        assertEquals("ä/ü.txt", e.getName());
    }

    @Test(timeout = 4000)
    public void testBackslashInNameViaZipEntryConstructor() throws ZipException {
        // Simulate reading from a ZipEntry that has backslash in name
        ZipEntry ze = new ZipEntry("dir\\file.txt");
        ZipArchiveEntry e = new ZipArchiveEntry(ze);
        // The constructor calls setName(entry.getName()), which should normalize
        assertEquals("dir/file.txt", e.getName());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetMethodNegativeThrows() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setMethod(-1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSizeNegativeThrows() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.setSize(-1);
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testRemoveExtraFieldNullMapThrows() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.removeExtraField(new ZipShort(1));
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testRemoveUnparseableExtraFieldDataNullThrows() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        e.removeUnparseableExtraFieldData();
    }

    @Test(timeout = 4000)
    public void testSetExtraInvalidDataThrowsRuntimeException() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        try {
            e.setExtra(new byte[] {0x01});
            fail("Expected RuntimeException");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof ZipException);
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testClone() throws Exception {
        ZipArchiveEntry original = new ZipArchiveEntry("original");
        original.setMethod(ZipEntry.DEFLATED);
        original.setSize(100);
        original.setInternalAttributes(1);
        original.setExternalAttributes(2);
        original.setUnixMode(0755);
        JarMarker marker = JarMarker.getInstance();
        original.addExtraField(marker);
        ZipArchiveEntry clone = (ZipArchiveEntry) original.clone();
        assertEquals(original.getName(), clone.getName());
        assertEquals(original.getMethod(), clone.getMethod());
        assertEquals(original.getSize(), clone.getSize());
        assertEquals(original.getInternalAttributes(), clone.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), clone.getExternalAttributes());
        assertEquals(original.getPlatform(), clone.getPlatform());
        assertEquals(original.getUnixMode(), clone.getUnixMode());
        // Extra fields should be independent copies
        assertNotSame(original.getExtraFields(), clone.getExtraFields());
        assertNotNull(clone.getExtraField(marker.getHeaderId()));
        // Modify clone, original unchanged
        clone.setName("clone");
        assertEquals("original", original.getName());
    }

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertTrue(e.equals(e));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertFalse(e.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ZipArchiveEntry e = new ZipArchiveEntry("test");
        assertFalse(e.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        assertTrue(e1.equals(e2));
        assertTrue(e2.equals(e1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentName() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("a");
        ZipArchiveEntry e2 = new ZipArchiveEntry("b");
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsNameNull() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("a") {};
        e1.setName(null);
        ZipArchiveEntry e2 = new ZipArchiveEntry("a");
        assertFalse(e1.equals(e2));
        // Both null
        ZipArchiveEntry e3 = new ZipArchiveEntry("a") {};
        e3.setName(null);
        assertTrue(e1.equals(e3));
    }

    @Test(timeout = 4000)
    public void testEqualsComment() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setComment("comment1");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setComment("comment2");
        assertFalse(e1.equals(e2));
        // Both null
        ZipArchiveEntry e3 = new ZipArchiveEntry("test");
        assertTrue(e1.equals(e1)); // same object
        // e1 vs e3: e3 has null comment, e1 has "comment1"
        assertFalse(e1.equals(e3));
    }

    @Test(timeout = 4000)
    public void testEqualsTime() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setTime(1000);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setTime(2000);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsInternalAttributes() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setInternalAttributes(1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setInternalAttributes(2);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsPlatform() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsExternalAttributes() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setExternalAttributes(1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setExternalAttributes(2);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsMethod() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setMethod(ZipEntry.STORED);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setMethod(ZipEntry.DEFLATED);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsSize() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setSize(100);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setSize(200);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsCrc() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setCrc(1);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setCrc(2);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsCompressedSize() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        e1.setCompressedSize(100);
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        e2.setCompressedSize(200);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsCentralDirectoryExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        // Both have empty central directory extra initially, so equal
        assertTrue(e1.equals(e2));
        // Modify one's extra fields
        e1.setCentralDirectoryExtra(new byte[] {0,0,0,0});
        e2.setCentralDirectoryExtra(new byte[] {0,0,0,1}); // different
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsLocalFileDataExtra() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        // Both have empty local extra initially
        assertTrue(e1.equals(e2));
        e1.setExtra(new byte[] {0,0,0,0});
        e2.setExtra(new byte[] {0,0,0,1});
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testEqualsGeneralPurposeBit() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        assertTrue(e1.equals(e2));
        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        e1.setGeneralPurposeBit(gpb);
        assertFalse(e1.equals(e2));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        assertEquals(e1.hashCode(), e2.hashCode());
        // Different name -> different hash
        ZipArchiveEntry e3 = new ZipArchiveEntry("other");
        assertNotEquals(e1.hashCode(), e3.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistentWithEquals() {
        ZipArchiveEntry e1 = new ZipArchiveEntry("test");
        ZipArchiveEntry e2 = new ZipArchiveEntry("test");
        assertTrue(e1.equals(e2));
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}