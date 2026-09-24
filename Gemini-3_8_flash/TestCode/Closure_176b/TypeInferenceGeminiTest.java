package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;
import com.google.javascript.jscomp.CodingConvention.AssertionFunctionSpec;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.jscomp.graph.DiGraph.DiGraphEdge;
import com.google.javascript.jscomp.type.FlowScope;
import com.google.javascript.jscomp.type.ReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: com.google.javascript.jscomp.TypeInference
 * Known Defect: Defects4J / Closure Issue 1056 (testIssue1056).
 * Root Cause:
 *   In traverseNew(Node n, FlowScope scope), traverseChildren(n, scope) was called
 *   before backwardsInferenceFromCallSite(n, ct). This caused constructor arguments
 *   (e.g., an empty object literal variable) to be traversed and have their types
 *   widened/mutated via inferPropertyTypesToMatchConstraint before TypeCheck could
 *   detect mismatched parameter constraints. Consequently, no warning was emitted.
 *
 * Targeted Decision Branches:
 * - traverseNew():
 *     * ct != null && ct.isConstructor()
 *     * constructor is unknown / non-constructor / NoObjectType
 *     * backwards type inference ordering on constructor arguments
 * - branchedFlowThrough():
 *     * input == bottomScope (fast return)
 *     * Branch.ON_TRUE / for-in loops (item.isVar(), item.isName(), obj index types)
 *     * Branch.ON_FALSE / conditions (AND, OR, case expressions)
 * - traverse():
 *     * Tokens: ASSIGN, NAME, GETPROP, AND, OR, HOOK, OBJECTLIT, CALL, NEW,
 *               ASSIGN_ADD, ADD, POS, NEG, ARRAYLIT, THIS, Bitwise/Arithmetic,
 *               PARAM_LIST, COMMA, TYPEOF, Comparisons, GETELEM, EXPR_RESULT,
 *               SWITCH, RETURN, VAR, THROW, CATCH, CAST.
 * - tightenTypesAfterAssertions():
 *     * assertionFunctionSpec != null with null assertedType (truthy expression)
 *     * assertionFunctionSpec != null with non-null assertedType (type narrowing)
 * - getBooleanOutcomes():
 *     * Truth-table testing of BooleanLiteralSet combinations under condition=true/false
 */
public class TypeInferenceGeminiTest {

  private static class InferenceContext {
    final Compiler compiler;
    final Node root;
    final Scope scope;
    final ControlFlowGraph<Node> cfg;
    final TypeInference typeInference;

    InferenceContext(Compiler compiler, Node root, Scope scope,
                     ControlFlowGraph<Node> cfg, TypeInference typeInference) {
      this.compiler = compiler;
      this.root = root;
      this.scope = scope;
      this.cfg = cfg;
      this.typeInference = typeInference;
    }
  }

