package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Date;
import org.apache.commons.compress.archivers.ArchiveEntry;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Target Defect (COMPRESS-358 / ArchiveUtilsTest::sanitizeShortensString):
 *    - Method: sanitize(String)
 *    - Defect: When an input string exceeds 255 characters, the specification states
 *      "the outcome is not longer than 255 chars". The defective implementation does not truncate
 *      and instead returns the full length string.
 *    - Expected Behavior: Strings longer than 255 chars are truncated to 252 characters followed by "...".
 *
 * 2. Coverage & Branch Partitioning:
 *    - toString(ArchiveEntry):
 *      * Directory vs file indicator ('d' vs '-')
 *      * Size formatting with padding (size length < 7, == 7, > 7)
 *    - matchAsciiBuffer(String, byte[], int, int) & matchAsciiBuffer(String, byte[]):
 *      * Full match, mismatch in characters, mismatch in lengths, zero length.
 *    - toAsciiBytes(String) & toAsciiString(byte[]) & toAsciiString(byte[], int, int):
 *      * Conversion round-trips, sub-array slicing, empty strings/arrays.
 *    - isEqual(byte[], int, int, byte[], int, int, boolean) and overloads:
 *      * Identical arrays and sub-slices.
 *      * Mismatch within minLen.
 *      * Equal lengths vs different lengths.
 *      * ignoreTrailingNulls = false vs true.
 *      * length1 > length2 with trailing zeros vs non-zero trailing bytes.
 *      * length1 < length2 with trailing zeros vs non-zero trailing bytes.
 *    - isEqualWithNull(byte[], int, int, byte[], int, int):
 *      * Verification of trailing null handling delegator.
 *    - isArrayZero(byte[], int):
 *      * size == 0 (true), all zeros (true), first byte non-zero (false),
 *        last checked byte non-zero (false), non-zero byte beyond size (true).
 *    - sanitize(String):
 *      * Printable ASCII characters.
 *      * ISO control characters (replaced with '?').
 *      * Unicode characters with Specials block (replaced with '?').
 *      * Printable Non-ASCII Unicode characters (preserved).
 *      * Strings > 255 chars (truncated to 255 chars ending in "...").
 *    - Private Constructor:
 *      * Reflection invocation for 100% utility class coverage.
 */
public class ArchiveUtilsGeminiTest {

    private static class MockArchiveEntry implements ArchiveEntry {
        private final String name;
        private final long size;
        private final boolean isDirectory;

        MockArchiveEntry(final String name, final long size, final boolean isDirectory) {
            this.name = name;
            this.size = size;
            this.isDirectory = isDirectory;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public boolean isDirectory() {
            return isDirectory;
        }

        @Override
        public Date getLastModifiedDate() {
            return null;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        final ArchiveEntry fileEntry = new MockArchiveEntry("main.c", 2000L, false);
        assertEquals("-    2000 main.c", ArchiveUtils.toString(fileEntry));

        final ArchiveEntry dirEntry = new MockArchiveEntry("testfiles", 100L, true);
        assertEquals("d     100 testfiles", ArchiveUtils.toString(dirEntry));

        // Size with exact length 7 (no padding loop iterations)
        final ArchiveEntry exact7 = new MockArchiveEntry("file7", 1234567L, false);
        assertEquals("- 1234567 file7", ArchiveUtils.toString(exact7));

        // Size with length greater than 7
        final ArchiveEntry largeSize = new MockArchiveEntry("huge.bin", 123456789L, false);
        assertEquals("- 123456789 huge.bin", ArchiveUtils.toString(largeSize));
    }

    @Test(timeout = 4000)
    public void testAsciiConversions() {
        final String original = "Apache Commons Compress";
        final byte[] bytes = ArchiveUtils.toAsciiBytes(original);
        assertNotNull(bytes);
        assertEquals(original.length(), bytes.length);

        final String reconstructed = ArchiveUtils.toAsciiString(bytes);
        assertEquals(original, reconstructed);

        final String sliced = ArchiveUtils.toAsciiString(bytes, 7, 7);
        assertEquals("Commons", sliced);
    }

    @Test(timeout = 4000)
    public void testMatchAsciiBuffer() {
        final byte[] buffer = "abcdef".getBytes();
        assertTrue(ArchiveUtils.matchAsciiBuffer("abcdef", buffer));
        assertFalse(ArchiveUtils.matchAsciiBuffer("abc", buffer));
        assertFalse(ArchiveUtils.matchAsciiBuffer("xyz", buffer));

        assertTrue(ArchiveUtils.matchAsciiBuffer("bcd", buffer, 1, 3));
        assertFalse(ArchiveUtils.matchAsciiBuffer("bcd", buffer, 1, 2));
        assertFalse(ArchiveUtils.matchAsciiBuffer("bce", buffer, 1, 3));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsArrayZeroBoundaries() {
        final byte[] allZeros = new byte[]{0, 0, 0, 0, 0};
        assertTrue(ArchiveUtils.isArrayZero(allZeros, 0));
        assertTrue(ArchiveUtils.isArrayZero(allZeros, 5));

        final byte[] nonZeroFirst = new byte[]{1, 0, 0, 0};
        assertFalse(ArchiveUtils.isArrayZero(nonZeroFirst, 4));

        final byte[] nonZeroLast = new byte[]{0, 0, 0, 5};
        assertFalse(ArchiveUtils.isArrayZero(nonZeroLast, 4));
        assertTrue(ArchiveUtils.isArrayZero(nonZeroLast, 3));
    }

    @Test(timeout = 4000)
    public void testIsEqualSubArraysAndOffsets() {
        final byte[] b1 = new byte[]{1, 2, 3, 4, 5};
        final byte[] b2 = new byte[]{9, 1, 2, 3, 4, 8};

        assertTrue(ArchiveUtils.isEqual(b1, 0, 4, b2, 1, 4));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 5, b2, 1, 4));
        assertFalse(ArchiveUtils.isEqual(b1, 0, 4, b2, 1, 3));
    }

