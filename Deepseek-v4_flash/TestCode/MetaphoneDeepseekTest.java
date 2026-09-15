package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class MetaphoneDeepseekTest {

    /**
     * @target metaphone(String)
     * @scenario null input
     * @defectRisk NullPointerException if null not handled
     */
    @Test(timeout = 4000)
    public void testMetaphoneNull() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(null));
    }

    /**
     * @target metaphone(String)
     * @scenario empty string input
     * @defectRisk ArrayIndexOutOfBoundsException if empty not handled
     */
    @Test(timeout = 4000)
    public void testMetaphoneEmpty() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(""));
    }

    /**
     * @target metaphone(String)
     * @scenario single character input
     * @defectRisk Incorrect uppercase conversion
     */
    @Test(timeout = 4000)
    public void testMetaphoneSingleChar() {
        Metaphone metaphone = new Metaphone();
        assertEquals("A", metaphone.metaphone("a"));
        assertEquals("Z", metaphone.metaphone("z"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial KN transformation
     * @defectRisk Incorrect handling of initial KN
     */
    @Test(timeout = 4000)
    public void testInitialKN() {
        Metaphone metaphone = new Metaphone();
        assertEquals("N", metaphone.metaphone("KN"));
        assertEquals("N", metaphone.metaphone("kn"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial GN transformation
     * @defectRisk Incorrect handling of initial GN
     */
    @Test(timeout = 4000)
    public void testInitialGN() {
        Metaphone metaphone = new Metaphone();
        assertEquals("N", metaphone.metaphone("GN"));
        assertEquals("N", metaphone.metaphone("gn"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial PN transformation
     * @defectRisk Incorrect handling of initial PN
     */
    @Test(timeout = 4000)
    public void testInitialPN() {
        Metaphone metaphone = new Metaphone();
        assertEquals("N", metaphone.metaphone("PN"));
        assertEquals("N", metaphone.metaphone("pn"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial AE transformation
     * @defectRisk Incorrect handling of initial AE
     */
    @Test(timeout = 4000)
    public void testInitialAE() {
        Metaphone metaphone = new Metaphone();
        assertEquals("E", metaphone.metaphone("AE"));
        assertEquals("E", metaphone.metaphone("ae"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial WR transformation
     * @defectRisk Incorrect handling of initial WR
     */
    @Test(timeout = 4000)
    public void testInitialWR() {
        Metaphone metaphone = new Metaphone();
        assertEquals("R", metaphone.metaphone("WR"));
        assertEquals("R", metaphone.metaphone("wr"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial WH transformation
     * @defectRisk Incorrect handling of initial WH
     */
    @Test(timeout = 4000)
    public void testInitialWH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("W", metaphone.metaphone("WH"));
        assertEquals("W", metaphone.metaphone("wh"));
    }

    /**
     * @target metaphone(String)
     * @scenario initial X transformation
     * @defectRisk Incorrect handling of initial X
     */
    @Test(timeout = 4000)
    public void testInitialX() {
        Metaphone metaphone = new Metaphone();
        assertEquals("S", metaphone.metaphone("X"));
        assertEquals("S", metaphone.metaphone("x"));
    }

    /**
     * @target metaphone(String)
     * @scenario vowel at beginning retained
     * @defectRisk Vowel not retained at beginning
     */
    @Test(timeout = 4000)
    public void testVowelAtBeginning() {
        Metaphone metaphone = new Metaphone();
        assertEquals("A", metaphone.metaphone("A"));
        assertEquals("E", metaphone.metaphone("E"));
        assertEquals("I", metaphone.metaphone("I"));
        assertEquals("O", metaphone.metaphone("O"));
        assertEquals("U", metaphone.metaphone("U"));
    }

    /**
     * @target metaphone(String)
     * @scenario vowel not at beginning dropped
     * @defectRisk Vowel incorrectly retained
     */
    @Test(timeout = 4000)
    public void testVowelNotAtBeginning() {
        Metaphone metaphone = new Metaphone();
        assertEquals("B", metaphone.metaphone("BA"));
        assertEquals("B", metaphone.metaphone("BE"));
        assertEquals("B", metaphone.metaphone("BI"));
        assertEquals("B", metaphone.metaphone("BO"));
        assertEquals("B", metaphone.metaphone("BU"));
    }

    /**
     * @target metaphone(String)
     * @scenario B at end after M silent
     * @defectRisk B not silent after M at end
     */
    @Test(timeout = 4000)
    public void testMBEnd() {
        Metaphone metaphone = new Metaphone();
        assertEquals("M", metaphone.metaphone("MB"));
        assertEquals("M", metaphone.metaphone("mb"));
    }

    /**
     * @target metaphone(String)
     * @scenario B not at end after M
     * @defectRisk B incorrectly silent
     */
    @Test(timeout = 4000)
    public void testMBNotEnd() {
        Metaphone metaphone = new Metaphone();
        assertEquals("MB", metaphone.metaphone("MBA"));
        assertEquals("MB", metaphone.metaphone("mba"));
    }

    /**
     * @target metaphone(String)
     * @scenario CIA -> X
     * @defectRisk CIA not mapped to X
     */
    @Test(timeout = 4000)
    public void testCIA() {
        Metaphone metaphone = new Metaphone();
        assertEquals("X", metaphone.metaphone("CIA"));
        assertEquals("X", metaphone.metaphone("cia"));
    }

    /**
     * @target metaphone(String)
     * @scenario CI, CE, CY -> S
     * @defectRisk CI/CE/CY not mapped to S
     */
    @Test(timeout = 4000)
    public void testCI_CY() {
        Metaphone metaphone = new Metaphone();
        assertEquals("S", metaphone.metaphone("CI"));
        assertEquals("S", metaphone.metaphone("CE"));
        assertEquals("S", metaphone.metaphone("CY"));
    }

    /**
     * @target metaphone(String)
     * @scenario SCH -> SK
     * @defectRisk SCH not mapped to SK
     */
    @Test(timeout = 4000)
    public void testSCH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("SK", metaphone.metaphone("SCH"));
        assertEquals("SK", metaphone.metaphone("sch"));
    }

    /**
     * @target metaphone(String)
     * @scenario CH at beginning followed by vowel -> K
     * @defectRisk CH not mapped to K
     */
    @Test(timeout = 4000)
    public void testCHBeginningVowel() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("CHA"));
        assertEquals("K", metaphone.metaphone("cha"));
    }

    /**
     * @target metaphone(String)
     * @scenario CH at beginning followed by consonant -> K
     * @defectRisk CH not mapped to K
     */
    @Test(timeout = 4000)
    public void testCHBeginningConsonant() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("CHR"));
        assertEquals("K", metaphone.metaphone("chr"));
    }

    /**
     * @target metaphone(String)
     * @scenario CH not at beginning -> X
     * @defectRisk CH not mapped to X
     */
    @Test(timeout = 4000)
    public void testCHNotBeginning() {
        Metaphone metaphone = new Metaphone();
        assertEquals("X", metaphone.metaphone("ACH"));
        assertEquals("X", metaphone.metaphone("ach"));
    }

    /**
     * @target metaphone(String)
     * @scenario DGE, DGI, DGY -> J
     * @defectRisk DGE/DGI/DGY not mapped to J
     */
    @Test(timeout = 4000)
    public void testDGE_DGI_DGY() {
        Metaphone metaphone = new Metaphone();
        assertEquals("J", metaphone.metaphone("DGE"));
        assertEquals("J", metaphone.metaphone("DGI"));
        assertEquals("J", metaphone.metaphone("DGY"));
    }

    /**
     * @target metaphone(String)
     * @scenario DT -> T
     * @defectRisk DT not mapped to T
     */
    @Test(timeout = 4000)
    public void testDT() {
        Metaphone metaphone = new Metaphone();
        assertEquals("T", metaphone.metaphone("DT"));
        assertEquals("T", metaphone.metaphone("dt"));
    }

    /**
     * @target metaphone(String)
     * @scenario GH silent at end
     * @defectRisk GH not silent at end
     */
    @Test(timeout = 4000)
    public void testGHEnd() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone("GH"));
        assertEquals("", metaphone.metaphone("gh"));
    }

    /**
     * @target metaphone(String)
     * @scenario GH silent before consonant
     * @defectRisk GH not silent before consonant
     */
    @Test(timeout = 4000)
    public void testGHBeforeConsonant() {
        Metaphone metaphone = new Metaphone();
        assertEquals("T", metaphone.metaphone("GHT"));
        assertEquals("T", metaphone.metaphone("ght"));
    }

    /**
     * @target metaphone(String)
     * @scenario GN or GNED silent G
     * @defectRisk G not silent in GN/GNED
     */
    @Test(timeout = 4000)
    public void testGN_GNE() {
        Metaphone metaphone = new Metaphone();
        assertEquals("N", metaphone.metaphone("GN"));
        assertEquals("NT", metaphone.metaphone("GNED"));
    }

    /**
     * @target metaphone(String)
     * @scenario G followed by I/E/Y -> J
     * @defectRisk G not mapped to J before I/E/Y
     */
    @Test(timeout = 4000)
    public void testGBeforeIEY() {
        Metaphone metaphone = new Metaphone();
        assertEquals("J", metaphone.metaphone("GI"));
        assertEquals("J", metaphone.metaphone("GE"));
        assertEquals("J", metaphone.metaphone("GY"));
    }

    /**
     * @target metaphone(String)
     * @scenario G followed by other -> K
     * @defectRisk G not mapped to K
     */
    @Test(timeout = 4000)
    public void testGOther() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("GA"));
        assertEquals("K", metaphone.metaphone("GO"));
        assertEquals("K", metaphone.metaphone("GU"));
    }

    /**
     * @target metaphone(String)
     * @scenario H at end silent
     * @defectRisk H not silent at end
     */
    @Test(timeout = 4000)
    public void testHEnd() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone("H"));
        assertEquals("", metaphone.metaphone("h"));
    }

    /**
     * @target metaphone(String)
     * @scenario H after C, S, P, T, G silent
     * @defectRisk H not silent after C/S/P/T/G
     */
    @Test(timeout = 4000)
    public void testHAfterCSPTG() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("CH"));
        assertEquals("S", metaphone.metaphone("SH"));
        assertEquals("P", metaphone.metaphone("PH"));
        assertEquals("T", metaphone.metaphone("TH"));
        assertEquals("K", metaphone.metaphone("GH"));
    }

    /**
     * @target metaphone(String)
     * @scenario H followed by vowel
     * @defectRisk H not mapped to H before vowel
     */
    @Test(timeout = 4000)
    public void testHBeforeVowel() {
        Metaphone metaphone = new Metaphone();
        assertEquals("H", metaphone.metaphone("HA"));
        assertEquals("H", metaphone.metaphone("HE"));
        assertEquals("H", metaphone.metaphone("HI"));
        assertEquals("H", metaphone.metaphone("HO"));
        assertEquals("H", metaphone.metaphone("HU"));
    }

    /**
     * @target metaphone(String)
     * @scenario PH -> F
     * @defectRisk PH not mapped to F
     */
    @Test(timeout = 4000)
    public void testPH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("F", metaphone.metaphone("PH"));
        assertEquals("F", metaphone.metaphone("ph"));
    }

    /**
     * @target metaphone(String)
     * @scenario SH, SIO, SIA -> X
     * @defectRisk SH/SIO/SIA not mapped to X
     */
    @Test(timeout = 4000)
    public void testSH_SIO_SIA() {
        Metaphone metaphone = new Metaphone();
        assertEquals("X", metaphone.metaphone("SH"));
        assertEquals("X", metaphone.metaphone("SIO"));
        assertEquals("X", metaphone.metaphone("SIA"));
    }

    /**
     * @target metaphone(String)
     * @scenario TIA, TIO -> X
     * @defectRisk TIA/TIO not mapped to X
     */
    @Test(timeout = 4000)
    public void testTIA_TIO() {
        Metaphone metaphone = new Metaphone();
        assertEquals("X", metaphone.metaphone("TIA"));
        assertEquals("X", metaphone.metaphone("TIO"));
    }

    /**
     * @target metaphone(String)
     * @scenario TCH silent T
     * @defectRisk T not silent in TCH
     */
    @Test(timeout = 4000)
    public void testTCH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("X", metaphone.metaphone("TCH"));
        assertEquals("X", metaphone.metaphone("tch"));
    }

    /**
     * @target metaphone(String)
     * @scenario TH -> 0
     * @defectRisk TH not mapped to 0
     */
    @Test(timeout = 4000)
    public void testTH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("0", metaphone.metaphone("TH"));
        assertEquals("0", metaphone.metaphone("th"));
    }

    /**
     * @target metaphone(String)
     * @scenario V, W, Y silent if not followed by vowel
     * @defectRisk V/W/Y not silent
     */
    @Test(timeout = 4000)
    public void testVWYNotBeforeVowel() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone("V"));
        assertEquals("", metaphone.metaphone("W"));
        assertEquals("", metaphone.metaphone("Y"));
    }

    /**
     * @target metaphone(String)
     * @scenario V, W, Y followed by vowel
     * @defectRisk V/W/Y incorrectly silent
     */
    @Test(timeout = 4000)
    public void testVWYBeforeVowel() {
        Metaphone metaphone = new Metaphone();
        assertEquals("V", metaphone.metaphone("VA"));
        assertEquals("W", metaphone.metaphone("WA"));
        assertEquals("Y", metaphone.metaphone("YA"));
    }

    /**
     * @target metaphone(String)
     * @scenario X -> KS
     * @defectRisk X not mapped to KS
     */
    @Test(timeout = 4000)
    public void testX() {
        Metaphone metaphone = new Metaphone();
        assertEquals("KS", metaphone.metaphone("AX"));
        assertEquals("KS", metaphone.metaphone("ax"));
    }

    /**
     * @target metaphone(String)
     * @scenario Z -> S
     * @defectRisk Z not mapped to S
     */
    @Test(timeout = 4000)
    public void testZ() {
        Metaphone metaphone = new Metaphone();
        assertEquals("S", metaphone.metaphone("Z"));
        assertEquals("S", metaphone.metaphone("z"));
    }

    /**
     * @target metaphone(String)
     * @scenario max code length truncation
     * @defectRisk Code not truncated to maxCodeLen
     */
    @Test(timeout = 4000)
    public void testMaxCodeLenTruncation() {
        Metaphone metaphone = new Metaphone();
        assertEquals(4, metaphone.metaphone("ABCDEFGHIJKLMNOP").length());
    }

    /**
     * @target setMaxCodeLen(int)
     * @scenario set max code length
     * @defectRisk maxCodeLen not updated
     */
    @Test(timeout = 4000)
    public void testSetMaxCodeLen() {
        Metaphone metaphone = new Metaphone();
        metaphone.setMaxCodeLen(2);
        assertEquals(2, metaphone.getMaxCodeLen());
        assertEquals(2, metaphone.metaphone("ABCDEFGHIJKLMNOP").length());
    }

    /**
     * @target getMaxCodeLen()
     * @scenario default max code length
     * @defectRisk default value incorrect
     */
    @Test(timeout = 4000)
    public void testGetMaxCodeLenDefault() {
        Metaphone metaphone = new Metaphone();
        assertEquals(4, metaphone.getMaxCodeLen());
    }

    /**
     * @target encode(Object)
     * @scenario valid String input
     * @defectRisk Incorrect encoding
     */
    @Test(timeout = 4000)
    public void testEncodeObjectValid() throws EncoderException {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.encode((Object) "K"));
    }

    /**
     * @target encode(Object)
     * @scenario null input
     * @defectRisk NullPointerException if null not handled
     */
    @Test(timeout = 4000)
    public void testEncodeObjectNull() throws EncoderException {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.encode((Object) null));
    }

    /**
     * @target encode(Object)
     * @scenario non-String input
     * @defectRisk EncoderException not thrown
     */
    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeObjectNonString() throws EncoderException {
        Metaphone metaphone = new Metaphone();
        metaphone.encode((Object) Integer.valueOf(1));
    }

    /**
     * @target encode(String)
     * @scenario valid String input
     * @defectRisk Incorrect encoding
     */
    @Test(timeout = 4000)
    public void testEncodeString() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.encode("K"));
    }

    /**
     * @target isMetaphoneEqual(String, String)
     * @scenario equal metaphones
     * @defectRisk Incorrect equality check
     */
    @Test(timeout = 4000)
    public void testIsMetaphoneEqualTrue() {
        Metaphone metaphone = new Metaphone();
        assertTrue(metaphone.isMetaphoneEqual("K", "C"));
        assertTrue(metaphone.isMetaphoneEqual("PH", "F"));
    }

    /**
     * @target isMetaphoneEqual(String, String)
     * @scenario different metaphones
     * @defectRisk Incorrect equality check
     */
    @Test(timeout = 4000)
    public void testIsMetaphoneEqualFalse() {
        Metaphone metaphone = new Metaphone();
        assertFalse(metaphone.isMetaphoneEqual("K", "L"));
        assertFalse(metaphone.isMetaphoneEqual("A", "B"));
    }

    /**
     * @target metaphone(String)
     * @scenario duplicate letters removed except C
     * @defectRisk Duplicate letters not removed
     */
    @Test(timeout = 4000)
    public void testDuplicateLetters() {
        Metaphone metaphone = new Metaphone();
        assertEquals("B", metaphone.metaphone("BB"));
        assertEquals("B", metaphone.metaphone("bb"));
    }

    /**
     * @target metaphone(String)
     * @scenario duplicate C not removed
     * @defectRisk Duplicate C incorrectly removed
     */
    @Test(timeout = 4000)
    public void testDuplicateC() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("CC"));
        assertEquals("K", metaphone.metaphone("cc"));
    }

    /**
     * @target metaphone(String)
     * @scenario locale independence Turkish
     * @defectRisk Turkish locale causes incorrect uppercase conversion
     */
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

    /**
     * @target metaphone(String)
     * @scenario complex word encoding
     * @defectRisk Incorrect encoding of complex words
     */
    @Test(timeout = 4000)
    public void testComplexWords() {
        Metaphone metaphone = new Metaphone();
        assertEquals("KMPL", metaphone.metaphone("COMPLETE"));
        assertEquals("KMPL", metaphone.metaphone("complete"));
        assertEquals("0M", metaphone.metaphone("THOMAS"));
        assertEquals("0M", metaphone.metaphone("thomas"));
    }

    /**
     * @target metaphone(String)
     * @scenario mixed case input
     * @defectRisk Incorrect case handling
     */
    @Test(timeout = 4000)
    public void testMixedCase() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("k"));
        assertEquals("K", metaphone.metaphone("K"));
        assertEquals("K", metaphone.metaphone("kK"));
    }

    /**
     * @target metaphone(String)
     * @scenario whitespace input
     * @defectRisk Whitespace not handled
     */
    @Test(timeout = 4000)
    public void testWhitespace() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(" "));
        assertEquals("", metaphone.metaphone("  "));
    }

    /**
     * @target metaphone(String)
     * @scenario special characters
     * @defectRisk Special characters not handled
     */
    @Test(timeout = 4000)
    public void testSpecialCharacters() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone("!"));
        assertEquals("", metaphone.metaphone("@"));
        assertEquals("", metaphone.metaphone("#"));
    }

    /**
     * @target metaphone(String)
     * @scenario numbers input
     * @defectRisk Numbers not handled
     */
    @Test(timeout = 4000)
    public void testNumbers() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone("123"));
        assertEquals("", metaphone.metaphone("0"));
    }

    /**
     * @target metaphone(String)
     * @scenario long input exceeding max code length
     * @defectRisk Code not truncated
     */
    @Test(timeout = 4000)
    public void testLongInput() {
        Metaphone metaphone = new Metaphone();
        String longString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        assertEquals(4, metaphone.metaphone(longString).length());
    }

    /**
     * @target metaphone(String)
     * @scenario input with only vowels
     * @defectRisk Vowels not handled correctly
     */
    @Test(timeout = 4000)
    public void testOnlyVowels() {
        Metaphone metaphone = new Metaphone();
        assertEquals("A", metaphone.metaphone("AEIOU"));
        assertEquals("A", metaphone.metaphone("aeiou"));
    }

    /**
     * @target metaphone(String)
     * @scenario input with only consonants
     * @defectRisk Consonants not handled correctly
     */
    @Test(timeout = 4000)
    public void testOnlyConsonants() {
        Metaphone metaphone = new Metaphone();
        assertEquals("BCDFG", metaphone.metaphone("BCDFG"));
        assertEquals("BCDFG", metaphone.metaphone("bcdfg"));
    }

    /**
     * @target metaphone(String)
     * @scenario input with repeated patterns
     * @defectRisk Repeated patterns not handled correctly
     */
    @Test(timeout = 4000)
    public void testRepeatedPatterns() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("CAC"));
        assertEquals("K", metaphone.metaphone("cac"));
    }

    /**
     * @target metaphone(String)
     * @scenario input with silent letters
     * @defectRisk Silent letters not handled correctly
     */
    @Test(timeout = 4000)
    public void testSilentLetters() {
        Metaphone metaphone = new Metaphone();
        assertEquals("K", metaphone.metaphone("KNIGHT"));
        assertEquals("K", metaphone.metaphone("knight"));
        assertEquals("R", metaphone.metaphone("WRITE"));
        assertEquals("R", metaphone.metaphone("write"));
    }

    /**
     * @target metaphone(String)
     * @scenario input with combinations
     * @defectRisk Combinations not handled correctly
     */
    @Test(timeout = 4000)
    public void testCombinations() {
        Metaphone metaphone = new Metaphone();
        assertEquals("F", metaphone.metaphone("PHONE"));
        assertEquals("F", metaphone.metaphone("phone"));
        assertEquals("X", metaphone.metaphone("SHIP"));
        assertEquals("X", metaphone.metaphone("ship"));
    }

    /**
     * @target metaphone(String)
     * @scenario input with edge cases
     * @defectRisk Edge cases not handled correctly
     */
    @Test(timeout = 4000)
    public void testEdgeCases() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(""));
        assertEquals("", metaphone.metaphone(null));
        assertEquals("A", metaphone.metaphone("A"));
        assertEquals("A", metaphone.metaphone("a"));
    }
}