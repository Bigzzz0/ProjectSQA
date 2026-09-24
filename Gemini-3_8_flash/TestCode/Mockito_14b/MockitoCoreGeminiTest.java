package org.mockito.internal;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.MockitoCore
 * Primary Branches & Boundary Conditions Covered:
 * 1. mock(Class<T>, MockSettings):
 *    - Standard interface mock creation (List.class)
 *    - Concrete class mock creation (ArrayList.class)
 * 2. stub():
 *    - Branch: ongoingStubbing == null -> triggers missingMethodInvocation Reporter exception
 *    - Branch: ongoingStubbing != null -> returns active IOngoingStubbing instance
 * 3. stub(T methodCall) / when(T methodCall):
 *    - Transitions state via stubbingStarted()
 *    - Returns DeprecatedOngoingStubbing and OngoingStubbing contracts respectively
 * 4. verify(T mock, VerificationMode mode):
 *    - Branch: mock == null -> reporter.nullPassedToVerify() (NullInsteadOfMockException)
 *    - Branch: !mockUtil.isMock(mock) -> reporter.notAMockPassedToVerify() (NotAMockException)
 *    - Branch: valid mock -> verificationStarted(mode), returns mock instance
 * 5. reset(T ... mocks):
 *    - Validates state, resets mocking progress and ongoing stubbing, resets individual mocks
 *    - Boundary: empty mocks array, single mock, multiple mocks
 * 6. verifyNoMoreInteractions(Object... mocks):
 *    - Branch: assertMocksNotEmpty -> null array or empty array throws MockitoException
 *    - Branch: mock == null -> reporter.nullPassedToVerifyNoMoreInteractions()
 *    - Branch: mock is not a mock (catches NotAMockException) -> reporter.notAMockPassedToVerifyNoMoreInteractions()
 *    - Branch: mock has unverified interactions -> throws NoInteractionsWanted
 *    - Branch: mock with all interactions verified -> succeeds
 * 7. verifyNoMoreInteractionsInOrder(List<Object>, InOrderContext):
 *    - Branch: mocks list empty / verified context -> succeeds
 *    - Branch: mock has unverified invocation in order context -> throws VerificationInOrderFailure
 * 8. inOrder(Object... mocks):
 *    - Branch: mocks == null || mocks.length == 0 -> reporter.mocksHaveToBePassedWhenCreatingInOrder()
 *    - Branch: element == null -> reporter.nullPassedWhenCreatingInOrder()
 *    - Branch: element !mock -> reporter.notAMockPassedWhenCreatingInOrder()
 *    - Branch: valid mocks -> returns InOrderImpl
 * 9. doAnswer(Answer) & stubVoid(T mock):
 *    - doAnswer: starts stubbing, resets ongoing stubbing, returns Stubber
 *    - stubVoid: retrieves MockHandlerInterface, starts stubbing, returns VoidMethodStubbable
 * 10. validateMockitoUsage():
 *    - Clean state -> passes without error
 *    - Unfinished verification or stubbing state -> throws UnfinishedVerificationException
 * 11. getLastInvocation():
 *    - Retrieves registered invocations from ongoing stubbing and extracts final invocation
 * 12. Defect Zone (VerifyingWithAnExtraCallToADifferentMockTest):
 *    - Calls to a secondary mock within the argument of verify(firstMock) must not steal the verification
 *      mode nor trigger unexpected verification failures on the secondary mock.
 * ----------------------------------------------------------------------------------------------------
 */

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.mockito.InOrder;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.progress.IOngoingStubbing;
import org.mockito.internal.progress.ThreadSafeMockingProgress;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.InOrderContext;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.DeprecatedOngoingStubbing;
import org.mockito.stubbing.OngoingStubbing;
import org.mockito.stubbing.Stubber;
import org.mockito.stubbing.VoidMethodStubbable;

