package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/**
 * White-box JUnit 4 test suite for {@link RandomStringUtils}.
 * <p>
 * Branch &amp; Defect Analysis Matrix:
 * <ul>
 *   <li>Branch 1: count == 0 → return ""</li>
 *   <li>Branch 2: count &lt; 0 → throw IllegalArgumentException</li>
 *   <li>Branch 3: chars != null &amp;&amp; chars.length == 0 → throw IllegalArgumentException</li>
 *   <li>Branch 4: start == 0 &amp;&amp; end == 0 → adjust start/end based on chars, letters, numbers</li>
 *   <li>Branch 5: chars == null → ch = random.nextInt(gap) + start</li>
 *   <li>Branch 6: chars != null → ch = chars[random.nextInt(gap) + start]</li>
 *   <li>Branch 7: (letters &amp;&amp; Character.isLetter(ch)) || (numbers &amp;&amp; Character.isDigit(ch)) || (!letters &amp;&amp; !numbers) → continue surrogate handling</li>
 *   <li>Branch 8: else → count++ (skip character)</li>
 *   <li>Sub-branches for surrogates: low (0xDC00-0xDFFF), high (0xD800-0xDB7F), private high (0xDB80-0xDBFF), else normal</li>
 *   <li>Boundary: start &gt; end → gap negative → Random.nextInt throws IllegalArgumentException with "bound must be positive" (missing "start" in message)</li>
 * </ul>
 * Known defect LANG-807: no validation for start > end; exception message lacks "start".
 * This suite targets that defect with explicit tests.
 */
