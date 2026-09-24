package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.NameAnalyzer
 *
 * Decision / Condition Branch Coverage Plan:
 * 1. ProcessExternals:
 *    - Var declarations in externs
 *    - Function declarations in externs
 * 2. FindDependencyScopes:
 *    - Global vs non-global scope traversal
 *    - Assign expressions: parent is FOR (init, cond, step) vs non-FOR
 *    - Var declarations, function declarations, class-defining calls (goog.inherits, singleton getter)
 * 3. HoistVariableAndFunctionDeclarations:
 *    - Early registration of global var and function names
 * 4. FindDeclarationsAndSetters:
 *    - Var declarations, function declarations, object literal keys
 *    - Prototype assignments (ClassName.prototype.prop = ...)
 *    - Dotted assignments (A.B.C = ...) and recordWriteOnProperties propagation
 *    - Class-defining function call recording
 * 5. FindReferences:
 *    - Control structure predicates (IF, WHILE, DO, FOR, FOR-IN, SWITCH, CASE, WITH)
 *    - Return and throw expressions simplified
 *    - Side effect subexpression collection (short-circuit && / ||, hook ? :)
 *    - Instanceof check recording and removal replacement with false
 *    - Aliasing: maybeHiddenAlias via non-local function return value, direct alias merging
 *    - Ancestor assignment / function fallback referencing window
 * 6. ReferenceParentNames & ReferenceAliases:
 *    - Bi-directional graph connections between child.prop and parent
 *    - Written descendants and instanceof reference propagation across alias sets
 * 7. RemoveUnreferenced:
 *    - Token.VAR (with single child)
 *    - Token.FUNCTION
 *    - Token.ASSIGN (within EXPR_RESULT vs within RHS expression)
 *    - PrototypeSetNode removal (expr result vs value consumed)
 *    - ClassDefiningFunctionNode removal (expr result vs replacement with void 0)
 *    - InstanceOfCheckNode removal (replacement with false)
 *    - Preservation of side-effects in removed expressions
 * 8. Defects4J Known Defect:
 *    - testIssue284: Unsupported parent node type in replaceWithRhs when an unreferenced
 *      assignment or var statement appears directly as a child of an IF/WHILE/FOR without a BLOCK.
 */
public class NameAnalyzerGeminiTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
    return compiler;
  }

  private Node parse(Compiler compiler, String js) {
    return compiler.parseTestCode(js);
  }

  private void process(Compiler compiler, Node externs, Node root, boolean removeUnreferenced) {
    NameAnalyzer analyzer = new NameAnalyzer(compiler, removeUnreferenced);
    analyzer.process(externs, root);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleUnreferencedVarRemoved() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var unreferenced = 1; window.referenced = 2;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("unreferenced"));
    assertTrue(source.contains("window.referenced = 2"));
  }

  @Test(timeout = 4000)
  public void testReferencedVarRetained() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var a = 1; window.b = a;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var a = 1"));
    assertTrue(source.contains("window.b = a"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsUsedAndUnused() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function unused() { return 1; }\n"
        + "function used() { return 2; }\n"
        + "window.res = used();");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("unused"));
    assertTrue(source.contains("function used"));
    assertTrue(source.contains("window.res = used()"));
  }

  @Test(timeout = 4000)
  public void testCircularDependencyElimination() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function f() { g(); }\n"
        + "function g() { f(); }\n"
        + "window.keep = 10;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("function f"));
    assertFalse(source.contains("function g"));
    assertTrue(source.contains("window.keep = 10"));
  }

  @Test(timeout = 4000)
  public void testDottedPropertyReference() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var a = {}; a.b = {}; a.b.c = 42; window.val = a.b.c;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var a = {}"));
    assertTrue(source.contains("a.b = {}"));
    assertTrue(source.contains("a.b.c = 42"));
    assertTrue(source.contains("window.val = a.b.c"));
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentsRetainedWhenClassReferenced() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function MyClass() {}\n"
        + "MyClass.prototype.sayHi = function() { return 'hi'; };\n"
        + "window.inst = new MyClass();");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("function MyClass"));
    assertTrue(source.contains("MyClass.prototype.sayHi = function"));
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignmentsRemovedWhenClassUnreferenced() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function UnusedClass() {}\n"
        + "UnusedClass.prototype.sayHi = function() { return 'hi'; };\n"
        + "window.alive = true;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("UnusedClass"));
    assertFalse(source.contains("sayHi"));
    assertTrue(source.contains("window.alive = true"));
  }

  @Test(timeout = 4000)
  public void testInheritancePreservedWhenSubclassUsed() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; var goog = {}; goog.inherits = function(a, b) {};");
    Node root = parse(compiler, "function SuperClass() {}\n"
        + "function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);\n"
        + "window.app = new SubClass();");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("function SuperClass"));
    assertTrue(source.contains("function SubClass"));
    assertTrue(source.contains("goog.inherits(SubClass, SuperClass)"));
  }

  @Test(timeout = 4000)
  public void testInheritanceRemovedWhenSubclassUnused() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; var goog = {}; goog.inherits = function(a, b) {};");
    Node root = parse(compiler, "function SuperClass() {}\n"
        + "function SubClass() {}\n"
        + "goog.inherits(SubClass, SuperClass);\n"
        + "window.keepAlive = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("SuperClass"));
    assertFalse(source.contains("SubClass"));
    assertFalse(source.contains("goog.inherits"));
    assertTrue(source.contains("window.keepAlive = 1"));
  }

  @Test(timeout = 4000)
  public void testGlobalObjectLiteralKeys() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var myObj = { k1: 10, k2: 20 }; window.result = myObj.k1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var myObj = {k1: 10, k2: 20}"));
    assertTrue(source.contains("window.result = myObj.k1"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScripts() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "");
    Node root = parse(compiler, "");

    NameAnalyzer analyzer = new NameAnalyzer(compiler, true);
    analyzer.process(externs, root);

    assertEquals("", compiler.toSource(root).trim());
    String report = analyzer.getHtmlReport();
    assertNotNull(report);
    assertTrue(report.contains("OVERALL STATS"));
  }

  @Test(timeout = 4000)
  public void testOnlyExternDeclarations() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; function externalFunc() {}");
    Node root = parse(compiler, "");

    process(compiler, externs, root, true);

    assertEquals("", compiler.toSource(root).trim());
  }

  @Test(timeout = 4000)
  public void testRecursiveSelfCallOnly() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function recurse() { recurse(); }");

    process(compiler, externs, root, true);

    assertEquals("", compiler.toSource(root).trim());
  }

  @Test(timeout = 4000)
  public void testDeadVariableWithSideEffectRhsPreserved() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; function alert(msg) {}");
    Node root = parse(compiler, "var a = alert('side effect!'); window.done = true;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("var a ="));
    assertTrue(source.contains("alert(\"side effect!\")"));
    assertTrue(source.contains("window.done = true"));
  }

  @Test(timeout = 4000)
  public void testDeeplyNestedPropertyChain() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var a = {}; a.b = {}; a.b.c = {}; a.b.c.d = {}; a.b.c.d.e = 99;\n"
        + "window.v = a.b.c.d.e;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("a.b.c.d.e = 99"));
    assertTrue(source.contains("window.v = a.b.c.d.e"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 284)
  // =========================================================================

  /**
   * Targets Defects4J known defect:
   * Issue 284: NameAnalyzer threw an IllegalArgumentException / INTERNAL COMPILER ERROR
   * ("Unsupported parent node type in replaceWithRhs IF") when an unreferenced assignment
   * or var declaration appeared as the direct child of an IF without a BLOCK.
   */
  @Test(timeout = 4000)
  public void testIssue284() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var x = 1; if (true) x = 2; window.keep = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("x = 2"));
    assertTrue(source.contains("window.keep = 1"));
  }

  @Test(timeout = 4000)
  public void testIssue284_ifWithoutBlockVar() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "if (true) var x = 1; window.keep = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("var x = 1"));
    assertTrue(source.contains("window.keep = 1"));
  }

  @Test(timeout = 4000)
  public void testIssue284_ifElseWithoutBlock() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var x = 1; if (false) { window.a = 1; } else x = 2;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("x = 2"));
    assertTrue(source.contains("window.a = 1"));
  }

  @Test(timeout = 4000)
  public void testIssue284_whileWithoutBlock() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var x = 1; while (false) x = 2; window.keep = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("x = 2"));
    assertTrue(source.contains("window.keep = 1"));
  }

  @Test(timeout = 4000)
  public void testIssue284_doWhileWithoutBlock() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var x = 1; do x = 2; while (false); window.keep = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("x = 2"));
    assertTrue(source.contains("window.keep = 1"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testInstanceOfRemovalReplacedWithFalse() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function UnusedClass() {}\n"
        + "window.isInst = (window.obj instanceof UnusedClass);");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("function UnusedClass"));
    assertTrue(source.contains("window.isInst = false"));
  }

  @Test(timeout = 4000)
  public void testPredicateDependenciesInIfWhileDo() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var cond1 = true;\n"
        + "var cond2 = true;\n"
        + "var cond3 = true;\n"
        + "if (cond1) { window.a = 1; }\n"
        + "while (cond2) { window.b = 2; }\n"
        + "do { window.c = 3; } while (cond3);");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var cond1 = true"));
    assertTrue(source.contains("var cond2 = true"));
    assertTrue(source.contains("var cond3 = true"));
  }

  @Test(timeout = 4000)
  public void testSwitchAndWithDependencies() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var switchVal = 1;\n"
        + "var caseVal = 1;\n"
        + "var withObj = {};\n"
        + "switch (switchVal) { case caseVal: window.x = 1; break; }\n"
        + "with (withObj) { window.y = 2; }");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var switchVal = 1"));
    assertTrue(source.contains("var caseVal = 1"));
    assertTrue(source.contains("var withObj = {}"));
  }

  @Test(timeout = 4000)
  public void testForAndForInLoops() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var forInit = 0;\n"
        + "var forLimit = 10;\n"
        + "for (var i = forInit; i < forLimit; i++) { window.sum = i; }\n"
        + "var obj = { k: 1 };\n"
        + "for (var key in obj) { window.last = key; }");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var forInit = 0"));
    assertTrue(source.contains("var forLimit = 10"));
    assertTrue(source.contains("var obj = {k: 1}"));
  }

  @Test(timeout = 4000)
  public void testForLoopAssignmentInitAndStep() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var a = 0;\n"
        + "var b = 0;\n"
        + "for (a = 1; window.cond; b = 2) { window.inside = true; }");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("window.inside = true"));
  }

  @Test(timeout = 4000)
  public void testThrowAndReturnExpressionsPreserved() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var throwErr = new Error();\n"
        + "function testFn() { var retVal = 42; return retVal; }\n"
        + "window.testFn = testFn;\n"
        + "if (window.fail) { throw throwErr; }");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var throwErr = new Error"));
    assertTrue(source.contains("var retVal = 42"));
    assertTrue(source.contains("return retVal"));
  }

  @Test(timeout = 4000)
  public void testAliasesWithWrittenDescendants() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var a = {};\n"
        + "var b = a;\n"
        + "b.prop = 123;\n"
        + "window.res = a.prop;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var a = {}"));
    assertTrue(source.contains("var b = a"));
    assertTrue(source.contains("b.prop = 123"));
    assertTrue(source.contains("window.res = a.prop"));
  }

  @Test(timeout = 4000)
  public void testClassDefiningFunctionInNonExprResult() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; var goog = {}; goog.inherits = function(a, b) {};");
    Node root = parse(compiler, "function Parent() {}\n"
        + "function Child() {}\n"
        + "var dummy = goog.inherits(Child, Parent);\n"
        + "window.alive = 1;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("Parent"));
    assertFalse(source.contains("Child"));
    assertFalse(source.contains("goog.inherits"));
    assertTrue(source.contains("window.alive = 1"));
  }

  @Test(timeout = 4000)
  public void testHookAndShortCircuitExpressionsInNodeAccumulator() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window; function alert(x) {}");
    Node root = parse(compiler, "var deadHook = window.cond ? alert(1) : alert(2);\n"
        + "var deadAnd = window.cond && alert(3);\n"
        + "var deadOr = window.cond || alert(4);\n"
        + "window.done = true;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertFalse(source.contains("deadHook"));
    assertFalse(source.contains("deadAnd"));
    assertFalse(source.contains("deadOr"));
    assertTrue(source.contains("window.cond ? alert(1) : alert(2)"));
    assertTrue(source.contains("window.cond && alert(3)"));
    assertTrue(source.contains("window.cond || alert(4)"));
    assertTrue(source.contains("window.done = true"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Report & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testHtmlReportGenerationWithClassesAndFunctions() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "function MyClass() {}\n"
        + "MyClass.prototype.fnA = function() {};\n"
        + "MyClass.prototype.fnB = function() {};\n"
        + "function globalHelper() {}\n"
        + "window.app = new MyClass();\n"
        + "window.help = globalHelper;");

    NameAnalyzer analyzer = new NameAnalyzer(compiler, false);
    analyzer.process(externs, root);

    String report = analyzer.getHtmlReport();
    assertNotNull(report);
    assertTrue(report.startsWith("<html><body>"));
    assertTrue(report.endsWith("</body></html>"));
    assertTrue(report.contains("OVERALL STATS"));
    assertTrue(report.contains("Total Names:"));
    assertTrue(report.contains("Total Classes:"));
    assertTrue(report.contains("Referenced Names:"));
    assertTrue(report.contains("ALL NAMES"));
    assertTrue(report.contains("MyClass"));
    assertTrue(report.contains("fnA"));
    assertTrue(report.contains("fnB"));
    assertTrue(report.contains("globalHelper"));
  }

  @Test(timeout = 4000)
  public void testProcessWithoutRemoveUnreferencedPreservesAllNodes() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    String js = "var unusedA = 1;\nfunction unusedB() {}\nwindow.live = 2;";
    Node root = parse(compiler, js);

    process(compiler, externs, root, false);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var unusedA = 1"));
    assertTrue(source.contains("function unusedB"));
    assertTrue(source.contains("window.live = 2"));
  }

  @Test(timeout = 4000)
  public void testGetElemAssignmentDoesNotCrash() {
    Compiler compiler = createCompiler();
    Node externs = parse(compiler, "var window;");
    Node root = parse(compiler, "var arr = []; arr[0] = 'elem'; window.arr = arr;");

    process(compiler, externs, root, true);

    String source = compiler.toSource(root);
    assertTrue(source.contains("var arr = []"));
    assertTrue(source.contains("arr[0] = \"elem\""));
    assertTrue(source.contains("window.arr = arr"));
  }
}