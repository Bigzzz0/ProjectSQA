package org.apache.commons.lang3;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.lang3.RandomStringUtils
 *
 * 1. Defect LANG-807 (Defects4J Ground Truth):
 *    - random(int count, int start, int end, boolean letters, boolean numbers, char[] chars, Random random)
 *    - Boundary: When start >= end (e.g. start=5, end=5, or start=10, end=5),
 *      gap <= 0 causes IllegalArgumentException. The defect is that RandomStringUtils does not validate
 *      end > start and instead relies on Random.nextInt() which throws "bound must be positive",
 *      missing expected message details about 'start' and 'end'.
 *
 * 2. Structural & Branch Coverage:
 *    - Constructor: RandomStringUtils() instantiation.
 *    - count == 0: returns empty string "".
 *    - count < 0: throws IllegalArgumentException with length message.
 *    - chars != null && chars.length == 0: throws IllegalArgumentException ("must not be empty").
 *    - start == 0 && end == 0:
 *      * chars != null: end set to chars.length.
 *      * chars == null && !letters && !numbers: end set to Integer.MAX_VALUE, start set to 0.
 *      * chars == null && (letters || numbers): start set to ' ', end set to 'z' + 1.
 *    - Character selection:
 *      * chars == null: ch chosen from random.nextInt(gap) + start.
 *      * chars != null: ch chosen from chars[random.nextInt(gap) + start].
 *    - Character validation filters:
 *      * letters && Character.isLetter(ch)
 *      * numbers && Character.isDigit(ch)
 *      * !letters && !numbers
 *      * Non-matching fallback: count++ (retry).
 *    - Unicode Surrogate Handling:
 *      * Low surrogate (56320 <= ch <= 57343): count == 0 (retry count++) vs count > 0 (pair inserted).
 *      * High surrogate (55296 <= ch <= 56191): count == 0 (retry count++) vs count > 0 (pair inserted).
 *      * Private high surrogate (56192 <= ch <= 56319): count++ (skipped).
 *      * Normal characters: buffer[count] = ch.
 *    - Standard Helper APIs:
 *      * random(count)
 *      * randomAscii(count)
 *      * randomAlphabetic(count)
 *      * randomAlphanumeric(count)
 *      * randomNumeric(count)
 *      * random(count, letters, numbers)
 *      * random(count, start, end, letters, numbers)
 *      * random(count, start, end, letters, numbers, chars)
 *      * random(count, String chars) (including null and non-null)
 *      * random(count, char... chars) (including null and non-null)
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

public class RandomStringUtilsGeminiTest {

    /**
     * Deterministic Random generator for white-box surrogate and branch path verification.
     */
    private static class DeterministicSequenceRandom extends Random {
        private static final long serialVersionUID = 1L;
        private final int[] values;
        private int index = 0;

