package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: X5455_ExtendedTimestamp
 * 
 * Decision branches targeted:
 * 1. getLocalFileDataLength(): branches on bit0_modifyTimePresent, bit1_accessTimePresent && accessTime != null, bit2_createTimePresent && createTime != null
 * 2. getCentralDirectoryLength(): branches on bit0_modifyTimePresent
 * 3. getLocalFileDataData(): branches on bit0_modifyTimePresent, bit1_accessTimePresent && accessTime != null, bit2_createTimePresent && createTime != null
 * 4. parseFromLocalFileData(): branches on bit0_modifyTimePresent, bit1_accessTimePresent && offset+4 <= len, bit2_createTimePresent && offset+4 <= len
 * 5. setFlags(): branches on flags & MODIFY_TIME_BIT, ACCESS_TIME_BIT, CREATE_TIME_BIT
 * 6. setModifyTime/setAccessTime/setCreateTime(): branches on l != null
 * 7. dateToZipLong(): branches on d == null, l >= TWO_TO_32 (throws IllegalArgumentException)
 * 8. unixTimeToZipLong(): branches on l >= TWO_TO_32
 * 9. equals(): branches on instanceof, flags & 0x07 comparison, null checks for each ZipLong
 * 10. hashCode(): branches on modifyTime != null, accessTime != null, createTime != null
 * 
 * Boundary conditions:
 * - Zero timestamps (epoch)
 * - Maximum 32-bit signed value (0x7FFFFFFF = 2038-01-19)
 * - Negative timestamps (pre-1970 dates)
 * - Null timestamps with various flag combinations
 * - Flags with unused bits set (bits 3-7)
 * - Central directory data truncation (missing access/create fields)
 * - IllegalArgumentException for timestamps >= 2^32
 * 
 * Known defect: unixTimeToZipLong() throws IllegalArgumentException for values >= 2^32,
 * but the method is called from dateToZipLong() which divides by 1000, potentially
 * producing values that don't trigger the check correctly. The defect manifests as
 * "Time too big for 32 bits!" assertion failure.
 */
