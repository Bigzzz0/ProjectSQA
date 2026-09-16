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

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.codec.language.DoubleMetaphone
 *
 * Branch & Path Coverage Matrix:
 * 1. Clean Input:
 *    - null input -> null
 *    - empty or blank string ("   ") -> null
 *    - case folding & trim -> uppercase english locale
 * 2. Silent Starts:
 *    - "GN", "KN", "PN", "WR", "PS" -> index starts at 1
 *    - normal start -> index starts at 0
 * 3. Slavo-Germanic:
 *    - contains 'W', 'K', 'CZ', or 'WITZ'
 * 4. Vowels (AEIOUY):
 *    - index 0 -> append 'A'
 *    - index > 0 -> ignored
 * 5. Consonants & Rules:
 *    - 'B' (single B, double BB)
 *    - '\u00C7' (C with Cedilla) -> append 'S'
 *    - 'C':
 *      - conditionC0: "CHIA", "ACH" after consonant ("BACHER", "MACHER")
 *      - "CAESAR" -> 'S'
 *      - "CH": "CHAE" (Michael), conditionCH0 ("CHEMISTRY", "CHORUS", "CHARAC", "CHORE"),
 *              conditionCH1 ("SCH", "ORCHES", "ARCHIT", "ORCHID", "CH" before T/S/consonants),
 *              "MC" prefix, index 0 default 'X'
 *      - "CZ" vs "WICZ"
 *      - "CIA" -> 'X'
 *      - "CC": "McClelland", "ACCIDENT", "BACCI", "BACCHUS"
 *      - "CK", "CG", "CQ" -> 'K'
 *      - "CI", "CE", "CY" -> "CIO"/"CIE"/"CIA" vs standard 'S'
 *      - "C " / " C" / " Q" / " G" / default 'K'
 *    - 'D':
 *      - "DG" + I/E/Y ("EDGE") vs "DG" + other ("EDGAR")
 *      - "DT", "DD" -> 'T'
 *      - default -> 'T'
 *    - 'F' (single F, double FF)
 *    - 'G':
 *      - "GH": index 0 ("GHI" -> 'J', other -> 'K'), Parker's rule ("HUGH"),
 *              "LAUGH"/"TOUGH" -> 'F', index > 0 -> 'K'
 *      - "GN": Italian/French vowel+GN ("CAGN") vs Germanic vs -EY
 *      - "GLI" -> 'KL' / 'L'
 *      - index 0 + "GES", "GEP", "GEL", "GIE", "GY" -> 'K', 'J'
 *      - "GER", "GY" without danger/ranger/manger -> 'K', 'J'
 *      - Italian "AGGI", "OGGI", or G + E/I/Y:
 *        - "VAN ", "VON ", "SCH", or "GET" -> 'K'
 *        - "IER" (Angier) -> 'J' (DEFECT TARGET ZONE)
 *        - other -> 'J', 'K'
 *      - "GG" -> 'K'
 *    - 'H':
 *      - between vowels or initial before vowel -> 'H'
 *      - other -> silent
 *    - 'J':
 *      - Spanish "JOSE", "SAN "
 *      - index 0 -> 'J', 'A'
 *      - vowel + J + A/O -> 'J', 'H'
 *      - end of word -> 'J', ' '
 *      - "JJ" -> jump 2
 *    - 'L':
 *      - "LL": conditionL0 ("ILLO", "ILLA", "ALLE" Spanish endings)
 *      - single 'L'
 *    - 'M':
 *      - conditionM0 ("THUMB", "PLUMBER", "MM")
 *    - 'N', '\u00D1' (Ene)
 *    - 'P': "PH" -> 'F', "PB", "PP"
 *    - 'Q': "Q", "QQ"
 *    - 'R': ending "IER" French non-Slavo-Germanic, "RR"
 *    - 'S':
 *      - "ISL", "YSL" (island, isle)
 *      - "SUGAR" -> 'X', 'S'
 *      - "SH" + HEIM/HOEK/HOLM/HOLZ vs other 'X'
 *      - "SIO", "SIA", "SIAN" -> Slavo-Germanic vs Italian/Armenian
 *      - initial S + M/N/L/W, "SZ" -> 'S', 'X'
 *      - "SC": "SCH" + OO/ER/EN/UY/ED/EM vs other, "SC" + I/E/Y, "SC" default
 *      - French -ais / -ois ending -> alternate 'S'
 *    - 'T':
 *      - "TION", "TIA", "TCH" -> 'X'
 *      - "TH", "TTH" ("THOMAS", Germanic vs standard '0', 'T')
 *      - "TT", "TD"
 *    - 'V': 'V', "VV" -> 'F'
 *    - 'W':
 *      - "WR" -> 'R'
 *      - initial "W" + vowel -> 'A', 'F'; "WH" -> 'A'
 *      - vowel + W at end / "EWSKI" / "OWSKI" -> alternate 'F'
 *      - "WICZ", "WITZ" -> "TS", "FX"
 *    - 'X':
 *      - initial 'X' -> 'S'
 *      - French endings "-IAUX", "-EAUX", "-AUX", "-OUX"
 *      - "XC", "XX"
 *    - 'Z':
 *      - "ZH" -> 'J'
 *      - "ZO", "ZI", "ZA" or Slavo-Germanic -> 'S', 'TS'
 *      - "ZZ"
 * 6. Defect Target:
 *    - "Angier" Alternate encoding: contains(value, index + 1, 4, "IER")
 *      triggers bug where length=4 prevents matching 3-letter "IER".
 * 7. Object Lifecycle & Contracts:
 *    - encode(Object) with valid String and invalid types
 *    - isDoubleMetaphoneEqual(s1, s2) and isDoubleMetaphoneEqual(s1, s2, alt)
 *    - maxCodeLen accessors & DoubleMetaphoneResult boundary truncation
 */
