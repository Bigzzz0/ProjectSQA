/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.MockHandler
 *
 * Decision / Condition Matrix:
 * 1. Constructor variants:
 *    - MockHandler(MockSettingsImpl) -> state initialization.
 *    - MockHandler() -> default MockSettingsImpl.
 *    - MockHandler(MockHandlerInterface) -> preserves settings from old handler.
 * 2. handle(Invocation):
 *    - Branch: invocationContainerImpl.hasAnswersForStubbing() == true
 *      -> bind matchers, setMethodForStubbing, return null.
 *    - Branch: verificationMode != null:
 *      - Sub-branch: verificationMode instanceof MockAwareVerificationMode && ((MockAwareVerificationMode) verificationMode).getMock() == invocation.getMock()
 *        -> creates VerificationDataImpl, invokes verificationMode.verify(), returns null.
 *      - Sub-branch (DEFECT ZONE - Bug 138): verificationMode != null BUT invocation.getMock() != verificationMode.getMock().
 *        -> Defect: verification mode was pulled from MockingProgress and dropped without being re-added,
 *           causing subsequent verification on the target mock to lose its verification state.
 *    - Branch: stubbedInvocation != null:
 *      -> captures arguments, returns stubbedInvocation.answer(invocation).
 *    - Branch: stubbedInvocation == null:
 *      -> falls back to mockSettings.getDefaultAnswer().answer(invocation), resets invocation for potential stubbing.
 * 3. voidMethodStubbable(mock): returns VoidMethodStubbableImpl instance.
 * 4. setAnswersForStubbing(answers): delegates to invocationContainerImpl.
 * 5. getInvocationContainer() and getMockSettings(): getter verification.
 */
package org.mockito.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.MockitoMethod;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.verification.MockAwareVerificationMode;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

public class MockHandlerGeminiTest {

    // Helper interface for synthetic mocks
    interface SampleService {
        String execute(String input);
        void doWork();
        int calculate(int value);
    }

    private static class DummyRealMethod implements RealMethod {
        private static final long serialVersionUID = 1L;
        @Override
        public Object invoke(Object target, Object[] arguments) throws Throwable {
            return null;
        }
    }

    private static class CustomMockitoMethod implements MockitoMethod {
        private final Method method;

        public CustomMockitoMethod(Method method) {
            this.method = method;
        }

        @Override
        public String getName() {
            return method.getName();
        }

        @Override
        public Class<?> getReturnType() {
            return method.getReturnType();
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return method.getParameterTypes();
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return method.getExceptionTypes();
        }

        @Override
        public boolean isVarArgs() {
            return method.isVarArgs();
        }

        @Override
        public Method getJavaMethod() {
            return method;
        }
    }

    private Invocation createDummyInvocation(Object mock, String methodName, Class<?>[] paramTypes, Object[] args) throws NoSuchMethodException {
        Method method = SampleService.class.getMethod(methodName, paramTypes);
        return new Invocation(mock, new CustomMockitoMethod(method), args, 1, new DummyRealMethod());
    }

    private SampleService createProxyMock() {
        return (SampleService) Proxy.newProxyInstance(
                SampleService.class.getClassLoader(),
                new Class<?>[]{SampleService.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        return null;
                    }
                });
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndGetters() {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        assertNotNull(handler.getMockSettings());
        assertNotNull(handler.getInvocationContainer());
        assertTrue(handler.getInvocationContainer() instanceof InvocationContainer);
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<SampleService> original = new MockHandler<SampleService>(settings);
        MockHandler<SampleService> copy = new MockHandler<SampleService>(original);

        assertSame(settings, copy.getMockSettings());
        assertNotNull(copy.getInvocationContainer());
    }

    @Test(timeout = 4000)
    public void testVoidMethodStubbableCreation() {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        SampleService mock = createProxyMock();
        VoidMethodStubbable<SampleService> stubbable = handler.voidMethodStubbable(mock);

        assertNotNull(stubbable);
    }

    @Test(timeout = 4000)
    public void testHandleWithDefaultAnswer() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        SampleService mock = createProxyMock();
        Invocation invocation = createDummyInvocation(mock, "execute", new Class<?>[]{String.class}, new Object[]{"testArg"});

