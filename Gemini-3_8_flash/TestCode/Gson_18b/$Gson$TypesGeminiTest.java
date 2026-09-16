/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: com.google.gson.internal.$Gson$Types
 * Defects4J Defect: com.google.gson.functional.CollectionTest::testIssue1107
 * Root Cause Analysis:
 *   In $Gson$Types.getSupertype(Type context, Class<?> contextRawType, Class<?> supertype), the comment
 *   states: "// wildcards are useless for resolving supertypes. As the upper bound has the same raw type,
 *   use it instead". However, the defective implementation fails to unwrap the WildcardType into its upper
 *   bound before invoking resolve() and getGenericSupertype(). As a result, when resolving a wildcard context
 *   like `? extends List<SmallClass>`, getGenericSupertype returns the WildcardType itself when matching
 *   raw types. resolveTypeVariable() checks `declaredBy instanceof ParameterizedType`, which evaluates to
 *   false for a WildcardType, causing type resolution to fail and fallback to Object.class. In Gson's
 *   CollectionTypeAdapterFactory, this results in deserializing collection elements as LinkedTreeMap instead
 *   of SmallClass, triggering a ClassCastException at runtime.
 *
 * Branch & Boundary Coverage Matrix:
 *   - Partition A: Core Functional Logic & State Transitions
 *     * canonicalize(): Class, GenericArrayType, ParameterizedType, WildcardType, unsupported custom Type.
 *     * getRawType(): Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType.
 *     * equals(): Reflexive (a == b), Class, ParameterizedType, GenericArrayType, WildcardType, TypeVariable.
 *     * typeToString(): Class vs generic types.
 *     * getGenericSupertype(): interface hierarchy, class hierarchy, rawType == toResolve, toResolve unreachable.
 *     * getArrayComponentType(): GenericArrayType vs Class[].
 *     * getCollectionElementType(): standard ParameterizedType, nested wildcards, raw collection fallback.
 *     * getMapKeyAndValueTypes(): Properties.class special case, ParameterizedType Map, raw Map fallback.
 *     * resolve(): TypeVariable, Array Class, GenericArrayType, ParameterizedType, WildcardType, recursion guards.
 *   - Partition B: Boundary Value Analysis (BVA) & Extremes
 *     * Arrays with 0 bounds, empty type arguments.
 *     * subtypeOf(Object.class) produces `?` vs subtypeOf(CharSequence.class) produces `? extends CharSequence`.
 *     * supertypeOf(bound) produces `? super bound`.
 *     * Type identity with null owners, top-level vs inner classes.
 *   - Partition C: Defect-Targeted Branch Zone (Issue 1107)
 *     * getSupertype() with WildcardType context (e.g. `? extends List<SmallClass>`).
 *     * getCollectionElementType() with WildcardType context.
 *     * Verifying that element type resolves to SmallClass instead of Object.class.
 *   - Partition D: Exception & Defensive Guard Paths
 *     * Private constructor invocation via reflection (UnsupportedOperationException).
 *     * ParameterizedType with primitive type arguments (IllegalArgumentException).
 *     * ParameterizedType for non-static inner class with null owner (IllegalArgumentException).
 *     * WildcardType with >1 bounds or non-Object upper bound when lower bound exists (IllegalArgumentException).
 *     * getRawType(null) or unsupported type (IllegalArgumentException).
 *     * getSupertype() when supertype is not assignable from contextRawType (IllegalArgumentException).
 *   - Partition E: Object Lifecycle & Contract Integrity
 *     * equals() & hashCode() symmetric, transitive, and consistent contracts.
 *     * Serialization round-trip for ParameterizedTypeImpl, GenericArrayTypeImpl, WildcardTypeImpl.
 *     * toString() formatting for all custom type representations.
 * ----------------------------------------------------------------------------------------------------
 */

package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import java.util.Properties;
import java.util.Set;

public class $Gson$TypesGeminiTest {

