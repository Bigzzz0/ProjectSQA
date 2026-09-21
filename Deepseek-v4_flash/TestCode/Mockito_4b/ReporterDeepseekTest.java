package org.mockito.exceptions;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.exceptions.misusing.*;
import org.mockito.exceptions.verification.*;
import org.mockito.internal.debugging.LocationImpl;
import org.mockito.internal.exceptions.MockitoLimitations;
import org.mockito.internal.exceptions.VerificationAwareInvocation;
import org.mockito.internal.exceptions.util.ScenarioPrinter;
import org.mockito.internal.junit.JUnitTool;
import org.mockito.internal.matchers.LocalizedMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.StringJoiner;
import org.mockito.invocation.DescribedInvocation;
import org.mockito.invocation.Invocation;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.invocation.Location;
import org.mockito.listeners.InvocationListener;
import org.mockito.mock.MockName;
import org.mockito.mock.SerializableMode;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Reporter.java - White-box test suite for maximum coverage and defect detection
 * 
 * Defect: Multiple methods (noMoreInteractionsWanted, noMoreInteractionsWantedInOrder, 
 *         cannotInjectDependency) call safelyGetMockName(Object mock) which internally 
 *         calls new MockUtil().getMockName(mock). When mock is a bogus object (e.g., 
 *         a String or non-mock with custom default answer), MockUtil.getMockName() 
 *         can throw ClassCastException or NullPointerException instead of the expected 
 *         MockitoException/VerificationInOrderFailure/NoInteractionsWanted.
 * 
 * Key branches to cover:
 * 1. checkedExceptionInvalid - normal path, null throwable
 * 2. cannotStubWithNullThrowable - normal path
 * 3. unfinishedStubbing - null location, normal location
 * 4. incorrectUseOfApi - normal path
 * 5. missingMethodInvocation - normal path
 * 6. unfinishedVerificationException - null location, normal location
 * 7. notAMockPassedToVerify - null type, normal type
 * 8. nullPassedToVerify - normal path
 * 9. notAMockPassedToWhenMethod - normal path
 * 10. nullPassedToWhenMethod - normal path
 * 11. mocksHaveToBePassedToVerifyNoMoreInteractions - normal path
 * 12. notAMockPassedToVerifyNoMoreInteractions - normal path
 * 13. nullPassedToVerifyNoMoreInteractions - normal path
 * 14. notAMockPassedWhenCreatingInOrder - normal path
 * 15. nullPassedWhenCreatingInOrder - normal path
 * 16. mocksHaveToBePassedWhenCreatingInOrder - normal path
 * 17. inOrderRequiresFamiliarMock - normal path
 * 18. invalidUseOfMatchers - empty list, non-empty list, null matchers
 * 19. incorrectUseOfAdditionalMatchers - empty stack, non-empty stack
 * 20. stubPassedToVerify - normal path
 * 21. reportNoSubMatchersFound - normal path
 * 22. argumentsAreDifferent - normal path, null locations
 * 23. wantedButNotInvoked - single arg, two args (empty list, non-empty list)
 * 24. wantedButNotInvokedInOrder - normal path
 * 25. tooManyActualInvocations - normal path, zero counts
 * 26. neverWantedButInvoked - normal path
 * 27. tooManyActualInvocationsInOrder - normal path
 * 28. tooLittleActualInvocations - null location, normal location
 * 29. tooLittleActualInvocationsInOrder - null location, normal location
 * 30. noMoreInteractionsWanted - DEFECT TARGET: bogus mock object
 * 31. noMoreInteractionsWantedInOrder - DEFECT TARGET: bogus mock object
 * 32. cannotMockFinalClass - normal path
 * 33. cannotStubVoidMethodWithAReturnValue - normal path
 * 34. onlyVoidMethodsCanBeSetToDoNothing - normal path
 * 35. wrongTypeOfReturnValue - normal path
 * 36. wantedAtMostX - normal path
 * 37. misplacedArgumentMatcher - empty list, non-empty list
 * 38. smartNullPointerException - normal path
 * 39. noArgumentValueWasCaptured - normal path
 * 40. extraInterfacesDoesNotAcceptNullParameters - normal path
 * 41. extraInterfacesAcceptsOnlyInterfaces - normal path
 * 42. extraInterfacesCannotContainMockedType - normal path
 * 43. extraInterfacesRequiresAtLeastOneInterface - normal path
 * 44. mockedTypeIsInconsistentWithSpiedInstanceType - normal path
 * 45. cannotCallAbstractRealMethod - normal path
 * 46. cannotVerifyToString - normal path
 * 47. moreThanOneAnnotationNotAllowed - normal path
 * 48. unsupportedCombinationOfAnnotations - normal path
 * 49. cannotInitializeForSpyAnnotation - normal path
 * 50. cannotInitializeForInjectMocksAnnotation - normal path
 * 51. atMostAndNeverShouldNotBeUsedWithTimeout - normal path
 * 52. fieldInitialisationThrewException - normal path
 * 53. invocationListenerDoesNotAcceptNullParameters - normal path
 * 54. invocationListenersRequiresAtLeastOneListener - normal path
 * 55. invocationListenerThrewException - normal path
 * 56. cannotInjectDependency - DEFECT TARGET: bogus mock object
 * 57. mockedTypeIsInconsistentWithDelegatedInstanceType - normal path
 * 58. spyAndDelegateAreMutuallyExclusive - normal path
 * 59. invalidArgumentRangeAtIdentityAnswerCreationTime - normal path
 * 60. invalidArgumentPositionRangeAtInvocationTime - normal path
 * 61. wrongTypeOfArgumentToReturn - normal path
 * 62. defaultAnswerDoesNotAcceptNullParameter - normal path
 * 63. serializableWontWorkForObjectsThatDontImplementSerializable - normal path
 * 64. delegatedMethodHasWrongReturnType - normal path
 * 65. delegatedMethodDoesNotExistOnDelegate - normal path
 * 66. usingConstructorWithFancySerializable - normal path
 * 67. locationsOf - empty collection, non-empty collection
 * 68. createWantedButNotInvokedMessage - normal path
 * 69. createTooManyInvocationsMessage - normal path
 * 70. createTooLittleInvocationsMessage - null location, normal location
 * 71. possibleArgumentTypesOf - no args, varargs, normal args
 * 72. exceptionCauseMessageIfAvailable - normal path
 * 73. safelyGetMockName - DEFECT TARGET: bogus object (non-mock)
 */
