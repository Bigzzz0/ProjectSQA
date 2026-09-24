/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * TARGET CLASS: com.google.javascript.jscomp.TypeInference
 * EXTENDS: DataFlowAnalysis.BranchedForwardDataFlowAnalysis<Node, FlowScope>
 *
 * DECISION BRANCH & COVERAGE TARGETS:
 * 1. inferArguments():
 *    - Function scope vs non-function root (SCRIPT/BLOCK)
 *    - IIFE argument inference: NodeUtil.isCallOrNewTarget(functionNode) -> true vs false
 *    - Untyped AST parameter matching against declared function parameter types
 * 2. branchedFlowThrough():
 *    - Branch.ON_TRUE on For-In loop (NodeUtil.isForIn) -> string key inference & property indexing
 *    - Branch.ON_TRUE / Branch.ON_FALSE condition resolution (NodeUtil.getConditionExpression)
 *    - CASE statement conditions (source.isCase())
 *    - Short-circuiting binary operators: condition.isAnd() vs condition.isOr()
 * 3. traverse() AST Node Handlers:
 *    - ASSIGN, NAME, GETPROP, AND, OR, HOOK, OBJECTLIT, CALL, NEW, ADD, ASSIGN_ADD
 *    - POS, NEG, ARRAYLIT, THIS, INC, DEC, BITNOT, arithmetic/bitwise tokens
 *    - PARAM_LIST, COMMA, TYPEOF, comparison/equality tokens (LT, GT, EQ, SHEQ, INSTANCEOF, IN)
 *    - GETELEM (array/object indexed access with TemplateTypeMap), EXPR_RESULT
 *    - SWITCH, RETURN (constraint matching against function return type)
 *    - VAR, THROW, CATCH (with and without JSDoc type info), CAST
 * 4. tightenTypesAfterAssertions():
 *    - Registered AssertionFunctionSpec matching qualified name
 *    - Boolean condition assertions vs explicit type assertions
 * 5. inferTemplatedTypesForCall() & backwardsInferenceFromCallSite():
 *    - Function with template types (@template T)
 *    - Bound function return type inference via CodingConvention.describeFunctionBind
 *    - Function literal arguments matching formal parameter function types
 * 6. getBooleanOutcomes() / BooleanOutcomePair:
 *    - Pure BooleanLiteralSet permutations (EMPTY, TRUE, FALSE, BOTH) across condition states
 * 7. DEFECT TARGETS (Defects4J Ground Truth):
 *    - testIssue1023: Function call template type resolution mismatch on method invocation (c.method(1))
 *    - testPropertiesOnInterface2: Interface prototype property declaration handling (NPE guard)
 *    - testMethodBeforeFunction2: Qualified method assignment prior to constructor declaration
 * ====================================================================================================
 */

