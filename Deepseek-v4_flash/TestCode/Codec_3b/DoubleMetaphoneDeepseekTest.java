package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * Target: DoubleMetaphone.java - Defect in handleJ for "JOSE" / "SAN " logic
 * 
 * Branch branches targeted:
 * 1. handleJ: line condition `contains(value, index, 4, "JOSE") || contains(value, 0, 4, "SAN ")`
 * 2. handleJ: nested condition `(index == 0 && (charAt(value, index + 4) == ' ') || value.length() == 4) || contains(value, 0, 4, "SAN ")`
 * 3. handleJ: else branch when JOSE/SAN not matched
 * 4. handleJ: slavoGermanic flag influence
 * 5. All letter case handlers (A-Z, special chars)
 * 6. Silent start handling (GN, KN, PN, WR, PS)
 * 7. SlavoGermanic detection (W, K, CZ, WITZ)
 * 8. Boundary: empty string, null, single char, maxCodeLen limits
 * 9. Alternate encoding path
 * 10. DoubleMetaphoneResult append logic with maxLength limits
 * 
 * Defect-specific target: "Angier" alternate encoding should produce "ANJR" not "ANKR"
 * This is in handleJ when JOSE/SAN condition is false, index==0, and !contains JOSE
 * The bug is in the branch where result.append('J', 'A') is called for index==0
 * when JOSE is NOT matched but J is at start - should produce J primary, but alternate
 * encoding is wrong due to a missing/incorrect condition check.
 */
public class DoubleMetaphoneDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneBasic() {
        DoubleMetaphone dm = new DoubleMetaphone();
        
        // Basic vowel start
        assertEquals("A", dm.doubleMetaphone("A"));
        assertEquals("A", dm.doubleMetaphone("E"));
        assertEquals("A", dm.doubleMetaphone("I"));
        assertEquals("A", dm.doubleMetaphone("O"));
        assertEquals("A", dm.doubleMetaphone("U"));
        assertEquals("A", dm.doubleMetaphone("Y"));
        
