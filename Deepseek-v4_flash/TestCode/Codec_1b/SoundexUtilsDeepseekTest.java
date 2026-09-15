package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for {@link SoundexUtils} targeting maximum coverage and the known
 * Turkish locale defect.
 */
public class SoundexUtilsDeepseekTest {

    // -----------------------------------------------------------------------
    // Tests for clean(String)
    // -----------------------------------------------------------------------

    /**
     * @target clean(String)
     * @scenario null input
     * @defectRisk NullPointerException if null not handled
     */
    @Test(timeout = 4000)
    public void testCleanNull() {
        assertNull("clean(null) should return null", SoundexUtils.clean(null));
    }

    /**
     * @target clean(String)
     * @scenario empty string input
     * @defectRisk Returning non-empty string for empty input
     */
    @Test(timeout = 4000)
    public void testCleanEmpty() {
        assertEquals("clean(\"\") should return empty string", "", SoundexUtils.clean(""));
    }

    /**
     * @target clean(String) – branch: count == len (all letters)
     * @scenario string consisting only of letters
     * @defectRisk Incorrect uppercase conversion (locale-sensitive)
     */
    @Test(timeout = 4000)
    public void testCleanAllLetters() {
        assertEquals("clean(\"hello\") should return \"HELLO\"", "HELLO", SoundexUtils.clean("hello"));
        assertEquals("clean(\"Hello\") should return \"HELLO\"", "HELLO", SoundexUtils.clean("Hello"));
        assertEquals("clean(\"HELLO\") should return \"HELLO\"", "HELLO", SoundexUtils.clean("HELLO"));
    }

    /**
     * @target clean(String) – branch: count < len (mixed letters and non-letters)
     * @scenario string with letters and digits
     * @defectRisk Non-letters not removed or wrong case
     */
    @Test(timeout = 4000)
    public void testCleanMixedLettersNonLetters() {
        assertEquals("clean(\"h3llo\") should return \"HLLO\"", "HLLO", SoundexUtils.clean("h3llo"));
        assertEquals("clean(\"a b c\") should return \"ABC\"", "ABC", SoundexUtils.clean("a b c"));
        assertEquals("clean(\"!@#\") should return \"\"", "", SoundexUtils.clean("!@#"));
    }

    /**
     * @target clean(String) – branch: count == 0 (no letters)
     * @scenario string with no letters at all
     * @defectRisk Returning non-empty string or exception
     */
    @Test(timeout = 4000)
    public void testCleanNoLetters() {
        assertEquals("clean(\"123\") should return \"\"", "", SoundexUtils.clean("123"));
        assertEquals("clean(\"!@#\") should return \"\"", "", SoundexUtils.clean("!@#"));
    }

