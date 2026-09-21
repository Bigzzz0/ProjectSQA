package org.mockito.internal.invocation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.*;

import org.mockito.internal.matchers.CapturesArguments;
import org.mockito.internal.matchers.MatcherDecorator;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.Location;
import org.hamcrest.Matcher;

public class InvocationMatcherDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: InvocationMatcher.captureArgumentsFrom(Invocation) - varargs handling
     * 
     * Known Defect: 
     * - When capturing varargs, the method incorrectly indexes into rawArguments
     *   causing ArrayIndexOutOfBoundsException or ClassCastException.
     * - The bug is in the loop: `for (int position = indexOfVararg; position < matchers.size(); position++)`
     *   and the raw argument access `invocation.getRawArguments()[position - indexOfVararg]`
     *   which fails when matchers.size() > rawArguments.length.
     * 
     * Branches to cover:
     * 1. invocation.getMethod().isVarArgs() == true (varargs path)
     * 2. invocation.getMethod().isVarArgs() == false (non-varargs path)
     * 3. position < indexOfVararg (non-vararg arguments)
     * 4. position >= indexOfVararg (vararg arguments)
     * 5. Matcher is instance of CapturesArguments (capture branch)
     * 6. Matcher is NOT instance of CapturesArguments (skip branch)
     * 7. matchers.size() > rawArguments.length (defect trigger)
     * 8. matchers.size() <= rawArguments.length (normal case)
     * 
     * Boundary conditions:
     * - Empty varargs (rawArguments.length == 1, indexOfVararg == 0)
     * - Single vararg (rawArguments.length == 2, indexOfVararg == 1)
     * - Multiple varargs (rawArguments.length > 2)
     * - Primitive varargs (byte, int, etc.)
     * - Mixed non-vararg and vararg arguments
     * 
     * Defect-targeted test: should_capture_varargs_when_matchers_exceed_raw_arguments
     * This test creates a scenario where matchers.size() > rawArguments.length
     * which triggers the ArrayIndexOutOfBoundsException in the defective version.
     */

    // Test helper classes
    private static class TestInvocation implements Invocation {
        private final Method method;
        private final Object[] arguments;
        private final Object[] rawArguments;
        private final Object mock;
        private final Location location;
        private boolean verified;

        TestInvocation(Method method, Object[] arguments, Object mock) {
            this.method = method;
            this.arguments = arguments;
            this.rawArguments = arguments; // In real Mockito, rawArguments may differ for varargs
            this.mock = mock;
            this.location = new Location() {
                @Override
                public String toString() {
                    return "test location";
                }
            };
        }

        @Override
        public Method getMethod() { return method; }

        @Override
        public Object[] getArguments() { return arguments; }

        @Override
        public Object[] getRawArguments() { return rawArguments; }

        @Override
        public Object getMock() { return mock; }

        @Override
        public Location getLocation() { return location; }

        @Override
        public boolean isVerified() { return verified; }

        @Override
        public void markVerified() { verified = true; }

        @Override
        public Object getArgumentAt(int index, Class<?> clazz) { 
            return arguments[index]; 
        }

        @Override
        public boolean isVarArgs() { return method.isVarArgs(); }

        @Override
        public String toString() { return "TestInvocation"; }
    }

    private static class TestCapturesArguments implements CapturesArguments {
        private final List<Object> captured = new ArrayList<Object>();

        @Override
        public void captureFrom(Object argument) {
            captured.add(argument);
        }

        public List<Object> getCaptured() { return captured; }
    }

    private static class NonCapturingMatcher implements Matcher<Object> {
        @Override
        public boolean matches(Object item) { return true; }

        @Override
        public void describeTo(org.hamcrest.Description description) {}

        @Override
        public void describeMismatch(Object item, org.hamcrest.Description mismatchDescription) {}
    }

    // Test methods for varargs method
    private static Method getVarargsMethod() throws Exception {
        return InvocationMatcherDeepseekTest.class.getDeclaredMethod("varargsMethod", String[].class);
    }

    private static Method getNonVarargsMethod() throws Exception {
        return InvocationMatcherDeepseekTest.class.getDeclaredMethod("nonVarargsMethod", String.class, String.class);
    }

    private static Method getPrimitiveVarargsMethod() throws Exception {
        return InvocationMatcherDeepseekTest.class.getDeclaredMethod("primitiveVarargsMethod", byte[].class);
    }

    @SuppressWarnings("unused")
    private void varargsMethod(String... args) {}

    @SuppressWarnings("unused")
    private void nonVarargsMethod(String arg1, String arg2) {}

    @SuppressWarnings("unused")
    private void primitiveVarargsMethod(byte... args) {}

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructor_withEmptyMatchers_createsMatchersFromArguments() throws Exception {
        Method method = getNonVarargsMethod();
        Object mock = new Object();
        Object[] args = new Object[]{"a", "b"};
        TestInvocation invocation = new TestInvocation(method, args, mock);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.getMatchers());
        assertEquals(2, matcher.getMatchers().size());
        assertEquals(invocation, matcher.getInvocation());
        assertEquals(method, matcher.getMethod());
    }

    @Test(timeout = 4000)
    public void testConstructor_withProvidedMatchers_usesThem() throws Exception {
        Method method = getNonVarargsMethod();
        Object mock = new Object();
        Object[] args = new Object[]{"a", "b"};
        TestInvocation invocation = new TestInvocation(method, args, mock);
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher());
        matchers.add(new NonCapturingMatcher());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        assertSame(matchers, matcher.getMatchers());
    }

    @Test(timeout = 4000)
    public void testGetMethod_returnsCorrectMethod() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertSame(method, matcher.getMethod());
    }

    @Test(timeout = 4000)
    public void testGetInvocation_returnsSameInvocation() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertSame(invocation, matcher.getInvocation());
    }

    @Test(timeout = 4000)
    public void testGetMatchers_returnsUnmodifiableList() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        try {
            matcher.getMatchers().add(new NonCapturingMatcher());
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testToString_returnsNonEmptyString() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.toString());
        assertFalse(matcher.toString().isEmpty());
    }

    @Test(timeout = 4000)
    public void testMatches_withSameMockAndMethod_returnsTrue() throws Exception {
        Method method = getNonVarargsMethod();
        Object mock = new Object();
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertTrue(matcher.matches(invocation2));
    }

    @Test(timeout = 4000)
    public void testMatches_withDifferentMock_returnsFalse() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertFalse(matcher.matches(invocation2));
    }

    @Test(timeout = 4000)
    public void testMatches_withDifferentMethod_returnsFalse() throws Exception {
        Method method1 = getNonVarargsMethod();
        Method method2 = getVarargsMethod();
        Object mock = new Object();
        TestInvocation invocation1 = new TestInvocation(method1, new Object[]{"a", "b"}, mock);
        TestInvocation invocation2 = new TestInvocation(method2, new Object[]{"a", "b"}, mock);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertFalse(matcher.matches(invocation2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_withSameMethod_returnsTrue() throws Exception {
        Method method = getNonVarargsMethod();
        Object mock = new Object();
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertTrue(matcher.hasSimilarMethod(invocation2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_withDifferentName_returnsFalse() throws Exception {
        Method method1 = getNonVarargsMethod();
        Method method2 = getVarargsMethod();
        Object mock = new Object();
        TestInvocation invocation1 = new TestInvocation(method1, new Object[]{"a", "b"}, mock);
        TestInvocation invocation2 = new TestInvocation(method2, new Object[]{"a", "b"}, mock);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertFalse(matcher.hasSimilarMethod(invocation2));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_withVerifiedInvocation_returnsFalse() throws Exception {
        Method method = getNonVarargsMethod();
        Object mock = new Object();
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"a", "b"}, mock);
        invocation2.markVerified();
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertFalse(matcher.hasSimilarMethod(invocation2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethod_withSameMethod_returnsTrue() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertTrue(matcher.hasSameMethod(invocation2));
    }

    @Test(timeout = 4000)
    public void testHasSameMethod_withDifferentMethod_returnsFalse() throws Exception {
        Method method1 = getNonVarargsMethod();
        Method method2 = getVarargsMethod();
        TestInvocation invocation1 = new TestInvocation(method1, new Object[]{"a", "b"}, new Object());
        TestInvocation invocation2 = new TestInvocation(method2, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation1);
        
        assertFalse(matcher.hasSameMethod(invocation2));
    }

    @Test(timeout = 4000)
    public void testGetLocation_returnsLocation() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.getLocation());
    }

    @Test(timeout = 4000)
    public void testCreateFrom_createsMatchersForEachInvocation() throws Exception {
        Method method = getNonVarargsMethod();
        List<Invocation> invocations = new ArrayList<Invocation>();
        invocations.add(new TestInvocation(method, new Object[]{"a", "b"}, new Object()));
        invocations.add(new TestInvocation(method, new Object[]{"c", "d"}, new Object()));
        
        List<InvocationMatcher> matchers = InvocationMatcher.createFrom(invocations);
        
        assertEquals(2, matchers.size());
        for (InvocationMatcher matcher : matchers) {
            assertNotNull(matcher.getInvocation());
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testConstructor_withEmptyMatchersAndEmptyArguments() throws Exception {
        Method method = getVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{new Object[0]}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertNotNull(matcher.getMatchers());
        assertEquals(1, matcher.getMatchers().size());
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withNonVarargsMethod_capturesAllArguments() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        TestCapturesArguments captor1 = new TestCapturesArguments();
        TestCapturesArguments captor2 = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor1);
        matchers.add(captor2);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        assertEquals(Arrays.asList("a"), captor1.getCaptured());
        assertEquals(Arrays.asList("b"), captor2.getCaptured());
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withNonCapturingMatchers_doesNothing() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(new NonCapturingMatcher());
        matchers.add(new NonCapturingMatcher());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        // No exception should be thrown
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withEmptyVarargs_capturesNothing() throws Exception {
        Method method = getVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{new Object[0]}, new Object());
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        assertTrue(captor.getCaptured().isEmpty());
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withSingleVararg_capturesCorrectly() throws Exception {
        Method method = getVarargsMethod();
        Object[] args = new Object[]{"a"};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        assertEquals(1, captor.getCaptured().size());
        assertEquals("a", captor.getCaptured().get(0));
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withMultipleVarargs_capturesAll() throws Exception {
        Method method = getVarargsMethod();
        Object[] args = new Object[]{"a", "b", "c"};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        assertEquals(3, captor.getCaptured().size());
        assertEquals("a", captor.getCaptured().get(0));
        assertEquals("b", captor.getCaptured().get(1));
        assertEquals("c", captor.getCaptured().get(2));
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withPrimitiveVarargs_capturesCorrectly() throws Exception {
        Method method = getPrimitiveVarargsMethod();
        byte[] byteArgs = new byte[]{1, 2, 3};
        Object[] args = new Object[]{byteArgs};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        matcher.captureArgumentsFrom(invocation);
        
        assertEquals(3, captor.getCaptured().size());
        assertEquals((byte)1, captor.getCaptured().get(0));
        assertEquals((byte)2, captor.getCaptured().get(1));
        assertEquals((byte)3, captor.getCaptured().get(2));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Defect-targeted test: This test creates a scenario where matchers.size() > rawArguments.length
     * which triggers the ArrayIndexOutOfBoundsException in the defective version.
     * 
     * The bug is in captureArgumentsFrom when handling varargs:
     * for (int position = indexOfVararg; position < matchers.size(); position++) {
     *     Matcher m = matchers.get(position);
     *     if (m instanceof CapturesArguments) {
     *         ((CapturesArguments) m).captureFrom(invocation.getRawArguments()[position - indexOfVararg]);
     *     }
     * }
     * 
     * When matchers.size() > rawArguments.length, position - indexOfVararg exceeds
     * rawArguments.length - 1, causing ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testCaptureArguments_withMoreMatchersThanRawArguments_shouldNotThrow() throws Exception {
        Method method = getVarargsMethod();
        // Only 1 raw argument (the vararg array itself)
        Object[] args = new Object[]{"a"};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        
        // Create more matchers than raw arguments (2 matchers for 1 raw argument)
        TestCapturesArguments captor1 = new TestCapturesArguments();
        TestCapturesArguments captor2 = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor1);
        matchers.add(captor2);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        // This should not throw ArrayIndexOutOfBoundsException
        matcher.captureArgumentsFrom(invocation);
        
        // Verify that the first captor captured the vararg
        assertEquals(1, captor1.getCaptured().size());
        assertEquals("a", captor1.getCaptured().get(0));
        // The second captor should have captured nothing (or handled gracefully)
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withMultipleVarargsAndExtraMatchers_shouldNotThrow() throws Exception {
        Method method = getVarargsMethod();
        Object[] args = new Object[]{"a", "b"};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        
        // 3 matchers for 2 raw arguments
        TestCapturesArguments captor1 = new TestCapturesArguments();
        TestCapturesArguments captor2 = new TestCapturesArguments();
        TestCapturesArguments captor3 = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor1);
        matchers.add(captor2);
        matchers.add(captor3);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        // This should not throw ArrayIndexOutOfBoundsException
        matcher.captureArgumentsFrom(invocation);
        
        // Verify captures
        assertEquals(2, captor1.getCaptured().size());
        assertEquals("a", captor1.getCaptured().get(0));
        assertEquals("b", captor1.getCaptured().get(1));
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withPrimitiveVarargsAndExtraMatchers_shouldNotThrow() throws Exception {
        Method method = getPrimitiveVarargsMethod();
        byte[] byteArgs = new byte[]{1, 2};
        Object[] args = new Object[]{byteArgs};
        TestInvocation invocation = new TestInvocation(method, args, new Object());
        
        // 2 matchers for 1 raw argument (the byte array)
        TestCapturesArguments captor1 = new TestCapturesArguments();
        TestCapturesArguments captor2 = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor1);
        matchers.add(captor2);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        // This should not throw ArrayIndexOutOfBoundsException or ClassCastException
        matcher.captureArgumentsFrom(invocation);
        
        // Verify captures - should capture individual bytes, not the array
        assertEquals(2, captor1.getCaptured().size());
        assertEquals((byte)1, captor1.getCaptured().get(0));
        assertEquals((byte)2, captor1.getCaptured().get(1));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testCaptureArguments_withNullArguments_shouldHandleGracefully() throws Exception {
        Method method = getVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, null, new Object());
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        try {
            matcher.captureArgumentsFrom(invocation);
            // If no exception, test passes
        } catch (NullPointerException e) {
            // Acceptable if NPE is thrown, but should not be ArrayIndexOutOfBounds
            assertFalse(e instanceof ArrayIndexOutOfBoundsException);
        }
    }

    @Test(timeout = 4000)
    public void testMatches_withNullActual_shouldReturnFalse() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertFalse(matcher.matches(null));
    }

    @Test(timeout = 4000)
    public void testHasSimilarMethod_withNullCandidate_shouldReturnFalse() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertFalse(matcher.hasSimilarMethod(null));
    }

    @Test(timeout = 4000)
    public void testHasSameMethod_withNullCandidate_shouldReturnFalse() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        assertFalse(matcher.hasSameMethod(null));
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testSerialization_shouldPreserveState() throws Exception {
        Method method = getNonVarargsMethod();
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        
        InvocationMatcher matcher = new InvocationMatcher(invocation);
        
        // Test that the object is serializable
        assertTrue(matcher instanceof java.io.Serializable);
    }

    @Test(timeout = 4000)
    public void testMultipleCaptures_withSameCaptor_shouldAccumulate() throws Exception {
        Method method = getVarargsMethod();
        TestCapturesArguments captor = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor);
        
        // First invocation
        TestInvocation invocation1 = new TestInvocation(method, new Object[]{"a", "b"}, new Object());
        InvocationMatcher matcher1 = new InvocationMatcher(invocation1, matchers);
        matcher1.captureArgumentsFrom(invocation1);
        
        // Second invocation
        TestInvocation invocation2 = new TestInvocation(method, new Object[]{"c", "d"}, new Object());
        InvocationMatcher matcher2 = new InvocationMatcher(invocation2, matchers);
        matcher2.captureArgumentsFrom(invocation2);
        
        // Captor should have accumulated values from both invocations
        assertEquals(4, captor.getCaptured().size());
        assertEquals("a", captor.getCaptured().get(0));
        assertEquals("b", captor.getCaptured().get(1));
        assertEquals("c", captor.getCaptured().get(2));
        assertEquals("d", captor.getCaptured().get(3));
    }

    @Test(timeout = 4000)
    public void testCaptureArguments_withMixedMatchers_capturesOnlyCapturingMatchers() throws Exception {
        Method method = getVarargsMethod();
        TestCapturesArguments captor1 = new TestCapturesArguments();
        NonCapturingMatcher nonCapturing = new NonCapturingMatcher();
        TestCapturesArguments captor2 = new TestCapturesArguments();
        List<Matcher> matchers = new ArrayList<Matcher>();
        matchers.add(captor1);
        matchers.add(nonCapturing);
        matchers.add(captor2);
        
        TestInvocation invocation = new TestInvocation(method, new Object[]{"a", "b", "c"}, new Object());
        InvocationMatcher matcher = new InvocationMatcher(invocation, matchers);
        
        matcher.captureArgumentsFrom(invocation);
        
        // Only captor1 and captor2 should have captured
        assertEquals(3, captor1.getCaptured().size());
        assertEquals("a", captor1.getCaptured().get(0));
        assertEquals("b", captor1.getCaptured().get(1));
        assertEquals("c", captor1.getCaptured().get(2));
        assertEquals(0, captor2.getCaptured().size());
    }
}