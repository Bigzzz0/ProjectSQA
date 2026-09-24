package org.mockito.internal.creation.instance;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mockito.internal.creation.instance.ConstructorInstantiator
 *
 * Decision / Condition Matrix:
 * 1. Branch: `outerClassInstance == null`
 *    - TRUE: Diverts to `noArgConstructor(cls)`
 *      * Path T1 (Success): Target class possesses accessible 0-arg constructor -> Instantiates instance.
 *      * Path T2 (Failure): Target class lacks 0-arg constructor (NoSuchMethodException / InstantiationException)
 *                           -> Throws InstantationException with message indicating parameter-less constructor required.
 *      * Path T3 (Failure): Target class is abstract or interface -> Throws InstantationException.
 *      * Path T4 (Failure): Target constructor throws an uncaught exception -> Caught and wrapped into InstantationException.
 *    - FALSE: Diverts to `withOuterClass(cls)`
 *      * Path F1 (Success - Exact Enclosing Type): Constructor matches `outerClassInstance.getClass()` -> Instantiates.
 *      * Path F2 (Defect - Subclass Enclosing Type): Constructor expects `SuperOuter` but `outerClassInstance` is `SubOuter`.
 *                  Defective implementation calls `cls.getDeclaredConstructor(outerClassInstance.getClass())`
 *                  which throws NoSuchMethodException on subclass outer instances.
 *                  Fixed implementation resolves assignable constructors -> TARGET DEFECT TEST.
 *      * Path F3 (Failure - Incompatible Outer Type): Target constructor does not accept given outer instance type -> Throws InstantationException.
 *      * Path F4 (Failure - Target Has No Outer Param): Class is top-level / static and has no constructor for outer instance -> Throws InstantationException.
 *      * Path F5 (Failure - Inner Constructor Throws): Inner class constructor throws an exception -> Caught and wrapped.
 *
 * Defects4J Ground Truth Target:
 * - Test `creates_instances_of_inner_classes` fails with `InstantationException: Unable to create mock instance of 'SomeInnerClass'`
 *   when outerClassInstance is a subclass of the declared enclosing class.
 * ====================================================================================================
 */
public class ConstructorInstantiatorGeminiTest {

    // -------------------------------------------------------------------------
    // Test Fixtures
    // -------------------------------------------------------------------------

    static class SomeOuterClass {
        class SomeInnerClass {
            SomeOuterClass getOuter() {
                return SomeOuterClass.this;
            }
        }
    }

    static class SomeChildOuterClass extends SomeOuterClass {
    }

    static class SomeGrandchildOuterClass extends SomeChildOuterClass {
    }

    static class UnrelatedOuterClass {
        class UnrelatedInnerClass {
        }
    }

    static class OuterWithThrowingInner {
        class ThrowingInner {
            ThrowingInner() {
                throw new IllegalStateException("Inner constructor error simulation");
            }
        }
    }

    public static class SimpleZeroArgClass {
        public SimpleZeroArgClass() {
        }
    }

    public static class ClassWithoutZeroArgConstructor {
        public ClassWithoutZeroArgConstructor(String requiredArg) {
        }
    }

    public static abstract class AbstractSampleClass {
        public AbstractSampleClass() {
        }
    }

    public interface SampleInterface {
    }

    public static class ClassThrowingInConstructor {
        public ClassThrowingInConstructor() {
            throw new ArithmeticException("Simulated constructor exception");
        }
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void newInstance_withNullOuter_instantiatesPublicNoArgConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        SimpleZeroArgClass instance = instantiator.newInstance(SimpleZeroArgClass.class);

        assertNotNull("Instance should not be null", instance);
        assertEquals(SimpleZeroArgClass.class, instance.getClass());
    }

    @Test(timeout = 4000)
    public void newInstance_withExactOuterInstance_instantiatesInnerClass() {
        SomeOuterClass outer = new SomeOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);

