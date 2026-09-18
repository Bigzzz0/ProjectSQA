package org.jfree.chart.imagemap;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for StandardToolTipTagFragmentGenerator.
 * Targets branch coverage, boundary conditions, and the known escaping defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Line coverage: the single method generateToolTipFragment.
 * - Branch coverage: no conditional branches; only a single return statement.
 * - Defect: HTML special characters (especially double quotes) are not escaped,
 *   causing invalid HTML attribute values. Expected: " → &quot;, & → &amp;, < → &lt;, > → &gt;.
 * - Boundary conditions: null input (NPE), empty string, strings with special chars,
 *   strings with only special chars, very long strings.
 * - Exception paths: null input should throw NullPointerException (defensive guard missing).
 */
public class StandardToolTipTagFragmentGeneratorDeepseekTest {

    // ========== Partition A: Core functional logic ==========

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentNormalText() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("Hello World");
        // Expected: no special chars, just wrapped
        assertEquals(" title=\"Hello World\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentEmptyString() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("");
        assertEquals(" title=\"\" alt=\"\"", result);
    }

    // ========== Partition B: Boundary values and extremes ==========

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithDoubleQuote() {
        // Defect-targeted: double quote must be escaped to &quot;
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("Series \"A\", 100.0");
        // Expected correct HTML-escaped output (escaped double quote)
        assertEquals(" title=\"Series &quot;A&quot;, 100.0\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithAmpersand() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("A & B");
        assertEquals(" title=\"A &amp; B\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithLessThan() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("x < 10");
        assertEquals(" title=\"x &lt; 10\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithGreaterThan() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("y > 5");
        assertEquals(" title=\"y &gt; 5\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithAllSpecialChars() {
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("\"<>&");
        assertEquals(" title=\"&quot;&lt;&gt;&amp;\" alt=\"\"", result);
    }

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentWithVeryLongString() {
        // Build a long string to stress buffer handling
        StringBuilder sb = new StringBuilder(1000);
        for (int i = 0; i < 100; i++) {
            sb.append("test");
        }
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment(sb.toString());
        // No special chars, so just wrapped
        assertEquals(" title=\"" + sb.toString() + "\" alt=\"\"", result);
    }

    // ========== Partition C: Defect-targeted branch (known escaping failure) ==========

    @Test(timeout = 4000)
    public void testGenerateToolTipFragmentDefectTrigger() {
        // This test directly reproduces the known failure from Defects4J:
        // input: "Series [\"A\"], 100.0" (with double quotes)
        // Expected (fixed) output: title="Series [&quot;A&quot;], 100.0" alt=""
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        String result = gen.generateToolTipFragment("Series [\"A\"], 100.0");
        assertEquals(" title=\"Series [&quot;A&quot;], 100.0\" alt=\"\"", result);
    }

    // ========== Partition D: Exception/defensive guard paths ==========

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGenerateToolTipFragmentNullInput() {
        // The method does not handle null; it should throw NPE (defect or by design)?
        // We test that it does throw NPE (current behavior). If fixed to handle null,
        // this test would need adjustment. But the known defect is about escaping, not null.
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        gen.generateToolTipFragment(null);
    }

    // ========== Partition E: Object lifecycle & contract integrity ==========

    @Test(timeout = 4000)
    public void testMultipleInstancesIndependence() {
        // Each instance should produce the same output for same input
        StandardToolTipTagFragmentGenerator gen1 = new StandardToolTipTagFragmentGenerator();
        StandardToolTipTagFragmentGenerator gen2 = new StandardToolTipTagFragmentGenerator();
        String input = "Hello & world";
        assertEquals(gen1.generateToolTipFragment(input), gen2.generateToolTipFragment(input));
    }

    @Test(timeout = 4000)
    public void testToStringNotOverridden() {
        // The class doesn't override toString, but we can call it to ensure no exception
        StandardToolTipTagFragmentGenerator gen = new StandardToolTipTagFragmentGenerator();
        assertNotNull(gen.toString());
    }
}