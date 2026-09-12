package com.google.gson;

import java.lang.reflect.*;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link TypeInfoFactory}.
 *
 * These tests target line/branch coverage for getTypeInfoForArray, getTypeInfoForField,
 * and the private helper getActualType (exercised indirectly), including the known
 * Defects4J defect where inherited TypeVariable resolution incorrectly throws
 * UnsupportedOperationException when the typeDefiningF is a raw Class<?> subclass
 * instead of a ParameterizedType.
 */
public class TypeInfoFactoryClaudeTest {

  // ---------------------------------------------------------------------
  // Helper classes used across multiple tests
  // ---------------------------------------------------------------------

  static class Simple {
    String name;
  }

  static class Box<T> {
    T content;
  }

  static class StringBox extends Box<String> {
  }

  static class Container<T> {
    T[] arr;
    T value;
    List<T> list;
  }

  static class StringContainer extends Container<String> {
  }

  static class WildcardHolder {
    List<? extends Number> wildcardList;
  }

  static class GenericParent<T> {
    T value;
  }

  static class ConcreteChild extends GenericParent<String> {
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForArray tests
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForArray(Type)
   * @scenario Valid reference array type (String[].class) passed in.
   * @defectRisk Ensures Preconditions.checkArgument does not incorrectly reject valid arrays,
   *             and TypeInfoArray is properly constructed with the array's raw class.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForArray_ValidReferenceArray() {
    TypeInfoArray typeInfoArray = TypeInfoFactory.getTypeInfoForArray(String[].class);
    assertNotNull(typeInfoArray);
    assertEquals(String[].class, typeInfoArray.getRawClass());
    assertTrue(typeInfoArray.getRawClass().isArray());
  }