public class MockitoCoreGeminiTest {

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
    public void testMockCreationForInterfaceAndClass() {
        List<?> interfaceMock = core.mock(List.class, new MockSettingsImpl());
        assertNotNull("Interface mock should not be null", interfaceMock);

        ArrayList<?> classMock = core.mock(ArrayList.class, new MockSettingsImpl());
        assertNotNull("Class mock should not be null", classMock);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testWhenThenReturnStubbing() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        OngoingStubbing<String> ongoing = core.when(mock.get(0));
        assertNotNull("OngoingStubbing should not be null", ongoing);
        ongoing.thenReturn("firstElement");

        assertEquals("firstElement", mock.get(0));
        assertNull("Unstubbed index should return default null", mock.get(1));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testDeprecatedStubToReturn() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        DeprecatedOngoingStubbing<String> depOngoing = core.stub(mock.get(5));
        assertNotNull("DeprecatedOngoingStubbing should not be null", depOngoing);
        depOngoing.toReturn("stubbedValue");

        assertEquals("stubbedValue", mock.get(5));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testDoAnswerStubbing() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return "computed_" + invocation.getArguments()[0];
            }
        };

        Stubber stubber = core.doAnswer(customAnswer);
        assertNotNull("Stubber should not be null", stubber);
        stubber.when(mock).get(42);

        assertEquals("computed_42", mock.get(42));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testStubVoidMethod() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        VoidMethodStubbable<List<String>> voidStubbable = core.stubVoid(mock);
        assertNotNull("VoidMethodStubbable should not be null", voidStubbable);

        voidStubbable.toThrow(new IllegalStateException("blocked")).on().clear();

        try {
            mock.clear();
            fail("Expected IllegalStateException from void stubbing");
        } catch (IllegalStateException e) {
            assertEquals("blocked", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testVerifyNormalInvocation() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("sample");

        List<String> verifiedMock = core.verify(mock, VerificationModeFactory.times(1));
        assertSame("verify() must return the same mock instance", mock, verifiedMock);
        verifiedMock.add("sample");

        core.verifyNoMoreInteractions(mock);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testResetMocks() {
        List<String> mock1 = core.mock(List.class, new MockSettingsImpl());
        List<String> mock2 = core.mock(List.class, new MockSettingsImpl());

        mock1.add("one");
        mock2.add("two");

        core.reset(mock1, mock2);

        // After reset, interactions must be empty
        core.verifyNoMoreInteractions(mock1);
        core.verifyNoMoreInteractions(mock2);
    }

    @Test(timeout = 4000)
    public void testResetWithEmptyVarArgs() {
        core.reset();
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testGetLastInvocation() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("itemA");

        Invocation lastInvocation = core.getLastInvocation();
        assertNotNull("Last invocation should not be null", lastInvocation);
        assertEquals("add", lastInvocation.getMethod().getName());
        assertEquals("itemA", lastInvocation.getArguments()[0]);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testInOrderExecutionSuccess() {
        List<String> mockA = core.mock(List.class, new MockSettingsImpl());
        List<String> mockB = core.mock(List.class, new MockSettingsImpl());

        mockA.add("first");
        mockB.add("second");

        InOrder inOrder = core.inOrder(mockA, mockB);
        assertNotNull("InOrder instance should not be null", inOrder);

        inOrder.verify(mockA).add("first");
        inOrder.verify(mockB).add("second");
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testVerifyNoMoreInteractionsInOrderSuccess() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("entry");

        InOrderContext context = new InOrderContext() {
            @Override
            public boolean isVerified(Invocation invocation) {
                return true;
            }

            @Override
            public void markVerified(Invocation invocation) {}
        };

        core.verifyNoMoreInteractionsInOrder(Collections.singletonList((Object) mock), context);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testVerifyNoMoreInteractionsNullArray() {
        core.verifyNoMoreInteractions((Object[]) null);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testVerifyNoMoreInteractionsEmptyArray() {
        core.verifyNoMoreInteractions(new Object[0]);
    }

    @Test(timeout = 4000, expected = NullInsteadOfMockException.class)
    public void testVerifyNoMoreInteractionsNullElement() {
        core.verifyNoMoreInteractions(new Object[] { null });
    }

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testVerifyNoMoreInteractionsNonMockElement() {
        core.verifyNoMoreInteractions("this is not a mock");
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testInOrderNullArray() {
        core.inOrder((Object[]) null);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testInOrderEmptyArray() {
        core.inOrder(new Object[0]);
    }

    @Test(timeout = 4000, expected = NullInsteadOfMockException.class)
    public void testInOrderNullElement() {
        core.inOrder(new Object[] { null });
    }

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testInOrderNonMockElement() {
        core.inOrder("stringLiteral");
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testInOrderMixedValidAndInvalidElements() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        try {
            core.inOrder(mock, null);
            fail("Expected NullInsteadOfMockException for null element in inOrder");
        } catch (NullInsteadOfMockException expected) {
            assertNotNull(expected.getMessage());
        }

        try {
            core.inOrder(mock, new Object());
            fail("Expected NotAMockException for non-mock element in inOrder");
        } catch (NotAMockException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone
    // Defects4J: VerifyingWithAnExtraCallToADifferentMockTest
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void shouldAllowVerifyingWhenOtherMockCallIsInTheSameLine() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        List<String> mockTwo = core.mock(List.class, new MockSettingsImpl());

        mockTwo.get(0);
        mock.add(mockTwo.get(0));

        // When evaluating the argument mockTwo.get(0), it must not consume the verification mode of mock
        core.verify(mock, VerificationModeFactory.times(1)).add(mockTwo.get(0));
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void shouldAllowVerifyingWhenOtherMockHasReturnValueInTheSameLine() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        List<String> mockTwo = core.mock(List.class, new MockSettingsImpl());

        core.when(mockTwo.get(0)).thenReturn("nestedArg");
        mock.add("nestedArg");

        core.verify(mock, VerificationModeFactory.times(1)).add(mockTwo.get(0));
        core.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void shouldMaintainAccurateVerificationCountWhenOtherMockInteractedInsideVerify() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        List<String> mockTwo = core.mock(List.class, new MockSettingsImpl());

        mockTwo.get(10);
        mock.add(mockTwo.get(10));

        core.verify(mock, VerificationModeFactory.times(1)).add(mockTwo.get(10));

        // Verify that mockTwo was not verified instead of mock
        core.verify(mockTwo, VerificationModeFactory.times(2)).get(10);
        core.verifyNoMoreInteractions(mock);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = MissingMethodInvocationException.class)
    public void testStubWithoutOngoingInvocationThrowsException() {
        core.stub();
    }

    @Test(timeout = 4000, expected = NullInsteadOfMockException.class)
    public void testVerifyNullMockThrowsException() {
        core.verify(null, VerificationModeFactory.times(1));
    }

    @Test(timeout = 4000, expected = NotAMockException.class)
    public void testVerifyNonMockThrowsException() {
        core.verify("NotAMockObject", VerificationModeFactory.times(1));
    }

    @Test(timeout = 4000, expected = NoInteractionsWanted.class)
    @SuppressWarnings("unchecked")
    public void testVerifyNoMoreInteractionsFailsOnUnverifiedCall() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("unverified");
        core.verifyNoMoreInteractions(mock);
    }

    @Test(timeout = 4000, expected = VerificationInOrderFailure.class)
    @SuppressWarnings("unchecked")
    public void testVerifyNoMoreInteractionsInOrderFailureOnUnverifiedInvocation() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("unverifiedInOrder");

        InOrderContext context = new InOrderContext() {
            @Override
            public boolean isVerified(Invocation invocation) {
                return false;
            }

            @Override
            public void markVerified(Invocation invocation) {}
        };

        core.verifyNoMoreInteractionsInOrder(Collections.singletonList((Object) mock), context);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testValidateMockitoUsageCatchesUnfinishedVerification() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        core.verify(mock, VerificationModeFactory.times(1)); // Verification started but method never invoked

        try {
            core.validateMockitoUsage();
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException expected) {
            assertNotNull(expected.getMessage());
        } finally {
            new ThreadSafeMockingProgress().reset();
        }
    }

    @Test(timeout = 4000)
    public void testValidateMockitoUsageCleanStateSucceeds() {
        core.validateMockitoUsage();
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testPullOngoingStubbingDirectly() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.get(100);

        IOngoingStubbing ongoing = core.stub();
        assertNotNull("Direct pull of ongoing stubbing must not be null", ongoing);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testMultipleSequentialVerifications() {
        List<String> mock = core.mock(List.class, new MockSettingsImpl());
        mock.add("a");
        mock.add("b");
        mock.clear();

        core.verify(mock, VerificationModeFactory.times(1)).add("a");
        core.verify(mock, VerificationModeFactory.times(1)).add("b");
        core.verify(mock, VerificationModeFactory.times(1)).clear();
        core.verifyNoMoreInteractions(mock);
    }
}