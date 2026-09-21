package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.lang3.RandomStringUtils
 *
 * Core Decision Branches & Boundaries:
 * 1. count validation:
 *    - count == 0 -> return ""
 *    - count < 0  -> throw IllegalArgumentException
 *    - count > 0  -> standard generation
 * 2. start / end range default fallback:
 *    - start == 0 && end == 0:
 *      * !letters && !numbers -> start = 0, end = Integer.MAX_VALUE (or chars.length in fixed version)
 *      * letters || numbers   -> start = ' ', end = 'z' + 1
 *    - non-zero start / end -> user specified bounds
 * 3. Character source:
 *    - chars == null -> full Unicode/specified range using random.nextInt(gap) + start
 *    - chars != null -> subset indexing chars[random.nextInt(gap) + start]
 * 4. Filtering predicate:
 *    - letters && isLetter(ch)
 *    - numbers && isDigit(ch)
 *    - !letters && !numbers
 *    - else -> reject character, retry (count++)
 * 5. Surrogate Handling:
 *    - Low surrogate (56320 - 57343): count == 0 vs count > 0 (paired with high surrogate)
 *    - High surrogate (55296 - 56191): count == 0 vs count > 0 (paired with low surrogate)
 *    - Private high surrogate (56192 - 56319): rejected/skipped (count++)
 *    - Non-surrogate regular character
 *
 * Defects4J Ground Truth Vulnerability (LANG-805 / testExceptions):
 * - Invoking random methods with start == 0, end == 0 and a non-null/empty chars array causes
 *   end to be erroneously set to Integer.MAX_VALUE or ('z' + 1). When indexing chars[...],
 *   an ArrayIndexOutOfBoundsException is thrown instead of either utilizing chars.length or
 *   throwing IllegalArgumentException for empty arrays.
 */
public class RandomStringUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testRandomAscii() {
        int length = 100;
        String result = RandomStringUtils.randomAscii(length);
        assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            char ch = result.charAt(i);
            assertTrue("Expected ASCII printable >= 32", ch >= 32);
            assertTrue("Expected ASCII printable <= 126", ch <= 126);
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphabetic() {
        int length = 50;
        String result = RandomStringUtils.randomAlphabetic(length);
        assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Expected alphabetic character", Character.isLetter(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphanumeric() {
        int length = 50;
        String result = RandomStringUtils.randomAlphanumeric(length);
        assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Expected letter or digit", Character.isLetterOrDigit(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomNumeric() {
        int length = 50;
        String result = RandomStringUtils.randomNumeric(length);
        assertEquals(length, result.length());
        for (int i = 0; i < result.length(); i++) {
            assertTrue("Expected digit", Character.isDigit(result.charAt(i)));
        }
    }

    @Test(timeout = 4000)
    public void testRandomCountOnly() {
        int length = 30;
        String result = RandomStringUtils.random(length);
        assertEquals(length, result.length());
    }

