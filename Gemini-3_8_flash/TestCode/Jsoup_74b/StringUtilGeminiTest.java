package org.jsoup.helper;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: org.jsoup.helper.StringUtil
 *
 * 1. join(Collection, sep), join(Iterator, sep), join(String[], sep)
 *    - Empty iterator / collection / array -> returns ""
 *    - Single-element iterator -> returns element directly without builder
 *    - Multiple elements -> joins with separator
 *    - Non-string object iterator -> invokes toString()
 *
 * 2. padding(int width)
 *    - width < 0 -> throws IllegalArgumentException
 *    - 0 <= width < 21 -> returns memoised StringUtil.padding[width]
 *    - width >= 21 -> allocates new char array filled with spaces
 *    - Boundary checks: width = 0, 20, 21, 22
 *
 * 3. isBlank(String)
 *    - null or empty string -> true
 *    - all whitespace (HTML spaces: ' ', '\t', '\n', '\f', '\r') -> true
 *    - non-whitespace present -> false
 *    - code points > Character.MAX_VALUE (surrogate pairs) handling
 *
 * 4. isNumeric(String)
 *    - null or empty string -> false
 *    - all digits (0-9) -> true
 *    - contains non-digit (including signs, decimals, letters, spaces) -> false
 *
 * 5. isWhitespace(int c) & isActuallyWhitespace(int c)
 *    - HTML whitespace: ' ', '\t', '\n', '\f', '\r' -> true
 *    - NBSP (160): false for isWhitespace, true for isActuallyWhitespace
 *    - Other characters -> false
 *
 * 6. normaliseWhitespace(String) & appendNormalisedWhitespace(...)
 *    - Whitespace collapse: multiple spaces/tabs/newlines collapsed to single space ' '
 *    - stripLeading = true: leading whitespace dropped before first non-white char
 *    - stripLeading = false: leading whitespace collapsed to single leading space
 *    - Consecutive whitespaces at different positions (beginning, middle, end)
 *    - Character codePoint iteration with supplementary characters
 *
 * 7. in(String needle, String... haystack) & inSorted(String needle, String[] haystack)
 *    - needle present vs not present
 *    - empty haystack, null needle
 *
 * 8. resolve(URL base, String relUrl) & resolve(String baseUrl, String relUrl)
 *    - Relative query string starting with '?' -> path concatenation logic
 *    - Base file without leading slash combined with './' -> root prefix fix
 *    - Valid base + relative path -> absolute URL
 *    - Malformed base + valid absolute relUrl -> returns relUrl
 *    - Malformed base + malformed relUrl -> returns ""
 *
 * 9. stringBuilder()
 *    - ThreadLocal reuse
 *    - Clears previous contents
 *    - Re-allocates if builder grows larger than MaxCachedBuilderSize (8192)
 *
 * 10. Defects4J Targeted Defect (testNormalizesInvisiblesInText / Invisible characters handling):
 *     - Characters like soft hyphen (\u00AD), zero-width space (\u200B), zero-width non-joiner (\u200C),
 *       zero-width joiner (\u200D) should be normalized/stripped according to HTML specification
 *       expectations (Defects4J known failure where invisibles remain in text normalization).
 */
