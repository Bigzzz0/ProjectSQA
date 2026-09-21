package org.mockito.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

import org.mockito.InOrder;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.IOngoingStubbing;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.stubbing.OngoingStubbingImpl;
import org.mockito.internal.stubbing.StubberImpl;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.verification.VerificationDataImpl;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.InOrderContext;
import org.mockito.internal.verification.api.VerificationDataInOrder;
import org.mockito.internal.verification.api.VerificationDataInOrderImpl;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.DeprecatedOngoingStubbing;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;
import org.mockito.stubbing.Answers;
import org.mockito.verification.VerificationMode;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - mock(Class, MockSettings): normal creation, mockingStarted called.
 *   - stub(): pullOngoingStubbing, null branch -> reset + report.
 *   - stub(T) / when(T): stubbingStarted, then stub().
 *   - verify(T, VerificationMode): null mock -> report; non-mock -> report; valid -> verificationStarted, return mock.
 *   - reset(T...): validateState, reset, resetOngoingStubbing, resetMock each.
 *   - verifyNoMoreInteractions(Object...): assertMocksNotEmpty, validateState, null mock -> report, non-mock -> catch NotAMockException -> report, valid -> verify.
 *   - verifyNoMoreInteractionsInOrder(List, InOrderContext): validateState, finder, verifyInOrder.
 *   - inOrder(Object...): null/empty -> report, null element -> report, non-mock element -> report, valid -> InOrderImpl.
 *   - doAnswer(Answer): stubbingStarted, resetOngoingStubbing, StubberImpl.doAnswer.
 *   - stubVoid(T): getHandler, stubbingStarted, voidMethodStubbable.
 *   - validateMockitoUsage: validateState.
 *   - getLastInvocation: pullOngoingStubbing, getRegisteredInvocations, last element.
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments for mock, verify, reset, inOrder, verifyNoMoreInteractions.
 *   - empty arrays for reset, inOrder, verifyNoMoreInteractions.
 *   - zero-length mocks list.
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Defect: VerifyingWithAnExtraCallToADifferentMockInSameLine
 *     Scenario: When verifying a mock, an extra call to a different mock on the same line (as argument) should not interfere.
 *     Test: verify(mock2).add(mock1.get(0)) where mock1.get(0) is stubbed and mock2.add was previously called with the same value.
 *     Expected: verification passes (no exception). Bug causes AssertionFailedError.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - reporter.nullPassedToVerify() -> RuntimeException
 *   - reporter.notAMockPassedToVerify() -> RuntimeException
 *   - reporter.missingMethodInvocation() -> RuntimeException
 *   - reporter.nullPassedToVerifyNoMoreInteractions() -> RuntimeException
 *   - reporter.notAMockPassedToVerifyNoMoreInteractions() -> RuntimeException
 *   - reporter.mocksHaveToBePassedToVerifyNoMoreInteractions() -> RuntimeException
 *   - reporter.mocksHaveToBePassedWhenCreatingInOrder() -> RuntimeException
 *   - reporter.nullPassedWhenCreatingInOrder() -> RuntimeException
 *   - reporter.notAMockPassedWhenCreatingInOrder() -> RuntimeException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - InOrderImpl creation with valid mocks.
 *   - StubberImpl.doAnswer returns Stubber.
 *   - VoidMethodStubbable returned from stubVoid.
 */
public class MockitoCoreDeepseekTest {

    private final MockitoCore core = new MockitoCore();

