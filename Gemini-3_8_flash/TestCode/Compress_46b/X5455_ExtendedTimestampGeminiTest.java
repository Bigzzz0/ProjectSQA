/* [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Class Under Test: org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp
 *
 * Decision / Condition Coverage Targets:
 * 1. getLocalFileDataLength():
 *    - bit0_modifyTimePresent: true / false
 *    - bit1_accessTimePresent && accessTime != null: (T,T), (T,F), (F,T/F)
 *    - bit2_createTimePresent && createTime != null: (T,T), (T,F), (F,T/F)
 * 2. getCentralDirectoryLength():
 *    - bit0_modifyTimePresent: true / false
 * 3. getLocalFileDataData():
 *    - bit0_modifyTimePresent: true (writes 4 bytes modTime), false
 *    - bit1_accessTimePresent && accessTime != null: true (writes 4 bytes accessTime), false
 *    - bit2_createTimePresent && createTime != null: true (writes 4 bytes createTime), false
 * 4. getCentralDirectoryData():
 *    - Truncates access and create times, returning only header flag byte + (optional) modTime
 * 5. parseFromLocalFileData(byte[], int, int):
 *    - bit0_modifyTimePresent: true (parses modTime), false
 *    - bit1_accessTimePresent && offset + 4 <= len: (T,T) parses acTime; (T,F) skips acTime; (F,*) skips
 *    - bit2_createTimePresent && offset + 4 <= len: (T,T) parses crTime; (T,F) skips crTime; (F,*) skips
 * 6. parseFromCentralDirectoryData(byte[], int, int):
 *    - delegates to parseFromLocalFileData after reset()
 * 7. setModifyTime / setAccessTime / setCreateTime (and Java Date equivalents):
 *    - null parameter: unsets flag bit, sets time to null
 *    - non-null parameter: sets flag bit, sets time
 * 8. unixTimeToZipLong(long):
 *    - [DEFECT ZONE]: checks boundary conditions for 32-bit timestamps.
 *      Defect in Apache Commons Compress: fails to reject negative timestamps that do not fit in a
 *      32-bit signed integer (i.e. l < -0x80000000L).
 * 9. equals / hashCode / clone / toString:
 *    - equals: null, non-instance, self, identical fields, flags with non-significant bits difference,
 *      mismatched modify/access/create times.
 *    - hashCode: consistency with equals, verification of bit shifts avoiding XOR cancellation.
 *    - clone: deep/independent clone integrity.
 *    - toString: outputs formatted binary flags and dates when present.
 * =====================================================================================================
 */
package org.apache.commons.compress.archivers.zip;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.ZipException;

import static org.junit.Assert.*;

public class X5455_ExtendedTimestampGeminiTest {

