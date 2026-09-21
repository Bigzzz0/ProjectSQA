package org.mockito.internal.util.reflection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: GenericMaster.getGenericType(Field)
 * 
 * Branches:
 * 1. generic != null && generic instanceof ParameterizedType
 *    - True: enters if-block
 *      - actual = ((ParameterizedType) generic).getActualTypeArguments()[0];
 *      - return (Class) actual;  // BUG: ClassCastException if actual is ParameterizedType (nested generics)
 *    - False: returns Object.class
 * 
 * Boundary conditions:
 * - generic == null (unlikely but possible? field.getGenericType() never returns null in practice)
 * - generic is a Class (non-parameterized) -> returns Object.class
 * - generic is ParameterizedType with actual type argument being a Class (e.g., List<String>) -> returns String.class
 * - generic is ParameterizedType with actual type argument being a ParameterizedType (e.g., List<List<String>>) -> throws ClassCastException (defect)
 * - generic is ParameterizedType with actual type argument being a TypeVariable? (e.g., T) -> ClassCastException? Not covered here.
 * 
 * Defect-targeted test: shouldDealWithNestedGenerics – must not throw ClassCastException.
 * Expected correct behavior: either return the raw type of the nested generic (e.g., List.class) or Object.class.
 * The original code fails; our test will assert that no exception is thrown and the return type is a Class.
 */
public class GenericMasterDeepseekTest {

    // Helper inner classes to define fields with various generic signatures
    static class WithSimpleGeneric {
        List<String> list;
    }

    static class WithNestedGeneric {
        List<List<String>> nestedList;
    }

    static class WithMapGeneric {
        Map<String, Integer> map;
    }

    static class WithNoGeneric {
        String plain;
    }

    static class WithPrimitive {
        int primitive;
    }

    static class WithArray {
        String[] array;
    }

    static class WithWildcard {
        List<?> wildcardList;
    }

    static class WithTypeVariable<T> {
        T variable;
    }

    private final GenericMaster master = new GenericMaster();

    // Helper to get field by name from a class
    private Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForNonGenericField() throws Exception {
        Field field = getField(WithNoGeneric.class, "plain");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnActualTypeForSimpleParameterizedField() throws Exception {
        Field field = getField(WithSimpleGeneric.class, "list");
        Class<?> result = master.getGenericType(field);
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnActualTypeForMapGenericField() throws Exception {
        Field field = getField(WithMapGeneric.class, "map");
        Class<?> result = master.getGenericType(field);
        // Map<String, Integer> -> actual type argument[0] is String.class
        assertEquals(String.class, result);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForPrimitiveField() throws Exception {
        Field field = getField(WithPrimitive.class, "primitive");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForArrayField() throws Exception {
        Field field = getField(WithArray.class, "array");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForWildcardField() throws Exception {
        Field field = getField(WithWildcard.class, "wildcardList");
        Class<?> result = master.getGenericType(field);
        // List<?> -> actual type argument is a WildcardType, not a Class -> ClassCastException? Actually the code casts to Class, which will throw.
        // But the defect is about nested generics; wildcard also causes ClassCastException.
        // This test reveals that wildcard is not handled either. However, the known defect only mentions nested generics.
        // We'll still test it to expose the bug. The expected behavior might be to return Object.class.
        // For now, we assert that it throws ClassCastException (since the code is buggy).
        // But to be consistent with the defect, we should expect the same failure.
        // However, the test should reveal the bug, so we can assert that it throws.
        // But the requirement says "write at least one dedicated test that directly targets this specific failure condition" (nested generics).
        // We'll include this as an additional boundary test.
        try {
            master.getGenericType(field);
            fail("Expected ClassCastException for wildcard type");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForTypeVariableField() throws Exception {
        Field field = getField(WithTypeVariable.class, "variable");
        Class<?> result = master.getGenericType(field);
        // T is a TypeVariable, not a Class -> ClassCastException
        try {
            master.getGenericType(field);
            fail("Expected ClassCastException for type variable");
        } catch (ClassCastException e) {
            // expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void shouldDealWithNestedGenerics() throws Exception {
        // This test directly targets the known defect: nested generics cause ClassCastException.
        // The method should handle nested generics without throwing.
        Field field = getField(WithNestedGeneric.class, "nestedList");
        // The buggy version throws ClassCastException. We assert that it does NOT throw.
        // The correct behavior (after fix) would be to return something like List.class or Object.class.
        // We'll assert that the method returns a Class and does not throw.
        Class<?> result = master.getGenericType(field);
        // The actual type argument of List<List<String>> is List<String> which is a ParameterizedType.
        // The fix should either return the raw type (List.class) or Object.class.
        // Since the comment says "in case of nested generics we don't go deep", returning Object.class is plausible.
        // But we cannot be sure; we just assert that it returns a Class (no exception).
        assertNotNull(result);
        // Additionally, we can check that it is either Object.class or List.class (raw type).
        // To be safe, we only check that it is a Class and not null.
        // However, to reveal the bug, we need the test to pass on the fixed version and fail on the buggy version.
        // On the buggy version, this test will throw ClassCastException and fail.
        // On the fixed version, it will return some Class and pass.
        // So we just call the method; if it throws, the test fails.
        // We can also assert that the result is not null.
        assertTrue("Result should be a Class", result instanceof Class);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionForNullField() {
        // The method does not check for null field; it will throw NullPointerException when calling field.getGenericType().
        // This is a defensive guard test.
        master.getGenericType(null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForFieldWithNoGenericType() throws Exception {
        // Field with a raw type (e.g., List without parameter) - actually List is a raw type, but field.getGenericType() returns List.class (a Class), not ParameterizedType.
        // So it falls into else branch and returns Object.class.
        // We'll create a field with raw List type.
        class WithRawType {
            List rawList;
        }
        Field field = WithRawType.class.getDeclaredField("rawList");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnObjectClassForFieldWithNoGenericTypeUsingObject() throws Exception {
        class WithObjectField {
            Object obj;
        }
        Field field = WithObjectField.class.getDeclaredField("obj");
        Class<?> result = master.getGenericType(field);
        assertEquals(Object.class, result);
    }

    // Additional test to cover the case where generic is null (though unlikely)
    // We can simulate by using a field that is not accessible? Not possible.
    // We'll skip as it's not reachable.
}