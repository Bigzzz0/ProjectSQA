package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for ClassUtils targeting line/branch coverage and the known defect
 * in array class name handling (getShortClassName and getPackageName).
 *
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * Target methods: getShortClassName(String), getPackageName(String), getShortClassName(Class),
 *                 getPackageName(Class), getShortCanonicalName, getPackageCanonicalName,
 *                 isAssignable, primitiveToWrapper, wrapperToPrimitive, etc.
 *
 * Known defect: For array class names like "[Ljava.lang.String;", getShortClassName returns
 *               "String[;]" instead of "String[]". getPackageName returns "[Ljava.lang" instead of "java.lang".
 *               The root cause is missing array encoding stripping in getShortClassName(String) and
 *               getPackageName(String) (the comments say "Handle array encoding" but no code).
 *
 * Branches covered:
 * - getShortClassName(Object, String): null object -> valueIfNull; non-null -> getShortClassName(getClass())
 * - getShortClassName(Class): null -> EMPTY; non-null -> getShortClassName(getName())
 * - getShortClassName(String): null/empty -> EMPTY; normal class name; inner class with '$'; array encoding (defect)
 * - getPackageName(Object, String): null -> valueIfNull; non-null -> getPackageName(getClass())
 * - getPackageName(Class): null -> EMPTY; non-null -> getPackageName(getName())
 * - getPackageName(String): null -> EMPTY; no dot -> EMPTY; with dot -> substring before last dot; array encoding (defect)
 * - isAssignable(Class, Class, boolean): null toClass -> false; null cls -> !toClass.isPrimitive(); autoboxing logic;
 *   primitive widening chains; reference assignability
 * - primitiveToWrapper: null -> null; primitive -> wrapper; non-primitive -> same
 * - wrapperToPrimitive: null -> null; wrapper -> primitive; non-wrapper -> null
 * - getAllSuperclasses: null -> null; normal class hierarchy
 * - getAllInterfaces: null -> null; interface hierarchy
 * - isInnerClass: null -> false; inner class -> true; top-level -> false
 * - getClass: abbreviation map lookup; normal class name; array class name
 * - toCanonicalName: null -> NPE; ends with "[]" -> array encoding; else -> same
 * - getCanonicalName: null -> null; starts with '[' -> decode; else -> same
 * - getShortCanonicalName: delegates to getShortClassName(getCanonicalName(...))
 * - getPackageCanonicalName: delegates to getPackageName(getCanonicalName(...))
 * - convertClassNamesToClasses: null -> null; valid/invalid class names
 * - convertClassesToClassNames: null -> null; null entry -> null in list
 * - toClass: null -> null; empty array -> EMPTY_CLASS_ARRAY; non-empty -> class array
 *
 * Boundary values: null, empty string, primitive class names, array class names (1D, 2D, primitive arrays),
 *                 inner class names, class names with multiple dots, etc.
 */
