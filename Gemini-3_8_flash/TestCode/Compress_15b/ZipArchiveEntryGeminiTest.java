/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.compress.archivers.zip.ZipArchiveEntry
 *
 * Targeted Defects & Branches:
 * 1. DEFECT: testNullCommentEqualsEmptyComment (COMPRESS defect)
 *    - In equals(Object): when this.getComment() is null and other.getComment() is "" (or vice versa),
 *      the defect causes equals() to return false rather than normalizing null and empty comments.
 * 2. Constructors:
 *    - ZipArchiveEntry(String): FAT platform slash conversion with backslashes vs forward slashes.
 *    - ZipArchiveEntry(java.util.zip.ZipEntry): entry with extra data vs entry with null extra.
 *    - ZipArchiveEntry(ZipArchiveEntry): deep-copying extra fields, internal/external attributes.
 *    - ZipArchiveEntry(File, String): Directory vs regular file, trailing slash normalization.
 *    - ZipArchiveEntry(): Protected zero-arg constructor producing entry with empty name.
 * 3. Method & Size Constraints:
 *    - setMethod: valid values (>= 0), invalid negative values (< 0 throws IllegalArgumentException).
 *    - setSize: valid sizes (0, positive, Zip64 values), negative size throws IllegalArgumentException.
 * 4. Permissions, Platform & Attributes:
 *    - setUnixMode: directory flag bitwise OR, read-only bitwise OR, shift logic.
 *    - getUnixMode: FAT platform returns 0, UNIX platform returns shifted permissions.
 *    - setPlatform / getPlatform.
 *    - setInternalAttributes / getInternalAttributes, setExternalAttributes / getExternalAttributes.
 * 5. Extra Fields Management:
 *    - setExtraFields, getExtraFields() vs getExtraFields(includeUnparseable).
 *    - addExtraField / addAsFirstExtraField: UnparseableExtraFieldData branch vs standard ZipExtraField.
 *    - addAsFirstExtraField: prepending when map is empty vs non-empty; replacing existing key.
 *    - removeExtraField: key present vs key missing (NoSuchElementException), null map (NoSuchElementException).
 *    - removeUnparseableExtraFieldData: unparseable present vs null (NoSuchElementException).
 *    - getExtraField: field exists vs field missing vs extraFields is null.
 *    - setExtra(byte[]): local extra parsing and mergeExtraFields(local=true).
 *    - setCentralDirectoryExtra(byte[]): central extra parsing and mergeExtraFields(local=false).
 *    - mergeExtraFields: merging into empty map vs existing fields update via parseFromLocal/Central.
 * 6. Raw Name & GeneralPurposeBit:
 *    - setName(String, byte[]) & getRawName() array copy immutability.
 *    - GeneralPurposeBit: getGeneralPurposeBit / setGeneralPurposeBit.
 * 7. Object Contracts:
 *    - equals: reflexive, null, non-ZipArchiveEntry class, different name, null name branches,
 *      different time, internalAttributes, platform, externalAttributes, method, size, crc,
 *      compressedSize, localFileDataExtra, centralDirectoryExtra, generalPurposeBit.
 *    - hashCode: contract matching getName().hashCode().
 *    - clone: independent copy verification.
 * -------------------------------------------------------------------------------------------------------
 */