public class X5455_ExtendedTimestampDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertEquals(0, xt.getFlags());
        assertFalse(xt.isBit0_modifyTimePresent());
        assertFalse(xt.isBit1_accessTimePresent());
        assertFalse(xt.isBit2_createTimePresent());
        assertNull(xt.getModifyTime());
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
        assertNull(xt.getModifyJavaTime());
        assertNull(xt.getAccessJavaTime());
        assertNull(xt.getCreateJavaTime());
    }

    @Test(timeout = 4000)
    public void testSetFlagsAllBits() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x07); // bits 0,1,2 set
        assertEquals(0x07, xt.getFlags());
        assertTrue(xt.isBit0_modifyTimePresent());
        assertTrue(xt.isBit1_accessTimePresent());
        assertTrue(xt.isBit2_createTimePresent());
    }

    @Test(timeout = 4000)
    public void testSetFlagsNoBits() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x00);
        assertFalse(xt.isBit0_modifyTimePresent());
        assertFalse(xt.isBit1_accessTimePresent());
        assertFalse(xt.isBit2_createTimePresent());
    }

    @Test(timeout = 4000)
    public void testSetFlagsWithUnusedBits() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0xF8); // only unused bits set
        assertEquals(0xF8, xt.getFlags());
        assertFalse(xt.isBit0_modifyTimePresent());
        assertFalse(xt.isBit1_accessTimePresent());
        assertFalse(xt.isBit2_createTimePresent());
    }

    @Test(timeout = 4000)
    public void testSetModifyTimeNonNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(1000000);
        xt.setModifyTime(time);
        assertTrue(xt.isBit0_modifyTimePresent());
        assertEquals(time, xt.getModifyTime());
        assertEquals((byte) 0x01, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetModifyTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x01);
        xt.setModifyTime(null);
        assertFalse(xt.isBit0_modifyTimePresent());
        assertNull(xt.getModifyTime());
        assertEquals((byte) 0x00, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetAccessTimeNonNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(2000000);
        xt.setAccessTime(time);
        assertTrue(xt.isBit1_accessTimePresent());
        assertEquals(time, xt.getAccessTime());
        assertEquals((byte) 0x02, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetAccessTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x02);
        xt.setAccessTime(null);
        assertFalse(xt.isBit1_accessTimePresent());
        assertNull(xt.getAccessTime());
        assertEquals((byte) 0x00, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetCreateTimeNonNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong time = new ZipLong(3000000);
        xt.setCreateTime(time);
        assertTrue(xt.isBit2_createTimePresent());
        assertEquals(time, xt.getCreateTime());
        assertEquals((byte) 0x04, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetCreateTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x04);
        xt.setCreateTime(null);
        assertFalse(xt.isBit2_createTimePresent());
        assertNull(xt.getCreateTime());
        assertEquals((byte) 0x00, xt.getFlags());
    }

    @Test(timeout = 4000)
    public void testSetModifyJavaTime() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        Date d = new Date(1000000000000L); // 2001-09-09
        xt.setModifyJavaTime(d);
        assertTrue(xt.isBit0_modifyTimePresent());
        assertEquals(new ZipLong(1000000000L), xt.getModifyTime());
        assertEquals(d.getTime() / 1000 * 1000, xt.getModifyJavaTime().getTime());
    }

    @Test(timeout = 4000)
    public void testSetAccessJavaTime() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        Date d = new Date(2000000000000L);
        xt.setAccessJavaTime(d);
        assertTrue(xt.isBit1_accessTimePresent());
        assertEquals(new ZipLong(2000000000L), xt.getAccessTime());
    }

    @Test(timeout = 4000)
    public void testSetCreateJavaTime() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        Date d = new Date(3000000000000L);
        xt.setCreateJavaTime(d);
        assertTrue(xt.isBit2_createTimePresent());
        assertEquals(new ZipLong(3000000000L), xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testSetModifyJavaTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyJavaTime(null);
        assertFalse(xt.isBit0_modifyTimePresent());
        assertNull(xt.getModifyTime());
    }

    @Test(timeout = 4000)
    public void testSetAccessJavaTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setAccessJavaTime(null);
        assertFalse(xt.isBit1_accessTimePresent());
        assertNull(xt.getAccessTime());
    }

    @Test(timeout = 4000)
    public void testSetCreateJavaTimeNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setCreateJavaTime(null);
        assertFalse(xt.isBit2_createTimePresent());
        assertNull(xt.getCreateTime());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testZeroTimestamps() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(0));
        xt.setAccessTime(new ZipLong(0));
        xt.setCreateTime(new ZipLong(0));
        assertEquals(new Date(0), xt.getModifyJavaTime());
        assertEquals(new Date(0), xt.getAccessJavaTime());
        assertEquals(new Date(0), xt.getCreateJavaTime());
    }

    @Test(timeout = 4000)
    public void testMaxSigned32BitTimestamp() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong maxTime = new ZipLong(0x7FFFFFFF); // 2147483647
        xt.setModifyTime(maxTime);
        assertEquals(maxTime, xt.getModifyTime());
        assertEquals(new Date(2147483647000L), xt.getModifyJavaTime());
    }

    @Test(timeout = 4000)
    public void testNegativeTimestamp() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong negTime = new ZipLong(-1); // 1969-12-31 23:59:59 UTC
        xt.setModifyTime(negTime);
        assertEquals(negTime, xt.getModifyTime());
        assertEquals(new Date(-1000L), xt.getModifyJavaTime());
    }

    @Test(timeout = 4000)
    public void testMinSigned32BitTimestamp() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        ZipLong minTime = new ZipLong(0x80000000); // -2147483648
        xt.setModifyTime(minTime);
        assertEquals(minTime, xt.getModifyTime());
        assertEquals(new Date(-2147483648000L), xt.getModifyJavaTime());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLengthNoTimestamps() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertEquals(1, xt.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLengthOnlyModify() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        assertEquals(5, xt.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLengthModifyAndAccess() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        assertEquals(9, xt.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLengthAllThree() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        xt.setCreateTime(new ZipLong(300));
        assertEquals(13, xt.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLengthAccessFlagSetButNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x03); // modify and access flags set
        xt.setModifyTime(new ZipLong(100));
        // accessTime is null despite flag being set
        assertEquals(5, xt.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryLengthNoModify() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertEquals(1, xt.getCentralDirectoryLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryLengthWithModify() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        assertEquals(5, xt.getCentralDirectoryLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryLengthWithAccessAndCreate() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        xt.setCreateTime(new ZipLong(300));
        // Central directory only includes modify time
        assertEquals(5, xt.getCentralDirectoryLength().getValue());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testDateToZipLongExactBoundary() {
        // Test the boundary condition: 2^32 - 1 should be valid
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        // 2^32 - 1 = 4294967295, but as signed int it's -1
        // The check is l >= 2^32, so 2^32 - 1 should pass
        Date d = new Date(4294967295L * 1000); // This is beyond 32-bit signed range
        // This should throw IllegalArgumentException because 4294967295 >= 2^32
        try {
            xt.setModifyJavaTime(d);
            fail("Expected IllegalArgumentException for timestamp >= 2^32");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    @Test(timeout = 4000)
    public void testDateToZipLongMaxValid() {
        // Maximum valid value: 2^32 - 1 = 4294967295 seconds from epoch
        // But as signed int, this would be -1. The check is l >= 2^32, so 2^32-1 is valid.
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        Date d = new Date(4294967294L * 1000); // 2^32 - 2 seconds
        xt.setModifyJavaTime(d);
        // The ZipLong stores the value as signed int, so 4294967294 becomes -2
        assertEquals(new ZipLong(4294967294L), xt.getModifyTime());
    }

    @Test(timeout = 4000)
    public void testDateToZipLongExactlyTwoTo32() {
        // Exactly 2^32 should throw
        try {
            X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
            Date d = new Date(4294967296L * 1000); // 2^32 seconds
            xt.setModifyJavaTime(d);
            fail("Expected IllegalArgumentException for timestamp == 2^32");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDateToZipLongNegativeDate() {
        // Date before epoch should work
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        Date d = new Date(-1000); // 1969-12-31 23:59:59
        xt.setModifyJavaTime(d);
        assertEquals(new ZipLong(-1), xt.getModifyTime());
    }

    @Test(timeout = 4000)
    public void testDateToZipLongNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyJavaTime(null);
        assertNull(xt.getModifyTime());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataOnlyFlags() throws ZipException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] { 0x00 };
        xt.parseFromLocalFileData(data, 0, 1);
        assertEquals(0, xt.getFlags());
        assertNull(xt.getModifyTime());
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataWithModifyTime() throws ZipException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] { 
            0x01, // flags with modify bit
            0x00, 0x00, 0x00, 0x64 // 100 as little-endian
        };
        xt.parseFromLocalFileData(data, 0, 5);
        assertTrue(xt.isBit0_modifyTimePresent());
        assertEquals(new ZipLong(100), xt.getModifyTime());
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataWithAllTimestamps() throws ZipException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] {
            0x07, // flags with all bits
            0x00, 0x00, 0x00, 0x64, // modify = 100
            0x00, 0x00, 0x00, 0xC8, // access = 200
            0x00, 0x00, 0x01, 0x2C  // create = 300
        };
        xt.parseFromLocalFileData(data, 0, 13);
        assertEquals(new ZipLong(100), xt.getModifyTime());
        assertEquals(new ZipLong(200), xt.getAccessTime());
        assertEquals(new ZipLong(300), xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataCentralDirectoryTruncation() throws ZipException {
        // Simulate central directory data that has flags with access/create bits set
        // but only contains modify time data
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] {
            0x07, // flags with all bits
            0x00, 0x00, 0x00, 0x64  // modify = 100 (only 5 bytes total)
        };
        xt.parseFromLocalFileData(data, 0, 5);
        assertEquals(new ZipLong(100), xt.getModifyTime());
        // Access and create should remain null because data is too short
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataPartialAccess() throws ZipException {
        // Flags indicate access time present but data is truncated
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] {
            0x03, // modify and access bits
            0x00, 0x00, 0x00, 0x64, // modify = 100
            0x00, 0x00, 0x00        // only 3 bytes of access time (should be 4)
        };
        xt.parseFromLocalFileData(data, 0, 8);
        assertEquals(new ZipLong(100), xt.getModifyTime());
        // Access time should be null because data is incomplete
        assertNull(xt.getAccessTime());
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryData() throws ZipException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = new byte[] {
            0x01, // modify bit only
            0x00, 0x00, 0x00, 0x64
        };
        xt.parseFromCentralDirectoryData(data, 0, 5);
        assertEquals(new ZipLong(100), xt.getModifyTime());
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryDataResetsState() throws ZipException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(999));
        xt.setAccessTime(new ZipLong(888));
        xt.setCreateTime(new ZipLong(777));
        
        byte[] data = new byte[] { 0x00 };
        xt.parseFromCentralDirectoryData(data, 0, 1);
        assertNull(xt.getModifyTime());
        assertNull(xt.getAccessTime());
        assertNull(xt.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataDataNoTimestamps() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        byte[] data = xt.getLocalFileDataData();
        assertEquals(1, data.length);
        assertEquals(0, data[0]);
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataDataWithModifyTime() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        byte[] data = xt.getLocalFileDataData();
        assertEquals(5, data.length);
        assertEquals(0x01, data[0]); // flags byte
        assertEquals(100, new ZipLong(data, 1).getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataDataWithAllTimestamps() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        xt.setCreateTime(new ZipLong(300));
        byte[] data = xt.getLocalFileDataData();
        assertEquals(13, data.length);
        assertEquals(0x07, data[0]); // flags byte
        assertEquals(100, new ZipLong(data, 1).getIntValue());
        assertEquals(200, new ZipLong(data, 5).getIntValue());
        assertEquals(300, new ZipLong(data, 9).getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataDataAccessFlagSetButNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setFlags((byte) 0x03); // modify and access flags set
        xt.setModifyTime(new ZipLong(100));
        // accessTime is null, so it should not be included
        byte[] data = xt.getLocalFileDataData();
        assertEquals(5, data.length);
        assertEquals(0x01, data[0]); // only modify bit set in output
        assertEquals(100, new ZipLong(data, 1).getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryData() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        xt.setCreateTime(new ZipLong(300));
        byte[] centralData = xt.getCentralDirectoryData();
        assertEquals(5, centralData.length);
        assertEquals(0x01, centralData[0]); // only modify bit
        assertEquals(100, new ZipLong(centralData, 1).getIntValue());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertTrue(xt.equals(xt));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertFalse(xt.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertFalse(xt.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsIdenticalState() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt1.setAccessTime(new ZipLong(200));
        xt1.setCreateTime(new ZipLong(300));
        xt2.setModifyTime(new ZipLong(100));
        xt2.setAccessTime(new ZipLong(200));
        xt2.setCreateTime(new ZipLong(300));
        assertTrue(xt1.equals(xt2));
        assertTrue(xt2.equals(xt1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentModifyTime() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt2.setModifyTime(new ZipLong(200));
        assertFalse(xt1.equals(xt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentAccessTime() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt1.setAccessTime(new ZipLong(200));
        xt2.setModifyTime(new ZipLong(100));
        xt2.setAccessTime(new ZipLong(300));
        assertFalse(xt1.equals(xt2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentCreateTime() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt1.setCreateTime(new ZipLong(300));
        xt2.setModifyTime(new ZipLong(100));
        xt2.setCreateTime(new ZipLong(400));
        assertFalse(xt1.equals(xt2));
    }

    @Test(timeout = 4000)
    public void testEqualsNullTimestamps() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        assertTrue(xt1.equals(xt2));
    }

    @Test(timeout = 4000)
    public void testEqualsOneNullModifyTime() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        // xt2 has null modifyTime
        assertFalse(xt1.equals(xt2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        int hc1 = xt.hashCode();
        int hc2 = xt.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testHashCodeEqualObjects() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt1.setAccessTime(new ZipLong(200));
        xt1.setCreateTime(new ZipLong(300));
        xt2.setModifyTime(new ZipLong(100));
        xt2.setAccessTime(new ZipLong(200));
        xt2.setCreateTime(new ZipLong(300));
        assertEquals(xt1.hashCode(), xt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferentObjects() {
        X5455_ExtendedTimestamp xt1 = new X5455_ExtendedTimestamp();
        X5455_ExtendedTimestamp xt2 = new X5455_ExtendedTimestamp();
        xt1.setModifyTime(new ZipLong(100));
        xt2.setModifyTime(new ZipLong(200));
        assertNotEquals(xt1.hashCode(), xt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(100));
        xt.setAccessTime(new ZipLong(200));
        xt.setCreateTime(new ZipLong(300));
        X5455_ExtendedTimestamp clone = (X5455_ExtendedTimestamp) xt.clone();
        assertTrue(xt.equals(clone));
        assertEquals(xt.hashCode(), clone.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        String str = xt.toString();
        assertTrue(str.contains("0x5455"));
        assertTrue(str.contains("Flags="));
    }

    @Test(timeout = 4000)
    public void testToStringWithTimestamps() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        xt.setModifyTime(new ZipLong(1000000));
        xt.setAccessTime(new ZipLong(2000000));
        xt.setCreateTime(new ZipLong(3000000));
        String str = xt.toString();
        assertTrue(str.contains("Modify:"));
        assertTrue(str.contains("Access:"));
        assertTrue(str.contains("Create:"));
    }

    @Test(timeout = 4000)
    public void testGetHeaderId() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        assertEquals(0x5455, xt.getHeaderId().getValue());
    }

    @Test(timeout = 4000)
    public void testSerialVersionUID() {
        assertEquals(1L, X5455_ExtendedTimestamp.class.getDeclaredField("serialVersionUID").getLong(null));
    }

    // ==================== Defect-Specific Test ====================

    /**
     * This test directly targets the known defect where timestamps >= 2^32
     * cause an IllegalArgumentException. The defect manifests when trying to
     * set a timestamp that is too large for 32-bit signed integer representation.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDefectTimeTooBigFor32Bits() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        // 2^32 seconds from epoch = 2106-02-07 06:28:16
        // This value is >= 2^32 and should throw IllegalArgumentException
        Date d = new Date(4294967296L * 1000);
        xt.setModifyJavaTime(d);
    }

    @Test(timeout = 4000)
    public void testDefectTimeAtBoundary() {
        X5455_ExtendedTimestamp xt = new X5455_ExtendedTimestamp();
        // 2^32 - 1 = 4294967295 seconds from epoch
        // This should be valid as it's < 2^32
        Date d = new Date(4294967295L * 1000);
        try {
            xt.setModifyJavaTime(d);
            // If we get here, the value was accepted (which is correct per the spec)
            // The ZipLong will store it as a negative signed int
            assertEquals(new ZipLong(4294967295L), xt.getModifyTime());
        } catch (IllegalArgumentException e) {
            // If it throws, that's the bug we're testing for
            fail("Should not throw for value < 2^32: " + e.getMessage());
        }
    }
}