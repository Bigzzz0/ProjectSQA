package org.apache.commons.codec.language;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringEncoder;
import java.util.Locale;
import org.junit.Test;
import static org.junit.Assert.*;

public class SoundexUtilsClaudeTest {

    /**
     * @target SoundexUtils.clean(String)
     * @scenario null input
     * @defectRisk should return null without throwing NPE
     */
    @Test(timeout = 4000)
    public void testCleanNull() {
        assertNull(SoundexUtils.clean(null));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario empty string input
     * @defectRisk should return empty string as-is (length==0 branch)
     */
    @Test(timeout = 4000)
    public void testCleanEmptyString() {
        assertEquals("", SoundexUtils.clean(""));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario all letters, count == len branch
     * @defectRisk should return uppercase of original string via toUpperCase() without Locale
     */
    @Test(timeout = 4000)
    public void testCleanAllLetters() {
        assertEquals("HELLO", SoundexUtils.clean("Hello"));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario mixed letters and non-letters, count < len branch
     * @defectRisk should filter out non-letter characters and uppercase remaining letters
     */
    @Test(timeout = 4000)
    public void testCleanMixedLettersAndDigits() {
        assertEquals("HELLO", SoundexUtils.clean("He11o!"));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario string with no letters at all, count == 0 branch
     * @defectRisk should return an empty string when no letters present
     */
    @Test(timeout = 4000)
    public void testCleanNoLetters() {
        assertEquals("", SoundexUtils.clean("12345!@#$%"));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario single letter, count == len branch
     * @defectRisk boundary case with a single character
     */
    @Test(timeout = 4000)
    public void testCleanSingleLetter() {
        assertEquals("A", SoundexUtils.clean("a"));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario whitespace embedded within letters
     * @defectRisk ensure whitespace treated as non-letter and filtered
     */
    @Test(timeout = 4000)
    public void testCleanWithWhitespace() {
        assertEquals("HELLOWORLD", SoundexUtils.clean("Hello World"));
    }

    /**
     * @target SoundexUtils.clean(String)
     * @scenario Turkish locale independence defect
     * @defectRisk clean("i") must return "I" (ASCII 0x49) not Turkish dotted capital I (U+0130)
     *             when default locale is Turkish; verifies fix uses Locale.ENGLISH explicitly.
     */
    @Test(timeout = 4000)
    public void testCleanLocaleIndependence_Turkish() {
        Locale orig = Locale.getDefault();
        Locale.setDefault(new Locale("tr"));
        try {
            String result1 = SoundexUtils.clean("i");
            String result4 = SoundexUtils.clean("test");

            assertEquals("I", result1);
            assertEquals(0x49, result1.charAt(0));
            assertNotEquals("\u0130", result1);
            assertEquals("TEST", result4);
        } finally {
            Locale.setDefault(orig);
        }
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario both arguments null
     * @defectRisk should return 0 immediately without NPE
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedBothNull() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, null));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario first argument null
     * @defectRisk should return 0 when es1 is null
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedFirstNull() {
        assertEquals(0, SoundexUtils.differenceEncoded(null, "ABC"));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario second argument null
     * @defectRisk should return 0 when es2 is null
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedSecondNull() {
        assertEquals(0, SoundexUtils.differenceEncoded("ABC", null));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario identical strings
     * @defectRisk should return length of string as all chars match
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedIdentical() {
        assertEquals(4, SoundexUtils.differenceEncoded("A123", "A123"));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario completely different strings of equal length
     * @defectRisk should return 0 when no characters match at same position
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedCompletelyDifferent() {
        assertEquals(0, SoundexUtils.differenceEncoded("ABCD", "WXYZ"));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario unequal length strings, partial match within min length
     * @defectRisk should only compare up to shortest string's length (lengthToMatch branch)
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedUnequalLength() {
        assertEquals(2, SoundexUtils.differenceEncoded("AB12", "AB"));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario both empty strings
     * @defectRisk lengthToMatch = 0, loop should not execute, return 0
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedBothEmpty() {
        assertEquals(0, SoundexUtils.differenceEncoded("", ""));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario one empty, one non-empty string
     * @defectRisk lengthToMatch = 0 due to min with empty string, loop should not execute
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedOneEmpty() {
        assertEquals(0, SoundexUtils.differenceEncoded("", "ABCD"));
    }

    /**
     * @target SoundexUtils.differenceEncoded(String,String)
     * @scenario partial character match at different positions
     * @defectRisk ensure only same-index matching characters counted, not just any occurrence
     */
    @Test(timeout = 4000)
    public void testDifferenceEncodedPartialMatch() {
        assertEquals(2, SoundexUtils.differenceEncoded("W252", "W250"));
    }

    /**
     * @target SoundexUtils.difference(StringEncoder,String,String)
     * @scenario normal encoder usage with valid strings producing a difference score
     * @defectRisk ensure encode() is called on both strings and result passed correctly to differenceEncoded
     */
    @Test(timeout = 4000)
    public void testDifferenceWithValidEncoder() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }

            @Override
            public String encode(String source) throws EncoderException {
                if (source == null) {
                    return null;
                }
                return source.toUpperCase(Locale.ENGLISH);
            }
        };
        int diff = SoundexUtils.difference(encoder, "abcd", "abcx");
        assertEquals(3, diff);
    }

    /**
     * @target SoundexUtils.difference(StringEncoder,String,String)
     * @scenario encoder returns null for one of the strings (simulating null-safe encoded values)
     * @defectRisk ensure differenceEncoded handles null encoded values gracefully returning 0
     */
    @Test(timeout = 4000)
    public void testDifferenceWithEncoderReturningNull() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }

            @Override
            public String encode(String source) throws EncoderException {
                return null;
            }
        };
        int diff = SoundexUtils.difference(encoder, "abc", "xyz");
        assertEquals(0, diff);
    }

    /**
     * @target SoundexUtils.difference(StringEncoder,String,String)
     * @scenario encoder throws EncoderException
     * @defectRisk ensure exception propagates correctly from encode() call
     */
    @Test(timeout = 4000)
    public void testDifferenceEncoderThrowsException() {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public Object encode(Object source) throws EncoderException {
                throw new EncoderException("forced failure");
            }

            @Override
            public String encode(String source) throws EncoderException {
                throw new EncoderException("forced failure");
            }
        };
        try {
            SoundexUtils.difference(encoder, "abc", "def");
            fail("Expected EncoderException to be thrown");
        } catch (EncoderException e) {
            assertEquals("forced failure", e.getMessage());
        }
    }

    /**
     * @target SoundexUtils.difference(StringEncoder,String,String)
     * @scenario identical strings encoded identically
     * @defectRisk verify full match count returned through the delegation chain
     */
    @Test(timeout = 4000)
    public void testDifferenceIdenticalStrings() throws EncoderException {
        StringEncoder encoder = new StringEncoder() {
            @Override
            public Object encode(Object source) throws EncoderException {
                return encode((String) source);
            }

            @Override
            public String encode(String source) throws EncoderException {
                return source;
            }
        };
        int diff = SoundexUtils.difference(encoder, "SAME", "SAME");
        assertEquals(4, diff);
    }
}