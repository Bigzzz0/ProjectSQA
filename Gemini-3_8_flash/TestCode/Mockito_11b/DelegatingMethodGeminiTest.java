package org.mockito.internal.creation;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.creation.DelegatingMethod
 *
 * Method Under Test        | Branch / Condition                   | Input Scenario                       | Expected Result
 * -------------------------+--------------------------------------+--------------------------------------+------------------------------
 * equals(Object) [DEFECT]  | o == this (Self reference)           | dm.equals(dm)                        | true (Fails in buggy code: method.equals(dm) -> false)
 * equals(Object) [DEFECT]  | o instanceof DelegatingMethod (Same) | dm1.equals(dm2) with identical method| true (Fails in buggy code: method.equals(dm2) -> false)
 * equals(Object)           | o instanceof DelegatingMethod (Diff) | dm1.equals(dm3) with diff method     | false
 * equals(Object)           | o instanceof Method                  | dm.equals(underlyingMethod)          | true (method.equals(method) -> true)
 * equals(Object)           | o == null                            | dm.equals(null)                      | false
 * equals(Object)           | o is unrelated type                  | dm.equals("string")                  | false
 * hashCode()               | Constant return value                | dm.hashCode()                        | 1
 * isAbstract()             | Modifier.ABSTRACT flag set           | Abstract method                      | true
 * isAbstract()             | Modifier.ABSTRACT flag clear         | Concrete method                      | false
 * isVarArgs()              | Method.isVarArgs() == true           | Varargs method                       | true
 * isVarArgs()              | Method.isVarArgs() == false          | Standard method                      | false
 * getJavaMethod()          | Field getter                         | Standard method                      | exact Method reference
 * getName()                | Forwarding call                      | Method name                          | exact method name
 * getReturnType()          | Forwarding call                      | Method return type                   | exact return Class
 * getParameterTypes()      | Forwarding call                      | Parameter types array                | exact parameter types
 * getExceptionTypes()      | Forwarding call                      | Declared exceptions                  | exact exception classes
 * ----------------------------------------------------------------------------------------------------
 */
public class DelegatingMethodGeminiTest {

    private static abstract class SampleFixture {
        public abstract void sampleAbstractMethod() throws IOException, ClassNotFoundException;
        public void sampleConcreteMethod(int a, String b) {}
        public void sampleVarArgsMethod(String... args) {}
        public int anotherMethod() { return 0; }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Inspection
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetJavaMethodReturnsOriginalMethod() throws Exception {
        Method method = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertSame(method, delegatingMethod.getJavaMethod());
    }

    @Test(timeout = 4000)
    public void testGetNameReturnsCorrectMethodName() throws Exception {
        Method method = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertEquals("sampleConcreteMethod", delegatingMethod.getName());
    }

    @Test(timeout = 4000)
    public void testGetReturnTypeMatchesUnderlyingMethod() throws Exception {
        Method voidMethod = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod dmVoid = new DelegatingMethod(voidMethod);
        assertEquals(void.class, dmVoid.getReturnType());

        Method intMethod = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod dmInt = new DelegatingMethod(intMethod);
        assertEquals(int.class, dmInt.getReturnType());
    }

    @Test(timeout = 4000)
    public void testGetParameterTypes() throws Exception {
        Method method = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        Class<?>[] paramTypes = delegatingMethod.getParameterTypes();
        assertNotNull(paramTypes);
        assertEquals(2, paramTypes.length);
        assertArrayEquals(new Class<?>[]{int.class, String.class}, paramTypes);
    }

