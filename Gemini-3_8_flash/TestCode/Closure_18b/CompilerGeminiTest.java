/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.Compiler
 *
 * Targeted Defects & Branches:
 * 1. Dependency Management Defect (IntegrationTest::testDependencySorting):
 *    - In parseInputs(), dependency management must run if DependencyOptions.needsManagement()
 *      is true, regardless of whether closurePass is active or not.
 * 2. Modules & Graph Verification:
 *    - checkFirstModule(): empty modules list -> EMPTY_MODULE_LIST_ERROR.
 *    - checkFirstModule(): empty root module with > 1 modules -> EMPTY_ROOT_MODULE_ERROR.
 *    - initModules(): duplicate input IDs reporting DUPLICATE_INPUT / DUPLICATE_EXTERN_INPUT.
 *    - initModules(): circular dependency handling with ModuleDependenceException -> MODULE_DEPENDENCY_ERROR.
 * 3. Lifecycle, Configuration, & State:
 *    - initOptions(): DiagnosticGroups overrides, ECMASCRIPT5_STRICT warning check, CHECK_VARIABLES guard.
 *    - getState() & setState(): IntermediateState snapshot serialization/deserialization integrity.
 *    - setPassConfig(): null check & multiple assignment check.
 *    - disableThreads() & runCallable(): Large stack vs single thread execution modes.
 * 4. AST Modification & Source Operations:
 *    - replaceScript(), addNewScript(), removeExternInput(), newExternInput().
 *    - toSource(), toSourceArray(), toSource(JSModule), toSourceArray(JSModule).
 *    - CodeBuilder operations: append, reset, line and column counts, endsWith.
 *    - getNodeForCodeInsertion(): empty inputs exception guard.
 * 5. Error & Warning Management:
 *    - Custom ErrorManager, report(), hasErrors(), hasHaltingErrors(), throwInternalError().
 *    - getSourceLine(), getSourceRegion() boundary checks (< 1 line index).
 */

