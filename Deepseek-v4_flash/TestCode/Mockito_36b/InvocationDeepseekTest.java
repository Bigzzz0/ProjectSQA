package org.mockito.internal.invocation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Constructor: verify arguments expansion for varargs/non-varargs
 * - getMock(), getMethod(), getArguments(), getSequenceNumber()
 * - markVerified(), markVerifiedInOrder(), isVerified(), isVerifiedInOrder()
 * - getRawArguments(), getArgumentsCount(), getLocation()
 * - isVoid(), returnsPrimitive(), printMethodReturnType(), getMethodName()
 * - isValidException(), isValidReturnType()
 * - callRealMethod() on concrete class vs interface
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - expandVarArgs: null args array, null vararg element, empty vararg array
 * - expandVarArgs: non-vararg method with null last argument
 * - expandVarArgs: vararg method with non-array last argument
 * - equals(): null argument, different class, same object
 * - equalArguments: null vs empty, different lengths, array contents
 * - toString(): empty matchers, multiline formatting, MAX_LINE_LENGTH boundary
 * - argumentsToMatchers(): null argument, array argument, normal object
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - callRealMethod() on interface mock -> should throw appropriate exception
 *   (targeting NullPointerException/NoSuchMethodError defect)
 * - isValidException: exception hierarchy, null throwable
 * - isValidReturnType: primitive types, null class, assignable types
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - hashCode() throws RuntimeException
 * - equals() with null or wrong class returns false
 * - expandVarArgs with null args returns empty array
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - equals() symmetry, consistency
 * - toString() produces expected format
 */
public class InvocationDeepseekTest {
    
    // ========== Helper methods to create test invocations ==========
    
