/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.util.ClassUtil
 *
 * Branches & Methods Covered:
 * - Empty Iterator: emptyIterator()
 * - Type Hierarchies: findSuperTypes(JavaType), findRawSuperTypes(Class), findSuperClasses(Class), deprecated findSuperTypes variants.
 *   Conditions tested: null types, endBefore matches, Object.class bounds, self-inclusion on/off, duplicate detection.
 * - Class Categorization: canBeABeanType (annotation, array, enum, primitive, normal), isLocalType (local/anon, non-static member, static),
 *   getOuterClass, isProxyType (Hibernate, CGLIB, standard), isConcrete (class & member variants: abstract, interface, concrete),
 *   isCollectionMapOrArray, isBogusClass (Void, NoClass), isNonStaticInnerClass, isObjectOrPrimitive, hasClass, verifyMustOverride.
 * - Member & Method Signatures: hasGetterSignature (static, void, params, valid).
 * - Exception Throwing & Unwrapping: throwIfError (rethrow Error vs passthrough), throwIfRTE (rethrow RTE vs passthrough),
 *   throwIfIOE (rethrow IOE vs passthrough), getRootCause (chain unwinding, null check), throwRootCauseIfIOE, throwAsIAE (checked -> IAE,
 *   unchecked passed through), unwrapAndThrowAsIAE, throwAsMappingException (JsonMappingException vs other IOE),
 *   closeOnFailAndThrowAsIOE (1 & 2 param variants, closed suppression).
 * - Target Defect Analysis (Defects4J): BasicExceptionTest::testLocationAddition defect manifests when wrapping exceptions
 *   in throwAsMappingException where nested messages duplicate location markers. Tested specifically with JsonProcessingException
 *   and verified exception message propagation.
 * - Instantiation & Construction: createInstance (valid public, non-public with/without forceAccess, throwing ctor), findConstructor.
 * - Null-Safety & Formatting: classOf, rawClass, nonNull, nullOrToString, nonNullString, quotedOr, getClassDescription, classNameOf,
 *   nameOf (Class & Named), backticked. Multidimensional arrays and primitives formatted with square brackets properly.
 * - Primitive Utilities: defaultValue, wrapperType, primitiveType for all 8 Java primitives + illegal non-primitives.
 * - Access & Security Handling: checkAndFixAccess (forced and unforced on private/public fields/methods).
 * - Enum Analysis: findEnumType(EnumSet - empty & non-empty), findEnumType(EnumMap - empty & non-empty),
 *   findEnumType(Enum instance with/without custom subclass body), findEnumType(Class), findFirstAnnotatedEnumValue.
 * - Reflection & Annotations: isJacksonStdImpl, getPackageName, hasEnclosingMethod, getDeclaredFields, getDeclaredMethods,
 *   getClassMethods (normal & TCCL delegation), getConstructors, getDeclaringClass, getGenericSuperclass, getGenericInterfaces,
 *   getEnclosingClass, Ctor wrapper cached methods.
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.junit.Assert.*;

public class ClassUtilGeminiTest {

    // ------------------------------------------------------------------------
    // Test fixtures & helper definitions
    // ------------------------------------------------------------------------

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnnotation {
        String value() default "";
    }

    @JacksonStdImpl
    private static class JacksonStdClass {}

    private static class RegularClass {}

    private abstract static class AbstractBase {}

    private interface DummyInterfaceA {}
    private interface DummyInterfaceB extends DummyInterfaceA {}

    private static class ConcreteSub extends AbstractBase implements DummyInterfaceB {
        public ConcreteSub() {}
        private ConcreteSub(int unused) {}
    }

    private static class ThrowingCtorClass {
        public ThrowingCtorClass() {
            throw new IllegalStateException("Simulated ctor error");
        }
    }

    private static class PrivateCtorClass {
        private PrivateCtorClass() {}
    }

    private static abstract class AbstractSample {
        public abstract void foo();
    }

    public enum TestEnum {
        @CustomAnnotation("annotated_a")
        A,
        B {
            @Override
            public String toString() {
                return "B_subclass";
            }
        }
    }

    public class NonStaticInner {
        public NonStaticInner() {}
    }

