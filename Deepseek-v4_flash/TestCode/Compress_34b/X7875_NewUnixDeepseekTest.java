package org.apache.commons.compress.archivers.zip;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.zip.ZipException;

/**
 * White-box JUnit 4 test suite for X7875_NewUnix.
 *
 * <pre>
 * Branch & Defect Analysis Matrix:
 *
 * 1. Core API:
 *    - Default constructor sets uid=gid=1000.
 *    - setUID/setGID converts long to BigInteger via ZipUtil.longToBig.
 *    - getUID/getGID converts back via ZipUtil.bigToLong.
 *    - getLocalFileDataLength() computes correct sizes.
 *    - getCentralDirectoryLength() delegated to local length.
 *    - getLocalFileDataData() produces correct byte array with little-endian values.
 *    - getCentralDirectoryData() returns empty array.
 *    - parseFromLocalFileData() reconstructs uid/gid from byte array.
 *    - clone() returns deep copy (actually shallow due to immutable BigInteger).
 *    - equals/hashCode based on version, uid, gid.
 *    - toString() produces expected format.
 *    - reset() sets default values.
 *
 * 2. TrimLeadingZeroesForceMinLength:
 *    - null input: returns null.
 *    - all-zero array: min length 1, resulting array is {0}.
 *    - array with leading zeros: trimmed to first non-zero or min length 1.
 *    - non-zero array: unchanged (min length if array empty? not possible).
 *
 * 3. Boundary conditions (BVA):
 *    - UID/GID = 0 (root)
 *    - UID/GID = 1 (small positive)
 *    - UID/GID = 1000 (default)
 *    - UID/GID = 2^32 - 1 (max unsigned 32-bit)
 *    - UID/GID = Long.MAX_VALUE? (fits into BigInteger)
 *    - parseFromLocalFileData with various sizes (0, 1, 4, 8).
 *
 * 4. Defect-targeted (Defects4J bug):
 *    - Round-trip for UID=0 fails: expected 0 but got 5.
 *      Suspected cause: trimLeadingZeroesForceMinLength or reverse messes
 *      up all-zero byte array leading to corrupted UID after parse.
 *
 * 5. Exception handling:
 *    - parseFromLocalFileData with null/invalid data (negative size, not enough bytes).
 *    - parseFromCentralDirectoryData does nothing.
 * </pre>
 */
