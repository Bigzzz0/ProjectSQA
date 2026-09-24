package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.base.Supplier;
import com.google.javascript.jscomp.Compiler.CodeBuilder;
import com.google.javascript.jscomp.CompilerOptions.DevMode;
import com.google.javascript.jscomp.CompilerOptions.TracerMode;
import com.google.javascript.rhino.Node;

import java.io.PrintStream;
import java.nio.CharSet;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;

public class CompilerDeepseekTest {

  /* [Branch & Defect Analysis Matrix]
   *
   * Branch/Decision Targets (Compiler.java):
   * - initOptions(): null stream -> LoggerErrorManager, non-null -> PrintStreamErrorManager
   * - init(): array vs list overloads
   * - initModules(): moduleGraph creation vs null for single module
   * - checkFirstModule(): empty list, root module empty
   * - fillEmptyModules(): empty module insertion
   * - initInputsByNameMap(): duplicate detection, extern vs input
   * - compile() -> runInCompilerThread(): threading branch (useThreads true/false)
   * - parseInputs(): externs parsing, staleInputs logic, JSDocInfo checks
   * - toSource(): printInputDelimiter branch, license comment, source map position
   * - setPassConfig(): null check, already set check
   * - getState()/setState(): state save/restore
   * - hasHaltingErrors(): IDE mode false branch
   * - getSourceLine(): lineNumber < 1 -> null
   * - getSourceRegion(): same
   *
   * Boundary Conditions:
   * - Empty module list (EMPTY_MODULE_LIST_ERROR)
   * - Root module with no inputs (EMPTY_ROOT_MODULE_ERROR)
   * - Duplicate input names (DUPLICATE_INPUT, DUPLICATE_EXTERN_INPUT)
   * - Null arguments (NullPointerException expected)
   * - Negative line numbers -> null
   * - Large module count (moduleGraph creation)
   * - Circular dependencies (not directly tested here)
   *
   * Defect Targeting:
   * - Charset expansion: when setOutputCharset("US-ASCII") is called, the
   *   outputCharset field must not be null and must equal Charset.forName("US-ASCII").
   *   The defect causes null due to improper expansion (bug in CompilerOptions).
   */
  
  // ===== Partition A: Core Functional Logic & State Transitions =====

  @Test(timeout = 4000)
  public void testEmptyConstructor() {
    Compiler compiler = new Compiler();
    assertNotNull(compiler);
    assertNull(compiler.getErrorManager());
    // options should be null initially
    assertNull(compiler.options);
  }

  @Test(timeout = 4000)
  public void testConstructorWithPrintStream() {
    PrintStream stream = System.out;
    Compiler compiler = new Compiler(stream);
    assertNotNull(compiler);
    // outStream is private, but we can check that after initOptions it uses PrintStreamErrorManager
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    assertTrue(compiler.getErrorManager() instanceof PrintStreamErrorManager);
  }

  @Test(timeout = 4000)
  public void testConstructorWithErrorManager() {
    ErrorManager errorManager = new LoggerErrorManager(null, null);
    Compiler compiler = new Compiler(errorManager);
    assertEquals(errorManager, compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testInitOptionsSetsErrorManager() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    assertNotNull(compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testInitWithArrayOverload() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", "var a = 1;")
    };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.getInput("test.js"));
  }

