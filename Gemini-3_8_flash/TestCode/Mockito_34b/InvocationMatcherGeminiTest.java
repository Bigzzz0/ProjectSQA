/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.invocation.InvocationMatcher
 * Known Defect (Defects4J): ArrayIndexOutOfBoundsException in captureArgumentsFrom() when actual
 *                          invocation arguments count is less than matchers count (e.g. empty varargs).
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor:
 *    - matchers.isEmpty() == true  -> derives matchers via invocation.argumentsToMatchers()
 *    - matchers.isEmpty() == false -> retains supplied matchers list
 *    - InvocationMatcher(Invocation) delegating constructor
 * 2. matches(Invocation actual):
 *    - invocation.getMock().equals(actual.getMock()) == false -> short-circuit returns false
 *    - hasSameMethod(actual) == false                         -> short-circuit returns false
 *    - argumentsMatch(this, actual) == false                  -> returns false
 *    - all match == true                                      -> returns true
 * 3. hasSameMethod(Invocation candidate):
 *    - invocation.getMethod().equals(candidate.getMethod()) == true  -> returns true
 *    - invocation.getMethod().equals(candidate.getMethod()) == false -> returns false
 * 4. hasSimilarMethod(Invocation candidate):
 *    - !methodNameEquals == true (names differ)                      -> returns false
 *    - !isUnverified == true (candidate already verified)            -> returns false
 *    - !mockIsTheSame == true (mock instance differs via ==)         -> returns false
 *    - overloadedButSameArgs == true (!methodEquals && argsMatch)    -> returns false
 *    - overloadedButSameArgs == false (methodEquals or args differ)  -> returns true
 * 5. safelyArgumentsMatch(Object[] actualArgs):
 *    - argumentsMatch succeeds without exception                     -> returns boolean result
 *    - argumentsMatch throws Throwable (e.g. Matcher exception)      -> caught, returns false
 * 6. captureArgumentsFrom(Invocation i):
 *    - m instanceof CapturesArguments == true                        -> invokes captureFrom
 *    - m instanceof CapturesArguments == false                       -> skips capture
 *    - matchers.size() > i.getArguments().length (DEFECT ZONE)      -> triggers ArrayIndexOutOfBoundsException on buggy code
 * 7. Getters & String Representations:
 *    - getMethod(), getInvocation(), getMatchers(), getLocation()
 *    - toString() and toString(PrintSettings)
 */
package org.mockito.internal.invocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;

public class InvocationMatcherGeminiTest {

    public interface SampleOverloadA {
        void doAction(String param);
    }

