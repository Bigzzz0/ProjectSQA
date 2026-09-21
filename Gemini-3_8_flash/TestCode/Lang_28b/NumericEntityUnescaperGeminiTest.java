/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

/**
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang3.text.translate.NumericEntityUnescaper
 *
 * Decision / Branch Points Targeted:
 * 1. (charAt(index) == '&' && charAt(index + 1) == '#'):
 *    - False/False: Not entity start, returns 0.
 *    - True/False: '&' followed by non-'#' (e.g., "&amp;"), returns 0.
 *    - True/True: Entity candidate, enters unescaping block.
 * 2. (firstChar == 'x' || firstChar == 'X'):
 *    - True ('x'): Lowercase hex prefix, isHex = true, start++.
 *    - True ('X'): Uppercase hex prefix, isHex = true, start++.
 *    - False: Decimal entity, isHex = false.
 * 3. while(input.charAt(end) != ';'):
 *    - Semicolon found: parses substring [start, end).
 *    - Semicolon missing: IndexOutOfBoundsException boundary.
 * 4. isHex:
 *    - True: Integer.parseInt(..., 16)
 *    - False: Integer.parseInt(..., 10)
 * 5. catch (NumberFormatException nfe):
 *    - Empty entities ("&#;", "&#x;"), invalid chars ("&#xyz;"), integer overflow.
 * 6. Defects4J Known Defect (LANG / Supplementary Characters):
 *    - Defect: out.write(entityValue) writes a single 16-bit char instead of surrogate pair.
 *    - Supplementary codepoint (> 0xFFFF / U+10000..U+10FFFF) gets truncated to 16 bits.
 *    - E.g., codepoint 68642 (0x10C22) truncates to 0x0C22 ('ఢ') instead of surrogate pair ("\uD803\uDC22").
 */
