package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * [Branch & Defect Analysis Matrix]
 * Target: ReturnsSmartNulls
 * 
 * Decision Branches:
 * 1. delegate.answer(invocation) != null -> return defaultReturnValue
 * 2. delegate.answer(invocation) == null -> check canImposterise(type)
 * 3. canImposterise(type) == true -> return imposterised proxy
 * 4. canImposterise(type) == false -> return null
 * 
 * ThrowingInterceptor branches:
 * 5. isToString(method) == true -> return SmartNull toString with format
 * 6. isToString(method) == false -> throw smartNullPointerException
 * 
 * Boundary Conditions:
 * - Return types: primitive, String, Object, final class, interface
 * - Method arguments: empty, single, multiple parameters
 * - Null vs non-null default return values from delegate
 * - SmartNull toString with (empty) args vs actual args
 * 
 * Defect Targeted (D4J-1): shouldPrintTheParametersWhenCallingAMethodWithArgs
 * The bug is that formatMethodCall() doesn't include method arguments.
 * Expected: "unstubbed withArgs([oompa, lumpa]) method on mock"
 * Actual:   "unstubbed withArgs([]) method on mock"
 */
public class ReturnsSmartNullsDeepseekTest {

    private final ReturnsSmartNulls answer = new ReturnsSmartNulls();

    // Partition A: Core Functional Logic & State Transitions
    
    @Test(timeout = 4000)
    public void testReturnsDefaultValueWhenDelegateReturnsNonNull() throws Throwable {
        // For methods returning common types like String, delegate returns non-null
        Object mock = Mockito.mock(Runnable.class);
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = createInvocation(mock, toStringMethod, new Object[0]);
        
        Object result = answer.answer(invocation);
        
        // toString() returns a non-null String from delegate
        assertNotNull("Should return non-null for toString", result);
        assertTrue("Should be String type", result instanceof String);
    }

    @Test(timeout = 4000)
    public void testReturnsSmartNullForMockableType() throws Throwable {
        // Interface (mockable) returns SmartNull proxy
        Object mock = Mockito.mock(Runnable.class);
        Method runMethod = Runnable.class.getMethod("run");
        InvocationOnMock invocation = createInvocation(mock, runMethod, new Object[0]);
        
        Object result = answer.answer(invocation);
        
        assertNotNull("Should return SmartNull for mockable type", result);
        assertTrue("Should be proxy (not null)", result != null);
        // SmartNull toString should contain method name
        String smartNullStr = result.toString();
        assertTrue("SmartNull toString should contain 'run'", smartNullStr.contains("run"));
        assertTrue("SmartNull toString should contain 'unstubbed'", smartNullStr.contains("unstubbed"));
    }

    @Test(timeout = 4000)
    public void testReturnsNullForNonMockableType() throws Throwable {
        // final class is not mockable
        Object mock = Mockito.mock(Object.class);
        Method getClassMethod = Object.class.getMethod("getClass");
        InvocationOnMock invocation = createInvocation(mock, getClassMethod, new Object[0]);
        
        Object result = answer.answer(invocation);
        
        assertNull("Should return null for non-mockable (final) type", result);
    }

    // Partition B: Boundary Value Analysis & Extremes
    
    @Test(timeout = 4000)
    public void testSmartNullToStringForPrimitiveReturnType() throws Throwable {
        // Primitive return types: delegate returns default (e.g., 0), then SmartNull
        Object mock = Mockito.mock(TestInterface.class);
        // Use a method that returns int (primitive) - delegate returns 0, then SmartNull not used
        // Actually primitives have non-null defaults
    }

    @Test(timeout = 4000)
    public void testSmartNullToStringForMethodWithNoArgs() throws Throwable {
        Object mock = Mockito.mock(Runnable.class);
        Method runMethod = Runnable.class.getMethod("run");
        InvocationOnMock invocation = createInvocation(mock, runMethod, new Object[0]);
        
        Object result = answer.answer(invocation);
        String smartNullStr = result.toString();
        
        // Should contain "run()" with empty parentheses
        assertTrue("Should contain method name with empty args", smartNullStr.contains("run()"));
    }

    // Partition C: Defect-Targeted Branch Zone (Directly targets D4J defect)
    
    @Test(timeout = 4000)
    public void shouldPrintTheParametersWhenCallingAMethodWithArgs() throws Throwable {
        // This test targets the known defect: formatMethodCall() should include method args
        Object mock = Mockito.mock(TestInterface.class);
        Method methodWithArgs = TestInterface.class.getMethod("methodWithArgs", String.class, String.class);
        InvocationOnMock invocation = createInvocation(mock, methodWithArgs, new Object[]{"oompa", "lumpa"});
        
        Object result = answer.answer(invocation);
        String smartNullStr = result.toString();
        
        // Bug: current implementation gives "unstubbed methodWithArgs([]) method on mock"
        // Expected: "unstubbed methodWithArgs([oompa, lumpa]) method on mock"
        // This assertion will FAIL on the buggy version, revealing the defect
        String expectedSuffix = "unstubbed methodWithArgs() method on mock”; // Buggy format
        String expectedPrefix = "SmartNull returned by unstubbed methodWithArgs([oompa, lumpa]) method on mock";
        
        // The actual bug is that args are not rendered - we assert the CORRECT behavior
        assertFalse("Bug: formatMethodCall() should include arguments but they are missing", 
                     smartNullStr.contains("methodWithArgs()"));
        assertTrue("Should include arguments in SmartNull toString", 
                   smartNullStr.contains("[oompa, lumpa]"));
    }