public class StringUtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testJoinCollection() {
        List<String> list = Arrays.asList("one", "two", "three");
        String result = StringUtil.join(list, ", ");
        assertEquals("one, two, three", result);
    }

    @Test(timeout = 4000)
    public void testJoinArray() {
        String[] arr = new String[]{"alpha", "beta", "gamma"};
        String result = StringUtil.join(arr, "-");
        assertEquals("alpha-beta-gamma", result);
    }

    @Test(timeout = 4000)
    public void testJoinSingleElement() {
        List<String> single = Collections.singletonList("lone");
        assertEquals("lone", StringUtil.join(single, ", "));
        assertEquals("lone", StringUtil.join(new String[]{"lone"}, ", "));
    }

    @Test(timeout = 4000)
    public void testJoinNonStringObjects() {
        List<Object> objects = new ArrayList<Object>();
        objects.add(1);
        objects.add(true);
        objects.add(3.14);
        assertEquals("1; true; 3.14", StringUtil.join(objects, "; "));
    }

    @Test(timeout = 4000)
    public void testPaddingMemoisedAndDynamic() {
        assertEquals("", StringUtil.padding(0));
        assertEquals(" ", StringUtil.padding(1));
        assertEquals("                    ", StringUtil.padding(20)); // length 20: memoised boundary

        // Dynamic allocation boundary (width >= 21)
        String pad21 = StringUtil.padding(21);
        assertEquals(21, pad21.length());
        assertEquals("                     ", pad21);

        String pad30 = StringUtil.padding(30);
        assertEquals(30, pad30.length());
        for (int i = 0; i < pad30.length(); i++) {
            assertEquals(' ', pad30.charAt(i));
        }
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceAndIsActuallyWhitespace() {
        int[] standardWhitespace = {' ', '\t', '\n', '\f', '\r'};
        for (int c : standardWhitespace) {
            assertTrue("Expected whitespace for: " + c, StringUtil.isWhitespace(c));
            assertTrue("Expected actuallyWhitespace for: " + c, StringUtil.isActuallyWhitespace(c));
        }

        // NBSP (160 / 0xA0)
        assertFalse(StringUtil.isWhitespace(160));
        assertTrue(StringUtil.isActuallyWhitespace(160));

        // Regular character
        assertFalse(StringUtil.isWhitespace('a'));
        assertFalse(StringUtil.isActuallyWhitespace('a'));
        assertFalse(StringUtil.isWhitespace('0'));
        assertFalse(StringUtil.isActuallyWhitespace('0'));
    }

    @Test(timeout = 4000)
    public void testIsBlankTrueCases() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertTrue(StringUtil.isBlank("   "));
        assertTrue(StringUtil.isBlank("\t\r\n\f "));
    }

    @Test(timeout = 4000)
    public void testIsBlankFalseCases() {
        assertFalse(StringUtil.isBlank("a"));
        assertFalse(StringUtil.isBlank("  b  "));
        assertFalse(StringUtil.isBlank("\t\r\nhello\f"));
    }

    @Test(timeout = 4000)
    public void testIsNumeric() {
        assertTrue(StringUtil.isNumeric("0"));
        assertTrue(StringUtil.isNumeric("1234567890"));
        assertFalse(StringUtil.isNumeric(""));
        assertFalse(StringUtil.isNumeric(null));
        assertFalse(StringUtil.isNumeric("123a45"));
        assertFalse(StringUtil.isNumeric("-123"));
        assertFalse(StringUtil.isNumeric("12.3"));
        assertFalse(StringUtil.isNumeric(" 123 "));
    }

    @Test(timeout = 4000)
    public void testInAndInSorted() {
        String[] hay = {"apple", "banana", "cherry"};
        assertTrue(StringUtil.in("banana", hay));
        assertFalse(StringUtil.in("orange", hay));
        assertFalse(StringUtil.in("apple")); // empty varargs

        assertTrue(StringUtil.inSorted("banana", hay));
        assertFalse(StringUtil.inSorted("orange", hay));
        assertFalse(StringUtil.inSorted("aardvark", hay));
    }

    @Test(timeout = 4000)
    public void testNormaliseWhitespaceBasic() {
        assertEquals("a b c", StringUtil.normaliseWhitespace("a   b   c"));
        assertEquals("a b c", StringUtil.normaliseWhitespace("  a \t\r\n b   c  "));
        assertEquals("hello world", StringUtil.normaliseWhitespace("hello\u00A0world")); // NBSP
    }

    @Test(timeout = 4000)
    public void testAppendNormalisedWhitespaceStripLeading() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello   world   ", true);
        assertEquals("hello world ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNormalisedWhitespaceKeepLeading() {
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   hello   world   ", false);
        assertEquals(" hello world ", sb.toString());
    }

    @Test(timeout = 4000)
    public void testAppendNormalisedWhitespaceSupplementaryCharacters() {
        // Surrogate pair: 𝄞 (U+1D11E, MUSICAL SYMBOL G CLEF)
        String gClef = new String(Character.toChars(0x1D11E));
        StringBuilder sb = new StringBuilder();
        StringUtil.appendNormalisedWhitespace(sb, "   " + gClef + "   ", true);
        assertEquals(gClef + " ", sb.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & URL Resolution
    // =========================================================================

    @Test(timeout = 4000)
    public void testJoinEmptyIteratorAndCollection() {
        assertEquals("", StringUtil.join(Collections.emptyList(), ","));
        assertEquals("", StringUtil.join(new String[0], ","));
        assertEquals("", StringUtil.join(new Iterator<String>() {
            public boolean hasNext() { return false; }
            public String next() { return null; }
            public void remove() {}
        }, ","));
    }

    @Test(timeout = 4000)
    public void testResolveUrlSuccess() throws MalformedURLException {
        URL base = new URL("http://example.com/dir/page.html");
        URL resolved = StringUtil.resolve(base, "other.html");
        assertEquals("http://example.com/dir/other.html", resolved.toExternalForm());

        URL resolvedAbs = StringUtil.resolve(base, "http://another.com/test");
        assertEquals("http://another.com/test", resolvedAbs.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testResolveUrlQueryWorkaround() throws MalformedURLException {
        URL base = new URL("http://example.com/file");
        URL resolved = StringUtil.resolve(base, "?foo=bar");
        assertEquals("http://example.com/file?foo=bar", resolved.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testResolveUrlDotRelativeWorkaround() throws MalformedURLException {
        // Base with file not starting with '/' (protocol handler custom cases)
        URL base = new URL("http", "example.com", 80, "file.html");
        URL resolved = StringUtil.resolve(base, "./other.html");
        assertEquals("http://example.com/other.html", resolved.toExternalForm());
    }

    @Test(timeout = 4000)
    public void testResolveStringBase() {
        String base = "http://example.com/path/index.html";
        assertEquals("http://example.com/path/sub.html", StringUtil.resolve(base, "sub.html"));
        assertEquals("http://example.com/path/index.html?query=1", StringUtil.resolve(base, "?query=1"));
    }

    @Test(timeout = 4000)
    public void testResolveStringInvalidBaseValidRel() {
        // Base is invalid URL, but relative is a valid absolute URL
        String result = StringUtil.resolve("invalid-url", "http://example.com/absolute");
        assertEquals("http://example.com/absolute", result);
    }

    @Test(timeout = 4000)
    public void testResolveStringBothInvalid() {
        String result = StringUtil.resolve("not_a_url", "neither_am_i");
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testStringBuilderPoolingAndReset() {
        StringBuilder sb1 = StringUtil.stringBuilder();
        sb1.append("some data");
        StringBuilder sb2 = StringUtil.stringBuilder();
        assertSame("Should return cached instance for same thread", sb1, sb2);
        assertEquals("Should be reset to empty", 0, sb2.length());

        // Fill beyond 8 * 1024 to trigger max size branch
        for (int i = 0; i < 9000; i++) {
            sb2.append('x');
        }
        assertTrue(sb2.length() > 8192);

        StringBuilder sb3 = StringUtil.stringBuilder();
        assertNotSame("Exceeded builder should be replaced", sb2, sb3);
        assertEquals(0, sb3.length());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * org.jsoup.nodes.ElementTest::testNormalizesInvisiblesInText
     * Expected: "Thisisonelongword"
     * Defective: "This\u00ADis\u200Bone\u200Clong\u200Dword"
     *
     * Invisible characters:
     * - \u00AD (Soft Hyphen)
     * - \u200B (Zero Width Space)
     * - \u200C (Zero Width Non-Joiner)
     * - \u200D (Zero Width Joiner)
     *
     * Normalization should strip these invisible control characters from the text.
     */
    @Test(timeout = 4000)
    public void testNormaliseWhitespaceRemovesInvisibleCharacters() {
        String inputWithInvisibles = "This\u00ADis\u200Bone\u200Clong\u200Dword";
        String normalized = StringUtil.normaliseWhitespace(inputWithInvisibles);
        assertEquals("Thisisonelongword", normalized);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPaddingNegativeWidthThrowsException() {
        StringUtil.padding(-1);
    }

    @Test(timeout = 4000)
    public void testClassInstantiationForCoverage() {
        // Verify class constructor integrity
        StringUtil util = new StringUtil();
        assertNotNull(util);
    }
}