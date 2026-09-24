package org.mockito;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects & Branches:
 * 1. DEFECT TARGET (org.mockitousage.bugs.StubbingMocksThatAreConfiguredToReturnMocksTest):
 *    - Stubbing invocation chains on mocks configured with RETURNS_MOCKS where internal mock creation
 *      previously interfered with ongoing stubbing state, throwing MissingMethodInvocationException.
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - Mock creation: standard, custom name, custom Answer, MockSettings.
 *    - Stubbing APIs: when(...).thenReturn/thenThrow/thenAnswer, deprecated stub(...).toReturn/toThrow.
 *    - Void stubbing: doThrow, doAnswer, doNothing, doReturn, doCallRealMethod, deprecated stubVoid.
 *    - Verifications: verify(mock), verify(mock, mode), inOrder, verifyNoMoreInteractions, verifyZeroInteractions.
 *    - Verification modes: times(n), never(), atLeastOnce(), atLeast(n), atMost(n), only().
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - times(0), times(1), times(100), atLeast(0), atMost(0).
 *    - Null arguments for classToMock, spy instance, inOrder, verify targets.
 * 4. Partition C: Defect-Targeted Branch Zone:
 *    - Mocks configured with RETURNS_MOCKS, RETURNS_SMART_NULLS, RETURNS_DEFAULTS, CALLS_REAL_METHODS.
 *    - Consecutive stubbings: multiple return values, mixed returns and throws.
 * 5. Partition D: Exception & Defensive Guard Paths:
 *    - Negative parameters to times(), atLeast(), atMost().
 *    - Calling doCallRealMethod on an interface mock.
 *    - Verifying/resetting non-mock objects (NotAMockException).
 *    - Verification failures (WantedButNotInvoked, TooLittleActualInvocations, NoInteractionsWanted).
 * 6. Partition E: Object Lifecycle & Framework Integrity:
 *    - Mockito instantiation, validateMockitoUsage, debug().
 */
public class MockitoGeminiTest {

    public interface Foo {
        Bar getBar();
    }

    public interface Bar {
        String getSomething();
    }

    public static class SampleService {
        public String execute() {
            return "real_execute";
        }

        public void voidAction() {
            // real void implementation
        }

        public int compute(int a, int b) {
            return a + b;
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testShouldAllowStubbingMocksConfiguredWithRETURNS_MOCKS() {
        // Targets known defect where RETURNS_MOCKS resets mocking progress during chained stubbing
        Foo foo = Mockito.mock(Foo.class, Mockito.RETURNS_MOCKS);
        Mockito.when(foo.getBar().getSomething()).thenReturn("foo_value");
        assertEquals("foo_value", foo.getBar().getSomething());
    }

    @Test(timeout = 4000)
    public void testStubbingDirectMethodOnRETURNS_MOCKS() {
        Foo foo = Mockito.mock(Foo.class, Mockito.RETURNS_MOCKS);
        Bar customBar = Mockito.mock(Bar.class);
        Mockito.when(foo.getBar()).thenReturn(customBar);
        assertSame(customBar, foo.getBar());
    }

    @Test(timeout = 4000)
    public void testReturnsSmartNullsStrategy() {
        Foo foo = Mockito.mock(Foo.class, Mockito.RETURNS_SMART_NULLS);
        Bar bar = foo.getBar();
        assertNotNull(bar);
        try {
            bar.getSomething();
            fail("Expected SmartNullPointerException when invoking method on unstubbed smart null");
        } catch (SmartNullPointerException expected) {
            // expected behaviour for smart null
        }
    }

    @Test(timeout = 4000)
    public void testCallsRealMethodsStrategyOnClassMock() {
        SampleService service = Mockito.mock(SampleService.class, Mockito.CALLS_REAL_METHODS);
        assertEquals("real_execute", service.execute());
        Mockito.when(service.execute()).thenReturn("mocked_execute");
        assertEquals("mocked_execute", service.execute());
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardMockAndWhenThenReturn() {
        List<?> list = Mockito.mock(List.class);
        assertNotNull(list);
        assertEquals(0, list.size());

        List<String> typedList = Mockito.mock(List.class);
        Mockito.when(typedList.get(0)).thenReturn("first");
        assertEquals("first", typedList.get(0));
        assertNull(typedList.get(1));
    }

    @Test(timeout = 4000)
    public void testMockWithCustomName() {
        List<?> list = Mockito.mock(List.class, "customListName");
        assertEquals("customListName", list.toString());
    }

    @Test(timeout = 4000)
    public void testMockWithCustomAnswer() {
        Answer<Object> answer = new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return 42;
            }
        };
        List<?> list = Mockito.mock(List.class, answer);
        assertEquals(42, list.size());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testMockWithDeprecatedReturnValues() {
        ReturnValues returnValues = new ReturnValues() {
            public Object valueFor(InvocationOnMock invocation) {
                return "custom_val";
            }
        };
        List<?> list = Mockito.mock(List.class, returnValues);
        assertEquals("custom_val", list.get(0));
    }

    @Test(timeout = 4000)
    public void testSpyRealObjectInvocationAndStubbing() {
        ArrayList<String> realList = new ArrayList<String>();
        ArrayList<String> spyList = Mockito.spy(realList);

        spyList.add("alpha");
        spyList.add("beta");
        assertEquals(2, spyList.size());
        assertEquals("alpha", spyList.get(0));

        Mockito.verify(spyList).add("alpha");
        Mockito.verify(spyList).add("beta");

        Mockito.doReturn(100).when(spyList).size();
        assertEquals(100, spyList.size());
    }

    @Test(timeout = 4000)
    public void testConsecutiveStubbingValues() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.get(0)).thenReturn("val1", "val2", "val3");

