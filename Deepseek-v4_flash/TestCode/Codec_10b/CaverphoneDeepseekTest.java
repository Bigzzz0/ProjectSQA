package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Normal encoding paths: various input strings producing expected Caverphone codes
 *   - Edge cases: single characters, repeated characters, special patterns
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - null input -> returns "1111111111"
 *   - empty string -> returns "1111111111"
 *   - strings with non-alphabetic characters (digits, symbols) -> filtered out
 *   - very long strings -> still produces 10-char output
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: "mbmb" input produces "MMP1111111" instead of "MPM1111111"
 *     Root cause: The regex "^mb" only matches at start, but "mb" in middle is not handled
 *     The algorithm processes "mbmb": 
 *       1. Lowercase: "mbmb"
 *       2. No initial replacements match (starts with 'm')
 *       3. Step 4: 'b' -> 'p' gives "mpmp"
 *       4. 'm+' -> 'M' gives "MpMp" (but should be "MpMp" then later processing)
 *       5. 'p+' -> 'P' gives "MPMP"
 *       6. Remove '2's (none)
 *       7. Remove trailing '3's (none)
 *       8. Add 10 '1's: "MPMP1111111111"
 *       9. Take first 10: "MPMP111111" -> but expected is "MPM1111111"
 *     The bug is that "mb" in the middle should also be transformed to "m2", but only "^mb" is handled
 *     This test will expose the discrepancy
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - encode(Object) with non-String -> EncoderException
 *   - encode(String) -> delegates to caverphone
 *   - isCaverphoneEqual -> compares two encodings
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor works
 *   - Multiple invocations produce consistent results
 */
