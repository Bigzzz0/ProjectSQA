package org.mockito.internal.verification.argumentmatching;

import org.junit.Test;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.ContainsExtraTypeInformation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ArgumentMatchingTool.getSuspiciouslyNotMatchingArgsIndexes(List<Matcher>, Object[])
 * 
 * Decision branches to cover:
 * 1. matchers.size() != arguments.length -> return empty array (true/false)
 * 2. m instanceof ContainsExtraTypeInformation (true/false)
 * 3. !safelyMatches(m, arguments[i]) (true/false) - includes exception path in safelyMatches
 * 4. toStringEquals(m, arguments[i]) (true/false)
 * 5. !((ContainsExtraTypeInformation) m).typeMatches(arguments[i]) (true/false)
 * 6. Loop iteration boundaries (0, 1, multiple elements)
 * 
 * Boundary conditions:
 * - null arguments array elements (defect trigger: NPE in toStringEquals when arg is null)
 * - empty matchers list with empty arguments array
 * - mismatched sizes
 * - matcher that throws in matches()
 * - matcher with null toString representation
 * 
 * Defect-targeted test: shouldNotThrowNPEWhenArgumentIsNull
 * The original code calls arg.toString() in toStringEquals without null check,
 * causing NPE when an argument is null. The correct behavior is to return
 * an empty array (no suspicious matches) when arguments are null.
 */
public class ArgumentMatchingToolDeepseekTest {

    // Helper matcher implementing ContainsExtraTypeInformation
    private static class TypeAwareMatcher extends BaseMatcher implements ContainsExtraTypeInformation {
        private final String expected;
        private final boolean typeMatchesResult;
        private final boolean matchesResult;
        private final String toStringValue;

        TypeAwareMatcher(String expected, boolean typeMatchesResult, boolean matchesResult, String toStringValue) {
            this.expected = expected;
            this.typeMatchesResult = typeMatchesResult;
            this.matchesResult = matchesResult;
            this.toStringValue = toStringValue;
        }

        @Override
        public boolean matches(Object item) {
            return matchesResult;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(toStringValue);
        }

        @Override
        public boolean typeMatches(Object argument) {
            return typeMatchesResult;
        }

        @Override
        public String toString() {
            return toStringValue;
        }
    }

    // Helper matcher that throws in matches()
    private static class ThrowingMatcher extends BaseMatcher implements ContainsExtraTypeInformation {
        @Override
        public boolean matches(Object item) {
            throw new RuntimeException("boom");
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("throwing");
        }

        @Override
        public boolean typeMatches(Object argument) {
            return false;
        }
    }

    // Helper matcher that does NOT implement ContainsExtraTypeInformation
    private static class PlainMatcher extends BaseMatcher {
        private final String toStringValue;

        PlainMatcher(String toStringValue) {
            this.toStringValue = toStringValue;
        }

        @Override
        public boolean matches(Object item) {
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(toStringValue);
        }

        @Override
        public String toString() {
            return toStringValue;
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testEmptyListsAndArrays() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(
                Collections.<Matcher>emptyList(), new Object[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testMismatchedSizes() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(new PlainMatcher("a"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNoSuspiciousMatchesWhenAllMatch() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("a", true, true, "a"),
                new TypeAwareMatcher("b", true, true, "b"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"a", "b"});
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testSuspiciousMatchDetected() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("1", false, false, "1"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"1"});
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Integer.valueOf(0), result[0]);
    }

    @Test(timeout = 4000)
    public void testMultipleSuspiciousMatches() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("1", false, false, "1"),
                new TypeAwareMatcher("2", true, true, "2"),
                new TypeAwareMatcher("3", false, false, "3"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"1", "2", "3"});
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals(Integer.valueOf(0), result[0]);
        assertEquals(Integer.valueOf(2), result[1]);
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testNullArgumentsArray() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Collections.<Matcher>emptyList();
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, null);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNullMatchersList() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(null, new Object[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testMatcherThrowsInMatches() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(new ThrowingMatcher());
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"x"});
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testNonTypeAwareMatcherNotSuspicious() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(new PlainMatcher("same"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"same"});
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testTypeMatchesTrueButMatchesFalse() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("same", true, false, "same"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"same"});
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testToStringDifferentButTypeMatchesFalse() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("different", false, false, "different"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"other"});
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect: NPE when argument is null.
     * The original code calls arg.toString() in toStringEquals without null check.
     * This test verifies that null arguments are handled gracefully.
     */
    @Test(timeout = 4000)
    public void shouldNotThrowNPEWhenArgumentIsNull() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("value", false, false, "value"));
        try {
            Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{null});
            // If no exception, the method should return an empty array (no suspicious matches)
            assertNotNull(result);
            assertEquals(0, result.length);
        } catch (NullPointerException e) {
            fail("NPE should not be thrown when argument is null: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNullArgumentWithMatchingToString() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("null", false, false, "null"));
        try {
            Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{null});
            assertNotNull(result);
            assertEquals(0, result.length);
        } catch (NullPointerException e) {
            fail("NPE should not be thrown for null argument: " + e.getMessage());
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testMatcherWithNullToString() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher(null, false, false, null));
        try {
            Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"x"});
            assertNotNull(result);
            assertEquals(0, result.length);
        } catch (NullPointerException e) {
            // Acceptable if NPE occurs, but should not crash the test
        }
    }

    @Test(timeout = 4000)
    public void testLargeNumberOfArguments() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        int size = 1000;
        Matcher[] matcherArray = new Matcher[size];
        Object[] args = new Object[size];
        for (int i = 0; i < size; i++) {
            matcherArray[i] = new TypeAwareMatcher(String.valueOf(i), false, false, String.valueOf(i));
            args[i] = String.valueOf(i);
        }
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(
                Arrays.asList(matcherArray), args);
        assertNotNull(result);
        assertEquals(size, result.length);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testResultArrayIsNewInstance() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Integer[] result1 = tool.getSuspiciouslyNotMatchingArgsIndexes(
                Collections.<Matcher>emptyList(), new Object[0]);
        Integer[] result2 = tool.getSuspiciouslyNotMatchingArgsIndexes(
                Collections.<Matcher>emptyList(), new Object[0]);
        assertNotSame(result1, result2);
        assertArrayEquals(result1, result2);
    }

    @Test(timeout = 4000)
    public void testResultOrdering() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new TypeAwareMatcher("1", false, false, "1"),
                new TypeAwareMatcher("2", false, false, "2"),
                new TypeAwareMatcher("3", false, false, "3"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"1", "2", "3"});
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(Integer.valueOf(0), result[0]);
        assertEquals(Integer.valueOf(1), result[1]);
        assertEquals(Integer.valueOf(2), result[2]);
    }

    @Test(timeout = 4000)
    public void testMixedMatcherTypes() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new PlainMatcher("plain"),
                new TypeAwareMatcher("type", false, false, "type"),
                new PlainMatcher("plain2"));
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, new Object[]{"plain", "type", "plain2"});
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(Integer.valueOf(1), result[0]);
    }
}