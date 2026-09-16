package com.google.gson.internal;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Methods & Decision Branches Under Test:
 * 1. Constructor: $Gson$Types() is private and throws UnsupportedOperationException.
 * 2. newParameterizedTypeWithOwner:
 *    - Owner required for non-static inner classes.
 *    - Owner optional/null for top-level or static classes.
 *    - Primitive type arguments rejection (checkNotPrimitive).
 *    - Null type argument rejection (checkNotNull).
 * 3. arrayOf:
 *    - GenericArrayType wrapping component types, nested array dimensions.
 * 4. subtypeOf & supertypeOf:
 *    - WildcardType construction with upper/lower bounds.
 *    - Multiple bounds check, primitive check, null check.
 * 5. canonicalize:
 *    - Class (regular and array), ParameterizedType, GenericArrayType, WildcardType, unknown custom Type.
 * 6. getRawType:
 *    - Class, ParameterizedType, GenericArrayType, TypeVariable (bounded and unbounded), WildcardType.
 *    - Unhandled types throw IllegalArgumentException.
 * 7. equals & hashCode:
 *    - Equality across combinations of types (Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable).
 *    - Symmetry, transitivity, null safety, mismatched components/arguments/bounds.
 * 8. getGenericSupertype & getSupertype:
 *    - Context matching target directly; interface hierarchies; class inheritance chain up to Object.
 *    - Unrelated class throws IllegalArgumentException via checkArgument.
 * 9. getArrayComponentType:
 *    - GenericArrayType vs Class<?> array.
 * 10. getCollectionElementType & getMapKeyAndValueTypes:
 *     - Collections, Wildcards, Properties special case handling, non-parameterized types defaulting to Object.
 * 11. resolve & resolveTypeVariable:
 *     - TypeVariable resolution via class hierarchy and type argument index.
 *     - Array types resolution (both Class and GenericArrayType).
 *     - ParameterizedType owner and arguments recursion.
 *     - WildcardType bounds resolution.
 * 12. Defect Targets (Ground Truth):
 *     - Recursive type resolution (e.g. T extends Recursive<T>) causing StackOverflowError.
 *     - Double wildcard resolution: subtypeOf(subtypeOf(T)), supertypeOf(supertypeOf(T)),
 *       subtypeOf(supertypeOf(T)), supertypeOf(subtypeOf(T)).
 * -------------------------------------------------------------------------------------------------
 */
public class $Gson$TypesGeminiTest {

  // Test helpers and fixtures
  private static class NonStaticInnerParent {
    class NonStaticInnerChild<T> {}
  }

  private static class StaticNested<T> {}

  private static class GenericBase<A, B> {}
  private static class GenericSub<X> extends GenericBase<X, String> {}
  private static class ConcreteSub extends GenericSub<Integer> {}

  private interface InterfaceBase<T> {}
  private interface InterfaceSub<T> extends InterfaceBase<List<T>> {}
  private static class InterfaceImpl implements InterfaceSub<Double> {}

  private static class RecursiveGeneric<T extends RecursiveGeneric<T>> {}

  private static class CustomType implements Type {
    @Override
    public String getTypeName() {
      return "CustomType";
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testNewParameterizedTypeWithOwnerTopLevel() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertNull(pt.getOwnerType());
    assertEquals(List.class, pt.getRawType());
    assertArrayEquals(new Type[] { String.class }, pt.getActualTypeArguments());
    assertEquals("java.util.List<java.lang.String>", pt.toString());
  }

  @Test(timeout = 4000)
  public void testNewParameterizedTypeWithOwnerStaticNested() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(
        $Gson$TypesGeminiTest.class, StaticNested.class, Integer.class);
    assertEquals($Gson$TypesGeminiTest.class, pt.getOwnerType());
    assertEquals(StaticNested.class, pt.getRawType());
    assertArrayEquals(new Type[] { Integer.class }, pt.getActualTypeArguments());
  }

  @Test(timeout = 4000)
  public void testNewParameterizedTypeWithOwnerNonStaticInner() {
    ParameterizedType owner = $Gson$Types.newParameterizedTypeWithOwner(null, NonStaticInnerParent.class);
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(
        owner, NonStaticInnerParent.NonStaticInnerChild.class, String.class);
    assertEquals(owner, pt.getOwnerType());
    assertEquals(NonStaticInnerParent.NonStaticInnerChild.class, pt.getRawType());
  }

