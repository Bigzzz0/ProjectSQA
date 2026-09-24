/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * TARGET: com.google.javascript.jscomp.Compiler
 * DEFECT TARGET: CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet
 *
 * KEY DECISION BRANCHES & COVERAGE TARGETS:
 * 1. initOptions:
 *    - errorManager null vs non-null; outStream null vs non-null (LoggerErrorManager vs PrintStreamErrorManager)
 *    - options.checkTypes (enables vs disables vs default with parse error suppression)
 *    - options.checkGlobalThisLevel (isOn vs off)
 *    - options.checkSymbols vs warningsGuard.disables(CHECK_VARIABLES) (DEFECT HOTSPOT)
 * 2. Module Validation & Inputs:
 *    - modules.isEmpty() -> EMPTY_MODULE_LIST_ERROR
 *    - modules.get(0).getInputs().isEmpty() && modules.size() > 1 -> EMPTY_ROOT_MODULE_ERROR
 *    - fillEmptyModules: empty input list handling
 *    - JSModuleGraph dependency error (cycle) -> MODULE_DEPENDENCY_ERROR
 *    - Duplicate extern input -> DUPLICATE_EXTERN_INPUT
 *    - Duplicate JS input -> DUPLICATE_INPUT
 * 3. Lifecycle & Compilation State:
 *    - compile() called when jsRoot != null -> IllegalStateException
 *    - disableThreads() -> runInCompilerThread without thread spawning
 *    - intermediate state capturing & restoring via getState() / setState()
 *    - PassConfig: null guard, re-assignment IllegalStateException guard
 * 4. Code Generation & Utilities:
 *    - CodeBuilder: line/col indices, reset, endsWith, append with/without newlines
 *    - toSource(), toSourceArray(), toSource(JSModule), toSourceArray(JSModule)
 *    - toSource with printInputDelimiter enabled
 *    - getSourceLine / getSourceRegion: boundary checks (lineNumber < 1, unknown source, valid source)
 *    - newExternInput: conflict check -> IllegalArgumentException
 *    - getNodeForCodeInsertion: null module with empty/non-empty inputs, empty module inputs check
 *    - getAstDotGraph: null jsRoot vs populated jsRoot
 *    - areNodesEqualForInlining: typed equivalent branch vs untyped branch
 * -------------------------------------------------------------------------------------------------
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.*;

