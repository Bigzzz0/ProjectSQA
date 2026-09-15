package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.codec.language.Caverphone (Caverphone 2.0 implementation)
 * Primary Defect: CODEC-1 / Locale Dependency in toLowerCase() without explicit Locale.ENGLISH.
 *                 In Turkish locale ("tr"), uppercase 'I' converts to dotless 'ı' (\u0131), which fails
 *                 matching regex [^a-z] and is stripped, causing empty string -> "1111111111" instead of "A111111111".
 *
 * Decision / Branch Coverage Target Points:
 * 1. caverphone(String):
 *    - txt == null branch -> returns "1111111111"
 *    - txt.length() == 0 branch -> returns "1111111111"
 *    - txt containing non-alpha characters (stripped via [^a-z])
 *    - txt ending with 'e' (stripped via e$)
 *    - Prefixes: ^cough, ^rough, ^tough, ^enough, ^trough, ^gn, ^mb
 *    - Intermediate digraphs: cq, ci, ce, cy, tch, c, q, x, v, dg, tio, tia, d, ph, b, sh, z
 *    - Initial and internal vowels: ^[aeiou] -> A, [aeiou] -> 3
 *    - 'j' and 'y' rules: j -> y, ^y3 -> Y3, ^y -> A, y -> 3
 *    - 'gh' and 'g' rules: 3gh3 -> 3kh3, gh -> 22, g -> k
 *    - Consonant deduplication & capitalization: s+, t+, p+, k+, f+, m+, n+
 *    - 'w' rules: w3 -> W3, wh3 -> Wh3, w$ -> 3, w -> 2
 *    - 'h' rules: ^h -> A, h -> 2
 *    - 'r' rules: r3 -> R3, r$ -> 3, r -> 2
 *    - 'l' rules: l3 -> L3, l$ -> 3, l -> 2
 *    - Removal / end-vowel transformations: 2 -> "", 3$ -> A, 3 -> ""
 *    - Post-padding with 1s and truncation to exactly 10 characters.
 *
 * 2. encode(Object):
 *    - !(pObject instanceof String) branch -> throws EncoderException
 *    - pObject instanceof String branch -> returns caverphone((String) pObject)
 *
 * 3. encode(String):
 *    - delegates to caverphone(String)
 *
 * 4. isCaverphoneEqual(String, String):
 *    - true when both produce identical 10-char caverphone representation
 *    - false when caverphone codes differ
 * ====================================================================================================
 */
