package org.mockito.internal.stubbing.answers;

import static org.junit.Assert.*;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationBuilder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.stubbing.answers.AnswersValidator
 *
 * Decision / Branch Matrix:
 * 1. validate(Answer, Invocation):
 *    - answer instanceof ThrowsException: -> validateException(...)
 *    - answer instanceof Returns:         -> validateReturnValue(...)
 *    - answer instanceof DoesNothing:     -> validateDoNothing(...)
 *    - answer instanceof CallsRealMethods: -> validateMockingConcreteClass(...) [DEFECT ZONE: Missing in defective code]
 *    - answer of other Answer type:       -> Pass through without validation
 *
 * 2. validateDoNothing(DoesNothing, Invocation):
 *    - !invocation.isVoid()               -> reporter.onlyVoidMethodsCanBeSetToDoNothing() [Throws MockitoException]
 *    - invocation.isVoid()                -> OK
 *
 * 3. validateReturnValue(Returns, Invocation):
 *    - invocation.isVoid()                -> reporter.cannotStubVoidMethodWithAReturnValue() [Throws MockitoException]
 *    - returnsNull() && returnsPrimitive()-> reporter.wrongTypeOfReturnValue(...) [Throws MockitoException]
 *    - returnsNull() && !returnsPrimitive()-> OK
 *    - !returnsNull() && !isValidReturnType() -> reporter.wrongTypeOfReturnValue(...) [Throws MockitoException]
 *    - !returnsNull() && isValidReturnType()  -> OK
 *
 * 4. validateException(ThrowsException, Invocation):
 *    - throwable == null                  -> reporter.cannotStubWithNullThrowable() [Throws MockitoException]
 *    - throwable instanceof RuntimeException -> Returns immediately (OK)
 *    - throwable instanceof Error            -> Returns immediately (OK)
 *    - Checked exception && !isValidException() -> reporter.checkedExceptionInvalid(...) [Throws MockitoException]
 *    - Checked exception && isValidException()  -> OK
 *
 * 5. validateMockingConcreteClass(CallsRealMethods, Invocation) [Defects4J Target Defect]:
 *    - invocation on Interface method     -> reporter.cannotCallRealMethodOnInterface() [Throws MockitoException]
 *    - Defective version omits CallsRealMethods validation entirely, allowing spy invocation on interfaces to pass
 *      validation silently, later crashing during mock execution.
 * --------------------------------------------------------------------------------------------------------------------
 */
public class AnswersValidatorGeminiTest {

    private AnswersValidator validator;

