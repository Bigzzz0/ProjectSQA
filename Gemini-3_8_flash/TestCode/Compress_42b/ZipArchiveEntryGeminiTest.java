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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: ZipArchiveEntry
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - Defect: isUnixSymlink() evaluates (getUnixMode() & UnixStat.LINK_FLAG) == UnixStat.LINK_FLAG.
 *      Because LINK_FLAG (0120000) shares bits with other flags, if multiple flags are set
 *      (e.g., LINK_FLAG | DIR_FLAG = 0160000), the bitwise AND still yields LINK_FLAG and incorrectly
 *      evaluates to true.
 *    - Required Behavior: isUnixSymlink() MUST evaluate (getUnixMode() & UnixStat.FILE_TYPE_FLAG) == LINK_FLAG.
 *      When more than one file type flag is set, it MUST return false.
 *    - Targeted Test: isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet()
 *
 * 2. EQUIVALENCE PARTITIONS & DECISION BRANCHES:
 *    - Constructors:
 *      * ZipArchiveEntry(String): directory ("/"), non-directory, FAT slash normalization.
 *      * ZipArchiveEntry(java.util.zip.ZipEntry): extra != null vs extra == null.
 *      * ZipArchiveEntry(ZipArchiveEntry): deep copy of GPB (null vs non-null), extra fields, attributes.
 *      * ZipArchiveEntry(File, String): isDirectory without slash -> appends slash; isFile -> sets size.
 *    - Extra Field Management:
 *      * addExtraField(): null extraFields array, add new, replace existing header ID, Unparseable instance.
 *      * addAsFirstExtraField(): null array, prepend, replace existing and move to index 0, Unparseable instance.
 *      * removeExtraField(): null array -> NoSuchElementException, found -> removed, not found -> NoSuchElementException.
 *      * removeUnparseableExtraFieldData(): null -> NoSuchElementException, non-null -> cleared.
 *      * getExtraFields(boolean): includeUnparseable true/false with/without unparseable data.
 *      * mergeExtraFields(): local vs central directory data, existing vs new extra fields.
 *    - Attributes & Modes:
 *      * setUnixMode() / getUnixMode(): FAT platform (always 0) vs UNIX platform; read-only bit, directory bit.
 *      * setSize() / getSize(): size >= 0 vs size < 0 (IllegalArgumentException).
 *      * setMethod() / getMethod(): valid method vs negative (IllegalArgumentException).
 *      * setName(String): FAT platform replaces '\' with '/' only if no '/' already exists; UNIX leaves '\'.
 *      * getRawName(): null vs non-null defensive copy.
 *    - Contracts & Lifecycle:
 *      * equals(): identity, null, class mismatch, name (null/non-null), comments, attributes, crc, size,
 *                  local/central extra equality, GPB equality.
 *      * hashCode(): name-based hashcode.
 *      * clone(): deep copy integrity verification.
 * =========================================================================
 */
