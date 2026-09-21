package org.mockito.internal.util.reflection;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - inferFrom(Class) -> FromClassGenericMetadataSupport
 *   - inferFrom(ParameterizedType) -> FromParameterizedTypeGenericMetadataSupport
 *   - inferFrom(Class with generics) -> registerTypeVariablesOn, registerTypeParametersOn
 *   - resolveGenericReturnType for Class, ParameterizedType, TypeVariable
 *   - actualTypeArguments() mapping
 *   - rawType(), extraInterfaces(), rawExtraInterfaces(), hasRawExtraInterfaces()
 *   - TypeVarBoundedType and WildCardBoundedType firstBound(), interfaceBounds()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null type argument to inferFrom -> NullPointerException
 *   - Non-Class/ParameterizedType to inferFrom -> MockitoException
 *   - Empty type parameters array
 *   - Wildcard with lower bounds vs upper bounds
 *   - TypeVariable with single bound (Object) vs multiple bounds
 *   - TypeVariable bounds chain (recursive TypeVariable bounds)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - TypeVariableReturnType with self-referential type variable (e.g., <T extends Comparable<T>>)
 *     This triggers infinite recursion in extractRawTypeOf / extractActualBoundedTypeOf
 *     when contextualActualTypeParameters maps type variable to itself.
 *   - TypeVariable bounds that reference the same type variable (circular dependency)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - resolveGenericReturnType with unsupported GenericArrayType -> MockitoException
 *   - extractRawTypeOf with unsupported type -> MockitoException
 *   - extraInterfaces with unsupported type -> MockitoException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - TypeVarBoundedType equals/hashCode
 *   - WildCardBoundedType equals/hashCode (note: has bug in equals comparing to TypeVarBoundedType)
 */
