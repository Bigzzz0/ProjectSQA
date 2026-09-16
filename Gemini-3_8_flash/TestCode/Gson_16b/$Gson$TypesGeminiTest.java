package com.google.gson.internal;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================
 * TARGET CLASS: com.google.gson.internal.$Gson$Types
 *
 * CRITICAL DEFECT TARGET:
 * - Recursive type variable resolution infinite recursion:
 *   com.google.gson.internal.bind.RecursiveTypesResolveTest::testRecursiveTypeVariablesResolve1
 *   com.google.gson.internal.bind.RecursiveTypesResolveTest::testRecursiveTypeVariablesResolve12
 *   Failure mode: java.lang.StackOverflowError when resolving recursive type bounds
 *   (e.g., T extends Test1<T>, T extends Comparable<T>, or wildcard types containing recursive bounds).
 *
 * COVERAGE PARTITIONS:
 * 1. Constructor: Defensive private constructor check (UnsupportedOperationException).
 * 2. Factory Methods:
 *    - newParameterizedTypeWithOwner (static vs non-static enclosing, primitive check, null check).
 *    - arrayOf (GenericArrayTypeImpl wrapping).
 *    - subtypeOf / supertypeOf (WildcardType handling, upper/lower bound unpacking, canonicalization).
 * 3. Canonicalization & Raw Types:
 *    - canonicalize: Class, GenericArrayType, ParameterizedType, WildcardType, unknown Type.
 *    - getRawType: Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType, invalid types.
 * 4. Equality & HashCode:
 *    - equals: identity, null vs non-null, Class vs Class, ParameterizedType vs ParameterizedType,
 *              GenericArrayType vs GenericArrayType, WildcardType vs WildcardType,
 *              TypeVariable vs TypeVariable (same name & declaration vs different), cross-type mismatches.
 *    - hashCode: consistency with equals, null-handling.
 * 5. Reflection Traversal & Resolution:
 *    - getGenericSupertype: raw == toResolve, interfaces hierarchy, class hierarchy, unresolved.
 *    - getSupertype: invalid sub-super relationship guard, resolution.
 *    - getArrayComponentType: GenericArrayType vs Class vs ClassCastException.
 *    - getCollectionElementType: Collection, Wildcard collection, Parameterized collection.
 *    - getMapKeyAndValueTypes: Properties.class workaround, ParameterizedType Map, raw Map.
 *    - resolve: Class array, GenericArrayType, ParameterizedType (owner / args change vs unchanged),
 *               WildcardType (upper / lower change vs unchanged), TypeVariable resolution.
 *    - declaringClassOf: Class GenericDeclaration vs Method GenericDeclaration (null).
 * 6. Object Contracts & Serialization:
 *    - toString, equals, hashCode, Serializable round-trip for ParameterizedTypeImpl,
 *      GenericArrayTypeImpl, WildcardTypeImpl.
 * ====================================================================================
 */

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.RandomAccess;
import java.util.Set;

import static org.junit.Assert.*;

public class $Gson$TypesGeminiTest {

  // ====================================================================================
  // Helper Classes for Type Resolution & Hierarchy Testing
  // ====================================================================================

  static class Outer {
    class InnerNonStatic {}
    static class InnerStatic<T> {}
  }

  interface BaseInterface<T> {}
  interface SubInterface<T> extends BaseInterface<T> {}
  static class ImplementingClass implements SubInterface<String> {}

  static class SuperClass<T> {
    T superField;
  }
  static class SubClass<E> extends SuperClass<E> {
    E subField;
  }
  static class ConcreteSubClass extends SuperClass<Integer> {}

  // Recursive generic structures for defect testing
  static class TestRecursive1<T extends TestRecursive1<T>> {
    T t;
  }
  static class HolderRecursive1 {
    TestRecursive1<?> recursiveField;
  }

  static class TestRecursive12<T extends Comparable<T>> {
    T t;
  }
  static class HolderRecursive12 {
    TestRecursive12<?> recursiveField;
  }

  static class TestRecursiveBounded<T extends List<T>> {
    T t;
  }
  static class HolderRecursiveBounded {
    TestRecursiveBounded<?> recursiveField;
  }

  static class MethodTypeVariableHolder {
    <M> void genericMethod(M param) {}
  }

  // ====================================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ====================================================================================

