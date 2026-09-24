package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.stubbing.answers.CallsRealMethods
 *
 * Decision / Condition Matrix:
 * 1. Normal Concrete Method Execution:
 *    - Modifier.isAbstract(method.getModifiers()) == false
 *    - Branch: Directly delegates to invocation.callRealMethod() and returns the actual value.
 *
 * 2. Defect Zone: Abstract & Interface Methods (Defects4J Ground Truth):
 *    - Modifier.isAbstract(method.getModifiers()) == true
 *    - In the defective implementation: Blindly calls invocation.callRealMethod(), which throws
 *      org.mockito.exceptions.base.MockitoException ("Cannot call real method on abstract method").
 *    - In the correct/fixed implementation: Intercepts abstract/interface method invocations and
 *      safely returns the default empty value (null, 0, false, etc.) without calling callRealMethod().
 *
 * 3. Exception Transparency:
 *    - Any Throwable thrown by invocation.callRealMethod() on concrete methods (RuntimeException,
 *      checked Exception, Error) must propagate transparently to the caller.
 *
 * 4. Defensive Boundaries:
 *    - Null InvocationOnMock input -> NullPointerException.
 *
 * 5. Lifecycle & Contract:
 *    - Implements Serializable: State serialization and deserialization integrity.
 */
public class CallsRealMethodsGeminiTest {

    // -------------------------------------------------------------------------
    // Test Doubles (Zero External Mocking Library Dependency)
    // -------------------------------------------------------------------------

    public abstract static class DummyAbstractClass {
        public abstract String dummyAbstractMethod();
        public abstract int dummyAbstractIntMethod();
        public abstract boolean dummyAbstractBooleanMethod();
        public abstract void dummyAbstractVoidMethod();

        public String dummyConcreteMethod() {
            return "concreteValue";
        }
    }

    public interface DummyInterface {
        String interfaceMethod();
        int interfaceIntMethod();
    }

    private InvocationOnMock createInvocation(final Method method,
                                              final Object realMethodResult,
                                              final Throwable realMethodThrowable) {
        return (InvocationOnMock) Proxy.newProxyInstance(
                InvocationOnMock.class.getClassLoader(),
                new Class<?>[]{InvocationOnMock.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                        String name = m.getName();
                        if ("getMethod".equals(name)) {
                            return method;
                        }
                        if ("callRealMethod".equals(name)) {
                            if (realMethodThrowable != null) {
                                throw realMethodThrowable;
                            }
                            return realMethodResult;
                        }
                        if ("getArguments".equals(name)) {
                            return new Object[0];
                        }
                        if ("getMock".equals(name)) {
                            return null;
                        }
                        return null;
                    }
                }
        );
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAnswer_withConcreteMethod_shouldReturnRealMethodValue() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
        InvocationOnMock invocation = createInvocation(method, "concreteValue", null);

        Object result = answer.answer(invocation);

