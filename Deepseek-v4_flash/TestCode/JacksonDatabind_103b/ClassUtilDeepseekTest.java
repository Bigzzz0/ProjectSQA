package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Targets for ClassUtil:
 * - emptyIterator singleton/behavior
 * - find*SuperTypes traversal, endBefore and addClassItself branches
 * - bean/local/proxy/concrete class detection branches
 * - member and exception helper branches
 * - primitive/wrapper mapping and invalid-type exception branches
 * - enum detection including empty EnumSet/EnumMap JDK-field fallback
 * - annotated enum value scanning
 * - constructor lookup/access fixing and instance creation failure paths
 * - name/description/accessor branches (null, arrays, primitives, Named)
 * - Ctor wrapper caching and annotation access
 * - KNOWN DEFECT: missing ClassUtil.exceptionMessage(Throwable) causes
 *   duplicate "at [" markers when wrapping JsonProcessingException messages.
 *   The dedicated test uses reflection so it compiles even on the defective
 *   version while failing at runtime when the method is absent.
 */
public class ClassUtilDeepseekTest {

    interface TestInterfaceA { void doIt(); }
    interface TestInterfaceB extends TestInterfaceA {}
    static class TestClassA implements TestInterfaceB {}
    static class TestClassB extends TestClassA {}

    @interface SampleAnnotation {}

    enum SampleEnum { A, B, C }

    enum ComplexEnum {
        A { @Override public String toString() { return "a"; } },
        B, C;
    }

    enum AnnotatedEnum {
        @Deprecated A,
        B,
        @Deprecated C;
    }

    class InnerMember {}
    static class NestedMember {}

    public static class PublicCtor {
        public PublicCtor() { }
    }

    private static class PrivateCtor {
        private PrivateCtor() { }
    }

    static class NoDefaultCtor {
        NoDefaultCtor(int x) { }
    }

    public abstract static class AbstractBean { }

    static class FieldsBean {
        public int x;
        private String y;
    }

    static class MethodsBean {
        public void publicMethod() { }
        private int privateMethod() { return 1; }
        public static int staticMethod() { return 0; }
        public void methodWithArg(int x) { }
        public void voidMethod() { }
        public String getValue() { return null; }
    }

    static class AccessBean {
        private int secret;
        private void hidden() { }
    }

    static class CtorBean {
        @Deprecated
        public CtorBean(String s, int i) { }
    }

    static class GenericChild extends ArrayList<String> implements Comparable<GenericChild> {
        @Override public int compareTo(GenericChild o) { return 0; }
    }

    @Deprecated
    static class AnnotatedBean { }

    @JacksonStdImpl
    static class StdImplBean { }

    static class PlainBean { }

    static class BaseType { }
    static class SubType extends BaseType { }

    @Test(timeout = 4000)
    public void testEmptyIterator() {
        Iterator<String> it = ClassUtil.emptyIterator();
        assertNotNull(it);
        assertFalse(it.hasNext());
        assertNotNull(ClassUtil.emptyIterator());
    }

    @Test(timeout = 4000)
    public void testFindSuperTypesJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType type = tf.constructType(TestClassB.class);

        List<JavaType> result = ClassUtil.findSuperTypes(type, Object.class, false);
        assertEquals(3, result.size());
        assertEquals(TestClassA.class, result.get(0).getRawClass());
        assertEquals(TestInterfaceB.class, result.get(1).getRawClass());
        assertEquals(TestInterfaceA.class, result.get(2).getRawClass());

        result = ClassUtil.findSuperTypes(type, Object.class, true);
        assertEquals(4, result.size());
        assertEquals(TestClassB.class, result.get(0).getRawClass());
        assertEquals(TestClassA.class, result.get(1).getRawClass());

        assertTrue(ClassUtil.findSuperTypes(null, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findSuperTypes(tf.constructType(Object.class), null, true).isEmpty());
        assertTrue(ClassUtil.findSuperTypes(type, TestClassB.class, true).isEmpty());
    }