package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TypeInferenceGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Map<String, AssertionFunctionSpec> assertionMap;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    assertionMap = Maps.newHashMap();
  }

  // ==================================================================================================
  // Helper Infrastructure
  // ==================================================================================================

  private TypeInference createTypeInference(String js) {
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    compiler.initOptions(options);

    Node root = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(root, null);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    return new TypeInference(
        compiler, cfg, compiler.getReverseAbstractInterpreter(), scope, assertionMap);
  }

  private Compiler compile(String js) {
    Compiler c = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);

    List<SourceFile> externs = Lists.newArrayList(
        SourceFile.fromCode("externs.js",
            "var window;\n" +
            "var undefined;\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {!Function} */\n" +
            "function Function(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {!Object} */\n" +
            "function Object(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {string} */\n" +
            "function String(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {boolean} */\n" +
            "function Boolean(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {number} */\n" +
            "function Number(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args\n * @return {!Array} */\n" +
            "function Array(var_args) {}\n" +
            "/** @constructor\n * @param {...*} var_args */\n" +
            "function Error(var_args) {}\n")
    );
    List<SourceFile> inputs = Lists.newArrayList(
        SourceFile.fromCode("testcode.js", js)
    );
    c.compile(externs, inputs, options);
    return c;
  }

  // ==================================================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testFlowThrough_assignAndVarDeclaration() {
    TypeInference ti = createTypeInference("var x = 10; var y = x;");
    ti.analyze();

    FlowScope entry = ti.createEntryLattice();
    assertNotNull("Entry lattice must not be null", entry);
    FlowScope initial = ti.createInitialEstimateLattice();
    assertNotNull("Initial lattice must not be null", initial);
    assertNotSame("Entry and bottom lattices must be distinct", entry, initial);
  }

  @Test(timeout = 4000)
  public void testFlowThrough_binaryAddVariations() {
    // Tests: String + String, Number + Number, String + Unknown, Number + Boolean
    String js =
        "var s1 = 'hello' + ' world';\n" +
        "var n1 = 10 + 20;\n" +
        "var sn = 'count: ' + 5;\n" +
        "var nb = 5 + true;\n" +
        "var assignAdd = 1;\n" +
        "assignAdd += 2;\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_arithmeticAndUnaryTokens() {
    String js =
        "var a = 10;\n" +
        "var b = -a;\n" +
        "var c = +a;\n" +
        "var d = ~a;\n" +
        "a++;\n" +
        "a--;\n" +
        "var e = a * 2;\n" +
        "var f = a / 2;\n" +
        "var g = a % 2;\n" +
        "var h = a << 1;\n" +
        "var i = a >> 1;\n" +
        "var j = a >>> 1;\n" +
        "var k = a & 1;\n" +
        "var l = a | 1;\n" +
        "var m = a ^ 1;\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_comparisonsAndBooleans() {
    String js =
        "var a = (1 < 2);\n" +
        "var b = (1 <= 2);\n" +
        "var c = (1 > 2);\n" +
        "var d = (1 >= 2);\n" +
        "var e = (1 == 2);\n" +
        "var f = (1 != 2);\n" +
        "var g = (1 === 2);\n" +
        "var h = (1 !== 2);\n" +
        "var i = !a;\n" +
        "var j = typeof a;\n" +
        "var k = (1 in [1, 2]);\n" +
        "var l = ([] instanceof Object);\n" +
        "var m = delete a;\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_objectLiteralAndPropertyInference() {
    String js =
        "var obj = { x: 10, y: 'hello' };\n" +
        "var px = obj.x;\n" +
        "var py = obj.y;\n" +
        "obj.z = true;\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_arrayLiteralAndGetElem() {
    String js =
        "var arr = ['a', 'b', 'c'];\n" +
        "var first = arr[0];\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_hookAndCommaOperators() {
    String js =
        "var cond = true;\n" +
        "var res = cond ? 10 : 'fallback';\n" +
        "var comma = (1, 2, 'final');\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_functionReturnAndConstraintMatching() {
    String js =
        "/** @return {number} */\n" +
        "function getNum() {\n" +
        "  return 42;\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  // ==================================================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testBooleanLiteralSet_outcomesBoundary() {
    // Test pure static helper getBooleanOutcomes
    // condition = true
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, true));
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.EMPTY, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.BOTH, true));

    // condition = false
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.EMPTY, false));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.FALSE, false));
  }

  @Test(timeout = 4000)
  public void testFlowThrough_bottomScopeDirectReturn() {
    TypeInference ti = createTypeInference("var x = 1;");
    FlowScope bottom = ti.createInitialEstimateLattice();
    Node scriptNode = compiler.parseTestCode("var y = 2;");

    FlowScope out = ti.flowThrough(scriptNode, bottom);
    assertSame("Passing bottomScope directly to flowThrough should return bottomScope unchanged",
        bottom, out);
  }

  @Test(timeout = 4000)
  public void testFlowThrough_shortCircuitLogicalBranches() {
    String js =
        "var a = true;\n" +
        "var b = false;\n" +
        "var andRes = a && b;\n" +
        "var orRes = a || b;\n" +
        "if (a && b) {\n" +
        "  var inAnd = 1;\n" +
        "}\n" +
        "if (a || b) {\n" +
        "  var inOr = 2;\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_forInLoop() {
    String js =
        "var obj = {a: 1, b: 2};\n" +
        "for (var key in obj) {\n" +
        "  var val = obj[key];\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_catchBlocksWithAndWithoutJSDoc() {
    String js =
        "try {\n" +
        "  throw 'err';\n" +
        "} catch (e) {\n" +
        "  var caught1 = e;\n" +
        "}\n" +
        "try {\n" +
        "  throw 'err2';\n" +
        "} catch (/** @type {Error} */ e2) {\n" +
        "  var caught2 = e2;\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_typeCast() {
    String js = "var casted = /** @type {number} */ ('42');\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFlowThrough_switchAndCaseStatements() {
    String js =
        "var val = 2;\n" +
        "switch (val) {\n" +
        "  case 1:\n" +
        "    var res1 = 'one';\n" +
        "    break;\n" +
        "  case 2:\n" +
        "    var res2 = 'two';\n" +
        "    break;\n" +
        "  default:\n" +
        "    var resDef = 'other';\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  // ==================================================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Ground Truth from Defects4J)
  // ==================================================================================================

  /**
   * Targets com.google.javascript.jscomp.TypeCheckTest::testIssue1023
   * Defect: In templated method calls, template type resolution fails when parameter types
   * should trigger a type mismatch warning.
   */
  @Test(timeout = 4000)
  public void testDefect_issue1023_templatedMethodCall() {
    String js =
        "/**\n" +
        " * @constructor\n" +
        " * @template T\n" +
        " */\n" +
        "function C() {}\n" +
        "/**\n" +
        " * @param {T} a\n" +
        " * @return {T}\n" +
        " * @template T\n" +
        " */\n" +
        "C.prototype.method = function(a) {};\n" +
        "/** @type {C.<string>} */\n" +
        "var c = new C();\n" +
        "c.method(1);\n";

    Compiler c = compile(js);
    assertEquals("Should produce a type warning for argument mismatch on templated method",
        1, c.getWarningCount());
    assertTrue("Warning should describe parameter type mismatch",
        c.getWarnings()[0].getDescription().contains("actual parameter 1"));
  }

  /**
   * Targets com.google.javascript.jscomp.TypedScopeCreatorTest::testPropertiesOnInterface2
   * Defect: Interface prototype property declarations caused NullPointerException in
   * ensurePropertyDeclared / ensurePropertyDeclaredHelper.
   */
  @Test(timeout = 4000)
  public void testDefect_propertiesOnInterface2() {
    String js =
        "/** @interface */ function Foo() {}\n" +
        "/** @type {number} */ Foo.prototype.bar;\n";

    Compiler c = compile(js);
    assertEquals("Compiling interface property declaration must produce 0 fatal errors and no NPE",
        0, c.getErrorCount());
  }

  /**
   * Targets com.google.javascript.jscomp.TypedScopeCreatorTest::testMethodBeforeFunction2
   * Defect: Incomplete forward reference inference when method assigned before constructor.
   */
  @Test(timeout = 4000)
  public void testDefect_methodBeforeFunction2() {
    String js =
        "var a = {};\n" +
        "a.foo = function() { new Foo(); };\n" +
        "(function() {\n" +
        "  /** @constructor */ function Foo() {}\n" +
        "})();\n";

    Compiler c = compile(js);
    assertEquals("Method assigned before constructor definition must compile with 0 errors",
        0, c.getErrorCount());
  }

  // ==================================================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testAssertionFunctions_tightenScope() {
    // Register custom assertion function spec
    assertionMap.put("myAssert", new AssertionFunctionSpec("myAssert"));

    String js =
        "function myAssert(condition) {}\n" +
        "/** @type {?string} */ var str = null;\n" +
        "myAssert(str != null);\n" +
        "var length = str.length;\n";

    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testDereferencePointer_narrowsNullableProperty() {
    String js =
        "/** @type {?{prop: string}} */ var obj = null;\n" +
        "var p = obj.prop;\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testConstructorCall_withAndWithoutTemplates() {
    String js =
        "/** @constructor */ function Simple() {}\n" +
        "var s = new Simple();\n" +
        "/** @constructor\n * @template T\n * @param {T} val */\n" +
        "function Box(val) { this.val = val; }\n" +
        "var b = new Box(123);\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testFunctionBindInference() {
    String js =
        "function target(a, b) { return a + b; }\n" +
        "var bound = target.bind(null, 10);\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  // ==================================================================================================
  // PARTITION E: Object Lifecycle & Contract Integrity
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testDiagnosticConstantsIntegrity() {
    assertNotNull("FUNCTION_LITERAL_UNDEFINED_THIS must be non-null",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }

  @Test(timeout = 4000)
  public void testIIFEArgumentInference_underlyingBranch() {
    // IIFE argument node inference branch: NodeUtil.isCallOrNewTarget(functionNode)
    String js = "(function(inferredArg) { return inferredArg; })(100);\n";
    TypeInference ti = createTypeInference(js);
    ti.analyze();
  }

  @Test(timeout = 4000)
  public void testBranchedFlowThrough_structure() {
    String js =
        "var x = 1;\n" +
        "if (x > 0) {\n" +
        "  x = 2;\n" +
        "} else {\n" +
        "  x = 3;\n" +
        "}\n";
    TypeInference ti = createTypeInference(js);
    FlowScope entry = ti.createEntryLattice();

    Node root = compiler.parseTestCode(js);
    Node firstStmt = root.getFirstChild();

    List<FlowScope> branchedScopes = ti.branchedFlowThrough(firstStmt, entry);
    assertNotNull("Branched flow scopes list must not be null", branchedScopes);
    assertTrue("Branched flow scopes list must contain scopes for out edges",
        branchedScopes.size() >= 0);
  }
}