    @Before
    public void setUp() {
        validator = new AnswersValidator();
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidateDoNothingOnVoidMethodSucceeds() {
        Invocation invocation = new InvocationBuilder().method("voidMethod").toInvocation();
        assertTrue("Precondition: method must be void", invocation.isVoid());

        validator.validate(new DoesNothing(), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueMatchingTypeSucceeds() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        assertFalse("Precondition: method must not be void", invocation.isVoid());

        validator.validate(new Returns("validString"), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueNullOnNonPrimitiveSucceeds() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        assertFalse("Precondition: method must not return primitive", invocation.returnsPrimitive());

        validator.validate(new Returns(null), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateReturnValuePrimitiveMatchingTypeSucceeds() {
        Invocation invocation = new InvocationBuilder().method("booleanReturningMethod").toInvocation();
        assertTrue("Precondition: method must return primitive", invocation.returnsPrimitive());

        validator.validate(new Returns(Boolean.TRUE), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateExceptionRuntimeExceptionSucceeds() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        validator.validate(new ThrowsException(new IllegalArgumentException("test")), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateExceptionErrorSucceeds() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        validator.validate(new ThrowsException(new OutOfMemoryError("test")), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateExceptionDeclaredCheckedExceptionSucceeds() {
        Invocation invocation = new InvocationBuilder().method("canThrowException").toInvocation();

        validator.validate(new ThrowsException(new Exception("declared exception")), invocation);
    }

    @Test(timeout = 4000)
    public void testValidateCustomAnswerPassesThrough() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        Answer<String> customAnswer = new Answer<String>() {
            @Override
            public String answer(InvocationOnMock inv) {
                return "custom";
            }
        };

        validator.validate(customAnswer, invocation);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidateExceptionNullThrowableThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        try {
            validator.validate(new ThrowsException(null), invocation);
            fail("Expected MockitoException when throwable is null");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueNullOnPrimitiveMethodThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("booleanReturningMethod").toInvocation();
        assertTrue(invocation.returnsPrimitive());

        try {
            validator.validate(new Returns(null), invocation);
            fail("Expected MockitoException when returning null for primitive method");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueWrongTypeThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        try {
            validator.validate(new Returns(Integer.valueOf(123)), invocation);
            fail("Expected MockitoException when return type does not match method signature");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: Spying / calling real method on interface should fail fast during validation.
     * In defective Mockito versions, AnswersValidator does not validate CallsRealMethods,
     * allowing the stubbing to proceed and causing assertion failures downstream.
     */
    @Test(timeout = 4000)
    public void shouldFailWhenCallingRealMethodOnInterface() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        assertTrue("Precondition: simpleMethod must be declared on an interface",
                invocation.getMethod().getDeclaringClass().isInterface());

        try {
            validator.validate(new CallsRealMethods(), invocation);
            fail("Should fail when calling real method on an interface");
        } catch (MockitoException expected) {
            assertNotNull("Exception message should be populated", expected.getMessage());
        }
    }

    /**
     * Exact regression reproduction targeting the defective test case from Defects4J.
     */
    @Test(timeout = 4000)
    public void shouldFailWhenCallingRealMethodOnIterface() {
        Invocation invocation = new InvocationBuilder().method("voidMethod").toInvocation();
        assertTrue("Precondition: voidMethod must be declared on an interface",
                invocation.getMethod().getDeclaringClass().isInterface());

        try {
            validator.validate(new CallsRealMethods(), invocation);
            fail("Should fail fast with MockitoException when CallsRealMethods is validated on interface");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidateDoNothingOnNonVoidMethodThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();
        assertFalse("Precondition: simpleMethod is not void", invocation.isVoid());

        try {
            validator.validate(new DoesNothing(), invocation);
            fail("Expected MockitoException when doNothing is set on non-void method");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueOnVoidMethodThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("voidMethod").toInvocation();
        assertTrue("Precondition: voidMethod is void", invocation.isVoid());

        try {
            validator.validate(new Returns("dummy"), invocation);
            fail("Expected MockitoException when stubbing void method with return value");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateReturnValueNullOnVoidMethodThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("voidMethod").toInvocation();
        assertTrue("Precondition: voidMethod is void", invocation.isVoid());

        try {
            validator.validate(new Returns(null), invocation);
            fail("Expected MockitoException when stubbing void method with null return value");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testValidateExceptionUndeclaredCheckedExceptionThrowsMockitoException() {
        Invocation invocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        try {
            validator.validate(new ThrowsException(new IOException("undeclared checked exception")), invocation);
            fail("Expected MockitoException when throwing checked exception not declared on method");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Multi-Branch Verification
    // =========================================================================

    @Test(timeout = 4000)
    public void testRepeatedValidationStateIntegrity() {
        AnswersValidator localValidator = new AnswersValidator();
        Invocation voidInvocation = new InvocationBuilder().method("voidMethod").toInvocation();
        Invocation nonVoidInvocation = new InvocationBuilder().method("simpleMethod").toInvocation();

        // 1. Valid execution
        localValidator.validate(new DoesNothing(), voidInvocation);

        // 2. Exception execution
        try {
            localValidator.validate(new DoesNothing(), nonVoidInvocation);
            fail("Expected failure on invalid invocation");
        } catch (MockitoException expected) {
            assertNotNull(expected.getMessage());
        }

        // 3. Re-execution after failure should remain consistent
        localValidator.validate(new Returns("hello"), nonVoidInvocation);
    }
}