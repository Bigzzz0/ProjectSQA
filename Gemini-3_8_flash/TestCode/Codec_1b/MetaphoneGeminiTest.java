/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: org.apache.commons.codec.language.Metaphone
 *
 * Decision / Condition Coverage Matrix:
 * 1. Initial 2-character transformations:
 *    - 'K', 'G', 'P' followed by 'N' vs other letters (KN/GN/PN -> N)
 *    - 'A' followed by 'E' vs other letters (AE -> E)
 *    - 'W' followed by 'R' (WR -> R)
 *    - 'W' followed by 'H' (WH -> W)
 *    - 'W' followed by vowel or other consonants
 *    - 'X' at index 0 (X -> S)
 *    - Default initial consonants/vowels
 * 2. Deduplication loop:
 *    - Duplicate letters: skipped if symb != 'C' and isPreviousChar matches.
 *    - Duplicate 'C' (e.g., "ACCIDENT" -> "C" not skipped; "AC" + "C").
 * 3. Switch cases & phonetics:
 *    - Vowels (A, E, I, O, U): retained only if leading char (n == 0).
 *    - 'B': silent when word ends in "MB"; pronounced otherwise.
 *    - 'C':
 *      * Discarded if preceded by 'S' and followed by front vowels (SCI, SCE, SCY).
 *      * "CIA" -> 'X'
 *      * Followed by front vowel (CI, CE, CY) without previous 'S' -> 'S'.
 *      * Preceded by 'S' and followed by 'H' (SCH -> SK) -> 'K'.
 *      * Followed by 'H': leading CH + vowel -> 'K'; otherwise -> 'X'.
 *      * Default 'C' -> 'K'.
 *    - 'D': followed by 'G' and front vowel (DGE, DGI, DGY) -> 'J' (n += 2); else 'T'.
 *    - 'G':
 *      * Silent before terminal 'H' or non-vowel after 'H' (GH silent / GHT).
 *      * Silent in "GN" / "GNED" (internal or ending).
 *      * Followed by front vowel with hard flag (after 'G') vs soft ('J' vs 'K').
 *      * Default 'G' -> 'K'.
 *    - 'H': terminal H (silent); preceded by VARSON ("CSPTG" -> silent); followed by vowel ('H'); else silent.
 *    - 'F', 'J', 'L', 'M', 'N', 'R' -> preserved directly.
 *    - 'K': silent if preceded by 'C'; pronounced if initial or not preceded by 'C'.
 *    - 'P': followed by 'H' -> 'F'; else -> 'P'.
 *    - 'Q' -> 'K'.
 *    - 'S': followed by "SH", "SIO", "SIA" -> 'X'; else -> 'S'.
 *    - 'T': "TIA", "TIO" -> 'X'; "TCH" -> silent; "TH" -> '0'; else -> 'T'.
 *    - 'V' -> 'F'.
 *    - 'W', 'Y': followed by vowel -> retained; else silent.
 *    - 'X' -> "KS".
 *    - 'Z' -> 'S'.
 * 4. Boundaries & Extreme values:
 *    - null input -> ""
 *    - empty string "" -> ""
 *    - 1-character strings (e.g., "A", "z", "i")
 *    - Max code length truncation (default 4, custom sizes).
 * 5. Defects4J Known Defect (Locale Independence):
 *    - Under Turkish (tr) locale, lowercase 'i'.toUpperCase() yields 'İ' (\u0130),
 *      causing single-character encoding failure or character mapping mismatch.
 * =========================================================================
 */

