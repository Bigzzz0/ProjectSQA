/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Test Target: com.google.javascript.jscomp.Compiler
 *
 * Partition A: Core Functional Logic & State Transitions
 *  - Basic compile lifecycle: init, parse, check, optimize, toSource
 *  - Multi-input and JSModule compilation (module graphs, dependency chain)
 *  - CodeBuilder: line/col metrics, appending, resetting, endsWith
 *  - Compiler State preservation and restoration (IntermediateState)
 *  - AST parsing helpers: parseTestCode, parseSyntheticCode
 *  - Unique name id generator and supplier reset
 *  - Code change handlers: registering, triggering, removing
 *  - Node equality check for inlining across property disambiguation settings
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *  - setProgress: clamp boundaries (< 0.0 -> 0.0, > 1.0 -> 1.0, normal values)
 *  - getSourceLine / getSourceRegion with invalid (< 1) line numbers and non-existent sources
 *  - Empty modules and createFillFileName behavior
 *  - getAstDotGraph with uninitialized vs compiled AST
 *  - toSource with empty inputs / modules
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 *  - CommandLineRunnerTest::testDependencySortingWhitespaceMode
 *    Target: Dependency sorting when WHITESPACE_ONLY mode or skipAllPasses / closurePass=false
 *    Branch: Compiler#parseInputs() checking options.dependencyOptions.needsManagement()
 *    In the defective version, dependency management was gated on (!options.skipAllPasses && options.closurePass),
 *    causing whitespace/pass-skipped compilations to omit topological dependency sorting.
 *
 * Partition D: Exception & Defensive Guard Paths
 *  - Null guards: setErrorManager(null), setPassConfig(null)
 *  - Double initialization guards: calling compile() twice, setPassConfig() twice
 *  - Pass management guards: startPass() without ending previous, endPass() without start
 *  - Insertion point guards: getNodeForCodeInsertion() on empty inputs / modules
 *  - Input integrity guards: duplicate input names, duplicate extern inputs, cyclic modules
 *  - Synthetic & extern input removal: removing non-extern input via removeExternInput
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *  - LanguageMode branches: ECMASCRIPT3, ECMASCRIPT5, ECMASCRIPT5_STRICT
 *  - Logging, ErrorManager bindings (PrintStreamErrorManager vs LoggerErrorManager)
 *  - Thread execution harness (runCallable, runCallableWithLargeStack, disableThreads)
 *  - Library injection mechanism: ensureLibraryInjected and duplicate injection cache
 * ---------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompileSingleInputAndToSource() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile extern = SourceFile.fromCode("externs.js", "function alert(x) {}");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1 + 2; alert(a);");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());

    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue(source.contains("alert(3)") || source.contains("var a=3"));
  }

  @Test(timeout = 4000)
  public void testCompileModulesAndModuleSourceArray() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile extern = SourceFile.fromCode("externs.js", "");

    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1_1.js", "var m1_var = 1;"));
    m1.add(SourceFile.fromCode("m1_2.js", "var m1_var2 = 2;"));

    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2_1.js", "var m2_var = 3;"));
    m2.addDependency(m1);

    Result result = compiler.compileModules(
        Lists.newArrayList(extern), Lists.newArrayList(m1, m2), options);

    assertTrue(result.success);
    assertNotNull(compiler.getModuleGraph());
    assertEquals(2, compiler.getModuleGraph().getModuleCount());

    String[] m1Sources = compiler.toSourceArray(m1);
    assertEquals(2, m1Sources.length);
    assertTrue(m1Sources[0].contains("m1_var"));
    assertTrue(m1Sources[1].contains("m1_var2"));

    String m2Source = compiler.toSource(m2);
    assertTrue(m2Source.contains("m2_var"));
  }

  @Test(timeout = 4000)
  public void testCodeBuilderMetricsAndReset() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertEquals("", cb.toString());
    assertFalse(cb.endsWith(";"));

    cb.append("var a = 1;\nvar b = 2;");
    assertEquals(21, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(10, cb.getColumnIndex());
    assertTrue(cb.endsWith("2;"));
    assertFalse(cb.endsWith("1;"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals("", cb.toString());
  }

  @Test(timeout = 4000)
  public void testSaveAndRestoreIntermediateState() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 10;");

    compiler.compile(extern, input, options);
    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    compiler.setState(state);
    assertNotNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();

    Node testCodeNode = compiler.parseTestCode("function foo() { return 42; }");
    assertNotNull(testCodeNode);
    assertEquals(Token.SCRIPT, testCodeNode.getType());

    Node synthNode = compiler.parseSyntheticCode("syntheticName.js", "var syn = true;");
    assertNotNull(synthNode);

    Node synthDirect = compiler.parseSyntheticCode("var bar = 1;");
    assertNotNull(synthDirect);
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplierAndReset() {
    Compiler compiler = new Compiler();
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testCodeChangeHandlerReporting() {
    Compiler compiler = new Compiler();
    final int[] changeCount = new int[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changeCount[0]++;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals(1, changeCount[0]);

    compiler.reportCodeChange();
    assertEquals(2, changeCount[0]);

    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals(2, changeCount[0]);
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInlining() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = Node.newString("prop");
    Node n2 = Node.newString("prop");
    Node n3 = Node.newString("other");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testSetProgressBoundaries() {
    Compiler compiler = new Compiler();

    compiler.setProgress(0.5);
    assertEquals(0.5, compiler.getProgress(), 0.0001);

    compiler.setProgress(-0.01);
    assertEquals(0.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(-100.0);
    assertEquals(0.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(1.01);
    assertEquals(1.0, compiler.getProgress(), 0.0001);

    compiler.setProgress(50.0);
    assertEquals(1.0, compiler.getProgress(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testGetSourceLineAndRegionBoundaries() {
    Compiler compiler = new Compiler();

    assertNull(compiler.getSourceLine("nonexistent.js", 1));
    assertNull(compiler.getSourceLine("nonexistent.js", 0));
    assertNull(compiler.getSourceLine("nonexistent.js", -1));
    assertNull(compiler.getSourceRegion("nonexistent.js", 1));
    assertNull(compiler.getSourceRegion("nonexistent.js", 0));
    assertNull(compiler.getSourceRegion("nonexistent.js", -10));

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile src = SourceFile.fromCode("test.js", "line1();\nline2();\nline3();");
    compiler.compile(ext, src, new CompilerOptions());

    assertEquals("line1();", compiler.getSourceLine("test.js", 1));
    assertEquals("line2();", compiler.getSourceLine("test.js", 2));
    assertNull(compiler.getSourceLine("test.js", 0));
    assertNull(compiler.getSourceLine("test.js", -1));
    assertNotNull(compiler.getSourceRegion("test.js", 2));
    assertNull(compiler.getSourceRegion("test.js", 0));
  }

  @Test(timeout = 4000)
  public void testCreateFillFileName() {
    assertEquals("[my_module]", Compiler.createFillFileName("my_module"));
    assertEquals("[]", Compiler.createFillFileName(""));
  }

  @Test(timeout = 4000)
  public void testGetAstDotGraphNullAndPopulated() throws IOException {
    Compiler compiler = new Compiler();
    assertEquals("", compiler.getAstDotGraph());

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile src = SourceFile.fromCode("src.js", "var a = 1;");
    compiler.compile(ext, src, new CompilerOptions());

    String dot = compiler.getAstDotGraph();
    assertNotNull(dot);
    assertTrue(dot.contains("digraph"));
  }

  @Test(timeout = 4000)
  public void testToSourceEmptyModule() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule emptyMod = new JSModule("emptyMod");

    compiler.initModules(
        Lists.newArrayList(SourceFile.fromCode("ext.js", "")),
        Lists.newArrayList(emptyMod),
        options);

    // Empty module should produce an empty string or empty array
    JSModule cleanEmptyMod = new JSModule("cleanEmpty");
    assertEquals("", compiler.toSource(cleanEmptyMod));
    assertEquals(0, compiler.toSourceArray(cleanEmptyMod).length);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets CommandLineRunnerTest::testDependencySortingWhitespaceMode defect.
   * In defective versions, dependency reordering was only executed when
   * (!options.skipAllPasses && options.closurePass) was true, causing
   * WHITESPACE_ONLY / pass-skipped modes to skip topological dependency sorting.
   */
  @Test(timeout = 4000)
  public void testDependencySortingWhitespaceModeDefect() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    // Configure WHITESPACE_ONLY compilation mode
    CompilationLevel.WHITESPACE_ONLY.setOptionsForCompilationLevel(options);

    // Configure dependency sorting
    DependencyOptions depOptions = new DependencyOptions();
    depOptions.setDependencySorting(true);
    options.setDependencyOptions(depOptions);

    SourceFile extern = SourceFile.fromCode("extern.js", "");
    // input1 requires 'beer', input2 provides 'beer'.
    // Provided input must be sorted BEFORE the dependent input.
    SourceFile input1 = SourceFile.fromCode("input1.js", "goog.require('beer'); var consumer = 1;");
    SourceFile input2 = SourceFile.fromCode("input2.js", "goog.provide('beer'); var beer = 1;");

    Result result = compiler.compile(
        Lists.newArrayList(extern),
        Lists.newArrayList(input1, input2),
        options);

    assertTrue("Compilation should succeed", result.success);

    List<CompilerInput> orderedInputs = compiler.getInputsInOrder();
    assertEquals("Should contain 2 source inputs", 2, orderedInputs.size());
    assertEquals(
        "Dependency sorting in whitespace mode must place providing input first",
        "input2.js",
        orderedInputs.get(0).getName());
    assertEquals(
        "Dependency sorting in whitespace mode must place requiring input second",
        "input1.js",
        orderedInputs.get(1).getName());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetErrorManagerNull() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetPassConfigNull() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testSetPassConfigTwice() {
    Compiler compiler = new Compiler();
    PassConfig passes = compiler.getPassConfig();
    assertNotNull(passes);
    compiler.setPassConfig(passes);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testCompileTwiceThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile input = SourceFile.fromCode("input.js", "var a = 1;");

    compiler.compile(extern, input, options);
    // Calling compile a second time must fail fast
    compiler.compile(extern, input, options);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testStartPassWithoutEndingPrevious() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    compiler.startPass("pass1");
    compiler.startPass("pass2");
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndPassWithoutStarting() {
    Compiler compiler = new Compiler();
    compiler.endPass();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionNoInputs() {
    Compiler compiler = new Compiler();
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionEmptyModule() {
    Compiler compiler = new Compiler();
    JSModule emptyMod = new JSModule("mod");
    compiler.getNodeForCodeInsertion(emptyMod);
  }

  @Test(timeout = 4000)
  public void testDuplicateSourceInputsReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile extern = SourceFile.fromCode("externs.js", "");
    SourceFile in1 = SourceFile.fromCode("same.js", "var x = 1;");
    SourceFile in2 = SourceFile.fromCode("same.js", "var y = 2;");

    Result result = compiler.compile(
        Lists.newArrayList(extern), Lists.newArrayList(in1, in2), options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals(Compiler.DUPLICATE_INPUT.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternInputsReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile ext1 = SourceFile.fromCode("ext.js", "var a;");
    SourceFile ext2 = SourceFile.fromCode("ext.js", "var b;");
    SourceFile in = SourceFile.fromCode("in.js", "var x = 1;");

    Result result = compiler.compile(
        Lists.newArrayList(ext1, ext2), Lists.newArrayList(in), options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals(Compiler.DUPLICATE_EXTERN_INPUT.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testModuleDependencyCycleReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    JSModule m1 = new JSModule("m1");
    m1.add(SourceFile.fromCode("m1.js", "var x = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(SourceFile.fromCode("m2.js", "var y = 2;"));

    m1.addDependency(m2);
    m2.addDependency(m1);

    Result result = compiler.compileModules(
        Lists.newArrayList(ext), Lists.newArrayList(m1, m2), options);

    assertFalse(result.success);
    assertTrue(compiler.getErrorCount() > 0);
    assertEquals(Compiler.MODULE_DEPENDENCY_ERROR.key, compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testRemoveNonExternInputThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, options);
    compiler.removeExternInput(new InputId("in.js"));
  }

  @Test(timeout = 4000)
  public void testNewExternInputAndConflictingNameGuard() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, options);
    CompilerInput synthExt = compiler.newExternInput("synthetic.js");
    assertNotNull(synthExt);
    assertTrue(synthExt.isExtern());

    try {
      compiler.newExternInput("synthetic.js");
      fail("Expected IllegalArgumentException on conflicting extern input name");
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Conflicting externs name"));
    }
  }

  @Test(timeout = 4000)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    try {
      compiler.throwInternalError("synthetic internal error", new IOException("disk failure"));
      fail("Expected RuntimeException");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("INTERNAL COMPILER ERROR"));
      assertTrue(expected.getMessage().contains("synthetic internal error"));
      assertNotNull(expected.getCause());
      assertTrue(expected.getCause() instanceof IOException);
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLanguageModeAndEcmaScript5Flags() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());
    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT3, compiler.languageMode());

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5, compiler.languageMode());

    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(CompilerOptions.LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
  }

  @Test(timeout = 4000)
  public void testCustomPrintStreamAndErrorManagerInit() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, new CompilerOptions());
    assertTrue(compiler.getErrorManager() instanceof PrintStreamErrorManager);
  }

  @Test(timeout = 4000)
  public void testCustomErrorManagerConstructor() {
    BasicErrorManager customManager = new LoggerErrorManager(Logger.getLogger("testLogger"));
    Compiler compiler = new Compiler(customManager);
    assertSame(customManager, compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testLoggingLevelSetting() {
    Compiler.setLoggingLevel(Level.FINE);
    Compiler.setLoggingLevel(Level.INFO);
  }

  @Test(timeout = 4000)
  public void testThreadExecutionHarness() {
    String result = Compiler.runCallableWithLargeStack(new Callable<String>() {
      @Override
      public String call() {
        return "completed";
      }
    });
    assertEquals("completed", result);

    Integer normalRun = Compiler.runCallable(new Callable<Integer>() {
      @Override
      public Integer call() {
        return 99;
      }
    }, false, false);
    assertEquals(Integer.valueOf(99), normalRun);

    try {
      Compiler.runCallable(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          throw new IllegalStateException("harness error");
        }
      }, false, false);
      fail("Expected RuntimeException wrapping inner exception");
    } catch (RuntimeException expected) {
      assertTrue(expected.getCause() instanceof IllegalStateException);
    }
  }

  @Test(timeout = 4000)
  public void testDisableThreadsCompile() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    Result result = compiler.compile(ext, in, new CompilerOptions());
    assertTrue(result.success);
    assertEquals("var a=1;", compiler.toSource().trim());
  }

  @Test(timeout = 4000)
  public void testEnsureLibraryInjectedCaching() {
    Compiler compiler = new Compiler();
    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, new CompilerOptions());

    Node injectedBase1 = compiler.ensureLibraryInjected("base");
    assertNotNull(injectedBase1);

    // Repeated call must return null as it is already injected
    Node injectedBase2 = compiler.ensureLibraryInjected("base");
    assertNull(injectedBase2);
  }

  @Test(timeout = 4000)
  public void testHasRegExpGlobalReferencesFlag() {
    Compiler compiler = new Compiler();
    assertTrue(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(true);
    assertTrue(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testCssRenamingMapBinding() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());

    CssRenamingMap map = new CssRenamingMap() {
      @Override
      public String get(String value) {
        return "prefixed_" + value;
      }

      @Override
      public CssRenamingMap.Style getStyle() {
        return CssRenamingMap.Style.BY_WHOLE;
      }
    };

    compiler.setCssRenamingMap(map);
    assertSame(map, compiler.getCssRenamingMap());
  }

  @Test(timeout = 4000)
  public void testIsInliningForbiddenPolicies() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    assertFalse(compiler.isInliningForbidden());

    options.propertyRenaming = PropertyRenamingPolicy.HEURISTIC;
    assertTrue(compiler.isInliningForbidden());

    options.propertyRenaming = PropertyRenamingPolicy.AGGRESSIVE_HEURISTIC;
    assertTrue(compiler.isInliningForbidden());
  }

  @Test(timeout = 4000)
  public void testCustomPassExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    final boolean[] passExecuted = new boolean[1];

    CompilerPass dummyPass = new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        passExecuted[0] = true;
      }
    };

    options.addCustomPass(CustomPassExecutionTime.BEFORE_CHECKS, dummyPass);

    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, options);
    assertTrue(passExecuted[0]);
  }

  @Test(timeout = 4000)
  public void testBuildKnownSymbolTable() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile ext = SourceFile.fromCode("ext.js", "");
    SourceFile in = SourceFile.fromCode("in.js", "var a = 1; function f(x) { return x; }");

    compiler.compile(ext, in, options);
    SymbolTable table = compiler.buildKnownSymbolTable();
    assertNotNull(table);
  }
}