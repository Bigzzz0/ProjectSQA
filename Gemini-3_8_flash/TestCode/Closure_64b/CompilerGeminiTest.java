package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.Compiler
 *
 * Core Defect Targeted:
 * - Defects4J / CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs
 *   When ECMASCRIPT5_STRICT is enabled and multiple inputs are compiled, 'use strict' should only appear once in the
 *   concatenated output (at the start of the first input). In the defective version, toSource(cb, inputSeqNum, root)
 *   calls toSource(root, sourceMap) which tags EVERY script as strict without respecting inputSeqNum == 0, causing
 *   repeated "'use strict';" statements, or fails to output 'use strict' when languageIn vs languageOut modes mismatch.
 *
 * Branch & Coverage Coverage Matrix:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - compile(JSSourceFile, JSSourceFile, CompilerOptions) -> single input and extern.
 *    - compile(JSSourceFile[], JSModule[], CompilerOptions) -> multi-module dependency compilation.
 *    - toSource(), toSourceArray(), toSource(JSModule), toSourceArray(JSModule).
 *    - CodeBuilder methods: append(), getLineIndex(), getColumnIndex(), endsWith(), reset(), toString().
 *    - Compiler.IntermediateState: getState() / setState() capture and restore AST and pass configuration.
 *    - PassConfig lifecycle: getPassConfig(), setPassConfig() enforcement of single assignment.
 *    - UniqueNameIdSupplier: nextUniqueNameId() and resetUniqueNameId().
 *    - Subsystem instantiation: getTypeRegistry(), getReverseAbstractInterpreter(), getTypeValidator().
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Empty module list: checkFirstModule -> EMPTY_MODULE_LIST_ERROR.
 *    - Empty root module with multiple modules: checkFirstModule -> EMPTY_ROOT_MODULE_ERROR.
 *    - Duplicate input names across sources and externs: DUPLICATE_INPUT and DUPLICATE_EXTERN_INPUT.
 *    - Circular module dependencies: MODULE_DEPENDENCY_ERROR reporting.
 *    - getSourceLine / getSourceRegion: boundary checks (lineNumber < 1, lineNumber > total lines, unknown source).
 *    - getAstDotGraph: jsRoot == null vs jsRoot populated.
 *
 * 3. Partition C: Defect-Targeted Branch Zone
 *    - testES5StrictUseStrictMultipleInputs_DefectTarget: Verifies that multiple inputs under ECMASCRIPT5_STRICT
 *      do not generate multiple duplicated "'use strict';" statements.
 *    - testES5StrictLanguageInOnly: Verifies strict behavior under ECMASCRIPT5_STRICT input mode.
 *    - areNodesEqualForInlining: ambiguateProperties / disambiguateProperties branch (typed vs untyped equivalence).
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - setErrorManager(null) -> NullPointerException.
 *    - setPassConfig(null) -> NullPointerException; setPassConfig() twice -> IllegalStateException.
 *    - compile() invoked more than once on the same Compiler instance -> IllegalStateException.
 *    - newExternInput() with duplicate name -> IllegalArgumentException.
 *    - replaceIncrementalSourceAst() for non-existent file -> NullPointerException.
 *    - addIncrementalSourceAst() with duplicate name -> IllegalStateException.
 *    - getNodeForCodeInsertion(null) when inputs are empty -> IllegalStateException.
 *    - throwInternalError() wraps message and cause into RuntimeException.
 *
 * 5. Partition E: Performance, Debugging, and Threading Controls
 *    - disableThreads() -> sequential execution via runCallable without spawning Thread.
 *    - runCallableWithLargeStack() with success and exceptional paths.
 *    - Tracer / TracerMode.ALL performance tracking.
 *    - CodeChangeHandler addition, invocation via reportCodeChange(), and removal.
 *    - DevMode.START_AND_END and DevMode.EVERY_PASS sanity check execution.
 * --------------------------------------------------------------------------------------------------------------------
 */

