package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced White-Box JUnit 4 test suite for LookupTranslator.
 * Targets line/branch coverage and the known Lang-882 defect.
 *
 * [Branch & Defect Analysis Matrix]
 *
 * Constructor branches:
 *  - lookup null vs non-null
 *  - empty lookup array
 *  - single entry, multiple entries
 *  - shortest/longest initial values (MAX_VALUE/0) and updates
 *
 * translate() branches:
 *  - case: index + longest <= input.length()  -> max = longest
 *  - case: index + longest > input.length()   -> max = input.length() - index
 *  - loop from max down to shortest inclusive
 *  - lookupMap.get(subSeq) null vs non-null
 *  - fallback return 0 when no match found
 *
 * Defect (Lang-882): lookupMap uses CharSequence keys, but subSequence may
 * return a CharSequence instance whose equals/hashCode is not content-based,
 * causing lookup failure. We simulate this with a custom CharSequence that
 * does not override equals.
 */
public class LookupTranslatorDeepseekTest {

    // ----------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleTranslation() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"abc", "xyz"}, {"def", "123"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("abcdef", 0, out);
        assertEquals("should consume 3 characters for 'abc'", 3, consumed);
        assertEquals("xyz", out.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleMatchesGreedy() throws IOException {
        // longest match first due to descending loop
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"ab", "X"}, {"abc", "Y"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("abc", 0, out);
        assertEquals("should consume the longest match (abc length 3)", 3, consumed);
        assertEquals("Y", out.toString());
    }

    @Test(timeout = 4000)
    public void testNoMatchReturnsZero() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"xyz", "ok"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("abc", 0, out);
        assertEquals("no match, should return 0", 0, consumed);
        assertTrue(out.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testIndexBeyondInputLength() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"a", "1"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("a", 1, out);
        assertEquals("index equals input length, max becomes 0, loop condition fails", 0, consumed);
        assertTrue(out.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testPartialInputShorterThanLongest() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"ab", "X"}, {"abc", "Y"}}
        );
        StringWriter out = new StringWriter();
        // input length = 2, longest = 3, so max = 2
        int consumed = translator.translate("ab", 0, out);
        assertEquals("should match 'ab' (length 2)", 2, consumed);
        assertEquals("X", out.toString());
    }

    // ----------------------------------------------------------------
    // Partition B: BVA & extremes
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyLookupTable() throws IOException {
        // no entries => shortest=MAX_VALUE, longest=0
        LookupTranslator translator = new LookupTranslator(new CharSequence[][]{});
        StringWriter out = new StringWriter();
        int consumed = translator.translate("any", 0, out);
        assertEquals("empty lookup should always return 0", 0, consumed);
        assertTrue(out.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testNullLookupTable() throws IOException {
        // constructor handles null gracefully
        LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        assertNotNull("translator should be created", translator);
        StringWriter out = new StringWriter();
        int consumed = translator.translate("test", 0, out);
        assertEquals("null lookup should always return 0", 0, consumed);
    }

    @Test(timeout = 4000)
    public void testSingleCharacterKey() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"x", "yyy"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("x", 0, out);
        assertEquals("should consume 1 character", 1, consumed);
        assertEquals("yyy", out.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleEntriesSameLength() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"ab", "1"}, {"cd", "2"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("cd", 0, out);
        assertEquals("should match 'cd'", 2, consumed);
        assertEquals("2", out.toString());
    }

    // ----------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (Lang-882)
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLang882_CharSequenceKeyWithoutEquals() throws IOException {
        // Simulate the defect: lookup table keys are String, but the input
        // subSequence returns a CharSequence that does NOT override equals/hashCode.
        // This causes lookupMap.get(subSeq) to return null.
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"abc", "xyz"}}
        );

        // Custom CharSequence that does not implement equals/hashCode properly
        CharSequence input = new CharSequence() {
            private final String str = "abc";

            @Override
            public int length() {
                return str.length();
            }

            @Override
            public char charAt(int index) {
                return str.charAt(index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                final String sub = str.substring(start, end);
                // Return a CharSequence that also lacks equals override
                return new CharSequence() {
                    @Override
                    public int length() {
                        return sub.length();
                    }

                    @Override
                    public char charAt(int index) {
                        return sub.charAt(index);
                    }

                    @Override
                    public CharSequence subSequence(int s, int e) {
                        return sub.subSequence(s, e);
                    }

                    @Override
                    public String toString() {
                        return sub;
                    }
                    // equals/hashCode inherited from Object
                };
            }

            @Override
            public String toString() {
                return str;
            }
        };

        StringWriter out = new StringWriter();
        int consumed = translator.translate(input, 0, out);
        // The bug would cause consumption 0, but correct behavior is 3.
        assertEquals("Should consume 3 characters (abc) even with custom CharSequence", 3, consumed);
        assertEquals("Should output 'xyz'", "xyz", out.toString());
    }

    // Additional test: ensure the fix works when passing a String directly (should work)
    @Test(timeout = 4000)
    public void testLang882_WithStringInput() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"abc", "xyz"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("abc", 0, out);
        assertEquals("String input should work", 3, consumed);
        assertEquals("xyz", out.toString());
    }

    // ----------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ----------------------------------------------------------------

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullElementInLookupArray() {
        // lookup array contains a null CharSequence[] -> seq[0] throws NPE
        new LookupTranslator(new CharSequence[][]{null});
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testIncompleteEntry() {
        // seq[0] exists but seq[1] missing -> ArrayIndexOutOfBounds when accessing seq[1]
        new LookupTranslator(new CharSequence[][]{{"a"}});
    }

    @Test(timeout = 4000)
    public void testTranslationWithUnicode() throws IOException {
        LookupTranslator translator = new LookupTranslator(
            new CharSequence[][]{{"\u00E9", "eacute"}}
        );
        StringWriter out = new StringWriter();
        int consumed = translator.translate("\u00E9", 0, out);
        assertEquals("should consume 1 character", 1, consumed);
        assertEquals("eacute", out.toString());
    }

    // ----------------------------------------------------------------
    // Partition E: Object lifecycle & contract integrity (not applicable)
    // No equals/hashCode/clone needed.
    // ----------------------------------------------------------------
}