  @Test(timeout = 4000)
  public void testInitWithModuleOverload() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSModule module = new JSModule("m1");
    module.add(JSSourceFile.fromCode("mod.js", "var x = 2;"));
    JSModule[] modules = new JSModule[] { module };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, modules, options);
    assertNotNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testInitModulesWithMoreThanOneModule() {
    Compiler compiler = new Compiler();
    List<JSSourceFile> externs = List.of();
    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    JSModule m2 = new JSModule("m2");
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));
    List<JSModule> modules = List.of(m1, m2);
    CompilerOptions options = new CompilerOptions();
    // This should generate a module graph; but ordering may cause MODULE_DEPENDENCY_EROR if bad dependency
    // We'll skip dependency setup; the test verifies no crash
    compiler.initModules(externs, modules, options);
    // After init, moduleGraph should be non-null
    assertNotNull(compiler.getModuleGraph()); // getModuleGraph() is package-private, but we have access
  }

  @Test(timeout = 4000)
  public void testGetResult() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setOutputCharset("US-ASCII");
    compier.init(new JSSourceFile[0],
                       new JSSourceFile[]{JSSourceFile.fromCode("test.js", "var a = 1;")},
                       options);
    Result result = compier.compile();
    assertNotNull(result);
    assertEquals(0, result.errors.length);
    assertEquals(0, result.warnings.length);
    assertNotNull(compier.toSource());
  }

  @Test(timeout = 4000)
  public void testNormalizationState() {
    Compiler comp = new Compiler();
    assertFalse(comp.isNormalized());
    comp.setNormalized();
    assertTrue(comp.isNormalized());
    comp.setUnnormalized();
    assertFalse(comp.isNormalized());
  }

  @Test(timeout = 4000)
  public void testUniqueNameId() {
    Compiler compiler = new Compiler();
    assertEquals(0, compiler.uniqueNameId); // package-private field
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    compiler.resetUniqueNameId();
    assertEquals("0", supplier.get());
  }

  // ===== Partition B: Boundary Value Analysis & Extremes =====

  @Test(timeout = 4000)
  public void testInitWithEmptyExternsAndInputs() {
    Compiler comp = new Compiler();
    CompilerOptions options = new CompilerOptions();
    comp.init(new JSSourceFile[0], new JSSourceFile[0], options);
    // Should work without error; the compile will produce empty output
    Result result = comp.compile();
    assertNotNull(result);
    assertEquals("", comp.toSource());
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetErrorManagerWithNull() {
    Compiler compiler = new Compiler();
    compiler.setErrorManager(null);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testInitWithNullOptions() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", "console.log('hello');");
    };
    compiler.init(externs, inputs, null);
  }

  @Test(timeout = 4000)
  public void testGetInputReturnsNullForUnknown() {
    Compiler compiler = new Compiler();
    assertNull(compiler.getInput("nonexistent"));
  }

  @Test(timeout = 4000)
  public void testGetSourceLineNegativeLineNumber() {
    Compiler compiler = new Compiler();
    String line = compiler.getSourceLine("test.js", -1);
    assertNull(line);
  }

  @Test(timeout = 4000)
  public void testGetSourceRegionNegativeLineNumber() {
    Compiler compiler = new Compiler();
    assertNull(compiler.getSourceRegion("test.js", 0));
  }

  // ===== Partition C: Defect-Targeted Branch =====

  @Test(timeout = 4000)
  public void testCharsetExpansion() {
    // Direct test on CompilerOptions.setOutputCharset
    CompilerOptions options = new CompilerOptions();
    options.setOutputCharset("US-ASCII");
    assertNotNull("outputCharset should not be null after setOutputCharset",
                  options.outputCharset);
    assertEquals(Charset.forName("US-ASCII"), options.outputCharset);
  }

  @Test(timeout = 4000)
  public void testCompilationWithCharset() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setOutputCharset("US-ASCII");
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", "var x = 1;")
    };
    compiler.init(externs, inputs, options);
    compiler.compile();
    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue(source.contains("var x=1"));
    // Ensure the charset field is correctly set
    assertNotNull(options.outputCharset);
  }

  // ===== Partition D: Exception & Defensive Guard Paths =====

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testSetPassConfigWhenAlreadySet() {
    Compiler compiler = new Compiler();
    PassConfig passes = new DefaultPassConfig(new CompilerOptions());
    compiler.setPassConfig(passes);
    // Second call should throw
    compiler.setPassConfig(new DefaultPassConfig(new CompilerOptions()));
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testSetPassConfigWithNull() {
    Compiler compiler = new Compiler();
    compiler.setPassConfig(null);
  }

 ‍@Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetNodeForCodeInsertionWithoutInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    // initialize with empty inputs
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);
    // This should throw because inputs list is empty
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(timeout = 4000)
  public void testHasHaltingErrorsInIdeMode() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    // Force ideMode
    compiler.options.ideMode = true;
    assertFalse(compiler.hasHaltingErrors());
  }

  @Test(timeout = 4000)
  public void testAddChangeHandler() {
    Compiler compiler = new Compiler();
    compiler.addChangeHandler(new CodeChangeHandler() {
      @Override
      public void reportChange() {}
    });
    // no exception
  }

  @Test(timeout = 4000)
  public void testRemoveChangeHandler() {
    Compiler compiler = new Compiler();
    CodeChangeHandler handler = new CodeChangeHandler() {
      @Override
      public void reportChange() {}
    };
    compiler.addChangeHandler(handler);
    compiler.removeChangeHandler(handler);
    // should not throw
  }

  // ===== Partition E: Object Lifecycle & Contract Integrity (optional) =====

  @Test(timeout = 4000)
  public void testStateSaveRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("test.js", "var a = 1;")
    };
    compier.init(externs, inputs, options);
    compier.compile();
    IntermediaState state = compier.getState();
    assertNotNull(state);
    // Create a new compiler and set state
    Compiler compier2 = new Compiler();
    compier2.init(externs, inputs, options);
    compier2.setState(state);
    assertNotNull(compier2.getRoot());
    assertEquals(compier.toSource(), compier2.toSource());
  }

  @Test(timeout = 4000)
  public void testGetErrorCountAndWarningCount() {
    Compiler comp = new Compiler();
    // Initially, before init, errorManager may be null; call initOptions to set it
    comp.initOptions(new CompilerOptions());
    assertEquals(0, comp.getErrorCount());
    assertEquals(0, comp.getWarningCount());
  }

  // Additional test for duplicate input detection
  @Test(timeout = 4000)
  public void testDuplicateInputNotDetectedWithoutInit() {
    Compiler compiler = new Compiler();
    // duplicate detection happens in initInputsByNameMap, which is called during init
    // We'll test the method directly (package-private)
    // First, set up some fields manually
    compiler.externs = List.of();
    compiler.inputs = List.of(
        new CompilerInput(new JsAst(JSSourceFile.fromCode("dup.js", "var a=1;"))),
        new CompilerInput(new JsAst(JSSourceFile.fromCode("dup.js", "var b=2;")))
    );
    compiler.initInputsByNameMap();
    // Should have reported an error
    assertTrue(compiler.hasErrors());
  }
}