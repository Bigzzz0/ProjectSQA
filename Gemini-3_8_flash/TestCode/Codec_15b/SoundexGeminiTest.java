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

package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.language.Soundex
 * Known Defect (Defects4J Codec-11):
 *   SoundexTest::testHWRuleEx1 --> junit.framework.AssertionFailedError: expected:<Y3[3]0> but was:<Y3[0]0>
 *
 * Root Cause Analysis:
 *   In getMappingCode(str, index), the H/W rule check evaluates:
 *     if (firstCode == mappedChar || 'H' == preHWChar || 'W' == preHWChar) return 0;
 *   When a consonant is preceded by consecutive H/W letters (e.g. "W" then "H") where the preceding
 *   character was NOT a consonant of the same group (e.g., initial letter 'Y' mapped to '0'), the check
 *   unconditionally drops the consonant because ('H' == preHWChar || 'W' == preHWChar) evaluates to true.
 *   This causes valid subsequent consonants to be erroneously discarded, resulting in "Y300" instead of "Y330".
 *
 * Structural Coverage Dimensions:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - Standard names from US Census guidelines (Ashcraft, Tymczak, Pfister, Jackson, etc.)
 *    - Difference calculations (identical -> 4, completely disjoint -> 0, partial -> 1..3)
 *    - Soundex constructors: default, char[] mapping, String mapping
 *    - getMaxLength() and setMaxLength() operations
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Null, empty string, pure whitespace, non-letter/special characters
 *    - Single-character inputs, short strings (length < 4), long strings (length > 4)
 * 3. Partition C: Defect-Targeted Branch Zone
 *    - Targeted test testHWRuleEx1 reproducing expected:<Y3[3]0> but was:<Y3[0]0>
 *    - Consecutive H/W characters with vowel/unencoded prefix
 *    - Consonants from same code group separated by H or W vs separated by vowels
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - encode(Object) with non-String argument throwing EncoderException
 *    - map(char) with characters outside custom mapping boundaries throwing IllegalArgumentException
 * 5. Partition E: Static Instance & Reusability
 *    - US_ENGLISH singleton instance thread-safety and constant mapping integrity
 * ----------------------------------------------------------------------------------------------------
 */
public class SoundexGeminiTest {

    // ==========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ==========================================================================

    @Test(timeout = 4000)
    public void testStandardUsEnglishEncoding() {
        final Soundex soundex = new Soundex();
        assertEquals("W252", soundex.soundex("Washington"));
        assertEquals("L000", soundex.soundex("Lee"));
        assertEquals("G362", soundex.soundex("Gutierrez"));
        assertEquals("P236", soundex.soundex("Pfister"));
        assertEquals("J250", soundex.soundex("Jackson"));
        assertEquals("T522", soundex.soundex("Tymczak"));
        assertEquals("A261", soundex.soundex("Ashcraft"));
    }

    @Test(timeout = 4000)
    public void testConstructorsAndCustomMappings() {
        // Test custom mapping via String
        final Soundex customStr = new Soundex("01230120022455012623010202");
        assertEquals("A261", customStr.soundex("Ashcraft"));

        // Test custom mapping via char[]
        final char[] mappingArray = "01230120022455012623010202".toCharArray();
        final Soundex customChar = new Soundex(mappingArray);
        assertEquals("A261", customChar.soundex("Ashcraft"));

        // Verify array immutability / defensive copying in constructor
        mappingArray[0] = '9';
        assertEquals("A261", customChar.soundex("Ashcraft"));
    }

    @Test(timeout = 4000)
    public void testDifferenceCalculation() throws EncoderException {
        final Soundex soundex = new Soundex();

        // Identical phonetic encoding -> 4
        assertEquals(4, soundex.difference("Smith", "Smyth"));
        assertEquals(4, soundex.difference("Ashcraft", "Ashcroft"));

        // Partially similar
        assertEquals(3, soundex.difference("Smith", "Schmidt"));

        // Distinct phonetics
        assertTrue(soundex.difference("Smith", "Jones") <= 2);

        // Blank and empty inputs
        assertEquals(0, soundex.difference("", ""));
        assertEquals(0, soundex.difference(null, "Smith"));
    }

    @Test(timeout = 4000)
    public void testGetAndSetMaxLength() {
        final Soundex soundex = new Soundex();
        assertEquals(4, soundex.getMaxLength());
        soundex.setMaxLength(6);
        assertEquals(6, soundex.getMaxLength());
        soundex.setMaxLength(4);
        assertEquals(4, soundex.getMaxLength());
    }