public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompileBasicScriptSuccess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "function alert(x) {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1 + 2;");

    Result result = compiler.compile(extern, input, options);

    assertTrue("Compilation should succeed", result.success);
    assertEquals("Error count should be 0", 0, compiler.getErrorCount());
    assertEquals("Warning count should be 0", 0, compiler.getWarningCount());
    assertNotNull("Root node must not be null after compile", compiler.getRoot());
    assertTrue("Generated code must contain var x", compiler.toSource().contains("var x=3"));
  }

  @Test(timeout = 4000)
  public void testCompileWithoutThreads() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var z = 42;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.toSource().contains("var z=42"));
  }

  @Test(timeout = 4000)
  public void testToSourceArraySingleAndModule() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSModule module = new JSModule("mod1");
    JSSourceFile f1 = JSSourceFile.fromCode("f1.js", "var a = 1;");
    JSSourceFile f2 = JSSourceFile.fromCode("f2.js", "var b = 2;");
    module.add(f1);
    module.add(f2);

    compiler.compile(new JSSourceFile[0], new JSModule[] { module }, options);

    String[] sources = compiler.toSourceArray();
    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var a=1"));
    assertTrue(sources[1].contains("var b=2"));

    String moduleSource = compiler.toSource(module);
    assertTrue(moduleSource.contains("var a=1"));
    assertTrue(moduleSource.contains("var b=2"));

    String[] moduleSources = compiler.toSourceArray(module);
    assertEquals(2, moduleSources.length);
  }

  @Test(timeout = 4000)
  public void testToSourceWithInputDelimiter() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.printInputDelimiter = true;
    options.inputDelimiter = "// FILE: %name% (%num%)";

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var k = 99;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    String source = compiler.toSource();
    assertTrue(source.contains("// FILE: test.js (0)"));
  }

  @Test(timeout = 4000)
  public void testGetSourceLineAndRegion() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var line1 = 1;\nvar line2 = 2;\nvar line3 = 3;");

    compiler.compile(extern, input, options);

    assertNull(compiler.getSourceLine("input.js", 0));
    assertNull(compiler.getSourceLine("input.js", -1));
    assertNull(compiler.getSourceLine("non_existent.js", 1));

    String line2 = compiler.getSourceLine("input.js", 2);
    assertEquals("var line2 = 2;", line2);

    assertNull(compiler.getSourceRegion("input.js", 0));
    assertNull(compiler.getSourceRegion("non_existent.js", 1));
    Region region = compiler.getSourceRegion("input.js", 2);
    assertNotNull(region);
    assertTrue(region.getSourceExcerpt().contains("var line2 = 2;"));
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplierAndReset() {
    Compiler compiler = new Compiler();
    Supplier<String> idSupplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", idSupplier.get());
    assertEquals("1", idSupplier.get());
    assertEquals("2", idSupplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", idSupplier.get());
  }

  @Test(timeout = 4000)
  public void testLanguageModeAndEcma5Flags() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    compiler.initOptions(options);
    assertFalse(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT3, compiler.languageMode());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT5, compiler.languageMode());

    options.setLanguageIn(LanguageMode.ECMASCRIPT5_STRICT);
    assertTrue(compiler.acceptEcmaScript5());
    assertEquals(LanguageMode.ECMASCRIPT5_STRICT, compiler.languageMode());
  }

  @Test(timeout = 4000)
  public void testCodeChangeHandlerRegistration() {
    Compiler compiler = new Compiler();
    final boolean[] changed = new boolean[] { false };
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changed[0] = true;
      }
    };

    compiler.addChangeHandler(handler);
    compiler.reportCodeChange();
    assertTrue(changed[0]);

    changed[0] = false;
    compiler.removeChangeHandler(handler);
    compiler.reportCodeChange();
    assertFalse(changed[0]);
  }

  @Test(timeout = 4000)
  public void testIntermediateStateSaveAndRestore() {
    Compiler compiler1 = new Compiler();
    CompilerOptions options1 = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var s = 'state';");
    compiler1.compile(extern, input, options1);

    Compiler.IntermediateState state = compiler1.getState();
    assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options1);
    compiler2.setState(state);

    assertEquals(compiler1.getRoot(), compiler2.getRoot());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testCodeBuilderMetrics() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("foo\nbar\nbaz");
    assertEquals(11, cb.getLength());
    assertEquals(2, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertTrue(cb.endsWith("baz"));
    assertFalse(cb.endsWith("bar"));
    assertFalse(cb.endsWith("longer_than_buffer_text"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex()); // line count preserved after reset
    assertEquals("", cb.toString());
  }

  @Test(timeout = 4000)
  public void testEmptyModuleListReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<JSModule> modules = new ArrayList<JSModule>();

    compiler.initModules(new ArrayList<JSSourceFile>(), modules, options);
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEmptyRootModuleInMultiModuleReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule mod1 = new JSModule("root");
    JSModule mod2 = new JSModule("m2");
    mod2.add(JSSourceFile.fromCode("m2.js", "var m2 = 1;"));

    List<JSModule> modules = new ArrayList<JSModule>();
    modules.add(mod1);
    modules.add(mod2);

    compiler.initModules(new ArrayList<JSSourceFile>(), modules, options);
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputsDetected() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile input1 = JSSourceFile.fromCode("dup.js", "var a = 1;");
    JSSourceFile input2 = JSSourceFile.fromCode("dup.js", "var b = 2;");

    compiler.compile(new JSSourceFile[0], new JSSourceFile[] { input1, input2 }, options);
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternInputsDetected() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile ext1 = JSSourceFile.fromCode("ext.js", "function f() {}");
    JSSourceFile ext2 = JSSourceFile.fromCode("ext.js", "function g() {}");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");

    compiler.compile(new JSSourceFile[] { ext1, ext2 }, new JSSourceFile[] { input }, options);
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testCircularModuleDependenciesDetected() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule mod1 = new JSModule("m1");
    JSModule mod2 = new JSModule("m2");
    mod1.add(JSSourceFile.fromCode("m1.js", "var m1 = 1;"));
    mod2.add(JSSourceFile.fromCode("m2.js", "var m2 = 2;"));

    mod1.addDependency(mod2);
    mod2.addDependency(mod1);

    List<JSModule> modules = new ArrayList<JSModule>();
    modules.add(mod1);
    modules.add(mod2);

    compiler.initModules(new ArrayList<JSSourceFile>(), modules, options);
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testToSourceEmptyModule() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    JSModule emptyMod = new JSModule("empty");
    assertEquals("", compiler.toSource(emptyMod));
    assertEquals(0, compiler.toSourceArray(emptyMod).length);
  }

  @Test(timeout = 4000)
  public void testAstDotGraphEmptyBeforeCompile() throws IOException {
    Compiler compiler = new Compiler();
    assertEquals("", compiler.getAstDotGraph());
  }

  @Test(timeout = 4000)
  public void testAstDotGraphPopulatedAfterCompile() throws IOException {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "function foo() { return 1; } foo();");

    compiler.compile(extern, input, options);
    String dot = compiler.getAstDotGraph();
    assertNotNull(dot);
    assertTrue(dot.contains("digraph"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // (Defects4J: CommandLineRunnerTest::testCheckSymbolsOverrideForQuiet)
  // =========================================================================

  @Test(timeout = 4000)
  public void testCheckSymbolsExplicitlyEnabledUnderQuietLevel() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    WarningLevel.QUIET.setOptionsForWarningLevel(options);
    options.checkSymbols = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = unknownUndefinedVar;");

    compiler.compile(extern, input, options);

    assertTrue("When checkSymbols is explicitly enabled, symbol errors/warnings must not be suppressed by QUIET mode",
        compiler.getErrorCount() > 0 || compiler.getWarningCount() > 0);
  }

  @Test(timeout = 4000)
  public void testCheckSymbolsDisabledSilencesSymbolErrors() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    options.checkSymbols = false;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = undeclaredVariable;");

    compiler.compile(extern, input, options);

    assertEquals("Disabling checkSymbols must silence missing variable warnings/errors",
        0, compiler.getErrorCount());
    assertEquals("Disabling checkSymbols must silence missing variable warnings/errors",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCheckSymbolsDefaultReportsSymbolErrors() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkSymbols = true;

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = undeclaredVariable;");

    compiler.compile(extern, input, options);

    assertTrue("Enabling checkSymbols without quiet guard must flag undefined symbol",
        compiler.getErrorCount() > 0 || compiler.getWarningCount() > 0);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCompileCannotBeCalledTwice() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 1;");

    compiler.compile(extern, input, options);
    compiler.compile(extern, input, options);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetErrorManagerNullThrowsNpe() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetPassConfigNullThrowsNpe() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetPassConfigReassignmentThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    PassConfig passConfig = new DefaultPassConfig(options);
    compiler.setPassConfig(passConfig);
    compiler.setPassConfig(passConfig);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNewExternInputConflictThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("ext.js", "");
    JSSourceFile input = JSSourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(extern, input, options);
    compiler.newExternInput("ext.js");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetNodeForCodeInsertionThrowsWhenNoInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    compiler.throwInternalError("Simulated bug", new IllegalArgumentException("detail"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAddIncrementalSourceAstDuplicateThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "var x = 1;");
    compiler.compile(extern, input, options);

    JsAst ast = new JsAst(JSSourceFile.fromCode("test.js", "var x = 2;"));
    compiler.addIncrementalSourceAst(ast);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, State Inspection & Configuration
  // =========================================================================

  @Test(timeout = 4000)
  public void testCustomPrintStreamErrorManagerInitialization() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);
    CompilerOptions options = new CompilerOptions();

    compiler.initOptions(options);
    ErrorManager em = compiler.getErrorManager();
    assertTrue(em instanceof PrintStreamErrorManager);
  }

  @Test(timeout = 4000)
  public void testIdeModeSuppressesHaltingErrors() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;

    compiler.initOptions(options);
    assertTrue(compiler.isIdeMode());
    assertFalse("ideMode must return false for hasErrors even if errorManager has errors",
        compiler.hasErrors());

    compiler.report(JSError.make("test.js", 1, 1, CheckLevel.ERROR,
        DiagnosticType.error("TEST_ERR", "Sample error")));
    assertEquals(1, compiler.getErrorCount());
    assertFalse("ideMode must continue to return false for hasErrors", compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testParseSyntheticCodeVariants() {
    Compiler compiler = new Compiler();
    Node node1 = compiler.parseSyntheticCode("var synt = 1;");
    assertNotNull(node1);

    Node node2 = compiler.parseSyntheticCode("synthFile.js", "var synth2 = 2;");
    assertNotNull(node2);

    Node node3 = compiler.parseTestCode("var test3 = 3;");
    assertNotNull(node3);
  }

  @Test(timeout = 4000)
  public void testInputRemovalAndInputLookup() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("removable.js", "var rem = 1;");

    compiler.compile(extern, input, options);
    assertNotNull(compiler.getInput("removable.js"));

    compiler.removeInput("removable.js");
    assertNull(compiler.getInput("removable.js"));

    compiler.removeInput("non_existent.js");
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInliningWithAmbiguate() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.ambiguateProperties = false;
    options.disambiguateProperties = false;
    compiler.initOptions(options);

    Node n1 = new Node(Token.NAME);
    n1.setString("foo");
    Node n2 = new Node(Token.NAME);
    n2.setString("foo");
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  @Test(timeout = 4000)
  public void testCssRenamingMapAndRegexpReferences() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    assertNull(compiler.getCssRenamingMap());
    CssRenamingMap map = new CssRenamingMap() {
      @Override
      public String get(String value) {
        return "renamed-" + value;
      }
      @Override
      public Style getStyle() {
        return Style.BY_WHOLE;
      }
    };
    compiler.setCssRenamingMap(map);
    assertSame(map, compiler.getCssRenamingMap());

    assertTrue(compiler.hasRegExpGlobalReferences());
    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testSetLoggingLevel() {
    Compiler.setLoggingLevel(Level.WARNING);
  }
}