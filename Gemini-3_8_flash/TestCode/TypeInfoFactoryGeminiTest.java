package com.google.gson;

import java.lang.reflect.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target: com.google.gson.TypeInfoFactory
 *
 * 1. getTypeInfoForArray(Type):
 *    - Branch: TypeUtils.isArray(type) == true  -> Construct and return TypeInfoArray (tested on int[], String[], Integer[][], GenericArrayType)
 *    - Branch: TypeUtils.isArray(type) == false -> Preconditions.checkArgument throws IllegalArgumentException (tested on String.class, int.class)
 *
 * 2. getTypeInfoForField(Field, Type):
 *    - Evaluation of raw class defining field -> delegating to getActualType()
 *
 * 3. getActualType(Type, Type, Class<?>):
 *    - Branch 1: typeToEvaluate instanceof Class<?>
 *      -> Returns unchanged Type (tested with String, int, Object)
 *    - Branch 2: typeToEvaluate instanceof ParameterizedType
 *      -> Extracts type parameters, owner, raw type, constructs ParameterizedTypeImpl (tested on List<String>, Map<String, Integer>, Inner generic class)
 *    - Branch 3: typeToEvaluate instanceof GenericArrayType
 *      -> Sub-branch 3a: componentType.equals(actualType) == true -> returns unchanged GenericArrayType
 *      -> Sub-branch 3b: actualType instanceof Class<?> == true   -> returns TypeUtils.wrapWithArray(rawClass)
 *      -> Sub-branch 3c: actualType instanceof Class<?> == false  -> returns GenericArrayTypeImpl(actualType)
 *    - Branch 4: typeToEvaluate instanceof TypeVariable<?>
 *      -> Sub-branch 4a: parentType instanceof ParameterizedType  -> resolves concrete type argument by index
 *      -> Sub-branch 4b: parentType not ParameterizedType         -> throws UnsupportedOperationException
 *      -> Sub-branch 4c [DEFECT GSON-1 / ISSUE 40]:
 *         Subclass inheriting from generic superclass with parentType passed as Class<?>:
 *         Fails with UnsupportedOperationException in defective build, resolves in fixed build.
 *    - Branch 5: typeToEvaluate instanceof WildcardType
 *      -> Evaluates upper bounds [0] (tested on List<? extends Number>)
 *    - Branch 6: Unknown Type implementation
 *      -> Throws IllegalArgumentException
 *
 * 4. getIndex(TypeVariable<?>[], TypeVariable<?>):
 *    - Branch: Match found     -> returns correct index
 *    - Branch: Match not found -> throws IllegalStateException
 *
 * 5. extractRealTypes(Type[], Type, Class<?>):
 *    - Branch: null actualTypeArguments -> throws NullPointerException
 * ====================================================================================================
 */
public class TypeInfoFactoryGeminiTest {

  // --- Static Test Models for Reflection ---

  static class GenericParent<T> {
    T value;
  }

  static class ConcreteChild extends GenericParent<String> {
  }

  static class SimpleClass {
    String strField;
    int intField;
    List<String> listField;
    Map<String, Integer> mapField;
    List<? extends Number> wildcardField;
  }

  static class GenericHolder<T> {
    T value;
  }

  static class MultiGenericHolder<K, V> {
    K key;
    V val;
  }

  static class GenericArrayHolder<T> {
    T[] array;
  }

  static class NestedGenericArrayHolder<T> {
    List<T>[] listArray;
  }

  static class Container {
    GenericHolder<Double> doubleHolder;
    MultiGenericHolder<String, Long> multiHolder;
    GenericArrayHolder<Float> floatArrayHolder;
    NestedGenericArrayHolder<Boolean> boolNestedArrayHolder;
  }

  static class OuterClass {
    class InnerParameterized<T> {
      T data;
    }
  }

  static class OuterContainer {
    OuterClass.InnerParameterized<String> innerField;
  }

  // --- Constructor Coverage ---

  @Test(timeout = 4000)
  public void testPrivateConstructor() throws Exception {
    Constructor<TypeInfoFactory> constructor = TypeInfoFactory.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    TypeInfoFactory instance = constructor.newInstance();
    assertNotNull(instance);
  }

  // --- Partition A: Array Type Construction ---

