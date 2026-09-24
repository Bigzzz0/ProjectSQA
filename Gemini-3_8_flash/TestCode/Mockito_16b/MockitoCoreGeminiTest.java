package org.mockito.internal;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.mockito.internal.MockitoCore
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. mock(Class, MockSettings, boolean shouldResetOngoingStubbing):
 *    - Branch: shouldResetOngoingStubbing = false vs true.
 *    - DEFECT (Defects4J): mock(Class, MockSettings, boolean) ignores the boolean flag and unconditionally resets
 *      ongoing stubbing. This breaks stubbing of mocks configured with RETURNS_MOCKS or chained calls where a mock
 *      is created during the evaluation of the method call being stubbed.
 * 2. stub():
 *    - Branch: stubbing == null (missing method invocation -> reporter.missingMethodInvocation()).
 *    - Branch: stubbing != null (returns ongoing stubbing).
 * 3. when(T methodCall) & stub(T methodCall):
 *    - Branch: Valid method call stubbing vs calling when() without preceding invocation.
 * 4. verify(T mock, VerificationMode mode):
 *    - Branch: mock == null -> reporter.nullPassedToVerify().
 *    - Branch: !mockUtil.isMock(mock) -> reporter.notAMockPassedToVerify().
 *    - Branch: Valid mock -> verificationStarted(mode) and returns mock.
 * 5. reset(T... mocks):
 *    - Branch: empty array (no-op on loop, resets state).
 *    - Branch: single mock reset.
 *    - Branch: multiple mocks reset.
 * 6. verifyNoMoreInteractions(Object... mocks):
 *    - Branch: mocks == null -> reporter.mocksHaveToBePassedToVerifyNoMoreInteractions().
 *    - Branch: mocks.length == 0 -> reporter.mocksHaveToBePassedToVerifyNoMoreInteractions().
 *    - Branch: mock in mocks == null -> reporter.nullPassedToVerifyNoMoreInteractions().
 *    - Branch: mock in mocks not a mock (catches NotAMockException) -> reporter.notAMockPassedToVerifyNoMoreInteractions().
 *    - Branch: valid mocks -> verifyNoMoreInteractions passes or detects unverified interactions.
 * 7. inOrder(Object... mocks):
 *    - Branch: mocks == null -> reporter.mocksHaveToBePassedWhenCreatingInOrder().
 *    - Branch: mocks.length == 0 -> reporter.mocksHaveToBePassedWhenCreatingInOrder().
 *    - Branch: mock in mocks == null -> reporter.nullPassedWhenCreatingInOrder().
 *    - Branch: mock in mocks not a mock -> reporter.notAMockPassedWhenCreatingInOrder().
 *    - Branch: valid mocks -> creates InOrderImpl successfully.
 * 8. doAnswer(Answer answer):
 *    - Normal flow: sets answer, starts stubbing, returns StubberImpl.
 * 9. stubVoid(T mock):
 *    - Branch: valid mock -> returns VoidMethodStubbable.
 *    - Branch: non-mock -> throws NotAMockException.
 * 10. validateMockitoUsage():
 *     - Branch: valid state -> succeeds silently.
 *     - Branch: invalid/unfinished state -> throws MockitoException.
 * 11. getLastInvocation():
 *     - Branch: invocations present -> returns most recent invocation.
 *     - Branch: no ongoing stubbing -> throws NullPointerException.
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.IOngoingStubbing;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.defaultanswers.ReturnsMocks;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.DeprecatedOngoingStubbing;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;

public class MockitoCoreGeminiTest {

    public interface TestSubject {
        String execute();
        TestSubject getNested();
        void performAction();
    }

    private MockitoCore core;

    @Before
    public void setUp() {
        core = new MockitoCore();
        new ThreadSafeMockingProgress().reset();
    }

    @After
    public void tearDown() {
        new ThreadSafeMockingProgress().reset();
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMockCreationAndSimpleStubbingWithWhen() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);
        assertNotNull("Mock instance should not be null", mock);

        OngoingStubbing<String> stubbing = core.when(mock.execute());
        assertNotNull("OngoingStubbing should not be null", stubbing);
        stubbing.thenReturn("alpha");

