/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.rhino.jstype.ArrowType
 * Defect Reference: Defects4J Closure Issue 791 / RecordTypeTest::testSubtypeWithUnknowns2
 *
 * Targeted Decision Branches & Boundaries:
 * 1. Constructor Branches:
 *    - parameters == null ? var_args(UNKNOWN_TYPE) : parameters
 *    - returnType == null ? UNKNOWN_TYPE : returnType
 *    - returnTypeInferred: true vs false
 *
 * 2. isSubtype(JSType other):
 *    - !(other instanceof ArrowType) -> false
 *    - !this.returnType.isSubtype(that.returnType) -> false (covariance)
 *    - Contravariant parameter check: thatParamType.isSubtype(thisParamType)
 *      - thisParamType != null && thatParamType == null -> false
 *      - thisParamType != null && !thatParamType.isSubtype(thisParamType) -> false
 *      - thisParamType == null -> allowed
 *    - Arity and Optional/VarArgs checking:
 *      - !thisIsOptional && thatIsOptional:
 *        - isTopFunction (thatIsVarArgs && (thatParamType == null || unknown || noType)) -> true
 *        - !isTopFunction -> false
 *      - Advancing pointers:
 *        - !thisIsVarArgs advances thisParam
 *        - !thatIsVarArgs advances thatParam
 *        - both varArgs -> terminate loop (thisParam=null, thatParam=null)
 *    - Post-loop arity check:
 *      - thisParam != null && !optional && !varArgs && thatParam == null -> false
 *      - thisParam != null && optional/varArgs && thatParam == null -> true
 *      - thisParam == null && thatParam != null -> true (JS arity leniency)
 *
 * 3. hasEqualParameters(ArrowType that, boolean tolerateUnknowns):
 *    - thisParamType != null && otherParamType != null -> checkEquivalenceHelper
 *    - thisParamType != null && otherParamType == null -> tolerate
 *    - thisParamType == null && otherParamType != null -> false
 *    - thisParamType == null && otherParamType == null -> true
 *    - arity mismatch -> false
 *
 * 4. checkArrowEquivalenceHelper(ArrowType that, boolean tolerateUnknowns):
 *    - returnType checkEquivalenceHelper differs -> false
 *    - returnType matches, parameters match -> true
 *    - returnType matches, parameters differ -> false
 *
 * 5. hashCode():
 *    - returnType != null vs null
 *    - returnTypeInferred: +1 if true
 *    - parameters traversal: paramType != null vs null
 *
 * 6. Unsupported Operations:
 *    - getLeastSupertype -> UnsupportedOperationException
 *    - getGreatestSubtype -> UnsupportedOperationException
 *    - testForEquality -> UnsupportedOperationException
 *    - visit -> UnsupportedOperationException
 *
 * 7. Template and Resolver Integrity:
 *    - hasAnyTemplateInternal: returnType vs parameters with template
 *    - hasUnknownParamsOrReturn: detection of UNKNOWN_TYPE / null
 *    - resolveInternal: safety on parameters and returnType
 * --------------------------------------------------------------------------------------------------
 */

package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

public class ArrowTypeGeminiTest {

