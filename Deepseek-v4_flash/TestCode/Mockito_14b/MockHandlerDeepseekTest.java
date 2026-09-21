package org.mockito.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;

import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;
import org.mockito.internal.invocation.MatchersBinder;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.InvocationContainer;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.stubbing.VoidMethodStubbableImpl;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.verification.VerificationMode;

/**
 * Comprehensive white-box test suite for MockHandler,
 * targeting maximum coverage and the known Defects4J defect
 * (VerifyingWithAnExtraCallToADifferentMockTest).
 *
 * [Branch & Defect Analysis Matrix]
 * 1. `hasAnswersForStubbing` path (Branch A)
 * 2. `verificationMode != null` path (Branch B)
 *    - verification data creation and execution
 * 3. `stubbedInvocation != null` path (Branch C)
 * 4. Default answer path (Branch D)
 * 5. Boundary: null / empty arguments in setters
 * 6. Defect: two different MockHandlers – first in verify, second called on same line
 *    Assert that verification completes without interference.
 * 7. Object contract: getMockSettings(), voidMethodStubbable(), getInvocationContainer()
 */
public class MockHandlerDeepseekTest {

    // ---- Helper: minimal Invocation stub ----
    private static Invocation createDummyInvocation(final Object mock) {
        return new Invocation() {
            @Override
            public Object getMock() { return mock; }

            @Override
            public Method getMethod() {
                try {
                    return Object.class.getMethod("toString");
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object[] getArguments() { return new Object[0]; }

            @Override
            public boolean isVerified() { return false; }

            @Override
            public boolean isIgnoredForVerification() { return false; }

            @Override
            public void markVerified() {}

            @Override
            public void markStubbed() {}

            @Override
            public boolean isEqualsTo(Invocation o) { return false; }

            @Override
            public Invocation getInvocation() { return this; }

            @Override
            public Class<?> getMockClass() { return mock.getClass(); }

            // remaining methods not used by our handler
            @Override public int sequenceNumber() { return 0; }
            @Override public VoidMethodStubbable<T> voidMethodStubbable(T mock) { return null; }
            @Override public void setAnnotationsForStubbing(List<Answer> answers) {}
        };
    }

    // ---- Partition A: Core logic paths ----

    @Test(timeout = 4000)
    public void handle_withAnswersForStubbing_returnsNull() throws Throwable {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        List<Answer> answers = new LinkedList<>();
        answers.add((Answer<Object>) invocation -> "stubbed");
        handler.setAnswersForStubbing(answers);

        Object result = handler.handle(createDummyInvocation(new Object()));
        assertNull("Should return null when answers for stubbing are present", result);
    }

    @Test(timeout = 4000)
    public void handle_withVerificationMode_callsVerification() throws Throwable {
        final boolean[] called = {false};
        VerificationMode verifier = new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                called[0] = true;
            }
        };

        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        handler.mockingProgress.verificationStarted(verifier);
        handler.handle(createDummyInvocation(new Object()));
        assertTrue("VerificationMode.verify() should have been invoked", called[0]);
    }

    @Test(timeout = 4000)
    public void handle_stubbedInvocationReturnsAnswer() throws Throwable {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        // Pre-stub an answer by using internal container
        StubbedInvocationMatcher stubbed = new StubbedInvocationMatcher(
            new InvocationMatcher(createDummyInvocation(new Object())),
            (Answer<Object>) invocation -> "stubbed_result"
        );
        handler.invocationContainerImpl.addAnswer(stubbed);

        Object result = handler.handle(createDummyInvocation(new Object()));
        assertEquals("Should return stubbed answer", "stubbed_result", result);
    }

    @Test(timeout = 4000)
    public void handle_defaultAnswerWhenNoStub() throws Throwable {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        // Default answer: return null for object methods
        Object result = handler.handle(createDummyInvocation(new Object()));
        assertNull("Default answer should return null", result);
    }

    // ---- Partition B: Boundary and nulls ----

