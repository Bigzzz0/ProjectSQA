package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.AnalyzePrototypeProperties
 * Defect Focus: Defects4J CrossModuleMethodMotionTest::testIssue600 / testIssue600b / testIssue600e
 *
 * Core Decision Logic & Branches Targeted:
 * 1. Prototype Declaration Styles:
 *    - AssignmentProperty via EXPR assignment (e.g. Foo.prototype.bar = function() {})
 *    - LiteralProperty via Object Literal assignment (e.g. Foo.prototype = { bar: function() {} })
 * 2. Scope & Closure Variable Detection (Issue 600 / Defect Zone):
 *    - Prototype method accessing variable in outer non-global scope (readsClosureVariables == true)
 *    - Prototype method accessing global variable (readsClosureVariables == false)
 *    - Prototype method accessing local inner variable (readsClosureVariables == false)
 *    - DEFECT TRIGGER: Object literal prototype assignments (LiteralProperty) enclosed in an outer
 *      scope must mark readsClosureVariables == true when reading closure variables.
 * 3. Module & Edge Propagation:
 *    - ModuleGraph null vs non-null branch execution
 *    - FixedPoint reference propagation across dependent modules
 *    - Implicitly used JS properties (length, toString, valueOf)
 *    - Extern property handling when canModifyExterns is true vs false
 * 4. AST Modification & Life-Cycle:
 *    - GlobalFunction.remove() for function statements, single-child vars, multi-child vars
 *    - AssignmentProperty.remove(), getPrototype(), getValue(), getModule()
 *    - LiteralProperty.remove(), getPrototype(), getValue(), getModule()
 *    - NameInfo contract: toString(), isReferenced(), markReference()
 * 5. Configuration Flags:
 *    - anchorUnusedVars (true vs false)
 *    - CodingConvention exported properties
 * -------------------------------------------------------------------------------------------------------
 */
public class AnalyzePrototypePropertiesGeminiTest {

  // =========================================================================
  // Test Harness Helpers
  // =========================================================================

  private static class ModuleTestResult {
    AnalyzePrototypeProperties pass;
    Compiler compiler;
    JSModule m1;
    JSModule m2;
    JSModule m3;
    JSModuleGraph graph;
  }