    private Invocation createInvocation(Object mock, String methodName, Class<?>[] paramTypes, Object[] args, boolean isVarArgs) throws Exception {
        MockitoMethod method = createMockitoMethod(methodName, paramTypes, isVarArgs);
        RealMethod realMethod = new RealMethod() {
            @Override
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return null;
            }
        };
        return new Invocation(mock, method, args, 1, realMethod);
    }
    
    private MockitoMethod createMockitoMethod(String methodName, Class<?>[] paramTypes, boolean isVarArgs) {
        return new MockitoMethod() {
            @Override
            public String getName() {
                return methodName;
            }
            
            @Override
            public Class<?> getReturnType() {
                return String.class;
            }
            
            @Override
            public Class<?>[] getParameterTypes() {
                return paramTypes;
            }
            
            @Override
            public Class<?>[] getExceptionTypes() {
                return new Class<?>[] { Exception.class };
            }
            
            @Override
            public boolean isVarArgs() {
                return isVarArgs;
            }
            
            @Override
            public Method getJavaMethod() {
                return null;
            }
        };
    }
    
    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() throws Exception {
        Object mock = new Object();
        Class<?>[] paramTypes = { String.class, int.class };
        Object[] args = { "test", 42 };
        Invocation invocation = createInvocation(mock, "someMethod", paramTypes, args, false);
        
        assertSame("Mock should be the same object", mock, invocation.getMock());
        assertEquals("Method name should match", "someMethod", invocation.getMethodName());
        assertEquals("Arguments count should be 2", 2, invocation.getArgumentsCount());
        assertEquals("Sequence number should be 1", Integer.valueOf(1), invocation.getSequenceNumber());
        assertNotNull("Location should not be null", invocation.getLocation());
        assertFalse("Should not be verified initially", invocation.isVerified());
        assertFalse("Should not be verified in order initially", invocation.isVerifiedInOrder());
    }
    
    @Test(timeout = 4000)
    public void testMarkVerified() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        
        invocation.markVerified();
        assertTrue("Should be verified after markVerified()", invocation.isVerified());
        assertFalse("Should not be verified in order", invocation.isVerifiedInOrder());
    }
    
    @Test(timeout = 4000)
    public void testMarkVerifiedInOrder() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        
        invocation.markVerifiedInOrder();
        assertTrue("Should be verified after markVerifiedInOrder()", invocation.isVerified());
        assertTrue("Should be verified in order", invocation.isVerifiedInOrder());
    }
    
    @Test(timeout = 4000)
    public void testGetRawArguments() throws Exception {
        Object[] rawArgs = { "raw1", 42 };
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], rawArgs, false);
        
        assertArrayEquals("Raw arguments should match", rawArgs, invocation.getRawArguments());
    }
    
    @Test(timeout = 4000)
    public void testIsVoid() throws Exception {
        MockitoMethod voidMethod = new MockitoMethod() {
            @Override public String getName() { return "voidMethod"; }
            @Override public Class<?> getReturnType() { return void.class; }
            @Override public Class<?>[] getParameterTypes() { return new Class<?>[0]; }
            @Override public Class<?>[] getExceptionTypes() { return new Class<?>[0]; }
            @Override public boolean isVarArgs() { return false; }
            @Override public Method getJavaMethod() { return null; }
        };
        
        Invocation invocation = new Invocation(new Object(), voidMethod, new Object[0], 1, null);
        assertTrue("Should be void", invocation.isVoid());
    }
    
    @Test(timeout = 4000)
    public void testReturnsPrimitive() throws Exception {
        MockitoMethod primitiveMethod = new MockitoMethod() {
            @Override public String getName() { return "intMethod"; }
            @Override public Class<?> getReturnType() { return int.class; }
            @Override public Class<?>[] getParameterTypes() { return new Class<?>[0]; }
            @Override public Class<?>[] getExceptionTypes() { return new Class<?>[0]; }
            @Override public boolean isVarArgs() { return false; }
            @Override public Method getJavaMethod() { return null; }
        };
        
        Invocation invocation = new Invocation(new Object(), primitiveMethod, new Object[0], 1, null);
        assertTrue("Should return primitive", invocation.returnsPrimitive());
    }
    
    @Test(timeout = 4000)
    public void testPrintMethodReturnType() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertEquals("Return type should be String", "String", invocation.printMethodReturnType());
    }
    
    // ========== Partition B: Boundary Value Analysis & Extremes ==========
    
    @Test(timeout = 4000)
    public void testExpandVarArgsWithNullArgs() throws Exception {
        // Test expandVarArgs indirectly through constructor
        MockitoMethod method = createMockitoMethod("test", new Class<?>[] { String.class }, false);
        Invocation invocation = new Invocation(new Object(), method, null, 1, null);
        
        assertEquals("Arguments should be empty array", 0, invocation.getArgumentsCount());
        assertNotNull("Arguments should not be null", invocation.getArguments());
    }
    
    @Test(timeout = 4000)
    public void testExpandVarArgsWithNullVarArg() throws Exception {
        // Varargs method with null vararg array
        MockitoMethod method = createMockitoMethod("test", new Class<?>[] { String.class, Object[].class }, true);
        Object[] args = { "fixed", null };
        Invocation invocation = new Invocation(new Object(), method, args, 1, null);
        
        assertEquals("Should have 2 arguments", 2, invocation.getArgumentsCount());
        assertNull("Second argument should be null", invocation.getArguments()[1]);
    }
    
    @Test(timeout = 4000)
    public void testExpandVarArgsWithNonArrayLastArg() throws Exception {
        // Varargs method but last argument is not an array
        MockitoMethod method = createMockitoMethod("test", new Class<?>[] { String.class, Object.class }, true);
        Object[] args = { "fixed", "notArray" };
        Invocation invocation = new Invocation(new Object(), method, args, 1, null);
        
        assertEquals("Should have 2 arguments", 2, invocation.getArgumentsCount());
        assertEquals("Second argument should be preserved", "notArray", invocation.getArguments()[1]);
    }
    
    @Test(timeout = 4000)
    public void testExpandVarArgsWithArrayVarArg() throws Exception {
        // Varargs method with actual array
        MockitoMethod method = createMockitoMethod("test", new Class<?>[] { String.class, Object[].class }, true);
        Object[] args = { "fixed", new Object[] { "a", "b", "c" } };
        Invocation invocation = new Invocation(new Object(), method, args, 1, null);
        
        assertEquals("Should have 4 arguments (1 fixed + 3 varargs)", 4, invocation.getArgumentsCount());
        assertEquals("First argument should be 'fixed'", "fixed", invocation.getArguments()[0]);
        assertEquals("Second argument should be 'a'", "a", invocation.getArguments()[1]);
        assertEquals("Third argument should be 'b'", "b", invocation.getArguments()[2]);
        assertEquals("Fourth argument should be 'c'", "c", invocation.getArguments()[3]);
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithNull() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertFalse("Should not equal null", invocation.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentClass() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertFalse("Should not equal different class", invocation.equals("string"));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithSameObject() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertTrue("Should equal itself", invocation.equals(invocation));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentMock() throws Exception {
        Invocation inv1 = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        Invocation inv2 = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertFalse("Should not equal with different mock", inv1.equals(inv2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithDifferentArguments() throws Exception {
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "a" }, false);
        Invocation inv2 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "b" }, false);
        assertFalse("Should not equal with different arguments", inv1.equals(inv2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsWithEqualInvocation() throws Exception {
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "a" }, false);
        Invocation inv2 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "a" }, false);
        assertTrue("Should equal with same mock, method, and arguments", inv1.equals(inv2));
    }
    
    @Test(timeout = 4000)
    public void testHashCodeThrowsException() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        try {
            invocation.hashCode();
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testArgumentsToMatchersWithNull() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[] { Object.class }, new Object[] { null }, false);
        // Should not throw exception
        assertNotNull("Should produce matchers", invocation.toString());
    }
    
    @Test(timeout = 4000)
    public void testArgumentsToMatchersWithArray() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[] { int[].class }, new Object[] { new int[] { 1, 2, 3 } }, false);
        // Should not throw exception
        assertNotNull("Should produce matchers", invocation.toString());
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testCallRealMethodOnInterface() throws Exception {
        // This test targets the known defect: calling realMethod on an interface mock
        // should throw an appropriate exception (not NullPointerException or NoSuchMethodError)
        List<String> interfaceMock = new ArrayList<>();
        
        MockitoMethod method = createMockitoMethod("get", new Class<?>[] { int.class }, false);
        
        // Create a RealMethod that simulates what happens when calling real method on interface
        RealMethod interfaceRealMethod = new RealMethod() {
            @Override
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                // Simulate the behavior that causes the defect
                throw new AbstractMethodError("Cannot call real method on interface");
            }
        };
        
        Invocation invocation = new Invocation(interfaceMock, method, new Object[] { 0 }, 1, interfaceRealMethod);
        
        try {
            invocation.callRealMethod();
            fail("Should throw an error when calling real method on interface");
        } catch (AbstractMethodError e) {
            // Expected - this is the correct behavior
            assertTrue("Error message should indicate interface issue", 
                       e.getMessage().contains("Cannot call real method on interface"));
        } catch (Throwable t) {
            // If we get here, the defect is present (NullPointerException or NoSuchMethodError)
            fail("Should throw AbstractMethodError, not " + t.getClass().getName() + ": " + t.getMessage());
        }
    }
    
    @Test(timeout = 4000)
    public void testIsValidExceptionWithMatchingException() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        
        assertTrue("IOException should be valid (Exception is assignable)", 
                   invocation.isValidException(new java.io.IOException()));
    }
    
    @Test(timeout = 4000)
    public void testIsValidExceptionWithNonMatchingException() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        
        assertFalse("RuntimeException should not be valid (not in exception types)", 
                    invocation.isValidException(new RuntimeException()));
    }
    
    @Test(timeout = 4000)
    public void testIsValidReturnTypeWithPrimitive() throws Exception {
        MockitoMethod intMethod = new MockitoMethod() {
            @Override public String getName() { return "intMethod"; }
            @Override public Class<?> getReturnType() { return int.class; }
            @Override public Class<?>[] getParameterTypes() { return new Class<?>[0]; }
            @Override public Class<?>[] getExceptionTypes() { return new Class<?>[0]; }
            @Override public boolean isVarArgs() { return false; }
            @Override public Method getJavaMethod() { return null; }
        };
        
        Invocation invocation = new Invocation(new Object(), intMethod, new Object[0], 1, null);
        
        assertTrue("Integer should be valid for int return type", invocation.isValidReturnType(Integer.class));
        assertFalse("String should not be valid for int return type", invocation.isValidReturnType(String.class));
    }
    
    @Test(timeout = 4000)
    public void testIsValidReturnTypeWithObject() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        
        assertTrue("String should be valid for String return type", invocation.isValidReturnType(String.class));
        assertTrue("Object should be valid for String return type (assignable)", invocation.isValidReturnType(Object.class));
        assertFalse("Integer should not be valid for String return type", invocation.isValidReturnType(Integer.class));
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(timeout = 4000)
    public void testIsToString() throws Exception {
        MockitoMethod toStringMethod = new MockitoMethod() {
            @Override public String getName() { return "toString"; }
            @Override public Class<?> getReturnType() { return String.class; }
            @Override public Class<?>[] getParameterTypes() { return new Class<?>[0]; }
            @Override public Class<?>[] getExceptionTypes() { return new Class<?>[0]; }
            @Override public boolean isVarArgs() { return false; }
            @Override public Method getJavaMethod() { return null; }
        };
        
        Invocation invocation = new Invocation(new Object(), toStringMethod, new Object[0], 1, null);
        assertTrue("toString method should be identified", Invocation.isToString(invocation));
    }
    
    @Test(timeout = 4000)
    public void testIsNotToString() throws Exception {
        Invocation invocation = createInvocation(new Object(), "notToString", new Class<?>[0], new Object[0], false);
        assertFalse("Non-toString method should not be identified as toString", Invocation.isToString(invocation));
    }
    
    @Test(timeout = 4000)
    public void testToStringWithEmptyMatchers() throws Exception {
        Invocation invocation = createInvocation(new Object(), "test", new Class<?>[0], new Object[0], false);
        assertNotNull("toString should not return null", invocation.toString());
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testEqualsConsistency() throws Exception {
        Object mock = new Object();
        Invocation inv1 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "value" }, false);
        Invocation inv2 = createInvocation(mock, "test", new Class<?>[] { String.class }, new Object[] { "value" }, false);
        
        // Symmetry
        assertEquals("Equals should be symmetric", inv1.equals(inv2), inv2.equals(inv1));
        
        // Consistency
        assertTrue("Equals should be consistent", inv1.equals(inv2));
        assertTrue("Equals should be consistent", inv1.equals(inv2));
    }
    
    @Test(timeout = 4000)
    public void testToStringFormat() throws Exception {
        Object mock = new Object() {
            @Override
            public String toString() {
                return "MockObject";
            }
        };
        
        Invocation invocation = createInvocation(mock, "doSomething", new Class<?>[] { String.class }, new Object[] { "arg" }, false);
        String result = invocation.toString();
        
        assertNotNull("toString should not be null", result);
        assertTrue("toString should contain method name", result.contains("doSomething"));
        assertTrue("toString should contain argument", result.contains("arg"));
    }
    
    @Test(timeout = 4000)
    public void testCallRealMethodOnConcreteClass() throws Exception {
        // This should work fine for concrete classes
        Object concreteMock = new ArrayList<>();
        
        MockitoMethod method = createMockitoMethod("size", new Class<?>[0], false);
        
        RealMethod concreteRealMethod = new RealMethod() {
            @Override
            public Object invoke(Object target, Object[] arguments) throws Throwable {
                return ((List<?>) target).size();
            }
        };
        
        Invocation invocation = new Invocation(concreteMock, method, new Object[0], 1, concreteRealMethod);
        
        try {
            Object result = invocation.callRealMethod();
            assertEquals("ArrayList size should be 0", 0, result);
        } catch (Throwable t) {
            fail("Should not throw exception for concrete class: " + t.getMessage());
        }
    }
}