    @Test(timeout = 4000)
    public void testRandomLettersNumbersOverloads() {
        String result = RandomStringUtils.random(20, true, false);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isLetter(c));
        }

        result = RandomStringUtils.random(20, false, true);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(Character.isDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithSpecificRange() {
        int count = 25;
        String result = RandomStringUtils.random(count, 'a', 'g' + 1, false, false);
        assertEquals(count, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char should be within 'a'..'g'", c >= 'a' && c <= 'g');
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithFixedSeedDeterministic() {
        Random rand1 = new Random(987654321L);
        Random rand2 = new Random(987654321L);
        String s1 = RandomStringUtils.random(25, 0, 0, true, true, null, rand1);
        String s2 = RandomStringUtils.random(25, 0, 0, true, true, null, rand2);
        assertEquals("Identically seeded random instances must yield identical strings", s1, s2);
    }

    @Test(timeout = 4000)
    public void testRandomWithStringAndCharArrayArgs() {
        String allowed = "ABC123";
        String s1 = RandomStringUtils.random(15, allowed);
        assertEquals(15, s1.length());
        for (char c : s1.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0);
        }

        String s2 = RandomStringUtils.random(15, allowed.toCharArray());
        assertEquals(15, s2.length());
        for (char c : s2.toCharArray()) {
            assertTrue(allowed.indexOf(c) >= 0);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroCountReturnsEmptyString() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.random(0, true, true));
        assertEquals("", RandomStringUtils.random(0, 0, 0, false, false));
        assertEquals("", RandomStringUtils.random(0, "ABC"));
        assertEquals("", RandomStringUtils.random(0, new char[]{'a', 'b'}));
        assertEquals("", RandomStringUtils.random(0, 0, 5, false, false, new char[]{'a'}, new Random(1L)));
    }

    @Test(timeout = 4000)
    public void testNullCharArrayAndStringFallbacks() {
        String fromNullString = RandomStringUtils.random(5, (String) null);
        assertEquals(5, fromNullString.length());

        String fromNullChars = RandomStringUtils.random(5, (char[]) null);
        assertEquals(5, fromNullChars.length());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-805 & ArrayIndexOutOfBounds)
    // =========================================================================

    /**
     * LANG-805: When start == 0 and end == 0 with a non-null char[] array,
     * end must default to chars.length, not Integer.MAX_VALUE or ('z' + 1).
     * On defective versions, this throws ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testLANG805WithLettersNumbersFalse() {
        Random fixedRandom = new Random(42L);
        char[] chars = new char[]{'x', 'y', 'z'};
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars, fixedRandom);
        assertNotNull(result);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue(c == 'x' || c == 'y' || c == 'z');
        }
    }

    @Test(timeout = 4000)
    public void testLANG805WithLettersNumbersTrue() {
        Random fixedRandom = new Random(42L);
        char[] chars = new char[]{'k'};
        String result = RandomStringUtils.random(5, 0, 0, true, true, chars, fixedRandom);
        assertNotNull(result);
        assertEquals("kkkkk", result);
    }

    /**
     * testExceptions from LANG ground truth: Empty array or empty string specification
     * must throw IllegalArgumentException, not ArrayIndexOutOfBoundsException.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyCharArrayThrowsIllegalArgumentException() {
        RandomStringUtils.random(5, new char[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEmptyStringThrowsIllegalArgumentException() {
        RandomStringUtils.random(5, "");
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountThrowsIllegalArgumentException() {
        RandomStringUtils.random(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountWithParametersThrowsIllegalArgumentException() {
        RandomStringUtils.random(-5, 0, 10, true, true, null, new Random(1L));
    }

    // =========================================================================
    // Partition E: Surrogate Pair & Unicode Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testLowSurrogateHandling() {
        // Range containing low surrogates: 56320 (0xDC00) to 57343 (0xDFFF)
        char lowSurrogate = 56320;
        char[] chars = new char[]{lowSurrogate};
        // Request count of 2 so pair insertion succeeds
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, new Random(12345L));
        assertEquals(2, result.length());
        assertTrue(Character.isHighSurrogate(result.charAt(0)));
        assertEquals(lowSurrogate, result.charAt(1));
    }

    @Test(timeout = 4000)
    public void testHighSurrogateHandling() {
        // Range containing standard high surrogates: 55296 (0xD800) to 56191 (0xDB7F)
        char highSurrogate = 55296;
        char[] chars = new char[]{highSurrogate};
        // Request count of 2 so pair insertion succeeds
        String result = RandomStringUtils.random(2, 0, chars.length, false, false, chars, new Random(12345L));
        assertEquals(2, result.length());
        assertEquals(highSurrogate, result.charAt(0));
        assertTrue(Character.isLowSurrogate(result.charAt(1)));
    }

    @Test(timeout = 4000)
    public void testPrivateHighSurrogateSkipping() {
        // Private high surrogate: 56192 (0xDB80) to 56319 (0xDBFF)
        char privateSurrogate = 56200;
        char regularChar = 'A';
        char[] chars = new char[]{privateSurrogate, regularChar};
        // Must filter out private high surrogate and only return regularChar
        String result = RandomStringUtils.random(3, 0, chars.length, false, false, chars, new Random(12345L));
        assertEquals("AAA", result);
    }

    @Test(timeout = 4000)
    public void testSurrogateBufferBoundaryCountZero() {
        // If count == 0 after decrement (meaning count was originally 1 when surrogate was chosen),
        // the code does count++ to re-sample a non-surrogate character or fill buffer
        char highSurrogate = 55296;
        char validAscii = 'Z';
        char[] chars = new char[]{highSurrogate, validAscii};
        String result = RandomStringUtils.random(1, 0, chars.length, false, false, chars, new Random(99L));
        assertEquals(1, result.length());
        assertEquals('Z', result.charAt(0));
    }

    // =========================================================================
    // Partition F: Object Lifecycle & JavaBean Contract
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        RandomStringUtils instance = new RandomStringUtils();
        assertNotNull(instance);
    }
}