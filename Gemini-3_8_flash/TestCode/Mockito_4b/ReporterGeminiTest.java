package org.mockito.exceptions;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.*;
import org.mockito.exceptions.verification.*;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.matchers.LocalizedMatcher;
import org.mockito.internal.reporting.Discrepancy;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.Location;
import org.mockito.listeners.InvocationListener;
import org.mockito.mock.SerializableMode;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.exceptions.Reporter
 *
 * Defects4J Known Fault Targets:
 * 1. cannotInjectDependency: when details.getCause() == null, defective code calls
 *    details.getCause().getMessage(), throwing NullPointerException instead of MockitoException.
 * 2. noMoreInteractionsWanted: when undesired.getMock().toString() fails (e.g. bogus answer throwing ClassCastException),
 *    string concatenation causes ClassCastException instead of throwing NoInteractionsWanted.
 * 3. noMoreInteractionsWantedInOrder: when undesired.getMock().toString() throws ClassCastException,
 *    string concatenation fails instead of throwing VerificationInOrderFailure.
 *
 * Equivalence Partitions & Boundary Conditions Covered:
 * - Partition A: Misuse exceptions (checkedExceptionInvalid, cannotStubWithNullThrowable, unfinishedStubbing,
 *                missingMethodInvocation, nullPassedToVerify, notAMockPassedToVerify, etc.)
 * - Partition B: Verification & Matching failures (argumentsAreDifferent, wantedButNotInvoked empty vs non-empty,
 *                wantedButNotInvokedInOrder, tooManyActualInvocations, tooLittleActualInvocations null vs non-null location,
 *                neverWantedButInvoked, wantedAtMostX, smartNullPointerException).
 * - Partition C: Argument matcher validation (invalidUseOfMatchers, incorrectUseOfAdditionalMatchers,
 *                misplacedArgumentMatcher, reportNoSubMatchersFound, locationsOf).
 * - Partition D: Annotation and Mocking lifecycle guards (moreThanOneAnnotationNotAllowed, cannotInitializeForSpyAnnotation,
 *                cannotInitializeForInjectMocksAnnotation, fieldInitialisationThrewException, cannotMockFinalClass,
 *                cannotStubVoidMethodWithAReturnValue, wrongTypeOfReturnValue, spyAndDelegateAreMutuallyExclusive).
 * - Partition E: Argument range & IdentityAnswer validation (possibleArgumentTypesOf zero-arg, multi-arg, varargs;
 *                invalidArgumentPositionRangeAtInvocationTime, wrongTypeOfArgumentToReturn).
 * - Partition F: Delegated instances & Extra interfaces (delegatedMethodHasWrongReturnType, delegatedMethodDoesNotExistOnDelegate,
 *                extraInterfaces constraints, usingConstructorWithFancySerializable).
 */
public class ReporterGeminiTest {

    private Reporter reporter;

    @Before
    public void setUp() {
        reporter = new Reporter();
    }