  @Test(timeout = 4000)
  public void testArrayOf() {
    GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
    assertEquals(String.class, arrayType.getGenericComponentType());
    assertEquals("java.lang.String[]", arrayType.toString());

    GenericArrayType arrayOfArray = $Gson$Types.arrayOf(arrayType);
    assertEquals(arrayType, arrayOfArray.getGenericComponentType());
    assertEquals("java.lang.String[][]", arrayOfArray.toString());
  }

  @Test(timeout = 4000)
  public void testSubtypeOf() {
    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertArrayEquals(new Type[] { Number.class }, wt.getUpperBounds());
    assertArrayEquals(new Type[0], wt.getLowerBounds());
    assertEquals("? extends java.lang.Number", wt.toString());

    WildcardType objectWildcard = $Gson$Types.subtypeOf(Object.class);
    assertEquals("?", objectWildcard.toString());
  }

  @Test(timeout = 4000)
  public void testSupertypeOf() {
    WildcardType wt = $Gson$Types.supertypeOf(Number.class);
    assertArrayEquals(new Type[] { Object.class }, wt.getUpperBounds());
    assertArrayEquals(new Type[] { Number.class }, wt.getLowerBounds());
    assertEquals("? super java.lang.Number", wt.toString());
  }

  @Test(timeout = 4000)
  public void testCanonicalize() {
    assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    // Array canonicalization produces GenericArrayTypeImpl
    Type arrayType = $Gson$Types.canonicalize(int[].class);
    assertTrue(arrayType instanceof GenericArrayType);
    assertEquals(int.class, ((GenericArrayType) arrayType).getGenericComponentType());

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type canonicalPt = $Gson$Types.canonicalize(pt);
    assertEquals(pt, canonicalPt);

    GenericArrayType gt = $Gson$Types.arrayOf(String.class);
    assertEquals(gt, $Gson$Types.canonicalize(gt));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(wt, $Gson$Types.canonicalize(wt));

    CustomType custom = new CustomType();
    assertSame(custom, $Gson$Types.canonicalize(custom));
  }

  @Test(timeout = 4000)
  public void testGetRawType() {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gt = $Gson$Types.arrayOf(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(gt));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(Number.class, $Gson$Types.getRawType(wt));

    TypeVariable<?> tv = GenericSub.class.getTypeParameters()[0];
    assertEquals(Object.class, $Gson$Types.getRawType(tv));
  }