    @Test(timeout = 4000)
    public void testIsEqualWithTrailingNulls() {
        final byte[] base = new byte[]{1, 2, 3};
        final byte[] withZeros = new byte[]{1, 2, 3, 0, 0};
        final byte[] withNonZero = new byte[]{1, 2, 3, 0, 4};

        // Overload with boolean
        assertTrue(ArchiveUtils.isEqual(base, withZeros, true));
        assertTrue(ArchiveUtils.isEqual(withZeros, base, true));
        assertFalse(ArchiveUtils.isEqual(base, withZeros, false));
        assertFalse(ArchiveUtils.isEqual(base, withNonZero, true));
        assertFalse(ArchiveUtils.isEqual(withNonZero, base, true));

        // Overload isEqual(byte[], byte[])
        assertFalse(ArchiveUtils.isEqual(base, withZeros));
        assertTrue(ArchiveUtils.isEqual(base, new byte[]{1, 2, 3}));

        // isEqualWithNull method
        assertTrue(ArchiveUtils.isEqualWithNull(base, 0, base.length, withZeros, 0, withZeros.length));
        assertTrue(ArchiveUtils.isEqualWithNull(withZeros, 0, withZeros.length, base, 0, base.length));
        assertFalse(ArchiveUtils.isEqualWithNull(base, 0, base.length, withNonZero, 0, withNonZero.length));
        assertFalse(ArchiveUtils.isEqualWithNull(withNonZero, 0, withNonZero.length, base, 0, base.length));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the defect where sanitize(String) fails to shorten strings longer
     * than 255 characters to a maximum length of 255 characters ending in "...".
     */
    @Test(timeout = 4000)
    public void testSanitizeShortensStringDefect() {
        final String chunk = "012345678901234567890123456789012345678901234567890123456789"; // 60 chars
        final String input = chunk + chunk + chunk + chunk + chunk; // 300 chars
        final String expected = input.substring(0, 252) + "...";

        final String actual = ArchiveUtils.sanitize(input);

        assertEquals("Sanitized string exceeding 255 chars must be shortened to 255 chars ending in '...'",
                expected, actual);
        assertEquals(255, actual.length());
    }

    @Test(timeout = 4000)
    public void testSanitizeCharacterFiltering() {
        assertEquals("hello world", ArchiveUtils.sanitize("hello world"));

        // ISO Controls replaced with '?'
        assertEquals("hello??world", ArchiveUtils.sanitize("hello\u0000\u0007world"));
        assertEquals("?tab?nl?cr?", ArchiveUtils.sanitize("\ttab\nnl\rcr\u001F"));

        // Specials unicode block replaced with '?'
        assertEquals("test?", ArchiveUtils.sanitize("test\uFFF9"));
        assertEquals("special?", ArchiveUtils.sanitize("special\uFFFF"));

        // Non-control international characters should be preserved
        assertEquals("Caff\u00E9 \u0422\u0435\u0441\u0442",
                ArchiveUtils.sanitize("Caff\u00E9 \u0422\u0435\u0441\u0442"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndZeroLengthInputs() {
        final byte[] empty = new byte[0];
        assertEquals("", ArchiveUtils.toAsciiString(empty));
        assertEquals("", ArchiveUtils.toAsciiString(empty, 0, 0));
        assertArrayEquals(empty, ArchiveUtils.toAsciiBytes(""));
        assertTrue(ArchiveUtils.matchAsciiBuffer("", empty));
        assertTrue(ArchiveUtils.isEqual(empty, empty));
        assertTrue(ArchiveUtils.isEqual(empty, empty, true));
        assertTrue(ArchiveUtils.isEqual(empty, empty, false));
        assertTrue(ArchiveUtils.isArrayZero(empty, 0));
        assertEquals("", ArchiveUtils.sanitize(""));
    }

    @Test(timeout = 4000)
    public void testIsEqualOffsetBounds() {
        final byte[] b1 = new byte[]{1, 2, 3};
        final byte[] b2 = new byte[]{1, 2, 4};

        try {
            ArchiveUtils.isEqual(b1, 0, 4, b2, 0, 4);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (final ArrayIndexOutOfBoundsException expected) {
            // expected
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructorReflection() throws Exception {
        final Constructor<ArchiveUtils> constructor = ArchiveUtils.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        final ArchiveUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }
}