    @Test(timeout = 4000)
    public void testFindRawSuperTypesAndDeprecatedVariants() {
        List<Class<?>> result = ClassUtil.findRawSuperTypes(TestClassB.class, Object.class, false);
        assertEquals(3, result.size());
        assertEquals(TestClassA.class, result.get(0));
        assertEquals(TestInterfaceB.class, result.get(1));
        assertEquals(TestInterfaceA.class, result.get(2));

        result = ClassUtil.findRawSuperTypes(TestClassB.class, Object.class, true);
        assertEquals(4, result.size());
        assertEquals(TestClassB.class, result.get(0));

        assertTrue(ClassUtil.findRawSuperTypes(null, Object.class, true).isEmpty());
        assertTrue(ClassUtil.findRawSuperTypes(Object.class, null, true).isEmpty());
        assertTrue(ClassUtil.findRawSuperTypes(TestClassB.class, TestClassB.class, true).isEmpty());

        List<Class<?>> deprecated = ClassUtil.findSuperTypes(TestClassB.class, Object.class);
        assertEquals(3, deprecated.size());

        List<Class<?>> sink = new ArrayList<Class<?>>();
        List<Class<?>> same = ClassUtil.findSuperTypes(TestClassB.class, Object.class, sink);
        assertSame(sink, same);
        assertEquals(3, sink.size());
    }

    @Test(timeout = 4000)
    public void testFindSuperClasses() {
        List<Class<?>> result = ClassUtil.findSuperClasses(TestClassB.class, Object.class, false);
        assertEquals(1, result.size());
        assertEquals(TestClassA.class, result.get(0));

        result = ClassUtil.findSuperClasses(TestClassB.class, Object.class, true);
        assertEquals(2, result.size());
        assertEquals(TestClassB.class, result.get(0));
        assertEquals(TestClassA.class, result.get(1));

        result = ClassUtil.findSuperClasses(TestClassB.class, TestClassA.class, true);
        assertEquals(1, result.size());
        assertEquals(TestClassB.class, result.get(0));

        assertTrue(ClassUtil.findSuperClasses(null, Object.class, false).isEmpty());
        assertTrue(ClassUtil.findSuperClasses(Object.class, null, false).isEmpty());
    }

    @Test(timeout = 4000)
    public void testClassTypeDetections() throws Exception {
        assertEquals("annotation", ClassUtil.canBeABeanType(SampleAnnotation.class));
        assertEquals("array", ClassUtil.canBeABeanType(String[].class));
        assertEquals("enum", ClassUtil.canBeABeanType(SampleEnum.class));
        assertEquals("primitive", ClassUtil.canBeABeanType(int.class));
        assertNull(ClassUtil.canBeABeanType(String.class));

        assertTrue(ClassUtil.isCollectionMapOrArray(String[].class));
        assertTrue(ClassUtil.isCollectionMapOrArray(ArrayList.class));
        assertTrue(ClassUtil.isCollectionMapOrArray(HashMap.class));
        assertFalse(ClassUtil.isCollectionMapOrArray(String.class));

        assertTrue(ClassUtil.isBogusClass(Void.class));
        assertTrue(ClassUtil.isBogusClass(Void.TYPE));
        assertTrue(ClassUtil.isBogusClass(Class.forName("com.fasterxml.jackson.databind.annotation.NoClass")));
        assertFalse(ClassUtil.isBogusClass(Integer.class));

        assertTrue(ClassUtil.isObjectOrPrimitive(Object.class));
        assertTrue(ClassUtil.isObjectOrPrimitive(int.class));
        assertFalse(ClassUtil.isObjectOrPrimitive(String.class));

        assertTrue(ClassUtil.hasClass("foo", String.class));
        assertFalse(ClassUtil.hasClass(new Object(), String.class));
        assertFalse(ClassUtil.hasClass(null, String.class));

        assertFalse(ClassUtil.isProxyType(String.class));
    }

