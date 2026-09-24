/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.invocation;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.internal.invocation.realmethod.RealMethod;
import org.mockito.internal.reporting.PrintSettings;
import org.mockito.invocation.InvocationOnMock;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.invocation.Invocation
 *
 * 1. expandVarArgs(isVarArgs, args):
 *    - Branch: !isVarArgs && args == null -> returns new Object[0]
 *    - Branch: !isVarArgs && args != null -> returns args
 *    - Branch: isVarArgs && lastArg != null && !lastArg.isArray() -> returns args unchanged
 *    - Branch: isVarArgs && lastArg == null -> expands to new Object[] { null }
 *    - Branch: isVarArgs && lastArg is Object[] -> flattens into newArgs
 *    - Branch: isVarArgs && lastArg is primitive array -> flattens primitive array via ArrayEquals.createObjectArray
 *    - Branch: isVarArgs && lastArg is empty array -> flattens correctly to non-vararg length
 *
 * 2. equals(Object o) & equalArguments(Object[]):
 *    - Branch: o == null -> false
 *    - Branch: o.getClass() != this.getClass() -> false (including subclasses)
 *    - Branch: mock mismatch -> false
 *    - Branch: method mismatch -> false
 *    - Branch: arguments mismatch -> false
 *    - Branch: identical fields -> true
 *    - Branch: same instance -> true
 *
 * 3. hashCode():
 *    - Branch: always throws RuntimeException("hashCode() is not implemented")
 *
 * 4. toString() & toString(matchers, printSettings):
 *    - Branch: arguments with Object[] / primitive array -> uses ArrayEquals matcher
 *    - Branch: arguments with non-array / null -> uses Equals matcher
 *    - Branch: invocation.length() <= 45 && !multiline -> single-line format
 *    - Branch: invocation.length() > 45 && !matchers.isEmpty() -> multiline format
 *    - Branch: printSettings.isMultiline() == true -> multiline format
 *    - Branch: matchers.isEmpty() && invocation.length() > 45 -> single-line format
 *
 * 5. isValidException(Throwable):
 *    - Branch: exception matches declared exception type -> true
 *    - Branch: exception is subclass of declared exception type -> true
 *    - Branch: exception does not match any declared exception type -> false
 *    - Branch: method declares no exception types -> false
 *
 * 6. isValidReturnType(Class):
 *    - Branch: primitive return type && primitiveTypeOf(clazz) == method.getReturnType() -> true
 *    - Branch: primitive return type && mismatch / non-primitive wrapper -> false
 *    - Branch: non-primitive return type && method.getReturnType().isAssignableFrom(clazz) -> true
 *    - Branch: non-primitive return type && unassignable clazz -> false
 *    - Branch: void.class return type -> handled as primitive
 *
 * 7. isVoid(), returnsPrimitive(), printMethodReturnType(), getMethodName():
 *    - Branch: returnType == Void.TYPE vs non-void
 *    - Branch: primitive vs non-primitive
 *
 * 8. Verification state transitions:
 *    - Branch: markVerified() -> verified=true, verifiedInOrder=false
 *    - Branch: markVerifiedInOrder() -> verified=true, verifiedInOrder=true
 *
 * 9. callRealMethod() & DEFECT ZONE:
 *    - Branch: normal invocation with valid RealMethod -> returns result
 *    - Branch: RealMethod throws checked/unchecked Throwable -> rethrown directly
 *    - DEFECT Branch: Defect ground truth (InvocationTest::shouldScreamWhenCallingRealMethodOnInterface)
 *      When realMethod is null (such as on interface calls), invoking realMethod throws NullPointerException
 *      instead of a descriptive MockitoException indicating interface methods have no implementation.
 * -------------------------------------------------------------------------------------------------------
 */
public class InvocationGeminiTest {

    // -------------------------------------------------------------------------
    // Test Infrastructure & Helper Factories
    // -------------------------------------------------------------------------

