/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.verification.argumentmatching.ArgumentMatchingTool
 *
 * Decision / Condition Coverage Targets:
 * 1. matchers.size() != arguments.length
 *    - matchers.size() < arguments.length -> returns empty Integer[0]
 *    - matchers.size() > arguments.length -> returns empty Integer[0]
 *    - matchers.size() == arguments.length -> proceeds to loop evaluation
 *
 * 2. Loop & Conditional:
 *    m instanceof ContainsExtraTypeInformation
 *      && !safelyMatches(m, arguments[i])
 *      && toStringEquals(m, arguments[i])
 *      && !((ContainsExtraTypeInformation) m).typeMatches(arguments[i])
 *
 *    Branch 2.1: !(m instanceof ContainsExtraTypeInformation)
 *      -> Not suspicious, skips index.
 *    Branch 2.2: safelyMatches(m, arguments[i]) == true
 *      -> Not suspicious, skips index.
 *    Branch 2.3: safelyMatches(m, arguments[i]) throws Throwable (safely caught in safelyMatches)
 *      -> Returns false from safelyMatches, continues evaluation to toStringEquals.
 *    Branch 2.4: toStringEquals(m, arguments[i]) == false
 *      -> Not suspicious, skips index.
 *    Branch 2.5: ((ContainsExtraTypeInformation) m).typeMatches(arguments[i]) == true
 *      -> Not suspicious, skips index.
 *    Branch 2.6: All conditions met (implements interface, does not match, string equals, type differs)
 *      -> Marked suspicious, index added to result.
 *
 * 3. Defect-Targeted Zone (Defects4J Ground Truth):
 *    - ArgumentMatchingToolTest::shouldWorkFineWhenGivenArgIsNull
 *    - ActualInvocationHasNullArgumentNPEBugTest::shouldAllowPassingNullArgument
 *    Flaw: In toStringEquals(m, arg), calling arg.toString() when arg is null throws NullPointerException.
 *    Targeted Tests: shouldWorkFineWhenGivenArgIsNull, shouldNotThrowNpeWhenMultipleArgsContainNull.
 */

package org.mockito.internal.verification.argumentmatching;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.internal.matchers.ContainsExtraTypeInformation;

@SuppressWarnings("rawtypes")
public class ArgumentMatchingToolGeminiTest {

    // =========================================================================
    // Stub Helper Matchers
    // =========================================================================

    private static class StubMatcherWithExtraInfo extends BaseMatcher<Object> implements ContainsExtraTypeInformation {
        private final Object expected;
        private final boolean typeMatchesValue;
        private final boolean shouldThrowInMatches;

        public StubMatcherWithExtraInfo(Object expected, boolean typeMatchesValue, boolean shouldThrowInMatches) {
            this.expected = expected;
            this.typeMatchesValue = typeMatchesValue;
            this.shouldThrowInMatches = shouldThrowInMatches;
        }

        public StubMatcherWithExtraInfo(Object expected, boolean typeMatchesValue) {
            this(expected, typeMatchesValue, false);
        }

        @Override
        public boolean matches(Object item) {
            if (shouldThrowInMatches) {
                throw new RuntimeException("Forced match exception for testing safelyMatches");
            }
            return expected == null ? item == null : expected.equals(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expected == null ? "null" : expected.toString());
        }

        @Override
        public boolean typeMatches(Object target) {
            return this.typeMatchesValue;
        }

        public Matcher withExtraTypeInfo() {
            return this;
        }
    }

    private static class StubMatcherWithoutExtraInfo extends BaseMatcher<Object> {
        private final Object expected;

        public StubMatcherWithoutExtraInfo(Object expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object item) {
            return expected == null ? item == null : expected.equals(item);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expected == null ? "null" : expected.toString());
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSuspiciousArgumentDetectedWhenTypesMismatchAndStringEquals() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        // Integer 10 vs Long 10L: toString() is both "10", matches is false, typeMatches is false
        Matcher matcher = new StubMatcherWithExtraInfo(new Integer(10), false);
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { new Long(10L) };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(1, suspicious.length);
        assertEquals(Integer.valueOf(0), suspicious[0]);
    }

