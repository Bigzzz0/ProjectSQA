package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: CrossModuleMethodMotion
 *
 * 1. Targeted Branches & Decisions:
 *   - process(externRoot, root):
 *       - Branch [moduleGraph == null]: No-op guard path.
 *       - Branch [moduleGraph.getModuleCount() <= 1]: Single module guard, pass skips execution.
 *       - Branch [moduleGraph.getModuleCount() > 1]: Normal analysis and method motion execution.
 *   - moveMethods(allNameInfo):
 *       - Branch [!nameInfo.isReferenced()]: Skip unreferenced prototype properties.
 *       - Branch [nameInfo.readsClosureVariables()]: Skip properties that close over lexical variables.
 *       - Branch [deepestCommonModuleRef == null]: Diagnostics report NULL_COMMON_MODULE_ERROR.
 *       - Branch [!(symbol instanceof Property)]: Skip non-property symbols in declaration iterator.
 *       - Branch [moduleGraph.dependsOn(deepestCommonModuleRef, prop.getModule()) && value.isFunction()]:
 *           - Sub-branch [valueParent.isGetterDef() || valueParent.isSetterDef()]: Skip ES5 getter/setter.
 *           - Sub-branch [Normal function prop]: Generate stub call, replace original AST node,
 *             insert unstub code into deepest common module, mark code change.
 *       - Branch [!hasStubDeclaration && idGenerator.hasGeneratedAnyIds()]: Top-level stub helper
 *         declarations insertion.
 *       - Branch [hasStubDeclaration == true]: Avoid duplicate stub declaration insertion when reused.
 *
 * 2. Defects4J Defect Target (Closure Issue 600 / testIssue600, testIssue600b, testIssue600e):
 *   - Defect: When a method is defined on a prototype in a module that is already at or deeper than
 *     deepestCommonModuleRef (e.g. class G declared in Module 1, where deepestCommonModuleRef is Module 1),
 *     CrossModuleMethodMotion mistakenly evaluates dependsOn(m1, m1) as true and stubs/unstubs
 *     G.prototype.bar within its own module instead of leaving it untouched.
 *   - Trigger & Detection: Verify that methods whose declaration module equals the deepest common module
 *     are NOT stubbed with JSCompiler_stubMethod.
 * ====================================================================================================
 */
public class CrossModuleMethodMotionGeminiTest {

  /**
   * Helper utility to initialize modules, parse JS inputs into AST, execute
   * CrossModuleMethodMotion pass, and return the emitted source per module.
   */
  private String[] compileAndRunPass(
      CrossModuleMethodMotion.IdGenerator idGenerator,
      boolean canModifyExterns,
      String... moduleSources) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Collections.singletonList(
        SourceFile.fromCode("externs.js", "function alert(x) {}")
    );

    JSModule[] modules = new JSModule[moduleSources.length];
    for (int i = 0; i < moduleSources.length; i++) {
      modules[i] = new JSModule("m" + i);
      if (i > 0) {
        modules[i].addDependency(modules[i - 1]);
      }
      modules[i].add(SourceFile.fromCode("input" + i + ".js", moduleSources[i]));
    }

    compiler.initModules(externs, Arrays.asList(modules), options);
    Node root = compiler.parseInputs();
    assertNotNull("Root AST node should not be null after parseInputs", root);
    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();

    CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
        compiler, idGenerator, canModifyExterns);
    motion.process(externsRoot, mainRoot);

    String[] result = new String[modules.length];
    for (int i = 0; i < modules.length; i++) {
      Node moduleRoot = modules[i].getInputs().get(0).getAstRoot(compiler);
      result[i] = compiler.toSource(moduleRoot);
    }
    return result;
  }

  // ==================================================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testStandardMethodMotionBetweenTwoModules() {
    String m0 = "function F() {} F.prototype.bar = function() { return 1; };";
    String m1 = "(new F).bar();";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    // Module 0 should contain stub declarations and the stubbed method
    assertTrue("Module 0 must contain stub declarations",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertTrue("Module 0 should stub F.prototype.bar",
        results[0].contains("F.prototype.bar = " + CrossModuleMethodMotion.STUB_METHOD_NAME + "(0)"));

    // Module 1 should contain unstub call
    assertTrue("Module 1 must unstub F.prototype.bar",
        results[1].contains(CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
    assertTrue("Module 1 must reference unstub id 0",
        results[1].contains("0, function()"));
  }

  @Test(timeout = 4000)
  public void testMultipleMethodsMovedAcrossThreeModules() {
    String m0 = "function F() {}" +
                "F.prototype.a = function() { return 'a'; };" +
                "F.prototype.b = function() { return 'b'; };";
    String m1 = "(new F).a();";
    String m2 = "(new F).b();";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1, m2);

    assertTrue("Module 0 must stub method a",
        results[0].contains("F.prototype.a = " + CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertTrue("Module 0 must stub method b",
        results[0].contains("F.prototype.b = " + CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertTrue("Module 1 must unstub method a",
        results[1].contains("F.prototype.a = " + CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
    assertTrue("Module 2 must unstub method b",
        results[2].contains("F.prototype.b = " + CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
  }

  // ==================================================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testProcessWithNullModuleGraph() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Collections.singletonList(SourceFile.fromCode("externs.js", ""));
    List<SourceFile> inputs = Collections.singletonList(
        SourceFile.fromCode("input.js", "function F() {} F.prototype.m = function() {};"));

    compiler.init(externs, inputs, options);
    Node root = compiler.parseInputs();
    assertNull("ModuleGraph should be null when initialized without modules", compiler.getModuleGraph());

    CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
        compiler, new CrossModuleMethodMotion.IdGenerator(), false);
    // process() must handle null moduleGraph safely without NPE
    motion.process(root.getFirstChild(), root.getLastChild());
  }

  @Test(timeout = 4000)
  public void testProcessWithSingleModuleDoesNotMoveMethods() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Collections.singletonList(SourceFile.fromCode("externs.js", ""));
    JSModule m0 = new JSModule("m0");
    m0.add(SourceFile.fromCode("input0.js",
        "function F() {} F.prototype.bar = function() { return 1; }; (new F).bar();"));

    compiler.initModules(externs, Collections.singletonList(m0), options);
    Node root = compiler.parseInputs();

    CrossModuleMethodMotion motion = new CrossModuleMethodMotion(
        compiler, new CrossModuleMethodMotion.IdGenerator(), false);
    motion.process(root.getFirstChild(), root.getLastChild());

    String output = compiler.toSource(m0.getInputs().get(0).getAstRoot(compiler));
    assertFalse("Single module pass should never inject stub declarations",
        output.contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
  }

  @Test(timeout = 4000)
  public void testUnreferencedMethodIsSkipped() {
    String m0 = "function F() {} F.prototype.unref = function() { return 10; };";
    String m1 = "function G() {}";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    assertFalse("Unreferenced method must not be stubbed",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertFalse("Unreferenced method must not be unstubbed in module 1",
        results[1].contains(CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
  }

  @Test(timeout = 4000)
  public void testNonFunctionPrototypePropertyIsSkipped() {
    String m0 = "function F() {} F.prototype.literalValue = 42;";
    String m1 = "var x = (new F).literalValue;";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    assertFalse("Non-function property should not be stubbed",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
  }

  // ==================================================================================================
  // PARTITION C: Defect-Targeted Branch Zone (Closure Issue 600)
  // ==================================================================================================

  /**
   * Targets Defects4J Issue 600:
   * When G.prototype.bar is declared in Module 1 and only referenced in Module 1,
   * CrossModuleMethodMotion must NOT stub G.prototype.bar in Module 1.
   * Only F.prototype.bar from Module 0 should be moved/stubbed.
   */
  @Test(timeout = 4000)
  public void testIssue600MethodDeclaredInDeeperModuleNotStubbed() {
    String m0 = "function F() {}" +
                "F.prototype.bar = function() { alert(1); };";
    String m1 = "function G() {}" +
                "G.prototype.bar = function() { alert(2); };" +
                "(new F).bar(); (new G).bar();";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    // Module 0 should stub F.prototype.bar
    assertTrue("Module 0 must stub F.prototype.bar",
        results[0].contains("F.prototype.bar = " + CrossModuleMethodMotion.STUB_METHOD_NAME + "(0)"));

    // Module 1 must unstub F.prototype.bar
    assertTrue("Module 1 must unstub F.prototype.bar",
        results[1].contains("F.prototype.bar = " + CrossModuleMethodMotion.UNSTUB_METHOD_NAME));

    // CRITICAL BUG ASSERTION: G.prototype.bar is ALREADY in Module 1.
    // It must NOT be stubbed into JSCompiler_stubMethod!
    assertFalse("G.prototype.bar in module 1 must not be stubbed (Defects4J Issue 600 defect)",
        results[1].contains("G.prototype.bar = " + CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertFalse("G.prototype.bar in module 1 must not be unstubbed",
        results[1].contains("G.prototype.bar = " + CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
  }

  /**
   * Targets Defects4J testIssue600e:
   * G.prototype.bar is declared in Module 2 and called in Module 2.
   * Module 2 must not stub G.prototype.bar.
   */
  @Test(timeout = 4000)
  public void testIssue600eMethodInDeepestModuleNotStubbed() {
    String m0 = "function F() {}" +
                "F.prototype.bar = function() { alert(1); };";
    String m1 = "function G() {}" +
                "(new F).bar();";
    String m2 = "G.prototype.bar = function() { alert(2); };" +
                "(new G).bar();";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1, m2);

    // F.prototype.bar is called in m1, so it is unstubbed in m1
    assertTrue("Module 0 stubs F.prototype.bar",
        results[0].contains("F.prototype.bar = " + CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertTrue("Module 1 unstubs F.prototype.bar",
        results[1].contains("F.prototype.bar = " + CrossModuleMethodMotion.UNSTUB_METHOD_NAME));

    // G.prototype.bar in m2 must NOT be stubbed
    assertFalse("Module 2 must not stub G.prototype.bar",
        results[2].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
  }

  // ==================================================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testMethodReadingClosureVariablesIsNotMoved() {
    String m0 = "var outerVar = 10;" +
                "function F() {}" +
                "F.prototype.bar = function() { return outerVar; };";
    String m1 = "(new F).bar();";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    assertFalse("Method reading closure variable must not be stubbed",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertFalse("Method reading closure variable must not be moved to module 1",
        results[1].contains(CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
  }

  @Test(timeout = 4000)
  public void testGetterAndSetterDefinitionsAreNotMoved() {
    String m0 = "function F() {}" +
                "F.prototype = {" +
                "  get bar() { return 1; }," +
                "  set bar(x) { this.x = x; }" +
                "};";
    String m1 = "var f = new F(); var a = f.bar; f.bar = 2;";

    String[] results = compileAndRunPass(
        new CrossModuleMethodMotion.IdGenerator(), false, m0, m1);

    assertFalse("Getter/setter properties must not be stubbed",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME));
    assertFalse("Getter/setter properties must not be unstubbed",
        results[1].contains(CrossModuleMethodMotion.UNSTUB_METHOD_NAME));
  }

  @Test(timeout = 4000)
  public void testPreGeneratedIdDoesNotReinsertStubDeclarations() {
    CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
    // Simulate that stubs declarations were already emitted by pre-generating an ID
    int preId = idGen.newId();
    assertEquals("First ID generated should be 0", 0, preId);
    assertTrue("idGenerator should report hasGeneratedAnyIds", idGen.hasGeneratedAnyIds());

    String m0 = "function F() {} F.prototype.bar = function() { return 1; };";
    String m1 = "(new F).bar();";

    String[] results = compileAndRunPass(idGen, false, m0, m1);

    // Because hasStubDeclaration was true initially, STUB_DECLARATIONS header is not added
    assertFalse("STUB_DECLARATIONS should not be injected if ids were pre-generated",
        results[0].contains("var JSCompiler_stubMap = [];"));
    // But the method itself gets stubbed with the next id (1)
    assertTrue("Method should be stubbed using next available id 1",
        results[0].contains(CrossModuleMethodMotion.STUB_METHOD_NAME + "(1)"));
  }

  // ==================================================================================================
  // PARTITION E: Object Lifecycle & Contract Integrity
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testIdGeneratorLifecycleAndState() {
    CrossModuleMethodMotion.IdGenerator idGen = new CrossModuleMethodMotion.IdGenerator();
    assertFalse("Initial IdGenerator must return false for hasGeneratedAnyIds",
        idGen.hasGeneratedAnyIds());

    assertEquals("First id must be 0", 0, idGen.newId());
    assertTrue("hasGeneratedAnyIds must be true after newId()", idGen.hasGeneratedAnyIds());

    assertEquals("Second id must be 1", 1, idGen.newId());
    assertEquals("Third id must be 2", 2, idGen.newId());
  }

  @Test(timeout = 4000)
  public void testIdGeneratorSerializationIntegrity() throws Exception {
    CrossModuleMethodMotion.IdGenerator original = new CrossModuleMethodMotion.IdGenerator();
    original.newId();
    original.newId(); // currentId is now 2

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(original);
    }

    CrossModuleMethodMotion.IdGenerator deserialized;
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
      deserialized = (CrossModuleMethodMotion.IdGenerator) ois.readObject();
    }

    assertNotNull("Deserialized object should not be null", deserialized);
    assertTrue("Deserialized generator should retain hasGeneratedAnyIds state",
        deserialized.hasGeneratedAnyIds());
    assertEquals("Deserialized generator must continue sequence from previous state",
        2, deserialized.newId());
  }

  @Test(timeout = 4000)
  public void testConstantDefinitionsAndDiagnosticType() {
    assertEquals("JSCompiler_stubMethod", CrossModuleMethodMotion.STUB_METHOD_NAME);
    assertEquals("JSCompiler_unstubMethod", CrossModuleMethodMotion.UNSTUB_METHOD_NAME);
    assertNotNull(CrossModuleMethodMotion.STUB_DECLARATIONS);
    assertTrue(CrossModuleMethodMotion.STUB_DECLARATIONS.contains("JSCompiler_stubMap"));

    assertNotNull(CrossModuleMethodMotion.NULL_COMMON_MODULE_ERROR);
    assertEquals("JSC_INTERNAL_ERROR_MODULE_DEPEND",
        CrossModuleMethodMotion.NULL_COMMON_MODULE_ERROR.key);
  }
}