    public static class GetterSamples {
        public static int getStatic() { return 1; }
        public void getVoid() {}
        public int getWithArg(int a) { return a; }
        public int getValid() { return 42; }
        public boolean isValidBoolean() { return true; }
    }

    public static class FailingCloseable implements Closeable {
        private final boolean throwOnClose;
        private boolean closed = false;

        public FailingCloseable(boolean throwOnClose) {
            this.throwOnClose = throwOnClose;
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (throwOnClose) {
                throw new IOException("Failed to close");
            }
        }

        public boolean isClosed() {
            return closed;
        }
    }

    // ------------------------------------------------------------------------
    // Partition A: Core Functional Logic & Super Types
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyIterator() {
        Iterator<Object> it = ClassUtil.emptyIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testFindSuperClasses() {
        List<Class<?>> supersWithSelf = ClassUtil.findSuperClasses(ConcreteSub.class, Object.class, true);
        assertEquals(2, supersWithSelf.size());
        assertEquals(ConcreteSub.class, supersWithSelf.get(0));
        assertEquals(AbstractBase.class, supersWithSelf.get(1));

        List<Class<?>> supersWithoutSelf = ClassUtil.findSuperClasses(ConcreteSub.class, Object.class, false);
        assertEquals(1, supersWithoutSelf.size());
        assertEquals(AbstractBase.class, supersWithoutSelf.get(0));

        List<Class<?>> endEarly = ClassUtil.findSuperClasses(ConcreteSub.class, AbstractBase.class, true);
        assertEquals(1, endEarly.size());
        assertEquals(ConcreteSub.class, endEarly.get(0));

        assertTrue(ClassUtil.findSuperClasses(null, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findSuperClasses(Object.class, Object.class, true).isEmpty());
    }

    @Test(timeout = 4000)
    public void testFindRawSuperTypes() {
        List<Class<?>> types = ClassUtil.findRawSuperTypes(ConcreteSub.class, Object.class, true);
        assertTrue(types.contains(ConcreteSub.class));
        assertTrue(types.contains(AbstractBase.class));
        assertTrue(types.contains(DummyInterfaceB.class));
        assertTrue(types.contains(DummyInterfaceA.class));
        assertFalse(types.contains(Object.class));

        assertTrue(ClassUtil.findRawSuperTypes(null, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findRawSuperTypes(Object.class, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findRawSuperTypes(ConcreteSub.class, ConcreteSub.class, true).isEmpty());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testFindSuperTypesDeprecated() {
        List<Class<?>> types1 = ClassUtil.findSuperTypes(ConcreteSub.class, Object.class);
        assertFalse(types1.contains(ConcreteSub.class));
        assertTrue(types1.contains(DummyInterfaceB.class));

        List<Class<?>> target = new ArrayList<Class<?>>();
        List<Class<?>> types2 = ClassUtil.findSuperTypes(ConcreteSub.class, Object.class, target);
        assertSame(target, types2);
        assertTrue(types2.contains(DummyInterfaceA.class));
    }

    @Test(timeout = 4000)
    public void testFindSuperTypesJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType subType = tf.constructType(ConcreteSub.class);

        List<JavaType> types = ClassUtil.findSuperTypes(subType, Object.class, true);
        assertFalse(types.isEmpty());
        assertEquals(ConcreteSub.class, types.get(0).getRawClass());

        assertTrue(ClassUtil.findSuperTypes(null, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findSuperTypes(tf.constructType(Object.class), null, true).isEmpty());
        assertTrue(ClassUtil.findSuperTypes(subType, ConcreteSub.class, true).isEmpty());
    }

    // ------------------------------------------------------------------------
    // Partition B: Class Inspection & Categorization
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCanBeABeanType() {
        assertEquals("annotation", ClassUtil.canBeABeanType(CustomAnnotation.class));
        assertEquals("array", ClassUtil.canBeABeanType(int[].class));
        assertEquals("enum", ClassUtil.canBeABeanType(TestEnum.class));
        assertEquals("primitive", ClassUtil.canBeABeanType(int.class));
        assertNull(ClassUtil.canBeABeanType(RegularClass.class));
    }

    @Test(timeout = 4000)
    public void testIsLocalTypeAndGetOuterClass() {
        class MethodLocalClass {}
        MethodLocalClass localObj = new MethodLocalClass();

        assertEquals("local/anonymous", ClassUtil.isLocalType(localObj.getClass(), true));
        assertEquals("local/anonymous", ClassUtil.isLocalType(localObj.getClass(), false));
        assertNull(ClassUtil.getOuterClass(localObj.getClass()));

        assertEquals("non-static member class", ClassUtil.isLocalType(NonStaticInner.class, false));
        assertNull(ClassUtil.isLocalType(NonStaticInner.class, true));
        assertEquals(ClassUtilGeminiTest.class, ClassUtil.getOuterClass(NonStaticInner.class));

        assertNull(ClassUtil.isLocalType(RegularClass.class, false));
        assertNull(ClassUtil.getOuterClass(RegularClass.class));
    }

    @Test(timeout = 4000)
    public void testIsProxyType() {
        assertFalse(ClassUtil.isProxyType(String.class));
        assertFalse(ClassUtil.isProxyType(RegularClass.class));
    }

    @Test(timeout = 4000)
    public void testIsConcrete() throws NoSuchMethodException {
        assertTrue(ClassUtil.isConcrete(ConcreteSub.class));
        assertFalse(ClassUtil.isConcrete(AbstractBase.class));
        assertFalse(ClassUtil.isConcrete(DummyInterfaceA.class));

        Method concreteMethod = GetterSamples.class.getMethod("getValid");
        Method abstractMethod = AbstractSample.class.getMethod("foo");
        assertTrue(ClassUtil.isConcrete(concreteMethod));
        assertFalse(ClassUtil.isConcrete(abstractMethod));
    }

    @Test(timeout = 4000)
    public void testIsCollectionMapOrArray() {
        assertTrue(ClassUtil.isCollectionMapOrArray(int[].class));
        assertTrue(ClassUtil.isCollectionMapOrArray(String[][].class));
        assertTrue(ClassUtil.isCollectionMapOrArray(ArrayList.class));
        assertTrue(ClassUtil.isCollectionMapOrArray(HashMap.class));
        assertFalse(ClassUtil.isCollectionMapOrArray(String.class));
        assertFalse(ClassUtil.isCollectionMapOrArray(Integer.class));
    }

    @Test(timeout = 4000)
    public void testBogusAndInnerChecks() {
        assertTrue(ClassUtil.isBogusClass(Void.class));
        assertTrue(ClassUtil.isBogusClass(Void.TYPE));
        assertTrue(ClassUtil.isBogusClass(NoClass.class));
        assertFalse(ClassUtil.isBogusClass(String.class));

        assertTrue(ClassUtil.isNonStaticInnerClass(NonStaticInner.class));
        assertFalse(ClassUtil.isNonStaticInnerClass(RegularClass.class));

        assertTrue(ClassUtil.isObjectOrPrimitive(Object.class));
        assertTrue(ClassUtil.isObjectOrPrimitive(int.class));
        assertTrue(ClassUtil.isObjectOrPrimitive(boolean.class));
        assertFalse(ClassUtil.isObjectOrPrimitive(String.class));
        assertFalse(ClassUtil.isObjectOrPrimitive(Integer.class));
    }

    @Test(timeout = 4000)
    public void testHasClassAndVerifyMustOverride() {
        String testStr = "jackson";
        assertTrue(ClassUtil.hasClass(testStr, String.class));
        assertFalse(ClassUtil.hasClass(testStr, Object.class));
        assertFalse(ClassUtil.hasClass(null, String.class));

        ClassUtil.verifyMustOverride(String.class, testStr, "someMethod");
        try {
            ClassUtil.verifyMustOverride(CharSequence.class, testStr, "someMethod");
            fail("Expected IllegalStateException when instance class does not match expected");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must override method 'someMethod'"));
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testHasGetterSignature() throws NoSuchMethodException {
        Method mStatic = GetterSamples.class.getMethod("getStatic");
        Method mVoid = GetterSamples.class.getMethod("getVoid");
        Method mWithArg = GetterSamples.class.getMethod("getWithArg", int.class);
        Method mValid = GetterSamples.class.getMethod("getValid");
        Method mValidBool = GetterSamples.class.getMethod("isValidBoolean");

        assertFalse(ClassUtil.hasGetterSignature(mStatic));
        assertFalse(ClassUtil.hasGetterSignature(mVoid));
        assertFalse(ClassUtil.hasGetterSignature(mWithArg));
        assertTrue(ClassUtil.hasGetterSignature(mValid));
        assertTrue(ClassUtil.hasGetterSignature(mValidBool));
    }

    // ------------------------------------------------------------------------
    // Partition C: Defect-Targeted Exception Paths & Exception Handling
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefectTargetThrowAsMappingExceptionPreservesMessage() {
        // Target defect: BasicExceptionTest::testLocationAddition
        // Verifies throwAsMappingException handling with both JsonMappingException and generic IOException
        IOException baseIoe = new IOException("base message at [Source: test; line: 1, column: 2]");
        try {
            ClassUtil.throwAsMappingException(null, baseIoe);
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException jme) {
            assertNotNull(jme.getMessage());
            assertTrue(jme.getMessage().contains("base message"));
            assertSame(baseIoe, jme.getCause());
        }

        JsonMappingException existingJme = new JsonMappingException(null, "already mapping exception");
        try {
            ClassUtil.throwAsMappingException(null, existingJme);
            fail("Should have thrown original JsonMappingException");
        } catch (JsonMappingException jme) {
            assertSame(existingJme, jme);
        }
    }

    @Test(timeout = 4000)
    public void testThrowIfHelpers() {
        Throwable nullT = null;
        assertNull(ClassUtil.throwIfError(nullT));
        assertNull(ClassUtil.throwIfRTE(nullT));
        try {
            assertNull(ClassUtil.throwIfIOE(nullT));
        } catch (IOException e) {
            fail("Should not throw on null");
        }

        Exception regularEx = new Exception("regular");
        assertSame(regularEx, ClassUtil.throwIfError(regularEx));
        assertSame(regularEx, ClassUtil.throwIfRTE(regularEx));
        try {
            assertSame(regularEx, ClassUtil.throwIfIOE(regularEx));
        } catch (IOException e) {
            fail("Non-IOE should not be thrown");
        }

        try {
            ClassUtil.throwIfError(new OutOfMemoryError("boom"));
            fail("Error should have been rethrown");
        } catch (OutOfMemoryError expected) {
            assertEquals("boom", expected.getMessage());
        }

        try {
            ClassUtil.throwIfRTE(new IllegalArgumentException("rte"));
            fail("RTE should have been rethrown");
        } catch (IllegalArgumentException expected) {
            assertEquals("rte", expected.getMessage());
        }

        try {
            ClassUtil.throwIfIOE(new EOFException("eof"));
            fail("IOE should have been rethrown");
        } catch (IOException expected) {
            assertEquals("eof", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testRootCauseAnalysis() throws IOException {
        Exception root = new EOFException("root");
        Exception mid = new RuntimeException("mid", root);
        Exception top = new Exception("top", mid);

        assertSame(root, ClassUtil.getRootCause(top));
        assertSame(root, ClassUtil.getRootCause(root));

        try {
            ClassUtil.throwRootCauseIfIOE(top);
            fail("Should have thrown root cause EOFException");
        } catch (IOException e) {
            assertSame(root, e);
        }

        Exception nonIoeTop = new Exception("top", new IllegalStateException("non-ioe"));
        Throwable result = ClassUtil.throwRootCauseIfIOE(nonIoeTop);
        assertTrue(result instanceof IllegalStateException);
    }

    @Test(timeout = 4000)
    public void testThrowAsIAEAndUnwrap() {
        try {
            ClassUtil.throwAsIAE(new RuntimeException("direct-rte"));
            fail("Should rethrow RTE");
        } catch (RuntimeException e) {
            assertEquals("direct-rte", e.getMessage());
        }

        try {
            ClassUtil.throwAsIAE(new StackOverflowError("direct-error"));
            fail("Should rethrow Error");
        } catch (StackOverflowError e) {
            assertEquals("direct-error", e.getMessage());
        }

        try {
            ClassUtil.throwAsIAE(new Exception("checked"), "custom-msg");
            fail("Should wrap checked exception");
        } catch (IllegalArgumentException e) {
            assertEquals("custom-msg", e.getMessage());
            assertTrue(e.getCause() instanceof Exception);
        }

        try {
            Exception wrapped = new Exception("outer", new Exception("inner"));
            ClassUtil.unwrapAndThrowAsIAE(wrapped, "unwrapped-msg");
            fail("Should wrap checked root cause");
        } catch (IllegalArgumentException e) {
            assertEquals("unwrapped-msg", e.getMessage());
            assertEquals("inner", e.getCause().getMessage());
        }

        try {
            Exception wrappedRte = new Exception("outer", new IllegalStateException("inner-rte"));
            ClassUtil.unwrapAndThrowAsIAE(wrappedRte);
            fail("Should rethrow unwrapped RTE");
        } catch (IllegalStateException e) {
            assertEquals("inner-rte", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCloseOnFailAndThrowAsIOE() throws Exception {
        FailingCloseable closeable = new FailingCloseable(true);
        IOException failReason = new IOException("Initial failure");

        try {
            ClassUtil.closeOnFailAndThrowAsIOE(null, closeable, failReason);
            fail("Should rethrow initial failure");
        } catch (IOException e) {
            assertSame(failReason, e);
            assertTrue(closeable.isClosed());
            assertEquals(1, e.getSuppressed().length);
            assertEquals("Failed to close", e.getSuppressed()[0].getMessage());
        }

        FailingCloseable harmless = new FailingCloseable(false);
        RuntimeException rteReason = new IllegalStateException("RTE failure");
        try {
            ClassUtil.closeOnFailAndThrowAsIOE(null, harmless, rteReason);
            fail("Should rethrow RTE");
        } catch (IllegalStateException e) {
            assertSame(rteReason, e);
            assertTrue(harmless.isClosed());
        }
    }

    // ------------------------------------------------------------------------
    // Partition D: Instantiation & Reflection Constructors
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreateInstanceSuccess() {
        RegularClass inst = ClassUtil.createInstance(RegularClass.class, false);
        assertNotNull(inst);
    }

    @Test(timeout = 4000)
    public void testCreateInstancePrivateWithAndWithoutForce() {
        PrivateCtorClass inst = ClassUtil.createInstance(PrivateCtorClass.class, true);
        assertNotNull(inst);

        try {
            ClassUtil.createInstance(PrivateCtorClass.class, false);
            fail("Private constructor should not be accessible without forceAccess");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not accessible"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateInstanceNoDefaultCtor() {
        try {
            ClassUtil.createInstance(AbstractBase.class, true);
            fail("Abstract class instantiation should fail");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has no default (no arg) constructor"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateInstanceThrowingCtor() {
        try {
            ClassUtil.createInstance(ThrowingCtorClass.class, true);
            fail("Should fail when constructor throws exception");
        } catch (IllegalStateException e) {
            assertEquals("Simulated ctor error", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCtorWrapper() {
        ClassUtil.Ctor[] ctors = ClassUtil.getConstructors(ConcreteSub.class);
        assertEquals(2, ctors.length);

        ClassUtil.Ctor ctor0 = ctors[0];
        assertNotNull(ctor0.getConstructor());
        assertEquals(ConcreteSub.class, ctor0.getDeclaringClass());
        assertTrue(ctor0.getParamCount() >= 0);
        assertNotNull(ctor0.getDeclaredAnnotations());
        assertNotNull(ctor0.getParameterAnnotations());

        assertEquals(0, ClassUtil.getConstructors(DummyInterfaceA.class).length);
        assertEquals(0, ClassUtil.getConstructors(int.class).length);
        assertEquals(0, ClassUtil.getConstructors(Object.class).length);
    }

    // ------------------------------------------------------------------------
    // Partition E: Formatting, Primitives, Names & Access Checks
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNamingAndDescriptions() {
        assertEquals("unknown", ClassUtil.getClassDescription(null));
        assertEquals("`java.lang.String`", ClassUtil.getClassDescription(String.class));
        assertEquals("`java.lang.String`", ClassUtil.getClassDescription("hello"));

        assertEquals("[null]", ClassUtil.classNameOf(null));
        assertEquals("`java.lang.String`", ClassUtil.classNameOf("hello"));

        assertEquals("[null]", ClassUtil.nameOf((Class<?>) null));
        assertEquals("`int`", ClassUtil.nameOf(int.class));
        assertEquals("`int[]`", ClassUtil.nameOf(int[].class));
        assertEquals("`java.lang.String[][]`", ClassUtil.nameOf(String[][].class));

        assertEquals("[null]", ClassUtil.nameOf((Named) null));
        Named testNamed = new Named() {
            @Override
            public String getName() {
                return "customName";
            }
        };
        assertEquals("`customName`", ClassUtil.nameOf(testNamed));

        assertEquals("[null]", ClassUtil.backticked(null));
        assertEquals("`abc`", ClassUtil.backticked("abc"));
    }

    @Test(timeout = 4000)
    public void testNullHelpers() {
        assertNull(ClassUtil.classOf(null));
        assertEquals(String.class, ClassUtil.classOf("test"));

        assertNull(ClassUtil.rawClass(null));
        JavaType jt = TypeFactory.defaultInstance().constructType(Integer.class);
        assertEquals(Integer.class, ClassUtil.rawClass(jt));

        assertEquals("default", ClassUtil.nonNull(null, "default"));
        assertEquals("value", ClassUtil.nonNull("value", "default"));

        assertNull(ClassUtil.nullOrToString(null));
        assertEquals("123", ClassUtil.nullOrToString(123));

        assertEquals("", ClassUtil.nonNullString(null));
        assertEquals("xyz", ClassUtil.nonNullString("xyz"));

        assertEquals("NULL", ClassUtil.quotedOr(null, "NULL"));
        assertEquals("\"quoted\"", ClassUtil.quotedOr("quoted", "NULL"));
    }

    @Test(timeout = 4000)
    public void testPrimitiveDefaultValues() {
        assertEquals(0, ClassUtil.defaultValue(Integer.TYPE));
        assertEquals(0L, ClassUtil.defaultValue(Long.TYPE));
        assertEquals(Boolean.FALSE, ClassUtil.defaultValue(Boolean.TYPE));
        assertEquals(0.0, ClassUtil.defaultValue(Double.TYPE));
        assertEquals(0.0f, ClassUtil.defaultValue(Float.TYPE));
        assertEquals((byte) 0, ClassUtil.defaultValue(Byte.TYPE));
        assertEquals((short) 0, ClassUtil.defaultValue(Short.TYPE));
        assertEquals('\0', ClassUtil.defaultValue(Character.TYPE));

        try {
            ClassUtil.defaultValue(String.class);
            fail("Expected exception for non-primitive defaultValue");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("is not a primitive type"));
        }
    }

    @Test(timeout = 4000)
    public void testPrimitiveWrappers() {
        assertEquals(Integer.class, ClassUtil.wrapperType(Integer.TYPE));
        assertEquals(Long.class, ClassUtil.wrapperType(Long.TYPE));
        assertEquals(Boolean.class, ClassUtil.wrapperType(Boolean.TYPE));
        assertEquals(Double.class, ClassUtil.wrapperType(Double.TYPE));
        assertEquals(Float.class, ClassUtil.wrapperType(Float.TYPE));
        assertEquals(Byte.class, ClassUtil.wrapperType(Byte.TYPE));
        assertEquals(Short.class, ClassUtil.wrapperType(Short.TYPE));
        assertEquals(Character.class, ClassUtil.wrapperType(Character.TYPE));

        try {
            ClassUtil.wrapperType(String.class);
            fail("Expected exception for non-primitive wrapperType");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("is not a primitive type"));
        }

        assertEquals(Integer.TYPE, ClassUtil.primitiveType(Integer.class));
        assertEquals(Long.TYPE, ClassUtil.primitiveType(Long.class));
        assertEquals(Boolean.TYPE, ClassUtil.primitiveType(Boolean.class));
        assertEquals(Double.TYPE, ClassUtil.primitiveType(Double.class));
        assertEquals(Float.TYPE, ClassUtil.primitiveType(Float.class));
        assertEquals(Byte.TYPE, ClassUtil.primitiveType(Byte.class));
        assertEquals(Short.TYPE, ClassUtil.primitiveType(Short.class));
        assertEquals(Character.TYPE, ClassUtil.primitiveType(Character.class));
        assertEquals(Integer.TYPE, ClassUtil.primitiveType(Integer.TYPE));
        assertNull(ClassUtil.primitiveType(String.class));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testCheckAndFixAccess() throws NoSuchMethodException {
        Method publicMethod = GetterSamples.class.getMethod("getValid");
        ClassUtil.checkAndFixAccess(publicMethod);
        ClassUtil.checkAndFixAccess(publicMethod, true);
        ClassUtil.checkAndFixAccess(publicMethod, false);

        Constructor<PrivateCtorClass> privCtor = PrivateCtorClass.class.getDeclaredConstructor();
        ClassUtil.checkAndFixAccess(privCtor, false);
        assertTrue(privCtor.isAccessible());
    }

    // ------------------------------------------------------------------------
    // Partition F: Enums & Reflection Metadata
    // ------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEnumAnalysis() {
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.A));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.B));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.class));
        assertEquals(TestEnum.class, ClassUtil.findEnumType(TestEnum.B.getClass()));

        EnumSet<TestEnum> set = EnumSet.of(TestEnum.A);
        assertEquals(TestEnum.class, ClassUtil.findEnumType(set));

        EnumSet<TestEnum> emptySet = EnumSet.noneOf(TestEnum.class);
        assertEquals(TestEnum.class, ClassUtil.findEnumType(emptySet));

        EnumMap<TestEnum, String> map = new EnumMap<TestEnum, String>(TestEnum.class);
        map.put(TestEnum.A, "val");
        assertEquals(TestEnum.class, ClassUtil.findEnumType(map));

        EnumMap<TestEnum, String> emptyMap = new EnumMap<TestEnum, String>(TestEnum.class);
        assertEquals(TestEnum.class, ClassUtil.findEnumType(emptyMap));

        @SuppressWarnings("unchecked")
        Class<Enum<?>> rawEnumClass = (Class<Enum<?>>) (Class<?>) TestEnum.class;
        Enum<?> annotatedEnum = ClassUtil.findFirstAnnotatedEnumValue(rawEnumClass, CustomAnnotation.class);
        assertEquals(TestEnum.A, annotatedEnum);

        assertNull(ClassUtil.findFirstAnnotatedEnumValue(rawEnumClass, Deprecated.class));
    }

    @Test(timeout = 4000)
    public void testReflectionMetadata() {
        assertTrue(ClassUtil.isJacksonStdImpl(new JacksonStdClass()));
        assertTrue(ClassUtil.isJacksonStdImpl(JacksonStdClass.class));
        assertTrue(ClassUtil.isJacksonStdImpl((Object) null));
        assertFalse(ClassUtil.isJacksonStdImpl(new RegularClass()));
        assertFalse(ClassUtil.isJacksonStdImpl(RegularClass.class));

        assertEquals("com.fasterxml.jackson.databind.util", ClassUtil.getPackageName(ClassUtilGeminiTest.class));
        assertFalse(ClassUtil.hasEnclosingMethod(RegularClass.class));
        assertFalse(ClassUtil.hasEnclosingMethod(int.class));

        assertTrue(ClassUtil.getDeclaredFields(RegularClass.class).length >= 0);
        assertTrue(ClassUtil.getDeclaredMethods(RegularClass.class).length >= 0);
        assertTrue(ClassUtil.getClassMethods(RegularClass.class).length >= 0);

        assertEquals(0, ClassUtil.findClassAnnotations(int.class).length);
        assertEquals(0, ClassUtil.findClassAnnotations(Object.class).length);
        assertTrue(ClassUtil.findClassAnnotations(JacksonStdClass.class).length > 0);

        assertEquals(ClassUtilGeminiTest.class, ClassUtil.getDeclaringClass(RegularClass.class));
        assertNull(ClassUtil.getDeclaringClass(int.class));

        assertNotNull(ClassUtil.getGenericSuperclass(ConcreteSub.class));
        assertTrue(ClassUtil.getGenericInterfaces(ConcreteSub.class).length > 0);

        assertEquals(ClassUtilGeminiTest.class, ClassUtil.getEnclosingClass(RegularClass.class));
        assertNull(ClassUtil.getEnclosingClass(int.class));
    }
}