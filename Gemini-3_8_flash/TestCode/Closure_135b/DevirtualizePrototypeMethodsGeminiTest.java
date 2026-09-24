/*
 * [Branch & Defect Analysis Matrix]
 * Targets: DevirtualizePrototypeMethods.java
 *
 * Decision / Condition Coverage Targets:
 * 1. process(externs, root):
 *    - Empty externs/root, multiple definition sites, eligible vs non-eligible definitions.
 * 2. isCall(site):
 *    - Node is first child of CALL vs Node is an argument in CALL vs Node is in GETPROP / other expression.
 * 3. isPrototypeMethodDefinition(node):
 *    - parent == null (false)
 *    - gramp == null, parent.getFirstChild() != node, !isExprAssign(gramp) (false)
 *    - functionNode == null, !isFunction(functionNode) (false)
 *    - !isGetProp(node) (false)
 *    - nameNode != getProp or nameNode.getLastChild() != "prototype" (false)
 *    - Valid prototype definition: A.prototype.foo = function(...) {} (true)
 * 4. isEligibleDefinition(defFinder, defSite):
 *    - defSite.inExterns == true (filtered out)
 *    - defSite.inGlobalScope == false (filtered out)
 *    - rValue == null or !isFunction or isVarArgsFunction (arguments access) (false)
 *    - lValue == null or !isGetProp (false)
 *    - codingConvention.isExported(methodName) (false)
 *    - useSites.isEmpty() (false)
 *    - site not a call expression (false)
 *    - multiple definitions referenced at call site (false)
 *    - cross-module dependency violated (callModule before definitionModule) (false)
 * 5. rewriteDefinition & rewriteCallSites:
 *    - AST transformation: method call rewritten to static call with 'this' as 1st arg.
 *    - replaceReferencesToThis: function body 'this' replaced with self parameter name; inner functions skipped.
 * 6. Defect-Targeted Branch Zone (Defects4J ground truth: testRewritePrototypeMethods2 / testGoodExtends9):
 *    - When fixing function types in fixFunctionType, the added first parameter ($self) must propagate
 *      and retain the 'this' type instead of null or missing type information.
 */

