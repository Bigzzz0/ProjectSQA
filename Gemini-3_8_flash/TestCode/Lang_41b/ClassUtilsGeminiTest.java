package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.lang.ClassUtils
 *
 * Defect Zone (Defects4J Ground Truth):
 * - getShortClassName(Class): Defective handling of array types (e.g., String[].class returns "String;" instead of "String[]").
 * - getPackageName(Class): Defective handling of array types (e.g., String[].class returns "[Ljava.lang" instead of "java.lang").
 *
 * Branch & Boundary Coverage Matrix:
 * 1. Constructor: Public constructor instantiation for JavaBean compliance.
 * 2. getShortClassName(Object, String) & getShortClassName(Class) & getShortClassName(String):
 *    - null object, fallback values, null Class, primitive, standard Class, inner Class ($), array notation.
 * 3. getPackageName(Object, String) & getPackageName(Class) & getPackageName(String):
 *    - null object, fallback values, unpackaged class, packaged class, inner class, array notation.
 * 4. getAllSuperclasses(Class) & getAllInterfaces(Class):
 *    - null, Object.class, standard hierarchy, multi-interface inheritance, duplicate diamond interface hierarchy.
 * 5. convertClassNamesToClasses & convertClassesToClassNames:
 *    - null list, empty list, valid class names, invalid/unresolvable class names (ClassNotFoundException branch), null elements.
 * 6. isAssignable (Arrays & Single Class with autoboxing flag true/false):
 *    - null arrays, unequal length arrays, empty arrays, null element assignment to primitive vs reference.
 *    - primitive widenings: byte -> short/int/long/float/double; short/char -> int/long/float/double; int -> long/float/double;
 *      long -> float/double; float -> double; boolean and double non-widening paths.
 *    - autoboxing conversions: primitive to wrapper, wrapper to primitive.
 * 7. primitiveToWrapper & wrapperToPrimitive (single & array variants):
 *    - null input, empty arrays, non-primitive classes, non-wrapper classes, Void.TYPE handling.
 * 8. isInnerClass(Class):
 *    - null class, top-level class, static nested class, inner member class.
 * 9. getClass(ClassLoader, String, boolean) & Overloads:
 *    - primitive abbreviations ("int", "boolean", etc.), canonical arrays ("int[]", "java.lang.String[]"),
 *      multi-dimensional arrays ("int[][]"), unresolvable names (ClassNotFoundException).
 * 10. getPublicMethod(Class, String, Class[]):
 *    - method on public class, method on package-private class implementing public interface,
 *      missing method (NoSuchMethodException), null class (NullPointerException).
 * 11. toClass(Object[]):
 *    - null array, empty array, heterogeneous object array.
 * 12. getShortCanonicalName & getPackageCanonicalName (Object, Class, String variants):
 *    - JLS canonical forms, JVM array descriptors ("[I", "[Ljava.lang.String;"), null checks, whitespace deletion.
 */
public class ClassUtilsGeminiTest {

    // Helper classes and interfaces for hierarchy testing
    private interface InterfaceA {}
    private interface InterfaceB extends InterfaceA {}
    private interface InterfaceC {}

    private static class ClassA implements InterfaceB {}
    private static class ClassB extends ClassA implements InterfaceC {}

    private static class InnerClass {
        public static class NestedInnerClass {}
    }

    // Abstract package-private class to test public method lookup through interface
    static abstract class PackagePrivateClass implements Runnable {
        public void run() {}
    }

    // ==========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ==========================================================================

    @Test(timeout = 4000)
    public void testDefect_getShortClassName_ArrayClass() {
        // Known bug: String[].class returns "String;" instead of "String[]"
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
        assertEquals("boolean[][][]", ClassUtils.getShortClassName(boolean[][][].class));
    }

    @Test(timeout = 4000)
    public void testDefect_getPackageName_ArrayClass() {
        // Known bug: String[].class returns "[Ljava.lang" instead of "java.lang"
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        assertEquals("", ClassUtils.getPackageName(int[].class));
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
    }

