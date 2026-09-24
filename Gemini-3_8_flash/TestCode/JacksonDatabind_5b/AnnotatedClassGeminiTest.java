package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector.MixInResolver;

/* [Branch & Defect Analysis Matrix]
 *
 * 1. Branch Coverage Targets:
 *   - construct vs constructWithoutSuperTypes (super type inheritance traversal)
 *   - resolveClassAnnotations:
 *       - _annotationIntrospector == null (early skip) vs non-null
 *       - _primaryMixIn != null vs null
 *       - superTypes iteration with mixins
 *       - Object.class mixin handling
 *   - resolveCreators:
 *       - constructors: 0-arg default vs multi-arg
 *       - inner member class parameter count mismatch (synthetic outer 'this')
 *       - enum constructor parameter count mismatch (+2 synthetic params)
 *       - ignore marker filtering for defaultConstructor and constructors list
 *       - static factory creator methods resolution & ignore marker filtering
 *       - constructor / factory mix-ins
 *   - resolveMemberMethods:
 *       - static vs non-static (static filtered out)
 *       - bridge/synthetic method filtering
 *       - parameter count > 2 filtering
 *       - interface vs class method overriding (JACKSON-450)
 *       - mixin method overriding / augmenting
 *       - mixins for Object.class (e.g. Object#hashCode)
 *   - resolveFields:
 *       - static vs transient vs synthetic filtering
 *       - inheritance of fields from superclasses
 *       - field mixin overrides
 *   - Annotation bundles resolution: isAnnotationBundle true/false recursion
 *   - withAnnotations() factory method
 *
 * 2. Defect Analysis (Jackson-databind Issue #515):
 *   - Target: Multi-level mixin merging on member methods (`_addMethodMixIns` / `_addMemberMethods`).
 *   - In issue #515, when mixins are defined across a hierarchy or multi-level mix-in interfaces,
 *     mixIn methods collected into the temporary `mixIns` map could fail to merge properly
 *     or get overwritten when resolving member methods for subclasses/implementations.
 *   - Specifically, `mixIns.add(_constructMethod(m))` in `_addMethodMixIns` overrides any existing
 *     annotations for the same method signature previously recorded in higher-level mixins instead of
 *     augmenting/preserving them.
 */
public class AnnotatedClassGeminiTest {