public class CaverphoneGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & Phonetic Rules
    // ================================================================================================

    @Test(timeout = 4000)
    public void testPrefixTransformations() {
        Caverphone caverphone = new Caverphone();

        // ^cough -> cou2f
        assertEquals(caverphone.caverphone("cough"), caverphone.caverphone("cou2f"));
        // ^rough -> rou2f
        assertEquals(caverphone.caverphone("rough"), caverphone.caverphone("rou2f"));
        // ^tough -> tou2f
        assertEquals(caverphone.caverphone("tough"), caverphone.caverphone("tou2f"));
        // ^enough -> enou2f
        assertEquals(caverphone.caverphone("enough"), caverphone.caverphone("enou2f"));
        // ^trough -> trou2f
        assertEquals(caverphone.caverphone("trough"), caverphone.caverphone("trou2f"));
        // ^gn -> 2n
        assertEquals(caverphone.caverphone("gnat"), caverphone.caverphone("nat"));
        // ^mb -> m2
        assertEquals(caverphone.caverphone("mbappe"), caverphone.caverphone("mappe"));
    }

    @Test(timeout = 4000)
    public void testDigraphAndConsonantReplacements() {
        Caverphone caverphone = new Caverphone();

        // cq -> 2q, ci -> si, ce -> se, cy -> sy
        assertEquals(caverphone.caverphone("acquit"), caverphone.caverphone("akwit"));
        assertEquals(caverphone.caverphone("city"), caverphone.caverphone("sity"));
        assertEquals(caverphone.caverphone("center"), caverphone.caverphone("senter"));
        assertEquals(caverphone.caverphone("cycle"), caverphone.caverphone("sycle"));

        // tch -> 2ch
        assertEquals(caverphone.caverphone("catch"), caverphone.caverphone("kach"));

        // c, q, x -> k
        assertEquals(caverphone.caverphone("quick"), caverphone.caverphone("kwik"));
        assertEquals(caverphone.caverphone("box"), caverphone.caverphone("boks"));

        // v -> f
        assertEquals(caverphone.caverphone("vat"), caverphone.caverphone("fat"));

        // dg -> 2g
        assertEquals(caverphone.caverphone("edge"), caverphone.caverphone("ege"));

        // tio -> sio, tia -> sia
        assertEquals(caverphone.caverphone("action"), caverphone.caverphone("acsion"));
        assertEquals(caverphone.caverphone("spatial"), caverphone.caverphone("spasial"));

        // d -> t, ph -> fh, b -> p, sh -> s2, z -> s
        assertEquals(caverphone.caverphone("door"), caverphone.caverphone("toor"));
        assertEquals(caverphone.caverphone("phone"), caverphone.caverphone("fhone"));
        assertEquals(caverphone.caverphone("bat"), caverphone.caverphone("pat"));
        assertEquals(caverphone.caverphone("shoe"), caverphone.caverphone("s2oe"));
        assertEquals(caverphone.caverphone("zero"), caverphone.caverphone("sero"));
    }

    @Test(timeout = 4000)
    public void testVowelAndYReplacements() {
        Caverphone caverphone = new Caverphone();

        // ^[aeiou] -> A, [aeiou] -> 3
        assertEquals("A111111111", caverphone.caverphone("a"));
        assertEquals("A111111111", caverphone.caverphone("e"));
        assertEquals("A111111111", caverphone.caverphone("i"));
        assertEquals("A111111111", caverphone.caverphone("o"));
        assertEquals("A111111111", caverphone.caverphone("u"));

        // j -> y, ^y3 -> Y3, ^y -> A, y -> 3
        assertEquals(caverphone.caverphone("jump"), caverphone.caverphone("yump"));
        assertEquals("YA11111111", caverphone.caverphone("yellow")); // ^y + e(3) -> Y3
        assertEquals("A111111111", caverphone.caverphone("y"));      // ^y -> A
    }

    @Test(timeout = 4000)
    public void testGhAndGReplacements() {
        Caverphone caverphone = new Caverphone();

        // 3gh3 -> 3kh3 (e.g. night: n + i(3) + gh + t) -> gh between vowels
        String light = caverphone.caverphone("light");
        assertNotNull(light);
        assertEquals(10, light.length());

        // gh standalone -> 22
        assertEquals(caverphone.caverphone("ghost"), caverphone.caverphone("khost"));
    }

    @Test(timeout = 4000)
    public void testConsonantRepetitions() {
        Caverphone caverphone = new Caverphone();

        // Multiple consecutive letters reduced: s+, t+, p+, k+, f+, m+, n+
        assertEquals(caverphone.caverphone("kiss"), caverphone.caverphone("kis"));
        assertEquals(caverphone.caverphone("matter"), caverphone.caverphone("mater"));
        assertEquals(caverphone.caverphone("apple"), caverphone.caverphone("aple"));
        assertEquals(caverphone.caverphone("flakk"), caverphone.caverphone("flak"));
        assertEquals(caverphone.caverphone("staff"), caverphone.caverphone("staf"));
        assertEquals(caverphone.caverphone("summer"), caverphone.caverphone("sumer"));
        assertEquals(caverphone.caverphone("dinner"), caverphone.caverphone("diner"));
    }

    @Test(timeout = 4000)
    public void testWHLRReplacements() {
        Caverphone caverphone = new Caverphone();

        // w3, wh3, w$, w
        assertEquals("WA11111111", caverphone.caverphone("we"));   // w + e(3) -> W3 -> WA
        assertEquals("WA11111111", caverphone.caverphone("whe"));  // wh + e(3) -> Wh3 -> WA
        assertEquals("SA11111111", caverphone.caverphone("saw"));  // s + a(3) + w$ -> w$ becomes 3, 3$ becomes A
        assertEquals("STA1111111", caverphone.caverphone("straw"));

        // ^h -> A, h -> 2
        assertEquals("A111111111", caverphone.caverphone("h"));
        assertEquals("AT11111111", caverphone.caverphone("hit"));
        assertEquals(caverphone.caverphone("that"), caverphone.caverphone("tat"));

        // r3, r$, r
        assertEquals("RA11111111", caverphone.caverphone("re"));   // r + e(3) -> R3 -> RA
        assertEquals("KRA1111111", caverphone.caverphone("car"));  // c -> k, a -> 3, r$ -> 3, 3$ -> A

        // l3, l$, l
        assertEquals("LA11111111", caverphone.caverphone("lee"));
        assertEquals("KLA1111111", caverphone.caverphone("call"));
    }

    @Test(timeout = 4000)
    public void testKnownCanonicalWords() {
        Caverphone caverphone = new Caverphone();

        // Standard pairs that should match phonetically in Caverphone 2.0
        assertTrue(caverphone.isCaverphoneEqual("Lee", "Leigh"));
        assertTrue(caverphone.isCaverphoneEqual("Smith", "Smyth"));
        assertTrue(caverphone.isCaverphoneEqual("Thompson", "Thomson"));
        assertTrue(caverphone.isCaverphoneEqual("Stephen", "Steven"));
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testNullAndEmptyInputs() {
        Caverphone caverphone = new Caverphone();

        assertEquals("1111111111", caverphone.caverphone(null));
        assertEquals("1111111111", caverphone.caverphone(""));
        assertEquals("1111111111", caverphone.encode((String) null));
        assertEquals("1111111111", caverphone.encode(""));
    }

    @Test(timeout = 4000)
    public void testNonAlphaFiltering() {
        Caverphone caverphone = new Caverphone();

        // Strings containing digits, punctuation, whitespace
        assertEquals("1111111111", caverphone.caverphone("12345"));
        assertEquals("1111111111", caverphone.caverphone("   "));
        assertEquals("1111111111", caverphone.caverphone("!@#$%^&*()_+"));
        // "Peter 123" should equal "Peter"
        assertEquals(caverphone.caverphone("Peter"), caverphone.caverphone("Peter 123"));
        assertEquals(caverphone.caverphone("John-Paul"), caverphone.caverphone("JohnPaul"));
    }

    @Test(timeout = 4000)
    public void testSingleCharacterInputs() {
        Caverphone caverphone = new Caverphone();

        // Single vowels -> 'A' followed by nine 1s
        assertEquals("A111111111", caverphone.caverphone("a"));
        assertEquals("A111111111", caverphone.caverphone("o"));
        // Single consonants
        assertEquals("T111111111", caverphone.caverphone("t"));
        assertEquals("P111111111", caverphone.caverphone("p"));
        assertEquals("K111111111", caverphone.caverphone("k"));
        assertEquals("S111111111", caverphone.caverphone("s"));
        assertEquals("M111111111", caverphone.caverphone("m"));
        assertEquals("N111111111", caverphone.caverphone("n"));
        assertEquals("F111111111", caverphone.caverphone("f"));
    }

    @Test(timeout = 4000)
    public void testOutputLengthAndPaddingInvariant() {
        Caverphone caverphone = new Caverphone();

        String[] testWords = {
            "", "a", "ab", "abc", "abcdef", "supercalifragilisticexpialidocious",
            "pneumonoultramicroscopicsilicovolcanoconiosis", "1234", "!?"
        };

        for (String word : testWords) {
            String encoded = caverphone.caverphone(word);
            assertNotNull("Encoded value must never be null", encoded);
            assertEquals("Encoded value must always be exactly 10 characters long: " + word, 10, encoded.length());
        }
    }

    @Test(timeout = 4000)
    public void testFinalEHandling() {
        Caverphone caverphone = new Caverphone();

        // "e" by itself -> removes final e -> empty -> "1111111111"
        assertEquals("1111111111", caverphone.caverphone("e"));
        // Final e removed on words
        assertEquals(caverphone.caverphone("nam"), caverphone.caverphone("name"));
        assertEquals(caverphone.caverphone("bak"), caverphone.caverphone("bake"));
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Codec-1 / Locale Independence)
    // ================================================================================================

    /**
     * Target Defect: CODEC-1 / Caverphone.caverphone uses txt.toLowerCase() without Locale.ENGLISH.
     * In Turkish ("tr"), "I".toLowerCase() becomes the dotless small letter 'ı' (U+0131).
     * Since 'ı' is not in the ASCII range [a-z], txt.replaceAll("[^a-z]", "") strips it,
     * reducing the string to empty and returning "1111111111" instead of "A111111111".
     */
    @Test(timeout = 4000)
    public void testLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            Caverphone caverphone = new Caverphone();

            // Under Turkish locale, uppercase "I" must encode identically to lowercase "i" -> "A111111111"
            assertEquals("A111111111", caverphone.caverphone("I"));
            assertEquals(caverphone.caverphone("I"), caverphone.caverphone("i"));

            // Other words with uppercase I
            assertEquals(caverphone.caverphone("island"), caverphone.caverphone("ISLAND"));
            assertEquals(caverphone.caverphone("inside"), caverphone.caverphone("INSIDE"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    @Test(timeout = 4000)
    public void testLocaleIndependence_Azerbaijani() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("az"));
            Caverphone caverphone = new Caverphone();

            assertEquals("A111111111", caverphone.caverphone("I"));
            assertEquals(caverphone.caverphone("I"), caverphone.caverphone("i"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEncodeWithValidStringObject() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        Object result = caverphone.encode((Object) "Stevenson");
        assertTrue("Result must be instance of String", result instanceof String);
        assertEquals(caverphone.caverphone("Stevenson"), result);
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithIntegerThrowsEncoderException() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        caverphone.encode(Integer.valueOf(12345));
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithDoubleThrowsEncoderException() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        caverphone.encode(Double.valueOf(3.14159));
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithNullObjectThrowsEncoderException() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        caverphone.encode((Object) null);
    }

    @Test(expected = EncoderException.class, timeout = 4000)
    public void testEncodeWithStringBufferThrowsEncoderException() throws EncoderException {
        Caverphone caverphone = new Caverphone();
        caverphone.encode(new StringBuffer("Test"));
    }

    // ================================================================================================
    // Partition E: Interface & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEncodeStringDelegation() {
        Caverphone caverphone = new Caverphone();
        assertEquals(caverphone.caverphone("Dwayne"), caverphone.encode("Dwayne"));
        assertEquals(caverphone.caverphone(""), caverphone.encode(""));
        assertEquals(caverphone.caverphone(null), caverphone.encode((String) null));
    }

    @Test(timeout = 4000)
    public void testIsCaverphoneEqualSymmetryAndLogic() {
        Caverphone caverphone = new Caverphone();

        // Symmetrical equality
        assertTrue(caverphone.isCaverphoneEqual("Peter", "peter"));
        assertTrue(caverphone.isCaverphoneEqual("peter", "Peter"));

        // Transitive equality
        assertTrue(caverphone.isCaverphoneEqual("Smith", "Smyth"));
        assertTrue(caverphone.isCaverphoneEqual("Smyth", "Schmidt"));
        assertTrue(caverphone.isCaverphoneEqual("Smith", "Schmidt"));

        // Inequality
        assertFalse(caverphone.isCaverphoneEqual("Peter", "Mary"));
        assertFalse(caverphone.isCaverphoneEqual("dog", "cat"));

        // Both empty or both null
        assertTrue(caverphone.isCaverphoneEqual(null, ""));
        assertTrue(caverphone.isCaverphoneEqual(null, null));
        assertTrue(caverphone.isCaverphoneEqual("", ""));
    }
}