  @Test(timeout = 4000)
  public void testTypeInfoForArray_PrimitiveArray() {
    TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(int[].class);
    assertNotNull(info);
    assertEquals(int[].class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testTypeInfoForArray_ObjectArray() {
    TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(String[].class);
    assertNotNull(info);
    assertEquals(String[].class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testTypeInfoForArray_MultiDimensionalArray() {
    TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(Integer[][].class);
    assertNotNull(info);
    assertEquals(Integer[][].class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testTypeInfoForArray_GenericArrayType() {
    GenericArrayType customGat = new GenericArrayType() {
      public Type getGenericComponentType() {
        return String.class;
      }
    };
    TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(customGat);
    assertNotNull(info);
    assertEquals(customGat, info.getActualType());
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testTypeInfoForArray_NonArrayClass_ThrowsException() {
    TypeInfoFactory.getTypeInfoForArray(String.class);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testTypeInfoForArray_PrimitiveType_ThrowsException() {
    TypeInfoFactory.getTypeInfoForArray(int.class);
  }

  // --- Partition B: Non-Generic & Parameterized Field Resolution ---

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_SimpleClassField() throws Exception {
    Field f = SimpleClass.class.getDeclaredField("strField");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleClass.class);
    assertNotNull(info);
    assertEquals(String.class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_PrimitiveField() throws Exception {
    Field f = SimpleClass.class.getDeclaredField("intField");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleClass.class);
    assertNotNull(info);
    assertEquals(int.class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_ParameterizedField() throws Exception {
    Field f = SimpleClass.class.getDeclaredField("listField");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleClass.class);
    assertNotNull(info);
    assertTrue(info.getActualType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) info.getActualType();
    assertEquals(List.class, pt.getRawType());
    assertEquals(1, pt.getActualTypeArguments().length);
    assertEquals(String.class, pt.getActualTypeArguments()[0]);
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_MapField() throws Exception {
    Field f = SimpleClass.class.getDeclaredField("mapField");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleClass.class);
    assertNotNull(info);
    assertTrue(info.getActualType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) info.getActualType();
    assertEquals(Map.class, pt.getRawType());
    assertEquals(2, pt.getActualTypeArguments().length);
    assertEquals(String.class, pt.getActualTypeArguments()[0]);
    assertEquals(Integer.class, pt.getActualTypeArguments()[1]);
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_WildcardField() throws Exception {
    Field f = SimpleClass.class.getDeclaredField("wildcardField");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(f, SimpleClass.class);
    assertNotNull(info);
    assertTrue(info.getActualType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) info.getActualType();
    assertEquals(List.class, pt.getRawType());
    assertEquals(1, pt.getActualTypeArguments().length);
    assertEquals(Number.class, pt.getActualTypeArguments()[0]);
  }

  // --- Partition C: Defect-Targeted Branch Zone (Gson-1 / Issue 40) ---

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_SubclassInheritedTypeVariable_GSON40() throws Exception {
    Field field = GenericParent.class.getDeclaredField("value");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
    assertNotNull(typeInfo);
    assertEquals(String.class, typeInfo.getActualType());
  }

  // --- Partition D: Type Variable on Directly Parameterized Type & Array Variations ---

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_DirectParameterizedType() throws Exception {
    Field containerField = Container.class.getDeclaredField("doubleHolder");
    Type parameterizedParent = containerField.getGenericType();
    Field field = GenericHolder.class.getDeclaredField("value");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, parameterizedParent);
    assertNotNull(info);
    assertEquals(Double.class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_MultipleTypeVariables() throws Exception {
    Field containerField = Container.class.getDeclaredField("multiHolder");
    Type parameterizedParent = containerField.getGenericType();
    Field keyField = MultiGenericHolder.class.getDeclaredField("key");
    Field valField = MultiGenericHolder.class.getDeclaredField("val");

    TypeInfo keyInfo = TypeInfoFactory.getTypeInfoForField(keyField, parameterizedParent);
    TypeInfo valInfo = TypeInfoFactory.getTypeInfoForField(valField, parameterizedParent);

    assertNotNull(keyInfo);
    assertNotNull(valInfo);
    assertEquals(String.class, keyInfo.getActualType());
    assertEquals(Long.class, valInfo.getActualType());
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_GenericArrayType_ResolvingToClass() throws Exception {
    Field containerField = Container.class.getDeclaredField("floatArrayHolder");
    Type parameterizedParent = containerField.getGenericType();
    Field field = GenericArrayHolder.class.getDeclaredField("array");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, parameterizedParent);
    assertNotNull(info);
    assertEquals(Float[].class, info.getActualType());
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_GenericArrayType_ResolvingToGenericArrayTypeImpl() throws Exception {
    Field containerField = Container.class.getDeclaredField("boolNestedArrayHolder");
    Type parameterizedParent = containerField.getGenericType();
    Field field = NestedGenericArrayHolder.class.getDeclaredField("listArray");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, parameterizedParent);
    assertNotNull(info);
    assertTrue(info.getActualType() instanceof GenericArrayType);
    GenericArrayType gat = (GenericArrayType) info.getActualType();
    assertTrue(gat.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType compPt = (ParameterizedType) gat.getGenericComponentType();
    assertEquals(List.class, compPt.getRawType());
    assertEquals(Boolean.class, compPt.getActualTypeArguments()[0]);
  }

  @Test(timeout = 4000)
  public void testGetTypeInfoForField_InnerClassWithOwnerType() throws Exception {
    Field innerField = OuterContainer.class.getDeclaredField("innerField");
    Type paramType = innerField.getGenericType();
    Field dataField = OuterClass.InnerParameterized.class.getDeclaredField("data");
    TypeInfo info = TypeInfoFactory.getTypeInfoForField(dataField, paramType);
    assertNotNull(info);
    assertEquals(String.class, info.getActualType());
  }

  // --- Partition E: Exception & Defensive Guard Paths ---

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetTypeInfoForField_RawClassTypeVariable_ThrowsUnsupportedOperationException() throws Exception {
    Field field = GenericHolder.class.getDeclaredField("value");
    TypeInfoFactory.getTypeInfoForField(field, GenericHolder.class);
  }

  @Test(timeout = 4000)
  public void testGetActualType_GenericArrayType_ComponentEqualsActual() throws Exception {
    Method method = TypeInfoFactory.class.getDeclaredMethod("getActualType", Type.class, Type.class, Class.class);
    method.setAccessible(true);
    GenericArrayType customGat = new GenericArrayType() {
      public Type getGenericComponentType() {
        return String.class;
      }
      @Override
      public boolean equals(Object obj) {
        return this == obj;
      }
    };
    Type result = (Type) method.invoke(null, customGat, Object.class, Object.class);
    assertSame(customGat, result);
  }

  @Test(timeout = 4000)
  public void testGetActualType_UnknownType_ThrowsIllegalArgumentException() throws Exception {
    Method method = TypeInfoFactory.class.getDeclaredMethod("getActualType", Type.class, Type.class, Class.class);
    method.setAccessible(true);
    Type unknownType = new Type() {
      @Override
      public String toString() {
        return "CustomUnknownType";
      }
    };
    try {
      method.invoke(null, unknownType, Object.class, Object.class);
      fail("Expected InvocationTargetException containing IllegalArgumentException");
    } catch (InvocationTargetException ite) {
      assertTrue(ite.getCause() instanceof IllegalArgumentException);
      assertTrue(ite.getCause().getMessage().contains("is not a Class, ParameterizedType, GenericArrayType or TypeVariable"));
    }
  }

  @Test(timeout = 4000)
  public void testGetIndex_TypeVariableNotFound_ThrowsIllegalStateException() throws Exception {
    Method method = TypeInfoFactory.class.getDeclaredMethod("getIndex", TypeVariable[].class, TypeVariable.class);
    method.setAccessible(true);
    TypeVariable<?>[] empty = new TypeVariable<?>[0];
    TypeVariable<?> tv = GenericHolder.class.getTypeParameters()[0];
    try {
      method.invoke(null, empty, tv);
      fail("Expected InvocationTargetException containing IllegalStateException");
    } catch (InvocationTargetException ite) {
      assertTrue(ite.getCause() instanceof IllegalStateException);
      assertTrue(ite.getCause().getMessage().contains("How can the type variable not be present"));
    }
  }

  @Test(timeout = 4000)
  public void testExtractRealTypes_NullArray_ThrowsNullPointerException() throws Exception {
    Method method = TypeInfoFactory.class.getDeclaredMethod("extractRealTypes", Type[].class, Type.class, Class.class);
    method.setAccessible(true);
    try {
      method.invoke(null, null, Object.class, Object.class);
      fail("Expected InvocationTargetException containing NullPointerException");
    } catch (InvocationTargetException ite) {
      assertTrue(ite.getCause() instanceof NullPointerException);
    }
  }
}