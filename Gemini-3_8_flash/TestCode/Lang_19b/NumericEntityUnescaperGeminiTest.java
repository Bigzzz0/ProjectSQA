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

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang3.text.translate.NumericEntityUnescaper
 *
 * Decision / Condition Branch Points:
 * 1. (input.charAt(index) == '&' && index < seqEnd - 1 && input.charAt(index + 1) == '#')
 *    - False branch: index char is not '&' -> returns 0
 *    - False branch: index is at last character (index >= seqEnd - 1) -> returns 0
 *    - False branch: next char is not '#' (e.g., "&amp;") -> returns 0
 *    - True branch: starts with "&#"
 * 2. (firstChar == 'x' || firstChar == 'X')
 *    - True ('x'): hexadecimal entity format
 *    - True ('X'): uppercase hexadecimal entity format
 *    - False: decimal entity format
 * 3. while (input.charAt(end) != ';')
 *    - Loop condition until semi-colon encountered (Vulnerable to StringIndexOutOfBoundsException if ';' missing)
 * 4. try { Integer.parseInt(...) } catch (NumberFormatException)
 *    - Hexadecimal parsing vs Decimal parsing
 *    - Exception branch: unparseable numeric entity -> returns 0
 * 5. (entityValue > 0xFFFF)
 *    - True: Supplementary Unicode codepoint (> 65535) -> Character.toChars surrogate pair written
 *    - False: BMP codepoint (<= 65535) -> single character written
 *
 * Defects4J Ground Truth Vulnerabilities Targeted:
 * - testOutOfBounds: IndexOutOfBoundsException when "&#" or "&#x" is near or at the end of the input sequence.
 * - testUnfinishedEntity: IndexOutOfBoundsException when entity lacks terminating semi-colon (e.g. "&#x30a2").
 * ----------------------------------------------------------------------------------------------------
 */
public class NumericEntityUnescaperGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTranslateDecimalEntityWithSemicolon() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Letter &#65; and &#66;!";
        String expected = "Letter A and B!";
        assertEquals(expected, unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateHexEntityLowerXWithSemicolon() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Hex: &#x41; &#x42; &#x61;";
        String expected = "Hex: A B a";
        assertEquals(expected, unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateHexEntityUpperXWithSemicolon() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Hex: &#X43; &#X63;";
        String expected = "Hex: C c";
        assertEquals(expected, unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateSupplementaryCharacters() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        // Codepoint 0x1D306 (119558 in decimal) - Tetragram for Centre
        String inputDecimal = "Tetragram: &#119558;;";
        String expected = "Tetragram: \uD834\uDF06;";
        assertEquals(expected, unescaper.translate(inputDecimal));

        String inputHex = "Tetragram: &#x1D306;;";
        assertEquals(expected, unescaper.translate(inputHex));
    }

    @Test(timeout = 4000)
    public void testTranslateNonEntityText() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Normal text without entities & and &amp; and #hashtag";
        assertEquals(input, unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testDirectTranslateMethodReturnsConsumedLength() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        
        // Entity: "&#65;" -> length: 2 ('&#') + 2 ('65') + 0 (isHex) + 1 (';') = 5
        String input = "&#65;";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(5, consumed);
        assertEquals("A", out.toString());

        // Hex Entity: "&#x41;" -> length: 2 + 2 + 1 + 1 = 6
        StringWriter outHex = new StringWriter();
        String hexInput = "&#x41;";
        int consumedHex = unescaper.translate(hexInput, 0, outHex);
        assertEquals(6, consumedHex);
        assertEquals("A", outHex.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTranslateNullAndEmpty() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        assertNull(unescaper.translate(null));
        assertEquals("", unescaper.translate(""));
    }

    @Test(timeout = 4000)
    public void testTranslateZeroCodepoint() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "&#0;";
        assertEquals("\u0000", unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateMaxBmpCodepoint() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        // 0xFFFF is 65535
        String input = "&#65535;";
        assertEquals("\uFFFF", unescaper.translate(input));
    }

    @Test(timeout = 4000)
    public void testTranslateDirectNonStartingEntity() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "abc";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateDirectAmpersandNotFollowedByHash() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&abc;";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testTranslateDirectIndexAtEnd() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        String input = "&";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects)
    // =========================================================================

    /**
     * Targets Defects4J bug: StringIndexOutOfBoundsException on unfinished entities
     * where the trailing semi-colon is omitted.
     */
    @Test(timeout = 4000)
    public void testUnfinishedEntityWithoutSemicolonDefect() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        String input = "Test &#x30a2 & #301Entity... ";
        String expected = "Test \u30a2 & #301Entity... ";
        String result = unescaper.translate(input);
        assertEquals("Failed to support unfinished entities (i.e. missing semi-colon)", expected, result);
    }

    /**
     * Targets Defects4J bug: StringIndexOutOfBoundsException when input ends abruptly
     * with an ampersand or "&#" or "&#x" near sequence end.
     */
    @Test(timeout = 4000)
    public void testOutOfBoundsDefect() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        assertEquals("Failed to ignore when at last character", "Test &", unescaper.translate("Test &"));
        assertEquals("Failed to ignore when almost at last character", "Test &#", unescaper.translate("Test &#"));
        assertEquals("Failed to ignore when at last character", "Test &#x", unescaper.translate("Test &#x"));
        assertEquals("Failed to ignore when at last character", "Test &#X", unescaper.translate("Test &#X"));
    }

    @Test(timeout = 4000)
    public void testOutOfBoundsExactEnd() {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        assertEquals("&#", unescaper.translate("&#"));
        assertEquals("&#x", unescaper.translate("&#x"));
        assertEquals("&#X", unescaper.translate("&#X"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNumberFormatExceptionFallbackDecimal() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        // "NaN" cannot be parsed as decimal
        String input = "&#NaN;";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testNumberFormatExceptionFallbackHex() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        // "GHI" cannot be parsed as hex
        String input = "&#xGHI;";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }

    @Test(timeout = 4000)
    public void testNumberFormatExceptionEmptyNumber() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter out = new StringWriter();
        // Empty entity value
        String input = "&#;";
        int consumed = unescaper.translate(input, 0, out);
        assertEquals(0, consumed);
        assertEquals("", out.toString());
    }
}