    @Test(timeout = 4000)
    public void testGetExceptionTypes() throws Exception {
        Method method = SampleFixture.class.getMethod("sampleAbstractMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        Class<?>[] exceptionTypes = delegatingMethod.getExceptionTypes();
        assertNotNull(exceptionTypes);
        assertEquals(2, exceptionTypes.length);
        assertTrue(Arrays.asList(exceptionTypes).contains(IOException.class));
        assertTrue(Arrays.asList(exceptionTypes).contains(ClassNotFoundException.class));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (Modifiers & Flags)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsAbstractWhenAbstractMethod() throws Exception {
        Method abstractMethod = SampleFixture.class.getMethod("sampleAbstractMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(abstractMethod);

        assertTrue("Expected isAbstract() to return true for abstract method", delegatingMethod.isAbstract());
    }

    @Test(timeout = 4000)
    public void testIsAbstractWhenConcreteMethod() throws Exception {
        Method concreteMethod = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(concreteMethod);

        assertFalse("Expected isAbstract() to return false for concrete method", delegatingMethod.isAbstract());
    }

    @Test(timeout = 4000)
    public void testIsVarArgsWhenVarArgsMethod() throws Exception {
        Method varArgsMethod = SampleFixture.class.getMethod("sampleVarArgsMethod", String[].class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(varArgsMethod);

        assertTrue("Expected isVarArgs() to return true for varargs method", delegatingMethod.isVarArgs());
    }

    @Test(timeout = 4000)
    public void testIsVarArgsWhenNonVarArgsMethod() throws Exception {
        Method nonVarArgsMethod = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);
        DelegatingMethod delegatingMethod = new DelegatingMethod(nonVarArgsMethod);

        assertFalse("Expected isVarArgs() to return false for non-varargs method", delegatingMethod.isVarArgs());
    }

    @Test(timeout = 4000)
    public void testEmptyParametersAndExceptions() throws Exception {
        Method noArgMethod = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(noArgMethod);

        assertEquals(0, delegatingMethod.getParameterTypes().length);
        assertEquals(0, delegatingMethod.getExceptionTypes().length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets defect: DelegatingMethod.equals(this) must evaluate to true.
     * Buggy implementation executes: method.equals(this), which returns false.
     */
    @Test(timeout = 4000)
    public void testEqualsShouldReturnTrueWhenSelf() throws Exception {
        Method method = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertTrue("DelegatingMethod must equal itself", delegatingMethod.equals(delegatingMethod));
    }

    /**
     * Targets defect: Two distinct DelegatingMethod instances wrapping the same
     * Method must evaluate to equal.
     * Buggy implementation executes: method.equals(otherDelegatingMethod), which returns false.
     */
    @Test(timeout = 4000)
    public void testEqualsShouldReturnTrueWhenEqual() throws Exception {
        Method method1 = SampleFixture.class.getMethod("anotherMethod");
        Method method2 = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod dm1 = new DelegatingMethod(method1);
        DelegatingMethod dm2 = new DelegatingMethod(method2);

        assertTrue("DelegatingMethod instances wrapping the same Method must be equal", dm1.equals(dm2));
        assertTrue("Symmetry requirement: dm2 must also equal dm1", dm2.equals(dm1));
    }

    @Test(timeout = 4000)
    public void testEqualsShouldReturnFalseWhenMethodsDiffer() throws Exception {
        Method method1 = SampleFixture.class.getMethod("anotherMethod");
        Method method2 = SampleFixture.class.getMethod("sampleAbstractMethod");
        DelegatingMethod dm1 = new DelegatingMethod(method1);
        DelegatingMethod dm2 = new DelegatingMethod(method2);

        assertFalse("DelegatingMethods wrapping different methods must not be equal", dm1.equals(dm2));
    }

    @Test(timeout = 4000)
    public void testEqualsDirectMethodComparison() throws Exception {
        Method method = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertTrue("DelegatingMethod should equal underlying java.lang.reflect.Method per Javadoc specification",
                delegatingMethod.equals(method));
    }

    // =========================================================================
    // Partition D: Object Lifecycle, Contract Integrity & Boundaries
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsWithNull() throws Exception {
        Method method = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertFalse("DelegatingMethod must not equal null", delegatingMethod.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithUnrelatedType() throws Exception {
        Method method = SampleFixture.class.getMethod("anotherMethod");
        DelegatingMethod delegatingMethod = new DelegatingMethod(method);

        assertFalse("DelegatingMethod must not equal an unrelated type instance", delegatingMethod.equals("arbitrary_string"));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() throws Exception {
        Method method1 = SampleFixture.class.getMethod("anotherMethod");
        Method method2 = SampleFixture.class.getMethod("sampleConcreteMethod", int.class, String.class);

        DelegatingMethod dm1 = new DelegatingMethod(method1);
        DelegatingMethod dm2 = new DelegatingMethod(method2);

        assertEquals("DelegatingMethod hashCode() contract specifies constant 1", 1, dm1.hashCode());
        assertEquals("DelegatingMethod hashCode() contract specifies constant 1", 1, dm2.hashCode());
        assertEquals("Equal hashCodes for both instances", dm1.hashCode(), dm2.hashCode());
    }
}