package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class MetaphoneGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & Phonetic Transformation Rules
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialPrefixTransformations() {
        Metaphone m = new Metaphone();
        // KN -> N
        assertEquals("NFT", m.metaphone("KNIGHT"));
        // GN -> N
        assertEquals("NT", m.metaphone("GNAT"));
        // PN -> N
        assertEquals("NMS", m.metaphone("PNEUMATIC"));
        // AE -> E
        assertEquals("ESTT", m.metaphone("AESTHETIC"));
        // WR -> R
        assertEquals("RT", m.metaphone("WRITE"));
        // WH -> W (followed by vowel)
        assertEquals("WT", m.metaphone("WHITE"));
        // Initial X -> S
        assertEquals("SLFN", m.metaphone("XYLOPHONE"));

        // Negative checks for initials without target second char
        assertEquals("KPK", m.metaphone("KAPPA"));
        assertEquals("GTS", m.metaphone("GATES"));
        assertEquals("PT", m.metaphone("PET"));
        assertEquals("APL", m.metaphone("APPLE"));
        assertEquals("WTR", m.metaphone("WATER"));
    }

    @Test(timeout = 4000)
    public void testDuplicateConsonantsHandling() {
        Metaphone m = new Metaphone();
        // Consecutive identical letters compressed, except 'C'
        assertEquals("BL", m.metaphone("BELL"));
        assertEquals("MR", m.metaphone("MARRY"));
        assertEquals("APL", m.metaphone("APPLE"));
        // Consecutive C should not simply be ignored: 'ACCIDENT' -> AKSDNT -> truncated to 4 chars "AKST"
        assertEquals("AKST", m.metaphone("ACCIDENT"));
        // 'ACCEPT' -> AKSPT -> "AKSP"
        assertEquals("AKSP", m.metaphone("ACCEPT"));
    }

    @Test(timeout = 4000)
    public void testVowelHandling() {
        Metaphone m = new Metaphone();
        // Vowels encoded only if initial
        assertEquals("A", m.metaphone("A"));
        assertEquals("E", m.metaphone("E"));
        assertEquals("I", m.metaphone("I"));
        assertEquals("O", m.metaphone("O"));
        assertEquals("U", m.metaphone("U"));
        assertEquals("AL", m.metaphone("ALE"));
        assertEquals("EL", m.metaphone("EEL"));
        assertEquals("IL", m.metaphone("ILE"));
        assertEquals("OL", m.metaphone("OLE"));
        assertEquals("UL", m.metaphone("ULE"));
        // Interior vowels are dropped
        assertEquals("B", m.metaphone("BEE"));
        assertEquals("BT", m.metaphone("BOAT"));
    }

    @Test(timeout = 4000)
    public void testBTransformations() {
        Metaphone m = new Metaphone();
        // Silent B if word ends in MB
        assertEquals("KLM", m.metaphone("CLIMB"));
        assertEquals("TM", m.metaphone("THUMB"));
        assertEquals("DM", m.metaphone("DUMB"));
        // Non-terminal MB retains B
        assertEquals("MMBR", m.metaphone("MEMBER"));
        // Non-MB B
        assertEquals("B", m.metaphone("BE"));
    }

    @Test(timeout = 4000)
    public void testCTransformations() {
        Metaphone m = new Metaphone();
        // Preceded by S and followed by E, I, Y -> discarded
        assertEquals("SN", m.metaphone("SCENE"));
        assertEquals("SNS", m.metaphone("SCIENCE"));
        assertEquals("S0", m.metaphone("SCYTHE"));

        // CIA -> X
        assertEquals("SPXL", m.metaphone("SPECIAL"));
        assertEquals("GLXL", m.metaphone("GLACIAL"));

        // Followed by front vowel (CI, CE, CY) -> S
        assertEquals("ST", m.metaphone("CITY"));
        assertEquals("SNT", m.metaphone("CENT"));
        assertEquals("SLNT", m.metaphone("CYLINDER"));

        // Preceded by S and followed by H (SCH -> SK) -> K
        assertEquals("SKL", m.metaphone("SCHOOL"));

        // CH: initial CH followed by vowel -> K
        assertEquals("KMS", m.metaphone("CHEMIST"));
        assertEquals("KRSM", m.metaphone("CHARISMA"));

        // CH: other CH -> X
        assertEquals("XRX", m.metaphone("CHURCH"));
        assertEquals("AX", m.metaphone("ACHE"));

        // Standard C -> K
        assertEquals("KT", m.metaphone("CAT"));
        assertEquals("KLK", m.metaphone("CLOCK"));
    }

    @Test(timeout = 4000)
    public void testDTransformations() {
        Metaphone m = new Metaphone();
        // DG + front vowel -> J (and advances index by 2)
        assertEquals("AJ", m.metaphone("EDGE"));
        assertEquals("BJ", m.metaphone("BADGE"));
        assertEquals("TJ", m.metaphone("DODGER"));
        assertEquals("EJ", m.metaphone("EDGY"));

        // Standard D -> T
        assertEquals("TT", m.metaphone("DADDY"));
        assertEquals("TR", m.metaphone("DOOR"));
    }

    @Test(timeout = 4000)
    public void testGTransformations() {
        Metaphone m = new Metaphone();
        // GH at end of word -> silent
        assertEquals("H", m.metaphone("HIGH"));
        assertEquals("0R", m.metaphone("THROUGH"));

        // GH followed by non-vowel -> silent
        assertEquals("NT", m.metaphone("NIGHT"));
        assertEquals("FT", m.metaphone("FIGHT"));
        assertEquals("WT", m.metaphone("WEIGHT"));

        // GN or GNED after initial -> silent G
        assertEquals("SN", m.metaphone("SIGN"));
        assertEquals("RSN", m.metaphone("RESIGNED"));

        // G followed by front vowel (EIY) without preceding G -> J
        assertEquals("JL", m.metaphone("GEL"));
        assertEquals("JJR", m.metaphone("GINGER"));
        assertEquals("JM", m.metaphone("GYM"));

        // Hard G (after another G) or before non-front vowel -> K
        assertEquals("KK", m.metaphone("GIGGLE"));
        assertEquals("K", m.metaphone("EGG"));
        assertEquals("KT", m.metaphone("GATE"));
        assertEquals("KT", m.metaphone("GOOD"));
    }

    @Test(timeout = 4000)
    public void testHTransformations() {
        Metaphone m = new Metaphone();
        // Initial H followed by vowel -> H
        assertEquals("HT", m.metaphone("HAT"));
        assertEquals("HM", m.metaphone("HOME"));

        // Terminal H -> silent
        assertEquals("A", m.metaphone("AH"));

        // Preceded by VARSON ("CSPTG") -> silent
        assertEquals("A", m.metaphone("OH"));
        assertEquals("F", m.metaphone("PHIL")); // PH -> F, H swallowed
        assertEquals("0NK", m.metaphone("THANK")); // TH -> 0, H swallowed

        // H not followed by vowel -> silent
        assertEquals("KST", m.metaphone("EXHAUST"));
    }

    @Test(timeout = 4000)
    public void testKTransformations() {
        Metaphone m = new Metaphone();
        // Initial K
        assertEquals("KT", m.metaphone("KITE"));
        // K after C -> silent
        assertEquals("BK", m.metaphone("BACK"));
        assertEquals("TK", m.metaphone("DUCK"));
        // K not after C
        assertEquals("AK", m.metaphone("AUK"));
    }

    @Test(timeout = 4000)
    public void testPTransformations() {
        Metaphone m = new Metaphone();
        // PH -> F
        assertEquals("FN", m.metaphone("PHONE"));
        assertEquals("FR", m.metaphone("PHRASE"));
        // Standard P -> P
        assertEquals("PT", m.metaphone("POT"));
    }

    @Test(timeout = 4000)
    public void testQTransformations() {
        Metaphone m = new Metaphone();
        // Q -> K
        assertEquals("KK", m.metaphone("QUICK"));
        assertEquals("KN", m.metaphone("QUEEN"));
    }

    @Test(timeout = 4000)
    public void testSTransformations() {
        Metaphone m = new Metaphone();
        // SH -> X
        assertEquals("XP", m.metaphone("SHIP"));
        // SIO -> X
        assertEquals("PXN", m.metaphone("PASSION"));
        // SIA -> X
        assertEquals("RX", m.metaphone("RUSSIA"));
        assertEquals("AX", m.metaphone("ASIA"));
        // Normal S -> S
        assertEquals("SN", m.metaphone("SUN"));
    }

    @Test(timeout = 4000)
    public void testTTransformations() {
        Metaphone m = new Metaphone();
        // TIA, TIO -> X
        assertEquals("NXN", m.metaphone("NATION"));
        assertEquals("INXL", m.metaphone("INITIAL"));

        // TCH -> silent T
        assertEquals("KX", m.metaphone("CATCH"));
        assertEquals("MX", m.metaphone("MATCH"));

        // TH -> 0
        assertEquals("0N", m.metaphone("THIN"));
        assertEquals("0TK", m.metaphone("THICK"));

        // Standard T -> T
        assertEquals("TN", m.metaphone("TEN"));
    }

    @Test(timeout = 4000)
    public void testVTransformations() {
        Metaphone m = new Metaphone();
        // V -> F
        assertEquals("FN", m.metaphone("VAN"));
        assertEquals("FT", m.metaphone("VOTE"));
    }

    @Test(timeout = 4000)
    public void testWYTransformations() {
        Metaphone m = new Metaphone();
        // Followed by vowel -> retained
        assertEquals("WST", m.metaphone("WEST"));
        assertEquals("YS", m.metaphone("YES"));
        // Terminal or followed by consonant -> silent
        assertEquals("SN", m.metaphone("SNOW"));
        assertEquals("T", m.metaphone("DAY"));
        assertEquals("RN", m.metaphone("RHYTHM"));
    }

    @Test(timeout = 4000)
    public void testXZTransformations() {
        Metaphone m = new Metaphone();
        // Non-initial X -> KS
        assertEquals("FKS", m.metaphone("FOX"));
        assertEquals("TKS", m.metaphone("TAXI"));
        // Z -> S
        assertEquals("SR", m.metaphone("ZERO"));
        assertEquals("SS", m.metaphone("ZEUS"));
    }

    @Test(timeout = 4000)
    public void testSimpleConsonantsFJLMNR() {
        Metaphone m = new Metaphone();
        assertEquals("FR", m.metaphone("FAR"));
        assertEquals("JR", m.metaphone("JAR"));
        assertEquals("LT", m.metaphone("LATE"));
        assertEquals("MN", m.metaphone("MAN"));
        assertEquals("NT", m.metaphone("NET"));
        assertEquals("RT", m.metaphone("RAT"));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyInputs() {
        Metaphone m = new Metaphone();
        assertEquals("", m.metaphone(null));
        assertEquals("", m.metaphone(""));
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInputs() {
        Metaphone m = new Metaphone();
        assertEquals("A", m.metaphone("A"));
        assertEquals("B", m.metaphone("b"));
        assertEquals("K", m.metaphone("K"));
        assertEquals("Z", m.metaphone("z"));
    }

    @Test(timeout = 4000)
    public void testMaxCodeLengthLimits() {
        Metaphone m = new Metaphone();
        assertEquals(4, m.getMaxCodeLen());

        m.setMaxCodeLen(2);
        assertEquals(2, m.getMaxCodeLen());
        assertEquals("AL", m.metaphone("ALLIGATOR"));

        m.setMaxCodeLen(6);
        assertEquals(6, m.getMaxCodeLen());
        assertEquals("ALKRTR", m.metaphone("ALLIGATOR"));

        m.setMaxCodeLen(1);
        assertEquals("A", m.metaphone("ALLIGATOR"));

        // Reset to default
        m.setMaxCodeLen(4);
        assertEquals("ALKR", m.metaphone("ALLIGATOR"));
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Codec-1 / Locale Independence)
    // =========================================================================

    @Test(timeout = 4000)
    public void testLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            Metaphone metaphone = new Metaphone();
            assertEquals("I", metaphone.metaphone("i"));
            assertEquals(metaphone.metaphone("I"), metaphone.metaphone("i"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithNonStringObjectThrowsException() throws EncoderException {
        Metaphone m = new Metaphone();
        m.encode(Integer.valueOf(12345));
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithNullObjectThrowsException() throws EncoderException {
        Metaphone m = new Metaphone();
        m.encode((Object) null);
    }

    // =========================================================================
    // PARTITION E: Interface & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeObjectWithString() throws EncoderException {
        Metaphone m = new Metaphone();
        Object result = m.encode((Object) "testing");
        assertNotNull(result);
        assertTrue(result instanceof String);
        assertEquals("TSTN", result);
    }

    @Test(timeout = 4000)
    public void testEncodeStringDirectly() {
        Metaphone m = new Metaphone();
        String result = m.encode("testing");
        assertEquals("TSTN", result);
    }

    @Test(timeout = 4000)
    public void testIsMetaphoneEqual() {
        Metaphone m = new Metaphone();
        assertTrue(m.isMetaphoneEqual("knight", "night"));
        assertTrue(m.isMetaphoneEqual("phone", "fone"));
        assertTrue(m.isMetaphoneEqual("white", "wite"));
        assertFalse(m.isMetaphoneEqual("knight", "day"));
        assertFalse(m.isMetaphoneEqual("apple", "banana"));
    }
}