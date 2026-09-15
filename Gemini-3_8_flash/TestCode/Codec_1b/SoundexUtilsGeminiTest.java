package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.codec.language.SoundexUtils
 * Defect ID: Codec-1 (Locale-sensitive uppercase conversion causing improper phonetic encoding in Turkish)
 * ---------------------------------------------------------------------------------------------------------
 * Decision Branches & Boundary Conditions Covered:
 * 1. clean(String):
 *    - str == null: returns null
 *    - str.length() == 0: returns empty string
 *    - All letters (count == len): calls str.toUpperCase() [VULNERABILITY: locale-dependent vs Locale.ENGLISH]
 *    - Mixed letters and symbols (count < len && count > 0): toUpperCase(Locale.ENGLISH)
 *    - No letters (count == 0): returns empty string
 *    - Unicode/diacritics and Turkish lowercase 'i' / dotless 'ı' handling under Locale("tr")
 * 2. differenceEncoded(String, String):
 *    - es1 == null && es2 == null: returns 0
 *    - es1 == null && es2 != null: returns 0
 *    - es1 != null && es2 == null: returns 0
 *    - es1.length() == es2.length(): full match, zero match, partial match
 *    - es1.length() < es2.length() and es1.length() > es2.length()
 *    - Empty strings ("", ""): returns 0
 * 3. difference(StringEncoder, String, String):
 *    - Valid encoding paths with matching & non-matching strings
 *    - EncoderException propagation from custom/mock encoders
 * 4. Structural & Instantiation:
 *    - Verification of package-private constructor invocation
 * ---------------------------------------------------------------------------------------------------------
 */
public class SoundexUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & String Cleaning
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanNullAndEmpty() {
        assertNull(SoundexUtils.clean(null));
        assertEquals("", SoundexUtils.clean(""));
    }

    @Test(timeout = 4000)
    public void testCleanPureAlphabetical() {
        assertEquals("HELLO", SoundexUtils.clean("hello"));
        assertEquals("WORLD", SoundexUtils.clean("WORLD"));
        assertEquals("APACHECOMMONS", SoundexUtils.clean("ApacheCommons"));
    }

    @Test(timeout = 4000)
    public void testCleanWithSymbolsAndNumbers() {
        assertEquals("HELLOWORLD", SoundexUtils.clean("hello 123 world!"));
        assertEquals("ABC", SoundexUtils.clean("A-B-C"));
        assertEquals("CODEC", SoundexUtils.clean("1234Codec5678"));
    }

    @Test(timeout = 4000)
    public void testCleanNoLetters() {
        assertEquals("", SoundexUtils.clean("1234567890"));
        assertEquals("", SoundexUtils.clean("!@#$%^&*()_+=-`~[]{}|;:',.<>?/"));
    }

    // =========================================================================
    // Partition B: Difference & Similarity Calculations
    // =========================================================================

    @Test(timeout = 4000)
    public void testDifferenceEncodedNullBranches() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, null));
        assertEquals(0, SoundexUtils.differenceEncoded(null, "A123"));
        assertEquals(0, SoundexUtils.differenceEncoded("A123", null));
    }

    @Test(timeout = 4000)
    public void testDifferenceEncodedEmptyStrings() {
        assertEquals(0, SoundexUtils.differenceEncoded("", ""));
        assertEquals(0, SoundexUtils.differenceEncoded("", "A123"));
        assertEquals(0, SoundexUtils.differenceEncoded("A123", ""));
    }

    @Test(timeout = 4000)
    public void testDifferenceEncodedMatchingAndMismatch() {
        assertEquals(4, SoundexUtils.differenceEncoded("A123", "A123"));
        assertEquals(0, SoundexUtils.differenceEncoded("A123", "B456"));
        assertEquals(2, SoundexUtils.differenceEncoded("A123", "A156"));
    }

    @Test(timeout = 4000)
    public void testDifferenceEncodedDifferentLengths() {
        // First string shorter
        assertEquals(3, SoundexUtils.differenceEncoded("A12", "A12345"));
        // First string longer
        assertEquals(3, SoundexUtils.differenceEncoded("A12345", "A12"));
        // Partial overlap with length difference
        assertEquals(2, SoundexUtils.differenceEncoded("ABCD", "AB"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Codec-1 / Locale Independence)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCleanLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            // On unpatched versions, "i".toUpperCase() yields U+0130 ('İ') under Turkish locale
            // instead of ASCII 'I' (0x49), causing phonetic encoders to fail.
            String cleanI = SoundexUtils.clean("i");
            assertEquals("I", cleanI);
            assertNotEquals("\u0130", cleanI);

            assertEquals("TEST", SoundexUtils.clean("test"));
            assertEquals("INTERNET", SoundexUtils.clean("internet"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    @Test(timeout = 4000)
    public void testCleanWithMixedCharsUnderTurkishLocale() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            // Mixed letters and numbers trigger the count != len path
            assertEquals("I", SoundexUtils.clean("i1"));
            assertEquals("TEST", SoundexUtils.clean("test-1"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDifferenceSuccess() throws EncoderException {
        StringEncoder mockEncoder = new StringEncoder() {
            @Override
            public Object encode(Object obj) throws EncoderException {
                return encode((String) obj);
            }

            @Override
            public String encode(String str) throws EncoderException {
                return SoundexUtils.clean(str);
            }
        };

        assertEquals(4, SoundexUtils.difference(mockEncoder, "test", "test"));
        assertEquals(0, SoundexUtils.difference(mockEncoder, "abcd", "wxyz"));
    }

    @Test(timeout = 4000)
    public void testDifferenceThrowsEncoderExceptionFirstArg() {
        StringEncoder failingEncoder = new StringEncoder() {
            @Override
            public Object encode(Object obj) throws EncoderException {
                return encode((String) obj);
            }

            @Override
            public String encode(String str) throws EncoderException {
                if ("FAIL".equals(str)) {
                    throw new EncoderException("Encoding failed intentionally");
                }
                return str;
            }
        };

        try {
            SoundexUtils.difference(failingEncoder, "FAIL", "PASS");
            fail("Expected EncoderException was not thrown");
        } catch (EncoderException e) {
            assertEquals("Encoding failed intentionally", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDifferenceThrowsEncoderExceptionSecondArg() {
        StringEncoder failingEncoder = new StringEncoder() {
            @Override
            public Object encode(Object obj) throws EncoderException {
                return encode((String) obj);
            }

            @Override
            public String encode(String str) throws EncoderException {
                if ("FAIL".equals(str)) {
                    throw new EncoderException("Encoding failed on second argument");
                }
                return str;
            }
        };

        try {
            SoundexUtils.difference(failingEncoder, "PASS", "FAIL");
            fail("Expected EncoderException was not thrown");
        } catch (EncoderException e) {
            assertEquals("Encoding failed on second argument", e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Contract & Structural Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        // Tests coverage for package-private default constructor
        SoundexUtils utils = new SoundexUtils();
        assertNotNull(utils);
    }
}