public class CaverphoneDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testBasicEncoding() {
        Caverphone c = new Caverphone();
        // Simple cases
        assertEquals("1111111111", c.caverphone(""));
        assertEquals("1111111111", c.caverphone("a"));
        assertEquals("1111111111", c.caverphone("e"));
        assertEquals("1111111111", c.caverphone("i"));
        assertEquals("1111111111", c.caverphone("o"));
        assertEquals("1111111111", c.caverphone("u"));
    }
    
    @Test(timeout = 4000)
    public void testConsonantEncoding() {
        Caverphone c = new Caverphone();
        // Single consonants
        assertEquals("1111111111", c.caverphone("b"));  // b->p, p+->P, then P1111111111 -> first 10: P1111111111? Actually P1111111111 is 11 chars, substring(0,10) = P111111111
        // Let's trace: "b" -> lowercase "b" -> no e$ -> no start matches -> b->p -> "p" -> p+->P -> "P" -> no more -> +10 ones -> "P1111111111" -> substring(0,10) = "P111111111"
        assertEquals("P111111111", c.caverphone("b"));
        
        // "c" -> c->k -> k+->K -> "K1111111111" -> "K111111111"
        assertEquals("K111111111", c.caverphone("c"));
        
        // "d" -> d->t -> t+->T -> "T1111111111" -> "T111111111"
        assertEquals("T111111111", c.caverphone("d"));
        
        // "f" -> f+->F -> "F1111111111" -> "F111111111"
        assertEquals("F111111111", c.caverphone("f"));
    }
    
    @Test(timeout = 4000)
    public void testSpecialPatterns() {
        Caverphone c = new Caverphone();
        // Test "cough" start pattern
        assertEquals("K111111111", c.caverphone("cough")); // cough -> cou2f -> then processing...
        // Actually: "cough" -> "cou2f" -> c->k -> "kou2f" -> ... complex, just check it doesn't crash
        
        // Test "rough" start
        assertEquals("R111111111", c.caverphone("rough")); // rough -> rou2f -> r->2 -> ... 
        // Actually rough -> rou2f -> then r->2 -> "2ou2f" -> 2 removed -> "ouf" -> o->3 -> "u3f" -> u->3 -> "33f" -> f+->F -> "33F" -> 3 removed -> "F" -> +10 ones -> "F1111111111" -> "F111111111"
        assertEquals("F111111111", c.caverphone("rough"));
    }
    
    @Test(timeout = 4000)
    public void testVowelHandling() {
        Caverphone c = new Caverphone();
        // Leading vowel becomes 'A'
        assertEquals("A111111111", c.caverphone("ab"));
        // Internal vowel becomes '3' then removed
        assertEquals("P111111111", c.caverphone("bab")); // b->p, a->A (leading? no, internal), b->p -> "pAp" -> A stays? Actually ^[aeiou] only at start, internal [aeiou] -> 3 -> "p3p" -> 3 removed -> "pp" -> p+->P -> "P" -> +10 -> "P1111111111" -> "P111111111"
        assertEquals("P111111111", c.caverphone("bab"));
    }
    
    @Test(timeout = 4000)
    public void testMultipleReplacements() {
        Caverphone c = new Caverphone();
        // "tio" -> "sio"
        assertEquals("S111111111", c.caverphone("tio")); // tio -> sio -> s+->S -> "S" -> +10 -> "S1111111111" -> "S111111111"
        
        // "tia" -> "sia"
        assertEquals("S111111111", c.caverphone("tia"));
        
        // "dg" -> "2g" -> 2 removed -> "g" -> g->k -> k+->K
        assertEquals("K111111111", c.caverphone("dg"));
    }
    
    @Test(timeout = 4000)
    public void testEndingRules() {
        Caverphone c = new Caverphone();
        // Words ending in 'e' have it removed
        assertEquals("K111111111", c.caverphone("make")); // make -> mak -> m->M? Actually m+->M -> "Mak" -> a->3 -> "M3k" -> 3 removed -> "Mk" -> k+->K -> "MK" -> +10 -> "MK1111111111" -> "MK111111111"
        assertEquals("MK111111111", c.caverphone("make"));
        
        // Words ending in 'w' become '3' then removed
        assertEquals("K111111111", c.caverphone("know")); // complex, just check
    }
    
    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====
    
    @Test(timeout = 4000)
    public void testNullInput() {
        Caverphone c = new Caverphone();
        assertEquals("1111111111", c.caverphone(null));
    }
    
    @Test(timeout = 4000)
    public void testEmptyString() {
        Caverphone c = new Caverphone();
        assertEquals("1111111111", c.caverphone(""));
    }
    
    @Test(timeout = 4000)
    public void testNonAlphabeticCharacters() {
        Caverphone c = new Caverphone();
        // Digits and symbols are stripped
        assertEquals("1111111111", c.caverphone("123!@#"));
        assertEquals("K111111111", c.caverphone("c1a2t3")); // "cat" -> c->k, a->A? Actually ^[aeiou] not at start, internal a->3 -> "k3t" -> 3 removed -> "kt" -> k+->K, t+->T -> "KT" -> +10 -> "KT1111111111" -> "KT111111111"
        assertEquals("KT111111111", c.caverphone("c1a2t3"));
    }
    
    @Test(timeout = 4000)
    public void testLongString() {
        Caverphone c = new Caverphone();
        // Very long input still produces 10-char output
        String longInput = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz";
        String result = c.caverphone(longInput);
        assertNotNull(result);
        assertEquals(10, result.length());
    }
    
    @Test(timeout = 4000)
    public void testUpperCaseInput() {
        Caverphone c = new Caverphone();
        // Upper case is converted to lower case
        assertEquals(c.caverphone("cat"), c.caverphone("CAT"));
        assertEquals(c.caverphone("CaT"), c.caverphone("cat"));
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    @Test(timeout = 4000)
    public void testEndMbDefect() {
        Caverphone c = new Caverphone();
        // Known defect: "mbmb" should produce "MPM1111111" but buggy version produces "MMP1111111"
        // The correct behavior: "mb" at start becomes "m2", then "mb" in middle should also be handled
        // Expected: MPM1111111
        String result = c.caverphone("mbmb");
        // The bug is that the code only handles "^mb" (start of string) but not internal "mb"
        // Correct behavior would transform both "mb" occurrences to "m2"
        // After processing: "m2m2" -> remove 2s -> "mm" -> m+->M -> "M" -> +10 -> "M1111111111" -> "M111111111"
        // But the actual algorithm does: "mbmb" -> no ^mb match (second occurrence not at start) -> b->p -> "mpmp" -> m+->M -> "MpMp" -> p+->P -> "MPMP" -> +10 -> "MPMP1111111111" -> "MPMP111111"
        // Expected from spec: "MPM1111111" (from the test name)
        // So we assert the expected correct value
        assertEquals("MPM1111111", result);
    }
    
    @Test(timeout = 4000)
    public void testMbAtStart() {
        Caverphone c = new Caverphone();
        // "mb" at start should become "m2"
        assertEquals("M111111111", c.caverphone("mb"));
        // Trace: "mb" -> ^mb -> "m2" -> remove 2 -> "m" -> m+->M -> "M" -> +10 -> "M1111111111" -> "M111111111"
    }
    
    @Test(timeout = 4000)
    public void testMbInMiddle() {
        Caverphone c = new Caverphone();
        // "amb" - 'mb' not at start, should it be handled? The bug is it's not
        // "amb" -> a->A (leading) -> "Amb" -> b->p -> "Amp" -> m+->M -> "AMp" -> p+->P -> "AMP" -> +10 -> "AMP1111111111" -> "AMP111111111"
        assertEquals("AMP111111111", c.caverphone("amb"));
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000, expected = EncoderException.class)
    public void testEncodeObjectWithNonString() throws EncoderException {
        Caverphone c = new Caverphone();
        c.encode(new Integer(42));
    }
    
    @Test(timeout = 4000)
    public void testEncodeObjectWithString() throws EncoderException {
        Caverphone c = new Caverphone();
        Object result = c.encode("test");
        assertTrue(result instanceof String);
        assertEquals(c.caverphone("test"), result);
    }
    
    @Test(timeout = 4000)
    public void testEncodeString() {
        Caverphone c = new Caverphone();
        assertEquals(c.caverphone("hello"), c.encode("hello"));
    }
    
    @Test(timeout = 4000)
    public void testIsCaverphoneEqual() {
        Caverphone c = new Caverphone();
        assertTrue(c.isCaverphoneEqual("cat", "kat")); // Both should produce same code
        assertTrue(c.isCaverphoneEqual("", "null")); // null becomes "1111111111", empty also "1111111111"
        assertFalse(c.isCaverphoneEqual("a", "b"));
    }
    
    @Test(timeout = 4000)
    public void testIsCaverphoneEqualWithNull() {
        Caverphone c = new Caverphone();
        assertTrue(c.isCaverphoneEqual(null, null));
        assertTrue(c.isCaverphoneEqual(null, ""));
        assertFalse(c.isCaverphoneEqual(null, "a"));
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testConstructor() {
        Caverphone c = new Caverphone();
        assertNotNull(c);
    }
    
    @Test(timeout = 4000)
    public void testConsistency() {
        Caverphone c = new Caverphone();
        String input = "hello world";
        String result1 = c.caverphone(input);
        String result2 = c.caverphone(input);
        assertEquals(result1, result2);
    }
    
    @Test(timeout = 4000)
    public void testMultipleInvocations() {
        Caverphone c = new Caverphone();
        // Ensure no state leakage between calls
        assertEquals(c.caverphone("cat"), c.caverphone("cat"));
        assertEquals(c.caverphone("dog"), c.caverphone("dog"));
        assertFalse(c.caverphone("cat").equals(c.caverphone("dog")));
    }
    
    @Test(timeout = 4000)
    public void testOutputLength() {
        Caverphone c = new Caverphone();
        // All outputs should be exactly 10 characters
        assertEquals(10, c.caverphone("").length());
        assertEquals(10, c.caverphone("a").length());
        assertEquals(10, c.caverphone("abcdefghijklmnopqrstuvwxyz").length());
        assertEquals(10, c.caverphone("1234567890").length());
        assertEquals(10, c.caverphone(null).length());
    }
    
    @Test(timeout = 4000)
    public void testOutputCharacters() {
        Caverphone c = new Caverphone();
        String result = c.caverphone("test");
        // Output should only contain digits and uppercase letters
        assertTrue(result.matches("[A-Z0-9]{10}"));
    }
}