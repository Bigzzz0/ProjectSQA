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
import java.io.Writer;

import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for NumericEntityUnescaper.
 *
 * <p><b>[Branch & Defect Analysis Matrix]</b></p>
 * <ul>
 *   <li><b>Branch 1:</b> `input.charAt(index) == '&' && input.charAt(index + 1) == '#'`</li>
 *   <li><b>Branch 1a:</b> false → return 0 (no entity)</li>
 *   <li><b>Branch 1b:</b> true → proceed to parse</li>
 *   <li><b>Branch 2:</b> `firstChar == 'x' || firstChar == 'X'` (hex vs decimal)</li>
 *   <li><b>Branch 2a:</b> true → isHex = true, start++</li>
 *   <li><b>Branch 2b:</b> false → isHex = false</li>
 *   <li><b>Branch 3:</b> `while(input.charAt(end) != ';')` – normal termination</li>
 *   <li><b>Branch 3a:</b> missing semicolon → infinite loop / exception (but method does not guard)</li>
 *   <li><b>Branch 4:</b> `Integer.parseInt(...)` throws NumberFormatException → return 0</li>
 *   <li><b>Branch 5:</b> `out.write(entityValue)` – only writes low 16 bits for values > 0xFFFF</li>
 * </ul>
 * <p>Known defect: supplementary characters (U+10000+) are incorrectly written as a single char instead of surrogate pair.</p>
 */
public class NumericEntityUnescaperDeepseekTest {

    // ===== Partition A: Core functional logic & state transitions =====

    @Test(timeout = 4000)
    public void testSimpleDecimal() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#65;";  // 'A'
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should have consumed all characters of the entity", 5, consumed);
        assertEquals("Should output 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testSimpleHexLowercase() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#x41;";  // 'A'
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should have consumed all characters of the entity", 6, consumed);
        assertEquals("Should output 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testSimpleHexUppercase() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#X41;";  // 'A'
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should have consumed all characters of the entity", 6, consumed);
        assertEquals("Should output 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testBMPMaxChar() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#65535;";  // 0xFFFF
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should have consumed all characters of the entity", 9, consumed);
        assertEquals("Should output one character of value 0xFFFF", 1, writer.toString().length());
        assertEquals("Character should be 0xFFFF", (char) 0xFFFF, writer.toString().charAt(0));
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testMinimalEntity() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#0;";  // null character
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should have consumed the full entity", 4, consumed);
        assertEquals("Should output null character", 1, writer.toString().length());
        assertEquals("Character should be 0", '\0', writer.toString().charAt(0));
    }

    @Test(timeout = 4000)
    public void testLeadingAmpersandOnlyNoHash() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&AB;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should not consume anything", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }

    @Test(timeout = 4000)
    public void testHashOnlyNoAmpersand() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "#65;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should not consume anything (no leading &)", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }

    @Test(timeout = 4000)
    public void testEntityWithLeadingZeros() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#00065;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should consume all characters", 8, consumed);
        assertEquals("Should output 'A'", "A", writer.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyNumericPart() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#;";
        // This will cause NumberFormatException because substring start=2, end=2 gives empty string
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should return 0 due to NumberFormatException on empty number", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }

    @Test(timeout = 4000)
    public void testInvalidHexCharacter() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#xGG;";
        // 'G' is not hex -> NumberFormatException
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should return 0 due to NumberFormatException", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }

    // ===== Partition C: Defect-Targeted Branch Zone (Supplementary Characters) ====

    /**
     * Tests the known defect: supplementary characters (U+10000+) are not correctly translated.
     * The bug: out.write(entityValue) writes only low 16 bits, resulting in an incorrect character.
     * Expected correct behavior: write the supplementary codepoint, which should produce a 2-char surrogate pair.
     */
    @Test(timeout = 4000)
    public void testSupplementaryHex() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        // U+10A20 (old Turkic letter Orkhon)
        String input = "&#x10A20;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should consume all characters of the entity", 10, consumed);
        String output = writer.toString();
        // If the bug exists, output length will be 1 and character will be wrong (e.g., Telugu ఢ U+0C22)
        // Correct handling should produce two chars (surrogate pair) representing U+10A20.
        assertEquals("Supplementary character should produce a 2-char string (surrogate pair)", 2, output.length());
        int codepoint = output.codePointAt(0);
        assertEquals("Codepoint should be 0x10A20", 0x10A20, codepoint);
    }

    @Test(timeout = 4000) // intentional typo? Fix: timeout=4000
    public void testSupplementaryDecimal() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        // Decimal for U+1D306 (TETRAGRAM FOR CENTRE) = 119558
        String input = "&#119558;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should consume all characters of the entity", 10, consumed);
        String output = writer.toString();
        assertEquals("Supplementary character should produce a 2-char string (surrogate pair)", 2, output.length());
        int codepoint = output.codePointAt(0);
        assertEquals("Codepoint should be 0x1D306", 0x1D306, codepoint);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = StringIndexOutOfBoundsException.class, timeout = 4000)
    public void testIndexOutOfBounds() throws IOException {
        // If index points near end of input, charAt(index+1) may throw
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&";
        // index = 0 -> input.charAt(0) = '&', but input.charAt(1) fails (length=1)
        unescaper.translate(input, 0, writer);
    }

    @Test(timeout = 4000)
    public void testNumberFormatExceptionOnNonDigit() throws IOException {
        NumericEntityUnescaper unescaper = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#ABC;";
        int consumed = unescaper.translate(input, 0, writer);
        assertEquals("Should return 0 due to NumberFormatException", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    // no equals/hashCode/clone/Serializable available, but we verify
    // that the translator correctly processes multiple entities in a sequence.

    @Test(timeout = 4000)
    public void testMultipleEntities() throws IOException {
        NumericEntityUnescaper unesc = new NumericEntityUnescaper();
        StringWriter writer = new StringWriter();
        String input = "&#65;&#66;";  // "AB"
        int pos = 0;
        int consumed;
        while (pos < input.length()) {
            consumed = unesc.translate(input, pos, writer);
            if (consumed == 0) break;
            pos += consumed;
        }
        assertEquals("Should have consumed entire string", input.length(), pos);
        assertEquals("Should output 'AB'", "AB", writer.toString());
    }

    @Test(timeout = 4000)
    public void testNonEntityTextPreserved() throws IOEXception {
        // Note: the translate method only processes entities at the given index.
        // It does not skip non-entity text. For a full unescape, we typically call translate(CharSequence, Writer).
        // We test that when called with index pointing to non-entity, it returns 0.
        NumericEntityUnescaper unesc = new NumericEntityUnescaper();
        StringWriter writer = new StringWrier();
        String input = "Hello";
        int consumed = unesc.translate(input, 0, writer);
        assertEquals("Should not consume anything", 0, consumed);
        assertEquals("Should output nothing", "", writer.toString());
    }
}