    // Helper to create a mock with default settings
    private <T> T createMock(Class<T> classToMock) {
        MockSettingsImpl settings = new MockSettingsImpl();
        settings.defaultAnswer(Answers.RETURNS_DEFAULTS);
        return core.mock(classToMock, settings);
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testMockCreation() {
        List<?> mock = createMock(List.class);
        assertNotNull("Mock should not be null", mock);
        assertTrue("Should be a Mockito mock", new MockUtil().isMock(mock));
    }

    @Test(timeout = 4000)
    public void testStubWhenNoOngoingStubbing() {
        try {
            core.stub();
            fail("Should have thrown RuntimeException due to missing method invocation");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testStubWithMethodCall() {
        List<?> mock = createMock(List.class);
        // stub(mock) will call stubbingStarted then stub() which will fail because no ongoing stubbing
        try {
            core.stub(mock);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWhen() {
        List<?> mock = createMock(List.class);
        try {
            core.when(mock);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyWithNullMock() {
        try {
            core.verify(null, VerificationModeFactory.times(1));
            fail("Should have thrown RuntimeException for null mock");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyWithNonMock() {
        try {
            core.verify("not a mock", VerificationModeFactory.times(1));
            fail("Should have thrown RuntimeException for non-mock");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyWithValidMock() {
        List<?> mock = createMock(List.class);
        // Verification should succeed (no exception) even if no interactions
        Object result = core.verify(mock, VerificationModeFactory.times(0));
        assertSame("Should return the mock", mock, result);
    }

    @Test(timeout = 4000)
    public void testReset() {
        List<?> mock1 = createMock(List.class);
        List<?> mock2 = createMock(List.class);
        // Reset should not throw
        core.reset(mock1, mock2);
        // After reset, mock should still be a mock
        assertTrue(new MockUtil().isMock(mock1));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsNullArray() {
        try {
            core.verifyNoMoreInteractions((Object[]) null);
            fail("Should have thrown RuntimeException for null array");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsEmptyArray() {
        try {
            core.verifyNoMoreInteractions();
            fail("Should have thrown RuntimeException for empty array");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsNullElement() {
        try {
            core.verifyNoMoreInteractions((Object) null);
            fail("Should have thrown RuntimeException for null element");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsNonMockElement() {
        try {
            core.verifyNoMoreInteractions("not a mock");
            fail("Should have thrown RuntimeException for non-mock element");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsValidMock() {
        List<?> mock = createMock(List.class);
        // No interactions, so verification should pass
        core.verifyNoMoreInteractions(mock); // no exception
    }

    @Test(timeout = 4000)
    public void testInOrderNullArray() {
        try {
            core.inOrder((Object[]) null);
            fail("Should have thrown RuntimeException for null array");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderEmptyArray() {
        try {
            core.inOrder();
            fail("Should have thrown RuntimeException for empty array");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderNullElement() {
        try {
            core.inOrder((Object) null);
            fail("Should have thrown RuntimeException for null element");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderNonMockElement() {
        try {
            core.inOrder("not a mock");
            fail("Should have thrown RuntimeException for non-mock element");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderValidMocks() {
        List<?> mock1 = createMock(List.class);
        List<?> mock2 = createMock(List.class);
        InOrder inOrder = core.inOrder(mock1, mock2);
        assertNotNull("InOrder should not be null", inOrder);
        assertTrue("Should be InOrderImpl", inOrder instanceof InOrderImpl);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testVerifyWithExtraCallToDifferentMockInSameLine() {
        // This test targets the known defect:
        // VerifyingWithAnExtraCallToADifferentMockTest::shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine
        // The bug causes an AssertionFailedError when verifying a mock while an extra call to a different mock
        // is made on the same line (as an argument).
        List<?> mock1 = createMock(List.class);
        List<?> mock2 = createMock(List.class);

        // Stub mock1.get(0) to return "value"
        OngoingStubbing<String> stubbing = core.when(mock1.get(0));
        stubbing.thenReturn("value");

        // Record an invocation on mock2: mock2.add("value")
        mock2.add("value");

        // Now verify mock2.add(mock1.get(0)) – this line contains an extra call to mock1.get(0)
        // In a correct implementation, this verification should pass because mock2.add was called with "value".
        // The bug causes it to fail.
        core.verify(mock2, VerificationModeFactory.times(1)).add(mock1.get(0));
        // If we reach here, the test passes (no exception)
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testDoAnswer() {
        Answer<?> answer = invocation -> "answer";
        Stubber stubber = core.doAnswer(answer);
        assertNotNull("Stubber should not be null", stubber);
        assertTrue("Should be StubberImpl", stubber instanceof StubberImpl);
    }

    @Test(timeout = 4000)
    public void testStubVoid() {
        List<?> mock = createMock(List.class);
        VoidMethodStubbable<List<?>> stubbable = core.stubVoid(mock);
        assertNotNull("VoidMethodStubbable should not be null", stubbable);
    }

    @Test(timeout = 4000)
    public void testValidateMockitoUsage() {
        // Should not throw
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    public void testGetLastInvocation() {
        List<?> mock = createMock(List.class);
        // First, we need to have an ongoing stubbing to pull
        try {
            core.when(mock.get(0)); // This will fail because no method call before when
        } catch (RuntimeException e) {
            // Expected – we just need to set up state
        }
        // Now getLastInvocation should throw because ongoing stubbing was pulled and is null
        try {
            core.getLastInvocation();
            fail("Should have thrown NullPointerException or similar");
        } catch (NullPointerException e) {
            // expected – ongoingStubbing is null
        } catch (Exception e) {
            // any exception is acceptable
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testInOrderImplWithMultipleMocks() {
        List<?> mock1 = createMock(List.class);
        List<?> mock2 = createMock(List.class);
        InOrder inOrder = core.inOrder(mock1, mock2);
        assertNotNull(inOrder);
        // InOrderImpl should contain the mocks
        InOrderImpl impl = (InOrderImpl) inOrder;
        assertEquals(2, impl.getMocks().size());
    }

    @Test(timeout = 4000)
    public void testVerifyNoMoreInteractionsInOrder() {
        List<?> mock1 = createMock(List.class);
        List<?> mock2 = createMock(List.class);
        // No interactions, so verification should pass
        InOrderContext context = new InOrderContext() {
            @Override
            public boolean isVerified(Invocation invocation) {
                return false;
            }

            @Override
            public void markVerified(Invocation invocation) {
            }
        };
        core.verifyNoMoreInteractionsInOrder(java.util.Arrays.asList(mock1, mock2), context);
    }
}