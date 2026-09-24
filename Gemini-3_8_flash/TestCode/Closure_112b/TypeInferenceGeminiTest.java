/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.TypeInference
 * Defects4J Bugs Targeted: Closure-1058 / TypeCheckTest::testIssue1058, testTemplatized11
 *
 * Key Decision Branches & Scenarios Covered:
 * 1. Template Type Resolution & Defect Zone (Issue 1058 / testTemplatized11):
 *    - Method calls on templatized constructor instances (e.g., C.<number>.filter(...))
 *    - Inferred parameter types on function callbacks passed to templatized methods
 *    - inferTemplateTypesFromParameters: fnType.getTypeOfThis() resolution with specialized receiver
 *    - backwardsInferenceFromCallSite & updateTypeOfParameters
 * 2. Short-Circuiting Binary Operators (&&, ||):
 *    - traverseAnd, traverseOr, traverseShortCircuitingBinOp
 *    - BooleanOutcomePair and BooleanLiteralSet calculations via getBooleanOutcomes
 * 3. Flow Through & Branched Flow Through:
 *    - bottomScope short-circuit guard in flowThrough
 *    - ON_TRUE branch for for-in statements (NodeUtil.isForIn) & simple var redeclaration
 *    - ON_FALSE / ON_TRUE branches with condition expressions (And/Or vs simple conditions)
 *    - Case statements in switch conditions
 * 4. Token-Specific Traversal Paths:
 *    - ASSIGN, ASSIGN_ADD, ADD (string + string, number + number, unknown joins)
 *    - Arithmetic and bitwise operators (Token.POS, NEG, MUL, DIV, MOD, BITAND, BITOR, LSH, etc.)
 *    - Comparisons (LT, LE, GT, GE, EQ, NE, SHEQ, SHNE, INSTANCEOF, IN, DELPROP)
 *    - TYPEOF, COMMA, PARAM_LIST, THIS
 *    - HOOK (? :) ternary operator type unioning
 *    - ARRAYLIT, OBJECTLIT, GETELEM, GETPROP
 *    - EXPR_RESULT with property declarations
 *    - RETURN with property constraint matching (inferPropertyTypesToMatchConstraint)
 *    - CATCH variable typing (declared vs unknown fallback)
 *    - CAST node evaluation with JSDocInfo
 * 5. Assertion Functions & Scope Narrowing:
 *    - tightenTypesAfterAssertions with goog.asserts (assert, assertNumber, assertString)
 *    - narrowScope for getprop vs simple name
 * 6. Function Invocations & Arguments Inference:
 *    - IIFE argument type inference on call target
 *    - Bind calls (updateBind via CodingConvention)
 *    - Unbound variable entry lattice initialization (VOID_TYPE)
 */

