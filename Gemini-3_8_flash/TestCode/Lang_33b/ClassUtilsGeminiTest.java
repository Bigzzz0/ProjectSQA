/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import org.junit.Test;

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

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET METHOD                | BRANCH / CONDITION TARGETED                          | DEFECT / COVERAGE
 * ----------------------------------------------------------------------------------------------------
 * toClass(Object[])            | array[i] == null in loop                             | DEFECT: NPE on null element
 * toClass(Object[])            | array == null, array.length == 0, valid array        | Full partition coverage
 * isAssignable(Class, Class)   | Widening primitives (Byte, Short, Char, Int, Long)   | All 8 primitive branches
 * isAssignable(Class, Class)   | Autoboxing true/false, primitive to wrapper & vice   | All autobox permutations
 * isAssignable(Class, Class)   | null checks (cls == null, toClass == null)           | Primitive vs reference null
 * isAssignable(Class[], ...)   | Array lengths differ, one/both null arrays           | ArrayUtils boundary parity
 * getShortClassName(...)       | Object, Class, String inputs; null, empty, arrays    | Multi-dim arrays, inner classes
 * getPackageName(...)          | Stripping '[', 'L...;', unpackaged classes           | Package extraction branches
 * getCanonicalName / Canonical | Abbreviation map, dim counters, bracket stripping    | JLS canonical format
 * getClass(...)                | Primitive abbreviations, arrays ('[]'), classLoader  | Class.forName and resolution
 * getPublicMethod(...)         | Declared vs interface vs superclass resolution       | Bug 4071957 workaround check
 * getAllSuperclasses/Interfaces| Class hierarchy traversal, null guards, interfaces   | Cyclic / duplicate protection
 * primitivesToWrappers / vice  | Null arrays, empty arrays, mixed arrays              | Bi-directional mapping tests
 * ----------------------------------------------------------------------------------------------------
 */
