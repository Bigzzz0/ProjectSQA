package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import java.io.*;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: CallsRealMethods (implements Answer<Object>, Serializable)
 *
 * Decision branches to cover:
 * - answer(InvocationOnMock) delegates to invocation.callRealMethod().
 * - If invocation is null -> NullPointerException (defensive path).
 * - If callRealMethod() returns a value -> answer() returns that value.
 * - If callRealMethod() throws a Throwable -> answer() propagates it (including MockitoException).
 *
 * Specific known defect trigger:
 * - Calling real methods on abstract methods/interfaces should raise MockitoException.
 *   Here we simulate an invocation that throws MockitoException when callRealMethod() is invoked,
 *   and assert that answer() propagates the exception (or, if defective, fails to do so).
 *
 * BVA boundary values:
 * - null invocation, null return value, non-null return value.
 * - Serialization roundtrip to ensure Serializable contract.
 *
 * Partitions:
 *   A) Core delegation logic (normal return and exception propagation).
 *   B) Null input and null return.
 *   C) Defect-targeted: MockitoException propagation for abstract method simulation.
 *   D) Generic exception propagation from callRealMethod().
 *   E) Serialization integrity.
 */
public class CallsRealMethodsDeepseekTest {

    // --- Test implementation of InvocationOnMock ----------------------------------------
    private static class TestInvocation implements org.mockito.invocation.InvocationOnMock {
        private final Object result;
        private final Throwable throwable;

        TestInvocation(Object result) {
            this.result = result;
            this.throwable = null;
        }

        TestInvocation(Throwable throwable) {
            this.result = null;
            this.throwable = throwable;
        }

        @Override
        public Object callRealMethod() throws Throwable {
            if (throwable != null) {
                throw throwable;
            }
            return result;
        }

        @Override
        public Object getMock() {
            return null;
        }

        @Override
        public org.mockito.invocation.Invocation getInvocation() {
            return null;
        }

        @Override
        public Class<?> getRawReturnType() {
            return Object.class;
        }

        @Override
        public org.mockito.invocation.InvocationOnMock getUnderlyingInvocation() {
            return null;
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public Object[] getRawArguments() {
            return new Object[0];
        }

        @Override
        public org.mockito.invocation.InvocationOnMock getInvocationOnMock() {
            return null;
        }
    }

    // --- Partition A: Core functional logic ------------------------------------------------
    @Test(timeout = 4000)
    public void testAnswerReturnsValueFromCallRealMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Object expected = "real result";
        TestInvocation invocation = new TestInvocation(expected);

        Object result = answer.answer(invocation);

        assertEquals("Should return the exact value from callRealMethod()", expected, result);
        assertSame("Should return the same instance", expected, result);
    }

    @Test(timeout = 4000)
    public void testAnswerReturnsNullWhenCallRealMethodReturnsNull() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        TestInvocation invocation = new TestInvocation(null);

        Object result = answer.answer(invocation);

        assertNull("Should return null when callRealMethod() returns null", result);
    }

    // --- Partition B: Boundary values & null handling ---------------------------------------
    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAnswerWithNullInvocationThrowsNPE() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        answer.answer(null);
    }

    // --- Partition C: Defect-targeted branch (abstract method => MockitoException) -----------
    @Test(expected = org.mockito.exceptions.base.MockitoException.class, timeout = 4000)
    public void testAnswerPropagatesMockitoExceptionFromAbstractMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        // Simulate an abstract method by making callRealMethod() throw MockitoException
        Throwable abstractMethodException =
            new org.mockito.exceptions.base.MockitoException("Cannot call real method on abstract method");
        TestInvocation invocation = new TestInvocation(abstractMethodException);

        answer.answer(invocation);
    }

    // --- Partition D: Generic exception propagation ------------------------------------------
    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testAnswerPropagatesRuntimeExceptionFromCallRealMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        TestInvocation invocation = new TestInvocation(new IllegalStateException("boom"));

        answer.answer(invocation);
    }

    @Test(expected = java.io.IOException.class, timeout = 4000)
    public void testAnswerPropagatesCheckedExceptionFromCallRealMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        TestInvocation invocation = new TestInvocation(new java.io.IOException("io"));

        answer.answer(invocation);
    }

    // --- Partition E: Object lifecycle & Serializable contract --------------------------------
    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws IOException, ClassNotFoundException {
        CallsRealMethods original = new CallsRealMethods();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized object should not be null", deserialized);
        assertTrue("Deserialized object should be an instance of CallsRealMethods",
            deserialized instanceof CallsRealMethods);
    }

    @Test(timeout = 4000)
    public void testSerializedInstanceStillDelegates() throws Throwable, IOException, ClassNotFoundException {
        CallsRealMethods answer = new CallsRealMethods();
        // Serialize the answer instance
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(answer);
        oos.close();

        // Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        CallsRealMethods deserialized = (CallsRealMethods) ois.readObject();
        ois.close();

        // Ensure deserialized instance works normally
        Object expected = "after serialization";
        TestInvocation invocation = new TestInvocation(expected);
        Object result = deserialized.answer(invocation);
        assertEquals("Deserialized instance should produce same result", expected, result);
    }
}