/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.AnalyzePrototypeProperties
 * Target Defect: RemoveUnusedPrototypePropertiesTest::testAliasing7 (Defects4J Closure)
 *
 * Core Decision Branches & Conditions Targeted:
 * 1. ProcessProperties.visit(NodeTraversal, Node, Node):
 *    - Node.GETPROP: propName.equals("prototype") -> processPrototypeParent vs isExported vs addSymbolUse.
 *    - Node.OBJECTLIT: parent is ASSIGN to prototype vs general object literal (propNameNode.isQuotedString() check).
 *    - Node.NAME: global var pointing to FUNCTION vs non-global accessing outer scope (readClosureVariables branch).
 *    - SymbolStack operations: push/pop for prototype assign, global function, anonymous functions.
 * 2. ProcessProperties.isPrototypePropertyAssign(Node):
 *    - LHS is GETPROP with child GETPROP whose second child is "prototype" (chained prototype check).
 * 3. ProcessProperties.processGlobalFunctionDeclaration(NodeTraversal, Node, Node, Node):
 *    - Named FUNCTION vs VAR with FUNCTION initial value; anchorUnusedVars condition.
 * 4. ProcessExternProperties.visit(NodeTraversal, Node, Node):
 *    - canModifyExterns flag: true skips extern traversal; false connects externNode to properties.
 * 5. PropagateReferences.traverseEdge(NameInfo, JSModule, NameInfo):
 *    - Deepest common module reference resolution across JSModuleGraph dependencies.
 * 6. Symbol AST Manipulations:
 *    - GlobalFunction.remove(): parent is FUNCTION vs single-child VAR vs multi-child VAR.
 *    - AssignmentProperty: remove(), getPrototype(), getValue(), getModule().
 *    - LiteralProperty: remove(), getPrototype(), getValue(), getModule().
 * 7. Known Defect Zone (testAliasing7):
 *    - Aliasing prototype properties through object literals, reassignment of prototype,
 *      or indirect property references where declarations vs symbol uses must be tracked accurately.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AnalyzePrototypePropertiesGeminiTest {

  private AnalyzePrototypeProperties createApp(
      Compiler compiler, JSModuleGraph graph, boolean canModifyExterns, boolean anchorUnusedVars) {
    return new AnalyzePrototypeProperties(compiler, graph, canModifyExterns, anchorUnusedVars);
  }

  private Map<String, AnalyzePrototypeProperties.NameInfo> toNameInfoMap(
      Collection<AnalyzePrototypeProperties.NameInfo> infos) {
    Map<String, AnalyzePrototypeProperties.NameInfo> map = new HashMap<String, AnalyzePrototypeProperties.NameInfo>();
    for (AnalyzePrototypeProperties.NameInfo info : infos) {
      map.put(info.name, info);
    }
    return map;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrototypePropertyAssignmentAndUse() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 42; };\n"
        + "var f = new Foo();\n"
        + "f.bar();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue("Property 'bar' should be recorded", map.containsKey("bar"));
    AnalyzePrototypeProperties.NameInfo barInfo = map.get("bar");
    assertTrue("Property 'bar' should be marked referenced", barInfo.isReferenced());
    assertEquals("Should have 1 declaration for 'bar'", 1, barInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol symbol = barInfo.getDeclarations().getFirst();
    assertTrue(symbol instanceof AnalyzePrototypeProperties.AssignmentProperty);
    AnalyzePrototypeProperties.AssignmentProperty assignProp = (AnalyzePrototypeProperties.AssignmentProperty) symbol;
    assertNotNull(assignProp.getPrototype());
    assertNotNull(assignProp.getValue());
    assertNull(assignProp.getModule());
  }

  @Test(timeout = 4000)
  public void testPrototypeLiteralPropertyAssignmentAndUse() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype = { methodA: function() { return 1; }, propB: 2 };\n"
        + "var f = new Foo();\n"
        + "f.methodA();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue(map.containsKey("methodA"));
    assertTrue(map.containsKey("propB"));

    AnalyzePrototypeProperties.NameInfo aInfo = map.get("methodA");
    AnalyzePrototypeProperties.NameInfo bInfo = map.get("propB");

    assertTrue("methodA is called, so it should be referenced", aInfo.isReferenced());
    assertFalse("propB is never accessed, so it should not be referenced", bInfo.isReferenced());

    assertEquals(1, aInfo.getDeclarations().size());
    AnalyzePrototypeProperties.Symbol sym = aInfo.getDeclarations().getFirst();
    assertTrue(sym instanceof AnalyzePrototypeProperties.LiteralProperty);
    AnalyzePrototypeProperties.LiteralProperty litProp = (AnalyzePrototypeProperties.LiteralProperty) sym;
    assertNotNull(litProp.getPrototype());
    assertNotNull(litProp.getValue());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionDeclarationAndUse() {
    Compiler compiler = new Compiler();
    String js = "function myGlobalFunc() { return 10; }\n"
        + "function caller() { myGlobalFunc(); }\n"
        + "caller();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue(map.containsKey("myGlobalFunc"));
    assertTrue(map.containsKey("caller"));

    assertTrue(map.get("caller").isReferenced());
    assertTrue(map.get("myGlobalFunc").isReferenced());
  }

  @Test(timeout = 4000)
  public void testVarFunctionDeclarationAndAnchorUnusedVars() {
    Compiler compiler = new Compiler();
    String js = "var unusedFunc = function() { return 1; };\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties appUnanchored = createApp(compiler, null, false, false);
    appUnanchored.process(externs, root);
    Map<String, AnalyzePrototypeProperties.NameInfo> mapUnanchored =
        toNameInfoMap(appUnanchored.getAllNameInfo());
    assertTrue(mapUnanchored.containsKey("unusedFunc"));
    assertFalse("Unused var function should NOT be referenced when anchorUnusedVars=false",
        mapUnanchored.get("unusedFunc").isReferenced());

    Compiler compiler2 = new Compiler();
    Node root2 = compiler2.parseTestCode(js);
    Node externs2 = compiler2.parseTestCode("");
    AnalyzePrototypeProperties appAnchored = createApp(compiler2, null, false, true);
    appAnchored.process(externs2, root2);
    Map<String, AnalyzePrototypeProperties.NameInfo> mapAnchored =
        toNameInfoMap(appAnchored.getAllNameInfo());
    assertTrue(mapAnchored.containsKey("unusedFunc"));
    assertTrue("Unused var function MUST be referenced when anchorUnusedVars=true",
        mapAnchored.get("unusedFunc").isReferenced());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testImplicitlyUsedProperties() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1;");
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue("length must be pre-created", map.containsKey("length"));
    assertTrue("toString must be pre-created", map.containsKey("toString"));
    assertTrue("valueOf must be pre-created", map.containsKey("valueOf"));

    assertTrue(map.get("length").isReferenced());
    assertTrue(map.get("toString").isReferenced());
    assertTrue(map.get("valueOf").isReferenced());
  }

  @Test(timeout = 4000)
  public void testEmptyProgram() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("");
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertEquals("Should only contain the 3 implicitly used properties", 3, map.size());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralQuotedVsUnquotedKeys() {
    Compiler compiler = new Compiler();
    String js = "var obj = { 'quotedProp': 1, unquotedProp: 2 };\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertFalse("Quoted string in standard object literal should not count as symbol use",
        map.containsKey("quotedProp"));
    assertTrue("Unquoted string in standard object literal should count as symbol use",
        map.containsKey("unquotedProp"));
    assertTrue(map.get("unquotedProp").isReferenced());
  }

  @Test(timeout = 4000)
  public void testExternPropertiesHandling() {
    Compiler compiler = new Compiler();
    String externsJs = "var window; window.externProp = 1;\n";
    String js = "function Foo() {}\nFoo.prototype.externProp = function() {};\n";

    Node externs = compiler.parseTestCode(externsJs);
    Node root = compiler.parseTestCode(js);

    AnalyzePrototypeProperties appNoModify = createApp(compiler, null, false, false);
    appNoModify.process(externs, root);
    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(appNoModify.getAllNameInfo());
    assertTrue(map.containsKey("externProp"));
    assertTrue("Property referenced in externs must be referenced when canModifyExterns=false",
        map.get("externProp").isReferenced());

    Compiler compiler2 = new Compiler();
    Node externs2 = compiler2.parseTestCode(externsJs);
    Node root2 = compiler2.parseTestCode(js);
    AnalyzePrototypeProperties appModify = createApp(compiler2, null, true, false);
    appModify.process(externs2, root2);
    Map<String, AnalyzePrototypeProperties.NameInfo> mapModify = toNameInfoMap(appModify.getAllNameInfo());
    assertTrue(mapModify.containsKey("externProp"));
    assertFalse("Property in externs is not marked referenced from externNode when canModifyExterns=true",
        mapModify.get("externProp").isReferenced());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (RemoveUnusedPrototypePropertiesTest::testAliasing7)
  // =========================================================================

  @Test(timeout = 4000)
  public void testAliasing7_prototypePropertyAliasedThroughObjectLiteral() {
    // Targets the defect condition from testAliasing7 where a prototype property is assigned
    // or aliased via an object literal or indirect assignment, ensuring that usage is propagated
    // accurately and not mistakenly pruned or lost during graph construction.
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype = { a: function() { return 1; } };\n"
        + "function Bar() {}\n"
        + "Bar.prototype = { a: Foo.prototype.a };\n"
        + "var b = new Bar();\n"
        + "b.a();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue("Property 'a' must be tracked in name info", map.containsKey("a"));
    AnalyzePrototypeProperties.NameInfo aInfo = map.get("a");
    assertTrue("Property 'a' must be marked as referenced because b.a() invokes it",
        aInfo.isReferenced());
    assertEquals("Property 'a' should have 2 declarations (one in Foo, one in Bar)",
        2, aInfo.getDeclarations().size());
  }

  @Test(timeout = 4000)
  public void testAliasing7_directPrototypeAssignmentToNonObjectLit() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype = 2;\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertFalse("Number literal assignment to prototype should not register prototype properties",
        map.containsKey("2"));
  }

  @Test(timeout = 4000)
  public void testAliasing7_prototypeAliasedToLocalVariableAndCalled() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype.bar = function() { return 1; };\n"
        + "var x = Foo.prototype;\n"
        + "x.bar();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue(map.containsKey("bar"));
    assertTrue("x.bar() access should mark 'bar' as referenced",
        map.get("bar").isReferenced());
  }

  @Test(timeout = 4000)
  public void testAliasing7_chainedPrototypeAssignmentAcrossObjects() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\n"
        + "Foo.prototype.baz = function() { return 99; };\n"
        + "function Bar() {}\n"
        + "Bar.prototype = Foo.prototype;\n"
        + "var b = new Bar();\n"
        + "b.baz();\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue(map.containsKey("baz"));
    assertTrue(map.get("baz").isReferenced());
  }

  // =========================================================================
  // Partition D: Exception, Closure Variables & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testReadClosureVariablesDetection() {
    Compiler compiler = new Compiler();
    String js = "function outer() {\n"
        + "  var secret = 42;\n"
        + "  function Foo() {}\n"
        + "  Foo.prototype.leak = function() {\n"
        + "    return secret;\n"
        + "  };\n"
        + "}\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    assertTrue(map.containsKey("leak"));
    AnalyzePrototypeProperties.NameInfo leakInfo = map.get("leak");
    assertTrue("Method reading outer variable must have readsClosureVariables=true",
        leakInfo.readsClosureVariables());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionAstRemoval_SingleChildVar() {
    Compiler compiler = new Compiler();
    String js = "var deadFunc = function() {};\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    AnalyzePrototypeProperties.NameInfo deadInfo = map.get("deadFunc");
    assertNotNull(deadInfo);
    assertEquals(1, deadInfo.getDeclarations().size());

    AnalyzePrototypeProperties.GlobalFunction gf =
        (AnalyzePrototypeProperties.GlobalFunction) deadInfo.getDeclarations().getFirst();
    assertNotNull(gf.getFunctionNode());

    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    gf.remove();
    assertNull("Single child VAR should be completely removed from parent", root.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionAstRemoval_MultiChildVar() {
    Compiler compiler = new Compiler();
    String js = "var kept = 1, deadFunc = function() {};\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    AnalyzePrototypeProperties.NameInfo deadInfo = map.get("deadFunc");
    AnalyzePrototypeProperties.GlobalFunction gf =
        (AnalyzePrototypeProperties.GlobalFunction) deadInfo.getDeclarations().getFirst();

    Node varNode = root.getFirstChild();
    assertEquals(2, varNode.getChildCount());
    gf.remove();
    assertEquals("Multi-child VAR should only remove the target NAME child", 1, varNode.getChildCount());
    assertEquals("kept", varNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionAstRemoval_FunctionDeclaration() {
    Compiler compiler = new Compiler();
    String js = "function deadNamed() {}\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    AnalyzePrototypeProperties.GlobalFunction gf =
        (AnalyzePrototypeProperties.GlobalFunction) map.get("deadNamed").getDeclarations().getFirst();

    assertNotNull(gf.getFunctionNode());
    assertEquals(Token.FUNCTION, gf.getFunctionNode().getType());
    gf.remove();
    assertNull("Named function declaration should be removed from parent", root.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testAssignmentPropertyRemoval() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\nFoo.prototype.dead = function() {};\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    AnalyzePrototypeProperties.AssignmentProperty prop =
        (AnalyzePrototypeProperties.AssignmentProperty) map.get("dead").getDeclarations().getFirst();

    assertEquals(2, root.getChildCount());
    prop.remove();
    assertEquals("Assignment expression should be removed", 1, root.getChildCount());
    assertEquals(Token.FUNCTION, root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testLiteralPropertyRemoval() {
    Compiler compiler = new Compiler();
    String js = "function Foo() {}\nFoo.prototype = { dead: function() {}, keep: 1 };\n";
    Node root = compiler.parseTestCode(js);
    Node externs = compiler.parseTestCode("");

    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    app.process(externs, root);

    Map<String, AnalyzePrototypeProperties.NameInfo> map = toNameInfoMap(app.getAllNameInfo());
    AnalyzePrototypeProperties.LiteralProperty prop =
        (AnalyzePrototypeProperties.LiteralProperty) map.get("dead").getDeclarations().getFirst();

    Node assignExpr = root.getLastChild();
    Node objLit = assignExpr.getFirstChild().getLastChild();
    assertEquals(2, objLit.getChildCount());

    prop.remove();
    assertEquals(1, objLit.getChildCount());
    assertEquals("keep", objLit.getFirstChild().getString());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGlobalFunctionConstructorPrecondition() {
    Compiler compiler = new Compiler();
    Node nameNode = Node.newString("fake");
    Node invalidParent = new Node(Token.EXPR_RESULT, nameNode);
    appCreateGlobalFunction(compiler, nameNode, invalidParent, null);
  }

  private AnalyzePrototypeProperties.GlobalFunction appCreateGlobalFunction(
      Compiler compiler, Node nameNode, Node parent, Node gramps) {
    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);
    return app.new GlobalFunction(nameNode, parent, gramps, null);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Multi-Module Graph & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testModuleGraphReferencePropagation() {
    Compiler compiler = new Compiler();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSModuleGraph moduleGraph = new JSModuleGraph(new JSModule[] {m1, m2});

    Node externs = compiler.parseTestCode("");
    AnalyzePrototypeProperties app = createApp(compiler, moduleGraph, false, false);

    AnalyzePrototypeProperties.NameInfo infoA = app.new NameInfo("propX");
    boolean firstMark = infoA.markReference(m2);
    assertTrue("First mark should return true (changed)", firstMark);
    assertEquals(m2, infoA.getDeepestCommonModuleRef());

    boolean secondMark = infoA.markReference(m1);
    assertTrue("Mark with earlier dependency m1 should change deepest common module", secondMark);
    assertEquals(m1, infoA.getDeepestCommonModuleRef());

    boolean redundantMark = infoA.markReference(m2);
    assertFalse("Mark with downstream module should return false (no change)", redundantMark);
    assertEquals(m1, infoA.getDeepestCommonModuleRef());
  }

  @Test(timeout = 4000)
  public void testNameInfoToStringAndDeclarationsIntegrity() {
    Compiler compiler = new Compiler();
    AnalyzePrototypeProperties app = createApp(compiler, null, false, false);

    AnalyzePrototypeProperties.NameInfo nameInfo = app.new NameInfo("testSymbol");
    assertEquals("testSymbol", nameInfo.toString());
    assertFalse(nameInfo.isReferenced());
    assertFalse(nameInfo.readsClosureVariables());
    assertNotNull(nameInfo.getDeclarations());
    assertTrue(nameInfo.getDeclarations().isEmpty());

    assertTrue(nameInfo.markReference(null));
    assertTrue(nameInfo.isReferenced());
    assertFalse("Subsequent markReference without modules should not report change",
        nameInfo.markReference(null));
  }
}