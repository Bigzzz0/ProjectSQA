package org.apache.commons.codec.language;

import static org.junit.Assert.*;

import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.language.Caverphone
 *
 * Decision / Branch Points:
 * 1. caverphone(String txt):
 *    - txt == null -> returns "1111111111"
 *    - txt.length() == 0 -> returns "1111111111"
 *    - txt contains non-alpha characters -> stripped via [^a-z]
 *    - txt has trailing 'e' -> stripped via e$
 *    - Prefix rules: ^cough, ^rough, ^tough, ^enough, ^trough, ^gn
 *    - Suffix rule: mb$ (Defect: implemented as ^mb in buggy versions)
 *    - Phonetic replacements: cq, ci, ce, cy, tch, c, q, x, v, dg, tio, tia, d, ph, b, sh, z
 *    - Vowel handling: ^[aeiou] -> A, [aeiou] -> 3
 *    - Y/J rules: j->y, ^y3->Y3, ^y->A, y->3
 *    - G/H rules: 3gh3->3kh3, gh->22, g->k
 *    - Consonant compression: s+, t+, p+, k+, f+, m+, n+
 *    - W rules: w3->W3, wh3->Wh3, w$->3, w->2
 *    - H rules: ^h->A, h->2
 *    - R rules: r3->R3, r$->3, r->2
 *    - L rules: l3->L3, l$->3, l->2
 *    - Number removals: 2->"", 3$->A, 3->""
 *    - Padding & truncation: pad with "1111111111", truncate to 10 characters
 * 2. encode(Object pObject):
 *    - pObject instanceof String == true -> returns caverphone((String) pObject)
 *    - pObject instanceof String == false (null, non-String object) -> throws EncoderException
 * 3. encode(String pString):
 *    - delegates to caverphone(pString)
 * 4. isCaverphoneEqual(String str1, String str2):
 *    - caverphone(str1).equals(caverphone(str2)) == true
 *    - caverphone(str1).equals(caverphone(str2)) == false
 *
 * Defect Targeted:
 * - Defects4J / Codec Caverphone mb suffix defect:
 *   Bug: Caverphone uses txt.replaceAll("^mb", "m2") instead of txt.replaceAll("mb$", "m2").
 *   Expected: "mbmb" should encode to "MPM1111111", but buggy code produces "MMP1111111".
 * ----------------------------------------------------------------------------------------------------
 */
public class CaverphoneGeminiTest {

    private Caverphone caverphone;