    // -------------------------------------------------------------------------
    // Test Annotations & Target Fixture Classes
    // -------------------------------------------------------------------------

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarkerA {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarkerB {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface MarkerC {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface BundleAnn {}

    @MarkerA("bundled")
    @Retention(RetentionPolicy.RUNTIME)
    public @interface BundleContainer {}

    public static class SimpleMixInResolver implements MixInResolver {
        private final java.util.Map<Class<?>, Class<?>> _mappings = new java.util.HashMap<Class<?>, Class<?>>();

        public SimpleMixInResolver add(Class<?> target, Class<?> mixin) {
            _mappings.put(target, mixin);
            return this;
        }

        @Override
        public Class<?> findMixInClassFor(Class<?> cls) {
            return _mappings.get(cls);
        }

        @Override
        public MixInResolver copy() {
            return this;
        }
    }

    public static class SimpleIntrospector extends AnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public com.fasterxml.jackson.core.Version version() {
            return com.fasterxml.jackson.core.Version.unknownVersion();
        }

        @Override
        public boolean isAnnotationBundle(Annotation ann) {
            return ann.annotationType() == BundleContainer.class;
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return m.hasAnnotation(MarkerC.class);
        }
    }

    // Fixture for Defect targeting Issue #515 (Multi-level Mixin Merging)
    public interface Person {
        String getName();
        String getCity();
    }

    public static class PersonImpl implements Person {
        @Override
        public String getName() { return "Bob"; }
        @Override
        public String getCity() { return "Seattle"; }
    }

    public interface PersonMixInTop {
        @MarkerA("topName")
        String getName();
    }

    public interface PersonMixInSub extends PersonMixInTop {
        @MarkerB("subCity")
        String getCity();
    }

    // Interface vs Implementation Fixtures
    public interface BaseInterface {
        @MarkerA("interfaceMethod")
        void action();
    }

    public static class BaseClass implements BaseInterface {
        public int superField = 10;
        @MarkerA("baseField")
        public int annotatedSuperField = 20;

        @Override
        public void action() {}

        public void methodTwoArgs(int a, int b) {}
        public void methodThreeArgs(int a, int b, int c) {}
    }

    @MarkerA("subClass")
    public static class SubClass extends BaseClass {
        public static int staticField = 1;
        public transient int transientField = 2;
        public int subField = 3;

        public SubClass() {}

        public SubClass(int a) {}

        @MarkerC // to be ignored by introspector
        public SubClass(String s) {}

        public static SubClass create(int a) { return new SubClass(a); }

        @MarkerC // static creator to be ignored
        public static SubClass ignoredCreator(String s) { return new SubClass(s); }

        public static void staticMethodNoArgs() {}

        @Override
        public void action() {}
    }

    public static class SubClassMixIn {
        @MarkerB("mixedField")
        public int subField;

        @MarkerB("mixedCtor")
        public SubClassMixIn(@MarkerB("param") int a) {}

        @MarkerB("mixedCreator")
        public static SubClass create(@MarkerB("creatorParam") int a) { return null; }

        @MarkerB("mixedAction")
        public void action() {}
    }

    // Inner class fixture to test synthetic 'this' parameter handling
    public class InnerClass {
        public InnerClass(@MarkerA("p1") String arg) {}
    }

    // Enum fixture to test synthetic name/ordinal constructor parameters
    public enum SampleEnum {
        A("first");
        SampleEnum(@MarkerA("enumParam") String desc) {}
    }

    public interface ObjectHashCodeMixIn {
        @MarkerA("mixedHashCode")
        int hashCode();
    }

    // -------------------------------------------------------------------------
    // PARTITION C: Defect-Targeted Branch Zone (Issue #515)
    // -------------------------------------------------------------------------

    /**
     * Targets Jackson-databind Issue #515 / TestMixinMerging::testDisappearingMixins515.
     * When a mix-in interface extends another mix-in interface, annotations from the
     * top mix-in interface must not be lost when methods are resolved.
     */
    @Test(timeout = 4000)
    public void testDisappearingMixins515MultiLevelInterface() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(PersonImpl.class, PersonMixInSub.class);

        AnnotatedClass ac = AnnotatedClass.construct(PersonImpl.class, aintr, resolver);

        // Find getName method which was annotated on PersonMixInTop
        AnnotatedMethod nameMethod = ac.findMethod("getName", new Class<?>[]{});
        assertNotNull("getName method should be found", nameMethod);
        MarkerA markerA = nameMethod.getAnnotation(MarkerA.class);
        assertNotNull("MarkerA annotation on PersonMixInTop.getName() was lost during multi-level mixin resolution!", markerA);
        assertEquals("topName", markerA.value());

        // Find getCity method which was annotated on PersonMixInSub
        AnnotatedMethod cityMethod = ac.findMethod("getCity", new Class<?>[]{});
        assertNotNull("getCity method should be found", cityMethod);
        MarkerB markerB = cityMethod.getAnnotation(MarkerB.class);
        assertNotNull("MarkerB annotation on PersonMixInSub.getCity() should be preserved", markerB);
        assertEquals("subCity", markerB.value());
    }

    // -------------------------------------------------------------------------
    // PARTITION A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testBasicClassIntrospectionAndGetters() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, null);

        assertEquals(SubClass.class, ac.getAnnotated());
        assertEquals(SubClass.class, ac.getRawType());
        assertEquals(SubClass.class, (Class<?>) ac.getGenericType());
        assertEquals(SubClass.class.getName(), ac.getName());
        assertEquals(SubClass.class.getModifiers(), ac.getModifiers());
        assertEquals("[AnnotedClass " + SubClass.class.getName() + "]", ac.toString());

        assertTrue(ac.hasAnnotations());
        assertEquals(1, ac.getAnnotations().size());
        assertNotNull(ac.getAnnotation(MarkerA.class));
        assertEquals("subClass", ac.getAnnotation(MarkerA.class).value());
        assertNull(ac.getAnnotation(MarkerB.class));

        int count = 0;
        for (Annotation ann : ac.annotations()) {
            count++;
        }
        assertEquals(1, count);
        assertNotNull(ac.getAllAnnotations());
    }

