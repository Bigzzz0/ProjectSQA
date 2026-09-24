package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import static org.junit.Assert.*;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * ArrowType methods covered:
 *  - Constructors (null parameters/return type normalization)
 *  - isSubtype:
 *      * non-ArrowType rejection
 *      * return type covariance
 *      * parameter contravariance
 *      * optional/var-args arity rules
 *      * top function special case for var-args unknown/no-type/untyped params
 *      * KNOWN DEFECT: an untyped var-args parameter was being rejected before
 *        the top-function special case could accept it. This was tracked by
 *        Closure TypeCheckTest.testIssue791 and RecordTypeTest.testSubtypeWithUnknowns2.
 *  - hasEqualParameters / checkArrowEquivalenceHelper
 *  - hashCode (including returnTypeInferred)
 *  - resolveInternal
 *  - hasUnknownParamsOrReturn
 *  - hasAnyTemplateInternal
 *  - toStringHelper
 *  - getPossibleToBooleanOutcomes
 *  - Unsupported operations
 */
public class ArrowTypeDeepseekTest {

  private final ErrorReporter reporter = new ErrorReporter() {
    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
    }
  };

  private final JSTypeRegistry registry = new JSTypeRegistry(reporter);

  private JSType number() {
    return registry.getNativeType(JSTypeNative.NUMBER_TYPE);
  }

  private JSType string() {
    return registry.getNativeType(JSTypeNative.STRING_TYPE);
  }

  private JSType unknown() {
    return registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
  }

  private JSType noType() {
    return registry.getNativeType(JSTypeNative.NO_TYPE);
  }

  private JSType allType() {
    return registry.getNativeType(JSTypeNative.ALL_TYPE);
  }

  private JSType objectType() {
    return registry.getNativeType(JSTypeNative.OBJECT_TYPE);
  }

  private Node paramList(Node... params) {
    Node list = new Node(Token.PARAM_LIST);
    for (Node p : params) {
      list.addChildToBack(p);
    }
    return list;
  }

  private Node param(String name, JSType type) {
    Node n = new Node(Token.NAME, name);
    if (type != null) {
      n.setJSType(type);
    }
    return n;
  }

  private Node param(String name, JSType type, boolean optional, boolean varArgs) {
    Node n = param(name, type);
    n.setOptionalArg(optional);
    n.setVarArgs(varArgs);
    return n;
  }

  private ArrowType createArrow(Node params, JSType returnType) {
    return new ArrowType(registry, params, returnType, false);
  }

  private ArrowType createArrow(Node params, JSType returnType, boolean inferred) {
    return new ArrowType(registry, params, returnType, inferred);
  }

  private StaticScope<JSType> emptyScope() {
    return new StaticScope<JSType>() {
      @Override
      public StaticSlot<JSType> getSlot(String name) {
        return null;
      }

      @Override
      public StaticSlot<JSType> getOwnSlot(String name) {
        return null;
      }

      @Override
      public JSType getTypeOfThis() {
        return null;
      }

      @Override
      public StaticScope<JSType> getParentScope() {
        return null;
      }
    };
  }

  @Test(timeout = 4000)
  public void testConstructorDefaults() {
    ArrowType arrow = new ArrowType(registry, null, null);
    assertNotNull(arrow.parameters);
    assertNotNull(arrow.returnType);
    assertTrue(arrow.returnType.isUnknownType());
    assertFalse(arrow.returnTypeInferred);
    assertTrue(arrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRejectsNonArrowType() {
    ArrowType arrow = createArrow(paramList(), number());
    assertFalse(arrow.isSubtype(objectType()));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRejectsNonCovariantReturnType() {
    ArrowType numberFn = createArrow(paramList(), number());
    ArrowType stringFn = createArrow(paramList(), string());
    assertFalse(numberFn.isSubtype(stringFn));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAcceptsContravariantParameters() {
    ArrowType sub = createArrow(paramList(param("a", allType())), number());
    ArrowType sup = createArrow(paramList(param("a", number())), number());
    assertTrue(sub.isSubtype(sup));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRejectsBadContravariantParameters() {
    ArrowType sub = createArrow(paramList(param("a", string())), number());
    ArrowType sup = createArrow(paramList(param("a", number())), number());
    assertFalse(sub.isSubtype(sup));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsNullParamTypeInThis() {
    ArrowType untypedThis = createArrow(paramList(param("x", null)), number());
    ArrowType typedThat = createArrow(paramList(param("x", number())), number());
    assertTrue(untypedThis.isSubtype(typedThat));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsNullParamTypeInBoth() {
    ArrowType a = createArrow(paramList(param("x", null)), number());
    ArrowType b = createArrow(paramList(param("x", null)), number());
    assertTrue(a.isSubtype(b));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRequiredIsNotSubtypeOfOptional() {
    ArrowType required = createArrow(paramList(param("a", number())), number());
    ArrowType optional = createArrow(
        paramList(param("a", number(), true, false)), number());
    assertFalse(required.isSubtype(optional));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeOptionalIsSubtypeOfRequired() {
    ArrowType required = createArrow(paramList(param("a", number())), number());
    ArrowType optional = createArrow(
        paramList(param("a", number(), true, false)), number());
    assertTrue(optional.isSubtype(required));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsRequiredSubtypeOfUnknownVarArgs() {
    ArrowType required = createArrow(paramList(param("a", number())), number());
    ArrowType topFn = createArrow(
        paramList(param("rest", unknown(), false, true)), number());
    assertTrue(required.isSubtype(topFn));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsRequiredSubtypeOfNoTypeVarArgs() {
    ArrowType required = createArrow(paramList(param("a", number())), number());
    ArrowType topFn = createArrow(
        paramList(param("rest", noType(), false, true)), number());
    assertTrue(required.isSubtype(topFn));
  }

  @Test(timeout = 4000)
  public void testKnownDefectUntypedVarArgsParamIsTopFunction() {
    // Bug: the untyped var-args parameter (thatParamType == null) was
    // rejected by the initial type check, preventing the top-function
    // special case from ever applying. The correct behavior is that
    // a required typed parameter can be a subtype of function(...?).
    ArrowType sub = createArrow(paramList(param("x", number())), number());
    ArrowType sup = createArrow(
        paramList(param("rest", null, false, true)), number());
    assertTrue(sub.isSubtype(sup));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsExtraParametersInThat() {
    ArrowType fewer = createArrow(paramList(param("a", number())), number());
    ArrowType more = createArrow(
        paramList(param("a", number()), param("b", number())), number());
    assertTrue(fewer.isSubtype(more));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRejectsExtraRequiredParametersInThis() {
    ArrowType more = createArrow(
        paramList(param("a", number()), param("b", number())), number());
    ArrowType fewer = createArrow(paramList(param("a", number())), number());
    assertFalse(more.isSubtype(fewer));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsExtraOptionalParametersInThis() {
    ArrowType more = createArrow(
        paramList(param("a", number()), param("b", number(), true, false)),
        number());
    ArrowType fewer = createArrow(paramList(param("a", number())), number());
    assertTrue(more.isSubtype(fewer));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsVarArgsInThisWithNoThatParams() {
    ArrowType varArgs = createArrow(
        paramList(param("a", number(), false, true)), number());
    ArrowType empty = createArrow(paramList(), number());
    assertTrue(varArgs.isSubtype(empty));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAllowsOptionalInThisWithNoThatParams() {
    ArrowType optional = createArrow(
        paramList(param("a", number(), true, false)), number());
    ArrowType empty = createArrow(paramList(), number());
    assertTrue(optional.isSubtype(empty));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeRejectsRequiredInThisWithNoThatParams() {
    ArrowType required = createArrow(paramList(param("a", number())), number());
    ArrowType empty = createArrow(paramList(), number());
    assertFalse(required.isSubtype(empty));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeBothVarArgsEnds() {
    ArrowType a = createArrow(
        paramList(param("a", number(), false, true)), number());
    ArrowType b = createArrow(
        paramList(param("b", number(), false, true)), number());
    assertTrue(a.isSubtype(b));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersTrue() {
    ArrowType a = createArrow(paramList(param("a", number())), number());
    ArrowType b = createArrow(paramList(param("b", number())), number());
    assertTrue(a.hasEqualParameters(b, false));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersFalseDifferentTypes() {
    ArrowType a = createArrow(paramList(param("a", number())), number());
    ArrowType b = createArrow(paramList(param("a", string())), number());
    assertFalse(a.hasEqualParameters(b, false));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersFalseDifferentLengths() {
    ArrowType one = createArrow(paramList(param("a", number())), number());
    ArrowType two = createArrow(
        paramList(param("a", number()), param("b", number())), number());
    assertFalse(one.hasEqualParameters(two, false));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersTrueForEmptyLists() {
    ArrowType a = createArrow(paramList(), number());
    ArrowType b = createArrow(paramList(), number());
    assertTrue(a.hasEqualParameters(b, false));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersFalseWhenThisNullOtherTyped() {
    ArrowType untyped = createArrow(paramList(param("x", null)), number());
    ArrowType typed = createArrow(paramList(param("x", number())), number());
    assertFalse(untyped.hasEqualParameters(typed, false));
  }

  @Test(timeout = 4000)
  public void testCheckArrowEquivalenceHelperTrue() {
    ArrowType a = createArrow(paramList(param("a", number())), number());
    ArrowType b = createArrow(paramList(param("b", number())), number());
    assertTrue(a.checkArrowEquivalenceHelper(b, false));
  }

  @Test(timeout = 4000)
  public void testCheckArrowEquivalenceHelperFalseReturnType() {
    ArrowType a = createArrow(paramList(param("a", number())), number());
    ArrowType b = createArrow(paramList(param("a", number())), string());
    assertFalse(a.checkArrowEquivalenceHelper(b, false));
  }

  @Test(timeout = 4000)
  public void testCheckArrowEquivalenceHelperFalseParameters() {
    ArrowType a = createArrow(paramList(param("a", number())), number());
    ArrowType b = createArrow(paramList(param("a", string())), number());
    assertFalse(a.checkArrowEquivalenceHelper(b, false));
  }

  @Test(timeout = 4000)
  public void testCheckArrowEquivalenceHelperFalseInferredReturnFlag() {
    ArrowType explicit = createArrow(paramList(), number(), false);
    ArrowType inferred = createArrow(paramList(), number(), true);
    assertFalse(explicit.checkArrowEquivalenceHelper(inferred, false));
  }

  @Test(timeout = 4000)
  public void testHashCodeIncludesInferredFlag() {
    ArrowType explicit = createArrow(paramList(), number(), false);
    ArrowType inferred = createArrow(paramList(), number(), true);
    assertNotEquals(explicit.hashCode(), inferred.hashCode());
  }

  @Test(timeout = 4000)
  public void testHashCodeWithParameters() {
    ArrowType arrow = createArrow(paramList(param("x", number())), number());
    assertNotNull(arrow.hashCode());
  }

  @Test(timeout = 4000)
  public void testHasUnknownParamsOrReturnFalseForFullyTyped() {
    ArrowType arrow = createArrow(paramList(param("x", number())), number());
    assertFalse(arrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testHasUnknownParamsOrReturnTrueForUnknownReturn() {
    ArrowType arrow = createArrow(paramList(param("x", number())), unknown());
    assertTrue(arrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testHasUnknownParamsOrReturnTrueForUnknownParam() {
    ArrowType arrow = createArrow(paramList(param("x", unknown())), number());
    assertTrue(arrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testHasUnknownParamsOrReturnTrueForNullParam() {
    ArrowType arrow = createArrow(paramList(param("x", null)), number());
    assertTrue(arrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testHasAnyTemplateInternalFalseForNativeTypes() {
    ArrowType arrow = createArrow(paramList(param("x", number())), number());
    assertFalse(arrow.hasAnyTemplateInternal());
  }

  @Test(timeout = 4000)
  public void testResolveInternalResolvesParamsAndReturn() {
    ArrowType arrow = createArrow(paramList(param("x", number())), number());
    assertSame(arrow, arrow.resolveInternal(reporter, emptyScope()));
    assertEquals(number(), arrow.returnType);
  }

  @Test(timeout = 4000)
  public void testToStringHelper() {
    ArrowType arrow = createArrow(paramList(), number());
    assertEquals("[ArrowType]", arrow.toStringHelper(false));
  }

  @Test(timeout = 4000)
  public void testGetPossibleToBooleanOutcomesIsAlwaysTrue() {
    ArrowType arrow = createArrow(paramList(), number());
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetLeastSupertypeUnsupported() {
    ArrowType arrow = createArrow(paramList(), number());
    arrow.getLeastSupertype(arrow);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetGreatestSubtypeUnsupported() {
    ArrowType arrow = createArrow(paramList(), number());
    arrow.getGreatestSubtype(arrow);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testTestForEqualityUnsupported() {
    ArrowType arrow = createArrow(paramList(), number());
    arrow.testForEquality(arrow);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testVisitUnsupported() {
    ArrowType arrow = createArrow(paramList(), number());
    arrow.visit(null);
  }
}