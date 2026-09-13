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

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mockito.internal.invocation.InvocationMatcher
 * Known Defect (Mockito-1 / Issue 188):
 * - Method captureArgumentsFrom(Invocation) directly executes `throw new UnsupportedOperationException()`
 *   when invocation.getMethod().isVarArgs() is true. The expected production behavior is to properly
 *   unpack and capture array/vararg arguments.
 *
 * Decision / Condition Matrix:
 * 1. Constructor InvocationMatcher(Invocation, List<Matcher>):
 *    - Branch: matchers.isEmpty() == true -> invokes ArgumentsProcessor.argumentsToMatchers(...)
 *    - Branch: matchers.isEmpty() == false -> stores provided matchers list directly.
 * 2. getMethod(), getInvocation(), getMatchers(), getLocation():
 *    - Direct delegation to underlying Invocation and Matcher collection.
 * 3. matches(Invocation actual):
 *    - Condition 1: invocation.getMock().equals(actual.getMock()) [T / F]
 *    - Condition 2: hasSameMethod(actual) [T / F]
 *    - Condition 3: new ArgumentsComparator().argumentsMatch(this, actual) [T / F]
 * 4. hasSameMethod(Invocation candidate):
 *    - Condition: m1.getName() != null && m1.getName().equals(m2.getName()) [T / F]
 *    - Condition: params1.length == params2.length [T / F]
 *    - Loop condition: params1[i] != params2[i] [Different types -> false, all same -> true]
 * 5. hasSimilarMethod(Invocation candidate):
 *    - Condition: !methodNameEquals || !isUnverified || !mockIsTheSame -> return false
 *    - Condition: overloadedButSameArgs (!methodEquals && safelyArgumentsMatch) -> return false
 *    - Default: return true
 * 6. captureArgumentsFrom(Invocation invocation):
 *    - Non-vararg branch: iterates matchers, detects CapturesArguments, passes argumentAt.
 *    - Vararg branch: defective implementation throws UnsupportedOperationException.
 * 7. createFrom(List<Invocation>):
 *    - Iterates candidate invocations and wraps each in an InvocationMatcher.
 * ====================================================================================================
 */
public class InvocationMatcherGeminiTest extends TestBase {

    @Mock
    private IMethods mock;

    @Mock
    private IMethods otherMock;

    @Before
    public void setUp() {
        // Initializes mocks declared via @Mock in TestBase
    }

    // ================================================================================================
    // Partition A: Initialization & Matchers Processing
    // ================================================================================================

    @Test(timeout = 4000)
    public void testInitializationWithEmptyMatchersConvertsArguments() {
        mock.simpleMethod("foo");
        Invocation invocation = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals(invocation.getMethod(), matcher.getMethod());
        assertSame(invocation, matcher.getInvocation());
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
        assertTrue(matcher.getMatchers().get(0) instanceof Equals);
        assertEquals("foo", ((Equals) matcher.getMatchers().get(0)).getWanted());
    }

    @Test(timeout = 4000)
    public void testInitializationWithExplicitMatchers() {
        mock.simpleMethod("foo");
        Invocation invocation = getLastInvocation();

        Matcher explicitMatcher = NotNull.NOT_NULL;
        InvocationMatcher matcher = new InvocationMatcher(invocation, Collections.singletonList(explicitMatcher));

        assertEquals(1, matcher.getMatchers().size());
        assertSame(explicitMatcher, matcher.getMatchers().get(0));
    }

    @Test(timeout = 4000)
    public void testGetLocationDelegatesToInvocation() {
        mock.simpleMethod("foo");
        Invocation invocation = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        Location location = matcher.getLocation();

        assertNotNull(location);
        assertEquals(invocation.getLocation().toString(), location.toString());
    }

    @Test(timeout = 4000)
    public void testCreateFromList() {
        mock.simpleMethod("one");
        Invocation inv1 = getLastInvocation();
        mock.simpleMethod("two");
        Invocation inv2 = getLastInvocation();

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(asList(inv1, inv2));

        assertEquals(2, matchers.size());
        assertSame(inv1, matchers.get(0).getInvocation());
        assertSame(inv2, matchers.get(1).getInvocation());
    }