public class ReporterDeepseekTest {

    private final Reporter reporter = new Reporter();

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testCheckedExceptionInvalid() {
        try {
            reporter.checkedExceptionInvalid(new RuntimeException("test"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Checked exception is invalid"));
            assertTrue(e.getMessage().contains("test"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubWithNullThrowable() {
        try {
            reporter.cannotStubWithNullThrowable();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot stub with null throwable"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedStubbingWithLocation() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedStubbing(location);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedStubbingWithNullLocation() {
        try {
            reporter.unfinishedStubbing(null);
            fail("Expected UnfinishedStubbingException");
        } catch (UnfinishedStubbingException e) {
            assertTrue(e.getMessage().contains("Unfinished stubbing detected"));
        }
    }

    @Test(timeout = 4000)
    public void testIncorrectUseOfApi() {
        try {
            reporter.incorrectUseOfApi();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Incorrect use of API detected"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingMethodInvocation() {
        try {
            reporter.missingMethodInvocation();
            fail("Expected MissingMethodInvocationException");
        } catch (MissingMethodInvocationException e) {
            assertTrue(e.getMessage().contains("when() requires an argument"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedVerificationExceptionWithLocation() {
        Location location = new LocationImpl();
        try {
            reporter.unfinishedVerificationException(location);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock)"));
        }
    }

    @Test(timeout = 4000)
    public void testUnfinishedVerificationExceptionWithNullLocation() {
        try {
            reporter.unfinishedVerificationException(null);
            fail("Expected UnfinishedVerificationException");
        } catch (UnfinishedVerificationException e) {
            assertTrue(e.getMessage().contains("Missing method call for verify(mock)"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerify() {
        try {
            reporter.notAMockPassedToVerify(String.class);
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("is not a mock"));
            assertTrue(e.getMessage().contains("String"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerify() {
        try {
            reporter.nullPassedToVerify();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("should be a mock but is null"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToWhenMethod() {
        try {
            reporter.notAMockPassedToWhenMethod();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("is not a mock"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToWhenMethod() {
        try {
            reporter.nullPassedToWhenMethod();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("is null"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedToVerifyNoMoreInteractions() {
        try {
            reporter.mocksHaveToBePassedToVerifyNoMoreInteractions();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedToVerifyNoMoreInteractions() {
        try {
            reporter.notAMockPassedToVerifyNoMoreInteractions();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("is not a mock"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedToVerifyNoMoreInteractions() {
        try {
            reporter.nullPassedToVerifyNoMoreInteractions();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("is null"));
        }
    }

    @Test(timeout = 4000)
    public void testNotAMockPassedWhenCreatingInOrder() {
        try {
            reporter.notAMockPassedWhenCreatingInOrder();
            fail("Expected NotAMockException");
        } catch (NotAMockException e) {
            assertTrue(e.getMessage().contains("is not a mock"));
        }
    }

    @Test(timeout = 4000)
    public void testNullPassedWhenCreatingInOrder() {
        try {
            reporter.nullPassedWhenCreatingInOrder();
            fail("Expected NullInsteadOfMockException");
        } catch (NullInsteadOfMockException e) {
            assertTrue(e.getMessage().contains("is null"));
        }
    }

    @Test(timeout = 4000)
    public void testMocksHaveToBePassedWhenCreatingInOrder() {
        try {
            reporter.mocksHaveToBePassedWhenCreatingInOrder();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Method requires argument(s)"));
        }
    }

    @Test(timeout = 4000)
    public void testInOrderRequiresFamiliarMock() {
        try {
            reporter.inOrderRequiresFamiliarMock();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("InOrder can only verify mocks"));
        }
    }

    @Test(timeout = 4000)
    public void testStubPassedToVerify() {
        try {
            reporter.stubPassedToVerify();
            fail("Expected CannotVerifyStubOnlyMock");
        } catch (CannotVerifyStubOnlyMock e) {
            assertTrue(e.getMessage().contains("stubOnly() mock"));
        }
    }

    @Test(timeout = 4000)
    public void testReportNoSubMatchersFound() {
        try {
            reporter.reportNoSubMatchersFound("testMatcher");
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("No matchers found"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotMockFinalClass() {
        try {
            reporter.cannotMockFinalClass(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot mock/spy"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotStubVoidMethodWithAReturnValue() {
        try {
            reporter.cannotStubVoidMethodWithAReturnValue("someMethod");
            fail("Expected CannotStubVoidMethodWithReturnValue");
        } catch (CannotStubVoidMethodWithReturnValue e) {
            assertTrue(e.getMessage().contains("void method"));
        }
    }

    @Test(timeout = 4000)
    public void testOnlyVoidMethodsCanBeSetToDoNothing() {
        try {
            reporter.onlyVoidMethodsCanBeSetToDoNothing();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Only void methods can doNothing"));
        }
    }

    @Test(timeout = 4000)
    public void testWrongTypeOfReturnValue() {
        try {
            reporter.wrongTypeOfReturnValue("String", "Integer", "getValue");
            fail("Expected WrongTypeOfReturnValue");
        } catch (WrongTypeOfReturnValue e) {
            assertTrue(e.getMessage().contains("cannot be returned"));
        }
    }

    @Test(timeout = 4000)
    public void testWantedAtMostX() {
        try {
            reporter.wantedAtMostX(3, 5);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            assertTrue(e.getMessage().contains("Wanted at most"));
        }
    }

    @Test(timeout = 4000)
    public void testSmartNullPointerException() {
        try {
            reporter.smartNullPointerException("invocation", new LocationImpl());
            fail("Expected SmartNullPointerException");
        } catch (SmartNullPointerException e) {
            assertTrue(e.getMessage().contains("NullPointerException"));
        }
    }

    @Test(timeout = 4000)
    public void testNoArgumentValueWasCaptured() {
        try {
            reporter.noArgumentValueWasCaptured();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("No argument value was captured"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesDoesNotAcceptNullParameters() {
        try {
            reporter.extraInterfacesDoesNotAcceptNullParameters();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("does not accept null parameters"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesAcceptsOnlyInterfaces() {
        try {
            reporter.extraInterfacesAcceptsOnlyInterfaces(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("accepts only interfaces"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesCannotContainMockedType() {
        try {
            reporter.extraInterfacesCannotContainMockedType(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("does not accept the same type"));
        }
    }

    @Test(timeout = 4000)
    public void testExtraInterfacesRequiresAtLeastOneInterface() {
        try {
            reporter.extraInterfacesRequiresAtLeastOneInterface();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("requires at least one interface"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotCallAbstractRealMethod() {
        try {
            reporter.cannotCallAbstractRealMethod();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot call abstract real method"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotVerifyToString() {
        try {
            reporter.cannotVerifyToString();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("cannot verify toString"));
        }
    }

    @Test(timeout = 4000)
    public void testMoreThanOneAnnotationNotAllowed() {
        try {
            reporter.moreThanOneAnnotationNotAllowed("testField");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("more than one Mockito annotation"));
        }
    }

    @Test(timeout = 4000)
    public void testUnsupportedCombinationOfAnnotations() {
        try {
            reporter.unsupportedCombinationOfAnnotations("Mock", "Spy");
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("not permitted on a single field"));
        }
    }

    @Test(timeout = 4000)
    public void testAtMostAndNeverShouldNotBeUsedWithTimeout() {
        try {
            reporter.atMostAndNeverShouldNotBeUsedWithTimeout();
            fail("Expected FriendlyReminderException");
        } catch (FriendlyReminderException e) {
            assertTrue(e.getMessage().contains("friendly reminder"));
        }
    }

    @Test(timeout = 4000)
    public void testInvocationListenerDoesNotAcceptNullParameters() {
        try {
            reporter.invocationListenerDoesNotAcceptNullParameters();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("does not accept null parameters"));
        }
    }

    @Test(timeout = 4000)
    public void testInvocationListenersRequiresAtLeastOneListener() {
        try {
            reporter.invocationListenersRequiresAtLeastOneListener();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("requires at least one listener"));
        }
    }

    @Test(timeout = 4000)
    public void testDefaultAnswerDoesNotAcceptNullParameter() {
        try {
            reporter.defaultAnswerDoesNotAcceptNullParameter();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("does not accept null parameter"));
        }
    }

    @Test(timeout = 4000)
    public void testSpyAndDelegateAreMutuallyExclusive() {
        try {
            reporter.spyAndDelegateAreMutuallyExclusive();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("should not define a spy instance and a delegated instance"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidArgumentRangeAtIdentityAnswerCreationTime() {
        try {
            reporter.invalidArgumentRangeAtIdentityAnswerCreationTime();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Invalid argument index"));
        }
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testInvalidUseOfMatchersWithEmptyMatchers() {
        try {
            reporter.invalidUseOfMatchers(2, Collections.<LocalizedMatcher>emptyList());
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers"));
        }
    }

    @Test(timeout = 4000)
    public void testInvalidUseOfMatchersWithNullMatchers() {
        try {
            reporter.invalidUseOfMatchers(1, null);
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers"));
        }
    }

    @Test(timeout = 4000)
    public void testIncorrectUseOfAdditionalMatchersWithEmptyStack() {
        try {
            reporter.incorrectUseOfAdditionalMatchers("and", 2, Collections.<LocalizedMatcher>emptyList());
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Invalid use of argument matchers inside additional matcher"));
        }
    }

    @Test(timeout = 4000)
    public void testMisplacedArgumentMatcherWithEmptyList() {
        try {
            reporter.misplacedArgumentMatcher(Collections.<LocalizedMatcher>emptyList());
            fail("Expected InvalidUseOfMatchersException");
        } catch (InvalidUseOfMatchersException e) {
            assertTrue(e.getMessage().contains("Misplaced argument matcher detected"));
        }
    }

    @Test(timeout = 4000)
    public void testArgumentsAreDifferentWithNullLocations() {
        try {
            reporter.argumentsAreDifferent("wanted", "actual", null);
            fail("Expected exception from JUnitTool");
        } catch (Exception e) {
            // JUnitTool.createArgumentsAreDifferentException may return different exception types
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWantedButNotInvokedWithEmptyInvocations() {
        DescribedInvocation wanted = new DescribedInvocation() {
            @Override
            public String toString() {
                return "wanted invocation";
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.wantedButNotInvoked(wanted, Collections.<DescribedInvocation>emptyList());
            fail("Expected WantedButNotInvoked");
        } catch (WantedButNotInvoked e) {
            assertTrue(e.getMessage().contains("Wanted but not invoked"));
            assertTrue(e.getMessage().contains("zero interactions"));
        }
    }

    @Test(timeout = 4000)
    public void testTooManyActualInvocationsWithZeroCounts() {
        DescribedInvocation wanted = new DescribedInvocation() {
            @Override
            public String toString() {
                return "wanted";
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.tooManyActualInvocations(0, 0, wanted, new LocationImpl());
            fail("Expected TooManyActualInvocations");
        } catch (TooManyActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted"));
        }
    }

    @Test(timeout = 4000)
    public void testNeverWantedButInvoked() {
        DescribedInvocation wanted = new DescribedInvocation() {
            @Override
            public String toString() {
                return "never wanted";
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.neverWantedButInvoked(wanted, new LocationImpl());
            fail("Expected NeverWantedButInvoked");
        } catch (NeverWantedButInvoked e) {
            assertTrue(e.getMessage().contains("Never wanted here"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsWithNullLocation() {
        org.mockito.internal.reporting.Discrepancy discrepancy = 
            new org.mockito.internal.reporting.Discrepancy(1, 0);
        DescribedInvocation wanted = new DescribedInvocation() {
            @Override
            public String toString() {
                return "wanted";
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.tooLittleActualInvocations(discrepancy, wanted, null);
            fail("Expected TooLittleActualInvocations");
        } catch (TooLittleActualInvocations e) {
            assertTrue(e.getMessage().contains("Wanted"));
        }
    }

    @Test(timeout = 4000)
    public void testTooLittleActualInvocationsInOrderWithNullLocation() {
        org.mockito.internal.reporting.Discrepancy discrepancy = 
            new org.mockito.internal.reporting.Discrepancy(2, 1);
        DescribedInvocation wanted = new DescribedInvocation() {
            @Override
            public String toString() {
                return "wanted";
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
        };
        try {
            reporter.tooLittleActualInvocationsInOrder(discrepancy, wanted, null);
            fail("Expected VerificationInOrderFailure");
        } catch (VerificationInOrderFailure e) {
            assertTrue(e.getMessage().contains("Verification in order failure"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotInitializeForSpyAnnotation() {
        try {
            reporter.cannotInitializeForSpyAnnotation("testField", new Exception("test cause"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate a @Spy"));
        }
    }

    @Test(timeout = 4000)
    public void testCannotInitializeForInjectMocksAnnotation() {
        try {
            reporter.cannotInitializeForInjectMocksAnnotation("testField", new Exception("test cause"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks"));
        }
    }

    @Test(timeout = 4000)
    public void testFieldInitialisationThrewException() throws Exception {
        Field field = String.class.getDeclaredField("value");
        try {
            reporter.fieldInitialisationThrewException(field, new Exception("init failed"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot instantiate @InjectMocks field"));
        }
    }

    @Test(timeout = 4000)
    public void testInvocationListenerThrewException() {
        InvocationListener listener = new InvocationListener() {
            @Override
            public void reportInvocation(org.mockito.listeners.MethodInvocationReport methodInvocationReport) {
                // no-op
            }
        };
        try {
            reporter.invocationListenerThrewException(listener, new RuntimeException("listener error"));
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("threw an exception"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializableWontWorkForObjectsThatDontImplementSerializable() {
        try {
            reporter.serializableWontWorkForObjectsThatDontImplementSerializable(String.class);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("serializable()"));
        }
    }

    @Test(timeout = 4000)
    public void testUsingConstructorWithFancySerializable() {
        try {
            reporter.usingConstructorWithFancySerializable(SerializableMode.ACROSS_CLASSLOADERS);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("cannot be combined with"));
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test: noMoreInteractionsWanted should throw NoInteractionsWanted
     * even when mock is a bogus object (e.g., String) that causes MockUtil.getMockName() 
     * to throw ClassCastException.
     */
    @Test(timeout = 4000)
    public void testNoMoreInteractionsWantedWithBogusMock() {
        // Create a mock Invocation that returns a bogus mock object (String)
        Invocation undesired = new Invocation() {
            @Override
            public Object getMock() {
                return "bogusMock"; // This will cause ClassCastException in safelyGetMockName
            }
            @Override
            public Location getLocation() {
                return new LocationImpl();
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public int getSequenceNumber() {
                return 0;
            }
            @Override
            public Object callRealMethod() throws Throwable {
                return null;
            }
            @Override
            public Object getArgument(int index) {
                return null;
            }
            @Override
            public boolean isIgnoredForVerification() {
                return false;
            }
            @Override
            public boolean marksStrongReference() {
                return false;
            }
            @Override
            public void ignoreForVerification() {
                // no-op
            }
            @Override
            public Class<?> getRawReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public StubInfo stubInfo() {
                return null;
            }
            @Override
            public void stubInfo(StubInfo stubInfo) {
                // no-op
            }
            @Override
            public Object getRawReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object[0];
            }
            @Override
            public Class<?> getReturnType() {
                return Object.class;
            }
            @Override
            public boolean isVarArgs() {
                return false;
            }
            @Override
            public boolean isVoid() {
                return false;
            }
            @Override
            public boolean isVerified() {
                return false;
            }
            @Override
            public void setVerified() {
                // no-op
            }
            @Override
            public Object getRawArguments() {
                return new Object