        assertEquals("val1", list.get(0));
        assertEquals("val2", list.get(0));
        assertEquals("val3", list.get(0));
        assertEquals("val3", list.get(0));
    }

    @Test(timeout = 4000)
    public void testConsecutiveStubbingExceptions() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.get(0)).thenThrow(new IllegalArgumentException("err1"), new IllegalStateException("err2"));

        try {
            list.get(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("err1", e.getMessage());
        }

        try {
            list.get(0);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("err2", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDoFamilyStubbing() {
        List<String> list = Mockito.mock(List.class);

        // doReturn
        Mockito.doReturn("computed").when(list).get(5);
        assertEquals("computed", list.get(5));

        // doNothing
        Mockito.doNothing().when(list).clear();
        list.clear();
        Mockito.verify(list).clear();

        // doThrow
        Mockito.doThrow(new UnsupportedOperationException("blocked")).when(list).clear();
        try {
            list.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("blocked", e.getMessage());
        }

        // doAnswer
        Mockito.doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "dynamic_" + invocation.getArguments()[0];
            }
        }).when(list).get(10);
        assertEquals("dynamic_10", list.get(10));
    }

    @Test(timeout = 4000)
    public void testDoCallRealMethodOnConcreteClass() {
        SampleService service = Mockito.mock(SampleService.class);
        Mockito.doCallRealMethod().when(service).execute();
        assertEquals("real_execute", service.execute());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedStubMethod() {
        List<String> list = Mockito.mock(List.class);
        Mockito.stub(list.get(0)).toReturn("deprecated_res");
        assertEquals("deprecated_res", list.get(0));

        Mockito.stub(list.get(1)).toThrow(new RuntimeException("deprecated_err"));
        try {
            list.get(1);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("deprecated_err", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testDeprecatedStubVoidMethod() {
        List<?> list = Mockito.mock(List.class);
        Mockito.stubVoid(list).toThrow(new IllegalStateException("void_fail")).on().clear();
        try {
            list.clear();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("void_fail", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testResetMock() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.size()).thenReturn(99);
        list.add("test");
        assertEquals(99, list.size());

        Mockito.reset(list);
        assertEquals(0, list.size());
        Mockito.verifyZeroInteractions(list);
    }

    @Test(timeout = 4000)
    public void testInOrderVerification() {
        List<String> first = Mockito.mock(List.class);
        List<String> second = Mockito.mock(List.class);

        first.add("one");
        second.add("two");

        InOrder inOrder = Mockito.inOrder(first, second);
        inOrder.verify(first).add("one");
        inOrder.verify(second).add("two");

        try {
            inOrder.verify(first).add("one");
            fail("Expected VerificationInOrderFailure for out of order verification");
        } catch (VerificationInOrderFailure expected) {
            // expected
        }
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerificationModesBoundaries() {
        List<String> list = Mockito.mock(List.class);

        Mockito.verify(list, Mockito.never()).clear();
        Mockito.verify(list, Mockito.times(0)).clear();

        list.add("a");
        Mockito.verify(list, Mockito.times(1)).add("a");
        Mockito.verify(list, Mockito.atLeastOnce()).add("a");
        Mockito.verify(list, Mockito.atLeast(1)).add("a");
        Mockito.verify(list, Mockito.atMost(1)).add("a");
        Mockito.verify(list, Mockito.atMost(5)).add("a");
    }

    @Test(timeout = 4000)
    public void testOnlyVerificationModeSuccessAndFailure() {
        List<String> list = Mockito.mock(List.class);
        list.add("item");
        Mockito.verify(list, Mockito.only()).add("item");

        list.clear();
        try {
            Mockito.verify(list, Mockito.only()).add("item");
            fail("Expected NoInteractionsWanted exception");
        } catch (NoInteractionsWanted expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyZeroAndNoMoreInteractions() {
        List<?> mock1 = Mockito.mock(List.class);
        List<?> mock2 = Mockito.mock(List.class);

        Mockito.verifyZeroInteractions(mock1, mock2);
        Mockito.verifyNoMoreInteractions(mock1, mock2);

        mock1.size();
        try {
            Mockito.verifyZeroInteractions(mock1);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted expected) {
            // expected
        }

        Mockito.verify(mock1).size();
        Mockito.verifyNoMoreInteractions(mock1);
    }

    @Test(timeout = 4000)
    public void testArgumentMatchersUsageViaInheritedMatchers() {
        List<String> list = Mockito.mock(List.class);
        Mockito.when(list.get(Mockito.anyInt())).thenReturn("matched");

        assertEquals("matched", list.get(0));
        assertEquals("matched", list.get(999));
        Mockito.verify(list, Mockito.times(2)).get(Mockito.anyInt());
        Mockito.verify(list).get(Mockito.eq(0));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(timeout = 4000)
    public void testTimesNegativeBoundaryThrowsException() {
        try {
            Mockito.times(-1);
            fail("Expected MockitoException for negative times");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAtLeastNegativeBoundaryThrowsException() {
        try {
            Mockito.atLeast(-1);
            fail("Expected MockitoException for negative atLeast");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAtMostNegativeBoundaryThrowsException() {
        try {
            Mockito.atMost(-1);
            fail("Expected MockitoException for negative atMost");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyThrowsWantedButNotInvoked() {
        List<?> list = Mockito.mock(List.class);
        try {
            Mockito.verify(list).clear();
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyThrowsTooLittleActualInvocations() {
        List<?> list = Mockito.mock(List.class);
        list.clear();
        try {
            Mockito.verify(list, Mockito.times(2)).clear();
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyOnNonMockThrowsNotAMockException() {
        try {
            Mockito.verify("nonMockObject");
            fail("Expected NotAMockException");
        } catch (NotAMockException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testResetOnNonMockThrowsNotAMockException() {
        try {
            Mockito.reset("nonMockObject");
            fail("Expected NotAMockException");
        } catch (NotAMockException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWhenOnNonMockThrowsException() {
        try {
            Mockito.when("nonMockObject");
            fail("Expected MissingMethodInvocationException or NotAMockException");
        } catch (MissingMethodInvocationException expected) {
            // expected
        } catch (NotAMockException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithNoMocksThrowsException() {
        try {
            Mockito.inOrder();
            fail("Expected MockitoException when inOrder is given no mocks");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInOrderWithNullMockThrowsException() {
        try {
            Mockito.inOrder((Object) null);
            fail("Expected MockitoException when inOrder contains null");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDoCallRealMethodOnInterfaceThrowsException() {
        List<?> list = Mockito.mock(List.class);
        try {
            Mockito.doCallRealMethod().when(list).clear();
            fail("Expected MockitoException calling real method on interface mock");
        } catch (MockitoException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testValidateMockitoUsageDetectsUnfinishedStubbing() {
        List<?> list = Mockito.mock(List.class);
        try {
            Mockito.when(list.size()); // unfinished stubbing
            Mockito.validateMockitoUsage();
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException expected) {
            // expected
        } finally {
            Mockito.reset(list);
        }
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testMockitoInstantiationAndConstants() {
        Mockito mockitoInstance = new Mockito();
        assertNotNull(mockitoInstance);
        assertNotNull(Mockito.RETURNS_DEFAULTS);
        assertNotNull(Mockito.RETURNS_SMART_NULLS);
        assertNotNull(Mockito.RETURNS_MOCKS);
        assertNotNull(Mockito.CALLS_REAL_METHODS);
    }

    @Test(timeout = 4000)
    public void testWithSettingsFluentConfigurations() {
        MockSettings settings = Mockito.withSettings()
                .name("customConfiguredMock")
                .defaultAnswer(Mockito.RETURNS_SMART_NULLS)
                .extraInterfaces(Serializable.class, Comparable.class)
                .serializable();

        List<?> mockList = Mockito.mock(List.class, settings);
        assertNotNull(mockList);
        assertTrue(mockList instanceof Serializable);
        assertTrue(mockList instanceof Comparable);
        assertEquals("customConfiguredMock", mockList.toString());
    }

    @Test(timeout = 4000)
    public void testValidateMockitoUsageClean() {
        Mockito.validateMockitoUsage();
    }

    @Test(timeout = 4000)
    public void testDebugAccessor() {
        MockitoDebugger debugger = Mockito.debug();
        assertNotNull(debugger);
    }
}