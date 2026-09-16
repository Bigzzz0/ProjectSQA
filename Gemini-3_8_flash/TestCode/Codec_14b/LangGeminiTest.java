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

import java.util.Locale;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * TARGET CLASS: org.apache.commons.codec.language.bm.Lang
 *
 * DECISION BRANCHES & CONDITION PATHS EVALUATED:
 * 1. static { ... }: Initialization of Lang instances for all NameType values (ASHKENAZI, GENERIC, SEPHARDIC).
 * 2. Lang.instance(NameType):
 *    - nameType in EnumMap (ASHKENAZI, GENERIC, SEPHARDIC) -> non-null instance.
 *    - nameType == null -> returns null.
 * 3. Lang.loadFromResource(String, Languages):
 *    - lRulesIS == null -> throws IllegalStateException("Unable to resolve required resource:...").
 *    - scanner reading loop:
 *      - inExtendedComment == true, endsWith("*/") -> inExtendedComment = false.
 *      - inExtendedComment == true, !endsWith("*/") -> line discarded, stays in comment mode.
 *      - inExtendedComment == false, startsWith("/*") -> inExtendedComment = true.
 *      - inExtendedComment == false, startsWith("//") or contains("//") -> discards comment.
 *      - trimmed line.length() == 0 -> skip.
 *      - parts.length != 3 -> throws IllegalArgumentException("Malformed line...").
 *      - parts.length == 3 -> Pattern.compile(), split("+"), parse boolean, adds LangRule.
 * 4. LangRule.matches(String): Pattern matcher regex evaluation on text.
 * 5. guessLanguage(String):
 *    - ls.isSingleton() == true -> returns ls.getAny().
 *    - ls.isSingleton() == false -> returns Languages.ANY.
 * 6. guessLanguages(String):
 *    - input.toLowerCase(Locale.ENGLISH) normalization.
 *    - for rule in rules:
 *      - rule.matches(text) == true:
 *        - rule.acceptOnMatch == true -> retainAll(rule.languages)
 *        - rule.acceptOnMatch == false -> removeAll(rule.languages)
 *      - rule.matches(text) == false -> no-op
 *    - ls.equals(NO_LANGUAGES) -> returns ANY_LANGUAGE; else returns ls.
 *
 * DEFECT CORRELATION (Defects4J - PhoneticEngineRegressionTest::testCompatibilityWithOriginalVersion):
 * - Ground truth defect reveals over-permissive or incorrect language retention for compound/prefix
 *   words (such as Dutch 'van' vs German 'von'), which incorrectly retained German rules causing
 *   unwanted phonetic transformations (e.g. 'vntsn' instead of expected 'vndzn').
 * - Targeted tests assert precise language discrimination for Dutch vs German prefixes ("van", "van der",
 *   "von", "von der") to verify that conflicting languages are correctly eliminated.
 * ====================================================================================================
 */