        Object result = handler.handle(invocation);
        assertNull("Default answer for object return type without stubbing should be null", result);
    }

    @Test(timeout = 4000)
    public void testHandleWithAnswersForStubbingQueue() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        SampleService mock = createProxyMock();

        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Answer<String>() {
            @Override
            public String answer(org.mockito.invocation.InvocationOnMock invocation) {
                return "stubbedValue";
            }
        });

        handler.setAnswersForStubbing(answers);

        Invocation invocation = createDummyInvocation(mock, "execute", new Class<?>[]{String.class}, new Object[]{"hello"});
        Object firstResult = handler.handle(invocation);

        // When answers are set for stubbing, handle() sets the method for stubbing and returns null
        assertNull(firstResult);

        // Subsequent handle on same invocation pattern should yield the stubbed answer
        Object secondResult = handler.handle(invocation);
        assertEquals("stubbedValue", secondResult);
    }

    // =========================================================================
    // Partition B: Verification Logic & Matching Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleUnderMatchingVerificationMode() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        final SampleService mock = createProxyMock();
        Invocation invocation = createDummyInvocation(mock, "doWork", new Class<?>[0], new Object[0]);

        final boolean[] verifiedCalled = new boolean[]{false};
        VerificationMode mockMode = new MockAwareVerificationMode(mock, new VerificationMode() {
            @Override
            public void verify(org.mockito.internal.verification.api.VerificationData data) {
                verifiedCalled[0] = true;
            }
        });

        MockingProgress progress = handler.mockingProgress;
        progress.verificationStarted(mockMode);

        Object result = handler.handle(invocation);

        assertNull(result);
        assertTrue("Verification mode should have executed verify()", verifiedCalled[0]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug 138)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectVerifyingWithExtraCallToDifferentMockPreservesVerificationMode() throws Throwable {
        /*
         * Ground Truth Defect:
         * VerifyingWithAnExtraCallToADifferentMockTest:
         * verify(mockA).execute(mockB.execute("arg"));
         * When mockB.execute() is called while verification is active on mockA,
         * handler for mockB pulls the verification mode from MockingProgress.
         * Since mock != verificationMode.getMock(), the verificationMode MUST be
         * re-added back to MockingProgress. In the buggy version, it is lost!
         */
        MockHandler<SampleService> handlerA = new MockHandler<SampleService>();
        MockHandler<SampleService> handlerB = new MockHandler<SampleService>();

        // Ensure both handlers share the same MockingProgress thread-safe instance
        MockingProgress sharedProgress = new ThreadSafeMockingProgress();
        handlerA.mockingProgress = sharedProgress;
        handlerB.mockingProgress = sharedProgress;

        SampleService mockA = createProxyMock();
        SampleService mockB = createProxyMock();

        final boolean[] verifiedA = new boolean[]{false};
        VerificationMode modeForA = new MockAwareVerificationMode(mockA, new VerificationMode() {
            @Override
            public void verify(org.mockito.internal.verification.api.VerificationData data) {
                verifiedA[0] = true;
            }
        });

        // 1. Verification started for mockA
        sharedProgress.verificationStarted(modeForA);

        // 2. An interleaved call occurs on mockB (e.g. evaluating an argument)
        Invocation invocationB = createDummyInvocation(mockB, "execute", new Class<?>[]{String.class}, new Object[]{"val"});
        handlerB.handle(invocationB);

        // 3. Now the actual call on mockA occurs
        Invocation invocationA = createDummyInvocation(mockA, "execute", new Class<?>[]{String.class}, new Object[]{"val"});
        handlerA.handle(invocationA);

        // In the fixed version, verification mode was re-added so mockA was verified.
        // In the defective version, verifiedA[0] will remain false.
        assertTrue("Verification mode for mockA must not be discarded when an intermediate call to mockB is made!", verifiedA[0]);
    }

    // =========================================================================
    // Partition D: Stubbing & Fallback Invocation Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testStubbedInvocationArgumentsCaptured() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        SampleService mock = createProxyMock();

        // Register stubbing answer
        handler.setAnswersForStubbing(Arrays.asList((Answer) new Answer<Integer>() {
            @Override
            public Integer answer(org.mockito.invocation.InvocationOnMock invocation) {
                Integer arg = (Integer) invocation.getArguments()[0];
                return arg * 2;
            }
        }));

        Invocation stubCall = createDummyInvocation(mock, "calculate", new Class<?>[]{int.class}, new Object[]{5});
        handler.handle(stubCall); // registers stub

        // Now invoke with the same signature
        Invocation call = createDummyInvocation(mock, "calculate", new Class<?>[]{int.class}, new Object[]{5});
        Object result = handler.handle(call);

        assertEquals(10, result);
    }

    @Test(timeout = 4000)
    public void testNonMockAwareVerificationModeDoesNotTriggerVerification() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        SampleService mock = createProxyMock();
        Invocation invocation = createDummyInvocation(mock, "execute", new Class<?>[]{String.class}, new Object[]{"arg"});

        final boolean[] verifiedCalled = new boolean[]{false};
        // Raw verification mode not wrapped in MockAwareVerificationMode
        VerificationMode rawMode = new VerificationMode() {
            @Override
            public void verify(org.mockito.internal.verification.api.VerificationData data) {
                verifiedCalled[0] = true;
            }
        };

        handler.mockingProgress.verificationStarted(rawMode);
        Object result = handler.handle(invocation);

        assertFalse("Non-MockAwareVerificationMode should not trigger verify directly", verifiedCalled[0]);
        assertNull(result);
    }

    // =========================================================================
    // Partition E: Boundary & Null State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStubbingAnswersList() {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        handler.setAnswersForStubbing(Collections.<Answer>emptyList());
        assertFalse(handler.invocationContainerImpl.hasAnswersForStubbing());
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testHandleNullInvocationThrowsException() throws Throwable {
        MockHandler<SampleService> handler = new MockHandler<SampleService>();
        handler.handle(null);
    }
}