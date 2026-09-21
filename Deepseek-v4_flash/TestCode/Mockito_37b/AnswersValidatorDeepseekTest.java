package org.mockito.internal.stubbing.answers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.invocation.Invocation;
import org.mockito.stubbing.Answer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targets the following branches in AnswersValidator:
 * 1. answer instanceof ThrowsException → validateException (null throwable, RT/Error, checked exception)
 * 2. answer instanceof Returns → validateReturnValue (void method, null on primitive, invalid return type)
 * 3. answer instanceof DoesNothing → validateDoNothing (non-void invocation)
 * 4. No handling for CallsRealMethods (known defect: missing validation for interface methods)
 * 
 * Boundary conditions:
 * - null throwable
 * - null return value on primitive return type
 * - non-void method with DoesNothing
 * - void method with Returns
 * - checked exception not declared
 * - invalid return type
 * 
 * Defect-specific: when answer is CallsRealMethods and invocation is on an interface,
 * the method should throw an exception (fixed version) but the defective version does nothing.
 */
public class AnswersValidatorDeepseekTest {

    // ---------- Helper: simple Invocation stub ----------
    private static class SimpleInvocation implements Invocation {
        private final boolean isVoid;
        private final boolean returnsPrimitive;
        private final boolean validReturnType;
        private final boolean validException;
        private final String methodReturnType;
        private final String methodName;

        SimpleInvocation(boolean isVoid, boolean returnsPrimitive,
                         boolean validReturnType, boolean validException,
                         String methodReturnType, String methodName) {
            this.isVoid = isVoid;
            this.returnsPrimitive = returnsPrimitive;
            this.validReturnType = validReturnType;
            this.validException = validException;
            this.methodReturnType = methodReturnType;
            this.methodName = methodName;
        }

        @Override
        public boolean isVoid() { return isVoid; }

        @Override
        public boolean returnsPrimitive() { return returnsPrimitive; }

        @Override
        public boolean isValidReturnType(Class<?> type) { return validReturnType; }

        @Override
        public boolean isValidException(Throwable throwable) { return validException; }

        @Override
        public String printMethodReturnType() { return methodReturnType; }

        @Override
        public String getMethodName() { return methodName; }

        // other Invocation methods – not needed for this test
        @Override public Object getMock() { return null; }
        @Override public Object[] getArguments() { return new Object[0]; }
        @Override public Class<?> getMethod() { return null; }
        @Override public boolean isInterface() { return false; } // overridden in defect test
        @Override public boolean isFromInterface() { return false; }
        @Override public Class<?> getRawReturnType() { return null; }
        @Override public String toString() { return ""; }
    }

    // Helper: create invocation that simulates an interface method
    private static class InterfaceInvocation extends SimpleInvocation {
        InterfaceInvocation() {
            super(false, false, true, true, "void", "interfaceMethod");
        }

        @Override
        public boolean isInterface() { return true; }
        @Override
        public boolean isFromInterface() { return true; }
    }

    private final AnswersValidator validator = new AnswersValidator();

    // =================== Partition A: Core Functional Logic ===================

    @Test(timeout = 4000)
    public void testValidateWithReturnsAndValidType() {
        Invocation invocation = new SimpleInvocation(false, false, true, true, "java.lang.String", "getValue");
        Answer<?> answer = new Returns("valid");
        validator.validate(answer, invocation);
        // no exception expected
    }

    @Test(timeout = 4000)
    public void testValidateWithThrowsExceptionRuntime() {
        Invocation invocation = new SimpleInvocation(false, false, true, true, "void", "doSomething");
        Answer<?> answer = new ThrowsException(new RuntimeException("runtime"));
        validator.validate(answer, invocation);
        // no exception expected
    }

    @Test(timeout = 4000)
    public void testValidateWithDoesNothingOnVoid() {
        Invocation invocation = new SimpleInvocation(true, false, true, true, "void", "doSomething");
        Answer<?> answer = new DoesNothing();
        validator.validate(answer, invocation);
        // no exception expected
    }

    @Test(timeout = 4000)
    public void testValidateWithThrowsExceptionError() {
        Invocation invocation = new SimpleInvocation(false, false, true, true, "void", "doSomething");
        Answer<?> answer = new ThrowsException(new Error("error"));
        validator.validate(answer, invocation);
        // Error is allowed, no exception expected
    }

    // =================== Partition B: Boundary & Exception Paths ===================

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateThrowsExceptionWithNullThrowable() {
        Invocation invocation = new SimpleInvocation(false, false, true, true, "void", "doSomething");
        Answer<?> answer = new ThrowsException(null);
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateReturnsOnVoidMethod() {
        Invocation invocation = new SimpleInvocation(true, false, true, true, "void", "doSomething");
        Answer<?> answer = new Returns("value");
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateReturnsNullOnPrimitive() {
        Invocation invocation = new SimpleInvocation(false, true, true, true, "int", "getCount");
        Answer<?> answer = new Returns(null);  // returnsNull() == true
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateReturnsInvalidType() {
        Invocation invocation = new SimpleInvocation(false, false, false, true, "java.lang.String", "getValue");
        Answer<?> answer = new Returns(123); // Integer returned, not String
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateThrowsExceptionCheckedNotDeclared() {
        Invocation invocation = new SimpleInvocation(false, false, true, false, "void", "doSomething");
        Answer<?> answer = new ThrowsException(new java.io.IOException("checked"));
        validator.validate(answer, invocation);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateDoesNothingOnNonVoid() {
        Invocation invocation = new SimpleInvocation(false, false, true, true, "java.lang.String", "getValue");
        Answer<?> answer = new DoesNothing();
        validator.validate(answer, invocation);
    }

    // =================== Partition C: Defect-Targeted Branch Zone ===================
    // Known defect: CallsRealMethods on an interface should fail, but the code does not validate it.
    // This test expects an exception (MockitoException). On the defective version no exception is thrown,
    // thus the test fails – revealing the bug.
    @Test(expected = MockitoException.class, timeout = 4000)
    public void testValidateCallsRealMethodsOnInterfaceShouldFail() {
        // Simulate an invocation from an interface (isInterface() returns true)
        Invocation invocation = new InterfaceInvocation();
        // The answer type that triggers the missing validation
        Answer<?> answer = new org.mockito.internal.stubbing.answers.CallsRealMethods();
        validator.validate(answer, invocation);
        // If no exception is thrown, the test fails (due to expected = MockitoException.class)
        // On the fixed version, validate would throw an exception.
    }

    // =================== Additional Edge Cases ===================

    @Test(timeout = 4000)
    public void testValidateWithNullAnswerDoesNothing() {
        // If answer is null, no instanceof check matches, so no validation actions occur.
        Invocation invocation = new SimpleInvocation(false, false, true, true, "void", "doSomething");
        validator.validate(null, invocation);
        // should not throw any exception
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testValidateWithNullInvocationThrowsNPE() {
        Answer<?> answer = new DoesNothing();
        validator.validate(answer, null);
        // NPE is expected because invocation methods are called without null check
    }
}