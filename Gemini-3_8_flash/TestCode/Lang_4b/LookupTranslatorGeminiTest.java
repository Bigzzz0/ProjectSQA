package org.apache.commons.lang3.text.translate;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang3.text.translate.LookupTranslator
 *
 * 1. Constructor Branch Analysis:
 *    - lookup == null -> shortest = Integer.MAX_VALUE, longest = 0.
 *    - lookup != null, empty array -> shortest = Integer.MAX_VALUE, longest = 0.
 *    - lookup with single entry -> shortest = longest = entry[0].length().
 *    - lookup with multiple entries -> tracks min (shortest) and max (longest).
 *    - lookup with CharSequence implementations (StringBuffer, StringBuilder, custom CharSequence).
 *
 * 2. translate(CharSequence, int, Writer) Branch Analysis:
 *    - Boundary: index + longest > input.length() -> max = input.length() - index.
 *    - Boundary: index + longest <= input.length() -> max = longest.
 *    - Loop condition: i >= shortest (skips completely if max < shortest).
 *    - Greedy evaluation: checks from max descending to shortest.
 *    - lookupMap.get(subSeq) != null -> writes result to out, returns i (consumed length).
 *    - lookupMap.get(subSeq) == null across all iterations -> returns 0.
 *
 * 3. Defects4J Known Defect (LANG-882):
 *    - LookupTranslator failed to translate CharSequence keys when non-String CharSequence
 *      (e.g., StringBuffer, StringBuilder) was used because HashMap key identity/equals failed.
 *    - Ground Truth: testLang882 expects codepoints consumption from CharSequence keys.
 */