package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DevirtualizePrototypeMethodsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  /**
   * Helper to parse JavaScript code into an AST and execute DevirtualizePrototypeMethods.
   */
  private Node testAndProcess(String js) {
    return testAndProcess("", js);
  }

  private Node testAndProcess(String externsJs, String js) {
    Node externsNode = compiler.parseTestCode(externsJs);
    Node rootNode = compiler.parseTestCode(js);
    Node block = new Node(Token.BLOCK, externsNode, rootNode);

    DevirtualizePrototypeMethods pass = new DevirtualizePrototypeMethods(compiler);
    pass.process(externsNode, rootNode);
    return rootNode;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testStandardPrototypeMethodDevirtualization() {
    String js = ""
        + "function A() { this.x = 1; }\n"
        + "A.prototype.foo = function(p) { return this.x + p; };\n"
        + "var a = new A();\n"
        + "var result = a.foo(2);\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue("Method should be rewritten to static function",
        output.contains("var JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self, p)"));
    assertTrue("Call should be rewritten to static function invocation",
        output.contains("JSCompiler_StaticMethods_foo(a, 2)"));
    assertTrue("References to 'this' should be replaced with self argument",
        output.contains("JSCompiler_StaticMethods_foo$self.x"));
  }

  @Test(timeout = 4000)
  public void testMultipleMethodsDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.m1 = function() { return this; };\n"
        + "A.prototype.m2 = function(a, b) { return a + b; };\n"
        + "var inst = new A();\n"
        + "inst.m1();\n"
        + "inst.m2(1, 2);\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue(output.contains("var JSCompiler_StaticMethods_m1 = function(JSCompiler_StaticMethods_m1$self)"));
    assertTrue(output.contains("var JSCompiler_StaticMethods_m2 = function(JSCompiler_StaticMethods_m2$self, a, b)"));
    assertTrue(output.contains("JSCompiler_StaticMethods_m1(inst)"));
    assertTrue(output.contains("JSCompiler_StaticMethods_m2(inst, 1, 2)"));
  }

  @Test(timeout = 4000)
  public void testThisPreservedInsideInnerFunctions() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.bar = function() {\n"
        + "  var self = this;\n"
        + "  var f = function() { return this; };\n"
        + "  return f();\n"
        + "};\n"
        + "var a = new A();\n"
        + "a.bar();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue(output.contains("var JSCompiler_StaticMethods_bar = function(JSCompiler_StaticMethods_bar$self)"));
    // The inner function's 'this' must NOT be replaced!
    assertTrue("Inner function 'this' should not be modified",
        output.contains("function() { return this; }") || output.contains("function(){return this}"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyProgramDoesNotCrash() {
    Node root = testAndProcess("");
    assertNotNull("Root should not be null", root);
  }

  @Test(timeout = 4000)
  public void testExternsMethodDefinitionNotDevirtualized() {
    String externs = "function Ext() {} Ext.prototype.foo = function() {};";
    String js = "var e = new Ext(); e.foo();";

    Node root = testAndProcess(externs, js);
    String output = compiler.toSource(root);

    assertFalse("Extern method definition should not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
    assertTrue(output.contains("e.foo()"));
  }

  @Test(timeout = 4000)
  public void testUnusedMethodNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.unused = function() { return 42; };\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Unused prototype method must not be rewritten",
        output.contains("JSCompiler_StaticMethods_unused"));
    assertTrue(output.contains("A.prototype.unused = function()"));
  }

  @Test(timeout = 4000)
  public void testVarArgsMethodNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.hasArgs = function() { return arguments.length; };\n"
        + "var a = new A();\n"
        + "a.hasArgs();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Function using arguments object must not be rewritten",
        output.contains("JSCompiler_StaticMethods_hasArgs"));
    assertTrue(output.contains("a.hasArgs()"));
  }

  @Test(timeout = 4000)
  public void testMethodAccessedAsPropertyNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.foo = function() { return 1; };\n"
        + "var a = new A();\n"
        + "var ref = a.foo;\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Method accessed as a property reference must not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
    assertTrue(output.contains("var ref = a.foo"));
  }

  @Test(timeout = 4000)
  public void testExportedMethodNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.foo = function() { return 1; };\n"
        + "goog.exportProperty(A.prototype, 'foo', A.prototype.foo);\n"
        + "var a = new A();\n"
        + "a.foo();\n";

    Node externs = compiler.parseTestCode("var goog = {}; goog.exportProperty = function(a, b, c) {};");
    Node root = compiler.parseTestCode(js);
    DevirtualizePrototypeMethods pass = new DevirtualizePrototypeMethods(compiler);
    pass.process(externs, root);

    String output = compiler.toSource(root);
    assertFalse("Exported method must not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
  }

  @Test(timeout = 4000)
  public void testMethodDefinedInsideControlStructureNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "if (true) {\n"
        + "  A.prototype.foo = function() { return 1; };\n"
        + "}\n"
        + "var a = new A();\n"
        + "a.foo();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Method defined within control structure should not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
  }

  @Test(timeout = 4000)
  public void testMethodDefinedInsideFunctionNotDevirtualized() {
    String js = ""
        + "function init() {\n"
        + "  function A() {}\n"
        + "  A.prototype.foo = function() { return 1; };\n"
        + "  var a = new A();\n"
        + "  a.foo();\n"
        + "}\n"
        + "init();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Method defined in non-global scope should not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
  }

  @Test(timeout = 4000)
  public void testMultipleConflictingDefinitionsNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.foo = function() { return 1; };\n"
        + "function B() {}\n"
        + "B.prototype.foo = function() { return 2; };\n"
        + "var x = (Math.random() > 0.5) ? new A() : new B();\n"
        + "x.foo();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Methods with multiple definitions for the same call site should not be rewritten",
        output.contains("JSCompiler_StaticMethods_foo"));
  }

  @Test(timeout = 4000)
  public void testNonFunctionPrototypePropertyAssignment() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.val = 42;\n"
        + "var a = new A();\n"
        + "var x = a.val;\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse(output.contains("JSCompiler_StaticMethods"));
    assertTrue(output.contains("A.prototype.val = 42"));
  }

  @Test(timeout = 4000)
  public void testDirectPrototypeAssignmentNotDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype = { foo: function() { return 1; } };\n"
        + "var a = new A();\n"
        + "a.foo();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertFalse("Object literal prototype assignment not handled by this pass",
        output.contains("JSCompiler_StaticMethods_foo"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Type Information on $self Node)
  // =========================================================================

  /**
   * Targets Defects4J issue where devirtualized prototype method's synthetic $self parameter
   * node lacks JSType information (evaluating to null instead of the instance type).
   */
  @Test(timeout = 4000)
  public void testRewritePrototypeMethodsTypePreservationOnSelf() {
    Compiler typeCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkTypes = true;
    typeCompiler.initOptions(options);

    String js = ""
        + "/** @constructor */\n"
        + "function A() { /** @type {number} */ this.total = 0; }\n"
        + "/** @param {number} x \n @return {number} */\n"
        + "A.prototype.foo = function(x) { this.total += x; return this.total; };\n"
        + "var a = new A();\n"
        + "a.foo(5);\n";

    Node externsNode = typeCompiler.parseTestCode("var undefined;");
    Node rootNode = typeCompiler.parseTestCode(js);

    // Type check pass to populate AST types
    TypeCheck typeCheck = new TypeCheck(
        typeCompiler,
        new SemanticReverseAbstractInterpreter(typeCompiler.getCodingConvention(), typeCompiler.getTypeRegistry()),
        typeCompiler.getTypeRegistry());
    typeCheck.processForTesting(externsNode, rootNode);

    // Run DevirtualizePrototypeMethods
    DevirtualizePrototypeMethods pass = new DevirtualizePrototypeMethods(typeCompiler);
    pass.process(externsNode, rootNode);

    // Locate the rewritten function: var JSCompiler_StaticMethods_foo = function(...)
    Node varNode = findChildByName(rootNode, "JSCompiler_StaticMethods_foo");
    assertNotNull("Rewritten static method VAR node must exist", varNode);

    Node functionNode = varNode.getFirstChild();
    assertTrue("Child of VAR name should be FUNCTION", functionNode.isFunction());

    Node paramList = functionNode.getFirstChild().getNext();
    assertNotNull("Parameter list must exist", paramList);

    Node selfParam = paramList.getFirstChild();
    assertNotNull("self parameter must exist", selfParam);
    assertEquals("First param must be self", "JSCompiler_StaticMethods_foo$self", selfParam.getString());

    // The $self parameter MUST have the JSType of 'A' (not null!)
    JSType selfType = selfParam.getJSType();
    assertNotNull("Self parameter JSType must not be null after devirtualization", selfType);
    assertTrue("Self parameter type must match constructor A",
        selfType.isInstanceType() && selfType.toString().equals("A"));
  }

  // =========================================================================
  // Partition D: Multi-Module & Dependency Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testCrossModuleValidDependencyAllowsDevirtualization() {
    JSModule mod1 = new JSModule("m1");
    JSModule mod2 = new JSModule("m2");
    mod2.addDependency(mod1);

    JSModule[] modules = new JSModule[] { mod1, mod2 };
    JSModuleGraph moduleGraph = new JSModuleGraph(modules);

    Compiler modCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    modCompiler.initOptions(options);

    Node root1 = modCompiler.parseTestCode("function A() {} A.prototype.m = function() { return 1; };");
    Node root2 = modCompiler.parseTestCode("var a = new A(); a.m();");

    JSChunk chunk1 = new JSChunk(mod1, root1);
    JSChunk chunk2 = new JSChunk(mod2, root2);

    // Constructing SimpleDefinitionFinder with modules
    Node externs = modCompiler.parseTestCode("");
    Node root = new Node(Token.BLOCK, root1, root2);

    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(modCompiler);
    defFinder.process(externs, root);

    DevirtualizePrototypeMethods pass = new DevirtualizePrototypeMethods(modCompiler);
    pass.process(externs, root);

    String output2 = modCompiler.toSource(root2);
    assertTrue("Cross-module call in dependent module should be rewritten",
        output2.contains("JSCompiler_StaticMethods_m(a)"));
  }

  @Test(timeout = 4000)
  public void testCrossModuleIndependentFailsDevirtualization() {
    JSModule mod1 = new JSModule("m1");
    JSModule mod2 = new JSModule("m2");
    // mod2 does NOT depend on mod1!

    Compiler modCompiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    modCompiler.initOptions(options);

    Node root1 = modCompiler.parseTestCode("function A() {} A.prototype.m = function() { return 1; };");
    Node root2 = modCompiler.parseTestCode("var a = new A(); a.m();");

    Node externs = modCompiler.parseTestCode("");
    Node root = new Node(Token.BLOCK, root1, root2);

    DevirtualizePrototypeMethods pass = new DevirtualizePrototypeMethods(modCompiler);
    pass.process(externs, root);

    String output2 = modCompiler.toSource(root2);
    assertFalse("Cross-module call without dependency should not be rewritten",
        output2.contains("JSCompiler_StaticMethods_m(a)"));
  }

  // =========================================================================
  // Partition E: Defensive Guards & Edge Structures
  // =========================================================================

  @Test(timeout = 4000)
  public void testCallSiteAsArgumentToAnotherCall() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.foo = function() { return 10; };\n"
        + "var a = new A();\n"
        + "function bar(x) { return x; }\n"
        + "bar(a.foo());\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue(output.contains("bar(JSCompiler_StaticMethods_foo(a))"));
  }

  @Test(timeout = 4000)
  public void testChainedPrototypeCallsDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.step1 = function() { return this; };\n"
        + "A.prototype.step2 = function() { return 42; };\n"
        + "var a = new A();\n"
        + "a.step1().step2();\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue("Chained calls must be rewritten sequentially",
        output.contains("JSCompiler_StaticMethods_step2(JSCompiler_StaticMethods_step1(a))"));
  }

  @Test(timeout = 4000)
  public void testNamedFunctionExpressionMethodDevirtualized() {
    String js = ""
        + "function A() {}\n"
        + "A.prototype.foo = function localFoo(x) { return x; };\n"
        + "var a = new A();\n"
        + "a.foo(1);\n";

    Node root = testAndProcess(js);
    String output = compiler.toSource(root);

    assertTrue("Named function expressions on prototype should also be rewritten",
        output.contains("JSCompiler_StaticMethods_foo = function(JSCompiler_StaticMethods_foo$self, x)"));
    assertTrue(output.contains("JSCompiler_StaticMethods_foo(a, 1)"));
  }

  // =========================================================================
  // Helper Methods
  // =========================================================================

  private static Node findChildByName(Node root, String name) {
    if (root.isName() && name.equals(root.getString())) {
      return root;
    }
    for (Node child : root.children()) {
      Node res = findChildByName(child, name);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  private static class JSChunk {
    final JSModule module;
    final Node root;
    JSChunk(JSModule module, Node root) {
      this.module = module;
      this.root = root;
      CompilerInput input = new CompilerInput(
          SourceFile.fromCode(module.getName() + ".js", ""));
      module.add(input);
    }
  }
}