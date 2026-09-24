package org.mockito.internal.invocation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.invocation.InvocationMatcher
 * Known Defects:
 *   - captureArgumentsFrom(Invocation) with varargs methods incorrectly indexes rawArguments:
 *     `invocation.getRawArguments()[position - indexOfVararg]`
 *     When pure varargs or multiple varargs are captured, this results in:
 *       1) ArrayIndexOutOfBoundsException (when matchers.size() exceeds rawArguments.length)
 *       2) Capturing the non-vararg argument (index 0) instead of vararg elements
 *       3) ClassCastException (capturing array instead of unwrapped vararg item)
 *
 * Branches & Decision Points Targeted:
 *   1. InvocationMatcher(Invocation, List<Matcher>)
 *      - Branch matchers.isEmpty() == true  -> ArgumentsProcessor.argumentsToMatchers()
 *      - Branch matchers.isEmpty() == false -> this.matchers = matchers
 *   2. matches(Invocation actual)
 *      - Branch mock.equals(actual.getMock()) -> true/false
 *      - Branch hasSameMethod(actual) -> true/false
 *      - Branch argumentsMatch() -> true/false
 *   3. hasSimilarMethod(Invocation candidate)
 *      - Branch !methodNameEquals -> false
 *      - Branch !isUnverified -> false
 *      - Branch !mockIsTheSame (reference equality: ==) -> false
 *      - Branch overloadedButSameArgs (!methodEquals && safelyArgumentsMatch) -> true/false
 *   4. safelyArgumentsMatch(Object[] actualArgs)
 *      - Normal return
 *      - Exception catch branch -> returns false
 *   5. hasSameMethod(Invocation candidate)
 *      - Name matches vs does not match
 *      - Param length matches vs does not match
 *      - Param types match vs differ at index i
 *   6. captureArgumentsFrom(Invocation invocation)
 *      - Branch isVarArgs() == false
 *        - Matcher is CapturesArguments -> captures argument
 *        - Matcher is NOT CapturesArguments -> skipped
 *      - Branch isVarArgs() == true (DEFECT ZONE)
 *        - Pure vararg invocation with multiple matchers
 *        - Mixed vararg invocation with multiple matchers
 *   7. createFrom(List<Invocation>)
 *      - Empty list vs populated list
 *   8. Serialization contract integrity
 * ---------------------------------------------------------------------------------------------------
 */
public class InvocationMatcherGeminiTest {

    // Target interface providing reflection metadata for testing
    public interface SampleService {
        void simple(String a);
        void simple(Integer a);
        void different(String a);
        void twoArgs(String a, int b);
        void twoArgs(String a, String b);
        void noArg();
        void vararg(String... args);
        void mixedVararg(int count, String... args);
    }

