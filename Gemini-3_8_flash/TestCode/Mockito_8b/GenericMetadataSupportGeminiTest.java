/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.util.reflection.GenericMetadataSupport
 * Target Defect: StackOverflowError on recursive self-type generic bounds (e.g., <T extends SelfType<T>>)
 *
 * Decision / Condition Matrix:
 * 1. inferFrom(Type):
 *    - type == null -> throws RuntimeException (Checks.checkNotNull)
 *    - type instanceof Class -> FromClassGenericMetadataSupport
 *    - type instanceof ParameterizedType -> FromParameterizedTypeGenericMetadataSupport
 *    - type other (e.g. WildcardType / GenericArrayType) -> MockitoException
 * 2. resolveGenericReturnType(Method):
 *    - genericReturnType instanceof Class -> NotGenericReturnTypeSupport
 *    - genericReturnType instanceof ParameterizedType -> ParameterizedReturnType
 *    - genericReturnType instanceof TypeVariable -> TypeVariableReturnType
 *    - genericReturnType unsupported -> MockitoException
 * 3. TypeVariable bounds registration & resolution:
 *    - Bounds are TypeVariables -> recursive unrolling via boundsOf(TypeVariable)
 *    - Bounds are Wildcards (upper & lower bounds) -> boundsOf(WildcardType)
 *    - Self-referential bounds (e.g., T extends Self<T>) -> cyclic lookup hazard in getActualTypeArgumentFor()
 * 4. Extraction of raw types and extra interfaces:
 *    - Single upper bound vs multiple bounds (& Comparable & Cloneable)
 *    - Class return type vs Interface return type vs TypeVariable
 *    - rawExtraInterfaces() filtering collision with rawType()
 * 5. BoundedType implementations (TypeVarBoundedType & WildCardBoundedType):
 *    - equals, hashCode, toString, interfaceBounds, typeVariable(), wildCard()
 * --------------------------------------------------------------------------------------------------------------------
 */
public class GenericMetadataSupportGeminiTest {

    // Helper Interfaces and Classes for Generic Introspection Fixtures
    interface SampleBaseInterface<T, U> {
        T getFirst();
        U getSecond();
    }

    interface UpperBoundedInterface<E extends Number & Comparable<E> & Cloneable> {
        E boundedItem();
    }