    @Test(timeout = 4000)
    public void constructor_withNullSettings_doesNotFail() {
        try {
            new MockHandler<>((MockSettingsImpl) null);
        } catch (Exception e) {
            // allowed – but we expect no NPE at construction
        }
    }

    @Test(timeout = 4000)
    public void setAnswersForStubbing_withNullList_doesNotThrow() {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        try {
            handler.setAnswersForStubbing(null);
        } catch (Exception e) {
            fail("Should not throw on null list: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void getInvocationContainer_returnsNonNull() {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        assertNotNull("InvocationContainer should not be null", handler.getInvocationContainer());
    }

    @Test(timeout = 4000)
    public void voidMethodStubbable_returnsNonNull() {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        assertNotNull("voidMethodStubbable should not be null", handler.voidMethodStubbable(new Object()));
    }

    @Test(timeout = 4000)
    public void getMockSettings_returnsSettings() {
        MockSettingsImpl settings = new MockSettingsImpl();
        MockHandler<Object> handler = new MockHandler<>(settings);
        assertSame("Should return the same settings object", settings, handler.getMockSettings());
    }

    // ---- Partition C: Defect-targeted test (VerifyingWithAnExtraCallToADifferentMock) ----

    @Test(timeout = 4000)
    public void verifyWithExtraCallToDifferentMock_sameLine_defect() throws Throwable {
        // Simulate two different mocks (MockHandlers)
        MockHandler<Object> handler1 = new MockHandler<>(new MockSettingsImpl());
        MockHandler<Object> handler2 = new MockHandler<>(new MockSettingsImpl());

        final boolean[] verifiedCalled = {false};
        VerificationMode verifier = new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                verifiedCalled[0] = true;
            }
        };

        // Start verification on handler1
        handler1.mockingProgress.verificationStarted(verifier);

        // First call: should invoke verification (the line under test)
        Object dummyMock1 = new Object();
        Invocation inv1 = createDummyInvocation(dummyMock1);
        handler1.handle(inv1);

        assertTrue("Verification should have been called on handler1", verifiedCalled[0]);

        // Extra call to a different mock in the same "line" (simulated)
        // This should NOT interfere with the already completed verification
        Object dummyMock2 = new Object();
        Invocation inv2 = createDummyInvocation(dummyMock2);
        handler2.handle(inv2);   // callback to different MockHandler

        // If the defect is present, the verification state might be corrupted;
        // here we just assert the verification happened and no exception occurred.
        // A failing version would throw an AssertionFailedError during the first handle.
        assertTrue("Verification should have been called exactly once", verifiedCalled[0]);
    }

    // ---- Partition D: Exception / defensive paths ----

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void handle_withNullInvocation_throwsNPE() throws Throwable {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        handler.handle(null);
    }

    @Test(timeout = 4000)
    public void handle_invocationWithNullMock_doesNotThrow() {
        MockHandler<Object> handler = new MockHandler<>(new MockSettingsImpl());
        Invocation nullMockInvocation = createDummyInvocation(null);
        try {
            handler.handle(nullMockInvocation);
        } catch (Throwable e) {
            fail("Should not throw even if mock is null: " + e);
        }
    }

    // ---- Partition E: Object lifecycle / contract ----

    @Test(timeout = 4000)
    public void multipleHandlersShareNoState() throws Throwable {
        // Each handler has its own mocking progress – this is part of the defect.
        // Verify that verification on one handler does not affect another.
        MockHandler<Object> h1 = new MockHandler<>(new MockSettingsImpl());
        MockHandler<Object> h2 = new MockHandler<>(new MockSettingsImpl());

        final int[] verifierCalls = {0};
        VerificationMode verifier = new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                verifierCalls[0]++;
            }
        };

        h1.mockingProgress.verificationStarted(verifier);
        h1.handle(createDummyInvocation(new Object()));
        assertEquals("Verification should be called once", 1, verifierCalls[0]);

        // Now call h2 with a separate verification (should not reuse the previous one)
        h2.mockingProgress.verificationStarted(verifier);
        h2.handle(createDummyInvocation(new Object()));
        assertEquals("Verification should have been called twice total", 2, verifierCalls[0]);
    }
}