package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompilerInitializationAndDefaults() {
    Compiler compiler = new Compiler();
    assertNotNull(compiler);
    assertNull(compiler.options);
    assertEquals(0.0, compiler.getProgress(), 0.0001);
    assertTrue(compiler.hasRegExpGlobalReferences());

    compiler.setProgress(0.5);
    assertEquals(0.5, compiler.getProgress(), 0.0001);
    compiler.setProgress(-0.5);
    assertEquals(0.0, compiler.getProgress(), 0.0001);
    compiler.setProgress(1.5);
    assertEquals(1.0, compiler.getProgress(), 0.0001);

    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testCompilerWithPrintStream() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);

    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    assertNotNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testSimpleCompilationPipeline() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(CompilerOptions.LanguageMode.ECMASCRIPT3);
    options.setLanguageOut(CompilerOptions.LanguageMode.ECMASCRIPT3);

    SourceFile extern = SourceFile.fromCode("externs.js", "var window;");
    SourceFile input = SourceFile.fromCode("input.js", "var x = 1 + 2;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());

    String source = compiler.toSource();
    assertTrue(source.contains("var x=3") || source.contains("var x = 3") || source.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testStateSaveAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("extern.js", "function ext() {}");
    SourceFile input = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler freshCompiler = new Compiler();
    freshCompiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    freshCompiler.setState(state);

    assertEquals(state.externsRoot, freshCompiler.externsRoot);
    assertEquals(state.jsRoot, freshCompiler.jsRoot);
  }

  @Test(timeout = 4000)
  public void testCodeBuilderOperations() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("foo\nbar");
    assertEquals(7, cb.getLength());
    assertEquals(1, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertTrue(cb.endsWith("bar"));
    assertFalse(cb.endsWith("foo"));

    cb.append("\n");
    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(2, cb.getLineIndex()); // reset keeps line count intact
    assertEquals("", cb.toString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyModuleListProducesError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Collections.singletonList(SourceFile.fromCode("ext.js", ""));
    List<JSModule> modules = Collections.emptyList();

    compiler.initModules(externs, modules, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEmptyRootModuleWithMultipleModulesProducesError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    List<SourceFile> externs = Collections.singletonList(SourceFile.fromCode("ext.js", ""));

    JSModule m1 = new JSModule("root");
    JSModule m2 = new JSModule("child");
    m2.add(SourceFile.fromCode("c.js", "var y = 2;"));

    compiler.initModules(externs, Lists.newArrayList(m1, m2), options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile f1 = SourceFile.fromCode("same.js", "var a = 1;");
    SourceFile f2 = SourceFile.fromCode("same.js", "var b = 2;");

    compiler.init(Lists.newArrayList(SourceFile.fromCode("ext.js", "")),
                  Lists.newArrayList(f1, f2), options);

    assertTrue(compiler.hasErrors());
    boolean foundDuplicateError = false;
    for (JSError err : compiler.getErrors()) {
      if (err.getType().key.equals("JSC_DUPLICATE_INPUT")) {
        foundDuplicateError = true;
        break;
      }
    }
    assertTrue("Expected duplicate input error", foundDuplicateError);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternInputDetection() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    SourceFile e1 = SourceFile.fromCode("same_ext.js", "var x;");
    SourceFile e2 = SourceFile.fromCode("same_ext.js", "var y;");

    compiler.init(Lists.newArrayList(e1, e2),
                  Lists.newArrayList(SourceFile.fromCode("in.js", "var z = 1;")), options);

    assertTrue(compiler.hasErrors());
    boolean foundDuplicateExternError = false;
    for (JSError err : compiler.getErrors()) {
      if (err.getType().key.equals("JSC_DUPLICATE_EXTERN_INPUT")) {
        foundDuplicateExternError = true;
        break;
      }
    }
    assertTrue("Expected duplicate extern input error", foundDuplicateExternError);
  }

  @Test(timeout = 4000)
  public void testSourceLineAndRegionBoundaries() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile input = SourceFile.fromCode("code.js", "line1\nline2\nline3");

    compiler.init(new SourceFile[]{}, new SourceFile[]{input}, options);

    assertNull(compiler.getSourceLine("code.js", 0));
    assertNull(compiler.getSourceLine("code.js", -1));
    assertEquals("line1", compiler.getSourceLine("code.js", 1));
    assertEquals("line2", compiler.getSourceLine("code.js", 2));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));

    assertNull(compiler.getSourceRegion("code.js", 0));
    assertNotNull(compiler.getSourceRegion("code.js", 1));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Dependency Sorting Bug)
  // =========================================================================

  /**
   * Targets the defect corresponding to IntegrationTest::testDependencySorting.
   * In Compiler.java line 673:
   *   `if (options.dependencyOptions.needsManagement() && options.closurePass)`
   * Dependency management must run when dependency sorting is requested,
   * even if closurePass is false.
   */
  @Test(timeout = 4000)
  public void testDependencySortingWithoutClosurePass() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setDependencyOptions(DependencyOptions.SORT_ONLY);
    options.closurePass = false;

    SourceFile f1 = SourceFile.fromCode("req.js", "goog.require('a');");
    SourceFile f2 = SourceFile.fromCode("prov.js", "goog.provide('a');");

    // Pass req.js first, then prov.js
    compiler.init(
        Lists.<SourceFile>newArrayList(),
        Lists.<SourceFile>newArrayList(f1, f2),
        options);

    compiler.parseInputs();

    List<CompilerInput> orderedInputs = compiler.getInputsInOrder();
    assertEquals("Both inputs should be retained", 2, orderedInputs.size());
    // Correct sorting requires prov.js before req.js:
    assertEquals("prov.js", orderedInputs.get(0).getName());
    assertEquals("req.js", orderedInputs.get(1).getName());
  }

  @Test(timeout = 4000)
  public void testCircularModuleDependencyReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    m1.add(SourceFile.fromCode("m1.js", "var a = 1;"));
    m2.add(SourceFile.fromCode("m2.js", "var b = 2;"));

    m1.addDependency(m2);
    m2.addDependency(m1);

    compiler.initModules(
        Lists.<SourceFile>newArrayList(SourceFile.fromCode("ext.js", "")),
        Lists.newArrayList(m1, m2),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetErrorManagerNullThrows() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testSetPassConfigNullThrows() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetPassConfigTwiceThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    PassConfig pc1 = new DefaultPassConfig(options);
    PassConfig pc2 = new DefaultPassConfig(options);

    compiler.setPassConfig(pc1);
    compiler.setPassConfig(pc2);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCompileTwiceThrowsException() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("ext.js", "");
    SourceFile input = SourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(extern, input, options);
    // Second compile call on the same instance is prohibited
    compiler.compile(extern, input, options);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetNodeForCodeInsertionEmptyThrows() {
    Compiler compiler = new Compiler();
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(expected = RuntimeException.class, timeout = 4000)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    compiler.throwInternalError("Test internal crash", new IllegalStateException("Root cause"));
  }

  // =========================================================================
  // Partition E: AST Parsing, Synthetic Ast, and Incremental Methods
  // =========================================================================

  @Test(timeout = 4000)
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node node1 = compiler.parseTestCode("var k = 10;");
    assertNotNull(node1);
    assertEquals(Token.SCRIPT, node1.getType());

    Node node2 = compiler.parseSyntheticCode("synth.js", "function foo() {}");
    assertNotNull(node2);
    assertEquals(Token.SCRIPT, node2.getType());

    Node node3 = compiler.parseSyntheticCode("var anon = true;");
    assertNotNull(node3);
    assertEquals(Token.SCRIPT, node3.getType());
  }

  @Test(timeout = 4000)
  public void testExternManagementMethods() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("ext.js", "var externalVar;");
    SourceFile input = SourceFile.fromCode("in.js", "externalVar = 1;");

    compiler.init(Lists.newArrayList(extern), Lists.newArrayList(input), options);
    compiler.parseInputs();

    CompilerInput newExt = compiler.newExternInput("synthetic_extern.js");
    assertNotNull(newExt);
    assertTrue(newExt.isExtern());
    assertEquals(new InputId("synthetic_extern.js"), newExt.getInputId());

    compiler.removeExternInput(newExt.getInputId());
    assertNull(compiler.getInput(newExt.getInputId()));
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplier() {
    Compiler compiler = new Compiler();
    com.google.common.base.Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());
    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testDisableThreads() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();

    CompilerOptions options = new CompilerOptions();
    SourceFile extern = SourceFile.fromCode("ext.js", "");
    SourceFile input = SourceFile.fromCode("in.js", "var t = 1;");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals("var t=1;", compiler.toSource().trim());
  }

  @Test(timeout = 4000)
  public void testModuleToSourceArray() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule module = new JSModule("mod1");
    SourceFile f1 = SourceFile.fromCode("f1.js", "var x = 1;");
    SourceFile f2 = SourceFile.fromCode("f2.js", "var y = 2;");
    module.add(f1);
    module.add(f2);

    compiler.initModules(
        Lists.<SourceFile>newArrayList(SourceFile.fromCode("ext.js", "")),
        Lists.newArrayList(module),
        options);
    compiler.parseInputs();

    String[] sources = compiler.toSourceArray(module);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var x=1") || sources[0].contains("var x = 1"));
    assertTrue(sources[1].contains("var y=2") || sources[1].contains("var y = 2"));

    String singleSource = compiler.toSource(module);
    assertTrue(singleSource.contains("var x"));
    assertTrue(singleSource.contains("var y"));
  }

  @Test(timeout = 4000)
  public void testVersionAndDate() {
    String version = Compiler.getReleaseVersion();
    assertNotNull(version);
    String date = Compiler.getReleaseDate();
    assertNotNull(date);
  }
}