    private MockitoMethod createMockitoMethod(
            final String name,
            final Class<?> returnType,
            final Class<?>[] parameterTypes,
            final Class<?>[] exceptionTypes,
            final boolean isVarArgs,
            final Method javaMethod) {
        return (MockitoMethod) Proxy.newProxyInstance(
                MockitoMethod.class.getClassLoader(),
                new Class<?>[] { MockitoMethod.class },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String mName = method.getName();
                        if ("getName".equals(mName)) {
                            return name;
                        } else if ("getReturnType".equals(mName)) {
                            return returnType;
                        } else if ("getParameterTypes".equals(mName)) {
                            return parameterTypes != null ? parameterTypes : new Class<?>[0];
                        } else if ("getExceptionTypes".equals(mName)) {
                            return exceptionTypes != null ? exceptionTypes : new Class<?>[0];
                        } else if ("isVarArgs".equals(mName)) {
                            return isVarArgs;
                        } else if ("getJavaMethod".equals(mName)) {
                            return javaMethod;
                        } else if ("equals".equals(mName)) {
                            return proxy == args[0];
                        } else if ("hashCode".equals(mName)) {
                            return System.identityHashCode(proxy);
                        } else if ("toString".equals(mName)) {
                            return "MockitoMethod[" + name + "]";
                        }
                        return null;
                    }
                }
        );
    }

    private RealMethod createRealMethod(final Object resultToReturn, final Throwable toThrow) {
        return new RealMethod() {
            private static final long serialVersionUID = 1L;
            @Override
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                if (toThrow != null) {
                    throw toThrow;
                }
                return resultToReturn;
            }
        };
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicGettersAndState() {
        Object mock = "mockInstance";
        MockitoMethod method = createMockitoMethod("sampleMethod", String.class, new Class<?>[0], new Class<?>[0], false, null);
        Object[] args = new Object[] { "arg1", 42 };
        RealMethod realMethod = createRealMethod("result", null);

        Invocation invocation = new Invocation(mock, method, args, 7, realMethod);

        assertSame(mock, invocation.getMock());
        assertSame(method, invocation.getMethod());
        assertEquals("sampleMethod", invocation.getMethodName());
        assertEquals("String", invocation.printMethodReturnType());
        assertEquals(Integer.valueOf(7), invocation.getSequenceNumber());
        assertEquals(2, invocation.getArgumentsCount());
        assertArrayEquals(args, invocation.getArguments());
        assertArrayEquals(args, invocation.getRawArguments());
        assertNotNull(invocation.getLocation());
        assertFalse(invocation.isVerified());
        assertFalse(invocation.isVerifiedInOrder());
    }

    @Test(timeout = 4000)
    public void testVerificationStateTransitions() {
        MockitoMethod method = createMockitoMethod("doWork", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation1 = new Invocation("mock1", method, new Object[0], 1, null);

        assertFalse(invocation1.isVerified());
        assertFalse(invocation1.isVerifiedInOrder());

        invocation1.markVerified();
        assertTrue(invocation1.isVerified());
        assertFalse(invocation1.isVerifiedInOrder());

        Invocation invocation2 = new Invocation("mock2", method, new Object[0], 2, null);
        invocation2.markVerifiedInOrder();
        assertTrue(invocation2.isVerified());
        assertTrue(invocation2.isVerifiedInOrder());
    }

    @Test(timeout = 4000)
    public void testIsToStringDetection() throws Exception {
        Method toStringMethod = Object.class.getMethod("toString");
        MockitoMethod mockitoToString = createMockitoMethod("toString", String.class, new Class<?>[0], new Class<?>[0], false, toStringMethod);
        Invocation invToString = new Invocation("mock", mockitoToString, new Object[0], 1, null);
        assertTrue(Invocation.isToString(invToString));

        Method hashCodeMethod = Object.class.getMethod("hashCode");
        MockitoMethod mockitoHashCode = createMockitoMethod("hashCode", int.class, new Class<?>[0], new Class<?>[0], false, hashCodeMethod);
        Invocation invHashCode = new Invocation("mock", mockitoHashCode, new Object[0], 2, null);
        assertFalse(Invocation.isToString(invHashCode));
    }

    @Test(timeout = 4000)
    public void testCallRealMethodNormalExecution() throws Throwable {
        RealMethod realMethod = createRealMethod("computedValue", null);
        MockitoMethod method = createMockitoMethod("calculate", String.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mockService", method, new Object[] { "test" }, 1, realMethod);

        Object result = invocation.callRealMethod();
        assertEquals("computedValue", result);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & VarArgs Expansion
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testExpandVarArgsWhenNotVarArgsAndNullArgs() {
        MockitoMethod method = createMockitoMethod("noArgs", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", method, null, 1, null);

        assertEquals(0, invocation.getArguments().length);
        assertNull(invocation.getRawArguments());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWhenNotVarArgsWithNonNullArgs() {
        MockitoMethod method = createMockitoMethod("fixedArgs", void.class, new Class<?>[] { String.class }, new Class<?>[0], false, null);
        Object[] rawArgs = new Object[] { "param" };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        assertSame(rawArgs, invocation.getArguments());
        assertSame(rawArgs, invocation.getRawArguments());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWhenLastArgIsNotAnArray() {
        MockitoMethod method = createMockitoMethod("varArgMethod", void.class, new Class<?>[] { String.class, Object[].class }, new Class<?>[0], true, null);
        Object[] rawArgs = new Object[] { "prefix", "scalarArgNotArray" };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        assertArrayEquals(new Object[] { "prefix", "scalarArgNotArray" }, invocation.getArguments());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWhenLastArgIsNull() {
        MockitoMethod method = createMockitoMethod("varArgMethod", void.class, new Class<?>[] { String.class, Object[].class }, new Class<?>[0], true, null);
        Object[] rawArgs = new Object[] { "prefix", null };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        Object[] expected = new Object[] { "prefix", null };
        assertArrayEquals(expected, invocation.getArguments());
        assertEquals(2, invocation.getArgumentsCount());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWithObjectArray() {
        MockitoMethod method = createMockitoMethod("varArgMethod", void.class, new Class<?>[] { int.class, String[].class }, new Class<?>[0], true, null);
        Object[] rawArgs = new Object[] { 10, new String[] { "a", "b", "c" } };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        Object[] expected = new Object[] { 10, "a", "b", "c" };
        assertArrayEquals(expected, invocation.getArguments());
        assertEquals(4, invocation.getArgumentsCount());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWithPrimitiveArray() {
        MockitoMethod method = createMockitoMethod("primitiveVarArg", void.class, new Class<?>[] { String.class, int[].class }, new Class<?>[0], true, null);
        Object[] rawArgs = new Object[] { "count", new int[] { 1, 2, 3 } };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        Object[] expected = new Object[] { "count", 1, 2, 3 };
        assertArrayEquals(expected, invocation.getArguments());
        assertEquals(4, invocation.getArgumentsCount());
    }

    @Test(timeout = 4000)
    public void testExpandVarArgsWithEmptyArray() {
        MockitoMethod method = createMockitoMethod("varArgMethod", void.class, new Class<?>[] { String.class, Object[].class }, new Class<?>[0], true, null);
        Object[] rawArgs = new Object[] { "prefix", new Object[0] };
        Invocation invocation = new Invocation("mock", method, rawArgs, 1, null);

        Object[] expected = new Object[] { "prefix" };
        assertArrayEquals(expected, invocation.getArguments());
        assertEquals(1, invocation.getArgumentsCount());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J Ground Truth Defect:
     * - org.mockito.internal.invocation.InvocationTest::shouldScreamWhenCallingRealMethodOnInterface
     *   --> java.lang.NullPointerException
     *
     * Calling callRealMethod on an interface mock (where realMethod is null or interface lacks implementation)
     * triggers NullPointerException in the defective code instead of throwing a clean MockitoException.
     */
    @Test(timeout = 4000)
    public void shouldScreamWhenCallingRealMethodOnInterface() throws Throwable {
        Method listGetMethod = List.class.getMethod("get", int.class);
        MockitoMethod method = createMockitoMethod(
                "get",
                Object.class,
                new Class<?>[] { int.class },
                new Class<?>[0],
                false,
                listGetMethod
        );

        // In the defective implementation, realMethod is null on interface mocks
        Invocation invocation = new Invocation("interfaceMock", method, new Object[] { 0 }, 1, null);

        try {
            invocation.callRealMethod();
            fail("Expected invocation.callRealMethod() to fail when invoked on interface");
        } catch (NullPointerException npe) {
            // Defect reproduced: Defective Mockito triggers NullPointerException here
            throw npe;
        } catch (org.mockito.exceptions.base.MockitoException expected) {
            // Correct behavior on fixed version: Reporter throws descriptive MockitoException
            String msg = expected.getMessage().toLowerCase();
            assertTrue(msg.contains("interface") || msg.contains("real method"));
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCallRealMethodRethrowsCheckedException() {
        IOException cause = new IOException("Disk failure");
        RealMethod realMethod = createRealMethod(null, cause);
        MockitoMethod method = createMockitoMethod("read", int.class, new Class<?>[0], new Class<?>[] { IOException.class }, false, null);
        Invocation invocation = new Invocation("ioMock", method, new Object[0], 1, realMethod);

        try {
            invocation.callRealMethod();
            fail("Expected IOException to be thrown");
        } catch (Throwable t) {
            assertSame(cause, t);
        }
    }

    @Test(timeout = 4000)
    public void testCallRealMethodRethrowsRuntimeException() {
        IllegalStateException cause = new IllegalStateException("Invalid internal state");
        RealMethod realMethod = createRealMethod(null, cause);
        MockitoMethod method = createMockitoMethod("compute", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mockObj", method, new Object[0], 1, realMethod);

        try {
            invocation.callRealMethod();
            fail("Expected IllegalStateException to be thrown");
        } catch (Throwable t) {
            assertSame(cause, t);
        }
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testHashCodeThrowsRuntimeException() {
        MockitoMethod method = createMockitoMethod("test", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", method, new Object[0], 1, null);

        invocation.hashCode();
    }

    @Test(timeout = 4000)
    public void testIsValidExceptionMatchingHierarchy() {
        MockitoMethod method = createMockitoMethod(
                "thrower",
                void.class,
                new Class<?>[0],
                new Class<?>[] { IOException.class, IllegalArgumentException.class },
                false,
                null
        );
        Invocation invocation = new Invocation("mock", method, new Object[0], 1, null);

        // Exact match
        assertTrue(invocation.isValidException(new IOException("error")));
        assertTrue(invocation.isValidException(new IllegalArgumentException("illegal")));

        // Subclass match
        assertTrue(invocation.isValidException(new FileNotFoundException("not found")));

        // Mismatch
        assertFalse(invocation.isValidException(new SQLException("sql error")));
        assertFalse(invocation.isValidException(new Exception("generic exception")));
    }

    @Test(timeout = 4000)
    public void testIsValidExceptionWithNoDeclaredExceptions() {
        MockitoMethod method = createMockitoMethod("safeMethod", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", method, new Object[0], 1, null);

        assertFalse(invocation.isValidException(new Exception()));
        assertFalse(invocation.isValidException(new RuntimeException()));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnTypeForPrimitiveTypes() {
        MockitoMethod intMethod = createMockitoMethod("getInt", int.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", intMethod, new Object[0], 1, null);

        assertTrue(invocation.returnsPrimitive());
        assertFalse(invocation.isVoid());
        assertTrue(invocation.isValidReturnType(Integer.class));
        assertFalse(invocation.isValidReturnType(Long.class));
        assertFalse(invocation.isValidReturnType(String.class));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnTypeForVoid() {
        MockitoMethod voidMethod = createMockitoMethod("doNothing", void.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", voidMethod, new Object[0], 1, null);

        assertTrue(invocation.isVoid());
        assertTrue(invocation.returnsPrimitive());
        assertTrue(invocation.isValidReturnType(Void.class));
        assertFalse(invocation.isValidReturnType(String.class));
    }

    @Test(timeout = 4000)
    public void testIsValidReturnTypeForReferenceTypes() {
        MockitoMethod seqMethod = createMockitoMethod("getSequence", CharSequence.class, new Class<?>[0], new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", seqMethod, new Object[0], 1, null);

        assertFalse(invocation.isVoid());
        assertFalse(invocation.returnsPrimitive());
        assertTrue(invocation.isValidReturnType(CharSequence.class));
        assertTrue(invocation.isValidReturnType(String.class));
        assertTrue(invocation.isValidReturnType(StringBuilder.class));
        assertFalse(invocation.isValidReturnType(Object.class));
        assertFalse(invocation.isValidReturnType(Integer.class));
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle, Contract Integrity & String Formatting
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsContract() {
        Object mockA = "mockInstanceA";
        Object mockB = "mockInstanceB";
        MockitoMethod methodA = createMockitoMethod("methodOne", void.class, new Class<?>[0], new Class<?>[0], false, null);
        MockitoMethod methodB = createMockitoMethod("methodTwo", void.class, new Class<?>[0], new Class<?>[0], false, null);

        Invocation inv1 = new Invocation(mockA, methodA, new Object[] { "x", 1 }, 1, null);
        Invocation inv1Clone = new Invocation(mockA, methodA, new Object[] { "x", 1 }, 1, null);
        Invocation invDifferentMock = new Invocation(mockB, methodA, new Object[] { "x", 1 }, 1, null);
        Invocation invDifferentMethod = new Invocation(mockA, methodB, new Object[] { "x", 1 }, 1, null);
        Invocation invDifferentArgs = new Invocation(mockA, methodA, new Object[] { "x", 2 }, 1, null);

        // Reflexive
        assertTrue(inv1.equals(inv1));

        // Symmetric & Equal
        assertTrue(inv1.equals(inv1Clone));
        assertTrue(inv1Clone.equals(inv1));

        // Different mock
        assertFalse(inv1.equals(invDifferentMock));

        // Different method
        assertFalse(inv1.equals(invDifferentMethod));

        // Different arguments
        assertFalse(inv1.equals(invDifferentArgs));

        // Null comparison
        assertFalse(inv1.equals(null));

        // Different class type
        assertFalse(inv1.equals("NotAnInvocation"));

        // Subclass type comparison
        Invocation subclassInv = new Invocation(mockA, methodA, new Object[] { "x", 1 }, 1, null) {
            private static final long serialVersionUID = 1L;
        };
        assertFalse(inv1.equals(subclassInv));
        assertFalse(subclassInv.equals(inv1));
    }

    @Test(timeout = 4000)
    public void testToStringSingleLineWhenShort() {
        MockitoMethod method = createMockitoMethod("foo", void.class, new Class<?>[] { String.class }, new Class<?>[0], false, null);
        Invocation invocation = new Invocation("myMock", method, new Object[] { "abc" }, 1, null);

        String result = invocation.toString();
        assertNotNull(result);
        assertTrue(result.contains("myMock.foo"));
        assertFalse(result.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testToStringMultilineWhenExceedsMaxLineLength() {
        MockitoMethod method = createMockitoMethod(
                "veryLongMethodNameToExceedLineLengthThreshold",
                void.class,
                new Class<?>[] { String.class, String.class },
                new Class<?>[0],
                false,
                null
        );
        Object[] args = new Object[] { "firstExtraLongArgumentString", "secondExtraLongArgumentString" };
        Invocation invocation = new Invocation("veryLongMockInstanceName", method, args, 1, null);

        String result = invocation.toString();
        assertNotNull(result);
        assertTrue(result.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testToStringWithPrintSettingsMultilineForced() {
        MockitoMethod method = createMockitoMethod("shortMethod", void.class, new Class<?>[] { int.class }, new Class<?>[0], false, null);
        Invocation invocation = new Invocation("mock", method, new Object[] { 5 }, 1, null);

        PrintSettings printSettings = new PrintSettings();
        printSettings.setMultiline(true);

        String result = invocation.toString(printSettings);
        assertNotNull(result);
        assertTrue(result.contains("\n"));
    }

    @Test(timeout = 4000)
    public void testToStringWithArrayArgumentsCoversArrayEqualsMatcher() {
        MockitoMethod method = createMockitoMethod("processArray", void.class, new Class<?>[] { int[].class, String.class, Object.class }, new Class<?>[0], false, null);
        Object[] args = new Object[] { new int[] { 10, 20 }, "text", null };
        Invocation invocation = new Invocation("mockService", method, args, 1, null);

        List<Matcher> matchers = invocation.argumentsToMatchers();
        assertEquals(3, matchers.size());
        assertTrue(matchers.get(0) instanceof org.mockito.internal.matchers.ArrayEquals);
        assertTrue(matchers.get(1) instanceof org.mockito.internal.matchers.Equals);
        assertTrue(matchers.get(2) instanceof org.mockito.internal.matchers.Equals);

        String str = invocation.toString();
        assertNotNull(str);
        assertTrue(str.contains("[10, 20]"));
    }

    @Test(timeout = 4000)
    public void testToStringWithEmptyMatchersAndLongInvocationLine() {
        MockitoMethod method = createMockitoMethod(
                "methodWithLongNameThatExceedsFortyFiveCharactersWithoutAnyArguments",
                void.class,
                new Class<?>[0],
                new Class<?>[0],
                false,
                null
        );
        Invocation invocation = new Invocation("mockName", method, new Object[0], 1, null);

        String result = invocation.toString();
        assertNotNull(result);
        assertFalse(result.contains("\n"));
    }
}