import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class CompilerGeminiTest {

  // ===================================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testCompileSimpleCodeSuccess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(msg) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1; alert(a);");

    Result result = compiler.compile(extern, input, options);

    assertTrue("Compilation should succeed", result.success);
    assertEquals("Should have 0 errors", 0, compiler.getErrorCount());
    assertEquals("Should have 0 warnings", 0, compiler.getWarningCount());
    assertFalse(compiler.hasErrors());
    assertFalse(compiler.hasHaltingErrors());

    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue("Output should contain variable declaration", source.contains("var a=1"));
    assertTrue("Output should contain function call", source.contains("alert(a)"));
  }

  @Test(timeout = 4000)
  public void testCompileModulesAndToSourceVariants() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 10;"));

    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = 20;"));
    m2.addDependency(m1);

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    Result result = compiler.compile(extern, new JSModule[] {m1, m2}, options);

    assertTrue("Module compilation should succeed", result.success);
    assertNotNull("Module graph should be initialized for >1 modules", compiler.getModuleGraph());

    String sourceM1 = compiler.toSource(m1);
    assertTrue("m1 source should contain x", sourceM1.contains("var x=10"));

    String sourceM2 = compiler.toSource(m2);
    assertTrue("m2 source should contain y", sourceM2.contains("var y=20"));

    String[] sourceArrM1 = compiler.toSourceArray(m1);
    assertEquals(1, sourceArrM1.length);
    assertTrue(sourceArrM1[0].contains("var x=10"));

    String[] allSources = compiler.toSourceArray();
    assertEquals(2, allSources.length);
    assertTrue(allSources[0].contains("var x=10"));
    assertTrue(allSources[1].contains("var y=20"));
  }

  @Test(timeout = 4000)
  public void testCompileWithSyntaxErrors() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("bad.js", "var = ;");

    Result result = compiler.compile(extern, input, options);

    assertFalse("Compilation should fail on syntax error", result.success);
    assertTrue("Compiler should report errors", compiler.hasErrors());
    assertTrue("Error count must be greater than zero", compiler.getErrorCount() > 0);
    assertNotNull(compiler.getErrors());
    assertTrue(compiler.getErrors().length > 0);
    assertNotNull(compiler.getMessages());
  }

  @Test(timeout = 4000)
  public void testIntermediateStateCaptureAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var data = 42;");

    compiler.compile(extern, input, options);
    Compiler.IntermediateState state = compiler.getState();
    assertNotNull("Captured state must not be null", state);

    Compiler newCompiler = new Compiler();
    newCompiler.initOptions(options);
    newCompiler.setState(state);

    assertSame("Externs root should be restored", state.externsRoot, newCompiler.externsRoot);
    assertSame("PassConfig state should match restored state", state.passConfigState, newCompiler.getPassConfig().getIntermediateState());
  }

  @Test(timeout = 4000)
  public void testCodeBuilderOperations() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("foo");
    assertEquals(3, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertTrue(cb.endsWith("oo"));
    assertFalse(cb.endsWith("bar"));

    cb.append("\nbar\nbaz");
    assertEquals(2, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertEquals("foo\nbar\nbaz", cb.toString());

    cb.reset();
    assertEquals("Length should reset to 0", 0, cb.getLength());
    assertEquals("Line count is preserved across reset per spec", 2, cb.getLineIndex());
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplierAndReset() {
    Compiler compiler = new Compiler();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testSubsystemLazyInitialization() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.closurePass = true;
    compiler.initOptions(options);

    assertNotNull("JSTypeRegistry should be initialized", compiler.getTypeRegistry());
    assertNotNull("ReverseAbstractInterpreter should be initialized", compiler.getReverseAbstractInterpreter());
    assertNotNull("TypeValidator should be initialized", compiler.getTypeValidator());
    assertNotNull("Default coding convention should be present", compiler.getCodingConvention());
  }

  // ===================================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testEmptyModuleListReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<JSSourceFile> externs = Collections.singletonList(JSSourceFile.fromCode("extern.js", ""));
    List<JSModule> emptyModules = Collections.emptyList();

    compiler.initModules(externs, emptyModules, options);

    assertTrue("Should report errors when module list is empty", compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEmptyRootModuleWithMultipleModulesReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<JSSourceFile> externs = Collections.singletonList(JSSourceFile.fromCode("extern.js", ""));

    JSModule rootModule = new JSModule("root");
    JSModule childModule = new JSModule("child");
    childModule.add(JSSourceFile.fromCode("child.js", "var c = 1;"));
    childModule.addDependency(rootModule);

    compiler.initModules(externs, com.google.common.collect.Lists.newArrayList(rootModule, childModule), options);

    assertTrue("Should report error when root module has no inputs in multi-module setup", compiler.hasErrors());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputNamesReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile in1 = JSSourceFile.fromCode("duplicate.js", "var a = 1;");
    JSSourceFile in2 = JSSourceFile.fromCode("duplicate.js", "var b = 2;");

    compiler.init(new JSSourceFile[] {extern}, new JSSourceFile[] {in1, in2}, options);

    assertTrue("Duplicate inputs should trigger error", compiler.hasErrors());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternNamesReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile ext1 = JSSourceFile.fromCode("extern_dup.js", "");
    JSSourceFile ext2 = JSSourceFile.fromCode("extern_dup.js", "");
    JSSourceFile in = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.init(new JSSourceFile[] {ext1, ext2}, new JSSourceFile[] {in}, options);

    assertTrue("Duplicate externs should trigger error", compiler.hasErrors());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testCircularModuleDependencyReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var y = 2;"));

    m1.addDependency(m2);
    m2.addDependency(m1);

    compiler.initModules(
        Collections.singletonList(JSSourceFile.fromCode("extern.js", "")),
        com.google.common.collect.Lists.newArrayList(m1, m2),
        options);

    assertTrue("Circular dependencies should report error", compiler.hasErrors());
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testSourceLineAndRegionBoundaries() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("source.js", "line1\nline2\nline3\n");
    compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    assertEquals("line1", compiler.getSourceLine("source.js", 1));
    assertEquals("line2", compiler.getSourceLine("source.js", 2));
    assertNull("Line 0 is invalid (1-based)", compiler.getSourceLine("source.js", 0));
    assertNull("Negative line is invalid", compiler.getSourceLine("source.js", -10));
    assertNull("Line beyond source length should return null", compiler.getSourceLine("source.js", 100));
    assertNull("Unknown source should return null", compiler.getSourceLine("non_existent.js", 1));

    Region region = compiler.getSourceRegion("source.js", 2);
    assertNotNull("Region for valid line should not be null", region);
    assertNull("Region for non-existent source should be null", compiler.getSourceRegion("non_existent.js", 1));
    assertNull("Region for 0th line should be null", compiler.getSourceRegion("source.js", 0));
  }

  @Test(timeout = 4000)
  public void testAstDotGraphBoundaries() throws IOException {
    Compiler compiler = new Compiler();
    assertEquals("Dot graph before compilation should be empty", "", compiler.getAstDotGraph());

    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var x = 10;");
    compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    String dotGraph = compiler.getAstDotGraph();
    assertNotNull(dotGraph);
    assertTrue("Graph should contain digraph keyword", dotGraph.contains("digraph"));
  }

  // ===================================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ===================================================================================================================

  /**
   * Directly targets CommandLineRunnerTest::testES5StrictUseStrictMultipleInputs.
   * In ECMASCRIPT5_STRICT mode, 'use strict' must be printed only once for the first input file.
   * Defective implementation emits 'use strict' repeatedly for each input or omits it when checking languageOut.
   */
  @Test(timeout = 4000)
  public void testES5StrictUseStrictMultipleInputs_DefectTarget() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    options.setLanguageOut(LanguageMode.ECMASCRIPT5_STRICT);

    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("extern.js", "")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("input1.js", "var x = 1;"),
        JSSourceFile.fromCode("input2.js", "var y = 2;")
    };

    compiler.compile(externs, inputs, options);
    String source = compiler.toSource();

    assertNotNull("Compiled source should not be null", source);
    int firstStrict = source.indexOf("'use strict'");
    assertTrue("Expected compiled source to contain 'use strict'", firstStrict != -1);

    int secondStrict = source.indexOf("'use strict'", firstStrict + "'use strict'".length());
    assertEquals("use strict should only appear once across multiple inputs in ES5 strict mode", -1, secondStrict);
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInliningTypedBranch() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = compiler.parseTestCode("var x = 1;");
    Node n2 = compiler.parseTestCode("var x = 1;");

    options.ambiguateProperties = false;
    options.disambiguateProperties = false;
    assertTrue("Without property (dis)ambiguation, standard equivalence is used",
        compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    assertTrue("With property ambiguation enabled, typed equivalence is used",
        compiler.areNodesEqualForInlining(n1, n2));
  }

  // ===================================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ===================================================================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetErrorManagerNullThrowsException() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetPassConfigNullThrowsException() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetPassConfigTwiceThrowsException() {
    Compiler compiler = new Compiler();
    PassConfig config = compiler.createPassConfigInternal();
    compiler.setPassConfig(config);
    compiler.setPassConfig(config);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCompileCalledTwiceThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.compile(extern, input, options);
    // Compiling a second time on the same instance is prohibited
    compiler.compile(extern, input, options);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNewExternInputConflictThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(new JSSourceFile[] {}, new JSSourceFile[] {JSSourceFile.fromCode("in.js", "var a = 1;")}, options);

    compiler.newExternInput("extern_common.js");
    compiler.newExternInput("extern_common.js");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAddIncrementalSourceAstDuplicateThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(new JSSourceFile[] {}, new JSSourceFile[] {JSSourceFile.fromCode("in.js", "var a = 1;")}, options);

    JsAst ast = new JsAst(JSSourceFile.fromCode("inc.js", "var x = 1;"));
    compiler.addIncrementalSourceAst(ast);
    compiler.addIncrementalSourceAst(ast);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testReplaceIncrementalSourceAstNotFoundThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(new JSSourceFile[] {}, new JSSourceFile[] {JSSourceFile.fromCode("in.js", "var a = 1;")}, options);

    JsAst ast = new JsAst(JSSourceFile.fromCode("unseen.js", "var x = 1;"));
    compiler.replaceIncrementalSourceAst(ast);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetNodeForCodeInsertionNoInputsThrowsException() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(timeout = 4000)
  public void testThrowInternalErrorPreservesCause() {
    Compiler compiler = new Compiler();
    Exception cause = new IOException("Underlying disk IO failure");
    try {
      compiler.throwInternalError("Pass failed", cause);
      fail("Should have thrown RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      assertTrue(e.getMessage().contains("Pass failed"));
      assertSame(cause, e.getCause());
    }
  }

  // ===================================================================================================================
  // Partition E: Object Lifecycle, Threading, Diagnostic and Custom Pass Integrity
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testDisableThreadsExecutesSequentially() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();

    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("no_thread.js", "var x = 100;");
    Result result = compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    assertTrue(result.success);
    assertTrue(compiler.toSource().contains("var x=100"));
  }

  @Test(timeout = 4000)
  public void testRunCallableWithLargeStack() {
    String expected = "computed_value";
    String actual = Compiler.runCallableWithLargeStack(new Callable<String>() {
      @Override
      public String call() throws Exception {
        return expected;
      }
    });
    assertEquals(expected, actual);
  }

  @Test(timeout = 4000)
  public void testRunCallablePropagatesException() {
    try {
      Compiler.runCallableWithLargeStack(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
          throw new IllegalStateException("Simulated thread error");
        }
      });
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getCause() instanceof IllegalStateException);
      assertEquals("Simulated thread error", e.getCause().getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testCodeChangeHandlerLifecycle() {
    Compiler compiler = new Compiler();
    final int[] changeCounter = new int[1];
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changeCounter[0]++;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals("Handler should be notified of code change", 1, changeCounter[0]);

    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    assertEquals("Removed handler should no longer receive notifications", 1, changeCounter[0]);
  }

  @Test(timeout = 4000)
  public void testTracingAndPerformanceTracker() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.tracer = TracerMode.ALL;

    JSSourceFile input = JSSourceFile.fromCode("trace.js", "var a = 1;");
    Result result = compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    assertTrue(result.success);
    assertNotNull("Performance tracker must be initialized when tracer mode is ON", compiler.tracker);
  }

  @Test(timeout = 4000)
  public void testSanityCheckModes() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.devMode = DevMode.START_AND_END;

    JSSourceFile input = JSSourceFile.fromCode("devmode.js", "var z = 5;");
    Result result = compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);
    assertTrue(result.success);

    Compiler compilerEveryPass = new Compiler();
    CompilerOptions optionsEveryPass = new CompilerOptions();
    optionsEveryPass.devMode = DevMode.EVERY_PASS;
    Result resultEveryPass = compilerEveryPass.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, optionsEveryPass);
    assertTrue(resultEveryPass.success);
  }

  @Test(timeout = 4000)
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node nodeSynthetic1 = compiler.parseSyntheticCode("var alpha = 1;");
    assertNotNull(nodeSynthetic1);

    Node nodeSynthetic2 = compiler.parseSyntheticCode("synth_file.js", "var beta = 2;");
    assertNotNull(nodeSynthetic2);

    Node nodeTest = compiler.parseTestCode("var gamma = 3;");
    assertNotNull(nodeTest);

    assertNotNull(compiler.getInput("synth_file.js"));
    assertNotNull(compiler.getInput(" [synthetic] "));
    assertNotNull(compiler.getInput(" [testcode] "));
  }

  @Test(timeout = 4000)
  public void testPrintStreamConstructorAndLogging() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);
    CompilerOptions options = new CompilerOptions();

    JSSourceFile input = JSSourceFile.fromCode("ps.js", "var logged = 1;");
    compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    Compiler.setLoggingLevel(Level.OFF);
    compiler.addToDebugLog("Debug message verification");
    Result result = compiler.getResult();
    assertNotNull(result.debugLog);
    assertTrue("Debug log should contain appended string", result.debugLog.contains("Debug message verification"));
  }

  @Test(timeout = 4000)
  public void testRegExpGlobalReferencesFlag() {
    Compiler compiler = new Compiler();
    assertTrue("Default hasRegExpGlobalReferences should be true", compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse("hasRegExpGlobalReferences should update to false", compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testInputDelimiterPrinting() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.printInputDelimiter = true;
    options.inputDelimiter = "// [%name% : %num%]";

    JSSourceFile input = JSSourceFile.fromCode("fileA.js", "var a = 1;");
    compiler.compile(new JSSourceFile[] {}, new JSSourceFile[] {input}, options);

    String source = compiler.toSource();
    assertTrue("Source should include customized delimiter", source.contains("// [fileA.js : 0]"));
  }
}