    // Helper: Dynamic Proxy to create Invocation instances without external mock framework
    @SuppressWarnings("unchecked")
    private <T> T createProxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        );
    }

    private DescribedInvocation createDescribedInvocation(final String description, final Location location) {
        return new DescribedInvocation() {
            @Override
            public String toString() {
                return description;
            }

            @Override
            public Location getLocation() {
                return location;
            }
        };
    }

    private Invocation createMockInvocation(final Object mock, final Method method, final Location location) {
        return createProxy(Invocation.class, (proxy, invokedMethod, args) -> {
            String name = invokedMethod.getName();
            if ("getMock".equals(name)) return mock;
            if ("getMethod".equals(name)) return method;
            if ("getLocation".equals(name)) return location;
            if ("toString".equals(name)) return "mock." + (method != null ? method.getName() : "method") + "()";
            if ("getArguments".equals(name)) return new Object[0];
            if ("isVerified".equals(name)) return false;
            return null;
        });
    }

    // Dummy sample class for Method and Field reflection
    private static class DummyClass {
        public String sampleField;

        public void voidMethod() {}
        public String noArgMethod() { return ""; }
        public void multiArgMethod(String str, int num) {}
        public void varargMethod(String prefix, Object... items) {}
    }

    // =========================================================================
    // Partition C: Defect-Targeted Ground Truth Tests (Defects4J Reproduction)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCannotInjectDependency_WhenCauseIsNull_ShouldThrowMockitoExceptionNotNPE() throws Exception {
        Field field = DummyClass.class.getField("sampleField");
        Exception detailsWithoutCause = new Exception("Instantiation failure without cause");

        try {
            reporter.cannotInjectDependency(field, "dummyMock", detailsWithoutCause);
            fail("Expected MockitoException to be thrown");
        } catch (MockitoException e) {
            assertTrue("Message should mention field name", e.getMessage().contains("sampleField"));
        } catch (NullPointerException npe) {
            fail("Target defect detected: NullPointerException thrown instead of MockitoException when exception cause is null");
        }
    }

    @Test(timeout = 4000)
    public void testNoMoreInteractionsWanted_WhenMockThrowsBogusException_ShouldHandleSafely() {
        Object bogusMock = new Object() {
            @Override
            public String toString() {
                throw new ClassCastException("Bogus answer threw ClassCastException on toString");
            }
        };

        Invocation invocation = createMockInvocation(bogusMock, null, new LocationImpl());
        List<VerificationAwareInvocation> invocations = Collections.emptyList();

        try {
            reporter.noMoreInteractionsWanted(invocation, invocations);
            fail("Expected NoInteractionsWanted exception");
        } catch (NoInteractionsWanted expected) {
            assertNotNull(expected.getMessage());
        } catch (ClassCastException cce) {
            fail("Target defect detected: ClassCastException propagated because mock.toString() was directly invoked");
        }
    }

    @Test(timeout = 4000)
    public void testNoMoreInteractionsWantedInOrder_WhenMockThrowsBogusException_ShouldHandleSafely() {
        Object bogusMock = new Object() {
            @Override
            public String toString() {
                throw new ClassCastException("Bogus answer threw ClassCastException on toString");
            }
        };

        Invocation invocation = createMockInvocation(bogusMock, null, new LocationImpl());

        try {
            reporter.noMoreInteractionsWantedInOrder(invocation);
            fail("Expected VerificationInOrderFailure exception");
        } catch (VerificationInOrderFailure expected) {
            assertNotNull(expected.getMessage());
        } catch (ClassCastException cce) {
            fail("Target defect detected: ClassCastException propagated in noMoreInteractionsWantedInOrder");
        }
    }

    // =========================================================================
    // Partition A: Stubbing & Misuse Exceptions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckedExceptionInvalid() {
        try {
            reporter.checkedExceptionInvalid(new Exception("Checked"));
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubWithNullThrowable() {
        try {
            reporter.cannotStubWithNullThrowable();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot stub with null throwable!"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedStubbing() {
        try {
            reporter.unfinishedStubbing(new LocationImpl());
            fail();
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected here:"));
        }
    }

    @Test(timeout = 4000)
    public void testIncorrectUseOfApi() {
        try {
            reporter.incorrectUseOfApi();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Incorrect use of API detected here:"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail();
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedVerificationException() {
        try {
            reporter.unfinishedVerificationException(new LocationImpl());
            fail();
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock) here:"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail();
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is of type String and is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail();
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() should be a mock but is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToWhenMethod() {
        try {
            reporter.notAMockPassedToWhenMethod();
            fail();
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToWhenMethod() {
        try {
            reporter.nullPassedToWhenMethod();
            fail();
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument passed to when() is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions() {
        try {
            reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerifyNoMoreInteractions() {
        try {
            reporter.notAMockPassedToVerifyNoMoreInteractions();
            fail();
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerifyNoMoreInteractions() {
        try {
            reporter.nullPassedToVerifyNoMoreInteractions();
            fail();
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedWhenCreatingInOrder() {
        try {
            reporter.notAMockPassedWhenCreatingInOrder();
            fail();
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is not a mock!"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedWhenCreatingInOrder() {
        try {
            reporter.nullPassedWhenCreatingInOrder();
            fail();
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("Argument(s) passed is null!"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedWhenCreatingInOrder() {
        try {
            reporter.mocksHaveToBePassedWhenCreatingInOrder();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)!"));
        }
    }

    @Test(timeout = 4000)
    public void testInOrderRequiresFamiliarMock() {
        try {
            reporter.inOrderRequiresFamiliarMock();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InOrder can only verify mocks that were passed in during creation"));
        }
    }

    @Test(timeout = 4000)
    public void testStubPassedToVerify() {
        try {
            reporter.stubPassedToVerify();
            fail();
        } catch (CannotVerifyStubOnlyMock e) {
            assertTrue(e.getMessage().contains("Argument passed to verify() is a stubOnly() mock"));
        }
    }

    // =========================================================================
    // Partition B: Verification Invocations & Discrepancies
    // =========================================================================

    @Test(timeout = 4000)
    public void testArgumentsAreDifferent() {
        try {
            reporter.argumentsAreDifferent("wantedMethod()", "actualMethod()", new LocationImpl());
            fail();
        } catch (AssertionError e) {
            assertTrue(e.getMessage().contains("Argument(s) are different!"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedSimple() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()", new LocationImpl());
        try {
            reporter.wantedButNotInvoked(wanted);
            fail();
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked:"));
            assertTrue(e.getMessage().contains("foo.bar()"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedWithEmptyInvocations() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()", new LocationImpl());
        try {
            reporter.wantedButNotInvoked(wanted, Collections.emptyList());
            fail();
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Actually, there were zero interactions with this mock."));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedWithOtherInvocations() {
        DescribedInvocation wanted = createDescribedInvocation("foo.bar()", new LocationImpl());
        DescribedInvocation other = createDescribedInvocation("foo.baz()", new LocationImpl());

        try {
            reporter.wantedButNotInvoked(wanted, Arrays.asList(other));
            fail();
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("However, there were other interactions with this mock:"));
            assertTrue(e.getMessage().contains("foo.baz()"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedInOrder() {
        DescribedInvocation wanted = createDescribedInvocation("mock.wanted()", new LocationImpl());
        DescribedInvocation previous = createDescribedInvocation("mock.previous()", new LocationImpl());

        try {
            reporter.wantedButNotInvokedInOrder(wanted, previous);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Wanted anywhere AFTER following interaction:"));
            assertTrue(e.getMessage().contains("mock.previous()"));
        }
    }

    @Test(timeout = 4000)
    public void testTooManyActualInvocations() {
        DescribedInvocation wanted = createDescribedInvocation("mock.perform()", new LocationImpl());
        Location firstUndesired = new LocationImpl();

        try {
            reporter.tooManyActualInvocations(2, 3, wanted, firstUndesired);
            fail();
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 3 times. Undesired invocation:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooManyActualInvocationsInOrder() {
        DescribedInvocation wanted = createDescribedInvocation("mock.perform()", new LocationImpl());
        Location firstUndesired = new LocationImpl();

        try {
            reporter.tooManyActualInvocationsInOrder(1, 2, wanted, firstUndesired);
            fail();
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("Wanted 1 time:"));
            assertTrue(e.getMessage().contains("But was 2 times."));
        }
    }

    @Test(timeout = 4000)
    public void testNeverWantedButInvoked() {
        DescribedInvocation wanted = createDescribedInvocation("mock.never()", new LocationImpl());
        Location firstUndesired = new LocationImpl();

        try {
            reporter.neverWantedButInvoked(wanted, firstUndesired);
            fail();
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here:"));
            assertTrue(e.getMessage().contains("But invoked here:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocations_WithLocation() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        DescribedInvocation wanted = createDescribedInvocation("mock.little()", new LocationImpl());
        Location lastActual = new LocationImpl();

        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, lastActual);
            fail();
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
            assertTrue(e.getMessage().contains("But was 1 time:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocations_WithNullLocation() {
        Discrepancy discrepancy = new Discrepancy(3, 0);
        DescribedInvocation wanted = createDescribedInvocation("mock.little()", new LocationImpl());

        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, null);
            fail();
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted 3 times:"));
            assertTrue(e.getMessage().contains("But was 0 times:"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsInOrder() {
        Discrepancy discrepancy = new Discrepancy(2, 1);
        DescribedInvocation wanted = createDescribedInvocation("mock.little()", new LocationImpl());

        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, new LocationImpl());
            fail();
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure:"));
            assertTrue(e.getMessage().contains("Wanted 2 times:"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedAtMostX() {
        try {
            reporter.wantedAtMostX(2, 4);
            fail();
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most 2 times but was 4"));
        }
    }

    @Test(timeout = 4000)
    public void testSmartNullPointerException() {
        try {
            reporter.smartNullPointerException("mock.call()", new LocationImpl());
            fail();
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("You have a NullPointerException here:"));
            assertTrue(e.getMessage().contains("mock.call()"));
        }
    }

    @Test(timeout = 4000)
    public void testNoArgumentValueWasCaptured() {
        try {
            reporter.noArgumentValueWasCaptured();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("No argument value was captured!"));
        }
    }

    // =========================================================================
    // Partition C: Matchers & Additional Matchers
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvalidUseOfMatchers() {
        LocalizedMatcher m = new LocalizedMatcher(null);
        try {
            reporter.invalidUseOfMatchers(2, Arrays.asList(m));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers!"));
            assertTrue(e.getMessage().contains("2 matchers expected, 1 recorded:"));
        }
    }

    @Test(timeout = 4000)
    public void testIncorrectUseOfAdditionalMatchers() {
        LocalizedMatcher m = new LocalizedMatcher(null);
        try {
            reporter.incorrectUseOfAdditionalMatchers("and", 2, Arrays.asList(m));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers inside additional matcher and !"));
            assertTrue(e.getMessage().contains("2 sub matchers expected, 1 recorded:"));
        }
    }

    @Test(timeout = 4000)
    public void testReportNoSubMatchersFound() {
        try {
            reporter.reportNoSubMatchersFound("or");
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("No matchers found for additional matcher or"));
        }
    }

    @Test(timeout = 4000)
    public void testMisplacedArgumentMatcher() {
        LocalizedMatcher m = new LocalizedMatcher(null);
        try {
            reporter.misplacedArgumentMatcher(Arrays.asList(m));
            fail();
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Misplaced argument matcher detected here:"));
        }
    }

    // =========================================================================
    // Partition D: Mock Settings, Final Classes & Method Signatures
    // =========================================================================

    @Test(timeout = 4000)
    public void testCannotMockFinalClass() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy class java.lang.String"));
            assertTrue(e.getMessage().contains("- final classes"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubVoidMethodWithAReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("clear");
            fail();
        } catch (CannotStubVoidMethodWithReturnValue e) {
            assertTrue(e.getMessage().contains("'clear' is a *void method* and it *cannot* be stubbed with a *return value*!"));
        }
    }

    @Test(timeout = 4000)
    public void testOnlyVoidMethodsCanBeSetToDoNothing() {
        try {
            reporter.onlyVoidMethodsCanBeSetToDoNothing();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Only void methods can doNothing()!"));
        }
    }

    @Test(timeout = 4000)
    public void testWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("Integer", "String", "getCount");
            fail();
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("String cannot be returned by getCount()"));
            assertTrue(e.getMessage().contains("getCount() should return Integer"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotCallAbstractRealMethod() {
        try {
            reporter.cannotCallAbstractRealMethod();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot call abstract real method on java object!"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotVerifyToString() {
        try {
            reporter.cannotVerifyToString();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito cannot verify toString()"));
        }
    }

    @Test(timeout = 4000)
    public void testAtMostAndNeverShouldNotBeUsedWithTimeout() {
        try {
            reporter.atMostAndNeverShouldNotBeUsedWithTimeout();
            fail();
        } catch (FriendlyReminderException e) {
            assertTrue(e.getMessage().contains("Don't panic! I'm just a friendly reminder!"));
        }
    }

    @Test(timeout = 4000)
    public void testDefaultAnswerDoesNotAcceptNullParameter() {
        try {
            reporter.defaultAnswerDoesNotAcceptNullParameter();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("defaultAnswer() does not accept null parameter"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializableWontWorkForObjectsThatDontImplementSerializable() {
        try {
            reporter.serializableWontWorkForObjectsThatDontImplementSerializable(DummyClass.class);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("DummyClass"));
            assertTrue(e.getMessage().contains("do not implement Serializable AND do not have a no-arg constructor."));
        }
    }

    @Test(timeout = 4000)
    public void testUsingConstructorWithFancySerializable() {
        try {
            reporter.usingConstructorWithFancySerializable(SerializableMode.ACROSS_JVM);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocks instantiated with constructor cannot be combined with ACROSS_JVM serialization mode."));
        }
    }

    // =========================================================================
    // Partition E: Extra Interfaces, Spying & Delegation
    // =========================================================================

    @Test(timeout = 4000)
    public void testExtraInterfacesDoesNotAcceptNullParameters() {
        try {
            reporter.extraInterfacesDoesNotAcceptNullParameters();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept null parameters."));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesAcceptsOnlyInterfaces() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() accepts only interfaces."));
            assertTrue(e.getMessage().contains("You passed following type: String which is not an interface."));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesCannotContainMockedType() {
        try {
            reporter.extraInterfacesCannotContainMockedType(List.class);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() does not accept the same type as the mocked type."));
            assertTrue(e.getMessage().contains("List"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesRequiresAtLeastOneInterface() {
        try {
            reporter.extraInterfacesRequiresAtLeastOneInterface();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("extraInterfaces() requires at least one interface."));
        }
    }

    @Test(timeout = 4000)
    public void testMockedTypeIsInconsistentWithSpiedInstanceType() {
        try {
            reporter.mockedTypeIsInconsistentWithSpiedInstanceType(List.class, new ArrayList<>());
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be the same as the type of your spied instance."));
            assertTrue(e.getMessage().contains("Mocked type must be: ArrayList, but is: List"));
        }
    }

    @Test(timeout = 4000)
    public void testMockedTypeIsInconsistentWithDelegatedInstanceType() {
        try {
            reporter.mockedTypeIsInconsistentWithDelegatedInstanceType(List.class, new ArrayList<>());
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mocked type must be the same as the type of your delegated instance."));
            assertTrue(e.getMessage().contains("Mocked type must be: ArrayList, but is: List"));
        }
    }

    @Test(timeout = 4000)
    public void testSpyAndDelegateAreMutuallyExclusive() {
        try {
            reporter.spyAndDelegateAreMutuallyExclusive();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Settings should not define a spy instance and a delegated instance at the same time."));
        }
    }

    @Test(timeout = 4000)
    public void testDelegatedMethodHasWrongReturnType() throws Exception {
        Method m1 = DummyClass.class.getMethod("voidMethod");
        Method m2 = DummyClass.class.getMethod("noArgMethod");

        try {
            reporter.delegatedMethodHasWrongReturnType(m1, m2, "mockInstance", new DummyClass());
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Methods called on delegated instance must have compatible return types"));
            assertTrue(e.getMessage().contains("return type should be: void, but was: String"));
        }
    }

    @Test(timeout = 4000)
    public void testDelegatedMethodDoesNotExistOnDelegate() throws Exception {
        Method m = DummyClass.class.getMethod("voidMethod");

        try {
            reporter.delegatedMethodDoesNotExistOnDelegate(m, "mockInstance", new DummyClass());
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Methods called on mock must exist in delegated instance."));
            assertTrue(e.getMessage().contains("no such method was found."));
        }
    }

    // =========================================================================
    // Partition F: Annotations & Injection
    // =========================================================================

    @Test(timeout = 4000)
    public void testMoreThanOneAnnotationNotAllowed() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("sampleField");
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("You cannot have more than one Mockito annotation on a field!"));
            assertTrue(e.getMessage().contains("sampleField"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedCombinationOfAnnotations() {
        try {
            reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("This combination of annotations is not permitted on a single field:"));
            assertTrue(e.getMessage().contains("@Mock and @Spy"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotInitializeForSpyAnnotation() {
        Exception cause = new RuntimeException("Constructor failed");
        try {
            reporter.cannotInitializeForSpyAnnotation("mySpy", cause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate a @Spy for 'mySpy' field."));
            assertSame(cause, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testCannotInitializeForInjectMocksAnnotation() {
        Exception cause = new RuntimeException("Cannot resolve dependencies");
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("myService", cause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'myService'."));
            assertSame(cause, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testFieldInitialisationThrewException() throws Exception {
        Field field = DummyClass.class.getField("sampleField");
        Throwable cause = new IllegalStateException("Init error");

        try {
            reporter.fieldInitialisationThrewException(field, cause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field named 'sampleField'"));
            assertSame(cause, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testCannotInjectDependency_WithCause() throws Exception {
        Field field = DummyClass.class.getField("sampleField");
        Exception cause = new Exception("Root failure");
        Exception detailsWithCause = new Exception("Wrapper failure", cause);

        try {
            reporter.cannotInjectDependency(field, "dummyMock", detailsWithCause);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Mockito couldn't inject mock dependency"));
            assertTrue(e.getMessage().contains("Root failure"));
        }
    }

    // =========================================================================
    // Partition G: Listeners & Argument Positions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInvocationListenerDoesNotAcceptNullParameters() {
        try {
            reporter.invocationListenerDoesNotAcceptNullParameters();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("invocationListeners() does not accept null parameters"));
        }
    }

    @Test(timeout = 4000)
    public void testInvocationListenersRequiresAtLeastOneListener() {
        try {
            reporter.invocationListenersRequiresAtLeastOneListener();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("invocationListeners() requires at least one listener"));
        }
    }

    @Test(timeout = 4000)
    public void testInvocationListenerThrewException() {
        InvocationListener listener = (methodInvocationReport) -> {};
        Throwable t = new RuntimeException("Listener exploded");

        try {
            reporter.invocationListenerThrewException(listener, t);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("threw an exception : java.lang.RuntimeExceptionListener exploded"));
            assertSame(t, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testInvalidArgumentRangeAtIdentityAnswerCreationTime() {
        try {
            reporter.invalidArgumentRangeAtIdentityAnswerCreationTime();
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Invalid argument index."));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidArgumentPositionRangeAtInvocationTime_NoArgs() throws Exception {
        Method method = DummyClass.class.getMethod("noArgMethod");
        InvocationOnMock invocation = createProxy(InvocationOnMock.class, (proxy, m, args) -> {
            if ("getMock".equals(m.getName())) return "mockInstance";
            if ("getMethod".equals(m.getName())) return method;
            return null;
        });

        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, false, 0);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Wanted parameter at position 0 but the method has no arguments."));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidArgumentPositionRangeAtInvocationTime_MultiArgsAndLastParam() throws Exception {
        Method method = DummyClass.class.getMethod("multiArgMethod", String.class, int.class);
        InvocationOnMock invocation = createProxy(InvocationOnMock.class, (proxy, m, args) -> {
            if ("getMock".equals(m.getName())) return "mockInstance";
            if ("getMethod".equals(m.getName())) return method;
            return null;
        });

        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, true, 2);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Last parameter wanted but the possible argument indexes for this method are :"));
            assertTrue(e.getMessage().contains("[0] String"));
            assertTrue(e.getMessage().contains("[1] int"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidArgumentPositionRangeAtInvocationTime_Vararg() throws Exception {
        Method method = DummyClass.class.getMethod("varargMethod", String.class, Object[].class);
        InvocationOnMock invocation = createProxy(InvocationOnMock.class, (proxy, m, args) -> {
            if ("getMock".equals(m.getName())) return "mockInstance";
            if ("getMethod".equals(m.getName())) return method;
            return null;
        });

        try {
            reporter.invalidArgumentPositionRangeAtInvocationTime(invocation, false, 5);
            fail();
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("[1+] Object  <- Vararg"));
        }
    }

    @Test(timeout = 4000)
    public void testWrongTypeOfArgumentToReturn() throws Exception {
        Method method = DummyClass.class.getMethod("multiArgMethod", String.class, int.class);
        InvocationOnMock invocation = createProxy(InvocationOnMock.class, (proxy, m, args) -> {
            if ("getMock".equals(m.getName())) return "mockInstance";
            if ("getMethod".equals(m.getName())) return method;
            return null;
        });

        try {
            reporter.wrongTypeOfArgumentToReturn(invocation, "Integer", String.class, 0);
            fail();
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("The argument of type 'String' cannot be returned because the following"));
            assertTrue(e.getMessage().contains("method should return the type 'Integer'"));
            assertTrue(e.getMessage().contains("Position of the wanted argument is 0"));
        }
    }
}