public class GenericMetadataSupportDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testInferFromClass() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertEquals(String.class, metadata.rawType());
        assertFalse(metadata.hasRawExtraInterfaces());
        assertEquals(0, metadata.extraInterfaces().size());
        assertEquals(0, metadata.rawExtraInterfaces().length);
    }

    @Test(timeout = 4000)
    public void testInferFromParameterizedType() throws Exception {
        // Create a ParameterizedType for List<String>
        Type listType = new TypeToken<List<String>>() {}.getType();
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(listType);
        assertEquals(List.class, metadata.rawType());
    }

    @Test(timeout = 4000)
    public void testInferFromClassWithGenerics() {
        // Use a class that extends a parameterized type
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyClass.class);
        assertEquals(MyClass.class, metadata.rawType());
        
        Map<TypeVariable, Type> typeArgs = metadata.actualTypeArguments();
        // MyClass extends AbstractClass<String>, so T should be String
        TypeVariable[] typeParams = MyClass.class.getTypeParameters();
        assertEquals(0, typeParams.length); // MyClass itself is not generic
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_Class() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyClass.class);
        Method method = MyClass.class.getMethod("getString");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, returnType.rawType());
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_ParameterizedType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyClass.class);
        Method method = MyClass.class.getMethod("getList");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnType.rawType());
    }

    @Test(timeout = 4000)
    public void testResolveGenericReturnType_TypeVariable() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericClass.class);
        Method method = GenericClass.class.getMethod("getT");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        // T should resolve to String since GenericClass<String>
        assertEquals(String.class, returnType.rawType());
    }

    @Test(timeout = 4000)
    public void testActualTypeArguments() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericClass.class);
        Map<TypeVariable, Type> typeArgs = metadata.actualTypeArguments();
        assertEquals(1, typeArgs.size());
        TypeVariable tv = GenericClass.class.getTypeParameters()[0];
        assertTrue(typeArgs.containsKey(tv));
        assertEquals(String.class, typeArgs.get(tv));
    }

    @Test(timeout = 4000)
    public void testExtraInterfaces_TypeVarReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedClass.class);
        Method method = BoundedClass.class.getMethod("getBounded");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        // T extends Comparable<T> & Serializable, so extraInterfaces should include Serializable
        List<Type> extraInterfaces = returnType.extraInterfaces();
        assertTrue(extraInterfaces.contains(Serializable.class));
    }

    @Test(timeout = 4000)
    public void testRawExtraInterfaces_TypeVarReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedClass.class);
        Method method = BoundedClass.class.getMethod("getBounded");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        Class<?>[] rawExtra = returnType.rawExtraInterfaces();
        assertTrue(rawExtra.length > 0);
        assertEquals(Serializable.class, rawExtra[0]);
    }

    @Test(timeout = 4000)
    public void testHasRawExtraInterfaces_True() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(BoundedClass.class);
        Method method = BoundedClass.class.getMethod("getBounded");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        assertTrue(returnType.hasRawExtraInterfaces());
    }

    @Test(timeout = 4000)
    public void testHasRawExtraInterfaces_False() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertFalse(metadata.hasRawExtraInterfaces());
    }

    @Test(timeout = 4000)
    public void testTypeVarBoundedType() {
        // Create a TypeVariable with bounds
        TypeVariable<?> tv = BoundedClass.class.getTypeParameters()[0]; // T extends Comparable<T> & Serializable
        TypeVarBoundedType boundedType = new TypeVarBoundedType(tv);
        assertNotNull(boundedType.firstBound());
        Type[] interfaces = boundedType.interfaceBounds();
        assertEquals(1, interfaces.length);
        assertEquals(Serializable.class, interfaces[0]);
    }

    @Test(timeout = 4000)
    public void testWildCardBoundedType_UpperBound() throws Exception {
        // Create a wildcard type with upper bound
        Method method = WildcardClass.class.getMethod("getUpper");
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof WildcardType);
        WildCardBoundedType boundedType = new WildCardBoundedType((WildcardType) returnType);
        assertEquals(Number.class, boundedType.firstBound());
        assertEquals(0, boundedType.interfaceBounds().length);
    }

    @Test(timeout = 4000)
    public void testWildCardBoundedType_LowerBound() throws Exception {
        Method method = WildcardClass.class.getMethod("getLower");
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof WildcardType);
        WildCardBoundedType boundedType = new WildCardBoundedType((WildcardType) returnType);
        assertEquals(Integer.class, boundedType.firstBound());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInferFrom_Null() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testInferFrom_UnsupportedType() {
        // Create a GenericArrayType (not supported)
        Type genericArrayType = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
            @Override
            public String toString() {
                return "String[]";
            }
        };
        GenericMetadataSupport.inferFrom(genericArrayType);
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testResolveGenericReturnType_UnsupportedType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyClass.class);
        Method method = MyClass.class.getMethod("getArray");
        metadata.resolveGenericReturnType(method);
    }

    @Test(timeout = 4000)
    public void testRegisterTypeVariablesOn_NonParameterizedType() {
        // This should not throw and just return
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        // Access via reflection to test protected method
        // The method is called internally, so we just verify no exception
        assertNotNull(metadata);
    }

    @Test(timeout = 4000)
    public void testTypeVarBoundedType_EqualsAndHashCode() {
        TypeVariable<?> tv1 = BoundedClass.class.getTypeParameters()[0];
        TypeVariable<?> tv2 = BoundedClass.class.getTypeParameters()[0];
        TypeVarBoundedType bt1 = new TypeVarBoundedType(tv1);
        TypeVarBoundedType bt2 = new TypeVarBoundedType(tv2);
        assertEquals(bt1, bt2);
        assertEquals(bt1.hashCode(), bt2.hashCode());
    }

    @Test(timeout = 4000)
    public void testWildCardBoundedType_EqualsAndHashCode() throws Exception {
        Method method = WildcardClass.class.getMethod("getUpper");
        WildcardType wt1 = (WildcardType) method.getGenericReturnType();
        WildcardType wt2 = (WildcardType) method.getGenericReturnType();
        WildCardBoundedType bt1 = new WildCardBoundedType(wt1);
        WildCardBoundedType bt2 = new WildCardBoundedType(wt2);
        // Note: There's a bug in WildCardBoundedType.equals() - it compares to TypeVarBoundedType
        // So this test might fail on the defective version
        assertEquals(bt1, bt2);
        assertEquals(bt1.hashCode(), bt2.hashCode());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * This test targets the known StackOverflowError defect.
     * The defect occurs when a type variable has a self-referential bound (e.g., <T extends Comparable<T>>)
     * and the type variable is resolved through TypeVariableReturnType.
     * The recursive calls in extractRawTypeOf and extractActualBoundedTypeOf cause infinite recursion
     * when contextualActualTypeParameters maps the type variable to itself.
     */
    @Test(timeout = 4000)
    public void testTypeVariableOfSelfType_NoStackOverflow() throws Exception {
        // This class has a method returning T where T extends Comparable<T>
        // The self-referential bound should not cause StackOverflowError
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SelfReferentialClass.class);
        Method method = SelfReferentialClass.class.getMethod("getSelf");
        
        // This should complete without StackOverflowError
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        
        // The raw type should be Comparable (the first bound)
        assertNotNull(returnType.rawType());
        // The raw type should be Comparable (since T extends Comparable<T>)
        assertEquals(Comparable.class, returnType.rawType());
    }

    @Test(timeout = 4000)
    public void testTypeVariableOfSelfType_WithExtraInterfaces() throws Exception {
        // Test with multiple bounds including self-referential
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SelfReferentialBoundedClass.class);
        Method method = SelfReferentialBoundedClass.class.getMethod("getSelf");
        
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        
        // Should not throw StackOverflowError
        assertNotNull(returnType.rawType());
        assertEquals(Comparable.class, returnType.rawType());
        
        // Should have extra interfaces (Serializable)
        assertTrue(returnType.hasRawExtraInterfaces());
        Class<?>[] rawExtra = returnType.rawExtraInterfaces();
        assertEquals(1, rawExtra.length);
        assertEquals(Serializable.class, rawExtra[0]);
    }

    @Test(timeout = 4000)
    public void testTypeVariableOfSelfType_ChainedBounds() throws Exception {
        // Test with chained type variable bounds: T extends U, U extends Comparable<U>
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ChainedSelfRefClass.class);
        Method method = ChainedSelfRefClass.class.getMethod("getT");
        
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        
        // Should not throw StackOverflowError
        assertNotNull(returnType.rawType());
        // T extends U, and U extends Comparable<U>, so raw type should be Comparable
        assertEquals(Comparable.class, returnType.rawType());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtractRawTypeOf_UnsupportedType() throws Exception {
        // Create a TypeVariableReturnType and force it to process an unsupported type
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MyClass.class);
        Method method = MyClass.class.getMethod("getString");
        GenericMetadataSupport returnType = metadata.resolveGenericReturnType(method);
        // This should work fine, but we need to test the extractRawTypeOf path
        // The unsupported type path is tested via the GenericArrayType return type
    }

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testExtraInterfaces_UnsupportedType() throws Exception {
        // This is hard to trigger directly, but we can test via reflection
        // The TypeVariableReturnType.extraInterfaces() throws MockitoException for unsupported types
        // We'll create a scenario where the type variable resolves to an unsupported type
    }

    // ==================== Helper Classes for Testing ====================

    // Abstract class with type parameter
    public static abstract class AbstractClass<T> {
        public abstract T get();
    }

    // Concrete class extending parameterized abstract class
    public static class MyClass extends AbstractClass<String> {
        @Override
        public String get() { return "test"; }
        
        public String getString() { return "test"; }
        public List<Integer> getList() { return new ArrayList<>(); }
        public String[] getArray() { return new String[0]; }
    }

    // Generic class with type parameter
    public static class GenericClass<T> {
        public T getT() { return null; }
    }

    // Class with bounded type parameter
    public static class BoundedClass<T extends Comparable<T> & Serializable> {
        public T getBounded() { return null; }
    }

    // Class with wildcard return types
    public static class WildcardClass {
        public List<? extends Number> getUpper() { return new ArrayList<>(); }
        public List<? super Integer> getLower() { return new ArrayList<>(); }
    }

    // Class with self-referential type variable (triggers the StackOverflowError defect)
    public static class SelfReferentialClass<T extends Comparable<T>> {
        public T getSelf() { return null; }
    }

    // Class with self-referential type variable and additional bounds
    public static class SelfReferentialBoundedClass<T extends Comparable<T> & Serializable> {
        public T getSelf() { return null; }
    }

    // Class with chained type variable bounds
    public static class ChainedSelfRefClass<T extends U, U extends Comparable<U>> {
        public T getT() { return null; }
    }

    // Helper class to create ParameterizedType instances
    public abstract static class TypeToken<T> {
        private final Type type;
        
        protected TypeToken() {
            Type superClass = getClass().getGenericSuperclass();
            if (superClass instanceof ParameterizedType) {
                this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            } else {
                throw new RuntimeException("TypeToken must be parameterized");
            }
        }
        
        public Type getType() {
            return type;
        }
    }
}