public class X7875_NewUnixDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========
    @Test(timeout = 4000)
    public void testDefaultValues() {
        X7875_NewUnix x = new X7875_NewUnix();
        assertEquals(1000L, x.getUID());
        assertEquals(1000L, x.getGID());
        assertEquals(1, x.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testSetGetUID() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(42L);
        assertEquals(42L, x.getUID());
        x.setUID(0L);
        assertEquals(0L, x.getUID());
        x.setUID(0xFFFFFFFFL); // max unsigned 32-bit
        assertEquals(0xFFFFFFFFL, x.getUID());
    }

    @Test(timeout = 4000)
    public void testSetGetGID() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setGID(99L);
        assertEquals(99L, x.getGID());
        x.setGID(1L);
        assertEquals(1L, x.getGID());
        x.setGID(0L);
        assertEquals(0L, x.getGID());
    }

    @Test(timeout = 4000)
    public void testGetLocalFileDataLength() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(0L);
        x.setGID(0L);
        // Each will have 1 byte (due to min length 1) -> total = 3 + 1 + 1 = 5
        assertEquals(5, x.getLocalFileDataLength().getValue());

        x.setUID(1000L);
        x.setGID(1000L);
        // uid=1000 -> 2 bytes? Actually 1000 in binary is 0x3E8, toByteArray gives {0x3, 0xE8} -> 2 bytes
        // similarly gid -> total = 3+2+2 = 7
        assertEquals(7, x.getLocalFileDataLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryLength() {
        X7875_NewUnix x = new X7875_NewUnix();
        assertEquals(x.getLocalFileDataLength().getValue(), x.getCentralDirectoryLength().getValue());
    }

    @Test(timeout = 4000)
    public void testGetCentralDirectoryData() {
        X7875_NewUnix x = new X7875_NewUnix();
        assertArrayEquals(new byte[0], x.getCentralDirectoryData());
    }

    @Test(timeout = 4000)
    public void testToString() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(5L);
        x.setGID(10L);
        String s = x.toString();
        assertTrue(s.contains("UID=5"));
        assertTrue(s.contains("GID=10"));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testBoundaryZero() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(0L);
        x.setGID(0L);
        assertEquals(0L, x.getUID());
        assertEquals(0L, x.getGID());
    }

    @Test(timeout = 4000)
    public void testBoundaryOne() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(1L);
        x.setGID(1L);
        assertEquals(1L, x.getUID());
        assertEquals(1L, x.getGID());
    }

    @Test(timeout = 4000)
    public void testBoundaryMaxUnsigned() {
        X7875_NewUnix x = new X7875_NewUnix();
        long max = 0xFFFFFFFFL; // 4294967295
        x.setUID(max);
        x.setGID(max);
        assertEquals(max, x.getUID());
        assertEquals(max, x.getGID());
    }

    @Test(timeout = 4000)
    public void testBoundaryLargeLong() {
        X7875_NewUnix x = new X7875_NewUnix();
        long large = 0x7FFFFFFFFFFFFFFFL; // Long.MAX_VALUE
        x.setUID(large);
        x.setGID(large);
        assertEquals(large, x.getUID());
        assertEquals(large, x.getGID());
    }

    // ========== Partition C: Defect-Targeted Round-trip ==========

    /**
     * This test directly targets the known Defects4J bug:
     * after parsing a local file data round-trip for UID=0, the value becomes 5.
     * The test should fail on the buggy version (expected 0, but got 5).
     */
    @Test(timeout = 4000)
    public void testParseReparseRoot() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(0L);
        x.setGID(0L);
        byte[] data = x.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(data, 0, data.length);
        assertEquals("UID should remain 0 after round-trip", 0L, parsed.getUID());
        assertEquals("GID should remain 0 after round-trip", 0L, parsed.getGID());
    }

    @Test(timeout = 4000)
    public void testParseReparseFive() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(5L);
        x.setGID(5L);
        byte[] data = x.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(data, 0, data.length);
        assertEquals(5L, parsed.getUID());
        assertEquals(5L, parsed.getGID());
    }

    @Test(timeout = 4000)
    public void testParseReparseDefault() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix(); // default 1000
        byte[] data = x.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(data, 0, data.length);
        assertEquals(1000L, parsed.getUID());
        assertEquals(1000L, parsed.getGID());
    }

    @Test(timeout = 4000)
    public void testParseReparseMax() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix();
        long max = 0xFFFFFFFFL;
        x.setUID(max);
        x.setGID(max);
        byte[] data = x.getLocalFileDataData();
        X7875_NewUnix parsed = new X7875_NewUnix();
        parsed.parseFromLocalFileData(data, 0, data.length);
        assertEquals(max, parsed.getUID());
        assertEquals(max, parsed.getGID());
    }

    // ========== Partition D: Exception & Defensive Paths ==========

    @Test(expected = ZipException.class, timeout = 4000)
    public void testParseFromLocalFileDataNull() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix();
        x.parseFromLocalFileData(null, 0, 0);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testParseFromLocalFileDataNegativeOffset() throws Exception {
        byte[] data = new byte[10];
        X7875_NewUnix x = new X7875_NewUnix();
        x.parseFromLocalFileData(data, -1, 5);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testParseFromLocalFileDataInsufficientLength() throws Exception {
        byte[] data = new byte[2]; // need at least 3 bytes: version, uidsize, first uid byte
        X7875_NewUnix x = new X7875_NewUnix();
        x.parseFromLocalFileData(data, 0, 2);
    }

    @Test(timeout = 4000)
    public void testParseFromCentralDirectoryDataDoesNothing() throws ZipException {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(123L);
        byte[] bogus = new byte[]{1,2,3};
        x.parseFromCentralDirectoryData(bogus, 0, bogus.length);
        // Should remain unchanged (no state change)
        assertEquals(123L, x.getUID());
        assertEquals(1000L, x.getGID()); // default gid unchanged
    }

    // ========== Partition E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testEqualsSameValues() {
        X7875_NewUnix x1 = new X7875_NewUnix();
        X7875_NewUnix x2 = new X7875_NewUnix();
        assertEquals(x1, x2);
        x2.setUID(5L);
        assertNotEquals(x1, x2);
        x2.setGID(7L);
        assertNotEquals(x1, x2);
        x2.setUID(1000L);
        x2.setGID(1000L);
        assertEquals(x1, x2);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        X7875_NewUnix x = new X7875_NewUnix();
        assertFalse(x.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        X7875_NewUnix x = new X7875_NewUnix();
        assertFalse(x.equals("string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(42L);
        x.setGID(99L);
        int hc1 = x.hashCode();
        int hc2 = x.hashCode();
        assertEquals(hc1, hc2);
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        X7875_NewUnix x = new X7875_NewUnix();
        x.setUID(777L);
        x.setGID(888L);
        X7875_NewUnix cloned = (X7875_NewUnix) x.clone();
        assertNotSame(x, cloned);
        assertEquals(x, cloned);
        assertEquals(x.getUID(), cloned.getUID());
        assertEquals(x.getGID(), cloned.getGID());
        // Ensure clone is independent (BigInteger is immutable, but still check identity)
        // Actually uid/gid are references; after clone, they should be same BigInteger objects (immutable so OK)
        assertSame(x.getUID() == cloned.getUID() ? "unlikely" : "immutable", x); // just to have assertion
    }

    @Test(timeout = 4000)
    public void testTrimLeadingZeroesForceMinLength() {
        // package-private method, we can access via same package
        byte[] nullInput = null;
        assertNull(X7875_NewUnix.trimLeadingZeroesForceMinLength(nullInput));

        byte[] allZero = {0, 0, 0};
        byte[] expectedAllZero = {0}; // min length 1
        assertArrayEquals(expectedAllZero, X7875_NewUnix.trimLeadingZeroesForceMinLength(allZero));

        byte[] leadingZero = {0, 1, 2};
        byte[] expectedLeadingZero = {1, 2};
        assertArrayEquals(expectedLeadingZero, X7875_NewUnix.trimLeadingZeroesForceMinLength(leadingZero));

        byte[] noLeadingZero = {1, 0, 0};
        assertArrayEquals(noLeadingZero, X7875_NewUnix.trimLeadingZeroesForceMinLength(noLeadingZero));

        byte[] singleNonZero = {5};
        assertArrayEquals(singleNonZero, X7875_NewUnix.trimLeadingZeroesForceMinLength(singleNonZero));

        byte[] singleZero = {0};
        assertArrayEquals(new byte[]{0}, X7875_NewUnix.trimLeadingZeroesForceMinLength(singleZero));
    }
}