public class DoubleMetaphoneGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & Standard Encodings
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicVowelAndSilentStarts() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Initial vowels append 'A'
        assertEquals("APL", dm.doubleMetaphone("Apple"));
        assertEquals("ATK", dm.doubleMetaphone("Attack"));

        // Silent starts (GN, KN, PN, WR, PS) skip first char
        assertEquals("NT", dm.doubleMetaphone("Gnat"));
        assertEquals("NT", dm.doubleMetaphone("Knight"));
        assertEquals("NM", dm.doubleMetaphone("Pneumonia"));
        assertEquals("RT", dm.doubleMetaphone("Write"));
        assertEquals("SM", dm.doubleMetaphone("Psalm"));
    }

    @Test(timeout = 4000)
    public void testBasicConsonantsSingleAndDouble() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // B and BB
        assertEquals("P", dm.doubleMetaphone("Bob"));
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("PP", dm.doubleMetaphone("Baby"));
        assertEquals("P", dm.doubleMetaphone("Abba"));

        // F and FF
        assertEquals("F", dm.doubleMetaphone("Fox"));
        assertEquals("F", dm.doubleMetaphone("Off"));

        // K and KK
        assertEquals("K", dm.doubleMetaphone("Kick"));
        assertEquals("K", dm.doubleMetaphone("Bakk"));

        // N, NN, and Ñ
        assertEquals("N", dm.doubleMetaphone("No"));
        assertEquals("N", dm.doubleMetaphone("Anna"));
        assertEquals("N", dm.doubleMetaphone("\u00D1and\u00FA"));

        // Q and QQ
        assertEquals("K", dm.doubleMetaphone("Quick"));
        assertEquals("K", dm.doubleMetaphone("Suqqu"));

        // V and VV
        assertEquals("F", dm.doubleMetaphone("Vivid"));
        assertEquals("F", dm.doubleMetaphone("Savvy"));
    }

    @Test(timeout = 4000)
    public void testLetterCVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Cedilla
        assertEquals("S", dm.doubleMetaphone("\u00C7a"));

        // Caesar
        assertEquals("SSR", dm.doubleMetaphone("Caesar"));

        // Condition C0: CHIA, BACHER, MACHER
        assertEquals("K", dm.doubleMetaphone("Chianti"));
        assertEquals("PKR", dm.doubleMetaphone("Bacher"));
        assertEquals("MKR", dm.doubleMetaphone("Macher"));

        // CZ and WICZ
        assertEquals("S", dm.doubleMetaphone("Czerny", false));
        assertEquals("X", dm.doubleMetaphone("Czerny", true));
        assertEquals("FTS", dm.doubleMetaphone("Wicz", false));

        // CIA
        assertEquals("FKX", dm.doubleMetaphone("Focaccia"));

        // CC: McClelland, Accident, Bacci, Bacchus
        assertEquals("MKLNT", dm.doubleMetaphone("McClelland"));
        assertEquals("AKSTNT", dm.doubleMetaphone("Accident"));
        assertEquals("PX", dm.doubleMetaphone("Bacci"));
        assertEquals("PKXS", dm.doubleMetaphone("Bacchus"));

        // CK, CG, CQ
        assertEquals("PK", dm.doubleMetaphone("Pack"));
        assertEquals("AK", dm.doubleMetaphone("Acquire"));

        // CI, CE, CY
        assertEquals("S", dm.doubleMetaphone("City"));
        assertEquals("S", dm.doubleMetaphone("Center"));
        assertEquals("S", dm.doubleMetaphone("Cycle"));
        assertEquals("SX", dm.doubleMetaphone("Cia"));
        assertEquals("SX", dm.doubleMetaphone("Cio"));
        assertEquals("SX", dm.doubleMetaphone("Cie"));

        // C + space C / Q / G
        assertEquals("MKFR", dm.doubleMetaphone("Mac Caffrey"));
        assertEquals("MKRK", dm.doubleMetaphone("Mac Gregor"));
    }

    @Test(timeout = 4000)
    public void testLetterCHVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Michael (CHAE)
        assertEquals("MKL", dm.doubleMetaphone("Michael", false));
        assertEquals("MXL", dm.doubleMetaphone("Michael", true));

        // conditionCH0: Greek roots
        assertEquals("KMST", dm.doubleMetaphone("Chemistry"));
        assertEquals("KRS", dm.doubleMetaphone("Chorus"));
        assertEquals("KRKT", dm.doubleMetaphone("Character"));
        assertEquals("KRSM", dm.doubleMetaphone("Charisma"));

        // conditionCH0 Chore exception
        assertEquals("XR", dm.doubleMetaphone("Chore"));

        // conditionCH1: Germanic, Greek or kh sound
        assertEquals("FRKT", dm.doubleMetaphone("Architect"));
        assertEquals("ARKTS", dm.doubleMetaphone("Orchestra"));
        assertEquals("ARKT", dm.doubleMetaphone("Orchid"));
        assertEquals("XT", dm.doubleMetaphone("Yacht"));

        // Mc prefix with CH
        assertEquals("MKX", dm.doubleMetaphone("McHugh"));

        // Initial CH default
        assertEquals("XMPN", dm.doubleMetaphone("Champion"));
    }

    @Test(timeout = 4000)
    public void testLetterDVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Edge vs Edgar
        assertEquals("AJ", dm.doubleMetaphone("Edge"));
        assertEquals("ATK", dm.doubleMetaphone("Edgar"));

        // DT, DD
        assertEquals("STT", dm.doubleMetaphone("Schmidt"));
        assertEquals("AT", dm.doubleMetaphone("Add"));
    }

    @Test(timeout = 4000)
    public void testLetterGAndGHVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // GH rules:
        assertEquals("K", dm.doubleMetaphone("Ghost"));
        assertEquals("J", dm.doubleMetaphone("Ghislane"));
        assertEquals("H", dm.doubleMetaphone("Hugh"));
        assertEquals("LF", dm.doubleMetaphone("Laugh"));
        assertEquals("KF", dm.doubleMetaphone("Cough"));
        assertEquals("RF", dm.doubleMetaphone("Rough"));
        assertEquals("TF", dm.doubleMetaphone("Tough"));

        // GN rules:
        assertEquals("KN", dm.doubleMetaphone("Agnes", false));
        assertEquals("N", dm.doubleMetaphone("Agnes", true));

        // GLI rules:
        assertEquals("KL", dm.doubleMetaphone("Gigli", false));
        assertEquals("L", dm.doubleMetaphone("Gigli", true));

        // Initial G followed by E/I/Y patterns
        assertEquals("K", dm.doubleMetaphone("Geste", false));
        assertEquals("J", dm.doubleMetaphone("Geste", true));

        // -ger-, -gy-
        assertEquals("KR", dm.doubleMetaphone("Gery", false));
        assertEquals("JR", dm.doubleMetaphone("Gery", true));
        assertEquals("TNJR", dm.doubleMetaphone("Danger"));

        // Italian AGGI, OGGI
        assertEquals("PJ", dm.doubleMetaphone("Biaggi", false));
        assertEquals("PK", dm.doubleMetaphone("Biaggi", true));
    }

    @Test(timeout = 4000)
    public void testLetterHVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // H between vowels or initial before vowel
        assertEquals("H", dm.doubleMetaphone("Hay"));
        assertEquals("AH", dm.doubleMetaphone("Aha"));

        // H silent
        assertEquals("T", dm.doubleMetaphone("Night"));
        assertEquals("A", dm.doubleMetaphone("Hour"));
    }

    @Test(timeout = 4000)
    public void testLetterJVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Spanish JOSE, SAN
        assertEquals("HS", dm.doubleMetaphone("Jose"));
        assertEquals("SN", dm.doubleMetaphone("San Jose"));

        // Initial J
        assertEquals("J", dm.doubleMetaphone("John", false));
        assertEquals("A", dm.doubleMetaphone("John", true));

        // Vowel + J + A/O
        assertEquals("AJ", dm.doubleMetaphone("Baja", false));
        assertEquals("AH", dm.doubleMetaphone("Baja", true));

        // J at end of word
        assertEquals("SMJ", dm.doubleMetaphone("Smaj", false));
        assertEquals("SM ", dm.doubleMetaphone("Smaj", true));

        // Double JJ
        assertEquals("HJ", dm.doubleMetaphone("Hajji"));
    }

    @Test(timeout = 4000)
    public void testLetterLVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Condition L0: Spanish -illo, -illa, -alle
        assertEquals("KPRL", dm.doubleMetaphone("Cabrillo", false));
        assertEquals("KPR", dm.doubleMetaphone("Cabrillo", true));
        assertEquals("TRT", dm.doubleMetaphone("Tortilla", true));
        assertEquals("AL", dm.doubleMetaphone("Calle", false));
        assertEquals("A", dm.doubleMetaphone("Calle", true));

        // Standard LL
        assertEquals("PL", dm.doubleMetaphone("Bell"));
    }

    @Test(timeout = 4000)
    public void testLetterMVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Condition M0: UMB at end or UMBER
        assertEquals("TM", dm.doubleMetaphone("Thumb"));
        assertEquals("PLMR", dm.doubleMetaphone("Plumber"));
        assertEquals("MM", dm.doubleMetaphone("Mamma"));
    }

    @Test(timeout = 4000)
    public void testLetterPAndPH() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals("F", dm.doubleMetaphone("Phone"));
        assertEquals("P", dm.doubleMetaphone("Puppy"));
        assertEquals("KPF", dm.doubleMetaphone("Campbell"));
    }

    @Test(timeout = 4000)
    public void testLetterRVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // French ending -ier
        assertEquals("R", dm.doubleMetaphone("Rogier", false));
        assertEquals("", dm.doubleMetaphone("Rogier", true));

        // Standard R and RR
        assertEquals("R", dm.doubleMetaphone("Red"));
        assertEquals("KR", dm.doubleMetaphone("Carrot"));
    }

    @Test(timeout = 4000)
    public void testLetterSVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Special cases island, isle
        assertEquals("ALNT", dm.doubleMetaphone("Island"));
        assertEquals("AL", dm.doubleMetaphone("Isle"));
        assertEquals("KRL", dm.doubleMetaphone("Carlisle"));

        // Sugar
        assertEquals("XKR", dm.doubleMetaphone("Sugar", false));
        assertEquals("SKR", dm.doubleMetaphone("Sugar", true));

        // SH with germanic endings vs default
        assertEquals("SM", dm.doubleMetaphone("Sheim"));
        assertEquals("SK", dm.doubleMetaphone("Shoek"));
        assertEquals("X", dm.doubleMetaphone("Shoe"));

        // SIO, SIA, SIAN
        assertEquals("SS", dm.doubleMetaphone("Asia", false));
        assertEquals("SX", dm.doubleMetaphone("Asia", true));

        // Initial S + M/N/L/W, or SZ
        assertEquals("SM", dm.doubleMetaphone("Smith", false));
        assertEquals("XM", dm.doubleMetaphone("Smith", true));
        assertEquals("SNTR", dm.doubleMetaphone("Snider", false));
        assertEquals("XNTR", dm.doubleMetaphone("Snider", true));

        // SC cases
        assertEquals("SK", dm.doubleMetaphone("School"));
        assertEquals("XRM", dm.doubleMetaphone("Schermerhorn", false));
        assertEquals("SKRM", dm.doubleMetaphone("Schermerhorn", true));
        assertEquals("S", dm.doubleMetaphone("Science"));
        assertEquals("SK", dm.doubleMetaphone("Scale"));

        // French endings -ais, -ois
        assertEquals("RSN", dm.doubleMetaphone("Resnais", false));
        assertEquals("RSNS", dm.doubleMetaphone("Resnais", true));
    }

    @Test(timeout = 4000)
    public void testLetterTVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // TION, TIA, TCH
        assertEquals("NX", dm.doubleMetaphone("Nation"));
        assertEquals("KX", dm.doubleMetaphone("Catch"));

        // TH, TTH
        assertEquals("TMS", dm.doubleMetaphone("Thomas"));
        assertEquals("0NK", dm.doubleMetaphone("Think", false));
        assertEquals("TNK", dm.doubleMetaphone("Think", true));

        // TT, TD
        assertEquals("BT", dm.doubleMetaphone("Bitter"));
    }

    @Test(timeout = 4000)
    public void testLetterWVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // WR
        assertEquals("RT", dm.doubleMetaphone("Wrong"));

        // Initial W + vowel
        assertEquals("ASRM", dm.doubleMetaphone("Wasserman", false));
        assertEquals("FSRM", dm.doubleMetaphone("Wasserman", true));
        assertEquals("AT", dm.doubleMetaphone("White"));

        // Polish endings: -owski, -ewski
        assertEquals("ARN", dm.doubleMetaphone("Arnow", false));
        assertEquals("ARNF", dm.doubleMetaphone("Arnow", true));
        assertEquals("FK", dm.doubleMetaphone("Filipowicz", false));
        assertEquals("FFX", dm.doubleMetaphone("Filipowicz", true));
    }

    @Test(timeout = 4000)
    public void testLetterXVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Initial X
        assertEquals("SFR", dm.doubleMetaphone("Xavier"));

        // French endings with silent X
        assertEquals("PR", dm.doubleMetaphone("Breaux"));

        // Standard X, XC, XX
        assertEquals("FKS", dm.doubleMetaphone("Fox"));
        assertEquals("AKSS", dm.doubleMetaphone("Axcell"));
    }

    @Test(timeout = 4000)
    public void testLetterZVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Chinese ZH
        assertEquals("JNK", dm.doubleMetaphone("Zhang"));

        // ZO, ZI, ZA
        assertEquals("S", dm.doubleMetaphone("Zoo", false));
        assertEquals("TS", dm.doubleMetaphone("Zoo", true));

        // Slavic Z
        assertEquals("TS", dm.doubleMetaphone("Taglcz", false));

        // Standard Z and ZZ
        assertEquals("S", dm.doubleMetaphone("Zero"));
        assertEquals("PS", dm.doubleMetaphone("Pizza"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullEmptyAndWhitespaceInputs() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertNull(dm.doubleMetaphone(null));
        assertNull(dm.doubleMetaphone(null, true));
        assertNull(dm.doubleMetaphone(null, false));

        assertNull(dm.doubleMetaphone(""));
        assertNull(dm.doubleMetaphone(""));
        assertNull(dm.doubleMetaphone("   "));
        assertNull(dm.doubleMetaphone("\t\n\r"));
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInputs() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals("A", dm.doubleMetaphone("A"));
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("T", dm.doubleMetaphone("D"));
        assertEquals("F", dm.doubleMetaphone("F"));
        assertEquals("K", dm.doubleMetaphone("G"));
        assertEquals("", dm.doubleMetaphone("H"));
        assertEquals("J", dm.doubleMetaphone("J", false));
        assertEquals("A", dm.doubleMetaphone("J", true));
        assertEquals("K", dm.doubleMetaphone("K"));
        assertEquals("L", dm.doubleMetaphone("L"));
        assertEquals("M", dm.doubleMetaphone("M"));
        assertEquals("N", dm.doubleMetaphone("N"));
        assertEquals("P", dm.doubleMetaphone("P"));
        assertEquals("K", dm.doubleMetaphone("Q"));
        assertEquals("R", dm.doubleMetaphone("R"));
        assertEquals("S", dm.doubleMetaphone("S"));
        assertEquals("T", dm.doubleMetaphone("T"));
        assertEquals("F", dm.doubleMetaphone("V"));
        assertEquals("", dm.doubleMetaphone("W"));
        assertEquals("S", dm.doubleMetaphone("X"));
        assertEquals("S", dm.doubleMetaphone("Z"));
    }

    @Test(timeout = 4000)
    public void testNonAlphabeticCharactersIgnored() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals("T", dm.doubleMetaphone("123T456"));
        assertEquals("P", dm.doubleMetaphone("@#$B%^&"));
        assertEquals("STSN", dm.doubleMetaphone("ST. JOHN"));
    }

    @Test(timeout = 4000)
    public void testMaxCodeLengthVariations() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals(4, dm.getMaxCodeLen());

        dm.setMaxCodeLen(2);
        assertEquals(2, dm.getMaxCodeLen());
        assertEquals("AL", dm.doubleMetaphone("Alexander"));

        dm.setMaxCodeLen(8);
        assertEquals(8, dm.getMaxCodeLen());
        assertEquals("ALKSNTR", dm.doubleMetaphone("Alexander"));

        dm.setMaxCodeLen(0);
        assertEquals(0, dm.getMaxCodeLen());
        assertEquals("", dm.doubleMetaphone("Alexander"));
    }

    @Test(timeout = 4000)
    public void testCharAtBoundary() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals(Character.MIN_VALUE, dm.charAt("A", -1));
        assertEquals(Character.MIN_VALUE, dm.charAt("A", 1));
        assertEquals(Character.MIN_VALUE, dm.charAt("A", 100));
        assertEquals('A', dm.charAt("A", 0));
    }

    @Test(timeout = 4000)
    public void testContainsHelperBoundaries() {
        // Negative start index
        assertFalse(DoubleMetaphone.contains("TEST", -1, 2, new String[] { "TE" }));
        // Start + length exceeding string length
        assertFalse(DoubleMetaphone.contains("TEST", 3, 2, new String[] { "ST" }));
        // Exact boundary matching
        assertTrue(DoubleMetaphone.contains("TEST", 2, 2, new String[] { "ST" }));
        // Empty criteria array
        assertFalse(DoubleMetaphone.contains("TEST", 0, 2, new String[] {}));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Ground Truth Defect:
     * org.apache.commons.codec.language.DoubleMetaphone2Test::testDoubleMetaphoneAlternate
     * ComparisonFailure: Test [19]=Angier expected:<AN[J]R> but was:<AN[K]R>
     *
     * In handleG():
     *   contains(value, index + 1, 4, "IER")
     * Because "IER" has length 3, passing length 4 requires matching a 4-character substring,
     * which never equals "IER", and index + 1 + 4 exceeds value length for "ANGIER" (length 6),
     * causing it to fall through to result.append('J', 'K') instead of result.append('J').
     *
     * This test explicitly asserts the expected correct behavior: "ANJR".
     */
    @Test(timeout = 4000)
    public void testAngierAlternateEncodingDefect() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("ANJR", dm.doubleMetaphone("Angier", true));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeWithValidStringObject() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        Object result = dm.encode((Object) "Smith");
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("SM0", result);
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeWithInvalidObjectTypeThrowsException() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        dm.encode(new Integer(42));
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeWithNullObjectThrowsException() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        dm.encode((Object) null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringEncoderContract() {
        DoubleMetaphone dm = new DoubleMetaphone();

        assertEquals("SM0", dm.encode("Smith"));
        assertNull(dm.encode((String) null));
    }

    @Test(timeout = 4000)
    public void testIsDoubleMetaphoneEqual() {
        DoubleMetaphone dm = new DoubleMetaphone();

        // Primary encoding equality
        assertTrue(dm.isDoubleMetaphoneEqual("Smith", "Schmidt"));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", "Jones"));

        // Alternate encoding equality
        assertTrue(dm.isDoubleMetaphoneEqual("Smith", "Schmidt", true));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", "Jones", true));

        // Default equality method delegates to primary
        assertEquals(dm.isDoubleMetaphoneEqual("Wasserman", "Vasserman", false),
                     dm.isDoubleMetaphoneEqual("Wasserman", "Vasserman"));
    }

    @Test(timeout = 4000)
    public void testDoubleMetaphoneResultInnerClassDirectly() {
        DoubleMetaphone dm = new DoubleMetaphone();
        DoubleMetaphone.DoubleMetaphoneResult res = dm.new DoubleMetaphoneResult(4);

        assertEquals("", res.getPrimary());
        assertEquals("", res.getAlternate());
        assertFalse(res.isComplete());

        res.append('A');
        assertEquals("A", res.getPrimary());
        assertEquals("A", res.getAlternate());

        res.append('B', 'C');
        assertEquals("AB", res.getPrimary());
        assertEquals("AC", res.getAlternate());

        res.append("DE");
        assertEquals("ABDE", res.getPrimary());
        assertEquals("ACDE", res.getAlternate());
        assertTrue(res.isComplete());

        // Appending past max length has no effect
        res.append('F');
        res.append("GH");
        res.appendPrimary('X');
        res.appendAlternate('Y');
        res.appendPrimary("ZZ");
        res.appendAlternate("WW");
        assertEquals("ABDE", res.getPrimary());
        assertEquals("ACDE", res.getAlternate());

        // Test appending strings larger than remaining capacity
        DoubleMetaphone.DoubleMetaphoneResult res2 = dm.new DoubleMetaphoneResult(3);
        res2.append("LONGSTRING", "LONGERSTRING");
        assertEquals("LON", res2.getPrimary());
        assertEquals("LON", res2.getAlternate());
        assertTrue(res2.isComplete());
    }
}