    // ==========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ==========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new ClassUtils());
        Constructor<?>[] constructors = ClassUtils.class.getConstructors();
        assertEquals(1, constructors.length);
        assertTrue(Modifier.isPublic(constructors[0].getModifiers()));
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals('.', ClassUtils.PACKAGE_SEPARATOR_CHAR);
        assertEquals(".", ClassUtils.PACKAGE_SEPARATOR);
        assertEquals('$', ClassUtils.INNER_CLASS_SEPARATOR_CHAR);
        assertEquals("$", ClassUtils.INNER_CLASS_SEPARATOR);
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_Class() {
        assertEquals("ClassUtilsGeminiTest", ClassUtils.getShortClassName(ClassUtilsGeminiTest.class));
        assertEquals("InnerClass", ClassUtils.getShortClassName(InnerClass.class));
        assertEquals("InnerClass.NestedInnerClass", ClassUtils.getShortClassName(InnerClass.NestedInnerClass.class));
        assertEquals("int", ClassUtils.getShortClassName(int.class));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_Object() {
        assertEquals("String", ClassUtils.getShortClassName("Hello", "default"));
        assertEquals("default", ClassUtils.getShortClassName((Object) null, "default"));
        assertNull(ClassUtils.getShortClassName((Object) null, null));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_String() {
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("ClassUtils", ClassUtils.getShortClassName("ClassUtils"));
        assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
        assertEquals("Entry", ClassUtils.getShortClassName("Entry"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_Class() {
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtilsGeminiTest.class));
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(InnerClass.NestedInnerClass.class));
        assertEquals("", ClassUtils.getPackageName(int.class));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_Object() {
        assertEquals("java.lang", ClassUtils.getPackageName("Hello", "default"));
        assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
        assertNull(ClassUtils.getPackageName((Object) null, null));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_String() {
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageName("UnpackagedClass"));
        assertEquals("java.util", ClassUtils.getPackageName("java.util.Map$Entry"));
    }

    @Test(timeout = 4000)
    public void testGetAllSuperclasses() {
        assertNull(ClassUtils.getAllSuperclasses(null));

        List<Class<?>> objectSupers = ClassUtils.getAllSuperclasses(Object.class);
        assertEquals(0, objectSupers.size());

        List<Class<?>> bSupers = ClassUtils.getAllSuperclasses(ClassB.class);
        assertEquals(2, bSupers.size());
        assertEquals(ClassA.class, bSupers.get(0));
        assertEquals(Object.class, bSupers.get(1));
    }

    @Test(timeout = 4000)
    public void testGetAllInterfaces() {
        assertNull(ClassUtils.getAllInterfaces(null));

        List<Class<?>> emptyInterfaces = ClassUtils.getAllInterfaces(Object.class);
        assertEquals(0, emptyInterfaces.size());

        List<Class<?>> bInterfaces = ClassUtils.getAllInterfaces(ClassB.class);
        // ClassB implements InterfaceC; superclass ClassA implements InterfaceB extends InterfaceA
        assertEquals(3, bInterfaces.size());
        assertEquals(InterfaceC.class, bInterfaces.get(0));
        assertEquals(InterfaceB.class, bInterfaces.get(1));
        assertEquals(InterfaceA.class, bInterfaces.get(2));
    }

    @Test(timeout = 4000)
    public void testConvertClassNamesToClasses() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));

        List<String> names = new ArrayList<String>();
        names.add("java.lang.String");
        names.add("java.lang.NonExistentClass");
        names.add(null);

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertNull(classes.get(2));
    }