public class ZipArchiveEntryGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesGettersSetters() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("test.txt");
        assertEquals("test.txt", entry.getName());
        assertFalse(entry.isDirectory());

        entry.setInternalAttributes(12);
        assertEquals(12, entry.getInternalAttributes());

        entry.setExternalAttributes(0x81A40000L);
        assertEquals(0x81A40000L, entry.getExternalAttributes());

        entry.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());

        entry.setMethod(ZipMethod.DEFLATED.getCode());
        assertEquals(ZipMethod.DEFLATED.getCode(), entry.getMethod());

        entry.setSize(1024L);
        assertEquals(1024L, entry.getSize());

        entry.setVersionMadeBy(45);
        assertEquals(45, entry.getVersionMadeBy());

        entry.setVersionRequired(20);
        assertEquals(20, entry.getVersionRequired());

        entry.setRawFlag(8);
        assertEquals(8, entry.getRawFlag());

        final GeneralPurposeBit gpb = new GeneralPurposeBit();
        gpb.useUTF8ForNames(true);
        entry.setGeneralPurposeBit(gpb);
        assertSame(gpb, entry.getGeneralPurposeBit());
        assertTrue(entry.getGeneralPurposeBit().usesUTF8ForNames());
    }

    @Test(timeout = 4000)
    public void testDirectoryDetection() {
        final ZipArchiveEntry dirEntry = new ZipArchiveEntry("folder/");
        assertTrue(dirEntry.isDirectory());

        final ZipArchiveEntry fileEntry = new ZipArchiveEntry("folder");
        assertFalse(fileEntry.isDirectory());
    }

    @Test(timeout = 4000)
    public void testLastModifiedDate() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("date.txt");
        final long fixedTime = 1580000000000L;
        entry.setTime(fixedTime);
        final Date date = entry.getLastModifiedDate();
        assertNotNull(date);
        assertEquals(entry.getTime(), date.getTime());
    }

    @Test(timeout = 4000)
    public void testUnixModeCalculation() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("script.sh");
        // Set mode 0755: writable by owner, non-directory
        entry.setUnixMode(0755);
        assertEquals(ZipArchiveEntry.PLATFORM_UNIX, entry.getPlatform());
        assertEquals(0755, entry.getUnixMode());
        assertEquals(0, entry.getExternalAttributes() & 0x01); // not read-only
        assertEquals(0, entry.getExternalAttributes() & 0x10); // not directory

        // Set mode 0444 on a directory: read-only, directory bit set
        final ZipArchiveEntry dirEntry = new ZipArchiveEntry("dir/");
        dirEntry.setUnixMode(0444);
        assertEquals(0444, dirEntry.getUnixMode());
        assertEquals(0x01, dirEntry.getExternalAttributes() & 0x01); // read-only
        assertEquals(0x10, dirEntry.getExternalAttributes() & 0x10); // directory bit
    }

    @Test(timeout = 4000)
    public void testGetUnixModeReturnsZeroForFatPlatform() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("fat.txt");
        entry.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry.setExternalAttributes(0755L << 16);
        assertEquals(0, entry.getUnixMode());
    }

    @Test(timeout = 4000)
    public void testRawNameHandling() {
        final ZipArchiveEntry entry = new ZipArchiveEntry();
        assertNull(entry.getRawName());

        final byte[] rawBytes = new byte[] { 't', 'e', 's', 't' };
        entry.setName("test", rawBytes);
        final byte[] retrieved = entry.getRawName();
        assertNotNull(retrieved);
        assertArrayEquals(rawBytes, retrieved);
        assertNotSame(rawBytes, retrieved); // Must be a defensive copy
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNameNormalizationOnFatPlatform() {
        // FAT platform without slash: backslashes should be converted to forward slashes
        final ZipArchiveEntry entry1 = new ZipArchiveEntry("dir\\sub\\file.txt");
        entry1.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry1.setName("dir\\sub\\file.txt");
        assertEquals("dir/sub/file.txt", entry1.getName());

        // FAT platform where name already contains forward slash: backslashes must NOT be replaced
        final ZipArchiveEntry entry2 = new ZipArchiveEntry("dir/sub\\file.txt");
        entry2.setPlatform(ZipArchiveEntry.PLATFORM_FAT);
        entry2.setName("dir/sub\\file.txt");
        assertEquals("dir/sub\\file.txt", entry2.getName());

        // UNIX platform: backslashes must NOT be replaced even if no forward slash exists
        final ZipArchiveEntry entry3 = new ZipArchiveEntry("dir\\sub\\file.txt");
        entry3.setPlatform(ZipArchiveEntry.PLATFORM_UNIX);
        entry3.setName("dir\\sub\\file.txt");
        assertEquals("dir\\sub\\file.txt", entry3.getName());
    }

    @Test(timeout = 4000)
    public void testFileConstructors() throws IOException {
        final File tempFile = File.createTempFile("compress_gemini_test", ".tmp");
        tempFile.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(new byte[] { 1, 2, 3, 4, 5 });
        }

        // Test file constructor for regular file
        final ZipArchiveEntry fileEntry = new ZipArchiveEntry(tempFile, "customFile.bin");
        assertEquals("customFile.bin", fileEntry.getName());
        assertEquals(5L, fileEntry.getSize());
        assertEquals(tempFile.lastModified(), fileEntry.getTime());

        // Test file constructor for directory
        final File tempDir = new File(tempFile.getParentFile(), "gemini_dir_test_" + tempFile.getName());
        assertTrue(tempDir.mkdir());
        tempDir.deleteOnExit();
        try {
            // entryName without slash should have slash appended
            final ZipArchiveEntry dirEntry1 = new ZipArchiveEntry(tempDir, "customDir");
            assertEquals("customDir/", dirEntry1.getName());
            assertTrue(dirEntry1.isDirectory());

            // entryName with slash should remain unchanged
            final ZipArchiveEntry dirEntry2 = new ZipArchiveEntry(tempDir, "customDir/");
            assertEquals("customDir/", dirEntry2.getName());
        } finally {
            tempDir.delete();
            tempFile.delete();
        }
    }

    @Test(timeout = 4000)
    public void testEmptyExtraFieldArrays() {
        final ZipArchiveEntry entry = new ZipArchiveEntry("emptyExtra.txt");
        assertNotNull(entry.getExtraFields());
        assertEquals(0, entry.getExtraFields().length);
        assertEquals(0, entry.getExtraFields(true).length);
        assertEquals(0, entry.getExtraFields(false).length);
        assertNotNull(entry.getLocalFileDataExtra());
        assertEquals(0, entry.getLocalFileDataExtra().length);
        assertNotNull(entry.getCentralDirectoryExtra());
        assertEquals(0, entry.getCentralDirectoryExtra().length);
        assertNull(entry.getExtraField(new ZipShort(0x1234)));
        assertNull(entry.getUnparseableExtraFieldData());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect Verification)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * org.apache.commons.compress.archivers.zip.ZipArchiveEntryTest::isUnixSymlinkIsFalseIfMoreThanOneFlagIsSet
     *
     * In the defective implementation:
     * (getUnixMode() & UnixStat.LINK_FLAG)