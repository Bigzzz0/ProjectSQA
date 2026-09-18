package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.cli.Util (package-private)
 * 
 * stripLeadingHyphens:
 *   - Branch: null check -> return null (Line 1)
 *   - Branch: startsWith("--") -> substring(2) (Line 2)
 *   - Branch: startsWith("-") -> substring(1) (Line 3)
 *   - Branch: otherwise -> return str unchanged (Line 4)
 * 
 * stripLeadingAndTrailingQuotes:
 *   - Branch: startsWith("\"") -> substring(1) (Line 1)
 *   - Branch: endsWith("\"") -> substring(0,len-1) (Line 2)
 *   - Both branches may combine independently.
 *   - DEFECT REPRODUCTION: When input starts with '"' and ends with '"' but the leading/trailing quotes are mismatched? Actually the known defect
 *     from Defects4J shows that the method fails for input like "\"foo \"bar\"" (three quotes) - the bug is that after removing leading quote,
 *     if the string ends with a quote that is not the original trailing quote (internal becomes trailing), it is incorrectly removed. This test
 *     targets exactly that scenario.
 *   - Additional edge cases: null (should throw NPE), empty string, single quote, leading-only, trailing-only, quotes with internal content.
 */
public class UtilDeepseekTest {

    // ======================== stripLeadingHyphens Tests ========================

    @Test(timeout = 4000)
    public void testStripLeadingHyphensNull() {
        assertNull(Util.stripLeadingHyphens(null));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensEmpty() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensSingleHyphen() {
        assertEquals("abc", Util.stripLeadingHyphens("-abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleHyphen() {
        assertEquals("abc", Util.stripLeadingHyphens("--abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensTripleHyphen() {
        assertEquals("-abc", Util.stripLeadingHyphens("---abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensNoHyphen() {
        assertEquals("abc", Util.stripLeadingHyphens("abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensHyphenOnly() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphensDoubleHyphenOnly() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    // ======================== stripLeadingAndTrailingQuotes Tests ========================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testStripLeadingAndTrailingQuotesNull() {
        // This method does not handle null - will throw NPE; test for defensive handling.
        Util.stripLeadingAndTrailingQuotes(null);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesEmpty() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesSingleQuote() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesLeadingOnly() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("\"abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesTrailingOnly() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("abc\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesBoth() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("\"abc\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesInternalQuotes() {
        // Input: "a"b"c"
        assertEquals("a\"b\"c", Util.stripLeadingAndTrailingQuotes("\"a\"b\"c\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesNoQuotes() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("abc"));
    }

    /**
     * DIRECT TARGET OF THE KNOWN DEFECT:
     * Input: "\"foo \"bar\""   (three quotes: leading, internal, trailing)
     * Expected: "foo \"bar"    (only leading and trailing quotes removed, internal preserved)
     * The buggy version incorrectly removes the internal quote if it becomes the trailing quote after leading removal.
     * This test will fail on the defective version and pass on the fixed version.
     */
    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesDefectReproduction() {
        String input = "\"foo \"bar\"";
        String expected = "foo \"bar";
        String actual = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals(expected, actual);
    }

    // Additional edge-case: quote at start and end with content containing quotes
    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesNested() {
        assertEquals("x\"y", Util.stripLeadingAndTrailingQuotes("\"x\"y\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesSingleCharInside() {
        assertEquals("x", Util.stripLeadingAndTrailingQuotes("\"x\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesOnlyLeadingAndInternal() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("\"a\"b"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotesOnlyInternalAndTrailing() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("a\"b\""));
    }
}