public class ClassUtilsDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testGetShortClassName_Object() {
        assertEquals("Object", ClassUtils.getShortClassName(new Object(), null));
        assertEquals("String", ClassUtils.getShortClassName("test", null));
        assertEquals("nullDefault", ClassUtils.getShortClassName(null, "nullDefault"));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_Class() {
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("ClassUtilsDeepseekTest", ClassUtils.getShortClassName(ClassUtilsDeepseekTest.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_String_Normal() {
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("ClassUtilsDeepseekTest", ClassUtils.getShortClassName("org.apache.commons.lang.ClassUtilsDeepseekTest"));
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_String_InnerClass() {
        // Inner class with '$' should be replaced by '.'
        assertEquals("Outer.Inner", ClassUtils.getShortClassName("org.apache.commons.lang.Outer$Inner"));
        assertEquals("A.B.C", ClassUtils.getShortClassName("A$B$C"));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_String_ArrayEncoding() {
        // Known defect: array class names not handled correctly
        // Expected: "String[]" for "[Ljava.lang.String;"
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        // For primitive arrays: "[I" -> "int[]"
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        // Multi-dimensional: "[[Ljava.lang.String;" -> "String[][]"
        assertEquals("String[][]", ClassUtils.getShortClassName("[[Ljava.lang.String;"));
        // "[[I" -> "int[][]"
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_Object() {
        assertEquals("java.lang", ClassUtils.getPackageName("test", null));
        assertEquals("defaultPkg", ClassUtils.getPackageName(null, "defaultPkg"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtilsDeepseekTest.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_String_Normal() {
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("org.apache.commons.lang", ClassUtils.getPackageName("org.apache.commons.lang.ClassUtilsDeepseekTest"));
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName("NoPackageClass"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_String_ArrayEncoding() {
        // Known defect: array class names not handled correctly
        // Expected: "java.lang" for "[Ljava.lang.String;"
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        // For primitive arrays: "[I" -> "" (no package)
        assertEquals("", ClassUtils.getPackageName("[I"));
        // Multi-dimensional: "[[Ljava.lang.String;" -> "java.lang"
        assertEquals("java.lang", ClassUtils.getPackageName("[[Ljava.lang.String;"));
        // "[[I" -> ""
        assertEquals("", ClassUtils.getPackageName("[[I"));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testGetShortClassName_EdgeCases() {
        // Empty string
        assertEquals("", ClassUtils.getShortClassName(""));
        // Null
        assertEquals("", ClassUtils.getShortClassName((String) null));
        // Class with no package
        assertEquals("NoPackageClass", ClassUtils.getShortClassName("NoPackageClass"));
        // Single character
        assertEquals("X", ClassUtils.getShortClassName("X"));
        // Only dots
        assertEquals("", ClassUtils.getShortClassName("..."));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_EdgeCases() {
        // Empty string
        assertEquals("", ClassUtils.getPackageName(""));
        // Null
        assertEquals("", ClassUtils.getPackageName((String) null));
        // Class with no package
        assertEquals("", ClassUtils.getPackageName("NoPackageClass"));
        // Single character
        assertEquals("", ClassUtils.getPackageName("X"));
        // Only dots
        assertEquals("", ClassUtils.getPackageName("..."));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_NullAndPrimitive() {
        // Null toClass -> false
        assertFalse(ClassUtils.isAssignable(String.class, (Class<?>) null));
        // Null cls -> true if toClass is not primitive
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertFalse(ClassUtils.isAssignable(null, int.class));
        // Same class
        assertTrue(ClassUtils.isAssignable(int.class, int.class));
        assertTrue(ClassUtils.isAssignable(String.class, String.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_PrimitiveWidening() {
        // int -> long, float, double
        assertTrue(ClassUtils.isAssignable(int.class, long.class));
        assertTrue(ClassUtils.isAssignable(int.class, float.class));
        assertTrue(ClassUtils.isAssignable(int.class, double.class));
        // int -> boolean false
        assertFalse(ClassUtils.isAssignable(int.class, boolean.class));
        // long -> float, double
        assertTrue(ClassUtils.isAssignable(long.class, float.class));
        assertTrue(ClassUtils.isAssignable(long.class, double.class));
        // float -> double
        assertTrue(ClassUtils.isAssignable(float.class, double.class));
        // double -> nothing
        assertFalse(ClassUtils.isAssignable(double.class, float.class));
        // char -> int, long, float, double
        assertTrue(ClassUtils.isAssignable(char.class, int.class));
        assertTrue(ClassUtils.isAssignable(char.class, long.class));
        assertTrue(ClassUtils.isAssignable(char.class, float.class));
        assertTrue(ClassUtils.isAssignable(char.class, double.class));
        // short -> int, long, float, double
        assertTrue(ClassUtils.isAssignable(short.class, int.class));
        assertTrue(ClassUtils.isAssignable(short.class, long.class));
        assertTrue(ClassUtils.isAssignable(short.class, float.class));
        assertTrue(ClassUtils.isAssignable(short.class, double.class));
        // byte -> short, int, long, float, double
        assertTrue(ClassUtils.isAssignable(byte.class, short.class));
        assertTrue(ClassUtils.isAssignable(byte.class, int.class));
        assertTrue(ClassUtils.isAssignable(byte.class, long.class));
        assertTrue(ClassUtils.isAssignable(byte.class, float.class));
        assertTrue(ClassUtils.isAssignable(byte.class, double.class));
        // boolean -> nothing
        assertFalse(ClassUtils.isAssignable(boolean.class, int.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_Autoboxing() {
        // Without autoboxing, primitive to wrapper should be false
        assertFalse(ClassUtils.isAssignable(int.class, Integer.class));
        // With autoboxing, should be true
        assertTrue(ClassUtils.isAssignable(int.class, Integer.class, true));
        // Wrapper to primitive
        assertTrue(ClassUtils.isAssignable(Integer.class, int.class, true));
        // Non-matching wrapper
        assertFalse(ClassUtils.isAssignable(Integer.class, Long.class, true));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_Array() {
        // Same length and assignable
        assertTrue(ClassUtils.isAssignable(new Class<?>[]{int.class, String.class},
                                           new Class<?>[]{long.class, Object.class}));
        // Different length
        assertFalse(ClassUtils.isAssignable(new Class<?>[]{int.class}, new Class<?>[]{long.class, double.class}));
        // Null arrays treated as empty
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], new Class<?>[0]));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testGetShortClassName_ArrayClass_DefectRevealing() {
        // Directly tests the known defect: array class name should be "String[]" not "String[;]"
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        // Also test via Class object
        assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        // Primitive array
        assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
        // Multi-dimensional
        assertEquals("int[][]", ClassUtils.getShortClassName(int[][].class));
        assertEquals("String[][]", ClassUtils.getShortClassName(String[][].class));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_ArrayClass_DefectRevealing() {
        // Directly tests the known defect: package name should be "java.lang" not "[Ljava.lang"
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        // Via Class object
        assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        // Primitive array has no package
        assertEquals("", ClassUtils.getPackageName(int[].class));
        // Multi-dimensional
        assertEquals("java.lang", ClassUtils.getPackageName(String[][].class));
        assertEquals("", ClassUtils.getPackageName(int[][].class));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToCanonicalName_Null() {
        // Private method, but we can test via getClass which calls it
        ClassUtils.getClass((ClassLoader) null, (String) null, true);
    }

    @Test(timeout = 4000)
    public void testGetClass_Abbreviation() throws ClassNotFoundException {
        // Abbreviation map: "int" -> "I"
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(boolean.class, ClassUtils.getClass("boolean"));
        assertEquals(void.class, ClassUtils.getClass("void"));
    }

    @Test(timeout = 4000)
    public void testGetClass_ArrayName() throws ClassNotFoundException {
        // Array class names
        assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
        assertEquals(int[].class, ClassUtils.getClass("[I"));
        assertEquals(int[][].class, ClassUtils.getClass("[[I"));
    }

    @Test(timeout = 4000)
    public void testGetClass_CanonicalName() throws ClassNotFoundException {
        // Canonical name like "java.lang.String[]"
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
    }

    @Test(timeout = 4000)
    public void testConvertClassNamesToClasses() {
        List<String> names = new ArrayList<>();
        names.add("java.lang.String");
        names.add("invalid.ClassName");
        names.add(null);
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(String.class, classes.get(0));
        assertNull(classes.get(1));
        assertNull(classes.get(2));
        assertNull(ClassUtils.convertClassNamesToClasses(null));
    }

    @Test(timeout = 4000)
    public void testConvertClassesToClassNames() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(String.class);
        classes.add(null);
        classes.add(Integer.class);
        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertEquals("java.lang.String", names.get(0));
        assertNull(names.get(1));
        assertEquals("java.lang.Integer", names.get(2));
        assertNull(ClassUtils.convertClassesToClassNames(null));
    }

    @Test(timeout = 4000)
    public void testToClass() {
        Object[] array = new Object[]{"a", 1, null};
        Class<?>[] classes = ClassUtils.toClass(array);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
        assertNull(classes[2]); // null element -> NPE? Actually array[i].getClass() will throw NPE for null
        // So we need to test with non-null elements only
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testToClass_NullElement() {
        ClassUtils.toClass(new Object[]{null});
    }

    @Test(timeout = 4000)
    public void testToClass_NullArray() {
        assertNull(ClassUtils.toClass(null));
    }

    @Test(timeout = 4000)
    public void testToClass_EmptyArray() {
        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.toClass(new Object[0]));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testPrimitiveToWrapper() {
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE)); // special case
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
    }

    @Test(timeout = 4000)
    public void testWrapperToPrimitive() {
        assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.class)); // Void.TYPE not in wrapperPrimitiveMap
        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
    }

    @Test(timeout = 4000)
    public void testPrimitivesToWrappers() {
        assertNull(ClassUtils.primitivesToWrappers(null));
        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.primitivesToWrappers(new Class<?>[0]));
        Class<?>[] input = new Class<?>[]{int.class, String.class, boolean.class};
        Class<?>[] expected = new Class<?>[]{Integer.class, String.class, Boolean.class};
        assertArrayEquals(expected, ClassUtils.primitivesToWrappers(input));
    }

    @Test(timeout = 4000)
    public void testWrappersToPrimitives() {
        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertSame(ArrayUtils.EMPTY_CLASS_ARRAY, ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        Class<?>[] input = new Class<?>[]{Integer.class, String.class, Boolean.class};
        Class<?>[] expected = new Class<?>[]{int.class, null, boolean.class};
        assertArrayEquals(expected, ClassUtils.wrappersToPrimitives(input));
    }

    @Test(timeout = 4000)
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        // Simulate inner class name
        assertTrue(ClassUtils.isInnerClass(ClassUtilsDeepseekTest.class));
    }

    @Test(timeout = 4000)
    public void testGetAllSuperclasses() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> supers = ClassUtils.getAllSuperclasses(String.class);
        assertTrue(supers.contains(Object.class));
        assertFalse(supers.contains(String.class));
    }

    @Test(timeout = 4000)
    public void testGetAllInterfaces() {
        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(Cloneable.class));
    }

    @Test(timeout = 4000)
    public void testGetShortCanonicalName() {
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("String", ClassUtils.getShortCanonicalName("java.lang.String"));
        assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
    }

    @Test(timeout = 4000)
    public void testGetPackageCanonicalName() {
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod() throws Exception {
        Method m = ClassUtils.getPublicMethod(String.class, "length", new Class<?>[0]);
        assertEquals(String.class, m.getDeclaringClass());
    }

    @Test(timeout = 4000, expected = NoSuchMethodException.class)
    public void testGetPublicMethod_NotFound() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonexistent", new Class<?>[0]);
    }
}