    /**
     * @target clean(String) – Turkish locale defect
     * @scenario default locale set to Turkish, input "i" (all letters branch)
     * @defectRisk toUpperCase() without locale returns "İ" (U+0130) instead of "I"
     */
    @Test(timeout = 4000)
    public void testCleanLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr"));
            // All letters branch: count == len
            assertEquals("clean(\"i\") under Turkish locale should return \"I\"", "I", SoundexUtils.clean("i"));
            assertEquals("clean(\"test\") under Turkish locale should return \"TEST\"", "TEST", SoundexUtils.clean("test"));
            // Mixed branch: count < len, uses Locale.ENGLISH
            assertEquals("clean(\"i1\") under Turkish locale should return \"I\"", "I", SoundexUtils.clean("i1"));
        } finally {
            Locale.setDefault(orig);
        }
    }

    // -----------------------------------------------------------------------
    // Tests for differenceEncoded(String, String)
    // -----------------------------------------------------------------------

    /**
     * @target differenceEncoded(String, String)
     * @scenario both arguments null
     * @defectRisk NullPointerException or non-zero return
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedBothNull() {
        assertEquals("differenceEncoded(null, null) should return 0", 0, SoundexUtils.differenceEncoded(null, null));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario first argument null
     * @defectRisk NullPointerException or non-zero return
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedFirstNull() {
        assertEquals("differenceEncoded(null, \"abc\") should return 0", 0, SoundexUtils.differenceEncoded(null, "abc"));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario second argument null
     * @defectRisk NullPointerException or non-zero return
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedSecondNull() {
        assertEquals("differenceEncoded(\"abc\", null) should return 0", 0, SoundexUtils.differenceEncoded("abc", null));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario both strings empty
     * @defectRisk Incorrect handling of empty strings
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedBothEmpty() {
        assertEquals("differenceEncoded(\"\", \"\") should return 0", 0, SoundexUtils.differenceEncoded("", ""));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario identical strings of equal length
     * @defectRisk Wrong count of matching characters
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedIdentical() {
        assertEquals("differenceEncoded(\"abc\", \"abc\") should return 3", 3, SoundexUtils.differenceEncoded("abc", "abc"));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario strings of different lengths
     * @defectRisk Using max instead of min length
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedDifferentLengths() {
        assertEquals("differenceEncoded(\"abc\", \"ab\") should return 2", 2, SoundexUtils.differenceEncoded("abc", "ab"));
        assertEquals("differenceEncoded(\"ab\", \"abc\") should return 2", 2, SoundexUtils.differenceEncoded("ab", "abc"));
    }

    /**
     * @target differenceEncoded(String, String)
     * @scenario completely different strings
     * @defectRisk Non-zero return when no characters match
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedNoMatch() {
        assertEquals("differenceEncoded(\"abc\", \"xyz\") should return 0", 0, SoundexUtils.differenceEncoded("abc", "xyz"));
    }

    // -----------------------------------------------------------------------
    // Tests for difference(StringEncoder, String, String)
    // -----------------------------------------------------------------------

    // Helper encoder that returns the input string unchanged (for testing)
    private static class IdentityEncoder implements StringEncoder {
        @Override
        public String encode(String source) throws EncoderException {
            return source;
        }

        @Override
        public Object encode(Object source) throws EncoderException {
            if (source instanceof String) {
                return encode((String) source);
            }
            throw new EncoderException("Object not a String");
        }
    }

    // Helper encoder that throws EncoderException for certain inputs
    private static class ThrowingEncoder implements StringEncoder {
        @Override
        public String encode(String source) throws EncoderException {
            if ("fail".equals(source)) {
                throw new EncoderException("Forced failure");
            }
            return source;
        }

        @Override
        public Object encode(Object source) throws EncoderException {
            if (source instanceof String) {
                return encode((String) source);
            }
            throw new EncoderException("Object not a String");
        }
    }

    /**
     * @target difference(StringEncoder, String, String)
     * @scenario normal operation with identity encoder
     * @defectRisk Incorrect delegation to differenceEncoded
     */
    @Test(timeout = 4000)
    public void testDifferenceNormal() throws EncoderException {
        IdentityEncoder encoder = new IdentityEncoder();
        assertEquals("difference with \"abc\", \"abc\" should return 3", 3, SoundexUtils.difference(encoder, "abc", "abc"));
        assertEquals("difference with \"abc\", \"ab\" should return 2", 2, SoundexUtils.difference(encoder, "abc", "ab"));
        assertEquals("difference with \"abc\", \"xyz\" should return 0", 0, SoundexUtils.difference(encoder, "abc", "xyz"));
    }

    /**
     * @target difference(StringEncoder, String, String)
     * @scenario encoder throws EncoderException
     * @defectRisk Exception not propagated
     */
    @Test(timeout = 4000, expected = EncoderException.class)
    public void testDifferenceEncoderException() throws EncoderException {
        ThrowingEncoder encoder = new ThrowingEncoder();
        SoundexUtils.difference(encoder, "fail", "anything");
    }

    /**
     * @target difference(StringEncoder, String, String)
     * @scenario null encoder (should throw NullPointerException)
     * @defectRisk Not handling null encoder gracefully
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testDifferenceNullEncoder() throws EncoderException {
        SoundexUtils.difference(null, "a", "b");
    }
}