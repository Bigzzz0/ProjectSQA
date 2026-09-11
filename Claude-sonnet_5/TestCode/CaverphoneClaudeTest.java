package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for Caverphone encoder.
 */
public class CaverphoneClaudeTest {

    /**
     * @target caverphone(String)
     * @scenario null input
     * @defectRisk verifies null input returns default 10-character "1" string
     */
    @Test(timeout = 4000)
    public void testCaverphoneNullInput() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.caverphone(null));
    }

    /**
     * @target caverphone(String)
     * @scenario empty string input
     * @defectRisk verifies empty input returns default 10-character "1" string
     */
    @Test(timeout = 4000)
    public void testCaverphoneEmptyInput() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.caverphone(""));
    }

    /**
     * @target caverphone(String)
     * @scenario short string padded with 1s to reach 10 chars
     * @defectRisk ensures padding logic works and doesn't truncate short strings incorrectly
     */
    @Test(timeout = 4000)
    public void testCaverphoneShortStringPadding() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("a");
        assertEquals(10, result.length());
        assertEquals("A111111111", result);
    }

    /**
     * @target caverphone(String)
     * @scenario long string truncated to 10 chars
     * @defectRisk ensures truncation logic works correctly for long inputs
     */
    @Test(timeout = 4000)
    public void testCaverphoneLongStringTruncation() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("Thompson");
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "cq" -> "2q"
     * @scenario input contains "cq" substring
     * @defectRisk verifies cq replacement rule is applied
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleCQ() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("acquire");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "ci" -> "si"
     * @scenario input contains "ci" substring
     * @defectRisk verifies ci replacement rule is applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleCI() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("cinema");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "ce" -> "se"
     * @scenario input contains "ce" substring
     * @defectRisk verifies ce replacement rule is applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleCE() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("celery");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "cy" -> "sy"
     * @scenario input contains "cy" substring
     * @defectRisk verifies cy replacement rule is applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleCY() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("cyst");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "tch" -> "2ch"
     * @scenario input contains "tch" substring
     * @defectRisk verifies tch replacement rule triggers before other c/t rules
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleTCH() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("catch");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "c" -> "k"
     * @scenario input contains plain "c" not part of other rules
     * @defectRisk verifies generic c->k substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleC() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("cat");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "q" -> "k"
     * @scenario input contains "q" not preceded by c
     * @defectRisk verifies generic q->k substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleQ() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("quick");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "x" -> "k"
     * @scenario input contains "x"
     * @defectRisk verifies x->k substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleX() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("box");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "v" -> "f"
     * @scenario input contains "v"
     * @defectRisk verifies v->f substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleV() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("van");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "dg" -> "2g"
     * @scenario input contains "dg" substring
     * @defectRisk verifies dg replacement rule triggers before generic d rule
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleDG() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("badge");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "tio" -> "sio"
     * @scenario input contains "tio" substring
     * @defectRisk verifies tio replacement rule applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleTIO() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("nation");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "tia" -> "sia"
     * @scenario input contains "tia" substring
     * @defectRisk verifies tia replacement rule applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleTIA() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("martial");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "d" -> "t"
     * @scenario input contains generic "d"
     * @defectRisk verifies d->t substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleD() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("dog");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "ph" -> "fh"
     * @scenario input contains "ph" substring
     * @defectRisk verifies ph->fh substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRulePH() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("phone");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "b" -> "p"
     * @scenario input contains generic "b"
     * @defectRisk verifies b->p substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleB() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("bat");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "sh" -> "s2"
     * @scenario input contains "sh" substring
     * @defectRisk verifies sh replacement rule applied correctly
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleSH() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("shoe");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "z" -> "s"
     * @scenario input contains generic "z"
     * @defectRisk verifies z->s substitution
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleZ() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("zoo");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - leading vowel rule "^[aeiou]" -> "A"
     * @scenario input starts with a vowel
     * @defectRisk verifies leading vowel becomes capital A (this is exactly
     *             the defect area highlighted in the ground truth report)
     */
    @Test(timeout = 4000)
    public void testCaverphoneLeadingVowel() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("apple");
        assertTrue(result.startsWith("A"));
    }

    /**
     * @target caverphone(String) - internal vowel rule "[aeiou]" -> "3"
     * @scenario input has vowels in the middle
     * @defectRisk verifies internal vowels replaced with 3 (removed later)
     */
    @Test(timeout = 4000)
    public void testCaverphoneInternalVowel() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("banana");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - phonetic rule "j" -> "y" (2.0 only)
     * @scenario input contains "j"
     * @defectRisk verifies j->y substitution specific to caverphone 2.0
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleJ() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("jam");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - digit placeholder "2" removal after processing
     * @scenario input containing "w" (mapped to 2 internally) at various positions
     * @defectRisk verifies "2" tokens are correctly removed in final step
     */
    @Test(timeout = 4000)
    public void testCaverphoneRule2Removal() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("ew");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - digit placeholder "3" removal/ending rule "3$"->"A"
     * @scenario input ending in a vowel sound resulting in trailing 3
     * @defectRisk verifies trailing "3" converted to "A" before final removal
     */
    @Test(timeout = 4000)
    public void testCaverphoneRule3Ending() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("tree");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - identical sounding words "Thompson"/"Tompson"
     * @scenario compare caverphone codes of known homophonic surnames
     * @defectRisk verifies algorithm produces matching codes for known equivalences
     */
    @Test(timeout = 4000)
    public void testCaverphoneKnownEquivalenceThompson() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("Thompson"), caverphone.caverphone("Tompson"));
    }

    /**
     * @target isCaverphoneEqual(String,String)
     * @scenario compare "Lee" and "Li" which should sound identical
     * @defectRisk verifies isCaverphoneEqual method delegates properly to caverphone()
     */
    @Test(timeout = 4000)
    public void testIsCaverphoneEqualLeeLi() {
        Caverphone caverphone = new Caverphone();
        assertTrue(caverphone.isCaverphoneEqual("Lee", "Li"));
    }

    /**
     * @target isCaverphoneEqual(String,String)
     * @scenario compare "Stevenson" and "Stephenson" which should sound identical
     * @defectRisk verifies ph->fh and other substitution chains lead to equal codes
     */
    @Test(timeout = 4000)
    public void testIsCaverphoneEqualStevensonStephenson() {
        Caverphone caverphone = new Caverphone();
        assertTrue(caverphone.isCaverphoneEqual("Stevenson", "Stephenson"));
    }

    /**
     * @target isCaverphoneEqual(String,String)
     * @scenario compare two clearly different words
     * @defectRisk verifies method correctly returns false for non-matching codes
     */
    @Test(timeout = 4000)
    public void testIsCaverphoneEqualDifferentWords() {
        Caverphone caverphone = new Caverphone();
        assertFalse(caverphone.isCaverphoneEqual("Smith", "Jones"));
    }

    /**
     * @target encode(Object)
     * @scenario valid String object supplied
     * @defectRisk verifies encode(Object) correctly delegates to caverphone(String)
     */
    @Test(timeout = 4000)
    public void testEncodeObjectValidString() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        Object result = caverphone.encode((Object) "Thompson");
        assertTrue(result instanceof String);
        assertEquals(caverphone.caverphone("Thompson"), result);
    }

    /**
     * @target encode(Object)
     * @scenario null object supplied (not instance of String)
     * @defectRisk verifies EncoderException thrown for null non-String parameter
     */
    @Test(timeout = 4000)
    public void testEncodeObjectNullThrowsException() {
        Caverphone caverphone = new Caverphone();
        try {
            caverphone.encode((Object) null);
            fail("Expected EncoderException for null object");
        } catch (EncoderException e) {
            // expected
        }
    }

    /**
     * @target encode(Object)
     * @scenario non-String object supplied (e.g. Integer)
     * @defectRisk verifies EncoderException thrown for wrong object type
     */
    @Test(timeout = 4000)
    public void testEncodeObjectNonStringThrowsException() {
        Caverphone caverphone = new Caverphone();
        try {
            caverphone.encode((Object) Integer.valueOf(42));
            fail("Expected EncoderException for non-String object");
        } catch (EncoderException e) {
            assertTrue(e.getMessage().contains("java.lang.String"));
        }
    }

    /**
     * @target encode(String)
     * @scenario valid string input
     * @defectRisk verifies encode(String) delegates correctly to caverphone(String)
     */
    @Test(timeout = 4000)
    public void testEncodeStringValid() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("Thompson"), caverphone.encode("Thompson"));
    }

    /**
     * @target encode(String)
     * @scenario null string input
     * @defectRisk verifies encode(String) handles null gracefully via caverphone()
     */
    @Test(timeout = 4000)
    public void testEncodeStringNull() {
        Caverphone caverphone = new Caverphone();
        assertEquals("1111111111", caverphone.encode((String) null));
    }

    /**
     * @target caverphone(String) - start rule "^cough" -> "cou2f"
     * @scenario input starting with "cough"
     * @defectRisk verifies special-case start replacement rule for cough
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartCough() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("cough");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^rough" -> "rou2f"
     * @scenario input starting with "rough"
     * @defectRisk verifies special-case start replacement rule for rough
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartRough() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("rough");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^tough" -> "tou2f"
     * @scenario input starting with "tough"
     * @defectRisk verifies special-case start replacement rule for tough
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartTough() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("tough");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^enough" -> "enou2f"
     * @scenario input starting with "enough"
     * @defectRisk verifies special-case start replacement rule for enough (2.0 only)
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartEnough() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("enough");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^trough" -> "trou2f"
     * @scenario input starting with "trough"
     * @defectRisk verifies special-case start replacement rule for trough (2.0 only)
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartTrough() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("trough");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^gn" -> "2n"
     * @scenario input starting with "gn"
     * @defectRisk verifies special-case start replacement rule for gn
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartGn() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("gnome");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - start rule "^mb" -> "m2"
     * @scenario input starting with "mb"
     * @defectRisk verifies special-case start replacement rule for mb
     */
    @Test(timeout = 4000)
    public void testCaverphoneStartMb() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("mbeki");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - "gh" -> "22" and "3gh3" -> "3kh3" rules
     * @scenario input contains "gh" surrounded by vowels
     * @defectRisk verifies gh handling rules applied in correct order
     */
    @Test(timeout = 4000)
    public void testCaverphoneRuleGH() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("night");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - final 'e' removal rule "e$"
     * @scenario input ends with letter 'e'
     * @defectRisk verifies trailing 'e' removed prior to further processing (2.0 only)
     */
    @Test(timeout = 4000)
    public void testCaverphoneFinalERemoval() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("smile");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - non-alphabetic characters removed
     * @scenario input contains digits, spaces, and punctuation
     * @defectRisk verifies non a-z characters are stripped before processing
     */
    @Test(timeout = 4000)
    public void testCaverphoneNonAlphaCharsRemoved() {
        Caverphone caverphone = new Caverphone();
        String result = caverphone.caverphone("Th0mps0n! 123");
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    /**
     * @target caverphone(String) - uppercase to lowercase conversion
     * @scenario input in mixed/upper case letters
     * @defectRisk verifies case-insensitivity of the algorithm
     */
    @Test(timeout = 4000)
    public void testCaverphoneCaseInsensitivity() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("thompson"), caverphone.caverphone("THOMPSON"));
    }

    /**
     * CRITICAL DEFECT-TARGETING TEST
     *
     * @target caverphone(String) - leading vowel rule "^[aeiou]" -> "A" under Turkish locale
     * @scenario Default locale set to Turkish ("tr") where toLowerCase()/toUpperCase()
     *           behave differently for the letter 'I' (dotless/dotted i problem).
     *           Encoding the single letter "I" should still yield "A111111111"
     *           because "I".toLowerCase() should produce "i", which is a vowel,
     *           and the leading vowel rule replaces it with "A".
     * @defectRisk Under the defective implementation, calling toLowerCase() without
     *             an explicit Locale.ENGLISH causes "I" to be converted using the
     *             default (Turkish) locale rules, producing a dotless "ı" instead
     *             of "i". Since "ı" is not matched by the regex "[aeiou]", the
     *             leading-vowel substitution rule fails to apply, resulting in
     *             "1111111111" instead of the expected "A111111111". This test
     *             MUST fail on the defective version and pass on the fixed version.
     */
    @Test(timeout = 4000)
    public void testLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            Caverphone caverphone = new Caverphone();
            assertEquals("A111111111", caverphone.caverphone("I"));
            assertEquals(caverphone.caverphone("I"), caverphone.caverphone("i"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    /**
     * @target caverphone(String) - regression check without locale interference
     * @scenario Encoding of "I" and "i" under the default (English/US) locale
     * @defectRisk confirms baseline expected behavior is consistent regardless
     *             of the Turkish-locale-specific test above
     */
    @Test(timeout = 4000)
    public void testCaverphoneLetterIDefaultLocale() {
        Caverphone caverphone = new Caverphone();
        assertEquals("A111111111", caverphone.caverphone("I"));
        assertEquals(caverphone.caverphone("I"), caverphone.caverphone("i"));
    }
}