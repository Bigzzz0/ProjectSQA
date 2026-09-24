package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.util.reflection.GenericMetadataSupport
 *
 * Decision / Branch Points Targeted:
 * 1. inferFrom(Type):
 *    - Type is Class -> FromClassGenericMetadataSupport
 *    - Type is ParameterizedType -> FromParameterizedTypeGenericMetadataSupport
 *    - Type is null -> Checks.checkNotNull (Defensive)
 *    - Type is neither -> MockitoException (Unsupported Type)
 * 2. resolveGenericReturnType(Method):
 *    - Generic return type is Class -> NotGenericReturnTypeSupport
 *    - Generic return type is ParameterizedType -> ParameterizedReturnType
 *    - Generic return type is TypeVariable -> TypeVariableReturnType
 *    - Generic return type is GenericArrayType or other -> MockitoException
 * 3. TypeVariable resolution & inheritance hierarchy:
 *    - Single type variable, chained type variables (T -> X -> ConcreteType)
 *    - WildcardType arguments (upper bound <? extends ...>, lower bound <? super ...>)
 *    - Wildcard with TypeVariable bound (<? extends K>)
 *    - TypeVariable bounded by another TypeVariable (<S, T extends S>)
 *    - Multi-interface bounds (<K extends Comparable<K> & Cloneable>)
 * 4. Contract integrity & bounds representation:
 *    - TypeVarBoundedType: firstBound, interfaceBounds, equals, hashCode, toString, typeVariable
 *    - WildCardBoundedType: firstBound (upper vs lower bound), interfaceBounds, equals, hashCode, toString, wildCard
 * 5. Ground Truth Defect (Defects4J):
 *    - DeepStubFailingWhenGenricNestedAsRawTypeTest::discoverDeepMockingOfGenerics
 *    - When a generic class is nested as a raw type (NotGenericReturnTypeSupport), its type context
 *      is empty. Resolving a method returning a TypeVariable yields a TypeVariableReturnType whose
 *      contextualActualTypeParameters.get(type) returns null.
 *      In extractRawTypeOf(Type), this triggers: MockitoException: Raw extraction not supported for : 'null'
 *    - Partition C tests assert the expected resolved type (Object.class or bounded class) to reveal the defect.
 */
public class GenericMetadataSupportGeminiTest {

    // =========================================================================
    // Test Generic Class & Interface Fixtures
    // =========================================================================

    interface SingleInterface<T> {
        T value();
    }

    static class BaseGenericClass<T, U> {
        public T getT() { return null; }
        public U getU() { return null; }
    }

    static class MiddleGenericClass<X> extends BaseGenericClass<X, String> {
        public X getX() { return null; }
    }

    static class ConcreteSubClass extends MiddleGenericClass<Integer> {
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Set<Number> remove(Object key);
        List<? super Integer> returning_wildcard_with_class_lower_bound();
        List<? super K> returning_wildcard_with_typeVar_lower_bound();
        List<? extends K> returning_wildcard_with_typeVar_upper_bound();
        K returningK();
        <O extends K> List<O> paramType_with_type_params();
        <S, T extends S> T two_type_params();
        <O extends K> O typeVar_with_type_params();
        Number returningNonGeneric();
    }

    static class ValidKey implements Comparable<ValidKey>, Cloneable {
        @Override
        public int compareTo(ValidKey o) { return 0; }
    }

    interface SubNest extends GenericsNest<ValidKey> {
    }

    interface WildcardSuperContainer extends SingleInterface<List<? super Integer>> {
    }

    interface WildcardExtendsContainer<E extends Number> extends SingleInterface<List<? extends E>> {
    }

    interface MultiBoundInterface<E extends Number & Comparable<E> & Cloneable> {
        E get();
    }

    interface ArrayReturnHolder {
        <T> T[] genericArray();
    }

    interface GenericsNestedAsRawType<T> {
        T getNested();
    }

    interface DeepStubHolder {
        GenericsNestedAsRawType getFoo();
    }

    interface BoundedGenericsNestedAsRawType<T extends CharSequence> {
        T getNested();
    }

    interface BoundedDeepStubHolder {
        BoundedGenericsNestedAsRawType getFoo();
    }

    public static class ParameterizedFieldHolder {
        public List<String> stringList;
        public Map<String, Integer> stringIntegerMap;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void test_inferFrom_class_and_resolve_non_generic_return_type() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        assertEquals(GenericsNest.class, meta.rawType());
        assertFalse(meta.hasRawExtraInterfaces());
        assertEquals(0, meta.rawExtraInterfaces().length);
        assertTrue(meta.extraInterfaces().isEmpty());

        Method nonGenMethod = GenericsNest.class.getMethod("returningNonGeneric");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(nonGenMethod);
        assertEquals(Number.class, returnMeta.rawType());
        assertFalse(returnMeta.hasRawExtraInterfaces());
    }

    @Test(timeout = 4000)
    public void test_inferFrom_class_and_resolve_parameterized_return_type() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method removeMethod = GenericsNest.class.getMethod("remove", Object.class);
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(removeMethod);

