package org.apache.commons.lang3.text.translate;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.lang3.text.translate.CharSequenceTranslator
 * Target Runtime: Java 8 / Defects4J / JUnit 4
 *
 * Decision / Branch Matrix:
 * 1. translate(CharSequence input)
 *    - Branch: input == null -> returns null
 *    - Branch: input != null -> instantiates StringWriter, calls translate(input, writer)
 *    - Exception Path: IOException from writer -> wraps in RuntimeException and throws
 *
 * 2. translate(CharSequence input, Writer out)
 *    - Branch: out == null -> throws IllegalArgumentException("The Writer must not be null")
 *    - Branch: input == null -> returns immediately (no-op)
 *    - Loop Condition: while (pos < len)
 *      - Sub-Branch: consumed == 0
 *        * BMP character: Character.toChars length = 1, out.write(c), pos += 1
 *        * Supplementary character (surrogate pair): Character.toChars length = 2, pos += 2
 *      - Sub-Branch: consumed > 0
 *        * Loop: for (int pt = 0; pt < consumed; pt++)
 *        * Defect Branch: When consumed represents character length (e.g. 2 for surrogate pair),
 *          the loop increments pos by codepoints. On pt = 0, pos advances across surrogate pair (+2).
 *          On pt = 1, pos is out of bounds (pos == len), triggering StringIndexOutOfBoundsException.
 *
 * 3. with(CharSequenceTranslator... translators)
 *    - Creates AggregateTranslator containing [this, ...translators]
 *    - Preserves execution sequence: this -> translators[0] -> ...
 *
 * 4. hex(int codepoint)
 *    - Converts codepoint to upper case hexadecimal String via Locale.ENGLISH
 *    - Boundary checks: 0, BMP bounds, Supplementary codepoints, Negative codepoints, Integer.MAX_VALUE
 * ====================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CharSequenceTranslatorGeminiTest {

    // -------------------------------------------------------------------------
    // Test Helpers
    // -------------------------------------------------------------------------

    private static class PassthroughTranslator extends CharSequenceTranslator {
        @Override
        public int translate(final CharSequence input, final int index, final Writer out) {
            return 0;
        }
    }

    private static class ReplaceCharTranslator extends CharSequenceTranslator {
        private final char target;
        private final String replacement;

        ReplaceCharTranslator(final char target, final String replacement) {
            this.target = target;
            this.replacement = replacement;
        }

        @Override
        public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
            if (input.charAt(index) == target) {
                out.write(replacement);
                return 1;
            }
            return 0;
        }
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTranslateStringWriterNormalFlow() throws IOException {
        final CharSequenceTranslator translator = new ReplaceCharTranslator('a', "ALPHA");
        final StringWriter writer = new StringWriter();
        translator.translate("cat", writer);
        assertEquals("cALPHAt", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateCharSequenceConvenienceMethod() {
        final CharSequenceTranslator translator = new ReplaceCharTranslator('o', "00");
        final String result = translator.translate("good");
        assertEquals("g0000d", result);
    }

    @Test(timeout = 4000)
    public void testTranslateConsumedZeroBranch() throws IOException {
        // When consumed is 0, translator writes original character directly
        final CharSequenceTranslator translator = new PassthroughTranslator();
        final StringWriter writer = new StringWriter();
        translator.translate("hello", writer);
        assertEquals("hello", writer.toString());
    }

    @Test(timeout = 4000)
    public void testWithMergesTranslatorsInCorrectSequence() {
        final CharSequenceTranslator t1 = new ReplaceCharTranslator('a', "1");
        final CharSequenceTranslator t2 = new ReplaceCharTranslator('b', "2");
        final CharSequenceTranslator t3 = new ReplaceCharTranslator('c', "3");

        final CharSequenceTranslator merged = t1.with(t2, t3);
        assertNotNull(merged);
        assertTrue(merged instanceof AggregateTranslator);
        assertEquals("123", merged.translate("abc"));
    }

    @Test(timeout = 4000)
    public void testWithEmptyVarargs() {
        final CharSequenceTranslator t1 = new ReplaceCharTranslator('a', "A");
        final CharSequenceTranslator merged = t1.with();
        assertNotNull(merged);
        assertEquals("Abc", merged.translate("abc"));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTranslateNullCharSequenceReturnsNull() {
        final CharSequenceTranslator translator = new PassthroughTranslator();
        assertNull(translator.translate((CharSequence) null));
    }

    @Test(timeout = 4000)
    public void testTranslateNullCharSequenceToWriterIsNoOp() throws IOException {
        final CharSequenceTranslator translator = new PassthroughTranslator();
        final StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateEmptyCharSequence() throws IOException {
        final CharSequenceTranslator translator = new PassthroughTranslator();
        assertEquals("", translator.translate(""));

        final StringWriter writer = new StringWriter();
        translator.translate("", writer);
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateSupplementaryCodepointsConsumedZero() throws IOException {
        // Surrogate pair: U+1F630 (D83D DE30) -> length 2 in chars, 1 codepoint
        final String surrogatePair = "\uD83D\uDE30";
        final CharSequenceTranslator passthrough = new PassthroughTranslator();

        final StringWriter writer = new StringWriter();
        passthrough.translate(surrogatePair, writer);
        assertEquals(surrogatePair, writer.toString());
        assertEquals(surrogatePair, passthrough.translate(surrogatePair));
    }

    @Test(timeout = 4000)
    public void testHexCodepointConversions() {
        assertEquals("0", CharSequenceTranslator.hex(0));
        assertEquals("A", CharSequenceTranslator.hex(10));
        assertEquals("F", CharSequenceTranslator.hex(15));
        assertEquals("10", CharSequenceTranslator.hex(16));
        assertEquals("FF", CharSequenceTranslator.hex(255));
        assertEquals("FFFF", CharSequenceTranslator.hex(0xFFFF));
        assertEquals("1F630", CharSequenceTranslator.hex(0x1F630));
        assertEquals("7FFFFFFF", CharSequenceTranslator.hex(Integer.MAX_VALUE));
        assertEquals("FFFFFFFF", CharSequenceTranslator.hex(-1));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Target Defect:
     * org.apache.commons.lang3.StringUtilsTest::testEscapeSurrogatePairs
     * --> java.lang.StringIndexOutOfBoundsException: String index out of range: 2
     *
     * Cause:
     * When a translator translates a surrogate pair (2 char length in Java) and
     * returns 2 (the char count consumed), CharSequenceTranslator treats 'consumed'
     * as codepoint count and advances 'pos' via Character.charCount(...) in a loop.
     * On pt = 0, pos advances by 2 (the surrogate pair).
     * On pt = 1, pos is now 2 (equal to input.length()), causing Character.codePointAt(input, 2)
     * to throw StringIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testEscapeSurrogatePairsDefectWithCustomTranslator() {
        final String surrogatePair = "\uD83D\uDE30"; // U+1F630 (length 2)
        final CharSequenceTranslator surrogateEscaper = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                final int cp = Character.codePointAt(input, index);
                if (cp == 0x1F630) {
                    out.write("&#" + cp + ";");
                    return Character.charCount(cp); // returns 2
                }
                return 0;
            }
        };

        final String result = surrogateEscaper.translate(surrogatePair);
        assertEquals("&#128560;", result);
    }

    @Test(timeout = 4000)
    public void testEscapeSurrogatePairsDefectWithNumericEntityEscaper() {
        // Direct reproduction of standard Commons Lang escaper on surrogate pairs
        final NumericEntityEscaper escaper = NumericEntityEscaper.between(0, Integer.MAX_VALUE);
        final String surrogatePair = "\uD83D\uDE30";
        final String result = escaper.translate(surrogatePair);
        assertEquals("&#128560;", result);
    }

    @Test(timeout = 4000)
    public void testEscapeSurrogatePairsFollowedByText() {
        final String input = "\uD83D\uDE30Hello"; // length 7
        final NumericEntityEscaper escaper = NumericEntityEscaper.between(0x1F000, 0x1FFFF);
        final String result = escaper.translate(input);
        assertEquals("&#128560;Hello", result);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTranslateNullWriterThrowsIllegalArgumentException() throws IOException {
        final CharSequenceTranslator translator = new PassthroughTranslator();
        translator.translate("test", null);
    }

    @Test(timeout = 4000)
    public void testTranslateIOExceptionWrappedInRuntimeException() {
        final IOException expectedIOException = new IOException("Simulated I/O failure");
        final CharSequenceTranslator faultyTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                throw expectedIOException;
            }
        };

        try {
            faultyTranslator.translate("sample");
            fail("Expected RuntimeException wrapping IOException");
        } catch (final RuntimeException re) {
            assertSame(expectedIOException, re.getCause());
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAggregateTranslatorChainingIntegrity() throws IOException {
        final CharSequenceTranslator t1 = new ReplaceCharTranslator('x', "1");
        final CharSequenceTranslator t2 = new ReplaceCharTranslator('y', "2");
        final CharSequenceTranslator t3 = new ReplaceCharTranslator('z', "3");

        final CharSequenceTranslator chain = t1.with(t2).with(t3);
        final StringWriter out = new StringWriter();
        chain.translate("xyz!", out);
        assertEquals("123!", out.toString());
    }

    @Test(timeout = 4000)
    public void testMultiCodepointConsumptionContract() throws IOException {
        // Translator consuming multiple non-surrogate characters at once
        final CharSequenceTranslator wordTranslator = new CharSequenceTranslator() {
            @Override
            public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
                if (index <= input.length() - 3 && "foo".equals(input.subSequence(index, index + 3).toString())) {
                    out.write("BAR");
                    return 3;
                }
                return 0;
            }
        };

        final String result = wordTranslator.translate("before-foo-after");
        assertEquals("before-BAR-after", result);
    }
}