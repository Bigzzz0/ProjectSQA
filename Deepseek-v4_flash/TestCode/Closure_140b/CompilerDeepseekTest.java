package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.PrintStream;

/**
 * White-box tests for the Compiler class, targeting state transitions,
 * boundary conditions, error handling, and the known defect in module
 * initialization that can trigger spurious reportCodeChange calls.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor variants (default, PrintStream, ErrorManager)
 *   - init() with externs/inputs and modules
 *   - parse(), getRoot(), getResult()
 *   - reportCodeChange() handler invocation
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty externs/inputs arrays
 *   - Empty module lists, root modules with zero inputs
 *   - Duplicate input/extern names
 *   - null arguments to setErrorManager, setPassConfig
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Empty module scenario that previously caused spurious codeChange report
 *   - Verifying zero code changes after init with empty root module
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null error manager (Preconditions)
 *   - setPassConfig with null
 *   - getNodeForCodeInsertion with no inputs
 *   - Check preconditions in init(modules) with empty modules
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - uniqueNameId reset and supplier
 *   - normalized flag get/set
 *   - getInput, newExternInput, addIncrementalSourceAst
 *   - toSource, getSourceLine, getSourceRegion
 */
public class CompilerDeepseekTest {

  // ------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testDefaultConstructor() {
    Compiler compiler = new Compiler();
    assertNotNull(compiler);
    // The compiler should have created a RecentChange handler, no error manager yet
    assertNull(compiler.options);
    assertNotNull(compiler.getTypeRegistry());
  }

  @Test(timeout = 4000)
  public void testConstructorWithPrintStream() {
    PrintStream ps = System.out;
    Compiler compiler = new Compiler(ps);
    assertNotNull(compiler);
    // options still null until init
    assertNull(compiler.options);
  }

  @Test(timeout = 4000)
  public void testConstructorWithErrorManager() {
    ErrorManager em = new LoggerErrorManager(null, null);
    Compiler compiler = new Compiler(em);
    assertNotNull(compiler);
    assertEquals(em, compiler.getErrorManager());
  }

  @Test(timeout = 4000)
  public void testSetErrorManagerNull() {
    Compiler compiler = new Compiler();
    try {
      compiler.setErrorManager(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testInitWithExternsAndInputs() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("extern1.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("input1.js", "var a = 1;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    assertNotNull(compiler.getInput("extern1.js"));
    assertNotNull(compiler.getInput("input1.js"));
    assertFalse(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testInitWithModules() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("ex.js", "") };
    JSModule module = new JSModule("m1", new JSSourceFile[] { JSSourceFile.fromCode("mod1.js", "var x=1;") });
    JSModule[] modules = new JSModule[] { module };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, modules, options);
    assertNotNull(compiler.getInput("ex.js"));
    assertNotNull(compiler.getInput("mod1.js"));
    assertFalse(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testInitWithEmptyModuleList() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSModule[] modules = new JSModule[0];
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, modules, options);
    // Should have error: at least one module must be provided
    assertTrue(compiler.hasErrors());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    assertTrue(errors[0].description.contains("At least one module must be provided"));
  }

  @Test(timeout = 4000)
  public void testInitWithEmptyRootModule() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSModule module = new JSModule("emptyRoot", new JSSourceFile[0]);  // no inputs
    JSModule[] modules = new JSModule[] { module };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, modules, options);
    // root module must contain at least one source input
    assertTrue(compiler.hasErrors());
    JSError[] errors = compiler.getErrors();
    assertEquals(1, errors.length);
    assertTrue(errors[0].description.contains("Root module 'emptyRoot' must contain at least one source code input"));
  }

  @Test(timeout = 4000)
  public void testDuplicateInputsReported() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("dup.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("dup.js", "var b=2;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    // Both extern and input have same name --> duplicate extern input error
    assertTrue(compiler.hasErrors());
    JSError[] errors = compiler.getErrors();
    boolean found = false;
    for (JSError e : errors) {
      if (e.description.contains("Duplicate extern input")) {
        found = true;
        break;
      }
    }
    assertTrue("Expected duplicate extern input error", found);
  }

  @Test(timeout = 4000)
  public void testParseAfterInit() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var y = 2;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    compiler.parse();
    assertNotNull(compiler.getRoot());
    assertFalse(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testGetRootBeforeParse() {
    Compiler compiler = new Compiler();
    assertNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testGetInputByExistingName() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("a.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("b.js", "") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    assertNotNull(compiler.getInput("a.js"));
    assertNotNull(compiler.getInput("b.js"));
    assertNull(compiler.getInput("nonexistent"));
  }

  @Test(timeout = 4000)
  public void testNewExternInput() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("src.js", "var a = 1;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    // Add a new extern dynamically
    compiler.newExternInput("dynamicExtern.d.js");
    assertNotNull(compiler.getInput("dynamicExtern.d.js"));
  }

  @Test(timeout = 4000)
  public void testNewExternInputConflictingName() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[] { JSSourceFile.fromCode("dup.js", "") };
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("src.js", "") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    try {
      compiler.newExternInput("dup.js");
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage().contains("Conflicting externs name"));
    }
  }

  @Test(timeout = 4000)
  public void testGetResultAfterCompile() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var x = 1;") };
    CompilerOptions options = new CompilerOptions();
    options.skipAllPasses = true;  // stop after check (no heavy optimization)
    Result result = compiler.compile(externs, inputs, options);
    assertNotNull(result);
    assertNotNull(result.errors);
    assertNotNull(result.warnings);
    // No errors expected
    assertEquals(0, result.errors.length);
  }

  // ------------------------------------------------------------------
  // Partition B: Boundary Value Analysis & Extremes
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testInitWithEmptyExternsAndInputs() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[0];
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    // No errors (empty arrays are valid)
    assertFalse(compiler.hasErrors());
    assertNotNull(compiler.getRoot());
  }

  @Test(timeout = 4000)
  public void testGetSourceLineLineNumberLessThanOne() {
    Compiler compiler = new Compiler();
    assertNull(compiler.getSourceLine("any.js", 0));
    assertNull(compiler.getSourceLine("any.js", -1));
  }

  @Test(timeout = 4000)
  public void testGetSourceRegionLineNumberLessThanOne() {
    Compiler compiler = new Compiler();
    assertNull(compiler.getSourceRegion("any.js", 0));
    assertNull(compiler.getSourceRegion("any.js", -1));
  }

  @Test(timeout = 4000)
  public void testGetSourceLineExistentFile() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var x = 1;\nvar y = 2;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    // After init, the input name is registered but source file not yet parsed? Actually getSourceLine works via inputsByName map, which is populated in init.
    assertEquals("var x = 1;", compiler.getSourceLine("test.js", 1));
    assertEquals("var y = 2;", compiler.getSourceLine("test.js", 2));
  }

  // ------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone
  //   Known defect: reportCodeChange() was called even though nothing changed
  //   This test replicates the scenario from CrossModuleCodeMotionTest::testEmptyModule
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testEmptyModuleNoCodeChange() {
    // Create a compiler and add a custom code change handler that counts calls
    final int[] changeCount = {0};
    Compiler compiler = new Compiler();
    compiler.addChangeHandler(new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changeCount[0]++;
      }
    });

    // Prepare a compilation with an empty root module (no inputs)
    JSSourceFile[] externs = new JSSourceFile[0];
    JSModule emptyModule = new JSModule("empty", new JSSourceFile[0]);
    JSModule[] modules = new JSModule[] { emptyModule };
    CompilerOptions options = new CompilerOptions();

    // init should fail because root module has no inputs
    compiler.init(externs, modules, options);
    assertTrue(compiler.hasErrors());
    // No code changes should have been reported during init
    assertEquals("reportCodeChange should not have been called after init", 0, changeCount[0]);

    // Even if we attempt compile (which will bail out early due to errors), no changes
    compiler.compile(externs, modules, options);
    assertEquals("reportCodeChange should not have been called after compile (errors)", 0, changeCount[0]);
  }

  // ------------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testSetPassConfigNull() {
    Compiler compiler = new Compiler();
    try {
      compiler.setPassConfig(null);
      fail("Expected NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testSetPassConfigTwice() {
    Compiler compiler = new Compiler();
    PassConfig pc = new DefaultPassConfig(new CompilerOptions());
    compiler.setPassConfig(pc);
    try {
      compiler.setPassConfig(pc);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("has already been assigned"));
    }
  }

  @Test(timeout = 4000)
  public void testGetNodeForCodeInsertionWithNoInputs() {
    Compiler compiler = new Compiler();
    // init with empty arrays
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[0];
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    try {
      compiler.getNodeForCodeInsertion(null);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("No inputs"));
    }
  }

  @Test(timeout = 4000)
  public void testSetErrorManagerTwice() {
    Compiler compiler = new Compiler();
    ErrorManager em1 = new LoggerErrorManager(null, null);
    compiler.setErrorManager(em1);
    // Setting another error manager should work (no exception mentioned)
    ErrorManager em2 = new LoggerErrorManager(null, null);
    compiler.setErrorManager(em2);
    assertEquals(em2, compiler.getErrorManager());
  }

  // ------------------------------------------------------------------
  // Partition E: Object Lifecycle & Contract Integrity
  // ------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testUniqueNameIdReset() {
    Compiler compiler = new Compiler();
    // The uniqueNameId supplier should start at 0
    Supplier<String> supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
    assertEquals("1", supplier.get());
    assertEquals("2", supplier.get());
    compiler.resetUniqueNameId();
    supplier = compiler.getUniqueNameIdSupplier();
    assertEquals("0", supplier.get());
  }

  @Test(timeout = 4000)
  public void testNormalizedFlag() {
    Compiler compiler = new Compiler();
    assertFalse(compiler.isNormalized());
    compiler.setNormalized();
    assertTrue(compiler.isNormalized());
    compiler.setUnnormalized();
    assertFalse(compiler.isNormalized());
  }

  @Test(timeout = 4000)
  public void testReportCodeChangeTriggersHandlers() {
    Compiler compiler = new Compiler();
    final int[] count = {0};
    compiler.addChangeHandler(new CodeChangeHandler() {
      @Override
      public void reportChange() {
        count[0]++;
      }
    });
    assertEquals(0, count[0]);
    compiler.reportCodeChange();
    assertEquals(1, count[0]);
    compiler.reportCodeChange();
    assertEquals(2, count[0]);
  }

  @Test(timeout = 4000)
  public void testToSourceAfterParse() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var z = 3;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    compiler.parse();
    String source = compiler.toSource();
    assertNotNull(source);
    assertTrue(source.contains("var z = 3;"));
  }

  @Test(timeout = 4000)
  public void testGetSourceRegionExistentFile() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("region.js", "line1\nline2\nline3") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    Region region = compiler.getSourceRegion("region.js", 2);
    assertNotNull(region);
    assertTrue(region.getSource().contains("line2"));
  }

  @Test(timeout = 4000)
  public void testCompilerOptionsExternExportsPath() {
    Compiler compiler = new Compiler();
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") };
    CompilerOptions options = new CompilerOptions();
    options.externExportsPath = "/tmp/externs.js";
    options.ideMode = true;  // stop before heavy rewriting
    compiler.compile(externs, inputs, options);
    // Should not crash; no assertion on output, just ensure path triggers code path
    assertNotNull(compiler.getResult());
  }

  @Test(timeout = 4000)
  public void testDisableThreads() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    // After disabling threads, runInCompilerThread should not use a new thread.
    // We can trigger a simple operation that uses it: toSource (after init/parse)
    JSSourceFile[] externs = new JSSourceFile[0];
    JSSourceFile[] inputs = new JSSourceFile[] { JSSourceFile.fromCode("test.js", "var a = 1;") };
    CompilerOptions options = new CompilerOptions();
    compiler.init(externs, inputs, options);
    compiler.parse();
    String src = compiler.toSource();
    assertNotNull(src);
  }
}