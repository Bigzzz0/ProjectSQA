package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CharSequenceTranslatorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Branches covered:
     * - translate(CharSequence): null input -> null return
     * - translate(CharSequence, Writer): null Writer -> IllegalArgumentException
     * - translate(CharSequence, Writer): null input -> early return
     * - translate(CharSequence, Writer): consumed == 0 branch (character fallback)
     * - translate(CharSequence, Writer): consumed > 0 branch (codepoint advance)
     * - translate(CharSequence, Writer): loop termination at pos >= len
     * - hex(int): codepoint to uppercase hex conversion
     * - with(CharSequenceTranslator...): null array, empty array, single array
     * 
     * Boundary conditions:
     * - Empty input CharSequence
     * - Single BMP character (U+0041) - consumed == 0
     * - Supplementary character (U+1D11E) - surrogate pair handling
     * - Mixed BMP and supplementary characters
     * - Large codepoint values (0x10FFFF)
     * - Negative codepoint values (for hex method)
     * - Zero codepoint (U+0000)
     * 
     * Defect target (Defects4J case: StringIndexOutOfBoundsException on surrogate pairs):
     * The bug is in the translate(CharSequence, Writer) method's loop advancement logic.
     * When consumed == 0, the code correctly handles surrogate pairs via Character.toChars().
     * But when consumed > 0, the advancement loop calls Character.codePointAt(input, pos)
     * which can overflow for surrogate pairs if pos is incorrectly managed.
     * The test testTranslateWithSurrogatePairConsumed reveals this by having a translator 
     * that consumes exactly one codepoint from a supplementary character input.
     */

    // ========================================================================
    // Helper test double classes
    // ========================================================================

    /**
     * A translator that consumes 0 codepoints, forcing the fallback branch.
     */
    private static class ZeroConsumingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            return 0; // Will write the character via fallback
        }
    }

    /**
     * A translator that consumes exactly 1 codepoint.
     */
    private static class OneConsumingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            // Consume one codepoint
            return 1;
        }
    }

    /**
     * A translator that consumes exactly 2 codepoints.
     */
    private static class TwoConsumingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            // Consume two codepoints
            return 2;
        }
    }

    /**
     * A translator that throws IOException (should never happen with StringWriter).
     */
    private static class ThrowingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            throw new IOException("Forced IO Exception");
        }
    }

    /**
     * A translator that writes transformed characters.
     */
    private static class TransformingTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            int cp = Character.codePointAt(input, index);
            if (cp == 'a') {
                out.write("1");
                return 1;
            } else if (cp == 'b') {
                out.write("22");
                return 1;
            } else if (cp == 0x1D11E) { // MUSICAL SYMBOL G CLEF (supplementary)
                out.write("[G CLEF]");
                return Character.charCount(cp); // Should return 2 for surrogate pair
            }
            return 0;
        }
    }

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testTranslateInputNullReturnsNull() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        assertNull("Null input should return null", translator.translate(null));
    }

    @Test(timeout = 4000)
    public void testTranslateInputEmptyString() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        assertEquals("Empty input should return empty string", "", translator.translate(""));
    }

    @Test(timeout = 4000)
    public void testTranslateInputWithBmpCharacters() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        assertEquals("Hello", translator.translate("Hello"));
    }

    @Test(timeout = 4000)
    public void testTranslateInputWithSurrogatePair() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        // MUSICAL SYMBOL G CLEF (U+1D11E) - requires surrogate pair in UTF-16
        String input = "\uD834\uDD1E";
        assertEquals("Surrogate pair should pass through", input, translator.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateInputMixed() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        String input = "A\uD834\uDD1EB";
        assertEquals("Mixed BMP and supplementary", input, translator.translate(input));
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testTranslateWriterNullWriterThrowsException() throws IOException {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        try {
            translator.translate("test", (Writer) null);
            fail("Expected IllegalArgumentException for null Writer");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testTranslateWriterNullInput() throws IOException {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("Null input should write nothing", "", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWriterEmptyInput() throws IOException {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("Empty input should write nothing", "", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWriterSingleCharacter() throws IOException {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        StringWriter writer = new StringWriter();
        translator.translate("A", writer);
        assertEquals("A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWriterLargeCodepoint() throws IOException {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        StringWriter writer = new StringWriter();
        // Largest valid Unicode codepoint U+10FFFF
        String input = new String(Character.toChars(0x10FFFF));
        translator.translate(input, writer);
        assertEquals(input, writer.toString());
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J surrogate pair bug)
    // ========================================================================

    @Test(timeout = 4000)
    public void testTranslateWithSurrogatePairConsumed() throws IOException {
        // This test targets the Defects4J bug: StringIndexOutOfBoundsException
        // when consuming a supplementary character (surrogate pair)
        CharSequenceTranslator translator = new OneConsumingTranslator();
        StringWriter writer = new StringWriter();
        // Input with a supplementary character (U+1D11E = "\uD834\uDD1E")
        translator.translate("\uD834\uDD1E", writer);
        // The translator consumed 1 codepoint, but the advancement loop uses
        // Character.codePointAt which would cause index overflow if buggy
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithSurrogatePairConsumedTwo() throws IOException {
        // Another variant: consuming 2 codepoints from a supplementary character
        CharSequenceTranslator translator = new TwoConsumingTranslator();
        StringWriter writer = new StringWriter();
        // Input with two surrogate pairs: U+1D11E and U+1D11E
        translator.translate("\uD834\uDD1E\uD834\uDD1E", writer);
        // Should consume both codepoints (4 code units)
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateMixedWithConsuming() throws IOException {
        CharSequenceTranslator translator = new TransformingTranslator();
        StringWriter writer = new StringWriter();
        // Input: 'a' + MUSICAL SYMBOL G CLEF + 'b'
        String input = "a\uD834\uDD1Eb";
        translator.translate(input, writer);
        // Expected: "1[G CLEF]22"
        assertEquals("1[G CLEF]22", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithConsumedZeroFollowedBySurrogate() throws IOException {
        // Edge case: consumed == 0 for a BMP char, then consumed > 0 for surrogate pair
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                int cp = Character.codePointAt(input, index);
                if (cp == 'A') {
                    return 1; // consume A
                }
                return 0; // fallback for others
            }
        };
        StringWriter writer = new StringWriter();
        translator.translate("A\uD834\uDD1E", writer);
        // A is consumed and written nothing, then surrogate pair falls through (consumed=0)
        // The fallback writes the surrogate pair directly
        assertEquals("\uD834\uDD1E", writer.toString());
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000)
    public void testTranslateWithIOException() {
        CharSequenceTranslator translator = new ThrowingTranslator();
        try {
            translator.translate("test");
            fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException e) {
            assertTrue("Cause should be IOException", e.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testHexWithZero() {
        assertEquals("0", CharSequenceTranslator.hex(0));
    }

    @Test(timeout = 4000)
    public void testHexWithPositive() {
        assertEquals("41", CharSequenceTranslator.hex('A'));
    }

    @Test(timeout = 4000)
    public void testHexWithLargeCodepoint() {
        assertEquals("1d11e", CharSequenceTranslator.hex(0x1D11E));
    }

    @Test(timeout = 4000)
    public void testHexWithMaxCodepoint() {
        assertEquals("10ffff", CharSequenceTranslator.hex(0x10FFFF));
    }

    @Test(timeout = 4000)
    public void testHexWithNegative() {
        // Negative numbers produce hex representations with leading signs in Java
        assertEquals("ffffffff", CharSequenceTranslator.hex(-1));
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testWithNullArray() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        CharSequenceTranslator result = translator.with((CharSequenceTranslator[]) null);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    @Test(timeout = 4000)
    public void testWithEmptyArray() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        CharSequenceTranslator result = translator.with(new CharSequenceTranslator[0]);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    @Test(timeout = 4000)
    public void testWithSingleTranslator() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        CharSequenceTranslator other = new OneConsumingTranslator();
        CharSequenceTranslator result = translator.with(other);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    @Test(timeout = 4000)
    public void testWithMultipleTranslators() {
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        CharSequenceTranslator other1 = new OneConsumingTranslator();
        CharSequenceTranslator other2 = new TwoConsumingTranslator();
        CharSequenceTranslator result = translator.with(other1, other2);
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    // ========================================================================
    // Additional edge case tests for thorough coverage
    // ========================================================================

    @Test(timeout = 4000)
    public void testTranslateWithConsumedZeroBoundary() throws IOException {
        // Test where consumed == 0 for a surrogate pair high surrogate
        CharSequenceTranslator translator = new ZeroConsumingTranslator();
        StringWriter writer = new StringWriter();
        // Input just a high surrogate (malformed but tests the code path)
        // The code will call Character.toChars on the codepoint of the high surrogate
        translator.translate("\uD834", writer);
        assertEquals("\uD834", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithAllCodepointsConsumed() throws IOException {
        CharSequenceTranslator translator = new OneConsumingTranslator();
        StringWriter writer = new StringWriter();
        translator.translate("AB", writer);
        // Consumes 1 codepoint (A), advances pos by 1, then consumes B, writes nothing
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithMultipleSurrogatePairsConsumed() throws IOException {
        CharSequenceTranslator translator = new OneConsumingTranslator();
        StringWriter writer = new StringWriter();
        // Two musical symbols: U+1D11E and U+1D11E
        translator.translate("\uD834\uDD1E\uD834\uDD1E", writer);
        // Should consume first surrogate pair (2 code units), then second
        assertEquals("", writer.toString());
    }
}