  /**
   * @target TypeInfoFactory.getTypeInfoForArray(Type)
   * @scenario Valid primitive array type (int[].class) passed in.
   * @defectRisk Ensures primitive array handling works through TypeUtils.isArray check.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForArray_ValidPrimitiveArray() {
    TypeInfoArray typeInfoArray = TypeInfoFactory.getTypeInfoForArray(int[].class);
    assertNotNull(typeInfoArray);
    assertEquals(int[].class, typeInfoArray.getRawClass());
  }

  /**
   * @target TypeInfoFactory.getTypeInfoForArray(Type)
   * @scenario Non-array type (String.class) passed in, expecting IllegalArgumentException
   *           via Preconditions.checkArgument(TypeUtils.isArray(type)).
   * @defectRisk Ensures the guard clause correctly rejects non-array types.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForArray_NonArrayType_ThrowsException() {
    try {
      TypeInfoFactory.getTypeInfoForArray(String.class);
      fail("Expected IllegalArgumentException for non-array type");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForField tests - regular class field
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type)
   * @scenario A regular non-generic field (String name) inside a plain class.
   * @defectRisk Verifies the simplest branch: typeToEvaluate instanceof Class<?> returns itself.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_RegularClassField() throws Exception {
    Field field = Simple.class.getDeclaredField("name");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, Simple.class);
    assertEquals(String.class, typeInfo.getActualType());
    assertEquals(String.class, typeInfo.getRawClass());
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForField tests - directly parameterized type variable
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType TypeVariable branch
   * @scenario Field of type T declared in Box<T>, resolved against a real ParameterizedType
   *           (Box<String>) obtained via StringBox's generic superclass. This is the "happy path"
   *           for TypeVariable resolution where parentType IS a ParameterizedType directly.
   * @defectRisk Contrasts with the known defect: confirms resolution succeeds when parentType
   *             is truly a ParameterizedType (not a bare Class subclass).
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_DirectParameterizedTypeVariable() throws Exception {
    Type parameterizedBoxType = StringBox.class.getGenericSuperclass();
    assertTrue(parameterizedBoxType instanceof ParameterizedType);

    Field field = Box.class.getDeclaredField("content");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parameterizedBoxType);
    assertEquals(String.class, typeInfo.getActualType());
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForField tests - ParameterizedType field (List<T>)
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType ParameterizedType branch
   * @scenario Field of type List<T> declared in Container<T>, resolved against
   *           ParameterizedType Container<String> obtained via StringContainer's generic superclass.
   * @defectRisk Validates extractRealTypes correctly substitutes T with String inside the
   *             actual type arguments of the resulting ParameterizedTypeImpl.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_ParameterizedListField() throws Exception {
    Type parameterizedContainerType = StringContainer.class.getGenericSuperclass();
    assertTrue(parameterizedContainerType instanceof ParameterizedType);

    Field field = Container.class.getDeclaredField("list");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parameterizedContainerType);

    assertEquals(List.class, typeInfo.getRawClass());
    Type actualType = typeInfo.getActualType();
    assertTrue(actualType instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) actualType;
    assertEquals(List.class, pt.getRawType());
    assertEquals(1, pt.getActualTypeArguments().length);
    assertEquals(String.class, pt.getActualTypeArguments()[0]);
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForField tests - GenericArrayType field (T[])
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType GenericArrayType branch
   * @scenario Field of type T[] declared in Container<T>, resolved against
   *           ParameterizedType Container<String>. Component type T resolves to String.class,
   *           and since resolved component differs from original and is a Class<?>, the method
   *           should wrap it via TypeUtils.wrapWithArray.
   * @defectRisk Validates the GenericArrayType branch's component-type resolution and the
   *             equals-check short-circuit path (componentType.equals(actualType)).
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_GenericArrayField() throws Exception {
    Type parameterizedContainerType = StringContainer.class.getGenericSuperclass();
    assertTrue(parameterizedContainerType instanceof ParameterizedType);

    Field field = Container.class.getDeclaredField("arr");
    assertTrue(field.getGenericType() instanceof GenericArrayType);

    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, parameterizedContainerType);
    Type actualType = typeInfo.getActualType();

    // Resolved component type should ultimately correspond to String, wrapped as an array.
    assertNotNull(actualType);
    Class<?> rawClass = typeInfo.getRawClass();
    assertTrue(rawClass.isArray());
    assertEquals(String.class, rawClass.getComponentType());
  }

  // ---------------------------------------------------------------------
  // getTypeInfoForField tests - WildcardType field
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType WildcardType branch
   * @scenario Field of type List<? extends Number>. The wildcard's actual type argument
   *           triggers the WildcardType instanceof branch inside getActualType, which resolves
   *           to the wildcard's upper bound (Number.class).
   * @defectRisk Validates that WildcardType resolution correctly recurses into
   *             getActualType(upperBounds[0], ...) and returns the upper bound class.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_WildcardTypeField() throws Exception {
    Field field = WildcardHolder.class.getDeclaredField("wildcardList");
    assertTrue(field.getGenericType() instanceof ParameterizedType);

    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, WildcardHolder.class);
    Type actualType = typeInfo.getActualType();

    assertTrue(actualType instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) actualType;
    assertEquals(List.class, pt.getRawType());
    assertEquals(1, pt.getActualTypeArguments().length);
    assertEquals(Number.class, pt.getActualTypeArguments()[0]);
  }

  // ---------------------------------------------------------------------
  // Exception path: TypeVariable field with non-ParameterizedType parent (UnsupportedOperationException)
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType TypeVariable branch,
   *         exception path where parentType is not a ParameterizedType.
   * @scenario A TypeVariable field is resolved directly against a raw Class<?> (not
   *           parameterized), which should throw UnsupportedOperationException per source logic.
   * @defectRisk Confirms the exception message/behavior for the "missing TypeToken idiom" case
   *             when field's declaring class itself is used unparameterized.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_TypeVariable_NonParameterizedParent_Throws() throws Exception {
    Field field = Box.class.getDeclaredField("content");
    try {
      TypeInfoFactory.getTypeInfoForField(field, Box.class);
      fail("Expected UnsupportedOperationException when parentType is not ParameterizedType");
    } catch (UnsupportedOperationException expected) {
      // expected
    }
  }

  // ---------------------------------------------------------------------
  // CRITICAL DEFECT-TARGETING TEST (GSON-40 / Defects4J known defect)
  // ---------------------------------------------------------------------

  /**
   * @target TypeInfoFactory.getTypeInfoForField(Field, Type) -> getActualType TypeVariable branch
   *         when field is declared in a generic superclass but typeDefiningF passed in is a
   *         concrete (non-parameterized) subclass Class<?>.
   * @scenario GenericParent<T> declares field "value" of type T. ConcreteChild extends
   *           GenericParent<String> directly (i.e., ConcreteChild.class is a raw Class<?>,
   *           not itself a ParameterizedType). getTypeInfoForField is invoked with the
   *           subclass as typeDefiningF, mirroring real-world inherited generic field usage.
   * @defectRisk KNOWN DEFECT: On the buggy version, getActualType's TypeVariable branch checks
   *             "parentType instanceof ParameterizedType" using the raw ConcreteChild.class,
   *             which fails (Class is not ParameterizedType), incorrectly throwing
   *             UnsupportedOperationException instead of walking up the class hierarchy to find
   *             GenericParent<String> and resolving T to String.class. This test MUST fail on
   *             the defective version and pass on the fixed version.
   */
  @Test(timeout = 4000)
  public void testGetTypeInfoForField_SubclassInheritedTypeVariable_GSON40() throws Exception {
    Field field = GenericParent.class.getDeclaredField("value");
    TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
    assertEquals(String.class, typeInfo.getActualType());
  }
}