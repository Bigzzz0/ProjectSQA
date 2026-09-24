package com.fasterxml.jackson.databind.type;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.type.TypeBindings
 *
 * 1. Factory Methods & Stash Optimizations:
 *    - create(Class, List<JavaType>): null list, empty list, populated list.
 *    - create(Class, JavaType[]): null array, length 0, 1 (delegates to paramsFor1),
 *      2 (delegates to paramsFor2), >=3 (explicit TypeVariable traversal).
 *    - TypeParamStash.paramsFor1 branches: Collection, List, ArrayList, AbstractList,
 *      Iterable, Custom Single-Type-Var Class, Non-generic Class (error branch).
 *    - TypeParamStash.paramsFor2 branches: Map, HashMap, LinkedHashMap, Custom Two-Type-Var
 *      Class, Non-generic Class (error branch).
 *    - createIfNeeded(Class, JavaType): 0 vars (returns EMPTY), 1 var (normal), >1 vars (error).
 *    - createIfNeeded(Class, JavaType[]): 0 vars (returns EMPTY), matching vars, mismatched vars (error).
 *
 * 2. Unbound Variables:
 *    - withUnboundVariable: initial addition (uvars == null) and chained additions.
 *    - hasUnbound: null uvars branch, match found (reverse iteration), match not found.
 *
 * 3. Accessors & Resolution:
 *    - findBoundType: name not found, found simple type, found ResolvedRecursiveType with
 *      selfReferencedType == null, found ResolvedRecursiveType with selfReferencedType != null.
 *    - getBoundName & getBoundType: negative index, out-of-bounds index, valid indices.
 *    - getTypeParameters: empty branch vs populated list.
 *    - typeParameterArray: protected accessor array check.
 *
 * 4. Ground-Truth Defect & Boundary Alignment (Issue 1215 / TypeRefinementForMap1215Test):
 *    - Resolution and binding creation with empty / refined bindings where generic Map types
 *      require exact type parameter alignment and recursive type resolution integrity.
 *
 * 5. Lifecycle, Equality, and Contract:
 *    - equals: identity, null, different class, different size, different type content, equal.
 *    - hashCode: consistency contract.
 *    - toString: empty bindings ("<>") vs multiple signatures separated by comma.
 *    - readResolve: empty canonicalization to EMPTY vs non-empty identity preservation.
 *    - Private Constructor reflection: mismatched names vs types length validation.
 */
public class TypeBindingsGeminiTest {

    // Generic test helper classes
    private static class SingleParam<T> { }
    private static class TwoParams<K, V> { }
    private static class ThreeParams<A, B, C> { }
    private static class NonGeneric { }

    private final TypeFactory _tf = TypeFactory.defaultInstance();
    private final JavaType _stringType = _tf.constructType(String.class);
    private final JavaType _intType = _tf.constructType(Integer.class);
    private final JavaType _boolType = _tf.constructType(Boolean.class);

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyBindingsState() {
        TypeBindings empty = TypeBindings.emptyBindings();
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertEquals("<>", empty.toString());
        assertTrue(empty.getTypeParameters().isEmpty());
        assertNull(empty.getBoundName(0));
        assertNull(empty.getBoundType(0));
        assertNull(empty.getBoundName(-1));
        assertNull(empty.getBoundType(-1));
        assertNull(empty.findBoundType("T"));
        assertFalse(empty.hasUnbound("T"));
        assertNotNull(empty.typeParameterArray());
        assertEquals(0, empty.typeParameterArray().length);
    }

    @Test(timeout = 4000)
    public void testCreateFromList() {
        TypeBindings bEmptyNull = TypeBindings.create(NonGeneric.class, (List<JavaType>) null);
        assertTrue(bEmptyNull.isEmpty());

        TypeBindings bEmptyList = TypeBindings.create(NonGeneric.class, Collections.<JavaType>emptyList());
        assertTrue(bEmptyList.isEmpty());

        List<JavaType> list1 = Collections.singletonList(_stringType);
        TypeBindings b1 = TypeBindings.create(SingleParam.class, list1);
        assertEquals(1, b1.size());
        assertEquals("T", b1.getBoundName(0));
        assertEquals(_stringType, b1.getBoundType(0));

        List<JavaType> list2 = Arrays.asList(_stringType, _intType);
        TypeBindings b2 = TypeBindings.create(TwoParams.class, list2);
        assertEquals(2, b2.size());
        assertEquals("K", b2.getBoundName(0));
        assertEquals("V", b2.getBoundName(1));
        assertEquals(_stringType, b2.getBoundType(0));
        assertEquals(_intType, b2.getBoundType(1));
    }