  @Test(timeout = 4000)
  public void testNewParameterizedTypeWithOwnerSimple() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertNull(pt.getOwnerType());
    assertEquals(List.class, pt.getRawType());
    assertArrayEquals(new Type[] { String.class }, pt.getActualTypeArguments());
    assertEquals("java.util.List<java.lang.String>", pt.toString());
  }

  @Test(timeout = 4000)
  public void testNewParameterizedTypeWithEnclosingOwner() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(
        Outer.class, Outer.InnerNonStatic.class, new Type[0]);
    assertEquals(Outer.class, pt.getOwnerType());
    assertEquals(Outer.InnerNonStatic.class, pt.getRawType());
    assertEquals(0, pt.getActualTypeArguments().length);
  }

  @Test(timeout = 4000)
  public void testArrayOf() {
    GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
    assertEquals(String.class, arrayType.getGenericComponentType());
    assertEquals("java.lang.String[]", arrayType.toString());
  }

  @Test(timeout = 4000)
  public void testSubtypeOf() {
    WildcardType wildcard = $Gson$Types.subtypeOf(CharSequence.class);
    assertArrayEquals(new Type[] { CharSequence.class }, wildcard.getUpperBounds());
    assertArrayEquals($Gson$Types.EMPTY_TYPE_ARRAY, wildcard.getLowerBounds());
    assertEquals("? extends java.lang.CharSequence", wildcard.toString());

    WildcardType objectWildcard = $Gson$Types.subtypeOf(Object.class);
    assertEquals("?", objectWildcard.toString());

    WildcardType unwrapped = $Gson$Types.subtypeOf(wildcard);
    assertArrayEquals(new Type[] { CharSequence.class }, unwrapped.getUpperBounds());
  }

  @Test(timeout = 4000)
  public void testSupertypeOf() {
    WildcardType wildcard = $Gson$Types.supertypeOf(String.class);
    assertArrayEquals(new Type[] { Object.class }, wildcard.getUpperBounds());
    assertArrayEquals(new Type[] { String.class }, wildcard.getLowerBounds());
    assertEquals("? super java.lang.String", wildcard.toString());

    WildcardType unwrapped = $Gson$Types.supertypeOf(wildcard);
    assertArrayEquals(new Type[] { String.class }, unwrapped.getLowerBounds());
  }

  @Test(timeout = 4000)
  public void testCanonicalize() {
    assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    Type canonicalArray = $Gson$Types.canonicalize(String[].class);
    assertTrue(canonicalArray instanceof GenericArrayType);
    assertEquals(String.class, ((GenericArrayType) canonicalArray).getGenericComponentType());

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type canonicalPt = $Gson$Types.canonicalize(pt);
    assertEquals(pt, canonicalPt);

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    assertEquals(gat, $Gson$Types.canonicalize(gat));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(wt, $Gson$Types.canonicalize(wt));

    Type customType = new Type() {
      @Override public String toString() { return "custom"; }
    };
    assertSame(customType, $Gson$Types.canonicalize(customType));
  }

  @Test(timeout = 4000)
  public void testGetRawType() {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(gat));

    TypeVariable<?> tv = SuperClass.class.getTypeParameters()[0];
    assertEquals(Object.class, $Gson$Types.getRawType(tv));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(Number.class, $Gson$Types.getRawType(wt));
  }

  @Test(timeout = 4000)
  public void testTypeToString() {
    assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", $Gson$Types.typeToString(pt));
  }

  @Test(timeout = 4000)
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType gat = $Gson$Types.arrayOf(Integer.class);
    assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gat));
    assertNull($Gson$Types.getArrayComponentType(String.class));
  }

  @Test(timeout = 4000)
  public void testGetCollectionElementType() {
    ParameterizedType listOfString = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(listOfString, List.class));

    assertEquals(Object.class, $Gson$Types.getCollectionElementType(Collection.class, Collection.class));

    WildcardType wt = $Gson$Types.subtypeOf(listOfString);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(wt, Collection.class));
  }

  @Test(timeout = 4000)
  public void testGetMapKeyAndValueTypes() {
    Type[] propTypes = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    assertEquals(String.class, propTypes[0]);
    assertEquals(String.class, propTypes[1]);

    ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] mapResolved = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    assertEquals(String.class, mapResolved[0]);
    assertEquals(Integer.class, mapResolved[1]);

    Type[] rawMapResolved = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
    assertEquals(Object.class, rawMapResolved[0]);
    assertEquals(Object.class, rawMapResolved[1]);
  }

  // ====================================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ====================================================================================

  @Test(timeout = 4000)
  public void testEqualHelperNullsAndReferences() {
    assertTrue($Gson$Types.equal(null, null));
    assertFalse($Gson$Types.equal("test", null));
    assertFalse($Gson$Types.equal(null, "test"));
    assertTrue($Gson$Types.equal("test", "test"));
  }

  @Test(timeout = 4000)
  public void testHashCodeOrZero() {
    assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    assertEquals("test".hashCode(), $Gson$Types.hashCodeOrZero("test"));
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeCloningIntegrity() {
    Type[] args = new Type[] { String.class };
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, args);
    Type[] returnedArgs = pt.getActualTypeArguments();
    returnedArgs[0] = Integer.class; // Attempt mutation
    assertEquals(String.class, pt.getActualTypeArguments()[0]);
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeZeroTypeArgumentsToString() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    assertEquals("java.lang.String", pt.toString());
  }

  @Test(timeout = 4000)
  public void testCheckNotPrimitive() {
    $Gson$Types.checkNotPrimitive(String.class);
    try {
      $Gson$Types.checkNotPrimitive(int.class);
      fail("Expected IllegalArgumentException for primitive int.class");
    } catch (IllegalArgumentException expected) {
      // Expected
    }
  }

  @Test(timeout = 4000)
  public void testDeclaringClassOfMethodTypeVariable() throws Exception {
    Method method = MethodTypeVariableHolder.class.getDeclaredMethod("genericMethod", Object.class);
    TypeVariable<?> methodTv = method.getTypeParameters()[0];
    Type resolved = $Gson$Types.resolveTypeVariable(String.class, String.class, methodTv);
    assertSame(methodTv, resolved);
  }

  // ====================================================================================
  // PARTITION C: Defect-Targeted Branch Zone (StackOverflowError on Recursive Types)
  // ====================================================================================

  @Test(timeout = 4000)
  public void testRecursiveTypeVariablesResolve1() throws Exception {
    Type context = HolderRecursive1.class.getDeclaredField("recursiveField").getGenericType();
    Type toResolve = TestRecursive1.class.getDeclaredField("t").getGenericType();
    Type resolved = $Gson$Types.resolve(context, TestRecursive1.class, toResolve);
    assertNotNull(resolved);
  }

  @Test(timeout = 4000)
  public void testRecursiveTypeVariablesResolve12() throws Exception {
    Type context = HolderRecursive12.class.getDeclaredField("recursiveField").getGenericType();
    Type toResolve = TestRecursive12.class.getDeclaredField("t").getGenericType();
    Type resolved = $Gson$Types.resolve(context, TestRecursive12.class, toResolve);
    assertNotNull(resolved);
  }

  @Test(timeout = 4000)
  public void testRecursiveBoundedTypeVariablesResolve() throws Exception {
    Type context = HolderRecursiveBounded.class.getDeclaredField("recursiveField").getGenericType();
    Type toResolve = TestRecursiveBounded.class.getDeclaredField("t").getGenericType();
    Type resolved = $Gson$Types.resolve(context, TestRecursiveBounded.class, toResolve);
    assertNotNull(resolved);
  }

  @Test(timeout = 4000)
  public void testRecursiveParameterizedTypeDirectResolve() {
    ParameterizedType recursivePt = $Gson$Types.newParameterizedTypeWithOwner(
        null, TestRecursive1.class, $Gson$Types.subtypeOf(Object.class));
    Type toResolve = TestRecursive1.class.getTypeParameters()[0];
    Type resolved = $Gson$Types.resolve(recursivePt, TestRecursive1.class, toResolve);
    assertNotNull(resolved);
  }

  // ====================================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // ====================================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorThrowsException() throws Exception {
    Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Constructor should throw UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof UnsupportedOperationException);
    }
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeInnerWithoutOwnerThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(null, Outer.InnerNonStatic.class);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithPrimitiveArgThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testNewParameterizedTypeWithNullArgThrows() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, new Type[] { null });
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetRawTypeInvalidThrows() {
    Type unsupported = new Type() {
      @Override public String toString() { return "unsupported"; }
    };
    $Gson$Types.getRawType(unsupported);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetRawTypeNullThrows() {
    $Gson$Types.getRawType(null);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testGetSupertypeNonAssignableThrows() {
    $Gson$Types.getSupertype(String.class, String.class, List.class);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testWildcardInvalidUpperBoundsThrows() {
    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    new $Gson$Types.newParameterizedTypeWithOwner(null, List.class) {
      {
        $Gson$Types.subtypeOf(int.class); // triggers primitive check in WildcardTypeImpl
      }
    };
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testWildcardLowerBoundNonObjectUpperBoundThrows() {
    WildcardType base = $Gson$Types.supertypeOf(String.class);
    // Lower bound set, but upper bound is not Object.class
    new Object() {
      void run() {
        $Gson$Types.canonicalize(new WildcardType() {
          @Override public Type[] getUpperBounds() { return new Type[] { Number.class }; }
          @Override public Type[] getLowerBounds() { return new Type[] { Integer.class }; }
        });
      }
    }.run();
  }

  // ====================================================================================
  // PARTITION E: Object Lifecycle & Contract Integrity (Equals, HashCode, Serialization)
  // ====================================================================================

  @Test(timeout = 4000)
  public void testEqualsContractForTypes() {
    assertTrue($Gson$Types.equals(null, null));
    assertFalse($Gson$Types.equals(String.class, null));
    assertFalse($Gson$Types.equals(null, String.class));
    assertTrue($Gson$Types.equals(String.class, String.class));
    assertFalse($Gson$Types.equals(String.class, Integer.class));

    // ParameterizedType
    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
    ParameterizedType pt4 = $Gson$Types.newParameterizedTypeWithOwner(null, Set.class, String.class);
    ParameterizedType pt5 = $Gson$Types.newParameterizedTypeWithOwner(Outer.class, Outer.InnerStatic.class, String.class);

    assertTrue($Gson$Types.equals(pt1, pt1));
    assertTrue($Gson$Types.equals(pt1, pt2));
    assertFalse($Gson$Types.equals(pt1, pt3));
    assertFalse($Gson$Types.equals(pt1, pt4));
    assertFalse($Gson$Types.equals(pt1, pt5));
    assertFalse($Gson$Types.equals(pt1, String.class));
    assertEquals(pt1.hashCode(), pt2.hashCode());

    // GenericArrayType
    GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gat3 = $Gson$Types.arrayOf(Integer.class);

    assertTrue($Gson$Types.equals(gat1, gat1));
    assertTrue($Gson$Types.equals(gat1, gat2));
    assertFalse($Gson$Types.equals(gat1, gat3));
    assertFalse($Gson$Types.equals(gat1, String[].class));
    assertEquals(gat1.hashCode(), gat2.hashCode());

    // WildcardType
    WildcardType wt1 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType wt2 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType wt3 = $Gson$Types.supertypeOf(String.class);
    WildcardType wt4 = $Gson$Types.supertypeOf(String.class);
    WildcardType wt5 = $Gson$Types.subtypeOf(String.class);

    assertTrue($Gson$Types.equals(wt1, wt1));
    assertTrue($Gson$Types.equals(wt1, wt2));
    assertFalse($Gson$Types.equals(wt1, wt3));
    assertFalse($Gson$Types.equals(wt1, wt5));
    assertTrue($Gson$Types.equals(wt3, wt4));
    assertFalse($Gson$Types.equals(wt1, String.class));
    assertEquals(wt1.hashCode(), wt2.hashCode());
    assertEquals(wt3.hashCode(), wt4.hashCode());

    // TypeVariable
    TypeVariable<?> tv1 = SuperClass.class.getTypeParameters()[0];
    TypeVariable<?> tv2 = SuperClass.class.getTypeParameters()[0];
    TypeVariable<?> tv3 = SubClass.class.getTypeParameters()[0];

    assertTrue($Gson$Types.equals(tv1, tv1));
    assertTrue($Gson$Types.equals(tv1, tv2));
    assertFalse($Gson$Types.equals(tv1, tv3));
    assertFalse($Gson$Types.equals(tv1, String.class));

    // Unsupported Type implementation
    Type unsupported1 = new Type() {};
    Type unsupported2 = new Type() {};
    assertFalse($Gson$Types.equals(unsupported1, unsupported2));
  }

  @Test(timeout = 4000)
  public void testHierarchyTraversalAndResolution() {
    // Interface resolution
    Type superInterface = $Gson$Types.getGenericSupertype(ImplementingClass.class, ImplementingClass.class, BaseInterface.class);
    assertTrue(superInterface instanceof ParameterizedType);
    assertEquals(String.class, ((ParameterizedType) superInterface).getActualTypeArguments()[0]);

    // Class resolution
    Type genericSuper = $Gson$Types.getGenericSupertype(ConcreteSubClass.class, ConcreteSubClass.class, SuperClass.class);
    assertTrue(genericSuper instanceof ParameterizedType);
    assertEquals(Integer.class, ((ParameterizedType) genericSuper).getActualTypeArguments()[0]);

    // Unrelated resolution returns toResolve
    Type unresolvable = $Gson$Types.getGenericSupertype(List.class, List.class, String.class);
    assertEquals(String.class, unresolvable);

    // Interface context with non-interface target
    Type ifaceToClass = $Gson$Types.getGenericSupertype(BaseInterface.class, BaseInterface.class, Object.class);
    assertEquals(Object.class, ifaceToClass);

    // Supertype resolution
    Type resolvedSuper = $Gson$Types.getSupertype(ArrayList.class, ArrayList.class, Collection.class);
    assertEquals(Collection.class, $Gson$Types.getRawType(resolvedSuper));
  }

  @Test(timeout = 4000)
  public void testResolveTypesComprehensive() throws Exception {
    // Array resolution
    Type arrayType = SuperClass.class.getDeclaredField("superField").getGenericType(); // T
    GenericArrayType arrayOfT = $Gson$Types.arrayOf(arrayType); // T[]
    ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, SuperClass.class, String.class);
    Type resolvedArray = $Gson$Types.resolve(context, SuperClass.class, arrayOfT);
    assertEquals($Gson$Types.arrayOf(String.class), resolvedArray);

    // Class array unchanged
    Type resolvedStringArray = $Gson$Types.resolve(context, SuperClass.class, String[].class);
    assertEquals(String[].class, resolvedStringArray);

    // Wildcard resolution
    WildcardType subtypeOfT = $Gson$Types.subtypeOf(arrayType); // ? extends T
    Type resolvedWildcard = $Gson$Types.resolve(context, SuperClass.class, subtypeOfT);
    assertEquals($Gson$Types.subtypeOf(String.class), resolvedWildcard);

    WildcardType supertypeOfT = $Gson$Types.supertypeOf(arrayType); // ? super T
    Type resolvedSuperWildcard = $Gson$Types.resolve(context, SuperClass.class, supertypeOfT);
    assertEquals($Gson$Types.supertypeOf(String.class), resolvedSuperWildcard);

    // Wildcard unchanged
    WildcardType exactWildcard = $Gson$Types.subtypeOf(Object.class);
    assertSame(exactWildcard, $Gson$Types.resolve(context, SuperClass.class, exactWildcard));

    // ParameterizedType unchanged vs changed
    ParameterizedType ptUnchanged = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertSame(ptUnchanged, $Gson$Types.resolve(context, SuperClass.class, ptUnchanged));

    ParameterizedType ptOfT = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, arrayType);
    Type resolvedPt = $Gson$Types.resolve(context, SuperClass.class, ptOfT);
    assertEquals($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class), resolvedPt);
  }

  @Test(timeout = 4000)
  public void testSerializationRoundTrip() throws Exception {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType ptDeserialized = reserialize(pt);
    assertEquals(pt, ptDeserialized);
    assertEquals(pt.hashCode(), ptDeserialized.hashCode());

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    GenericArrayType gatDeserialized = reserialize(gat);
    assertEquals(gat, gatDeserialized);
    assertEquals(gat.hashCode(), gatDeserialized.hashCode());

    WildcardType wt = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType wtDeserialized = reserialize(wt);
    assertEquals(wt, wtDeserialized);
    assertEquals(wt.hashCode(), wtDeserialized.hashCode());

    WildcardType superWt = $Gson$Types.supertypeOf(String.class);
    WildcardType superWtDeserialized = reserialize(superWt);
    assertEquals(superWt, superWtDeserialized);
    assertEquals(superWt.hashCode(), superWtDeserialized.hashCode());
  }

  @SuppressWarnings("unchecked")
  private static <T> T reserialize(T object) throws Exception {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    new ObjectOutputStream(bytes).writeObject(object);
    return (T) new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())).readObject();
  }
}