package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.jstype.ArrowType
 * Defect Reference: Defects4J Closure-164 / FunctionType subtyping arity mismatch.
 *
 * Core Decision Points & Coverage Targets:
 * 1. Constructor:
 *    - parameters == null branch (creates default varargs unknown) vs non-null parameters.
 *    - returnType == null branch (defaults to UNKNOWN_TYPE) vs non-null returnType.
 *    - returnTypeInferred flag (true / false).
 * 2. isSubtype(JSType other):
 *    - other not an ArrowType -> false.
 *    - Covariant return type check: this.returnType.isSubtype(that.returnType) -> false.
 *    - Contravariant parameter check: thatParamType.isSubtype(thisParamType) -> false when not subtype.
 *    - Parameter type null handling: thisParamType != null && thatParamType == null -> false.
 *    - Varargs handling: thisIsVarArgs, thatIsVarArgs, both varArgs (loop termination).
 *    - [DEFECT TARGET] Arity mismatch: When `this` has remaining required parameters and `that` has
 *      exhausted its parameters (`thatParam == null`), `this` CANNOT be a subtype of `that`!
 * 3. hasEqualParameters(ArrowType that):
 *    - Parameter list length mismatch (thisParam != otherParam at end).
 *    - Types equivalent vs not equivalent.
 *    - One param type null vs other non-null.
 * 4. isEquivalentTo(JSType object):
 *    - Not ArrowType -> false.
 *    - Return type not equivalent -> false.
 *    - Return type equivalent and parameter list equal -> true.
 * 5. hashCode():
 *    - Distinct values based on returnType, returnTypeInferred, and parameter types.
 * 6. Unsupported Operations:
 *    - getLeastSupertype, getGreatestSubtype, testForEquality, visit -> UnsupportedOperationException.
 * 7. Boolean outcomes:
 *    - getPossibleToBooleanOutcomes() -> BooleanLiteralSet.TRUE.
 * 8. Resolution and Inspection:
 *    - resolveInternal(ErrorReporter, StaticScope): recursively resolves params and returnType.
 *    - hasUnknownParamsOrReturn(): branches for unknown return, unknown param, null param type, all known.
 *    - toStringHelper(boolean): invokes super.toString().
 */
