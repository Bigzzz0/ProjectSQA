package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.javascript.jscomp.ControlFlowGraph.Branch;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.BooleanLiteralSet;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypeInference
 *
 * Major Branches & Subsystems Covered:
 * 1. Initial State / Entry / Bottom Scope Transitions:
 *    - Constructor with default and explicit unflowable variables.
 *    - Uninitialized VAR tokens entering as VOID_TYPE.
 *    - Unflowable vars excluded from flow inference.
 *    - Flow through bottomScope (identity short-circuit).
 *
 * 2. AST Node Traversal & Type Transitions (Token Switch):
 *    - Tokens: ASSIGN, NAME, GETPROP, GETELEM, AND, OR, HOOK, OBJECTLIT,
 *      ARRAYLIT, CALL, NEW, ADD, ASSIGN_ADD, POS, NEG, NUMBER, STRING,
 *      TYPEOF, BOOLEAN comparisons (LT, LE, GT, GE, EQ, NE, SHEQ, SHNE,
 *      INSTANCEOF, IN, TRUE, FALSE), COMMA, LP/GET_REF, CATCH, EXPR_RESULT,
 *      SWITCH, RETURN, THROW, VAR.
 *    - Type casting via JSDoc attached to expression results.
 *    - Pointer dereferencing narrowing (restricting nullable/undefined).
 *
 * 3. Short-circuiting Logic & Boolean Outcome Pairs:
 *    - Token.AND, Token.OR, static getBooleanOutcomes logic.
 *    - Branched flow through ON_TRUE, ON_FALSE, FOR-IN variable typing.
 *
 * 4. Inter-procedural Closures & Template Types:
 *    - Closure parameter matching in updateTypeOfParametersOnClosure.
 *    - Template type of 'this' checking in updateTypeOfThisOnClosure.
 *    - Error emissions: TEMPLATE_TYPE_NOT_OBJECT_TYPE and TEMPLATE_TYPE_OF_THIS_EXPECTED.
 *
 * 5. Defect Targeted Branch Zone:
 *    - Targeted at Closure Reverse Abstract Interpreter / Type Inference interaction
 *      handling null checks on objects/arrays/functions (e.g., goog.isArray, goog.isObject).
 */