    public interface SampleOverloadB {
        void doAction(String param);
    }

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        private Object captured;
        private int captureCount = 0;

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            if (description != null) {
                description.appendText("capturing matcher");
            }
        }

        @Override
        public void captureFrom(Object argument) {
            this.captured = argument;
            this.captureCount++;
        }

        public Object getCaptured() {
            return captured;
        }

        public int getCaptureCount() {
            return captureCount;
        }
    }

    private static class DummyMatcher extends BaseMatcher<Object> {
        private final boolean matchResult;

        public DummyMatcher(boolean matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public boolean matches(Object item) {
            return matchResult;
        }

        @Override
        public void describeTo(Description description) {
            if (description != null) {
                description.appendText("dummy matcher");
            }
        }
    }

    private static class ThrowingMatcher extends BaseMatcher<Object> {
        @Override
        public boolean matches(Object item) {
            throw new RuntimeException("Simulated exception during argument match evaluation");
        }

        @Override
        public void describeTo(Description description) {
            if (description != null) {
                description.appendText("throwing matcher");
            }
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorDelegationAndImplicitMatchersCreation() {
        Invocation invocation = new InvocationBuilder().args("alpha", "beta").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame(invocation, matcher.getInvocation());
        assertEquals(invocation.getMethod(), matcher.getMethod());
        assertNotNull(matcher.getMatchers());
        assertEquals(2, matcher.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testConstructorWithExplicitMatchersRetained() {
        Invocation invocation = new InvocationBuilder().args("alpha").toInvocation();
        DummyMatcher dummyMatcher = new DummyMatcher(true);
        List<Matcher> explicitMatchers = Collections.singletonList(dummyMatcher);

        InvocationMatcher matcher = new InvocationMatcher(invocation, explicitMatchers);

        assertSame(invocation, matcher.getInvocation());
        assertEquals(1, matcher.getMatchers().size());
        assertSame(dummyMatcher, matcher.getMatchers().get(0));
    }

    @Test(timeout = 4000)
    public void testConstructorFallbackWhenEmptyMatchersProvided() {
        Invocation invocation = new InvocationBuilder().args("alpha").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.<Matcher>emptyList());

        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testGetLocationPropagatesInvocationLocation() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        Location location = matcher.getLocation();
        assertEquals(invocation.getLocation(), location);
    }

    @Test(timeout = 4000)
    public void testToStringWithDefaultAndExplicitPrintSettings() {
        Invocation invocation = new InvocationBuilder().args("testArg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String defaultString = matcher.toString();
        assertNotNull(defaultString);
        assertTrue(defaultString.contains("testArg") || defaultString.contains("simpleMethod"));

        PrintSettings printSettings = new PrintSettings();
        String settingsString = matcher.toString(printSettings);
        assertNotNull(settingsString);
        assertEquals(defaultString, settingsString);
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsTrueWhenMockMethodAndArgumentsMatch() {
        Object sharedMock = "sharedMock";
        Invocation inv1 = new InvocationBuilder().mock(sharedMock).args("matchVal").toInvocation();
        Invocation inv2 = new InvocationBuilder().mock(sharedMock).args("matchVal").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertTrue(matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenArgumentsDoNotMatch() {
        Object sharedMock = "sharedMock";
        Invocation inv1 = new InvocationBuilder().mock(sharedMock).args("expectedVal").toInvocation();
        Invocation inv2 = new InvocationBuilder().mock(sharedMock).args("differentVal").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenMethodDiffers() {
        Object sharedMock = "sharedMock";
        Invocation inv1 = new InvocationBuilder().mock(sharedMock).toInvocation();
        Invocation inv2 = new InvocationBuilder().mock(sharedMock).differentMethod().toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenMockDiffers() {
        Invocation inv1 = new InvocationBuilder().mock("mockAlpha").args("val").toInvocation();
        Invocation inv2 = new InvocationBuilder().mock("mockBeta").args("val").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethodBranching() {
        Invocation base = new InvocationBuilder().toInvocation();
        Invocation sameMethod = new InvocationBuilder().toInvocation();
        Invocation differentMethod = new InvocationBuilder().differentMethod().toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(base);
        assertTrue(matcher.hasSameMethod(sameMethod));
        assertFalse(matcher.hasSameMethod(differentMethod));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsTrueForSameMockMethodAndUnverifiedCandidate() {
        Object sharedMock = "mockInstance";
        Invocation wanted = new InvocationBuilder().mock(sharedMock).args("wantedArg").toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).args("otherArg").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenMethodNamesDiffer() {
        Object sharedMock = "mockInstance";
        Invocation wanted = new InvocationBuilder().mock(sharedMock).toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).differentMethod().toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenCandidateIsAlreadyVerified() {
        Object sharedMock = "mockInstance";
        Invocation wanted = new InvocationBuilder().mock(sharedMock).toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).verified().toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenMockReferenceNotIdentical() {
        // hasSimilarMethod checks reference equality (==), so two distinct strings fail
        String mockA = new String("mockIdenticalContent");
        String mockB = new String("mockIdenticalContent");

        Invocation wanted = new InvocationBuilder().mock(mockA).toInvocation();
        Invocation candidate = new InvocationBuilder().mock(mockB).toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenOverloadedWithIdenticalArguments() throws Exception {
        Method methodA = SampleOverloadA.class.getMethod("doAction", String.class);
        Method methodB = SampleOverloadB.class.getMethod("doAction", String.class);
        Object sharedMock = "sharedMock";

        Invocation wanted = new InvocationBuilder().mock(sharedMock).method(methodA).args("sameParam").toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).method(methodB).args("sameParam").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsTrueWhenOverloadedWithDifferentArguments() throws Exception {
        Method methodA = SampleOverloadA.class.getMethod("doAction", String.class);
        Method methodB = SampleOverloadB.class.getMethod("doAction", String.class);
        Object sharedMock = "sharedMock";

        Invocation wanted = new InvocationBuilder().mock(sharedMock).method(methodA).args("paramA").toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).method(methodB).args("paramB").toInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testSafelyArgumentsMatchCatchesThrowableInSimilarMethodCheck() throws Exception {
        Method methodA = SampleOverloadA.class.getMethod("doAction", String.class);
        Method methodB = SampleOverloadB.class.getMethod("doAction", String.class);
        Object sharedMock = "sharedMock";

        Invocation wanted = new InvocationBuilder().mock(sharedMock).method(methodA).args("val").toInvocation();
        Invocation candidate = new InvocationBuilder().mock(sharedMock).method(methodB).args("val").toInvocation();

        InvocationMatcher matcherWithException = new InvocationMatcher(wanted, Collections.singletonList(new ThrowingMatcher()));
        assertTrue(matcherWithException.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFromCapturesWhenMatcherImplementsCapturesArguments() {
        CapturingMatcher capturingMatcher1 = new CapturingMatcher();
        DummyMatcher dummyMatcher = new DummyMatcher(true);
        CapturingMatcher capturingMatcher2 = new CapturingMatcher();

        Invocation base = new InvocationBuilder().args("a", "b", "c").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(base, Arrays.asList(capturingMatcher1, dummyMatcher, capturingMatcher2));

        Invocation actual = new InvocationBuilder().args("first", "second", "third").toInvocation();
        matcher.captureArgumentsFrom(actual);

        assertEquals("first", capturingMatcher1.getCaptured());
        assertEquals(1, capturingMatcher1.getCaptureCount());
        assertEquals("third", capturingMatcher2.getCaptured());
        assertEquals(1, capturingMatcher2.getCaptureCount());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvocationMatcherWithEmptyArguments() {
        Invocation invocation = new InvocationBuilder().args().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(0, matcher.getMatchers().size());
        Invocation actual = new InvocationBuilder().args().toInvocation();
        assertTrue(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testInvocationMatcherWithNullArgument() {
        Invocation invocation = new InvocationBuilder().args(new Object[] { null }).toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(1, matcher.getMatchers().size());
        Invocation actual = new InvocationBuilder().args(new Object[] { null }).toInvocation();
        assertTrue(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testInvocationMatcherWithMultipleMixedTypes() {
        Invocation invocation = new InvocationBuilder().args(100, "text", 3.14159, true, null).toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(5, matcher.getMatchers().size());
        Invocation actualMatching = new InvocationBuilder().args(100, "text", 3.14159, true, null).toInvocation();
        assertTrue(matcher.matches(actualMatching));

        Invocation actualNonMatching = new InvocationBuilder().args(100, "text", 3.14159, false, null).toInvocation();
        assertFalse(matcher.matches(actualNonMatching));
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFromEmptyMatchersList() {
        Invocation invocation = new InvocationBuilder().args().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, new ArrayList<Matcher>());

        Invocation actual = new InvocationBuilder().args("val").toInvocation();
        matcher.captureArgumentsFrom(actual);
        assertEquals(0, matcher.getMatchers().size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Target Defect 1:
     * org.mockito.internal.invocation.InvocationMatcherTest::shouldMatchCaptureArgumentsWhenArgsCountDoesNOTMatch
     * In defective versions, looping over matchers when actual invocation has fewer arguments triggers
     * java.lang.ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void shouldMatchCaptureArgumentsWhenArgsCountDoesNOTMatch() {
        InvocationMatcher m = new InvocationBuilder().args("a", "b").toInvocationMatcher();
        Invocation i = new InvocationBuilder().args("a").toInvocation();

        m.captureArgumentsFrom(i);
    }

    /**
     * Defects4J Target Defect 2:
     * org.mockitousage.basicapi.UsingVarargsTest::shouldMatchEasilyEmptyVararg
     * Triggering ArrayIndexOutOfBoundsException: 0 when actual invocation arguments are empty.
     */
    @Test(timeout = 4000)
    public void shouldMatchEasilyEmptyVararg() {
        InvocationMatcher m = new InvocationBuilder().args("a").toInvocationMatcher();
        Invocation i = new InvocationBuilder().args().toInvocation();

        m.captureArgumentsFrom(i);
    }

    @Test(timeout = 4000)
    public void shouldNotThrowArrayIndexOutOfBoundsWhenExplicitCapturingMatchersExceedActualArgs() {
        CapturingMatcher firstMatcher = new CapturingMatcher();
        CapturingMatcher secondMatcher = new CapturingMatcher();
        Invocation wanted = new InvocationBuilder().args("alpha", "beta").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(wanted, Arrays.asList(firstMatcher, secondMatcher));

        Invocation actualWithSingleArg = new InvocationBuilder().args("onlySingleArg").toInvocation();
        matcher.captureArgumentsFrom(actualWithSingleArg);

        assertEquals("onlySingleArg", firstMatcher.getCaptured());
    }

    @Test(timeout = 4000)
    public void shouldNotThrowArrayIndexOutOfBoundsWhenActualArgsAreCompletelyEmpty() {
        CapturingMatcher capturingMatcher = new CapturingMatcher();
        Invocation wanted = new InvocationBuilder().args("alpha").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(wanted, Collections.singletonList(capturingMatcher));

        Invocation actualEmpty = new InvocationBuilder().args().toInvocation();
        matcher.captureArgumentsFrom(actualEmpty);

        assertEquals(0, capturingMatcher.getCaptureCount());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void matchesShouldThrowNullPointerExceptionWhenActualIsNull() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        try {
            matcher.matches(null);
            fail("Expected NullPointerException when actual invocation is null");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void hasSimilarMethodShouldThrowNullPointerExceptionWhenCandidateIsNull() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        try {
            matcher.hasSimilarMethod(null);
            fail("Expected NullPointerException when candidate is null");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void hasSameMethodShouldThrowNullPointerExceptionWhenCandidateIsNull() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation);

        try {
            matcher.hasSameMethod(null);
            fail("Expected NullPointerException when candidate is null");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void captureArgumentsFromShouldThrowNullPointerExceptionWhenInvocationIsNullAndMatcherCaptures() {
        CapturingMatcher cm = new CapturingMatcher();
        Invocation invocation = new InvocationBuilder().args("arg").toInvocation();
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(cm));

        try {
            matcher.captureArgumentsFrom(null);
            fail("Expected NullPointerException when capturing from null invocation");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testObjectIdentityAndContractIntegrity() {
        Invocation invocation = new InvocationBuilder().toInvocation();
        InvocationMatcher matcher1 = new InvocationMatcher(invocation);
        InvocationMatcher matcher2 = new InvocationMatcher(invocation);

        assertEquals(matcher1, matcher1);
        assertFalse(matcher1.equals(null));
        assertFalse(matcher1.equals("differentType"));
        assertFalse(matcher1.equals(matcher2));
        assertEquals(matcher1.hashCode(), matcher1.hashCode());
    }
}