  private JSTypeRegistry registry;
  private JSType NUMBER;
  private JSType STRING;
  private JSType UNKNOWN;
  private JSType ALL;
  private JSType NO;
  private JSType VOID;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    NUMBER = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    STRING = registry.getNativeType(JSTypeNative.STRING_TYPE);
    UNKNOWN = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ALL = registry.getNativeType(JSTypeNative.ALL_TYPE);
    NO = registry.getNativeType(JSTypeNative.NO_TYPE);
    VOID = registry.getNativeType(JSTypeNative.VOID_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicCreationAndStateInspection() {
    Node params = registry.createParameters(NUMBER);
    ArrowType arrow1 = new ArrowType(registry, params, STRING);

    assertSame(params, arrow1.parameters);
    assertSame(STRING, arrow1.returnType);
    assertFalse(arrow1.returnTypeInferred);

    ArrowType arrow2 = new ArrowType(registry, params, STRING, true);
    assertTrue(arrow2.returnTypeInferred);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeCovariantReturn() {
    Node emptyParams = registry.createParameters();
    ArrowType retNumber = new ArrowType(registry, emptyParams, NUMBER);
    ArrowType retAll = new ArrowType(registry, emptyParams, ALL);
    ArrowType retString = new ArrowType(registry, emptyParams, STRING);

    // NUMBER <: ALL (covariant return type)
    assertTrue(retNumber.isSubtype(retAll));
    // ALL !<: NUMBER
    assertFalse(retAll.isSubtype(retNumber));
    // NUMBER !<: STRING
    assertFalse(retNumber.isSubtype(retString));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeContravariantParameters() {
    // Subtyping arrow types requires parameter contravariance: thatParam <: thisParam
    Node paramAll = registry.createParameters(ALL);
    Node paramNumber = registry.createParameters(NUMBER);

    ArrowType takesAll = new ArrowType(registry, paramAll, VOID);
    ArrowType takesNumber = new ArrowType(registry, paramNumber, VOID);

    // takesAll <: takesNumber because NUMBER <: ALL
    assertTrue(takesAll.isSubtype(takesNumber));
    // takesNumber !<: takesAll because ALL !<: NUMBER
    assertFalse(takesNumber.isSubtype(takesAll));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeArityLeniency() {
    // In JS type system: function g() can be used where function f(a) is expected.
    // So g <: f, but f !<: g
    Node param0 = registry.createParameters();
    Node param1 = registry.createParameters(NUMBER);

    ArrowType takes0 = new ArrowType(registry, param0, VOID);
    ArrowType takes1 = new ArrowType(registry, param1, VOID);

    assertTrue(takes0.isSubtype(takes1));
    assertFalse(takes1.isSubtype(takes0));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeOptionalParameters() {
    Node param1Req = registry.createParameters(NUMBER);

    Node param1Req1Opt = registry.createParameters(NUMBER);
    Node opt = registry.createOptionalParameters(STRING).removeFirstChild();
    param1Req1Opt.addChildToBack(opt);

    ArrowType takesReqAndOpt = new ArrowType(registry, param1Req1Opt, VOID);
    ArrowType takesOnlyReq = new ArrowType(registry, param1Req, VOID);

    // A function with an optional 2nd arg can be used where only 1 arg is expected
    assertTrue(takesReqAndOpt.isSubtype(takesOnlyReq));

    // However, if "that" has an optional parameter where "this" has a required parameter,
    // "that" cannot be a supertype unless it's top function
    Node param1Opt = registry.createOptionalParameters(NUMBER);
    ArrowType takesOpt = new ArrowType(registry, param1Opt, VOID);
    assertFalse(takesOnlyReq.isSubtype(takesOpt));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeVarArgsAdvancement() {
    // this has var_args, that has regular params
    Node thisVarArgs = registry.createParametersWithVarArgs(NUMBER);
    Node thatTwoParams = registry.createParameters(NUMBER, NUMBER);

    ArrowType arrowVarArgs = new ArrowType(registry, thisVarArgs, VOID);
    ArrowType arrowTwoParams = new ArrowType(registry, thatTwoParams, VOID);

    // (...number) -> void can be used where (number, number) -> void is expected
    assertTrue(arrowVarArgs.isSubtype(arrowTwoParams));

    // Both having var_args
    Node thatVarArgs = registry.createParametersWithVarArgs(NUMBER);
    ArrowType arrowVarArgs2 = new ArrowType(registry, thatVarArgs, VOID);
    assertTrue(arrowVarArgs.isSubtype(arrowVarArgs2));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorNullFallbacks() {
    ArrowType fallbackArrow = new ArrowType(registry, null, null);

    assertNotNull(fallbackArrow.parameters);
    assertTrue(fallbackArrow.parameters.getFirstChild().isVarArgs());
    assertSame(UNKNOWN, fallbackArrow.parameters.getFirstChild().getJSType());
    assertSame(UNKNOWN, fallbackArrow.returnType);
    assertFalse(fallbackArrow.returnTypeInferred);

    ArrowType fallbackWithInferred = new ArrowType(registry, null, null, true);
    assertTrue(fallbackWithInferred.returnTypeInferred);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeNonArrowTypeReturnsFalse() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);
    assertFalse(arrow.isSubtype(NUMBER));
    assertFalse(arrow.isSubtype(ALL));
    assertFalse(arrow.isSubtype(null));
  }

  @Test(timeout = 4000)
  public void testHasEqualParametersBoundaryChecks() {
    Node p1 = registry.createParameters(NUMBER);
    Node p2 = registry.createParameters(NUMBER, STRING);
    ArrowType a1 = new ArrowType(registry, p1, VOID);
    ArrowType a2 = new ArrowType(registry, p2, VOID);

    // Length mismatch
    assertFalse(a1.hasEqualParameters(a2, false));
    assertFalse(a2.hasEqualParameters(a1, false));

    // Both empty
    ArrowType empty1 = new ArrowType(registry, registry.createParameters(), VOID);
    ArrowType empty2 = new ArrowType(registry, registry.createParameters(), VOID);
    assertTrue(empty1.hasEqualParameters(empty2, false));

    // One param list with null JSType on child node
    Node pNull1 = registry.createParameters(NUMBER);
    pNull1.getFirstChild().setJSType(null);
    ArrowType aNull1 = new ArrowType(registry, pNull1, VOID);

    // thisParamType == null && otherParamType != null
    assertFalse(aNull1.hasEqualParameters(a1, false));
    // thisParamType != null && otherParamType == null
    // Under ArrowType.hasEqualParameters, if thisParamType!=null and otherParamType==null, returns true (tolerated)
    assertTrue(a1.hasEqualParameters(aNull1, false));

    // Both null param types
    Node pNull2 = registry.createParameters(NUMBER);
    pNull2.getFirstChild().setJSType(null);
    ArrowType aNull2 = new ArrowType(registry, pNull2, VOID);
    assertTrue(aNull1.hasEqualParameters(aNull2, false));
  }

  @Test(timeout = 4000)
  public void testCheckArrowEquivalenceHelperBranches() {
    ArrowType a1 = new ArrowType(registry, registry.createParameters(NUMBER), NUMBER);
    ArrowType a2 = new ArrowType(registry, registry.createParameters(NUMBER), NUMBER);
    ArrowType aDiffRet = new ArrowType(registry, registry.createParameters(NUMBER), STRING);
    ArrowType aDiffParam = new ArrowType(registry, registry.createParameters(STRING), NUMBER);

    assertTrue(a1.checkArrowEquivalenceHelper(a2, false));
    assertFalse(a1.checkArrowEquivalenceHelper(aDiffRet, false));
    assertFalse(a1.checkArrowEquivalenceHelper(aDiffParam, false));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 791 & Unknown Subtyping)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectIssue791HasUnknownParamsOrReturn() {
    // Defect check: hasUnknownParamsOrReturn accurately reports unknown parameters or returns
    ArrowType fullyKnown = new ArrowType(registry, registry.createParameters(NUMBER), STRING);
    assertFalse(fullyKnown.hasUnknownParamsOrReturn());

    ArrowType unknownReturn = new ArrowType(registry, registry.createParameters(NUMBER), UNKNOWN);
    assertTrue(unknownReturn.hasUnknownParamsOrReturn());

    ArrowType unknownParam = new ArrowType(registry, registry.createParameters(UNKNOWN), NUMBER);
    assertTrue(unknownParam.hasUnknownParamsOrReturn());

    Node paramWithNull = registry.createParameters(NUMBER);
    paramWithNull.getFirstChild().setJSType(null);
    ArrowType nullParam = new ArrowType(registry, paramWithNull, NUMBER);
    assertTrue(nullParam.hasUnknownParamsOrReturn());

    ArrowType nullReturnArrow = new ArrowType(registry, registry.createParameters(NUMBER), NUMBER);
    nullReturnArrow.returnType = null;
    assertTrue(nullReturnArrow.hasUnknownParamsOrReturn());
  }

  @Test(timeout = 4000)
  public void testDefectSubtypeWithUnknownsTolerateUnknownsEquivalence() {
    // Targets RecordTypeTest::testSubtypeWithUnknowns2 parameter checking with unknown types
    ArrowType unknownParamArrow = new ArrowType(registry, registry.createParameters(UNKNOWN), NUMBER);
    ArrowType numberParamArrow = new ArrowType(registry, registry.createParameters(NUMBER), NUMBER);

    // With tolerateUnknowns = false, UNKNOWN and NUMBER are not equivalent
    assertFalse(unknownParamArrow.hasEqualParameters(numberParamArrow, false));
    assertFalse(unknownParamArrow.checkArrowEquivalenceHelper(numberParamArrow, false));

    // With tolerateUnknowns = true, UNKNOWN is tolerated and treated as matching NUMBER
    assertTrue(unknownParamArrow.hasEqualParameters(numberParamArrow, true));
    assertTrue(unknownParamArrow.checkArrowEquivalenceHelper(numberParamArrow, true));

    // Also verify return type unknown tolerance
    ArrowType unknownRetArrow = new ArrowType(registry, registry.createParameters(NUMBER), UNKNOWN);
    ArrowType numberRetArrow = new ArrowType(registry, registry.createParameters(NUMBER), NUMBER);
    assertFalse(unknownRetArrow.checkArrowEquivalenceHelper(numberRetArrow, false));
    assertTrue(unknownRetArrow.checkArrowEquivalenceHelper(numberRetArrow, true));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeTopFunctionWithVarArgsBranches() {
    // "that" has var_args of UNKNOWN_TYPE: arity is unconstrained, top function branch
    Node thatVarArgsUnknown = registry.createParametersWithVarArgs(UNKNOWN);
    ArrowType superTopArrow = new ArrowType(registry, thatVarArgsUnknown, VOID);

    Node thisReq = registry.createParameters(NUMBER);
    ArrowType subArrow = new ArrowType(registry, thisReq, VOID);

    // Subtype must succeed because super is top function (...?)
    assertTrue(subArrow.isSubtype(superTopArrow));

    // "that" has var_args of NO_TYPE: isTopFunction is also true
    Node thatVarArgsNo = registry.createParametersWithVarArgs(NO);
    ArrowType superTopNoArrow = new ArrowType(registry, thatVarArgsNo, VOID);
    assertTrue(subArrow.isSubtype(superTopNoArrow));

    // "that" has var_args of null type: isTopFunction is also true
    Node thatVarArgsNull = registry.createParametersWithVarArgs(NUMBER);
    thatVarArgsNull.getFirstChild().setJSType(null);
    ArrowType superTopNullArrow = new ArrowType(registry, thatVarArgsNull, VOID);
    assertTrue(subArrow.isSubtype(superTopNullArrow));

    // "that" has var_args of NUMBER (concrete type): isTopFunction is false, subtyping fails
    Node thatVarArgsNumber = registry.createParametersWithVarArgs(NUMBER);
    ArrowType superConcreteVarArgs = new ArrowType(registry, thatVarArgsNumber, VOID);
    assertFalse(subArrow.isSubtype(superConcreteVarArgs));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetLeastSupertypeThrowsUnsupported() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);
    arrow.getLeastSupertype(NUMBER);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testGetGreatestSubtypeThrowsUnsupported() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);
    arrow.getGreatestSubtype(NUMBER);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testTestForEqualityThrowsUnsupported() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);
    arrow.testForEquality(NUMBER);
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testVisitThrowsUnsupported() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);
    arrow.visit(null);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeParamNullTypeBranches() {
    // thisParam has null type, thatParam has NUMBER type
    Node thisNullNode = registry.createParameters(NUMBER);
    thisNullNode.getFirstChild().setJSType(null);
    ArrowType thisNullArrow = new ArrowType(registry, thisNullNode, VOID);

    Node thatNumberNode = registry.createParameters(NUMBER);
    ArrowType thatNumberArrow = new ArrowType(registry, thatNumberNode, VOID);

    // thisParamType == null -> param type check passes, subtyping holds
    assertTrue(thisNullArrow.isSubtype(thatNumberArrow));

    // thisParam has NUMBER type, thatParam has null type -> thatParamType == null -> returns false
    assertFalse(thatNumberArrow.isSubtype(thisNullArrow));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testHashCodeContractAndBranches() {
    Node params1 = registry.createParameters(NUMBER);
    ArrowType a1 = new ArrowType(registry, params1, STRING, false);

    Node params2 = registry.createParameters(NUMBER);
    ArrowType a2 = new ArrowType(registry, params2, STRING, false);

    assertEquals("Equal configurations must yield identical hash codes", a1.hashCode(), a2.hashCode());

    ArrowType aInferred = new ArrowType(registry, registry.createParameters(NUMBER), STRING, true);
    assertEquals("returnTypeInferred must increment hashCode by 1", a1.hashCode() + 1, aInferred.hashCode());

    // Null returnType in hashCode()
    ArrowType aNullRet = new ArrowType(registry, registry.createParameters(NUMBER), STRING, false);
    aNullRet.returnType = null;
    int expectedHash = NUMBER.hashCode();
    assertEquals(expectedHash, aNullRet.hashCode());

    // Null parameter type in hashCode()
    Node paramsNull = registry.createParameters(NUMBER);
    paramsNull.getFirstChild().setJSType(null);
    ArrowType aNullParam = new ArrowType(registry, paramsNull, STRING, false);
    assertEquals(STRING.hashCode(), aNullParam.hashCode());
  }

  @Test(timeout = 4000)
  public void testResolveInternalIntegrity() {
    SimpleErrorReporter reporter = new SimpleErrorReporter();
    Node params = registry.createParameters(NUMBER);
    ArrowType arrow = new ArrowType(registry, params, STRING);

    JSType resolved = arrow.resolveInternal(reporter, null);
    assertSame(arrow, resolved);
    assertSame(STRING, arrow.returnType);
    assertSame(NUMBER, arrow.parameters.getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testToStringAndBooleanOutcomes() {
    ArrowType arrow = new ArrowType(registry, registry.createParameters(), VOID);

    assertEquals("[ArrowType]", arrow.toString());
    assertEquals("[ArrowType]", arrow.toStringHelper(false));
    assertEquals("[ArrowType]", arrow.toStringHelper(true));
    assertEquals(BooleanLiteralSet.TRUE, arrow.getPossibleToBooleanOutcomes());
  }

  @Test(timeout = 4000)
  public void testHasAnyTemplateInternalBranches() {
    TemplateType template = new TemplateType(registry, "T");

    // Case 1: returnType has template
    ArrowType retTemplateArrow = new ArrowType(registry, registry.createParameters(NUMBER), template);
    assertTrue(retTemplateArrow.hasAnyTemplateInternal());

    // Case 2: parameter has template
    ArrowType paramTemplateArrow = new ArrowType(registry, registry.createParameters(template), NUMBER);
    assertTrue(paramTemplateArrow.hasAnyTemplateInternal());

    // Case 3: neither has template
    ArrowType noTemplateArrow = new ArrowType(registry, registry.createParameters(NUMBER), STRING);
    assertFalse(noTemplateArrow.hasAnyTemplateInternal());

    // Case 4: parameter has null JSType
    Node pNull = registry.createParameters(NUMBER);
    pNull.getFirstChild().setJSType(null);
    ArrowType nullParamArrow = new ArrowType(registry, pNull, STRING);
    assertFalse(nullParamArrow.hasAnyTemplateInternal());
  }
}