        public DeterministicSequenceRandom(int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(int bound) {
            if (index < values.length) {
                int val = values[index++];
                if (val >= bound) {
                    return bound - 1;
                }
                return val;
            }
            return 0;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testRandomAscii() {
        final int count = 50;
        final String result = RandomStringUtils.randomAscii(count);
        assertNotNull(result);
        assertEquals(count, result.length());
        for (int i = 0; i < result.length(); i++) {
            char ch = result.charAt(i);
            assertTrue("Char should be between 32 and 126 inclusive: " + (int) ch, ch >= 32 && ch <= 126);
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphabetic() {
        final int count = 50;
        final String result = RandomStringUtils.randomAlphabetic(count);
        assertNotNull(result);
        assertEquals(count, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Char should be alphabetic", Character.isLetter(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomNumeric() {
        final int count = 50;
        final String result = RandomStringUtils.randomNumeric(count);
        assertNotNull(result);
        assertEquals(count, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Char should be numeric", Character.isDigit(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphanumeric() {
        final int count = 50;
        final String result = RandomStringUtils.randomAlphanumeric(count);
        assertNotNull(result);
        assertEquals(count, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Char should be alphanumeric", Character.isLetterOrDigit(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomCountOnly() {
        final int count = 25;
        final String result = RandomStringUtils.random(count);
        assertNotNull(result);
        assertEquals(count, result.length());
    }

    @Test(timeout = 4000)
    public void testRandomWithLettersAndNumbersBooleans() {
        final int count = 30;
        final String lettersOnly = RandomStringUtils.random(count, true, false);
        for (char c : lettersOnly.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }

        final String numbersOnly = RandomStringUtils.random(count, false, true);
        for (char c : numbersOnly.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }

        final String anyAsciiPrintable = RandomStringUtils.random(count, true, true);
        for (char c : anyAsciiPrintable.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithCustomCharArray() {
        final char[] set = new char[]{'a', 'b', 'c', '1'};
        final String result = RandomStringUtils.random(40, 0, set.length, false, false, set);
        assertNotNull(result);
        assertEquals(40, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'a' || c == 'b' || c == 'c' || c == '1');
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithCustomString() {
        final String set = "xyz987";
        final String result = RandomStringUtils.random(35, set);
        assertNotNull(result);
        assertEquals(35, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(set.indexOf(c) != -1);
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithStartEndBounds() {
        final String result = RandomStringUtils.random(20, 'a', 'e' + 1, false, false);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c >= 'a' && c <= 'e');
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroCountReturnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.random(0, true, true));
        assertEquals("", RandomStringUtils.random(0, 0, 10, true, true));
        assertEquals("", RandomStringUtils.random(0, "abc"));
        assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b'}));
        assertEquals("", RandomStringUtils.random(0, (String) null));
        assertEquals("", RandomStringUtils.random(0, (char[]) null));
        assertEquals("", RandomStringUtils.random(0, 0, 0, false, false, null, new Random(42L)));
    }

    @Test(timeout = 4000)
    public void testNullStringDelegation() {
        final String result = RandomStringUtils.random(5, (String) null);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test(timeout = 4000)
    public void testNullCharArrayDelegation() {
        final String result = RandomStringUtils.random(5, (char[]) null);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test(timeout = 4000)
    public void testNullCharsWithZeroBoundsAndFalseBooleans() {
        // Triggers: start == 0 && end == 0 && chars == null && !letters && !numbers
        // end becomes Integer.MAX_VALUE
        final Random deterministicRandom = new Random(100L);
        final String result = RandomStringUtils.random(3, 0, 0, false, false, null, deterministicRandom);
        assertNotNull(result);
        assertEquals(3, result.length());
    }

    @Test(timeout = 4000)
    public void testCharsNotNullWithZeroBounds() {
        // Triggers: start == 0 && end == 0 && chars != null -> end = chars.length
        char[] chars = new char[]{'A', 'B', 'C'};
        final String result = RandomStringUtils.random(6, 0, 0, false, false, chars, new Random(100L));
        assertEquals(6, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'A' || c == 'B' || c == 'C');
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J: LANG-807)
    // =========================================================================

    /**
     * LANG-807: When end <= start, RandomStringUtils should throw IllegalArgumentException
     * with an informative message mentioning 'start' and 'end', rather than leaking
     * an uninformative exception from Random.nextInt().
     */
    @Test(timeout = 4000)
    public void testLANG807() {
        try {
            RandomStringUtils.random(5, 5, 5, false, false);
            fail("IllegalArgumentException expected when start == end");
        } catch (final IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            assertNotNull("Exception message should not be null", msg);
            assertTrue("Message (" + msg + ") must contain 'start'", msg.contains("start"));
            assertTrue("Message (" + msg + ") must contain 'end'", msg.contains("end"));
        }
    }

    @Test(timeout = 4000)
    public void testLANG807_StartGreaterThanEnd() {
        try {
            RandomStringUtils.random(5, 10, 5, false, false);
            fail("IllegalArgumentException expected when start > end");
        } catch (final IllegalArgumentException ex) {
            final String msg = ex.getMessage();
            assertNotNull("Exception message should not be null", msg);
            assertTrue("Message (" + msg + ") must contain 'start'", msg.contains("start"));
            assertTrue("Message (" + msg + ") must contain 'end'", msg.contains("end"));
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountThrowsException() {
        RandomStringUtils.random(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountWithCharsThrowsException() {
        RandomStringUtils.random(-5, new char[]{'a'});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountWithStringThrowsException() {
        RandomStringUtils.random(-5, "abc");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyCharArrayThrowsException() {
        RandomStringUtils.random(5, new char[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyStringThrowsException() {
        RandomStringUtils.random(5, "");
    }

    // =========================================================================
    // Partition E: White-Box Surrogate & Filtering Logic (Full Branch Coverage)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSurrogatePairs_LowSurrogatePairing() {
        // ch in [56320, 57343] (dc00 - dfff)
        // With count=2, count-- becomes 1 -> count != 0, so buffer[1] = low surrogate,
        // count-- becomes 0, buffer[0] = high surrogate (55296 + offset).
        final int lowSurrogate = 56320;
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom(lowSurrogate, 10);
        final String result = RandomStringUtils.random(2, 0, 65536, false, false, null, mockRandom);
        assertEquals(2, result.length());
        assertTrue("First character must be high surrogate", Character.isHighSurrogate(result.charAt(0)));
        assertTrue("Second character must be low surrogate", Character.isLowSurrogate(result.charAt(1)));
        assertEquals((char) lowSurrogate, result.charAt(1));
    }

    @Test(timeout = 4000)
    public void testSurrogatePairs_LowSurrogateAtCountZeroRetries() {
        // When count=1, count-- becomes 0.
        // If low surrogate is chosen and count == 0, count++ is executed (retry).
        // Next pick is 'A' (65).
        final int lowSurrogate = 56320;
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom(lowSurrogate, 'A');
        final String result = RandomStringUtils.random(1, 0, 65536, false, false, null, mockRandom);
        assertEquals(1, result.length());
        assertEquals("A", result);
    }

    @Test(timeout = 4000)
    public void testSurrogatePairs_HighSurrogatePairing() {
        // ch in [55296, 56191] (d800 - db7f)
        // With count=2, count-- becomes 1 -> count != 0, so buffer[1] = low surrogate (56320 + offset),
        // count-- becomes 0, buffer[0] = high surrogate.
        final int highSurrogate = 55296;
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom(highSurrogate, 5);
        final String result = RandomStringUtils.random(2, 0, 65536, false, false, null, mockRandom);
        assertEquals(2, result.length());
        assertTrue("First character must be high surrogate", Character.isHighSurrogate(result.charAt(0)));
        assertTrue("Second character must be low surrogate", Character.isLowSurrogate(result.charAt(1)));
        assertEquals((char) highSurrogate, result.charAt(0));
    }

    @Test(timeout = 4000)
    public void testSurrogatePairs_HighSurrogateAtCountZeroRetries() {
        // When count=1, count-- becomes 0.
        // If high surrogate is chosen and count == 0, count++ is executed (retry).
        // Next pick is 'Z' (90).
        final int highSurrogate = 55296;
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom(highSurrogate, 'Z');
        final String result = RandomStringUtils.random(1, 0, 65536, false, false, null, mockRandom);
        assertEquals(1, result.length());
        assertEquals("Z", result);
    }

    @Test(timeout = 4000)
    public void testSurrogatePairs_PrivateHighSurrogateSkipped() {
        // ch in [56192, 56319] (db80 - dbff)
        // Must increment count and skip.
        final int privateHighSurrogate = 56192;
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom(privateHighSurrogate, 'X');
        final String result = RandomStringUtils.random(1, 0, 65536, false, false, null, mockRandom);
        assertEquals(1, result.length());
        assertEquals("X", result);
    }

    @Test(timeout = 4000)
    public void testFilterRejectionRetries() {
        // Request letters=true, numbers=false.
        // First pick: '1' (digit -> rejected -> count++).
        // Second pick: 'B' (letter -> accepted).
        final DeterministicSequenceRandom mockRandom = new DeterministicSequenceRandom('1', 'B');
        final String result = RandomStringUtils.random(1, 0, 128, true, false, null, mockRandom);
        assertEquals(1, result.length());
        assertEquals("B", result);
    }

    // =========================================================================
    // Partition F: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        final RandomStringUtils instance = new RandomStringUtils();
        assertNotNull("Public JavaBean constructor must produce non-null instance", instance);
    }
}