  // --- Test Fixture Types ---
  public static class SmallClass {
    public String name;
  }

  static class NonStaticInnerParent {
    class NonStaticInnerChild {}
  }

  static class StaticInnerParent {
    static class StaticInnerChild {}
  }

  interface CustomGenericInterface<T> {}
  interface CustomSubInterface<T> extends CustomGenericInterface<T> {}
  static class CustomImplementer implements CustomSubInterface<SmallClass> {}

  static class BaseClass<T> {
    T field;
  }
  static class IntermediateClass<T> extends BaseClass<T> {}
  static class ConcreteSubClass extends IntermediateClass<String> {}

  static class RecursiveGeneric<T extends RecursiveGeneric<T>> {
    T recursiveField;
  }

  static class CustomTypeImpl implements Type {
    @Override
    public String toString() {
      return "CustomTypeImpl";
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCanonicalizeCoreTypes() {
    // Class
    assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    // Array Class becomes GenericArrayTypeImpl
    Type arrayType = $Gson$Types.canonicalize(String[].class);
    assertTrue(arrayType instanceof GenericArrayType);
    assertEquals(String.class, ((GenericArrayType) arrayType).getGenericComponentType());

    // ParameterizedType
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type canonicalPt = $Gson$Types.canonicalize(pt);
    assertEquals(pt, canonicalPt);

    // GenericArrayType
    GenericArrayType gat = $Gson$Types.arrayOf(pt);
    Type canonicalGat = $Gson$Types.canonicalize(gat);
    assertEquals(gat, canonicalGat);

    // WildcardType
    WildcardType wt = $Gson$Types.subtypeOf(CharSequence.class);
    Type canonicalWt = $Gson$Types.canonicalize(wt);
    assertEquals(wt, canonicalWt);

    // Unsupported custom type passes through
    CustomTypeImpl custom = new CustomTypeImpl();
    assertSame(custom, $Gson$Types.canonicalize(custom));
  }

  @Test(timeout = 4000)
  public void testGetRawTypeComprehensive() {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(gat));

    TypeVariable<?> tv = List.class.getTypeParameters()[0];
    assertEquals(Object.class, $Gson$Types.getRawType(tv));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(Number.class, $Gson$Types.getRawType(wt));
  }

  @Test(timeout = 4000)
  public void testTypeToString() {
    assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(pt.toString(), $Gson$Types.typeToString(pt));
  }

