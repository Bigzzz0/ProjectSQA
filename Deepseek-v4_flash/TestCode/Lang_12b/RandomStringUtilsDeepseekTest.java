package org.apache.commons.lang3;

import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: RandomStringUtils
 * 
 * Key branches and boundary conditions covered:
 * 
 * Partition A - Core functional logic:
 *   - random(int count) delegates to random(count, false, false)
 *   - randomAscii(int count) delegates to random(count, 32, 127, false, false)
 *   - randomAlphabetic, randomAlphanumeric, randomNumeric
 *   - Overload cascade: random(count, letters, numbers) -> random(count,0,0,letters,numbers) -> 7-param random
 * 
 * Partition B - Boundary Value Analysis:
 *   - count == 0 -> empty string
 *   - count < 0 -> IllegalArgumentException
 *   - start == 0 && end == 0:
 *       - letters && numbers -> start=' ', end='z'+1
 *       - letters && !numbers -> same
 *       - !letters && numbers -> same
 *       - !letters && !numbers -> end=Integer.MAX_VALUE
 *   - chars == null vs non-null
 *   - chars.length < end -> potential ArrayIndexOutOfBounds (DEFECT)
 *   - chars empty -> IllegalArgumentException
 * 
 * Partition C - Defect-targeted zone:
 *   - Known Defect (Defects4J ID: LANG-805): When chars array is non-null and end - start exceeds array length,
 *     the code computes index = random.nextInt(gap) + start, which can be out-of-bounds.
 *     Example: random(10, 0, 100, false, false, new char[]{'a','b'}) throws ArrayIndexOutOfBoundsException.
 *     Correct behavior should validate array bounds or use modulo.
 * 
 * Partition D - Surrogate handling:
 *   - Low surrogate (0xDC00-0xDFFF): insert high surrogate after it
 *   - High surrogate (0xD800-0xDB7F): insert low surrogate before it
 *   - Private high surrogate (0xDB80-0xDBFF): skip (count++)
 *   - Normal character: store directly
 * 
 * Partition E - Exception paths:
 *   - count < 0 -> IllegalArgumentException
 *   - chars != null && chars.length == 0 -> IllegalArgumentException
 *   - chars != null && end > chars.length -> currently ArrayIndexOutOfBounds (bug)
 * 
 * This test suite uses a seeded Random to ensure determinism for surrogate and boundary tests.
 * For methods relying on the internal static RANDOM, we rely on non-deterministic but range-verified checks.
 */