public class ArrowTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;
  private JSType unknownType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorWithExplicitParametersAndReturn() {
    Node params = registry.createParameters(numberType, stringType);
    ArrowType arrow = new ArrowType(registry, params, booleanType, true);

    assertSame(params, arrow.parameters);
    assertSame(booleanType, arrow.returnType);
    assertTrue(arrow.returnTypeInferred);
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  @Test(timeout = 4000)
  public void testThreeArgConstructorDefaultsInferredToFalse() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, stringType);

    assertFalse(arrow.returnTypeInferred);
    assertSame(stringType, arrow.returnType);
    assertSame(params, arrow.parameters);
  }

  @Test(timeout = 4000)
  public void testSubtypeIdenticalSignatures() {
    Node params1 = registry.createParameters(numberType, stringType);
    Node params2 = registry.createParameters(numberType, stringType);
    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);

    assertTrue(arrow1.isSubtype(arrow2));
    assertTrue(arrow2.isSubtype(arrow1));
  }

  @Test(timeout = 4000)
  public void testSubtypeContravariantParameters() {
    // In arrow subtyping: (A -> R) <: (B -> R) requires that B <: A (contravariant)
    // ALL_TYPE is a supertype of NUMBER_TYPE: Number <: All
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);

    // arrowGeneral accepts All
    Node paramsGeneral = registry.createParameters(allType);
    ArrowType arrowGeneral = new ArrowType(registry, paramsGeneral, booleanType);

    // arrowSpecific accepts Number
    Node paramsSpecific = registry.createParameters(numberType);
    ArrowType arrowSpecific = new ArrowType(registry, paramsSpecific, booleanType);

    // thatParamType (All) <: thisParamType (Number) is FALSE, so arrowSpecific is not subtype of arrowGeneral
    assertFalse(arrowSpecific.isSubtype(arrowGeneral));
    // thatParamType (Number) <: thisParamType (All) is TRUE, so arrowGeneral is a subtype of arrowSpecific
    assertTrue(arrowGeneral.isSubtype(arrowSpecific));
  }

  @Test(timeout = 4000)
  public void testSubtypeCovariantReturn() {
    // ALL_TYPE is supertype of NUMBER_TYPE
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
    Node params = registry.createParameters(numberType);

    ArrowType returnsNumber = new ArrowType(registry, params, numberType);
    ArrowType returnsAll = new ArrowType(registry, params, allType);

    // Number <: All => returnsNumber <: returnsAll
    assertTrue(returnsNumber.isSubtype(returnsAll));
    // All !<: Number => returnsAll !<: returnsNumber
    assertFalse(returnsAll.isSubtype(returnsNumber));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorWithNullParametersAndNullReturnType() {
    ArrowType arrow = new ArrowType(registry, null, null);

    assertNotNull(arrow.parameters);
    assertTrue(arrow.parameters.getFirstChild().isVarArgs());
    assertEquals(unknownType, arrow.parameters.getFirstChild().getJSType());
    assertEquals(unknownType, arrow.returnType);
    assertFalse(arrow.returnTypeInferred);
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersDifferentLengths() {
    Node oneParam = registry.createParameters(numberType);
    Node twoParams = registry.createParameters(numberType, stringType);

    ArrowType arrow1 = new ArrowType(registry, oneParam, booleanType);
    ArrowType arrow2 = new ArrowType(registry, twoParams, booleanType);

    assertFalse(arrow1.hasEqualParameters(arrow2));
    assertFalse(arrow2.hasEqualParameters(arrow1));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersWithNullParamTypes() {
    Node nodeA = new Node(com.google.javascript.rhino.Token.LP);
    Node paramA = Node.newString("a");
    paramA.setJSType(null);
    nodeA.addChildToBack(paramA);

    Node nodeB = registry.createParameters(numberType);

    ArrowType arrowA = new ArrowType(registry, nodeA, booleanType);
    ArrowType arrowB = new ArrowType(registry, nodeB, booleanType);

    // this has null paramType, other has non-null -> false
    assertFalse(arrowA.hasEqualParameters(arrowB));
    // this has non-null paramType, other has null -> false
    assertFalse(arrowB.hasEqualParameters(arrowA));

    // Both have null param types
    Node nodeA2 = new Node(com.google.javascript.rhino.Token.LP);
    Node paramA2 = Node.newString("a2");
    paramA2.setJSType(null);
    nodeA2.addChildToBack(paramA2);
    ArrowType arrowA2 = new ArrowType(registry, nodeA2, booleanType);

    assertTrue(arrowA.hasEqualParameters(arrowA2));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithBothVarArgsTerminating() {
    Node varArgs1 = registry.createParametersWithVarArgs(numberType);
    Node varArgs2 = registry.createParametersWithVarArgs(numberType);

    ArrowType arrow1 = new ArrowType(registry, varArgs1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, varArgs2, booleanType);

    assertTrue(arrow1.isSubtype(arrow2));
  }

  @Test(timeout = 4000)
  public void testSubtypeParamTypeNullCheck() {
    Node nodeA = new Node(com.google.javascript.rhino.Token.LP);
    Node paramA = Node.newString("a");
    paramA.setJSType(numberType);
    nodeA.addChildToBack(paramA);

    Node nodeB = new Node(com.google.javascript.rhino.Token.LP);
    Node paramB = Node.newString("b");
    paramB.setJSType(null);
    nodeB.addChildToBack(paramB);

    ArrowType arrowA = new ArrowType(registry, nodeA, booleanType);
    ArrowType arrowB = new ArrowType(registry, nodeB, booleanType);

    // thisParamType is numberType, thatParamType is null -> !thatParamType.isSubtype(thisParamType) -> false
    assertFalse(arrowA.isSubtype(arrowB));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-164 Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where ArrowType.isSubtype allows a function requiring more parameters
   * to be considered a subtype of a function expecting fewer parameters.
   * If f requires (number, number), and g only requires (number),
   * f CANNOT be used where g is expected because callers of g will only provide 1 argument!
   * Therefore, f.isSubtype(g) MUST be false.
   */
  @Test(timeout = 4000)
  public void testSubtypeExtraRequiredParametersFails() {
    Node twoRequiredParams = registry.createParameters(numberType, numberType);
    Node oneRequiredParam = registry.createParameters(numberType);

    ArrowType takesTwo = new ArrowType(registry, twoRequiredParams, booleanType);
    ArrowType takesOne = new ArrowType(registry, oneRequiredParam, booleanType);

    // takesTwo requires 2 arguments. takesOne only takes 1.
    // takesTwo cannot be a subtype of takesOne!
    assertFalse("A function requiring 2 parameters cannot be a subtype of a function taking only 1",
        takesTwo.isSubtype(takesOne));
  }

  @Test(timeout = 4000)
  public void testSubtypeOptionalParametersAllowed() {
    Node oneRequiredParam = registry.createParameters(numberType);
    Node oneRequiredOneOptional = registry.createParameters(numberType);
    Node optParam = Node.newString("opt");
    optParam.setJSType(numberType);
    optParam.putBooleanProp(Node.OPT_ARG_NAME, true);
    oneRequiredOneOptional.addChildToBack(optParam);

    ArrowType func1 = new ArrowType(registry, oneRequiredParam, booleanType);
    ArrowType func2 = new ArrowType(registry, oneRequiredOneOptional, booleanType);

    // func1 (1 param) can be passed where func2 (1 req + 1 opt) is expected
    assertTrue(func1.isSubtype(func2));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetLeastSupertypeThrowsException() {
    ArrowType arrow = new ArrowType(registry, null, booleanType);
    arrow.getLeastSupertype(booleanType);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetGreatestSubtypeThrowsException() {
    ArrowType arrow = new ArrowType(registry, null, booleanType);
    arrow.getGreatestSubtype(booleanType);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testTestForEqualityThrowsException() {
    ArrowType arrow = new ArrowType(registry, null, booleanType);
    arrow.testForEquality(booleanType);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testVisitThrowsException() {
    ArrowType arrow = new ArrowType(registry, null, booleanType);
    arrow.visit(new Visitor<Object>() {
      @Override public Object caseNoType() { return null; }
      @Override public Object caseUnknownType() { return null; }
      @Override public Object caseNullType() { return null; }
      @Override public Object caseNamedType(NamedType type) { return null; }
      @Override public Object caseBooleanType() { return null; }
      @Override public Object caseNumberType() { return null; }
      @Override public Object caseStringType() { return null; }
      @Override public Object caseObjectType(ObjectType type) { return null; }
      @Override public Object caseUnionType(UnionType type) { return null; }
      @Override public Object caseRecordType(RecordType type) { return null; }
      @Override public Object caseParameterizedType(ParameterizedType type) { return null; }
      @Override public Object caseTemplateType(TemplateType type) { return null; }
      @Override public Object caseAllType() { return null; }
      @Override public Object caseVoidType() { return null; }
      @Override public Object caseNoObjectType() { return null; }
      @Override public Object caseFunctionType(FunctionType type) { return null; }
      @Override public Object caseEnumElementType(EnumElementType type) { return null; }
    });
  }

  @Test(timeout = 4000)
  public void testSubtypeNonArrowTypeReturnsFalse() {
    ArrowType arrow = new ArrowType(registry, null, booleanType);
    assertFalse(arrow.isSubtype(numberType));
    assertFalse(arrow.isSubtype(null));
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Equivalence, & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsEquivalentToContract() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);
    Node params3 = registry.createParameters(stringType);

    ArrowType arrow1 = new ArrowType(registry, params1, booleanType);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType);
    ArrowType arrow3 = new ArrowType(registry, params3, booleanType);
    ArrowType arrowDiffReturn = new ArrowType(registry, params1, stringType);

    assertTrue(arrow1.isEquivalentTo(arrow2));
    assertFalse(arrow1.isEquivalentTo(arrow3));
    assertFalse(arrow1.isEquivalentTo(arrowDiffReturn));
    assertFalse(arrow1.isEquivalentTo(numberType));
    assertFalse(arrow1.isEquivalentTo(null));
  }

  @Test(timeout = 4000)
  public void testHashCodeContract() {
    Node params1 = registry.createParameters(numberType);
    Node params2 = registry.createParameters(numberType);

    ArrowType arrow1 = new ArrowType(registry, params1, booleanType, false);
    ArrowType arrow2 = new ArrowType(registry, params2, booleanType, false);
    ArrowType arrowInferred = new ArrowType(registry, params1, booleanType, true);

    assertEquals(arrow1.hashCode(), arrow2.hashCode());
    assertEquals(arrow1.hashCode() + 1, arrowInferred.hashCode());

    // Null param inside parameters list should not throw NPE
    Node nodeNullParam = new Node(com.google.javascript.rhino.Token.LP);
    Node p = Node.newString("x");
    p.setJSType(null);
    nodeNullParam.addChildToBack(p);
    ArrowType arrowNullParam = new ArrowType(registry, nodeNullParam, booleanType);
    assertTrue(arrowNullParam.hashCode() != 0);
  }

  @Test(timeout = 4000)
  public void testHasUnknownParamsOrReturn() {
    ArrowType allKnown = new ArrowType(registry, registry.createParameters(numberType), booleanType);
    assertFalse(allKnown.hasUnknownParamsOrReturn());

    ArrowType unknownReturn = new ArrowType(registry, registry.createParameters(numberType), unknownType);
    assertTrue(unknownReturn.hasUnknownParamsOrReturn());

    ArrowType unknownParam = new ArrowType(registry, registry.createParameters(unknownType), booleanType);
    assertTrue(unknownParam.hasUnknownParamsOrReturn());

    Node nodeNullParam = new Node(com.google.javascript.rhino.Token.LP);
    Node p = Node.newString("x");
    p.setJSType(null);
    nodeNullParam.addChildToBack(p);
    ArrowType nullParamType = new ArrowType(registry, nodeNullParam, booleanType);
    assertTrue(nullParamType.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testResolveInternal() {
    Node params = registry.createParameters(numberType);
    ArrowType arrow = new ArrowType(registry, params, stringType);
    SimpleErrorReporter reporter = new SimpleErrorReporter();

    JSType resolved = arrow.resolveInternal(reporter, null);
    assertSame(arrow, resolved);
    assertEquals(stringType, arrow.returnType);
    assertEquals(numberType, arrow.parameters.getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testToStringHelper() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(numberType), booleanType);
    String str = arrow.toStringHelper(false);
    assertNotNull(str);
    assertFalse(str.isEmpty());
  }
}