    private static final ZipShort HEADER_ID = new ZipShort(0x5455);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHeaderIdConstant() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        assertEquals(HEADER_ID, xf.getHeaderId());
        assertEquals(0x5455, xf.getHeaderId().getValue());
    }

    @Test(timeout = 4000)
    public void testDefaultState() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        assertEquals(0, xf.getFlags());
        assertFalse(xf.isBit0_modifyTimePresent());
        assertFalse(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        assertNull(xf.getModifyTime());
        assertNull(xf.getAccessTime());
        assertNull(xf.getCreateTime());
        assertNull(xf.getModifyJavaTime());
        assertNull(xf.getAccessJavaTime());
        assertNull(xf.getCreateJavaTime());
        assertEquals(1, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        final byte[] localData = xf.getLocalFileDataData();
        assertArrayEquals(new byte[]{0}, localData);

        final byte[] centralData = xf.getCentralDirectoryData();
        assertArrayEquals(new byte[]{0}, centralData);
    }

    @Test(timeout = 4000)
    public void testSetAndGetZipLongTimes() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final ZipLong time1 = new ZipLong(100000L);
        final ZipLong time2 = new ZipLong(200000L);
        final ZipLong time3 = new ZipLong(300000L);

        xf.setModifyTime(time1);
        assertTrue(xf.isBit0_modifyTimePresent());
        assertEquals(X5455_ExtendedTimestamp.MODIFY_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.MODIFY_TIME_BIT);
        assertEquals(time1, xf.getModifyTime());

        xf.setAccessTime(time2);
        assertTrue(xf.isBit1_accessTimePresent());
        assertEquals(X5455_ExtendedTimestamp.ACCESS_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.ACCESS_TIME_BIT);
        assertEquals(time2, xf.getAccessTime());

        xf.setCreateTime(time3);
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(X5455_ExtendedTimestamp.CREATE_TIME_BIT, xf.getFlags() & X5455_ExtendedTimestamp.CREATE_TIME_BIT);
        assertEquals(time3, xf.getCreateTime());

        assertEquals(7, xf.getFlags() & 0x07);
        assertEquals(13, xf.getLocalFileDataLength().getValue());
        assertEquals(5, xf.getCentralDirectoryLength().getValue());

        // Unset modify time
        xf.setModifyTime(null);
        assertFalse(xf.isBit0_modifyTimePresent());
        assertNull(xf.getModifyTime());
        assertEquals(0, xf.getFlags() & X5455_ExtendedTimestamp.MODIFY_TIME_BIT);
        assertEquals(9, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        // Unset access time
        xf.setAccessTime(null);
        assertFalse(xf.isBit1_accessTimePresent());
        assertNull(xf.getAccessTime());
        assertEquals(5, xf.getLocalFileDataLength().getValue());

        // Unset create time
        xf.setCreateTime(null);
        assertFalse(xf.isBit2_createTimePresent());
        assertNull(xf.getCreateTime());
        assertEquals(1, xf.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testSetAndGetJavaDates() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final Date modDate = new Date(123456789000L);
        final Date accDate = new Date(987654321000L);
        final Date crDate = new Date(111111111000L);

        xf.setModifyJavaTime(modDate);
        xf.setAccessJavaTime(accDate);
        xf.setCreateJavaTime(crDate);

        assertEquals(modDate, xf.getModifyJavaTime());
        assertEquals(accDate, xf.getAccessJavaTime());
        assertEquals(crDate, xf.getCreateJavaTime());

        // Null dates clear fields
        xf.setModifyJavaTime(null);
        assertNull(xf.getModifyJavaTime());
        assertNull(xf.getModifyTime());
        assertFalse(xf.isBit0_modifyTimePresent());

        xf.setAccessJavaTime(null);
        assertNull(xf.getAccessJavaTime());
        assertNull(xf.getAccessTime());
        assertFalse(xf.isBit1_accessTimePresent());

        xf.setCreateJavaTime(null);
        assertNull(xf.getCreateJavaTime());
        assertNull(xf.getCreateTime());
        assertFalse(xf.isBit2_createTimePresent());
    }

    @Test(timeout = 4000)
    public void testDateTruncationToSeconds() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // 123456789555L has 555ms; should be truncated to per-second precision
        final Date dateWithMillis = new Date(123456789555L);
        final Date dateTruncated = new Date(123456789000L);

        xf.setModifyJavaTime(dateWithMillis);
        assertEquals(dateTruncated, xf.getModifyJavaTime());
        assertEquals(new ZipLong(123456789L), xf.getModifyTime());
    }

    @Test(timeout = 4000)
    public void testLocalAndCentralDataGenerationAllPresent() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.setModifyTime(new ZipLong(0x11223344L));
        xf.setAccessTime(new ZipLong(0x55667788L));
        xf.setCreateTime(new ZipLong(0x01020304L));

        final byte[] local = xf.getLocalFileDataData();
        assertEquals(13, local.length);
        assertEquals(7, local[0]); // bit 0, 1, 2 set

        // Check modTime little-endian (0x11223344)
        assertEquals((byte) 0x44, local[1]);
        assertEquals((byte) 0x33, local[2]);
        assertEquals((byte) 0x22, local[3]);
        assertEquals((byte) 0x11, local[4]);

        // Check accTime little-endian (0x55667788)
        assertEquals((byte) 0x88, local[5]);
        assertEquals((byte) 0x77, local[6]);
        assertEquals((byte) 0x66, local[7]);
        assertEquals((byte) 0x55, local[8]);

        // Check crTime little-endian (0x01020304)
        assertEquals((byte) 0x04, local[9]);
        assertEquals((byte) 0x03, local[10]);
        assertEquals((byte) 0x02, local[11]);
        assertEquals((byte) 0x01, local[12]);

        // Central data must contain flags and modTime only (length 5)
        final byte[] central = xf.getCentralDirectoryData();
        assertEquals(5, central.length);
        assertEquals(7, central[0]);
        assertEquals((byte) 0x44, central[1]);
        assertEquals((byte) 0x33, central[2]);
        assertEquals((byte) 0x22, central[3]);
        assertEquals((byte) 0x11, central[4]);
    }

    @Test(timeout = 4000)
    public void testParseFullLocalFileData() throws ZipException {
        final byte[] data = new byte[]{
                7, // flags: mod, acc, cr
                0x44, 0x33, 0x22, 0x11, // mod: 0x11223344
                (byte) 0x88, 0x77, 0x66, 0x55, // acc: 0x55667788
                0x04, 0x03, 0x02, 0x01  // cr:  0x01020304
        };

        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(data, 0, data.length);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(0x11223344L), xf.getModifyTime());
        assertEquals(new ZipLong(0x55667788L), xf.getAccessTime());
        assertEquals(new ZipLong(0x01020304L), xf.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParseCentralDirectoryDataDelegation() throws ZipException {
        final byte[] centralData = new byte[]{
                7, // flags say all 3 present in local, but central only has modTime
                0x44, 0x33, 0x22, 0x11
        };

        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromCentralDirectoryData(centralData, 0, centralData.length);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(0x11223344L), xf.getModifyTime());
        assertNull(xf.getAccessTime());
        assertNull(xf.getCreateTime());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Truncation
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseWithOffsetAndExtraTrailingBytes() throws ZipException {
        final byte[] buffer = new byte[]{
                (byte) 0xFF, (byte) 0xEE, // garbage prefix
                3,                        // flags: mod and acc present
                0x10, 0x20, 0x30, 0x40,   // mod
                0x50, 0x60, 0x70, (byte) 0x80, // acc
                (byte) 0xAA, (byte) 0xBB  // garbage suffix
        };

        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(buffer, 2, 9);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertFalse(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(0x40302010L), xf.getModifyTime());
        assertEquals(new ZipLong(0x80706050L), xf.getAccessTime());
        assertNull(xf.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testParsePartialBufferWithFlagsBit1SetButTruncated() throws ZipException {
        // Flags indicate accessTime is present (bit 1), but buffer ends after modTime
        final byte[] data = new byte[]{
                2, // flags: only access time bit set
                // but buffer only has 1 byte length
        };
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(data, 0, 1);

        assertTrue(xf.isBit1_accessTimePresent());
        assertNull(xf.getAccessTime());
    }

    @Test(timeout = 4000)
    public void testParsePartialBufferWithFlagsBit2SetButTruncated() throws ZipException {
        // Flags indicate createTime is present (bit 2), but buffer ends after mod and access
        final byte[] data = new byte[]{
                7, // flags: mod, acc, cr
                0x01, 0x02, 0x03, 0x04, // mod (4 bytes)
                0x05, 0x06, 0x07, 0x08  // acc (4 bytes)
                // cr is missing (requires 4 more bytes)
        };
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        xf.parseFromLocalFileData(data, 0, 9);

        assertTrue(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());
        assertEquals(new ZipLong(0x04030201L), xf.getModifyTime());
        assertEquals(new ZipLong(0x08070605L), xf.getAccessTime());
        assertNull(xf.getCreateTime());
    }

    @Test(timeout = 4000)
    public void testDecoupledFlagsAndNullTimestamps() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // Explicitly set flags indicating fields are present, but keep ZipLong references null
        xf.setFlags((byte) (X5455_ExtendedTimestamp.ACCESS_TIME_BIT | X5455_ExtendedTimestamp.CREATE_TIME_BIT));

        assertFalse(xf.isBit0_modifyTimePresent());
        assertTrue(xf.isBit1_accessTimePresent());
        assertTrue(xf.isBit2_createTimePresent());

        // When accessTime and createTime are null, length must only be 1 byte (flags)
        assertEquals(1, xf.getLocalFileDataLength().getValue());
        assertEquals(1, xf.getCentralDirectoryLength().getValue());

        final byte[] local = xf.getLocalFileDataData();
        assertEquals(1, local.length);
        assertEquals(0, local[0]); // No bits written because references were null
    }

    @Test(timeout = 4000)
    public void testOnlyAccessTimeOrOnlyCreateTime() {
        final X5455_ExtendedTimestamp xfAccessOnly = new X5455_ExtendedTimestamp();
        xfAccessOnly.setAccessTime(new ZipLong(100L));
        assertEquals(5, xfAccessOnly.getLocalFileDataLength().getValue());
        assertEquals(1, xfAccessOnly.getCentralDirectoryLength().getValue());
        final byte[] localAccess = xfAccessOnly.getLocalFileDataData();
        assertEquals(X5455_ExtendedTimestamp.ACCESS_TIME_BIT, localAccess[0]);

        final X5455_ExtendedTimestamp xfCreateOnly = new X5455_ExtendedTimestamp();
        xfCreateOnly.setCreateTime(new ZipLong(200L));
        assertEquals(5, xfCreateOnly.getLocalFileDataLength().getValue());
        assertEquals(1, xfCreateOnly.getCentralDirectoryLength().getValue());
        final byte[] localCreate = xfCreateOnly.getLocalFileDataData();
        assertEquals(X5455_ExtendedTimestamp.CREATE_TIME_BIT, localCreate[0]);
    }

    @Test(timeout = 4000)
    public void testValidNegativeTimestampSigned32Bit() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // Negative epoch seconds (before 1970), within signed 32-bit range: -1000L
        final Date date1969 = new Date(-1000L);
        xf.setModifyJavaTime(date1969);
        assertEquals(date1969, xf.getModifyJavaTime());

        // Min signed 32-bit timestamp: -2147483648 seconds = -2147483648000 ms
        final Date minSigned32Date = new Date(-2147483648000L);
        xf.setModifyJavaTime(minSigned32Date);
        assertEquals(minSigned32Date, xf.getModifyJavaTime());
    }

    @Test(timeout = 4000)
    public void testValidMaxPositiveTimestampSigned32Bit() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // Max signed 32-bit timestamp: 2147483647 seconds = 2147483647000 ms
        final Date maxSigned32Date = new Date(2147483647000L);
        xf.setModifyJavaTime(maxSigned32Date);
        assertEquals(maxSigned32Date, xf.getModifyJavaTime());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect:
     * When date is before minimum 32-bit signed integer (seconds < -0x80000000L / -2147483648L),
     * unixTimeToZipLong must throw IllegalArgumentException.
     * In defective versions, only (l >= 0x100000000L) is checked, allowing negative overflow.
     */
    @Test(timeout = 4000)
    public void testModifyJavaTimeNegativeOverflowThrowsException() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // -2147483649 seconds = -2147483649000L ms (1 second below Integer.MIN_VALUE)
        final Date outOfBoundsDate = new Date(-2147483649000L);
        try {
            xf.setModifyJavaTime(outOfBoundsDate);
            fail("Time too big for 32 bits!");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    @Test(timeout = 4000)
    public void testAccessJavaTimeNegativeOverflowThrowsException() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final Date outOfBoundsDate = new Date(-2147483649000L);
        try {
            xf.setAccessJavaTime(outOfBoundsDate);
            fail("Time too big for 32 bits!");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateJavaTimeNegativeOverflowThrowsException() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final Date outOfBoundsDate = new Date(-2147483649000L);
        try {
            xf.setCreateJavaTime(outOfBoundsDate);
            fail("Time too big for 32 bits!");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    @Test(timeout = 4000)
    public void testPositiveOverflowThrowsException() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        // 0x100000000L = 4294967296 seconds (exceeds 32 unsigned bits)
        final Date outOfBoundsDate = new Date(0x100000000L * 1000L);
        try {
            xf.setModifyJavaTime(outOfBoundsDate);
            fail("Time too big for 32 bits!");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("must fit in a signed 32 bit integer"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataBufferTooShortForFlag0() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        try {
            xf.parseFromLocalFileData(new byte[0], 0, 0);
            fail("Expected ArrayIndexOutOfBoundsException for empty array");
        } catch (ZipException | ArrayIndexOutOfBoundsException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testParseFromLocalFileDataBufferTooShortForModTime() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final byte[] data = new byte[]{1, 0x11, 0x22}; // flag 1 indicates modTime, but only 2 bytes follow
        try {
            xf.parseFromLocalFileData(data, 0, data.length);
            fail("Expected ArrayIndexOutOfBoundsException for truncated modTime");
        } catch (ZipException | ArrayIndexOutOfBoundsException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        final X5455_ExtendedTimestamp xf1 = new X5455_ExtendedTimestamp();
        final X5455_ExtendedTimestamp xf2 = new X5455_ExtendedTimestamp();

        // Reflexive
        assertEquals(xf1, xf1);

        // Not equal to null or other types
        assertNotEquals(xf1, null);
        assertNotEquals(xf1, "non-instance");

        // Both empty
        assertEquals(xf1, xf2);
        assertEquals(xf1.hashCode(), xf2.hashCode());

        // Upper bits of flags ignored in equals/hashCode (only last 3 bits matter)
        xf1.setFlags((byte) 0b00000001);
        xf2.setFlags((byte) 0b11111001);
        assertEquals(xf1, xf2);
        assertEquals(xf1.hashCode(), xf2.hashCode());

        // Mismatched modTime
        xf1.setModifyTime(new ZipLong(100L));
        xf2.setModifyTime(new ZipLong(200L));
        assertNotEquals(xf1, xf2);

        // One null modTime, other non-null
        xf2.setModifyTime(null);
        assertNotEquals(xf1, xf2);
        assertNotEquals(xf2, xf1);

        // Same modTime
        xf2.setModifyTime(new ZipLong(100L));
        assertEquals(xf1, xf2);
        assertEquals(xf1.hashCode(), xf2.hashCode());

        // Mismatched accessTime
        xf1.setAccessTime(new ZipLong(300L));
        assertNotEquals(xf1, xf2);
        xf2.setAccessTime(new ZipLong(300L));
        assertEquals(xf1, xf2);
        assertEquals(xf1.hashCode(), xf2.hashCode());

        // Mismatched createTime
        xf1.setCreateTime(new ZipLong(400L));
        assertNotEquals(xf1, xf2);
        xf2.setCreateTime(new ZipLong(400L));
        assertEquals(xf1, xf2);
        assertEquals(xf1.hashCode(), xf2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() throws CloneNotSupportedException {
        final X5455_ExtendedTimestamp original = new X5455_ExtendedTimestamp();
        original.setModifyTime(new ZipLong(1000L));
        original.setAccessTime(new ZipLong(2000L));
        original.setCreateTime(new ZipLong(3000L));

        final X5455_ExtendedTimestamp clone = (X5455_ExtendedTimestamp) original.clone();
        assertNotSame(original, clone);
        assertEquals(original, clone);
        assertEquals(original.hashCode(), clone.hashCode());

        // Mutating clone does not mutate original
        clone.setModifyTime(new ZipLong(9999L));
        assertNotEquals(original.getModifyTime(), clone.getModifyTime());
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        final X5455_ExtendedTimestamp original = new X5455_ExtendedTimestamp();
        original.setModifyJavaTime(new Date(1600000000000L));
        original.setAccessJavaTime(new Date(1700000000000L));
        original.setCreateJavaTime(new Date(1800000000000L));

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final X5455_ExtendedTimestamp deserialized = (X5455_ExtendedTimestamp) ois.readObject();

        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(original.getModifyJavaTime(), deserialized.getModifyJavaTime());
        assertEquals(original.getAccessJavaTime(), deserialized.getAccessJavaTime());
        assertEquals(original.getCreateJavaTime(), deserialized.getCreateJavaTime());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentation() {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final String emptyString = xf.toString();
        assertNotNull(emptyString);
        assertTrue(emptyString.contains("0x5455 Zip Extra Field"));
        assertTrue(emptyString.contains("Flags="));
        assertFalse(emptyString.contains("Modify:["));
        assertFalse(emptyString.contains("Access:["));
        assertFalse(emptyString.contains("Create:["));

        xf.setModifyJavaTime(new Date(1000000000L));
        xf.setAccessJavaTime(new Date(2000000000L));
        xf.setCreateJavaTime(new Date(3000000000L));

        final String populatedString = xf.toString();
        assertTrue(populatedString.contains("Modify:["));
        assertTrue(populatedString.contains("Access:["));
        assertTrue(populatedString.contains("Create:["));
    }

    @Test(timeout = 4000)
    public void testResetBehaviorOnConsecutiveParses() throws ZipException {
        final X5455_ExtendedTimestamp xf = new X5455_ExtendedTimestamp();
        final byte[] fullData = new byte[]{
                7,
                0x01, 0x01, 0x01, 0x01,
                0x02, 0x02, 0x02, 0x02,
                0x03, 0x03, 0x03, 0x03
        };
        xf.parseFromLocalFileData(fullData, 0, fullData.length);
        assertNotNull(xf.getModifyTime());
        assertNotNull(xf.getAccessTime());
        assertNotNull(xf.getCreateTime());

        // Subsequent parse with empty data should cleanly reset existing state
        final byte[] emptyData = new byte[]{0};
        xf.parseFromLocalFileData(emptyData, 0, 1);
        assertEquals(0, xf.getFlags());
        assertNull(xf.getModifyTime());
        assertNull(xf.getAccessTime());
        assertNull(xf.getCreateTime());
    }
}