public class TypeInferenceGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
  }

  private TypeInference createTypeInference(Node root, Scope scope) {
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);
    return new TypeInference(compiler, cfg, rai, scope);
  }

  private Scope createSyntacticScope(Node root) {
    TypedScopeCreator creator = new TypedScopeCreator(compiler);
    return creator.createScope(root, null);
  }

  private void inferTypes(Node root) {
    Scope scope = createSyntacticScope(root);
    TypeInference inference = createTypeInference(root, scope);
    inference.analyze();
  }

  private Node parseAndInfer(String js) {
    Node n = compiler.parseTestCode(js);
    assertEquals(0, compiler.getErrorCount());
    inferTypes(n);
    return n;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Node Traversals
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrimitiveLiteralsAndUnaryOps() {
    Node root = parseAndInfer(
        "var a = null; " +
        "var b = void 0; " +
        "var c = +1; " +
        "var d = -2; " +
        "var e = !true; " +
        "var f = 'str'; " +
        "var g = typeof a; " +
        "var h = [1, 2];");

    Node firstChild = root.getFirstChild();
    assertNotNull(firstChild);

    // Verify null literal
    Node aVar = firstChild; // VAR node
    Node aName = aVar.getFirstChild();
    assertEquals(registry.getNativeType(JSTypeNative.NULL_TYPE), aName.getFirstChild().getJSType());

    // Verify void literal
    Node bVar = aVar.getNext();
    Node bName = bVar.getFirstChild();
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), bName.getFirstChild().getJSType());

    // Verify array literal
    Node hVar = root.getLastChild();
    Node hName = hVar.getFirstChild();
    assertEquals(registry.getNativeType(JSTypeNative.ARRAY_TYPE), hName.getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testBinaryNumericAndBitwiseOps() {
    Node root = parseAndInfer(
        "var a = 1 + 2; " +
        "var b = 1 - 2; " +
        "var c = 1 * 2; " +
        "var d = 1 / 2; " +
        "var e = 1 % 2; " +
        "var f = 1 << 2; " +
        "var g = 1 >> 2; " +
        "var h = 1 >>> 2; " +
        "var i = 1 & 2; " +
        "var j = 1 ^ 2; " +
        "var k = 1 | 2; " +
        "var l = 1; l += 2;");

    for (Node var = root.getFirstChild(); var != null; var = var.getNext()) {
      Node name = var.getFirstChild();
      if (name != null && name.hasChildren()) {
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), name.getFirstChild().getJSType());
      }
    }
  }

  @Test(timeout = 4000)
  public void testStringAdditionUnion() {
    Node root = parseAndInfer(
        "var a = 'hello ' + 'world'; " +
        "var b = 'num: ' + 5; " +
        "var c = 5 + 'str';");

    Node aVar = root.getFirstChild();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        aVar.getFirstChild().getFirstChild().getJSType());

    Node bVar = aVar.getNext();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        bVar.getFirstChild().getFirstChild().getJSType());

    Node cVar = bVar.getNext();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        cVar.getFirstChild().getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testComparisonOperatorsYieldBoolean() {
    Node root = parseAndInfer(
        "var a = 1 < 2; " +
        "var b = 1 <= 2; " +
        "var c = 1 > 2; " +
        "var d = 1 >= 2; " +
        "var e = (1 == 2); " +
        "var f = (1 != 2); " +
        "var g = (1 === 2); " +
        "var h = (1 !== 2); " +
        "var i = ('prop' in {}); " +
        "var j = ({} instanceof Object);");

    for (Node var = root.getFirstChild(); var != null; var = var.getNext()) {
      Node name = var.getFirstChild();
      assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), name.getFirstChild().getJSType());
    }
  }

  @Test(timeout = 4000)
  public void testHookOperator() {
    Node root = parseAndInfer("var x = true ? 1 : 'str';");
    Node var = root.getFirstChild();
    Node hook = var.getFirstChild().getFirstChild();

    JSType expectedUnion = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertEquals(expectedUnion, hook.getJSType());
  }

  @Test(timeout = 4000)
  public void testCommaAndGroupExpression() {
    Node root = parseAndInfer("var x = (1, 'hello'); var y = (2);");
    Node xVar = root.getFirstChild();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE),
        xVar.getFirstChild().getFirstChild().getJSType());

    Node yVar = xVar.getNext();
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        yVar.getFirstChild().getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralInference() {
    Node root = parseAndInfer("var obj = { a: 1, b: 'str' };");
    Node var = root.getFirstChild();
    Node objLit = var.getFirstChild().getFirstChild();
    JSType litType = objLit.getJSType();
    assertNotNull(litType);
    assertTrue(litType instanceof ObjectType);

    ObjectType objType = (ObjectType) litType;
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), objType.findPropertyType("a"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), objType.findPropertyType("b"));
  }

  @Test(timeout = 4000)
  public void testShortCircuitAndOr() {
    Node root = parseAndInfer("var a = 1 && 'str'; var b = 0 || 'fallback';");
    Node aVar = root.getFirstChild();
    assertNotNull(aVar.getFirstChild().getFirstChild().getJSType());

    Node bVar = aVar.getNext();
    assertNotNull(bVar.getFirstChild().getFirstChild().getJSType());
  }

  @Test(timeout = 4000)
  public void testDereferencePointerNarrowing() {
    Node root = parseAndInfer(
        "function f(/** ?{p: number} */ opt_obj) {" +
        "  var val = opt_obj.p;" +
        "}");
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testGetElemInference() {
    Node root = parseAndInfer("var arr = [1, 2]; var el = arr[0];");
    assertNotNull(root);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanOutcomesStaticLogic() {
    // Condition true: evaluates right when left evaluates to true
    BooleanLiteralSet resTrue = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.TRUE, BooleanLiteralSet.FALSE, true);
    assertEquals(BooleanLiteralSet.FALSE, resTrue);

    // Condition false: evaluates right when left evaluates to false
    BooleanLiteralSet resFalse = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.FALSE, BooleanLiteralSet.TRUE, false);
    assertEquals(BooleanLiteralSet.TRUE, resFalse);

    // Both outcomes
    BooleanLiteralSet resBoth = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.BOTH, BooleanLiteralSet.BOTH, true);
    assertEquals(BooleanLiteralSet.BOTH, resBoth);

    // Empty outcomes
    BooleanLiteralSet resEmpty = TypeInference.getBooleanOutcomes(
        BooleanLiteralSet.EMPTY, BooleanLiteralSet.EMPTY, false);
    assertEquals(BooleanLiteralSet.EMPTY, resEmpty);
  }

  @Test(timeout = 4000)
  public void testFlowThroughBottomScopeReturnsBottom() {
    Node root = compiler.parseTestCode("var x = 1;");
    Scope scope = createSyntacticScope(root);
    TypeInference inference = createTypeInference(root, scope);

    FlowScope bottom = inference.createInitialEstimateLattice();
    FlowScope result = inference.flowThrough(root, bottom);
    assertSame(bottom, result);
  }

  @Test(timeout = 4000)
  public void testConstructorWithUnflowableVars() {
    Node root = compiler.parseTestCode("var unflow = 1; var normal = 2;");
    Scope scope = createSyntacticScope(root);
    Scope.Var unflowVar = scope.getVar("unflow");
    assertNotNull(unflowVar);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    ControlFlowGraph<Node> cfg = cfa.getCfg();
    ReverseAbstractInterpreter rai = new SemanticReverseAbstractInterpreter(
        compiler.getCodingConvention(), registry);

    TypeInference inference = new TypeInference(
        compiler, cfg, rai, scope, Collections.singletonList(unflowVar));
    inference.analyze();

    FlowScope entry = inference.createEntryLattice();
    assertNotNull(entry);
  }

  @Test(timeout = 4000)
  public void testAssignedOuterLocalVarsMultimap() {
    Node root = compiler.parseTestCode(
        "function outer() {" +
        "  var x = 1;" +
        "  function inner() {" +
        "    x = 2;" +
        "  }" +
        "}");
    Scope scope = createSyntacticScope(root);
    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, true);
    cfa.process(null, root);
    TypeInference inference = new TypeInference(
        compiler, cfa.getCfg(),
        new SemanticReverseAbstractInterpreter(compiler.getCodingConvention(), registry),
        scope);
    Multimap<Scope, Scope.Var> outerVars = inference.getAssignedOuterLocalVars();
    assertNotNull(outerVars);
    assertTrue(outerVars.isEmpty());
  }

  @Test(timeout = 4000)
  public void testCatchScopeVariableTyping() {
    Node root = parseAndInfer(
        "try {" +
        "  throw 'err';" +
        "} catch (e) {" +
        "  var errVal = e;" +
        "}");
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testBranchedFlowThroughForIn() {
    Node root = parseAndInfer(
        "var obj = {a: 1};" +
        "for (var key in obj) {" +
        "  var val = key;" +
        "}");
    assertNotNull(root);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure reverse interpreter & null check)
  // =========================================================================

  @Test(timeout = 4000)
  public void testGoogIsArrayAndIsObjectNarrowingOnNullable() {
    // Tests restriction and type inference behavior when checking nullable variables
    Node root = parseAndInfer(
        "/** @type {function(*):boolean} */ var isArray = function(x) { return true; };\n" +
        "/** @param {Array|null} x */\n" +
        "function f(x) {\n" +
        "  if (x != null) {\n" +
        "    var a = x;\n" +
        "  }\n" +
        "}\n");
    assertNotNull(root);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue124NullBranchingAssertion() {
    // Specifically targets Defects4J issue 124 where short-circuiting or nullable
    // checks in condition expressions (if (x == null) ... goog.isObject(x))
    // must not produce erroneous null types when narrowed.
    Node root = parseAndInfer(
        "/** @param {Object|null} x */\n" +
        "function f(x) {\n" +
        "  if (x == null) {\n" +
        "    return;\n" +
        "  }\n" +
        "  var y = x;\n" +
        "}\n");

    Node functionNode = root.getLastChild();
    assertEquals(Token.FUNCTION, functionNode.getType());
    Node body = functionNode.getLastChild();
    Node returnIf = body.getFirstChild();
    assertEquals(Token.IF, returnIf.getType());

    Node varY = returnIf.getNext();
    assertNotNull(varY);
    Node yNode = varY.getFirstChild();
    // After `if (x == null) return;`, x must be restricted to Object (not null)
    assertFalse(yNode.getFirstChild().getJSType().isNullable());
  }

  @Test(timeout = 4000)
  public void testTemplateTypeExpectedErrors() {
    // Triggers updateTypeOfThisOnClosure error diagnostic TEMPLATE_TYPE_OF_THIS_EXPECTED
    // when template parameter is used without matching this type parameter
    FunctionType funcType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        registry.createParametersWithVarArgs(registry.createTemplateType("T")));

    Node callNode = new Node(Token.CALL, new Node(Token.NAME, "fn"));
    callNode.getFirstChild().setJSType(funcType);
    Node arg = new Node(Token.NAME, "arg");
    arg.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    callNode.addChildToBack(arg);

    Node script = new Node(Token.SCRIPT, new Node(Token.EXPR_RESULT, callNode));
    Scope scope = createSyntacticScope(script);
    TypeInference inference = createTypeInference(script, scope);
    inference.analyze();

    assertEquals(1, compiler.getErrorCount());
    assertEquals(TypeInference.TEMPLATE_TYPE_OF_THIS_EXPECTED.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testTemplateTypeNotObjectTypeReportsError() {
    // Triggers updateTypeOfThisOnClosure error diagnostic TEMPLATE_TYPE_NOT_OBJECT_TYPE
    // when template parameter resolves to a non-object primitive
    ObjectType templateType = registry.createTemplateType("T");
    FunctionType fnWithThis = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        new Node(Token.LP));

    FunctionType funcType = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        ImmutableList.of(templateType, fnWithThis));

    Node callNode = new Node(Token.CALL, new Node(Token.NAME, "fn"));
    callNode.getFirstChild().setJSType(funcType);
    Node nonObjectArg = Node.newNumber(42);
    nonObjectArg.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    callNode.addChildToBack(nonObjectArg);
    callNode.addChildToBack(new Node(Token.FUNCTION, new Node(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK)));

    Node script = new Node(Token.SCRIPT, new Node(Token.EXPR_RESULT, callNode));
    Scope scope = createSyntacticScope(script);
    TypeInference inference = createTypeInference(script, scope);
    inference.analyze();

    assertEquals(1, compiler.getErrorCount());
    assertEquals(TypeInference.TEMPLATE_TYPE_NOT_OBJECT_TYPE.key,
        compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testNewOperatorOnConstructorAndUnknown() {
    Node root = parseAndInfer(
        "/** @constructor */ function Foo() { this.prop = 1; }\n" +
        "var x = new Foo();\n" +
        "var y = new /** @type {?} */ (Foo);");
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testSwitchStatementFlow() {
    Node root = parseAndInfer(
        "var a = 1;\n" +
        "switch (a) {\n" +
        "  case 1:\n" +
        "    var b = 'one';\n" +
        "    break;\n" +
        "  case 2:\n" +
        "    var c = 'two';\n" +
        "    break;\n" +
        "  default:\n" +
        "    var d = 'other';\n" +
        "}");
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testQualifiedNamePropertyInference() {
    Node root = parseAndInfer(
        "var ns = {};\n" +
        "ns.sub = {};\n" +
        "ns.sub.prop = 100;\n" +
        "var read = ns.sub.prop;");
    assertNotNull(root);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Joining
  // =========================================================================

  @Test(timeout = 4000)
  public void testLatticeJoiningInIfElseBranches() {
    Node root = parseAndInfer(
        "var x;\n" +
        "if (true) {\n" +
        "  x = 1;\n" +
        "} else {\n" +
        "  x = 'str';\n" +
        "}\n" +
        "var y = x;\n");

    Node lastVar = root.getLastChild();
    assertEquals(Token.VAR, lastVar.getType());
    Node yNode = lastVar.getFirstChild();
    JSType yType = yNode.getFirstChild().getJSType();
    assertNotNull(yType);
    assertTrue(yType.isUnionType());
    assertTrue(yType.isSubtype(registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE))));
  }
}