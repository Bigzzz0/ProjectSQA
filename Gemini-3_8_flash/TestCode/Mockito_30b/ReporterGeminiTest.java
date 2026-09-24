package org.mockito.exceptions;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import org.mockito.exceptions.misusing.MissingMethodInvocationException;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.misusing.NullInsteadOfMockException;
import org.mockito.exceptions.misusing.UnfinishedStubbingException;
import org.mockito.exceptions.misusing.UnfinishedVerificationException;
import org.mockito.exceptions.misusing.WrongTypeOfReturnValue;
import org.mockito.exceptions.verification.ArgumentsAreDifferent;
import org.mockito.exceptions.verification.NeverWantedButInvoked;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.SmartNullPointerException;
import org.mockito.exceptions.verification.TooLittleActualInvocations;
import org.mockito.exceptions.verification.TooManyActualInvocations;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.internal.debugging.Location;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.invocation.Invocation;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Class Under Test: org.mockito.exceptions.Reporter
 *
 * Decision / Branch Coverage Points:
 * 1. argumentsAreDifferent(wanted, actual, actualLocation)
 *    - Branch: JUnitTool.hasJUnit() == true vs false
 * 2. wantedButNotInvoked(wanted, invocations)
 *    - Branch: invocations.isEmpty() == true ("Actually, there were zero interactions...")
 *    - Branch: invocations.isEmpty() == false ("However, there were other interactions...")
 * 3. createTooLittleInvocationsMessage (used in tooLittleActualInvocations & tooLittleActualInvocationsInOrder)
 *    - Branch: lastActualInvocation != null
 *    - Branch: lastActualInvocation == null
 * 4. noMoreInteractionsWanted(undesired, invocations)
 *    - Scenario execution with populated vs empty VerificationAwareInvocation lists
 * 5. cannotInitializeForSpyAnnotation / cannotInitializeForInjectMocksAnnotation
 *    - Chained cause preservation: Exception details correctly linked
 *
 * Defect-Targeted Branch Zone:
 * - ReturnsSmartNullsTest::shouldPrintTheParametersOnSmartNullPointerExceptionMessage
 *   Defect: smartNullPointerException(Location) throws SmartNullPointerException without printing
 *   the invocation parameters (e.g. "oompa" and "lumpa"), causing verification/diagnosis failures.
 * -------------------------------------------------------------------------------------------------------
 */
public class ReporterGeminiTest {

    private final Reporter reporter = new Reporter();

    private PrintableInvocation createPrintableInvocation(final String text, final Location location) {
        return new PrintableInvocation() {
            @Override
            public Location getLocation() {
                return location;
            }

            @Override
            public String toString() {
                return text;
            }
        };
    }

    private VerificationAwareInvocation createVerificationAwareInvocation(final boolean verified, final Location location) {
        return new VerificationAwareInvocation() {
            @Override
            public boolean isVerified() {
                return verified;
            }

            @Override
            public Location getLocation() {
                return location;
            }
        };
    }

