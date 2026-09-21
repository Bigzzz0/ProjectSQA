package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang.WordUtils
 * Defect ID: Lang (Defects4J known issue in WordUtils.abbreviate)
 * Root Cause: When 'lower' exceeds str.length(), 'upper' gets adjusted to str.length(),
 *             but then 'upper < lower' causes 'upper' to be set to 'lower', exceeding
 *             str.length() and causing StringIndexOutOfBoundsException on str.substring(0, upper).
 *
 * Coverage Target Breakdown:
 * - wrap(str, wrapLength, newLineStr, wrapLongWords):
 *     * str == null (null branch)
 *     * newLineStr == null (default SystemUtils.LINE_SEPARATOR) vs explicit newLineStr
 *     * wrapLength < 1 (forced boundary wrapLength = 1)
 *     * leading space handling (offset increment & continue)
 *     * normal wrap on whitespace (spaceToWrapAt >= offset)
 *     * long words: wrapLongWords = true (forced chunk split)
 *     * long words: wrapLongWords = false (extend to next space or to string end)
 *     * remaining tail appended correctly
 * - capitalize / capitalizeFully:
 *     * null / empty input
 *     * delimiters == null (default Character.isWhitespace)
 *     * delimiters.length == 0 (early exit returning str)
 *     * custom delimiters (e.g., '.', '-', '/')
 *     * title-casing vs lowercasing rest of word
 * - uncapitalize:
 *     * null / empty input
 *     * delimiters == null vs empty delimiters vs custom delimiters
 *     * first character after delimiter uncapitalized, others untouched
 * - swapCase:
 *     * null / empty input
 *     * uppercase -> lowercase
 *     * titlecase -> lowercase (Unicode titlecase character \u01C5)
 *     * lowercase at word boundary (start/whitespace) -> toTitleCase
 *     * lowercase within word -> toUpperCase
 *     * non-letter characters unchanged
 * - initials:
 *     * null / empty input
 *     * delimiters == null vs empty delimiter array (returns "") vs custom delimiters
 *     * multiple consecutive delimiters (gap compression)
 * - abbreviate:
 *     * null / empty input
 *     * upper == -1 (no upper limit desired)
 *     * upper > str.length()
 *     * upper < lower (raise upper to lower)
 *     * lower > str.length() (CRITICAL DEFECT PATH: StringIndexOutOfBoundsException)
 *     * index == -1 vs index > upper vs index <= upper
 *     * appendToEnd == null, empty, or custom string
 */