    @Test(timeout = 4000)
    public void testConstructWithoutSuperTypes() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SubClass.class, aintr, null);

        assertNotNull(ac);
        assertEquals(SubClass.class, ac.getAnnotated());
        // SubClass method action() exists; base methods from BaseClass will not be augmented via _superTypes
        assertNotNull(ac.findMethod("action", new Class<?>[]{}));
    }

    @Test(timeout = 4000)
    public void testCreatorsResolutionAndIgnoredMarkers() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, null);

        // Default constructor exists and not ignored
        AnnotatedConstructor defaultCtor = ac.getDefaultConstructor();
        assertNotNull(defaultCtor);
        assertEquals(0, defaultCtor.getParameterCount());

        // Single argument constructors: SubClass(int) should remain, SubClass(String) has @MarkerC (ignored)
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
        assertEquals(int.class, ctors.get(0).getRawParameterType(0));

        // Static factory creator methods: create(int) should remain, ignoredCreator(String) has @MarkerC (ignored)
        List<AnnotatedMethod> staticMethods = ac.getStaticMethods();
        assertEquals(2, staticMethods.size()); // create(int) and staticMethodNoArgs()
        boolean foundCreate = false;
        for (AnnotatedMethod am : staticMethods) {
            if ("create".equals(am.getName())) {
                foundCreate = true;
                assertEquals(1, am.getParameterCount());
            }
        }
        assertTrue("Expected create(int) factory method to be found", foundCreate);
    }

    @Test(timeout = 4000)
    public void testMemberMethodsResolutionAndFiltering() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, null);

        int methodCount = ac.getMemberMethodCount();
        assertTrue(methodCount > 0);

        // methodTwoArgs (<= 2 args) should be included
        assertNotNull(ac.findMethod("methodTwoArgs", new Class<?>[]{int.class, int.class}));

        // methodThreeArgs (> 2 args) should be filtered out
        assertNull(ac.findMethod("methodThreeArgs", new Class<?>[]{int.class, int.class, int.class}));

        // action() was declared in BaseInterface, BaseClass and SubClass
        AnnotatedMethod actionMethod = ac.findMethod("action", new Class<?>[]{});
        assertNotNull(actionMethod);
        assertFalse(Modifier.isStatic(actionMethod.getModifiers()));

        // Iteration via memberMethods()
        int iteratedCount = 0;
        for (AnnotatedMethod am : ac.memberMethods()) {
            assertNotNull(am);
            iteratedCount++;
        }
        assertEquals(methodCount, iteratedCount);
    }

    @Test(timeout = 4000)
    public void testFieldsResolutionExclusionsAndInheritance() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, null);

        int fieldCount = ac.getFieldCount();
        assertTrue(fieldCount >= 3);

        boolean foundSuperField = false;
        boolean foundAnnotatedSuperField = false;
        boolean foundSubField = false;
        boolean foundStaticOrTransient = false;

        for (AnnotatedField f : ac.fields()) {
            String name = f.getName();
            if ("superField".equals(name)) foundSuperField = true;
            if ("annotatedSuperField".equals(name)) {
                foundAnnotatedSuperField = true;
                assertNotNull(f.getAnnotation(MarkerA.class));
            }
            if ("subField".equals(name)) foundSubField = true;
            if ("staticField".equals(name) || "transientField".equals(name)) {
                foundStaticOrTransient = true;
            }
        }

        assertTrue("Should include inherited superField", foundSuperField);
        assertTrue("Should include inherited annotatedSuperField", foundAnnotatedSuperField);
        assertTrue("Should include subField", foundSubField);
        assertFalse("Static and transient fields must be excluded", foundStaticOrTransient);
    }

    // -------------------------------------------------------------------------
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullAnnotationIntrospectorDisablesProcessing() {
        // When AnnotationIntrospector is null, annotation processing is disabled
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, null, null);

        assertFalse(ac.hasAnnotations());
        assertEquals(0, ac.getAnnotations().size());
        assertNull(ac.getAnnotation(MarkerA.class));

        // Creators still resolved, but without annotations
        assertNotNull(ac.getDefaultConstructor());
        // Since introspector is null, MarkerC ignored constructor will NOT be removed
        assertEquals(2, ac.getConstructors().size());
        assertNotNull(ac.fields());
        assertTrue(ac.getFieldCount() > 0);
        assertNotNull(ac.memberMethods());
    }

    @Test(timeout = 4000)
    public void testWithAnnotationsImmutability() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, null);
        AnnotationMap map = new AnnotationMap();

        AnnotatedClass copy = ac.withAnnotations(map);
        assertNotSame(ac, copy);
        assertEquals(0, copy.getAnnotations().size());
    }

    @Test(timeout = 4000)
    public void testPrimitiveAndInterfaceFieldResolution() {
        // Primitive types and Interfaces have no superclass; _findFields should handle null parent gracefully
        AnnotatedClass acPrimitive = AnnotatedClass.construct(int.class, null, null);
        assertEquals(0, acPrimitive.getFieldCount());

        AnnotatedClass acInterface = AnnotatedClass.construct(BaseInterface.class, null, null);
        assertEquals(0, acInterface.getFieldCount());
    }

    @Test(timeout = 4000)
    public void testObjectWithoutCreators() {
        AnnotatedClass ac = AnnotatedClass.construct(Object.class, null, null);
        assertNotNull(ac.getDefaultConstructor());
        assertEquals(0, ac.getConstructors().size());
    }

    // -------------------------------------------------------------------------
    // PARTITION D: Constructor Parameter Mismatch & Mix-in Workarounds
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testInnerClassConstructorImplicitThisWorkaround() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(InnerClass.class, aintr, null);

        assertNull("Inner class should not have a default (0-arg) constructor", ac.getDefaultConstructor());
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());

        AnnotatedConstructor ctor = ctors.get(0);
        assertEquals(2, ctor.getParameterCount()); // outer class instance + String arg
        assertEquals(AnnotatedClassGeminiTest.class, ctor.getRawParameterType(0));
        assertEquals(String.class, ctor.getRawParameterType(1));
    }

    @Test(timeout = 4000)
    public void testEnumConstructorImplicitParamsWorkaround() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SampleEnum.class, aintr, null);

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());

        AnnotatedConstructor ctor = ctors.get(0);
        // Enum constructor has String name, int ordinal, String desc
        assertEquals(3, ctor.getParameterCount());
        assertEquals(String.class, ctor.getRawParameterType(0));
        assertEquals(int.class, ctor.getRawParameterType(1));
        assertEquals(String.class, ctor.getRawParameterType(2));
    }

    @Test(timeout = 4000)
    public void testConstructorAndFactoryMixIns() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(SubClass.class, SubClassMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, aintr, resolver);

        // Constructor mixin
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        assertNotNull("Constructor parameter should have mixin annotation", ctor.getAnnotation(MarkerB.class));
        assertNotNull(ctor.getParameterAnnotation(0, MarkerB.class));

        // Factory mixin
        boolean foundFactoryMixin = false;
        for (AnnotatedMethod am : ac.getStaticMethods()) {
            if ("create".equals(am.getName())) {
                MarkerB mb = am.getAnnotation(MarkerB.class);
                if (mb != null && "mixedCreator".equals(mb.value())) {
                    foundFactoryMixin = true;
                    assertNotNull(am.getParameterAnnotation(0, MarkerB.class));
                }
            }
        }
        assertTrue("Factory mixin should be merged", foundFactoryMixin);

        // Field mixin
        boolean foundFieldMixin = false;
        for (AnnotatedField f : ac.fields()) {
            if ("subField".equals(f.getName())) {
                MarkerB mb = f.getAnnotation(MarkerB.class);
                if (mb != null && "mixedField".equals(mb.value())) {
                    foundFieldMixin = true;
                }
            }
        }
        assertTrue("Field mixin should be merged", foundFieldMixin);
    }

    @Test(timeout = 4000)
    public void testObjectMethodMixIn() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(Object.class, ObjectHashCodeMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, aintr, resolver);

        AnnotatedMethod hashCodeMethod = ac.findMethod("hashCode", new Class<?>[]{});
        assertNotNull("hashCode should be augmented via Object.class mixin", hashCodeMethod);
        MarkerA marker = hashCodeMethod.getAnnotation(MarkerA.class);
        assertNotNull("hashCode should have MarkerA from ObjectHashCodeMixIn", marker);
        assertEquals("mixedHashCode", marker.value());
    }

    // -------------------------------------------------------------------------
    // PARTITION E: Annotation Bundle Handling
    // -------------------------------------------------------------------------

    @BundleContainer
    public static class BundledClass {}

    @Test(timeout = 4000)
    public void testAnnotationBundleExpansion() {
        SimpleIntrospector aintr = new SimpleIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(BundledClass.class, aintr, null);

        assertTrue(ac.hasAnnotations());
        // Since isAnnotationBundle returns true for BundleContainer,
        // it expands BundleContainer into its meta-annotation @MarkerA("bundled")
        MarkerA markerA = ac.getAnnotation(MarkerA.class);
        assertNotNull("Bundled annotation @MarkerA should be extracted", markerA);
        assertEquals("bundled", markerA.value());
    }
}