        SomeOuterClass.SomeInnerClass inner = instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);

        assertNotNull("Inner instance should not be null", inner);
        assertEquals(SomeOuterClass.SomeInnerClass.class, inner.getClass());
        assertSame("Inner instance must retain reference to the enclosing outer instance", outer, inner.getOuter());
    }

    @Test(timeout = 4000)
    public void instantiator_implementsInstantiatorInterface() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        assertTrue("Must implement Instantiator SPI", instantiator instanceof Instantiator);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void newInstance_calledMultipleTimes_createsDistinctInstances() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);

        SimpleZeroArgClass first = instantiator.newInstance(SimpleZeroArgClass.class);
        SimpleZeroArgClass second = instantiator.newInstance(SimpleZeroArgClass.class);

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame("Consecutive invocations must create new distinct instances", first, second);
    }

    @Test(timeout = 4000)
    public void newInstance_withOuterInstance_calledMultipleTimes_createsDistinctInstances() {
        SomeOuterClass outer = new SomeOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);

        SomeOuterClass.SomeInnerClass first = instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);
        SomeOuterClass.SomeInnerClass second = instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame("Consecutive invocations must create new distinct inner instances", first, second);
        assertSame("Both instances must share the same enclosing outer reference", outer, first.getOuter());
        assertSame("Both instances must share the same enclosing outer reference", outer, second.getOuter());
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets the defect where ConstructorInstantiator uses exact type matching
     * (cls.getDeclaredConstructor(outerClassInstance.getClass())) rather than
     * accommodating subclasses of the outer class.
     */
    @Test(timeout = 4000)
    public void creates_instances_of_inner_classes() {
        SomeChildOuterClass childOuter = new SomeChildOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(childOuter);

        SomeOuterClass.SomeInnerClass inner = instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);

        assertNotNull("Inner class instance should be created using outer subclass instance", inner);
        assertEquals(SomeOuterClass.SomeInnerClass.class, inner.getClass());
        assertSame("Enclosing instance should match the provided child outer instance", childOuter, inner.getOuter());
    }

    @Test(timeout = 4000)
    public void creates_instances_of_inner_classes_with_multilevel_subclass() {
        SomeGrandchildOuterClass grandchildOuter = new SomeGrandchildOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(grandchildOuter);

        SomeOuterClass.SomeInnerClass inner = instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);

        assertNotNull("Inner class instance should be created using deeply nested subclass outer instance", inner);
        assertEquals(SomeOuterClass.SomeInnerClass.class, inner.getClass());
        assertSame(grandchildOuter, inner.getOuter());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void newInstance_nullOuter_throwsWhenNoParameterlessConstructor() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ClassWithoutZeroArgConstructor.class);
            fail("Expected InstantationException due to missing 0-arg constructor");
        } catch (InstantationException e) {
            assertTrue("Message should contain target simple class name",
                    e.getMessage().contains("ClassWithoutZeroArgConstructor"));
            assertTrue("Message should guide user regarding parameter-less constructor",
                    e.getMessage().contains("parameter-less constructor"));
            assertNotNull("Exception cause must be preserved", e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void newInstance_nullOuter_throwsWhenTargetIsAbstract() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(AbstractSampleClass.class);
            fail("Expected InstantationException when instantiating abstract class");
        } catch (InstantationException e) {
            assertTrue("Message should contain target simple class name",
                    e.getMessage().contains("AbstractSampleClass"));
            assertTrue("Message should indicate parameter-less constructor requirement",
                    e.getMessage().contains("parameter-less constructor"));
            assertNotNull("Exception cause must be preserved", e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void newInstance_nullOuter_throwsWhenTargetIsInterface() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(SampleInterface.class);
            fail("Expected InstantationException when instantiating interface");
        } catch (InstantationException e) {
            assertTrue("Message should contain target simple class name",
                    e.getMessage().contains("SampleInterface"));
            assertNotNull("Exception cause must be preserved", e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void newInstance_nullOuter_throwsWhenConstructorFails() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(ClassThrowingInConstructor.class);
            fail("Expected InstantationException when constructor body throws exception");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("ClassThrowingInConstructor"));
            assertNotNull("Cause must be preserved", e.getCause());
            assertTrue("Underlying failure should be preserved or wrapped",
                    e.getCause() instanceof ArithmeticException || e.getCause() instanceof InvocationTargetException);
        }
    }

    @Test(timeout = 4000)
    public void newInstance_withOuter_throwsWhenOuterInstanceIsIncompatible() {
        UnrelatedOuterClass unrelatedOuter = new UnrelatedOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(unrelatedOuter);

        try {
            instantiator.newInstance(SomeOuterClass.SomeInnerClass.class);
            fail("Expected InstantationException when outer instance type is completely incompatible");
        } catch (InstantationException e) {
            assertTrue("Message should contain target simple class name",
                    e.getMessage().contains("SomeInnerClass"));
            assertTrue("Message should guide user regarding outer instance type",
                    e.getMessage().contains("outer instance has correct type"));
            assertNotNull("Exception cause must be preserved", e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void newInstance_withOuter_throwsWhenTargetClassHasNoOuterConstructor() {
        SomeOuterClass outer = new SomeOuterClass();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);

        try {
            instantiator.newInstance(SimpleZeroArgClass.class);
            fail("Expected InstantationException when target class does not accept outer class");
        } catch (InstantationException e) {
            assertTrue("Message should contain target simple class name",
                    e.getMessage().contains("SimpleZeroArgClass"));
            assertTrue("Message should guide user regarding outer instance type",
                    e.getMessage().contains("outer instance has correct type"));
            assertNotNull("Exception cause must be preserved", e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void newInstance_withOuter_throwsWhenInnerConstructorFails() {
        OuterWithThrowingInner outer = new OuterWithThrowingInner();
        ConstructorInstantiator instantiator = new ConstructorInstantiator(outer);

        try {
            instantiator.newInstance(OuterWithThrowingInner.ThrowingInner.class);
            fail("Expected InstantationException when inner class constructor throws");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("ThrowingInner"));
            assertNotNull("Exception cause must be preserved", e.getCause());
            assertTrue("Cause should wrap InvocationTargetException",
                    e.getCause() instanceof InvocationTargetException);
            assertEquals("Underlying exception must be preserved",
                    IllegalStateException.class,
                    ((InvocationTargetException) e.getCause()).getTargetException().getClass());
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void newInstance_withPrimitiveWrapperTargetClass() {
        ConstructorInstantiator instantiator = new ConstructorInstantiator(null);
        try {
            instantiator.newInstance(Integer.class);
            fail("Expected InstantationException since Integer has no 0-arg constructor");
        } catch (InstantationException e) {
            assertTrue(e.getMessage().contains("Integer"));
            assertNotNull(e.getCause());
        }
    }
}