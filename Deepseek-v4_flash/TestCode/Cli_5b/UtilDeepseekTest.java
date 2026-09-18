package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: Util (package-private, static methods)
 * 
 * Methods under test:
 * 1. stripLeadingHyphens(String str)
 *    - Branch 1: str.startsWith("--") -> true/false
 *    - Branch 2: str.startsWith("-") -> true/false (only if Branch 1 false)
 *    - Branch 3: implicit else (no leading hyphens)
 *    - Boundary conditions: 
 *      - null input (defect: NPE, not handled)
 *      - empty string ""
 *      - single "-"
 *      - double "--"
 *      - triple "---"
 *      - strings with hyphens in middle/end
 *      - strings with only hyphens
 *      - strings with no hyphens
 *      - strings with leading spaces before hyphens
 *      - Unicode characters
 * 
 * 2. stripLeadingAndTrailingQuotes(String str)
 *    - Branch 1: str.startsWith("\"") -> true/false
 *    - Branch 2: str.endsWith("\"") -> true/false (independent of Branch 1)
 *    - Boundary conditions:
 *      - null input (defect: NPE, not handled)
 *      - empty string ""
 *      - single quote "\""
 *      - double quotes "\"\""
 *      - quoted string with content
 *      - string with only leading quote
 *      - string with only trailing quote
 *      - string with quotes in middle
 *      - string with multiple quotes at ends
 * 
 * Defect targeting (from ground truth):
 * - Both methods throw NullPointerException when passed null.
 * - The correct behavior should be to handle null gracefully (return null or throw IllegalArgumentException with clear message).
 * - Test methods explicitly pass null to trigger the NPE and assert that it should NOT happen (i.e., the test will fail on defective version).
 * 
 * Partitions:
 * A. Core functional logic (normal inputs)
 * B. Boundary values (empty, single char, max lengths)
 * C. Defect-targeted (null inputs)
 * D. Exception/defensive paths (null handling)
 * E. Contract integrity (immutability, no side effects)
 */
public class UtilDeepseekTest {

