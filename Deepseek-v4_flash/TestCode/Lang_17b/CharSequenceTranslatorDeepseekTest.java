package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Comprehensive test suite for CharSequenceTranslator.
 * Targets line/branch coverage and the known Defects4J defect (Lang720)
 * involving supplementary character handling.
 *
 * [Branch & Defect Analysis Matrix]
 * - translate(CharSequence): null input → null; non-null → delegates to translate(CharSequence, Writer)
 * - translate(CharSequence, Writer): null Writer → IllegalArgumentException; null input → silent return
 * - Loop: pos < len; consumed == 0 branch writes char via Character.toChars; consumed > 0 branch increments pos
 * - consumed > 0 inner loop: pos < len - 2 uses Character.charCount, else pos++
 * - pos decrement and final pos++ after loop
 * - with() method: array creation and AggregateTranslator delegation
 * - hex() method: uppercase hex string
 * - Defect: supplementary character (e.g., U+20BB7) when consumed == 0 should be written correctly,
 *   but bug in consumed > 0 branch may corrupt surrogate pairs.
 */
public class CharSequenceTranslatorDeepseekTest {

    // ---------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTranslateWithNullInputReturnsNull() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertNull("Null input should return null", translator.translate((CharSequence) null));
    }

    @Test(timeout = 4000)
    public void testTranslateWithEmptyInput() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        assertEquals("Empty input should produce empty output", "", translator.translate(""));
    }

    @Test(timeout = 4000)
    public void testTranslateWithBasicAscii() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                // consume 0 codepoints, write the character as-is
                return 0;
            }
        };
        assertEquals("Hello", translator.translate("Hello"));
    }

    @Test(timeout = 4000)
    public void testTranslateWithConsumedCodepoints() throws IOException {
        // Translator that consumes each codepoint and writes 'X'
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('X');
                return 1; // consume one codepoint
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate("ABC", sw);
        assertEquals("Each char replaced by X", "XXX", sw.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithMixedConsumedAndUnconsumed() throws IOException {
        // Translator that consumes only 'A' and writes 'a', otherwise returns 0
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                char c = input.charAt(index);
                if (c == 'A') {
                    out.write('a');
                    return 1;
                }
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate("AB", sw);
        assertEquals("A replaced by a, B unchanged", "aB", sw.toString());
    }

    // ---------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTranslateWithNullWriterThrowsException() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        try {
            translator.translate("test", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTranslateWithNullInputAndWriterDoesNotThrow() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(null, sw);
        assertEquals("Null input should produce empty output", "", sw.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithSingleSupplementaryCharacter() throws IOException {
        // Supplementary character U+20BB7 (𠮷)
        final String input = "𠮷";
        // Translator that returns 0 (writes the character as-is)
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(input, sw);
        assertEquals("Supplementary character should be preserved", input, sw.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateWithSupplementaryAndAscii() throws IOException {
        final String input = "A𠮷B";
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(input, sw);
        assertEquals("Mixed input should be preserved", input, sw.toString());
    }

    // ---------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Lang720)
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLang720DefectSupplementaryCharacterWithConsumedZero() {
        // This test directly targets the known defect: supplementary character
        // should not be replaced with '?' when translator returns 0.
        final String input = "𠮷A";
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                // Return 0 for all codepoints – original character should be written
                return 0;
            }
        };
        String result = translator.translate(input);
        assertEquals("Supplementary character must be preserved, not replaced with '?'", input, result);
    }

    @Test(timeout = 4000)
    public void testLang720DefectSupplementaryCharacterWithConsumedOne() throws IOException {
        // Translator that consumes each codepoint and writes 'X'
        final String input = "𠮷";
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('X');
                return 1; // consume one codepoint
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(input, sw);
        // The supplementary character is one codepoint, so it should be replaced by 'X'
        assertEquals("Supplementary codepoint consumed and replaced", "X", sw.toString());
    }

    @Test(timeout = 4000)
    public void testLang720DefectSupplementaryCharacterWithMixedConsumption() throws IOException {
        // Translator that consumes only the supplementary codepoint and writes 'S',
        // returns 0 for ASCII
        final String input = "A𠮷B";
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                int cp = Character.codePointAt(input, index);
                if (cp == 0x20BB7) {
                    out.write('S');
                    return 1;
                }
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(input, sw);
        assertEquals("Supplementary replaced by S, ASCII unchanged", "ASB", sw.toString());
    }

    // ---------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ---------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testTranslateWithNullWriterThrowsIllegalArgumentException() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        translator.translate("test", null);
    }

    @Test(timeout = 4000)
    public void testTranslateWithIOExceptionFromWriter() {
        // Simulate a Writer that throws IOException
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw new IOException("Simulated");
            }
        };
        try {
            translator.translate("test");
            fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException e) {
            assertTrue("Cause should be IOException", e.getCause() instanceof IOException);
        }
    }

    // ---------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ---------------------------------------------------------------

    @Test(timeout = 4000)
    public void testWithMethodCreatesAggregateTranslator() {
        CharSequenceTranslator base = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator extra = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator result = base.with(extra);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    @Test(timeout = 4000)
    public void testWithMethodWithMultipleTranslators() {
        CharSequenceTranslator base = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator t2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };
        CharSequenceTranslator result = base.with(t1, t2);
        assertTrue("Result should be AggregateTranslator", result instanceof AggregateTranslator);
    }

    @Test(timeout = 4000)
    public void testHexMethod() {
        assertEquals("Hex of 0 should be 0", "0", CharSequenceTranslator.hex(0));
        assertEquals("Hex of 10 should be A", "A", CharSequenceTranslator.hex(10));
        assertEquals("Hex of 255 should be FF", "FF", CharSequenceTranslator.hex(255));
        assertEquals("Hex of supplementary codepoint", "20BB7", CharSequenceTranslator.hex(0x20BB7));
    }

    @Test(timeout = 4000)
    public void testHexMethodUpperCase() {
        // Ensure hex returns uppercase
        assertEquals("Lowercase hex should be uppercase", "A", CharSequenceTranslator.hex(10));
        assertEquals("Lowercase hex should be uppercase", "FF", CharSequenceTranslator.hex(255));
    }

    // Additional edge case: translator that returns 0 but writes nothing (should still write char)
    @Test(timeout = 4000)
    public void testTranslateWithConsumedZeroAndNoWrite() throws IOException {
        // Translator that returns 0 but does not write anything – the framework should write the char
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                // do nothing, return 0
                return 0;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate("A", sw);
        assertEquals("Character should be written by framework", "A", sw.toString());
    }

    // Edge case: translator that consumes more than one codepoint (e.g., 2)
    @Test(timeout = 4000)
    public void testTranslateWithConsumedMultipleCodepoints() throws IOException {
        // Translator that consumes two codepoints and writes 'Z'
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('Z');
                return 2; // consume two codepoints
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate("ABCD", sw);
        // First two codepoints consumed -> 'Z', then next two consumed -> 'Z'
        assertEquals("Two codepoints consumed each time", "ZZ", sw.toString());
    }

    // Edge case: translator that consumes codepoints at end of input (boundary)
    @Test(timeout = 4000)
    public void testTranslateWithConsumedAtEnd() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('E');
                return 1;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate("X", sw);
        assertEquals("Single char consumed", "E", sw.toString());
    }

    // Edge case: input with only supplementary character and consumed > 0
    @Test(timeout = 4000)
    public void testTranslateSupplementaryWithConsumedOne() throws IOException {
        final String input = "𠮷";
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write('S');
                return 1;
            }
        };
        StringWriter sw = new StringWriter();
        translator.translate(input, sw);
        assertEquals("Supplementary codepoint consumed", "S", sw.toString());
    }
}