  private InferenceContext setupInference(String js, Map<String, AssertionFunctionSpec> assertions) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);
    compiler.initOptions(options);

    Node script = compiler.parseTestCode(js);
    TypedScopeCreator scopeCreator = new TypedScopeCreator(compiler);
    Scope scope = scopeCreator.createScope(script, null);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, script);
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    ReverseAbstractInterpreter rai = compiler.getReverseAbstractInterpreterForCheck();
    if (assertions == null) {
      assertions = Collections.emptyMap();
    }

    TypeInference ti = new TypeInference(compiler, cfg, rai, scope, assertions);
    return new InferenceContext(compiler, script, scope, cfg, ti);
  }

  private InferenceContext setupInference(String js) {
    return setupInference(js, Collections.<String, AssertionFunctionSpec>emptyMap());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 1056 / Closure 25)
  // =========================================================================

  /**
   * Targets Closure Issue 1056.
   * Passing an empty object variable to a constructor expecting an object with
   * specific properties must produce a type mismatch warning. In the defective
   * code, traverseNew executes traverseChildren before backwards inference,
   * modifying the object type's constraints prematurely and suppressing the warning.
   */
  @Test(timeout = 4000)
  public void testIssue1056() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCheckTypes(true);

    SourceFile externs = SourceFile.fromCode("externs.js",
        "var undefined;\n" +
        "/** @constructor */ function Object() {}\n" +
        "/** @constructor */ function Function() {}\n");

    SourceFile input = SourceFile.fromCode("test.js",
        "var obj = {};\n" +
        "/** @constructor\n" +
        " *  @param {{foo: string}} x */\n" +
        "function F(x) {}\n" +
        "new F(obj);\n");

    compiler.compile(Collections.singletonList(externs), Collections.singletonList(input), options);
    assertTrue("expected a warning for parameter mismatch in testIssue1056",
        compiler.getWarnings().length > 0);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testLatticeInitializationAndBottomShortCircuit() {
    InferenceContext ctx = setupInference("var a = 1;");
    FlowScope entryLattice = ctx.typeInference.createEntryLattice();
    FlowScope bottomLattice = ctx.typeInference.createInitialEstimateLattice();

    assertNotNull(entryLattice);
    assertNotNull(bottomLattice);
    assertNotSame(entryLattice, bottomLattice);

    // flowThrough with bottomScope should immediately return bottomScope unchanged
    Node firstChild = ctx.root.getFirstChild();
    FlowScope resultBottom = ctx.typeInference.flowThrough(firstChild, bottomLattice);
    assertSame(bottomLattice, resultBottom);

    // flowThrough with entryScope should yield a new child scope
    FlowScope resultNormal = ctx.typeInference.flowThrough(firstChild, entryLattice);
    assertNotNull(resultNormal);
    assertNotSame(entryLattice, resultNormal);
  }

  @Test(timeout = 4000)
  public void testVariableAssignmentAndLookup() {
    InferenceContext ctx = setupInference("var x = 42; var y = x;");
    ctx.typeInference.analyze();

    Node varX = ctx.root.getFirstChild();
    Node nameX = varX.getFirstChild();
    assertNotNull(nameX.getJSType());
    assertTrue(nameX.getJSType().isNumberValueType());

    Node varY = varX.getNext();
    Node nameY = varY.getFirstChild();
    assertNotNull(nameY.getJSType());
    assertTrue(nameY.getJSType().isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testTraverseAddOperatorBranches() {
    // 1. String concatenation
    InferenceContext ctx1 = setupInference("var a = 'hello' + 'world';");
    ctx1.typeInference.analyze();
    Node nameA = ctx1.root.getFirstChild().getFirstChild();
    assertTrue(nameA.getJSType().isString());

    // 2. Numeric addition
    InferenceContext ctx2 = setupInference("var b = 1 + 2;");
    ctx2.typeInference.analyze();
    Node nameB = ctx2.root.getFirstChild().getFirstChild();
    assertTrue(nameB.getJSType().isNumberValueType());

    // 3. String + Number
    InferenceContext ctx3 = setupInference("var c = 'count: ' + 5;");
    ctx3.typeInference.analyze();
    Node nameC = ctx3.root.getFirstChild().getFirstChild();
    assertTrue(nameC.getJSType().isString());

    // 4. Assign add
    InferenceContext ctx4 = setupInference("var d = 10; d += 5;");
    ctx4.typeInference.analyze();
    Node assignAdd = ctx4.root.getLastChild().getFirstChild();
    assertEquals(Token.ASSIGN_ADD, assignAdd.getType());
    assertTrue(assignAdd.getJSType().isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testTraverseHookTernary() {
    InferenceContext ctx = setupInference("var res = true ? 'yes' : 0;");
    ctx.typeInference.analyze();
    Node resNode = ctx.root.getFirstChild().getFirstChild();
    JSType resType = resNode.getJSType();
    assertNotNull(resType);
    assertTrue(resType.isUnionType());
    assertTrue(resType.isSubtype(ctx.compiler.getTypeRegistry().createUnionType(
        JSTypeNative.STRING_TYPE, JSTypeNative.NUMBER_TYPE)));
  }

  @Test(timeout = 4000)
  public void testShortCircuitAndOrOperators() {
    InferenceContext ctx = setupInference("var a = true && 'str'; var b = false || 123;");
    ctx.typeInference.analyze();

    Node varA = ctx.root.getFirstChild();
    Node nameA = varA.getFirstChild();
    assertNotNull(nameA.getJSType());

    Node varB = varA.getNext();
    Node nameB = varB.getFirstChild();
    assertNotNull(nameB.getJSType());
  }

  @Test(timeout = 4000)
  public void testTraverseObjectLiteralAndProperties() {
    InferenceContext ctx = setupInference("var obj = { a: 'foo', b: 10 }; var propA = obj.a;");
    ctx.typeInference.analyze();

    Node varObj = ctx.root.getFirstChild();
    Node objLit = varObj.getFirstChild().getFirstChild();
    assertTrue(objLit.getJSType().isObjectType());

    Node varPropA = varObj.getNext();
    Node propAName = varPropA.getFirstChild();
    assertTrue(propAName.getJSType().isString());
  }

  @Test(timeout = 4000)
  public void testTraverseArrayLiteralAndGetElem() {
    InferenceContext ctx = setupInference("var arr = [1, 2, 3]; var elem = arr[0];");
    ctx.typeInference.analyze();

    Node varArr = ctx.root.getFirstChild();
    Node arrLit = varArr.getFirstChild().getFirstChild();
    assertTrue(arrLit.getJSType().isArrayType());

    Node varElem = varArr.getNext();
    assertNotNull(varElem.getFirstChild().getJSType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanOutcomesTruthTable() {
    // Condition = true: right.union(left.intersection(BooleanLiteralSet.FALSE))
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.TRUE, true));
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.EMPTY, true));
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.EMPTY, true));

    // Condition = false: right.union(left.intersection(BooleanLiteralSet.TRUE))
    assertEquals(BooleanLiteralSet.FALSE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.FALSE, false));
    assertEquals(BooleanLiteralSet.BOTH,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.BOTH, BooleanLiteralSet.FALSE, false));
    assertEquals(BooleanLiteralSet.TRUE,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.TRUE, BooleanLiteralSet.EMPTY, false));
    assertEquals(BooleanLiteralSet.EMPTY,
        TypeInference.getBooleanOutcomes(BooleanLiteralSet.FALSE, BooleanLiteralSet.EMPTY, false));
  }

  @Test(timeout = 4000)
  public void testUnaryAndBitwiseOperators() {
    String js =
        "var x = 10;\n" +
        "var p = +x; var neg = -x;\n" +
        "var not = ~x; var inc = ++x; var dec = --x;\n" +
        "var lsh = x << 1; var rsh = x >> 1; var ursh = x >>> 1;\n" +
        "var mul = x * 2; var div = x / 2; var mod = x % 2;\n" +
        "var band = x & 1; var bxor = x ^ 1; var bor = x | 1;\n" +
        "var t = typeof x;\n" +
        "var cmp = x < 5;\n";
    InferenceContext ctx = setupInference(js);
    ctx.typeInference.analyze();

    Node curr = ctx.root.getFirstChild().getNext(); // skip var x
    // p, neg, not, inc, dec, lsh, rsh, ursh, mul, div, mod, band, bxor, bor -> all numbers
    for (int i = 0; i < 14; i++) {
      Node nameNode = curr.getFirstChild();
      assertTrue("Operator at index " + i + " must produce NUMBER",
          nameNode.getJSType().isNumberValueType());
      curr = curr.getNext();
    }
    // typeof x -> string
    assertTrue(curr.getFirstChild().getJSType().isString());
    curr = curr.getNext();
    // x < 5 -> boolean
    assertTrue(curr.getFirstChild().getJSType().isBooleanValueType());
  }

  @Test(timeout = 4000)
  public void testCatchBlockWithoutAndWithJSDoc() {
    String js =
        "try {} catch (e) { var x = e; }\n" +
        "try {} catch (/** @type {string} */ err) { var y = err; }\n";
    InferenceContext ctx = setupInference(js);
    ctx.typeInference.analyze();

    Node try1 = ctx.root.getFirstChild();
    Node catch1 = try1.getLastChild().getFirstChild();
    Node catchName1 = catch1.getFirstChild();
    assertTrue(catchName1.getJSType().isUnknownType());

    Node try2 = try1.getNext();
    Node catch2 = try2.getLastChild().getFirstChild();
    Node catchName2 = catch2.getFirstChild();
    assertTrue(catchName2.getJSType().isString());
  }

  // =========================================================================
  // Partition D: Assertion Functions & Type Tightening
  // =========================================================================

  @Test(timeout = 4000)
  public void testTightenTypesWithAssertionFunctions() {
    Map<String, AssertionFunctionSpec> assertions = Maps.newHashMap();
    assertions.put("assert", new AssertionFunctionSpec("assert"));
    assertions.put("assertString",
        new AssertionFunctionSpec("assertString", JSTypeNative.STRING_TYPE));

    String js =
        "function assert(x) {}\n" +
        "function assertString(x) {}\n" +
        "var /** (string|null) */ a = null;\n" +
        "assert(a);\n" +
        "var /** (string|number) */ b = 'init';\n" +
        "assertString(b);\n";

    InferenceContext ctx = setupInference(js, assertions);
    ctx.typeInference.analyze();

    // Verify calls tighten the types appropriately
    Node script = ctx.root;
    Node callAssert = script.getChildAtIndex(3).getFirstChild();
    assertEquals(Token.CALL, callAssert.getType());
    assertNotNull(callAssert.getJSType());

    Node callAssertString = script.getChildAtIndex(5).getFirstChild();
    assertEquals(Token.CALL, callAssertString.getType());
    assertEquals(ctx.compiler.getTypeRegistry().getNativeType(JSTypeNative.STRING_TYPE),
        callAssertString.getJSType());
  }

  // =========================================================================
  // Partition E: Branch Flow, IIFE & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testBranchedFlowThroughForInLoop() {
    String js =
        "var obj = { a: 1, b: 2 };\n" +
        "for (var key in obj) {\n" +
        "  var val = key;\n" +
        "}\n";
    InferenceContext ctx = setupInference(js);
    ctx.typeInference.analyze();

    Node forNode = ctx.root.getLastChild();
    assertEquals(Token.FOR, forNode.getType());

    List<FlowScope> branched = ctx.typeInference.branchedFlowThrough(
        forNode, ctx.typeInference.createEntryLattice());
    assertNotNull(branched);
    assertFalse(branched.isEmpty());
  }

  @Test(timeout = 4000)
  public void testIifeArgumentInference() {
    String js = "(function(param) { return param; })(100);";
    InferenceContext ctx = setupInference(js);
    ctx.typeInference.analyze();

    Node expr = ctx.root.getFirstChild();
    Node call = expr.getFirstChild();
    Node fn = call.getFirstChild();
    Node paramList = fn.getFirstChild().getNext();
    Node param = paramList.getFirstChild();

    assertNotNull(param.getJSType());
    assertTrue(param.getJSType().isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testNewTargetOnNonConstructor() {
    String js = "var notFn = 42; var instance = new notFn();";
    InferenceContext ctx = setupInference(js);
    ctx.typeInference.analyze();

    Node varInstance = ctx.root.getLastChild();
    Node newTarget = varInstance.getFirstChild().getFirstChild();
    assertEquals(Token.NEW, newTarget.getType());
    // Since notFn is a number, instance type should be null or unknown
    JSType instanceType = newTarget.getJSType();
    assertTrue(instanceType == null || instanceType.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDiagnosticConstantContract() {
    assertNotNull(TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS);
    assertEquals("JSC_FUNCTION_LITERAL_UNDEFINED_THIS",
        TypeInference.FUNCTION_LITERAL_UNDEFINED_THIS.key);
  }
}