    // ==================== PARTITION A: CORE FUNCTIONAL LOGIC ====================

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NormalDoubleHyphen() {
        assertEquals("option", Util.stripLeadingHyphens("--option"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NormalSingleHyphen() {
        assertEquals("option", Util.stripLeadingHyphens("-option"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NoHyphens() {
        assertEquals("option", Util.stripLeadingHyphens("option"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_MixedHyphens() {
        assertEquals("-option", Util.stripLeadingHyphens("---option"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_OnlyDoubleHyphen() {
        assertEquals("", Util.stripLeadingHyphens("--"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_OnlySingleHyphen() {
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_EmptyString() {
        assertEquals("", Util.stripLeadingHyphens(""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_WhitespaceBeforeHyphen() {
        assertEquals(" --option", Util.stripLeadingHyphens(" --option"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_HyphenInMiddle() {
        assertEquals("opt-ion", Util.stripLeadingHyphens("opt-ion"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_HyphenAtEnd() {
        assertEquals("option-", Util.stripLeadingHyphens("option-"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_UnicodeCharacters() {
        assertEquals("--옵션", Util.stripLeadingHyphens("--옵션"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_LongString() {
        StringBuilder sb = new StringBuilder("--");
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        String input = sb.toString();
        String expected = input.substring(2);
        assertEquals(expected, Util.stripLeadingHyphens(input));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_NormalQuoted() {
        assertEquals("one two", Util.stripLeadingAndTrailingQuotes("\"one two\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_NoQuotes() {
        assertEquals("plain", Util.stripLeadingAndTrailingQuotes("plain"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_OnlyLeadingQuote() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("\"abc"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_OnlyTrailingQuote() {
        assertEquals("abc", Util.stripLeadingAndTrailingQuotes("abc\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_EmptyString() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes(""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_SingleQuote() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_DoubleQuotes() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_QuotesInMiddle() {
        assertEquals("a\"b", Util.stripLeadingAndTrailingQuotes("a\"b"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_MultipleQuotesAtEnds() {
        assertEquals("\"abc\"", Util.stripLeadingAndTrailingQuotes("\"\"abc\"\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_WhitespaceInside() {
        assertEquals("  spaced  ", Util.stripLeadingAndTrailingQuotes("\"  spaced  \""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_UnicodeQuotes() {
        assertEquals("옵션", Util.stripLeadingAndTrailingQuotes("\"옵션\""));
    }

    // ==================== PARTITION B: BOUNDARY VALUE ANALYSIS ====================

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Boundary_SingleChar() {
        assertEquals("a", Util.stripLeadingHyphens("a"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Boundary_DoubleHyphenSingleChar() {
        assertEquals("a", Util.stripLeadingHyphens("--a"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Boundary_TripleHyphen() {
        assertEquals("-", Util.stripLeadingHyphens("---"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Boundary_MaxLength() {
        StringBuilder sb = new StringBuilder("--");
        for (int i = 0; i < 10000; i++) {
            sb.append('x');
        }
        String input = sb.toString();
        String expected = input.substring(2);
        assertEquals(expected, Util.stripLeadingHyphens(input));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Boundary_OnlyQuote() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Boundary_TwoQuotes() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Boundary_ThreeQuotes() {
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\"\"\""));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Boundary_WhitespaceOnly() {
        assertEquals("   ", Util.stripLeadingAndTrailingQuotes("   "));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Boundary_TabAndNewline() {
        assertEquals("\t\n", Util.stripLeadingAndTrailingQuotes("\t\n"));
    }

    // ==================== PARTITION C: DEFECT-TARGETED BRANCH ZONE ====================

    /**
     * Defect: Both methods throw NullPointerException when passed null.
     * The correct behavior should be to handle null gracefully.
     * This test will FAIL on the defective version (NPE) and PASS on fixed version.
     */
    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NullInput_ShouldNotThrowNPE() {
        try {
            String result = Util.stripLeadingHyphens(null);
            // If we reach here, no exception was thrown - this is the expected correct behavior
            // The method should return null or handle it gracefully
            assertNull("Should return null for null input", result);
        } catch (NullPointerException e) {
            fail("NullPointerException thrown for null input - defect detected!");
        }
    }

    /**
     * Defect: Both methods throw NullPointerException when passed null.
     * The correct behavior should be to handle null gracefully.
     * This test will FAIL on the defective version (NPE) and PASS on fixed version.
     */
    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_NullInput_ShouldNotThrowNPE() {
        try {
            String result = Util.stripLeadingAndTrailingQuotes(null);
            // If we reach here, no exception was thrown - this is the expected correct behavior
            assertNull("Should return null for null input", result);
        } catch (NullPointerException e) {
            fail("NullPointerException thrown for null input - defect detected!");
        }
    }

    // ==================== PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS ====================

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NullInput_ExplicitExceptionCheck() {
        boolean threwNPE = false;
        try {
            Util.stripLeadingHyphens(null);
        } catch (NullPointerException e) {
            threwNPE = true;
        }
        // The defect is that NPE is thrown; correct behavior should not throw NPE
        // This test documents the current (defective) behavior and will fail if fixed
        // To make it a proper defect-revealing test, we assert the CORRECT behavior
        assertFalse("NPE should not be thrown for null input - defect detected!", threwNPE);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_NullInput_ExplicitExceptionCheck() {
        boolean threwNPE = false;
        try {
            Util.stripLeadingAndTrailingQuotes(null);
        } catch (NullPointerException e) {
            threwNPE = true;
        }
        assertFalse("NPE should not be thrown for null input - defect detected!", threwNPE);
    }

    // ==================== PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY ====================

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_NoSideEffects_OriginalUnchanged() {
        String input = "--option";
        String original = input;
        Util.stripLeadingHyphens(input);
        assertEquals("Original string should not be modified", original, input);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_NoSideEffects_OriginalUnchanged() {
        String input = "\"option\"";
        String original = input;
        Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("Original string should not be modified", original, input);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Immutability_ResultIsNewString() {
        String input = "--option";
        String result = Util.stripLeadingHyphens(input);
        assertNotSame("Result should be a new String object", input, result);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Immutability_ResultIsNewString() {
        String input = "\"option\"";
        String result = Util.stripLeadingAndTrailingQuotes(input);
        assertNotSame("Result should be a new String object", input, result);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_Consistency_SameInputSameOutput() {
        String input = "--option";
        assertEquals(Util.stripLeadingHyphens(input), Util.stripLeadingHyphens(input));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_Consistency_SameInputSameOutput() {
        String input = "\"option\"";
        assertEquals(Util.stripLeadingAndTrailingQuotes(input), Util.stripLeadingAndTrailingQuotes(input));
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_CombinedWithQuotes() {
        String input = "\"--option\"";
        String stripped = Util.stripLeadingHyphens(input);
        assertEquals("\"option\"", stripped);
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_CombinedWithHyphens() {
        String input = "\"--option\"";
        String stripped = Util.stripLeadingAndTrailingQuotes(input);
        assertEquals("--option", stripped);
    }

    @Test(timeout = 4000)
    public void testStripLeadingHyphens_EmptyResult() {
        assertEquals("", Util.stripLeadingHyphens("--"));
        assertEquals("", Util.stripLeadingHyphens("-"));
    }

    @Test(timeout = 4000)
    public void testStripLeadingAndTrailingQuotes_EmptyResult() {
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\"\""));
        assertEquals("", Util.stripLeadingAndTrailingQuotes("\""));
    }
}