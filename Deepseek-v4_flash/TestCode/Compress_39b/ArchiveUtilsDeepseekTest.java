package org.apache.commons.compress.utils;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * White-box test suite for ArchiveUtils.
 * Targets: full line/branch coverage and the known defect in sanitize (not truncating to 255 chars).
 *
 * [Branch & Defect Analysis Matrix]
 * - toString: ternary isDirectory(), padding loop (7 - size.length), name concatenation
 * - matchAsciiBuffer: getBytes(US_ASCII), delegation to isEqual with offset/length
 * - toAsciiBytes/toAsciiString: charset conversion
 * - isEqual (7 overloads): minLen loop, length equality, ignoreTrailingNulls loops (both directions)
 * - isArrayZero: loop over size elements, early return on non-zero
 * - sanitize: loop over chars, isISOControl, UnicodeBlock null or SPECIALS, append '?', no length limit => defect
 * Defect: sanitize should limit length to 255 but doesn't. Test expects <=255.
 */
public class ArchiveUtilsDeepseekTest {

    // ---------- Helper: create a simple ArchiveEntry stub ----------
    private ArchiveEntry createEntry(final String name, final long size, final boolean isDirectory) {
        return new ArchiveEntry() {
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
            // not needed for toString
            @Override
            public long getLastModifiedDate() {
                return 0;
            }
        };
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testToStringFileEntry() {
        ArchiveEntry entry = createEntry("main.c", 2000, false);
        String result = ArchiveUtils.toString(entry);
        assertEquals("-     2000 main.c", result);
    }

    @Test(timeout = 4000)
    public void testToStringDirectoryEntry() {
        ArchiveEntry entry = createEntry("testfiles", 100, true);
        String result = ArchiveUtils.toString(entry);
        assertEquals("d      100 testfiles", result);
    }

    @Test(timeout = 4000)
    public void testToStringLargeSize() {
        ArchiveEntry entry = createEntry("bigfile", 123456789L, false);
        String result = ArchiveUtils.toString(entry);
        assertEquals("- 123456789 bigfile", result);
    }

    @Test(timeout = 4000)
    public void testToStringSizeZero() {
        ArchiveEntry entry = createEntry("empty.txt", 0, false);
        String result = ArchiveUtils.toString(entry);
        assertEquals("-        0 empty.txt", result);
    }

    // ==================== Partition B: Boundary & Null / Empty ====================

    @Test(timeout = 4000)
    public void testMatchAsciiBufferExact() {
        byte[] buffer = "hello".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(ArchiveUtils.matchAsciiBuffer("hello", buffer));
    }

    @Test(timeout = 4000)
    public void testMatchAsciiBufferMismatch() {
        byte[] buffer = "world".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertFalse(ArchiveUtils.matchAsciiBuffer("hello", buffer));
    }

    @Test(timeout = 4000)
    public void testMatchAsciiBufferWithOffset() {
        byte[] buffer = "prefixABC".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertTrue(ArchiveUtils.matchAsciiBuffer("ABC", buffer, 6, 3));
    }

    @Test(timeout = 4000)
    public void testMatchAsciiBufferEmptyString() {
        byte[] buffer = new byte[0];
        assertTrue(ArchiveUtils.matchAsciiBuffer("", buffer));
    }

    @Test(timeout = 4000)
    public void testToAsciiBytesNormal() {
        byte[] expected = "test".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertArrayEquals(expected, ArchiveUtils.toAsciiBytes("test"));
    }

    @Test(timeout = 4000)
    public void testToAsciiBytesEmpty() {
        assertArrayEquals(new byte[0], ArchiveUtils.toAsciiBytes(""));
    }

    @Test(timeout = 4000)
    public void testToAsciiStringFull() {
        byte[] bytes = "ASCII".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals("ASCII", ArchiveUtils.toAsciiString(bytes));
    }

    @Test(timeout = 4000)
    public void testToAsciiStringWithOffset() {
        byte[] bytes = "hello world".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals("world", ArchiveUtils.toAsciiString(bytes, 6, 5));
    }

    @Test(timeout = 4000)
    public void testToAsciiStringEmpty() {
        assertEquals("", ArchiveUtils.toAsciiString(new byte[0]));
    }

    // ==================== Partition C: isEqual variants ====================

    // Basic equal
    @Test(timeout = 4000)
    public void testIsEqualExact() {
        byte[] a = {1,2,3};
        byte[] b = {1,2,3};
        assertTrue(ArchiveUtils.isEqual(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferent() {
        byte[] a = {1,2,3};
        byte[] b = {1,2,4};
        assertFalse(ArchiveUtils.isEqual(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferentLengthsNoTrailingNulls() {
        byte[] a = {1,2,3};
        byte[] b = {1,2,3,0};
        assertFalse(ArchiveUtils.isEqual(a, b, false));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferentLengthsWithTrailingNulls_AisLonger() {
        byte[] a = {1,2,3,0,0};
        byte[] b = {1,2,3};
        assertTrue(ArchiveUtils.isEqual(a, b, true));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferentLengthsWithTrailingNulls_BisLonger() {
        byte[] a = {1,2,3};
        byte[] b = {1,2,3,0,0};
        assertTrue(ArchiveUtils.isEqual(a, b, true));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferentLengthsWithTrailingNulls_FailNonZeroA() {
        byte[] a = {1,2,3,5};
        byte[] b = {1,2,3};
        assertFalse(ArchiveUtils.isEqual(a, b, true));
    }

    @Test(timeout = 4000)
    public void testIsEqualDifferentLengthsWithTrailingNulls_FailNonZeroB() {
        byte[] a = {1,2,3};
        byte[] b = {1,2,3,5};
        assertFalse(ArchiveUtils.isEqual(a, b, true));
    }

    @Test(timeout = 4000)
    public void testIsEqualWithNullIgnoresTrailingNulls() {
        byte[] a = {1,2,3,0,0};
        byte[] b = {1,2,3};
        assertTrue(ArchiveUtils.isEqualWithNull(a, 0, 5, b, 0, 3));
    }

    @Test(timeout = 4000)
    public void testIsEqualWithOffset() {
        byte[] a = {0,1,2,3};
        byte[] b = {1,2,3};
        assertTrue(ArchiveUtils.isEqual(a, 1, 3, b, 0, 3, false));
    }

    @Test(timeout = 4000)
    public void testIsEqualSameLengthWithNullsNotIgnored() {
        byte[] a = {1,2,3,0};
        byte[] b = {1,2,3,0};
        assertTrue(ArchiveUtils.isEqual(a, b)); // default ignoreTrailingNulls=false, but lengths eq so true
    }

    // ==================== Partition C: isArrayZero ====================

    @Test(timeout = 4000)
    public void testIsArrayZeroAllZeros() {
        byte[] arr = new byte[10];
        assertTrue(ArchiveUtils.isArrayZero(arr, 5));
    }

    @Test(timeout = 4000)
    public void testIsArrayZeroNonZeroAtStart() {
        byte[] arr = {1,0,0};
        assertFalse(ArchiveUtils.isArrayZero(arr, 1));
    }

    @Test(timeout = 4000)
    public void testIsArrayZeroNonZeroLater() {
        byte[] arr = {0,0,1};
        assertFalse(ArchiveUtils.isArrayZero(arr, 3));
    }

    @Test(timeout = 4000)
    public void testIsArrayZeroSizeZero() {
        byte[] arr = {1,2,3};
        assertTrue(ArchiveUtils.isArrayZero(arr, 0));
    }

    // ==================== Partition D: sanitize – targeted coverage + defect ====================

    @Test(timeout = 4000)
    public void testSanitizeNormalString() {
        assertEquals("hello", ArchiveUtils.sanitize("hello"));
    }

    @Test(timeout = 4000)
    public void testSanitizeControlCharacter() {
        // tab (0x09) is ISO control
        assertEquals("?ello", ArchiveUtils.sanitize("\thello"));
    }

    @Test(timeout = 4000)
    public void testSanitizeUnicodeNonSpecialBlock() {
        // 'A' is basic latin, should pass
        assertEquals("A", ArchiveUtils.sanitize("A"));
    }

    @Test(timeout = 4000)
    public void testSanitizeSpecialBlock() {
        // U+FFF0 is in UnicodeBlock.SPECIALS
        String special = "\uFFF0";
        assertEquals("?", ArchiveUtils.sanitize(special));
    }

    @Test(timeout = 4000)
    public void testSanitizeNullInput() {
        // method will throw NullPointerException
        try {
            ArchiveUtils.sanitize(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== KNOWN DEFECT: sanitize should truncate to 255 chars ====================

    @Test(timeout = 4000)
    public void testSanitizeShortensString() {
        // Defect: sanitize does not limit length to 255.
        // Build a string of 300 normal characters.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append('a');
        }
        String longString = sb.toString();
        String sanitized = ArchiveUtils.sanitize(longString);
        // According to spec: "the outcome is not longer than 255 chars"
        assertTrue("Sanitized string length should be at most 255, was " + sanitized.length(),
                   sanitized.length() <= 255);
        // Additionally, the first 255 characters should be unchanged (since all are printable ASCII)
        assertEquals(longString.substring(0, 255), sanitized);
    }

    // Additional edge: mix of control chars and long string
    @Test(timeout = 4000)
    public void testSanitizeLongStringWithControls() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            sb.append((i % 2 == 0) ? 'b' : '\t'); // alternate printable and control
        }
        String result = ArchiveUtils.sanitize(sb.toString());
        assertTrue("Sanitized length must be <= 255", result.length() <= 255);
        // all control chars replaced with '?'
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            if (i % 2 == 0) {
                assertEquals('b', c);
            } else {
                assertEquals('?', c);
            }
        }
    }

    // ==================== Partition E: Object lifecycle / contract not applicable ====================
    // No equals/hashCode/clone/serialization to test.

    // Ensure private constructor coverage (optional reflection, but not required since constructor is private and class is static utility)
}