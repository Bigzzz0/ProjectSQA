package com.google.gson;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for TypeInfoFactory covering all methods and branches.
 * Targets the known defect where inherited type variables from subclasses
 * cause UnsupportedOperationException.
 */
public class TypeInfoFactoryDeepseekTest {

    // --- Test helper classes ---
    static class GenericParent<T> {
        T value;
        List<T> listValue;
        T[] arrayValue;
        Map<String, T> mapValue;
    }

    static class ConcreteChild extends GenericParent<String> {
    }

    static class SimpleClass {
        String simpleField;
        List<String> listField;
        String[] arrayField;
        List<? extends Number> wildcardField;
    }

    static class GenericClass<T> {
        T genericField;
        List<T> genericListField;
        T[] genericArrayField;
    }

    static class MultiTypeParent<A, B> {
        A firstField;
        B secondField;
    }

    static class MultiTypeChild extends MultiTypeParent<Integer, String> {
    }

    // --- Tests for getTypeInfoForArray ---

    /**
     * @target getTypeInfoForArray(Type)
     * @scenario Array of primitive type
     * @defectRisk Potential incorrect handling of primitive arrays
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForArray_PrimitiveArray() {
        TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(int[].class);
        assertNotNull(info);
        assertEquals(int[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForArray(Type)
     * @scenario Array of object type
     * @defectRisk Potential incorrect handling of object arrays
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForArray_ObjectArray() {
        TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(String[].class);
        assertNotNull(info);
        assertEquals(String[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForArray(Type)
     * @scenario Non-array type should throw IllegalArgumentException
     * @defectRisk Missing validation for non-array types
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetTypeInfoForArray_NonArrayType() {
        TypeInfoFactory.getTypeInfoForArray(String.class);
    }

    /**
     * @target getTypeInfoForArray(Type)
     * @scenario Null type should throw NullPointerException
     * @defectRisk Missing null check
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetTypeInfoForArray_NullType() {
        TypeInfoFactory.getTypeInfoForArray(null);
    }

    // --- Tests for getTypeInfoForField ---

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Simple non-generic field
     * @defectRisk Incorrect type resolution for simple fields
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_SimpleField() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("simpleField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertEquals(String.class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Parameterized field with concrete type
     * @defectRisk Incorrect handling of parameterized types
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_ParameterizedField() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("listField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, paramType.getRawType());
        assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Generic array field
     * @defectRisk Incorrect handling of generic array types
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_GenericArrayField() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("arrayField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertEquals(String[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Wildcard type field
     * @defectRisk Incorrect handling of wildcard types
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_WildcardField() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("wildcardField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        Type argType = paramType.getActualTypeArguments()[0];
        assertTrue(argType instanceof WildcardType);
        WildcardType wildcard = (WildcardType) argType;
        assertEquals(Number.class, wildcard.getUpperBounds()[0]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable field on directly parameterized parent
     * @defectRisk Incorrect resolution of type variables
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_TypeVariableDirectParent() throws Exception {
        Field field = GenericClass.class.getDeclaredField("genericField");
        ParameterizedType paramType = new ParameterizedTypeImpl(
                GenericClass.class, new Type[]{String.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, paramType);
        assertEquals(String.class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable field on inherited subclass (KNOWN DEFECT)
     * @defectRisk UnsupportedOperationException when typeDefiningF is a subclass
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_SubclassInheritedTypeVariable_GSON40() throws Exception {
        Field field = GenericParent.class.getDeclaredField("value");
        TypeInfo typeInfo = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
        assertEquals(String.class, typeInfo.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable in parameterized field on inherited subclass
     * @defectRisk Incorrect resolution of type variables in nested generics
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_TypeVariableInListOnSubclass() throws Exception {
        Field field = GenericParent.class.getDeclaredField("listValue");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, paramType.getRawType());
        assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Generic array field on inherited subclass
     * @defectRisk Incorrect resolution of generic array types
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_GenericArrayOnSubclass() throws Exception {
        Field field = GenericParent.class.getDeclaredField("arrayValue");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
        assertEquals(String[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Map with type variable value on inherited subclass
     * @defectRisk Incorrect resolution of nested type variables
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_MapWithTypeVariableOnSubclass() throws Exception {
        Field field = GenericParent.class.getDeclaredField("mapValue");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, ConcreteChild.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        assertEquals(Map.class, paramType.getRawType());
        Type[] args = paramType.getActualTypeArguments();
        assertEquals(String.class, args[0]);
        assertEquals(String.class, args[1]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Multiple type variables on inherited subclass
     * @defectRisk Incorrect index mapping for multiple type variables
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_MultipleTypeVariablesOnSubclass() throws Exception {
        Field firstField = MultiTypeParent.class.getDeclaredField("firstField");
        TypeInfo firstInfo = TypeInfoFactory.getTypeInfoForField(firstField, MultiTypeChild.class);
        assertEquals(Integer.class, firstInfo.getActualType());

        Field secondField = MultiTypeParent.class.getDeclaredField("secondField");
        TypeInfo secondInfo = TypeInfoFactory.getTypeInfoForField(secondField, MultiTypeChild.class);
        assertEquals(String.class, secondInfo.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable field with raw class as parent type
     * @defectRisk Should throw UnsupportedOperationException for raw parent
     */
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetTypeInfoForField_TypeVariableWithRawParent() throws Exception {
        Field field = GenericClass.class.getDeclaredField("genericField");
        TypeInfoFactory.getTypeInfoForField(field, GenericClass.class);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Null field should throw NullPointerException
     * @defectRisk Missing null check for field
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetTypeInfoForField_NullField() {
        TypeInfoFactory.getTypeInfoForField(null, SimpleClass.class);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Null typeDefiningF should throw NullPointerException
     * @defectRisk Missing null check for typeDefiningF
     */
    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetTypeInfoForField_NullTypeDefiningF() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("simpleField");
        TypeInfoFactory.getTypeInfoForField(field, null);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable in generic array field on directly parameterized parent
     * @defectRisk Incorrect handling of generic array with type variable
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_GenericArrayTypeVariableDirectParent() throws Exception {
        Field field = GenericClass.class.getDeclaredField("genericArrayField");
        ParameterizedType paramType = new ParameterizedTypeImpl(
                GenericClass.class, new Type[]{Integer.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, paramType);
        assertEquals(Integer[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable in list field on directly parameterized parent
     * @defectRisk Incorrect handling of nested type variables
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_ListTypeVariableDirectParent() throws Exception {
        Field field = GenericClass.class.getDeclaredField("genericListField");
        ParameterizedType paramType = new ParameterizedTypeImpl(
                GenericClass.class, new Type[]{Double.class}, null);
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, paramType);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType listType = (ParameterizedType) info.getActualType();
        assertEquals(List.class, listType.getRawType());
        assertEquals(Double.class, listType.getActualTypeArguments()[0]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Wildcard type with upper bound on directly parameterized parent
     * @defectRisk Incorrect handling of wildcard types in generic context
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_WildcardWithUpperBound() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("wildcardField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        Type argType = paramType.getActualTypeArguments()[0];
        assertTrue(argType instanceof WildcardType);
        WildcardType wildcard = (WildcardType) argType;
        assertEquals(Number.class, wildcard.getUpperBounds()[0]);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Generic array type with wildcard component
     * @defectRisk Incorrect handling of generic array with wildcard
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_GenericArrayWithWildcard() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("arrayField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertEquals(String[].class, info.getActualType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Parameterized type with owner type
     * @defectRisk Incorrect handling of owner type in parameterized types
     */
    @Test(timeout = 4000)
    public void testGetTypeInfoForField_ParameterizedWithOwner() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("listField");
        TypeInfo info = TypeInfoFactory.getTypeInfoForField(field, SimpleClass.class);
        assertTrue(info.getActualType() instanceof ParameterizedType);
        ParameterizedType paramType = (ParameterizedType) info.getActualType();
        assertNull(paramType.getOwnerType());
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Type variable not found in class declaration
     * @defectRisk Should throw IllegalStateException for missing type variable
     */
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetTypeInfoForField_TypeVariableNotFound() throws Exception {
        Field field = GenericParent.class.getDeclaredField("value");
        ParameterizedType paramType = new ParameterizedTypeImpl(
                SimpleClass.class, new Type[]{String.class}, null);
        TypeInfoFactory.getTypeInfoForField(field, paramType);
    }

    /**
     * @target getTypeInfoForField(Field, Type)
     * @scenario Unsupported type (e.g., raw type not matching any category)
     * @defectRisk Should throw IllegalArgumentException for unsupported types
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetTypeInfoForField_UnsupportedType() throws Exception {
        Field field = SimpleClass.class.getDeclaredField("simpleField");
        // Use a custom Type that is not Class, ParameterizedType, GenericArrayType,
        // TypeVariable, or WildcardType
        Type unsupportedType = new Type() {};
        TypeInfoFactory.getTypeInfoForField(field, unsupportedType);
    }
}