  @Test(timeout = 4000)
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType gt = $Gson$Types.arrayOf(Integer.class);
    assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gt));
  }

  @Test(timeout = 4000)
  public void testGetCollectionElementType() {
    ParameterizedType listType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(listType, List.class));

    // Raw collection defaults to Object.class
    assertEquals(Object.class, $Gson$Types.getCollectionElementType(Collection.class, Collection.class));

    // Wildcard collection
    WildcardType wt = $Gson$Types.subtypeOf(listType);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(wt, Collection.class));
  }

  @Test(timeout = 4000)
  public void testGetMapKeyAndValueTypes() {
    // Normal map
    ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] kv = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    assertEquals(2, kv.length);
    assertEquals(String.class, kv[0]);
    assertEquals(Integer.class, kv[1]);

    // Raw map
    Type[] rawKv = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
    assertEquals(Object.class, rawKv[0]);
    assertEquals(Object.class, rawKv[1]);

    // Properties special case
    Type[] propKv = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    assertEquals(String.class, propKv[0]);
    assertEquals(String.class, propKv[1]);
  }

  @Test(timeout = 4000)
  public void testGetGenericSupertype() {
    Type supertype = $Gson$Types.getGenericSupertype(ConcreteSub.class, ConcreteSub.class, GenericBase.class);
    assertTrue(supertype instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) supertype;
    assertEquals(GenericBase.class, pt.getRawType());
    assertArrayEquals(new Type[] { Integer.class, String.class }, pt.getActualTypeArguments());

    Type ifaceSupertype = $Gson$Types.getGenericSupertype(InterfaceImpl.class, InterfaceImpl.class, InterfaceBase.class);
    assertTrue(ifaceSupertype instanceof ParameterizedType);
    assertEquals(InterfaceBase.class, ((ParameterizedType) ifaceSupertype).getRawType());

    // When toResolve equals rawType
    assertEquals(ConcreteSub.class, $Gson$Types.getGenericSupertype(ConcreteSub.class, ConcreteSub.class, ConcreteSub.class));

    // When rawType is unrelated
    assertEquals(Set.class, $Gson$Types.getGenericSupertype(String.class, String.class, Set.class));
  }

  @Test(timeout = 4000)
  public void testResolveParameterizedHierarchy() {
    Type resolved = $Gson$Types.resolve(ConcreteSub.class, ConcreteSub.class,
        GenericBase.class.getTypeParameters()[0]);
    assertEquals(Integer.class, resolved);

    Type resolvedB = $Gson$Types.resolve(ConcreteSub.class, ConcreteSub.class,
        GenericBase.class.getTypeParameters()[1]);
    assertEquals(String.class, resolvedB);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEqualsAndHashCodeContracts() {
    Type pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);

    assertTrue($Gson$Types.equals(pt1, pt2));
    assertFalse($Gson$Types.equals(pt1, pt3));
    assertEquals(pt1.hashCode(), pt2.hashCode());

    Type gt1 = $Gson$Types.arrayOf(String.class);
    Type gt2 = $Gson$Types.arrayOf(String.class);
    Type gt3 = $Gson$Types.arrayOf(Integer.class);

    assertTrue($Gson$Types.equals(gt1, gt2));
    assertFalse($Gson$Types.equals(gt1, gt3));
    assertEquals(gt1.hashCode(), gt2.hashCode());

    Type wt1 = $Gson$Types.subtypeOf(Number.class);
    Type wt2 = $Gson$Types.subtypeOf(Number.class);
    Type wt3 = $Gson$Types.supertypeOf(Number.class);

    assertTrue($Gson$Types.equals(wt1, wt2));
    assertFalse($Gson$Types.equals(wt1, wt3));
    assertEquals(wt1.hashCode(), wt2.hashCode());

    // Null comparisons
    assertTrue($Gson$Types.equals(null, null));
    assertFalse($Gson$Types.equals(pt1, null));
    assertFalse($Gson$Types.equals(null, pt1));

    // Cross-type comparisons
    assertFalse($Gson$Types.equals(pt1, gt1));
    assertFalse($Gson$Types.equals(gt1, wt1));
    assertFalse($Gson$Types.equals(wt1, pt1));
    assertFalse($Gson$Types.equals(String.class, pt1));
    assertFalse($Gson$Types.equals(pt1, new CustomType()));

    // Reflexive
    assertTrue($Gson$Types.equals(pt1, pt1));
    assertTrue($Gson$Types.equals(gt1, gt1));
    assertTrue($Gson$Types.equals(wt1, wt1));
  }

  @Test(timeout = 4000)
  public void testTypeVariableEquality() {
    TypeVariable<?> tv1 = GenericBase.class.getTypeParameters()[0];
    TypeVariable<?> tv2 = GenericBase.class.getTypeParameters()[0];
    TypeVariable<?> tvOther = GenericBase.class.getTypeParameters()[1];

    assertTrue($Gson$Types.equals(tv1, tv2));
    assertFalse($Gson$Types.equals(tv1, tvOther));
    assertFalse($Gson$Types.equals(tv1, String.class));
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeToString() {
    ParameterizedType ptNoArgs = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    assertEquals("java.lang.String", ptNoArgs.toString());

    ParameterizedType ptMultipleArgs = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", ptMultipleArgs.toString());
  }

  @Test(timeout = 4000)
  public void testTypeToStringHelper() {
    assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals("java.util.List<java.lang.String>", $Gson$Types.typeToString(pt));
  }

  @Test(timeout = 4000)
  public void testResolveArrayAndGenericArrayTypes() {
    // Array of unresolved type variable
    TypeVariable<?> tv = GenericSub.class.getTypeParameters()[0];
    GenericArrayType arrayType = $Gson$Types.arrayOf(tv);
    Type resolvedArray = $Gson$Types.resolve(ConcreteSub.class, ConcreteSub.class, arrayType);
    assertEquals($Gson$Types.arrayOf(Integer.class), resolvedArray);

    // Array of concrete class resolves to itself
    Type resolvedConcreteArray = $Gson$Types.resolve(Object.class, Object.class, String[].class);
    assertEquals(String[].class, resolvedConcreteArray);
  }

  @Test(timeout = 4000)
  public void testResolveParameterizedOwnerChanged() {
    ParameterizedType original = $Gson$Types.newParameterizedTypeWithOwner(
        GenericBase.class, StaticNested.class, String.class);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, original);
    assertEquals(original, resolved);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoubleSubtype() {
    Type innerSubtype = $Gson$Types.subtypeOf(Number.class);
    Type outerSubtype = $Gson$Types.subtypeOf(innerSubtype);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, outerSubtype);
    assertEquals($Gson$Types.subtypeOf(Number.class), resolved);
  }

  @Test(timeout = 4000)
  public void testDoubleSupertype() {
    Type innerSupertype = $Gson$Types.supertypeOf(Number.class);
    Type outerSupertype = $Gson$Types.supertypeOf(innerSupertype);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, outerSupertype);
    assertEquals($Gson$Types.supertypeOf(Number.class), resolved);
  }

  @Test(timeout = 4000)
  public void testSubSupertype() {
    Type innerSupertype = $Gson$Types.supertypeOf(Number.class);
    Type outerSubtype = $Gson$Types.subtypeOf(innerSupertype);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, outerSubtype);
    assertEquals($Gson$Types.subtypeOf(Object.class), resolved);
  }

  @Test(timeout = 4000)
  public void testSuperSubtype() {
    Type innerSubtype = $Gson$Types.subtypeOf(Number.class);
    Type outerSupertype = $Gson$Types.supertypeOf(innerSubtype);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, outerSupertype);
    assertEquals($Gson$Types.subtypeOf(Object.class), resolved);
  }

  @Test(timeout = 4000)
  public void testRecursiveResolveSimple() {
    Type t = RecursiveGeneric.class.getTypeParameters()[0];
    Type resolved = $Gson$Types.resolve(RecursiveGeneric.class, RecursiveGeneric.class, t);
    // Should resolve without StackOverflowError and its raw type is RecursiveGeneric
    assertEquals(RecursiveGeneric.class, $Gson$Types.getRawType(resolved));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = UnsupportedOperationException.class)
  public void testPrivateConstructorThrows() throws Throwable {
    Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeNonStaticInnerWithoutOwnerThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(
        null, NonStaticInnerParent.NonStaticInnerChild.class, String.class);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testNewParameterizedTypeNullTypeArgumentThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, (Type) null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNewParameterizedTypePrimitiveArgumentThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSubtypeOfNullThrows() {
    $Gson$Types.subtypeOf(null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testSubtypeOfPrimitiveThrows() {
    $Gson$Types.subtypeOf(int.class);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSupertypeOfNullThrows() {
    $Gson$Types.supertypeOf(null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testSupertypeOfPrimitiveThrows() {
    $Gson$Types.supertypeOf(int.class);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetRawTypeUnsupportedTypeThrows() {
    $Gson$Types.getRawType(new CustomType());
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetRawTypeNullThrows() {
    $Gson$Types.getRawType(null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetSupertypeNotAssignableThrows() {
    $Gson$Types.getSupertype(String.class, String.class, List.class);
  }

  @Test(timeout = 4000, expected = ClassCastException.class)
  public void testGetArrayComponentTypeNonArrayThrows() {
    $Gson$Types.getArrayComponentType(String.class);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetCollectionElementTypeNonCollectionThrows() {
    $Gson$Types.getCollectionElementType(String.class, String.class);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testParameterizedTypeImplEqualsAndHashcodeVariations() {
    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Set.class, String.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(Object.class, List.class, String.class);

    assertNotEquals(pt1, pt2);
    assertNotEquals(pt1, pt3);
    assertNotEquals(pt1, "someString");
    assertNotNull(pt1);
  }

  @Test(timeout = 4000)
  public void testWildcardHashCodeAndToString() {
    WildcardType subtype = $Gson$Types.subtypeOf(Number.class);
    WildcardType supertype = $Gson$Types.supertypeOf(Number.class);

    assertNotEquals(subtype.hashCode(), supertype.hashCode());
    assertFalse(subtype.equals("otherObject"));

    assertEquals("? super java.lang.Number", supertype.toString());
    assertEquals("? extends java.lang.Number", subtype.toString());
  }

  @Test(timeout = 4000)
  public void testGenericArrayTypeHashCodeAndToString() {
    GenericArrayType gat1 = $Gson$Types.arrayOf(Number.class);
    GenericArrayType gat2 = $Gson$Types.arrayOf(Number.class);
    GenericArrayType gat3 = $Gson$Types.arrayOf(String.class);

    assertEquals(gat1.hashCode(), gat2.hashCode());
    assertNotEquals(gat1.hashCode(), gat3.hashCode());
    assertEquals("java.lang.Number[]", gat1.toString());
    assertFalse(gat1.equals("otherObject"));
  }

  @Test(timeout = 4000)
  public void testSerializationIntegrity() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertTrue(pt instanceof Serializable);

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    assertTrue(gat instanceof Serializable);

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    assertTrue(wt instanceof Serializable);
  }
}