        assertEquals("concreteValue", result);
    }

    @Test(timeout = 4000)
    public void testAnswer_withConcreteMethodReturningNull_shouldReturnNull() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
        InvocationOnMock invocation = createInvocation(method, null, null);

        Object result = answer.answer(invocation);

        assertNull(result);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAnswer_withNullInvocation_shouldThrowNullPointerException() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        answer.answer(null);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Targeting Ground Truth Defect)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAnswer_whenAbstractMethodReturnsObject_shouldReturnDefaultNullWithoutCallingRealMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method abstractMethod = DummyAbstractClass.class.getMethod("dummyAbstractMethod");
        // If callRealMethod is invoked on abstract method, MockitoException is thrown
        InvocationOnMock invocation = createInvocation(
                abstractMethod,
                null,
                new MockitoException("Cannot call abstract real method on a class!")
        );

        Object result = answer.answer(invocation);

        assertNull("Abstract method must return default value (null) and not attempt real call", result);
    }

    @Test(timeout = 4000)
    public void testAnswer_whenAbstractMethodReturnsPrimitiveInt_shouldReturnDefaultZero() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method abstractIntMethod = DummyAbstractClass.class.getMethod("dummyAbstractIntMethod");
        InvocationOnMock invocation = createInvocation(
                abstractIntMethod,
                null,
                new MockitoException("Cannot call abstract real method on a class!")
        );

        Object result = answer.answer(invocation);

        assertNotNull("Primitive int return type must return default wrapped primitive", result);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testAnswer_whenAbstractMethodReturnsPrimitiveBoolean_shouldReturnDefaultFalse() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method abstractBooleanMethod = DummyAbstractClass.class.getMethod("dummyAbstractBooleanMethod");
        InvocationOnMock invocation = createInvocation(
                abstractBooleanMethod,
                null,
                new MockitoException("Cannot call abstract real method on a class!")
        );

        Object result = answer.answer(invocation);

        assertNotNull("Primitive boolean return type must return default wrapped false", result);
        assertEquals(Boolean.FALSE, result);
    }

    @Test(timeout = 4000)
    public void testAnswer_whenAbstractMethodReturnsVoid_shouldReturnNull() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method abstractVoidMethod = DummyAbstractClass.class.getMethod("dummyAbstractVoidMethod");
        InvocationOnMock invocation = createInvocation(
                abstractVoidMethod,
                null,
                new MockitoException("Cannot call abstract real method on a class!")
        );

        Object result = answer.answer(invocation);

        assertNull("Void abstract method must return null without calling real method", result);
    }

    @Test(timeout = 4000)
    public void testAnswer_whenInterfaceMethod_shouldReturnDefaultWithoutCallingRealMethod() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method interfaceMethod = DummyInterface.class.getMethod("interfaceMethod");
        InvocationOnMock invocation = createInvocation(
                interfaceMethod,
                null,
                new MockitoException("Cannot call real method on java interface!")
        );

        Object result = answer.answer(invocation);

        assertNull("Interface method must return default empty value", result);
    }

    @Test(timeout = 4000)
    public void testAnswer_whenInterfacePrimitiveMethod_shouldReturnDefaultValue() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method interfaceIntMethod = DummyInterface.class.getMethod("interfaceIntMethod");
        InvocationOnMock invocation = createInvocation(
                interfaceIntMethod,
                null,
                new MockitoException("Cannot call real method on java interface!")
        );

        Object result = answer.answer(invocation);

        assertNotNull(result);
        assertEquals(0, result);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAnswer_whenConcreteRealMethodThrowsRuntimeException_shouldPropagateException() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
        RuntimeException expectedEx = new IllegalStateException("Simulated runtime error");
        InvocationOnMock invocation = createInvocation(method, null, expectedEx);

        try {
            answer.answer(invocation);
            fail("Expected IllegalStateException to be propagated");
        } catch (IllegalStateException actual) {
            assertSame(expectedEx, actual);
        }
    }

    @Test(timeout = 4000)
    public void testAnswer_whenConcreteRealMethodThrowsCheckedException_shouldPropagateThrowable() {
        CallsRealMethods answer = new CallsRealMethods();
        try {
            Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
            Exception expectedEx = new Exception("Simulated checked exception");
            InvocationOnMock invocation = createInvocation(method, null, expectedEx);

            answer.answer(invocation);
            fail("Expected checked Exception to be propagated");
        } catch (Throwable actual) {
            assertEquals("Simulated checked exception", actual.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAnswer_whenConcreteRealMethodThrowsError_shouldPropagateError() throws Throwable {
        CallsRealMethods answer = new CallsRealMethods();
        Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
        AssertionError expectedError = new AssertionError("Simulated fatal assertion failure");
        InvocationOnMock invocation = createInvocation(method, null, expectedError);

        try {
            answer.answer(invocation);
            fail("Expected AssertionError to be propagated");
        } catch (AssertionError actual) {
            assertSame(expectedError, actual);
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializationIntegrity_shouldMaintainBehaviorAfterDeserialization() throws Exception {
        CallsRealMethods original = new CallsRealMethods();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue("Deserialized instance must be an instance of CallsRealMethods",
                deserialized instanceof CallsRealMethods);

        CallsRealMethods deserializedAnswer = (CallsRealMethods) deserialized;
        Method method = DummyAbstractClass.class.getMethod("dummyConcreteMethod");
        InvocationOnMock invocation = createInvocation(method, "deserializedSuccess", null);

        try {
            Object result = deserializedAnswer.answer(invocation);
            assertEquals("deserializedSuccess", result);
        } catch (Throwable t) {
            fail("Deserialized instance invocation failed: " + t.getMessage());
        }
    }
}