    @Test(timeout = 4000)
    public void testCreateFromJavaTypeArray() {
        TypeBindings bNull = TypeBindings.create(NonGeneric.class, (JavaType[]) null);
        assertTrue(bNull.isEmpty());

        TypeBindings b3 = TypeBindings.create(ThreeParams.class, new JavaType[] { _stringType, _intType, _boolType });
        assertEquals(3, b3.size());
        assertEquals("A", b3.getBoundName(0));
        assertEquals("B", b3.getBoundName(1));
        assertEquals("C", b3.getBoundName(2));
        assertEquals(_stringType, b3.getBoundType(0));
        assertEquals(_intType, b3.getBoundType(1));
        assertEquals(_boolType, b3.getBoundType(2));
        assertEquals(3, b3.typeParameterArray().length);
    }

    @Test(timeout = 4000)
    public void testTypeParamStashParamsFor1() {
        TypeBindings bList = TypeBindings.create(List.class, _stringType);
        assertEquals(1, bList.size());
        assertEquals("E", bList.getBoundName(0));

        TypeBindings bArrayList = TypeBindings.create(ArrayList.class, _stringType);
        assertEquals(1, bArrayList.size());
        assertEquals("E", bArrayList.getBoundName(0));

        TypeBindings bCollection = TypeBindings.create(Collection.class, _stringType);
        assertEquals(1, bCollection.size());
        assertEquals("E", bCollection.getBoundName(0));

        TypeBindings bAbstractList = TypeBindings.create(AbstractList.class, _stringType);
        assertEquals(1, bAbstractList.size());
        assertEquals("E", bAbstractList.getBoundName(0));

        TypeBindings bIterable = TypeBindings.create(Iterable.class, _stringType);
        assertEquals(1, bIterable.size());
        assertEquals("T", bIterable.getBoundName(0));

        TypeBindings bCustom = TypeBindings.create(SingleParam.class, _stringType);
        assertEquals(1, bCustom.size());
        assertEquals("T", bCustom.getBoundName(0));
    }

    @Test(timeout = 4000)
    public void testTypeParamStashParamsFor2() {
        TypeBindings bMap = TypeBindings.create(Map.class, _stringType, _intType);
        assertEquals(2, bMap.size());
        assertEquals("K", bMap.getBoundName(0));
        assertEquals("V", bMap.getBoundName(1));

        TypeBindings bHashMap = TypeBindings.create(HashMap.class, _stringType, _intType);
        assertEquals(2, bHashMap.size());
        assertEquals("K", bHashMap.getBoundName(0));
        assertEquals("V", bHashMap.getBoundName(1));

        TypeBindings bLinkedHashMap = TypeBindings.create(LinkedHashMap.class, _stringType, _intType);
        assertEquals(2, bLinkedHashMap.size());
        assertEquals("K", bLinkedHashMap.getBoundName(0));
        assertEquals("V", bLinkedHashMap.getBoundName(1));

        TypeBindings bCustom = TypeBindings.create(TwoParams.class, _stringType, _intType);
        assertEquals(2, bCustom.size());
        assertEquals("K", bCustom.getBoundName(0));
        assertEquals("V", bCustom.getBoundName(1));
    }

