/*
 * Copyright 2024 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Standard compilation lifecycle: init, parse, check, optimize, toSource, getResult.
 * - Options initialization variants (outStream null vs PrintStream, summaryDetailLevel).
 * - CodeBuilder manipulation (append, line/col tracking, reset, endsWith).
 * - AST & SymbolTable accessors (getTypeRegistry, getReverseAbstractInterpreter, getTypeValidator, buildKnownSymbolTable).
 * - Inlining equivalence checks (ambiguateProperties vs plain isEquivalentTo).
 * - Change handlers registration, dispatch (reportCodeChange), and removal.
 * - Source line / source region retrieval boundaries.
 * - Dot AST graph generation.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Empty module list -> JSC_EMPTY_MODULE_LIST_ERROR.
 * - Root module empty when modules.size() > 1 -> JSC_EMPTY_ROOT_MODULE_ERROR.
 * - Module dependencies: circular dependency cycle -> JSC_MODULE_DEPENDENCY_ERROR.
 * - Duplicate input / extern input name collision -> JSC_DUPLICATE_INPUT / JSC_DUPLICATE_EXTERN_INPUT.
 * - Non-positive line numbers in getSourceLine and getSourceRegion.
 * - Single-pass vs skipped passes (options.skipAllPasses, options.devMode variants).
 *
 * Partition C: Defect-Targeted Branch Zone
 * - Targeted Defect: CommandLineRunnerTest::testCheckGlobalThisOff (AssertionFailedError: Expected no warnings or errors).
 *   Root Cause: In Compiler#initOptions, when options.checkGlobalThisLevel.isOn() is true (e.g. set by default/verbose),
 *   it blindly overrides DiagnosticGroups.GLOBAL_THIS warning level, suppressing user-configured
 *   CheckLevel.OFF guards and emitting spurious JSC_USED_GLOBAL_THIS warnings.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Multiple compilation attempts on single Compiler instance (Preconditions.checkState(jsRoot == null)).
 * - Null errorManager -> NullPointerException in setErrorManager.
 * - PassConfig reassignment or setting null -> IllegalStateException / NullPointerException.
 * - Duplicate extern input creation via newExternInput -> IllegalArgumentException.
 * - Removal of non-extern input via removeExternInput -> IllegalStateException.
 * - throwInternalError properly wrapping cause and preserving message.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - IntermediateState save and restore (getState / setState).
 * - UniqueNameIdSupplier increment and reset.
 * - RegExp global reference state management.
 */
