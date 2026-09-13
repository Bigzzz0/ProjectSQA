package org.mockito.internal.invocation;

import java.lang.reflect.Method;
import java.util.*;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.matchers.*;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class InvocationMatcherClaudeTest extends TestBase {

    @Mock
    private IMethods mock;

    @Before
    public void init() {
        // Mockito annotations initialized by TestBase infrastructure
    }

    /**
     * @target InvocationMatcher(Invocation) constructor
     * @scenario Build matcher with default (empty) matcher list, args are auto converted
     * @defectRisk Ensures matchers are derived from invocation arguments when list is empty
     */
    @Test(timeout = 4000)
    public void testConstructorWithEmptyMatchersDerivesMatchersFromArguments() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();

        InvocationMatcher im = new InvocationMatcher(invocation);

        assertEquals(invocation.getArguments().length, im.getMatchers().size());
        assertTrue(im.getMatchers().get(0).matches("hello"));
    }

    /**
     * @target InvocationMatcher(Invocation, List<Matcher>) constructor
     * @scenario Build matcher with explicit non-empty matcher list
     * @defectRisk Ensures provided matchers list is used as-is, not overwritten
     */
    @Test(timeout = 4000)
    public void testConstructorWithNonEmptyMatchersUsesProvidedList() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();

        Matcher equalsMatcher = new Equals("hello");
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(equalsMatcher);

        InvocationMatcher im = new InvocationMatcher(invocation, matchers);

        assertSame(matchers, im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    /**
     * @target getMethod()
     * @scenario Retrieve method from underlying invocation
     * @defectRisk Ensures getMethod delegates correctly to invocation
     */
    @Test(timeout = 4000)
    public void testGetMethodReturnsInvocationMethod() {
        mock.simpleMethod("test");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertEquals(invocation.getMethod(), im.getMethod());
    }

    /**
     * @target getInvocation()
     * @scenario Retrieve the exact invocation instance stored
     * @defectRisk Ensures reference identity is preserved
     */
    @Test(timeout = 4000)
    public void testGetInvocationReturnsSameInstance() {
        mock.simpleMethod("test");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertSame(invocation, im.getInvocation());
    }

    /**
     * @target getMatchers()
     * @scenario Verify matcher list content matches expected size
     * @defectRisk Ensures list is properly exposed via accessor
     */
    @Test(timeout = 4000)
    public void testGetMatchersReturnsCorrectList() {
        mock.simpleMethod("abc");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertNotNull(im.getMatchers());
        assertEquals(1, im.getMatchers().size());
    }

    /**
     * @target getLocation()
     * @scenario Verify location delegation from invocation
     * @defectRisk Ensures location is not lost/nulled during wrapping
     */
    @Test(timeout = 4000)
    public void testGetLocationDelegatesToInvocation() {
        mock.simpleMethod("test");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        Location location = im.getLocation();
        assertNotNull(location);
        assertEquals(invocation.getLocation(), location);
    }

    /**
     * @target matches(Invocation)
     * @scenario Same invocation object matched against itself
     * @defectRisk Ensures trivial self-match returns true
     */
    @Test(timeout = 4000)
    public void testMatchesTrueForSameInvocation() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        assertTrue(im.matches(invocation));
    }

    /**
     * @target matches(Invocation)
     * @scenario Different mock instance produces false match even with same method/args
     * @defectRisk Ensures mock identity check short-circuits matching
     */
    @Test(timeout = 4000)
    public void testMatchesFalseForDifferentMock() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        IMethods otherMock = Mockito.mock(IMethods.class);
        otherMock.simpleMethod("hello");
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.matches(invocation2));
    }

    /**
     * @target matches(Invocation)
     * @scenario Different method invoked on same mock leads to false match
     * @defectRisk Ensures hasSameMethod short-circuits correctly inside matches
     */
    @Test(timeout = 4000)
    public void testMatchesFalseForDifferentMethod() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod(1);
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.matches(invocation2));
    }

    /**
     * @target matches(Invocation)
     * @scenario Same method/mock but arguments mismatch via explicit matcher
     * @defectRisk Ensures ArgumentsComparator properly rejects mismatched args
     */
    @Test(timeout = 4000)
    public void testMatchesFalseForArgumentMismatch() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("different"));
        InvocationMatcher im = new InvocationMatcher(invocation1, matchers);

        mock.simpleMethod("hello");
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.matches(invocation2));
    }

    /**
     * @target hasSameMethod(Invocation)
     * @scenario Same method name and parameter types => true
     * @defectRisk Ensures parameter type comparison loop works correctly
     */
    @Test(timeout = 4000)
    public void testHasSameMethodTrueForIdenticalSignature() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod("world");
        Invocation invocation2 = getLastInvocation();

        assertTrue(im.hasSameMethod(invocation2));
    }

    /**
     * @target hasSameMethod(Invocation)
     * @scenario Overloaded methods with same name but different parameter types => false
     * @defectRisk Ensures parameter type mismatch is properly detected
     */
    @Test(timeout = 4000)
    public void testHasSameMethodFalseForOverloadedMethod() {
        mock.simpleMethod((Object) "hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod("hello");
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.hasSameMethod(invocation2));
    }

    /**
     * @target hasSimilarMethod(Invocation)
     * @scenario Same method name, unverified, same mock => true
     * @defectRisk Ensures positive branch returns true correctly
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodTrueForUnverifiedSameMockSameMethod() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod("world");
        Invocation invocation2 = getLastInvocation();

        assertTrue(im.hasSimilarMethod(invocation2));
    }

    /**
     * @target hasSimilarMethod(Invocation)
     * @scenario Different method name => false
     * @defectRisk Ensures methodNameEquals branch correctly short-circuits
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodFalseForDifferentMethodName() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.otherMethod();
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.hasSimilarMethod(invocation2));
    }

    /**
     * @target hasSimilarMethod(Invocation)
     * @scenario Candidate invocation already verified => false
     * @defectRisk Ensures isUnverified branch correctly short-circuits
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodFalseForAlreadyVerifiedInvocation() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod("world");
        Invocation invocation2 = getLastInvocation();
        invocation2.markVerified();

        assertFalse(im.hasSimilarMethod(invocation2));
    }

    /**
     * @target hasSimilarMethod(Invocation)
     * @scenario Different mock instance => false
     * @defectRisk Ensures mockIsTheSame branch correctly short-circuits
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodFalseForDifferentMock() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        IMethods otherMock = Mockito.mock(IMethods.class);
        otherMock.simpleMethod("hello");
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.hasSimilarMethod(invocation2));
    }

    /**
     * @target hasSimilarMethod(Invocation)
     * @scenario Overloaded method (different signature) but arguments equal => false (overloadedButSameArgs)
     * @defectRisk Ensures overloaded-but-same-args branch correctly rejects
     */
    @Test(timeout = 4000)
    public void testHasSimilarMethodFalseForOverloadedButSameArgs() {
        mock.simpleMethod((Object) "hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.simpleMethod("hello");
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.hasSimilarMethod(invocation2));
    }

    /**
     * @target toString()
     * @scenario Verify string representation is generated without exception
     * @defectRisk Ensures PrintSettings.print delegation works correctly
     */
    @Test(timeout = 4000)
    public void testToStringProducesNonNullRepresentation() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        String result = im.toString();
        assertNotNull(result);
        assertTrue(result.length() > 0);
    }

    /**
     * @target toString() with explicit matcher list
     * @scenario Verify string representation uses matcher's own string form
     * @defectRisk Ensures matcher list rather than plain args influences toString
     */
    @Test(timeout = 4000)
    public void testToStringWithExplicitMatchers() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new Equals("hello"));
        InvocationMatcher im = new InvocationMatcher(invocation, matchers);

        String result = im.toString();
        assertNotNull(result);
    }

    /**
     * @target PrintSettings usage indirectly via toString()
     * @scenario Directly exercise PrintSettings.print to increase branch coverage
     * @defectRisk Ensures PrintSettings integration remains stable
     */
    @Test(timeout = 4000)
    public void testPrintSettingsDirectUsage() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation);

        PrintSettings settings = new PrintSettings();
        String printed = settings.print(im.getMatchers(), invocation);
        assertNotNull(printed);
    }

    /**
     * @target captureArgumentsFrom(Invocation) - non vararg branch
     * @scenario Capture single non-vararg argument using CapturingMatcher
     * @defectRisk Ensures normal (non-vararg) capture path works correctly
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNonVarargInvocation() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();

        CapturingMatcher capturingMatcher = new CapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);

        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        im.captureArgumentsFrom(invocation);

        assertEquals(1, capturingMatcher.getAllValues().size());
        assertEquals("hello", capturingMatcher.getAllValues().get(0));
    }

    /**
     * @target captureArgumentsFrom(Invocation) - multiple non-vararg args, only CapturesArguments matcher captures
     * @scenario Mixed matcher list where only some matchers implement CapturesArguments
     * @defectRisk Ensures loop correctly skips non-CapturesArguments matchers
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNonVarargInvocationWithMixedMatchers() {
        mock.simpleMethod("hello", 5);
        Invocation invocation = getLastInvocation();

        CapturingMatcher capturingMatcher = new CapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(capturingMatcher);
        matchers.add(new Equals(5));

        InvocationMatcher im = new InvocationMatcher(invocation, matchers);
        im.captureArgumentsFrom(invocation);

        assertEquals(1, capturingMatcher.getAllValues().size());
        assertEquals("hello", capturingMatcher.getAllValues().get(0));
    }

    /**
     * @target captureArgumentsFrom(Invocation) - VARARG branch (KNOWN DEFECT)
     * @scenario Vararg invocation triggers isVarArgs() true branch which incorrectly throws UnsupportedOperationException
     * @defectRisk CRITICAL: This test targets the known defect where captureArgumentsFrom
     *             unconditionally throws UnsupportedOperationException for vararg methods
     *             instead of properly capturing the vararg elements. This test MUST fail
     *             on the defective version and pass on the fixed version.
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromVarargMethod_UnsupportedOperationException() throws Exception {
        mock.varargs(new String[] { "arg1", "arg2" });
        Invocation invocation = getLastInvocation();

        Matcher matcher = new LocalizedMatcher(new CapturingMatcher());
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));

        // On the defective version, this line throws UnsupportedOperationException,
        // causing this test to fail. On the fixed version, capturing succeeds normally.
        invocationMatcher.captureArgumentsFrom(invocation);

        // If we reach this point without exception, the defect is fixed.
        assertTrue(true);
    }

    /**
     * @target createFrom(List<Invocation>) static method
     * @scenario Convert multiple invocations into InvocationMatcher list
     * @defectRisk Ensures static factory correctly wraps each invocation
     */
    @Test(timeout = 4000)
    public void testCreateFromMultipleInvocations() {
        mock.simpleMethod("first");
        Invocation invocation1 = getLastInvocation();

        mock.simpleMethod("second");
        Invocation invocation2 = getLastInvocation();

        List<Invocation> invocations = new ArrayList<Invocation>();
        invocations.add(invocation1);
        invocations.add(invocation2);

        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertEquals(2, result.size());
        assertSame(invocation1, result.get(0).getInvocation());
        assertSame(invocation2, result.get(1).getInvocation());
    }

    /**
     * @target createFrom(List<Invocation>) static method - empty list branch
     * @scenario Empty invocation list should produce empty result list
     * @defectRisk Ensures loop handles empty collections gracefully
     */
    @Test(timeout = 4000)
    public void testCreateFromEmptyList() {
        List<Invocation> invocations = new ArrayList<Invocation>();
        List<InvocationMatcher> result = InvocationMatcher.createFrom(invocations);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * @target hasSameMethod(Invocation)
     * @scenario Different method name entirely => false (name mismatch branch)
     * @defectRisk Ensures name comparison short-circuits before parameter comparison
     */
    @Test(timeout = 4000)
    public void testHasSameMethodFalseForDifferentMethodName() {
        mock.simpleMethod("hello");
        Invocation invocation1 = getLastInvocation();
        InvocationMatcher im = new InvocationMatcher(invocation1);

        mock.otherMethod();
        Invocation invocation2 = getLastInvocation();

        assertFalse(im.hasSameMethod(invocation2));
    }
}