public class RandomStringUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testRandomCountOnly() {
        String result = RandomStringUtils.random(10);
        assertNotNull(result);
        assertEquals(10, result.length());
    }

    @Test(timeout = 4000)
    public void testRandomAsciiLength() {
        String result = RandomStringUtils.randomAscii(5);
        assertEquals(5, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char out of ASCII range: " + (int) c, c >= 32 && c <= 126);
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphabetic() {
        String result = RandomStringUtils.randomAlphabetic(100);
        assertEquals(100, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Not alphabetic: " + c, Character.isLetter(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphanumeric() {
        String result = RandomStringUtils.randomAlphanumeric(50);
        assertEquals(50, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Not alphanumeric: " + c, Character.isLetterOrDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomNumeric() {
        String result = RandomStringUtils.randomNumeric(20);
        assertEquals(20, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Not digit: " + c, Character.isDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithLettersAndNumbers() {
        // Both true: not guaranteed to have both, but should be alphanumeric
        String result = RandomStringUtils.random(10, true, true);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Not letter or digit: " + c, Character.isLetterOrDigit(c));
        }
    }

    @Test(timeout = 4000)
    public void testRandomWithStartEndCharsAndRandom() {
        // Fully controlled via seeded Random
        Random seed = new Random(12345);
        String result = RandomStringUtils.random(4, 0, 10, false, false, new char[]{'a','b','c','d','e','f','g','h','i','j'}, seed);
        assertEquals(4, result.length());
        // deterministic: seed 12345 produces chars? we won't check exact sequence, but ensure length
        for (char c : result.toCharArray()) {
            assertTrue("unexpected char", c >= 'a' && c <= 'j');
        }
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testCountZero() {
        assertEquals("", RandomStringUtils.random(0));
        assertEquals("", RandomStringUtils.randomAscii(0));
        assertEquals("", RandomStringUtils.randomAlphabetic(0));
        assertEquals("", RandomStringUtils.randomAlphanumeric(0));
        assertEquals("", RandomStringUtils.randomNumeric(0));
    }

    @Test(timeout = 4000)
    public void testCountOne() {
        assertEquals(1, RandomStringUtils.random(1).length());
        assertEquals(1, RandomStringUtils.randomAscii(1).length());
    }

    @Test(timeout = 4000)
    public void testNegativeCount() {
        try {
            RandomStringUtils.random(-1);
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("less than 0"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyCharsArray() {
        try {
            RandomStringUtils.random(5, 0, 10, false, false, new char[0]);
            fail("Expected IllegalArgumentException for empty chars array");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("chars array must not be empty"));
        }
    }

    @Test(timeout = 4000)
    public void testStartEndBothZeroLettersNumbersBothFalse() {
        // Should use Integer.MAX_VALUE as end, so random includes all Unicode
        String result = RandomStringUtils.random(100, 0, 0, false, false);
        assertEquals(100, result.length());
    }

    @Test(timeout = 4000)
    public void testStartEndBothZeroWithCharsArray() {
        char[] chars = "abc".toCharArray();
        String result = RandomStringUtils.random(10, 0, 0, false, false, chars);
        assertEquals(10, result.length());
        for (char c : result.toCharArray()) {
            assertTrue("Char not in set", c >= 'a' && c <= 'c');
        }
    }

    // ========== Partition C: Defect-Targeted Branch (LANG-807) ==========

    @Test(timeout = 4000)
    public void testStartGreaterThanEnd_ShouldThrowWithStartInMessage() {
        // Known defect: the method should validate that start <= end,
        // and if start > end, throw IllegalArgumentException with message containing "start".
        try {
            RandomStringUtils.random(5, 10, 5, false, false, (char[]) null);
            fail("Expected IllegalArgumentException when start > end");
        } catch (IllegalArgumentException e) {
            // The defect version throws "bound must be positive" without "start".
            // This test expects the correct behavior (fixed version) to include "start".
            assertTrue("Exception message should contain 'start'", e.getMessage().contains("start"));
        }
    }

    @Test(timeout = 4000)
    public void testStartGreaterThanEndWithLetters() {
        try {
            RandomStringUtils.random(5, 10, 5, true, false, (char[]) null);
            fail("Expected IllegalArgumentException when start > end");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should contain 'start'", e.getMessage().contains("start"));
        }
    }

    @Test(timeout = 4000)
    public void testStartGreaterThanEndWithCharsArray() {
        try {
            RandomStringUtils.random(5, 5, 3, false, false, new char[]{'a','b','c','d','e'});
            fail("Expected IllegalArgumentException when start > end");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should contain 'start'", e.getMessage().contains("start"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testNullCharsViaStringMethod() {
        String result = RandomStringUtils.random(3, (String) null);
        assertEquals(3, result.length());
    }

    @Test(timeout = 4000)
    public void testEmptyStringParameter() {
        try {
            RandomStringUtils.random(5, "");
            fail("Expected IllegalArgumentException for empty string");
        } catch (IllegalArgumentException e) {
            // Should mention "chars" or "string"?
            assertTrue(e.getMessage().contains("empty"));
        }
    }

    @Test(timeout = 4000)
    public void testNullCharArrayMethod() {
        String result = RandomStringUtils.random(3, (char[]) null);
        assertEquals(3, result.length());
    }

    @Test(timeout = 4000)
    public void testCountNegativeInFullMethod() {
        try {
            RandomStringUtils.random(-2, 0, 10, false, false, null, new Random());
            fail("Expected IllegalArgumentException for negative count");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("less than 0"));
        }
    }

    @Test(timeout = 4000)
    public void testEmptyCharsExplicitRandom() {
        try {
            RandomStringUtils.random(3, 0, 5, false, false, new char[0], new Random());
            fail("Expected IllegalArgumentException for empty chars array");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("chars array must not be empty"));
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructorIsPublic() {
        // Ensure constructor is accessible (though not typically used)
        new RandomStringUtils();
    }

    @Test(timeout = 4000)
    public void testDeterministicWithSeededRandom() {
        // Verify that two calls with the same seed produce the same string
        Random seed1 = new Random(42);
        Random seed2 = new Random(42);
        String result1 = RandomStringUtils.random(10, 0, 100, false, false, null, seed1);
        String result2 = RandomStringUtils.random(10, 0, 100, false, false, null, seed2);
        assertEquals("Same seed should produce same result", result1, result2);
    }

    @Test(timeout = 4000)
    public void testRandomAsciiBounds() {
        // Ensure randomAscii never produces characters outside 32-126
        for (int i = 0; i < 1000; i++) {
            String s = RandomStringUtils.randomAscii(10);
            for (char c : s.toCharArray()) {
                assertTrue("Char out of ASCII bound", c >= 32 && c <= 126);
            }
        }
    }

    @Test(timeout = 4000)
    public void testRandomAlphabeticContainsOnlyLetters() {
        for (int i = 0; i < 100; i++) {
            String s = RandomStringUtils.randomAlphabetic(50);
            for (char c : s.toCharArray()) {
                assertTrue("Not letter: " + c, Character.isLetter(c));
            }
        }
    }

    // Additional coverage for surrogate handling with seeded Random
    @Test(timeout = 4000)
    public void testSurrogateLowHighWithSeededRandom() {
        // Create a random that will generate chars in low surrogate range (56320-57343)
        // We'll use a custom random that forces a specific sequence.
        // For simplicity, we'll just call the method with a range that includes surrogates
        // and rely on deterministic random.
        Random r = new Random(54321);
        String result = RandomStringUtils.random(200, 56320, 57344, false, false, null, r);
        assertEquals(200, result.length());
        // The result may contain low surrogates and high surrogates; just check length
        // More precise would be to mock Random, but we'll keep it simple.
    }

    @Test(timeout = 4000)
    public void testPrivateHighSurrogateSkipped() {
        // Private high surrogates 56192-56319 are skipped (count++)
        // Use a range that includes them and ensure no crash
        Random r = new Random(9999);
        String result = RandomStringUtils.random(50, 56192, 56320, false, false, null, r);
        assertEquals(50, result.length());
    }
}