  @Test(timeout = 4000)
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType gat = $Gson$Types.arrayOf(Integer.class);
    assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gat));
  }

  @Test(timeout = 4000)
  public void testGetMapKeyAndValueTypes() {
    // Properties special case
    Type[] propTypes = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    assertEquals(2, propTypes.length);
    assertEquals(String.class, propTypes[0]);
    assertEquals(String.class, propTypes[1]);

    // Parameterized Map
    ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] mapTypes = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    assertEquals(2, mapTypes.length);
    assertEquals(String.class, mapTypes[0]);
    assertEquals(Integer.class, mapTypes[1]);

    // Raw Map fallback
    Type[] rawMapTypes = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
    assertEquals(2, rawMapTypes.length);
    assertEquals(Object.class, rawMapTypes[0]);
    assertEquals(Object.class, rawMapTypes[1]);
  }

  @Test(timeout = 4000)
  public void testGetGenericSupertypeResolution() {
    // Directly matching
    assertEquals(String.class, $Gson$Types.getGenericSupertype(String.class, String.class, String.class));

    // Interface hierarchy
    Type resolvedInterface = $Gson$Types.getGenericSupertype(
        CustomImplementer.class, CustomImplementer.class, CustomGenericInterface.class);
    assertTrue(resolvedInterface instanceof ParameterizedType);
    assertEquals(CustomGenericInterface.class, ((ParameterizedType) resolvedInterface).getRawType());

    // Superclass hierarchy
    Type resolvedSuperclass = $Gson$Types.getGenericSupertype(
        ConcreteSubClass.class, ConcreteSubClass.class, BaseClass.class);
    assertTrue(resolvedSuperclass instanceof ParameterizedType);
    assertEquals(BaseClass.class, ((ParameterizedType) resolvedSuperclass).getRawType());

    // Unrelated type fallback
    Type unresolved = $Gson$Types.getGenericSupertype(String.class, String.class, Set.class);
    assertEquals(Set.class, unresolved);
  }

  @Test(timeout = 4000)
  public void testResolveTypeTransitions() {
    // Resolve Class array where component doesn't change
    assertEquals(String[].class, $Gson$Types.resolve(String.class, String.class, String[].class));

    // Resolve GenericArrayType where component changes
    TypeVariable<?> tv = BaseClass.class.getTypeParameters()[0];
    GenericArrayType gat = $Gson$Types.arrayOf(tv);
    ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, BaseClass.class, Integer.class);
    Type resolvedGat = $Gson$Types.resolve(context, BaseClass.class, gat);
    assertEquals($Gson$Types.arrayOf(Integer.class), resolvedGat);

    // Resolve ParameterizedType with type argument change
    ParameterizedType ptWithTv = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, tv);
    Type resolvedPt = $Gson$Types.resolve(context, BaseClass.class, ptWithTv);
    ParameterizedType expectedPt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
    assertEquals(expectedPt, resolvedPt);

    // Resolve WildcardType upper & lower bounds
    WildcardType wtSub = $Gson$Types.subtypeOf(tv);
    Type resolvedWtSub = $Gson$Types.resolve(context, BaseClass.class, wtSub);
    assertEquals($Gson$Types.subtypeOf(Integer.class), resolvedWtSub);

    WildcardType wtSuper = $Gson$Types.supertypeOf(tv);
    Type resolvedWtSuper = $Gson$Types.resolve(context, BaseClass.class, wtSuper);
    assertEquals($Gson$Types.supertypeOf(Integer.class), resolvedWtSuper);

    // Resolve unchanged WildcardType
    WildcardType objectWildcard = $Gson$Types.subtypeOf(Object.class);
    assertSame(objectWildcard, $Gson$Types.resolve(String.class, String.class, objectWildcard));
  }

  @Test(timeout = 4000)
  public void testResolveRecursiveTypeVariableTerminates() {
    TypeVariable<?> tv = RecursiveGeneric.class.getTypeParameters()[0];
    Type resolved = $Gson$Types.resolve(RecursiveGeneric.class, RecursiveGeneric.class, tv);
    assertEquals(tv, resolved);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testWildcardBoundaryRepresentations() {
    WildcardType unbounded = $Gson$Types.subtypeOf(Object.class);
    assertEquals("?", unbounded.toString());
    assertEquals(1, unbounded.getUpperBounds().length);
    assertEquals(Object.class, unbounded.getUpperBounds()[0]);
    assertEquals(0, unbounded.getLowerBounds().length);

    WildcardType subtypeOfSubtype = $Gson$Types.subtypeOf(unbounded);
    assertEquals(unbounded, subtypeOfSubtype);

    WildcardType supertype = $Gson$Types.supertypeOf(String.class);
    assertEquals("? super java.lang.String", supertype.toString());
    assertEquals(1, supertype.getUpperBounds().length);
    assertEquals(Object.class, supertype.getUpperBounds()[0]);
    assertEquals(1, supertype.getLowerBounds().length);
    assertEquals(String.class, supertype.getLowerBounds()[0]);

    WildcardType supertypeOfSupertype = $Gson$Types.supertypeOf(supertype);
    assertEquals(supertype, supertypeOfSupertype);
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeZeroArgumentsBoundary() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    assertEquals(0, pt.getActualTypeArguments().length);
    assertEquals("java.lang.String", pt.toString());
  }

  @Test(timeout = 4000)
  public void testEqualsBoundaryMatrix() {
    assertTrue($Gson$Types.equals(null, null));
    assertFalse($Gson$Types.equals(String.class, null));
    assertFalse($Gson$Types.equals(null, String.class));
    assertTrue($Gson$Types.equals(String.class, String.class));
    assertFalse($Gson$Types.equals(String.class, Integer.class));

    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
    ParameterizedType ptDifferentRaw = $Gson$Types.newParameterizedTypeWithOwner(null, Collection.class, String.class);

    assertTrue($Gson$Types.equals(pt1, pt2));
    assertFalse($Gson$Types.equals(pt1, pt3));
    assertFalse($Gson$Types.equals(pt1, ptDifferentRaw));
    assertFalse($Gson$Types.equals(pt1, String.class));

    GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gat3 = $Gson$Types.arrayOf(Integer.class);

    assertTrue($Gson$Types.equals(gat1, gat2));
    assertFalse($Gson$Types.equals(gat1, gat3));
    assertFalse($Gson$Types.equals(gat1, String[].class));

    WildcardType wt1 = $Gson$Types.subtypeOf(Number.class);
    WildcardType wt2 = $Gson$Types.subtypeOf(Number.class);
    WildcardType wt3 = $Gson$Types.subtypeOf(Integer.class);

    assertTrue($Gson$Types.equals(wt1, wt2));
    assertFalse($Gson$Types.equals(wt1, wt3));
    assertFalse($Gson$Types.equals(wt1, pt1));

    TypeVariable<?> tv1 = List.class.getTypeParameters()[0];
    TypeVariable<?> tv2 = List.class.getTypeParameters()[0];
    TypeVariable<?> tv3 = Set.class.getTypeParameters()[0];

    assertTrue($Gson$Types.equals(tv1, tv2));
    assertFalse($Gson$Types.equals(tv1, tv3));
    assertFalse($Gson$Types.equals(tv1, wt1));

    CustomTypeImpl custom1 = new CustomTypeImpl();
    CustomTypeImpl custom2 = new CustomTypeImpl();
    assertFalse($Gson$Types.equals(custom1, custom2));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1107)
  // =========================================================================

  /**
   * Targets the defect revealed in com.google.gson.functional.CollectionTest::testIssue1107.
   * When getSupertype() is called with a WildcardType context (e.g. `? extends List<SmallClass>`),
   * it must properly resolve the collection supertype to Collection<SmallClass>.
   * On defective versions, the wildcard is not unwrapped, causing type resolution to fail and
   * element type to revert to Object.class, producing LinkedTreeMap at runtime instead of SmallClass.
   */
  @Test(timeout = 4000)
  public void testIssue1107_WildcardCollectionElementTypeResolution() {
    ParameterizedType listOfSmallClass = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, SmallClass.class);
    WildcardType wildcardContext = $Gson$Types.subtypeOf(listOfSmallClass);

    Type elementType = $Gson$Types.getCollectionElementType(wildcardContext, List.class);
    assertEquals("Collection element type of `? extends List<SmallClass>` must resolve to SmallClass",
        SmallClass.class, elementType);
  }

  @Test(timeout = 4000)
  public void testIssue1107_WildcardSupertypeResolutionDirect() {
    ParameterizedType listOfSmallClass = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, SmallClass.class);
    WildcardType wildcardContext = $Gson$Types.subtypeOf(listOfSmallClass);

    Type supertype = $Gson$Types.getSupertype(wildcardContext, List.class, Collection.class);
    ParameterizedType expectedSupertype = $Gson$Types.newParameterizedTypeWithOwner(null, Collection.class, SmallClass.class);

    assertEquals("getSupertype with wildcard context must produce Collection<SmallClass>",
        expectedSupertype, supertype);
  }

  @Test(timeout = 4000)
  public void testIssue1107_SubclassOfParameterizedCollection() {
    class SmallClassList extends ArrayList<SmallClass> {
      private static final long serialVersionUID = 1L;
    }

    Type elementType = $Gson$Types.getCollectionElementType(SmallClassList.class, SmallClassList.class);
    assertEquals("Collection element type of custom ArrayList<SmallClass> subclass must resolve to SmallClass",
        SmallClass.class, elementType);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorThrowsUnsupportedOperationException() throws Exception {
    Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected InvocationTargetException wrapping UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof UnsupportedOperationException);
    }
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetRawTypeNullThrows() {
    $Gson$Types.getRawType(null);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetRawTypeUnsupportedCustomTypeThrows() {
    $Gson$Types.getRawType(new CustomTypeImpl());
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testParameterizedTypeDisallowsPrimitiveTypeArgument() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testParameterizedTypeNonStaticInnerClassRequiresOwner() {
    $Gson$Types.newParameterizedTypeWithOwner(null, NonStaticInnerParent.NonStaticInnerChild.class);
  }

  @Test(timeout = 4000)
  public void testParameterizedTypeStaticInnerClassAllowsNullOwner() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, StaticInnerParent.StaticInnerChild.class);
    assertNull(pt.getOwnerType());
    assertEquals(StaticInnerParent.StaticInnerChild.class, pt.getRawType());
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetSupertypeNonAssignableThrows() {
    $Gson$Types.getSupertype(String.class, String.class, List.class);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testCheckNotPrimitiveThrowsOnPrimitive() {
    $Gson$Types.checkNotPrimitive(int.class);
  }

  @Test(timeout = 4000)
  public void testCheckNotPrimitivePassesOnNonPrimitive() {
    $Gson$Types.checkNotPrimitive(Integer.class);
    $Gson$Types.checkNotPrimitive(String.class);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testParameterizedTypeImplEqualsAndHashCodeContract() {
    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    ParameterizedType ptDifferent = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);

    assertEquals(pt1, pt1);
    assertEquals(pt1, pt2);
    assertEquals(pt2, pt1);
    assertEquals(pt1.hashCode(), pt2.hashCode());

    assertNotEquals(pt1, ptDifferent);
    assertNotEquals(pt1, null);
    assertNotEquals(pt1, "not a type");

    assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", pt1.toString());
  }

  @Test(timeout = 4000)
  public void testGenericArrayTypeImplEqualsAndHashCodeContract() {
    GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
    GenericArrayType gatDifferent = $Gson$Types.arrayOf(Integer.class);

    assertEquals(gat1, gat1);
    assertEquals(gat1, gat2);
    assertEquals(gat1.hashCode(), gat2.hashCode());

    assertNotEquals(gat1, gatDifferent);
    assertNotEquals(gat1, null);

    assertEquals("java.lang.String[]", gat1.toString());
  }

  @Test(timeout = 4000)
  public void testWildcardTypeImplEqualsAndHashCodeContract() {
    WildcardType wt1 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType wt2 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType wtDifferent = $Gson$Types.subtypeOf(String.class);

    assertEquals(wt1, wt1);
    assertEquals(wt1, wt2);
    assertEquals(wt1.hashCode(), wt2.hashCode());

    assertNotEquals(wt1, wtDifferent);
    assertNotEquals(wt1, null);

    assertEquals("? extends java.lang.CharSequence", wt1.toString());
  }

  @Test(timeout = 4000)
  public void testSerializationRoundTrip() throws Exception {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    GenericArrayType gat = $Gson$Types.arrayOf(pt);
    WildcardType wt = $Gson$Types.subtypeOf(Number.class);

    verifySerializationRoundTrip(pt);
    verifySerializationRoundTrip(gat);
    verifySerializationRoundTrip(wt);
  }

  private void verifySerializationRoundTrip(Object original) throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
    }
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    try (ObjectInputStream ois = new ObjectInputStream(bais)) {
      Object deserialized = ois.readObject();
      assertEquals("Deserialized object must equal original", original, deserialized);
      assertEquals("Deserialized hashCode must equal original", original.hashCode(), deserialized.hashCode());
    }
  }
}