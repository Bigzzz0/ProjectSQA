/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNulls
 *
 * Decision / Condition Matrix:
 * 1. ReturnsSmartNulls#answer(InvocationOnMock):
 *    - Branch 1: delegate.answer(invocation) != null (Returns standard empty values, e.g., empty String, 0, false, empty collection)
 *    - Branch 2: delegate.answer(invocation) == null:
 *       - Branch 2a: ClassImposterizer.INSTANCE.canImposterise(type) == true (Interface or non-final class -> returns SmartNull proxy)
 *       - Branch 2b: ClassImposterizer.INSTANCE.canImposterise(type) == false (Final class, primitive void, array -> returns ordinary null)
 *
 * 2. ThrowingInterceptor#intercept(Object, Method, Object[], MethodProxy):
 *    - Branch 3: ObjectMethodsGuru#isToString(method) == true
 *       - Formats unstubbed method call via formatMethodCall(): invocation.getMethod().getName() + "(" + args + ")"
 *       - Tests with 0 args, 1 arg, multiple args.
 *       - Verifies exact string format: "SmartNull returned by unstubbed ... method on mock"
 *    - Branch 4: ObjectMethodsGuru#isToString(method) == false
 *       - Invokes Reporter#smartNullPointerException(location).
 *       - Throws SmartNullPointerException (Defects4J Known Defect: exception message is expected to include
 *         method arguments such as "oompa" and "lumpa", but fails in defective version).
 *
 * Defect-Targeted Zone:
 * - Defects4J Mockito defect: shouldPrintTheParametersOnSmartNullPointerExceptionMessage
 *   When unstubbed method was invoked with arguments (e.g., "oompa", "lumpa"), calling a method on the resulting
 *   SmartNull should include the unstubbed method's arguments in the SmartNullPointerException message.
 */