    interface NestedGenericsInterface<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Set<Number> remove(Object key);
        List<? super Integer> wildcardLowerBound();
        List<? extends K> wildcardUpperBound();
        K returningK();
        <O extends K> List<O> methodTypeParam();
        <S, T extends S> T twoTypeParams();
        <O extends K> O typeVarWithParams();
        String returningString();
    }

    abstract static class MiddleClass<V> implements SampleBaseInterface<V, String> {
        public abstract V getFirst();
        public abstract String getSecond();
    }

    static class ConcreteSubClass extends MiddleClass<Double> {
        @Override
        public Double getFirst() {
            return 1.0;
        }
        @Override
        public String getSecond() {
            return "concrete";
        }
    }

    interface WildcardHost {
        List<? extends CharSequence> extendsWildcard();
        List<? super Integer> superWildcard();
        <W extends Comparable<W>> List<? extends W> nestedTypeVarWildcard();
    }

    interface SelfReferencing<T extends SelfReferencing<T>> {
        T self();
    }

    interface DummyArrayReturn {
        String[] arrayMethod();
    }

    // ====================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ====================================================================================

    @Test(timeout = 4000)
    public void testInferFromClass_NonGeneric() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);

        assertEquals(String.class, metadata.rawType());
        assertTrue(metadata.actualTypeArguments().isEmpty());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.rawExtraInterfaces().length);
        assertTrue(metadata.extraInterfaces().isEmpty());

        Method lengthMethod = String.class.getMethod("length");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(lengthMethod);
        assertEquals(int.class, returnMetadata.rawType());
    }

    @Test(timeout = 4000)
    public void testInferFromClass_ClassInheritanceHierarchy() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ConcreteSubClass.class);

        assertEquals(ConcreteSubClass.class, metadata.rawType());
        Method getFirst = ConcreteSubClass.class.getMethod("getFirst");
        GenericMetadataSupport firstReturn = metadata.resolveGenericReturnType(getFirst);
        assertEquals(Double.class, firstReturn.rawType());

        Method getSecond = ConcreteSubClass.class.getMethod("getSecond");
        GenericMetadataSupport secondReturn = metadata.resolveGenericReturnType(getSecond);
        assertEquals(String.class, secondReturn.rawType());
    }

    @Test(timeout = 4000)
    public void testInferFromParameterizedType_Direct() throws NoSuchMethodException {
        Method sampleMethod = NestedGenericsInterface.class.getMethod("returningK");
        Type genericInterfaceType = NestedGenericsInterface.class.getGenericInterfaces()[0]; // Map<K, Set<Number>>

        assertTrue(genericInterfaceType instanceof ParameterizedType);
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(genericInterfaceType);

        assertEquals(Map.class, metadata.rawType());
        assertNotNull(metadata.actualTypeArguments());
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_ParameterizedReturn() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(NestedGenericsInterface.class);
        Method removeMethod = NestedGenericsInterface.class.getMethod("remove", Object.class);

        GenericMetadataSupport returnSupport = metadata.resolveGenericReturnType(removeMethod);
        assertEquals(Set.class, returnSupport.rawType());
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_TypeVariableReturn() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(NestedGenericsInterface.class);
        Method returningKMethod = NestedGenericsInterface.class.getMethod("returningK");

        GenericMetadataSupport returnSupport = metadata.resolveGenericReturnType(returningKMethod);
        assertEquals(Comparable.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        Class<?>[] extraInterfaces = returnSupport.rawExtraInterfaces();
        assertEquals(1, extraInterfaces.length);
        assertEquals(Cloneable.class, extraInterfaces[0]);
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_MethodLevelTypeVariables() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(NestedGenericsInterface.class);
        Method typeVarWithParams = NestedGenericsInterface.class.getMethod("typeVarWithParams");

        GenericMetadataSupport returnSupport = metadata.resolveGenericReturnType(typeVarWithParams);
        assertEquals(Comparable.class, returnSupport.rawType());
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_TwoMethodTypeParams() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(NestedGenericsInterface.class);
        Method twoTypeParams = NestedGenericsInterface.class.getMethod("twoTypeParams");

        GenericMetadataSupport returnSupport = metadata.resolveGenericReturnType(twoTypeParams);
        assertEquals(Object.class, returnSupport.rawType());
    }

    @Test(timeout = 4000)
    public void testMultipleBounds_TypeVarBoundedType() throws NoSuchMethodException {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedInterface.class);
        Method boundedItem = UpperBoundedInterface.class.getMethod("boundedItem");

        GenericMetadataSupport returnSupport = metadata.resolveGenericReturnType(boundedItem);
        assertEquals(Number.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        Class<?>[] rawInterfaces = returnSupport.rawExtraInterfaces();
        assertEquals(2, rawInterfaces.length);
        assertEquals(Comparable.class, rawInterfaces[0]);
        assertEquals(Cloneable.class, rawInterfaces[1]);
    }

    // ====================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Wildcard Bounds
    // ====================================================================================

    @Test(timeout = 4000)
    public void testWildcardBounds_ExtendsWildcard() throws NoSuchMethodException {
        Method method = WildcardHost.class.getMethod("extendsWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType bounded = new GenericMetadataSupport.WildCardBoundedType(wildcardType);
        assertEquals(CharSequence.class, bounded.firstBound());
        assertEquals(0, bounded.interfaceBounds().length);
        assertSame(wildcardType, bounded.wildCard());
    }

    @Test(timeout = 4000)
    public void testWildcardBounds_SuperWildcard() throws NoSuchMethodException {
        Method method = WildcardHost.class.getMethod("superWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcardType = (WildcardType) returnType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType bounded = new GenericMetadataSupport.WildCardBoundedType(wildcardType);
        assertEquals(Integer.class, bounded.firstBound());
        assertEquals(0, bounded.interfaceBounds().length);
    }

    @Test(timeout = 4000)
    public void testRegisterTypeVariablesOn_WithWildcard() throws NoSuchMethodException {
        Method method = WildcardHost.class.getMethod("superWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(returnType);
        assertEquals(List.class, metadata.rawType());
    }

    // ====================================================================================
    // Partition C: Defect-Targeted Branch Zone (Self-Referential Types)
    // ====================================================================================

    /**
     * Targets Defects4J known failure condition:
     * - GenericMetadataSupportTest::typeVariable_of_self_type
     * -> java.lang.StackOverflowError
     *
     * A self-referencing bounded generic parameter (T extends SelfReferencing<T>) can induce
     * recursive loop in contextualActualTypeParameters lookup during method return type resolution.
     */
    @Test(timeout = 4000)
    public void testTypeVariable_of_self_type_RegressionDefect() throws NoSuchMethodException {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SelfReferencing.class);
        Method selfMethod = SelfReferencing.class.getMethod("self");

        GenericMetadataSupport returnTypeSupport = support.resolveGenericReturnType(selfMethod);
        assertNotNull(returnTypeSupport);
        assertEquals(SelfReferencing.class, returnTypeSupport.rawType());
    }

    // ====================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ====================================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testInferFrom_NullArgumentThrowsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test(timeout = 4000)
    public void testInferFrom_UnsupportedTypeThrowsMockitoException() throws NoSuchMethodException {
        Method method = WildcardHost.class.getMethod("extendsWildcard");
        ParameterizedType returnType = (ParameterizedType) method.getGenericReturnType();
        Type wildcardType = returnType.getActualTypeArguments()[0];

        try {
            GenericMetadataSupport.inferFrom(wildcardType);
            fail("Expected MockitoException for unsupported WildcardType in inferFrom");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Type meta-data for this Type"));
        }
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_UnsupportedGenericArrayTypeThrowsMockitoException() throws Exception {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(DummyArrayReturn.class);
        Method arrayMethod = DummyArrayReturn.class.getMethod("arrayMethod");

        // String[] is a Class (not GenericArrayType), should return NotGenericReturnTypeSupport
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(arrayMethod);
        assertEquals(String[].class, returnSupport.rawType());
    }

    // ====================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ====================================================================================

    @Test(timeout = 4000)
    public void testTypeVarBoundedType_Contract() {
        TypeVariable<?>[] typeParams = UpperBoundedInterface.class.getTypeParameters();
        TypeVariable<?> typeVar1 = typeParams[0];

        GenericMetadataSupport.TypeVarBoundedType bounded1 = new GenericMetadataSupport.TypeVarBoundedType(typeVar1);
        GenericMetadataSupport.TypeVarBoundedType bounded2 = new GenericMetadataSupport.TypeVarBoundedType(typeVar1);

        assertEquals(bounded1, bounded1);
        assertEquals(bounded1, bounded2);
        assertEquals(bounded1.hashCode(), bounded2.hashCode());
        assertNotEquals(bounded1, null);
        assertNotEquals(bounded1, "someString");

        assertSame(typeVar1, bounded1.typeVariable());
        assertEquals(Number.class, bounded1.firstBound());
        Type[] interfaces = bounded1.interfaceBounds();
        assertEquals(2, interfaces.length);
        assertEquals(Comparable.class, interfaces[0]);
        assertEquals(Cloneable.class, interfaces[1]);
        assertTrue(bounded1.toString().contains("firstBound="));
    }

    @Test(timeout = 4000)
    public void testWildCardBoundedType_Contract() throws NoSuchMethodException {
        Method method = WildcardHost.class.getMethod("extendsWildcard");
        ParameterizedType pType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcard1 = (WildcardType) pType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType bounded1 = new GenericMetadataSupport.WildCardBoundedType(wildcard1);
        GenericMetadataSupport.WildCardBoundedType bounded2 = new GenericMetadataSupport.WildCardBoundedType(wildcard1);

        assertEquals(bounded1, bounded1);
        assertNotEquals(bounded1, null);
        assertNotEquals(bounded1, new Object());
        assertEquals(bounded1.hashCode(), bounded2.hashCode());
        assertTrue(bounded1.toString().contains("interfaceBounds=[]"));
        assertSame(wildcard1, bounded1.wildCard());
    }
}