  private AnalyzePrototypeProperties process(String js, boolean canModifyExterns, boolean anchorUnusedVars) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", js);
    compiler.init(new JSSourceFile[] {extern}, new JSSourceFile[] {input}, options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST must not be null", root);
    Node externRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, null, canModifyExterns, anchorUnusedVars);
    pass.process(externRoot, mainRoot);
    return pass;
  }

  private ModuleTestResult processModules(String js1, String js2, boolean anchorUnusedVars) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    JSSourceFile f1 = JSSourceFile.fromCode("m1.js", js1);
    JSSourceFile f2 = JSSourceFile.fromCode("m2.js", js2);
    m1.add(f1);
    m2.add(f2);
    JSModule[] modules = new JSModule[] { m1, m2 };
    JSModuleGraph moduleGraph = new JSModuleGraph(modules);
    compiler.initModules(
        Collections.singletonList(JSSourceFile.fromCode("externs.js", "function extProp() {}")),
        Arrays.asList(modules),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST must not be null", root);
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, moduleGraph, false, anchorUnusedVars);
    pass.process(root.getFirstChild(), root.getLastChild());

    ModuleTestResult res = new ModuleTestResult();
    res.pass = pass;
    res.compiler = compiler;
    res.m1 = m1;
    res.m2 = m2;
    res.graph = moduleGraph;
    return res;
  }

  private ModuleTestResult processThreeModules(String js1, String js2, String js3) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    JSModule m3 = new JSModule("m3");
    m2.addDependency(m1);
    m3.addDependency(m2);
    m1.add(JSSourceFile.fromCode("m1.js", js1));
    m2.add(JSSourceFile.fromCode("m2.js", js2));
    m3.add(JSSourceFile.fromCode("m3.js", js3));
    JSModule[] modules = new JSModule[] { m1, m2, m3 };
    JSModuleGraph moduleGraph = new JSModuleGraph(modules);
    compiler.initModules(
        Collections.singletonList(JSSourceFile.fromCode("externs.js", "var ext;")),
        Arrays.asList(modules),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST must not be null", root);
    AnalyzePrototypeProperties pass = new AnalyzePrototypeProperties(
        compiler, moduleGraph, false, false);
    pass.process(root.getFirstChild(), root.getLastChild());

    ModuleTestResult res = new ModuleTestResult();
    res.pass = pass;
    res.compiler = compiler;
    res.m1 = m1;
    res.m2 = m2;
    res.m3 = m3;
    res.graph = moduleGraph;
    return res;
  }

  private AnalyzePrototypeProperties.NameInfo findNameInfo(
      AnalyzePrototypeProperties app, String name) {
    for (AnalyzePrototypeProperties.NameInfo info : app.getAllNameInfo()) {
      if (name.equals(info.name)) {
        return info;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAssignmentPropertyDeclarationAndUsage() {
    String js =
        "function Foo() {}\n" +
        "Foo.prototype.bar = function() { return 1; };\n" +
        "var x = new Foo();\n" +
        "x.bar();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo barInfo = findNameInfo(pass, "bar");
    assertNotNull("NameInfo for 'bar' must exist", barInfo);
    assertTrue("Property 'bar' should be marked referenced", barInfo.isReferenced());
    assertFalse("Property 'bar' does not read closure variables", barInfo.readsClosureVariables());
    assertEquals(1, barInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol sym = barInfo.getDeclarations().getFirst();
    assertTrue(sym instanceof AnalyzePrototypeProperties.AssignmentProperty);
    AnalyzePrototypeProperties.AssignmentProperty ap = (AnalyzePrototypeProperties.AssignmentProperty) sym;
    assertEquals("Foo.prototype", ap.getPrototype().getQualifiedName());
    assertTrue("Assigned value should be a function", ap.getValue().isFunction());
  }

  @Test(timeout = 4000)
  public void testLiteralPropertyDeclarationAndUsage() {
    String js =
        "function Foo() {}\n" +
        "Foo.prototype = {\n" +
        "  methodA: function() { return 1; },\n" +
        "  propB: 42\n" +
        "};\n" +
        "var f = new Foo();\n" +
        "f.methodA();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo methodAInfo = findNameInfo(pass, "methodA");
    assertNotNull("NameInfo for 'methodA' must exist", methodAInfo);
    assertTrue("methodA must be referenced", methodAInfo.isReferenced());
    assertEquals(1, methodAInfo.getDeclarations().size());

    AnalyzePrototypeProperties.Symbol sym = methodAInfo.getDeclarations().getFirst();
    assertTrue(sym instanceof AnalyzePrototypeProperties.LiteralProperty);
    AnalyzePrototypeProperties.LiteralProperty lp = (AnalyzePrototypeProperties.LiteralProperty) sym;
    assertEquals("Foo.prototype", lp.getPrototype().getQualifiedName());
    assertTrue(lp.getValue().isFunction());

    AnalyzePrototypeProperties.NameInfo propBInfo = findNameInfo(pass, "propB");
    assertNotNull("NameInfo for 'propB' must exist", propBInfo);
    assertFalse("propB was not accessed so should be unreferenced", propBInfo.isReferenced());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionsVarAndDeclaration() {
    String js =
        "function globalFnDecl() { return 1; }\n" +
        "var globalVarFn = function() { return 2; };\n" +
        "globalFnDecl();\n" +
        "globalVarFn();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo declInfo = findNameInfo(pass, "globalFnDecl");
    assertNotNull(declInfo);
    assertTrue(declInfo.isReferenced());
    assertEquals(1, declInfo.getDeclarations().size());
    AnalyzePrototypeProperties.GlobalFunction gf1 =
        (AnalyzePrototypeProperties.GlobalFunction) declInfo.getDeclarations().getFirst();
    assertTrue(gf1.getFunctionNode().isFunction());

    AnalyzePrototypeProperties.NameInfo varInfo = findNameInfo(pass, "globalVarFn");
    assertNotNull(varInfo);
    assertTrue(varInfo.isReferenced());
    assertEquals(1, varInfo.getDeclarations().size());
    AnalyzePrototypeProperties.GlobalFunction gf2 =
        (AnalyzePrototypeProperties.GlobalFunction) varInfo.getDeclarations().getFirst();
    assertTrue(gf2.getFunctionNode().isFunction());
  }

  @Test(timeout = 4000)
  public void testClosureVariableReadingMarkedTrue() {
    String js =
        "(function() {\n" +
        "  var outerVar = 100;\n" +
        "  Foo.prototype.reader = function() {\n" +
        "    return outerVar;\n" +
        "  };\n" +
        "})();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo readerInfo = findNameInfo(pass, "reader");
    assertNotNull("NameInfo for 'reader' must exist", readerInfo);
    assertTrue("Method reading outer closure variable must have readsClosureVariables == true",
        readerInfo.readsClosureVariables());
  }

  @Test(timeout = 4000)
  public void testGlobalAndLocalVarsDoNotMarkClosureVariables() {
    String js =
        "var globalVar = 1;\n" +
        "function Foo() {}\n" +
        "Foo.prototype.method = function() {\n" +
        "  var localVar = 2;\n" +
        "  return globalVar + localVar;\n" +
        "};\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo info = findNameInfo(pass, "method");
    assertNotNull(info);
    assertFalse("Accessing global or intra-method local vars should NOT mark readsClosureVariables",
        info.readsClosureVariables());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Module Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testImplicitlyUsedPropertiesAlwaysReferenced() {
    AnalyzePrototypeProperties pass = process("var a = 1;", false, false);
    AnalyzePrototypeProperties.NameInfo lengthInfo = findNameInfo(pass, "length");
    assertNotNull("length property must be tracked", lengthInfo);
    assertTrue("length must be implicitly referenced", lengthInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo toStringInfo = findNameInfo(pass, "toString");
    assertNotNull("toString property must be tracked", toStringInfo);
    assertTrue("toString must be implicitly referenced", toStringInfo.isReferenced());

    AnalyzePrototypeProperties.NameInfo valueOfInfo = findNameInfo(pass, "valueOf");
    assertNotNull("valueOf property must be tracked", valueOfInfo);
    assertTrue("valueOf must be implicitly referenced", valueOfInfo.isReferenced());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralKeyQuotedVsUnquoted() {
    String js =
        "var obj = {\n" +
        "  unquotedKey: 1,\n" +
        "  'quotedKey': 2\n" +
        "};\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo unquoted = findNameInfo(pass, "unquotedKey");
    assertNotNull("Unquoted object literal key should be registered as property use", unquoted);
    assertTrue(unquoted.isReferenced());

    AnalyzePrototypeProperties.NameInfo quoted = findNameInfo(pass, "quotedKey");
    assertNull("Quoted object literal key should NOT be registered as property use", quoted);
  }

  @Test(timeout = 4000)
  public void testAnchorUnusedVarsSetting() {
    String js = "var unusedFn = function() { return 42; };\n";
    AnalyzePrototypeProperties passNoAnchor = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo infoNoAnchor = findNameInfo(passNoAnchor, "unusedFn");
    assertNotNull(infoNoAnchor);
    assertFalse("Unused function should not be referenced when anchorUnusedVars is false",
        infoNoAnchor.isReferenced());

    AnalyzePrototypeProperties passAnchor = process(js, false, true);
    AnalyzePrototypeProperties.NameInfo infoAnchor = findNameInfo(passAnchor, "unusedFn");
    assertNotNull(infoAnchor);
    assertTrue("Unused function must be referenced when anchorUnusedVars is true",
        infoAnchor.isReferenced());
  }

  @Test(timeout = 4000)
  public void testMultiModuleDeepestCommonModulePropagation() {
    String m1Js =
        "function Foo() {}\n" +
        "Foo.prototype.work = function() { return 10; };\n";
    String m2Js =
        "var x = new Foo();\n" +
        "x.work();\n";
    ModuleTestResult res = processModules(m1Js, m2Js, false);
    AnalyzePrototypeProperties.NameInfo workInfo = findNameInfo(res.pass, "work");
    assertNotNull(workInfo);
    assertTrue(workInfo.isReferenced());
    assertEquals("Deepest common module reference should be m2",
        res.m2, workInfo.getDeepestCommonModuleRef());
  }

  @Test(timeout = 4000)
  public void testThreeModuleDependencyCascade() {
    String m1Js = "function Foo() {}\nFoo.prototype.step = function() {};\n";
    String m2Js = "function middle() { new Foo().step(); }\n";
    String m3Js = "middle();\n";
    ModuleTestResult res = processThreeModules(m1Js, m2Js, m3Js);
    AnalyzePrototypeProperties.NameInfo stepInfo = findNameInfo(res.pass, "step");
    assertNotNull(stepInfo);
    assertTrue(stepInfo.isReferenced());
    assertNotNull(stepInfo.getDeepestCommonModuleRef());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 600)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectIssue600LiteralPropertyReadsClosureVariables() {
    // Directly targets Defects4J failure: CrossModuleMethodMotionTest::testIssue600
    // When prototype methods are assigned via Object Literal inside an outer closure,
    // the NameInfo for that method MUST record readsClosureVariables() == true so that
    // CrossModuleMethodMotion will not illegally move it across modules without its closure.
    String js =
        "(function() {\n" +
        "  var closedVar = 10;\n" +
        "  Foo.prototype = {\n" +
        "    issue600Method: function() {\n" +
        "      return closedVar;\n" +
        "    }\n" +
        "  };\n" +
        "})();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo info = findNameInfo(pass, "issue600Method");
    assertNotNull("NameInfo for 'issue600Method' must be recorded in property graph", info);
    assertTrue("Method 'issue600Method' in prototype object literal reads outer closure variable " +
        "and must report readsClosureVariables() == true",
        info.readsClosureVariables());
  }

  @Test(timeout = 4000)
  public void testDefectIssue600bMultipleLiteralPropertiesReadingClosure() {
    // Variant 600b: Object literal with multiple functions and nested references
    String js =
        "(function() {\n" +
        "  var outerA = 1;\n" +
        "  var outerB = 2;\n" +
        "  Foo.prototype = {\n" +
        "    methodOne: function() { return outerA; },\n" +
        "    methodTwo: function() { return outerB; }\n" +
        "  };\n" +
        "})();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo infoOne = findNameInfo(pass, "methodOne");
    assertNotNull("NameInfo for 'methodOne' must be found", infoOne);
    assertTrue("methodOne must report readsClosureVariables() == true",
        infoOne.readsClosureVariables());

    AnalyzePrototypeProperties.NameInfo infoTwo = findNameInfo(pass, "methodTwo");
    assertNotNull("NameInfo for 'methodTwo' must be found", infoTwo);
    assertTrue("methodTwo must report readsClosureVariables() == true",
        infoTwo.readsClosureVariables());
  }

  @Test(timeout = 4000)
  public void testDefectIssue600eNestedAnonymousFunctionReadingOuterVar() {
    // Variant 600e: Function inside literal returning an anonymous function accessing closure
    String js =
        "(function() {\n" +
        "  var outerScoped = 'secret';\n" +
        "  Foo.prototype = {\n" +
        "    factory: function() {\n" +
        "      return function() { return outerScoped; };\n" +
        "    }\n" +
        "  };\n" +
        "})();\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    AnalyzePrototypeProperties.NameInfo factoryInfo = findNameInfo(pass, "factory");
    assertNotNull("NameInfo for 'factory' must exist", factoryInfo);
    assertTrue("Method enclosing closure variable in nested function must report readsClosureVariables() == true",
        factoryInfo.readsClosureVariables());
  }

  // =========================================================================
  // Partition D: AST Manipulation, Removal Guard Paths & Externs
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalFunctionRemovalSingleAndMultiChildVar() {
    String js =
        "var singleFn = function() {};\n" +
        "var extra = 1, multiFn = function() {};\n" +
        "function standaloneFn() {}\n";
    AnalyzePrototypeProperties pass = process(js, false, false);

    // Single-child VAR
    AnalyzePrototypeProperties.NameInfo singleInfo = findNameInfo(pass, "singleFn");
    assertNotNull(singleInfo);
    AnalyzePrototypeProperties.GlobalFunction gfSingle =
        (AnalyzePrototypeProperties.GlobalFunction) singleInfo.getDeclarations().getFirst();
    Node singleParent = gfSingle.getFunctionNode().getParent().getParent();
    assertNotNull(singleParent);
    gfSingle.remove();
    assertNull("Single child var statement must be removed from parent", singleParent.getParent());

    // Multi-child VAR
    AnalyzePrototypeProperties.NameInfo multiInfo = findNameInfo(pass, "multiFn");
    assertNotNull(multiInfo);
    AnalyzePrototypeProperties.GlobalFunction gfMulti =
        (AnalyzePrototypeProperties.GlobalFunction) multiInfo.getDeclarations().getFirst();
    Node varNode = gfMulti.getFunctionNode().getParent();
    gfMulti.remove();
    assertEquals("Remaining VAR node should only have 1 child ('extra')", 1, varNode.getChildCount());

    // Standalone function declaration
    AnalyzePrototypeProperties.NameInfo standaloneInfo = findNameInfo(pass, "standaloneFn");
    assertNotNull(standaloneInfo);
    AnalyzePrototypeProperties.GlobalFunction gfStandalone =
        (AnalyzePrototypeProperties.GlobalFunction) standaloneInfo.getDeclarations().getFirst();
    Node fnNode = gfStandalone.getFunctionNode();
    Node fnParent = fnNode.getParent();
    assertNotNull(fnParent);
    gfStandalone.remove();
    assertNull("Standalone function must be detached from AST", fnNode.getParent());
  }

  @Test(timeout = 4000)
  public void testAssignmentPropertyAndLiteralPropertyRemoval() {
    String js =
        "function Foo() {}\n" +
        "Foo.prototype.toRemove = function() {};\n" +
        "Foo.prototype = { litToRemove: function() {} };\n";
    AnalyzePrototypeProperties pass = process(js, false, false);

    // AssignmentProperty remove
    AnalyzePrototypeProperties.NameInfo assignPropInfo = findNameInfo(pass, "toRemove");
    assertNotNull(assignPropInfo);
    AnalyzePrototypeProperties.AssignmentProperty ap =
        (AnalyzePrototypeProperties.AssignmentProperty) assignPropInfo.getDeclarations().getFirst();
    assertNull("Module should be null for single script pass", ap.getModule());
    ap.remove();

    // LiteralProperty remove
    AnalyzePrototypeProperties.NameInfo litPropInfo = findNameInfo(pass, "litToRemove");
    assertNotNull(litPropInfo);
    AnalyzePrototypeProperties.LiteralProperty lp =
        (AnalyzePrototypeProperties.LiteralProperty) litPropInfo.getDeclarations().getFirst();
    assertNull("Module should be null for single script pass", lp.getModule());
    lp.remove();
  }

  @Test(timeout = 4000)
  public void testCanModifyExternsFlagBranches() {
    String externsJs = "function extObj() {} extObj.prototype.extMethod = function() {};\n";
    String mainJs = "var x = new extObj(); x.extMethod();\n";

    Compiler compiler1 = new Compiler();
    CompilerOptions options1 = new CompilerOptions();
    compiler1.init(
        new JSSourceFile[] { JSSourceFile.fromCode("externs.js", externsJs) },
        new JSSourceFile[] { JSSourceFile.fromCode("input.js", mainJs) },
        options1);
    Node root1 = compiler1.parseInputs();
    AnalyzePrototypeProperties passExternsNotModifiable = new AnalyzePrototypeProperties(
        compiler1, null, false, false);
    passExternsNotModifiable.process(root1.getFirstChild(), root1.getLastChild());
    AnalyzePrototypeProperties.NameInfo info1 = findNameInfo(passExternsNotModifiable, "extMethod");
    assertNotNull("extMethod should be captured when canModifyExterns is false", info1);

    Compiler compiler2 = new Compiler();
    CompilerOptions options2 = new CompilerOptions();
    compiler2.init(
        new JSSourceFile[] { JSSourceFile.fromCode("externs.js", externsJs) },
        new JSSourceFile[] { JSSourceFile.fromCode("input.js", mainJs) },
        options2);
    Node root2 = compiler2.parseInputs();
    AnalyzePrototypeProperties passExternsModifiable = new AnalyzePrototypeProperties(
        compiler2, null, true, false);
    passExternsModifiable.process(root2.getFirstChild(), root2.getLastChild());
    // ProcessExternProperties was bypassed when canModifyExterns == true
    assertNotNull(passExternsModifiable.getAllNameInfo());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testNameInfoToStringAndMarkReferenceLifecycle() {
    AnalyzePrototypeProperties pass = process("var dummy = 1;", false, false);
    Collection<AnalyzePrototypeProperties.NameInfo> allInfo = pass.getAllNameInfo();
    assertNotNull(allInfo);
    assertFalse(allInfo.isEmpty());

    AnalyzePrototypeProperties.NameInfo sample = allInfo.iterator().next();
    assertEquals("toString() must return the name field", sample.name, sample.toString());

    // Verify markReference state changes
    boolean changedFirst = sample.markReference(null);
    assertTrue("Calling markReference first time should indicate change or referenced", sample.isReferenced());
    boolean changedSecond = sample.markReference(null);
    assertFalse("Calling markReference with identical module should not change state", changedSecond);
  }

  @Test(timeout = 4000)
  public void testExportedPropertyConventionHandling() {
    // By standard Google convention, properties starting with _ are not exported unless configured,
    // but test normal property vs convention queries
    String js =
        "function Foo() {}\n" +
        "Foo.prototype._exportedProp = function() {};\n" +
        "Foo.prototype.normalProp = function() {};\n";
    AnalyzePrototypeProperties pass = process(js, false, false);
    assertNotNull(findNameInfo(pass, "_exportedProp"));
    assertNotNull(findNameInfo(pass, "normalProp"));
  }
}