package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.invocation.InvocationOnMock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ReturnsSmartNullsGeminiTest {

    // Helper dummy interfaces and classes for testing
    interface SampleService {
        String getString();
        int getInt();
        List<String> getList();
        Set<String> getSet();
        Map<String, String> getMap();
        FinalClass getFinalClass();
        DummyCollaborator getCollaborator();
        DummyCollaborator withArgs(String arg1, String arg2);
        DummyCollaborator noArgs();
        DummyCollaborator singleArg(int val);
    }

    static final class FinalClass {
        public void execute() {}
    }

    interface DummyCollaborator {
        void performAction();
        String retrieveData();
    }

    private static class DummyInvocation implements InvocationOnMock {
        private final Object mock;
        private final Method method;
        private final Object[] arguments;

        public DummyInvocation(Object mock, Method method, Object[] arguments) {
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

    /* ====================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ==================================================================== */

    @Test(timeout = 4000)
    public void testReturnsMoreEmptyValuesDelegateHandledTypes() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();

        // Primitive int -> 0
        Method getIntMethod = SampleService.class.getMethod("getInt");
        Object intResult = returnsSmartNulls.answer(new DummyInvocation(new Object(), getIntMethod, new Object[0]));
        assertEquals(0, intResult);

        // String -> ""
        Method getStringMethod = SampleService.class.getMethod("getString");
        Object strResult = returnsSmartNulls.answer(new DummyInvocation(new Object(), getStringMethod, new Object[0]));
        assertEquals("", strResult);

        // Collections -> empty collections
        Method getListMethod = SampleService.class.getMethod("getList");
        Object listResult = returnsSmartNulls.answer(new DummyInvocation(new Object(), getListMethod, new Object[0]));
        assertEquals(Collections.emptyList(), listResult);

        Method getSetMethod = SampleService.class.getMethod("getSet");
        Object setResult = returnsSmartNulls.answer(new DummyInvocation(new Object(), getSetMethod, new Object[0]));
        assertEquals(Collections.emptySet(), setResult);

        Method getMapMethod = SampleService.class.getMethod("getMap");
        Object mapResult = returnsSmartNulls.answer(new DummyInvocation(new Object(), getMapMethod, new Object[0]));
        assertEquals(Collections.emptyMap(), mapResult);
    }

    @Test(timeout = 4000)
    public void testReturnsSmartNullForMockableType() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("getCollaborator");
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[0]);

        Object result = returnsSmartNulls.answer(invocation);
        assertNotNull("SmartNull proxy should not be null for mockable interface", result);
        assertTrue("SmartNull should implement target interface", result instanceof DummyCollaborator);
    }

    @Test(timeout = 4000)
    public void testSmartNullToStringFormattingNoArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("noArgs");
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[0]);

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        String toString = smartNull.toString();
        assertEquals("SmartNull returned by unstubbed noArgs() method on mock", toString);
    }

    @Test(timeout = 4000)
    public void testSmartNullToStringFormattingSingleArg() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("singleArg", int.class);
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[]{42});

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        String toString = smartNull.toString();
        assertEquals("SmartNull returned by unstubbed singleArg(42) method on mock", toString);
    }

    @Test(timeout = 4000)
    public void testSmartNullToStringFormattingMultipleArgs() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("withArgs", String.class, String.class);
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[]{"alpha", "beta"});

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        String toString = smartNull.toString();
        assertEquals("SmartNull returned by unstubbed withArgs(alpha, beta) method on mock", toString);
    }

    /* ====================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ==================================================================== */

    @Test(timeout = 4000)
    public void testReturnsNullForUnmockableFinalClass() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("getFinalClass");
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[0]);

        Object result = returnsSmartNulls.answer(invocation);
        assertNull("Final classes cannot be imposterised, standard null must be returned", result);
    }

    @Test(timeout = 4000)
    public void testSmartNullToStringWithNullArguments() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("withArgs", String.class, String.class);
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[]{null, "nonNull"});

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        String toString = smartNull.toString();
        assertEquals("SmartNull returned by unstubbed withArgs(null, nonNull) method on mock", toString);
    }

    /* ====================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * ==================================================================== */

    /**
     * Target Defect:
     * org.mockito.internal.stubbing.defaultanswers.ReturnsSmartNullsTest::shouldPrintTheParametersOnSmartNullPointerExceptionMessage
     * -> junit.framework.AssertionFailedError: Exception message should include oompa and lumpa, but was: ...
     *
     * Calling any method other than toString() on a SmartNull must trigger a SmartNullPointerException
     * that details the unstubbed method call and includes the parameters passed ("oompa", "lumpa").
     */
    @Test(timeout = 4000)
    public void shouldPrintTheParametersOnSmartNullPointerExceptionMessage() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("withArgs", String.class, String.class);
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[]{"oompa", "lumpa"});

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        try {
            smartNull.performAction();
            fail("Expected SmartNullPointerException when invoking method on SmartNull");
        } catch (SmartNullPointerException e) {
            String message = e.getMessage();
            assertNotNull("Exception message should not be null", message);
            assertTrue("Exception message should include oompa and lumpa, but was: " + message,
                    message.contains("oompa") && message.contains("lumpa"));
        }
    }

    /* ====================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ==================================================================== */

    @Test(timeout = 4000)
    public void testSmartNullThrowsSmartNullPointerExceptionOnAnyNonToStringMethod() throws Throwable {
        ReturnsSmartNulls returnsSmartNulls = new ReturnsSmartNulls();
        Method method = SampleService.class.getMethod("noArgs");
        InvocationOnMock invocation = new DummyInvocation(new Object(), method, new Object[0]);

        DummyCollaborator smartNull = (DummyCollaborator) returnsSmartNulls.answer(invocation);
        assertNotNull(smartNull);

        try {
            smartNull.retrieveData();
            fail("Expected SmartNullPointerException on non-toString method invocation");
        } catch (SmartNullPointerException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    /* ====================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ==================================================================== */

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

        assertNotNull("Deserialized ReturnsSmartNulls should not be null", deserialized);
        assertTrue("Deserialized object must be instance of ReturnsSmartNulls", deserialized instanceof ReturnsSmartNulls);
    }
}