    @Test(timeout = 4000)
    public void testConcreteAndLocalClassDetection() throws Exception {
        assertTrue(ClassUtil.isConcrete(PublicCtor.class));
        assertFalse(ClassUtil.isConcrete(AbstractBean.class));
        assertFalse(ClassUtil.isConcrete(TestInterfaceA.class));
        assertFalse(ClassUtil.isConcrete(SampleAnnotation.class));

        Method concreteMethod = MethodsBean.class.getDeclaredMethod("publicMethod");
        assertTrue(ClassUtil.isConcrete(concreteMethod));
        Method interfaceMethod = TestInterfaceA.class.getMethod("doIt");
        assertFalse(ClassUtil.isConcrete(interfaceMethod));

        class LocalDummy { }

        assertTrue(ClassUtil.isNonStaticInnerClass(InnerMember.class));
        assertFalse(ClassUtil.isNonStaticInnerClass(NestedMember.class));
        assertTrue(ClassUtil.isNonStaticInnerClass(LocalDummy.class));

        assertEquals("non-static member class", ClassUtil.isLocalType(InnerMember.class, false));
        assertNull(ClassUtil.isLocalType(InnerMember.class, true));
        assertNull(ClassUtil.isLocalType(NestedMember.class, false));
        assertEquals("local/anonymous", ClassUtil.isLocalType(LocalDummy.class, true));
        assertNull(ClassUtil.isLocalType(null, true));

        assertSame(ClassUtilDeepseekTest.class, ClassUtil.getOuterClass(InnerMember.class));
        assertNull(ClassUtil.getOuterClass(NestedMember.class));
        assertNull(ClassUtil.getOuterClass(LocalDummy.class));

        BaseType base = new BaseType();
        ClassUtil.verifyMustOverride(BaseType.class, base, "foo");
        try {
            ClassUtil.verifyMustOverride(BaseType.class, new SubType(), "foo");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("must override"));
        }
    }

    @Test(timeout = 4000)
    public void testHasGetterSignature() throws Exception {
        assertFalse(ClassUtil.hasGetterSignature(MethodsBean.class.getDeclaredMethod("staticMethod")));
        assertFalse(ClassUtil.hasGetterSignature(MethodsBean.class.getDeclaredMethod("methodWithArg", int.class)));
        assertFalse(ClassUtil.hasGetterSignature(MethodsBean.class.getDeclaredMethod("voidMethod")));
        assertTrue(ClassUtil.hasGetterSignature(MethodsBean.class.getDeclaredMethod("getValue")));
    }

    @Test(timeout = 4000)
    public void testThrowIfHelpers() throws Exception {
        Error error = new Error("err");
        try {
            ClassUtil.throwIfError(error);
            fail("Should have thrown Error");
        } catch (Error e) {
            assertSame(error, e);
        }

        RuntimeException rte = new RuntimeException("rte");
        try {
            ClassUtil.throwIfRTE(rte);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertSame(rte, e);
        }
        assertSame(error, ClassUtil.throwIfRTE(error));

        IOException ioe = new IOException("ioe");
        try {
            ClassUtil.throwIfIOE(ioe);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }
        assertSame(rte, ClassUtil.throwIfIOE(rte));
    }

    @Test(timeout = 4000)
    public void testRootCauseAndWrappers() throws Exception {
        Throwable root = new IllegalArgumentException("root");
        Throwable middle = new Exception("mid", root);
        Throwable top = new Exception("top", middle);
        assertSame(root, ClassUtil.getRootCause(top));

        IOException rootIO = new IOException("ioRoot");
        try {
            ClassUtil.throwRootCauseIfIOE(new Exception("wrap", rootIO));
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertSame(rootIO, e);
        }

        Throwable runtimeRoot = new IllegalStateException("rtRoot");
        assertSame(runtimeRoot, ClassUtil.throwRootCauseIfIOE(new Exception("wrap", runtimeRoot)));
    }

    @Test(timeout = 4000)
    public void testThrowAsIAE() {
        RuntimeException rte = new RuntimeException("rte");
        try {
            ClassUtil.throwAsIAE(rte);
            fail("Should have thrown original RuntimeException");
        } catch (RuntimeException e) {
            assertSame(rte, e);
        }

        try {
            ClassUtil.throwAsIAE(rte, "override");
            fail("Should have thrown original RuntimeException");
        } catch (RuntimeException e) {
            assertSame(rte, e);
        }

        Error error = new Error("err");
        try {
            ClassUtil.throwAsIAE(error);
            fail("Should have thrown original Error");
        } catch (Error e) {
            assertSame(error, e);
        }

        Exception checked = new Exception("checked");
        try {
            ClassUtil.throwAsIAE(checked);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("checked", e.getMessage());
            assertSame(checked, e.getCause());
        }

        try {
            ClassUtil.throwAsIAE(checked, "custom");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("custom", e.getMessage());
            assertSame(checked, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testUnwrapAndThrowAsIAE() {
        Exception root = new Exception("root");
        Exception wrapper = new Exception("wrapper", root);

        try {
            ClassUtil.unwrapAndThrowAsIAE(wrapper);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("root", e.getMessage());
            assertSame(root, e.getCause());
        }

        try {
            ClassUtil.unwrapAndThrowAsIAE(wrapper, "custom");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("custom", e.getMessage());
            assertSame(root, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testThrowAsMappingException() {
        JsonMappingException jme = new JsonMappingException((java.io.Closeable) null, "mapping problem");
        try {
            ClassUtil.throwAsMappingException(null, jme);
            fail("Should have thrown JsonMappingException");
        } catch (JsonMappingException e) {
            assertSame(jme, e);
        }
    }

    @Test(timeout = 4000)
    public void testCloseOnFailOverloadWithNullGenerator() throws Exception {
        IOException ioe = new IOException("io");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ClassUtil.closeOnFailAndThrowAsIOE(null, out, ioe);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }

        IllegalStateException rte = new IllegalStateException("rte");
        try {
            ClassUtil.closeOnFailAndThrowAsIOE(null, new ByteArrayOutputStream(), rte);
            fail("Should have thrown RuntimeException");
        } catch (IllegalStateException e) {
            assertSame(rte, e);
        }

        Exception checked = new Exception("checked");
        try {
            ClassUtil.closeOnFailAndThrowAsIOE(null, null, checked);
            fail("Should have thrown RuntimeException");
        } catch (RuntimeException e) {
            assertSame(checked, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testCreateInstanceAndFindConstructor() {
        assertNotNull(ClassUtil.findConstructor(PublicCtor.class, false));
        assertNotNull(ClassUtil.findConstructor(PublicCtor.class, true));
        assertNotNull(ClassUtil.findConstructor(PrivateCtor.class, true));
        assertNull(ClassUtil.findConstructor(NoDefaultCtor.class, true));

        try {
            ClassUtil.findConstructor(PrivateCtor.class, false);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not accessible"));
        }

        PublicCtor p = ClassUtil.createInstance(PublicCtor.class, true);
        assertNotNull(p);
        assertNotNull(ClassUtil.createInstance(PrivateCtor.class, true));

        try {
            ClassUtil.createInstance(NoDefaultCtor.class, true);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("has no default"));
        }

        try {
            ClassUtil.createInstance(AbstractBean.class, true);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Failed to instantiate"));
        }
    }

    @Test(timeout = 4000)
    public void testClassDescriptionAndNameMethods() {
        assertEquals("unknown", ClassUtil.getClassDescription(null));
        assertEquals("`java.lang.String`", ClassUtil.getClassDescription(String.class));
        assertEquals("`java.lang.String`", ClassUtil.getClassDescription("abc"));

        assertEquals("[null]", ClassUtil.classNameOf(null));
        assertEquals("`java.lang.String`", ClassUtil.classNameOf("abc"));

        assertEquals("[null]", ClassUtil.nameOf((Class<?>) null));
        assertEquals("`java.lang.String`", ClassUtil.nameOf(String.class));
        assertEquals("`java.lang.String[]`", ClassUtil.nameOf(String[].class));
        assertEquals("`int`", ClassUtil.nameOf(int.class));
        assertEquals("`int[]`", ClassUtil.nameOf(int[].class));
        assertEquals("`int[][]`", ClassUtil.nameOf(int[][].class));

        Named named = new Named() {
            @Override
            public String getName() { return "bob"; }
        };
        assertEquals("[null]", ClassUtil.nameOf((Named) null));
        assertEquals("`bob`", ClassUtil.nameOf(named));

        assertEquals("[null]", ClassUtil.backticked(null));
        assertEquals("`abc`", ClassUtil.backticked("abc"));
    }

    @Test(timeout = 4000)
    public void testNullAndStringHelpers() {
        String defaultValue = "d";
        String value = "v";
        assertSame(defaultValue, ClassUtil.nonNull(null, defaultValue));
        assertSame(value, ClassUtil.nonNull(value, defaultValue));

        assertNull(ClassUtil.nullOrToString(null));
        assertEquals("abc", ClassUtil.nullOrToString("abc"));

        assertEquals("", ClassUtil.nonNullString(null));
        assertSame("s", ClassUtil.nonNullString("s"));

        assertEquals("forNull", ClassUtil.quotedOr(null, "forNull"));
        assertEquals("\"abc\"", ClassUtil.quotedOr("abc", "forNull"));

        assertNull(ClassUtil.classOf(null));
        assertEquals(String.class, ClassUtil.classOf("abc"));
        assertNull(ClassUtil.rawClass(null));
        assertEquals(String.class, ClassUtil.rawClass(TypeFactory.defaultInstance().constructType(String.class)));
    }

    @Test(timeout = 4000)
    public void testPrimitiveSupport() {
        assertEquals(Integer.valueOf(0), ClassUtil.defaultValue(Integer.TYPE));
        assertEquals(Long.valueOf(0L), ClassUtil.defaultValue(Long.TYPE));
        assertEquals(Boolean.FALSE, ClassUtil.defaultValue(Boolean.TYPE));
        assertEquals(Double.valueOf(0.0), ClassUtil.defaultValue(Double.TYPE));
        assertEquals(Float.valueOf(0.0f), ClassUtil.defaultValue(Float.TYPE));
        assertEquals(Byte.valueOf((byte) 0), ClassUtil.defaultValue(Byte.TYPE));
        assertEquals(Short.valueOf((short) 0), ClassUtil.defaultValue(Short.TYPE));
        assertEquals(Character.valueOf('\0'), ClassUtil.defaultValue(Character.TYPE));

        try {
            ClassUtil.defaultValue(String.class);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

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
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        assertSame(Integer.TYPE, ClassUtil.primitiveType(Integer.class));
        assertSame(Integer.TYPE, ClassUtil.primitiveType(Integer.TYPE));
        assertNull(ClassUtil.primitiveType(String.class));
        assertNull(ClassUtil.primitiveType(Void.class));
    }

    @Test(timeout = 4000)
    public void testCheckAndFixAccess() throws Exception {
        Field field = AccessBean.class.getDeclaredField("secret");
        ClassUtil.checkAndFixAccess(field);
        assertTrue(field.isAccessible());

        Field field2 = AccessBean.class.getDeclaredField("secret");
        ClassUtil.checkAndFixAccess(field2, true);
        assertTrue(field2.isAccessible());

        Method method = AccessBean.class.getDeclaredMethod("hidden");
        ClassUtil.checkAndFixAccess(method, true);
        assertTrue(method.isAccessible());

        Constructor<PrivateCtor> ctor = PrivateCtor.class.getDeclaredConstructor();
        ClassUtil.checkAndFixAccess(ctor, true);
        assertTrue(ctor.isAccessible());

        Field publicField = FieldsBean.class.getDeclaredField("x");
        ClassUtil.checkAndFixAccess(publicField, false);
    }

    @Test(timeout = 4000)
    public void testEnumTypeDetection() {
        EnumSet<SampleEnum> nonEmpty = EnumSet.of(SampleEnum.B);
        assertEquals(SampleEnum.class, ClassUtil.findEnumType(nonEmpty));

        EnumSet<SampleEnum> empty = EnumSet.noneOf(SampleEnum.class);
        assertEquals(SampleEnum.class, ClassUtil.findEnumType(empty));

        EnumMap<SampleEnum, String> nonEmptyMap = new EnumMap<SampleEnum, String>(SampleEnum.class);
        nonEmptyMap.put(SampleEnum.A, "a");
        assertEquals(SampleEnum.class, ClassUtil.findEnumType(nonEmptyMap));

        EnumMap<SampleEnum, String> emptyMap = new EnumMap<SampleEnum, String>(SampleEnum.class);
        assertEquals(SampleEnum.class, ClassUtil.findEnumType(emptyMap));

        assertEquals(SampleEnum.class, ClassUtil.findEnumType(SampleEnum.A));
        assertEquals(SampleEnum.class, ClassUtil.findEnumType(SampleEnum.class));
        assertEquals(ComplexEnum.class, ClassUtil.findEnumType(ComplexEnum.A));
        assertEquals(ComplexEnum.class, ClassUtil.findEnumType(ComplexEnum.class));
    }

    @Test(timeout = 4000)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void testFindFirstAnnotatedEnumValue() {
        Enum<?> found = ClassUtil.findFirstAnnotatedEnumValue(
                (Class<Enum<?>>) (Class<?>) AnnotatedEnum.class, Deprecated.class);
        assertSame(AnnotatedEnum.A, found);

        Enum<?> none = ClassUtil.findFirstAnnotatedEnumValue(
                (Class<Enum<?>>) (Class<?>) SampleEnum.class, Deprecated.class);
        assertNull(none);
    }

    @Test(timeout = 4000)
    public void testJacksonStdImpl() {
        assertTrue(ClassUtil.isJacksonStdImpl((Object) null));
        assertTrue(ClassUtil.isJacksonStdImpl(new StdImplBean()));
        assertFalse(ClassUtil.isJacksonStdImpl(new PlainBean()));
        assertTrue(ClassUtil.isJacksonStdImpl(StdImplBean.class));
        assertFalse(ClassUtil.isJacksonStdImpl(PlainBean.class));
    }

    @Test(timeout = 4000)
    public void testClassAccessors() throws Exception {
        assertEquals("java.lang", ClassUtil.getPackageName(String.class));

        class LocalDummy { }
        assertFalse(ClassUtil.hasEnclosingMethod(String.class));
        assertTrue(ClassUtil.hasEnclosingMethod(LocalDummy.class));
        assertFalse(ClassUtil.hasEnclosingMethod(Object.class));
        assertFalse(ClassUtil.hasEnclosingMethod(int.class));

        Field[] fields = ClassUtil.getDeclaredFields(FieldsBean.class);
        Set<String> fieldNames = new HashSet<String>();
        for (Field f : fields) {
            fieldNames.add(f.getName());
        }
        assertTrue(fieldNames.contains("x"));
        assertTrue(fieldNames.contains("y"));

        Method[] methods = ClassUtil.getDeclaredMethods(MethodsBean.class);
        Set<String> methodNames = new HashSet<String>();
        for (Method m : methods) {
            methodNames.add(m.getName());
        }
        assertTrue(methodNames.contains("publicMethod"));
        assertTrue(methodNames.contains("getValue"));

        assertEquals(0, ClassUtil.findClassAnnotations(Object.class).length);
        assertEquals(0, ClassUtil.findClassAnnotations(int.class).length);
        assertTrue(ClassUtil.findClassAnnotations(AnnotatedBean.class).length > 0);

        assertTrue(ClassUtil.getClassMethods(MethodsBean.class).length >= 2);

        assertEquals(0, ClassUtil.getConstructors(List.class).length);
        assertEquals(0, ClassUtil.getConstructors(Object.class).length);
        assertEquals(1, ClassUtil.getConstructors(CtorBean.class).length);

        assertNull(ClassUtil.getDeclaringClass(String.class));
        assertEquals(Map.class, ClassUtil.getDeclaringClass(Map.Entry.class));
        assertNull(ClassUtil.getDeclaringClass(Object.class));

        assertNotNull(ClassUtil.getGenericSuperclass(GenericChild.class));
        assertTrue(ClassUtil.getGenericInterfaces(GenericChild.class).length > 0);

        assertEquals(Map.class, ClassUtil.getEnclosingClass(Map.Entry.class));
        assertNull(ClassUtil.getEnclosingClass(String.class));
        assertEquals(ClassUtilDeepseekTest.class, ClassUtil.getEnclosingClass(InnerMember.class));
    }

    @Test(timeout = 4000)
    public void testCtorWrapper() {
        ClassUtil.Ctor[] ctors = ClassUtil.getConstructors(CtorBean.class);
        assertEquals(1, ctors.length);

        ClassUtil.Ctor ctor = ctors[0];
        assertEquals(CtorBean.class, ctor.getDeclaringClass());
        assertEquals(2, ctor.getParamCount());
        assertNotNull(ctor.getConstructor());
        assertTrue(ctor.getDeclaredAnnotations().length > 0);
        assertEquals(2, ctor.getParameterAnnotations().length);

        assertSame(ctor.getDeclaredAnnotations(), ctor.getDeclaredAnnotations());
        assertSame(ctor.getParameterAnnotations(), ctor.getParameterAnnotations());
    }

    @Test(timeout = 4000)
    public void testExceptionMessageAvoidsDuplicateLocationInformation() throws Exception {
        Method exceptionMessage;
        try {
            exceptionMessage = ClassUtil.class.getDeclaredMethod("exceptionMessage", Throwable.class);
            exceptionMessage.setAccessible(true);
        } catch (NoSuchMethodException e) {
            fail("ClassUtil.exceptionMessage(Throwable) is missing; duplicate 'at [' marker bug is present");
            return;
        }

        IOException plain = new IOException("plain: at [one]");
        assertEquals("plain: at [one]", exceptionMessage.invoke(null, plain));

        final String original = "Cannot deserialize Map key of type `ABC` from String \"value\": "
                + "not a valid representation, problem: (InvalidFormatException) "
                + "Cannot deserialize Map key of type `ABC` from String \"value\": "
                + "not one of values excepted for Enum class: [A, B, C]";

        JsonProcessingException jpe = new JsonProcessingException(original) {
            @Override
            public String getMessage() {
                return original + " at [Source: (String)\"value\"; line: 1, column: 3]";
            }
        };

        String methodResult = (String) exceptionMessage.invoke(null, jpe);
        assertEquals(original, methodResult);

        String combined = methodResult + " at [Source: (String)\"value\"; line: 1, column: 3]";
        int count = countOccurrences(combined, "at [");
        assertEquals("Should only get one 'at [' marker, got " + count + ", source: " + combined,
                1, count);
    }

    private static int countOccurrences(String text, String token) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }
        return count;
    }
}