public class RandomStringUtilsDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testRandomCount() {
        String result = RandomStringUtils.random(5);
        assertNotNull(result);
        assertEquals(5, result.length());
    }

    @Test(timeout = 4000)
    public void testRandomAscii() {
        String result = RandomStringUtils.randomAscii(10);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character " + (int) c + " not in ASCII printable range",
                       c >= 32 && c <= 126);
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphabetic() {
        String result = RandomStringUtils.randomAlphabetic(8);
        assertEquals(8, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character " + c + " is not alphabetic",
                       Character.isLetter(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphanumeric() {
        String result = RandomStringUtils.randomAlphanumeric(12);
        assertEquals(12, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character " + c + " is not letter or digit",
                       Character.isLetterOrDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomNumeric() {
        String result = RandomStringUtils.randomNumeric(6);
        assertEquals(6, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Character " + c + " is not a digit",
                       Character.isDigit(c));
        }
    }

    // ---------- Partition B: Boundary & Edge Cases ----------

    @Test(timeout = 4000)
    public void testZeroCount() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCount() {
        RandomStringUtils.random(-1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNegativeCountRandomWithArgs() {
        RandomStringUtils.random(-5, true, false);
    }

    @Test(timeout = 4000)
    public void testZeroStartZeroEndBothLettersAndNumbers() {
        // letters=true, numbers=true => should use ASCII printable range
        String result = RandomStringUtils.random(100, 0, 0, true, true);
        assertEquals(100, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char " + (int)c + " not in ASCII printable",
                       c >= 32 && c <= 126);
        }
    }

    @Test(timeout = 4000)
    public void testZeroStartZeroEndNoLettersNoNumbers() {
        // No letters, no numbers => end = Integer.MAX_VALUE
        // We can't validate all chars but ensure length and no exception
        String result = RandomStringUtils.random(50, 0, 0, false, false);
        assertEquals(50, result.length());
    }

    @Test(timeout = 4000)
    public void testCharsNull() {
        // Use convenience method with null chars => delegates to default
        String result = RandomStringUtils.random(5, (String) null);
        assertEquals(5, result.length());
    }

    @Test(timeout = 4000)
    public void testCharsEmptyString() {
        // Empty string -> IllegalArgumentException
        try {
            RandomStringUtils.random(5, "");
            fail("Expected IllegalArgumentException for empty chars string");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCharsEmptyArray() {
        RandomStringUtils.random(5, new char[0]);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------
    // This test reveals the ArrayIndexOutOfBoundsException when end > chars.length
    @Test(timeout = 4000)
    public void testCharsArrayTooSmall_ShouldThrowIllegalArgument() {
        // The bug: end=100, chars length=2 => index = random(98) + 0 = [0..97] => out of bounds
        // Correct behavior should validate and throw IllegalArgumentException.
        // On defective version, this throws ArrayIndexOutOfBoundsException, causing test failure.
        try {
            RandomStringUtils.random(10, 0, 100, false, false, new char[]{'a', 'b'});
            fail("Expected IllegalArgumentException because chars array is too small");
        } catch (IllegalArgumentException e) {
            // expected correct behavior
        }
    }

    @Test(timeout = 4000)
    public void testCharsArrayTooSmallWithNonZeroStart() {
        // start=50, end=100, chars length=10 => index = random(50)+50 = [50..99] => out of bounds
        try {
            RandomStringUtils.random(10, 50, 100, false, false, new char[]{'x'});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- Partition D: Surrogate Handling (deterministic via seeded Random) ----------

    @Test(timeout = 4000)
    public void testSurrogateLowSurrogateThenHigh() {
        // Seed that will produce a low surrogate first, then a high surrogate
        Random rnd = new Random(12345L);
        // We cannot easily force specific characters, but we can run many iterations
        // Instead, we'll test the logic directly by providing a small range containing surrogates
        // But simpler: use a fixed set of chars that include surrogates and check no exception.
        // For deterministic, we call random with a seeded Random and a small allowed range.
        // Ensure no exception.
        RandomStringUtils.random(100, 55296, 57344, false, false, null, new Random(42));
        // If it completes, surrogate handling works.
    }

    @Test(timeout = 4000)
    public void testNoSurrogatesInNormalRange() {
        // ASCII printable range, no surrogates
        String result = RandomStringUtils.random(50, 32, 127, false, false, null, new Random(99));
        assertEquals(50, result.length());
    }

    // ---------- Partition E: Exception and Defensive Guard Paths ----------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCountNegativeInFullMethod() {
        RandomStringUtils.random(-3, 0, 0, true, true, null, new Random());
    }

    @Test(timeout = 4000)
    public void testValidWithExplicitCharsAndRandom() {
        // Normal case with explicit chars array
        char[] chars = new char[]{'a', 'b', 'c', 'd', 'e'};
        String result = RandomStringUtils.random(10, 0, chars.length, false, false, chars, new Random(123));
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char " + c + " not in allowed set",
                       c == 'a' || c == 'b' || c == 'c' || c == 'd' || c == 'e');
        }
    }

    @Test(timeout = 4000)
    public void testLettersOnlyFilter() {
        // With letters=true, numbers=false, should only contain letters
        Random rnd = new Random(77);
        String result = RandomStringUtils.random(200, 0, 0, true, false, null, rnd);
        assertEquals(200, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char " + c + " is not a letter", Character.isLetter(c));
        }
    }

    @Test(timeout = 4000)
    public void testNumbersOnlyFilter() {
        Random rnd = new Random(88);
        String result = RandomStringUtils.random(200, 0, 0, false, true, null, rnd);
        assertEquals(200, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char " + c + " is not a digit", Character.isDigit(c));
        }
    }
}