        assertEquals(Set.class, returnMeta.rawType());
        assertFalse(returnMeta.hasRawExtraInterfaces());
    }

    @Test(timeout = 4000)
    public void test_resolve_type_variable_with_multiple_bounds_and_extra_interfaces() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method methodK = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(methodK);

        assertEquals(Comparable.class, returnMeta.rawType());
        assertTrue(returnMeta.hasRawExtraInterfaces());
        assertEquals(1, returnMeta.rawExtraInterfaces().length);
        assertEquals(Cloneable.class, returnMeta.rawExtraInterfaces()[0]);
        assertEquals(1, returnMeta.extraInterfaces().size());
        assertEquals(Cloneable.class, returnMeta.extraInterfaces().get(0));
    }

    @Test(timeout = 4000)
    public void test_resolve_recursive_type_parameters_through_inheritance_hierarchy() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(ConcreteSubClass.class);
        assertEquals(ConcreteSubClass.class, meta.rawType());

        Method getX = ConcreteSubClass.class.getMethod("getX");
        assertEquals(Integer.class, meta.resolveGenericReturnType(getX).rawType());

        Method getT = ConcreteSubClass.class.getMethod("getT");
        assertEquals(Integer.class, meta.resolveGenericReturnType(getT).rawType());

        Method getU = ConcreteSubClass.class.getMethod("getU");
        assertEquals(String.class, meta.resolveGenericReturnType(getU).rawType());
    }

    @Test(timeout = 4000)
    public void test_sub_nest_closed_generic_arguments() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(SubNest.class);
        Method methodK = SubNest.class.getMethod("returningK");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(methodK);

        assertEquals(ValidKey.class, returnMeta.rawType());
        assertFalse(returnMeta.hasRawExtraInterfaces());
    }

    @Test(timeout = 4000)
    public void test_inferFrom_parameterized_type_directly() throws Exception {
        ParameterizedType pt = (ParameterizedType) ParameterizedFieldHolder.class
                .getField("stringIntegerMap").getGenericType();
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(pt);

        assertEquals(Map.class, meta.rawType());
        Map<TypeVariable, Type> typeArgs = meta.actualTypeArguments();
        assertEquals(2, typeArgs.size());

        TypeVariable<?>[] typeParams = Map.class.getTypeParameters();
        assertEquals(String.class, typeArgs.get(typeParams[0]));
        assertEquals(Integer.class, typeArgs.get(typeParams[1]));
    }

    @Test(timeout = 4000)
    public void test_method_with_type_parameters_returning_parameterized_type() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method m = GenericsNest.class.getMethod("paramType_with_type_params");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(List.class, returnMeta.rawType());
    }

    @Test(timeout = 4000)
    public void test_method_with_type_var_bounded_by_another_type_var() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method m = GenericsNest.class.getMethod("two_type_params");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(Object.class, returnMeta.rawType());
    }

    @Test(timeout = 4000)
    public void test_method_with_type_param_bounded_by_class_type_param() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method m = GenericsNest.class.getMethod("typeVar_with_type_params");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(Comparable.class, returnMeta.rawType());
        assertTrue(returnMeta.hasRawExtraInterfaces());
        assertEquals(Cloneable.class, returnMeta.rawExtraInterfaces()[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Wildcards
    // =========================================================================

    @Test(timeout = 4000)
    public void test_wildcard_with_class_lower_bound() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(WildcardSuperContainer.class);
        Method m = WildcardSuperContainer.class.getMethod("value");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(List.class, returnMeta.rawType());
    }

    @Test(timeout = 4000)
    public void test_wildcard_with_type_variable_upper_bound() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method m = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(List.class, returnMeta.rawType());
    }

    @Test(timeout = 4000)
    public void test_wildcard_with_type_variable_lower_bound() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method m = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_lower_bound");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(List.class, returnMeta.rawType());
    }

    @Test(timeout = 4000)
    public void test_multiple_interface_bounds_on_class_type_parameter() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(MultiBoundInterface.class);
        Method m = MultiBoundInterface.class.getMethod("get");
        GenericMetadataSupport returnMeta = meta.resolveGenericReturnType(m);

        assertEquals(Number.class, returnMeta.rawType());
        assertTrue(returnMeta.hasRawExtraInterfaces());
        Class<?>[] extraRaw = returnMeta.rawExtraInterfaces();
        assertEquals(2, extraRaw.length);
        assertEquals(Comparable.class, extraRaw[0]);
        assertEquals(Cloneable.class, extraRaw[1]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug:
     * org.mockitousage.bugs.deepstubs.DeepStubFailingWhenGenricNestedAsRawTypeTest::discoverDeepMockingOfGenerics
     *
     * When a generic type is returned as a raw type (e.g. getFoo() -> GenericsNestedAsRawType),
     * it creates a NotGenericReturnTypeSupport instance with an empty contextualActualTypeParameters map.
     * When subsequently resolving a method on that raw type returning a TypeVariable (getNested() -> T),
     * extractRawTypeOf() looks up the type in the empty map, obtaining null, and throws:
     * MockitoException: Raw extraction not supported for : 'null'
     *
     * The correct behavior is to fallback to the TypeVariable's upper bound (Object.class).
     */
    @Test(timeout = 4000)
    public void test_defect_discoverDeepMockingOfGenerics_raw_nested_type() throws Exception {
        GenericMetadataSupport holderMetadata = GenericMetadataSupport.inferFrom(DeepStubHolder.class);
        Method getFooMethod = DeepStubHolder.class.getMethod("getFoo");
        GenericMetadataSupport nestedMetadata = holderMetadata.resolveGenericReturnType(getFooMethod);

        Method getNestedMethod = GenericsNestedAsRawType.class.getMethod("getNested");
        GenericMetadataSupport returnMetadata = nestedMetadata.resolveGenericReturnType(getNestedMethod);

        Class<?> rawType = returnMetadata.rawType();
        assertEquals(Object.class, rawType);
    }

    @Test(timeout = 4000)
    public void test_defect_discoverDeepMockingOfGenerics_with_bounded_type_variable() throws Exception {
        GenericMetadataSupport holderMetadata = GenericMetadataSupport.inferFrom(BoundedDeepStubHolder.class);
        Method getFooMethod = BoundedDeepStubHolder.class.getMethod("getFoo");
        GenericMetadataSupport nestedMetadata = holderMetadata.resolveGenericReturnType(getFooMethod);

        Method getNestedMethod = BoundedGenericsNestedAsRawType.class.getMethod("getNested");
        GenericMetadataSupport returnMetadata = nestedMetadata.resolveGenericReturnType(getNestedMethod);

        Class<?> rawType = returnMetadata.rawType();
        assertEquals(CharSequence.class, rawType);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void test_inferFrom_null_type_throws_exception() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void test_inferFrom_unsupported_type_throws_exception() {
        Type customUnsupportedType = new Type() {
            @Override
            public String getTypeName() {
                return "UnsupportedCustomType";
            }
        };
        GenericMetadataSupport.inferFrom(customUnsupportedType);
    }

    @Test(expected = MockitoException.class, timeout = 4000)
    public void test_resolveGenericReturnType_unsupported_generic_array_type() throws Exception {
        GenericMetadataSupport meta = GenericMetadataSupport.inferFrom(ArrayReturnHolder.class);
        Method genericArrayMethod = ArrayReturnHolder.class.getMethod("genericArray");
        meta.resolveGenericReturnType(genericArrayMethod);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void test_TypeVarBoundedType_contract() throws Exception {
        Method methodK = GenericsNest.class.getMethod("returningK");
        TypeVariable<?> tv = (TypeVariable<?>) methodK.getGenericReturnType();

        GenericMetadataSupport.TypeVarBoundedType b1 = new GenericMetadataSupport.TypeVarBoundedType(tv);
        GenericMetadataSupport.TypeVarBoundedType b2 = new GenericMetadataSupport.TypeVarBoundedType(tv);

        assertEquals(b1, b1);
        assertEquals(b1, b2);
        assertNotEquals(b1, null);
        assertNotEquals(b1, "otherType");
        assertEquals(b1.hashCode(), b2.hashCode());
        assertSame(tv, b1.typeVariable());

        assertEquals(Comparable.class, ((ParameterizedType) b1.firstBound()).getRawType());
        Type[] interfaces = b1.interfaceBounds();
        assertEquals(1, interfaces.length);
        assertEquals(Cloneable.class, interfaces[0]);
        assertTrue(b1.toString().contains("firstBound="));
        assertTrue(b1.toString().contains("interfaceBounds="));
    }

    @Test(timeout = 4000)
    public void test_WildCardBoundedType_contract_with_lower_bound() throws Exception {
        Method m = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType pt = (ParameterizedType) m.getGenericReturnType();
        WildcardType wt = (WildcardType) pt.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wb = new GenericMetadataSupport.WildCardBoundedType(wt);

        assertEquals(wb, wb);
        assertNotEquals(wb, null);
        assertNotEquals(wb, "differentObject");
        assertEquals(wt.hashCode(), wb.hashCode());
        assertSame(wt, wb.wildCard());

        assertEquals(Integer.class, wb.firstBound());
        assertEquals(0, wb.interfaceBounds().length);
        assertTrue(wb.toString().contains("firstBound="));
        assertTrue(wb.toString().contains("interfaceBounds=[]"));
    }

    @Test(timeout = 4000)
    public void test_WildCardBoundedType_contract_with_upper_bound() throws Exception {
        Method m = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        ParameterizedType pt = (ParameterizedType) m.getGenericReturnType();
        WildcardType wt = (WildcardType) pt.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wb = new GenericMetadataSupport.WildCardBoundedType(wt);
        Type firstBound = wb.firstBound();
        assertTrue(firstBound instanceof TypeVariable);
        assertEquals("K", ((TypeVariable<?>) firstBound).getName());
    }
}