public class LangGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstanceRetrievalForAllNameTypes() {
        for (final NameType nameType : NameType.values()) {
            final Lang lang = Lang.instance(nameType);
            assertNotNull("Lang instance should not be null for " + nameType, lang);
        }
    }

    @Test(timeout = 4000)
    public void testGuessLanguageSingletonMatches() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // English unambiguous match
        assertEquals("english", genericLang.guessLanguage("smith"));
        assertEquals("english", genericLang.guessLanguage("o'flaherty"));

        // Italian unambiguous match
        assertEquals("italian", genericLang.guessLanguage("rossi"));

        // French unambiguous match
        assertEquals("french", genericLang.guessLanguage("dupont"));

        // Spanish unambiguous match
        assertEquals("spanish", genericLang.guessLanguage("garcia"));

        // Polish unambiguous match
        assertEquals("polish", genericLang.guessLanguage("kowalczyk"));
        assertEquals("polish", genericLang.guessLanguage("czernik"));

        // German unambiguous match
        assertEquals("german", genericLang.guessLanguage("schmidt"));

        // Portuguese unambiguous match
        assertEquals("portuguese", genericLang.guessLanguage("da silva"));
    }

    @Test(timeout = 4000)
    public void testGuessLanguageAmbiguousYieldsAny() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // An ambiguous or unrecognized word should return Languages.ANY
        final String language = genericLang.guessLanguage("al-mansoor");
        assertEquals("Expected ANY for ambiguous word", Languages.ANY, language);
    }

    @Test(timeout = 4000)
    public void testGuessLanguagesReturnsDetailedSet() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        final Languages.LanguageSet langSet = genericLang.guessLanguages("smith");
        assertNotNull("LanguageSet must not be null", langSet);
        assertTrue("Expected singleton language set for unambiguous word", langSet.isSingleton());
        assertTrue("LanguageSet should be restricted", langSet.isRestricted());
        assertEquals("english", langSet.getAny());
        assertTrue(langSet.contains("english"));
        assertFalse(langSet.contains("french"));
    }

    @Test(timeout = 4000)
    public void testAshkenaziLanguageGuessing() {
        final Lang ashkenaziLang = Lang.instance(NameType.ASHKENAZI);
        assertNotNull("Ashkenazi Lang should exist", ashkenaziLang);

        final Languages.LanguageSet ls = ashkenaziLang.guessLanguages("shapiro");
        assertNotNull("LanguageSet should not be null", ls);
        assertFalse("LanguageSet should not be empty", ls.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSephardicLanguageGuessing() {
        final Lang sephardicLang = Lang.instance(NameType.SEPHARDIC);
        assertNotNull("Sephardic Lang should exist", sephardicLang);

        final Languages.LanguageSet ls = sephardicLang.guessLanguages("toledano");
        assertNotNull("LanguageSet should not be null", ls);
        assertFalse("LanguageSet should not be empty", ls.isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCaseInsensitiveNormalization() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // Verify lowercase, uppercase, and mixed case evaluate identically
        assertEquals("english", genericLang.guessLanguage("smith"));
        assertEquals("english", genericLang.guessLanguage("SMITH"));
        assertEquals("english", genericLang.guessLanguage("Smith"));
        assertEquals("english", genericLang.guessLanguage("sMiTh"));

        assertEquals("german", genericLang.guessLanguage("SCHMIDT"));
        assertEquals("italian", genericLang.guessLanguage("ROSSI"));
    }

    @Test(timeout = 4000)
    public void testEmptyStringInput() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        final Languages.LanguageSet ls = genericLang.guessLanguages("");
        assertNotNull("LanguageSet for empty string must not be null", ls);
        assertFalse("Empty string should not yield singleton", ls.isSingleton());
        assertEquals(Languages.ANY, genericLang.guessLanguage(""));
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyInput() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        final Languages.LanguageSet ls = genericLang.guessLanguages("   \t  \n");
        assertNotNull("LanguageSet for whitespace must not be null", ls);
        assertEquals(Languages.ANY, genericLang.guessLanguage("   \t  \n"));
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInput() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        final Languages.LanguageSet ls = genericLang.guessLanguages("a");
        assertNotNull("LanguageSet for single char must not be null", ls);
        assertEquals(Languages.ANY, genericLang.guessLanguage("a"));
    }

    @Test(timeout = 4000)
    public void testNonAsciiCharacters() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // Cyrillic script
        final Languages.LanguageSet cyrillicSet = genericLang.guessLanguages("иванов");
        assertNotNull(cyrillicSet);
        assertTrue(cyrillicSet.contains("cyrillic"));

        // Hebrew script
        final Languages.LanguageSet hebrewSet = genericLang.guessLanguages("כהן");
        assertNotNull(hebrewSet);
        assertTrue(hebrewSet.contains("hebrew"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J PhoneticEngine Issue)
    // =========================================================================

    /**
     * Targets the root cause of the regression in PhoneticEngineRegressionTest where
     * Dutch words (e.g. "van ...") incorrectly produced German phonetic equivalents
     * (e.g. 'vntsn' instead of expected 'vndzn') due to improper language exclusion.
     */
    @Test(timeout = 4000)
    public void testDefectDutchVsGermanExclusionInLanguageGuessing() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // "van der bilt" and "van der merwe" are quintessential Dutch names with prefix "van der"
        final Languages.LanguageSet dutchSet = genericLang.guessLanguages("van der merwe");
        assertNotNull(dutchSet);
        assertTrue("Dutch must be in the guessed languages", dutchSet.contains("dutch"));
        assertFalse("German MUST NOT be retained for 'van der merwe'", dutchSet.contains("german"));
        assertEquals("dutch", genericLang.guessLanguage("van der merwe"));

        final Languages.LanguageSet vanDerBiltSet = genericLang.guessLanguages("van der bilt");
        assertTrue("Must contain dutch", vanDerBiltSet.contains("dutch"));
        assertFalse("Must not contain german", vanDerBiltSet.contains("german"));

        // Conversely, "von" should identify German/Austrian and strictly eliminate Dutch
        final Languages.LanguageSet germanSet = genericLang.guessLanguages("von halle");
        assertNotNull(germanSet);
        assertTrue("German must be guessed for 'von halle'", germanSet.contains("german"));
        assertFalse("Dutch must NOT be retained for 'von halle'", germanSet.contains("dutch"));

        // Verify words related to the exact regression tokens (vndzn vs vntsn)
        final Languages.LanguageSet vandSet = genericLang.guessLanguages("vandusen");
        assertNotNull(vandSet);
        assertFalse("Guessed language set must not be empty", vandSet.isEmpty());
    }

    @Test(timeout = 4000)
    public void testExclusionRulesTriggerLanguageRemoval() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);

        // Distinct linguistic markers that rule out specific families
        final Languages.LanguageSet polishSet = genericLang.guessLanguages("przemyslaw");
        assertTrue("Must contain polish", polishSet.contains("polish"));
        assertFalse("Must rule out english", polishSet.contains("english"));
        assertFalse("Must rule out spanish", polishSet.contains("spanish"));

        final Languages.LanguageSet spanishSet = genericLang.guessLanguages("sanchez");
        assertTrue("Must contain spanish", spanishSet.contains("spanish"));
        assertFalse("Must rule out german", spanishSet.contains("german"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGuessLanguageNullInputThrowsException() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);
        genericLang.guessLanguage(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGuessLanguagesNullInputThrowsException() {
        final Lang genericLang = Lang.instance(NameType.GENERIC);
        genericLang.guessLanguages(null);
    }

    @Test(timeout = 4000)
    public void testLoadFromResourceNotFoundThrowsIllegalStateException() {
        try {
            Lang.loadFromResource("non/existent/path/rules.txt", Languages.getInstance(NameType.GENERIC));
            fail("Expected IllegalStateException for missing resource");
        } catch (final IllegalStateException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Unable to resolve required resource:"));
        }
    }

    @Test(timeout = 4000)
    public void testLoadFromResourceMalformedLineThrowsIllegalArgumentException() {
        // ash_languages.txt has single-word lines (not 3-column rule lines), triggering malformed line error
        try {
            Lang.loadFromResource(
                    "org/apache/commons/codec/language/bm/ash_languages.txt",
                    Languages.getInstance(NameType.ASHKENAZI)
            );
            fail("Expected IllegalArgumentException for malformed rule line");
        } catch (final IllegalArgumentException e) {
            assertNotNull(e.getMessage());
            assertTrue("Message must indicate malformed line", e.getMessage().contains("Malformed line"));
            assertTrue("Message must contain resource name", e.getMessage().contains("ash_languages.txt"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstanceCachingIdentity() {
        final Lang langGen1 = Lang.instance(NameType.GENERIC);
        final Lang langGen2 = Lang.instance(NameType.GENERIC);
        assertSame("Lang instances for the same NameType must be identical (cached singleton)", langGen1, langGen2);

        final Lang langAsh = Lang.instance(NameType.ASHKENAZI);
        final Lang langSep = Lang.instance(NameType.SEPHARDIC);
        assertNotSame("GENERIC and ASHKENAZI instances must be distinct", langGen1, langAsh);
        assertNotSame("ASHKENAZI and SEPHARDIC instances must be distinct", langAsh, langSep);
    }

    @Test(timeout = 4000)
    public void testInstanceNullNameTypeReturnsNull() {
        final Lang langNull = Lang.instance(null);
        assertNull("Lang.instance(null) should return null", langNull);
    }

    @Test(timeout = 4000)
    public void testCustomLoadFromResourceProducesWorkingInstance() {
        // Load explicitly from valid default resource
        final String resourcePath = "org/apache/commons/codec/language/bm/lang.txt";
        final Lang customLang = Lang.loadFromResource(resourcePath, Languages.getInstance(NameType.GENERIC));

        assertNotNull("Loaded custom Lang instance must not be null", customLang);
        assertEquals("english", customLang.guessLanguage("smith"));
        assertEquals("french", customLang.guessLanguage("dupont"));
    }
}