    @Test(timeout = 4000)
    public void testConvertClassesToClassNames() {
        assertNull(ClassUtils.convertClassesToClassNames(null));

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(String.class);
        classes.add(null);
        classes.add(Integer.class);

        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertEquals(3, names.size());
        assertEquals("java.lang.String", names.get(0));
        assertNull(names.get(1));
        assertEquals("java.lang.Integer", names.get(2));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_ArrayOverload() {
        assertFalse(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{String.class, Integer.class}));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, new Class<?>[0]));

        Class<?>[] from = new Class<?>[]{Integer.class, String.class};
        Class<?>[] to = new Class<?>[]{Number.class, Object.class};
        assertTrue(ClassUtils.isAssignable(from, to));

        Class<?>[] incompatible = new Class<?>[]{Double.class, String.class};
        assertFalse(ClassUtils.isAssignable(from, incompatible));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_Autoboxing_PrimitiveWidening() {
        // byte -> short, int, long, float, double
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        assertFalse(ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));

        // short -> int, long, float, double
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));
        assertFalse(ClassUtils.isAssignable(Short.TYPE, Character.TYPE));

        // char -> int, long, float, double
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Character.TYPE, Short.TYPE));
        assertFalse(ClassUtils.isAssignable(Character.TYPE, Byte.TYPE));

        // int -> long, float, double
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));

        // long -> float, double
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));

        // float -> double
        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Float.TYPE, Long.TYPE));

        // boolean & double cannot widen
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_AutoboxingFlag() {
        // Without autoboxing
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));

        // With autoboxing
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Number.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Long.TYPE, true));
        assertFalse(ClassUtils.isAssignable(Integer.class, String.class, true));

        // null handling with autoboxing
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE, true));
        assertTrue(ClassUtils.isAssignable(null, Integer.class, true));
        assertFalse(ClassUtils.isAssignable(Integer.class, null, true));
    }

    @Test(timeout = 4000)
    public void testPrimitiveAndWrapperConversions() {
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));

        assertNull(ClassUtils.primitivesToWrappers(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));
        assertArrayEquals(new Class<?>[]{Integer.class, String.class},
                ClassUtils.primitivesToWrappers(new Class<?>[]{Integer.TYPE, String.class}));

        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.TYPE));

        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        assertArrayEquals(new Class<?>[]{Integer.TYPE, null},
                ClassUtils.wrappersToPrimitives(new Class<?>[]{Integer.class, String.class}));
    }

    @Test(timeout = 4000)
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(InnerClass.class));
        assertTrue(ClassUtils.isInnerClass(InnerClass.NestedInnerClass.class));
    }

    @Test(timeout = 4000)
    public void testGetClass() throws ClassNotFoundException {
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(boolean.class, ClassUtils.getClass("boolean"));
        assertEquals(byte.class, ClassUtils.getClass("byte"));
        assertEquals(char.class, ClassUtils.getClass("char"));
        assertEquals(short.class, ClassUtils.getClass("short"));
        assertEquals(long.class, ClassUtils.getClass("long"));
        assertEquals(float.class, ClassUtils.getClass("float"));
        assertEquals(double.class, ClassUtils.getClass("double"));

        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(int[][].class, ClassUtils.getClass("int[][]"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(String[][].class, ClassUtils.getClass("java.lang.String[][]"));

        ClassLoader cl = ClassUtilsGeminiTest.class.getClassLoader();
        assertEquals(String.class, ClassUtils.getClass(cl, "java.lang.String"));
        assertEquals(String.class, ClassUtils.getClass(cl, "java.lang.String", true));
        assertEquals(String.class, ClassUtils.getClass("java.lang.String", true));
    }

    @Test(expected = ClassNotFoundException.class, timeout = 4000)
    public void testGetClass_NotFound() throws ClassNotFoundException {
        ClassUtils.getClass("org.apache.commons.lang.NonExistentClassXYZ");
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod_Standard() throws Exception {
        Method method = ClassUtils.getPublicMethod(String.class, "indexOf", new Class<?>[]{String.class});
        assertNotNull(method);
        assertEquals("indexOf", method.getName());
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod_ThroughInterface() throws Exception {
        Set<?> unmodifiable = Collections.unmodifiableSet(new HashSet<Object>());
        Method method = ClassUtils.getPublicMethod(unmodifiable.getClass(), "isEmpty", new Class<?>[0]);
        assertNotNull(method);
        assertTrue(Modifier.isPublic(method.getDeclaringClass().getModifiers()));
    }

    @Test(timeout = 4000)
    public void testToClass() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.toClass(new Object[0]));

        Object[] array = new Object[]{"text", 123, true};
        Class<?>[] result = ClassUtils.toClass(array);
        assertEquals(3, result.length);
        assertEquals(String.class, result[0]);
        assertEquals(Integer.class, result[1]);
        assertEquals(Boolean.class, result[2]);
    }

    // ==========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ==========================================================================

    @Test(timeout = 4000)
    public void testCanonicalNameMethods_NullAndEmpty() {
        assertEquals("", ClassUtils.getShortCanonicalName((Object) null, ""));
        assertEquals("nullVal", ClassUtils.getShortCanonicalName((Object) null, "nullVal"));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        assertEquals("", ClassUtils.getShortCanonicalName(""));

        assertEquals("", ClassUtils.getPackageCanonicalName((Object) null, ""));
        assertEquals("nullVal", ClassUtils.getPackageCanonicalName((Object) null, "nullVal"));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        assertEquals("", ClassUtils.getPackageCanonicalName(""));
    }

    @Test(timeout = 4000)
    public void testCanonicalNameMethods_PrimitivesAndArrays() {
        assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], ""));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName(new int[0][0], ""));
        assertEquals("String[]", ClassUtils.getShortCanonicalName(new String[0], ""));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String")); // non-standard without semicolon
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));

        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0], "default"));
        assertEquals("", ClassUtils.getPackageCanonicalName(new int[0][0], "default"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(new String[0], "default"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
    }

    @Test(timeout = 4000)
    public void testCanonicalNameMethods_ObjectsAndInnerClasses() {
        InnerClass inner = new InnerClass();
        assertEquals("ClassUtilsGeminiTest.InnerClass", ClassUtils.getShortCanonicalName(inner, ""));
        assertEquals("ClassUtilsGeminiTest.InnerClass", ClassUtils.getShortCanonicalName(InnerClass.class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(inner, ""));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageCanonicalName(InnerClass.class));
    }

    // ==========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ==========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetPublicMethod_NullClass() throws Exception {
        ClassUtils.getPublicMethod(null, "someMethod", new Class<?>[0]);
    }

    @Test(expected = NoSuchMethodException.class, timeout = 4000)
    public void testGetPublicMethod_NoSuchMethod() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethod", new Class<?>[0]);
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod_NoPublicImplementationThrowsNoSuchMethod() {
        try {
            ClassUtils.getPublicMethod(PackagePrivateClass.class, "nonExistentMethod", new Class<?>[0]);
            fail("Expected NoSuchMethodException");
        } catch (NoSuchMethodException ex) {
            assertTrue(ex.getMessage().contains("Can't find a public method"));
        }
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testConvertClassNamesToClasses_ClassCastException() {
        @SuppressWarnings("rawtypes")
        List rawList = Arrays.asList(Integer.valueOf(1));
        @SuppressWarnings("unchecked")
        List<String> invalidList = (List<String>) rawList;
        ClassUtils.convertClassNamesToClasses(invalidList);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testConvertClassesToClassNames_ClassCastException() {
        @SuppressWarnings("rawtypes")
        List rawList = Arrays.asList("NotAClass");
        @SuppressWarnings("unchecked")
        List<Class<?>> invalidList = (List<Class<?>>) rawList;
        ClassUtils.convertClassesToClassNames(invalidList);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetClass_NullName() throws ClassNotFoundException {
        ClassUtils.getClass((String) null);
    }

    // ==========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ==========================================================================

    @Test(timeout = 4000)
    public void testIsAssignable_PrimitivesDirect() {
        // Identity conversions
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Void.TYPE, Void.TYPE));

        // Primitive to non-primitive without autoboxing is always false
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Object.class, Integer.TYPE, false));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_NullChecks() {
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertFalse(ClassUtils.isAssignable(null, int.class));
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertTrue(ClassUtils.isAssignable((Class<?>) null, (Class<?>) null));
    }
}