    @Test(timeout = 4000)
    public void testCreateIfNeededVariants() {
        TypeBindings b1 = TypeBindings.createIfNeeded(NonGeneric.class, _stringType);
        assertTrue(b1.isEmpty());

        TypeBindings b2 = TypeBindings.createIfNeeded(SingleParam.class, _stringType);
        assertEquals(1, b2.size());
        assertEquals("T", b2.getBoundName(0));

        TypeBindings b3 = TypeBindings.createIfNeeded(NonGeneric.class, new JavaType[] { _stringType });
        assertTrue(b3.isEmpty());

        TypeBindings b4 = TypeBindings.createIfNeeded(TwoParams.class, new JavaType[] { _stringType, _intType });
        assertEquals(2, b4.size());
        assertEquals("K", b4.getBoundName(0));
        assertEquals("V", b4.getBoundName(1));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Unbound Variables
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnboundVariablesChaining() {
        TypeBindings base = TypeBindings.create(SingleParam.class, _stringType);
        assertFalse(base.hasUnbound("X"));
        assertFalse(base.hasUnbound("Y"));

        TypeBindings withX = base.withUnboundVariable("X");
        assertTrue(withX.hasUnbound("X"));
        assertFalse(withX.hasUnbound("Y"));

        TypeBindings withXY = withX.withUnboundVariable("Y");
        assertTrue(withXY.hasUnbound("X"));
        assertTrue(withXY.hasUnbound("Y"));
        assertFalse(withXY.hasUnbound("Z"));
    }

    @Test(timeout = 4000)
    public void testIndexBoundaries() {
        TypeBindings b = TypeBindings.create(TwoParams.class, _stringType, _intType);
        assertNull(b.getBoundName(-1));
        assertNull(b.getBoundName(2));
        assertNull(b.getBoundName(100));

        assertNull(b.getBoundType(-1));
        assertNull(b.getBoundType(2));
        assertNull(b.getBoundType(100));

        assertEquals("K", b.getBoundName(0));
        assertEquals("V", b.getBoundName(1));
        assertEquals(_stringType, b.getBoundType(0));
        assertEquals(_intType, b.getBoundType(1));
    }

    @Test(timeout = 4000)
    public void testFindBoundTypeWithRecursiveType() {
        TypeBindings b = TypeBindings.create(SingleParam.class, _stringType);
        assertEquals(_stringType, b.findBoundType("T"));
        assertNull(b.findBoundType("Unknown"));

        ResolvedRecursiveType rrtUnresolved = new ResolvedRecursiveType(SingleParam.class, TypeBindings.emptyBindings());
        TypeBindings bRecursive1 = TypeBindings.create(SingleParam.class, rrtUnresolved);
        JavaType resolved1 = bRecursive1.findBoundType("T");
        assertSame(rrtUnresolved, resolved1);

        ResolvedRecursiveType rrtResolved = new ResolvedRecursiveType(SingleParam.class, TypeBindings.emptyBindings());
        rrtResolved.setReference(_stringType);
        TypeBindings bRecursive2 = TypeBindings.create(SingleParam.class, rrtResolved);
        JavaType resolved2 = bRecursive2.findBoundType("T");
        assertEquals(_stringType, resolved2);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (TypeRefinementForMap1215 Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapRefinementBindingsIntegrity() {
        // Targets known defect where Map type refinement expects exact bindings synchronization
        JavaType keyType = _tf.constructType(String.class);
        JavaType valType = _tf.constructType(Object.class);

        TypeBindings mapBindings = TypeBindings.create(Map.class, keyType, valType);
        assertEquals(2, mapBindings.size());
        assertEquals("K", mapBindings.getBoundName(0));
        assertEquals("V", mapBindings.getBoundName(1));
        assertEquals(keyType, mapBindings.getBoundType(0));
        assertEquals(valType, mapBindings.getBoundType(1));

        // Refined map subclass bindings resolution
        TypeBindings hashMapBindings = TypeBindings.createIfNeeded(HashMap.class, new JavaType[] { keyType, valType });
        assertEquals(2, hashMapBindings.size());
        assertEquals(keyType, hashMapBindings.findBoundType("K"));
        assertEquals(valType, hashMapBindings.findBoundType("V"));

        // Ensure toString generates valid signatures without NPE
        String repr = mapBindings.toString();
        assertTrue(repr.startsWith("<"));
        assertTrue(repr.endsWith(">"));
        assertTrue(repr.contains(keyType.getGenericSignature()));
        assertTrue(repr.contains(valType.getGenericSignature()));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateWithMismatch1() {
        try {
            TypeBindings.create(NonGeneric.class, _stringType);
            fail("Expected IllegalArgumentException for NonGeneric class expecting 0 params");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 1 type parameter: class expects 0"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithMismatch2() {
        try {
            TypeBindings.create(SingleParam.class, _stringType, _intType);
            fail("Expected IllegalArgumentException for SingleParam class expecting 1 param");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 2 type parameters: class expects 1"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithMismatchArraySingular() {
        try {
            TypeBindings.create(TwoParams.class, new JavaType[] { _stringType });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 1 type parameter: class expects 2"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateWithMismatchArrayPlural() {
        try {
            TypeBindings.create(SingleParam.class, new JavaType[] { _stringType, _intType, _boolType });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 3 type parameters: class expects 1"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateIfNeededMismatch1() {
        try {
            TypeBindings.createIfNeeded(TwoParams.class, _stringType);
            fail("Expected IllegalArgumentException for TwoParams class expecting 2 params");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 1 type parameter: class expects 2"));
        }
    }

    @Test(timeout = 4000)
    public void testCreateIfNeededMismatchArray() {
        try {
            TypeBindings.createIfNeeded(TwoParams.class, new JavaType[] { _stringType });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 1 type parameter: class expects 2"));
        }

        try {
            TypeBindings.createIfNeeded(SingleParam.class, (JavaType[]) null);
            fail("Expected IllegalArgumentException for null array with generic class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("with 0 type parameters: class expects 1"));
        }
    }

    @Test(timeout = 4000)
    public void testPrivateConstructorDefensiveGuard() throws Exception {
        Constructor<TypeBindings> c = TypeBindings.class.getDeclaredConstructor(
                String[].class, JavaType[].class, String[].class);
        c.setAccessible(true);

        try {
            c.newInstance(new String[] { "A" }, new JavaType[0], null);
            fail("Expected IllegalArgumentException on mismatched names and types");
        } catch (InvocationTargetException ite) {
            assertTrue(ite.getCause() instanceof IllegalArgumentException);
            assertTrue(ite.getCause().getMessage().contains("Mismatching names (1), types (0)"));
        }

        TypeBindings bNulls = c.newInstance(null, null, null);
        assertTrue(bNulls.isEmpty());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        TypeBindings b1a = TypeBindings.create(TwoParams.class, _stringType, _intType);
        TypeBindings b1b = TypeBindings.create(TwoParams.class, _stringType, _intType);
        TypeBindings b2 = TypeBindings.create(TwoParams.class, _stringType, _boolType);
        TypeBindings b3 = TypeBindings.create(SingleParam.class, _stringType);

        assertEquals(b1a, b1a);
        assertEquals(b1a, b1b);
        assertEquals(b1b, b1a);
        assertEquals(b1a.hashCode(), b1b.hashCode());

        assertFalse(b1a.equals(null));
        assertFalse(b1a.equals("not-a-type-binding"));
        assertFalse(b1a.equals(b2));
        assertFalse(b1a.equals(b3));
    }

    @Test(timeout = 4000)
    public void testSerializationLifecycle() throws Exception {
        TypeBindings empty = TypeBindings.emptyBindings();
        ByteArrayOutputStream baosEmpty = new ByteArrayOutputStream();
        ObjectOutputStream oosEmpty = new ObjectOutputStream(baosEmpty);
        oosEmpty.writeObject(empty);
        oosEmpty.close();

        ObjectInputStream oisEmpty = new ObjectInputStream(new ByteArrayInputStream(baosEmpty.toByteArray()));
        Object deserializedEmpty = oisEmpty.readResolve();
        if (deserializedEmpty == null) {
            deserializedEmpty = oisEmpty.readObject();
        }
        assertSame(TypeBindings.emptyBindings(), deserializedEmpty);

        TypeBindings populated = TypeBindings.create(TwoParams.class, _stringType, _intType);
        ByteArrayOutputStream baosPop = new ByteArrayOutputStream();
        ObjectOutputStream oosPop = new ObjectOutputStream(baosPop);
        oosPop.writeObject(populated);
        oosPop.close();

        ObjectInputStream oisPop = new ObjectInputStream(new ByteArrayInputStream(baosPop.toByteArray()));
        Object deserializedPop = oisPop.readObject();
        assertEquals(populated, deserializedPop);
        assertNotSame(populated, deserializedPop);
    }
}