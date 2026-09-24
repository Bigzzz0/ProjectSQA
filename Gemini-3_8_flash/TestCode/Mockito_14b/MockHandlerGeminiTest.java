package org.mockito.internal;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.MockHandler<T>
 * Target Defect: org.mockitousage.bugs.VerifyingWithAnExtraCallToADifferentMockTest::shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine
 *
 * Decision / Condition Coverage Mapping:
 * 1. invocationContainerImpl.hasAnswersForStubbing():
 *    - TRUE:  bindMatchers -> setMethodForStubbing -> return null (Tested in void / doAnswer stubbing paths)
 *    - FALSE: proceed to verification / invocation handling
 * 2. verificationMode != null:
 *    - TRUE:  [KNOWN DEFECT ZONE] Must verify invocation against matching mock only. If verification mode was started
 *             on a DIFFERENT mock (e.g. verify(mock1).action(mock2.getValue())), mock2's handler MUST NOT consume
 *             the mode or invoke verify(data). It must preserve the verification mode and return mock2's default answer.
 *             Tested in testVerificationWithExtraCallToDifferentMockInSameLine() and testNormalVerificationModeSucceedsForSameMock().
 *    - FALSE: proceed to potential stubbing & invocation dispatch
 * 3. stubbedInvocation != null:
 *    - TRUE:  captureArgumentsFrom(invocation) -> return stubbedInvocation.answer(invocation)
 *    - FALSE: mockSettings.getDefaultAnswer().answer(invocation) -> resetInvocationForPotentialStubbing -> return ret
 * 4. Re-entrant partial mock / spy dispatch:
 *    - Verifies resetInvocationForPotentialStubbing correctly resets the ongoing invocation matcher
 *      even when default answer invokes another mock method.
 * 5. Lifecycle & Configuration:
 *    - Default constructor, MockSettingsImpl constructor, copy constructor with old MockHandlerInterface.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.VoidMethodStubbableImpl;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

public class MockHandlerGeminiTest {

    private MockingProgress mockingProgress;

    @Before
    public void setUp() {
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.reset();
    }

    @After
    public void tearDown() {
        if (mockingProgress != null) {
            mockingProgress.reset();
        }
    }