public class LookupTranslatorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicTranslationSingleMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "one", "1" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("one", 0, out);

        assertEquals("Consumed count should equal length of 'one'", 3, consumed);
        assertEquals("Output should be translated value", "1", out.toString());
    }

    @Test(timeout = 4000)
    public void testBasicTranslationMultipleEntries() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "cat", "feline" },
            { "dog", "canine" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final StringWriter out1 = new StringWriter();
        final int consumed1 = translator.translate("cat", 0, out1);
        assertEquals(3, consumed1);
        assertEquals("feline", out1.toString());

        final StringWriter out2 = new StringWriter();
        final int consumed2 = translator.translate("dog", 0, out2);
        assertEquals(3, consumed2);
        assertEquals("canine", out2.toString());
    }

    @Test(timeout = 4000)
    public void testTranslationAtNonZeroIndex() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "world", "earth" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("hello world", 6, out);

        assertEquals(5, consumed);
        assertEquals("earth", out.toString());
    }

    @Test(timeout = 4000)
    public void testGreedyPrefixMatching() throws IOException {
        // "ab" should take precedence over "a" when translating "abc"
        final CharSequence[][] lookup = new CharSequence[][] {
            { "a", "1" },
            { "ab", "2" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("abc", 0, out);

        assertEquals("Should consume longer prefix greedily", 2, consumed);
        assertEquals("Output should correspond to longer prefix", "2", out.toString());
    }

    @Test(timeout = 4000)
    public void testInheritedTranslateCharSequence() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "foo", "bar" },
            { "123", "456" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final String translated = translator.translate("foo 123 baz foo");

        assertEquals("bar 456 baz bar", translated);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullLookupArray() throws IOException {
        final LookupTranslator translator = new LookupTranslator((CharSequence[][]) null);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("test", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyLookupArray() throws IOException {
        final LookupTranslator translator = new LookupTranslator(new CharSequence[0][0]);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("test", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testInputShorterThanShortestKey() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "longkey", "val" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("sh", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testIndexNearEndOfInputTriggersMaxAdjustment() throws IOException {
        // longest is 5 ("apple"), input length is 4 ("appl")
        // index (0) + longest (5) > input.length (4) -> max = 4 - 0 = 4
        final CharSequence[][] lookup = new CharSequence[][] {
            { "apple", "fruit" },
            { "app", "application" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("appl", 0, out);

        assertEquals("Should match 'app'", 3, consumed);
        assertEquals("application", out.toString());
    }

    @Test(timeout = 4000)
    public void testNoMatchReturnsZeroAndOutputsNothing() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "alpha", "beta" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("omega", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyInputString() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "a", "b" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("", 0, out);

        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testSingleCharacterLookup() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "x", "y" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("x", 0, out);

        assertEquals(1, consumed);
        assertEquals("y", out.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-882)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLang882WithStringBufferKey() throws IOException {
        // LANG-882: LookupTranslator key defined as StringBuffer should match String and CharSequence
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuffer("one"), "two" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate(new StringBuffer("one"), 0, out);

        assertEquals("Incorrect codepoint consumption for StringBuffer key", 3, consumed);
        assertEquals("two", out.toString());
    }

    @Test(timeout = 4000)
    public void testLang882WithStringBuilderKeyAndStringInput() throws IOException {
        // LANG-882: LookupTranslator key defined as StringBuilder matched against String input
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuilder("hello"), new StringBuilder("world") }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("hello there", 0, out);

        assertEquals(5, consumed);
        assertEquals("world", out.toString());
    }

    @Test(timeout = 4000)
    public void testLang882ViaInheritedTranslate() {
        final CharSequence[][] lookup = new CharSequence[][] {
            { new StringBuffer("quick"), "slow" },
            { new StringBuilder("brown"), "white" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final String result = translator.translate("the quick brown fox");

        assertEquals("the slow white fox", result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorWithNullSubArrayThrowsNullPointer() {
        final CharSequence[][] lookup = new CharSequence[][] {
            null
        };
        new LookupTranslator(lookup);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testConstructorWithIncompleteSubArrayThrowsException() {
        final CharSequence[][] lookup = new CharSequence[][] {
            new CharSequence[] { "keyOnly" }
        };
        new LookupTranslator(lookup);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTranslateNullInputThrowsException() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "a", "b" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        translator.translate(null, 0, new StringWriter());
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTranslateNullWriterThrowsExceptionOnMatch() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "key", "val" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        translator.translate("key", 0, null);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testTranslateWriterThrowsIOExceptionPropagated() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "error", "throw" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final Writer failingWriter = new Writer() {
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                throw new IOException("Simulated write failure");
            }

            @Override
            public void write(final String str) throws IOException {
                throw new IOException("Simulated write failure");
            }

            @Override
            public void flush() throws IOException {}

            @Override
            public void close() throws IOException {}
        };

        translator.translate("error", 0, failingWriter);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Multiple Keys / Edge Mapping
    // =========================================================================

    @Test(timeout = 4000)
    public void testKeyReplacingExistingEntry() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "key", "value1" },
            { "key", "value2" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        final int consumed = translator.translate("key", 0, out);

        assertEquals(3, consumed);
        assertEquals("value2", out.toString());
    }

    @Test(timeout = 4000)
    public void testNullOutputMapping() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "key", null }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);
        final StringWriter out = new StringWriter();

        // When translation value is null, out.write("null") occurs
        final int consumed = translator.translate("key", 0, out);

        assertEquals(3, consumed);
        assertEquals("null", out.toString());
    }

    @Test(timeout = 4000)
    public void testVariableKeyLengthMinMaxTracking() throws IOException {
        final CharSequence[][] lookup = new CharSequence[][] {
            { "12345", "five" },
            { "12", "two" },
            { "12345678", "eight" },
            { "1", "one" }
        };
        final LookupTranslator translator = new LookupTranslator(lookup);

        final StringWriter out1 = new StringWriter();
        assertEquals(8, translator.translate("123456789", 0, out1));
        assertEquals("eight", out1.toString());

        final StringWriter out2 = new StringWriter();
        assertEquals(1, translator.translate("100", 0, out2));
        assertEquals("one", out2.toString());

        final StringWriter out3 = new StringWriter();
        assertEquals(2, translator.translate("120", 0, out3));
        assertEquals("two", out3.toString());
    }
}