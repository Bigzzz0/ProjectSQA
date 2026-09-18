package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.cli.Util
 *
 * Methods Under Test:
 * 1. Util.stripLeadingHyphens(String str)
 *    - Branch 1: str == null (Defects4J Bug CLI-133: currently throws NPE, expected to return null)
 *    - Branch 2: str.startsWith("--") -> substring(2, length)
 *    - Branch 3: str.startsWith("-") (not "--") -> substring(1, length)
 *    - Branch 4: does not start with "-" -> returns str unchanged
 *    - Boundaries: "", "-", "--", "---", "--a", "-a", "a", null
 *
 * 2. Util.stripLeadingAndTrailingQuotes(String str)
 *    - Branch 1: str.startsWith("\"") -> true: substring(1, length); false: unmodified
 *    - Branch 2: str.endsWith("\"") -> true: substring(0, length - 1); false: unmodified
 *    - Combinations:
 *      * Leading and trailing quotes: "\"hello\"" -> "hello"
 *      * Leading quote only: "\"hello" -> "hello"
 *      * Trailing quote only: "hello\"" -> "hello"
 *      * No quotes: "hello" -> "hello"
 *      * Empty string: "" -> ""
 *      * Single quote: "\"" -> ""
 *      * Double quote: "\"\"" -> ""
 *      * Triple quote: "\"\"\"" -> "\""
 *
 * 3. Util constructor
 *    - Default package-private constructor invocation for full code/class coverage.
 */
public class UtilGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleHyphenWord() {
        String input = "--option";
        String expected = "option";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Double hyphen prefix should be stripped", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensSingleHyphenWord() {
        String input = "-opt";
        String expected = "opt";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Single hyphen prefix should be stripped", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensNoHyphen() {
        String input = "option";
        String expected = "option";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("String without hyphens should remain unchanged", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesSurrounded() {
        String input = "\"sample text\"";
        String expected = "sample text";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Enclosing quotes should be removed", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesLeadingOnly() {
        String input = "\"sample";
        String expected = "sample";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Leading quote should be removed", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesTrailingOnly() {
        String input = "sample\"";
        String expected = "sample";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Trailing quote should be removed", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesNone() {
        String input = "sample";
        String expected = "sample";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("String without quotes should remain unchanged", expected, actual);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStripLeadingHyphensEmptyString() {
        String input = "";
        String expected = "";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Empty string should return empty string", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensSingleHyphenOnly() {
        String input = "-";
        String expected = "";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Single hyphen should be stripped to empty string", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleHyphenOnly() {
        String input = "--";
        String expected = "";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Double hyphen should be stripped to empty string", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensMultipleHyphens() {
        String input = "---flag";
        String expected = "-flag";
        String actual = Util.stripLeadingHyphens(input);
        assertEquals("Triple hyphen should strip only first two hyphens", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesEmptyString() {
        String input = "";
        String expected = "";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Empty string should return empty string", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesSingleQuoteOnly() {
        String input = "\"";
        String expected = "";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Single quote should be stripped completely", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesDoubleQuoteOnly() {
        String input = "\"\"";
        String expected = "";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Pair of quotes should be stripped to empty string", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesTripleQuoteOnly() {
        String input = "\"\"\"";
        String expected = "\"";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Triple quotes stripped from both ends should leave one quote", expected, actual);
    }

    @Test(timeout = 4000)
    public void testStripQuotesWithInternalQuotes() {
        String input = "\"foo\"bar\"";
        String expected = "foo\"bar";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Only outer quotes should be stripped, preserving internal ones", expected, actual);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CLI-133 / NPE on null)
    // =========================================================================

    /**
     * Defects4J Ground Truth Defect:
     * Util.stripLeadingHyphens(null) currently throws java.lang.NullPointerException
     * when str.startsWith(...) is invoked without null check.
     * Expected behavior: stripLeadingHyphens(null) should safely return null.
     */
    @Test(timeout = 4000)
    public void testStripLeadingHyphensWithNull() {
        String result = Util.stripLeadingHyphens(null);
        assertNull("stripLeadingHyphens(null) must return null instead of throwing NPE", result);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testUtilInstantiation() {
        Util utilInstance = new Util();
        assertNotNull("Util instance should be instantiable", utilInstance);
    }
}