public class ClassUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth: Defects4J)
    // =========================================================================

    /**
     * Targets Defects4J known failure: ClassUtils.toClass(Object[]) throwing
     * NullPointerException when an element in the input array is null.
     */
    @Test(timeout = 4000)
    public void testToClass_objectArrayContainingNull_shouldYieldNullElement() {
        Object[] input = new Object[]{"stringVal", null, Integer.valueOf(42)};
        Class<?>[] result = ClassUtils.toClass(input);

        assertNotNull("Resulting class array must not be null", result);
        assertEquals("Resulting array length must match input", 3, result.length);
        assertEquals("First element should be String.class", String.class, result[0]);
        assertNull("Second element must be null without throwing NullPointerException", result[1]);
        assertEquals("Third element should be Integer.class", Integer.class, result[2]);
    }

    /**
     * Targets single-element null array to isolate the NPE defect boundary.
     */
    @Test(timeout = 4000)
    public void testToClass_singleNullElement() {
        Object[] input = new Object[]{null};
        Class<?>[] result = ClassUtils.toClass(input);

        assertNotNull("Resulting array must not be null", result);
        assertEquals(1, result.length);
        assertNull("Null object element must map to null Class", result[0]);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        ClassUtils instance = new ClassUtils();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals('.', ClassUtils.PACKAGE_SEPARATOR_CHAR);
        assertEquals(".", ClassUtils.PACKAGE_SEPARATOR);
        assertEquals('$', ClassUtils.INNER_CLASS_SEPARATOR_CHAR);
        assertEquals("$", ClassUtils.INNER_CLASS_SEPARATOR);
    }

    @Test(timeout = 4000)
    public void testPrimitiveToWrapper() {
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
        assertNull(ClassUtils.primitiveToWrapper(null));
    }

    @Test(timeout = 4000)
    public void testWrapperToPrimitive() {
        assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.class));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertNull(ClassUtils.wrapperToPrimitive(null));
    }

    @Test(timeout = 4000)
    public void testPrimitivesToWrappers() {
        assertNull(ClassUtils.primitivesToWrappers(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));

        Class<?>[] primitives = new Class<?>[]{Integer.TYPE, String.class, Double.TYPE};
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        assertNotNull(wrappers);
        assertEquals(3, wrappers.length);
        assertEquals(Integer.class, wrappers[0]);
        assertEquals(String.class, wrappers[1]);
        assertEquals(Double.class, wrappers[2]);
    }

    @Test(timeout = 4000)
    public void testWrappersToPrimitives() {
        assertNull(ClassUtils.wrappersToPrimitives(null));
        assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));

        Class<?>[] wrappers = new Class<?>[]{Integer.class, String.class, Double.class};
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        assertNotNull(primitives);
        assertEquals(3, primitives.length);
        assertEquals(Integer.TYPE, primitives[0]);
        assertNull(primitives[1]);
        assertEquals(Double.TYPE, primitives[2]);
    }

    @Test(timeout = 4000)
    public void testIsAssignable_PrimitiveWidening() {
        // Byte widenings: Short, Integer, Long, Float, Double
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        assertFalse(ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));

        // Short widenings: Integer, Long, Float, Double
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));

        // Character widenings: Integer, Long, Float, Double
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Character.TYPE, Short.TYPE));

        // Integer widenings: Long, Float, Double
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));

        // Long widenings: Float, Double
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));

        // Float widening: Double
        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Float.TYPE, Float.class, false));
        assertFalse(ClassUtils.isAssignable(Float.TYPE, Long.TYPE));

        // Boolean & Double: cannot be widened to other primitives
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_Autoboxing() {
        // Autoboxing enabled (default in Java >= 1.5)
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.class, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Number.class));

        // Explicit autoboxing disabled
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Object.class, false));
        assertFalse(ClassUtils.isAssignable(Object.class, Integer.TYPE, false));

        // Unboxing a non-wrapper reference into primitive
        assertFalse(ClassUtils.isAssignable(String.class, Integer.TYPE, true));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_ReferenceHierarchy() {
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertTrue(ClassUtils.isAssignable(ArrayList.class, List.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));
        assertFalse(ClassUtils.isAssignable(List.class, ArrayList.class));
    }

    @Test(timeout = 4000)
    public void testIsAssignable_Arrays() {
        Class<?>[] from = new Class<?>[]{Integer.TYPE, String.class};
        Class<?>[] to = new Class<?>[]{Long.TYPE, Object.class};
        assertTrue(ClassUtils.isAssignable(from, to));
        assertTrue(ClassUtils.isAssignable(from, to, true));

        Class<?>[] mismatch = new Class<?>[]{Double.TYPE};
        assertFalse(ClassUtils.isAssignable(from, mismatch));
        assertFalse(ClassUtils.isAssignable(from, null));
        assertFalse(ClassUtils.isAssignable(null, to));
        assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        assertTrue(ClassUtils.isAssignable(new Class<?>[0], new Class<?>[0]));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName() {
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("ClassUtilsGeminiTest", ClassUtils.getShortClassName(ClassUtilsGeminiTest.class));
        assertEquals("Map.Entry", ClassUtils.getShortClassName(Map.Entry.class));

        // Object variant
        assertEquals("String", ClassUtils.getShortClassName("Hello", "default"));
        assertEquals("default", ClassUtils.getShortClassName(null, "default"));

        // Class variant null
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));

        // Array representations
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        assertEquals("String[][]", ClassUtils.getShortClassName("[[Ljava.lang.String;"));
        assertEquals("Thread.State", ClassUtils.getShortClassName("java.lang.Thread$State"));
        assertEquals("Inner", ClassUtils.getShortClassName("Unpackaged$Inner"));
        assertEquals("Standalone", ClassUtils.getShortClassName("Standalone"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName() {
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("java.util", ClassUtils.getPackageName(Map.Entry.class));
        assertEquals("", ClassUtils.getPackageName("UnpackagedClass"));

        // Object variant
        assertEquals("java.lang", ClassUtils.getPackageName("Test", "default"));
        assertEquals("default", ClassUtils.getPackageName(null, "default"));

        // Class variant null
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));

        // Array types
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageName("[[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageName("[I"));
    }

    @Test(timeout = 4000)
    public void testCanonicalNames() {
        assertEquals("String", ClassUtils.getShortCanonicalName("java.lang.String"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName(new String[0], "def"));
        assertEquals("def", ClassUtils.getShortCanonicalName(null, "def"));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));

        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(new String[0], "def"));
        assertEquals("def", ClassUtils.getPackageCanonicalName(null, "def"));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
    }

    @Test(timeout = 4000)
    public void testGetAllSuperclasses() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> supers = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertNotNull(supers);
        assertTrue(supers.contains(java.util.AbstractList.class));
        assertTrue(supers.contains(java.util.AbstractCollection.class));
        assertTrue(supers.contains(Object.class));

        List<Class<?>> objectSupers = ClassUtils.getAllSuperclasses(Object.class);
        assertNotNull(objectSupers);
        assertEquals(0, objectSupers.size());
    }

    @Test(timeout = 4000)
    public void testGetAllInterfaces() {
        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertNotNull(interfaces);
        assertTrue(interfaces.contains(List.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(Cloneable.class));
        assertTrue(interfaces.contains(java.io.Serializable.class));

        // Interface itself
        List<Class<?>> listInterfaces = ClassUtils.getAllInterfaces(List.class);
        assertTrue(listInterfaces.contains(java.util.Collection.class));
        assertTrue(listInterfaces.contains(Iterable.class));
    }

    @Test(timeout = 4000)
    public void testConvertClassNamesToClasses() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));

        List<String> names = new ArrayList<String>();
        names.add("java.lang.String");
        names.add("java.lang.NonExistentClassXYZ");
        names.add(null);

        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertNotNull(classes);
        assertEquals(3, classes.size());
        assertEquals(String.class, classes[0]);
        assertNull(classes[1]);
        assertNull(classes[2]);
    }

    @Test(timeout = 4000)
    public void testConvertClassesToClassNames() {
        assertNull(ClassUtils.convertClassesToClassNames(null));

        List<Class<?>> classes = new ArrayList<Class<?>>();
        classes.add(String.class);
        classes.add(null);
        classes.add(Integer.class);

        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertNotNull(names);
        assertEquals(3, names.size());
        assertEquals("java.lang.String", names[0]);
        assertNull(names[1]);
        assertEquals("java.lang.Integer", names[2]);
    }

    @Test(timeout = 4000)
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        assertTrue(ClassUtils.isInnerClass(Thread.State.class));
    }

    @Test(timeout = 4000)
    public void testGetClass() throws ClassNotFoundException {
        // Primitives
        assertEquals(int.class, ClassUtils.getClass("int"));
        assertEquals(boolean.class, ClassUtils.getClass("boolean"));
        assertEquals(byte.class, ClassUtils.getClass("byte"));
        assertEquals(char.class, ClassUtils.getClass("char"));
        assertEquals(short.class, ClassUtils.getClass("short"));
        assertEquals(long.class, ClassUtils.getClass("long"));
        assertEquals(float.class, ClassUtils.getClass("float"));
        assertEquals(double.class, ClassUtils.getClass("double"));

        // Standard objects & arrays
        assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
        assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        assertEquals(int[].class, ClassUtils.getClass("int[]"));
        assertEquals(int[][].class, ClassUtils.getClass("int[][]"));
        assertEquals(String[][].class, ClassUtils.getClass("java.lang.String[][]"));

        // ClassLoader overload
        ClassLoader cl = ClassUtilsGeminiTest.class.getClassLoader();
        assertEquals(String.class, ClassUtils.getClass(cl, "java.lang.String"));
        assertEquals(int.class, ClassUtils.getClass(cl, "int", false));
        assertEquals(String[].class, ClassUtils.getClass(cl, "java.lang.String[]", false));
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod_StandardMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(String.class, "indexOf", new Class<?>[]{String.class});
        assertNotNull(method);
        assertEquals("indexOf", method.getName());
        assertTrue(Modifier.isPublic(method.getModifiers()));
    }

    @Test(timeout = 4000)
    public void testGetPublicMethod_InterfaceResolution() throws Exception {
        // Package-private Collections$UnmodifiableSet implements Set
        Set<String> normalSet = new HashSet<String>();
        Set<String> unmodifiable = Collections.unmodifiableSet(normalSet);

        Method method = ClassUtils.getPublicMethod(unmodifiable.getClass(), "isEmpty", new Class<?>[0]);
        assertNotNull(method);
        assertEquals("isEmpty", method.getName());
        assertTrue(Modifier.isPublic(method.getDeclaringClass().getModifiers()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testToClass_NullAndEmpty() {
        assertNull("Null input array should yield null", ClassUtils.toClass(null));
        Class<?>[] empty = ClassUtils.toClass(new Object[0]);
        assertNotNull("Empty input array should yield non-null empty array", empty);
        assertEquals(0, empty.length);
    }

    @Test(timeout = 4000)
    public void testIsAssignable_NullClassBoundaries() {
        // null target class
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, null));

        // null source class -> can be assigned to reference type, but not primitive
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertTrue(ClassUtils.isAssignable(null, Object.class));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(null, Boolean.TYPE));
    }

    @Test(timeout = 4000)
    public void testGetShortClassName_MalformedAndEdgeCases() {
        assertEquals("[]", ClassUtils.getShortClassName("[]"));
        assertEquals("[L;", ClassUtils.getShortClassName("[L;"));
        assertEquals("String", ClassUtils.getShortClassName("Ljava.lang.String;"));
        assertEquals("", ClassUtils.getShortClassName("."));
        assertEquals("Bar", ClassUtils.getShortClassName("Foo$Bar"));
    }

    @Test(timeout = 4000)
    public void testGetPackageName_EdgeCases() {
        assertEquals("", ClassUtils.getPackageName("L;"));
        assertEquals("", ClassUtils.getPackageName("NoPackage"));
        assertEquals("", ClassUtils.getPackageName("."));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = ClassNotFoundException.class, timeout = 4000)
    public void testGetClass_NotFound() throws ClassNotFoundException {
        ClassUtils.getClass("com.invalid.nonexistent.ClassXYZ");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetClass_NullName() throws ClassNotFoundException {
        ClassUtils.getClass((String) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetPublicMethod_NullClass() throws Exception {
        ClassUtils.getPublicMethod(null, "toString", new Class<?>[0]);
    }

    @Test(expected = NoSuchMethodException.class, timeout = 4000)
    public void testGetPublicMethod_NonExistentMethod() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethodXYZ", new Class<?>[0]);
    }

    @Test(expected = NoSuchMethodException.class, timeout = 4000)
    public void testGetPublicMethod_PrivateMethod() throws Exception {
        // Testing that private method is not returned
        ClassUtils.getPublicMethod(ClassUtilsGeminiTest.class, "dummyPrivateMethod", new Class<?>[0]);
    }

    private void dummyPrivateMethod() {
        // For reflection test
    }
}