public class NumericEntityUnescaperGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnescapeDecimal() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("A", neu.translate("&#65;"));
        assertEquals("z", neu.translate("&#122;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexLowerCase() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("A", neu.translate("&#x41;"));
        assertEquals("z", neu.translate("&#x7a;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHexUpperCase() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("A", neu.translate("&#X41;"));
        assertEquals("z", neu.translate("&#X7A;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeSurroundedByText() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        String input = "Prefix &#65; Middle &#x42; Suffix";
        String expected = "Prefix A Middle B Suffix";
        assertEquals(expected, neu.translate(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeMultipleEntities() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("ABC", neu.translate("&#65;&#66;&#67;"));
        assertEquals("ABC", neu.translate("&#x41;&#x42;&#x43;"));
    }

    @Test(timeout = 4000)
    public void testTranslateDirectConsumptionCount() throws IOException {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();

        // "&#65;" -> 2 (&#) + 2 (65) + 0 (isHex) + 1 (;) = 5
        int consumedDec = neu.translate("&#65;", 0, out);
        assertEquals(5, consumedDec);
        assertEquals("A", out.toString());

        out = new StringWriter();
        // "&#x41;" -> 2 (&#) + 2 (41) + 1 (isHex) + 1 (;) = 6
        int consumedHex = neu.translate("&#x41;", 0, out);
        assertEquals(6, consumedHex);
        assertEquals("A", out.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTranslateNullInput() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertNull(neu.translate(null));
    }

    @Test(timeout = 4000)
    public void testTranslateEmptyString() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("", neu.translate(""));
    }

    @Test(timeout = 4000)
    public void testTranslateNoEntities() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("Hello World!", neu.translate("Hello World!"));
    }

    @Test(timeout = 4000)
    public void testTranslateZeroCodePoint() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("\0", neu.translate("&#0;"));
        assertEquals("\0", neu.translate("&#x0;"));
    }

    @Test(timeout = 4000)
    public void testTranslateMaxBmpCodePoint() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // 0xFFFF = 65535 (last code point in Basic Multilingual Plane)
        assertEquals("\uFFFF", neu.translate("&#65535;"));
        assertEquals("\uFFFF", neu.translate("&#xFFFF;"));
        assertEquals("\uFFFF", neu.translate("&#xffff;"));
    }

    @Test(timeout = 4000)
    public void testTranslateControlCharacters() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("\n", neu.translate("&#10;"));
        assertEquals("\r", neu.translate("&#13;"));
        assertEquals("\t", neu.translate("&#9;"));
    }

    @Test(timeout = 4000)
    public void testTranslateMultibyteBmpCharacter() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Japanese Hiragana 'A' (U+3042 = 12354)
        assertEquals("あ", neu.translate("&#12354;"));
        assertEquals("あ", neu.translate("&#x3042;"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J failure:
     * NumericEntityUnescaperTest::testSupplementaryUnescaping
     * junit.framework.ComparisonFailure: Failed to unescape numeric entities
     * supplementary characters expected:<[...]> but was:<[ఢ]>
     *
     * 68642 (0x10C22) is a supplementary character.
     * The defect writes only 16 bits: 0x10C22 & 0xFFFF = 0x0C22 = 'ఢ'.
     * The fix correctly writes surrogate pairs.
     */
    @Test(timeout = 4000)
    public void testSupplementaryUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        int codePoint = 68642;
        String input = "&#" + codePoint + ";";
        String expected = new String(Character.toChars(codePoint));
        assertEquals("Failed to unescape numeric entities supplementary characters", expected, neu.translate(input));
    }

    @Test(timeout = 4000)
    public void testSupplementaryHexUnescaping() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        int codePoint = 0x10C22;
        String input = "&#x" + Integer.toHexString(codePoint) + ";";
        String expected = new String(Character.toChars(codePoint));
        assertEquals("Failed to unescape numeric entities supplementary characters (hex)", expected, neu.translate(input));
    }

    @Test(timeout = 4000)
    public void testSupplementaryBoundaryMin() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Smallest supplementary codepoint: 0x10000 = 65536
        int codePoint = 0x10000;
        String input = "&#" + codePoint + ";";
        String expected = new String(Character.toChars(codePoint));
        assertEquals("Failed to unescape min supplementary character", expected, neu.translate(input));
    }

    @Test(timeout = 4000)
    public void testSupplementaryBoundaryMax() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Largest valid Unicode codepoint: 0x10FFFF = 1114111
        int codePoint = 0x10FFFF;
        String input = "&#" + codePoint + ";";
        String expected = new String(Character.toChars(codePoint));
        assertEquals("Failed to unescape max supplementary character", expected, neu.translate(input));
    }

    @Test(timeout = 4000)
    public void testSupplementaryUnescapingDirectWriter() throws IOException {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        int codePoint = 68642;
        String input = "&#" + codePoint + ";";
        int consumed = neu.translate(input, 0, out);
        assertEquals(input.length(), consumed);
        assertEquals(new String(Character.toChars(codePoint)), out.toString());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testTranslateInvalidNumberDecimalIgnored() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // NumberFormatException caught, returns 0, entity preserved
        assertEquals("&#abc;", neu.translate("&#abc;"));
    }

    @Test(timeout = 4000)
    public void testTranslateInvalidNumberHexIgnored() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Invalid hex digits: 'z' is not a hex digit
        assertEquals("&#xzz;", neu.translate("&#xzz;"));
        assertEquals("&#XGG;", neu.translate("&#XGG;"));
    }

    @Test(timeout = 4000)
    public void testTranslateEmptyEntitiesIgnored() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // NumberFormatException on empty string in Integer.parseInt("", base)
        assertEquals("&#;", neu.translate("&#;"));
        assertEquals("&#x;", neu.translate("&#x;"));
        assertEquals("&#X;", neu.translate("&#X;"));
    }

    @Test(timeout = 4000)
    public void testTranslateNumberOverflowIgnored() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // Exceeds Integer.MAX_VALUE
        assertEquals("&#999999999999999999999999;", neu.translate("&#999999999999999999999999;"));
    }

    @Test(timeout = 4000)
    public void testTranslateAmpersandWithoutHash() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        assertEquals("&amp;", neu.translate("&amp;"));
        assertEquals("&", neu.translate("& "));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testTranslateIncompleteEntityAtEndOfStringThrowsException() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // charAt(index + 1) exceeds length when string ends with '&'
        neu.translate("&");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testTranslateMissingSemicolonThrowsException() {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        // while(charAt(end) != ';') runs off the end of input
        neu.translate("&#65");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectWriterConsumptionNonEntity() throws IOException {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        int consumed = neu.translate("Hello", 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateIntoProvidedWriter() throws IOException {
        NumericEntityUnescaper neu = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        neu.translate("Test &#65; string", out);
        assertEquals("Test A string", out.toString());
    }
}