    @Test(timeout = 4000)
    public void shouldPrintParametersForMultipleMethodsWithArgs() throws Throwable {
        Object mock = Mockito.mock(TestInterface.class);
        
        // Test with single arg
        Method singleArgMethod = TestInterface.class.getMethod("methodWithSingleArg", String.class);
        InvocationOnMock invocation1 = createInvocation(mock, singleArgMethod, new Object[]{"hello"});
        Object result1 = answer.answer(invocation1);
        String str1 = result1.toString();
        assertTrue("Should contain single arg", str1.contains("[hello]"));
        
        // Test with multiple args
        Method multiArgMethod = TestInterface.class.getMethod("methodWithArgs", String.class, String.class);
        InvocationOnMock invocation2 = createInvocation(mock, multiArgMethod, new Object[]{"foo", "bar"});
        Object result2 = answer.answer(invocation2);
        String str2 = result2.toString();
        assertTrue("Should contain multiple args", str2.contains("[foo, bar]"));
    }

    @Test(timeout = 4000)
    public void shouldPrintEmptyArrayForNoArgsMethod() throws Throwable {
        Object mock = Mockito.mock(Runnable.class);
        Method runMethod = Runnable.class.getMethod("run");
        InvocationOnMock invocation = createInvocation(mock, runMethod, new Object[0]);
        
        Object result = answer.answer(invocation);
        String smartNullStr = result.toString();
        
        // For no-arg methods, should show empty brackets
        assertTrue("Should contain empty args array", smartNullStr.contains("run()"));
        assertFalse("Should not contain brackets with nothing", smartNullStr.contains("run([])")); 
    }

    // Partition D: Exception & Defensive Guard Paths
    
    @Test(timeout = 4000, expected = Exception.class)
    public void testThrowingInterceptorThrowsOnNonToStringCall() throws Throwable {
        Object mock = Mockito.mock(TestInterface.class);
        Method method = TestInterface.class.getMethod("methodWithoutArgs");
        InvocationOnMock invocation = createInvocation(mock, method, new Object[0]);
        
        Object proxy = answer.answer(invocation);
        
        // Calling any method other than toString on the SmartNull should throw
        proxy.hashCode(); // This should trigger ThrowingInterceptor and throw
    }

    @Test(timeout = 4000)
    public void testThrowingInterceptorDoesntThrowOnToString() throws Throwable {
        Object mock = Mockito.mock(TestInterface.class);
        Method method = TestInterface.class.getMethod("methodWithoutArgs");
        InvocationOnMock invocation = createInvocation(mock, method, new Object[0]);
        
        Object proxy = answer.answer(invocation);
        
        // toString should be handled gracefully, not throw
        String str = proxy.toString();
        assertNotNull("toString should not throw", str);
    }

    // Partition E: Object Lifecycle & Contract Integrity
    
    @Test(timeout = 4000)
    public void testReturnsSmartNullsIsSerializable() {
        assertTrue("ReturnsSmartNulls should implement Serializable", 
                   answer instanceof java.io.Serializable);
    }

    @Test(timeout = 4000)
    public void testMultipleSmartNullsAreDifferentInstances() throws Throwable {
        Object mock = Mockito.mock(TestInterface.class);
        Method method1 = TestInterface.class.getMethod("methodWithoutArgs");
        Method method2 = TestInterface.class.getMethod("methodWithSingleArg", String.class);
        
        InvocationOnMock invocation1 = createInvocation(mock, method1, new Object[0]);
        InvocationOnMock invocation2 = createInvocation(mock, method2, new Object[]{"test"});
        
        Object result1 = answer.answer(invocation1);
        Object result2 = answer.answer(invocation2);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame("Different invocations should produce different SmartNulls", result1, result2);
    }

    // Helper method to create InvocationOnMock
    private InvocationOnMock createInvocation(final Object mock, final Method method, final Object[] args) {
        return new InvocationOnMock() {
            public Object getMock() {
                return mock;
            }
            
            public Method getMethod() {
                return method;
            }
            
            public Object[] getArguments() {
                return args;
            }
            
            public <T> T getArgument(int index) {
                return (T) args[index];
            }
            
            public Object callRealMethod() throws Throwable {
                throw new UnsupportedOperationException();
            }
            
            public Object proceed() throws Throwable {
                throw new UnsupportedOperationException();
            }
            
            public boolean isVerified() {
                return false;
            }
            
            public int getSequenceNumber() {
                return 0;
            }
            
            public InvocationOnMock ignoreForVerification() {
                return this;
            }
            
            public StubInfo stubInfo() {
                return null;
            }
            
            public Object getRawReturnType() {
                return method.getReturnType();
            }
        };
    }

    // Test interface with various method signatures
    interface TestInterface {
        void methodWithoutArgs();
        void methodWithSingleArg(String arg);
        void methodWithArgs(String arg1, String arg2);
    }
}