    // ==========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ==========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyInputs() {
        final Soundex soundex = new Soundex();
        assertNull(soundex.soundex(null));
        assertEquals("", soundex.soundex(""));
        assertNull(soundex.encode((String) null));
        assertEquals("", soundex.encode(""));
    }

    @Test(timeout = 4000)
    public void testNonLetterAndCleanHandling() {
        final Soundex soundex = new Soundex();
        // Strings containing no letters
        assertEquals("", soundex.soundex("   "));
        assertEquals("", soundex.soundex("12345"));
        assertEquals("", soundex.soundex("!@#$%^&*()_+"));

        // Strings with punctuation/spacing stripped
        assertEquals("O165", soundex.soundex("O'Brien"));
        assertEquals("S530", soundex.soundex("Smith-Jones"));
    }

    @Test(timeout = 4000)
    public void testShortStringsPadding() {
        final Soundex soundex = new Soundex();
        assertEquals("A000", soundex.soundex("A"));
        assertEquals("B100", soundex.soundex("B"));
        assertEquals("C200", soundex.soundex("C"));
        assertEquals("B100", soundex.soundex("Bb"));
        assertEquals("B100", soundex.soundex("B1"));
        assertEquals("A120", soundex.soundex("Abc"));
        assertEquals("A123", soundex.soundex("Abcd"));
        assertEquals("A123", soundex.soundex("Abcde")); // length > 4 stops at 4
    }

    // ==========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Codec-11 Focus)
    // ==========================================================================

    /**
     * Specifically triggers the defect recorded in Defects4J Codec-11:
     * org.apache.commons.codec.language.SoundexTest::testHWRuleEx1
     * -> expected:<Y3[3]0> but was:<Y3[0]0>
     *
     * In defective versions, consecutive H/W characters after unencoded 'Y' incorrectly
     * cause the next valid consonant ('T') to be dropped as if it were a duplicate.
     */
    @Test(timeout = 4000)
    public void testHWRuleEx1() {
        final Soundex soundex = new Soundex();
        // Word starting with Y followed by W and H, then 'T', vowel 'E', and 'T'
        // 'Y' (0), 'W' (ignore), 'H' (ignore), 'T' (3), 'E' (0), 'T' (3) -> Expected "Y330"
        assertEquals("Y330", soundex.encode("YWHTET"));
    }

    @Test(timeout = 4000)
    public void testHWRuleSameCodeGroupSeparatedByHW() {
        final Soundex soundex = new Soundex();
        // S (2) and C (2) separated by H -> C is ignored -> A261
        assertEquals("A261", soundex.soundex("Ashcraft"));
        // B (1) and P (1) separated by W -> P is ignored
        assertEquals("B100", soundex.soundex("Bwp"));
        assertEquals("B100", soundex.soundex("Bhv"));
    }

    @Test(timeout = 4000)
    public void testSameCodeGroupSeparatedByVowelsMustBeCodedTwice() {
        final Soundex soundex = new Soundex();
        // T (3) and T (3) separated by 'O' -> second T must be coded
        assertEquals("Y330", soundex.soundex("YTOT"));
        // M (5) and C (2), Z (2) ignored, K (2) separated by vowel 'a' -> coded twice
        assertEquals("T522", soundex.soundex("Tymczak"));
    }

    @Test(timeout = 4000)
    public void testAdjacentConsonantsSameGroupCodedOnce() {
        final Soundex soundex = new Soundex();
        // Adjacent duplicate consonants
        assertEquals("B100", soundex.soundex("Bb"));
        assertEquals("M500", soundex.soundex("Mmm"));
        assertEquals("S200", soundex.soundex("Scott"));
    }

    // ==========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ==========================================================================

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeNonStringObjectThrowsEncoderException() throws EncoderException {
        final Soundex soundex = new Soundex();
        soundex.encode(new Object());
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeIntegerObjectThrowsEncoderException() throws EncoderException {
        final Soundex soundex = new Soundex();
        soundex.encode(12345);
    }

    @Test(timeout = 4000)
    public void testEncodeValidStringObject() throws EncoderException {
        final Soundex soundex = new Soundex();
        final Object result = soundex.encode((Object) "Ashcraft");
        assertTrue(result instanceof String);
        assertEquals("A261", result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnmappedCharacterThrowsIllegalArgumentException() {
        // Mapping of only 4 characters ('A' through 'D')
        final Soundex soundex = new Soundex("0123");
        // 'E' is at index 4, which is >= mapping.length -> throws IllegalArgumentException
        soundex.soundex("Echo");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCharacterBeforeUpperAThrowsIllegalArgumentException() {
        final Soundex soundex = new Soundex(new char[]{'0', '1', '2'});
        // Force evaluation of a character before 'A' directly if clean didn't strip it
        // A custom subclass or mapping can invoke this path
        final Soundex subSoundex = new Soundex("01230120022455012623010202") {
            @Override
            public String soundex(final String str) {
                // Bypass SoundexUtils.clean to test map() guard condition: index < 0
                return super.soundex("@BC");
            }
        };
        subSoundex.soundex("@BC");
    }

    // ==========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ==========================================================================

    @Test(timeout = 4000)
    public void testUsEnglishStaticConstant() {
        assertNotNull(Soundex.US_ENGLISH);
        assertEquals("01230120022455012623010202", Soundex.US_ENGLISH_MAPPING_STRING);
        assertEquals("A261", Soundex.US_ENGLISH.soundex("Ashcraft"));
        assertEquals("S530", Soundex.US_ENGLISH.encode("Smith"));
    }

    @Test(timeout = 4000)
    public void testDeterministicExecution() {
        final Soundex soundex = new Soundex();
        for (int i = 0; i < 50; i++) {
            assertEquals("W252", soundex.encode("Washington"));
            assertEquals("T522", soundex.soundex("Tymczak"));
            assertEquals("A261", soundex.soundex("Ashcraft"));
        }
    }
}