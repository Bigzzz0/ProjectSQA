/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.mockito.internal.util.reflection.GenericMaster
 * Target Method: public Class getGenericType(Field field)
 * ----------------------------------------------------------------------------------------------------
 * Decision / Condition Matrix:
 * 1. generic == null:
 *    - Branch: Returns Object.class.
 * 2. !(generic instanceof ParameterizedType):
 *    - Condition: Non-generic types (Class), raw types, arrays, primitive types.
 *    - Branch: Returns Object.class.
 * 3. generic instanceof ParameterizedType:
 *    - Condition 3a (Flat Generic): actual is Class (e.g., List<String>).
 *      Branch: Returns actual (String.class).
 *    - Condition 3b (Nested Generic - DEFECT DEF-1): actual is ParameterizedType (e.g., List<Set<String>>).
 *      Defect Behavior: Attempts `(Class) actual`, causing ClassCastException.
 *      Correct Behavior: Should extract raw type `(Class) ((ParameterizedType) actual).getRawType()`.
 *    - Condition 3c (Wildcard Generic - DEFECT DEF-2): actual is WildcardType (e.g., List<?>).
 *      Defect Behavior: Attempts `(Class) actual`, causing ClassCastException.
 *      Correct Behavior: Returns Object.class.
 *    - Condition 3d (TypeVariable Generic - DEFECT DEF-3): actual is TypeVariable (e.g., List<T>).
 *      Defect Behavior: Attempts `(Class) actual`, causing ClassCastException.
 *      Correct Behavior: Returns Object.class.
 * 4. field == null:
 *    - Defensive branch: Throws NullPointerException.
 * ====================================================================================================
 */

package org.mockito.internal.util.reflection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericMasterGeminiTest {

    // Test fixture fields for reflection inspection
    public static List<String> stringList;
    public static Map<Integer, String> integerStringMap;
    public static Set<Double> doubleSet;
    public static List<GenericMaster> customObjectList;

    public static String nonGenericField;
    public static int primitiveField;
    public static String[] stringArrayField;
    @SuppressWarnings("rawtypes")
    public static List rawListField;

    public static List<Set<String>> nestedListSet;
    public static List<Map<String, Object>> nestedListMap;
    public static List<List<Set<Integer>>> deeplyNestedList;

    public static List<?> wildcardList;
    public static List<? extends Number> boundedWildcardList;

    private static class GenericContainer<T> {
        public List<T> typeVariableList;
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & Standard Parameterized Types
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetGenericTypeSingleTypeArgument() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("stringList");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Generic type result must not be null", result);
        assertEquals("List<String> must resolve to String.class", String.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericTypeMultiTypeArgumentSelectsFirst() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("integerStringMap");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Generic type result must not be null", result);
        assertEquals("Map<Integer, String> must return the first actual type argument Integer.class",
                Integer.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericTypeWithPrimitiveWrapperAndCustomClass() throws Exception {
        GenericMaster master = new GenericMaster();

        Field setField = GenericMasterGeminiTest.class.getField("doubleSet");
        assertEquals("Set<Double> must resolve to Double.class", Double.class, master.getGenericType(setField));

        Field customField = GenericMasterGeminiTest.class.getField("customObjectList");
        assertEquals("List<GenericMaster> must resolve to GenericMaster.class",
                GenericMaster.class, master.getGenericType(customField));
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Non-Generic Types
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetGenericTypeNonGenericObjectField() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("nonGenericField");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null", result);
        assertEquals("Non-generic String field must return Object.class", Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericTypePrimitiveField() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("primitiveField");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null", result);
        assertEquals("Primitive int field must return Object.class", Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericTypeRawCollectionField() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("rawListField");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null", result);
        assertEquals("Raw List field without generics must return Object.class", Object.class, result);
    }

    @Test(timeout = 4000)
    public void testGetGenericTypeArrayField() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("stringArrayField");

        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null", result);
        assertEquals("Array field must return Object.class", Object.class, result);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Nested & Non-Class Generic Types)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void shouldDealWithNestedGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("nestedListSet");

        // Ground-Truth Defect: Throws ClassCastException when actual is ParameterizedTypeImpl
        // Expected behavior: Retrieves raw type of the nested generic (Set.class)
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for nested generic", result);
        assertEquals("Nested generic List<Set<String>> must extract raw type Set.class", Set.class, result);
    }

    @Test(timeout = 4000)
    public void shouldDealWithNestedMapGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("nestedListMap");

        // Ground-Truth Defect: Throws ClassCastException when casting ParameterizedType to Class
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for nested map", result);
        assertEquals("Nested generic List<Map<String, Object>> must extract raw type Map.class",
                Map.class, result);
    }

    @Test(timeout = 4000)
    public void shouldDealWithDeeplyNestedGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("deeplyNestedList");

        // Without going deep, the first level generic type of List<List<Set<Integer>>> is List.class
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for deeply nested generic", result);
        assertEquals("Deeply nested generic must return first level raw type List.class", List.class, result);
    }

    @Test(timeout = 4000)
    public void shouldDealWithUnboundedWildcardGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("wildcardList");

        // Ground-Truth Defect: Throws ClassCastException when casting WildcardTypeImpl to Class
        // Expected behavior: Unresolvable wildcard falls back to Object.class
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for wildcard list", result);
        assertEquals("Wildcard List<?> must fallback to Object.class", Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldDealWithBoundedWildcardGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericMasterGeminiTest.class.getField("boundedWildcardList");

        // Ground-Truth Defect: Throws ClassCastException when casting WildcardTypeImpl to Class
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for bounded wildcard list", result);
        assertEquals("Bounded wildcard List<? extends Number> must fallback to Object.class",
                Object.class, result);
    }

    @Test(timeout = 4000)
    public void shouldDealWithTypeVariableGenerics() throws Exception {
        GenericMaster master = new GenericMaster();
        Field field = GenericContainer.class.getField("typeVariableList");

        // Ground-Truth Defect: Throws ClassCastException when casting TypeVariableImpl to Class
        Class<?> result = master.getGenericType(field);

        assertNotNull("Result should not be null for type variable list", result);
        assertEquals("Unresolved type variable List<T> must fallback to Object.class",
                Object.class, result);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetGenericTypeWithNullFieldThrowsNullPointerException() {
        GenericMaster master = new GenericMaster();
        master.getGenericType(null);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Idempotency / State Verification
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGenericMasterStateIndependenceAndDeterminism() throws Exception {
        GenericMaster master = new GenericMaster();
        Field listField = GenericMasterGeminiTest.class.getField("stringList");
        Field intField = GenericMasterGeminiTest.class.getField("primitiveField");

        Class<?> firstCall = master.getGenericType(listField);
        Class<?> secondCall = master.getGenericType(intField);
        Class<?> thirdCall = master.getGenericType(listField);

        assertEquals("First call must resolve to String.class", String.class, firstCall);
        assertEquals("Second call must resolve to Object.class", Object.class, secondCall);
        assertEquals("Third call must deterministically resolve to String.class without state leakage",
                String.class, thirdCall);
        assertSame("Repeated invocations must return identical Class singleton instance", firstCall, thirdCall);
    }
}