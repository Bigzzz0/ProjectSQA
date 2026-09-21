package org.mockito.internal.creation.instance;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 - newInstance: if outerClassInstance == null -> noArgConstructor; else -> withOuterClass.
 - noArgConstructor: try cls.newInstance() success/failure.
 - withOuterClass: try getDeclaredConstructor(outerClassInstance.getClass()) and c.newInstance(outerClassInstance) success/failure.
 - Defect: Inner class (non-static) creation fails when outerClassInstance is provided because reflection does not call setAccessible(true) on the constructor, causing IllegalAccessException to be wrapped in InstantationException.
 - Boundary: null outer, non-null outer; valid vs missing constructor; public vs non-public constructor.
*/

public class ConstructorInstantiatorDeepseekTest {

    // Helper classes for testing various constructor scenarios
    static class Outer {
        class Inner {
            // implicit package-private constructor taking Outer
        }
    }

    static class HasOuterConstructor {
        public HasOuterConstructor(Outer outer) {
            // public constructor with Outer parameter
        }
    }

    // -------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testNewInstance_withNullOuterInstance_usesNoArgConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        String instance = instantiator.newInstance(String.class);
        assertNotNull("Instance should not be null", instance);
        assertTrue("Instance should be String", instance instanceof String);
    }

    @Test(timeout = 4000)
    public void testNewInstance_withOuterInstance_usesConstructorWithOuterType() {
        Outer outer = new Outer();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        HasOuterConstructor instance = instantiator.newInstance(HasOuterConstructor.class);
        assertNotNull("Instance should not be null", instance);
        assertTrue("Instance should be HasOuterConstructor", instance instanceof HasOuterConstructor);
    }

    // -------- Partition B: Boundary & Error Paths ----------

    @Test(timeout = 4000)
    public void testNewInstance_withNullOuterInstance_throwsWhenNoNoArgConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(Integer.class);
            fail("Expected InstantationException for class without no-arg constructor");
        } catch (InstantationException e) {
            assertEquals("Message should indicate missing no-arg constructor",
                    "Unable to create mock instance of 'Integer'.\nPlease ensure it has parameter-less constructor.",
                    e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNewInstance_withOuterInstance_throwsWhenNoConstructorWithOuterType() {
        Outer outer = new Outer();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        try {
            instantiator.newInstance(String.class);
            fail("Expected InstantationException for missing constructor with outer type");
        } catch (InstantationException e) {
            assertTrue("Message should contain outer instance hint",
                    e.getMessage().contains("Please ensure that the outer instance has correct type"));
            assertTrue("Message should contain target class name",
                    e.getMessage().contains("String"));
        }
    }

    // -------- Partition C: Defect-Targeted Branch Zone ----------

    // This test targets the specific defect: inner class creation should succeed,
    // but on the defective implementation it fails because the constructor is not made accessible.
    @Test(timeout = 4000)
    public void testNewInstance_withOuterInstance_createsInnerClass() {
        Outer outer = new Outer();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);
        Outer.Inner inner = instantiator.newInstance(Outer.Inner.class);
        assertNotNull("Inner class instance should be created", inner);
        assertTrue("Instance should be of type Outer.Inner", inner instanceof Outer.Inner);
    }

    // -------- Partition D: Defensive Guards & Edge Cases ----------

    @Test(timeout = 4000)
    public void testNewInstance_withNullOuterInstance_throwsForInnerClass() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(Outer.Inner.class);
            fail("Expected InstantationException for inner class without outer instance");
        } catch (InstantationException e) {
            assertTrue("Message should contain inner class simple name",
                    e.getMessage().contains("Inner"));
            assertTrue("Message should mention parameter-less constructor",
                    e.getMessage().contains("parameter-less constructor"));
        }
    }
}