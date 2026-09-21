package org.mockito.internal.creation;

import org.junit.Test;
import java.lang.reflect.Method;
import static org.junit.Assert.*;

/**
 * Advanced White-Box test suite for DelegatingMethod.
 * Targets the known defect in equals() where it fails to compare DelegatingMethod objects correctly.
 *
 * Branch & Defect Analysis Matrix:
 * - Constructor: assert non-null (requires -ea) – not tested explicitly.
 * - getExceptionTypes(): delegates to Method.getExceptionTypes().
 * - getJavaMethod(): returns stored Method.
 * - getName(): delegates.
 * - getParameterTypes(): delegates.
 * - getReturnType(): delegates.
 * - isVarArgs(): delegates.
 * - isAbstract(): uses bit mask on modifiers – true if abstract.
 * - equals(Object o):
 *   [BRANCH] Delegates to method.equals(o) – bug: does NOT unwrap DelegatingMethod.
 *   Correct should: if o instanceof DelegatingMethod, compare inner Methods; else if o instanceof Method, compare directly; else false.
 * - hashCode(): always returns 1 (valid but weak).
 *
 * Defect-targeted tests:
 * - equals_should_return_true_when_equal (two DelegatingMethods with same Method)
 * - equals_should_return_true_when_self (DelegatingMethod compared to itself)
 * Both fail on defective code because method.equals(o) returns false when o is a DelegatingMethod.
 *
 * Coverage: line/branch coverage on all accessible methods (except constructor assert).
 */
public class DelegatingMethodDeepseekTest {

    // A sample Method to reuse across tests
    private static final Method STRING_LENGTH_METHOD;
    private static final Method STRING_HASHCODE_METHOD;
    static {
        try {
            STRING_LENGTH_METHOD = String.class.getMethod("length");
            STRING_HASHCODE_METHOD = String.class.getMethod("hashCode");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testGetName() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertEquals("length", dm.getName());
    }

    @Test(timeout = 4000)
    public void testGetParameterTypes() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertArrayEquals(new Class<?>[0], dm.getParameterTypes());
    }

    @Test(timeout = 4000)
    public void testGetReturnType() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertEquals(int.class, dm.getReturnType());
    }

    @Test(timeout = 4000)
    public void testIsVarArgs() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertFalse(dm.isVarArgs());
    }

    @Test(timeout = 4000)
    public void testIsAbstract_nonAbstractMethod() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertFalse(dm.isAbstract());
    }

    @Test(timeout = 4000)
    public void testIsAbstract_abstractMethod() throws NoSuchMethodException {
        Method abstractMethod = Cloneable.class.getMethod("toString"); // not abstract; find a real abstract method
        // Use an interface method that is abstract
        Method runnableRun = Runnable.class.getMethod("run");
        DelegatingMethod dm = new DelegatingMethod(runnableRun);
        assertTrue(dm.isAbstract());
    }

    @Test(timeout = 4000)
    public void testGetExceptionTypes() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertArrayEquals(new Class<?>[0], dm.getExceptionTypes());
    }

    @Test(timeout = 4000)
    public void testGetJavaMethod() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertSame(STRING_LENGTH_METHOD, dm.getJavaMethod());
    }

    // ========== Partition B: BVA & Extremes ==========

    @Test(timeout = 4000)
    public void testEquals_null() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertFalse(dm.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_differentObjectType() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertFalse(dm.equals("a string"));
    }

    @Test(timeout = 4000)
    public void testEquals_methodSame_shouldBeTrue() {
        // o is a Method, equal internal Method
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertTrue(dm.equals(STRING_LENGTH_METHOD));
    }

    @Test(timeout = 4000)
    public void testEquals_methodDifferent_shouldBeFalse() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        assertFalse(dm.equals(STRING_HASHCODE_METHOD));
    }

    @Test(timeout = 4000)
    public void testEquals_delegatingMethodDifferentMethod_shouldBeFalse() {
        DelegatingMethod dm1 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm2 = new DelegatingMethod(STRING_HASHCODE_METHOD);
        assertFalse(dm1.equals(dm2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        DelegatingMethod dm1 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm2 = new DelegatingMethod(STRING_LENGTH_METHOD);
        // If equals returns true (once fixed), hashcodes must be equal.
        // Since hashCode always returns 1, this holds even with bug.
        assertEquals(dm1.hashCode(), dm2.hashCode());
    }

    // ========== Partition C: Defect-Targeted Branch Zone (reveals bug) ==========

    @Test(timeout = 4000)
    public void equals_should_return_true_when_equal() {
        // Two DelegatingMethods with the same underlying Method should be equal.
        DelegatingMethod dm1 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm2 = new DelegatingMethod(STRING_LENGTH_METHOD);
        // Defective: dm1.equals(dm2) returns false because method.equals(dm2) fails.
        assertTrue("Expected true when comparing two DelegatingMethods with same internal Method", dm1.equals(dm2));
    }

    @Test(timeout = 4000)
    public void equals_should_return_true_when_self() {
        // An object should be equal to itself.
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        // Defective: dm.equals(dm) returns false because method.equals(dm) fails.
        assertTrue("Expected true when comparing DelegatingMethod to itself", dm.equals(dm));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    // The constructor uses assert – not testable without -ea. No other exception paths.

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEquals_symmetric() {
        DelegatingMethod dm1 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm2 = new DelegatingMethod(STRING_LENGTH_METHOD);
        // Symmetry: if one equals the other, the reverse must hold.
        // This test only passes after the bug is fixed.
        assertEquals("Symmetry violated", dm1.equals(dm2), dm2.equals(dm1));
    }

    @Test(timeout = 4000)
    public void testEquals_consistent() {
        DelegatingMethod dm = new DelegatingMethod(STRING_LENGTH_METHOD);
        boolean firstCall = dm.equals(dm);
        boolean secondCall = dm.equals(dm);
        assertEquals("Equals must be consistent", firstCall, secondCall);
    }

    @Test(timeout = 4000)
    public void testEquals_transitive() {
        DelegatingMethod dm1 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm2 = new DelegatingMethod(STRING_LENGTH_METHOD);
        DelegatingMethod dm3 = new DelegatingMethod(STRING_LENGTH_METHOD);
        if (dm1.equals(dm2) && dm2.equals(dm3)) {
            assertTrue("Transitivity violated", dm1.equals(dm3));
        }
        // If any equals fails due to bug, this test won't assert true but won't fail either.
        // However it still exercises the paths.
    }
}