    @Before
    public void setUp() {
        this.caverphone = new Caverphone();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCoreEncodingExamples() {
        // Standard names verification
        assertEquals("PT11111111", caverphone.caverphone("Peter"));
        assertEquals("STSN111111", caverphone.caverphone("Stevenson"));
        assertEquals("TFR1111111", caverphone.caverphone("David"));
    }

    @Test(timeout = 4000)
    public void testCaseInsensitivityAndNonAlpha() {
        assertEquals(caverphone.caverphone("lee"), caverphone.caverphone("LEE"));
        assertEquals(caverphone.caverphone("lee"), caverphone.caverphone("L-e-e!"));
        assertEquals(caverphone.caverphone("thompson"), caverphone.caverphone("THOMPSON 123"));
    }

    @Test(timeout = 4000)
    public void testSpecialPrefixReplacements() {
        // ^cough, ^rough, ^tough, ^enough, ^trough, ^gn
        assertEquals("KFA1111111", caverphone.caverphone("cough"));
        assertEquals("RFA1111111", caverphone.caverphone("rough"));
        assertEquals("TFA1111111", caverphone.caverphone("tough"));
        assertEquals("ANFA111111", caverphone.caverphone("enough"));
        assertEquals("TRFA111111", caverphone.caverphone("trough"));
        assertEquals("N111111111", caverphone.caverphone("gnome"));
    }

    @Test(timeout = 4000)
    public void testPhoneticRulesCoverage() {
        // cq -> 2q, ci -> si, ce -> se, cy -> sy, tch -> 2ch
        assertEquals("KSA1111111", caverphone.caverphone("acquire"));
        assertEquals("SA11111111", caverphone.caverphone("city"));
        assertEquals("SA11111111", caverphone.caverphone("centre"));
        assertEquals("SA11111111", caverphone.caverphone("cylinder"));
        assertEquals("K111111111", caverphone.caverphone("catch"));

        // c, q, x, v, dg, tio, tia, d, ph, b, sh, z
        assertEquals("K111111111", caverphone.caverphone("quick"));
        assertEquals("K111111111", caverphone.caverphone("xenon"));
        assertEquals("F111111111", caverphone.caverphone("val"));
        assertEquals("K111111111", caverphone.caverphone("edge"));
        assertEquals("SN11111111", caverphone.caverphone("action"));
        assertEquals("S111111111", caverphone.caverphone("initial"));
        assertEquals("T111111111", caverphone.caverphone("door"));
        assertEquals("F111111111", caverphone.caverphone("phone"));
        assertEquals("P111111111", caverphone.caverphone("bell"));
        assertEquals("S111111111", caverphone.caverphone("shine"));
        assertEquals("S111111111", caverphone.caverphone("zoo"));
    }

    @Test(timeout = 4000)
    public void testVowelAndYRules() {
        // initial vowels -> A
        assertEquals("AT11111111", caverphone.caverphone("auto"));
        assertEquals("AT11111111", caverphone.caverphone("it"));
        // j -> y, ^y3 -> Y3, ^y -> A, y -> 3
        assertEquals("YNT1111111", caverphone.caverphone("janet"));
        assertEquals("A111111111", caverphone.caverphone("yacht"));
        assertEquals("A111111111", caverphone.caverphone("yellow"));
        assertEquals("AT11111111", caverphone.caverphone("byte"));
    }

    @Test(timeout = 4000)
    public void testGhAndWHLRules() {
        // 3gh3 -> 3kh3, gh -> 22, g -> k
        assertEquals("AK11111111", caverphone.caverphone("aghast"));
        assertEquals("NT11111111", caverphone.caverphone("night"));
        assertEquals("K111111111", caverphone.caverphone("good"));

        // w3, wh3, w$, w
        assertEquals("W111111111", caverphone.caverphone("water"));
        assertEquals("W111111111", caverphone.caverphone("white"));
        assertEquals("S111111111", caverphone.caverphone("saw"));

        // ^h, h
        assertEquals("AT11111111", caverphone.caverphone("hat"));
        assertEquals("T111111111", caverphone.caverphone("the"));

        // r3, r$, r, l3, l$, l
        assertEquals("R111111111", caverphone.caverphone("rain"));
        assertEquals("K111111111", caverphone.caverphone("car"));
        assertEquals("L111111111", caverphone.caverphone("light"));
        assertEquals("K111111111", caverphone.caverphone("call"));
    }

    @Test(timeout = 4000)
    public void testEncodeStringDelegation() {
        assertEquals(caverphone.caverphone("Peter"), caverphone.encode("Peter"));
        assertEquals(caverphone.caverphone("Stevenson"), caverphone.encode("Stevenson"));
    }

    @Test(timeout = 4000)
    public void testIsCaverphoneEqual() {
        assertTrue(caverphone.isCaverphoneEqual("Peter", "PETER"));
        assertTrue(caverphone.isCaverphoneEqual("Stevenson", "Stephenson"));
        assertFalse(caverphone.isCaverphoneEqual("Peter", "David"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullInputReturnsDefaultCode() {
        assertEquals("1111111111", caverphone.caverphone(null));
        assertEquals("1111111111", caverphone.encode((String) null));
    }

    @Test(timeout = 4000)
    public void testEmptyStringReturnsDefaultCode() {
        assertEquals("1111111111", caverphone.caverphone(""));
        assertEquals("1111111111", caverphone.encode(""));
    }

    @Test(timeout = 4000)
    public void testOnlyNonAlphaReturnsDefaultCode() {
        assertEquals("1111111111", caverphone.caverphone("1234567890"));
        assertEquals("1111111111", caverphone.caverphone("!@#$%^&*()"));
        assertEquals("1111111111", caverphone.caverphone("   "));
    }

    @Test(timeout = 4000)
    public void testSingleCharacters() {
        assertEquals("A111111111", caverphone.caverphone("a"));
        assertEquals("P111111111", caverphone.caverphone("b"));
        assertEquals("K111111111", caverphone.caverphone("c"));
        assertEquals("1111111111", caverphone.caverphone("e")); // 'e' gets removed by e$
    }

    @Test(timeout = 4000)
    public void testLongStringTruncationToTenChars() {
        String code = caverphone.caverphone("Supercalifragilisticexpialidocious");
        assertNotNull(code);
        assertEquals(10, code.length());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where "mb" at the end of a string should be replaced with "m2",
     * but the buggy implementation incorrectly uses "^mb" (start of string) instead of "mb$".
     *
     * In buggy Caverphone:
     * - "mbmb" -> "^mb" matches first mb -> "m2mb" -> b->p -> "m2mp" -> 2 removed -> "mmp" -> "MMP1111111"
     * In fixed Caverphone:
     * - "mbmb" -> "mb$" matches last mb -> "mbm2" -> b->p -> "mpm2" -> 2 removed -> "mpm" -> "MPM1111111"
     */
    @Test(timeout = 4000)
    public void testEndMbDefect() {
        assertEquals("MPM1111111", caverphone.caverphone("mbmb"));
    }

    @Test(timeout = 4000)
    public void testEndMbWord() {
        // Tests the end-of-word mb replacement on common words
        assertEquals("KLM1111111", caverphone.caverphone("climb"));
        assertEquals("TM11111111", caverphone.caverphone("dumb"));
        assertEquals("TM11111111", caverphone.caverphone("thumb"));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeWithNonStringObjectThrowsEncoderException() {
        try {
            caverphone.encode(new Object());
            fail("Expected EncoderException when encoding non-String Object");
        } catch (EncoderException e) {
            assertTrue(e.getMessage().contains("Parameter supplied to Caverphone encode is not of type java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testEncodeWithNullObjectThrowsEncoderException() {
        try {
            caverphone.encode((Object) null);
            fail("Expected EncoderException when encoding null Object");
        } catch (EncoderException e) {
            assertTrue(e.getMessage().contains("Parameter supplied to Caverphone encode is not of type java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testEncodeWithIntegerThrowsEncoderException() {
        try {
            caverphone.encode(Integer.valueOf(42));
            fail("Expected EncoderException when encoding Integer");
        } catch (EncoderException e) {
            assertNotNull(e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEncodeValidStringAsObject() throws EncoderException {
        Object result = caverphone.encode((Object) "Peter");
        assertTrue("Result must be of type String", result instanceof String);
        assertEquals("PT11111111", result);
    }

    @Test(timeout = 4000)
    public void testIsCaverphoneEqualBoundaryCases() {
        assertTrue(caverphone.isCaverphoneEqual(null, null));
        assertTrue(caverphone.isCaverphoneEqual("", ""));
        assertTrue(caverphone.isCaverphoneEqual(null, ""));
        assertFalse(caverphone.isCaverphoneEqual("Peter", null));
        assertFalse(caverphone.isCaverphoneEqual(null, "Peter"));
    }
}