public class WordUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Normal Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testWrapNormalWrapping() {
        String input = "Here is a simple sentence to be wrapped across lines.";
        String wrapped = WordUtils.wrap(input, 20, "\n", false);
        assertEquals("Here is a simple\nsentence to be\nwrapped across\nlines.", wrapped);
    }

    @Test(timeout = 4000)
    public void testWrapDefaultNewline() {
        String input = "Hello World";
        String wrapped = WordUtils.wrap(input, 5);
        assertEquals("Hello" + SystemUtils.LINE_SEPARATOR + "World", wrapped);
    }

    @Test(timeout = 4000)
    public void testWrapLongWordsTrue() {
        String input = "longwordthatcannotfit into normal space";
        String wrapped = WordUtils.wrap(input, 10, "\n", true);
        assertEquals("longwordth\natcannotfi\nt into\nnormal\nspace", wrapped);
    }

    @Test(timeout = 4000)
    public void testWrapLongWordsFalseExtendsToNextSpace() {
        String input = "supercalifragilisticexpialidocious then short words";
        String wrapped = WordUtils.wrap(input, 10, "\n", false);
        assertEquals("supercalifragilisticexpialidocious\nthen short\nwords", wrapped);
    }

    @Test(timeout = 4000)
    public void testWrapLongWordsFalseNoSubsequentSpace() {
        String input = "supercalifragilisticexpialidocious";
        String wrapped = WordUtils.wrap(input, 10, "\n", false);
        assertEquals("supercalifragilisticexpialidocious", wrapped);
    }

    @Test(timeout = 4000)
    public void testCapitalizeAndCapitalizeFully() {
        assertEquals("I Am Fine", WordUtils.capitalize("i am fine"));
        assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
        assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", new char[]{'.'}));
        assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", new char[]{'.'}));
    }

    @Test(timeout = 4000)
    public void testUncapitalize() {
        assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE"));
        assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", new char[]{'.'}));
    }

    @Test(timeout = 4000)
    public void testSwapCaseStandard() {
        assertEquals("tHE DOG HAS A bone", WordUtils.swapCase("The dog has a BONE"));
        assertEquals("hELLO wORLD", WordUtils.swapCase("Hello World"));
        assertEquals("1234 !? aBC", WordUtils.swapCase("1234 !? Abc"));
    }

    @Test(timeout = 4000)
    public void testInitialsStandard() {
        assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
        assertEquals("BJL", WordUtils.initials("Ben J.Lee", new char[]{' ', '.'}));
    }

    @Test(timeout = 4000)
    public void testAbbreviateStandardBranches() {
        // Space within bounds
        assertEquals("Now is the", WordUtils.abbreviate("Now is the time for all good men", 0, 10, null));
        assertEquals("Now is the...", WordUtils.abbreviate("Now is the time for all good men", 0, 10, "..."));

        // Space found beyond upper bound -> forced substring to upper
        assertEquals("Now is the...", WordUtils.abbreviate("Now is thelongwordthatkeepsgoing", 0, 10, "..."));

        // No space found anywhere after lower -> substring to upper
        assertEquals("Nowisthelongword...", WordUtils.abbreviate("Nowisthelongwordthatkeepsgoing", 5, 19, "..."));
        
        // Upper is -1 (no upper limit desired) -> upper becomes str.length()
        assertEquals("Now is the", WordUtils.abbreviate("Now is the", 0, -1, "..."));
        assertEquals("Now...", WordUtils.abbreviate("Now is the time", 0, -1, "..."));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testWrapBoundaryConditions() {
        assertNull(WordUtils.wrap(null, 10));
        assertEquals("", WordUtils.wrap("", 10));

        // wrapLength < 1 is coerced to 1
        String wrapped = WordUtils.wrap("a b c", 0, "\n", false);
        assertEquals("a\nb\nc", wrapped);

        // Leading and consecutive spaces
        String inputWithSpaces = "  first   second  ";
        String wrappedSpaces = WordUtils.wrap(inputWithSpaces, 5, "\n", false);
        assertNotNull(wrappedSpaces);
    }

    @Test(timeout = 4000)
    public void testCapitalizeBoundaries() {
        assertNull(WordUtils.capitalize(null));
        assertEquals("", WordUtils.capitalize(""));
        assertEquals("test", WordUtils.capitalize("test", new char[0]));

        assertNull(WordUtils.capitalizeFully(null));
        assertEquals("", WordUtils.capitalizeFully(""));
        assertEquals("test", WordUtils.capitalizeFully("test", new char[0]));
    }

    @Test(timeout = 4000)
    public void testUncapitalizeBoundaries() {
        assertNull(WordUtils.uncapitalize(null));
        assertEquals("", WordUtils.uncapitalize(""));
        assertEquals("TEST", WordUtils.uncapitalize("TEST", new char[0]));
    }

    @Test(timeout = 4000)
    public void testSwapCaseBoundaries() {
        assertNull(WordUtils.swapCase(null));
        assertEquals("", WordUtils.swapCase(""));

        // Test Unicode TitleCase swap to lowercase (\u01C5 is LATIN CAPITAL LETTER D WITH SMALL LETTER Z WITH CARON)
        char titleCaseChar = '\u01C5';
        assertTrue(Character.isTitleCase(titleCaseChar));
        String swappedTitle = WordUtils.swapCase(String.valueOf(titleCaseChar));
        assertEquals(String.valueOf(Character.toLowerCase(titleCaseChar)), swappedTitle);
    }

    @Test(timeout = 4000)
    public void testInitialsBoundaries() {
        assertNull(WordUtils.initials(null));
        assertEquals("", WordUtils.initials(""));
        assertEquals("", WordUtils.initials("Any String", new char[0]));
        assertEquals("", WordUtils.initials("   ", null));
    }

    @Test(timeout = 4000)
    public void testAbbreviateBoundaries() {
        assertNull(WordUtils.abbreviate(null, 0, 10, "..."));
        assertEquals("", WordUtils.abbreviate("", 0, 10, "..."));
        assertEquals("abc", WordUtils.abbreviate("abc", 0, 10, "..."));
        assertEquals("abc", WordUtils.abbreviate("abc", 0, -1, "..."));

        // Upper less than lower raises upper to lower
        assertEquals("hello...", WordUtils.abbreviate("hello world", 5, 2, "..."));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * CRITICAL DEFECT TEST:
     * Targets StringIndexOutOfBoundsException when lower limit > str.length().
     * On buggy versions:
     *   - upper is capped to str.length() (e.g. 10)
     *   - then 'if (upper < lower)' elevates upper to lower (15)
     *   - substring(0, 15) throws StringIndexOutOfBoundsException: String index out of range: 15
     * Expected correct behavior:
     *   - Since lower exceeds str.length(), the string is returned unabbreviated ("0123456789").
     */
    @Test(timeout = 4000)
    public void testAbbreviateDefectOutOfBoundsLowerGreaterThanLength() {
        String input = "0123456789";
        String result = WordUtils.abbreviate(input, 15, 20, null);
        assertEquals("0123456789", result);
    }

    @Test(timeout = 4000)
    public void testAbbreviateDefectOutOfBoundsLowerGreaterThanLengthWithAppend() {
        String input = "0123456789";
        String result = WordUtils.abbreviate(input, 15, -1, "...");
        assertEquals("0123456789", result);
    }

    @Test(timeout = 4000)
    public void testAbbreviateExactLengthBoundaries() {
        String input = "0123456789";
        assertEquals("0123456789", WordUtils.abbreviate(input, 10, 10, "..."));
        assertEquals("0123456789", WordUtils.abbreviate(input, 10, 20, "..."));
        assertEquals("0123456789", WordUtils.abbreviate(input, 10, -1, "..."));
    }

    // =========================================================================
    // Partition D: Object Lifecycle & Constructor Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        WordUtils utils = new WordUtils();
        assertNotNull(utils);
    }
}