        assertEquals("alpha", mock.execute());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedStubMethod() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        DeprecatedOngoingStubbing<String> stubbing = core.stub(mock.execute());
        assertNotNull("DeprecatedOngoingStubbing should not be null", stubbing);
        stubbing.toReturn("deprecated_value");

        assertEquals("deprecated_value", mock.execute());
    }

    @Test(timeout = 4000)
    public void testVerifySuccessfulInvocation() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        mock.execute();

        TestSubject verifiedMock = core.verify(mock, VerificationModeFactory.times(1));
        assertSame("verify should return the same mock instance", mock, verifiedMock);
        verifiedMock.execute();
    }

    @Test(timeout = 4000)
    public void testResetWithSingleAndMultipleMocks() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock1 = core.mock(TestSubject.class, settings);
        TestSubject mock2 = core.mock(TestSubject.class, settings);

        core.when(mock1.execute()).thenReturn("mock1");
        core.when(mock2.execute()).thenReturn("mock2");

        assertEquals("mock1", mock1.execute());
        assertEquals("mock2", mock2.execute());

        core.reset(mock1, mock2);

        assertNull("Reset mock should return default answer (null)", mock1.execute());
        assertNull("Reset mock should return default answer (null)", mock2.execute());
    }

    @Test(timeout = 4000)
    public void testResetWithEmptyArgs() {
        core.reset();
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsSuccess() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        mock.execute();
        core.verify(mock, VerificationModeFactory.times(1)).execute();

        core.verifyNoMoreInteractions(mock);
    }

    @Test(timeout = 4000)
    public void testInOrderSuccess() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock1 = core.mock(TestSubject.class, settings);
        TestSubject mock2 = core.mock(TestSubject.class, settings);

        mock1.execute();
        mock2.execute();

        InOrder inOrder = core.inOrder(mock1, mock2);
        assertNotNull("InOrder object should be created", inOrder);
        inOrder.verify(mock1).execute();
        inOrder.verify(mock2).execute();
    }

    @Test(timeout = 4000)
    public void testDoAnswerStubbing() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        Stubber stubber = core.doAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) {
                return "custom_answer";
            }
        });

        stubber.when(mock).execute();

        assertEquals("custom_answer", mock.execute());
    }

    @Test(timeout = 4000)
    public void testStubVoidSuccess() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        VoidMethodStubbable<TestSubject> voidStubbable = core.stubVoid(mock);
        assertNotNull("VoidMethodStubbable should not be null", voidStubbable);

        voidStubbable.toThrow(new IllegalStateException("void_fail")).on().performAction();

        try {
            mock.performAction();
            fail("Expected IllegalStateException from stubbed void method");
        } catch (IllegalStateException e) {
            assertEquals("void_fail", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGetLastInvocation() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        mock.execute();
        Invocation invocation = core.getLastInvocation();
        assertNotNull("Last invocation should not be null", invocation);
        assertEquals("execute", invocation.getMethod().getName());
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsWithNullVarargs() {
        try {
            core.verifyNoMoreInteractions((Object[]) null);
            fail("Expected MockitoException for null array in verifyNoMoreInteractions");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsWithEmptyArray() {
        try {
            core.verifyNoMoreInteractions(new Object[0]);
            fail("Expected MockitoException for empty array in verifyNoMoreInteractions");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsWithNullElement() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);
        try {
            core.verifyNoMoreInteractions(mock, null);
            fail("Expected MockitoException when a null mock is passed to verifyNoMoreInteractions");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithNullVarargs() {
        try {
            core.inOrder((Object[]) null);
            fail("Expected MockitoException when null array passed to inOrder");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithEmptyArray() {
        try {
            core.inOrder(new Object[0]);
            fail("Expected MockitoException when empty array passed to inOrder");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithNullElement() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);
        try {
            core.inOrder(mock, null);
            fail("Expected MockitoException when null element passed to inOrder");
        } catch (MockitoException expected) {
            // Success
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J Ground Truth Defect:
     * org.mockitousage.bugs.StubbingMocksThatAreConfiguredToReturnMocksTest::shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS
     *
     * In the defective implementation:
     * public <T> T mock(Class<T> classToMock, MockSettings mockSettings, boolean shouldResetOngoingStubbing)
     * delegates to mock(classToMock, mockSettings) which unconditionally resets ongoing stubbing.
     * When a mock configured with ReturnsMocks is called during when(mock.getNested()), ReturnsMocks
     * creates an internal child mock via mock(..., shouldResetOngoingStubbing=false).
     * The bug resets the ongoing stubbing of getNested(), causing when() to fail with MissingMethodInvocationException.
     */
    @Test(timeout = 4000)
    public void shouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS() {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(new ReturnsMocks());

        TestSubject mock = core.mock(TestSubject.class, settings);

        // This invocation invokes ReturnsMocks, which internally calls mock(..., shouldResetOngoingStubbing=false).
        // On the defective code, this resets the ongoing stubbing and triggers MissingMethodInvocationException.
        OngoingStubbing<TestSubject> stubbing = core.when(mock.getNested());
        assertNotNull("Stubbing should not be null when configuring returns-mocks mock", stubbing);

        stubbing.thenReturn(null);
        assertNull("Nested mock stubbing should take effect", mock.getNested());
    }

    /**
     * Directly exercises the 3-argument mock method with shouldResetOngoingStubbing = false
     * to verify whether ongoing stubbing is preserved.
     */
    @Test(timeout = 4000)
    public void testMockMethodHonorsShouldResetOngoingStubbingFalse() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        // Record an invocation to establish ongoing stubbing
        mock.execute();

        // Create a mock specifying shouldResetOngoingStubbing = false
        core.mock(TestSubject.class, new MockSettingsImpl(), false);

        // In fixed Mockito, ongoing stubbing is retained. In defective version, it was cleared.
        IOngoingStubbing ongoingStubbing = core.stub();
        assertNotNull("Ongoing stubbing must be preserved when shouldResetOngoingStubbing is false", ongoingStubbing);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = MissingMethodInvocationException.class)
    public void testWhenWithoutInvocationThrowsMissingMethodInvocation() {
        core.when(null);
    }

    @Test(timeout = 4000, expected = MissingMethodInvocationException.class)
    public void testStubWithoutInvocationThrowsMissingMethodInvocation() {
        core.stub();
    }

    @Test(timeout = 4000)
    public void testVerifyWithNullMockThrowsMockitoException() {
        try {
            core.verify(null, VerificationModeFactory.times(1));
            fail("Expected MockitoException when passing null to verify()");
        } catch (MockitoException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testVerifyWithNonMockThrowsNotAMockException() {
        try {
            core.verify("Not A Mock Object", VerificationModeFactory.times(1));
            fail("Expected NotAMockException when passing non-mock to verify()");
        } catch (NotAMockException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsWithNonMockThrowsNotAMockException() {
        try {
            core.verifyNoMoreInteractions("InvalidStringMock");
            fail("Expected NotAMockException when non-mock passed to verifyNoMoreInteractions");
        } catch (NotAMockException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithNonMockThrowsNotAMockException() {
        try {
            core.inOrder("NotAMock");
            fail("Expected NotAMockException when non-mock passed to inOrder");
        } catch (NotAMockException expected) {
            // Success
        }
    }

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testStubVoidWithNonMockThrowsNotAMockException() {
        core.stubVoid(new Object());
    }

    @Test(timeout = 4000)
    public void testValidateMockitoUsageDetectsUnfinishedVerification() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mock = core.mock(TestSubject.class, settings);

        // Start verification without completing it with a method call
        core.verify(mock, VerificationModeFactory.times(1));

        try {
            core.validateMockitoUsage();
            fail("Expected UnfinishedVerificationException due to incomplete verify()");
        } catch (UnfinishedVerificationException expected) {
            // Success
        }
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetLastInvocationThrowsWhenNoOngoingStubbing() {
        core.getLastInvocation();
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidateMockitoUsageSucceedsOnCleanState() {
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialMocksLifecycle() {
        MockSettingsImpl settings = new MockSettingsImpl();
        TestSubject mockA = core.mock(TestSubject.class, settings);
        TestSubject mockB = core.mock(TestSubject.class, settings);

        core.when(mockA.execute()).thenReturn("A");
        core.when(mockB.execute()).thenReturn("B");

        assertEquals("A", mockA.execute());
        assertEquals("B", mockB.execute());

        core.verify(mockA, VerificationModeFactory.times(1)).execute();
        core.verify(mockB, VerificationModeFactory.times(1)).execute();

        core.verifyNoMoreInteractions(mockA, mockB);
        core.validateMockitoUsage();
    }
}