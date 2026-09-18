package org.jfree.chart.imagemap;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class: StandardToolTipTagFragmentGenerator
 *
 * Decision / Branch Matrix:
 * 1. Constructor: Default constructor initialization and interface contract verification.
 * 2. generateToolTipFragment(String):
 *    - Partition A (Core Functional): Standard alpha-numeric text strings without escapable characters.
 *    - Partition B (Boundary Values): Empty strings, whitespace-only strings.
 *    - Partition C (Defect-Targeted Branch Zone - Ground Truth Regression):
 *      * Tooltip text containing double quotes (e.g. "Series [\"A\"], 100.0").
 *      * Tooltip text containing HTML special characters ('&', '<', '>', '"').
 *      * Target Defect: Ground truth regression where unescaped double quotes corrupt the HTML attribute delimiter.
 *        Fixed implementation passes text through ImageMapUtilities.htmlEscape(toolTipText), transforming
 *        double quote (") into "&quot;".
 *    - Partition D (Defensive & Robustness): Null reference behavior in string concatenation / escaping.
 *    - Partition E (Contract Integrity): Verifying instance conforms to ToolTipTagFragmentGenerator.
 */
public class StandardToolTipTagFragmentGeneratorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentStandardText() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "Series 1, 100.0";
        String expected = " title=\"Series 1, 100.0\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("Standard text without special characters should be wrapped correctly", expected, actual);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentSimpleAlphaNumeric() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "Sales2023";
        String expected = " title=\"Sales2023\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentEmptyString() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "";
        String expected = " title=\"\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("Empty string should generate empty title attribute", expected, actual);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWhitespaceOnly() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "   ";
        String expected = " title=\"   \" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("Whitespace-only tooltip should be preserved within title attribute", expected, actual);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Exact ground truth failure reproduction:
     * junit.framework.ComparisonFailure:
     *   expected:< title="Series [&quot;A&quot;], 100.0" alt="">
     *   but was:< title="Series ["A"], 100.0" alt="">
     */
    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithEmbeddedQuotes() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "Series [\"A\"], 100.0";
        String expected = " title=\"Series [&quot;A&quot;], 100.0\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("Embedded double quotes must be HTML-escaped to &quot; to prevent tag corruption",
                expected, actual);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentSingleQuoteOnly() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "\"";
        String expected = " title=\"&quot;\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("A single quote must be escaped to &quot;", expected, actual);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentHtmlSpecialCharacters() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String text = "Value < 100 & Value > 50 with \"Quotes\"";
        String expected = " title=\"Value &lt; 100 &amp; Value &gt; 50 with &quot;Quotes&quot;\" alt=\"\"";
        String actual = generator.generateToolTipFragment(text);
        assertEquals("HTML entities (&, <, >, \") should all be escaped properly", expected, actual);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentNullInput() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        String actual = generator.generateToolTipFragment(null);
        String expected = " title=\"null\" alt=\"\"";
        assertEquals("Null input string should result in 'null' text without throwing NullPointerException",
                expected, actual);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testContractIntegrityAndInstanceType() {
        StandardToolTipTagFragmentGenerator generator = new StandardToolTipTagFragmentGenerator();
        assertNotNull("Constructor must instantiate object", generator);
        assertTrue("Must implement ToolTipTagFragmentGenerator interface",
                generator instanceof ToolTipTagFragmentGenerator);
    }
}