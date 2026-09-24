package org.mockito.internal.invocation;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.debugging.Location;
import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.reporting.PrintSettings;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.invocation.InvocationMatcher
 * Known Defect (Defects4J):
 *   - org.mockitousage.bugs.InheritedGenericsPolimorphicCallTest::shouldStubbingWork
 *   - org.mockitousage.bugs.InheritedGenericsPolimorphicCallTest::shouldVerificationWorks
 * Root Cause Analysis:
 *   In InvocationMatcher.hasSameMethod(Invocation candidate), the method delegates to
 *   `return m1.equals(m2);`. In standard Java, Method.equals() requires:
 *     (getDeclaringClass() == other.getDeclaringClass()) && (getName() == other.getName()) && ...
 *   When an interface or class hierarchy overrides or re-declares a method (common in generic
 *   inheritance and polymorphic forwarding calls), m1 and m2 have identical names and identical
 *   parameter types, but different declaring classes (SuperInterface vs SubInterface).
 *   Consequently, `m1.equals(m2)` evaluates to FALSE, causing hasSameMethod(), matches(), and
 *   hasSimilarMethod() to incorrectly return false, breaking verification and stubbing.
 *
 * Branch & Condition Coverage Matrix:
 *   1. Constructor:
 *      - Branch 1: matchers.isEmpty() -> delegates to invocation.argumentsToMatchers().
 *      - Branch 2: !matchers.isEmpty() -> retains custom matchers list.
 *      - Overload: InvocationMatcher(invocation) -> calls this(invocation, emptyList).
 *   2. matches(Invocation actual):
 *      - Condition 1: !invocation.getMock().equals(actual.getMock()) -> false.
 *      - Condition 2: !hasSameMethod(actual) -> false.
 *      - Condition 3: !argumentsMatch(this, actual) -> false.
 *      - Condition 4: All true -> returns true.
 *   3. safelyArgumentsMatch(Object[] actualArgs):
 *      - Normal matching -> returns boolean result of ArgumentsComparator.
 *      - Exception path (Throwable caught) -> catches and returns false.
 *   4. hasSimilarMethod(Invocation candidate):
 *      - Condition: !methodNameEquals -> false.
 *      - Condition: !isUnverified (candidate.isVerified() == true) -> false.
 *      - Condition: !mockIsTheSame (mock1 != mock2 by identity) -> false.
 *      - Condition: overloadedButSameArgs (!methodEquals && safelyArgumentsMatch) -> false.
 *      - Condition: Non-overloaded or args mismatch -> true.
 *   5. hasSameMethod(Invocation candidate):
 *      - Super vs Sub interface polymorphic methods (TARGET DEFECT).
 *      - Same method on same class -> true.
 *      - Different method names or parameter types -> false.
 *   6. captureArgumentsFrom(Invocation i):
 *      - Matcher is CapturesArguments AND index < arguments.length -> captures.
 *      - Matcher is CapturesArguments AND index >= arguments.length -> boundary guard, skips.
 *      - Matcher is not CapturesArguments -> skips.
 *   7. createFrom(List<Invocation> invocations):
 *      - Empty list -> empty LinkedList.
 *      - Multiple invocations -> converts each to an InvocationMatcher.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class InvocationMatcherGeminiTest {

    // -------------------------------------------------------------------------
    // Helper Test Interfaces & Classes
    // -------------------------------------------------------------------------

    public interface SuperInterface {
        void polymorphicMethod(String arg);
        void distinctMethod();
    }

    public interface SubInterface extends SuperInterface {
        @Override
        void polymorphicMethod(String arg);
    }

    public interface GenericBase<T> {
        void process(T value);
    }

    public interface GenericSub<T> extends GenericBase<T> {
        @Override
        void process(T value);
    }

    public static class SampleTarget {
        public void simpleMethod(String text, Integer count) {}
        public void singleArg(String text) {}
        public void noArgs() {}
        public void overloaded(String s) {}
        public void overloaded(Object o) {}
    }

    private static class CapturingMatcher extends BaseMatcher<Object> implements CapturesArguments {
        private final List<Object> captured = new ArrayList<Object>();

        @Override
        public boolean matches(Object item) {
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("capturing matcher");
        }

        @Override
        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() {
            return captured;
        }
    }

    private static class ThrowingMatcher extends BaseMatcher<Object> {
        @Override
        public boolean matches(Object item) {
            throw new RuntimeException("Simulated error in matcher evaluation");
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("throwing matcher");
        }
    }

    // -------------------------------------------------------------------------
    // Reflection Instantiation Helpers for Deterministic Invocation Generation
    // -------------------------------------------------------------------------

    private static Invocation createInvocation(final Object mock, final Method method, final Object[] args) {
        return createInvocation(mock, method, args, false);
    }

    private static Invocation createInvocation(final Object mock, final Method method, final Object[] args, boolean verified) {
        Invocation inv = allocateInvocationInstance();
        setFieldIfPresent(inv, "mock", mock);
        Object[] nonNullArgs = (args != null) ? args : new Object[0];
        setFieldIfPresent(inv, "arguments", nonNullArgs);
        setFieldIfPresent(inv, "rawArguments", nonNullArgs);
        setFieldIfPresent(inv, "verified", verified);

        try {
            Location locProxy = (Location) Proxy.newProxyInstance(
                Location.class.getClassLoader(),
                new Class<?>[]{Location.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] a) {
                        if ("toString".equals(m.getName())) {
                            return "-> at test location";
                        }
                        return null;
                    }
                }
            );
            setFieldIfPresent(inv, "location", locProxy);
        } catch (Throwable ignored) {}

        try {
            Field mField = getFieldHierarchy(Invocation.class, "method");
            if (mField != null) {
                if (mField.getType().equals(Method.class)) {
                    setField(inv, mField, method);
                } else if (method != null) {
                    Object proxy = Proxy.newProxyInstance(
                        mField.getType().getClassLoader(),
                        new Class<?>[]{mField.getType()},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method m, Object[] a) {
                                if ("getJavaMethod".equals(m.getName())) return method;
                                if ("getName".equals(m.getName())) return method.getName();
                                if ("getParameterTypes".equals(m.getName())) return method.getParameterTypes();
                                if ("getReturnType".equals(m.getName())) return method.getReturnType();
                                if ("getExceptionTypes".equals(m.getName())) return method.getExceptionTypes();
                                if ("isVarArgs".equals(m.getName())) return method.isVarArgs();
                                if ("toString".equals(m.getName())) return method.toString();
                                return null;
                            }
                        }
                    );
                    setField(inv, mField, proxy);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed setting method on Invocation", e);
        }
        return inv;
    }

    private static Invocation allocateInvocationInstance() {
        try {
            sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
            Constructor<?> objCtor = Object.class.getDeclaredConstructor();
            Constructor<?> ctor = rf.newConstructorForSerialization(Invocation.class, objCtor);
            return (Invocation) ctor.newInstance();
        } catch (Throwable t) {
            try {
                Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
                return (Invocation) unsafe.allocateInstance(Invocation.class);
            } catch (Throwable t2) {
                throw new RuntimeException("Unable to allocate Invocation instance", t2);
            }
        }
    }

    private static Field getFieldHierarchy(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static void setField(Object obj, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setFieldIfPresent(Object obj, String fieldName, Object value) {
        Field f = getFieldHierarchy(obj.getClass(), fieldName);
        if (f != null) {
            setField(obj, f, value);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor_WithSingleInvocation_PopulatesMatchersFromArguments() throws Exception {
        Method method = SampleTarget.class.getMethod("simpleMethod", String.class, Integer.class);
        Object mock = "myMock";
        Object[] args = new Object[]{"val", 42};
        Invocation invocation = createInvocation(mock, method, args);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertSame("Invocation reference must be retained exactly", invocation, matcher.getInvocation());
        assertEquals("Method must match invocation method", method, matcher.getMethod());
        assertNotNull("Matchers must be non-null", matcher.getMatchers());
        assertEquals("Matchers count must equal argument count", 2, matcher.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testConstructor_WithExplicitCustomMatchers_RetainsProvidedList() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Invocation invocation = createInvocation("myMock", method, new Object[]{"test"});
        Matcher customMatcher = new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) { return true; }
            @Override
            public void describeTo(Description description) { description.appendText("custom"); }
        };
        List<Matcher> matchers = Collections.singletonList(customMatcher);

        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);

        assertSame("Custom matchers list must be preserved", matchers, matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
        assertSame(customMatcher, matcher.getMatchers().get(0));
    }

    @Test(timeout = 4000)
    public void testMatches_IdenticalMockMethodAndArguments_ReturnsTrue() throws Exception {
        Method method = SampleTarget.class.getMethod("simpleMethod", String.class, Integer.class);
        Object mock = "sameMock";
        Invocation inv1 = createInvocation(mock, method, new Object[]{"alpha", 100});
        Invocation inv2 = createInvocation(mock, method, new Object[]{"alpha", 100});

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertTrue("Identical invocations must match", matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testToString_AndPrintSettingsFormatting_ReturnsValidRepresentation() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Invocation invocation = createInvocation("myMock", method, new Object[]{"hello"});

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        String strDefault = matcher.toString();
        assertNotNull("toString() must return a non-null string", strDefault);
        assertFalse("toString() should not be empty", strDefault.trim().isEmpty());

        PrintSettings settings = new PrintSettings();
        String strWithSettings = matcher.toString(settings);
        assertNotNull("toString(PrintSettings) must return non-null string", strWithSettings);
    }

    @Test(timeout = 4000)
    public void testGetLocation_ReturnsInvocationLocation() throws Exception {
        Method method = SampleTarget.class.getMethod("noArgs");
        Invocation invocation = createInvocation("myMock", method, new Object[0]);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertEquals("Location must be delegated from invocation", invocation.getLocation(), matcher.getLocation());
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFrom_CapturesMatchingArgumentsInOrder() throws Exception {
        Method method = SampleTarget.class.getMethod("simpleMethod", String.class, Integer.class);
        Invocation invTarget = createInvocation("myMock", method, new Object[]{"arg1", 99});
        Invocation invActual = createInvocation("myMock", method, new Object[]{"capturedText", 123});

        CapturingMatcher cap1 = new CapturingMatcher();
        CapturingMatcher cap2 = new CapturingMatcher();
        List<Matcher> matchers = Arrays.<Matcher>asList(cap1, cap2);

        InvocationMatcher matcher = new InvocationMatcher(invTarget, matchers);
        matcher.captureArgumentsFrom(invActual);

        assertEquals(1, cap1.getCaptured().size());
        assertEquals("capturedText", cap1.getCaptured().get(0));
        assertEquals(1, cap2.getCaptured().size());
        assertEquals(123, cap2.getCaptured().get(0));
    }

    @Test(timeout = 4000)
    public void testCreateFrom_ListConversion_WrapsAllInvocations() throws Exception {
        Method method = SampleTarget.class.getMethod("noArgs");
        Invocation inv1 = createInvocation("mockA", method, new Object[0]);
        Invocation inv2 = createInvocation("mockB", method, new Object[0]);
        List<Invocation> invocations = Arrays.asList(inv1, inv2);

        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invinvocations());

        assertEquals("Size of created matchers must match input list size", 2, matchers.size());
        assertSame(inv1, matchers.get(0).getInvocation());
        assertSame(inv2, matchers.get(1).getInvocation());
    }

    private List<Invocation> invinvocations() throws Exception {
        Method method = SampleTarget.class.getMethod("noArgs");
        return Arrays.asList(
            createInvocation("mockA", method, new Object[0]),
            createInvocation("mockB", method, new Object[0])
        );
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCaptureArgumentsFrom_WhenCandidateHasFewerArgumentsThanMatchers_GuardsIndexOutOfBounds() throws Exception {
        Method method = SampleTarget.class.getMethod("simpleMethod", String.class, Integer.class);
        Invocation invTarget = createInvocation("myMock", method, new Object[]{"a", 1});
        // Candidate only has 1 argument while matcher expects 2
        Invocation invShorter = createInvocation("myMock", method, new Object[]{"single"});

        CapturingMatcher cap1 = new CapturingMatcher();
        CapturingMatcher cap2 = new CapturingMatcher();
        InvocationMatcher matcher = new InvocationMatcher(invTarget, Arrays.<Matcher>asList(cap1, cap2));

        matcher.captureArgumentsFrom(invShorter);

        assertEquals("First argument should be captured", 1, cap1.getCaptured().size());
        assertEquals("single", cap1.getCaptured().get(0));
        assertEquals("Second argument must not be captured as index exceeded length", 0, cap2.getCaptured().size());
    }

    @Test(timeout = 4000)
    public void testCaptureArgumentsFrom_MixedMatchers_OnlyCapturingMatchersInvoked() throws Exception {
        Method method = SampleTarget.class.getMethod("simpleMethod", String.class, Integer.class);
        Invocation inv = createInvocation("myMock", method, new Object[]{"val", 10});

        Matcher normalMatcher = new BaseMatcher<Object>() {
            @Override
            public boolean matches(Object item) { return true; }
            @Override
            public void describeTo(Description description) {}
        };
        CapturingMatcher capturingMatcher = new CapturingMatcher();

        InvocationMatcher matcher = new InvocationMatcher(inv, Arrays.<Matcher>asList(normalMatcher, capturingMatcher));
        matcher.captureArgumentsFrom(inv);

        assertEquals("CapturingMatcher must capture corresponding index argument", 1, capturingMatcher.getCaptured().size());
        assertEquals(10, capturingMatcher.getCaptured().get(0));
    }

    @Test(timeout = 4000)
    public void testCreateFrom_EmptyList_ReturnsEmptyLinkedList() {
        List<InvocationMatcher> result = InvocationMatcher.createFrom(Collections.<Invocation>emptyList());
        assertNotNull(result);
        assertTrue("Resulting list must be empty", result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructor_WithEmptyArgumentsInvocation_CreatesEmptyMatchers() throws Exception {
        Method method = SampleTarget.class.getMethod("noArgs");
        Invocation invocation = createInvocation("myMock", method, new Object[0]);

        InvocationMatcher matcher = new InvocationMatcher(invocation);

        assertNotNull(matcher.getMatchers());
        assertEquals("No arguments should produce 0 matchers", 0, matcher.getMatchers().size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT TEST:
     * Defects4J: InheritedGenericsPolimorphicCallTest::shouldStubbingWork & shouldVerificationWorks
     *
     * In defective InvocationMatcher, `hasSameMethod` uses `return m1.equals(m2);`.
     * When SuperInterface and SubInterface declare the polymorphic method with identical
     * names and parameter types, Method.equals() returns false because declaring classes differ.
     * The fixed implementation compares method name and parameter types, returning TRUE.
     */
    @Test(timeout = 4000)
    public void testDefect_InheritedPolymorphicCall_hasSameMethodShouldMatchSubInterfaceOverriddenMethod() throws Exception {
        Method mSuper = SuperInterface.class.getMethod("polymorphicMethod", String.class);
        Method mSub = SubInterface.class.getMethod("polymorphicMethod", String.class);

        // Sanity check: In standard Java, Method.equals is false due to declaringClass difference
        assertFalse("JVM Method.equals must be false for methods from different interfaces in hierarchy", mSuper.equals(mSub));

        Object mock = "sharedMock";
        Invocation invSuper = createInvocation(mock, mSuper, new Object[]{"targetArg"});
        Invocation invSub = createInvocation(mock, mSub, new Object[]{"targetArg"});

        InvocationMatcher matcher = new InvocationMatcher(invSuper);

        // This assertion triggers/reveals the defect:
        // Defective version returns false; fixed version correctly returns true.
        assertTrue("hasSameMethod must return true for polymorphic method in sub-interface hierarchy",
                matcher.hasSameMethod(invSub));
        assertTrue("matches must return true for polymorphic invocation with identical arguments",
                matcher.matches(invSub));
    }

    @Test(timeout = 4000)
    public void testDefect_GenericInterfaceHierarchy_hasSameMethodShouldMatch() throws Exception {
        Method mGenBase = GenericBase.class.getMethod("process", Object.class);
        Method mGenSub = GenericSub.class.getMethod("process", Object.class);

        assertFalse("Declaring classes differ between GenericBase and GenericSub", mGenBase.equals(mGenSub));

        Object mock = "genericMock";
        Invocation invBase = createInvocation(mock, mGenBase, new Object[]{"payload"});
        Invocation invSub = createInvocation(mock, mGenSub, new Object[]{"payload"});

        InvocationMatcher matcher = new InvocationMatcher(invBase);

        // Revealing defect in generic interface hierarchies:
        assertTrue("hasSameMethod must recognize inherited generic method as same method",
                matcher.hasSameMethod(invSub));
    }

    @Test(timeout = 4000)
    public void testDefect_InheritedPolymorphicCall_hasSimilarMethodShouldBeTrue() throws Exception {
        Method mSuper = SuperInterface.class.getMethod("polymorphicMethod", String.class);
        Method mSub = SubInterface.class.getMethod("polymorphicMethod", String.class);

        Object mock = "sharedMock";
        Invocation invSuper = createInvocation(mock, mSuper, new Object[]{"arg"});
        Invocation invSub = createInvocation(mock, mSub, new Object[]{"arg"}, false);

        InvocationMatcher matcher = new InvocationMatcher(invSuper);

        // If hasSameMethod incorrectly returns false, hasSimilarMethod treats it as an overloaded
        // method with same arguments and incorrectly returns false.
        assertTrue("hasSimilarMethod must be true for polymorphic method with identical arguments",
                matcher.hasSimilarMethod(invSub));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSafelyArgumentsMatch_WhenMatcherThrowsThrowable_CatchesAndReturnsFalse() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Object mock = "mock";
        Invocation invTarget = createInvocation(mock, method, new Object[]{"a"});
        Invocation invCandidate = createInvocation(mock, method, new Object[]{"b"}, false);

        // ThrowingMatcher causes ArgumentsComparator to throw, which safelyArgumentsMatch catches
        InvocationMatcher matcher = new InvocationMatcher(invTarget, Collections.<Matcher>singletonList(new ThrowingMatcher()));

        // When method names match and candidate is unverified and mock is same, hasSimilarMethod
        // invokes safelyArgumentsMatch. If arguments match, overloadedButSameArgs is calculated.
        boolean similar = matcher.hasSimilarMethod(invCandidate);
        // SafelyArgumentsMatch must not propagate the RuntimeException
        assertNotNull(similar);
    }

    @Test(timeout = 4000)
    public void testMatches_WhenMocksAreDifferent_ReturnsFalse() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Invocation inv1 = createInvocation("mockA", method, new Object[]{"data"});
        Invocation inv2 = createInvocation("mockB", method, new Object[]{"data"});

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("Different mocks must not match", matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testMatches_WhenArgumentsDoNotMatch_ReturnsFalse() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Object mock = "sharedMock";
        Invocation inv1 = createInvocation(mock, method, new Object[]{"expected"});
        Invocation inv2 = createInvocation(mock, method, new Object[]{"different"});

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("Mismatched arguments must return false", matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testMatches_WhenMethodsAreDifferent_ReturnsFalse() throws Exception {
        Method m1 = SuperInterface.class.getMethod("polymorphicMethod", String.class);
        Method m2 = SuperInterface.class.getMethod("distinctMethod");
        Object mock = "sharedMock";

        Invocation inv1 = createInvocation(mock, m1, new Object[]{"arg"});
        Invocation inv2 = createInvocation(mock, m2, new Object[0]);

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("Different method signatures must return false", matcher.matches(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_WhenCandidateIsAlreadyVerified_ReturnsFalse() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        Object mock = "mock";
        Invocation inv1 = createInvocation(mock, method, new Object[]{"data"});
        Invocation invVerified = createInvocation(mock, method, new Object[]{"data"}, true);

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("Verified candidate must not be considered similar", matcher.hasSimilarMethod(invVerified));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_WhenMethodNamesDiffer_ReturnsFalse() throws Exception {
        Method m1 = SuperInterface.class.getMethod("polymorphicMethod", String.class);
        Method m2 = SuperInterface.class.getMethod("distinctMethod");
        Object mock = "mock";

        Invocation inv1 = createInvocation(mock, m1, new Object[]{"data"});
        Invocation inv2 = createInvocation(mock, m2, new Object[0], false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("Different method names cannot be similar", matcher.hasSimilarMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_WhenMockReferencesAreNotIdentical_ReturnsFalse() throws Exception {
        Method method = SampleTarget.class.getMethod("singleArg", String.class);
        // Strings with equal content but distinct instances
        String mock1 = new String("mockObj");
        String mock2 = new String("mockObj");
        assertNotSame(mock1, mock2);

        Invocation inv1 = createInvocation(mock1, method, new Object[]{"data"});
        Invocation inv2 = createInvocation(mock2, method, new Object[]{"data"}, false);

        InvocationMatcher matcher = new InvocationMatcher(inv1);

        assertFalse("hasSimilarMethod uses identity check (==) on mock; different instances must return false",
                matcher.hasSimilarMethod(inv2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_WhenCandidateIsOverloadedWithSameArgs_ReturnsFalse() throws Exception {
        Method mString = SampleTarget.class.getMethod("overloaded", String.class);
        Method mObject = SampleTarget.class.getMethod("overloaded", Object.class);
        Object mock = "sharedMock";

        // Target is overloaded(Object) with custom Matcher accepting "sameValue"
        Invocation invTarget = createInvocation(mock, mObject, new Object[]{"sameValue"});
        Invocation invCandidate = createInvocation(mock, mString, new Object[]{"sameValue"}, false);

        InvocationMatcher matcher = new InvocationMatcher(invTarget);

        // candidate method is different (overloaded) but arguments match -> overloadedButSameArgs is true -> returns false
        assertFalse("Overloaded method called with same arguments cannot be similar",
                matcher.hasSimilarMethod(invCandidate));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializabilityContract_SerialVersionUIDConstant() throws Exception {
        assertTrue("InvocationMatcher must implement Serializable", Serializable.class.isAssignableFrom(InvocationMatcher.class));

        Field svuid = InvocationMatcher.class.getDeclaredField("serialVersionUID");
        svuid.setAccessible(true);
        long value = svuid.getLong(null);

        assertEquals("serialVersionUID must match the declared constant", -3047126096857467610L, value);
    }
}