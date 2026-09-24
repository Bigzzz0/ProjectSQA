/* [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.Compiler
 *
 * 1. Defect-Targeted Zones:
 *    - CrossModuleCodeMotion / Empty Modules: When modules are provided where a dependent module has
 *      no source inputs (empty module), getNodeForCodeInsertion(module) falls back to transitive
 *      dependencies instead of having a module input, or triggers anomalous AST modifications where
 *      reportCodeChange() is called without actual movement.
 *    - Compile pipeline with JSModule[] having an empty dependent module.
 *
 * 2. Core Functional Logic & State Transitions:
 *    - Compiler lifecycle: init(), initOptions(), compile() paths (success, error exit, single-file,
 *      multi-file, module-based).
 *    - State preservation: IntermediateState capture via getState() and restore via setState().
 *    - AST getters & builders: parse(), parseSyntheticCode(), parseTestCode(), toSource(), toSourceArray().
 *    - CodeBuilder operations: append, reset, endsWith, getLength, getLineIndex, getColumnIndex.
 *
 * 3. Boundary Value Analysis (BVA):
 *    - Null / Empty externs, inputs, modules arrays.
 *    - Empty root module check (EMPTY_ROOT_MODULE_ERROR) and empty module list check (EMPTY_MODULE_LIST_ERROR).
 *    - Duplicate input detection: DUPLICATE_INPUT and DUPLICATE_EXTERN_INPUT and DUPLICATE_INPUT_IN_MODULES.
 *    - Cyclic or out-of-order module dependencies: MODULE_DEPENDENCY_ERROR.
 *
 * 4. Defensive Guard Paths & Exceptions:
 *    - Re-compilation prevention: checkState(jsRoot == null) when compile() is called twice.
 *    - PassConfig reassignment guard: setPassConfig() throws IllegalStateException if already assigned.
 *    - ErrorManager null guard: setErrorManager(null) throws Preconditions NPE.
 *    - getNodeForCodeInsertion: null module with empty inputs throws IllegalStateException;
 *      module with empty inputs and root module without inputs throws IllegalStateException.
 *    - newExternInput with conflicting name throws IllegalArgumentException.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;

import static org.junit.Assert.*;

public class CompilerGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompilerDefaultInitialization() {
    Compiler compiler = new Compiler();
    assertNotNull(compiler.getErrorManager());
    assertEquals(0, compiler.getErrorCount());
    assertEquals(0, compiler.getWarningCount());
    assertFalse(compiler.hasErrors());
    assertNull(compiler.getRoot());
    assertFalse(compiler.isNormalized());
  }

  @Test(timeout = 4000)
  public void testCompilerPrintStreamInitialization() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(out);
    Compiler compiler = new Compiler(ps);

    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    assertTrue(compiler.getErrorManager() instanceof PrintStreamErrorManager);
    assertSame(options, compiler.getOptions());
  }

  @Test(timeout = 4000)
  public void testCompileSimpleCodeSuccess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "var window;");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1; function f(x) { return x + a; }");

    Result result = compiler.compile(extern, input, options);
    assertTrue(result.success);
    assertEquals(0, compiler.getErrorCount());
    assertNotNull(compiler.getRoot());
    assertNotNull(compiler.toSource());
  }

  @Test(timeout = 4000)
  public void testToSourceArraySingleAndMultipleInputs() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("in1.js", "var x = 1;"),
        JSSourceFile.fromCode("in2.js", "var y = 2;")
    };

    Result result = compiler.compile(externs, inputs, options);
    assertTrue(result.success);

    String[] sources = compiler.toSourceArray();
    assertNotNull(sources);
    assertEquals(2, sources.length);
    assertTrue(sources[0].contains("x=1") || sources[0].contains("x = 1"));
    assertTrue(sources[1].contains("y=2") || sources[1].contains("y = 2"));
  }

  @Test(timeout = 4000)
  public void testNormalizationFlagLifecycle() {
    Compiler compiler = new Compiler();
    assertFalse(compiler.isNormalized());
    compiler.setNormalized();
    assertTrue(compiler.isNormalized());
    compiler.setUnnormalized();
    assertFalse(compiler.isNormalized());
  }

  @Test(timeout = 4000)
  public void testIntermediateStateSaveAndRestore() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 10;");

    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);
    compiler.parseInputs();

    Compiler.IntermediateState state = compiler.getState();
    assertNotNull(state);

    Compiler compiler2 = new Compiler();
    compiler2.initOptions(options);
    compiler2.setState(state);

    assertSame(state.externsRoot, compiler2.externsRoot);
    assertEquals(compiler.isNormalized(), compiler2.isNormalized());
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
  public void testSymbolTableAcquisition() {
    Compiler compiler = new Compiler();
    SymbolTable table1 = compiler.acquireSymbolTable();
    assertNotNull(table1);
    SymbolTable table2 = compiler.acquireSymbolTable();
    assertSame(table1, table2);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & CodeBuilder Tests
  // =========================================================================

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
    assertFalse(cb.endsWith("baz"));
    assertFalse(cb.endsWith("verylongstring"));

    cb.reset();
    assertEquals(0, cb.getLength());
    assertEquals(1, cb.getLineIndex()); // Line index persists after reset
  }

  @Test(timeout = 4000)
  public void testSourceLineAndRegionBounds() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("test.js", "line1\nline2\nline3\n");
    compiler.init(new JSSourceFile[] { extern }, new JSSourceFile[] { input }, options);

    // Negative and zero line bounds
    assertNull(compiler.getSourceLine("test.js", 0));
    assertNull(compiler.getSourceLine("test.js", -1));
    assertNull(compiler.getSourceRegion("test.js", 0));
    assertNull(compiler.getSourceRegion("test.js", -5));

    // Valid bounds
    assertEquals("line1", compiler.getSourceLine("test.js", 1));
    assertEquals("line2", compiler.getSourceLine("test.js", 2));
    assertNotNull(compiler.getSourceRegion("test.js", 1));

    // Unknown file
    assertNull(compiler.getSourceLine("nonexistent.js", 1));
    assertNull(compiler.getSourceRegion("nonexistent.js", 1));
  }

  @Test(timeout = 4000)
  public void testToSourceWithDelimiterAndLicensing() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.printInputDelimiter = true;
    options.inputDelimiter = "// input: %name% #%num%";
    compiler.initOptions(options);

    Node scriptNode = new Node(Token.SCRIPT);
    scriptNode.putProp(Node.SOURCENAME_PROP, "source_sample.js");
    com.google.javascript.rhino.JSDocInfo info = new com.google.javascript.rhino.JSDocInfo();
    info.setLicense("Apache 2.0 License");
    scriptNode.setJSDocInfo(info);

    Compiler.CodeBuilder cb = new Compiler.CodeBuilder();
    compiler.toSource(cb, 42, scriptNode);
    String output = cb.toString();

    assertTrue(output.contains("// input: source_sample.js #42"));
    assertTrue(output.contains("/*\nApache 2.0 License*/"));
  }

  @Test(timeout = 4000)
  public void testAreNodesEqualForInliningOptionVariants() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node n1 = Node.newString("alpha");
    Node n2 = Node.newString("alpha");
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));

    options.ambiguateProperties = false;
    options.disambiguateProperties = true;
    assertTrue(compiler.areNodesEqualForInlining(n1, n2));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (Empty Modules & Dependency Handling)
  // =========================================================================

  /**
   * Targets the defect where an empty dependent module interacts with
   * code motion and node insertion logic. Verifies that compilation with
   * an empty secondary module does not unexpectedly fail or trigger invalid state.
   */
  @Test(timeout = 4000)
  public void testEmptyDependentModuleCodeMotionAndNodeInsertion() {
    Compiler compiler = new Compiler();
    compiler.disableThreads();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("m1.js", "var x = 1; function foo() { return x; }"));

    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    // m2 is explicitly empty (contains no inputs)

    JSModule[] modules = new JSModule[] { m1, m2 };
    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("externs.js", "")
    };

    // Tracking code changes
    final int[] changeCount = new int[1];
    compiler.addChangeHandler(new CodeChangeHandler() {
      @Override
      public void reportChange() {
        changeCount[0]++;
      }
    });

    Result result = compiler.compile(externs, modules, options);
    assertTrue(result.success);

    // Verify getNodeForCodeInsertion behaves correctly with an empty module
    Node insertionNode = compiler.getNodeForCodeInsertion(m2);
    assertNotNull("Code insertion node must resolve even for empty dependent module", insertionNode);
    assertEquals(Token.SCRIPT, insertionNode.getType());

    String module2Source = compiler.toSource(m2);
    assertEquals("", module2Source);

    String[] moduleSources = compiler.toSourceArray(m2);
    assertEquals(0, moduleSources.length);
  }

  @Test(timeout = 4000)
  public void testCheckFirstModuleErrors() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    // 1. Empty module list
    JSModule[] emptyList = new JSModule[0];
    compiler.init(new JSSourceFile[0], emptyList, options);
    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_EMPTY_MODULE_LIST_ERROR", compiler.getErrors()[0].getType().key);

    // 2. First module is empty
    Compiler compiler2 = new Compiler();
    compiler2.initOptions(options);
    JSModule mRootEmpty = new JSModule("root");
    JSModule[] modules = new JSModule[] { mRootEmpty };
    compiler2.init(new JSSourceFile[0], modules, options);
    assertTrue(compiler2.hasErrors());
    assertEquals(1, compiler2.getErrorCount());
    assertEquals("JSC_EMPTY_ROOT_MODULE_ERROR", compiler2.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testBadModuleDependencyOrderError() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    JSModule m2 = new JSModule("m2");
    // m1 depends on m2, but m1 is listed first in the array -> out of dependency order
    m1.addDependency(m2);
    m1.add(JSSourceFile.fromCode("m1.js", "var a = 1;"));
    m2.add(JSSourceFile.fromCode("m2.js", "var b = 2;"));

    JSModule[] modules = new JSModule[] { m1, m2 };
    compiler.init(new JSSourceFile[0], modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_MODULE_DEPENDENCY_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputInModulesReported() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("shared.js", "var s = 1;"));

    JSModule m2 = new JSModule("m2");
    m2.addDependency(m1);
    m2.add(JSSourceFile.fromCode("shared.js", "var s = 2;"));

    JSModule[] modules = new JSModule[] { m1, m2 };
    compiler.init(new JSSourceFile[0], modules, options);

    assertTrue(compiler.hasErrors());
    assertEquals(1, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_INPUT_IN_MODULES_ERROR", compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDuplicateInputNamesInInputsAndExterns() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSSourceFile[] externs = new JSSourceFile[] {
        JSSourceFile.fromCode("ext.js", "var ext1;"),
        JSSourceFile.fromCode("ext.js", "var ext2;")
    };
    JSSourceFile[] inputs = new JSSourceFile[] {
        JSSourceFile.fromCode("in.js", "var in1;"),
        JSSourceFile.fromCode("in.js", "var in2;")
    };

    compiler.init(externs, inputs, options);
    assertTrue(compiler.hasErrors());
    assertEquals(2, compiler.getErrorCount());
    assertEquals("JSC_DUPLICATE_EXTERN_INPUT", compiler.getErrors()[0].getType().key);
    assertEquals("JSC_DUPLICATE_INPUT", compiler.getErrors()[1].getType().key);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCompileCannotBeCalledTwice() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("extern.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");

    compiler.compile(extern, input, options);
    // Second invocation must throw IllegalStateException
    compiler.compile(extern, input, options);
  }

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

  @Test(timeout = 4000)
  public void testSetPassConfigCannotBeReassigned() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    PassConfig passConfig = new DefaultPassConfig(options);
    compiler.setPassConfig(passConfig);
    assertSame(passConfig, compiler.getPassConfig());

    try {
      compiler.setPassConfig(new DefaultPassConfig(options));
      fail("Expected IllegalStateException on reassignment of PassConfig");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("already been assigned"));
    }
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNewExternInputConflictThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("common.js", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("app.js", "") },
        options);
    compiler.parseInputs();

    compiler.newExternInput("common.js");
  }

  @Test(timeout = 4000)
  public void testNewExternInputSuccess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        new JSSourceFile[] { JSSourceFile.fromCode("ext.js", "") },
        new JSSourceFile[] { JSSourceFile.fromCode("app.js", "") },
        options);
    compiler.parseInputs();

    CompilerInput input = compiler.newExternInput("synthetic_ext.js");
    assertNotNull(input);
    assertTrue(input.isExtern());
    assertSame(input, compiler.getInput("synthetic_ext.js"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetNodeForCodeInsertionNullModuleNoInputsThrows() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(new JSSourceFile[0], new JSSourceFile[0], options);
    compiler.getNodeForCodeInsertion(null);
  }

  @Test(timeout = 4000)
  public void testThrowInternalError() {
    Compiler compiler = new Compiler();
    try {
      compiler.throwInternalError("Custom error message", new RuntimeException("Underlying root cause"));
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("INTERNAL COMPILER ERROR."));
      assertTrue(e.getMessage().contains("Custom error message"));
      assertNotNull(e.getCause());
      assertEquals("Underlying root cause", e.getCause().getMessage());
    }
  }

  // =========================================================================
  // Partition E: Subsystems, Passes & Helper Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testCssRenamingMapAndIdeMode() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.ideMode = true;
    compiler.initOptions(options);

    assertTrue(compiler.isIdeMode());
    assertFalse(compiler.hasHaltingErrors());

    CssRenamingMap map = new CssRenamingMap() {
      @Override
      public String get(String value) {
        return "renamed_" + value;
      }
      @Override
      public CssRenamingMap.Style getStyle() {
        return CssRenamingMap.Style.BY_PART;
      }
    };

    compiler.setCssRenamingMap(map);
    assertSame(map, compiler.getCssRenamingMap());
  }

  @Test(timeout = 4000)
  public void testSyntheticAndTestCodeParsing() {
    Compiler compiler = new Compiler();
    Node n1 = compiler.parseSyntheticCode("synthetic.js", "var z = 50;");
    assertNotNull(n1);
    assertEquals(Token.SCRIPT, n1.getType());

    Node n2 = compiler.parseSyntheticCode("var anonymous = 100;");
    assertNotNull(n2);

    Node n3 = compiler.parseTestCode("var t = 200;");
    assertNotNull(n3);
    assertNotNull(compiler.getInput(" [testcode] "));
  }

  @Test(timeout = 4000)
  public void testTypeRegistryAndValidator() {
    Compiler compiler = new Compiler();
    JSTypeRegistry registry = compiler.getTypeRegistry();
    assertNotNull(registry);
    assertSame(registry, compiler.getTypeRegistry());
    assertNotNull(compiler.getTypeValidator());
    assertNotNull(compiler.getDefaultErrorReporter());
    assertNotNull(compiler.getReverseAbstractInterpreter());
  }

  @Test(timeout = 4000)
  public void testPassLifecycleStartEndPass() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    compiler.startPass("testPass");
    compiler.endPass();

    try {
      compiler.endPass();
      fail("Calling endPass without startPass should fail");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Tracer should not be null"));
    }
  }

  @Test(timeout = 4000)
  public void testComputeCFGAndAstDotGraph() throws Exception {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "function test(a) { if (a) { return 1; } return 0; }");

    compiler.compile(extern, input, options);
    ControlFlowGraph<Node> cfg = compiler.computeCFG();
    assertNotNull(cfg);

    String dotGraph = compiler.getAstDotGraph();
    assertNotNull(dotGraph);
    assertTrue(dotGraph.contains("digraph"));
  }

  @Test(timeout = 4000)
  public void testRebuildInputsFromModules() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    JSModule m1 = new JSModule("m1");
    m1.add(JSSourceFile.fromCode("file1.js", "var a = 1;"));

    JSModule[] modules = new JSModule[] { m1 };
    compiler.init(new JSSourceFile[0], modules, options);

    assertNotNull(compiler.getInput("file1.js"));

    // Dynamically add a second file to the module and rebuild
    m1.add(JSSourceFile.fromCode("file2.js", "var b = 2;"));
    compiler.rebuildInputsFromModules();

    assertNotNull(compiler.getInput("file2.js"));
  }

  @Test(timeout = 4000)
  public void testCustomPassExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    final boolean[] passRan = new boolean[1];
    CompilerPass customPass = new CompilerPass() {
      @Override
      public void process(Node externs, Node root) {
        passRan[0] = true;
      }
    };

    options.customPasses = com.google.common.collect.ArrayListMultimap.create();
    options.customPasses.put(CustomPassExecutionTime.BEFORE_CHECKS, customPass);

    JSSourceFile extern = JSSourceFile.fromCode("externs.js", "");
    JSSourceFile input = JSSourceFile.fromCode("input.js", "var a = 1;");
    compiler.compile(extern, input, options);

    assertTrue("Custom pass should have been executed", passRan[0]);
  }

  @Test(timeout = 4000)
  public void testWarningsGuardFiltering() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();

    // Create a guard that turns everything off
    options.addWarningsGuard(new WarningsGuard() {
      @Override
      public CheckLevel level(JSError error) {
        return CheckLevel.OFF;
      }
    });

    compiler.initOptions(options);
    JSError error = JSError.make("test.js", 1, 1, CheckLevel.ERROR, Compiler.OPTIMIZE_LOOP_ERROR, "10");
    compiler.report(error);

    assertEquals(0, compiler.getErrorCount());
    assertFalse(compiler.hasErrors());
  }

  @Test(timeout = 4000)
  public void testCodingConventionDefaultAndCustom() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    assertTrue(compiler.getCodingConvention() instanceof GoogleCodingConvention);

    CodingConvention custom = new ClosureCodingConvention();
    options.setCodingConvention(custom);
    assertSame(custom, compiler.getCodingConvention());
  }

  @Test(timeout = 4000)
  public void testLoggingLevelConfiguration() {
    java.util.logging.Level originalLevel = java.util.logging.Logger.getLogger("com.google.javascript.jscomp").getLevel();
    try {
      Compiler.setLoggingLevel(java.util.logging.Level.FINEST);
      assertEquals(java.util.logging.Level.FINEST, java.util.logging.Logger.getLogger("com.google.javascript.jscomp").getLevel());
    } finally {
      Compiler.setLoggingLevel(originalLevel);
    }
  }
}