    // ================================================================================================
    // Partition B: Invocation Matching Logic (matches)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testMatchesReturnsTrueWhenIdentical() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        mock.simpleMethod("test");
        Invocation actual = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenDifferentMock() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        otherMock.simpleMethod("test");
        Invocation actual = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenDifferentMethod() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        mock.differentMethod();
        Invocation actual = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesReturnsFalseWhenDifferentArguments() {
        mock.simpleMethod("expected");
        Invocation wanted = getLastInvocation();
        mock.simpleMethod("different");
        Invocation actual = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesWithNullArguments() {
        mock.simpleMethod((String) null);
        Invocation wanted = getLastInvocation();
        mock.simpleMethod((String) null);
        Invocation actual = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.matches(actual));

        mock.simpleMethod("notNull");
        Invocation nonNullActual = getLastInvocation();
        assertFalse(matcher.matches(nonNullActual));
    }

    // ================================================================================================
    // Partition C: Method Similarity & Overload Equality
    // ================================================================================================

    @Test(timeout = 4000)
    public void testHasSameMethodIdentical() {
        mock.simpleMethod("a");
        Invocation inv1 = getLastInvocation();
        mock.simpleMethod("b");
        Invocation inv2 = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertTrue(matcher.hasSameMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethodDifferentParameterTypes() {
        mock.twoArgumentMethod(1, 2);
        Invocation inv1 = getLastInvocation();
        mock.simpleMethod("string");
        Invocation inv2 = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSameMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethodDifferentParamLengthSameName() {
        mock.simpleMethod();
        Invocation noArgs = getLastInvocation();
        mock.simpleMethod("arg");
        Invocation withArg = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(noArgs);
        assertFalse(matcher.hasSameMethod(withArg));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodSameUnverifiedMethod() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        mock.simpleMethod("differentArg");
        Invocation candidate = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenDifferentMethodName() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        mock.differentMethod();
        Invocation candidate = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenDifferentMock() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        otherMock.simpleMethod("test");
        Invocation candidate = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenAlreadyVerified() {
        mock.simpleMethod("test");
        Invocation wanted = getLastInvocation();
        mock.simpleMethod("test");
        Invocation candidate = getLastInvocation();

        candidate.markVerified();

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenOverloadedWithSameArguments() throws Exception {
        // Subclass with overloaded method signatures where arguments can match
        OverloadedMethods overloadedMock = Mockito.mock(OverloadedMethods.class);
        overloadedMock.method((Object) "stringArg");
        Invocation objectArgInvocation = getLastInvocation();
        overloadedMock.method("stringArg");
        Invocation stringArgInvocation = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(objectArgInvocation);
        // Overloaded methods with same argument values should return false for similarity
        assertFalse(matcher.hasSameMethod(stringArgInvocation));
        assertFalse(matcher.hasSimilarMethod(stringArgInvocation));
    }

    interface OverloadedMethods {
        void method(Object obj);
        void method(String str);
    }

    // ================================================================================================
    // Partition D: Printing & Rendering
    // ================================================================================================

    @Test(timeout = 4000)
    public void testToStringRendersMatcherAndArguments() {
        mock.simpleMethod("hello");
        Invocation invocation = getLastInvocation();

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String rendered = matcher.toString();

        assertNotNull(rendered);
        assertTrue("Expected rendered string to contain method name", rendered.contains("simpleMethod"));
        assertTrue("Expected rendered string to contain argument value", rendered.contains("hello"));
    }

    // ================================================================================================
    // Partition E: Argument Capture & Defect Zone (Mockito-1 / Issue 188)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNonVarargMethod() {
        mock.simpleMethod("capturedValue");
        Invocation invocation = getLastInvocation();

        CapturingMatcher<String> capturingMatcher = new CapturingMatcher<String>();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(capturingMatcher));

        invocationMatcher.captureArgumentsFrom(invocation);

        assertEquals("capturedValue", capturingMatcher.getLastValue());
    }

    /**
     * Fault-revealing test for Mockito-1 defect (Issue 188).
     * On the defective version, captureArgumentsFrom on a vararg method throws UnsupportedOperationException.
     * On the repaired version, arguments are successfully captured from the vararg array.
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromVarargMethod_UnsupportedOperationException() {
        mock.varargs(new String[] { "arg1", "arg2" });
        Invocation invocation = getLastInvocation();

        Matcher matcher = new LocalizedMatcher(new CapturingMatcher());
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(matcher));

        invocationMatcher.captureArgumentsFrom(invocation);
    }
}