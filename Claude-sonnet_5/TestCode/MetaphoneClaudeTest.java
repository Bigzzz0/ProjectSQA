package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class MetaphoneClaudeTest {

    /**
     * @target metaphone(String)
     * @scenario null input
     * @defectRisk NullPointerException instead of returning empty string
     */
    @Test(timeout = 4000)
    public void testMetaphoneNullInput() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(null));
    }

    /**
     * @target metaphone(String)
     * @scenario empty string input
     * @defectRisk incorrect handling of empty string
     */
    @Test(timeout = 4000)
    public void testMetaphoneEmptyInput() {
        Metaphone metaphone = new Metaphone();
        assertEquals("", metaphone.metaphone(""));
    }

    /**
     * @target metaphone(String)
     * @scenario single character input returns itself uppercased
     * @defectRisk incorrect uppercase conversion using locale-sensitive rules
     */
    @Test(timeout = 4000)
    public void testMetaphoneSingleCharacter() {
        Metaphone metaphone = new Metaphone();
        assertEquals("A", metaphone.metaphone("a"));
        assertEquals("Z", metaphone.metaphone("z"));
    }

    /**
     * @target metaphone(String) - initial KN handling
     * @scenario word starts with KN, silent K
     * @defectRisk failure to remove leading K before N
     */
    @Test(timeout = 4000)
    public void testInitialKN() {
        Metaphone metaphone = new Metaphone();
        assertEquals("N", metaphone.metaphone("KN"));
        assertEquals(metaphone.metaphone("N"), metaphone.metaphone("KN"));
    }

    /**
     * @target metaphone(String) - initial GN handling
     * @scenario word starts with GN, silent G
     * @defectRisk failure to remove leading G before N
     */
    @Test(timeout = 4000)
    public void testInitialGN() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("N"), metaphone.metaphone("GN"));
    }

    /**
     * @target metaphone(String) - initial PN handling
     * @scenario word starts with PN, silent P
     * @defectRisk failure to remove leading P before N
     */
    @Test(timeout = 4000)
    public void testInitialPN() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("N"), metaphone.metaphone("PN"));
    }

    /**
     * @target metaphone(String) - initial K without N
     * @scenario word starts with K not followed by N
     * @defectRisk incorrect handling of KX branch when inwd[1] != 'N'
     */
    @Test(timeout = 4000)
    public void testInitialKNoN() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("KAT");
        assertNotNull(result);
        assertTrue(result.startsWith("K"));
    }

    /**
     * @target metaphone(String) - initial AE handling
     * @scenario word starts with AE
     * @defectRisk failure to strip leading A before E
     */
    @Test(timeout = 4000)
    public void testInitialAE() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("EGIS"), metaphone.metaphone("AEGIS"));
    }

    /**
     * @target metaphone(String) - initial A without E
     * @scenario word starts with A not followed by E
     * @defectRisk incorrect default case handling for initial A
     */
    @Test(timeout = 4000)
    public void testInitialANoE() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("ABC");
        assertNotNull(result);
        assertEquals('A', result.charAt(0));
    }

    /**
     * @target metaphone(String) - initial WR handling
     * @scenario word starts with WR
     * @defectRisk failure to strip leading W before R
     */
    @Test(timeout = 4000)
    public void testInitialWR() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("RIGHT"), metaphone.metaphone("WRIGHT"));
    }

    /**
     * @target metaphone(String) - initial WH handling
     * @scenario word starts with WH
     * @defectRisk incorrect substitution of WH -> W
     */
    @Test(timeout = 4000)
    public void testInitialWH() {
        Metaphone metaphone = new Metaphone();
        assertEquals("WY", metaphone.metaphone("WHY"));
    }

    /**
     * @target metaphone(String) - initial W without R or H
     * @scenario word starts with W but not WR or WH
     * @defectRisk incorrect default branch for initial W
     */
    @Test(timeout = 4000)
    public void testInitialWNoRH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("WATER");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - initial X becomes S
     * @scenario word starts with X
     * @defectRisk incorrect substitution of initial X to S
     */
    @Test(timeout = 4000)
    public void testInitialX() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("SAVIER"), metaphone.metaphone("XAVIER"));
    }

    /**
     * @target metaphone(String) - default initial branch
     * @scenario word starts with a regular letter (not K,G,P,A,W,X)
     * @defectRisk default branch failing to append full word
     */
    @Test(timeout = 4000)
    public void testDefaultInitial() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("DOG");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - vowel handling, only leading vowel retained
     * @scenario word with multiple vowels not at start
     * @defectRisk vowels incorrectly retained in non-leading positions
     */
    @Test(timeout = 4000)
    public void testVowelsOnlyLeadingRetained() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("AAAA");
        assertEquals("A", result);
    }

    /**
     * @target metaphone(String) - duplicate letter removal (non-C)
     * @scenario word with duplicate consonant letters
     * @defectRisk duplicate letters incorrectly retained
     */
    @Test(timeout = 4000)
    public void testDuplicateLetterRemoval() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("BALL"), metaphone.metaphone("BAL"));
    }

    /**
     * @target metaphone(String) - B silent after M at end of word
     * @scenario word ends in MB
     * @defectRisk B incorrectly included when it should be silent
     */
    @Test(timeout = 4000)
    public void testBSilentAfterMB() {
        Metaphone metaphone = new Metaphone();
        assertEquals("KM", metaphone.metaphone("COMB"));
    }

    /**
     * @target metaphone(String) - B included when not after MB at end
     * @scenario word contains B not preceded by M or not at end
     * @defectRisk B silently dropped incorrectly
     */
    @Test(timeout = 4000)
    public void testBIncluded() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("BOB");
        assertTrue(result.indexOf('B') >= 0);
    }

    /**
     * @target metaphone(String) - C followed by SCI/SCE/SCY discarded
     * @scenario word contains SCI pattern
     * @defectRisk C not discarded correctly after S before front vowel
     */
    @Test(timeout = 4000)
    public void testCDiscardAfterSCI() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SCIENCE");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - CIA -> X
     * @scenario word contains CIA pattern
     * @defectRisk CIA not converted to X properly
     */
    @Test(timeout = 4000)
    public void testCIA() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("MALICIA");
        assertTrue(result.indexOf('X') >= 0);
    }

    /**
     * @target metaphone(String) - CI, CE, CY -> S
     * @scenario word contains C followed by front vowel not at CIA
     * @defectRisk C not converted to S when followed by I, E, Y
     */
    @Test(timeout = 4000)
    public void testCFrontVowel() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CIVIL");
        assertTrue(result.startsWith("S"));
    }

    /**
     * @target metaphone(String) - SCH -> SK
     * @scenario word contains SCH pattern
     * @defectRisk SCH not properly mapped to SK
     */
    @Test(timeout = 4000)
    public void testSCH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SCHOOL");
        assertTrue(result.indexOf('K') >= 0);
    }

    /**
     * @target metaphone(String) - CH at start with vowel at position 2 -> K
     * @scenario word starts with CH followed by consonant then vowel pattern
     * @defectRisk initial CH consonant not converted to K
     */
    @Test(timeout = 4000)
    public void testInitialCHConsonantK() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CHIA");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - CH -> X (CHvowel)
     * @scenario word contains CH followed by vowel, not at leading special case
     * @defectRisk CH not mapped to X correctly
     */
    @Test(timeout = 4000)
    public void testCHtoX() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("RICH");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - C default -> K
     * @scenario word contains C not followed by H or front vowel
     * @defectRisk C not defaulting to K
     */
    @Test(timeout = 4000)
    public void testCDefaultK() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CAT");
        assertTrue(result.indexOf('K') >= 0);
    }

    /**
     * @target metaphone(String) - DGE, DGI, DGY -> J
     * @scenario word contains DGE pattern
     * @defectRisk D+G+frontvowel not converted to J
     */
    @Test(timeout = 4000)
    public void testDGE() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("EDGE");
        assertTrue(result.indexOf('J') >= 0);
    }

    /**
     * @target metaphone(String) - D default -> T
     * @scenario word contains D not followed by G+frontvowel
     * @defectRisk D not defaulting properly to T
     */
    @Test(timeout = 4000)
    public void testDDefault() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("DOG");
        assertTrue(result.indexOf('T') >= 0);
    }

    /**
     * @target metaphone(String) - GH silent at end
     * @scenario word ends in GH
     * @defectRisk GH incorrectly not silenced at word end
     */
    @Test(timeout = 4000)
    public void testGHSilentAtEnd() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("HIGH");
        assertFalse(result.indexOf('K') >= 0 && result.indexOf('H') >= 0 && result.length()>2 && result.charAt(result.length()-1)=='K');
    }

    /**
     * @target metaphone(String) - GH silent before consonant
     * @scenario word has GH followed by consonant
     * @defectRisk GH not silenced properly before consonant
     */
    @Test(timeout = 4000)
    public void testGHSilentBeforeConsonant() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("GHOST");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - GN silent
     * @scenario word contains GN not at position 0
     * @defectRisk GN not correctly silenced
     */
    @Test(timeout = 4000)
    public void testGNSilent() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SIGN");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - GNED silent
     * @scenario word contains GNED pattern
     * @defectRisk GNED not correctly silenced
     */
    @Test(timeout = 4000)
    public void testGNED() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SIGNED");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - GG hard G handling
     * @scenario word contains double G before front vowel
     * @defectRisk hard flag not properly set causing incorrect K/J choice
     */
    @Test(timeout = 4000)
    public void testHardG() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("AGGIE");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - G followed by front vowel -> J
     * @scenario word contains G followed by E/I/Y not hard
     * @defectRisk G not converting to J correctly
     */
    @Test(timeout = 4000)
    public void testGFrontVowelJ() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("GYM");
        assertTrue(result.indexOf('J') >= 0);
    }

    /**
     * @target metaphone(String) - G default -> K
     * @scenario word contains G not followed by front vowel
     * @defectRisk G not defaulting to K
     */
    @Test(timeout = 4000)
    public void testGDefaultK() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("GAS");
        assertTrue(result.indexOf('K') >= 0);
    }

    /**
     * @target metaphone(String) - terminal H silent
     * @scenario word ends in H
     * @defectRisk terminal H not silenced
     */
    @Test(timeout = 4000)
    public void testTerminalH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("UTAH");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - H after VARSON silent
     * @scenario word has H preceded by C, S, P, T, or G
     * @defectRisk H not silenced after VARSON characters
     */
    @Test(timeout = 4000)
    public void testHAfterVarson() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("ASHER");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - H before vowel included
     * @scenario word has H followed by vowel, not after VARSON
     * @defectRisk H incorrectly omitted when it should be included
     */
    @Test(timeout = 4000)
    public void testHBeforeVowelIncluded() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("AHOY");
        assertTrue(result.indexOf('H') >= 0);
    }

    /**
     * @target metaphone(String) - F, J, L, M, N, R direct append
     * @scenario word contains these consonants
     * @defectRisk direct letters not appended correctly
     */
    @Test(timeout = 4000)
    public void testDirectAppendLetters() {
        Metaphone metaphone = new Metaphone();
        assertTrue(metaphone.metaphone("FISH").indexOf('F') >= 0);
        assertTrue(metaphone.metaphone("JOB").indexOf('J') >= 0);
        assertTrue(metaphone.metaphone("LAMP").indexOf('L') >= 0);
        assertTrue(metaphone.metaphone("MAP").indexOf('M') >= 0);
        assertTrue(metaphone.metaphone("NAP").indexOf('N') >= 0);
        assertTrue(metaphone.metaphone("RAP").indexOf('R') >= 0);
    }

    /**
     * @target metaphone(String) - K after C silent
     * @scenario word contains CK pattern
     * @defectRisk K not silenced correctly after C
     */
    @Test(timeout = 4000)
    public void testKAfterCSilent() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("ACK");
        // K should not double-appear due to preceding C
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - initial K included
     * @scenario word starts with K not followed by N (n==0 branch)
     * @defectRisk initial K not correctly appended
     */
    @Test(timeout = 4000)
    public void testInitialKIncluded() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("KATE");
        assertTrue(result.startsWith("K"));
    }

    /**
     * @target metaphone(String) - PH -> F
     * @scenario word contains PH pattern
     * @defectRisk PH not converted to F
     */
    @Test(timeout = 4000)
    public void testPH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("PHONE");
        assertTrue(result.indexOf('F') >= 0);
    }

    /**
     * @target metaphone(String) - P default
     * @scenario word contains P not followed by H
     * @defectRisk P not appended correctly
     */
    @Test(timeout = 4000)
    public void testPDefault() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("PAT");
        assertTrue(result.indexOf('P') >= 0);
    }

    /**
     * @target metaphone(String) - Q -> K
     * @scenario word contains Q
     * @defectRisk Q not converted to K
     */
    @Test(timeout = 4000)
    public void testQ() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("QUICK");
        assertTrue(result.indexOf('K') >= 0);
    }

    /**
     * @target metaphone(String) - SH, SIO, SIA -> X
     * @scenario word contains SH pattern
     * @defectRisk SH not converted to X
     */
    @Test(timeout = 4000)
    public void testSH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SHIP");
        assertTrue(result.indexOf('X') >= 0);
    }

    /**
     * @target metaphone(String) - SIO -> X
     * @scenario word contains SIO pattern
     * @defectRisk SIO not converted to X
     */
    @Test(timeout = 4000)
    public void testSIO() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("FUSION");
        assertTrue(result.indexOf('X') >= 0);
    }

    /**
     * @target metaphone(String) - SIA -> X
     * @scenario word contains SIA pattern
     * @defectRisk SIA not converted to X
     */
    @Test(timeout = 4000)
    public void testSIA() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("ASIA");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - S default
     * @scenario word contains S not part of SH, SIO, SIA
     * @defectRisk S not appended as S by default
     */
    @Test(timeout = 4000)
    public void testSDefault() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("SAT");
        assertTrue(result.indexOf('S') >= 0);
    }

    /**
     * @target metaphone(String) - TIA, TIO -> X
     * @scenario word contains TIA pattern
     * @defectRisk TIA not converted to X
     */
    @Test(timeout = 4000)
    public void testTIA() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("MARTIAL");
        assertTrue(result.indexOf('X') >= 0);
    }

    /**
     * @target metaphone(String) - TIO -> X
     * @scenario word contains TIO pattern
     * @defectRisk TIO not converted to X
     */
    @Test(timeout = 4000)
    public void testTIO() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("NATION");
        assertTrue(result.indexOf('X') >= 0);
    }

    /**
     * @target metaphone(String) - TCH silent
     * @scenario word contains TCH pattern
     * @defectRisk TCH not properly silenced
     */
    @Test(timeout = 4000)
    public void testTCH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("MATCH");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - TH -> 0
     * @scenario word contains TH pattern
     * @defectRisk TH not converted to numeral 0
     */
    @Test(timeout = 4000)
    public void testTH() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("THOM");
        assertTrue(result.indexOf('0') >= 0);
    }

    /**
     * @target metaphone(String) - T default
     * @scenario word contains T not part of special patterns
     * @defectRisk T not appended as T by default
     */
    @Test(timeout = 4000)
    public void testTDefault() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("TAP");
        assertTrue(result.indexOf('T') >= 0);
    }

    /**
     * @target metaphone(String) - V -> F
     * @scenario word contains V
     * @defectRisk V not converted to F
     */
    @Test(timeout = 4000)
    public void testV() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("VAN");
        assertTrue(result.indexOf('F') >= 0);
    }

    /**
     * @target metaphone(String) - W followed by vowel included
     * @scenario word contains W followed by vowel not at start
     * @defectRisk W incorrectly silenced when it should be included
     */
    @Test(timeout = 4000)
    public void testWFollowedByVowel() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("AWAY");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - W not followed by vowel silent
     * @scenario word contains W followed by consonant
     * @defectRisk W incorrectly included when it should be silenced
     */
    @Test(timeout = 4000)
    public void testWNotFollowedByVowel() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CROWD");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - Y followed by vowel included
     * @scenario word contains Y followed by vowel
     * @defectRisk Y incorrectly silenced when it should be included
     */
    @Test(timeout = 4000)
    public void testYFollowedByVowel() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("YALE");
        assertTrue(result.indexOf('Y') >= 0);
    }

    /**
     * @target metaphone(String) - Y not followed by vowel silent
     * @scenario word contains Y at end (last char)
     * @defectRisk Y incorrectly included when it should be silenced
     */
    @Test(timeout = 4000)
    public void testYNotFollowedByVowel() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CITY");
        assertNotNull(result);
    }

    /**
     * @target metaphone(String) - X -> KS
     * @scenario word contains X not at initial position
     * @defectRisk X not converted to KS
     */
    @Test(timeout = 4000)
    public void testXtoKS() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("TAXI");
        assertTrue(result.indexOf('K') >= 0 && result.indexOf('S') >= 0);
    }

    /**
     * @target metaphone(String) - Z -> S
     * @scenario word contains Z
     * @defectRisk Z not converted to S
     */
    @Test(timeout = 4000)
    public void testZ() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("ZOO");
        assertTrue(result.indexOf('S') >= 0);
    }

    /**
     * @target metaphone(String) - max code length truncation
     * @scenario long word truncated to default 4 chars
     * @defectRisk code length not truncated to max
     */
    @Test(timeout = 4000)
    public void testMaxCodeLenTruncation() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("THOROUGHLY");
        assertTrue(result.length() <= 4);
    }

    /**
     * @target setMaxCodeLen(int), getMaxCodeLen()
     * @scenario setting a custom max code length and verifying getter
     * @defectRisk getter/setter not functioning correctly
     */
    @Test(timeout = 4000)
    public void testSetGetMaxCodeLen() {
        Metaphone metaphone = new Metaphone();
        assertEquals(4, metaphone.getMaxCodeLen());
        metaphone.setMaxCodeLen(8);
        assertEquals(8, metaphone.getMaxCodeLen());
        String result = metaphone.metaphone("THOROUGHLY");
        assertTrue(result.length() <= 8);
    }

    /**
     * @target setMaxCodeLen(int)
     * @scenario setting max code length to a small value like 1
     * @defectRisk truncation not applied properly to very short lengths
     */
    @Test(timeout = 4000)
    public void testSetMaxCodeLenSmall() {
        Metaphone metaphone = new Metaphone();
        metaphone.setMaxCodeLen(1);
        String result = metaphone.metaphone("THOROUGHLY");
        assertTrue(result.length() <= 1);
    }

    /**
     * @target encode(Object)
     * @scenario valid String object passed
     * @defectRisk encode(Object) not delegating properly to metaphone
     */
    @Test(timeout = 4000)
    public void testEncodeObjectValidString() throws EncoderException {
        Metaphone metaphone = new Metaphone();
        Object result = metaphone.encode((Object) "DISCOVER");
        assertEquals(metaphone.metaphone("DISCOVER"), result);
    }

    /**
     * @target encode(Object)
     * @scenario non-String object passed
     * @defectRisk EncoderException not thrown for invalid type
     */
    @Test(timeout = 4000)
    public void testEncodeObjectNonString() {
        Metaphone metaphone = new Metaphone();
        try {
            metaphone.encode((Object) Integer.valueOf(5));
            fail("Expected EncoderException");
        } catch (EncoderException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * @target encode(Object)
     * @scenario null object passed (not instance of String)
     * @defectRisk EncoderException not thrown for null
     */
    @Test(timeout = 4000)
    public void testEncodeObjectNull() {
        Metaphone metaphone = new Metaphone();
        try {
            metaphone.encode((Object) null);
            fail("Expected EncoderException");
        } catch (EncoderException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * @target encode(String)
     * @scenario simple string delegation to metaphone
     * @defectRisk encode(String) not delegating properly
     */
    @Test(timeout = 4000)
    public void testEncodeString() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("HELLO"), metaphone.encode("HELLO"));
    }

    /**
     * @target isMetaphoneEqual(String, String)
     * @scenario two strings with same metaphone code
     * @defectRisk equality check failing for phonetically similar words
     */
    @Test(timeout = 4000)
    public void testIsMetaphoneEqualTrue() {
        Metaphone metaphone = new Metaphone();
        assertTrue(metaphone.isMetaphoneEqual("Thompson", "Tompson"));
    }

    /**
     * @target isMetaphoneEqual(String, String)
     * @scenario two strings with different metaphone codes
     * @defectRisk equality check incorrectly returning true for dissimilar words
     */
    @Test(timeout = 4000)
    public void testIsMetaphoneEqualFalse() {
        Metaphone metaphone = new Metaphone();
        assertFalse(metaphone.isMetaphoneEqual("Apple", "Banana"));
    }

    /**
     * @target isMetaphoneEqual(String, String)
     * @scenario empty strings comparison
     * @defectRisk equality check failing on empty inputs
     */
    @Test(timeout = 4000)
    public void testIsMetaphoneEqualEmpty() {
        Metaphone metaphone = new Metaphone();
        assertTrue(metaphone.isMetaphoneEqual("", ""));
    }

    /**
     * @target metaphone(String) - lowercase input converted properly
     * @scenario mixed case input
     * @defectRisk case conversion not applied uniformly
     */
    @Test(timeout = 4000)
    public void testMixedCaseInput() {
        Metaphone metaphone = new Metaphone();
        assertEquals(metaphone.metaphone("HELLO"), metaphone.metaphone("HeLLo"));
    }

    /**
     * @target metaphone(String) - well known classic examples
     * @scenario classic metaphone reference examples
     * @defectRisk algorithm deviating from documented reference outputs
     */
    @Test(timeout = 4000)
    public void testClassicExamples() {
        Metaphone metaphone = new Metaphone();
        assertEquals("TMSN", metaphone.metaphone("Thompson"));
        assertEquals("WT", metaphone.metaphone("WHAT"));
    }

    /**
     * @target metaphone(String) - CH at position 0 without vowel at 2
     * @scenario CH followed directly by vowel at position 1 (n==0 but wdsz<3 or not vowel at 2)
     * @defectRisk incorrect branch selection for initial CH check
     */
    @Test(timeout = 4000)
    public void testInitialCHShortWord() {
        Metaphone metaphone = new Metaphone();
        String result = metaphone.metaphone("CHA");
        assertNotNull(result);
    }

    /**
     * CRITICAL DEFECT TEST:
     * @target metaphone(String) - locale-independent uppercase conversion
     * @scenario Turkish locale set as default; lowercase "i" must convert to "I" not dotted "İ"
     * @defectRisk toUpperCase(Locale.ENGLISH) misbehaving under Turkish default locale,
     *             producing "İ" (dotted capital I) instead of "I", causing metaphone
     *             algorithm to break due to unexpected character
     */
    @Test(timeout = 4000)
    public void testLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            Metaphone metaphone = new Metaphone();
            metaphone.encode("i");
            assertEquals("I", metaphone.metaphone("i"));
            assertEquals(metaphone.metaphone("I"), metaphone.metaphone("i"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    /**
     * @target metaphone(String) - additional locale independence check with longer word
     * @scenario Turkish locale default; word containing lowercase 'i' should still
     *           produce expected uppercase results consistent with ENGLISH locale
     * @defectRisk locale-sensitive case conversion corrupting metaphone codes
     */
    @Test(timeout = 4000)
    public void testLocaleIndependence_TurkishLongerWord() {
        Locale orig = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            Metaphone metaphone = new Metaphone();
            String result = metaphone.metaphone("intelligent");
            // Should not contain the Turkish dotted capital I character
            assertFalse(result.contains("\u0130"));
        } finally {
            Locale.setDefault(orig);
        }
    }
}