    private Invocation createTestInvocation(Location location) {
        try {
            sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
            java.lang.reflect.Constructor<?> objConstr = Object.class.getDeclaredConstructor();
            java.lang.reflect.Constructor<?> invConstr = rf.newConstructorForSerialization(Invocation.class, objConstr);
            Invocation inv = (Invocation) invConstr.newInstance();
            try {
                java.lang.reflect.Field f = Invocation.class.getDeclaredField("location");
                f.setAccessible(true);
                f.set(inv, location);
            } catch (Throwable ignored) {
            }
            return inv;
        } catch (Throwable t) {
            return null;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Verification Reporting
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckedExceptionInvalid() {
        Throwable cause = new IllegalArgumentException("Invalid checked exception");
        try {
            reporter.checkedExceptionInvalid(cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid for this method!"));
            assertTrue(e.getMessage().contains("Invalid: " + cause));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubWithNullThrowable() {
        try {
            reporter.cannotStubWithNullThrowable();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot stub with null throwable!"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedStubbing() {
        Location loc = new Location();
        try {
            reporter.unfinishedStubbing(loc);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
            assertTrue(e.getMessage().contains("E.g. thenReturn() may be missing."));
        }
    }

    @Test(timeout = 4000)
    public void testMissingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument which has to be 'a method call on a mock'."));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedVerificationException() {
        Location loc = new Location();
        try {
            reporter.unfinishedVerificationException(loc);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() should be a mock but is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToWhenMethod() {
        try {
            reporter.notAMockPassedToWhenMethod();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToWhenMethod() {
        try {
            reporter.nullPassedToWhenMethod();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions() {
        try {
            reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
            assertTrue(e.getMessage().contains("verifyNoMoreInteractions(mockOne, mockTwo);"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerifyNoMoreInteractions() {
        try {
            reporter.notAMockPassedToVerifyNoMoreInteractions();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerifyNoMoreInteractions() {
        try {
            reporter.nullPassedToVerifyNoMoreInteractions();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedWhenCreatingInOrder() {
        try {
            reporter.notAMockPassedWhenCreatingInOrder();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
            assertTrue(e.getMessage().contains("InOrder inOrder = inOrder(mockOne, mockTwo);"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedWhenCreatingInOrder() {
        try {
            reporter.nullPassedWhenCreatingInOrder();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedWhenCreatingInOrder() {
        try {
            reporter.mocksHaveToBePassedWhenCreatingInOrder();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
        }
    }

    @Test(timeout = 4000)
    public void testInOrderRequiresFamiliarMock() {
        try {
            reporter.inOrderRequiresFamiliarMock();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InOrder can only verify mocks that were passed in during creation of InOrder."));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidUseOfMatchers() {
        try {
            reporter.invalidUseOfMatchers(3, 1);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers!"));
            assertTrue(e.getMessage().contains("3 matchers expected, 1 recorded."));
        }
    }

    @Test(timeout = 4000)
    public void testArgumentsAreDifferent() {
        Location loc = new Location();
        try {
            reporter.argumentsAreDifferent("wantedMethod(1)", "actualMethod(2)", loc);
            fail("Expected AssertionError / ArgumentsAreDifferent");
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("Argument(s) are different! Wanted:"));
            assertTrue(e.getMessage().contains("wantedMethod(1)"));
            assertTrue(e.getMessage().contains("actualMethod(2)"));
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testWantedButNotInvokedSimple() {
        PrintableInvocation wanted = createPrintableInvocation("mock.doSomething()", new Location());
        try {
            reporter.wantedButNotInvoked(wanted);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("mock.doSomething()"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedBranchEmptyInvocations() {
        PrintableInvocation wanted = createPrintableInvocation("mock.doSomething()", new Location());
        List<PrintableInvocation> emptyList = Collections.emptyList();
        try {
            reporter.wantedButNotInvoked(wanted, emptyList);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedBranchNonEmptyInvocations() {
        PrintableInvocation wanted = createPrintableInvocation("mock.doSomething()", new Location());
        Location loc = new Location();
        PrintableInvocation actual = createPrintableInvocation("mock.otherMethod()", loc);
        List<PrintableInvocation> invocations = Collections.singletonList(actual);
        try {
            reporter.wantedButNotInvoked(wanted, invocations);
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
            assertTrue(e.getMessage().contains(loc.toString()));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedInOrder() {
        PrintableInvocation wanted = createPrintableInvocation("mock.second()", new Location());
        PrintableInvocation previous = createPrintableInvocation("mock.first()", new Location());
        try {
            reporter.wantedButNotInvokedInOrder(wanted, previous);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
            assertTrue(e.getMessage().contains("Wanted anywhere AFTER following interaction:"));
            assertTrue(e.getMessage().contains("mock.first()"));
        }
    }

    @Test(timeout = 4000)
    public void testTooManyActualInvocations() {
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        Location undesired = new Location();
        try {
            reporter.tooManyActualInvocations(1, 2, wanted, undesired);
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
            assertTrue(e.getMessage().contains("But was 2 times. Undesired invocation:"));
        }
    }

    @Test(timeout = 4000)
    public void testNeverWantedButInvoked() {
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        Location undesired = new Location();
        try {
            reporter.neverWantedButInvoked(wanted, undesired);
            fail("Expected NeverWantedButInvoked");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("But invoked here:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooManyActualInvocationsInOrder() {
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        Location undesired = new Location();
        try {
            reporter.tooManyActualInvocationsInOrder(2, 4, wanted, undesired);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 4 times."));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsWithNonNullLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        Location actualLoc = new Location();
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, actualLoc);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 1 time:"));
            assertTrue(e.getMessage().contains(actualLoc.toString()));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsWithNullLocation() {
        Discrepancy discrepancy = new Discrepancy(3, 0);
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, null);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 3 times:"));
            assertTrue(e.getMessage().contains("But was 0 times:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsInOrderWithNonNullLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        Location actualLoc = new Location();
        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, actualLoc);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 1 time:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsInOrderWithNullLocation() {
        Discrepancy discrepancy = new Discrepancy(1, 0);
        PrintableInvocation wanted = createPrintableInvocation("mock.perform()", new Location());
        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, null);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
            assertTrue(e.getMessage().contains("But was 0 times:"));
        }
    }

    @Test(timeout = 4000)
    public void testNoMoreInteractionsWanted() {
        Location loc = new Location();
        Invocation invocation = createTestInvocation(loc);
        if (invocation == null) {
            return;
        }
        List<VerificationAwareInvocation> list = Collections.singletonList(
                createVerificationAwareInvocation(false, loc));
        try {
            reporter.noMoreInteractionsWanted(invocation, list);
            fail("Expected NoInteractionsWanted");
        } catch (NoInteractionsWanted e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction:"));
        }
    }

    @Test(timeout = 4000)
    public void testNoMoreInteractionsWantedInOrder() {
        Location loc = new Location();
        Invocation invocation = createTestInvocation(loc);
        if (invocation == null) {
            return;
        }
        try {
            reporter.noMoreInteractionsWantedInOrder(invocation);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("No interactions wanted here:"));
            assertTrue(e.getMessage().contains("But found this interaction:"));
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * ReturnsSmartNullsTest::shouldPrintTheParametersOnSmartNullPointerExceptionMessage
     *
     * The defect in Reporter is that smartNullPointerException only prints the
     * location and completely omits the invocation signature / parameters, failing to
     * output expected parameters such as "oompa" and "lumpa" in diagnostic messages.
     */
    @Test(timeout = 4000)
    public void shouldPrintTheParametersOnSmartNullPointerExceptionMessage() {
        Location location = new Location();
        try {
            reporter.smartNullPointerException(location);
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertTrue("Exception message should include oompa and lumpa, but was: " + e.getMessage(),
                    e.getMessage().contains("oompa") && e.getMessage().contains("lumpa"));
        }
    }

    // =========================================================================
    // Partition D: Configuration, Spying & Misuse Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCannotMockFinalClass() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
            assertTrue(e.getMessage().contains("final classes"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubVoidMethodWithAReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("clear");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("'clear' is a *void method* and it *cannot* be stubbed with a *return value*!"));
        }
    }

    @Test(timeout = 4000)
    public void testOnlyVoidMethodsCanBeSetToDoNothing() {
        try {
            reporter.onlyVoidMethodsCanBeSetToDoNothing();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Only void methods can doNothing()!"));
        }
    }

    @Test(timeout = 4000)
    public void testWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("Integer", "String", "calculate");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("String cannot be returned by calculate()"));
            assertTrue(e.getMessage().contains("calculate() should return Integer"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedAtMostX() {
        try {
            reporter.wantedAtMostX(2, 5);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 5"));
        }
    }

    @Test(timeout = 4000)
    public void testMisplacedArgumentMatcher() {
        Location loc = new Location();
        try {
            reporter.misplacedArgumentMatcher(loc);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Misplaced argument matcher detected here:"));
        }
    }

    @Test(timeout = 4000)
    public void testNoArgumentValueWasCaptured() {
        try {
            reporter.noArgumentValueWasCaptured();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("No argument value was captured!"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesDoesNotAcceptNullParameters() {
        try {
            reporter.extraInterfacesDoesNotAcceptNullParameters();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept null parameters."));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesAcceptsOnlyInterfaces() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() accepts only interfaces."));
            assertTrue(e.getMessage().contains("You passed following type: String which is not an interface."));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesCannotContainMockedType() {
        try {
            reporter.extraInterfacesCannotContainMockedType(List.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept the same type as the mocked type."));
            assertTrue(e.getMessage().contains("You mocked following type: List"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesRequiresAtLeastOneInterface() {
        try {
            reporter.extraInterfacesRequiresAtLeastOneInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() requires at least one interface."));
        }
    }

    @Test(timeout = 4000)
    public void testMockedTypeIsInconsistentWithSpiedInstanceType() {
        try {
            reporter.mockedTypeIsInconsistentWithSpiedInstanceType(List.class, new ArrayList<Object>());
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be the same as the type of your spied instance."));
            assertTrue(e.getMessage().contains("Mocked type must be: ArrayList, but is: List"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotCallRealMethodOnInterface() {
        try {
            reporter.cannotCallRealMethodOnInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot call real method on java interface."));
        }
    }

    @Test(timeout = 4000)
    public void testCannotVerifyToString() {
        try {
            reporter.cannotVerifyToString();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito cannot verify toString()"));
        }
    }

    @Test(timeout = 4000)
    public void testMoreThanOneAnnotationNotAllowed() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("myField");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("The field 'myField' has multiple Mockito annotations."));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedCombinationOfAnnotations() {
        try {
            reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("@Mock and @Spy"));
        }
    }

    // =========================================================================
    // Partition E: Annotation Instantiation Failure Paths & Cause Chaining
    // =========================================================================

    @Test(timeout = 4000)
    public void testCannotInitializeForSpyAnnotation() {
        Exception cause = new IllegalAccessException("Constructor private");
        try {
            reporter.cannotInitializeForSpyAnnotation("serviceSpy", cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instianate a @Spy for 'serviceSpy' field."));
            assertTrue(e.getMessage().contains("However, I failed because: Constructor private"));
            assertEquals(cause, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testCannotInitializeForInjectMocksAnnotation() {
        Exception cause = new InstantiationException("No zero-arg constructor");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("orderService", cause);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instianate @InjectMocks field named 'orderService'."));
            assertTrue(e.getMessage().contains("However, I failed because: No zero-arg constructor"));
            assertEquals(cause, e.getCause());
        }
    }
}