package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class TypeInferenceGeminiTest {

  // =========================================================================
  // Test Helpers
  // =========================================================================

  private Compiler compileAndCheck(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    String externs = ""
        + "var undefined;\n"
        + "/** @constructor @template T\n"
        + " * @param {...*} var_args */\n"
        + "function Array(var_args) {}\n"
        + "/** @constructor */\n"
        + "function Object() {}\n"
        + "/** @constructor\n"
        + " * @param {...*} var_args */\n"
        + "function Function(var_args) {}\n"
        + "/** @constructor\n"
        + " * @param {*=} opt_str */\n"
        + "function String(opt_str) {}\n"
        + "/** @constructor\n"
        + " * @param {*=} opt_num */\n"
        + "function Number(opt_num) {}\n"
        + "/** @constructor\n"
        + " * @param {*=} opt_bool */\n"
        + "function Boolean(opt_bool) {}\n"
        + "/** @type {!Function} */\n"
        + "Function.prototype.bind = function(thisArg, var_args) {};\n"
        + "var goog = {};\n"
        + "goog.asserts = {};\n"
        + "goog.asserts.assert = function(cond, opt_msg) {};\n"
        + "goog.asserts.assertNumber = function(val, opt_msg) {};\n"
        + "goog.asserts.assertString = function(val, opt_msg) {};\n"
        + "goog.bind = function(fn, selfObj, var_args) {};\n";

    SourceFile externFile = SourceFile.fromCode("externs.js", externs);
    SourceFile inputFile = SourceFile.fromCode("input.js", js);
    compiler.compile(ImmutableList.of(externFile), ImmutableList.of(inputFile), options);
    return compiler;
  }

  private String formatErrors(Compiler compiler) {
    StringBuilder sb = new StringBuilder();
    for (JSError error : compiler.getErrors()) {
      sb.append("ERROR: ").append(error.toString()).append("\n");
    }
    for (JSError warning : compiler.getWarnings()) {
      sb.append("WARNING: ").append(warning.toString()).append("\n");
    }
    return sb.toString();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 1058 & testTemplatized11)
  // =========================================================================

  /**
   * Targets Issue 1058:
   * In defective versions, inferTemplateTypesFromParameters failed to resolve the
   * template type T from the receiver instance (c of type C.<number>).
   * Consequently, the parameter 'n' in the callback was not inferred as number,
   * producing an unexpected warning on `n > 0`.
   */
  @Test(timeout = 4000)
  public void testIssue1058() {
    String js = ""
        + "/**\n"
        + " * @constructor\n"
        + " * @template T\n"
        + " */\n"
        + "function C() {}\n"
        + "/**\n"
        + " * @param {function(T): boolean} filter\n"
        + " * @return {C.<T>}\n"
        + " */\n"
        + "C.prototype.filter = function(filter) {};\n"
        + "/**\n"
        + " * @param {C.<number>} c\n"
        + " */\n"
        + "function foo(c) {\n"
        + "  c.filter(function(n) { return n > 0; });\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Unexpected errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Unexpected warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  /**
   * Targets testTemplatized11:
   * Confirms backwards type inference for higher-order function arguments
   * invoked on specialized templatized objects.
   */
  @Test(timeout = 4000)
  public void testTemplatized11() {
    String js = ""
        + "/**\n"
        + " * @constructor\n"
        + " * @template T\n"
        + " */\n"
        + "function Collection() {}\n"
        + "/**\n"
        + " * @param {function(T): void} callback\n"
        + " */\n"
        + "Collection.prototype.forEach = function(callback) {};\n"
        + "/**\n"
        + " * @param {Collection.<string>} coll\n"
        + " */\n"
        + "function processCollection(coll) {\n"
        + "  coll.forEach(function(item) {\n"
        + "    var /** string */ s = item;\n"
        + "  });\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Unexpected errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Unexpected warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAssignmentAndLocalVariableTypeInference() {
    String js = ""
        + "function testVarScope() {\n"
        + "  var a = 10;\n"
        + "  a = 20;\n"
        + "  var b = 'hello';\n"
        + "  b = 'world';\n"
        + "  var c = true;\n"
        + "  c = false;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testArithmeticAndBitwiseOperations() {
    String js = ""
        + "function mathOps(x, y) {\n"
        + "  var pos = +x;\n"
        + "  var neg = -y;\n"
        + "  var bitnot = ~x;\n"
        + "  var inc = x++;\n"
        + "  var dec = --y;\n"
        + "  var mul = x * y;\n"
        + "  var div = x / y;\n"
        + "  var mod = x % y;\n"
        + "  var sub = x - y;\n"
        + "  var lsh = x << 1;\n"
        + "  var rsh = x >> 1;\n"
        + "  var ursh = x >>> 1;\n"
        + "  var band = x & y;\n"
        + "  var bor = x | y;\n"
        + "  var bxor = x ^ y;\n"
        + "  x += 1;\n"
        + "  x -= 1;\n"
        + "  x *= 2;\n"
        + "  x /= 2;\n"
        + "  x %= 2;\n"
        + "  x &= 1;\n"
        + "  x |= 1;\n"
        + "  x ^= 1;\n"
        + "  x <<= 1;\n"
        + "  x >>= 1;\n"
        + "  x >>>= 1;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testAddOperatorBranches() {
    String js = ""
        + "function addCombinations(p1, p2) {\n"
        + "  var str1 = 'abc' + 'def';\n"
        + "  var str2 = 'num: ' + 123;\n"
        + "  var str3 = 123 + ' is num';\n"
        + "  var num1 = 10 + 20;\n"
        + "  var num2 = true + 5;\n"
        + "  var num3 = null + 5;\n"
        + "  var unk = p1 + p2;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testLogicalShortCircuitingAndHook() {
    String js = ""
        + "function logicAndTernary(a, b, c) {\n"
        + "  var andRes = a && b;\n"
        + "  var orRes = a || b;\n"
        + "  var hookRes = a ? b : c;\n"
        + "  if (a && b) {\n"
        + "    var branch1 = a;\n"
        + "  }\n"
        + "  if (a || b) {\n"
        + "    var branch2 = a;\n"
        + "  }\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testComparisonAndTypeofOperators() {
    String js = ""
        + "function comparisons(x, y) {\n"
        + "  var lt = x < y;\n"
        + "  var le = x <= y;\n"
        + "  var gt = x > y;\n"
        + "  var ge = x >= y;\n"
        + "  var eq = x == y;\n"
        + "  var ne = x != y;\n"
        + "  var sheq = x === y;\n"
        + "  var shne = x !== y;\n"
        + "  var not = !x;\n"
        + "  var inst = x instanceof Object;\n"
        + "  var inOp = 'prop' in y;\n"
        + "  var del = delete y.prop;\n"
        + "  var tof = typeof x;\n"
        + "  var comma = (x, y);\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForInLoopControlFlow() {
    String js = ""
        + "function forInTest(obj) {\n"
        + "  for (var prop in obj) {\n"
        + "    var /** string */ key = prop;\n"
        + "  }\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testSwitchAndCaseControlFlow() {
    String js = ""
        + "function switchTest(val) {\n"
        + "  switch (val) {\n"
        + "    case 1:\n"
        + "      var a = 'one';\n"
        + "      break;\n"
        + "    case 2:\n"
        + "      var b = 'two';\n"
        + "      break;\n"
        + "    default:\n"
        + "      var d = 'default';\n"
        + "  }\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testReturnWithConstraintWidening() {
    String js = ""
        + "/** @return {{name: string, age: (number|undefined)}} */\n"
        + "function makePerson() {\n"
        + "  return {name: 'Alice'};\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCatchClausesWithTypeAndWithout() {
    String js = ""
        + "function testCatch() {\n"
        + "  try {\n"
        + "    throw new Error('err');\n"
        + "  } catch (e) {\n"
        + "    var caught = e;\n"
        + "  }\n"
        + "  try {\n"
        + "    throw 'err string';\n"
        + "  } catch (/** @type {string} */ errStr) {\n"
        + "    var /** string */ s = errStr;\n"
        + "  }\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testTypeCastTraversal() {
    String js = ""
        + "function testCast(x) {\n"
        + "  var y = /** @type {number} */ (x);\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBooleanOutcomesMatrixDirect() {
    // Verifies TypeInference.getBooleanOutcomes calculation truth table:
    // right.union(left.intersection(BooleanLiteralSet.get(!condition)))
    
    // Condition = true -> !condition = false -> intersection with BooleanLiteralSet.FALSE
    assertEquals(
        BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    assertEquals(
        BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true));
    assertEquals(
        BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, true));
    assertEquals(
        BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.EMPTY, true));
    assertEquals(
        BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));

    // Condition = false -> !condition = true -> intersection with BooleanLiteralSet.TRUE
    assertEquals(
        BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.EMPTY, false));
    assertEquals(
        BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, false));
    assertEquals(
        BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.FALSE, false));
    assertEquals(
        BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.EMPTY, false));
  }

  @Test(timeout = 4000)
  public void testDiagnosticTypeConstant() {
    assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test(timeout = 4000)
  public void testDirectTypeInferenceLatticeLifecycle() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);

    Node name = Node.newString(Token.NAME, "testFn");
    Node params = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "param1"));
    Node body = new Node(Token.BLOCK);
    Node functionNode = new Node(Token.FUNCTION, name, params, body);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, functionNode);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    Scope fnScope = scopeCreator.createScope(functionNode, null);

    TypeInference inference = new TypeInference(
        compiler, cfg, compiler.getReverseAbstractInterpreter(),
        fnScope, Collections.emptyMap());

    assertNotNull(inference.createInitialEstimateLattice());
    assertNotNull(inference.createEntryLattice());

    // flowThrough with bottomScope returns bottomScope immediately
    com.google.javascript.jscomp.type.FlowScope bottom = inference.createInitialEstimateLattice();
    com.google.javascript.jscomp.type.FlowScope flowResult = inference.flowThrough(functionNode, bottom);
    assertSame(bottom, flowResult);
  }

  @Test(timeout = 4000)
  public void testIIFEArgumentInference() {
    String js = ""
        + "(function(x, y) {\n"
        + "  var sum = x + y;\n"
        + "})(10, 20);\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testUnboundVarsWithoutTypesEntryScope() {
    String js = ""
        + "function unboundVars() {\n"
        + "  var a;\n"
        + "  var b;\n"
        + "  var c;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testEmptyAndNestedLiterals() {
    String js = ""
        + "function literals() {\n"
        + "  var emptyObj = {};\n"
        + "  var emptyArr = [];\n"
        + "  var nestedObj = { a: { b: 1 } };\n"
        + "  var nestedArr = [[1, 2], [3, 4]];\n"
        + "  var val = nestedObj.a.b;\n"
        + "  var el = nestedArr[0][1];\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypeAssertionsNarrowing() {
    String js = ""
        + "function testAssertions(x, y, z) {\n"
        + "  goog.asserts.assert(x != null);\n"
        + "  var narrowedX = x;\n"
        + "  goog.asserts.assertNumber(y);\n"
        + "  var /** number */ numY = y;\n"
        + "  goog.asserts.assertString(z);\n"
        + "  var /** string */ strZ = z;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstructorAndPrototypePropertyDefinitions() {
    String js = ""
        + "/** @constructor */\n"
        + "function Point(x, y) {\n"
        + "  this.x = x;\n"
        + "  this.y = y;\n"
        + "}\n"
        + "Point.prototype.translate = function(dx, dy) {\n"
        + "  this.x += dx;\n"
        + "  this.y += dy;\n"
        + "};\n"
        + "function run() {\n"
        + "  var pt = new Point(1, 2);\n"
        + "  pt.translate(3, 4);\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testFunctionBindInference() {
    String js = ""
        + "function add(a, b) {\n"
        + "  return a + b;\n"
        + "}\n"
        + "function testBind() {\n"
        + "  var boundAdd = add.bind(null, 5);\n"
        + "  var googBound = goog.bind(add, null, 10);\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testQualifiedNameInference() {
    String js = ""
        + "var ns = {};\n"
        + "ns.sub = {};\n"
        + "ns.sub.val = 42;\n"
        + "function readNs() {\n"
        + "  var /** number */ x = ns.sub.val;\n"
        + "}\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptFlow() {
    String js = "";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testThisInMethodsAndFunctions() {
    String js = ""
        + "/** @constructor */\n"
        + "function Widget() {\n"
        + "  this.val = 1;\n"
        + "}\n"
        + "Widget.prototype.getVal = function() {\n"
        + "  return this.val;\n"
        + "};\n";
    Compiler compiler = compileAndCheck(js);
    assertEquals("Errors: " + formatErrors(compiler), 0, compiler.getErrorCount());
    assertEquals("Warnings: " + formatErrors(compiler), 0, compiler.getWarningCount());
  }
}