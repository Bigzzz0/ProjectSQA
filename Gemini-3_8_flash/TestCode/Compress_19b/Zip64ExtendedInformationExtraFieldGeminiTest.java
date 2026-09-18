/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.util.Arrays;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField
 * ---------------------------------------------------------------------------------------------------------
 * Branch / Path Analysis:
 * 1. getHeaderId() -> Constant HEADER_ID (0x0001).
 * 2. getLocalFileDataLength() -> size != null ? 16 : 0.
 * 3. getCentralDirectoryLength() -> Combinations of size (8), compressedSize (8), offset (8), diskStart (4).
 * 4. getLocalFileDataData():
 *    - size == null && compressedSize == null -> EMPTY (0-byte array).
 *    - size != null && compressedSize == null -> throws IllegalArgumentException.
 *    - size == null && compressedSize != null -> throws IllegalArgumentException.
 *    - size != null && compressedSize != null -> returns 16 bytes with both values.
 * 5. getCentralDirectoryData():
 *    - all 4 fields present -> returns 28 bytes.
 *    - subsets (e.g. only offset, only diskStart, only sizes) -> correct offset progression and packing.
 * 6. parseFromLocalFileData():
 *    - length == 0 -> early return.
 *    - length > 0 && length < 16 -> throws ZipException.
 *    - length == 16 (both sizes only).
 *    - length == 24 (both sizes + offset).
 *    - length == 28 (both sizes + offset + diskStart).
 *    - length > 28 -> remaining bytes ignored.
 * 7. parseFromCentralDirectoryData():
 *    - length >= 28 (3*DWORD + WORD) -> calls parseFromLocalFileData.
 *    - length == 24 (3*DWORD) -> reads size, compressedSize, relativeHeaderOffset.
 *    - length % 8 == 4 (e.g., length = 4, 12, 20) -> reads diskStart from buffer end.
 *    - other lengths -> rawCentralDirectoryData stored, fields unpopulated.
 * 8. reparseCentralDirectoryData():
 *    - rawCentralDirectoryData == null -> no-op.
 *    - defective check: rawCentralDirectoryData.length != expectedLength vs rawCentralDirectoryData.length < expectedLength.
 * ---------------------------------------------------------------------------------------------------------
 * Defect Analysis (Defects4J):
 * - Target Bug: ZipFileTest#testExcessDataInZip64ExtraField
 *   A central directory extra field containing excess data (e.g. 28 bytes when only 16 are expected)
 *   causes reparseCentralDirectoryData to fail with ZipException in defective versions because it enforces
 *   strict length equality (`rawCentralDirectoryData.length != expectedLength`) instead of accepting excess bytes.
 *   Test: testReparseCentralDirectoryDataWithExcessData() expects successful parsing without throwing ZipException.
 * ---------------------------------------------------------------------------------------------------------
 */
public class Zip64ExtendedInformationExtraFieldGeminiTest {

