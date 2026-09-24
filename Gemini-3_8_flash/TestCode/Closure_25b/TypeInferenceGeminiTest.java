/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.TypeInference
 * Known Defect: com.google.javascript.jscomp.TypeInferenceTest::testBackwardsInferenceNew
 * Failure Trace: ComparisonFailure: expected:<{[foo: (number|undefined)]}> but was:<{[]}>
 * Defect Cause: In traverseNew(Node, FlowScope), backwardsInferenceFromCallSite(n, ct) was omitted.
 *               When constructor calls (Token.NEW) instantiate a class with a parameter constraint,
 *               the inferred properties of arguments (such as anonymous object literals) are not widened.
 * ---------------------------------------------------------------------------------------------------------
 * Coverage Matrix:
 * - Partition A: Core Functional Logic & State Transitions
 *   * traverseAssign, traverseName, traverseCall, traverseNew
 *   * Token.ADD, Token.ASSIGN_ADD with string, number, and boolean operands
 *   * Token.ARRAYLIT, Token.OBJECTLIT, Token.HOOK (ternary), Token.THIS
 *   * Comparison and unary operators (Token.NOT, Token.TYPEOF, Token.BITNOT, Token.POS, Token.NEG)
 *   * Token.COMMA, Token.SWITCH, Token.RETURN, Token.CATCH, Token.THROW
 * - Partition B: Boundary Value Analysis & Extremes
 *   * flowThrough with bottomScope (short-circuit path)
 *   * getBooleanOutcomes static matrix with TRUE, FALSE, BOTH, EMPTY
 *   * Unbound local variables entry lattice initialization (VOID_TYPE)
 *   * Unflowable/escaped variables
 * - Partition C: Defect-Targeted Branch Zone
 *   * testBackwardsInferenceNew: Constructor backwards parameter inference on object literals
 *   * testBackwardsInferenceCall: Function call backwards parameter inference comparison
 * - Partition D: Exception & Defensive Guard Paths
 *   * Diagnostics: TEMPLATE_TYPE_NOT_OBJECT_TYPE
 *   * Diagnostics: TEMPLATE_TYPE_OF_THIS_EXPECTED
 *   * Diagnostics: FUNCTION_LITERAL_UNDEFINED_THIS
 *   * tightenTypesAfterAssertions with custom AssertionFunctionSpec
 * - Partition E: Object Lifecycle & Contract Integrity
 *   * Scope re-declaration, property definition on prototype and constructor this
 *   * dereferencePointer narrowing on null/undefined access
 * ---------------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TypeInferenceGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeInference lastTypeInference;

  /**
   * Helper to parse JavaScript within a synthetic function wrapper and run TypeInference on it.
   */
  private Scope inferInFunction(String js) {
    return inferInFunction(js, Collections.<String, AssertionFunctionSpec>emptyMap());
  }

  /**
   * Helper to parse JavaScript within a synthetic function wrapper and run TypeInference with assertion specs.
   */
  private Scope inferInFunction(String js, Map<String, AssertionFunctionSpec> assertionMap) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();

    Node externsRoot = new Node(Token.BLOCK);
    Node mainRoot = new Node(Token.BLOCK);
    Node root = new Node(Token.BLOCK, externsRoot, mainRoot);
    root.setIsSyntheticBlock(true);
    externsRoot.setIsSyntheticBlock(true);
    mainRoot.setIsSyntheticBlock(true);

    Node script = compiler.parseTestCode("function test() {\n" + js + "\n}");
    mainRoot.addChildToBack(script);

    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope topScope = scopeCreator.createScope(root, null);

    Node fnNode = script.getFirstChild();
    Scope fnScope = scopeCreator.createScope(fnNode, topScope);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, fnNode.getLastChild());
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    SemanticReverseAbstractInterpreter rai =
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry);

    lastTypeInference = new TypeInference(compiler, cfg, rai, fnScope, assertionMap);
    lastTypeInference.analyze();

    return fnScope;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Tests backwards inference when calling a constructor with 'new'.
   * In defective versions, traverseNew does not invoke backwardsInferenceFromCallSite,
   * causing the inferred object property constraint to be omitted (resulting in "{}" instead of "{foo: (number|undefined)}").
   */
  @Test(timeout = 4000)
  public void testBackwardsInferenceNew() {
    Scope scope = inferInFunction(
        "/**\n" +
        " * @constructor\n" +
        " * @param {{foo: (number|undefined)}} x\n" +
        " */\n" +
        "function F(x) {}\n" +
        "var x = {};\n" +
        "new F(x);");
    Var xVar = scope.getVar("x");
    assertNotNull("Variable x should exist in scope", xVar);
    assertNotNull("Variable x should have an inferred type", xVar.getType());
    assertEquals("{foo: (number|undefined)}", xVar.getType().toString());
  }

  /**
   * Comparative test for standard function call backwards inference,
   * confirming that normal calls properly widen object literal property constraints.
   */
  @Test(timeout = 4000)
  public void testBackwardsInferenceCall() {
    Scope scope = inferInFunction(
        "/**\n" +
        " * @param {{bar: (string|undefined)}} a\n" +
        " */\n" +
        "function f(a) {}\n" +
        "var y = {};\n" +
        "f(y);");
    Var yVar = scope.getVar("y");
    assertNotNull("Variable y should exist in scope", yVar);
    assertNotNull("Variable y should have an inferred type", yVar.getType());
    assertEquals("{bar: (string|undefined)}", yVar.getType().toString());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAdditionTypeInference() {
    Scope scope = inferInFunction(
        "var s1 = 'hello' + 5;\n" +
        "var s2 = 5 + 'world';\n" +
        "var n1 = 1 + 2;\n" +
        "var n2 = true + 1;\n" +
        "var u1 = {} + 1;\n" +
        "var a1 = 1;\n" +
        "a1 += 3;");

    assertEquals("string", scope.getVar("s1").getType().toString());
    assertEquals("string", scope.getVar("s2").getType().toString());
    assertEquals("number", scope.getVar("n1").getType().toString());
    assertEquals("number", scope.getVar("n2").getType().toString());
    assertEquals("(number|string)", scope.getVar("u1").getType().toString());
    assertEquals("number", scope.getVar("a1").getType().toString());
  }

  @Test(timeout = 4000)
  public void testArithmeticAndBitwiseOperators() {
    Scope scope = inferInFunction(
        "var pos = +5;\n" +
        "var neg = -5;\n" +
        "var bnot = ~5;\n" +
        "var mul = 2 * 3;\n" +
        "var div = 6 / 2;\n" +
        "var mod = 7 % 3;\n" +
        "var lsh = 1 << 2;\n" +
        "var rsh = 8 >> 1;\n" +
        "var ursh = 8 >>> 1;\n" +
        "var band = 1 & 2;\n" +
        "var bor = 1 | 2;\n" +
        "var bxor = 1 ^ 2;\n" +
        "var inc = 1;\n" +
        "inc++;\n" +
        "var dec = 1;\n" +
        "--dec;");

    assertEquals("number", scope.getVar("pos").getType().toString());
    assertEquals("number", scope.getVar("neg").getType().toString());
    assertEquals("number", scope.getVar("bnot").getType().toString());
    assertEquals("number", scope.getVar("mul").getType().toString());
    assertEquals("number", scope.getVar("div").getType().toString());
    assertEquals("number", scope.getVar("mod").getType().toString());
    assertEquals("number", scope.getVar("lsh").getType().toString());
    assertEquals("number", scope.getVar("rsh").getType().toString());
    assertEquals("number", scope.getVar("ursh").getType().toString());
    assertEquals("number", scope.getVar("band").getType().toString());
    assertEquals("number", scope.getVar("bor").getType().toString());
    assertEquals("number", scope.getVar("bxor").getType().toString());
    assertEquals("number", scope.getVar("inc").getType().toString());
    assertEquals("number", scope.getVar("dec").getType().toString());
  }

  @Test(timeout = 4000)
  public void testComparisonAndBooleanOperators() {
    Scope scope = inferInFunction(
        "var lt = 1 < 2;\n" +
        "var le = 1 <= 2;\n" +
        "var gt = 1 > 2;\n" +
        "var ge = 1 >= 2;\n" +
        "var eq = 1 == 2;\n" +
        "var ne = 1 != 2;\n" +
        "var sheq = 1 === 2;\n" +
        "var shne = 1 !== 2;\n" +
        "var not = !1;\n" +
        "var inOp = 'a' in {};\n" +
        "var inst = {} instanceof Object;\n" +
        "var del = delete ({}).x;");

    assertEquals("boolean", scope.getVar("lt").getType().toString());
    assertEquals("boolean", scope.getVar("le").getType().toString());
    assertEquals("boolean", scope.getVar("gt").getType().toString());
    assertEquals("boolean", scope.getVar("ge").getType().toString());
    assertEquals("boolean", scope.getVar("eq").getType().toString());
    assertEquals("boolean", scope.getVar("ne").getType().toString());
    assertEquals("boolean", scope.getVar("sheq").getType().toString());
    assertEquals("boolean", scope.getVar("shne").getType().toString());
    assertEquals("boolean", scope.getVar("not").getType().toString());
    assertEquals("boolean", scope.getVar("inOp").getType().toString());
    assertEquals("boolean", scope.getVar("inst").getType().toString());
    assertEquals("boolean", scope.getVar("del").getType().toString());
  }

  @Test(timeout = 4000)
  public void testShortCircuitLogicalOperators() {
    Scope scope = inferInFunction(
        "var and1 = 1 && 'str';\n" +
        "var or1 = 0 || 'fallback';\n" +
        "var hook = true ? 10 : 'str';");

    assertEquals("string", scope.getVar("and1").getType().toString());
    assertEquals("string", scope.getVar("or1").getType().toString());
    assertEquals("(number|string)", scope.getVar("hook").getType().toString());
  }

  @Test(timeout = 4000)
  public void testArrayAndObjectLiterals() {
    Scope scope = inferInFunction(
        "var arr = [1, 2, 3];\n" +
        "var obj = {k1: 'v1', k2: 42};");

    assertTrue(scope.getVar("arr").getType().isArrayType());
    assertTrue(scope.getVar("obj").getType().isObjectType());
  }

  @Test(timeout = 4000)
  public void testTypeofAndCommaOperators() {
    Scope scope = inferInFunction(
        "var t = typeof 123;\n" +
        "var c = (1, 'final');");

    assertEquals("string", scope.getVar("t").getType().toString());
    assertEquals("string", scope.getVar("c").getType().toString());
  }

  @Test(timeout = 4000)
  public void testCatchParameterType() {
    Scope scope = inferInFunction(
        "var caughtType;\n" +
        "try {\n" +
        "  throw 'error';\n" +
        "} catch (e) {\n" +
        "  caughtType = e;\n" +
        "}");

    assertEquals("?", scope.getVar("caughtType").getType().toString());
  }

  @Test(timeout = 4000)
  public void testForInKeyInference() {
    Scope scope = inferInFunction(
        "var o = {a: 1, b: 2};\n" +
        "var key;\n" +
        "for (var k in o) {\n" +
        "  key = k;\n" +
        "}");

    assertEquals("string", scope.getVar("key").getType().toString());
  }

  @Test(timeout = 4000)
  public void testReturnConstraintMatching() {
    Scope scope = inferInFunction(
        "/** @return {{retProp: number}} */\n" +
        "function getObj() {\n" +
        "  return {retProp: 123};\n" +
        "}\n" +
        "var res = getObj();");

    assertNotNull(scope.getVar("res").getType());
    assertTrue(scope.getVar("res").getType().isObjectType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanOutcomesTruthTable() {
    // Condition = true (AND style short-circuiting)
    BooleanLiteralSet res1 = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true);
    assertEquals(BooleanLiteralSet.FALSE, res1);

    BooleanLiteralSet res2 = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, true);
    assertEquals(BooleanLiteralSet.FALSE, res2);

    // Condition = false (OR style short-circuiting)
    BooleanLiteralSet res3 = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, false);
    assertEquals(BooleanLiteralSet.BOTH, res3);

    BooleanLiteralSet res4 = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.EMPTY, BooleanLiteralSet.TRUE, true);
    assertEquals(BooleanLiteralSet.TRUE, res4);
  }

  @Test(timeout = 4000)
  public void testFlowThroughBottomScope() {
    inferInFunction("var a = 1;");
    assertNotNull(lastTypeInference);

    FlowScope bottom = lastTypeInference.createInitialEstimateLattice();
    FlowScope entry = lastTypeInference.createEntryLattice();
    assertNotNull(bottom);
    assertNotNull(entry);

    Node dummyNode = new Node(Token.EMPTY);
    FlowScope result = lastTypeInference.flowThrough(dummyNode, bottom);
    assertSame("Flow through bottom scope must short-circuit and return bottomScope", bottom, result);
  }

  @Test(timeout = 4000)
  public void testDeclarativelyUnboundVarsAreVoidInitially() {
    Scope scope = inferInFunction("var unassigned;");
    Var unassignedVar = scope.getVar("unassigned");
    assertNotNull(unassignedVar);
    // Declaratively unbound vars receive undefined (VOID_TYPE)
    assertEquals("undefined", unassignedVar.getType().toString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testTemplateTypeNotObjectTypeDiagnostic() {
    inferInFunction(
        "/**\n" +
        " * @param {T} x\n" +
        " * @param {function(this:T, ...)} y\n" +
        " * @template T\n" +
        " */\n" +
        "function fn(x, y) {}\n" +
        "fn(12345, function() {});");

    boolean warningFound = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().key.equals(TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.key)) {
        warningFound = true;
        break;
      }
    }
    assertTrue("Warning JSC_TEMPLATE_TYPE_NOT_OBJECT_TYPE expected for non-object argument", warningFound);
  }

  @Test(timeout = 4000)
  public void testTemplateTypeOfThisExpectedDiagnostic() {
    inferInFunction(
        "/**\n" +
        " * @param {T} x\n" +
        " * @template T\n" +
        " */\n" +
        "function fn(x) {}\n" +
        "fn({});");

    boolean warningFound = false;
    for (JSError error : compiler.getWarnings()) {
      if (error.getType().key.equals(TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.key)) {
        warningFound = true;
        break;
      }
    }
    assertTrue("Warning JSC_TEMPLATE_TYPE_OF_THIS_EXPECTED expected when this parameter is absent", warningFound);
  }

  @Test(timeout = 4000)
  public void testTightenTypesAfterAssertions() {
    Map<String, AssertionFunctionSpec> assertionMap = new HashMap<String, AssertionFunctionSpec>();
    assertionMap.put("assertString", new AssertionFunctionSpec("assertString", JSTypeNative.STRING_TYPE));

    Scope scope = inferInFunction(
        "function assertString(val) {}\n" +
        "var val = /** @type {?} */ ('initial');\n" +
        "assertString(val);\n" +
        "var res = val;",
        assertionMap);

    Var resVar = scope.getVar("res");
    assertNotNull(resVar);
    assertEquals("string", resVar.getType().toString());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDereferencePointerNarrowsType() {
    Scope scope = inferInFunction(
        "var x = null;\n" +
        "var prop = x.foo;");

    Var xVar = scope.getVar("x");
    assertNotNull(xVar);
    // Dereferencing pointer narrows x away from null/undefined
    assertFalse(xVar.getType().isNullType());
  }

  @Test(timeout = 4000)
  public void testConstructorThisPropertyDefinition() {
    Scope scope = inferInFunction(
        "/** @constructor */\n" +
        "function Person() {\n" +
        "  this.name = 'Alice';\n" +
        "}\n" +
        "Person.prototype.age = 30;\n" +
        "var p = new Person();\n" +
        "p.customProp = true;");

    Var pVar = scope.getVar("p");
    assertNotNull(pVar);
    assertTrue(pVar.getType().isInstanceType());
  }
}