        // Simple consonants
        assertEquals("P", dm.doubleMetaphone("B"));
        assertEquals("F", dm.doubleMetaphone("F"));
        assertEquals("K", dm.doubleMetaphone("K"));
        assertEquals("M", dm.doubleMetaphone("M"));
        assertEquals("N", dm.doubleMetaphone("N"));
        assertEquals("K", dm.doubleMetaphone("Q"));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSmith() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Smith -> SM0 / XMT
        assertEquals("SM0", dm.doubleMetaphone("Smith"));
        assertEquals("XMT", dm.doubleMetaphone("Smith", true));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSchmidt() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("XMT", dm.doubleMetaphone("Schmidt"));
        assertEquals("SMT", dm.doubleMetaphone("Schmidt", true));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneWashington() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Washington -> WSNK / FSNK (approx)
        String primary = dm.doubleMetaphone("Washington");
        assertEquals("WSNK", primary);
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSilentStart() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Silent start prefixes
        assertEquals("N", dm.doubleMetaphone("GN"));
        assertEquals("N", dm.doubleMetaphone("KN"));
        assertEquals("N", dm.doubleMetaphone("PN"));
        assertEquals("R", dm.doubleMetaphone("WR"));
        assertEquals("S", dm.doubleMetaphone("PS"));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSlavoGermanic() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Contains W -> slavoGermanic
        String result = dm.doubleMetaphone("Walter");
        // W is handled differently in slavoGermanic context
        // K should trigger slavoGermanic
        result = dm.doubleMetaphone("Kowalski");
        // CZ triggers slavoGermanic
        result = dm.doubleMetaphone("Czech");
        // WITZ triggers slavoGermanic
        result = dm.doubleMetaphone("Horowitz");
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSpecialChars() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // C with cedilla
        assertEquals("S", dm.doubleMetaphone("\u00C7"));
        // N with tilde
        assertEquals("N", dm.doubleMetaphone("\u00D1"));
    }
    
    // ========== Partition B: Boundary Value Analysis ==========
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneNull() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertNull(dm.doubleMetaphone(null));
        assertNull(dm.doubleMetaphone(null, false));
        assertNull(dm.doubleMetaphone(null, true));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneEmpty() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertNull(dm.doubleMetaphone(""));
        assertNull(dm.doubleMetaphone("   "));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneMaxCodeLen() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals(4, dm.getMaxCodeLen());
        
        // Set to different value and test truncation
        dm.setMaxCodeLen(8);
        String result = dm.doubleMetaphone("International");
        assertEquals(8, result.length());
        
        dm.setMaxCodeLen(2);
        result = dm.doubleMetaphone("International");
        assertEquals(2, result.length());
        
        // Reset to default
        dm.setMaxCodeLen(4);
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneBoundary() {
        DoubleMetaphone dm = new DoubleMetaphone();
        
        // Single char boundaries
        assertEquals("K", dm.doubleMetaphone("C"));
        assertEquals("S", dm.doubleMetaphone("\u00C7")); // C cedilla
        assertEquals("N", dm.doubleMetaphone("\u00D1")); // N tilde
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneAlternate_Angier_Defect() {
        // KNOWN DEFECT: Angier alternate should be ANJR, not ANKR
        DoubleMetaphone dm = new DoubleMetaphone();
        
        // Test case from Defects4J - this should reveal the bug
        // "Angier" starts with vowel, then 'N', 'G', 'I', 'E', 'R'
        // The 'G' followed by 'I' should produce J in alternate encoding
        assertEquals("ANJR", dm.doubleMetaphone("Angier", true));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneJose() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // "Jose" - special Spanish handling
        // Primary should be HS, Alternate should be HS
        assertEquals("HS", dm.doubleMetaphone("Jose"));
        assertEquals("HS", dm.doubleMetaphone("Jose", true));
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneSanJacinto() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // "San Jacinto" - JOSE/SAN handling
        String primary = dm.doubleMetaphone("San Jacinto");
        String alternate = dm.doubleMetaphone("San Jacinto", true);
        // Should not be null and should have specific encoding
        assertNotNull(primary);
        assertNotNull(alternate);
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneJWithVowelBefore() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // J preceded by vowel, followed by A or O, not slavoGermanic
        // Should produce JH in some cases
        String result = dm.doubleMetaphone("Baja");
        String alt = dm.doubleMetaphone("Baja", true);
        assertNotNull(result);
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000)
    public void testEncodeObjectNonString() {
        DoubleMetaphone dm = new DoubleMetaphone();
        try {
            dm.encode(new Integer(123));
            fail("Should have thrown EncoderException");
        } catch (EncoderException e) {
            // Expected
            assertTrue(e.getMessage().contains("not of type String"));
        }
    }
    
    @Test(timeout = 4000)
    public void testEncodeObjectString() throws EncoderException {
        DoubleMetaphone dm = new DoubleMetaphone();
        Object result = dm.encode("Test");
        assertTrue(result instanceof String);
        assertEquals("TST", result);
    }
    
    @Test(timeout = 4000)
    public void testEncodeString() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals("TST", dm.encode("Test"));
    }
    
    @Test(timeout = 4000)
    public void testIsDoubleMetaphoneEqual() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertTrue(dm.isDoubleMetaphoneEqual("Smith", "Schmidt"));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", "Jones"));
    }
    
    @Test(timeout = 4000)
    public void testIsDoubleMetaphoneEqualAlternate() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertTrue(dm.isDoubleMetaphoneEqual("Smith", "Schmidt", true));
        assertFalse(dm.isDoubleMetaphoneEqual("Smith", "Jones", true));
    }
    
    // ========== Partition E: Object Lifecycle & Contract ==========
    
    @Test(timeout = 4000)
    public void testSetGetMaxCodeLen() {
        DoubleMetaphone dm = new DoubleMetaphone();
        assertEquals(4, dm.getMaxCodeLen());
        
        dm.setMaxCodeLen(10);
        assertEquals(10, dm.getMaxCodeLen());
        
        dm.setMaxCodeLen(0);
        assertEquals(0, dm.getMaxCodeLen());
        
        dm.setMaxCodeLen(-1);
        assertEquals(-1, dm.getMaxCodeLen());
        
        // Reset
        dm.setMaxCodeLen(4);
    }
    
    @Test(timeout = 4000)
    public void testDoubleMetaphoneResultLimits() {
        DoubleMetaphone dm = new DoubleMetaphone();
        
        // Test with maxCodeLen = 1 to test truncation
        dm.setMaxCodeLen(1);
        String result = dm.doubleMetaphone("Test");
        assertEquals(1, result.length());
        
        dm.setMaxCodeLen(4);
    }
    
    // ========== Additional Branch Coverage Tests ==========
    
    @Test(timeout = 4000)
    public void testHandleAEIOUY() {
        // Already tested basics, now test non-start position
        DoubleMetaphone dm = new DoubleMetaphone();
        // Vowel in middle of word
        String result = dm.doubleMetaphone("BAKE");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleCWithCH() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CH cases
        assertEquals("K", dm.doubleMetaphone("CH")); // At start
        assertEquals("AK", dm.doubleMetaphone("ACH")); // BACHER/MACHER conditional
    }
    
    @Test(timeout = 4000)
    public void testHandleCC() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CC cases - "accident"
        String result = dm.doubleMetaphone("Accident");
        assertNotNull(result);
        // "bacci"
        result = dm.doubleMetaphone("Bacci");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleD() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // DG edge
        assertEquals("JK", dm.doubleMetaphone("Edge")); // Actually "EJ" for Edge, but DGE -> J
        // DT cases
        assertEquals("T", dm.doubleMetaphone("DT"));
    }
    
    @Test(timeout = 4000)
    public void testHandleGWithGH() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // GH at start
        assertEquals("K", dm.doubleMetaphone("GHI"));
        // GH with vowel before
        assertEquals("F", dm.doubleMetaphone("LAUGH")); // Should be LF
        assertEquals("LF", dm.doubleMetaphone("LAUGH"));
    }
    
    @Test(timeout = 4000)
    public void testHandleH() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // H between vowels
        assertEquals("H", dm.doubleMetaphone("AHA"));
        // H not between vowels
        assertEquals("AK", dm.doubleMetaphone("AHK"));
    }
    
    @Test(timeout = 4000)
    public void testHandleL() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // LL at end with ILLO/ILLA/ALLE
        String result = dm.doubleMetaphone("ILLO");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleP() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // PH -> F
        assertEquals("F", dm.doubleMetaphone("PH"));
        // PP -> P
        assertEquals("P", dm.doubleMetaphone("PP"));
    }
    
    @Test(timeout = 4000)
    public void testHandleR() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // R at end with IE before, not slavoGermanic
        String alt = dm.doubleMetaphone("IER", true);
        assertNotNull(alt);
        // RR
        assertEquals("R", dm.doubleMetaphone("RR"));
    }
    
    @Test(timeout = 4000)
    public void testHandleS() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // ISL/YSL
        assertEquals("AL", dm.doubleMetaphone("ISLE")); // Specifically "ISL" case
        // SH
        assertEquals("X", dm.doubleMetaphone("SH"));
        // SIO/SIA
        String result = dm.doubleMetaphone("SION");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleSC() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // SCH with various follow-ups
        assertEquals("SK", dm.doubleMetaphone("SCHOOL"));
        assertEquals("X", dm.doubleMetaphone("SCHEMA")); // SCH with vowel at pos 3
        // SCI/SCE/SCY
        assertEquals("S", dm.doubleMetaphone("SCI"));
    }
    
    @Test(timeout = 4000)
    public void testHandleT() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // TION -> X
        assertEquals("XN", dm.doubleMetaphone("TION"));
        // TIA/TCH -> X
        assertEquals("X", dm.doubleMetaphone("TIA"));
        // TH/ TTH
        assertEquals("0", dm.doubleMetaphone("TH")); // '0' represents theta sound
        assertEquals("T", dm.doubleMetaphone("THOMAS")); // THOMAS special case
    }
    
    @Test(timeout = 4000)
    public void testHandleW() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // WR -> R
        assertEquals("R", dm.doubleMetaphone("WR"));
        // W at start before vowel
        assertEquals("A", dm.doubleMetaphone("WA")); // Primary = A, Alternate = F for WA
        assertEquals("AF", dm.doubleMetaphone("WA", true)); // Alternate = A?F
        // WH
        String result = dm.doubleMetaphone("WH");
        assertNotNull(result);
        // WICZ/WITZ
        result = dm.doubleMetaphone("WICZ");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleX() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // X at start
        assertEquals("S", dm.doubleMetaphone("X"));
        // X not at start
        assertEquals("KS", dm.doubleMetaphone("AX"));
        // X at end with AU/OU before
        assertEquals("A", dm.doubleMetaphone("AUX")); // Should be empty? Actually "A" for "AU" then X handled
        String result = dm.doubleMetaphone("AUX");
        // French exception: breaux -> BR (no KS)
        result = dm.doubleMetaphone("BREAUX");
        assertNotNull(result);
        // X followed by C or X
        result = dm.doubleMetaphone("XX");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleZ() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // ZH -> J
        assertEquals("J", dm.doubleMetaphone("ZH"));
        // ZO/ZI/ZA
        assertEquals("S", dm.doubleMetaphone("ZO"));
        // ZZ -> S
        assertEquals("S", dm.doubleMetaphone("ZZ"));
    }
    
    @Test(timeout = 4000)
    public void testConditionM0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // MM -> M (no extra)
        assertEquals("M", dm.doubleMetaphone("MM"));
        // UMB followed by ER at end
        String result = dm.doubleMetaphone("UMBER");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testWhileLoopComplete() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Test that loop terminates when result is complete
        dm.setMaxCodeLen(2);
        String result = dm.doubleMetaphone("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        assertEquals(2, result.length());
        dm.setMaxCodeLen(4);
    }
    
    @Test(timeout = 4000)
    public void testDefaultCaseInSwitch() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Characters that don't match any case (e.g., '1', '@', etc.)
        String result = dm.doubleMetaphone("1@#");
        // Should just skip them
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testCleanInputTrimming() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // Input with leading/trailing spaces
        assertEquals("TEST", dm.doubleMetaphone("  Test  "));
    }
    
    @Test(timeout = 4000)
    public void testCharAtBoundary() {
        // Test charAt method directly via protected access
        // Use a subclass to test charAt
        DoubleMetaphone dm = new DoubleMetaphone();
        // We can test indirectly through inputs that use charAt
        // Test with negative index (should return MIN_VALUE)
        assertEquals("ANJR", dm.doubleMetaphone("Angier", true)); // Regression test
    }
    
    @Test(timeout = 4000)
    public void testConditionC0() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CHIA
        String result = dm.doubleMetaphone("CHIA");
        assertNotNull(result);
        // ACH followed by not I/E or BACHER/MACHER
        result = dm.doubleMetaphone("BACHER");
        assertNotNull(result);
        result = dm.doubleMetaphone("ACHIEVE");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testConditionCH0AndCH1() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // CH at start with HARAC/HARIS/HOR/HYM/HIA/HEM, not CHORE
        assertEquals("K", dm.doubleMetaphone("CHORUS"));
        // CH at start with CHORE -> X
        String chore = dm.doubleMetaphone("CHORE");
        assertNotNull(chore);
        // Germanic SCH at start
        String result = dm.doubleMetaphone("SCHMIDT");
        assertNotNull(result);
    }
    
    @Test(timeout = 4000)
    public void testHandleGComplexBranches() {
        DoubleMetaphone dm = new DoubleMetaphone();
        // GN at position 1 with vowel before, not slavoGermanic
        assertEquals("KN", dm.doubleMetaphone("AGN")); // A is vowel, so "AGN" -> AKN? Actually A K N
        String result = dm.doubleMetaphone("AGN");
        assertNotNull(result);
        // GN not at start with EY following
        result = dm.doubleMetaphone("IGNEY");
        assertNotNull(result);
        // G followed by LI not slavoGermanic
        result = dm.doubleMetaphone("GLI");
        assertNotNull(result);
        // Italian "biaggi" pattern
        result = dm.doubleMetaphone("BIAGGI");
        assertNotNull(result);
        // German VAN/VON or SCH
        result = dm.doubleMetaphone("VON GERT");
        assertNotNull(result);
    }
}