    private static Method getMethod(String name, Class<?>... paramTypes) {
        try {
            return SampleService.class.getMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    // Dynamic invocation handler ensuring 100% deterministic, mock-free Invocation proxies
    private static class DummyInvocationHandler implements InvocationHandler, Serializable {
        private static final long serialVersionUID = 1L;
        private final Object mock;
        private final Method method;
        private final Object[] arguments;
        private final Object[] rawArguments;
        private final Location location;
        private boolean verified;

        public DummyInvocationHandler(Object mock, Method method, Object[] arguments, Object[] rawArguments, Location location, boolean verified) {
            this.mock = mock;
            this.method = method;
            this.arguments = arguments != null ? arguments : new Object[0];
            this.rawArguments = rawArguments != null ? rawArguments : this.arguments;
            this.location = location;
            this.verified = verified;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            String name = m.getName();
            if ("getMock".equals(name)) {
                return mock;
            } else if ("getMethod".equals(name)) {
                return method;
            } else if ("getArguments".equals(name)) {
                return arguments;
            } else if ("getRawArguments".equals(name)) {
                return rawArguments;
            } else if ("getArgumentAt".equals(name)) {
                int idx = (Integer) args[0];
                Class<?> clazz = (Class<?>) args[1];
                return clazz.cast(arguments[idx]);
            } else if ("getLocation".equals(name)) {
                return location;
            } else if ("isVerified".equals(name)) {
                return verified;
            } else if ("markVerified".equals(name)) {
                this.verified = true;
                return null;
            } else if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return "MockInvocation[" + (method != null ? method.getName() : "null") + "]";
            }
            Class<?> returnType = m.getReturnType();
            if (returnType == boolean.class) return false;
            if (returnType == int.class) return 0;
            return null;
        }
    }

    private static Invocation createInvocation(Object mock, Method method, Object[] args, Object[] rawArgs, Location location, boolean verified) {
        return (Invocation) Proxy.newProxyInstance(
            Invocation.class.getClassLoader(),
            new Class<?>[] { Invocation.class },
            new DummyInvocationHandler(mock, method, args, rawArgs, location, verified)
        );
    }

    private static Location createLocation(final String description) {
        return (Location) Proxy.newProxyInstance(
            Location.class.getClassLoader(),
            new Class<?>[] { Location.class },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    if ("toString".equals(method.getName())) {
                        return description;
                    }
                    return null;
                }
            }
        );
    }

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments, Serializable {
        private static final long serialVersionUID = 1L;
        private final List<Object> captured = new ArrayList<Object>();

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("capturingMatcher");
        }

        @Override
        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() {
            return captured;
        }
    }

    private static class NonCapturingMatcher extends BaseMatcher<Object> implements Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean matchResult;

        public NonCapturingMatcher(boolean matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public boolean matches(Object item) {
            return matchResult;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("nonCapturingMatcher(" + matchResult + ")");
        }
    }

    private static class ExceptionalMatcher extends BaseMatcher<Object> implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object item) {
            throw new RuntimeException("Forced matcher exception for safe matching test");
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("exceptionalMatcher");
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithExplicitMatchers() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[] { "test" }, null, createLocation("loc1"), false);
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher(true));

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        assertSame(invocation, invocationMatcher.getInvocation());
        assertSame(method, invocationMatcher.getMethod());
        assertEquals(1, invocationMatcher.getMatchers().size());
        assertSame(matchers.get(0), invocationMatcher.getMatchers().get(0));
    }

    @Test(timeout = 4000)
    public void testConstructorWithImplicitMatchersFromInvocationArguments() {
        Method method = getMethod("twoArgs", String.class, int.class);
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[] { "param", 42 }, null, createLocation("loc2"), false);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation);

        assertSame(invocation, invocationMatcher.getInvocation());
        assertEquals(2, invocationMatcher.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testMatchesSuccessfulWhenMockMethodAndArgumentsMatch() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, method, new Object[] { "value" }, null, createLocation("loc"), false);
        Invocation actual = createInvocation(mock, method, new Object[] { "value" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesFailsWhenMockDiffers() {
        Method method = getMethod("simple", String.class);
        Object mockWanted = "mock1";
        Object mockActual = "mock2";
        Invocation wanted = createInvocation(mockWanted, method, new Object[] { "val" }, null, createLocation("loc"), false);
        Invocation actual = createInvocation(mockActual, method, new Object[] { "val" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesFailsWhenMethodDiffers() {
        Method methodWanted = getMethod("simple", String.class);
        Method methodActual = getMethod("different", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, methodWanted, new Object[] { "val" }, null, createLocation("loc"), false);
        Invocation actual = createInvocation(mock, methodActual, new Object[] { "val" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testMatchesFailsWhenArgumentsDiffer() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, method, new Object[] { "val1" }, null, createLocation("loc"), false);
        Invocation actual = createInvocation(mock, method, new Object[] { "val2" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.matches(actual));
    }

    @Test(timeout = 4000)
    public void testGetLocationReturnsInvocationLocation() {
        Method method = getMethod("noArg");
        Object mock = new Object();
        Location location = createLocation("line 42");
        Invocation invocation = createInvocation(mock, method, new Object[0], null, location, false);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation);
        assertSame(location, invocationMatcher.getLocation());
    }

    @Test(timeout = 4000)
    public void testToStringContainsMethodDescription() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[] { "sample" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(invocation);
        String desc = matcher.toString();
        assertNotNull(desc);
        assertTrue(desc.contains("simple"));
    }

    @Test(timeout = 4000)
    public void testCreateFromList() {
        Method method = getMethod("noArg");
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, method, new Object[0], null, createLocation("loc1"), false);
        Invocation inv2 = createInvocation(mock, method, new Object[0], null, createLocation("loc2"), false);

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(Arrays.asList(inv1, inv2));
        assertEquals(2, matchers.size());
        assertSame(inv1, matchers.get(0).getInvocation());
        assertSame(inv2, matchers.get(1).getInvocation());
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNonVarargsWithCapturesArguments() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[] { "captureMe" }, null, createLocation("loc"), false);

        CapturingMatcher capturingMatcher = new CapturingMatcher();
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(capturingMatcher));

        invocationMatcher.captureArgumentsFrom(invocation);

        assertEquals(1, capturingMatcher.getCaptured().size());
        assertEquals("captureMe", capturingMatcher.getCaptured().get(0));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithEmptyArguments() {
        Method method = getMethod("noArg");
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[0], null, createLocation("loc"), false);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation);
        assertTrue(invocationMatcher.getMatchers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testHasSameMethodDifferentParameterCounts() {
        Method m1 = getMethod("twoArgs", String.class, int.class);
        Method m2 = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, m1, new Object[] { "a", 1 }, null, createLocation("loc"), false);
        Invocation inv2 = createInvocation(mock, m2, new Object[] { "a" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSameMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethodDifferentParameterTypesSameLength() {
        Method m1 = getMethod("twoArgs", String.class, int.class);
        Method m2 = getMethod("twoArgs", String.class, String.class);
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, m1, new Object[] { "a", 1 }, null, createLocation("loc"), false);
        Invocation inv2 = createInvocation(mock, m2, new Object[] { "a", "b" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertFalse(matcher.hasSameMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethodIdenticalSignatures() {
        Method m1 = getMethod("twoArgs", String.class, int.class);
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, m1, new Object[] { "a", 1 }, null, createLocation("loc"), false);
        Invocation inv2 = createInvocation(mock, m1, new Object[] { "x", 99 }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);
        assertTrue(matcher.hasSameMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFromNonVarargsWhenMatcherDoesNotImplementCapturesArguments() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[] { "val" }, null, createLocation("loc"), false);

        NonCapturingMatcher nonCapturing = new NonCapturingMatcher(true);
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, Collections.<Matcher>singletonList(nonCapturing));

        // Should execute smoothly without throwing exceptions or casting
        invocationMatcher.captureArgumentsFrom(invocation);
        assertTrue(nonCapturing.matches("val"));
    }

    @Test(timeout = 4000)
    public void testCreateFromEmptyListReturnsEmpty() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Varargs Argument Capture Bug)
    // Targets: captureArgumentsFrom with varargs methods where rawArguments indexing
    // throws ArrayIndexOutOfBoundsException or captures incorrect values.
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * org.mockitousage.matchers.CapturingArgumentsTest::captures_correctly_when_captor_used_on_pure_vararg_method
     * java.lang.ArrayIndexOutOfBoundsException: 1
     *
     * In defective code:
     *   indexOfVararg = invocation.getRawArguments().length - 1 = 0
     *   position = 1 accesses getRawArguments()[1 - 0] -> getRawArguments()[1] -> IndexOutOfBounds!
     *
     * Expected correct behavior:
     *   Each vararg argument ("first", "second") is captured by its respective matcher.
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromPureVarargsMethod() {
        Method method = getMethod("vararg", String[].class);
        Object mock = new Object();
        String[] varargsArray = new String[] { "first", "second" };
        Object[] rawArgs = new Object[] { varargsArray };
        Object[] expandedArgs = new Object[] { "first", "second" };

        Invocation invocation = createInvocation(mock, method, expandedArgs, rawArgs, createLocation("loc"), false);

        CapturingMatcher matcher1 = new CapturingMatcher();
        CapturingMatcher matcher2 = new CapturingMatcher();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(matcher1);
        matchers.add(matcher2);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        invocationMatcher.captureArgumentsFrom(invocation);

        assertEquals(1, matcher1.getCaptured().size());
        assertEquals("first", matcher1.getCaptured().get(0));

        assertEquals(1, matcher2.getCaptured().size());
        assertEquals("second", matcher2.getCaptured().get(0));
    }

    /**
     * Target Defect Ground Truth:
     * org.mockito.internal.invocation.InvocationMatcherTest::should_capture_varargs_as_vararg
     * junit.framework.AssertionFailedError: expected:<[['a', 'b']]> but was:<[[1]]>
     *
     * In defective code:
     *   indexOfVararg = 2 - 1 = 1
     *   position = 1 accesses getRawArguments()[1 - 1] = getRawArguments()[0] (non-vararg argument 100!)
     */
    @Test(timeout = 4000)
    public void testCaptureArgumentsFromMixedVarargsMethod() {
        Method method = getMethod("mixedVararg", int.class, String[].class);
        Object mock = new Object();
        String[] varargsArray = new String[] { "alpha", "beta" };
        Object[] rawArgs = new Object[] { 100, varargsArray };
        Object[] expandedArgs = new Object[] { 100, "alpha", "beta" };

        Invocation invocation = createInvocation(mock, method, expandedArgs, rawArgs, createLocation("loc"), false);

        CapturingMatcher countMatcher = new CapturingMatcher();
        CapturingMatcher alphaMatcher = new CapturingMatcher();
        CapturingMatcher betaMatcher = new CapturingMatcher();

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(countMatcher);
        matchers.add(alphaMatcher);
        matchers.add(betaMatcher);

        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);

        invocationMatcher.captureArgumentsFrom(invocation);

        assertEquals(1, countMatcher.getCaptured().size());
        assertEquals(100, countMatcher.getCaptured().get(0));

        assertEquals(1, alphaMatcher.getCaptured().size());
        assertEquals("alpha", alphaMatcher.getCaptured().get(0));

        assertEquals(1, betaMatcher.getCaptured().size());
        assertEquals("beta", betaMatcher.getCaptured().get(0));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths (hasSimilarMethod logic)
    // =========================================================================

    @Test(timeout = 4000)
    public void testHasSimilarMethodFailsWhenMethodNameDiffers() {
        Method m1 = getMethod("simple", String.class);
        Method m2 = getMethod("different", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, m1, new Object[] { "a" }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock, m2, new Object[] { "a" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodFailsWhenCandidateAlreadyVerified() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, method, new Object[] { "a" }, null, createLocation("loc"), false);
        Invocation candidateVerified = createInvocation(mock, method, new Object[] { "a" }, null, createLocation("loc"), true);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidateVerified));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodFailsWhenMockInstancesAreDifferent() {
        Method method = getMethod("simple", String.class);
        Object mock1 = "mockInstance1";
        Object mock2 = "mockInstance2";
        Invocation wanted = createInvocation(mock1, method, new Object[] { "a" }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock2, method, new Object[] { "a" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsTrueForExactSameMethodUnverified() {
        Method method = getMethod("simple", String.class);
        Object mock = new Object();
        Invocation wanted = createInvocation(mock, method, new Object[] { "a" }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock, method, new Object[] { "b" }, null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(wanted);
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsFalseWhenOverloadedAndArgumentsMatch() {
        Method wantedMethod = getMethod("simple", Integer.class);
        Method candidateMethod = getMethod("simple", String.class);
        Object mock = new Object();

        Invocation wanted = createInvocation(mock, wantedMethod, new Object[] { 10 }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock, candidateMethod, new Object[] { "value" }, null, createLocation("loc"), false);

        // A matcher that matches any argument value
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher(true));

        InvocationMatcher matcher = new InvocationMatcher(wanted, matchers);

        // Overloaded method where matchers match candidate's argument -> overloadedButSameArgs is true -> returns false
        assertFalse(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethodReturnsTrueWhenOverloadedAndArgumentsDoNotMatch() {
        Method wantedMethod = getMethod("simple", Integer.class);
        Method candidateMethod = getMethod("simple", String.class);
        Object mock = new Object();

        Invocation wanted = createInvocation(mock, wantedMethod, new Object[] { 10 }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock, candidateMethod, new Object[] { "value" }, null, createLocation("loc"), false);

        // A matcher that refuses to match
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher(false));

        InvocationMatcher matcher = new InvocationMatcher(wanted, matchers);

        // Overloaded method where matchers do not match candidate's argument -> overloadedButSameArgs is false -> returns true
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    @Test(timeout = 4000)
    public void testSafelyArgumentsMatchCatchesExceptionAndReturnsFalse() {
        Method wantedMethod = getMethod("simple", Integer.class);
        Method candidateMethod = getMethod("simple", String.class);
        Object mock = new Object();

        Invocation wanted = createInvocation(mock, wantedMethod, new Object[] { 10 }, null, createLocation("loc"), false);
        Invocation candidate = createInvocation(mock, candidateMethod, new Object[] { "value" }, null, createLocation("loc"), false);

        // ExceptionalMatcher throws RuntimeException during matching
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new ExceptionalMatcher());

        InvocationMatcher matcher = new InvocationMatcher(wanted, matchers);

        // safelyArgumentsMatch catches the exception and returns false -> overloadedButSameArgs is false -> returns true
        assertTrue(matcher.hasSimilarMethod(candidate));
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationAndDeserializationIntegrity() throws Exception {
        Method method = getMethod("simple", String.class);
        String mock = "serializableMock";
        Location location = createLocation("serializableLocation");
        Invocation invocation = createInvocation(mock, method, new Object[] { "argValue" }, null, location, false);

        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher(true));

        InvocationMatcher original = new InvocationMatcher(invocation, matchers);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        InvocationMatcher deserialized = (InvocationMatcher) ois.readObject();

        assertNotNull(deserialized);
        assertEquals(original.getMethod().getName(), deserialized.getMethod().getName());
        assertEquals(original.getMatchers().size(), deserialized.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testTypeContracts() {
        Method method = getMethod("noArg");
        Object mock = new Object();
        Invocation invocation = createInvocation(mock, method, new Object[0], null, createLocation("loc"), false);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertTrue(matcher instanceof DescribedInvocation);
        assertTrue(matcher instanceof CapturesArgumensFromInvocation);
        assertTrue(matcher instanceof Serializable);
    }
}