    @Test(timeout = 4000)
    public void testNonSuspiciousWhenArgumentsActuallyMatch() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Matcher matcher = new StubMatcherWithExtraInfo("exactMatch", true);
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { "exactMatch" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testNonSuspiciousWhenMatcherDoesNotImplementContainsExtraTypeInformation() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Matcher matcher = new StubMatcherWithoutExtraInfo(new Integer(10));
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { new Long(10L) };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testNonSuspiciousWhenStringRepresentationDiffers() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Matcher matcher = new StubMatcherWithExtraInfo(new Integer(10), false);
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { new Long(20L) };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testNonSuspiciousWhenTypeMatchesIsTrue() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        // matches() is false, toStringEquals is true ("10" vs "10"), but typeMatches is true
        Matcher matcher = new StubMatcherWithExtraInfo(new Integer(10), true);
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { new Long(10L) };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testMultipleArgumentsMixedSuspiciousAndNonSuspicious() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();

        Matcher m0 = new StubMatcherWithoutExtraInfo("arg0");
        Matcher m1 = new StubMatcherWithExtraInfo(new Integer(100), false); // Suspicious vs Long(100L)
        Matcher m2 = new StubMatcherWithExtraInfo("exact", true);           // Matches exact
        Matcher m3 = new StubMatcherWithExtraInfo(new Double(5.5), false);  // Suspicious vs "5.5"

        List<Matcher> matchers = Arrays.asList(m0, m1, m2, m3);
        Object[] args = new Object[] { "arg0-diff", new Long(100L), "exact", "5.5" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(2, suspicious.length);
        assertEquals(Integer.valueOf(1), suspicious[0]);
        assertEquals(Integer.valueOf(3), suspicious[1]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyMatchersAndEmptyArguments() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Collections.emptyList();
        Object[] args = new Object[0];

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testMatchersCountLessThanArgumentsCount() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Collections.singletonList((Matcher) new StubMatcherWithoutExtraInfo("a"));
        Object[] args = new Object[] { "a", "b" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    @Test(timeout = 4000)
    public void testMatchersCountGreaterThanArgumentsCount() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        List<Matcher> matchers = Arrays.asList(
            new StubMatcherWithoutExtraInfo("a"),
            new StubMatcherWithoutExtraInfo("b")
        );
        Object[] args = new Object[] { "a" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defect:
     * - ArgumentMatchingToolTest::shouldWorkFineWhenGivenArgIsNull
     * In defective versions, toStringEquals calls arg.toString() on a null argument,
     * causing NullPointerException instead of returning an empty or appropriate array.
     */
    @Test(timeout = 4000)
    public void shouldWorkFineWhenGivenArgIsNull() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        // Matcher expects non-null "testString", but argument is null.
        // matches(null) returns false, leading into toStringEquals(matcher, null).
        Matcher matcher = new StubMatcherWithExtraInfo("testString", false);
        List<Matcher> matchers = Collections.singletonList(matcher);
        Object[] args = new Object[] { null };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(0, suspicious.length);
    }

    /**
     * Targets Defect:
     * - ActualInvocationHasNullArgumentNPEBugTest::shouldAllowPassingNullArgument
     * Multi-argument invocation where one argument is null and another is suspicious.
     */
    @Test(timeout = 4000)
    public void shouldNotThrowNpeWhenMultipleArgsContainNull() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        Matcher matcherNull = new StubMatcherWithExtraInfo("first", false);
        Matcher matcherSuspicious = new StubMatcherWithExtraInfo(new Integer(42), false);

        List<Matcher> matchers = Arrays.asList(matcherNull, matcherSuspicious);
        Object[] args = new Object[] { null, new Long(42L) };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(1, suspicious.length);
        assertEquals(Integer.valueOf(1), suspicious[0]);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSafelyMatchesCatchesRuntimeException() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        // Matcher throws runtime exception during matches() call
        Matcher throwingMatcher = new StubMatcherWithExtraInfo(new Integer(77), false, true);
        List<Matcher> matchers = Collections.singletonList(throwingMatcher);
        Object[] args = new Object[] { new Long(77L) };

        // safelyMatches should catch RuntimeException, return false, and proceed to toStringEquals
        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        assertEquals(1, suspicious.length);
        assertEquals(Integer.valueOf(0), suspicious[0]);
    }

    @Test(timeout = 4000)
    public void testSafelyMatchesCatchesError() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        // Matcher throws an Error (subclass of Throwable)
        Matcher errorMatcher = new BaseMatcher<Object>() implements ContainsExtraTypeInformation {
            @Override
            public boolean matches(Object item) {
                throw new AssertionError("Simulated assertion error");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("errorExpected");
            }

            @Override
            public boolean typeMatches(Object target) {
                return false;
            }
        };

        List<Matcher> matchers = Collections.singletonList(errorMatcher);
        Object[] args = new Object[] { "errorExpected" };

        Integer[] suspicious = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);

        assertNotNull(suspicious);
        // String representation is both "errorExpected", typeMatches is false -> detected as suspicious
        assertEquals(1, suspicious.length);
        assertEquals(Integer.valueOf(0), suspicious[0]);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullMatchersListThrowsNpe() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        tool.getSuspiciouslyNotMatchingArgsIndexes(null, new Object[0]);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNullArgumentsArrayThrowsNpe() {
        ArgumentMatchingTool tool = new ArgumentMatchingTool();
        tool.getSuspiciouslyNotMatchingArgsIndexes(new ArrayList<Matcher>(), null);
    }
}