    private static final ZipEightByteInteger SIZE_100 = new ZipEightByteInteger(100L);
    private static final ZipEightByteInteger SIZE_200 = new ZipEightByteInteger(200L);
    private static final ZipEightByteInteger OFFSET_300 = new ZipEightByteInteger(300L);
    private static final ZipLong DISK_1 = new ZipLong(1L);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorInitialState() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        assertEquals(new ZipShort(0x0001), field.getHeaderId());
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
        assertEquals(0, field.getLocalFileDataLength().getValue());
        assertEquals(0, field.getCentralDirectoryLength().getValue());
        assertArrayEquals(new byte[0], field.getLocalFileDataData());
        assertArrayEquals(new byte[0], field.getCentralDirectoryData());
    }

    @Test(timeout = 4000)
    public void testTwoArgConstructorAndGettersSetters() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE_100, SIZE_200);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());

        assertEquals(16, field.getLocalFileDataLength().getValue());
        assertEquals(16, field.getCentralDirectoryLength().getValue());

        ZipEightByteInteger newSize = new ZipEightByteInteger(500L);
        ZipEightByteInteger newComp = new ZipEightByteInteger(600L);
        ZipEightByteInteger newOff = new ZipEightByteInteger(700L);
        ZipLong newDisk = new ZipLong(2L);

        field.setSize(newSize);
        field.setCompressedSize(newComp);
        field.setRelativeHeaderOffset(newOff);
        field.setDiskStartNumber(newDisk);

        assertEquals(newSize, field.getSize());
        assertEquals(newComp, field.getCompressedSize());
        assertEquals(newOff, field.getRelativeHeaderOffset());
        assertEquals(newDisk, field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testFourArgConstructor() {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE_100, SIZE_200, OFFSET_300, DISK_1);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertEquals(OFFSET_300, field.getRelativeHeaderOffset());
        assertEquals(DISK_1, field.getDiskStartNumber());

        assertEquals(16, field.getLocalFileDataLength().getValue());
        assertEquals(28, field.getCentralDirectoryLength().getValue());

        byte[] cdData = field.getCentralDirectoryData();
        assertEquals(28, cdData.length);
        assertArrayEquals(SIZE_100.getBytes(), Arrays.copyOfRange(cdData, 0, 8));
        assertArrayEquals(SIZE_200.getBytes(), Arrays.copyOfRange(cdData, 8, 16));
        assertArrayEquals(OFFSET_300.getBytes(), Arrays.copyOfRange(cdData, 16, 24));
        assertArrayEquals(DISK_1.getBytes(), Arrays.copyOfRange(cdData, 24, 28));
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryDataPartialCombinations() {
        // Only relativeHeaderOffset and diskStart populated
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setRelativeHeaderOffset(OFFSET_300);
        field.setDiskStartNumber(DISK_1);

        assertEquals(0, field.getLocalFileDataLength().getValue());
        assertEquals(12, field.getCentralDirectoryLength().getValue());

        byte[] cdData = field.getCentralDirectoryData();
        assertEquals(12, cdData.length);
        assertArrayEquals(OFFSET_300.getBytes(), Arrays.copyOfRange(cdData, 0, 8));
        assertArrayEquals(DISK_1.getBytes(), Arrays.copyOfRange(cdData, 8, 12));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Parsing Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataZeroLength() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[10];
        field.parseFromLocalFileData(buffer, 0, 0);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataExact16Bytes() throws ZipException {
        byte[] buffer = new byte[16];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 16);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileData24BytesWithOffset() throws ZipException {
        byte[] buffer = new byte[24];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET_300.getBytes(), 0, buffer, 16, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 24);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertEquals(OFFSET_300, field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileData28BytesAllFields() throws ZipException {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET_300.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_1.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(buffer, 0, 28);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertEquals(OFFSET_300, field.getRelativeHeaderOffset());
        assertEquals(DISK_1, field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryDataExact24Bytes() throws ZipException {
        byte[] buffer = new byte[24];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET_300.getBytes(), 0, buffer, 16, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 24);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertEquals(OFFSET_300, field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryDataDiskStartModuloBranch() throws ZipException {
        // length % DWORD == WORD -> length % 8 == 4 (e.g. 4 bytes for diskStart alone)
        byte[] buffer = new byte[4];
        System.arraycopy(DISK_1.getBytes(), 0, buffer, 0, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 4);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertEquals(DISK_1, field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryDataNonStandardLength() throws ZipException {
        // length that does not match >= 28, == 24, or % 8 == 4 (e.g. length = 8)
        byte[] buffer = new byte[8];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 8);

        // Fields remain unpopulated until reparse is called
        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testReparseCentralDirectoryDataExactMatch() throws ZipException {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET_300.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_1.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 28);
        // Reset fields to ensure reparse repopulates them
        field.setSize(null);
        field.setCompressedSize(null);
        field.setRelativeHeaderOffset(null);
        field.setDiskStartNumber(null);

        field.reparseCentralDirectoryData(true, true, true, true);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertEquals(OFFSET_300, field.getRelativeHeaderOffset());
        assertEquals(DISK_1, field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testReparseCentralDirectoryDataNoRawDataIsNoOp() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.reparseCentralDirectoryData(true, true, true, true);

        assertNull(field.getSize());
        assertNull(field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets ZipFileTest::testExcessDataInZip64ExtraField.
     * Some ZIP creation tools write all 28 bytes (sizes, offset, disk start) into the
     * central directory extra field, even if only sizes (16 bytes) are actually required
     * according to the central directory record headers.
     * The defective code strictly requires rawCentralDirectoryData.length == expectedLength,
     * which throws ZipException when excess data is present.
     */
    @Test(timeout = 4000)
    public void testReparseCentralDirectoryDataWithExcessData() throws ZipException {
        byte[] buffer = new byte[28];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, buffer, 8, 8);
        System.arraycopy(OFFSET_300.getBytes(), 0, buffer, 16, 8);
        System.arraycopy(DISK_1.getBytes(), 0, buffer, 24, 4);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 28);

        // Expected length is 16 (8 + 8), but raw length is 28.
        // Valid ZIP processors must tolerate excess data in the extra field.
        field.reparseCentralDirectoryData(true, true, false, false);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataThrowsWhenSizeMissing() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setCompressedSize(SIZE_200);
        field.getLocalFileDataData();
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLocalFileDataDataThrowsWhenCompressedSizeMissing() {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.setSize(SIZE_100);
        field.getLocalFileDataData();
    }

    @Test(timeout = 4000, expected = ZipException.class)
    public void testParseFromLocalFileDataUnderflowThrows() throws ZipException {
        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        byte[] buffer = new byte[15];
        // length > 0 but < 16 must throw ZipException
        field.parseFromLocalFileData(buffer, 0, 15);
    }

    @Test(timeout = 4000, expected = ZipException.class)
    public void testReparseCentralDirectoryDataLengthTooShortThrows() throws ZipException {
        byte[] buffer = new byte[8];
        System.arraycopy(SIZE_100.getBytes(), 0, buffer, 0, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromCentralDirectoryData(buffer, 0, 8);

        // Expecting 16 bytes (size + compressed size), but only 8 provided
        field.reparseCentralDirectoryData(true, true, false, false);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Buffer Offset Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseWithNonZeroBufferOffset() throws ZipException {
        byte[] container = new byte[50];
        int offset = 10;
        System.arraycopy(SIZE_100.getBytes(), 0, container, offset, 8);
        System.arraycopy(SIZE_200.getBytes(), 0, container, offset + 8, 8);

        Zip64ExtendedInformationExtraField field = new Zip64ExtendedInformationExtraField();
        field.parseFromLocalFileData(container, offset, 16);

        assertEquals(SIZE_100, field.getSize());
        assertEquals(SIZE_200, field.getCompressedSize());
        assertNull(field.getRelativeHeaderOffset());
        assertNull(field.getDiskStartNumber());
    }

    @Test(timeout = 4000)
    public void testLocalFileDataDataRoundTrip() throws ZipException {
        Zip64ExtendedInformationExtraField field =
            new Zip64ExtendedInformationExtraField(SIZE_100, SIZE_200);
        byte[] lfhData = field.getLocalFileDataData();
        assertEquals(16, lfhData.length);

        Zip64ExtendedInformationExtraField parsedField = new Zip64ExtendedInformationExtraField();
        parsedField.parseFromLocalFileData(lfhData, 0, lfhData.length);

        assertEquals(field.getSize(), parsedField.getSize());
        assertEquals(field.getCompressedSize(), parsedField.getCompressedSize());
    }

    @Test(timeout = 4000)
    public void testCentralDirectoryDataRoundTrip() throws ZipException {
        Zip64ExtendedInformationExtraField original =
            new Zip64ExtendedInformationExtraField(SIZE_100, SIZE_200, OFFSET_300, DISK_1);
        byte[] cdData = original.getCentralDirectoryData();
        assertEquals(28, cdData.length);

        Zip64ExtendedInformationExtraField parsed = new Zip64ExtendedInformationExtraField();
        parsed.parseFromCentralDirectoryData(cdData, 0, cdData.length);

        assertEquals(original.getSize(), parsed.getSize());
        assertEquals(original.getCompressedSize(), parsed.getCompressedSize());
        assertEquals(original.getRelativeHeaderOffset(), parsed.getRelativeHeaderOffset());
        assertEquals(original.getDiskStartNumber(), parsed.getDiskStartNumber());
    }
}