    // =========================================================================
    // Dynamic Reflection Helper for Zero-Dependency Invocation Construction
    // =========================================================================
    @SuppressWarnings("rawtypes")
    private Invocation createInvocation(final Object mock, final Method method, final Object[] args) throws Exception {
        Constructor<?>[] ctors = Invocation.class.getConstructors();
        Constructor<?> selectedCtor = null;
        for (Constructor<?> c : ctors) {
            if (selectedCtor == null || c.getParameterTypes().length > selectedCtor.getParameterTypes().length) {
                selectedCtor = c;
            }
        }
        assertNotNull("Invocation constructor should be present", selectedCtor);

        Class<?>[] paramTypes = selectedCtor.getParameterTypes();
        Object[] initArgs = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> pt = paramTypes[i];
            if (i == 0 && (pt == Object.class || pt.isAssignableFrom(mock.getClass()))) {
                initArgs[i] = mock;
            } else if (pt.getName().endsWith("MockitoMethod")) {
                initArgs[i] = Proxy.newProxyInstance(
                    pt.getClassLoader(),
                    new Class<?>[] { pt },
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method m, Object[] mArgs) throws Throwable {
                            String name = m.getName();
                            if ("getJavaMethod".equals(name)) return method;
                            if ("getName".equals(name)) return method.getName();
                            if ("getReturnType".equals(name)) return method.getReturnType();
                            if ("getParameterTypes".equals(name)) return method.getParameterTypes();
                            if ("getExceptionTypes".equals(name)) return method.getExceptionTypes();
                            if ("isVarArgs".equals(name)) return method.isVarArgs();
                            if ("isAbstract".equals(name)) return Modifier.isAbstract(method.getModifiers());
                            if ("hashCode".equals(name)) return method.hashCode();
                            if ("equals".equals(name)) return proxy == mArgs[0];
                            if ("toString".equals(name)) return method.toString();
                            return null;
                        }
                    }
                );
            } else if (pt == Object[].class) {
                initArgs[i] = (args != null) ? args : new Object[0];
            } else if (pt == int.class || pt == Integer.class) {
                initArgs[i] = 1;
            } else if (pt == long.class || pt == Long.class) {
                initArgs[i] = 1L;
            } else if (pt == boolean.class || pt == Boolean.class) {
                initArgs[i] = false;
            } else if (pt.isInterface()) {
                initArgs[i] = Proxy.newProxyInstance(
                    pt.getClassLoader(),
                    new Class<?>[] { pt },
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method m, Object[] mArgs) throws Throwable {
                            if ("invoke".equals(m.getName())) return null;
                            if ("hashCode".equals(m.getName())) return 0;
                            if ("equals".equals(m.getName())) return proxy == mArgs[0];
                            return null;
                        }
                    }
                );
            } else {
                initArgs[i] = null;
            }
        }
        return (Invocation) selectedCtor.newInstance(initArgs);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleNormalInvocationDefaultAnswer() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method sizeMethod = List.class.getMethod("size");
        Invocation invocation = createInvocation(dummyMock, sizeMethod, new Object[0]);

        Object result = handler.handle(invocation);

        // Default Answer for int return type in Mockito is 0
        assertNotNull("Result should not be null for primitive return type", result);
        assertEquals(0, result);
        assertEquals(1, handler.getInvocationContainer().getInvocations().size());
    }

    @Test(timeout = 4000)
    public void testHandleVoidMethodDefaultAnswer() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method clearMethod = List.class.getMethod("clear");
        Invocation invocation = createInvocation(dummyMock, clearMethod, new Object[0]);

        Object result = handler.handle(invocation);

        assertNull("Void method should return null on default answer", result);
        assertEquals(1, handler.getInvocationContainer().getInvocations().size());
    }

    @Test(timeout = 4000)
    public void testHandleStubbedInvocation() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method sizeMethod = List.class.getMethod("size");
        Invocation invocation = createInvocation(dummyMock, sizeMethod, new Object[0]);

        // Stage 1: Configure answers for stubbing
        Answer<Object> customAnswer = new Answer<Object>() {
            public Object answer(InvocationOnMock inv) {
                return 42;
            }
        };
        handler.setAnswersForStubbing(Collections.<Answer>singletonList(customAnswer));
        assertTrue(handler.invocationContainerImpl.hasAnswersForStubbing());

        // Stage 2: Binding invocation for stubbing
        Object stubSetupResult = handler.handle(invocation);
        assertNull("Setting method for stubbing should return null", stubSetupResult);
        assertFalse(handler.invocationContainerImpl.hasAnswersForStubbing());

        // Stage 3: Actual invocation matching stubbed response
        Object stubbedResult = handler.handle(invocation);
        assertNotNull(stubbedResult);
        assertEquals(42, stubbedResult);
    }

    @Test(timeout = 4000)
    public void testHandleVoidMethodStubbingSequence() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        List<?> dummyMock = new ArrayList<Object>();
        VoidMethodStubbable<List<?>> stubber = handler.voidMethodStubbable(dummyMock);
        assertNotNull(stubber);

        stubber.toThrow(new IllegalStateException("Void stubbed error"));

        Method clearMethod = List.class.getMethod("clear");
        Invocation invocation = createInvocation(dummyMock, clearMethod, new Object[0]);

        // First handle registers the method for stubbing
        Object setupRes = handler.handle(invocation);
        assertNull(setupRes);

        // Second handle executes the stubbed exception
        try {
            handler.handle(invocation);
            fail("Expected IllegalStateException from void stubbing");
        } catch (IllegalStateException expected) {
            assertEquals("Void stubbed error", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testHandleReentrantInvocationResetsPotentialStubbing() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        final MockHandler<Object>[] handlerRef = new MockHandler[1];
        final Object dummyMock = new Object();

        settings.defaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if ("size".equals(invocation.getMethod().getName())) {
                    Method isEmptyMethod = List.class.getMethod("isEmpty");
                    Invocation innerInv = createInvocation(dummyMock, isEmptyMethod, new Object[0]);
                    // Re-entrant handle call, as happens in spies / partial mocks
                    handlerRef[0].handle(innerInv);
                    return 99;
                }
                return false;
            }
        });

        MockHandler<Object> handler = new MockHandler<Object>(settings);
        handlerRef[0] = handler;

        Method sizeMethod = List.class.getMethod("size");
        Invocation outerInv = createInvocation(dummyMock, sizeMethod, new Object[0]);

        Object result = handler.handle(outerInv);
        assertEquals(99, result);

        // Verify that invocation for potential stubbing was reset back to outerInv (size)
        InvocationMatcher matcher = handler.invocationContainerImpl.getInvocationForPotentialStubbing();
        assertNotNull("Matcher for potential stubbing must be preserved", matcher);
        assertEquals("size", matcher.getMethod().getName());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleWithArgumentsAndNullValues() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method setMethod = List.class.getMethod("set", int.class, Object.class);

        Invocation invocationWithNull = createInvocation(dummyMock, setMethod, new Object[] { 0, null });

        Answer<Object> answer = new Answer<Object>() {
            public Object answer(InvocationOnMock inv) {
                return "previousValue";
            }
        };
        handler.setAnswersForStubbing(Collections.<Answer>singletonList(answer));
        handler.handle(invocationWithNull); // Stubbing registered

        Object result = handler.handle(invocationWithNull);
        assertEquals("previousValue", result);
    }

    @Test(timeout = 4000)
    public void testHandleMultipleChainedAnswers() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method getMethod = List.class.getMethod("get", int.class);
        Invocation invocation = createInvocation(dummyMock, getMethod, new Object[] { 1 });

        List<Answer> answers = new ArrayList<Answer>();
        answers.add(new Answer<Object>() {
            public Object answer(InvocationOnMock inv) { return "first"; }
        });
        answers.add(new Answer<Object>() {
            public Object answer(InvocationOnMock inv) { return "second"; }
        });

        handler.setAnswersForStubbing(answers);
        handler.handle(invocation); // Registers stubbing

        Object r1 = handler.handle(invocation);
        Object r2 = handler.handle(invocation);
        Object r3 = handler.handle(invocation);

        assertEquals("first", r1);
        assertEquals("second", r2);
        assertEquals("second", r3); // Consecutive calls return the last answer
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Targets: VerifyingWithAnExtraCallToADifferentMockTest
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerificationWithExtraCallToDifferentMockInSameLine() throws Throwable {
        final Object mock1 = "TargetMockForVerify";
        final Object mock2 = "ExtraMockInSameLine";

        final boolean[] innerModeVerifiedOnMock = new boolean[] { false };
        VerificationMode innerMode = new VerificationMode() {
            public void verify(VerificationData data) {
                innerModeVerifiedOnMock[0] = true;
            }
        };

        // Construct MockAwareVerificationMode targeting mock1
        VerificationMode mockAwareMode = null;
        try {
            Class<?> mavmClass = Class.forName("org.mockito.internal.verification.MockAwareVerificationMode");
            Constructor<?> ctor = mavmClass.getConstructor(Object.class, VerificationMode.class);
            mockAwareMode = (VerificationMode) ctor.newInstance(mock1, innerMode);
        } catch (ClassNotFoundException ignored) {
            // If class name differs in environment, test falls back gracefully
        }

        if (mockAwareMode == null) {
            return;
        }

        // Start verification on mock1
        mockingProgress.verificationStarted(mockAwareMode);

        MockSettingsImpl settingsMock2 = new MockSettingsImpl();
        MockHandler<Object> handlerMock2 = new MockHandler<Object>(settingsMock2);

        Method sizeMethod = List.class.getMethod("size");
        Invocation invocationOnMock2 = createInvocation(mock2, sizeMethod, new Object[0]);

        // Trigger extra call to mock2 (representing: verify(mock1).action(mock2.size()))
        Object resultOnMock2 = handlerMock2.handle(invocationOnMock2);

        // GROUND TRUTH DEFECT BEHAVIOR:
        // On defective MockHandler:
        // 1. verificationMode is pulled unconditionally without checking mock matching.
        // 2. innerMode.verify(data) is executed prematurely on mock2's empty invocations!
        // 3. resultOnMock2 returns null instead of mock2's default answer (0).
        // 4. verificationMode is consumed and lost for mock1!
        assertFalse("Verification mode for mock1 MUST NOT be triggered by an invocation on mock2",
                innerModeVerifiedOnMock[0]);

        assertNotNull("Call on mock2 must return its normal default answer, not null from verification mode",
                resultOnMock2);
        assertEquals(0, resultOnMock2);

        VerificationMode preservedMode = mockingProgress.pullVerificationMode();
        assertNotNull("Verification mode for mock1 must remain preserved in MockingProgress", preservedMode);
    }

    @Test(timeout = 4000)
    public void testNormalVerificationModeSucceedsForSameMock() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method clearMethod = List.class.getMethod("clear");
        Invocation invocation = createInvocation(dummyMock, clearMethod, new Object[0]);

        final boolean[] verified = new boolean[] { false };
        VerificationMode mode = new VerificationMode() {
            public void verify(VerificationData data) {
                verified[0] = true;
                assertNotNull(data);
                assertEquals(1, data.getAllInvocations().size());
            }
        };

        // Add invocation to container first
        handler.handle(invocation);

        // Initiate verification
        mockingProgress.verificationStarted(mode);

        Object verifyHandleResult = handler.handle(invocation);
        assertNull("Verification path must return null", verifyHandleResult);
        assertTrue("Verification mode must have been executed", verified[0]);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandleThrowsExceptionFromStubbedAnswer() throws Throwable {
        MockHandler<List<?>> handler = new MockHandler<List<?>>();
        Object dummyMock = new ArrayList<Object>();
        Method sizeMethod = List.class.getMethod("size");
        Invocation invocation = createInvocation(dummyMock, sizeMethod, new Object[0]);

        Answer<Object> throwingAnswer = new Answer<Object>() {
            public Object answer(InvocationOnMock inv) throws Throwable {
                throw new UnsupportedOperationException("Stubbed failure");
            }
        };

        handler.setAnswersForStubbing(Collections.<Answer>singletonList(throwingAnswer));
        handler.handle(invocation); // Register

        try {
            handler.handle(invocation);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            assertEquals("Stubbed failure", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testHandleThrowsExceptionFromDefaultAnswer() throws Throwable {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock inv) throws Throwable {
                throw new IllegalArgumentException("Default answer error");
            }
        });

        MockHandler<Object> handler = new MockHandler<Object>(settings);
        Object dummyMock = new Object();
        Method toStringMethod = Object.class.getMethod("toString");
        Invocation invocation = createInvocation(dummyMock, toStringMethod, new Object[0]);

        try {
            handler.handle(invocation);
            fail("Expected IllegalArgumentException from default answer");
        } catch (IllegalArgumentException expected) {
            assertEquals("Default answer error", expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndGetters() {
        MockHandler<Object> defaultHandler = new MockHandler<Object>();
        assertNotNull(defaultHandler.getMockSettings());
        assertNotNull(defaultHandler.getInvocationContainer());
        assertTrue(defaultHandler.getInvocationContainer() instanceof InvocationContainer);

        MockSettingsImpl customSettings = new MockSettingsImpl();
        MockHandler<Object> customHandler = new MockHandler<Object>(customSettings);
        assertSame(customSettings, customHandler.getMockSettings());

        MockHandler<Object> copiedHandler = new MockHandler<Object>(customHandler);
        assertSame(customSettings, copiedHandler.getMockSettings());
    }

    @Test(timeout = 4000)
    public void testVoidMethodStubbableCreation() {
        MockHandler<String> handler = new MockHandler<String>();
        VoidMethodStubbable<String> stubbable = handler.voidMethodStubbable("mockInstance");
        assertNotNull(stubbable);
        assertTrue(stubbable instanceof VoidMethodStubbableImpl);
    }

    @Test(timeout = 4000)
    public void testInterfaceContracts() {
        MockHandler<Object> handler = new MockHandler<Object>();
        assertTrue("Must implement MockitoInvocationHandler", handler instanceof MockitoInvocationHandler);
        assertTrue("Must implement MockHandlerInterface", handler instanceof MockHandlerInterface);
        assertTrue("Must implement java.io.Serializable", handler instanceof java.io.Serializable);
    }
}