public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompileBasicJsSource() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1 + 2;");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertNotNull(compiler.getRoot());
    assertTrue(compiler.toSource().contains("var a=3"));
  }

  @Test(timeout = 4000)
  public void testCompileWithPrintStreamOutput() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    Compiler compiler = new Compiler(ps);

    CompilerOptions options = new CompilerOptions();
    options.summaryDetailLevel = 1;
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var x = 'Closure';");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertNotNull(compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testCodeBuilderLineAndColumnTracking() {
    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    assertEquals(0, cb.getLength());
    assertEquals(0, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());

    cb.append("foo;\nbar;\n");
    assertEquals(2, cb.getLineIndex());
    assertEquals(0, cb.getColumnIndex());
    assertTrue(cb.endsWith("bar;\n"));
    assertFalse(cb.endsWith("baz"));

    cb.append("baz");
    assertEquals(2, cb.getLineIndex());
    assertEquals(3, cb.getColumnIndex());
    assertEquals("foo;\nbar;\nbaz", cb.toString());

    cb.reset();
    assertEquals(0, cb.getLength());
    // reset clears text but preserves line count per contract
    assertEquals(2, cb.getLineIndex());
  }

  @Test(timeout = 4000)
  public void testToSourceArrayMultipleInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("file1.js", "var x = 1;"),
        JSSourceFile.fromCode("file2.js", "var y = 2;")
    };

    compiler.compile(externs, inputs, options);
    String[] sources = compiler.toSourceArray();

    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("var x=1"));
    assertTrue(sources[1].contains("var y=2"));
  }

  @Test(timeout = 4000)
  public void testModuleCompilationAndToSource() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule rootMod = new JSModule("root");
    rootMod.add(JSSourceFile.fromCode("root.js", "var gRoot = 1;"));

    JSModule childMod = new JSModule("child");
    childMod.add(JSSourceFile.fromCode("child.js", "var gChild = 2;"));
    childMod.addDependency(rootMod);

    Result result = compiler.compileModules(
        Collections.<JSSourceFile>emptyList(),
        Lists.newArrayList(rootMod, childMod),
        options);

    assertTrue(result.success);
    assertEquals(2, compiler.getModuleGraph().getModuleCount());

    String rootSrc = compiler.toSource(rootMod);
    assertTrue(rootSrc.contains("var gRoot=1"));

    String[] childSources = compiler.toSourceArray(childMod);
    assertEquals(1, childSources.length);
    assertTrue(childSources[0].contains("var gChild=2"));
  }

  @Test(timeout = 4000)
  public void testTypeRegistryAndValidatorInitialization() {
    Compiler compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();

    JSTypeRegistry registry = compiler.getTypeRegistry();
    assertNotNull(registry);
    assertSame(registry, compiler.getTypeRegistry());

    assertNotNull(compiler.getTypeValidator());
    assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInlining() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = Node.newString("a");
    Node n2 = Node.newString("a");
    Node n3 = Node.newString("b");

    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
    assertFalse(compiler.areNodesEqualForInlining(n1, n3));
  }

  @Test(timeout = 4000)
  public void testCodeChangeHandlerLifecycle() {
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
  public void testSourceLineAndRegionExtraction() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile input = JSSourceFile.fromCode("test.js", "line1();\nline2();\nline3();");
    compiler.init(new JSSourceFile[0], new JSSourceFile[] { input }, options);

    assertEquals("line1();", compiler.getSourceLine("test.js", 1));
    assertEquals("line2();", compiler.getSourceLine("test.js", 2));
    assertNull(compiler.getSourceLine("test.js", 0));
    assertNull(compiler.getSourceLine("test.js", -1));
    assertNull(compiler.getSourceLine("nonexistent.js", 1));

    assertNotNull(compiler.getSourceRegion("test.js", 2));
    assertNull(compiler.getSourceRegion("test.js", 0));
  }

  @Test(timeout = 4000)
  public void testAstDotGraphGeneration() throws IOException {
    Compiler compiler = new Compiler();
    assertEquals("", compiler.getAstDotGraph());

    CompilerOptions options = new CompilerOptions();
    compiler.compile(
        JSSourceFile.fromCode("ext.js", ""),
        JSSourceFile.fromCode("in.js", "function f() { return 1; }"),
        options);

    String dotGraph = compiler.getAstDotGraph();
    assertNotNull(dotGraph);
    assertTrue(dotGraph.contains("digraph"));
  }

  @Test(timeout = 4000)
  public void testParseSyntheticAndTestCode() {
    Compiler compiler = new Compiler();
    Node node1 = compiler.parseSyntheticCode("synthetic.js", "var syn = 1;");
    assertNotNull(node1);
    assertEquals(Token.SCRIPT, node1.getType());

    Node node2 = compiler.parseTestCode("var t = 2;");
    assertNotNull(node2);
    assertEquals(Token.SCRIPT, node2.getType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyModuleListReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    compiler.initModules(
        Collections.<JSSourceFile>emptyList(),
        Collections.<JSModule>emptyList(),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testEmptyRootModuleWithMultipleModulesReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule root = new JSModule("root");
    JSModule child = new JSModule("child");
    child.add(JSSourceFile.fromCode("child.js", "var c = 1;"));

    compiler.initModules(
        Collections.<JSSourceFile>emptyList(),
        Lists.newArrayList(root, child),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testCircularModuleDependencyReportsError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));

    m1.addDependency(m2);
    m2.addDependency(m1);

    compiler.initModules(
        Collections.<JSSourceFile>emptyList(),
        Lists.newArrayList(m1, m2),
        options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputNamesReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("dup.js", "var a = 1;"),
        JSSourceFile.fromCode("dup.js", "var b = 2;")
    };

    compiler.init(externs, inputs, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateExternNamesReportError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("ext.js", "var a;"),
        JSSourceFile.fromCode("ext.js", "var b;")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("in.js", "var x = 1;")
    };

    compiler.init(externs, inputs, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testSkipAllPassesExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;

    Result result = compiler.compile(
        JSSourceFile.fromCode("ext.js", ""),
        JSSourceFile.fromCode("in.js", "var a = 10;"),
        options);

    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (CommandLineRunnerTest defect)
  // =========================================================================

  /**
   * Targets the defect where options.checkGlobalThisLevel is enabled (WARNING)
   * while DiagnosticGroups.GLOBAL_THIS has been explicitly set to CheckLevel.OFF.
   * Defective logic in Compiler#initOptions unconditionally reapplies
   * checkGlobalThisLevel when isOn() is true, overriding the user's OFF guard
   * and generating a spurious JSC_USED_GLOBAL_THIS warning.
   */
  @Test(timeout = 4000)
  public void testCheckGlobalThisOffDefect() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.setWarningLevel(DiagnosticGroups.GLOBAL_THIS, CheckLevel.OFF);

    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js",
        "/** @constructor */ function F() {} "
            + "F.prototype.bar = function() { this.a = 3; }; "
            + "function f() { this.a = 3; }");

    Result result = compiler.compile(extern, input, options);

    assertTrue(result.success);
    assertEquals("Expected no warnings when GLOBAL_THIS is explicitly OFF",
        0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompileCannotBeCalledTwice() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile ext = JSSourceFile.fromCode("ext.js", "");
    JSSourceFile in = JSSourceFile.fromCode("in.js", "var a = 1;");

    compiler.compile(ext, in, options);

    try {
      compiler.compile(ext, in, options);
      fail("Expected IllegalStateException when compile is invoked twice");
    } catch (IllegalStateException expected) {
      // Success: compiler prevents multiple compile calls
    }
  }

  @Test(timeout = 4000)
  public void testSetErrorManagerNullThrows() {
    Compiler compiler = new Compiler();
    try {
      compiler.setErrorManager(null);
      fail("Expected NullPointerException for null ErrorManager");
    } catch (NullPointerException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testSetPassConfigNullOrReassignmentThrows() {
    Compiler compiler = new Compiler();
    try {
      compiler.setPassConfig(null);
      fail("Expected NullPointerException for null PassConfig");
    } catch (NullPointerException expected) {
      // Success
    }

    PassConfig defaultPasses = compiler.getPassConfig();
    assertNotNull(defaultPasses);

    try {
      compiler.setPassConfig(defaultPasses);
      fail("Expected IllegalStateException when re-assigning PassConfig");
    } catch (IllegalStateException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testNewExternInputConflictThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "var ext1;") },
        new JSSourceFile[] { JSSourceFile.fromCode("in.js", "var in1;") },
        options);
    compiler.parseInputs();

    try {
      compiler.newExternInput("ext.js");
      fail("Expected IllegalArgumentException for conflicting extern input name");
    } catch (IllegalArgumentException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testRemoveNonExternInputThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] {},
        new JSSourceFile[] { JSSourceFile.fromCode("in.js", "var in1;") },
        options);
    compiler.parseInputs();

    try {
      compiler.removeExternInput("in.js");
      fail("Expected IllegalStateException when removing non-extern input via removeExternInput");
    } catch (IllegalStateException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testThrowInternalErrorPreservesCause() {
    Compiler compiler = new Compiler();
    IOException cause = new IOException("Disk failure");

    try {
      compiler.throwInternalError("Pass failed", cause);
      fail("Expected RuntimeException from throwInternalError");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR"));
      assertTrue(e.getMessage().contains("Pass failed"));
      assertSame(cause, e.getCause());
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testStateSaveAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "var ext = 1;") },
        new JSSourceFile[] { JSSourceFile.fromCode("in.js", "var a = 2;") },
        options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);
    assertNotNull(state.externsRoot);

    Compiler compiler2 = new Compiler();
    compiler2.init(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "var ext = 1;") },
        new JSSourceFile[] { JSSourceFile.fromCode("in.js", "var a = 2;") },
        options);
    compiler2.setState(state);

    assertSame(state.externsRoot, compiler2.externsRoot);
    assertSame(state.typeRegistry, compiler2.getTypeRegistry());
  }

  @Test(timeout = 4000)
  public void testUniqueNameIdSupplierIncrementAndReset() {
    Compiler compiler = new Compiler();
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();

    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());

    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testRegExpGlobalReferencesFlag() {
    Compiler compiler = new Compiler();
    assertTrue(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(false);
    assertFalse(compiler.hasRegExpGlobalReferences());

    compiler.setHasRegExpGlobalReferences(true);
    assertTrue(compiler.hasRegExpGlobalReferences());
  }

  @Test(timeout = 4000)
  public void testSetLoggingLevel() {
    Compiler.setLoggingLevel(Level.FINE);
    Compiler.setLoggingLevel(Level.INFO);
  }

  @Test(timeout = 4000)
  public void testDisableThreads() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();

    CompilerOptions options = new CompilerOptions();
    Result result = compiler.compile(
        JSSourceFile.fromCode("ext.js", ""),
        JSSourceFile.fromCode("in.js", "var x = 123;"),
        options);

    assertTrue(result.success);
    assertTrue(compiler.toSource().contains("var x=123"));
  }
}