public class ZipArchiveEntryGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the defect where null comment and empty comment fail equality comparison.
     * In the defective version, entry1.equals(entry2) returns false when comments differ
     * between null and empty string "", causing assertion failure: expected:<foo> but was:<foo>.
     */
    @Test(timeout = 4000)
    public void testNullCommentEqualsEmptyComment() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("foo");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("foo");
        entry1.setComment(null);
        entry2.setComment("");

        assertEquals("entry1 (null comment) should equal entry2 (empty comment)", entry1, entry2);
        assertEquals("entry2 (empty comment) should equal entry1 (null comment)", entry2, entry1);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicNameAndDirectoryResolution() {
        ZipArchiveEntry fileEntry = new ZipArchiveEntry("folder/file.txt");
        assertFalse(fileEntry.isDirectory());
        assertEquals("folder/file.txt", fileEntry.getName());

        ZipArchiveEntry dirEntry = new ZipArchiveEntry("folder/sub/");
        assertTrue(dirEntry.isDirectory());
        assertEquals("folder/sub/", dirEntry.getName());
    }

    @Test(timeout = 4000)
    public void testConstructorFromJavaZipEntryWithExtra() throws Exception {
        java.util.zip.ZipEntry standardEntry = new java.util.zip.ZipEntry("entry.bin");
        standardEntry.setMethod(java.util.zip.ZipEntry.DEFLATED);
        standardEntry.setSize(2048L);

        // Header ID: 0x0001 (Zip64), Length: 0
        byte[] dummyExtra = new byte[] { 0x01, 0x00, 0x00, 0x00 };
        standardEntry.setExtra(dummyExtra);

        ZipArchiveEntry archiveEntry = new ZipArchiveEntry(standardEntry);
        assertEquals("entry.bin", archiveEntry.getName());
        assertEquals(java.util.zip.ZipEntry.DEFLATED, archiveEntry.getMethod());
        assertEquals(2048L, archiveEntry.getSize());
        assertNotNull(archiveEntry.getExtraFields());
        assertTrue(archiveEntry.getExtraFields().length > 0);
    }

    @Test(timeout = 4000)
    public void testConstructorFromJavaZipEntryWithoutExtra() throws Exception {
        java.util.zip.ZipEntry standardEntry = new java.util.zip.ZipEntry("noExtra.bin");
        standardEntry.setMethod(java.util.zip.ZipEntry.STORED);
        standardEntry.setSize(0L);

        ZipArchiveEntry archiveEntry = new ZipArchiveEntry(standardEntry);
        assertEquals(0, archiveEntry.getExtraFields().length);
        assertEquals(java.util.zip.ZipEntry.STORED, archiveEntry.getMethod());
    }

    @Test(timeout = 4000)
    public void testCopyConstructorFromZipArchiveEntry() throws Exception {
        ZipArchiveEntry source = new ZipArchiveEntry("src.txt");
        source.setInternalAttributes(42);
        source.setExternalAttributes(0x81A40000L);
        UnrecognizedExtraField extra = new UnrecognizedExtraField();
        extra.setHeaderId(new ZipShort(0x1234));
        extra.setLocalFileDataData(new byte[] { 1, 2, 3 });
        source.addExtraField(extra);

        ZipArchiveEntry copy = new ZipArchiveEntry(source);
        assertEquals(source.getName(), copy.getName());
        assertEquals(42, copy.getInternalAttributes());
        assertEquals(0x81A40000L, copy.getExternalAttributes());
        assertNotNull(copy.getExtraField(new ZipShort(0x1234)));
    }

    @Test(timeout = 4000)
    public void testFileConstructorForDirectory() throws IOException {
        File tempDir = File.createTempFile("zip_test_dir", "");
        assertTrue(tempDir.delete());
        assertTrue(tempDir.mkdir());
        try {
            // entryName without trailing slash should have it appended for directory
            ZipArchiveEntry entry = new ZipArchiveEntry(tempDir, "directoryName");
            assertEquals("directoryName/", entry.getName());
            assertTrue(entry.isDirectory());

            // entryName already ending with slash
            ZipArchiveEntry entry2 = new ZipArchiveEntry(tempDir, "directoryName2/");
            assertEquals("directoryName2/", entry2.getName());
            assertTrue(entry2.isDirectory());
        } finally {
            tempDir.delete();
        }
    }

    @Test(timeout = 4000)
    public void testFileConstructorForRegularFile() throws IOException {
        File tempFile = File.createTempFile("zip_test_file", ".tmp");
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(new byte[] { 10, 20, 30, 40 });
            fos.close();

            ZipArchiveEntry entry = new ZipArchiveEntry(tempFile, "fileName.txt");
            assertEquals("fileName.txt", entry.getName());
            assertFalse(entry.isDirectory());
            assertEquals(4L, entry.getSize());
            assertEquals(tempFile.lastModified(), entry.getTime());
        } finally {
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testUnixModeAndPlatform() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test.sh");
        assertEquals(ZipArchiveEntry.PLATFORM_FAT, entry.getPlatform());
        assertEquals(0, entry.getUnixMode());

        // Set unix permissions 0755: rwxr-xr-x
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());

        // Non-writable directory check for MS-DOS flags
        ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        dirEntry.setUnixMode(0555); // read-only directory
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, dirEntry.getPlatform());
        assertEquals(0555, dirEntry.getUnixMode());
        // Verify MS-DOS directory flag (0x10) and read-only flag (1)
        long ext = dirEntry.getExternalAttributes();
        assertEquals(1, ext & 1); // read-only bit
        assertEquals(0x10, ext & 0x10); // directory flag

        // Reverting platform to FAT makes getUnixMode return 0
        dirEntry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertEquals(0, dirEntry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testInternalAndExternalAttributes() {
        ZipArchiveEntry entry = new ZipArchiveEntry("attr.bin");
        entry.setInternalAttributes(123);
        assertEquals(123, entry.getInternalAttributes());

        entry.setExternalAttributes(0xFFFFFFFFL);
        assertEquals(0xFFFFFFFFL, entry.getExternalAttributes());
    }

    @Test(timeout = 4000)
    public void testMethodGetterAndSetter() {
        ZipArchiveEntry entry = new ZipArchiveEntry("method.bin");
        assertEquals(-1, entry.getMethod());

        entry.setMethod(8);
        assertEquals(8, entry.getMethod());

        entry.setMethod(0);
        assertEquals(0, entry.getMethod());

        entry.setMethod(99);
        assertEquals(99, entry.getMethod());
    }

    @Test(timeout = 4000)
    public void testSizeGetterAndSetter() {
        ZipArchiveEntry entry = new ZipArchiveEntry("size.bin");
        entry.setSize(0L);
        assertEquals(0L, entry.getSize());

        entry.setSize(0x100000000L); // > 4GB Zip64 size
        assertEquals(0x100000000L, entry.getSize());
    }

    @Test(timeout = 4000)
    public void testLastModifiedDate() {
        ZipArchiveEntry entry = new ZipArchiveEntry("date.bin");
        long now = 1600000000000L;
        entry.setTime(now);
        assertEquals(new Date(now), entry.getLastModifiedDate());
    }

    @Test(timeout = 4000)
    public void testGeneralPurposeBit() {
        ZipArchiveEntry entry = new ZipArchiveEntry("gpb.bin");
        assertNotNull(entry.getGeneralPurposeBit());

        GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
        assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFatPlatformBackslashReplacement() {
        // FAT platform replaces backslashes if no forward slash is present
        ZipArchiveEntry entry = new ZipArchiveEntry("folder\\sub\\file.txt");
        assertEquals("folder/sub/file.txt", entry.getName());

        // If a forward slash is already present, backslash is not replaced in constructor
        ZipArchiveEntry mixed = new ZipArchiveEntry("folder/sub\\file.txt");
        assertEquals("folder/sub\\file.txt", mixed.getName());
    }

    @Test(timeout = 4000)
    public void testRawNameAndSetName() {
        ZipArchiveEntry entry = new ZipArchiveEntry("initial.txt");
        assertNull(entry.getRawName());

        byte[] raw = new byte[] { 'r', 'a', 'w' };
        entry.setName("updated.txt", raw);
        assertEquals("updated.txt", entry.getName());

        byte[] returnedRaw = entry.getRawName();
        assertArrayEquals(raw, returnedRaw);

        // Immutability check: modifying returned raw bytes should not affect internal storage
        returnedRaw[0] = 'x';
        assertArrayEquals(new byte[] { 'r', 'a', 'w' }, entry.getRawName());
    }

    @Test(timeout = 4000)
    public void testProtectedDefaultConstructor() {
        class CustomArchiveEntry extends ZipArchiveEntry {
            CustomArchiveEntry() {
                super();
            }
        }
        CustomArchiveEntry entry = new CustomArchiveEntry();
        assertEquals("", entry.getName());
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
    public void testRemoveExtraFieldFromEmptyEntryThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeExtraField(new ZipShort(0x1234));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveNonExistentExtraFieldThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(0x1111));
        entry.addExtraField(field);

        entry.removeExtraField(new ZipShort(0x2222));
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testRemoveUnparseableExtraFieldDataWhenNonePresentThrowsException() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        entry.removeUnparseableExtraFieldData();
    }

    // =========================================================================
    // Partition E: Extra Fields Manipulation & Merging
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndRemoveExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        ZipShort id1 = new ZipShort(0x1111);
        ZipShort id2 = new ZipShort(0x2222);

        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(id1);
        f1.setLocalFileDataData(new byte[] { 1 });

        UnrecognizedExtraField f2 = new UnrecognizedExtraField();
        f2.setHeaderId(id2);
        f2.setLocalFileDataData(new byte[] { 2 });

        entry.addExtraField(f1);
        entry.addExtraField(f2);
        assertEquals(2, entry.getExtraFields().length);
        assertEquals(f1, entry.getExtraField(id1));
        assertEquals(f2, entry.getExtraField(id2));

        entry.removeExtraField(id1);
        assertEquals(1, entry.getExtraFields().length);
        assertNull(entry.getExtraField(id1));
        assertEquals(f2, entry.getExtraField(id2));
    }

    @Test(timeout = 4000)
    public void testAddAsFirstExtraField() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        ZipShort id1 = new ZipShort(0x1111);
        ZipShort id2 = new ZipShort(0x2222);

        UnrecognizedExtraField f1 = new UnrecognizedExtraField();
        f1.setHeaderId(id1);
        f1.setLocalFileDataData(new byte[] { 1 });

        UnrecognizedExtraField f2 = new UnrecognizedExtraField();
        f2.setHeaderId(id2);
        f2.setLocalFileDataData(new byte[] { 2 });

        // Add f1, then add f2 as first
        entry.addExtraField(f1);
        entry.addAsFirstExtraField(f2);

        ZipExtraField[] fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(id2, fields[0].getHeaderId());
        assertEquals(id1, fields[1].getHeaderId());

        // Replace f1 with addAsFirstExtraField
        UnrecognizedExtraField f1New = new UnrecognizedExtraField();
        f1New.setHeaderId(id1);
        f1New.setLocalFileDataData(new byte[] { 99 });
        entry.addAsFirstExtraField(f1New);

        fields = entry.getExtraFields();
        assertEquals(2, fields.length);
        assertEquals(id1, fields[0].getHeaderId());
        assertEquals(id2, fields[1].getHeaderId());
    }

    @Test(timeout = 4000)
    public void testUnparseableExtraFieldDataHandling() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[] { 1, 2, 3 }, 0, 3);

        assertNull(entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields(true).length);

        entry.addExtraField(unparseable);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields(false).length);
        assertEquals(1, entry.getExtraFields(true).length);

        // addAsFirst with unparseable
        UnparseableExtraFieldData unparseable2 = new UnparseableExtraFieldData();
        unparseable2.parseFromLocalFileData(new byte[] { 4, 5 }, 0, 2);
        entry.addAsFirstExtraField(unparseable2);
        assertSame(unparseable2, entry.getUnparseableExtraFieldData());

        entry.removeUnparseableExtraFieldData();
        assertNull(entry.getUnparseableExtraFieldData());
        assertEquals(0, entry.getExtraFields(true).length);
    }

    @Test(timeout = 4000)
    public void testSetExtraFieldsWithUnparseable() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        UnrecognizedExtraField field1 = new UnrecognizedExtraField();
        field1.setHeaderId(new ZipShort(0x1234));
        field1.setLocalFileDataData(new byte[] { 1 });

        UnparseableExtraFieldData unparseable = new UnparseableExtraFieldData();
        unparseable.parseFromLocalFileData(new byte[] { 9 }, 0, 1);

        entry.setExtraFields(new ZipExtraField[] { field1, unparseable });
        assertEquals(1, entry.getExtraFields(false).length);
        assertEquals(2, entry.getExtraFields(true).length);
        assertSame(unparseable, entry.getUnparseableExtraFieldData());
    }

    @Test(timeout = 4000)
    public void testSetExtraAndMergeExtraFields() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        // Extra field with HeaderId = 0x1234, length = 2, data = {0x0A, 0x0B}
        byte[] extraBytes = new byte[] { 0x34, 0x12, 0x02, 0x00, 0x0A, 0x0B };
        entry.setExtra(extraBytes);

        ZipExtraField field = entry.getExtraField(new ZipShort(0x1234));
        assertNotNull(field);
        assertArrayEquals(new byte[] { 0x0A, 0x0B }, field.getLocalFileDataData());

        // Merge updated local data with different payload {0x0C, 0x0D}
        byte[] extraBytesUpdate = new byte[] { 0x34, 0x12, 0x02, 0x00, 0x0C, 0x0D };
        entry.setExtra(extraBytesUpdate);
        assertArrayEquals(new byte[] { 0x0C, 0x0D }, field.getLocalFileDataData());

        // Merging central directory extra
        byte[] centralBytes = new byte[] { 0x34, 0x12, 0x01, 0x00, (byte) 0xEE };
        entry.setCentralDirectoryExtra(centralBytes);
        assertArrayEquals(new byte[] { (byte) 0xEE }, field.getCentralDirectoryData());
    }

    @Test(timeout = 4000)
    public void testLocalAndCentralDirectoryExtraOutput() {
        ZipArchiveEntry entry = new ZipArchiveEntry("test");
        byte[] localExtra = entry.getLocalFileDataExtra();
        assertNotNull(localExtra);
        assertEquals(0, localExtra.length);

        byte[] centralExtra = entry.getCentralDirectoryExtra();
        assertNotNull(centralExtra);
        assertEquals(0, centralExtra.length);

        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(0xCAFE));
        field.setLocalFileDataData(new byte[] { 1, 2 });
        field.setCentralDirectoryData(new byte[] { 1, 2, 3 });
        entry.addExtraField(field);

        assertTrue(entry.getLocalFileDataExtra().length > 0);
        assertTrue(entry.getCentralDirectoryExtra().length > 0);
    }

    // =========================================================================
    // Partition F: Object Lifecycle & Contract Integrity (Equals, HashCode, Clone)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneIntegrity() {
        ZipArchiveEntry original = new ZipArchiveEntry("cloneTarget");
        original.setMethod(8);
        original.setSize(1024L);
        original.setInternalAttributes(10);
        original.setExternalAttributes(20L);
        UnrecognizedExtraField field = new UnrecognizedExtraField();
        field.setHeaderId(new ZipShort(0x5678));
        field.setLocalFileDataData(new byte[] { 1 });
        original.addExtraField(field);

        ZipArchiveEntry clone = (ZipArchiveEntry) original.clone();
        assertNotSame(original, clone);
        assertEquals(original, clone);
        assertEquals(original.getInternalAttributes(), clone.getInternalAttributes());
        assertEquals(original.getExternalAttributes(), clone.getExternalAttributes());
        assertNotNull(clone.getExtraField(new ZipShort(0x5678)));

        // Modifying clone's extra fields shouldn't affect original
        clone.removeExtraField(new ZipShort(0x5678));
        assertNotNull(original.getExtraField(new ZipShort(0x5678)));
        assertNull(clone.getExtraField(new ZipShort(0x5678)));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("entryHashCode");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("entryHashCode");
        assertEquals(entry1.hashCode(), entry2.hashCode());
        assertEquals("entryHashCode".hashCode(), entry1.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsComprehensiveBranches() {
        ZipArchiveEntry entry1 = new ZipArchiveEntry("test");
        ZipArchiveEntry entry2 = new ZipArchiveEntry("test");

        // Reflexive
        assertTrue(entry1.equals(entry1));

        // Null and non-ZipArchiveEntry
        assertFalse(entry1.equals(null));
        assertFalse(entry1.equals("NotAZipArchiveEntry"));
        assertFalse(entry1.equals(new java.util.zip.ZipEntry("test")));

        // Equal states
        assertTrue(entry1.equals(entry2));

        // Different Name
        ZipArchiveEntry differentName = new ZipArchiveEntry("different");
        assertFalse(entry1.equals(differentName));

        // Different Comments
        entry1.setComment("CommentA");
        entry2.setComment("CommentB");
        assertFalse(entry1.equals(entry2));
        entry2.setComment("CommentA");
        assertTrue(entry1.equals(entry2));

        // Different Time
        entry1.setTime(1000L);
        entry2.setTime(2000L);
        assertFalse(entry1.equals(entry2));
        entry2.setTime(1000L);
        assertTrue(entry1.equals(entry2));

        // Different Internal Attributes
        entry1.setInternalAttributes(1);
        entry2.setInternalAttributes(2);
        assertFalse(entry1.equals(entry2));
        entry2.setInternalAttributes(1);
        assertTrue(entry1.equals(entry2));

        // Different Platform
        entry1.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry2.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertFalse(entry1.equals(entry2));
        entry2.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        assertTrue(entry1.equals(entry2));

        // Different External Attributes
        entry1.setExternalAttributes(100L);
        entry2.setExternalAttributes(200L);
        assertFalse(entry1.equals(entry2));
        entry2.setExternalAttributes(100L);
        assertTrue(entry1.equals(entry2));

        // Different Method
        entry1.setMethod(0);
        entry2.setMethod(8);
        assertFalse(entry1.equals(entry2));
        entry2.setMethod(0);
        assertTrue(entry1.equals(entry2));

        // Different Size
        entry1.setSize(100L);
        entry2.setSize(200L);
        assertFalse(entry1.equals(entry2));
        entry2.setSize(100L);
        assertTrue(entry1.equals(entry2));

        // Different CRC
        entry1.setCrc(111L);
        entry2.setCrc(222L);
        assertFalse(entry1.equals(entry2));
        entry2.setCrc(111L);
        assertTrue(entry1.equals(entry2));

        // Different Compressed Size
        entry1.setCompressedSize(50L);
        entry2.setCompressedSize(60L);
        assertFalse(entry1.equals(entry2));
        entry2.setCompressedSize(50L);
        assertTrue(entry1.equals(entry2));

        // Different Extra Fields (Central / Local)
        UnrecognizedExtraField ef1 = new UnrecognizedExtraField();
        ef1.setHeaderId(new ZipShort(0x1234));
        ef1.setLocalFileDataData(new byte[] { 1 });
        entry1.addExtraField(ef1);
        assertFalse(entry1.equals(entry2));
        entry2.addExtraField(ef1);
        assertTrue(entry1.equals(entry2));

        // Different GeneralPurposeBit
        GeneralPurposeBit gpb1 = new GeneralPurposeBit();
        gpb1.useEncryption(true);
        entry1.setGeneralPurposeBit(gpb1);
        assertFalse(entry1.equals(entry2));
        GeneralPurposeBit gpb2 = new GeneralPurposeBit();
        gpb2.useEncryption(true);
        entry2.setGeneralPurposeBit(gpb2);
        assertTrue(entry1.equals(entry2));
    }
}