/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MockitoMethod;
import org.mockito.internal.invocation.SerializableMockitoMethod;
import org.mockito.invocation.InvocationOnMock;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: ReturnsSmartNulls
 *
 * Branches:
 * 1. answer(InvocationOnMock invocation):
 *    - delegate.answer(invocation) != null -> returns default return value (e.g. primitives, empty collections, strings)
 *    - delegate.answer(invocation) == null:
 *        - ClassImposterizer.INSTANCE.canImposterise(type) == true -> returns SmartNull proxy
 *        - ClassImposterizer.INSTANCE.canImposterise(type) == false (final class, primitive, etc.) -> returns null
 *
 * 2. ThrowingInterceptor.intercept(Object obj, Method method, Object[] args, MethodProxy proxy):
 *    - isToString(method) == true -> returns formatted message "SmartNull returned by unstubbed " + formatMethodCall() + " method on mock"
 *    - isToString(method) == false -> calls Reporter().smartNullPointerException(location) -> throws SmartNullPointerException
 *
 * 3. formatMethodCall():
 *    - Known Defect: in defective code, formatMethodCall() returns `invocation.getMethod().getName() + "()"`, omitting arguments!
 *      When arguments are passed (e.g., withArgs("oompa", "lumpa")), it should format the call with arguments
 *      expected: "SmartNull returned by unstubbed withArgs(oompa, lumpa) method on mock"
 *      defective: "SmartNull returned by unstubbed withArgs() method on mock"
 *
 * 4. Serialization / Object Contract:
 *    - Implements Serializable. Serialization and deserialization test ensures standard contract integrity.
 */
public class ReturnsSmartNullsGeminiTest {

    // Helper interface for testing invocations
    interface DummyService {
        String getString();
        int getInt();
        final class FinalClass {}
        FinalClass getFinal();
        DummyService getService();
        DummyService withArgs(String arg1, String arg2);
        void doSomething();
    }

    // Helper fake InvocationOnMock
    private static class FakeInvocation implements InvocationOnMock {
        private final Object mock;
        private final Method method;
        private final Object[] arguments;

        public FakeInvocation(Object mock, Method method, Object[] arguments) {
            this.mock = mock;
            this.method = method;
            this.arguments = arguments != null ? arguments : new Object[0];
        }

        public Object getMock() {
            return mock;
        }

        public Method getMethod() {
            return method;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public Object callRealMethod() throws Throwable {
            return null;
        }
    }

    /*
     * PARTITION A: Core Functional Logic & State Transitions
     */

    @Test(timeout = 4000)
    public void testReturnsSmartNullForMockableType() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = DummyService.class.getMethod("getService");
        InvocationOnMock invocation = new FakeInvocation(new Object(), method, new Object[0]);

        Object result = returnsSmartNulls.answer(invocation);
        assertNotNull("SmartNull proxy should not be null for mockable interface", result);
        assertTrue("SmartNull proxy should implement DummyService", result instanceof DummyService);
    }

    @Test(timeout = 4000)
    public void testToStringOnSmartNullWithoutArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = DummyService.class.getMethod("getService");
        InvocationOnMock invocation = new FakeInvocation(new Object(), method, new Object[0]);

        Object smartNull = returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        String toStringResult = smartNull.toString();
        assertEquals("SmartNull returned by unstubbed getService() method on mock", toStringResult);
    }

    @Test(timeout = 4000)
    public void testSmartNullThrowsExceptionOnNonToStringInvocation() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = DummyService.class.getMethod("getService");
        InvocationOnMock invocation = new FakeInvocation(new Object(), method, new Object[0]);

        DummyService smartNull = (DummyService) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        try {
            smartNull.doSomething();
            fail("Expected SmartNullPointerException when invoking method on SmartNull proxy");
        } catch (SmartNullPointerException e) {
            // Expected exception
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
        }
    }

    /*
     * PARTITION B: Boundary Value Analysis (BVA) & Extremes
     */

    @Test(timeout = 4000)
    public void testDelegateHandlesPrimitiveAndStandardDefaults() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();

        // int return type -> ReturnsMoreEmptyValues delegates to ReturnsEmptyValues -> 0
        Method intMethod = DummyService.class.getMethod("getInt");
        InvocationOnMock intInvocation = new FakeInvocation(new Object(), intMethod, new Object[0]);
        Object intResult = returnsSmartNulls.answer(intInvocation);
        assertEquals(0, intResult);

        // String return type -> ReturnsMoreEmptyValues -> ""
        Method stringMethod = DummyService.class.getMethod("getString");
        InvocationOnMock stringInvocation = new FakeInvocation(new Object(), stringMethod, new Object[0]);
        Object stringResult = returnsSmartNulls.answer(stringInvocation);
        assertEquals("", stringResult);
    }

    @Test(timeout = 4000)
    public void testUnmockableTypeReturnsOrdinaryNull() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method finalMethod = DummyService.class.getMethod("getFinal");
        InvocationOnMock invocation = new FakeInvocation(new Object(), finalMethod, new Object[0]);

        Object result = returnsSmartNulls.answer(invocation);
        assertNull("Unmockable (final) return type should yield ordinary null", result);
    }

    /*
     * PARTITION C: Defect-Targeted Branch Zone (Defects4J Known Defect)
     */

    @Test(timeout = 4000)
    public void testShouldPrintTheParametersWhenCallingAMethodWithArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method withArgsMethod = DummyService.class.getMethod("withArgs", String.class, String.class);
        InvocationOnMock invocation = new FakeInvocation(
                new Object(),
                withArgsMethod,
                new Object[]{"oompa", "lumpa"}
        );

        Object smartNull = returnsSmartNulls.answer(invocation);
        assertNotNull("SmartNull proxy should not be null", smartNull);

        // Defect target: The defective code returns "... withArgs() method on mock"
        // Correct behavior expects parameters included: "... withArgs(oompa, lumpa) method on mock"
        String expectedMessage = "SmartNull returned by unstubbed withArgs(oompa, lumpa) method on mock";
        assertEquals(expectedMessage, smartNull.toString());
    }

    /*
     * PARTITION D: Exception & Defensive Guard Paths
     */

    @Test(timeout = 4000)
    public void testSmartNullThrowsExceptionWhenCallingGetter() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = DummyService.class.getMethod("getService");
        InvocationOnMock invocation = new FakeInvocation(new Object(), method, new Object[0]);

        DummyService smartNull = (DummyService) returnsSmartNulls.answer(invocation);

        try {
            smartNull.getString();
            fail("Expected SmartNullPointerException when invoking method on SmartNull proxy");
        } catch (SmartNullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }

    /*
     * PARTITION E: Object Lifecycle & Contract Integrity
     */

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(returnsSmartNulls);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsSmartNulls);
    }
}