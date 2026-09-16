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

package org.apache.commons.codec.language.bm;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.codec.language.bm.PhoneticEngine
 *
 * Branch & Decision Coverage Map:
 * 1. Constructor Guard:
 *    - ruleType == RuleType.RULES -> throws IllegalArgumentException
 *    - ruleType != RuleType.RULES -> instantiates correctly (3-param and 4-param variants)
 * 2. Prefix Handling (encode):
 *    - nameType == NameType.GENERIC:
 *      * input starts with "d'" -> prefix split recursion
 *      * input starts with recognized generic prefix (e.g., "van ", "da ", "de la ") -> split recursion
 *      * input without prefix -> standard processing
 *    - nameType == NameType.SEPHARDIC:
 *      * apostrophe splitting: words split by "'" and prefixes removed
 *    - nameType == NameType.ASHKENAZI:
 *      * prefix stripping ("bar", "ben", "van", "von", etc.)
 * 3. Multi-word & Concat Branches:
 *    - concat == true -> join(words2, " ")
 *    - concat == false && words2.size() == 1 -> input = words.iterator().next()
 *    - concat == false && words2.size() > 1 -> hyphen-separated independent encodings
 * 4. maxPhonemes Limiter & PhonemeBuilder:
 *    - PhonemeBuilder.apply(): newPhonemes.size() < maxPhonemes vs >= maxPhonemes (early break EXPR)
 *    - PhonemeBuilder.makeString(): empty builder, single phoneme, multiple phonemes with pipe delimiter
 * 5. Defects4J Ground Truth Target:
 *    - PhoneticEngineRegressionTest::testCompatibilityWithOriginalVersion failure:
 *      expected:<...dzn|bntsn|bnzn|vndzn[]> but was:<...dzn|bntsn|bnzn|vndzn[|vntsn]>
 *      Input: "bendzin" with NameType.GENERIC, RuleType.APPROX, concat=true.
 */
public class PhoneticEngineGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndGettersDefaultMaxPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        assertEquals(NameType.GENERIC, engine.getNameType());
        assertEquals(RuleType.APPROX, engine.getRuleType());
        assertTrue(engine.isConcat());
        assertEquals(20, engine.getMaxPhonemes());
        assertNotNull(engine.getLang());
    }

    @Test(timeout = 4000)
    public void testConstructorAndGettersCustomMaxPhonemes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.EXACT, false, 10);
        assertEquals(NameType.ASHKENAZI, engine.getNameType());
        assertEquals(RuleType.EXACT, engine.getRuleType());
        assertFalse(engine.isConcat());
        assertEquals(10, engine.getMaxPhonemes());
        assertNotNull(engine.getLang());
    }

    @Test(timeout = 4000)
    public void testEncodeSimpleWordGenericApprox() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String encoded = engine.encode("smith");
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEncodeSimpleWordAshkenaziExact() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.EXACT, true);
        String encoded = engine.encode("cohen");
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEncodeSimpleWordSephardicApprox() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, true);
        String encoded = engine.encode("toledo");
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEncodeWithExplicitLanguageSet() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        Set<String> langs = new HashSet<String>();
        langs.add("english");
        Languages.LanguageSet customSet = Languages.LanguageSet.from(langs);

        String encoded = engine.encode("baker", customSet);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test(timeout = 4000)
    public void testMultiWordNotConcatMultipleWords() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, false);
        String encoded = engine.encode("Smith Jones");
        assertTrue("Multi-word non-concat must contain hyphens", encoded.contains("-"));

        String single1 = engine.encode("Smith");
        String single2 = engine.encode("Jones");
        assertEquals(single1 + "-" + single2, encoded);
    }

    @Test(timeout = 4000)
    public void testMultiWordNotConcatSingleWordRemainderAshkenazi() {
        // "ben" is stripped by Ashkenazi prefix logic, leaving only "gurion"
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, false);
        String encoded = engine.encode("ben Gurion");
        assertNotNull(encoded);
        assertFalse("Single remaining word should not have leading/trailing hyphens", encoded.startsWith("-"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeGenericPrefixDApostrophe() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        String result = engine.encode("d'angelo");
        assertTrue("Prefix d' must result in paren pattern (remainder)-(combined)",
                result.startsWith("(") && result.contains(")-("));
    }

    @Test(timeout = 4000)
    public void testEncodeGenericPrefixSpaced() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        String result = engine.encode("van der bilt");
        assertTrue("Recognized prefix must trigger split encoding",
                result.startsWith("(") && result.contains(")-("));
    }

    @Test(timeout = 4000)
    public void testEncodeSephardicApostropheSplit() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.EXACT, true);
        // Sephardic splits words on apostrophe and takes the last part
        String result = engine.encode("habib'el");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testEncodeInputNormalizationHyphensAndWhitespace() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        String resultWithHyphens = engine.encode(" Jean - Paul ");
        String resultWithSpaces = engine.encode("jean paul");
        assertEquals("Hyphens and extra whitespace should be normalized equally",
                resultWithSpaces, resultWithHyphens);
    }

    @Test(timeout = 4000)
    public void testMaxPhonemesLimiterOne() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, 1);
        String result = engine.encode("alexander");
        assertFalse("maxPhonemes=1 must contain at most 1 phoneme (no pipes)", result.contains("|"));
    }

    @Test(timeout = 4000)
    public void testMaxPhonemesLimiterSmallBoundary() {
        final int limit = 2;
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, limit);
        String result = engine.encode("alexander");
        String[] parts = result.split("\\|");
        assertTrue("Output phonemes count must not exceed maxPhonemes boundary", parts.length <= limit);
    }

    @Test(timeout = 4000)
    public void testPhonemeBuilderDirectLifecycle() {
        Languages.LanguageSet anyLang = Languages.ANY_LANGUAGE;
        PhoneticEngine.PhonemeBuilder pb = PhoneticEngine.PhonemeBuilder.empty(anyLang);
        assertNotNull(pb);
        assertEquals("", pb.makeString());
        assertEquals(1, pb.getPhonemes().size());

        pb.append("abc");
        assertEquals("abc", pb.makeString());

        pb.append("def");
        assertEquals("abcdef", pb.makeString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect identified in Defects4J:
     * PhoneticEngineRegressionTest::testCompatibilityWithOriginalVersion
     * Failure: expected:<...dzn|bntsn|bnzn|vndzn[]> but was:<...dzn|bntsn|bnzn|vndzn[|vntsn]>
     */
    @Test(timeout = 4000)
    public void testCompatibilityWithOriginalVersion() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        assertEquals("Defects4J compatibility failure for input 'bendzin'",
                "bndzn|bntsn|bnzn|vndzn", engine.encode("bendzin"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorRejectsRuleTypeRules3Args() {
        new PhoneticEngine(NameType.GENERIC, RuleType.RULES, true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorRejectsRuleTypeRules4Args() {
        new PhoneticEngine(NameType.ASHKENAZI, RuleType.RULES, false, 5);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testEncodeNullInputThrowsNpe() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        engine.encode(null);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testEncodeNullLanguageSetThrowsNpe() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        engine.encode("test", null);
    }
}