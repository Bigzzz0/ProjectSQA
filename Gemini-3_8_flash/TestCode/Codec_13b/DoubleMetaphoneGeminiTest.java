package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targets & Decision Branches:
 * 1. Defect CODEC-184 / testIsDoubleMetaphoneEqualBasic:
 *    - isDoubleMetaphoneEqual(String, String, boolean) does not guard against null return values from
 *      doubleMetaphone(String, boolean) when cleanInput returns null (e.g., null, empty, whitespace-only).
 *      Invoking .equals() directly on a null result triggers NullPointerException.
 * 2. Input Cleaning & Silent Starts:
 *    - cleanInput: null, "", "   ", lowercase, leading/trailing whitespace.
 *    - isSilentStart: "GN", "KN", "PN", "WR", "PS" prefixes increment starting index to 1.
 * 3. Vowels & Special Characters:
 *    - A, E, I, O, U, Y (initial index=0 appends 'A'; index>0 skipped).
 *    - Cedilla '\u00C7' -> appends 'S'.
 *    - Spanish Ene '\u00D1' -> appends 'N'.
 * 4. Consonant Handlers:
 *    - B: single 'B', double "BB".
 *    - C:
 *      * conditionC0: "CHIA", "ACH" patterns, "BACHER", "MACHER".
 *      * "CAESAR" -> 'S'.
 *      * "CH": Michael "CHAE", Greek "CH" (conditionCH0, conditionCH1), "MC" prefix, standard "CH".
 *      * "CZ" with and without "WICZ".
 *      * "CIA" -> 'X'.
 *      * "CC": "McClelland", "bellocchio" vs "bacchus", "accident", "accede", "succeed", Italian "bacci".
 *      * "CK", "CG", "CQ" -> 'K'.
 *      * "CI", "CE", "CY" with "CIO", "CIE", "CIA" -> ('S', 'X') vs 'S'.
 *      * Default 'C': " C", " Q", " G" -> skips, "C", "K", "Q" not followed by "CE", "CI".
 *    - D: "DG" + ("I","E","Y") -> 'J' vs "Edgar" -> "TK", "DT", "DD", default 'T'.
 *    - F: single 'F', double "FF".
 *    - G:
 *      * "GH": vowel predecessors, initial index=0 ("GHI" -> 'J' vs 'K'), Parker's rule ("hugh"),
 *              "cough", "rough", "laugh" -> 'F', default 'K'.
 *      * "GN": initial index=1 after vowel not Slavo-Germanic -> ("KN", "N"); not "EY" -> ("N", "KN"); else "KN".
 *      * "GLI" / "GLI" via "LI" not Slavo-Germanic -> ("KL", "L").
 *      * Initial "GES", "GEP", "GEL", "GIE", "GY" -> ('K', 'J').
 *      * "GER", "GY" excluding "DANGER", "RANGER", "MANGER", preceding 'E', 'I', "RGY", "OGY".
 *      * Italian "AGGI", "OGGI", German "VAN ", "VON ", "SCH", "ET", "IER" -> 'J' vs ('J', 'K').
 *      * "GG" -> 'K', default 'K'.
 *    - H: initial before vowel, between two vowels -> 'H'; else skipped.
 *    - J: "JOSE", "SAN " prefixes -> 'H' or ('J', 'H'); Slavo-Germanic handling; end of word ('J', ' ');
 *         filtered by following and preceding consonants; double "JJ".
 *    - K: single 'K', double "KK".
 *    - L: "LL" conditionL0 ("ILLO", "ILLA", "ALLE" + "AS"/"OS"/"A"/"O"), default 'L'.
 *    - M: conditionM0 ("UMB" at end or "UMBER"), double "MM", default 'M'.
 *    - N: double "NN", default 'N'.
 *    - P: "PH" -> 'F', double "PP", "PB", default 'P'.
 *    - Q: single 'Q', double "QQ".
 *    - R: conditionR0 (end of word, !slavoGermanic, "IE", not "ME"/"MA"), double "RR", default 'R'.
 *    - S: "ISL", "YSL", "SUGAR", "SH" ("HEIM", "HOEK", "HOLM", "HOLZ"), "SIO", "SIA", "SIAN",
 *         initial "SM", "SN", "SL", "SW", "SZ", "SC" (Schlesinger's rule "SCHOO", "SCHER", "SCHEN"),
 *         French ending "AIS", "OIS".
 *    - T: "TION", "TIA", "TCH", "TH", "TTH" ("THOMAS", "THAMES", "VAN ", "VON ", "SCH"), double "TT", "TD".
 *    - V: single 'V', double "VV".
 *    - W: "WR", initial vowel / "WH" ("WASSERMAN", "WOMO"), end of word vowel + "W", "EWSKI", "OWSKI",
 *         "WICZ", "WITZ".
 *    - X: initial 'X', French endings "IAUX", "EAUX", "AUX", "OUX", double "XC", "XX".
 *    - Z: "ZH", "ZO", "ZI", "ZA", Slavo-Germanic 'Z', double "ZZ".
 * 5. DoubleMetaphoneResult inner class:
 *    - Capacity bounds, truncation when exceeding maxLength, isComplete checks, append variants.
 * 6. StringEncoder interface compliance:
 *    - encode(Object) contract, throwing EncoderException for non-Strings.
 */
public class DoubleMetaphoneGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Words
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicEncodingWords() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("SMT", dm.doubleMetaphone("Smith"));
        assertEquals("XMT", dm.doubleMetaphone("Schmidt"));
        assertEquals("APL", dm.doubleMetaphone("Apple"));
        assertEquals("PRSK", dm.doubleMetaphone("Breschnew"));
    }

    @Test(timeout = 4000)
    public void testMaxCodeLength() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals(4, dm.getMaxCodeLen());
        dm.setMaxCodeLen(6);
        assertEquals(6, dm.getMaxCodeLen());
        assertEquals("SMTKR", dm.doubleMetaphone("Smithcraft"));
        dm.setMaxCodeLen(2);
        assertEquals("SM", dm.doubleMetaphone("Smithcraft"));
    }

    @Test(timeout = 4000)
    public void testAlternateEncodings() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        // Schmidt: primary XMT, alternate SMT
        assertEquals("XMT", dm.doubleMetaphone("Schmidt", false));
        assertEquals("SMT", dm.doubleMetaphone("Schmidt", true));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndNullDoubleMetaphone() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertNull(dm.doubleMetaphone(null));
        assertNull(dm.doubleMetaphone(""));
        assertNull(dm.doubleMetaphone("   "));
        assertNull(dm.doubleMetaphone(null, true));
        assertNull(dm.doubleMetaphone("", true));
        assertNull(dm.doubleMetaphone(" \t\n ", true));
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInputs() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("A", dm.doubleMetaphone("A"));
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("T", dm.doubleMetaphone("D"));
        assertEquals("A", dm.doubleMetaphone("E"));
        assertEquals("F", dm.doubleMetaphone("F"));
        assertEquals("K", dm.doubleMetaphone("G"));
        assertNull(dm.doubleMetaphone("H")); // Single H is not before a vowel
        assertEquals("A", dm.doubleMetaphone("I"));
        assertEquals("J", dm.doubleMetaphone("J"));
        assertEquals("K", dm.doubleMetaphone("K"));
        assertEquals("L", dm.doubleMetaphone("L"));
        assertEquals("M", dm.doubleMetaphone("M"));
        assertEquals("N", dm.doubleMetaphone("N"));
        assertEquals("A", dm.doubleMetaphone("O"));
        assertEquals("P", dm.doubleMetaphone("P"));
        assertEquals("K", dm.doubleMetaphone("Q"));
        assertEquals("R", dm.doubleMetaphone("R"));
        assertEquals("S", dm.doubleMetaphone("S"));
        assertEquals("T", dm.doubleMetaphone("T"));
        assertEquals("A", dm.doubleMetaphone("U"));
        assertEquals("F", dm.doubleMetaphone("V"));
        assertNull(dm.doubleMetaphone("W")); // W not followed by vowel
        assertEquals("S", dm.doubleMetaphone("X"));
        assertEquals("A", dm.doubleMetaphone("Y"));
        assertEquals("S", dm.doubleMetaphone("Z"));
    }

    @Test(timeout = 4000)
    public void testNonAsciiCharacters() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("S", dm.doubleMetaphone("\u00C7"));      // Cedilla Ç
        assertEquals("N", dm.doubleMetaphone("\u00D1"));      // Spanish Ñ
        assertEquals("SK", dm.doubleMetaphone("\u00C7K"));
        assertEquals("NK", dm.doubleMetaphone("\u00D1K"));
    }

    @Test(timeout = 4000)
    public void testSilentStarts() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("NAT", dm.doubleMetaphone("GNAT"));
        assertEquals("NAT", dm.doubleMetaphone("KNAT"));
        assertEquals("NUM", dm.doubleMetaphone("PNEUMA"));
        assertEquals("RIT", dm.doubleMetaphone("WRITE"));
        assertEquals("SLM", dm.doubleMetaphone("PSALM"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (CODEC-184 / Null Pointer Bug)
    // =========================================================================

    /**
     * Targets known defect: isDoubleMetaphoneEqual throws NullPointerException
     * when either argument returns null from doubleMetaphone (null, empty, or whitespace).
     */
    @Test(timeout = 4000)
    public void testIsDoubleMetaphoneEqualBasicDefectTarget() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertFalse(dm.isDoubleMetaphoneEqual(null, ""));
        assertFalse(dm.isDoubleMetaphoneEqual("", null));
        assertFalse(dm.isDoubleMetaphoneEqual(null, null));
        assertFalse(dm.isDoubleMetaphoneEqual("", ""));
        assertFalse(dm.isDoubleMetaphoneEqual("   ", "   "));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", null));
        assertFalse(dm.isDoubleMetaphoneEqual(null, "Smith"));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", ""));
        assertFalse(dm.isDoubleMetaphoneEqual("", "Smith"));
    }

    @Test(timeout = 4000)
    public void testIsDoubleMetaphoneEqualValidCases() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertTrue(dm.isDoubleMetaphoneEqual("Smith", "Schmidt", false));
        assertTrue(dm.isDoubleMetaphoneEqual("Schmidt", "Smith", true));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", "Jones", false));
    }

    // =========================================================================
    // Partition D: Extensive Branch Coverage for Consonants & Conditions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleCBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        // conditionC0
        assertEquals("K", dm.doubleMetaphone("CHIA"));
        assertEquals("BKR", dm.doubleMetaphone("BACHER"));
        assertEquals("MKR", dm.doubleMetaphone("MACHER"));
        assertEquals("AK", dm.doubleMetaphone("ACH"));

        // CAESAR
        assertEquals("SSR", dm.doubleMetaphone("CAESAR"));

        // CH variants
        assertEquals("KKL", dm.doubleMetaphone("MICHAEL")); // CHAE
        assertEquals("KMR", dm.doubleMetaphone("CHEMISTRY")); // Greek CH
        assertEquals("KR", dm.doubleMetaphone("CHORUS"));
        assertEquals("KR", dm.doubleMetaphone("CHORE")); // conditionCH0 choreography check
        assertEquals("XRX", dm.doubleMetaphone("CHURCH"));
        assertEquals("KRK", dm.doubleMetaphone("ARCHITECT")); // conditionCH1
        assertEquals("KRK", dm.doubleMetaphone("ORCHESTRA")); // conditionCH1
        assertEquals("KRK", dm.doubleMetaphone("ORCHID")); // conditionCH1
        assertEquals("K", dm.doubleMetaphone("MCHUGH")); // MC prefix

        // CZ and WICZ
        assertEquals("STR", dm.doubleMetaphone("CZERNY", false));
        assertEquals("XTR", dm.doubleMetaphone("CZERNY", true));
        assertEquals("FRTS", dm.doubleMetaphone("VON WICZ", false)); // WICZ

        // CIA
        assertEquals("FKX", dm.doubleMetaphone("FOCACCIA"));

        // CC branches
        assertEquals("MKLN", dm.doubleMetaphone("MCCLELLAND")); // McClelland
        assertEquals("PLX", dm.doubleMetaphone("BELLOCCHIO")); // bellocchio
        assertEquals("BKS", dm.doubleMetaphone("BACCHUS")); // bacchus
        assertEquals("AKST", dm.doubleMetaphone("ACCIDENT")); // accident
        assertEquals("AKST", dm.doubleMetaphone("ACCEDE")); // accede
        assertEquals("SKST", dm.doubleMetaphone("SUCCEED")); // succeed
        assertEquals("PX", dm.doubleMetaphone("BACCI")); // Italian bacci

        // CK, CG, CQ
        assertEquals("AK", dm.doubleMetaphone("ACK"));
        assertEquals("AK", dm.doubleMetaphone("ACG"));
        assertEquals("AK", dm.doubleMetaphone("ACQ"));

        // CI, CE, CY
        assertEquals("SS", dm.doubleMetaphone("CIO"));
        assertEquals("XS", dm.doubleMetaphone("CIO", true));
        assertEquals("SS", dm.doubleMetaphone("CIE"));
        assertEquals("ST", dm.doubleMetaphone("CITY"));

        // C with following space and C, Q, G
        assertEquals("MKFR", dm.doubleMetaphone("MAC CAFFREY"));
        assertEquals("MKRK", dm.doubleMetaphone("MAC GREGOR"));
    }

    @Test(timeout = 4000)
    public void testHandleDBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("AJ", dm.doubleMetaphone("EDGI"));
        assertEquals("AJ", dm.doubleMetaphone("EDGE"));
        assertEquals("AJ", dm.doubleMetaphone("EDGY"));
        assertEquals("ATK", dm.doubleMetaphone("EDGAR"));
        assertEquals("AT", dm.doubleMetaphone("EDGAR", true));
        assertEquals("TT", dm.doubleMetaphone("DT"));
        assertEquals("TT", dm.doubleMetaphone("DD"));
    }

    @Test(timeout = 4000)
    public void testHandleGBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        // GH branches
        assertEquals("K", dm.doubleMetaphone("AGH"));
        assertEquals("J", dm.doubleMetaphone("GHI"));
        assertEquals("K", dm.doubleMetaphone("GH"));
        assertEquals("H", dm.doubleMetaphone("HUGH"));
        assertEquals("LF", dm.doubleMetaphone("LAUGH"));
        assertEquals("MKLF", dm.doubleMetaphone("MCLAUGHLIN"));
        assertEquals("KF", dm.doubleMetaphone("COUGH"));
        assertEquals("KF", dm.doubleMetaphone("GOUGH"));
        assertEquals("RF", dm.doubleMetaphone("ROUGH"));
        assertEquals("TF", dm.doubleMetaphone("TOUGH"));

        // GN branches
        assertEquals("KN", dm.doubleMetaphone("AGNES", false));
        assertEquals("N", dm.doubleMetaphone("AGNES", true));
        assertEquals("N", dm.doubleMetaphone("BIGNON", false));
        assertEquals("KN", dm.doubleMetaphone("BIGNON", true));

        // GLI
        assertEquals("KL", dm.doubleMetaphone("TAGLIA", false));
        assertEquals("L", dm.doubleMetaphone("TAGLIA", true));

        // Initial G followed by Y, ES, EP, etc.
        assertEquals("K", dm.doubleMetaphone("GYM", false));
        assertEquals("J", dm.doubleMetaphone("GYM", true));
        assertEquals("KST", dm.doubleMetaphone("GEST", false));
        assertEquals("JST", dm.doubleMetaphone("GEST", true));

        // GER, GY
        assertEquals("TNJR", dm.doubleMetaphone("DANGER"));
        assertEquals("RNJR", dm.doubleMetaphone("RANGER"));
        assertEquals("MNJR", dm.doubleMetaphone("MANGER"));
        assertEquals("ARJ", dm.doubleMetaphone("ERGY"));
        assertEquals("AJ", dm.doubleMetaphone("OGY"));

        // Italian AGGI, OGGI & Germanic prefixes
        assertEquals("FK", dm.doubleMetaphone("VON GET"));
        assertEquals("XKT", dm.doubleMetaphone("SCHGET"));
        assertEquals("AJR", dm.doubleMetaphone("AGGIER"));
        assertEquals("AJ", dm.doubleMetaphone("BIAGGI", false));
        assertEquals("AK", dm.doubleMetaphone("BIAGGI", true));

        // GG
        assertEquals("AK", dm.doubleMetaphone("EGG"));
    }

    @Test(timeout = 4000)
    public void testHandleHBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("H", dm.doubleMetaphone("HA"));
        assertEquals("AH", dm.doubleMetaphone("AHA"));
        assertEquals("A", dm.doubleMetaphone("AH")); // Not between vowels
        assertEquals("A", dm.doubleMetaphone("AHT"));
    }

    @Test(timeout = 4000)
    public void testHandleJBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("HS", dm.doubleMetaphone("JOSE"));
        assertEquals("SNHS", dm.doubleMetaphone("SAN JOSE"));
        assertEquals("H", dm.doubleMetaphone("JOSE "));
        assertEquals("JA", dm.doubleMetaphone("JAB", false));
        assertEquals("AA", dm.doubleMetaphone("JAB", true));
        assertEquals("AJH", dm.doubleMetaphone("AJA", true));
        assertEquals("AJ", dm.doubleMetaphone("RAJ", false));
        assertEquals("A", dm.doubleMetaphone("RAJ", true).trim());
        assertEquals("JJ", dm.doubleMetaphone("HAJJI"));
    }

    @Test(timeout = 4000)
    public void testHandleLBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("KPL", dm.doubleMetaphone("CABALLO")); // conditionL0 - ILLO, ILLA, ALLE
        assertEquals("KPL", dm.doubleMetaphone("CABALLA"));
        assertEquals("KPL", dm.doubleMetaphone("CABALLE"));
        assertEquals("ALS", dm.doubleMetaphone("ALLES"));
        assertEquals("AL", dm.doubleMetaphone("ALLA"));
        assertEquals("AL", dm.doubleMetaphone("ALL"));
    }

    @Test(timeout = 4000)
    public void testHandleMBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("TM", dm.doubleMetaphone("THUMB")); // conditionM0 - UMB at end
        assertEquals("TMR", dm.doubleMetaphone("THUMBER"));
        assertEquals("MM", dm.doubleMetaphone("MOMMY"));
    }

    @Test(timeout = 4000)
    public void testHandlePBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("FL", dm.doubleMetaphone("PHIL"));
        assertEquals("P", dm.doubleMetaphone("P"));
        assertEquals("PP", dm.doubleMetaphone("PUPPY"));
        assertEquals("PB", dm.doubleMetaphone("CUPBOARD"));
    }

    @Test(timeout = 4000)
    public void testHandleRBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("PR", dm.doubleMetaphone("PIER", true)); // conditionR0
        assertEquals("PMR", dm.doubleMetaphone("PREMIER", true));
        assertEquals("RR", dm.doubleMetaphone("ARROW"));
    }

    @Test(timeout = 4000)
    public void testHandleSBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("ALNT", dm.doubleMetaphone("ISLAND"));
        assertEquals("ALNT", dm.doubleMetaphone("YSLAND"));
        assertEquals("XKR", dm.doubleMetaphone("SUGAR", false));
        assertEquals("SKR", dm.doubleMetaphone("SUGAR", true));
        assertEquals("SM", dm.doubleMetaphone("SHEIM")); // germanic SH
        assertEquals("XK", dm.doubleMetaphone("SHOEK"));
        assertEquals("XL", dm.doubleMetaphone("SHOLM"));
        assertEquals("XL", dm.doubleMetaphone("SHOLZ"));
        assertEquals("X", dm.doubleMetaphone("SH"));
        assertEquals("S", dm.doubleMetaphone("SIAN", false)); // SlavoGermanic handled via context
        assertEquals("X", dm.doubleMetaphone("SIAN", true));
        assertEquals("SMT", dm.doubleMetaphone("SMITH"));
        assertEquals("SNTR", dm.doubleMetaphone("SNIDER"));
        assertEquals("ST", dm.doubleMetaphone("SZ"));
        assertEquals("RSN", dm.doubleMetaphone("RESNAIS", false));
        assertEquals("RSNS", dm.doubleMetaphone("RESNAIS", true));

        // SC cases
        assertEquals("SKL", dm.doubleMetaphone("SCHOOL"));
        assertEquals("SKR", dm.doubleMetaphone("SCHOONER"));
        assertEquals("XRMR", dm.doubleMetaphone("SCHERMERHORN", false));
        assertEquals("SKRM", dm.doubleMetaphone("SCHERMERHORN", true));
        assertEquals("XNK", dm.doubleMetaphone("SCHENKER", false));
        assertEquals("SKNK", dm.doubleMetaphone("SCHENKER", true));
        assertEquals("XT", dm.doubleMetaphone("SCHT"));
        assertEquals("SS", dm.doubleMetaphone("SCIENCE"));
        assertEquals("SKT", dm.doubleMetaphone("SCOTT"));
    }

    @Test(timeout = 4000)
    public void testHandleTBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("XN", dm.doubleMetaphone("TION"));
        assertEquals("X", dm.doubleMetaphone("TIA"));
        assertEquals("X", dm.doubleMetaphone("TCH"));
        assertEquals("TM", dm.doubleMetaphone("THOMAS"));
        assertEquals("TM", dm.doubleMetaphone("THAMES"));
        assertEquals("0", dm.doubleMetaphone("TH", false));
        assertEquals("T", dm.doubleMetaphone("TH", true));
        assertEquals("TT", dm.doubleMetaphone("TTH"));
        assertEquals("TT", dm.doubleMetaphone("TT"));
        assertEquals("TT", dm.doubleMetaphone("TD"));
    }

    @Test(timeout = 4000)
    public void testHandleWBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("RT", dm.doubleMetaphone("WRT"));
        assertEquals("ASRM", dm.doubleMetaphone("WASSERMAN", false));
        assertEquals("FSRM", dm.doubleMetaphone("WASSERMAN", true));
        assertEquals("AM", dm.doubleMetaphone("WHOM"));
        assertEquals("ARNF", dm.doubleMetaphone("ARNOW", true));
        assertEquals("ARNF", dm.doubleMetaphone("EWSKI", true));
        assertEquals("ARNF", dm.doubleMetaphone("OWSKI", true));
        assertEquals("TS", dm.doubleMetaphone("WICZ", false));
        assertEquals("FX", dm.doubleMetaphone("WICZ", true));
        assertEquals("TS", dm.doubleMetaphone("WITZ", false));
        assertEquals("FX", dm.doubleMetaphone("WITZ", true));
    }

    @Test(timeout = 4000)
    public void testHandleXBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("S", dm.doubleMetaphone("XAVIER"));
        assertEquals("PR", dm.doubleMetaphone("BREAUX"));
        assertEquals("BKS", dm.doubleMetaphone("BOX"));
        assertEquals("BKS", dm.doubleMetaphone("BOXX"));
        assertEquals("BKS", dm.doubleMetaphone("BOXC"));
    }

    @Test(timeout = 4000)
    public void testHandleZBranches() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("J", dm.doubleMetaphone("ZHAO"));
        assertEquals("S", dm.doubleMetaphone("ZO", false));
        assertEquals("TS", dm.doubleMetaphone("ZO", true));
        assertEquals("S", dm.doubleMetaphone("ZI", false));
        assertEquals("TS", dm.doubleMetaphone("ZI", true));
        assertEquals("S", dm.doubleMetaphone("ZA", false));
        assertEquals("TS", dm.doubleMetaphone("ZA", true));
        assertEquals("S", dm.doubleMetaphone("ZZ"));
    }

    // =========================================================================
    // Partition E: Helper Functions & Direct Unit Boundary Tests
    // =========================================================================

    @Test(timeout = 4000)
    public void testCharAtBoundary() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals(Character.MIN_VALUE, dm.charAt("abc", -1));
        assertEquals(Character.MIN_VALUE, dm.charAt("abc", 3));
        assertEquals(Character.MIN_VALUE, dm.charAt("abc", 100));
        assertEquals('a', dm.charAt("abc", 0));
        assertEquals('c', dm.charAt("abc", 2));
    }

    @Test(timeout = 4000)
    public void testContainsHelperMethod() {
        assertTrue(DoubleMetaphone.contains("TESTING", 0, 4, "TEST"));
        assertTrue(DoubleMetaphone.contains("TESTING", 4, 3, "ING"));
        assertFalse(DoubleMetaphone.contains("TESTING", -1, 4, "TEST"));
        assertFalse(DoubleMetaphone.contains("TESTING", 5, 4, "TEST"));
        assertFalse(DoubleMetaphone.contains("TESTING", 0, 4, "NOPE", "FAIL"));
        assertTrue(DoubleMetaphone.contains("TESTING", 0, 4, "NOPE", "TEST"));
    }

    // =========================================================================
    // Partition F: DoubleMetaphoneResult Inner Class Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleMetaphoneResultClassDirectly() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        final DoubleMetaphone.DoubleMetaphoneResult result = dm.new DoubleMetaphoneResult(4);

        assertFalse(result.isComplete());
        result.append('A');
        assertEquals("A", result.getPrimary());
        assertEquals("A", result.getAlternate());

        result.append('B', 'C');
        assertEquals("AB", result.getPrimary());
        assertEquals("AC", result.getAlternate());

        result.append("DE");
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
        assertTrue(result.isComplete());

        // Append past max length should truncate
        result.append('F');
        result.append("GH");
        result.appendPrimary('X');
        result.appendAlternate('Y');
        result.appendPrimary("LONGSTRING");
        result.appendAlternate("LONGSTRING");
        assertEquals("ABDE", result.getPrimary());
        assertEquals("ACDE", result.getAlternate());
    }

    @Test(timeout = 4000)
    public void testDoubleMetaphoneResultStringAppendVariants() {
        final DoubleMetaphone dm = new DoubleMetaphone();
        final DoubleMetaphone.DoubleMetaphoneResult result = dm.new DoubleMetaphoneResult(5);

        result.append("AB", "CD");
        assertEquals("AB", result.getPrimary());
        assertEquals("CD", result.getAlternate());

        result.appendPrimary("EFG");
        result.appendAlternate("HIJ");
        assertEquals("ABEFG", result.getPrimary());
        assertEquals("CDHIJ", result.getAlternate());
        assertTrue(result.isComplete());
    }

    // =========================================================================
    // Partition G: StringEncoder Interface Contracts & Exceptions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeStringObject() throws EncoderException {
        final DoubleMetaphone dm = new DoubleMetaphone();
        final Object encoded = dm.encode((Object) "Smith");
        assertTrue(encoded instanceof String);
        assertEquals("SMT", encoded);
        assertEquals("SMT", dm.encode("Smith"));
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeNonStringThrowsException() throws EncoderException {
        final DoubleMetaphone dm = new DoubleMetaphone();
        dm.encode(new Integer(42));
    }

    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeNullObjectThrowsException() throws EncoderException {
        final DoubleMetaphone dm = new DoubleMetaphone();
        dm.encode((Object) null);
    }
}